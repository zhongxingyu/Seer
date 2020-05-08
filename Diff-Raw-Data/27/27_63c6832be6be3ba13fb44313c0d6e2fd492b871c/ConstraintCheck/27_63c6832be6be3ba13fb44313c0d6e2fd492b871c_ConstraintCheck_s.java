 import java.awt.*;
 import java.io.*;
 import static java.lang.System.out;
 
 import javax.swing.*;
 
 import lcm.lcm.*;
 import april.jmat.*;
 import april.util.*;
 import april.vis.*;
 import armlab.lcmtypes.*;
 import april.*;
 
 public class ConstraintCheck
 {
     double angles_old[] = new double[6];
     LCMSend lcm = new LCMSend();
 
     public ConstraintCheck(){
 	    angles_old[0] = 0;
 	    angles_old[1] = 0;
 	    angles_old[2] = 0;
         angles_old[3] = 0;
         angles_old[4] = 0;
        	angles_old[5] = 0;
     }
 
     double[] check(double[] angles){ 
        for (int i = 0; i < 6; i++){
            angles[i] = angles[i] / 10;
        }
     
         //Create matrix to hold current location of parts
 	    double curXYZ[][] = new double[4][4];
 	    double tempXYZ[][] = new double[4][4];
 	    for(int i = 0; i < 4; i ++){
 		    for(int j = 0; j < 4; j ++){
 			    curXYZ[i][j] = 0;
 			    if(i == j){
 				    curXYZ[i][j] = 1;
 			    }
 		    }
 	    }
 
         boolean isCrashed = false;
 
 	    //Update matrix to offset by base
 	    curXYZ = LinAlg.multiplyMany(curXYZ, LinAlg.translate(0.0,0.0,0.75));
 	    if(curXYZ[2][3] < 0.25){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 	    }
 
 	    //Update matrix to offset by seg1
 	    curXYZ = LinAlg.multiplyMany(	curXYZ,
 				    LinAlg.translate(0.1,0.0,0.0),
 				    LinAlg.rotateZ(-(angles[0])-1.5707963267948966192313216916397514420985846996875529),
 				    LinAlg.translate(0.0,0.0,0.45)
 				    );
 	    if(curXYZ[2][3] < 0.25){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 		    angles[0] = angles_old[0];
 	    }
 
 	    //Update matrix to offset by seg2
 	    curXYZ = LinAlg.multiplyMany(	curXYZ,
 					    LinAlg.rotateX(-(angles[1])),
 					    LinAlg.translate(0.0,0.0,1.05)
 			         	    );
 	    if(curXYZ[2][3] < 0.25){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 		    angles[0] = angles_old[0];
 		    angles[1] = angles_old[1];
 	    }
 
 	    //Update matrix to offset by seg3
 	    curXYZ = LinAlg.multiplyMany(	curXYZ,
 					    LinAlg.rotateX(-(angles[2])),
 					    LinAlg.translate(0.0,0.0,1.0)
 			         	    );
 	    if(curXYZ[2][3] < 0.25){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 		    angles[0] = angles_old[0];
 		    angles[1] = angles_old[1];
 		    angles[2] = angles_old[2];
 	    }
 
 	    //Update matrix to offset by seg4
 	    curXYZ = LinAlg.multiplyMany(	curXYZ,
 					    LinAlg.rotateX(-(angles[3])),
 					    LinAlg.translate(0.0,0.0,0.8)
 			         	    );
 	    if(curXYZ[2][3] < 0.25){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 		    angles[0] = angles_old[0];
 		    angles[1] = angles_old[1];
 		    angles[2] = angles_old[2];
 		    angles[3] = angles_old[3];
 	    }
 
 	    //Update matrix to offset by seg5
 	    curXYZ = LinAlg.multiplyMany(	curXYZ,
 					    LinAlg.rotateZ(-(angles[4])),
 					    LinAlg.translate(0.0,0.0,0.2)
 			         	    );
 	    if(curXYZ[2][3] < 0.25){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 	    }
 
 	    //Update matrix to offset by two prong gripper
 	    curXYZ = LinAlg.multiplyMany(	curXYZ,
 					    LinAlg.translate(0.0,0.0,0.85)
 			         	    );
 	    if(curXYZ[2][3] < 0.25){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 		    angles[0] = angles_old[0];
 		    angles[1] = angles_old[1];
 		    angles[2] = angles_old[2];
 		    angles[3] = angles_old[3];
 		    angles[4] = angles_old[4];
 	    }
 
 	    //Update matrix to offset by three prong gripper
 	    curXYZ = LinAlg.multiplyMany(	curXYZ,
 					    LinAlg.translate(0.0,0.0,-0.85),
 					    LinAlg.rotateX(-(angles[5])),
 					    LinAlg.translate(0.0,0.0,0.85)
 			         	    ); 	//first should be LinAlg.translate(0.0,-0.15,-0.85), but for some reason
 					    //it doesn't work when rotating in +x direction, only -x...
 	    if(curXYZ[2][3] < 0.05 || ((curXYZ[2][3] > 0.05 && curXYZ[2][3] < 0.25) && curXYZ[0][3] < 0.5)){ //Checks to see if the bot can move there, if it can't reset the prior joints to how they were
 		    isCrashed = true;
 		    angles[0] = angles_old[0];
 		    angles[1] = angles_old[1];
 		    angles[2] = angles_old[2];
 		    angles[3] = angles_old[3];
 		    angles[4] = angles_old[4];
 		    angles[5] = angles_old[5];
 	    }
 	
 	    for (int i = 0; i < 6; i++){
 	        System.out.println(angles[i]);
 	    }
 	    lcm.send(angles);
 
 	    return angles;
     }
 }
