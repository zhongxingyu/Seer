 package cx.it.hyperbadger.spacezombies;
 
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import cx.it.hyperbadger.spacezombies.explorer.Mass;
 import cx.it.hyperbadger.spacezombies.explorer.Planet;
 import cx.it.hyperbadger.spacezombies.explorer.Ship;
 import cx.it.hyperbadger.spacezombies.explorer.SolarSystem;
 import cx.it.hyperbadger.spacezombies.explorer.Sun;
 import cx.it.hyperbadger.spacezombies.gui.GUI;
 
 public class Console implements Runnable{
 	private Ship ship;
 	private SolarSystem sol;
 	private GUI gui;
 	private boolean run = true;
 	private Game game;
 	BufferedReader in; 
 	public Console(Ship s, SolarSystem sol, GUI gui, Game game){
 		this.ship = s;
 		this.sol = sol;
 		this.gui = gui;
 		this.game = game;
 		in = new BufferedReader(new InputStreamReader(System.in)); 
 	}
 	@Override
 	public void run() {
 		while(run){
 			String line;
 			try {
 				while((line = in.readLine()) != null) {
 					game.addCommand(line);
 				}
 			} catch (IOException e) {
 				System.err.println("Could not read terminal");
 			}
 			Thread.yield();
 		}
 	}
 	public void processLine(String line){
 		if(line.contains(" ")){
 			String[] split = line.split(" ");
 			String a = split[0];
 			String b = split[1];
 			if("setlocation".equals(a)&&split.length==4){
 				String c = split[2],d = split[3];
 				Mass m = sol.findMass(b);
 				if(m==null){ return; }
 				try{
 					double x = Double.parseDouble(c);
 					double y = Double.parseDouble(d);
 					m.setX(x);
 					m.setY(y);
 					System.out.println(m.getName()+" moved to ("+x+","+y+")");
 				}catch(NumberFormatException e){
 					printCommands();
 					return;
 				}
 			}else if("setlocation".equals(a)&&split.length==3){
 					String c = split[2];
 					Mass m = sol.findMass(b);
 					Mass n = sol.findMass(c);
 					if(m==null||n==null){ 
 						printCommands();
 						return; 
 					}
 					if((n instanceof Planet)||(n instanceof Sun)){
 						m.setX(n.getX()+(n.getCollisionRadius()/100*105));
 						m.setY(n.getY());
 					}else{
 					m.setLocation(n.getLocation());
 					}
 					System.out.println(m.getName()+" moved to ("+m.getLocation()+")");
 			}else if("setscale".equals(a)){
 				try{
 					double scale = Double.parseDouble(b);
 					gui.setScale(scale);
 				}catch(NumberFormatException e){
 					return;
 				}
 			}else if("setfocus".equals(a)&&split.length==2){
 				Mass m = sol.findMass(b);
 				if(m==null){ 
 					printCommands();
 					return;
 				}
 				game.setFocus(m);
 			}else if("setfocus".equals(a)&&split.length==3){
 				String c = split[2];
 				try{
 					double x = Double.parseDouble(b);
 					double y = Double.parseDouble(c);
 					game.setFocus(new Point2D.Double(x,y));
 				}catch(NumberFormatException e){
 					printCommands();
 					return;
 				}
 			}else if("setforce".equals(a)){
 				try{
 					double f = Double.parseDouble(b);
 					ship.setForce(f);
 				}catch(NumberFormatException e){
 					printCommands();
 					return;
 				}
 			}else if("auto".equals(a)&&split.length==3){
 				String c = split[2];
 				if("enable".equals(b)){
 					if("true".equals(c)){
 						ship.setAuto(true);
 					}else{
 						ship.setAuto(false);
 					}
 				}else if("velocity".equals(b)){
 					try{
 						Double v = Double.parseDouble(c);
 						ship.setAutoVelocity(v);
 					}catch(NumberFormatException e){
 						printCommands();
 						return;
 					}
 				}
 			}
 		}else{
 			printCommands();
 		}
 	}
 	public void stop(){
 		run = false;
 	}
 	private void printCommands(){
 		pc("setlocation <Mass> <x> <y>");
 		pc("setlocation <Mass> <Mass>");
 		pc("setfocus <Mass>");
 		pc("setfocus <x> <y>");
 		pc("setscale <scale>");
 	}
 	private void pc(String s){
 		System.out.println(s);
 	}
 }
