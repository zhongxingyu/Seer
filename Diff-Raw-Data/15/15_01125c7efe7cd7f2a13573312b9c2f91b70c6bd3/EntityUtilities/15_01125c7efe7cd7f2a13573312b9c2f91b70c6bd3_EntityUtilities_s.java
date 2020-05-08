 package org.jboss.pressgang.ccms.filter.utils;
 
 import javax.persistence.EntityManager;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.util.Version;
 import org.hibernate.Session;
 import org.hibernate.envers.AuditReader;
 import org.hibernate.envers.AuditReaderFactory;
 import org.hibernate.envers.query.AuditEntity;
 import org.hibernate.envers.query.AuditQuery;
 import org.hibernate.search.FullTextSession;
 import org.hibernate.search.Search;
 import org.jboss.pressgang.ccms.filter.base.IFieldFilter;
 import org.jboss.pressgang.ccms.model.Category;
 import org.jboss.pressgang.ccms.model.Filter;
 import org.jboss.pressgang.ccms.model.FilterCategory;
 import org.jboss.pressgang.ccms.model.FilterField;
 import org.jboss.pressgang.ccms.model.FilterLocale;
 import org.jboss.pressgang.ccms.model.FilterTag;
 import org.jboss.pressgang.ccms.model.Project;
 import org.jboss.pressgang.ccms.model.Tag;
 import org.jboss.pressgang.ccms.model.Topic;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class EntityUtilities {
     private static final Logger log = LoggerFactory.getLogger(EntityUtilities.class);
 
     /**
      * This function takes the url parameters and uses them to populate a Filter object
      */
     public static Filter populateFilter(final EntityManager entityManager, final Map<String, String> paramMap, final String filterName,
             final String tagPrefix, final String groupTagPrefix, final String categoryInternalPrefix, final String categoryExternalPrefix,
             final String localePrefix, final IFieldFilter fieldFilter) {
         // attempt to get the filter id from the url
         Integer filterId = null;
         if (paramMap.containsKey(filterName)) {
             final String filterQueryParam = paramMap.get(filterName);
 
             try {
                 filterId = Integer.parseInt(filterQueryParam);
             } catch (final Exception ex) {
                 // filter value was not an integer
                 filterId = null;
 
                 log.debug("The filter ID URL query parameter was not an integer. Got " + filterQueryParam + ". Probably a malformed URL.",
                         ex);
             }
         }
 
         Filter filter = null;
 
         /* First attempt to populate the filter from a filterID variable */
         if (filterId != null) {
             filter = entityManager.find(Filter.class, filterId);
         }
 
         /* If that fails, use the other URL params */
         if (filter == null) {
             filter = new Filter();
 
             for (final String key : paramMap.keySet()) {
                 final boolean tagVar = tagPrefix != null && key.startsWith(tagPrefix);
                 final boolean groupTagVar = groupTagPrefix != null && key.startsWith(groupTagPrefix);
                 final boolean catIntVar = categoryInternalPrefix != null && key.startsWith(categoryInternalPrefix);
                 final boolean catExtVar = categoryExternalPrefix != null && key.startsWith(categoryExternalPrefix);
                 final boolean localeVar = localePrefix != null && key.matches("^" + localePrefix + "\\d*$");
                 final String state = paramMap.get(key);
 
                 // add the filter category states
                 if (catIntVar || catExtVar) {
                     /*
                      * get the category and project id data from the variable name
                      */
                     final String catProjDetails = catIntVar ? key.replaceFirst(categoryInternalPrefix, "") : key.replaceFirst(
                             categoryExternalPrefix, "");
                     // split the category and project id out of the data
                     final String[] catProjID = catProjDetails.split("-");
 
                     /*
                      * some validity checks. make sure we have one or two strings after the split.
                      */
                     if (catProjID.length != 1 && catProjID.length != 2) continue;
 
                     // try to get the category and project ids
                     Integer catID = null;
                     Integer projID = null;
                     try {
                         catID = Integer.parseInt(catProjID[0]);
 
                         /*
                          * if the array has just one element, we have only specified the category. in this case the project is
                          * the common project
                          */
                         if (catProjID.length == 2) projID = Integer.parseInt(catProjID[1]);
                     } catch (final Exception ex) {
                         log.debug("Was expecting an integer. Got " + catProjID[0] + ". Probably a malformed URL.", ex);
                         continue;
                     }
 
                     // at this point we have found a url variable that
                     // contains a catgeory and project id
 
                     final Category category = entityManager.find(Category.class, catID);
                     final Project project = projID != null ? entityManager.find(Project.class, projID) : null;
 
                     Integer dbState;
 
                     if (catIntVar) {
                         if (state.equals(CommonFilterConstants.AND_LOGIC)) dbState = CommonFilterConstants.CATEGORY_INTERNAL_AND_STATE;
                         else dbState = CommonFilterConstants.CATEGORY_INTERNAL_OR_STATE;
                     } else {
                         if (state.equals(CommonFilterConstants.AND_LOGIC)) dbState = CommonFilterConstants.CATEGORY_EXTERNAL_AND_STATE;
                         else dbState = CommonFilterConstants.CATEGORY_EXTERNAL_OR_STATE;
                     }
 
                     final FilterCategory filterCategory = new FilterCategory();
                     filterCategory.setFilter(filter);
                     filterCategory.setProject(project);
                     filterCategory.setCategory(category);
                     filterCategory.setCategoryState(dbState);
 
                     filter.getFilterCategories().add(filterCategory);
                 }
 
                 // add the filter tag states
                 else if (tagVar) {
                     try {
                         final Integer tagId = Integer.parseInt(key.replaceFirst(tagPrefix, ""));
                         final Integer intState = Integer.parseInt(state);
 
                         // get the Tag object that the tag id represents
                         final Tag tag = entityManager.getReference(Tag.class, tagId);
 
                         if (tag != null) {
                             final FilterTag filterTag = new FilterTag();
                             filterTag.setTag(tag);
                             filterTag.setTagState(intState);
                             filterTag.setFilter(filter);
                             filter.getFilterTags().add(filterTag);
                         }
                     } catch (final Exception ex) {
                         log.debug("Probably an invalid tag query parameter. Parameter: " + key + " Value: " + state, ex);
                     }
                 } else if (groupTagVar) {
                     final Integer tagId = Integer.parseInt(key.replaceFirst(groupTagPrefix, ""));
                     // final Integer intState = Integer.parseInt(state);
 
                     // get the Tag object that the tag id represents
                     final Tag tag = entityManager.getReference(Tag.class, tagId);
 
                     if (tag != null) {
                         final FilterTag filterTag = new FilterTag();
                         filterTag.setTag(tag);
                         filterTag.setTagState(CommonFilterConstants.GROUP_TAG_STATE);
                         filterTag.setFilter(filter);
                         filter.getFilterTags().add(filterTag);
                     }
                 } else if (localeVar) {
                     try {
                         final String localeName = state.replaceAll("\\d", "");
                         final Integer intState = Integer.parseInt(state.replaceAll("[^\\d]", ""));
 
                         final FilterLocale filterLocale = new FilterLocale();
                         filterLocale.setLocaleName(localeName);
                         filterLocale.setLocaleState(intState);
                         filterLocale.setFilter(filter);
                         filter.getFilterLocales().add(filterLocale);
                     } catch (final Exception ex) {
                         log.debug("Probably an invalid locale query parameter. Parameter: " + key + " Value: " + state, ex);
                     }
                 }
 
                 // add the filter field states
                 else {
                     if (fieldFilter.hasFieldName(key)) {
                         final FilterField filterField = new FilterField();
                         filterField.setFilter(filter);
                         filterField.setField(key);
                         filterField.setValue(state);
                         filterField.setDescription(fieldFilter.getFieldDesc(key));
                         filter.getFilterFields().add(filterField);
                     }
                 }
 
             }
 
         }
 
         return filter;
     }
 
     @SuppressWarnings("unchecked")
     public static <E> List<Integer> getEditedEntities(final EntityManager entityManager, final Class<E> type, final String pkColumnName,
             final DateTime startDate, final DateTime endDate) {
         if (startDate == null && endDate == null) return null;
 
         final AuditReader reader = AuditReaderFactory.get(entityManager);
 
         final AuditQuery query = reader.createQuery().forRevisionsOfEntity(type, true, false).addOrder(
                 AuditEntity.revisionProperty("timestamp").asc()).addProjection(
                 AuditEntity.property("originalId." + pkColumnName).distinct());
 
         if (startDate != null) query.add(AuditEntity.revisionProperty("timestamp").ge(startDate.toDate().getTime()));
 
         if (endDate != null) query.add(AuditEntity.revisionProperty("timestamp").le(endDate.toDate().getTime()));
 
         final List<Integer> entityIds = query.getResultList();
 
         return entityIds;
     }
 
     /**
      * @return A comma separated list of topic ids that have been included in a content spec
      * @throws Exception
      */
     public static List<Integer> getTopicsInContentSpec(final EntityManager entityManager, final Integer contentSpecId) {
         try {
             final ContentSpec contentSpec = entityManager.find(ContentSpec.class, contentSpecId);
 
             if (contentSpec == null) return null;
 
            final List<Integer> topicIds = new ArrayList<Integer>();
             for (final CSNode node : contentSpec.getCSNodes()) {
                if (!node.getCSNodeType().equals(CommonConstants.CS_NODE_TOPIC) && !node.getCSNodeType().equals(
                        CommonConstants.CS_NODE_INNER_TOPIC)) {
                     continue;
                 } else if (node.getEntityId() != null) {
                     topicIds.add(node.getEntityId());
                 }
             }
         } catch (final Exception ex) {
             log.warn("An invalid Topic ID was stored for a Content Spec in the database, or the topic was not a valid content spec", ex);
         }
 
         return null;
     }
 
     public static List<Integer> getOutgoingRelatedTopicIDs(final EntityManager entityManager, final Integer topicRelatedTo) {
         try {
             if (topicRelatedTo != null) {
                 final Topic topic = entityManager.find(Topic.class, topicRelatedTo);
                 return topic.getRelatedTopicIDs();
             }
         } catch (final Exception ex) {
             log.error(topicRelatedTo + " is probably not a valid Topic ID", ex);
         }
 
         return null;
     }
 
     public static List<Integer> getIncomingRelatedTopicIDs(final EntityManager entityManager, final Integer topicRelatedFrom) {
         try {
             if (topicRelatedFrom != null) {
                 final Topic topic = entityManager.find(Topic.class, topicRelatedFrom);
                 return topic.getIncomingRelatedTopicIDs();
             }
         } catch (final Exception ex) {
             log.error(topicRelatedFrom + " is probably not a valid Topic ID", ex);
         }
 
         return null;
     }
 
     @SuppressWarnings("unchecked")
     public static List<Integer> getTextSearchTopicMatch(final EntityManager entityManager, final String phrase) {
         final List<Integer> retValue = new ArrayList<Integer>();
 
         try {
             // get the Hibernate session from the EntityManager
             final Session session = (Session) entityManager.getDelegate();
             // get a Hibernate full text session. we use the Hibernate version,
             // instead of the JPA version,
             // because we can use the Hibernate versions to do projections
             final FullTextSession fullTextSession = Search.getFullTextSession(session);
             // create a query parser
             final QueryParser parser = new QueryParser(Version.LUCENE_31, "TopicSearchText",
                     fullTextSession.getSearchFactory().getAnalyzer(Topic.class));
             // parse the query string
             final Query query = parser.parse(phrase);
 
             // build a lucene query
             /*
              * final org.apache.lucene.search.Query query = qb .keyword() .onFields("TopicSearchText") .matching(phrase)
              * .createQuery();
              */
 
             // build a hibernate query
             final org.hibernate.search.FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Topic.class);
             // set the projection to return the id's of any topic's that match
             // the query
             hibQuery.setProjection("topicId");
             // get the results. because we setup a projection, there is no trip
             // to the database
             final List<Object[]> results = hibQuery.list();
             // extract the data into the List<Integer>
             for (final Object[] projection : results) {
                 final Integer id = (Integer) projection[0];
                 retValue.add(id);
             }
         } catch (final Exception ex) {
             log.error("Probably an error using Lucene", ex);
         }
 
         /*
          * an empty list will be interpreted as no restriction as opposed to return none. so add a non existent topic id so no
          * matches are made
          */
         if (retValue.size() == 0) retValue.add(-1);
 
         return retValue;
     }
 }
