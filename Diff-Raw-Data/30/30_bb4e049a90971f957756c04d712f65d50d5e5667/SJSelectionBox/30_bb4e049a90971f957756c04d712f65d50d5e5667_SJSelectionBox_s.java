 /**
  *
  * This file is part of the SJWidget library.
  * (c) 2005-2012 Bjorn Roche
  * Development of this library has been supported by Indaba Media (http://www.indabamusic.com)
  * and XO Audio (http://www.xoaudio.com)
  *
  * for copyright and sharing permissions, please see the COPYING.txt file which you should
  * have recieved with this file.
  *
  */
 
 package com.xowave.sjwidget;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ListModel;
 import javax.swing.event.EventListenerList;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 
 import com.xowave.util.StringShortener;
 
 /**
  * Like a simple uneditable combo box.
  * 
  * @author bjorn
  *
  */
 public class SJSelectionBox extends SJButton implements SJWidget, ListDataListener {
 	private final String labelID;
 	private final SJSelectionBoxPopup popupMenu;
 	private ListModel model;
 	private int selectedIndex = 0;
 	private final EventListenerList ell = new EventListenerList();
 	private boolean reduceTextToIconSize = false;
 	private int additionalAmount;
 	private String realText;
 
 	public SJSelectionBox(String mainID, String popupID, String labelID, Object[] items ) {
 		this( mainID, popupID, labelID, createListModel(items) );
 	}
 	
 	private static DefaultListModel createListModel(Object[] items) {
 		DefaultListModel model = new DefaultListModel();
 		for( Object item : items )
 			((DefaultListModel)model).addElement(item);
 		return model;
 	}
 
 	public SJSelectionBox(String mainID, String popupID, String labelID ) {
 		this( mainID, popupID, labelID, new DefaultListModel() );
 	}
 	
 	public SJSelectionBox(String mainID, String popupID, String labelID, ListModel model ) {
 		super(mainID);
 		popupMenu = new SJSelectionBoxPopup(popupID) {
 			@Override
 			public void setVisible( boolean vis ) {
 				super.setVisible(vis);
 				SJSelectionBox.this.setSelected( vis );
 			}
 		} ;
 
 		this.model = model;
 		this.labelID = labelID;
 		model.addListDataListener(this);
 		
 		super.addActionListener( new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
				popupMenu.show( SJSelectionBox.this, SJSelectionBox.this, 0, getHeight() );
 			}
 		});
 
 		setupSelectionBox();
 	}
 	
 	@Override
 	public void setFocusable( boolean focusable ) {
 		super.setFocusable( focusable );
 		if( popupMenu != null )
 			popupMenu.setFocusable( focusable );
 	}
 	
 	public void setReduceTextToIconSize( boolean reduce, int additionalAmount ) {
 		this.reduceTextToIconSize = reduce;
 		this.additionalAmount = additionalAmount;
 		setText(realText);
 	}
 	/**
 	 * returns the list model associated with this selection box. Unless a different model
 	 * is given to this class, a DefaultListModel is used.
 	 */
 	public ListModel getListModel() {
 		return model;
 	}
 
 	@Override
 	public void addActionListener( ActionListener al ) {
 		ell.add(ActionListener.class, al);
 	}
 	
 	@Override
 	public void removeActionListener( ActionListener al ) {
 		ell.remove(ActionListener.class, al);
 	}
 	
 	@Override
 	public void addItemListener( ItemListener il ) {
 		ell.add(ItemListener.class, il);
 	}
 	
 	@Override
 	public void removeItemListener( ItemListener il ) {
 		ell.remove(ItemListener.class, il);
 	}
 	
 	@Override
 	public void setText( String text ) {
 		realText = text;
 		if( reduceTextToIconSize ) {
 			text = StringShortener.getShortendStringThatFits(text, getIcon().getIconWidth()-additionalAmount, getFontMetrics(getFont()));
 		}
 		super.setText(text);
 	}
 	
 	private void setupSelectionBox() {
 		popupMenu.removeAll();
 		if( selectedIndex < model.getSize() && selectedIndex >= 0 ) {
 			if( model.getElementAt(selectedIndex) instanceof Icon ) {
 				setIcon( (Icon) model.getElementAt(selectedIndex) );
 			} else {
 				setText( model.getElementAt(selectedIndex).toString() );
 			}
 		}
 
 		final SJMenuItem jmi[] = new SJMenuItem[ model.getSize() ];
 
 		for( int i=0; i<jmi.length; ++i ) {
 			jmi[i] = new SJMenuItem( model.getElementAt(i).toString(), labelID );
 			final int idx = i;
 			if( i == getSelectedIndex() )
 				jmi[i].setSelected(true);
 			jmi[idx].addActionListener( new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setSelectedIndex(idx);
 					fireActionEvent();
 				}
 			} );
 		}
 		for( int i=0; i<jmi.length; ++i )
 			popupMenu.add( jmi[i] );
 		popupMenu.validate();
 		popupMenu.repaint();
 	}
 	public int getItemCount() {
 		return model.getSize();
 	}
 	public void setSelectedIndex( int i ) {
 		int old = selectedIndex;
 		selectedIndex = i;
 		if( old >= 0 && old < model.getSize() )
 			fireDeselectedEvent( old );
 		fireSelectedEvent( selectedIndex );
 		setupSelectionBox();
 	}
 	
 	public void setSelectedItem( Object obj ) {
 		for( int i=0; i<model.getSize(); ++i ) {
 			if( model.getElementAt(i).equals(obj) ) {
 				setSelectedIndex(i);
 				return;
 			}
 		}
 	}
 	
 	public int getSelectedIndex() {
 		return selectedIndex;
 	}
 	
 	public Object getSelectedItem() {
 		if( selectedIndex >= 0 && selectedIndex < model.getSize() )
 			return model.getElementAt(selectedIndex);
 		else return null;
 	}
 	
 	protected void fireActionEvent() {
 		ActionEvent ae = new ActionEvent(this, 0, model.getElementAt(selectedIndex).toString());
 		for( ActionListener al : ell.getListeners(ActionListener.class) )
 			al.actionPerformed(ae);
 	}
 	
 	protected void fireDeselectedEvent(int index) {
 		ItemEvent ie = new ItemEvent(this, 0, model.getElementAt(index), ItemEvent.DESELECTED);
 		for( ItemListener il : ell.getListeners(ItemListener.class) )
 			il.itemStateChanged(ie);
 	}
 	
 	protected void fireSelectedEvent(int index) {
 		ItemEvent ie = new ItemEvent(this, 0, model.getElementAt(index), ItemEvent.SELECTED);
 		for( ItemListener il : ell.getListeners(ItemListener.class) )
 			il.itemStateChanged(ie);
 	}
 	
 	@Override
 	public String getWidgetID() {
 		return (String) this.getClientProperty(ID_KEY);
 	}
 	
 	@Override
 	public void setBackground( Color bg ) {
 		setOpaque( bg.getAlpha() == 255 );
 		super.setBackground(bg);
 	}
 	
 	@Override
 	public String getWidgetClass() {
 		return (String) this.getClientProperty(CLASS_KEY);
 	}
 
 	@Override
 	public SJSelectionBox setWidgetID(String ID) {
 		WidgetUtil.registerAndSetup(this, ID);
 		return this;
 	}
 	public Object getItemAt(int i) {
 		return model.getElementAt(i);
 	}
 	@Override
 	public Object[] getSelectedObjects() {
 		return new Object[] { model.getElementAt(selectedIndex) };
 	}
 
 	public void contentsChanged(ListDataEvent e) {
 		setupSelectionBox();
 	}
 
 	public void intervalAdded(ListDataEvent e) {
 		setupSelectionBox();
 	}
 
 	public void intervalRemoved(ListDataEvent e) {
 		setupSelectionBox();
 	}
 
 	public void selectPrevious() {
 		if( getSelectedIndex() <= 0 )
 			return;
 		setSelectedIndex( getSelectedIndex() - 1 );
 	}
 	public void selectNext() {
 		if( getSelectedIndex() + 1 >= getItemCount() )
 			return;
 		setSelectedIndex( getSelectedIndex() + 1 );
 	}
 }
