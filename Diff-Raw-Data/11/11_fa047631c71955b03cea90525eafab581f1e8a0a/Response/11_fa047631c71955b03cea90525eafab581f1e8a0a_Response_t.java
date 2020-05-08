 /*
  * Copyright 2011 Armin Čoralić
  * 
  * 	http://blog.coralic.nl
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package nl.coralic.beta.sms.utils.objects;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import nl.coralic.beta.sms.R;
 import nl.coralic.beta.sms.utils.ApplicationContextHelper;
 import nl.coralic.beta.sms.utils.Utils;
 
 public class Response
 {
     protected boolean responseOke = false;
     protected int errorCode;
     protected String errorMessage;
     protected String response;
 
     public Response(int errorCode)
     {
 	this.errorCode = errorCode;
     }
 
     public Response(String response)
     {
 	this.responseOke = true;
 	this.response = response;
     }
 
     public boolean isResponseOke()
     {
 	return responseOke;
     }
 
     public int getErrorCode()
     {
 	return errorCode;
     }
 
     public String getErrorMessage()
     {
 	// it the error code is BETAMAX_FIXED_ERROR_CODE then it means the error message is coming from Betamax provider, show that one instead of our own
 	if (errorCode == Const.BETAMAX_FIXED_ERROR_CODE)
 	{
 	    return errorMessage;
 	}
 	else
 	{
 	    return ApplicationContextHelper.getErrorMsgFromErrorCode(errorCode);
 	}
     }
 
     public String getResponse()
     {
 	return response;
     }
 
     public void validateBetamaxResponse()
     {
 	if (responseOke)
 	{
 	    try
 	    {
		Document doc = Utils.getDocument(response);
 		Element root = doc.getDocumentElement();
 		root.normalize();
		if (!root.getElementsByTagName("SmsResponse").item(0).getFirstChild().getNodeValue().equalsIgnoreCase("success"))
 		{
 		    responseOke = false;
 		    String tmpCause = root.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
 		    if (tmpCause.equalsIgnoreCase(""))
 		    {
 			errorCode = R.string.ERR_READ_RESP;
 		    }
 		    else
 		    {
 			// For now we use the Betamax errors to show to the user so we need custom error code, all other errors are coming from text.xml
 			errorMessage = tmpCause;
 			errorCode = Const.BETAMAX_FIXED_ERROR_CODE;
 		    }
 		}
 	    }
 	    catch (Exception e)
 	    {
 		responseOke = false;
 		errorCode = R.string.ERR_READ_RESP;
 	    }
 	}
     }
 }
