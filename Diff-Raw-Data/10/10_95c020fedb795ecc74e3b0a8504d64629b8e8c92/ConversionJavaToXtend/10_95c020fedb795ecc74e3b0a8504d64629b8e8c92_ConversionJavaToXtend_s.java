 package nyao.util;
 
 import java.util.List;
 
 import static com.google.gwt.query.client.GQuery.*;
 
 import com.google.gwt.query.client.Function;
 import com.google.gwt.query.client.GQuery;
 import com.google.gwt.user.client.Element;
 
 public final class ConversionJavaToXtend {
 
     public static <T extends GQuery> T gqAs(GQuery src, Class<T> plugin) {
         return src.as(plugin);
     }
     
     public static String gqVal(GQuery src) {
         return src.val();
     }
 
     public static GQuery gqVal(GQuery src, String name) {
         return src.val(name);
     }
     
     public native static void callTableDnD(String src) /*-{
         $wnd.jQuery(src).tableDnD();
     }-*/;
     
     public native static void calltableDnDUpdate(String src) /*-{
         $wnd.jQuery(src).tableDnDUpdate();
     }-*/;
     
     public static String[] mapByAttr(GQuery gq, final String key) {
         List<String> result = gq.map(new Function() {
             @Override
             public Object f(Element e, int i) {
                 return $(e).attr(key);
             }
         });
         return result.toArray(new String[0]);
     }
 }
