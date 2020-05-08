 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.enr.markdownj.extras;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.petebevin.markdown.Entities;
 
 
 /**
  * Markdown app. Transforms in html every file found in source dir, maintaining
  * the original directory structure.
  * 
  * mvn exec:java -Dexec.mainClass="com.github.enr.markdownj.extras.MarkdownApp"
  * 
  * mvn exec:java -Dexec.mainClass="com.github.enr.markdownj.extras.MarkdownApp"
  * -Dexec.args="--header src/test/resources/site/templates/header.html
  * --footer src/test/resources/site/templates/footer.html
  * --source src/test/resources/site/markdown
  * --destination target/markdownj-extras"
  * 
  */
 public class MarkdownApp {
 
     private static final String CL_EXTENSIONS_SEPARATOR = ",";
 
     static private Logger logger;
 
     @SuppressWarnings("static-access")
     protected Logger log() {
         if (this.logger == null)
             this.logger = LoggerFactory.getLogger(this.getClass());
         return this.logger;
     }
 
     /**
      * Path of the directory containing markdown files.
      * If used from command line, the path is translated to absolute path.
      */
     private String source;
 
     /**
      * Path of the target directory.
      * If used from command line, the path is translated to absolute path.
      */
     private String destination;
 
     /**
      * Path to the html file used as header for every transformed markdown file.
      * Could be absolute or relative.
      * 
      */
     private String header;
 
     /**
      * Path to the html file used as footer for every transformed markdown file.
      * Could be absolute or relative.
      * 
      */
     private String footer;
     
     /**
      * The format string used to render code blocks.
      * 
      */
     private String codeBlockTemplate;
     
     /**
      * @see com.petebevin.markdown.Entities
      */
     private Map<Character, String> htmlEntities;
 
     /**
      * Only files with extension in list will be processed.
      */
     private List<String> processableExtensions = new ArrayList<String>();
     
     /**
      * Character encoding to write and read files.
      * null means platform default.
      * 
      */
     private String charEncoding;
 
     private MarkdownService markdown = new MarkdownServiceImpl();
 
     public static void main(String[] args) {
         MarkdownApp app = new MarkdownApp();
         app.log().debug("Markdown app starting with args: {}", Arrays.toString(args));
         CommandLineParser parser = new PosixParser();
         Options options = new Options();
         options.addOption("s", "source", true, "The source directory for markdown files");
         options.addOption("d", "destination", true, "The destination directory for html files");
         options.addOption("h", "header", true, "The path to the html header file");
         options.addOption("f", "footer", true, "The path to the html footer file");
         options.addOption("t", "code-template", true, "The template for code blocks");
         options.addOption("e", "extensions", true, "A comma separated list of file extensions to process. If setted, files with extension not in list won't be processed");
         options.addOption("c", "char-encoding", true, "The encoding to read and write files");
         HelpFormatter formatter = new HelpFormatter();
         String helpHeader = String.format("%s", MarkdownApp.class.getName());
         try {
             CommandLine line = parser.parse(options, args);
             app.process(line);
         } catch (ParseException e) {
             app.log().warn(e.getMessage(), e);
             formatter.printHelp(helpHeader, options);
         }
     }
 
     public void process(CommandLine commandLine) {
         File sourceFile = null;
         File destinationFile = null;
         if (commandLine.hasOption("source")) {
             String sourceArg = commandLine.getOptionValue("source");
             if (sourceArg == null) {
                 log().warn("sourceArg NULL. Exiting");
                 return;
             }
             sourceFile = new File(sourceArg);
             setSource(FileUtils.normalizedPath(sourceFile.getAbsolutePath()));
             log().debug("using source path '{}'", source);
             if (!sourceFile.exists()) {
                 log().warn("source not found '{}'. Exiting", source);
                 return;
             }
         } else {
             log().warn("source argument is mandatory. Exiting");
             return;
         }
         if (commandLine.hasOption("destination")) {
             destinationFile = new File(commandLine.getOptionValue("destination"));
             setDestination(FileUtils.normalizedPath(destinationFile.getAbsolutePath()));
             log().debug("using destination path '{}'", destination);
             if (!destinationFile.exists()) {
                 destinationFile.mkdirs();
             }
         }
         if (commandLine.hasOption("header")) {
             setHeader(commandLine.getOptionValue("header"));
         }
         if (commandLine.hasOption("footer")) {
             setFooter(commandLine.getOptionValue("footer"));
         }
         if (commandLine.hasOption("code-template")) {
             setCodeBlockTemplate(commandLine.getOptionValue("code-template"));
         }
         if (commandLine.hasOption("extensions")) {
             List<String> exts = extensionsToList(commandLine.getOptionValue("extensions"));
             setProcessableExtensions(exts);
         }
         if (commandLine.hasOption("char-encoding")) {
             setCharEncoding(commandLine.getOptionValue("char-encoding"));
         }
         process();
     }
 
     public void process() {
         File sourceFile = new File(getSource());
         if (getHeader() != null) {
             markdown.setHeaderPath(getHeader());
         }
         if (getFooter() != null) {
             markdown.setFooterPath(getFooter());
         }
         if (getCodeBlockTemplate() != null) {
             markdown.setCodeBlockTemplate(getCodeBlockTemplate());
         }
         if (getHtmlEntities() != null) {
             markdown.setHtmlEntities(getHtmlEntities());
         }
         if (getCharEncoding() != null) {
             markdown.setEncoding(getCharEncoding());
         }
         try {
             traverse(sourceFile);
         } catch (IOException e) {
             log().warn(e.getMessage(), e);
         }
     }
 
     public final void traverse(final File f) throws IOException {
         if (f.isDirectory()) {
             final File[] childs = f.listFiles();
             for (File child : childs) {
                 traverse(child);
             }
             return;
         }
         processFile(f);
     }
 
     public void processFile(final File f) {
         String mdFilePath = FileUtils.normalizedPath(f.getAbsolutePath());
         String df = mdFilePath.replaceFirst(source, destination);
         String extension = FileUtils.extension(df);
         if ((getProcessableExtensions().size() == 0) || (getProcessableExtensions().contains(extension))) {
             String destinationFile = FileUtils.changeExtension(df, ".html");
             log().debug("process '{}' -> '{}'", mdFilePath, destinationFile);
             try {
                 String markdownContent = FileUtils.readFileFromPath(mdFilePath, getCharEncoding());
                 markdown.setContent(markdownContent);
                 String html = markdown.process();
                 FileUtils.writeFile(destinationFile, html, getCharEncoding());
             } catch (IOException e) {
                 log().warn(e.getMessage(), e);
             }
         } else {
             log().info("Skipping {} (no processable extension '{}')", df, extension);
         }
     }
 
     /**
      * Converts the comma separated list of extensions from command line to a java.util.List.
      * 
      * @param optionValue
      * @return
      */
     private List<String> extensionsToList(String extensions) {
         String[] exts = extensions.split(MarkdownApp.CL_EXTENSIONS_SEPARATOR);
         if (exts.length > 0)
             return Arrays.asList(exts);
         return Collections.emptyList();
     }
     
     public String getSource() {
         return source;
     }
 
     public void setSource(String source) {
         this.source = source;
     }
 
     public String getDestination() {
         return destination;
     }
 
     public void setDestination(String destination) {
         this.destination = destination;
     }
 
     public String getHeader() {
         return header;
     }
 
     public void setHeader(String header) {
         this.header = header;
     }
 
     public String getFooter() {
         return footer;
     }
 
     public void setFooter(String footer) {
         this.footer = footer;
     }
 
     public String getCodeBlockTemplate() {
         return codeBlockTemplate;
     }
 
     public void setCodeBlockTemplate(String codeBlockTemplate) {
         this.codeBlockTemplate = codeBlockTemplate;
     }
 
     public List<String> getProcessableExtensions() {
         return processableExtensions;
     }
 
     public void setProcessableExtensions(List<String> processableExtensions) {
         this.processableExtensions = processableExtensions;
     }
 
     public void addProcessableExtension(String extension) {
         this.processableExtensions.add(extension);
     }
 
     public Map<Character, String> getHtmlEntities() {
         return htmlEntities;
     }
 
     public void setHtmlEntities(Map<Character, String> htmlEntities) {
         this.htmlEntities = htmlEntities;
     }
     
     public void strictHtmlEncoding()
     {
        this.htmlEntities = Entities.HTML_401;
     }
 
     public String getCharEncoding() {
         return charEncoding;
     }
 
     public void setCharEncoding(String charEncoding) {
         this.charEncoding = charEncoding;
     }
 
 }
