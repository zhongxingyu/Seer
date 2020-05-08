 /*
 Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org
 
 Copyright (c) 2006-2013 Ewan Branda
 
 NETLab Hub is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 NETLab Hub is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package netlab.hub.processing.desktop;
 
 import java.awt.BorderLayout;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.TextArea;
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import netlab.hub.core.Config;
 import netlab.hub.core.Hub;
 import netlab.hub.core.IDataActivityMonitor;
 import netlab.hub.core.IHubLifecycleMonitor;
 import netlab.hub.core.ISessionLifecycleMonitor;
 import netlab.hub.serial.SerialPortClientRegistry;
 import netlab.hub.util.GUILogger;
 import netlab.hub.util.IGUILogger;
 import netlab.hub.util.Logger;
 import netlab.hub.util.NetworkUtils;
 import netlab.hub.util.ThreadUtil;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PImage;
 import processing.serial.Serial;
 import controlP5.Button;
 import controlP5.Canvas;
 import controlP5.CheckBox;
 import controlP5.ControlEvent;
 import controlP5.ControlFont;
 import controlP5.ControlP5;
 import controlP5.ControlWindowCanvas;
 import controlP5.Textarea;
 import controlP5.Textfield;
 import controlP5.Textlabel;
 
 public class HubDesktopApplication implements IDataActivityMonitor, ISessionLifecycleMonitor, IHubLifecycleMonitor {
 	
 	public static PApplet parent; // Make the instance available to anyone who needs it (eg serial port implementation)
 	
 	Hub hub;
 	MainWindow mainWindow;
 	LogWindow logWindow;
 	
 	List<String> clients = new ArrayList<String>();
 	
 	public HubDesktopApplication(Hub hub, PApplet parent) {
 		this.hub = hub;
 		hub.setHubLifecycleMonitor(this);
 		hub.setDataActivityMonitor(this);
 		hub.setSessionLifecycleMonitor(this);
 		// Special handling for toolbar icon for Windows. 
 		// See http://wiki.processing.org/w/Export_Info_and_Tips
 		if (PApplet.platform == PApplet.WINDOWS) {
 			File winIcon = new File(new File(hub.getRootDir(), "data"), "iconsmall.png");
 			ImageIcon winTitlebaricon = new ImageIcon(parent.loadBytes(winIcon.getAbsolutePath()));
 			parent.frame.setIconImage(winTitlebaricon.getImage());
 		}
 		File mainIconFile = new File(new File(hub.getRootDir(), "data"), "iconlarge.jpg");
 		PImage mainIconImg = parent.loadImage(mainIconFile.getAbsolutePath());
 		this.mainWindow = new MainWindow(mainIconImg, parent);
 		this.logWindow = new LogWindow();
 		HubDesktopApplication.parent = parent;
 		GUILogger.gui = new IGUILogger() {
 			public void print(String msg) {
 				logWindow.print(msg);
 				mainWindow.logPanel.append(msg, 30);
 			}
 		};
 	}
 	
 	public void showDashboardPage(String command) {
 		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
 	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
 	        try {
 	        	StringBuffer url = new StringBuffer();
 	        	url.append("http://").append(NetworkUtils.getLocalMachineAddress())
 	        		.append(":").append(Config.getAdminPort());
 	        	if (command != null && command.length() > 0) {
 	        		url.append("?").append("command=").append(command);
 	        	}
 	            desktop.browse(new URI(url.toString()));
 	        } catch (Exception ex) {
 	            Logger.error("Could not link to dashboard", ex);
 	        }
 	    }
 	}
 	
 	public void controlEvent(ControlEvent e) {
 		if (e.isFrom(mainWindow.showLog)) {
 //			showDashboardPage("log");
 			if (!logWindow.isVisible()) {
 				int x = parent.getLocationOnScreen().x + parent.width + 10;
 				int y = parent.getLocationOnScreen().y + 10;
 				logWindow.setLocation(x, y);
 				logWindow.setVisible(true);
 			}
 		} else if (e.isFrom(mainWindow.showDashboard)) {
 			showDashboardPage("");
 		} else if (e.isFrom(mainWindow.logDebugInfo)) {
 			Logger.switchDebugLevel();
 		} else if (e.isFrom(mainWindow.releasePorts)) {
 			SerialPortClientRegistry.releasePorts();
 		}
 	}
 	
 	public void displayAlert(String msg) {
 		this.mainWindow.displayAlert(msg);
 	}
 	
 	public void displayStatus(String s) {
 		//mainWindow.setStatus(s);
 	}
 	
 	public void toggleDebug() {
 		mainWindow.toggleDebug();
 	}
 	
 	public void dataReceived() {
 		mainWindow.showDataReceived();
 	}
 	
 	public void dataSent() {
 		mainWindow.showDataSent();
 	}
 	
 	public void sessionStarted(String clientId) {
 		clients.add(clientId);
 		mainWindow.updateClientList(clients);
 	}
 	
 	public void sessionEnded(String clientId) {
 		clients.remove(clientId);
 		mainWindow.updateClientList(clients);
 	}
 	
 	public void initializationComplete() {
 		new Thread(new Runnable() {
 			public void run() {
 				ThreadUtil.pause(1000); // Give the user time to read the splash screen
 				mainWindow.initializationComplete();
 			}
 		}).start();
 	}
 	
 	public void initializationFailed() {
 		mainWindow.setStatus("Startup failed. See log for details.");
 	}
 
 }
 
 
 class MainWindow {
 
 	ControlP5 controls;
 	
 	Textarea tagline;
 	ItemList clientList;
 	ItemList serialDeviceList;
 	Textarea messages;
 	Textlabel status;
 	Textarea alert;
 	Button releasePorts;
 	Button showLog;
 	Button showDashboard;
 	CheckBox logDebugInfo;
 	LabelFlasher rx, tx;
 	Textlabel appNameLabel;
 	Textlabel appVersionLabel;
 	IconCanvas icon;
 	Textarea logPanel;
 	
 	public MainWindow(PImage iconImg, PApplet parent) {
 		//parent.size(365, 350);
 		parent.size(750, 350);
 		controls = new ControlP5(parent);
 		
 		controls.addTextlabel("loglabel")
 		.setPosition(380, 50)
 		.setText("LOG")
 		;
 		
 		logPanel = controls.addTextarea("log")
 		.setPosition(380, 70)
 		.setSize(360, 260)
 		.setLineHeight(14)
 		//.setColorBackground(parent.color(255,100))
 		//.setColorForeground(parent.color(255,100))
 		.setColor(parent.color(170,200))
 		.scroll(1)
 		//.setFont(new ControlFont(parent.createFont("arial", 10, false)))
 		;
 		
 		tagline = controls.addTextarea("tagline")
 		.setPosition(50, 200)
 		.setWidth(400)
 		.setLineHeight(12) // Seems to have no effect when using custom font
 		//.setFont(new ControlFont(parent.createFont("arial", 11, false)))
 		;
 		StringBuffer intro = new StringBuffer();
 		//intro.append(Config.getAppVersion()).append("\n").append("\n");
 		intro.append("Part of the NETLab Toolkit open project").append("\n");
 		intro.append("(c) 2012 Phil Van Allen and Ewan Branda").append("\n");
 		tagline.setText(intro.toString());
 		
 		appNameLabel = controls.addTextlabel("appnamelabel")
 		.setPosition(50, 25)
 		//.setPosition(10, 5)
 		.setText(Config.getAppName())
 		.setFont(new ControlFont(parent.createFont("arial", 18, true)))
 		//.hide()
 		;
 		
 		appVersionLabel = controls.addTextlabel("appversionlabel")
 		.setPosition(165, 36)
 		//.setPosition(125, 16)
 		.setText("Version "+Config.getAppVersion())
 		//.setFont(new ControlFont(parent.createFont("arial", 18, true)))
 		//.hide()
 		;
 		
 		clientList = new ClientList("clients", 10, 50, 160, 10, controls);
 		clientList.hide();
 		updateClientList(new ArrayList<String>());
 		
 		serialDeviceList = new SerialDeviceList("serialdevices", 190, 50, 160, 10, controls);
 		serialDeviceList.hide();
 		updateSerialDeviceList(new ArrayList<String>());
 
 		Textlabel rxLabel = controls.addTextlabel("rxlabel")
 		.setPosition(400, 10)
 		.setText("Receive".toUpperCase())
 		.hide()
 		;
 		rx = new LabelFlasher(rxLabel, 0x0099ff, 0x666666, 2);
 		controls.addCanvas(rx);
 
 		Textlabel txLabel = controls.addTextlabel("txlabel")
 		.setPosition(450, 10)
 		.setText("Send".toUpperCase())
 		.hide()
 		;
 		tx = new LabelFlasher(txLabel, 0x0099ff, 0x666666, 2);
 		controls.addCanvas(tx);
 		
 		int buttonsOffset = 370;
 		
 		showDashboard = controls.addButton("dashboard")
 		.setPosition(buttonsOffset+10, 320)
 		.hide()
 		;
 	
 		showLog = controls.addButton("get full log")
 		.setPosition(buttonsOffset+90, 320)
 		.hide()
 		;
 		
 		logDebugInfo = controls.addCheckBox("logdebug")
 		.setPosition(buttonsOffset+180, 325)
 		.addItem("log debug info", 1)
 		.hide()
 		;
 		
 		releasePorts = controls.addButton("release ports")
 		.setPosition(buttonsOffset+280, 320)
 		.hide()
 		;
 		
 //		messages = controls.addTextarea("messages")
 //		.setWidth(160)
 //		.setPosition(300, 295)
 //		.setLineHeight(15)
 //		.setText("")
 //		;
 		
 		status = controls.addTextlabel("status")
 		.setWidth(150)
 		.setPosition(50, 320)
 		.setText("")
 		;
 		
 		PFont pfont = parent.createFont("Arial", 20, true);
 		alert = controls.addTextarea("alert")
 		.setWidth(350)
 		.setHeight(200)
 		.setPosition(75, 75)
 		.setLineHeight(16)
 		.setText("")
 		//.setColorForeground(parent.color(255, 0, 0, 255))
 		.setColorBackground(parent.color(255, 255))
 		.setColorValue(parent.color(255, 0, 0, 255))
 		.setFont(new ControlFont(pfont, 13))
 		.hide()
 		;
 
 		icon = new IconCanvas(50, 80, iconImg);
 		controls.addCanvas(icon);
 		
 		setStatus("Starting...");
 	}
 	
 	public void displayAlert(String msg) {
 		// alert.show(); // Don't show this for now until it is refined.
 		alert.setText(msg);
 	}
 	
 	public void setStatus(String s) {
 		if (s != null) {
 			status.show();
 			status.setText(s);
 		}
 	}
 	
 	public void toggleDebug() {
 		float newValue = logDebugInfo.getArrayValue()[0] > 0 ? 0 : 1;
 		logDebugInfo.setArrayValue(new float[]{newValue});
 	}
 	
 	public void initializationComplete() {
 		status.hide();
 		tagline.hide();
 		clientList.show();
 		serialDeviceList.show();
 		rx.label.show();
 		tx.label.show();
 		logPanel.show();
 //		messages.setText(("Server address: "+NetworkUtils.getLocalMachineAddress()+
 //							"\nTCP/IP socket port: "+Config.getPort()+
 //							"\nWebSocket port: "+Config.getWebSocketPort()).toUpperCase());
 		showLog.show();
 		showDashboard.show();
 		releasePorts.show();
 		logDebugInfo.show();
 		appNameLabel.setPosition(10, 5);
 		appVersionLabel.setPosition(125, 16);
 		icon.hide();
 		// Start monitoring for changes to connected serial devices
 		new Thread(new Runnable() {
 			List<String> devices = new ArrayList<String>();
 			public void run() {
				devices.clear();
				String[] current = Serial.list();
				for (int i=0; i<current.length; i++) {
					if (current[i].startsWith("/dev/cu.Bluetooth") || current[i].startsWith("/dev/tty.Bluetooth")) {
						//continue;
 					}
					devices.add(current[i]);
 				}
				updateSerialDeviceList(devices);
				ThreadUtil.pause(5000);
 			}
 		}).start();
 	}
 	
 	public void updateClientList(List<String> clients) {
 		clientList.update(clients);
 	}
 	
 	public void updateSerialDeviceList(List<String> devices) {
 		serialDeviceList.update(devices);
 	}
 
 	public void showDataReceived() {
 		rx.setOn();
 	}
 	
 	public void showDataSent() {
 		tx.setOn();
 	}
 }
 
 class IconCanvas extends ControlWindowCanvas {
 	PImage img = null;
 	boolean visible = true;
 	int x, y;
 	public IconCanvas(int x, int y, PImage img) {
 		this.x = x;
 		this.y = y;
 		this.img = img;
 	}
 	public void show() {
 		visible = true;
 	}
 	public void hide() {
 		visible = false;
 	}
 	public void draw(PApplet parent) {
 		if (visible)
 			parent.image(img, x, y);
 	}
 };
 
 class ClientList extends ItemList {
 	
 	public ClientList(String name, int x, int y, int width, int size, ControlP5 controls) {
 		super(name, x, y, width, size, controls);
 	}
 	
 	public void update(List<String> items) {
 		StringBuffer message = new StringBuffer();
 		message.append("Connected clients (")
 			.append(items.size())
 			.append(")")
 		;
 		setHeader(message.toString());
 		super.update(items);
 	}
 }
 
 class SerialDeviceList extends ItemList {
 		
 	public SerialDeviceList(String name, int x, int y, int width, int size, ControlP5 controls) {
 		super(name, x, y, width, size, controls);
 	}
 	
 	public void update(List<String> items) {
 		setHeader("Available serial devices");
 		super.update(items);
 	}
 }
 
 class ItemList {
 	
 	Textlabel header;
 	Textfield[] items;
 	Textlabel footer;
 	
 	public ItemList(String name, int x, int y, int width, int size, ControlP5 controls) {
 		items = new Textfield[size];
 		header = controls.addTextlabel(name+"itemlistheader")
 		.setWidth(width)
 		.setPosition(x, y)
 		;
 		for (int i=0; i<items.length; i++) {
 			items[i] = controls.addTextfield(name+"item"+i)
 			.setWidth(width)
 			.setPosition(x+2, header.getPosition().y+header.getHeight()-5+(i*22))
 			.setVisible(false)
 			.setUserInteraction(false)
 			.setLabelVisible(false)
 			;
 		}
 		footer = controls.addTextlabel(name+"itemlistfooter")
 		.setWidth(width)
 		.setPosition(x, items[items.length-1].getPosition().y+items[items.length-1].getHeight()+8)
 		;
 	}
 	
 	public void show() {
 		header.show();
 		for (int i=0; i<items.length; i++) {
 //			if (items[i].getText() == null || items[i].getText().length() == 0) {
 //				continue;
 //			}
 			items[i].show();
 		}
 		footer.show();
 	}
 	
 	public void hide() {
 		header.hide();
 		for (int i=0; i<items.length; i++) {
 			items[i].hide();
 		}
 		footer.hide();
 	}
 	
 	public void setHeader(String str) {
 		header.setText(str.toUpperCase());
 	}
 	
 	public void update(List<String> items) {
 		for (int i=0; i<Math.min(items.size(), this.items.length); i++) {
 			this.items[i].setText(items.get(i));
 			this.items[i].getCaptionLabel().hide();
 			this.items[i].setVisible(true);
 		}
 		for (int i=items.size(); i<this.items.length; i++) {
 			this.items[i].setText("");
 			//this.items[i].setVisible(false);
 			this.items[i].getCaptionLabel().hide();
 		}
 		if (items.size() > this.items.length) {
 			footer.setText("(Showing first "+this.items.length+" only)");
 		} else {
 			footer.setText("");
 		}
 	}
 }
 
 class LabelFlasher extends Canvas { // Canvas is used here simply as a hook between ControlP5 and draw()
 	
 	int onColor, offColor;
 	int numFramesOn;
 	int frameOnCount;
 	boolean on;
 	Textlabel label;
 	
 	public LabelFlasher(Textlabel label, int onColor, int offColor, int numFramesOn) {
 		this.onColor = onColor;
 		this.offColor = offColor;
 		this.numFramesOn = numFramesOn;
 		this.label = label;
 		label.setColorValueLabel(offColor);
 		on = false;
 	}
 	
 	public void setOn() {
 		on = true;
 		frameOnCount = 0;
 	}
 
 	public void draw(PApplet parent) {
 		if (on) {
 			if (frameOnCount == 0) {
 				label.setColorValueLabel(onColor);
 			}
 			frameOnCount++;
 			if (frameOnCount >= numFramesOn) {
 				on = false;
 				label.setColorValueLabel(offColor);
 			}
 		}
 	}
 	
 }
 
 @SuppressWarnings("serial")
 class LogWindow extends JFrame {
 	
 	TextArea outputField;
 	
 	public LogWindow() {
 		super("NETLab Hub Log");
 		getContentPane().setLayout(new BorderLayout());
 	    outputField = new TextArea();
 		JPanel textPanel = new JPanel();
 		textPanel.setLayout(new BorderLayout());
 		textPanel.add(outputField, BorderLayout.CENTER);
 		textPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
 		outputField.setFont(new Font("Helvetica", Font.PLAIN, 11));
 		getContentPane().add(textPanel, BorderLayout.CENTER);
 		setSize(700, 500);
 	}
 	
 	public Dimension getPreferredSize() {
 		return new Dimension(600, 600);
 	}
 	
 	public void print(String msg) {
 		outputField.append(msg);
 	}
 	
 	public void println(String msg) {
 		print(msg);
 		print("\n");
 	}
 	
 }
