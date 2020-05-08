 /**
  * EntityScreen
  * 
  * Class representing the screen that manages user interactions 
  * pertaining to grid entities, including triggers and appearance.
  * 
  * @author Willy McHie
  * Wheaton College, CSCI 335, Spring 2013
  */
 
 package edu.wheaton.simulator.gui.screen;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.wheaton.simulator.entity.Prototype;
 import edu.wheaton.simulator.gui.Gui;
 import edu.wheaton.simulator.gui.HorizontalAlignment;
 import edu.wheaton.simulator.gui.PrefSize;
 import edu.wheaton.simulator.gui.ScreenManager;
 import edu.wheaton.simulator.gui.SimulatorGuiManager;
 import edu.wheaton.simulator.simulation.Simulator;
 
 public class EntityScreen extends Screen {
 
 	private static final long serialVersionUID = 8471925846048875713L;
 
 	private JList entityList;
 
 	private DefaultListModel listModel;
 	
 	private JButton delete;
 	
 	private JButton edit;
 	
 	public EntityScreen(final SimulatorGuiManager gm) {
 		super(gm);
 		this.setLayout(new GridBagLayout());
 		
 		listModel = new DefaultListModel();
 		entityList = new JList(listModel);
 		entityList.setBackground(Color.white);
 		entityList.setPreferredSize(new Dimension(400, 500));
 		entityList.setLayoutOrientation(JList.VERTICAL_WRAP);
 		entityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		entityList.setFixedCellWidth(400);
 		entityList.setVisibleRowCount(20);
 		entityList.setBorder(BorderFactory.createLineBorder(Color.RED));
 		entityList.addListSelectionListener( new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent le){
 					edit.setEnabled(!gm.hasSimStarted());
 			}
 		});
 		delete = Gui.makeButton("Delete",null,new DeleteListener());
 		edit = Gui.makeButton("Edit",null,new EditListener());
 		edit.setEnabled(false);
 		entityList.addListSelectionListener(
 				new ListSelectionListener() {
 					@Override
 					public void valueChanged(ListSelectionEvent e) {
 						edit.setEnabled(!gm.hasSimStarted());
 						delete.setEnabled(!gm.hasSimStarted());
 					}
 				}
 				);
 
 		//formatting needs a little work but this is now in GridBagLayout 
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 2;
		/*
 		this.add(
 			Gui.makeButton("Add",null,
 				new AddListener()),c); 
		*/
 		c.gridx = 1;
 		this.add(edit, c);
 		
 		c.gridx = 2;
 		this.add(delete, c);
 		
 		//The code between this comment and the next
 		c.gridx = 3;
 		this.add(
 			Gui.makeButton("Back",null,
 				new BackListener()),c);
 		
 		//will be removed once added to tabbed pane
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 4;
 		this.add(
 			Gui.makeLabel("Entities",new PrefSize(300, 100),HorizontalAlignment.CENTER), 
 			c);
 		
 		c.gridy = 1;
 		this.add(entityList, c);
 	}
 	
 	public void reset() {
 		listModel.clear();
 	}
 	
 	@Override
 	public void load() {
 		reset();
 		Set<String> entities = Simulator.prototypeNames();
 		for (String s : entities){
 			listModel.addElement(s);
 		}
 		delete.setEnabled(false); 
 		edit.setEnabled(false);
 	}
 	
 	public JList getEntityList(){
 		return entityList;
 	}
 	
 	class DeleteListener implements ActionListener {	
 		@Override
 		public void actionPerformed(ActionEvent e){
 			int index = entityList.getSelectedIndex();
 			Prototype.removePrototype(
 				(String)entityList.getSelectedValue()
 			);
 			listModel.remove(index);
 			int size = listModel.getSize();
 			if(size == 0){
 				delete.setEnabled(false);
 				edit.setEnabled(false);
 			}
 			if(index == size)
 				index--;
 			entityList.setSelectedIndex(index);
 			entityList.ensureIndexIsVisible(index);
 		}
 	}
 	
 	class AddListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			ScreenManager sm = getScreenManager();
 			EditEntityScreen screen = (EditEntityScreen)sm.getScreen("Edit Entities");
 			screen.load();
 			screen.setEditing(false);
 			sm.update(screen);
 		}
 	}
 	
 	class EditListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			ScreenManager sm = getScreenManager();
 			EditEntityScreen screen = (EditEntityScreen)sm.getScreen("Edit Entities");
 			screen.load(
 				(String)entityList.getSelectedValue());
 			screen.setEditing(true);
 			sm.update(screen);
 		}
 	}
 	
 	class BackListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e){
 			ScreenManager sm = getScreenManager();
 			sm.update(sm.getScreen("View Simulation"));
 		}
 	}
 }
