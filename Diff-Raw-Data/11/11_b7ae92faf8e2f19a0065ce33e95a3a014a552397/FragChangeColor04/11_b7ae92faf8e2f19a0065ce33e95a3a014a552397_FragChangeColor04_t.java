 package display;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL12.*;
 import static org.lwjgl.opengl.GL13.*;
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.opengl.GL20.*;
 import static org.lwjgl.opengl.GL30.*;
 import static org.lwjgl.opengl.GL32.*;
 
 import org.lwjgl.BufferUtils;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import texturestuff.Texture;
 import data.Toggle;
 import data.TwoDModel;
 import data.Location;
 import data.Rectangle;
 import data.StaticEntity;
 import data.Entity;
 import data.ResourceLoader;
 import data.UIEntity;
 import data.World;
 import data.WorldCuller;
 import framework.Framework;
 
 public class FragChangeColor04 extends LWJGLWindow {
 
 
 	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 	@Override
 	protected void init() {
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glEnable( GL_BLEND );
 		rl=new ResourceLoader();
 		
 		
 		//fbo = new FrameBuffer(width, height, Texture.NEAREST);
 		
 		
 		
 		FramebufferName = glGenFramebuffers( );
 		glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName);
 		//texture code
 		// The texture we're going to render to
 		renderedTexture=glGenTextures();
 		
 		// "Bind" the newly created texture : all future texture functions will modify this texture
 		glBindTexture(GL_TEXTURE_2D, renderedTexture);
 
 		// Give an empty image to OpenGL ( the last "0" means "empty" )
 		bb=BufferUtils.createByteBuffer(500*500*3);
 		glTexImage2D(GL_TEXTURE_2D, 0,GL_RGB, 500, 500, 0,GL_RGB, GL_UNSIGNED_BYTE,bb);
 
 		// Poor filtering
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); 
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
 		
 		//int depthrenderbuffer=glGenRenderbuffers();
 		//glBindRenderbuffer(GL_RENDERBUFFER, depthrenderbuffer);
 		//glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, 1024, 768);
 		//glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthrenderbuffer);
 
 		// Set "renderedtexture" as our colour attachement #0
 		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, renderedTexture, 0);
 		 
 		// Set the list of draw buffers.
 		//enum DrawBuffers[2] = {GL_COLOR_ATTACHMENT0};
 		//DrawBuffers[2] = {GL_COLOR_ATTACHMENT0;}
 		//glDrawBuffers(1); // "1" is the size of DrawBuffers
 		// Set the list of draw buffers.
 		IntBuffer DrawBuffers = BufferUtils.createIntBuffer(1);
 		DrawBuffers.put(GL_COLOR_ATTACHMENT0);
 		glDrawBuffers( 1); // "1" is the size of DrawBuffers
 		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE){
 			try {
 				System.out.println("shit is broken try line 84ish near the sleep statement");
 				Thread.sleep(5000);
 				
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 		}
 		final float g_quad_vertex_buffer_data[] = { 
 			-1.0f, -1.0f, 0.0f,1f,
 			-1.0f,  1.0f, 0.0f,1f,
 			 1.0f,  1.0f, 0.0f,1f,
 			 1.0f, -1.0f, 0.0f,1f
 		};
 		fullscreenQuad=BufferUtils.createFloatBuffer(g_quad_vertex_buffer_data.length);
 		fullscreenQuad.put(g_quad_vertex_buffer_data);
 		fullscreenQuad.flip();
 		screenpositionBufferObject = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, screenpositionBufferObject);
 		glBufferData(GL_ARRAY_BUFFER, fullscreenQuad, GL_STATIC_DRAW);
 		
 		
 		
 		
 		
 		
 		
 		
 		float[] UNIT_TEXTURE_ARRAY={0.0f,0.0f,1.0f,0.0f,1.0f,1.0f,0f,1.0f};
 		FloatBuffer tFlo=BufferUtils.createFloatBuffer(UNIT_TEXTURE_ARRAY.length);
 		tFlo.put(UNIT_TEXTURE_ARRAY);
 		tFlo.flip();
 		rl.addFloatBuffer("ALLFLOAT", tFlo);
 		int bo=glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, bo);
 		glBufferData(GL_ARRAY_BUFFER, tFlo, GL_STREAM_DRAW);
 		rl.addBuffer("ALLBUFFER", bo);
 		initializeProgram();
 		initializeVertexBuffer(); 
 		vao = glGenVertexArrays();
 		xlock=new Toggle(Keyboard.KEY_X);
 		ylock=new Toggle(Keyboard.KEY_Y);
 		clicked=false;
 		unclicked=true;
 		updown=false;
 		leftdown=false;
 		rightdown=false;
 		downdown=false;
 		ylocktoggle=false;
 		xlocktoggle=false;
 		gridXcoord=0;
 		gridYcoord=0;//position on the grid
 		posx=0;//position of the grid
 		posy=0;
 		scale=1;
 		newscale=1;
 		lastscale=1;
 		scaleanimationcounter=60;
 		System.out.println("blah:"+getInitialHeight());
 		wheight=getInitialHeight();
 		wwidth=getInitialWidth();		
 		glBindVertexArray(vao);
 		glEnable(GL_CULL_FACE);
 		glCullFace(GL_BACK);
 		glFrontFace(GL_CW);
 	    Mouse.setGrabbed(true);
 		aspect=1.0f;
 		glUseProgram(theProgram);
 		glUniform1f(uniformAspect, aspect);
 		glUseProgram(0);
 		final float depthZNear = 0.0f;
 		final float depthZFar = 1.0f;//i may need these later if i do 3d
 		gridXtranslate=500/2;
 		gridYtranslate=500/2;
 		glEnable(GL_DEPTH_TEST);
 		glDepthMask(true);
 		glDepthFunc(GL_LEQUAL);
 		glDepthRange(depthZNear, depthZFar);
 		glEnable(GL_DEPTH_CLAMP);
 		sr=new StringRenderer("",0.1f,1);
 		
 		
 		
 //experimental entity drawing code
 		world=new World(100, 100);
 		e=new StaticEntity(new TwoDModel(new Rectangle(.5f, .5f ,"ass"),0,0),4,4);
 		masterEntityList=new ArrayList<Entity>();
 		masterUIList=new ArrayList<Entity>();
 		cursor=new UIEntity(new Location(Mouse.getX(),Mouse.getY()),new TwoDModel(new Rectangle(.2f,.2f,"redtorch"),0f,0f));
 		world.addEntity(e);
 		wcull=new WorldCuller(world);
 		activeEntity=e;
 		float y=-0.6f;
 		Entity testui=new UIEntity(new Location(0f,y),new TwoDModel(new Rectangle(1.6f, 0.2f, "background"), 0f, 0f) );
 		Entity test1=new UIEntity(new Location(-1.6f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "wood"), 0f, 0f) );
 		Entity test2=new UIEntity(new Location(-1.2f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "stone"), 0f, 0f) );
 		Entity test3=new UIEntity(new Location(-0.8f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "snow"), 0f, 0f) );
 		Entity test4=new UIEntity(new Location(-0.4f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "vine"), 0f, 0f) );
 		Entity test5=new UIEntity(new Location(0f,y),new TwoDModel(new Rectangle(.2f, 0.2f, "sand"), 0f, 0f) );
 		Entity test6=new UIEntity(new Location(0.4f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "dirt"), 0f, 0f) );
 		Entity test7=new UIEntity(new Location(0.8f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "rose"), 0f, 0f) );
 		Entity test8=new UIEntity(new Location(1.2f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "grass_side"), 0f, 0f) );
 		Entity test9=new UIEntity(new Location(1.6f/2,y),new TwoDModel(new Rectangle(.2f, 0.2f, "hellrock"), 0f, 0f) );
 
 		masterUIList.add(testui);
 		masterUIList.add(test1);
 		masterUIList.add(test2);
 		masterUIList.add(test3);
 		masterUIList.add(test4);
 		masterUIList.add(test5);
 		masterUIList.add(test6);
 		masterUIList.add(test7);
 		masterUIList.add(test8);
 		masterUIList.add(test9);
 	}
 	@Override
 	protected void initialwh() {
 		wheight=getInitialHeight();
 		wwidth=getInitialWidth();
 	}
 
 	@Override
 	protected void display() {	
 		glBindFramebuffer(GL_FRAMEBUFFER, FramebufferName);
 		glViewport(0, 0, (int)wwidth, (int)wheight);
 		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		glClear(GL_COLOR_BUFFER_BIT);
 		rl.async.executeQueuedJobs();
 		glUseProgram(theProgram);
 		glUniform1f(elapsedTimeUniform, getElapsedTime() / 1000.0f);
 		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
 		glEnableVertexAttribArray(0);
 		glEnableVertexAttribArray(1);
 		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
 		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 16000);//1000vertexes*16//where the color data starts
 		glDrawArrays(GL_LINES, 0, 1000);
 		glDisableVertexAttribArray(0);
 		glDisableVertexAttribArray(1);
 		glUseProgram(0);
		System.out.println("size of masterEntityList:"+masterEntityList.size());
 		for(int i=0;i<masterEntityList.size();i++){
 			masterEntityList.get(i).draw();
 		}
 		for(int i=0;i<masterUIList.size();i++){
 			masterUIList.get(i).draw();
 		}
 		glBindFramebuffer(GL_FRAMEBUFFER, 0);
 		glViewport(0, 0, (int)wwidth, (int)wheight);
 		glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 		
 		glUseProgram(secondProgram);
 		// Bind our texture in Texture Unit 0
 		glActiveTexture(GL_TEXTURE0);
 		glBindTexture(GL_TEXTURE_2D, renderedTexture);
 		// Set our "renderedTexture" sampler to user Texture Unit 0
 		glUniform1i(texID, 0);
 
 		glUniform1f(elapsedTimeUniform, (float)(getElapsedTime()) );
 		glBindBuffer(GL_ARRAY_BUFFER, screenpositionBufferObject);
 		glEnableVertexAttribArray(0);
 		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
 		glDrawArrays(GL_QUADS, 0, 4); // 2*3 indices starting at 0 -> 2 triangles
 
 		glDisableVertexAttribArray(0);
 		glUseProgram(0);
 		sr.render("X:"+finalgridx+" Y:"+finalgridy, -0.91f, 0.91f);
 		sr.render("XL:"+xlocktoggle+"   YL:"+ylocktoggle,-0.91f,0.80f);
 		
 		cursor.draw();
 		//System.out.println("cursor:"+cursor.)
 		
 	}
 
 	@Override
 	protected void reshape(int width, int height) {
 		aspect = 1.0f / ((float)width / (float) height);
 		System.out.println("aspect:"+aspect);
 		glUseProgram(theProgram);
 		glUniform1f(uniformAspect, aspect);
 		glUseProgram(0);
 		glViewport(0, 0, width, height);
 		gridXtranslate=width/2;
 		gridYtranslate=height/2;
 		wwidth=width;
 		wheight=height;
 		//modifying the render texture's properties to have the proper width,height and buffersize 
 		glBindTexture(GL_TEXTURE_2D, renderedTexture);
 		bb.clear();
 		bb=BufferUtils.createByteBuffer(width*height*3);
 		glTexImage2D(GL_TEXTURE_2D, 0,GL_RGB, width, height, 0,GL_RGB, GL_UNSIGNED_BYTE,bb);
 	}
 
 
 	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 	@Override
 	protected void update(){
 		//need a general event queue
 		xlock.toggleUpdate();
 		ylock.toggleUpdate();
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 				leaveMainLoop();
 			}else if(Keyboard.getEventKey() == Keyboard.KEY_F11){
 				fullscreentoggle=true;
 			}
 		    if (Keyboard.getEventKeyState()) {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
 		        	//System.out.println("UP Key Pressed");
 		        	updown=true;
 		        }
 		    }
 		    else {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
 		        	//System.out.println("UP Key Released");
 		        	updown=false;
 		        }
 		    }
 		    if (Keyboard.getEventKeyState()) {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
 		        	//System.out.println("Right Key Pressed");
 		        	rightdown=true;
 		        }
 		    }
 		    else {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_RIGHT) {
 		        	//System.out.println("Right Key Released");
 		        	rightdown=false;
 		        }
 		    }
 		    if (Keyboard.getEventKeyState()) {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
 		        	//System.out.println("left Key Pressed");
 		        	leftdown=true;
 		        }
 		    }
 		    else {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_LEFT) {
 		        	//System.out.println("left Key Released");
 		        	leftdown=false;
 		        }
 		    }
 		    if (Keyboard.getEventKeyState()) {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_DOWN) {
 		        	//System.out.println("down Key Pressed");
 		        	downdown=true;
 		        }
 		    }
 		    else {
 		        if (Keyboard.getEventKey() == Keyboard.KEY_DOWN) {
 		        	//System.out.println("down Key Released");
 		        	downdown=false;
 		        }
 		    }
 		}
 		while(Mouse.next()){
 			if(Mouse.getEventDWheel()>0){
 				newscale=lastscale*2;
 				lastscale=newscale;
 				System.out.println("greater than 0");
 				scaleanimationcounter=0;
 				scalediff=newscale-scale;
 			}else if (Mouse.getEventDWheel()<0){
 				newscale=lastscale/2;
 				lastscale=newscale;
 				System.out.println("greater than 0");
 				scaleanimationcounter=0;
 				scalediff=newscale-scale;
 			}
 	
 			gridXcoord=Mouse.getX();
 			gridYcoord=Mouse.getY();
 			gridXcoord-=gridXtranslate;
 			gridYcoord-=gridYtranslate;
 		}
 		
 		if(clicked==false&&unclicked){
 			if(Mouse.isButtonDown(0)){
 				clicked=true;
 				unclicked=false;
 			}
 		}else if(!unclicked){
 			clicked=false;
 			if(!Mouse.isButtonDown(0)){
 				unclicked=true;
 			}
 		}
 		if(scaleanimationcounter<60&&scale!=newscale){
 			scale+=scalediff/60f;
 			scaleanimationcounter++;
 		}else{
 			scale=newscale;
 		}
 		if(updown){
 			posy-=.02f/scale;
 		}
 		if(rightdown){
 			posx-=.02f/scale;
 		}
 		if(leftdown){
 			posx+=.02f/scale;
 		}
 		if(downdown){
 			posy+=.02f/scale;
 		}
 	    glUseProgram(theProgram);
 	    glUniform1f(uniformScale,scale);
 	    glUniform1f(uniformXoffset, posx);
 	    glUniform1f(uniformYoffset, posy);
 	    glUseProgram(0);
 	  //  System.out.println("clicked:"+clicked);
 	   // System.out.println("unclicked:"+unclicked);
 	    gridXfake=(((gridXcoord)/(wwidth/2)/scale/aspect)-posx/aspect);//recently refactored the x, too lazy to update the y
 	    //gridYfake=(((gridYcoord)/(wheight/2))-posy*scale)/scale;
 	    gridYfake=(2*gridYcoord)/(scale*wheight)-posy;
 	    ///////////////////////now i'm doing world cull and stuff like that
 	    if(ylock.isOn()){
 	    	ylocktoggle=!ylocktoggle;
 	    }
 	    if(xlock.isOn()){
 	    	xlocktoggle=!xlocktoggle;
 	    }
 	    if(ylocktoggle&&xlocktoggle){
 	    	finalgridx=Math.round(gridXfake);
 	    	finalgridy=Math.round(gridYfake);
 	    	float q=(wwidth*scale)/(2*aspect);
 	    	Mouse.setCursorPosition((int)(finalgridx*q+gridXtranslate+posx*q),(int)(gridYtranslate+finalgridy*wheight*scale/2));
 	    }else if(ylocktoggle){
 	    	finalgridy=gridYfake;
 	    	finalgridy=Math.round(gridYfake);
 	    	Mouse.setCursorPosition(Mouse.getX(),(int)(gridYtranslate+finalgridy*wheight*scale/2));
 	    }else if(xlocktoggle){
 	    	finalgridx=Math.round(gridXfake);
 	    	finalgridy=gridYfake;
 	    	float q=(wwidth*scale)/(2*aspect);
 	    	Mouse.setCursorPosition((int)(finalgridx*q+gridXtranslate+posx*q),Mouse.getY());
 	    }else{
 	    	finalgridx=gridXfake;
 	    	finalgridy=gridYfake;
 	    }
 	    	
 	    if(clicked){
 	    	world.addEntity(new StaticEntity(((StaticEntity)activeEntity).getModel(), gridXfake,gridYfake));
 	    	wcull.updateEntity();
 	    }
 	    cursor.setLocation(((2*gridXcoord)/wwidth)/aspect, (2*gridYcoord)/wheight);
 	    wcull.update();
 
 	};
 	private void initializeProgram() {			
 		//my code for making the grid// should probably be a resource but i'm just hardcoding it in
 		vertexPositions=new float[8000];
 		for(float i=0;i<2000;i+=8){
 			vertexPositions[(int) i]=i/8;
 			vertexPositions[(int) i+1]=250;
 			vertexPositions[(int) i+2]=0;
 			vertexPositions[(int) i+3]=1;
 			vertexPositions[(int) i+4]=i/8;
 			vertexPositions[(int) i+5]=0;
 			vertexPositions[(int) i+6]=0;
 			vertexPositions[(int) i+7]=1;
 		}
 		for(float i=0;i<2000;i+=8){
 			vertexPositions[(int) i+2000]=0;
 			vertexPositions[(int) i+1+2000]=i/8;
 			vertexPositions[(int) i+2+2000]=0;
 			vertexPositions[(int) i+3+2000]=1;
 			vertexPositions[(int) i+4+2000]=250;
 			vertexPositions[(int) i+5+2000]=i/8;
 			vertexPositions[(int) i+6+2000]=0;
 			vertexPositions[(int) i+7+2000]=1;
 		}
 		//colors
 		for(float i=0;i<2000;i+=8){
 			vertexPositions[(int) i+0+4000]=1;
 			vertexPositions[(int) i+1+4000]=0;
 			vertexPositions[(int) i+2+4000]=0;
 			vertexPositions[(int) i+3+4000]=1;
 			vertexPositions[(int) i+4+4000]=1;
 			vertexPositions[(int) i+5+4000]=1;
 			vertexPositions[(int) i+6+4000]=1;
 			vertexPositions[(int) i+7+4000]=1;
 		}
 		for(float i=0;i<2000;i+=8){
 			vertexPositions[(int) i+0+6000]=0;
 			vertexPositions[(int) i+1+6000]=1;
 			vertexPositions[(int) i+2+6000]=0;
 			vertexPositions[(int) i+3+6000]=1;
 			vertexPositions[(int) i+4+6000]=1;
 			vertexPositions[(int) i+5+6000]=1;
 			vertexPositions[(int) i+6+6000]=1;
 			vertexPositions[(int) i+7+6000]=1;
 		}
 
 	
 		ArrayList<Integer> shaderList = new ArrayList<>();
 		ArrayList<Integer> othershaderList = new ArrayList<>();
 		shaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	"CalcOffset.vert"));
 		shaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "CalcColor.frag"));
 		theProgram = Framework.createProgram(shaderList);
 		othershaderList.add(Framework.loadShader(GL_VERTEX_SHADER, 	"Passthrough.vert"));
 		othershaderList.add(Framework.loadShader(GL_FRAGMENT_SHADER, "WobblyTexture.frag"));
 		secondProgram=Framework.createProgram(othershaderList);
 		elapsedTimeUniform = glGetUniformLocation(secondProgram, "time");
 		texID = glGetUniformLocation(secondProgram, "renderedTexture");
 		uniformAspect=glGetUniformLocation(theProgram,"aspect");
 		uniformScale=glGetUniformLocation(theProgram,"scaler");
 	    uniformXoffset = glGetUniformLocation(theProgram, "xOffset");
 	    uniformYoffset = glGetUniformLocation(theProgram, "yOffset");
 	    glUseProgram(theProgram);
 	    glUniform1f(uniformXoffset, 0.0f);
 	    glUniform1f(uniformYoffset, 0.0f);
 	    glUseProgram(0);
 	}
 
 	private void initializeVertexBuffer() {
 		FloatBuffer vertexPositionsBuffer = BufferUtils.createFloatBuffer(vertexPositions.length);
 		vertexPositionsBuffer.put(vertexPositions);
 		vertexPositionsBuffer.flip();
 
         positionBufferObject = glGenBuffers();	       
 		glBindBuffer(GL_ARRAY_BUFFER, positionBufferObject);
 	    glBufferData(GL_ARRAY_BUFFER, vertexPositionsBuffer, GL_STREAM_DRAW);
 		glBindBuffer(GL_ARRAY_BUFFER, 0);
 	}
 
 	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 	private float wheight,wwidth;
 	private float gridXfake,gridYfake=0;//these are the coords we print out which are really different from world coords
 	private float gridXtranslate,gridYtranslate;
 	private float gridXcoord,gridYcoord;
 	String fontPath;
 	public static List <Entity>masterEntityList;
 	public static List <Entity>masterUIList;
 	private World world;
 	private WorldCuller wcull;
 	private Entity activeEntity;
 	private boolean clicked,unclicked;
 	private Toggle xlock,ylock;
 	private float finalgridx,finalgridy;
 	public static float aspect;
 	private StringRenderer sr;
 	private float vertexPositions[];
 	public static float posx,posy,scale,scalediff;
 	private float lastscale,newscale,scaleanimationcounter;
 	boolean ispos=false;
 	private int uniformAspect;
 	private int uniformScale;
 	private int uniformXoffset;
 	private int uniformYoffset;
 	private int positionBufferObject,screenpositionBufferObject;
 	private boolean updown,leftdown,rightdown,downdown,xlocktoggle,ylocktoggle;
 	private int theProgram,secondProgram;
 	private int elapsedTimeUniform;
 	private int vao;
 	private Entity e;
 	private int FramebufferName;
 	public static ResourceLoader rl;
 	public static FloatBuffer fullscreenQuad;
 	private Texture tivo;
 	private int texID,renderedTexture;
 	private ByteBuffer bb;
 	private UIEntity cursor;
 }
