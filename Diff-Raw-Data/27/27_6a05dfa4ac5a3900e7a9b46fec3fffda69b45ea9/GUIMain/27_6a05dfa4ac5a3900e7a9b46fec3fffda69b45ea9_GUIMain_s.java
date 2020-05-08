 package common;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import common.FileManager.FileManager;
 import common.FileManager.ListProcessor;
 
 public class GUIMain extends JFrame implements ActionListener {
 	
 	public String[] display = new String[]{"1","2","3","4","5","6"};
 	public String[] disList = new String[300];	
 	
 	public int disCount = 0;
 	
 	public boolean firstUpdate = true;
 	
 	Icon updateB = new ImageIcon(getClass().getResource("update.png"));
 	Icon hovUpdateB = new ImageIcon(getClass().getResource("hovupdate.png"));
 	Image background = new ImageIcon(getClass().getResource("hovupdate.png")).getImage();
 	
 	private static final long serialVersionUID = 2696364157973172973L;
 	
 	private JPanel pnl = new JPanel();
 	private JButton updateButton = new JButton("",updateB);;
 	private JButton Up = new JButton("U",updateB);;
 	private JButton Down = new JButton("D",updateB);;
 	private JTextArea consoleOut = new JTextArea(30,100);
 	private JScrollPane area;
 	public GUIMain()
 	{
 		super("UE Mod Downloader");
 		if(firstUpdate)
 		{
 			disList[0] = "Starting up...";
 			firstUpdate = false;
 		}
 		setSize(800,600);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//this.setResizable(false);
 		this.setLayout(null);
 		addTextBox(consoleOut,pnl,10, 470, 650, 100);
 		addButton(updateButton,pnl,690, 470, 100, 100);
 		addButton(Up,pnl,660, 470, 20, 40);
 		addButton(Down,pnl,660, 530, 20, 40);
 		//console box
 		consoleOut.setEditable(false);
 		consoleOut.setWrapStyleWord(true);
 		consoleOut.setText("Debug Console");
 	     //update button		
 		updateButton.setRolloverIcon(hovUpdateB);
 		updateButton.setBorderPainted(false);
 				
 		
 		this.setBackground(Color.red);
 		
 		setVisible(true);
 
 	}
 	private void adjDisplay()
 	{
 		for(int i = 0; i<6;i++)
 		{
 			if(disList[i+disCount] != null)
 			{
 				display[i] = disList[i+disCount];
 			}
 			else
 			{
 				display[i] = "";
 			}
 		}
 		consoleOut.setText( display[0] + "\n" + display[1] + "\n" + display[2] + "\n" + display[3] + "\n" + display[4] + "\n" + display[5]) ;
 		consoleOut.repaint();
 	}
 	private void addButton(JButton button,JPanel pn, int x, int y, int h,int w)
 	{
 		add(button);
 		button.setBounds(x,y,h,w);
 		button.addActionListener(this);
 	}
 	private void addTextBox(Component box,JPanel pn,int x, int y, int h,int w)
 	{		
 		add(box);
 		box.setBounds(x,y,h,w);
 	}
 	public void addToConsole(String msg)
 	{
 		for(int i = 0; i < disList.length; i++)
 		{
 			if(disList[i] == null && disList[i+1] == null)
 			{
 				disList[i] = msg;
 				break;
 			}
 		}
 		if(disList[7] != null)
 		{
 			disCount++;
 		}
 		adjDisplay();
 	}
 		@Override
 		public void actionPerformed(ActionEvent event) 
 		{
 			if(event.getSource() == updateButton)
 			{
 				updateButton.setEnabled(false);
 				addToConsole("Checking root files");
 				Boolean fileExist = FileManager.rootFileCheck();
 				if(fileExist)
 				{
 					
 					if(FileManager.errors.size() > 0)
 					{
						addToConsole("Main file check clear with errors:");
 						for(int i = 0; i < FileManager.errors.size(); i++)
 						{
 							addToConsole("   "+FileManager.errors.get(i));
 						}
 					}
 					else
 					{
 						addToConsole("root file check cleared");
 					}
 					Boolean uc = FileManager.updateList();
 					if(!uc)
 					{
 						addToConsole("Critical:Can't update list.");
 						if(new File(FileManager.updaterDir+"/ModList.list").exists())
 						{
 							addToConsole("Defaulting to old list");
 						}
 						else
 						{
 							addToConsole("Critical:Can't find any mod lists.");
 						}
 					}
 					boolean listRan = ListProcessor.ProcessorUpdateList();
 					List debug = ListProcessor.debug;
 					if(ListProcessor.debug.size() > 0)
 					{
 						addToConsole("Starting Update Processor");
 						for(int i = 0; i < ListProcessor.debug.size(); i++)
 						{
 							addToConsole(ListProcessor.debug.get(i));
 						}
 					}
 				}
 				else
 				{
 					addToConsole("Critical:Main file check failed");
 					if(FileManager.errors.size() > 0)
 					{
 						addToConsole("Failure errors:");
 						for(int i = 0; i < FileManager.errors.size(); i++)
 						{
 							addToConsole("   "+FileManager.errors.get(i));
 						}
 					}
 				}
 				updateButton.setEnabled(true);
 			}
 			if(event.getSource() == Up)
 			{
 				if( disCount-1 > 1 &&disList[disCount] !=null)
 				{
 					disCount--;
 					adjDisplay();
 				}
 			}
 			if(event.getSource() == Down)
 			{
 				if(disCount >0)
 				{
 					disCount++;
 					adjDisplay();
 				}
 			}
 		}
 		
 	
 }
