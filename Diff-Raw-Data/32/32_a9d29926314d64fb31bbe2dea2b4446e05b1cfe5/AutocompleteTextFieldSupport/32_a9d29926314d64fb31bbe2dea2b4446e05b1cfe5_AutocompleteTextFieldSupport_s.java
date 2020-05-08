 package org.wicketstuff.scriptaculous.autocomplete;
 
 import java.util.HashMap;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.behavior.HeaderContributor;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.Model;
 import org.wicketstuff.scriptaculous.JavascriptBuilder;
 import org.wicketstuff.scriptaculous.ScriptaculousAjaxBehavior;
 
 
 /**
  * support class for all autocomplete text fields. 
  * handles binding of required css and javascript.
  *
  * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
  */
 public abstract class AutocompleteTextFieldSupport extends TextField
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Construct.
 	 *
 	 * @param id
 	 */
 	public AutocompleteTextFieldSupport(String id)
 	{
 		super(id);
 		add(ScriptaculousAjaxBehavior.newJavascriptBindingBehavior());
 		add(new AttributeModifier("autocomplete", new Model("off")));
 		add(HeaderContributor.forCss(getCss(), "screen"));
 
 		setOutputMarkupId(true);
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
 		
 		JavascriptBuilder builder = new JavascriptBuilder();
 		builder.addLine("new " + getAutocompleteType() + "(");
 		builder.addLine("  '" + getMarkupId() + "', ");
 		builder.addLine("  '" + getAutocompleteId() + "', ");
		builder.addLine("  '" + getThirdAutocompleteArgument() + "', ");
 		builder.addOptions(new HashMap());
 		builder.addLine(");");
 		getResponse().write(builder.buildScriptTagString());
 	}
 }
