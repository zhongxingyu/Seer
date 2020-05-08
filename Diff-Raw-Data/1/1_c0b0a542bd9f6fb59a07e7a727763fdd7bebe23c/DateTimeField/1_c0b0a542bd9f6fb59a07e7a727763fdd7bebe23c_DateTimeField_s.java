 package com.xaf.form.field;
 
 import java.io.*;
 import java.util.*;
 import java.text.*;
 
 import org.w3c.dom.*;
 import com.xaf.form.*;
 
 public class DateTimeField extends TextField
 {
 	static public final long FLDFLAG_FUTUREONLY = TextField.FLDFLAG_STARTCUSTOM;
 	static public final long FLDFLAG_PASTONLY   = FLDFLAG_FUTUREONLY * 2;
 
 	static public final int DTTYPE_DATEONLY = 0;
 	static public final int DTTYPE_TIMEONLY = 1;
 	static public final int DTTYPE_BOTH     = 2;
 
 	static public String[] formats = new String[] { "MM/dd/yyyy", "HH:mm", "MM/dd/yyyy HH:mm" };
 
 	private int dataType;
 	private SimpleDateFormat format;
     private SimpleDateFormat sqlFormat;
 	private Date preDate = null;
 	private Date postDate = null;
 
 	public DateTimeField()
 	{
 		super();
 		setDataType(DTTYPE_DATEONLY);
 	}
 
 	public DateTimeField(String aName, String aCaption, int aType)
 	{
 		super(aName, aCaption);
 		setDataType(aType);
 	}
 
 	public final int getDataType() { return dataType; }
 	public void setDataType(int value)
 	{
 		dataType = value;
 		format = new SimpleDateFormat(formats[dataType]);
         sqlFormat = new SimpleDateFormat(formats[0]);
 		setSize(formats[dataType].length());
 		setMaxLength(getSize());
 	}
 
 	public final SimpleDateFormat getFormat() { return format; }
 
 	public final Date getPreDate() { return preDate; }
 	public void setPreDate(Date value) { preDate = value; }
 
 	public final Date getPostDate() { return postDate; }
 	public void setPostDate(Date value) { postDate = value; }
 
 	public void setPrePostDate(Date low, Date high)
 	{
 		preDate = low;
 		postDate = high;
 	}
 
 	public Object getValueForSqlBindParam(String value)
 	{
         try
         {
             if (dataType == DTTYPE_TIMEONLY)
                 return new String(this.formatTimeValue(value));
             else
                 return new java.sql.Date(format.parse(value).getTime());
         }
         catch(ParseException e)
         {
             throw new RuntimeException(e.toString());
         }
 	}
 
      /**
      * Strips the ":" from the Time field. Must only be used when the
      * DateTime field contains only time.
      *
      * @param value Time field string
      * @returns String formatted Time string
      */
 	private String formatTimeValue(String value)
 	{
         if(value == null)
             return value;
 
         StringBuffer timeValueStr = new StringBuffer();
         StringTokenizer tokens = new StringTokenizer(value, ":");
         while(tokens.hasMoreTokens())
             timeValueStr.append(tokens.nextToken());
 
         return timeValueStr.toString();
 	}
 
 	public void importFromXml(Element elem)
 	{
 		super.importFromXml(elem);
 
 		String nodeName = elem.getNodeName();
 		if(nodeName.equals("field.datetime"))
 			setDataType(DTTYPE_BOTH);
 		else if(nodeName.equals("field.time"))
 			setDataType(DTTYPE_TIMEONLY);
 
 		if(elem.getAttribute("future-only").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_REQUIRED);
 
 		if(elem.getAttribute("past-only").equalsIgnoreCase("yes"))
 			setFlag(DialogField.FLDFLAG_READONLY);
 	}
 
 	public boolean isValid(DialogContext dc)
 	{
 		boolean textValid = super.isValid(dc);
 		if(! textValid)	return false;
 
 		String strValue = dc.getValue(this);
 		if(! isRequired(dc) && (strValue == null || strValue.length() == 0))
 			return true;
 
 		Date value = null;
 		try
 		{
 			value = format.parse(strValue);
 		}
 		catch(Exception e)
 		{
 			invalidate(dc, "'" + strValue + "' is not valid (format is "+ formats[dataType] +").");
 			return false;
 		}
 
 		Date now = new Date();
 		long flags = getFlags();
 		if((flags & FLDFLAG_FUTUREONLY) != 0 && value.before(now))
 		{
 			invalidate(dc, getCaption(dc) + " must be in the future.");
 			return false;
 		}
 		if((flags & FLDFLAG_PASTONLY) != 0 && value.after(now))
 		{
 			invalidate(dc, getCaption(dc) + " must be in the past.");
 			return false;
 		}
 		if(preDate != null && value.after(preDate))
 		{
 			invalidate(dc, getCaption(dc) + " must be after " + preDate + ".");
 			return false;
 		}
 		if(postDate != null && value.before(postDate))
 		{
 			invalidate(dc, getCaption(dc) + " must be before " + postDate + ".");
 			return false;
 		}
 
 		return true;
 	}
 }
