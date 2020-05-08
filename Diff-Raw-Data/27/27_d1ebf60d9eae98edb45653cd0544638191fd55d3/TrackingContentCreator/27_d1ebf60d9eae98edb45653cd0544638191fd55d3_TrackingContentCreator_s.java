 package de.age.logtool.util;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
public class TrackingContentCreator implements ContentCreator {
 	
 	private static final SimpleDateFormat dateformat = new SimpleDateFormat("yyMMdd,HH:mm:ss.SSS");;
 	private final List<String> entries = new ArrayList<String>();
 
 	@Override
 	public String createContent() {
 		StringBuffer newContent = new StringBuffer();
 		newContent.append(dateformat.format(new Date()));
 		newContent.append(" :: Contententry [");
 		newContent.append(entries.size());
		newContent.append("]\n");
 		entries.add(newContent.toString());
 		return newContent.toString();
 	}
 
 }
