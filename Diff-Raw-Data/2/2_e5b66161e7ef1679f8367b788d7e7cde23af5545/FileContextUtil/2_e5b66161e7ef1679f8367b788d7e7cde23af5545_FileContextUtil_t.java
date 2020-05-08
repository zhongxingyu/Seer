 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.designtime.internal.provisional.symbols;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.internal.provisional.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.internal.provisional.IWorkspaceContextResolver;
 import org.eclipse.jst.jsf.context.structureddocument.internal.provisional.IStructuredDocumentContext;
 
 /**
  * Utility for deriving an IFile from an IAdaptable
  * 
  * @author cbateman
  *
  */
 public final class FileContextUtil 
 {
     /**
      * @param context
      * @return an IFile derived from context or null if not derivable
      */
     public static IFile deriveIFileFromContext(IAdaptable context)
     {
         // 
         if (context instanceof IFile)
         {
             return (IFile) context;
         }
         else if (context.getAdapter(IFile.class) != null)
         {
             return (IFile) context.getAdapter(IFile.class);
         }
         else if (context instanceof IStructuredDocumentContext)
         {
            return deriveIFileFromContext((IStructuredDocumentContext)context);
         }
         else
         {
             IStructuredDocumentContext  sdContext = 
                 (IStructuredDocumentContext) context.getAdapter(IStructuredDocumentContext.class);
             
             if (sdContext != null)
             {
                 return deriveIFileFromContext(sdContext);
             }
         }
  
         return null;
     }
     
     private static IFile deriveIFileFromContext(IStructuredDocumentContext context)
     {
         IWorkspaceContextResolver resolver = 
             IStructuredDocumentContextResolverFactory.
                 INSTANCE.
                     getWorkspaceContextResolver(context);
         
         if (resolver != null)
         {
             IResource res = resolver.getResource();
             
             if (res instanceof IFile)
             {
                 return (IFile) res;
             }
         }
         
         return null;
     }
 }
