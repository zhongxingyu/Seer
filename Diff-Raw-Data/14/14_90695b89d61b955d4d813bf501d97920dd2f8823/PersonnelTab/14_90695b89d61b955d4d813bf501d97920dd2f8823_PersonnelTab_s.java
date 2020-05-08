 package overwatch.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 
 import net.miginfocom.swing.MigLayout;
 
 public class PersonnelTab extends JPanel{
 	
 	private JPanel personnelPanel;
 	private JPanel listHolder;
 	
 	private JList namesList;
 	
 	private JLabel personnelListLabel;
 	private JLabel personnelNameLabel;
 	private JLabel detailsForLabel;
 	private JLabel personnelAgeLabel;
 	private JLabel personnelSexLabel;
 	private JLabel personnelRankLabel;
 	private JLabel personnelSalaryLabel;	
 	
 	private JTextField personnelName;
 	private JTextField personnelAge;
 	private JTextField personnelSex;
 	private JTextField personnelRank;
 	private JTextField personnelSalary;
 	
 	private JButton changeLogIn;
 	private JButton addNew;
 	private JButton save;
 	private JButton delete;
 	
 	
 	public PersonnelTab() 
 	{
 		//Create panel and components for the panel
 		personnelPanel 			= new JPanel(new MigLayout("debug"));
 		namesList				= new JList();
 		personnelListLabel 		= new JLabel("Personnel List");
 		personnelName			= new JTextField();
 		personnelNameLabel		= new JLabel("Name:");
 		detailsForLabel			= new JLabel("Details for ");  //A variable with the whatever selected from the list will go here
 		personnelAgeLabel		= new JLabel("Age:");
 		personnelAge			= new JTextField();
 		personnelSexLabel		= new JLabel("Sex:");
 		personnelRankLabel		= new JLabel("Rank:");
 		personnelSalaryLabel	= new JLabel("Salary");
 		personnelSex			= new JTextField();
 		personnelRank			= new JTextField();
 		personnelSalary			= new JTextField();
 		changeLogIn				= new JButton("Change Login details");
 		addNew					= new JButton("Add new");
 		save					= new JButton("Save");
 		delete					= new JButton("Delete");
 					
 		//Add stuff to the main panel
 		personnelPanel.add(personnelListLabel, "north");
 		personnelPanel.add(detailsForLabel, "wrap");
 		personnelPanel.add(personnelNameLabel);
 		personnelPanel.add(personnelName, "wrap");
 		personnelPanel.add(personnelAgeLabel);
 		personnelPanel.add(personnelAge, "grow, wrap");
 		personnelPanel.add(personnelSexLabel);
 		personnelPanel.add(personnelSex, "grow, wrap");
 		personnelPanel.add(personnelRankLabel);
 		personnelPanel.add(personnelRank, "grow, wrap");
 		personnelPanel.add(personnelSalaryLabel);
 		personnelPanel.add(personnelSalary, "grow, wrap");
 		personnelPanel.add(changeLogIn, "wrap");
 		personnelPanel.add(addNew);
 		personnelPanel.add(save);
 		personnelPanel.add(delete);
 		personnelPanel.add(namesList, "west");
 				
 		//Set  the size for the list
 		namesList.setPreferredSize(new Dimension(200,400));
 		personnelName.setPreferredSize(new Dimension(200, 25));
 	};
 	
 	
 	
 	
 	
 	
 	//Add action listeners
 	//Save button
 	public void savePersonnel(ActionListener e)
 	{
 		save.addActionListener(e);
 	}
 	
 	//DeleteButton
 	public void deletePersonnell(ActionListener e)
 	{
 		delete.addActionListener(e);
 	}
 	
 	//Add new button
 	public void addNewPersonnel(ActionListener e)
 	{
 		addNew.addActionListener(e);
 	}
 	
 	//Change login details
 	public void changeLogin(ActionListener e)
 	{
 		changeLogIn.addActionListener(e);
 	}
 	
 	
 
 }
