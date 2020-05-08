 package ecologylab.services.distributed.common;
 
 import ecologylab.xml.TranslationScope;
 
 /**
  * Constants that define general ecologylab objects that get stored in the
  * Session ObjectRegistry.
  * 
  * @author andruid
  * 
  */
 public interface SessionObjects
 {
     public static final String           MAIN_START_AND_STOPPABLE      = "main_start_and_stoppable";
 
     public static final String           MAIN_SHUTDOWNABLE             = "main_shutdownable";
 
     public static final TranslationScope BROWSER_SERVICES_TRANSLATIONS = TranslationScope
                                                                                .get(
                                                                                        "Browse",
                                                                                        "ecologylab.services.messages");
 
     public static final String           BROWSER_SERVICES_CLIENT       = "browser_services_client";
 
     public static final String           LOGGING                       = "logging";
 
     public static final String           TOP_LEVEL                     = "top_level";
 }
