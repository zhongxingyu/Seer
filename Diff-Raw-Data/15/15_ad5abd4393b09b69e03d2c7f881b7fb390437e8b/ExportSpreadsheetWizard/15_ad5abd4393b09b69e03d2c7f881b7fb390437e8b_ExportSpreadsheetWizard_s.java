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
 
 package org.amanzi.awe.views.calls;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import org.amanzi.awe.views.calls.views.CallAnalyserView.ColumnHeaders;
 import org.amanzi.awe.views.calls.views.CallAnalyserView.PeriodWrapper;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.splash.swing.Cell;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.amanzi.splash.ui.wizards.CreateSpreadsheetOperation;
 import org.amanzi.splash.ui.wizards.SplashNewSpreadsheetWizard;
 import org.amanzi.splash.utilities.NeoSplashUtil;
 import org.amanzi.splash.utilities.SpreadsheetCreator;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.graphdb.Transaction;
 import org.rubypeople.rdt.core.RubyModelException;
 
 /**
 * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class ExportSpreadsheetWizard extends SplashNewSpreadsheetWizard {
     private final List<PeriodWrapper> elements;
     private final List<ColumnHeaders> columnHeaders;
 
     public ExportSpreadsheetWizard(List<PeriodWrapper> elements, List<ColumnHeaders> columnHeaders) {
         super();
         this.elements = elements;
         this.columnHeaders = columnHeaders;
     }
 
     @Override
     protected void doFinish(String containerName, final String fileName, final IProgressMonitor monitor) throws CoreException {
         // create a sample file
         Transaction tx = NeoServiceProvider.getProvider().getService().beginTx();
         final IResource resource;
         try {
             monitor.beginTask("Creating " + fileName, 2);
             IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
             resource = root.findMember(new Path(containerName));
             if (!resource.exists() || !(resource instanceof IContainer)) {
                 throwCoreException("Container \"" + containerName + "\" does not exist.");
             }
             monitor.worked(1);
 
             monitor.setTaskName("Create and import data...");
             SpreadsheetCreator spreadsheetCreator = new SpreadsheetCreator(new Path(containerName), fileName);
             for (int column = 0; column < columnHeaders.size(); column++) {
                 Cell cellToadd = new Cell(0, column, "", columnHeaders.get(column).getName(), null);
                 spreadsheetCreator.saveCell(cellToadd);
             }
             for (int column = 0; column < columnHeaders.size(); column++) {
                 for (int row = 0; row < elements.size(); row++) {
                     Object value = columnHeaders.get(column).getValue(elements.get(row), column);
                     Cell cellToadd = new Cell(row + 1, column, "", value, null);
                     spreadsheetCreator.saveCell(cellToadd);
 
                 }
             }
             tx.success();
         } finally {
             tx.finish();
         }
             // Lagutko 20.07.2009, create Spreadsheet also for EMF structure
             try {
                 final URL spreadsheetURL = NeoSplashUtil.getSpeadsheetURL(fileName);
                 getShell().getDisplay().asyncExec(new Runnable() {
                     public void run() {
                         NeoSplashUtil.openSpreadsheet(PlatformUI.getWorkbench(), spreadsheetURL, resource.getProject().getName());
 
                         // Lagutko, 11.08.2009, put newly created Spreadsheet to Delta
                         CreateSpreadsheetOperation op = new CreateSpreadsheetOperation((IProject)resource, fileName);
                         try {
                             op.runOperation(monitor);
                         } catch (RubyModelException e) {
                             SplashPlugin.error(null, e);
                         }
                     }
                 });
             } catch (MalformedURLException e) {
                 throw new CoreException(new Status(Status.ERROR, SplashPlugin.getId(), null, e));
             }
         monitor.worked(1);
     }
 }
