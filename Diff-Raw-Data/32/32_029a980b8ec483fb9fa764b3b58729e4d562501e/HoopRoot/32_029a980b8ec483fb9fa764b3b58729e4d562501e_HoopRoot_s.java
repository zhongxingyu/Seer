 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.base;
 
 import java.lang.reflect.Field;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.JOptionPane;
 
 import org.jdom.Element;
 
 import edu.cmu.cs.in.base.HoopFilterBase;
 import edu.cmu.cs.in.base.io.HoopFileManager;
 
 public class HoopRoot extends HoopDataType
 {
 	private SimpleDateFormat df;	
 	private String instanceName="Undefined";
 	private String className="HoopBase";
 	private String classType="Unknown"; // Used for serialization and should be a base type
 	private String errorString="";
 	
 	private ArrayList<HoopFilterBase> filters=null;
 		
 	/**
 	 *
 	 */
 	public HoopRoot () 
 	{
 		setClassName ("HoopRoot");
		debug ("HoopRoot ()");
 		
 		df=new SimpleDateFormat ("HH:mm:ss.SSS");
 		filters=new ArrayList<HoopFilterBase> ();
 	}
 	/**
 	 *
 	 */
 	public void setClassName (String aName)
 	{
 		className=aName;
 	}
 	/**
 	 *
 	 */
 	public String getClassName ()
 	{
 		return (className);
 	}
 	/**
 	 *
 	 */
    public void setClassType (String aName)
    {
    	classType=aName;
    }
 	/**
 	 *
 	 */
    	public String getClassType ()
    	{
    		return (classType);
    	} 	
 	/**
 	 * 
 	 */	
 	public void setErrorString(String errorString) 
 	{
 		this.errorString = errorString;
 	}
 	/**
 	 * 
 	 */	
 	public String getErrorString() 
 	{
 		return errorString;
 	}   
 	/**
 	 *
 	 */    
 	public String getCurrentDate ()
 	{
 		return (df.format(new Date()));
 	}
 	/**
 	 *
 	 */
 	public ArrayList<HoopFilterBase> getFilters ()
 	{
 		return (filters);
 	}
 	/**
 	 *
 	 */
 	public void addFilter (HoopFilterBase aFilter)
 	{
 		filters.add(aFilter);
 	}
 	/**
 	 *
 	 */
 	public static String generateFileTimestamp ()
 	{
 		SimpleDateFormat dfStatic=new SimpleDateFormat ("HH-mm-ss-SSS");
 		return (dfStatic.format(new Date()));
 	}
 	/**
 	 *
 	 */		
 	public static void debug(String aClass,String s) 
 	{
 		if (HoopLink.nodebug==true)
 			return;
 		
 		StringBuffer buffer=new StringBuffer ();
 					
 		buffer.append(String.format ("[%s] [%d] <"+aClass+"> %s\n", generateFileTimestamp (), ++HoopLink.debugLine, s));
 
 		System.out.print (buffer.toString());
 		
 		if (HoopLink.console!=null)
 		{
 			HoopLink.console.appendString (buffer.toString ());		
 		}
 	}    
 	/**
 	 *
 	 */		
 	public void debug(String s) 
 	{
 		if (HoopLink.nodebug==true)
 			return;		
 		
 		StringBuffer buffer=new StringBuffer ();
 
 		buffer.append (String.format ("[%s] [%d] <"+className+"> %s\n", generateFileTimestamp (), ++HoopLink.debugLine, s));
 
 		System.out.print (buffer.toString());
 		
 		if (HoopLink.console!=null)
 		{
 			HoopLink.console.appendString (buffer.toString ());
 		}
 	}  
 	/**
 	 *
 	 */		
 	public void dumpDebugData(String s) 
 	{
 		debug ("dumpDebugData ()");
 		
 		HoopFileManager fManager=new HoopFileManager ();
 		fManager.saveContents("C://temp/debug.txt",s);
 	}
 	/**
 	 *
 	 */	
 	public String getName() 
 	{
 		return instanceName;
 	}
 	/**
 	 *
 	 */	
 	public void setName(String instanceName) 
 	{
 		this.instanceName = instanceName;
 	}	
 	/**
 	 *
 	 */	
 	public String getInstanceName() 
 	{
 		return instanceName;
 	}
 	/**
 	 *
 	 */	
 	public void setInstanceName(String instanceName) 
 	{
 		this.instanceName = instanceName;
 	}
 	/**
 	 * 
 	 */
 	protected Element getClassElement ()
 	{
 		Element classElement=new Element (getClassName ());
 				
 		return (classElement);
 	}
 	/**
 	 * 
 	 */
 	protected Element getClassElement (String aName)
 	{
 		Element classElement=new Element (getClassName ());
 		
 		classElement.setAttribute("name",aName);
 		
 		return (classElement);
 	}	
 	/**
 	*	
 	*/		
 	protected String getClassOpen ()
 	{
 		return ("<"+getClassType ()+">");
 	}
 	/**
 	*	
 	*/		
 	protected String getClassOpen (String aName)
 	{
 		return ("<"+getClassType ()+" name=\""+aName+"\">");
 	}	
 	/**
 	*	
 	*/		
 	protected String getClassClose ()
 	{
 		return ("</"+getClassType ()+">");
 	}
 	/**
 	 * 
 	 */
 	public static void alert (String aMessage)
 	{
 		JOptionPane.showMessageDialog(HoopLink.mainFrame,aMessage);
 	}
 	/**
 	 * 
 	 */
 	public void printFieldNames()		
 	{	    
 		debug ("printFieldNames ()");
 		
 		Field [] fields=this.getClass().getFields();
 		
 		debug ("Found " + fields.length + " fields");
 		
 	    for (int i=0;i<fields.length;i++) 
 	    {
 	    	Field aField=fields [i];
 	    	debug ("["+i+"] " + aField.getName());
 	    }
 	}
 	/**
 	 * 
 	 */
 	public ArrayList <Field> getFields()		
 	{	    
 		debug ("getFields ()");
 		
 		ArrayList<Field> fList=new ArrayList<Field> ();
 		
 		Field [] fields=this.getClass().getFields();
 		
 		debug ("Found " + fields.length + " fields");
 		
 	    for (int i=0;i<fields.length;i++) 
 	    {
 	    	Field aField=fields [i];
 	    	debug ("["+i+"] " + aField.getName());
 	    	fList.add(aField);
 	    }
 	    
 	    return (fList);
 	}
 	/**
 	 * Field.equals (): Compares this Field against the specified object. Returns true 
 	 * if the objects are the same. Two Field objects are the same if they were declared 
 	 * by the same class and have the same name and type.  
 	 */
 	public String getFieldName(Object aReference)
 	{	    
 		debug ("getFieldName ("+aReference.getClass().getSimpleName()+")");
 				
 		Field [] fields=this.getClass().getFields();
 		
 		debug ("Found " + fields.length + " fields");
 		
 	    for (int i=0;i<fields.length;i++) 
 	    {
 	    	Field aField=fields [i];
 	    
 	    	/*
 	    	try 
 	    	{
 				if (aField.get (aReference)!=null)
 				{
 					debug ("["+i+"] " + aField.getName() + " ref hash: " + aReference.hashCode() + " field hash: " + aField.hashCode());
 					return (aField.getName());
 				}
 			} 
 	    	catch (IllegalArgumentException e) 
 	    	{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 	    	catch (IllegalAccessException e) 
 	    	{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			*/
 	    	
 	    	debug ("["+i+"] " + aField.getName() + " ref hash: " + aReference.hashCode() + " field hash: " + aField.hashCode());
 	    
 	    	if (aField.equals(aReference)==true)
 	    		return (aField.getName());	    	
 	    }
 	    
 	    return (null);
 	}	
 }
