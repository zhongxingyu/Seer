 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.eclipse.ui.views;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import com.google.jstestdriver.TestResult;
 import com.google.jstestdriver.eclipse.ui.launch.model.EclipseJstdTestResult;
 import com.google.jstestdriver.eclipse.ui.launch.model.EclipseJstdTestRunResult;
 import com.google.jstestdriver.eclipse.ui.launch.model.ResultModel;
 
 /**
  * Show the test results.
  * 
  * @author shyamseshadri@google.com (Shyam Seshadri)
  */
 public class TestResultsPanel extends Composite {
 
   private EclipseJstdTestRunResult testRunResult;
   private TreeViewer testResultsTree;
   private TestResultProgressBar testProgressIndicator;
   private Label failuresLabel;
   private Label errorsLabel;
   private Label totalRunLabel;
   private Text testDetailsText;
   private int totalNumTests;
   private ILaunchConfiguration lastLaunchConfiguration;
 
   public TestResultsPanel(Composite parent, int style) {
     super(parent, style);
     setLayout(new GridLayout(3, true));
     GridData layoutData = new GridData();
     layoutData.grabExcessHorizontalSpace = true;
     layoutData.grabExcessVerticalSpace = true;
     layoutData.verticalAlignment = SWT.FILL;
     layoutData.horizontalAlignment = SWT.FILL;
     setLayoutData(layoutData);
     testRunResult = new EclipseJstdTestRunResult();
 
     GridData totalRunLabelData = new GridData();
     totalRunLabelData.grabExcessHorizontalSpace = true;
     totalRunLabelData.horizontalAlignment = SWT.FILL;
     totalRunLabel = new Label(this, SWT.CENTER);
     totalRunLabel.setText("Run : 0 / 0");
     totalRunLabel.setLayoutData(totalRunLabelData);
     GridData otherLabelData = new GridData();
     otherLabelData.horizontalAlignment = SWT.FILL;
     errorsLabel = new Label(this, SWT.CENTER);
     errorsLabel.setText("Errors : 0");
     errorsLabel.setLayoutData(otherLabelData);
     failuresLabel = new Label(this, SWT.CENTER);
     failuresLabel.setText("Failed : 0");
     failuresLabel.setLayoutData(otherLabelData);
     
     GridData progressIndicatorData = new GridData();
     progressIndicatorData.horizontalSpan = 4;
     progressIndicatorData.horizontalAlignment = SWT.FILL;
     progressIndicatorData.verticalAlignment = SWT.FILL;
     progressIndicatorData.heightHint = 20;
     progressIndicatorData.minimumWidth = 180;
     testProgressIndicator = new TestResultProgressBar(this, SWT.NONE);
     testProgressIndicator.setLayoutData(progressIndicatorData);
     
     SashForm resultsSashForm = new SashForm(this, SWT.VERTICAL);
     GridData sashFormData = new GridData();
     sashFormData.horizontalSpan = 3;
     sashFormData.horizontalAlignment = SWT.FILL;
     sashFormData.grabExcessHorizontalSpace = true;
     sashFormData.grabExcessVerticalSpace = true;
     sashFormData.heightHint = 340;
     sashFormData.verticalAlignment = SWT.FILL;
     resultsSashForm.setLayoutData(sashFormData);
     testResultsTree = new TreeViewer(resultsSashForm, SWT.NONE);
     testResultsTree.getTree().setLayoutData(sashFormData);
     testResultsTree.setLabelProvider(new TestResultsTreeLabelProvider());
     testResultsTree.setContentProvider(new TestResultsTreeContentProvider());
     testResultsTree.setInput(testRunResult);
     
     testResultsTree.addSelectionChangedListener(new ISelectionChangedListener() {
       
       public void selectionChanged(SelectionChangedEvent event) {
         TreeSelection selection = (TreeSelection) event.getSelection();
         if (selection.getFirstElement() instanceof EclipseJstdTestResult) {
           EclipseJstdTestResult result = (EclipseJstdTestResult) selection
           .getFirstElement();
          StringBuilder details = new StringBuilder();
           if (!result.getResult().getParsedMessage().trim().equals("")) {
            details.append("Message : ")
                         .append(result.getResult().getParsedMessage())
                         .append("\n");
           }
           if (!result.getResult().getStack().trim().equals("")) {
            details.append("Stack Trace : ")
                         .append(result.getResult().getStack())
                         .append("\n");
           }
          details.append("Log : ").append(result.getResult().getLog());
          testDetailsText.setText(details.toString());
         } else {
           testDetailsText.setText("");
         }
       }
     });
     
     GridData testDetailsData = new GridData();
     testDetailsData.horizontalSpan = 3;
     testDetailsData.grabExcessHorizontalSpace = true;
     testDetailsData.heightHint = 100;
     testDetailsData.verticalAlignment = SWT.FILL;
     testDetailsData.horizontalAlignment = SWT.FILL;
     testDetailsText = new Text(resultsSashForm, SWT.MULTI | SWT.WRAP);
     testDetailsText.setLayoutData(testDetailsData);
   }
 
   public ILaunchConfiguration getLastLaunchConfiguration() {
     return lastLaunchConfiguration;
   }
   
   public void setupForNextTestRun(ILaunchConfiguration launchConfiguration) {
     totalNumTests = 0;
     totalRunLabel.setText("Run : 0 / 0");
     errorsLabel.setText("Errors : 0");
     failuresLabel.setText("Failed : 0");
     lastLaunchConfiguration = launchConfiguration;
     testRunResult.clear();
     testResultsTree.refresh();
     testProgressIndicator.reset();
     update();
   }
   
   public synchronized void addNumberOfTests(int numTests) {
     totalNumTests += numTests;
     testProgressIndicator.setMax(totalNumTests);
     totalRunLabel.setText("Run : 0 / " + totalNumTests);
   }
 
   public synchronized void addTestResults(Collection<TestResult> testResults) {
     Collection<ResultModel> failedTests = new ArrayList<ResultModel>();
     for (TestResult result : testResults) {
       ResultModel addedResult = testRunResult.addTestResult(result);
       if (!addedResult.didPass()) {
         failedTests.add(addedResult);
       }
     }
     testProgressIndicator.step(testResults.size(), failedTests.size() == 0);
     testResultsTree.refresh();
     
     for (ResultModel resultModel : failedTests) {
       testResultsTree.expandToLevel(resultModel, TreeViewer.ALL_LEVELS);
     }
     errorsLabel.setText("Errors : " + testRunResult.getNumberOfErrors());
     failuresLabel.setText("Failed : " + testRunResult.getNumberOfFailures());
     totalRunLabel.setText("Run : " + testRunResult.getNumberOfTests() + " / " + totalNumTests);
   }
 
   public void setTreeFilter(ViewerFilter filter) {
     testResultsTree.setFilters(new ViewerFilter[] { filter });
   }
 
   public void clearTreeFilter() {
     testResultsTree.setFilters(new ViewerFilter[0]);
   }
 }
