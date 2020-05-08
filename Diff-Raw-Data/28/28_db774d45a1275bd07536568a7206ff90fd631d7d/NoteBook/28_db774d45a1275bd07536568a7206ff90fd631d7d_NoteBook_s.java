 import java.io.*;
 
 /**
 * Created with IntelliJ IDEA.
  * User: Анна
  * Date: 05.04.13
  * Time: 7:58
 * To change this template use File | Settings | File Templates.
  */
 public class NoteBook {
     private final File file;
 
     //создавание файла
     public NoteBook() throws IOException {
         // File f;
         file = new File("myfile.txt");
         if (!file.exists()) {
             file.createNewFile();
             System.out.println("New file \"myfile.txt\" has been created in the current directory");
         }
     }
 
     //добавление данных в файл file формат txt
     public void Add() throws Exception {
         String name;
         BufferedReader reader;
         reader = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("What is your name? ");
         name = reader.readLine();
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line = null;
         while ((line = br.readLine()) != null) {
             if (line.trim().equals(name)) {
                 System.out.print(name + " is already created. Rename your contact");
                 Add();
 
                 throw new Exception(name + " is already created. Rename your contact");
             }
             line = br.readLine();
         }
 
         String content = "";
         String lineSeparator = System.getProperty("line.separator");   // перевод на след. строку в файле
         content += lineSeparator + name;
         FileWriter fw = new FileWriter(file, true);
         BufferedWriter bw = new BufferedWriter(fw);
         bw.write(content);
 
         String phone;
         System.out.print("Please, enter your phone ");
         phone = reader.readLine();
         String content2 = "";
         content2 += lineSeparator + phone;
         FileWriter fw2 = new FileWriter(file, true);
         BufferedWriter bw2 = new BufferedWriter(fw);
         bw.write(content2);
         bw.close();
         // fw.close();
         bw2.close();
         //fw2.close();
 
 
     }
 
     // удаление выбранных данных
     public void remove() throws IOException {
         String nameToRemove;
         BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
         System.out.println("Enter the contact to remove -> ");
         nameToRemove = br1.readLine();
 
         try {
             //Construct the new file that will later be renamed to the original filename.
             File tempFile = new File(file.getAbsolutePath() + ".tmp");
             BufferedReader br = new BufferedReader(new FileReader(file));
             PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
            String line = null;
             //Read from the original file and write to the new
             //unless content matches data to be removed.
             while ((line = br.readLine()) != null) {
                 if (!line.trim().equals(nameToRemove)) {
 
                     pw.println(line);
                     pw.flush();
                 } else {
                     line = br.readLine();
                 }
             }
             pw.close();
             br.close();
 
             //Delete the original file
             if (!file.delete()) {
                 System.out.println("Could not delete file");
                 return;
             }
 
             //Rename the new file to the filename the original file had.
             if (!tempFile.renameTo(file))
                 System.out.println("Could not rename file");
 
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         }
 
     }
 
     //поиск номера по имени. Метод возвращает номер, записанный в файле на строчку ниже, чем имя.
    public String searchByName() throws IOException {
         String name;
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         System.out.println("Please, enter the name to search -> ");
         name = br.readLine();
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br2.readLine()) != null) {
 
                if (line.trim().equals(name)) {
                    return br2.readLine();
                }
             }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }
 
     public String searchByPhone() throws IOException {
         String phone;
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         System.out.println("Please, enter the phone to search -> ");
         phone = br.readLine();
         try {
             BufferedReader br2 = new BufferedReader(new FileReader(file));
             String line = null;
             String line2 = null;
             while ((line2 = br2.readLine()) != null) {
                 if (line2.trim().equals(phone)) {
                     return line;
                 }
                 line = line2;
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }
 
     public void Open() throws IOException {
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line = null;
         while ((line = br.readLine()) != null) {
             System.out.println(line + " ");
         }
     }
 
     public void Commander() throws Exception {
         final NoteBook c = new NoteBook();
         String command;
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         System.out.println("Enter the command -> ");
         command = br.readLine();
         if (command.equals("open")) {
             c.Open();
         }
         if (command.equals("searchByName")) {
             System.out.println(c.searchByName());
         }
         if (command.equals("searchByPhone")) {
             System.out.println(c.searchByPhone());
         }
         if (command.equals("remove")) {
             c.remove();
         }
         if (command.equals("add")) {
             c.Add();
         }
         if (!command.equals("add") && !command.equals("remove") && !command.equals("open") && !command.equals("searchByName") && !command.equals("searchByPhone")) {
             System.out.println("your command is impossible. " +
                     "Possible commands: open, searchByName, searchByPhone, remove or add. Please,enter the command again -> ");
             c.Commander();
         }
     }
 
 
     public static void main(String[] args) throws Exception {
         final NoteBook c = new NoteBook();
         //c.Commander();
         //c.Commander();
         c.Add();
 
     }
 }
