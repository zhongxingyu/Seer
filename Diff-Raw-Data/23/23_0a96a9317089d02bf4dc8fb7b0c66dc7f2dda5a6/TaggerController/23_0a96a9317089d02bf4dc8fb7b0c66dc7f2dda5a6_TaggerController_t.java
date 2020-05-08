 package edu.mit.wi.haploview.tagger;
 
 import edu.mit.wi.tagger.*;
 import edu.mit.wi.haploview.*;
 import edu.mit.wi.haploview.SNP;
 
 import java.util.Vector;
 import java.util.Hashtable;
 import java.io.File;
 import java.io.IOException;
 
 
 public class TaggerController {
     private Tagger tagger;
     private Vector results;
    private int numToCapture;
     private boolean taggingCompleted;
     private Hashtable snpHash;
 
     public TaggerController(HaploData hd, Vector included, Vector excluded,
                             Vector sitesToCapture, Hashtable designScores, int aggressionLevel, int maxNumTags,boolean findTags)
             throws TaggerException{
         Vector taggerSNPs = new Vector();
 
         snpHash = new Hashtable();
 
        numToCapture = sitesToCapture.size();

         for(int i=0;i<sitesToCapture.size();i++) {
             SNP tempSNP = (SNP) sitesToCapture.get(i);
             edu.mit.wi.tagger.SNP s = new edu.mit.wi.tagger.SNP(tempSNP.getDisplayName(),tempSNP.getPosition(),tempSNP.getMAF());
             taggerSNPs.add(s);
             snpHash.put(tempSNP.getDisplayName(),s);
         }
 
         Vector includedSNPs = new Vector();
         for(int i=0;i<included.size();i++) {
             includedSNPs.add(snpHash.get(included.get(i)));
         }
 
         Vector excludedSNPs = new Vector();
         for(int i=0;i<excluded.size();i++) {
             excludedSNPs.add(snpHash.get(excluded.get(i)));
         }
 
         Hashtable indicesByVarSeq = new Hashtable();
         for(int i=0;i<Chromosome.getSize();i++) {
             if(sitesToCapture.contains(Chromosome.getMarker(i))) {
                 indicesByVarSeq.put(snpHash.get(Chromosome.getMarker(i).getDisplayName()),new Integer(i));
             }
         }
 
         for (int i = 0; i < sitesToCapture.size(); i++){
             SNP tempSNP = (SNP) sitesToCapture.get(i);
             edu.mit.wi.tagger.SNP taggerSNP = (edu.mit.wi.tagger.SNP) snpHash.get(tempSNP.getDisplayName());
             int p = ((Integer)indicesByVarSeq.get(taggerSNP)).intValue();
             for (int j = 1; j < hd.dpTable.getLength(p); j++){
                 PairwiseLinkage pl = hd.dpTable.getLDStats(p,j+p);
                 if (pl != null && pl.getLOD() >= Options.getTaggerLODCutoff()){
                     if (indicesByVarSeq.containsValue(new Integer(j+p))){
                         edu.mit.wi.tagger.SNP ldsnp =
                                 (edu.mit.wi.tagger.SNP) snpHash.get(Chromosome.getMarker(j+p).getDisplayName());
                         taggerSNP.addToLDList(ldsnp);
                         ldsnp.addToLDList(taggerSNP);
                     }
                 }
             }
         }
 
         HaploviewAlleleCorrelator hac = new HaploviewAlleleCorrelator(indicesByVarSeq, hd);
         tagger = new Tagger(taggerSNPs,includedSNPs,excludedSNPs, designScores, hac, Options.getTaggerRsqCutoff(),
                 aggressionLevel, Options.getMaxDistance(), maxNumTags,findTags,Options.isPrintAllTags());
     }
 
     public void runTagger() {
         TagThread tagThread = new TagThread(tagger);
         taggingCompleted = false;
         tagThread.start();
     }
 
     public int getTaggedSoFar() {
         return tagger.taggedSoFar;
     }
 
     public int getUntaggableCount() {
         return tagger.getUntaggableCount();
     }
 
     public Vector getForceIncludeds(){
         return tagger.getForceInclude();
     }
 
     public Vector getMarkerTagDetails(int i){
         //returns a vector with the details of how this marker was tagged:
         //name, tag_name, r^2 with its tag
         Vector res = new Vector();
         String name = Chromosome.getMarker(i).getDisplayName();
         res.add(name);
         if (snpHash.containsKey(name)){
             edu.mit.wi.tagger.SNP ts = (edu.mit.wi.tagger.SNP)snpHash.get(name);
             TagSequence bestTag = ts.getBestTag();
             if(bestTag != null) {
                 res.add(bestTag.getName());
                 res.add(String.valueOf(tagger.getPairwiseCompRsq(ts,bestTag.getSequence())));
             } else {
                 res.add("Untaggable");
                 res.add(new String());
             }
         }else{
             res.add(new String());
             res.add(new String());
         }
 
         return res;
     }
 
     private void taggingFinished() {
         taggingCompleted = true;
     }
 
     public boolean isTaggingCompleted() {
         return taggingCompleted;
     }
 
     public Vector getResults() {
         return results;
     }
 
     public int getNumTagSNPs(){
         return tagger.getTagSNPs().size();
     }
 
    public int getNumToCapture(){
        return numToCapture;
    }

     public void saveResultsToFile(File outFile) throws IOException {
         if(taggingCompleted) {
             tagger.saveResultToFile(outFile);
         }
     }
 
     public void dumpTests(File outFile) throws IOException {
         tagger.dumpTests(outFile);
     }
 
     public void dumpTags(File outFile) throws IOException {
         tagger.dumpTags(outFile);
     }
 
     private class TagThread extends Thread{
         Tagger tagger;
         public TagThread(Tagger t) {
             tagger =t;
         }
         public void run() {
             results = tagger.findTags();
             taggingFinished();
         }
     }
 
     public double getMeanRSq(){
         return tagger.getMeanRSq();
     }
 
     public int getPercentCaptured(){
         return tagger.getPercentCaptured();
     }
 }
