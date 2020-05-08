 /*
  * LoadableCategoriesListModel.java
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.web.models;
 
 import loc.marc.commons.business.services.general.GeneralService;
 
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.marc.lastweek.business.entities.category.Subcategory;
 import com.marc.lastweek.web.application.LastweekApplication;
 
 public class LoadableSubcategoriesListModel extends LoadableDetachableModel {
 
 	private static final long serialVersionUID = 2338695327167347518L;
 
 	@SpringBean
 	GeneralService generalService;
 	
 	@Override
 	protected Object load() {
		return LastweekApplication.get().getGeneralService().findAll(Subcategory.class);
 	}
 
 }
