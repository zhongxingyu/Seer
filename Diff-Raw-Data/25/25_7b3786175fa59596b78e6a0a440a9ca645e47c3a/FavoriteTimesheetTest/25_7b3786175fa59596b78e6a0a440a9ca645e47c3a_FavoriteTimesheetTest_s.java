 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.persistence.Country;
 import com.thoughtworks.twu.persistence.FavoriteTimesheet;
 import com.thoughtworks.twu.persistence.HibernateConnection;
 import com.thoughtworks.twu.service.CountryService;
 import com.thoughtworks.twu.service.FavoriteTimesheetService;
 import org.junit.*;
 import org.springframework.jdbc.datasource.embedded.*;
 
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class FavoriteTimesheetTest {
 
     private static EmbeddedDatabase db;
 
     public FavoriteTimesheetTest() {
 
     }
 
     @BeforeClass
     public static void beforeClass() throws Exception {
         EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
         db = builder.setType(EmbeddedDatabaseType.H2).setName("test").
                 addScript("/twu_database/schema.sql").
                 addScript("/twu_database/import.sql").build();
     }
 
     @AfterClass
     public static void afterClass() throws Exception {
         if ( db != null && !db.getConnection().isClosed())
             db.shutdown();
         if ( HibernateConnection.getInstance().getSession().isConnected() )
             HibernateConnection.getInstance().getSession().close();
     }
 
    @After
    public void tearDown() throws Exception {


    }

     @Test
     public void shouldReceiveACountryList() throws Exception {
         //Given
         CountryService countryService = new CountryService();
         //When
         List<Country> countries = countryService.getCountries();
         //Then
         assertThat(countries.size(), is(239));
     }
 
     @Test
     public void shouldRetrieveMyFavoriteTimesheets()
     {
         //Given
         FavoriteTimesheetService timesheetService = new FavoriteTimesheetService();
         //When
         List<FavoriteTimesheet> timesheets = timesheetService.getFavoriteTimesheets();
         //Then
         assertNotNull(timesheets);
     }
 
     @Test
     public void shouldSaveMyFavoriteTimesheet()
     {
         //Given
         FavoriteTimesheetService favoriteTimesheetService= new FavoriteTimesheetService();
         FavoriteTimesheet favoriteTimesheet= new FavoriteTimesheet("TW");
         //When
         int sizeBeforeInsert = favoriteTimesheetService.getFavoriteTimesheets().size();
         favoriteTimesheetService.save(favoriteTimesheet);
         int sizeAfterInsert = favoriteTimesheetService.getFavoriteTimesheets().size();
         //Then
         assertThat(sizeAfterInsert,is(sizeBeforeInsert+1));
 
         HibernateConnection.getInstance().getSession().delete(favoriteTimesheet);
         HibernateConnection.getInstance().getSession().flush();
     }
 }
