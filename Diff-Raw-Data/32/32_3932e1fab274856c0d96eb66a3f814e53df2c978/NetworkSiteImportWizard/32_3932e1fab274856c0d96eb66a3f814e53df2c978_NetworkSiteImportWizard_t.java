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
 
 package org.amanzi.neo.wizards;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.NetworkFileType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.loader.NeighbourLoader;
 import org.amanzi.neo.loader.NetworkLoader;
 import org.amanzi.neo.loader.NetworkSiteLoader;
 import org.amanzi.neo.loader.ProbeLoader;
 import org.amanzi.neo.loader.TransmissionLoader;
import org.amanzi.neo.loader.internal.NeoLoaderPluginMessages;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 
 /**
  * <p>
  * Network import wizard
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class NetworkSiteImportWizard extends Wizard implements IImportWizard {
 
    private static final String PAGE_TITLE = NeoLoaderPluginMessages.NetworkSiteImportWizard_PAGE_TITLE;
    private static final String PAGE_DESCR = NeoLoaderPluginMessages.NetworkSiteImportWizard_PAGE_DESCR;
     private NetworkSiteImportWizardPage mainPage;
     private Display display;
 
     @Override
     public boolean performFinish() {
         Job job = new Job("Load Network Site'" + (new File(mainPage.getFileName())).getName() + "'") {
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 NetworkFileType fileType = mainPage.getFileType();
                 try {
                     switch (fileType) {
                     case RADIO_SITE:
                         NetworkSiteLoader networkSiteLoader = new NetworkSiteLoader(mainPage.getNetworkName(), mainPage.getFileName(), display);
                         networkSiteLoader.run(monitor);
                         networkSiteLoader.printStats(false);
                         break;
                     case RADIO_SECTOR:
                         NetworkLoader networkLoader = new NetworkLoader(mainPage.getNetworkName(), mainPage.getFileName(), display);
                         networkLoader.setup();
                         SubMonitor monitor2 = SubMonitor.convert(monitor, 100);
                         networkLoader.run(monitor2);
                         networkLoader.printStats(false);
                         NetworkLoader.addDataToCatalog();
                         networkLoader.addLayersToMap();
                         break;
                     case PROBE:
                         ProbeLoader loader = new ProbeLoader(mainPage.getNetworkName(), mainPage.getFileName(), display);
                         loader.run(monitor);
                         NetworkLoader.addDataToCatalog();
                         loader.addLayersToMap();                        
                         break;
                     case NEIGHBOUR:
                         NeighbourLoader neighbourLoader;
                         neighbourLoader = new NeighbourLoader(mainPage.getNetworkNode(), mainPage.getFileName(), NeoServiceProvider.getProvider().getService());
                         neighbourLoader.run(monitor);
                         NeoServiceProvider.getProvider().commit();
                         break;
                     case TRANSMISSION:
                         TransmissionLoader transmissionLoader;
                         transmissionLoader = new TransmissionLoader(mainPage.getNetworkName(), mainPage.getFileName(), NeoServiceProvider.getProvider().getService());
                         transmissionLoader.run(monitor);
                         NeoServiceProvider.getProvider().commit();
                         break;
                     default:
                         break;
 
                     }
                 } catch (IOException e) {
                     NeoCorePlugin.error("Error loading Network file", e);
                     return new Status(Status.ERROR, "org.amanzi.neo.loader", e.getMessage());
                 }
                 return Status.OK_STATUS;
             }
         };
         job.schedule(50);
         return true;
     }
 
     @Override
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         mainPage = new NetworkSiteImportWizardPage(PAGE_TITLE, PAGE_DESCR);
         display = workbench.getDisplay();
     }
 
     @Override
     public void addPages() {
         super.addPages();
         addPage(mainPage);
     }
 }
