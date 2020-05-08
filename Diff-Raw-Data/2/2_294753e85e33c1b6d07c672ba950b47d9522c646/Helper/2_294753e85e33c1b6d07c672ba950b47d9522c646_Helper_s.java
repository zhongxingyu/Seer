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
 package eu.trentorise.smartcampus.domain.discovertrento;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import eu.trentorise.smartcampus.domain.semantic.Tag;
 
 
 public class Helper {
 
     public static String[] merge(String[] ids, String id) {
     	if (ids == null) return new String[]{id};
     	String[] na = new String[ids.length+1];
     	System.arraycopy(ids, 0, na, 0, ids.length);
     	na[ids.length] = id;
     	return na;
     }
 
     public static String[] addUser(String[] inArray, String user) {
     	String[] array = inArray == null ? new String[0] : inArray;
 
     	List<String> list = new ArrayList<String>();
     	if (inArray != null) list.addAll(Arrays.asList(inArray));
     	if (!list.contains(user)) {
     		list.add(user);
     		return list.toArray(new String[list.size()]);
     	}
     	return array;
     }
     public static String[] removeUser(String[] array, String user) {
     	if (array == null) return new String[0];
     	List<String> list = new ArrayList<String>(Arrays.asList(array));
     	if (list.contains(user)) {
     		list.remove(user);
     		return list.toArray(new String[list.size()]);
     	}
     	return array;
     }
     
     public static RatingData[] addRating(RatingData[] inArray, String user, int rating) {
     	List<RatingData> list = new ArrayList<RatingData>();
     	if (inArray != null) list.addAll(Arrays.asList(inArray));
     	for (int i = 0; i < list.size(); i++) {
     		if (list.get(i).getUser().equals(user)) {
     			list.get(i).setValue(rating);
     			return list.toArray(new RatingData[list.size()]);
     		}
     	}
     	RatingData data = new RatingData();
     	data.setUser(user);
     	data.setValue(rating);
     	list.add(data);
     	return list.toArray(new RatingData[list.size()]);
     }
 
     public static int averageRating(RatingData[] inArray) {
     	if (inArray == null || inArray.length == 0) return 10;
     	int sum = 0;
     	for (RatingData rd : inArray) {
     		sum += rd.getValue();
     	}
     	return (int)(sum / inArray.length); 
     }
     
     
     public static Long[] toSingleton(Long l) {
     	return new Long[]{l}; 
     }
     
     public static CommunityData mergeCommunityData(CommunityData communityData, CommunityData newCommunityData) {
     	if (communityData == null) return newCommunityData;
 
     	if (newCommunityData.getRatings() != null && newCommunityData.getRatings().length > 0) communityData.setRatings(newCommunityData.getRatings());
     	if (newCommunityData.getTags() != null) communityData.setTags(newCommunityData.getTags());
     	if (newCommunityData.getNotes() != null) communityData.setNotes(newCommunityData.getNotes());
     	if (communityData.getRatings() != null) communityData.setAverageRating(averageRating(communityData.getRatings()));
     	
     	return communityData;
     }
     
     public static CommunityData addRating(CommunityData data, String user, int rating) {
     	CommunityData res = data;
     	if (data == null) {
     		res = new CommunityData();
     	}
     	res.setRatings(addRating(res.getRatings(), user, rating));
     	res.setAverageRating(averageRating(res.getRatings()));
     	return res;
     }
 //    public static EventCustomData mergeEventCustomData(EventCustomData customData, GenericEvent data) {
 //    	EventCustomData cd = customData == null ? new EventCustomData() : customData;
 //		if (data.getType() != null) cd.setCategory(data.getType());
 //    	if (data.getFromTime() != null && data.getFromTime() > 0 ) cd.setCustomFromTime(data.getFromTime());
 //    	if (data.getToTime() != null && data.getToTime() != null) cd.setCustomToTime(data.getToTime());
 //    	if (data.getPoiId() != null) cd.setCustomPoiId(data.getPoiId());
 //    	return cd;
 //    }
 //    public static POICustomData mergePOICustomData(POICustomData customData, GenericPOI data) {
 //    	POICustomData cd = customData == null ? new POICustomData() : customData;
 //		if (data.getType() != null) cd.setCategory(data.getType());
 //    	return cd;
 //    }
     
     public static boolean requiresUpdateOnCommunityData(CommunityData newData, CommunityData data) {
     	Set<Tag> set1 = newData == null || newData.getTags() == null ? 
     			new HashSet<Tag>() : 
     			new HashSet<Tag>(Arrays.asList(newData.getTags())); 
     	Set<Tag> set2 = data == null || data.getTags() == null ? 
     			new HashSet<Tag>() : 
     			new HashSet<Tag>(Arrays.asList(data.getTags())); 
     		
    	return set1.equals(set2);		
     }    
     public static boolean requiresUpdateOnEventCustomData(EventCustomData newData, EventCustomData data) {
     	if (newData != null && data == null ||
     		newData.getFromTime() != null && !newData.getFromTime().equals(data.getFromTime()) ||
     	    newData.getTiming() != null && !newData.getTiming().equals(data.getTiming()) ||
     		newData.getPoiId() != null && !newData.getPoiId().equals(data.getPoiId())) 
     	{
     		return true;
     	}
     	return false;
     }
     public static boolean requiresUpdateOnGenericEvent(GenericEvent newData, GenericEvent data) {
     	if (newData == null && data != null) {
     		return true;
     	} 
     	else if (newData != null && data != null) {
     		if (newData.getFromTime() != null && !newData.getFromTime().equals(data.getFromTime()) ||
     			newData.getTiming() != null && !newData.getTiming().equals(data.getTiming()) ||	
     			newData.getPoiId() != null && !newData.getPoiId().equals(data.getPoiId())) return true;
     	}
     	return false;
     }
     public static boolean requiresUpdateOnGenericPOI(GenericPOI newData, GenericPOI data) {
     	if (newData == null && data != null) {
     		return true;
     	} 
     	else if (newData != null && data != null) {
     		if (newData.getPoiData() != null && !newData.getPoiData().equals(data.getPoiData())) return true;
     	}
     	return false;
     }
     public static boolean requiresUpdateOnGenericStory(GenericStory newData, GenericStory data) {
     	if (newData == null && data != null) {
     		return true;
     	} 
     	else if (newData != null && data != null) {
     		GenericStoryStep[] arr = data.getSteps() == null ? new GenericStoryStep[0] : data.getSteps();
     		GenericStoryStep[] nArr = newData.getSteps() == null ? new GenericStoryStep[0] : newData.getSteps();
     		if (arr.length != nArr.length) return true;
     		for (GenericStoryStep step : arr) {
     			boolean found = false;
     			for (GenericStoryStep nStep: nArr) {
     				if (step.getPoiId().equals(nStep.getPoiId())) {
     					found = true;
     					break;
     				}
     			}
     			if (!found) return true;
     		}
     	}
     	return false;
     }
     
     public static Long[] mergeLong(Long[] ids, Long id) {
     	if (ids == null) return new Long[]{id};
     	Long[] na = new Long[ids.length+1];
     	System.arraycopy(ids, 0, na, 0, ids.length);
     	na[ids.length] = id;
     	return na;
     }
     
 	public static void main(String[] args) {
     	String[] attending = null;
     	attending = addUser(attending, "xx");
     	System.err.println(Arrays.toString(attending));
     	attending = removeUser(attending, "xx");
     	System.err.println(Arrays.toString(attending));
 	}
     
 }
