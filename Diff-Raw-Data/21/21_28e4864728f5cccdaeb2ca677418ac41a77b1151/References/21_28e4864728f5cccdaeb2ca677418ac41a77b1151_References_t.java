 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.jasmin.model;
 
 import com.google.javascript.jscomp.BasicErrorManager;
 import com.google.javascript.jscomp.CheckLevel;
 import com.google.javascript.jscomp.Compiler;
 
 import com.google.javascript.jscomp.CompilerOptions;
 import com.google.javascript.jscomp.JSError;
 import com.google.javascript.jscomp.Result;
 import com.google.javascript.jscomp.SourceFile;
 import net.oneandone.jasmin.main.Servlet;
 import net.oneandone.mork.mapping.ExceptionErrorHandler;
 import net.oneandone.mork.mapping.Mapper;
 import net.oneandone.mork.misc.GenericException;
 import net.oneandone.ssass.scss.Output;
 import net.oneandone.ssass.scss.Stylesheet;
import net.oneandone.sushi.fs.CreateInputStreamException;
 import net.oneandone.sushi.fs.GetLastModifiedException;
 import net.oneandone.sushi.fs.Node;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.fs.webdav.WebdavNode;
 import net.oneandone.sushi.fs.zip.ZipNode;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 /** Reference to a node, with minimize flag and type. */
 public class References {
     public static final byte LF = 10;
     private static final Mapper SSASS;
 
     static {
         SSASS = new Mapper("net.oneandone.ssass.Mapper");
         SSASS.load();
     }
 
     public static References create(MimeType type, boolean minimize, Node node) {
         References result;
 
         result = new References(type, minimize);
         result.add(minimize, node);
         return result;
     }
 
     public final MimeType type;
     public final boolean overallMinimize;
     public final List<Boolean> minimizes;
     public final List<Node> nodes;
 
     public References(MimeType type, boolean overallMinimize) {
         this.type = type;
         this.overallMinimize = overallMinimize;
         this.minimizes = new ArrayList<>();
         this.nodes = new ArrayList<>();
     }
 
     public void add(boolean minimize, Node node) {
         minimizes.add(minimize);
         nodes.add(node);
     }
 
     /** core method */
     public void writeTo(Writer writer) throws IOException {
         switch (type) {
             case CSS:
                 writeCssTo(writer);
                 break;
             case JS:
                 writeJsTo(writer);
                 break;
             default:
                 throw new IllegalStateException(type.toString());
         }
     }
 
     /** core method */
     private void writeJsTo(Writer writer) throws IOException {
         Compiler compiler;
         CompilerOptions options;
         List<SourceFile> externals;
         List<SourceFile> sources;
         Result result;
         boolean first;
         int i;
         Node node;
 
         compiler = new Compiler(new LoggerErrorManager());
         options = new CompilerOptions();
         options.setOutputCharset("utf-8");
         sources = new ArrayList<>();
         externals = new ArrayList<>();
         for (i = 0; i < nodes.size(); i++) {
             node = nodes.get(i);
             sources.add(SourceFile.fromCode(location(node)
                    + /* to get unique names, which is checked by the compiler: */ "_" + i, readString(node)));
         }
         result = compiler.compile(externals, sources, options);
         if (!result.success) {
             if (result.errors.length < 1) {
                 throw new IllegalStateException();
             }
             throw new IOException(result.errors[0].sourceName + ":" + result.errors[0].lineNumber + ":" + result.errors[0].description);
         }
         if (overallMinimize) {
             writer.write(compiler.toSource());
         } else {
             first = true;
             for (SourceFile source : sources) {
                 if (first) {
                     first = false;
                 } else {
                     writer.write(LF);
                 }
                 if (!overallMinimize) {
                     writer.write(type.comment(source.getName()));
                 }
                 writer.write(source.getCode());
             }
         }
     }
 
    private static String readString(Node node) throws IOException {
        try {
            return node.readString();
        } catch (CreateInputStreamException e) {
            if ((e.getCause() instanceof org.apache.http.NoHttpResponseException) && (node instanceof WebdavNode)) {
                try {
                    return node.readString();
                } catch (IOException e2) {
                    e.addSuppressed(e2);
                    throw e;
                }
            }
            throw e;
        }
    }

     /** hook method */
     protected void writeCssTo(Writer writer) throws IOException {
         writeCssTo(writer, new Output(writer, overallMinimize));
     }
 
     protected void writeCssTo(Writer writer, Output output) throws IOException {
         Object[] results;
         Mapper mapper;
         Node node;
 
         mapper = SSASS.newInstance();
         mapper.setErrorHandler(new ExceptionErrorHandler());
         for (int i = 0; i < nodes.size(); i++) {
             node = nodes.get(i);
             if (i > 0) {
                 writer.write(LF);
             }
             if (!overallMinimize) {
                 writer.write(type.comment(location(node)));
             }
             results = mapper.run(node);
             if (results == null) {
                 throw new IOException(node.toString() + ": css/sass error");
             }
             try {
                 ((Stylesheet) results[0]).toCss(output);
             } catch (GenericException e) {
                 throw new IOException(node.toString() + ": css generation failed: " + e.getMessage(), e);
             }
         }
     }
 
     public String readString() throws IOException {
         StringWriter result;
 
         result = new StringWriter();
         writeTo(result);
         return result.toString();
     }
 
     public byte[] readBytes() throws IOException {
         return readString().getBytes("utf-8");
     }
 
     /* @return -1 for unknown */
     public long getLastModified() throws GetLastModifiedException {
         long result;
 
         result = Long.MIN_VALUE;
         for (Node node : nodes) {
             if (node instanceof WebdavNode) {
                 // skip getLastModified - it's not supported by Webservice Stub Servlet
             } else {
                 result = Math.max(result , node.getLastModified());
             }
         }
         return result == Long.MIN_VALUE ? -1 : result;
     }
 
     //--
 
     @Override
     public int hashCode() {
         return nodes.hashCode();
     }
 
     @Override
     public boolean equals(Object obj) {
         References references;
 
         if (obj instanceof References) {
             references = (References) obj;
             return type == references.type && minimizes.equals(references.minimizes) && nodes.equals(references.nodes);
         }
         return false;
     }
 
     @Override
     public String toString() {
         StringBuilder builder;
 
         builder = new StringBuilder();
         for (int i = 0; i < nodes.size(); i++) {
             builder.append(nodes.get(i).getURI());
             builder.append(minimizes.get(i) ? "(min) " : " ");
         }
         return builder.toString();
     }
 
     //--
 
     // do not return full paths -- security!
     public static String location(Node node) {
         String name;
         int idx;
 
         if (node instanceof ZipNode) {
             name = ((ZipNode) node).getRoot().getZip().getName();
             idx = name.lastIndexOf('/');
             if (idx >= 0) {
                 name = name.substring(idx + 1);
             }
             return "zip:" + name + "/" + node.getPath();
         } else if (node instanceof FileNode) {
             return "file:" + node.getRelative(node.getWorld().getWorking());
         } else if (node instanceof WebdavNode) {
             return "http:" + node.getName() + ((WebdavNode) node).getQuery();
         } else {
             return node.getName();
         }
     }
 
 
     //--
 
     public class LoggerErrorManager extends BasicErrorManager {
         public LoggerErrorManager() {
         }
 
         @Override
         public void println(CheckLevel level, JSError error) {
             switch (level) {
                 case ERROR:
                     Servlet.LOG.debug("error: " + error.toString());
                     break;
                 case WARNING:
                     Servlet.LOG.debug("warning: " + error.toString());
                     break;
             }
         }
 
         @Override
         protected void printSummary() {
         }
     }
 }
