 package com.megginson.sloopsql;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.content.Context;
 import android.widget.Toast;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Static utility methods for the application.
  */
 public final class Util
 {
 
 	/**
 	 * Inflate a layout.
 	 *
 	 * @param context The context (e.g. activity) from which to grab an
 	 * inflater.
 	 * @param id The identifier of the XML layout to inflate.
 	 * @return The inflated view.
 	 */
 	public static View inflate(Context context, int id)
 	{
 		LayoutInflater inflater = LayoutInflater.from(context);
 		return inflater.inflate(id, null);
 	}
 
 	/**
 	 * Show a long toast pop-up message.
 	 *
 	 * @param context The context (e.g. activity) in which to show the toast.
 	 * @param message The string message to show.
 	 */
 	public static void toast(Context context, String message)
 	{
 		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
 	}
 
 	/**
 	 * Construct a stacktrace string from a throwable.
 	 *
 	 * Combine all the {@link StackTrace} objects in a throwable into a
 	 * single string, for debugging.
 	 *
 	 * @param t The throwable from which to extract the stacktrace.
 	 * @return A representation of the stacktrace as a single string.
 	 */
 	public static String makeStackTrace(Throwable t)
 	{
 		StringBuffer s = new StringBuffer(t.getMessage());
 		for (StackTraceElement e : t.getStackTrace())
 		{
 			s.append('\n');
 			s.append(e.toString());
 		}
 		return s.toString();
 	}
 
 	/**
 	 * Split a string into multiple SQL statements using ";" as a separator.
 	 */
 	public static List<String> splitSQL(String statementsText)
 	{
 		List<String> statementList = new ArrayList<String>();
 		StringBuffer statement = new StringBuffer();
 
 		for (int i = 0; i < statementsText.length(); i++)
 		{
 			char c = statementsText.charAt(i);
 			switch (c)
 			{
 				case ';':
 					String s = statement.toString().trim();
 					if (s.length() > 0)
 					{
 						statementList.add(s.toString());
 						statement.setLength(0);
 					}
 					break;
 				case '\'':
 				case '"':
 				case '`':
 					statement.append(c);
 					while (++i < statementsText.length())
 					{
 						char c1 = statementsText.charAt(i);
 						statement.append(c1);
 						if (c == c1)
 						{
 							break;
 						}
 					}
 					break;
 				default:
 					statement.append(c);
 					break;
 			}
 		}
 
 		// straggler without a semicolon?
 		String s = statement.toString().trim();
 		if (s.length() > 0)
 		{
 			statementList.add(s);
		}	
 
 		return statementList;
 	}
 
 }
