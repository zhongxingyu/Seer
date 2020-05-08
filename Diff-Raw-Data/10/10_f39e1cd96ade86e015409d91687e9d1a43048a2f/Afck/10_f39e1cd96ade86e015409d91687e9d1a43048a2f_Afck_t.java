 /* (C) Copyright 2013 Stephen Chandler Paul (thatslyude@gmail.com)
  *
  * This program and accompanying materials are made available under the terms of
  * the GNU Lesser General Public License (LGPL) version 2.1 which accompanies
  * this distribution (LICENSE at the root of this project's directory), and is
  * also available at
  * http://www.gnu.org/licenses/gpl-3.0.html
  *
  * This program is distributed in the hopes that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  */
 
 import java.util.*;
 import java.util.zip.*;
 import java.io.*;
 import javax.xml.parsers.*;
 import org.xml.sax.SAXException;
 import org.w3c.dom.*;
 
 public class Afck {
 
     public static final String VERSION = "0.0.0";
 
     // Buffer size for extracting Alice worlds
     public static final int BUFFER_SIZE = 2048;
 
     /**
      * Recursively returns all the elementData files in the directory
      * {@link parent}.
      *
      * @param parent The directory to search
      * @return       All the elementData files found
      */
     public static List<File> getAllElementData(File parent) {
         List<File> filteredList = new ArrayList<File>();
         for (File file : parent.listFiles()) {
             if (file.isDirectory())
                 filteredList.addAll(getAllElementData(file));
             else if (file.getName().equals("elementData.xml"))
                 filteredList.add(file);
         }
         return filteredList;
     }
 
     public static void main(String[] args) {
         // LGPL stuff
        System.out.print("afck Copyright (C) 2013 Chandler Paul\n"             +
                         "This program comes with ABSOLUTELY NO WARRANTY "     +
                         "and is distributed under the terms of the LGPL "     +
                         "license. For more information, please see the file " +
                         "LICENSE at the root of the source code, or go to "   +
                         "http://www.gnu.org/licenses/lgpl.html\n");
 
         // Don't bother with the GUI if a path is specified on the command line
         if (args.length > 0) {
             File brokenAliceWorld = new File(args[0]);
             File tmpDir;
             String worldName;
             Random generator = new Random();
             
             /* Check to see if the main argument is a file name, otherwise
              * return an error
              */
             if (!brokenAliceWorld.exists()) {
                 System.err.printf("Path specified is not valid! Exiting...\n");
                 System.exit(-1);
             }
 
             // Determine the actual name of the project
             worldName = brokenAliceWorld.getPath();
             // Create a temporary directory and extract the zip
             tmpDir = new File(System.getProperty("java.io.tmpdir") + "/afck-" +
                               Integer.toString(Math.abs(generator.nextInt())));
             tmpDir.mkdirs();
 
             // Attempt to extract the alice world to the temporary directory
             try {
                 ZipFile brokenAliceWorldZip = new ZipFile(brokenAliceWorld);
                 Enumeration aliceZipEntries = brokenAliceWorldZip.entries();
 
                 System.out.print("Extracting world...");
 
                 // Extract each file in the Alice world
                 while (aliceZipEntries.hasMoreElements()) {
                     // Get the current zip entry
                     ZipEntry entry = (ZipEntry) aliceZipEntries.nextElement();
                     String currentEntry = entry.getName();
                     File destFile = new File(tmpDir, currentEntry);
 
                     /* If the parent directory structure for the current entry needs
                      * to be created, do so
                      */
                     File destinationParent = destFile.getParentFile();
                     destinationParent.mkdirs();
 
                     // If the current entry is a file, begin writing it to the disk
                     if (!entry.isDirectory()) {
                         BufferedInputStream inputStream = new BufferedInputStream(brokenAliceWorldZip.getInputStream(entry));
                         int currentByte;
                         byte data[] = new byte[BUFFER_SIZE]; // Buffer
 
                         // Create output streams
                         FileOutputStream outputStream = new FileOutputStream(destFile);
                         BufferedOutputStream dest = new BufferedOutputStream(outputStream, BUFFER_SIZE);
 
                         // Read/Write until EOF
                         while ((currentByte = inputStream.read(data, 0, BUFFER_SIZE)) != -1)
                             dest.write(data, 0, currentByte);
 
                         // Flush the data
                         dest.flush();
 
                         // Close the streams
                         dest.close();
                         outputStream.close();
                         inputStream.close();
                     }
                 }
             }
             catch (ZipException e) {
                 System.err.printf("Fatal zip file exception! %s\n" +
                                   "Exiting...\n",
                                   e.getMessage());
                     
                 // TODO: Clean up
 
                 System.exit(-1);
             }
             catch (IOException e) {
                 System.err.printf("Fatal I/O Exception: %s\n" +
                                   "Exiting...\n",
                                   e.getMessage());
                 System.exit(-1);
             }
 
             System.out.print(" done!\n");
 
             // TODO: Fix the alice file
             /* Look through all the element data in the alice world and find
              * improperly marked index variables
              * Note: At some point I might convert this to the SAX parser, but
              * right now I'm on a deadline to get this in by it's due date, so
              * for now we'll just stick with DOM
              */
             try {
                 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 
                 for (File file : getAllElementData(tmpDir)) {
                     Document elementData = docBuilder.parse(file);
                     Element rootElement = elementData.getDocumentElement();
                     NodeList propertyList;
                     NodeList itemList;
                     int componentResponsesIndex = 0;
                     boolean indexVariableMarkedCorrectly = false;
 
                     /* Check to see if the current element is a loop, if it's not
                      * then there's no chance of a index variable being in there, so
                      * we can safely skip it
                      */
                     if (!rootElement.getAttribute("class").equals(
                         "edu.cmu.cs.stage3.alice.core.response.LoopNInOrder"))
                         continue;
                     
                     // Find the componentResponses section
                     propertyList = rootElement.getElementsByTagName("property");
                     for (int i = 0; i < propertyList.getLength(); i++) {
                         if (((Element) propertyList.item(i)).getAttribute("name").equals(
                             "componentResponses")) {
                             componentResponsesIndex = i;
                             break;
                         }
                     }
 
                     // Check to ensure the index variable is marked correctly
                     itemList = ((Element) propertyList.item(componentResponsesIndex)).getElementsByTagName("item");
                     for (int i = 0; i < itemList.getLength(); i++) {
                         Node item = itemList.item(i);
                         /* Figure out the expected text representation of the
                          * index variable
                          */
                         String indexString = file.getAbsolutePath().replaceFirst(
                             tmpDir.getAbsolutePath() + "(/|\\\\)(.*(/|\\\\))elementData\\.xml", "$2").
                             replaceAll("/|\\\\", ".") + "index";
                         if (((Element) item).getAttribute("criterionClass").equals(
                                 "edu.cmu.cs.stage3.alice.core.criterion.InternalReferenceKeyedCriterion") &&
                             item.getFirstChild().getNodeValue().equals(indexString)) {
                             indexVariableMarkedCorrectly = true;
                             break;
                         }
                     }
                     if (indexVariableMarkedCorrectly)
                         continue;
                     // Else fix it
                     System.out.println("Found one!");
                 }
             }
             catch (ParserConfigurationException e) {
                 System.err.println(e.getMessage());
                 System.exit(-1);
             }
             catch (SAXException e) {
                 System.err.println(e.getMessage());
                 System.exit(-1);
             }
             catch (IOException e) {
                 System.err.println(e.getMessage());
                 System.exit(-1);
             }
 
             // Clean up
             tmpDir.delete();
         }
     }
 }
