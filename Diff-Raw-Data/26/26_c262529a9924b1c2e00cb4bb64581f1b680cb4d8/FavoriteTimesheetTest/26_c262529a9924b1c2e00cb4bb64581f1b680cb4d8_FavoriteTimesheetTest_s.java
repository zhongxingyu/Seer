 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.Country;
 import com.thoughtworks.twu.persistence.FavoriteTimesheet;
 import com.thoughtworks.twu.service.CountryService;
 import com.thoughtworks.twu.service.FavoriteTimesheetService;
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class FavoriteTimesheetTest {
     @Test
     public void shouldReceiveACountryList() throws Exception {
         //Given
         CountryService countryService = new CountryService();
         //When
         List<Country> countries = countryService.getCountries();
         //Then
         assertThat(countries.size(), is(5));
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
        FavoriteTimesheet favoriteTimesheet= new FavoriteTimesheet("TWU");
        //When
        int sizeBeforeInsert = favoriteTimesheetService.getFavoriteTimesheets().size();
        favoriteTimesheetService.save(favoriteTimesheet);
        int sizeAfterInsert = favoriteTimesheetService.getFavoriteTimesheets().size();
        //Then
        assertThat(sizeAfterInsert,is(sizeBeforeInsert+1));
    }
 }
