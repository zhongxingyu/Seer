 package nl.surfnet.bod.service;
 
 import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
 import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
import java.util.HashSet;
import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import nl.surfnet.bod.support.ReservationFactory;
 
 import org.junit.Test;
 
 import com.google.common.util.concurrent.Uninterruptibles;
 
 public class ReservationEventPublisherTest {
 
   private ReservationEventPublisher subject = new ReservationEventPublisher();
 
   @Test
   public void shouldNotGiveAConccurrenModifidationException() throws InterruptedException {
     final int numberOfEvents = 100;
    final Set<Long> set = new HashSet<>();
 
     final ReservationListener reservationListener = new ReservationListener() {
       @Override
       public void onStatusChange(ReservationStatusChangeEvent event) {
        set.add(event.getReservation().getId());
       }
     };
 
     Runnable adder = new Runnable() {
       @Override
       public void run() {
         for (int i = 0; i < 100; i++) {
           subject.addListener(reservationListener);
          Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS);
         }
       }
     };
 
     Runnable notifyer = new Runnable() {
       @Override
       public void run() {
         for (int i = 0; i < numberOfEvents; i++) {
           subject.notifyListeners(new ReservationStatusChangeEvent(REQUESTED, new ReservationFactory().setStatus(SCHEDULED).create()));
         }
       }
     };
 
    // make sure we have at least on listener
    subject.addListener(reservationListener);

     Thread adderThread = new Thread(adder);
     Thread notifyThread = new Thread(notifyer);
     notifyThread.start();
     adderThread.start();
 
     adderThread.join();
     notifyThread.join();
 
    assertThat(set.size(), is(numberOfEvents));
   }
 
 }
