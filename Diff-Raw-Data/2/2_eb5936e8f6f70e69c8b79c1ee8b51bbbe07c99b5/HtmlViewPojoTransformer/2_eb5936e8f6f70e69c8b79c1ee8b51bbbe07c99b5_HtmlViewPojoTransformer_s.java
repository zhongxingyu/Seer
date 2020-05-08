 package org.romaframework.aspect.view.html.transformer.plain;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.aspect.view.html.area.HtmlViewBinder;
 import org.romaframework.aspect.view.html.area.HtmlViewRenderable;
 import org.romaframework.aspect.view.html.binder.NullBinder;
 import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.component.HtmlViewGenericComponent;
 import org.romaframework.aspect.view.html.transformer.AbstractHtmlViewTransformer;
 import org.romaframework.aspect.view.html.transformer.Transformer;
 
 public class HtmlViewPojoTransformer extends AbstractHtmlViewTransformer implements Transformer {
 
 	private static final Log		LOG		= LogFactory.getLog(HtmlViewPojoTransformer.class);
 
	public static final String	NAME	= "POJO";
 
 	public HtmlViewBinder getBinder(HtmlViewRenderable renderable) {
 		return NullBinder.getInstance();
 	}
 
 	public void transformPart(final HtmlViewRenderable component, final String part, Writer writer) throws IOException {
 		final HtmlViewConfigurableEntityForm form = (HtmlViewConfigurableEntityForm) component;
 		String htmlClass = helper.getHtmlClass(this.toString(), null, (HtmlViewGenericComponent) component);
 		String htmlId = helper.getHtmlId(form, null);
 
 		writer.write("<div class=\"");
 		writer.write(htmlClass);
 		writer.write("\" id=\"");
 		writer.write(htmlId);
 		writer.write("\">\n");
 
 		form.getRootArea().render(writer);
 		writer.write("</div>\n");
 	}
 
 	@Override
 	public String toString() {
 		return NAME;
 	}
 
 	public String getType() {
 		return Transformer.PRIMITIVE;
 	}
 }
