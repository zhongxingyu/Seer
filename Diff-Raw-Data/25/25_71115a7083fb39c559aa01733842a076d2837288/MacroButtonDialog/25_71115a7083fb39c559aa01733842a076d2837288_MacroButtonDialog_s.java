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
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import com.jeta.forms.components.panel.FormPanel;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolUtil;
 import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
 import net.rptools.maptool.model.MacroButtonProperties;
 import net.rptools.maptool.model.Token;
 
 public class MacroButtonDialog extends JDialog {
 
 	FormPanel panel;
 	MacroButton button;
 	MacroButtonProperties properties;
 	boolean isTokenMacro = false;
 	int oldHashCode = 0;
 	Boolean startingCompareGroup;
 	Boolean startingCompareSortPrefix;
 	Boolean startingCompareCommand;
 	Boolean startingCompareIncludeLabel;
 	Boolean startingCompareAutoExecute;
 	Boolean startingCompareApplyToSelectedTokens;
 	Boolean startingAllowPlayerEdits;
 	
 	public MacroButtonDialog() {
 		
 		super (MapTool.getFrame(), "", true);
 		panel = new FormPanel("net/rptools/maptool/client/ui/forms/macroButtonDialog.jfrm");
 		setContentPane(panel);
 
 		installOKButton();
 		installCancelButton();
 		installHotKeyCombo();
 		installColorCombo();
 		installFontColorCombo();
 		installFontSizeCombo();
 		
 		panel.getCheckBox("applyToTokensCheckBox").setEnabled(!isTokenMacro);
 		panel.getComboBox("hotKey").setEnabled(!isTokenMacro);
 		panel.getTextField("maxWidth").setEnabled(false); // can't get max-width to work, so temporarily disabling it.
 		panel.getCheckBox("allowPlayerEditsCheckBox").setEnabled(MapTool.getPlayer().isGM());
 		
 		pack();
 	}
 	
 	private void installHotKeyCombo() {
 		String[] hotkeys = MacroButtonHotKeyManager.HOTKEYS;
 		JComboBox combo = panel.getComboBox("hotKey");
 		for( int i = 0; i < hotkeys.length; i++ )
 			combo.insertItemAt(hotkeys[i], i);		
 	}
 	
 	private void installColorCombo() { 
 		JComboBox combo = panel.getComboBox("colorComboBox");
 		combo.setModel(new DefaultComboBoxModel(MapToolUtil.getColorNames().toArray()));
 		combo.insertItemAt("default", 0);
 	}
 
 	private void installFontColorCombo() { 
 		JComboBox combo = panel.getComboBox("fontColorComboBox");
 		combo.setModel(new DefaultComboBoxModel(MacroButtonProperties.getFontColors()));
 		combo.insertItemAt("default", 0);
 	}
 
 	private void installFontSizeCombo() { 
 		String[] fontSizes = { "0.75em", "0.80em", "0.85em", "0.90em", "0.95em", "1.00em", "1.05em", "1.10em", "1.15em", "1.20em", "1.25em"};
 		JComboBox combo = panel.getComboBox("fontSizeComboBox");
 		combo.setModel(new DefaultComboBoxModel(fontSizes));
 	}
 
 	private void installOKButton() {
 		JButton button = (JButton) panel.getButton("okButton");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				save();
 			}
 		});
 		getRootPane().setDefaultButton(button);
 	}
 	
 	private void installCancelButton() {
 		JButton button = (JButton) panel.getButton("cancelButton");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cancel();
 			}
 		});
 	}
 	
 	@Override
 	public void setVisible(boolean b) {
 		if (b) {
 			SwingUtil.centerOver(this, MapTool.getFrame());
 		}
 		super.setVisible(b);
 	}
 	
 	public void show(MacroButton button) {
 		this.button = button;
 		this.isTokenMacro = button.getToken() == null ? false : true;
 		this.properties = button.getProperties();
 		oldHashCode = properties.hashCodeForComparison();
 		if(properties != null) {
 			Boolean playerCanEdit = !MapTool.getPlayer().isGM() && properties.getAllowPlayerEdits();
 			Boolean onGlobalPanel = properties.getSaveLocation().equals("Global");
 			Boolean allowEdits = onGlobalPanel || MapTool.getPlayer().isGM() || playerCanEdit;
 			Boolean isCommonMacro = button.getPanelClass().equals("SelectionPanel") && MapTool.getFrame().getSelectionPanel().getCommonMacros().contains(properties);
 			if(allowEdits) {
 				this.setTitle("Macro ID: " + Integer.toString(this.properties.hashCodeForComparison()));
 				
 				getColorComboBox().setSelectedItem(properties.getColorKey());
 				getHotKeyCombo().setSelectedItem(properties.getHotKey());
 				getLabelTextField().setText(properties.getLabel());
 				getGroupTextField().setText(properties.getGroup());
 				getSortbyTextField().setText(properties.getSortby());
 				getCommandTextArea().setText(properties.getCommand());
 				getAutoExecuteCheckBox().setSelected(properties.getAutoExecute());
 				getIncludeLabelCheckBox().setSelected(properties.getIncludeLabel());
 				getApplyToTokensCheckBox().setSelected(properties.getApplyToTokens());
 				getFontColorComboBox().setSelectedItem(properties.getFontColorKey());
 				getFontSizeComboBox().setSelectedItem(properties.getFontSize());
 				getMinWidthTextField().setText(properties.getMinWidth());
 				getMaxWidthTextField().setText(properties.getMaxWidth());
 				getCompareGroupCheckBox().setSelected(properties.getCompareGroup());
 				getCompareSortPrefixCheckBox().setSelected(properties.getCompareSortPrefix());
 				getCompareCommandCheckBox().setSelected(properties.getCompareCommand());
 				getCompareIncludeLabelCheckBox().setSelected(properties.getCompareIncludeLabel());
 				getCompareAutoExecuteCheckBox().setSelected(properties.getCompareAutoExecute());
 				getCompareApplyToSelectedTokensCheckBox().setSelected(properties.getCompareApplyToSelectedTokens());
 				getAllowPlayerEditsCheckBox().setSelected(properties.getAllowPlayerEdits());
 				
 				if(isCommonMacro) {
 					getColorComboBox().setEnabled(false);
 					getHotKeyCombo().setEnabled(false);
 					getGroupTextField().setEnabled(properties.getCompareGroup());
 					getSortbyTextField().setEnabled(properties.getCompareSortPrefix());
 					getCommandTextArea().setEnabled(properties.getCompareCommand());
 					getAutoExecuteCheckBox().setEnabled(properties.getCompareAutoExecute());
 					getIncludeLabelCheckBox().setEnabled(properties.getCompareIncludeLabel());
 					getApplyToTokensCheckBox().setEnabled(properties.getCompareApplyToSelectedTokens());
 					getFontColorComboBox().setEnabled(false);
 					getFontSizeComboBox().setEnabled(false);
 					getMinWidthTextField().setEnabled(false);
 					getMaxWidthTextField().setEnabled(false);
 				}
 				
 				startingCompareGroup = properties.getCompareGroup();
 				startingCompareSortPrefix = properties.getCompareSortPrefix();
 				startingCompareCommand = properties.getCompareCommand();
 				startingCompareAutoExecute = properties.getCompareAutoExecute();
 				startingCompareIncludeLabel = properties.getCompareIncludeLabel();
 				startingCompareApplyToSelectedTokens = properties.getCompareApplyToSelectedTokens();
 				startingAllowPlayerEdits = properties.getAllowPlayerEdits();
 				
 				setVisible(true);
 			} else {
 				MapTool.showWarning("The GM has not allowed players to change this macro!");
 			}
 		} else {
 			MapTool.showError("Button properties are null.");
 		}
 	}
 	
 	private void save() {
 		
 		String hotKey = getHotKeyCombo().getSelectedItem().toString();
 		button.getHotKeyManager().assignKeyStroke(hotKey);
 		button.setColor(getColorComboBox().getSelectedItem().toString());
 		button.setText(this.button.getButtonText());
 		properties.setHotKey(hotKey);
 		properties.setColorKey(getColorComboBox().getSelectedItem().toString());
 		properties.setLabel(getLabelTextField().getText());
 		properties.setGroup(getGroupTextField().getText());
 		properties.setSortby(getSortbyTextField().getText());
 		properties.setCommand(getCommandTextArea().getText());
 		properties.setAutoExecute(getAutoExecuteCheckBox().isSelected());
 		properties.setIncludeLabel(getIncludeLabelCheckBox().isSelected());
 		properties.setApplyToTokens(getApplyToTokensCheckBox().isSelected());
 		properties.setFontColorKey(getFontColorComboBox().getSelectedItem().toString());
 		properties.setFontSize(getFontSizeComboBox().getSelectedItem().toString());
 		properties.setMinWidth(getMinWidthTextField().getText());
 		properties.setMaxWidth(getMaxWidthTextField().getText());
 		properties.setCompareGroup(getCompareGroupCheckBox().isSelected());
 		properties.setCompareSortPrefix(getCompareSortPrefixCheckBox().isSelected());
 		properties.setCompareCommand(getCompareCommandCheckBox().isSelected());
 		properties.setCompareIncludeLabel(getCompareIncludeLabelCheckBox().isSelected());
 		properties.setCompareAutoExecute(getCompareAutoExecuteCheckBox().isSelected());
 		properties.setCompareApplyToSelectedTokens(getCompareApplyToSelectedTokensCheckBox().isSelected());
 		properties.setAllowPlayerEdits(getAllowPlayerEditsCheckBox().isSelected());
 		properties.save();
 		
 		if(button.getPanelClass().equals("SelectionPanel")) {
 			if(MapTool.getFrame().getSelectionPanel().getCommonMacros().contains(button.getProperties())) {
 				Boolean changeAllowPlayerEdits = false;
 				Boolean endingAllowPlayerEdits = false;
 				if(startingAllowPlayerEdits) {
 					if(!properties.getAllowPlayerEdits()) {
 						Boolean confirmDisallowPlayerEdits = MapTool.confirm("<html><body>Are you " +
 								"sure you wish to prevent players from editing any macro common to " +
 								"this one?<br><br>Select \"Yes\" to continue with the change.  " +
 								"Select \"No\" to revert.</body></html>");
 						if(confirmDisallowPlayerEdits) {
 							changeAllowPlayerEdits = true;
 							endingAllowPlayerEdits = false;
 						} else {
 							properties.setAllowPlayerEdits(true);
 						}
 					}
 				} else {
 					if(properties.getAllowPlayerEdits()) {
 						Boolean confirmAllowPlayerEdits = MapTool.confirm("<html><body>Are you sure " +
 								"you wish to allow players from editing any macro common to this one?" +
 								"<br><br>Select \"Yes\" to continue with the change.  Select \"No\" " +
 								"to revert.</body></html>");
 						if(confirmAllowPlayerEdits) {
 							changeAllowPlayerEdits = true;
 							endingAllowPlayerEdits = true;
 						} else {
 							properties.setAllowPlayerEdits(false);
 						}
 					}
 				}
 				Boolean trusted = true;
 				for(Token nextToken : MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
 					if(AppUtil.playerOwns(nextToken)) {
 						trusted = true;
 					} else {
 						trusted = false;
 					}
 					for(MacroButtonProperties nextMacro : nextToken.getMacroList(trusted)) {
 						if(MapTool.getPlayer().isGM() || (!MapTool.getPlayer().isGM() && nextMacro.getApplyToTokens())) {
 							if(nextMacro.hashCodeForComparison() == oldHashCode) {
 								nextMacro.setLabel(properties.getLabel());
 								if(properties.getCompareGroup() && startingCompareGroup) {
 									nextMacro.setGroup(properties.getGroup());
 								}
 								if(properties.getCompareSortPrefix() && startingCompareSortPrefix) {
 									nextMacro.setSortby(properties.getSortby());
 								}
 								if(properties.getCompareCommand() && startingCompareCommand) {
 									nextMacro.setCommand(properties.getCommand());
 								}
 								if(properties.getCompareAutoExecute() && startingCompareAutoExecute) {
 									nextMacro.setAutoExecute(properties.getAutoExecute());
 								}
 								if(properties.getCompareIncludeLabel() && startingCompareIncludeLabel) {
 									nextMacro.setIncludeLabel(properties.getIncludeLabel());
 								}
 								if(properties.getCompareApplyToSelectedTokens() && startingCompareApplyToSelectedTokens) {
 									nextMacro.setApplyToTokens(properties.getApplyToTokens());
 								}
 								if(changeAllowPlayerEdits) {
 									nextMacro.setAllowPlayerEdits(endingAllowPlayerEdits);
 								}
 								nextMacro.setCompareGroup(properties.getCompareGroup());
 								nextMacro.setCompareSortPrefix(properties.getCompareSortPrefix());
 								nextMacro.setCompareCommand(properties.getCompareCommand());
 								nextMacro.setCompareAutoExecute(properties.getCompareAutoExecute());
 								nextMacro.setCompareIncludeLabel(properties.getCompareIncludeLabel());
 								nextMacro.setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
 								nextMacro.save();
 							}
 						}
 					}				
 				}
 			}
 		}
 		
 		setVisible(false);
 		MapTool.getFrame().getSelectionPanel().reset();
 	}
 	
 	private void cancel() {
 		setVisible(false);
 	}
 
 	private JCheckBox getAutoExecuteCheckBox() {
 		return  panel.getCheckBox("autoExecuteCheckBox");
 	}
 	
 	private JCheckBox getIncludeLabelCheckBox() {
 		return  panel.getCheckBox("includeLabelCheckBox");
 	}
 	
 	private JCheckBox getApplyToTokensCheckBox() {
 		return  panel.getCheckBox("applyToTokensCheckBox");
 	}
 	
 	private JComboBox getHotKeyCombo() {
 		return panel.getComboBox("hotKey");
 	}
 	
 	private JComboBox getColorComboBox() {
 		return panel.getComboBox("colorComboBox");
 	}
 	
 	private JTextField getLabelTextField() {
 		return panel.getTextField("label");
 	}
 	
 	private JTextField getGroupTextField() {
 		return panel.getTextField("group");
 	}
 	
 	private JTextField getSortbyTextField() {
 		return panel.getTextField("sortby");
 	}
 	
 	private JTextArea getCommandTextArea() {
 		return (JTextArea) panel.getTextComponent("command");
 	}
 
 	private JComboBox getFontColorComboBox() {
 		return panel.getComboBox("fontColorComboBox");
 	}
 	
 	private JComboBox getFontSizeComboBox() {
 		return panel.getComboBox("fontSizeComboBox");
 	}
 	
 	private JTextField getMinWidthTextField() {
 		return panel.getTextField("minWidth");
 	}
 	
 	private JTextField getMaxWidthTextField() {
 		return panel.getTextField("maxWidth");
 	}
 	
 	private JCheckBox getAllowPlayerEditsCheckBox() {
 		return panel.getCheckBox("allowPlayerEditsCheckBox");
 	}
 	
 	// Begin comparison customization
 	
 	private JCheckBox getCompareIncludeLabelCheckBox() {
 		return  panel.getCheckBox("commonUseIncludeLabel");
 	}
 	private JCheckBox getCompareAutoExecuteCheckBox() {
 		return  panel.getCheckBox("commonUseAutoExecute");
 	}
 	private JCheckBox getCompareApplyToSelectedTokensCheckBox() {
 		return  panel.getCheckBox("commonUseApplyToSelectedTokens");
 	}
 	private JCheckBox getCompareGroupCheckBox() {
 		return  panel.getCheckBox("commonUseGroup");
 	}
 	private JCheckBox getCompareSortPrefixCheckBox() {
 		return  panel.getCheckBox("commonUseSortPrefix");
 	}
 	private JCheckBox getCompareCommandCheckBox() {
 		return  panel.getCheckBox("commonUseCommand");
 	}
 	
 	// End comparison customization
 }
