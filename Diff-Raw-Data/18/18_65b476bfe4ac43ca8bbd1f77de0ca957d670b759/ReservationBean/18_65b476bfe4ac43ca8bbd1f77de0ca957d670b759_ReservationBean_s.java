 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package reserv.beans;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.persistence.EntityManager;
 import reserv.config.DBManager;
 import reserv.entity.Reservation;
 import reserv.entity.Seance;
 import reserv.objects.Place;
 
 
 /**
  *
  * @author muody
  */
 
 
 
 
 @ManagedBean(name="ReservationBean")
 @SessionScoped
 public class ReservationBean {
 
     private String placeNumber;
     private Integer seanceId;
     
     public String checkStep(Integer step){
        System.out.println("CO JEST?");
        System.out.println(seanceId);
        System.out.println(placeNumber);
         
         Boolean status = true;
         String returning = "";
         
         if(step == 1){
             System.out.println("step 1");
             if(seanceId <= 0){
                 status = false;
                 returning = "seances.xhtml";
             }
         }
         else if(step == 2){
             System.out.println("step 2");
             if(seanceId <= 0 || Integer.parseInt(placeNumber) < 0 ){
                 status = false;
                 returning = "reservations.xhtml";
             }
         }
         
         
         
         if(!status){
             try {
                 System.out.println("REDIREXCT");
                 FacesContext.getCurrentInstance().getExternalContext().redirect(returning);
             } catch (IOException ex) {
                 Logger.getLogger(ReservationBean.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return "";
     }
 
     public String getPlaceNumber() {
         return placeNumber;
     }
 
     public void setPlaceNumber(ActionEvent event) {
         String name = (String) event.getComponent().getAttributes().get("name");  
         this.placeNumber=name;
         System.out.println(name);
     }
 
     public Integer getSeanceId() {
         return seanceId;
     }
 
     private Reservation reservation = new Reservation();
     
 
     public void setSeanceId(ActionEvent event) {
         String seance_id = (String) event.getComponent().getAttributes().get("seance_id");
         System.out.println(seance_id);
         this.seanceId = Integer.parseInt(seance_id);
     }
 
     public Reservation getReservation() {
         return reservation;
     }
 
     public void setReservation(Reservation reservation) {
         this.reservation = reservation;
     }
     /**
      * Creates a new instance of ReservationBeans
      */
     public ReservationBean() {
         reset();
         
     }
     
     public List<Reservation> getAll(){
         EntityManager em = DBManager.getManager().createEntityManager();
         List<Reservation> list = em.createNamedQuery("Reservation.findBySeanceId").setParameter("seanceId", this.seanceId).getResultList();
         em.close();
         return list;
     }
     
     public void reservPlaces(ActionEvent event)
     {
         
        String name = (String) event.getComponent().getAttributes().get("name"); 
        System.out.println("asd");
        System.out.println(name);
        
         //this.str=name; 
         
     }
     
     public List<Place> getSeancePlaces(){
         
         
         List<Place> placesList = new ArrayList<Place>();
         List<Reservation> reservationList = this.getAll();
         List<Integer> placesReserved = new ArrayList<Integer>();
         
         for (Reservation reserv : reservationList) {
             placesReserved.add(reserv.getPlace());
         }
         
         for (int i=0; i<100; i++)
         {
             Place p = new Place();
             if ( placesReserved.contains(i))
             {
                 p.setReservedPlace(true);        
                 p.setReservation(reservationList.get(placesReserved.lastIndexOf(i)));
             }
             else
             {
                 Reservation r = new Reservation();
                 r.setPlace(i);
                 p.setReservation(r);
             }
             placesList.add(p);
         }
         return placesList;
     }
     
     public String getInfo(){
         
         Seance s = this.getSeance(seanceId);
         String txt = "";
         if(s != null){
             txt = "Rezerwacja dla" + s.getMovie().getName()+", data: "+s.getSeanceDate("yyyy-MM-dd HH:mm");
         }
         
         return txt;
     }
     
     public Seance getSeance(Integer seance_id){
         EntityManager em = DBManager.getManager().createEntityManager();
         List<Seance> s = em.createNamedQuery("Seance.findById").setParameter("id", seanceId).getResultList();
         Seance s2 = null;
         if (!s.isEmpty())
             s2 = s.get(0);
         return s2;
     }
     
     public String create()
     {
         
         System.out.println("Tutaj");
         
         EntityManager em = DBManager.getManager().createEntityManager();
         em.getTransaction().begin();
         try{
             reservation.setId(null);
 
 
             reservation.setPlace(Integer.parseInt(placeNumber));
             UUID token = UUID.randomUUID();
             reservation.setToken(token.toString());
             Seance s = this.getSeance(seanceId);
 
             reservation.setSeance(s);
             System.out.print(placeNumber);
             em.persist(reservation);
             em.getTransaction().commit();    
             em.close();
 
             //this.reservation = new Reservation();
            
         }
         catch(Exception e)
         {
             System.out.println("WYJATEK !!!!!");
             //javax.validation.ConstraintViolationException e
             //System.out.println(e.getConstraintViolations().toString());
             return "seances";
             
         }
         
         
         
         return "orderSummary";
         
         
     }
     
     public String reset(){
         this.reservation = new Reservation();
         seanceId = -1;
         placeNumber = "";
         return "";
     }
     
 }
