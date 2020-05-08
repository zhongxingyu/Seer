 /*
  * Created on 07-May-2006
  */
 package uk.org.ponder.darwin.rsf.producers;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 
 import uk.org.ponder.darwin.rsf.params.AdvancedSearchParams;
 import uk.org.ponder.darwin.rsf.params.SearchResultsParams;
 import uk.org.ponder.darwin.search.DocTypeInterpreter;
 import uk.org.ponder.darwin.search.ItemFieldTables;
 import uk.org.ponder.rsf.components.UIBoundBoolean;
 import uk.org.ponder.rsf.components.UIBranchContainer;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.components.UISelect;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 import uk.org.ponder.stringutil.StringList;
 
 public class AdvancedSearchProducer implements ViewComponentProducer,
     ViewParamsReporter {
   public static final String VIEWID = "advanced-search";
   private Map subtablemap;
   private DocTypeInterpreter doctypeinterpreter;
 
   public String getViewID() {
     return VIEWID;
   }
 
   public void setItemFieldTables(ItemFieldTables fieldtables) {
     this.subtablemap = fieldtables.getTableMap();
   }
 
   public void setDocTypeInterpreter(DocTypeInterpreter doctypeinterpreter) {
     this.doctypeinterpreter = doctypeinterpreter;
   }
 
   public void fillComponents(UIContainer tofill, ViewParameters viewparamso,
       ComponentChecker checker) {
     AdvancedSearchParams viewparams = (AdvancedSearchParams) viewparamso;
     UIForm searchform = UIForm.make(tofill, "search-form",
         new SearchResultsParams());
     boolean advanced = !(viewparams.manuscript ^ viewparams.published);
 
     String title = "Advanced search";
     String identifiertitle = "('F', 'A' or manuscript catalogue number)";
     if (viewparams.manuscript) {
       title = "Darwin Online Manuscript Catalogue";
       identifiertitle = "(e.g. CUL-Dar99.94v)";
     }
     if (viewparams.published) {
       title = "Freeman Bibliographical Database";
       identifiertitle = "(Freeman's 'F' or Ancillary 'A' number. e.g. F373)";
     }
 
     UIOutput.make(tofill, "title", title);
     UIOutput.make(tofill, "identifier-title", identifiertitle);
 
     if (advanced) {
       UIBranchContainer itembranch = UIBranchContainer.make(searchform,
           "itemtypebranch:");
       UIBoundBoolean manuscript = UIBoundBoolean.make(itembranch, "manuscript",
           viewparams.manuscript);
       manuscript.fossilize = false;
       UIBoundBoolean published = UIBoundBoolean.make(itembranch, "published",
           viewparams.published);
       published.fossilize = false;
     }
     else {
       if (viewparams.published) {
         UIBoundBoolean.make(searchform, "published");
       }
       if (viewparams.manuscript) {
         UIBoundBoolean.make(searchform, "manuscript");
       }
     }
 
     if (viewparams.published || advanced) {
       UIBranchContainer langbranch = UIBranchContainer.make(searchform,
           "languagebranch:");
       Collection languagec = ((TreeMap) subtablemap.get("Language")).values();
       String[] languages = (String[]) languagec.toArray(new String[languagec
           .size()]);
       Arrays.sort(languages);
 
       UISelect.make(langbranch, "language", languages, languages, null, false);
     }
     if (viewparams.manuscript || advanced) {
       UIBranchContainer.make(searchform, "descriptionbranch:");
     }
 
     TreeMap doctypemap = (TreeMap) subtablemap.get("PartOfDocument");
     String[] doctypes = getDocTypes(doctypemap, viewparams.manuscript,
         viewparams.published);
     Arrays.sort(doctypes);
 
     UISelect.make(searchform, "documenttype", doctypes, doctypes, null, false);
   }
 
   private String[] getDocTypes(TreeMap doctypemap, boolean manuscript,
       boolean published) {
     StringList togo = new StringList();
     for (Iterator kit = doctypemap.keySet().iterator(); kit.hasNext();) {
       String key = (String) kit.next();
       String value = (String) doctypemap.get(key);
       boolean isconcise = doctypeinterpreter.isConciseType(value);
       if (published && isconcise || manuscript && !isconcise
           || !(manuscript ^ published)) {
         togo.add(value);
       }
     }
     return togo.toStringArray();
   }
 
   public ViewParameters getViewParameters() {
     return new AdvancedSearchParams();
   }
 
 }
