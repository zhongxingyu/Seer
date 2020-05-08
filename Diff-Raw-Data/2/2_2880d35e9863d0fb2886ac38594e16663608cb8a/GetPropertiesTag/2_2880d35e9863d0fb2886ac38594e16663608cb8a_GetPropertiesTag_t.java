 /*
  * Copyright 2012 - Six Dimensions
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package com.sixdimensions.wcm.cq.cqex.tags;
 
 import java.util.Map;
 
 import javax.servlet.jsp.JspException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.sling.api.SlingHttpServletRequest;
 import org.apache.sling.api.resource.Resource;
 import org.apache.sling.api.resource.ValueMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tldgen.annotations.Attribute;
 import org.tldgen.annotations.BodyContent;
 import org.tldgen.annotations.Tag;
 
 /**
  * Tag that retrieves the properties from the resource at a path and saves them
  * into a page context variable as a ValueMap.
  * 
  * @author dklco
  */
 @Tag(bodyContent = BodyContent.EMPTY, example = "&lt;cqex:getProperties "
 		+ "var=\"properties\" path=\"jcr:content/myNode\" resource=\"${resource}\" />")
 public class GetPropertiesTag extends AttributeSettingTag {
 	private static final Logger log = LoggerFactory
 			.getLogger(GetPropertiesTag.class);
 
 	private static final long serialVersionUID = 2906794811653608479L;
 
 	/**
 	 * The path of the resource to retrieve as a ValueMap. If a resource is
 	 * specified, this is treated as a relative path, if there is no resource,
 	 * it is treated as an absolute path.
 	 */
 	@Attribute
 	private String path;
 	/**
 	 * The resource to use as a base for retrieving the properties. If this and
 	 * path are specified the properties at the sub-resource specified by the
 	 * path will be retrieved.
 	 */
 	@Attribute
 	private transient Resource resource;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
 	 */
 	@Override
 	public int doEndTag() throws JspException {
 		log.trace("doEndTag");
 
 		Map<?, ?> properties = null;
 		final SlingHttpServletRequest request = (SlingHttpServletRequest) this.pageContext
 				.getRequest();
 
 		Resource rsrc = null;
 		if ((this.resource != null) && !StringUtils.isEmpty(this.path)) {
 			log.trace("Finding resource at relative path: " + this.path);
 			rsrc = request.getResourceResolver().getResource(
 					request.getResource(), this.path);
 		} else if ((this.resource != null) && StringUtils.isEmpty(this.path)) {
 			log.trace("Using resource: " + this.resource.getPath());
 			rsrc = this.resource;
		} else if ((this.path != null) && this.path.startsWith("/")) {
 			log.trace("Finding resource at absolute path: " + this.path);
 			rsrc = request.getResourceResolver().getResource(this.path);
 		} else {
 			log.warn("Unable to retrieve resource, neither path nor resource specified.");
 		}
 
 		if ((rsrc != null)
 				&& !rsrc.getResourceType().equals(
 						Resource.RESOURCE_TYPE_NON_EXISTING)) {
 			properties = rsrc.adaptTo(ValueMap.class);
 			this.setAttribute(this.getVar(), properties);
 		} else {
 			log.debug("Resource not found at path: " + this.path);
 		}
 
 		return javax.servlet.jsp.tagext.Tag.EVAL_PAGE;
 	}
 
 	/**
 	 * Gets the path of the resource to retrieve.
 	 * 
 	 * @return the path of the resource to retrieve
 	 */
 	public String getPath() {
 		return this.path;
 	}
 
 	/**
 	 * Gets the resource to retrieve the properties from.
 	 * 
 	 * @return the resource from which to retrieve the properties
 	 */
 	public Resource getResource() {
 		return this.resource;
 	}
 
 	/**
 	 * Set the path of the resource to retrieve.
 	 * 
 	 * @param path
 	 *            the path of the resource to retrieve
 	 */
 	public void setPath(final String path) {
 		this.path = path;
 	}
 
 	/**
 	 * Set the resource to retrieve the properties from.
 	 * 
 	 * @param resource
 	 *            the resource from which to retrieve the properties
 	 */
 	public void setResource(final Resource resource) {
 		this.resource = resource;
 	}
 }
