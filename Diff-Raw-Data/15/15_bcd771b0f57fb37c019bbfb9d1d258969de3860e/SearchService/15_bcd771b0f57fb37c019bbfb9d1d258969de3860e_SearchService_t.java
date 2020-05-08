 package com.amee.platform.search;
 
 import com.amee.base.domain.ResultsWrapper;
 import com.amee.domain.IAMEEEntity;
 import com.amee.domain.ObjectType;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.DataItem;
 import com.amee.service.data.DataService;
 import com.amee.service.locale.LocaleService;
 import com.amee.service.metadata.MetadataService;
 import com.amee.service.tag.TagService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.*;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.util.Version;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.io.Reader;
 import java.util.*;
 
 public class SearchService {
 
     private final Log log = LogFactory.getLog(getClass());
 
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
     private DataService dataService;
 
     @Autowired
     private MetadataService metadataService;
 
     @Autowired
     private LocaleService localeService;
 
     @Autowired
     private TagService tagService;
 
     @Autowired
     private SearchQueryService searchQueryService;
 
     @Autowired
     private LuceneService luceneService;
 
     // Entity search.
 
     public ResultsWrapper<IAMEEEntity> getEntities(SearchFilter filter) {
         Long entityId;
         ObjectType entityType;
         // Ensure filter.types contains NDI if DI is present.
         if (filter.getTypes().contains(ObjectType.DI)) {
             filter.getTypes().add(ObjectType.NDI);
         }
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
         Map<ObjectType, Map<Long, IAMEEEntity>> entities = new HashMap<ObjectType, Map<Long, IAMEEEntity>>();
         // Load DataCategories.
         if (entityIds.containsKey(ObjectType.DC)) {
             Map<Long, DataCategory> dataCategoriesMap = dataService.getDataCategoryMap(entityIds.get(ObjectType.DC));
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
         // Load DataItems (LegacyDataItem & NuDataItem).
         if (entityIds.containsKey(ObjectType.DI) || entityIds.containsKey(ObjectType.NDI)) {
             // Collate LegacyDataItem & NuDataItem IDs.
             Set<Long> dataItemIds = new HashSet<Long>();
             if (entityIds.containsKey(ObjectType.DI)) {
                 dataItemIds.addAll(entityIds.get(ObjectType.DI));
             }
             if (entityIds.containsKey(ObjectType.NDI)) {
                 dataItemIds.addAll(entityIds.get(ObjectType.NDI));
             }
             // Load the items.
             Map<Long, DataItem> dataItemsMap =
                     dataService.getDataItemMap(dataItemIds, filter.isLoadDataItemValues());
             addDataItems(entities, dataItemsMap);
             // Pre-loading of LocaleNames & Metadatas.
             if (filter.isLoadMetadatas()) {
                 metadataService.loadMetadatasForDataItems(dataItemsMap.values());
             }
             localeService.loadLocaleNamesForDataItems(dataItemsMap.values(), filter.isLoadDataItemValues());
         }
         // Create result list in relevance order.
         List<IAMEEEntity> results = new ArrayList<IAMEEEntity>();
         for (Document document : resultsWrapper.getResults()) {
             entityId = new Long(document.getField("entityId").stringValue());
             entityType = ObjectType.valueOf(document.getField("entityType").stringValue());
             IAMEEEntity result = entities.get(entityType).get(entityId);
             if (result != null) {
                 if (!results.contains(result)) {
                     results.add(result);
                 }
             } else {
                 log.warn("getEntities() Entity was missing: " + entityType + " / " + entityId);
             }
         }
         return new ResultsWrapper<IAMEEEntity>(results, resultsWrapper.isTruncated());
     }
 
     protected void addDataCategories(Map<ObjectType, Map<Long, IAMEEEntity>> entities, Map<Long, DataCategory> dataCategoriesMap) {
         Map<Long, IAMEEEntity> e = new HashMap<Long, IAMEEEntity>();
         for (Long id : dataCategoriesMap.keySet()) {
             e.put(id, dataCategoriesMap.get(id));
         }
         entities.put(ObjectType.DC, e);
     }
 
     protected void addDataItems(Map<ObjectType, Map<Long, IAMEEEntity>> entities, Map<Long, DataItem> dataItemsMap) {
         Map<Long, IAMEEEntity> legacyDataItems = new HashMap<Long, IAMEEEntity>();
         Map<Long, IAMEEEntity> nuDataItems = new HashMap<Long, IAMEEEntity>();
         for (Long id : dataItemsMap.keySet()) {
             IAMEEEntity entity = dataItemsMap.get(id);
             if (entity.getObjectType().equals(ObjectType.DI)) {
                 legacyDataItems.put(id, dataItemsMap.get(id));
             } else if (entity.getObjectType().equals(ObjectType.NDI)) {
                 nuDataItems.put(id, dataItemsMap.get(id));
             }
         }
         entities.put(ObjectType.DI, legacyDataItems);
         entities.put(ObjectType.NDI, nuDataItems);
     }
 
     // DataCategory Search.
 
     public ResultsWrapper<DataCategory> getDataCategories(DataCategoriesFilter filter) {
         ResultsWrapper<DataCategory> resultsWrapper;
         // Filter based on an allowed query parameter.
         if (!filter.getQueries().isEmpty()) {
             BooleanQuery query = new BooleanQuery();
 
             Query excTags = filter.removeExcTags();
             if (excTags != null){
                 query.add(excTags, BooleanClause.Occur.MUST_NOT);    
             }
 
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
 
    private ResultsWrapper<DataCategory> getDataCategories(BooleanQuery query, int resultStart, int resultLimit) {
        if (query == null) {
            query = new BooleanQuery();
         }
        Query entityQuery = new TermQuery(new Term("entityType", ObjectType.DC.getName()));
        query.add(entityQuery, BooleanClause.Occur.MUST);
        
         ResultsWrapper<Document> resultsWrapper =
                luceneService.doSearch(query, resultStart, resultLimit);
         Set<Long> dataCategoryIds = new HashSet<Long>();
         for (Document document : resultsWrapper.getResults()) {
             dataCategoryIds.add(new Long(document.getField("entityId").stringValue()));
         }
         return new ResultsWrapper<DataCategory>(
                 dataService.getDataCategories(dataCategoryIds),
                 resultsWrapper.isTruncated());
     }
 
     // DataItem search.
 
     public ResultsWrapper<DataItem> getDataItems(DataCategory dataCategory, DataItemsFilter filter) {
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
         BooleanQuery typesQuery = new BooleanQuery();
         typesQuery.add(new TermQuery(new Term("entityType", ObjectType.DI.getName())), BooleanClause.Occur.SHOULD);
         typesQuery.add(new TermQuery(new Term("entityType", ObjectType.NDI.getName())), BooleanClause.Occur.SHOULD);
         q.add(typesQuery, BooleanClause.Occur.MUST);
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
                 dataService.getDataItems(dataCategoryIds),
                 resultsWrapper.isTruncated());
     }
 }
