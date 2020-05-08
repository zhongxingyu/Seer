 package wicket.contrib.scriptaculous.inplaceeditor;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import wicket.RequestCycle;
 import wicket.ajax.AjaxRequestTarget;
 import wicket.behavior.AbstractAjaxBehavior;
 import wicket.contrib.scriptaculous.JavascriptBuilder;
 import wicket.contrib.scriptaculous.ScriptaculousAjaxHandler;
 import wicket.markup.ComponentTag;
 import wicket.markup.MarkupStream;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.form.AbstractTextComponent;
 import wicket.markup.html.form.FormComponent;
 import wicket.model.IModel;
 import wicket.request.target.basic.StringRequestTarget;
 
 /**
  * @see http://wiki.script.aculo.us/scriptaculous/show/Ajax.InPlaceEditor
  *
  * @author <a href="mailto:wireframe6464@sf.net">Ryan Sonnek</a>
  */
 public class AjaxEditInPlaceLabel extends AbstractTextComponent {
 	private AbstractAjaxBehavior handler;
 	private AbstractAjaxBehavior onCompleteHandler;
 	private Map options = new HashMap();
 
 	public AjaxEditInPlaceLabel(WebMarkupContainer parent, String wicketId, IModel model) {
 		super(parent, wicketId);
 		setModel(model);
 
 		this.handler = new ScriptaculousAjaxHandler() {
 
 			public void onRequest() {
 				FormComponent formComponent = (FormComponent) getComponent();
 				formComponent.validate();
 				if (formComponent.isValid()) {
 					formComponent.updateModel();
 				}
 				String value = formComponent.getValue();
 
 				RequestCycle.get().setRequestTarget(new StringRequestTarget(value));
 			}
 		};
 		add(handler);
 		onCompleteHandler = new ScriptaculousAjaxHandler() {
 			public void onRequest() {
 				AjaxRequestTarget target = new AjaxRequestTarget();
 				getRequestCycle().setRequestTarget(target);
 				target.appendJavascript("new Effect.Highlight('" + getMarkupId() + "')");
 
 				onComplete(target);
 			}
 
 		};
 		add(onCompleteHandler);
 
 		options.put("onComplete", new JavascriptBuilder.JavascriptFunction("function() { wicketAjaxGet('" + onCompleteHandler.getCallbackUrl() + "'); }"));
 		setOutputMarkupId(true);
 	}
 
 	public String getInputName() {
 		return "value";
 	}
 
 	/**
 	 * configure use of okButton for InPlaceEditor.
 	 * @param value
 	 */
 	public void setOkButton(boolean value) {
 		options.put("okButton", Boolean.valueOf(value));
 	}
 
 	public void setCancelLink(boolean value) {
 		options.put("cancelLink", Boolean.valueOf(value));
 	}
 
 	public void setSubmitOnBlur(boolean value) {
 		options.put("submitOnBlur", Boolean.valueOf(value));
 	}
 
 	public void setRows(int rows) {
 		options.put("rows", new Integer(rows));
 	}
 
 	/**
 	 * Handle the container's body.
 	 *
 	 * @param markupStream
 	 *            The markup stream
 	 * @param openTag
 	 *            The open tag for the body
 	 * @see wicket.Component#onComponentTagBody(MarkupStream, ComponentTag)
 	 */
 	protected final void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
 		replaceComponentTagBody(markupStream, openTag, getValue());
 	}
 
 	protected void onRender(MarkupStream markupStream) {
 		super.onRender(markupStream);
 
 		JavascriptBuilder builder = new JavascriptBuilder();
 		builder.addLine("new Ajax.InPlaceEditor('" + getMarkupId() + "', ");
 		builder.addLine("  '" + handler.getCallbackUrl() + "', ");
 		builder.addOptions(options);
 		builder.addLine(");");
 		getResponse().write(builder.buildScriptTagString());
 	}
 
 	/**
 	 * extension point to override default onComplete behavior.
 	 */
 	protected void onComplete(final AjaxRequestTarget target) {
 	}

	protected void addOption(String key, Object value) {
		options.put(key, value);
	}
 }
