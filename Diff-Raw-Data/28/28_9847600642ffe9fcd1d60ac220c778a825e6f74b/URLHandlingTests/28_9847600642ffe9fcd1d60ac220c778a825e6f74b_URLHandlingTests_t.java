 /*******************************************************************************
  * Copyright (c) 2009 The Eclipse Foundation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.examples.slideshow.jdt;
 
 import static org.junit.Assert.*;
 
 import java.net.URL;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 public class URLHandlingTests {
 	@BeforeClass
 	public static void setup() throws Exception {
 		// TODO This doesn't work. Need to figure out what's missing.
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IProject project = root.getProject("JavaProject");
 		project.create(null);
 		project.open(null);
 		
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(new String[] {JavaCore.NATURE_ID});
 		project.setDescription(description, null);
 		
 		IJavaProject javaProject = JavaCore.create(project);
 		IPackageFragmentRoot sourceFolder = javaProject.getPackageFragmentRoot(project);
 		sourceFolder.open(null);
 		
 		IPackageFragment pack = sourceFolder.createPackageFragment("org.eclipse.test", false, null);
 		pack.open(null);
 		String contents = "package org.eclipse.test;\npublic class Junk {\npublic static void main(String[] args) {}}";
 		pack.createCompilationUnit("Junk.java", contents, false, null);
 		
 		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
 		
//		root.accept(new IResourceVisitor() {
//
//			public boolean visit(IResource resource) throws CoreException {
//				System.out.println(resource);
//				return true;
//			}
//			
//		});
 	}
 	
 	@Test
 	public void testParseJavaURLWithoutMethod() throws Exception {
 		URL url = new URL("java://JavaProject/org.eclipse.tests.Junk");
 		assertEquals("java", url.getProtocol());
 		assertEquals("JavaProject", url.getHost());
 		assertEquals("/org.eclipse.tests.Junk", url.getPath());
 		assertNull(url.getQuery());
 	}
 	
 	@Test
 	public void testParseJavaURLWithMethod() throws Exception {
 		URL url = new URL("java://JavaProject/org.eclipse.tests.Junk#main([QString;)");
 		assertEquals("java", url.getProtocol());
 		assertEquals("JavaProject", url.getHost());
 		assertEquals("/org.eclipse.tests.Junk", url.getPath());
 		assertEquals("main([QString;)", url.getRef());
 		
 		assertEquals("main(String[] args) {}", url.getContent());
 	}
 	
 //	@Test
 //	public void testParseJavadocURLWithMethod() throws Exception {
 //		URL url = new URL("javadoc://Stuff/org.eclipse.examples.tests.Junk#main([QString;)");
 //		assertEquals("javadoc", url.getProtocol());
 //		assertEquals("Stuff", url.getHost());
 //		assertEquals("/org.eclipse.examples.tests.Junk", url.getPath());
 //		assertEquals("main([QString;)", url.getRef());
 //	}
 }
