 import java.util.*;
 
 public class Proof {
 	private String var = "1";
 	private LineNumber start = new LineNumber(var);
     private LinkedList<Bundle> truths;
     private Bundle lastShow; //reassign tail in globalTruth to this after completing shows
     private ArrayList<Bundle> allStatements;
     private final int numberOfUserTheorems;
     
     private boolean extendBlock = false; 
     private boolean notmatch = false;
 
     public Proof (TheoremSet theorems) {
         if (theorems.getMyTheorems() == null){
             truths = new LinkedList<Bundle>();
             numberOfUserTheorems = 0;
         } else {
             truths = theorems.getMyTheorems();
 //            System.out.println(truths);
             numberOfUserTheorems = truths.size();
         }
         lastShow = null;
         allStatements = new ArrayList<Bundle>();
     }
 
     public Proof (){
         this(null);
     }
     public LineNumber nextLineNumber ( ) {
 //    	start.addLine(extendBlock, notmatch);
 //    	System.out.print(start);
     	return start;
         //return null;
     }
     
     public LinkedList<Bundle> getTruths(){
         return truths;
     }
     public Bundle getLastShow(){
         return lastShow;
     }
     public void extendProof (String x) throws IllegalLineException, IllegalInferenceException{
         String[] inputs = x.split("\\s+");
         if (inputs.length > 4){
             throw new IllegalLineException("Input has too many fields");
         }
         if (inputs.length == 0) {
         	throw new IllegalLineException("Must have input");
         }
 
         if (inputs.length == 1){
            if (inputs[0].equals("print")) {
                print();
            } else {
                throw new IllegalLineException("The only legal one field input is \"print\"");
            }
         }
 
         String reason = inputs[0];
         Bundle checkBundle = null;
 
         if (inputs.length == 2){
             String expression = inputs[1];
             if (reason.equals("show")){
             	extendBlock = true;
             	
                 checkBundle = new Bundle(var,makeTree(expression),reason);
                 System.out.println(checkBundle);
                 var = start.addLine(true, false); //addline is working, not 
 //                System.out.println(var);
 //                System.out.println(start + "asdf");
                 return;
             }
             if (reason.equals("assume")) {
                 checkBundle = new Bundle(var,makeTree(expression),reason);
                 notmatch = true;
                 var = start.addLine(false, true);
 //                System.out.println(var);
                 return;
             }
             Bundle testBundle = findUserTheorem(reason);
             if (testBundle != null){
             	if (makeTree(expression).equals(lastShow.getMyTree())) {
             		checkBundle = new Bundle(var,makeTree(expression),reason);
             		var = start.addLine(false, true);
             	} else {
             		checkBundle = new Bundle(var,makeTree(expression),reason);
             	}	var = start.addLine(false, false);
             } else {
                 if (reason.equals("co")||
                         reason.equals("ic")||
                         reason.equals("mt")||
                         reason.equals("mp")||
                         reason.equals("repeat")){
                     throw new IllegalLineException(reason + " takes at least one line reference");
                 } else {
                 throw new IllegalLineException(reason + " is not a valid reason");
                 }
             }
         }
 
         if(inputs.length == 3){
             String expression = inputs[2];
             String refLine1 = inputs[1];
             if (reason.equals("repeat")){
             	if (makeTree(expression).equals(lastShow.getMyTree())) {
             		checkBundle = new Bundle(var,makeTree(expression),reason,refLine1);
             		var = start.addLine(false, true);
             	} else {
             		checkBundle = new Bundle(var,makeTree(expression),reason,refLine1);
             		var = start.addLine(false, false);
             	}
                 return;
             }
             if (reason.equals("ic")){
             	if (makeTree(expression).equals(lastShow.getMyTree())) {
             		checkBundle = new Bundle(var,makeTree(expression),reason,refLine1);
             		var = start.addLine(false, true);
             	}
                 checkBundle = new Bundle(var,makeTree(expression),reason,refLine1);
                 var = start.addLine(false, false);
                 return;
             }
             Bundle userBundle = findUserTheorem(reason);
             if (userBundle != null ||
                     reason.equals("mt")||
                     reason.equals("mp") ||
                     reason.equals("co")) {
                     throw new IllegalLineException(reason + " does not accept a single line reference");
             } else {
                 throw new IllegalLineException(reason + " is not a valid reason");
             }
         }
         try {
             checkLine(checkBundle);
         } catch (Exception e){
             System.err.println("NOOOOOOOOOOOOOOOOOOO");
         }
 	}
 
     /**
      * Takes in the String form of the expression and outputs the Binary Tree form
      * If the expression is erroneous in any way, this is where exceptions will be
      * thrown.
      * @param expression
      * @return BinaryTree
      * @throws IllegalLineException
      */
     private BinaryTree makeTree(String expression) throws  IllegalLineException{
         //
     	return BinaryTree.exprTree(expression);
         //return null;  //To change body of created methods use File | Settings | File Templates.
     }
 
     /**
      * A call to this will tell you what the next valid line number is.
      * It views the most recent truth. Whether or not this truth is labelled
      * "true" in the theorem name determines if the block should be back indednted.
      *
      * @param extendBlock
      * @return String
      */
 
 
     public String toString ( ) {
         return "";
     }
 
     public boolean isComplete( ) {
     	if (truths.size() - numberOfUserTheorems == 1){
             if (truths.get(numberOfUserTheorems).getThrmName().equals("true")){
                 return true;
             } else {
                 System.err.println("AHHHHHHHHHHHHHHHHHHH!");
                 return false;
             }
         } else {
             return false;
         }
 
     }
 
     /**
      * Called when a show is struck, should be the first line of the proof.
      * These show statements must be included in the truths list. After each
      * interior show statement is proven the lastProofInScope field will be
      * updated to that show statement, because the level above is now allowed to
      * use that show statement as a truth.
      */
     public void show(Bundle showBundle) throws IllegalLineException{
         if (showBundle.getMyTree() == null) {
             throw new IllegalLineException("ERROR: Bundle cannot contain a null tree");
         }
         lastShow = showBundle;
         allStatements.add(lastShow);
         truths.add(lastShow); // updates the latest scope.
     }
 
     /**
      * Any assumption can be immediately added to the list of truths
      */
     public void assume(Bundle assumeBundle) throws Exception{
         BinaryTree assumeTree = assumeBundle.getMyTree();
         Bundle lastShow = null;
         BinaryTree lastShowTree = null;
 
         try{
             lastShow  = (Bundle)truths.get(truths.size()-1);
             lastShowTree = lastShow.getMyTree();
         } catch (NullPointerException n){
             System.err.println("ERROR: List has failed to call the last object");
         } catch (ClassCastException c){
             System.err.println("ERROR: The truth list is internally inconsistent.");
         }
         if (lastShow.getThrmName().equals("show")){
             BinaryTree notTree;
             BinaryTree leftTree;
             BinaryTree.TreeNode leftNode = lastShowTree.getMyLeft();
             BinaryTree.TreeNode leftLeftNode = leftNode.getMyLeft();
             BinaryTree.TreeNode rightLeftNode = leftNode.getMyRight();
             notTree = new BinaryTree(new BinaryTree.TreeNode("~",lastShowTree.getMyRoot(),null));
             leftTree = new BinaryTree(new BinaryTree.TreeNode(leftNode.getMyItem(),leftLeftNode,rightLeftNode));
             if (assumeTree.equals(notTree) || assumeTree.equals(leftTree)){
                 allStatements.add(assumeBundle);
                 truths.add(assumeBundle);
             } else {
                 throw new IllegalInferenceException("Invalid assumption code 2");
             }
         } else {
             throw new IllegalInferenceException("Invalid assumption code 1");
         }
 
     }
 
     public void repeat(Bundle repeatBundle) throws IllegalLineException{
         if (truths.contains(repeatBundle)){
             Bundle oldBundle = truths.get(truths.indexOf(repeatBundle));
             if (oldBundle.getLineNumber().equals( repeatBundle.getRefLine1())){
                 truths.remove(repeatBundle);
                 allStatements.add(repeatBundle);
                 truths.add(repeatBundle);
             } else {
                 throw new IllegalLineException("Error: The repeat reference line did not match the expression at that line.");
             }
         } else {
             throw new IllegalLineException("Error: Repeat is only legal if the expression already exists");
         }
     }
 
     public void IC(Bundle ICBundle) throws IllegalLineException,IllegalInferenceException{
         BinaryTree expressionToProve = ICBundle.getMyTree();
         String lineReference = ICBundle.getRefLine1();
         BinaryTree rightTree = new BinaryTree(expressionToProve.getMyRight());
         Bundle rightICBundle = new Bundle(ICBundle.getLineNumber(),rightTree,ICBundle.getThrmName(),ICBundle.getRefLine1());
         if (expressionToProve.getMyRoot().getMyItem().equals("=>")){      //The expression must have an implication
             if (truths.contains(rightICBundle)){            // Checks if this expression is in the list of truths and shows
                 Bundle referencedBundle = truths.get(truths.indexOf(rightICBundle));
                 if (!referencedBundle.getThrmName().equals("show")){    //Checks if you are trying to use an unporven statement
                     if (lineReference.equals(referencedBundle.getLineNumber())){   //Checks if you entered the wrong line
                         allStatements.add(ICBundle);
                         if (ICBundle.equals(lastShow)){
                             changeTruths(); //If you have proven the most recent show.
                         } else {
                             truths.add(ICBundle); // If you have proven something that isn't the most recent show
                         }
                     } else {
                         throw new IllegalInferenceException("Line number does not reference the \n right-hand side  of the implication");
                     }
                 } else {
                     throw new IllegalInferenceException("ic: referenced line can't be an unproven show");
                 }
             } else {
                 throw new IllegalInferenceException("Right-hand side must be proven already");
             }
         } else {
             throw new IllegalInferenceException("IC takes an implication");
         }
     }
 
 
     public void MT(Bundle mtBundle) throws IllegalInferenceException, IllegalLineException {
         Bundle firstReference = findReferences(mtBundle.getRefLine1());
         Bundle secondReference = findReferences(mtBundle.getRefLine2());
 
         //Testing that the references are already proved statements
         if (firstReference == null || secondReference == null){
             throw new IllegalInferenceException("mt: The lines referenced must already be proven");
         }
 
 
         //Testing that one of the tree roots for the reference arguments
         // is an implication;
 
         Bundle[] bothBundles = testRootDesired(firstReference, secondReference, "mt", "=>");
         Bundle implicationBundle = bothBundles[0];
         Bundle notRightArgumentBundle= bothBundles[1];
         if (implicationBundle == null || notRightArgumentBundle == null){
             throw new IllegalLineException("Inconsistency in checking for implication");
         }
 
         //The meat of the MT argument.
 
         BinaryTree implicationBranchLeft = new BinaryTree(implicationBundle.getMyTree().getMyLeft());
         BinaryTree implicationBranchRight = new BinaryTree(implicationBundle.getMyTree().getMyRight());
         BinaryTree notImplicationBranchRight = new BinaryTree(new BinaryTree.TreeNode("~",implicationBranchRight.getMyRoot(),null));
         Bundle notImplicationBranchLeft = new Bundle(implicationBundle.getLineNumber(),
                 new BinaryTree(new BinaryTree.TreeNode("~",implicationBranchLeft.getMyRoot(),null)),
                 implicationBundle.getThrmName());
         if(notImplicationBranchRight.equals(notRightArgumentBundle)){
             if(notImplicationBranchLeft.equals(mtBundle.getMyTree())){
                 allStatements.add(mtBundle);
                 if (mtBundle.equals(lastShow)){
                     changeTruths();
                 } else {
                     truths.add(mtBundle);
                 }
             } else {
                 throw new IllegalInferenceException("mt: the expression must be the negation of the \n" +
                         "left argument of the referenced implication of the referenced implication");
             }
         } else {
             throw new IllegalInferenceException("mt: the second proposition referenced is not the negation of the " +
                     "\n left argument of the implication referenced");
         }
     }
 
     public void CO(Bundle coBundle) throws IllegalInferenceException, IllegalLineException{
         Bundle firstReference = findReferences(coBundle.getRefLine1());
         Bundle secondReference = findReferences(coBundle.getRefLine2());
 
         //Testing that the references are already proved statements
         if (firstReference == null || secondReference == null){
             throw new IllegalInferenceException("co: The lines referenced must already be proven or assumed");
         }
 
         // Discovering which proposition referenced has a not symbol
         Bundle[] bothBundles = testRootDesired(firstReference,secondReference,"co","~");
         Bundle notPropositionBundle = bothBundles[0];
         Bundle propositionBundle = bothBundles[1];
         if (notPropositionBundle == null || propositionBundle == null){
             throw new IllegalLineException("Inconsistency in checking for not operator");
         }
 
 
         //The meat of the CO argument.
 
         BinaryTree newNotTree = new BinaryTree(new BinaryTree.TreeNode("~",propositionBundle.getMyTree().getMyRoot(),null));
         Bundle newNotPropositionBundle = new Bundle (
                 propositionBundle.getLineNumber(),
                 newNotTree,
                 propositionBundle.getThrmName()
                 );
         if (newNotPropositionBundle.equals(notPropositionBundle)){
             if(newNotPropositionBundle.equals(coBundle) || notPropositionBundle.equals(coBundle)){
                 allStatements.add(coBundle);
                 if (coBundle.equals(lastShow)){
                        changeTruths();
                 }else {
                        truths.add(coBundle);
                     }
             } else {
                 throw new IllegalInferenceException("One of the lines referenced must be equivalent to the expression");
             }
         }   else {
             throw new IllegalInferenceException("The first line referenced by CO must be the negation of \n" +
                     "the second line number referenced");
         }
     }
 
     public Bundle[] testRootDesired(Bundle firstRef, Bundle secondRef, String inferenceType, String rootDesired) throws IllegalInferenceException {
         Bundle[] rtn = new Bundle[2];
         if (!firstRef.getMyTree().getMyRoot().getMyItem().equals(rootDesired)
                 && !secondRef.getMyTree().getMyRoot().getMyItem().equals(rootDesired)){
             throw new IllegalInferenceException(inferenceType + " must reference at least one tree rooted in " + rootDesired);
         } else if ( firstRef.getMyTree().getMyRoot().getMyItem().equals(rootDesired)){
             rtn[0] = firstRef;
             rtn[1] = secondRef;
         } else {
             rtn[1] = firstRef;
             rtn[0] = secondRef;
         }
         return rtn;
     }
 
     public Bundle findReferences(String refLine){
         Bundle rtnBundle = null;
         for (Bundle truth:truths) {
             if (truth.getLineNumber().equals(refLine) && !truth.getThrmName().equals("show")){
                 rtnBundle = truth;
             } else {
                 continue;
             }
         }
         return rtnBundle;
     }
 
     public Bundle findUserTheorem(String thrmName){
         Bundle rtnBundle = null;
         for (Bundle truth: truths) {
             if (truth.getThrmName().equals(thrmName) && truth.getLineNumber()==null){
                 rtnBundle = truth;
             } else {
                 continue;
             }
         }
         return rtnBundle;
     }
 
 
     public void MP(Bundle mpBundle) throws IllegalInferenceException,IllegalLineException {
         Bundle firstReference = findReferences(mpBundle.getRefLine1());
         Bundle secondReference = findReferences(mpBundle.getRefLine2());
 
         //Testing that the references are already proved statements
         if (firstReference == null || secondReference == null){
             throw new IllegalInferenceException("mp: The lines referenced must already be proven");
         }
 
         //Testing that one of the tree roots for the reference arguments
         // is an implication;
         Bundle[] bothBundles = testRootDesired(firstReference,secondReference,"mp","=>");
         Bundle implicationBundle = bothBundles[0];
         Bundle leftArgumentBundle = bothBundles[1];
 
         //The meat of the MP argument.
         BinaryTree implicationBranchLeft = new BinaryTree(implicationBundle.getMyTree().getMyLeft());
         BinaryTree implicationBranchRight = new BinaryTree(implicationBundle.getMyTree().getMyRight());
         if(implicationBranchLeft.equals(leftArgumentBundle.getMyTree())){
             if(implicationBranchRight.equals(mpBundle.getMyTree())){
                     allStatements.add(mpBundle);
                 if (mpBundle.equals(lastShow)){
 
                     changeTruths();
                 } else {
                     truths.add(mpBundle);
                 }
             } else {
                 throw new IllegalInferenceException("mp: the expression must be the right argument \n" +
                         "of the referenced implication");
             }
         } else {
             throw new IllegalInferenceException("mp: the second reference is not the left argument " +
                     "\n of the implication");
         }
 
     }
 
     public void changeTruths(){
         Iterator<Bundle> iter = truths.iterator();
         Bundle lastShow = null;
         while (iter.hasNext()){
             Bundle nxtBundle = iter.next();
             if (nxtBundle.getThrmName().equals("show")){
                 lastShow = nxtBundle;
             }
         }
         if (lastShow == null){
             System.err.println("ERROR: truths is internally inconsistent");
         } else {
             int lastIndex = truths.lastIndexOf(lastShow);
             lastShow = new Bundle(lastShow.getLineNumber(),lastShow.getMyTree(),"true");
             int truthsSize = truths.size();
             truths.add(lastIndex, lastShow);
             for (int i = lastIndex+1; i < truthsSize+1; i++){
                 truths.removeLast();
             }
         }
         iter = truths.iterator();
         lastShow = null;
         while (iter.hasNext()){
             Bundle nxtBundle = iter.next();
             if (nxtBundle.getThrmName().equals("show")){
                 lastShow = nxtBundle;
             }
         }
 
         this.lastShow = lastShow;  //it is now not technically a show, but a "true"
 
     }
 
     public void print() {
         Iterator<Bundle> iter = allStatements.iterator();
         while (iter.hasNext()){
             Bundle bundle = iter.next();
             String outString = "";
             outString += bundle.getLineNumber() + " ";
             outString += bundle.getThrmName()  + " ";
             if (bundle.getRefLine1() != null) {
                 outString += bundle.getRefLine1() + " ";
             }
             if (bundle.getRefLine2() != null){
                 outString += bundle.getRefLine2()+ " ";
             }
             outString += bundle.getMyTree().inOrderString() + " ";
             outString += "\n";
             System.out.println(outString);
         }
     }
 
     /**
      * Takes in a check Bundle and asks if it is consistent
      * within the defined scope (defined by lastTruthInScope)
      *
      * Set all methods called in checkline to private when you
      * get a chance
      */
     public void checkLine(Bundle checkBundle) throws Exception{
 
         String thrmName = checkBundle.getThrmName();
         //Purposely avoided using switch because the
         //java version of the testing systems was not
         //given.
         if (thrmName.equals("show")){
             show(checkBundle);
         } else if (thrmName.equals("assume")){
             assume(checkBundle);
         } else if (thrmName.equals("ic")){
             IC(checkBundle);
         } else if (thrmName.equals("repeat")) {
             repeat(checkBundle);
         } else if (thrmName.equals("co")){
             CO(checkBundle);
         } else if (thrmName.equals("mt")){
             MT(checkBundle);
         } else if (thrmName.equals("mp")){
             MP(checkBundle);
         } else {
             Bundle thrmBundle = findUserTheorem(thrmName);
             if (thrmBundle == null){
                 throw new IllegalLineException("Theorem name is invalid");
             } else {
                 userTheorems(thrmBundle,checkBundle);
             }
         }
         isComplete();
 
     }
 
     public void userTheorems(Bundle thrmBundle, Bundle checkBundle) throws
                                                                         Exception,
                                                                         IllegalInferenceException,
                                                                         IllegalLineException{
                                                                         	
                                                                         	
         if (thrmBundle == null || checkBundle == null){
             throw new Exception("Error: Inconsistency in processing user input line");
         }
 
         if (thrmBundle.equals(checkBundle)) {//checks trees within each Bundle for equivalence
         //I think this is wrong. Need to pattern match--that is, call CANBEMATCHED 
             allStatements.add(checkBundle);
             if (checkBundle.equals(lastShow)){
                 changeTruths();
             } else {
                 truths.add(checkBundle);
             }
         } else {
             throw new IllegalInferenceException("Expression given must be equivalent \n" +
                     "to the theorem's expression.");
         }
     }
 
 
 }
