 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 
 import net.rptools.clientserver.hessian.client.ClientConnection;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Campaign;
 import net.rptools.maptool.model.Player;
 import net.rptools.maptool.server.MapToolServer;
 import net.rptools.maptool.util.FileUtil;
 import net.rptools.maptool.util.PersistenceUtil;
 
 
 /**
  */
 public class ClientActions {
 
     public static final Action TOGGLE_GRID = new ClientAction() {
 
         {
             putValue(Action.NAME, "Toggle Grid");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
         }
 
         public void execute(ActionEvent e) {
             ZoneRenderer renderer = MapToolClient.getCurrentZoneRenderer();
             if (renderer != null) {
                 renderer.toggleGrid();
             }
         }
     };
 
     public static final Action ZOOM_IN = new ClientAction() {
 
         {
             putValue(Action.NAME, "Zoom In");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0));
         }
 
         public void execute(ActionEvent e) {
             ZoneRenderer renderer = MapToolClient.getCurrentZoneRenderer();
             if (renderer != null) {
             	Dimension size = renderer.getSize();
                 renderer.zoomIn(size.width/2, size.height/2);
             }
         }
     };
 
     public static final Action ZOOM_OUT = new ClientAction() {
 
         {
             putValue(Action.NAME, "Zoom Out");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0));
         }
 
         public void execute(ActionEvent e) {
             ZoneRenderer renderer = MapToolClient.getCurrentZoneRenderer();
             if (renderer != null) {
             	Dimension size = renderer.getSize();
                 renderer.zoomOut(size.width/2, size.height/2);
             }
         }
     };
 
     public static final Action ZOOM_RESET = new ClientAction() {
 
         {
             putValue(Action.NAME, "Zoom 1:1");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.SHIFT_MASK));
         }
 
         public void execute(ActionEvent e) {
             ZoneRenderer renderer = MapToolClient.getCurrentZoneRenderer();
             if (renderer != null) {
                 renderer.zoomReset();
             }
         }
     };
 
     public static final Action TOGGLE_ZONE_SELECTOR = new ClientAction() {
 
         {
             putValue(Action.NAME, "Toggle Zone Selector");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0));
         }
 
         public void execute(ActionEvent e) {
         	
         	ZoneSelectionPanel panel = MapToolClient.getInstance().getZoneSelectionPanel();
         	
         	panel.setVisible(!panel.isVisible());
         }
     };
 
     public static final Action START_SERVER = new ClientAction() {
 
         {
             putValue(Action.NAME, "Start server");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
         }
 
         public void execute(ActionEvent e) {
 
             runBackground(new Runnable(){
                 public void run() {
 
                 	StartServerDialog dialog = new StartServerDialog();
                 	
                 	dialog.setVisible(true);
 
                 	if (dialog.getOption() == StartServerDialog.OPTION_CANCEL) {
                 		new MainMenuDialog().setVisible(true);
                 		return;
                 	}
                 	
                 	
                 	try {
                 		int port = dialog.getPort();
                 		
                 		MapToolClient.startServer(port);
 
                 		// Connect to server
                        MapToolClient.getInstance().createConnection("localhost", port, new Player(dialog.getUsername(), Player.Role.GM));
                 		
                 	} catch (UnknownHostException uh) {
                 		MapToolClient.showError("Whoah, 'localhost' is not a valid address.  Weird.");
                 		return;
                 	} catch (IOException ioe) {
                 		MapToolClient.showError("Could not connect to server: " + ioe);
                 		return;
                 	}
                 	
                 	// TODO: I don't like this here
                 	CONNECT_TO_SERVER.setEnabled(false);
                 	
 				}
         	});
         }
 
     };
 
     public static final Action CONNECT_TO_SERVER = new ClientAction() {
 
         {
             putValue(Action.NAME, "Connect to server");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
         }
 
         public void execute(ActionEvent e) {
 
             try {
             	
             	ConnectToServerDialog dialog = new ConnectToServerDialog();
             	
             	dialog.setVisible(true);
             	
             	if (dialog.getOption() == ConnectToServerDialog.OPTION_CANCEL) {
             		
             		new MainMenuDialog().setVisible(true);
             		return;
             	}
             	
                 MapToolClient.getInstance().createConnection(dialog.getServer(), dialog.getPort(), new Player(dialog.getUsername(), dialog.getRole()));
             } catch (UnknownHostException e1) {
                 // TODO Auto-generated catch block
                 MapToolClient.showError("Unknown host");
                 e1.printStackTrace();
             } catch (IOException e1) {
                 // TODO Auto-generated catch block
                 MapToolClient.showError("IO Error: " + e1);
                 e1.printStackTrace();
             }
             
             // TODO: I don't like this here
             START_SERVER.setEnabled(false);
         }
 
     };
 
     public static final Action LOAD_CAMPAIGN = new ClientAction () {
     	
         {
             putValue(Action.NAME, "Load Campaign");
         }
     	
         public void execute(ActionEvent ae) {
         
         	JFileChooser chooser = MapToolClient.getLoadFileChooser();
         	chooser.setDialogTitle("Load Campaign");
         	
         	if (chooser.showOpenDialog(MapToolClient.getInstance()) == JFileChooser.APPROVE_OPTION) {
         		
         		try {
         			Campaign campaign = PersistenceUtil.loadCampaign(chooser.getSelectedFile());
         			
         			if (campaign != null) {
         				
         				MapToolClient.setCampaign(campaign);
         				
         				if (MapToolClient.isConnected()) {
         					
                             ClientConnection conn = MapToolClient.getInstance().getConnection();
                             
                             conn.callMethod(MapToolClient.COMMANDS.setCampaign.name(), campaign);
         				}
         			}
         			
         		} catch (IOException ioe) {
         			MapToolClient.showError("Could not load campaign: " + ioe);
         		}
         	}
         }
     };
     
     public static final Action SAVE_CAMPAIGN = new ClientAction () {
     	
         {
             putValue(Action.NAME, "Save Campaign");
         }
     	
         public void execute(ActionEvent ae) {
         
         	Campaign campaign = MapToolClient.getCampaign();
         	
         	// TODO: this should eventually just remember the last place it was saved
         	JFileChooser chooser = MapToolClient.getSaveFileChooser();
         	chooser.setDialogTitle("Save Campaign");
         	
         	if (chooser.showSaveDialog(MapToolClient.getInstance()) == JFileChooser.APPROVE_OPTION) {
         		
         		try {
         			PersistenceUtil.saveCampaign(campaign, chooser.getSelectedFile());
         		} catch (IOException ioe) {
         			MapToolClient.showError("Could not save campaign: " + ioe);
         		}
         	}
         }
     };
     
     public static final Action LOAD_MAP = new ClientAction() {
         {
             putValue(Action.NAME, "Load Map");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
         }
 
         public void execute(java.awt.event.ActionEvent e) {
 
             runBackground(new Runnable() {
 
                 public void run() {
                     JFileChooser loadFileChooser = MapToolClient.getLoadFileChooser();
 
                     loadFileChooser.setDialogTitle("Load Map");
                     loadFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
                     if (loadFileChooser.showOpenDialog(MapToolClient.getInstance()) == JFileChooser.CANCEL_OPTION) {
                     	return;
                     }
                     if (loadFileChooser.getSelectedFile() == null) {
                         return;
                     }
 
                     try {
                         byte[] imgData = FileUtil.loadFile(loadFileChooser.getSelectedFile());
                         Asset asset = new Asset(imgData);
                         AssetManager.putAsset(asset);
 
                         // TODO: this needs to be abstracted into the client
                         if (MapToolClient.isConnected()) {
                             ClientConnection conn = MapToolClient.getInstance().getConnection();
                             
                             conn.callMethod(MapToolClient.COMMANDS.putAsset.name(), asset);
                         }
 
                         MapToolClient.addZone(asset.getId());
                     } catch (IOException ioe) {
                         MapToolClient.showError("Could not load image: " + ioe);
                         return;
                     }
                 }
 
             });
         }
     };
 
     public static final Action TOGGLE_ASSET_PANEL = new ClientAction() {
         
         {
             putValue(Action.NAME, "Toggle Asset Panel");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
         }
         
         /* (non-Javadoc)
          * @see net.rptools.maptool.client.ClientActions.ClientAction#execute(java.awt.event.ActionEvent)
          */
         public void execute(ActionEvent e) {
 
             MapToolClient.toggleAssetTree();
         }
     };
     
     public static final Action ADD_ASSET_PANEL = new ClientAction() {
         
         {
             putValue(Action.NAME, "Add Asset Panel");
             putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
         }
         
         public void execute(ActionEvent e) {
             
             runBackground(new Runnable() {
                 
                 public void run() {
 
                     JFileChooser chooser = MapToolClient.getLoadFileChooser();
                     chooser.setDialogTitle("Load Asset Tree");
                     chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
                     if (chooser.showOpenDialog(MapToolClient.getInstance()) != JFileChooser.APPROVE_OPTION) {
                         return;
                     }
                     
                     MapToolClient.addAssetTree(new AssetTree(chooser.getSelectedFile()));
                 }
                 
             });
         }
     };
     
     public static final Action EXIT = new ClientAction () {
     	
         {
             putValue(Action.NAME, "Exit");
         }
     	
         public void execute(ActionEvent ae) {
         
         	System.exit(0);
         }
     };
     
     private static abstract class ClientAction extends AbstractAction {
 
         public final void actionPerformed(ActionEvent e) {
 
             execute(e);
         }
 
         public abstract void execute(ActionEvent e);
 
         public void runBackground(Runnable r) {
 
             new Thread(r).start();
         }
     }
 }
