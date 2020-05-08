 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.editor.tool.autocomplete.internal;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Singleton;
 
 import org.apache.commons.lang3.StringUtils;
 import org.xwiki.component.annotation.Component;
 
 /**
  * Returns autocompletion hints by finding all methods using introspection.
  *
  * @version $Id$
  * @since 4.2M2
  */
 @Component
 @Singleton
 public class DefaultAutoCompletionMethodFinder extends AbstractAutoCompletionMethodFinder
 {
     /**
      * Keyword prefix for getter methods.
      */
     private static final String GETTER_KEYWORD = "get";
 
     @Override
     public List<HintData> findMethods(Class propertyClass, String fragmentToMatch)
     {
         List<HintData> hintData = new ArrayList<HintData>();
         String lowerCaseFragment = fragmentToMatch.toLowerCase();
 
         for (Method method : propertyClass.getDeclaredMethods()) {
             String methodName = method.getName().toLowerCase();
             if (methodName.startsWith(lowerCaseFragment)
                 || methodName.startsWith(GETTER_KEYWORD + lowerCaseFragment))
             {
                 // Add simplified velocity without the get()
                 if (methodName.startsWith(GETTER_KEYWORD + lowerCaseFragment) 
                     && method.getParameterTypes().length == 0)
                 {
                     String getter = StringUtils.uncapitalize(method.getName().substring(3));
                    hintData.add(new HintData(getter.substring(fragmentToMatch.length()),
                         printShorthand(getter, method)));
                 } else {
                     // Remove the fragmentToMatch from the returned hint name since we want the client side to just
                     // append our returned result where the cursor is.
                     hintData.add(new HintData(StringUtils.removeStart(method.getName(), fragmentToMatch),
                         printMethod(method.getName(), method)));
                 }
             }
         }
 
         return hintData;
     }
 }
