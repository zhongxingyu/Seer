 package lge.xmlevents;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.tools.JavaCompiler;
 import javax.tools.ToolProvider;
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import lge.jaxb.Event;
 import lge.jaxb.Event.Extensions;
 import lge.jaxb.Event.Extensions.Extension;
 import lge.jaxb.Event.Images;
 import lge.jaxb.Event.Options.Option;
 import lge.jaxb.KeyType;
 import lge.jaxb.ObjectFactory;
 import lge.util.FileUtils;
 import lge.util.PathFinder;
 
 import org.xml.sax.SAXException;
 
 public final class XMLEventsHelper {
   private static final Unmarshaller UNMARSHALLER;
   private static final Marshaller MARSHALLER;
   static {
     JAXBContext context = null;
     Unmarshaller unmarshaller = null;
     Marshaller marshaller = null;
     try {
       context = JAXBContext.newInstance(ObjectFactory.class);
       final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
       
       final Schema schema = sf.newSchema(XMLEventsHelper.class.getResource("/lge/xmlevents/schema/event.xsd"));
       try {
         unmarshaller = context.createUnmarshaller();
         unmarshaller.setSchema(schema);
       } catch (final JAXBException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
       try {
         marshaller = context.createMarshaller();
         marshaller.setSchema(schema);
       } catch (final JAXBException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     } catch (final JAXBException | SAXException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     UNMARSHALLER = unmarshaller;
     MARSHALLER = marshaller;
   }
   
   public static Collection<File> generateSource(final File xmlFile) {
     if (!xmlFile.isFile()) {
       // TODO error
     }
     final File baseXML = PathFinder.getModEventsXmlDir();
     final Stack<String> names = new Stack<>();
     File tmp = xmlFile;
     while (!baseXML.equals(tmp) && tmp.getParentFile() != null) {
       names.push(tmp.getName());
       tmp = tmp.getParentFile();
     }
     
     File genSrc = PathFinder.getModEventsGensrcDir();
     String packageName = "";
     while (names.size() > 1) {
       final String packageNamePart = names.pop();
       if (packageName.equals("")) {
         packageName = packageNamePart;
       } else {
         packageName += "." + packageNamePart;
       }
       genSrc = new File(genSrc, packageNamePart);
     }
     
     String eventName = names.pop();
     if (eventName.indexOf(".") != -1) {
       eventName = eventName.split("\\.")[0];
     }
     genSrc = new File(genSrc, eventName + ".java");
     
     return generateSource(xmlFile, genSrc, packageName, eventName);
   }
   
   private static Collection<File> generateSource(final File xmlFile, final File genSrc, final String packageName,
       final String eventName) {
     final List<File> sources = new LinkedList<>();
     try {
       genSrc.getParentFile().mkdirs();
       final Event event = (Event) UNMARSHALLER.unmarshal(xmlFile);
       
       String source = FileUtils.readContentAsString(XMLEventsHelper.class
           .getResourceAsStream("/lge/xmlevents/Event.java.template"));
       
       source = source.replaceAll("%packageName%", packageName);
       source = source.replaceAll("%eventName%", eventName);
       source = replaceImportEvents(source, event);
       source = replaceTexts(source, event);
       source = replaceImages(source, event);
       source = updateImageArea(source, event.getImages());
       source = doEventsText(source, event.getTexts().getText());
       source = doEventsOptions(source, event.getOptions().getOption());
       
       FileUtils.writeContentToFile(genSrc, source);
       
       sources.add(genSrc);
       
       final Extensions extensions = event.getExtensions();
       if (extensions != null) {
         for (final Extension ext : extensions.getExtension()) {
           sources.add(generateExtensionSource(genSrc.getParentFile(), eventName, packageName, ext));
         }
       }
       
     } catch (final JAXBException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     return sources;
   }
   
   private static File generateExtensionSource(final File dir, final String eventName, final String packageName,
       final Extension ext) {
     final String event = ext.getEvent();
     
     final String extSimpleName = eventName + "Ext" + getSimpleName(event);
     final File genSrcExt = new File(dir, extSimpleName + ".java");
     
     String source = FileUtils.readContentAsString(XMLEventsHelper.class
         .getResourceAsStream("/lge/xmlevents/EventExtension.java.template"));
     
     source = source.replaceAll("%packageName%", packageName);
     source = source.replaceAll("%eventName%", extSimpleName);
     source = source.replaceAll("%extendedEvent%", ext.getEvent());
     source = doExtOption(source, ext, eventName);
     
     FileUtils.writeContentToFile(genSrcExt, source);
     return genSrcExt;
   }
   
   private static String doExtOption(final String source, final Extension ext, final String eventName) {
     final StringBuilder sb = new StringBuilder();
     sb.append("    e.addOption(");
     final KeyType key = ext.getKey();
     if (key != null) {
       sb.append("Key.OPTION_" + key.name() + ", ");
     }
     sb.append(getShortRef(eventName + ".Text", ext.getText()) + ", new ");
     sb.append(eventName);
     sb.append("());\n");
     return source.replaceAll("%doEventExtension%", sb.toString());
   }
   
   private static String doEventsOptions(final String source, final List<Option> options) {
     final StringBuilder sb = new StringBuilder();
     for (final Option option : options) {
       sb.append("    addOption(");
       final KeyType key = option.getKey();
       if (key != null) {
         sb.append("Key.OPTION_" + key.name() + ", ");
       }
       sb.append(getShortRef("Text", option.getText()) + ", new ");
       sb.append(getSimpleName(option.getEvent()));
       sb.append("());\n");
     }
     return source.replaceAll("%doEventsOptions%", sb.toString());
   }
   
   private static String getSimpleName(final String event) {
     final String[] split = event.split("\\.");
     return split[split.length - 1];
   }
   
   private static String doEventsText(final String source, final List<String> texts) {
     final StringBuilder sb = new StringBuilder();
     final String firstText = texts.get(0);
     sb.append("    setText(");
     sb.append(getShortRef("Text", firstText));
     sb.append(");\n");
     for (int i = 1; i < texts.size(); i++) {
       sb.append("    addText(");
       sb.append(getShortRef("Text", texts.get(i)));
       sb.append(");\n");
     }
     
     return source.replaceAll("%doEventsText%", sb.toString());
   }
   
   private static String updateImageArea(final String source, final Images images) {
     final StringBuilder sb = new StringBuilder();
     if (images == null) {
       sb.append("    return ImgChange.clearFG();"); // default
     } else {
       final String bg = images.getBg();
       final List<String> fg = images.getFg();
       final boolean clearBG = bg == null && images.isClearBackground();
       final boolean clearFG = images.isClearForeground();
       if (clearBG) {
         if (clearFG) {
           if (fg.isEmpty()) {
             sb.append("    return ImgChange.clearScreen();");
           } else {
             sb.append("    return ImgChange.setBGAndFG(null");
             for (final String fgImage : fg) {
               sb.append(", " + getShortRef("Image", fgImage));
             }
             sb.append(");");
           }
         } else {
           if (!fg.isEmpty()) {
             sb.append("    return ImgChange.setBG(null).addForeground(");
             for (final String fgImage : fg) {
               sb.append(", " + getShortRef("Image", fgImage));
             }
             sb.append(");");
           } else {
             sb.append("    return ImgChange.setBG(null);");
           }
         }
       } else {
         if (images.isClearForeground()) {
           sb.append("    return ImgChange.setBGAndFG(");
           sb.append(getShortRef("Image", bg));
           for (final String fgImage : fg) {
             sb.append(", " + getShortRef("Image", fgImage));
           }
           sb.append(");");
         } else {
           sb.append("    return ImgChange.setBG(");
           sb.append(getShortRef("Image", bg));
           sb.append(").addForeground(");
           for (final String fgImage : fg) {
             sb.append(", " + getShortRef("Image", fgImage));
           }
           sb.append(");");
         }
       }
     }
     return source.replaceAll("%updateImageArea%", sb.toString());
   }
   
   private static String getShortRef(final String prefix, final String reference) {
     if (isInternalRef(reference)) {
       return prefix + "." + reference;
     } else {
       return reference;
     }
   }
   
   private static String replaceImportEvents(final String source, final Event event) {
     final StringBuilder sb = new StringBuilder();
     // final Extensions extensions = event.getExtensions();
     // if (extensions != null) {
     // for (final Extension extension : extensions.getExtension()) {
     // final String extEvent = extension.getEvent();
     // addImport(sb, extEvent);
     // }
     // }
     
     for (final Option option : event.getOptions().getOption()) {
       final String optEvent = option.getEvent();
       addImport(sb, optEvent);
     }
     
     return source.replaceAll("%importEvents%", sb.toString());
   }
   
   private static String replaceTexts(final String source, final Event event) {
     final StringBuilder internal = new StringBuilder();
     final StringBuilder external = new StringBuilder();
     
     final Extensions extensions = event.getExtensions();
     if (extensions != null) {
       for (final Extension extension : extensions.getExtension()) {
         final String text = extension.getText();
         if (isInternalRef(text)) {
           addConstant(internal, text);
         } else {
           addImport(external, removeLastSegment(text));
         }
       }
     }
     
     for (final String text : event.getTexts().getText()) {
       if (isInternalRef(text)) {
         addConstant(internal, text);
       } else {
         addImport(external, removeLastSegment(text));
       }
     }
     
     for (final Option option : event.getOptions().getOption()) {
       final String text = option.getText();
       if (isInternalRef(text)) {
         addConstant(internal, text);
       } else {
         addImport(external, removeLastSegment(text));
       }
     }
     
     return source.replaceAll("%texts%", internal.toString()).replaceAll("%importTexts%", external.toString());
   }
   
   private static String replaceImages(final String source, final Event event) {
     final StringBuilder internal = new StringBuilder();
     final StringBuilder external = new StringBuilder();
     
     final Images images = event.getImages();
     if (images != null) {
       final String bg = images.getBg();
       if (bg != null) {
         if (isInternalRef(bg)) {
           addConstant(internal, bg);
         } else {
           addImport(external, removeLastSegment(bg));
         }
       }
       for (final String image : images.getFg()) {
         if (isInternalRef(image)) {
           addConstant(internal, image);
         } else {
           addImport(external, removeLastSegment(image));
         }
       }
     }
     
     return source.replaceAll("%images%", internal.toString()).replaceAll("%importImages%", external.toString());
   }
   
   private static String removeLastSegment(final String fqcn) {
     return fqcn.substring(0, fqcn.lastIndexOf("."));
   }
   
   private static boolean isInternalRef(final String reference) {
     return reference.matches("[A-Z_]+");
   }
   
   private static void addImport(final StringBuilder sb, final String fqcn) {
     sb.append("import ");
     sb.append(fqcn);
     sb.append(";\n");
   }
   
   private static void addConstant(final StringBuilder sb, final String constantValue) {
     sb.append("    ");
     sb.append(constantValue);
     sb.append(",\n");
   }
   
   public static void buildAll() {
     final List<File> sourceFiles = new LinkedList<>();
     for (final File xmlFile : FileUtils.getAllFilesRecursive(PathFinder.getModEventsXmlDir())) {
       sourceFiles.addAll(generateSource(xmlFile));
     }
     if (!sourceFiles.isEmpty()) {
       PathFinder.getModEventsClassDir().mkdirs();
       final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
       final List<String> arguments = new LinkedList<>();
       arguments.add("-d");
       arguments.add(PathFinder.getModEventsClassDir().getAbsolutePath());
       for (final File srcFile : sourceFiles) {
         arguments.add(srcFile.getAbsolutePath());
       }
       final int result = compiler.run(null, null, null, arguments.toArray(new String[arguments.size()]));
       if (result != 0) {
         // TODO
       }
     }
   }
 }
