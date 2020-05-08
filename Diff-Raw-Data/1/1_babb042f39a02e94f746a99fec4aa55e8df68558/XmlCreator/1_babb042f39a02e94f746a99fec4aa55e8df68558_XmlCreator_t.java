 package polly.core.persistence;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import polly.core.plugins.Plugin;
 import polly.core.plugins.PluginManagerImpl;
 
 public class XmlCreator {
 
     private EntityList entities;
     private DatabaseProperties properties;
     private String persistenceUnit;
     private PluginManagerImpl pluginManager;
     private String pluginFolder;
     
     
     public XmlCreator(EntityList entities, DatabaseProperties properties,
             String persistenceUnit, PluginManagerImpl pluginManager, 
             String pluginFolder) {
         this.entities = entities;
         this.properties = properties;
         this.persistenceUnit = persistenceUnit;
         this.pluginManager = pluginManager;
        this.pluginFolder = pluginFolder;
     }
     
     
     
     public void writePersistenceXml(String path) throws IOException {
         File file = new File(path);
         PrintStream out = new PrintStream(file);
         this.buildPersistenceXml(out);
         out.flush();
         out.close();
     }
 
 
     
     private void buildPersistenceXml(PrintStream s) {
         s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         s.append("<persistence version=\"1.0\" xmlns=\"http://java.sun.com/xml/ns/persistence\"\n");
         s.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
         s.append("    xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence\n");
         s.append("    http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd\">\n\n");
         s.append("<persistence-unit name=\"");
         s.append(this.persistenceUnit);
         s.append("\" transaction-type=\"RESOURCE_LOCAL\">\n\n");
         s.append("    <!-- Internal settings -->\n");
         s.append("    <provider>org.eclipse.persistence.jpa.PersistenceProvider</provider>\n");
         s.append("    <description>Auto generated persistence.xml file</description>\n");
         s.append("    <exclude-unlisted-classes>false</exclude-unlisted-classes>\n\n");
         
         s.append("    <!-- plugin jar file references -->\n");
         for (Plugin pluginCfg : this.pluginManager.loadedPlugins()) {
             s.append("    <jar-file>file:");
             s.append("../" + this.pluginFolder + "/"); 
             s.append(pluginCfg.getProperty(Plugin.JAR_FILE));
             s.append("</jar-file>\n");
         }
         s.append("\n");
         
         s.append("    <!-- plugin entity classes -->\n");
         this.entities.toString(s);
         s.append("\n");
         
         s.append("    <!-- property settings -->\n");
         this.properties.toString(s);
         s.append("</persistence-unit>\n");
         s.append("</persistence>");
     }
     
     
 
 }
