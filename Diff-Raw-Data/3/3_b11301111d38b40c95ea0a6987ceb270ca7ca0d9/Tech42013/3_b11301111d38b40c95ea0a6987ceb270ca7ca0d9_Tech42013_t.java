 package tech4;
 
 import org.primefaces.model.chart.PieChartModel;
 import org.springframework.stereotype.Component;
 import org.springframework.web.context.request.SessionScope;
 
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 import java.util.*;
 
 /**
  *  Demo Backingbean fuer Tech 4
  *
  */
 @Component
 public class Tech42013 {
 
     static int counter = 0;
 
     private String pruefziffer = "  version 0.0   ";
 
     private static Map<String,Integer> users = new HashMap<String,Integer>();
 
     private static Set<String> agents = new HashSet<String>();
 
     public static LinkedList<String> kommentare = new LinkedList<String>();
 
     public synchronized int getCounter() {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
 
         String names = request.getHeader("user-agent");
         String sh = "andere";
 
         agents.add(names);
 
 
         if(names.toLowerCase().contains("samsung")){
             sh = "Samsung";
         }
         else
         if(names.contains("iPhone")){
             sh = "IPhone";
         }
         else
 
         if(names.contains("iPad")){
             sh = "IPad";
         }
           else
         if(names.contains("Mac OS X")){
 
             if(names.contains("Chrome")){
                 sh = "Mac OS X: Chrome";
             } else
             if(names.contains("Safari")){
                 sh = "Mac OS X: Safari";
             } else
             if(names.contains("Firefox")){
                 sh = "Mac OS X: Firefox";
             }  else{
                 sh = "Mac OS X: Unbekannt";
             }
 
         }         else  if(names.contains("Windows")){
 
             if(names.contains("Chrome")){
                 sh = "Windows: Chrome";
             } else
             if(names.contains("Safari")){
                 sh = "Windows: Safari";
             } else
             if(names.contains("Firefox")){
                 sh = "Windows: Firefox";
             }  else{
                 sh = "Windows: Unbekannt";
             }
 
         }
 
         Integer inte = users.get(sh);
 
         if(inte == null){
             users.put(sh,Integer.valueOf(1));
         } else{
             inte = inte +1;
             users.put(sh,inte);
         }
 
 
 
         // Mac OS + Safari
         // Mac OS + Chrome
 
       Tech42013.counter = Tech42013.counter + 1;
         return Tech42013.counter;
     }
 
     public void updateList(String komm) {
         if (komm != null && !komm.equals("")) {
             kommentare.add(0,komm);
             if (kommentare.size() > 10) {
                 kommentare.removeLast();
             }
             kommentar = null;
         }
     }
 
     public synchronized void save() {
         HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
         String txtProperty = request.getParameter("form:text");
         updateList(txtProperty);
     }
 
     public List<String> getKommentare() {
         return (List<String>) kommentare;
     }
 
     private String kommentar = "";
 
     public synchronized String getKommentar() {
         return kommentar;
     }
 
     public void setKommentar(String kommentar) {
         this.kommentar = kommentar;
     }
 
     public String getPruefziffer() {
         return pruefziffer;
     }
 
 
     public PieChartModel getPieModel() {
         PieChartModel pieModel = new PieChartModel();
 
 
         for(String name : this.users.keySet()){
                 pieModel.set(name, users.get(name));
 
         }
           return pieModel;
 
     }
 
     public List<String> getAgents(){
         List<String> list = new ArrayList<String>();
 
 
         for(String agent : agents){
             list.add(agent);
         }
 
         return list;
     }
 
 
 
 
 }
