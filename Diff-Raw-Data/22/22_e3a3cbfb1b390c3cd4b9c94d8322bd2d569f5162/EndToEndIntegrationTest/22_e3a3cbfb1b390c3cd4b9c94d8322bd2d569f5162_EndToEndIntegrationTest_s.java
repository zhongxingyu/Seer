 package nl.xebia.si.university.lab4;
 
 import nl.xebia.si.university.kitchen.domain.Meal;
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.integration.Message;
 import org.springframework.integration.core.PollableChannel;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Jeroen van Erp
  */
@ContextConfiguration(locations = "/home-dinner-flow.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 public class EndToEndIntegrationTest {
 
     @Autowired
     private PollableChannel meals;
 
    @Autowired
    private File recipebookLocation;
 
     @Test
     public void shouldCreateAMeal() throws IOException {
         File resource = new ClassPathResource("/pilav.xml").getFile();
        FileUtils.copyFile(resource, new File(recipebookLocation, "pilav.xml"));
 
        Message<?> message = meals.receive(20000);
         Meal meal = (Meal) message.getPayload();
         assertThat(meal.getRecipe().getName(), is("Pilav"));
     }
 }
