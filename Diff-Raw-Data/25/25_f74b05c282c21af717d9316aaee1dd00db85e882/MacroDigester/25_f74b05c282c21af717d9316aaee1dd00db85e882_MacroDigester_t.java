 package net.sozinsoft.tokenlab;
 
 import net.rptools.maptool.client.ui.MacroButtonHotKeyManager;
 import net.rptools.maptool.model.MacroButtonProperties;
 import org.apache.commons.digester3.Digester;
 import org.xml.sax.SAXException;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: lhayhurst
  * Date: 11/11/11
  * Time: 9:39 AM
  * To change this template use File | Settings | File Templates.
  */
 public class MacroDigester {
 
     private String macroXMLFile;
 
     public MacroDigester( String macroXML ) {
       this.macroXMLFile = macroXML;
     }
 
     public void parseConfigFile() throws IOException, SAXException {
 
         Digester d = new Digester();
         d.push( this );
 
         /* they look like this:
          <macro name="Character Sheet"
            group="Generic"
            sortPrefix="1"
            buttonColor="black"
            fontColor="white"
            fontSize="1.10em"
            minWidth="323"
            applyToSelectedTokens="0"
            file="res/macros/CharacterSheet.txt">
          */
 
        d.addCallMethod( "macros/macro", "addMacro", 10 );
         d.addCallParam( "macros/macro", 0, "name" );
         d.addCallParam( "macros/macro", 1, "group" );
         d.addCallParam( "macros/macro", 2, "sortPrefix" );
         d.addCallParam( "macros/macro", 3, "buttonColor" );
         d.addCallParam( "macros/macro", 4, "fontColor" );
         d.addCallParam( "macros/macro", 5, "fontSize" );
         d.addCallParam( "macros/macro", 6, "minWidth" );
         d.addCallParam( "macros/macro", 7, "applyToSelectedTokens" );
        d.addCallParam( "macros/macro", 8, "allowPlayersToEdit");
        d.addCallParam( "macros/macro", 9, "file" );
 
         d.parse( ResourceManager.getMacroConfigFile() );
     }
 
     private HashMap<String, HashMap<String, MacroEntry >> macroEntries = new HashMap<String, HashMap<String, MacroEntry>>();
 
     public void addMacro( String name, String group, String sortPrefix, String buttonColor, String fontColor,
                          String fontSize, String minWidth, String applyToSelectedTokens, String allowPlayersToEdit, String file  ) {
         MacroEntry me = new MacroEntry( name, group , sortPrefix , buttonColor , fontColor , fontSize,
                                        minWidth , applyToSelectedTokens , allowPlayersToEdit, file );
         HashMap<String, MacroEntry> groupMap = null;
         if ( macroEntries.containsKey(me.group)) {
             groupMap = macroEntries.get(me.group);
         }
         else {
             groupMap =   new HashMap<String, MacroEntry>();
             macroEntries.put( me.group, groupMap );
         }
         groupMap.put( me.name, me );
     }
 
     public HashMap<String, MacroEntry> getGroup( String group ) {
         return macroEntries.get( group );
     }
 
 
 
     public class MacroEntry {
         String name;
         String group;
         String sortPrefix;
         String buttonColor;
         String fontColor;
         String fontSize;
         String minWidth;
         String applyToSelectedTokens;
        String allowPlayersToEdit;
         String file;
         String toolTip = "";
 
         public MacroEntry(String name, String group, String sortPrefix, String buttonColor, String fontColor, String fontSize,
                          String minWidth, String applyToSelectedTokens, String allowPlayersToEdit, String file ) {
             this.name = name;
             this.group = group;
             this.sortPrefix = sortPrefix ;
             this.buttonColor  = buttonColor ;
             this.fontColor  = fontColor ;
             this.fontSize  = fontSize ;
             this.minWidth  = minWidth;
             this.applyToSelectedTokens  = applyToSelectedTokens ;
            this.allowPlayersToEdit = allowPlayersToEdit;
             this.file = file;
         }
 
         public void rejiggerIndex( int byAmount ) {
             int currentIndex = Integer.parseInt( sortPrefix );
             int newIndex = currentIndex + byAmount;
             sortPrefix = new Integer( newIndex).toString();
         }
         private String readFileAsString(String filePath) throws java.io.IOException {
             byte[] buffer = new byte[(int) new File(filePath).length()];
             BufferedInputStream f = null;
             try {
                 f = new BufferedInputStream(new FileInputStream(filePath));
                 f.read(buffer);
             } finally {
                 if (f != null) try {
                     f.close();
                 } catch (IOException ignored) {
                 }
             }
             return new String(buffer);
         }
 
         public MacroButtonProperties getMacroButtonProperties(int index, IMacroReplacer replacer ) throws IOException {
 
             boolean applyToTokens = false;
             if ( applyToSelectedTokens.equals("1")) {
                 applyToTokens  = true;
             }
            MacroButtonProperties mbp = new MacroButtonProperties(index, //index
                     this.buttonColor, //colorKey,
                     MacroButtonHotKeyManager.HOTKEYS[0], //hotkey
                     replacer.replace( readFileAsString(ResourceManager.getResouceByName( this.file ).getAbsolutePath() ) ),
                     this.name, //label
                     this.group, //group
                     this.sortPrefix, //sortby
                     true,  //autoexecute
                     false, //includeLabel
                     applyToTokens, //applyToTokens
                     this.fontColor, //fontcolor
                     this.fontSize, //fontSize
                     this.minWidth, //minWidth
                     this.minWidth, //maxWidth
                     this.toolTip); //toolTip
            mbp.setAllowPlayerEdits( allowPlayersToEdit.equals("1")? true : false );
            return mbp;
         }
     }
 }
