/*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package checktubes;
 
 /**
  *
  * @author Vasily Glazunov
  */
 public class InfoProbirka {
     
     public String examid;
     public int id_tube;
     public int count;
     public String name;
     
     public InfoProbirka(String examid, int id_tube, int count) {
         this(examid, id_tube, count, "");
     }
     
     public InfoProbirka(String examid, int id_tube, int count, String name) {
         this.examid = examid;
         this.id_tube = id_tube;
         this.count = count;
         this.name = name;
     }
     
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof InfoProbirka)) {
            return false; 
         }
         InfoProbirka info = (InfoProbirka) obj;
         return (this.examid.equals(info.examid) && this.id_tube == info.id_tube && this.count == info.count );
     }
     
 
     
 }
