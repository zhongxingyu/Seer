 /*
  * Copyright (c) 2013 Alastair R. Beresford
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
 package uk.ac.cam.db538.dexter.manifest;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Stack;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import pxb.android.axml.AxmlReader;
 import pxb.android.axml.AxmlVisitor;
 import pxb.android.axml.AxmlVisitor.NodeVisitor;
 import pxb.android.axml.AxmlWriter;
 
 /**
  * Simple utility methods for rewriting the binary version of the AndroidManifest.xml file.
  *
  * @author Alastair R. Beresford
  *
  */
 public class BinXmlUtil {
 
     private static final String ANDROID_APP_TAG = "application";
     private static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";
     private static final String ANDROID_APP_NAME = "name";
     private static final String ANDROID_PACKAGE = "package";
 
     /**
      * Represent the details of the application class for the Android application.
      *
      * The full name of the class may be specified directly in the "name" attribute
      * of the &lt;application&gt; tag. Alternatively the name of the class may need
      * to be combined with the package specified in the &lt;manifest&gt; tag.
      */
     private static class FullAppName {
         String packageName;
         String appName;
         @Override
         public String toString() {
        	if (packageName == null)
        		return appName;
        	else if (appName == null)
        		return null;
        	else if (appName.startsWith("."))
        		return packageName + appName;
        	else if (!appName.contains("."))
        		return packageName + "." + appName;
        	else
        		return appName;
         }
     }
 
     private static FullAppName getFullAppName(final InputStream xml) throws IOException {
 
         AxmlReader ar = new AxmlReader(IOUtils.toByteArray(xml));
         final FullAppName fullAppName = new FullAppName();
 
         ar.accept(new AxmlVisitor() {
             public NodeVisitor first(String ns, String name) {//top level: <manifest>
                 final NodeVisitor rootNv = super.first(ns, name);
                 return new NodeVisitor(rootNv) {
                     @Override
                     public void attr(String ns, String name, int resourceId, int type, Object obj) {
                         if (name.equals(ANDROID_PACKAGE) &&
                                 type == TYPE_STRING &&
                                 obj instanceof String) {
                             fullAppName.packageName = (String) obj;
                         }
                     }
                     @Override
                     public NodeVisitor child(String ns, String name) {//first-level child tag <application>
                         final NodeVisitor childNv = super.child(ns, name);
                         if (name.equals(ANDROID_APP_TAG)) {
                             return new NodeVisitor(childNv) {
                                 @Override
                                 public void attr(String ns, String name, int resourceId, int type, Object obj) {
                                     if (ns.equals(ANDROID_SCHEMA) &&
                                             name.equals(ANDROID_APP_NAME) &&
                                             type == TYPE_STRING &&
                                             obj instanceof String) {
                                         //Android spec says at most one attribute with this name so no overwrite possible.
                                         fullAppName.appName = (String) obj;
                                     }
                                 }
                             };
                         }
                         return null;
                     }
                 };
             }
         });
         return fullAppName;
     }
 
     /**
      * Return the fully qualified application class name from the manifest, or null if not found.
      *
      * This method will combine the data from the manifest tag if necessary to generate a fully qualified class name.
      *
      * @param xml
      * @return
      * @throws IOException
      */
     public static String getApplicationClass(final InputStream xml) throws IOException {
         return getFullAppName(xml).toString();
     }
 
     /**
      * Return the package associated with this application in the &lt;manifest&gt; tag.
      *
      * @param xml
      * @return
      * @throws IOException
      */
     public static String getPackage(final InputStream xml) throws IOException {
         return getFullAppName(xml).packageName;
     }
 
     private static class NvReplaceAppName extends NodeVisitor {
         NvReplaceAppName(NodeVisitor nv, String appName) {
             super(nv);
             super.attr(ANDROID_SCHEMA, ANDROID_APP_NAME, 0x1010003, AxmlVisitor.TYPE_STRING, appName);
         }
         @Override
         public void attr(String ns, String name, int resourceId, int type, Object obj) {
             if (!ns.equals(ANDROID_SCHEMA) || !name.equals(ANDROID_APP_NAME)) {
                 super.attr(ns, name, resourceId, type, obj);
             }
         }
     }
 
     /**
      * Edit a binary AndroidManifest.xml file to insert/update the "android:name" attribute of the application tag.
      *
      * @param xml input file containing the current binary xml file
      * @param applicationClassName the new name for the value associated with the android:name attribute
      * @return new binary XML file as an array of bytes
      * @throws IOException if an error occurs reading from the input stream
      */
     public static byte[] setApplicationClass(final InputStream xml, final String applicationClassName) throws IOException {
 
         AxmlReader ar = new AxmlReader(IOUtils.toByteArray(xml));
         AxmlWriter aw = new AxmlWriter();
 
         ar.accept(new AxmlVisitor(aw) {
             public NodeVisitor first(String ns, String name) {//top level: <manifest>
 
                 final NodeVisitor rootNv = super.first(ns, name);
                 return new NodeVisitor(rootNv) {
                     @Override
                     public NodeVisitor child(String ns, String name) {//<application>
                         if (name.equals(ANDROID_APP_TAG)) {
                             return new NvReplaceAppName(super.child(ns, name), applicationClassName);
                         }
                         return super.child(ns, name);
                     }
                 };
             };
         });
 
         return aw.toByteArray();
     }
 
     /**
      * Print out a copy of the binary XML file in text format. Attribute schemas are currently not displayed.
      *
      * @param xml
      * @throws IOException
      */
     public static void prettyPrint(final InputStream xml) throws IOException {
 
         AxmlReader ar = new AxmlReader(IOUtils.toByteArray(xml));
 
         final Stack<String> stack = new Stack<String>();
 
         ar.accept(new AxmlVisitor() {
             boolean closeRequired = false;
             public NodeVisitor first(String ns, String name) {//top level: <manifest>
 
                 final NodeVisitor rootNv = super.first(ns, name);
                 String padding = StringUtils.repeat(" ", stack.size());
                 System.out.print(padding + "<" + name);
                 closeRequired = true;
                 stack.push(name);
 
                 return new NodeVisitor(rootNv) {
                     @Override
                     public NodeVisitor child(String ns, String name) {
                         if (closeRequired)
                             System.out.println(">");
                         closeRequired = true;
                         String padding = StringUtils.repeat(" ", stack.size());
                         System.out.print(padding + "<" + name);
                         stack.push(name);
                         return this;
                     }
                     @Override
                     public void attr(String ns, String name, int resourceId, int type, Object obj) {
                         System.out.print(" " + name + "=\"" + obj + "\"");
                     }
                     @Override
                     public void end() {
                         if (closeRequired)
                             System.out.println(">");
                         closeRequired = false;
                         String name = stack.pop();
                         String padding = StringUtils.repeat(" ", stack.size());
                         System.out.println(padding + "</" + name + ">");
                     }
                 };
             }
             @Override
             public void end() {
                 if (closeRequired)
                     System.out.println(">");
                 String name = stack.pop();
                 String padding = StringUtils.repeat(" ", stack.size());
                 System.out.println(padding + "</" + name + ">");
             }
         });
     }
 
     /**
      * Read the contents of first argument and display applicationClass; write modified version if other args provided.
      *
      * The input and output filenames may be the same.
      *
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
 
         if (args.length == 0) {
             System.out.println("Usage: java BinXmlUtil input_file [new_name output_file]");
             return;
         }
 
         byte[] inputBinXml = FileUtils.readFileToByteArray(new File(args[0]));
         InputStream inputBinXmlStream = new ByteArrayInputStream(inputBinXml);
 
         System.out.println("App name read as: " + getApplicationClass(inputBinXmlStream));
         inputBinXmlStream.reset();
         System.out.println("App package read as: " + getPackage(inputBinXmlStream));
         //inputBinXmlStream.reset();
         //prettyPrint(inputBinXmlStream);
 
         if (args.length >= 3) {
             inputBinXmlStream.reset();
             byte[] modified = setApplicationClass(inputBinXmlStream, args[1]);
             InputStream modStream = new ByteArrayInputStream(modified);
             System.out.println("App name changed to: " + getApplicationClass(modStream));
             modStream.reset();
             FileUtils.writeByteArrayToFile(new File(args[2]), modified);
         }
     }
 }
