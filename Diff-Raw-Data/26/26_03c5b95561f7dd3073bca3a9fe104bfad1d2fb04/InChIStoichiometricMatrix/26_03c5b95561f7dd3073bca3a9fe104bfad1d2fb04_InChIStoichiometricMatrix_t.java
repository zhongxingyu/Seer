 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.ebi.metabolomes.core.reaction.matrix;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 import uk.ac.ebi.metabolomes.core.reaction.BiochemicalReaction;
 import uk.ac.ebi.metabolomes.identifier.ECNumber;
 import uk.ac.ebi.metabolomes.identifier.InChI;
 
 /**
  * InChIStoichiometricMatrix.java
  *
  *
  * @author johnmay
  * @date Apr 6, 2011
  */
 public class InChIStoichiometricMatrix
         extends StoichiometricMatrix<InChI , ECNumber> {
 
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            InChIStoichiometricMatrix.class );
 
     public InChIStoichiometricMatrix() {
     }
 
     public InChIStoichiometricMatrix( int n , int m ) {
         super( n , m );
     }
 
    public boolean addReaction( ECNumber ecNumber , BiochemicalReaction reaction ) {
 
         if ( reaction == null ) {
             logger.error( "Skipping reaction for: " + ecNumber + " as reaction is null" );
            return false;
         }
 
         ArrayList<InChI> reactantInChIs = reaction.getInchiReactants();
         ArrayList<InChI> productInChIs = reaction.getInchiProducts();
 
 
         ArrayList<InChI> allCompounds = new ArrayList<InChI>();
         allCompounds.addAll( reactantInChIs );
         allCompounds.addAll( productInChIs );
 
         if ( allCompounds.size() == 0 ) {
            return false;
         }
 
         ArrayList<Double> reactantCoefficients = reaction.getInChIReactantCoefficients();
         ArrayList<Double> productCoefficients = reaction.getInChIProductCoefficients();
 
         for ( int i = 0; i < reactantCoefficients.size(); i++ ) {
             reactantCoefficients.set( i , -reactantCoefficients.get( i ) );
         }
 
         for ( int i = 0; i < productCoefficients.size(); i++ ) {
             productCoefficients.set( i , +productCoefficients.get( i ) );
         }
 
         ArrayList<Double> coefficientArrayList = reactantCoefficients;
         coefficientArrayList.addAll( productCoefficients );
 
 
         Double[] coefficients = coefficientArrayList.toArray( new Double[ 0 ] );
 
 
         // add to the matrix
         addReaction( ecNumber , allCompounds.toArray( new InChI[ 0 ] ) , coefficients );
        return true;
 
     }
 
     /**
      * Returns reactions with stochiometric < 0
      * @param metabolite
      * @return
      */
     public ArrayList<ECNumber> getReactionsProducingMolecule( InChI metabolite ) {
         ArrayList<ECNumber> productionReactions = new ArrayList<ECNumber>();
         Map<ECNumber , Double> reactions = getReactions( metabolite );
         for ( Entry<ECNumber , Double> e : reactions.entrySet() ) {
             if ( e.getValue() > 0.0 ) {
                 productionReactions.add( e.getKey() );
             }
         }
         return productionReactions;
     }
 
     /**
      * Returns reactions with stochiometric < 0
      * @param metabolite
      * @return
      */
     public ArrayList<ECNumber> getReactionsConsumingMolecule( InChI metabolite ) {
         ArrayList<ECNumber> productionReactions = new ArrayList<ECNumber>();
         Map<ECNumber , Double> reactions = getReactions( metabolite );
         for ( Entry<ECNumber , Double> e : reactions.entrySet() ) {
             if ( e.getValue() < 0.0 ) {
                 productionReactions.add( e.getKey() );
             }
         }
         return productionReactions;
     }
 }
