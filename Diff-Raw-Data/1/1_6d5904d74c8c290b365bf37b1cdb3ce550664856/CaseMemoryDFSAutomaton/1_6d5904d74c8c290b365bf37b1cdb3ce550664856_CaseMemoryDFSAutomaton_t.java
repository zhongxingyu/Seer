 /*
  * Paquete que se encarga de las funciones de b&uacute;squeda a trav&eacute;s de algoritmos de entre los
  * cuales se encuentra la b&uacute;squeda primero en profundidad y back-tracking
  * @see "Categor&iacute;a Sukia Search Automata en SUKIA SmallTalk" 
  */
 
 package system.searchAutomata;
 
 import redundantDiscriminationNet.RootNorm;
 import jade.util.leap.Iterator;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import oracleIDGui.SingleDSuggestionDialog;
 
 import ontology.CBR.PossibleSolution;
 import ontology.common.Description;
 import ontology.common.Descriptor;
 import ontology.common.SingleValue;
 
 
 import redundantDiscriminationNet.Index;
 import redundantDiscriminationNet.Node;
 import redundantDiscriminationNet.Norm;
 import redundantDiscriminationNet.SheetCase;
 import system.OracleIDSystem;
 import system.searchAutomata.output.CaseMemoryDFSAutomatonOutput;
 
 /**
  * SAVCase Depth-First-Search Automaton.
  * 1. The search process is based on a problem description composed of a non-empty set of SAVDescriptors.
  * 2. The search strategy is depth-first. That is, using all possible SAVDescriptors from the problem
  * description, advance as deep as possible within the Case Memory, before selecting solution cases. This
  * strategy ensures that all descriptors are considered.
  * @author pabloq
  */
 
 public class CaseMemoryDFSAutomaton {
     private Description solutionDescription;
     private Description confirmedDescription;
     private Description unconfirmedDescription;
     private Description doubtfulDescription;
     private Description unmatchedDescription;
     private Description justification;
     private List<PossibleSolution> possibleSolutions;
     private RootNorm netRoot;
     private Norm currentNorm;
     private int currentLevel;
     private int stopLevel;
     private SearchStatus status;
     private CaseMemoryDFSAutomatonOutput searchOutput;
 
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public CaseMemoryDFSAutomaton(RootNorm aSAVRoot){
         netRoot = aSAVRoot;
         this.initialize();
     }
     
     protected void initialize() {
         solutionDescription = new Description();
         confirmedDescription = new Description();
         unconfirmedDescription = new Description();
         doubtfulDescription = new Description();
         unmatchedDescription = new Description();
         justification = new Description();
         possibleSolutions = new ArrayList<PossibleSolution>();
         currentNorm = null;
         resetLevel();
         setStopLevel(currentLevel);
         newOutput();
         status = SearchStatus.FAIL;
     }
     /**
  * @see Define method name.
  * @param my parameters list
  * @return my return values
  */
     public void newOutput(){
         searchOutput = new CaseMemoryDFSAutomatonOutput();
     }
 /**
  *Category adding
  */
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void setCurrentNorm(Norm aNorm){
         currentNorm = aNorm;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void addToJustification(Descriptor aJustificationElement){
         justification.addToConcreteDescription(aJustificationElement);
     }
     
 	/**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void addToPossibleSolutions(PossibleSolution aPossibleSolution){
         this.possibleSolutions.add(aPossibleSolution);
         Collections.sort(this.possibleSolutions);
     }
 
     /**
      * 
      * @param possibleSolutions
      */
     public void setPossibleSolutions(List<PossibleSolution> possibleSolutions){
     	 this.possibleSolutions = possibleSolutions;
     }
     
     /**
      * 
      * @return
      */
     public List<PossibleSolution> getPossibleSolutions(){
         return possibleSolutions;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void setNextLevel(){
         currentLevel += 1;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void setPreviousLevel(){
         currentLevel -= 1;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void resetLevel(){
         currentLevel = 0;
     }
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void setStatus(SearchStatus aStatusValue){
         status = aStatusValue;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void setStopLevel(int aLevelNumber){
      stopLevel = aLevelNumber;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void addToConfirmedDescription(Descriptor aSAVDescriptor){
         confirmedDescription.addToConcreteDescription(aSAVDescriptor);
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void addToDoubtfulDescription(Descriptor aSAVDescriptor){
         doubtfulDescription.addToConcreteDescription(aSAVDescriptor);
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void addToSolutionDescription(Descriptor aSAVDescriptor){
         solutionDescription.addToConcreteDescription(aSAVDescriptor);
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void addToUnconfirmedDescription(Descriptor aSAVDescriptor){
         unconfirmedDescription.addToConcreteDescription(aSAVDescriptor);
     }
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void addToUnmatchedDescription(Descriptor aSAVDescriptor){
         unmatchedDescription.addToConcreteDescription(aSAVDescriptor);
     }
 
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public int getCurrentLevel(){
         return currentLevel;
     }
 
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public Object getCurrentNorm(){
         return currentNorm;
     }
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public Description getJustification(){
         return justification;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public RootNorm getNetRoot(){
         return netRoot;
     }
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public int getRootLevel(){
         return 1;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public CaseMemoryDFSAutomatonOutput getSearchOutput(){
        return searchOutput;
     }
 
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public SearchStatus getStatus(){
         return status;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public int getStopLevel(){
         return stopLevel;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public Description getConfirmedDescription(){
         return confirmedDescription;
     }
 
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public Description getDoubtfulDescription(){
     	return doubtfulDescription;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public Description getSolutionDescription(){
         return solutionDescription;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public Description getUnconfirmedDescription(){
         return unconfirmedDescription;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public Description getUnmatchedDescription(){
         return unmatchedDescription;
     }
 
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void backtrack(){
         if (!removeFromConfirmedDescription(currentNorm.getDescriptor()) ||
         		!processPreviousNorm()) {
             this.setStatus(SearchStatus.FAIL);
         }
         
         if (OracleIDSystem.getInstance().isInteractive())
         	indexDialog();
     }
     
     /**
      * Moves up a norm. This operation fails if an attempt is made to move farther up than the stop-level
      * norm, or if control is already at the net root.  If all goes well, an index verification is performed
      * on the norm. 
      * Automaton reference: PPN
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return Returns: nil - if the process fails; self - if everything dandy.
 
 	 */
     public boolean processPreviousNorm(){
         currentNorm = currentNorm.getNearestPredecessorNorm();
         
         this.setPreviousLevel();
         
         if ((currentLevel < stopLevel) || (currentNorm == netRoot))
             return false;
 
         return verifyIndices();        
     }
     /**
      * Removes, from tConfirmedDescription, a Descriptor<Object> that matches aNorm's attribute 
      * and value.  The removed Descriptor<Object> is placed in the tUnconfirmedDescription.
      * Automaton reference: RtCD
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return null - if the aNorm's level (i.e., currentLevel) is the less than or equal to the stopLevel,
      * or if the Descriptor<Object> was not found in tConfirmedDescriptor;
      * self - if all OK.
 	 */
     public boolean removeFromConfirmedDescription(Descriptor descritor){
         
         if (currentLevel <= stopLevel) return false;
         
         Iterator i = this.getConfirmedDescription().getAllDescriptors();
 		
 		while (i.hasNext()) {
 			Descriptor d = (Descriptor) i.next(); 
             if (d.equals(descritor)) {
                 addToUnconfirmedDescription((Descriptor)getConfirmedDescription().getDescriptors()
                 		.remove(getConfirmedDescription().getDescriptors().indexOf(d)));
                 return true;
             }
 
         }
         return false;
     }
     /**
      * This method is called during the backtracking process.  The purpose here is to determine if there is
      * at least one index to show to the user, hence continuing with the dialog. All of the current norm's
      * successors are scanned. As a reminder, at this point all of the current norm's successors must be
      * indices. For every retrieved index, all its index values should be checked against the unconfirmed or
      * doubtful descriptions. If at least ONE index value is not a member of either list, the dialog with the
      * user may continue in the context of the current norm. Else, a new backtrack operation should be
      * performed.
      * NOTE: The possibility of finding successor cases for the current norm is discarded, because successor
      * cases have already been searched for by the method retrieveCasesUnderNorm.
      * Automaton reference: VIdx
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return self - if at least one index value (in the form of a Descriptor<Object>) is not member of
 	 * either the the unconfirmed or doubtful lists; null - if removeSAVDescriptorFromTConfirmedDesc:
 	 * returns null; backtrack - if a new bactrack operation should be performed.
 	 */
     public boolean verifyIndices(){        
         for (Node normSucc: currentNorm.getSuccessors()){
             if (normSucc instanceof Index) {	
 	            //Scan the list of successors for the current index
 	            for (Node idxSucc: ((Index)normSucc).getSuccessors()){
 	                // If the Descriptor<Object> is not a member of the unconfirmed description, check it
 	                // against the doubtful description. If this time the SAVDescriptior is NOT a member of
 	                // the doubtful description, return self, indicating that there is at least one
 	                // index-value to show to the user
 	                if (!this.getUnconfirmedDescription().getDescriptors().contains(idxSucc.getDescriptor())) {
 	                    if (!this.getDoubtfulDescription().getDescriptors().contains(idxSucc.getDescriptor()))
 	                        return true;
 	                }
 	            }
             }
         }
         
         // At this point, the entire list of norm successors was scanned, and all of them were members
         // of either the unconfirmed or doubtful descriptions. Return null to indicate that another backtrack
         // must be performed. Before returning, the descriptor of the current norm must be removed from
         // the confirmed description and placed in the unconfirmed description
         if (!removeFromConfirmedDescription(currentNorm.getDescriptor()))
             return false;
 
         return processPreviousNorm();
       }
 
     /**
      * This method searches for indices, strictly under a norm other than the net root, that point to cases.
      * If a Descriptor in the argument list aProblemDescription matches one such index, it is placed in the
      * solution description. If a Descriptor matches an index that points to a Norm, it will remain in
      * aProblemDescription. If a Descriptor does not match any index, it will also remain in aProblemDescription.
      * PRECONDITION: (aProblemDescription isEmpty not) and (self currentNorm != self netRoot not) and
      * (aPossibleSolutionsList isEmpty)
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return self : if the process ran OK; cancel : is the user cancels; -1 : error value, if the
 	 * precondition is not met.
  	 */
     public void searchPossibleSolutionsUnderCurrNorm(Description aProblemDescription){
 
         //Check precondition
         if (aProblemDescription.getDescriptors().isEmpty() || (currentNorm == netRoot) || 
         		!this.getPossibleSolutions().isEmpty()) {
         	this.setStatus(SearchStatus.ERROR);
         	return;
         }
 
         //Create the temporary process lists
         List<Descriptor>  tempList = new ArrayList<Descriptor>();
         List<SheetCase> aCaseList = new ArrayList<SheetCase>();
         //Scan the the Descriptor list of the problem description. Look for indices that strictly point to cases
 
         while (aProblemDescription.getDescriptors().isEmpty() != true){
             //Remove the next Descriptor<Object>
             Descriptor d = (Descriptor) aProblemDescription.getDescriptors().remove(0);
             //Look for a matching index
 
             Index idx = currentNorm.getSuccessorIndex(d);
 
             //If the descriptor did not match any index, take it out of the problem description and
             //place it in the temporary list
             if (idx == null){
             	if (OracleIDSystem.getInstance().isInteractive()) {
 	                //The descriptor may have an inaccurate value. Try to establish a dialog with the user.
 	            	SearchStatus result = searchPossibleSolutionsDialog(d);
 	            	
 	                if (result.equals(SearchStatus.CANCEL))
 	                	return;
 	                
 	                if (result.equals(SearchStatus.FAIL) || result.equals(SearchStatus.IDXNOTFOUND))
 	                	tempList.add(d);
 	                
 	                if (result.equals(SearchStatus.SUCCESS))
 	                	addToUnmatchedDescription(d);
             	}
             } else{
                 //Index found. get the IndexValue successor
                 Node succ = idx.getSuccessor(d);
 
                 //If a matched index points to a Norm, don't process it. Place the descriptor in a temporary list
                 if (succ instanceof Norm){
                     tempList.add(d);
                 } else {
                     /*The matched index points to a case. Place the corresponding descriptor in the solution
                      *description and associate the corresponding case to a PossibleSolution. Next, place the
                      *ossible solution in the output possible solutions list. Finally, remove the descriptor
                      *from the solution description
                      */
                     this.addToSolutionDescription(d);
                     
                     aCaseList.add((SheetCase)succ);
                     List<PossibleSolution> pSolutionList = associateCasesToPossibleSolutions(aCaseList);
                     this.addToPossibleSolutions(pSolutionList.remove(0));
 
                     this.getSolutionDescription().getDescriptors().remove(d);
                 }
             }
         }
         
         //Put the descriptors that didn't match indices back in the problem description list
         while (tempList.isEmpty() != true)
             aProblemDescription.addDescriptors(tempList.remove(0));
     }
 
     /**
      * This method searches for indices, strictly under the net root, that point to cases. If a Descriptor
      * in the argument list aProblemDescription matches one such index, it is placed in the solution
      * description. If a Descriptor matches an index that points to a Norm, it will remain in
      * aProblemDescription. If a Descriptor does not match any index, it will be placed in the unmatched
      * descriptor list.
      * PRECONDITION:(aProblemDescription isEmpty not) and (self currentNorm = self netRoot) and (aPossibleSolutionsList isEmpty) and  (tSolutionDescription isEmpty)"
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return true : if the process ran OK; cancel : is the user cancels; false : error value, if the
 	 * precondition is not met.    
 	 */
     public void searchPossibleSolutionsUnderRoot(Description aProblemDescription){
         //Check precondition
         if (aProblemDescription.getDescriptors().isEmpty()) {
         	this.setStatus(SearchStatus.ERROR);
         	return;
         }
 
         if (!((currentNorm == netRoot) && this.getPossibleSolutions().isEmpty() 
         		&& this.getSolutionDescription().getDescriptors().isEmpty())) {
         	this.setStatus(SearchStatus.ERROR);
         	return;
         }
         
         //Create the temporary process lists
         List<Descriptor> tempList = new ArrayList<Descriptor>();
         List<SheetCase> aCaseList = new ArrayList<SheetCase>();
 
         //Scan the the Descriptor list of the problem description. Look for indices that strictly point to cases
         while (aProblemDescription.getDescriptors().isEmpty() != true){
             //Remove the next Descriptor<Object>
             Descriptor d = (Descriptor) aProblemDescription.getDescriptors().remove(0);
             //Look for a matching index
             Index idx = currentNorm.getSuccessorIndex(d);
 
             //If the descriptor did not match any index, take it out of the problem description and
             //place it in the unmatched descriptor list
 
             if (idx == null){
                 //At this point, the descriptor inevitably goes to the unmatched description
                 addToUnmatchedDescription(d);
 
                 //However, the descriptor may have an inaccurate value. Try to establish a dialog with the user
                 //using a partial match.
 
                 if (OracleIDSystem.getInstance().isInteractive()) {
 	                SearchStatus result = searchPossibleSolutionsDialog(d);
 	                if (result.equals(SearchStatus.CANCEL))
 	                	return;
                 }
             } else {
                 //Index found. get the IndexValue successor
                 Node succ = idx.getSuccessor(d);
 
                 if (succ instanceof Norm)
                     tempList.add(d);
                 else {
                     //The matched index points to a case. Place the corresponding descriptor in the solution description
                     //and associate the corresponding case to a PossibleSolution. Next, place the possible solution in the
                     //output possible solutions list. Finally, remove the descriptor from the solution description
                     addToSolutionDescription(d);
                     
                     aCaseList.add((SheetCase)succ);
                     List<PossibleSolution> pSolutionList = associateCasesToPossibleSolutions(aCaseList);
                     this.addToPossibleSolutions(pSolutionList.remove(0));
 
                     getSolutionDescription().getDescriptors().remove(d);
 
                 }
             }
 
         }
         
         //Put the descriptors that match Norms back in the problem description list
         while (tempList.isEmpty() != true)
             aProblemDescription.addDescriptors(tempList.remove(0));
     }
 
     /**
  * @see Define method name.
  * @param my parameters list
  * @return my return values
  */
     public void searchPossibleSolutionsUsingIndices(Description aProblemDescription) {
         //If the argument search-list is empty, something wrong happened. Return error value
         if (aProblemDescription.getDescriptors().isEmpty()) {
         	status = SearchStatus.ERROR;
         	return;
         }
         //Make sure the current level is ALWAYS greater than or equal to the root level
         if (currentLevel < getRootLevel()) {
         	status = SearchStatus.ERROR;
         	return;
         }
 
         //Check if control is located at the root (i.e., no Norm was ever found in the previous state)
         if (currentLevel == getRootLevel()){
             //Search for cases pointed to by indices directly under the root. If there are any, the argument
             //list possibleSolutions will not be empty
         	searchPossibleSolutionsUnderRoot(aProblemDescription);
         	
             return;
         }
         
         // At this point, control is located on a Norm other than the net root. Initially, scan the Descriptor list in search of indices
         // that strictly point to cases. If a matched index points to a case, remove the corresponding Descriptor from the problem
         // description and place it in the solution description.  Next, associate the retrieved case to a PossibleSolution, and place
         // this possible solution in the case list. Finally, remove the last descriptor of the solution list. If the matched index DOES
         // NOT point to a case, leave the corresponding Descriptor in the problem description
         
         searchPossibleSolutionsUnderCurrNorm(aProblemDescription);
 
         if (this.getStatus() == SearchStatus.CANCEL || this.getStatus() == SearchStatus.ERROR)
         	return;
         
         // If at the end of the scanning process, the problem description is empty, the possible solutions list MUST have at least
         // one item. Else, something weird happened.  In that situation, return the error value. However, if the case list is not empty,
         // return self
         if (aProblemDescription.getDescriptors().isEmpty())
             return;
         
         // At this point, the problem description is not empty. Move control to the root and search for
         // indices that strictly point to cases.
         Norm savNorm = currentNorm;
         int savLevel = currentLevel;
         setCurrentNorm(netRoot);
         resetLevel();
         setNextLevel();
         
         //Searching for cases under the root means that the solution description thus far obtained is invalid.
         //Therefore, this description should be considered as unmatched. Thus, place the solution description
         //items in the unmatched description, and also copy them to a temporary list. The reason for the copy
         //list is that, depending on the output from the root-search, it may be necessary to put all descriptors
         //back in the solution description, in order to try the next search strategy
 
         Description tempMovedSolution = moveDescriptors(getSolutionDescription(),getUnmatchedDescription());
 
         // Same sitution as with the solution description.  In this case, place the confirmed description items in the
         // unconfirmed description, and also copy them to another temporary list
 
         Description tempMovedConfirmed = moveDescriptors(getConfirmedDescription(),getUnconfirmedDescription());
         
         // Call the search-cases-under-root method with a clean &amp; empty possible solutions list (its part of the precondition)
         //List<PossibleSolution> pSolutions = new ArrayList<PossibleSolution>(); OJO
 
         searchPossibleSolutionsUnderRoot(aProblemDescription);
         
         if (this.getStatus().equals(SearchStatus.CANCEL) || this.getStatus().equals(SearchStatus.ERROR))
         	return;
                 
         //Restore everything to its previous state
 
         setCurrentNorm(savNorm);
         resetLevel();
 
         while (savLevel> 0) {
             setNextLevel();
             savLevel = savLevel -1;
         }
         
         // If after the root-search the possible solutions and problem description lists are empty, the next strategy is to try
         // retrieving cases from the current norm. Therefore, just as with the current norm and current level, both the solution
         // and confirmed descriptions MUST be set back to their original state (i.e., before doing the root-search). So, remove
         // all the matching items in the temporary lists from the unmatched and unconfirmed descriptions and place them back
         // in the corresponding solution and confirmed ones
         deleteDescriptors(tempMovedSolution, getUnmatchedDescription());
         while (!tempMovedSolution.getDescriptors().isEmpty())
         	getSolutionDescription().addDescriptors((Descriptor) tempMovedSolution.getDescriptors().remove(0));
         deleteDescriptors(tempMovedConfirmed,getUnconfirmedDescription());
         while (!tempMovedConfirmed.getDescriptors().isEmpty())
         	getSolutionDescription().addDescriptors((Descriptor) tempMovedConfirmed.getDescriptors().remove(0));        
 
         //Upon return from the root search, the following situations may occur:
         //	a) the possible solutions list is empty.
         //		a1) if the problem description is now empty (i.e., all Descriptors are located in the unmatched list), then call the
         //		     state-method retrieveCasesUnderCurrNorm, in order to give the search process a last chance.
         //		a2) if the problem description is still NOT empty (i.e., the remaining descriptors point to Norms in other paths), return nil.
         //	b) the possible solutions list is NOT empty. Return self, regardless of whether or not the problem description is empty.
 
         if (possibleSolutions.isEmpty()) {
             //Precondition for method retrieveCasesUnderCurrNorm : isEmpty(aProblemDescription)
             if (aProblemDescription.getDescriptors().isEmpty())
                 retrieveCasesUnderCurrentNorm();
         }
     }
 /**
  *Category private
  */
     
  /**
   * This method is used in conjuntion with prepareSuccessfulOutput.  The purpose of this method is to create
   * an instance of PossibleSolution for every case in the list argument aCaseList.
   * Automaton reference: none.
   * @see Define method name.
   * @param my parameters list
   * @return a list of PossibleSolutions.
   */
     private List<PossibleSolution> associateCasesToPossibleSolutions(List<SheetCase> aCaseList){
         List<PossibleSolution> psList = new ArrayList<PossibleSolution>();
         
         for (SheetCase mycase: aCaseList){
             PossibleSolution ps = new PossibleSolution();
             ps.setSolution(mycase.getCase());
             ps.getSolutionDescription().addAllToConcreteDescription(getSolutionDescription());
             ps.getConfirmedDescription().addAllToConcreteDescription(getConfirmedDescription());
             ps.getUnconfirmedDescription().addAllToConcreteDescription(getUnconfirmedDescription());
             ps.getDoubtfulDescription().addAllToConcreteDescription(getDoubtfulDescription());
             psList.add(ps);
         }
         
         return psList;
     }
 
     /**
  * @see Define method name.
  * @param my parameters list
  * @return my return values
  */
     private void deleteDescriptors(Description aTempDeleteList, Description aList) {
         for (int i=0;(i<aTempDeleteList.getDescriptors().size());i++) {
             Descriptor d = (Descriptor) aTempDeleteList.getDescriptors().get(i);
             int j = 0;
             while (j<= aList.getDescriptors().size()){
                 if ((aList.getDescriptors().get(j).equals(d))){
                     aList.getDescriptors().remove(0);
                     j = aList.getDescriptors().size() + 1;
                 }else{
                     j += 1;
                 }
             }
         }
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     // Ojo este mtodo no se usa
     @SuppressWarnings("unused")
 	private List<Norm> filterUselessNorms(List<Norm> aNormAlternativeList){
 
         List<Norm> newList = new ArrayList<Norm>();
         while (aNormAlternativeList.isEmpty() != true){
             Norm normAlternative = aNormAlternativeList.remove(0);
 
             if (normAlternative.successorCases().isEmpty() != true)
                 newList.add(normAlternative);
             else {
                 //First, get the list of successors for the current norm
                 List<Index> successorList = ((Norm)normAlternative).successorIndexes();
                 for (int i = 0; (i<successorList.size());i++){
                     //Proceed to extract the next index
                     Index idx = successorList.get(i);
 
                     //Parse the list of IndexValues associated to the index
                     List<Node> idxSuccessors = idx.getSuccessors();
                     for (int j = 0; (j<idxSuccessors.size());j++) {
                         //Make sure that the descriptor is NOT already included in neither the unconfirmed and doubtful descriptions
                         if (!getUnconfirmedDescription().getDescriptors().contains(idxSuccessors.get(j).getDescriptor()) &&
                         		!getDoubtfulDescription().getDescriptors().contains(idxSuccessors.get(j).getDescriptor())) {
                             newList.add((Norm)normAlternative);
                         }
                     }
 
                 }
             }
         }
         
         return newList;
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     private boolean isUseless(Node n){
         if ((n instanceof SheetCase)) return false;
         
         if (((Norm)n).successorCases().isEmpty() != true) return false;
         
         //First, get the list of successors for the current norm
         for (Index idx:((Norm)n).successorIndexes()) {
             //Parse the list of IndexValues associated to the index
             for (Node idxSuccessor:idx.getSuccessors()) {
                 //Make sure that the descriptor is NOT already included in neither the unconfirmed and doubtful descriptions
                 if (!getUnconfirmedDescription().getDescriptors().contains(idxSuccessor.getDescriptor())
                 		&& !getDoubtfulDescription().getDescriptors().contains(idxSuccessor.getDescriptor()))
                 	return false;
             }
         }
         
         return true;
     }
     /**
  * @see Define method name.
  * @param my parameters list
  * @return my return values
  */
     private Description moveDescriptors(Description aList, Description anotherList) {
     	Description aCopyList;
     	
     	aCopyList = new Description();
     	
         while (aList.getDescriptors().isEmpty() != true){
             Descriptor d  = (Descriptor) aList.getDescriptors().remove(0);
             anotherList.addDescriptors(d);
             aCopyList.addDescriptors(d);
         }
         
         return aCopyList;
     }
     
 /**
  * @see Define method name.
  * @param my parameters list
  * @return my return values
  */
     public void beginNewSearch(Description anOldProblemDescription){
     	/*If the automaton returns a non-empty problem description list, then the REASONER MUST call it
     	again with that remaining description, using this method. Before doing so, all lists, except the
     	doubtful and unconfirmed ones, MUST be flushed. Make sure the new search begins at root level, and
     	all necessary control variables are correctly set. This process repeats until the problem description
     	list is EMPTY*/
     	currentNorm = null;
         resetLevel();
         setStopLevel(currentLevel);
         solutionDescription = new Description();
         confirmedDescription = new Description();
         unmatchedDescription = new Description();
         justification = new Description();
        possibleSolutions = new ArrayList<PossibleSolution>();
         newOutput();
         status = SearchStatus.FAIL;
         
         beginSearch(anOldProblemDescription);
     }
 
 /**
  *Category user dialog
  */
     
     /**
      * MANAGEMENT OF AN INCOMPLETE DESCRIPTION. A dialog with the user is established when the automaton
      * has run out of descriptors from the problem description, and has not located any cases.  In addition,
      * the current norm does not have any successor cases. The idea is then to present index alternatives to
      * the user, initially those indices pointing to cases. If no case is retrieved, index alternatives
      * pointing to norms is then presented to the user. If nothing works, the user has the chance to cancel
      * the operation or backtrack to a previous norm.
      * PRECONDITION: The successor list of the current norm does not contain any associated cases.
      * Automaton reference: IdxDial"
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return null - If the process was unsuccessful in finding a case; self - If one case was found.
 	 */
     public void indexDialog(){
         List<Node> alternativeCases = new ArrayList<Node>();
         List<Node> alternativeNorms = new ArrayList<Node>();
         
         //First, get the list of successors for the current norm
         List<Index> successorList = currentNorm.successorIndexes();
         
         for (Index idx:successorList){
             //Parse the list of IndexValues associated to the index            
             for (Node idxSuccessor:idx.getSuccessors()){
                 //Make sure that the descriptor is NOT already included in neither the unconfirmed and doubtful descriptions
                 if (!getUnconfirmedDescription().getDescriptors().contains(idxSuccessor.getDescriptor())
                 		&& !getDoubtfulDescription().getDescriptors().contains(idxSuccessor.getDescriptor()))
                     if (idxSuccessor instanceof SheetCase)
                     	alternativeCases.add(idxSuccessor);
                     else alternativeNorms.add(idxSuccessor);
             }
         }
         //Present the list of alternatives (associated to one index) to the user, preferably the cases
         if (alternativeCases.isEmpty() != true) {
             presentChoicesDialog(alternativeCases);
             
             return;
         }
 
         if (alternativeNorms.isEmpty() != true){
             List<Node> newList = new ArrayList<Node>();
             
             //Dejar las normas ms tiles
             while (alternativeNorms.isEmpty() != true){
             	Node n = alternativeNorms.remove(0);
             	//Estaba isUseless(alternativeNorms.remove(0))
                 if (!isUseless(n)) newList.add(n);
             }
             
             if (newList.isEmpty() != true){
             	presentChoicesDialog(newList);
             	
             	return;
             }
 
         }
         
         //At this point, all alternatives for this norm failed because they were either unconfirmed or
         // rejected due to doubt. Present to the user the possibility to BACKTRACK
 
         int answer = JOptionPane.showConfirmDialog(OracleIDSystem.getInstance().getSystemGui(), "Hasta ahora las alternativas presentadas no han " +
         		"ayudado a resolver el problema. \n Desea continuar evaluando otras alternativas?", "OracleID",
         		JOptionPane.YES_NO_OPTION);
 
         if (answer == JOptionPane.NO_OPTION) {
             this.setStatus(SearchStatus.CANCEL);
             
             return;
         }
         
         backtrack();
     }
 
 
     /**
      * The elements of the argument alternativeList are lists of the form: (Descriptor<Object> IndexValue)
      * Automaton reference: This method is an extension of indexDialog, referenced by IdxDial
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return self - if successful (i.e., at least one possible solution was found); nil - if
 	 * retrieveCasesUnderNorm fails; cancel - if the user cancels the dialog; fail - if all alternatives
 	 * were rejected (either because they did not match or the user was in doubt)
 	 */
 	public void presentChoicesDialog(List<Node> alternativeList){
     	String message;
     	SingleDSuggestionDialog.Response answer;
     	String value[];
     	
     	for (Node n:alternativeList) {
     		if (!this.isUseless(n)) {
     			value = new String[1];
     			
     			if (n.getDescriptor().getValue() instanceof String)
     				value[0] = (String)n.getDescriptor().getValue();
     			else if (n.getDescriptor().getValue() instanceof SingleValue)
     				value[0] = "" + ((SingleValue)n.getDescriptor().getValue()).getValue();    			    		
     			    			
         		// Prepare the inquiry to be presented to the user
         		message = "Presenta la caracterstica \"" + n.getDescriptor().getAttribute() 
         			+ "\" de la estructura \"" + n.getDescriptor().getStructure() +         					
         					"\" el siguiente valor?";
         		
         		SingleDSuggestionDialog dialog = new SingleDSuggestionDialog(OracleIDSystem.getInstance().getSystemGui(), message, value);
         		
         		answer = dialog.getResponse();
         		
         		if (answer == SingleDSuggestionDialog.Response.CANCEL) {
         			this.setStatus(SearchStatus.CANCEL);
         			return;
         		}
         		
         		if (answer == SingleDSuggestionDialog.Response.ACEPT) {
         			// The solution is a norm
         			if (n instanceof Norm) {
         				processNextNorm((Norm)n);
         				retrieveCasesUnderCurrentNorm();
         				
         				return;
         			} else if (n instanceof SheetCase) {
         				// The solution is a case
         				addToConfirmedDescription(n.getDescriptor());
         				
         				List<SheetCase> caseList = new ArrayList<SheetCase>();
         				caseList.add((SheetCase)n);
         				List<PossibleSolution> ps = associateCasesToPossibleSolutions(caseList);
         				
         				this.getConfirmedDescription().getDescriptors().remove(n.getDescriptor());
         				
         				while(ps.isEmpty() != true)
                             addToPossibleSolutions(ps.remove(0));
         				
         				return;
         			}
         		}
         		
         		if (answer == SingleDSuggestionDialog.Response.REJECT)
         			addToUnconfirmedDescription(n.getDescriptor());
         		
         		if (answer == SingleDSuggestionDialog.Response.DOUBT)
         			addToDoubtfulDescription(n.getDescriptor());
     		}
         	
     	}
 
     	this.setStatus(SearchStatus.FAIL);
     }
     /**
      * Automaton reference: PNN
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void processNextNorm(Norm aNorm){
         addToConfirmedDescription(aNorm.getDescriptor());
         setCurrentNorm(aNorm);
         setNextLevel();
     }
     
     /**
      * MANAGEMENT OF A POSSIBLY INNACURATE DESCRIPTOR.
      * This method establishes a dialog with the user. First, an index is searched, under the current 
      * norm, that matches the descriptor argument's attribute only. If there is a match, a new
      * Descriptor<Object> is created for every index value found for the retrieved index. The new
      * information is presented to the user, in order for him/her to determine if it applies.
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return idxNotFound - if an index was not found using the descriptor argument's attribute. 
 	 * cancel - if the user cancels; success - if a case was accepted; fail - if index values point to norms,
 	 * or the user rejected possibilities, or a combination of both situtions occurred.
 	 */
 	public SearchStatus searchPossibleSolutionsDialog(Descriptor aSAVDescriptor){
 		Map<String, Descriptor> descriptors;
 		List<String> values;
 		SingleDSuggestionDialog.Response response;
 		
         //Partial match: Look for an index under the current norm, whose label matches the
         //descriptor's attribute. Disregard the descriptor's value
         Index idx = currentNorm.getSuccessorIndex(aSAVDescriptor.getAttribute());
         if (idx == null)
         	return SearchStatus.IDXNOTFOUND;
 
         //The index was found. Create a temporary possible solutions list
         List<SheetCase> aCaseList = new ArrayList<SheetCase>();
 
         for (Node n:idx.getSuccessors()){
             //Determine if the successor is a norm
             if ((n instanceof Norm) != true){
                 //Since this descriptor corresponds to a partial match, make sure that it is NOT already
                 //included in neither the unconfirmed, doubtful, unmatched, solution, or confirmed descriptions
                 if (!this.getUnconfirmedDescription().getDescriptors().contains(n.getDescriptor()) &&
                 		!this.getDoubtfulDescription().getDescriptors().contains(n.getDescriptor()) &&
                 		!this.getUnmatchedDescription().getDescriptors().contains(n.getDescriptor()) &&
                 		!this.getSolutionDescription().getDescriptors().contains(n.getDescriptor()) &&
                 		!this.getConfirmedDescription().getDescriptors().contains(n.getDescriptor())) {
                     //FUTURE IMPROVEMENT (documented by HB on 10-Sep-1999):
                     //1. Retrieve the taxon corresponding to the successor case.
                     //2. Retrieve the structure-atribute, from the taxon's description, that matches (d structure) and (d attribbute).
                     //3. Retrieve the weighted-value list from the corresponding taxon's structure-attribute.
                     //4. Determine, using the weighted-value list, SimAssessor, and SimRanges, the degree of similarity
                     //between d and aSAVDescriptor.
                     //5. If the degree of similarity satisfies the user-defined expectation E7, then present d to the user.
                     //Else, don't present it to the user and continue with the rest of this process.
                     //6. Document the whole process in the search process justification.
                     aCaseList.add((SheetCase)n);
                 }
             }
             
         }
         
         if (aCaseList.size() == 0)
         	return SearchStatus.FAIL;
         
         String oldValue = null;
         if (aSAVDescriptor.getValue() instanceof String)
 			oldValue = (String) aSAVDescriptor.getValue();
 		else if (aSAVDescriptor.getValue() instanceof SingleValue)
 			oldValue = "" + ((SingleValue)aSAVDescriptor.getValue()).getValue();
         
         String message = "No reconozco el valor \"" + oldValue + "\" del atributo \""+ aSAVDescriptor.getAttribute()+
         			"\" de la estructura \"" +aSAVDescriptor.getStructure()+ "\" brindado en la descripcin del espcimen." +
         			"\nSin embargo, spuedo reconocer los siguientes valores.\nEs algun valor vlido?";
         
         descriptors = new HashMap<String, Descriptor>();
         
         values = new ArrayList<String>();
         
     	for (SheetCase sc:aCaseList) {
     		if (sc.getDescriptor().getValue() instanceof String) {
     			values.add((String)sc.getDescriptor().getValue());
     			descriptors.put((String)sc.getDescriptor().getValue(), sc.getDescriptor());
     		} else if (sc.getDescriptor().getValue() instanceof SingleValue) {
     			values.add(Double.toString(((SingleValue)sc.getDescriptor().getValue()).getValue()));
     			descriptors.put(Double.toString(((SingleValue)sc.getDescriptor().getValue()).getValue()),
     					sc.getDescriptor());
     		}
     	}
     	
         SingleDSuggestionDialog dialog = new SingleDSuggestionDialog(OracleIDSystem.getInstance().getSystemGui(), message, values.toArray());
 		
 		response = dialog.getResponse();
     	    	
     	//El usuario rechaza la sugerencia
         if (response == SingleDSuggestionDialog.Response.REJECT){
         	for (Descriptor d:descriptors.values())
                 addToUnconfirmedDescription(d);
             
             this.setStatus(SearchStatus.FAIL);
             
             return SearchStatus.FAIL;
         }
 
         //User is in doubt. Flush the descriptor list by placing all SAVDescriptors in the doubtful
         //description. Continue processing the next attribute
         if (response == SingleDSuggestionDialog.Response.DOUBT){
         	for (Descriptor d:descriptors.values())
                 addToDoubtfulDescription(d);
             
         	this.setStatus(SearchStatus.FAIL);
             
             return SearchStatus.FAIL;
         }
         
         //User cancels. Cancel the process and exit.
         if (response == SingleDSuggestionDialog.Response.CANCEL)
         	return (status = SearchStatus.CANCEL);
 
         SheetCase sc = this.getSheetCase(descriptors.get(dialog.getValue()), aCaseList);
         
         //At this point, the answer must be successful.
         //Associate the confirmed case to PossibleSolution. Then exit successfully
         addToConfirmedDescription(sc.getDescriptor());
         
         aCaseList.add(sc);
         List<PossibleSolution> pSolutionList = associateCasesToPossibleSolutions(aCaseList);
         this.addToPossibleSolutions(pSolutionList.remove(0));
         
         this.getConfirmedDescription().getDescriptors().remove(sc.getDescriptor());
         
         return (status = SearchStatus.SUCCESS);
     }
     
 /**
  *Category norm-pointing search
  */
 	
 	/**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public boolean checkPrecondition(Description  aProblemDescription){
         if (aProblemDescription.getDescriptors().isEmpty()) return false;
 
         String sName = ((Descriptor)aProblemDescription.getDescriptors().get(0)).getStructure();
         if (aProblemDescription.getDescriptors().size() > 0) {
             for (int i = 1; i < aProblemDescription.getDescriptors().size(); i++)                
                 if (sName.equals(((Descriptor)aProblemDescription.getDescriptors().get(i)).getStructure()) 
                 		!= true) return false;
         }
         
         return true;
     }
     
     /**
      * This method is called from indexDialog, and it may be called recursively. However, it needs to be
      * executed only once. In order to avoid continuous execution of this method, check the (list) variable
      * 'justification' of searchOutput. Upon termination of the automaton process, 'justification' MUST
      * always be non-nil. Thus, if it's non-nil, then don't execute this method.
      * This method is executed whennthe automaton has failed to provide a solution for the given problem
      * description.
      * Automaton reference: PFO
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     protected void prepareFailedOutput(){
         /*if (searchOutput.getJustification() != null) return;*/
         
         searchOutput.setJustification(justification);
         searchOutput.setUnmatchedDescription(getUnmatchedDescription());
         
         setStatus(SearchStatus.FAIL);
     }
 
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     protected void prepareSuccessfulOutput(){
         /*if (searchOutput.getPossibleSolutions() != null)
         	return;*/
         
         searchOutput.setPossibleSolutions(getPossibleSolutions());
         searchOutput.setJustification(justification);
         searchOutput.setUnmatchedDescription(getUnmatchedDescription());
         
         setStatus(SearchStatus.SUCCESS);
     }
     
     /**
      * Initial state of the search automaton.  Control is placed on the root norm. The final step is a call
      * to the next state (i.e., searchForNormWith).
      * NOTE: The argument aProblemDescription is a non-empty list of Descriptors.
      * Precondition:
      * 1. aProblemDescription is a non-empty set of SAVDescriptors.
      * 2. For all s1, s2::Descriptor in aProblemDescription : (s1 structure) = (s2 structure).
      * Automaton reference: bW
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return null - if the argument is an empty list, or object returned by searchForNormWith:
 	 */
     public SearchStatus beginSearch(Description aProblemDescription){
     	
         //Check part 1. of the precondition
     	if (checkPrecondition(aProblemDescription) == false) {
         	setStatus(SearchStatus.ERROR);
         	return status;
         }
 
         //Initialization steps. At this point, nextLevel = 1. Thus, the root level is 1
         setCurrentNorm(netRoot);
         setNextLevel();
 
         //Move to the next state, and return its returning value (nil or self)
         searchPossibleSolutions(aProblemDescription);
         
         if (getPossibleSolutions().isEmpty()) {
         	prepareFailedOutput();
         } else {
 	        //this.compressPossibleSolutions();
 	        prepareSuccessfulOutput();
         }
         
         return status;
     }
     
     /**
      * The argument aProblemDescription is scanned in search for descriptors that correspond to indices
      * pointing to norms. Every time one such descriptor is located, currNorm is reassigned and the
      * descriptor placed in the solution description.  If after the process the argument list is empty, a
      * call to state retrieveCasesUnderNorm is performed.  If the list is completely scanned and no
      * descriptor was located, a call to state searchForCasesWith is executed.
      * NOTE: The argument aProblemDescription is a list of Descriptors
      * Automaton reference: SNW
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return self, or object returned by retrieveCasesUnderCurrNorm, or object returned by
 	 * searchForCasesUsing:
 	 */
     public void searchPossibleSolutions(Description aProblemDescription){
         //If the problem description is empty, all its descriptors matched norms. Make the current norm
         //the stop-level norm, and start the search for cases under it
         if (aProblemDescription.getDescriptors().isEmpty()){
             setStopLevel(currentLevel);
             retrieveCasesUnderCurrentNorm();
             return;
         }
         
         //Scan the problem description
         int i = 0;
         Descriptor d = null;
         Norm nextNorm = null;
         while (i < aProblemDescription.getDescriptors().size()){
             //Search for a norm whose descriptor matches the scanned descriptor. If found,
             //remove the descriptor from the problem case and stop the loop
             nextNorm = currentNorm.getNearestSuccessorNorm((Descriptor)aProblemDescription.getDescriptors().get(i));
 
             if (nextNorm == null){
                 i += 1;
             }else{
                 d = (Descriptor) aProblemDescription.getDescriptors().remove(i);
                 i = aProblemDescription.getDescriptors().size() + 1;
             }
         }
         //if no descriptor available, the entire list was scanned and no norm was found.  Start the search
         //for indices (using the remaining descriptors) that point to cases
         if (d == null){
             setStopLevel(currentLevel);
             searchPossibleSolutionsUsingIndices(aProblemDescription);
             return;
         }
         
         //A norm with a matching descriptor was found.  Add the removed descriptor to the solution
         //list, and move on to the next (recursive) norm search
         updateNormSearch(d,nextNorm);
         searchPossibleSolutions(aProblemDescription);
         return;
     }
     
     /**
      * This method attempts to retrieve norm-dependant cases, from the stop-level norm. At this point, the
      * problem description is empty of descriptors.  If at least one case was retrieved, prepare the
      * successful output and stop the automaton. Else, start a dialog with the user.
      * Automaton reference: RCUN
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return object returned by either prepareSuccessfulOutputWith: (self), or indexDialog.
 	 */
     public void retrieveCasesUnderCurrentNorm(){
         List<SheetCase> caseList = currentNorm.successorCases();
         
         if (caseList.isEmpty() != true) {
         	List<PossibleSolution> ps = associateCasesToPossibleSolutions(caseList);
 			
 			while(ps.isEmpty() != true)
                 addToPossibleSolutions(ps.remove(0));        
         } else {
         	if (OracleIDSystem.getInstance().isInteractive())
         		indexDialog();
         }
     }
     
     /**
 	 * @see Define method name.
 	 * @param my parameters list
 	 * @return my return values
 	 */
     public void updateNormSearch(Descriptor aSAVDescriptor, Norm aNewNorm){
         addToSolutionDescription(aSAVDescriptor);
         currentNorm = aNewNorm;
         setNextLevel();
     }
     
     /**
      * 
      * @param value
      * @param aCaseList
      * @return
      */
 	SheetCase getSheetCase(Descriptor descriptor, List<SheetCase> aCaseList) {
 		
     	for (SheetCase sc:aCaseList) {
     		if (sc.getDescriptor().equals(descriptor))
         				return sc;
     	}
     	
     	return null;
     }
 }
