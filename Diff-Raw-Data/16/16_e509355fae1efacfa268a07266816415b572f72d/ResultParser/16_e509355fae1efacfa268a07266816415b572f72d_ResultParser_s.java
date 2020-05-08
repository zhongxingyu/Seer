 package suggest;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 public class ResultParser {
 	
 	
 	public ResultParser() {
 		
 	}
 
 	public String parse(String response, int number) {
 		System.out.println("NUMBERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR:         " + number);
 		if (number == 0) {
 			System.out.println(response);
 			return wikiParse(response);
 			
 		}
 		else if (number==1) {
 			System.out.println("dict:   " + response);
 			return dictParse(response);
 		}
 		else if (number==2) {
 			System.out.println("ducky:  " + response);
 			return duckParse(response);
 //			int indexOf = response.indexOf("Definition");
 //			System.out.println(indexOf);
 //			int indexOf2 = response.indexOf("DefinitionS", indexOf);
 //			System.out.println(indexOf2);
 //			String substring = "";
 //			if (indexOf < 0 || indexOf2 < 0) {
 //				substring = response;
 //			}
 //			else {
 //				substring = response.substring(indexOf, indexOf2);
 //			}
 //			System.out.println(substring);
 //			return substring;
 		}
 		
 		return "No Results Returned.";
 	}
 
 	private String dictParse(String response) {
 		response = response.substring(25, response.length()-10);
 		System.out.println("|" + response + "|");
 		
 		JSONObject json = (JSONObject) JSONSerializer.toJSON(response);
 		String querystr = json.getString("query");
 		System.out.println("dictquery:  " + querystr);
 		
 		// Primaries Definitions
 		if (!json.has("primaries")) {
 			// no results
 			return "No Definitions Available";
 		}
 		JSONArray primariesArr = json.getJSONArray("primaries");
 		StringBuilder sb = new StringBuilder();
 		sb.append("Definition Results:\n");
 		String line = "";
 		int numTotal = 1;
 		for (int i = 0; i < primariesArr.size(); i++) {
 			JSONObject topObj = primariesArr.getJSONObject(i);
 			JSONArray objEntriesArr = topObj.getJSONArray("entries");
 			for (int j = 0; j < objEntriesArr.size(); j++) {
 				JSONObject typeObj = objEntriesArr.getJSONObject(j);
 				if (!typeObj.getString("type").equals("meaning")) {
 					System.out.println("skipping:  " + typeObj.getString("type"));
 					continue;
 				}
 				JSONArray termArr = typeObj.getJSONArray("terms");
 				for (int k = 0; k < termArr.size(); k++) {
 					line = "";
 					line += numTotal + ".   ";
 					JSONObject textObj = termArr.getJSONObject(k);
 					String text = textObj.getString("text");
 					// remove <*> tags
 					while (text.contains("<")) {
 						int indexOf = text.indexOf("<");
 						int indexOf2 = text.indexOf(">");
 						text= text.substring(0,indexOf) + text.substring(indexOf2+1, text.length());
 					}
 					line += text;
 					line += "\n\n";
 					sb.append(line);
 					numTotal++;
 				}
 			}
 		}
 		System.out.println("totsdef" + sb.toString());
 		return sb.toString();
 		
 	}
 
 	private String duckParse(String response) {
 		JSONObject json = (JSONObject) JSONSerializer.toJSON(response);
 		StringBuilder sb = new StringBuilder("                    Related Categories and Topics : \n");
 		JSONArray relatedArr = json.getJSONArray("RelatedTopics");
 		if (relatedArr.size() == 0) {
 			sb.append("No Topics Found.");
 			return sb.toString();
 		}
 		String line = "";
 		for (int i = 0; i < relatedArr.size(); i++) {
 			JSONObject topicObj = relatedArr.getJSONObject(i);
 			if (!topicObj.has("Topics")) {
 				continue;
 			}
 			JSONArray topicsArr = topicObj.getJSONArray("Topics");
 			String name = topicObj.getString("Name");
 			sb.append(name + ":\n");
 			int numper = 1;
 			for (int j = 0; j < topicsArr.size(); j++) {
 				line = "";
 				line += numper + ".   ";
 				JSONObject elementObj = topicsArr.getJSONObject(j);
 				String text = elementObj.getString("Text");
 				
 				line += text;
 				line += "\n\n";
 				System.out.println("aline:   " + line);
 				sb.append(line);
 				numper++;
 			}
 			
 		}
 		System.out.println("totsduck" + sb.toString());
 		return sb.toString();
 	}
 	
 
 	private String wikiParse(String response) {
 	    JSONObject json = (JSONObject) JSONSerializer.toJSON(response);
 	    JSONObject queryObj = json.getJSONObject("query");
 	    JSONObject pagesObj = queryObj.getJSONObject("pages");
 	    
 	    int indexOf = response.indexOf("pages");
 		System.out.println("in1  " + indexOf);
 		int indexOf2 = response.indexOf("{", indexOf);
 		System.out.println("in2  " + indexOf2);
 		indexOf = response.indexOf(":", indexOf2);
 		System.out.println("in3  " + indexOf);
 		String substring = "";
 		if (indexOf < 0 || indexOf2 < 0) {
 			substring = response;
 		}
 		else {
 			substring = response.substring(indexOf2+2, indexOf-1);
 		}
 		System.out.println("pageId" + substring);
 	    if (substring.equals("-1")) {
 	    	//TODO handle no page results from wiki
 	    	// return/break out of parsing
 	    	return("No Wikipedia Results Found");
 	    }
 	    
 	    JSONObject pageidObj = pagesObj.getJSONObject(substring);
 	    JSONArray revisionArr = pageidObj.getJSONArray("revisions");
 	    JSONObject revisionObj = revisionArr.getJSONObject(0);
 	    String article = revisionObj.getString("*");
 	    
 	    // TODO handle redirects
 	    if (article.startsWith("#REDIRECT") || article.startsWith("#redirect")) {
 	    	return article;
 	    }
 	    
 	    article = removeTriple(article);
 	    article  = removeImages(article);
 	    article = removeBraces(article);
 	    article = removeFiles(article);
 	    article = removeTags(article);
 	    article  = removeTagBraces(article);
 	    article = removeDouble(article);
 	    while (article.startsWith("\n") || article.startsWith(":") || article.startsWith(" ")) {
 	    	article = article.substring(1, article.length());
 	    }
 	    // TODO handle multiple options
 	    if (article.indexOf(":") < article.indexOf("\n")) {
 	    	System.out.println("Multiple Options son");
 	    	return article;
 	    }
 	    article = removeParens(article);
 	    while (article.startsWith("\n") || article.startsWith(":") || article.startsWith(" ")) {
 	    	article = article.substring(1, article.length());
 	    }
 	    
 	    
 	    indexOf = article.indexOf("==");
 	    if (indexOf >=0) {
 	    	article = article.substring(0, indexOf);
 	    }
 	    
 		return article;
 	}
 	
 	private String removeImages(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int index = sb.indexOf("[[Image:");
 		int index2 = sb.indexOf("]]", index);
 		int index3 = sb.indexOf("[[", index+4);
 		while (index >= 0) {
 			index2 = sb.indexOf("]]", index);
 			index3 = sb.indexOf("[[", index+4);
 			while (index3 >= 0 && index3 < index2) {
 				index2 = sb.indexOf("]]", index2+2);
 				index3 = sb.indexOf("[[", index3+2);
 			}
 			if (index >= 0 && index2 >= 0) {
 				sb.delete(index, index2+2);
 			} else {
 				break;
 			}
 			
 			index = sb.indexOf("[[Image:");
 		}
 		return sb.toString();
 	}
 	
 	private String removeFiles(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int index = sb.indexOf("[[File:");
 		int index2 = sb.indexOf("]]", index);
 		int index3 = sb.indexOf("[[", index+4);
 		while (index >= 0) {
 			index2 = sb.indexOf("]]", index);
 			index3 = sb.indexOf("[[", index+4);
 			while (index3 >= 0 && index3 < index2) {
 				index2 = sb.indexOf("]]", index2+2);
 				index3 = sb.indexOf("[[", index3+2);
 			}
 			if (index >= 0 && index2 >= 0) {
 				sb.delete(index, index2+2);
 			} else {
 				break;
 			}
 			index = sb.indexOf("[[File:");
 		}
 		return sb.toString();
 	}
 
 	private String removeTagBraces(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int balance = 0;
 		int start = 0;
 		int end = 0;
 		boolean done = false;
 		for (int i = 0; i < sb.length(); i++) {
 			char letter = sb.charAt(i);
 			if(letter == '<') {
 				balance++;
 				if (balance == 1) {
 					start = i;
 				}
 			}
 			else if (letter == '>') {
 				balance--;
 				if (balance == 0) {
 					end = i;
 					done = true;
 				}
 			}
 			if (done) {
 				i = i - end + start - 1;
 				if (start >= 0 && end >= 0) {
 					sb.delete(start, end+1);
 				}
 				done = false;
 			}
 		}
 		return sb.toString();
 	}
 	
 	private String removeTags(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int index = sb.indexOf("<ref");
 		int index2 = sb.indexOf("/ref>", index);
 		while (index >= 0 && index2 >= 0) {
 			sb.delete(index, index2+5);
 			index = sb.indexOf("<ref");
 			index2 = sb.indexOf("/ref>", index);
 		}
 		return sb.toString();
 	}
 	
 	private String removeTriple(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int index = sb.indexOf("'''");
 		while(index != -1) {
 			sb.delete(index, index+3);
 			index = sb.indexOf("'''");
 		}
 		return sb.toString();
 	}
 
 	// this needs to be fixed
 	// the deletion of everything between doubles is tough at the same time as just deleting the triples
 	// probably should break them up or do a better job of if-else statements
 	// ex: systems engineering, commonwealth of nations, Geology
 	private String removeDouble(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int index = sb.indexOf("''");
 		int index2 = sb.indexOf("''", index+2);
 		boolean erase = false;
 		while (index >= 0 && index2 >= 0) {
 			if (erase) {
 				sb.delete(index, index2+2);
 				erase = false;
 			} else {
 				erase = true;
 			}
 		index = sb.indexOf("''");
 		index2 = sb.indexOf("''", index+2);
 		}
 		return sb.toString();
 	}
 
 	private String removeParens(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int balance = 0;
 		int start = 0;
 		int end = 0;
 		char letter = 'd';
 		boolean done = false;
 		for (int i = 0; i < sb.length(); i++) {
 			letter = sb.charAt(i);
 			if(letter == '(') {
 				balance++;
 				if (balance == 1) {
 					start = i;
 				}
 			}
 			else if (letter == ')') {
 				balance--;
 				if (balance == 0) {
 					end = i;
 					done = true;
 				}
 			}
 			if (done) {
 				i = i - end + start - 1;
 				if (start >= 0 && end >= 0) {
 					sb.delete(start, end+1);
 				}
 				done = false;
 			}
 		}
 		return sb.toString();
 	}
 	
 	private String removeBraces(String article) {
 		StringBuilder sb = new StringBuilder(article);
 		int balance = 0;
 		int start = 0;
 		int end = 0;
 		char letter = 'd';
 		boolean done = false;
 		for (int i = 0; i < sb.length(); i++) {
 			letter = sb.charAt(i);
 			if(letter == '{') {
 				balance++;
 				if (balance == 1) {
 					start = i;
 				}
 			}
 			else if (letter == '}') {
 				balance--;
 				if (balance == 0) {
 					end = i;
 					done = true;
 				}
 			}
 			if (done) {
 				i = i - end + start - 1;
 				if (start >= 0 && end >= 0) {
 					sb.delete(start, end+1);
 				}
 				done = false;
 			}
 		}
 		return sb.toString();
 	}
 	
 	
 
 }
