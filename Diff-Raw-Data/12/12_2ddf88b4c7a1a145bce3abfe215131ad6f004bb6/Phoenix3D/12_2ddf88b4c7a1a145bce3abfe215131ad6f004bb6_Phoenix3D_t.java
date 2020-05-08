 package de.haw.hamburg.inf.pro.ws11.petersenwienrich;
 
 import static javax.media.opengl.GL.GL_ARRAY_BUFFER_ARB;
 import static javax.media.opengl.GL.GL_DYNAMIC_COPY_ARB;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import javax.media.opengl.GL;
 
 import msa.opencl.OpenCL;
 import msa.opencl.OpenCLBuffer;
 import msa.opencl.OpenCLKernel;
 import processing.core.PApplet;
 import processing.core.PImage;
 import processing.core.PVector;
 import processing.opengl.PGraphicsOpenGL;
 import SimpleOpenNI.SimpleOpenNI;
 
 import com.nativelibs4java.opencl.CLMem;
 
 public class Phoenix3D extends PApplet {
 
     private static final long serialVersionUID = 1L;
 
     // ParticleConstants
     private final int NUM_PARTICLES = 1024 * 128; // 1024 * 256;
     private final int PARTICLE_NFLOAT = 8;
     private final int PARTICLEFLOATSIZE = NUM_PARTICLES * PARTICLE_NFLOAT;
     private final int PARTICLEBYTESIZE = PARTICLEFLOATSIZE * 4;
 
     // Modes
     private static final int MODE_STATIC = 0;
     private static final int MODE_GRAVITY = 1;
     private static final int MODE_WEIGHTLESS = 2;
     private static final int MODE_NEG_WEIGHTLESS = 3;
     private static final int MODE_RANDOM = 4;
     private static final int MODE_PLANETARY = 5;
     private static final int NMODES = 6;
 
     private int mayorMode = MODE_STATIC;
 
     private static final int GRAVITY_FALLING = 102;
     private static final int GRAVITY_EXPLODE = 103;
     private int gravityMode = GRAVITY_EXPLODE;
 
     // Ressources Location
     private static final String CL_RESSOURCE = Config.CL_LOCATION;
     private static final String IMG_RESSOURCE = Config.IMAGES_LOCATION;
 
     // Open*L Objects
     private OpenCL openCL;
     private PGraphicsOpenGL pgl;
     private GL gl;
     private OpenCLKernel clKernel;
 
     // FrameCounter
     private final FrequencyCounter fpsCounter = new FrequencyCounter();
 
     // Particle Buffers
     private Particle3D[] particle;
     private ByteBuffer particleBytes;
     private FloatBuffer particleBuffer;
     private OpenCLBuffer clMemParticles;
     private int[] VBOParticleBuffer;
 
     // ColorBuffers
     private int[] particleColor;
     private ByteBuffer colorBytes;
     private FloatBuffer colorBuffer;
     private OpenCLBuffer clMemColors;
     private int[] VBOColorBuffer;
 
     // Center of Mass Buffers
     private PVector[] userCenters = null;
     private ByteBuffer comBytes;
     private FloatBuffer comBuffer;
     private OpenCLBuffer clMemCom;
     private static final int MAX_USERS = 10;
 
     // SimpleOpenNI Objects
     private SimpleOpenNI ni;
     private float camZoomF = 0.24f;
     private float camRotX = radians(180) - 0.0f; // by default rotate the whole
                                                  // scene 180deg around
     // the x-axis,
     // the data from openni comes upside down
     private float camRotY = radians(0);
     private float camRotTick;
 
     // Moved Particle Counter
     private int tmpParticleNumber;
 
     // draw counter
     private int drawCount;
 
     // for the user colors
     private PImage rgbImg;
     private PImage backgroundImg;
     private PImage floorImg;
 
     // stopwatch
     long timestamp;
 
     private float floorLevel;
     private float leftWall;
     private float rightWall;
     private float backWall;
     private float backGroundWall;
 
     private int moveBackCounter;
 
     private final int[] texture = new int[2];
 
     @Override
     public void setup() {
 
         // SimpleOpenNI Setup
         timestamp = millis();
         ni = new SimpleOpenNI(this, SimpleOpenNI.RUN_MODE_MULTI_THREADED);
         ni.setMirror(false);
         ni.enableDepth();
         ni.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
         // ni.enableScene();
        // ni.enableRGB(ni.depthWidth(), ni.depthHeight(), 30);
        ni.enableRGB();
         ni.alternativeViewPointDepthToImage();
         timestamp = millis() - timestamp;
         System.out.println("SimpleOpenNI Setup time: " + timestamp + " ms");
 
         // initialize Processing window:
         timestamp = millis();
         size(1024, 768, OPENGL);
         frameRate(30);
         timestamp = millis() - timestamp;
         System.out.println("Processing Setup time: " + timestamp + " ms");
 
         floorLevel = -(height + 500);
         leftWall = -3000;
         rightWall = 3000;
         backWall = 4000;
         backGroundWall = 4000;
 
         backgroundImg = loadImage(IMG_RESSOURCE + "background2.jpg");
         floorImg = loadImage(IMG_RESSOURCE + "floor2.jpg");
 
         // initialize GL object
         timestamp = millis();
         pgl = (PGraphicsOpenGL) g;
         gl = pgl.beginGL();
 
         gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
         gl.glEnable(GL.GL_DEPTH_TEST);
 
         gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
 
         gl.glGenTextures(2, texture, 0);
         gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0]);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
         gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, backgroundImg.width, backgroundImg.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
                 IntBuffer.wrap(backgroundImg.pixels));
         gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
         gl.glBindTexture(GL.GL_TEXTURE_2D, texture[1]);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
         gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
         gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, floorImg.width, floorImg.height, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
                 IntBuffer.wrap(floorImg.pixels));
         gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
 
         // gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0]);
         // gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, backgroundImg.width,
         // backgroundImg.height, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE,
         // IntBuffer.wrap(backgroundImg.pixels));
         // gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
 
         timestamp = millis() - timestamp;
         System.out.println("OpenGL Setup time: " + timestamp + " ms");
 
         // init openCL
         timestamp = millis();
         openCL = OpenCL.getSingleton();
         openCL.setupFromOpenGL();
         timestamp = millis() - timestamp;
         System.out.println("OpenCL Setup time: " + timestamp + " ms");
 
         System.out.println("Interoperability: " + openCL.isSharingContext());
 
         tmpParticleNumber = 0;
 
         // init particle buffer
         timestamp = millis();
         particle = new Particle3D[NUM_PARTICLES];
         particleBytes = openCL.createByteBuffer(PARTICLEBYTESIZE);
         particleBuffer = particleBytes.asFloatBuffer();
 
         // init color buffer
         particleColor = new int[NUM_PARTICLES];
         colorBytes = openCL.createByteBuffer(NUM_PARTICLES * 4 * OpenCL.SIZEOF_FLOAT);
         colorBuffer = colorBytes.asFloatBuffer();
 
         // init CoM Buffer
         comBytes = openCL.createByteBuffer(MAX_USERS * 3 * OpenCL.SIZEOF_FLOAT);
         comBuffer = comBytes.asFloatBuffer();
 
         timestamp = millis() - timestamp;
         System.out.println("OpenCL Buffer Setup time: " + timestamp + " ms");
 
         timestamp = millis();
         for (int i = 0; i < NUM_PARTICLES; i++) {
             particle[i] = new Particle3D(0, floorLevel, 0, 0, 0, 0);
             particleBuffer.put(particle[i].x);
             particleBuffer.put(particle[i].y);
             particleBuffer.put(particle[i].z);
             particleBuffer.put(particle[i].velX);
             particleBuffer.put(particle[i].velY);
             particleBuffer.put(particle[i].velZ);
             particleColor[i] = color(255);
             colorBuffer.put(red(particleColor[i]));
             colorBuffer.put(green(particleColor[i]));
             colorBuffer.put(blue(particleColor[i]));
             colorBuffer.put(0);
         }
 
         particleBuffer.rewind();
         colorBuffer.rewind();
         timestamp = millis() - timestamp;
         System.out.println("OpenCL Buffer Fill time: " + timestamp + " ms");
 
         timestamp = millis();
         VBOParticleBuffer = new int[1];
         // This does nothing more than create a handle (like a link) to a
         // buffer.
         gl.glGenBuffersARB(1, VBOParticleBuffer, 0);
         // This attaches our handle to the array buffer. In essence, an array
         // buffer is a buffer used to store the
         // vertex Positions and Speeds for OpenCL.
         gl.glBindBufferARB(GL_ARRAY_BUFFER_ARB, VBOParticleBuffer[0]);
         // This allocates the memory needed to store our point data, and also
         // fills it with our point data (variable:
         // particleBuffer);
         gl.glBufferDataARB(GL_ARRAY_BUFFER_ARB, PARTICLEBYTESIZE, particleBuffer, GL_DYNAMIC_COPY_ARB);
         // detach handle from Buffer
         gl.glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
 
         VBOColorBuffer = new int[1];
         gl.glGenBuffersARB(1, VBOColorBuffer, 0);
         gl.glBindBufferARB(GL_ARRAY_BUFFER_ARB, VBOColorBuffer[0]);
         gl.glBufferDataARB(GL_ARRAY_BUFFER_ARB, PARTICLEBYTESIZE, colorBuffer, GL_DYNAMIC_COPY_ARB);
         gl.glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0);
         timestamp = millis() - timestamp;
         System.out.println("OpenCL VBO Setup time: " + timestamp + " ms");
 
         clMemParticles = openCL.createBufferFromGLObject(VBOParticleBuffer[0], CLMem.Usage.InputOutput);
         clMemColors = openCL.createBufferFromGLObject(VBOColorBuffer[0], CLMem.Usage.InputOutput);
 
         clMemCom = new OpenCLBuffer();
         clMemCom.initBuffer(MAX_USERS * 3 * OpenCL.SIZEOF_FLOAT, CLMem.Usage.InputOutput, comBuffer);
 
         System.out.println("Load particle program");
         openCL.loadProgramFromFile(dataPath(CL_RESSOURCE + "Particle3D.cl"));
         clKernel = openCL.loadKernel("updateParticle");
 
         clKernel.setArg(0, clMemParticles.getCLMem());
         clKernel.setArg(1, clMemColors.getCLMem());
         clKernel.setArg(2, clMemCom.getCLMem());
         clKernel.setArg(3, floorLevel);
         clKernel.setArg(4, leftWall);
         clKernel.setArg(5, rightWall);
         clKernel.setArg(6, backWall);
         clKernel.setArg(7, mayorMode);
 
         gl.glPointSize(4);
         pgl.endGL();
         perspective(95, width / height, 10, 150000);
 
     }
 
     private void updateMayorMode() {
         /* ################################################ */
         // Sequential all modes
         // if (mayorMode == MODE_STATIC) {
         // if (moveBackCounter >= 100) {
         // mayorMode = MODE_GRAVITY;
         // gravityMode = GRAVITY_EXPLODE;
         // System.out.println("Explode");
         // moveBackCounter = 0;
         // drawCount = 0;
         // } else {
         // moveBackCounter++;
         // }
         // } else if (mayorMode == MODE_GRAVITY && gravityMode ==
         // GRAVITY_EXPLODE) {
         // if (moveBackCounter >= 150) {
         // mayorMode = MODE_RANDOM;
         // System.out.println("Random");
         // moveBackCounter = 0;
         // drawCount = 0;
         // } else {
         // moveBackCounter++;
         // }
         // } else if (mayorMode == MODE_RANDOM) {
         // if (moveBackCounter >= 500) {
         // mayorMode = MODE_WEIGHTLESS;
         // System.out.println("Weightless");
         // moveBackCounter = 0;
         // drawCount = 0;
         // } else {
         // moveBackCounter++;
         // }
         // } else if (mayorMode == MODE_WEIGHTLESS) {
         // if (moveBackCounter >= 150) {
         // mayorMode = MODE_NEG_WEIGHTLESS;
         // System.out.println("Negativ Weightless");
         // moveBackCounter = 0;
         // drawCount = 0;
         // } else {
         // moveBackCounter++;
         // }
         // } else if (mayorMode == MODE_NEG_WEIGHTLESS) {
         // if (moveBackCounter >= 1) {
         // mayorMode = MODE_GRAVITY;
         // gravityMode = GRAVITY_FALLING;
         // System.out.println("Falling");
         // moveBackCounter = 0;
         // drawCount = 0;
         // } else {
         // moveBackCounter++;
         // }
         // }
 
         /* ################################################ */
         // First Try
 
         if (mayorMode == MODE_WEIGHTLESS) {
             if (moveBackCounter >= 80) {
                 mayorMode = MODE_NEG_WEIGHTLESS;
                 moveBackCounter = 0;
             } else {
                 moveBackCounter++;
             }
         } else if (mayorMode == MODE_NEG_WEIGHTLESS) {
             if (moveBackCounter >= 1) {
                 mayorMode = MODE_GRAVITY;
                 gravityMode = GRAVITY_FALLING;
                 moveBackCounter = 0;
             } else {
                 moveBackCounter++;
             }
         }
         /* ################################################ */
     }
 
     @Override
     public void draw() {
 
         PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
         ni.update();
 
         fpsCounter.triggerCount();
 
         // set the cam position
         translate(width / 2, height / 2, 0);
         // camRotTick = (camRotTick + 0.1f) % 12.8f;
         // rotX = radians(180) + radians(sin(2 * tick));
         // rotY = radians(0) + 10 * radians(cos(tick / 2));
         rotateX(camRotX);
         rotateY(camRotY);
         scale(camZoomF);
 
         rgbImg = ni.rgbImage();
 
         int pointCloudSteps = 2; // to speed up the drawing, draw every third
                                  // point
                                  // drawBackground();
         int userCount = ni.getNumberOfUsers();
         if (userCount > MAX_USERS) {
             userCount = MAX_USERS;
         }
 
         int[] userMap = null;
         int lastChangedParticleNr = tmpParticleNumber;
         int firstChangedParticleNr = tmpParticleNumber;
         if (userCount > 0) {
             userMap = ni.getUsersPixels(SimpleOpenNI.USERS_ALL);
             userCenters = new PVector[userCount];
             // calculate mass centers
             for (int i = 0; i < userCount; i++) {
                 userCenters[i] = new PVector();
                 ni.getCoM(i + 1, userCenters[i]);
             }
             updateComVBO(userCenters);
 
             if (mayorMode == MODE_STATIC) {
                 drawBackground();
                 drawStaticPoints(pgl, pointCloudSteps, userMap);
                 lastChangedParticleNr = tmpParticleNumber;
             } else if (mayorMode == MODE_GRAVITY || mayorMode == MODE_WEIGHTLESS || mayorMode == MODE_NEG_WEIGHTLESS) {
                 createFreeMovingParticles(pointCloudSteps, userMap);
                 lastChangedParticleNr = tmpParticleNumber;
                 drawBackground();
             } else if (mayorMode == MODE_RANDOM) {
                 makeAllParticlesRandom();
                 lastChangedParticleNr = tmpParticleNumber;
                 drawBackground();
             } else if (mayorMode == MODE_PLANETARY) {
                 drawBackground();
             }
 
             updateMayorMode();
 
         } else {
             drawBackground();
 
         }
         Update(firstChangedParticleNr, lastChangedParticleNr);
 
         drawRoom();
         Render();
         drawCenterOfMass(userCenters);
 
         System.out.println("FPS:" + fpsCounter.getFrequency());
     }
 
     private void updateComVBO(PVector[] centers) {
         if (centers != null) {
             for (int i = 0; i < centers.length; i++) {
                 comBuffer.put(i + 0, centers[i].x);
                 comBuffer.put(i + 1, centers[i].y);
                 comBuffer.put(i + 2, centers[i].z);
             }
             for (int i = centers.length; i < MAX_USERS; i++) {
                 comBuffer.put(i + 0, 0);
                 comBuffer.put(i + 1, 0);
                 comBuffer.put(i + 2, 0);
             }
         }
     }
 
     private void drawCenterOfMass(PVector[] centers) {
         if (centers != null) {
             gl = pgl.beginGL();
            gl.glPointSize(8);
             gl.glBegin(GL.GL_POINTS);
             for (int i = 0; i < centers.length; i++) {
                 gl.glColor3f(0, 255, 0);
                 gl.glVertex3f(centers[i].x, centers[i].y, centers[i].z);
             }
             gl.glEnd();
             gl.glPointSize(4);
         }
     }
 
     private void makeAllParticlesRandom() {
 
         if (drawCount <= 0) {
 
             for (int i = 0; i < NUM_PARTICLES; i++) {
                 if (tmpParticleNumber >= NUM_PARTICLES) {
                     tmpParticleNumber = 0;
                 }
                 particle[i].y = floorLevel;
                 float a = random(80, 100);
                 float b = random(0, 360);
                 particle[tmpParticleNumber].velX = 20 * sin(a) * cos(b);
                 particle[tmpParticleNumber].velY = 20 * cos(a);
                 particle[tmpParticleNumber].velZ = 20 * sin(a) * sin(b);
                 updateVBOParticle(tmpParticleNumber);
 
                 // paintParticle(color(165, 180, 10), i);
                 tmpParticleNumber++;
             }
             drawCount++;
         }
     }
 
     private void createFreeMovingParticles(int pointCloudSteps, int[] userMap) {
         PVector realWorldPoint;
         if (userMap != null) {
             if (drawCount <= 0) {
                 for (int i = 0; i < userMap.length; i += pointCloudSteps) {
                     realWorldPoint = ni.depthMapRealWorld()[i];
                     int x = i % ni.depthWidth();
                    int y = (i - x) / ni.depthWidth();
                     // check if there is a user
                     if (userMap[i] != 0) {
                         createGravityParticle(realWorldPoint.x + random(-2, 2), realWorldPoint.y + random(-2, 2), realWorldPoint.z
                                 + random(-2, 2), x, y);
                     }
                 }
             }
             drawCount++;
         }
     }
 
     private void drawStaticPoints(PGraphicsOpenGL pgl, int pointCloudSteps, int[] userMap) {
         PVector realWorldPoint;
         if (userMap != null) {
             gl = pgl.beginGL();
             gl.glEnable(GL.GL_POINTS);
             gl.glBegin(GL.GL_POINTS);
             // colorMode(RGB, 255);
             for (int i = 0; i < userMap.length; i += pointCloudSteps) {
                 realWorldPoint = ni.depthMapRealWorld()[i];
                 int x = i % ni.depthWidth();
                int y = (i - x) / ni.depthWidth();
                 // check if there is a user
                 if (userMap[i] != 0) {
                     // gl.glColor3f(255, 255, 255);
                     // gl.glColor3f(norm(random(255), 0, 255),
                     // norm(random(255), 0, 255), norm(random(255), 0,
                     // 255));
                     gl.glColor3f(norm(red(rgbImg.get(x, y)), 0, 255), norm(green(rgbImg.get(x, y)), 0, 255),
                             norm(blue(rgbImg.get(x, y)), 0, 255));
 
                     // gl.glColor3f(1.0f, 0, 0);
                     gl.glVertex3f(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);
                 }
             }
 
             gl.glEnd();
             gl.glDisable(GL.GL_POINTS);
             pgl.endGL();
         }
     }
 
     public void drawBackground() {
         background(0);
         gl = pgl.beginGL();
         {
             gl.glEnable(GL.GL_TEXTURE_2D);
             // gl.glActiveTexture(GL.GL_TEXTURE0);
             gl.glBindTexture(GL.GL_TEXTURE_2D, texture[0]);
             gl.glColor3f(255, 255, 255);
 
             gl.glBegin(GL.GL_QUADS);
             {
                 gl.glTexCoord2f(0f, 0f);
                 gl.glVertex3f(-(backgroundImg.width * 3), (backgroundImg.height * 3), backGroundWall);
                 gl.glTexCoord2f(1f, 0f);
                 gl.glVertex3f((backgroundImg.width * 3), (backgroundImg.height * 3), backGroundWall);
                 gl.glTexCoord2f(1f, 1f);
                 gl.glVertex3f((backgroundImg.width * 3), -(backgroundImg.height * 3), backGroundWall);
                 gl.glTexCoord2f(0f, 1f);
                 gl.glVertex3f(-(backgroundImg.width * 3), -(backgroundImg.height * 3), backGroundWall);
             }
             gl.glEnd();
             gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
 
             {
                 gl.glEnable(GL.GL_TEXTURE_2D);
                 // gl.glActiveTexture(GL.GL_TEXTURE1);
                 gl.glBindTexture(GL.GL_TEXTURE_2D, texture[1]);
 
                 gl.glBegin(GL.GL_QUADS);
                 gl.glTexCoord2f(0f, 0f);
                 gl.glVertex3f(-(backgroundImg.width * 3), floorLevel, backGroundWall);
                 gl.glTexCoord2f(1f, 0f);
                 gl.glVertex3f((backgroundImg.width * 3), floorLevel, backGroundWall);
                 gl.glTexCoord2f(1f, 1f);
                 gl.glVertex3f((backgroundImg.width * 3), floorLevel, 0);
                 gl.glTexCoord2f(0f, 1f);
                 gl.glVertex3f(-(backgroundImg.width * 3), floorLevel, 0);
                 gl.glDisable(GL.GL_TEXTURE_2D);
             }
             gl.glEnd();
             gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
         }
         pgl.endGL();
     }
 
     public void drawRoom() {
         gl = pgl.beginGL();
         float c = 255;
         // line floor
         gl.glBegin(GL.GL_LINE_LOOP);
         {
             gl.glColor3f(c, c, c);
             gl.glTexCoord2f(0, 0);
             gl.glVertex3f(leftWall, floorLevel + .5f, 0);
             gl.glTexCoord2f(1, 0);
             gl.glVertex3f(rightWall, floorLevel + .5f, 0);
             gl.glTexCoord2f(1, 1);
             gl.glVertex3f(rightWall, floorLevel + .5f, backWall);
             gl.glTexCoord2f(0, 1);
             gl.glVertex3f(leftWall, floorLevel + .5f, backWall);
         }
         gl.glEnd();
 
         // line room
         gl.glBegin(GL.GL_LINES);
         {
             gl.glColor3f(c, c, c);
             gl.glVertex3f(leftWall, floorLevel + .5f, 0);
             gl.glVertex3f(leftWall, 2000, 0);
             gl.glVertex3f(leftWall, floorLevel + .5f, backWall);
             gl.glVertex3f(leftWall, 2000, backWall);
             gl.glVertex3f(rightWall, floorLevel + .5f, 0);
             gl.glVertex3f(rightWall, 2000, 0);
             gl.glVertex3f(rightWall, floorLevel + .5f, backWall);
             gl.glVertex3f(rightWall, 2000, backWall);
         }
         gl.glEnd();
 
         pgl.endGL();
     }
 
     public void createWeightlessParticle(float x, float y, float z, int realX, int realY) {
         if (tmpParticleNumber >= NUM_PARTICLES) {
             tmpParticleNumber = 0;
         }
         createWeightlessParticle(x, y, z, realX, realY, tmpParticleNumber);
         tmpParticleNumber++;
     }
 
     public void createWeightlessParticle(float x, float y, float z, int realX, int realY, int particleNumber) {
         if (z <= 10) {
             return;
         }
 
         particle[particleNumber].x = x + random(-1.0f, 1.0f);
         particle[particleNumber].y = y + random(-1.0f, 1.0f);
         particle[particleNumber].z = z + random(-1.0f, 1.0f);
 
         if (mayorMode == MODE_WEIGHTLESS) {
             float a = random(0, 180);
             float b = random(0, 360);
             particle[particleNumber].velX = 3 * sin(a) * cos(b);
             particle[particleNumber].velY = 3 * cos(a);
             particle[particleNumber].velZ = 3 * sin(a) * sin(b);
         }
 
         updateVBOParticle(particleNumber);
         paintParticle(realX, realY, particleNumber);
     }
 
     public void createGravityParticle(float x, float y, float z, int realX, int realY) {
         if (tmpParticleNumber >= NUM_PARTICLES) {
             tmpParticleNumber = 0;
         }
         createGravityParticle(x, y, z, realX, realY, tmpParticleNumber);
         tmpParticleNumber++;
     }
 
     public void createGravityParticle(float x, float y, float z, int realX, int realY, int particleNumber) {
         if (z <= 10) {
             return;
         }
 
         particle[particleNumber].x = x + random(-1.0f, 1.0f);
         particle[particleNumber].y = y + random(-1.0f, 1.0f);
         particle[particleNumber].z = z + random(-1.0f, 1.0f);
 
         if (gravityMode == GRAVITY_FALLING) {
             // falling mode
             float a = random(0, 180);
             float b = random(0, 360);
             particle[particleNumber].velX = sin(a) * cos(b);
             particle[particleNumber].velY = cos(a);
             particle[particleNumber].velZ = sin(a) * sin(b);
         } else if (gravityMode == GRAVITY_EXPLODE) {
             // explode mode
             float a = random(0, 180);
             float b = random(0, 360);
             particle[particleNumber].velX = 50 * sin(a) * cos(b);
             particle[particleNumber].velY = 50 * cos(a);
             particle[particleNumber].velZ = 50 * sin(a) * sin(b);
         }
         updateVBOParticle(particleNumber);
         paintParticle(realX, realY, particleNumber);
 
     }
 
     private void paintParticle(int realX, int realY, int particleNumber) {
         paintParticle(rgbImg.get(realX, realY), particleNumber);
     }
 
     private void paintParticle(int color, int particleNumber) {
         particleColor[particleNumber] = color;
 
         colorBuffer.put(particleNumber * 4 + 0, norm(red(particleColor[particleNumber]), 0, 255));
         colorBuffer.put(particleNumber * 4 + 1, norm(green(particleColor[particleNumber]), 0, 255));
         colorBuffer.put(particleNumber * 4 + 2, norm(blue(particleColor[particleNumber]), 0, 255));
         colorBuffer.put(particleNumber * 4 + 3, norm(alpha(particleColor[particleNumber]), 0, 255));
     }
 
     void Render() {
         gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOColorBuffer[0]);
         gl.glEnableClientState(GL.GL_COLOR_ARRAY);
         gl.glColorPointer(4, GL.GL_FLOAT, 0, 0);
 
         gl = pgl.beginGL();
         {
             openCL.finish();
 
             gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOParticleBuffer[0]);
 
             gl.glPushMatrix();
             gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
             gl.glVertexPointer(3, GL.GL_FLOAT, 32, 0);
             gl.glDrawArrays(GL.GL_POINTS, 0, NUM_PARTICLES);
             gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
             gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
             gl.glDisableClientState(GL.GL_COLOR_ARRAY);
             gl.glPopMatrix();
         }
         pgl.endGL();
 
     }
 
     void Update(int firstPart, int lastPart) {
 
         openCL.acquireGLObjects();
 
         gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOColorBuffer[0]);
         gl.glBufferDataARB(GL_ARRAY_BUFFER_ARB, PARTICLEBYTESIZE, colorBuffer, GL_DYNAMIC_COPY_ARB);
         gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
 
         clMemParticles.write(particleBytes, 0, PARTICLEBYTESIZE, true);
         clMemCom.write(comBytes, 0, MAX_USERS * 3 * OpenCL.SIZEOF_FLOAT, true);
 
         try {
             long wgs = 0;
             clKernel.setArg(0, clMemParticles.getCLMem());
             clKernel.setArg(1, clMemColors.getCLMem());
             clKernel.setArg(2, clMemCom.getCLMem());
             clKernel.setArg(3, floorLevel);
             clKernel.setArg(4, leftWall);
             clKernel.setArg(5, rightWall);
             clKernel.setArg(6, backWall);
             clKernel.setArg(7, mayorMode);
 
             clKernel.run1D(NUM_PARTICLES, (int) wgs);
         } catch (Throwable ex) {
             ex.printStackTrace();
             System.exit(1);
         }
 
         clMemParticles.read(particleBytes, 0, PARTICLEBYTESIZE, true);
         particleBuffer = particleBytes.asFloatBuffer();
 
         clMemColors.read(colorBytes, 0, NUM_PARTICLES * 4 * 4, true);
         colorBuffer = colorBytes.asFloatBuffer();
 
         openCL.releaseGLObjects();
     }
 
     private void updateVBOParticle(int particleNumber) {
         particleBuffer.put(particleNumber * PARTICLE_NFLOAT + 0, particle[particleNumber].x);
         particleBuffer.put(particleNumber * PARTICLE_NFLOAT + 1, particle[particleNumber].y);
         particleBuffer.put(particleNumber * PARTICLE_NFLOAT + 2, particle[particleNumber].z);
         particleBuffer.put(particleNumber * PARTICLE_NFLOAT + 3, particle[particleNumber].velX);
         particleBuffer.put(particleNumber * PARTICLE_NFLOAT + 4, particle[particleNumber].velY);
         particleBuffer.put(particleNumber * PARTICLE_NFLOAT + 5, particle[particleNumber].velZ);
     }
 
     // Keyboard events
 
     @Override
     public void mousePressed() {
         if (mayorMode == MODE_STATIC && gravityMode == GRAVITY_EXPLODE) {
             mayorMode = MODE_WEIGHTLESS;
             gravityMode = GRAVITY_FALLING;
         } else if (mayorMode == MODE_GRAVITY && gravityMode == GRAVITY_FALLING) {
             mayorMode = MODE_STATIC;
             drawCount = 0;
         } else if (mayorMode == MODE_STATIC && gravityMode == GRAVITY_FALLING) {
             mayorMode = MODE_GRAVITY;
             gravityMode = GRAVITY_EXPLODE;
         } else if (mayorMode == MODE_GRAVITY && gravityMode == GRAVITY_EXPLODE) {
             mayorMode = MODE_RANDOM;
             drawCount = 0;
         } else if (mayorMode == MODE_RANDOM) {
             mayorMode = MODE_PLANETARY;
             drawCount = 0;
         } else if (mayorMode == MODE_PLANETARY) {
             mayorMode = MODE_STATIC;
             drawCount = 0;
         }
         // mayorMode = (mayorMode + 1) % NMODES;
         // drawCount = 0;
         // System.out.println("Mode: " + mayorMode);
     }
 
     @Override
     public void keyPressed() {
         switch (key) {
         case ' ':
             ni.setMirror(!ni.mirror());
             break;
         case 'e':
             gravityMode = GRAVITY_EXPLODE;
             break;
         case 'f':
             gravityMode = GRAVITY_FALLING;
             break;
         }
 
         switch (keyCode) {
 
         case LEFT:
             camRotY += 0.1f;
             break;
         case RIGHT:
             // zoom out
             camRotY -= 0.1f;
             break;
         case UP:
             if (keyEvent.isShiftDown())
                 camZoomF += 0.01f;
             else
                 camRotX += 0.1f;
             break;
         case DOWN:
             if (keyEvent.isShiftDown()) {
                 camZoomF -= 0.01f;
                 if (camZoomF < 0.01)
                     camZoomF = 0.01f;
             } else
                 camRotX -= 0.1f;
             break;
         }
     }
 }
