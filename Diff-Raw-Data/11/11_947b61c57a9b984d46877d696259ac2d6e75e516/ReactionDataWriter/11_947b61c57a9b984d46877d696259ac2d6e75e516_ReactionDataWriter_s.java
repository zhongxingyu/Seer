 package uk.ac.ebi.chemet.io.domain.data;
 
 import org.apache.log4j.Logger;
 import uk.ac.ebi.caf.utility.version.annotation.CompatibleSince;
 import uk.ac.ebi.chemet.io.core.EnumWriter;
 import uk.ac.ebi.chemet.io.domain.EntityOutput;
 import uk.ac.ebi.chemet.io.domain.EntityWriter;
 import uk.ac.ebi.interfaces.AnnotatedEntity;
 import uk.ac.ebi.interfaces.entities.GeneProduct;
 import uk.ac.ebi.interfaces.entities.MetabolicParticipant;
 import uk.ac.ebi.interfaces.entities.MetabolicReaction;
 
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.Collection;
 
 /**
  * ProteinProductDataWriter - 12.03.2012 <br/>
  * <p/>
  * Class descriptions.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 @CompatibleSince("0.9")
 public class ReactionDataWriter
         implements EntityWriter<MetabolicReaction> {
 
     private static final Logger LOGGER = Logger.getLogger(ReactionDataWriter.class);
 
     private DataOutput out;
     private EntityOutput entityOut;
     private EnumWriter enumWriter;
 
     public ReactionDataWriter(DataOutput out, EntityOutput entityOut){
         this.out = out;
         this.entityOut = entityOut;
         this.enumWriter = new EnumWriter(out);
     }
 
     public void write(MetabolicReaction rxn) throws IOException {
 
 
         out.writeByte(rxn.getReactantCount());
 
         for (MetabolicParticipant p : rxn.getReactants()) {
             out.writeDouble(p.getCoefficient());
             enumWriter.writeEnum(p.getCompartment());
             entityOut.writeData(p.getMolecule());
         }
 
         out.writeByte(rxn.getProductCount());
 
         for (MetabolicParticipant p : rxn.getProducts()) {
             out.writeDouble(p.getCoefficient());
             enumWriter.writeEnum(p.getCompartment());
             entityOut.writeData(p.getMolecule());
         }
 
        enumWriter.writeEnum(rxn.getDirection());
 
         // write modifiers
         Collection<GeneProduct> modifiers = rxn.getModifiers();
         out.writeByte(modifiers.size());
         for(AnnotatedEntity entity : modifiers ){
             entityOut.write(entity);
         }
 
 
     }
 
 }
