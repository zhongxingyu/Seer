 package org.jboss.pressgang.ccms.server.utils;
 
 import static ch.lambdaj.Lambda.filter;
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.collection.LambdaCollections.with;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.collection.IsArray.array;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 
 import com.j2bugzilla.base.BugzillaConnector;
 import com.j2bugzilla.base.ECSBug;
 import com.j2bugzilla.rpc.BugSearch;
 import com.j2bugzilla.rpc.GetBug;
 import com.j2bugzilla.rpc.LogIn;
 import org.jboss.pressgang.ccms.contentspec.constants.CSConstants;
 import org.jboss.pressgang.ccms.model.BlobConstants;
 import org.jboss.pressgang.ccms.model.BugzillaBug;
 import org.jboss.pressgang.ccms.model.Category;
 import org.jboss.pressgang.ccms.model.StringConstants;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.TagToCategory;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.TopicToBugzillaBug;
 import org.jboss.pressgang.ccms.model.TopicToTag;
 import org.jboss.pressgang.ccms.model.TopicToTopic;
 import org.jboss.pressgang.ccms.model.TopicToTopicSourceUrl;
 import org.jboss.pressgang.ccms.model.TranslatedTopic;
 import org.jboss.pressgang.ccms.model.TranslatedTopicData;
 import org.jboss.pressgang.ccms.model.sort.CategoryNameComparator;
 import org.jboss.pressgang.ccms.model.sort.TagNameComparator;
 import org.jboss.pressgang.ccms.model.sort.TagToCategorySortingComparator;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 import org.jboss.pressgang.ccms.utils.common.DocBookUtilities;
 import org.jboss.pressgang.ccms.utils.common.StringUtilities;
 import org.jboss.pressgang.ccms.utils.common.XMLUtilities;
 import org.jboss.pressgang.ccms.utils.common.XMLValidator;
 import org.jboss.pressgang.ccms.utils.common.ZipUtilities;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.utils.structures.NameIDSortMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class TopicUtilities {
     private static final Logger log = LoggerFactory.getLogger(TopicUtilities.class);
 
     /**
      * Creates a CSV string representation of all the topics in the provided list.
      *
      * @param entityManager
      * @param topicList     The topics to create a CSV representation for.
      * @return The CSV string representation of the topics.
      */
     public static String getCSVForTopics(final EntityManager entityManager, final List<Topic> topicList) {
         // Build the csv
         final StringBuilder csv = new StringBuilder(TopicUtilities.getCSVHeaderRow(entityManager));
 
         // loop through each topic
         for (final Topic topic : topicList)
             csv.append("\n").append(TopicUtilities.getCSVRow(entityManager, topic));
 
         return csv.toString();
     }
 
     public static byte[] getZIPTopicXMLDump(final List<Topic> topicList) {
         // build up the files that will make up the zip file
         final HashMap<String, byte[]> files = new HashMap<String, byte[]>();
 
         for (final Topic topic : topicList) {
             try {
                 files.put(topic.getTopicId() + ".xml",
                         topic.getTopicXML() == null ? "".getBytes("UTF-8") : topic.getTopicXML().getBytes("UTF-8"));
             } catch (UnsupportedEncodingException e) {
                 /* UTF-8 is a valid format so this should exception should never get thrown */
             }
         }
 
         byte[] zipFile = null;
         try {
             zipFile = ZipUtilities.createZip(files);
         } catch (final Exception ex) {
             log.error("Probably a stream error", ex);
             zipFile = null;
         }
 
         return zipFile;
     }
 
     /**
      * Returns the headers for the CSV columns
      *
      * @param entityManager
      * @return
      */
     @SuppressWarnings("unchecked")
     public static String getCSVHeaderRow(final EntityManager entityManager) {
         final List<Category> categories = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
         Collections.sort(categories, new CategoryNameComparator());
 
         final StringBuilder topicColumns = new StringBuilder(
                 StringUtilities.buildString(new String[]{"Topic ID", "Topic Title", "Topic Text", "Topic URL"}, ","));
 
         for (final Category category : categories)
             topicColumns.append("," + category.getCategoryName());
 
         return topicColumns.toString();
     }
 
     /**
      * Validate and Fix a topics relationships to ensure that the topics related topics are still matched by the Related Topics
      * themselves.
      *
      * @param topic The topic to validate and fix the relationships for.
      */
     public static void validateAndFixRelationships(final Topic topic) {
         /* remove relationships to this topic in the parent collection */
         final ArrayList<TopicToTopic> removeList = new ArrayList<TopicToTopic>();
         for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
             if (topicToTopic.getRelatedTopic().getTopicId().equals(topic.getTopicId())) removeList.add(topicToTopic);
 
         for (final TopicToTopic topicToTopic : removeList)
             topic.getParentTopicToTopics().remove(topicToTopic);
 
         /* remove relationships to this topic in the child collection */
         final ArrayList<TopicToTopic> removeChildList = new ArrayList<TopicToTopic>();
         for (final TopicToTopic topicToTopic : topic.getChildTopicToTopics())
             if (topicToTopic.getMainTopic().getTopicId().equals(topic.getTopicId())) removeChildList.add(topicToTopic);
 
         for (final TopicToTopic topicToTopic : removeChildList)
             topic.getChildTopicToTopics().remove(topicToTopic);
     }
 
     /**
      * Validate and Fix a topics tags so that mutually exclusive tags are enforced and also remove any tags that may have been
      * duplicated.
      *
      * @param topic The topic to fix the tags for.
      */
     public static void validateAndFixTags(final Topic topic) {
         /*
          * validate the tags that are applied to this topic. generally the gui should enforce these rules, with the exception of
          * the bulk tag apply function
          */
 
         // Create a collection of Categories mapped to TagToCategories, sorted by the Category sorting order
         final TreeMap<Category, ArrayList<TagToCategory>> tagDB = new TreeMap<Category, ArrayList<TagToCategory>>(
                 Collections.reverseOrder());
 
         for (final TopicToTag topicToTag : topic.getTopicToTags()) {
             final Tag tag = topicToTag.getTag();
             for (final TagToCategory tagToCategory : tag.getTagToCategories()) {
                 final Category category = tagToCategory.getCategory();
 
                 if (!tagDB.containsKey(category)) tagDB.put(category, new ArrayList<TagToCategory>());
 
                 tagDB.get(category).add(tagToCategory);
             }
         }
 
         // now remove conflicting tags
         for (final Category category : tagDB.keySet()) {
             /* sort by the tags position in the category */
             Collections.sort(tagDB.get(category), new TagToCategorySortingComparator(false));
 
             /*
              * because of the way we have ordered the tagDB collections, and the ArrayLists it contains, this process will
              * remove those tags that belong to lower priority categories, and lower priority tags in those categories
              */
             final ArrayList<TagToCategory> tagToCategories = tagDB.get(category);
 
             // remove tags in the same mutually exclusive categories
             if (category.isMutuallyExclusive() && tagToCategories.size() > 1) {
                 while (tagToCategories.size() > 1) {
                     final TagToCategory tagToCategory = tagToCategories.get(1);
                     /* get the lower priority tag */
                     final Tag removeTag = tagToCategory.getTag();
                     /* remove it from the tagDB collection */
                     tagToCategories.remove(tagToCategory);
 
                     /* and remove it from the tag collection */
                     final ArrayList<TopicToTag> removeTopicToTagList = new ArrayList<TopicToTag>();
                     for (final TopicToTag topicToTag : topic.getTopicToTags()) {
                         if (topicToTag.getTag().equals(removeTag)) removeTopicToTagList.add(topicToTag);
                     }
 
                     for (final TopicToTag removeTopicToTag : removeTopicToTagList) {
                         topic.getTopicToTags().remove(removeTopicToTag);
                     }
                 }
             }
 
             /* remove tags that are explicitly defined as mutually exclusive */
             for (final TagToCategory tagToCategory : tagToCategories) {
                 final Tag tag = tagToCategory.getTag();
                 for (final Tag exclusionTag : tag.getExcludedTags()) {
                     if (filter(having(on(TopicToTag.class).getTag(), equalTo(tagToCategory.getTag())),
                             topic.getTopicToTags()).size() != 0 && // make
                             /*
                              * sure that we have not removed this tag already
                              */
                             filter(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)),
                                     topic.getTopicToTags()).size() != 0 && // make
                             /*
                              * sure the exclusion tag exists
                              */
                             !exclusionTag.equals(tagToCategory.getTag())) // make
                     /*
                      * sure we are not trying to remove ourselves
                      */ {
                         with(topic.getTopicToTags()).remove(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)));
                     }
                 }
             }
         }
     }
 
     /**
      * Process a Topics XML and sync the topic title with the XML Title. Also reformat the XML using the formatting String
      * Constant rules.
      *
      * @param entityManager An open EntityManager instance to lookup formatting constants.
      * @param topic         The Topic to process the XML for.
      */
     protected static void processXML(final EntityManager entityManager, final Topic topic) {
         if (topic.isTaggedWith(Constants.CONTENT_SPEC_TAG_ID)) return;
 
         Document doc = null;
         try {
             doc = XMLUtilities.convertStringToDocument(topic.getTopicXML());
         } catch (SAXException ex) {
             // Do nothing with this as it's handled later by the validator
         } catch (Exception ex) {
             log.warn("An Error occurred transforming a XML String to a DOM Document", ex);
         }
         if (doc != null) {
             // Sync the topic title, if the topic is a regular topic
             if (isTopicNormalTopic(topic)) {
                 DocBookUtilities.setSectionTitle(topic.getTopicTitle(), doc);
             }
 
             // Get the XML elements that require special formatting/processing
             final StringConstants xmlElementsProperties = entityManager.find(StringConstants.class,
                     CommonConstants.XML_ELEMENTS_STRING_CONSTANT_ID);
 
             // Load the String Constants as Properties
             final Properties prop = new Properties();
             try {
                 prop.load(new StringReader(xmlElementsProperties.getConstantValue()));
             } catch (IOException ex) {
                 log.error("The XML Elements Properties file couldn't be loaded as a property file", ex);
             }
 
             // Find the XML elements that need formatting for different display rules.
             final String verbatimElementsString = prop.getProperty(CommonConstants.VERBATIM_XML_ELEMENTS_PROPERTY_KEY);
             final String inlineElementsString = prop.getProperty(CommonConstants.INLINE_XML_ELEMENTS_PROPERTY_KEY);
             final String contentsInlineElementsString = prop.getProperty(CommonConstants.CONTENTS_INLINE_XML_ELEMENTS_PROPERTY_KEY);
 
             final ArrayList<String> verbatimElements = verbatimElementsString == null ? new ArrayList<String>() : CollectionUtilities
                     .toArrayList(
                     verbatimElementsString.split("[\\s]*,[\\s]*"));
 
             final ArrayList<String> inlineElements = inlineElementsString == null ? new ArrayList<String>() : CollectionUtilities
                     .toArrayList(
                     inlineElementsString.split("[\\s]*,[\\s]*"));
 
             final ArrayList<String> contentsInlineElements = contentsInlineElementsString == null ? new ArrayList<String>() :
                     CollectionUtilities.toArrayList(
                     contentsInlineElementsString.split("[\\s]*,[\\s]*"));
 
             // Convert the document to a String applying the XML Formatting property rules
             topic.setTopicXML(XMLUtilities.convertNodeToString(doc, verbatimElements, inlineElements, contentsInlineElements, true));
         }
     }
 
     /**
      * Syncs the XML with the topic details, such as setting the topic title as the title of the XML.
      *
      * @param entityManager An open EntityManager instance to lookup formatting constants.
      * @param topic         The Topic to sync the XML for.
      */
     public static void syncXML(final EntityManager entityManager, final Topic topic) {
         /* remove line breaks from the title */
         if (topic.getTopicTitle() != null) topic.setTopicTitle(topic.getTopicTitle().replaceAll("\n", " ").trim());
         processXML(entityManager, topic);
     }
 
     /**
      * Get an Alphabetical Sorted list of Categories to Tags for a Topic. The sort is performed based on the Category Sort value
      * first and then Name.
      *
      * @param topic The topic to get the tags from.
      * @return A Sorted Tree map of categories to tags.
      */
     private static TreeMap<NameIDSortMap, ArrayList<Tag>> getCategoriesMappedToTags(final Topic topic) {
         final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = new TreeMap<NameIDSortMap, ArrayList<Tag>>();
 
         final Set<TopicToTag> topicToTags = topic.getTopicToTags();
         for (final TopicToTag topicToTag : topicToTags) {
             final Tag tag = topicToTag.getTag();
             final Set<TagToCategory> tagToCategories = tag.getTagToCategories();
 
             if (tagToCategories.size() == 0) {
                 final NameIDSortMap categoryDetails = new NameIDSortMap("Uncatagorised", -1, 0);
 
                 if (!tags.containsKey(categoryDetails)) tags.put(categoryDetails, new ArrayList<Tag>());
 
                 tags.get(categoryDetails).add(tag);
             } else {
                 for (final TagToCategory category : tagToCategories) {
                     final NameIDSortMap categoryDetails = new NameIDSortMap(category.getCategory().getCategoryName(),
                             category.getCategory().getCategoryId(),
                             category.getCategory().getCategorySort() == null ? 0 : category.getCategory().getCategorySort());
 
                     if (!tags.containsKey(categoryDetails)) tags.put(categoryDetails, new ArrayList<Tag>());
 
                     tags.get(categoryDetails).add(tag);
                 }
             }
         }
 
         return tags;
     }
 
     /**
      * Get a Comma Separated list of all the tags attached to a topic, grouped by category.
      *
      * @param topic The topic to get the tags from.
      * @return A CSV tag list for the topic.
      */
     public static String getCommaSeparatedTagList(final Topic topic) {
         final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = getCategoriesMappedToTags(topic);
 
         String tagsList = "";
         for (final NameIDSortMap key : tags.keySet()) {
             // sort alphabetically
             Collections.sort(tags.get(key), new TagNameComparator());
 
             if (tagsList.length() != 0) tagsList += " ";
 
             tagsList += key.getName() + ": ";
 
             String thisTagList = "";
 
             for (final Tag tag : tags.get(key)) {
                 if (thisTagList.length() != 0) thisTagList += ", ";
 
                 thisTagList += tag.getTagName();
             }
 
             tagsList += thisTagList + " ";
         }
 
         return tagsList;
     }
 
     /**
      * Get a CSV Row representation of a topic.
      *
      * @param entityManager An open EntityManager instance to lookup data.
      * @param topic         The topic to transform into the CSV row.
      * @return A Comma Separated Value list containing information about the topic.
      */
     @SuppressWarnings("unchecked")
     public static String getCSVRow(final EntityManager entityManager, final Topic topic) {
         // get a space separated list of source URLs
         String sourceUrls = "";
         for (final TopicToTopicSourceUrl url : topic.getTopicToTopicSourceUrls()) {
             if (sourceUrls.length() != 0) sourceUrls += " ";
             sourceUrls += url.getTopicSourceUrl().getSourceUrl();
         }
 
         String topicColumns = StringUtilities.cleanTextForCSV(topic.getTopicId().toString()) + "," + StringUtilities.cleanTextForCSV(
                 topic.getTopicTitle()) + "," + StringUtilities.cleanTextForCSV(
                 topic.getTopicText()) + "," + StringUtilities.cleanTextForCSV(sourceUrls);
 
         final List<Category> categories = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
         Collections.sort(categories, new CategoryNameComparator());
 
         for (final Category category : categories) {
             final List<TopicToTag> matchingTags = filter(having(on(TopicToTag.class).getTag().getTagToCategories().toArray(),
                     array(org.hamcrest.Matchers.hasProperty("category", equalTo(category)))), topic.getTopicToTags());
 
             String tags = "";
             for (final TopicToTag topicToTag : matchingTags) {
                 final Tag tag = topicToTag.getTag();
                 if (tags.length() != 0) tags += ", ";
                 tags += tag.getTagId() + ": " + tag.getTagName();
             }
 
             topicColumns += "," + StringUtilities.cleanTextForCSV(tags);
         }
 
         return topicColumns;
     }
 
     /**
      * Generates a HTML formatted and categorized list of the tags that are associated with this topic
      *
      * @return A HTML String to display in a table
      */
     public static String getTagsList(final Topic topic, final boolean brLineBreak) {
         // define the line breaks for html and for tooltips
         final String lineBreak = brLineBreak ? "<br/>" : " \n";
         final String boldStart = brLineBreak ? "<b>" : "";
         final String boldEnd = brLineBreak ? "</b>" : "";
 
         final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = getCategoriesMappedToTags(topic);
 
         String tagsList = "";
         for (final NameIDSortMap key : tags.keySet()) {
             // sort alphabetically
             Collections.sort(tags.get(key), new TagNameComparator());
 
             if (tagsList.length() != 0) tagsList += lineBreak;
 
             tagsList += boldStart + key.getName() + boldEnd + ": ";
 
             String thisTagList = "";
 
             for (final Tag tag : tags.get(key)) {
                 if (thisTagList.length() != 0) thisTagList += ", ";
 
                 thisTagList += "<span title=\"Tag ID: " + tag.getTagId() + " &#13;Tag Description: " + tag.getTagDescription() + "\">" +
                         tag.getTagName() + "</span>";
             }
 
             tagsList += thisTagList + " ";
         }
 
         return tagsList;
     }
 
     public static String getRelatedTopicsList(final Topic topic) {
         String topicList = "";
         for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics()) {
             final Topic relatedTopic = topicToTopic.getRelatedTopic();
 
             if (topicList.length() != 0) topicList += ", ";
 
             topicList += "<span title=\"" + relatedTopic.getTopicTitle() + " \n" + getCommaSeparatedTagList(
                     relatedTopic) + "\">" + topicToTopic.getRelatedTopic().getTopicId() + "</span>";
         }
         return topicList;
     }
 
     /**
      * Validate the XML, and save any errors
      *
      * @param entityManager  The EntityManager
      * @param blobConstantId The BlobConstants ID that is the DTD to validate against
      */
     public static void validateXML(final EntityManager entityManager, final Topic topic, final int blobConstantId) {
         if (topic.isTaggedWith(Constants.CONTENT_SPEC_TAG_ID)) return;
 
         if (entityManager == null) throw new IllegalArgumentException("entityManager cannot be null");
         if (blobConstantId < 0) throw new IllegalArgumentException("blobConstantId must be positive");
 
         final BlobConstants dtd = entityManager.find(BlobConstants.class, blobConstantId);
 
         if (dtd == null) throw new IllegalArgumentException("blobConstantId must be a valid BlobConstants entity id");
 
         try {
             final StringBuilder xmlErrors = new StringBuilder();
             final Document doc = XMLUtilities.convertStringToDocument(topic.getTopicXML());
 
             // Do a normal DTD validation on the topic
             final XMLValidator validator = new XMLValidator();
             if (validator.validateTopicXML(doc, dtd.getConstantName(), dtd.getConstantValue()) == null) {
                 final String errorText = validator.getErrorText();
                 if (errorText != null) {
                     xmlErrors.append(errorText);
                 }
             }
 
             if (!isTopicNormalTopic(topic)) {
                 if (topic.isTaggedWith(CSConstants.REVISION_HISTORY_TAG_ID)) {
                     // Make sure the revision history is an appendix
                     if (!doc.getDocumentElement().getNodeName().equals("appendix")) {
                         xmlErrors.append("Root element must be <appendix> for Revision History Topics.");
                     }
 
                     // Check to make sure that a revhistory entry exists
                     final NodeList revHistoryList = doc.getElementsByTagName("revhistory");
                     if (revHistoryList.getLength() == 0) {
                         xmlErrors.append("No <revhistory> element found. A <revhistory> must exist for Revision Histories.\n");
                     }
                 } else if (topic.isTaggedWith(CSConstants.LEGAL_NOTICE_TAG_ID)) {
                     // Make sure the Legal Notice is a legalnotice
                     if (!doc.getDocumentElement().getNodeName().equals("legalnotice")) {
                         xmlErrors.append("Root element must be <legalnotice> for Legal Notice Topics.");
                     }
                 }
             } else {
                 // Make sure the topic is a section
                 if (!doc.getDocumentElement().getNodeName().equals(DocBookUtilities.TOPIC_ROOT_NODE_NAME)) {
                     xmlErrors.append("Root element must be <" + DocBookUtilities.TOPIC_ROOT_NODE_NAME + "> for Topics.");
                 }
 
                 // Check the tables are valid
                 if (!validateTopicTables(doc)) {
                     xmlErrors.append("Table column declaration doesn't match the number of entry elements.\n");
                 }
             }
 
             if (xmlErrors.length() != 0) {
                 topic.setTopicXMLErrors(xmlErrors.toString());
             } else {
                 topic.setTopicXMLErrors(null);
             }
         } catch (SAXException e) {
             topic.setTopicXMLErrors(e.getMessage());
        } catch (Exception e) {
            topic.setTopicXMLErrors(e.getMessage());
         }
     }
 
     /**
      * Check to see if a Topic is a normal topic, instead of a Revision History or Legal Notice
      *
      * @param topic The topic to be checked.
      * @return True if the topic is a normal topic, otherwise false.
      */
     public static boolean isTopicNormalTopic(final Topic topic) {
         return !(topic.isTaggedWith(CSConstants.REVISION_HISTORY_TAG_ID) || topic.isTaggedWith(CSConstants.LEGAL_NOTICE_TAG_ID));
     }
 
     /**
      * Checks to see if the Rows, in XML Tables exceed the maximum number of columns.
      *
      * @param doc The XML DOM Document to be validated.
      * @return True if the XML is valid, otherwise false.
      */
     protected static boolean validateTopicTables(final Document doc) {
         final NodeList tables = doc.getElementsByTagName("table");
         for (int i = 0; i < tables.getLength(); i++) {
             final Element table = (Element) tables.item(i);
             if (!DocBookUtilities.validateTableRows(table)) {
                 return false;
             }
         }
 
         final NodeList informalTables = doc.getElementsByTagName("informaltable");
         for (int i = 0; i < informalTables.getLength(); i++) {
             final Element informalTable = (Element) informalTables.item(i);
             if (!DocBookUtilities.validateTableRows(informalTable)) {
                 return false;
             }
         }
 
         return true;
     }
 
     public static TranslatedTopic createTranslatedTopic(final EntityManager entityManager, final Integer topicId, final Number revision) {
         /*
          * Search to see if a translated topic already exists for the current revision
          */
         final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
         final CriteriaQuery<TranslatedTopic> criteriaQuery = criteriaBuilder.createQuery(TranslatedTopic.class);
         final Root<TranslatedTopic> root = criteriaQuery.from(TranslatedTopic.class);
         final Predicate whereTopicId = criteriaBuilder.equal(root.get("topicId"), topicId);
         final Predicate whereTopicRevision = criteriaBuilder.equal(root.get("topicRevision"), revision);
         criteriaQuery.where(criteriaBuilder.and(whereTopicId, whereTopicRevision));
 
         final TypedQuery<TranslatedTopic> query = entityManager.createQuery(criteriaQuery);
         final List<TranslatedTopic> result = query.getResultList();
 
         if (result.size() == 0) {
             final TranslatedTopic translatedTopic = new TranslatedTopic();
             translatedTopic.setTopicId(topicId);
             translatedTopic.setTopicRevision((Integer) revision);
 
             /*
              * Create a TranslationTopicData entity that represents the original locale
              */
             final Topic revisionTopic = entityManager.find(Topic.class, topicId);
             if (revisionTopic != null) {
                 final String locale = revisionTopic.getTopicLocale();
 
                 final TranslatedTopicData translatedTopicData = new TranslatedTopicData();
                 translatedTopicData.setTranslatedTopic(translatedTopic);
                 translatedTopicData.setTranslatedXml(revisionTopic.getTopicXML());
                 translatedTopicData.setTranslationLocale(locale == null ? CommonConstants.DEFAULT_LOCALE : locale);
                 translatedTopicData.setTranslationPercentage(100);
                 translatedTopic.getTranslatedTopicDatas().add(translatedTopicData);
             }
 
             return translatedTopic;
         }
 
         return null;
     }
 
     /**
      * Update the Bugzilla Bugs for a Topic by connecting and then searching for bugs from bugzilla.
      *
      * @param entityManager An open EntityManager instance.
      * @param topic         The topic to be updated.
      */
     public static void updateBugzillaBugs(final EntityManager entityManager, final Topic topic) {
         final String username = System.getProperty(CommonConstants.BUGZILLA_USERNAME_PROPERTY);
         final String password = System.getProperty(CommonConstants.BUGZILLA_PASSWORD_PROPERTY);
         final String url = System.getProperty(CommonConstants.BUGZILLA_URL_PROPERTY);
 
         // return if the system properties have not been setup properly
         if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() || url == null || url.trim()
                 .isEmpty())
             return;
 
         final LogIn login = new LogIn(username, password);
         final BugzillaConnector connector = new BugzillaConnector();
         final BugSearch<ECSBug> search = new BugSearch<ECSBug>(ECSBug.class);
 
         // Create a query that will return all bugs whose Build ID matches what is pre-populated by PressGang
         search.addQueryParam("f1", "cf_build_id");
         search.addQueryParam("o1", "regexp");
         search.addQueryParam("v1", topic.getTopicId() + CommonConstants.BUGZILLA_BUILD_ID_RE);
         search.addQueryParam("query_format", "advanced");
 
         try {
             connector.connectTo("https://" + url + "/xmlrpc.cgi");
             connector.executeMethod(login);
             connector.executeMethod(search);
 
             /* clear the mappings */
             while (topic.getTopicToBugzillaBugs().size() != 0) {
                 final TopicToBugzillaBug map = topic.getTopicToBugzillaBugs().iterator().next();
                 map.getBugzillaBug().getTopicToBugzillaBugs().remove(map);
                 topic.getTopicToBugzillaBugs().remove(map);
             }
 
             /* For each bug, get the bug details and comments */
             for (ECSBug bug : search.getSearchResults()) {
                 int id = bug.getID();
 
                 final GetBug<ECSBug> getBug = new GetBug<ECSBug>(ECSBug.class, id);
                 connector.executeMethod(getBug);
                 final ECSBug bugDetails = getBug.getBug();
 
                 final BugzillaBug bugzillaBug = new BugzillaBug();
                 bugzillaBug.setBugzillaBugBugzillaId(bugDetails.getID());
                 bugzillaBug.setBugzillaBugOpen(bugDetails.getIsOpen());
                 bugzillaBug.setBugzillaBugSummary(bugDetails.getSummary());
 
                 entityManager.persist(bugzillaBug);
 
                 final TopicToBugzillaBug mapping = new TopicToBugzillaBug();
                 mapping.setBugzillaBug(bugzillaBug);
                 mapping.setTopic(topic);
 
                 topic.getTopicToBugzillaBugs().add(mapping);
             }
 
         } catch (final Exception ex) {
             log.error("Probably an error searching Bugzilla", ex);
         }
     }
 
     public static String getXMLDoctypeString(final Topic topic) {
         if (topic.getXmlDoctype() == null) {
             return "None";
         } else if (topic.getXmlDoctype() == CommonConstants.DOCBOOK_45) {
             return "Docbook 4.5";
         } else if (topic.getXmlDoctype() == CommonConstants.DOCBOOK_50) {
             return "Docbook 5.0";
         }
 
         return "Unknown";
     }
 }
