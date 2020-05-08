 package org.jboss.pressgang.ccms.model;
 
 import static javax.persistence.GenerationType.IDENTITY;
 
 import javax.persistence.Cacheable;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.hibernate.annotations.BatchSize;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.envers.Audited;
 import org.hibernate.validator.constraints.NotBlank;
 import org.jboss.pressgang.ccms.model.base.AuditedEntity;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.contentspec.TranslatedCSNode;
 import org.jboss.pressgang.ccms.model.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 @Entity
 @Audited
 @Cacheable
 @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
 @Table(name = "TranslatedTopicData",
         uniqueConstraints = @UniqueConstraint(columnNames = {"TranslatedTopicID", "TranslationLocale", "TranslatedCSNodeID"}))
 public class TranslatedTopicData extends AuditedEntity implements java.io.Serializable {
     private static final long serialVersionUID = 7470594104954257672L;
     public static final String SELECT_ALL_QUERY = "select translatedTopicData from TranslatedTopicData translatedTopicData";
 
     private Integer translatedTopicDataId;
     private TranslatedTopic translatedTopic;
     private TranslatedCSNode translatedCSNode;
     private String translatedXml;
     private String translatedXmlErrors;
     private String translatedXmlRendered;
     private String translationLocale;
     private String translatedXMLCondition;
     private Set<TranslatedTopicString> translatedTopicStrings = new HashSet<TranslatedTopicString>(0);
     private Date translatedXmlRenderedUpdated;
     private Integer translationPercentage = 0;
 
     @Transient
     public Integer getId() {
         return translatedTopicDataId;
     }
 
     @Id
     @GeneratedValue(strategy = IDENTITY)
     @Column(name = "TranslatedTopicDataID", unique = true, nullable = false)
     public Integer getTranslatedTopicDataId() {
         return translatedTopicDataId;
     }
 
     public void setTranslatedTopicDataId(Integer translatedTopicDataId) {
         this.translatedTopicDataId = translatedTopicDataId;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "TranslatedXMLRenderedUpdated", nullable = true, length = 0)
     public Date getTranslatedXmlRenderedUpdated() {
         return translatedXmlRenderedUpdated;
     }
 
     public void setTranslatedXmlRenderedUpdated(final Date translatedXmlRenderedUpdated) {
         this.translatedXmlRenderedUpdated = translatedXmlRenderedUpdated;
     }
 
     @ManyToOne
     @JoinColumn(name = "TranslatedTopicID", nullable = false)
     @NotNull
     public TranslatedTopic getTranslatedTopic() {
         return translatedTopic;
     }
 
     public void setTranslatedTopic(final TranslatedTopic translatedTopic) {
         this.translatedTopic = translatedTopic;
     }
 
     @ManyToOne
     @JoinColumn(name = "TranslatedCSNodeID")
     public TranslatedCSNode getTranslatedCSNode() {
         return translatedCSNode;
     }
 
     public void setTranslatedCSNode(final TranslatedCSNode translatedCSNode) {
         this.translatedCSNode = translatedCSNode;
     }
 
     @Column(name = "TranslatedXML", columnDefinition = "MEDIUMTEXT")
     @Size(max = 16777215)
     public String getTranslatedXml() {
         return translatedXml;
     }
 
     public void setTranslatedXml(final String translatedXml) {
         this.translatedXml = translatedXml;
     }
 
     @Column(name = "TranslatedXMLErrors", columnDefinition = "TEXT")
     @Size(max = 65535)
     public String getTranslatedXmlErrors() {
         return translatedXmlErrors;
     }
 
     public void setTranslatedXmlErrors(final String translatedXmlErrors) {
         this.translatedXmlErrors = translatedXmlErrors;
     }
 
     @Column(name = "TranslatedXMLRendered", columnDefinition = "MEDIUMTEXT")
     @Size(max = 16777215)
     public String getTranslatedXmlRendered() {
         return translatedXmlRendered;
     }
 
     public void setTranslatedXmlRendered(final String translatedXmlRendered) {
         this.translatedXmlRendered = translatedXmlRendered;
     }
 
     @Column(name = "TranslationLocale", nullable = false, length = 45)
     @NotNull(message = "{translatedtopic.locale.notBlank}")
     @NotBlank(message = "{translatedtopic.locale.notBlank}")
     @Size(max = 45)
     public String getTranslationLocale() {
         return translationLocale;
     }
 
     public void setTranslationLocale(final String translationLocale) {
         this.translationLocale = translationLocale;
     }
 
     @Column(name = "TranslationPercentage", nullable = false)
     @NotNull
     public Integer getTranslationPercentage() {
         return translationPercentage;
     }
 
     public void setTranslationPercentage(Integer translationPercentage) {
         this.translationPercentage = translationPercentage;
     }
 
     @Column(name = "TranslatedXMLCondition", nullable = false, length = 255)
    @Length(max = 255)
     public String getTranslatedXMLCondition() {
         return translatedXMLCondition;
     }
 
     public void setTranslatedXMLCondition(final String translatedXMLCondition) {
         this.translatedXMLCondition = translatedXMLCondition;
     }
 
     @OneToMany(fetch = FetchType.LAZY, mappedBy = "translatedTopicData", cascade = CascadeType.ALL, orphanRemoval = true)
     @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
     @BatchSize(size = Constants.DEFAULT_BATCH_SIZE)
     public Set<TranslatedTopicString> getTranslatedTopicStrings() {
         return translatedTopicStrings;
     }
 
     public void setTranslatedTopicStrings(final Set<TranslatedTopicString> translatedTopicStrings) {
         this.translatedTopicStrings = translatedTopicStrings;
     }
 
     @Transient
     public String getFormattedTranslatedXmlRenderedUpdated() {
         if (translatedXmlRenderedUpdated != null) {
             final SimpleDateFormat formatter = new SimpleDateFormat(CommonConstants.FILTER_DISPLAY_DATE_FORMAT);
             return formatter.format(translatedXmlRenderedUpdated);
         }
 
         return new String();
     }
 
     @Transient
     public List<TranslatedTopicString> getTranslatedTopicDataStringsArray() {
         final List<TranslatedTopicString> results = new ArrayList<TranslatedTopicString>(translatedTopicStrings);
         return results;
     }
 
     @Transient
     public List<TranslatedTopicData> getOutgoingRelatedTranslatedTopicData(final EntityManager entityManager) {
         final Topic enversTopic = translatedTopic.getEnversTopic(entityManager);
 
         return getRelatedTranslatedTopicData(entityManager, enversTopic.getOutgoingRelatedTopicsArray());
     }
 
     @Transient
     public List<TranslatedTopicData> getIncomingRelatedTranslatedTopicData(final EntityManager entityManager) {
         final Topic enversTopic = translatedTopic.getEnversTopic(entityManager);
 
         return getRelatedTranslatedTopicData(entityManager, enversTopic.getIncomingRelatedTopicsArray());
     }
 
     @Transient
     private List<TranslatedTopicData> getRelatedTranslatedTopicData(final EntityManager entityManager, final List<Topic> relatedTopics) {
 
         final List<TranslatedTopicData> relatedTranslatedTopicDatas = new ArrayList<TranslatedTopicData>();
         for (final Topic relatedTopic : relatedTopics) {
             // Get the related TranslatedTopicData for the related TranslatedTopic
             final List<TranslatedTopicData> translatedTopics = relatedTopic.getTranslatedTopics(entityManager, null);
 
             for (final TranslatedTopicData relatedTranslation : translatedTopics) {
                 if (relatedTranslation.getTranslationLocale().equals(translationLocale)) {
                     relatedTranslatedTopicDatas.add(relatedTranslation);
                 }
             }
         }
 
         return relatedTranslatedTopicDatas;
     }
 
     @Transient
     public TranslatedTopicData getLatestRelatedTranslationDataByTopicID(final EntityManager entityManager, final int topicId) {
         final List<TranslatedTopicData> relatedOutgoingTranslatedTopicDatas = getOutgoingRelatedTranslatedTopicData(entityManager);
         TranslatedTopicData relatedTranslatedTopicData = null;
         if (relatedOutgoingTranslatedTopicDatas != null) {
             // Loop through the related TranslatedTopicData to find the latest complete translation
             for (final TranslatedTopicData translatedTopicData : relatedOutgoingTranslatedTopicDatas) {
                 if (translatedTopicData.getTranslatedTopic().getTopicId().equals(topicId)
                         // Check that the translation is complete
                         && translatedTopicData.getTranslationPercentage() >= 100
                         // Check to see if this TranslatedTopic revision is higher then the current revision
                         && (relatedTranslatedTopicData == null || relatedTranslatedTopicData.getTranslatedTopic().getTopicRevision() <
                         translatedTopicData.getTranslatedTopic().getTopicRevision())) {
                     relatedTranslatedTopicData = translatedTopicData;
                 }
             }
         }
         return relatedTranslatedTopicData;
     }
 
     @Transient
     public List<TranslatedTopicData> getOutgoingDummyFilledRelatedTranslatedTopicDatas(final EntityManager entityManager) {
         final List<TranslatedTopicData> outgoingRelatedTranslatedTopicDatas = getOutgoingRelatedTranslatedTopicData(entityManager);
 
         final List<TranslatedTopicData> results = getDummyFilledRelatedTranslatedTopicData(entityManager,
                 outgoingRelatedTranslatedTopicDatas, getTranslatedTopic().getEnversTopic(entityManager).getOutgoingRelatedTopicsArray());
         return results;
     }
 
     @Transient
     public List<TranslatedTopicData> getIncomingDummyFilledRelatedTranslatedTopicDatas(final EntityManager entityManager) {
         final List<TranslatedTopicData> incomingRelatedTranslatedTopicDatas = getIncomingRelatedTranslatedTopicData(entityManager);
 
         final List<TranslatedTopicData> results = getDummyFilledRelatedTranslatedTopicData(entityManager,
                 incomingRelatedTranslatedTopicDatas, getTranslatedTopic().getEnversTopic(entityManager).getIncomingRelatedTopicsArray());
         return results;
     }
 
     @Transient
     private List<TranslatedTopicData> getDummyFilledRelatedTranslatedTopicData(final EntityManager entityManager,
             final List<TranslatedTopicData> currentRelatedTranslatedTopicData, final List<Topic> enversRelatedTopicData) {
         final List<TranslatedTopicData> relationships = new ArrayList<TranslatedTopicData>();
 
         // Get the latest complete versions of the translated topics
         if (currentRelatedTranslatedTopicData != null) {
             final Map<Integer, TranslatedTopicData> translatedTopics = new HashMap<Integer, TranslatedTopicData>();
 
             for (final TranslatedTopicData translatedTopicData : currentRelatedTranslatedTopicData) {
                 final Integer topicId = translatedTopicData.getTranslatedTopic().getTopicId();
 
                 if (
                     // Check that the translation is complete
                         translatedTopicData.getTranslationPercentage() >= 100
                                 // Check that a related topic hasn't been set or the topics revision is higher then the current topic
                                 // revision
                                 && (!translatedTopics.containsKey(
                                 topicId) || translatedTopicData.getTranslatedTopic().getTopicRevision() > translatedTopics.get(
                                 topicId).getTranslatedTopic().getTopicRevision())) {
                     translatedTopics.put(topicId, translatedTopicData);
                 }
             }
 
             // Loop through and create dummy relationships for topics that haven't been translated yet
             for (final Topic topic : enversRelatedTopicData) {
                 if (!translatedTopics.containsKey(topic.getId())) {
                     final TranslatedTopicData dummyTranslation = createDummyTranslatedTopicData(entityManager, topic);
                     translatedTopics.put(topic.getId(), dummyTranslation);
                 }
             }
 
             for (final Integer topicId : translatedTopics.keySet()) {
                 relationships.add(translatedTopics.get(topicId));
             }
         }
         return relationships;
     }
 
     @Transient
     public TranslatedTopicData createDummyTranslatedTopicData(final EntityManager entityManager, final Topic topic) {
         final TranslatedTopicData translatedTopicData = new TranslatedTopicData();
         final TranslatedTopic translatedTopic = new TranslatedTopic();
 
         translatedTopic.setTopicId(topic.getId());
 
         // find the revision for the related topic
         Number topicRevision = 0;
         for (final Number revision : EnversUtilities.getRevisions(entityManager, topic)) {
             if (revision.longValue() <= this.translatedTopic.getTopicRevision().longValue() && revision.longValue() > topicRevision
                     .longValue())
                 topicRevision = revision;
         }
 
         translatedTopic.setTopicRevision(topicRevision.intValue());
         translatedTopicData.setTranslatedTopic(translatedTopic);
 
         translatedTopicData.setTranslatedTopicDataId(topic.getId() * -1);
         translatedTopicData.setTranslatedXml(topic.getTopicXML());
         translatedTopicData.setTranslationPercentage(100);
         translatedTopicData.setTranslatedXmlRendered(topic.getTopicRendered());
         translatedTopicData.setTranslationLocale(topic.getTopicLocale());
 
         return translatedTopicData;
     }
 
     @Transient
     public Boolean containsFuzzyTranslation() {
         for (final TranslatedTopicString translatedTopicString : getTranslatedTopicStrings()) {
             if (translatedTopicString.getFuzzyTranslation()) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Transient
     public void addTranslatedTopicString(final TranslatedTopicString translatedTopicString) {
         getTranslatedTopicStrings().add(translatedTopicString);
         translatedTopicString.setTranslatedTopicData(this);
     }
 
     @Transient
     public void removeTranslatedTopicString(final TranslatedTopicString translatedTopicString) {
         getTranslatedTopicStrings().remove(translatedTopicString);
         translatedTopicString.setTranslatedTopicData(null);
     }
 }
