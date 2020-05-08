 package jsc_controller;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import jsc_server.CantFindMachine;
 import jsc_server.MenuItem;
 import translation.*;
 
 public class Jsc_panel extends JPanel implements ActionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	//private static String ADD_COMMAND = "add";
 	//private static String REMOVE_COMMAND = "remove";
 	//private static String CLEAR_COMMAND = "clear";
 	private static String WAKEUP_COMMAND = "wol";
 	private static String SHUTDOWN_COMMAND = "shutdown";
 	private static String REBOOT_COMMAND = "reboot";
 	private static String REFRESH_COMMAND = "refresh";
 	private DynamicTree treePanel;
 	//public static JTextField currentSelectionField;
 	
 	//private Machine[] machines;
 	
 	private ArrayList<Group> groups;
 
 	/*
 	public Jsc_panel (Machine[] machines) {
 		super(new GridLayout(0, 1));
 		
 		String[] columnNames = {"Maskin",
                 "Siste respons",
                 "Status",
                 "Sl p",
                 "Sl av",
                 "Restart"};
 		
 		Object[][] data = new Object[machines.length][5];
 		for (int i = 0; i < machines.length; i++) {
 			data[i][0] = machines[i].getName();
 			data[i][1] = (int)(machines[i].getLastPing()/1000);
 			data[i][2] = machines[i].getStatusText();
 			data[i][3] = "";
 			data[i][4] = "";
 			System.out.println("i = " + i);
 		}
 		
 		final JTable table = new JTable(data, columnNames);
         table.setPreferredScrollableViewportSize(new Dimension(500, 70));
         table.setFillsViewportHeight(true);
         
         if (DEBUG) {
             table.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     printDebugData(table);
                 }
             });
         }
 
         //Create the scroll pane and add the table to it.
         JScrollPane scrollPane = new JScrollPane(table);
 
         //Add the scroll pane to this panel.
         add(scrollPane);
 	}*/
 	public Jsc_panel (MenuItem[] menyelementer) {
 		super(new BorderLayout());
 		
 		groups = new ArrayList<Group>();
 		
 		this.getGroups();
 		for (int i = 0; i < menyelementer.length; i++) {
 			this.addGroupContent(0, menyelementer[i]);
 		}
 		
 		//Create the components.
 		treePanel = new DynamicTree();
 		populateTree(treePanel);
 
 		JButton wakeupButton = new JButton("Sl p");
 		wakeupButton.setActionCommand(WAKEUP_COMMAND);
 		wakeupButton.addActionListener(this);
 		
 		JButton shutdownButton = new JButton("Sl av");
 		shutdownButton.setActionCommand(SHUTDOWN_COMMAND);
 		shutdownButton.addActionListener(this);
 
 		JButton rebootButton = new JButton("Restart");
 		rebootButton.setActionCommand(REBOOT_COMMAND);
 		rebootButton.addActionListener(this);
 		
 		JButton refreshButton = new JButton("Oppdater listen");
 		refreshButton.setActionCommand(REFRESH_COMMAND);
 		refreshButton.addActionListener(this);
 		
 		//Lay everything out.
 		//treePanel.setPreferredSize(new Dimension(500, 315));
 		add(treePanel, BorderLayout.CENTER);
 		
 		JPanel panel = new JPanel(new GridLayout(0,4));
 		//panel.add(addButton);
 		//panel.add(removeButton); 
 		//panel.add(clearButton);
 		panel.add(wakeupButton);
 		panel.add(shutdownButton);
 		panel.add(rebootButton);
 		panel.add(refreshButton);
 		add(panel, BorderLayout.SOUTH);
 }
 	
 	//public void populateTree(DynamicTree treePanel, Machine[] machines) {
 	public void populateTree(DynamicTree treePanel) {
 		
 		/*
 		String p1Name = new String("Parent 1");
 		String p2Name = new String("Parent 2");
 		String c1Name = new String("Child 1");
 		String c2Name = new String("Child 2");
 		
 		DefaultMutableTreeNode p1, p2;
 		
 		p1 = treePanel.addObject(null, p1Name);
 		p2 = treePanel.addObject(null, p2Name);
 		
 		treePanel.addObject(p1, c1Name);
 		treePanel.addObject(p1, c2Name);
 		
 		treePanel.addObject(p2, c1Name);
 		treePanel.addObject(p2, c2Name);*/
 		
 		/*
 		DefaultMutableTreeNode gruppe1;
 		gruppe1 = treePanel.addObject(null, "Alle maskiner");
 		
 		treePanel.machines = machines;
 		for (int i = 0; i < machines.length; i++) {
 			//treePanel.addObject (gruppe1, "" + i + " (" + machines[i].getStatusText() + ")" + " " + machines[i].getName() );
 			treePanel.addObject (gruppe1, machines[i]);
 		}*/
 		
 		// Leser grupper
 		if(groups.size() > 0)
 		{
 			DefaultMutableTreeNode gruppeX;
 			for (int i = 0; i < groups.size(); i++) {
 				gruppeX = treePanel.addObject(null, groups.get(i));
 				MenuItem[] content = groups.get(i).getContent();
 				for (int j = 0; j < groups.get(i).getContentNum(); j++) {
 					//treePanel.addObject (gruppeX, (Machine)content[j]);
 					treePanel.addObject (gruppeX, content[j]);
 				}
 			}
 		}
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		
 		if (WAKEUP_COMMAND.equals(command)) {
 			treePanel.currentSelected().object.wakeup();
 		} else if (SHUTDOWN_COMMAND.equals(command)) {
 			treePanel.currentSelected().object.shutdown();
 		} else if (REBOOT_COMMAND.equals(command)) {
 			treePanel.currentSelected().object.reboot();
 			treePanel.repaint();
 		} else if (REFRESH_COMMAND.equals(command)) {
 			treePanel.clear();
 			populateTree(treePanel);
 			treePanel.reloadTree();
 		}
 	}
 	
 	/*
 	public void reloadMachines () {
 		treePanel.clear();
 		for (int i = 0; i < machines.length; i++) {
 			try {
 				machines[i].loadConfig();
 			} catch (FileNotFoundException z) {
 				
 			}
 		}
 		populateTree(treePanel);
 		treePanel.reloadTree();
 	}*/
 	
 	public void getGroups () {
 		// Make the group "All machines"
 		this.addGroup(T.t("All machines"));
 		
 		// Getting groups
 		File gruppefil = new File(System.getProperty("user.home") + File.separatorChar + "jsc_config" + File.separatorChar + "groups.conf");
 		
		if(gruppefil.exists())
 		{
 			// Behandle
 			FileInputStream fis = null;
 			BufferedInputStream bis = null;
 			DataInputStream dis = null;
 			
 			try {
 				fis = new FileInputStream(gruppefil);
 				
 				// Here BufferedInputStream is added for fast reading.
 				bis = new BufferedInputStream(fis);
 				dis = new DataInputStream(bis);
 				String line;
 				int ny_id = 0;
 				// dis.available() returns 0 if the file does not have more lines.
 				while (dis.available() != 0) {
 					// this statement reads the line from the file and print it to
 					// the console.
 					line = dis.readLine();
 					if(!line.equals(""))
 					{
 						if(line.startsWith("[") && line.length() > 2)
 						{
							// Ny gruppe
 							ny_id = groups.size();
 							this.addGroup(line.substring(1, line.length()-1));
 						}
 						else if (line.startsWith("projectorNEC ") && line.length() > 13) {
 							try {
 								ProjectorNEC element = new ProjectorNEC (line.substring(13));
 								this.addGroupContent(ny_id, element);
 							} catch (CantFindMachine e) {
								
 							}
 						}
 						else {
 							this.addGroupContent(ny_id, line);
 						}
 					}
 				}
 				
 				// dispose all the resources after using them.
 				fis.close();
 				bis.close();
 				dis.close();
 				
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void addGroup(String gruppe_navn) {
 		groups.add(new Group(gruppe_navn));
 	}
 
 	public void addGroupContent (int gruppe_num, String innhold_navn) {
 		
 		groups.get(gruppe_num).addContent (innhold_navn);
 	}
 	
 	public void addGroupContent (int gruppe_num, MenuItem maskin) {
 		groups.get(gruppe_num).addContent (maskin);
 	}
 }
