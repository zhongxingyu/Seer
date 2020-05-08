 /**
  * <copyright>
  * 
  * Copyright (c) 2010-2012 Thales Global Services S.A.S.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Thales Global Services S.A.S. - initial API and implementation
  * 
  * </copyright>
  */
 package org.eclipse.emf.diffmerge.ui.gmf;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
 import org.eclipse.emf.diffmerge.gmf.GMFScope;
 import org.eclipse.emf.diffmerge.ui.specification.IModelScopeDefinition;
 import org.eclipse.emf.diffmerge.ui.specification.ext.FileScopeDefinition;
 import org.eclipse.emf.diffmerge.ui.specification.ext.FileScopeDefinitionFactory;
 import org.eclipse.emf.edit.domain.EditingDomain;
 
 
 /**
  * A factory for model scopes within GMF models.
  * @author Olivier Constant
  */
 public class GMFScopeDefinitionFactory extends FileScopeDefinitionFactory {
   
   /**
   * @see org.eclipse.emf.diffmerge.ui.specification.ext.FileScopeDefinitionFactory#createScopeDefinitionFromURI(org.eclipse.emf.common.util.URI, java.lang.String, boolean)
    */
   @Override
  protected IModelScopeDefinition createScopeDefinitionFromURI(URI uri_p, String label_p,
       boolean editable_p) {
     return new FileScopeDefinition(uri_p, label_p, editable_p) {
       /**
        * @see org.eclipse.emf.diffmerge.ui.specification.ext.FileScopeDefinition#createScope(org.eclipse.emf.edit.domain.EditingDomain)
        */
       @Override
       public IEditableModelScope createScope(EditingDomain domain_p) {
         return new GMFScope(getEntrypointResource(domain_p));
       }
     };
   }
   
   /**
    * @see org.eclipse.emf.diffmerge.ui.specification.AbstractScopeDefinitionFactory#getOverridenClasses()
    */
   @Override
   public Collection<? extends Class<?>> getOverridenClasses() {
     return Collections.<Class<?>>singleton(FileScopeDefinitionFactory.class);
   }
   
 }
