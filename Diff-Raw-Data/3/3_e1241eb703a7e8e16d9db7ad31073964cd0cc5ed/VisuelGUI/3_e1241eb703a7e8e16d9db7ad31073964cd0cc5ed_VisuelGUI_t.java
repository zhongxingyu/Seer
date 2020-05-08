 package gomoku.jeu;
 
 import javax.swing.JOptionPane;
 import javax.swing.JComponent;
 
 public class VisuelGUI implements Visuel {
 
   private JComponent component;
 
   public VisuelGUI(JComponent component) {
     this.component = component;
   }
 
 /** Affiche un message quand un joueur gagne
   * @param le message à afficher
   */
   public void leJoueurAGagne(String str) {
     JOptionPane.showMessageDialog(null, "Le joueur " + str +
         " a gagné !" , "Fin", JOptionPane.CLOSED_OPTION,null);
    System.exit(0);
   }
 
 /** Affiche un message si la partie est nulle
   * @param le message à afficher
   */
   public void laPartieEstNulle() {
     JOptionPane.showMessageDialog(null, "Partie nulle !" ,
         "Fin", JOptionPane.CLOSED_OPTION,null);
    System.exit(0);
   }
 
 /** Permet de mettre à jour l'affichage après un coup.
   */
   public void afficherLaGrille() {
     this.component.repaint();
     this.component.validate();
   }
 }
