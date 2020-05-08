 package com.level42.mixit.models;
 
 import java.util.Date;
 import java.util.List;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 /**
  * Classe représentant un Talk.
  */
@JsonIgnoreProperties(ignoreUnknown = true)
 public class Talk implements Comparable<Talk> {
 
     /**
      * Identifiant du talk.
      */
     private Integer id;
 
     /**
      * Titre du talk.
      */
     private String title;
 
     /**
      * Sommaire du talk.
      */
     private String summary;
 
     /**
      * Description du talk.
      */
     private String description;
 
     /**
      * Liste des identifiant des tags "centre d'intérêt".
      */
     @JsonProperty("interests")
     private List<Integer> interestsId;
 
     /**
      * Liste des tags "centre d'intérêt".
      */
     @JsonIgnore
     private List<Interest> interests;
 
     /**
      * Liste des identifiant des speakers.
      */
     @JsonProperty("speakers")
     private List<Integer> speakersId;
 
     /**
      * Liste des speakers.
      */
     @JsonIgnore
     private List<Speaker> speakers;
 
     /**
      * Format du talk.
      */
     private String format;
 
     /**
      * Niveau de difficulté du talk.
      */
     private String level;
 
     /**
      * Session du talk.
      */
     @JsonIgnore
     private Session session;
 
     /**
      * Date de démarrage.
      */
     private String start;
 
     /**
      * Date de fin.
      */
     private String end;
 
     /**
      * Retourne l'identifiant du talk.
      * @return the id
      */
     public Integer getId() {
         return id;
     }
 
     /**
      * Renseigne l'identifiant du talk.
      * @param id the id to set
      */
     public void setId(Integer id) {
         this.id = id;
     }
 
     /**
      * Retourne le titre du talk.
      * @return the title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Renseigne le titre du talk.
      * @param title the title to set
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * Retourne le sommaire du talk.
      * @return the summary
      */
     public String getSummary() {
         return summary;
     }
 
     /**
      * Renseigne le sommaire du talk.
      * @param summary the summary to set
      */
     public void setSummary(String summary) {
         this.summary = summary;
     }
 
     /**
      * Retourne la description du talk.
      * @return the description
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * Renseigne la description du talk.
      * @param description the description to set
      */
     public void setDescription(String description) {
         this.description = description;
     }
 
     /**
      * Retourne les identifiants des tags "centre d'intérêts".
      * @return the interests Id
      */
     public List<Integer> getInterestsId() {
         return interestsId;
     }
 
     /**
      * Renseigne les identifiants des tags "centre d'intérêts".
      * @param interestsId the interests Id to set
      */
     public void setInterestsId(List<Integer> interestsId) {
         this.interestsId = interestsId;
     }
 
     /**
      * Retourne la liste des identifiants des speakers du talk.
      * @return the speakers Id
      */
     public List<Integer> getSpeakersId() {
         return speakersId;
     }
 
     /**
      * Renseigne la liste des identifiants des speakers du talk.
      * @param speakersId the speakers Id to set
      */
     public void setSpeakersId(List<Integer> speakersId) {
         this.speakersId = speakersId;
     }
 
     /**
      * Retourne le format du talk.
      * @return the format
      */
     public String getFormat() {
         return format;
     }
 
     /**
      * Renseigne le format du talk.
      * @param format the format to set
      */
     public void setFormat(String format) {
         this.format = format;
     }
 
     /**
      * Retourne le niveau de difficulté du talk.
      * @return the level
      */
     public String getLevel() {
         return level;
     }
 
     /**
      * Renseigne le niveau de difficulté du talk.
      * @param level the level to set
      */
     public void setLevel(String level) {
         this.level = level;
     }
 
     /**
      * Renseigne la session du talk.
      * @param session the session to set
      */
     public void setSession(Session session) {
         this.session = session;
     }
 
     /**
      * Retourne la date de la session du Talk.
      * @return Date de la session du Talk
      */
     public Date getDateSession() {
         if (this.session != null) {
             return this.session.getDateFormat();
         } else {
             return null;
         }
     }
 
     /**
      * Retourne la salle de la session du Talk.
      * @return Salle de la session du Talk
      */
     public String getSalleSession() {
         if (this.session != null) {
             return this.session.getSalle();
         } else {
             return null;
         }
     }
 
     /**
      * Retourne la liste des centres d'intérêt du talk.
      * @return the interests
      */
     public List<Interest> getInterests() {
         return interests;
     }
 
     /**
      * Renseigne la liste des centres d'intérêt du talk.
      * @param interests the interests to set
      */
     public void setInterests(List<Interest> interests) {
         this.interests = interests;
     }
 
     /**
      * Retourne la liste des speakers.
      * @return the speakers
      */
     public List<Speaker> getSpeakers() {
         return speakers;
     }
 
     /**
      * Renseigne la liste des speakers.
      * @param speakers the speakers to set
      */
     public void setSpeakers(List<Speaker> speakers) {
         this.speakers = speakers;
     }
 
     /**
      * Retourne la date de début.
      * @return the start
      */
     public String getStart() {
         return start;
     }
 
     /**
      * Renseigne la date de début.
      * @param start the start to set
      */
     public void setStart(String start) {
         this.start = start;
     }
 
     /**
      * Retourne la date de fin.
      * @return the end
      */
     public String getEnd() {
         return end;
     }
 
     /**
      * Renseigne la date de fin.
      * @param end the end to set
      */
     public void setEnd(String end) {
         this.end = end;
     }
 
     /*
      * (non-Javadoc)
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     @Override
     public int compareTo(Talk another) {
         if (another.getDateSession() == null) {
             return 1;
         }
         if (this.getDateSession() == null) {
             return -1;
         }
         return getDateSession().compareTo(another.getDateSession());
     }
 }
