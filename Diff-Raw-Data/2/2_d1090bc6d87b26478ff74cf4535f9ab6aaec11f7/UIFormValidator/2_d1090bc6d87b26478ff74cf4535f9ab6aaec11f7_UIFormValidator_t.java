 package gov.nih.nci.nautilus.ui.struts.form;
 
 import gov.nih.nci.nautilus.lookup.AllGeneAliasLookup;
 import gov.nih.nci.nautilus.lookup.LookupManager;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.upload.FormFile;
 
 /**
  * @author BauerD Dec 15, 2004 
  * This class is used to validate input fields from the UI
  *  
  */
 public class UIFormValidator {
     private static Logger logger = Logger.getLogger(UIFormValidator.class);
     
 	   
     public static ActionErrors validateGeneSymbolisNotEmpty(String geneSymbol,
 			ActionErrors errors) {
 	    
 	    if (geneSymbol == null || geneSymbol.equals("")) {
 			errors
 					.add(
 							ActionErrors.GLOBAL_ERROR,
 							new ActionError(
 									"gov.nih.nci.nautilus.ui.struts.form.quicksearch.emptyGene"));
 		}
 		return errors;
 		/*
 		 * else { try { Collection results = LookupManager.getGeneSymbols();
 		 * }catch(Exception e){ e.printStackTrace(); } }
 		 */
 	}
 
 	public static ActionErrors validateQueryName(String queryName,
 			ActionErrors errors) {
 		if ((queryName == null || queryName.length() < 1)) {
 			errors.add("queryName", new ActionError(
 					"gov.nih.nci.nautilus.ui.struts.form.queryname.no.error"));
 		}
 		return errors;
 	}
 
 	public static ActionErrors validateChromosomalRegion(String chrosomeNumber,
 			String region, String cytobandRegion, String basePairStart,
 			String basePairEnd, ActionErrors errors) {
 		if (chrosomeNumber.trim().length() > 0) {
 			if (region.trim().length() < 1)
				errors.add("chromosomeNumber", new ActionError(
 						"gov.nih.nci.nautilus.ui.struts.form.region.no.error"));
 			else {
 				if (region.trim().equalsIgnoreCase("cytoband")) {
 					if (cytobandRegion.trim().length() < 1)
 						errors
 								.add(
 										"cytobandRegion",
 										new ActionError(
 												"gov.nih.nci.nautilus.ui.struts.form.cytobandregion.no.error"));
 				}
 				if (region.trim().equalsIgnoreCase("basePairPosition")) {
 					if ((basePairStart.trim().length() < 1)
 							|| (basePairEnd.trim().length() < 1)) {
 						errors
 								.add(
 										"basePairEnd",
 										new ActionError(
 												"gov.nih.nci.nautilus.ui.struts.form.basePair.no.error"));
 					} else {
 						if (!isBasePairValid(basePairStart, basePairEnd)) {
 							errors
 									.add(
 											"basePairEnd",
 											new ActionError(
 													"gov.nih.nci.nautilus.ui.struts.form.basePair.incorrect.error"));
 						}
 					}
 
 				}
 
 			}
 
 		}
 		return errors;
 
 	}
     
     public static ActionErrors validateGOClassification(String goClassification, ActionErrors errors) {
         if (goClassification!= null  && goClassification.trim().length() > 0) {
             goClassification = goClassification.trim();
             if (goClassification.startsWith("GO:")) {
                 String numberValue = goClassification.substring(goClassification.indexOf(":")+1);
                 if (goClassification.length() == 10){
                     try {
                         int n = Integer.parseInt(numberValue);
                     } catch (NumberFormatException ne){
                         errors
                                 .add(
                                         "goClassification",
                                         new ActionError(
                                                 "gov.nih.nci.nautilus.ui.struts.form.go.numeric.error"));
                     }
                 }else {
                     errors
                             .add(
                                     "goClassification",
                                     new ActionError(
                                             "gov.nih.nci.nautilus.ui.struts.form.go.length.error"));
                 }
             }else {
                 errors
                         .add(
                                 "goClassification",
                                 new ActionError(
                                         "gov.nih.nci.nautilus.ui.struts.form.go.startswith.error"));
             }
         }
     	return errors;
     }
     
     public static ActionErrors validate(String geneGroup, String geneList, FormFile geneFile, ActionErrors errors) {
         if (geneGroup!= null && geneGroup.trim().length() >= 1){
             if (geneList.trim().length() < 1 && geneFile == null){
                 errors.add("geneGroup", new ActionError("gov.nih.nci.nautilus.ui.struts.form.geneGroup.no.error"));
             }
             
         }
         
         return errors;
     }
     
     public static ActionErrors validateTextFileType(FormFile formFile, String fileContents, ActionErrors errors) {
     //Make sure the uploaded File is of type txt and MIME type is text/plain
         if(formFile != null  &&
           (!(formFile.getFileName().endsWith(".txt"))) &&
           (!(formFile.getContentType().equals("text/plain")))){
             errors.add(fileContents, new ActionError(
                             "gov.nih.nci.nautilus.ui.struts.form.uploadFile.no.error"));
         }   
         
         return errors;
     }
 
 	public static ActionErrors validateCloneId(String cloneId, String cloneListSpecify, FormFile cloneListFile, ActionErrors errors) {
          if (cloneId!= null && cloneId.trim().length() >= 1){
             if (cloneListSpecify.trim().length() < 1 && cloneListFile == null){
                 errors.add("cloneId",new ActionError(
                                 "gov.nih.nci.nautilus.ui.struts.form.cloneid.no.error"));
             }
             
         }
     
 		return errors;
     }
     
     public static ActionErrors validateSnpId(String snpId, String snpList, FormFile snpListFile, ActionErrors errors) {
           if (snpId != null && snpId.trim().length() >= 1) {
             if (snpList.trim().length() < 1
                     && snpListFile == null) {
                 errors.add("snpId", new ActionError(
                         "gov.nih.nci.nautilus.ui.struts.form.snpid.no.error"));
             }
         }
   
     	return errors;
     }
     
     /**
      * <p>Checks whether the string is ASCII 7 bit.</p>
      *
      * @param str  the string to check
      * @return false if the string contains a char that is greater than 128
      */
     public static boolean isAscii(String str){
         boolean flag = false;
         if(str != null){
             for(int i = 0; i < str.length(); i++){
                 if(str.charAt(i)>128){
                 return false;
                 }                   
             }
             flag = true;
         }
         return flag;
     }
     
     private static boolean isBasePairValid(String basePairStart,
 			String basePairEnd) {
 
 		int intBasePairStart;
 		int intBasePairEnd;
 		logger.debug("Start " + basePairStart + " End " + basePairEnd);
 		try {
 			intBasePairStart = Integer.parseInt(basePairStart);
 			intBasePairEnd = Integer.parseInt(basePairEnd);
 
 		} catch (NumberFormatException e) {
 			return false;
 		}
 
 		if (intBasePairStart >= intBasePairEnd)
 			return false;
 		return true;
 	}
     
     public static ActionErrors validateGeneSymbol(GeneValidator bean, ActionErrors errors) throws Exception{
         String gene = bean.getGeneSymbol();
 //      see if geneSymbol can't be found, if it cannot look for aliases
 	    if(!LookupManager.isGeneSymbolFound(gene)){
 			AllGeneAliasLookup[] allGeneAlias = LookupManager.getGenesForAlias(gene);
 			// if there are aliases , set the array to be displayed in the form and return the showAlias warning
 			if(allGeneAlias != null){
 			    bean.setAllGeneAlias(allGeneAlias);
 				for(int i =0; i < allGeneAlias.length ; i++){
 					AllGeneAliasLookup alias = allGeneAlias[i];
 					System.out.println(alias.getAlias()+"\t"+alias.getApprovedSymbol()+"\t"+alias.getApprovedName()+"\n");
 					errors
 					   .add(
 							ActionErrors.GLOBAL_ERROR,
 							new ActionError(
 									"gov.nih.nci.nautilus.ui.struts.form.quicksearch.showAlias",
 									gene));
 				}
 			}
 			// if there are no aliases, we don't have record, so show noRecord error message
 			else{
 			    System.out.println("no aliases found! \n");
 			    errors
 				   .add(
 						ActionErrors.GLOBAL_ERROR,
 						new ActionError(
 								"gov.nih.nci.nautilus.ui.struts.form.quicksearch.noRecord",
 								gene));
 			}
 		}
 	    //if gene Symbol can be found , execute query
 		else{
 		System.out.println(gene+" found! \n");
       }
 	    return errors;
     }
    
 }
