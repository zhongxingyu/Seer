 package com.thx.bizcat.http.apiproxy.JSONParser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.thx.bizcat.http.apiproxy.JSONParser.Result.sp_GetMyUpdatedBiddings_Result;
 import com.thx.bizcat.http.apiproxy.JSONParser.Result.sp_GetMyUpdatedMatch_Result;
 import com.thx.bizcat.http.apiproxy.JSONParser.Result.sp_GetMyUpdatedProvides_Result;
 import com.thx.bizcat.http.apiproxy.JSONParser.Result.sp_GetMyUpdatedRequests_Result;
 import com.thx.bizcat.http.apiproxy.JSONParser.Result.sp_GetNewMessage_Result;
 import com.thx.bizcat.util.Logger;
 
 public class sp_UserGetUpdatedContents_Items {
 	public List<sp_GetMyUpdatedRequests_Result> newrequests;
 	public List<sp_GetMyUpdatedProvides_Result> newprovides;
 	public List<sp_GetMyUpdatedBiddings_Result> newbiddings;
 	public List<sp_GetNewMessage_Result> newbiddingmessages;
 	public List<sp_GetMyUpdatedMatch_Result> newmatches;
 	
 	public String  last_modified_time_request;
 	public String  last_modified_time_provide;
 	public String  last_modified_time_bidding;
 	public String  last_modified_time_biddingmessage;
 	public String  last_modified_time_match;
 	
 	
 	public boolean bsuccess = false;
 	public ResultCode resultCode = ResultCode.NONE;
 	public sp_UserGetUpdatedContents_Items(JSONObject obj) {
 		try {
 			bsuccess = obj.getBoolean("success");
 			
 			if(bsuccess)
 			{
				JSONObject requests = obj.getJSONObject("newrequests");
 				if(requests != null)//
 				{
 					newrequests = new ArrayList<sp_GetMyUpdatedRequests_Result>();
 					JSONArray array = requests.getJSONArray("requests");
 					for(int i=0;i<array.length();i++)
 					{
 						newrequests.add(new sp_GetMyUpdatedRequests_Result(array.getJSONObject(i)));
 					}
 					
 					last_modified_time_request = requests.getString("latest_modified_time");
 				}
 				
				JSONObject provides = obj.getJSONObject("newprovides");
 				if(provides != null)//
 				{
 					newprovides = new ArrayList<sp_GetMyUpdatedProvides_Result>();
 					JSONArray array = provides.getJSONArray("provides");
 					for(int i=0;i<array.length();i++)
 					{
 						newprovides.add(new sp_GetMyUpdatedProvides_Result(array.getJSONObject(i)));
 					}
 					
 					last_modified_time_provide = provides.getString("latest_modified_time");
 				}
 				
				JSONObject biddings = obj.getJSONObject("newbidding");
 				if(biddings != null)//
 				{
 					newbiddings = new ArrayList<sp_GetMyUpdatedBiddings_Result>();
 					JSONArray array = biddings.getJSONArray("biddings");
 					for(int i=0;i<array.length();i++)
 					{
 						newbiddings.add(new sp_GetMyUpdatedBiddings_Result(array.getJSONObject(i)));
 					}
 					
 					last_modified_time_bidding = biddings.getString("latest_modified_time");
 				}
 				
				JSONObject biddingmessages = obj.getJSONObject("newbiddingmessage");
 				if(biddingmessages != null)//
 				{
 					newbiddingmessages = new ArrayList<sp_GetNewMessage_Result>();
 					JSONArray array = biddingmessages.getJSONArray("biddingmessages");
 					for(int i=0;i<array.length();i++)
 					{
 						newbiddingmessages.add(new sp_GetNewMessage_Result(array.getJSONObject(i)));
 					}
 					
 					last_modified_time_biddingmessage = biddingmessages.getString("latest_modified_time");
 				}
 				
				JSONObject matches = obj.getJSONObject("newmatch");
 				if(matches != null)//
 				{
 					newmatches = new ArrayList<sp_GetMyUpdatedMatch_Result>();
 					JSONArray array = matches.getJSONArray("matches");
 					for(int i=0;i<array.length();i++)
 					{
 						newmatches.add(new sp_GetMyUpdatedMatch_Result(array.getJSONObject(i)));
 					}
 					
 					last_modified_time_match = matches.getString("latest_modified_time");
 				}
 			}
 			else//실패시는 errorcode입력
 			{
 				try
 				{
 					resultCode = ResultCode.valueOf(obj.getString("data"));
 				} catch (JSONException e) {
 					Logger.Instance().Write(e);
 				}
 			}
 			
 		} catch (JSONException e) {
 			Logger.Instance().Write(e);
 		}
 	}
 }
