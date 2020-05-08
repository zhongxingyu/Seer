 /*
 Copyright (c) 2013 Ferdinand Niedermann
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.nerdinand.podcasttimer;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 
 public class PodcastTimer extends Frame implements ActionListener,
 		ClipboardOwner {
 	private static final long serialVersionUID = -4468814973297824610L;
 
 	private JButton _startTimerButton;
 	private long _startTime;
 	private JButton _enterChapter;
 
 	private SimpleDateFormat _dateFormat = new SimpleDateFormat("HH:mm:ss.000",
 			Locale.getDefault());
 	private JTextArea _chapterText;
 
 	public void addComponentsToPane(Container pane) {
 		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
 
 		_enterChapter = new JButton("Enter Chapter");
 		_enterChapter.setAlignmentX(Component.CENTER_ALIGNMENT);
 		pane.add(_enterChapter);
 		_enterChapter.addActionListener(this);
 
 		_chapterText = new JTextArea();
 		_chapterText.setAlignmentX(Component.CENTER_ALIGNMENT);
 		_chapterText.setFont(new Font("Courier", Font.PLAIN, 12));
 		pane.add(_chapterText);
 
 		_startTimerButton = new JButton("Start Timer");
 		_startTimerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
 		pane.add(_startTimerButton);
 		_startTimerButton.addActionListener(this);
 
 	}
 
 	/**
 	 * Create the GUI and show it. For thread safety, this method should be
 	 * invoked from the event-dispatching thread.
 	 */
 	private void createAndShowGUI() {
 		// Create and set up the window.
		JFrame frame = new JFrame("BoxLayoutDemo");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// Set up the content pane.
 		addComponentsToPane(frame.getContentPane());
 
 		frame.setMinimumSize(new Dimension(500, 800));
 
 		// Display the window.
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static void main(String[] args) {
 		// Schedule a job for the event-dispatching thread:
 		// creating and showing this application's GUI.
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				PodcastTimer podcastTimer = new PodcastTimer();
 				podcastTimer.createAndShowGUI();
 			}
 		});
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		Object source = event.getSource();
 		if (source == _startTimerButton) {
 			_startTime = System.currentTimeMillis();
 		} else if (source == _enterChapter) {
 			long seconds = System.currentTimeMillis() - _startTime - 5 * 1000;
 
 			String time = _dateFormat.format(new Date(seconds
 					- TimeZone.getDefault().getRawOffset()));
 
 			String chapterTitle = (String) JOptionPane.showInputDialog(this,
 					"Enter a chapter title: ", "Chapter title at " + time,
 					JOptionPane.PLAIN_MESSAGE, null, null, "");
 
 			_chapterText.append("\n" + time + " " + chapterTitle);
 
 			setClipboard("[" + time + "]");
 		}
 	}
 
 	private void setClipboard(String string) {
 		StringSelection stringSelection = new StringSelection(string);
 		Clipboard clipboard = getToolkit().getSystemClipboard();
 		clipboard.setContents(stringSelection, this);
 	}
 
 	@Override
 	public void lostOwnership(Clipboard arg0, Transferable arg1) {
 	}
 }
