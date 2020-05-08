 package hmmtagger;
 
 /**
  *
  * @author Nikolay Borisov
  */
 public enum State {
 
     I_GENE("I-GENE", 0),
     O("O", 1);
     private String name;
     private int id;
 
     State(String name, int id) {
 
         this.name = name;
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public int getId() {
         return id;
     }
     
     public static State getStateFromId(int id) {
         switch (id) {
             case 0:
                 return I_GENE;
 
             case 1:
                 return O;
         }
             
        throw new IllegalArgumentException("Unrecognised state id");
     }
     
     public static int getStateSize() { return 2; }
 }
