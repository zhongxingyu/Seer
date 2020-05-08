 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package woodcock;
 
 import java.io.*;
 import java.util.*;
 import org.neos.gams.*;
 
 /**
  *
  * @author Z98
  */
 public class WCConservation {
     /*
      * This class handles searching for candidate
      * habitats where woodcock can live and finding
      * candidates for cutting to create these habitats.
      */
     
     boolean hasGams = false;
     private PriorityQueue<Patch> habitatCandidates;
     public HashMap<List<Integer>, Patch> candidateMap;
     int requiredHabitats;
 
     public WCConservation(int forestPatchSize) {
         Comparator<Patch> comparator = new PatchLumberComparator();
         habitatCandidates = new PriorityQueue<>(forestPatchSize, comparator);
         candidateMap = new HashMap<>();
 
         String pathEnv = System.getenv("PATH");
         hasGams = pathEnv.contains("GAMS");
         if (hasGams == false) {
             hasGams = pathEnv.contains("gams");
         }
     }
     
     // get the suitable patch for woodcock habitat
     // still have to check against water patch and forest area
     public void queueDevelopedPatch(RTree developedArea, RTree waterArea, int x, int y, Patch p) {
         int rangeDevelop = 1000;
         while (rangeDevelop > 100) {
             if (LumberCompany.rangeQuery(developedArea, x, y, rangeDevelop) == null) {
                 if (LumberCompany.rangeQuery(waterArea, x, y, 1) != null) {
                     p.developDistance = rangeDevelop / 100;
                     habitatCandidates.add(p);
                     List<Integer> key = Arrays.asList(p.x, p.y);
                     candidateMap.put(key, p);
                 }
             }
             rangeDevelop -= 100;
         }
     }
 
     public boolean isCandidate(Patch p) {
         List<Integer> key = Arrays.asList(p.x, p.y);
         Patch patch = candidateMap.get(key);
         if (patch == null) {
             return false;
         }
 
         return true;
     }
 
     public PriorityQueue<Patch> getPQueue() {
         return habitatCandidates;
     }
 
     public LinkedList<Patch> optimizeCuts() {
         LinkedList<Patch> selectedPatches = new LinkedList<>();
         String results = new String();
         HashMap<Integer, Integer> xValues = new HashMap<>();
         HashMap<Integer, Integer> yValues = new HashMap<>();
         PriorityQueue<Integer> xOrdered = new PriorityQueue<>();
         PriorityQueue<Integer> yOrdered = new PriorityQueue<>();
         ArrayList<PriorityQueue<Integer> > orderedSet = new ArrayList<>();
         orderedSet.add(xOrdered);
         orderedSet.add(yOrdered);
         ArrayList<org.neos.gams.Set> gSets = new ArrayList<>();
         final ArrayList<PriorityQueue<Integer> > fOrderedSet = orderedSet;
 
         try {
             if (hasGams) {
                 org.neos.gams.Set xSet = new org.neos.gams.Set("x", "X coordinates");
                 org.neos.gams.Set ySet = new org.neos.gams.Set("y", "X coordinates");
                 gSets.add(xSet);
                 gSets.add(ySet);
                 Parameter value = new Parameter("patchValue(x,y)", "Patch values");
                 Parameter isCandidate = new Parameter("isCandidate(x,y)", "Is a candidate");
                 Scalar minVal =
                         new Scalar(
                         "minVal",
                         "Minimally acceptable value",
                         String.valueOf(LumberCompany.MIN_VALUE));
                 Scalar required =
                         new Scalar(
                         "requiredPatches",
                         "Required number of patches to cut",
                         String.valueOf(requiredHabitats));
                 
                 for(Patch candidate : habitatCandidates)
                 {
                     value.add(candidate.x + "." + candidate.y, String.valueOf(candidate.calcValue()));
                     isCandidate.add(candidate.x + "." + candidate.y, "1");
                     if(xValues.get(candidate.x) == null)
                     {
                         xValues.put(candidate.x, candidate.x);
                         xOrdered.add(candidate.x);
                     }
                    if(yValues.get(candidate.y) == null)
                     {
                        yValues.put(candidate.y, candidate.y);
                        yOrdered.add(candidate.y);
                     }
                 }
                 
                 final ArrayList<org.neos.gams.Set> fSets = gSets;
                 
                /*
                 * Add set members in parallel.
                 */
                 Parallel.withIndex(0, 1, new Parallel.Each() {
 
                     @Override
                     public void run(int i) {
                         PriorityQueue<Integer> xO = fOrderedSet.get(i);
                         for (Integer itgr : xO) {
                             fSets.get(i).addValue(String.valueOf(itgr));
                         }
                     }
                 });
                 
                 StringBuilder modelContent = new StringBuilder();
                 try (Scanner scanner = new Scanner(new FileInputStream(Calculation.inputTemplatePath),
                                        "ANSI")) {
                     while (scanner.hasNextLine()) {
                         modelContent.append(scanner.nextLine()).append("\n");
                     }
                 }
                 
                 FileWriter modelFile =
                         new FileWriter(Calculation.outputModelPath);
                 modelFile.write(xSet.toString() + "\n");
                 modelFile.write(ySet.toString() + "\n");
                 modelFile.write(value.toString() + "\n");
                 modelFile.write(isCandidate.toString() + "\n");
                 modelFile.write(minVal.toString() + "\n");
                 modelFile.write(required.toString());
                 modelFile.write("\n");
                 modelFile.write(modelContent.toString());
                 
                 ProcessBuilder pBuilder = new ProcessBuilder("gams");
                 pBuilder.redirectOutput();
                 Process gamsProcess = pBuilder.start();
                 BufferedReader gamsReader =
                         new BufferedReader(new InputStreamReader(gamsProcess.getInputStream()));
                 
                 String line;
                 while((line = gamsReader.readLine()) != null)
                 {
                     results.concat(line + "\n");
                     if (gamsProcess.exitValue() == 0) {
                         
                     }
                 }
             }
         } catch(IOException ex) {
             System.err.println("Error: running GAMS locally failed: " + ex.getMessage());
         }
         
         SolutionParser parser = new SolutionParser(results);
         SolutionData bCut = new SolutionData();
         parser.getSymbol("cut", SolutionData.VAR, habitatCandidates.size());
         
         for(SolutionRow sRow : bCut.getRows())
         {
             int level = sRow.getLevel().intValue();
             if(level == 1)
             {
                 int xCoord = Integer.valueOf(sRow.getIndex(0));
                 int yCoord = Integer.valueOf(sRow.getIndex(1));
                 List<Integer> key = Arrays.asList(xCoord, yCoord);
                 Patch cutPatch = candidateMap.get(key);
                 selectedPatches.add(cutPatch);
             }
         }
         
         return selectedPatches;
     }
 }
