 /**
  * MetaboliteMarshal_0_8_5_0.java
  *
  * 2012.01.31
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
 package uk.ac.ebi.io.core.marshal.versions;
 
 import java.io.IOException;
 import java.util.Arrays;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.caf.utility.version.Version;
 import uk.ac.ebi.chemet.entities.reaction.DirectionImplementation;
 import uk.ac.ebi.core.MetabolicReactionImplementation;
 import uk.ac.ebi.core.reaction.Membrane;
 import uk.ac.ebi.interfaces.entities.Metabolite;
 import uk.ac.ebi.core.reaction.MetabolicParticipantImplementation;
import uk.ac.ebi.core.reaction.compartment.Organelle;
 import uk.ac.ebi.interfaces.AnnotatedEntity;
 import uk.ac.ebi.interfaces.entities.MetabolicParticipant;
 import uk.ac.ebi.interfaces.entities.MetabolicReaction;
 import uk.ac.ebi.interfaces.io.ReconstructionInputStream;
 import uk.ac.ebi.interfaces.io.ReconstructionOutputStream;
 import uk.ac.ebi.interfaces.io.marshal.EntityMarshaller;
 import uk.ac.ebi.interfaces.reaction.Compartment;
 import uk.ac.ebi.io.core.marshal.AbstractAnnotatedEntityMarshaller;
 
 
 /**
  *
  *          MetaboliteMarshal_0_8_5_0 2012.01.31
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  *
  *          Class description
  *
  */
 public class ReactionMarshaller_0_8_5_1 extends AbstractAnnotatedEntityMarshaller {
 
     private static final Logger LOGGER = Logger.getLogger(ReactionMarshaller_0_8_5_1.class);
 
 
     public ReactionMarshaller_0_8_5_1() {
         super(new Version(0, 8, 5, 1));
     }
 
 
     public EntityMarshaller newInstance() {
         return new ReactionMarshaller_0_8_5_1();
     }
 
 
     public Compartment getCompartment(byte index) {

         for (Compartment[] compartments : Arrays.asList((Compartment[]) Organelle.values(),
                                                         (Compartment[]) Membrane.values())) {
             for (Compartment comparment : compartments) {
                 if (comparment.getRanking() == index) {
                     return comparment;
                 }
             }
        }

         return Organelle.CYTOPLASM;

     }
 
 
     @Override
     public AnnotatedEntity read(ReconstructionInputStream in) throws IOException, ClassNotFoundException {
 
         MetabolicReactionImplementation rxn = new MetabolicReactionImplementation();
 
         int reactantCount = in.readInt();
 
         for (int i = 0; i < reactantCount; i++) {
 
             MetabolicParticipantImplementation p = new MetabolicParticipantImplementation();
             p.setCoefficient(in.readDouble());
             p.setCompartment(getCompartment(in.readByte()));
             p.setMolecule((Metabolite) in.getMetabolite(in.readInt()));
 
             rxn.addReactant(p);
         }
 
         int productCount = in.readInt();
 
         for (int i = 0; i < productCount; i++) {
 
             MetabolicParticipantImplementation p = new MetabolicParticipantImplementation();
             p.setCoefficient(in.readDouble());
             p.setCompartment(getCompartment(in.readByte()));
             p.setMolecule((Metabolite) in.getMetabolite(in.readInt()));
 
             rxn.addProduct(p);
         }
 
         rxn.setDirection(DirectionImplementation.valueOf(in.readByte()));
 
         return rxn;
 
     }
 
 
     @Override
     public void write(ReconstructionOutputStream out, AnnotatedEntity entity) throws IOException {
 
         MetabolicReaction rxn = (MetabolicReaction) entity;
 
         out.writeInt(rxn.getReactantCount());
 
         for (MetabolicParticipant p : rxn.getReactants()) {
             out.writeDouble(p.getCoefficient());
             out.writeByte(p.getCompartment().getRanking());
             out.writeIndex(p.getMolecule());
         }
 
         out.writeInt(rxn.getProductCount());
 
         for (MetabolicParticipant p : rxn.getProducts()) {
             out.writeDouble(p.getCoefficient());
             out.writeByte(p.getCompartment().getRanking());
             out.writeIndex(p.getMolecule());
         }
 
         out.writeByte(rxn.getDirection().getIndex());
 
     }
 }
