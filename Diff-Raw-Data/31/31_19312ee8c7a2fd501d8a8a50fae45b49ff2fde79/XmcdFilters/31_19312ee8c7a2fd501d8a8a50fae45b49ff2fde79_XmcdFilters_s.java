 package my.triviagame.xmcd;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterators;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import my.triviagame.dal.TrackRow;
 
 /**
  * Static utility methods for generating iterator chains for working with {@link XmcdDisc}s.
  */
 public class XmcdFilters {
     
     /**
      * White-listed genres.
      * Determined by getting the top 100 genres in a complete FreeDB archive from 2012 and then manually filtering
      * stuff that looks dodgy.
      */
     public static Set<String> GENRE_WHITELIST = ImmutableSet.of(
             "Jazz", "Rock", "Folk", "Pop", "Electronic", "Metal", "Vocal", "Dance", "Hip Hop", "Rap", "Indie", "Gospel",
             "House", "Trance", "Karaoke", "Latin", "Progressive Rock", "Hard Rock", "Classical", "Ambient",
             "Instrumental", "Techno", "Ethnic", "Comedy", "Celtic", "Soul", "Folklore", "Industrial", "Easy Listening",
             "Avantgarde", "Heavy Metal", "Fusion", "Acoustic", "Funk", "Punk Rock", "Death Metal", "Meditative",
             "Hardcore", "Punk", "Christmas", "Big Band", "Black Metal", "Christian", "Bluegrass", "Blues", "Oldies",
             "Swing", "Disco", "Club", "Contemporary Christian", "Psychedelic", "Christian Rock", "Ska", "Country",
             "Noise", "Chorus", "National Folk", "Soundtrack", "Classic Rock", "Gothic", "World", "Chanson", "Musical",
             "New Age", "Children", "Trip", "Contemporary Jazz", "Ballad", "Psychedelic Rock", "Thrash Metal",
             "Synthpop", "Eurodance", "Jpop", "Drum", "Salsa", "A Cappella", "Alternative Rock", "Polka", "Tango",
             "Acid Jazz", "Gothic Rock", "Language", "Electronica", "Spoken Word", "Worship", "New Wave", "Cabaret");
     
     /**
      * Genres that are white-listed but require conversion.
      */
     public static Map<String, String> GENRE_CONVERSION = ImmutableMap.of(
             "Hip", "Hip Hop",
             "Alt", "Alternative Rock",
             "Alternative", "Alternative Rock",
             "Acapalla", "A Capella",
             "A capela", "A Capella");
     
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
         
         public Iterator<XmcdDisc> chain() {
             return iter;
         }
         
         /**
          * Default filtering behavior for the application.
          */
         public Factory defaultFilters() {
            return hasYear().notVarious().notBadGenre().stripTrackVariant();
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
                 String genre = disc.albumRow.freeTextGenre;
                 if (GENRE_WHITELIST.contains(genre)) {
                     // The genre is in the white-list
                     return true;
                 }
                 String fixedGenre = GENRE_CONVERSION.get(genre);
                 if (fixedGenre != null) {
                     // The genre is in the white-list after a fix (e.g. "Hip" instead of "Hip Hop")
                     disc.albumRow.freeTextGenre = fixedGenre;
                     return true;
                 }
                 return false;
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
 }
