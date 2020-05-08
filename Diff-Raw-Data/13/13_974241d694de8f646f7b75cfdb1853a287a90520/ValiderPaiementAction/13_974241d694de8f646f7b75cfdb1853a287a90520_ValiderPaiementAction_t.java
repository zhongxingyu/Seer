 package ihm.actions;
 
 import general.Constantes;
 import ihm.fenetres.FenetreDetails;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 
 import modele.Achat;
 import modele.ListeAchats;
 import modele.Personne;
 
 @SuppressWarnings("serial")
 public class ValiderPaiementAction extends AbstractAction implements KeyListener {
 
 	private FenetreDetails fenetreDetails;
 	private JTable tableau;
 	private ListeAchats listeAchats;
 	
 	public ValiderPaiementAction(FenetreDetails fenetre, ListeAchats listeAchats, JTable tableau) {
 		super("Payer");
 		this.fenetreDetails = fenetre;
 		this.tableau = tableau;
 		this.listeAchats = listeAchats;
 	}
 	
 	private void valider() {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				boolean select = true;
 				int selectionCorrige = 0;
 				int selection = tableau.getSelectedRow();
 				try {
 					selectionCorrige = listeAchats.getTableau().getRowSorter().convertRowIndexToModel(selection);
 				} catch (Exception e) {
 					// Cas ou aucun billet n'est selectionne
 					select = false;
 					JOptionPane.showMessageDialog(new JFrame(), 
 							"Vous n'avez pas de selectionne de billet", "Attention", JOptionPane.INFORMATION_MESSAGE);
             	}
 				if(select) {
 					// Cas ou un billet est bien selectionne
 					try {
 						Achat achat = (Achat) listeAchats.getObjetByIndex(selectionCorrige);
 						Personne personne = fenetreDetails.getPersonne();
 						if(!achat.getPaye()){
 							System.out.println("" + personne + " paye le billet "+ achat);
 							achat.setPayer(true);
							System.out.println("ok");
 						} else {
 							JOptionPane.showMessageDialog(new JFrame(), 
 									"Ce billet a deja ete paye", "Attention", JOptionPane.INFORMATION_MESSAGE);
 		            	
 						}
 					} catch (Exception e1) {
 						Constantes.afficherException(e1);
 					}		
 				}
 			}
 		});
     }
 	
 	public void actionPerformed(ActionEvent e) {
 		valider();
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
