 package com.versionone.taskview.views;
 
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
 
 public class Configuration {
 
 private static final String CONFIGURATION_FILE = "configuration.xml";
 //    private GridSettings gridSettings = new GridSettings();
 //    private ProjectTreeSetttings projectTreeSettings = new ProjectTreeSetttings();
     public AssetDetailSettings assetDetailSettings;
     public String apiVersion = "8.3";
     private static Configuration configuration;
 
     public Configuration() {
         // TODO temporary
         assetDetailSettings = new AssetDetailSettings();
     }
 
     public static Configuration getInstance() {
         if (configuration == null){
             //TODO load from XML
             configuration = new Configuration();
             
            try {
                JAXBContext jc = JAXBContext.newInstance(Configuration.class);
            } catch (JAXBException e) {
                throw new RuntimeException("Cannot read " + CONFIGURATION_FILE, e);
            }
         }
         return configuration;
     }
 
     public static class AssetDetailSettings {
         private ColumnSetting[] taskColumns;
         private ColumnSetting[] storyColumns;
         private ColumnSetting[] testColumns;
         private ColumnSetting[] defectColumns;
 
         public AssetDetailSettings() {
             // TODO temporary
             taskColumns = new ColumnSetting[6];
             testColumns = new ColumnSetting[0];
             storyColumns = new ColumnSetting[0];
             defectColumns = new ColumnSetting[0];
             
             taskColumns[0] = new ColumnSetting("ColumnTitle'Title", "String", "Name", "Main", false, false);
             taskColumns[1] = new ColumnSetting("ColumnTitle'Description", "RichText", "Description", "Main", false, false);
             taskColumns[2] = new ColumnSetting("ColumnTitle'Project", "String", "Scope.Name", "Main", false, false);
             taskColumns[3] = new ColumnSetting("ColumnTitle'Parent", "String", "Parent.Name", "Main", false, false);
             taskColumns[4] = new ColumnSetting("ColumnTitle'Owner", "Multi", "Owners", "Extended", false, false);
             taskColumns[5] = new ColumnSetting("ColumnTitle'Status", "List", "Status", "Extended", false, false);
         }
 
         public ColumnSetting[] getColumns(String type) {
             if (type.equals("Story")) {
                 return storyColumns;
             } else if (type.equals("Task")) {
                 return taskColumns;
             } else if (type.equals("Defect")) {
                 return defectColumns;
             } else if (type.equals("Test")) {
                 return testColumns;
             } else {
                 throw new IllegalArgumentException("Unknown type: " + type);
             }
         }
     }
     
     public static class ColumnSetting {
         public String name;
         public String type;
         public String attribute;
         public String category;
         public boolean readOnly;
         public boolean effortTracking;
         public int width = 100;
         
         ColumnSetting(String name, String type, String attribute, String category, boolean readOnly,
                 boolean effortTracking) {
             super();
             this.name = name;
             this.type = type;
             this.attribute = attribute;
             this.category = category;
             this.readOnly = readOnly;
             this.effortTracking = effortTracking;
         }
     }
 }
 /*
     [XmlRoot("Configuration")]
     public class Configuration {
         private GridSettings gridSettings = new GridSettings();
         private ProjectTreeSetttings projectTreeSettings = new ProjectTreeSetttings();
         private AssetDetailSettings assetDetailSettings = new AssetDetailSettings();
         private string apiVersion = "8.3";
         private static Configuration configuration;
 
         private void Save(Stream stream) {
             XmlSerializer xmlSerializer = new XmlSerializer(typeof(Configuration));
 
             xmlSerializer.Serialize(stream, this);
         }
 
         private static Configuration Load(Stream stream) {
             XmlSerializer xmlSerializer = new XmlSerializer(typeof(Configuration));
 
             return (Configuration)xmlSerializer.Deserialize(stream);
         }
 
         public GridSettings GridSettings {
             get { return gridSettings; }
             set { gridSettings = value; }
         }
 
         public ProjectTreeSetttings ProjectTree {
             get { return projectTreeSettings; }
             set { projectTreeSettings = value; }
         }
 
         public AssetDetailSettings AssetDetail {
             get { return assetDetailSettings; }
             set { assetDetailSettings = value; }
         }
 
         public string APIVersion {
             get { return apiVersion; }
             set { apiVersion = value; }
 
         }
 
 
         private static string ConfigurationFile {
             get {
                 DirectoryInfo info = new DirectoryInfo(Assembly.GetCallingAssembly().Location);
                 return info.Parent.FullName + "\\configuration.xml";
             }
         }
 
         public static Configuration Instance {
             get {
                 if (configuration == null) {
                     using (FileStream configurationFile = File.OpenRead(ConfigurationFile)) {
                         configuration = Load(configurationFile);
                     }
                 }
                 return configuration;
             }
         }
     }
 
     public class GridSettings {
         private string attributeSelection;
         private string storyAttributeSelection;
         private ColumnSetting[] columns;
 
         public string StoryAttributeSelection {
             get { return storyAttributeSelection; }
             set { storyAttributeSelection = value; }
         }
 
         public string AttributeSelection {
             get { return attributeSelection; }
             set { attributeSelection = value; }
         }
 
         public ColumnSetting[] Columns {
             get { return columns; }
             set { columns = value; }
         }
     }
 
     public class ProjectTreeSetttings {
         private ColumnSetting[] columns;
 
         public ColumnSetting[] Columns {
             get { return columns; }
             set { columns = value; }
         }
     }
 
     public class AssetDetailSettings {
         private ColumnSetting[] taskColumns;
                 private ColumnSetting[] storyColumns;
         private ColumnSetting[] testColumns;
                 private ColumnSetting[] defectColumns;
 
         public ColumnSetting[] TaskColumns {
             get { return taskColumns; }
             set { taskColumns = value; }
         }
 
         public ColumnSetting[] StoryColumns {
             get { return storyColumns; }
             set { storyColumns = value; }
         }
 
         public ColumnSetting[] TestColumns {
             get { return testColumns; }
             set { testColumns = value; }
         }
 
         public ColumnSetting[] DefectColumns {
             get { return defectColumns; }
             set { defectColumns = value; }
         }
 
         public ColumnSetting[] GetColumns(string type) {
             switch (type) {
                 case "Story":
                     return storyColumns;
                 case "Task":
                     return taskColumns;
                 case "Defect":
                     return defectColumns;
                 case "Test":
                     return testColumns;
                 default:
                     throw new ArgumentException("Unknown type: " + type);
             }
         }
     }
 
     public class ColumnSetting {
         private string name;
         private string type;
         private string attribute;
         private string category;
         private bool readOnly;
         private bool effortTracking;
         private int width = 100;
 
         public string Attribute {
             get { return attribute; }
             set { attribute = value; }
         }
 
         public string Type {
             get { return type; }
             set { type = value; }
         }
 
         public string Name {
             get { return name; }
             set { name = value; }
         }
 
         public bool ReadOnly {
             get { return readOnly; }
             set { readOnly = value; }
         }
 
         public string Category {
             get { return category; }
             set { category = value; }
         }
 
         public bool EffortTracking {
             get { return effortTracking; }
             set { effortTracking = value; }
         }
 
         public int Width {
             get { return width; }
             set { width = value; }
         }
     }
 }
  */
