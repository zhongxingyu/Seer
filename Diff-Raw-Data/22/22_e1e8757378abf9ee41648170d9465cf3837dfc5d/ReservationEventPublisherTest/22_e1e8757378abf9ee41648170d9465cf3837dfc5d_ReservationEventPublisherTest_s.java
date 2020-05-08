 package nl.surfnet.bod.service;
 
 import static nl.surfnet.bod.domain.ReservationStatus.REQUESTED;
 import static nl.surfnet.bod.domain.ReservationStatus.SCHEDULED;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.util.concurrent.TimeUnit;
 
 import nl.surfnet.bod.support.ReservationFactory;
 
 import org.junit.Test;
 
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
 import com.google.common.util.concurrent.Uninterruptibles;
 
 public class ReservationEventPublisherTest {
 
   private ReservationEventPublisher subject = new ReservationEventPublisher();
 
   @Test
   public void shouldNotGiveAConccurrenModifidationException() throws InterruptedException {
     final int numberOfEvents = 100;
    final Multimap<Long, ReservationStatusChangeEvent> map = ArrayListMultimap.create();
 
     final ReservationListener reservationListener = new ReservationListener() {
       @Override
       public void onStatusChange(ReservationStatusChangeEvent event) {
        map.put(event.getReservation().getId(), event);
       }
     };
 
     Runnable adder = new Runnable() {
       @Override
       public void run() {
         for (int i = 0; i < 100; i++) {
          Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS);
           subject.addListener(reservationListener);
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
 
     Thread adderThread = new Thread(adder);
     Thread notifyThread = new Thread(notifyer);
     notifyThread.start();
     adderThread.start();
 
     adderThread.join();
     notifyThread.join();
 
    assertThat(map.keySet().size(), is(numberOfEvents));
   }
 
 }
