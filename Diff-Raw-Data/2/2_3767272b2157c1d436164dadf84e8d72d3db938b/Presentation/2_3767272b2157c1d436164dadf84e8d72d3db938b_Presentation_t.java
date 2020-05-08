 /*
  * Copyrighted 2012-2013 Netherlands eScience Center.
  *
  * Licensed under the Apache License, Version 2.0 (the "License").  
  * You may not use this file except in compliance with the License. 
  * For details, see the LICENCE.txt file location in the root directory of this 
  * distribution or obtain the Apache License at the following location: 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  * 
  * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
  * ---
  */
 // source: 
 
 package nl.esciencecenter.ptk.presentation;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import nl.esciencecenter.ptk.data.StringList;
 
 /**
  * Custom Presentation and formatting methods to show values, attributes, etc. 
  * 
  * @author P.T. de Boer
  */
 public class Presentation
 {
     // =======================================================================
     // Static Fields
     // =======================================================================
 
     protected static Hashtable<String, Presentation> presentationStore = new Hashtable<String, Presentation>();
 
     // /** Default Attribute Name to show for VFSNodes */
     // public static String defaultFileAttributeNames[] =
     // {
     // ICON,
     // NAME,
     // RESOURCE_TYPE,
     // LENGTH,
     // // ATTR_MODIFICATION_TIME_STRING,
     // MODIFICATION_TIME,
     // MIMETYPE,
     // PERMISSIONS_STRING
     // };
 
     /** format number to 00-99 format */
     public static String to2decimals(long val)
     {
         String str = "";
 
         if (val < 0)
         {
             str = "-";
             val = -val;
         }
 
         if (val < 10)
             return str + "0" + val;
         else
             return str + val;
     }
 
     /**
      * Format number to 000-999 format.
      * If the number is bigger the 999 the string will be bigger also
      */
     public static String to3decimals(long val)
     {
         String str = "";
 
         if (val < 0)
         {
             str = "-";
             val = -val;
         }
 
         if (val < 10)
             return str = "00" + val;
         else if (val < 100)
             return str + "0" + val;
         else
             return str + val;
     }
     
     /** Format number to 0000-9999 format */
     public static String to4decimals(long val)
     {
         String str = "";
 
         if (val < 0)
         {
             str = "-";
             val = -val;
         }
 
         if (val < 10)
             return str = "000" + val;
         else if (val < 100)
             return str + "00" + val;
         else if (val < 1000)
             return str + "000" + val;
         else
             return str + val;
     }
     /**
      * Returns time string relative to current time in millis since 'epoch'. If,
      * for example, the date is 'today' it will print 'today hh:mm:ss' if the
      * year is this year, the year will be ommitted.
      * 
      * @param date
      * @return
      */
     public static String relativeTimeString(Date dateTime)
     {
         if (dateTime == null)
             return "?";
 
         long current = System.currentTimeMillis();
         GregorianCalendar now = new GregorianCalendar();
         now.setTimeInMillis(current);
 
         // convert to local timezone !
         TimeZone localTZ = now.getTimeZone();
         GregorianCalendar time = new GregorianCalendar();
         time.setTime(dateTime);
         time.setTimeZone(localTZ);
 
         String tstr = "";
 
         int y = time.get(GregorianCalendar.YEAR);
         int M = time.get(GregorianCalendar.MONTH);
         int D = time.get(GregorianCalendar.DAY_OF_MONTH);
         int cont = 0;
 
         if (y != now.get(GregorianCalendar.YEAR))
         {
             tstr = "" + y + " ";
             cont = 1;
         }
 
         if ((cont == 1) || (M != now.get(GregorianCalendar.MONTH)) || (D != now.get(GregorianCalendar.DAY_OF_MONTH)))
         {
             tstr = tstr + PresentationConst.getMonthNames()[M];
 
             tstr += " " + to2decimals(D);
         }
         else
         {
             tstr += "today ";
         }
 
         tstr += " " + to2decimals(time.get(GregorianCalendar.HOUR_OF_DAY)) + ":"
                 + to2decimals(time.get(GregorianCalendar.MINUTE)) + ":"
                 + to2decimals(time.get(GregorianCalendar.SECOND));
 
         // add timezone string:
         tstr += " (" + localTZ.getDisplayName(true, TimeZone.SHORT) + ")";
         return tstr;
     }
 
     /** @see getPresentationFor(String, String, String, boolean) */
     public static Presentation getPresentation(String key, boolean autoCreate)
     {
         synchronized (presentationStore)
         {
             Presentation pres = presentationStore.get(key);
             if (pres != null)
                 return pres;
 
             pres = Presentation.createDefault();
             presentationStore.put(key, pres);
             return pres;
         }
     }
 
     public static Presentation getPresentationForSchemeType(String scheme, String type, Boolean autoCreate)
     {
         return getPresentation(createKey(scheme,null, type), autoCreate);
     }
 
     public static Presentation getPresentationFor(String scheme, String host, String type, boolean autoCreate)
     {
         return getPresentation(createKey(scheme,host, type), autoCreate);
     }
     
     protected static String createKey(String scheme, String host, String type)
     {
         if (scheme == null)
             scheme = "";
         if (type == null)
             type = "";
         if (host==null)
             host= "";
 
         return scheme + "-" + host+"-"+type;
     }
 
     public static void storeSchemeType(String scheme, String type, Presentation pres)
     {
         store(createKey(scheme, null,type), pres);
     }
 
     public static void store(String key, Presentation pres)
     {
         if (pres == null)
             return;
 
         synchronized (presentationStore)
         {
             presentationStore.put(key, pres);
         }
     }
 
     /**
      * Returns size in xxx.yyy[KMGTP] format. argument base1024 specifies wether
      * unit base is 1024 or 1000.
      * 
      * @param size
      *            actual size
      * @param base1024
      *            whether to use unit = base 1024 (false is base 1000)
      * @param unitScaleThreshold
      *            at which 1000 unit to show ISO term (1=K,2=M,3=G,etc)
      * @param nrDecimalsBehindPoint
      *            number of decimals behind the point.
      */
     public static String createSizeString(long size, boolean base1024, int unitScaleThreshold, int nrDecimals)
     {
         // boolean negative;
         String prestr = "";
 
         if (size < 0)
         {
             size = -size;
             // negative = true;
             prestr = "-";
         }
 
         long base = 1000;
 
         if (base1024)
             base = 1024;
 
         String unitstr = "";
         int scale = 0; //
 
         if (size < base)
         {
             unitstr = "";
             scale = 0;
         }
 
         scale = (int) Math.floor(Math.log(size) / Math.log(base));
 
         switch ((int) scale)
         {
             default:
             case 0:
                 unitstr = "";
                 break;
             case 1:
                 unitstr = "K";
                 break;
             case 2:
                 unitstr = "M";
                 break;
             case 3:
                 unitstr = "G";
                 break;
             case 4:
                 unitstr = "T";
                 break;
             case 5:
                 unitstr = "P";
                 break;
         }
         if (base1024 == false)
             unitstr += "i"; // ISO Ki = Real Kilo, Mi=Million Gi = real Giga.
 
         // 1024^5 fits in long !
         double norm = (double) size / (Math.pow(base, scale));
 
         // unitScaleThreshold = upto '1'
 
         if (scale < unitScaleThreshold)
             return "" + size;
 
         // format to xxx.yyy<UNIT>
         double fracNorm = Math.pow(10, nrDecimals);
         norm = Math.floor((norm * fracNorm)) / fracNorm;
 
         return prestr + norm + unitstr;
     }
 
     public static Presentation createDefault()
     {
         return new Presentation(); // return default object;
     }
 
     /**
      * Size of Strings, at which they are consider to be 'big'. Currentlty this
      * value determines when the AttributeViewer pop-ups.
      * 
      * @return
      */
     public static int getBigStringSize()
     {
         return 42;
     }
 
     /** Convert millis since Epoch to Date object */
     public static Date createDate(long millis)
     {
 
         if (millis < 0)
             return null;
 
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTimeZone(TimeZone.getTimeZone("GMT"));
         cal.setTimeInMillis(millis);
         return cal.getTime();
     }
 
     /** Convert System millies to Date */
     public static Date now()
     {
         long millis = System.currentTimeMillis();
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTimeZone(TimeZone.getTimeZone("GMT"));
         cal.setTimeInMillis(millis);
         return cal.getTime();
     }
 
     /**
      * Create GMT Normalized Date Time String from millis since Epoch.
      * <p>
      * Normalized time = YYYYYY MM DD hh:mm:ss.mss <br>
      * Normalized Timezone is GMT.<br>
      */
     public static String createNormalizedDateTimeString(long millis)
     {
         if (millis < 0)
             return null;
 
         Date date = createDate(millis);
         return Presentation.createNormalizedDateTimeString(date);
     }
 
     /**
      * Normalized date time string: "YYYYYY-MM-DD hh:mm:ss.milllis".<br>
      * Normalized Timezone is GMT.<br>
      */
     public static Date createDateFromNormalizedDateTimeString(String value)
     {
         if (value == null)
             return null;
 
         String strs[] = value.split("[ :-]");
 
         int year = new Integer(strs[0]);
         int month = new Integer(strs[1]) - 1; // January=0!
         int day = new Integer(strs[2]);
         int hours = new Integer(strs[3]);
         int minutes = new Integer(strs[4]);
         double secondsD = 0;
         if (strs.length > 5)
             secondsD = new Double(strs[5]);
 
         // String tzStr=null;
         TimeZone storedTimeZone = TimeZone.getTimeZone("GMT");
 
         /*
          * if (strs.length>6) { tzStr=strs[6];
          * 
          * if (tzStr!=null) storedTimeZone=TimeZone.getTimeZone(tzStr); }
          */
         int seconds = (int) Math.floor(secondsD);
         // Warning: millis is in exact 3 digits, but Double create
         // floating point offsets to approximate 3 digit precizion!
         int millis = (int) Math.round(((secondsD - Math.floor(secondsD)) * 1000));
 
         GregorianCalendar now = new GregorianCalendar();
         // TimeZone localTMZ=now.getTimeZone();
 
         now.clear();
         // respect timezone:
         now.setTimeZone(storedTimeZone);
         now.set(year, month, day, hours, minutes, seconds);
         now.set(GregorianCalendar.MILLISECOND, millis); // be precize!
         // convert timezone back to 'local'
         // now.setTimeZone(localTMZ);
 
         return now.getTime();
     }
 
     /**
      * Create normalized date time string: [YY]YYYY-DD-MM hh:mm:ss.ms in GMT
      * TimeZone.
      */
     public static String createNormalizedDateTimeString(Date date)
     {
         GregorianCalendar gmtTime = new GregorianCalendar();
         gmtTime.setTime(date);
         // normalize to GMT:
         gmtTime.setTimeZone(TimeZone.getTimeZone("GMT"));
 
         int year = gmtTime.get(GregorianCalendar.YEAR);
         int month = 1 + gmtTime.get(GregorianCalendar.MONTH); // January=0!
         int day = gmtTime.get(GregorianCalendar.DAY_OF_MONTH);
         int hours = gmtTime.get(GregorianCalendar.HOUR_OF_DAY);
         int minutes = gmtTime.get(GregorianCalendar.MINUTE);
         int seconds = gmtTime.get(GregorianCalendar.SECOND);
         int millies = gmtTime.get(GregorianCalendar.MILLISECOND);
 
         return "" + to4decimals(year) + "-" + to2decimals(month) + "-" + to2decimals(day) + " " + to2decimals(hours) + ":"
                 + to2decimals(minutes) + ":" + to2decimals(seconds) + "." + to3decimals(millies);
     }
 
     /** Convert Normalized DateTime string to millis since epoch */
     public static long createMillisFromNormalizedDateTimeString(String value)
     {
         if (value == null)
             return -1;
 
         Date date = Presentation.createDateFromNormalizedDateTimeString(value);
 
         if (date == null)
             return -1;
 
         return date.getTime();
     }
 
     // =======================================================================
     // Static Initializer!
     // =======================================================================
 
     static
     {
         staticInitDefaults();
     }
 
     private static void staticInitDefaults()
     {
         // Presentation pres=new Presentation();
         // pres.setChildAttributeNames(defaultFileAttributeNames);
         // String key=generateKey(AnyFile.FILE_SCHEME,AnyFile.FILE_TYPE);
         // Presentation.store(key);
     }
 
     // =============================================================
     // Instance
     // =============================================================
 
     /**
      * Which unit to skip and and which to start. 
      * For example,2 = skip Kilo byte (1), start at Megabytes (2).
      */
     protected Integer defaultUnitScaleThreshold = 2; 
 
     /** Numbers of decimals behind point */ 
     protected Integer defaultNrDecimals = 1;
     
     /** KiB/Mib versus KB and MB */ 
     protected Boolean useBase1024 = true;
 
     /** Whether to sort contents */ 
     protected Boolean allowSort = null;
 
     protected Boolean sortIgnoreCase = true;
 
     /** Attribute names from child (contents) to show by default. See also UIPresentation */ 
     protected StringList childAttributeNames = null;
 
     protected StringList sortFields = null;
 
     /** Parent Presentation object. */
     protected Presentation parent = null; // No hierarchical presentation (yet)
 
     /** Unless set this to override platform Locale */  
     protected Locale locale = null; 
     
     /** Default Presentation. */ 
     public Presentation()
     {
         initDefaults();
     }
 
     private void initDefaults()
     {
         // this.childAttributeNames=new StringList();
         //
         // setAttributePreferredWidth(ICON, 32);
         // setAttributePreferredWidth("Index", 32);
         // setAttributePreferredWidth(NAME, 200);
         // setAttributePreferredWidth("Type", 90);
         // setAttributePreferredWidth(SCHEME, 48);
         // setAttributePreferredWidth(HOSTNAME, 120);
         // setAttributePreferredWidth(LENGTH, 70);
         // setAttributePreferredWidth(PATH, 200);
         // setAttributePreferredWidth("Status",48);
         // setAttributePreferredWidth("ResourceStatus",48);
         // setAttributePreferredWidth(ACCESS_TIME, 120);
         // setAttributePreferredWidth(MODIFICATION_TIME, 120);
         // setAttributePreferredWidth(CREATION_TIME, 120);
     }
 
     /**
      * Get which Child Attribute to show by default. Note that it is the PARENT
      * object which holds the presentation information about the child
      * attributes. For example when opening a Directory in Table view the
      * Presentation of the (parent) directory holds the default file attributes
      * to show.
      */
     public String[] getChildAttributeNames()
     {
        if (childAttributeNames==null)
            return null; 
         return childAttributeNames.toArray();
     }
 
     /** Set which child attribute to show */
     public void setChildAttributeNames(String names[])
     {
         childAttributeNames = new StringList(names);
     }
 
     /** Returns sizeString +"[KMG]&lt;uni&gt;>" from "size" bytes per second */
     public String speedString(long size, String unit)
     {
         return sizeString(size) + unit;
     }
 
     /**
      * Returns size in xxx.yyy[KMGTP] format (base 1024). Uses settings from
      * Presentation instance.
      * 
      * @see #createSizeString(long, boolean, int, int)
      */
     public String sizeString(long size)
     {
         return sizeString(size, useBase1024, this.defaultUnitScaleThreshold, this.defaultNrDecimals);
     }
 
     /** @see #createSizeString(long, boolean, int, int) */
     public String sizeString(long size, boolean base1024, int unitScaleThreshold, int nrDecimals)
     {
         return createSizeString(size, base1024, unitScaleThreshold, nrDecimals);
     }
 
     /**
      * Create Relative Time String: "DD (days) hh:mm:ss.ms" time string" from
      * the specified nr of milli seconds.
      */
     public String createRelativeTimeString(long timeInMillis, boolean showMillis)
     {
         String timestr = "";
 
         if (timeInMillis > 1000L * 24L * 60L * 60L)
         {
             long days = timeInMillis / (1000L * 24L * 60L * 60L);
             timestr += days + " (days) ";
         }
 
         if (timeInMillis > 1000 * 60 * 60)
         {
             long hours = (timeInMillis / (1000L * 60 * 60)) % 60;
             timestr += timestr + to2decimals(hours) + ":";
         }
         // show it anyway to always show 00:00s format
         // if (time>1000*60)
         {
             long mins = (timeInMillis / (1000 * 60)) % 60;
             timestr += timestr + to2decimals(mins) + ":";
         }
 
         long secs = (timeInMillis / 1000L) % 60L;
         timestr += to2decimals(secs) + "s";
 
         if (showMillis)
             timestr += "." + (timeInMillis % 1000);
 
         return timestr;
     }
 
     /**
      * Whether automatic sorting is allowed or that the returned order of this
      * node should be kept as-is.
      */
     public boolean getAutoSort()
     {
         if (this.allowSort == null)
             return true;
 
         return this.allowSort;
     }
 
     /**
      * Specify whether nodes should be sorted automatically when fetched from
      * this resource, or nodes should be displayed 'in order'.
      */
     public void setAutoSort(boolean newVal)
     {
         this.allowSort = newVal;
     }
 
     /** Whether to ignore case when sorting files */
     public boolean getSortIgnoreCase()
     {
         if (sortIgnoreCase == null)
             return false;
 
         return this.sortIgnoreCase;
     }
 
     public void setSortIgnoreCase(boolean val)
     {
         this.sortIgnoreCase = val;
     }
 
     public Locale getLocale()
     {
         if (this.locale != null)
             return this.locale;
 
         return Locale.getDefault();
     }
 
     public void setLocale(Locale locale)
     {
         this.locale = locale;
     }
 
     /**
      * The first month (Jan) is 0 not 1. If month is null of len is < 3 will
      * return -1
      * 
      * @param month
      *            String of len 3 (Jan,Feb..,etc)
      * @return the 0-based month number;
      */
     public static int getMonthNumber(String month)
     {
         if (month == null || month.length() < 3)
         {
             return -1;
         }
 
         for (int i = 0; i < PresentationConst.getMonthNames().length; i++)
         {
             if (month.substring(0, 3).compareToIgnoreCase(PresentationConst.getMonthNames()[i]) == 0)
             {
                 return i;
             }
         }
         return -1;
 
     }
 
     /**
      * Returns optional attribute sort fields, by which this contents should be
      * sorted. If set the attribute names will be used to sort the contents of a
      * resource. If sortFields are NULL, then the default Type+Name sort is
      * used.
      */
     public StringList getSortFields()
     {
         return this.sortFields;
     }
 
     /**
      * Set optional (Attribute) sort fields. If set the attribute names will be
      * used to sort the contents of a resource. If sortFields are null, then the
      * default Type+Name sort is used.
      */
     public void setSortFields(String[] fields)
     {
         this.sortFields = new StringList(fields);
     }
 
    
 
 }
