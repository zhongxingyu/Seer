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
 
 import javax.servlet.jsp.JspException;
 
 import org.apache.sling.api.resource.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tldgen.annotations.Attribute;
 import org.tldgen.annotations.BodyContent;
 import org.tldgen.annotations.Tag;
 
 import com.day.cq.dam.api.Asset;
 import com.day.cq.dam.commons.util.DamUtil;
 
 /**
  * Retrieves an asset from the resource. Checks whether the given resource is an
  * asset, and if not, travels upwards the resource hierarchy until a resource is
  * an asset.
  * 
  * @author dklco
  */
 @Tag(bodyContent = BodyContent.EMPTY, example = "&lt;cqex:getAsset "
 		+ "var=\"asset\" resource=\"${resource}\" />")
 public class GetAssetTag extends AttributeSettingTag {
 	private static final long serialVersionUID = -4882565776256570621L;
 	private static final Logger log = LoggerFactory
 			.getLogger(GetAssetTag.class);
 
 	/**
 	 * The resource to use to retrieve the asset.
 	 */
 	@Attribute(required = true)
 	private Resource resource;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
 	 */
 	@Override
 	public int doEndTag() throws JspException {
 		log.trace("doEndTag");
 
		log.debug("Retrieving asset from resource: " + this.resource.getPath());
		final Asset asset = DamUtil.resolveToAsset(this.resource);
		if (asset != null) {
			log.debug("Retrieved asset: " + asset.getPath());
		} else {
			log.warn("No asset retrieved from resource: "
 					+ this.resource.getPath());
		}
 
		this.setAttribute(this.getVar(), asset);
 
 		return javax.servlet.jsp.tagext.Tag.EVAL_PAGE;
 	}
 
 	/**
 	 * Get the resource from which the asset is to be retrieve.
 	 * 
 	 * @return the resource
 	 */
 	public Resource getResource() {
 		return this.resource;
 	}
 
 	/**
 	 * Set the resource from which to retrieve the asset.
 	 * 
 	 * @param resource
 	 *            the resource from which to retrieve the asset
 	 */
 	public void setResource(final Resource resource) {
 		this.resource = resource;
 	}
 
 }
