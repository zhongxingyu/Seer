 package org.atlasapi.media.entity;
 
 import java.util.Set;
 
 public class CrewMember extends Identified {
     
     public enum Role {
         ABRIDGED_BY("abridged_by", "Abridged By"),
         DEPUTY_EDITOR("deputy_editor", "Deputy Editor"),
         DIRECTOR("director", "Director"),
         DRAMATISED_BY("dramatised_by", "Dramatised By"),
         EDITOR("editor", "Editor"),
         EXECUTIVE_EDITOR("executive_editor", "Executive Editor"),
         EXECUTIVE_PRODUCER("executive_producer", "Executive Producer"),
         PRODUCER("producer", "Producer"),
         SERIES_DIRECTOR("series_director", "Series Director"),
         SERIES_EDITOR("series_editor", "Series Editor"),
         SERIES_PRODUCER("series_producer", "Series Producer"),
         WRITER("writer", "Writer"),
         ADAPTED_BY("adapted_by", "Adapted By"),
         ACTOR("actor", "Actor");
         
         private final String key;
         private final String title;
         
         private Role(String key, String title) {
             this.key = key;
             this.title = title;
         }
         
         public String key() {
             return key;
         }
         
         public String title() {
             return title;
         }
         
         public static Role fromKey(String key) {
             for (Role role: Role.values()) {
                 if (role.key.equals(key)) {
                     return role;
                 }
             }
             throw new IllegalArgumentException("Unkown role: "+key);
         }
     }
     
     private Role role;
     protected String name;
     private Publisher publisher;
     
     public CrewMember() {
         super();
     }
     
     public CrewMember(String uri, String curie, Publisher publisher) {
         super(uri, curie);
         this.publisher = publisher;
     }
     
     public Role role() {
         return role;
     }
     
     public String name() {
         return name;
     }
     
     public Publisher publisher() {
         return publisher;
     }
     
     public Set<String> profileLinks() {
         return getAliases();
     }
     
     public CrewMember withPublisher(Publisher publisher) {
         this.publisher = publisher;
         return this;
     }
     
     public CrewMember withRole(Role role) {
         this.role = role;
         return this;
     }
     
     public CrewMember withName(String name) {
         this.name = name;
         return this;
     }
     
     public CrewMember withProfileLink(String profileLink) {
         this.addAlias(profileLink);
         return this;
     }
     
     public CrewMember withProfileLinks(Set<String> profileLinks) {
         this.setAliases(profileLinks);
         return this;
     }
     
     public static CrewMember crewMember(String name, String roleKey, Publisher publisher) {
         Role role = Role.fromKey(roleKey);
         String key = Person.formatForUri(name);
         String uri = String.format(Person.BASE_URI, publisher.key(), key);
         String curie = publisher.key()+':'+key;
         return new CrewMember(uri, curie, publisher)
             .withRole(role)
             .withName(name);
     }
     
     public Person toPerson() {
         return new Person(this.getCanonicalUri(), this.getCurie(), this.publisher()).withName(name).withProfileLinks(profileLinks());
     }
     
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof CrewMember) {
             CrewMember crew = (CrewMember) obj;
             return this.getCanonicalUri().equals(crew.getCanonicalUri()) && name.equals(crew.name) && role == crew.role;
         }
         return false;
     }
     
     @Override
     public int hashCode() {
         return getCanonicalUri().hashCode();
     }
     
     @Override
     public String toString() {
         return "Crew "+name+" worked as a "+role.title;
     }
 }
