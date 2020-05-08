 /*
  * File name: IsabelleToSpidersTranslator.java
  *    Author: Matej Urbas [matej.urbas@gmail.com]
  * 
  *  Copyright © 2012 Matej Urbas
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package speedith.mixr.logic;
 
 import mixr.isabelle.terms.TermFormatDescriptor;
 import mixr.isabelle.terms.TermGoal;
import mixr.logic.normalization.Formula;
 import mixr.logic.FormulaRepresentation;
 import mixr.logic.FormulaTranslator;
 import isabelle.Term;
 import java.util.ArrayList;
 import java.util.List;
 import org.openide.util.NbBundle;
 import speedith.core.lang.SpiderDiagram;
 import speedith.core.lang.reader.ReadingException;
 
 /**
  * This translator is able to translate
  * {@link isabelle.Term.Term Isabelle terms} to
  * {@link SpiderDiagram spider diagrams}.
  *
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 @NbBundle.Messages({
     "ISAtoSDTrans_internal_name=IsabelleTerms_to_SpiderDiagrams"
 })
 public class IsabelleToSpidersTranslator extends FormulaTranslator {
 
     // <editor-fold defaultstate="collapsed" desc="Singleton stuff">
     private IsabelleToSpidersTranslator() {
         super(TermFormatDescriptor.getInstance(), SpeedithFormatDescriptor.getInstance(), TranslationType.ToEquivalent, Bundle.ISAtoSDTrans_internal_name());
     }
 
     public static IsabelleToSpidersTranslator getInstance() {
         return SingletonContainer.Instance;
     }
 
     private static class SingletonContainer {
 
         private static final IsabelleToSpidersTranslator Instance = new IsabelleToSpidersTranslator();
     }
     // </editor-fold>
 
     @Override
     @NbBundle.Messages({
         "ISAtoSDTrans_description=Translation of Isabelle terms formulae to spider diagrams."
     })
     public String getDescription() {
         return Bundle.ISAtoSDTrans_description();
     }
 
     @Override
     @NbBundle.Messages({
         "ISAtoSDTrans_pretty_name=Isabelle terms to spider diagrams"
     })
     public String getPrettyName() {
         return Bundle.ISAtoSDTrans_pretty_name();
     }
 
     @Override
     @NbBundle.Messages({
         "ISAtoSDTrans_translation_error_no_isa_term=The formula does not have an Isabelle term representation.",
         "ISAtoSDTrans_translation_error_reading_failed=The Isabelle formula is not of the format that can be translated to spider diagrams.",
         "ISAtoSDTrans_translation_error_isa_formula_not_a_term=The Isabelle driver might be faulty. It returned an Isabelle term formula that is not a Term.Term.",
         "ISAtoSDTrans_translation_error_null_sd_returned=The translation failed to produce a valid spider diagram."
     })
     public FormulaRepresentation translate(Formula formula) throws TranslationException {
         ArrayList<? extends FormulaRepresentation> isaReps = formula.fetchRepresentations(TermFormatDescriptor.getInstance());
         if (isaReps == null || isaReps.isEmpty()) {
             throw new TranslationException(Bundle.ISAtoSDTrans_translation_error_no_isa_term());
         }
         if (isaReps.get(0).getFormula() instanceof Term.Term) {
             Term.Term term = (Term.Term) isaReps.get(0).getFormula();
             try {
                 SpiderDiagram sd = speedith.mixr.isabelle.Translations.termToSpiderDiagram(term);
                 if (sd == null || !sd.isValid()) {
                     throw new TranslationException(Bundle.ISAtoSDTrans_translation_error_null_sd_returned());
                 }
                 return new FormulaRepresentation(sd, SpeedithFormatDescriptor.getInstance());
             } catch (ReadingException ex) {
                 throw new TranslationException(Bundle.ISAtoSDTrans_translation_error_reading_failed(), ex);
             }
         } else {
             throw new IllegalStateException();
         }
     }
 
     @NbBundle.Messages({
         "ITST_not_all_premises=The list of formulae does not contain only premises.",
         "ITST_context_unknown=The type of the given context goal is not supported.",
         "ITST_some_premises_not_terms=Some of the premises are not Isabelle terms.",
         "ITST_premises_translation_failed=Could not extract a spider diagram from the given premises."
     })
     @Override
     public FormulaRepresentation translate(List<? extends Formula> premises) throws TranslationException {
         if (!arePremises(premises)) {
             throw new TranslationException(Bundle.ITST_not_all_premises());
         } else if (!(premises.get(0).getHostingGoal() instanceof TermGoal)) {
             throw new TranslationException(Bundle.ITST_context_unknown());
         } else {
             TermGoal termGoal = (TermGoal) premises.get(0).getHostingGoal();
             ArrayList<Term.Term> terms = new ArrayList<>();
             for (Formula premise : premises) {
                 if (premise.getMainRepresentation().getFormula() instanceof Term.Term) {
                     terms.add((Term.Term) premise.getMainRepresentation().getFormula());
                 } else {
                     throw new TranslationException(Bundle.ITST_some_premises_not_terms());
                 }
             }
             try {
                 return new FormulaRepresentation(speedith.mixr.isabelle.Translations.termToSpiderDiagram(terms, termGoal.getVariables()), SpeedithFormatDescriptor.getInstance());
             } catch (ReadingException ex) {
                 throw new TranslationException(Bundle.ITST_premises_translation_failed(), ex);
             }
         }
     }
 }
