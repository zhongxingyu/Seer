 package com.springsense.wikiindex;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.umd.cloud9.io.JSONObjectWritable;
 
 public class JsonMultivalueMergeReducer extends
 		Reducer<Text, JSONObject, Text, JSONObject> {
 
 	protected void reduce(Text key, Iterable<JSONObject> values, Context context)
 			throws IOException, InterruptedException {
 
 		JSONObject merged = new JSONObjectWritable();
 
 		for (Iterator<JSONObject> i = values.iterator(); i.hasNext();) {
 			JSONObject right = i.next();
 
 			try {
 				mergeIntoLeft(merged, right);
 			} catch (JSONException e) {
 				throw new RuntimeException(
 						"Problem while merging JSON objects", e);
 			}
 		}
 		
 		context.write(key, merged);
 	}
 
 	protected static Object mergeValues(Object leftHand, Object rightHand)
 			throws JSONException {
 
 		if (leftHand == null) {
 			return rightHand;
 		}
 
 		if (rightHand == null) {
 			return leftHand;
 		}
 
 		if ((leftHand instanceof JSONObject)
 				&& (rightHand instanceof JSONObject)) {
 			return mergeIntoLeft((JSONObject) leftHand, (JSONObject) rightHand);
 		}
 
 		Object leftHandNormalised = leftHand;
 		if (!(leftHandNormalised instanceof JSONArray)) {
 			JSONArray leftHandAsArray = new JSONArray();
 			leftHandAsArray.put(leftHand);
 
 			leftHandNormalised = leftHandAsArray;
 		}
 
 		Object rightHandNormalised = rightHand;
 		if (!(rightHandNormalised instanceof JSONArray)) {
 			JSONArray rightHandAsArray = new JSONArray();
 			rightHandAsArray.put(rightHand);
 
 			rightHandNormalised = rightHandAsArray;
 		}
 
 		return mergeJSONArray((JSONArray) leftHandNormalised,
 				(JSONArray) rightHandNormalised);
 	}
 
 	private static Object mergeJSONArray(JSONArray leftHand, JSONArray rightHand)
 			throws JSONException {
 		JSONArray mergedArray = new JSONArray();
 
 		int leftLength = leftHand.length();
 		for (int i = 0; i < leftLength; i++) {
 			mergedArray.put(leftHand.get(i));
 		}
 
 		int rightLength = rightHand.length();
 		for (int i = 0; i < rightLength; i++) {
 			mergedArray.put(rightHand.get(i));
 		}
 
 		return mergedArray;
 	}
 
 	public static JSONObject mergeIntoLeft(JSONObject leftHand,
 			JSONObject rightHand) throws JSONException {
 		Set<String> keys = new HashSet<String>();
 
 		for (Iterator<String> i = leftHand.keys(); i.hasNext();) {
 			keys.add(i.next());
 		}
 
 		for (Iterator<String> i = rightHand.keys(); i.hasNext();) {
 			keys.add(i.next());
 		}
 
 		for (String key : keys) {
 			leftHand.put(key,
 					mergeValues(leftHand.opt(key), rightHand.opt(key)));
 		}
 
 		return leftHand;
 	}
 }
