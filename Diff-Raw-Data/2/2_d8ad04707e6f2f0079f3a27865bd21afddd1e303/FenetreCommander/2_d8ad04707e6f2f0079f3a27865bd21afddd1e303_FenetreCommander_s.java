 package ihm.fenetres;
 
 import ihm.actions.ValiderCommanderAction;
 import ihm.barresOutils.BarreOutilsCommande;
 
 import java.awt.BorderLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.TableRowSorter;
 
 import modele.Billeterie;
 import modele.Commande;
 import modele.ListeBillets;
 import modele.Personne;
 
 @SuppressWarnings("serial")
 public class FenetreCommander extends Fenetre {
 	private JPanel contentPane;
 	private JTable tableauBillets;
 	private Commande commande;
 	private JLabel label;
 	private JPanel southPane;
 	
 	public FenetreCommander(Personne personne, Billeterie billeterie) {
 		
 		//Initialisation des attributs de la classe
 		this.setTitle("Commande de billet pour "+ personne);
 		this.contentPane = new JPanel();
 		this.southPane = new JPanel();
 		this.commande = new Commande(personne);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
 		this.add(contentPane);
 		
 		//Initialisation du tableau de billets
 		tableauBillets = new JTable(billeterie.getListeBillets());
 		tableauBillets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		tableauBillets.setAutoCreateRowSorter(true);
 		TableRowSorter<ListeBillets> sorter2 = new TableRowSorter<ListeBillets>((ListeBillets) tableauBillets.getModel());   
 		tableauBillets.setRowSorter(sorter2);
 		sorter2.setSortsOnUpdates(true);
 		
 		// Ajout des elements de la fenetre
 		contentPane.setLayout(new BorderLayout(0, 0));
 		JScrollPane scrollPane = new JScrollPane(tableauBillets);
 		contentPane.add(scrollPane, "Center");
 		contentPane.add(new BarreOutilsCommande(this, billeterie, tableauBillets, commande), "North");
 		label = new JLabel(commande.toString());
 		
 		southPane.add(label);
 		southPane.add(new JButton(new ValiderCommanderAction(this, commande)));	
 		contentPane.add(southPane, "South");
 		
 		//Affichage de la fenetre
 		this.afficher();
 	}
 	
 	public void majLabel() {
		label = new JLabel(commande.toString());
 	}
 }
