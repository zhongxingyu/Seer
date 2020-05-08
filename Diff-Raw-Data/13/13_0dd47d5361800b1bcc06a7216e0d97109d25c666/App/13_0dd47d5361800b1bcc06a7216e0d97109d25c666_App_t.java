 package net.anzix.snipsnap2xwiki;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 /**
  * Hello world!
  *
  * @todo the users can't modify own profile datas
  * @todo migrate the user first name
  * @todo EJB page links?
  */
 public class App {
 
     private File oldFile;
     private File newDir;
     private Document descriptor;
     private Document xwikiAllGroups;
     private Element groupTemplate;
     private Map<String, Element> userCache = new HashMap();
     private Map<String, Element> snipCache = new HashMap();
     private Map<String, List<Element>> commentsCache = new HashMap<String, List<Element>>();
     private Map<String, String> snipNameCache = new HashMap();
     private Transformation syntaxTransformation;
     int userNo = 3;
 
     public App(File oldFile, File newDir) {
         this.oldFile = oldFile;
         this.newDir = newDir;
     }
 
     public void migrate() {
         try {
             new File(newDir, "Main").mkdirs();
             new File(newDir, "XWiki").mkdirs();
 
             File packageTemplate = new File("src/main/template/package.xml");
             SAXBuilder builder = new SAXBuilder();
             descriptor = builder.build(packageTemplate);
 
             //initialize group file
             File groupFile = new File("src/main/template/XWikiAllGroup.xml");
             xwikiAllGroups = builder.build(groupFile).getDocument();
             List<Element> groupObjects = xwikiAllGroups.getRootElement().getChildren("object");
             for (Element e : groupObjects) {
                 groupTemplate = e;
             }
 
             Document d = builder.build(oldFile);
             Element root = d.getRootElement();
             List<Element> childs = root.getChildren();
             System.out.println(childs.size());
 
             //pre process
             for (Element e : childs) {
                 if (e.getName().equals("snip")) {
                     String snipName = e.getChildText("name");
                     snipNameCache.put(snipName.toLowerCase(), snipName);
                     snipCache.put(snipName, e);
 
                     //comments cache
                     if (snipName.startsWith("comment-")) {
                         String parentName = snipName.substring("comment-".length());
                         parentName = parentName.substring(0, parentName.lastIndexOf("-"));
 
                         if (commentsCache.get(parentName) == null) {
                             commentsCache.put(parentName, new ArrayList<Element>());
                         }
                         commentsCache.get(parentName).add(e);
 
 
                     }
 
                 } else if (e.getName().equals("user")) {
                     String login = e.getChildText("login");
                     userCache.put(login, e);
                 }
             }
             syntaxTransformation = new SnipSnap2XWiki1(snipNameCache);
 
             //migration
             for (Element e : childs) {
                 if (e.getName().equals("snip")) {
                     migrateSnip(e);
                 }
             }
             writeFile("XWiki", "XWikiAllGroup", xwikiAllGroups);
             XMLOutputter outputter = new XMLOutputter();
             outputter.output(descriptor, new FileWriter(new File(newDir, "package.xml")));
 
 
         } catch (JDOMException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     public static void main(String[] args) {
         new App(new File( args.length < 1 ? "/home/elek/jhacks.snip" : args[0]), new File(args.length < 2 ? "/tmp/xwiki" : args[1])).migrate();
 
     }
 
     private void migrateSnip(Element oldRoot) {
         try {
 
             String name = oldRoot.getChildText("name");
             if (name.equals("admin")) {
                 System.out.println("ignore page " + name);
                 return;
             }
             if (name.startsWith("comment") || name.startsWith("start") || name.startsWith("topic")) {
                 return;
             }
             if (name.startsWith("SnipSnap")) {
                 System.out.println("ignore comment" + name);
                 return;
             }
             if (userCache.containsKey(name)) {
                 migrateUser(oldRoot, userCache.get(name), name);
                 return;
             }
 
             SAXBuilder builder = new SAXBuilder();
             Document d = builder.build(new File("src/main/template/Page.xml"));
 
             //open template
 
             Element newRoot = d.getRootElement();
 
             Transformation xwikiPrefix = new AddPrefix("XWiki.");
 
             if (name.contains("/")) {
 
                 String parentName = name.substring(0, name.lastIndexOf("/"));
                 name = name.replaceAll("/", "");
                 parentName = parentName.replaceAll("/", "");
                 newRoot.getChild("parent").setText("Main." + parentName);
             }
 
 
             //copy properties
             DomCopier copier = new DomCopier(oldRoot, newRoot);
             copier.copyText("name", "name", new Transformation() {
 
                 @Override
                 public String transform(String source) {
                     return source.replaceAll("/", "");
                 }
             });
 
             copier.copyText("name", "title");
             copier.copyText("mUser", "contentAuthor", xwikiPrefix);
             copier.copyText("mUser", "author", xwikiPrefix);
             copier.copyText("cUser", "creator", xwikiPrefix);
 
             copier.copyText("cTime", "creationDate");
             copier.copyText("mTime", "date");
             copier.copyText("mTime", "contentUpdateDate");
 
             copier.copyText("content", "content", syntaxTransformation);
 
             for(Element attachemnt : (List<Element>)oldRoot.getChild("attachments").getChildren("attachment")) {
             	Element newAttachemnt = new Element("attachment");
             	newRoot.addContent(newAttachemnt);
             	DomCopier attahcmentCopier = new DomCopier(attachemnt, newAttachemnt);
             	attahcmentCopier.copyText("name", "filename");
             	attahcmentCopier.copyText("size", "filesize");
             	attahcmentCopier.copyText("date", "date");
             	attahcmentCopier.copyText("data", "content");
            	addTag(newAttachemnt, "version", "1.0");
            	addTag(newAttachemnt, "author", "XWiki.Admin");
            	addTag(newAttachemnt, "comment", "migrated from snipsnap");
             }
             
             //comments?
             if (commentsCache.get(name) != null) {
                 for (Element commentSnipElement : commentsCache.get(name)) {
                     Document commentDoc = builder.build(new File("src/main/template/comment.xml"));
                     Element commentRoot = (Element) commentDoc.getRootElement().clone();
 
                     //author of comment
                     Element p = new Element("property");
                     Element t = new Element("author");
                     p.addContent(t);
                     t.addContent("XWiki." + commentSnipElement.getChildText("cUser"));
                     commentRoot.addContent(p);
 
                     //text of comment
                     p = new Element("property");
                     t = new Element("comment");
                     p.addContent(t);
                     t.addContent(commentSnipElement.getChildText("content"));
                     commentRoot.addContent(p);
 
                     //date of comment
                     p = new Element("property");
                     t = new Element("date");
                     p.addContent(t);
                     Date createdTime = new Date(Long.parseLong(commentSnipElement.getChildText("cTime")));
                     SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd H:m:s.S");
                     t.addContent(dateFormatter.format(createdTime));
                     commentRoot.addContent(p);
 
                     newRoot.addContent(commentRoot);
                 }
 
             }
 
             //write output
             writeFile("Main", name, d);
 
         } catch (JDOMException ex) {
             Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
    private void addTag(Element parent, String name, String value) {
    	Element element = new Element(name);
    	element.setText(value);
    	parent.addContent(element);
	}

	private void migrateUser(Element oldRoot, Element userRoot, String name) {
         try {
             SAXBuilder builder = new SAXBuilder();
             Document d = builder.build(new File("src/main/template/User.xml"));
             //open template
             Element newRoot = d.getRootElement();
             //copy properties
             DomCopier copier = new DomCopier(userRoot, newRoot);
             copier.copyText("login", "name");
 
             copier.copyText("cTime", "creationDate");
             copier.copyText("mTime", "date");
 
             //replace object name tags
             List<Element> objects = newRoot.getChildren("object");
             for (Element object : objects) {
                 object.getChild("name").setText("XWiki." + name);
             }
 
             writeFile("XWiki", name, d);
 
             //add user to XWikiAllGroup file
             Element e = (Element) groupTemplate.clone();
             e.getChild("property").getChild("member").setText("XWiki." + name);
             e.getChild("number").setText("" + userNo++);
             xwikiAllGroups.getRootElement().addContent(e);
 
             System.out.println("User:" + name + " migrated");
         } catch (JDOMException ex) {
             Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void writeFile(String space, String name, Document content) throws IOException {
         //write output
         XMLOutputter outputter = new XMLOutputter();
         //d.getRootElement().getChild("name").setText("xxx");
         outputter.output(content, new FileWriter(new File(new File(newDir, space), name + ".xml")));
         //write to package descriptor
         Element files = descriptor.getRootElement().getChild("files");
         Element file = new Element("file");
         file.setAttribute("defaultAction", "0");
         file.setAttribute("language", "");
         file.setText(space + "." + name);
         files.addContent(file);
         files.addContent("\n");
     }
 }
