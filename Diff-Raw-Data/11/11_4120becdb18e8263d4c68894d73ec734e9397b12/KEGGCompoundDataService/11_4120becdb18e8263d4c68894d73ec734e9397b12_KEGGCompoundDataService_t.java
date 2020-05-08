 package uk.ac.ebi.chemet.service.query.data;
 
 import org.apache.log4j.Logger;
 import org.openscience.cdk.DefaultChemObjectBuilder;
 import org.openscience.cdk.interfaces.IChemObjectBuilder;
 import org.openscience.cdk.interfaces.IMolecularFormula;
 import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
 import uk.ac.ebi.chemet.service.index.data.HMDBDataIndex;
import uk.ac.ebi.chemet.service.index.data.KEGGCompoundDataIndex;
 import uk.ac.ebi.chemet.service.loader.writer.DefaultDataIndexWriter;
 import uk.ac.ebi.chemet.service.query.AbstractQueryService;
 import uk.ac.ebi.resource.chemical.HMDBIdentifier;
 import uk.ac.ebi.resource.chemical.KEGGCompoundIdentifier;
 import uk.ac.ebi.service.query.data.MolecularChargeService;
 import uk.ac.ebi.service.query.data.MolecularFormulaService;
 
 import java.util.Collection;
 
 /**
  * KEGGCompoundDataService - 27.02.2012 <br/>
  * <p/>
  * Class provides MolecularChargeService and MolecularFormulaService for the
  * KEGG Compound Database
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 public class KEGGCompoundDataService
         extends AbstractQueryService<KEGGCompoundIdentifier>
         implements MolecularFormulaService<KEGGCompoundIdentifier> {
 
     private static final Logger LOGGER = Logger.getLogger(KEGGCompoundDataService.class);
 
     private static final IChemObjectBuilder BUILDER = DefaultChemObjectBuilder.getInstance();
 
     public KEGGCompoundDataService() {
        super(new KEGGCompoundDataIndex());
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public IMolecularFormula getIMolecularFormula(KEGGCompoundIdentifier identifier) {
         String formula = getMolecularFormula(identifier);
         if (formula.isEmpty())
             return BUILDER.newInstance(IMolecularFormula.class);
         return MolecularFormulaManipulator.getMolecularFormula(formula,
                                                                BUILDER);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public String getMolecularFormula(KEGGCompoundIdentifier identifier) {
         return getFirstValue(identifier, MOLECULAR_FORMULA);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Collection<KEGGCompoundIdentifier> searchMolecularFormula(IMolecularFormula formula, boolean approximate) {
         return searchMolecularFormula(MolecularFormulaManipulator.getString(formula), approximate);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Collection<KEGGCompoundIdentifier> searchMolecularFormula(String formula, boolean approximate) {
         if (approximate) {
             LOGGER.error("Approximate search is not yet implemented");
         }
         return getIdentifiers(create(formula, MOLECULAR_FORMULA));
     }
 
     /**
      * @inheritDoc
      */
     @Override
    public KEGGCompoundIdentifier getIdentifier() {
        return new KEGGCompoundIdentifier();
     }
 
 }
