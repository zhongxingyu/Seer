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
 package net.rptools.maptool.client.ui.token;
 
 import javax.swing.JButton;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.swing.AbeillePanel;
 import net.rptools.maptool.client.swing.GenericDialog;
 import net.rptools.maptool.client.ui.macrobuttons.buttons.TokenMacroButton;
 import net.rptools.maptool.model.Token;
 
 public class EditTokenMacroDialog extends AbeillePanel {
 	
 	private static final String jfrm = "net/rptools/maptool/client/ui/forms/editTokenMacroDialog.jfrm"; 
 	private TokenMacroButton button;
 	private Token token;
 	private List<Token> tokenList;
 
 	//private boolean macroSaved;
 	private GenericDialog dialog;
 	private String oldMacroName;
 	//private String oldMacroCommand;
 	
 	public EditTokenMacroDialog() {
 		super(jfrm);
 	}
 	
 	public EditTokenMacroDialog(TokenMacroButton button) {
 		super(jfrm);
 		this.button = button;
 	}
 	
 	public EditTokenMacroDialog(Token token) {
 		super(jfrm);
 		this.token = token;
 	}
 
 	public EditTokenMacroDialog(List<Token> tokenList) {
 		super(jfrm);
 		this.tokenList = tokenList;
 	}
 
 	public void showDialog() {
 		dialog = new GenericDialog("Edit Macro", MapTool.getFrame(), this) {
 			@Override
 			public void closeDialog() {
 				// TODO: I don't like this.  There should really be a AbeilleDialog class that does this
 				unbind();
 				super.closeDialog();
 			}
 		};
 
 		//bind(button);
 		panelInit();
 		
 		getRootPane().setDefaultButton(getOKButton());
 		dialog.showDialog();
 	}
 	
 	public JButton getOKButton() {
 		return (JButton) getComponent("OKButton");
 	}
 	
 	public JButton getCancelButton() {
 		return (JButton) getComponent("cancelButton");
 	}
 	
 	public JTextArea getMacroCommandTextArea() {
 		return (JTextArea) getComponent("macroCommand");
 	}
 
 	public JTextField getMacroNameTextField() {
 		return (JTextField) getComponent("macroName");
 	}
 
 	public void initOKButton() {
 		getOKButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (button != null) {
 					updateMacro();
 				} else if (token != null) {
 					addMacro();
 				} else if (tokenList != null) {
 					addMacroToAll();
 				}
 				dialog.closeDialog();
 			}
 		});
 	}
 
 	public void initCancelButton() {
 		getCancelButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//unbind();
 				dialog.closeDialog();
 			}
 		});
 	}
 
 	public void initMacroNameTextField() {
 		JTextField field = getMacroNameTextField();
 		if (button != null) {
 			field.setText(button.getMacro());
 			oldMacroName = button.getMacro();
 		}
 	}
 	
 	public void initMacroCommandTextField() {
 		JTextArea textArea = getMacroCommandTextArea();
 		textArea.setWrapStyleWord(true);
 		textArea.setLineWrap(true);
 
 		if (button != null) {
 			textArea.setText(button.getCommand());
 			//oldMacroCommand = button.getCommand();
 		}
 	}
 	
 	// invoked when called by a token macro button
 	public void updateMacro() {
 		String newMacroName = getMacroNameTextField().getText();
 
		Token token = button.getToken();
		
 		// if we have changed the name of the macro, delete the old macro from token
 		// otherwise the edited macro will be added as a new one.
 		if (!newMacroName.equals(oldMacroName)) {
			token.deleteMacro(oldMacroName);
 		}
 
		token.addMacro(newMacroName, getMacroCommandTextArea().getText());
 		MapTool.getFrame().updateSelectionPanel();

		if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
			MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
		}
 	}
 	
 	public void addMacro() {
 		token.addMacro(getMacroNameTextField().getText(), getMacroCommandTextArea().getText());
 		MapTool.getFrame().updateSelectionPanel();
 		if (MapTool.getFrame().getCommandPanel().getIdentity().equals(token.getName())) {
 			// we are impersonating this token, we have to update the impersonate tab
 			MapTool.getFrame().updateImpersonatePanel(token);
 		}
		if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
			MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
		}
 	}
 	
 	public void addMacroToAll() {
 		for (Token token : tokenList) {
 			token.addMacro(getMacroNameTextField().getText(), getMacroCommandTextArea().getText());
			
			if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
				MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
			}
 		}
 		MapTool.getFrame().updateSelectionPanel();
 		//TODO: here is a bug: if a token in the group is impersonated the impersonate panel doesn't get updated.
 		// add listeners to solve this.
 	}
 }
