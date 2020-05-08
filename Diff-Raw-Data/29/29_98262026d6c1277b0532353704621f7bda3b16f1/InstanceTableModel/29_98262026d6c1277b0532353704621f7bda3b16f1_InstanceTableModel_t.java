 package gde.gui.tablemodels;
 
 import gde.models.Game;
 import gde.models.Instance;
 import javax.swing.SwingUtilities;
 
 public class InstanceTableModel extends CollectionTableModel<Instance> {
     private static final Class[] TYPES = {String.class};
     private static final String[] TITLES = {"Identifier"};
     private static final String COLLECTION = "instances";
     
     public InstanceTableModel(Game game) {
         super(game, TYPES, TITLES, COLLECTION);
     }
 
     @Override
     public void populate(String query) {
         ids.clear();
         setRowCount(0);
         Iterable<Instance> instances = collection.find(query).as(Instance.class);
         for (final Instance instance : instances) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                     addRow(instance);
                }
            });
         }
     }
     
     @Override
     public void populate() {
         String query = String.format("{gameId: '%s'}", game.getKey().toString());
         populate(query);
     }
     
     @Override
     public Instance getEntryAt(int rowId) {
         return collection.findOne(ids.get(rowId)).as(Instance.class);
     }
 
     @Override
     public void setEntryAt(int rowId, Instance entry) {
         setValueAt(entry.getIdentifier(), rowId, 0);
     }
 
     @Override
     protected void addRow(Instance entry) {
         addRow(new Object[] {entry.getIdentifier()});
         ids.add(entry.getKey());
     }
 }
