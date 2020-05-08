 /**
  * ReactionLoader.java
  *
  * 2011.08.15
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
 package uk.ac.ebi.chemet.io.sbml;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import javax.xml.stream.XMLStreamException;
 import org.apache.log4j.Logger;
 import org.openscience.cdk.AtomContainer;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.iupac.parser.MoleculeBuilder;
 import org.sbml.jsbml.*;
 import uk.ac.ebi.chemet.entities.Compartment;
 import uk.ac.ebi.chemet.entities.reaction.AtomContainerReaction;
 import uk.ac.ebi.chemet.entities.reaction.participant.AtomContainerParticipant;
 import uk.ac.ebi.chemet.entities.reaction.participant.GenericParticipant;
 import uk.ac.ebi.chemet.exceptions.AbsentAnnotationException;
 import uk.ac.ebi.chemet.exceptions.MissingStructureException;
 import uk.ac.ebi.chemet.exceptions.UnknownCompartmentException;
 import uk.ac.ebi.metabolomes.io.xml.MIRIAMResourceLoader;
 import uk.ac.ebi.metabolomes.util.CDKUtils;
 import uk.ac.ebi.metabolomes.webservices.ChEBIWebServiceConnection;
 
 /**
  * ReactionLoader – 2011.08.15
  *
  * Loads the reactions from an SBML document in to various types of reactions
  *
  * <pre>
 * InputStream sbmlStream                = getClass().getResourceAsStream( "streptomyces-coelicolor-6.2005.xml" );
 * ReactionLoader loader                 = ReactionLoader.getInstance();
  * List<AtomContainerReaction> reactions = loader.getReactions( sbmlStream );
  *
  * for ( AtomContainerReaction r : reactions ) {
  *     System.out.println( r );
  * }
  * </pre>
  *
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  *
  */
 public class ReactionLoader {
 
     private static final Logger LOGGER = Logger.getLogger( ReactionLoader.class );
    private final SBMLReader reader = new SBMLReader();
    private final ChEBIWebServiceConnection chebiWS = new ChEBIWebServiceConnection();

    private ReactionLoader(){
    }

    /**
     * Singleton accessor method
     * @return
     */
    public static ReactionLoader getInstance(){
        return ReactionLoaderHolder.INSTANCE;
    }

    public static class ReactionLoaderHolder {
        private static ReactionLoader INSTANCE = new ReactionLoader();
    }
 
     /**
      *
      * Loads the specified Reactions as an AtomContainer reaction (Note: RDF annotations) for
      * ChEBI (KEGG also in future) is required.
      *
      * @param Model SBML Model
      * @return A collection of fully structured reactions
      *
      */
     public List<AtomContainerReaction> getReactions( Model model ) {
 
         List<AtomContainerReaction> loadedReactions = new ArrayList<AtomContainerReaction>( model.getNumReactions() );
 
         for ( int i = 0; i < model.getNumReactions(); i++ ) {
 
             try {
 
                 Reaction sbmlReaction = model.getReaction( i );
                 loadedReactions.add( getReaction( sbmlReaction ) );
 
             } catch ( UnknownCompartmentException ex ) {
                 LOGGER.error( model.getReaction( i ).getId() + ": " +
                               ex.getMessage() );
             } catch ( AbsentAnnotationException ex ) {
                 LOGGER.error( model.getReaction( i ).getId() + ": " + ex.getMessage() );
 
             } catch ( MissingStructureException ex ) {
                 LOGGER.error( model.getReaction( i ).getId() + ": " + ex.getMessage() );
             }
         }
 
         return loadedReactions;
 
     }
 
     /**
      *
      * Convenience method for loading model from an input stream
      *
      * @param stream SBML XML input stream
      * @return Collection of {@see AtomContainerReaction}s
      *
      * @throws XMLStreamException Thrown if there was a problem reading the SBML document
      *
      */
     public List<AtomContainerReaction> getReactions( InputStream stream ) throws XMLStreamException {
 
         SBMLDocument document = reader.readSBMLFromStream( stream );
         Model model = document.getModel();
 
         return getReactions( model );
 
     }
 
     /**
      *
      * Loads the SBML {@see Reaction} object as a custom {@see AtomContainerReaction} instance.
      *
      *
      * @param sbmlReaction Instance of a loaded SBML reaction
      * @return Instance of {@see AtomContainerReaction}
      *
      * @throws UnknownCompartmentException Thrown if a compartment cannot be found
      * @throws MissingAnnotationException Thrown if a reaction is missing annotations
      * @throws MissingStructureException Thrown if a structure for the molecule could not be loaded
      *
      */
     public AtomContainerReaction getReaction( Reaction sbmlReaction ) throws UnknownCompartmentException ,
                                                                              AbsentAnnotationException ,
                                                                              MissingStructureException {
         AtomContainerReaction reaction = new AtomContainerReaction();
 
         for ( int i = 0; i < sbmlReaction.getNumReactants(); i++ ) {
 
             reaction.addReactant( getParticipant( sbmlReaction.getReactant( i ) ) );
         }
 
         for ( int i = 0; i < sbmlReaction.getNumProducts(); i++ ) {
 
             reaction.addReactant( getParticipant( sbmlReaction.getProduct( i ) ) );
         }
 
         // TODO(johnmay): Add Enzyme annotations and modifiers
 
         return reaction;
     }
 
     /**
      *
      * Constructs a reaction participant (i.e. AtomContainerParticipant or GenericParticipant if the molecule contains
      * and R/Alkyl group)
      *
      * @param   speciesReference An instance of SBML {@see SpeciesReference}
      * @return                   An {@see AtomContainerParticipant} or {@see GenericParticipant}.
      *                           The class {@see GenericParticipant} extends {@see AtomContainerParticipant} and so can
      *                           be handled identically.
      *
      * @throws UnknownCompartmentException Thrown if compartment identifier is not recognised (valid names are found
      *                                     in the {@see Compartment} class)
      * @throws MissingAnnotationException   Thrown
      * @throws MissingStructureException
      *
      */
     public AtomContainerParticipant getParticipant( SpeciesReference speciesReference ) throws
             UnknownCompartmentException , AbsentAnnotationException , MissingStructureException {
         Species species = speciesReference.getSpeciesInstance();
         Compartment compartment = Compartment.getCompartment( species.getCompartmentInstance().getId() );
 
         if ( compartment == Compartment.UNKNOWN ) {
             throw new UnknownCompartmentException( "Compartment " + species.getCompartmentInstance().getId() +
                                                    " was not identifiable" );
         }
 
         if ( species.getNumCVTerms() == 0 ) {
             throw new AbsentAnnotationException( "Species " + species.getId() +
                                                  " did not have any associated Controlled Vocabulary terms" );
         }
 
         IAtomContainer molecule = getAtomContainer( species );
         Double coefficient = speciesReference.getStoichiometry();
 
         return CDKUtils.isMoleculeGeneric( molecule ) ?
                new GenericParticipant( molecule , coefficient , compartment ) :
                new AtomContainerParticipant( molecule , coefficient , compartment );
 
     }
 
     /**
      *
      * Constructs and IAtomContainer from the given SBML chemical {@see Species} using RDF MIRIAM annotations
      *
      * @param  species An instance of an SBML Species using the attached Controlled Vocabulary (CV) terms. The CV
      *         terms must contain a CHEBI identifier (MIRIAM Registry) to allow fetching of a structure using Web Services
      * @return A instance of a CDK {@see IAtomContainer}
      *
      * @throws MissingStructureException Thrown if an {@see IAtomContainer} can not be built
      *
      */
     public IAtomContainer getAtomContainer( Species species ) throws MissingStructureException {
 
         for ( int i = 0; i < species.getNumCVTerms(); i++ ) {
             for ( String resource : species.getCVTerm( i ).getResources() ) {
 
                 String id = MIRIAMResourceLoader.getIdentifier( resource );
                 if ( id.startsWith( "CHEBI" ) ) {
 
                     try {
 
                         return chebiWS.getAtomContainer( id );
                     } catch ( Exception ex ) {
 
                         LOGGER.error( "There was a problem loading: " + id + " : " + ex.getMessage() );
                     }
                 }
             }
         }
 
         throw new MissingStructureException( "Could not load structure for: " + species.getId() );
 
     }
 }
