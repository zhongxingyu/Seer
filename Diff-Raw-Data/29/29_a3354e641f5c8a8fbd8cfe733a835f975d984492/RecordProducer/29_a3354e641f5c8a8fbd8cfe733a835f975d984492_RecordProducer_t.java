 /*
  * Created on 24 Aug 2006
  */
 package uk.org.ponder.darwin.rsf.producers;
 
 import org.apache.lucene.document.Document;
 
 import uk.org.ponder.darwin.item.ItemCollection;
 import uk.org.ponder.darwin.item.ItemDetails;
 import uk.org.ponder.darwin.lucene.DocHit;
 import uk.org.ponder.darwin.lucene.IndexItemSearcher;
 import uk.org.ponder.darwin.rsf.params.AdvancedSearchParams;
 import uk.org.ponder.darwin.rsf.params.NavParams;
 import uk.org.ponder.darwin.rsf.params.RecordParams;
 import uk.org.ponder.darwin.search.DocTypeInterpreter;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIInternalLink;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 import uk.org.ponder.util.Logger;
 
 public class RecordProducer implements ViewComponentProducer,
     ViewParamsReporter {
   public static final String VIEWID = "record";
   private ItemCollection itemcollection;
   private IndexItemSearcher indexItemSearcher;
   private DocTypeInterpreter doctypeinterpreter;
 
   public String getViewID() {
     return VIEWID;
   }
 
   public ViewParameters getViewParameters() {
     return new RecordParams();
   }
 
   public void setItemCollection(ItemCollection itemcollection) {
     this.itemcollection = itemcollection;
   }
 
   public void setIndexItemSearcher(IndexItemSearcher indexItemSearcher) {
     this.indexItemSearcher = indexItemSearcher;
   }
 
   public void setDocTypeInterpreter(DocTypeInterpreter doctypeinterpreter) {
     this.doctypeinterpreter = doctypeinterpreter;
   }
   
   private static NavParams getNavParams(String itemID) {
     NavParams navparams = new NavParams();
     navparams.viewID = FramesetProducer.VIEWID;
     navparams.itemID = itemID;
     navparams.pageseq = 1;
     return navparams;
   }
   
   private static class FieldAdder {
     private UIContainer tofill;
     private Document hit;
     public FieldAdder(UIContainer tofill, Document hit) {
       this.tofill = tofill;
       this.hit = hit;
     }
     public void add(String fieldname, String field) {
       String fieldvalue = hit.get(field);
       if (fieldvalue != null) {
         UIBranchContainer row = UIBranchContainer.make(tofill, "field-row:");
         UIOutput.make(row, "field-name", fieldname);
         UIOutput.make(row, "field", fieldvalue);
       }
     }
   }
   
   public void fillComponents(UIContainer tofill, ViewParameters viewparams,
       ComponentChecker checker) {
     RecordParams recparams = (RecordParams) viewparams;
     ItemDetails item = itemcollection.getItem(recparams.itemID);
 
     UIOutput.make(tofill, "identifier", recparams.itemID);
 
     if (item != null) {
       if (item.hasimage && item.hastext) {
         NavParams sideparams = getNavParams(recparams.itemID);
         sideparams.viewtype = NavParams.SIDE_VIEW;
         UIInternalLink.make(tofill, "switch-side", sideparams);
       }
       if (item.hasimage) {
         NavParams sideparams = getNavParams(recparams.itemID);
         sideparams.viewtype = NavParams.IMAGE_VIEW;
         UIInternalLink.make(tofill, "switch-image", sideparams);
       }
       if (item.hastext) {
         NavParams sideparams = getNavParams(recparams.itemID);
         sideparams.viewtype = NavParams.TEXT_VIEW;
         UIInternalLink.make(tofill, "switch-text", sideparams);
       }
     }
     
     String prev = itemcollection.adjust(recparams.itemID, ItemCollection.PREV);
     if (prev != null) {
       UIInternalLink.make(tofill, "prev", new RecordParams(prev));
     }
     
     String next = itemcollection.adjust(recparams.itemID, ItemCollection.NEXT);
     if (next != null) {
       UIInternalLink.make(tofill, "next", new RecordParams(next));
     }
     
     UIBranchContainer row = UIBranchContainer.make(tofill, "field-row:");
     UIOutput.make(row, "field-name", "Identifier:");
     UIOutput.make(row, "field", recparams.itemID);
     
     UIInternalLink.make(tofill, "advanced-search", new AdvancedSearchParams());
     
     try {
       DocHit[] hits = indexItemSearcher.getItemHit(recparams.itemID);
       Document hit = hits[0].document;
       FieldAdder adder = new FieldAdder(tofill, hit);
       String doctype = hit.get("documenttype");
       
       if (doctypeinterpreter.isConciseType(doctype)) {
         adder.add("Concise reference:", "reference");
         adder.add("Detailed reference:", "notes");
        UIOutput.make(tofill, "title", "The Freeman Bibliographical Database");
       }
       else {
         UIOutput.make(tofill, "title", "The Darwin Online Manuscript Catalogue");
         adder.add("Name:", "name");
         adder.add("Attributed title:", "attributedtitle");
         //adder.add("Source date:", "sourcedate");
        adder.add("Description:", "description");
        adder.add("Document type:", "documenttype");
        adder.add("Place created:", "place");
        adder.add("Cross reference:", "xref");
       }
     }
     catch (Exception e) {
       Logger.log.warn("Error performing search", e);
     }
   }
 
 }
