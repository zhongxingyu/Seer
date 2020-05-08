 package com.fermata.ui;
 
 import java.awt.Color;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLayeredPane;
 
 import com.fermata.music.midi.MidiHelpers;
 
 @SuppressWarnings("serial")
 public class Piano extends JLayeredPane {
 
 	List<PianoListener> listeners = new ArrayList<PianoListener>();
 	
 	public void addListener(PianoListener toAdd) {
         listeners.add(toAdd);
     }
 	
 	// the keys
 	private PianoKey[] keys;
 	public PianoKey[] getKeys() { return keys; }
 	
 	private int highKey;
 	private int lowKey;
 	
 	// the number of white keys (tracked as convenience for resizing)
 	private int numWhite;
 	public int getNumWhite() { return numWhite; }
 	
 	public Piano()
 	{
 		this(21, 108);
 	}
 	
 	public Piano(int lKey, int hKey)
 	{
 		super();
 		
 		highKey = hKey;
 		lowKey = lKey;
 		
 		keys = new PianoKey[highKey - lowKey + 1];
 		
 		// for every key
 		for (int i = lowKey; i <= highKey; i++)
 		{
 			// create the key
 			PianoKey k = PianoKey.CreateKey(i, this);
 			
 			// store it
 			keys[i - lowKey] = k;
 
 			// add it to the pane
 			// if it is a black key add it on the second layer
 			add(k, (k instanceof BlackKey) ? 51 : 50, 0);
 			
 			// track number of white keys
 			if (k instanceof WhiteKey) numWhite++;
 		}
 		
 		this.addComponentListener(new PianoResizeListener());
 	}
 	
 	private void pushKey(int midiValue)
 	{
 		keys[midiValue - lowKey].pushKey();
 	}
 	
 	private boolean checkBounds(int midiValue) {
 		return midiValue >= lowKey && midiValue <= highKey;
 	}
 
 	public void PianoKeyDown(int midiValue) {
 		if (!checkBounds(midiValue)) return;
		if (keys[midiValue - lowKey].isDown()) return;
 		pushKey(midiValue);
 		for (PianoListener pl : listeners) pl.PianoKeyDown(midiValue);
 	}
 
 	public void PianoKeyUp(int midiValue) {
 		if (!checkBounds(midiValue)) return;
		if (!keys[midiValue - lowKey].isDown()) return;
 		pushKey(midiValue);
 		for (PianoListener pl : listeners) pl.PianoKeyUp(midiValue);
 	}
 	
 }
