 package net.tuschhcm.routercontrol.ui;
 
 import java.util.Scanner;
 
 public class ConsoleUI implements UserInterface {
 
 	private Action mPresetSelectedAction;
 	
 	private Action mLockSetAction;
 	
 	private boolean mLocked;
 	
 	private int mLastPreset;
 	
     @Override
     public void addPreset(int number, final String name) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void setPresetSelectionAction(final Action action) {
     	mPresetSelectedAction = action;
     }
 
     @Override
     public int getSelectedPreset() {
         return mLastPreset;
     }
 
     @Override
     public void run() {
         
     	Scanner keyboard = new Scanner(System.in);
     	String userInput;
     	boolean running = true;
     	
    	System.out.println("/n/nWelcome to Router Control!");
         System.out.println("--------------------------");
         System.out.println("Available commands: \nLock: Locks/unlocks the physical controls " +
         		"on the router\nExit: Exits the application");
         
         do{
         
         	System.out.print("\nEnter a command or preset number: ");
         	userInput = keyboard.next();
         
         
         	if(userInput.equalsIgnoreCase("exit")){
             	running = false;
            
             } else if(userInput.equalsIgnoreCase("lock")){
             	mLocked = !mLocked;
             	mLockSetAction.onAction();
             	if(mLocked){
             		System.out.println("Physical Controls are Locked.");
             	} else {
             		System.out.println("Physical Controls are Unlocked");
             	}
             
             } else {
             	try{
             		mLastPreset = Integer.valueOf(userInput);
             		mPresetSelectedAction.onAction();
             	} catch (final NumberFormatException nfe){
             		System.out.println("ERROR: Invalid Entry");
             	}
             }
         }while(running);
         
        keyboard.close();
     }
 
     @Override
     public void setToggleControlLockAction(final Action action) {
         mLockSetAction = action;
     }
 
     @Override
     public boolean getControlLockStatus() {
         return mLocked;
     }
 
     @Override
     public void setControlLockStatus(boolean enabled) {
         mLocked = enabled;
     }
 }
