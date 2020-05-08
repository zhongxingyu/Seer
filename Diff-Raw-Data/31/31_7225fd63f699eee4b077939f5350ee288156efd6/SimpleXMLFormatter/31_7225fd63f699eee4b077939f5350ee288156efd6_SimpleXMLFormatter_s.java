 package fedora.logging;
 
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.nio.charset.Charset;
 
 /**
  * Formater to turn a <code>LogRecord</code> into a simple XML format.
  * <p></p>
  * This differs from <code>XMLFormatter</code> in that it doesn't
  * save information about sequence, logger, class, or method,
  * and the time of the error is given only in milliseconds since the epoch.
  * It also uses the name of the log level instead of a record element,
 * and (if desired) will parse the messages if they start with [...], and use 
 * the inner text as a 'mid', or message id, and the remaining text for the 
 * text of the message (until non-whitespace after ]).  SimpleXMLFormatter 
 * does not handle keys and params for resource bundles -- when using this 
 * class, you buy into the strategy of pre-localizing your messages, and 
  * providing a key in the message itself (before the ']' character), if wanted.
  * <p></p>
  * Default encoding is the string representing the encoding of the output
  * XML *if* the handler doesn't identify it's own encoding.  It is not
  * a flag for saying what kind of output you want!
  * <p></p>
 * Like <code>XMLFormatter</code>, this can be used with arbitrary character 
  * encodings.
  *
  * @author cwilper@cs.cornell.edu
  */
public class SimpleXMLFormatter 
         extends Formatter {
 
     /** Whether to log mids or not */
     private boolean m_logMids;
    
     /** Default encoding for XML 'encoding' attribute */
     private String m_defaultEncoding;
 
     /**
      * Constructs a <code>SimpleXMLFormatter</code>.
      *
      * @param logMids Whether or not to log message ids.
      * @param defaultEncoding The default encoding string to use for the XML
      *        if the handler doesn't define one.
      */
     public SimpleXMLFormatter(boolean logMids, String defaultEncoding) {
         m_logMids=logMids;
         m_defaultEncoding=defaultEncoding;
     }
 
     /**
      * Substitutes &lt, &gt;, &amp;, and &quot; with entity references.
      *
      * @param sb The StringBuffer to copy the fixed string to.
      * @param text The original string.
      */
     private void appendEscaped(StringBuffer sb, String text) {
         for (int i = 0; i < text.length(); i++) {
             char ch = text.charAt(i);
             if (ch == '<') {
                 sb.append("&lt;");
             } else if (ch == '>') {
                 sb.append("&gt;");
             } else if (ch == '&') {
                 sb.append("&amp;");
             } else if (ch == '"') {
                 sb.append("&quot;");
             } else {
                 sb.append(ch);
             }
         }
     }
 
     /**
      * Gives a level name for a recognized level, or &amp;custom&amp; if
      * the level is unrecognized.
      *
      * @param Level The log level.
      * @return The name of the level.
      */
     private final static String getLevelName(Level level) {
         if (level==Level.FINEST) {
             return "finest";
         } else if (level==Level.FINER) {
             return "finer";
         } else if (level==Level.FINE) {
             return "fine";
         } else if (level==Level.CONFIG) {
             return "config";
         } else if (level==Level.INFO) {
             return "info";
         } else if (level==Level.WARNING) {
             return "warning";
         } else if (level==Level.SEVERE) {
             return "severe";
         } else {
             return "custom";
         }
     }
 
     /**
      * Gets a formatted <code>String</code> for the given <code>LogRecord</code>.
      *
      * @param record The record to be formatted.
      * @return A formatted log record.
      */
     public String format(LogRecord record) {
         if (record.getMessage()==null) {
             record.setMessage("");
         }
         StringBuffer sb = new StringBuffer(record.getMessage().length() + 150);
         sb.append("  <");
         String kind=getLevelName(record.getLevel());
         sb.append(kind);
         if (kind.equals("custom")) {
             sb.append(" class=\"");
             sb.append(record.getLevel().getClass().getName());
             sb.append("'");
         }
         sb.append(" time=\"");
         sb.append(record.getMillis());
         sb.append("\" thread=\"");
         sb.append(record.getThreadID());
         String formatted=formatMessage(record);
         if (m_logMids) {
             if (formatted.charAt(0)=='[') {
                 int i=formatted.indexOf(']');
                 if (i!=-1) {
                    sb.append(" mid=\"");
                     sb.append(formatted.substring(1,i));
                     try {
                         StringBuffer t=new StringBuffer();
                         t.append(formatted.substring(i+1));
                         while (t.charAt(0)==' ') {
                             t.deleteCharAt(0);
                         }
                         formatted=t.toString();
                     } catch (Throwable t) {
                         formatted="";
                     }
                 }
             }
         }
         sb.append("\"");
         if (formatted.equals("")) {
             sb.append("/");
         } else {
             sb.append(">");
             appendEscaped(sb, formatted);
             sb.append("</");
             sb.append(kind);
         }
         sb.append(">\n");
         return sb.toString();
     }
 
     /**
      * Gets the header string for a set of XML formatted records.
     * 
      * @param   h  The target handler.
      * @return  header string
      */
     public String getHead(Handler h) {
         StringBuffer sb = new StringBuffer();
         sb.append("<?xml version=\"1.0\"");
         String encoding = h.getEncoding();
         if (encoding == null) {
             encoding = m_defaultEncoding;
         }
         // map to canonical name, if possible
         try {
             Charset cs = Charset.forName(encoding);
             encoding = cs.name();
         } catch (Exception ex) {
             // prob getting canonical name, use raw
        }	
 
         sb.append(" encoding=\"");
         sb.append(encoding);
         sb.append("\"");
         sb.append(" standalone=\"yes\"?>\n");
         sb.append("<log xmlns=\"http://dlrg.cornell.edu/logging/SimpleXMLFormatter/1/0/\">\n");
         return sb.toString();
     }
 
     /**
      * Gets the tail string for a set of XML formatted records.
     * 
      * @param   h  The target handler.
      * @return  tail string
      */
     public String getTail(Handler h) {
         return "</log>\n";
     }
 }
