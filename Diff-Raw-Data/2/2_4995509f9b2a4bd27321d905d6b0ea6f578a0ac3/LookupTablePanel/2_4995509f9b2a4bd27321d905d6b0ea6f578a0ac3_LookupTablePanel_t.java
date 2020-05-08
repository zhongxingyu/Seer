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
 package net.rptools.maptool.client.ui.lookuptable;
 
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import net.rptools.lib.swing.ImagePanel;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.swing.AbeillePanel;
 import net.rptools.maptool.model.LookupTable;
 
 public class LookupTablePanel extends AbeillePanel {
 
 	private ImagePanel imagePanel;
 	private JDialog editorDialog;
 	private EditLookupTablePanel editorPanel;
 	
 	public LookupTablePanel() {
 		super("net/rptools/maptool/client/ui/forms/lookupTablePanel.jfrm");
 		
 		panelInit();
 	}
 	
 	public void updateView() {
 		
 		getButtonPanel().setVisible(MapTool.getPlayer().isGM());
 		revalidate();
 		repaint();
 	}
 
 	public JDialog getEditorDialog() {
 		if (editorDialog == null) {
 			editorDialog = new JDialog(MapTool.getFrame(), true);
 			editorDialog.setSize(500, 400);
 			editorDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 
 			editorDialog.add(editorPanel);
 			
 			SwingUtil.centerOver(editorDialog, MapTool.getFrame());
 		}
 		
 		return editorDialog;
 	}
 	
 	public void initImagePanel() {
 		imagePanel = new ImagePanel();
 		imagePanel.setBackground(Color.white);
 		imagePanel.setModel(new LookupTableImagePanelModel(this));
 		imagePanel.setSelectionMode(ImagePanel.SelectionMode.SINGLE);
 		imagePanel.addMouseListener(new MouseAdapter() { 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				
 				if (e.getClickCount() == 2) {
 					
 					List<Object> ids = getImagePanel().getSelectedIds();
 					if (ids == null || ids.size() == 0) {
 						return;
 					}
 					
 					LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get((String)ids.get(0));
 					if (lookupTable == null) {
 						return;
 					}
 					
 					  MapTool.getFrame().getCommandPanel().getCommandTextArea().setText("/tbl \"" + lookupTable.getName() + "\"");
 					  MapTool.getFrame().getCommandPanel().commitCommand();
 				}
 			}
 		});
 		
 		replaceComponent("mainForm", "imagePanel", new JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
 	}
 	
 	public JPanel getButtonPanel() {
 		return (JPanel) getComponent("buttonPanel");
 	}
 	
 	public void initEditorPanel() {
 		editorPanel = new EditLookupTablePanel();
 	}
 	
 	public JButton getNewButton() {
 		return (JButton) getComponent("newButton");
 	}
 	
 	public JButton getEditButton() {
 		return (JButton) getComponent("editButton");
 	}
 	
 	public JButton getDeleteButton() {
 		return (JButton) getComponent("deleteButton");
 	}
 	
 	public JButton getDuplicateButton() {
 		return (JButton) getComponent("duplicateButton");
 	}
 	
 	public JButton getRunButton() {
 		return (JButton) getComponent("runButton");
 	}
 	
 	public ImagePanel getImagePanel() {
 		return imagePanel;
 	}
 	
 	public void initDuplicateButton() {
 		getDuplicateButton().setMargin(new Insets(0, 0, 0, 0));
 		getDuplicateButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				List<Object> ids = getImagePanel().getSelectedIds();
 				if (ids == null || ids.size() == 0) {
 					return;
 				}
 				
 				LookupTable lookupTable = new LookupTable(MapTool.getCampaign().getLookupTableMap().get((String)ids.get(0)));
				lookupTable.setName("Copy of " + lookupTable.getName());
 				
 				editorPanel.attach(lookupTable);
 				
 				getEditorDialog().setTitle("New Table");
 				getEditorDialog().setVisible(true);
 				
 				imagePanel.clearSelection();
 				repaint();
 			}
 		});
 	}
 	
 	public void initEditTableButton() {
 		getEditButton().setMargin(new Insets(0, 0, 0, 0));
 		getEditButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				List<Object> ids = getImagePanel().getSelectedIds();
 				if (ids == null || ids.size() == 0) {
 					return;
 				}
 				
 				LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get((String)ids.get(0));
 				
 				editorPanel.attach(lookupTable);
 				
 				getEditorDialog().setTitle("Edit Table");
 				getEditorDialog().setVisible(true);
 				
 			}
 		});
 	}
 
 	public void initNewTableButton() {
 		getNewButton().setMargin(new Insets(0, 0, 0, 0));
 		getNewButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				editorPanel.attach(null);
 				
 				getEditorDialog().setTitle("New Table");
 				getEditorDialog().setVisible(true);
 				
 				imagePanel.clearSelection();
 				repaint();
 			}
 		});
 	}
 
 	public void initDeleteTableButton() {
 		getDeleteButton().setMargin(new Insets(0, 0, 0, 0));
 		getDeleteButton().addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				List<Object> ids = getImagePanel().getSelectedIds();
 				if (ids == null || ids.size() == 0) {
 					return;
 				}
 				
 				LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get((String)ids.get(0));
 
 				if (MapTool.confirm("Delete table '" + lookupTable.getName() + "'")) {
 					MapTool.getCampaign().getLookupTableMap().remove(lookupTable.getName());
 					MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
 					
 					imagePanel.clearSelection();
 					repaint();
 				}
 				
 			}
 		});
 	}
 
 
 }
