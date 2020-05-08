 /**
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package uk.ac.ebi.mdk.domain.annotation;
 
 import org.apache.log4j.Logger;
 import org.openscience.cdk.Molecule;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.io.MDLV2000Reader;
 import org.openscience.cdk.io.MDLV2000Writer;
 import uk.ac.ebi.mdk.domain.MetaInfo;
 import uk.ac.ebi.mdk.lang.annotation.Brief;
 import uk.ac.ebi.mdk.lang.annotation.Description;
 import uk.ac.ebi.mdk.lang.annotation.Context;
 import uk.ac.ebi.mdk.domain.entity.Metabolite;
 import uk.ac.ebi.mdk.domain.DefaultLoader;
 
 import java.io.*;
 
 
 /**
  *          ChemicalStructure – 2011.09.08 <br>
  *          Class metaInfo
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 @Context(Metabolite.class)
 @Brief("Chemical Structure")
 @Description("The chemical structure of the metabolite")
 public class AtomContainerAnnotation
         extends AbstractAnnotation
         implements ChemicalStructure {
 
     private static final Logger LOGGER = Logger.getLogger(AtomContainerAnnotation.class);
 
     private IAtomContainer molecule;
 
     private static MetaInfo metaInfo = DefaultLoader.getInstance().getMetaInfo(
             AtomContainerAnnotation.class);
 
 
     public AtomContainerAnnotation() {
     }
 
 
     public AtomContainerAnnotation(IAtomContainer molecule) {
         this.molecule = molecule;
     }
 
 
     /**
      * Use {@see getStructure()}
      * @return
      * @deprecated
      */
     @Deprecated
     public IAtomContainer getMolecule() {
         return molecule;
     }
 
 
     public IAtomContainer getStructure() {
         return this.molecule;
     }
 
 
     public void setStructure(IAtomContainer structure) {
         this.molecule = structure;
     }
 
 
     @Override
     public void writeExternal(ObjectOutput out) throws IOException {
 
         try {
 
             super.writeExternal(out);
             StringWriter stringWriter = new StringWriter();
             MDLV2000Writer writer = new MDLV2000Writer(stringWriter);
             writer.writeMolecule(molecule);
             writer.close();
 
             out.writeUTF(stringWriter.toString());
 
         } catch (Exception ex) {
             throw new IOException("Unable to write IAtomContainer: " + ex.getMessage());
         }
 
     }
 
 
     @Override
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
 
         try {
 
             super.readExternal(in);
             MDLV2000Reader reader = new MDLV2000Reader(new StringReader(in.readUTF()));
             molecule = new Molecule();
             reader.read(molecule);
             reader.close();
 
         } catch (CDKException ex) {
 
             throw new IOException("Unable to read IAtomContainer: " + ex.getMessage());
         }
 
     }
 
 
     @Override
     public String toString() {
        return getShortDescription() + ": " + molecule != null ? molecule.toString() : " null";
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String getShortDescription() {
         return metaInfo.brief;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String getLongDescription() {
         return metaInfo.description;
     }
 
 
 
 
     /**
      * @inheritDoc
      */
     @Override
     public AtomContainerAnnotation newInstance() {
         return new AtomContainerAnnotation();
     }
 }
