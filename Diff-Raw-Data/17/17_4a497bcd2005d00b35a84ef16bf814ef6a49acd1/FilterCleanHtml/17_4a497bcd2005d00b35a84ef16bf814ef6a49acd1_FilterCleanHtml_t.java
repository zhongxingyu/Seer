 package db_processor.filter;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import db_processor.Filter;
 
 public class FilterCleanHtml extends Filter
 {
 	public FilterCleanHtml()
 	{
 		// Will be run less often than process()
 	}
 	
 	@Override
 	public void process(ResultSet row) throws SQLException
 	{
 		// Whether to fix invalid nesting: <i>Italic <b>Bold & Italic</i> Bold</b>
 		boolean fix_invalid_nesting = false;
 		
 		// Ending a non-started tag will remove the ending tag.
 		// Ending a incorrectly nested tag will end tags up to the starting tag.
 		// Unended tags will be appended to the end
 		
 		// <d><a><b></a></b></c>
 		// <d><a><b></b></a></b></c>
 		// <d><a><b></b></a>
 		// <d><a><b></b></a></d>
 		
 		boolean changed = false;
 		StringBuilder str = new StringBuilder(row.getString("text"));
 		LinkedList<String> tags = new LinkedList<String>();
 		
 		int start;
 		int end = 0;
 		while ((start = str.indexOf("<", end)) != -1)
 		{
 			// Is open or close tag?
 			int i = start;
 			while (Character.isWhitespace(str.charAt(++i)));
 			boolean end_tag = str.charAt(i) == '/';
 			if (end_tag) {i++;}
 			
 			// Find end of tag
 			int j, k;
 			while (true)
 			{
 				j = Math.min(str.indexOf("\"", i), str.indexOf("'", i));
 				k = str.indexOf(">", i);
 				if (k == -1)
 				{
 					// Tag not ended
 					//end_tag
 				}
 				if (j == -1 || k < j) {break;}
 				
 				// If a quote comes before a ">", find the end quote:
 				String quote = str.substring(j, j + 1);
 				do
 				{
 					j = str.indexOf(quote, j + 1);
 				} while (str.charAt(j - 1) == '\\');
 			}
 			
 			String tag = "div";
 			if (end_tag)
 			{
 				if (!tags.removeLastOccurrence(tag))
 				{
 					// If there was no starting tag, remove the end tag.
 					str.replace(start, end, "");
 					changed = true;
 				}
 			}
 			else
 			{
 				tags.add(tag);
 			}
 		}
 		
 		if (!tags.isEmpty())
 		{
 			ListIterator<String> i = tags.listIterator();
 			while (i.hasNext())
 			{
 				str.append("</" + i.next() + ">");
 			}
 			changed = true;
 		}
 		
 		if (changed)
 		{
 			row.updateString("text", str.toString());
 			row.updateRow();
			count("updated");
 		}
 	}
 }
