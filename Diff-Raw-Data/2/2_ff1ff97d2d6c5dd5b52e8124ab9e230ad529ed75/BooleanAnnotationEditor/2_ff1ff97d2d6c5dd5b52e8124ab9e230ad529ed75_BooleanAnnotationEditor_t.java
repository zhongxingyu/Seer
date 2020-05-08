 /*
  * Copyright (c) 2013. EMBL, European Bioinformatics Institute
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
package uk.ac.ebi.mdk.ui.edit.annotation;
 
 import uk.ac.ebi.caf.component.factory.CheckBoxFactory;
 import uk.ac.ebi.mdk.domain.annotation.primitive.BooleanAnnotation;
 
 import javax.swing.*;
 
 
 /**
  *
  *          NewClass 2012.02.14
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  *
  *          Class description
  *
  */
 public class BooleanAnnotationEditor
         extends AbstractAnnotationEditor<BooleanAnnotation> {
 
     private JCheckBox is = CheckBoxFactory.newCheckBox("Yes?");
 
     public BooleanAnnotationEditor() {
     }
 
     @Override
     public void setAnnotation(BooleanAnnotation annotation) {
         super.setAnnotation(annotation);
         is.setSelected(annotation.getValue());
     }
 
 
     @Override
     public BooleanAnnotation newAnnotation() {
         BooleanAnnotation a = super.newAnnotation();
         a.setValue(is.isSelected());
         return a;
     }
 
     @Override
     public JComponent getComponent() {
         return is;
     }
 
     @Override
     public AbstractAnnotationEditor newInstance() {
         return new BooleanAnnotationEditor();
     }
 }
