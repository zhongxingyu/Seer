 package org.lantern.loggly;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.lantern.JsonUtils;
 
 public class LogglyMessage {
     private static final Sanitizer[] SANITIZERS = new Sanitizer[] {
             new IPv4Sanitizer(),
             new EmailSanitizer()
     };
 
     private String reporterId;
     private String message;
     private String fullMessage;
     private Date occurredAt;
     private String locationInfo;
     private Throwable throwable;
     private String throwableOrigin;
     private String stackTrace;
     private Object extra;
     private AtomicInteger nSimilarSuppressed = new AtomicInteger(0);
 
     public LogglyMessage(String reporterId, String message, Date occurredAt) {
         this.reporterId = reporterId;
        // This holds the full original message
         this.fullMessage = message;
        // We truncate message to 100 because Loggly can't index fields with more than 100 characters of data
         this.message = message.length() > 100 ? message.substring(0, 100) : message;
         this.occurredAt = occurredAt;
     }
 
     public String getReporterId() {
         return reporterId;
     }
 
     public String getMessage() {
         return message;
     }
     
     public String getFullMessage() {
         return fullMessage;
     }
 
     public Date getOccurredAt() {
         return occurredAt;
     }
 
     /**
      * Sanitizes {@link #message}, {@link #stackTrace}, and if sanitizeExtra is
      * true, {@link #extra} (which must then be JSON-serializable), using the
      * {@link #SANITIZERS} defined.
      *
      * @return this
      */
     public LogglyMessage sanitized(boolean sanitizeExtra) {
         if (message != null) {
             message = sanitize(message);
         }
         if (stackTrace != null) {
             stackTrace = sanitize(stackTrace);
         }
         if (sanitizeExtra && extra != null) {
             String json = JsonUtils.jsonify(extra);
             json = sanitize(json);
             this.setExtraFromJson(json);
         }
         return this;
     }
 
     public LogglyMessage sanitized() {
         return sanitized(true);
     }
 
     public String getLocationInfo() {
         return locationInfo;
     }
 
     public void setLocationInfo(String locationInfo) {
         this.locationInfo = locationInfo;
     }
 
     @JsonIgnore
     public Throwable getThrowable() {
         return throwable;
     }
 
     public LogglyMessage setThrowable(Throwable throwable) {
         this.throwable = throwable;
         if (throwable == null) {
             stackTrace = null;
             throwableOrigin = null;
         } else {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             throwable.printStackTrace(pw);
             pw.close();
             stackTrace = sw.getBuffer().toString();
             throwableOrigin = throwable.getStackTrace()[0].toString();
         }
         return this;
     }
 
     /**
      * Returns a key that uniquely identifies this message.
      * 
      * @return
      */
     public String getKey() {
         if (locationInfo != null) {
             return locationInfo;
         } else {
             return throwableOrigin;
         }
     }
 
     public Object getExtra() {
         return extra;
     }
 
     public LogglyMessage setExtra(Object extra) {
         this.extra = extra;
         return this;
     }
     
     public LogglyMessage setExtraFromJson(String json) {
         this.extra = json != null ? JsonUtils.decode(json, Map.class) : null;
         return this;
     }
 
     public String getStackTrace() {
         return stackTrace;
     }
 
     public int getnSimilarSuppressed() {
         return nSimilarSuppressed.get();
     }
 
     public LogglyMessage incrementNsimilarSuppressed() {
         this.nSimilarSuppressed.incrementAndGet();
         return this;
     }
 
     public LogglyMessage setnSimilarSuppressed(int nSimilarSuppressed) {
         this.nSimilarSuppressed.set(nSimilarSuppressed);
         return this;
     }
 
     /**
      * Applies all {@link #SANITIZERS}s to the original string.
      * 
      * @param original
      * @return
      */
     private static String sanitize(String original) {
         if (original == null || original.length() == 0) {
             return original;
         }
         String result = original;
         for (Sanitizer filter : SANITIZERS) {
             result = filter.sanitize(result);
         }
         return result;
     }
 
     private static interface Sanitizer {
         /**
          * Sanitize the given original string.
          * 
          * @param original
          * @return the sanitized string
          */
         String sanitize(String original);
     }
 
     /**
      * Sanitizer that sanitizes content by replacing occurrences of a regex with
      * a static string.
      */
     private static class RegexSanitizer implements Sanitizer {
         private final Pattern pattern;
         private final String replacement;
 
         /**
          * 
          * @param regex
          *            the regex
          * @param replacement
          *            the string with which to replace occurrences of the regex
          */
         public RegexSanitizer(String regex, String replacement) {
             super();
             this.pattern = Pattern.compile(regex);
             this.replacement = replacement;
         }
 
         @Override
         public String sanitize(String original) {
             Matcher matcher = pattern.matcher(original);
             StringBuffer result = new StringBuffer();
             while (matcher.find()) {
                 matcher.appendReplacement(result, replacement);
             }
             matcher.appendTail(result);
             return result.toString();
         }
     }
 
     /**
      * A {@link Sanitizer} that replaces everything that looks like an IPv4
      * address with ???.???.???.???.
      */
     private static class IPv4Sanitizer extends RegexSanitizer {
         private static final String IP_REGEX = "(?:[0-9]{1,3}\\.){3}[0-9]{1,3}"; // TODO (see [1] below)
         private static final String IP_REPLACEMENT = "<IP hidden>";
 
         public IPv4Sanitizer() {
             super(IP_REGEX, IP_REPLACEMENT);
         }
     }
 
     /**
      * A {@link Sanitizer} that replaces everything that looks like an email
      * address with <email hidden>.
      */
     private static class EmailSanitizer extends RegexSanitizer {
         // based on http://www.regular-expressions.info/email.html
         private static final String EMAIL_REGEX = "[a-zA-Z0-9._%+-]+@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}"; // TODO (see [1] below)
         private static final String EMAIL_REPLACEMENT = "<email hidden>";
 
         public EmailSanitizer() {
             super(EMAIL_REGEX, EMAIL_REPLACEMENT);
         }
     }
 
     // [1] Maybe these should be moved to LanternConstants. 
     // Would also be nice if the frontend could share these kinds of constants with the backend,
     // since currently they're being duplicated. 
 
     public static void main(String[] args) throws Exception {
         // Testing
         LogglyMessage msg = new LogglyMessage("reporter",
             "This message contains a dummy IP address (12.34.56.789) " +
             "and two emails: a@foo.com and b@bar.com.", new Date())
             .setExtraFromJson("{\"key\": \"email! c@qux.co.uk! IP? 123.45.67.89?\", \"otherKey\": 5}");
         msg = msg.sanitized();
         System.out.println(msg.getMessage());
         System.out.println(msg.getExtra());
     }
 }
