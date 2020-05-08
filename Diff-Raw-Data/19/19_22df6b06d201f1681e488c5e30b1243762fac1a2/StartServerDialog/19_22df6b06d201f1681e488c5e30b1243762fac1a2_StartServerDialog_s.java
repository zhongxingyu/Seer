 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.rptools.maptool.client.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.text.DecimalFormat;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 
 import net.rptools.lib.service.EchoServer;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolRegistry;
 import net.rptools.maptool.client.swing.AbeillePanel;
 import net.rptools.maptool.client.swing.GenericDialog;
 import net.rptools.maptool.client.walker.WalkerMetric;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.util.StringUtil;
 import net.rptools.maptool.util.UPnPUtil;
 import yasb.Binder;
 
 import com.caucho.hessian.client.HessianRuntimeException;
 /**
  * @author trevor
  */
 public class StartServerDialog extends AbeillePanel<StartServerDialogPreferences> {
 
 	private boolean accepted;
 
 	private StartServerDialogPreferences prefs;
 	private GenericDialog dialog;
 	private JComboBox movementMetricCombo;
 	private JCheckBox useIndividualFOW;
 	private JCheckBox useIndividualViews;
 	private JCheckBox autoRevealOnMovement;
 	private JCheckBox playersCanRevealVision;
 
 	public StartServerDialog() {
 		super("net/rptools/maptool/client/ui/forms/startServerDialog.xml");
 		panelInit();
 	}
 
 	public boolean accepted() {
 		return accepted;
 	}
 
 	public void showDialog() {
 		dialog = new GenericDialog("Start Server", MapTool.getFrame(), this);
 		prefs = new StartServerDialogPreferences();
 
 		bind(prefs);
 		useIndividualFOW = (JCheckBox) getComponent("@useIndividualFOW");
 		useIndividualViews = (JCheckBox) getComponent("@useIndividualViews");
 		autoRevealOnMovement = (JCheckBox) getComponent("@autoRevealOnMovement");
 		playersCanRevealVision = (JCheckBox) getComponent("@playersCanRevealVision");
 
 		useIndividualFOW.setEnabled(prefs.getUseIndividualViews());
 		useIndividualViews.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (!useIndividualViews.isSelected()) {
 					useIndividualFOW.setSelected(false);
 					useIndividualFOW.setEnabled(false);
 				} else {
 					useIndividualFOW.setEnabled(true);
 				}
 			}
 		});
 
		autoRevealOnMovement.setEnabled(prefs.isAutoRevealOnMovement());
 		playersCanRevealVision.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (!playersCanRevealVision.isSelected()) {
 					autoRevealOnMovement.setSelected(false);
 					autoRevealOnMovement.setEnabled(false);
 				} else {
 					autoRevealOnMovement.setEnabled(true);
 				}
 			}
 		});
 
 		movementMetricCombo = (JComboBox) getComponent("movementMetric");
 		DefaultComboBoxModel movementMetricModel = new DefaultComboBoxModel();
 		movementMetricModel.addElement(WalkerMetric.ONE_TWO_ONE);
 		movementMetricModel.addElement(WalkerMetric.ONE_ONE_ONE);
 		movementMetricModel.addElement(WalkerMetric.MANHATTAN);
 		movementMetricModel.addElement(WalkerMetric.NO_DIAGONALS);
 		movementMetricModel.setSelectedItem(AppPreferences.getMovementMetric());
 
 		movementMetricCombo.setModel(movementMetricModel);
 		movementMetricCombo.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				prefs.setMovementMetric((WalkerMetric) movementMetricCombo.getSelectedItem());
 			}
 		});
 		getRootPane().setDefaultButton(getOKButton());
 		dialog.showDialog();
 	}
 
 	public JTextField getPortTextField() {
 		return (JTextField) getComponent("@port");
 	}
 
 	public JTextField getUsernameTextField() {
 		return (JTextField) getComponent("@username");
 	}
 
 	public JButton getOKButton() {
 		return (JButton) getComponent("okButton");
 	}
 
 	public JButton getCancelButton() {
 		return (JButton) getComponent("cancelButton");
 	}
 
 	public JComboBox getRoleCombo() {
 		return (JComboBox) getComponent("@role");
 	}
 
 	public JButton getNetworkingHelpButton() {
 		return (JButton) getComponent("networkingHelpButton");
 	}
 
 	public JCheckBox getUseUPnPCheckbox() {
 		return (JCheckBox) getComponent("@useUPnP");
 	}
 
 	public JCheckBox getUseTooltipForRolls() {
 		return (JCheckBox) getComponent("@useToolTipsForUnformattedRolls");
 	}
 
 	public JComboBox getMovementMetric() {
 		return (JComboBox) getComponent("movementMetric");
 	}
 
 	@Override
 	protected void preModelBind() {
 		Binder.setFormat(getPortTextField(), new DecimalFormat("####"));
 	}
 
 	public void initOKButton() {
 		getOKButton().addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent e) {
 				if (getPortTextField().getText().length() == 0) {
 					MapTool.showError("Must supply a port");
 					return;
 				}
 				try {
 					Integer.parseInt(getPortTextField().getText());
 				} catch (NumberFormatException nfe) {
 					MapTool.showError("Port must be numeric");
 					return;
 				}
 				if (StringUtil.isEmpty(getUsernameTextField().getText())) {
 					MapTool.showError("Must supply a username");
 					return;
 				}
 				if (commit()) {
 					prefs.setMovementMetric((WalkerMetric) movementMetricCombo.getSelectedItem());
 					prefs.setAutoRevealOnMovement(autoRevealOnMovement.isSelected());
 					accepted = true;
 					dialog.closeDialog();
 				}
 			}
 		});
 	}
 
 	public void initCancelButton() {
 		getCancelButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				accepted = false;
 				dialog.closeDialog();
 			}
 		});
 	}
 
 	public void initTestConnectionButton() {
 		getNetworkingHelpButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				// We don't have a good, server-side way of testing any more.
 				boolean ok;
 				ok = MapTool.confirm("msg.info.server.networkingHelp");
 				if (ok)
 					MapTool.showDocument(I18N.getString("msg.info.server.forumNFAQ_URL"));
 			}
 			public void actionPerformed_original(ActionEvent e) {
 				dialog.setVisible(false);			// FJE Added modal dialog to TestConnection button
 				final StaticMessageDialog smdSettingUp = new StaticMessageDialog("Setting Up For Connection Test...");
 				final StaticMessageDialog smdTesting = new StaticMessageDialog("Performing connection test.  Success is usually quick; failure often takes longer...");
 				MapTool.getFrame().showFilledGlassPane(smdSettingUp);
 				new Thread(new Runnable() {
 					public void run() {
 						EchoServer server = null;
 						int port;
 						try {
 							port = Integer.parseInt(getPortTextField().getText());
 						} catch (NumberFormatException nfe) {
 							MapTool.showError("Port must be a number.");
 							return;
 						}
 						try {
 							// Create a temporary server that will listen on the port we
 							// want to start MapTool on.  This provides two things: First
 							// it tells us we can open that port, second it creates a way
 							// for the connection test service to call back and verify it is
 							// the type of service we want.
 							// LATER: Extend EchoServer to do something more than just parrot the input
 							server = new EchoServer(port);
 							server.start();
 
 							if (getUseUPnPCheckbox().isSelected()) {
 								UPnPUtil.openPort(port);
 							}
 							MapTool.getFrame().hideGlassPane();
 							MapTool.getFrame().showFilledGlassPane(smdTesting);
 							if (MapToolRegistry.testConnection(port)) {
 								MapTool.showInformation("Success!  I successfully connected to your computer from the Internet.");
 							} else {
 								MapTool.showError("Could not see your computer from the Internet.<br><br>It could be a port forwarding issue.  Visit the RPTools forum (<b>Tools -> MapTool -> HOWTO</b>) to find the Networking FAQ.");
 							}
 						} catch (ConnectException e) {
 							MapTool.showError("Unable to see your computer from the Internet.<br><br>It could be a port forwarding issue.  Visit the RPTools forum (<b>Tools -> MapTool -> HOWTO</b>) to find the Networking FAQ.");
 						} catch (HessianRuntimeException e) {
 							MapTool.showError("Communication error during test...", e);
 						} catch (IOException e) {
 							MapTool.showError("Unknown or unexpected exception during test.", e);
 						} finally {
 							if (getUseUPnPCheckbox().isSelected()) {
 								UPnPUtil.closePort(port);
 							}
 							MapTool.getFrame().hideGlassPane();
 							dialog.setVisible(true);
 							// Need to make sure it dies so that it doesn't keep the port open ...
 							// we're going to need it very soon !
 							if (server != null) {
 								server.stop();
 							}
 						}
 					}
 				}).start();
 			}
 		});
 	}
 }
