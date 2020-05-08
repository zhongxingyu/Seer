 package ihm.actions;
 
 import ihm.fenetres.FenetreCommander;
 import ihm.fenetres.FenetreCommanderForcer;
 import ihm.fenetres.FenetreQuantite;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import modele.AchatException;
 import modele.Billet;
 import modele.Commande;
 
 @SuppressWarnings("serial")
 public class ValiderQuantiteAction extends AbstractAction implements KeyListener {
 	
 		private FenetreQuantite fenetre;
 		private FenetreCommander fenetreCommande;
 		private Billet billet;
 		private Commande commande;
 		
 		/**
 		 * 
 		 * @param fenetreCommande la fenetre commande en cours
 		 * @param fenetreQuantite la fenetre quantite en cours
 		 * @param commande la commande en cours
 		 * @param billet le billet a commander
 		 * @see FenetreCommander
 		 * @see FenetreQuantite
 		 * @see Commande
 		 * @see Billet
 		 */
 		public ValiderQuantiteAction(FenetreCommander fenetreCommande, FenetreQuantite fenetreQuantite, Commande commande, Billet billet) {
 		    super("Valider");
 		    this.fenetreCommande = fenetreCommande;
 		    this.fenetre = fenetreQuantite;
 		    this.billet = billet;
 		    this.commande = commande;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			validerQuantite();
 		}
 		
 		/**
 		 *
 		 */
 		private void validerQuantite(){
 			try {
 				commande.ajoutCommande(billet, fenetre.getQuantite(), fenetre.getPaye(), fenetre.getDonne(), fenetre.getSubventionne());
 				fenetreCommande.majLabel();
 			} catch (AchatException ae) {
 				new FenetreCommanderForcer(commande, ae.toString(), fenetreCommande);
 			} catch (Exception e1) {
 				String message = "Erreur dans le choix de la quantité\nCommande de ce billet annulée";
				System.out.println(e1);
 				JOptionPane.showMessageDialog(new JFrame(), message, "Erreur", JOptionPane.ERROR_MESSAGE);
 			}
 			fenetre.dispose();
 			
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
