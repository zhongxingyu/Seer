 package fi.dratini.keikkalista.core.integration;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import fi.dratini.keikkalista.core.integration.json.EventsJsonWrapper.EventGson;
 import fi.dratini.keikkalista.core.integration.json.LovedTracksJsonWrapper.TrackGson;
 import fi.dratini.keikkalista.core.integration.json.TopArtistsJsonWrapper.ArtistGson;
 import fi.dratini.keikkalista.core.model.Event;
 import fi.dratini.keikkalista.core.model.EventArtist;
 import fi.dratini.keikkalista.core.model.Preference;
 
 public class ModelConverter {
     private static final ModelConverter instance = new ModelConverter();
     private static SimpleDateFormat dateformat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
     
     private ModelConverter() {
         
     }
     
     public static ModelConverter getInstance()
     {
         return instance;
     }
     
     public List<Event> toEvents(List<EventGson> eventGsons) {
        if (eventGsons == null) {
            return new ArrayList<Event>();
        }
        
         ArrayList<Event> events = new ArrayList<Event>(eventGsons.size());
         for (EventGson gson : eventGsons) {
             events.add(toEvent(gson));
         }
         return events;
     }
     
     private Event toEvent(EventGson eventGson) {
         Event event = new Event();
         event.setId(eventGson.id);
         event.setTitle(eventGson.title);
         event.setCancelled(eventGson.cancelled);
         event.setUrl(eventGson.url);
         event.setDate(parseDate(eventGson.startDate));
         event.setCity(eventGson.venue.location.city);
         event.setHeadliner(eventGson.program.headliner);
         for (String artistName : eventGson.program.artists) {
             event.AddArtist(new EventArtist(event, artistName));
         }
         return event;
     }
     
     private Date parseDate(String dateStr) {
         try {
             return dateformat.parse(dateStr);
         } catch (ParseException e) {
             throw new RuntimeException(e);
         }
     }
     
     public Preference LovedTrackToPreference(List<TrackGson> trackGsons) {
        if (trackGsons == null) {
            return new Preference();
        }
        
         Preference preference = new Preference();
         for (TrackGson gson : trackGsons) {
             preference.AddArtist(gson.artist.name);
         }
         return preference;
     }
 
     public Preference TopArtistToPreference(List<ArtistGson> artistGsons) {
        if (artistGsons == null) {
            return new Preference();
        }
        
         Preference preference = new Preference();
         for (ArtistGson gson : artistGsons) {
             preference.AddArtist(gson.name);
         }
         return preference;
     } 
 }
