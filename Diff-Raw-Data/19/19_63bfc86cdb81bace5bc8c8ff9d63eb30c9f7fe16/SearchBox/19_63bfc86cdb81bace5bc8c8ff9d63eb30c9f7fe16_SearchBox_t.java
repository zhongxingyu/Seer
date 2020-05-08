 /*
  * SearchBox.java
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.web.components;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 
 import com.marc.lastweek.web.naming.PageParametersNaming;
 import com.marc.lastweek.web.pages.classifiedadslisting.FilterResultsPage;
 
 public class SearchBox  extends Panel {
 	private static final long serialVersionUID = 6833647109908845147L;
 
 	public SearchBox(String id, PageParameters parameters) {
 		super(id);
 		this.add(new SearchForm("searchForm", parameters));
 	}
 	
 	protected class SearchForm extends Form {
 		private static final long serialVersionUID = -3849601177703765496L;
 		
 		private PageParameters parameters;
 		protected TextField searchInput;
 		
 		public SearchForm(String id, PageParameters parameters) {
 			super(id);
 
			if (parameters!=null) {
				this.parameters = parameters;
			} else {
				this.parameters = new PageParameters();
			}
 			this.searchInput = new TextField("searchInput", new Model());
 			this.add(this.searchInput);
 		}
 		
 		@Override
 		public void onSubmit() {
 			String searchTerm = (String) this.searchInput.getModelObject();
 			if (searchTerm!=null) {
 				this.parameters.put(PageParametersNaming.PARAM_NAME_SEARCH_TERM, 
 						StringEscapeUtils.escapeHtml(searchTerm));
 				setResponsePage(FilterResultsPage.class, this.parameters);
 			}
 		}
 	}
 }
