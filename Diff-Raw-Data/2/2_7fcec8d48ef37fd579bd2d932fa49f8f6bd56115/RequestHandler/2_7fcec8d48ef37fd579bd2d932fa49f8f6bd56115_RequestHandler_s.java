 package com.mobilewiki;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.mobilewiki.controls.WebserviceAdapter;
 
 public class RequestHandler {
 	
     private static RequestHandler _instance;
     private static WebserviceAdapter webserivce_adapter = new WebserviceAdapter();
     
     public static RequestHandler getInstance() {
         if(null == _instance) {
             _instance = new RequestHandler();
         }
         return _instance;
     }
 
     private RequestHandler() {
         _instance = this;
     }
 
     @SuppressWarnings("unchecked")
 	public List<Integer> getArticleIds() {
     	List<Integer> result = new ArrayList<Integer>();
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getArticleIds");
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = (List<Integer>) jsonobject_response.get("result");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
     }
 
 	public String getTitleForArticleId(int article_id) {
     	String result = "";
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getTitleForArticleId");
 			jsonobject_request.put("article_id", article_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = (String) jsonobject_response.get("result");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 
 	public int getArticleIdForTitle(String title) {
     	int result = 0;
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getArticleIdForTitle");
 			jsonobject_request.put("title", title);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = Integer.parseInt(jsonobject_response.get("result").toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Integer> getContentIdsforArticleId(int article_id) {
     	List<Integer> result = new ArrayList<Integer>();
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getContentIdsforArticleId");
 			jsonobject_request.put("article_id", article_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = convertJsonArrayToArrayList((JSONArray) jsonobject_response.get("result"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 
 	public String getDateChangeForContentId(int content_id) {
     	String result = "";
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getDateChangeForContentId");
 			jsonobject_request.put("content_id", content_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = (String) jsonobject_response.get("result");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 
 	public int getArticleIdForContentId(int content_id) {
     	int result = 0;
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getArticleIdForContentId");
 			jsonobject_request.put("content_id", content_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = Integer.parseInt(jsonobject_response.get("result").toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 
 	public String getContentForContentId(int content_id) {
     	String result = "";
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getContentForContentId");
 			jsonobject_request.put("content_id", content_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = (String) jsonobject_response.get("result");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 
 	public String getTagForContentId(int content_id) {
 		String result = "";
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getTagForContentId");
 			jsonobject_request.put("content_id", content_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = (String) jsonobject_response.get("result");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 
 	public HashMap<String, String> getContentTitleTagForArticleId(int article_id) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getContentTitleTagForArticleId");
 			jsonobject_request.put("article_id", article_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result.put("article_id", (String) jsonobject_response.get("article_id"));
 				result.put("content_id", (String) jsonobject_response.get("content_id"));
 				result.put("title", (String) jsonobject_response.get("title"));
 				result.put("content", (String) jsonobject_response.get("content"));
 				result.put("tag", (String) jsonobject_response.get("tag"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 
 	public HashMap<String, String> getContentTitleTagForContentId(int content_id) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getContentTitleTagForContentId");
 			jsonobject_request.put("content_id", content_id);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result.put("article_id", (String) jsonobject_response.get("article_id"));
 				result.put("content_id", (String) jsonobject_response.get("content_id"));
 				result.put("title", (String) jsonobject_response.get("title"));
 				result.put("content", (String) jsonobject_response.get("content"));
 				result.put("tag", (String) jsonobject_response.get("tag"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 	
 	public HashMap<String, String> getContentTitleTagForTitle(String title) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "getContentTitleTagForTitle");
 			jsonobject_request.put("title", title);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result.put("article_id", (String) jsonobject_response.get("article_id"));
 				result.put("content_id", (String) jsonobject_response.get("content_id"));
 				result.put("title", (String) jsonobject_response.get("title"));
 				result.put("content", (String) jsonobject_response.get("content"));
 				result.put("tag", (String) jsonobject_response.get("tag"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 	
 	public int createMain(String wiki_name, String logo_link) {
 		int result = -1;
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "createMain");
 			jsonobject_request.put("wiki_name", wiki_name);
 			jsonobject_request.put("logo_link", logo_link);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = Integer.parseInt(jsonobject_response.get("result").toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 	
 	public int createArticle(String title) {
 		int result = -1;
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
 			jsonobject_request.put("function", "createArticle");
 			jsonobject_request.put("title", title);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = Integer.parseInt(jsonobject_response.get("result").toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 	
 	public int createContent(int article_id, String content, String tag) {
 		int result = -1;
     	
     	try {
     		JSONObject jsonobject_request = new JSONObject();
			jsonobject_request.put("function", "createArticle");
 			jsonobject_request.put("article_id", article_id);
 			jsonobject_request.put("content", content);
 			jsonobject_request.put("tag", tag);
 	    	
 			JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 			
 			if (jsonobject_response.get("result") != null) {
 				result = Integer.parseInt(jsonobject_response.get("result").toString());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     	
         return result;
 	}
 	
     public Map<String, String> get_all_titles_with_tags() {
         Map<String, String> result = new HashMap<String, String>();
 
         String resultString;
 
         try {
             JSONObject jsonobject_request = new JSONObject();
             jsonobject_request.put("function", "getAllTitlesWithTags");
 
             JSONObject jsonobject_response = webserivce_adapter.callWebservice(jsonobject_request);
 
             if (jsonobject_response.get("result") != null) {
                 resultString = jsonobject_response.get("result").toString();
                 String[] resultArray = resultString.split("\n");
                 for(int i = 0; i < resultArray.length; i += 2) {
                     result.put(resultArray[i], resultArray[i + 1]);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return result;
     }
     
 	private ArrayList<Integer> convertJsonArrayToArrayList(JSONArray jsonArray) {
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		for (int i = 0; i < jsonArray.length(); i++) {
 			try {
 				list.add(Integer.parseInt(jsonArray.get(i).toString()));
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return list;
 	}
 }
