 package at.ac.tuwien.awdei.integration;
 
 import at.ac.tuwien.awdei.ReuseableScripts;
 import java.util.concurrent.TimeUnit;
 import org.junit.After;
 import static org.junit.Assert.assertEquals;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class RatingTest {
 
     private WebDriver driver;
 
     @Before
     public void setup() {
         driver = new FirefoxDriver();
     }
 
     @After
     public void tearDown() {
         driver.close();
     }
 
     private void sleep() {
         try {
             Thread.sleep(4000);
         } catch (InterruptedException ex) {
         }
     }
 
     @Test
     public void testRating() {
         ReuseableScripts rs = new ReuseableScripts(driver);
         rs.login();
 
         driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
 
         //navigate to the first recipe (center of the home site)
         driver.findElement(By.xpath("//*[@id=\"container\"]/div[3]/div/div/div[1]/div[4]/div/div/div/h2/a")).click();
 
 
         //test the rating feature
 
         //read the currrent number of ratings for this recipe
         int count = Integer.parseInt(driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dt/span[1]")).getText());
         //click the fifth star icon of the rating actions
         driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dd[5]/a")).click();
 
         this.sleep();
 
         //check the new rating count (should be old count + 1) [this time there is no span element]
         String new_count_as_string = driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dt")).getText();
         //increase the old count
         count++;
         //check equals
         assertEquals(new_count_as_string, count + " Bewertungen");
 
 
         //testing the rating again WITHOUT leaving this site -> new rating NOT possible
 
         //getting the current count as integer
         count = Integer.parseInt(new_count_as_string.split(" ")[0]);
         //click the fifth star icon of the rating actions AGAIN
         driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dd[5]/a")).click();
         //check rating count (shouldnt be any different)
         new_count_as_string = driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dt")).getText();
         assertEquals(new_count_as_string, count + " Bewertungen");
 
 
         //testing the rating again WITH leaving this site -> new rating possible [kind of funny ;)]
 
         //history back to main site
         driver.navigate().back();
         //history forward to last seen and rated recipe
         driver.navigate().forward();
 
         //read the currrent number of ratings for this recipe
         count = Integer.parseInt(driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dt/span[1]")).getText());
         //click the fifth star icon of the rating actions
         driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dd[5]/a")).click();
 
         this.sleep();
 
         //check the new rating count (should be old count + 1) [this time there is no span element]
         new_count_as_string = driver.findElement(By.xpath("//*[@id=\"recipe_grades\"]/dl/dt")).getText();
         //increase the old count
         count++;
         //check equals
         assertEquals(new_count_as_string, count + " Bewertungen");
 
     }
 
     @Test
     public void testFirstnameSave() {
         ReuseableScripts rs = new ReuseableScripts(driver);
         rs.login();
 
         driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
 
         //navigate to profile site
         driver.findElement(By.xpath("//*[@id=\"header\"]/ul/li[1]/a[1]")).click();
 
         //clears the textfield
         driver.findElement(By.xpath("//*[@id=\"id_fw3k2_form_user_firstname\"]")).clear();
 
         //set fristname to "awdei-ss13"
         driver.findElement(By.xpath("//*[@id=\"id_fw3k2_form_user_firstname\"]")).sendKeys("awdei-ss13");
 
         //click save button
         driver.findElement(By.xpath("//*[@id=\"form_user_profile\"]/div/button/span")).click();
 
         this.sleep();
 
         //read the result message
         String result = driver.findElement(By.xpath("//*[@id=\"user_tab_cont\"]/div[2]/h2")).getText();
 
         //checks for the result message
         assertEquals("Profil aktualisiert", result);
 
         //reads the new saved firstname
         String firstname = driver.findElement(By.xpath("//*[@id=\"id_fw3k2_form_user_firstname\"]")).getAttribute("value");
 
         System.out.println("---" + firstname + "---");
 
         //checks the firstname
        assertEquals("awdei-ss13", firstname);
 
     }
 }
