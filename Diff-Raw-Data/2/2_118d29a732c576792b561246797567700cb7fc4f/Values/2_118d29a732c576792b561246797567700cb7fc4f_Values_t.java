 package org.team691.robot2013;
 
 public class Values {
     
     public static final int SIDECAR_1 = 1;
     public static final int SIDECAR_2 = 2;
     
     public static final int DRIVE_JOYSTICK      = 1;
    public static final int SHOOTER_JOYSTICK    = 2;
     
     public static final int FR_DRIVE_VICTOR     = 1;
     public static final int FL_DRIVE_VICTOR     = 10;
     public static final int BR_DRIVE_VICTOR     = 2;
     public static final int BL_DRIVE_VICTOR     = 8; 
     public static final int[] DRIVE_VICTOR_SIDECARS  = {1, 1, 1, 1};
                                                       //FR FL BR BL
     public static final int[] DRIVE_VICTORS          = {FR_DRIVE_VICTOR, FL_DRIVE_VICTOR, BR_DRIVE_VICTOR, BL_DRIVE_VICTOR};
     
     public static final int FR_DRIVE_ENCODER_A  	 = 0; //1
     public static final int FR_DRIVE_ENCODER_B  	 = 0; //1
     public static final int FR_DRIVE_ENCODER_SIDECAR = 0;
     public static final int FR_DRIVE_ENCODER_COUNT	 = 0;
     
     public static final int FL_DRIVE_ENCODER_A  	 = 0;
     public static final int FL_DRIVE_ENCODER_B  	 = 0;
     public static final int FL_DRIVE_ENCODER_SIDECAR = 0;
     public static final int FL_DRIVE_ENCODER_COUNT 	 = 0;
     
     public static final int BR_DRIVE_ENCODER_A  	 = 0;
     public static final int BR_DRIVE_ENCODER_B  	 = 0;
     public static final int BR_DRIVE_ENCODER_SIDECAR = 0;
     public static final int BR_DRIVE_ENCODER_COUNT	 = 0;
     
     public static final int BL_DRIVE_ENCODER_A  	 = 0;
     public static final int BL_DRIVE_ENCODER_B  	 = 0;
     public static final int BL_DRIVE_ENCODER_SIDECAR = 0;
     public static final int BL_DRIVE_ENCODER_COUNT 	 = 0;
     
     public static final int[][] DRIVE_ENCODERS 		 = {
                                                                 {FR_DRIVE_ENCODER_A, FR_DRIVE_ENCODER_B},
                                                                 {FL_DRIVE_ENCODER_A, FL_DRIVE_ENCODER_B},
                                                                 {BR_DRIVE_ENCODER_A, BR_DRIVE_ENCODER_B},
                                                                 {BL_DRIVE_ENCODER_A, BL_DRIVE_ENCODER_B},
                                                           };
     public static final int[] DRIVE_ENCODER_SIDECARS = {FR_DRIVE_ENCODER_SIDECAR, FL_DRIVE_ENCODER_SIDECAR, BR_DRIVE_ENCODER_SIDECAR, BL_DRIVE_ENCODER_SIDECAR};
     public static final int[] DRIVE_ENCODER_COUNTS   = {FR_DRIVE_ENCODER_COUNT, FL_DRIVE_ENCODER_COUNT, BR_DRIVE_ENCODER_COUNT, BL_DRIVE_ENCODER_COUNT};
     
     public static final double FR_DRIVE_PID_KP = 0.0;
     public static final double FL_DRIVE_PID_KP = 0.0;
     public static final double BR_DRIVE_PID_KP = 0.0;
     public static final double BL_DRIVE_PID_KP = 0.0;
     public static final double FR_DRIVE_PID_KI = 0.0;
     public static final double FL_DRIVE_PID_KI = 0.0;
     public static final double BR_DRIVE_PID_KI = 0.0;
     public static final double BL_DRIVE_PID_KI = 0.0;
     public static final double FR_DRIVE_PID_KD = 0.0;
     public static final double FL_DRIVE_PID_KD = 0.0;
     public static final double BR_DRIVE_PID_KD = 0.0;
     public static final double BL_DRIVE_PID_KD = 0.0;
     public static final double[] DRIVE_PID_KP  = {FR_DRIVE_PID_KP, FL_DRIVE_PID_KP, BR_DRIVE_PID_KP, BL_DRIVE_PID_KP};
     public static final double[] DRIVE_PID_KI  = {FR_DRIVE_PID_KI, FL_DRIVE_PID_KI, BR_DRIVE_PID_KI, BL_DRIVE_PID_KI};
     public static final double[] DRIVE_PID_KD  = {FR_DRIVE_PID_KD, FL_DRIVE_PID_KD, BR_DRIVE_PID_KD, BL_DRIVE_PID_KD};
     public static final double[][] DRIVE_PID   = {DRIVE_PID_KP, DRIVE_PID_KI, DRIVE_PID_KD};
     
     public static final int SHOOTER_VICTOR          = 0;
     
     public static final int SHOOTER_ENCODER_A       = 0;
     public static final int SHOOTER_ENCODER_B       = 0;
     public static final int SHOOTER_ENCODER_SIDECAR = 0;
     public static final int SHOOTER_ENCODER_COUNT   = 0;
     public static final int[] SHOOTER_ENCODER       = {SHOOTER_ENCODER_SIDECAR, SHOOTER_ENCODER_A, SHOOTER_ENCODER_B, SHOOTER_ENCODER_COUNT};
     
     public static final int SHOOTER_TILT_VICTOR     = 0;
 }
