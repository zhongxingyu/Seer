 //;-*- mode: java -*-
 /*
                         QueryJ
 
     Copyright (C) 2002-2005  Jose San Leandro Armendariz
                         chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile: $
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Represents generic templates.
  *
  */
 package org.acmsl.queryj.tools.templates;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.tools.metadata.CachingDecoratorFactory;
 import org.acmsl.queryj.tools.metadata.DecorationUtils;
 import org.acmsl.queryj.tools.metadata.DecoratorFactory;
 import org.acmsl.queryj.tools.templates.DefaultThemeConstants;
 import org.acmsl.queryj.tools.templates.InvalidTemplateException;
 import org.acmsl.queryj.tools.templates.STUtils;
 import org.acmsl.queryj.tools.templates.Template;
 
 /*
  * Importing some ACM-SL Commons classes.
  */
 import org.acmsl.commons.logging.UniqueLogFactory;
 
 /*
  * Importing Commons-Logging classes.
  */
 import org.apache.commons.logging.Log;
 
 /*
  * Importing StringTemplate classes.
  */
 import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Represents generic templates.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public abstract class AbstractTemplate
     implements  Template,
                 DefaultThemeConstants
 {
     /**
      * Caches the StringTemplateGroup for each template class.
      */
     private static Map m__mSTCache;
 
     /**
      * The optional header.
      */
     private String m__strHeader;
 
     /**
      * The cached processed header.
      */
     private String m__strCachedProcessedHeader;
     
     /**
      * The decorator factory.
      */
     private DecoratorFactory m__DecoratorFactory;
 
     /**
      * Builds a <code>AbstractTemplate</code> with given
      * decorator factory.
      * @param header the optional header.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      * @precondition decoratorFactory != null
      */
     protected AbstractTemplate(
         final String header, final DecoratorFactory decoratorFactory)
     {
         immutableSetHeader(header);
         immutableSetDecoratorFactory(decoratorFactory);
         immutableSetSTCache(new HashMap());
     }
 
     /**
      * Specifies the header.
      * @param header the header.
      */
     protected final void immutableSetHeader(final String header)
     {
         m__strHeader = header;
     }
     
     /**
      * Specifies the header.
      * @param header the header.
      */
     protected void setHeader(final String header)
     {
         immutableSetHeader(header);
     }
     
     /**
      * Retrieves the header.
      * @return the header.
      */
     protected String getHeader()
     {
         return m__strHeader;
     }
 
     /**
      * Specifies the cached processed header.
      * @param header such value.
      */
     protected final void immutableSetCachedProcessedHeader(final String header)
     {
         m__strCachedProcessedHeader = header;
     }
     
     /**
      * Specifies the cached processed header.
      * @param header such value.
      */
     protected void setCachedProcessedHeader(final String header)
     {
         immutableSetCachedProcessedHeader(header);
     }
     
     /**
      * Retrieves the cached processed header.
      * @returnsuch value.
      */
     protected String getCachedProcessedHeader()
     {
         return m__strCachedProcessedHeader;
     }
 
     /**
      * Retrieves the processed header.
      * @param input the input.
      * @return such information.
      * @precondition input != null
      */
     public String getProcessedHeader(final Map input)
     {
         String result = getCachedProcessedHeader();
         
         if  (result == null)
         {
             result = processHeader(input, getHeader());
         }
         
         return result;
     }
 
     /**
      * Retrieves the processed header.
      * @param input the input.
      * @return such information.
      * @precondition input != null
      */
     public String processHeader(final Map input, final String header)
     {
         String result = processInnerTemplate(header, input);
 
         setCachedProcessedHeader(result);
         
         return result;
     }
 
     /**
      * Specifies the decorator factory.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      */
     protected final void immutableSetDecoratorFactory(
         final DecoratorFactory factory)
     {
         m__DecoratorFactory = factory;
     }
 
     /**
      * Specifies the decorator factory.
      * @param decoratorFactory the <code>DecoratorFactory</code> instance.
      */
     protected void setDecoratorFactory(
         final DecoratorFactory factory)
     {
         immutableSetDecoratorFactory(factory);
     }
 
     /**
      * Retrieves the <code>DecoratorFactory</code> instance.
      * @return such instance.
      */
     public DecoratorFactory getDecoratorFactory()
     {
         return m__DecoratorFactory;
     }
 
     /**
      * Specifies the ST cache.
      * @param map the map to use as cache.
      */
     protected static final void immutableSetSTCache(final Map map)
     {
         m__mSTCache = map;
     }
 
     /**
      * Retrieves the ST cache.
      * @return the map being used as cache.
      */
     protected static final Map immutableGetSTCache()
     {
         return m__mSTCache;
     }
 
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @return such instance.
      * @precondition path != null
      */
     protected StringTemplateGroup retrieveGroup(final String path)
     {
         return
             retrieveGroup(
                 path,
                 "/org/acmsl/queryj/queryj.stg",
                 immutableGetSTCache());
     }
     
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param theme the theme.
      * @param cache the ST cache.
      * @return such instance.
      * @precondition path != null
      * @precondition theme != null
      * @precondition cache != null
      */
     protected StringTemplateGroup retrieveGroup(
         final String path, final String theme, final Map cache)
     {
         StringTemplateGroup result = null;
         
         Object t_Key = buildSTGroupKey(path, theme);
 
         result = (StringTemplateGroup) cache.get(t_Key);
 
         if  (result == null)
         {
             result = retrieveGroup(path, theme, STUtils.getInstance());
 
             if  (result != null)
             {
                 cache.put(t_Key, result);
             }
         }
         
         return result;
     }
 
     /**
      * Builds a key to store the ST group associated to
      * given path and theme.
      * @param path the ST path.
      * @param theme the ST theme.
      * @return such key.
      * @precondition path != null
      * @precondition theme != null
      */
     protected final Object buildSTGroupKey(final String path, final String theme)
     {
         return ".:\\AbstractTemplate//STCACHE//" + path + "//" + theme;
     }
 
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param theme the theme.
      * @param stUtils the <code>STUtils</code> instance.
      * @return such instance.
      * @precondition path != null
      * @precondition theme != null
      * @precondition stUtils != null
      */
     protected StringTemplateGroup retrieveGroup(
         final String path, final String theme, final STUtils stUtils)
     {
         return stUtils.retrieveGroup(path, theme);
     }
 
     /**
      * Configures given <code>StringTemplate</code> instance.
      * @param stringTemplate such template.
      * @precondition stringTemplate != null
      */
     protected void configure(final StringTemplate stringTemplate)
     {
         stringTemplate.setPassThroughAttributes(true);
         stringTemplate.setLintMode(true);
     }
 
     /**
      * Retrieves the template in given group.
      * @param group the StringTemplate group.
      * @return the template.
      * @precondition group != null
      */
     protected StringTemplate retrieveTemplate(final StringTemplateGroup group)
     {
         StringTemplate result = null;
         
         if  (group != null)
         {
             result = group.getInstanceOf(TEMPLATE_NAME);
         }
 
         return result;
     }
     
     /**
      * Retrieves the current year.
      * @return such value.
      */
     protected int retrieveCurrentYear()
     {
         return retrieveYear(Calendar.getInstance());
     }
     
     /**
      * Retrieves the year defined in given date.
      * @param calendar the calendar.
      * @return such value.
      * @precondition calendar != null
      */
     protected int retrieveYear(final Calendar calendar)
     {
         return calendar.get(Calendar.YEAR);
     }
 
     /**
      * Creates a timestamp.
      * @return such timestamp.
      */
     protected String createTimestamp()
     {
         return createTimestamp(new Date(), TIMESTAMP_FORMATTER);
     }
     
     /**
      * Formats given date.
      * @param date the date.
      * @param formatter the formatter.
      * @return the formatted date.
      * @precondition date != null
      * @precondition formatter != null
      */
     protected String createTimestamp(
         final Date date, final DateFormat formatter)
     {
         return formatter.format(date);
     }
 
     /**
      * Generates the source code.
      * @return such output.
      * @throws InvalidTemplateException if the template cannot be generated.
      */
     public String generate()
       throws  InvalidTemplateException
     {
         logHeader(buildHeader());
 
         return generateOutput(getHeader());
     }
 
     /**
      * Logs a custom header.
      * @param header the header.
      */
     protected void logHeader(final String header)
     {
         Log t_Log = UniqueLogFactory.getLog(AbstractTemplate.class);
         
         if  (t_Log != null)
         {
             t_Log.trace(header);
         }
     }
 
     /**
      * Builds the header for logging purposes.
      * @return such header.
      */
     protected String buildHeader()
     {
         return "Generating " + getClass().getName() + ".";
     }
 
     /**
      * Processes given text as a template.
      * @param template the template.
      * @param input the input.
      * @return the processed text.
      * @precondition template != null
      * @precondition input != null
      */
     protected String processInnerTemplate(final String template, final Map input)
     {
         String result = null;
         
         if  (template != null)
         {
             
             StringTemplate t_strInnerTemplate =
                 new StringTemplate(template, AngleBracketTemplateLexer.class);
 
             t_strInnerTemplate.setAttribute("input", input);
         
             result = t_strInnerTemplate.toString();
         }
         
         return result;
     }
     
     /**
      * Generates the actual source code.
      * @param header the header.
      * @return such output. 
      * @throws InvalidTemplateException if the template cannot be generated.
      */
     protected abstract String generateOutput(final String header)
       throws  InvalidTemplateException;
 
     /**
      * Normalizes given value, in lower-case.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return such output.
      * @precondition value != null
      * @precondition decorationUtils != null
      */
     protected String normalizeLowercase(
         final String value, final DecorationUtils decorationUtils)
     {
         return decorationUtils.normalizeLowercase(value);
     }
 
     /**
      * Capitalizes given value.
      * @param value the value.
      * @return such output.
      * @precondition value != null
      */
     protected String capitalize(final String value)
     {
         return capitalize(value, DecorationUtils.getInstance());
     }
 
     /**
      * Capitalizes given value.
      * @param value the value.
      * @param decorationUtils the <code>DecorationUtils</code> instance.
      * @return such output.
      * @precondition value != null
      * @precondition decorationUtils != null
      */
     protected String capitalize(
         final String value, final DecorationUtils decorationUtils)
     {
         return decorationUtils.capitalize(value);
     }
 }
