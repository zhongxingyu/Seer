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
 package org.nuxeo.ide.sdk.ui;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.dialogs.PropertyPage;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.sdk.NuxeoSDK;
 import org.nuxeo.ide.sdk.index.MavenDownloader;
 import org.nuxeo.ide.sdk.index.MavenDownloader.FileRef;
 import org.nuxeo.ide.sdk.model.Artifact;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class SDKClassPathContainerEntryPage extends PropertyPage {
 
     @Override
     protected Control createContents(Composite parent) {
         final IPackageFragmentRoot root = (IPackageFragmentRoot) getElement().getAdapter(
                 IPackageFragmentRoot.class);
         if (root == null) {
             Label label = new Label(parent, SWT.NONE);
             label.setText("Input si not a classpath entry!");
             return label;
         }
 
         String msg = null;
         try {
             IPath path = root.getSourceAttachmentPath();
             if (path != null) {
                 msg = "Sources are already configured";
             } else {
                 msg = "Souces are not yet configured";
             }
         } catch (JavaModelException e) {
             msg = "Failed to get source status";
         }
         setMessage(msg);
         Composite panel = new Composite(parent, SWT.NONE);
         panel.setLayout(new RowLayout(SWT.VERTICAL));
         Label label = new Label(panel, SWT.NONE);
         label.setText("You can download and configure sources by clicking on 'Download' button");
         Button dwn = new Button(panel, SWT.BORDER);
         dwn.setText("Download");
         dwn.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 Job job = new Job("Download Sources") {
                     @Override
                     protected IStatus run(IProgressMonitor monitor) {
                         monitor.beginTask("Downloading", 1);
                         installSources(root);
                         monitor.worked(1);
                         monitor.done();
                         return Status.OK_STATUS;
                     }
                 };
                 job.setUser(true);
                 job.schedule();
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
 
         return panel;
     }
 
     public void installSources(IPackageFragmentRoot root) {
         NuxeoSDK sdk = NuxeoSDK.getDefault();
         if (sdk == null) {
             UI.showError("No Nuxeo SDK configured. Cannot continue.");
             return;
         }
         Artifact artifact = Artifact.fromJarName(root.getElementName());
         if (artifact == null) {
             UI.showError("Cannot resolve JAR " + root.getElementName()
                     + " to Maven GAV");
             return;
         }
         try {
             FileRef ref = MavenDownloader.downloadSourceJar(artifact);
             if (ref == null) {
                 UI.showError("No sources found for corresponding artifact: "
                         + artifact);
                 return;
             }
             ref.installTo(sdk.getBundlesSrcDir());
             Display.getDefault().asyncExec(new Runnable() {
                 @Override
                 public void run() {
                    NuxeoSDK.reload();
                     setMessage("Sources are configured");
                 }
             });
         } catch (IOException e) {
             UI.showError("Faield to download artifact file", e);
         }
     }
 
 }
