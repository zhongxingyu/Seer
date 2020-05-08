 package com.fermata.ui;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import java.util.*;
 
 @SuppressWarnings("serial")
 public abstract class PianoKey extends JButton {
 	
 	// maintain pressed state of key
 	protected boolean down;
 	public boolean isDown() { return down; }
 	
 	// the midi value
 	private int value;
 	public int getValue() { return value; }
 	
 	// the note name
 	private String name;
 	public String getName() { return name; }
 	
 	// the octave
 	private int octave;
 	public int getOctave() { return octave; }
 	
 	// the piano this key is on
 	private Piano piano;
 	
 	public PianoKey(int v, Piano p)
 	{
 		value = v;
 		name = KeyNames[v % 12];
 		octave = (v / 12) - 1;
 		piano = p;
 		
 		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 				
 		this.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				if (down) piano.PianoKeyUp(value);
 				else piano.PianoKeyDown(value);
 			}
 		});
 	}
 	
 	public void pushKey()
 	{
 		down = !down;
 		updateBackground();
 	}
 	
 	public abstract void updateBackground();
 	
 	// create a piano key from a numeric value
 	public static PianoKey CreateKey(int v, Piano p)
 	{
 		switch (v % 12)
 		{
 		case 1:
 		case 3:
 		case 6:
 		case 8:
 		case 10:
 			return new BlackKey(v, p);
 		default:
 			return new WhiteKey(v, p);
 		}
 	}
 
	public static String[] KeyNames = { "C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B" }; 
 }
 
