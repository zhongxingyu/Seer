 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package woodcock;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.*;
 
 /**
  *
  * @author Z98
  */
 public class Master {
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
         int tick = 0;
         int columns = 0;
         int rows = 0;
         int cIndex = 0;
         int rIndex = 0;
         Charset charset = Charset.forName("US-ASCII");
         Path waterPath = Paths.get("test");
         Path coverPath = Paths.get("test");
         Path landPath = Paths.get("test");
         HashMap<List<Integer>, Patch> patches = new HashMap<>();
         
         LinkedList<Patch> forestPatches = new LinkedList<>();
         LinkedList<Patch> grassPatches = new LinkedList<>();
         
         PriorityQueue<Patch> cutCandidates = new PriorityQueue<>();
         
         Calculation.initializeWeightedRandom();
         Calculation.initializeGrowth();
         
         try(BufferedReader reader = Files.newBufferedReader(waterPath, charset))
         {
             /*
              * The water depth data is used to create an R tree
              * to cover areas where the ground is likely to be
              * saturated. This is then searched to determine
              * proximity of suitable foraging ground for woodcock
              * habitats.
              */
             String line = reader.readLine();;
             String[] sCol = line.split("\\s+");
             columns = Integer.parseInt(sCol[1]);
             line = reader.readLine();
             String[] sRow = line.split("\\s+");
             rows = Integer.parseInt(sRow[1]);
             reader.readLine();
             reader.readLine();
             reader.readLine();
             reader.readLine();
             
             while((line = reader.readLine()) != null)
             {
                 String cols[] = line.split("\\s+");
                 for(String col : cols)
                 {
                     int wDepth = Integer.parseInt(col);
                     if(wDepth != -9999)
                     {
                         Patch patch = new Patch(cIndex, rIndex);
                         patch.waterDepth = wDepth;
                         List<Integer> coord = Arrays.asList(cIndex, rIndex);
                         patches.put(coord, patch);
                         
                         if(wDepth <= 30)
                         {
                             //Insert into R tree.
                         }
                     }
                     ++cIndex;
                 }
                 
                 ++rIndex;
                 cIndex = 0;
             }
             
             reader.close();
         }
         catch(IOException ioe)
         {
             System.err.format("IOException: %s\n", ioe);
             System.exit(-1);
         }
         try(BufferedReader reader = Files.newBufferedReader(coverPath, charset))
         {
             cIndex = 0;
             rIndex = 0;
             String line = reader.readLine();;
             String[] sCol = line.split("\\s+");
             columns = Integer.parseInt(sCol[1]);
             line = reader.readLine();
             String[] sRow = line.split("\\s+");
             rows = Integer.parseInt(sRow[1]);
             reader.readLine();
             reader.readLine();
             reader.readLine();
             reader.readLine();
             while((line = reader.readLine()) != null)
             {
                 String cols[] = line.split("\\s+");
                 for(String col : cols)
                 {
                     int landCover = Integer.parseInt(col);
                     if(landCover != -9999)
                     {
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
                         if(landCover == 141 || landCover == 142 || landCover == 143)
                         {
                             /*
                              * Unilaterally add any forested patches to list. We
                              * need to iterate through this list later to
                              * determine whether it is already suitable habitat
                              * or is "candidate" for cutting to become suitable.
                              */
                             forestPatches.add(patch);
                            double treeSize[] = null;
                             int diameter = 0;
                             switch(landCover)
                             {
                                 case 141:
                                     diameter = Calculation.conDefRand.next();
                                     if(diameter == 10) diameter = Calculation.lessConDefRand.next();
                                     break;
                                 case 142:
                                     diameter = Calculation.decDefRand.next();
                                     if(diameter == 10) diameter = Calculation.lessDecDefRand.next();
                                     break;
                                 case 143:
                                     diameter = Calculation.mixDefRand.next();
                                     if(diameter == 10) diameter = Calculation.lessMixDefRand.next();
                                     break;
                             }
                         }
                         
 
                         /*
                          * Grassland. If we ever decide to plant trees to create
                          * habitat or require that cut candidates be within some
                          * range of grasslands.
                          */
                         if(landCover == 152 || landCover == 171 || landCover ==195)
                         {
                             /*
                              * Unilaterally add any grassland patches to list.
                              * We need to iterate through this list later to
                              * determine whether it is already suitable habitat.
                              */
                             grassPatches.add(patch);
                         }
                     }
                     
                     ++cIndex;
                 }
                 
                 cIndex = 0;
                 ++rIndex;
             }
             
             reader.close();
         }
         catch(IOException ioe)
         {
             System.err.format("IOException: %s\n", ioe);
             System.exit(-1);
         }
         
         try(BufferedReader reader = Files.newBufferedReader(landPath, charset))
         {
             /*
              * The landing data indicates suitability of
              * a patch for use landing harvested timber.
              * This helps determine the profitability of
              * harvesting specific patches based on distance
              * from the target patch to the nearest landing
              * patch.
              */
             String line = reader.readLine();;
             String[] sCol = line.split("\\s+");
             columns = Integer.parseInt(sCol[1]);
             line = reader.readLine();
             String[] sRow = line.split("\\s+");
             rows = Integer.parseInt(sRow[1]);
             reader.readLine();
             reader.readLine();
             reader.readLine();
             reader.readLine();
             
             while((line = reader.readLine()) != null)
             {
                 String cols[] = line.split("\\s+");
                 for(String col : cols)
                 {
                     int landing = Integer.parseInt(col);
                     if(landing != -9999)
                     {
                         List<Integer> coord = Arrays.asList(cIndex, rIndex);
                         Patch patch = patches.get(coord);
                         if (patch == null) {
                             patch = new Patch(cIndex, rIndex);
                             patches.put(coord, patch);
                         }
                         patch.landing = landing;
                         
                         /*
                          * For now, let's only insert landings that are
                          * very suitable for landing into the R tree. If
                          * we feel like it, we can create another R tree
                          * for the moderately suitable and attribute a cost
                          * function of some sort for the two.
                          */
                         if(landing == 3)
                         {
                             
                         }
                     }
                     
                     ++cIndex;
                 }
                 
                 cIndex = 0;
                 ++rIndex;
             }
             
             reader.close();
         }
         catch(IOException ioe)
         {
             System.err.format("IOException: %s\n", ioe);
             System.exit(-1);
         }
         
         for(Patch p : forestPatches)
         {
             int x = p.x;
             int y = p.y;
             /*
              * Check hydrology info to see if this patch is a suitable
              * candidate for cutting.  If not, skip.
              */
             
             /*
              * If a suitable candidate, check age of forest.  If already
              * below 10 years of age, add to list of usable habitats.  If
              * not, add to cutting candidate list.  Order in which cutting
              * candidates are added needs to be determined based off of some
              * kind of priority, likely the age of the forest balanced against
              * information like road length to help determine ease of transport.
              */
         }
     }
 }
