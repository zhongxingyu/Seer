 package de.age.logtool.util;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;

public class TrackingContentCreator implements ContentCreator, SelfDescribing {
 	
 	private static final SimpleDateFormat dateformat = new SimpleDateFormat("yyMMdd,HH:mm:ss.SSS");;
 	private final List<String> entries = new ArrayList<String>();
 
 	@Override
 	public String createContent() {
 		StringBuffer newContent = new StringBuffer();
 		newContent.append(dateformat.format(new Date()));
 		newContent.append(" :: Contententry [");
 		newContent.append(entries.size());
		newContent.append("]");
 		entries.add(newContent.toString());
		newContent.append("\n");
 		return newContent.toString();
 	}
	
	public int getNumberOfEntries() {
		return entries.size();
	}
	
	public List<String> getEntries() {
		return Collections.unmodifiableList(entries);
	}
 
	@Override
	public void describeTo(Description description) {
		description.appendValueList("[", ",", "]", entries);
	}
	
 }
