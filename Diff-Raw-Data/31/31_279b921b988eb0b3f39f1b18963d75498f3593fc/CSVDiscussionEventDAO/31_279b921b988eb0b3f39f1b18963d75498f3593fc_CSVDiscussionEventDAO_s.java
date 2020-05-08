 package org.computer.knauss.reqtDiscussion.io.csv;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.computer.knauss.reqtDiscussion.io.IDiscussionEventDAO;
import org.computer.knauss.reqtDiscussion.io.Util;
 import org.computer.knauss.reqtDiscussion.model.DiscussionEvent;
 import org.computer.knauss.reqtDiscussion.model.IDiscussionEventFilter;
 
 public class CSVDiscussionEventDAO implements IDiscussionEventDAO {
 
 	public final static String PROP_FILENAME = "file";
 	public final static String PROP_DELIMITER = "delim";
 	public final static String PROP_ID_COL = "id";
 	public final static String PROP_DISC_ID_COL = "did";
 	public final static String PROP_CONTENT_COL = "content";
 	public final static String PROP_CDATE_COL = "cdate";
 	public final static String PROP_CREATOR_COL = "creator";
 	public final static String PROP_START_ROW = "start";
 
 	private Properties properties;
 
 	@Override
 	public DiscussionEvent[] getDiscussionEventsOfDiscussion(
 			final int discussionId) {
 		return getDiscussionEvents(new IDiscussionEventFilter() {
 
 			@Override
 			public boolean accept(DiscussionEvent de) {
 				return de.getDiscussionID() == discussionId;
 			}
 		});
 	}
 
 	@Override
 	public DiscussionEvent getDiscussionEvent(final int id) {
 		DiscussionEvent[] result = getDiscussionEvents(new IDiscussionEventFilter() {
 
 			@Override
 			public boolean accept(DiscussionEvent de) {
 				return de.getID() == id;
 			}
 		});
 
 		if (result == null || result.length == 0)
 			return null;
 		return result[0];
 	}
 
 	private DiscussionEvent[] getDiscussionEvents(IDiscussionEventFilter filter) {
 		if (this.properties == null)
 			throw new RuntimeException("Not configured with properties!");
 
 		List<DiscussionEvent> result = new Vector<DiscussionEvent>();
 		try {
 			// 1. open a file reader
 			BufferedReader r = new BufferedReader(new FileReader(
 					this.properties.getProperty(PROP_FILENAME)));
 			// 2. generate one DiscussionEvent per row
 			int startRow = Integer.valueOf(this.properties
 					.getProperty(PROP_START_ROW));
 			String line = null;
 
 			for (int i = -2; i < startRow; i++)
 				line = r.readLine();
 
 			while (line != null) {
 				DiscussionEvent discussionEvent = createDiscussionEvent(line);
 				if (discussionEvent != null) {
 					if (filter.accept(discussionEvent))
 						result.add(discussionEvent);
 				}
 				line = r.readLine();
 			}
 			r.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return result.toArray(new DiscussionEvent[0]);
 	}
 
 	private DiscussionEvent createDiscussionEvent(String line) {
 		DiscussionEvent ret = new DiscussionEvent();
 
 		// System.out.println("################################");
 		// System.out.println(line);
 		if (line.trim().length() == 0)
 			return null;
 		char delimiter = this.properties.getProperty(PROP_DELIMITER)
 				.toCharArray()[0];
 		// System.out.println("delimiter = " + delimiter);
 
 		String[] vals = new String[5];
 		StringBuffer sb = new StringBuffer();
 
 		boolean insideString = false;
 		int col = 0;
 		for (char c : line.toCharArray()) {
 			if (c == '"')
 				insideString = !insideString;
 			else if (!insideString && c == delimiter) {
 				vals[col] = sb.toString();
 				sb = new StringBuffer();
 				col++;
 			} else {
 				sb.append(c);
 			}
 		}
 		vals[col] = sb.toString();
 
 		Integer idCol = Integer.valueOf(this.properties
 				.getProperty(PROP_ID_COL));
 		// System.out.println(idCol);
 		ret.setID(Integer.valueOf(vals[idCol]));
 		ret.setDiscussionID(Integer.valueOf(vals[Integer
 				.valueOf(this.properties.getProperty(PROP_DISC_ID_COL))]));
 		ret.setContent(vals[Integer.valueOf(this.properties
 				.getProperty(PROP_CONTENT_COL))]);
 
 		// System.out.println("---Date");
 		String dateString = vals[Integer.valueOf(this.properties
 				.getProperty(PROP_CDATE_COL))];
 		// System.out.println(dateString);
 
 		ret.setCreationDate(Util.parseDate(dateString));
 		ret.setCreator(vals[Integer.valueOf(this.properties
 				.getProperty(PROP_CREATOR_COL))]);
 
 		return ret;
 	}
 
 	@Override
 	public void storeDiscussionEvent(DiscussionEvent de) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void storeDiscussionEvents(DiscussionEvent[] des) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void configure(Properties p) {
 		if (p.getProperty(PROP_FILENAME) == null) {
 			throw new IllegalArgumentException("You did not specify the 'file'");
 		}
 		if (!p.containsKey(PROP_DELIMITER)) {
 			p.setProperty(PROP_DELIMITER, ";");
 		}
 		if (p.getProperty(PROP_ID_COL) == null) {
 			p.setProperty(PROP_ID_COL, String.valueOf(0));
 		}
 		if (p.getProperty(PROP_DISC_ID_COL) == null) {
 			p.setProperty(PROP_DISC_ID_COL, String.valueOf(1));
 		}
 		if (p.getProperty(PROP_CONTENT_COL) == null) {
 			p.setProperty(PROP_CONTENT_COL, String.valueOf(2));
 		}
 		if (p.getProperty(PROP_CDATE_COL) == null) {
 			p.setProperty(PROP_CDATE_COL, String.valueOf(3));
 		}
 		if (p.getProperty(PROP_CREATOR_COL) == null) {
 			p.setProperty(PROP_CREATOR_COL, String.valueOf(4));
 		}
 		if (!p.contains(PROP_START_ROW)) {
 			p.setProperty(PROP_START_ROW, "0");
 		}
 
 		this.properties = p;
 	}
 
 	@Override
 	public Properties getConfiguration() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Map<String, String> checkConfiguration() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
