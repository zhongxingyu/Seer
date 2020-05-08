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
            Integer chNumber = randomGenerator.nextInt(3);
            if (id != chNumber) {
                channels[id][chNumber].push("Read");
                System.out.println("Process " + id + " pushed 'read' to " + chNumber + " channel");

 
             }
 
         }
         //reading phase
        for (int i = 0; i < 2; i++) {
             if (id != i) {
                 System.out.println("Process " + id + " got " + channels[i][id].pull() + " from " + i);

             }
 
         }
     }
 
     public void send(String data) {
 
     }
 
 //
 //    public void addChannel()
 
 }
