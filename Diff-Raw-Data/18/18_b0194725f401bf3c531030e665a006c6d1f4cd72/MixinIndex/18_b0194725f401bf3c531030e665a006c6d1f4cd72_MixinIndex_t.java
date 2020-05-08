 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.search.index;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.util.HashtableOfObject;
 import org.eclipse.dltk.compiler.util.SimpleSetOfCharArray;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class MixinIndex extends Index {
 
 	private static final String HEADER = "MIXIN INDEX 0.1";
 	
 	private HashtableOfObject docNamesToKeys;
 
 	private final String fileName;
 
 	private boolean dirty;
 
 	public MixinIndex(String fileName, String containerPath, boolean reuseFile)
 			throws IOException {
 		super(fileName, containerPath);
 		this.fileName = fileName;
 		this.dirty = false;
 		if (reuseFile) {
 			initialize(reuseFile);
 		} else {
 			save();
 		}
 	}
 
 	public void addIndexEntry(char[] category, char[] key,
 			String containerRelativePath) {
 		this.dirty = true;
 		if (DLTKCore.DEBUG_INDEX) {
 			System.out.println("DEBUG INDEX: Add Index Entry:"
 					+ new String(category) + " " + new String(key) + " path:"
 					+ containerRelativePath);
 		}
 		Assert.isTrue(CharOperation.equals(category, IIndexConstants.MIXIN));
 		addIndexEntry(key, containerRelativePath.toCharArray());
 	}
 
 	private void addIndexEntry(char[] key, char[] containerRelativePath) {
 		SimpleSetOfCharArray names = (SimpleSetOfCharArray) docNamesToKeys
 				.get(containerRelativePath);
 		if (names == null) {
 			names = new SimpleSetOfCharArray(1);
 			docNamesToKeys.put(containerRelativePath, names);
 		}
 		names.add(key);
 	}
 
 	public String containerRelativePath(String documentPath) {
 		int index = documentPath.indexOf(IDLTKSearchScope.FILE_ENTRY_SEPARATOR);
 		if (index == -1) {
 			index = this.containerPath.length();
 			if (documentPath.length() <= index)
 				throw new IllegalArgumentException(
 						"Document path " + documentPath + " must be relative to " + this.containerPath); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return documentPath.substring(index + 1);
 	}
 
 	public File getIndexFile() {
 		return new File(fileName);
 	}
 
 	public boolean hasChanged() {
 		return this.dirty;
 	}
 
 	public EntryResult[] query(char[][] categories, char[] key, int matchRule)
 			throws IOException {
		boolean found = false;
		for (int i = 0; i < categories.length; i++) {
			if (CharOperation.equals(categories[i], IIndexConstants.MIXIN)) {
				found = true;
			}
		}
		if (!found)
			return new EntryResult[0];
		
 		HashtableOfObject results = new HashtableOfObject(10);
 
 		performQuery(key, matchRule, results);
 
 		EntryResult[] entryResults = new EntryResult[results.elementSize];
 		int count = 0;
 		Object[] values = results.valueTable;
 		for (int i = 0, l = values.length; i < l; i++) {
 			EntryResult result = (EntryResult) values[i];
 			if (result != null)
 				entryResults[count++] = result;
 		}
 		return entryResults;
 	}
 
 	private void performQuery(char[] key, int matchRule,
 			HashtableOfObject results) {
 		char[][] keyTable = docNamesToKeys.keyTable;
 		for (int i = 0; i < keyTable.length; i++) {
 			char[] docName = keyTable[i];
 			if (docName == null)
 				continue;
 			SimpleSetOfCharArray keys = (SimpleSetOfCharArray) docNamesToKeys
 					.get(docName);
 			if (keys != null) {
 				for (int j = 0; j < keys.values.length; j++) {
 					char[] k = keys.values[j];
 					if (k != null && Index.isMatch(key, k, matchRule)) {
 						EntryResult s = (EntryResult) results.get(k);
 						if (s == null) {
 							s = new EntryResult(k, null);
 							results.put(k, s);
 						}
 						s.addDocumentName(new String(docName));
 					}
 				}
 			}
 		}
 	}
 
 	private String[] extractKeysFromTable(HashtableOfObject table) {
 		String[] documentNames = new String[table.elementSize];
 		int count = 0;
 		Object[] values = table.keyTable;
 		for (int i = 0, l = values.length; i < l; i++) {
 			char[] result = (char[]) values[i];
 			if (result != null)
 				documentNames[count++] = new String(result);
 		}
 		return documentNames;
 	}
 
 	/**
 	 * Returns the document names that contain the given substring, if null then
 	 * returns all of them.
 	 */
 	public String[] queryDocumentNames(String substring) throws IOException {
 		if (substring == null) {
 			return extractKeysFromTable(docNamesToKeys);
 		}
 
 		HashtableOfObject results = new HashtableOfObject(10);
 
 		performQuery(substring.toCharArray(), SearchPattern.R_PATTERN_MATCH,
 				results);
 
 		return extractKeysFromTable(results);
 	}
 
 	public void remove(String containerRelativePath) {
 		this.dirty = true;
 		docNamesToKeys.removeKey(containerRelativePath.toCharArray());
 	}
 
 	public void save() throws IOException {
 		long t = System.currentTimeMillis();
 		if (docNamesToKeys == null)
 			docNamesToKeys = new HashtableOfObject(0);
 
 		File f = getIndexFile();
 		DataOutputStream stream = new DataOutputStream(new FileOutputStream(f));
 		int docNamesCount = docNamesToKeys.elementSize;
 		Util.writeUTF(stream, HEADER.toCharArray());
 		stream.writeInt(docNamesCount);
 		for (int i = 0; i < docNamesToKeys.keyTable.length; i++) {
 			char[] docName = docNamesToKeys.keyTable[i];
 			if (docName == null)
 				continue;
 			Util.writeUTF(stream, docName);
 			SimpleSetOfCharArray wordSet = (SimpleSetOfCharArray) docNamesToKeys
 					.get(docName);
 			if (wordSet != null) {
 				stream.writeInt(wordSet.elementSize);
 				for (int j = 0; j < wordSet.values.length; j++) {
 					char[] word = wordSet.values[j];
 					if (word != null) {
 						Util.writeUTF(stream, word);
 					}
 				}
 			} else
 				stream.writeInt(0);
 		}
 		
 		stream.close();
 		this.dirty = false;
 		if (DLTKCore.DEBUG_INDEX) {
 			System.out.println("Mixin index for " + this.containerPath + " ("
 					+ new Path(this.fileName).lastSegment() + ") saved, took "
 					+ (System.currentTimeMillis() - t));
 			System.out.println("Mixin modules: " + this.docNamesToKeys.size());
 		}
 	}
 
 	private void initialize(boolean reuseExistingFile) throws IOException {
 		boolean successful = false;
 		File indexFile = getIndexFile();
 		if (indexFile.exists()) {
 			if (reuseExistingFile) {
 				this.docNamesToKeys = new HashtableOfObject(0);
 				try {
 					monitor.enterRead();
 					DataInputStream stream = new DataInputStream(
 							new FileInputStream(indexFile));
 					char[] header = Util.readUTF(stream);
 					if (new String(header).equals(HEADER)) {						
 						int documentsCount = stream.readInt();
 						for (int i = 0; i < documentsCount; i++) {
 							char[] docName = Util.readUTF(stream);
 							int wordsCount = stream.readInt();
 							for (int j = 0; j < wordsCount; j++) {
 								char[] word = Util.readUTF(stream);
 								addIndexEntry(word, docName);
 							}
 						}
 						successful = true;
 					}
 					stream.close();
 				} catch (FileNotFoundException e) {
 					if (DLTKCore.DEBUG_INDEX)
 						e.printStackTrace();
 				} catch (IOException e) {
 					if (DLTKCore.DEBUG_INDEX)
 						e.printStackTrace();
 				} finally {
 					monitor.exitRead();
 				}
 				if (successful)
 					return;
 			}
 			if (!indexFile.delete()) {
 				if (DLTKCore.DEBUG_INDEX)
 					System.out
 							.println("initialize - Failed to delete mixin index " + this.fileName); //$NON-NLS-1$
 				throw new IOException(
 						"Failed to delete mixin index " + this.fileName); //$NON-NLS-1$
 			}
 		}
 		if (indexFile.createNewFile()) {
 			this.docNamesToKeys = new HashtableOfObject();
 			save();
 		} else {
 			if (DLTKCore.DEBUG_INDEX)
 				System.out
 						.println("initialize - Failed to create new index " + this.fileName); //$NON-NLS-1$
 			throw new IOException("Failed to create new index " + this.fileName); //$NON-NLS-1$
 		}
 		this.dirty = false;
 	}
 
 	public void startQuery() {
 	}
 
 	public void stopQuery() {
 	}
 
 	public String toString() {
 		return "Mixin Index for " + this.containerPath; //$NON-NLS-1$
 	}
 }
