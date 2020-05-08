 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue.terminal;
 
 import controller.terminal.controller.ReturnSummary;
 import controller.terminal.controller.TerminalReturnSummaryController;
 import controller.terminal.interfacesGUI.TerminalReturnSummary;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class TerminalReturnSummaryPanel extends AbstractTerminalPanel implements TerminalReturnSummary {
 
 	private TerminalReturnSummaryBikesPanel bikeSummaryPanels;
 	private JTextField txtGuarantee;
 	private JTextField txtSupplementAmount;
 	private JButton btnConfirm;
 
 	public TerminalReturnSummaryPanel(LayoutManager lm, boolean bln) {
 		super(lm, bln);
 		initialize();
 	}
 
 	public TerminalReturnSummaryPanel(LayoutManager lm) {
 		super(lm);
 		initialize();
 	}
 
 	public TerminalReturnSummaryPanel(boolean bln) {
 		super(bln);
 		initialize();
 	}
 
 	public TerminalReturnSummaryPanel() {
 		initialize();
 	}
 
 	private void initialize() {
 
 		GridBagLayout gbl;
 		GridBagConstraints gbc;
 
 		{//Content
 			gbl = new GridBagLayout();
 			{
				gbl.columnWidths = new int[]{0, 0, 0, 0};
 				gbl.rowHeights = new int[]{0, TerminalMainFrame.ROW_HEIGHT, TerminalMainFrame.ROW_HEIGHT, 0};
 				gbl.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
 				gbl.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
 			}
 			this.getPanelContent().setLayout(gbl);
 
 			JScrollPane scrollPane = new JScrollPane();
 			{
 				this.bikeSummaryPanels = new TerminalReturnSummaryBikesPanel();
 				scrollPane.setViewportView(this.bikeSummaryPanels);
 				{//Position
 					gbc = new GridBagConstraints();
 					gbc.gridwidth = 3;
 					gbc.insets = new Insets(0, 0, TerminalMainFrame.VERTICAL_GAP, 0);
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.gridx = 0;
 					gbc.gridy = 0;
 				}
 			}
 			this.getPanelContent().add(scrollPane, gbc);
 
 			JLabel lblTotalAmount = new JLabel("Supplement à payer");
 			{//Position
 				gbc = new GridBagConstraints();
 				gbc.anchor = GridBagConstraints.EAST;
				gbc.fill = GridBagConstraints.VERTICAL;
 				gbc.insets = new Insets(0, 0, TerminalMainFrame.VERTICAL_GAP, TerminalMainFrame.HORIZONTAL_GAP);
 				gbc.gridx = 1;
 				gbc.gridy = 1;
 			}
 			this.getPanelContent().add(lblTotalAmount, gbc);
 
 			this.txtSupplementAmount = new JTextField();
 			{
 				this.txtSupplementAmount.setEnabled(false);
 				this.txtSupplementAmount.setColumns(10);
 				{//Position
 					gbc = new GridBagConstraints();
 					gbc.insets = new Insets(0, 0, TerminalMainFrame.VERTICAL_GAP, TerminalMainFrame.HORIZONTAL_GAP);
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.gridx = 2;
 					gbc.gridy = 1;
 				}
 			}
 			this.getPanelContent().add(this.txtSupplementAmount, gbc);
 
 			JLabel lblGuarantee = new JLabel("Garantie (restitu\u00E9e)");
 			{//Position
 				gbc = new GridBagConstraints();
 				gbc.anchor = GridBagConstraints.EAST;
				gbc.fill = GridBagConstraints.VERTICAL;
 				gbc.insets = new Insets(0, 0, 0, TerminalMainFrame.HORIZONTAL_GAP);
 				gbc.gridx = 1;
 				gbc.gridy = 2;
 			}
 			this.getPanelContent().add(lblGuarantee, gbc);
 
 			this.txtGuarantee = new JTextField();
 			{
 				this.txtGuarantee.setEnabled(false);
 				this.txtGuarantee.setColumns(10);
 				{//Position
 					gbc = new GridBagConstraints();
 					gbc.insets = new Insets(0, 0, 0, TerminalMainFrame.HORIZONTAL_GAP);
 					gbc.fill = GridBagConstraints.BOTH;
 					gbc.gridx = 2;
 					gbc.gridy = 2;
 				}
 			}
 			this.getPanelContent().add(this.txtGuarantee, gbc);
 		}
 
 		{//Actions
 			this.getPanelActions().setLayout(new GridLayout(1, 1));
 
 			this.btnConfirm = new JButton("Ok");
 			{
 				this.btnConfirm.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						TerminalReturnSummaryController.getTerminalReturnSummaryController().doConfirm();
 					}
 				});
 			}
 			this.getPanelActions().add(this.btnConfirm);
 		}
 	}
 
 	@Override
 	public void init() {
 		ReturnSummary summary = TerminalReturnSummaryController.getTerminalReturnSummaryController().getReturnSummary();
 		this.bikeSummaryPanels.setSummary(summary);
 		this.txtGuarantee.setText("" + summary.guaranteeAmount());
 		this.txtSupplementAmount.setText("" + summary.supplementAmount());
 	}
 }
