 package uk.ac.ebi.chemet.io.domain.data;
 
 import org.apache.log4j.Logger;
 import uk.ac.ebi.caf.utility.version.annotation.CompatibleSince;
 import uk.ac.ebi.chemet.io.core.EnumReader;
 import uk.ac.ebi.chemet.io.domain.EntityInput;
 import uk.ac.ebi.chemet.io.domain.EntityReader;
 import uk.ac.ebi.interfaces.entities.*;
 
 import java.io.DataInput;
 import java.io.IOException;
 
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
 public class ReactionDataReader
         implements EntityReader<MetabolicReaction> {
 
     private static final Logger LOGGER = Logger.getLogger(ReactionDataReader.class);
 
     private DataInput in;
     private EntityInput entityIn;
     private EnumReader enumReader;
     private EntityFactory factory;
 
     public ReactionDataReader(DataInput in, EntityFactory factory, EntityInput entityIn){
         this.in = in;
         this.entityIn = entityIn;
         this.enumReader = new EnumReader(in);
         this.factory = factory;
     }
 
     public MetabolicReaction readEntity() throws IOException, ClassNotFoundException {
 
         MetabolicReaction rxn = factory.newInstance(MetabolicReaction.class);
 
         int nReactants = in.readByte();
        
         for(int i = 0; i < nReactants; i++){
             MetabolicParticipant p = factory.newInstance(MetabolicParticipant.class);
             p.setCoefficient(in.readDouble());
             p.setCompartment(enumReader.readEnum());
             p.setMolecule(entityIn.read(Metabolite.class));
             rxn.addReactant(p);
         }
 
         int nProducts = in.readByte();
 
         for(int i = 0; i < nProducts; i++){
             MetabolicParticipant p = factory.newInstance(MetabolicParticipant.class);
             p.setCoefficient(in.readDouble());
             p.setCompartment(enumReader.readEnum());
             p.setMolecule(entityIn.read(Metabolite.class));
             rxn.addProduct(p);
         }
 
        rxn.setDirection(enumReader.readEnum());
 
         // read modifiers
         int nModifiers = in.readByte();
         for(int i = 0; i < nModifiers; i++){
             rxn.addModifier( (GeneProduct) entityIn.read());
         }
 
 
         return rxn;
 
     }
 
 }
