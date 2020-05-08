 package factory.graphics;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.swing.*;
 
 class PartsRobotClient extends JPanel implements ActionListener
 {
 	ArrayList<Nest> nests;
 	Station station;
 	int flashCount;
 	int flashLane;
 	Image flashImage;
 	int destinationNest;
 	PartsRobot robot;
 	static int frameWidth, frameHeight;
 	static JButton btnGotoCenter, btnGotoStation;
 
 	public PartsRobotClient()
 	{
 		// Initialize array
 		nests = new ArrayList<Nest>();
 		// Add the kitting station
		station = new Station(10,75,0,0,0,0,150,300,"kittingStation.png");
 		// Add the parts robot
		robot = new PartsRobot(frameWidth/2,frameHeight/2,0,5,5,10,100,100,"robot1.png");
 		// Add 8 nests
 		for(int i = 0; i < 8; i++)
 		{
			Nest newNest = new Nest(frameWidth-100,i*85+75,0,0,0,0,75,75,"nest3x3.png");
 			Random randomGen = new Random();
 			for(int j = 0; j < randomGen.nextInt(5)+4; j++)
				newNest.addItem(new GraphicItem(20,20,"CircleItem.png"));
 			nests.add(newNest);
 		}
 		// Initialize flash
 		flashCount = 0;				// flash is off
 		flashLane = 1;				// lane 1 by default; this will automatically be changed by code later
 		flashImage = Toolkit.getDefaultToolkit().getImage("Images/flash3x3.png");
 		//moveRobotTo(2);
 	}
 
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);					// required to clear the screen
 		final Graphics2D g2 = (Graphics2D)g.create();
 		// Draw all nests
 		for(int i = 0; i < nests.size(); i++)
 		{
 			Nest currentNest = nests.get(i);
 			// Draw nest
 			currentNest.paint(g);
 			// Draw flash?
 			if(flashCount > 0)		// is flash on?
 				if(flashLane-1 == i)	// is the flash for the current nest?
 					g2.drawImage(flashImage,currentNest.getX(),currentNest.getY(),currentNest.getImageWidth(),currentNest.getImageHeight(),this);
 		}
 		// Draw station
 		g2.drawImage(station.getImage(), station.getX(), station.getY(), station.getImageWidth(), station.getImageHeight(), this);
 		// Draw items on staiton
 		for(int i = 0; i < station.getSize(); i++)
 		{
 			station.getItemAt(i).paint(g, 5+station.getX()+(i/14)*20, 5+station.getY()+(i%14)*20);
 		}
 		g2.dispose();
 		//System.out.println(robot.getAngle());
 		// Draw robot
 		final Graphics2D g3 = (Graphics2D)g.create();
 		g3.rotate(Math.toRadians(360-robot.getAngle()), robot.getX()+robot.getImageWidth()/2, robot.getY()+robot.getImageHeight()/2);
 		g3.drawImage(robot.getImage(), robot.getX(), robot.getY(), robot.getImageWidth(), robot.getImageHeight(), this);
 		// Draw items robot is carrying
 		for(int i = 0; i < robot.getSize(); i++)
 		{
			robot.getItemAt(i).paint(g, robot.getX()+robot.getImageWidth()-25,robot.getY()+10+i*20);
 		}
 		g3.dispose();
 	}
 
 	public static void main(String arg[])
 	{
 	   JFrame frame = new JFrame("Parts Robot Client");
 	   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	   frameWidth = 720;
 	   frameHeight = 800;
 	   frame.setSize(frameWidth, frameHeight);
 	   
 	   PartsRobotClient panel = new PartsRobotClient();
 	   frame.setContentPane(panel);
 	   
 	   for(int i = 0; i < 8; i++)
 	   {
 		   JButton btnGotoNestX = new JButton("Nest "+(i+1)+" Full");
 		   btnGotoNestX.setActionCommand("gotoNest"+(i+1));
 		   btnGotoNestX.addActionListener(panel);
 		   frame.getContentPane().add(btnGotoNestX);
 	   }
 
 	   btnGotoCenter = new JButton("Goto Center"); btnGotoCenter.addActionListener(panel);
 	   btnGotoStation = new JButton("Goto Station"); btnGotoStation.addActionListener(panel);
 
 	   frame.getContentPane().add(btnGotoCenter);
 	   frame.getContentPane().add(btnGotoStation);
 	   
 	   frame.setVisible(true);
 	   
 	   new Timer(50, panel).start();
 	}
 	
 	public void moveRobotTo(int location)
 	{
 		// 0 = kitting station, 1-8 = lane #
 		if(location == -1)
 			robot.setDestination(frameWidth/2-robot.getImageWidth()/2, frameHeight/2-robot.getImageHeight()/2);
 		else if(location == 0)
 			robot.setDestination(station.getX()+station.getImageWidth(),station.getY()+station.getImageHeight()/2);
 		else if(location >= 1 && location <= 8)
 		{
 			robot.adjustShift(5);
 			robot.setDestination(nests.get(location-1).getX()-nests.get(location-1).getImageWidth()-10,nests.get(location-1).getY()-15);
 		}
 	}
 	
 	public void robotArrivedAtNest()
 	{
 		System.out.println("Debug:ARRIVED AT NEST");
 	}
 	
 	public void robotArrivedAtStation()
 	{
 		System.out.println("DEBUG: ARRIVED AT STATION");
 	}
 	
 	public void robotArrivedAtCenter()
 	{
 		System.out.println("DEBUG: ARRIVED AT CENTER");
 	}
 	
 	public void createFlash(int lifespan)
 	{
 		flashCount = 10;
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{
 		// Has robot arrived at its destination?
 		if(robot.getState() == 1)		// robot has arrived at nest
 		{
 			// Give item to robot
 			robot.addItem(nests.get(destinationNest-1).popItem());
 			robot.setState(2);
 			robotArrivedAtNest();
 		}
 		else if(robot.getState() == 4)	// robot has arrived at kitting station
 		{
 			for(int i = 0; i < robot.getSize(); i++)
 				station.addItem(robot.popItem());
 			robotArrivedAtStation();
 		}
 		else if(robot.getState() == 6)
 		{
 			robotArrivedAtCenter();
 		}
 		robot.move();							// Update position and angle of robot
 		if(flashCount > 0) flashCount -= 1;		// Deduct flash lifespan
 		repaint();
 		if(e.getActionCommand() != null && e.getActionCommand().startsWith("gotoNest"))
 		{
 			int nestNumber = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().length()-1));
 			flashCount = 10;
 			flashLane = nestNumber;
 			destinationNest = nestNumber;
 			moveRobotTo(nestNumber);
 			robot.setState(0);
 		}
 		else if(e.getSource() == btnGotoCenter)
 		{
 			robot.setState(5);
 			moveRobotTo(-1);
 		}
 		else if(e.getSource() == btnGotoStation)
 		{
 			robot.setState(3);
 			moveRobotTo(0);
 		}
 	}
 }
