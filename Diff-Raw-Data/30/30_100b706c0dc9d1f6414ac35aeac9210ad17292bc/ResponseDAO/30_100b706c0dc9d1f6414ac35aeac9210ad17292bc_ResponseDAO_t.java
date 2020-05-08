 package edu.unsw.cse.comp9323.group1.DAOs;
 
import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 
 import edu.unsw.cse.comp9323.group1.models.Question;
 
 public class ResponseDAO {
 
 	private String userPass = "michaelfebrianto@gmail.com:C0urs3Evalu@t10n!";
 
 	public void saveResponse(List<Question> responseList) {
 		
 		String url = "https://restapi.surveygizmo.com/head/survey/"
 				+ responseList.get(0).getSurveyId() + "/surveyresponse";
 
 		HttpClient client = new HttpClient();
 
 		/*
 		 * setup question type to survey gizmo format
 		 */
 		
 
 		GetMethod method = new GetMethod(url);
 
 		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
 				new DefaultHttpMethodRetryHandler(3, false));
 		
		//NameValuePair nameValuePair[] = new NameValuePair[responseList.size()+2];
		List<NameValuePair> nameValuePairLst = new ArrayList<NameValuePair>();
		nameValuePairLst.add(new NameValuePair("user:pass", userPass));
		nameValuePairLst.add(new NameValuePair("_method", "PUT"));
 		Iterator<Question> responseListItr = responseList.iterator();
 		
 		while(responseListItr.hasNext()){
 			Question response = responseListItr.next();
 			
 			if(response.getResponse().contains("###") && response.getResponse().contains(",")){
 				String loopToBeProcessed[] = response.getResponse().split(",");
 				for(String toBeProccessed:loopToBeProcessed){
 					String needToBeProcessed[] = toBeProccessed.split("###");
 					System.out.println(">>>>>"+needToBeProcessed[1]);
					nameValuePairLst.add(new NameValuePair("data["+response.getId()+"]["+needToBeProcessed[0]+"]", needToBeProcessed[1]));
 					//nameValuePairIndex++;
 				}
 			}
 			else if(response.getResponse().contains("###")){
 				String needToBeProcessed[] = response.getResponse().split("###");
				nameValuePairLst.add(new NameValuePair("data["+response.getId()+"]["+needToBeProcessed[0]+"]", needToBeProcessed[1]));
 			}
 			else{
				nameValuePairLst.add(new NameValuePair("data["+response.getId()+"][value]", response.getResponse()));
 			}
 		}
 		
		/*
		 * convert list to array
		 */
		NameValuePair nameValuePair[] = new NameValuePair[nameValuePairLst.size()];
		nameValuePair = nameValuePairLst.toArray(nameValuePair);
	    
 		method.setQueryString(nameValuePair);
 		
 		try {
 			// Execute the method.
 			int statusCode = client.executeMethod(method);
 
 			if (statusCode != HttpStatus.SC_OK) {
 				System.err.println("Method failed: " + method.getStatusLine());
 			}
 
 			// Read the response body.
 			byte[] responseBody = method.getResponseBody();
 
 			// Deal with the response.
 			// Use caution: ensure correct character encoding and is not binary
 			// data
 			String responseBodyStr = new String(responseBody);
 
 			System.out.println(">>>>>>>"+responseBodyStr);
 
 
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		} finally {
 			// Release the connection.
 			method.releaseConnection();
 		}
 
 	}
 
 	
 }
