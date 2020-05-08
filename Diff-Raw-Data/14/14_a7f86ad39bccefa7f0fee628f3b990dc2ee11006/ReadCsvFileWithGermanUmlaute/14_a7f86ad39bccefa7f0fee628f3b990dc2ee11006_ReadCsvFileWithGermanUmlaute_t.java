 package integration;
 
 import de.jhamel.csv.CsvLineProcessor;
 import de.jhamel.csv.CsvReader;
 import org.junit.Test;
 
import java.io.File;

 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 
 public class ReadCsvFileWithGermanUmlaute {
     @Test
     public void readUmlaute() {
 
         CsvReader csvReader = new CsvReader(new CsvLineProcessor() {
             public void processLine(String[] line) {
                 assertThat(line[0], equalTo("äbc"));
             }
         });
        try {
            csvReader.readFile(FixtureConstants.CSV_FILE_WITH_GERMAN_UMLAUTE);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
     }
 }
