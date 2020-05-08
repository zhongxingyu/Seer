 package com.ventyx.sample.application.service;
 
 import com.ventyx.sample.application.api.model.Country;
 import com.ventyx.sample.application.api.model.Language;
 import com.ventyx.sample.application.exception.LocationServiceException;
 
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Service providing location information for states and countries
  */
 
 public class LocationService {
 
     /**
      *  Gets a list of countries supported
      *
      * @return countries
      */
     public List<Country> getCountries() {
 
         List<Country> countries = new ArrayList<Country>();
 
         for (Locale locale : Locale.getAvailableLocales()) {
 
             if ("".equals(locale.getCountry())) {
                 continue;
             }
 
             Country country = new Country();
             country.setCode(locale.getCountry());
             country.setName(locale.getDisplayCountry());
 
             countries.add(country);
         }
 
         return countries;
 
     }
 
     /**
      * Gets the languages for a given country
      *
      * @param countryCode
      * @return languages
      * @throws LocationServiceException
      */
     public List<Language> getLanguages(String countryCode) throws LocationServiceException {
 
         if (countryCode == null || countryCode.isEmpty()) {
             throw new LocationServiceException("A valid country code is required.");
         }
 
         List<Language> languages = new ArrayList<Language>();
 
         for (Locale locale : Locale.getAvailableLocales()) {
 
             if (locale.getCountry().equalsIgnoreCase(countryCode)) {
 
                 Country country = new Country();
                 country.setCode(locale.getCountry());
                 country.setName(locale.getDisplayCountry());
 
                 Language language = new Language();
                 language.setCode(locale.getLanguage());
                 language.setName(locale.getDisplayLanguage());
                 language.setCountry(country);
                 languages.add(language);
 
             }
         }
 
         //only return a valid language collection
         if (languages != null && !languages.isEmpty()) {
             return languages;
         }
 
         return null;    //country was supplied but not found
 
     }
 
     /**
      *
      * Gets a country by code
      *
      * @param countryCode
      * @return Country
      * @throws LocationServiceException
      */
     public Country getCountry(String countryCode) throws LocationServiceException {
 
         if (countryCode == null || countryCode.isEmpty()) {
             throw new LocationServiceException("A valid country code is required.");
         }
 
         Locale[] locales = Locale.getAvailableLocales();
 
         for (Locale locale : locales) {
 
             if (locale.getCountry().equalsIgnoreCase(countryCode)) {
 
                 Country country = new Country();
                 country.setCode(locale.getCountry());
                 country.setName(locale.getDisplayCountry());
                 return country;
             }
         }
 
         return null;    //country was supplied but not found
 
     }
 
 
    public String getLanguageInformation(String languageCode, Collection c) throws Exception{
 
         //VIOLATION
         if ("AA" != "BB") {
             return "English";
         }
 
         //VIOLATION
        String[] invalid = (String[]) c.toArray();

        //VIOLATION
         try {
             if (languageCode == "A") return null;
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             throw new Exception("Should not be here...");
         }
 

     }
 }
