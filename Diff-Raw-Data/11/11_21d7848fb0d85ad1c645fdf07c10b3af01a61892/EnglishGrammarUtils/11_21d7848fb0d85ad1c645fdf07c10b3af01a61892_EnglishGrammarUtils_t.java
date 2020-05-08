 /*
                         ACM-SL Commons
 
     Copyright (C) 2002-2003  Jose San Leandro Armendriz
                              jsanleandro@yahoo.es
                              chousz@yahoo.com
 
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
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-307  USA
 
 
     Thanks to ACM S.L. for distributing this library under the LGPL license.
     Contact info: jsr000@terra.es
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabaas
                     Boadilla del monte
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendriz
  *
  * Description: Provides some grammar rules for English language.
  *
  * Last modified by: $Author$ at $Date$
  *
  * File version: $Revision$
  *
  * Project version: $Name$
  *                  ("Name" means no concrete version has been checked out)
  *
  * $Id$
  *
  */
 package org.acmsl.commons.utils;
 
 /*
  * Importing some ACM-SL classes.
  */
 import org.acmsl.commons.patterns.Utils;
 
 /*
  * Importing some JDK classes.
  */
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Provides some grammar rules for English language.
  * @author <a href="mailto:jsanleandro@yahoo.es"
            >Jose San Leandro Armendriz</a>
  * @version $Revision$
  */
 public class EnglishGrammarUtils
     implements  Utils
 {
     /**
      * Singleton implemented as a weak reference.
      */
     private static WeakReference singleton;
 
     /**
      * The irregular plural word list.
      */
     private static Map m__mIrregularPluralForms;
 
     /**
      * Protected constructor to avoid accidental instantiation.
      */
     protected EnglishGrammarUtils()  {};
 
     /**
      * Specifies a new weak reference.
      * @param utils the utils instance to use.
      */
     protected static void setReference(final EnglishGrammarUtils utils)
     {
         singleton = new WeakReference(utils);
     }
 
     /**
      * Retrieves the weak reference.
      * @return such reference.
      */
     protected static WeakReference getReference()
     {
         return singleton;
     }
 
     /**
      * Specifies the irregular plural forms.
      * @param map the collection.
      * @precondition map != null
      */
     protected static synchronized final void setIrregularPluralForms(
         final Map map)
     {
         m__mIrregularPluralForms = map;
     }
 
     /**
      * Retrieves the plural exceptions.
      * @return such exceptions.
      */
     protected static final Map getIrregularPluralForms()
     {
         Map result = m__mIrregularPluralForms;
 
         if  (result == null)
         {
             result = new HashMap();
             initIrregularPluralForms(result);
             setIrregularPluralForms(result);
         }
 
         return result;
     }
 
     /**
      * Initializes the plural exceptions.
      * @param map the map.
      * @precondition map != null
      */
     protected static void initIrregularPluralForms(final Map map)
     {
         put("man", "men", map);
         put("woman", "women", map);
         put("water", "water", map);
         put("fish", "fish", map);
        put("role", "roles", map);
     }
 
     /**
      * Builds a plural key.
      * @param word the word.
      * @precondition word != null
      */
     protected static Object buildPluralKey(final String word)
     {
         return ".|plural:" + word.toLowerCase() + "|.";
     }
 
     /**
      * Builds a plural key.
      * @param word the word.
      * @precondition word != null
      */
     protected static Object buildSingularKey(final String word)
     {
         return ".|singular:" + word.toLowerCase() + "|.";
     }
 
     /**
      * Puts the irregular plural version of given word.
      * @param singular the word in singular form.
      * @param plural the word in plural form.
      * @param map the map.
      * @precondition singular != null
      * @precondition plural != null
      * @precondition map != null
      */
     protected static void put(
         final String singular, final String plural, final Map map)
     {
         map.put(buildSingularKey(singular), plural);
         map.put(buildSingularKey(plural), plural);
         map.put(buildPluralKey(plural), singular);
         map.put(buildPluralKey(singular), singular);
     }
 
     /**
      * Retrieves the irregular plural form of given word.
      * @param singular the word.
      * @return the plural form.
      * @precondition singular != null
      */
     protected String getIrregularPlural(final String singular)
     {
         return getIrregularPlural(singular, getIrregularPluralForms());
     }
 
     /**
      * Retrieves the irregular plural form of given word.
      * @param singular the word.
      * @param irregularPluralForms the irregular plural forms.
      * @return the plural form.
      * @precondition singular != null
      * @precondition irregularPluralForms != null
      */
     protected String getIrregularPlural(
         final String singular, final Map irregularPluralForms)
     {
         return (String) irregularPluralForms.get(buildSingularKey(singular));
     }
 
     /**
      * Retrieves the irregular singular form of given word.
      * @param plural the word.
      * @return the plural form.
      * @precondition plural != null
      */
     protected String getIrregularSingular(final String plural)
     {
         return getIrregularSingular(plural, getIrregularPluralForms());
     }
 
     /**
      * Retrieves the irregular singular form of given word.
      * @param plural the word.
      * @param irregularPluralForms the irregular plural forms.
      * @return the plural form.
      * @precondition plural != null
      * @precondition irregularPluralForms != null
      */
     protected String getIrregularSingular(
         final String plural, final Map irregularPluralForms)
     {
         return (String) irregularPluralForms.get(buildPluralKey(plural));
     }
 
     /**
      * Retrieves a EnglishGrammarUtils instance.
      * @return such instance.
      */
     public static EnglishGrammarUtils getInstance()
     {
         EnglishGrammarUtils result = null;
 
         WeakReference reference = getReference();
 
         if  (reference != null) 
         {
             result = (EnglishGrammarUtils) reference.get();
         }
 
         if  (result == null) 
         {
             result = new EnglishGrammarUtils() {};
         }
         
         return result;
     }
 
     /**
      * Converts given word to plural.
      * @param word the word to convert.
      * @return the converted word.
      * @precondition word != null
      */
     public String getPlural(final String word)
     {
         return getPlural(word, getIrregularPluralForms());
     }
 
     /**
      * Converts given word to plural.
      * @param word the word to convert.
      * @param irregularSingularForms words whose singular form is not
      * constructed using the regular rules.
      * @return the converted word.
      * @precondition word != null
      * @precondition irregularSingularForms != null
      */
     protected String getPlural(
         final String word, final Map irregularSingularForms)
     {
         String result =
             (String) irregularSingularForms.get(buildSingularKey(word));
 
         if  (result == null)
         {
             result = word.trim().toLowerCase();
 
             if  (   (result.endsWith("s"))
                  || (result.endsWith("x"))
                  || (result.endsWith("sh"))
                  || (result.endsWith("ch")))
             {
                 result += "e";
             }
 
             result += "s";
         }
 
         if  (word.trim().toUpperCase().equals(word))
         {
             result = result.toUpperCase();
         }
 
         return result;
     }
 
     /**
      * Converts given word to singular.
      * @param word the word to convert.
      * @return the converted word.
      * @precondition word != null
      */
     public String getSingular(final String word)
     {
         return getSingular(word, getIrregularPluralForms());
     }
 
     /**
      * Converts given word to singular.
      * @param word the word to convert.
      * @param irregularPluralForms words whose plural form is not
      * constructed using the regular rules.
      * @return the converted word.
      * @precondition word != null
      * @precondition irregularPluralForms != null
      */
     protected String getSingular(
         final String word, final Map irregularPluralForms)
     {
         String result =
             (String) irregularPluralForms.get(buildPluralKey(word));
 
         if  (result == null)
         {
             result = word.trim().toLowerCase();
 
             if  (result.endsWith("s"))
             {
                 if  (   (result.endsWith("es"))
                      || (result.endsWith("xes"))
                      || (result.endsWith("shes"))
                      || (result.endsWith("ches")))
                 {
                     result = result.substring(0, result.length() - 1);
                 }
 
                 result = result.substring(0, result.length() - 1);
             }
         }
 
         if  (word.trim().toUpperCase().equals(word))
         {
             result = result.toUpperCase();
         }
 
         return result;
     }
 }
