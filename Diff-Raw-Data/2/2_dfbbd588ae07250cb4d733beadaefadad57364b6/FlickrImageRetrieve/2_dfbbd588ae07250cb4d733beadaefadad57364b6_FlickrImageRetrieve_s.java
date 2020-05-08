 /*
  * Copyright 2011 Nicolas Herv.
  * 
  * This file is part of FlickrImageRetrieve, which is an ICY plugin.
  * 
  * FlickrImageRetrieve is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * FlickrImageRetrieve is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FlickrImageRetrieve. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package plugins.nherve.flickr;
 
 import icy.gui.component.ComponentUtil;
 import icy.gui.util.GuiUtil;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import plugins.nherve.flickr.tools.FlickrFrontend;
 import plugins.nherve.flickr.tools.FlickrImage;
 import plugins.nherve.toolbox.NherveToolbox;
 import plugins.nherve.toolbox.plugin.HelpWindow;
 import plugins.nherve.toolbox.plugin.SingletonPlugin;
 
 /**
  * 
  * @author Nicolas HERVE - n.herve@laposte.net
  */
 public class FlickrImageRetrieve extends SingletonPlugin implements ActionListener, FlickrWorkerListener {
 	public final static String COPYRIGHT_HTML = "Copyright 2011 Nicolas HERVE";
	private static String HELP = "<html>" + "<p align=\"center\"><b>" + HelpWindow.TAG_FULL_PLUGIN_NAME + "</b></p>" + "<p align=\"center\"><b>" + NherveToolbox.DEV_NAME_HTML + "</b></p>" + "<p align=\"center\"><a href=\"http://www.herve.name/pmwiki.php/Main/FlickrImageRetrieve\">Online help is available</a></p>" + "<p align=\"center\"><b>" + COPYRIGHT_HTML + "</b></p>" + "<hr/>" + "<p>" + HelpWindow.TAG_PLUGIN_NAME + NherveToolbox.LICENCE_HTML + "</p>" + "<p>" + NherveToolbox.LICENCE_HTMLLINK + "</p>" + "</html>";
 	private final static String APP_KEY = "70331e00a63dc50a87f0a7a40e1242ad";
 	private JButton btGrabRandom;
 	private JButton btGrabByTag;
 	private JTextField tfTag;
 	private JTextArea taLog;
 	private JTextField tfMaxToGrab;
 	private JButton btHelp;
 	private JRadioButton rbRecent;
 	private JRadioButton rbInterestingness;
 	private JProgressBar pbProgress;
 	private JCheckBox cbSingleImage;
 
 	private FlickrFrontend flickr;
 //	private FlickrThumbnailProvider provider;
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object o = e.getSource();
 
 		if (o == null) {
 			return;
 		}
 
 		if (o instanceof JButton) {
 			JButton b = (JButton) e.getSource();
 			if (b == null) {
 				return;
 			}
 
 			if (b == btHelp) {
 				openHelpWindow(HELP, 400, 300);
 				return;
 			}
 
 			if (b == btGrabRandom) {
 				if (rbRecent.isSelected()) {
 					grab(FlickrWorker.TYPE_RECENT, null);
 				} else if (rbInterestingness.isSelected()) {
 					grab(FlickrWorker.TYPE_INTERESTINGNESS, null);
 				}
 			}
 
 			if (b == btGrabByTag) {
 				grab(FlickrWorker.TYPE_TAGS, tfTag.getText());
 			}
 		}
 	}
 
 	public void display(FlickrImage img) {
 		btGrabByTag.setEnabled(false);
 		btGrabRandom.setEnabled(false);
 		pbProgress.setIndeterminate(true);
 		pbProgress.setValue(0);
 		pbProgress.setStringPainted(true);
 
 		FlickrWorker worker = new FlickrWorker(flickr, this);
 		worker.setType(FlickrWorker.TYPE_IMAGE);
 		worker.setImage(img);
 		worker.addListener(this);
 
 		Thread t = new Thread(worker);
 		t.start();
 	}
 
 	@Override
 	public void displayMessage(String message) {
 		taLog.append(message + "\n");
 	}
 
 	@Override
 	public void fillInterface(JPanel mainPanel) {
 		setUIDisplayEnabled(true);
 
 		flickr = new FlickrFrontend(APP_KEY);
 		new FlickrThumbnailProvider(flickr);
 
 		// Random
 		ButtonGroup bgRandomSource = new ButtonGroup();
 		rbInterestingness = new JRadioButton("Interestingness");
 		bgRandomSource.add(rbInterestingness);
 		rbRecent = new JRadioButton("Recent");
 		bgRandomSource.add(rbRecent);
 		rbInterestingness.setSelected(true);
 
 		btGrabRandom = new JButton("Grab");
 		btGrabRandom.addActionListener(this);
 
 		JPanel randomPanel = GuiUtil.createLineBoxPanel(rbInterestingness, Box.createHorizontalGlue(), rbRecent, Box.createHorizontalGlue(), btGrabRandom);
 		randomPanel.setBorder(new TitledBorder("Random image"));
 
 		// Search
 		tfTag = new JTextField();
 		tfTag.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				int key = e.getKeyCode();
 				if (key == KeyEvent.VK_ENTER) {
 					grab(FlickrWorker.TYPE_TAGS, tfTag.getText());
 				}
 			}
 		});
 		ComponentUtil.setFixedSize(tfTag, new Dimension(300, 25));
 		btGrabByTag = new JButton("Grab");
 		btGrabByTag.addActionListener(this);
 
 		JPanel searchPanel = GuiUtil.createLineBoxPanel(tfTag, Box.createHorizontalGlue(), btGrabByTag);
 		searchPanel.setBorder(new TitledBorder("Search image"));
 
 		// Log window
 		taLog = new JTextArea();
 		taLog.setEditable(false);
 		taLog.setLineWrap(true);
 		taLog.setColumns(35);
 		taLog.setRows(10);
 		JScrollPane taLogScroll = new JScrollPane(taLog);
 		taLogScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		// Progress bar
 		btHelp = new JButton(NherveToolbox.questionIcon);
 		btHelp.setToolTipText("About this plugin");
 		btHelp.addActionListener(this);
 		tfMaxToGrab = new JTextField("100");
 		ComponentUtil.setFixedWidth(tfMaxToGrab, 50);
 		cbSingleImage = new JCheckBox("Single");
 		pbProgress = new JProgressBar();
 		JPanel progressPanel = GuiUtil.createLineBoxPanel(btHelp, tfMaxToGrab, cbSingleImage, pbProgress);
 
 		// Frame stuff
 		int spacing = 10;
 		JPanel myPanel = GuiUtil.createPageBoxPanel(Box.createVerticalGlue(), randomPanel, Box.createVerticalStrut(spacing), searchPanel, Box.createVerticalStrut(spacing), progressPanel, Box.createVerticalStrut(spacing), taLogScroll, Box.createVerticalGlue());
 		mainPanel.add(myPanel);
 	}
 
 	@Override
 	public Dimension getDefaultFrameDimension() {
 		return null;
 	}
 
 	private void grab(int type, String tags) {
 		if (cbSingleImage.isSelected()) {
 			btGrabByTag.setEnabled(false);
 			btGrabRandom.setEnabled(false);
 			pbProgress.setIndeterminate(true);
 			pbProgress.setValue(0);
 			pbProgress.setStringPainted(true);
 
 			FlickrWorker worker = new FlickrWorker(flickr, this);
 			worker.setType(type);
 			if (type == FlickrWorker.TYPE_TAGS) {
 				worker.setTags(tags);
 			}
 			worker.addListener(this);
 
 			Thread t = new Thread(worker);
 			t.start();
 		} else {
 			grabGrid(type, tags);
 		}
 	}
 	
 	private void grabGrid(int type, String tags) {
 		btGrabByTag.setEnabled(false);
 		btGrabRandom.setEnabled(false);
 		pbProgress.setIndeterminate(true);
 		pbProgress.setValue(0);
 		pbProgress.setStringPainted(true);
 
 		FlickrWorker worker = new FlickrWorker(flickr, this);
 		int mtg = 100;
 		try {
 			mtg = Integer.parseInt(tfMaxToGrab.getText());
 		} catch (NumberFormatException e) {
 			mtg = 100;
 			displayMessage(e.getMessage());
 		}
 		worker.setMaxToGrab(mtg);
 		worker.setType(type);
 		if (type == FlickrWorker.TYPE_TAGS) {
 			worker.setTags(tags);
 		}
 		worker.addListener(this);
 
 		Thread t = new Thread(worker);
 		t.start();
 	}
 
 	public boolean isGrabEnabled() {
 		return btGrabRandom.isEnabled();
 	}
 
 	@Override
 	public void notifyNewProgressionStep(String step) {
 		pbProgress.setIndeterminate(true);
 		pbProgress.setString(step);
 	}
 
 	@Override
 	public void notifyProcessEnded(FlickrWorker w) {
 		if (w.getMaxToGrab() > 1) {
 			if (w.getImages() != null) {
 				FlickrImageGrid grid = new FlickrImageGrid();
 				switch (w.getType()) {
 				case FlickrWorker.TYPE_RECENT:
 					grid.setTitle("FiR - recent uploads");
 					break;
 				case FlickrWorker.TYPE_INTERESTINGNESS:
 					grid.setTitle("FiR - interestingness");
 					break;
 				case FlickrWorker.TYPE_TAGS:
 					grid.setTitle("FiR - " + w.getTags());
 					break;
 				default:
 					grid.setTitle("FiR");
 					break;
 				}
 				
 				grid.setImages(w.getImages());
 				grid.startInterface(getFrame());
 
 				for (final FlickrImage i : grid.getImages()) {
 					i.setPlugin(this);
 //					new Thread(new Runnable() {
 //
 //						@Override
 //						public void run() {
 //							try {
 //								i.setInternal(flickr.loadImageThumbnail(i, null));
 //							} catch (final FlickrException e) {
 //								i.setInternal(null);
 //								i.removedFromGrid();
 //								ThreadUtil.invokeLater(new Runnable() {
 //									@Override
 //									public void run() {
 //										displayMessage(e.getClass().getName() + " : " + e.getMessage());
 //									}
 //								});
 //							}
 //						}
 //					}).start();
 				}
 			}
 		}
 
 		btGrabByTag.setEnabled(true);
 		btGrabRandom.setEnabled(true);
 		pbProgress.setIndeterminate(false);
 		pbProgress.setStringPainted(false);
 		pbProgress.setValue(0);
 		pbProgress.setString(null);
 	}
 
 	@Override
 	public boolean notifyProgress(double position, double length) {
 		pbProgress.setIndeterminate(false);
 		pbProgress.setMaximum((int) length);
 		pbProgress.setValue((int) position);
 		return true;
 	}
 
 	@Override
 	public void sequenceHasChanged() {
 	}
 
 	@Override
 	public void sequenceWillChange() {
 	}
 
 	@Override
 	public void stopInterface() {
 		
 	}
 
 }
