 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue.terminal;
 
 import controller.terminal.controller.TerminalRentController;
 import controller.terminal.interfacesGUI.TerminalRent;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.Set;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
import model.database.DataBaseElements;
 import vue.common.ValidityPanel;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class TerminalRentPanel extends AbstractTerminalPanel implements TerminalRent {
 
 	//Maximal amount of bikes that can be selected
 	private static final int ROWHEIGHT = 32;
 	private static final int VERTICAL_GAP = 5;
 	private static final int HORIZONTAL_GAP = 5;
 	//Panel to manage informations
 	private GridBagLayout gblInformations;
 	//First line (number of bikes
 	private JLabel lblNbBikes;
 	private JComboBox cboNbBikes;
 	private ValidityPanel panelNbBikesValid;
 	//Second line (duration)
 	private JLabel lblDuration;
 	private JComboBox cboDuration;
 	private JComboBox cboDurationUnit;
 	private ValidityPanel panelDurationValid;
 	//Panel to fire actions
 	private JButton btnRent;
 
 	public TerminalRentPanel(LayoutManager lm, boolean bln) {
 		super(lm, bln);
 		initialize();
 	}
 
 	public TerminalRentPanel(LayoutManager lm) {
 		super(lm);
 		initialize();
 	}
 
 	public TerminalRentPanel(boolean bln) {
 		super(bln);
 		initialize();
 	}
 
 	public TerminalRentPanel() {
 		initialize();
 	}
 
 	private void initialize() {
 		GridBagConstraints gbc;
 
 		{//Contenu
 			this.gblInformations = new GridBagLayout();
 			{
 				this.gblInformations.columnWidths = new int[]{0, 0, 0, ROWHEIGHT, 0};
 				this.gblInformations.rowHeights = new int[]{ROWHEIGHT, ROWHEIGHT, 0};
 				this.gblInformations.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
 				this.gblInformations.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
 			}
 			this.getPanelContent().setLayout(this.gblInformations);
 
 			{//First line
 				//Nb selector of bikes
 				this.cboNbBikes = new JComboBox();
 				{
 					{
 						this.cboNbBikes.addItemListener(new ItemListener() {
 							@Override
 							public void itemStateChanged(ItemEvent ie) {
 								if (cboNbBikes.getSelectedIndex() == -1) {
 									panelNbBikesValid.setValid(ValidityPanel.NONE);
 								} else {
 									if (TerminalRentController.getTerminalRentController().getMaxAvailableBikes()
 											>= (((Integer) (cboNbBikes.getSelectedItem())).intValue())) {
 										panelNbBikesValid.setValid(ValidityPanel.VALID);
 									} else {
 										panelNbBikesValid.setValid(ValidityPanel.INVALID);
 									}
 								}
 								panelNbBikesValid.repaint();
 							}
 						});
 					}
 					{
 						gbc = new GridBagConstraints();
 						gbc.insets = new Insets(0, 0, VERTICAL_GAP, HORIZONTAL_GAP);
 						gbc.fill = GridBagConstraints.BOTH;
 						gbc.gridwidth = 2;
 						gbc.gridx = 1;
 						gbc.gridy = 0;
 					}
 				}
 				this.getPanelContent().add(this.cboNbBikes, gbc);
 				//Label for nb bikes
 				this.lblNbBikes = new JLabel("Nombre de vélos");
 				{
 					this.lblNbBikes.setLabelFor(this.cboNbBikes);
 					{
 						gbc = new GridBagConstraints();
 						gbc.fill = GridBagConstraints.VERTICAL;
 						gbc.insets = new Insets(0, HORIZONTAL_GAP, VERTICAL_GAP, HORIZONTAL_GAP);
 						gbc.anchor = GridBagConstraints.EAST;
 						gbc.gridx = 0;
 						gbc.gridy = 0;
 					}
 				}
 				this.getPanelContent().add(this.lblNbBikes, gbc);
 				//Validity for nb bikes
 				this.panelNbBikesValid = new ValidityPanel();
 				{
 					gbc = new GridBagConstraints();
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.insets = new Insets(0, 0, VERTICAL_GAP, 0);
 					gbc.gridx = 3;
 					gbc.gridy = 0;
 				}
 				this.getPanelContent().add(this.panelNbBikesValid, gbc);
 			}//First line
 
 			{//Second line
 				//Duration selector
 				this.cboDuration = new JComboBox();
 				{
 					{
 						//TODO : Add model / listener
 					}
 					{
 						gbc = new GridBagConstraints();
 						gbc.insets = new Insets(0, 0, VERTICAL_GAP, HORIZONTAL_GAP);
 						gbc.fill = GridBagConstraints.BOTH;
 						gbc.gridx = 1;
 						gbc.gridy = 1;
 					}
 				}
 				this.getPanelContent().add(this.cboDuration, gbc);
 				//Duration unit selector
 				this.cboDurationUnit = new JComboBox();
 				{
 					{
 						this.cboDurationUnit.addItemListener(new ItemListener() {
 							@Override
 							public void itemStateChanged(ItemEvent ie) {
 								initializePossibleDurations();
 							}
 						});
 					}
 					{
 						gbc = new GridBagConstraints();
 						gbc.insets = new Insets(0, 0, VERTICAL_GAP, HORIZONTAL_GAP);
 						gbc.fill = GridBagConstraints.BOTH;
 						gbc.gridx = 2;
 						gbc.gridy = 1;
 					}
 				}
 				this.getPanelContent().add(this.cboDurationUnit, gbc);
 				//Label for duration selector
 				this.lblDuration = new JLabel("Durée de location");
 				{
 					this.lblDuration.setLabelFor(this.cboDuration);
 					{
 						gbc = new GridBagConstraints();
 						gbc.fill = GridBagConstraints.VERTICAL;
 						gbc.insets = new Insets(0, HORIZONTAL_GAP, VERTICAL_GAP, HORIZONTAL_GAP);
 						gbc.anchor = GridBagConstraints.EAST;
 						gbc.gridx = 0;
 						gbc.gridy = 1;
 					}
 				}
 				this.getPanelContent().add(this.lblDuration, gbc);
 				//Validity for duration (and duration unit)
 				this.panelDurationValid = new ValidityPanel();
 				{
 					gbc = new GridBagConstraints();
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.insets = new Insets(0, 0, VERTICAL_GAP, 0);
 					gbc.gridx = 3;
 					gbc.gridy = 1;
 				}
 				this.getPanelContent().add(this.panelDurationValid, gbc);
 			}//Second line
 		}
 
 		{//Actions
 			this.getPanelActions().setLayout(new GridLayout(1, 1));
 
 			this.btnRent = new JButton("Louer");
 			{
 				this.btnRent.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						TerminalRentController.getTerminalRentController().doRent(
 								((Integer) cboNbBikes.getSelectedItem()).intValue(),
 								((Integer) cboDuration.getSelectedItem()).intValue(),
 								((String) cboDurationUnit.getSelectedItem()).toString());
 					}
 				});
 			}
 			this.getPanelActions().add(this.btnRent);
 			//TODO : Add Sign in button
 		}
 	}
 
 	@Override
 	public void init() {
 		Set<String> durationUnits;
 		//Initialize bike amount
 		this.cboNbBikes.removeAllItems();
 		for (int i = 1; i <= TerminalRentController.getTerminalRentController().getMaxAvailableBikes(); i++) {
 			this.cboNbBikes.addItem(new Integer(i));
 		}
 		//Initialize duration units
 		this.cboDurationUnit.removeAllItems();
 		durationUnits = TerminalRentController.getTerminalRentController().getPossibleDurationUnits();
 		for (String durationUnit : durationUnits) {
 			this.cboDurationUnit.addItem(durationUnit);
 		}
 		//Choose first available duration unit
 		if (this.cboDurationUnit.getItemCount() > 0) {
 			this.cboDurationUnit.setSelectedIndex(0);
 		}
 		initializePossibleDurations();
 	}
 	private void initializePossibleDurations() {
 		Set<Integer> durations;
 		//Initialize available durations
 		this.cboDuration.removeAllItems();
 		if (this.cboDurationUnit.getItemCount() > 0) {
 			durations = TerminalRentController.getTerminalRentController().getPossibleDurations(((String) this.cboDurationUnit.getSelectedItem()));
 			for (Integer duration : durations) {
 				this.cboDuration.addItem(duration);
 			}
 		}
 	}
 }
