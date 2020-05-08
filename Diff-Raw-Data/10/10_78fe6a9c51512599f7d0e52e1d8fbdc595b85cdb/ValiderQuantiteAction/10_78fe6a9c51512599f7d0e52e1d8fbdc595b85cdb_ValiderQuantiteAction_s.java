 package ihm.actions;
 
 import ihm.fenetres.FenetreQuantite;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import modele.Billet;
 import modele.Commande;
 
 @SuppressWarnings("serial")
 public class ValiderQuantiteAction extends AbstractAction implements KeyListener {
 		private FenetreQuantite fenetre;
 		private Billet billet;
 		private Commande commande;
 		
 		public ValiderQuantiteAction(FenetreQuantite fenetreQuantite, Commande commande, Billet billet) {
 		    super("Valider");
 		    this.fenetre = fenetreQuantite;
 		    this.billet = billet;
 		    this.commande = commande;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			validerQuantite();
 		}
 		
 		private void validerQuantite(){
 			try {
 				commande.ajoutCommande(billet, fenetre.getQuantite(), fenetre.getPaye(), fenetre.getDonne(), fenetre.getSubventionne());
 		    	fenetre.dispose();
 			} catch (Exception e1) {
 				String message = "Erreur dans le choix de la quantité\nCommande de ce billet annulée";
 				JOptionPane.showMessageDialog(new JFrame(), message, "Erreur", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 
 		public void keyTyped(KeyEvent e) {
 		}
 
 		public void keyPressed(KeyEvent e) {
 	        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 		    	validerQuantite();
 	        }
 		}
 
 		public void keyReleased(KeyEvent e) {
 		}
 	}
