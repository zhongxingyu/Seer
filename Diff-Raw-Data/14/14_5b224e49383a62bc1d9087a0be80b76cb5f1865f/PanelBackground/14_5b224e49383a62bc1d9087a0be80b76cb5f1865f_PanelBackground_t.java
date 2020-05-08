 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 public class PanelBackground extends JPanel
 {
 	public static final int MAX_VEHICLES = 2;
 	
 	private Image imgBackground;
 	private PanelRoad pnlRoads[] = new PanelRoad[MAX_VEHICLES];
 	private JButton btnStart;
 	
 	private Timer tmrMover;
 	private Timer tmrSpeed;
 	
 	private Vehicle vehicles[][] = 
 	{
 		{new VehicleGoogle(0), new VehicleTreadwheel(0)},
 		{new VehicleIronman(0), new VehicleBus(0)}
 	};
 	
 	public PanelBackground()
 	{
 		// Load image
 		imgBackground = new ImageIcon("res/background.png").getImage();
 		
 		// Set properties
 		setLayout(null);	// This enable free locating using setBounds or setLocation.
 		
 		// Create components
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			pnlRoads[i] = new PanelRoad(vehicles[i]);
 			pnlRoads[i].setLocation(0, 32*9 + 32*4*i);
 			pnlRoads[i].setBackground(new Color(0, 0, 0, 0));	// last parameter is to transparent background.
 		}
 		
 		btnStart = new ButtonStart(vehicles);
 		btnStart.setLocation(32*13, 32*18);
 		
 		// Add components
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			add(pnlRoads[i]);
 		}
 		add(btnStart);
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		// Draw background image
 		g.drawImage(imgBackground, 0, 0, this);
 	}
 	
 	public int getSelectedVehicleIndex(int idxRoad)
 	{
 		return pnlRoads[idxRoad].getSelectedVehicleIndex();
 	}
 	
 	public void hideControlComponents()
 	{
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			pnlRoads[i].hideControlComponents();	// This hide combobox
 		}
 		btnStart.setVisible(false);
 	}
 	
 	public void showControlComponents()
 	{
 		for (int i=0 ; i<pnlRoads.length ; i++)
 		{
 			pnlRoads[i].showControlComponents();	// This show combobox
 		}
 		btnStart.setVisible(true);
 	}
 	
 	public void startRacing()
 	{
 		final int delay = 3;
 		final PanelBackground parent = this;
 		final int selectedIndex[] = new int[vehicles.length];
 		final int speed[] = new int[vehicles.length];
 		final boolean isMagnet[] = new boolean[vehicles.length];
 		
 		for (int i=0 ; i<speed.length ; i++)
 		{
 			speed[i] = (int)(Math.random() * 2) + 1;
 		}
 		
 		// Get selected index in combobox
 		for (int i=0 ; i<selectedIndex.length ; i++)
 		{
 			selectedIndex[i] = getSelectedVehicleIndex(i);
 		}
 		
 		// Define class
 		class TimerMoverListener implements ActionListener
 		{
 			private boolean isStop;
 			
 			public void actionPerformed(ActionEvent event)
 			{
 				for (int i=0 ; i<vehicles.length ; i++)
 				{
 					vehicles[i][getSelectedVehicleIndex(i)].move(Vehicle.DIR_EAST);
 					
 					// Check this vehicle reach the finish line.
 					if (parent.getBounds().width <=
 						vehicles[i][selectedIndex[i]].getBounds().x +
 						vehicles[i][selectedIndex[i]].getBounds().width)
 					{
 						parent.stopRacing();
 						isStop = true;
 					}
 				}
 				
 				// If timer is stop(racing ended)...
 				int diff = vehicles[0][selectedIndex[0]].getBounds().x -
 						   vehicles[1][selectedIndex[1]].getBounds().x;
 				
 				// Skill
 				for (int i=0 ; i<vehicles.length ; i++)
 				{
 					if (vehicles[i][selectedIndex[i]].getIsSkill())
 					{
 						switch (vehicles[i][selectedIndex[i]].getName())
 						{
 						case "Google":
 							// Magnet
 							vehicles[0][getSelectedVehicleIndex(0)].setSpeed(speed[0] * 2 * (diff>0?-1:1));
 							break;
 						case "TreadWheel":
 							// Swap location
 							Rectangle tmpRect;
 							
 							tmpRect = vehicles[1][selectedIndex[1]].getBounds();
 							vehicles[1][selectedIndex[1]].setLocation(vehicles[0][selectedIndex[0]].getBounds().x,
 																	  vehicles[1][selectedIndex[1]].getBounds().y);
 							vehicles[0][selectedIndex[0]].setLocation(tmpRect.x, vehicles[0][selectedIndex[0]].getBounds().y);
 							vehicles[0][getSelectedVehicleIndex(0)].disableSkill();
 							break;
 						case "Ironman":
 							// Booster
 							if (speed[1] == 1)
 							{
 								vehicles[1][getSelectedVehicleIndex(1)].setSpeed(speed[1] * 2);
 							}
 							break;
 						case "Bus":
 							// Slow
 							if (speed[0] == 2)
 							{
 								vehicles[1][getSelectedVehicleIndex(1)].setSpeed(speed[1] / 2);
 							}
 							break;
 						}
 					}
 				}
 				
 				if (isStop)
 				{
 					// Player1 win
 					if (diff + vehicles[0][selectedIndex[0]].getBounds().width >
 							   vehicles[1][selectedIndex[1]].getBounds().width)
 					{
 						JOptionPane.showMessageDialog(parent, "Player1 win!");
 					}
 					// Player2 win
 					else if (diff + vehicles[0][selectedIndex[0]].getBounds().width <
 									vehicles[1][selectedIndex[1]].getBounds().width)
 					{
 						JOptionPane.showMessageDialog(parent, "Player2 win!");
 					}
 					// Draw
 					else
 					{
 						JOptionPane.showMessageDialog(parent, "Draw!");
 					}
 					
 					// Init
 					for (int i=0 ; i<vehicles.length ; i++)
 					{
 						for (int j=0 ; j<vehicles[i].length ; j++)
 						{
 							vehicles[i][j].initVehicle();
 						}
 					}
 				}
 			}
 		}
 		class TimerSpeedListener implements ActionListener
 		{
 			public void actionPerformed(ActionEvent e)
 			{
 				for (int i=0 ; i<vehicles.length ; i++)
 				{
 					speed[i] = (int)(Math.random() * 2) + 1; 
 					vehicles[i][selectedIndex[i]].setSpeed(speed[i]);
 				}
 				
 				// Init	magnet
 				for (int i=0 ; i<vehicles.length ; i++)
 				{
 					for (int j=0 ; j<selectedIndex.length ; j++)
 					{
 						vehicles[i][j].disableSkill();
 					}
 				}
 				
 				int p = (int)(Math.random() * 101);
 				int diff = vehicles[0][selectedIndex[0]].getBounds().x -
 						   vehicles[1][selectedIndex[1]].getBounds().x;
 				
 				// Magnet
				if (selectedIndex[0] == 0 && diff < 0 && 0 <= p && Math.abs(diff) / 1024 * 100 <= p)
 				{
 					System.out.println("Manget 1 to 2(" +
 							(((Math.abs(diff) / 10) * 3) - (Math.abs(diff) / 10)) + "%)");
					vehicles[0][selectedIndex[1]].enableSkill();
 				}
 				// Swap location
				if (selectedIndex[0] == 1 && diff < 0 && 0 <= p && Math.abs(diff) / 1024 * 100 / 2 <= p)
 				{
 					System.out.println("Swap location skill affected(" + Math.abs(diff) / 10 + "%)");
 					vehicles[0][selectedIndex[0]].enableSkill();
 				}
 				// Booster
				if (selectedIndex[1] == 0 && 0 <= p && p < 30 && diff < 0)
 				{
 					System.out.println("Bootster("  + 40 + "%)");
 					vehicles[1][selectedIndex[0]].enableSkill();
 				}
 				// Slower
				if (selectedIndex[1] == 1 && 0 <= p && p < 30 && diff > 0)
 				{
 					System.out.println("Slower("  + 40 + "%)");
 					vehicles[1][selectedIndex[1]].enableSkill();
 				}
 			}
 		}
 		tmrMover = new Timer(delay, new TimerMoverListener());
 		tmrMover.start();
 		
 		tmrSpeed = new Timer(delay*256, new TimerSpeedListener());
 		tmrSpeed.start();
 		
 		hideControlComponents();
 	}
 	
 	public void stopRacing()
 	{
 		tmrMover.stop();
 		tmrSpeed.stop();
 		
 		showControlComponents();
 	}
 }
