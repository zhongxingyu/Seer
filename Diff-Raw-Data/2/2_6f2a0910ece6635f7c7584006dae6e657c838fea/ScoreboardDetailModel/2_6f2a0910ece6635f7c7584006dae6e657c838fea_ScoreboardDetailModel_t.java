 package polly.rx.httpv2;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import polly.rx.MSG;
 import polly.rx.MyPlugin;
 import polly.rx.core.ScoreBoardManager;
 import polly.rx.entities.ScoreBoardEntry;
 import de.skuzzle.polly.http.api.HttpEvent;
 import de.skuzzle.polly.sdk.time.Milliseconds;
 import de.skuzzle.polly.sdk.util.DirectedComparator.SortOrder;
 
 public class ScoreboardDetailModel extends ScoreboardTableModel {
 
     private final static String[] COLUMNS = MSG.scoreboardDetailModelColumns.split(","); //$NON-NLS-1$
     private final static String VENAD_NAME = "venadName"; //$NON-NLS-1$
 
     public ScoreboardDetailModel(ScoreBoardManager sbManager) {
         super(sbManager);
         this.requirePermission(MyPlugin.SBE_PERMISSION);
     }
     
     
     
     @Override
     public Object getCellValue(int column, ScoreBoardEntry element) {
         switch (column) {
         case 0:return element.getRank();
         case 1: return element.getVenadName();
         case 2: return element.getClan();
         case 3: return element.getPoints();
         case 4: return element.getDiffToPrevious();
         case 5: return element.getDaysToPrevious();
         case 6: return element.getDiscDerivative();
         case 7: return element.getDate();
         default: return ""; //$NON-NLS-1$
         }
     }
 
 
     
     @Override
     public int getColumnCount() {
         return COLUMNS.length;
     }
 
 
 
     @Override
     public String getHeader(int column) {
         return COLUMNS[column];
     }
 
 
 
     @Override
     public Class<?> getColumnClass(int column) {
         switch (column) {
         case 6: return Double.class;
         case 7: return Date.class;
         default: return Object.class;
         }
     }
 
 
 
     @Override
     public Map<String, String> getRequestParameters(HttpEvent e) {
         final Map<String, String> result = new HashMap<String, String>();
         result.put(VENAD_NAME, e.get(VENAD_NAME));
         return result;
     }
 
     
     
     @Override
     public int getDefaultSortColumn() {
        return COLUMNS.length - 1; // last col
     }
     
     
     
     @Override
     public SortOrder getDefaultSortOrder() {
         return SortOrder.DESCENDING;
     }
     
 
 
     @Override
     public List<ScoreBoardEntry> getData(HttpEvent e) {
         final String venad = e.get(VENAD_NAME);
         final List<ScoreBoardEntry> entries = this.sbManager.getEntries(venad);
         Collections.sort(entries, ScoreBoardEntry.BY_DATE);
 
         if (entries.size() > 1) {
             for (int i = 1; i < entries.size(); ++i) {
                 ScoreBoardEntry before = entries.get(i - 1);
                 ScoreBoardEntry current = entries.get(i);
 
                 long diff = Math.abs(before.getDate().getTime()
                         - current.getDate().getTime());
                 long days = Milliseconds.toDays(diff);
                 int pointDiff = current.getPoints() - before.getPoints();
                 double pointsPerDay = (double) pointDiff / (double) days;
 
                 current.setDaysToPrevious((int) days);
                 current.setDiffToPrevious(pointDiff);
                 current.setDiscDerivative(pointsPerDay);
             }
         }
         return entries;
     }
 }
