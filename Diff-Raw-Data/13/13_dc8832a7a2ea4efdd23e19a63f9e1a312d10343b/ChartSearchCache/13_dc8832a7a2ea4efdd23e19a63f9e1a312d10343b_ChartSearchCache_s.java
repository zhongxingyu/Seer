 /**
  * This Source Code Form is subject to the terms of the Mozilla Public License,
  * v. 2.0. If a copy of the MPL was not distributed with this file, You can
  * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
  * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
  *
  * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
  * graphic logo is a trademark of OpenMRS Inc.
  */
 package org.openmrs.module.chartsearch;
 
 import java.util.Date;
 import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.chartsearch.api.ChartSearchService;
 
 import com.openmrs.module.chartsearch.saving.ChartSearchBookmark;
 import com.openmrs.module.chartsearch.saving.ChartSearchHistory;
 
 /**
  * This basically provides access to chart-search module stored records such as notes on searches,
  * bookmarked searches, search history, search suggestions among others from the database emulating
  * the normal cache/buffer
  */
 public class ChartSearchCache {
 	
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer uniqueNumber;
 	
 	private static final Logger logger = Logger.getLogger(ChartSearchCache.class);
 	
 	private ChartSearchService chartSearchService = getComponent(ChartSearchService.class);
 	
 	public void saveOrUpdateSearchHistory(String searchText, Integer patientId) {
 		ChartSearchHistory history = new ChartSearchHistory();
 		List<ChartSearchHistory> allHistory = chartSearchService.getAllSearchHistory();
 		ChartSearchHistory exisitingHistory = checkIfSearchIsAlreadyInHistory(allHistory, searchText);
 		
 		if (StringUtils.isNotBlank(searchText) && null != patientId) {
 			history.setLastSearchedAt(new Date());//Date was duplicated, probably use Calendar
 			
 			if (null != exisitingHistory) {//change only the date to current and generate new searchId
 				history.setHistoryOwner(exisitingHistory.getHistoryOwner());
 				history.setPatient(exisitingHistory.getPatient());
 				history.setSearchPhrase(exisitingHistory.getSearchPhrase());
 				history.setUuid(exisitingHistory.getUuid());
 				
 				chartSearchService.deleteSearchHistory(exisitingHistory);
 			} else {
 				history.setHistoryOwner(Context.getAuthenticatedUser());
 				history.setPatient(Context.getPatientService().getPatient(patientId));
 				history.setSearchPhrase(searchText);
 			}
 			chartSearchService.saveSearchHistory(history);
 		}
 	}
 	
 	private ChartSearchHistory checkIfSearchIsAlreadyInHistory(List<ChartSearchHistory> allHistory, String searchText) {
 		if (!allHistory.isEmpty()) {
 			for (int i = 0; i < allHistory.size(); i++) {
 				ChartSearchHistory history = allHistory.get(i);
 				if (searchText.equals(history.getSearchPhrase())) {
 					return history;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Deletes a search history whose uuid is provided
 	 * 
 	 * @see ChartSearchService#deleteSearchHistory(ChartSearchHistory)
 	 * @param historyUuid
 	 * @return deleted, whether the history was or not deleted
 	 */
 	public boolean deleteSearchHistory(String historyUuid) {
 		ChartSearchHistory history = chartSearchService.getSearchHistoryByUuid(historyUuid);
 		
 		if (StringUtils.isNotBlank(historyUuid)) {
 			chartSearchService.deleteSearchHistory(history);
 			return true;
 		} else
 			return false;
 	}
 	
 	public boolean checkIfPhraseExisitsInHistory(String searchPhrase) {
 		List<ChartSearchHistory> allHistory = chartSearchService.getAllSearchHistory();
 		boolean exists = false;
 		
 		for (ChartSearchHistory history : allHistory) {
 			if (history.getSearchPhrase().equals(searchPhrase)) {
 				exists = true;
 			}
 		}
 		
 		return exists;
 	}
 	
 	public JSONObject saveOrUpdateBookmark(String selectedCategories, String searchPhrase, String bookmarkName,
 	                                       Integer patientId) {
 		JSONObject json = new JSONObject();
 		
 		if (StringUtils.isNotBlank(searchPhrase) && StringUtils.isNotBlank(bookmarkName) && null != patientId) {
 			ChartSearchBookmark bookmark = new ChartSearchBookmark();
 			ChartSearchBookmark existingBookmark = checkIfBookmarkExistsForPhrase(searchPhrase, selectedCategories);
 			
 			if (null != existingBookmark) {
 				existingBookmark.setBookmarkName(bookmarkName);
 				chartSearchService.saveSearchBookmark(existingBookmark);
 				
 				json.put("currentUuid", chartSearchService.getSearchBookmarkByUuid(existingBookmark.getUuid()));
 			} else {
 				if (bookmarkNameExists(bookmarkName)) {
					bookmarkName += uniqueNumber;
 				}
 				bookmark.setBookmarkName(bookmarkName);
 				bookmark.setSearchPhrase(searchPhrase);
 				bookmark.setPatient(Context.getPatientService().getPatient(patientId));
 				bookmark.setSelectedCategories(selectedCategories);
 				bookmark.setBookmarkOwner(Context.getAuthenticatedUser());
 				chartSearchService.saveSearchBookmark(bookmark);
 				
 				json.put("currentUuid", chartSearchService.getSearchBookmarkByUuid(bookmark.getUuid()).getUuid());
 			}
 			json.put("allBookmarks", GeneratingJson.getAllSearchBookmarksToReturnToUI(false));
 			
 			return json;
 		} else {
 			return null;
 		}
 	}
 	
 	private boolean bookmarkNameExists(String bookmarkName) {
 		List<ChartSearchBookmark> existingBookmarks = chartSearchService.getAllSearchBookmarks();
 		
 		if (!existingBookmarks.isEmpty()) {
 			for (ChartSearchBookmark bookmark : existingBookmarks) {
 				if (bookmark.getBookmarkName().equals(bookmarkName)) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public ChartSearchBookmark checkIfBookmarkExistsForPhrase(String phrase, String categories) {
 		List<ChartSearchBookmark> existingBookmarks = chartSearchService.getAllSearchBookmarks();
 		
 		if (!existingBookmarks.isEmpty()) {
 			for (ChartSearchBookmark bookmark : existingBookmarks) {
 				if (bookmark.getSearchPhrase().equals(phrase) && bookmark.getSelectedCategories().equals(categories)) {
 					return bookmark;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public JSONArray deleteSearchBookmark(String uuid) {
 		ChartSearchBookmark bookmark = chartSearchService.getSearchBookmarkByUuid(uuid);
 		
 		if (StringUtils.isNotBlank(uuid) && null != bookmark) {
 			chartSearchService.deleteSearchBookmark(bookmark);
 			return GeneratingJson.getAllSearchBookmarksToReturnToUI(false);
 		} else
 			return null;
 	}
 	
 	public JSONObject getSearchBookmarkSearchDetailsByUuid(String uuid) {
 		if (StringUtils.isNotBlank(uuid)) {
 			ChartSearchBookmark bookmark = chartSearchService.getSearchBookmarkByUuid(uuid);
 			JSONObject json = new JSONObject();
 			String[] categories = bookmark.getSelectedCategories().split(", ");
 			
 			json.put("searchPhrase", bookmark.getSearchPhrase());
 			json.put("bookmarkName", bookmark.getBookmarkName());
 			json.put("commaCategories", bookmark.getSelectedCategories());
 			json.put("categories", categories);
 			
 			return json;
 		} else
 			return null;
 	}
 	
 	private <T> T getComponent(Class<T> clazz) {
 		List<T> list = Context.getRegisteredComponents(clazz);
 		if (list == null || list.size() == 0)
 			throw new RuntimeException("Cannot find component of " + clazz);
 		return list.get(0);
 	}
 }
