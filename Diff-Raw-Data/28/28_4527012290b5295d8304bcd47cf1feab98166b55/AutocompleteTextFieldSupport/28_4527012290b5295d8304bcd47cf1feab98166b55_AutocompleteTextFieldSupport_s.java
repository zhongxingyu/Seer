 package wicket.contrib.scriptaculous.autocomplete;
 
 import wicket.ResourceReference;
 import wicket.contrib.scriptaculous.ScriptaculousAjaxHandler;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.PackageResourceReference;
 import wicket.markup.html.form.TextField;
 import wicket.markup.html.internal.HtmlHeaderContainer;
 
 /**
  * support class for all autocomplete text fields.
  * handles binding of needed css and javascript.
  *
  * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
  */
 public class AutocompleteTextFieldSupport extends TextField {
 
     public AutocompleteTextFieldSupport(String id) {
         super(id);
 		add(ScriptaculousAjaxHandler.newJavascriptBindingHandler());
     }
 
     protected void onComponentTag(ComponentTag tag) {
         super.onComponentTag(tag);
         tag.put("id", getId());
         tag.put("autocomplete", "off");
     }
 
     public void renderHead(HtmlHeaderContainer container) {
         super.renderHead(container);
 
         addCssReference(container, getCss());
     }
 
     private void addCssReference(HtmlHeaderContainer container, ResourceReference ref) {
         String url = container.getPage().urlFor(ref.getPath());
         String s =
             "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + url + "\"/>\n";
         write(container, s);
     }
 
     /**
      * Writes the given string to the header container.
      * @param container the header container
      * @param s the string to write
      */
     private void write(HtmlHeaderContainer container, String s) {
         container.getResponse().write(s);
     }
 
     protected ResourceReference getCss() {
         return new PackageResourceReference(AutocompleteTextFieldSupport.class, "style.css");
     }
 
     /**
      * adds a placeholder div where auto completion results will be populated.
      */
    protected void onRender() {
        super.onRender();
 
         getResponse().write("<div class=\"auto_complete\" id=\"" + getAutocompleteId()  + "\"></div>");
     }
 
     protected final String getAutocompleteId() {
         return getId() + "_autocomplete";
     }
 }
