 package data.parser;
 
 import org.junit.Test;
 
 import java.io.IOException;
 import java.util.List;
 
 import static org.apache.commons.io.IOUtils.toByteArray;
 import static org.fest.assertions.Assertions.assertThat;
 
 public class LiveLanosParserTest {
 
   @Test
   public void parse()
     throws IOException {
     byte[] input = toByteArray(this.getClass().getResourceAsStream("/data/parser/LiveLanosParserTest.html"));
 
     List<ParsedEvent> events = new LiveLanosParser().parse(input);
 
     assertThat(events).hasSize(6);
 
     ParsedEvent firstEvent = events.get(0);
     assertThat(firstEvent.sportDescr).startsWith("Tennis");
     assertThat(firstEvent.firstSide).isEqualTo("Lajovic, Dusan");
     assertThat(firstEvent.secondSide).isEqualTo("Wu, Di");
     assertThat(firstEvent.date).isEqualTo("02:00");
     assertThat(firstEvent.firstKof).isEqualTo("1.59");
     assertThat(firstEvent.secondKof).isEqualTo("2.375");
   }
 }
