 /*
  * Author - Jan Dufek, dufeja@gmail.com
  * Copying and using only with permission of the author.
  */
 package cz.sortivo.sklikapi;
 
 /**
  *
  * @author Jan Dufek
  */
 public class Attributes {
     
     private Status status;
    private Integer cpc;
     
     public Attributes(Status status){
         this.status = status;
     }
 
     public Status getStatus() {
         return status;
     }
 
     public void setStatus(Status status) {
         this.status = status;
     }
     
     
 }
