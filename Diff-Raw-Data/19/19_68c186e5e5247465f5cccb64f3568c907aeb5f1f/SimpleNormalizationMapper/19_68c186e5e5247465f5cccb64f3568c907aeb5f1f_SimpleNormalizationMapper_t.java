 package nl.tudelft.in4325.a1.normalization;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import nl.tudelft.in4325.a1.indexing.TextArrayWritable;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.Mapper;
import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Normalizes corpus of XML documents with tokenization according to white spaces.
  */
 public class SimpleNormalizationMapper extends Mapper<Object, Text, Text, TextArrayWritable>{
 	
 	private static final String ID_TAG = "id";
 	private static final String TEXT_TAG = "text";
 	
     private Text word = new Text();
 
     private static final Logger LOGGER = LoggerFactory.getLogger(SimpleNormalizationMapper.class);
     
     public void map(Object key, Text value, Context context) throws IOException, InterruptedException{
     	//parsing the document contents
     	String stringValue = value.toString();
     	
     	int id = Integer.valueOf(extractContents(stringValue, ID_TAG)).intValue();
     	String text = extractContents(stringValue, TEXT_TAG);
     	if (text.length() == 0){
     		LOGGER.warn("No text was extracted for id " + id);
     	}
     	
     	//normalization and output
         StringTokenizer st = new StringTokenizer(text);
         int positionCounter = 0;
 		Map<String, List<String>> wordPossitions = new HashMap<String, List<String>>();
 		
         while(st.hasMoreTokens())
         {
         	String word = st.nextToken();
 			if (!wordPossitions.containsKey(word)) {
 				wordPossitions.put(word, new ArrayList<String>());
 			}
 
 			wordPossitions.get(word).add(String.valueOf(positionCounter));
 			
             positionCounter++;
         }
         
         for (String term : wordPossitions.keySet()) {
 			Writable[] tuples = {
 					new Text(String.valueOf(id)),
					new Text("[ " + StringUtils.join(wordPossitions.get(term).toArray(new String[0]), ",") + " ]") };
 			TextArrayWritable writableArrayWritable = new TextArrayWritable();
 			writableArrayWritable.set(tuples);
 			word.set(term);
 			context.write(word, writableArrayWritable);
 		}
     }
     
     /**
      * Extract contents of a specified tag from submitted XML data. Only first occurrence 
      * for data with existent starting and ending tags is considered. 
      * @param value XML data
      * @param tag tag to search with no brackets
      * @return contents inside the tag
      */
     protected String extractContents(String value, String tag){
     	if(value.length() < 100){
     		LOGGER.info(value);
     	}
     	int start = value.indexOf("<" + tag);
     	
     	if (start > -1){
     		//find matching closing bracket and move start pointer behind it
     		start = value.indexOf(">", start) + 1;
     	}
     	
     	int end = value.indexOf("</" + tag + ">");
     	if (start > -1 && end > -1){
     		return value.substring(start, end);
     	} else {
     		LOGGER.warn("Cannot extract contents: tag not found");
     		return "";
     	}
     }
     
 }
