 package com.gentics.cr.taglib.servlet;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import com.gentics.api.portalnode.connector.PLinkReplacer;
 import com.gentics.cr.CRException;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.rendering.ContentRenderer;
 
 /**
  * @author norbert
  * Implementation of a tag that renders content with plink replacing and velocity
  */
 public class RenderContentTag extends TagSupport {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -940890400477192009L;
 
 	/**
 	 * Name of the render request attribute for the instance of {@link ContentRenderer}
 	 */
 	public final static String RENDERER_PARAM = "rendercontenttag.renderer";
 
 	/**
 	 * Name of the render request attribute for the instance of {@link PLinkReplacer}
 	 */
 	public final static String PLINK_PARAM = "rendercontenttag.plinkreplacer";
 
 	/**
 	 * Rendered object
 	 */
 	protected CRResolvableBean object;
 
 	/**
 	 * name of the rendered attribute
 	 */
 	protected String contentAttribute = "content";
 	
 	protected String var = null;
 
 	/**
 	 * flag if the output should be urlencoded
 	 */
 	protected boolean urlencode = false;
 
 	/**
 	 * Set the object to be rendered. Must be an instance of {@link CRResolvableBean}.
 	 * @param object rendered object
 	 */
 	public void setObject(Object object) {
 		if (object instanceof CRResolvableBean) {
 			this.object = (CRResolvableBean) object;
 		}
 	}
 
 	/**
 	 * Set the content attribute to be rendered
 	 * @param contentAttribute name of the rendered content attribute
 	 */
 	public void setContentAttribute(String contentAttribute) {
 		this.contentAttribute = contentAttribute;
 	}
 	
 	/**
 	 * Set the content attribute to be rendered
 	 * @param contentAttribute name of the rendered content attribute
 	 */
 	public void setUrlencode(String urlencode) {
 		this.urlencode = "true".equals(urlencode);
 	}
 	
 	/**
 	 * 
 	 * @param var
 	 */
 	public void setVar(String var) {
 		this.var = var;
 	}
 	
 	
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
 	 */
 	public int doEndTag() throws JspException {
 		// get the ContentRenderer
 		ServletRequest renderRequest = getServletRequest();
 
 		ContentRenderer renderer = (ContentRenderer)renderRequest.getAttribute(RENDERER_PARAM);
 		PLinkReplacer pLinkReplacer = (PLinkReplacer)renderRequest.getAttribute(PLINK_PARAM);
 
 		try {
 			if (object != null) {
 				try {
 					String content = renderer.renderContent(object, contentAttribute, true, pLinkReplacer, false, null);
 					if (urlencode) {
 						content = URLEncoder.encode(content, "UTF-8");
 					}
 					if (var != null) {
 						if (content!=null && "".equals(content)) {
 							content = null;
 						}
 						pageContext.setAttribute(var, content, PageContext.REQUEST_SCOPE);
 					} else {
 						pageContext.getOut().write(content);
 					}
 					
 				} catch (CRException e) {
 					throw new JspException("Error while rendering object "
 							+ object.getContentid(), e);
 				}
 			} else {
 				pageContext.getOut().write(" -- no object set --");
 			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 		return super.doEndTag();
 	}
 
 	/**
 	 * Get the servlet request
 	 * 
 	 * @return servlet request
 	 * @throws JspException
 	 *             when the servlet request could not be found
 	 */
 	protected ServletRequest getServletRequest() throws JspException {
 		Object renderRequestObject = pageContext.findAttribute("javax.servlet.ServletRequest");
 
 		if (renderRequestObject instanceof ServletRequest) {
 			return (ServletRequest) renderRequestObject;
 		} else {
 			throw new JspException(
					"Error while rendering tag: could not find javax.servlet.ServletRequest");
 		}
 	}
 
 }
