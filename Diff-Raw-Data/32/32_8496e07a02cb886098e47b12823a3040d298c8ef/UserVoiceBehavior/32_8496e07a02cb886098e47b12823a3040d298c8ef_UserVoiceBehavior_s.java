 package de.flower.common.ui.behavior;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 
 /**
  * Renders uservoice feedback tab into page.
  * @author flowerrrr
  */
public class UserVoiceBehavior extends Behavior {
 
     @Override
     public void renderHead(final Component component, final IHeaderResponse response) {
         String token = getToken();
         if (token == null) {
             return;
         }
         String javascript = "var uv = document.createElement('script'); uv.type = 'text/javascript'; uv.async = true;\n" +
                 "    uv.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'widget.uservoice.com/" + token + ".js';\n" +
                 "    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(uv, s);";
         response.renderOnDomReadyJavaScript(javascript);
     }
 
    protected String getToken() {
        // return "mNTsoCUuSEaF5qjGfDGIuQ";
         return null;
    }

 }
