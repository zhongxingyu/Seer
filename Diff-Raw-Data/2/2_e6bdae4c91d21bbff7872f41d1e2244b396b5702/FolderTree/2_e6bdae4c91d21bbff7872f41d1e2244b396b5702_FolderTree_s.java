 package folder;
 
 import file.File;
 import file.FileClient;
 import org.apache.log4j.Logger;
 import pl.edu.pjwstk.mteam.jcsync.core.implementation.collections.JCSyncHashMap;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 /**
  * Klasa przechowująca strukturę plików we współdzielonym folderze.
  *
  * @author Marcin Weiss
  * @version 0.9
  */
 public class FolderTree implements Serializable {
 
     public static final Logger LOG = Logger.getLogger(FolderTree.class);
 
     /**
      *
      */
     private static final long serialVersionUID = -7730198483613602338L;
     public boolean updated = false;
     public HashMap<String, Nod> folder = new HashMap<String, Nod>();
     public JCSyncHashMap<String, Nod> syncFolder;
     // klucz to dla korzenia "root"
     // dla użytkownika jego ID
     // dla pliku ID użytkownika + ID pliku
     public String usr;
 
     /**
      * @param path - ścieżka do folderu ze współdzoelonymi plikami
      * @param usr  - nazwa użytkownika uruchamiającego udział
      * @param tree -
      */
     public FolderTree(String path, String usr, JCSyncHashMap<String, Nod> tree, String ip, int port) {
         LOG.info("FolderTree constructor");
         this.syncFolder = tree;
         LOG.info("Drzewo podpięte");
         this.usr = usr;
         LOG.info("User dodany");
         LOG.info("Root dodany do drzewa");
         if (this.syncFolder != null) {
             LOG.info("SyncFolder nie ejst NULLe,");
             if (this.syncFolder.containsKey("root")) {
                 LOG.info("Jest Root");
                 this.folder.put("root", syncFolder.get("root"));
                 this.updated = true;
             } else {
                 this.folder.put("root", new Nod(path));
                 this.syncFolder.put("root", folder.get("root"));
                 LOG.info("dodano root");
             }
 
 
             new Thread(new FolderServer(this, usr, path)).start();
             LOG.info("Uruchomiono nowy FolderServer");
         } else {
             this.folder.put("root", new Nod(path));
             LOG.info("JCSync jest null-em");
         }
         this.addUser(usr, path, ip, port);
         LOG.info("FolderTree created with path: " + path + " for user: " + usr + " IP:" + ip + ":" + port);
     }
 
     public HashMap<String, Nod> getFolder() {
         return folder;
     }
 
     /**
      * Metoda dodająca użytkownika
      *
      * @param usr  - ID użytkownika
      * @param path - ścieżka do folderu lokalna dla użytkownika
      */
     public void addUser(String usr, String path, String ip, int port) {
         LOG.info("adding new user " + usr);
         Nod n = new Nod(usr, folder.get("root"), ip, port);
         System.out.println(n);
         System.out.println("Użytkownik :" + usr);
         System.out.println("folder w :" + path);
         n.setPath(path);
         folder.put(usr, n);
 
         if (this.syncFolder != null) {
         	System.out.println("root syncFolder: "+ this.syncFolder.get("root"));
         	System.out.println("jego dzieci: "+ this.syncFolder.get("root").getChildren());
         	if(this.syncFolder.get("root").getChildren().size()>0 && !this.syncFolder.get("root").getChildren().contains("usr")){
        		this.syncFolder.get("root").getChildren().add("usr");
         	}
             this.syncFolder.put(usr, n);
         }
         updated = true;
         LOG.info("User added");
     }
 
     public Nod getRoot() {
         return folder.get("root");
     }
 
     public void setRoot(Nod root) {
         this.getFolder().put("root", root);
         updated = true;
     }
 
     @Override
     public String toString() {
         String s = "FolderTree [root=" + this.getFolder().get("root").getName() + "]" + "\n";
 
         for (String k : this.getFolder().get("root").getChildren()) {
             Nod n = this.getFolder().get(k);
             s += "\n\t";
             s += n.name;
             s += " ";
             s += n.ip;
             for (String c : n.getChildren()) {
                 if (folder.get(c).getHistory().getLast() != null) {
                     s += "\n\t\t" + folder.get(c).getHistory().getLast().getData();
                 } else {
                     s += "\n\t\twykasowany";
                 }
                 s += "\n\t\t";
                 s += c;
             }
         }
         return s;
     }
 
     public void setFolder(JCSyncHashMap<String, Nod> folder) {
         this.folder = folder;
         updated = true;
     }
 
     public void addFile(File f, String usr) {
         LOG.info("Method addFile(" + f.getFilePath() + "," + usr);
         Nod file = new Nod(usr + f.getFileName(), folder.get(usr), f.getSingleFileHistory(), folder.get(usr), f.getFileName(), f.getFilePath());
         file.setParent(folder.get(usr));
         synchronized(folder){
         	folder.put(usr + file.getName(), file);
         }
 
         if (this.syncFolder != null) {
             System.out.println("Dodaje do drzewaJCsync: " + usr + file.getName());
             synchronized(this.syncFolder){
             	this.syncFolder.put(usr + file.getName(), file);
             }
         }
         updated = true;
     }
 
     /**
      * Metoda aktualizująca historie pliku o najnowszy wpis z listy lokalnej
      *
      * @param f
      * @param usr - ID użytkownika
      */
     public void updateFile(File f, String usr) {
         Nod file = folder.get(usr + f.getFileName());
         if (file == null) {
             addFile(f, usr);
         } else if (file.history.getLast().getData() < f.getSingleFileHistory().getLast().getData()) {
             file.history.add(f.getSingleFileHistory().getLast());
         }
         updated = true;
     }
 
     /**
      * Metoda przechodzi po liscie lokalnych plików i ich historii porównując je
      * z plikami trzymanymi w drzewie synchronizowanym przez JCSync
      *
      * @param usr - ID Użytkownika
      */
     public void updateAll(String usr) {
         for (File f : MFolderListener.filesAndTheirHistory.values()) {
             if (!f.getSingleFileHistory().getLast().equals(this.folder.get(usr + f.getFileName()))) {
                 this.updateFile(f, usr);
             }
         }
     }
 
     public void putAll(HashMap<String, Nod> f) {
         LinkedList<String> usrs = new LinkedList<String>();
         for (Nod n : f.values()) {
             if (f.get("root").getChildren().contains(n.getName())) {
                 usrs.add(n.getName());
                 folder.put(n.value, n);
                 if (!folder.get("root").children.contains(n.getName())) {
                     folder.get("root").children.add(n.getName());
                 }
             } else if (!n.getName().equals("root")) {
                 addNod(n);
             }
         }
     }
 
     public void putAll() {
         LinkedList<String> usrs = new LinkedList<String>();
         for (Nod n : this.syncFolder.values()) {
             if (this.syncFolder.get("root").getChildren().contains(n.getName())) {
                 usrs.add(n.getName());
                 folder.put(n.value, n);
                 if (!folder.get("root").children.contains(n.getName())) {
                     folder.get("root").children.add(n.getName());
                 }
             } else if (!n.getName().equals("root")) {
                 addNod(n);
             }
         }
     }
 
     public void addNod(Nod n) {
         if (!folder.containsKey(n.value)) {
             folder.put(n.value, n);
         } else if (n.getHistory().size() > 0 && n.getHistory().getLast() == null) {
             folder.get(n.getValue()).getHistory().add(null);
         } else if (folder.get(n.value).getHistory().size() > 0 && (folder.get(n.value).getHistory().getLast().getData() < n.getHistory().getLast().getData())) {
             folder.get(n.value).getHistory().add(n.getHistory().getLast());
         }
     }
 
     /**
      * Aktualizacja drzewa
      *
      * @param folder2
      */
     public void update(HashMap<String, Nod> folder2) {
         this.putAll(folder2);//dodawanie nowych
 
         LinkedList<String> users = new LinkedList<String>();
         LinkedList<Nod> changes = new LinkedList<Nod>();
         LinkedList<Nod> created = new LinkedList<Nod>();
 
 
         Nod rootLocal = folder.get("root");
         System.out.println("\nusrs " + rootLocal.getChildren());
         System.out.println(this);
         //plik zaktualizowano
         for (Nod n : folder.values()) {
             if (!n.getParent().equals(usr) && folder.get(n.getValue()).getHistory().size() > 0 && folder.get(usr + n.getName()) != null) {
 
                 if (n.getHistory().getLast() == null && folder.get(usr + n.getName()).getHistory().getLast() != null) {
                     changes.add(n);
                 } else if (folder.get(usr + n.getName()).getHistory().getLast() == null) {
                     //raz wykasowany jest ignorowany
                 } else if (folder.get(usr + n.getName()).getHistory().getLast().getData() < n.getHistory().getLast().getData()) {
                     System.out.println("zmieniono " + n.name);
                     changes.add(n);
                 }
             }
         }
         users.addAll(folder.get("root").getChildren());
 
         //pliki utworzone
         for (Nod n : folder.values()) {
             if (!n.getValue().equals("root") && !users.contains(n.getName())) {
                 if (!folder.containsKey(usr + n.getName())) {
                     System.out.println("stworzono " + n.name);
                     System.out.println(n.getPath());
                     System.out.println(n.getValue());
                     System.out.println(n.getPort());
                     System.out.println(n.getIp());
                     created.add(n);
                 }
             }
         }
         for (Nod nod : created) {
             if (nod.getHistory().getLast() != null) {
                 System.out.println("FolderTree: żądanie pliku " + nod.getParent() + nod.getName());
                 System.out.println("FolderTree: czas zmiany zdalnego: " + folder.get(nod.getParent() + nod.getName()).getHistory().getLast().getData());
                 System.out.println("przez port: " + folder.get(nod.getParent()).port);
                 @SuppressWarnings("unused")
                 FileClient fileClient = new FileClient(this, nod, folder.get(nod.getParent()).ip, nod.getParent() + nod.getName(), folder.get(usr).getPath(), nod.getName(), usr, folder.get(nod.getParent()).port);
             } else {
                 //kod kasujący plik
                 MFolderListener.deleteFileFromDisc(folder.get(usr).getPath() + nod.getName());
                 if (this.getFolder().containsKey(usr + nod.getName())) {
                     this.getFolder().get(usr + nod.getName()).history.add(null);
                     this.updated = true;
                 } else {
                     File deletedFile = new File(nod.getName(), nod.getPath(), usr);
                     deletedFile.setFileId(nod.history.getFirst().getFileId());
                     addFile(deletedFile, usr);
                 }
             }
         }
         for (Nod nod : changes) {
             if (nod.getHistory().getLast() != null) {
                 System.out.println("FolderTree: żądanie pliku " + nod.getParent() + nod.getName());
                 System.out.println("FolderTree: czas zmiany zdalnego: " + folder.get(nod.getParent() + nod.getName()).getHistory().getLast().getData());
                 System.out.println("przez port: " + folder.get(nod.getParent()).port);
                 System.err.println(this);
                 System.err.println(nod);
                 System.err.println(folder.get(nod.getParent()).ip);
                 System.err.println(nod.getParent() + nod.getName());
                 System.err.println(folder.get(usr).getPath());
                 System.err.println(nod.getName());
                 System.err.println(usr);
                 System.err.println(folder.get(nod.parent).getPort());
                 @SuppressWarnings("unused")
                 FileClient fileClient = new FileClient(this, nod, folder.get(nod.getParent()).ip, nod.getParent() + nod.getName(), folder.get(usr).getPath(), nod.getName(), usr, folder.get(nod.getParent()).port);
             } else {
                 //kod kasujący plik
                 System.out.println("Kasuje plik: " + nod.name);
                 System.out.println("Ścieżka: " + folder.get(usr).getPath() + System.getProperty("file.separator") + nod.getName());
                 MFolderListener.deleteFileFromDisc(folder.get(usr).getPath() + System.getProperty("file.separator") + nod.getName());
                 if (this.getFolder().containsKey(usr + nod.getName())) {
                     this.getFolder().get(usr + nod.getName()).history.add(null);
                     this.updated = true;
                 } else {
                     File deletedFile = new File(nod.getName(), nod.getPath(), usr);
                     deletedFile.setFileId(nod.history.getFirst().getFileId());
                     addFile(deletedFile, usr);
                 }
             }
         }
     }
 
     public void update() {
         this.putAll(syncFolder);//dodawanie nowych
         
         LinkedList<String> users = new LinkedList<String>();
         LinkedList<Nod> changes = new LinkedList<Nod>();
         LinkedList<Nod> created = new LinkedList<Nod>();
 
 
         Nod rootLocal = folder.get("root");
         System.out.println("\nusrs " + rootLocal.getChildren());
         System.out.println(this);
         //plik zaktualizowano
         for (Nod n : this.folder.values()) {
             if (!n.getParent().equals(usr) && folder.get(n.getValue()).getHistory().size() > 0 && folder.get(usr + n.getName()) != null) {
                 if (n.getHistory().getLast() == null && folder.get(usr + n.getName()).getHistory().getLast() != null) {
                     changes.add(n);
                 } else if (folder.get(usr + n.getName()).getHistory().getLast() == null) {
                     //raz wykasowany jest ignorowany
                 } else if (folder.get(usr + n.getName()).getHistory().getLast().getData() < n.getHistory().getLast().getData()) {
                     System.out.println("zmieniono " + n.name);
                     changes.add(n);
                 }
             }
         }
         users.addAll(folder.get("root").getChildren());
         System.out.println("usrs: " + users);
         System.out.println("usrs jcsync: " + folder.get("root").getChildren());
         System.out.println("usrs: " + folder.get("root").getChildren());
         //pliki utworzone
         for (Nod n : this.folder.values()) {
             if (!n.getValue().equals("root") && !users.contains(n.getName())) {
                 if (!folder.containsKey(usr + n.getName())) {
                     System.out.println("stworzono " + n.name);
                     System.out.println(n.getPath());
                     System.out.println(n.getValue());
                     System.out.println(n.getPort());
                     System.out.println(n.getIp());
                     created.add(n);
                 }
             }
         }
 
         for (Nod nod : created) {
             //if (nod.getHistory().getLast() != null) {
 
             if (nod.getHistory()!=null && !nod.getHistory().isEmpty() && nod.getHistory().getLast()!=null) {
                 System.out.println("FolderTree: rządanie pliku " + nod.getParent() + nod.getName());
                 System.out.println("FolderTree: czas zmiany zdalneg: " + folder.get(nod.getParent() + nod.getName()).getHistory().getLast().getData());
                 @SuppressWarnings("unused")
                 FileClient fileClient = new FileClient(this, nod, this.folder.get(nod.getParent()).ip, nod.getParent() + nod.getName(), folder.get(usr).getPath(), nod.getName(), usr, folder.get(nod.getParent()).port);
             } else if (nod.getHistory()!=null && !nod.getHistory().isEmpty() && nod.getHistory().getLast()==null){
                 //kod kasujący plik
                 MFolderListener.deleteFileFromDisc(folder.get(usr).getPath() + nod.getName());
                 if (this.getFolder().containsKey(usr + nod.getName())) {
                     this.getFolder().get(usr + nod.getName()).history.add(null);
                     this.updated = true;
                 } else {
                     File deletedFile = new File(nod.getName(), nod.getPath(), usr);
                     deletedFile.setFileId(nod.history.getFirst().getFileId());
                     addFile(deletedFile, usr);
                 }
             }
         }
 //        for (Nod nod : created) {
 //            try {
 //                // if (nod.getHistory().getLast() != null) {
 //                System.out.println("FolderTree: rządanie pliku " + nod.getParent() + nod.getName());
 //                System.out.println("FolderTree: czas zmiany zdalneg: " + folder.get(nod.getParent() + nod.getName()).getHistory().getLast().getData());
 //                @SuppressWarnings("unused")
 //                FileClient fileClient = new FileClient(this, nod, this.syncFolder.get(nod.getParent()).ip, nod.getParent() + nod.getName(), folder.get(usr).getPath(), nod.getName(), usr, folder.get(nod.getParent()).port);
 //            } catch (Exception e) {
 //                LOG.info("nod.getHistory().getLast() == null");
 //
 //
 //            }
 //                //kod kasujący plik
 //                MFolderListener.deleteFileFromDisc(folder.get(usr).getPath() + nod.getName());
 //                if (this.getFolder().containsKey(usr + nod.getName())) {
 //                    this.getFolder().get(usr + nod.getName()).history.add(null);
 //                    this.updated = true;
 //                } else {
 //                    File deletedFile = new File(nod.getName(), nod.getPath(), usr);
 //                        deletedFile.setFileId(nod.history.getFirst().getFileId());
 //                        addFile(deletedFile, usr);
 //
 //                }
 //
 //
 //        }
         for (Nod nod : changes) {
             //if (nod.getHistory().getLast() != null) {
             if (!nod.getHistory().isEmpty()) {
                 @SuppressWarnings("unused")
                 FileClient fileClient = new FileClient(this, nod, this.folder.get(nod.getParent()).ip, nod.getParent() + nod.getName(), folder.get(usr).getPath(), nod.getName(), usr, folder.get(nod.getParent()).port);
             } else {
                 //kod kasujący plik
                 MFolderListener.deleteFileFromDisc(folder.get(usr).getPath() + System.getProperty("file.separator") + nod.getName());
                 if (this.getFolder().containsKey(usr + nod.getName())) {
                     this.getFolder().get(usr + nod.getName()).history.add(null);
                     this.updated = true;
                 } else if(nod.history!=null){
                     File deletedFile = new File(nod.getName(), nod.getPath(), usr);
                     deletedFile.setFileId(nod.history.getFirst().getFileId());
                     addFile(deletedFile, usr);
                 }
             }
         }
         for (Nod nod : changes) {
             if (nod.getHistory().getLast() != null) {
                 System.out.println("FolderTree: żądanie pliku " + nod.getParent() + nod.getName());
                 System.out.println("FolderTree: czas zmiany zdalnego: " + folder.get(nod.getParent() + nod.getName()).getHistory().getLast().getData());
                 System.out.println("przez port: " + folder.get(nod.getParent()).port);
                 System.err.println(this);
                 System.err.println(nod);
                 System.err.println(folder.get(nod.getParent()).ip);
                 System.err.println(nod.getParent() + nod.getName());
                 System.err.println(folder.get(usr).getPath());
                 System.err.println(nod.getName());
                 System.err.println(usr);
                 System.err.println(folder.get(nod.parent).getPort());
                 @SuppressWarnings("unused")
                 FileClient fileClient = new FileClient(this, nod, folder.get(nod.getParent()).ip, nod.getParent() + nod.getName(), folder.get(usr).getPath(), nod.getName(), usr, folder.get(nod.getParent()).port);
             } else {
                 //kod kasujący plik
                 System.out.println("Kasuje plik: " + nod.name);
                 System.out.println("Ścieżka: " + folder.get(usr).getPath() + System.getProperty("file.separator") + nod.getName());
                 MFolderListener.deleteFileFromDisc(folder.get(usr).getPath() + System.getProperty("file.separator") + nod.getName());
                 if (this.getFolder().containsKey(usr + nod.getName())) {
                     this.getFolder().get(usr + nod.getName()).history.add(null);
                     this.updated = true;
                 } else {
                     File deletedFile = new File(nod.getName(), nod.getPath(), usr);
                     deletedFile.setFileId(nod.history.getFirst().getFileId());
                     addFile(deletedFile, usr);
                 }
             }
         }
         synchronized(this.syncFolder){
         	this.syncFolder.putAll(folder);
         }
     }
 }
