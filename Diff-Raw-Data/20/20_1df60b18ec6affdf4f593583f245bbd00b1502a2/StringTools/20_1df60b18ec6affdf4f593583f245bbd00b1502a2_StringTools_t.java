 package cm.generic;
 
 import java.lang.*;
 import java.net.*;
 import java.util.*;
 
 /**
  * @author andruid
  *
  * Methods for manipulating </ode>String</code> and <code>StringBuffer</code>s.
  */
 public class StringTools
 extends Debug
 {
    static final String[]	oneDotDomainStrings = 
    {
       "com", "edu", "gov", "org", "net",
    };
   static final HashMap	oneDotDomains	= 
      Generic.buildHashMapFromStrings(oneDotDomainStrings);

   public static final String	EMPTY_STRING	= "";
   
 /**
  * Changes the StringBuffer to lower case, in place, without any new storage
  * allocation.
  */
    public static final void toLowerCase(StringBuffer buffer)
    {
       int length	= buffer.length();
       for (int i=0; i<length; i++)
       {
 	 char c		= buffer.charAt(i);
 //	 if (Character.isUpperCase(c))
 	 // A = 0x41, Z = 0x5A; a = 0x61, z = 0x7A
 	 if ((c >='A') && (c <= 'Z'))
 	 {
 	    c	       += 0x20;
 	    buffer.setCharAt(i, c);
 	 }
       }
    }
    public static final boolean sameDomain(URL url1, URL url2)
    {
       return domain(url1).equals(domain(url2));
    }
    public static final String domain(URL url)
    {
       return domain(url.getHost());
    }
 /**
  * Useful for finding common domains.
  */
    public static final String domain(String urlString)
    {
      if ((urlString == null) || (urlString.length() == 0))
	 return null;
       int end	= urlString.length() - 1;
       int domainStartingDot	= 0;
       boolean foundFirstDot	= false;
       boolean foundSecondDot	= false;
       boolean international	= false;
       String result	= urlString;
       for (int i=end; i>0; i--)
       {
 	 if (urlString.charAt(i) == '.')
 	 {
 	    if (foundFirstDot)
 	    {
 	       if (international && !foundSecondDot)
 	       {
 		  foundSecondDot	= true;
 	       }
 	       else
 	       {
 		  domainStartingDot	= i + 1;
 		  break;
 	       }
 	    }
 	    else
 	    {
 	       foundFirstDot	= true;
 	       String suffix	= urlString.substring(i+1);
 	       international	= !oneDotDomains.containsKey(suffix);
 	    }
 	 }
       }
       return urlString.substring(domainStartingDot, end + 1);
    }
    
 /**
  * Use this method to efficiently get a <code>String</code> from a
  * <code>StringBuffer</code> on those occassions when you plan to keep
  * using the <code>StringBuffer</code>, and want an efficiently made copy.
  * In those cases, <i>much</i> better than 
  * <code>new String(StringBuffer)</code>
  */
    public static final String toString(StringBuffer buffer)
    {
       return buffer.substring(0);
    }
    public static final boolean contains(String in, String toMatch)
    {
       return (in == null) ? false : in.indexOf(toMatch) != -1;
    }
 
 /**
  * Very efficiently forms String representation of url (better than 
  * <code>URL.toExternalForm()</code>). Doesn't include query or anchor.
  */
    public static final String pageString(URL u)
    {
       String protocol	= u.getProtocol();
       String authority	= u.getAuthority(); // authority is host:port
       String path	= u.getPath();	    // doesn't include query
 
       int pathLength	= (path == null) ? 0 : path.length();
       // pre-compute length of StringBuffer
       int length =0;
       
       try
       {
 	 length	=
 	 protocol.length() + 3 /* :// */ + authority.length() + pathLength;
    } catch (Exception e)
    {
       Debug.println("protocol="+protocol+" authority="+authority+
 		     u.toExternalForm());
       e.printStackTrace();
    }
       
       StringBuffer result = new StringBuffer(length);
       result.append(protocol).append("://").append(authority).append(path);
 
       return new String(result);
    }
    public static void main(String[] args)
    {
       for (int i=0; i<args.length; i++)
 	 println(pageString(Generic.getURL(args[i], "oops " + i)));
 	 
    }
 }
