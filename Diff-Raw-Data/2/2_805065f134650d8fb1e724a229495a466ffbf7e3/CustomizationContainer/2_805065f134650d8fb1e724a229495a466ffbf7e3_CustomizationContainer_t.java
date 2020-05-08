 /**
  * Copyright (C) 2009 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.gatein.mop.core.api.workspace.content;
 
 import org.chromattic.api.annotations.FormattedBy;
 import org.chromattic.api.annotations.PrimaryType;
 import org.gatein.mop.api.content.Customization;
 import org.gatein.mop.api.content.ContentType;
 import org.gatein.mop.api.workspace.WorkspaceCustomizationContext;
 import org.chromattic.api.annotations.Create;
 import org.chromattic.api.annotations.OneToOne;
 import org.chromattic.api.annotations.OneToMany;
 import org.chromattic.api.annotations.RelatedMappedBy;
 import org.gatein.mop.core.api.MOPFormatter;
 import org.gatein.mop.core.api.workspace.WorkspaceCustomizationContextImpl;
 
 import java.util.Map;
 
 /**
  * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
  * @version $Revision$
  */
 @PrimaryType(name = "mop:customizationcontainer")
 @FormattedBy(MOPFormatter.class)
 public abstract class CustomizationContainer
 {
 
    @OneToOne
   @RelatedMappedBy("customizations")
    public abstract WorkspaceCustomizationContextImpl getOwner();
 
    @OneToMany
    public abstract Map<String, WorkspaceCustomization> getCustomizations();
 
    @Create
    public abstract WorkspaceClone createClone();
 
    @Create
    public abstract WorkspaceSpecialization createSpecialization();
 
    public Customization<?> getCustomization(String name)
    {
       Map<String, WorkspaceCustomization> customizations = getCustomizations();
       return customizations.get(name);
    }
 
    public <S> Customization<S> customize(String name, ContentType<S> contentType, String contentId, S state)
    {
       Map<String, WorkspaceCustomization> contents = getCustomizations();
       WorkspaceClone content = createClone();
       contents.put(name, content);
       content.setContentId(contentId);
       content.setMimeType(contentType.getMimeType());
       content.setState(state);
       return (Customization<S>)content;
    }
 
    public <S> Customization<S> customize(String name, Customization<S> customization)
    {
       Map<String, WorkspaceCustomization> contents = getCustomizations();
       WorkspaceCustomization workspaceCustomization = (WorkspaceCustomization)customization;
       WorkspaceSpecialization content = createSpecialization();
       contents.put(name, content);
       content.setMimeType(workspaceCustomization.getMimeType());
       content.setContentId(workspaceCustomization.getContentId());
       content.setCustomization(workspaceCustomization);
       return (Customization<S>)content;
    }
 
    public String nameOf(Customization customization)
    {
       if (customization instanceof WorkspaceClone)
       {
          WorkspaceClone wc = (WorkspaceClone)customization;
          Map<String, WorkspaceCustomization> contents = getCustomizations();
          if (contents.containsValue(wc))
          {
             return wc.getFooName();
          }
       }
       return null;
    }
 }
