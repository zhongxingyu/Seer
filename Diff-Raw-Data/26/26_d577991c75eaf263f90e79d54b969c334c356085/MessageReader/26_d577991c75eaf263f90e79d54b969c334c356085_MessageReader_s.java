 package de.consistec.syncframework.common.i18n;
 
 import static de.consistec.syncframework.common.util.CollectionsUtil.newConcurrentHashMap;
 
 import de.consistec.syncframework.common.util.DefaultMessageConveyorSearcher;
 
 import ch.qos.cal10n.IMessageConveyor;
 import ch.qos.cal10n.util.AnnotationExtractor;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * @company Consistec Engineering and Consulting GmbH
  * @date 29.11.2012 10:09:04
  * @author Piotr Wieczorek
  * @since 0.0.1-SNAPSHOT
  */
 public final class MessageReader {
 
 //<editor-fold defaultstate="expanded" desc=" Class fields " >
 
     /**
      * Default conveyor is specified as field to avoid unnecessary searches through the map.
      */
     private static final IMessageConveyor DEFAULT_MESSAGE_CONVEYOR = DefaultMessageConveyorSearcher.search();
    private static Map<Locale, IMessageConveyor> cache = newConcurrentHashMap(5);
 //</editor-fold>
 
     static {
        cache.put(Locale.getDefault(), DEFAULT_MESSAGE_CONVEYOR);
     }
 
 //<editor-fold defaultstate="expanded" desc=" Class constructors " >
     private MessageReader() {
         throw new AssertionError("No instances allowed");
     }
 
 //</editor-fold>
 //<editor-fold defaultstate="expanded" desc=" Class methods " >
     /**
      * Reads translated string.
      * Method searches the ResourceBundle for default system locale, if provided, and if not, it searches the English
      * ResourceBundle, distributed with framework.
      *
      * @param keysEnum Key for searching the string in a ResourceBundle
      * @param args Optional arguments for message
      * @return Message from ResourceBundle for default locale.
      */
     public static String read(Enum<?> keysEnum, Object... args) {
         checkEnum(keysEnum);
         return DEFAULT_MESSAGE_CONVEYOR.getMessage(keysEnum, args);
     }
 
     private static void checkEnum(Enum<?> keysEnum) {
         if (AnnotationExtractor.getBaseName(keysEnum.getDeclaringClass()) == null) {
             throw new IllegalArgumentException("Provided enumeration is not an CAL10n enum!");
         }
     }
 
 //    This method will be needed in future, for various bundles.
 //    public static IMessageConveyor lookup(Locale locale) {
 //        IMessageConveyor conveyor = cache.get(locale);
 //        if (conveyor == null) {
 //            conveyor = new MessageConveyor(locale);
 //            cache.put(locale, conveyor);
 //        }
 //        return conveyor;
 //    }
 
 //</editor-fold>
 }
