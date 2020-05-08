 package gov.nih.nci.rembrandt.web.struts.form;
 
 import gov.nih.nci.rembrandt.dto.lookup.AllGeneAliasLookup;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 
 
 public class QuickSearchForm extends BaseForm implements GeneValidator{
 	private String plot = null;
     private AllGeneAliasLookup[] allGeneAlias;
     private String quickSearchName = null;
 	private String quickSearchType = null;
 	private static Logger logger = Logger.getLogger(QuickSearchForm.class);
 	
 	public String getPlot() {
 		return plot;
 	}
 
 	public void setPlot(String str) {
 		plot = str;
 	}
 
 	public void setQuickSearchName(String str) {	    
 	    String[] quickSearchNameStrings = str.split(":");
 	    str = quickSearchNameStrings[0];
 		this.quickSearchName = str.trim();
 	}
 
 	public String getQuickSearchName() {
 		return this.quickSearchName;
 	}
 
 	public void setQuickSearchType(String str) {
 		this.quickSearchType = str;
 	}
 
 	public String getQuickSearchType() {
 		return this.quickSearchType;
 	}
 	public void setAllGeneAlias(AllGeneAliasLookup[] allGeneAlias){
 	    this.allGeneAlias = allGeneAlias;
 	    
 	}
 	public AllGeneAliasLookup[] getAllGeneAlias(){
 	    return this.allGeneAlias;
 	}
 	
 	public ActionErrors validate(ActionMapping mapping,
 			HttpServletRequest request) {
 	    ActionErrors errors = new ActionErrors();
	    UIFormValidator.validateGeneSymbolisNotEmpty(quickSearchName, errors);
		try {
			UIFormValidator.validateGeneSymbol(this, errors);
		} catch (Exception e) {
			logger.error(e);
		}
 		return errors;
 
 	}
 
     /* (non-Javadoc)
      * @see gov.nih.nci.nautilus.ui.struts.form.GeneValidator#setGeneSymbol(java.lang.String)
      */
     public void setGeneSymbol(String geneSymbol) {
         if(geneSymbol != null){
             geneSymbol = geneSymbol.trim();
         }
        this.quickSearchName = geneSymbol;
         
     }
 
     /* (non-Javadoc)
      * @see gov.nih.nci.nautilus.ui.struts.form.GeneValidator#getGeneSymbol()
      */
     public String getGeneSymbol() {
         return this.quickSearchName;
     }
 	
 }
