 package cm.generic;
 
 import java.awt.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 /**
  * @author madhur+andruid
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class Generic 
 {
 /**
  * Get a boolean parameter from the runtime environment. If the value is the
  * string <code>true</code> or <code>yes</code>, the result is 
  * <code>true</code>; else false.
  * 
  * @param	name	The name of the parameter's key.
  */
    public boolean parameterBool(String name)
    {
       String value	= parameter(name);
       boolean result	= value != null;
       if (result)
 	 result		=  value.equalsIgnoreCase("true") ||
 	    value.equalsIgnoreCase("yes") || (value.equals("1"));
       return result;
    }
 /**
  * Get an integer parameter from the runtime environment. The default is 0.
  * 
  * @param	name	The name of the parameter's key.
  */
    public int parameterInt(String paramName)
    { return parameterInt(paramName, 0); }
    
 /**
  * Get an integer parameter from the runtime environment. 
  * 
  * @param	name		The name of the parameter's key.
  * @param	defaultValue	Default integer value, in case param is 
  *				unspecified in the runtime env.
  */
    public int parameterInt(String paramName, int defaultValue)
    {
       String paramValue	= parameter(paramName);
       int result	= defaultValue;
       if (paramValue != null)
 	 try
 	 {
 	    result	= Integer.parseInt(paramValue);
 	 } catch (NumberFormatException e)
 	 {
 	    Debug.println("bad number format: "+paramName+"="+paramValue);
 	 }
       return result;
    }
 /**
  * Get a float parameter from the runtime environment.
  * 
  * @param	name		The name of the parameter's key.
  * @param	defaultValue	Default floating point value, in case param is 
  *				unspecified in the runtime env.
  */
    public static float parameterFloat(String paramName, float defaultValue)
    {
       String paramValue	= parameter(paramName);
       float result	= defaultValue;
       if (paramValue != null)
       {
 	 float parsedValue	= Generic.parseFloat(paramValue);
 	 if (!Float.isNaN(parsedValue))
 	    result	= parsedValue;
       }
       return result;
    }
    public static final String parameter(String paramName)
    {
 
       return Environment.the.get().parameter(paramName);
    }
 /**
  * Turn a string into a float.
  * 
  * @return	the float, if the String is cool; else Float.NaN
  */
    static public float parseFloat(String floatString)
    {
       float result;
       try
       {
 	 Double fObj	= Double.valueOf(floatString);
 	 result		= fObj.floatValue();
       } catch (NumberFormatException e)
       {
 	 result		= Float.NaN;
       }
       return result;
    }
 
 
 	/**
  * Sleep easily, ignoring (unlikely) <code>InterruptedException</code>s.
  */
    public static final void sleep(int time)
    {
       try
       {
 	 Thread.sleep(time);
       }
       catch (InterruptedException e)
       {
 //	 System.out.println("Sleep was interrupted -- clearing if possible.\n"
 //			    + e);
 	 // in jdk 1.1x clears the interrupt
 	 // !!! (undocumented) !!! (see Thread src)
 	 Thread.interrupted();	
       }
    }
 
 /**
  * Form a URL easily, without perhaps throwing an exception.
  */
    public static URL getURL(URL base, String path, String error)
    {
       // ??? might want to allow this default behaviour ???
       if (path == null)
 	 return null;
       try 
       {
 		//System.err.println("\nGENERIC - base, path, error = \n" + base + "\n" + path);
 		URL newURL = new URL(base,path);
 		//System.err.println("\nNEW URL = " + newURL);
 	 return newURL;
       } catch (MalformedURLException e) 
       {
 	 if (error != null)
 	    throw new Error(e + "\n" + error + " " + base + " -> " + path);
 	 return null;
       }
    }
 
 
 	/**
  * Form a URL easily, without perhaps throwing an exception.
  */
    public static URL getURL(String path, String error)
    {
       // ??? might want to allow this default behaviour ???
       if (path == null)
 	 return null;
       try 
       {
 	 	// System.err.println("\nGENERIC - path, error = \n" +  path + "\n" + error);
 		URL newURL = new URL(path);
 		// System.err.println("\nNEW URL = " + newURL);
 	
 	 return newURL;
       } catch (MalformedURLException e) 
       {
 	 throw new Error(e + "\n" + error + " " + path);
       }
    }
 
 /**
  * Find the path to system files.
  */
    public static URL systemPath(String relativePath)
    {
      return Environment.the.get().docRelativeURL(relativePath);
    }
 /**
  * @return	The version of Java we're using (but not the specific release),
  *		as in 1.2, 1.3, 1.4,...
  */
    public static float javaVersion()
    {
       return Environment.the.javaVersion();
    }
    public static final void propogateValues(Rectangle src, Rectangle dest)
    {
       dest.x		= src.x;
       dest.y		= src.y;
       dest.width	= src.width;
       dest.height	= src.height;
    }
 
 /**
  * Set the priority of the current thread.
  */
    static final public void setPriority(int priority)
    {
       Thread.currentThread().setPriority(priority);
    }
 
    public static final boolean contains(String in, String toMatch)
    {
       return StringTools.contains(in, toMatch);
    }
 
    //////////////////////////////////////////////////////////////
    public static String round(float f, int powerOfTen)
    {
       int factor;
       switch (powerOfTen)
       {
       case 0:
 	 factor	= 1;
 	 break;
       case 1:
 	 factor	= 10;
 	 break;
       case 2:
 	 factor	= 100;
 	 break;
       case 3:
 	 factor	= 1000;
 	 break;
       default:
 	 factor	= 10000;
 	 break;
       }
       return Float.toString(((float) Math.round(f * factor)) / factor);
    }
    ///////////////////////////////////////////////////////////////////////
 }
