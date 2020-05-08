 package fr.aumgn.motd;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Random;
 
 public class MotdsManager {
 
    private Random rand;
     private List<MotdsProvider> providers;
     private List<ConditionalMotdsProvider> conditionalProviders;
     private int totalSize;
 
     public MotdsManager() {
        this.rand = new Random();
         this.providers = new ArrayList<MotdsProvider>();
         this.conditionalProviders = new ArrayList<ConditionalMotdsProvider>();
     }
 
     public void load(List<?> list) {
 
     }
 
     public void update() {
         totalSize = 0;
         for (MotdsProvider provider : providers) {
             totalSize += provider.size();
         }
     }
 
     public int size() {
         return totalSize;
     }
 
     public String get() {
         // Handles conditional providers.
         for (ConditionalMotdsProvider provider : conditionalProviders) {
             if (provider.isActive()) {
                 return provider.get();
             }
         }
 
         // Handles regular providers.
        int i = rand.nextInt(size());
         for (MotdsProvider provider : providers) {
             int size = provider.size();
             if (i < size) {
                 return provider.get(i);
             } else {
                 i -= size;
             }
         }
 
         // Should never be reached. 
         return null;
     }
 
 }
