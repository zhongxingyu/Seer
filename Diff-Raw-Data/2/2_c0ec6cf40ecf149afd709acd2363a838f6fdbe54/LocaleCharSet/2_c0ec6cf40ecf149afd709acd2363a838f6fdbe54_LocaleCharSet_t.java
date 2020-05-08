 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Locale;
 import java.util.Properties;
 
 import org.wings.ClasspathResource;
 
 
 /**
  * Map {@link java.util.Locale} to html/iso character set.
 * @author <a href="mailto:andre@lison.de">Andre Lison</a>
  */
 public class LocaleCharSet
 {
     
     private static LocaleCharSet fInstance = null;
     private Properties fCharSet;
     
 	protected LocaleCharSet()
 		throws IOException
 	{
 	    fCharSet = new Properties();
 		fCharSet.load(
 			this.getClass().
 			getClassLoader().
 			getResourceAsStream("org/wings/util/charset.properties"));
 	}
 	
 	/**
 	 * Get a instance of LocaleCharSet.
 	 * @throws IOException error while loading charset.properties
 	 * 	from classpath
 	 */
 	public static LocaleCharSet getInstance()
 		throws IOException
 	{
 	    if (fInstance == null)
 	    {
 	        fInstance = new LocaleCharSet();
 	    }
 	    return fInstance;
 	}
 	
 	/**
 	 * Try to find a matching character set for this locale.
 	 * @return if found the charset, "iso-8859-1" otherwise
 	 */
 	public String getCharSet(Locale aLocale)
 	{
 	    String cs = null;
 	    //System.out.println("Getting charset for locale " + aLocale);
 	    
 	    cs = fCharSet.getProperty(
 	    	aLocale.getCountry()+"_"+aLocale.getLanguage());
 	    if (cs == null)
 	    {
 	        cs = fCharSet.getProperty(
 	    		aLocale.getCountry());
 	    }
 	    if (cs == null)
 	    {
 	        cs = fCharSet.getProperty(
 	    		aLocale.getLanguage());
 	    }
 	    
 	    if (cs != null)
 	    	return cs;
 
 	    return "iso-8859-1";
 	}
 	
 	/**
 	 * Just for testing
 	 */
 	public static void main(String[] args)
 	{
 	    try
 	    {
 	    	System.out.println(Locale.GERMAN+", charset="+LocaleCharSet.getInstance().getCharSet(Locale.GERMAN));
 	    	System.out.println(Locale.JAPANESE+", charset="+LocaleCharSet.getInstance().getCharSet(Locale.JAPANESE));
 	    	System.out.println(Locale.TAIWAN+", charset="+LocaleCharSet.getInstance().getCharSet(Locale.TAIWAN));
 	    	System.out.println(new Locale("PL", "pl")+", charset="+LocaleCharSet.getInstance().getCharSet(new Locale("PL", "pl")));
 	    }
 	    catch (Exception ex)
 	    {
 	        ex.printStackTrace();
 	    }
 	}
 }
 
