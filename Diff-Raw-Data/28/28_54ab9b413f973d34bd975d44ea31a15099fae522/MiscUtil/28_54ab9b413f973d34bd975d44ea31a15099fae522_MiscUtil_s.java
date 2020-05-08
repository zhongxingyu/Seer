 /*******************************************************************************
  * Copyright (c) 2007 DSource.org and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Bruno Medeiros - initial implementation
  *******************************************************************************/
 package melnorme.miscutil;
 
 import static melnorme.miscutil.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
public class MiscUtil 
	extends CoreUtil // This code-reuse extension is for backwards compatibility 
{
 	
 	public static <T> IPredicate<T> notNullPredicate() {
 		return new NotNullPredicate<T>();
 	}
 	
 	public static final class NotNullPredicate<T> implements IPredicate<T> {
 		public boolean evaluate(T obj) {
 			return obj != null;
 		}
 	}
 	
 	public static final class IsNullPredicate<T> implements IPredicate<T> {
 		public boolean evaluate(T obj) {
 			return obj == null;
 		}
 	}

 	/** Combines two hash codes to make a new one. */
 	public static int combineHashCodes(int hashCode1, int hashCode2) {
 		return hashCode1 * 17 + hashCode2;
 	}

 	/** Runs a shell command as a Process and waits for it to terminate. 
 	 * Assumes the process does not read input. 
 	 * @return exit value of the process */
 	public static int runShellCommand(String directory, String cmd, String... args) throws IOException {
 		ProcessBuilder procBuilder = new ProcessBuilder();
 		ArrayList<String> cmdList = new ArrayList<String>(args.length+1);
 		cmdList.add(cmd);
 		cmdList.addAll(Arrays.asList(args));
 		procBuilder.command(cmdList);
 		procBuilder.redirectErrorStream(true);
 		procBuilder.directory(new File(directory));
 	
 		Process process = procBuilder.start();
 		// read proccess's stdout and stderr, so it doesn't get stuck in I/O
 		StreamUtil.readAllBytesFromStream(process.getInputStream());
 		
 		try {
 			return process.waitFor();
 		} catch (InterruptedException e) {
 			throw melnorme.miscutil.ExceptionAdapter.unchecked(e);
 		}
 	}

 	/** Counts the active flags in the given bitfield */
 	public static int countActiveFlags(int bitfield, int[] flags) {
 		int count = 0;
 		for (int i = 0; i < flags.length; i++) {
 			if((bitfield & flags[i]) != 0)
 				count++;
 		}
 		return count;
 	}

 	/** Convenience method for extracting the element of a single element collection . */
 	public static <T> T getSingleElement(Collection<T> singletonDefunits) {
 		assertTrue(singletonDefunits.size() == 1);
 		return singletonDefunits.iterator().next();
 	}

 	/** Returns a copy of given collection, synchs on the given collection. */
 	public static <T> List<T> synchronizedCreateCopy(Collection<T> collection) {
 		ArrayList<T> newCollection;
 		synchronized (collection) {
 			newCollection = new ArrayList<T>(collection);
 		}
 		return newCollection;
 	}
 	
 	/** Sleeps current thread for given millis amount. 
 	 * If interrupted throws an unchecked exception. */
 	public static void sleepUnchecked(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			throw melnorme.miscutil.ExceptionAdapter.unchecked(e);
 		}
 	}
 }
