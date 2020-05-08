 /*
  * Copyright 2012 Robert 'Bobby' Zenz. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of
  * conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list
  * of conditions and the following disclaimer in the documentation and/or other materials
  * provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY Robert 'Bobby' Zenz ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of Robert 'Bobby' Zenz.
  */
 package org.bonsaimind.minecraftmiddleknife;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Allows blending of multiple jars.
  * The last given jar is the "canonical" jar, meaning that files from
  * all the other jars are only appended and never overwritten.
 *
  * What that means? You first pass in the minecraft.jar and then the mod.jar.
  */
 public class Blender {
 
 	private boolean keepManifest = false;
 	private List<String> stack = new ArrayList<String>();
 
 	public Blender() {
 	}
 
 	/**
 	 * Add one jar to the stack.
 	 * @param jar The jar to add.
 	 */
 	public void add(String jar) {
 		stack.add(jar);
 	}
 
 	/**
 	 * Blends the stack into one and saves it into the given outputJar.
 	 * @param outputJar
 	 * @throws FileNotFoundException
	 * @throws IOException
 	 */
 	public void blend(String outputJar) throws FileNotFoundException, IOException {
 		final File outputFile = new File(outputJar).getAbsoluteFile();
 
 		if (outputFile.exists()) {
 			outputFile.delete();
 		}
 
 		ZipOutputStream blendedOutput = new ZipOutputStream(new FileOutputStream(outputFile));
 
 		// We will walk backwards through the stack.
 		ListIterator<String> iterator = stack.listIterator();
 		while (iterator.hasPrevious()) {
 			File jar = new File(iterator.previous()).getAbsoluteFile();
 			copyToZip(blendedOutput, jar, keepManifest);
 		}
 
 		blendedOutput.close();
 	}
 
 	/**
 	 * If true keeps the manifest of the jar.
	 * @return
 	 */
 	public boolean isKeepManifest() {
 		return keepManifest;
 	}
 
 	/**
 	 * Set to true to keep the manifest.
	 * @param keepManifest
 	 */
 	public void setKeepManifest(boolean keepManifest) {
 		this.keepManifest = keepManifest;
 	}
 
 	/**
 	 * Copies the contents of "from" into "output".
 	 * Please be aware that this method is evil and swallows exceptions during
 	 * the creation of entries (because of duplicates).
 	 * @param output
 	 * @param from
 	 * @throws IOException
 	 */
 	public static void copyToZip(ZipOutputStream output, File from, boolean keepManifest) throws IOException {
 		ZipFile input = new ZipFile(from);
 		Enumeration<? extends ZipEntry> entries = input.entries();
 		while (entries.hasMoreElements()) {
 			try {
 				ZipEntry entry = entries.nextElement();
 
 				if (!keepManifest && entry.getName().equals("META-INF/MANIFEST.MF")) {
 					// Continue with the next entry in case it is the manifest.
 					continue;
 				}
 
 				output.putNextEntry(entry);
 
 				InputStream inputStream = input.getInputStream(entry);
 				byte[] buffer = new byte[4096];
 				while (inputStream.available() > 0) {
 					output.write(buffer, 0, inputStream.read(buffer, 0, buffer.length));
 				}
 				inputStream.close();
 				output.closeEntry();
 			} catch (ZipException ex) {
				// Assume that the error is the warning about a duplicate and ignore it.
 				// I know that this is evil...
 			}
 		}
 		input.close();
 	}
 }
