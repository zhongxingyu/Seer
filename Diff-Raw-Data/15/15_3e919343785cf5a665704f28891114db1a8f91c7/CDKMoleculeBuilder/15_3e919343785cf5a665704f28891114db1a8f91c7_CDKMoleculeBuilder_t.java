 /**
  * CDKMoleculeBuilder.java
  *
  * 2011.07.18
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
 package uk.ac.ebi.chemet.util;
 
 import java.io.StringReader;
 import org.apache.log4j.Logger;
 import org.openscience.cdk.Molecule;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IMolecule;
 import org.openscience.cdk.io.MDLV2000Reader;
 import org.openscience.cdk.io.MDLV3000Reader;
 import uk.ac.ebi.metabolomes.identifier.InChI;
 
 /**
  * @name    CDKMoleculeBuilder
  * @date    2011.07.18
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  * @brief   ...class description...
  *
  */
 public class CDKMoleculeBuilder {
 
     private static final Logger LOGGER = Logger.getLogger( CDKMoleculeBuilder.class );
 
     /**
      * Given and InChI string and a molFile string try and build a IMolecule object
      * @param inchi
      * @param molFile
      * @return
      */
     public static IMolecule build( String inchi , String molFile ) {
 
        // Todo smiles
         IMolecule molecule = null;
         molecule = buildFromMolFileV2000( new MDLV2000Reader( new StringReader( molFile ) ) );
         if ( molecule != null ) {
             return molecule;
         }
         molecule = buildFromMolFileV3000( new MDLV3000Reader( new StringReader( molFile ) ) );
         if ( molecule != null ) {
             return molecule;
         }
         molecule = buildFromInChI( new InChI( inchi ) );
         return molecule;
     }
 
     /**
      * Builds an IMolecule from an InChI String
      * @param inchi
      * @return
      */
     public static IMolecule buildFromInChI( InChI inchi ) {
        throw new UnsupportedOperationException( "Not supported yet" );
     }
 
     /**
      * Builds an IMolecule from a V2000 mol file
      * @param reader
      * @return
      */
     public static IMolecule buildFromMolFileV2000( MDLV2000Reader reader ) {

        IMolecule template = new Molecule();

         try {
             return reader.read( template );
         } catch ( CDKException ex ) {
             // do nothing
         }
         return null;
     }
 
     /**
      * Builds an IMolecule from a V3000 mol file
      * @param reader
      * @return
      */
     public static IMolecule buildFromMolFileV3000( MDLV3000Reader reader ) {
        IMolecule template = new Molecule();
         try {
             return reader.read( template );
         } catch ( CDKException ex ) {
             // do nothing
         }
         return null;
     }
 }
