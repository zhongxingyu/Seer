 package org.wings.plaf.css;
 
 import java.io.IOException;
 
 import org.wings.SBorderLayout;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SDimension;
 import org.wings.SForm;
 import org.wings.SGridBagLayout;
 import org.wings.SLayoutManager;
 import org.wings.session.ScriptManager;
 import org.wings.io.Device;
 import org.wings.plaf.css.script.LayoutFillScript;
 import org.wings.plaf.css.script.OnPageRenderedScript;
 
 /**
  * <code>TemplateIntegrationFormCG<code>.
  * <p/>
  * User: rrd
  * Date: 14.08.2007
  * Time: 16:42:21
  *
  * @author rrd
  * @version $Id
  */
 public class TemplateIntegrationFormCG extends FormCG implements org.wings.plaf.TemplateIntegrationFormCG {
     
     private static final long serialVersionUID = 1L;
 
     @Override
     public void writeInternal(final Device device, final SComponent component) throws IOException {
         final SForm form = (SForm) component;
         SLayoutManager layout = form.getLayout();
 
         // Prevent nesting of forms
         boolean formTagRequired = !form.getResidesInForm();
 
         if (formTagRequired) {
             // Is there a default button?
             String defaultButtonName = "undefined";
             if (form.getDefaultButton() != null) {
                 defaultButtonName = Utils.event(form.getDefaultButton());
             }
             StringBuilder script = new StringBuilder();
             script.append("wingS.update.defaultButtonName('").append(defaultButtonName).append("');");
             ScriptManager.getInstance().addScriptListener(new OnPageRenderedScript(script.toString()));
 
             device.print("<form method=\"");
             if (form.isPostMethod()) {
                 device.print("post");
             } else {
                 device.print("get");
             }
             device.print("\"");
             Utils.writeAllAttributes(device, form);
             Utils.optAttribute(device, "name", form.getName());
             Utils.optAttribute(device, "enctype", form.getEncodingType());
             Utils.optAttribute(device, "action", form.getRequestURL());
             Utils.writeEvents(device, form, null);
 
             // The "onsubmit"-handler of the form gets triggered
             // ONLY if the user submits it by pressing <enter> in
             // any of its fields. In all other cases - i.e. if a
             // button is clicked - the affected component fires its
             // "onclick"-event which calls "sendEvent(...)" which in
             // turn submits the form VIA JAVASCRIPT (either by means
             // of Ajax or the traditional way). Whenever forms are
             // submitted via JS (e.g. form.submit()) the "onsubmit"-
             // handler is NOT triggered. So once again, the code below
             // will only be executed when <enter> has been pressed.
             //
             // Therefore we can use this mechanism in order to handle
             // the default button of the form. (see SessionServlet)
             device.print(" onsubmit=\"wingS.request.sendEvent(");
             device.print("event,");
             device.print("true,");
             device.print(!component.isReloadForced());
             device.print(",'default_button', wingS.global.defaultButtonName); return false;\">");
 
             writeCapture(device, form);
 
             // This code is needed to trigger form events
             device.print("<input type=\"hidden\" name=\"");
             Utils.write(device, Utils.event(form));
             device.print("\" value=\"");
             Utils.write(device, form.getName());
             device.print(SConstants.UID_DIVIDER);
             device.print("\" />");
         }
 
         SDimension preferredSize = form.getPreferredSize();
         String height = preferredSize != null ? preferredSize.getHeight() : null;
        boolean clientLayout = isMSIE(form) && height != null && !"auto".equals(height)
             && (layout instanceof SBorderLayout || layout instanceof SGridBagLayout);
 
         String tableName = form.getName() + "_div";
         device.print("<div id=\"");
         device.print(tableName);
         device.print("\"");
 
         if (clientLayout) {
             device.print(" style=\"width:100%\"");
             Utils.optAttribute(device, "layoutHeight", height);
             form.getSession().getScriptManager().addScriptListener(new LayoutFillScript(tableName));
         }
         else
             Utils.printCSSInlineFullSize(device, form.getPreferredSize());
 
         device.print(">");
 
         // Render the container itself
         Utils.renderContainer(device, form);
 
         device.print("</div>");
 
         if (formTagRequired) {
             writeCapture(device, form);
             device.print("</form>");
         }
     }
 
     /*
      * we render two icons into the page that captures pressing simple 'return'
      * in the page. Why ? Depending on the Browser, the Browser sends the
      * first or the last submit-button it finds in the page as 'default'-Submit
      * when we simply press 'return' somewhere.     *
      * However, we don't want to have this arbitrary behaviour in wingS.
      * So we add these two (invisible image-) submit-Buttons, either of it
      * gets triggered on simple 'return'.
      *
      * Formerly this mechanism was also used for the default button handling of
      * the form. This is now done further above by the "onsubmit"-handler. However,
      * we still need theses two images in order to always get the latter invoked.
      *
      * Watchout: the style of these images once had been changed to display:none;
      * to prevent taking some pixel renderspace. However, display:none; made
      * the Internet Explorer not accept this as an input getting the default-focus,
      * so it fell back to the old behaviour. So changed that style to no-padding,
      * no-margin, no-whatever (HZ).
      */
     private void writeCapture(Device device, SForm form) throws IOException {
         // Whenever a form is submitted via JS (like done in this case - see above)
         // a input field of type image (like the one below) won't be sent. This is
         // because for some reason it doesn't belong to the "form.elements"-collection
         // which is eventually used to assemble the post-parameters. That's why we
         // don't even name it - would be useless anyway...
         device.print("<input type=\"image\" border=\"0\" ");
         Utils.optAttribute(device, "src", getBlindIcon().getURL());
         device.print(" width=\"0\" height=\"0\" tabindex=\"-1\"" +
                 " style=\"border:none;padding:0px;margin:0px;position:absolute\"/>");
     }
 }
