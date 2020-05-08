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
 package org.nuxeo.ide.connect.features.adapter;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.swt.widgets.Composite;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.common.forms.Form;
 import org.nuxeo.ide.connect.ConnectPlugin;
 import org.nuxeo.ide.connect.StudioProjectBinding;
 import org.nuxeo.ide.connect.studio.DocumentSchema;
 import org.nuxeo.ide.sdk.features.FeatureCreationWizard;
 import org.nuxeo.ide.sdk.features.FeatureTemplateContext;
 import org.nuxeo.ide.sdk.features.FeatureWizardPage;
 import org.nuxeo.ide.sdk.ui.widgets.ObjectChooser;
 import org.nuxeo.ide.sdk.ui.widgets.PackageChooserWidget;
 import org.nuxeo.ide.sdk.ui.widgets.ProjectChooser;
 import org.nuxeo.ide.sdk.ui.widgets.ProjectChooserWidget;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class DocumentAdapterWizardPage extends FeatureWizardPage {
 
     public DocumentAdapterWizardPage() {
         super("createDocumentAdapter1", "Create Document Adapter", null);
     }
 
     @Override
     public Form createForm() {
         Form form = super.createForm();
         form.addWidgetType(PackageChooserWidget.class);
         form.addWidgetType(ProjectChooserWidget.class);
         form.addWidgetType(StudioSchemasWidget.class);
         return form;
     }
 
     @Override
     public void createControl(Composite parent) {
         super.createControl(parent);
         ProjectChooser projChooser = (ProjectChooser) form.getWidgetControl("project");
         FeatureCreationWizard wiz = (FeatureCreationWizard) getWizard();
         IJavaProject project = wiz.getSelectedNuxeoProject();
         final StudioSchemasTable table = (StudioSchemasTable) form.getWidgetControl("schemas");
         if (project != null) {
             updateSchemas(table, project.getProject());
         }
         projChooser.addValueChangedListener(new ObjectChooser.ValueChangedListener<IJavaProject>() {
             @Override
             public void valueChanged(ObjectChooser<IJavaProject> source,
                     IJavaProject oldValue, IJavaProject newValue) {
                 updateSchemas(table, newValue.getProject());
             }
         });
 
     }
 
     protected void updateSchemas(StudioSchemasTable table, IProject project) {
         if (project == null) {
             table.setInput(new DocumentSchema[0]);
         } else {
             StudioProjectBinding binding = ConnectPlugin.getStudioProvider().getBinding(
                     project);
             if (binding == null) {
                UI.showWarning("No schemas are available since the project you selected is not nound to Nuxeo Studio!");
             } else {
                 table.setInput(binding.getSchemas());
             }
         }
     }
 
     @Override
     public void update(FeatureTemplateContext ctx) {
         super.update(ctx);
         StudioSchemasTable widget = (StudioSchemasTable) form.getWidgetControl("schemas");
         DocumentSchema[] schemas = widget.getSelectedSchemas();
         // generate fields for selected schemas
         ArrayList<SchemaField> fields = new ArrayList<SchemaField>();
         for (DocumentSchema ds : schemas) {
             for (DocumentSchema.Field f : ds.getFields()) {
                 fields.add(new SchemaField(ds, f));
             }
         }
         ctx.put("fields", fields);
     }
 
 }
