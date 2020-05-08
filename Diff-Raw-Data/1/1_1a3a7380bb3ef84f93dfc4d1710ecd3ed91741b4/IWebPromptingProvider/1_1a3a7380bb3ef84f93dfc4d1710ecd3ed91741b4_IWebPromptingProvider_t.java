 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.project.list;
 
 import java.util.*;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.project.IPromptingProvider;
 
 public interface IWebPromptingProvider extends IPromptingProvider {
 	static String ERROR = "error"; //$NON-NLS-1$
 	static List<Object> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<Object>());
 
 	static String JSF_BUNDLES = "jsf.bundles"; //$NON-NLS-1$
 	static String JSF_REGISTERED_BUNDLES = "jsf.registered.bundles"; //$NON-NLS-1$
 	static String JSF_BUNDLE_PROPERTIES = "jsf.bundle.properties"; //$NON-NLS-1$
 	static String JSF_MANAGED_BEANS = "jsf.beans"; //$NON-NLS-1$
 	static String JSF_BEAN_PROPERTIES = "jsf.bean.properties"; //$NON-NLS-1$
 	static String JSF_BEAN_METHODS = "jsf.bean.methods"; //$NON-NLS-1$
 	static String JSF_BEAN_ADD_PROPERTY = "jsf.bean.add.property"; //$NON-NLS-1$
 	static String JSF_VIEW_ACTIONS = "jsf.view.action"; //$NON-NLS-1$
 	static String JSF_BEAN_OPEN = "jsf.bean.open"; //$NON-NLS-1$
 	static String JSF_GET_PATH = "jsf.get.path"; //$NON-NLS-1$
 	static String JSF_OPEN_ACTION = "jsf.open.action"; //$NON-NLS-1$
 	static String JSF_OPEN_CONVERTOR = "jsf.open.convertor"; //$NON-NLS-1$
 	static String JSF_OPEN_RENDER_KIT = "jsf.open.render-kit"; //$NON-NLS-1$
 	static String JSF_OPEN_VALIDATOR = "jsf.open.validator"; //$NON-NLS-1$
 	static String JSF_OPEN_CLASS_PROPERTY = "jsf.open.property"; //$NON-NLS-1$
 	static String JSF_OPEN_TAG_LIBRARY = "jsf.open.taglibrary"; //$NON-NLS-1$
 	static String JSF_OPEN_KEY = "jsf.open.key"; //$NON-NLS-1$
 	static String JSF_OPEN_BUNDLE = "jsf.open.bundle"; //$NON-NLS-1$
 	static String JSF_GET_URL = "jsf.get.url"; //$NON-NLS-1$
 	static String JSF_GET_TAGLIBS = "jsf.get.taglibs"; //$NON-NLS-1$
 	static String JSF_CONVERTER_IDS = "jsf.converter.ids"; //$NON-NLS-1$
 	static String JSF_VALIDATOR_IDS = "jsf.validator.ids"; //$NON-NLS-1$
	static String JSF_FACES_CONFIG = "jsf.faces.config"; //$NON-NLS-1$
 
 	static String JSF_CONVERT_URL_TO_PATH = "jsf.url.to.path"; //$NON-NLS-1$
 
 	static String STRUTS_OPEN_PARAMETER = "struts.open.parameter"; //$NON-NLS-1$
 	static String STRUTS_OPEN_BUNDLE = "struts.open.bundle"; //$NON-NLS-1$
 	static String STRUTS_OPEN_KEY = "struts.open.key"; //$NON-NLS-1$
 	static String STRUTS_OPEN_LINK_FORWARD = "struts.open.link.forward"; //$NON-NLS-1$
 	static String STRUTS_OPEN_LINK_PAGE = "struts.open.link.page"; //$NON-NLS-1$
 	static String STRUTS_OPEN_LINK_ACTION = "struts.open.link.action"; //$NON-NLS-1$
 	static String STRUTS_OPEN_PROPERTY = "struts.open.property"; //$NON-NLS-1$
 	static String STRUTS_OPEN_ACTION_MAPPING = "struts.open.action.mapping"; //$NON-NLS-1$
 	static String STRUTS_OPEN_FORM_BEAN = "struts.open.form.bean"; //$NON-NLS-1$
 	static String STRUTS_OPEN_FORWARD_PATH = "struts.open.forward.path"; //$NON-NLS-1$
 	static String STRUTS_OPEN_OBJECT_BY_PATH = "struts.open.object.by.path"; //$NON-NLS-1$
 	static String STRUTS_OPEN_FILE_IN_WEB_ROOT = "struts.open.file.in.web.root"; //$NON-NLS-1$
 	static String STRUTS_OPEN_VALIDATOR = "struts.open.validator"; //$NON-NLS-1$
 	static String STRUTS_OPEN_TAG_LIBRARY = "struts.open.taglibrary"; //$NON-NLS-1$
 	static String STRUTS_OPEN_METHOD = "struts.open.method"; //$NON-NLS-1$
 
 	static String PROPERTY_TYPE = "propertyType"; //$NON-NLS-1$
 	static String PROPERTY_BEAN_ONLY = "bean-only"; //$NON-NLS-1$
 	static String PARAMETER_TYPES = "parameterTypes";  // String[] //$NON-NLS-1$
 	static String RETURN_TYPE = "returnType"; //$NON-NLS-1$
 	static String VIEW_PATH = "viewPath"; //$NON-NLS-1$
 	static String FILE = "file"; //$NON-NLS-1$
 	static String BUNDLE = "bundle"; //$NON-NLS-1$
 	static String KEY = "key"; //$NON-NLS-1$
 	static String MODULE = "module"; //$NON-NLS-1$
 	static String ACTION = "action"; //$NON-NLS-1$
 	static String TYPE = "type"; //$NON-NLS-1$
 	static String PROPERTY = "property"; //$NON-NLS-1$
 	static String MODEL_OBJECT_PATH = "model-path"; //$NON-NLS-1$
 	static String NAME = "name"; //$NON-NLS-1$
 	static String ATTRIBUTE = "attribute"; //$NON-NLS-1$
 	static String LOCALE = "locale"; //$NON-NLS-1$
 	
 	public boolean isSupporting(String id);
 	public List<Object> getList(XModel model, String id, String prefix, Properties properties);
 }
