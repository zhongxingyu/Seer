 package jadeagents;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Random;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.TickerBehaviour;
 import jade.gui.GuiAgent;
 import jade.gui.GuiEvent;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.UnreadableException;
 import jade.wrapper.AgentContainer;
 import jade.wrapper.StaleProxyException;
 import jadeagents.Msg.RobotInform;
 
 public class World extends GuiAgent {
 	static final int TIME_LAPSE = 1000;
 	static final int robotDelta = -6;
 	static final int roomDelta = 1;
 	static final int startRange = 50;
 	static final int maxRange = 255;
 	
 	
 	static final String filepath = "D:/agent/";
 	String filename = filepath + "out.txt";
 	Gui gui;
 	ArrayList<String> cleanRooms;
 	PriorityQueue<Room> queue;
 	LinkedList<String> fifo;
 	HashMap<String, Room> map;
 	HashMap<String, String> robotMap;
 	String name;
 	int roomAmount, robotAmount;
 	Statistic stats;
 	protected void setup() {
 		name = getLocalName();
 		map = new HashMap<String, Room>();
 		robotMap = new HashMap<String, String>();
 		fifo = new LinkedList<String>();
 		gui = new Gui(this);
 		
 		// Create file
 		File f =new File(filename);
 		int k = 0;
 		while(f.exists()){
 			k++;
 			filename = filepath + "out" + k + ".txt";
 			f = new File(filename);
 		}
 		try {
 			f.createNewFile();
 			System.out.println("New data file created: " + filename);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		roomAmount = 30;
 		robotAmount = 7;
 		String tmp;
 		Random rand = new Random();
 		Room tmpRoom;
 		for (int i = 0; i < roomAmount; i++) {
 			tmp = "room"+i;
 			tmpRoom = new Room(rand.nextInt(startRange), tmp, 0);
 			map.put(tmp, tmpRoom);
 		}
 		
 		AgentContainer c = getContainerController();
 		//add robots
 		ArrayList<String> robots = new ArrayList<String>();
 		for(int i = 0; i < robotAmount; i++){
 			robots.add("robot"+i);
 		}
 		
 		Object [] args = new Object[1];
         args[0] = "room";
 		try {
 			for (int i = 0; i < robots.size(); i++) {
 				int roomrand = new Random().nextInt(roomAmount);
 				args[0] = "room"+roomrand;
 				c.createNewAgent( robots.get(i), "jadeagents.Robot", args).start();
 			}
 		}
 		catch (Exception e){
 			e.printStackTrace();
 		}
 		
 		addBehaviour(new Ticker(this, TIME_LAPSE));
 		addBehaviour(new Listen(this));
 		System.out.println(name + " is started!");
 	}
 	
 	class Listen extends CyclicBehaviour {
 		World w;
 		public Listen(World w){
 			super(w);
 			this.w = w;
 		}
 
 		@Override
 		public void action() {
 			ACLMessage msg = receive();
 			if (msg != null){
 				Object myObject = null;
 				try {
 					myObject = msg.getContentObject();
 				} catch (UnreadableException e1) {
 					System.err.println(name + " caught unreadable exception from " + msg.getSender().getLocalName());
 				}
 				
 				if (myObject instanceof Msg.RobotInform) {
 					RobotInform tmpMsg = (Msg.RobotInform) myObject;
 					Room getRoom = map.get(tmpMsg.room); 
 					int value = getRoom.dustlevel;
 					value += tmpMsg.delta;
 					if (value <= 0){
 						value = 0;
 					}
 					getRoom.dustlevel = value;
 					map.put(tmpMsg.room, getRoom);
 					robotMap.put(tmpMsg.robot, tmpMsg.room);
 					// Pop myself
 					boolean removed = false;
 					for(String str : fifo){
 						if (str.equals(tmpMsg.room)){
 							removed = true;
 							//System.err.println("Compare for removal: " + str + " vs " + tmpMsg.room + " " + removed + " LENGTH: " + fifo.size());
 							removed = false;
 							removed = fifo.remove(str);
 							//System.err.println("WAS REMOVED: " + removed + " length: " + fifo.size()) ;
 							break;
 						}
 					}
 					// UPDATE ROBOTS IN ROOMS
 					for (Entry<String, Room> entry : map.entrySet()){
 						String roo = entry.getKey();
 						Room room = entry.getValue();
 						room.robots = 0;
 						//System.out.println("Clear: " + room.name + " robots: " + room.robots);
 						map.put(roo, room);
 					}
 					for (Entry<String, String> entry : robotMap.entrySet()){
 						String rob = entry.getKey();
 						String roo = entry.getValue();
 						if (map.get(roo) != null){
 							Room tmpRoom = map.get(roo);
 							tmpRoom.robots++;
 							//System.out.println("Update: " + tmpRoom.name + " robots: " + tmpRoom.robots);
 							map.put(roo, tmpRoom);
 						}
 					}
 					stats = new Statistic(w);
 					System.out.println(new Date(System.currentTimeMillis()) + ": avg:\tq1:\tmedian:\tq3:");
 					System.out.println(new Date(System.currentTimeMillis()) + ": "+stats.avg + "\t" + stats.q1 + "\t"+stats.median+ "\t" + stats.q3);
 					gui.statusLbl.setText("Status - Robots: " + robotAmount + " - Rooms: " + roomAmount + " - Avg: " + stats.avg + " - Q1: " + stats.q1 + " - Q2: " + stats.median + " - Q: " + stats.q3 + " - Sum: " + stats.sum + " / " + roomAmount * 255 + " - fillrate: " + ((stats.sum*100) / (roomAmount*255)) + "%" );
 					ArrayList<Room> l = stats.getSortedList(map);
 					// check if robot needs to vacate his room
 					if (cleanRooms != null && cleanRooms.contains(tmpMsg.room)){
 						// tell robot to vacate
 						String newRoomForRobot = null;
 						for(int i = 0; i < l.size(); i++){
 							newRoomForRobot = l.get(i).name;
 							if (!fifo.contains(newRoomForRobot)){
 								fifo.add(newRoomForRobot);
 								//System.out.println("BREAK P : " + i);
 								break;
 							}
 						}
 						for(String s : fifo){
 							//System.out.println("FIFO: " + s);
 						}
 						// Try to pop
 						//System.out.println("room?: " + newRoomForRobot + " Length: " + fifo.size() + "Removed? " + removed);
 						
 						//System.out.println(tmpMsg.robot + " changes to room " + newRoomForRobot.name + " with level: " + newRoomForRobot.getCompareVal() + " / " + newRoomForRobot.dustlevel + " - Robots: " + newRoomForRobot.robots);
 						//System.out.println(tmpMsg.robot + " will be asked to go to: " + newRoomForRobot.stringTest);
 						msg = new ACLMessage(ACLMessage.INFORM);
 						msg.addReceiver(new AID(tmpMsg.robot, AID.ISLOCALNAME));
 						Msg.WorldInform msgToRobot = new Msg.WorldInform();
 						msgToRobot.robot = tmpMsg.robot;
 						msgToRobot.room = newRoomForRobot;
 						try {
 							msg.setContentObject(msgToRobot);
 							send(msg);
 							//robotMap.put(msgToRobot.robot, msgToRobot.room);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}	
 				gui.updateRoomPanes(map, robotMap);
 			}
 			block(1000);
 		}
 	}
 	
 	class Ticker extends TickerBehaviour {
 
 		public Ticker(Agent a, long period) {
 			super(a, period);
 		}
 
 		@Override
 		protected void onTick() {
 			String roomStr;
 			Room room;
 			int value;
 			String strout = new Date(System.currentTimeMillis()) + ": Updated values: ";
 			for (Entry<String, Room> entry : map.entrySet()){
 				roomStr = entry.getKey();
 				room = entry.getValue();
 				value = room.dustlevel;
 				value += roomDelta;
 				if (value >= maxRange){
 					value = maxRange;
 				}
 				room.dustlevel  = value;
 				map.put(roomStr, room);
 				strout += map.get(room)+"\t";
 			}
 			
 			// Pop item from fifo
 			if (fifo != null && !fifo.isEmpty()){
 				fifo.removeFirst();
 			}
 			
 			// find out if robot room is dirty or robot needs to change room
 			cleanRooms = new ArrayList<String>(); 
 			queue = new PriorityQueue<Room>();
 			//Room room; String roomStr;
 			if (stats != null){
 				for (Entry<String, Room> entry : map.entrySet()){
 					roomStr = entry.getKey();
 					room = entry.getValue();
 					value = room.dustlevel;
 					queue.add(new  Room(value, roomStr, room.robots));
 					//System.out.println("QUEUE: " + room.name + " - robots : " + room.robots);
 					//if (value < Math.abs(robotDelta)*2){ // Robot has nothing to do
 					if (value < stats.median){ // Robot has nothing to do
 						cleanRooms.add(roomStr);
 					}
 				}
 			}
 			
 			
 			
 			//System.out.println(strout);
 			/*
 			stats = new Statistic(map);
 			System.out.println(new Date(System.currentTimeMillis()) + ": avg:\tq1:\tmedian:\tq3:");
 			System.out.println(new Date(System.currentTimeMillis()) + ": "+stats.avg + "\t" + stats.q1 + "\t"+stats.median+ "\t" + stats.q3);
 			gui.statusLbl.setText("Status - Robots: " + robotAmount + " - Rooms: " + roomAmount);
 			
 			// find out if robot room is dirty or robot needs to change room
 			cleanRooms = new ArrayList<String>(); 
 			queue = new PriorityQueue<Room>();
 			for (Entry<String, Room> entry : map.entrySet()){
 				roomStr = entry.getKey();
 				room = entry.getValue();
 				value = room.dustlevel;
 				queue.add(new  Room(value, roomStr, room.robots));
 				//if (value < Math.abs(robotDelta)*2){ // Robot has nothing to do
 				if (value < stats.median){ // Robot has nothing to do
 					cleanRooms.add(roomStr);
 				}
 			}
 			*/
 			//gui.updateRoomPanes(map, robotMap);
 		}
 		
 	}
 	
 	@Override
 	protected void onGuiEvent(GuiEvent arg0) {
 		System.err.println("asdsd");
 	}
 
 	public void addAgent(){
 		robotAmount++;
 		AgentContainer c = getContainerController();
 		//add robots
 		Object [] args = new Object[1];
         args[0] = "room1";
 		try {
 			c.createNewAgent("robot"+robotAmount,"jadeagents.Robot", args).start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
	public void removeAgent(){
		if (robotAmount > 0){
 			AgentContainer c = getContainerController();
			//add robots
			Object [] args = new Object[1];
	        args[0] = "room1";
 			try {
				c.getAgent("robot"+robotAmount).kill();
 			} catch (Exception e) {
				e.printStackTrace();
 			}
 			robotAmount--;
 		}
 	}
 }
