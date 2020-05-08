 /**
  * Copyright (c) 2010, 2011 Darmstadt University of Technology.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Marcel Bruch - initial API and implementation.
  */
 package org.eclipse.recommenders.internal.codesearch.rcp.views;
 
 import org.apache.lucene.document.Document;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.viewers.ILazyContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.recommenders.codesearch.rcp.index.Fields;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.SearchResult;
 import org.eclipse.recommenders.utils.names.IMethodName;
 import org.eclipse.recommenders.utils.names.ITypeName;
 import org.eclipse.recommenders.utils.names.VmMethodName;
 import org.eclipse.recommenders.utils.names.VmTypeName;
 import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
 import org.eclipse.recommenders.utils.rcp.internal.RecommendersUtilsPlugin;
 
 import com.google.common.base.Optional;
 
 public class LazyContentProvider implements ILazyContentProvider {
 
     private TableViewer viewer;
     private SearchResult input;
 
     @Override
     public void dispose() {
     }
 
     @Override
     public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
         this.viewer = (TableViewer) viewer;
         this.input = (SearchResult) newInput;
     }
 
     @Override
     public void updateElement(final int index) {
         Document doc = null;
         try {
             doc = input.scoreDoc(index);
             final String handle = doc.get(Fields.JAVA_ELEMENT_HANDLE);
             final IJavaElement e = JavaCore.create(handle);
             if (e != null) {
                 viewer.replace(e, index);
                 return;
             }
             // this is needed to handle special cases which are not directly
             // bound to a java element.
             final String docId = doc.get(Fields.QUALIFIED_NAME);
             final String docType = doc.get(Fields.TYPE);
             final String declaringType = doc.get(Fields.DECLARING_TYPE);
             final String declaringMethod = doc.get(Fields.DECLARING_METHOD);
             if (docType.equals(Fields.TYPE_CLASS)) {
                 final ITypeName typeName = VmTypeName.get(docId);
                 final Optional<IType> type = JavaElementResolver.INSTANCE.toJdtType(typeName);
                 if (type.isPresent()) {
                     viewer.replace(type.get(), index);
                 }
             } else if (docType.equals(Fields.TYPE_METHOD)) {
                 final IMethodName methodName = VmMethodName.get(docId);
                 final Optional<IMethod> method = JavaElementResolver.INSTANCE.toJdtMethod(methodName);
                 if (method.isPresent()) {
                     viewer.replace(method.get(), index);
                 }
             } else if (docType.equals(Fields.TYPE_VARUSAGE)) {
                 final IMethodName methodName = VmMethodName.get(declaringMethod);
                 final Optional<IMethod> method = JavaElementResolver.INSTANCE.toJdtMethod(methodName);
                 if (method.isPresent()) {
                     viewer.replace(method.get(), index);
                 }
             } else if (docType.equals(Fields.TYPE_TRYCATCH) || docType.equals(Fields.TYPE_FIELD)) {
                 final ITypeName typeName = VmTypeName.get(declaringType);
                 final Optional<IType> type = JavaElementResolver.INSTANCE.toJdtType(typeName);
                 if (type.isPresent()) {
                     viewer.replace(type.get(), index);
                 }
             }
 
         } catch (final Exception e) {
            RecommendersUtilsPlugin.logError(e, "Failed to determine java element for document '%d'", doc);
         }
     }
 }
