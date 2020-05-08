 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.ip.component.elements.attributes;
 
 import com.google.common.base.Objects;
 import org.agatom.springatom.component.EmbeddableComponent;
 import org.agatom.springatom.component.elements.DefaultComponent;
 import org.agatom.springatom.core.util.Localized;
 import org.agatom.springatom.ip.component.elements.meta.AttributeDisplayAs;
 
 import javax.annotation.Nonnull;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public class InfoPageAttributeComponent
         extends DefaultComponent
         implements Localized,
                    EmbeddableComponent {
     protected int                position   = -1;
     protected String             path       = null;
     protected AttributeDisplayAs displayAs  = null;
     protected String             messageKey = null;
 
     public String getPath() {
         return path;
     }
 
     public InfoPageAttributeComponent setPath(final String path) {
         this.path = path;
         return this;
     }
 
     public AttributeDisplayAs getDisplayAs() {
         return displayAs;
     }
 
     public InfoPageAttributeComponent setDisplayAs(final AttributeDisplayAs displayAs) {
         this.displayAs = displayAs;
         return this;
     }
 
     public boolean isValueAttribute() {
         return this.displayAs.equals(AttributeDisplayAs.VALUE);
     }
 
     public boolean isInfoPageAttribute() {
         return this.displayAs.equals(AttributeDisplayAs.INFOPAGE);
     }
 
     public boolean isTableAttribute() {
         return this.displayAs.equals(AttributeDisplayAs.TABLE);
     }
 
     @Override
     public String getMessageKey() {
         return this.messageKey;
     }
 
     public void setMessageKey(final String messageKey) {
         this.messageKey = messageKey;
     }
 
     @Override
     public int getPosition() {
         return position;
     }
 
     @Override
     public void setPosition(final int position) {
         this.position = position;
     }
 
     @Override
     public int compareTo(final @Nonnull EmbeddableComponent panel) {
         return Integer.compare(this.position, panel.getPosition());
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(position, path, displayAs, messageKey, title);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         InfoPageAttributeComponent that = (InfoPageAttributeComponent) o;
 
         return Objects.equal(this.position, that.position) &&
                 Objects.equal(this.path, that.path) &&
                 Objects.equal(this.displayAs, that.displayAs) &&
                 Objects.equal(this.messageKey, that.messageKey) &&
                 Objects.equal(this.title, that.title);
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                       .addValue(position)
                       .addValue(path)
                       .addValue(displayAs)
                       .addValue(messageKey)
                       .addValue(title)
                       .toString();
     }
 }
