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
 
import java.util.HashMap;
import java.util.Map;

 import loc.marc.commons.business.services.general.GeneralService;
 
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.marc.lastweek.business.entities.category.Subcategory;
 import com.marc.lastweek.web.application.LastweekApplication;
 
 public class LoadableSubcategoriesListModel extends LoadableDetachableModel {
 
 	private static final long serialVersionUID = 2338695327167347518L;
 
 	@SpringBean
 	GeneralService generalService;
 	
	private Long id;
	
	public LoadableSubcategoriesListModel(Long id) {
		this.id = id;
	}
	
 	@Override
 	protected Object load() {
		Map<String,Object> parameters = new HashMap<String, Object>();
        parameters.put("categoryId", this.id);

		return LastweekApplication.get().getGeneralService().queryForList(Subcategory.class, "findSubcategoriesByCategoryId", parameters);
 	}
 
 }
