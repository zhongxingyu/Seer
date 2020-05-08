 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.structure;
 
import org.apache.commons.lang.StringUtils;

 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Option for structure elements (groups, properties, assignments)
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxStructureOption implements Serializable {
 
     private static final long serialVersionUID = 737017384585248182L;
     public final static String OPTION_MULTILANG = "MULTILANG";
     public final static String OPTION_SHOW_OVERVIEW = "SHOW.OVERVIEW";
     public final static String OPTION_HTML_EDITOR = "HTML.EDITOR";
     public final static String OPTION_SEARCHABLE = "SEARCHABLE";
 
     public final static String OPTION_MULTILINE = "MULTILINE";
     public final static String VALUE_TRUE = "1";
 
 
     public final static String VALUE_FALSE = "0";
     private String key;
     protected boolean overrideable;
     private String value;
     private boolean set;
 
     /**
      * Ctor
      *
      * @param key          key identifying the option
      * @param overrideable is the option overridable in assignments
      * @param set          is the option set? (non-existing options are returned as not-set options!)
      * @param value        the options value
      */
     public FxStructureOption(String key, boolean overrideable, boolean set, String value) {
         this.key = key.toUpperCase();
         this.overrideable = overrideable;
         this.set = set;
         this.value = value;
     }
 
     /**
      * Copy Constructor
      *
      * @param o an FxStructureOption
      */
     public FxStructureOption(FxStructureOption o) {
         this.key = o.key;
         this.overrideable = o.overrideable;
         this.set = o.set;
         this.value = o.value;
     }
 
     /**
      * Get the option key
      *
      * @return option key
      */
     public String getKey() {
         return key;
     }
 
     /**
      * Is the option overrideable (in assignments)?
      *
      * @return option overrideable (in assignments)?
      */
     public boolean isOverrideable() {
         return overrideable;
     }
 
     /**
      * Is the option set, will return <code>false</code> if an unknown option is requested
      *
      * @return if option is set, will return <code>false</code> if an unknown option is requested
      */
     public boolean isSet() {
         return set;
     }
 
     /**
      * Get the value assigned to the option
      *
      * @return value assigned to the option
      */
     public String getValue() {
         return value;
     }
 
     /**
      * Convenience method to check if value is set to true
      *
      * @return if value is set to true
      */
     public boolean isValueTrue() {
         return VALUE_TRUE.equals(value);
     }
 
     /**
      * Check if an option is set for the requested key
      *
      * @param key     option key
      * @param options the available options
      * @return if an option is set for the requested key
      */
     public static boolean hasOption(String key, List<FxStructureOption> options) {
         if (key != null)
             key = key.trim().toUpperCase();
         if (key == null || key.length() == 0 || options == null || options.size() == 0)
             return false;
         for (FxStructureOption option : options)
             if (key.equals(option.getKey()))
                 return true;
         return false;
     }
 
     /**
      * Get a default entry for an unknown option
      *
      * @param key the key
      * @return an unknown option
      */
     private static FxStructureOption getUnknownOption(String key) {
         return new FxStructureOption(key, false, false, "");
     }
 
     /**
      * Get an option entry for the given key, if the key is invalid or not found a <code>FxStructureOption</code> object
      * will be returned with <code>set</code> set to <code>false</code>, overrideable set to <code>false</code> and value
      * set to an empty String.
      *
      * @param key     option key
      * @param options the available options
      * @return the found option or an object that indicates that the option is not set
      */
     public static FxStructureOption getOption(String key, List<FxStructureOption> options) {
         if (key != null)
             key = key.trim().toUpperCase();
         if (key == null || key.length() == 0 || options == null || options.size() == 0)
            return getUnknownOption(StringUtils.defaultString(key));
         for (FxStructureOption option : options)
             if (key.equals(option.getKey()))
                 return option;
         return getUnknownOption(key);
     }
 
     /**
      * Get a list of empty options
      *
      * @param capacity desired capacity
      * @return list of empty options
      */
     public static List<FxStructureOption> getEmptyOptionList(int capacity) {
         return new ArrayList<FxStructureOption>(capacity);
     }
 
     /**
      * Set or add a String value in a list of options
      *
      * @param options      list of existing options
      * @param key          option key
      * @param overrideable should the option be overrideable?
      * @param value        String value to set for the option
      */
     public static void setOption(List<FxStructureOption> options, String key, boolean overrideable, String value) {
         synchronized (options) {
             if (hasOption(key, options)) {
                 FxStructureOption opt = getOption(key, options);
                 opt.overrideable = overrideable;
                 opt.value = value;
                 opt.set = true;
                 return;
             }
             FxStructureOption opt = new FxStructureOption(key, overrideable, true, value);
             options.add(opt);
         }
     }
 
     /**
      * Set or add a boolean value in a list of options
      *
      * @param options      list of existing options
      * @param key          option key
      * @param overrideable should the option be overrideable?
      * @param value        boolean value to set for the option (will be converted internally to a String)
      */
     public static void setOption(List<FxStructureOption> options, String key, boolean overrideable, boolean value) {
         setOption(options, key, overrideable, value ? VALUE_TRUE : VALUE_FALSE);
     }
 
     /**
      * Clear the option with the given key - removing it from the list if it exists
      *
      * @param options list options to clear the option for
      * @param key     key of the option to remove
      */
     public static void clearOption(List<FxStructureOption> options, String key) {
         if (key != null)
             key = key.trim().toUpperCase();
         if (key == null || key.length() == 0 || options == null || options.size() == 0)
             return;
         synchronized (options) {
             for (FxStructureOption option : options)
                 if (key.equals(option.getKey())) {
                     options.remove(option);
                     return;
                 }
         }
     }
 
     /**
      * Get a copy of the a list of options
      *
      * @param options list to clone
      * @return cloned list of options
      */
     public static List<FxStructureOption> cloneOptions(List<FxStructureOption> options) {
         if (options == null)
             return null;
         else {
             ArrayList<FxStructureOption> clone = new ArrayList<FxStructureOption>(options.size());
             for (FxStructureOption o : options) {
                 clone.add(new FxStructureOption(o));
             }
             return clone;
         }
     }
 
     /**
      * Convert a list of options to an unmodifieable list
      *
      * @param options list to convert
      * @return unmodifieable list of options
      */
     public static List<FxStructureOption> getUnmodifieableOptions(List<FxStructureOption> options) {
         return Collections.unmodifiableList(options);
     }
 
     /**
      * Checks for equality
      * @return if the option's values equal this
      */
     @Override
     public boolean equals(Object o) {
         if (o == null || !(o instanceof FxStructureOption))
             return false;
         if (o == this)
             return true;
         final FxStructureOption other = (FxStructureOption) o;
         if (!this.key.equals(other.key) || !this.value.equals(other.value) || this.overrideable != other.overrideable || this.set != other.set)
             return false;
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         int result;
         result = (key != null ? key.hashCode() : 0);
         result = 31 * result + (overrideable ? 1 : 0);
         result = 31 * result + (value != null ? value.hashCode() : 0);
         result = 31 * result + (set ? 1 : 0);
         return result;
     }
 }
