 // Copyright © 2011 Martin Ueding <dev@martin-ueding.de>
 
 /*
  * This file is part of jscribble.
  *
  * jscribble is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 2 of the License, or (at your option)
  * any later version.
  *
  * jscribble is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * jscribble.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tests.jscribble.notebook;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 import jscribble.helpers.FileComparator;
 import jscribble.notebook.NoteBookCompressor;
 import jscribble.notebook.NoteSheetFileFilter;
 import junit.framework.TestCase;
 
 /**
  * Exercises the NoteBookCompressor.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 public class NoteBookCompressorTest extends TestCase {
 	public void testCompression() {
 		File folder;
 		try {
 			folder = File.createTempFile("jscribble", "");
 			String tempname = folder.getAbsolutePath();
 			folder.delete();
 			folder = new File(tempname);
 			folder.mkdirs();
 
 			// Create some files which are not in direct order.
 			File[] files = {
				new File(tempname + File.separator + "000000.png"),
 				new File(tempname + File.separator + "000001.png"),
 				new File(tempname + File.separator + "000003.png"),
 				new File(tempname + File.separator + "000004.png"),
 				new File(tempname + File.separator + "000005.png"),
 				new File(tempname + File.separator + "000010.png"),
 				new File(tempname + File.separator + "000017.png"),
 			};
 
 			for (File file : files) {
 				file.createNewFile();
 			}
 
 			NoteBookCompressor nbc = new NoteBookCompressor(folder);
 			nbc.compress();
 
 			File[] renamedFiles = folder.listFiles(new NoteSheetFileFilter());
 			assertEquals(files.length, renamedFiles.length);
 			Arrays.sort(renamedFiles, new FileComparator());
 			assertEquals(files.length, renamedFiles.length);
 

 			for (int i = 0; i < renamedFiles.length; i++) {
				System.out.println(renamedFiles[i]);
				renamedFiles[i].getName().equals(String.format("%06d.png", i + 1));
 			}

 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
