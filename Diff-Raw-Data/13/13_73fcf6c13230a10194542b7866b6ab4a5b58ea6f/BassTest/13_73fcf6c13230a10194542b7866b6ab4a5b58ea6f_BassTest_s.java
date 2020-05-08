 /* BassTest.java - An example player for the Bass device
  * Copyright (C) 2000 Fredrik Ehnbom
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 import silence.*;
 import silence.devices.*;
 import silence.devices.bass.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * An example player for the Bass device
  * @author Fredrik Ehnbom
 * @version $Id: BassTest.java,v 1.1 2000/06/10 18:04:39 quarn Exp $
  */
 public class BassTest extends Frame {
 
 	static {
 		// load the library for bass
 		System.loadLibrary("bassglue");
 	}
 
 	/**
 	 * The AudioDevice to use for playing the AudioFormat 
 	 */
 	private AudioDevice audioDevice = new BassDevice() {
 		// This is how you redefine the sync method.
 		public void sync(int eff) {
 			System.out.println("Bass wants to sync... (effect num: " + eff + ")");
 		}
 	};
 
         private Scrollbar vol = new Scrollbar(Scrollbar.HORIZONTAL, 128, 4, 0, 100);
 	private String file = null;
 
 	/**
 	 * Creates a new Silence player.
 	 * @param file The file to play
 	 */
 	public BassTest(String file) {
                 super("BASS test: " + file);
 		this.file = file;
 		setLayout(new BorderLayout());
 
 		vol.addAdjustmentListener(volListener);
 		add("North", vol);
 
 		try {
 			audioDevice.init(true);
 		} catch (AudioException me) {
 			me.printStackTrace();
 			audioDevice.close();
 			System.exit(1);
 		}
 
 		Button b = new Button("Play");
 		b.addActionListener(listener);
 		add("West", b);
 
 		b = new Button("Stop");
 		b.addActionListener(listener);
 		add("East", b);
 
 		b = new Button("Pause");
 		b.addActionListener(listener);
 		add("South", b);
 
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				audioDevice.stop();
 				audioDevice.close();
 				dispose();
 				System.exit(0);
 			}
 		});
 		setSize(320, 240);
 		show();
 	}
 
 	AdjustmentListener volListener = new AdjustmentListener() {
 		public void adjustmentValueChanged(AdjustmentEvent e) {
 			audioDevice.setVolume(vol.getValue());
 		}
 	};
 
 	ActionListener listener = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			String event = ((Button) e.getSource()).getLabel();
 
 			if (event.equals("Play")) {
 				try {
 					audioDevice.play(file, false);
 					audioDevice.setVolume(vol.getValue());
 				} catch (AudioException ae) {
 					ae.printStackTrace();
 				}
 			} else if (event.equals("Stop")) {
 				audioDevice.stop();
                         } else if (event.equals("Pause")) {
                                 try {
                                         audioDevice.pause();
                                 } catch (AudioException fe) {
                                         fe.printStackTrace();
                                 }
                         }
 		}
 	};
 
 	public static void main(String args[]) {
 		if (args.length != 1) {
 			System.out.println("Usage: java BassTest <song>");
 		} else {
			new FmodTest(args[0]);
 		}
 	}
 }
 /*
  * ChangeLog:
  * $Log: BassTest.java,v $
 * Revision 1.1  2000/06/10 18:04:39  quarn
 * A test for Bass
  *
  */
