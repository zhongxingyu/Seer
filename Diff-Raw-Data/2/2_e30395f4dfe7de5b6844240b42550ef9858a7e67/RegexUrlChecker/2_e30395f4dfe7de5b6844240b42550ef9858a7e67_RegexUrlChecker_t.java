 package gnutch.urls;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import java.lang.Thread;
 import java.util.regex.Pattern;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 class RegexUrlChecker {
     @Autowired PatternService patternService;
 
     private static final ThreadLocal<RegexUrlChecker> checkerLocal =
         new ThreadLocal<RegexUrlChecker>() {
         @Override 
         protected RegexUrlChecker initialValue() {
             return new RegexUrlChecker();
         }
     };
 
     private Log log = LogFactory.getLog(RegexUrlChecker.class)  ;
     protected RegexUrlChecker(){
         log.trace("RegexUrlChecker instance created. Thread:" + Thread.currentThread().getId());
     };
     /**
      * Using pre-define list of ignore patterns
      * checks the {@param url}. If {@param url} matches any pattern from the list
      * <false> is returned. If does not match any - return <true>
      */
     public boolean check(String url){
         boolean result; 
         boolean allowedResult = false, ignoredResult = false;
         
         for(Pattern pattern: patternService.getAllowedPatterns()){
             allowedResult |= pattern.matcher(url).matches();
         }
 
         for(Pattern pattern: patternService.getIgnoredPatterns()){
             ignoredResult |= pattern.matcher(url).matches();
         }
 
         result = (allowedResult && (ignoredResult == false));
        log.trace("Checking " + url + ": " + result);
         return result;
     };
 
     public static RegexUrlChecker getInstance(){
         return checkerLocal.get();
     }
 }
