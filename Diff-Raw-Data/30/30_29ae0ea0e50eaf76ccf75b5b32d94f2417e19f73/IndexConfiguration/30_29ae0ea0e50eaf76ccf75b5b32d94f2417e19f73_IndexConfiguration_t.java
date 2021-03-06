 package fr.sciencespo.medialab.hci.memorystructure.index;
 
 import fr.sciencespo.medialab.hci.memorystructure.thrift.PageItem;
 import fr.sciencespo.medialab.hci.memorystructure.thrift.WebEntity;
 import fr.sciencespo.medialab.hci.memorystructure.thrift.WebEntityCreationRule;
 import org.apache.commons.lang.StringUtils;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Fieldable;
 import org.apache.lucene.index.FieldInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 /**
  * @author heikki doeleman
  */
 public class IndexConfiguration {
 
     private static Logger logger = LoggerFactory.getLogger(IndexConfiguration.class);
 
     /**
      * Names of fields in the index. Not every doc needs to have all of these fields.
      */
     enum FieldName {
         ID,
         TYPE,
         LRU,
         CRAWLERTIMESTAMP,
         DEPTH,
         ERRORCODE,
         HTTPSTATUSCODE,
        REGEXP,
        NAME
     }
 
     /**
      * Types of objects in the index. These values are stored in the docs in field TYPE.
      */
     enum DocType {
         PAGE_ITEM,
         PRECISION_EXCEPTION,
         WEBENTITY,
         WEBENTITY_CREATION_RULE
     }
 
     public static final String DEFAULT_WEBENTITY_CREATION_RULE = "DEFAULT_WEBENTITY_CREATION_RULE";
 
     /**
      * Converts a PageItem into a Lucene document.
      *
      * @param pageItem
      * @return
      */
     protected static Document PageItemDocument(PageItem pageItem) {
         Document document = new Document();
         //
         // id: generate random UUID
         //
         Field idField = new Field(FieldName.ID.name(), UUID.randomUUID().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         idField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(idField);
 
         Field typeField = new Field(FieldName.TYPE.name(), DocType.PAGE_ITEM.name(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         typeField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(typeField);
 
         //
         // if the PageItem has no LRU, don't create a Lucene document for it
         //
         if(StringUtils.isNotEmpty(pageItem.getLru())) {
             Field lruField = new Field(FieldName.LRU.name(), pageItem.getLru(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
             lruField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
             document.add(lruField);
         }
         else {
             logger.warn("attempt to create Lucene document for PageItem without LRU");
             return null;
         }
 
         if(StringUtils.isNotEmpty(pageItem.getCrawlerTimestamp())) {
             Field crawlerTimestampField = new Field(FieldName.CRAWLERTIMESTAMP.name(), pageItem.getCrawlerTimestamp(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
             crawlerTimestampField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
             document.add(crawlerTimestampField);
         }
 
         if(StringUtils.isNotEmpty(Integer.toString(pageItem.getDepth()))) {
             Field depthField = new Field(FieldName.DEPTH.name(), Integer.toString(pageItem.getDepth()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
             depthField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
             document.add(depthField);
         }
 
         if(StringUtils.isNotEmpty(pageItem.getErrorCode())) {
             Field errorCodeField = new Field(FieldName.ERRORCODE.name(), pageItem.getErrorCode(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
             errorCodeField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
             document.add(errorCodeField);
         }
 
         if(StringUtils.isNotEmpty(Integer.toString(pageItem.getHttpStatusCode()))) {
             Field httpStatusCodeField = new Field(FieldName.HTTPSTATUSCODE.name(), Integer.toString(pageItem.getHttpStatusCode()), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
             httpStatusCodeField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
             document.add(httpStatusCodeField);
         }
 
         return document;
     }
 
 
     /**
      * Converts a WebEntityCreationRule into a Lucene document.
      *
      * @param webEntityCreationRule
      * @return
      */
     protected static Document WebEntityCreationRuleDocument(WebEntityCreationRule webEntityCreationRule) throws IndexException {
 
         if(webEntityCreationRule == null) {
             throw new IndexException("WebEntityCreationRule is null");
         }
         if(webEntityCreationRule.getLRU() == null && webEntityCreationRule.getRegExp() == null) {
             throw new IndexException("WebEntityCreationRule has null properties");
         }
 
         Document document = new Document();
 
         Field idField = new Field(FieldName.ID.name(), UUID.randomUUID().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         idField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(idField);
 
         Field typeField = new Field(FieldName.TYPE.name(), DocType.WEBENTITY_CREATION_RULE.name(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         typeField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(typeField);
 
         String lru = DEFAULT_WEBENTITY_CREATION_RULE;
         if(StringUtils.isNotEmpty(webEntityCreationRule.getLRU())) {
             lru = webEntityCreationRule.getLRU();
         }
         Field lruField = new Field(FieldName.LRU.name(), lru, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         lruField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(lruField);
 
         Field regExpField = new Field(FieldName.REGEXP.name(), webEntityCreationRule.getRegExp(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         regExpField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(regExpField);
 
         return document;
     }
 
     /**
      * Converts a WebEntity into a Lucene document. If the webEntity has no ID, one is created (in case of new
      * WebEntities that weren't stored before).
      *
      * @param webEntity
      * @return
      */
     protected static Document WebEntityDocument(WebEntity webEntity) {
         Document document = new Document();
 
         String id = webEntity.getId();
         if(StringUtils.isEmpty(webEntity.getId())) {
             id = UUID.randomUUID().toString();
         }
         logger.debug("lucene document for webentity with id " + id);
         Field idField = new Field(FieldName.ID.name(), id, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         idField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(idField);
 
        Field nameField = new Field(FieldName.NAME.name(), webEntity.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        nameField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
        document.add(nameField);

         Field typeField = new Field(FieldName.TYPE.name(), DocType.WEBENTITY.name(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
         typeField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
         document.add(typeField);
 
         logger.debug("lucene document adding # " + webEntity.getLRUSet().size() + " lrus");
         for(String lru : webEntity.getLRUSet()) {
             Field lruField = new Field(FieldName.LRU.name(), lru, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
             lruField.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
             document.add(lruField);
         }
         logger.debug("lucene document has # " + document.getFieldables(FieldName.LRU.name()).length + " lrufields in webentity " + id);
         return document;
     }
 
     /**
      * Returns a WebEntity object from a WebEntity Lucene document.
      *
      * @param document
      * @return
      */
     protected static WebEntity convertLuceneDocument2WebEntity(Document document) {
         WebEntity webEntity = new WebEntity();

         String id = document.get(FieldName.ID.name());
         webEntity.setId(id);

        String name = document.get(FieldName.NAME.name());
        webEntity.setName(name);

         Fieldable[] lruFields = document.getFieldables(FieldName.LRU.name());
         logger.debug("lucene doc for webentity has # " + lruFields.length + " lru fields");
         Set<String> lruList = new HashSet<String>();
         for(Fieldable lruField : lruFields) {
             lruList.add(lruField.stringValue());
         }
         webEntity.setLRUSet(lruList);
         logger.debug("convertLuceneDocument2WebEntity returns webentity with id: " + id);
         return webEntity;
     }
 
     /**
      * Returns a WebEntityCreationRule object from a WebEntityCreationRule Lucene document.
      *
      * @param document
      * @return
      */
     protected static WebEntityCreationRule convertLuceneDocument2WebEntityCreationRule(Document document) {
         WebEntityCreationRule webEntityCreationRule = new WebEntityCreationRule();
         String lru = document.get(FieldName.LRU.name());
         String regexp = document.get(FieldName.REGEXP.name());
 
         webEntityCreationRule.setLRU(lru);
         webEntityCreationRule.setRegExp(regexp);
 
         logger.debug("convertLuceneDocument2WebEntity returns webEntityCreationRule with lru: " + lru + " and regexp " + regexp);
         return webEntityCreationRule;
     }
 
 }
