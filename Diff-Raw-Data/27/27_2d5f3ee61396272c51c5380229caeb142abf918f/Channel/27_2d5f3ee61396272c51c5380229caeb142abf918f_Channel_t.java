 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.communicator.model;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import eu.trentorise.smartcampus.presentation.data.BasicObject;
 
 public class Channel extends BasicObject {
 	private static final String FD_KEYWORDS = "keywords";
 
 	private static final long serialVersionUID = -8544225509851840357L;
 
 	private String title;
 	private Long userId;
 	private List<String> labelIds;
 	private List<Action> actions;
 
 	private Map<String,Object> filterData;
 
 	private String sourceType;
 
 	private boolean isFeed;
 	
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public Long getUserId() {
 		return userId;
 	}
 	public void setUserId(Long userId) {
 		this.userId = userId;
 	}
 	public Map<String, Object> getFilterData() {
 		return filterData;
 	}
 	public void setFilterData(Map<String, Object> filterData) {
 		this.filterData = filterData;
 	}
 	public List<String> getLabelIds() {
 		return labelIds;
 	}
 	public void setLabelIds(List<String> labelIds) {
 		this.labelIds = labelIds;
 	}
 	public List<Action> getActions() {
 		return actions;
 	}
 	public void setActions(List<Action> actions) {
 		this.actions = actions;
 	}
 	public String getSourceType() {
 		return sourceType;
 	}
 	public void setSourceType(String sourceType) {
 		this.sourceType = sourceType;
 	}
 	public boolean isFeed() {
 		return isFeed;
 	}
 	public void setFeed(boolean isFeed) {
 		this.isFeed = isFeed;
 	}
 	public Map<String,Action> asActionMap() {
 		Map<String,Action> actionMap = new HashMap<String, Action>();
 		if (actions != null && actions.size() > 0) {
 			for (Action a : actions) {
 				actionMap.put(a.getLabel(), a);
 			}
 		}
 		return actionMap;
 	}
 	
 	public boolean applies(Notification notification) {
 		// source type should match
 		if (sourceType != null && ! sourceType.equals(notification.getType())) return false;
 		// keywords should match
 		String text = (notification.getTitle()+ " "+notification.getDescription()).toLowerCase();
 		
 		String keywords = (filterData == null || filterData.get(FD_KEYWORDS) == null) ? null : (String)filterData.get(FD_KEYWORDS);
 		if (keywords != null) {
 			String[] keywordArr = keywords.split(",");
 			for (String keyword : keywordArr) {
 				String k = keyword.trim().toLowerCase();
 				if (text.matches(".*[\\p{Punct}\\p{Blank}]" + k + "[\\p{Punct}\\p{Blank}].*")) {
 					return true;
 				}
 			}
			return false;
 		}
 		
 //		List<String> keywords = (filterData == null || filterData.get(FD_KEYWORDS) == null) ? Collections.<String>emptyList() : (List<String>)filterData.get(FD_KEYWORDS);
 //		
 //		if (keywords.size() > 0) {
 //			for (String keyword : keywords) {
 //				String k = keyword.toLowerCase();
 //				if (text.matches(".*[\\p{Punct}\\p{Blank}]" + k + "[\\p{Punct}\\p{Blank}].*")) {
 //					return true;
 //				}
 //			}
 //			return false;
 //		}
 		return true;
 	}
 
 }
