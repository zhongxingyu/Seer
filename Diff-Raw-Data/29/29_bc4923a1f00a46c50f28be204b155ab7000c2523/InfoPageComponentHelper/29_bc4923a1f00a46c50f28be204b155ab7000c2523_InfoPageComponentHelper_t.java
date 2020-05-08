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
 
 package org.agatom.springatom.ip.component.helper;
 
 import org.agatom.springatom.component.helper.ComponentHelper;
 import org.agatom.springatom.component.meta.LayoutType;
 import org.agatom.springatom.ip.component.elements.InfoPageComponent;
 import org.agatom.springatom.ip.component.elements.InfoPagePanelComponent;
 import org.agatom.springatom.ip.component.elements.attributes.InfoPageAttributeComponent;
 import org.agatom.springatom.ip.component.elements.meta.AttributeDisplayAs;
 import org.springframework.hateoas.Link;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public interface InfoPageComponentHelper
         extends ComponentHelper {
     Link getInfoPageLink(String path, Long id);
 
     InfoPagePanelComponent newBasicPanel(InfoPageComponent cmp, LayoutType layout);
 
     InfoPagePanelComponent newOneToManyPanel(InfoPageComponent cmp, LayoutType layout);
 
     InfoPagePanelComponent newManyToOnePanel(InfoPageComponent cmp, LayoutType layout);
 
     InfoPagePanelComponent newSystemPanel(InfoPageComponent cmp, LayoutType layout);
 
     InfoPageAttributeComponent newValueAttribute(InfoPagePanelComponent panel, String path, String entityName);
 
     InfoPageAttributeComponent newLinkAttribute(InfoPagePanelComponent panel, String path, String entityName);
 
    InfoPageAttributeComponent newEmailAttribute(InfoPagePanelComponent panel, String path, String entityName);

     InfoPageAttributeComponent newTableAttribute(InfoPagePanelComponent panel, String path, String entityName);
 
     InfoPageAttributeComponent newAttribute(InfoPagePanelComponent panel, String path, String messageKey, AttributeDisplayAs displayAs);
 }
