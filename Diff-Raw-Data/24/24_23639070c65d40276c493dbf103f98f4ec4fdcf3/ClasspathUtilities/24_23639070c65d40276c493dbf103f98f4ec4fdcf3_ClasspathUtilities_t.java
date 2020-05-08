 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.operations;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.jdt.core.IClasspathEntry;
 
 public class ClasspathUtilities {
 
 	static public IClasspathEntry[] addClasspathEntries(IClasspathEntry[] entries1, IClasspathEntry[] entries2) {
 		List list = new ArrayList(entries1.length + entries2.length);
 		list.addAll(Arrays.asList(entries1));
 
 		for (int i = 0; i < entries2.length; i++) {
 			IClasspathEntry iClasspathEntry = entries2[i];
 			if (!list.contains(iClasspathEntry))
 				list.add(iClasspathEntry);
 		}
 
		IClasspathEntry[] ret = new IClasspathEntry[list.size()];
 		return (IClasspathEntry[]) list.toArray(ret);
 	}
 
 	static protected boolean entryExists(IClasspathEntry entry, IClasspathEntry[] col) {
 		for (int i = 0; i < col.length; i++) {
 			IClasspathEntry otherEntry = col[i];
 			if (entry.getContentKind() != otherEntry.getContentKind())
 				continue;
 
 			if (entry.getEntryKind() != otherEntry.getEntryKind())
 				continue;
 
 			if (entry.isExported() != otherEntry.isExported())
 				continue;
 
 			if (!entry.getPath().equals(otherEntry.getPath()))
 				continue;
 			return true;
 		}
 		return false;
 	}
 
 	static public IClasspathEntry[] removeClasspathEntries(IClasspathEntry[] removeFromCol, IClasspathEntry[] removeCol) {
 		List list = new ArrayList(removeFromCol.length);
 		//		list.addAll(Arrays.asList(removeFromCol));
 
 		for (int i = 0; i < removeFromCol.length; i++) {
 			IClasspathEntry iClasspathEntry = removeFromCol[i];
 			if (!entryExists(iClasspathEntry, removeCol))
 				list.add(iClasspathEntry);
 		}
 
		IClasspathEntry[] ret = new IClasspathEntry[list.size()];
 		return (IClasspathEntry[]) list.toArray(ret);
 	}
 
 }
