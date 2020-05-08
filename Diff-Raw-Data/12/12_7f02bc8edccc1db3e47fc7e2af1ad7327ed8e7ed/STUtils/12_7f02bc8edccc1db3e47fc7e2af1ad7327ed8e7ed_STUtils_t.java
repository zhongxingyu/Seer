 //;-*- mode: java -*-
 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
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
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: STUtils.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Provides some methods commonly-reused when working with
  *              ST templates.
  */
 package org.acmsl.queryj.api;
 
 /*
  * Importing ACM-SL Commons classes.
  */
 import org.acmsl.commons.patterns.Singleton;
 import org.acmsl.commons.patterns.Utils;
 
 /*
  * Importing StringTemplate classes.
  */
 import org.stringtemplate.v4.STErrorListener;
 import org.stringtemplate.v4.STGroup;
 
 /*
  * Importing Jetbrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /*
  * Importing some JDK classes.
  */
 import java.nio.charset.Charset;
 import java.util.WeakHashMap;
 import java.util.Map;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 import org.stringtemplate.v4.STGroupDir;
 import org.stringtemplate.v4.STGroupFile;
 
 /**
  * Provides some methods commonly-used when working with ST templates.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 @ThreadSafe
 public class STUtils
     implements  Singleton,
                 Utils
 {
     /**
      * The cached template groups.
      */
     private static final Map<String, STGroup> ST_GROUPS = new WeakHashMap<String, STGroup>();
 
     /**
      * Singleton implemented to avoid the double-checked locking.
      */
     private static class STUtilsSingletonContainer
     {
         /**
          * The actual singleton.
          */
         public static final STUtils SINGLETON = new STUtils();
     }
 
     /**
      * Protected constructor to avoid accidental instantiation.
      */
     protected STUtils() {};
 
     /**
      * Retrieves a <code>STUtils</code> instance.
      * @return such instance.
      */
     @NotNull
     public static STUtils getInstance()
     {
         return STUtilsSingletonContainer.SINGLETON;
     }
     
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param errorListener the {@link STErrorListener} instance.
      * @param charset the charset.
      * @return such instance.
      */
     @Nullable
     protected STGroup retrieveGroup(
         @NotNull final String path,
         @NotNull final STErrorListener errorListener,
         @NotNull final Charset charset)
     {
         @Nullable STGroup result = ST_GROUPS.get(path);
         
         if  (result == null)
         {
             result = retrieveUncachedGroup(path, errorListener, charset);
             ST_GROUPS.put(path, result);
         }
         
         return result;
     }
             
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param errorListener the {@link STErrorListener}
      * instance.
      * @param charset the charset.
      * @return such instance.
      */
    @NotNull
     protected STGroup retrieveUncachedGroup(
         @NotNull final String path,
         @NotNull final STErrorListener errorListener,
         @NotNull final Charset charset)
     {
        @NotNull final STGroupFile result = new STGroupFile(path, charset.displayName());
 
//        result.importTemplates(new STGroupDir("org/acmsl/queryj/dao", charset.displayName()));
//        result.importTemplates(new STGroupDir("org/acmsl/queryj/vo", charset.displayName()));
        result.importTemplates(new STGroupDir("org/acmsl/queryj/", charset.displayName()));
        result.isDefined("source");
         result.setListener(errorListener);
 
 //        STGroup.registerGroupLoader(loader);
 //        STroup.registerDefaultLexer(AngleBracketTemplateLexer.class);
 
 //        result = STGroup.loadGroup(path);
 
         return result;
     }
 
 }
