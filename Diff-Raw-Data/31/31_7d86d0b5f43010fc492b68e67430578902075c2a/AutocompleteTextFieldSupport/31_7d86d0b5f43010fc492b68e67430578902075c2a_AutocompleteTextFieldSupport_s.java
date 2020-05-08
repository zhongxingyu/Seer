 package wicket.contrib.scriptaculous.autocomplete;
 
 import java.util.HashMap;
 
 import wicket.AttributeModifier;
 import wicket.MarkupContainer;
 import wicket.ResourceReference;
 import wicket.contrib.scriptaculous.JavascriptBuilder;
 import wicket.contrib.scriptaculous.ScriptaculousAjaxBehavior;
 import wicket.markup.MarkupStream;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.form.TextField;
 import wicket.model.Model;
 
 /**
  * support class for all autocomplete text fields. handles binding of needed css
  * and javascript.
  *
  * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
  */
 public abstract class AutocompleteTextFieldSupport<T> extends TextField<T>
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Construct.
 	 *
 	 * @param id
 	 */
 	public AutocompleteTextFieldSupport(MarkupContainer parent, String id)
 	{
 		super(parent, id);
 		add(ScriptaculousAjaxBehavior.newJavascriptBindingBehavior());
 		add(new AttributeModifier("autocomplete", new Model("off")));
 
 		setOutputMarkupId(true);
 	}
 
	@Override
	public final void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.renderCSSReference(getCss());

		JavascriptBuilder builder = new JavascriptBuilder();
		builder.addLine("new " + getAutocompleteType() + "(");
		builder.addLine("  '" + getMarkupId() + "', ");
		builder.addLine("  '" + getAutocompleteId() + "', ");
		builder.addLine("  '" + getThirdAutocompleteArgument() + "', ");
		builder.addOptions(new HashMap());
		builder.addLine(");");
		response.getResponse().write(builder.buildScriptTagString());
	}

 	protected abstract String getThirdAutocompleteArgument();
 
 	protected abstract String getAutocompleteType();
 
 	protected final String getAutocompleteId()
 	{
 		return getMarkupId() + "_autocomplete";
 	}
 
 	/**
 	 * extension point to customize what css is used to style the component.
 	 * @return
 	 */
 	protected ResourceReference getCss()
 	{
 		return new ResourceReference(AutocompleteTextFieldSupport.class, "style.css");
 	}
 
 	/**
 	 * adds a placeholder div where auto completion results will be populated.
 	 */
 	protected final void onRender(MarkupStream markupStream)
 	{
 		super.onRender(markupStream);
 
 		getResponse().write(
 				"<div class=\"auto_complete\" id=\"" + getAutocompleteId() + "\"></div>");
 	}
 }
