 package ee.mattijagula.mikker.upload;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 
 /**
  * Configuration context for the recorder applet.
  */
 public class Configuration {
     public interface ParameterSource {
         String getParameter(String name);
         URI getBaseUri();
         String getCookies();
         String determineUserAgent();
     }
 
     private final int MAX_RECORDING_DURATION = 2 * 60 * 1000;
 
     private final String uploadUrl;
     private final String fileFieldName;
     private final CookieParser.Cookie[] cookies;
     private String filename;
     private KeyValuePairParser.Pair[] additionalPostParameters;
     private int maxRecordingDuration = MAX_RECORDING_DURATION;
     private String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8";
 
     public Configuration(String uploadUrl, String fieldName, String filename) {
         this(uploadUrl, fieldName, filename, new CookieParser.Cookie[0]);
     }
 
     public Configuration(String uploadUrl, String fieldName, String filename, CookieParser.Cookie[] cookies) {
         this.uploadUrl = uploadUrl;
         this.fileFieldName = fieldName;
         this.filename = filename;
         this.cookies = new CookieParser.Cookie[0];
         this.additionalPostParameters = new KeyValuePairParser.Pair[0];
     }
 
     public Configuration(ParameterSource cfg) {
         this.fileFieldName = arg(cfg, "upload_field_name", "file");
         this.filename = arg(cfg, "filename", "sound-" + System.currentTimeMillis());
 
         String relativePath = arg(cfg, "upload_url", "");
         this.uploadUrl = composeUploadUrl(cfg, relativePath);
 
         this.cookies = parseCookies(cfg);
 
         String postParameters = arg(cfg, "post_parameters", "");
         this.additionalPostParameters = new KeyValuePairParser(postParameters).getPairs();
 
         String maxDurationStr = arg(cfg, "max_duration_seconds", "");
         if (!"".equals(maxDurationStr))
             this.maxRecordingDuration = 1000 * Integer.parseInt(maxDurationStr);
 
         this.userAgent = cfg.determineUserAgent();
 
        System.out.println("Parameters: upload to = " + uploadUrl +
                 ", upload field = " + fileFieldName +
                 ", duration = " + maxRecordingDuration +
                 ", default filename = " + filename +
                 ", cookies = " + Arrays.asList(cookies) +
                 ", post parameters = " + Arrays.asList(additionalPostParameters) +
                 ", user agent = " + userAgent);
     }
 
     public static String[][] supportedParameters() {
         return new String[][] {
                 { "upload_url", "url", "relative or absolute url where the recording is to be uploaded, " +
                         "defaults to applet document url" },
                 { "upload_field_name", "string", "name of the file field on the upload form" },
                 { "filename", "string", "initial value of the filename for the recording" },
                 { "max_duration_seconds", "integer", "maximal duration of the recording in seconds, defaults to 120" },
                 { "post_parameters", "string", "semicolon-separated list of key-value pairs with POST parameters " +
                         "to submit when uploading the file" }
         };
     }
 
     public String getUploadUrl() {
         return uploadUrl;
     }
 
     public String getFileFieldName() {
         return fileFieldName;
     }
 
     public CookieParser.Cookie[] getCookies() {
         return cookies;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public KeyValuePairParser.Pair[] getAdditionalPostParameters() {
         return additionalPostParameters;
     }
 
     public void updateFilename(String filename) {
         this.filename = filename;
     }
     
     public String getUploadMimeType() {
         return "application/zip";
     }
 
     public String getUploadFilename() {
         return urlencode(filename) + "-" + currentDate() + ".zip";
     }
 
     public String getRecordingFilename() {
         return "recording-" + currentDate() + ".au";
     }
 
     public int getMaxRecordingDuration() {
         return maxRecordingDuration;
     }
 
     public String getUserAgent() {
         return this.userAgent;
     }
 
     private String currentDate() {
         return new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
     }
 
     private String urlencode(String str) {
         try {
             return URLEncoder.encode(str, "UTF-8").replaceAll("\\.", "_");
         } catch (UnsupportedEncodingException e) {
             return "";
         }
     }
 
     private String composeUploadUrl(ParameterSource cfg, String relativePath) {
         return cfg.getBaseUri().resolve(relativePath).toString();
     }
 
     private String arg(ParameterSource conf, String argName, String defaultValue) {
         String val = conf.getParameter(argName);
         if (val == null || "".equals(val.trim()))
             return defaultValue;
         return val.trim();
     }
 
     private CookieParser.Cookie[] parseCookies(ParameterSource cfg) {
         String browserCookies = cfg.getCookies();
 
         CookieParser parser = new CookieParser(browserCookies);
         return parser.getAllCookies();
     }
 }
