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
 
 package org.agatom.springatom.ip.component.helper.impl;
 
 import org.agatom.springatom.component.elements.PanelComponent;
 import org.agatom.springatom.component.helper.impl.DefaultComponentHelper;
 import org.agatom.springatom.component.meta.LayoutType;
 import org.agatom.springatom.component.meta.PanelType;
 import org.agatom.springatom.ip.component.elements.InfoPageComponent;
 import org.agatom.springatom.ip.component.elements.InfoPagePanelComponent;
 import org.agatom.springatom.ip.component.elements.attributes.InfoPageAttributeComponent;
 import org.agatom.springatom.ip.component.elements.meta.AttributeDisplayAs;
 import org.agatom.springatom.ip.component.elements.meta.PanelHolds;
 import org.agatom.springatom.ip.component.helper.InfoPageComponentHelper;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.hateoas.Link;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 
 @Qualifier(value = "InfoPageComponentHelper")
 public class DefaultInfoPageComponentHelper
         extends DefaultComponentHelper
         implements InfoPageComponentHelper {
 
     @Override
     public Link getInfoPageLink(String path, Long id) {
         return new Link(String.format("/app/ip/%s/%d", path, id)).withRel(String.format("infopage.%s", path));
     }
 
     @Override
     public InfoPagePanelComponent newBasicPanel(final InfoPageComponent cmp, LayoutType layout) {
         final PanelComponent component = new InfoPagePanelComponent()
                 .setHolds(PanelHolds.BASIC_ATTRIBUTES)
                 .setType(PanelType.ATTRIBUTES)
                 .setLayout(layout);
         cmp.addContent((InfoPagePanelComponent) component);
         return populateTitle((InfoPagePanelComponent) component);
     }
 
     @Override
     public InfoPagePanelComponent newOneToManyPanel(final InfoPageComponent cmp, LayoutType layout) {
         final PanelComponent component = new InfoPagePanelComponent()
                 .setHolds(PanelHolds.ONE_TO_MANY_ATTRIBUTES)
                 .setType(PanelType.TABLE)
                 .setLayout(layout);
         cmp.addContent((InfoPagePanelComponent) component);
         return populateTitle((InfoPagePanelComponent) component);
     }
 
     @Override
     public InfoPagePanelComponent newManyToOnePanel(final InfoPageComponent cmp, LayoutType layout) {
         final PanelComponent component = new InfoPagePanelComponent()
                 .setHolds(PanelHolds.MANY_TO_ONE_ATTRIBUTES)
                 .setType(PanelType.ATTRIBUTES)
                 .setLayout(layout);
         cmp.addContent((InfoPagePanelComponent) component);
         return populateTitle((InfoPagePanelComponent) component);
     }
 
     @Override
     public InfoPagePanelComponent newSystemPanel(final InfoPageComponent cmp, LayoutType layout) {
         final PanelComponent component = new InfoPagePanelComponent()
                 .setHolds(PanelHolds.SYSTEM_ATTRIBUTES)
                 .setType(PanelType.ATTRIBUTES)
                 .setLayout(layout);
         cmp.addContent((InfoPagePanelComponent) component);
         return populateTitle((InfoPagePanelComponent) component);
     }
 
     @Override
     public InfoPageAttributeComponent newValueAttribute(final InfoPagePanelComponent panel, final String path, final String entityName) {
         return this.newAttribute(panel, path, String.format("%s.%s", entityName.toLowerCase(), path.toLowerCase()), AttributeDisplayAs.VALUE);
     }
 
     @Override
     public InfoPageAttributeComponent newLinkAttribute(InfoPagePanelComponent panel, String path, String entityName) {
         return this.newAttribute(panel, path, String.format("%s.%s", entityName.toLowerCase(), path.toLowerCase()), AttributeDisplayAs.INFOPAGE);
     }
 
     @Override
    public InfoPageAttributeComponent newEmailAttribute(final InfoPagePanelComponent panel, final String path, final String entityName) {
        return this.newAttribute(panel, path, String.format("%s.%s", entityName.toLowerCase(), path.toLowerCase()), AttributeDisplayAs.EMAIL);
    }

    @Override
     public InfoPageAttributeComponent newTableAttribute(final InfoPagePanelComponent panel, final String path, final String entityName) {
         return this.newAttribute(panel, path, String.format("%s.%s", entityName.toLowerCase(), path.toLowerCase()), AttributeDisplayAs.TABLE);
     }
 
     @Override
     public InfoPageAttributeComponent newAttribute(final InfoPagePanelComponent panel, final String path, final String messageKey, final AttributeDisplayAs displayAs) {
         final InfoPageAttributeComponent attribute = new InfoPageAttributeComponent();
         attribute.setPath(path);
         attribute.setMessageKey(messageKey);
         attribute.setDisplayAs(displayAs);
         panel.addContent(attribute);
         return this.populateTitle(attribute);
     }
 
     private InfoPagePanelComponent populateTitle(final InfoPagePanelComponent val) {
         val.setTitle(
                 this.messageSource.getMessage(
                         val.getHolds().getMessageKey(),
                         LocaleContextHolder.getLocale()
                 )
         );
         return val;
     }
 
     private InfoPageAttributeComponent populateTitle(final InfoPageAttributeComponent val) {
         val.setTitle(
                 entitleFromMessageKey(val)
         );
         return val;
     }
 }
