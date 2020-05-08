 /*
  * This file is part of Synth.
  *
  * Synth is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Synth is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Synth.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.mangrajalkin.synth;
 public class Synth{
 	
 	protected native int synthStartup();
 	protected native int synthShutdown();
 	protected native void synthNoteOn(int midiNumber);
 	protected native void synthNoteOff(int midiNumber);
 	
 	static {System.loadLibrary("synth");}
 	
	public static void main(){
		main(new String[0]);
	}

 	public static void main(String[] args){
 		Synth synth = new Synth();
 		UniversalListener listener = new UniversalListener(synth);
		//javax.swing.SwingUtilities.invokeLater(
		Thread t = new Thread(
 			new GUI(listener, 48, 24));
		t.start();
		try{
			t.join();
		} catch (Exception e){}
 	}
 	
 	protected Synth(){
 		if (synthStartup() != 0){
 			System.out.println("Could not start native synth");
 			System.exit(0);
 		}
 	}
 	
 	protected void noteOn(int midiNumber){
 		synthNoteOn(midiNumber);
 	}
 	
 	protected void noteOff(int midiNumber){
 		synthNoteOff(midiNumber);
 	}
 	
 	protected void shutdown(){
 		if (synthShutdown() != 0)
 			System.out.println("Could not shut down native synth");
 	}
 }
