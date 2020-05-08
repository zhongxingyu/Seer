 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.search.index;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
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
 import org.eclipse.dltk.compiler.util.ObjectVector;
 import org.eclipse.dltk.compiler.util.SimpleSet;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 import org.eclipse.dltk.core.search.indexing.IndexManager;
 import org.eclipse.dltk.internal.core.util.Util;
 
 public class MixinIndex extends Index {
 
 	private static final char[] OLD_HEADER = "MIXIN INDEX 0.1".toCharArray(); //$NON-NLS-1$
 	private static final char[] HEADER = "MIXIN INDEX 0.2".toCharArray(); //$NON-NLS-1$
 
 	private final HashtableOfObject keyToDocs = new HashtableOfObject(10);
 	private final SimpleSet documentNames = new SimpleSet(10);
 
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
 		Assert.isTrue(CharOperation.equals(category, IIndexConstants.MIXIN));
		if (false) {
			System.out.println("MIXIN: addIndexEntry '" + new String(key) //$NON-NLS-1$
					+ "' path '" + containerRelativePath + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
 		addIndexEntry(key, internDocName(containerRelativePath));
 	}
 
 	/**
 	 * Adds document to the index without any associated words. This method is
 	 * needed to save in the index names of all indexed documents - some
 	 * documents contains no MIXIN-related information, so this method is used
 	 * to record just the document name.
 	 * 
 	 * @param containerRelativePath
 	 */
 	public void addDocumentName(String containerRelativePath) {
 		internDocName(containerRelativePath);
 	}
 
 	private void addIndexEntry(char[] key, String containerRelativePath) {
 		SimpleSet docs = (SimpleSet) keyToDocs.get(key);
 		if (docs == null) {
 			docs = new SimpleSet(1);
 			keyToDocs.put(key, docs);
 		}
 		docs.add(containerRelativePath);
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
 
 	private static boolean isMixinCategory(char[][] categories) {
 		for (int i = 0; i < categories.length; i++) {
 			if (CharOperation.equals(categories[i], IIndexConstants.MIXIN)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public EntryResult[] query(char[][] categories, char[] key, int matchRule)
 			throws IOException {
 		if (!isMixinCategory(categories))
 			return new EntryResult[0];
 		final ObjectVector results = new ObjectVector();
 		performQuery(key, matchRule, results);
 		final EntryResult[] entryResults = new EntryResult[results.size];
 		results.copyInto(entryResults);
 		return entryResults;
 	}
 
 	private void performQuery(char[] key, int matchRule, ObjectVector results) {
 		final char[][] keyTable = keyToDocs.keyTable;
 		for (int i = 0, keyLen = keyTable.length; i < keyLen; i++) {
 			final char[] nextKey = keyTable[i];
 			if (nextKey == null)
 				continue;
 			if (Index.isMatch(key, nextKey, matchRule)) {
 				final EntryResult s = new EntryResult(nextKey, null);
 				results.add(s);
 				final Object[] docTable = ((SimpleSet) keyToDocs.valueTable[i]).values;
 				for (int j = 0, docLen = docTable.length; j < docLen; j++) {
 					final String doc = (String) docTable[j];
 					if (doc != null) {
 						s.addDocumentName(doc);
 					}
 				}
 			}
 		}
 	}
 
 	private static String[] extractKeysFromTable(SimpleSet table,
 			String substring) {
 		String[] documentNames = new String[table.elementSize];
 		int count = 0;
 		final Object[] values = table.values;
 		for (int i = 0, l = values.length; i < l; i++) {
 			final String result = (String) values[i];
 			if (result != null
 					&& (substring == null || result.startsWith(substring))) {
 				documentNames[count++] = result;
 			}
 		}
 		if (count != documentNames.length) {
 			final String[] result = new String[count];
 			System.arraycopy(documentNames, 0, result, 0, count);
 			documentNames = result;
 		}
 		return documentNames;
 	}
 
 	/**
 	 * Returns the document names that contain the given substring, if null then
 	 * returns all of them.
 	 */
 	public String[] queryDocumentNames(String substring) throws IOException {
 		return extractKeysFromTable(documentNames, substring);
 	}
 
 	public void remove(String containerRelativePath) {
 		this.dirty = true;
 		if (documentNames.remove(containerRelativePath) != null) {
 			final char[][] keyTable = keyToDocs.keyTable;
 			for (int i = 0; i < keyTable.length; i++) {
 				final SimpleSet docs = (SimpleSet) keyToDocs.valueTable[i];
 				if (docs != null) {
 					docs.remove(containerRelativePath);
 				}
 			}
 		}
 	}
 
 	public void save() throws IOException {
 		long start = DLTKCore.VERBOSE_MIXIN ? System.currentTimeMillis() : 0;
 		if (!hasChanged()) {
 			return;
 		}
 
 		File f = getIndexFile();
 		FileOutputStream fouts = new FileOutputStream(f, false);
 		BufferedOutputStream bufout = new BufferedOutputStream(fouts, 2048);
 		DataOutputStream stream = new DataOutputStream(bufout);
 		final SimpleSet allDocuments = new SimpleSet();
 		allDocuments.addAll(documentNames);
 		Util.writeUTF(stream, HEADER);
 		int keyCount = keyToDocs.elementSize;
 		stream.writeInt(keyCount);
 		for (int i = 0; i < keyToDocs.keyTable.length; i++) {
 			char[] key = keyToDocs.keyTable[i];
 			if (key == null)
 				continue;
 			Util.writeUTF(stream, key);
 			final SimpleSet docs = (SimpleSet) keyToDocs.valueTable[i];
 			stream.writeInt(docs.elementSize);
 			for (int j = 0; j < docs.values.length; j++) {
 				final String docName = (String) docs.values[j];
 				if (docName != null) {
 					Util.writeUTF(stream, docName.toCharArray());
 					allDocuments.remove(docName);
 				}
 			}
 		}
 		stream.writeInt(allDocuments.size());
		for (int i = 0, docTableLen = allDocuments.values.length; i < docTableLen; ++i) {
 			String docName = (String) allDocuments.values[i];
 			if (docName != null) {
 				Util.writeUTF(stream, docName.toCharArray());
 			}
 		}
 		bufout.close();
 		stream.close();
 		fouts.close();
 		this.dirty = false;
 		if (DLTKCore.VERBOSE_MIXIN) {
 			System.out.println("Mixin index for " + this.containerPath + " (" //$NON-NLS-1$ //$NON-NLS-2$
 					+ new Path(this.fileName).lastSegment() + ") saved, took " //$NON-NLS-1$
 					+ (System.currentTimeMillis() - start));
 			System.out.println("Mixin modules: " + this.documentNames.size()); //$NON-NLS-1$
 			System.out.println("Mixin keys: " + this.keyToDocs.size()); //$NON-NLS-1$
 		}
 	}
 
 	private void initialize(boolean reuseExistingFile) throws IOException {
 		boolean successful = false;
 		File indexFile = getIndexFile();
 		if (indexFile.exists()) {
 			if (reuseExistingFile) {
 				try {
 					monitor.enterRead();
 					DataInputStream stream = new DataInputStream(
 							new BufferedInputStream(new FileInputStream(
 									indexFile), 2048));
 					try {
 						final char[] header = Util.readUTF(stream);
 						if (CharOperation.equals(OLD_HEADER, header)) {
 							loadDocToKeyFormat(stream);
 							successful = true;
 						} else if (CharOperation.equals(HEADER, header)) {
 							loadKeyToDocFormat(stream);
 							successful = true;
 						}
 					} finally {
 						stream.close();
 					}
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
 			save();
 		} else {
 			if (DLTKCore.DEBUG_INDEX)
 				System.out
 						.println("initialize - Failed to create new index " + this.fileName); //$NON-NLS-1$
 			throw new IOException("Failed to create new index " + this.fileName); //$NON-NLS-1$
 		}
 		this.dirty = false;
 	}
 
 	private void loadKeyToDocFormat(DataInputStream stream) throws IOException {
 		final int keyCount = stream.readInt();
 		for (int i = 0; i < keyCount; i++) {
 			final char[] key = Util.readUTF(stream);
 			final int docCount = stream.readInt();
 			for (int j = 0; j < docCount; j++) {
 				String docName = internDocName(new String(Util.readUTF(stream)));
 				addIndexEntry(key, docName);
 			}
 		}
 		final int docCount = stream.readInt();
 		for (int i = 0; i < docCount; ++i) {
 			internDocName(new String(Util.readUTF(stream)));
 		}
 	}
 
 	private void loadDocToKeyFormat(DataInputStream stream) throws IOException {
 		int documentsCount = stream.readInt();
 		for (int i = 0; i < documentsCount; i++) {
 			String docName = internDocName(new String(Util.readUTF(stream)));
 			int wordsCount = stream.readInt();
 			if (wordsCount > 0) {
 				for (int j = 0; j < wordsCount; j++) {
 					char[] word = Util.readUTF(stream);
 					addIndexEntry(word, docName);
 				}
 			}
 		}
 	}
 
 	private final String internDocName(String docName) {
 		return (String) documentNames.addIntern(docName);
 	}
 
 	public void startQuery() {
 	}
 
 	public void stopQuery() {
 	}
 
 	public String toString() {
 		return "Mixin Index for " + this.containerPath; //$NON-NLS-1$
 	}
 
 	public boolean isRebuildable() {
 		return false;
 	}
 
 	public String getContainerPath() {
 		if (containerPath.startsWith(IndexManager.SPECIAL_MIXIN)) {
 			return containerPath.substring(IndexManager.SPECIAL_MIXIN.length());
 		} else {
 			return containerPath;
 		}
 	}
 }
