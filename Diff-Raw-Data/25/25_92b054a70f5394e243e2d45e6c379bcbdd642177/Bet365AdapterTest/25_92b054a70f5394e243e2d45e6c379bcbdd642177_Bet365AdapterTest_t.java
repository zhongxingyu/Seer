 package data.adapter;
 
 import data.AdaptedEvent;
 import data.parser.ParsedEvent;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import static java.util.Calendar.DAY_OF_MONTH;
 import static java.util.Calendar.HOUR_OF_DAY;
 import static java.util.Calendar.MILLISECOND;
 import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
 import static java.util.Calendar.OCTOBER;
 import static java.util.Calendar.SECOND;
 import static java.util.TimeZone.getTimeZone;
 import static models.store.Organisation.BET365;
 import static org.fest.assertions.Assertions.assertThat;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({Date.class, AdaptedEvent.class})
 public class Bet365AdapterTest {
 
   @Test
   public void parse()
     throws Exception {
    Date adoptedDate = new Date();
    PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(adoptedDate);
 
     ParsedEvent event = new ParsedEvent("Florian Mayer", "Kristina Mladenovic", "11 Oct 07:30", "1.50", "3.28");
     AdaptedEvent adaptedEvent = new Bet365Adapter().adapt(event);
 
     Calendar calendar = Calendar.getInstance(getTimeZone("GMT+1"));
    calendar.set(MONTH, OCTOBER);
     calendar.set(DAY_OF_MONTH, 11);
     calendar.set(HOUR_OF_DAY, 7);
     calendar.set(MINUTE, 30);
     calendar.set(SECOND, 0);
     calendar.set(MILLISECOND, 0);
     assertThat(adaptedEvent.date).isEqualTo(calendar.getTime());
 
     assertThat(adaptedEvent.organisation).isEqualTo(BET365);
     assertThat(adaptedEvent.firstSide).isEqualTo("Florian Mayer");
     assertThat(adaptedEvent.secondSide).isEqualTo("Kristina Mladenovic");
     assertThat(adaptedEvent.firstKof).isEqualTo(1.5);
     assertThat(adaptedEvent.secondKof).isEqualTo(3.28);
    assertThat(adaptedEvent.adoptedDate).isSameAs(adoptedDate);
   }
 
   @Test
   public void codeAndOrder()
     throws Exception {
     ParsedEvent event = new ParsedEvent("Florian Del Mayer", "Aisam-Ul Haq Qureshi & Jean-Julien Rojer", "11 Oct 07:30", "3.28", "1.50");
     AdaptedEvent adaptedEvent = new Bet365Adapter().adapt(event);
 
     Calendar calendar = Calendar.getInstance(getTimeZone("GMT+1"));
    calendar.set(MONTH, OCTOBER);
     calendar.set(DAY_OF_MONTH, 11);
     calendar.set(HOUR_OF_DAY, 7);
     calendar.set(MINUTE, 30);
     calendar.set(SECOND, 0);
     calendar.set(MILLISECOND, 0);
 
     String eventCode = "qureshi,rojer_mayer_" + new SimpleDateFormat("yyyy-MM-dd mm").format(calendar.getTime());
 
     assertThat(adaptedEvent.code).isEqualTo(eventCode);
   }
 
   @Test
   public void order()
     throws Exception {
     ParsedEvent event = new ParsedEvent("Kristina Mladenovic", "Florian Mayer", "11 Oct 07:30", "3.28", "1.50");
     AdaptedEvent adaptedEvent = new Bet365Adapter().adapt(event);
 
     assertThat(adaptedEvent.firstSide).isEqualTo("Florian Mayer");
     assertThat(adaptedEvent.secondSide).isEqualTo("Kristina Mladenovic");
     assertThat(adaptedEvent.firstKof).isEqualTo(1.5);
     assertThat(adaptedEvent.secondKof).isEqualTo(3.28);
   }
 
   @Test
   public void nunUnicodeCharacters()
     throws Exception {
     ParsedEvent event = new ParsedEvent("Florian Hradečka", "Kristina Hradečka", "11 Oct 07:30", "1.50", "3.28");
     AdaptedEvent adaptedEvent = new Bet365Adapter().adapt(event);
 
     assertThat(adaptedEvent.firstSide).isEqualTo("Florian Hradecka");
     assertThat(adaptedEvent.secondSide).isEqualTo("Kristina Hradecka");
   }
 
   @Test
   public void date24hours()
     throws Exception {
     ParsedEvent event = new ParsedEvent("Florian Mayer", "Kristina Mladenovic & Flavia Pennetta", "11 Oct 12:30", "3.50", "1.28");
     AdaptedEvent adaptedEvent = new Bet365Adapter().adapt(event);
 
     Calendar calendar = Calendar.getInstance(getTimeZone("GMT+1"));
    calendar.set(MONTH, OCTOBER);
     calendar.set(DAY_OF_MONTH, 11);
     calendar.set(HOUR_OF_DAY, 12);
     calendar.set(MINUTE, 30);
     calendar.set(SECOND, 0);
     calendar.set(MILLISECOND, 0);
     assertThat(adaptedEvent.date).isEqualTo(calendar.getTime());
   }
 }
