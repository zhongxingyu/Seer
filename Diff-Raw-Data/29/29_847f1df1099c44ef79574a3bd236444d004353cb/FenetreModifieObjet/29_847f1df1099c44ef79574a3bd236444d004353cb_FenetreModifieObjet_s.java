 package ihm.fenetres;
 
 import ihm.barresOutils.Champs;
 
 import java.awt.event.ActionEvent;
import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
 
import modele.Billeterie;
 import modele.ListeObjet;
 import modele.Objet;
import modele.Personne;
@SuppressWarnings("unused")
 
 
 public class FenetreModifieObjet extends JFrame {
 	private static final long serialVersionUID = 1L;
 	Objet personne;
 	Champs champs;
 	ListeObjet listeObjet;
	// TODO : les champs doivent êter prérempli avec les données
 	
 	/**
 	 * Constructeur
 	 * @param billeterie
 	 */
 	public FenetreModifieObjet(Objet personne, ListeObjet listeObjet) {
 		this.personne = personne;
 		this.listeObjet = listeObjet;
 		champs = new Champs(listeObjet.getAttributsType());
 		champs.setValeurs(personne.getHashMap());
 		setBounds(100, 100, 450, 300);
 		this.add(champs, "Center");
 		this.add(new JButton(new ValiderAction(this)), "South");
 		this.setTitle("Modifier une nouvelle personne");
 		this.setVisible(true);
 	}
 	
 	/**
 	 * @return les Champs de données
 	 */
 	public Champs getChamps() {
 		return champs;
 	}
 	
 	public Objet getObjet() {
 		return personne;
 	}
 	
 	private class ValiderAction extends AbstractAction {
 		private static final long serialVersionUID = 1L;
 		FenetreModifieObjet fenetre;
         private ValiderAction(FenetreModifieObjet fenetreModifiePersonne) {
             super("Valider");
             this.fenetre = fenetreModifiePersonne;
         }
  
         public void actionPerformed(ActionEvent e) {
 			try {
 				Map<String, Object>  map = fenetre.getChamps().getDonnees();
 				fenetre.getObjet().modifie(map);
 	        	fenetre.setVisible(false);
 			} catch (Exception e1) {
 				String message = "\"Erreur lors de la modification\"\n"
 				            + "Tous les champs n'ont pas ete renseignes\n";
 				JOptionPane.showMessageDialog(new JFrame(), message, "Dialog", JOptionPane.ERROR_MESSAGE);
 			}
         }
     }
 }
