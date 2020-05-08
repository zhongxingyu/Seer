 package net.cyklotron.cms.files.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.utils.StackTrace;
 
 /**
  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
  * @version $Id: CSVFileReader.java,v 1.4 2005-02-21 16:29:27 zwierzem Exp $
  */
 public class CSVFileReader
 {
 	private LineNumberReader reader;
 	
 	private List<String> columnNames;
 	
 	private char fieldSeparator; 
 	
 	private int counter;
 	
 	public CSVFileReader(InputStream is, String encoding, char fieldSeparator)
 	{
 		columnNames = new ArrayList<String>();
 		this.fieldSeparator = fieldSeparator;
 		try
 		{
 			reader = new LineNumberReader(new InputStreamReader(is,encoding));
 			String header = reader.readLine();
 			StringTokenizer st = new StringTokenizer(header,""+fieldSeparator);
 			while(st.hasMoreTokens())
 			{
				String name = st.nextToken();
				//System.out.println("HEADER : "+name.substring(1,name.length()-1));
				columnNames.add(name.substring(1,name.length()-1));
 			}
 		}
 		catch(Exception e)
 		{
 			throw new ComponentInitializationError("Failed to initialize CSV Reader class "+new StackTrace(e).toString());
 		}
 		counter = 1;
 	}
 	
 	public String[] getColumnNames()
 	{
 		String[] columns = new String[columnNames.size()];
 		columnNames.toArray(columns);
 		return columns;
 	}
 
 	public Map<String, String> getNextLine()
 		throws IOException
 	{
 		
 		if(!reader.ready())
 		{
 			return null;
 		}
 		Map<String, String> map = new HashMap<String, String>();
 		String line = reader.readLine();
 		counter++;
 		if(!checkParity(line) && reader.ready())
 		{
 			StringBuilder sb = new StringBuilder(line);
 			do
 			{
 				line = reader.readLine();
 				counter++;
 				sb.append("\r\n");
 				sb.append(line);
 			}
 			while(reader.ready() && checkParity(line));
 			line = sb.toString();
 		}
 		
 		String[] columns = parse(line);
 		//System.out.println("liczba kolumn w wierszu "+counter+": "+columns.length);
 		for(int i = 0; i < columns.length; i++)
 		{
 			map.put(columnNames.get(i), columns[i]);
 		}
 		return map;
 	}
 	
 	private boolean checkParity(String line)
 	{
 		boolean parity = true;
 		char[] chars = line.toCharArray();
 		for(int i = 0; i < chars.length; i++)
 		{
 			if(chars[i] == '"')
 			{
 				parity = !parity;
 			}
 		}
 		return parity;
 	}
 	
 	private String[] parse(String line)
 	{
 		StringBuilder sb = new StringBuilder();
 		List<String> list = new ArrayList<String>();
 		int i = 0;
 		if (line.length() == 0) 
 		{
 			list.add(line);
 			return (String[])list.toArray(new String[]{});
 		}
 		do 
 		{
 			sb.setLength(0);
 			if (i < line.length() && line.charAt(i) == '"')
 			{
 				i = parseQuoted(line, sb, ++i);
 			}
 			else
 			{
 				i = parseScalar(line, sb, i);
 			}
 			//System.out.println("Dodaje "+ (list.size() + 1) +" : " + sb.toString());
 			//System.out.println("..."+ i);
 			list.add(sb.toString());
 			i++;
 		} 
 		while (i <= line.length());
 		return (String[])list.toArray(new String[]{});
 	}
 
 	private int parseQuoted(String s, StringBuilder sb, int i)
 	{
 		int j;
 		int len= s.length();
 		for (j=i; j<len; j++)
 		{
 			if (s.charAt(j) == '"' && j+1 < len)
 			{
 				if (s.charAt(j+1) == '"')
 				{
 					j++;
 				}
 				else if (s.charAt(j+1) == fieldSeparator)
 				{
 					j++;
 					break;
 				}
 			} 
 			else if (s.charAt(j) == '"' && j+1 == len)
 			{
 				break;
 			}
 			sb.append(s.charAt(j));
 		}
 		// consider case that " ends the line
 		if(j + 1 == len)
 		{
 			return len;
 		}
 		return j;
 	}
 
 	private int parseScalar(String s, StringBuilder sb, int i)
 	{
 		int index = s.indexOf(fieldSeparator, i);
 		if (index == -1)
 		{    
 			sb.append(s.substring(i));
 			return s.length();
 		}
 		else
 		{
 			sb.append(s.substring(i, index));
 			return index;
 		}
 	}
 }
