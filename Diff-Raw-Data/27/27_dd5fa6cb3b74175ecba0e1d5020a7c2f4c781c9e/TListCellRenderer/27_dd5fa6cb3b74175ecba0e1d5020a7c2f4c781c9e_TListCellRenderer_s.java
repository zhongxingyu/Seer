 package com.bluebarracudas.model;
 
 import java.awt.Color;
 import java.awt.Component;
 
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 public class TListCellRenderer extends JLabel implements ListCellRenderer {
 
 	/** Default constructor */
 	public TListCellRenderer() {
 		setOpaque(true);
 	}
 
 	/**
 	 * Returns a component with all of the color and display settings already
 	 * set
 	 */
 	public Component getListCellRendererComponent(JList list, Object value,
 			int index, boolean isSelected, boolean cellHasFocus) {
 		String s_stop = value.toString().split(" ")[0];
 		TStop stop = TFactory.getStop(Integer.parseInt(s_stop));
		TLineData line = stop.getStopData().getLineData();
 		String dispValue = value.toString().replaceAll(s_stop,
 				stop.getStation().getName());
 		setBackground(stop.getStopData().getLineData().getColor());
 		setForeground(Color.white);
 
 		setText(dispValue);
 
 		return this;
 	}
 }
