 /*
  * Copyright 2013 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dbflute.emecha.eclipse.plugin.dfassist.link;
 
 import java.util.List;
 
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
 import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 
import frend.org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector;
 
 /**
  * @author schatten
  *
  */
 @SuppressWarnings("restriction")
 public class OutsideSqlHyperlinkDetector extends JavaElementHyperlinkDetector {
     /**
      * @see org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector#addHyperlinks(org.eclipse.jface.text.IRegion, org.eclipse.jdt.ui.actions.SelectionDispatchAction, org.eclipse.jdt.core.IJavaElement, boolean, org.eclipse.ui.texteditor.ITextEditor)
      */
     @Override
     protected void addHyperlinks(List<IHyperlink> hyperlinksCollector, IRegion wordRegion, SelectionDispatchAction openAction, IJavaElement element, boolean qualify, JavaEditor editor) {
         if (element instanceof IType) {
             String packageName = ((IType) element).getPackageFragment().getElementName();
             if (packageName.endsWith(".exbhv.pmbean") || packageName.endsWith(".bsbhv.pmbean")) {
                 SqlFileHyperlink sqlLink = new SqlFileHyperlink(wordRegion, openAction, (IType) element, qualify);
                 if (sqlLink.existSqlFile()) {
                     hyperlinksCollector.add(sqlLink);
                 }
                 CustomizeEntityHyperlink entityLink = new CustomizeEntityHyperlink(wordRegion, openAction, (IType) element, qualify);
                 if (entityLink.existEntityType()) {
                     hyperlinksCollector.add(entityLink);
                 }
             } else if (packageName.endsWith(".exentity.customize") || packageName.endsWith(".bsentity.customize")) {
                 SqlFileHyperlink sqlLink = new SqlFileHyperlink(wordRegion, openAction, (IType) element, qualify);
                 if (sqlLink.existSqlFile()) {
                     hyperlinksCollector.add(sqlLink);
                 }
                 ParameterBeanHyperlink pmbLink = new ParameterBeanHyperlink(wordRegion, openAction, (IType) element, qualify);
                 if (pmbLink.existPmbType()) {
                     hyperlinksCollector.add(pmbLink);
                 }
             }
         }
     }
 }
