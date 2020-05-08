 package org.iugonet.www;
 
 import javax.swing.JPanel;
 
abstract public class Iplot extends Aplot{
 
 	Iplot(){
 		
 	}
 	
 	abstract void readData(String arg0);
 
 	@Deprecated
 	abstract void file_http_copy(String start, String end);
 	
 	abstract public JPanel getJPanel();
 
 }
