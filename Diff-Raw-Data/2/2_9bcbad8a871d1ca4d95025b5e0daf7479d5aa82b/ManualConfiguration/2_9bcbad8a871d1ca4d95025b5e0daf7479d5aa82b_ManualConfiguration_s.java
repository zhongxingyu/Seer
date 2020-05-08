 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package configuration;
 
 import cache.TwoLayerCache;
 import cpu.instruction.InstructionThread;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 /**
  *
  * @author Nathan
  */
 public class ManualConfiguration extends Optimizer {
 
     private long reconfigCount;
     private long addressBlockSize;
     private TwoLayerCache[] coreCaches;
     
     // adress block id -> priority van blok
     private HashMap<Long, Integer> priorities;
     // priority -> aantal threads op de priority aanwezig
     private int[] priorityCounts;
     // priority -> configuration
     private HashMap<Integer, Configuration> configurations;
     private int currentPriority;
     
     public ManualConfiguration(File configFile, TwoLayerCache[] caches, long addressBlockSize, int core) throws FileNotFoundException, IOException {
         this.addressBlockSize = addressBlockSize;
         this.coreCaches = caches;
         this.reconfigCount = 0;
         
         BufferedReader reader = new BufferedReader(new FileReader(configFile));
         setConfigurations(reader);
         
         coreCaches[core].installConfiguration(configurations.get(currentPriority));
     }
     
     private void setConfigurations(BufferedReader reader) throws IOException {
         this.priorities = new HashMap<>();
         this.configurations = new HashMap<>();
         
         String line = reader.readLine();
         int priority = 0;
         while(line != null) {
             String[] parts = line.split(":");
             long[] longs = new long[parts.length];
             for(int i = 0; i < parts.length; i++) {
                 longs[i] = Long.parseLong(parts[i]);
             }
             
             for(int i = 0; i < longs.length-1; i++) {
                 priorities.put(longs[i], priority);
             }
             configurations.put(priority, new Configuration((int)longs[longs.length-1]));
             
             priority++;
             line = reader.readLine();
         }
         priorityCounts = new int[priority];
         currentPriority = priorityCounts.length-1;
     }
     
     private int getCurrentBestPriority() {
         int i = 0;
        while(priorityCounts[i] == 0 && i < priorityCounts.length) {
             i++;
         }
         if(i == priorityCounts.length) {
             i--;
         }
         return i;
     }
     
     private int getPriority(long address) {
         if(priorities.containsKey(address)) {
             return priorities.get(address);
         } else {
             return priorityCounts.length - 1;
         }
     }
     
     private void setNewPriorityConfig(int newHighestPriority, int core) {
         currentPriority = newHighestPriority;
         Configuration currentBestConfig = configurations.get(currentPriority);
         Configuration currentConfig = coreCaches[core].getConfiguration();
         if(!currentBestConfig.equals(currentConfig)) {
             coreCaches[core].installConfiguration(currentBestConfig);
             reconfigCount++;
         }
     }
     
     @Override
     public void check(InstructionThread thread, int core) {
         long prevAddress = thread.getPrevBlock().getAddress();
         if(thread.getInstruction() != null) {
             long address = thread.getAddressIndex(addressBlockSize);
             if(address != prevAddress) {
                 int newPriority = getPriority(address);
                 priorityCounts[newPriority]++;
                 if(prevAddress != -1) {
                     int prevPriority = getPriority(prevAddress);
                     priorityCounts[prevPriority]--;
                     // prioriteit is mogelijk gezakt
                     if(prevPriority == currentPriority && currentPriority < newPriority) {
                         int newCurrentBestPriority = getCurrentBestPriority();
                         if(newCurrentBestPriority != currentPriority) {
                             setNewPriorityConfig(newCurrentBestPriority, core);
                         }
                     }
                 }
                 // prioriteit is gestegen
                 if(currentPriority > newPriority) {
                     setNewPriorityConfig(newPriority, core);
                 }
             }
         } else {
             if(prevAddress != -1) {
                 int prevPriority = getPriority(prevAddress);
                 priorityCounts[prevPriority]--;
                 if(prevPriority == currentPriority) {
                     int newCurrentBestPriority = getCurrentBestPriority();
                     if(newCurrentBestPriority != currentPriority) {
                         setNewPriorityConfig(newCurrentBestPriority, core);
                     }
                 }
             }
         }
     }
 
     @Override
     public void addThread(InstructionThread thread) {}
 
     @Override
     public long getReconfigCount() {
         return reconfigCount;
     }
     
 }
