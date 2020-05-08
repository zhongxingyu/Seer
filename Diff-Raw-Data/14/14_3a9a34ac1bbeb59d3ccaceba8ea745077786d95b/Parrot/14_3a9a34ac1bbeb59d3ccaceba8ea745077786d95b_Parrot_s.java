 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  * hi
  */
 package pirate.pets;
 
 import pirate.seamen.Vernacularrrgh;
 
 /**
  *
  * @author Simon
  */
 public class Parrot {
     
     String name;
     String owner;
     public boolean canTalk = true;
     
     public Parrot (String name, String owner, boolean talk) {
         this.name = name + " the Parrot";
         this.owner =  owner + "'s parrot";
         talk = canTalk;
     }
     
    public String talk {
         String noise;
         
         if (canTalk = true) {
             if (Math.random() < 0.7) {
                String noise = "Polly want a cracker!";
             } else {
                String noise = Vernacularrrgh.SENTENCES[(int)(Math.random() * Vernacularrrgh.SENTENCES.length)];
                 for(int i = 0; i < Vernacularrrgh.PARTS_OF_SPEECH.length; i++){
                     while(noise.contains(Vernacularrrgh.PARTS_OF_SPEECH[i])) {//sentence.indexOf(name) > -1){
                         noise = noise.replaceFirst("\\([^)]*" + Vernacularrrgh.PARTS_OF_SPEECH[i] + "\\)", Vernacularrrgh.get(Vernacularrrgh.PARTS_OF_SPEECH[i]));
                     }
                 }
             }
         }
        return "Squawk!" + noise
     }
 }
