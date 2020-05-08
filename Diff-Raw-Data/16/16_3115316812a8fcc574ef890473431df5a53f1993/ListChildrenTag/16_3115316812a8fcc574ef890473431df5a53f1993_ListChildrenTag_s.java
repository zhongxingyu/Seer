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
 
 import org.apache.sling.api.resource.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tldgen.annotations.Attribute;
 import org.tldgen.annotations.BodyContent;
 import org.tldgen.annotations.Tag;
 
 import com.day.cq.wcm.api.Page;
 import com.sixdimensions.wcm.cq.cqex.util.IterableIterator;
 
 /**
  * Tag for listing the children of a resource. The children are saved into the
  * page context as a variable with the name var and are saved as an
  * IterableIterator, which means they can be iterated through JSTL.
  * 
  * @author dklco
  */
 @Tag(bodyContent = BodyContent.EMPTY, example = "&lt;cqex:listChildren var=\"resources\" resource=\"${resource}\" />")
 public class ListChildrenTag extends AttributeSettingTag {
 	private static final Logger log = LoggerFactory
 			.getLogger(ListChildrenTag.class);
 	private static final long serialVersionUID = 5861756752614447760L;
 
 	/**
 	 * The page to list the children of, either this or the resource must be
 	 * specified.
 	 */
 	@Attribute
 	private transient Page page;
 	/**
 	 * The resource to list the children of, either this or the page must be
 	 * specified.
 	 */
 	@Attribute
 	private transient Resource resource;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
 	 */
 	@Override
 	public int doEndTag() {
 		log.trace("doEndTag");
 		IterableIterator<?> children = null;
 		if (this.resource != null) {
 			log.debug("Listing children of: " + this.resource.getPath());
 			children = new IterableIterator<Resource>(
 					this.resource.listChildren());
 		} else if (this.page != null) {
 			log.debug("Listing children of: " + this.page.getPath());
 			children = new IterableIterator<Page>(this.page.listChildren());
 		} else {
 			log.debug("No/null resource specified");
 		}
 		this.setAttribute(this.getVar(), children);
 		return javax.servlet.jsp.tagext.Tag.EVAL_PAGE;
 	}
 
 	/**
 	 * The page specified to list the children of.
 	 * 
 	 * @return the page or null
 	 */
 	public Page getPage() {
 		return this.page;
 	}
 
 	/**
 	 * Get the resource of which the children will be listed.
 	 * 
 	 * @return the resource or null
 	 */
 	public Resource getResource() {
 		return this.resource;
 	}
 
 	/**
 	 * Set the page to list the children of.
 	 * 
 	 * @param currentPage
 	 *            the page
 	 */
 	public void setPage(final Page currentPage) {
 		this.page = currentPage;
 	}
 
 	/**
 	 * Set the resource to list the children of.
 	 * 
	 * @param currentResource
 	 *            the resource
 	 */
	public void setResource(final Resource currentResource) {
		this.resource = currentResource;
 	}
 }
