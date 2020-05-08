 package org.eclipse.dltk.core;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKFeatures.BooleanFeature;
 import org.eclipse.dltk.core.DLTKFeatures.IntegerFeature;
 import org.eclipse.dltk.core.DLTKFeatures.StringFeature;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.internal.core.ZipArchiveFile;
 
 public abstract class AbstractLanguageToolkit implements IDLTKLanguageToolkit {
 
 	public AbstractLanguageToolkit() {
 	}
 
 	public boolean languageSupportZIPBuildpath() {
 		return false;
 	}
 
 	public boolean validateSourcePackage(IPath path, IEnvironment environment) {
 		return true;
 	}
 
 	public IStatus validateSourceModule(IResource resource) {
 		return Status.OK_STATUS;
 	}
 
 	protected static boolean isEmptyExtension(String name) {
 		return name.indexOf('.') == -1;
 	}
 
 	public boolean canValidateContent(IResource resource) {
 		final IProject project = resource.getProject();
 		if (project == null) { // This is workspace root.
 			return false;
 		}
 		final IEnvironment environment = EnvironmentManager
 				.getEnvironment(project);
 		if (environment == null || !environment.isLocal()) {
 			return false;
 		}
 		return isEmptyExtension(resource.getName());
 	}
 
 	public boolean canValidateContent(File file) {
 		return isEmptyExtension(file.getName());
 	}
 
 	public boolean canValidateContent(IFileHandle file) {
 		return false;
 	}
 
 	public String getPreferenceQualifier() {
 		return null;
 	}
 
 	public boolean get(BooleanFeature feature) {
 		return feature.getDefaultValue();
 	}
 
 	public int get(IntegerFeature feature) {
 		return feature.getDefaultValue();
 	}
 
 	public String get(StringFeature feature) {
 		return feature.getDefaultValue();
 	}
 
 	/**
 	 * @throws IOException
 	 * @since 2.0
 	 */
	public IArchive openArchive(File localFile) throws IOException {
 		return new ZipArchiveFile(localFile);
 	}
 }
