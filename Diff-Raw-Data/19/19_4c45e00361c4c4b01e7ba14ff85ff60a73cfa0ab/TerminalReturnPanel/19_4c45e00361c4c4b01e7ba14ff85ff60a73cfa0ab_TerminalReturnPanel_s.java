 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue.terminal;
 
 import controller.terminal.controller.TerminalReturnController;
 import controller.terminal.interfacesGUI.TerminalReturn;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.HashSet;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class TerminalReturnPanel extends AbstractTerminalPanel implements TerminalReturn {
 
 	//Content
 	JComboBox cboNbBikes;
 	TerminalReturnBikesPanel panelBikes;
 	//Actions
 	private JButton btnReturn;
 
 
 	public TerminalReturnPanel(LayoutManager lm, boolean bln) {
 		super(lm, bln);
 		initialize();
 	}
 
 	public TerminalReturnPanel(LayoutManager lm) {
 		super(lm);
 		initialize();
 	}
 
 	public TerminalReturnPanel(boolean bln) {
 		super(bln);
 		initialize();
 	}
 
 	public TerminalReturnPanel() {
 		initialize();
 	}
 
 	private void initialize() {
 		GridBagConstraints gbc;
 
 		{//content
 			GridBagLayout gbl = new GridBagLayout();
 			{
 				gbl.columnWidths = new int[]{0, 0, 0};
				gbl.rowHeights = new int[]{32, 0, 0};
 				gbl.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 				gbl.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 			}
 			this.getPanelContent().setLayout(gbl);
 
 			JLabel lblNombreDeVelos = new JLabel("Nombre de velos a rendre");
 			{//Position
 				gbc = new GridBagConstraints();
				gbc.insets = new Insets(0, 0, 5, 5);
 				gbc.anchor = GridBagConstraints.WEST;
 				gbc.gridx = 0;
 				gbc.gridy = 0;
 			}
 			this.getPanelContent().add(lblNombreDeVelos, gbc);
 
 			this.cboNbBikes = new JComboBox();
 			{
 				this.cboNbBikes.addItemListener(new ItemListener() {
 					@Override
 					public void itemStateChanged(ItemEvent ie) {
 						panelBikes.setBikeQuantity(((Integer) cboNbBikes.getSelectedItem()).intValue());
 					}
 				});
 				{//Position
 					gbc = new GridBagConstraints();
					gbc.insets = new Insets(0, 0, 5, 0);
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.gridx = 1;
 					gbc.gridy = 0;
 				}
 			}
			add(this.cboNbBikes, gbc);
 
 			JScrollPane scrollPane = new JScrollPane();
 			{
 				this.panelBikes = new TerminalReturnBikesPanel();
 				//The view port contains the panel which allows to handle bikes
 				scrollPane.setViewportView(this.panelBikes);
 
 				{//Position
 					gbc = new GridBagConstraints();
 					gbc.gridwidth = 2;
					gbc.insets = new Insets(0, 0, 0, 5);
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.gridx = 0;
 					gbc.gridy = 1;
 				}
 			}
 			this.getPanelContent().add(scrollPane, gbc);
 		}
 
 		{//Actions
 			this.getPanelActions().setLayout(new GridLayout(1, 1));
 
 			this.btnReturn = new JButton("Rendre");
 			{
 				this.btnReturn.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						//TODO : Construct the list of bike serial numbers
 						TerminalReturnController.getTerminalReturnController().doReturn(new HashSet<Integer>());
 					}
 				});
 			}
 			this.getPanelActions().add(this.btnReturn);
 		}
 	}
 
 	@Override
 	public void init() {
 		this.cboNbBikes.removeAllItems();
 		for (int i = 1; i <= TerminalReturnController.getTerminalReturnController().getMaxAvailableStorages(); i++) {
 			this.cboNbBikes.addItem(new Integer(i));
 		}
 	}
 }
