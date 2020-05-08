 package polly.rx.core;
 
 import java.awt.Color;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import de.skuzzle.polly.sdk.PersistenceManager;
 import de.skuzzle.polly.sdk.WriteAction;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.time.DateUtils;
 import de.skuzzle.polly.sdk.time.Time;
 import polly.rx.entities.ScoreBoardEntry;
 import polly.rx.graphs.ImageGraph;
 import polly.rx.graphs.Point;
 import polly.rx.graphs.PointSet;
 import polly.rx.graphs.Point.PointType;
 
 
 public class ScoreBoardManager {
     
     public final static int X_LABELS = 24;
     
     private final static Color[] COLORS = {
         Color.RED, Color.BLUE, Color.BLACK, Color.GREEN, Color.PINK
     };
     
     public final static NumberFormat NUMBER_FORMAT = DecimalFormat.getInstance(
             Locale.ENGLISH);
     static {
         ((DecimalFormat) NUMBER_FORMAT).applyPattern("0.00");
     }
 
     private PersistenceManager persistence;
     
     
     
     public ScoreBoardManager(PersistenceManager persistence) {
         this.persistence = persistence;
     }
     
     
     
     public int maxColors() {
         return COLORS.length;
     }
     
     
     
     public InputStream createGraph(List<ScoreBoardEntry> all) {
         if (all.isEmpty()) {
             return null;
         }
         Collections.sort(all, ScoreBoardEntry.BY_DATE);
         
         final ImageGraph g = new ImageGraph(700, 400, 2000, 30000, 2000);
         g.setxLabels(this.createXLabels());
         g.setDrawGridHorizontal(true);
         g.setDrawGridVertical(true);
         g.setConnect(true);
         g.addPointSet(this.createPointSet(all, Color.RED));
 
         
         g.updateImage();
         return g.getBytes();
     }
     
     
     
     public InputStream createMultiGraph(String...names) {
         ImageGraph g = new ImageGraph(700, 400, 2000, 30000, 2000);
         g.setxLabels(this.createXLabels());
         g.setDrawGridHorizontal(true);
         g.setDrawGridVertical(true);
         g.setConnect(true);
 
         int max = Math.min(COLORS.length, names.length);
         for (int i = 0; i < max; ++i) {
             final List<ScoreBoardEntry> entries = this.getEntries(names[i]);
             Collections.sort(entries, ScoreBoardEntry.BY_DATE);
             final Color next = COLORS[i];
             final PointSet points = this.createPointSet(entries, next);
             g.addPointSet(points);
         }
         
         g.updateImage();
         return g.getBytes();
     }
     
     
     
     private final String[] createXLabels() {
         final DateFormat df = new SimpleDateFormat("MMM yyyy");
         
         String[] labels = new String[X_LABELS];
         final Date today = Time.currentTime();
         for (int i = 0; i < X_LABELS; ++i) {
             final Calendar c = Calendar.getInstance();
             c.setTime(today);
             c.add(Calendar.MONTH, -(X_LABELS - (i + 1)));
             labels[i] = df.format(c.getTime());
         }
         return labels;
     }
     
     
     
     private PointSet createPointSet(List<ScoreBoardEntry> entries, Color color) {
         final ScoreBoardEntry oldest = entries.get(0);
         
         final Date today = Time.currentTime();
         boolean zero = false;
         final PointSet points = new PointSet(color);
         Point greatestLowerZero = null;
         Point lowestGreaterZero = null;
         
         for (final ScoreBoardEntry entry : entries) {
             final int monthsAgo = this.getMonthsAgo(today, entry.getDate());
             final double x = this.calcX(entry.getDate(), monthsAgo);
             final Point p = new Point(x, entry.getPoints(), PointType.NONE);
 
             if (x < 0.0 && (greatestLowerZero == null || greatestLowerZero.getX() < x)) {
                 greatestLowerZero = p;
             }
             if (x > 0.0 && (lowestGreaterZero == null || lowestGreaterZero.getX() > x)) {
                 lowestGreaterZero = p;
             }
             
             zero |= monthsAgo == 0;
             if (monthsAgo < 0) {
                 // do not add points that are older than X_LABELS months
                 continue;
             }
             points.add(p);
         }
         if (!zero && Math.abs(
                     DateUtils.monthsBetween(today, oldest.getDate())) > X_LABELS) {
             
             // interpolate correct y-axis intersection
             double m = (lowestGreaterZero.getY() - greatestLowerZero.getY()) / 
                     (lowestGreaterZero.getX() - greatestLowerZero.getX());
             double y = m * (-lowestGreaterZero.getX()) + lowestGreaterZero.getY();
             points.add(new Point(0.0, y, PointType.DOT));
         }
         points.setName(oldest.getVenadName());
         return points;
     }
     
     
     
 
     private int getMonthsAgo(Date today, Date other) {
         final int monthsBetween = DateUtils.monthsBetween(today, other);
         final int monthsAgo =  X_LABELS - monthsBetween - 1;
         return monthsAgo;
     }
     
     
     
     private double calcX(Date d, int monthsAgo) {
         final Calendar c = Calendar.getInstance();
         c.setTime(d);
         final int dayInMonth = c.get(Calendar.DAY_OF_MONTH);
         final int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);
         final double x = monthsAgo + (double) dayInMonth / (double) days; 
         return x;
     }
 
 
     
     public void addEntries(Collection<ScoreBoardEntry> entries) throws DatabaseException {
         try {
             this.persistence.writeLock();
             this.persistence.startTransaction();
             
             for (final ScoreBoardEntry entry : entries) {
                 final Collection<ScoreBoardEntry> existing = this.persistence.findList(
                         ScoreBoardEntry.class, ScoreBoardEntry.SBE_BY_USER, 
                         entry.getVenadName());
                 
                 for (final ScoreBoardEntry e : existing) {
                     if (!DateUtils.isSameDay(e.getDate(), entry.getDate())) {
                        this.persistence.persist(e);
                     }
                 }
             }
             this.persistence.commitTransaction();
         } finally {
             this.persistence.writeUnlock();
         }
     }
     
     
     
     public void addEntry(final ScoreBoardEntry entry) throws DatabaseException {
         
         Collection<ScoreBoardEntry> existing = this.getEntries(entry.getVenadName());
         
         // skip identical entries.
         for (ScoreBoardEntry e : existing) {
             if (DateUtils.isSameDay(e.getDate(), entry.getDate())) {
                 return;
             }
         }
         
         this.persistence.atomicWriteOperation(new WriteAction() {
             @Override
             public void performUpdate(PersistenceManager persistence) {
                 persistence.persist(entry);
             }
         });
     }
     
     
     
     public void deleteEntry(int id) throws DatabaseException {
         ScoreBoardEntry sbe = this.persistence.atomicRetrieveSingle(
             ScoreBoardEntry.class, id);
         
         if (sbe == null) { 
             return;
         }
         
         this.persistence.atomicRemove(sbe);
     }
     
     
     
     
     public List<ScoreBoardEntry> getEntries(String venad) {
         return this.persistence.atomicRetrieveList(ScoreBoardEntry.class, 
             ScoreBoardEntry.SBE_BY_USER, venad);
     }
     
     
     
     public List<ScoreBoardEntry> getEntries() {
         return this.persistence.atomicRetrieveList(ScoreBoardEntry.class, 
             ScoreBoardEntry.ALL_SBE_DISTINCT);
     }
     
 }
