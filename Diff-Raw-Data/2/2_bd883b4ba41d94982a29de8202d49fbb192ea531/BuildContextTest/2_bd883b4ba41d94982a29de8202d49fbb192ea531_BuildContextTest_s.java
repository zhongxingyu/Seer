 /*******************************************************************************
  * Copyright (c) 2010 Broadcom Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Broadcom Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.tests.internal.builders;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.eclipse.core.internal.events.BuildContext;
 import org.eclipse.core.internal.resources.BuildConfiguration;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 
 /**
  * These tests exercise the build context functionality that tells a builder in what context
  * it was called.
  */
 public class BuildContextTest extends AbstractBuilderTest {
 	public static Test suite() {
 		return new TestSuite(BuildContextTest.class);
 	}
 
 	private IProject project0;
 	private IProject project1;
 	private IProject project2;
 	private IProject project3;
 	private IProject project4;
 	private final String variant0 = "Variant0";
 	private final String variant1 = "Variant1";
 
 	public BuildContextTest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		// Create resources
 		IWorkspaceRoot root = getWorkspace().getRoot();
 		project0 = root.getProject("BuildContextTests_p0");
 		project1 = root.getProject("BuildContextTests_p1");
 		project2 = root.getProject("BuildContextTests_p2");
 		project3 = root.getProject("BuildContextTests_p3");
 		project4 = root.getProject("BuildContextTests_p4");
 		IResource[] resources = {project0, project1, project2, project3, project4};
 		ensureExistsInWorkspace(resources, true);
 		setAutoBuilding(false);
 		setupProject(project0);
 		setupProject(project1);
 		setupProject(project2);
 		setupProject(project3);
 		setupProject(project4);
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 
 		// Cleanup
 		project0.delete(true, null);
 		project1.delete(true, null);
 		project2.delete(true, null);
 		project3.delete(true, null);
 		project4.delete(true, null);
 	}
 
 	/**
 	 * Helper method to configure a project with a build command and several buildConfigs.
 	 */
 	private void setupProject(IProject project) throws CoreException {
 		IProjectDescription desc = project.getDescription();
 
 		// Add build command
 		ICommand command = createCommand(desc, ContextBuilder.BUILDER_NAME, "Build0");
 		command.setBuilding(IncrementalProjectBuilder.AUTO_BUILD, true);
 		command.setBuilding(IncrementalProjectBuilder.FULL_BUILD, true);
 		command.setBuilding(IncrementalProjectBuilder.INCREMENTAL_BUILD, true);
 		command.setBuilding(IncrementalProjectBuilder.CLEAN_BUILD, true);
 		desc.setBuildSpec(new ICommand[] {command});
 
 		// Create buildConfigs
		desc.setBuildConfigurations(new IBuildConfiguration[] {project.getWorkspace().newBuildConfiguration(project.getName(), variant0, null), project.getWorkspace().newBuildConfiguration(project.getName(), variant1, null)});
 
 		project.setDescription(desc, getMonitor());
 	}
 
 	/**
 	 * Setup a reference graph, then test the build context for for each project involved
 	 * in the 'build'.
 	 */
 	public void testBuildContext() throws CoreException {
 		// Create reference graph
 		IBuildConfiguration p0v0 = project0.getBuildConfiguration(variant0);
 		IBuildConfiguration p0v1 = project0.getBuildConfiguration(variant1);
 		IBuildConfiguration p1v0 = project1.getBuildConfiguration(variant0);
 
 		// Create build order
 		final IBuildConfiguration[] buildOrder = new IBuildConfiguration[] {p0v0, p0v1, p1v0};
 
 		IBuildContext context;
 
 		context = new BuildContext(p0v0, new IBuildConfiguration[] {p0v0, p1v0}, buildOrder);
 		assertArraysContainSameElements("1.0", new IBuildConfiguration[] {}, context.getAllReferencedBuildConfigurations());
 		assertArraysContainSameElements("1.1", new IBuildConfiguration[] {p0v1, p1v0}, context.getAllReferencingBuildConfigurations());
 		assertArraysContainSameElements("1.1", new IBuildConfiguration[] {p0v0, p1v0}, context.getRequestedConfigs());
 
 		context = new BuildContext(p0v1, buildOrder, buildOrder);
 		assertArraysContainSameElements("1.0", new IBuildConfiguration[] {p0v0}, context.getAllReferencedBuildConfigurations());
 		assertArraysContainSameElements("1.1", new IBuildConfiguration[] {p1v0}, context.getAllReferencingBuildConfigurations());
 
 		context = new BuildContext(p1v0, buildOrder, buildOrder);
 		assertArraysContainSameElements("1.0", new IBuildConfiguration[] {p0v0, p0v1}, context.getAllReferencedBuildConfigurations());
 		assertArraysContainSameElements("1.1", new IBuildConfiguration[] {}, context.getAllReferencingBuildConfigurations());
 
 		// And it works with no build context too
 		context = new BuildContext(p1v0);
 		assertArraysContainSameElements("1.0", new IBuildConfiguration[] {}, context.getAllReferencedBuildConfigurations());
 		assertArraysContainSameElements("1.1", new IBuildConfiguration[] {}, context.getAllReferencingBuildConfigurations());
 
 	}
 
 	public void testSingleProjectBuild() throws CoreException {
 		setupSimpleReferences();
 		ContextBuilder.clearStats();
 		project0.build(IncrementalProjectBuilder.FULL_BUILD, getMonitor());
 		assertTrue("1.0", ContextBuilder.checkValid());
 		IBuildContext context = ContextBuilder.getContext(project0.getActiveBuildConfiguration());
 		assertEquals("2.0", 0, context.getAllReferencedProjects().length);
 		assertEquals("2.1", 0, context.getAllReferencingProjects().length);
 		assertEquals("2.2", 0, context.getAllReferencedBuildConfigurations().length);
 		assertEquals("2.3", 0, context.getAllReferencingBuildConfigurations().length);
 	}
 
 	public void testWorkspaceBuildProject() throws CoreException {
 		setupSimpleReferences();
 		ContextBuilder.clearStats();
 		getWorkspace().build(new IBuildConfiguration[] {project0.getActiveBuildConfiguration()}, IncrementalProjectBuilder.FULL_BUILD, true, getMonitor());
 		assertTrue("1.0", ContextBuilder.checkValid());
 		IBuildContext context = ContextBuilder.getContext(project0.getActiveBuildConfiguration());
 		assertArraysContainSameElements("2.0", new IProject[] {project1, project2}, context.getAllReferencedProjects());
 		assertEquals("2.1", 0, context.getAllReferencingProjects().length);
 		context = ContextBuilder.getBuilder(project1.getActiveBuildConfiguration()).contextForLastBuild;
 		assertArraysContainSameElements("3.0", new IProject[] {project2}, context.getAllReferencedProjects());
 		assertArraysContainSameElements("3.1", new IProject[] {project0}, context.getAllReferencingProjects());
 		context = ContextBuilder.getBuilder(project2.getActiveBuildConfiguration()).contextForLastBuild;
 		assertEquals("4.0", 0, context.getAllReferencedProjects().length);
 		assertArraysContainSameElements("4.1", new IProject[] {project0, project1}, context.getAllReferencingProjects());
 	}
 
 	public void testWorkspaceBuildProjects() throws CoreException {
 		setupSimpleReferences();
 		ContextBuilder.clearStats();
 		getWorkspace().build(new IBuildConfiguration[] {project0.getActiveBuildConfiguration(), project2.getActiveBuildConfiguration()}, IncrementalProjectBuilder.FULL_BUILD, true, getMonitor());
 		assertTrue("1.0", ContextBuilder.checkValid());
 		IBuildContext context = ContextBuilder.getContext(project0.getActiveBuildConfiguration());
 		assertArraysContainSameElements("2.0", new IProject[] {project2, project1}, context.getAllReferencedProjects());
 		assertArraysContainSameElements("2.1", new IProject[] {}, context.getAllReferencingProjects());
 		context = ContextBuilder.getBuilder(project1.getActiveBuildConfiguration()).contextForLastBuild;
 		assertArraysContainSameElements("3.0", new IProject[] {project2}, context.getAllReferencedProjects());
 		assertArraysContainSameElements("3.1", new IProject[] {project0}, context.getAllReferencingProjects());
 		context = ContextBuilder.getBuilder(project2.getActiveBuildConfiguration()).contextForLastBuild;
 		assertEquals("4.0", 0, context.getAllReferencedProjects().length);
 		assertEquals("4.1", 2, context.getAllReferencingProjects().length);
 	}
 
 	public void testReferenceActiveVariant() throws CoreException {
 		setReferences(project0.getActiveBuildConfiguration(), new IBuildConfiguration[] {new BuildConfiguration(project1, null)});
 		setReferences(project1.getActiveBuildConfiguration(), new IBuildConfiguration[] {new BuildConfiguration(project2, null)});
 		setReferences(project2.getActiveBuildConfiguration(), new IBuildConfiguration[] {});
 
 		ContextBuilder.clearStats();
 
 		getWorkspace().build(new IBuildConfiguration[] {project0.getActiveBuildConfiguration()}, IncrementalProjectBuilder.FULL_BUILD, true, getMonitor());
 		assertTrue("1.0", ContextBuilder.checkValid());
 
 		IBuildContext context = ContextBuilder.getContext(project0.getActiveBuildConfiguration());
 		assertArraysContainSameElements("2.0", new IProject[] {project1, project2}, context.getAllReferencedProjects());
 		assertEquals("2.1", 0, context.getAllReferencingProjects().length);
 		context = ContextBuilder.getBuilder(project1.getActiveBuildConfiguration()).contextForLastBuild;
 		assertArraysContainSameElements("3.0", new IProject[] {project2}, context.getAllReferencedProjects());
 		assertArraysContainSameElements("3.1", new IProject[] {project0}, context.getAllReferencingProjects());
 		context = ContextBuilder.getBuilder(project2.getActiveBuildConfiguration()).contextForLastBuild;
 		assertEquals("4.0", 0, context.getAllReferencedProjects().length);
 		assertArraysContainSameElements("4.1", new IProject[] {project0, project1}, context.getAllReferencingProjects());
 	}
 
 	/**
 	 * Attempts to build a project that references the active variant of another project,
 	 * and the same variant directly. This should only result in one referenced variant being built.
 	 */
 	public void testReferenceVariantTwice() throws CoreException {
 		IBuildConfiguration ref1 = new BuildConfiguration(project1, null);
 		IBuildConfiguration ref2 = new BuildConfiguration(project1, project1.getActiveBuildConfiguration().getId());
 		setReferences(project0.getActiveBuildConfiguration(), new IBuildConfiguration[] {ref1, ref2});
 		setReferences(project1.getActiveBuildConfiguration(), new IBuildConfiguration[] {});
 
 		ContextBuilder.clearStats();
 
 		getWorkspace().build(new IBuildConfiguration[] {project0.getActiveBuildConfiguration()}, IncrementalProjectBuilder.FULL_BUILD, true, getMonitor());
 		assertTrue("1.0", ContextBuilder.checkValid());
 
 		IBuildContext context = ContextBuilder.getContext(project0.getActiveBuildConfiguration());
 		assertArraysContainSameElements("2.0", new IProject[] {project1}, context.getAllReferencedProjects());
 		assertEquals("2.1", 0, context.getAllReferencingProjects().length);
 		assertArraysContainSameElements("2.2", new IBuildConfiguration[] {project1.getActiveBuildConfiguration()}, context.getAllReferencedBuildConfigurations());
 		assertEquals("2.3", 0, context.getAllReferencingBuildConfigurations().length);
 
 		context = ContextBuilder.getBuilder(project1.getActiveBuildConfiguration()).contextForLastBuild;
 		assertEquals("3.0", 0, context.getAllReferencedProjects().length);
 		assertArraysContainSameElements("3.1", new IProject[] {project0}, context.getAllReferencingProjects());
 		assertEquals("3.2", 0, context.getAllReferencedBuildConfigurations().length);
 		assertArraysContainSameElements("3.3", new IBuildConfiguration[] {project0.getActiveBuildConfiguration()}, context.getAllReferencingBuildConfigurations());
 	}
 
 	/**
 	 * p0 --> p1 --> p2
 	 * @throws CoreException
 	 */
 	private void setupSimpleReferences() throws CoreException {
 		setReferences(project0.getActiveBuildConfiguration(), new IBuildConfiguration[] {project1.getActiveBuildConfiguration()});
 		setReferences(project1.getActiveBuildConfiguration(), new IBuildConfiguration[] {project2.getActiveBuildConfiguration()});
 		setReferences(project2.getActiveBuildConfiguration(), new IBuildConfiguration[] {});
 	}
 
 	/**
 	 * Helper method to set the references for a project.
 	 */
 	private void setReferences(IBuildConfiguration variant, IBuildConfiguration[] refs) throws CoreException {
 		IProjectDescription desc = variant.getProject().getDescription();
 		desc.setBuildConfigReferences(variant.getId(), refs);
 		variant.getProject().setDescription(desc, getMonitor());
 	}
 
 	private void assertArraysContainSameElements(String id, IBuildConfiguration[] expected, IBuildConfiguration[] actual) {
 		assertArraysContainSameElements(id, expected, actual, new Comparator() {
 			public int compare(Object left, Object right) {
 				IBuildConfiguration leftV = (IBuildConfiguration) left;
 				IBuildConfiguration rightV = (IBuildConfiguration) right;
 				int ret = leftV.getProject().getName().compareTo(rightV.getProject().getName());
 				if (ret == 0)
 					ret = leftV.getId().compareTo(rightV.getId());
 				return ret;
 			}
 		});
 	}
 
 	private void assertArraysContainSameElements(String id, IProject[] expected, IProject[] actual) {
 		assertArraysContainSameElements(id, expected, actual, new Comparator() {
 			public int compare(Object left, Object right) {
 				return ((IProject) left).getName().compareTo(((IProject) right).getName());
 			}
 		});
 	}
 
 	/** Helper method to check if two project variant arrays contain the same elements, but in any order */
 	private void assertArraysContainSameElements(String id, Object[] expected, Object[] actual, Comparator comparator) {
 		assertEquals(id, expected.length, actual.length);
 		Arrays.sort(expected, comparator);
 		Arrays.sort(actual, comparator);
 		for (int i = 0; i < expected.length; i++)
 			assertEquals(id, expected[i], actual[i]);
 	}
 }
