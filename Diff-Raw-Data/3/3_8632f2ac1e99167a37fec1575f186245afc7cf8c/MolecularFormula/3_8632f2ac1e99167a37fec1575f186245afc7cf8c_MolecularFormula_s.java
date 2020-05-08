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
 package uk.ac.ebi.annotation.chemical;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import org.apache.log4j.Logger;
 import org.openscience.cdk.DefaultChemObjectBuilder;
 import org.openscience.cdk.interfaces.IChemObjectBuilder;
 import org.openscience.cdk.interfaces.IMolecularFormula;
 import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
 import uk.ac.ebi.annotation.base.AbstractStringAnnotation;
 import uk.ac.ebi.annotation.util.AnnotationLoader;
 import uk.ac.ebi.core.Description;
 import uk.ac.ebi.interfaces.annotation.Context;
 import uk.ac.ebi.interfaces.annotation.Descriptor;
 import uk.ac.ebi.interfaces.entities.Metabolite;
 
 
 /**
  *          MolecularFormula – 2011.09.14 <br>
  *          Annotation of molecular formula
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 @Context(Metabolite.class)
 @Descriptor(brief = "Molecular Formula",
             description = "The chemical formula of a metabolite")
 public class MolecularFormula
         extends AbstractStringAnnotation {
 
     private static final Logger LOGGER = Logger.getLogger(MolecularFormula.class);
 
     private IMolecularFormula formula;
 
     private String html; // speeds up rendering
 
     private static Description description = AnnotationLoader.getInstance().getMetaInfo(
             MolecularFormula.class);
 
 
     /**
      *
      * Default constructor need for externalization
      *
      */
     public MolecularFormula() {
     }
 
 
     /**
      *
      * Constructs a formula annotation with a provided {@see MolecularFormula} from CDK library
      *
      * @param formula
      *
      */
     public MolecularFormula(IMolecularFormula formula) {
         this.formula = formula;
         super.setValue(MolecularFormulaManipulator.getString(formula));
         if (formula != null) {
             this.html = MolecularFormulaManipulator.getHTML(formula);
         }
     }
 
 
     /**
      *
      * Construct a MolecularFormula annotation from a string
      *
      * @param formula
      *
      */
     public MolecularFormula(String formula) {
         super(formula);
         IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
         this.formula = MolecularFormulaManipulator.getMolecularFormula(formula, builder);
         if (this.formula != null) {
             this.html = MolecularFormulaManipulator.getHTML(this.formula);
         }
 
     }
 
 
     /**
      *
      * Accessor to the underlying formula object
      *
      * @return An instance of IMolecularFormula
      *
      */
     public IMolecularFormula getFormula() {
         return formula;
     }
 
 
     public void setFormula(IMolecularFormula formula) {
         this.formula = formula;
         super.setValue(MolecularFormulaManipulator.getString(formula));
     }
 
 
     public void setFormula(String formula) {
         IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
         super.setValue(formula);
         this.formula = MolecularFormulaManipulator.getMolecularFormula(formula, builder);
     }
 
 
     @Override
     public void setValue(String value) {
         this.setFormula(value);
     }
 
 
     /**
      * Returns HTML formula
      * @return
      */
     public String toHTML() {
         return html;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String getShortDescription() {
         return description.shortDescription;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String getLongDescription() {
         return description.longDescription;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public Byte getIndex() {
         return description.index;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public MolecularFormula newInstance() {
         return new MolecularFormula();
     }
 
 
     public MolecularFormula getInstance(String formula) {
         return new MolecularFormula(formula);
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
         super.readExternal(in);
         IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
         this.formula = MolecularFormulaManipulator.getMolecularFormula(getValue(), builder);
         this.html = MolecularFormulaManipulator.getHTML(formula);
     }
 }
