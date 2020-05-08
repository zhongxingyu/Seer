 /**
  * Copyright 2011 Michael R. Lange <michael.r.lange@langmi.de>.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.langmi.javasnippets.file.split;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import org.junit.Test;
 
 /**
  * Splitting files tests.
  *
  * @author Michael R. Lange <michael.r.lange@langmi.de>
  */
 public class SplitFileInHalfTest {
 
     private static final String INPUT_FILE = "src/test/resources/input/input.txt";
     private static final String OUTPUT_FILE = "target/output.txt";
     private static final String OUTPUT_FILE_FIRST_HALF = "target/file-split-output.txt";
     private static final String OUTPUT_FILE_SECOND_HALF = "target/file-split-output-2.txt";
     private static final int EXPECTED_COUNT = 20;
     private static final String ENCODING_UTF_8 = "UTF-8";
 
     @Test
     public void testWriteFile() throws Exception {
         // open input        
         BufferedReader input = null;
         try {
             FileInputStream fis = new FileInputStream(INPUT_FILE);
             InputStreamReader is = new InputStreamReader(fis, ENCODING_UTF_8);
             input = new BufferedReader(is);
             SplitFileInHalf.writeFile(input, EXPECTED_COUNT, OUTPUT_FILE);
         } finally {
             if (input != null) {
                 // close'em all
                 input.close();
             }
         }
        // check successful writing java
         assert SplitFileInHalf.getLineCount(OUTPUT_FILE) == EXPECTED_COUNT;
     }
 
     @Test
     public void testSplitInHalf() throws Exception {
         // split the file
         SplitFileInHalf.splitFileInHalf(INPUT_FILE, OUTPUT_FILE_FIRST_HALF, OUTPUT_FILE_SECOND_HALF);
         // load files and assert counts
         assert SplitFileInHalf.getLineCount(OUTPUT_FILE_FIRST_HALF) == (EXPECTED_COUNT / 2);
         assert SplitFileInHalf.getLineCount(OUTPUT_FILE_SECOND_HALF) == (EXPECTED_COUNT - (EXPECTED_COUNT / 2));
     }
 
     @Test
     public void testGetLineCount() throws Exception {
         assert SplitFileInHalf.getLineCount(INPUT_FILE) == EXPECTED_COUNT;
     }
 
     @Test
     public void testGetLineCountAlternative() throws Exception {
         assert SplitFileInHalf.getLineCountAlternative(INPUT_FILE) == EXPECTED_COUNT;
     }
 }
