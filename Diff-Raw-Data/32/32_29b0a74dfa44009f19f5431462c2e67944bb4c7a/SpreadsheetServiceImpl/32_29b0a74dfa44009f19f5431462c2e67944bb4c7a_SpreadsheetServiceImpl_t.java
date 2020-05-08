 package com.nkhoang.gae.service.impl;
 
 import com.google.gdata.client.spreadsheet.SpreadsheetService;
 import com.google.gdata.data.spreadsheet.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URL;
 import java.util.List;
 
 public class SpreadsheetServiceImpl implements com.nkhoang.gae.service.SpreadsheetService {
     private static final String SPREADSHEET_SERVICE_NAME = "Spreadsheet Search";
     private static final Logger LOGGER = LoggerFactory.getLogger(SpreadsheetService.class);
 
     // singleton service.
     private SpreadsheetService _service;
     // username and password are configured in a property file.
     private String _username;
     private String _password;
 
 
     /**
      * Singleton Google Spreadsheet service. Username and password must be provided from the property file.
      */
     public SpreadsheetService getService() {
         if (_service == null) {
             try {
                 _service = new SpreadsheetService(SPREADSHEET_SERVICE_NAME);
                 _service.setUserCredentials(_username, _password);
                 _service.setProtocolVersion(SpreadsheetService.Versions.V1);
             } catch (Exception e) {
                 // set null to service if we cannot get it from Google.
                 _service = null;
                 LOGGER.error("Could not create google spreadsheet service", e);
             }
         }
         return _service;
     }
 
     /**
      * Find spreadsheet cell url by title.
      *
      * @return url or null.
      */
     public String findSpreadSheetCellUrlByTitle(String title, String worksheetName) {
         String cellfeedUrl = null;
         try {
             SpreadsheetService _service = getService();
 
             URL metafeedUrl = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
             SpreadsheetFeed feed = _service.getFeed(metafeedUrl, SpreadsheetFeed.class);
             List<SpreadsheetEntry> spreadsheets = feed.getEntries();
 
             for (int i = 0; i < spreadsheets.size(); i++) {
                 SpreadsheetEntry spreadsheetEntry = spreadsheets.get(i);
                 String spreadsheetTitle = spreadsheetEntry.getTitle().getPlainText();
                 //LOGGER.info("Spreadsheet title : " + spreadsheetTitle);
                 if (spreadsheetTitle.equals(title)) {
                     //LOGGER.info("\t" + spreadsheetTitle);
                     List<WorksheetEntry> worksheets = spreadsheetEntry.getWorksheets();
                     for (int j = 0; j < worksheets.size(); j++) {
                         WorksheetEntry worksheetEntry = worksheets.get(j);
 
                         String worksheetTitle = worksheetEntry.getTitle().getPlainText();
                         if (worksheetTitle.equals(worksheetName)) {
                             //LOGGER.info(worksheetTitle);
                            URL url = worksheetEntry.getCellFeedUrl();
                            if (url != null) {
                                {
                                    cellfeedUrl = url.toString();
                                }
                            }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             LOGGER.debug("Could not find the requested worksheet [" + worksheetName + "] or spreadsheet with name [" + title + "].");
         }
         return cellfeedUrl;
     }
 
     public String getUsername() {
         return _username;
     }
 
     public void setUsername(String username) {
         _username = username;
     }
 
     public String getPassword() {
         return _password;
     }
 
     public void setPassword(String password) {
         _password = password;
     }
 
     /**
      * Part of getter and setter methods.
      */
     public void setService(SpreadsheetService service) {
         _service = service;
     }
 }
