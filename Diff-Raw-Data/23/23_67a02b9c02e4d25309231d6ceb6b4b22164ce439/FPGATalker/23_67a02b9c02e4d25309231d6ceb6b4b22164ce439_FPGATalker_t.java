 package fr.bretagne.ensta.jog.jarmadeus;
 
 import fr.bretagne.ensta.jog.jarmadeus.hard.HardFPGATalker;
 import fr.bretagne.ensta.jog.jarmadeus.settings.Settings;
import fr.bretagne.ensta.jog.jarmadeus.virtual.VirtualFPGATalker;
 
 public abstract class FPGATalker {
 	
 	public abstract int readChar(char reg);
 	public abstract int writeChar(char reg, char data);
 	public abstract int readShort(char reg);
 	public abstract void setDebugMode(boolean mode);
 	public abstract boolean getDebugMode();
 	
 	public FPGATalker create(){
 		switch(Settings.getSettings().getMode()){
 		case Settings.HARDWARE:
 			return new HardFPGATalker();
		case Settings.VIRTUAL:
			return new VirtualFPGATalker();
 		default:
 			return null;
 		}
 	}
 }
