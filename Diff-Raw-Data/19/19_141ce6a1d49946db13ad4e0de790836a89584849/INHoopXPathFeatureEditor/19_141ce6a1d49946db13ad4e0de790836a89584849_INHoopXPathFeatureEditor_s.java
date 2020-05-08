 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.controls;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 //import java.io.File;
 //import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 //import javax.swing.JFileChooser;
 //import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 
 import edu.cmu.cs.in.base.INHoopLink;
 //import edu.cmu.cs.in.controls.INJFeatureList;
 import edu.cmu.cs.in.controls.base.INEmbeddedJPanel;
 import edu.cmu.cs.in.controls.base.INHoopJTable;
 //import edu.cmu.cs.in.controls.base.INJInternalFrame;
 
 /**
  * 
  */
 public class INHoopXPathFeatureEditor extends INEmbeddedJPanel implements ActionListener, TableModelListener
 {
 	private static final long serialVersionUID = 1L;
 	
     //private JFileChooser fc=null;
 	private INHoopJTable featureList=null;
 	private JButton addFeature=null;
 	private JButton deleteFeature=null;
 	private  JComboBox featureCombo=null;
 	private JTextField featureName=null;
 	
 	/**
 	 * 
 	 */
 	public INHoopXPathFeatureEditor ()  
     {
     	//super("StopWord Editor", true, true, true, true);
     	
 		setClassName ("INHoopXPathFeatureEditor");
 		debug ("INHoopXPathFeatureEditor ()");    	
     
 	    Box buttonBox = new Box (BoxLayout.X_AXIS);
 	    buttonBox.setBorder(BorderFactory.createEmptyBorder(1,1,1,2));
 	    
 	    featureName=new JTextField ();
 	    featureName.setFont(new Font("Dialog", 1, 8));
 	    featureName.setPreferredSize(new Dimension (100,20));
 	    featureName.setMaximumSize(new Dimension (100,20));
 	    buttonBox.add (featureName);
 	    
 	    addFeature=new JButton ();
 	    addFeature.setFont(new Font("Dialog", 1, 8));
 	    addFeature.setPreferredSize(new Dimension (20,20));
 	    addFeature.setMaximumSize(new Dimension (20,20));
 	    addFeature.setIcon(INHoopLink.getImageByName("gtk-add.png"));
 	    addFeature.addActionListener(this);
 	    buttonBox.add (addFeature);
 	    
 	    deleteFeature=new JButton ();
 	    deleteFeature.setFont(new Font("Dialog", 1, 8));
 	    deleteFeature.setPreferredSize(new Dimension (20,20));
 	    deleteFeature.setMaximumSize(new Dimension (20,20));
 	    deleteFeature.setIcon(INHoopLink.getImageByName("delete.gif"));
 	    //foldButton.setText("None");
 	    deleteFeature.addActionListener(this);
 	    buttonBox.add (deleteFeature);
 	    
 	    String[] featureStrings = { "Boolean", "Integer", "String", "URI", "Date" };
 
 	    featureCombo = new JComboBox(featureStrings);
 	    //featureCombo.setSelectedIndex(4);
 
 	    featureCombo.setFont(new Font("Dialog", 1, 8));
 	    featureCombo.setPreferredSize(new Dimension (100,20));
 	    featureCombo.setMaximumSize(new Dimension (100,20));
 	    featureCombo.addActionListener(this);
 	    	    
 	    buttonBox.add(featureCombo);
 	    
 	    buttonBox.add(Box.createHorizontalGlue());		
 									
 		Box stopBox = new Box (BoxLayout.Y_AXIS);
 		stopBox.setMinimumSize(new Dimension (50,50));
 		stopBox.setPreferredSize(new Dimension (150,5000));
 		stopBox.setMaximumSize(new Dimension (150,5000));
 		
 		String[] columnNames = {"Name","XPath","Value"};
 		
 		DefaultTableModel featureModel=new DefaultTableModel (null,columnNames);
         featureModel.addTableModelListener (this);
 		
 		featureList=new INHoopJTable ();
 		featureList.setModel(featureModel);
 		
 		JScrollPane posScrollList = new JScrollPane (featureList);
 		
 		INHoopXMLTreeView xmlTree=new INHoopXMLTreeView ();
 		xmlTree.setMinimumSize(new Dimension (50,50));
		xmlTree.setPreferredSize(new Dimension (150,150));
 		
 		stopBox.add(xmlTree);
 		stopBox.add(buttonBox);
 		stopBox.add(posScrollList);		
 		
 		setContentPane (stopBox);		
     }
 	/**
 	 * 
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event) 
 	{
 		debug ("actionPerformed ("+event.getActionCommand()+")");
 		
 		//int returnVal=0;
 		
 		String act=event.getActionCommand();
 		JButton button = (JButton)event.getSource();
 
 		/*
 		//>---------------------------------------------------------
 				
 		if (loadStops==button)
 		{
 
 		}			
 		
 		//>---------------------------------------------------------		
 				
 		if (saveStops==button)
 		{
 			
 		}
 		
 		//>---------------------------------------------------------
 		 * 
         JComboBox cb = (JComboBox)e.getSource();
         String petName = (String)cb.getSelectedItem();
         updateLabel(petName); 
  
 		*/
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void tableChanged(TableModelEvent arg0) 
 	{
 		debug ("tableChanged ()");
 		
 	}  
 }
