 package pgu.client.place;
 
 import static pgu.client.place.PlaceHelper.GRANULARITY;
 import static pgu.client.place.PlaceHelper.LANGUAGE;
 import static pgu.client.place.PlaceHelper.SUBSELECTIONS;
 import static pgu.client.place.PlaceHelper.THEME;
 
 import java.util.ArrayList;
 
 import pgu.client.enums.LabelHelper;
 import pgu.client.enums.Language;
 import pgu.client.enums.LanguageGranularity;
 import pgu.client.enums.Theme;
 import pgu.client.utils.guava.Lists;
 
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.place.shared.PlaceTokenizer;
 
 public class SubselectionLevelPlace extends Place {
 
     private final Language language;
     private final LanguageGranularity granularity;
     private final Theme theme;
     private final ArrayList<String> subselections = Lists.newArrayList();
 
     public SubselectionLevelPlace(final Language language, //
             final LanguageGranularity granularity, //
             final Theme theme, //
             final ArrayList<String> subselections) {
 
         this.language = language;
         this.granularity = granularity;
         this.theme = theme;
        this.subselections.addAll(subselections);
     }
 
     public static class Tokenizer implements PlaceTokenizer<SubselectionLevelPlace> {
 
         @Override
         public String getToken(final SubselectionLevelPlace place) {
             return new PlaceHelper() //
                     .put(LANGUAGE, place.language) //
                     .put(GRANULARITY, place.granularity) //
                     .put(THEME, place.theme) //
                    .put(SUBSELECTIONS, place.subselections) //
                     .getToken();
 
         }
 
         @Override
         public SubselectionLevelPlace getPlace(final String token) {
             final PlaceHelper placeHelper = new PlaceHelper(token);
 
             return new SubselectionLevelPlace( //
                     LabelHelper.fromLanguage(placeHelper.get(LANGUAGE)), //
                     LabelHelper.fromGranularity(placeHelper.get(GRANULARITY)), //
                     LabelHelper.fromTheme(placeHelper.get(THEME)), //
                     placeHelper.list(SUBSELECTIONS) //
             );
         }
     }
 
     public Theme getTheme() {
         return theme;
     }
 
     public Language getLanguage() {
         return language;
     }
 
     public LanguageGranularity getGranularity() {
         return granularity;
     }
 
     public ArrayList<String> getSubselections() {
         return subselections;
     }
 
 }
