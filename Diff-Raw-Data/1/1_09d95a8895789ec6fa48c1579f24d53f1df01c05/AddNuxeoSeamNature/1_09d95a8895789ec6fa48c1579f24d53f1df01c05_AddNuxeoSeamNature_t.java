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
 package org.nuxeo.ide.sdk.ui.actions;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * Fake add nature - used as an example
  * 
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class AddNuxeoSeamNature extends AddNuxeoNature {
 
     public AddNuxeoSeamNature() {
     }
 
     @Override
     public void install(IProject project, String natureId,
             IProgressMonitor monitor) throws CoreException {
         super.install(project, natureId, monitor);
     }
 
     @Override
     protected void createSourceFolders(IProject project,
             IProgressMonitor monitor) throws CoreException {
         super.createSourceFolders(project, monitor);
         createSourceFolder(project, "src/main/seam", monitor);
     }
 }
