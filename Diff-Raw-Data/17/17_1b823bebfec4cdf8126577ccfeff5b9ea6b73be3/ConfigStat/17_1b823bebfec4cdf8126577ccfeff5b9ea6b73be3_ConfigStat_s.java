 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package configuration;
 
 import cache.Cache;
 import cpu.instruction.InstructionThread;
 import statistics.AddressBlock;
 import statistics.CacheStats;
 import statistics.Stats;
 
 /**
  *
  * @author Nathan
  */
 public class ConfigStat extends Stats{
 
     public ConfigStat(long addressBlockSize) {
         super(addressBlockSize);
     }
 
     @Override
     public void threadAction(InstructionThread thread, Cache cache) {
         long address = thread.getAddressIndex(getAddressBlockSize());
         if(!getAddressBlocks().containsKey(address)) {
             getAddressBlocks().put(address, new AddressBlock(address));
         }
 
         AddressBlock block = getAddressBlocks().get(address);
         block.addInstruction(thread.getInstruction());

         CacheStats cacheStats = cache.getStats();
         cacheStats.addChangeToStat(block.getStats());
         cacheStats.forgetChanges();
     }
     
 }
