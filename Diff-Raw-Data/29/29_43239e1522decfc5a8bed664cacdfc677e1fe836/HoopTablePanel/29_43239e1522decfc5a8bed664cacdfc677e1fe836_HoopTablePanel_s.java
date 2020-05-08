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
 
 package edu.cmu.cs.in.hoop;
 
 //import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 //import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 //import javax.swing.border.Border;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 
 import edu.cmu.cs.in.base.kv.HoopKV;
 import edu.cmu.cs.in.base.kv.HoopKVType;
 import edu.cmu.cs.in.controls.base.HoopEmbeddedJPanel;
 import edu.cmu.cs.in.controls.base.HoopJTable;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 
 /** 
  * @author vvelsen
  *
  */
 public class HoopTablePanel extends HoopEmbeddedJPanel implements ActionListener
 {	
 	private static final long serialVersionUID = 1L;
 			
 	private HoopJTable table=null;
 	
 	private JTextField status=null;
     private JTextField minRange=null;
     private JTextField maxRange=null;    
     private JButton setRange=null;    
     private JButton previousSet=null;
     private JButton nextSet=null;	
     
 	private int primColWidth=100;
 	
 	private String[] columnNames = {"Key","Value"};	
 	
 	/**
 	 * http://stackoverflow.com/questions/965023/how-to-wrap-lines-in-a-jtable-cell
 	 */	
 	public HoopTablePanel()
 	{
 		setClassName ("HoopTablePanel");
 		debug ("HoopTablePanel ()");
 		
     	Box holder = new Box (BoxLayout.Y_AXIS);    	
 		Box controlBox = new Box (BoxLayout.X_AXIS);
 		
 		//Border border=BorderFactory.createLineBorder(Color.black);
 		
 	    minRange=new JTextField ();
 	    minRange.setText("0");
 	    minRange.setFont(new Font("Dialog", 1, 10));
 	    minRange.setMinimumSize(new Dimension (50,18));
 	    minRange.setPreferredSize(new Dimension (50,18));
 	    controlBox.add(minRange);
 	    	    	    	    
 	    maxRange=new JTextField ();
 	    maxRange.setText("100");
 	    maxRange.setFont(new Font("Dialog", 1, 10));
 	    maxRange.setMinimumSize(new Dimension (50,20));
 	    maxRange.setPreferredSize(new Dimension (50,20));
 	    controlBox.add(maxRange);
 	    	    
 	    setRange=new JButton ();
 	    setRange.setMargin(new Insets(1,1,1,1));
 	    setRange.setText("Set");
 	    setRange.setFont(new Font("Dialog", 1, 10));
 	    setRange.setMinimumSize(new Dimension (60,20));
 	    setRange.setPreferredSize(new Dimension (60,20));
 	    setRange.addActionListener(this);
 	    controlBox.add(setRange);	    
 	    
 	    previousSet=new JButton ();
 	    previousSet.setMargin(new Insets(1,1,1,1));
 	    previousSet.setText("Previous");
 	    previousSet.setFont(new Font("Dialog", 1, 10));
 	    previousSet.setMinimumSize(new Dimension (60,20));
 	    previousSet.setPreferredSize(new Dimension (60,20));
 	    previousSet.addActionListener(this);
 	    controlBox.add(previousSet);
 	    
 	    nextSet=new JButton ();
 	    nextSet.setMargin(new Insets(1,1,1,1));  
 	    nextSet.setText("Next");
 	    nextSet.setFont(new Font("Dialog", 1, 10));
 	    nextSet.setMinimumSize(new Dimension (60,20));
 	    nextSet.setPreferredSize(new Dimension (60,20));
 	    nextSet.addActionListener(this);
 	    controlBox.add(nextSet);	    
 	    
 	    controlBox.add(new JSeparator(SwingConstants.VERTICAL));
 
 	    status=new JTextField ();
 	    status.setText("Status: OK");
 	    status.setEditable(false);
 	    //status.setBorder(border);
 	    status.setFont(new Font("Dialog", 1, 10));
 	    status.setMinimumSize(new Dimension (20,20));
 	    status.setPreferredSize(new Dimension (100,20));
 	    controlBox.add(status);	    
 
 		//controlBox.add(Box.createHorizontalGlue());
 		
 		controlBox.setMinimumSize(new Dimension (100,24));
 		controlBox.setPreferredSize(new Dimension (100,24));		
 
 		// We need some empty data because Java crashes when you provide a null parameter in the constructor
 		Object[][] data ={}; 
 		
 		table=new HoopJTable (data,columnNames);
 		
 		JScrollPane scrollPane = new JScrollPane(table);
 		//table.setFillsViewportHeight(true);		
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		
 		holder.add(controlBox);
 		holder.add (scrollPane);
 		
 		this.add(holder);		
 	}
 	/**
 	 * 
 	 */	
 	public HoopJTable getTable() 
 	{
 		return table;
 	}
 	/**
 	 * 
 	 */
 	public void showHoop (HoopBase aHoop)
 	{
 		debug ("showHoop ()");
 		
 		//TableColumnModel columnModel=table.getColumnModel();
 		
 		ArrayList <HoopKV> content=aHoop.getData();
 					
 		// Convert KV model to table model and show
		
		String[] cNames = new String [aHoop.getMaxValues()+1];
		
 		ArrayList <HoopKVType> types=aHoop.getTypes();
 		
 		for (int n=0;n<types.size();n++)
 		{
 			HoopKVType aType=types.get(n);
 			
 			cNames [n]=aType.getTypeValue()+"("+aType.typeToString()+")";
 		}
 		
 		for (int w=0;w<(aHoop.getMaxValues()-2);w++)
 		{
 			cNames [w+2]="V"+w;
 		}
 		
 		DefaultTableModel model=new DefaultTableModel (null,cNames);
 				
 		// For large data sets we will have to use ranges on the index!
 		
 		if (content!=null)
 		{
 			for (HoopKV p : content) 
 			{				
 				String [] rowData=new String [p.getValueSize()+1];
 				
 				rowData [0]=p.getKeyString();
 				
 				//debug ("Adding " + p.getValueSize() + " values");
 				
 				for (int i=0;i<p.getValueSize();i++)
 				{
					rowData [i+1]=p.getValueAsString(i);
 				}
 				
 				model.addRow(rowData);
 			}
 		
 			table.setModel(model);
 		}
 		
 		debug ("showHoop () done");
 	}
 	/**
 	 * 
 	 */	
 	public void setTable(HoopJTable table) 
 	{
 		this.table = table;
 	}	
 	/**
 	 * 
 	 */
 	public void handleCloseEvent ()
 	{
 		debug ("handleCloseEvent ()");
 		
 		// Process this in child class!!
 	}
 	/**
 	 *
 	 */	
 	public void updateContents() 
 	{
 		debug ("updateContents ()");
 		
 		// Implement in child class!!
 	}
 	/**
 	 * 
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event) 
 	{
 		debug ("actionPerformed ()");
 		
 		if (event.getSource()==setRange)
 		{
 			/*
 			sortby=BYRANGE;
 			
 			createRange ();
 			
 			refreshTable ();
 			*/
 			
 			return;
 		}		
 	}	
 	/**
 	 *
 	 */	
 	public void updateSize() 
 	{
 		debug ("updateSize ()");
 		
 		TableColumn col1 = table.getColumnModel().getColumn(0);
 		col1.setPreferredWidth(primColWidth);		
 	 
 		this.setVisible(true);
 		
 		debug ("Total width: " + this.getWidth());
 		
 		TableColumn col2 = table.getColumnModel().getColumn(1);
 		col2.setPreferredWidth(this.getWidth()-primColWidth);		
 	}	
 }
