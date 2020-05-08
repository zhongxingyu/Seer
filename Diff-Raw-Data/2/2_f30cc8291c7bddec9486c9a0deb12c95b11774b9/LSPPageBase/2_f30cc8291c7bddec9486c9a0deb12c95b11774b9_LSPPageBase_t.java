 /*
  * Copyright (c) 2003-2005, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lsp;
 
 import java.io.*;
 import java.util.*;
 
 import javax.xml.transform.Result;
 import org.xml.sax.*;
 import org.xml.sax.helpers.AttributesImpl;
 
 import nu.staldal.util.*;
 
 import nu.staldal.lsp.expr.*;
 import nu.staldal.lsp.compile.*;
 import nu.staldal.lsp.compiledexpr.*;
 import nu.staldal.lsp.wrapper.*;
 
 
 public abstract class LSPPageBase implements LSPPage
 {
 	protected final String[] extLibsURLs;
 	protected final String[] extLibsClassNames;
 	protected final String[] compileDependentFiles;
 	protected final boolean compileDynamic;
 	protected final long timeCompiled;
 	protected final String pageName;
 	protected final String compiledVersionName;
 	protected final int compiledVersionNum;
     protected final Properties outputProperties;
     
 	
 	protected LSPPageBase(String[] extLibsURLs, String[] extLibsClassNames,
 		String[] compileDependentFiles, boolean compileDynamic, 
         long timeCompiled, String pageName, String versionName, int versionNum)
 	{
 		this.extLibsURLs = extLibsURLs;
 		this.extLibsClassNames = extLibsClassNames;
 		this.compileDependentFiles = compileDependentFiles;
 		this.compileDynamic = compileDynamic;
 		this.timeCompiled = timeCompiled;
 		this.pageName = pageName;
 		this.compiledVersionName = versionName;
 		this.compiledVersionNum = versionNum;
         this.outputProperties = new Properties();
 	}
     
     protected final void setOutputProperty(String key, String value)
     {
         outputProperties.setProperty(key, value);    
     }
 
     public final String[] getCompileDependentFiles()
 	{
 		return compileDependentFiles;	
 	}
 
     public final boolean isCompileDynamic()
 	{
 		return compileDynamic;	
 	}
 
     public final long getTimeCompiled()
 	{	
 		return timeCompiled;
 	}	
 	
     public final String getPageName()
 	{	
 		return pageName;
 	}
     
 	public final Properties getOutputProperties()
     {
         return outputProperties;
     }        
 	
 
     public final void execute(ContentHandler sax, Map params, Object extContext)
         throws SAXException
 	{
         if (compiledVersionNum > LSPPage.LSP_VERSION_NUM)
         {
             throw new LSPException(
                 "LSP version mismatch: compiled="+compiledVersionName
                 + ", runtime="+LSPPage.LSP_VERSION_NAME
                 + ". Please update your LSP runtime.");
         }
         else if (compiledVersionNum < LSPPage.LSP_VERSION_NUM)
         {
             throw new LSPException(
                 "LSP version mismatch: compiled="+compiledVersionName
                 + ", runtime="+LSPPage.LSP_VERSION_NAME
                 + ". Please recompile this LSP page.");
         }
         
         Environment env = new Environment(params);
 
 		Map extLibs = new HashMap();
 		
 		for (int i = 0; i < extLibsURLs.length; i++)
 		{
 			String nsURI = extLibsURLs[i];
 			String className = extLibsClassNames[i];
 			
 			LSPExtLib extLib = lookupExtensionHandler(extLibs, nsURI, className);			
 			
 			extLib.startPage(extContext, pageName);
 		}
 
 		try {
 			_execute(sax, env, extLibs, sax, new AttributesImpl());
 		}
 		catch (IllegalArgumentException e)
 		{
 			throw new SAXException(e);	
 		}
 		
 		for (int i = 0; i < extLibsURLs.length; i++)
 		{
 			String nsURI = extLibsURLs[i];
 			String className = extLibsClassNames[i];
 			
 			LSPExtLib extLib = (LSPExtLib)extLibs.get(className);			
 			
 			extLib.endPage();
 		}
 	}
 
 
 	protected static final LSPExtLib lookupExtensionHandler(Map extLibs, String nsURI, String className)
 		throws SAXException
 	{
 		try {
 			LSPExtLib extLib = (LSPExtLib)extLibs.get(className);
 			if (extLib == null)
 			{
 				Class extClass = Class.forName(className);
 				extLib = (LSPExtLib)extClass.newInstance();
 				extLib.init(nsURI);
 				extLibs.put(className, extLib);
 			}
 			return extLib;
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw new LSPException("Extension class not found: " 
 				+ className);
 		}
 		catch (InstantiationException e)
 		{
 			throw new LSPException("Unable to instantiate extension class: " 
 				+ e.getMessage());
 		}
 		catch (IllegalAccessException e)
 		{
 			throw new LSPException("Unable to instantiate extension class: " 
 				+ e.getMessage());
 		}
 		catch (ClassCastException e)
 		{
 			throw new LSPException("Extension class " + className 
 				+ " does not implement the required interface");
 		}
 	}
 
 
 	protected static Object convertObjectToLSP(Object value, String name)
 		throws LSPException
 	{
 		if (value == null)
 			throw new LSPException(name + ": LSP cannot handle null objects");
 		else if (value == Void.TYPE)
 			return value;
 		else if (value instanceof String)
 			return value;
 		else if (value instanceof Boolean)
 			return value;
 		else if (value instanceof Number)
 			return value;
 		else if (value instanceof Collection) 
 			return value;
 		else if (value instanceof Map) 
 			return value;
 		else if (value instanceof int[])
         {
 			return new IntArrayCollection((int[])value);
         }
 		else if (value instanceof short[])
         {
 			return new ShortArrayCollection((short[])value);
         }
 		else if (value instanceof long[])
         {
 			return new LongArrayCollection((long[])value);
         }
 		else if (value instanceof float[])
         {
 			return new FloatArrayCollection((float[])value);
         }
 		else if (value instanceof double[])
         {
 			return new DoubleArrayCollection((double[])value);
         }
 		else if (value instanceof boolean[])
         {
 			return new BooleanArrayCollection((boolean[])value);
         }
 		else if (value instanceof char[])
         {
 			return new String((char[])value);
         }
 		else if (value instanceof byte[])
 		{
 			try {
 				return new String((byte[])value, "ISO-8859-1");
 			}
 			catch (UnsupportedEncodingException e)
 			{
 				throw new Error("JVM doesn't support ISO-8859-1 encoding");	
 			}
 		}
 		else if (value instanceof Object[])
         {
             Object[] arr = (Object[])value;
             if (arr.length == 0)
                 return Collections.EMPTY_LIST;
             else
                 return Arrays.asList(arr);
         }
         else if (value instanceof java.sql.ResultSet)
         {
             return new LSPResultSetTupleList((java.sql.ResultSet)value);    
         }
         else if (value instanceof Enum)
         {
             return value.toString();    
         }
         else
         {
             try {
                 return new org.apache.commons.collections.BeanMap(value);
             }
             catch (NoClassDefFoundError e)
             {
                 throw new LSPException(
 				    name + ": LSP cannot handle objects of type "
                     + value.getClass().getName());
             }
         }
 	}
 
 	
 	protected static String convertToString(Object value) throws LSPException
 	{
 		if (value instanceof String)
 		{
 			return (String)value;
 		}
         else if (value == Void.TYPE)
         {
             return "";
         }
 		else if (value instanceof Number)
 		{
 			double d = ((Number)value).doubleValue();
 			if (d == 0)
 				return "0";
 			else if (d == Math.rint(d))
 				return Long.toString(Math.round(d));
 			else
 				return Double.toString(d);
 		}
 		else if (value instanceof Boolean)
 		{
 			return value.toString();
 		}
 		else
 		{
 			throw new LSPException(
 				"Convert to String not implemented for type "
 				+ value.getClass().getName());
 		}
 	}
 
 	
 	protected static double convertToNumber(Object value) throws LSPException
 	{
 		if (value instanceof Number)
 		{
 			return ((Number)value).doubleValue();
 		}
         else if (value == Void.TYPE)
         {
             return 0.0d;
         }
 		else if (value instanceof Boolean)
 		{
 			return ((Boolean)value).booleanValue() ? 1.0d : 0.0d;
 		}
 		else if (value instanceof String)
 		{
 			try {
 				return Double.valueOf((String)value).doubleValue();
 			}
 			catch (NumberFormatException e)
 			{
 				return Double.NaN;
 			}
 		}
 		else
 		{
 			throw new LSPException(
 				"Convert to Number not implemented for type "
 				+ value.getClass().getName());
 		}
 	}
 
 
 	protected static boolean convertToBoolean(Object value) throws LSPException
 	{
 		if (value instanceof Boolean)
 		{
 			return ((Boolean)value).booleanValue();
 		}
         else if (value == Void.TYPE)
         {
             return false;
         }
 		else if (value instanceof Number)
 		{
 			double d = ((Number)value).doubleValue();
 			return !((d == 0) || Double.isNaN(d));
 		}
 		else if (value instanceof String)
 		{
 			return ((String)value).length() > 0;
 		}
 		else if (value instanceof Collection)
 		{
 			return !(((Collection)value).isEmpty());
 		}
 		else
 		{
 			throw new LSPException(
 				"Convert to Boolean not implemented for type "
 				+ value.getClass().getName());
 		}
 	}
 
 
 	protected static boolean convertToBooleanAcceptNull(Object value) throws LSPException
 	{
 		if (value instanceof Boolean)
 		{
 			return ((Boolean)value).booleanValue();
 		}
         else if (value == Void.TYPE)
         {
             return false;
         }
 		else if (value instanceof Number)
 		{
 			double d = ((Number)value).doubleValue();
 			return !((d == 0) || Double.isNaN(d));
 		}
 		else if (value instanceof String)
 		{
 			return ((String)value).length() > 0;
 		}
 		else if (value instanceof Collection)
 		{
 			return !(((Collection)value).isEmpty());
 		}
 		else if (value instanceof FullMap)
 		{
 			return false;
 		}
 		else if (value instanceof Map)
 		{
 			return true;
 		}
 		else
 		{
 			throw new LSPException(
 				"Convert to Boolean not implemented for type "
 				+ value.getClass().getName());
 		}
 	}
 
 
 	protected static Collection convertToList(Object value) throws LSPException
 	{
 		if (value instanceof Collection) 
 			return (Collection)value;
         else if (value == Void.TYPE)
             return Collections.EMPTY_LIST;
 		else           
 			throw new LSPException(
 				"Convert to list not implemented for type "
 				+ value.getClass().getName());
 	}
 
 
 	protected static Map convertToTuple(Object value) throws LSPException
 	{
 		if (value instanceof Map) 
 			return (Map)value;
         else if (value == Void.TYPE)
             return FullMap.getInstance();
 		else
 			throw new LSPException(
 				"Convert to tuple not implemented for type "
 				+ value.getClass().getName());
 	}
 
 
 	protected static void outputStringWithoutCR(ContentHandler sax, String s,
             boolean disableOutputEscaping)
 		throws SAXException
 	{
 		char[] cb = new char[s.length()];
 		
 		int ci = 0;
 		for (int si = 0; si<s.length(); si++)
 		{
 			char sc = s.charAt(si);
 			if (sc == '\r')
 			{
 				if (si<s.length() && (s.charAt(si+1) == '\n'))
 					; // convert CR+LF to LF
 				else
 					cb[ci++] = '\n'; // convert alone CR to LF
 			}
 			else
 			{
 				cb[ci++] = sc;
 			}
 		}
         if (disableOutputEscaping)
             sax.processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");    		
 		sax.characters(cb, 0, ci);
         if (disableOutputEscaping)
             sax.processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");    		
 	}
 	
 	
 	protected static Double doubleValueOf(double d)
 	{
 		return new Double(d);	
 	}
 		
 
 	protected static Object getElementFromTuple(Map tuple, String key)
 		throws LSPException
 	{
 		Object o = tuple.get(key);
 		
         if (o == null)
         {
             throw new LSPException(
                 "Element \'" + key + "\' not found in tuple");
         }
         else
         {
             return convertObjectToLSP(o, "."+key);
         }
 	}
 
 	
 	protected static Object getElementFromTupleAcceptNull(Map tuple, String key)
 		throws LSPException
 	{
 		Object o = tuple.get(key);
 		
         if (o == null)
         {
             return Void.TYPE;
         }
         else
         {
             return convertObjectToLSP(o, "."+key);
         }
 	}
 
 
 	protected static Object getVariableValue(Environment env, String varName)
 		throws LSPException
 	{
 		Object o = env.lookup(varName);
 		
 		if (o == null)
         {
             throw new LSPException(
                 "Attempt to reference unbound variable: " + varName);
         }
 		else
         {
 			return convertObjectToLSP(o, varName);
         }
 	}
     
     
 	protected static Object getVariableValueAcceptNull(Environment env, String varName)
 		throws LSPException
 	{
 		Object o = env.lookup(varName);
 		
 		if (o == null)
         {
             return Void.TYPE;
         }
 		else
         {
 			return convertObjectToLSP(o, varName);
         }
 	}
 				
 
 	protected static boolean compareEqual(Object left, Object right)
 		throws LSPException
 	{		
 		if ((left instanceof Boolean) || (right instanceof Boolean))
 		{
 			return convertToBoolean(left) == convertToBoolean(right);
 		}
 		else if ((left instanceof Number) || (right instanceof Number))
 		{
 		 	return convertToNumber(left) == convertToNumber(right);
 		}
 		else
 		{
 		 	return convertToString(left).equals(convertToString(right));
 		}
 	}
 	
 		
 	protected static boolean fnContains(String a, String b)
 	{
 		return a.indexOf(b) > -1;
 	}
 		
 		
 	protected static String fnSubstringBefore(String a, String b)
 	{
 		int index = a.indexOf(b);
 
 		if (index < 0)
 			return "";
 		else
 			return a.substring(0, index);		
 	}
 		
 
 	protected static String fnSubstringAfter(String a, String b)
 	{
 		int index = a.indexOf(b);
 
 		if (index < 0)
 			return "";
 		else
 			return a.substring(index+1);
 	}
 	
 
 	protected static String fnSubstring(String a, double bd)
 	{
 		if (Double.isNaN(bd)) return "";
 				
 		int b = (int)Math.round(bd);
 		int c = a.length()+1;
 
 		if (b > a.length()) b = a.length();
 		if (c < 1) return "";
 		if (c > (a.length()-b+1)) c = a.length()-b+1;
 
 		return a.substring((b-1 < 0) ? 0 : (b-1), b-1+c);
 	}
 
 
 	protected static String fnSubstring(String a, double bd, double cd)
 	{
 		if (Double.isNaN(bd) || Double.isNaN(cd)) return "";
 
 		int b = (int)Math.round(bd);
 		int c = (int)Math.round(cd);
 
 		if (b > a.length()) b = a.length();
 		if (c < 1) return "";
 		if (c > (a.length()-b+1)) c = a.length()-b+1;
 
 		return a.substring((b-1 < 0) ? 0 : (b-1), b-1+c);
 	}
 
 
 	protected static String fnNormalizeSpace(String a)
 	{
 		String x = a.trim();
 
 		StringBuffer sb = new StringBuffer(x.length());
 		boolean inSpace = false;
 		for (int i = 0; i<x.length(); i++)
 		{
 			char c = x.charAt(i);
 			if (c > ' ')
 			{
 				inSpace = false;
 				sb.append(c);
 			}
 			else
 			{
 				if (!inSpace)
 				{
 					sb.append(' ');
 					inSpace = true;
 				}
 			}
 		}
 		return sb.toString();
 	}
 
 	
 	protected static String fnTranslate(String a, String b, String c)
 	{	
 		StringBuffer sb = new StringBuffer(a.length());
 		for (int i = 0; i<a.length(); i++)
 		{
 			char ch = a.charAt(i);
 			int index = b.indexOf(ch);
 			if (index < 0)
				sb.append(ch);
 			else if (index >= c.length())
 				;
 			else
 				sb.append(c.charAt(index));
 		}
 		return sb.toString();
 	}
 	
 	
 	protected static double fnRound(double a)
 	{	
 		return Math.floor(a + 0.5d);
 	}
 	
 
 	protected static Collection fnSeq(double start, double end, double step)
 	{
 		ArrayList vec = new ArrayList((int)((end-start)/step));
 		for (; start <= end; start+=step)
 		 	vec.add(new Double(start));
 		
 		return vec;
 	}
 
 		
 	protected abstract void _execute(
 			ContentHandler sax, Environment env,
 			Map extLibs, ContentHandler _sax, AttributesImpl attrs)
 		throws SAXException, IllegalArgumentException;	
 }
 
