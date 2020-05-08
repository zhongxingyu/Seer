 package cz.cvut.fel.beans;
 
 import cz.cvut.fel.model.Flight;
 import cz.cvut.fel.model.Payment;
 import cz.cvut.fel.model.Reservation;
 import cz.cvut.fel.service.PaymentService;
 import cz.cvut.fel.service.ReservationService;
 import lombok.AccessLevel;
 import lombok.Getter;
 import lombok.Setter;
 import org.hibernate.validator.constraints.NotBlank;
 
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.validation.constraints.Min;
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.List;
 
 /** @author Karel Cemus */
 @Setter
 @SessionScoped
 @Named( "reservationBean" )
 public class ReservationBean extends BeanBase implements Serializable {
 
     @Inject
     @Setter( AccessLevel.NONE )
     private ReservationService service;
 
     @Inject
     @Setter( AccessLevel.NONE )
     private PaymentService paymentService;
 
     @Getter
     private Reservation reservation;
 
     @Getter
     private Flight flight;
 
     private long id;
 
     private String password;
 
     private List<Payment> payments;
 
     public String cancel() {
 
         try {
             reservation = service.cancel( id, password );
             addInformation( "Reservation was cancelled." );
         } catch ( Throwable ex ) {
             addError( processException( ex ) );
         }
 
         return "reservation?includeViewParams=true&faces-redirect=true";
     }
 
     public void printETicket() {
 
         try {
             byte[] ticket = service.printETicket( id, password );
             download( "e-ticket.txt", ticket.length, ticket );
         } catch ( Throwable ex ) {
             addError( processException( ex ) );
         }
     }
 
     public void printConfirmation() {
         try {
             byte[] confirmation;
             if ( reservation.isCancelled() ) {
                 confirmation = service.printCancelConfirmation( id, password );
             } else {
                 confirmation = service.printReservationConfirmation( id, password );
             }
             download( "confirmation.txt", confirmation.length, confirmation );
         } catch ( Throwable ex ) {
             addError( processException( ex ) );
         }
     }
 
     public void printPaymentConfirmation( long identifier ) {
         try {
             byte[] confirmation = paymentService.printPaymentConfirmation( identifier );
             download( "confirmation.txt", confirmation.length, confirmation );
         } catch ( Throwable ex ) {
             addError( processException( ex ) );
         }
     }
 
     public String logIn() {
 
         try {
 
             if ( id > 0 && password != null ) {
 
                 // verify login information
                 reservation = service.find( id, password );
                 flight = reservation.getFlight();
             }
 
         } catch ( Throwable ex ) {
 
             // verification failed
             addError( processException( ex ) );
         }
 
         return "reservation?includeViewParams=true&faces-redirect=true";
     }
 
     public String getReservationStatus() {
         if ( reservation.isCancelled() ) {
             return "Cancelled";
         } else if ( reservation.isFullyPaid() ) {
             return "Paid";
         } else {
             return "Active";
         }
     }
 
     public String logout() {
         reservation = null;
         password = null;
         flight = null;
         id = 0;
 
         return "flights?includeViewParams=true&faces-redirect=true";
     }
 
     public boolean isLoggedIn() {
         return reservation != null;
     }
 
     @Min( value = 1l, message = "Invalid identifier. It must be at least 1." )
     public long getId() {
         return id;
     }
 
     @NotBlank( message = "Password is missing." )
     public String getPassword() {
         return password;
     }
 
     public void reload() {
         reservation = service.find( id, password );
         flight = reservation.getFlight();
         payments = null;
     }
 
     public Collection<Payment> getPayments() {
         if ( payments == null ) {
             payments = paymentService.findPayments( id, password );
         }
         return payments;
     }
 }
