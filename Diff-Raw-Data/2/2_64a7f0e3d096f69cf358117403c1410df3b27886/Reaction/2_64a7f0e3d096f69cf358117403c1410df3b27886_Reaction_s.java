 /**
  * Reaction.java
  *
  * 2011.08.08
  *
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.chemet.entities.reaction;
 
 import uk.ac.ebi.chemet.entities.reaction.participant.Participant;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.chemet.entities.reaction.participant.GenericParticipant;
 import uk.ac.ebi.metabolomes.core.ObjectDescriptor;
 
 /**
  * @name    Reaction
  *          2011.08.08
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  * @param <M> The molecule class type (IAtomContainer, String, etc. ) should overide hashCode/equals
  * @param <S> The stoichiometry class type (normally int or double) should overide hashCode/equals
  * @param <C> The compartment class type (can be a string e.g. [e] or Enumeration) overide hashCode/equals
  *          Generic reaction class allows the the specification of a reaction
  *          storing the molecule (M), stoichiometry (S) and compartment (C).
  *
  * Note that the molecule comparator is given in the constructor, this is because common molecule
  * object (i.e. IAtomContainer) does not implement comparable. We extends MetabolicReconstructionObject so
  * we can add annotations/observations to the reaction
  *
  */
 public class Reaction<M , S , C>
         extends ObjectDescriptor implements Serializable {
 
     private static final Logger LOGGER = Logger.getLogger( Reaction.class );
     private static final long serialVersionUID = 8309040049214143031L;
     transient protected List<M> reactantsMolecules = new ArrayList<M>();
     transient protected List<M> productsMolecules = new ArrayList<M>();
     transient protected List<S> reactantStoichiometries = new ArrayList<S>();
     transient protected List<S> productStoichiometries = new ArrayList<S>();
     transient protected List<C> reactantCompartments = new ArrayList<C>();
     transient protected List<C> productCompartments = new ArrayList<C>();
     // flags whether the reaction is generic or not
     transient protected Boolean generic = false;
     // new class
     protected List<Participant<M , S , C>> reactants;
     protected List<Participant<M , S , C>> products;
     // revesibility
     protected Reversibility reversibility = Reversibility.UNKNOWN;
     transient private Integer cachedHash = null;
 
     /**
      * Constructor for a generic reaction. The constructor must provide a comparator for
      * class of molecule used in the generic reaction. Ideally this should be a singleton class.
      * @param moleculeComparator
      */
     public Reaction() {
         reactants = new LinkedList<Participant<M , S , C>>();
         products = new LinkedList<Participant<M , S , C>>();
     }
 
     /**
      * Sets the reversibility of the reaction. By default the reaction reversibility is unknown.
      * The reversibility is not tested in the {@see equals(M} method as this method treats reactions
      * with same products (coefficients and compartments), different sides as being the same.
      * @param reversibility The reaction reversibility
      */
     public void setReversibility( Reversibility reversibility ) {
         this.reversibility = reversibility;
     }
 
     /**
      * Accessor for the reversibility of the reaction
      * @return Reaction reversibility enumeration
      */
     public Reversibility getReversibility() {
         return reversibility;
     }
 
     /**
      * Accessor for all the reactant compartments of the reaction
      * @return Fixed size array of reactant compartments
      */
     public C[] getReactantCompartments() {
         return ( C[] ) reactantCompartments.toArray( new Object[ 0 ] );
     }
 
     /**
      * Accessor for all the reactant coefficients of the reaction
      * @return Fixed size array of reactant coefficients
      */
     public S[] getReactantStoichiometries() {
         return ( S[] ) reactantStoichiometries.toArray( new Object[ 0 ] );
     }
 
     /**
      * Accessor for all the reactants of the reaction
      * @return Fixed size array of reactants of class 'M'
      */
     public M[] getReactantMolecules() {
         return ( M[] ) reactants.toArray( new Object[ 0 ] );
     }
 
     /**
      * Add a reactant (left side) to the reaction
      * @param product The reactant to add
      */
     public void addReactant( Participant<M , S , C> participant ) {
         this.reactants.add( participant );
         this.reactantsMolecules.add( participant.getMolecule() );
         this.reactantCompartments.add( participant.getCompartment() );
         this.reactantStoichiometries.add( participant.getCoefficient() );
         this.cachedHash = null;
     }
 
     /**
      * Accessor for all the product compartments of the reaction
      * @return Fixed size array of compartments
      */
     public C[] getProductCompartments() {
         return ( C[] ) productCompartments.toArray( new Object[ 0 ] );
     }
 
     /**
      * Accessor for all the product coefficients of the reaction
      * @return Fixed size array of coefficients
      */
     public S[] getProductStoichiometries() {
         return ( S[] ) productStoichiometries.toArray( new Object[ 0 ] );
     }
 
     /**
      * Accessor for all the products of the reaction
      * @return Fixed size array of products of class 'M'
      */
     public M[] getProductMolecules() {
         return ( M[] ) productsMolecules.toArray( new Object[ 0 ] );
     }
 
     /**
      * Add a product (right side) to the reaction
      * @param product The product to add
      */
     public void addProduct( Participant<M , S , C> participant ) {
 //        participants.add( new Participant<M , S , C>( product ) );
         this.products.add( participant );
         this.productsMolecules.add( participant.getMolecule() );
         this.productCompartments.add( participant.getCompartment() );
         this.productStoichiometries.add( participant.getCoefficient() );
         this.cachedHash = null;
     }
 
     /**
      * Accessor to all the reaction molecules
      * @return shallow copy combined list of all products (ordered reactant, product)
      */
     public M[] getAllReactionMolecules() {
         List<M> allMolecules = new ArrayList<M>( Arrays.asList( getReactantMolecules() ) );
         allMolecules.addAll( Arrays.asList( getProductMolecules() ) );
         return ( M[] ) allMolecules.toArray( new Object[ 0 ] );
     }
 
     /**
      * Accessor to all the reaction participants
      * @return shallow copy combined list of all products (ordered reactant, product)
      */
     public List<Participant> getAllReactionParticipants() {
         List<Participant> participants = new ArrayList<Participant>( reactants );
         participants.addAll( products );
         return participants;
     }
 
     /**
      * Accessor to all the reaction compartments
      * @return shallow copy combined list of all coefficients (ordered reactant, product)
      */
     public S[] getAllReactionCoefficients() {
         List<S> allMolecules = new ArrayList<S>( Arrays.asList( getReactantStoichiometries() ) );
         allMolecules.addAll( Arrays.asList( getProductStoichiometries() ) );
         return ( S[] ) allMolecules.toArray( new Object[ 0 ] );
     }
 
     /**
      * Accessor to all the reaction compartments
      * @return shallow copy combined list of all compartments (ordered reactant, product)
      */
     public C[] getAllReactionCompartments() {
         List<C> allMolecules = new ArrayList<C>( Arrays.asList( getReactantCompartments() ) );
         allMolecules.addAll( Arrays.asList( getProductCompartments() ) );
         return ( C[] ) allMolecules.toArray( new Object[ 0 ] );
     }
 
     /**
      * Accessor for the number of reactants
      * @return
      */
     public int getReactantCount() {
         return reactants.size();
     }
 
     /**
      * Accessor for the number of products
      * @return
      */
     public int getProductCount() {
         return products.size();
     }
 
     /**
      * Accessor for the coefficient of a specified reactant
      * @return
      */
     public S getReactantCoefficient( M m ) {
         return reactantStoichiometries.get( reactants.indexOf( m ) );
     }
 
     /**
      * Accessor for the coefficient of a specified product
      * @return
      */
     public S getProductCoefficient( M m ) {
         return productStoichiometries.get( products.indexOf( m ) );
     }
 
     public Boolean isGeneric() {
         return generic;
     }
 
     public void setGeneric( Boolean generic ) {
         this.generic = generic;
     }
 
     /**
      * Calculates the hash code for the reaction in it's current state. As the molecules need to be sorted
      * this operation is more expensive and thus non-optimal. The hash is therefore cached in a the variable
      * this.cachedHash which is 'nullified' if the state of the object changes.
      * Warning: This hash code will not persist between different virtual machines if enumerations are used. However
      * as the hash code is meant to be calculated when needed this should not be a problem
      * @return hash code the reaction, note – there is no guarantee of unique hash code and the equals method should
      *         be called if the the matching hashCodes are found
      */
     @Override
     public int hashCode() {
 
         int hash = 5;
 
         // (re)calculate the hash if needed (i.e. there has been a state-change)
         // Can't do this anymore as the internal workings of the Participants may change
         //   if ( this.cachedHash == null ) {
 
         List<Participant<M , S , C>> reac = new ArrayList<Participant<M , S , C>>( this.reactants );
         List<Participant<M , S , C>> prod = new ArrayList<Participant<M , S , C>>( this.products );
 
         // sort before calculating the hashCode()
         Collections.sort( reac );
         Collections.sort( prod );
 
         Integer[] hashes = new Integer[]{ reac.hashCode() ,
                                           prod.hashCode() };
 
         // sort them to be the same
         Arrays.sort( hashes );
         hash = 59 * hash + hashes[0];
         hash = 59 * hash + hashes[1];
 
         this.cachedHash = hash;
         // }
 
         return this.cachedHash;
     }
 
     /**
      * Check equality of reactions based on state. Reactions are considered equals if their reactants and products
      * (coefficients and compartments) are equals regardless of the side they reside on. For example A + B <-> C + D is
      * considered equals to C + D <-> A + B. The participant stoichiometric coefficients and compartments are also checked.
      *
      * To accomplish this the reactionParticipants and copied, sorted and then a hash for each side (four total) is made.
      * Duplicate hashes are then removed via the {@See Set} interface and the query (this) and the other (obj) sides are
      * tested for coefficient, compartment and finally molecule similarity
      *
      * @param obj The other object to test equality with
      * @return Whether the objects are considered equals
      */
     @Override
     public boolean equals( Object obj ) {
 
         if ( obj == null ) {
             return false;
         }
         if ( getClass() != obj.getClass() ) {
             return false;
         }
         final Reaction<M , S , C> other = ( Reaction<M , S , C> ) obj;
 
         /* Make shallow copies of compounds, coeffcients and compartments and sort */
         // query compounds
         List<Participant<M , S , C>> queryReactants = new ArrayList( this.reactants );
         List<Participant<M , S , C>> queryProducts = new ArrayList( this.products );
         Collections.sort( queryReactants );
         Collections.sort( queryProducts );
 
         // other compounds
         List<Participant<M , S , C>> otherReactants = new ArrayList( other.reactants );
         List<Participant<M , S , C>> otherProducts = new ArrayList( other.products );
         Collections.sort( otherReactants );
         Collections.sort( otherProducts );
 
         if ( this.generic == false && other.generic == false ) {
 
             /* calculate the hashes for these all the reaction sides and flattern into a hashset to test the length */
             Set hashes = new HashSet<Integer>( Arrays.asList( queryReactants.hashCode() , queryProducts.hashCode() ,
                                                               otherReactants.hashCode() , otherProducts.hashCode() ) );
 
 
             /* Check the correct number of side */
             if ( hashes.size() != 2 ) {
                 // not handling cases where reactants and products are the same.. could just be same hashcode
                 if ( hashes.size() == 1 ) {
                     throw new UnsupportedOperationException( "Reaction.equals(): Unresolvable reaction comparision [1]" );
                 }
                 return false;
             }
 
             // these are sorted so can do direct equals on the sets
             if ( queryReactants.hashCode() == otherReactants.hashCode() ) {
                 return queryReactants.equals( otherReactants ) && queryProducts.equals( otherProducts );
             } else if ( queryReactants.hashCode() == otherProducts.hashCode() ) {
                 return queryReactants.equals( otherProducts ) && queryProducts.equals( otherReactants );
             } else {
                 return false; // this.reac == this.prod and other.reac == other.prod... and so must be false (2x hashe values)
             }
 
         } else {
             if ( equals( queryReactants , otherReactants ) && equals( queryProducts , otherProducts ) ) {
                 return true;
             }
            if ( equals( queryReactants , otherProducts ) && equals( queryReactants , otherReactants ) ) {
                 return true;
             }
         }
 
         return false;
 
     }
 
     private boolean equals( List<Participant<M , S , C>> q , List<Participant<M , S , C>> o ) {
 
         if ( q.size() != o.size() ) {
             return false;
         }
 
         Set<Participant<M , S , C>> matchedSet = new HashSet<Participant<M , S , C>>();
         for ( Participant queryParticipant : q ) {
             Participant<M , S , C> matched = queryParticipant.getMatching( o );
             if ( matched == null ) {
                 return false;
             }
             matchedSet.add( matched );
         }
 
         return o.size() == matchedSet.size();
 
     }
 
     /**
      * Displays the objects stored in the reaction in string form prefixed with the stoichiometric
      * coefficients and post fixed with compartments if either exists. The reaction reversibility is determined
      * by the enumeration value {@see Reversibility} enumeration. Example representation:
      * <code>(1)A [e] + (1)B [c] <=?=> (1)C [c] + (1)A [c]</code>
      *
      *
      * @return textual representation of the reaction
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder( 40 );
 
         sb.append( StringUtils.join( reactants , " + " ) ).append( ' ' ).append( reversibility ).append( ' ' ).append( StringUtils.
                 join( products , " + " ) );
 
 
         return sb.toString();
     }
 }
