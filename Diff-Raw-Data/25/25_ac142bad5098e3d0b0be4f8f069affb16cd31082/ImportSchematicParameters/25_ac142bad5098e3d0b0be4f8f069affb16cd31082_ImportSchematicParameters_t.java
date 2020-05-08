 package jo.sm.plugins.ship.imp;
 
 import jo.sm.ui.act.plugin.Description;
 
@Description(displayName="Import Schematic Model", shortDescription="Import a model in Schematic format from your disk")
 public class ImportSchematicParameters
 {
     @Description(displayName="File", shortDescription="Path to Schematic model")
     private String  mFile;
     
     public ImportSchematicParameters()
     {
     }
 
     public String getFile()
     {
         return mFile;
     }
 
     public void setFile(String file)
     {
         mFile = file;
     }
 }
