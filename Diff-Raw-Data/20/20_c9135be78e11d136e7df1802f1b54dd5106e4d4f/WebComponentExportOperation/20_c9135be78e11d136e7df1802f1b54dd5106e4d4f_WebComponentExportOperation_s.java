 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.archive.operations;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchiveFactory;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchivePackage;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.SaveFailureException;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.JavaEEArchiveUtilities;
 import org.eclipse.jst.j2ee.internal.archive.operations.AppClientArchiveOpsResourceHandler;
 import org.eclipse.jst.j2ee.internal.archive.operations.J2EEArtifactExportOperation;
 import org.eclipse.jst.jee.archive.IArchive;
 import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 public class WebComponentExportOperation extends J2EEArtifactExportOperation {
 
 	public WebComponentExportOperation() {
 		super();
 	}
 
 	public WebComponentExportOperation(IDataModel model) {
 		super(model);
 	}
 
 	protected void export() throws SaveFailureException, CoreException, InvocationTargetException, InterruptedException {
 		IProgressMonitor subMonitor = new SubProgressMonitor(progressMonitor, EXPORT_WORK);
 		IArchive archiveFromComponent = null;
 		try {
 			archiveFromComponent = JavaEEArchiveUtilities.INSTANCE.openArchive(getComponent());
 			JavaEEQuickPeek quickPeek = JavaEEArchiveUtilities.INSTANCE.getJavaEEQuickPeek(archiveFromComponent);
 			if (quickPeek.getJavaEEVersion() == J2EEConstants.JEE_5_0_ID) {
 				saveArchive(archiveFromComponent, getDestinationPath().toOSString(), subMonitor);
 			} else {
 				CommonarchiveFactory caf = ((CommonarchivePackage) EPackage.Registry.INSTANCE.getEPackage(CommonarchivePackage.eNS_URI)).getCommonarchiveFactory();
 				WebComponentLoadStrategyImpl ls = new WebComponentLoadStrategyImpl(getComponent());
 				ls.setExportSource(isExportSource());
 				setModuleFile(caf.openWARFile(ls, getDestinationPath().toOSString()));
 				ls.setProgressMonitor(subMonitor);
 				getModuleFile().saveAsNoReopen(getDestinationPath().toOSString());
 			}
			
 		} catch (SaveFailureException ex) {
 			throw ex;
 		} catch (Exception e) {
 			throw new SaveFailureException(AppClientArchiveOpsResourceHandler.ARCHIVE_OPERATION_OpeningArchive, e);
 		} finally {
 			if (archiveFromComponent != null)
 				JavaEEArchiveUtilities.INSTANCE.closeArchive(archiveFromComponent);
 
 			subMonitor.done();
 		}
 	}
 
 	protected String archiveString() {
 		return "War File";
 	}
 
 }
