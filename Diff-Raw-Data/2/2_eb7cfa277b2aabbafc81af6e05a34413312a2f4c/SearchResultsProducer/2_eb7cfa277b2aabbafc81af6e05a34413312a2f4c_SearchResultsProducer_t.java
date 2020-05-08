 /*
  * Created on 07-May-2006
  */
 package uk.org.ponder.darwin.rsf.producers;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Sort;
 import org.springframework.util.StringUtils;
 
 import uk.org.ponder.darwin.item.ItemCollection;
 import uk.org.ponder.darwin.lucene.DarwinHighlighter;
 import uk.org.ponder.darwin.lucene.IndexItemSearcher;
 import uk.org.ponder.darwin.lucene.QueryBuilder;
 import uk.org.ponder.darwin.rsf.params.AdvancedSearchParams;
 import uk.org.ponder.darwin.rsf.params.NavParams;
 import uk.org.ponder.darwin.rsf.params.RecordParams;
 import uk.org.ponder.darwin.rsf.params.SearchResultsParams;
 import uk.org.ponder.darwin.rsf.util.DarwinUtil;
 import uk.org.ponder.darwin.search.DocFields;
 import uk.org.ponder.darwin.search.DocTypeInterpreter;
 import uk.org.ponder.darwin.search.SearchParams;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.components.UIVerbatim;
 import uk.org.ponder.rsf.request.EarlyRequestParser;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 import uk.org.ponder.stringutil.CharWrap;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 public class SearchResultsProducer implements ViewComponentProducer,
     ViewParamsReporter {
   public static final String VIEWID = "search-results";
   private IndexItemSearcher itemsearcher;
   private QueryBuilder querybuilder;
   private ItemCollection itemcollection;
   private DocTypeInterpreter doctypeinterpreter;
 
   public String getViewID() {
     return VIEWID;
   }
 
   public ViewParameters getViewParameters() {
     return new SearchResultsParams();
   }
 
   public void setIndexItemSearcher(IndexItemSearcher itemsearcher) {
     this.itemsearcher = itemsearcher;
   }
 
   public void setQueryBuilder(QueryBuilder querybuilder) {
     this.querybuilder = querybuilder;
   }
 
   public void setItemCollection(ItemCollection itemcollection) {
     this.itemcollection = itemcollection;
   }
 
   public void setDocTypeInterpreter(DocTypeInterpreter doctypeinterpreter) {
     this.doctypeinterpreter = doctypeinterpreter;
   }
 
 
   private String rewriteQuery(Query unrwquery) {
     try {
       CharWrap togo = new CharWrap();
       Query query = unrwquery.rewrite(itemsearcher.getIndexSearcher().getIndexReader());
       Set terms = new HashSet();
       query.extractTerms(terms);
       
       boolean first = true;
       for (Iterator it = terms.iterator(); it.hasNext();) {
         Term term = (Term) it.next();
         if (!term.field().equals("text")) continue;
         String text = term.text();
         if (!first) {
           togo.append(" ");
         }
         first = false;
         togo.append(text);
       }
       return togo.toString();
     }
     catch (Exception e) {
       throw UniversalRuntimeException.accumulate(e, "Error rewriting query " + unrwquery);
     }
   }
   
   public void fillComponents(UIContainer tofill, ViewParameters viewparamso,
       ComponentChecker checker) {
     SearchResultsParams viewparams = (SearchResultsParams) viewparamso;
 
     if (viewparams.pageno == 0) {
       viewparams.pageno = 1;
     }
 
     SearchParams params = viewparams.params;
 
     UIForm repage = UIForm.make(tofill, "repage-form",
         EarlyRequestParser.RENDER_REQUEST);
     UISelect.make(repage, "pagesize", SearchResultsParams.pagesizes,
         SearchResultsParams.pagesizes, Integer.toString(viewparams.pagesize));
 
     UIForm resort = UIForm.make(tofill, "resort-form",
         EarlyRequestParser.RENDER_REQUEST);
     UISelect.make(resort, "sort", SearchParams.SORT_OPTIONS,
         SearchParams.SORT_OPTIONS_NAMES, params.sort);
 
     UIForm research = UIForm.make(tofill, "research-form",
         new SearchResultsParams());
     UIInput.make(research, "freetext", null, "");
 
     UIInternalLink.make(tofill, "search-again", new AdvancedSearchParams());
 
     boolean issingleitemquery = StringUtils.hasText(params.identifier);
     boolean isfreetext = StringUtils.hasText(params.freetext);
 
     int pagesize = viewparams.pagesize;
     int thispage = viewparams.pageno;
     if (thispage < 1)
       thispage = 1;
     if (pagesize < 5)
       pagesize = 5;
 
     int start = (thispage - 1) * pagesize;
     int limit = start + pagesize;
 
     if (thispage != 1) {
       SearchResultsParams prevparams = (SearchResultsParams) viewparams
           .copyBase();
       prevparams.pageno = thispage - 1;
       UIInternalLink.make(tofill, "previous-page-text", prevparams);
     }
     else {
       UIOutput.make(tofill, "no-previous-text");
     }
     UIForm gotoform = UIForm.make(tofill, "goto-form",
         EarlyRequestParser.RENDER_REQUEST);
     UIInput.make(gotoform, "pageno", null, "");
 
     try {
       Query query = querybuilder.convertQuery(params);
 
       UIOutput.make(tofill, "search-string", query.toString());
 
       Sort sort = QueryBuilder.convertSort(params);
       IndexSearcher searcher = itemsearcher.getIndexSearcher();
 
       Hits hits = null;
       try {
         hits = searcher.search(query, sort);
       }
       catch (Exception e) {
         Logger.log.warn("Error performing search", e);
       }
 
       if (hits == null || hits.length() == 0) {
         UIOutput.make(tofill, "no-results");
       }
       else {
         if (limit > hits.length())
           limit = hits.length();
         if (start >= limit)
           start = limit - pagesize;
         if (start < 0)
           start = 0;
         // 101 / 10 = 11, 100/10 = 10, 99/10 -> 10
         int maxpage = (hits.length() + (pagesize - 1)) / pagesize;
 
         UIOutput.make(tofill, "this-page", Integer.toString(thispage));
         UIOutput.make(tofill, "page-max", Integer.toString(maxpage));
         if (thispage < maxpage) {
           SearchResultsParams nextparams = (SearchResultsParams) viewparams
               .copyBase();
           nextparams.pageno = thispage + 1;
           UIInternalLink.make(tofill, "next-page-text", nextparams);
         }
         else {
           UIOutput.make(tofill, "no-next-text");
         }
 
         UIOutput.make(tofill, "this-page-hits", (start + 1) + "-" + limit);
         UIOutput.make(tofill, "totalhits", Integer.toString(hits.length()));
 
         for (int i = start; i < limit; ++i) {
           Document hit = hits.doc(i);
           
           String keywords = rewriteQuery(query);
           ViewParameters navparams = findLink(hit, keywords);
 
           UIBranchContainer hitrow = UIBranchContainer.make(tofill,
               "hit-item:", Integer.toString(i));
           String doctype = hit.get("documenttype");
       
           String name = hit.get("name");
           UIOutput.make(hitrow, "document-type", doctype);
 
           UIOutput.make(hitrow, "identifier", hit.get("identifier"));
 
           if (doctypeinterpreter.isConciseType(doctype)) {
             String concise = hit.get("reference");
             if (navparams != null) {
               UIInternalLink.make(hitrow, "concise-link", concise, navparams);
             }
             else {
               UIOutput.make(hitrow, "concise", concise);
             }
           }
           else {
             String datestring = hit.get("displaydate");
             UIOutput.make(hitrow, "date", datestring);
             UIOutput.make(hitrow, "name", name);
             String description =  hit.get("description");
             if (description != null) {
              UIOutput.make(hitrow, "description", description);
             }
         
             if (!doctypeinterpreter.isType(doctype,
                 DocTypeInterpreter.CORRESPONDENCE)) {
 
               String attribtitle = hit.get("attributedtitle");
               if (navparams != null) {
                 UIInternalLink.make(hitrow, "attrib-title-link", attribtitle,
                     navparams);
               }
               else {
                 UIOutput.make(hitrow, "attrib-title", attribtitle);
               }
             }
             else {
               String place = hit.get("place");
               if (place != null) {
                 UIOutput.make(hitrow, "place-created", place);
               }
               if (navparams != null) {
                 UIInternalLink.make(hitrow, "name-link", name, navparams);
               }
               else {
                 UIOutput.make(hitrow, "name", name);
               }
             }
           }
 
           if (isfreetext) {
             float score = hits.score(i);
             String relperc = (int) (score * 100) + "%";
             UIOutput.make(hitrow, "hit-weight", relperc);
             String pagetext = hit.get(DocFields.FLAT_TEXT);
             String rendered = DarwinHighlighter.getHighlightedHit(query,
                 pagetext, searcher.getIndexReader());
             UIVerbatim.make(hitrow, "rendered-hit", rendered);
           }
           else {
             UIOutput.make(hitrow, "hit-weight", (1 + i) + ". ");
           }
         }
       }
 
     }
     catch (Exception e) {
       Logger.log.warn("Error performing query: ", e);
     }
   }
 
 
   private ViewParameters findLink(Document hit, String freetext) {
     // We could presumably use DocFields.TYPE_ITEM here
     String pageno = hit.get(DocFields.PAGESEQ_START);
     if (pageno != null) {
       NavParams togo = new NavParams();
       togo.itemID = hit.get("identifier");
       if (togo.itemID == null) {
         Logger.log.warn("Warning: identifier not found for " + hit);
         togo.itemID = hit.get("itemID");
       }
       togo.pageseq = Integer.parseInt(pageno);
       DarwinUtil.chooseBestView(togo, itemcollection);
       togo.viewID = FramesetProducer.VIEWID;
       
       togo.keywords = freetext;
       return togo;
     }
     else {
       RecordParams togo = new RecordParams(hit.get("identifier"));
       return togo;
     }
   }
 
 }
