 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package utilities.statistics;
 
 import controller.traffic.StatsPMVController;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import model.traffic.ItineraryStats;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartFrame;
 import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
 
 
 
 
 
 
 /**
 *
  * @author mael
  */
 public class Stats {
    
     
     /**
      * Construct an XY graph. In X we have time (hour), in Y we have time (minutes) 
      * to go at the destination of the itinerary.
      * @param int id, the id of itineray
      * @param String d1, the start date for the graph
      * @param String d2, the end daye for the graph
      */
     public static void ItineraryStatsXYSeries(int id, String d1, String d2) throws SQLException, ParseException{
         
         StatsPMVController statsContr = new StatsPMVController();     
         List<ItineraryStats> its = statsContr.getItinerariesStats(id, d1, d2);
         
         //Remove doublons for statistics
         Set<ItineraryStats> itsSet = new TreeSet();
         itsSet.addAll(its);
         
         TimeSeries series = new TimeSeries("Itinerary stats");
 
         
         for(ItineraryStats it : itsSet){
            SimpleDateFormat simpledate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
             Date h = simpledate.parse(it.getDate() + " " + it.getHour());
             System.out.println(h.toString());
            series.add(new Millisecond(h),it.getTime());
         }
         
         TimeSeriesCollection data = new TimeSeriesCollection();
         data.addSeries(series);
         
          JFreeChart chart = ChartFactory.createTimeSeriesChart(
             "Itinéraire " + id + " (" + d1 + ")",
             "Heure de la journée", 
             "Temps de parcours (minutes)", 
             data,
             false,
             true,
             false
         );
 
             //create and display a frame...
             ChartFrame frame=new ChartFrame("First",chart);
             frame.pack();
             frame.setVisible(true);
         
         
     }
     
     
     public static void main(String args[]){
         try {
             try {
                Stats.ItineraryStatsXYSeries(11,"2012-10-15","2012-10-17");
             } catch (ParseException ex) {
                 Logger.getLogger(Stats.class.getName()).log(Level.SEVERE, null, ex);
             }
         } catch (SQLException ex) {
             Logger.getLogger(Stats.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
