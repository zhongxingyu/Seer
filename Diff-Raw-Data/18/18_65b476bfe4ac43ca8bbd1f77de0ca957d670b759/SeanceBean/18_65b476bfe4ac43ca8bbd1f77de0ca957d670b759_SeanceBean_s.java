 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package reserv.beans;
 
 import java.sql.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.persistence.EntityManager;
 import reserv.config.DBManager;
 import reserv.entity.Movie;
 import reserv.entity.Seance;
 import reserv.objects.SeanceHelper;
 
 /**
  *
  * @author muody
  */
 @ManagedBean(name="SeanceBean")
 @RequestScoped
 public class SeanceBean {
     
     private String hour;
     private String day;
     private String month;
     private String movie_name;
 
     public String getMovie_name() {
         return movie_name;
     }
 
     public void setMovie_name(String movie_name) {
         this.movie_name = movie_name;
     }
 
     public String getDay() {
         return day;
     }
 
     public void setDay(String day) {
         this.day = day;
     }
 
     public String getHour() {
         return hour;
     }
 
     public void setHour(String hour) {
         this.hour = hour;
     }
 
     public String getMonth() {
         return month;
     }
 
     public void setMonth(String month) {
         this.month = month;
     }
 
     private Seance seance = new Seance();
 
     public Seance getSeance() {
         return seance;
     }
 
     public void setSeance(Seance seance) {
         this.seance = seance;
     }
     
     /**
      * Creates a new instance of SeanceBean
      */
     public SeanceBean() {
     }
     
     public List<Movie> getTodaySeanceMovies()
     {
         EntityManager em = DBManager.getManager().createEntityManager();
         
        List list = em.createNamedQuery("Seance.findMovies").getResultList();
        
 //        Movie test = (Movie) list.get(0);
 //        
 //        System.err.println(test.getName());
         
         
         //List<Integer> movies = new ArrayList<Integer>();
 //        for (Iterator it = list.iterator(); it.hasNext();) {
 //            Movie object = (Movie) it.next();
 //            movies.add(object.getId());
 //            System.out.println(movies.get(0));
 //        }
         
         em.close();
         return list;
     }
     
     public List<String> allHours()
     {
         return SeanceHelper.getHours();
     }
     public List<String> allDays()
     {
         return SeanceHelper.getDays();
     }
     public List<String> allMonths()
     {
         return SeanceHelper.getMonths();
     }
     
     public String addSeance()
     {
         return "panel";
     }
     
     
 }
