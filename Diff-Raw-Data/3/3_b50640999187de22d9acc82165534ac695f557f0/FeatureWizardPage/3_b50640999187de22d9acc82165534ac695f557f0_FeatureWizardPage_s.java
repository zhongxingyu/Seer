 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk.features;
 
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.widgets.Composite;
 import org.nuxeo.ide.common.forms.Form;
 import org.nuxeo.ide.common.wizards.FormWizardPage;
 import org.nuxeo.ide.sdk.features.automation.OperationWizard;
 import org.nuxeo.ide.sdk.ui.NuxeoNature;
 import org.nuxeo.ide.sdk.ui.widgets.ObjectChooser;
 import org.nuxeo.ide.sdk.ui.widgets.PackageChooser;
 import org.nuxeo.ide.sdk.ui.widgets.PackageChooserWidget;
 import org.nuxeo.ide.sdk.ui.widgets.ProjectChooser;
 import org.nuxeo.ide.sdk.ui.widgets.ProjectChooserWidget;
 
 /**
  * This page must use a 'project' and 'package' form widget having the ID
  * 'project' and 'package'. The page is correctly initializing the
  * project/package chooser widgets and updating the template context with the
  * selected values.
  * 
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public abstract class FeatureWizardPage extends
         FormWizardPage<FeatureTemplateContext> {
 
     public FeatureWizardPage(String pageName, String title,
             ImageDescriptor image) {
         super(pageName, title, image);
     }
 
     @Override
     public Form createForm() {
         Form form = super.createForm();
         form.addWidgetType(PackageChooserWidget.class);
         form.addWidgetType(ProjectChooserWidget.class);
         return form;
     }
 
     @Override
     public void createControl(Composite parent) {
         super.createControl(parent);
         ProjectChooser projChooser = (ProjectChooser) form.getWidgetControl("project");
         final PackageChooser pkgChooser = (PackageChooser) form.getWidgetControl("package");
         OperationWizard wiz = (OperationWizard) getWizard();
         IJavaProject project = wiz.getSelectedNuxeoProject();
         projChooser.setNature(NuxeoNature.ID);
         if (project != null) {
             projChooser.setValue(project);
             pkgChooser.setProject(project);
         }
        pkgChooser.setValue(wiz.getSelectedPackageFragment());
         projChooser.addValueChangedListener(new ObjectChooser.ValueChangedListener<IJavaProject>() {
             @Override
             public void valueChanged(ObjectChooser<IJavaProject> source,
                     IJavaProject oldValue, IJavaProject newValue) {
                 pkgChooser.setProject(newValue);
             }
         });
 
         // projChooser.addValueChangedListener(new
         // ValueChangedListener<IJavaProject>() {
         // @Override
         // public void valueChanged(ObjectChooser<IJavaProject> source,
         // IJavaProject oldValue, IJavaProject newValue) {
         // if (newValue != null && !newValue.exists()) {
         // return
         // }
         // }
         // });
     }
 
     @Override
     public void update(FeatureTemplateContext ctx) {
         IJavaProject project = (IJavaProject) ((ProjectChooserWidget) form.getWidget("project")).getValue();
         ctx.setProject(project);
         ctx.setPackage(((PackageChooserWidget) form.getWidget("package")).getValueAsString());
         String className = form.getWidgetValueAsString("className");
         ctx.setClassName(className);
     }
 
 }
