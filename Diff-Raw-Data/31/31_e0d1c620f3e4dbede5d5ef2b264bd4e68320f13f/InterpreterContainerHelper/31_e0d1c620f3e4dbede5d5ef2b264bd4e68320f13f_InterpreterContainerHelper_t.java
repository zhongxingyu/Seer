 package org.eclipse.dltk.launching;
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathAttribute;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.utils.TextUtils;
 
 public class InterpreterContainerHelper {
 	private static final char SEPARATOR = '|';
 	private static final String PACKAGES_ATTR = "user_dependencies"; //$NON-NLS-1$
 	private static final String PACKAGES_AUTO_ATTR = "auto_dependencies"; //$NON-NLS-1$
 	public static final String CONTAINER_PATH = ScriptRuntime.INTERPRETER_CONTAINER;
 
 	public static void getInterpreterContainerDependencies(
 			IScriptProject project, Set<String> packages,
 			Set<String> autoPackages) {
 		IBuildpathEntry[] rawBuildpath = null;
 		try {
 			rawBuildpath = project.getRawBuildpath();
 		} catch (ModelException e1) {
 			if (DLTKCore.DEBUG) {
 				e1.printStackTrace();
 			}
 		}
 		if (rawBuildpath == null) {
 			return;
 		}
 		IBuildpathEntry containerEntry = null;
 		for (int i = 0; i < rawBuildpath.length; i++) {
 			if (rawBuildpath[i].getPath().segment(0).equals(CONTAINER_PATH)) {
 				containerEntry = rawBuildpath[i];
 			}
 		}
 		if (containerEntry == null) {
 			return;
 		}
 		IBuildpathAttribute[] extraAttributes = containerEntry
 				.getExtraAttributes();
 		for (int i = 0; i < extraAttributes.length; i++) {
 			if (extraAttributes[i].getName().equals(PACKAGES_ATTR)) {
 				String value = extraAttributes[i].getValue();
 				String[] split = split(value);
 				for (int j = 0; j < split.length; j++) {
 					packages.add(split[j]);
 				}
 			} else if (extraAttributes[i].getName().equals(PACKAGES_AUTO_ATTR)) {
 				String value = extraAttributes[i].getValue();
 				String[] split = split(value);
 				for (int j = 0; j < split.length; j++) {
 					autoPackages.add(split[j]);
 				}
 			}
 		}
 		return;
 	}
 
 	private static String[] split(String value) {
 		return TextUtils.split(value, SEPARATOR);
 	}
 
 	public static void setInterpreterContainerDependencies(
 			IScriptProject project, Set<String> packages,
 			Set<String> autoPackages) {
 		IBuildpathEntry[] rawBuildpath = null;
 		try {
 			rawBuildpath = project.getRawBuildpath();
 		} catch (ModelException e1) {
 			if (DLTKCore.DEBUG) {
 				e1.printStackTrace();
 			}
 		}
 		IPath containerName = new Path(CONTAINER_PATH).append(project
 				.getElementName());
 
 		List<IBuildpathEntry> newBuildpath = new ArrayList<IBuildpathEntry>();
 		boolean found = false;
 		for (int i = 0; i < rawBuildpath.length; i++) {
 			if (!rawBuildpath[i].getPath().segment(0).equals(CONTAINER_PATH)) {
 				newBuildpath.add(rawBuildpath[i]);
 			} else {
 				found = true;
 				newBuildpath.add(createPackagesContainer(packages,
 						autoPackages, rawBuildpath[i].getPath()));
 			}
 		}
 		if (!found) {
 			newBuildpath.add(createPackagesContainer(packages, autoPackages,
 					containerName));
 		}
 		IBuildpathEntry[] nbp = newBuildpath
 				.toArray(new IBuildpathEntry[newBuildpath.size()]);
 		try {
 			project.setRawBuildpath(nbp, new NullProgressMonitor());
 		} catch (ModelException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static IBuildpathEntry createPackagesContainer(Set<String> names,
 			Set<String> autoPackages, IPath containerName) {
		final List<IBuildpathAttribute> attributes = new ArrayList<IBuildpathAttribute>(
				2);
		if (names != null && !names.isEmpty()) {
			attributes.add(DLTKCore.newBuildpathAttribute(PACKAGES_ATTR,
					pkgsToString(names)));
		}
		if (autoPackages != null && !autoPackages.isEmpty()) {
			attributes.add(DLTKCore.newBuildpathAttribute(PACKAGES_AUTO_ATTR,
					pkgsToString(autoPackages)));
		}
		return DLTKCore.newContainerEntry(containerName,
				BuildpathEntry.NO_ACCESS_RULES,
				attributes.toArray(new IBuildpathAttribute[attributes.size()]),
				false/* not exported */);
 	}
 
 	private static String pkgsToString(Set<String> names) {
		final List<String> sorted = new ArrayList<String>(names);
		Collections.sort(sorted);
		return TextUtils.join(sorted, SEPARATOR);
 	}
 }
