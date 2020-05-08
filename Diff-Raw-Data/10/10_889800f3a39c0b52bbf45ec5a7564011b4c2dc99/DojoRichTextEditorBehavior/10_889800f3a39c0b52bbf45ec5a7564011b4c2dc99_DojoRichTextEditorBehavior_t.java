 package wicket.contrib.dojo;
 
 import wicket.AttributeModifier;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.markup.ComponentTag;
 import wicket.model.Model;
 
 /**
 * Behavior that allows a TextArea to show as the Dojo Rich Text editor
  * 
  * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
  */
 public class DojoRichTextEditorBehavior extends AbstractRequireDojoBehavior {
 	public void setRequire(RequireDojoLibs libs) {
 		libs.add("dojo.widget.Editor2");
 	}
 
 	protected void respond(AjaxRequestTarget arg0) {
 		// Do nothing
 	}
 
 	protected void onBind() {
 		getComponent().add(new AttributeModifier("dojoType", true, new Model("Editor2")));
 	}
 
 	protected void onComponentTag(ComponentTag tag) {
 		if (! tag.getName().equals("textarea"))
 			throw new IllegalArgumentException("Dojo Rich Text Editor works with a <textarea>, found a <" + tag.getName() + ">");
 		super.onComponentTag(tag);
 	}
 }
