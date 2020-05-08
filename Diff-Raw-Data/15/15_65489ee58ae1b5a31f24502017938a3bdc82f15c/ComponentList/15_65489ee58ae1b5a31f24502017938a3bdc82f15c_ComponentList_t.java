 /*
  *  ___  ___   _      ___                 _    
  * |   \/ __| /_\    / __|___ _ _  ___ __(_)___
  * | |) \__ \/ _ \  | (_ / -_) ' \/ -_|_-< (_-<
  * |___/|___/_/ \_\  \___\___|_||_\___/__/_/__/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package dsagenesis.core.ui.list;
 
 import java.awt.GridLayout;
 import java.awt.SystemColor;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 
 import dsagenesis.core.ui.Colors;
 
 /**
  * ComponentList
  * 
  * A scrollable list of components.
  * Each list item is a line of the same components.
  */
 public class ComponentList 
 		extends JScrollPane 
 		implements MouseListener 
 {
 
 	// ============================================================================
 	//  Constants
 	// ============================================================================
 
 	private static final long serialVersionUID = 1L;
 	
 	public static final int NO_SELECTION = 0;
 
 	public static final int SINGLE_SELECTION = 1;
 	
 	public static final int MULTI_SELECTION = 2;
 	
 
 	// ============================================================================
 	//  Variables
 	// ============================================================================
 	
 	private JPanel paneList;
 	
 	private int maxVisibleItems;
 	
 	private int selectionType = 0;
 	
 	private Vector<AbstractComponentListItem> items;
 	
 	private int selectedItems = 0;
 	
 	
 	// ============================================================================
 	//  Constructors
 	// ============================================================================
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param visibleItems
 	 * @param selectiontype
 	 */
 	public ComponentList(
 			int visibleItems,
 			int selectiontype
 		)
 	{
 		super();
 		
 		this.items = new Vector<AbstractComponentListItem>(); 
 		this.maxVisibleItems = visibleItems;
 		this.selectionType = selectiontype;
 		
 		this.paneList = new JPanel();
 		GridLayout gl = new GridLayout(maxVisibleItems,1);
 		gl.setVgap(-1);
 		this.paneList.setLayout(gl);
 		
 		this.setVerticalScrollBarPolicy(
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
 			);
 		this.setViewportView(this.paneList);
 	}
 
 	
 	// ============================================================================
 	//  Functions
 	// ============================================================================
 
 	/**
 	 * recolorItems
 	 * 
 	 * @param index if -1 all items are recolored
 	 */
 	private void recolorItems(int index)
 	{
 		int start = 0;
 		int end = items.size();
 		
 		if( index > -1 )
 		{
 			start = index;
 			end = index +1;
 		}
 		
 		for( int i=start; i< end; i++ )
 		{
 			AbstractComponentListItem item = 
 					(AbstractComponentListItem)items.get(i);
 			
 			if( item.isHovered() )
 			{
 				item.setBackground(
 						Colors.colorHoverLine
 					);
 				item.setTextColor(SystemColor.textText);
 				
 			} else if( item.isSelected() ){
 				item.setBackground(
 						Colors.colorTableRowActive
 					);
 				item.setTextColor(SystemColor.textHighlightText);
 				
 			} else if( i%2 == 0 ) {
 				item.setBackground(
 						Colors.colorTableRowInactive1
 					);
 				item.setTextColor(SystemColor.textText);
 				
 			} else {
 				item.setBackground(
 						Colors.colorTableRowInactive2
 					);
 				item.setTextColor(SystemColor.textText);
 			}
 		}
 	}
 	
 	/**
 	 * updateItemLayout
 	 */
 	private void updateItemLayout()
 	{
 		int gridrows = maxVisibleItems;
 	
 		if( items.size() > maxVisibleItems )
 			gridrows = items.size();
 		
 		GridLayout gl = (GridLayout)paneList.getLayout();
 		gl.setRows(gridrows);
		paneList.setLayout(gl);
 		
 		recolorItems(-1);
 		this.validate();
 	}
 	
 	/**
 	 * getItems
 	 * 
 	 * @return
 	 */
 	public Vector<AbstractComponentListItem> getItems()
 	{
 		return this.items;
 	}
 	
 	/**
 	 * addItem
 	 */
 	public void addItem(AbstractComponentListItem item)
 	{
 		paneList.add(item);
 		items.add(item);
 		updateItemLayout();
 	}
 
 	/**
 	 * actionRemoveArgument
 	 */
 	public void removeItem(AbstractComponentListItem item)
 	{
 		paneList.remove(item);
 		items.remove(item);
		updateItemLayout();
 	}
 	
 	/**
 	 * removeAllSelectedItems
 	 */
 	public void removeAllSelectedItems()
 	{
		int start = items.size()-1;
 		
		for( int i = start; i > -1; i-- )
 		{
 			if( items.get(i).isSelected() )
 			{	
 				AbstractComponentListItem item = items.remove(i);
				item.setVisible(false);
 				paneList.remove(item);
				item = null;
 			}
 		}
		updateItemLayout();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent me) 
 	{
 		// not used
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent me)
 	{
 		AbstractComponentListItem item = (AbstractComponentListItem)me.getComponent();
 		item.setHovered(true);
 		recolorItems(items.indexOf(item));
 	}
 
 	@Override
 	public void mouseExited(MouseEvent me) 
 	{
 		AbstractComponentListItem item = (AbstractComponentListItem)me.getComponent();
 		item.setHovered(false);
 		recolorItems(items.indexOf(item));
 	}
 
 	@Override
 	public void mousePressed(MouseEvent me) 
 	{
 		if( selectionType == NO_SELECTION )
 			return;
 		
 		AbstractComponentListItem item = (AbstractComponentListItem)me.getComponent();
 		item.setHovered(false);
 		
 		boolean isSelected = item.isSelected();
 		
 		if( selectionType == SINGLE_SELECTION )
 		{
 			if( isSelected )
 			{
 				item.setSelected( (!isSelected) ); 
 				selectedItems--;
 				
 			} else if( selectedItems == 0 ) {
 				item.setSelected( (!isSelected) ); 
 				selectedItems++;
 			}
 			
 		} else {
 			item.setSelected( (!isSelected) ); 
 		}
 		recolorItems(items.indexOf(item));
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent me) 
 	{
 		AbstractComponentListItem item = (AbstractComponentListItem)me.getComponent();
 		item.setHovered(true);
 		recolorItems(items.indexOf(item));
 	}
 	
 }
