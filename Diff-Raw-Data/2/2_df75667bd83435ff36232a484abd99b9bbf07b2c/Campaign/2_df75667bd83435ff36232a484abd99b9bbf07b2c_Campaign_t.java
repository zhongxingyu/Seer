 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.fhg.fokus.persistence;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import javax.xml.bind.annotation.XmlTransient;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author Hannes Gorges
  */
 @Entity
 @Table(name = "campaign")
 @XmlRootElement
 @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
 @NamedQueries({
     @NamedQuery(name = "Campaign.findAll", query = "SELECT c FROM Campaign c"),
     @NamedQuery(name = "Campaign.findByIdCampaign", query = "SELECT c FROM Campaign c WHERE c.idCampaign = :idCampaign"),
     @NamedQuery(name = "Campaign.findByTitle", query = "SELECT c FROM Campaign c WHERE c.title = :title"),
     @NamedQuery(name = "Campaign.findByActive", query = "SELECT c FROM Campaign c WHERE c.active = :active"),
     @NamedQuery(name = "Campaign.findByCreationdate", query = "SELECT c FROM Campaign c WHERE c.creationdate = :creationdate"),
     @NamedQuery(name = "Campaign.findByStartdate", query = "SELECT c FROM Campaign c WHERE c.startdate = :startdate"),
     @NamedQuery(name = "Campaign.findByEnddate", query = "SELECT c FROM Campaign c WHERE c.enddate = :enddate"),
     @NamedQuery(name = "Campaign.findByNotes", query = "SELECT c FROM Campaign c WHERE c.notes = :notes"),
     @NamedQuery(name = "Campaign.findByUrl", query = "SELECT c FROM Campaign c WHERE c.url = :url"),
     @NamedQuery(name = "Campaign.findByHashTag", query = "SELECT c FROM Campaign c WHERE c.hashTag = :hashTag")})
 public class Campaign implements Serializable {
 
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @NotNull
     @Column(name = "idCampaign")
     private Integer idCampaign;
     @Size(max = 255)
     @Column(name = "title")
     private String title;
     @Basic(optional = false)
     @NotNull
     @Column(name = "active")
     private boolean active;
     @Column(name = "creationdate")
     @Temporal(TemporalType.DATE)
     private Date creationdate;
     @Column(name = "startdate")
     @Temporal(TemporalType.DATE)
     private Date startdate;
     @Column(name = "enddate")
     @Temporal(TemporalType.DATE)
     private Date enddate;
     @Size(max = 255)
     @Column(name = "notes")
     private String notes;
     @Size(max = 255)
     @Column(name = "url")
     private String url;
     @Size(max = 255)
     @Column(name = "hashTag")
     private String hashTag;
     @JoinTable(name = "campaign_has_publishchannel", joinColumns = {
         @JoinColumn(name = "Campaign_idCampaign", referencedColumnName = "idCampaign")}, inverseJoinColumns = {
         @JoinColumn(name = "PublishChannel_idPublishChannel", referencedColumnName = "idPublishChannel")})
     @ManyToMany
     private List<Publishchannel> publishchannelList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<CampaignHasPlatform> campaignHasPlatformList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "campaignidCampaign")
     private List<Campaigntopics> campaigntopicsList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Userrole> userroleList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Action> actionList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Youtube> youtubeList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Survey> surveyList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Invitation> invitationList;
     @JoinColumn(name = "idLocation", referencedColumnName = "idLocation")
     @ManyToOne(optional = false)
     private Location idLocation;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Blogger> bloggerList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Message> messageList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Facebookvisits> facebookvisitsList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "idCampaign")
     private List<Facebookdata> facebookdataList;
 
     public Campaign() {
     }
 
     public Campaign(Integer idCampaign) {
         this.idCampaign = idCampaign;
     }
 
     public Campaign(Integer idCampaign, boolean active) {
         this.idCampaign = idCampaign;
         this.active = active;
     }
 
     public Integer getIdCampaign() {
         return idCampaign;
     }
 
     public void setIdCampaign(Integer idCampaign) {
         this.idCampaign = idCampaign;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public boolean getActive() {
         return active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     public Date getCreationdate() {
         return creationdate;
     }
 
     public void setCreationdate(Date creationdate) {
         this.creationdate = creationdate;
     }
 
     public Date getStartdate() {
         return startdate;
     }
 
     public void setStartdate(Date startdate) {
         this.startdate = startdate;
     }
 
     public Date getEnddate() {
         return enddate;
     }
 
     public void setEnddate(Date enddate) {
         this.enddate = enddate;
     }
 
     public String getNotes() {
         return notes;
     }
 
     public void setNotes(String notes) {
         this.notes = notes;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getHashTag() {
         return hashTag;
     }
 
     public void setHashTag(String hashTag) {
         this.hashTag = hashTag;
     }
 
     @XmlElementWrapper(name = "publishchannels")
     @XmlElement(name = "publishchannel")
     public List<Publishchannel> getPublishchannelList() {
         return publishchannelList;
     }
 
     public void setPublishchannelList(List<Publishchannel> publishchannelList) {
         this.publishchannelList = publishchannelList;
     }
 
     public boolean addPublishchannel(Publishchannel publishchannel) {
         return this.publishchannelList.add(publishchannel);
     }
 
     public boolean removePublishchannel(Publishchannel publishchannel) {
         return this.publishchannelList.remove(publishchannel);
     }
 
     @JsonIgnore
     @XmlTransient
     public List<CampaignHasPlatform> getCampaignHasPlatformList() {
         return campaignHasPlatformList;
     }
 
     public void setCampaignHasPlatformList(List<CampaignHasPlatform> campaignHasPlatformList) {
         this.campaignHasPlatformList = campaignHasPlatformList;
     }
 
     @XmlElementWrapper(name = "topics")
     @XmlElement(name = "topic")
     public List<Campaigntopics> getCampaigntopicsList() {
         return campaigntopicsList;
     }
 
     public void setCampaigntopicsList(List<Campaigntopics> campaigntopicsList) {
         this.campaigntopicsList = campaigntopicsList;
     }
 
     public boolean addCampaigntopic(Campaigntopics ct) {
         return this.campaigntopicsList.add(ct);
     }
 
     public boolean removeCampaigntopic(Campaigntopics ct) {
         return this.campaigntopicsList.remove(ct);
     }
 
     public List<Userrole> getUserroleList() {
         return userroleList;
     }
 
     public void setUserroleList(List<Userrole> userroleList) {
         this.userroleList = userroleList;
     }
 
     public boolean addUserrole(Userrole userrole) {
         return this.userroleList.add(userrole);
     }
 
     public boolean removeUserrole(Userrole userrole) {
         return this.userroleList.remove(userrole);
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Action> getActionList() {
         return actionList;
     }
 
     public void setActionList(List<Action> actionList) {
         this.actionList = actionList;
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Youtube> getYoutubeList() {
         return youtubeList;
     }
 
     public void setYoutubeList(List<Youtube> youtubeList) {
         this.youtubeList = youtubeList;
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Survey> getSurveyList() {
         return surveyList;
     }
 
     public void setSurveyList(List<Survey> surveyList) {
         this.surveyList = surveyList;
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Invitation> getInvitationList() {
         return invitationList;
     }
 
     public void setInvitationList(List<Invitation> invitationList) {
         this.invitationList = invitationList;
     }
 
     @XmlElement(name = "location")
     public Location getIdLocation() {
         return idLocation;
     }
 
     public void setIdLocation(Location idLocation) {
         this.idLocation = idLocation;
     }
 
     @XmlTransient
     public List<Blogger> getBloggerList() {
         return bloggerList;
     }
 
     public void setBloggerList(List<Blogger> bloggerList) {
         this.bloggerList = bloggerList;
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Message> getMessageList() {
         return messageList;
     }
 
     public void setMessageList(List<Message> messageList) {
         this.messageList = messageList;
     }
 
     public boolean addMessage(Message message) {
         return this.messageList.add(message);
     }
 
     public boolean removeMessage(Message message) {
         return this.messageList.remove(message);
     }
 
     @JsonIgnore
     @XmlTransient
     public List<Facebookvisits> getFacebookvisitsList() {
         return facebookvisitsList;
     }
 
     public void setFacebookvisitsList(List<Facebookvisits> facebookvisitsList) {
         this.facebookvisitsList = facebookvisitsList;
     }
 
     @XmlTransient
     public List<Facebookdata> getFacebookdataList() {
         return facebookdataList;
     }
 
     public void setFacebookdataList(List<Facebookdata> facebookdataList) {
         this.facebookdataList = facebookdataList;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idCampaign != null ? idCampaign.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof Campaign)) {
             return false;
         }
         Campaign other = (Campaign) object;
         if ((this.idCampaign == null && other.idCampaign != null) || (this.idCampaign != null && !this.idCampaign.equals(other.idCampaign))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "de.fhg.fokus.persistence.Campaign[ idCampaign=" + idCampaign + " ]";
     }
 }
