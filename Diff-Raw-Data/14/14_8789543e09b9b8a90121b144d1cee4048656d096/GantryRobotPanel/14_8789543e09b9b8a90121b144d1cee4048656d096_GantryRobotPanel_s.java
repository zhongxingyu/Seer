 package factory.graphics;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Scanner;
 
 import factory.client.*;
 import factory.Part;
 
 public class GantryRobotPanel extends GraphicPanel implements ActionListener{
 	
 	public static final int WIDTH = 400, HEIGHT = 720;
 	
 	public GantryRobotPanel(JFrame GR) {
 		super();
 		isGantryRobotManager = true;
 		
 		if (GR instanceof Client)
 			am = (Client)GR;
 		
 		lane = new GraphicLaneManager [4];
 		for (int i = 0; i < lane.length; i++)
 			lane[i] = new GraphicLaneManager(-200, 160*i + 50, i, this);
 		
 		gantryRobot = new GantryRobot(WIDTH-150,HEIGHT/2,0,5,5,10,100,100,"Images/robot2.png");
 		
 		(new Timer(delay, this)).start();
 		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
 		this.setVisible(true);
 	}
 	
 	public void moveGantryRobotToPickup(String path) {
 		//System.out.println("Moving");
 		gantryRobot.setState(0);
 		gantryRobot.setDestination(WIDTH-100,-100);
 	}
 	
 	public void moveGantryRobotToFeeder(int feederIndex) {
 		gantryRobot.setState(3);
 		gantryRobot.setDestinationFeeder(feederIndex);
 		gantryRobot.setDestination(lane[feederIndex].feederX+95, lane[feederIndex].feederY+15);
 	}
 	
 	public void gantryRobotStateCheck() {
 		if(gantryRobot.getState() == 1)
 		{
 			gantryRobotArrivedAtPickup();
 		}
 		else if(gantryRobot.getState() == 4)
 		{
			lane[gantryRobot.getDestinationFeeder()].setBin(gantryRobot.takeBin());
			gantryRobotArrivedAtFeeder();
 		}
 	}
 	
 	/*public void paint(Graphics g) {
 		g.setColor(new Color(200, 200, 200));
 		g.fillRect(0, 0, getWidth(), getHeight());
 		if (lane != null)
 			for (int i = 0; i < lane.length; i++) {
 				if (lane[i] != null)
 					lane[i].paintFeeder(g);
 			}
 		
 		final Graphics2D g4 = (Graphics2D)g.create();
 		g4.rotate(Math.toRadians(360-gantryRobot.getAngle()), gantryRobot.getX()+gantryRobot.getImageWidth()/2, gantryRobot.getY()+gantryRobot.getImageHeight()/2);
 		gantryRobot.paint(g4);
 		// Draw bin gantryRobot is carrying
 		
 		g4.dispose();
 	}*/
 	
 	public void actionPerformed(ActionEvent arg0) {
 		gantryRobotStateCheck();
 		gantryRobot.move();
 		
 		repaint();
 	}
 	
 	public static void main(String args[]) {
 		JFrame f = new JFrame();
 		GantryRobotPanel gr = new GantryRobotPanel(f);
 		f.add(gr);
 		f.setVisible(true);
 		f.pack();
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Scanner kbr = new Scanner(System.in);
 		String command;
 		do {
 			command = kbr.nextLine();
 			switch (command.charAt(0)) {
 			case 'n': gr.moveGantryRobotToPickup("IMAGE PATH"); break;
 			case '1': gr.moveGantryRobotToFeeder(0); break;
 			case '2': gr.moveGantryRobotToFeeder(1); break;
 			case '3': gr.moveGantryRobotToFeeder(2); break;
 			case '4': gr.moveGantryRobotToFeeder(3); break;
 			case 'q': System.exit(0); break;
 			}
 		} while(command.charAt(0) != 'q');
 	}
 
 }
