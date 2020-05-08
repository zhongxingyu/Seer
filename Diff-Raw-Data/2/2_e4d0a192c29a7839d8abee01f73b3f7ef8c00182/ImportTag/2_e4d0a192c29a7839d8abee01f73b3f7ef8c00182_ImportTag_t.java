 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.deliver.taglib.common;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.jsp.JspException;
 
 import org.apache.log4j.Logger;
 import org.infoglue.deliver.taglib.TemplateControllerTag;
 import org.infoglue.deliver.util.HttpHelper;
 
 public class ImportTag extends TemplateControllerTag 
 {
 	private static final long serialVersionUID = 4050206323348354355L;
 
 	private final static Logger logger = Logger.getLogger(ImportTag.class.getName());
 
 	private String url;
 	private String charEncoding;
 	private Map requestParameters = new HashMap();
 	private Integer timeout = new Integer(30000);
 	
 	private HttpHelper helper = new HttpHelper();
 	
     public ImportTag()
     {
         super();
     }
 
 	/**
 	 * Initializes the parameters to make it accessible for the children tags (if any).
 	 * 
 	 * @return indication of whether to evaluate the body or not.
 	 * @throws JspException if an error occurred while processing this tag.
 	 */
 	public int doStartTag() throws JspException 
 	{
 		return EVAL_BODY_INCLUDE;
 	}
 
 	/**
 	 * Generates the url and either sets the result attribute or writes the url
 	 * to the output stream. 
 	 * 
 	 * @return indication of whether to continue evaluating the JSP page.
 	 * @throws JspException if an error occurred while processing this tag.
 	 */
 
 	public int doEndTag() throws JspException
     {
 		try
         {
			String result = helper.getUrlContent(url, requestParameters, charEncoding, timeout.intValue());
 		    produceResult(result);
         } 
 		catch (Exception e)
         {
             logger.error("An error occurred when we tried during (" + timeout + " ms) to import the url:" + this.url + ":" + e.getMessage());
 		    produceResult("");
         }
 		
         return EVAL_PAGE;
     }
 
     public void setUrl(String url) throws JspException
     {
         this.url = evaluateString("importTag", "url", url);
     }
     
     public void setCharEncoding(String charEncoding) throws JspException
     {
         this.charEncoding = evaluateString("importTag", "charEncoding", charEncoding);
     }
     
     public void setTimeout(String timeout) throws JspException
     {
         this.timeout = evaluateInteger("importTag", "timeout", timeout);
     }
 
     protected final void addParameter(final String name, final String value)
 	{
 		requestParameters.put(name, value);
 	}
 
 }
