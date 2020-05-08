 package com.amee.platform.search;
 
 import com.amee.base.domain.ResultsWrapper;
 import com.amee.domain.IDataCategoryReference;
 import com.amee.domain.ObjectType;
 import com.amee.domain.sheet.Choice;
 import com.amee.service.data.AbstractDrillDownService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.TermQuery;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * A search index backed sub-class of DrillDownService which overrides the getDataItemChoices method with an
  * implementation that uses the Lucene index instead of the original SQL based implementation.
  */
 public class LuceneDrillDownService extends AbstractDrillDownService {
 
     private final Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private LuceneService luceneService;
 
     @Override
     protected List<Choice> getDataItemChoices(
         IDataCategoryReference dataCategory, List<Choice> selections, List<Choice> drillDownChoices) {
 
         // Create Query for Data Items within the given DataCategory matching the supplied selections and drillDownChoices.
         BooleanQuery typesQuery = new BooleanQuery();
         typesQuery.add(new TermQuery(new Term("entityType", ObjectType.DI.getName())), BooleanClause.Occur.SHOULD);
         BooleanQuery query = new BooleanQuery();
         query.add(typesQuery, BooleanClause.Occur.MUST);
         query.add(new TermQuery(new Term("categoryUid", dataCategory.getEntityUid())), BooleanClause.Occur.MUST);
         for (Choice choice : selections) {
            if (!choice.getValue().isEmpty()) {
                query.add(new TermQuery(new Term(choice.getName(), choice.getValue().toLowerCase())), BooleanClause.Occur.MUST);
            }
         }
         ResultsWrapper<Document> results = luceneService.doSearch(query);
 
         // Create choices array.
         List<Choice> choices = new ArrayList<Choice>();
 
         // What kind of choices?
         if (drillDownChoices.size() > 0) {
 
             // Value choices.
             String path = drillDownChoices.get(0).getName();
             Set<String> values = new HashSet<String>();
             for (Document doc : results.getResults()) {
                 Field field = doc.getField(path + "_drill");
                 if (field != null) {
                     String value = doc.getField(path + "_drill").stringValue();
                     if (!values.contains(value)) {
                         choices.add(new Choice(path, value));
                         values.add(value);
                     }
                 } else {
                     log.warn("getDataItemChoices() Missing index document field '" + path +
                             "' for DataItem with UID: " + doc.getField("entityUid").stringValue());
                 }
             }
         } else {
 
             // UID choices.
             for (Document doc : results.getResults()) {
                 choices.add(new Choice(doc.getField("entityUid").stringValue()));
             }
         }
 
         // Remove duplicates.
         return choices;
     }
 }
