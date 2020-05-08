 package org.linkedgov.questions.components;
 
 import org.apache.tapestry5.Block;
 import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.linkedgov.questions.model.Query;
 import org.linkedgov.questions.model.QuestionType;
 import org.linkedgov.questions.model.SelectResultDataSource;
 import org.linkedgov.questions.model.Triple;
 import org.linkedgov.questions.services.QueryDataService;
 
 /**
  * 
  * @author Luke Wilson-Mawer <a href="http://viscri.co.uk/">Viscri</a> and 
  * @author <a href="http://mmt.me.uk/foaf.rdf#mischa">Mischa Tuffield</a> for LinkedGov
  */
 public class Results {
     
     /**
      * The value of the query.
      */
     @Parameter
     private Query query;
     
     //@Property
     //private String gridColumns = "resultSubject,resultPredicate,resultObject";
    
     /**
      * A row in the list of triples that come back as results.
      */
     @SuppressWarnings("unused")
     @Property
     private Triple triple;
     
     /**
      * QueryDataService service, to actually do the query with.
      */
     @Inject
     private QueryDataService queryDataService;
     
     /**
      * Block to show results table in.
      */
     @Inject
     private Block resultsTableBlock;
     
     /**
      * Block to show empty results in.
      */
     @Inject
     private Block emptyResultsBlock;
     
     /**
      * Block with no results yet.
      */
     @Inject
     private Block noResultsYetBlock;
     
     /**
      * Datasource to back the result table.
      */
     @Property
    @Persist
     private SelectResultDataSource dataSource;
 
     /**
      * Select Result Data Source
      */
     @SetupRender
     public void setupDataSource(){
         dataSource = new SelectResultDataSource(query, queryDataService);
     }
     
     /**
      * @return - a block which contains the results or an error message if there aren't any.
      */
     public Block getResultsBlock(){
         if(query.getSubject() == null){
             return noResultsYetBlock;
         } else {
             return dataSource.getAvailableRows() > 0 ? resultsTableBlock : emptyResultsBlock;
         }
     }
     
     public String getGridColumns() {
         if (query.getQuestionType().equals(QuestionType.SELECT)) {
             return "resultSubject,resultPredicate,resultObject";
         } else {
             return "resultCount";
         }
     }
 }
