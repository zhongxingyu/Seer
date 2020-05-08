 package org.atlasapi.remotesite.pa.film;
 
 import java.util.List;
 
 import nu.xom.Element;
 import nu.xom.Elements;
 
 import org.atlasapi.media.entity.Actor;
 import org.atlasapi.media.entity.CrewMember;
 import org.atlasapi.media.entity.CrewMember.Role;
 import org.atlasapi.media.entity.Film;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Restriction;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.pa.PaCountryMap;
 import org.atlasapi.remotesite.pa.PaHelper;
 import org.joda.time.Duration;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.text.MoreStrings;
 
 public class PaFilmProcessor {
     
     private final ContentResolver contentResolver;
     private final ContentWriter contentWriter;
     private final ItemsPeopleWriter peopleWriter;
     private final AdapterLog log;
     private final PaCountryMap countryMapper = new PaCountryMap();
 
     public PaFilmProcessor(ContentResolver contentResolver, ContentWriter contentWriter, ItemsPeopleWriter peopleWriter, AdapterLog log) {
         this.contentResolver = contentResolver;
         this.contentWriter = contentWriter;
         this.peopleWriter = peopleWriter;
         this.log = log;
     }
     
     public void process(Element filmElement) {
         String id = filmElement.getFirstChildElement("film_reference_no").getValue();
         
         Film film;
         Identified existingFilm = contentResolver.findByCanonicalUris(ImmutableList.of(PaHelper.getFilmUri(id))).getFirstValue().valueOrNull();
         if (existingFilm != null) {
             if (existingFilm instanceof Film) {
                 film = (Film) existingFilm;
             } else {
                 film = new Film();
                 Item.copyTo((Item) existingFilm, film);
             }
         } else {
             film = new Film(PaHelper.getFilmUri(id), PaHelper.getFilmCurie(id), Publisher.PA);
         }
         
         Element imdbElem = filmElement.getFirstChildElement("imdb_ref");
         if (imdbElem != null) {
             film.addAlias(normalize(imdbElem.getValue()));
         }
 
         film.setSpecialization(Specialization.FILM);
         film.setTitle(filmElement.getFirstChildElement("title").getValue());
         String year = filmElement.getFirstChildElement("year").getValue();
         if (!Strings.isNullOrEmpty(year) && MoreStrings.containsOnlyAsciiDigits(year)) {
             film.setYear(Integer.parseInt(year));
         }
 
         Version version = new Version();
         version.setProvider(Publisher.PA);
         Element certificateElement = filmElement.getFirstChildElement("certificate");
         if (certificateElement != null && !Strings.isNullOrEmpty(certificateElement.getValue()) && MoreStrings.containsOnlyAsciiDigits(certificateElement.getValue())) {
             version.setRestriction(Restriction.from(Integer.parseInt(certificateElement.getValue())));
         }
 
         Element durationElement = filmElement.getFirstChildElement("running_time");
         if (durationElement != null && !Strings.isNullOrEmpty(durationElement.getValue()) && MoreStrings.containsOnlyAsciiDigits(durationElement.getValue())) {
             version.setDuration(Duration.standardMinutes(Long.parseLong(durationElement.getValue())));
         }
 
         film.setVersions(ImmutableSet.of(version));
         
         Element countriesElement = filmElement.getFirstChildElement("country_of_origin");
         if (countriesElement != null && !Strings.isNullOrEmpty(countriesElement.getValue())) {
             film.setCountriesOfOrigin(countryMapper.parseCountries(countriesElement.getValue()));
         }
         
         List<CrewMember> otherPublisherPeople = getOtherPublisherPeople(film);
         
         if (otherPublisherPeople.isEmpty()) {
             film.setPeople(ImmutableList.copyOf(Iterables.concat(getActors(filmElement.getFirstChildElement("cast")), getDirectors(filmElement.getFirstChildElement("direction")))));
         }
         else {
             film.setPeople(otherPublisherPeople);
         }
         
         contentWriter.createOrUpdate(film);
         
         peopleWriter.createOrUpdatePeople(film);
     }
     
     private String normalize(String imdbRef) {
        return imdbRef.replace("www.", "http://");
     }
 
     private List<CrewMember> getOtherPublisherPeople(Film film) {
         Builder<CrewMember> builder = ImmutableList.builder();
         for (CrewMember crewMember : film.getPeople()) {
             if (crewMember.publisher() != Publisher.RADIO_TIMES) {
                 builder.add(crewMember);
             }
         }
         return builder.build();
     }
     
     private List<Actor> getActors(Element castElement) {
         Elements actorElements = castElement.getChildElements("actor");
         
         List<Actor> actors = Lists.newArrayList();
         
         for (int i = 0; i < actorElements.size(); i++) {
             Element actorElement = actorElements.get(i);
             
             String role = actorElement.getFirstChildElement("role").getValue();
             
             actors.add(Actor.actorWithoutId(name(actorElement), role, Publisher.RADIO_TIMES));
         }
         
         return actors;
     }
     
     private List<CrewMember> getDirectors(Element directionElement) {
         Elements directorElements = directionElement.getChildElements("director");
         
         List<CrewMember> actors = Lists.newArrayList();
         
         for (int i = 0; i < directorElements.size(); i++) {
             Element directorElement = directorElements.get(i);
             
             String role = directorElement.getFirstChildElement("role").getValue();
             role = role.trim().replace(" ", "_");
             
             String name = name(directorElement);
             
             if (name != null) {
                 if (Role.fromPossibleKey(role).isNothing()) {
                     log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Ignoring crew member with unrecognised role: " + role));
                 } else {
                     actors.add(CrewMember.crewMemberWithoutId(name, role, Publisher.RADIO_TIMES));
                 }
             }
         }
         
         return actors;
     }
     
     private String name(Element personElement) {
         
         Element forename = personElement.getFirstChildElement("forename");
         Element surname = personElement.getFirstChildElement("surname");
         
         if (forename == null && surname == null) {
             log.record(new AdapterLogEntry(Severity.WARN).withDescription("Person found with no name: " + personElement.toXML()).withSource(getClass()));
             return null;
         }
         
         if (forename != null && surname != null) {
            return forename.getValue() + " " + surname.getValue();
         }
         else {
             if (forename != null) {
                 return forename.getValue();
             }
             else {
                 return surname.getValue();
             }
         }
     }
 }
