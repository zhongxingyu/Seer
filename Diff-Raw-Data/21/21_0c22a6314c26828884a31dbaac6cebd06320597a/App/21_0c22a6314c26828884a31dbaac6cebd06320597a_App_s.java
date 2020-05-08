 package cz.fjfi.pvs.kosapi;
 
 import cz.fjfi.pvs.kosapi.chart.PieChart3D;
import cz.fjfi.pvs.kosapi.parser.AtomContentParser;
import cz.fjfi.pvs.kosapi.parser.AtomParser;
import cz.fjfi.pvs.kosapi.parser.AtomTagParser;
 import cz.fjfi.pvs.kosapi.statistic.ExamVsNonexamStatistic;
 import cz.fjfi.pvs.kosapi.statistic.TeachersPerDivisionStatistic;
 import cz.fjfi.pvs.kosapi.web.KosAtomReader;
 import java.net.URL;
import java.util.Iterator;
import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.Logger;
 
 
 public class App
 {
     static Logger logger = Logger.getLogger(App.class);
     
     public static void main( String[] args )
     {   
         loadLoggerProperties();
         logger.info("Starting application");
         try 
         {
             KosAtomReader coursesReader = new KosAtomReader("courses");
             String coursesResponse = coursesReader.getKosResponse();
             ExamVsNonexamStatistic examsVsNonexams = new ExamVsNonexamStatistic(coursesResponse, "completion");
             examsVsNonexams.printStatisticValues();
 
             Map<String, Double> examsStatisticValues = examsVsNonexams.getStatisticValues();
             PieChart3D examsChart = new PieChart3D(examsStatisticValues, "Courses chart");
             examsChart.saveAsPNG("tmp/examsVsNonexams");
             
             KosAtomReader teachersReader = new KosAtomReader("teachers");
             String teachersResponse = teachersReader.getKosResponse();
             TeachersPerDivisionStatistic teachersPerDivision = new TeachersPerDivisionStatistic(teachersResponse, "division");
             teachersPerDivision.printStatisticValues();
             
             Map<String, Double> teachersStatisticValues = teachersPerDivision.getStatisticValues();
             PieChart3D teachersChart = new PieChart3D(teachersStatisticValues, "Teachers Per Division chart");
             teachersChart.saveAsPNG("tmp/teachersPerDivision");
         } 
         catch (Exception e) 
         {
             logger.error(e.getMessage(), e);
         }
     }
     
     public static void loadLoggerProperties () {
         URL log4Jresource = App.class.getResource("logger.properties");
         PropertyConfigurator.configure(log4Jresource);
     }
 }
