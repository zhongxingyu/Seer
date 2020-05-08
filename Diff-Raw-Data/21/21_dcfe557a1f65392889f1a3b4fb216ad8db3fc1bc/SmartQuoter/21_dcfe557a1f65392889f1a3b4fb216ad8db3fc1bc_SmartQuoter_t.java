 package name.vysoky.epub;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Smart quotes text processor.
  *
  * @author Jiri Vysoky
  */
 public class SmartQuoter implements TextProcessor {
 
     final Logger logger = LoggerFactory.getLogger(SmartQuoter.class);
     
     public static final char[] DOUBLE_QUOTES = { '"', '“', '”', '„', '"' };
     public static final char[] SINGLE_QUOTES = { '\'', '‘', '’', '‹', '›' };
     
     public static final char DEFAULT_DOUBLE_QUOTE = '"';
     public static final char DEFAULT_SINGLE_QUOTE = '\'';
 
     // switches
     private boolean lastDoubleQuoteIsLeading = false;
     private boolean lastSingleQuoteIsLeading = false;
 
     private char leadingDoubleQuote = '"';
     private char leadingSingleQuote = '\'';
     private char trailingDoubleQuote = '"';
     private char trailingSingleQuote = '\'';
     
     private final Pattern leadingDoubleQuotePattern  = Pattern.compile("[\\s\\n]\"[^\\s]");
     private final Pattern leadingSingleQuotePattern = Pattern.compile("[\\s\\n]'[^\\s]");
     private final Pattern trailingDoubleQuotePattern = Pattern.compile("[^\\s]\"[\\s\\n.,;?!]");
     private final Pattern trailingSingleQuotePattern = Pattern.compile("[^\\s]'[\\s\\n.,;?!]");
 
     private String document = "UNKNOWN";
     private int line = 1;
 
     public SmartQuoter(char leadingDoubleQuote, char trailingDoubleQuote,
                        char leadingSingleQuote, char trailingSingleQuote) {
         this.leadingDoubleQuote = leadingDoubleQuote;
         this.trailingDoubleQuote = trailingDoubleQuote;
         this.leadingSingleQuote = leadingSingleQuote;
         this.trailingSingleQuote = trailingSingleQuote;
     }
 
     public void setDocument(String document) {
         this.document = document;
         this.line = 1;
     }
 
     @Override
     public String process(String input) {
         if (input.replace("\n", "").trim().isEmpty()) {
             line += StringUtils.countMatches(input, "\n");
             return input; // skip empty lines
         }
         input = convertToDefaultQuotes(input);
         char[] chars = input.toCharArray();
         processChars(chars);
         return new String(chars);
     }    
 
    public static String convertToDefaultQuotes(String input) {
         for (char c : SINGLE_QUOTES) input = input.replace(c, DEFAULT_SINGLE_QUOTE);
         for (char c : DOUBLE_QUOTES) input = input.replace(c, DEFAULT_DOUBLE_QUOTE);
         return input;
     }
     
     String getNearChars(char[] input, int index) {
         char[] chars = new char[3];
         chars[0] = ((index - 1) >= 0) ? input[index - 1] : '\n';
         chars[1] = input[index];
         chars[2] = ((index + 1) < input.length) ? input[index + 1] : '\n';
         return new String(chars);        
     }
     
     boolean isProbablyLeadingDoubleQuote(char[] input, int index) {
         Matcher matcher = leadingDoubleQuotePattern.matcher(getNearChars(input, index));
         return matcher.matches();
     }
 
     boolean isProbablyLeadingSingleQuote(char[] input, int index) {
         Matcher matcher = leadingSingleQuotePattern.matcher(getNearChars(input, index));
         return matcher.matches();
     }
 
 
     boolean isProbablyTrailingDoubleQuote(char[] input, int index) {
         Matcher matcher = trailingDoubleQuotePattern.matcher(getNearChars(input, index));
         return matcher.matches();
     }
 
     boolean isProbablyTrailingSingleQuote(char[] input, int index) {
         Matcher matcher = trailingSingleQuotePattern.matcher(getNearChars(input, index));
         return matcher.matches();
     }
 
     private void processDoubleQuote(char[] input, int index) {
         if (!lastDoubleQuoteIsLeading && isProbablyLeadingDoubleQuote(input, index)) {
             input[index] = leadingDoubleQuote;
             lastDoubleQuoteIsLeading = true;
             return;
         }
 
         if (lastDoubleQuoteIsLeading && isProbablyTrailingDoubleQuote(input, index)) {
             input[index] = trailingDoubleQuote;
             lastDoubleQuoteIsLeading = false;
             return;
         }
         
         if (lastDoubleQuoteIsLeading && isProbablyLeadingDoubleQuote(input, index)) {
             logger.warn("{}[{}:{}]: Missing trailing quote: {}", document, line, index, getText(input, index));
             input[index] = leadingDoubleQuote;
             lastDoubleQuoteIsLeading = true;
             return;
         }
 
         if (!lastDoubleQuoteIsLeading && isProbablyTrailingDoubleQuote(input, index)) {
             logger.warn("{}[{}:{}]: Missing leading quote: {}", document, line, index, getText(input, index));
             input[index] = trailingDoubleQuote;
             lastDoubleQuoteIsLeading = false;
             return;
         }
         
         if (!isProbablyLeadingDoubleQuote(input, index) && !isProbablyTrailingDoubleQuote(input, index)) {
             logger.warn("{}[{}:{}]: Unknown quote type: {}", document, line, index, getText(input, index));
             input[index] = lastDoubleQuoteIsLeading ? trailingDoubleQuote : leadingDoubleQuote;
             lastDoubleQuoteIsLeading = !lastDoubleQuoteIsLeading;
         }            
     }
     
     private void processSingleQuote(char[] input, int index) {
         if (!lastSingleQuoteIsLeading && isProbablyLeadingSingleQuote(input, index)) {
             input[index] = leadingSingleQuote;
             lastSingleQuoteIsLeading = true;
             return;
         }
 
         if (lastSingleQuoteIsLeading && isProbablyTrailingSingleQuote(input, index)) {
             input[index] = trailingSingleQuote;
             lastSingleQuoteIsLeading = false;
             return;
         }
 
         if (lastSingleQuoteIsLeading && isProbablyLeadingSingleQuote(input, index)) {
             logger.warn("{}[{}:{}]: Missing trailing quote: {}", document, line, index, getText(input, index));
             input[index] = leadingSingleQuote;
             lastSingleQuoteIsLeading = true;
             return;
         }
 
         if (!lastSingleQuoteIsLeading && isProbablyTrailingSingleQuote(input, index)) {
             logger.warn("{}[{}:{}]: Missing leading quote: {}", document, line, index, getText(input, index));
             input[index] = trailingDoubleQuote;
             lastSingleQuoteIsLeading = false;
             return;
         }
 
         if (!isProbablyLeadingSingleQuote(input, index) && !isProbablyTrailingSingleQuote(input, index)) {
             logger.warn("{}[{}:{}]: Unknown quote type: {}", document, line, index, getText(input, index));
             input[index] = lastSingleQuoteIsLeading ? trailingSingleQuote : leadingSingleQuote;
             lastSingleQuoteIsLeading = !lastSingleQuoteIsLeading;
         }
     }
 
     private String getText(char[] input, int index) {
         final int BEFORE = 20;
         final int AFTER = 20;
         int from = ((index - BEFORE) < 0) ? 0 : (index - BEFORE);
         int to = ((index + AFTER) > (input.length - 1)) ? (input.length - 1) : (index + AFTER);
         StringBuilder sb = new StringBuilder();
         sb.append("\"...");
         for (int i = from; i < to; i++) sb.append(input[i]);
         sb.append("...\"");
         return sb.toString();
     }
     
     private void processChars(char[] chars) {
         for (int i = 0; i < chars.length; i++) {
             char c = chars[i];
             if (c == '\n') line++;
             if (c == DEFAULT_DOUBLE_QUOTE) processDoubleQuote(chars, i);
             if (c == DEFAULT_SINGLE_QUOTE) processSingleQuote(chars, i);
         }
     }
 }
