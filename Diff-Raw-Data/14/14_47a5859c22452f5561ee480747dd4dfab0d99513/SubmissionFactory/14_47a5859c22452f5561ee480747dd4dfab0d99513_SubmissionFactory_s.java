 /*
  *  (c) 2012 University of Bolton
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package uk.ac.bolton.spaws.model.util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import uk.ac.bolton.spaws.model.IActor;
 import uk.ac.bolton.spaws.model.IRating;
 import uk.ac.bolton.spaws.model.ISubmitter;
 import uk.ac.bolton.spaws.model.impl.Submission;
 
 public class SubmissionFactory {
 	
 	public static Submission createSubmission(JSONObject object, String resourceURL) throws JSONException, ParseException{
 		
 		Submission submission = new Submission();
 		submission.setResourceURL(resourceURL);
 		
 		//
 		// Set the action 
 		//
 		IRating rating = RatingFactory.createRating(object);
 		submission.setAction(rating);
 		
 		//
 		// Set the actor
 		//
 		IActor actor = ActorFactory.createActor(object);
 		submission.setActor(actor);
 		
 		//
		// Set the updated date
 		//
 		String updated = object.getString("update_timestamp");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSSSSS'Z'");
 		df.setTimeZone(TimeZone.getTimeZone("UTC"));
 		Date date = df.parse(updated);
 		submission.setUpdated(date);
 		
 		//
 		// Set the submitter
 		//
 		ISubmitter submitter = SubmitterFactory.createSubmitter(object);
 		submission.setSubmitter(submitter);
 		
 		return submission;
 	}
 
 }
