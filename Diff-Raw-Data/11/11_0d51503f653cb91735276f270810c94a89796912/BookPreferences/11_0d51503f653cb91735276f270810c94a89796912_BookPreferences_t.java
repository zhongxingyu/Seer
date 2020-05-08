 package net.codjo.sample.server.broadcast;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.SQLException;
 import java.sql.Types;
 import net.codjo.broadcast.common.Context;
 import net.codjo.broadcast.common.PostBroadcaster;
 import net.codjo.broadcast.common.Preferences;
 import net.codjo.broadcast.common.Selector;
 import net.codjo.broadcast.common.computed.ComputedField;
 import net.codjo.broadcast.common.computed.ConstantField;
 import net.codjo.broadcast.server.selector.AbstractGenericSelector;
 /**
  *
  */
 public class BookPreferences extends Preferences {
     public static final String FAMILY = "BOOKS";
 
 
     public BookPreferences() {
         super(FAMILY,
               "AP_BOOK",
              "$dbApplicationUser$.TMP_ALL_BOOK_SEL",
              "$dbApplicationUser$.TMP_COMPUTED_TAB");
     }
 
 
     @Override
     public PostBroadcaster buildPostBroadcaster(Connection con, BigDecimal fileId,
                                                 BigDecimal contentId, BigDecimal sectionId) {
         return null;
     }
 
 
     @Override
     protected ComputedField[] initComputedFields() {
         return new ComputedField[]{new ConstantField("CTE_STRING", Types.VARCHAR, "CTE_STRING VARCHAR(15)", "TOPOLINO")
         };
     }
 
 
     @Override
     protected void initJoinKeys() {
         addJoinKeys(INNER_JOIN, getComputedTableName(), getSelectionTableName(),
                     new String[][]{
                           {"SELECTION_ID", "SELECTION_ID", "="}
                     });
         addJoinKeys(RIGHT_JOIN, "AP_BOOK", getSelectionTableName(),
                     new String[][]{
                           {"TITLE", "TITLE", "="},
                           {"AUTHOR", "AUTHOR", "="}
                     });
     }
 
 
     @Override
     public Selector buildSelector(Connection con, BigDecimal contentID,
                                   BigDecimal sectionID, BigDecimal selectionID)
           throws SQLException {
         return new BookSelector(selectionID.intValue());
     }
 
 
     public static class BookSelector extends AbstractGenericSelector {
         private static final String BASE_INSERT_QUERY = "insert into $selectionTable$ (SELECTION_ID, TITLE, AUTHOR) ";
 
 
         public BookSelector(int selectorId) {
             super(selectorId);
         }
 
 
         @Override
         protected void executeStaticSelector(Context context,
                                              Connection connection,
                                              String selectionTableName,
                                              Date broadcastDate,
                                              int selectorId) throws SQLException {
             createSelectionTable(connection, context.replaceVariables(selectionTableName));
             String selectorQuery = BASE_INSERT_QUERY
                                    + " select SEQ_BROADCAST_SELECTION.nextval ,TITLE, AUTHOR from AP_BOOK";
             executeQueryWithVariables(context, connection, selectorQuery);
         }
 
 
         @Override
         protected void executeGenericSelector(Context context,
                                               Connection connection,
                                               String selectionTableName,
                                               Date broadcastDate,
                                               int selectorId) throws SQLException {
             createSelectionTable(connection, selectionTableName);
             String selectorQuery = BASE_INSERT_QUERY + getSelectorQuery(connection, selectorId);
             executeQueryWithVariables(context, connection, selectorQuery);
         }
 
 
         private void createSelectionTable(Connection connection, String tableName) throws SQLException {
             // TODO[Oracle Support] re-init de la sequence??
             createTempTable(connection, tableName,
                             " SELECTION_ID         numeric(18)   , "
                             + " TITLE      varchar(255)  not null,"
                             + " AUTHOR      varchar(150)  not null ");
         }
     }
 }
