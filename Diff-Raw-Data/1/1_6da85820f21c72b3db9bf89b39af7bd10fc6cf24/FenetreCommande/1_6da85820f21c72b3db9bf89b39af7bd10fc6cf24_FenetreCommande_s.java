 package ihm.fenetres;
 
 import ihm.actions.ValiderCommandeAction;
 import ihm.barresOutils.BarreOutilsCommande;
 
 import java.awt.BorderLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.TableRowSorter;
 
 import modele.Billeterie;
 import modele.Commande;
 import modele.ListeBillets;
 import modele.Personne;
 
 @SuppressWarnings("serial")
 public class FenetreCommande extends Fenetre {
 	private JPanel contentPane;
 	private Billeterie billeterie;
 	private JTable tableauBillets;
 	private Commande commande;
 	private JTextPane txtPane;
 	private JPanel southPane;
 	
 	public FenetreCommande(Personne personne, Billeterie billets) {
 		//Initialisation des attributs de la classe
 		this.setTitle("Commande de billet pour "+ personne);
 		this.billeterie = billets;
 		this.contentPane = new JPanel();
 		this.southPane = new JPanel();
 		this.commande = new Commande(personne);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
 		this.add(contentPane);
 		
 		//Initialisation du tableau de billets
 		tableauBillets = new JTable(billeterie.getListeBillets());
 		tableauBillets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		billeterie.getListeBillets().setTableau(tableauBillets);
 		tableauBillets.setAutoCreateRowSorter(true);
 		TableRowSorter<ListeBillets> sorter2 = new TableRowSorter<ListeBillets>((ListeBillets) tableauBillets.getModel());   
 		tableauBillets.setRowSorter(sorter2);
 		sorter2.setSortsOnUpdates(true);
 		
 		// Ajout des elements de la fenetre
 		contentPane.setLayout(new BorderLayout(0, 0));
 		JScrollPane scrollPane = new JScrollPane(tableauBillets);
 		contentPane.add(scrollPane, "Center");
 		contentPane.add(new BarreOutilsCommande(billeterie), "North");
 		txtPane = new JTextPane();
 		txtPane.setText(commande.toString());
 		
 		southPane.add(txtPane);
 		southPane.add(new JButton(new ValiderCommandeAction(this, commande)));	
 		contentPane.add(southPane, "South");
 		
 		//Affichage de la fenetre
 		this.afficher();
 	}
 }
