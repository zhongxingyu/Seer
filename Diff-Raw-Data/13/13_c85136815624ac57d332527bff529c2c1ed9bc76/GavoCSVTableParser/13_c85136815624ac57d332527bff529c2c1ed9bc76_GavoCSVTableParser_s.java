 package uk.ac.starlink.topcat.contrib;
 
 /*
  * CSVTableParser.java
  *
  * Created on September 18, 2003, 3:45 PM
  *
  * Copyright 2003-2004 German Astrophysical Virtual Observatory (GAVO)
  */
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import uk.ac.starlink.table.AbstractStarTable;
 import uk.ac.starlink.table.ColumnInfo;
 import uk.ac.starlink.table.RowSequence;
 import uk.ac.starlink.table.RowStore;
 import uk.ac.starlink.table.StarTable;
 import uk.ac.starlink.table.StoragePolicy;
 import uk.ac.starlink.table.Tables;
 
 
 /**
  * Class for a parser that can parse CSV input streams, such as those produced by
  * GAVO's database query web apps.
  * 
  * <p>
  * We follow and expand on the specification for CSV files as detailed in
  * http://www.edoceo.com/utilis/csv-file-format.php. We expand on this by
  * allowing zero, one or more header lines. If header lines are there, we
  * assume the last one to give the names of the columns. By default we assume
  * 1 header line is available, the actual number may be set in the appropriate
  * parseCSV method.
  * </p>
  * 
  * <p>
  * We do allow for delimiters different from a comma, but we treat their
  * occurence exactly as otherwise the comma would be treated according to the
  * specification.
  * </p>
  * 
  * <p>
  * We do NOT allow for string demarcations different from the double-quote.
  * </p>
  * 
  * <p>
  * A a final touch we allow for "comment" lines starting with a comment string.
  * Lines starting with the comment string (e.g. ";;") will be ignored (but see next paragraph). 
  * This is needed in order to be able to successfully parse CSV files as output by
  * CDS/Vizier, or the GAVO "simpledb" web applications.
  * </p>
  *
  * <p>
  * Addition: we assume that there are comment lines starting with "#COLUMN" which indicate metadata about all the columns.
  * We assume that the first line without "#" symbol contains the column names.
  * All subsequent rows are data.
  * </p>
  * 
  * @author Gerard Lemson
  * @author Hans-Martin Adorf
  * @author Gerard Lemson
  * @author Mark Taylor
  */
 public class GavoCSVTableParser  {
 
     /** Storage policy for managing bulk table data. */
     final StoragePolicy storage;
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** The default delimiter between table entries */
     public static final String DEFAULT_DELIMITER = ",";
     /** The default comment prefix */
     public static final String DEFAULT_COMMENT_PREFIX = "#";
 
     /** A shorthand for a quote */
     private static final String QUOTE = "\"";
 
     /** The parser that actually parses a line */
     private RecursiveDescentParser recursiveDescentParser = new RecursiveDescentParser();
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of CSVTableParser
      */
     public GavoCSVTableParser(StoragePolicy storage) {
         this(storage, DEFAULT_DELIMITER);
     }
 
     /**
      * Creates a new instance of CSVTableParser
      *
      * @param delimiter the character delimiter ('separator')
      */
     public GavoCSVTableParser(StoragePolicy storage, String delimiter) {
         this.storage = storage;
         setDelimiter(delimiter);
     }
 
     //~ Methods ----------------------------------------------------------------
 
 
     /**
      * Getter for property commentPrefix.
      *
      * @return Value of property commentPrefix.
      */
     public String getCommentPrefix() {
         return DEFAULT_COMMENT_PREFIX;
     }
 
     /**
      * Sets the delimiter to use.
      *
      * @param delimiter the delimiter.
      */
     public void setDelimiter(String delimiter) {
         recursiveDescentParser.setDelimiter(delimiter);
     }
 
     /**
      * Gets the delimiter.
      *
      * @return a String with the delimiter.
      */
     public String getDelimiter() {
         return recursiveDescentParser.getDelimiter();
     }
 
 
     /**
      * Return a List of tokens (Strings) from the specified line, delimited by
     * the specified delimiter.<br>
      *
      * @param line a String with the line to parse
     *
      * @return a List with the tokens
      */
     public List parseLine(String line) {
         return recursiveDescentParser.parse(line);
     }
     
     public StarTable parse(InputStream stream) throws Exception
     {
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
         String line = reader.readLine();
         
         if(line.startsWith("#OK"))
         {
           Vector columnLines = new Vector();
           while((line = reader.readLine()) != null && line.startsWith("#"))
             if(line.startsWith("#COLUMN"))
               columnLines.add(line);
           final ColumnInfo[] columnInfo = new ColumnInfo[columnLines.size()];
           int dim = columnLines.size();
           int[] types = new int[dim];
           for(int i = 0; i < dim; i++)
           {
               String columnLine = (String)columnLines.get(i);
               int index1 = columnLine.indexOf("JDBC_TYPE=")+10;
               int index2= columnLine.indexOf(" ",index1);
               types[i] = Integer.parseInt(columnLine.substring(index1, index2));
               Class type = classForJDBCType(types[i]);
               index1 = columnLine.indexOf("name=")+5;
               index2= columnLine.indexOf(" ",index1);
               String name = columnLine.substring(index1, index2);
               columnInfo[i] = new ColumnInfo(name, type, "");
           }
           StarTable metaData = new AbstractStarTable() {
               public int getColumnCount() {
                   return columnInfo.length;
               }
               public ColumnInfo getColumnInfo(int icol) {
                   return columnInfo[icol];
               }
               public long getRowCount() {
                   return -1L;
               }
               public RowSequence getRowSequence() {
                   throw new UnsupportedOperationException();
               }
           };
           RowStore rowStore = storage.makeConfiguredRowStore( metaData );
           
           try {
               Object[] row = null;
               while((line = reader.readLine()) != null)
               {
                   if ( Thread.interrupted() ) {
                       throw new InterruptedException();
                   }
                   if ( line.startsWith( "#ERROR" ) ) {
                       StringBuffer sb = new StringBuffer( line );
                       while ( (line = reader.readLine()) != null && 
                               sb.length() < 8096 ) {
                           sb.append(line+"\n");
                       }
                       throw new IOException( sb.toString() );
                   }
                   List cells_ = parseLine(line);
                   
                   String[] cells = (String[])cells_.toArray(new String[]{}); 
                   row = new Object[dim];
                   for(int i = 0; i < dim; i++)
                       row[i] = objectForJDBCType(types[i],cells[i]);
                   rowStore.acceptRow(row);
               }
           }
           finally {
               rowStore.endRows();
               reader.close();
           }
           return Tables.randomTable( rowStore.getStarTable() );
         }
         else
         {
             StringBuffer sb = new StringBuffer();
             while((line = reader.readLine()) != null && sb.length() < 8096)
               sb.append(line+"\n");
             throw new IOException("Error\n"+sb.toString());
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * Recursive descent parser for parsing a single line
      */
     public static class RecursiveDescentParser {
         //~ Static fields/initializers -----------------------------------------
 
         private static final boolean DEBUG = false;
 
         //~ Instance fields ----------------------------------------------------
 
         private String lastToken;
         private String nextToken;
 
         private StringTokenizer stringTokenizer;
         private String delimiter = DEFAULT_DELIMITER;
         private StringBuffer currentItem = new StringBuffer();
         private List itemList;
 
         /** Holds value of property line. */
         private String line;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new instance of RecursiveDescentParser
          */
         public RecursiveDescentParser() {
         }
 
         /**
          * Creates a new instance of RecursiveDescentParser
          *
          * @param line the line to parse
          */
         public RecursiveDescentParser(String line) {
             setLine(line);
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * Setter for property delimiter.
          *
          * @param delimiter New value of property delimiter.
          */
         public void setDelimiter(String delimiter) {
             this.delimiter = delimiter;
         }
 
         /**
          * Getter for property delimiter.
          *
          * @return Value of property delimiter.
          */
         public String getDelimiter() {
             return this.delimiter;
         }
 
         /**
          * Setter for property line.
          *
          * @param line New value of property line.
          */
         public void setLine(String line) {
             this.line = line;
 
             if (line == null) {
                 System.out.println("help");
             }
 
             stringTokenizer = new StringTokenizer(line, delimiter + QUOTE, true);
         }
 
         /**
          * Getter for property line.
          *
          * @return Value of property line.
          */
         public String getLine() {
             return this.line;
         }
 
         /**
          * Parses the given line of text
          *
          * @param line a String with delimiters and double quotes
          *
          * @return a List with the parsed items
          */
         public List parse(String line) {
             setLine(line);
             parse();
 
             return this.itemList;
         }
 
         /**
          * Parses a line previously set
          *
          * @return a List with the parsed items
          *
          * @see #setLine
          */
         public List parse() {
             if (stringTokenizer == null) {
                 return null;
             }
 
             itemList = new ArrayList();
             parseLine();
             if(delimiter.equals(lastToken)) // take care of null as last item
               itemList.add("");
 
             if (DEBUG) {
                 System.out.println(itemList);
             }
 
             return this.itemList;
         }
 
         private void consumeToken() {
             lastToken = nextToken;
             nextToken = null;
         }
 
         private boolean moreTokens() {
             return ((nextToken != null) || stringTokenizer.hasMoreTokens());
         }
 
         private String nextToken() {
             if (nextToken == null) {
                 if (stringTokenizer.hasMoreTokens()) {
                     nextToken = stringTokenizer.nextToken();
                 }
             }
 
             return nextToken;
         }
 
         private void parseError(String msg) {
           throw new IllegalArgumentException("Parse error: " + msg);
         }
 
         private void parseLine() {
             if (moreTokens()) {
                 String token = nextToken();
 
                 if (delimiter.equals(token)) {
                     itemList.add("");
                     consumeToken();
                     parseLine();
                 } else if (QUOTE.equals(token)) {
                     consumeToken();
                     currentItem = new StringBuffer();
                     parseQuotedWords();
                     parseLine();
                 } else {
                     parseUnquotedWords();
                     parseLine();
                 }
             }
         }
 
         private void parseQuotedWords() {
             String token = nextToken();
 
             if (token == null) {
                 parseError("Expected quoted string, but found <EOL>");
             } else if (delimiter.equals(token)) {
                 // delimiter found inside of quoted string
                 currentItem.append(delimiter);
                 consumeToken();
                 parseQuotedWords();
             } else if (QUOTE.equals(token)) {
                 consumeToken();
                 nextToken = nextToken();
 
                 if (QUOTE.equals(nextToken)) {
                     // a pair of double quotes found, translates to a single double quote
                     currentItem.append(QUOTE);
                     consumeToken();
                     parseQuotedWords();
                 } else {
                     // end of quoted string found
                     itemList.add(currentItem.toString());
                     consumeToken();
 
                     return;
                 }
             } else {
                 // normal text token found
                 currentItem.append(token);
                 consumeToken();
                 parseQuotedWords();
             }
         }
 
         private void parseUnquotedWords() {
             itemList.add(nextToken());
             consumeToken();
 
             String nextToken = nextToken();
 
             if (nextToken == null) {
                 return;
             } else if (delimiter.equals(nextToken)) {
                 consumeToken();
             } else {
                 String msg = "This shouldn't happen.";
                 parseError(msg);
                 throw new IllegalStateException(msg);
             }
         }
     }
 
     public static Class classForJDBCType(int jdbcType)
     {
         switch(jdbcType)
         {
           case Types.BIGINT: return Long.class;
           case Types.INTEGER:  return Integer.class;
           case Types.SMALLINT: return Short.class;
           case Types.DOUBLE:
           case Types.FLOAT: return Double.class;
           case Types.REAL: return Float.class;
           default: return String.class;
         }
     }
 
     public static Object objectForJDBCType(int jdbcType, String value)
     {
         if(value == null || value.trim().length() == 0)
           return null;
         switch(jdbcType)
         {
           case Types.BIGINT: return Long.valueOf(value);
           case Types.INTEGER:  return Integer.valueOf(value);
           case Types.SMALLINT: return Short.valueOf(value);
           case Types.DOUBLE:
           case Types.FLOAT: return Double.valueOf(value);
           case Types.REAL: return Float.valueOf(value);
           default: return value;
         }
     }
 }
