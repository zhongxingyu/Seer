 /**
  * ***************************************************************************
  * Copyright (c) 2010 Qcadoo Limited
  * Project: Qcadoo Framework
  * Version: 1.1.2
  *
  * This file is part of Qcadoo.
  *
  * Qcadoo is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation; either version 3 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  * ***************************************************************************
  */
 package com.qcadoo.view.internal.ribbon.templates.model;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 import com.qcadoo.view.internal.api.ViewDefinition;
 import com.qcadoo.view.internal.ribbon.model.InternalRibbonGroup;
 import com.qcadoo.view.internal.ribbon.model.RibbonGroupsPack;
 import com.qcadoo.view.internal.ribbon.templates.RibbonTemplateParameters;
 
 public class TemplateRibbonGroupsPack implements RibbonGroupsPack {
 
     private final RibbonTemplate template;
 
     private final RibbonTemplateParameters parameters;
 
     private List<InternalRibbonGroup> groups;
 
     private final ViewDefinition viewDefinition;
 
     public TemplateRibbonGroupsPack(final RibbonTemplate template, final RibbonTemplateParameters parameters,
             final ViewDefinition viewDefinition) {
         this.template = template;
         this.parameters = parameters;
         this.viewDefinition = viewDefinition;
     }
 
     private TemplateRibbonGroupsPack(final RibbonTemplate template, final RibbonTemplateParameters parameters,
             final ViewDefinition viewDefinition, final List<InternalRibbonGroup> updatedGroups) {
         this.template = template;
         this.parameters = parameters;
         this.viewDefinition = viewDefinition;
         this.groups = updatedGroups;
     }
 
     @Override
     public List<InternalRibbonGroup> getGroups() {
        if (groups == null) {
             groups = template.getRibbonGroups(parameters, viewDefinition);
         }
         return groups;
     }
 
     @Override
     public InternalRibbonGroup getGroupByName(final String groupName) {
         for (InternalRibbonGroup group : getGroups()) {
             if (group.getName().equals(groupName)) {
                 return group;
             }
         }
         return null;
     }
 
     @Override
     public RibbonGroupsPack getCopy() {
         return new TemplateRibbonGroupsPack(template, parameters, viewDefinition);
     }
 
     @Override
     public RibbonGroupsPack getUpdate() {
         List<InternalRibbonGroup> updatedGroups = Lists.newArrayList();
         for (InternalRibbonGroup group : getGroups()) {
             InternalRibbonGroup updatedGroup = group.getUpdate();
             if (updatedGroup != null) {
                 updatedGroups.add(updatedGroup);
             }
         }
         if (updatedGroups.isEmpty()) {
             return null;
         }
 
         return new TemplateRibbonGroupsPack(template, parameters, viewDefinition, updatedGroups);
     }
 
 }
