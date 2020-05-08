 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.fields;
 
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.IPropertyControl;
 import net.sf.jmoney.model2.PropertyAccessor;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * A property control to handle ordinary text input.
  * 
  * @author Nigel Westbury
  * @author Johann Gyger
  */
 public class TextEditor implements IPropertyControl {
 
     private ExtendableObject fExtendableObject;
 
     private PropertyAccessor propertyAccessor;
 
     private Text propertyControl;
 
     /** Creates new TextEditor */
     public TextEditor(Composite parent, PropertyAccessor propertyAccessor) {
         propertyControl = new Text(parent, 0);
         this.propertyAccessor = propertyAccessor;
     }
 
     /** Creates new TextEditor */
     public TextEditor(Composite parent, int style, PropertyAccessor propertyAccessor) {
         propertyControl = new Text(parent, style);
         this.propertyAccessor = propertyAccessor;
     }
 
     public void load(ExtendableObject object) {
         fExtendableObject = object;
         String text = object.getStringPropertyValue(propertyAccessor);
         propertyControl.setText(text == null ? "" : text);
     }
 
     public void save() {
         String text = propertyControl.getText();
         fExtendableObject.setStringPropertyValue(propertyAccessor, text.length() == 0 ? null : text);
     }
 
     /* (non-Javadoc)
      * @see net.sf.jmoney.model2.IPropertyControl#getControl()
      */
     public Control getControl() {
        propertyControl.dispose();
         return propertyControl;
     }
 
 }
