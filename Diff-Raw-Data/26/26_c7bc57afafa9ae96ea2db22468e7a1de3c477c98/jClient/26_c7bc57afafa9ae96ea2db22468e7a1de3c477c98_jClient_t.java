 /*
     This file is part of ciberRatoToolsSrc.
 
     Copyright (C) 2001-2011 Universidade de Aveiro
 
     ciberRatoToolsSrc is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     ciberRatoToolsSrc is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Foobar; if not, write to the Free Software
     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 import ciberIF.beaconMeasure;
 import ciberIF.ciberIF;
 
 
 /**
  * example of a basic agent
  * implemented using the java interface library.
  */
 public class jClient {
 
 	ciberIF cif;
 
 	enum State {RUN, WAIT, RETURN}
 
 	public static void main(String[] args) {
 
 		String host, robName;
 		int pos; 
 		int arg;
 
 		//default values
 		host = "localhost";
 		robName = "jClient";
 		pos = 1;
 
 
 		// parse command-line arguments
 		try {
 			arg = 0;
 			while (arg<args.length) {
 				if(args[arg].equals("-pos")) {
 					if(args.length > arg+1) {
 						pos = Integer.valueOf(args[arg+1]).intValue();
 						arg += 2;
 					}
 				}
 				else if(args[arg].equals("-robname")) {
 					if(args.length > arg+1) {
 						robName = args[arg+1];
 						arg += 2;
 					}
 				}
 				else if(args[arg].equals("-host")) {
 					if(args.length > arg+1) {
 						host = args[arg+1];
 						arg += 2;
 					}
 				}
 				else throw new Exception();
 			}
 		}
 		catch (Exception e) {
 			print_usage();
 			return;
 		}
 
 		// create client
 		jClient client = new jClient();
 
 		client.robName = robName;
 
 		// register robot in simulator
 		client.cif.InitRobot(robName, pos, host);
 
 		// main loop
 		client.mainLoop();
 
 	}
 
 	// Constructor
 	jClient() {
 		cif = new ciberIF();
 		beacon = new beaconMeasure();
 		beaconToFollow = 0;
 		ground = -1;
 		state = State.RUN;
 		counter = 0;
 	}
 
 	/** 
 	 * reads a new message, decides what to do and sends action to simulator
 	 */
 	public void mainLoop () {
 
 		while(true) {
 			cif.ReadSensors();
 			decide();
 		}
 	}
 	
 	public void getInfo() {
 		if(cif.IsObstacleReady(0))
 			irSensor0 = cif.GetObstacleSensor(0);
 		if(cif.IsObstacleReady(1))
 			irSensor1 = cif.GetObstacleSensor(1);
 		if(cif.IsObstacleReady(2))
 			irSensor2 = cif.GetObstacleSensor(2);
 		if(cif.IsGroundReady())
 			ground = cif.GetGroundSensor();
 		if(cif.IsBeaconReady(beaconToFollow))
 			beacon = cif.GetBeaconSensor(beaconToFollow);
 	}
 	
 	public void requestInfo() {
 		cif.Say(robName);
 		cif.RequestIRSensor(0);
 		cif.RequestIRSensor(1);
 		cif.RequestIRSensor(2);
 		cif.RequestBeaconSensor(beaconToFollow);
 		cif.RequestGroundSensor();
 	}
 	
 	public void original_wander(boolean followBeacon) {
 	    if(irSensor0>2.0 || irSensor1>2.0 ||  irSensor2>2.0) 
 		    cif.DriveMotors(0.1,-0.1);
 	    else if(irSensor1>1.0) cif.DriveMotors(0.1,0.0);
 	    else if(irSensor2>1.0) cif.DriveMotors(0.0,0.1);
 	    else if(followBeacon && beacon.beaconVisible && beacon.beaconDir > 20.0) 
 	    cif.DriveMotors(0.0,0.1);
 	    else if(followBeacon && beacon.beaconVisible && beacon.beaconDir < -20.0) 
 	    cif.DriveMotors(0.1,0.0);
 	    else cif.DriveMotors(0.1,0.1);
 	}
 
 	public void wander(boolean followBeacon) {
		// verifica se há algum obstáculo a evitar
 		if(irSensor0 > 2.0 || irSensor1 > 2.0 ||  irSensor2 > 2.0)
 			processWall();
		// se não houver obstáculos e o beacon não estiver enquandrado, roda
 		else if(followBeacon && beacon.beaconVisible && beacon.beaconDir > 10.0) 
 			cif.DriveMotors(0.0,0.15);
 		else if(followBeacon && beacon.beaconVisible && beacon.beaconDir < -10.0) 
 			cif.DriveMotors(0.15,0.0);
		// caso contrário, anda em frente
 		else cif.DriveMotors(0.1,0.1);
 		
 	}
 
 	private void processWall() {
 		
		// enquanto o beacon não estiver enquadrado ou não estiver visível, percorre a parede
 		while(beacon.beaconDir > 15.0 || beacon.beaconDir < -15.0 || !beacon.beaconVisible)
 		{
			// se bater, tenta rodar para sair da situação
 			if(cif.GetBumperSensor()) {
 				cif.DriveMotors(-0.15, 0.15);
 			}
 			
 			requestInfo();
 			cif.ReadSensors();
 			getInfo();
 			
			// se estiver numa esquisa, roda no sentido do relógio
 			if (irSensor2 < 2.0 && irSensor0 < 2.0 && irSensor2 < 3.0) {
 				cif.DriveMotors(0.1, -0.1);
			// se estiver num canto, roda no sentido contrário ao do rel�relógio
 			} else if (irSensor0 <= 2.0 && irSensor1 <= 2.0 && irSensor2 <= 6.0) {
 				cif.DriveMotors(0.1, 0.1);
			// caso não haja obstáculo ou esteja perto da parede, anda
 			} else if (irSensor0 >= 2.0 || irSensor2 > 6.0) {
 				cif.DriveMotors(-0.1, 0.1);
 			}
 
 		}
 		
		// se o beacon estiver visível, tenta desviar-se dos obstáculos
 		if(beacon.beaconVisible) {
 			if(irSensor0 > 2.0 && irSensor1 >= irSensor2) cif.DriveMotors(0.1,-0.1);
 			else if(irSensor0 > 2.0 && irSensor1 < irSensor2) cif.DriveMotors(-0.1,0.1);
 			else if(irSensor1>2.0) cif.DriveMotors(0.1,0.0);
 			else if(irSensor2>2.0) cif.DriveMotors(0.0,0.1);
 		}
 		
 		//System.out.println(irSensor1 + " " + irSensor0 + " " + irSensor2);
 	}
 
 	/**
 	 * basic reactive decision algorithm, decides action based on current sensor values
 	 */
 	public void decide() {
 		getInfo();
 
 		//System.out.println("Measures: ir0=" + irSensor0 + " ir1=" + irSensor1 + " ir2=" + irSensor2 + "\n");
 		//System.out.println("Measures: x=" + x + " y=" + y + " dir=" + dir);
 
 		//System.out.println(robName + " state " + state);
 
 		switch(state) {
 		case RUN:    /* Go */
 			if( cif.GetVisitingLed() ) state = State.WAIT;
 			if( ground == 0 ) {         /* Visit Target */
 				cif.SetVisitingLed(true);
 				System.out.println(robName + " visited target at " + cif.GetTime() + "\n");
 			}
 
 			else {
 				wander(true);
 			}
 			break;
 		case WAIT: /* Wait for others to visit target */
 			if(cif.GetReturningLed()) state = State.RETURN;
 
 			cif.DriveMotors(0.0,0.0);
 			break;
 		case RETURN: /* Return to home area */
 
 			if( cif.GetFinished() ) System.exit(0); /* Terminate agent */
 			if( ground == 1) { /* Finish */
 				cif.Finish();
 				System.out.println(robName + " found home at " + cif.GetTime() + "\n");
 			}
 			else {
 				original_wander(false);
 			}
 			break;
 
 		}
 
 		requestInfo();
 	}
 
 	static void print_usage() {
 		System.out.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
 	}
 
 	private String robName;
 	private double irSensor0, irSensor1, irSensor2;
 	private beaconMeasure beacon;
 	private int    ground;
 	private int counter;
 	private int corners;
 	private State state;
 
 	private int beaconToFollow;
 };
 
