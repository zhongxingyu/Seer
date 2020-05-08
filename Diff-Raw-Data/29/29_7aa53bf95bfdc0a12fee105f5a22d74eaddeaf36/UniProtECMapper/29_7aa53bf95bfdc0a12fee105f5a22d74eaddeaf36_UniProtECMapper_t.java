 /**
  * UniProtECMapper.java
  *
  * 2011.07.13
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
 package uk.ac.ebi.chemet.io.mapping;
 
 import au.com.bytecode.opencsv.CSVReader;
 import java.util.HashSet;
 import java.util.Set;
 import org.apache.log4j.Logger;
import uk.ac.ebi.core.IdentifierSet;
 import uk.ac.ebi.metabolomes.identifier.AbstractIdentifier;
 import uk.ac.ebi.resource.classification.ECNumber;
 import uk.ac.ebi.resource.IdentifierFactory;
 import uk.ac.ebi.resource.protein.SwissProtIdentifier;
 import uk.ac.ebi.resource.protein.UniProtIdentifier;
 
 /**
  * @name    UniProtECMapper
  * @date    2011.07.13
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  * @brief   Provides mapping of UniProtIds to EC Numbers from a given file
  *
  */
 public class UniProtECMapper
         extends FileHashMapper<UniProtIdentifier , Set<ECNumber>> {
 
     private static final Logger logger = Logger.getLogger( UniProtECMapper.class );
 
     /**
      * Constructor instantiates the UniProt to EC Number mapper with a provided CSVReader
      * @param reader
      * @param keyIndex
      * @param valueIndex
      */
     public UniProtECMapper( CSVReader reader , Integer keyIndex , Integer valueIndex ) {
         super( reader , keyIndex , valueIndex );
     }
 
     @Override
     public UniProtIdentifier parseKey( String keyString ) {
        IdentifierSet identifiers = IdentifierFactory.getInstance().resolveSequenceHeader(keyString);
        if(!identifiers.getIdentifiers().isEmpty()){
            return (UniProtIdentifier) identifiers.getIdentifiers().iterator().next();
         }
        return new SwissProtIdentifier( keyString );  // FIX ME: Convert SwissProt/TrEMBL to a unifier UniProt Id
     }
 
     @Override
     public Set<ECNumber> parseValue( String valueString ) {
         Set<ECNumber> ecs = new HashSet<ECNumber>( 4 );
         // to handle multiple ec numbers first split the string on semi-colon (standard divider)
         String[] ecStrings = valueString.split( ";" );
         // add all to the set
         for ( String ecString : ecStrings ) {
             ecs.add( new ECNumber( ecString ) );
         }
         return ecs;
     }
 
     @Override
     public Set<ECNumber> clash( Set<ECNumber> oldValue ,
                                 Set<ECNumber> newValue ) {
         oldValue.addAll( newValue );
         return oldValue;
     }
 }
