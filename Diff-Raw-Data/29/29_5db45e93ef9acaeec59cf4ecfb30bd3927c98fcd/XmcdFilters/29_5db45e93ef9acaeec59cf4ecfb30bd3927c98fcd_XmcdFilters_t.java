 package my.triviagame.xmcd;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import java.util.Iterator;
 import java.util.List;
 import my.triviagame.dal.TrackRow;
 
 /**
  * Static utility methods for generating iterator chains for working with {@link XmcdDisc}s.
  */
 public class XmcdFilters {
     
     /**
      * Handy factory for chaining filters.
      * Sample usage - get only discs which have the year specified and are not compilations:
      * Iterator<XmcdDisc> discsIWant = new XmcdFilters.Factory(allDiscs).hasYear().notVarious().chain();
      * 
      * Note: be careful with the chaining order!
      * For instance, the following will give you the first 100 discs not by Various:
      * new XmcdFilters.Factory(allDiscs).notVarious().firstN(100).chain();
      * While the following will give you the discs not by Various out of the first 100 discs:
      * new XmcdFilters.Factory(allDiscs).firstN(100).notVarious().chain();
      */
     public static class Factory {
         
         public Factory(Iterator<XmcdDisc> discs) {
             iter = discs;
         }
         
         public Factory firstN(int n) {
             iter = XmcdFilters.firstN(iter, n);
             return this;
         }
         
         public Factory hasYear() {
             iter = XmcdFilters.hasYear(iter);
             return this;
         }
         
         public Factory notVarious() {
             iter = XmcdFilters.notVarious(iter);
             return this;
         }
         
         public Factory notBadGenre() {
             iter = XmcdFilters.notBadGenre(iter);
             return this;
         }
         
         public Factory stripTrackVariant() {
             iter = XmcdFilters.stripTrackVariant(iter);
             return this;
         }
         
         public Factory trimWhitespace() {
             iter = XmcdFilters.stripWhitespace(iter);
             return this;
         }
         
         public Iterator<XmcdDisc> chain() {
             return iter;
         }
         
         /**
          * Default filtering behavior for the application.
          */
         public Factory defaultFilters() {
             return hasYear().notVarious().notBadGenre().stripTrackVariant().trimWhitespace();
         }
         
         private Iterator<XmcdDisc> iter;
     }
     
     /**
      * Gets at most the first N discs.
      */
     public static Iterator<XmcdDisc> firstN(Iterator<XmcdDisc> iter, int n) {
         return Iterators.limit(iter, n);
     }
     
     /**
      * Gets only discs that have a year set.
      */
     public static Iterator<XmcdDisc> hasYear(Iterator<XmcdDisc> iter) {
         return Iterators.filter(iter, new Predicate<XmcdDisc>() {
             @Override
             public boolean apply(XmcdDisc disc) {
                 return disc.albumRow.year != XmcdDisc.INVALID_YEAR;
             }
             
         });
     }
     
     /**
      * Ignores mix & compilation discs.
      * A disc is assumed to be a compilation if the album artist name starts with Various.
      */
     public static Iterator<XmcdDisc> notVarious(Iterator<XmcdDisc> iter) {
         return Iterators.filter(iter, new Predicate<XmcdDisc>() {
             @Override
             public boolean apply(XmcdDisc disc) {
                 return !disc.albumRow.artistName.startsWith("Various");
             }
         });
     }
     
     /**
      * Ignores albums in genres that we don't like such as audiobooks.
      */
     public static Iterator<XmcdDisc> notBadGenre(Iterator<XmcdDisc> iter) {
         return Iterators.filter(iter, new Predicate<XmcdDisc>() {
             @Override
             public boolean apply(XmcdDisc disc) {
                 return !XmcdRegEx.BAD_GENRE.matcher(disc.albumRow.freeTextGenre).matches();
             }
         });
     }
     
     /**
      * Removes trailing parenthesis (i.e. "My Song (Radio Mix)" -> "My Song").
      */
     public static Iterator<XmcdDisc> stripTrackVariant(Iterator<XmcdDisc> iter) {
         return Iterators.transform(iter, new Function<XmcdDisc, XmcdDisc>() {
             @Override
             public XmcdDisc apply(XmcdDisc disc) {
                for (TrackRow trackRow : disc.trackRows) {
                    trackRow.title = XmcdRegEx.TRACK_VARIANT.matcher(trackRow.title).replaceFirst("");
                }
                return disc;
             }
         });
     }
     
     /**
      * Removes leading & trailing whitespace.
      */
     public static Iterator<XmcdDisc> stripWhitespace(Iterator<XmcdDisc> iter) {
         return Iterators.transform(iter, new Function<XmcdDisc, XmcdDisc>() {
             @Override
             public XmcdDisc apply(XmcdDisc disc) {
                for (TrackRow trackRow : disc.trackRows) {
                    trackRow.title = trackRow.title.trim();
                }
                 disc.albumRow.title = disc.albumRow.title.trim();
                return disc;
             }
         });
     }
 }
