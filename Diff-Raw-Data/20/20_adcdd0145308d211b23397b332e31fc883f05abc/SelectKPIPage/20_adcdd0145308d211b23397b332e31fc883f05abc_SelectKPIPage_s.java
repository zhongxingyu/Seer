 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.wizards.pages;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.amanzi.awe.views.kpi.KPIPlugin;
 import org.amanzi.awe.wizards.kpi.report.KPIReportWizard;
 import org.amanzi.awe.wizards.utils.ScriptUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.jruby.Ruby;
 import org.jruby.runtime.builtin.IRubyObject;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author Pechko_E
  * @since 1.0.0
  */
 public class SelectKPIPage extends WizardPage {
     private static final Logger LOGGER = Logger.getLogger(SelectKPIPage.class);
     private Button btnDefaultKPI;
     private Button btnCustomKPI;
     private Combo cmbSelectGroup;
     private Combo cmbSelectVendor;
     private Combo cmbSelectKPI;
     private Ruby ruby;
     protected String parameter;
     protected boolean needsAggregation;
 
     public SelectKPIPage(String pageName) {
         super(pageName);
         setTitle("Select KPI");
         ruby = KPIPlugin.getDefault().getRubyRuntime();
     }
 
     @Override
     public void createControl(Composite parent) {
         Composite container = new Composite(parent, SWT.NONE);
         container.setLayout(new FormLayout());
 
         Group group = new Group(container, SWT.NONE);
         group.setText("Parameters:");
 
         FormData layoutData = new FormData();
         group.setLayout(new GridLayout());
         layoutData.top = new FormAttachment(0, 2);
         layoutData.left = new FormAttachment(0, 2);
         layoutData.right = new FormAttachment(100, -2);
         group.setLayoutData(layoutData);
 
         btnDefaultKPI = new Button(group, SWT.RADIO);
         btnDefaultKPI.setText("Use default KPIs");
         btnDefaultKPI.setSelection(true);
 
         btnCustomKPI = new Button(group, SWT.RADIO);
         btnCustomKPI.setText("Use custom KPIs");
         btnCustomKPI.setEnabled(false);
 
         Label lblSelectOperator = new Label(container, SWT.LEFT);
         lblSelectOperator.setText("Select vendor/operator:");
         layoutData = new FormData();
         layoutData.top = new FormAttachment(group, 2);
         layoutData.left = new FormAttachment(0, 2);
         layoutData.right = new FormAttachment(20, -2);
         lblSelectOperator.setLayoutData(layoutData);
 
         cmbSelectVendor = new Combo(container, SWT.READ_ONLY);
         layoutData = new FormData();
         layoutData.top = new FormAttachment(group, 2);
         layoutData.left = new FormAttachment(lblSelectOperator, 2);
         layoutData.right = new FormAttachment(100, -2);
         cmbSelectVendor.setLayoutData(layoutData);
         cmbSelectVendor.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateGroups();
                 updatePageComplete(false);
             }
 
         });
 
         Label lblSelectGroup = new Label(container, SWT.LEFT);
         lblSelectGroup.setText("Select group:");
         layoutData = new FormData();
         layoutData.top = new FormAttachment(cmbSelectVendor, 2);
         layoutData.left = new FormAttachment(0, 2);
         layoutData.right = new FormAttachment(20, -2);
         lblSelectGroup.setLayoutData(layoutData);
 
         cmbSelectGroup = new Combo(container, SWT.READ_ONLY);
         layoutData = new FormData();
         layoutData.top = new FormAttachment(cmbSelectVendor, 2);
         layoutData.left = new FormAttachment(lblSelectGroup, 2);
         layoutData.right = new FormAttachment(100, -2);
         cmbSelectGroup.setLayoutData(layoutData);
         cmbSelectGroup.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateKPIs();
                 updatePageComplete(false);
             }
 
         });
 
         Label lblSelectKPI = new Label(container, SWT.LEFT);
         lblSelectKPI.setText("Select KPI:");
         layoutData = new FormData();
         layoutData.top = new FormAttachment(cmbSelectGroup, 2);
         layoutData.left = new FormAttachment(0, 2);
         layoutData.right = new FormAttachment(20, -2);
         lblSelectKPI.setLayoutData(layoutData);
 
         cmbSelectKPI = new Combo(container, SWT.READ_ONLY);
         layoutData = new FormData();
         layoutData.top = new FormAttachment(cmbSelectGroup, 2);
         layoutData.left = new FormAttachment(lblSelectKPI, 2);
         layoutData.right = new FormAttachment(100, -2);
         cmbSelectKPI.setLayoutData(layoutData);
         cmbSelectKPI.addSelectionListener(new SelectionAdapter() {
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 try {
                     try {
                        URL entry = Platform.getBundle(KPIPlugin.PLUGIN_ID).getEntry(KPIPlugin.RUBY_FOLDER);
                        String file = FileLocator.resolve(entry).getFile();
                         LOGGER.debug(file);
                        String scriptText = "IO.readlines(\"" + file + File.separator + cmbSelectVendor.getText().toLowerCase()
                                + ".rb\").to_s";
                         IRubyObject result = ruby.evalScriptlet(scriptText);
                         LOGGER.debug("result: " + result);
                         String res = result.asJavaString();
                         Matcher matcher = Pattern.compile(
                                 "(?<=def\\s" + cmbSelectGroup.getText() + "\\." + cmbSelectKPI.getText() + ")\\((.*)\\)").matcher(
                                 res);
                         if (matcher.find()) {
                             List<String> parameters = Arrays.asList(matcher.group(1).split(","));
                             for (String param : parameters) {
                                 if (param.matches("counters")) {
                                     parameter = "counters";
                                 } else if (param.matches("drive")) {
                                     parameter = "drive";
                                 } else if (param.matches("network")) {
                                     parameter = "network";
                                 } else if (param.matches("aggregation.*")) {
                                     needsAggregation = true;
                                 }
                             }
                         }
                         updatePageComplete(true);
                     } catch (Exception e1) {
                         // TODO Handle IOException
                         throw (RuntimeException)new RuntimeException().initCause(e1);
                     }
                 } catch (RuntimeException e1) {
                     // TODO Handle RuntimeException
                     throw (RuntimeException)new RuntimeException().initCause(e1);
                 }
             }
 
         });
 
         // TODO implement
         Button btnStoreResult = new Button(container, SWT.CHECK);
         btnStoreResult.setText("Save result to database");
         btnStoreResult.setEnabled(false);
         layoutData = new FormData();
         layoutData.top = new FormAttachment(cmbSelectKPI, 2);
         layoutData.left = new FormAttachment(0, 2);
         layoutData.right = new FormAttachment(100, -2);
         btnStoreResult.setLayoutData(layoutData);
 
         setPageComplete(false);
         setControl(container);
     }
 
     protected void updatePageComplete(boolean complete) {
         setPageComplete(complete);
         if (complete) {
             KPIReportWizard wiz = (KPIReportWizard)getWizard();
             String selectedKpiMethodName = getSelectedKpiMethodName();
             StringBuffer kpiSignature = new StringBuffer(selectedKpiMethodName);
             kpiSignature.append("(%s");
             if (needsAggregation) {
                 kpiSignature.append(",:%s");
             }
             kpiSignature.append(")");
             wiz.setSelectedKPI(selectedKpiMethodName);
             wiz.setKpiScript(kpiSignature.toString());
             wiz.setDatasetType(parameter);
             wiz.setNeedsAggregation(needsAggregation);
         }
     }
 
     @Override
     public void setVisible(boolean visible) {
         updateVendors();
         super.setVisible(visible);
     }
 
     protected void updateKPIs() {
         String fullModuleName = getFullGroupModuleName();
         String[] availableMethods = ScriptUtils.getAvailableMethods(ruby, fullModuleName);
         cmbSelectKPI.setItems(availableMethods);
     }
 
     private String getFullGroupModuleName() {
         return getFullVendorModuleName() + "::" + cmbSelectGroup.getText();
     }
 
     public String getSelectedKpiMethodName() {
         return getFullGroupModuleName() + "." + cmbSelectKPI.getText();
     }
 
     private String getFullVendorModuleName() {
         if (btnDefaultKPI.getSelection()) {
             return KPIPlugin.DEFAULT_KPI_RUBY_MODULE + "::" + cmbSelectVendor.getText();
         } else {
             return KPIPlugin.CUSTOM_KPI_RUBY_MODULE + "::" + cmbSelectVendor.getText();
         }
     }
 
     protected void updateGroups() {
         String[] availableModules = ScriptUtils.getAvailableModules(ruby, getFullVendorModuleName());
         cmbSelectGroup.setItems(availableModules);
         cmbSelectKPI.removeAll();
     }
 
     private void updateVendors() {
         String[] availableModules = ScriptUtils.getAvailableModules(ruby, KPIPlugin.DEFAULT_KPI_RUBY_MODULE);
         cmbSelectVendor.setItems(availableModules);
         cmbSelectGroup.removeAll();
         cmbSelectKPI.removeAll();
     }
 
 }
