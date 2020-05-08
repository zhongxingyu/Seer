 package blast;
 
 import BLAST.NCBI.output.Hit;
 import BLAST.NCBI.output.Iteration;
 import format.BadFromatException;
 import helper.Ranks;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * //TODO: document
  */
 public class NormalyzedIteration {
 
     protected final BLAST_Identifier blastIdentifier;
     protected final Iteration iteration;
     protected List<NormalizedHit> normalizedHits;
     protected final int queryLength;
 
     /**
      * Current level of specification
      */
     protected Ranks currentRank;
     /**
      * A current candidate NormalizedHit that may become the pivotal one
      */
     protected NormalizedHit pivotalHit;
 
     public NormalyzedIteration(Iteration iteration, BLAST_Identifier blastIdentifier) {
         this.iteration = iteration;
         this.blastIdentifier = blastIdentifier;
         this.queryLength = Integer.parseInt(this.iteration.getIterationQueryLen());
     }
 
     protected void normalyzeHits() throws SQLException, BadFromatException {
         //Check if the costly procedure of Hits normalization has already been performed
         if (this.normalizedHits == null) {
             //If not yet - create a new list of the size of the list of hits
             this.normalizedHits = new ArrayList<NormalizedHit>(this.iteration.getIterationHits().getHit().size());
             //For every hit on the hit list
             for (Hit hit : this.iteration.getIterationHits().getHit()) {
                 //Create a normalized version and store in the newly created list
                 NormalizedHit normalizedHit = this.blastIdentifier.assignTaxonomy(NormalizedHit.newDefaultInstance(hit, this.queryLength));
                 //The hit may be returned as null upon errors and inability of the blastIdentifier module to process the request
                 if (normalizedHit != null) {
                     this.normalizedHits.add(normalizedHit);
                 }
             }
         }
     }
 
     protected void findLowestRank() {
 
         Ranks lowestRank = Ranks.root_of_life;
         for (NormalizedHit normalizedHit : this.normalizedHits) {
             if (normalizedHit.getAssignedRank().ordinal() > lowestRank.ordinal()) {
                 this.currentRank = normalizedHit.getAssignedRank();
             }
         }
     }
 
     protected boolean couldLiftCurrentRank() {
         if (this.currentRank != Ranks.superkingdom) {
             this.currentRank = Ranks.previous(this.currentRank);
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Creates a list of hits that display GIs that point to the current taxonomic rank
      *
      * @return {@link List<NormalizedHit>} of normalized hits at current taxonomic rank
      */
     protected List<NormalizedHit> gatherHitsAtCurrentRank() {
         //First - count how many hits with this rank exist
         int numberOfHitsThatQualify = 0;
         for (NormalizedHit normalizedHit : this.normalizedHits) {
             if (normalizedHit.getAssignedRank().equals(this.currentRank)) {
                 numberOfHitsThatQualify++;
             }
         }
         //Knowing the exact number - create a list of exactly the needed size
         if (numberOfHitsThatQualify > 0) {
             List<NormalizedHit> normalizedHitsAtCurrentRank = new ArrayList<NormalizedHit>(numberOfHitsThatQualify);
             for (NormalizedHit normalizedHit : this.normalizedHits) {
                 //and then put all matching normalized hits into the list
                 if (normalizedHit.getAssignedRank().equals(this.currentRank)) {
                     normalizedHitsAtCurrentRank.add(normalizedHit);
                 }
             }
             return normalizedHitsAtCurrentRank;
         } else {
             return null;
         }
     }
 
     /**
      * Ensures that all of the hits from a given {@link List<NormalizedHit>} pass the cutoffs threshold at the current level of taxonomic identification.
      *
      * @param normalizedHitsUnderTest{@link List<NormalizedHit>} of normalized hits to test
      * @return {@link List<NormalizedHit>} of only those normalized hits that have passed, {@link null} in case none of the normalized hits passed the cutoffs,
      *         if a {@link null} or an empty list was passed as a parameter.
      * @throws SQLException in case a communication error occurs during the database interaction
      */
     protected List<NormalizedHit> ensureNormalyzedHitsPassCutoffsAtCurrentRank(List<NormalizedHit> normalizedHitsUnderTest) throws SQLException {
         //First check the incoming list for null and for that at least one normalized hit exists there
         if (normalizedHitsUnderTest != null && normalizedHitsUnderTest.size() > 0) {
             //Create a new list to hold those hits that have passed the cutoffs assuming in an optimistic way that all of the
             //hits will pass and a list of the same size will be needed
             List<NormalizedHit> esuredNormalizedHits = new ArrayList<NormalizedHit>(normalizedHitsUnderTest.size());
             for (NormalizedHit normalizedHit : normalizedHitsUnderTest) {
                 if (this.blastIdentifier.normalyzedHitChecksAgainstParametersForRank(normalizedHit, this.currentRank)) {
                     esuredNormalizedHits.add(normalizedHit);
                 } else {
                     //If the hit does not check, it should be identified at a higher taxonomic level in the next round (if such occurs)
                     this.blastIdentifier.liftRankForNormalyzedHit(normalizedHit);
                 }
             }
             if (esuredNormalizedHits.size() > 0) {
                 return esuredNormalizedHits;
             } else {
                 return null;
             }
 
         } else {
             return null;
         }
     }
 
     /**
      * Assembles a list of normalized hits that have better E-values than the potential pivotal hit.
      *
      * @return {@link List<NormalizedHit>} of normalized hits that have better E-values than the potential pivotal hit.
      *         {@link null} in case the current potential pivotal hit was the first one on the list, or if the potential pivotal hit
      *         has not been assigned yet
      */
     protected List<NormalizedHit> getNormalyzedHitsWithBetterEvalue() {
         //Check if any potential pivotal hit has been assigned
         if (this.pivotalHit != null) {
             //Prepare a new list to store the normalized hits that have better E-values than the potential pivotal hit.
             List<NormalizedHit> normalizedHitsWithBetterEvalue = new ArrayList<NormalizedHit>();
             //Add hits until the potential pivotal has been found
             for (NormalizedHit normalizedHit : this.normalizedHits) {
                 if (!normalizedHit.equals(this.pivotalHit)) {
                     normalizedHitsWithBetterEvalue.add(normalizedHit);
                 } else {
                     break;
                 }
             }
             //If the potential pivotal hit was the first one on the list - just return null
             if (normalizedHitsWithBetterEvalue.size() > 0) {
                 return normalizedHitsWithBetterEvalue;
             } else {
                 return null;
             }
 
         } else {
             return null;
         }
 
     }
 
     /**
      * Checks whether a sublist of normalized hits that possess a better E-value than the current potential pivotal hit
      * allow the assignment of the pivotal hit. If any of the hits with better E-value point to a taxonomic group that does not
      * contain a subnode of the potential pivotal hit, than such a normalized hit "contradicts" the assignment of the potential
      * pivotal hit
      *
      * @return {@link true} if the normalized hits with better E-value allow the assignment of the pivotal,
      *         {@link false} otherwise
      * @throws SQLException in case something goes wrong during the database communication
      */
     protected boolean normalyzedHitsWithBetterEvalueAllowPivotal() throws SQLException {
         //Prepare a list of hits with better E-value than the current potential pivotal hit
         List<NormalizedHit> normalizedHitsWithBetterEvalue = this.getNormalyzedHitsWithBetterEvalue();
         if (normalizedHitsWithBetterEvalue != null) {
             for (NormalizedHit normalizedHit : normalizedHitsWithBetterEvalue) {
                 //Assign taxonomy down to the leaves for each hit on the list
                normalizedHit = this.blastIdentifier.assignTaxonomy(normalizedHit);
                 //Check if the any of the hits point to a taxonomic node that is (despite being higher ranked then the pivotal hit)
                 //different form that to which the pivotal hit points
                 if (normalizedHit.refusesParenthood(this.pivotalHit)) {
                     return false;
                 }
             }
         } else {
             return false;
         }
         return true;
     }
 
     /**
      * Attempts to set a potential pivotal hit at current taxonomic level.
      *
      * @return {@link true} in case succeeds, {@link false} in case was not able to
      *         set any hit as pivotal at the current level of specification
      * @throws SQLException in case something goes wrong during the database communication
      */
     protected boolean couldSetPivotalHitAtCurrentRank() throws SQLException {
         //Prepare a list of normalized hits that have been checked against the cutoffs at current rank
         List<NormalizedHit> normalizedHitsAtCurrentRank = this.ensureNormalyzedHitsPassCutoffsAtCurrentRank(this.gatherHitsAtCurrentRank());
         if (normalizedHitsAtCurrentRank != null) {
             //If any normalized hits exist on the list - set the firs one as pivotal
             this.pivotalHit = normalizedHitsAtCurrentRank.get(0);
             return true;
         } else {
             //Try lifting one step the current rank
             if (this.couldLiftCurrentRank()) {
                 //Retry to set potential pivotal hit
                 return this.couldSetPivotalHitAtCurrentRank();
             }
             return false;
         }
     }
 
     protected boolean normalyzedHitsWithWorseEvalueAllowPivotal() {
 
         //Go through the hits that have worse E-value than the pivotal hit
         if(this.pivotalHit!=null){
             for(int i=this.normalizedHits.indexOf(this.pivotalHit)+1;i<this.normalizedHits.size();i++){
                 NormalizedHit normalizedHit=this.normalizedHits.get(i);
                 //If the next hit has current rank and points to a different taxonomic node
                 if(normalizedHit.getAssignedRank()==this.currentRank){
                     //if the E-value difference (in folds) between the next hit and the current pivotal
                     //is less then the threshold cutoff - do not allow the pivotal hit
                     if(this.blastIdentifier.hitsAreFarEnoughByEvalueAtRank(normalizedHit, this.pivotalHit,this.currentRank)){
                         return true;
                     }
                 }
             }
             return true;
         } else{
             return false;
         }
     }
 
     protected void specify() throws SQLException, BadFromatException {
 
         this.normalyzeHits();
         this.findLowestRank();
 
         while (couldSetPivotalHitAtCurrentRank()) {
             if (normalyzedHitsWithBetterEvalueAllowPivotal() && normalyzedHitsWithWorseEvalueAllowPivotal()) {
                 //success
                 break;
             }
         }
         //fail
     }
 
 }
