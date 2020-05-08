 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 package com.flexive.shared.value;
 
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxInvalidStateException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.value.renderer.FxValueRendererFactory;
 import org.apache.commons.lang.ArrayUtils;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Abstract base class of all value objects.
  * Common base classed is used for multilingual properties, etc.
  * <p/>
  * To check if a value is empty a flag is used for each language resp. the single value.
  * Use the setEmpty() method to explicity set a value to be empty
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public abstract class FxValue<T, TDerived extends FxValue<T, TDerived>> implements Serializable, Comparable<FxValue> {
     private static final long serialVersionUID = -5005063788615664383L;
 
     public static final boolean DEFAULT_MULTILANGUAGE = true;
     protected boolean multiLanguage;
     protected long defaultLanguage = FxLanguage.SYSTEM_ID;
     private long selectedLanguage;
     private int maxInputLength;
     private String XPath = "";
 
     private final static long[] SYSTEM_LANG_ARRAY = new long[]{FxLanguage.SYSTEM_ID};
 
     /**
      * Data if <code>multiLanguage</code> is enabled
      */
     protected Map<Long, T> translations;
     protected Map<Long, Boolean> emptyTranslations;
 
     /**
      * Data if <code>multiLanguage</code> is disabled
      */
     protected T singleValue;
     private boolean singleValueEmpty;
     private boolean readOnly;
 
     /**
      * Constructor
      *
      * @param multiLanguage   multilanguage value?
      * @param defaultLanguage the default language
      * @param translations    HashMap containing language->translation mapping
      */
     protected FxValue(boolean multiLanguage, long defaultLanguage, Map<Long, T> translations) {
         this.defaultLanguage = defaultLanguage;
         this.multiLanguage = multiLanguage;
         this.maxInputLength = -1;
         this.readOnly = false;
         if (multiLanguage) {
             if (translations == null) {
                 //valid to pass null, create an empty one
                 this.translations = new HashMap<Long, T>(5);
                 this.emptyTranslations = new HashMap<Long, Boolean>(5);
             } else {
                 this.translations = new HashMap<Long, T>(translations);
                 this.emptyTranslations = new HashMap<Long, Boolean>(translations.size());
             }
             if (this.defaultLanguage < 0) {
                 this.defaultLanguage = FxLanguage.SYSTEM_ID;
                 this.selectedLanguage = FxLanguage.SYSTEM_ID;
                 if (translations != null && !translations.entrySet().isEmpty())
                     for (Entry<Long, T> e : translations.entrySet())
                         if (e.getValue() != null) {
                             this.selectedLanguage = e.getKey();
                             break;
                         }
             } else
                 this.selectedLanguage = this.defaultLanguage;
             for (Long lang : this.translations.keySet())
                 emptyTranslations.put(lang, this.translations.get(lang) == null);
         } else {
             if (translations != null && !translations.isEmpty()) {
                 //a translation is provided, use the defaultLanguage element or very first element if not present
                 singleValue = translations.get(defaultLanguage);
                 if (singleValue == null)
                     singleValue = translations.values().iterator().next();
             }
             this.defaultLanguage = FxLanguage.SYSTEM_ID;
             this.selectedLanguage = FxLanguage.SYSTEM_ID;
             this.translations = null;
             this.singleValueEmpty = false;
         }
     }
 
     /**
      * Initialize an empty FxValue (used for initalization for XML import, etc.)
      *
      * @param defaultLanguage default language
      * @param multiLanguage   multilanguage value?
      */
     protected FxValue(long defaultLanguage, boolean multiLanguage) {
         this.multiLanguage = multiLanguage;
         this.defaultLanguage = defaultLanguage;
         this.maxInputLength = -1;
         this.readOnly = false;
         if (this.multiLanguage) {
             this.translations = new HashMap<Long, T>(5);
             this.emptyTranslations = new HashMap<Long, Boolean>(5);
             if (this.defaultLanguage < 0) {
                 this.defaultLanguage = FxLanguage.SYSTEM_ID;
                 this.selectedLanguage = FxLanguage.SYSTEM_ID;
             } else
                 this.selectedLanguage = this.defaultLanguage;
             for (Long lang : this.translations.keySet())
                 emptyTranslations.put(lang, this.translations.get(lang) == null);
         } else {
             this.defaultLanguage = FxLanguage.SYSTEM_ID;
             this.selectedLanguage = FxLanguage.SYSTEM_ID;
             this.translations = null;
             this.singleValueEmpty = false;
         }
     }
 
     /**
      * Constructor
      *
      * @param defaultLanguage the default language
      * @param translations    HashMap containing language->translation mapping
      */
     protected FxValue(long defaultLanguage, Map<Long, T> translations) {
         this(DEFAULT_MULTILANGUAGE, defaultLanguage, translations);
     }
 
     /**
      * Constructor
      *
      * @param multiLanguage multilanguage value?
      * @param translations  HashMap containing language->translation mapping
      */
     protected FxValue(boolean multiLanguage, Map<Long, T> translations) {
         this(multiLanguage, FxLanguage.SYSTEM_ID, translations);
     }
 
     /**
      * Constructor
      *
      * @param translations HashMap containing language->translation mapping
      */
     protected FxValue(Map<Long, T> translations) {
         this(DEFAULT_MULTILANGUAGE, FxLanguage.SYSTEM_ID, translations);
     }
 
     /**
      * Constructor - create value from an array of translations
      *
      * @param translations HashMap containing language->translation mapping
      * @param pos          position (index) in the array to use
      */
     protected FxValue(Map<Long, T[]> translations, int pos) {
         this(DEFAULT_MULTILANGUAGE, FxLanguage.SYSTEM_ID, new HashMap<Long, T>((translations == null ? 5 : translations.size())));
         if (multiLanguage) {
             if (translations == null)
                 return;
             for (Entry<Long, T[]> e : translations.entrySet())
                 if (e.getValue()[pos] != null)
                     this.translations.put(e.getKey(), e.getValue()[pos]);
                 else
                     this.emptyTranslations.put(e.getKey(), Boolean.TRUE);
         } else {
             this.singleValue = (translations == null || translations.isEmpty() ? null : translations.values().iterator().next()[pos]);
             if( this.singleValue == null )
                 this.singleValueEmpty = true;
         }
     }
 
     /**
      * Constructor
      *
      * @param multiLanguage   multilanguage value?
      * @param defaultLanguage the default language
      * @param value           single initializing value
      */
     protected FxValue(boolean multiLanguage, long defaultLanguage, T value) {
         this(multiLanguage, defaultLanguage, (Map<Long, T>) null);
         if (value == null) {
             if(multiLanguage)
                 this.emptyTranslations.put(defaultLanguage, Boolean.TRUE);
             else
                 this.singleValueEmpty = true;
         } else {
             if (multiLanguage)
                 this.translations.put(defaultLanguage, value);
             else
                 this.singleValue = value;
         }
     }
 
     /**
      * Constructor
      *
      * @param defaultLanguage the default language
      * @param value           single initializing value
      */
     protected FxValue(long defaultLanguage, T value) {
         this(DEFAULT_MULTILANGUAGE, defaultLanguage, value);
     }
 
     /**
      * Constructor
      *
      * @param multiLanguage multilanguage value?
      * @param value         single initializing value
      */
     protected FxValue(boolean multiLanguage, T value) {
         this(multiLanguage, FxLanguage.DEFAULT_ID, value);
     }
 
     /**
      * Constructor
      *
      * @param value single initializing value
      */
     protected FxValue(T value) {
         this(DEFAULT_MULTILANGUAGE, value);
     }
 
     /**
      * Constructor
      *
      * @param clone original FxValue to be cloned
      */
     @SuppressWarnings("unchecked")
     protected FxValue(FxValue<T, TDerived> clone) {
         this(clone.isMultiLanguage(), clone.getDefaultLanguage(), new HashMap<Long, T>((clone.translations != null ? clone.translations.size() : 1)));
         this.XPath = clone.XPath;
         this.maxInputLength = clone.maxInputLength;
         if (clone.isImmutableValueType()) {
             if (multiLanguage) {
                 // clone only hashmap
                 this.translations = new HashMap(clone.translations);
                 this.emptyTranslations = new HashMap(clone.emptyTranslations);
             } else {
                 this.singleValue = clone.singleValue;
                 this.singleValueEmpty = clone.singleValueEmpty;
             }
         } else {
             if (multiLanguage) {
                 // clone hashmap values
                 Method meth = null;
                 for (long k : clone.translations.keySet()) {
                     T t = clone.translations.get(k);
                     if (t == null)
                         this.translations.put(k, null);
                     else {
                         try {
                             if (meth != null) {
                                 this.translations.put(k, (T) meth.invoke(t));
                             } else {
                                 Class<?> clzz = t.getClass();
                                 meth = clzz.getMethod("clone");
                             }
                         } catch (Exception e) {
                             throw new IllegalArgumentException("clone not supported", e);
                         }
                     }
                 }
                 this.emptyTranslations = new HashMap(clone.emptyTranslations);
             } else {
                 try {
                     this.singleValue = (T) clone.singleValue.getClass().
                             getMethod("clone").invoke(clone.singleValue);
                 } catch (Exception e) {
                     throw new IllegalArgumentException("clone not supported", e);
                 }
                 this.singleValueEmpty = clone.singleValueEmpty;
             }
         }
     }
 
     /**
      * Get the XPath for this value - the XPath is optional and can be an empty String if
      * not explicitly assigned!
      *
      * @return XPath (optional! can be an empty String)
      */
     public String getXPath() {
         return XPath;
     }
 
     /**
      * Returns the name of the value from the xpath.
      * <p/>
      * If the xpath is an empty string the name will also return an emptry String.
      *
      * @return the property name
      */
     public String getXPathName() {
         try {
             String xpathSplit[] = getXPath().split("/");
             return xpathSplit[xpathSplit.length - 1].split("\\[")[0];
         } catch (Throwable t) {
             return "";
         }
     }
 
     /**
      * Set the XPath (unless readonly)
      *
      * @param XPath the XPath to set, will be ignored if readonly
      * @return this
      */
     @SuppressWarnings({"unchecked"})
     public TDerived setXPath(String XPath) {
         if (!this.readOnly) {
             this.XPath = XPath;
         }
         return (TDerived) this;
     }
 
     /**
      * One-time operation to flag this FxValue as read only.
      * This is not reversible!
      */
     public void setReadOnly() {
         this.readOnly = true;
     }
 
     /**
      * Mark this FxValue as empty
      *
      * @return this
      */
     @SuppressWarnings("unchecked")
     public TDerived setEmpty() {
         if (this.multiLanguage) {
             if (this.emptyTranslations == null)
                 this.emptyTranslations = new HashMap<Long, Boolean>(this.translations.size());
             for (Long lang : this.translations.keySet())
                 this.emptyTranslations.put(lang, true);
         } else {
             this.singleValueEmpty = true;
         }
         return (TDerived) this;
     }
 
     /**
      * Mark the entry for the given language as empty
      *
      * @param language the language to flag as empty
      */
     public void setEmpty(long language) {
         if (this.multiLanguage) {
             if (this.emptyTranslations == null)
                 this.emptyTranslations = new HashMap<Long, Boolean>(this.translations.size());
             this.emptyTranslations.put(language, true);
         } else {
             this.singleValueEmpty = true;
         }
     }
 
     /**
      * Return the class instance of the value type.
      *
      * @return the class instance of the value type.
      */
     public abstract Class<T> getValueClass();
 
     /**
      * Evaluates the given string value to an object of type T.
      *
      * @param value string value to be evaluated
      * @return the value interpreted as T
      */
     public abstract T fromString(String value);
 
     /**
      * Converts the given instance of T to a string that can be
      * parsed again by {@link FxValue#fromString(String)}.
      *
      * @param value the value to be converted
      * @return a string representation of the given value that can be parsed again using
      *         {@link FxValue#fromString(String)}.
      */
     public String getStringValue(T value) {
         return String.valueOf(value);
     }
 
     /**
      * Creates a copy of the given object (useful if the actual type is unknown).
      *
      * @return a copy of the given object (useful if the actual type is unknown).
      */
     public abstract TDerived copy();
 
     /**
      * Return true if T is immutable (e.g. java.lang.String). This prevents cloning
      * of the translations in copy constructors.
      *
      * @return true if T is immutable (e.g. java.lang.String)
      */
     public boolean isImmutableValueType() {
         return true;
     }
 
     /**
      * Is this value editable by the user?
      * This always returns true except it is a FxNoAccess value or flagged as readOnly
      *
      * @return if this value editable?
      * @see FxNoAccess
      */
     public boolean isReadOnly() {
         return readOnly;
     }
 
     /**
      * Returns true if this value is valid for the actual type (e.g. if
      * a FxNumber property actually contains only valid numbers).
      *
      * @return true if this value is valid for the actual type
      */
     public boolean isValid() {
         //noinspection UnusedCatchParameter
         try {
             getErrorValue();
             // an error value exists, thus this instance is invalid
             return false;
         } catch (IllegalStateException e) {
             // this instance is valid, thus no error value could be retrieved
             return true;
         }
     }
 
     /**
      * Returns true if the translation for the given language is valid. An empty translation
      * is always valid.
      *
      * @param languageId     the language ID
      * @return               true if the translation for the given language is valid
      * @since 3.1
      */
     public boolean isValid(long languageId) {
         final T value = getTranslation(languageId);
         if (value == null || !(value instanceof String)) {
             // empty or non-string translations are always valid
             return true;
         }
         // try a conversion to the native type
         try {
             fromString((String) value);
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     /**
      * Returns true if the translation for the given language is valid. An empty translation
      * is always valid.
      *
      * @param language       the language
      * @return               true if the translation for the given language is valid
      * @since 3.1
      */
     public boolean isValid(FxLanguage language) {
         return isValid(language != null ? language.getId() : -1);
     }
 
     /**
      * Returns the value that caused {@link #isValid} to return false. If isValid() is true,
      * a RuntimeException is thrown.
      *
      * @return the value that caused the validation via {@link #isValid} to fail
      * @throws IllegalStateException if the instance is valid and the error value is undefined
      */
     @SuppressWarnings({"UnusedCatchParameter"})
     public T getErrorValue() throws IllegalStateException {
         if (multiLanguage) {
             for (T translation : translations.values()) {
                 if (translation instanceof String) {
                     // if a string was used, check if it is a valid representation of our type
                     try {
                         fromString((String) translation);
                     } catch (Exception e) {
                         return translation;
                     }
                 }
             }
         } else if (singleValue instanceof String) {
             try {
                 fromString((String) singleValue);
             } catch (Exception e) {
                 return singleValue;
             }
         }
         throw new IllegalStateException();
     }
 
     /**
      * Get a representation of this value in the default translation
      *
      * @return T
      */
     public T getDefaultTranslation() {
         if (!multiLanguage)
             return singleValue;
         T def = getTranslation(getDefaultLanguage());
         if (def != null)
             return def;
         if (translations.size() > 0)
             return translations.values().iterator().next(); //first available translation if default does not exist
         return def; //empty as last fallback
     }
 
     /**
      * Get the translation for a requested language
      *
      * @param lang requested language
      * @return translation or an empty String if it does not exist
      */
     public T getTranslation(long lang) {
         return (multiLanguage ? translations.get(lang) : singleValue);
     }
 
     /**
      * Get a String representation of this value in the requested language or
      * an empty String if the translation does not exist
      *
      * @param lang requested language id
      * @return T translation
      */
     public T getTranslation(FxLanguage lang) {
         if (!multiLanguage) //redundant but faster
             return singleValue;
         return getTranslation((int) lang.getId());
     }
 
     /**
      * Get the translation that best fits the requested language.
      * The requested language is queried and if it does not exist the
      * default translation is returned
      *
      * @param lang requested best-fit language
      * @return best fit translation
      */
     public T getBestTranslation(long lang) {
         if (!multiLanguage) //redundant but faster
             return singleValue;
         T ret = getTranslation(lang);
         if (ret != null)
             return ret;
         return getDefaultTranslation();
     }
 
     /**
      * Get the translation that best fits the requested language.
      * The requested language is queried and if it does not exist the
      * default translation is returned
      *
      * @param language requested best-fit language
      * @return best fit translation
      */
     public T getBestTranslation(FxLanguage language) {
         if (!multiLanguage)     //redundant but faster
             return singleValue;
         if (language == null)   // user ticket language
             return getBestTranslation();
         return getBestTranslation((int) language.getId());
     }
 
     /**
      * Get the translation that best fits the requested users language.
      * The requested users language is queried and if it does not exist the
      * default translation is returned
      *
      * @param ticket UserTicket to obtain the users language
      * @return best fit translation
      */
     public T getBestTranslation(UserTicket ticket) {
         if (!multiLanguage) //redundant but faster
             return singleValue;
         return getBestTranslation((int) ticket.getLanguage().getId());
     }
 
     /**
      * Get the translation that best fits the current users language.
      * The user language is obtained from the FxContext thread local.
      *
      * @return best fit translation
      */
     public T getBestTranslation() {
         if (!multiLanguage) //redundant but faster
             return singleValue;
         return getBestTranslation(FxContext.getUserTicket().getLanguage());
     }
 
     /**
      * Get all languages for which translations exist
      *
      * @return languages for which translations exist
      */
     public long[] getTranslatedLanguages() {
         return (multiLanguage ? ArrayUtils.toPrimitive(translations.keySet().toArray(new Long[translations.keySet().size()])) : SYSTEM_LANG_ARRAY.clone());
     }
 
     /**
      * Does a translation exist for the given language?
      *
      * @param languageId language to query
      * @return translation exists
      */
     public boolean translationExists(long languageId) {
         return !multiLanguage || translations.get(languageId) != null;
     }
 
     /**
      * Like empty(), for JSF EL, since empty cannot be used.
      *
      * @return true if the value is empty
      */
     public boolean getIsEmpty() {
         return isEmpty();
     }
 
     /**
      * Is this value empty?
      *
      * @return if value is empty
      */
     public boolean isEmpty() {
         if (multiLanguage) {
             syncEmptyTranslations();
             for (boolean check : emptyTranslations.values())
                 if (!check)
                     return false;
             return true;
         } else
             return singleValueEmpty;
     }
 
     /**
      * Check if the translation for the given language is empty
      *
      * @param lang language to check
      * @return if translation for the given language is empty
      */
     public boolean isTranslationEmpty(FxLanguage lang) {
         return lang != null ? isTranslationEmpty(lang.getId()) : isEmpty();
     }
 
     /**
      * Check if the translation for the given language is empty
      *
      * @param lang language to check
      * @return if translation for the given language is empty
      */
     public boolean isTranslationEmpty(long lang) {
         if (!multiLanguage)
             return singleValueEmpty;
         syncEmptyTranslations();
         return !emptyTranslations.containsKey(lang) || emptyTranslations.get(lang);
     }
 
     /**
      * Synchronize - and create if needed - empty tanslations
      */
     private void syncEmptyTranslations() {
         if (!multiLanguage)
             return;
         if (emptyTranslations == null)
             emptyTranslations = new HashMap<Long, Boolean>(translations.size());
         if (emptyTranslations.size() != translations.size()) {
             //resync
             for (Long _lang : translations.keySet()) {
                 if (!emptyTranslations.containsKey(_lang))
                     emptyTranslations.put(_lang, translations.get(_lang) == null);
             }
         }
     }
 
     /**
      * Get the language selected in user interfaces
      *
      * @return selected language
      */
     public long getSelectedLanguage() {
         return selectedLanguage;
     }
 
     /**
      * Set the user selected language
      *
      * @param selectedLanguage selected language ID
      * @return self
      * @throws FxNoAccessException if the selected Language is not contained
      */
     public FxValue setSelectedLanguage(long selectedLanguage) throws FxNoAccessException {
         if (selectedLanguage < 0 || !multiLanguage)
             return this;
         //throw exception if selectedLanguage is not contained!
         if (!translations.containsKey(selectedLanguage))
             throw new FxNoAccessException("ex.content.value.invalid.language", selectedLanguage);
         this.selectedLanguage = selectedLanguage;
         return this;
     }
 
     /**
      * Set the translation for a language or override the single language value if
      * this value is not flagged as multi language enabled. This method cannot be
      * overridden since it not only accepts parameters of type T, but also of type
      * String for web form handling.
      *
      * @param language language to set the translation for
      * @param value    translation
      * @return this
      */
     public final TDerived setTranslation(long language, T value) {
         if (value instanceof String) {
             try {
                 value = this.fromString((String) value);
             } catch (Exception e) {
                 // do nothing. The resulting FxValue will be invalid,
                 // but the invalid value will be preserved.
                 // TODO: use a "safer" concept of representing invalid translations,
                 // since this may lead to unexpeced ClassCastExceptions in parameterized
                 // methods expecting a <T> value
             }
         }
         if (!multiLanguage) {
             if (value == null && !isAcceptsEmptyDefaultTranslations()) {
                 throw new FxInvalidParameterException("value", "ex.content.invalid.default.empty", getClass().getSimpleName()).asRuntimeException();
             }
             //override the single value
             if (singleValue == null || !singleValue.equals(value))
                 this.singleValue = value;
             this.singleValueEmpty = value == null;
             //noinspection unchecked
             return (TDerived) this;
         }
         if (translations == null) {
             //create an empty one, not yet initialized
             this.translations = new HashMap<Long, T>(5);
             this.emptyTranslations = new HashMap<Long, Boolean>(5);
         }
         if (language == FxLanguage.SYSTEM_ID)
             throw new FxInvalidParameterException("language", "ex.content.value.invalid.multilanguage.sys").asRuntimeException();
         if (value == null) {
             translations.remove(language);
             emptyTranslations.remove(language);
         } else {
             if (!value.equals(translations.get(language))) {
                 translations.put(language, value);
             }
             emptyTranslations.put(language, false);
         }
         //noinspection unchecked
         return (TDerived) this;
     }
 
     /**
      * Set the translation for a language or override the single language value if
      * this value is not flagged as multi language enabled
      *
      * @param lang        language to set the translation for
      * @param translation translation
      * @return this
      */
     public TDerived setTranslation(FxLanguage lang, T translation) {
         return setTranslation((int) lang.getId(), translation);
     }
 
     /**
      * For multilanguage values, set the default translation.
      * For single language values, set the value.
      *
      * @param value the value to be stored
      */
     public void setValue(T value) {
         setTranslation(getDefaultLanguage(), value);
     }
 
     /**
      * Set the translation in the default language. For single-language values,
      * sets the value.
      *
      * @param translation the default translation
      * @return this
      */
     public FxValue setDefaultTranslation(T translation) {
         return setTranslation(defaultLanguage, translation);
     }
 
     /**
      * Get the default language of this value
      *
      * @return default language
      */
     public long getDefaultLanguage() {
         if (!isMultiLanguage())
             return FxLanguage.SYSTEM_ID;
         return this.defaultLanguage;
     }
 
 
     /**
      * Returns the maximum input length an input field should have for this value
      * (or -1 for unlimited length).
      *
      * @return the maximum input length an input field should have for this value
      */
     public int getMaxInputLength() {
         return maxInputLength;
     }
 
 
     /**
      * Set the maximum input length for this value (-1 for unlimited length).
      *
      * @param maxInputLength the maximum input length for this value (-1 for unlimited length).
      */
     public void setMaxInputLength(int maxInputLength) {
         this.maxInputLength = maxInputLength;
     }
 
     /**
      * Set the default language.
      * It will only be set if a translation in the requested default language
      * exists!
      *
      * @param defaultLanguage requested default language
      */
     public void setDefaultLanguage(long defaultLanguage) {
         setDefaultLanguage(defaultLanguage, false);
     }
 
     /**
      * Set the default language. Will have no effect if the value is not multi language enabled
      *
      * @param defaultLanguage requested default language
      * @param force           if true, the default language will also be updated if no translation exists (for UI input)
      */
     public void setDefaultLanguage(long defaultLanguage, boolean force) {
         if (multiLanguage && (force || translationExists(defaultLanguage))) {
             this.defaultLanguage = defaultLanguage;
         }
     }
 
     /**
      * Reset the default language to the system language
      */
     public void clearDefaultLanguage() {
         this.defaultLanguage = FxLanguage.SYSTEM_ID;
     }
 
     /**
      * Is a default value set for this FxValue?
      *
      * @return default value set
      */
     public boolean hasDefaultLanguage() {
         return defaultLanguage != FxLanguage.SYSTEM_ID && isMultiLanguage();
     }
 
     /**
      * Check if the passed language is the default language
      *
      * @param language the language to check
      * @return passed language is the default language
      */
     public boolean isDefaultLanguage(long language) {
        return hasDefaultLanguage() && language == defaultLanguage;
     }
 
     /**
      * Remove the translation for the given language
      *
      * @param language the language to remove the translation for
      */
     public void removeLanguage(long language) {
         if (!multiLanguage) {
             setEmpty();
             // ensure that the old value is not "leaked" to clients that don't check isEmpty()
             // and that the behaviour is consistent with multi-language inputs (FX-485)
             singleValue = getEmptyValue();
         } else {
             translations.remove(language);
             emptyTranslations.remove(language);
         }
     }
 
     /**
      * Is this value available for multiple languages?
      *
      * @return value available for multiple languages
      */
     public boolean isMultiLanguage() {
         return this.multiLanguage;
     }
 
     protected boolean isAcceptsEmptyDefaultTranslations() {
         return true;
     }
 
     /**
      * Format this FxValue for inclusion in a SQL statement. For example,
      * a string is wrapped in single quotes and escaped properly (' --> '').
      * For multilanguage values the default translation is used. If the value is
      * empty (@link #isEmpty()), a runtime exception is thrown.
      *
      * @return the formatted value
      */
     public String getSqlValue() {
         if (isEmpty()) {
             throw new FxInvalidStateException("ex.content.value.sql.empty").asRuntimeException();
         }
         return FxFormatUtils.escapeForSql(getDefaultTranslation());
     }
 
     /**
      * Returns an empty value object for this FxValue type.
      *
      * @return  an empty value object for this FxValue type.
      */
     public abstract T getEmptyValue();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         // format value in the current user's locale - also used in the JSF UI
         return FxValueRendererFactory.getInstance().format(this);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object other) {
         if (this == other) return true;
         if (other == null) return false;
         if (this.getClass() != other.getClass()) return false;
         FxValue<?, ?> otherValue = (FxValue<?, ?>) other;
         if (this.isEmpty() != otherValue.isEmpty()) return false;
         if (this.isMultiLanguage() != otherValue.isMultiLanguage()) return false;
         if (multiLanguage) {
             if (!ArrayUtils.isEquals(this.translations.keySet().toArray(new Long[this.translations.keySet().size()]),
                     otherValue.translations.keySet().toArray(new Long[otherValue.translations.keySet().size()])))
                 return false;
             if (!ArrayUtils.isEquals(this.translations.values().toArray(),
                     otherValue.translations.values().toArray())) return false;
         } else {
             if (!this.isEmpty())
                 if (!this.singleValue.equals(otherValue.singleValue)) return false;
         }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         int hash = 7;
         if (translations != null) {
             for (T translation : translations.values()) {
                 hash = 31 * hash + translation.hashCode();
             }
             for (long language : translations.keySet()) {
                 hash = 31 * hash + (int) language;
             }
         }
         hash = 31 * hash + (int) defaultLanguage;
         return hash;
     }
 
     /**
      * A generic comparable implementation based on the value's string representation.
      *
      * @param o the other object
      * @return see {@link Comparable#compareTo}.
      */
     @SuppressWarnings({"unchecked"})
     public int compareTo(FxValue o) {
         if (isEmpty() && !o.isEmpty()) {
             return -1;
         }
         if (isEmpty() && o.isEmpty()) {
             return 0;
         }
         if (!isEmpty() && o.isEmpty()) {
             return 1;
         }
         final String value = getStringValue(getBestTranslation());
         final String oValue = o.getStringValue(o.getBestTranslation());
         if (value == null && oValue == null) {
             return 0;
         } else if (value == null) {
             return -1;
         } else if (oValue == null) {
             return 1;
         } else {
             return FxSharedUtils.getCollator().compare(value, oValue);
         }
     }
 }
 
