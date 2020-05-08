 package au.edu.griffith.ict;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Scanner;
 
 /**
 * 
  * @author Daniel Devine
  */
 public class Menu implements Manager{
     /**
      * A map of ItemIDNumber to MenuItem
      */
     private HashMap<Integer, MenuItem> menuItems = new HashMap<Integer, MenuItem>();
     
     /** 
      * Keeps track of the next available ID number 
      */
     private int nextIDNo;
     
     /**
      * A reference to the file that the menu is serialised to and deserialised from. 
      */
     private File file;
     
     /** 
     * Get a MenuItem from the menu identified by it's item number.
     * @param itemNo The identifier for a particular item.
     * @return A MenuItem object that matches the identifier, else null.
     */
     public MenuItem getItem(int itemNo){
         return menuItems.get(itemNo);
      }
     
     /**
     * This method does not make sense.
     */
     public void updateItem(MenuItem item){
         this.save();
     }
     
     /**
     * Deserialise a Menu from a database given a file to use as a database.
     * @param file The file to use as the database.
     * @return Return True if the database was found and successfully loaded.
     */
     @Override
     public boolean loadDatabase(String file) {
         try{
             this.file = new File(file);
             this.file.createNewFile();
             
             Scanner sc = new Scanner(this.file);
             String s;
             while(sc.hasNextLine()){
                 try{
                     s = sc.nextLine();
                     String[] data = s.split("--");
                     
                     int id = Integer.parseInt(data[0]);
                     String name = data[1];
                     float price = Float.parseFloat(data[2]);
                     
                     MenuItem item = new MenuItem(id, name, price);
                     
                     menuItems.put(item.getItemNo(), item);
                     
                     nextIDNo = Math.max(nextIDNo, item.getItemNo() + 1);
                 }
                 catch(Exception e){
                     e.printStackTrace(); //DEBUG
                     //Something went wrong with loading this item. (Corrupt?)
                     continue; //Continue trying the next
                 }
             }
             sc.close();
         }
         catch(IOException e){
             e.printStackTrace(); //DEBUG
             return false;
         }
         return true;
     }
     
     /**
     * Serialise the MenuItems on the Menu and write them to the database.
     */
     private void save(){
          try{
              PrintStream ps = new PrintStream(file);
              for(MenuItem it : menuItems.values()){
                  if(it == null) continue;
                  ps.print(it.getItemNo() + "--");
                  ps.print(it.getName() + "--");
                  ps.print(it.getPrice() + "--");
                  ps.println();
              }
              ps.close();
          }
          catch(IOException e){
              //Irrecoverable error
              e.printStackTrace();
          }
     }
 
     /**
     * Add a new item to the menu.
     * @param item The item to be added to the menu.
     */
     public void newItem(MenuItem item){
         menuItems.put(item.getItemNo(), item);
         this.save();
     }
     
     /**
     * Increment and return the next available Item ID number.
     * @return An int representing the next unused item ID number.
     */
     public int nextItemNo(){
         return nextIDNo++;
     }
 
     /**
     * Remove an item from the menu.
     * @param itemNo The item number of the item to be removed.
     * @return Returns True if the item was successfully removed. If the item number does not exist within the menu it will return False.
     */
     public boolean removeItem(int itemNo){
         if(menuItems.remove(itemNo) != null){
             this.save();
             return true;
         }
         return false; //Not in the item list
     }
     
     /**
      * Returns a text representation of the menu in the format:
      * ID    Item    Price
      * 0    Pizza    $9.50
      * 1    Apples    $0.30
      */
     @Override
     public String toString(){
         StringBuilder sb = new StringBuilder();
         sb.append(String.format("%-3s %-20.20s Price\n", "ID", "Item"));
         for(MenuItem item : menuItems.values()){
             if(item == null) continue;
             sb.append(String.format("%-3d %-20.20s $%.2f\n", item.getItemNo(), item.getName(), item.getPrice()));
         }
         return sb.toString();
     }
 }
