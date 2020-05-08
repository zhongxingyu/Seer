 package im.yuri.jcom;
 
 import java.io.*;
 import java.net.*;
 import java.util.UUID;
 import java.util.Random;
 
 public class Process implements Runnable {
 
 
     private Channel[][] channels;
     private Integer id;
     public Process(Integer id, Channel[][] channels) {
       this.id = id;
       this.channels = channels;
     }
 
     public void run() {
       Random randomGenerator = new Random();
         //writing phase
         for (int i = 0; i < 2; i++) {
             //writing to random channels 5 times
            Integer chNumber = randomGenerator.nextInt(2);
            if (id != i) {
                channels[id][i].push("Read");
                System.out.println("Process " + id + " pushed 'read' to " + i + " channel");
                System.out.println(System.nanoTime());
 
             }
 
         }
         //reading phase
        for (int i = 0; i < 5; i++) {
             if (id != i) {
                 System.out.println("Process " + id + " got " + channels[i][id].pull() + " from " + i);
                System.out.println(System.nanoTime());
             }
 
         }
     }
 
     public void send(String data) {
 
     }
 
 //
 //    public void addChannel()
 
 }
