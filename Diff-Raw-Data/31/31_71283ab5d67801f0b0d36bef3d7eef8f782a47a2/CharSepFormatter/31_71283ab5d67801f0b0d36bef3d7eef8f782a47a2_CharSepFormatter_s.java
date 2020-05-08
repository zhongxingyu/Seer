 package gov.cida.sedmap.data;
 
 import gov.cida.sedmap.io.IoUtils;
 
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.List;
 
 public class CharSepFormatter implements Formatter {
 
 	private final String CONTENT_TYPE;
 	private final String FILE_TYPE;
 	private final String TYPE;
 	private final String SEPARATOR;
         
        public static final String DEFAULT_HEADER_FILENAME = "/default-header.txt";
        public static final String SITE_HEADER_FILENAME = "/site-header.txt";
        public static final String DISCRETE_HEADER_FILENAME = "/discrete-header.txt";
        public static final String DAILY_HEADER_FILENAME = "/daily-header.txt";
         
        public static final String DEFAULT_HEADER;
         public static final String SITE_HEADER;
         public static final String DISCRETE_HEADER;
         public static final String DAILY_HEADER;
         static {
             DEFAULT_HEADER = IoUtils.readTextResource(DEFAULT_HEADER_FILENAME);
             SITE_HEADER = IoUtils.readTextResource(SITE_HEADER_FILENAME);
             DISCRETE_HEADER = IoUtils.readTextResource(DISCRETE_HEADER_FILENAME);
             DAILY_HEADER = IoUtils.readTextResource(DAILY_HEADER_FILENAME);
         }
 
 	public CharSepFormatter(String contentType, String separator, String type) {
 		CONTENT_TYPE = contentType;
 		SEPARATOR    = separator;
 		FILE_TYPE    = "."+type;
 		TYPE         = type;
 	}
 
 
 
 	@Override
 	public final String getContentType() {
 		return CONTENT_TYPE;
 	}
 	@Override
 	public final String getSeparator() {
 		return SEPARATOR;
 	}
 	@Override
 	public String getFileType() {
 		return FILE_TYPE;
 	}
 	@Override
 	public String getType() {
 		return TYPE;
 	}
 
 
 	@Override
 	public String transform(String line, Formatter from) {
 
 		if ( ! SEPARATOR.equals( from.getSeparator() ) ) {
 			// String.replaceAll uses regex string and comma is a special char
 			while ( line.contains(from.getSeparator()) ) {
 				line = line.replace(from.getSeparator(), SEPARATOR);
 			}
 		}
 		return line;
 	}
 
 
 	@Override
 	public String fileHeader(List<Column> columns) throws SQLException {
 		StringBuilder header = new StringBuilder();
                 
 		String sep = "";
 		for (Column col : columns) {
 			header.append(sep).append(col.name);
 			sep = SEPARATOR;
 		}
 		header.append( IoUtils.LINE_SEPARATOR );
 
 		return header.toString();
 	}
 
 
 
 	@Override
 	public String fileRow(Iterator<String> values) throws SQLException {
 		StringBuilder row = new StringBuilder();
 
 
 		String sep = "";
 		while (values.hasNext()) {
 			// JDBC is one-based
 			String val = values.next();
 			val = val==null ?"" :val;
 			if (val.contains(SEPARATOR)) {
 				val = val.contains("\"") ?val.replaceAll("\"", "'") :val;
 				val = "\""+val+"\"";
 			}
 			row.append(sep).append(val);
 			sep = SEPARATOR;
 		}
 		row.append( IoUtils.LINE_SEPARATOR );
 
 		return row.toString();
 	}
 
     @Override
     public String fileHeader(List<Column> columns, HeaderType headerType) throws SQLException {
         StringBuilder header = new StringBuilder();
        header.append(DEFAULT_HEADER);
         String typeSpecificHeader = "";
         switch (headerType) {
             case DAILY:
                 typeSpecificHeader = DAILY_HEADER;
                 break;
             case DISCRETE:
                 typeSpecificHeader = DISCRETE_HEADER;
                 break;
 
             case SITE:
                 typeSpecificHeader = SITE_HEADER;
                 break;
         }
         header.append(typeSpecificHeader);
         header.append(fileHeader(columns));
         return header.toString();
     }
 }
