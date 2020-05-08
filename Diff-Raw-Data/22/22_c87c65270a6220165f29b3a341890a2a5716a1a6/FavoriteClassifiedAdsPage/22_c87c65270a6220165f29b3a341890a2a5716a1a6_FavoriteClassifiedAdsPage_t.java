 /*
  * FavoriteClassifiedAdsPage.java
  * Copyright (c) 2009, Monte Alto Research Center, All Rights Reserved.
  *
  * This software is the confidential and proprietary information of
  * Monte Alto Research Center ("Confidential Information"). You shall not
  * disclose such Confidential Information and shall use it only in
  * accordance with the terms of the license agreement you entered into
  * with Monte Alto Research Center
  */
 package com.marc.lastweek.web.pages.classifiedadslisting;
 
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.LoadableDetachableModel;
 
 import com.marc.lastweek.business.entities.classifiedad.ClassifiedAd;
 import com.marc.lastweek.web.application.LastweekApplication;
 import com.marc.lastweek.web.behaviours.JavaScriptAlertConfirmBehaviour;
 import com.marc.lastweek.web.components.selfpropaganda.CreateNewAdPropagandaPanel;
 import com.marc.lastweek.web.naming.PageParametersNaming;
 import com.marc.lastweek.web.pages.StandardPage;
 import com.marc.lastweek.web.pages.classifiedad.ClassifiedAdDetailPage;
 import com.marc.lastweek.web.session.LastweekSession;
 import com.marc.lastweek.web.util.ViewUtils;
 
 public class FavoriteClassifiedAdsPage extends StandardPage {
 
 	public FavoriteClassifiedAdsPage() {
 		super();
 		
 		this.add(new CreateNewAdPropagandaPanel("createNewAd"));
 
 		ListView classifiedList = new ListView("classifiedAdsList", 
 				new LoadableFavoritesModel()){
 
 			private static final long serialVersionUID = 4626162638077489818L;
 
 			@Override
 			protected void populateItem(ListItem item) {
 				ClassifiedAd classifiedAd = (ClassifiedAd)item.getModelObject();
 				final Long classifiedAdId = classifiedAd.getId();
 				
 				item.add(new Label("classifiedAdPublicationDate",ViewUtils.labelizer(classifiedAd.getPublicationDate())));
 				item.add(new Label("classifiedAdTitle",ViewUtils.labelizer(classifiedAd.getTitle())));
 				item.add(new Label("classifiedAdDescription",ViewUtils.labelizer(classifiedAd.getDescription())));
 				item.add(new Label("classifiedAdPrice",ViewUtils.labelizer(classifiedAd.getPrice())));
 				
 				PageParameters linkParameters = new PageParameters();
 				linkParameters.put(PageParametersNaming.PARAM_NAME_CLASSIFIED_AD_ID, 
 						classifiedAd.getId());
 				item.add(new BookmarkablePageLink("classifiedAdLink", ClassifiedAdDetailPage.class, linkParameters));
 				
 				Link deleteLink = new Link("deleteLink") {
 					private static final long serialVersionUID = -3978160233917347727L;
 
 					@Override
 					public void onClick() {
 						LastweekSession.get().removeFavorite(classifiedAdId);
 					}
 					
 				};
 				deleteLink.add(new JavaScriptAlertConfirmBehaviour("msg.delete"));
 				item.add(deleteLink);
 			}
 		};
 		this.add(classifiedList);
		
		// TODO: if list is empty, then add some kind of 'how to use favorites'
		
		// TODO: add send by email use case
 	}
 
 	public class LoadableFavoritesModel extends LoadableDetachableModel {
 
 		private static final long serialVersionUID = 2338695327167347518L;
 
 		@Override
 		protected Object load() {
 			List<Long> favoritesList = LastweekSession.get().getFavorites();
			if (favoritesList.size() > 0) {
 			Map<String,Object> parameters = new HashMap<String, Object>();
 	        parameters.put("favorites", favoritesList);
 			return LastweekApplication.get().getGeneralService().queryForList(ClassifiedAd.class,
 					"getClassifiedAdsInList", parameters);
			} 
			return new ArrayList<Long>();
 		}
 
 	}
 }
