 /*******************************************************************************
  * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
  *
  * This file is part of SITools2.
  *
  * SITools2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SITools2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package fr.cnes.sitools.dataset.database.jdbc;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.restlet.Context;
 
 import fr.cnes.sitools.common.exception.SitoolsException;
 import fr.cnes.sitools.dataset.database.DatabaseRequestParameters;
 import fr.cnes.sitools.dataset.database.Range;
 import fr.cnes.sitools.datasource.jdbc.business.SitoolsSQLDataSource;
 import fr.cnes.sitools.datasource.jdbc.dbexplorer.DBResultSet;
 
 /**
  * Execute a request for some records specified by a List of ranges
  * 
  * 
  * @author m.gond
  */
 public class SQLRangeDatabaseRequest extends SQLDatabaseRequest {
   /** Error starting message */
   private static final String ERROR = "ERROR ";
   /** logger de la resource Context / Application ? ok */
   private Logger logger = Context.getCurrentLogger();
 
   /** the connection used */
   private Connection con;
 
   /** The list of ranges to query */
   private List<Range> ranges;
 
   /** The number of the current range in the ranges List */
   private int currentRangeIndex = 0;
 
   /** the prepared statement */
   private PreparedStatement prepStm;
 
   /** The nbTotalResults */
   private int nbTotalResults = 0;
 
   /** The Datasource **/
   private SitoolsSQLDataSource datasource;
 
   /** The nbResultsToSend */
   private int limitParam = 0;
 
   /** the startIndex */
   private int startIndex = 0;
 
   /** nb results send */
   private int nbResultsSent = 0;
 
   /**
    * Create a new SQLRangeDatabaseRequest with the given {@link DatabaseRequestParameters}
    * 
    * @param params
    *          the {@link DatabaseRequestParameters}
    */
   public SQLRangeDatabaseRequest(DatabaseRequestParameters params) {
     super(params);
     ranges = params.getRanges();
     datasource = (SitoolsSQLDataSource) params.getDb();
     limitParam = params.getPaginationExtend();
     startIndex = params.getStartIndex();
   }
 
   @Override
   public final void createRequest() throws SitoolsException {
     try {
 
       // set is count done to true to calculate the maximum total number of records for the request (in order to
       // calculate to actual total number from the range numbers)
       params.setCountDone(true);
       String sql = this.getRequestAsString();
       int maxOffset = super.getTotalCount();
       
 
       clearInvalidRanges(maxOffset);
       nbTotalResults = calculateTotalCount();
      setStartRange(startIndex);     
       
       if (limitParam < 0) {
         limitParam = nbTotalResults;
       }
 
       sql += " LIMIT " + "?" + " OFFSET " + "?";
 
       con = datasource.getConnection();
 
       prepStm = con.prepareStatement(sql);
 
       if (ranges.size() > 0) {
         executeRequest(ranges.get(currentRangeIndex));
       }
     }
     catch (NumberFormatException e) {
       throw new SitoolsException(ERROR + e.getMessage(), e);
     }
     catch (SQLException e) {
       throw new SitoolsException(ERROR + e.getMessage(), e);
     }
   }
 
   @Override
   public void createDistinctRequest() throws SitoolsException {
     this.createRequest();
   }
 
   @Override
   public final boolean nextResult() throws SitoolsException {
     if (rs == null) {
       return false;
     }
     try {
       if (rs.next()) {
         return true;
       }
       else {
         currentRangeIndex++;
         if (currentRangeIndex < ranges.size() && nbResultsSent < limitParam) {
           executeRequest(ranges.get(currentRangeIndex));
           rs.next();
           return true;
         }
         else {
           return false;
         }
       }
     }
     catch (SQLException e) {
       this.logger.log(Level.SEVERE, e.getMessage());
       throw new SitoolsException("ERROR_FETCHING_RESULTSET", e);
     }
   }
 
   /**
    * Execute the SQL request for a particular range
    * 
    * @param range
    *          the range
    * @throws SQLException
    *           if there is an error while executing the request
    */
   private void executeRequest(Range range) throws SQLException {
     // if we are at the start of a range, startIndex = the start of the range
     if (startIndex == -1) {
       startIndex = range.getStart();
     }
     int limit = -1;
     if (range.getEnd() - startIndex > (limitParam - nbResultsSent)) {
       limit = (limitParam - nbResultsSent);
     }
     else {
       limit = range.getEnd() - startIndex + 1;
     }
 
     int offset = startIndex;
 
     nbResultsSent += limit;
 
     prepStm.setInt(1, limit);
     prepStm.setInt(2, offset);
 
     logger.log(Level.INFO, "SQL WITH LIMIT = " + prepStm);
     rs = new DBResultSet(prepStm.executeQuery(), prepStm, con);
     // lets put the startIndex at -1 to tell that we finished completely to read a range
     startIndex = -1;
   }
 
   @Override
   public int getTotalCount() {
     return nbTotalResults;
   }
 
   @Override
   public int getCount() {
     return nbResultsSent;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see fr.cnes.sitools.dataset.jdbc.SQLDatabaseRequest#calculateTotalCountFromBase()
    */
   @Override
   public int calculateTotalCountFromBase() throws SitoolsException {
     params.setCountDone(true);
     int max = super.calculateTotalCountFromBase();
     clearInvalidRanges(max);
     return calculateTotalCount();
   }
 
   /**
    * Calculate the number of records for the list of ranges
    * 
    * @return the total number of records to query
    */
   private int calculateTotalCount() {
     int totalCount = 0;
     for (Range range : ranges) {
       totalCount += range.getEnd() - range.getStart() + 1;
     }
     return totalCount;
   }
 
   /**
    * Clear all invalid range
    * 
    * @param max
    *          the maximum range number allowed
    */
   private void clearInvalidRanges(int max) {
     Iterator<Range> it = ranges.iterator();
     while (it.hasNext()) {
       Range range = it.next();
       if (range.getStart() >= max) {
         it.remove();
         continue;
       }
       if (range.getEnd() >= max) {
         range.setEnd(max - 1);
       }
 
       if (range.getStart() < 0) {
         it.remove();
         continue;
       }
 
       if (range.getEnd() < 0) {
         it.remove();
         continue;
       }
 
       if (range.getStart() > range.getEnd()) {
         it.remove();
         continue;
       }
     }
 
   }
 
   /**
    * Set the start range depending on the start parameter Also calculate the start index in that range
    * 
    * @param start
    *          the start index given by the user
    */
   private void setStartRange(int start) {
     int rangedLoopSize = 0;
     Iterator<Range> it = ranges.iterator();
     while (it.hasNext()) {
       Range range = it.next();
       if (rangedLoopSize <= start && start <= rangedLoopSize + range.getSize()) {
         startIndex += range.getStart();
         return;
       }
       else {
         rangedLoopSize += range.getSize();
         startIndex -= range.getSize();
         it.remove();
         continue;
       }
     }
   }
 }
