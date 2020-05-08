 package fi.dratini.keikkalista.core.integration;
 
 import java.util.List;
 
 import fi.dratini.keikkalista.core.dataaccess.Config;
 import fi.dratini.keikkalista.core.integration.json.EventsJsonWrapper;
 import fi.dratini.keikkalista.core.integration.json.GsonFactory;
 import fi.dratini.keikkalista.core.integration.json.LovedTracksJsonWrapper;
 import fi.dratini.keikkalista.core.integration.json.TopArtistsJsonWrapper;
 import fi.dratini.keikkalista.core.model.Event;
 import fi.dratini.keikkalista.core.model.Preference;
 
 public class IntegrationProvider {
     private LastFmJsonLoader jsonLoader;
     
     public IntegrationProvider() {
         String apikey = Config.getInstance().getApiKey();
         jsonLoader = new LastFmJsonLoader(apikey);
     }
     
     public List<Event> GetEvents(int count) {
         String json = jsonLoader.GetEvents(count);
         EventsJsonWrapper wrap = GsonFactory.buildGson().fromJson(json, EventsJsonWrapper.class);
         return ModelConverter.getInstance().toEvents(wrap.root.events);
     }
     
     public Preference GetTopArtists(String username, PeriodValues period, int count) {
         String json = jsonLoader.GetTopArtists(username, period, count);
         TopArtistsJsonWrapper wrap = GsonFactory.buildGson().fromJson(json, TopArtistsJsonWrapper.class);
         return ModelConverter.getInstance().TopArtistToPreference(wrap.root.artists);
     }
     
     public Preference GetLovedTracks(String username, int count) {
        String json = jsonLoader.GetLovedTracks(username, count);
         LovedTracksJsonWrapper wrap = GsonFactory.buildGson().fromJson(json, LovedTracksJsonWrapper.class);
         return ModelConverter.getInstance().LovedTrackToPreference(wrap.root.tracks);
     }
 }
