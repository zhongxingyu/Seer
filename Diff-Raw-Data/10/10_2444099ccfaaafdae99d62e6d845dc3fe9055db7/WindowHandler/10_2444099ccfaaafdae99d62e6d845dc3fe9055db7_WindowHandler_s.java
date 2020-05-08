 package com.jpii.navalbattle.data;
 
 import java.util.*;
 import javax.swing.*;
 import com.jpii.navalbattle.gui.*;
 
 public class WindowHandler {
 	
 	private ArrayList<Window> windows;
 	private int current;
 	
 	public WindowHandler(){
 		current = 0;
 		windows = new ArrayList<Window>();
 		initArray();
 		windows.get(current).setVisible(true);
		System.out.println("Showing");
 	}
 	
 	private void initArray(){
 		windows.add(new LoginWindow());
 		windows.add(new LoggingInWindow());
 		windows.add(new LoginOptionsWindow());
 		windows.add(new MainMenuWindow());
 		windows.add(new SPOptions());
 		windows.add(new HelpWindow());
 		windows.add(new CreditsWindow());
 	}
 	
 	public Window getCurrentClass(){
 		if(windows.get(current)!=null)
 			return windows.get(current);
 		return null;
 	}
 	
 	public void setNewWindow(String a){
 		for(int index = 0; index<windows.size(); index++){
			JFrame temp = (Window) windows.get(current);
			if(a.equals( temp.getClass().toString().substring((getClass().toString().lastIndexOf(".")+1)) )){
 				temp.setVisible(true);
				System.out.println("Worked");
 			}
 			else{
 				temp.setVisible(false);
				System.out.println("hiding");
 			}
 		}
 	}
 }
