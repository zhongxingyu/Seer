 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package configuration;
 
 import cache.Cache;
 import cache.TwoLayerCache;
 import cpu.instruction.InstructionThread;
 import java.util.LinkedList;
 import statistics.AddressBlock;
 import statistics.Stats;
 
 /**
  *
  * @author Nathan
  */
 public class CacheOptimizer extends Optimizer {
     
     private long reconfigCount;
     
     private TwoLayerCache[] coreCaches;
 
     private int currentConfig;
     
     // [i][j]: i is core index en j is configuratie index
     private TwoLayerCache[][] simCaches;
     
     private Stats[] configStats;
     private long addressBlockSize;
     
     private LinkedList<InstructionThread> threads;
     
     public CacheOptimizer(TwoLayerCache[] caches, int currentConfig, TwoLayerCache[][] simCaches, long addressBlockSize) {
         this.coreCaches = caches;
         this.simCaches = simCaches;
         this.addressBlockSize = addressBlockSize;
         configStats = new Stats[simCaches[0].length];
         for(int i = 0; i < configStats.length; i++) {
             // de config stat verandert de thread die hij krijgt niet, gevolg is wel geen next block info over de verschillende configs
             // maar dit is toch enkel van belang bij het hoofd programma en dus het main stats object
             configStats[i] = new ConfigStat(addressBlockSize);
         }
         threads = new LinkedList<>();
         this.currentConfig = currentConfig;
         reconfigCount = 0;
     }
 
     @Override
     public long getReconfigCount() {
         return reconfigCount;
     }
     
     private AddressBlock getCurrentAddressBlock(InstructionThread thread, int configId) {
         return configStats[configId].getAddressBlocks().get(thread.getAddressIndex(addressBlockSize));
     }
 
     private int getBestConfig(InstructionThread thread) {
         int bestIndex = currentConfig;
         long bestGain = 0;
         
         AddressBlock currentConfigBlock = getCurrentAddressBlock(thread, currentConfig);
         for(int i = 0; i < configStats.length; i++) {
             long gain = 0;
             for(InstructionThread t: threads) {
                // nieuwe instructie thread, heeft nog geen instructies, moet dus ook niet meegeteld worden
                if(t.getInstruction() != null) {
                    AddressBlock proposedConfigBlock = getCurrentAddressBlock(t, i);
                    // nieuwe potentiele colds
                    gain -= proposedConfigBlock.getMemoryCount();
 
                    gain += (currentConfigBlock.getStats().getConflictMiss()/currentConfigBlock.getJumpCount())
                        - (proposedConfigBlock.getStats().getConflictMiss()/proposedConfigBlock.getJumpCount());
                }
             }
 
             if(gain > bestGain) {
                 bestGain = gain;
                 bestIndex = i;
             }
         }
         
         return bestIndex;
     }
     
     /**
      * word opgeroepen voor de thread iets heeft gedaan met zijn instructie maar nadat de nieuwe instructie is klaargezet
      * @param thread
      * @param core 
      */
     @Override
     public void check(InstructionThread thread, int core) {
         if(thread.getInstruction() != null) {
             AddressBlock currentConfigBlock = getCurrentAddressBlock(thread, currentConfig);
             // enkel als er al blocks bestaan en we zijn geswitcht van blok, anders nutteloze controle
             if(currentConfigBlock != null && currentConfigBlock.getJumpCount() != 0 && currentConfigBlock.getAddress() != thread.getPrevBlock().getAddress()) {
                 int config = getBestConfig(thread);
                 Cache cache = simCaches[core][config];
                 if(currentConfig != config) {
                     coreCaches[core].installConfiguration(cache.getConfiguration());
 
                     currentConfig = config;
 
                     reconfigCount++;
                 }
             }
 
             // voer actie hier ook door
             for(int i = 0; i < simCaches[core].length; i++) {
                 thread.getInstruction().getExecutionTime(simCaches[core][i]);
                 configStats[i].threadAction(thread, simCaches[core][i]);
             }
         } else {
             threads.remove(thread);
         }
     }
 
     @Override
     public void addThread(InstructionThread thread) {
         threads.add(thread);
     }
 
 }
