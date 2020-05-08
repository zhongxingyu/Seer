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
 import javax.xml.bind.annotation.XmlRootElement; import org.codehaus.jackson.map.annotate.JsonSerialize;
 import javax.xml.bind.annotation.XmlTransient;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  *
  * @author Hannes Gorges
  */
 @Entity
 @Table(name = "comment")
 @XmlRootElement  @JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
 @NamedQueries({
     @NamedQuery(name = "Comment.findAll", query = "SELECT c FROM Comment c"),
     @NamedQuery(name = "Comment.findByIdComment", query = "SELECT c FROM Comment c WHERE c.idComment = :idComment"),
     @NamedQuery(name = "Comment.findByAnnotation", query = "SELECT c FROM Comment c WHERE c.annotation = :annotation"),
     @NamedQuery(name = "Comment.findByNetwork", query = "SELECT c FROM Comment c WHERE c.network = :network"),
     @NamedQuery(name = "Comment.findByNetworkCommentId", query = "SELECT c FROM Comment c WHERE c.networkCommentId = :networkCommentId"),
     @NamedQuery(name = "Comment.findByNetworkCommentUrl", query = "SELECT c FROM Comment c WHERE c.networkCommentUrl = :networkCommentUrl"),
     @NamedQuery(name = "Comment.findByCreateTime", query = "SELECT c FROM Comment c WHERE c.createTime = :createTime"),
    @NamedQuery(name = "Comment.findByUserProfileUrl", query = "SELECT c FROM Comment c WHERE c.userProfileUrl = :userProfileUrl")})
 public class Comment implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @NotNull
     @Column(name = "idComment")
     private Integer idComment;
     @Column(name = "annotation")
     private Boolean annotation;
     @Lob
     @Size(max = 65535)
     @Column(name = "content")
     private String content;
     @Size(max = 255)
     @Column(name = "network")
     private String network;
     @Size(max = 255)
     @Column(name = "networkCommentId")
     private String networkCommentId;
     @Size(max = 255)
     @Column(name = "networkCommentUrl")
     private String networkCommentUrl;
     @Column(name = "createTime")
     @Temporal(TemporalType.TIMESTAMP)
     private Date createTime;
     @Size(max = 255)
     @Column(name = "authorName")
     private String authorName;
     @Size(max = 255)
     @Column(name = "authorProfileUrl")
     private String authorProfileUrl;
     @OneToMany(mappedBy = "idComment")
     private List<Publisheditem> publisheditemList;
     @JoinColumn(name = "idMessage", referencedColumnName = "idMessage")
     @ManyToOne(optional = false)
     private Message idMessage;
     @JoinColumn(name = "idUserData", referencedColumnName = "idUserData")
     @ManyToOne
     private Userdata idUserData;
 
     public Comment() {
     }
 
     public Comment(Integer idComment) {
         this.idComment = idComment;
     }
 
     public Integer getIdComment() {
         return idComment;
     }
 
     public void setIdComment(Integer idComment) {
         this.idComment = idComment;
     }
 
     public Boolean getAnnotation() {
         return annotation;
     }
 
     public void setAnnotation(Boolean annotation) {
         this.annotation = annotation;
     }
 
     public String getContent() {
         return content;
     }
 
     public void setContent(String content) {
         this.content = content;
     }
 
     public String getNetwork() {
         return network;
     }
 
     public void setNetwork(String network) {
         this.network = network;
     }
 
     public String getNetworkCommentId() {
         return networkCommentId;
     }
 
     public void setNetworkCommentId(String networkCommentId) {
         this.networkCommentId = networkCommentId;
     }
 
     public String getNetworkCommentUrl() {
         return networkCommentUrl;
     }
 
     public void setNetworkCommentUrl(String networkCommentUrl) {
         this.networkCommentUrl = networkCommentUrl;
     }
 
     public Date getCreateTime() {
         return createTime;
     }
 
     public void setCreateTime(Date createTime) {
         this.createTime = createTime;
     }   
 
        @JsonIgnore     @XmlTransient
     public List<Publisheditem> getPublisheditemList() {
         return publisheditemList;
     }
 
     public void setPublisheditemList(List<Publisheditem> publisheditemList) {
         this.publisheditemList = publisheditemList;
     }
 
      @JsonIgnore     @XmlTransient
     public Message getIdMessage() {
         return idMessage;
     }
 
     public void setIdMessage(Message idMessage) {
         this.idMessage = idMessage;
     }
 
      @JsonIgnore     @XmlTransient
     public Userdata getIdUserData() {
         return idUserData;
     }
 
     public void setIdUserData(Userdata idUserData) {
         this.idUserData = idUserData;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (idComment != null ? idComment.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         if (!(object instanceof Comment)) {
             return false;
         }
         Comment other = (Comment) object;
         if ((this.idComment == null && other.idComment != null) || (this.idComment != null && !this.idComment.equals(other.idComment))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "de.fhg.fokus.persistence.Comment[ idComment=" + idComment + " ]";
     }
 
     /**
      * @return the authorName
      */
     public String getAuthorName() {
         return authorName;
     }
 
     /**
      * @param authorName the authorName to set
      */
     public void setAuthorName(String authorName) {
         this.authorName = authorName;
     }
 
     /**
      * @return the authorProfileUrl
      */
     public String getAuthorProfileUrl() {
         return authorProfileUrl;
     }
 
     /**
      * @param authorProfileUrl the authorProfileUrl to set
      */
     public void setAuthorProfileUrl(String authorProfileUrl) {
         this.authorProfileUrl = authorProfileUrl;
     }
     
 }
