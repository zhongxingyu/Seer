 package org.romaframework.aspect.view.html.taglib;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.BodyTagSupport;
 
 import org.romaframework.aspect.view.html.HtmlViewAspectHelper;
import org.romaframework.aspect.view.html.component.HtmlViewConfigurableEntityForm;
 import org.romaframework.aspect.view.html.constants.RequestConstants;
 
 public class RomaAddJsTag extends BodyTagSupport {
 
 	private static final long	serialVersionUID	= 1L;
 
 	@Override
 	public int doAfterBody() throws JspException {
		final Object currentForm = pageContext.getRequest().getAttribute(RequestConstants.CURRENT_REQUEST_FORM);
		if (currentForm != null && currentForm instanceof HtmlViewConfigurableEntityForm) {
 			try {
				HtmlViewConfigurableEntityForm form = (HtmlViewConfigurableEntityForm) currentForm;
 				String body = getBodyContent().getString();
 				HtmlViewAspectHelper.getJsBuffer().setScript(form.getHtmlId(), body);
 			} catch (final Exception e) {
 				try {
 					pageContext.getOut().print("Error processing AddJs tag: " + e);
 				} catch (final Exception ex) {
 				}
 			}
 		}
 		return SKIP_BODY;
 	}
 
 }
