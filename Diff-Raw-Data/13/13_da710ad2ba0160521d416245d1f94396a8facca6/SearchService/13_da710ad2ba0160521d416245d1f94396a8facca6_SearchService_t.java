 package com.amee.platform.search;
 
 import com.amee.base.domain.ResultsWrapper;
 import com.amee.base.transaction.TransactionController;
 import com.amee.domain.AMEEEntity;
 import com.amee.domain.ObjectType;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.DataItem;
 import com.amee.domain.data.ItemValue;
 import com.amee.domain.path.PathItem;
 import com.amee.domain.path.PathItemGroup;
 import com.amee.platform.science.Amount;
 import com.amee.service.data.DataService;
 import com.amee.service.environment.EnvironmentService;
 import com.amee.service.invalidation.InvalidationMessage;
 import com.amee.service.locale.LocaleService;
 import com.amee.service.metadata.MetadataService;
 import com.amee.service.path.PathItemService;
 import com.amee.service.tag.TagService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.*;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.NumericField;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.util.Version;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.context.ApplicationListener;
 
 import java.io.Reader;
 import java.util.*;
 
 public class SearchService implements ApplicationListener {
 
     protected class DocumentContext {
 
         // Current Data Category.
         private DataCategory dataCategory;
 
         // Should Data Item documents be handled when handling a Data Category.
         private boolean handleDataItems = false;
 
         // Work-in-progress List of Data Item Documents.
         private List<Document> dataItemDocs;
 
         // Current Data Item.
         private DataItem dataItem;
 
         // Current Data Item Document
         private Document dataItemDoc;
     }
 
     private final Log log = LogFactory.getLog(getClass());
 
     public final static DateTimeFormatter DATE_TO_SECOND = DateTimeFormat.forPattern("yyyyMMddHHmmss");
 
     public final static Analyzer STANDARD_ANALYZER = new StandardAnalyzer(Version.LUCENE_30);
     public final static Analyzer KEYWORD_ANALYZER = new KeywordAnalyzer();
     public final static Analyzer LOWER_CASE_KEYWORD_ANALYZER = new Analyzer() {
         public TokenStream tokenStream(String fieldName, Reader reader) {
             TokenStream result = new KeywordTokenizer(reader);
             result = new LowerCaseFilter(result);
             return result;
         }
     };
 
     @Autowired
     private TransactionController transactionController;
 
     @Autowired
     private EnvironmentService environmentService;
 
     @Autowired
     private DataService dataService;
 
     @Autowired
     private MetadataService metadataService;
 
     @Autowired
     private LocaleService localeService;
 
     @Autowired
     private TagService tagService;
 
     @Autowired
     private PathItemService pathItemService;
 
     @Autowired
     private SearchQueryService searchQueryService;
 
     @Autowired
     private LuceneService luceneService;
 
     private boolean clearIndex = false;
     private boolean indexDataCategories = false;
     private boolean indexDataItems = false;
 
     // Events
 
     public void onApplicationEvent(ApplicationEvent event) {
         if (InvalidationMessage.class.isAssignableFrom(event.getClass())) {
             onInvalidationMessage((InvalidationMessage) event);
         }
     }
 
     private void onInvalidationMessage(InvalidationMessage invalidationMessage) {
         if (!invalidationMessage.isLocal() && invalidationMessage.getObjectType().equals(ObjectType.DC)) {
             log.debug("onInvalidationMessage() Handling InvalidationMessage.");
             transactionController.begin(false);
             DataCategory dataCategory = dataService.getDataCategoryByUid(invalidationMessage.getEntityUid(), true);
             if (dataCategory != null) {
                 DocumentContext ctx = new DocumentContext();
                 ctx.dataCategory = dataCategory;
                 updateDataCategory(ctx);
             }
             transactionController.end();
         }
     }
 
     // Scheduled jobs.
 
     public void update() {
         updateCategories();
         updateDataItems();
     }
 
     /**
      * Update all Data Categories in the search index which have been modified in
      * the last one hour segment.
      */
     public void updateCategories() {
         log.debug("updateCategories()");
         transactionController.begin(false);
         DateTime anHourAgoRoundedUp = new DateTime().minusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
         List<DataCategory> dataCategories = dataService.getDataCategoriesModifiedWithin(
                 environmentService.getEnvironmentByName("AMEE"),
                 anHourAgoRoundedUp.toDate(),
                 anHourAgoRoundedUp.plusHours(1).toDate());
         for (DataCategory dataCategory : dataCategories) {
             DocumentContext ctx = new DocumentContext();
             ctx.dataCategory = dataCategory;
             updateDataCategory(ctx);
         }
         transactionController.end();
     }
 
     /**
      * Update all Data Categories & Data Items in the search index where the
      * Data Items have been modified in the last one hour segment.
      */
     public void updateDataItems() {
         log.debug("updateDataItems()");
         transactionController.begin(false);
         DateTime anHourAgoRoundedUp = new DateTime().minusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
         List<DataCategory> dataCategories = dataService.getDataCategoriesForDataItemsModifiedWithin(
                 environmentService.getEnvironmentByName("AMEE"),
                 anHourAgoRoundedUp.toDate(),
                 anHourAgoRoundedUp.plusHours(1).toDate());
         for (DataCategory dataCategory : dataCategories) {
             DocumentContext ctx = new DocumentContext();
             ctx.dataCategory = dataCategory;
             ctx.handleDataItems = true;
             updateDataCategory(ctx);
         }
         transactionController.end();
     }
 
     // Index & Document management.
 
     /**
      * Will update or create the whole search index.
      */
     public void init() {
         // Always make sure index is unlocked.
         luceneService.unlockIndex();
         // Clear the index?
         if (clearIndex) {
             luceneService.clearIndex();
         }
         // Add DataCategories?
         if (indexDataCategories) {
             buildDataCategories();
         }
     }
 
     /**
      * Add all DataCategories to the index.
      */
     protected void buildDataCategories() {
         log.info("handleDataCategories()");
         buildDataCategories(getDataCategoryUids());
     }
 
     /**
      * Get a Set of Data Category UIDs for all.
      *
      * @return Set of Data Category UIDs
      */
     protected Set<String> getDataCategoryUids() {
         log.debug("getDataCategoryUids()");
         transactionController.begin(false);
         // We need PathItems to exclude test DataCategories.
         PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environmentService.getEnvironmentByName("AMEE"));
         // Iterate over all DataCategories and gather DataCategory UIDs.
         Set<String> dataCategoryUids = new HashSet<String>();
         for (DataCategory dataCategory : dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"))) {
             PathItem pathItem = pathItemGroup.findByUId(dataCategory.getUid());
             if (pathItem == null || !pathItem.getFullPath().startsWith("/test")) {
                 dataCategoryUids.add(dataCategory.getUid());
             }
         }
         transactionController.end();
         return dataCategoryUids;
     }
 
     /**
      * Add all DataCategories to the index.
      *
      * @param dataCategoryUids UIDs of Data Categories to index.
      */
     protected void buildDataCategories(Set<String> dataCategoryUids) {
         log.info("handleDataCategories()");
         for (String uid : dataCategoryUids) {
             buildDataCategory(uid);
         }
     }
 
     protected void buildDataCategory(String dataCategoryUid) {
         log.info("buildDataCategory()");
         transactionController.begin(false);
         DocumentContext ctx = new DocumentContext();
         ctx.dataCategory = dataService.getDataCategoryByUid(dataCategoryUid);
         ctx.handleDataItems = indexDataItems;
         updateDataCategory(ctx);
         transactionController.end();
     }
 
     /**
      * Create all DataItem documents for the supplied DataCategory.
      *
      * @param ctx
      */
     public void handleDataItems(DocumentContext ctx) {
         ctx.dataItemDoc = null;
         ctx.dataItemDocs = null;
         // There are only Data Items for a Data Category if there is an Item Definition.
         if (ctx.dataCategory.getItemDefinition() != null) {
             log.info("handleDataItems() Starting... (" + ctx.dataCategory.toString() + ")");
             // Pre-cache metadata and locales for the Data Items.
             metadataService.loadMetadatasForItemValueDefinitions(ctx.dataCategory.getItemDefinition().getItemValueDefinitions());
             localeService.loadLocaleNamesForItemValueDefinitions(ctx.dataCategory.getItemDefinition().getItemValueDefinitions());
             List<DataItem> dataItems = dataService.getDataItems(ctx.dataCategory, false);
             metadataService.loadMetadatasForDataItems(dataItems);
             localeService.loadLocaleNamesForDataItems(dataItems);
             // Are we handling Data Items?
             if (ctx.handleDataItems) {
                 ctx.dataItemDocs = new ArrayList<Document>();
             }
             for (DataItem dataItem : dataItems) {
                 ctx.dataItem = dataItem;
                 // Are we handling Data Items?
                 if (ctx.handleDataItems) {
                     // Create new Data Item Document.
                     ctx.dataItemDoc = getDocumentForDataItem(dataItem);
                     ctx.dataItemDocs.add(ctx.dataItemDoc);
                 }
                 // Handle the Data Item Values.
                 handleDataItemValues(ctx);
             }
             // Clear caches.
             metadataService.clearMetadatas();
             localeService.clearLocaleNames();
             // Update Data Items in index (if relevant).
             if (ctx.handleDataItems) {
                 // Ensure we clear existing DataItem Documents for this Data Category.
                 searchQueryService.removeDataItems(ctx.dataCategory);
                 // Add the new Data Item Documents to the index (if any).
                 luceneService.addDocuments(ctx.dataItemDocs);
             }
             log.info("handleDataItems() ...done (" + ctx.dataCategory.toString() + ").");
         } else {
             log.debug("handleDataItems() DataCategory does not have items: " + ctx.dataCategory.toString());
             // Ensure we clear any Data Item Documents for this Data Category.
             searchQueryService.removeDataItems(ctx.dataCategory);
         }
     }
 
     /**
      * Update or remove Data Category & Data Items from the search index.
      *
      * @param ctx
      */
     protected void updateDataCategory(DocumentContext ctx) {
         log.debug("updateDataCategory() DataCategory: " + ctx.dataCategory.toString());
         if (!ctx.dataCategory.isTrash()) {
             Document document = searchQueryService.getDocument(ctx.dataCategory);
             if (document != null) {
                 Field modifiedField = document.getField("entityModified");
                 if (modifiedField != null) {
                     DateTime modifiedInIndex =
                             DATE_TO_SECOND.parseDateTime(modifiedField.stringValue());
                     DateTime modifiedInDatabase =
                             new DateTime(ctx.dataCategory.getModified()).withMillisOfSecond(0);
                     if (indexDataCategories || ctx.handleDataItems || modifiedInDatabase.isAfter(modifiedInIndex)) {
                         log.debug("updateDataCategory() DataCategory has been modified or re-index requested, updating.");
                         handleDataCategory(ctx);
                     } else {
                         log.debug("updateDataCategory() DataCategory is up-to-date, skipping.");
                     }
                 } else {
                     log.warn("updateDataCategory() The modified field was missing, updating");
                     handleDataCategory(ctx);
                 }
             } else {
                 log.debug("updateDataCategory() DataCategory not in index, adding.");
                 handleDataCategory(ctx);
             }
         } else {
             log.debug("updateDataCategory() DataCategory needs to be removed.");
             searchQueryService.removeDataCategory(ctx.dataCategory);
             searchQueryService.removeDataItems(ctx.dataCategory);
         }
     }
 
     /**
      * Add a Document for the supplied DataCategory to the Lucene index.
      *
      * @param ctx
      */
     protected void handleDataCategory(DocumentContext ctx) {
         log.debug("handleDataCategory() " + ctx.dataCategory.toString());
         PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(environmentService.getEnvironmentByName("AMEE"));
         // Get Data Category Document.
         Document dataCategoryDoc = getDocumentForDataCategory(ctx.dataCategory, pathItemGroup.findByUId(ctx.dataCategory.getUid()));
         // Handle Data Items (Create, store & update documents).
         handleDataItems(ctx);
         // Store / update the Data Category Document.
         luceneService.updateDocument(
                 dataCategoryDoc,
                 new Term("entityType", ObjectType.DC.getName()),
                 new Term("entityUid", ctx.dataCategory.getUid()));
     }
 
     // Lucene Document creation.
 
     protected Document getDocumentForDataCategory(DataCategory dataCategory, PathItem pathItem) {
         Document doc = getDocumentForAMEEEntity(dataCategory);
         doc.add(new Field("name", dataCategory.getName().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("path", dataCategory.getPath().toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED));
         if (pathItem != null) {
             doc.add(new Field("fullPath", pathItem.getFullPath().toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED));
         }
         doc.add(new Field("wikiName", dataCategory.getWikiName().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("wikiDoc", dataCategory.getWikiDoc().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("provenance", dataCategory.getProvenance().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("authority", dataCategory.getAuthority().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         if (dataCategory.getDataCategory() != null) {
             doc.add(new Field("parentUid", dataCategory.getDataCategory().getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
             doc.add(new Field("parentWikiName", dataCategory.getDataCategory().getWikiName().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         }
         if (dataCategory.getItemDefinition() != null) {
             doc.add(new Field("itemDefinitionUid", dataCategory.getItemDefinition().getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
             doc.add(new Field("itemDefinitionName", dataCategory.getItemDefinition().getName().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         }
         doc.add(new Field("tags", tagService.getTagsCSV(dataCategory).toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         return doc;
     }
 
     protected Document getDocumentForDataItem(DataItem dataItem) {
         PathItemGroup pathItemGroup = pathItemService.getPathItemGroup(dataItem.getEnvironment());
         PathItem pathItem = pathItemGroup.findByUId(dataItem.getDataCategory().getUid());
         Document doc = getDocumentForAMEEEntity(dataItem);
         doc.add(new Field("name", dataItem.getName().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("path", dataItem.getPath().toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED));
         if (pathItem != null) {
             doc.add(new Field("fullPath", pathItem.getFullPath().toLowerCase() + "/" + dataItem.getDisplayPath().toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED));
         }
         doc.add(new Field("wikiDoc", dataItem.getWikiDoc().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("provenance", dataItem.getProvenance().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("categoryUid", dataItem.getDataCategory().getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("categoryWikiName", dataItem.getDataCategory().getWikiName().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         doc.add(new Field("itemDefinitionUid", dataItem.getItemDefinition().getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("itemDefinitionName", dataItem.getItemDefinition().getName().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         for (ItemValue itemValue : dataItem.getItemValues()) {
             if (itemValue.isUsableValue()) {
                 if (itemValue.getItemValueDefinition().isDrillDown()) {
                     doc.add(new Field(itemValue.getDisplayPath(), itemValue.getValue().toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED));
                 } else {
                     if (itemValue.isDouble()) {
                         try {
                             doc.add(new NumericField(itemValue.getDisplayPath()).setDoubleValue(new Amount(itemValue.getValue()).getValue()));
                         } catch (NumberFormatException e) {
                             log.warn("getDocumentForDataItem() Could not parse '" + itemValue.getDisplayPath() + "' value '" + itemValue.getValue() + "' for DataItem " + dataItem.toString() + ".");
                             doc.add(new Field(itemValue.getDisplayPath(), itemValue.getValue().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
                         }
                     } else {
                         doc.add(new Field(itemValue.getDisplayPath(), itemValue.getValue().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
                     }
                 }
             }
         }
         doc.add(new Field("label", dataItem.getLabel().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
         return doc;
     }
 
     protected Document getDocumentForAMEEEntity(AMEEEntity entity) {
         Document doc = new Document();
         doc.add(new Field("entityType", entity.getObjectType().getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("entityId", entity.getId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("entityUid", entity.getUid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("entityCreated",
                 new DateTime(entity.getCreated()).toString(DATE_TO_SECOND), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("entityModified",
                 new DateTime(entity.getModified()).toString(DATE_TO_SECOND), Field.Store.YES, Field.Index.NOT_ANALYZED));
         return doc;
     }
 
     protected void handleDataItemValues(DocumentContext ctx) {
         if (ctx.dataItemDoc != null) {
             for (ItemValue itemValue : ctx.dataItem.getItemValues()) {
                 if (itemValue.isUsableValue()) {
                     if (itemValue.getItemValueDefinition().isDrillDown()) {
                         ctx.dataItemDoc.add(new Field(itemValue.getDisplayPath(), itemValue.getValue().toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED));
                     } else {
                         if (itemValue.isDouble()) {
                             try {
                                 ctx.dataItemDoc.add(new NumericField(itemValue.getDisplayPath()).setDoubleValue(new Amount(itemValue.getValue()).getValue()));
                             } catch (NumberFormatException e) {
                                 log.warn("handleDataItemValues() Could not parse '" + itemValue.getDisplayPath() + "' value '" + itemValue.getValue() + "' for DataItem " + ctx.dataItem.toString() + ".");
                                 ctx.dataItemDoc.add(new Field(itemValue.getDisplayPath(), itemValue.getValue().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
                             }
                         } else {
                             ctx.dataItemDoc.add(new Field(itemValue.getDisplayPath(), itemValue.getValue().toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
                         }
                     }
                 }
             }
         }
     }
 
     // Entity search.
 
     public ResultsWrapper<AMEEEntity> getEntities(SearchFilter filter) {
         Long entityId;
         ObjectType entityType;
         // Do search and fetch Lucene documents.
         ResultsWrapper<Document> resultsWrapper = searchQueryService.doSearch(filter);
         // Collate entityIds against entityTypes.
         Map<ObjectType, Set<Long>> entityIds = new HashMap<ObjectType, Set<Long>>();
         for (Document document : resultsWrapper.getResults()) {
             entityId = new Long(document.getField("entityId").stringValue());
             entityType = ObjectType.valueOf(document.getField("entityType").stringValue());
             Set<Long> idSet = entityIds.get(entityType);
             if (idSet == null) {
                 idSet = new HashSet<Long>();
                 entityIds.put(entityType, idSet);
             }
             idSet.add(entityId);
         }
         // Collate AMEEEntities.
         Map<ObjectType, Map<Long, AMEEEntity>> entities = new HashMap<ObjectType, Map<Long, AMEEEntity>>();
         // Load DataCategories.
         if (entityIds.containsKey(ObjectType.DC)) {
             Map<Long, DataCategory> dataCategoriesMap = dataService.getDataCategoryMap(
                     environmentService.getEnvironmentByName("AMEE"),
                     entityIds.get(ObjectType.DC));
             addDataCategories(entities, dataCategoriesMap);
             // Pre-loading of EntityTags, LocaleNames & DataCategories.
             if (filter.isLoadEntityTags()) {
                 tagService.loadEntityTagsForDataCategories(dataCategoriesMap.values());
             }
             if (filter.isLoadMetadatas()) {
                 metadataService.loadMetadatasForDataCategories(dataCategoriesMap.values());
             }
             localeService.loadLocaleNamesForDataCategories(dataCategoriesMap.values());
         }
         // Load DataItems.
         if (entityIds.containsKey(ObjectType.DI)) {
             Map<Long, DataItem> dataItemsMap = dataService.getDataItemMap(
                     environmentService.getEnvironmentByName("AMEE"),
                     entityIds.get(ObjectType.DI),
                     filter.isLoadDataItemValues());
             addDataItems(entities, dataItemsMap);
             // Pre-loading of LocaleNames & DataCategories.
             if (filter.isLoadMetadatas()) {
                 metadataService.loadMetadatasForDataItems(dataItemsMap.values());
             }
             localeService.loadLocaleNamesForDataItems(dataItemsMap.values(), filter.isLoadDataItemValues());
         }
         // Create result list in relevance order.
         List<AMEEEntity> results = new ArrayList<AMEEEntity>();
         for (Document document : resultsWrapper.getResults()) {
             entityId = new Long(document.getField("entityId").stringValue());
             entityType = ObjectType.valueOf(document.getField("entityType").stringValue());
             AMEEEntity result = entities.get(entityType).get(entityId);
             if (result != null) {
                 if (!results.contains(result)) {
                     results.add(result);
                 }
             } else {
                 log.warn("getEntities() Entity was missing: " + entityType + " / " + entityId);
             }
         }
         return new ResultsWrapper<AMEEEntity>(results, resultsWrapper.isTruncated());
     }
 
     protected void addDataCategories(Map<ObjectType, Map<Long, AMEEEntity>> entities, Map<Long, DataCategory> dataCategoriesMap) {
         Map<Long, AMEEEntity> e = new HashMap<Long, AMEEEntity>();
         for (Long id : dataCategoriesMap.keySet()) {
             e.put(id, dataCategoriesMap.get(id));
         }
         entities.put(ObjectType.DC, e);
     }
 
     protected void addDataItems(Map<ObjectType, Map<Long, AMEEEntity>> entities, Map<Long, DataItem> dataItemsMap) {
         Map<Long, AMEEEntity> e = new HashMap<Long, AMEEEntity>();
         for (Long id : dataItemsMap.keySet()) {
             e.put(id, dataItemsMap.get(id));
         }
         entities.put(ObjectType.DI, e);
     }
 
     // DataCategory Search.
 
     public ResultsWrapper<DataCategory> getDataCategories(DataCategoryFilter filter) {
         ResultsWrapper<DataCategory> resultsWrapper;
         // Filter based on an allowed query parameter.
         if (!filter.getQueries().isEmpty()) {
             BooleanQuery query = new BooleanQuery();
             for (Query q : filter.getQueries().values()) {
                 query.add(q, BooleanClause.Occur.MUST);
             }
             resultsWrapper = getDataCategories(
                     query,
                     filter.getResultStart(),
                     filter.getResultLimit());
         } else {
             // Just get a simple list of Data Categories.
             resultsWrapper = dataService.getDataCategories(
                     environmentService.getEnvironmentByName("AMEE"),
                     filter.getResultStart(),
                     filter.getResultLimit());
         }
         // Pre-loading of EntityTags, LocaleNames & DataCategories.
         if (filter.isLoadEntityTags()) {
             tagService.loadEntityTagsForDataCategories(resultsWrapper.getResults());
         }
         if (filter.isLoadMetadatas()) {
             metadataService.loadMetadatasForDataCategories(resultsWrapper.getResults());
         }
         localeService.loadLocaleNamesForDataCategories(resultsWrapper.getResults());
         return resultsWrapper;
     }
 
     public ResultsWrapper<DataCategory> getDataCategories(Query query, int resultStart, int resultLimit) {
         BooleanQuery q = new BooleanQuery();
         q.add(new TermQuery(new Term("entityType", ObjectType.DC.getName())), BooleanClause.Occur.MUST);
         if (query != null) {
             q.add(query, BooleanClause.Occur.MUST);
         }
         ResultsWrapper<Document> resultsWrapper =
                 luceneService.doSearch(q, resultStart, resultLimit);
         Set<Long> dataCategoryIds = new HashSet<Long>();
         for (Document document : resultsWrapper.getResults()) {
             dataCategoryIds.add(new Long(document.getField("entityId").stringValue()));
         }
         return new ResultsWrapper<DataCategory>(
                 dataService.getDataCategories(environmentService.getEnvironmentByName("AMEE"), dataCategoryIds),
                 resultsWrapper.isTruncated());
     }
 
     // DataItem search.
 
     public ResultsWrapper<DataItem> getDataItems(DataCategory dataCategory, DataItemFilter filter) {
         BooleanQuery query = null;
         if (!filter.getQueries().isEmpty()) {
             query = new BooleanQuery();
             for (Query q : filter.getQueries().values()) {
                 query.add(q, BooleanClause.Occur.MUST);
             }
         }
         // Get the DataItems.
         ResultsWrapper<DataItem> resultsWrapper = getDataItems(dataCategory, query, filter.getResultStart(), filter.getResultLimit());
         // Pre-loading of Metadatas, LocaleNames and ItemValues.
         if (filter.isLoadMetadatas()) {
             metadataService.loadMetadatasForDataItems(resultsWrapper.getResults());
         }
         localeService.loadLocaleNamesForDataItems(resultsWrapper.getResults(), filter.isLoadDataItemValues());
         return resultsWrapper;
     }
 
     public ResultsWrapper<DataItem> getDataItems(DataCategory dataCategory, Query query, int resultStart, int resultLimit) {
         BooleanQuery q = new BooleanQuery();
         q.add(new TermQuery(new Term("entityType", ObjectType.DI.getName())), BooleanClause.Occur.MUST);
         q.add(new TermQuery(new Term("categoryUid", dataCategory.getUid())), BooleanClause.Occur.MUST);
         if (query != null) {
             q.add(query, BooleanClause.Occur.MUST);
         }
         ResultsWrapper<Document> resultsWrapper =
                 luceneService.doSearch(q, resultStart, resultLimit);
         Set<Long> dataCategoryIds = new HashSet<Long>();
         for (Document document : resultsWrapper.getResults()) {
             dataCategoryIds.add(new Long(document.getField("entityId").stringValue()));
         }
         return new ResultsWrapper<DataItem>(
                 dataService.getDataItems(environmentService.getEnvironmentByName("AMEE"), dataCategoryIds),
                 resultsWrapper.isTruncated());
     }
 
     @Value("#{ systemProperties['amee.clearIndex'] }")
     public void setClearIndex(Boolean clearIndex) {
         this.clearIndex = clearIndex;
     }
 
     @Value("#{ systemProperties['amee.indexDataCategories'] }")
     public void setIndexDataCategories(Boolean indexDataCategories) {
         this.indexDataCategories = indexDataCategories;
     }
 
     @Value("#{ systemProperties['amee.indexDataItems'] }")
     public void setIndexDataItems(Boolean indexDataItems) {
         this.indexDataItems = indexDataItems;
     }
 }
