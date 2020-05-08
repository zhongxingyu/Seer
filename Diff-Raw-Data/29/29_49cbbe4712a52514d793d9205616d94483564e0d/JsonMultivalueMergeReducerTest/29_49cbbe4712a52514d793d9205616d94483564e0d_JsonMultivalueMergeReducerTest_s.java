 package com.springsense.wikiindex;
 
 import static com.springsense.wikiindex.JsonMultivalueMergeReducer.mergeIntoLeft;
 import static org.hamcrest.Matchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
 import org.apache.hadoop.mrunit.types.Pair;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.umd.cloud9.io.JSONObjectWritable;
 
 public class JsonMultivalueMergeReducerTest {
 	ReduceDriver<Text, JSONObject, Text, JSONObject> driver = null;
 
 	@Before
 	public void setUp() {
 		driver = new ReduceDriver<Text, JSONObject, Text, JSONObject>(
 				new JsonMultivalueMergeReducer());
 	}
 
 	@Test
 	public void reducerShouldMergeJsonCorrectly() throws JSONException,
 			IOException {
 
 		JSONObject jsonValue1 = generateJSONValue(1);
 		JSONObject jsonValue2 = generateJSONValue(2);
 		JSONObject jsonValue3 = generateJSONValue(3);
 
 		Text expectedKey = new Text("article title");
 		
 		driver.withInputKey(expectedKey)
 				.withInputValue(jsonValue1).withInputValue(jsonValue2)
 				.withInputValue(jsonValue3);
 
 		List<Pair<Text, JSONObject>> output = driver.run();
 
 		assertThat(output.size(), equalTo(1));
 		assertThat(output.get(0).getFirst(), equalTo(expectedKey));
 
 		String actualAsString = output.get(0).getSecond().toString();
 		
 		String expectedAsString = mergeIntoLeft(
 				mergeIntoLeft(generateJSONValue(1),
 						generateJSONValue(2)), generateJSONValue(3)).toString();
 
 		assertThat(actualAsString, equalTo(expectedAsString));
 		
 	}
 
 	@Test
 	public void mergeShouldMergeJsonCorrectlySimple() throws JSONException {
 		JSONObject left = new JSONObject(
				"{\"letters\":\"alpha\",\"obj\":{\"one\":1},\"array\":[1,2,3]}");
 		JSONObject right = new JSONObject(
				"{\"letters\":\"beta\",\"obj\":{\"two\":2},\"array\":[4,5,6]}");
 
 		JSONObject result = JsonMultivalueMergeReducer.mergeIntoLeft(left,
 				right);
 
 		assertThat(
 				result.toString(),
				equalTo("{\"obj\":{\"two\":2,\"one\":1},\"letters\":[\"alpha\",\"beta\"],\"array\":[1,2,3,4,5,6]}"));
 	}
 
 	@Test
 	public void mergeShouldMergeJsonCorrectlyComplex() throws JSONException {
 		JSONObject left = generateJSONValue(1);
 		JSONObject right = generateJSONValue(2);
 
 		JSONObject result = JsonMultivalueMergeReducer.mergeIntoLeft(left,
 				right);
 
 		assertThat(
 				result.toString(),
 				equalTo("{\"title\":[\"Article 1\",\"Article 2\"],\"articles\":[{\"title\":\"Article 12\",\"number\":12,\"array\":[121,122,123]},{\"title\":\"Article 13\",\"number\":13,\"array\":[131,132,133]},{\"title\":\"Article 14\",\"number\":14,\"array\":[141,142,143]},{\"title\":\"Article 22\",\"number\":22,\"array\":[221,222,223]},{\"title\":\"Article 23\",\"number\":23,\"array\":[231,232,233]},{\"title\":\"Article 24\",\"number\":24,\"array\":[241,242,243]}],\"article\":{\"title\":[\"Article 11\",\"Article 21\"],\"number\":[11,21],\"array\":[111,112,113,211,212,213]},\"number\":[1,2],\"array\":[11,12,13,21,22,23]}"));
 	}
 
 	private JSONObject generateJSONValue(int i) throws JSONException {
 		JSONObject result = new JSONObjectWritable();
 
 		if (i < 10) {
 			result.append("articles", generateJSONValue((i * 10) + 2));
 			result.append("articles", generateJSONValue((i * 10) + 3));
 			result.append("articles", generateJSONValue((i * 10) + 4));
 		}
 
 		result.put("title", String.format("Article %d", i));
 		result.put("number", i);
 
 		if (i < 10) {
 			result.put("article", generateJSONValue((i * 10) + 1));
 		}
 
 		result.append("array", i * 10 + 1);
 		result.append("array", i * 10 + 2);
 		result.append("array", i * 10 + 3);
 
 		return result;
 	}
 }
