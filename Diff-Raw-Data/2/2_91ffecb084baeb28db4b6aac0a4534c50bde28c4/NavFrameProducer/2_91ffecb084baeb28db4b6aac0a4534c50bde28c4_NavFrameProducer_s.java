 /*
  * Created on 14-Dec-2005
  */
 package uk.org.ponder.darwin.rsf.producers;
 
 import uk.org.ponder.darwin.item.ItemCollection;
 import uk.org.ponder.darwin.item.ItemDetails;
 import uk.org.ponder.darwin.item.PageInfo;
 import uk.org.ponder.darwin.parse.URLMapper;
 import uk.org.ponder.darwin.rsf.ViewParamGetter;
 import uk.org.ponder.darwin.rsf.params.AdvancedSearchParams;
 import uk.org.ponder.darwin.rsf.params.NavParams;
 import uk.org.ponder.darwin.rsf.params.SearchResultsParams;
 import uk.org.ponder.darwin.rsf.params.TextBlockRenderParams;
 import uk.org.ponder.rsac.RSACBeanLocator;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 import uk.org.ponder.rsf.viewstate.ViewStateHandler;
 
 /**
  * @author Antranig Basman (amb26@ponder.org.uk)
  * 
  */
 public class NavFrameProducer implements ViewComponentProducer, ViewParamsReporter {
   public static final String VIEWID = "nav-frame";
   private ItemCollection collection;
   private URLMapper urlmapper;
   private ViewStateHandler viewStateHandler;
 
   public String getViewID() {
     return VIEWID;
   }
 
   public void setURLMapper(URLMapper urlmapper) {
     this.urlmapper = urlmapper;
   }
 
   public void setItemCollection(ItemCollection collection) {
     this.collection = collection;
   }
   
   public void setViewStateHandler(ViewStateHandler viewStateHandler) {
     this.viewStateHandler = viewStateHandler;
   }
 
   public void fillComponents(UIContainer tofill, ViewParameters origviewparams,
       ComponentChecker checker) {
 
     NavParams navparams = (NavParams) origviewparams;
 
     ItemDetails item = collection.getItem(navparams.itemID);
     
     AdvancedSearchParams bibparams = new AdvancedSearchParams();
     bibparams.published = true;
     UIInternalLink.make(tofill, "search-bibliography", bibparams);
     AdvancedSearchParams manparams = new AdvancedSearchParams();
    bibparams.manuscript = true;
     UIInternalLink.make(tofill, "search-manuscripts", manparams);
 
     if (navparams.viewtype.equals(NavParams.TEXT_VIEW)
         || navparams.viewtype.equals(NavParams.SIDE_VIEW)) {
       TextBlockRenderParams contentparams = new TextBlockRenderParams();
       contentparams.itemID = navparams.itemID;
       for (int i = 1; i < item.pages.size(); ++i) {
         PageInfo page = (PageInfo) item.pages.get(i);
         contentparams.pageseq = new Integer(page.sequence);
         contentparams.keywords = navparams.keywords;
         contentparams.viewtype = navparams.viewtype;
         contentparams.hitpage = navparams.pageseq;
         
         ViewParamGetter.fillTextParams(collection, contentparams);
         String texturl = viewStateHandler.getFullURL(contentparams);
         UIOutput.make(tofill, ComponentIDs.TEXT_TARGET + i, texturl);
       }
     }
     if (navparams.viewtype.equals(NavParams.IMAGE_VIEW)
         || navparams.viewtype.equals(NavParams.SIDE_VIEW)) {
       for (int i = 1; i < item.pages.size(); ++i) {
         // PageRenderParams contentparams = new PageRenderParams();
         // contentparams.itemID = navparams.itemID;
         // contentparams.viewtype = PageRenderParams.IMAGE_VIEW;
         PageInfo page = (PageInfo) item.pages.get(i);
         // contentparams.pageseq = page.sequence;
         // String imageurl = vsh.getFullURL(contentparams);
 
         String imageurl = (page == null || page.imagefile == null) ? "#"
             : urlmapper.fileToURL(page.imagefile);
         UIOutput.make(tofill, ComponentIDs.IMAGE_TARGET + i, imageurl);
       }
     }
 
     String[] values = new String[item.pages.size() - 1];
     String[] labels = new String[item.pages.size() - 1];
 
     for (int i = 1; i < item.pages.size(); ++i) {
       PageInfo page = (PageInfo) item.pages.get(i);
       values[i - 1] = Integer.toString(page.sequence);
       String text = page.text == null ? (item.hastext ? ""
           : " image ") + values[i - 1] : page.text.trim();
       if (text.length() == 0) {
         text = "[unnumbered]";
       }
       labels[i - 1] = text;
     }
     int page = navparams.pageseq;
     UIOutput.make(tofill, ComponentIDs.CURRENT_PAGE, "" + page);
     UIOutput.make(tofill, ComponentIDs.LAST_PAGE, "" + values.length);
     UIOutput.make(tofill, ComponentIDs.VIEW_TYPE, navparams.viewtype);
 
     UISelect.make(tofill, ComponentIDs.PAGE_SELECT, values, labels,
         values[page - 1]);
     boolean switchany = false;
     if (!(navparams.viewtype.equals(NavParams.SIDE_VIEW)) && item.hasimage
         && item.hastext) {
       NavParams sideparams = (NavParams) navparams.copyBase();
       sideparams.viewtype = NavParams.SIDE_VIEW;
       sideparams.viewID = FramesetProducer.VIEWID;
       UIInternalLink.make(tofill, "switch-side", sideparams);
       switchany = true;
     }
     if (!(navparams.viewtype.equals(NavParams.IMAGE_VIEW)) && item.hasimage) {
       NavParams sideparams = (NavParams) navparams.copyBase();
       sideparams.viewtype = NavParams.IMAGE_VIEW;
       sideparams.viewID = FramesetProducer.VIEWID;
       UIInternalLink.make(tofill, "switch-image", sideparams);
       switchany = true;
     }
     if (!(navparams.viewtype.equals(NavParams.TEXT_VIEW)) && item.hastext) {
       NavParams sideparams = (NavParams) navparams.copyBase();
       sideparams.viewtype = NavParams.TEXT_VIEW;
       sideparams.viewID = FramesetProducer.VIEWID;
       UIInternalLink.make(tofill, "switch-text", sideparams);
       switchany = true;
     }
     if (switchany) {
       UIOutput.make(tofill, "switch-any");
     }
     
     UIInternalLink.make(tofill, "advanced-search", new AdvancedSearchParams());
     UIInternalLink.make(tofill, "search-submit", new SearchResultsParams());
   }
 
   public ViewParameters getViewParameters() {
     return new NavParams();
   }
 
 }
