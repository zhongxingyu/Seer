 public abstract class AbstractFilm implements Film {
 
     private String myName, myProducer, myDirector;
     private int myYear, myLength;
     private String[] myActors;
 
     public AbstractFilm(String name, String producer, String director,
                         int year, String[] actors, int length) {
         myName = name;
         myProducer = producer;
         myDirector = director;
         myYear = year;
         myActors = actors;
         myLength = length;
     }
     public AbstractFilm(){
         myName = "";
         myProducer = "";
         myDirector = "";
         myYear = 0;
         myActors = new String[]{};
         myLength = 0;
     }
 
     public final void setName(String name){myName = name;}
     public final void setProducer(String producer){myProducer = producer;}
     public final void setDirector(String director){myDirector = director;}
     public final void setYear(int year){myYear = year;}
     public final void setActors(String[] actors){myActors = actors;}
     public final void setLength(int length){myLength = length;}
 
     public final String getName(){return myName;}
     public final String getProducer(){return myProducer;}
     public final String getDirector(){return myDirector;}
     public final int getYear(){return myYear;}
     public final String[] getActors(){return myActors;}
     public final int getLength(){return myLength;}
 
     public String toString(){
         return "\"" + getName() + "\"" + ":\n" +
             "\tProducer: " + getProducer() + "\n" +
             "\tDirector: " + getDirector() + "\n" +
             "\tYear: " + getYear() + "\n" +
             "\tActors: " + formatActors() + "\n" +
             "\tLength: " + formatLength() + "\n";
     }
 
     public final String formatActors(){
         String result = "";
         String[] actors = getActors();
         for (int i = 0; i < actors.length-1; i++) {
             result += actors[i] + ", ";
         }
         result += actors[actors.length-1];
         return result.toString();
     }
 
     public final String formatLength(){
         int length = getLength();
         int hours = (int)length/60;
         length -= hours*60;
         int minutes = (int)length;
         length -= minutes;
         return hours + ":" + minutes;
     }
     public int compareTo(AbstractFilm other){
         return awards() - other.awards();
     }
     abstract public int awards();
 }
