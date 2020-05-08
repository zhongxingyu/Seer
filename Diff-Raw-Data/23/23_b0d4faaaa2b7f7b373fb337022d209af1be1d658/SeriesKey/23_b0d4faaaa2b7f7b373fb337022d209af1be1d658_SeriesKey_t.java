 package com.tms.threed.threedCore.shared;
 
 import com.tms.threed.util.lang.shared.Path;
 import com.tms.threed.util.lang.shared.Strings;
 
 import javax.annotation.Nonnull;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SeriesKey implements Serializable, Comparable<SeriesKey> {
 
     private /*final*/ int year;
     private /*final*/ String name;
    private String originalName;
 
     public SeriesKey(final int year, @Nonnull final String name) {
         assert Strings.notEmpty(name);
         this.year = year;
        this.originalName = name.trim();
         this.name = name.trim().toLowerCase().replaceAll(" ","");
     }
 
     public SeriesKey(@Nonnull final String year, @Nonnull final String name) {
         this(Integer.parseInt(year), name);
     }
 
     //For serialization
     protected SeriesKey() {
         year = 0;
         name = null;
     }
 
     public static int parserModelYear(String modelYear) {
         return Integer.parseInt(modelYear);
     }
 
     private static String getKeyAsString(int year, String name) {return year + "-" + name.toLowerCase();}
 
     public int getYear() {
         return year;
     }
 
     public String getName() {
         return name;
     }
 
    public String getOriginalName() {
        return originalName;
    }

     public String getLabel() {
        return originalName.toUpperCase();
     }
 
     @Override public String toString() {
         return year + " " + name;
     }
 
     @Override
     public int hashCode() {
         int result = year;
         result = 31 * result + (name != null ? name.hashCode() : 0);
         return result;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null) return false;
         if (SeriesKey.class != o.getClass()) return false;
         SeriesKey that = (SeriesKey) o;
         return this.year == that.year && this.name.equals(that.name);
     }
 
 
     public static final String YARIS = "yaris";
     public static final String RAV4 = "rav4";
     public static final String TACOMA = "tacoma";
     public static final String AVALON = "avalon";
     public static final String VENZA = "venza";
     public static final String CAMRY = "camry";
     public static final String SIENNA = "sienna";
     public static final String SEQUOIA = "sequoia";
     public static final String TUNDRA = "tundra";
     public static final String FOUR_RUNNER = "4runner";
 
     public static final int VENZA_ID = 28;
     public static final int CAMRY_ID = 3;
     public static final int SIENNA_ID = 14;
     public static final int TUNDRA_ID = 17;
     public static final int FOUR_RUNNER_ID = 1;
 
 
     public static final SeriesKey YARIS_2010 = new SeriesKey(2010, YARIS);
     public static final SeriesKey RAV4_2010 = new SeriesKey(2010, RAV4);
     public static final SeriesKey TACOMA_2011 = new SeriesKey(2011, TACOMA);
     public static final SeriesKey AVALON_2011 = new SeriesKey(2011, AVALON);
     public static final SeriesKey AVALON_2010 = new SeriesKey(2010, AVALON);
     public static final SeriesKey CAMRY_2011 = new SeriesKey(2011, CAMRY);
     public static final SeriesKey SEQUOIA_2011 = new SeriesKey(2011, SEQUOIA);
     public static final SeriesKey SIENNA_2011 = new SeriesKey(2011, SIENNA);
     public static final SeriesKey VENZA_2011 = new SeriesKey(2011, VENZA);
     public static final SeriesKey VENZA_2010 = new SeriesKey(2010, VENZA);
     public static final SeriesKey VENZA_2009 = new SeriesKey(2009, VENZA);
 
     public static List<SeriesKey> getAll() {
         ArrayList<SeriesKey> a = new ArrayList<SeriesKey>();
         a.add(AVALON_2010);
         a.add(AVALON_2011);
         a.add(CAMRY_2011);
         a.add(SIENNA_2011);
         a.add(VENZA_2009);
         a.add(VENZA_2010);
         a.add(VENZA_2011);
         return a;
     }
 
     public static String getSeriesName(int seriesCategoryId) {
         switch (seriesCategoryId) {
             case VENZA_ID:
                 return VENZA;
             case CAMRY_ID:
                 return CAMRY;
             case SIENNA_ID:
                 return SIENNA;
             default:
                 throw new IllegalArgumentException();
         }
     }
 
     public Path getSeriesPngRoot(Path pngRootDir) {
         return pngRootDir.append(year + "").append(name);
     }
 
     public Path getSeriesJpgRoot(Path jpgRootDir) {
         return jpgRootDir.append(year + "").append(name);
     }
 
     public boolean isa(String seriesName) {
         return name.equalsIgnoreCase(seriesName);
     }
 
     @Override public int compareTo(SeriesKey that) {
         if (this.getYear() != that.getYear()) {
             Integer thisYear = new Integer(this.getYear());
             Integer thatYear = new Integer(that.getYear());
             return thisYear.compareTo(thatYear);
         } else {
             return this.name.compareTo(that.name);
         }
     }
 
     public static SeriesKey get(int year, String series) {
         return SeriesKey.getByYearAndSeries(year, series);
     }
 
     public static SeriesKey getByYearAndSeries(int year, String name) {
         for (SeriesKey sk : getAll()) {
             if (sk.year == year && sk.name.equals(name)) return sk;
         }
         return null;
     }
 
     public static final SeriesKey DUMMY = new SeriesKey(2010, "dummy");
 
 
     public String getYourMessage() {
         return "your " + getYear() + " " + getLabel();
     }
     
 }
