 package cz.cvut.fel.beans;
 
 import cz.cvut.fel.model.Reservation;
 import cz.cvut.fel.service.PaymentService;
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.validation.constraints.Max;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 
 /** @author Karel Cemus */
 @Data
 @ViewScoped
 @Named( "sendBackBean" )
 @EqualsAndHashCode( callSuper = false )
 public class SendBackBean extends BeanBase implements Serializable {
 
     @Inject
     private ReservationBean reservationBean;
 
     @Inject
     private PaymentService service;
 
     private long account;
 
     private int bank;
 
     private Reservation reservation;
 
     @NotNull( message = "You are not logged in, no reservation is attached" )
     public Reservation getReservation() {
         return reservation;
     }
 
 
    @Min( value = 100000L, message = "Invalid account number, range must be 6-10 digits." )
     @Max( value = 9999999999L, message = "Invalid account number, range must be 6-10 digits." )
     public long getAccount() {
         return account;
     }
 
     @Min( value = 1, message = "Invalid bank code." )
     @Max( value = 9999, message = "Invalid bank code." )
     public int getBank() {
         return bank;
     }
 
     public String sendBack() {
 
         try {
             // try to make transaction
             service.returnMoney( reservation.getId(), reservationBean.getPassword(), account, bank );
 
             // log success
             addInformation( "Transaction accepted." );
 
             // reload session scoped data
             reservationBean.reload();
 
             // redirect back to reservation overview
             return "reservation";
 
         } catch ( Throwable ex ) {
             addError( processException( ex ) );
             return null;
         }
     }
 
     @PostConstruct
     public void init() {
 
         // load reservation
         if ( reservationBean != null && reservationBean.getReservation() != null ) {
             reservation = reservationBean.getReservation();
         }
     }
 
 }
