 
 /**
  * Write a description of class Filesystem here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Filesystem
 {
     private static Directory root = new Directory("/");
     //Set the default Directory to root   
     private static Directory currentDirectory = root;
         //Set exits for all maps
        // filesystem.setExit("mainframe", mainframe);
        // filesystem.setExit("home", mainframeHome);
            
     /**
      * Add the Child directories to their parents
      */
     public static void createFilesystem()
     {
         //De directories
         Directory root = new Directory("/");
         Directory home = new Directory("home", root);
         Directory userdir = new Directory(GameController.getUsername(), home);
         Directory mainframe = new Directory("mainframe", root);
         Directory mainframeEtc = new Directory("etc", mainframe);
         Directory mainframeBin = new Directory("bin", mainframe);
         Directory mainframeHome = new Directory("home", mainframe);
         Directory mainframeTmp = new Directory("tmp", mainframe);
         Directory mainframeVar = new Directory("var", mainframe);
         Directory earl = new Directory("earl", "My Name=", mainframeHome);
         Directory thomas = new Directory("thomas", "1337", mainframeHome);
         Directory et = new Directory("et", "Go Home", mainframeHome);
         Directory henry = new Directory("henry", "Frankenstein", mainframeHome);    
         Directory www = new Directory("www", mainframeVar);
 
         //addChilds
        root.addChild(home);
        home.addChild(userdir);        
        root.addChild(mainframe);
        mainframe.addChild(mainframeEtc);
        mainframe.addChild(mainframeHome);
        mainframe.addChild(mainframeBin);
        mainframe.addChild(mainframeTmp);
        mainframe.addChild(mainframeVar);
        mainframeHome.addChild(earl);
        mainframeHome.addChild(thomas);
        mainframeHome.addChild(et);
        mainframeHome.addChild(henry); 
        mainframeVar.addChild(www);
             
         //The Files
         mainframeEtc.addFile(new File("passwords", "user:thomas \npassword:1337 \nuser:et \npassword:Go Home \nuser:earl \npassword:My Name= \nuser:henry \npassword:Frankenstein", 10));
         mainframeEtc.addFile(new File("shadow", "6zI18EUZW4watEJ15NEmk0GpQjrAJU5h5BVWPMxwnPdjtdQdqhpRf7bWQo+4TcB2XkRKYL1q+hYZkDaLSi4+BltcmIYeWq0Y9ByNU7LPJsF11vkfGp8LoSaKBMwWG23DJNKk/tVH2Lu9R/Z60HhdhbbWQo+4TcB2CNLlRpNkZ8tMaPesK9xEog==", 50));
         mainframeEtc.addFile(new File("profile", "", 1));
         mainframeEtc.addFile(new File("networks", "", 1));
         mainframeEtc.addFile(new File("modules", "", 1));
         mainframeEtc.addFile(new File("host.conf", "", 1));
         mainframeEtc.addFile(new File("sevices", "", 18));
         mainframeBin.addFile(new File("bash", "", 712));
         mainframeBin.addFile(new File("cat", "", 30));
         mainframeBin.addFile(new File("cp", "", 74));
         mainframeBin.addFile(new File("ls", "", 94));
         mainframeTmp.addFile(new File("aliendb.sql","#\n# Database structure for database 'alien'\n#\n\nCREATE DATABASE /*!32312 IF NOT EXISTS*/ \"alien\" /*!40100 DEFAULT CHARACTER SET latin1 */;\n\nUSE \"alien\";\n\n\n#\n# Table structure for table 'administration'\n#\n\nCREATE TABLE /*!32312 IF NOT EXISTS*/ \"administration\" (\n  \"idAlien\" int(11) NOT NULL,\n  \"alienSpecie\" varchar(45) DEFAULT NULL,\n  \"picture\" varchar(45) DEFAULT NULL,\n  \"length\" int(11) DEFAULT NULL,\n  \"weight\" varchar(45) DEFAULT NULL,\n  PRIMARY KEY (\"idAlien\")\n);\n\n\n\n#\n# Dumping data for table 'administration'\n#\n\nLOCK TABLES \"administration\" WRITE;\n/*!40000 ALTER TABLE \"administration\" DISABLE KEYS;*/\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n    (0,'Vulcan','http://memory-alpha.org/en/wiki/Vulcan',179,'63');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n   (1,'Klingon','http://memory-alpha.org/en/wiki/Klingon',150,'100');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n    (2,'Romulan','http://memory-alpha.org/en/wiki/Romulan',90,'10');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n  (3,'Cardassien','http://memory-alpha.org/en/wiki/Cardassian',120,'40');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n   (4,'Ferengi','http://memory-alpha.org/en/wiki/Ferengi',50,'20');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n  (5,'Borg','http://memory-alpha.org/en/wiki/Borg',102,'16');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n   (6,'Bejoran','http://memory-alpha.org/en/wiki/Bajoran',393,'23');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n (7,'Betazoid','http://memory-alpha.org/en/wiki/Betazoid',42,'89');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n    (8,'wookiee','http://musicselections.files.wordpress.com/20',200,'54');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n   (9,'umpa lumpa','http://sipr.net/oompa_loompa.jpg',120,'40');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n (10,'tribble','http://www.product-reviews.net/wp-content/use',10,'3');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n    (11,'tril','http://memory-alpha.org/en/wiki/Trill',24,'3');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n   (12,'goa''uld',NULL,127,'30');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n    (13,'wraith','http://memory-alpha.org/en/wiki/Wraith',579,'440');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n (14,'furling',NULL,376,'146');\nREPLACE INTO \"administration\" (\"idAlien\", \"alienSpecie\", \"picture\", \"length\", \"weight\") VALUES\n    (15,'jem''hadar',NULL,234,'78');\n/*!40000 ALTER TABLE \"administration\" ENABLE KEYS;*/\nUNLOCK TABLES;\n/*!40101 SET SQL_MODE=@OLD_SQL_MODE;*/\n/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;*/", 14));
     }
     /**
      * Get the current opened directory
      * 
      * @return  return the current opened directory
      */
     public static Directory getCurrentDirectory()
     {
         return currentDirectory;
     }
     
     /**
      * Set the current opened directory
      * 
      * @param   Directory dir   The directory which should be opened.  
      */
     public static void setCurrentDirectory(Directory dir)
     {
         currentDirectory = dir;
     }
     
     /**
      * Find an object of the class Directory with a specific name
      * 
      * @param   String name     The name of the directory one searches
      * @return  directory       The object of the class Directory. returns null if not found.
      */
     public static Directory findDirectoryByName(String name)
     {
         Directory directory = null;
         for(Directory dir : currentDirectory.getChilds())
         {
             if(dir.getName().equals(name) == true)
             {
                 directory = dir;
             }
         }
         return directory;
     }
    public static void print()
    {
        for(Directory dir : currentDirectory.getChilds()){
       System.out.println(dir);}
    }
 }
