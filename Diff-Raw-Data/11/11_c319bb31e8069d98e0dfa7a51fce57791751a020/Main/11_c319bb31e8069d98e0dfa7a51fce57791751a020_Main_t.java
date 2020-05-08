 /*
 Mjdj MIDI Morph - an extensible MIDI processor and translator.
 Copyright (C) 2010 Confusionists, LLC (www.confusionists.com)
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 
 See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>. 
 
 You may contact the author at mjdj_midi_morph [at] confusionists.com
 */
 package com.confusionists.mjdj;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.util.*;
 import java.util.Timer;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 
 import org.codehaus.groovy.runtime.StackTraceUtils;
 
 import com.confusionists.mjdj.fileIO.MorphLoaderJava;
 import com.confusionists.mjdj.midi.ServiceImpl;
 import com.confusionists.mjdj.settings.MorphAdaptor;
 import com.confusionists.mjdj.settings.Settings;
 import com.confusionists.mjdj.ui.*;
 import com.confusionists.mjdjApi.midiDevice.DeviceWrapper;
 import com.confusionists.mjdjApi.morph.DeviceNotFoundException;
 import com.confusionists.mjdjApi.morph.Morph;
 import com.confusionists.mjdjApi.util.MjdjService;
 import com.confusionists.swing.JFrameRedux;
 import com.confusionists.swing.SwingOps;
 
 @SuppressWarnings("serial")
 public class Main extends JFrameRedux {
 
 	public static final String PRODUCT_NAME = "Mjdj MIDI Morph";
	public static final String PRODUCT_VERSION = "Beta 0.1.04";
 	public MorphCheckboxList morphCheckboxList;
 	JTextArea outputArea;
 	MidiDeviceCheckboxList inputList;
 	MidiDeviceCheckboxList outputList;
 	JMenu jMenuTools = new JMenu();
 	JToolBar toolBar = new JToolBar();
 	ClockSourceCombo clockSourceCombo = new ClockSourceCombo();
 	public JToggleButton debugToggle;
 	private Timer logTimer = new Timer();
 	private StringBuffer toLog = new StringBuffer();
 	private boolean inited = false;
 
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 			com.confusionists.mjdj.Main frame = Universe.instance.main = new Main();
 			frame.setSize(900, 600);
 			frame.setTitle(PRODUCT_NAME + " " + PRODUCT_VERSION);
 			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			frame.setup();
 			frame.setVisible(true);
 		} catch (Throwable e) {
 			e = StackTraceUtils.deepSanitize(e);
 			JOptionPane.showMessageDialog(null, "Mjdj cannot start, sorry\n(" + e + ")", "Mjdj", JOptionPane.OK_OPTION);
 			System.out.println("There has been an error, exiting.");
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 
 
 	public void logInner(String text, boolean linefeed) {
 		String line;
 		if (linefeed) {
 			line = text + "\n";
 		} else {
 			line = text;
 		}
 		toLog.append(line);
 	}
 
 	public void logTimerRun() {
 		if (toLog.length() == 0 || !inited)
 			return;
 		String text = toLog.toString();
 		toLog.delete(0, text.length());
 		outputArea.setText(outputArea.getText() + text);
 		outputArea.setCaretPosition(outputArea.getText().length());
 		System.out.println(text);
 	}
 
 	/** File | Exit action performed */
 	@Override
 	public void jMenuFileExit_actionPerformed(ActionEvent e) {
 		super.jMenuFileExit_actionPerformed(e);
 		onClose();
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		super.windowClosing(e);
 		onClose();
 	}
 
 	boolean alreadyClosed = false;
 
 	public void onClose() {
 		if (alreadyClosed)
 			return;
 		try {
 			alreadyClosed = true;
 			for (MorphAdaptor morph : morphCheckboxList.getMorphs()) {
 				morph.getMorph().shutdown();
 				morph.saveMorphSettings();
 			}
			for (DeviceWrapper device : Universe.instance.midiDriverManager.inputTransmitters) {
				System.out.println("closing " + device.getName());
				device.close();
			}
			for (DeviceWrapper device : Universe.instance.midiDriverManager.outputReceivers) {
				System.out.println("closing " + device.getName());
				device.close();
			}
 			Settings.getInstance().save();
 			logTimer.cancel();
 		} catch (IOException e1) {
 			System.err.println("Error saving settings " + e1.getMessage());
 		}
 	}
 
 	public void setup() throws Exception {
 		OSXAdapter.setQuitHandler(this, getClass().getDeclaredMethod("onClose", (Class[]) null));
 
 		logTimer.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
 				Universe.instance.main.logTimerRun();
 
 			}
 		}, 0, 100);
 
 		toolBar.add(Universe.instance.syncButton);
 		toolBar.add(Box.createHorizontalStrut(5));
 
 		JLabel label;
 		label = new JLabel("Clock Source");
 		toolBar.add(label);
 		this.getContentPane().add(toolBar, BorderLayout.NORTH);
 		toolBar.add(clockSourceCombo);
 
 		toolBar.add(Box.createHorizontalGlue());
 		toolBar.add(Box.createHorizontalGlue());
 
 		JButton button;
 		button = new JButton("Move Morph Up");
 		toolBar.add(button);
 		button.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				MorphCheckboxList.instance.moveSelectedMorph(true);
 			}
 		});
 		
 		button = new JButton("Move Morph Down");
 		toolBar.add(button);
 		button.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				MorphCheckboxList.instance.moveSelectedMorph(false);
 			}
 		});
 		
 		
 		toolBar.add(Box.createHorizontalGlue());
 		toolBar.add(Box.createHorizontalGlue());
 		
 		button = new JButton("Reload Morphs");
 		button.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				rescanMorphs();
 			}
 		});
 		toolBar.add(button);
 
 		button = new JButton("Clear Output");
 		button.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				outputArea.setText("");
 				outputArea.setCaretPosition(0);
 			}
 		});
 		toolBar.add(button);
 
 		debugToggle = new JToggleButton("Debug");
 		toolBar.add(debugToggle);
 
 		outputArea = new JTextArea();
 		JScrollPane scroller = new JScrollPane();
 		scroller.getViewport().add(outputArea);
 		SwingOps.setAllSizes(scroller, new Dimension(100, 200));
 		getContentPane().add(scroller, BorderLayout.SOUTH);
 
 		jMenuTools.setText("Tools");
 
 		JMenuItem menuItem;
 		menuItem = new JMenuItem();
 		menuItem.setText("Rescan MIDI (Broken)");
 		menuItem.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				rescanMidi();
 			}
 		});
 		jMenuTools.add(menuItem);
 
 		menuItem = new JMenuItem();
 		menuItem.setText("Rescan Morphs");
 		menuItem.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				rescanMorphs();
 			}
 		});
 		jMenuTools.add(menuItem);
 
 		menuItem = new JMenuItem();
 		menuItem.setText("Compile Java Morphs (and reload)");
 		menuItem.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				rescanMorphs();
 			}
 		});
 		jMenuTools.add(menuItem);
 
 		menuItem = new JMenuItem();
 		menuItem.setText("Diagnostics");
 		menuItem.addActionListener(new java.awt.event.ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				runDiagnostics();
 			}
 		});
 		jMenuTools.add(menuItem);
 
 		jMenuBar1.add(jMenuTools);
 
 		JMenu menuHelp = new JMenu("Help");
 		jMenuBar1.add(menuHelp);
 
 		boolean aboutMac = OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("onAboutClick", (Class[]) null));
 		if (aboutMac) {
 			// we need to suppress the File menu FileMenu
 			jMenuBar1.remove(jMenuFile);
 		} else {
 			JMenuItem menuAbout = new JMenuItem("About Mjdj MIDI Morph");
 			menuAbout.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent e) {
 					Main.this.onAboutClick();
 				}
 			});
 			menuHelp.add(menuAbout);
 		}
 
 		inputList = new MidiDeviceCheckboxList("Input MIDI devices");
 		scroller = getTitledScroller(inputList, inputList.getName());
 		scroller.setMinimumSize(new Dimension(250, 50));
 		scroller.setPreferredSize(new Dimension(250, 100));
 		getContentPane().add(scroller, BorderLayout.WEST);
 
 		outputList = new MidiDeviceCheckboxList("Ouput MIDI devices");
 		scroller = getTitledScroller(outputList, outputList.getName());
 		scroller.setMinimumSize(new Dimension(250, 50));
 		scroller.setPreferredSize(new Dimension(250, 100));
 		getContentPane().add(scroller, BorderLayout.EAST);
 
 		morphCheckboxList = new MorphCheckboxList();
 		scroller = getTitledScroller(morphCheckboxList, "MIDI Morphs (right-click to open)");
 		getContentPane().add(scroller, BorderLayout.CENTER);
 
 		// first scan
 		rescanMidi();
 		rescanMorphs();
 
 		Logger.log(this.getTitle() + " starting, JVM: " + System.getProperty("java.version")
 				+ ".\nNote: this log is asynchronous (NOT in realtime) so using debug should not affect performance (much).");
 		
 		inited = true;
 	}
 
 	public void onAboutClick() {
 		AboutBox dlg = new AboutBox(this);
 		Dimension dlgSize = dlg.getPreferredSize();
 		Dimension frmSize = getSize();
 		Point loc = getLocation();
 		dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
 		dlg.setModal(true);
 		dlg.pack();
 		dlg.setSize(new Dimension(400, 250));
 		dlg.setVisible(true);
 		dlg.setResizable(false);
 
 	}
 
 	private static JScrollPane getTitledScroller(JComponent wrapThis, String title) {
 		JScrollPane scroller = new JScrollPane();
 		scroller.getViewport().add(wrapThis);
 		scroller.setBorder(new TitledBorder(title));
 		return scroller;
 	}
 
 	protected void runDiagnostics() {
 		System.gc();
 		// get all translators
 		Logger.log("Found " + morphCheckboxList.getMorphs().size() + " Morphers");
 		List<MorphAdaptor> list = morphCheckboxList.getMorphs();
 		for (MorphAdaptor morph : list) {
 			Logger.log("Found " + morph.getMorph().getName() + " and is active? " + morph.isActive());
 			String text = morph.getMorph().diagnose();
 			if (text != null)
 				Logger.log(text);
 		}
 
 		Logger.log("");
 
 		Logger.log("Found " + inputList.getWrappers().size() + " Input Transmitters");
 		List<? extends DeviceWrapper> list2 = inputList.getWrappers();
 		for (DeviceWrapper wrapper : list2) {
 			Logger.log("'" + wrapper + "'" + " - active=" + wrapper.isActive());
 		}
 
 		Logger.log("");
 
 		Logger.log("Found " + outputList.getWrappers().size() + " Input Transmitters");
 		List<? extends DeviceWrapper> list3 = outputList.getWrappers();
 		for (DeviceWrapper wrapper : list3) {
 			Logger.log("'" + wrapper + "'" + " - active=" + wrapper.isActive());
 		}
 
 		Universe.instance.clockHandler.diagnose();
 
 	}
 
 	public void rescanMidi() {
 		Universe.instance.midiDriverManager.close();
 
 		Universe.instance.midiDriverManager.init();
 
 		inputList.setList(Universe.instance.midiDriverManager.inputTransmitters);
 		clockSourceCombo.setList(Universe.instance.midiDriverManager.inputTransmitters);
 		outputList.setListReceivers(Universe.instance.midiDriverManager.outputReceivers);
 	}
 
 	public void rescanMorphs() {
 		morphCheckboxList.setListData(new Vector<MorphCheckbox>());
 		MjdjService serviceImpl = new ServiceImpl();
 
 		try {
 
 			List<Morph> morphs = new ArrayList<Morph>();
 			MorphLoaderJava loader = new MorphLoaderJava();
 			loader.load(morphs);
 
 			for (Morph morph : morphs) {
 				String morphName = "Unknown";
 				try {
 					// plug it into the adaptor that's been loaded, or get a new one
 					MorphAdaptor morphAdaptor = Settings.getInstance().getMorphAdaptor(morph);
 					
 					morphAdaptor.setDead(true); // how we keep track of whether it ever
 											// made it...
 					morphName = morph.getName();
 					if (morphName != null) {
 						morph.setService(serviceImpl);
 						morph.setInDeviceNames(inputList.getNames());
 						morph.setOutDeviceNames(outputList.getNames());
 						morph.init();
 						morphAdaptor.restore();
 						morphAdaptor.setDead(false);
 					} else {
 						Logger.log("Morph class " + morph.getClass().getName() + " has no name set!");
 					}
 				} catch (DeviceNotFoundException e) {
 					Logger.log("Morph '" + morphName + "' not loaded, requires device '" + e.deviceName + "'");
 				} catch (Exception e2) {
 					Logger.log("Unknown problem loading morph '" + morphName + "', see stack trace");
 					e2.printStackTrace();
 				}
 
 			}
 
 			morphCheckboxList.setMorphs(morphs);
 
 			Logger.log("Sucessfully loaded " + morphs.size() + " translators");
 		} catch (Exception e) {
 			Logger.log("Problems loading Java translators ");
 			throw new RuntimeException(e);
 		}
 
 	}
 }
