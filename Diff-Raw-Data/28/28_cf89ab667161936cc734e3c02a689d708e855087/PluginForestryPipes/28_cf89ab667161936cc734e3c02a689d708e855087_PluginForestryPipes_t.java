 package denoflionsx.DenPipes.AddOns.Forestry;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardAnyDrone;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardAnyPrincess;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardAnyQueen;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardHumidity;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardBool;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardBool.specials;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardClosed;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardFlowers;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardNonBee;
import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardSpecies;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.CardWorkspeed;
 import denoflionsx.DenPipes.AddOns.Forestry.Cards.ICard;
 import static denoflionsx.denLib.Lib.denLib.debug;
 import denoflionsx.denLib.Mod.Handlers.WorldHandler.IdenWorldEventHandler;
 import denoflionsx.denLib.Mod.Handlers.WorldHandler.WorldEventHandler;
 import denoflionsx.denLib.Mod.denLibMod;
import forestry.api.apiculture.IAlleleBeeSpecies;
 import forestry.api.core.IPlugin;
 import forestry.api.genetics.AlleleManager;
 import forestry.api.genetics.IAllele;
 import java.io.File;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 public class PluginForestryPipes implements IPlugin, IdenWorldEventHandler {
 
     public static final String speciesKey = "species";
     public static final String workspeedKey = "speed";
     public static final String lifespanKey = "lifespan";
     public static final String fertilityKey = "fertility";
     public static final String toleranceKey = "tolerance";
     public static final String toleranceKey2 = "humidity";
     public static final String flowerKey = "flowers";
     public static final String specialKey = "bool";
     public static final String pollinationKey = "flowering";
     public static final String territoryKey = "territory";
     public static final String effectKey = "effect";
     public static final ArrayList<String> manualControlList = new ArrayList();
     public static final BiMap<Integer, String> keyMap = HashBiMap.create();
     public static final HashMap<String, ArrayList<ICard>> cardRegistry = new HashMap();
     public static final HashMap<String, Class> classMap = new HashMap();
     public static ArrayList<Class> extended = new ArrayList();
     public static HashMap<String, IAllele> dummyAlleles = new HashMap();
     public static File source;
 
     static {
         try {
             int index = -1;
             for (Field f : PluginForestryPipes.class.getDeclaredFields()) {
                 if (f.getType().equals(String.class)) {
                     index++;
                     String key = f.get(null).toString();
                     keyMap.put(index, key);
                     cardRegistry.put(key, new ArrayList<ICard>());
                     ForestryPipe.proxy.print("Created card registry " + key);
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
        manualControlList.add(speciesKey);
         manualControlList.add(toleranceKey2);
         manualControlList.add(specialKey);
         manualControlList.add(flowerKey);
         manualControlList.add(workspeedKey);
     }

    public static ArrayList<ICard> getAllCards() {
         ArrayList<ICard> cards = new ArrayList();
        for (ArrayList<ICard> c : cardRegistry.values()) {
             cards.addAll(c);
         }
         return cards;
     }
 
     public static int getCardID(String typeKey, ICard card) {
         return cardRegistry.get(typeKey).indexOf(card);
     }
 
     public static ICard getCard(String typeKey, int index) {
         return cardRegistry.get(typeKey).get(index);
     }
 
     @Override
     public boolean isAvailable() {
         return true;
     }
 
     @Override
     public void preInit() {
     }
 
     @Override
     public void doInit() {
     }
 
     @Override
     public void postInit() {
         WorldEventHandler.registerHandler(this);
     }
 
     @Override
     public void onWorldLoaded() {
         ForestryPipe.proxy.print("Scanning Forestry Allele registry for stuff...");
         for (String s : AlleleManager.alleleRegistry.getRegisteredAlleles().keySet()) {
             //ForestryPipe.proxy.print(s + " : " + AlleleManager.alleleRegistry.getAllele(s).getClass().getName());
             for (String key : keyMap.values()) {
                 if (manualControlList.contains(key)) {
                     continue;
                 }
                 if (s.toLowerCase().contains(key.toLowerCase())) {
                     try {
                         Class c = classMap.get(key);
                         if (c == null) {
                             continue;
                         }
                         ICard card = (ICard) c.getConstructor(new Class[]{IAllele.class}).newInstance(new Object[]{AlleleManager.alleleRegistry.getAllele(s)});
                         cardRegistry.get(key).add(card);
                     } catch (Throwable pokeball) {
                         pokeball.printStackTrace();
                     }
                 }
             }
         }
         // Why are tolerances special fucking snowflakes using the same Alleles twice?
         // This makes my normal system shit itself. Gotta do this one by hand.
         for (String s : AlleleManager.alleleRegistry.getRegisteredAlleles().keySet()) {
             if (s.contains(toleranceKey)) {
                 ICard card = new CardHumidity(AlleleManager.alleleRegistry.getAllele(s));
                 cardRegistry.get(toleranceKey2).add(card);
             }
             // I had to do flower pollination manually too. Binnie does not follow convention, so the default scanner can't find ExtraBees added ones.
            if (s.contains(flowerKey) || (s.contains(".flower") && !s.contains("." + pollinationKey))) {
                 ICard card = new CardFlowers(AlleleManager.alleleRegistry.getAllele(s));
                 cardRegistry.get(flowerKey).add(card);
             }
             // This one has to be done manually because some effects have speed in the name.
            if (s.contains(workspeedKey) && !s.contains("effect")) {
                 ICard card = new CardWorkspeed(AlleleManager.alleleRegistry.getAllele(s));
                 cardRegistry.get(workspeedKey).add(card);
             }
            // Sigh. ExtraTrees does not follow species name convention.
            if (s.contains(speciesKey)) {
                IAllele a = AlleleManager.alleleRegistry.getAllele(s);
                if (a instanceof IAlleleBeeSpecies) {
                    ICard card = new CardSpecies(a);
                    cardRegistry.get(speciesKey).add(card);
                }
            }
         }
         // These are also special snowflakes due to me wanting to save GUI space.
         for (specials s : specials.values()) {
             cardRegistry.get(specialKey).add(new CardBool(s));
         }
         cardRegistry.get(speciesKey).add(new CardAnyPrincess());
         cardRegistry.get(speciesKey).add(new CardAnyDrone());
         cardRegistry.get(speciesKey).add(new CardAnyQueen());
         cardRegistry.get(speciesKey).add(new CardNonBee());
         cardRegistry.get(speciesKey).add(new CardClosed());
         WorldEventHandler.unregisterHandler(this);
     }
 
     public static ArrayList<Class> getClassesThatExtend(File source, Class base) {
         ArrayList<Class> classes = new ArrayList();
         try {
             JarFile jarFile = new JarFile(source);
             Enumeration e = jarFile.entries();
             while (e.hasMoreElements()) {
                 JarEntry je = (JarEntry) e.nextElement();
                 if (je.isDirectory() || !je.getName().endsWith(".class")) {
                     continue;
                 }
                 // -6 because of .class
                 String className = je.getName().substring(0, je.getName().length() - 6);
                 className = className.replace('/', '.');
                 if (debug) {
                     denLibMod.Proxy.print(className);
                 }
                 Class c = null;
                 try {
                     c = Class.forName(className);
                 } catch (Exception ex) {
                     // This is needed for server side. Some client only classes won't exist.
                     continue;
                 } catch (NoSuchFieldError ex) {
                     denLibMod.Proxy.print("Skipping " + className + " due to SideOnly = CLIENT.");
                     ex.printStackTrace();
                     continue;
                 }
                 if (c.isInterface()) {
                     continue;
                 }
                 if (c.getSuperclass().equals(base)) {
                     classes.add(c);
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return classes;
     }
 
     @Override
     public void onWorldEnded() {
     }
 }
