 package org.padacore.core.builder;
 
 import java.util.Map;
 import java.util.Observer;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.padacore.core.utils.ErrorLogger;
 import org.padacore.core.utils.ExternalProcess;
 import org.padacore.core.utils.ExternalProcessOutput;
 
 /**
  * This class implements an Ada incremental project build using gprbuild.
  * 
  * @author RS
  * 
  */
 public class AdaProjectBuilder extends IncrementalProjectBuilder {
 
 	private Job cleaningJob;
 
 	public AdaProjectBuilder() {
 		this.cleaningJob = null;
 	}
 
 	/**
 	 * Returns the path of GPR (GNAT Project) file.
 	 * 
 	 * @return the absolute path of GPR file.
 	 */
 	private String getGprFullPath() {
 		String gprProjectFullPath = this.getProject()
 				.getFile(this.getProject().getName() + ".gpr").getRawLocation()
 				.toOSString();
 
 		return gprProjectFullPath;
 	}
 
 	/**
 	 * Returns the build command to use with its arguments.
 	 * 
 	 * @return the build command with its arguments as an array of String
 	 *         (command followed by arguments).
 	 */
 	private String[] buildCommand() {
 		return new String[] { "gprbuild", "-d", "-p", "-P", getGprFullPath() };
 	}
 
 	/**
 	 * Returns the clean command to use with its arguments.
 	 * 
 	 * @return the clean command with its arguments as an array of String
 	 *         (command followed by arguments).
 	 */
 	private String[] cleanCommand() {
 		return new String[] { "gprclean", "-r", "-P", getGprFullPath() };
 	}
 
 	/**
 	 * Performs the build according to the kind of build requested.
 	 * 
 	 * @param kind
 	 *            the type of build requested (valid values are FULL_BUILD,
 	 *            INCREMENTAL_BUILD or AUTO_BUILD).
 	 * @throws CoreException
 	 *             if the resources of the project cannot be retrieved.
 	 */
 	private void build(int kind) throws CoreException {
 		Assert.isLegal(kind == FULL_BUILD || kind == INCREMENTAL_BUILD
 				|| kind == AUTO_BUILD);
 
 		final String message = "Building of " + getProject().getName();
 
 		Job buildJob = new Job(message) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 
 				ExternalProcess process = new ExternalProcess(message,
 						new Observer[] { new GprbuildObserver(monitor),
 								new ExternalProcessOutput() }, new Observer[] {
 								new GprbuildErrObserver(getProject()),
 								new ExternalProcessOutput() });
 
 				process.run(buildCommand(), monitor);
 				refreshBuiltProject();
 
 				return Status.OK_STATUS;
 			}
 		};
 
 		buildJob.addJobChangeListener(new DerivedResourcesIdentifier(this
 				.getProject(), this.cleaningJob));
 		buildJob.schedule();
 	}
 
 	@Override
 	protected IProject[] build(int kind, Map<String, String> args,
 			IProgressMonitor monitor) throws CoreException {
 
 		if (kind == FULL_BUILD) {
 			this.build(kind);
 		} else {
 			if (this.isIncrementalBuildRequired(this.getProject())) {
 				this.build(kind);
 			}
 		}
 
		return this.getProject().getReferencedProjects();
 	}
 
 	/**
 	 * Returns true if an incremental build is required for the given project,
 	 * false otherwise.
 	 * 
 	 * @param project
 	 *            the project for which incremental build is requested.
 	 * @return if incremental build shall be performed, False otherwise.
 	 */
 	private boolean isIncrementalBuildRequired(IProject project) {
 		boolean buildIsRequired = true;
 		IResourceDelta delta = this.getDelta(project);
 
 		if (delta != null) {
 			IResource concernedResource = delta.getResource();
 			Assert.isTrue(concernedResource.getType() == IResource.PROJECT);
 
 			buildIsRequired = this
 					.doesChangeInProjectAffectsAtLeastOneNonDerivedFile(delta);
 		}
 
 		return buildIsRequired;
 	}
 
 	private class NonDerivedResourcesFinder implements IResourceDeltaVisitor {
 
 		private boolean nonDerivedFileFound;
 
 		public NonDerivedResourcesFinder() {
 			this.nonDerivedFileFound = false;
 		}
 
 		@Override
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			IResource resource = delta.getResource();
 
 			boolean resourceIsNotADerivedFolder = !(resource.getType() == IResource.FOLDER && resource
 					.isDerived());
 
 			this.nonDerivedFileFound = resource.getType() == IResource.FILE
 					&& !resource.isDerived();
 
 			return !this.nonDerivedFileFound && resourceIsNotADerivedFolder;
 		}
 
 		public boolean hasNonDerivedFileBeenFound() {
 			return this.nonDerivedFileFound;
 		}
 
 	}
 
 	/**
 	 * Checks if the given delta contains at least one change to a non-derived
 	 * file.
 	 * 
 	 * @param delta
 	 *            the delta to examine.
 	 * @return true if the given delta contains at least one change to a
 	 *         non-derived file, false otherwise.
 	 */
 	// TODO This method can easily be improved by taking into account
 	// information from GPR project: non derived files could be searched only in
 	// source directories.
 	private boolean doesChangeInProjectAffectsAtLeastOneNonDerivedFile(
 			IResourceDelta delta) {
 		NonDerivedResourcesFinder derivedResourcesFinder = new NonDerivedResourcesFinder();
 
 		try {
 			delta.accept(derivedResourcesFinder);
 		} catch (CoreException e) {
 			ErrorLogger.appendExceptionToErrorLog(e);
 		}
 		return derivedResourcesFinder.hasNonDerivedFileBeenFound();
 	}
 
 	@Override
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		final String message = "Cleaning of " + getProject().getName();
 		this.cleaningJob = new Job(message) {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 
 				ExternalProcess process = new ExternalProcess(message,
 						new Observer[] { new ExternalProcessOutput() },
 						new Observer[] { new ExternalProcessOutput() });
 
 				process.run(cleanCommand(), monitor);
 				refreshBuiltProject();
 				return Status.OK_STATUS;
 			}
 		};
 		this.cleaningJob.schedule();
 	}
 
 	/**
 	 * Refreshes the build project.
 	 */
 	private void refreshBuiltProject() {
 		try {
 			getProject().refreshLocal(IResource.DEPTH_INFINITE,
 					new NullProgressMonitor());
 		} catch (CoreException e) {
 			ErrorLogger.appendExceptionToErrorLog(e);
 		}
 	}
 }
