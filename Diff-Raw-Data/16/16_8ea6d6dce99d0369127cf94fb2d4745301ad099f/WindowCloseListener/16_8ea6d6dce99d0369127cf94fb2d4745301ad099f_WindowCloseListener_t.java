 package edu.brown.cs32.atian.crassus.gui;
 
import edu.brown.cs32.atian.crassus.backend.StockEvent;

 public interface WindowCloseListener {
 
	public void windowClosedWithEvent(StockEvent e);
 	
 	public void windowClosedWithCancel();
 	
 }
