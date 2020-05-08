 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
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
 
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxValueChangeListener;
 import com.flexive.shared.content.FxValueChangeListener.ChangeType;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxInvalidStateException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.value.renderer.FxValueRendererFactory;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.primitives.Longs;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 /**
  * Abstract base class of all value objects.
  * Common base classed is used for multilingual properties, etc.
  * <p/>
  * To check if a value is empty a flag is used for each language resp. the single value.
  * Use the setEmpty() method to explicitly set a value to be empty
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @SuppressWarnings("UnusedDeclaration")
 public abstract class FxValue<T, TDerived extends FxValue<T, TDerived>> implements Serializable, Comparable<FxValue> {
     private static final long serialVersionUID = -5005063788615664383L;
 
     public static final boolean DEFAULT_MULTILANGUAGE = true;
     public static final Integer VALUE_NODATA = null;
 
     private static final long[] SYSTEM_LANG_ARRAY = new long[]{FxLanguage.SYSTEM_ID};
     private static final EmptyTranslation EMPTY_TRANSLATION = new EmptyTranslation();
 
     protected long defaultLanguage = FxLanguage.SYSTEM_ID;
     private String XPath = "", xpathPrefix = "";
     private Integer valueData = VALUE_NODATA;
     private FxValueChangeListener changeListener = null;
 
 
     /**
      * Data if <code>multiLanguage</code> is enabled
      */
     protected Map<Long, T> translations;
     
     /**
      * Value data for each language
      */
     protected Map<Long, Integer> multiLangData = null;
      
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
         this.readOnly = false;
         if (multiLanguage) {
             if (translations == null) {
                 //valid to pass null, create an empty one
                 this.translations = new HashMap<Long, T>(4);
             } else {
                 this.translations = new HashMap<Long, T>(translations);
             }
             if (this.defaultLanguage < 0) {
                 this.defaultLanguage = FxLanguage.SYSTEM_ID;
             }
         } else {
             if (translations != null && !translations.isEmpty()) {
                 //a translation is provided, use the defaultLanguage element or very first element if not present
                 singleValue = removeEmptyMark(translations.get(defaultLanguage));
                 if (singleValue == null)
                     singleValue = translations.values().iterator().next();
             }
             this.defaultLanguage = FxLanguage.SYSTEM_ID;
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
         this.defaultLanguage = defaultLanguage;
         this.readOnly = false;
         if (multiLanguage) {
             this.translations = new HashMap<Long, T>(4);
             if (this.defaultLanguage < 0) {
                 this.defaultLanguage = FxLanguage.SYSTEM_ID;
             }
         } else {
             this.defaultLanguage = FxLanguage.SYSTEM_ID;
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
         if (translations == null)
             return;
         for (Entry<Long, T[]> e : translations.entrySet())
             if (e.getValue()[pos] != null)
                 this.translations.put(e.getKey(), e.getValue()[pos]);
             else
                 this.translations.put(e.getKey(), null);
     }
 
     /**
      * Constructor
      *
      * @param multiLanguage   multilanguage value?
      * @param defaultLanguage the default language and the language for the value
      * @param value           single initializing value
      */
     protected FxValue(boolean multiLanguage, long defaultLanguage, T value) {
         //noinspection RedundantCast
         this(multiLanguage, defaultLanguage, (Map<Long, T>) null);
         if (value == null) {
             if(multiLanguage)
                 markEmpty(defaultLanguage);
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
         this.xpathPrefix = clone.xpathPrefix;
         this.valueData = clone.valueData;
         if(clone.multiLangData != null)
             this.multiLangData = Maps.newHashMap(clone.multiLangData);
         this.changeListener = clone.changeListener;
         if (clone.isImmutableValueType()) {
             if (clone.isMultiLanguage()) {
                 // clone only hashmap
                 this.translations = new HashMap(clone.translations);
             } else {
                 this.singleValue = clone.singleValue;
                 this.singleValueEmpty = clone.singleValueEmpty;
             }
         } else {
             if (clone.isMultiLanguage()) {
                 for (long k : clone.translations.keySet()) {
                     T t = removeEmptyMark(clone.translations.get(k));
                     if (t == null) {
                         this.translations.put(k, null);
                     } else {
                         this.translations.put(k, t == null ? null : copyValue(t));
                     }
                 }
             } else {
                 this.singleValue = clone.singleValue == null ? null : copyValue(clone.singleValue);
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
         return xpathPrefix == null || xpathPrefix.length() == 0 ? XPath : xpathPrefix + XPath;
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
             this.XPath = XPath != null ? XPathElement.xpToUpperCase(XPath) : null;
             this.xpathPrefix = "";
         }
         return (TDerived) this;
     }
 
     /**
      * Set the XPath (unless readonly)
      *
      * @param xpathPrefix   the xpath prefix (e.g. instance PK or type)
      * @param xpath         the XPath
      * @return  this
      * @since 3.2.0
      */
     @SuppressWarnings("unchecked")
     public TDerived setXPath(String xpathPrefix, String xpath) {
         if (!this.readOnly) {
             this.XPath = StringUtils.isBlank(xpath) ? "" : XPathElement.xpToUpperCase(xpath);
             this.xpathPrefix = StringUtils.isBlank(xpathPrefix) ? "" : XPathElement.xpToUpperCase(xpathPrefix);
         }
         return (TDerived) this;
     }
 
     /**
      * Copy the internal "meta" properties associated with a value into this instance (XPath, value data).
      *
      * @param other    the instance to copy the data from
      * @param copyValueData if the value data should be copied as well (optional since technically the valueData is
      *                      not an internal bookkeeping property, but associated with the wrapped value).
      * @since 3.2.1
      */
     @SuppressWarnings("unchecked")
     public void setInternalProperties(FxValue other, boolean copyValueData) {
         this.XPath = other.XPath;
         this.xpathPrefix = other.xpathPrefix;
         this.readOnly = other.readOnly;
         this.changeListener = other.changeListener;
         if (copyValueData) {
             this.valueData = other.valueData;
             this.multiLangData = other.multiLangData == null ? null : Maps.<Long, Integer>newHashMap(other.multiLangData);
         }
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
         if (isMultiLanguage()) {
             for (Long lang : this.translations.keySet())
                 markEmpty(lang);
         } else {
             this.singleValueEmpty = true;
         }
         if (this.changeListener != null) {
             this.changeListener.onValueChanged(getXPath(), ChangeType.Remove);
         }
         return (TDerived) this;
     }
 
     /**
      * Mark the entry for the given language as empty
      *
      * @param language the language to flag as empty
      */
     public void setEmpty(long language) {
         if (isMultiLanguage()) {
             markEmpty(language);
         } else {
             this.singleValueEmpty = true;
         }
         if (this.changeListener != null) {
             if (isEmpty()) {
                 this.changeListener.onValueChanged(getXPath(), ChangeType.Remove);
             }
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
      * Convert from a portable (not locale specific format)
      *
      * @param value portable string value to be evaluated
      * @return the value interpreted as T
      * @since 3.1.6
      */
     public T fromPortableString(String value) {
         return fromString(value);
     }
 
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
      * Converts the given instance of T to a string that can be
      * parsed again by {@link FxValue#fromPortableString(String)}.
      *
      * @param value the value to be converted
      * @return a string representation of the given value that can be parsed again using
      *         {@link FxValue#fromPortableString(String)}.
      * @since 3.1.6
      */
     public String getPortableStringValue(T value) {
         return getStringValue(value);
     }
 
     /**
      * Creates a copy of the given object (useful if the actual type is unknown).
      *
      * @return a copy of the given object (useful if the actual type is unknown).
      */
     public abstract TDerived copy();
 
     /**
      * Implement this method for data types that return false from {@link #isImmutableValueType()}.
      *
      * <p>
      *     The default implementation returns the argument as-is and throws an IllegalArgumentException
      *     when the container class has mutable value types.
      * </p>
      *
      * @param value    the value to be copied (not null)
      * @return  an independent copy of {@code value}
      * @since 3.2.0
      */
     protected T copyValue(T value) {
         if (!isImmutableValueType()) {
             throw new IllegalArgumentException("Mutable datatype, but no implementation of copyValue provided: " + getClass());
         }
         return value;
     }
 
     /**
      * Return true if T is immutable (e.g. java.lang.String). This prevents cloning
      * of the translations in copy constructors.
      *
      * @return true if T is immutable (e.g. java.lang.String)
      */
     public boolean isImmutableValueType() {
         return false;
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
         return _getErrorValue() == null;
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
         final T val = _getErrorValue();
         if (val == null) {
             // no error value is defined
             throw new IllegalStateException();
         }
         return val;
     }
 
     /**
      * Return the value that failed to validate.
      *
      * @return  the error value, or null if this FxValue instance is valid.
      */
     private T _getErrorValue() {
         if (isMultiLanguage()) {
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
         return null;
     }
 
     /**
      * Get a representation of this value in the default translation
      *
      * @return T
      */
     public T getDefaultTranslation() {
         if (!isMultiLanguage())
             return singleValue;
         T def = getTranslation(getDefaultLanguage());
         if (def != null)
             return def;
         if (translations.size() > 0)
             return removeEmptyMark(translations.values().iterator().next()); //first available translation if default does not exist
         return null; //empty as last fallback
     }
 
     /**
      * Get the translation for a requested language
      *
      * @param lang requested language
      * @return translation or an empty String if it does not exist
      */
     public T getTranslation(long lang) {
         return (isMultiLanguage() ? removeEmptyMark(translations.get(lang)) : singleValue);
     }
 
     /**
      * Get a String representation of this value in the requested language or
      * an empty String if the translation does not exist
      *
      * @param lang requested language id
      * @return T translation
      */
     public T getTranslation(FxLanguage lang) {
         if (!isMultiLanguage()) //redundant but faster
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
         if (!isMultiLanguage()) //redundant but faster
             return singleValue;
         T ret = getTranslation(lang);
         return ret != null ? ret : getDefaultTranslation();
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
         if (!isMultiLanguage())     //redundant but faster
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
         if (!isMultiLanguage()) //redundant but faster
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
         if (!isMultiLanguage()) //redundant but faster
             return singleValue;
         return getBestTranslation(FxContext.getUserTicket().getLanguage());
     }
 
     /**
      * Get all languages for which translations exist
      *
      * @return languages for which translations exist
      */
     public long[] getTranslatedLanguages() {
         if (isMultiLanguage()) {
             final List<Long> languages = Lists.newArrayListWithCapacity(translations.size());
             for (Entry<Long, T> entry : translations.entrySet()) {
                 if (!valueEmpty(entry.getValue())) {
                     languages.add(entry.getKey());
                 }
             }
             return Longs.toArray(languages);
         } else {
             return SYSTEM_LANG_ARRAY.clone();
         }
     }
 
     /**
      * Does a translation exist for the given language?
      *
      * @param languageId language to query
      * @return translation exists
      */
     public boolean translationExists(long languageId) {
         return !isMultiLanguage() || (translations.get(languageId) != null && !isMarkedEmpty(languageId));
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
         if (isMultiLanguage()) {
             if (translations.isEmpty()) {
                 return true;
             }
             for (Long lang : translations.keySet()) {
                 if (!isMarkedEmpty(lang)) {
                     return false;
                 }
             }
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
         if (!isMultiLanguage())
             return singleValueEmpty;
         return isMarkedEmpty(lang);
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
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public final TDerived setTranslation(long language, T value) {
         if (value instanceof FxValue) {
             throw new FxInvalidParameterException("value", "ex.content.invalid.translation.fxvalue",
                     value.getClass().getCanonicalName()).asRuntimeException();
         }
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
         FxValueChangeListener.ChangeType change = null;
         final String xpath = getXPath();
         if (!isMultiLanguage()) {
             if (value == null && !isAcceptsEmptyDefaultTranslations()) {
                 throw new FxInvalidParameterException("value", "ex.content.invalid.default.empty", getClass().getSimpleName()).asRuntimeException();
             }
             if (StringUtils.isNotBlank(xpath) && this.changeListener != null) {
                 if (value != null) {
                     if (this.singleValueEmpty)
                         change = FxValueChangeListener.ChangeType.Add;
                     else if (!this.singleValue.equals(value))
                         change = FxValueChangeListener.ChangeType.Update;
                 } else if (!this.singleValueEmpty) {
                     change = FxValueChangeListener.ChangeType.Remove;
                 }
             }
             //override the single value
             if (singleValue == null || !singleValue.equals(value))
                 this.singleValue = value;
             this.singleValueEmpty = value == null;
             if (changeListener != null && change != null)
                 changeListener.onValueChanged(xpath, change);
             //noinspection unchecked
             return (TDerived) this;
         }
         if (translations == null) {
             //create an empty one, not yet initialized
             this.translations = new HashMap<Long, T>(4);
         }
         if (language == FxLanguage.SYSTEM_ID)
             throw new FxInvalidParameterException("language", "ex.content.value.invalid.multilanguage.sys").asRuntimeException();
         if (StringUtils.isNotBlank(xpath) && this.changeListener != null && value != null) {
             if (this.isEmpty())
                 change = FxValueChangeListener.ChangeType.Add;
             else {
                 if (!value.equals(translations.get(language)))
                     change = FxValueChangeListener.ChangeType.Update;
             }
         }
         boolean wasEmpty = this.changeListener != null && isEmpty(); //only evaluate if we have a change listener attached
         if (value == null) {
             translations.remove(language);
         } else {
             if (!value.equals(translations.get(language))) {
                 translations.put(language, value);
             }
         }
         if (StringUtils.isNotBlank(xpath) && this.changeListener != null && value == null && !wasEmpty && isEmpty()) {
             change = FxValueChangeListener.ChangeType.Remove;
         }
         if (changeListener != null && change != null)
             changeListener.onValueChanged(xpath, change);
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
         final String xp = getXPath();
         if (StringUtils.isBlank(xp)) {
             return -1;
         } else {
             try {
                 final FxPropertyAssignment pa = CacheAdmin.getEnvironment().getPropertyAssignment(xp);
                 final int maxLength = pa.getMaxLength();
                 if (maxLength > 0) {
                     return maxLength;
                 }
                 final FxDataType dataType = pa.getProperty().getDataType();
                 return dataType == FxDataType.String1024 ? 1024 : -1;
             } catch (FxRuntimeException e) {
                 return -1;
             }
         }
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
         if (isMultiLanguage() && (force || translationExists(defaultLanguage))) {
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
         return !isMultiLanguage() && language == FxLanguage.SYSTEM_ID ||
                 hasDefaultLanguage() && language == defaultLanguage;
     }
 
     /**
      * Remove the translation for the given language
      *
      * @param language the language to remove the translation for
      */
     public void removeLanguage(long language) {
         if (!isMultiLanguage()) {
             setEmpty();
             // ensure that the old value is not "leaked" to clients that don't check isEmpty()
             // and that the behaviour is consistent with multi-language inputs (FX-485)
             singleValue = getEmptyValue();
         } else {
             translations.remove(language);
         }
     }
 
     /**
      * Is this value available for multiple languages?
      *
      * @return value available for multiple languages
      */
     public boolean isMultiLanguage() {
         return this.translations != null;
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
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
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
         if (!equalsValueData(otherValue))
             return false;
         if (this.isMultiLanguage() != otherValue.isMultiLanguage()) return false;
         if (isMultiLanguage()) {
             for (Entry<Long, T> entry : this.translations.entrySet()) {
                 final Long key = entry.getKey();
                 if (valueEmpty(entry.getValue())) {
                     if (!valueEmpty(otherValue.translations.get(key))) {
                         return false;
                     }
                 } else {
                     final T value = entry.getValue();
                     if (!value.equals(otherValue.translations.get(key))) {
                         return false;
                     }
                 }
             }
             for (Entry<Long, ?> otherEntry : otherValue.translations.entrySet()) {
                 if (!valueEmpty(otherEntry.getValue()) && isTranslationEmpty(otherEntry.getKey())) {
                     return false;
                 }
             }
         } else {
             if (!this.isEmpty())
                 if (!this.singleValue.equals(otherValue.singleValue)) return false;
         }
         return true;
     }
 
     /**
      * Compare value data with another FxValue for equality
      *
      * @param otherValue other FxValue to compare
      * @return equal
      */
     private boolean equalsValueData(FxValue<?, ?> otherValue) {
         if(hasValueData() != otherValue.hasValueData())
             return false;
         if (isMultiLanguage() != otherValue.isMultiLanguage())
             return false;
         if(!hasValueData())
             return true;
         if (isMultiLanguage()) {
            // check if value data is equal, taking into account that the "default value" (the main valuData field) can
             // be used as fallback. The other direction (otherValue -> this) does not need to be checked, because if the number
             // of translations does not match, the equality check will return false anyway.
 
             final Map<Long, Integer> thisML = multiLangData != null ? multiLangData : Collections.<Long, Integer>emptyMap();
             final Map<Long, Integer> otherML = otherValue.multiLangData != null ? otherValue.multiLangData : Collections.<Long, Integer>emptyMap();
             for (Entry<Long, Integer> entry : thisML.entrySet()) {
                 final Integer value = entry.getValue();
                 final Integer other = otherML.get(entry.getKey());
                if (value != null && value.equals(valueData)) {
                    if (other != null && !value.equals(other)) {
                        return false;
                    }
                 }
             }
             return true;
         } else {
             return valueData.equals(otherValue.valueData);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         int hash = 7;
         if (translations != null)
             hash = 31 * hash + translations.hashCode();
         hash = 31 * hash + (int) defaultLanguage;
         if (hasValueData()) {
             if (isMultiLanguage())
                 for (Integer val : multiLangData.values())
                     hash += val;
             else
                 hash += getValueDataRaw();
         }
         return hash;
     }
 
     /**
      * A generic comparable implementation based on the value's string representation.
      *
      * @param o the other object
      * @return see {@link Comparable#compareTo}.
      */
     @Override
     @SuppressWarnings({"unchecked", "NullableProblems"})
     public int compareTo(FxValue o) {
         if (o == null) {
             return 1;
         }
         if (isEmpty() && !o.isEmpty()) {
             return -1;
         }
         if (isEmpty()) {
             return 0;
         }
         if (o.isEmpty()) {
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
 
     /**
      * Get attached value data (optional, if not set will return <code>VALUE_NODATA</code>).
      * As value data might contain some bit-coded flags in the future, it is not certain if the full Integer range
      * will be available as some bits might be masked out.
      *
      * @return attached value data, if not set will return <code>VALUE_NODATA</code>
      * @since 3.1.4
      */
     public Integer getValueData() {
         return getValueDataRaw();
     }
 
     /**
      * Get attached value data (optional, if not set will return <code>VALUE_NODATA</code>) including any bit-coded flags.
      * Internal use only!
      *
      * @return raw attached value data, if not set will return <code>VALUE_NODATA</code>
      * @since 3.1.4
      */
     public Integer getValueDataRaw() {
         if(isMultiLanguage()) {
             if(multiLangData == null || multiLangData.isEmpty())
                 return valueData;
             //fall back to the first available entry
             return multiLangData.values().iterator().next();
         }
         return valueData;
     }
 
     /**
      * Get attached language specific value data (optional, if not set will return <code>VALUE_NODATA</code>) including any bit-coded flags.
      * Internal use only!
      *
      * @param language fetch the value data for this language
      * @return raw attached value data, if not set will return <code>VALUE_NODATA</code>
      * @since 3.2
      */
     public Integer getValueDataRaw(long language) {
         if(isMultiLanguage()) {
             if(multiLangData == null || !multiLangData.containsKey(language))
                 return valueData;
             return multiLangData.get(language);
         } else
             return valueData;
     }
 
     /**
      * Attach additional data to this value instance
      *
      * @param valueData value data to attach
      * @return this
      * @since 3.1.4
      */
     @SuppressWarnings({"unchecked"})
     public TDerived setValueData(Integer valueData) {
         if (isMultiLanguage()) {
             if (this.translations != null) {
                 // keep "global" flag for languages that may be added in the future
                 this.valueData = valueData;
                 if (valueData == null) {
                     this.multiLangData = null;
                 } else {
                     if (this.multiLangData != null) {
                         // replace existing "custom" values
                         for (Entry<Long, Integer> entry : multiLangData.entrySet()) {
                             entry.setValue(valueData);
                         }
                     }
 
                 }
             }
         } else
             this.valueData = valueData;
         return (TDerived) this;
     }
 
     /**
      * Attach additional data per language to this value instance
      *
      * @param language language to attach data for
      * @param valueData value data to attach
      * @return this
      * @since 3.2.1
      */
     public TDerived setValueData(long language, Integer valueData) {
         return setValueData(language, valueData, false);
     }
 
     /**
      * Attach additional data per language to this value instance
      *
      * @param language language to attach data for
      * @param valueData value data to attach
      * @param allowUseAsFallback if the value may be used as a fallback for other languages (only relevant when no other value data is set)
      * @return this
      * @since 3.2.1
      */
     public TDerived setValueData(long language, Integer valueData, boolean allowUseAsFallback) {
         if (isMultiLanguage()) {
             if (multiLangData != null && multiLangData.containsKey(language)) {
                 // clean reset of the default value to avoid losing the association with
                 // the fallback valueData instance - which needs to be cleared if this value was the last one
                 clearValueData(language);
             }
             if (this.valueData == null && allowUseAsFallback) {
                 this.valueData = valueData;
             }
             if (valueData != null) {
                 final Integer specValue = multiLangData != null ? multiLangData.get(language) : null;
                 if (specValue != null) {
                     // replace existing value
                     multiLangData.put(language, valueData);
                 } else if (!valueData.equals(this.valueData) || multiLangData == null || multiLangData.isEmpty()) {
                     // store only "non-default" flags (and the initial value) in the hashmap
                     if (multiLangData == null)
                         multiLangData = Maps.newHashMap();
                     multiLangData.put(language, valueData);
                 }
             }
         } else
             this.valueData = valueData;
         //noinspection unchecked
         return (TDerived) this;
     }
 
     /**
      * Unset value data
      * @since 3.1.4
      */
     public void clearValueData() {
         this.valueData = VALUE_NODATA;
         this.multiLangData = null;
     }
 
     /**
      * Unset value data for a language
      * @param language language to clear value data for
      * @since 3.2
      */
     public void clearValueData(long language) {
         if(this.multiLangData != null && this.multiLangData.containsKey(language)) {
             final Integer oldValue = this.multiLangData.remove(language);
             if (this.multiLangData.isEmpty()) {
                 this.multiLangData = null;
                 if (oldValue != null && oldValue.equals(this.valueData)) {
                     this.valueData = VALUE_NODATA;
                 }
             }
         } else if (multiLangData == null || multiLangData.isEmpty()) {
             // reset only when there are no other values
             this.multiLangData = null;
             this.valueData = VALUE_NODATA;
         }
     }
 
 
     /**
      * Are additional value data set for this value instance?
      *
      * @return value data set
      * @since 3.1.4
      */
     public boolean hasValueData() {
         return this.valueData != null || (isMultiLanguage() && multiLangData != null && !multiLangData.isEmpty());
     }
 
     /**
      * Are additional value data set for this value instance in the requested language?
      *
      * @return value data set
      * @since 3.2
      */
     public boolean hasValueData(long language) {
         return isMultiLanguage()
                 ? valueData != null || (multiLangData != null && multiLangData.containsKey(language) && multiLangData.get(language) != null)
                 : valueData != null;
     }
 
     /**
      * Set the change listener
      *
      * @param changeListener change listener
      * @since 3.1.6
      */
     public void setChangeListener(FxValueChangeListener changeListener) {
         this.changeListener = changeListener;
     }
 
     @SuppressWarnings("unchecked")
     private void markEmpty(Long language) {
         ((Map) translations).put(language, EMPTY_TRANSLATION);
     }
 
     private boolean isMarkedEmpty(Long language) {
         final Object value = ((Map) translations).get(language);
         return valueEmpty(value);
     }
 
     private boolean valueEmpty(Object value) {
         return value == null || value instanceof EmptyTranslation;
     }
 
     @SuppressWarnings("unchecked")
     private T removeEmptyMark(Object value) {
         return value instanceof EmptyTranslation ? null : (T) value;
     }
 
     private static class EmptyTranslation implements Serializable {
     }
 }
 
