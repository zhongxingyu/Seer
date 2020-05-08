 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue.terminal;
 
 import controller.terminal.controller.TerminalWelcomeController;
 import controller.terminal.interfacesGUI.TerminalWelcome;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class TerminalWelcomePanel extends JPanel implements TerminalWelcome {
 
 	private JPanel panelActions;
 	private JButton btnRent;
 	private JButton btnReturn;
 
 	public TerminalWelcomePanel(LayoutManager lm, boolean bln) {
 		super(lm, bln);
 		initialize();
 	}
 
 	public TerminalWelcomePanel(LayoutManager lm) {
 		super(lm);
 		initialize();
 	}
 
 	public TerminalWelcomePanel(boolean bln) {
 		super(bln);
 		initialize();
 	}
 
 	public TerminalWelcomePanel() {
 		initialize();
 	}
 
 	private void initialize() {
 		this.setLayout(new BorderLayout());
 		this.panelActions = new JPanel();
 		{
 			this.panelActions.setLayout(new GridLayout(1, 2));
 			//Rent button
 			btnRent = new JButton("Louer");
 			{
 				btnRent.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						TerminalWelcomeController.getTerminalWelcomeController().askRent();
 					}
 				});
 			}
 			this.panelActions.add(btnRent);
 
 			//Return button
 			btnReturn = new JButton("Rendre");
 			{
 				btnReturn.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent ae) {
 						TerminalWelcomeController.getTerminalWelcomeController().askReturn();
 					}
 				});
 			}
 			this.panelActions.add(btnReturn);
 		}
 		this.add(panelActions, BorderLayout.CENTER);
 	}
 }
