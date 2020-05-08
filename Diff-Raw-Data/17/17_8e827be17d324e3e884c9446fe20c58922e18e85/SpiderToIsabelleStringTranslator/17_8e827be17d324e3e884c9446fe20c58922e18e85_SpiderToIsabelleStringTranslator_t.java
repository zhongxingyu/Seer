 /*
  * File name: SpiderToIsabelleStringTranslator.java
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
 
 import mixr.isabelle.terms.StringFormat;
 import mixr.isabelle.terms.StringFormula;
import mixr.logic.Formula;
 import mixr.logic.FormulaRepresentation;
 import mixr.logic.FormulaTranslator;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 import speedith.core.lang.SpiderDiagram;
 import speedith.core.lang.export.ExportException;
 import speedith.core.lang.export.Isabelle2011ExportProvider;
 import speedith.core.lang.export.SDExporter;
 import speedith.core.lang.export.SDExporting;
 import propity.util.Maps;
 
 /**
  *
  * @author Matej Urbas [matej.urbas@gmail.com]
  */
 public class SpiderToIsabelleStringTranslator extends FormulaTranslator {
     
     //<editor-fold defaultstate="collapsed" desc="Fields">
     private final SDExporter SDPrettyExporter;
     private final SDExporter SDNormalExporter;
     //</editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Singleton stuff">
     @NbBundle.Messages({
         "SDtoISAStringTrans_internal_name=SpiderDiagram_to_Isabelle_string_formula"
     })
     private SpiderToIsabelleStringTranslator() {
         super(SpeedithFormatDescriptor.getInstance(), StringFormat.getInstance(), TranslationType.ToEquivalent, Bundle.SDtoISAStringTrans_internal_name());
         
         SDPrettyExporter = SDExporting.getExporter(Isabelle2011ExportProvider.FormatName, Maps.createTreeMap(new String[]{
                                        Isabelle2011ExportProvider.Parameter_UseXSymbols
                                    }, "true"));
         
         SDNormalExporter = SDExporting.getExporter(Isabelle2011ExportProvider.FormatName, null);
     }
 
     public static SpiderToIsabelleStringTranslator getInstance() {
         return SpiderToIsabelleStringTranslator.SingletonContainer.Instance;
     }
 
     private static class SingletonContainer {
 
         private static final SpiderToIsabelleStringTranslator Instance = new SpiderToIsabelleStringTranslator();
     }
     // </editor-fold>
 
     @Override
     @NbBundle.Messages({
         "STIST_description=Translation of spider diagrams into Isabelle string formulae."
     })
     public String getDescription() {
         return Bundle.STIST_description();
     }
 
     @Override
     @NbBundle.Messages({
         "STIST_pretty_name=Spider diagrams to Isabelle formulae."
     })
     public String getPrettyName() {
         return Bundle.STIST_pretty_name();
     }
 
     @Override
     @NbBundle.Messages({
         "STIST_no_sd_format_representation=Cannot translate the given formula with this translator. The formula has no spider diagram representation.",
         "STIST_no_sd=The formula contains an invalid spider diagram.",
         "STIST_translation_failed=The translation of the spider diagram to an Isabelle formula failed."
     })
     public FormulaRepresentation translate(Formula formula) throws TranslationException {
         ArrayList<? extends FormulaRepresentation> sdFormulae = formula.fetchRepresentations(SpeedithFormatDescriptor.getInstance());
         if (sdFormulae == null || sdFormulae.isEmpty()) {
             throw new TranslationException(Bundle.STIST_no_sd_format_representation());
         }
         FormulaRepresentation sdFormula = sdFormulae.get(0);
         try {
             if (sdFormula.getFormula() instanceof SpiderDiagram) {
             String export = SDNormalExporter.export((SpiderDiagram)sdFormula.getFormula());
             return new FormulaRepresentation(new StringFormula(export), StringFormat.getInstance());
             } else {
                 throw new TranslationException(Bundle.STIST_no_sd());
             }
         } catch (ExportException ex) {
             throw new TranslationException(Bundle.STIST_translation_failed(), ex);
         }
     }
 
     @Override
     public FormulaRepresentation translate(List<? extends Formula> premises) throws TranslationException {
         throw new TranslationException("Not supported yet.");
     }
 }
