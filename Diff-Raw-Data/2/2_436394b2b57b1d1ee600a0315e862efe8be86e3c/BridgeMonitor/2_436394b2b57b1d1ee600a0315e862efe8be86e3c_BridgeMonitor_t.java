 /*
  * Copyright 2007 Sun Microsystems, Inc.
  *
  * This file is part of jVoiceBridge.
  *
  * jVoiceBridge is free software: you can redistribute it and/or modify 
  * it under the terms of the GNU General Public License version 2 as 
  * published by the Free Software Foundation and distributed hereunder 
  * to you.
  *
  * jVoiceBridge is distributed in the hope that it will be useful, 
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied this 
  * code. 
  */
 
 package bridgemonitor;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 import java.io.IOException;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import com.sun.mpk20.voicelib.impl.service.voice.BridgeConnection;
 import com.sun.mpk20.voicelib.impl.service.voice.BridgeOfflineListener;
 
 import java.awt.BorderLayout;
 import java.awt.Point;
 
 import java.util.ArrayList;
 
 import com.sun.voip.CallParticipant;
 
 /**
  *
  * @author  jp
  */
 public class BridgeMonitor implements Runnable, BridgeOfflineListener {
     
     /** a logger */
     private static final Logger logger =
             Logger.getLogger(BridgeMonitor.class.getName());
 
     private BridgeStatusPanel bsp;
 
     private BridgeConnection bc;
 
     private static BridgeMonitor callTableOwner;
 
     private InsideCallBar insideCallBar;
 
     public BridgeMonitor(BridgeStatusPanel bsp) {
 	this.bsp = bsp;
     }
 
     public void initialize() {
         insideCallBar = new InsideCallBar();
 
         JPanel callBar = bsp.getCallBar();
 	callBar.add(insideCallBar, BorderLayout.CENTER);
 	callBar.validate();
 
         insideCallBar.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 insideCallBarMouseClicked(evt);
             }
 	});
 
 	setInitialState();
     }
     
     public void bridgeTextFieldKeyTyped(KeyEvent evt) {
 	setMonitorButton();
 	setBridgeInfoButton();
 	setEnableButton();
     }
 
     private void connect() throws IOException {
 	if (bc != null && bc.isConnected()) {
 	    return;
 	}
 
 	String bridgeText = bsp.getBridgeTextField().getText();
 
         bridgeText = bridgeText.substring(0, bridgeText.length());
 
 	String[] tokens = bridgeText.split(":");
 
 	if (tokens.length < 1) {
 	    logger.info("Syntax is <host>:<sipPort>:<controlPort>");
 	    throw new IOException("Syntax is <host>:<sipPort>:<controlPort>");
 	}
 
         String server;
         
 	try {
 	    server = InetAddress.getByName(tokens[0]).getHostAddress();
 	} catch (UnknownHostException e) {
 	    throw new IOException("Couldn't get bridge connection:  " 
 	     	+ e.getMessage());
 	}
  
 	int sipPort = 5060;
 
 	if (tokens.length >= 2) {
 	    try {
 	        sipPort = Integer.parseInt(tokens[1]);
 	    } catch (NumberFormatException e) {
 	        logger.info("Syntax is <host>:<sipPort>:<controlPort>");
 	        throw new IOException("Syntax is <host>:<sipPort>:<controlPort>");
 	    }
 	}
 
 	int controlPort = 6666;
 
 	if (tokens.length >= 3) {
 	    try {
 	        controlPort = Integer.parseInt(tokens[2]);
 	    } catch (NumberFormatException e) {
 	        logger.info("Syntax is <host>:<sipPort>:<controlPort>");
 	        throw new IOException("Syntax is <host>:<sipPort>:<controlPort>");
 	    }
 	}
 
 	logger.info(" trying to connect to " + server + ":" 
 	    + sipPort + ":" + controlPort);
 
 	bc = new BridgeConnection(server, sipPort, controlPort, true);
 
 	bc.addBridgeOfflineListener(this);
     }
 
     public void bridgeTextFieldKeyReleased(KeyEvent evt) {
         String s = bsp.getBridgeTextField().getText(); 
 
         if (s.length() == 0) {
             setInitialState();
         }
     }
 
     public void monitorButtonMouseClicked(MouseEvent evt) {
 	JButton monitorButton = bsp.getMonitorButton();
 
         String s = monitorButton.getText();
 	
 	if (s.equalsIgnoreCase("Monitor")) {
 	    try {
 	        connect();
 	    } catch (IOException e) {
 	        logger.info("Unable to connect to bridge:  " + e.getMessage());
 	        return;
 	    }
 
 	    setMonitoring();
 	    showBridgeCalls();
 
 	    done = false;
             new Thread(this).start();
 	    return;
 	}
 
 	done();
 	setInitialState();
     }
 
     private void setInitialState() {
 	setMonitorButton();
 	setEnableButton();
 	setBridgeInfoButton();
         bsp.getMonitorButton().setText("Monitor");
         bsp.getStatusLabel().setText("");
         bsp.getBridgeTextField().setEditable(true);
 	bsp.getEnableButton().setText("Disable");
     }
 
     private void setMonitoring() {
         bsp.getMonitorButton().setText("Stop Monitoring");
         bsp.getStatusLabel().setText("Online");
         bsp.getBridgeTextField().setEditable(false);
     }
 
     private void setOffline() {
	setMonitorButton();
 	setEnableButton();
 	setBridgeInfoButton();
 
         bsp.getMonitorButton().setText("Monitor");
         bsp.getStatusLabel().setText("Offline");
         bsp.getBridgeTextField().setEditable(true);
 	bsp.getCallLabel().setVisible(true);
 	bsp.getCallLabel().setText("");
     }
 
     private void setMonitorButton() {
 	if (bsp.getBridgeTextField().getText().length() == 0) {
             bsp.getMonitorButton().setEnabled(false);
 	} else {
 	    bsp.getMonitorButton().setEnabled(true);
 	}
     }
 
     private void setEnableButton() {
 	if (bsp.getBridgeTextField().getText().length() == 0) {
             bsp.getEnableButton().setEnabled(false);
 	} else {
 	    bsp.getEnableButton().setEnabled(true);
 	}
     }
 
     private void setBridgeInfoButton() {
 	if (bsp.getBridgeTextField().getText().length() == 0) {
 	    bsp.getBridgeInfoButton().setEnabled(false);
 	} else {
 	    bsp.getBridgeInfoButton().setEnabled(true);
 	}
     }
 
     public void bridgeInfoButtonMouseClicked(MouseEvent evt) {
 	try {
 	    connect();
 	} catch (IOException e) {
 	    logger.info("Unable to connect to bridge:  " + e.getMessage());
 	    setOffline();
 	    return;
 	}
 
 	try {
 	    String info = bc.getBridgeInfo();
 
 	    JFrame jFrame = new JFrame("Bridge Info for " + bc);
 
 	    String[] tokens = info.split("\n");
 
 	    JTable jTable = new JTable(0, 2);
 
 	    DefaultTableModel model = (DefaultTableModel) jTable.getModel();
 
 	    model.setRowCount(tokens.length);
 
 	    String[] columnIdentifiers = new String[2];
 	    columnIdentifiers[0] = "Parameter";
 	    columnIdentifiers[1] = "Value";
 
 	    model.setColumnIdentifiers(columnIdentifiers);
 
 	    logger.info("tokens length " + tokens.length);
 
 	    for (int i = 0; i < tokens.length; i++) {
 		String s = tokens[i];
 
 		String[] nameValue = s.split("=");
 
 		jTable.setValueAt(nameValue[0].trim(), i, 0);
 		jTable.setValueAt(nameValue[1].trim(), i, 1);
 	    }
 
 	    JScrollPane jScrollPane = new JScrollPane(jTable);
 
 	    jFrame.add(jScrollPane);
 
 	    jFrame.setVisible(true);
 	    jFrame.pack();
 	    jFrame.toFront();
 	    jFrame.setResizable(true);
 	} catch (IOException e) {
 	    logger.info(e.getMessage());
 	}
     }
 
     public void enableButtonMouseClicked(MouseEvent evt) {
 	JButton enableButton = bsp.getEnableButton();
 
         String s = enableButton.getText();
 	
 	try {
 	    connect();
 	} catch (IOException e) {
 	    logger.info("Unable to connect to bridge:  " + e.getMessage());
 	    return;
 	}
 
         monitorButtonMouseClicked(null);
 
 	if (s.equalsIgnoreCase("Enable")) {
 	    try {
 	        bc.suspend(false);
 		enableButton.setText("Disable");
 		bsp.getMonitorButton().setEnabled(true);
 	    } catch (IOException e) {
 	        logger.info("Unable to resume " 
 		    + bsp.getBridgeTextField().getText()
 		    + " " + e.getMessage());
 		setOffline();
 	    }
 
 	    return;
 	} 
 
 	done();
 
 	try {
 	    bc.suspend(true);
 	    bc.disconnect();
 	    setOffline();
 	    enableButton.setText("Enable");
 	} catch (IOException e) {
 	    logger.info("Unable to suspend " + bsp.getBridgeTextField().getText()
 		+ " " + e.getMessage());
 	    setOffline();
 	}
     }
 
     public void insideCallBarMouseClicked(java.awt.event.MouseEvent evt) {
 	if (bc.isConnected() == false) {
 	    logger.info("Not connected:  " + bc);
 	    setOffline();
 	    return;
 	}
 
 	bridgeCalls = bc;
 
 	showBridgeCalls();
     }
 
     private void showBridgeCalls() {
         String status;
         
 	try {
 	    status = bc.getCallInfo();
 	} catch (IOException e) {
 	    logger.info(e.getMessage());
 	    return;
 	}
 
 	String[] tokens = status.split("\n");
 
 	int callCount = 0;
 
 	for (int i = 0; i < tokens.length; i++) {
 	    if (tokens[i].startsWith("    ") == false) {
 		continue;
 	    }
 
 	    callCount++;
 	}
 
 	if (callCount == 0) {
 	    return;
 	}
 
 	JTable callTable = bsp.getCallTable();
 
 	synchronized (callTable) {
 	    DefaultTableModel model = (DefaultTableModel) callTable.getModel();
 
 	    if (callTableOwner != this) {	
 	        String[] columnIdentifiers = new String[3];
                 columnIdentifiers[0] = "CallID";
                 columnIdentifiers[1] = "Bridge " + bc;
                 columnIdentifiers[2] = "Audio Quality";
 	        model.setColumnIdentifiers(columnIdentifiers);
 	    }
 
 	    callTableOwner = this;
 	    model.setRowCount(callCount);
             
 	    callCount = 0;
             int myCt = 0;
             
 	    for (int i = 0; i < tokens.length; i++) {
                 String[] items = tokens[i].split("@");
                 String cID = items[0];
                 
 	        if (tokens[i].startsWith("    ") == false) {
 		    continue;
 	        }
                 
                
                 String[] callDetails = items[1].split(" ");
                 int cdLen = callDetails.length;
                 String aQualityTmp = callDetails[cdLen -1];
                 String[] aQTarr = aQualityTmp.split(":");
                 String aQuality = aQTarr[1];
                 String fName = callDetails[0];
                 String[] checkFname = items[1].split(" ");
                     if(checkFname.length > 2) {
                         // if the audio filename has a space in it, build the name string using the array elts
                         // in between 0 and cdLen - 1
                         
                         // reset fName
                         fName = "";
                         for(int n = 0;n < cdLen - 1; n++) {
                             // if the audio filename has a space in it, build the name string using the array elts
                             // in between 0 and cdLen - 2 
                             fName = fName + callDetails[n] + " ";
                         }
                     }
                 
                 String[] infoArr = {cID, fName, aQuality};
                 for(int j = 0; j < infoArr.length; j++) {
                     model.setValueAt(infoArr[j], myCt, j);
                 }
                     
                 myCt++;
 	    }
 	}
     }
 
     public void callTableMouseClicked(java.awt.event.MouseEvent evt) {
 	JTable callTable = bsp.getCallTable();
 
 	int[] rows = callTable.getSelectedRows();
 
 	for (int i = 0; i < rows.length; i++) {
 	    String callId = (String) 
 		callTable.getValueAt(rows[i], 0);
 
 	    callId = callId.trim();
 
 	    if (callId.startsWith("sip:")) {
 		callId = callId.substring(4);
 	    }
 
             new CallInfoUpdater(i * 200, bc, (String) callId);
 	}
     }
     
     private static int lastTotalCalls = 0;
 
     private BridgeConnection bridgeCalls;
 
     class InsideCallBar extends JPanel {
 	private int callCount;
         
         public void setCallCount(int callCount) {
             this.callCount = callCount;
             repaint();
         }
         
 	public void paintComponent(Graphics g) {
             //int height = callCount * 20;
 	    int height = (getHeight() * callCount) / 10;
             
             g.setColor(getBackground());
             g.fillRect(0, 0, getWidth(), getHeight());
             
 	    g.setColor(Color.GREEN);
 	    g.fillRect(0, getHeight() - height, getWidth(), height);
             
             //g.setColor(Color.RED);
             //g.drawRect(0, 0, getWidth(), getHeight());
         }
     }
 
     private boolean done;
 
     public void done() {
 	synchronized (this) {
 	    if (done) {
 		return;
 	    }
 
 	    done = true;
 
 	    try {
 	  	wait();
 	    } catch (InterruptedException e) {
 	    }
 	}
     }
 
     public void run() {
 	int lastNumberOfCalls = 0;
 
         while (!done) {
     	    try {
 		Thread.sleep(2000);
 	    } catch (InterruptedException e) {
 	    }
 
 	    if (callTableOwner == this) {
 		showBridgeCalls();
 	    }
             
 	    try {
 		String status = bc.getBridgeStatus();
 
 		status = status.replaceAll("\t", "");
 
 		String[] tokens = status.split("\n");
 
 		tokens = tokens[1].split(":");
 
 		int n;
 
 		try {
 		    n = Integer.parseInt(tokens[1]);		
 		} catch (NumberFormatException e) {
 		    logger.info("Unable to parse status:  " + status);
 		    continue;
 		}
 
 		String s = " Calls";
 
 		if (n == 1) {
 		    s = " Call";
 		}
 
                 bsp.getCallLabel().setText(n + s);
 
 		if (n == lastNumberOfCalls) {
 		    continue;
 		}
 
 		insideCallBar.setCallCount(n);  
 		if (n > lastTotalCalls) {
 		    lastTotalCalls = n;
 		}
 
 		bsp.getEnableButton().setText("Disable");
 	    } catch (IOException e) {
 		logger.info("Unable to get status for " + bc);
                 
 		setOffline();
 		break;
 	    }
 	}
 
 	lastTotalCalls = 0;
 
 	if (callTableOwner == this) {
             synchronized (callTableOwner) {
 		bridgeCalls = null;
 
                 JTable callTable = bsp.getCallTable();
 	        callTable.removeAll();
 	        callTableOwner = null;
 
                 //String[] columnIdentifiers = new String[1];
                 //columnIdentifiers[0] = "Calls";
 
 	        //DefaultTableModel model = (DefaultTableModel) callTable.getModel();
                 //model.setColumnIdentifiers(columnIdentifiers);
             }
 	}
 
 	insideCallBar.setCallCount(0);
 	bsp.getCallLabel().setText("");
 
 	synchronized (this) {
 	    done = true;
 	    notifyAll();
 	}
     }
 
     public void bridgeOffline(BridgeConnection bc, 
 	    ArrayList<CallParticipant> calls) {
 
 	logger.info("Bridge offline:  " + bc);
 	bc.disconnect();
 	setOffline();
     }
 
 }
