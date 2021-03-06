 /*******************************************************************************
  * Copyright (c) 2007, 2009 Red Hat, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Red Hat - initial API and implementation
  *    Alphonse Van Assche
  *******************************************************************************/
 
 package org.eclipse.linuxtools.rpm.ui.editor.parser;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.linuxtools.rpm.ui.editor.RpmTags;
 
 public class Specfile {
 
 	SpecfilePreamble preamble;
 	
 	SpecfilePackageContainer packages;
 
 	List<SpecfileSection> sections;
 	List<SpecfileSection> complexSections;
 
 	Map<String, SpecfileDefine> defines;
 
 	Map<Integer, SpecfileSource> sources;
 
 	Map<Integer, SpecfileSource> patches;
 
 	private IDocument document;
 
 	public Specfile() {
 		packages = new SpecfilePackageContainer();
 		preamble = new SpecfilePreamble();
 		sections = new ArrayList<SpecfileSection>();
 		complexSections = new ArrayList<SpecfileSection>();
 		defines = new HashMap<String, SpecfileDefine>();
 		sources = new HashMap<Integer, SpecfileSource>();
 		patches = new HashMap<Integer, SpecfileSource>();
 	}
 
 	public List<SpecfileSection> getSections() {
 		return sections;
 	}
 	
 	public List<SpecfileSection> getComplexSections() {
 		return complexSections;
 	}
 	
 	public SpecfileSource getPatch(int number) {
 		return patches.get(number);
 	}
 
 	public SpecfileSource getSource(int number) {
 		return sources.get(number);
 	}
 
 	public String getName() {
 		SpecfileDefine define = getDefine(RpmTags.NAME.toLowerCase());
 		if (define != null){
 			return define.getStringValue();
 		}
 		return " "; //$NON-NLS-1$
 	}
 
 	public void addSection(SpecfileSection section) {
 		sections.add(section);
 	}
 
 	public void addComplexSection(SpecfileSection section) {
 		complexSections.add(section);
 	}
 	
 	public void addSource(SpecfileSource source) {
 		sources.put(Integer.valueOf(source.getNumber()), source);
 	}
 
 	public void addPatch(SpecfileSource patch) {
 		patches.put(Integer.valueOf(patch.getNumber()), patch);
 	}
 
     /**
      * Adds the given define to the map of defines for this specfile.
      * 
      * @param define The define to add.
      */
     public void addDefine(SpecfileDefine define) {
 		defines.put(define.getName(), define);
 	}
     
     public void addDefine(SpecfileTag tag) {
 		addDefine(new SpecfileDefine(tag));
 	}
 	
 	public SpecfileDefine getDefine(String defineName) {
 		return defines.get(defineName);
 	}
 
 	public int getEpoch() {
 		SpecfileDefine define = getDefine(RpmTags.EPOCH.toLowerCase());
 		if (define != null){
 			return define.getIntValue();
 		}
 		return -1;
 	}
 
 	public String getRelease() {
		SpecfileDefine define = getDefine(RpmTags.RELEASE.toLowerCase());
		if (define != null){
			return define.getStringValue();
		}
		return "0"; //$NON-NLS-1$
 	}
 
 	public String getVersion() {
		SpecfileDefine define = getDefine(RpmTags.VERSION.toLowerCase());
		if (define != null){
			return define.getStringValue();
		}
		return "0"; //$NON-NLS-1$
 	}
 
 	public List<SpecfileSource> getPatches() {
 		List<SpecfileSource> patchesList = new ArrayList<SpecfileSource>(patches.values());
 		Collections.sort(patchesList, new SourceComparator());
 		return patchesList;
 	}
 
 	public Collection<SpecfileSource> getSources() {
 		List<SpecfileSource> sourcesList = new ArrayList<SpecfileSource>(sources.values());
 		Collections.sort(sourcesList, new SourceComparator());
 		return sourcesList;
 	}
 
 	public List<SpecfileDefine> getDefines() {
 		List<SpecfileDefine> definesList = new ArrayList<SpecfileDefine>(defines.values());
 		return definesList;
 	}
 	
 	public void organizePatches() {
 		List<SpecfileSource> patches = getPatches();
 		int newPatchNumber = 0;
 		int oldPatchNumber = -1;
 		Map<Integer, SpecfileSource> newPatches = new HashMap<Integer, SpecfileSource>();
 		for (SpecfileSource thisPatch: patches) {
 			if (thisPatch.getSpecfile() == null)
 				thisPatch.setSpecfile(this);
 			oldPatchNumber = thisPatch.getNumber();
 			thisPatch.setNumber(newPatchNumber);
 			thisPatch.changeDeclaration(oldPatchNumber);
 			thisPatch.changeReferences(oldPatchNumber);
 			newPatches.put(Integer.valueOf(newPatchNumber), thisPatch);
 			newPatchNumber++;
 		}
 		setPatches(newPatches);
 	}
 
 	public void setDocument(IDocument specfileDocument) {
 		document = specfileDocument;
 	}
 
 	public String getLine(int lineNumber) throws BadLocationException {
 		int offset = document.getLineOffset(lineNumber);
 		int length = getLineLength(lineNumber);
 		String lineContents = document.get(offset, length);
 		return lineContents;
 	}
 
 	public IDocument getDocument() {
 		return document;
 	}
 
 	public int getLineLength(int lineNumber) throws BadLocationException {
 		int length = document.getLineLength(lineNumber);
 		String lineDelimiter = document.getLineDelimiter(lineNumber);
 		if (lineDelimiter != null)
 			length = length - lineDelimiter.length();
 		return length;
 	}
 
 	public void changeLine(int lineNumber, String string)
 			throws BadLocationException {
 		document.replace(document.getLineOffset(lineNumber),
 				getLineLength(lineNumber), string);
 	}
 
 	public void setPatches(Map<Integer, SpecfileSource> patches) {
 		this.patches = patches;
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	public String getLicense() {
 		return defines.get(RpmTags.LICENSE.toLowerCase()).getStringValue();
 	}
 
 	public SpecfilePackageContainer getPackages() {
 		return packages;
 	}
 
 	public SpecfileElement getPreamble() {
 		return preamble;
 	}
 
 	public SpecfilePackage getPackage(String packageName) {
 		return getPackages().getPackage(packageName);
 	}
 
 	public void addPackage(SpecfilePackage subPackage) {
 		if (! packages.contains(subPackage))
 			packages.addPackage(subPackage);
 	}
 }
