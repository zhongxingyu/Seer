 /*
  * This file is part of props2xls.
  *
  * props2xls is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * props2xls is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with props2xls.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pl.xsolve.props2xls.tools.gdata;
 
 import com.google.gdata.client.spreadsheet.*;
 import com.google.gdata.client.spreadsheet.FeedURLFactory;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.batch.*;
 import com.google.gdata.data.spreadsheet.*;
 import com.google.gdata.util.ServiceException;
 import pl.xsolve.props2xls.tools.ProgressBar;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * An application that serves as a sample to show how the SpreadsheetService
  * can be used to import delimited text file to a spreadsheet.
  */
 public class ImportClient {
 
     public static final String DELIM = ";;;;";
    private static int ITEMS_PER_BATCH = 100;
 
     private SpreadsheetService service;
 
     private SpreadsheetEntry spreadsheet;
 
     private WorksheetEntry backingEntry;
 
     private FeedURLFactory factory;
 
     public ImportClient(String username, String password, int itemsPerBatch, String spreadsheetName) throws Exception {
         ITEMS_PER_BATCH = itemsPerBatch;
 
         factory = FeedURLFactory.getDefault();
         service = new SpreadsheetService("Props-2-Xls");
         service.setUserCredentials(username, password);
         service.setProtocolVersion(SpreadsheetService.Versions.V1);//bug workaround! http://code.google.com/p/gdata-java-client/issues/detail?id=103
 
         spreadsheet = getSpreadsheet(spreadsheetName);
         backingEntry = spreadsheet.getDefaultWorksheet();
 
         CellQuery cellQuery = new CellQuery(backingEntry.getCellFeedUrl());
         cellQuery.setReturnEmpty(true);
         CellFeed cellFeed = service.getFeed(cellQuery, CellFeed.class);
     }
 
     /**
      * Gets the SpreadsheetEntry for the first spreadsheet with that name
      * retrieved in the feed.
      *
      * @param spreadsheet the name of the spreadsheet
      * @return the first SpreadsheetEntry in the returned feed, so latest
      *         spreadsheet with the specified name
      * @throws Exception if error is encountered, such as no spreadsheets with the
      *                   name
      */
     public SpreadsheetEntry getSpreadsheet(String spreadsheet) throws Exception {
 
         SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(factory.getSpreadsheetsFeedUrl());
         spreadsheetQuery.setTitleQuery(spreadsheet);
         SpreadsheetFeed spreadsheetFeed = service.query(spreadsheetQuery, SpreadsheetFeed.class);
         List<SpreadsheetEntry> spreadsheets = spreadsheetFeed.getEntries();
         if (spreadsheets.isEmpty()) {
             throw new Exception("No spreadsheets with that name");
         }
 
         return spreadsheets.get(0);
     }
 
     /**
      * Get the WorksheetEntry for the worksheet in the spreadsheet with the
      * specified name.
      *
      * @param spreadsheetName the name of the spreadsheet
      * @param worksheetName   the name of the worksheet in the spreadsheet
      * @return worksheet with the specified name in the spreadsheet with the
      *         specified name
      * @throws Exception if error is encountered, such as no spreadsheets with the
      *                   name, or no worksheet wiht the name in the spreadsheet
      */
     public Worksheet getWorksheet(String spreadsheetName, String worksheetName) throws Exception {
         SpreadsheetEntry spreadsheetEntry = getSpreadsheet(spreadsheetName);
 
         WorksheetQuery worksheetQuery = new WorksheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
 
         worksheetQuery.setTitleQuery(worksheetName);
         WorksheetFeed worksheetFeed = service.query(worksheetQuery, WorksheetFeed.class);
         List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
         if (worksheets.isEmpty()) {
             throw new Exception("No worksheets with that name in spreadhsheet "
                     + spreadsheetEntry.getTitle().getPlainText());
         }
 
         return new Worksheet(worksheets.get(0), service);
     }
 
     /**
      * Prints the usage of this application.
      */
     private static void usage() {
         System.out.println("Usage: java ImportClient --username [user] "
                 + "--password [pass] --filename [file] --spreadsheet [name] "
                 + "--worksheet [name] --delimiter [regex]");
         System.out.println("\nA simple application that uses the provided Google\n"
                 + "Account username and password to locate the\n"
                 + "spreadsheet and worksheet in user's Google\n"
                 + "Spreadsheet account, and import the provided\n"
                 + "delimited text file into the worksheet.");
     }
 
     public static void gogogo(String username, String password, int itemsPerBatch, String spreadsheetName, String worksheetName, String data) throws Exception {
         System.out.println("# Initializing upload to Google Spreadsheets...");
         System.out.print("# Logging in as: \"" + username + "\"... ");
         ImportClient client = new ImportClient(username, password, itemsPerBatch, spreadsheetName);
         System.out.println("Success!");
 
         Pattern delim = Pattern.compile(DELIM);
         try {
             int row = 0;
             String[] allLines = data.split("\n");
 
             int currentCell = 1;
             int allRow = allLines.length;
             System.out.println("# Preparing " + allRow + " rows to be updated... ");
 
             List<CellEntry> updatedCells = new LinkedList<CellEntry>();
             Worksheet workSheet = client.getWorksheet(spreadsheetName, worksheetName);
 
            //todo batch stuff here
             ProgressBar.updateProgress(0, allRow);
             for (String line : allLines) {
                 // Break up the line by the delimiter and insert the cells
                 String[] cells = delim.split(line, -1);
                 for (int col = 0; col < cells.length; col++) {
                     // old way - send the change
 //                    client.insertCellEntry(spreadsheet, worksheet, row + 1, col + 1, cells[col]);
 
                     // prepare change
                     CellEntry cellEntry = workSheet.getCell(row + 1, col + 1);
                     String value = cells[col];
                     cellEntry.changeInputValueLocal(value);
                     updatedCells.add(cellEntry);
                 }
                //todo end batch stuff here
                 // Advance the loop
                 ProgressBar.updateProgress(++row, allRow);
             }
 
            //todo send batch stuff here
             //send the batches
             int allBatches = updatedCells.size();
             int currentBatch = 0;
 
             List<List<CellEntry>> batches = chunkList(updatedCells, ITEMS_PER_BATCH);
             System.out.println("\n\n# Uploading changes in " + batches.size() + " chunks, ");
             System.out.println("# containing a total of " + allBatches + " operations... ");
 
             for (List<CellEntry> batch : batches) {
                 CellFeed batchFeed = new CellFeed();
                 for (CellEntry cellEntry : batch) {
                     ProgressBar.updateProgress(++currentBatch, allBatches);
                     Cell cell = cellEntry.getCell();
                     BatchUtils.setBatchId(cellEntry, "R" + cell.getRow() + "C" + cell.getCol());
                     BatchUtils.setBatchOperationType(cellEntry, BatchOperationType.UPDATE);
                     batchFeed.getEntries().add(cellEntry);
                 }
 
                 Link batchLink = workSheet.getBatchUpdateLink();
                 CellFeed batchResultFeed = client.service.batch(new URL(batchLink.getHref()), batchFeed);
                 // Make sure all the operations were successful.
                 for (CellEntry entry : batchResultFeed.getEntries()) {
                     if (!BatchUtils.isSuccess(entry)) {
                         String batchId = BatchUtils.getBatchId(entry);
                         BatchStatus status = BatchUtils.getBatchStatus(entry);
                         System.err.println("Failed entry");
                         System.err.println("\t" + batchId + " failed (" + status.getReason() + ") ");
                         return;
                     }
                 }
             }
            //todo end send batch stuff here
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Chunks a list of items into sublists where each sublist contains at most
      * the specified maximum number of items.
      *
      * @param ts        The list of elements to chunk
      * @param chunkSize The maximum number of elements per sublist
      * @return A list of sublists, where each sublist has chunkSize or fewer elements
      *         and all elements from ts are present, in order, in some sublist
      */
     private static <T> List<List<T>> chunkList(List<? extends T> ts, int chunkSize) {
         Iterator<? extends T> iterator = ts.iterator();
         List<List<T>> returnList = new LinkedList<List<T>>();
         while (iterator.hasNext()) {
             List<T> sublist = new LinkedList<T>();
             for (int i = 0; i < chunkSize && iterator.hasNext(); i++) {
                 sublist.add(iterator.next());
             }
             returnList.add(sublist);
         }
         return returnList;
     }
 
 /****************************************************************************
  * HELPER CLASSES
  *
  * These classes are slightly smarter versions of more standard classes,
  * equipped with little bits of extra functionality that are useful for our
  * purposes.
  ****************************************************************************/
 
     /**
      * Wrapper around Spreadsheets Worksheet entries that adds some utility
      * methods useful for our purposes.
      */
     private static class Worksheet {
         private final SpreadsheetService spreadsheetService;
         private WorksheetEntry backingEntry;
         private CellFeed cellFeed;
         private int rows;
         private int columns;
         private CellEntry[][] cellEntries;
 
         Worksheet(WorksheetEntry backingEntry, SpreadsheetService spreadsheetService) throws IOException, ServiceException {
 
             this.backingEntry = backingEntry;
             this.spreadsheetService = spreadsheetService;
             this.rows = backingEntry.getRowCount();
             this.columns = backingEntry.getColCount();
             refreshCachedData();
         }
 
         /**
          * Presents the given cell feed as a map from row, column pair to CellEntry.
          */
         private void refreshCachedData() throws IOException, ServiceException {
 
             CellQuery cellQuery = new CellQuery(backingEntry.getCellFeedUrl());
             cellQuery.setReturnEmpty(true);
             this.cellFeed = spreadsheetService.getFeed(cellQuery, CellFeed.class);
 
 //            A subtlety: Spreadsheets row,col numbers are 1-based whereas the
 //            cellEntries array is 0-based. Rather than wasting an extra row and
 //            column worth of cells in memory, we adjust accesses by subtracting
 //            1 from each row or column number.
             cellEntries = new CellEntry[rows][columns];
             for (CellEntry cellEntry : cellFeed.getEntries()) {
                 Cell cell = cellEntry.getCell();
                 cellEntries[cell.getRow() - 1][cell.getCol() - 1] = cellEntry;
             }
         }
 
         /**
          * Gets the cell entry corresponding to the given row and column.
          */
         CellEntry getCell(int row, int column) {
             return cellEntries[row - 1][column - 1];
         }
 
         /**
          * Returns this worksheet's column count.
          */
         int getColCount() {
             return columns;
         }
 
         /**
          * Returns this worksheet's row count.
          * @return
          */
         int getRowCount() {
             return rows;
         }
 
         /**
          * Sets this worksheets's row count.
          * @param newRowCount
          * @throws com.google.gdata.util.ServiceException
          */
         void setRowCount(int newRowCount) throws IOException, ServiceException {
             rows = newRowCount;
             backingEntry.setRowCount(newRowCount);
             backingEntry = backingEntry.update();
             refreshCachedData();
         }
 
         /**
          * Gets a link to the batch update URL for this worksheet.
          * @return
          */
         Link getBatchUpdateLink() {
             return cellFeed.getLink(Link.Rel.FEED_BATCH, Link.Type.ATOM);
         }
     }
 
 }
