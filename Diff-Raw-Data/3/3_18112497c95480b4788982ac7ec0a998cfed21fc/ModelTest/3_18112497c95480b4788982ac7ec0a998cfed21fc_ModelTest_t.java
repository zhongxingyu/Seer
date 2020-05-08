 import static org.fest.assertions.Assertions.assertThat;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.inMemoryDatabase;
 import static play.test.Helpers.start;
 import static play.test.Helpers.stop;
 
 import java.util.Date;
 
 import models.Company;
 import models.Computer;
 
import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.FakeApplication;
 
 import com.avaje.ebean.Page;
 
 public class ModelTest {
 
     private FakeApplication app;
 
     @Before
     public void setup() {
         app = fakeApplication(inMemoryDatabase());
         start(app);
     }
 
    @After
     public void teardown() {
         stop(app);
     }
 
     @Test
     public void createAndLoad() {
         final Company company = Company.find.all().get(0);
         Computer computer = new Computer("c64", new Date(), company);
         computer.save();
         assertThat(computer.id).isNotNull();
         Computer c64 = Computer.find.byId(computer.id);
         assertThat(c64.name).isEqualTo(computer.name);
     }
 
     @Test
     public void pagination() {
         Page<Computer> computers = Computer.page(0, 20, "id", "asc", "");
         assertThat(computers.getTotalRowCount()).isEqualTo(574);
         assertThat(computers.getList().size()).isEqualTo(20);
         assertThat(computers.getList().get(0).company).isNotNull();
     }
 
 }
