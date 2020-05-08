 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.component.opennlp;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 /**
  * This class provides basic mechanics to install OpenNLP
  * models in the environment for the component to reach.
  *
  * @author Xavier Llor&agrave;
  *
  */
 public class ModelInstaller {
 
 	/** Chunk size */
 	private static final int READ_WRITE_CHUNK_SIZE = 65536;
 
 	/** Install the contents of the jar at the given location. If location
 	 * exists no installation is performed, unless forced.
 	 *
 	 * @param sRootDir The location of the root directory where to install the stuff
 	 * @param sJarName The name of the jar to expand
 	 * @param bForce Force the installation by deleting the folder
 	 * @return True is the process finished correctly, false otherwhise.
 	 */
	public static synchronized boolean installJar ( String sRootDir, InputStream jarStream, boolean bForce ) {
 		File fRootDir = new File(sRootDir);
 		// Basic checking
 		if ( fRootDir.exists() ) {
 			if ( bForce ) {
 				boolean bOK = deleteDir(fRootDir);
 				if ( !bOK ) return false;
 			}
 			else {
 				return true;
 			}
 		}
 		else
 			fRootDir.mkdirs();
 
 		// Unjar the contents
 		try {
 			JarInputStream jar = new JarInputStream(jarStream);
 			JarEntry je = null;
 			while ( (je=jar.getNextJarEntry())!=null ) {
 	            File fileTarget = new File(sRootDir+File.separator+je.getName().replaceAll("/", File.separator));
 				if ( je.isDirectory() ) {
 					fileTarget.mkdirs();
 				} else {
 					FileOutputStream fos = new FileOutputStream(fileTarget);
 	                byte [] baBuf = new byte[READ_WRITE_CHUNK_SIZE];
 	                int len;
 	                while ((len = jar.read(baBuf)) > 0) {
 	                    fos.write(baBuf, 0, len);
 	                }
 	                fos.close();
 	            }
 	        }
 		} catch (Throwable t) {
 			deleteDir(new File(sRootDir));
 			return false;
 		}
 
 		return true;
 	}
 
 
 	/**  Deletes all files and subdirectories under dir.
 	 *   Returns true if all deletions were successful.
 	 *   If a deletion fails, the method stops attempting to delete and returns false.
 	 *
 	 * @param dir The directory to delete
 	 * @return True if it was properly cleaned, false otherwise
 	 */
     protected static boolean deleteDir(File dir) {
     	if (dir.isDirectory()) {
             String[] children = dir.list();
             for (int i=0; i<children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
             }
         }
 
         // The directory is now empty so delete it
     	if (dir.exists())
             return dir.delete();
     	else
     		return true;
     }
 
 //    public static void main ( String [] sArgs ) {
 //    	File run = new File("run/opennlp/models");
 //    	run.mkdirs();
 //    	installJar(run.toString(), "opennlp-english-models.jar", true);
 //
 //    }
 }
