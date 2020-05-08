 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.ajax4jsf.renderkit.compiler;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.faces.FacesException;
 
 import org.ajax4jsf.Messages;
 import org.ajax4jsf.util.style.CSSFormat;
 import org.xml.sax.SAXException;
 
 /**
  * @author asmirnov@exadel.com (latest modification by $Author: alexsmirnov $)
  * @version $Revision: 1.1.2.1 $ $Date: 2007/01/09 18:57:43 $
  * 
  */
 public class ClassElement extends ElementBase {
 
 	private String _name;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ajax4jsf.renderkit.compiler.ElementBase#encode(org.ajax4jsf.renderkit.compiler.TemplateContext,
 	 *      java.lang.String)
 	 */
 	public void encode(TemplateContext context, String breakPoint)
 			throws IOException {
 		encode(context);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ajax4jsf.renderkit.compiler.ElementBase#encode(org.ajax4jsf.renderkit.compiler.TemplateContext)
 	 */
 	public void encode(TemplateContext context) throws IOException {
		// To fix https://jira.jboss.org/jira/browse/RF-7992 , writeText was replaced by 'write'
		context.getWriter().write(getString(context));
 	}
 
 	/**
 	 * @return Returns the name.
 	 */
 	public String getName() {
 		return _name;
 	}
 
 	/**
 	 * @param name
 	 *            The name to set.
 	 */
 	public void setName(String name) {
 		_name = name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ajax4jsf.renderkit.compiler.ElementBase#getString(org.ajax4jsf.renderkit.compiler.TemplateContext)
 	 */
 	public String getString(TemplateContext context) throws FacesException {
 		if (null == getName()) {
 			throw new FacesException(Messages
 					.getMessage(Messages.STYLE_ATTRIBUTE_ERROR));
 		}
 		String stringOrDefault = valueGetter.getStringOrDefault(context);
 		StringBuffer string = new StringBuffer();
 		string.append(stringOrDefault);
 		for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
 			Object element = iter.next();
 			if (element instanceof ElementBase) {
 				ElementBase stringElement = (ElementBase) element;
 				String str = stringElement.getString(context);
 				if (str.length() > 0) {
 					string.append(" ");
 					boolean isUrl = element instanceof ResourceElement;
 					if (isUrl) {
 						string.append(CSSFormat.url(str));
 					} else {
 						string.append(str);
 					}
 				}
 			}
 		}
 		if (string.length() > 0) {
 			string.insert(0, ":").insert(0, getName()).append(";");
 			return string.toString();
 		} else {
 			return "";
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ajax4jsf.renderkit.compiler.PreparedTemplate#getTag()
 	 */
 	public String getTag() {
 		// TODO Auto-generated method stub
 		return HtmlCompiler.NS_PREFIX + HtmlCompiler.STYLE_TAG;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ajax4jsf.renderkit.compiler.ElementBase#setParent(org.ajax4jsf.renderkit.compiler.PreparedTemplate)
 	 */
 	public void setParent(PreparedTemplate parent) throws SAXException {
 		super.setParent(parent);
 		if (getName() == null) {
 			throw new SAXException(Messages.getMessage(
 					Messages.NO_NAME_ATTRIBUTE_ERROR, getTag()));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.ajax4jsf.renderkit.compiler.ElementBase#getAllowedClasses()
 	 */
 	protected Class[] getAllowedClasses() {
 		// TODO Auto-generated method stub
 		return new Class[] { MethodCallElement.class, TextElement.class,
 				ResourceElement.class };
 	}
 
 }
