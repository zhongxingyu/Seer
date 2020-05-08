 package graphique.admin;
 
 import graphique.models.TrajetsTableModel;
 import graphique.widgets.AbstractTablePanel;
 import graphique.widgets.TableSpinnerEditor;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import logiqueMetier.Serveur;
 import objets.Trajet;
 import objets.Vehicule;
 import objets.Ville;
 
 /**
  * Cette classe fournit un tableau et des boutons associés permettant de modifier
  * créer et supprimer des trajets.
  * 
  * @author Fauvel-jaeger Olivier, Tanguy Arnaud, Ceschel Marvin, Kruck Nathan
  * @version 2012.01.29
  */
 
 public class TableTrajetsPanel extends AbstractTablePanel {
     private static final long serialVersionUID = 1L;
 
     private TableSpinnerEditor dateDepartSpinner;
     private TableSpinnerEditor dateArriveeSpinner;
     
     JComboBox departCombo;
 
     private ArrayList<Trajet> trajets;
     private ArrayList<Ville> villes;
 
     public TableTrajetsPanel(Serveur s) {
         super(s);
         trajets = serveur.getTrajets();
         villes = serveur.getVilles();
         departCombo = new JComboBox();
         build();
     }
 
     private void build() {
         setBorder(BorderFactory.createTitledBorder("Gestion des trajets"));
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 
         buildReservationsTable();
         buildButtons();
     }
 
     private void buildReservationsTable() {
         String[] columnNames = { "Départ", "Arrivée", "Date départ",
                 "Date arrivée", "Transport" };
 
         // Create a Spinner for the date, using a date model with current date
         // as the initial value.
         dateDepartSpinner = new TableSpinnerEditor(new SpinnerDateModel());
         dateArriveeSpinner = new TableSpinnerEditor(new SpinnerDateModel());
 
         model = new TrajetsTableModel<Trajet>(trajets);
         model.setColumnNames(columnNames);
         table = new JTable();
         table.setModel(model);
         table.setFillsViewportHeight(true); // Fill all the container
         table.getModel().addTableModelListener(new CellListener());
 
        JComboBox combo = buildDepartCombo();
        addComboToTable(combo, 0);
        addComboToTable(combo, 1);
         addSpinnerToTable(dateDepartSpinner, 2);
         addSpinnerToTable(dateArriveeSpinner, 3);
 
         scrollPane = new JScrollPane(table);
         add(scrollPane);
     }
 
    private JComboBox buildDepartCombo() {
         for (Ville v : villes) {
             departCombo.addItem(v);
         }
        return departCombo;
     }
 
     private void buildButtons() {
         buttonsPanel
                 .setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
         buttonsPanel.add(new JButton(new AddAction("Ajouter")),
                 BorderLayout.CENTER);
         buttonsPanel.add(new JButton(new DeleteAction("Supprimer")),
                 BorderLayout.CENTER);
         buttonsPanel
                 .add(new JButton(new LinkAction("Lier à un tranport", this)));
         add(buttonsPanel);
     }
     /**
      * Lie un trajet avec un transport
      */
     public class LinkAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
         private TableTrajetsPanel parent;
 
         public LinkAction(String texte, TableTrajetsPanel parent) {
             super(texte);
             this.parent = parent;
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             System.out.println("Link !");
             int[] selected = table.getSelectedRows();
             for (final int i : selected) {
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         TransportSelectorDialog d = new TransportSelectorDialog(
                                 serveur, parent, i);
                         d.setVisible(true);
                     }
                 });
 
             }
         }
     }
 
     /** 
      * Ajoute un nouveau trajet
      */
     public class AddAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public AddAction(String texte) {
             super(texte);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             System.out.println("Ajout !");
             model.addRow(new Trajet(serveur.getTrajetNewIdentifiant()));
         }
     }
 
     public class DeleteAction extends AbstractAction {
         private static final long serialVersionUID = 1L;
 
         public DeleteAction(String texte) {
             super(texte);
         }
 
         @Override
         public void actionPerformed(ActionEvent arg0) {
             System.out.println("Supprimé !");
             int[] selectedIndexes = table.getSelectedRows();
             for (int i = selectedIndexes.length - 1; i >= 0; i--) {
                 int row = selectedIndexes[i];
                 System.out.println(model.getValueAt(row, 0));
                 serveur.removeTrajet((Trajet) model.get(row));
                 model.removeRow(row);
             }
         }
     }
     /**
      * Ecoute les modifications effectuées dans le tableau, et met à jour les données 
      * du serveur si nécessaire.
      */
     private class CellListener implements TableModelListener {
         public CellListener() {
         }
 
         public void tableChanged(TableModelEvent e) {
             int row = e.getFirstRow();
             int column = e.getColumn();
             System.out.println("Row " + row);
             System.out.println("Column " + column);
 
             switch (e.getType()) {
             case TableModelEvent.UPDATE:
                 System.out.println("Updated");
                 for (Trajet t : trajets) {
                     Trajet tt = (Trajet) model.get(row);
                     if (t.getIdentifiant() == tt.getIdentifiant()) {
                         try {
                             serveur.modifierTrajet(t, tt);
                         } catch (Exception e1) {
                             e1.printStackTrace();
                         }
                     }
                 }
                 break;
             }
         }
     }
 
     /** 
      * Fonction permettant de lier le transport sélectionné à un véhicule donné
      * @param parentSelectedRow 
      *          ligne sélectionnée dans le tableau des trajets
      * @param selectedTransport
      *          vehicule choisi
      */
     public void linkTransport(int parentSelectedRow, Vehicule selectedTransport) {
         model.setValueAt(selectedTransport, parentSelectedRow, 4);
         System.out.println("Lié à " + selectedTransport.toString());
     }
 
     public void updateVilles() {
         villes = serveur.getVilles();
         departCombo.removeAll();
        buildDepartCombo();
     }
 }
