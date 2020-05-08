 package com.lavida.service.remote.google;
 
 import com.google.gdata.client.spreadsheet.CellQuery;
 import com.google.gdata.client.spreadsheet.FeedURLFactory;
 import com.google.gdata.client.spreadsheet.SpreadsheetService;
 import com.google.gdata.data.spreadsheet.CellEntry;
 import com.google.gdata.data.spreadsheet.CellFeed;
 import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
 import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.lavida.service.settings.Settings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 /**
  * GoogleSpreadsheetWorker
  * Created: 22:15 15.08.13
  *
  * @author Pavel
  */
 public class GoogleSpreadsheetWorker {
     private static final Logger logger = LoggerFactory.getLogger(GoogleSpreadsheetWorker.class);
     private static final String APPLICATION_NAME = "LA VIDA Finance.";
 
     private SpreadsheetService spreadsheetService = new SpreadsheetService(APPLICATION_NAME);
     private URL worksheetUrl;
 
     public GoogleSpreadsheetWorker(Settings settings) throws ServiceException, IOException {
         loginToGmail(spreadsheetService, settings.getRemoteUser(), settings.getRemotePass());
         List spreadsheets = getSpreadsheetList(spreadsheetService);
         SpreadsheetEntry spreadsheet = getSpreadsheetByName(spreadsheets, settings.getSpreadsheetName());
         worksheetUrl = getWorksheetUrl(spreadsheet, settings.getWorksheetNumber());
     }
 
     public CellFeed getWholeDocument() throws IOException, ServiceException {
         return spreadsheetService.getFeed(worksheetUrl, CellFeed.class);
     }
 
     public CellFeed getCellsInRange(Integer minRow, Integer maxRow, Integer minCol, Integer maxCol) throws IOException, ServiceException {
         CellQuery query = new CellQuery(worksheetUrl);
         query.setMinimumRow(minRow);
         query.setMaximumRow(maxRow);
         query.setMinimumCol(minCol);
         query.setMaximumCol(maxCol);
         return spreadsheetService.query(query, CellFeed.class);
     }
 
     public void saveOrUpdateCells(List<CellEntry> cellEntries) throws IOException, ServiceException {
         for (CellEntry cellEntry : cellEntries) {
             spreadsheetService.insert(worksheetUrl, cellEntry);
         }
     }
 
     public CellFeed getRow(int row) throws IOException, ServiceException {
         return getCellsInRange(row, row, null, null);
     }
 
     private void loginToGmail(SpreadsheetService spreadsheetService, String username, String password) throws AuthenticationException {
         spreadsheetService.setUserCredentials(username, password);
     }
 
     private List getSpreadsheetList(SpreadsheetService spreadsheetService) throws IOException, ServiceException {
         FeedURLFactory factory = FeedURLFactory.getDefault();
         SpreadsheetFeed feed = spreadsheetService.getFeed(factory.getSpreadsheetsFeedUrl(), SpreadsheetFeed.class);
         return feed.getEntries();
     }
 
     private SpreadsheetEntry getSpreadsheetByName(List spreadsheets, String spreadsheetName) {
         for (Object spreadsheetObject : spreadsheets) {
             if (spreadsheetObject instanceof SpreadsheetEntry) {
                 SpreadsheetEntry spreadsheet = (SpreadsheetEntry) spreadsheetObject;
                 String sheetTitle = spreadsheet.getTitle().getPlainText();
                if (sheetTitle != null && sheetTitle.equals(spreadsheetName)) {
                     return spreadsheet;
                 }
 
             } else {
                 logger.warn("Found spreadsheet which is not SpreadsheetEntry instance: " + spreadsheetObject);
             }
         }
         throw new RuntimeException("No spreadsheet found with name");
         // todo change exception
     }
 
     private URL getWorksheetUrl(SpreadsheetEntry spreadsheet, int worksheetNumber) throws IOException, ServiceException {
         return spreadsheet.getWorksheets().get(worksheetNumber).getCellFeedUrl();
     }
 }
