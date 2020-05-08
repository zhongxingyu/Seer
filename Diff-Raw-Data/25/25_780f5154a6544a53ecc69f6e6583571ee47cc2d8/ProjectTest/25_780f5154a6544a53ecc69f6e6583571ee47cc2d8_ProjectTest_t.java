 package models;
 
 import org.junit.Test;
 import java.util.Calendar;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class ProjectTest {
 
     @Test
     public void shouldCalculateDaysLeft() {
         Calendar creationDate = Calendar.getInstance();
         creationDate.set(2012, 11, 1);
         Calendar endDateCalendar = Calendar.getInstance();
         endDateCalendar.set(2012, 12, 10);
         Project project = new Project(0,"DummyProject", creationDate.getTime(), endDateCalendar.getTime(), 0.0,
                 ProjectStatus.CURRENT, "this is a dummy project", "description", "image/children.jpg", "image/children.jpg", (long)1 );
         assertThat(project.leftDays(), is(40));
     }
 
     @Test
     public void shouldCalculateCurrentProjectAmount() throws Exception {
         Project project1 = new Project();
        Double currentAmount = project1.totalDonation();
         assertThat(currentAmount, is(0.0));
     }
 
     @Test
     public void shouldCalculateTotalDonationOf1000BasedOnDonationOf1000() throws Exception {
         Project project1 = new Project();
         Donation donation = new Donation(project1, 1000.0);
         project1.addDonation(donation);
        Double currentAmount = project1.totalDonation();
         assertThat(currentAmount, is(1000.0));
     }
 
     @Test
     public void shouldCalculateTotalDonationOf1500BasedOnDonationOf1000AndDonationOf500() throws Exception {
         Project project1 = new Project();
         Donation donation1 = new Donation(project1, 1000.0);
         Donation donation2 = new Donation(project1, 500.0);
         project1.addDonation(donation1);
         project1.addDonation(donation2);
        Double currentAmount = project1.totalDonation();
         assertThat(currentAmount, is(1500.0));
     }
 
    @Test
     public void shouldCaculateDonationPercentage() {
         double targetAmount = 2000.0;
         Project project = new Project(0,"DummyProject", Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), targetAmount,
                 ProjectStatus.CURRENT, "this is a dummy project", "description", "image/children.jpg", "image/children.jpg", (long)1 );
         Donation donation = new Donation(project, 1000.0);
         project.addDonation(donation);
         assertThat(project.donationPercentage(), is(50.0));
     }
 
     @Test
     public void shouldCalculateDonationPercentageDouble() {
         double targetAmount = 2040.0;
         Project project = new Project(0,"DummyProject", Calendar.getInstance().getTime(), Calendar.getInstance().getTime(), targetAmount,
                 ProjectStatus.CURRENT, "this is a dummy project", "description", "image/children.jpg", "image/children.jpg", (long)1 );
         Donation donation = new Donation(project, 456.0);
         project.addDonation(donation);
         assertThat(project.donationPercentage(), is(22.35));
     }
 }
