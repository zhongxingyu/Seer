 
 
 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01741
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  */
 /*
  * Last modification information:
 * $Revision: 1.8 $
 * $Date: 2004-11-23 16:08:36 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.data.ui;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.StringReader;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableCellRenderer;
 
 
 /**
  * DataTablePanel
  * Class name and description
  *
  * Date created: Aug 24, 2004
  *
  * @author imoncada<p>
  *
  */
 public class DataTablePanel extends JPanel
 	implements TableModelListener, ComponentListener, MouseListener, ActionListener
 {
 	protected JTable table;
 	protected DataTableModel tableModel;
 	protected JScrollPane scrollPane;
 	/**
 	 * 
 	 */
 	public DataTablePanel()
 	{
 		super();
 		
 		tableModel = new DataTableModel();
 		table = new JTable(tableModel);
 				
 		scrollPane = new JScrollPane(table);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		add(scrollPane);
 				
 		tableModel.addTableModelListener(this);
 		
 		addComponentListener(this);
 		
 		table.addMouseListener(this);
 	}
 
 	
 	/**
 	 * @return Returns the tableModel.
 	 */
 	public DataTableModel getTableModel()
 	{
 		return tableModel;
 	}
 	
 	/**
 	 * @param tableModel The tableModel to set.
 	 */
 	public void setTableModel(DataTableModel tableModel)
 	{
 		this.tableModel = tableModel;
 	}
 	
 	public void setRenderer(TableCellRenderer cellRenderer)
 	{
 		table.setDefaultRenderer(Object.class, cellRenderer);
 	}
 	
 	/**
 	 * @return Returns the table.
 	 */
 	public JTable getTable()
 	{
 		return table;
 	}
 	/**
 	 * @return Returns the scrollPane.
 	 */
 	public JScrollPane getScrollPane()
 	{
 		return scrollPane;
 	}
 	/**
 	 * @param scrollPane The scrollPane to set.
 	 */
 	public void setScrollPane(JScrollPane scrollPane)
 	{
 		this.scrollPane = scrollPane;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
 	 */
 	public void tableChanged(TableModelEvent e)
 	{
 		//Scroll down automatically
 		table.scrollRectToVisible(table.getCellRect(table.getRowCount()-1, -1, true));	
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
 	 */
 	public void componentHidden(ComponentEvent e)
 	{
 		// Do nothing		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
 	 */
 	public void componentMoved(ComponentEvent e)
 	{
 		// do nothing		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
 	 */
 	public void componentResized(ComponentEvent e)
 	{
 		table.setPreferredScrollableViewportSize(new Dimension(getSize().width - 20,getSize().height - 40));
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
 	 */
 	public void componentShown(ComponentEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
 	 */
 	public void mouseClicked(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
 	 */
 	public void mouseEntered(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
 	 */
 	public void mouseExited(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
 	 */
 	public void mousePressed(MouseEvent e)
 	{
 		
 		if (SwingUtilities.isRightMouseButton(e)){
 			System.out.println("right click");
 			
 			JPopupMenu popup = new JPopupMenu();
 			JMenuItem menuItem = new JMenuItem("Copy");			
 		    menuItem.addActionListener(this);
 		    popup.add(menuItem);
 			
		    menuItem = new JMenuItem("Paste");			
 		    menuItem.addActionListener(this);
 		    popup.add(menuItem);
 		    
 		    popup.show(this, e.getX() + 5, e.getY() + 20);
 			
 		}
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
 	 */
 	public void mouseReleased(MouseEvent e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		String strVal="";
 		
 		if(e.getActionCommand().equals("Copy")) {
 		
 			ByteArrayOutputStream outS = new ByteArrayOutputStream();
 			tableModel.printData(new PrintStream(outS), table.getSelectedRows(), false);
 			strVal = outS.toString();
 			
 			try{
 				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 				
 				clipboard.setContents(new StringSelection(strVal), null);
 			}
 			catch(Exception ex){}
		} else if (e.getActionCommand().equals("Paste")){
 			InputStream inS;
 			try {
 				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 
 				Transferable contents = clipboard.getContents(null);
 				String data = (String)contents.getTransferData(DataFlavor.stringFlavor);
 
 				StringReader dataReader = new StringReader(data);
 				
 				tableModel.loadData(dataReader);
 			} catch(Exception ex){
 				ex.printStackTrace();
 			}			
 		}
 	}
 }
