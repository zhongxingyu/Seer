 package wicket.contrib.dojo;
 
 import wicket.AttributeModifier;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.markup.ComponentTag;
 import wicket.model.Model;
 
 /**
 * Behavior that extends a TextArea to load the Dojo Rich Text editor
  * 
  * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
  */
 public class DojoRichTextEditorBehavior extends AbstractRequireDojoBehavior {
	@Override
 	public void setRequire(RequireDojoLibs libs) {
 		libs.add("dojo.widget.Editor2");
 	}
 
	@Override
 	protected void respond(AjaxRequestTarget arg0) {
 		// Do nothing
 	}
 
	@Override
 	protected void onBind() {
 		getComponent().add(new AttributeModifier("dojoType", true, new Model("Editor2")));
 	}
 
	@Override
 	protected void onComponentTag(ComponentTag tag) {
 		if (! tag.getName().equals("textarea"))
 			throw new IllegalArgumentException("Dojo Rich Text Editor works with a <textarea>, found a <" + tag.getName() + ">");
 		super.onComponentTag(tag);
 	}
 }
