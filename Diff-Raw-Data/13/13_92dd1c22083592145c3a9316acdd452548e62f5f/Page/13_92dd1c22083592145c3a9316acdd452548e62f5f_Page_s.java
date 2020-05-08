 /**
  *
  * Shire - Blog aware static site generator 
  * Copyright (c) 2012, Sandeep Gupta
  * 
  * http://www.sangupta/projects/shire
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.shire.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.sangupta.shire.util.ShireUtils;
 
 /**
  * Holds data model for a given page - the page which is under
  * processing by the layout.
  * 
  * @author sangupta
  *
  */
 public class Page {
 	
 	/**
 	 * The unrendered content of the page
 	 */
 	private String content;
 	
 	/**
 	 * The title of the page
 	 */
 	private String title;
 	
 	/**
 	 * The URL of the page without the domain
 	 */
 	private String url;
 	
 	/**
 	 * The date assigned to this post
 	 */
 	private Date date;
 	
 	/**
 	 * Unique pageID assigned to this post
 	 */
 	private String id;
 	
 	/**
 	 * Indicates if publishing of this page is to be put on hold.
 	 */
 	private boolean published = true;
 	
 	/**
 	 * The categories the page in
 	 */
 	private final List<String> categories = new ArrayList<String>();
 	
 	/**
 	 * The tags this page belongs to
 	 */
 	private final List<String> tags = new ArrayList<String>();
 	
 	/**
 	 * @param pageFrontMatter
 	 */
 	public void mergeFrontMatter(Properties pageFrontMatter) {
 		// read the title from the front matter
 		this.setTitle(pageFrontMatter.getProperty("title"));
 
 		// read the date from the front matter
 		String date = pageFrontMatter.getProperty("date");
 		if(date != null) {
 			this.setDate(ShireUtils.parsePostDate(date));
 		}
 		
 		// read the tags from the front matter
 		String tags = pageFrontMatter.getProperty("tags");
 		if(tags != null && !tags.trim().isEmpty()) {
 			String[] tokens = StringUtils.split(tags, " ;,");
 			for(String tag : tokens) {
 				if(!("".equals(tag.trim()))) {
 					this.tags.add(tag);
 				}
 			}
 		}
 		
 		// read the categories from the fron matter
 		String categories = pageFrontMatter.getProperty("categories");
 		if(categories != null && !categories.trim().isEmpty()) {
 			String[] tokens = StringUtils.split(categories, " ;,");
 			for(String category : tokens) {
 				if(!("".equals(category.trim()))) {
 					this.categories.add(category);
 				}
 			}
 		}
 		
 		// check for published flags
 		String published = pageFrontMatter.getProperty("published");
 		if("false".equalsIgnoreCase(published)) {
 			this.published = false;
 		}
 	}
 
 	/**
 	 * All properties of the page have been set and are
 	 * ready for post-processing.
 	 */
 	public void postProcessProperties() {
 		this.id = ShireUtils.createUniquePageID(this.url);
 		updateCategoriesFromUrl();
 	}
 
 	/**
 	 * Update the categories as extracted from the url.
 	 */
 	private void updateCategoriesFromUrl() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	// Usual accessors follow
 
 	/**
 	 * @return the url
 	 */
 	public String getUrl() {
 		return url;
 	}
 
 	/**
 	 * @param url the url to set
 	 */
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the content
 	 */
 	public String getContent() {
 		return content;
 	}
 
 	/**
 	 * @param content the content to set
 	 */
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	/**
 	 * @return the date
 	 */
 	public Date getDate() {
 		return date;
 	}
 
 	/**
 	 * @param date the date to set
 	 */
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public String getId() {
 		return id;
 	}
 
 	/**
 	 * @param id the id to set
 	 */
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	/**
 	 * @return the categories
 	 */
 	public List<String> getCategories() {
		return categories;
 	}
 
 	/**
 	 * @return the tags
 	 */
 	public List<String> getTags() {
		return tags;
 	}
 
 	/**
 	 * @return the published
 	 */
 	public boolean isPublished() {
 		return published;
 	}
 
 	/**
 	 * @param published the published to set
 	 */
 	public void setPublished(boolean published) {
 		this.published = published;
 	}
 
 }
