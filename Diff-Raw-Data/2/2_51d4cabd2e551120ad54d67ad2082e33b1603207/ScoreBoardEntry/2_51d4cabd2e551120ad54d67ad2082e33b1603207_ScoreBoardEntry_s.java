 package polly.rx.entities;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import de.skuzzle.polly.sdk.time.Milliseconds;
 
 
 @Entity
 @NamedQueries({
     @NamedQuery(
         name = ScoreBoardEntry.ALL_SBE_DISTINCT,
        query= "SELECT sbe FROM ScoreBoardEntry sbe GROUP BY sbe.venadName ORDER BY sbe.date, sbe.rank DESC"
     ),
     @NamedQuery(
         name = ScoreBoardEntry.SBE_BY_USER,
         query = "SELECT sbe FROM ScoreBoardEntry sbe WHERE sbe.venadName = ?1 ORDER BY sbe.date, sbe.rank DESC"
     )
 })
 public class ScoreBoardEntry {
     
     
     public final static Comparator<ScoreBoardEntry> BY_DATE = 
         new Comparator<ScoreBoardEntry>() {
     
             @Override
             public int compare(ScoreBoardEntry o1, ScoreBoardEntry o2) {
                 return o1.getDate().compareTo(o2.getDate());
             }
     };
     
     
     public static List<ScoreBoardEntry> postFilter(
             List<ScoreBoardEntry> entries) {
         
         // HACK: most inefficient shit that has ever existed
         Collections.sort(entries, new Comparator<ScoreBoardEntry>() {
             @Override
             public int compare(ScoreBoardEntry o1, ScoreBoardEntry o2) {
                 return o1.getVenadName().compareTo(o2.getVenadName());
             }
         });
         
         
         String name = null;
         Collection<ScoreBoardEntry> tmp = new ArrayList<ScoreBoardEntry>();
         List<ScoreBoardEntry> result = new ArrayList<ScoreBoardEntry>();
         
         for (ScoreBoardEntry e : entries) {
             if (name != null && !name.equals(e.getVenadName())) {
                 if (tmp.isEmpty()) { // ?????
                     tmp.clear();
                     continue;
                 }
                 
                 // tmp now contains all entries with same name
                 ScoreBoardEntry oldest = tmp.iterator().next();
                 ScoreBoardEntry youngest = tmp.iterator().next();
                 
                 for (ScoreBoardEntry e1 : tmp) {
                     if (e1.getDate().getTime() < oldest.getDate().getTime()) {
                         oldest = e1;
                     }
                     if (e1.getDate().getTime() > youngest.getDate().getTime()) {
                         youngest = e1;
                     }
                 }
                 
                 long diff = Math.abs(
                         youngest.getDate().getTime() - oldest.getDate().getTime());
                 long days = Milliseconds.toFullDays(diff);
                 int pointDiff = youngest.getPoints() - oldest.getPoints();
                 
                 double pointsPerDay = Double.NaN;
                 if (days != 0) {
                     pointsPerDay = (double) pointDiff / (double)days;
                 }
                 
                 youngest.pointsPerDay = pointsPerDay;
                 youngest.entries = tmp.size();
                 youngest.span = diff;
                 result.add(youngest);
                 tmp.clear();
             }
             tmp.add(e);
             name = e.getVenadName();
         }
         return result;
     }
     
     
     
     public final static String ALL_SBE_DISTINCT = "ALL_SBE_DISTINCT"; //$NON-NLS-1$
     public final static String SBE_BY_USER = "SBE_BY_USER"; //$NON-NLS-1$
     
     public final static DateFormat getCSVDateFormat() {
         return new SimpleDateFormat("dd.MM.yyyy"); //$NON-NLS-1$
     }
     
     @Id@GeneratedValue(strategy = GenerationType.TABLE)
     private int id;
     
     private String venadName;
     
     private String clan;
     
     private int rank;
     
     private int points;
 
     @Temporal(TemporalType.TIMESTAMP)
     private Date date;
     
     private transient double pointsPerDay;
     
     private transient int entries;
     
     private transient long span;
     
     private transient int diffToPrevious;
     
     private transient int daysToPrevious;
     
     private transient double discDerivative;
     
     
     public ScoreBoardEntry() {}
     
     
     public ScoreBoardEntry(String venadName, String clan, int rank, int points, 
             Date date) {
         super();
         this.venadName = venadName;
         this.clan = clan;
         this.rank = rank;
         this.points = points;
         this.date = date;
     }
 
     
     
     public int getId() {
         return this.id;
     }
 
     
     
     public String getVenadName() {
         return this.venadName;
     }
     
     
     
     public String getClan() {
         return this.clan;
     }
 
     
     
     public int getRank() {
         return this.rank;
     }
 
     
     public int getPoints() {
         return this.points;
     }
     
     
     
     public Date getDate() {
         return this.date;
     }
     
     
     
     public double getPointsPerDay() {
         return this.pointsPerDay;
     }
     
     
     
     public int getEntries() {
         return this.entries;
     }
     
     
     
     public long getSpan() {
         return this.span;
     }
     
     
     
     public void setDiscDerivative(double discDerivative) {
         this.discDerivative = discDerivative;
     }
     
     
     
     public double getDiscDerivative() {
         return this.discDerivative;
     }
     
     
     
     public int getDiffToPrevious() {
         return this.diffToPrevious;
     }
     
     
     
     public void setDiffToPrevious(int diffToPrevious) {
         this.diffToPrevious = diffToPrevious;
     }
     
     
     
     public int getDaysToPrevious() {
         return this.daysToPrevious;
     }
     
     
     
     public void setDaysToPrevious(int daysToPrevious) {
         this.daysToPrevious = daysToPrevious;
     }
 }
