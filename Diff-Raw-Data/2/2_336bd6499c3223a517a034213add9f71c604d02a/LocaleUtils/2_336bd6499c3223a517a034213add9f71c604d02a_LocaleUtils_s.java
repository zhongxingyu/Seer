 package org.smartly.commons.util;
 
 import java.text.DecimalFormatSymbols;
 import java.util.*;
 
 public class LocaleUtils {
 
     public static final Locale DEFAULT = new Locale("en", "US");
     private final static InheritableThreadLocal<Locale> _thdLocale = new InheritableThreadLocal<Locale>();
     /**
      * Locales that are found so far.
      */
     private static final Map<Locale, Locale> _founds = Collections.synchronizedMap(
             new LinkedHashMap<Locale, Locale>(18));
 
     static {
         final Locale[] ls = new Locale[]{
                 Locale.UK, Locale.ENGLISH,
                 Locale.US,
                 Locale.JAPAN, Locale.JAPANESE,
                 Locale.KOREA, Locale.KOREAN,
                 Locale.FRANCE, Locale.FRENCH,
                 Locale.GERMANY, Locale.GERMAN,
                 Locale.ITALY, Locale.ITALIAN,
                 Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE,
                 Locale.CHINA, Locale.CHINESE
         };
         for (int j = 0; j < ls.length; ++j) {
             _founds.put(ls[j], ls[j]);
         }
     }
 
     /**
      * Returns the current locale; never null.
      * This is the locale that every other objects shall use,
      * unless they have special consideration.
      * <p>Default: If {@link #setThreadLocal} was called with non-null,
      * the value is returned. Otherwise, Locale.getDefault() is returned,
      */
     public static Locale getCurrent() {
         final Locale l = (Locale) _thdLocale.get();
         return l != null ? l : Locale.getDefault();
     }
 
     public static void setCurrent(final Locale locale) {
         if (null != locale) {
             _thdLocale.set(locale);
             if (Locale.getDefault() != locale) {
                 Locale.setDefault(locale);
             }
         }
     }
 
     /**
      * Returns whether the current locale ({@link #getCurrent}) belongs
      * to the specified language and/or country.
      *
      * @param lang    the language code, e.g., en and zh. Ignored if null.
      * @param country the country code, e.g., US. Ignored if null.
      *                If empty, it means no country code at all.
      */
     public static boolean testCurrent(String lang, String country) {
         final Locale l = getCurrent();
         return (lang == null || lang.equals(l.getLanguage())) && (country == null || country.equals(l.getCountry()));
     }
 
     /**
      * Sets the locale for the current thread only.
      * <p/>
      * <p>Each thread could have an independent locale, called
      * the thread locale.
      * <p/>
      * <p>When Invoking this method under a thread that serves requests,
      * remember to clean up the setting upon completing each request.
      * <p/>
      * <pre><code>Locale old = Locales.setThreadLocal(newValue);
      * try {
      *  ...
      * } finally {
      *  Locales.setThreadLocal(old);
      * }</code></pre>
      *
      * @param locale the thread locale; null to denote no thread locale
      * @return the previous thread locale
      */
     @SuppressWarnings("unchecked")
     public static Locale setThreadLocal(Locale locale) {
         final Locale old = (Locale) _thdLocale.get();
         _thdLocale.set(locale);
         return old;
     }
 
     /**
      * Converts a Locale to one of them being used before.
      * To save memory (since locale is used frequently), it is suggested
      * to pass thru this method after creating a new instance of Locale.<br>
      * Example, getLocale(new Locale(...)).
      * <p/>
      * <p>This method first look for any locale
      */
     public static Locale getLocale(final Locale locale) {
         synchronized (_founds) {
             final Locale l = _founds.get(locale);
             if (l != null) {
                 return l;
             }
 
             _founds.put(locale, locale);
             return locale;
         }
     }
 
     public static Locale getLocale(final String lang, final String country) {
         if (StringUtils.hasText(lang)) {
             if (StringUtils.hasText(country)) {
                 return getLocale(new Locale(lang, country));
             } else {
                 return getLocaleByLang(lang);
             }
         }
         return DEFAULT;
     }
 
     public static Locale getLocaleByLang(final String lang) {
         if (StringUtils.hasText(lang)) {
             final Set<Locale> keys = _founds.keySet();
             for (final Locale locale : keys) {
                 if (locale.getLanguage().equalsIgnoreCase(lang)) {
                     return _founds.get(locale);
                 }
             }
         }
        return DEFAULT;
     }
 
     public static Locale getLocaleByCountry(final String country) {
         if (StringUtils.hasText(country)) {
             final Set<Locale> keys = _founds.keySet();
             for (final Locale locale : keys) {
                 if (locale.getCountry().equalsIgnoreCase(country)) {
                     return _founds.get(locale);
                 }
             }
         }
         return DEFAULT;
     }
 
     public static String getLanguage(final String slocale) {
         final String[] tokens = StringUtils.split(slocale, "_");
         if (tokens.length > 0) {
             return tokens[0];
         } else {
             final Locale locale = LocaleUtils.getLocaleFromString(slocale);
             return locale.getLanguage();
         }
     }
 
     /**
      * Return Country from locale even if passed locale has not a country declared.
      *
      * @param locale Locale can have only Language. i.e. "it"
      * @return Country for passed locale. i.e. getCountry(new Locale("it")) returns "IT"
      */
     public static Locale getCountry(final Locale locale) {
         Locale result = null;
         final String country = locale.getCountry();
         final String language = locale.getLanguage();
         if (StringUtils.hasText(country)) {
             result = LocaleUtils.getLocale(locale);
         } else {
             // loop on all available locales searching for best matching
             final Locale[] locales = Locale.getAvailableLocales();
             for (final Locale item : locales) {
                 final String itemcountry = item.getCountry();
                 final String itemlang = item.getLanguage();
                 if (StringUtils.hasText(itemcountry)
                         && itemlang.equalsIgnoreCase(language)) {
                     result = LocaleUtils.getLocale(item);
                     if (itemcountry.equalsIgnoreCase(itemlang)) {
                         break;
                     }
                 }
             }
         }
         return null != result
                 ? result
                 : locale;
     }
 
     /**
      * Create a new Locale from passed string.<br>
      * Allow a string with language-country-variant, ex: "it-IT", "en", "en-US", "th_TH_TH"
      * If passed locale is not available, return Default Locale
      *
      * @param localeString A string like "it-IT", "en", "en-US", "th_TH_TH"
      * @return Locale from String
      */
     public static Locale getLocaleFromString(final String localeString) {
         Locale result = parseLocaleString(localeString);
 
         if (null == result) {
             result = getLocale(getCurrent());
         }
 
         return result;
     }
 
     /**
      * Parse the given locale string into a <code>java.util.Locale</code>.
      * This is the inverse operation of Locale's <code>toString</code>.
      *
      * @param localeString the locale string, following
      *                     <code>java.util.Locale</code>'s toString format ("en", "en_UK", etc).
      *                     Also accepts spaces ' ' as separators, as alternative to underscores '_', or ':' or '-'.
      * @return a corresponding Locale instance
      */
     public static Locale parseLocaleString(final String localeString) {
         if (StringUtils.hasText(localeString)) {
             final String[] parts = StringUtils.split(localeString, "_ -:", false, false);
             final String language = (parts.length > 0 ? parts[0] : "");
             final String country = (parts.length > 1 ? parts[1] : "");
             final String variant = (parts.length > 2 ? parts[2] : "");
 
             return (language.length() > 0 ? getLocale(new Locale(language, country, variant)) : null);
         }
         return getLocale(Locale.ENGLISH);
     }
 
     /**
      * Create a new Locale from passed string.<br>
      * Allow a string with language-country-variant, ex: "it-IT", "en", "en-US", "th_TH_TH"
      * If passed locale is not available, return 'defaultValue' parameter
      *
      * @param sLocale      A string like "it-IT", "en", "en-US", "th_TH_TH"
      * @param defaultValue A default Locale to return if passed string produce an invalid locale
      * @return Locale from String
      */
     public static Locale getLocaleFromString(final String sLocale,
                                              final Locale defaultValue) {
         Locale result = null;
         if (sLocale.indexOf("-") > -1) {
             result = getLocaleFromString(sLocale, "-", defaultValue);
         } else if (sLocale.indexOf("_") > -1) {
             result = getLocaleFromString(sLocale, "_", defaultValue);
         } else if (sLocale.indexOf(":") > -1) {
             result = getLocaleFromString(sLocale, ":", defaultValue);
         } else {
             result = getLocaleFromString(sLocale, " ", defaultValue);
         }
 
         if (null == result) {
             result = defaultValue;
         }
 
         return result;
     }
 
     /**
      * Create a new Locale from passed string.<br>
      * Allow a string with language-country-variant, ex: "it-IT", "en", "en-US", "th_TH_TH"
      * If passed locale is not available, return US (english)
      *
      * @param sLocale   A string like "it-IT", "en", "en-US", "th_TH_TH"
      * @param delimiter Separator of passed char ex: '-', ':', etc..
      * @return Locale from String
      */
     public static Locale getLocaleFromString(final String sLocale, final String delimiter) {
         Locale def = getLocale(getCurrent());
         return getLocaleFromString(sLocale, delimiter, def);
     }
 
     /**
      * Create a new Locale from passed string.<br>
      * Allow a string with language-country-variant, ex: "it-IT", "en", "en-US", "th_TH_TH".<br>
      * The sLocale parameter string can use a custom 'delimiter'.
      * If passed locale is not available, return 'defaultValue' parameter
      *
      * @param sLocale      A string like "it-IT", "en", "en-US", "th_TH_TH"
      * @param delimiter    Separator of passed string ex: "-", ":", etc..
      * @param defaultValue A default Locale to return if passed string produce an invalid locale
      * @return Locale from String
      */
     public static Locale getLocaleFromString(final String sLocale,
                                              final String delimiter, Locale defaultValue) {
         Locale result = null;
         String[] arr = sLocale.split(delimiter);
         if (arr.length == 1) {
             result = getLocale(new Locale(arr[0]));
         } else if (arr.length == 2) {
             result = getLocale(new Locale(arr[0], arr[1]));
         } else if (arr.length == 3) {
             result = getLocale(new Locale(arr[0], arr[1], arr[2]));
         }
         if (null == result) {
             result = defaultValue;
         }
 
         if (null == result) {
             result = getCurrent();
         }
 
         return result;
     }
 
     /**
      * Return Loacale from Object instance (Locale or String).
      *
      * @param locale
      * @param defaultValue
      * @return
      */
     public static Locale getLocaleFromObject(final Object locale,
                                              final Locale defaultValue) {
         if (null != locale) {
             if (locale instanceof Locale) {
                 return getLocale((Locale) locale);
             } else {
                 final String slocale = locale.toString();
                 return getLocaleFromString(slocale);
             }
         }
         return defaultValue;
     }
 
     /**
      * Check locale compatibility.
      *
      * @param locale1 First locale
      * @param locale2 Second locale
      * @return true if locales are near or equals.
      */
     public static boolean like(Locale locale1, Locale locale2) {
         return like(locale1.toString(), locale2.toString());
     }
 
     /**
      * Check locale compatibility.
      *
      * @param value1 First locale
      * @param value2 Second locale
      * @return true if locales are near or equals.
      */
     public static boolean like(final String value1,
                                final String value2) {
         if (!StringUtils.hasText(value1) && !StringUtils.hasText(value2)) {
             return true;
         }
         if (null == value1 || null == value2) {
             return false;
         }
         // equals?
         if (value1.equalsIgnoreCase(value2)) {
             return true;
         }
         if (StringUtils.hasText(value2)) {
             // value1 near value2?
             if (value1.startsWith(value2)) {
                 return true;
             }
             // value2 near value1?
             if (value2.startsWith(value1)) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isISOLanguage(final String language) {
         final Locale locale = new Locale(language);
         final String[] langs = Locale.getISOLanguages();
         for (String lang : langs) {
             if (lang.equalsIgnoreCase(locale.getLanguage())) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isISOCountry(final String language) {
         final Locale locale = new Locale(language);
         final String[] langs = Locale.getISOCountries();
         for (String lang : langs) {
             if (lang.equalsIgnoreCase(locale.getCountry())) {
                 return true;
             }
         }
         return false;
     }
 
     public static DecimalFormatSymbols getDecimalFormatSymbols() {
         final Locale locale = LocaleUtils.getCurrent();
         return getDecimalFormatSymbols(locale);
     }
 
     public static DecimalFormatSymbols getDecimalFormatSymbols(final Locale locale) {
         final Locale loc = LocaleUtils.getLocale(locale);
         final DecimalFormatSymbols result = new DecimalFormatSymbols(loc);
         return result;
     }
 
     public static Currency getCurrency() {
         final Locale locale = LocaleUtils.getCurrent();
         return LocaleUtils.getCurrency(locale);
     }
 
     /**
      * Returns the <code>Currency</code> instance for the country of the
      * given locale. The language and variant components of the locale
      * are ignored. The result may vary over time, as countries change their
      * currencies. For example, for the original member countries of the
      * European Monetary Union, the method returns the old national currencies
      * until December 31, 2001, and the Euro from January 1, 2002, local time
      * of the respective countries.
      * <p/>
      * The method returns <code>null</code> for territories that don't
      * have a currency, such as Antarctica or if the given locale
      * is not a supported ISO 3166 country code.
      *
      * @param locale the locale for whose country a <code>Currency</code>
      *               instance is needed
      * @return the <code>Currency</code> instance for the country of the given
      *         locale, or null
      */
     public static Currency getCurrency(final Locale locale) {
         try {
             final DecimalFormatSymbols dfs = getDecimalFormatSymbols(locale);
             final Currency currency = dfs.getCurrency();
             return currency;
         } catch (Throwable t) {
         }
         return null;
     }
 
     /**
      * Return currency code and currency symbol in unique formatted string.
      * i.e. "€(EUR)"
      *
      * @return
      */
     public static String getCurrencyAsString() {
         final Locale locale = LocaleUtils.getCurrent();
         return LocaleUtils.getCurrencyAsString(locale);
     }
 
     /**
      * Return currency code and currency symbol in unique formatted string.
      * i.e. "€(EUR)"
      *
      * @param locale
      * @return
      */
     public static String getCurrencyAsString(final Locale locale) {
         final Locale loc = LocaleUtils.getCountry(locale);
         final Currency currency = Currency.getInstance(loc);
         final String symbol = currency.getSymbol();
         final String code = currency.getCurrencyCode();
         if (StringUtils.hasText(symbol)
                 && !symbol.equalsIgnoreCase(code)) {
             return symbol + "(" + code + ")";
         } else {
             return code;
         }
     }
 
     public static String getCurrencySymbol() {
         final Locale locale = LocaleUtils.getCurrent();
         return LocaleUtils.getCurrencySymbol(locale);
     }
 
     public static String getCurrencySymbol(final Locale locale) {
         final Locale loc = LocaleUtils.getCountry(locale);
         final Currency currency = Currency.getInstance(loc);
         return currency.getSymbol();
     }
 
     public static String getCurrencySymbol(final String currencyCode) {
         final Currency currency = Currency.getInstance(currencyCode);
         return currency.getSymbol();
     }
 
     public static String getCurrencyCode() {
         final Locale locale = LocaleUtils.getCurrent();
         return LocaleUtils.getCurrencyCode(locale);
     }
 
     public static String getCurrencyCode(final Locale locale) {
         final Locale loc = LocaleUtils.getCountry(locale);
         final Currency currency = Currency.getInstance(loc);
         return currency.getCurrencyCode();
     }
 
     public static String getCurrencyDisplayName() {
         final Locale locale = LocaleUtils.getCurrent();
         return LocaleUtils.getCurrencyDisplayName(locale);
     }
 
     public static String getCurrencyDisplayName(final Locale locale) {
         final Locale loc = LocaleUtils.getCountry(locale);
         final Currency currency = Currency.getInstance(loc);
         return currency.getDisplayName();
     }
 
     public static String getTimeZoneID() {
         return TimeZone.getDefault().getID();
     }
 
     public static TimeZone getTimeZone() {
         return TimeZone.getDefault();
     }
 
     public static TimeZone getTimeZone(final String ID) {
         return TimeZone.getTimeZone(ID);
     }
 
     public static String getTimeZoneDisplay(final String ID) {
         final TimeZone tz = TimeZone.getTimeZone(ID);
         final Locale locale = LocaleUtils.getCurrent();
         return LocaleUtils.getTimeZoneDisplay(tz, locale);
     }
 
     public static String getTimeZoneDisplay(final String ID,
                                             final String langCode) {
         final TimeZone tz = TimeZone.getTimeZone(ID);
         final Locale locale = LocaleUtils.getLocaleFromString(langCode);
         return LocaleUtils.getTimeZoneDisplay(tz, locale);
     }
 
     public static String getTimeZoneDisplay(final TimeZone tz,
                                             final Locale locale) {
         final Locale loc = null != locale
                 ? locale
                 : LocaleUtils.getCurrent();
         return null != tz
                 ? tz.getDisplayName(loc)
                 : TimeZone.getDefault().getDisplayName(loc);
     }
 }
