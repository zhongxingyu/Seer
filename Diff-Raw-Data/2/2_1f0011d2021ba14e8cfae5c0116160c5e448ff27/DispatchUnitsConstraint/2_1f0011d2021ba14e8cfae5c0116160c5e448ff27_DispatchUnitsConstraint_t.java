 package projectswop20102011.domain.validators;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import projectswop20102011.domain.Unit;
 
 /**
  * An abstract class representing a constraint to dispatch units.
  * @author Willem Van Onsem, Jonas Vanthornhout & Pieter-Jan Vuylsteke
  */
 public abstract class DispatchUnitsConstraint {
 
     /**
      * Checks if the given list of units passes this constraint and all units are relevant to this constraint.
      * @param units
 	 *		The list of units to check.
      * @pre All the elements in units are effective.
      * @return True if the given list passes the constraints and all the units are used, otherwise false.
      */
     public boolean areAllUnitsReleventAndConstraintPassed(Collection<Unit> units) {
         HashSet<Integer> relevantIndices = new HashSet<Integer>();
         if(areValidDispatchUnits(new ArrayList<Unit>(units), relevantIndices)) {
 			return false;
 		}
         for(int i = 0; i < units.size(); i++) {
             if(!relevantIndices.remove(i)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Tests if all the given units in the list are relevant for the constraint.
      * @param units
 	 *		The list of units to check.
      * @pre All the elements in units are effective.
      * @return True if all units are relevant for the constraint, otherwise false.
      */
     public boolean areAllUnitsRelevant(Collection<Unit> units) {
         HashSet<Integer> relevantIndices = new HashSet<Integer>();
         areValidDispatchUnits(new ArrayList<Unit>(units), relevantIndices);
         for(int i = 0; i < units.size(); i++) {
             if(!relevantIndices.remove(i)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks if this constraint passes by the given units (not all the units need te be relevant however).
      * @param units
 	 *		The list of units to check.
      * @pre All the units are effective.
      * @return True if this constraint passes by the given units.
      */
     public boolean areValidDispatchUnits(Collection<Unit> units) {
         return areValidDispatchUnits(new ArrayList<Unit>(units), new HashSet<Integer>());
     }
 
     /**
      * Tests if the given Iterable object of units could be allocated to the emergency where this DispatchUnitsConstraint is part of.
      * @param units
 	 *		An iterable object containing only unique and only effective units.
      * @param relevantUnitIndices
 	 *		A list of indices where the indices of the units are added to if they are relevant to met the contstraint.
      * @pre The given units parameter contains only effective units (duplicates are allowed).
      * @return True if this constraint passes with the given iterable of units, otherwise false.
      * @note The implementation of this method needs to be designed that the units in front of the units list will be added first to the relevantUnits.
      */
     public abstract boolean areValidDispatchUnits(List<Unit> units, Set<Integer> relevantUnitIndices);
 
     /**
      * Generates a proposal where all the elements of the fixed part are used, and where some of the variableParts are used. The proposal tries to pass the constraint, if that's impossible all Units that
      * @param fixedPart
 	 *		A list of units, where all the elements need to be used.
      * @param variablePart
 	 *		A list of units where some (or even all/none) units are selected.
      * @pre Both the fixed and the variable part don't contain ineffective elements.
      * @return If the fixed part is completly used a subset of the varialbePart containing a selection of units that are relevant fo the constraint. Although a combination of this selection and the fixed part doesn't need to pass the constraint. Otherwise an empty list.
      */
     public Set<Unit> generateProposal(List<Unit> fixedPart, List<Unit> variablePart) {
         int n = fixedPart.size();
         ArrayList<Unit> combinedParts = new ArrayList<Unit>(fixedPart);
         combinedParts.addAll(variablePart);
         HashSet<Integer> proposalIndices = new HashSet<Integer>();
         this.areValidDispatchUnits(combinedParts, proposalIndices);
 
         for(int i = 0; i < n; i++) {
            if (!proposalIndices.remove(i)) {
                 return new HashSet<Unit>();
             }
         }
 
         Set<Unit> proposal = new HashSet<Unit>();
         for(Integer i : proposalIndices) {
             proposal.add(combinedParts.get(i));
         }
 
         return proposal;
     }
 }
