 /**
  * The main class to draw.
  *
  * Draw is called on the dragonfly, and the dragonfly is the main interface to the bug
  * This is my own interface so that the Dragonfly is its own entity (more object oriented)
  * This will also be useful for assignment three, so that this bug can be adapted and many
  * bugs can be generated
  *
  * I used this for the bug wing texture
  *  http://www.utilitydesign.co.uk/mall/UtilityDesign/customerimages/products/l_UT61057.jpg
  *
  * I used this for the body texture
  *  http://1.bp.blogspot.com/_k3Z3PpjnALE/SgAbafKBlHI/AAAAAAAABpk/SGmuODFsCWs/s400/Mosaic+%231.jpg
  *
  * @author Raphael Landaverde
  * @since Spring 2013
  */
 
 package insect;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GL2GL3;
 import javax.media.opengl.glu.GLU;
 
 import com.jogamp.opengl.util.gl2.GLUT;
 
 public class Dragonfly
 {
     /** The different names for limbs */
 
     /** The color for components which are selected for rotation. */
     private static final FloatColor ACTIVE_COLOR = FloatColor.RED;
 
     /** The color for components which are selected for rotation. */
     private static final FloatColor INACTIVE_COLOR = new FloatColor(1, 1, 1);
 
     private static ArrayList<String> names = new ArrayList<String>();
     public static String FRONT_LEFT_ONE = "front left one";
     public static String FRONT_LEFT_TWO = "front left two";
     public static String FRONT_LEFT_THREE = "front left three";
     public static String MID_LEFT_ONE = "mid left one";
     public static String MID_LEFT_TWO = "mid left two";
     public static String MID_LEFT_THREE = "mid left three";
     public static String BACK_LEFT_ONE = "back left one";
     public static String BACK_LEFT_TWO = "back left two";
     public static String BACK_LEFT_THREE = "back left three";
     public static String FRONT_RIGHT_ONE = "front right one";
     public static String FRONT_RIGHT_TWO = "front right two";
     public static String FRONT_RIGHT_THREE = "front right three";
     public static String MID_RIGHT_ONE = "mid right one";
     public static String MID_RIGHT_TWO = "mid right two";
     public static String MID_RIGHT_THREE = "mid right three";
     public static String BACK_RIGHT_ONE = "back right one";
     public static String BACK_RIGHT_TWO = "back right two";
     public static String BACK_RIGHT_THREE = "back right three";
     public static String TAIL_ONE = "tail one";
     public static String TAIL_TWO = "tail two";
     public static String TAIL_THREE = "tail three";
     public static String WING_LEFT = "wing left";
     public static String WING_RIGHT = "wing right";
     public static String BODY = "body";
     public static String TOP_NAME = "Dragonfly";
     public static String LEFT_EYE = "left eye";
     public static String RIGHT_EYE = "right eye";
 
     /** The test cases */
     private final TestCases tests = new TestCases();
 
     /** The components of the dragonfly */
     private List<List<Component>> bodyParts;
     private ArrayList<List<Component>> activeParts = new ArrayList<List<Component>>();
     private Set<Component> activeJoints = new HashSet<Component>(23);
     private Component topComponent;
     private List<Component> eyes;
 
     /** The dimensions of the body parts */
     private static float LEG_RADIUS = 0.05f;
     private static float LEG_HEIGHT = 0.35f;
     private static float TAIL_RADIUS = 0.15f;
     private static float TAIL_HEIGHT = 0.35f;
     private static float TAIL_OFFSET = 0.9f;
 
     /** Special Wing offsets */
     private static float BODY_RADIUS = 1f;
     private static float WING_OFFSET = -0.25f;
     private static float WING_Z_POS = 6.3f * TAIL_RADIUS;
     private static float WING_Y_POS = 0.19f * BODY_RADIUS;
 
     /** Leg positioning */
     private static float FIRST_LEG_DIST = 0.15f;
     private static float FRONT_OFFSET = -0.5f;
     private static float MID_OFFSET = -0.25f;
     private static float BACK_OFFSET = 0f;
 
     /** Offsets for the eye */
     private static float EYE_RADIUS = 0.15f;
     private static float EYE_X_OFFSET = -1.1f;
     private static float EYE_Z_OFFSET = 0.2f;
 
     private Point3D INIT_POS = new Point3D(0, 0, 0);
 
     /** The parent glut object */
     private GLUT glut;
     private GLU glu;
 
     /**
      * Initializes all of the components in their initial position
      */
     Dragonfly(GLUT glut, GLU glu)
     {
         /* Add all of the strings to the name ArrayList */
         names.add(FRONT_LEFT_ONE);
         names.add(FRONT_LEFT_TWO);
         names.add(FRONT_LEFT_THREE);
         names.add(MID_LEFT_ONE);
         names.add(MID_LEFT_TWO);
         names.add(MID_LEFT_THREE);
         names.add(BACK_LEFT_ONE);
         names.add(BACK_LEFT_TWO);
         names.add(BACK_LEFT_THREE);
         names.add(FRONT_RIGHT_ONE);
         names.add(FRONT_RIGHT_TWO);
         names.add(FRONT_RIGHT_THREE);
         names.add(MID_RIGHT_ONE);
         names.add(MID_RIGHT_TWO);
         names.add(MID_RIGHT_THREE);
         names.add(BACK_RIGHT_ONE);
         names.add(BACK_RIGHT_TWO);
         names.add(BACK_RIGHT_THREE);
         names.add(TAIL_ONE);
         names.add(TAIL_TWO);
         names.add(TAIL_THREE);
         names.add(WING_LEFT);
         names.add(WING_RIGHT);
 
         this.glut = glut;
         this.glu = glu;
         /* Initializes all of the body parts */
         Component dragonflyBody = new Component(new Point3D(0, 0, 0),
                                                 new DragonflyBody(BODY_RADIUS, this.glut, this.glu), BODY, false);
 
         /* The left limbs */
         Component leftFront1 = new Component(new Point3D(FRONT_OFFSET, 0,
                                              FIRST_LEG_DIST), new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT,
                                                      this.glut, this.glu), FRONT_LEFT_ONE, false);
         Component leftFront2 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                              new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                              FRONT_LEFT_TWO, false);
         Component leftFront3 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                              new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                              FRONT_LEFT_THREE, false);
         Component leftMid1 = new Component(new Point3D(MID_OFFSET, 0,
                                            FIRST_LEG_DIST), new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT,
                                                    this.glut, this.glu), MID_LEFT_ONE, false);
         Component leftMid2 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                            new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                            MID_LEFT_TWO, false);
         Component leftMid3 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                            new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                            MID_LEFT_THREE, false);
         Component leftBack1 = new Component(new Point3D(BACK_OFFSET, 0,
                                             FIRST_LEG_DIST), new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT,
                                                     this.glut, this.glu), BACK_LEFT_ONE, false);
         Component leftBack2 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                             new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                             BACK_LEFT_TWO, false);
         Component leftBack3 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                             new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                             BACK_LEFT_THREE, false);
 
         /* The right limbs (I mirror top level limbs) */
         Component rightFront1 = new Component(new Point3D(FRONT_OFFSET, 0,
                                               FIRST_LEG_DIST), new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT,
                                                       this.glut, this.glu), FRONT_RIGHT_ONE, true);
         Component rightFront2 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                               new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                               FRONT_RIGHT_TWO, false);
         Component rightFront3 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                               new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                               FRONT_RIGHT_THREE, false);
         Component rightMid1 = new Component(new Point3D(MID_OFFSET, 0,
                                             FIRST_LEG_DIST), new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT,
                                                     this.glut, this.glu), MID_RIGHT_ONE, true);
         Component rightMid2 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                             new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                             MID_RIGHT_TWO, false);
         Component rightMid3 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                             new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                             MID_RIGHT_THREE, false);
         Component rightBack1 = new Component(new Point3D(BACK_OFFSET, 0,
                                              FIRST_LEG_DIST), new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT,
                                                      this.glut, this.glu), BACK_RIGHT_ONE, true);
         Component rightBack2 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                              new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                              BACK_RIGHT_TWO, false);
         Component rightBack3 = new Component(new Point3D(0, 0, LEG_HEIGHT),
                                              new RoundedCylinder(LEG_RADIUS, LEG_HEIGHT, this.glut, this.glu),
                                              BACK_RIGHT_THREE, false);
 
         /* The tail */
         Component tail1 = new Component(new Point3D(TAIL_OFFSET, 0, 0),
                                         new RoundedCylinder(TAIL_RADIUS, TAIL_HEIGHT, this.glut, this.glu),
                                         TAIL_ONE, false);
         Component tail2 = new Component(new Point3D(0, 0, TAIL_HEIGHT),
                                         new RoundedCylinder(TAIL_RADIUS, TAIL_HEIGHT, this.glut, this.glu),
                                         TAIL_ONE, false);
         Component tail3 = new Component(new Point3D(0, 0, TAIL_HEIGHT),
                                         new RoundedCylinder(TAIL_RADIUS, TAIL_HEIGHT, this.glut, this.glu),
                                         TAIL_ONE, false);
 
         /*
          * The wings, Constructor is slightly different for CylinderWings, see
          * file for details
          */
         Component wingLeft = new Component(new Point3D(WING_OFFSET, WING_Y_POS,
                                            0), new CylinderWings(0.25, 0.05, WING_Z_POS, this.glut, this.glu),
                                            WING_LEFT, false);
         Component wingRight = new Component(new Point3D(WING_OFFSET,
                                             WING_Y_POS, 0), new CylinderWings(0.25, 0.05, WING_Z_POS,
                                                     this.glut, this.glu), WING_LEFT, true);
 
 
         /* The two eyes */
         Component leftEye = new Component(new Point3D(EYE_X_OFFSET, 0,
                                           EYE_Z_OFFSET), new BugEyes(EYE_RADIUS, this.glut, this.glu),
                                           LEFT_EYE, false);
 
         Component rightEye = new Component(new Point3D(EYE_X_OFFSET, 0,
                                            -EYE_Z_OFFSET), new BugEyes(EYE_RADIUS, this.glut, this.glu),
                                            RIGHT_EYE, false);
 
         rightEye.setColor(new FloatColor(1, 1, 1));
         leftEye.setColor(new FloatColor(1, 1, 1));
 
         eyes = Arrays.asList(rightEye, leftEye);
         for (Component c : eyes)
         {
             c.setXPositiveExtent(0);
             c.setXNegativeExtent(0);
             c.setYPositiveExtent(40);
             c.setYNegativeExtent(-40);
             c.setZPositiveExtent(40);
             c.setZNegativeExtent(-40);
         }
 
         /* Set up the child relation to limbs, and add all to body */
         leftFront2.addChild(leftFront3);
         leftFront1.addChild(leftFront2);
         leftMid2.addChild(leftMid3);
         leftMid1.addChild(leftMid2);
         leftBack2.addChild(leftBack3);
         leftBack1.addChild(leftBack2);
         dragonflyBody.addChild(leftFront1);
         dragonflyBody.addChild(leftMid1);
         ;
         dragonflyBody.addChild(leftBack1);
 
         rightFront2.addChild(rightFront3);
         rightFront1.addChild(rightFront2);
         rightMid2.addChild(rightMid3);
         rightMid1.addChild(rightMid2);
         rightBack2.addChild(rightBack3);
         rightBack1.addChild(rightBack2);
         dragonflyBody.addChild(rightFront1);
         dragonflyBody.addChild(rightMid1);
         dragonflyBody.addChild(rightBack1);
 
         tail2.addChild(tail3);
         tail1.addChild(tail2);
         dragonflyBody.addChild(tail1);
 
         dragonflyBody.addChild(wingRight);
         dragonflyBody.addChild(wingLeft);
 
         dragonflyBody.addChild(leftEye);
         dragonflyBody.addChild(rightEye);
 
         /* Add all to the component array for toggling */
         bodyParts = Arrays.asList(
                         Arrays.asList(leftFront1, leftFront2, leftFront3),
                         Arrays.asList(leftMid1, leftMid2, leftMid3),
                         Arrays.asList(leftBack1, leftBack2, leftBack3),
                         Arrays.asList(rightFront1, rightFront2, rightFront3),
                         Arrays.asList(rightMid1, rightMid2, rightMid3),
                         Arrays.asList(rightBack1, rightBack2, rightBack3),
                         Arrays.asList(tail1, tail2, tail3), Arrays.asList(wingLeft),
                         Arrays.asList(wingRight));
 
         /* Set up initial rotations for many of the parts */
         this.setModelState(tests.basic());
 
         /* Set up the rotation limits */
         for (Component c : Arrays.asList(wingLeft, wingRight))
         {
             c.setXPositiveExtent(20);
             c.setXNegativeExtent(-20);
             c.setYPositiveExtent(c.yAngle());
             c.setYNegativeExtent(c.yAngle());
             c.setZPositiveExtent(c.zAngle());
             c.setZNegativeExtent(c.zAngle());
         }
 
         for (List<Component> limb : this.bodyParts)
         {
             for (int ii = 0 ; ii < 3 ; ii++)
             {
                 if (limb.size() < 3)
                     break; /* Ignore tail here */
                 Component c = limb.get(ii);
                 if (ii == 0)
                 {
                     c.setXPositiveExtent(102);
                     c.setXNegativeExtent(28);
                     c.setYPositiveExtent(18);
                     c.setYNegativeExtent(-30);
                     c.setZPositiveExtent(12);
                     c.setZNegativeExtent(-12);
                 }
                 else if (ii == 1)
                 {
                     c.setXPositiveExtent(60);
                     c.setXNegativeExtent(-10);
                     c.setYPositiveExtent(20);
                     c.setYNegativeExtent(-52);
                     c.setZPositiveExtent(12);
                     c.setZNegativeExtent(-12);
                 }
                 else
                 {
                     c.setXPositiveExtent(44);
                     c.setXNegativeExtent(-22);
                     c.setYPositiveExtent(82);
                     c.setYNegativeExtent(-2);
                     c.setZPositiveExtent(12);
                     c.setZNegativeExtent(-12);
                 }
             }
         }
 
         /* Set up tail finally */
         tail1.setXPositiveExtent(0);
         tail1.setXNegativeExtent(0);
         tail1.setYPositiveExtent(106);
         tail1.setYNegativeExtent(74);
         tail1.setZPositiveExtent(0);
         tail1.setZNegativeExtent(0);
         tail2.setXPositiveExtent(20);
         tail2.setXNegativeExtent(-20);
         tail2.setYPositiveExtent(16);
         tail2.setYNegativeExtent(-16);
         tail2.setZPositiveExtent(0);
         tail2.setZNegativeExtent(0);
         tail3.setXPositiveExtent(20);
         tail3.setXNegativeExtent(-20);
         tail3.setYPositiveExtent(16);
         tail3.setYNegativeExtent(-16);
         tail3.setZPositiveExtent(0);
         tail3.setZNegativeExtent(0);
 
         /* Special case.  To orient the tail properly, I give it an initial turn of 90 */
         tail1.setAngles(0, 90, 0);
 
         /* Finally, set the color of all components to white */
         for (List<Component> limb : this.bodyParts)
         {
             for (Component c : limb)
             {
                 c.setColor(new FloatColor(1, 1, 1));
             }
         }
 
         /* Add body to the top level invisible component */
         this.topComponent = new Component(INIT_POS, "Dragonfly");
                 this.topComponent.addChild(dragonflyBody);
 //        this.topComponent.addChild(rightEye);
 
         /* I used to rotate the bug, but it complicates the eye moving */
         //        /* Turn top component to properly view bug */
         //        this.topComponent.rotate(Axis.Y, 45);
         //        this.topComponent.rotate(Axis.X, 25);
     }
 
     /**
      * Sets the angles of the model based on the TestCases
      */
     public boolean setModelState(Map<String, Angled> state)
     {
         int ii = 0;
         for (List<Component> ll : this.bodyParts)
         {
             for (Component cc : ll)
             {
                 cc.setAngles(state.get(names.get(ii)));
                 ii++;
             }
         }
         return true;
     }
 
     /**
      * Sets the model state based on next state
      */
     public boolean setModelState()
     {
         Map<String, Angled> state = tests.next();
         if (state == null)
             return false;
 
         int ii = 0;
         for (List<Component> ll : this.bodyParts)
         {
             for (Component cc : ll)
             {
                 cc.setAngles(state.get(names.get(ii)));
                 ii++;
             }
         }
 
         return true;
     }
 
     /**
      * Initializes all of the displaylists for rendering
      */
     public void initialize(GL2 gl)
     {
         this.topComponent.initialize(gl);
     }
 
     /**
      * Updates the state of the display list to reflect current state
      */
     public void update(GL2 gl)
     {
         this.topComponent.update(gl);
     }
 
     /**
      * Draws the components on the screen
      */
     public void draw(GL2 gl)
     {
         this.topComponent.draw(gl);
     }
 
     /**
      * Resets the angles of all of the body parts
      */
     public boolean resetAngles()
     {
         this.setModelState(tests.basic());
         return true;
     }
 
     /**
      * Rotate the selected components via the selected axis
      */
     public void rotate(Axis selectedAxis, double rotAngle)
     {
         for (Component c : this.activeJoints)
         {
             c.rotate(selectedAxis, rotAngle);
         }
     }
 
     /**
      * Rotates eyeball towards mouse cursor
      *
      * I use the world quaternion to rotate the coordinate to a usable angle first
      * Then I rotate the eye
      */
     public void trackMouse(int x, int y, GL2 gl, Quaternion world)
     {
         final double worldWidth = 5, worldHeight = 5;
         /* The mouse coordinates at a plane located at z=6 */
         double mouseCoord[] = {x, y, 2};
         /* The vector of each eyeball through the pupil */
         double eyeVector[] = new double[3];
 
         /* Now I convert my mouse coordinates into my APPROXIMATE world coordinates */
         /* This is very hacky (I assume the window size is constant), but I did what I could */
         mouseCoord[0] = (mouseCoord[0] / InsectMain.DEFAULT_WINDOW_WIDTH)  * worldWidth;
         mouseCoord[0] -= (worldWidth / 2);
         mouseCoord[1] = (mouseCoord[1] / InsectMain.DEFAULT_WINDOW_HEIGHT) * worldHeight;
         mouseCoord[1] = worldHeight - mouseCoord[1];
         mouseCoord[1] -= (worldHeight / 2);
         
         /* wcoord contains our points */
         double mouseVector[] = new double[3];
         float transform[] = new float[16];
         
         /* Now, I use the quaternion from the world to calculate the proper eyeVector */
         transform = world.toMatrix(); /* Specified in column major order */
 
         /* I rotate the mouse position to a new position based on if the mouse position was
          * rotated back from what the world rotation did. This is so we can figure out the
          * necessary angles of rotation for the eye based on the eye coordinate system.  This
          * transform is the inverse of the of the matrix from the world rotation
          */
 
         mouseCoord[0] = mouseCoord[0] * transform[0] + mouseCoord[1] * transform[1] +
                         mouseCoord[2] * transform[2];
         mouseCoord[1] = mouseCoord[0] * transform[4] + mouseCoord[1] * transform[5] +
                         mouseCoord[2] * transform[6];
         mouseCoord[2] = mouseCoord[0] * transform[8] + mouseCoord[1] * transform[9] +
                         mouseCoord[2] * transform[10];
         
         System.out.println("newX = " + mouseCoord[0] + " newY = " + mouseCoord[1] + " newZ = " + mouseCoord[2] + "\n");
 
         double ox, oy, oz;
         double xAngle, yAngle, zAngle;
         double newX, newY, newZ;
         for (Component eye : this.eyes)
         {
        	/* Transform each eye-coordinate back to center for proper angles */
        	newX = eye.position.x() * transform[0] + eye.position.y() * transform[1] +
                    eye.position.z() * transform[2];
        	newY = eye.position.x() * transform[4] + eye.position.y() * transform[5] +
                    eye.position.z() * transform[6];
        	newZ = eye.position.x() * transform[8] + eye.position.y() * transform[9] +
                    eye.position.z() * transform[10];
        	
             /* Calculate the vector from eye center to the adjusted mouse position */
            mouseVector[0] = mouseCoord[0] - newX;//eye.position.x();
            mouseVector[1] = mouseCoord[1] - newY;//eye.position.y();
            mouseVector[2] = mouseCoord[2] - newZ;//eye.position.z();
 
             /* The vector to the pupil can be thought of as this unit vector */
             eyeVector[0] = -1;
             eyeVector[1] = 0;
             eyeVector[2] = 0;
 
             /* Convert to unit vector */
             double mouseMag = Math.sqrt(Math.pow(mouseVector[0], 2) + Math.pow(mouseVector[1], 2) + Math.pow(mouseVector[2], 2));
             /* eyeMag is always 0 */
 
             mouseVector[0] /= mouseMag;
             mouseVector[1] /= mouseMag;
             mouseVector[2] /= mouseMag;
 
             /* Now, all of the vectors are unit vectors */
 
             /* Now, I split this into a simple problem.  Since there is no need for rotation around x (pupil aiming at -1 x, any rotation can thus be reached via y and z rots)
             * then I can make the simplification for this problem to simply finding two angles.  The angle to rotate around y, and angle to rotate around z.  This
             * can be done using dot products and if statements to account for > 90 degree angles
             */
 
             /* Calculate rotation around y first (rotation is positive counterclockwise always) */
             double yDot = mouseVector[0] * eyeVector[0] + mouseVector[2] * eyeVector[2];
             /* Have to detect negative dot products that give us info on position */
             if (yDot < 0)
             {
             	/* Angle is > 90 */
             	yAngle = 180 - Math.acos( -yDot ) * (180 / (Math.PI));
             }
             else
             {
             	/* Normal case, angle is < 90 */
             	yAngle = Math.acos( yDot ) * (180 / (Math.PI));
             }
             
             /* Figure out which way to rotate based on Z */
             if (yAngle > 90)
             {
                 if (mouseCoord[0] > eye.position.x() )
                 {
                     if (mouseCoord[2] < eye.position.z())
                     {
                         /* Mouse behind eye */
                         yAngle *= -1;
                     }
                     else
                     {
                         /* Mouse in fornt of eye, no need to do anything */
                     }
                 }
                 else
                 {
                     /* We know eye is pointing to -1 x, so yAngle should never be > 90 while mouse is left of eye */
                     System.out.println("Mouse on left of eye but angle greater than 90! This should never occur, error!");
                 }
             }
             else
             {
                 /*Smaller than 90 degrees, different case */
                 if (mouseCoord[0] < eye.position.x())
                 {
                     if (mouseCoord[2] < 0)
                     {
                         /* Mouse behind eye, rotate negative */
                         yAngle = -yAngle;
                     }
                     else
                     {
                     	/* Already set */
                     }
                 }
                 else
                 {
                     /* For the same reason as above, this should never occur */
                     System.out.println("Mouse on right of eye, but angle smaller than 90! This should never occur, error!");
                 }
             }
 
             /* Now similarly, I calculate the zAngle.  In this case, a positive rotation will aim the eye downwards */
             /* I use regular trig here, because the dot product method fail here for a variety of reasons.  Since I know that
              * the pupil is (-1, 0, 0), I find the zAngle by making right triangles of the mouseVector
              */
             if (mouseCoord[2] < 0)
             {
             	/* This will be > 90 degree angle, trying to look behind itself */
             	if (mouseVector[1] < 0)
             	{
             		/* Below the eye, rotation is positive */
             		zAngle = 180 - Math.atan2(Math.abs(mouseVector[1]), Math.abs(mouseVector[0])) * (180/Math.PI);
             	}
             	else
             	{
             		/* Above the eye, rotation is negative */
             		zAngle = -1 * (180 - Math.atan2(Math.abs(mouseVector[1]), Math.abs(mouseVector[0])) * (180/Math.PI));
             	}
             }
             else
             {
             	/* Angle in front, only thing that matters is height to angle */
             	if (mouseVector[1] > 0)
             	{
             		/* Neg rotation */
             		zAngle = -1 * Math.atan2(Math.abs(mouseVector[1]), Math.abs(mouseVector[0])) * (180/Math.PI);
             	}
             	else
             	{
             		zAngle = Math.atan2(Math.abs(mouseVector[1]), Math.abs(mouseVector[0])) * (180/Math.PI);
             	}
             	
             }
 
 
             /* Have the angles, rotate them */
             System.out.println("zAngle = " + zAngle + " and yAngle = " + yAngle + "\n");
             eye.rotateTo(Axis.Y, yAngle);
             eye.rotateTo(Axis.Z, zAngle);
         }
 
     }
 
     /**
      * Toggles the active limbs
      *
      */
     public boolean toggleLimb(int index)
     {
         List<Component> set = this.bodyParts.get(index);
         if (this.activeParts.contains(set))
         {
             this.activeParts.remove(set);
             this.activeJoints.removeAll(set); /* Remove all joints from this limb */
             for (Component c : set)
                 c.setColor(INACTIVE_COLOR);
         }
         else
         {
             this.activeParts.add(set);
         }
         return true;
     }
 
     /**
      * Toggles the active joints for rotation
      */
     public boolean toggleSelection(int index)
     {
         boolean hasChanged = false;
         for (List<Component> limb : this.activeParts)
         {
             /* Account for wings that only have 1 joint */
             if (limb.size() < 3 && index > 0)
                 break;
 
             /* Normal Case */
             Component joint = limb.get(index);
             if (this.activeJoints.contains(joint))
             {
                 this.activeJoints.remove(joint);
                 joint.setColor(INACTIVE_COLOR);
             }
             else
             {
                 this.activeJoints.add(joint);
                 joint.setColor(ACTIVE_COLOR);
             }
             hasChanged = true;
         }
         return hasChanged;
     }
 
 }
