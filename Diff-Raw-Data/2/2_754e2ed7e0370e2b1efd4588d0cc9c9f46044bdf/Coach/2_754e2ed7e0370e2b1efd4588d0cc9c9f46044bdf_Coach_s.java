 package com.hazelcast.heartattack;
 
 import com.hazelcast.client.HazelcastClient;
 import com.hazelcast.client.config.ClientConfig;
 import com.hazelcast.config.Config;
 import com.hazelcast.config.XmlConfigBuilder;
 import com.hazelcast.core.*;
 import com.hazelcast.logging.ILogger;
 import com.hazelcast.logging.Logger;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 
 import static com.hazelcast.heartattack.Utils.*;
 import static java.lang.String.format;
 
 public abstract class Coach {
 
     final static ILogger log = Logger.getLogger(Coach.class.getName());
 
     public static final String KEY_COACH = "Coach";
     public static final String COACH_HEART_ATTACK_QUEUE = "Coach:headCoachCount";
 
     public final static File userDir = new File(System.getProperty("user.dir"));
     public final static String classpath = System.getProperty("java.class.path");
     public final static File heartAttackHome = getHeartAttackHome();
     public final static File traineesHome = new File(getHeartAttackHome(), "trainees");
 
     protected File coachHzFile;
     protected volatile HazelcastInstance coachHz;
     protected volatile HazelcastInstance traineeClient;
     protected volatile IExecutorService traineeExecutor;
     protected volatile IQueue<HeartAttack> heartAttackQueue;
     protected volatile Exercise exercise;
     private final List<TraineeJvm> traineeJvms = Collections.synchronizedList(new LinkedList<TraineeJvm>());
 
     public Exercise getExercise() {
         return exercise;
     }
 
     public void setExercise(Exercise exercise) {
         this.exercise = exercise;
     }
 
     public void setCoachHzFile(File coachHzFile) {
         this.coachHzFile = coachHzFile;
     }
 
     public File getCoachHzFile() {
         return coachHzFile;
     }
 
     protected HazelcastInstance initCoachHazelcastInstance() {
         FileInputStream in;
         try {
             in = new FileInputStream(coachHzFile);
         } catch (FileNotFoundException e) {
             throw new RuntimeException(e);
         }
 
         Config config;
         try {
             config = new XmlConfigBuilder(in).build();
         } finally {
             closeQuietly(in);
         }
         config.getUserContext().put(KEY_COACH, this);
         coachHz = Hazelcast.newHazelcastInstance(config);
         heartAttackQueue = coachHz.getQueue(COACH_HEART_ATTACK_QUEUE);
         new Thread(new HeartAttackMonitor()).start();
         return coachHz;
     }
 
     private class HeartAttackMonitor implements Runnable {
         public void run() {
             for (; ; ) {
                 for (TraineeJvm jvm : traineeJvms) {
                     HeartAttack heartAttack = null;
 
                     if (heartAttack == null) {
                         heartAttack = detectHeartAttackFile(jvm);
                     }
 
                     if (heartAttack == null) {
                         heartAttack = detectUnexpectedExit(jvm);
                     }
 
                     if (heartAttack == null) {
                         heartAttack = detectMembershipFailure(jvm);
                     }
 
                     if (heartAttack != null) {
                         traineeJvms.remove(jvm);
                         heartAttackQueue.add(heartAttack);
                     }
                 }
 
                 Utils.sleepSeconds(1);
             }
         }
 
         private HeartAttack detectMembershipFailure(TraineeJvm jvm) {
             //if the jvm is not assigned a hazelcast address yet.
             if (jvm.getAddress() == null) {
                 return null;
             }
 
             Member member = findMember(jvm);
             if (member == null) {
                 jvm.getProcess().destroy();
                 return new HeartAttack("Hazelcast membership failure (member missing)",
                         coachHz.getCluster().getLocalMember().getInetSocketAddress(),
                         jvm.getAddress(),
                         jvm.getId(),
                         exercise);
             }
 
             return null;
         }
 
         private Member findMember(TraineeJvm jvm) {
             if (traineeClient == null) return null;
 
             for (Member member : traineeClient.getCluster().getMembers()) {
                 if (member.getInetSocketAddress().equals(jvm.getAddress())) {
                     return member;
                 }
             }
 
             return null;
         }
 
         private HeartAttack detectHeartAttackFile(TraineeJvm jvm) {
             File file = new File(traineesHome, jvm.getId() + ".heartattack");
             if (!file.exists()) {
                 return null;
             }
             HeartAttack heartAttack = new HeartAttack(
                     "out of memory",
                     coachHz.getCluster().getLocalMember().getInetSocketAddress(),
                     jvm.getAddress(),
                     jvm.getId(),
                     exercise);
             jvm.getProcess().destroy();
             return heartAttack;
         }
 
         private HeartAttack detectUnexpectedExit(TraineeJvm jvm) {
             Process process = jvm.getProcess();
             try {
                 if (process.exitValue() != 0) {
                     return new HeartAttack(
                             "exit code not 0",
                             coachHz.getCluster().getLocalMember().getInetSocketAddress(),
                             jvm.getAddress(),
                             jvm.getId(),
                             exercise);
                 }
             } catch (IllegalThreadStateException ignore) {
             }
             return null;
         }
     }
 
     public void spawnTrainees(TraineeSettings settings) throws Exception {
         File traineeHzFile = File.createTempFile("trainee-hazelcast", "xml");
         traineeHzFile.deleteOnExit();
         Utils.write(traineeHzFile, settings.getHzConfig());
 
         List<TraineeJvm> trainees = new LinkedList<TraineeJvm>();
 
         for (int k = 0; k < settings.getTraineeCount(); k++) {
             TraineeJvm trainee = startTraineeJvm(settings.getVmOptions(), traineeHzFile);
             Process process = trainee.getProcess();
             String traineeId = trainee.getId();
 
             trainees.add(trainee);
 
             new TraineeLogger(traineeId, process.getInputStream(), settings.isTrackLogging()).start();
         }
 
         Config config = new XmlConfigBuilder(traineeHzFile.getAbsolutePath()).build();
         ClientConfig clientConfig = new ClientConfig().addAddress("localhost:" + config.getNetworkConfig().getPort());
         clientConfig.getGroupConfig()
                 .setName(config.getGroupConfig().getName())
                 .setPassword(config.getGroupConfig().getPassword());
         traineeClient = HazelcastClient.newHazelcastClient(clientConfig);
         traineeExecutor = traineeClient.getExecutorService(Trainee.TRAINEE_EXECUTOR);
 
         for (TraineeJvm trainee : trainees) {
             waitForTraineeStartup(trainee);
         }
     }
 
     private TraineeJvm startTraineeJvm(String traineeVmOptions, File traineeHzFile) throws IOException {
         String traineeId = "" + System.currentTimeMillis();
 
         String[] clientVmOptionsArray = new String[]{};
         if (traineeVmOptions != null && !traineeVmOptions.trim().isEmpty()) {
             clientVmOptionsArray = traineeVmOptions.split("\\s+");
         }
 
        String java = System.getProperties().getProperty("java.home") + File.pathSeparator + "bin" + File.pathSeparator + "java";
         List<String> args = new LinkedList<String>();
         args.add(java);
         args.add(format("-XX:OnOutOfMemoryError=\"\"touch %s.heartattack\"\"", traineeId));
         args.add("-DHEART_ATTACK_HOME=" + getHeartAttackHome());
         args.add("-Dhazelcast.logging.type=log4j");
         args.add("-Dlog4j.configuration=file:" + heartAttackHome + File.separator + "conf" + File.separator + "trainee-log4j.xml");
         args.add("-cp");
         args.add(classpath);
         args.addAll(Arrays.asList(clientVmOptionsArray));
         args.add(Trainee.class.getName());
         args.add(traineeId);
         args.add(traineeHzFile.getAbsolutePath());
 
         ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(new String[args.size()]))
                 .directory(traineesHome)
                 .redirectErrorStream(true);
         Process process = processBuilder.start();
         final TraineeJvm traineeJvm = new TraineeJvm(traineeId, process);
         traineeJvms.add(traineeJvm);
         return traineeJvm;
     }
 
     private void waitForTraineeStartup(TraineeJvm jvm) throws InterruptedException {
         IMap<String, InetSocketAddress> traineeParticipantMap = traineeClient.getMap(Trainee.TRAINEE_PARTICIPANT_MAP);
 
         boolean found = false;
         for (int l = 0; l < 300; l++) {
             if (traineeParticipantMap.containsKey(jvm.getId())) {
                 InetSocketAddress address = traineeParticipantMap.remove(jvm.getId());
                 jvm.setAddress(address);
                 found = true;
                 break;
             } else {
                 Utils.sleepSeconds(1);
             }
         }
 
         if (!found) {
             throw new RuntimeException(format("Trainee %s didn't start up in time", jvm.getId()));
         }
         log.log(Level.INFO, "Trainee: " + jvm.getId() + " Started");
     }
 
 
     public void destroyTrainees() {
         if (traineeClient != null) {
             traineeClient.getLifecycleService().shutdown();
         }
 
         for (TraineeJvm jvm : traineeJvms) {
             jvm.getProcess().destroy();
         }
 
         for (TraineeJvm jvm : traineeJvms) {
             int exitCode = 0;
             try {
                 exitCode = jvm.getProcess().waitFor();
             } catch (InterruptedException e) {
             }
 
             if (exitCode != 0) {
                 log.log(Level.INFO, format("trainee process exited with exit code: %s", exitCode));
             }
         }
         traineeJvms.clear();
     }
 }
