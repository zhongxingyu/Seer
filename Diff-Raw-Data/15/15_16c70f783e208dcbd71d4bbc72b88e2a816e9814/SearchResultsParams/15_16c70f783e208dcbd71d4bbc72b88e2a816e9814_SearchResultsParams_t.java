 /*
  * Created on 07-May-2006
  */
 package uk.org.ponder.darwin.rsf.params;
 
 import uk.org.ponder.darwin.rsf.producers.SearchResultsProducer;
 import uk.org.ponder.darwin.search.SearchParams;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 
 public class SearchResultsParams extends SimpleViewParameters {
   public int pageno;
  public int pagesize = 20;
   public static final String[] pagesizes = new String[] {"10", "20", "50", "100"};
   
   public SearchResultsParams() {
     super(SearchResultsProducer.VIEWID);
   }
   
   public SearchParams params = new SearchParams();
   
   public String getParseSpec() {
     return super.getParseSpec() + ", pageno, pagesize, params.*:";
   }
 }
