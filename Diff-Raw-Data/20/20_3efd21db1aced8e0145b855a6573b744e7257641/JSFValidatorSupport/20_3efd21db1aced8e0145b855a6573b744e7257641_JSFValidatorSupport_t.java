 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.dom;
 
 import javax.xml.namespace.QName;
 
 import org.eclipse.jst.jsf.core.internal.tld.IJSFConstants;
 import org.eclipse.jst.jsf.core.internal.tld.ITLDConstants;
 import org.eclipse.jst.pagedesigner.utils.BodyHelper;
 import org.eclipse.jst.pagedesigner.validation.caret.JSFRootContainerPositionRule;
 import org.w3c.dom.Document;
 
 /**
  * This is a temparary class, for automatically insert "view" and "form" tag
  * when user drag from palette those controls that need them. it is still pretty
  * hardcoded, in the future we may need some extendable mechanism.
  * 
  * @author mengbo
  * @version 1.5
  */
 public class JSFValidatorSupport {
 	static private QName _qnameView = new QName(ITLDConstants.URI_JSF_CORE,
 			IJSFConstants.TAG_VIEW);
 
 	static private QName _qnameForm = new QName(ITLDConstants.URI_JSF_HTML,
 			IJSFConstants.TAG_FORM);
 
 //	static private QName[] _views = new QName[] { _qnameView,
 //			new QName(IJMTConstants.URI_JSF_CORE, IJSFConstants.TAG_SUBVIEW) };
 
 	/**
 	 * If not inside a view/subview, then f:view will be created. If
 	 * generateHForm is true, then will also generate h:form is no one exists.
 	 * 
 	 * @param position
 	 * @param uri
 	 * @param tagName
 	 * @param generateHForm
	 * @return the insertion dom position  based on any required containers
	 * that have been added.
 	 */
 	public static IDOMPosition prepareInsertJSFComponent(IDOMPosition position,
 			String uri, String tagName, boolean generateHForm) {
 		position = prepareView(position, uri, tagName);
 		if (position == null) {
 			return null;
 		}
 
 		if (generateHForm) {
 			return prepareForm(position);
 		}
         return position;
 	}
 
 	/**
 	 * @param position
 	 * @param uri
 	 * @param localname
	 * @return the new dom position
 	 */
 	public static IDOMPosition prepareInsertJSFComponent(IDOMPosition position,
 			String uri, String localname) {
 		return prepareInsertJSFComponent(position, uri, localname, false);
 	}
 
	/**
	 * @param position
	 * @return the new dom position if a form has been added or position
	 * otherwise
	 */
 	public static IDOMPosition prepareForm(IDOMPosition position) {
 		boolean hasform = ValidatorSupport.checkContainer(position, _qnameForm);
 		IDOMPosition newPosition = position;
 		if (!hasform) {
 			newPosition = ValidatorSupport
 					.insertContainer(position, _qnameForm);
 			if (newPosition == null) {
 				newPosition = position;
 			}
 		}
 		return newPosition;
 	}
 
 	/**
 	 * If there is no view created, create one; If view exists, the caret
 	 * movement rules will ensured the position is within view.
 	 * 
 	 * @param position
	 * @param uri 
	 * @param localname 
	 * @return the new dom position with the view added
 	 */
 	public static IDOMPosition prepareView(IDOMPosition position, String uri,
 			String localname) {
 		//Node view = null;
 		Document document = EditModelQuery.getDocumentNode(position
 				.getContainerNode());
 		if (JSFRootContainerPositionRule.getBasicContainer(document) == null) {
 			if (!(IJSFConstants.TAG_VIEW.equals(localname))) {
 				position = BodyHelper.insertBody(position, _qnameView, "f");
 			}
 		} else if (IJSFConstants.TAG_VIEW.equals(localname)) {
 			position = null;
 		}
 
 		return position;
 	}
 
 	/**
 	 * @param position
	 * @return the new dom position with the view added
 	 */
 	public static IDOMPosition prepareView(IDOMPosition position) {
 		return prepareView(position, null, null);
 	}
 
 }
