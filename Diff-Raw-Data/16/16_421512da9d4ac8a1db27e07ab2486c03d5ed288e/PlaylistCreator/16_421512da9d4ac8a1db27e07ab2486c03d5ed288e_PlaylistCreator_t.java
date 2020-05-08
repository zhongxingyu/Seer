 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lastfmlove;
 
 import java.util.ArrayList;
 import java.io.PrintWriter;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 
 /**
  *
  * @author Тим
  */
 public class PlaylistCreator {
     
     public void create(ArrayList<Song> song_list) throws FileNotFoundException {
          PrintWriter pls;
         int n = 0;
          pls = new PrintWriter(new FileOutputStream("lastfmlove.pls"));
          pls.println("[playlist]");
          for (int i = 0; i < song_list.size(); i++)
          {
             if (!song_list.get(i).path.isEmpty() && song_list.get(i).length != 0)
             {
                n++;
                pls.println("File"+n+"="+song_list.get(i).path);
                pls.println("Length"+n+"="+song_list.get(i).length);
             }
          }
         pls.println("NumberOfEntries="+n);
         pls.println("Version=2");  
          pls.close();
         
     }
     
 }
