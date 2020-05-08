 /**
  * MetaboliteMatchIndication.java
  *
  * 2012.02.02
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
 package uk.ac.ebi.chemet.render.components;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.Box;
 import javax.swing.JComponent;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.openscience.cdk.Element;
 import org.openscience.cdk.Isotope;
 import org.openscience.cdk.interfaces.IMolecularFormula;
 import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
 import uk.ac.ebi.annotation.chemical.MolecularFormula;
 import uk.ac.ebi.caf.utility.TextUtility;
 import uk.ac.ebi.interfaces.entities.Metabolite;
 
 
 /**
  *
  *          MetaboliteMatchIndication 2012.02.02
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  *
  *          Class description
  *
  */
 public class MetaboliteMatchIndication {
 
     private static final Logger LOGGER = Logger.getLogger(MetaboliteMatchIndication.class);
 
     private JComponent component;
 
     private Metabolite query;
 
     private Metabolite subject;
 
     private MatchIndication name = new MatchIndication(300, 300);
 
     private MatchIndication formula = new MatchIndication(300, 300);
 
     private MatchIndication charge = new MatchIndication(300, 300);
 
 
     public MetaboliteMatchIndication() {
         component = Box.createVerticalBox();
         component.add(name.getComponent());
         component.add(formula.getComponent());
         component.add(charge.getComponent());
     }
 
 
     public JComponent getComponent() {
         return component;
     }
 
 
     public void setQueryName(String string) {
         name.setLeft(string);
     }
 
 
     public void setQuery(Metabolite query) {
         this.query = query;
         name.setLeft(query.getName());
         charge.setLeft(query.getCharge() == null ? "N/A" : query.getCharge().toString());
     }
 
 
     public void setSubject(Metabolite subject) {
         this.subject = subject;
 
         name.setRight(subject.getName());
 
         Integer nameDiff = StringUtils.getLevenshteinDistance(query.getName().toLowerCase(), subject.getName().toLowerCase());
         name.setDifference(nameDiff.toString());
         name.setQuality(nameDiff <= 2 ? MatchIndication.Quality.Good
                         : nameDiff <= 5 ? MatchIndication.Quality.Okay
                           : MatchIndication.Quality.Bad);
 
 
         double queryCharge = query.getCharge() == null ? 0 : query.getCharge();
         double subjectCharge = subject.getCharge() == null ? 0 : subject.getCharge();
 
         charge.setRight(Double.toString(subjectCharge));
 
         double chargeDiff = Math.abs(queryCharge - subjectCharge);
 
         charge.setQuality(chargeDiff < 1 ? MatchIndication.Quality.Good
                           : chargeDiff < 2 ? MatchIndication.Quality.Okay
                             : MatchIndication.Quality.Bad);
 
         setFormulaQuality();
 
     }
 
 
     public void setFormulaQuality() {
 
         formula.setQuality(MatchIndication.Quality.Bad);
 
         List<MolecularFormula> queryMfs = new ArrayList(query.getAnnotationsExtending(MolecularFormula.class));
        List<MolecularFormula> subjectMfs = new ArrayList(subject.getAnnotationsExtending(MolecularFormula.class));
 
         formula.setLeft("N/A");
         formula.setRight("N/A");
 
         if (!queryMfs.isEmpty()) {
             formula.setLeft(TextUtility.html(queryMfs.get(0).toHTML()));
         }
         if (!subjectMfs.isEmpty()) {
             formula.setRight(TextUtility.html(subjectMfs.get(0).toHTML()));
         }
 
         Isotope hydrogen = new Isotope(new Element("H"));
         for (MolecularFormula queryMf : queryMfs) {
             for (MolecularFormula subjectMf : subjectMfs) {
 
                 if (MolecularFormulaManipulator.compare(queryMf.getFormula(), subjectMf.getFormula())) {
                     formula.setQuality(MatchIndication.Quality.Good);
                     formula.setLeft(TextUtility.html(queryMf.toHTML()));
                     formula.setRight(TextUtility.html(subjectMf.toHTML()));
                     return;
                 } else {
                     IMolecularFormula mf1 = queryMf.getFormula();
                     IMolecularFormula mf2 = subjectMf.getFormula();
 
                     int mf1hc = MolecularFormulaManipulator.getElementCount(mf1, hydrogen);
                     int mf2hc = MolecularFormulaManipulator.getElementCount(mf2, hydrogen);
 
                     mf1 = MolecularFormulaManipulator.removeElement(mf1, hydrogen);
                     mf2 = MolecularFormulaManipulator.removeElement(mf2, hydrogen);
 
 
                     if (MolecularFormulaManipulator.compare(mf1, mf2)) {
                         formula.setQuality(MatchIndication.Quality.Okay);
                         formula.setLeft(TextUtility.html(queryMf.toHTML()));
                         formula.setRight(TextUtility.html(subjectMf.toHTML()));
                     }
 
                     mf1.addIsotope(hydrogen, mf1hc);
                     mf2.addIsotope(hydrogen, mf2hc);
 
                 }
             }
         }
 
     }
 }
