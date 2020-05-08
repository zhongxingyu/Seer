 import java.util.Date;
 import java.lang.Math;
 
 public class Song implements Timespan{
 
   public Song(String name, int duration, Date come){
     this.name=name;
     this.duration=duration;
     this.come=come;
     this.gone=new Date(Long.MAX_VALUE);
   }
   
   public Song(String name, int duration){
     this(name,duration,new Date());
   }
   
   public String getName(){return name;}
   public Date getBegin(){return come;}
   public Date getEnd() { return gone; }
   
   public Song remove(){
     Song s = new Song (name, duration);
     s.come=this.come;
     
     s.gone=new Date();
     return s;
   }
   
   public String toString() {
    int temp= duration%60;
    int min = (int)(duration/60) ;
     return (name+ " - "+min+":"+temp);
   }
 
   protected String name;
   protected int duration;
   protected Date come;
   protected Date gone;
 }
