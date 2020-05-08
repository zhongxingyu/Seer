 package gde.gui.tablemodels;
 
 
 import static gde.gui.tablemodels.TableModel.database;
 import gde.models.CapturedData;
 import gde.models.Field;
 import gde.models.Field.FieldType;
 import gde.models.Game;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import org.bson.types.ObjectId;
 import org.jongo.MongoCollection;
 
 public class SummaryTableModel extends TableModel {
 
     class DatabaseQueryRunnable implements Runnable {
 
         private final ObjectId id;
 
         DatabaseQueryRunnable(ObjectId id) {
             this.id = id;
         }
 
         @Override
         public void run() {
             MongoCollection capturedDataCollection = database.getCollection("captureddata");
             String query = String.format("{instanceId: '%s'}", id.toString());
             CapturedData capturedData = capturedDataCollection.find(query).sort("{_id: -1}").limit(1).as(CapturedData.class).iterator().next();
             for (Entry<String, String> data : capturedData.getData().entrySet()) {
                 if (statsMap.containsKey(data.getKey())) {
                     statsMap.get(data.getKey()).add(data.getValue());
                 } else {
                     CopyOnWriteArrayList<String> stats = new CopyOnWriteArrayList<String>();
                     stats.add(data.getValue());
                     statsMap.put((String) data.getKey(), stats);
                 }
             }
         }
     }
     private static final Class[] TYPES = {String.class, String.class, String.class, String.class, String.class, String.class};
     private static final String[] TITLES = {"Field", "Least", "Most", "Average", "Minimum", "Maximum"};
     private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> statsMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();
 
     public SummaryTableModel(Game game) {
         super(game, TYPES, TITLES);
     }
 
     public void populate(List<ObjectId> ids, int[] selectedRows) {
         setRowCount(0);
         statsMap.clear();
         ExecutorService executor = Executors.newFixedThreadPool(50);
         for (int selectedRow : selectedRows) {
             Runnable worker = new DatabaseQueryRunnable(ids.get(selectedRow));
             executor.execute(worker);
         }
 
         executor.shutdown();
         while (!executor.isTerminated()) {
         }
 
         if (statsMap.size() > 0) {
             MongoCollection fieldsCollection = database.getCollection("fields");
             String fieldQuery = String.format("{gameId: '%s'}", game.getKey().toString());
             Iterable<Field> fields = fieldsCollection.find(fieldQuery).as(Field.class);
             for (Field field : fields) {
                 List<String> stats = statsMap.get(field.getName().toLowerCase());
                 String least = "-";
                 String most = "-";
                 String average = "-";
                 String minimum = "-";
                 String maximum = "-";
                 if (field.getType() == FieldType.TEXT) {
                     Map<String, Integer> textCounts = new HashMap<String, Integer>();
                     for (String stat : stats) {
                         if (textCounts.containsKey(stat)) {
                             textCounts.put(stat, textCounts.get(stat) + 1);
                         } else {
                             textCounts.put(stat, 1);
                         }
                         Map.Entry<String, Integer> maxEntry = null;
                         Map.Entry<String, Integer> minEntry = null;
                         Double sum = 0.0;
                         for (Entry<String, Integer> entry : textCounts.entrySet()) {
                             sum += entry.getValue();
                             if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                                 maxEntry = entry;
                             }
                             if (minEntry == null || entry.getValue() < minEntry.getValue()) {
                                 minEntry = entry;
                             }
                         }
                         least = String.format("%s (%s%%)", minEntry.getKey(), round(minEntry.getValue() / sum) * 100);
                         most = String.format("%s (%s%%)", maxEntry.getKey(), round(maxEntry.getValue() / sum) * 100);     
                     }
                 } else if (field.getType() == FieldType.INTEGER || field.getType() == FieldType.DECIMAL) {
                     List<Double> doubleStats = new LinkedList<Double>();
                     for (String stat : stats) {
                         doubleStats.add(Double.valueOf(stat));
                     }
                     minimum = Collections.min(doubleStats).toString();
                     maximum = Collections.max(doubleStats).toString();
                     double sum = 0;
                     for (double stat : doubleStats) {
                         sum += stat;
                     }
                     average = String.valueOf(round(sum / doubleStats.size()));
                 }
                 addRow(new Object[]{
                     field,
                     least,
                     most,
                     average,
                     minimum,
                     maximum,});
             }
         }
     }
     
     private double round(Double x) {
         return Math.round(x * 100.0) / 100.0;
     }
}
