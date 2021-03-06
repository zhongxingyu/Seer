 /*
  ** 2013 July 01
  **
  ** The author disclaims copyright to this source code.  In place of
  ** a legal notice, here is a blessing:
  **    May you do good and not evil.
  **    May you find forgiveness for yourself and forgive others.
  **    May you share freely, never taking more than you give.
  */
 package info.ata4.unity.extract.handler;
 
 import info.ata4.unity.serdes.UnityObject;
 import info.ata4.unity.struct.ObjectPath;
 import java.io.IOException;
 
 /**
  *
  * @author Nico Bergemann <barracuda415 at yahoo.de>
  */
 public class TextAssetHandler extends ExtractHandler {
 
     @Override
     public String getClassName() {
         return "TextAsset";
     }
     
     @Override
     public String getFileExtension() {
         return "txt";
     }
 
     @Override
     public void extract(ObjectPath path, UnityObject obj) throws IOException {
         String name = obj.getValue("m_Name");
         String script = obj.getValue("m_Script");
        writeFile(script.getBytes(), path.pathID, name);
     }
 }
