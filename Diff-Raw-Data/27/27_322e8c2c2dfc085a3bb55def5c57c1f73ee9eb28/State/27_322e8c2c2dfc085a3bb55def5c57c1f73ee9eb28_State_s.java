 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.builder;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.util.SimpleLookupTable;
 
 public class State {
 	// NOTE: this state cannot contain types that are not defined in this
 	// project
 
 	final String scriptProjectName;
 
 	int buildNumber;
 	long lastStructuralBuildTime;
 	SimpleLookupTable structuralBuildTimes;
 
 	/**
 	 * 0x16 boolean noCleanExternalFolders is always present
 	 **/
 	public static final byte VERSION = 0x0017;
 
 	Set<IPath> externalFolderLocations = new HashSet<IPath>();
 
 	boolean noCleanExternalFolders = false;
 
 	/**
 	 * Full (absolute,including project) path to the set of paths, depending on
 	 * it.
 	 */
 	private final Map<IPath, Set<IPath>> dependencies = new HashMap<IPath, Set<IPath>>();
 
 	private final Set<IPath> importProblems = new HashSet<IPath>();
 
 	static final byte SOURCE_FOLDER = 1;
 	static final byte BINARY_FOLDER = 2;
 	static final byte EXTERNAL_JAR = 3;
 	static final byte INTERNAL_JAR = 4;
 
 	private State(String projectName) {
 		this.scriptProjectName = projectName;
 	}
 
 	public State(IProject project) {
 		this.scriptProjectName = project.getName();
 
 		this.buildNumber = 0; // indicates a full build
 		this.lastStructuralBuildTime = System.currentTimeMillis();
 		this.structuralBuildTimes = new SimpleLookupTable(3);
 		this.noCleanExternalFolders = false;
 	}
 
 	protected State(ScriptBuilder scriptBuilder) {
 		this.scriptProjectName = scriptBuilder.currentProject.getName();
 
 		this.buildNumber = 0; // indicates a full build
 		this.lastStructuralBuildTime = System.currentTimeMillis();
 		this.structuralBuildTimes = new SimpleLookupTable(3);
 		this.noCleanExternalFolders = false;
 	}
 
 	void copyFrom(State lastState) {
 		this.buildNumber = lastState.buildNumber + 1;
 		this.lastStructuralBuildTime = lastState.lastStructuralBuildTime;
 		this.structuralBuildTimes = lastState.structuralBuildTimes;
 
 		this.externalFolderLocations.clear();
 		this.externalFolderLocations.addAll(lastState.externalFolderLocations);
 		this.noCleanExternalFolders = false;
 		this.dependencies.clear();
 		this.dependencies.putAll(lastState.dependencies);
 		this.importProblems.clear();
 		this.importProblems.addAll(lastState.importProblems);
 	}
 
 	public Set<IPath> getExternalFolders() {
 		return this.externalFolderLocations;
 	}
 
 	static State read(IProject project, DataInputStream in) throws IOException {
 		if (ScriptBuilder.DEBUG)
 			System.out.println("About to read state " + project.getName()); //$NON-NLS-1$
 		if (VERSION != in.readByte()) {
 			if (ScriptBuilder.DEBUG)
 				System.out
 						.println("Found non-compatible state version... answered null for " + project.getName()); //$NON-NLS-1$
 			return null;
 		}
 
 		State newState = new State(in.readUTF());
 		if (!project.getName().equals(newState.scriptProjectName)) {
 			if (ScriptBuilder.DEBUG)
 				System.out
 						.println("Project's name does not match... answered null"); //$NON-NLS-1$
 			return null;
 		}
 		newState.buildNumber = in.readInt();
 		newState.lastStructuralBuildTime = in.readLong();
 
 		int length = in.readInt();
 		newState.externalFolderLocations.clear();
 		for (int i = 0; i < length; i++) {
 			String folderName = in.readUTF();
 			if (folderName.length() > 0)
 				newState.externalFolderLocations.add(Path
 						.fromPortableString(folderName));
 		}
 		newState.noCleanExternalFolders = in.readBoolean();
 		final int dependencyCount = in.readInt();
 		newState.dependencies.clear();
 		for (int i = 0; i < dependencyCount; ++i) {
 			final Set<IPath> paths = new HashSet<IPath>();
 			newState.dependencies.put(Path.fromOSString(in.readUTF()), paths);
 			readPaths(in, paths);
 		}
 		newState.importProblems.clear();
 		readPaths(in, newState.importProblems);
 		if (ScriptBuilder.DEBUG)
 			System.out
 					.println("Successfully read state for " + newState.scriptProjectName); //$NON-NLS-1$
 		return newState;
 	}
 
 	void tagAsNoopBuild() {
 		this.buildNumber = -1; // tag the project since it has no source
 		// folders and can be skipped
 	}
 
 	boolean wasNoopBuild() {
 		return buildNumber == -1;
 	}
 
 	boolean wasStructurallyChanged(IProject prereqProject, State prereqState) {
 		if (prereqState != null) {
 			Object o = structuralBuildTimes.get(prereqProject.getName());
 			long previous = o == null ? 0 : ((Long) o).longValue();
 			if (previous == prereqState.lastStructuralBuildTime)
 				return false;
 		}
 		return true;
 	}
 
 	void write(DataOutputStream out) throws IOException {
 		/**
 		 * byte VERSION<br>
 		 * String project name<br>
 		 * int build number<br>
 		 * int last structural build number
 		 */
 		out.writeByte(VERSION);
 		out.writeUTF(scriptProjectName);
 		out.writeInt(buildNumber);
 		out.writeLong(lastStructuralBuildTime);
 
 		/*
 		 * ClasspathMultiDirectory[] int id String path(s)
 		 */
 		out.writeInt(externalFolderLocations.size());
 		for (Iterator<IPath> iterator = this.externalFolderLocations.iterator(); iterator
 				.hasNext();) {
 			IPath path = iterator.next();
 			out.writeUTF(path.toPortableString());
 		}
 		out.writeBoolean(this.noCleanExternalFolders);
 		out.writeInt(dependencies.size());
 		for (Map.Entry<IPath, Set<IPath>> entry : dependencies.entrySet()) {
 			out.writeUTF(entry.getKey().toPortableString());
 			writePaths(out, entry.getValue());
 		}
 		writePaths(out, importProblems);
 	}
 
 	private static void readPaths(DataInputStream in, Collection<IPath> paths)
 			throws IOException {
 		final int pathCount = in.readInt();
 		for (int j = 0; j < pathCount; ++j) {
 			paths.add(Path.fromPortableString(in.readUTF()));
 		}
 	}
 
 	private void writePaths(DataOutputStream out, Collection<IPath> paths)
 			throws IOException {
 		out.writeInt(paths.size());
 		for (IPath path : paths) {
 			out.writeUTF(path.toPortableString());
 		}
 	}
 
 	/**
 	 * Returns a string representation of the receiver.
 	 */
 	@Override
 	public String toString() {
 		return "State for " + scriptProjectName //$NON-NLS-1$
 				+ " (#" + buildNumber //$NON-NLS-1$
 				+ " @ " + new Date(lastStructuralBuildTime) //$NON-NLS-1$
 				+ ")"; //$NON-NLS-1$
 	}
 
 	/**
 	 * 
 	 */
 	public void setNoCleanExternalFolders() {
 		this.noCleanExternalFolders = true;
 	}
 
 	protected void recordImportProblem(IPath path) {
 		Assert.isLegal(scriptProjectName.equals(path.segment(0)));
 		importProblems.add(path);
 	}
 
 	protected void recordDependency(IPath path, IPath dependency) {
 		Assert.isLegal(scriptProjectName.equals(path.segment(0)));
 		Set<IPath> paths = dependencies.get(dependency);
 		if (paths == null) {
 			paths = new HashSet<IPath>();
 			dependencies.put(dependency, paths);
 		}
 		paths.add(path);
 	}
 
 	protected void resetDependencies() {
 		dependencies.clear();
 		importProblems.clear();
 	}
 
 	protected void removeDependenciesFor(Set<IPath> paths) {
 		for (Iterator<Map.Entry<IPath, Set<IPath>>> i = dependencies.entrySet()
 				.iterator(); i.hasNext();) {
 			final Map.Entry<IPath, Set<IPath>> entry = i.next();
 			if (entry.getValue().removeAll(paths) && entry.getValue().isEmpty()) {
 				i.remove();
 			}
 		}
 		importProblems.removeAll(paths);
 	}
 
	protected Collection<IPath> dependenciesOf(Set<IPath> paths) {
 		final Collection<IPath> result = new ArrayList<IPath>();
		result.addAll(importProblems);
 		for (IPath path : paths) {
 			final Set<IPath> deps = dependencies.get(path);
 			if (deps != null) {
 				result.addAll(deps);
 			}
 		}
 		return result;
 	}
 }
