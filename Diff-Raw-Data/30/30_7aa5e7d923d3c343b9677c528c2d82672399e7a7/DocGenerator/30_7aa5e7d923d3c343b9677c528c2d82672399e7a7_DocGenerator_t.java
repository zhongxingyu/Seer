 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.gen;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.dom.DOMSource;
 
 import org.w3c.dom.Document;
 
 import org.znerd.logdoc.LogDef;
 
 /**
  * Documentation generator.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public final class DocGenerator {
 
    /**
     * Creates a new <code>DocGenerator</code> for the specified log 
     * definition.
     *
     * @param def
     *    the {@link LogDef} object, cannot be <code>null</code>.
     *
     * @param targetDir
     *    the target directory to create the HTML documentation files in,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>def == null || targetDir == null</code>.
     */
    public DocGenerator(LogDef def, File targetDir) throws IllegalArgumentException {
       if (def == null) {
          throw new IllegalArgumentException("def == null");
       } else if (targetDir == null) {
          throw new IllegalArgumentException("targetDir == null");
       }
 
       _def       = def;
       _targetDir = targetDir;
    }
 
    private final LogDef _def;
    private final File _targetDir;
 
    /**
     * Generates the HTML documentation for the specified log definition.
     *
     * @throws IOException
     *    if the HTML documentation files could not be generated.
     */
   public void generateDocs() throws IllegalArgumentException, IOException {
      generateOverviewDoc();
      generateEntryListDoc();
      generateGroupAndEntryDocs();
   }
 
   private final void generateOverviewDoc() throws IOException {
      Map<String,String> xsltParams = new HashMap<String,String>();
      xsltParams.put("package_name", _def.getDomainName());
      transformToDoc("", "index", xsltParams);
   }
 
   private final void generateEntryListDoc() throws IOException {
      Map<String,String> xsltParams = new HashMap<String,String>();
      transformToDoc("_list", "entry-list", new HashMap<String,String>());
    }
 
   private final void generateGroupAndEntryDocs() throws IOException {
       for (LogDef.Group group : _def.getGroups()) {
          String groupID = group.getID();
          Map<String,String> xsltParams = new HashMap<String,String>();
          xsltParams.put("group", groupID);
          String stylesheetName = "_group";
          String        outName = "group-" + groupID;
          transformToDoc(stylesheetName, outName, xsltParams);
 
          transformEntries(group);
       }
    }
 
    private final void transformEntries(LogDef.Group group) throws IOException {
       for (LogDef.Entry entry : group.getEntries()) {
          String entryID = entry.getID();
          Map<String,String> xsltParams = new HashMap<String,String>();
          xsltParams.put("entry", entryID);
          String stylesheetName = "_entry";
          String outName = "entry-" + entryID;
          transformToDoc(stylesheetName, outName, xsltParams);
       }
    }
 
    private final void transformToDoc(String stylesheetName, String outName, Map<String,String> xsltParams)
    throws IOException {
       Source      source = new DOMSource(_def.getXML());
       String    xsltPath = "log_to" + stylesheetName + "_html.xslt";
       String outFileName = outName + ".html";
 
       new Xformer(_def).transform(source, xsltPath, xsltParams, _targetDir, outFileName);
    }
 }
