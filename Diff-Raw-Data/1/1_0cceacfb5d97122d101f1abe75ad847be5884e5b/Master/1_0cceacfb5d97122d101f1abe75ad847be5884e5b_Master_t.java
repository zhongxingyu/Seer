 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package woodcock;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Z98
  */
 public class Master extends JFrame {
 
     public static int x, y, color;
     public static boolean DEBUG_FLAG = false;
     
     public enum SCENARIOS {
         S1, //Unlimited conservation resource, all cuts directed by conservation needs.
         S2, //Lumber interests cut based on profit margin and harvest ability limitations.
         S3, //Conservation groups first checks whether lumber interest will automatically create habitats without intervention.
         S4, //More intelligent conservation selection model based on distance of patches from each other.
         S5  //Variant of S4, samve objective but different optimization model.
     };
     
     public static SCENARIOS currentScenario = SCENARIOS.S1;
     // Function to draw patches of forest, the color changes according to age, older forest has lighter color
 
     public Master() {
         super("Vilas County Forest Map");
         //JScrollPane jp = new JScrollPane();
 
     }
 
     @Override
     public void paint(Graphics g) {
         for (Patch p : forestPatches) {
             Master.color = p.age;
             Master.x = p.x;
             Master.y = p.y;
 
             color += 50;
             if (color > 255) {
                 color = 255;
             }
             Color c = new Color(0, color, 0);
             Graphics g1 = g;
             g1.setColor(c);
             //g1.drawRect(10 + x, 32 + y, 1, 1);
             validate();
             g1.fillRect(10 + x, 32 + y, 1, 1);
             validate();
             repaint();
         }
     }
     public static RTree waterDepth = null;
     public static RTree timberSuitable = null;
     public static RTree allForests = null;
     public static RTree youngForests = null;
     public static RTree developedArea = null;
     public static ArrayList<Patch> forestPatches = null;
     static int columns;
     static int rows;
     public static final Thread mainThread = Thread.currentThread();
 
     public static void main(String[] args) {
         // TODO code application logic here
         int tick = 0;
         int cIndex = 0;
         int rIndex = 0;
         double leastprofit = 0.0;
         double mostprofit = 0.0;
         int foundWater = 0;
         int countSuitablePatch = 0; // for checking the total # of suitable patches for woodcock
         // check for both water patch in vicinity and suitability of nearby 
         // land for gathering lumber
         Charset charset = Charset.forName("US-ASCII");
         Path waterPath = Paths.get("wtdepthcmascii.txt");
         Path coverPath = Paths.get("cdl2011.txt");
         //Path landPath = Paths.get("landingsuitability.txt");
         HashMap<List<Integer>, Patch> patches = new HashMap<>(); // update every year
 
         forestPatches = new ArrayList<>();
         LinkedList<Patch> grassPatches = new LinkedList<>();
 
         int lowestDepth = Integer.MAX_VALUE;
         //HashMap<Integer, Integer> waterDepthLevels = new HashMap<>();
 
         // rtree for nearby water area and suitability for harvesting lumber
         waterDepth = new RTree(4, 8);
         timberSuitable = new RTree(4, 8);
         allForests = new RTree(4, 8);
         youngForests = new RTree(4, 8);
         developedArea = new RTree(4, 8);
 
         // input box for lumber company, still haven't done
         // none functional go and step buttons
         new InputField().setVisible(true);
         //if (InputField.clicked == true) {
         //    System.out.println("clicked");
         //}
         //int number1 = InputField.num1;
         //int number2 = InputField.num2;
         //while (InputField.num1 == 0|| InputField.num2 == 0) {
         //    System.out.println("0");
         //}
         //System.out.println("num1, num2: " + InputField.num1 + " " + InputField.num2);
 
         Calculation.initializeWeightedRandom();
         Calculation.initializeGrowth();
         System.err.println(System.getProperty("user.dir"));
 
         Patch testPatch = new Patch(0, 0);
         testPatch.landCover = 141;
         /*
          * for(int index = 1; index < 51; index++) { //System.out.println();
          * System.out.print("Year " + index + " Value: ");
          * testPatch.growTrees(); //System.out.print(testPatch.baselArea
          * +",\n"); System.out.println(testPatch.calcValue()); }
          */
         try (BufferedReader reader = Files.newBufferedReader(coverPath, charset)) {
             cIndex = 0;
             rIndex = 0;
             String line = reader.readLine();
             String[] sCol = line.split("\\s+");
             columns = Integer.parseInt(sCol[1]);
             line = reader.readLine();
             String[] sRow = line.split("\\s+");
             rows = Integer.parseInt(sRow[1]);
             reader.readLine();
             reader.readLine();
             reader.readLine();
             reader.readLine();
             while ((line = reader.readLine()) != null) {
                 String cols[] = line.split("\\s+");
                 for (String col : cols) {
                     int landCover = Integer.parseInt(col);
                     if (landCover != -9999) {
                         List<Integer> coord = Arrays.asList(cIndex, rIndex);
                         Patch patch = patches.get(coord);
                         if (patch == null) {
                             patch = new Patch(cIndex, rIndex);
                             patches.put(coord, patch);
                         }
                         patch.landCover = landCover;
 
                         /*
                          * Two different spatial partitioning trees are filled
                          * in, each one storing patches based on slightly
                          * different information.
                          */
 
                         /*
                          * Forested regions to check to make sure grasslands are
                          * within range of suitable nesting areas before being
                          * declared suitable habitats.
                          */
                         if (landCover == 141 || landCover == 142 || landCover == 143) {
                             /*
                              * Unilaterally add any forested patches to list. We
                              * need to iterate through this list later to
                              * determine whether it is already suitable habitat
                              * or is "candidate" for cutting to become suitable.
                              */
                             // insert into forest rtree to enable search for grassland area that is near forest
                             // for habitat purpose
                             AABB box = new AABB(cIndex, rIndex);
                             allForests.insert(box);
                             patch.age = Calculation.rand.nextInt(51);
                             forestPatches.add(patch);
                             if (patch.age < 10) {
                                 youngForests.insert(box);
                             }
                         }
 
 
                         /*
                          * Grassland. If we ever decide to plant trees to create
                          * habitat or require that cut candidates be within some
                          * range of grasslands.
                          */
                         if (landCover == 152 || landCover == 171 || landCover == 195) {
                             /*
                              * Unilaterally add any grassland patches to list.
                              * We need to iterate through this list later to
                              * determine whether it is already suitable habitat.
                              */
                             grassPatches.add(patch);
                         }
 
                         // get the patches that are developed area
                         if (landCover >= 121 && landCover <= 124) {
                             AABB box = new AABB(cIndex, rIndex);
                             developedArea.insert(box);
                         }
                     }
 
                     ++cIndex;
                 }
 
                 cIndex = 0;
                 ++rIndex;
             }
 
             reader.close();
         } catch (IOException ioe) {
             System.err.format("IOException: %s\n", ioe);
             System.exit(-1);
         }
 
         System.gc();
 
         System.out.println("Done reading in cover data");
 
         try (BufferedReader reader = Files.newBufferedReader(waterPath, charset)) {
             /*
              * The water depth data is used to create an R tree to cover areas
              * where the ground is likely to be saturated. This is then searched
              * to determine proximity of suitable foraging ground for woodcock
              * habitats.
              */
             String line = reader.readLine();
             String[] sCol = line.split("\\s+");
             columns = Integer.parseInt(sCol[1]);
             line = reader.readLine();
             String[] sRow = line.split("\\s+");
             rows = Integer.parseInt(sRow[1]);
             reader.readLine();
             reader.readLine();
             reader.readLine();
             reader.readLine();
 
             while ((line = reader.readLine()) != null) {
                 String cols[] = line.split("\\s+");
                 for (String col : cols) {
                     int wDepth = Integer.parseInt(col);
                     if (wDepth != -9999) {
                         List<Integer> coord = Arrays.asList(cIndex, rIndex);
                         Patch patch = patches.get(coord);
                         if (patch == null) {
                             patch = new Patch(cIndex, rIndex);
                             patches.put(coord, patch);
                         }
                         patch.waterDepth = wDepth;
                         if (lowestDepth > wDepth) {
                             lowestDepth = wDepth;
                         }
                         /*
                          * if(waterDepthLevels.get(wDepth) == null)
                          * waterDepthLevels.put(wDepth, wDepth);
                          */
                         if (wDepth <= 80 && patch.landCover != 111) {
                             //Insert into R tree.
                             AABB box = new AABB(cIndex, rIndex);
                             waterDepth.insert(box);
                             foundWater++;
                         }
                     }
                     ++cIndex;
                 }
 
                 ++rIndex;
                 cIndex = 0;
             }
 
             reader.close();
         } catch (IOException ioe) {
             System.err.format("IOException: %s\n", ioe);
             System.exit(-1);
         }
 
         System.gc();
 
         System.out.println("Done reading in water data");
         System.out.println("Found water: " + foundWater);
         /*
          * Iterator it = waterDepthLevels.entrySet().iterator();
          * while(it.hasNext()) { Map.Entry<Integer, Integer> entry =
          * (Map.Entry<Integer, Integer>)it.next();
          * System.out.println(entry.getKey()); }
          */
         /*
          * try (BufferedReader reader = Files.newBufferedReader(landPath,
          * charset)) { /* The landing data indicates suitability of a patch for
          * use landing harvested timber. This helps determine the profitability
          * of harvesting specific patches based on distance from the target
          * patch to the nearest landing patch.
          *
          * String line = reader.readLine(); String[] sCol = line.split("\\s+");
          * columns = Integer.parseInt(sCol[1]); line = reader.readLine();
          * String[] sRow = line.split("\\s+"); rows = Integer.parseInt(sRow[1]);
          * reader.readLine(); reader.readLine(); reader.readLine();
          * reader.readLine();
          *
          * while((line = reader.readLine()) != null) {
          *
          * String cols[] = line.split("\\s+"); for(String col : cols) { int
          * landing = Integer.parseInt(col); if(landing != -9999) { List<Integer>
          * coord = Arrays.asList(cIndex, rIndex); Patch patch =
          * patches.get(coord); if (patch == null) { patch = new Patch(cIndex,
          * rIndex); patches.put(coord, patch); } patch.landing = landing;
          *
          * /*
          * For now, let's only insert landings that are very suitable for
          * landing into the R tree. If we feel like it, we can create another R
          * tree for the moderately suitable and attribute a cost function of
          * some sort for the two.
          *
          * if(landing == 3) { AABB box = new AABB(cIndex, rIndex);
          * timberSuitable.insert(box); } }
          *
          * ++cIndex; }
          *
          * cIndex = 0; ++rIndex; }
          *
          * reader.close(); } catch(IOException ioe) {
          * System.err.format("IOException: %s\n", ioe); System.exit(-1); }
          */
         System.gc();
         System.out.println("Done loading in files.");
 
         final Master sp = new Master();
         sp.setVisible(true);
         sp.setSize(1422, 1000);
         sp.setLocation(-5, -15);
         sp.setResizable(false);
         final ArrayList<Patch> finalForests = forestPatches;
 
         // Comparator for both PQueue
         LumberCompany lumCompany = new LumberCompany(forestPatches.size());
         WCConservation conservGroup = new WCConservation(forestPatches.size());
         int notSuitable = 0;
         for (Patch p : forestPatches) {
             /*
              * Check hydrology info to see if this patch is a suitable candidate
              * for cutting. If not, skip.
              */
 
             // calculate the value earned from the forest wood and store the profit in 
             // p.lumberProfit for comparison against other forest patch in pqueue
             p.calcValue();
             // check if the patch is suitable for lumber
             lumCompany.queueTimberPatch(p);
             // check if the forest patch is near to any lumber gathering area
             // also, check if water patch(or patch with suitable water concentration) 
             // is within the range of 1; unit distance is in acre
             if (p.age >= 10) {
                 if (conservGroup.checkSuitability(p) == false) {
                     ++notSuitable;
                 }
             } else {
                 ++notSuitable;
             }
 
 
 
             /*
              * If a suitable candidate, check age of forest. If already below 10
              * years of age, add to list of usable habitats. If not, add to
              * cutting candidate list. Order in which cutting candidates are
              * added needs to be determined based off of some kind of priority,
              * likely the age of the forest balanced against information like
              * road length to help determine ease of transport.
              */
         }
         if (notSuitable == forestPatches.size()) {
             boolean got = false;
             while (got == false) {
                 got = conservGroup.ForceHabitat(forestPatches);
             }
         }
 
         System.out.println("Acres of non-wetland forest: " + forestPatches.size());
         System.out.println("Acres of grassland: " + grassPatches.size());
 
         System.out.println("Generating forests.\n");
         /*
          * for(Patch p : forestPatches) { p.generateTrees(); p.age++; }
          */
         Parallel.withIndex(0, forestPatches.size() - 1, new Parallel.Each() {
 
             @Override
             public void run(int i) {
                 Patch patch = finalForests.get(i);
                 patch.generateTrees();
                 patch.age++;
             }
         });
 
         System.out.println("Done generating forests.\n");
 
         System.out.print("Initiating scenario ");
         switch(currentScenario)
         {
             case S1:
                 System.out.println("1");
                 break;
             case S2:
                 System.out.println("2");
             case S3:
                 System.out.println("3");
         }
         System.out.println();
         
         while (true) {
             // clicked means go; step means just step through once
             // go can be stopped by clicking go again the next time 
             if(InputField.clicked == false && InputField.step == false)
             {
                 try {
                     /*
                     try {
                         synchronized (mainThread) {
                             mainThread.wait();
                         }
                     } catch (InterruptedException ex) {
                         //Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                         continue;
                     }
                     * 
                     */
                     Thread.sleep(1000);
                 } catch (InterruptedException ex) {
                     Logger.getLogger(Master.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
             while (InputField.clicked == true || InputField.step == true) {
                 // for checking purpose
                 //System.out.println("entered");
                 
                 // changing the step button value to false
                 InputField.step = false;
                 final int time = tick;
                 //conservGroup.candidateTree = new RTree(4, 8);
                 Parallel.withIndex(0, forestPatches.size() - 1, new Parallel.Each() {
 
                     @Override
                     public void run(int i) {
                         Patch p = finalForests.get(i);
                         p.growTrees();
                         p.calcValue();
                         if (DEBUG_FLAG) {
                             System.out.println("Value at time " + time + " at age " + p.age + ": " + p.lumberProfit);
                         }
                         p.age++;
                     }
                 });
                 sp.repaint();
                 youngForests = new RTree(4, 8);
                 conservGroup.candidateMap.clear();
                 conservGroup.habitatCandidates.clear();
                 //conservGroup.candidateTree = new RTree(4, 8);
                 
                 for(Patch p : finalForests)
                 {
                     if(p.age < 15) youngForests.insert(p.box);
                 }
 
                 PriorityQueue<Patch> habitatCandidates;
                 conservGroup.closeToWater = 0;
                 conservGroup.alreadySuitable = 0;
                 while((habitatCandidates = conservGroup.CheckForestSuitability()).size() + conservGroup.alreadySuitable < WCConservation.requiredHabitats)
                 {
                     System.err.println("Error: failed to locate enough patches to cut.");
                     System.err.println("Current size: " + habitatCandidates.size());
                 }
                 
                 System.out.println("Forests \"close\" to water: " + conservGroup.closeToWater);
                 
                 System.out.println("Habitat candidates: " + conservGroup.habitatCandidates.size());
 
                 System.out.println("Model generated, waiting.");
                 PriorityQueue<Patch> conCuts = null;
                 if(conservGroup.alreadySuitable < WCConservation.requiredHabitats)
                 {
                     System.out.println("Need to cut: " + (WCConservation.requiredHabitats - conservGroup.alreadySuitable));
                     conCuts = conservGroup.OptimizeCutsScenario1();
                     if (conCuts == null) {
                         System.err.println("Attempt to optimize failed.");
                         System.exit(-1);
                    }
                 }
                 else
                 {
                     System.out.println("Did not need to cut this year.");
                 }
                 
                 double totalCutValue = 0;
                 
                 switch(currentScenario)
                 {
                     case S1:
                         if(conCuts != null) totalCutValue = lumCompany.ClearCut(conCuts);
                         break;
                     case S2:
                         lumCompany.ClearTimberList();
                         for (Patch fPatch : forestPatches) {
                             lumCompany.queueTimberPatch(fPatch);
                         }
                         double conValue = lumCompany.CalcProfit(conCuts);
                         System.out.println("Conservation cut value: " + conValue);
                         PriorityQueue<Patch> actualCuts = new PriorityQueue<>(lumCompany.harvestLimit, Calculation.pLComparator);
                         lumCompany.ConservationHarvested(conCuts, actualCuts);
                         totalCutValue = lumCompany.ClearCut(actualCuts);
                         System.out.println("Actual patches cut: " + actualCuts.size());
                         break;
                     case S3:
                         break;
                 }
 
                 System.out.println("Total value from harvested patches for timestep " + tick + " : " + totalCutValue);
                 System.out.println();
                 ++tick;
                 System.gc();
             }
         }
     }
 }
