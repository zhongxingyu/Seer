 package com.hazelcast.heartattack;
 
 import com.hazelcast.config.Config;
 import com.hazelcast.config.XmlConfigBuilder;
 import com.hazelcast.core.Hazelcast;
 import com.hazelcast.core.HazelcastInstance;
 import com.hazelcast.core.IMap;
 import com.hazelcast.logging.ILogger;
 import com.hazelcast.logging.Logger;
 
 import java.io.FileNotFoundException;
 import java.net.InetSocketAddress;
 import java.util.logging.Level;
 
 public class Trainee {
 
     public static final String TRAINEE_PARTICIPANT_MAP = "Trainee:ParticipantMap";
     public static final String TRAINEE_EXECUTOR = "Trainee:Executor";
 
     private String traineeId;
     private HazelcastInstance hz;
     private IMap<String, InetSocketAddress> map;
     private String traineeHzFile;
 
     public void setTraineeId(String traineeId) {
         this.traineeId = traineeId;
     }
 
     private void setTraineeHzFile(String traineeHzFile) {
         this.traineeHzFile = traineeHzFile;
     }
 
     public void start() {
         this.hz = createHazelcastInstance();
         this.map = hz.getMap(TRAINEE_PARTICIPANT_MAP);
         this.map.put(traineeId, hz.getCluster().getLocalMember().getInetSocketAddress());
     }
 
     public HazelcastInstance createHazelcastInstance() {
         XmlConfigBuilder configBuilder;
         try {
             configBuilder = new XmlConfigBuilder(traineeHzFile);
         } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
         }
 
         Config config = configBuilder.build();
         return Hazelcast.newHazelcastInstance(config);
     }
 
     public static void main(String[] args) {
         System.out.println("Starting Hazelcast Heart Attack Trainee");
 
         String traineeId = args[0];
         System.out.println("Trainee id:" + traineeId);
         String traineeHzFile = args[1];
         System.out.println("Trainee hz config file:" + traineeHzFile);
 
         System.setProperty("traineeId",traineeId);
 
         Trainee trainee = new Trainee();
         trainee.setTraineeId(traineeId);
         trainee.setTraineeHzFile(traineeHzFile);
         trainee.start();
 
        System.out.println( "Successfully started Hazelcast Heart Attack Trainee:"+traineeId);
     }
 }
