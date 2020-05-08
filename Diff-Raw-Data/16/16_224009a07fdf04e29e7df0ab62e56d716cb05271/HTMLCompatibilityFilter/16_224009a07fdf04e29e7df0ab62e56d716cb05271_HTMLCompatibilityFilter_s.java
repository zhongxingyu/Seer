 package au.com.miskinhill.web.util;
 
 import java.util.regex.Pattern;
 
 import org.springframework.stereotype.Component;
 
 @Component("htmlCompatibilityFilter")
 public class HTMLCompatibilityFilter extends HttpResponseBufferingFilter {
     
     /* XXX lame */
     
     private static final Pattern SELF_CLOSING_SCRIPT_PATTERN = Pattern.compile("<script(\\s[^>]*?)?\\s*/>");
     private static final Pattern SELF_CLOSING_SPAN_PATTERN = Pattern.compile("<span(\\s[^>]*?)?\\s*/>");
     private static final Pattern SELF_CLOSING_DIV_PATTERN = Pattern.compile("<div(\\s[^>]*?)?\\s*/>");
     private static final Pattern SELF_CLOSING_ABBR_PATTERN = Pattern.compile("<abbr(\\s[^>]*?)?\\s*/>");
     private static final Pattern SELF_CLOSING_A_PATTERN = Pattern.compile("<a(\\s[^>]*?)?\\s*/>");
     private static final Pattern SELF_CLOSING_TEXTAREA_PATTERN = Pattern.compile("<textarea(\\s[^>]*?)?\\s*/>");
     
     @Override
     protected String postprocessResponse(String responseBody) {
         responseBody = SELF_CLOSING_SCRIPT_PATTERN.matcher(responseBody).replaceAll("<script$1></script>");
         responseBody = SELF_CLOSING_SPAN_PATTERN.matcher(responseBody).replaceAll("<span$1></span>");
         responseBody = SELF_CLOSING_DIV_PATTERN.matcher(responseBody).replaceAll("<div$1></div>");
         responseBody = SELF_CLOSING_ABBR_PATTERN.matcher(responseBody).replaceAll("<abbr$1></abbr>");
         responseBody = SELF_CLOSING_A_PATTERN.matcher(responseBody).replaceAll("<a$1></a>");
         responseBody = SELF_CLOSING_TEXTAREA_PATTERN.matcher(responseBody).replaceAll("<textarea$1></textarea>");
         return responseBody;
    };
 
 }
