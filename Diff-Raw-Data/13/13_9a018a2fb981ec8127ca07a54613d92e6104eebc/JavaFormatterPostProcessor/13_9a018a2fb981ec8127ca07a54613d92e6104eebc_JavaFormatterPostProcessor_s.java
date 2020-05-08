 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2t.java.core.internal.postprocessors;
 
 import java.io.StringWriter;
 
 import net.atos.optimus.common.tools.jdt.XACodeFormatter;
 import net.atos.optimus.m2t.java.core.FileHandler;
 import net.atos.optimus.m2t.java.core.IPostProcessor;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 
 /**
  * Post process that formats the source code. This has been implemented this
  * way, since the modisco formatter always formats the whole destination folder
  * (which may contain elements that were not generated this time), and with the
  * default formatter! (not the project one)
  * 
  * @author Maxence Vanbésien (mvaawl@gmail.com)
  * @since 1.0
  * 
  */
 public class JavaFormatterPostProcessor implements IPostProcessor {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
	 * net.atos.optimus.m2t.java.core.IPostProcessor#beforeWrite(net.atos.xa.optimus
	 * .m2t.java.FileHandler)
 	 */
 	@Override
 	public void beforeWrite(FileHandler fileHandler) {
 
 		// Retrieve the java project that will contain the source
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IPath filePath = new Path(fileHandler.getFilePath()).makeRelativeTo(root.getLocation());
 		IFile file = root.getFile(filePath);
 		IProject project = file.getProject();
 		IJavaProject javaProject = JavaCore.create(project);
 
 		// Formats the contents (delegates to XA Common)
 		String contents = fileHandler.getWriter().toString();
 		String newContents = XACodeFormatter.format(contents, javaProject);
 
 		// If formatting led to modification, update the file handler with new
 		// source.
 		if (!contents.equals(newContents)) {
 			StringWriter newWriter = new StringWriter(newContents.length());
 			newWriter.append(newContents);
 			fileHandler.setWriter(newWriter);
 		}
 
 	}
 
 }
