 package manager.panel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
 
 import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import manager.KitManager;
 import manager.panel.KitsListPanel.KitSelectHandler;
 import manager.util.OverlayPanel;
 import manager.util.WhiteLabel;
 import Utils.Constants;
import factory.PartType;
 import factory.KitConfig;
 
 /**
  * Prettified KitManagerPanel, powers KitManager. Uses PartsListPanel to
  * display parts.
  * 
  * @author Aaron Harris
  */
 public class KitManagerPanelV2 extends JPanel {
 
 	private JPanel panels;
 	private JPanel leftPanel;
 	private KitsListPanel kitsPanel;
 	private JScrollPane kitsjsp;
 	private PartsListPanel partsPanel;
 	private JScrollPane jsp;
 	
 	private JScrollPane kitPartsJsp;
 	private OverlayPanel rightPanel;
 	private JPanel rightTitlePanel;
 	private PartsListPanel kitPartsPanel;
 	
 	private WhiteLabel rightTitle;
 	private JTextField nameField;
 	private JButton submitButton;
 
 	private KitManager manager;
 
 	private boolean isEditing;
 	private boolean isDeleting;
 	// Stores the selected kitConfig
 	private KitConfig selectedKit;
 
 	public KitManagerPanelV2(KitManager mngr) {
 		manager = mngr;
 
 		setLayout(new BorderLayout());
 		setBorder(Constants.PADDING);
 
 		JLabel title = new WhiteLabel("Kit Manager");
 		title.setFont(new Font("Arial", Font.BOLD, 30));
 		title.setBorder(Constants.VERTICAL_PADDING);
 		add(title, BorderLayout.NORTH);
 
 		panels = new JPanel(new GridLayout(1, 2));
 		panels.setOpaque(false);
 		panels.setVisible(true);
 		add(panels);
 		
 		leftPanel = new JPanel();
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
 		leftPanel.setOpaque(false);
 		leftPanel.setVisible(true);
 		panels.add(leftPanel);
 		
 		// Setup KitsListPanel
 		kitsPanel = new KitsListPanel("Choose Kit to edit",
 				new KitSelectHandler() {
 					@Override
 					public void onKitSelect(KitConfig kc) {
 						startEditing(kc);
 					}
 					
 					public void onKitButton(KitConfig kc) {
 						startDeleting(kc);
 					}
 				}, "Delete");
 
 		kitsPanel.setVisible(true);
 		kitsPanel.setBackground(new Color(0, 0, 0, 30));
 
 		kitsjsp = new JScrollPane(kitsPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		kitsjsp.setOpaque(false);
 		kitsjsp.getViewport().setOpaque(false);
 		kitsjsp.setPreferredSize(new Dimension(379, 477 / 3));
 		leftPanel.add(kitsjsp);
 
 		partsPanel = new PartsListPanel(
 				new PartsListPanel.PartsListPanelHandler() {
 					@Override
 					public void panelClicked(PartType pt) {
 						// Do nothing
 					}
 
 					@Override
 					public void buttonClicked(PartType pt) {
 						addPart(pt);
 					}
 				}, "Add to Kit");
 		partsPanel.setVisible(true);
 		partsPanel.setBackground(new Color(0, 0, 0, 30));
 
 		jsp = new JScrollPane(partsPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		jsp.setOpaque(false);
 		jsp.getViewport().setOpaque(false);
 		leftPanel.add(jsp, BorderLayout.CENTER);
 		
 		rightPanel = new OverlayPanel();
 		rightPanel.setLayout(new BorderLayout());
 		rightPanel.setOpaque(false);
 		rightPanel.setVisible(true);
 		panels.add(rightPanel);
 		
 		rightTitlePanel = new JPanel();
 		rightTitlePanel.setVisible(true);
 		rightTitlePanel.setOpaque(false);
 		rightPanel.add(rightTitlePanel, BorderLayout.NORTH);
 
 		kitPartsPanel = new PartsListPanel(
 				new PartsListPanel.PartsListPanelHandler() {
 					@Override
 					public void panelClicked(PartType pt) {
 						// Do nothing
 					}
 
 					@Override
 					public void buttonClicked(PartType pt) {
 						removePart(pt);
 					}
 				}, "Remove");
 		kitPartsPanel.setVisible(true);
 		kitPartsPanel.setBackground(new Color(0, 0, 0, 30));
 
 		kitPartsJsp = new JScrollPane(kitPartsPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		kitPartsJsp.setOpaque(false);
 		kitPartsJsp.getViewport().setOpaque(false);
 		rightPanel.add(kitPartsJsp, BorderLayout.CENTER);
 
 		setUpRightPanel();
 	}
 
 	public void setUpRightPanel() {
 		rightTitlePanel.removeAll();
 
 		rightTitlePanel.setLayout(new BoxLayout(rightTitlePanel, BoxLayout.PAGE_AXIS));
 		rightTitlePanel.setAlignmentX(LEFT_ALIGNMENT);
 		rightTitlePanel.setBorder(Constants.PADDING);
 
 		rightTitle = new WhiteLabel("Create a New Kit");
 		rightTitle.setFont(new Font("Arial", Font.PLAIN, 16));
 		rightTitle.setLabelSize(300, 40);
 		rightTitle.setAlignmentX(0);
 		rightTitlePanel.add(rightTitle);
 
 		//if (!isEditing && !isDeleting) {
 			JPanel namePanel = new JPanel();
 			namePanel.setBorder(Constants.TOP_PADDING);
 			namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
 			namePanel.setOpaque(false);
 			namePanel.setVisible(true);
 			namePanel.setAlignmentX(0);
 			rightTitlePanel.add(namePanel);
 	
 			WhiteLabel nameLabel = new WhiteLabel("Name");
 			nameLabel.setLabelSize(100, 25);
 			namePanel.add(nameLabel);
 	
 			nameField = new JTextField("name");
 	
 			nameField.setMaximumSize(new Dimension(200, 25));
 			nameField.setBorder(Constants.FIELD_PADDING);
 			namePanel.add(nameField);
 		//}
 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBorder(Constants.TOP_PADDING);
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
 		buttonPanel.setOpaque(false);
 		buttonPanel.setVisible(true);
 		buttonPanel.setAlignmentX(0);
 		rightTitlePanel.add(buttonPanel);
 
 		if (isEditing || isDeleting) {
 			namePanel.setVisible(false);
 			
 			JButton cancelButton = new JButton("Cancel");
 			cancelButton.setMinimumSize(new Dimension(100, 25));
 			cancelButton.setMaximumSize(new Dimension(100, 25));
 			cancelButton.setPreferredSize(new Dimension(100, 25));
 			cancelButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 					// puts the values as they were before
 					restoreRightPanel();
 				}
 			});
 			buttonPanel.add(cancelButton);
 		} else {
 			WhiteLabel fakeLabel = new WhiteLabel("");
 			fakeLabel.setLabelSize(100, 25);
 			buttonPanel.add(fakeLabel);
 		}
 
 		submitButton = new JButton("Submit >");
 		submitButton.setMinimumSize(new Dimension(200, 25));
 		submitButton.setMaximumSize(new Dimension(200, 25));
 		submitButton.setPreferredSize(new Dimension(200, 25));
 		submitButton.setAlignmentX(0);
 		buttonPanel.add(submitButton);
 
 		removeAllActionListener(submitButton);
 		submitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				KitConfig newKit = new KitConfig(nameField.getText(), kitPartsPanel.getItemList());
 				manager.addKit(newKit);
 				restoreRightPanel();
 			}
 		});
 		
 		// Clear out the parts in the list of a Kit's parts
 		kitPartsPanel.updateList(new ArrayList<PartType>());
 		kitPartsJsp.validate();
 		validateSubmit();
 	}
 	
 	/**
 	 * This function is called by KitManager whenever KitConfigs
 	 * are updated.
 	 * 
 	 * @param kc
 	 *            ArrayList of current KitConfigs
 	 */
 	public void updateKitConfig(ArrayList<KitConfig> kc) {
 		// TODO: Why does this prevent KitManager from displaying?
		// kitsPanel.updateList(kc);
 		kitsjsp.validate();
 	}
 
 	public void updatePartTypes(ArrayList<PartType> pt) {
 		if (pt != null) {
 			partsPanel.updateList(pt);
 			jsp.validate();
 		}
 	}
 	
 	public void addPart(PartType pt) {
 		if (pt != null) {
 			ArrayList<PartType> kitPt = kitPartsPanel.getItemList();
 			if (kitPt.size() < 8) {
 				kitPt.add(pt);
 				kitPartsPanel.updateList(kitPt);
 				kitPartsJsp.validate();
 
 			}
 			validateSubmit();
 		}
 	}
 	
 	public void removePart(PartType pt) {
 		if (pt != null) {
 			ArrayList<PartType> kitPt = kitPartsPanel.getItemList();
 			kitPt.remove(pt);
 			kitPartsPanel.updateList(kitPt);
 			kitPartsJsp.validate();
 			validateSubmit();
 		}
 	}
 	
 	public void validateSubmit() {
 		if (kitPartsPanel.getItemList().size() < 4) {
 			submitButton.setEnabled(false);
 		} else if (kitPartsPanel.getItemList().size() >= 4) {
 			submitButton.setEnabled(true);
 		}
 		if (isDeleting) {
 			submitButton.setEnabled(true);
 		}
 	}
 
 	public void startEditing(final KitConfig kc) {
 		isEditing = true;
 		isDeleting = false;
 		setUpRightPanel();
 
 		rightTitle.setText("Editing Kit " + kc.getName());
 		nameField.setText(kc.getName());
 		submitButton.setText("Edit >");
 
 		removeAllActionListener(submitButton);
 		submitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				kc.setName(nameField.getText());
 				kc.setConfig(kitPartsPanel.getItemList());
 
 				manager.editKit(kc);
 				restoreRightPanel();
 			}
 		});
 		
 		kitPartsPanel.updateList(kc.getAllParts());
 		kitPartsJsp.validate();
 		validateSubmit();
 	}
 
 	public void startDeleting(final KitConfig kc) {
 		isEditing = false;
 		isDeleting = true;
 		setUpRightPanel();
 
 		rightTitle.setText("Deleting Kit " + kc.getName());
 		nameField.setEnabled(false);
 		submitButton.setText("Confirm Delete >");
 		submitButton.setEnabled(true);
 
 		removeAllActionListener(submitButton);
 		submitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				manager.deleteKit(kc);
 				restoreRightPanel();
 			}
 		});
 		
 		kitPartsPanel.updateList(kc.getAllParts());
 		kitPartsJsp.validate();
 	}
 
 	public void restoreRightPanel() {
 		isEditing = false;
 		isDeleting = false;
 		setUpRightPanel();
 
 		kitPartsPanel.restoreColors();
 
 		validate();
 	}
 
 	@Override
 	public void paintComponent(Graphics gg) {
 		Graphics2D g = (Graphics2D) gg;
 		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
 	}
 
 	/**
 	 * Removes all action listeners from a button. TODO consider moving this
 	 * out, or starting own JButton subclass.
 	 */
 	public void removeAllActionListener(JButton button) {
 		for (ActionListener al : button.getActionListeners()) {
 			button.removeActionListener(al);
 		}
 	}
 }
