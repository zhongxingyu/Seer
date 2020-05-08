 package org.strategoxt.imp.testing.views;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.internal.junit.ui.JUnitProgressBar;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.part.ViewPart;
 import org.strategoxt.imp.runtime.EditorState;
 import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
 import org.strategoxt.imp.testing.model.TestRun;
 import org.strategoxt.imp.testing.model.TestcaseRun;
 import org.strategoxt.imp.testing.model.TestsuiteRun;
 
 
 public class TestRunViewPart extends ViewPart {
 
     private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
     private TestRun testrun = null;
     private Label lblRatio;
     private JUnitProgressBar pb;
     private TreeViewer treeViewer;
 	private int nrFailedTests = 0;
     
     public TestRunViewPart() {
     	
     }
     
     /**
      * Create contents of the view part.
      * @param parent
      */
     @Override
     public void createPartControl(Composite parent) {
         parent.setLayout(new FormLayout());
        
         pb = new JUnitProgressBar(parent);
         FormData fd_pb = new FormData();
         fd_pb.left = new FormAttachment(0, 10);
         fd_pb.top = new FormAttachment(0, 10);
         pb.setLayoutData(fd_pb);
         toolkit.adapt(pb, true, true);
        
         Label lblTests = new Label(parent, SWT.NONE);
         fd_pb.right = new FormAttachment(100, -100);
         FormData fd_lblTests = new FormData();
         fd_lblTests.top = new FormAttachment(0, 11);
         fd_lblTests.right = new FormAttachment(100, -65);
         lblTests.setLayoutData(fd_lblTests);
         toolkit.adapt(lblTests, true, true);
         lblTests.setText("Tests");
        
         lblRatio = new Label(parent, SWT.NONE);
         lblRatio.setAlignment(SWT.LEFT);
         lblRatio.setSize(55, 25);
         FormData fd_lblRatio = new FormData();
         fd_lblRatio.right = new FormAttachment(100, -10);
         fd_lblRatio.top = new FormAttachment(0, 11);
         lblRatio.setLayoutData(fd_lblRatio);
         toolkit.adapt(lblRatio, true, true);
        
         treeViewer = new TreeViewer(parent, SWT.BORDER);
         Tree tv = treeViewer.getTree();
         FormData fd_tv = new FormData();
         fd_tv.bottom = new FormAttachment(100, -10);
         fd_tv.top = new FormAttachment(0, 35);
         fd_tv.left = new FormAttachment(0, 10);
         fd_tv.right = new FormAttachment(100, -10);
         tv.setLayoutData(fd_tv);
         toolkit.paintBordersFor(tv);
 
 		TreeColumn column = new TreeColumn(treeViewer.getTree(),SWT.NONE);
 		column.setWidth(10);
 		column.setText("");
 		
         treeViewer.setContentProvider(new TestRunContentProvider());
         treeViewer.setLabelProvider(new TestRunLabelProvider());
         treeViewer.setSorter(new ViewerSorter());
         treeViewer.addDoubleClickListener(new IDoubleClickListener() {
 			
 			public void doubleClick(DoubleClickEvent event) {
 				Object selectObject = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
 
 				String file = null;
 				int offset = 0;
 				
 				if (selectObject instanceof TestcaseRun) {
 					TestcaseRun tcr = (TestcaseRun) selectObject ;
 					file = tcr.getParent().getName();
 					offset = tcr.getOffset();
 				} else if(selectObject instanceof TestsuiteRun) {
 					file = ((TestsuiteRun)selectObject).getName();
					offset = 1;
 				}
 
 				if(file != null) {
 					File f = new File(file);
 					IResource res;
 					try {
 						res = EditorIOAgent.getResource(f);
 						EditorState.asyncOpenEditor(Display.getDefault(), (IFile)res, offset, true);
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		});
         
         createActions();
         initializeToolBar();
         initializeMenu();
         
         reset();
 
 		testrun = new TestRun();
 
 		treeViewer.expandAll();
         
         updateHeader();
     }
     
     private void updateHeader() {
     	int nrTests = testrun.getNrTests();
     	if(testrun == null) {
     		lblRatio.setText("0 / 0");
     	} else {
     		lblRatio.setText(String.format("%d / %d    ", (nrTests-nrFailedTests), nrTests));
     	}
         pb.setMaximum(nrTests);
     }
     
 	public void dispose() {
         toolkit.dispose();
         super.dispose();
     }
 
     /**
      * Create the actions.
      */
     private void createActions() {
         // Create the actions
     }
 
     /**
      * Initialize the toolbar.
      */
     private void initializeToolBar() {
     }
 
     /**
      * Initialize the menu.
      */
     private void initializeMenu() {
     }
 
     @Override
     public void setFocus() {
     }
     
     public void reset() {
 		nrFailedTests = 0;
     	testrun = new TestRun();
 		treeViewer.setInput(testrun);
 		pb.reset();
     }
     
     public void refresh() {
     	updateHeader();
     	treeViewer.refresh();
     	treeViewer.expandAll();
     }
     
     public void addTestsuite(String name) {
     	testrun.addTestsuite(name);
     	refresh();
     }
 
     public void addTestcase(String testsuite, String description, int offset) {
     	TestsuiteRun ts = testrun.getTestsuite(testsuite);
     	ts.addTestCase(description, offset);
     	refresh();
     }
     
     public void startTestcase(String testsuite, String description) {
     	TestcaseRun tcr = testrun.getTestsuite(testsuite).getTestcase(description);
     	tcr.start();
     }
 
     public void finishTestcase(String testsuite, String description, boolean succeeded) {
     	TestcaseRun tcr = testrun.getTestsuite(testsuite).getTestcase(description);
     	tcr.finished(succeeded);
     	if(!succeeded) 
     		nrFailedTests++;
     	pb.step(nrFailedTests);
     	refresh();
     }
 }
