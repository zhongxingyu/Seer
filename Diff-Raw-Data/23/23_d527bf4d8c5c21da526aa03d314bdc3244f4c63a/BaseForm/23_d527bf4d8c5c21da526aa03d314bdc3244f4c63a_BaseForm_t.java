 // Created by Xslt generator for Eclipse.
 // XSL :  not found (java.io.FileNotFoundException:  (Bad file descriptor))
 // Default XSL used : easystruts.jar$org.easystruts.xslgen.JavaClass.xsl
 package gov.nih.nci.nautilus.struts.form;
 
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.util.LabelValueBean;
 
 import java.util.*;
 
 
 
 
 
 /** 
  * GeneExpressionForm.java created by EasyStruts - XsltGen.
  * http://easystruts.sf.net
  * created on 08-11-2004
  * 
  * XDoclet definition:
  * @struts:form name="geneExpressionForm"
  */
 public class BaseForm extends ActionForm {
 
 		
 	// Collections used for Lookup values.  
 	private ArrayList diseaseType;
 	private ArrayList geneTypeColl;
 	
 
 	// --------------------------------------------------------- Methods
 	public BaseForm(){
 			
 		// Create Lookups for Gene Expression screens 
 		setLookups();
 
 	}
 
 	protected boolean isBasePairValid(String basePairStart, String basePairEnd) {
 
 		int intBasePairStart;
 		int intBasePairEnd;
 		System.out.println("Start "+basePairStart+" End "+basePairEnd);
 		try {
 			intBasePairStart = Integer.parseInt(basePairStart);
 			intBasePairEnd = Integer.parseInt(basePairEnd);
 			
 		}
 		catch (NumberFormatException e) {
 			return false;
 		}
 		
 		if (intBasePairStart >= intBasePairEnd) return false;
 		return true;
 	}
 	
 	public void setLookups() {
 
 		diseaseType = new ArrayList();
 		geneTypeColl = new ArrayList();
 	
 		// These are hardcoded but will come from DB
		diseaseType.add( new LabelValueBean( "Astrocytic", "ASTROCYTOMA" ) );
		diseaseType.add( new LabelValueBean( "Oligodendroglial", "OLIG" ) );
		//diseaseType.add( new LabelValueBean( "Ependymal cell", "Ependymal cell" ) );
		diseaseType.add( new LabelValueBean( "Mixed gliomas", "MIXED" ) );
		//diseaseType.add( new LabelValueBean( "Neuroepithelial", "Neuroepithelial" ) );
		//diseaseType.add( new LabelValueBean( "Choroid Plexus", "Choroid Plexus" ) );
		//diseaseType.add( new LabelValueBean( "Neuronal and mixed neuronal-glial", "neuronal-glial" ) );
		//diseaseType.add( new LabelValueBean( "Pineal Parenchyma", "Pineal Parenchyma" ));
		//diseaseType.add( new LabelValueBean( "Embryonal", "Embryonal" ));
		diseaseType.add( new LabelValueBean( "Glioblastoma", "GBM" ));
 
 		
		//geneTypeColl.add( new LabelValueBean( "All Genes", "allgenes" ) );
 		geneTypeColl.add( new LabelValueBean( "Name/Symbol", "genesymbol" ) );
 		geneTypeColl.add( new LabelValueBean( "Locus Link Id", "genelocus" ) );
 		geneTypeColl.add( new LabelValueBean( "GenBank AccNo.", "genbankno" ) );
 
 	}
 
 // Getter methods for Gene Expression Lookup
 	public ArrayList getDiseaseType() 
 	{ return diseaseType; }
 
 	public ArrayList getGeneTypeColl() 
 	{ return geneTypeColl; }
 
 }
