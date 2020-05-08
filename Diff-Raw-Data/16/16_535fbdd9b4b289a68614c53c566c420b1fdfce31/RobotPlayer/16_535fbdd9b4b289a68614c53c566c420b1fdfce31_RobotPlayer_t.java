 package oc004;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameConstants;
 import battlecode.common.GameObject;
 import battlecode.common.MapLocation;
 import battlecode.common.Robot;
 import battlecode.common.RobotController;
 import battlecode.common.RobotType;
 import battlecode.common.Team;
 import battlecode.common.Upgrade;
 
 public class RobotPlayer {
 
 	private static RobotController rc;
 	private static Team myTeam;
 	private static Team opposingTeam;
 	private static int mapWidth;
 	private static int mapHeight;
 
 	// DEBUG
 	private static IntStack bytecodeStack;
 
 	public static void run(RobotController rc) {
 		initialise(rc);
 
 		while(true) {
 
 			try {
 				decideMove();
 			} catch (Exception e) {
 				debug_printf(e);
 			}
 
 			rc.yield();
 		}
 	}
 
 	private static void initialise(RobotController rc) {
 		debug_initialise();
 
 		debug_startMethod();
 
 		RobotPlayer.rc = rc;
 		RobotPlayer.myTeam = rc.getTeam();
 		RobotPlayer.opposingTeam = myTeam.opponent();
 		RobotPlayer.mapHeight = rc.getMapHeight();
 		RobotPlayer.mapWidth = rc.getMapWidth();
 
 		debug_endMethod();
 	}
 
 	private static void debug_initialise() {
 		bytecodeStack = new IntStack(10);
 	}
 
	// use bc.conf to turn debug mode on
	private static void debug_printf(String format, Object ... objects) {
		System.out.printf(format, objects);
		System.out.printf("\n");
	}

	private static void debug_printf(Exception e) {
		debug_printf("%s: %s", e.getStackTrace()[0].getMethodName(), e.getMessage());		
 	}
 
 	private static void debug_printf(Object o) {
 		debug_printf("%s", o.toString());
 	}
 
 	private static void debug_startMethod() {
 		bytecodeStack.push(Clock.getBytecodeNum());
 	}
 
 	private static void debug_endMethod() {
 		int bytecodesNow = Clock.getBytecodeNum();
 		int bytecodesBefore = bytecodeStack.pop();
 		String methodName = new Throwable().getStackTrace()[1].getMethodName();
 		debug_printf("%d bytecodes used by %s (and any functions it calls)\n", bytecodesNow - bytecodesBefore, methodName);
 	}
 
 	private static void decideMove() {
 		debug_startMethod();
 
 		// todo read game specs
 		// todo read javadoc
 		// todo read lecture notes
 
 		debug_endMethod();
 	}
 }
