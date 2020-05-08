 package rpg;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 /**
  * @author Mcat12
  */
 public class SaveLoadEngine {
    /*
     * Edit the save method to set all the character's properties you need
     * to save (ex. name=GridBag), then edit the load method to modify your 
     * variables according to the .properties file loaded.
     */
    
    protected static FileNameExtensionFilter filter = new FileNameExtensionFilter("Properties File", "properties");
     protected static Properties gameProp = new Properties();
    
     /**
      * Save Method
      */
     public static void save() {
         javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
         fc.setMultiSelectionEnabled(false);
         fc.setAcceptAllFileFilterUsed(false);
         fc.setFileFilter(filter);
         int retval = fc.showSaveDialog(fc);
         gameProp.setProperty("hp", Integer.toString(RPG.hp));
         gameProp.setProperty("name", RPG.name);
         gameProp.setProperty("level", Integer.toString(RPG.level));
         gameProp.setProperty("gold", Integer.toString(RPG.gold));
         gameProp.setProperty("maxhp", Integer.toString(RPG.maxhp));
         gameProp.setProperty("class", RPG.whatclass);
         gameProp.setProperty("strength", Integer.toString(RPG.strength));
         gameProp.setProperty("magic", Integer.toString(RPG.magic));
         gameProp.setProperty("initiative", Integer.toString(RPG.initiative));
         gameProp.setProperty("exp", Integer.toString(RPG.exp));
         gameProp.setProperty("positionX", Integer.toString(RPGFrame.x));
         gameProp.setProperty("positionY", Integer.toString(RPGFrame.y));
         gameProp.setProperty("wep1", RPG.inventory[0]);
         gameProp.setProperty("wep2", RPG.inventory[1]);
         gameProp.setProperty("wep3", RPG.inventory[2]);
         gameProp.setProperty("wep4", RPG.inventory[3]);
         gameProp.setProperty("wep5", RPG.inventory[4]);
         gameProp.setProperty("wep1pow", Integer.toString(RPG.inventorypower[0]));
         gameProp.setProperty("wep2pow", Integer.toString(RPG.inventorypower[1]));
         gameProp.setProperty("wep3pow", Integer.toString(RPG.inventorypower[2]));
         gameProp.setProperty("wep4pow", Integer.toString(RPG.inventorypower[3]));
         gameProp.setProperty("wep5pow", Integer.toString(RPG.inventorypower[4]));
         /*
          * Here, add in:
          * 
          * gameProp.setProperty(String keyword, String value);
          * 
          * for all the character's properties so it gets the values of the 
          * character for the new .properties file.
          * ex.
          * gameProp.setProperty("hp", RPG.character.getHp());
          */
         if (retval == JFileChooser.APPROVE_OPTION) {
             try {
                 File f = fc.getSelectedFile();
                 if (!f.getPath().toLowerCase().endsWith(".properties")) {
                     f = new File(f.getPath() + ".properties");
                 }
                 gameProp.store(new FileOutputStream(f), null);
             } catch (IOException ioe) {
                 System.out.println("You must first open a save file.");
                 System.out.println(ioe.getMessage());
             }
         }
     }
 
     /**
      * Load Method
      */
     public static void load() {
         javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
         fc.setMultiSelectionEnabled(false);
         fc.setAcceptAllFileFilterUsed(false);
         fc.setFileFilter(filter);
         int retval = fc.showOpenDialog(fc);
         fc.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);
         File f = fc.getSelectedFile();
         try {
             if (retval == JFileChooser.APPROVE_OPTION) {
                 gameProp.load(new FileInputStream(f));
                 RPG.hp = Integer.parseInt(gameProp.getProperty("hp"));
                 RPG.name = gameProp.getProperty("name");
                 RPG.level = Integer.parseInt(gameProp.getProperty("level"));
                 RPG.gold = Integer.parseInt(gameProp.getProperty("gold"));
                 RPG.maxhp = Integer.parseInt(gameProp.getProperty("maxhp"));
                 RPG.whatclass = gameProp.getProperty("class");
                 RPG.strength = Integer.parseInt(gameProp.getProperty("strength"));
                 RPG.magic = Integer.parseInt(gameProp.getProperty("magic"));
                 RPG.initiative = Integer.parseInt(gameProp.getProperty("initiative"));
                 RPG.exp = Integer.parseInt(gameProp.getProperty("exp"));
                 RPGFrame.x = Integer.parseInt(gameProp.getProperty("positionX"));
                 RPGFrame.y = Integer.parseInt(gameProp.getProperty("positionY"));
                 RPG.inventory[0] = gameProp.getProperty("wep1");
                 RPG.inventory[1] = gameProp.getProperty("wep2");
                 RPG.inventory[2] = gameProp.getProperty("wep3");
                 RPG.inventory[3] = gameProp.getProperty("wep4");
                 RPG.inventory[4] = gameProp.getProperty("wep5");
                 RPG.inventorypower[0] = Integer.parseInt(gameProp.getProperty("wep1pow"));
                 RPG.inventorypower[1] = Integer.parseInt(gameProp.getProperty("wep2pow"));
                 RPG.inventorypower[2] = Integer.parseInt(gameProp.getProperty("wep3pow"));
                 RPG.inventorypower[3] = Integer.parseInt(gameProp.getProperty("wep4pow"));
                 RPG.inventorypower[4] = Integer.parseInt(gameProp.getProperty("wep5pow"));
 
                 /*
                  * Here add in code to change the character's values (HP)
                  * to match the .properties file, loading the character.
                  * ex.
                  * RPG.character.setHp(gameProp.getProperty("hp"));
                  * 
                  * The "hp" is the keyword you used to save the .properties
                  * file.
                  */
             }
         } catch (FileNotFoundException e) {
             System.out.println("FileNotFoundException");
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("IOException");
             e.printStackTrace();
         }
     }
 }
