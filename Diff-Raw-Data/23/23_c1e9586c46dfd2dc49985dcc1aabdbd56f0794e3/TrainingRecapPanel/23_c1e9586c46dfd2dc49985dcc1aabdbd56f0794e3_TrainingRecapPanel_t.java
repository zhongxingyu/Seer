 // %776182880:hoplugins.trainingExperience.ui%
 package ho.module.training.ui;
 
 import ho.core.gui.comp.panel.ImagePanel;
 import ho.core.gui.model.BaseTableModel;
 import ho.core.model.HOVerwaltung;
 import ho.core.model.UserParameter;
 import ho.core.model.player.ISkillup;
 import ho.core.model.player.Spieler;
 import ho.core.training.FutureTrainingManager;
 import ho.core.training.TrainingPerWeek;
 import ho.module.training.ui.renderer.TrainingRecapRenderer;
 
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 
 
 /**
  * Recap Panel when future preview of skillups is shown ("Prediction" tab, "Training Recap"
  * table").
  *
  * @author <a href=mailto:draghetto@users.sourceforge.net>Massimiliano Amato</a>
  */
 public class TrainingRecapPanel extends JPanel {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 7240288702397251461L;
	private static final int fixedColumns = 2;
 
 	private BaseTableModel tableModel;
 
     //private HashMap playerRef;
     private TrainingRecapTable recapTable;
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new TrainingRecapPanel object.
      */
     public TrainingRecapPanel() {
         super();
         reload();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Reload the panel
      */
     public void reload() {
         jbInit();
 
         // empty the table
         Vector<String> columns = getColumns();
 
         //playerRef = new HashMap();
         List<TrainingPerWeek> trainings = ho.module.training.TrainingPanel.getTrainPanel().getFutureTrainings();
 
         Vector<Spieler> v = HOVerwaltung.instance().getModel().getAllSpieler();
 
         List<Vector<String>> players = new ArrayList<Vector<String>>();
 
         for (Iterator<Spieler> iter = v.iterator(); iter.hasNext();) {
             Spieler player = iter.next();
             StaffPanel sp = ho.module.training.TrainingPanel.getStaffPanel();
             FutureTrainingManager ftm = new FutureTrainingManager(player,trainings, 
             		sp.getCoTrainerNumber(), sp.getTrainerLevelNumber());
             List<ISkillup> su = ftm.getFutureSkillups();
 
             // Skip player!
             if (su.size() == 0) {
                 continue;
             }
 
             HashMap<String,ISkillup> maps = new HashMap<String,ISkillup>();
 
             for (Iterator<ISkillup> iterator = su.iterator(); iterator.hasNext();) {
                 ISkillup skillup = iterator.next();
 
                 maps.put(skillup.getHtSeason() + " " + skillup.getHtWeek(), skillup); //$NON-NLS-1$
             }
 
             Vector<String> row = new Vector<String>();
 
             row.add(player.getName());
             row.add(player.getAlterWithAgeDaysAsString());
 
             for (int i = 0; i < UserParameter.instance().futureWeeks; i++) {
                ISkillup s = (ISkillup) maps.get(columns.get(i + fixedColumns));
 
                 if (s == null) {
                     row.add(""); //$NON-NLS-1$
                 } else {
                     row.add(s.getType() + " " + s.getValue()); //$NON-NLS-1$
                 }
             }
 
             row.add(Integer.toString(player.getSpielerID()));
 
             //playerRef.put(player.getName(), player.getSpielerID() + "");
             players.add(row);
 
             //count++;
         }
 
         // Sort the players
        SortedSet<Vector<String>> set = new TreeSet<Vector<String>>(new TrainingComparator(2, fixedColumns));
         if ((players != null) && (players.size() > 0)) 
                 set.addAll(players);
 
         // and add them to the model
         for (Iterator<Vector<String>>  iter = set.iterator(); iter.hasNext();) {
             Vector<String> row = iter.next();
 
             tableModel.addRow(row);
         }
 
         updateUI();
     }
 
     /**
      * Get Columns name
      *
      * @return List of string
      */
     private Vector<String> getColumns() {
         Vector<String> columns = new Vector<String>();
 
         columns.add(HOVerwaltung.instance().getLanguageString("Spieler")); //$NON-NLS-1$
         columns.add(HOVerwaltung.instance().getLanguageString("Alter")); //$NON-NLS-1$
 
         int actualSeason =HOVerwaltung.instance().getModel().getBasics().getSeason();
         int actualWeek = HOVerwaltung.instance().getModel().getBasics().getSpieltag();
 
         // We are in the middle where season has not been updated!
         try {
             if (HOVerwaltung.instance().getModel().getXtraDaten().getTrainingDate().after(HOVerwaltung.instance().getModel()
                                                                                  .getXtraDaten()
                                                                                  .getSeriesMatchDate())) {
                 actualWeek++;
 
                 if (actualWeek == 17) {
                     actualWeek = 1;
                     actualSeason++;
                 }
             }
         } catch (Exception e1) {
             // Null when first time HO is launched		
         }
 
         for (int i = 0; i < UserParameter.instance().futureWeeks; i++) {
             // calculate the week and season of the future training
             int week = (actualWeek + i) - 1;
             int season = actualSeason + (week / 16);
 
             week = (week % 16) + 1;
 
             columns.add(season + " " + week); //$NON-NLS-1$
         }
 
         columns.add(HOVerwaltung.instance().getLanguageString("ID")); //$NON-NLS-1$
 
         return columns;
     }
 
     /**
      * Initialize the GUI
      */
     private void jbInit() {
         removeAll();
         setOpaque(false);
         setLayout(new BorderLayout());
 
         JPanel panel = new ImagePanel();
 
         panel.setOpaque(false);
         panel.setLayout(new BorderLayout());
 
         JLabel title = new JLabel(HOVerwaltung.instance().getLanguageString("Recap"), SwingConstants.CENTER); //$NON-NLS-1$
 
         title.setOpaque(false);
         panel.add(title, BorderLayout.NORTH);
 
         Vector<String> columns = getColumns();
 
         tableModel = new BaseTableModel(new Vector<Object>(), columns);
 
         JTable table = new JTable(tableModel);
 
         table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
        recapTable = new TrainingRecapTable(table, fixedColumns);
 
         recapTable.getScrollTable().setDefaultRenderer(Object.class, new TrainingRecapRenderer());
 
         JTable scrollTable = recapTable.getScrollTable();
 
         // Hide the last column
         int lastSTCol = scrollTable.getColumnCount() - 1;
 
         scrollTable.getTableHeader().getColumnModel().getColumn(lastSTCol).setPreferredWidth(0);
         scrollTable.getTableHeader().getColumnModel().getColumn(lastSTCol).setMinWidth(0);
         scrollTable.getTableHeader().getColumnModel().getColumn(lastSTCol).setMaxWidth(0);
 
         JTable lockedTable = recapTable.getLockedTable();
         lockedTable.getSelectionModel().addListSelectionListener(new PlayerSelectionListener(scrollTable,
                                                                                              lastSTCol));
         panel.add(recapTable, BorderLayout.CENTER);
         recapTable.getScrollTable().getTableHeader().setReorderingAllowed(false);
 
         // Add legend panel. 
         panel.add(new TrainingLegendPanel(), BorderLayout.SOUTH);
         add(panel, BorderLayout.CENTER);
     }
 }
