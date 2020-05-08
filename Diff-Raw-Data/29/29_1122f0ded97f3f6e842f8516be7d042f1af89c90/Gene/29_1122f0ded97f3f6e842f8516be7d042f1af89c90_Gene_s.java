 package edu.ucdenver.ccp.PhenoGen.data.Bio;
 
 import edu.ucdenver.ccp.PhenoGen.data.Bio.Exon;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.Intron;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.TranscriptElement;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.Transcript;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.TranscriptCluster;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.ProbeSet;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.EQTL;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.EQTLCount;
 import edu.ucdenver.ccp.PhenoGen.data.Bio.SequenceVariant;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * Class for handling data related to Downloads
  *  @author  Cheryl Hornbaker
  */
 
 public class Gene {
     String geneID="",bioType="",chromosome="",strand="",geneSymbol="",source="",description="",ensemblAnnot="";
     long start=0,end=0,length=0,min=-1,max=-1;
     int probesetCountTotal=0,probesetCountEns=0,probesetCountRNA=0,heritCount=0,dabgCount=0;
     double exonCoverageEns=0,exonCoverageRna=0;
     HashMap fullProbeList=new HashMap();
     HashMap hcounts=new HashMap();
     HashMap dcounts=new HashMap();
     HashMap havg=new HashMap();
     HashMap hmin=new HashMap();
     HashMap hmax=new HashMap();
     HashMap davg=new HashMap();
     HashMap dmin=new HashMap();
     HashMap dmax=new HashMap();
     HashMap qtls=new HashMap();
     HashMap qtlCounts=new HashMap();
     HashMap totalCounts=new HashMap();
     HashMap snps=new HashMap();
     TranscriptCluster tc=null;
     
     
     
     ArrayList<Transcript> transcripts=new ArrayList<Transcript>();
     
     public Gene(String geneID,long start,long end){
         this(geneID,start,end,"","","","","","");
     }
     public Gene(String geneID,long start, long end,String chromosome,String strand,String biotype,String symbol,String source,String description){
         this.geneID=geneID;
         this.start=start;
         this.end=end;
         this.geneSymbol=symbol;
         this.chromosome=chromosome;
         if(strand.equals("1")||strand.equals("+")||strand.equals("+1")){
             this.strand="+";
         }else if(strand.equals("-1")||strand.equals("-")){
             this.strand="-";
         }else{
             this.strand=".";
             //System.err.println("Unknown Strand Type:"+strand);
         }
         this.bioType=biotype;
         if(start>end){
             this.length=start-end;
         }else{
             this.length=end-start;
         }
         this.source=source;
         this.description=description;
         
     }
 
     public String getBioType() {
         return bioType;
     }
 
     public void setBioType(String bioType) {
         this.bioType = bioType;
     }
 
     public String getGeneSymbol() {
         return geneSymbol;
     }
 
     public void setGeneSymbol(String geneSymbol) {
         this.geneSymbol = geneSymbol;
     }
 
     public String getChromosome() {
         return chromosome;
     }
 
     public void setChromosome(String chromosome) {
         this.chromosome = chromosome;
     }
 
     public long getEnd() {
         return end;
     }
 
     public void setEnd(long end) {
         this.end = end;
     }
 
     public String getGeneID() {
         return geneID;
     }
 
     public void setGeneID(String geneID) {
         this.geneID = geneID;
     }
 
     public long getLength() {
         return length;
     }
 
     public void setLength(long length) {
         this.length = length;
     }
 
     public long getStart() {
         return start;
     }
 
     public void setStart(long start) {
         this.start = start;
     }
 
     public String getStrand() {
         return strand;
     }
 
     public void setStrand(String strand) {
         this.strand = strand;
     }
 
     public String getSource() {
         return source;
     }
 
     public void setSource(String source) {
         this.source = source;
     }
 
     public String getDescription() {
         return description;
     }
     
     public String getShortDescription() {
         String shortDesc=description;
         if(description.indexOf("[")>0){
             shortDesc=description.substring(0,description.indexOf("["));
         }
         return shortDesc;
     }
     
     public void setDescription(String description) {
         this.description = description;
     }
     
     public String getEnsemblAnnotation(){
         return this.ensemblAnnot;
     }
     
     
 
     public ArrayList<Transcript> getTranscripts() {
         return transcripts;
     }
 
     public void setTranscripts(ArrayList<Transcript> transcripts) {
         this.transcripts = transcripts;
         if(transcripts!=null){
             for(int i=0;i<transcripts.size();i++){
                 ArrayList<Annotation> annot=transcripts.get(i).getAnnotationBySource("AKA");
                 if(annot!=null && annot.size()>=1){
                 String geneID=annot.get(0).getEnsemblGeneID();
                     if(this.ensemblAnnot.equals("")){
                         ensemblAnnot=geneID;
                     }else if(ensemblAnnot.equals(geneID)){
 
                     }else{
                         System.err.println("ERROR: Gene is assigned multiple ensembl genes:"+this.geneID+":"+ensemblAnnot+":"+geneID);               
                     }
                 }
             }
             this.setupSnps(transcripts);
         }
     }
     
     public void addTranscripts(ArrayList<Transcript> toAdd) {
         for(int i=0;i<toAdd.size();i++){
             transcripts.add(toAdd.get(i));
             ArrayList<Annotation> annot=toAdd.get(i).getAnnotationBySource("AKA");
             String geneID=annot.get(0).getEnsemblGeneID();
             if(this.ensemblAnnot.equals("")){
                 ensemblAnnot=geneID;
             }else if(ensemblAnnot.equals(geneID)){
                 
             }else{
                 System.err.println("ERROR: Gene is assigned multiple ensembl genes:"+this.geneID+":"+ensemblAnnot+":"+geneID);               
             }
         }
         this.setupSnps(toAdd);
     }
     
     public void addTranscript(Transcript toAdd) {
             transcripts.add(toAdd);
             ArrayList<Transcript> tmp=new ArrayList<Transcript>();
             tmp.add(toAdd);
             this.setupSnps(tmp);
             ArrayList<Annotation> annot=toAdd.getAnnotationBySource("AKA");
             if(annot!=null && annot.size()>0){
                 String geneID=annot.get(0).getEnsemblGeneID();
                 if(this.ensemblAnnot.equals("")){
                     ensemblAnnot=geneID;
                 }else if(ensemblAnnot.equals(geneID)){
 
                 }else{
                     System.err.println("ERROR: Gene is assigned multiple ensembl genes:"+this.geneID+":"+ensemblAnnot+":"+geneID);               
                 }
             }
     }
     
     public int getTranscriptCountEns(){
         int count=0;
         for(int i=0;i<transcripts.size();i++){
             if(transcripts.get(i).getID().startsWith("ENS")){
                 count++;
             }
         }
         return count;
     }
     
     public int getTranscriptCountRna(){
         int count=0;
         for(int i=0;i<transcripts.size();i++){
             if(!transcripts.get(i).getID().startsWith("ENS")){
                 count++;
             }
         }
         return count;
     }
     
     public ArrayList<Transcript> getSMNCTranscripts(){
         ArrayList<Transcript> ret=new ArrayList<Transcript>();
         for(int i=0;i<transcripts.size();i++){
             if(transcripts.get(i).getID().startsWith("smRNA")){
                 ret.add(transcripts.get(i));
             }
         }
         return ret;
     }
     
     public int getProbeCount(){
         return fullProbeList.size();
     }
     
     @Override
     public String toString(){
         return this.geneID+" "+this.geneSymbol+" "+this.bioType+" "+this.chromosome+" "+this.length+"bp";
     }
     
     public boolean isSingleExon(){
         boolean ret=true;
         if(this.transcripts.size()>1){
             ret=false;
         }else{
             if(transcripts.size()==1){
                 int exonSize=transcripts.get(0).getExons().size();
                 if(exonSize>1){
                     ret=false;
                 }
             }else{
                 ret=false;
             }
         }
         
         return ret;
     }
     
     public void setHeritDabg(HashMap phm){
         probesetCountTotal=0;
         probesetCountEns=0;
         probesetCountRNA=0;
         
         exonCoverageEns=0;
         exonCoverageRna=0;
         fullProbeList=new HashMap();
         for(int i=0;i<transcripts.size();i++){
             transcripts.get(i).setHeritDabg(phm,fullProbeList);
         }
         Set tmpSet=fullProbeList.keySet();
         Object[] psList=tmpSet.toArray();
         hcounts=new HashMap();
         havg=new HashMap<String,Double>();
         hmin=new HashMap<String,Double>();
         hmax=new HashMap<String,Double>();
         dcounts=new HashMap();
         davg=new HashMap<String,Double>();
         dmin=new HashMap<String,Double>();
         dmax=new HashMap<String,Double>();
         if(psList!=null&&psList.length>0){
             HashMap tisHM=(HashMap) phm.get(psList[0].toString());
             int count=1;
             while(tisHM==null&&count<psList.length){
                 tisHM=(HashMap) phm.get(psList[count].toString());
                 count++;
             }
             if(tisHM!=null){
                 Set tisS=tisHM.keySet();
                 Object[] tisAr=tisS.toArray();
                 String[] tissue=new String[tisAr.length];
                 for(int i=0;i<tisAr.length;i++){
                     tissue[i]=tisAr[i].toString();
                     hcounts.put(tissue[i],0);
                     dcounts.put(tissue[i],0);
                     havg.put(tissue[i],0);
                     davg.put(tissue[i],0);
                     hmin.put(tissue[i],1);
                     hmax.put(tissue[i],0);
                     dmin.put(tissue[i],100);
                     dmax.put(tissue[i],0);
                 }
                 for(int i=0;i<psList.length;i++){
                     HashMap tmpHM=(HashMap) phm.get(psList[i].toString());
                     if(tmpHM!=null){
                         for(int j=0;j<tissue.length;j++){
                             HashMap values=(HashMap) tmpHM.get(tissue[j]);
                             double herit=Double.parseDouble(values.get("herit").toString());
                             double dabg=Double.parseDouble(values.get("dabg").toString());
                             
                             if(herit>0.33){
                                 int tmpCount=Integer.parseInt(hcounts.get(tissue[j]).toString());
                                 double tmpSum=Double.parseDouble(havg.get(tissue[j]).toString());
                                 tmpSum=tmpSum+herit;
                                 tmpCount++;
                                 hcounts.put(tissue[j], tmpCount);
                                 havg.put(tissue[j], tmpSum);
                                 if(herit<(new Double(hmin.get(tissue[j]).toString())).doubleValue()){
                                     hmin.put(tissue[j], herit);
                                 }
                                 if(herit>(new Double(hmax.get(tissue[j]).toString())).doubleValue()){
                                     hmax.put(tissue[j], herit);
                                 }
                             }
                             if(dabg>1.0){
                                 int tmpCount=Integer.parseInt(dcounts.get(tissue[j]).toString());
                                 double tmpSum=Double.parseDouble(davg.get(tissue[j]).toString());
                                 tmpSum=tmpSum+dabg;
                                 tmpCount++;
                                 dcounts.put(tissue[j], tmpCount);
                                 davg.put(tissue[j], tmpSum);
                                 if(dabg<(new Double(dmin.get(tissue[j]).toString())).doubleValue()){
                                     dmin.put(tissue[j], dabg);
                                 }
                                 if(dabg>(new Double(dmax.get(tissue[j]).toString())).doubleValue()){
                                     dmax.put(tissue[j], dabg);
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
     
     
     public HashMap getHeritCounts(){
         return hcounts;
     }
     
     public HashMap getDabgCounts(){
         return dcounts;
     }
     public HashMap getHeritAvg(){
         return havg;
     }
     
     public HashMap getHeritMin(){
         return hmin;
     }
     
     public HashMap getHeritMax(){
         return hmax;
     }
     
     public HashMap getDabgAvg(){
         return davg;
     }
     public HashMap getDabgMin(){
         return dmin;
     }
     public HashMap getDabgMax(){
         return dmax;
     }
     
     public void addEQTLs(ArrayList<EQTL> eqtls,HashMap eqtlInd,Logger log){
         //log.debug("fullprobelist.size():"+fullProbeList.size());
         if(fullProbeList.size()>0){
             qtls=new HashMap();
             //fill qtls with (tissue,ArrayList<EQTL>)
             Set tmpSets=fullProbeList.keySet();
             Object[] psList=tmpSets.toArray();
             for(int i=0;i<psList.length;i++){
                 if(eqtlInd.containsKey(psList[i].toString())){
                     //log.debug("probe found:"+psList[i].toString());
                     int index=Integer.parseInt(eqtlInd.get(psList[i].toString()).toString());
                     EQTL tmp=eqtls.get(index);
                     String tmpTissue=tmp.getTissue();
                     if(qtls.containsKey(tmpTissue)){
                         //log.debug("Already contained:"+tmpTissue);
                         ArrayList<EQTL> tmpList=(ArrayList<EQTL>)qtls.get(tmpTissue);
                         tmpList.add(tmp);
                     }else{
                         //log.debug("did not contain:"+tmpTissue);
                         ArrayList<EQTL> tmpList=new ArrayList<EQTL>();
                         tmpList.add(tmp);
                         qtls.put(tmpTissue, tmpList);
                     }
                 }else{
                     //log.debug("probe not found:"+psList[i].toString());
                 }
             }
 
             //System.out.println("HEART EQTL SIZE:"+((ArrayList<EQTL>)qtls.get("Heart")).size());
 
             //fill qtlCount with (tissue, ArrayList<EQTLCount>)
             this.qtlCounts=new HashMap();
             Set tmpSet=qtls.keySet();
             if(tmpSet.size()>0){
                 Object[] tisAr=tmpSet.toArray();
                 String[] tissue=new String[tisAr.length];
                 for(int i=0;i<tisAr.length;i++){
                     tissue[i]=tisAr[i].toString();
                 }
                 for(int i=0;i<tissue.length;i++){
                     ArrayList<EQTLCount> ec;
                     //if(qtlCounts.containsKey(tissue[i])){
                     //    ec=(ArrayList<EQTLCount>) qtlCounts.get(tissue[i]);
                     //}else{
                         ec=new ArrayList<EQTLCount>();
                         //qtlCounts.put(tissue[i], ec);
                     //}
                     ArrayList<EQTL> qtlToCount=(ArrayList<EQTL>) qtls.get(tissue[i]);
                     for(int k=0;k<qtlToCount.size();k++){
                         boolean found=false;
                         EQTL tmpEQTL=qtlToCount.get(k);
                         String tmpEQTLLoc="chr"+tmpEQTL.getMarkerChr()+":"+tmpEQTL.getMarkerLocationMB()+"MB";
                         //System.err.println("Looking for:"+tmpEQTLLoc+":");
                         for(int j=0;j<ec.size()&&!found;j++){
                             EQTLCount tmpCount=ec.get(j);
                             //System.err.println("Checking:"+tmpCount.getLocation());
                             if(tmpCount.getLocation().equals(tmpEQTLLoc)){
                                 tmpCount.add(tmpEQTL);
                                 found=true;
                                 //System.err.println("After adding:"+tmpCount.getProbeCount());
                             }
                         }
                         if(!found){
                             //System.err.println("not found:"+tmpEQTLLoc+":");
                             EQTLCount newCount=new EQTLCount();
                             newCount.setLocation(tmpEQTLLoc);
                             newCount.add(tmpEQTL);
                             ec.add(newCount);
                         }
                     }
                     Collections.sort(ec);
                     //System.err.println("Gene:"+this.getGeneSymbol()+"::"+tissue[i]);
                     int tmpTotal=0;
                     for(int j=0;j<ec.size();j++){
                         System.err.println(ec.get(j).getLocation()+"::"+ec.get(j).getProbeCount());
                         tmpTotal=tmpTotal+ec.get(j).getProbeCount();
                     }
                     totalCounts.put(tissue[i], tmpTotal);
                     qtlCounts.put(tissue[i], ec);
                 }
             }
         }
     }
     
     public HashMap getQTLs(){
         return qtls;
     }
     
     public HashMap getQTLCounts(){
         return this.qtlCounts;
     }
     
     public ArrayList<EQTLCount> getQTLCounts(String tissue){
         return (ArrayList<EQTLCount>) qtlCounts.get(tissue);
     }
     public int getTotalQTLProbesetCounts(String tissue){
         int ret=0;
         Object tmp=totalCounts.get(tissue);
         if(tmp!=null){
             ret=Integer.parseInt(tmp.toString());
         }
         return ret;
     }
     
     public void addTranscriptCluster(HashMap transcriptClustersCore,HashMap transcriptClustersExt,HashMap transcriptClustersFull,Logger log){
         //log.debug("process Gene:"+this.geneID);
         TranscriptCluster max=this.getMaxOverlap(transcriptClustersCore);
         if(max!=null){
             tc=max;
         }else{
             max=this.getMaxOverlap(transcriptClustersExt);
             if(max!=null){
                 tc=max;
             }else{
                 max=this.getMaxOverlap(transcriptClustersFull);
                 if(max!=null){
                     tc=max;
                 }
             }
         }
         
     }
     
     private TranscriptCluster getMaxOverlap(HashMap transcriptClusters){
         TranscriptCluster max=null;
         double percOverlap=0;
         Iterator trxItr=transcriptClusters.keySet().iterator();
         while(trxItr.hasNext()){
             TranscriptCluster tc=(TranscriptCluster)transcriptClusters.get(trxItr.next());
             if(tc.getStrand().equals(this.strand)){
                 long tcStart=tc.getStart();
                 long tcEnd=tc.getEnd();
                 long maxStart=this.start;
                 if(tcStart>this.start){
                     maxStart=tcStart;
                 }
                 long minEnd=this.end;
                 if(tcEnd<this.end){
                     minEnd=tcEnd;
                 }
                 long overlapLen=minEnd-maxStart;
                 double geneLen=this.length;
                 if(overlapLen>0){
                     double curPercOverLap=overlapLen/geneLen*100;
                     //log.debug(" overlapLen >0 :"+tc.getTranscriptClusterID()+" w/ "+curPercOverLap);
                     if(curPercOverLap>percOverlap){
                         percOverlap=curPercOverLap;
                         max=tc;
                     }
                 }
             }
         }
         if(max!=null && percOverlap>0){
             transcriptClusters.remove(max);
         }
         return max;
     }
     
     public TranscriptCluster getTranscriptCluster(){
         return tc;
     }
     
     public int getSnpCount(String strain,String type){
         int ret=0;
         HashMap m=(HashMap)snps.get(strain);
         if(m!=null){
             HashMap t=(HashMap)m.get(type);
             if(t!=null){
                 ret=t.size();
             }
         }
         return ret;
     }
     
     private void setupSnps(ArrayList<Transcript> addedTranscripts){
         //Logger log = Logger.getRootLogger();
         //fill strain specific
         for(int i=0;i<addedTranscripts.size();i++){
             ArrayList<SequenceVariant> trVar=addedTranscripts.get(i).getVariants();
             for(int j=0;j<trVar.size();j++){
                 HashMap strain=null;
                 HashMap type=null;
                 if(snps.containsKey(trVar.get(j).getStrain())){
                     strain=(HashMap)snps.get(trVar.get(j).getStrain());
                 }else{
                     strain=new HashMap();
                     snps.put(trVar.get(j).getStrain(), strain);
                 }
                 if(strain.containsKey(trVar.get(j).getShortType())){
                     type=(HashMap)strain.get(trVar.get(j).getShortType());
                 }else{
                     type=new HashMap();
                     strain.put(trVar.get(j).getShortType(), type);
                 }
                 if(type.containsKey(trVar.get(j).getId())){
                     
                 }else{
                     type.put(trVar.get(j).getId(), trVar.get(j));
                 }
             }
         }
         //find common variants
         HashMap common=null;
         if(snps.containsKey("common")){
             common=(HashMap)snps.get("common");
         }else{
             common=new HashMap();
             snps.put("common", common);
         }
         //Need to make this work for different strains in the future but for now this will work.
         HashMap bnlx=(HashMap)snps.get("BNLX");
         HashMap shrh=(HashMap)snps.get("SHRH");
         if(bnlx!=null&&shrh!=null){
             Iterator bnlxKey=bnlx.keySet().iterator();
             while(bnlxKey.hasNext()){
                 String bTypeKey=(String)bnlxKey.next();
                 HashMap bTypeHM=(HashMap)bnlx.get(bTypeKey);
                 HashMap sTypeHM=(HashMap)shrh.get(bTypeKey);
                 //log.debug("bType:"+bTypeKey);
                 ArrayList<Integer> bToRemove=new ArrayList<Integer>();
                 ArrayList<Integer> sToRemove=new ArrayList<Integer>();
                 if(bTypeHM!=null&&sTypeHM!=null){
                     //log.debug("TYPE MATCH");
                     matchCommon(common,bTypeHM,sTypeHM,bToRemove,sToRemove);
                     while(!bToRemove.isEmpty()){
                         Integer tmp=bToRemove.get(0);
                         bTypeHM.remove(tmp.intValue());
                         bToRemove.remove(0);
                     }
                     while(!sToRemove.isEmpty()){
                         Integer tmp=sToRemove.get(0);
                         sTypeHM.remove(tmp.intValue());
                         sToRemove.remove(0);
                     }
                 }
             }
        }
     }
     
     private void matchCommon(HashMap common,HashMap bTypeHM,HashMap sTypeHM,ArrayList<Integer> bToRemove,ArrayList<Integer> sToRemove){
        //Logger log = Logger.getRootLogger();
        Iterator bnlxKey=bTypeHM.keySet().iterator();
         while(bnlxKey.hasNext()){
                 int bk=((Integer)bnlxKey.next()).intValue();
                 SequenceVariant bv=(SequenceVariant)bTypeHM.get(bk);
                 if(bv!=null){
                     //log.debug("bnlxVar:"+bv.toString());
                     boolean foundMatch=false;
                     Iterator shrhKey=sTypeHM.keySet().iterator();
                     while(!foundMatch&&shrhKey.hasNext()){
                         int sk=((Integer)shrhKey.next()).intValue();
                         SequenceVariant sv=(SequenceVariant)sTypeHM.get(sk);
                         if(sv!=null&& bv.getShortType().equals(sv.getShortType())){
                             //log.debug(" Compare to shrhVar:"+sv.toString());
                             if(bv.getStart()==sv.getStart() && bv.getStop()==sv.getStop() && 
                                     bv.getRefSeq().equals(sv.getRefSeq()) && bv.getStrainSeq().equals(sv.getStrainSeq())){
                                 HashMap type;
                                 if(common.containsKey(bv.getShortType())){
                                     type=(HashMap)common.get(bv.getShortType());
                                 }else{
                                     type=new HashMap();
                                     common.put(bv.getShortType(), type);
                                 }
                                 //log.debug("FOUND MATCH");
                                 foundMatch=true;
                                 ArrayList<SequenceVariant> combined=new ArrayList<SequenceVariant>();
                                 combined.add(sv);
                                 combined.add(bv);
                                 type.put(bv.getId()+":"+sv.getId(),combined);
                                 sToRemove.add(sk);
                                 bToRemove.add(bk);
                             }
                         }
                     }
                 }
 
             }
     }
     
     //Methods to read Gene Data from RegionXML file.
     public static ArrayList<Gene> readGenes(String url) {
         ArrayList<Gene> genelist=new ArrayList<Gene>();
         try {
             DocumentBuilder build=DocumentBuilderFactory.newInstance().newDocumentBuilder();
             Document transcriptDoc=build.parse(url);
             NodeList genes=transcriptDoc.getElementsByTagName("Gene");
             //System.out.println("# Genes"+genelist.length);
             for(int i=0;i<genes.getLength();i++){
                 NamedNodeMap attrib=genes.item(i).getAttributes();
                 if(attrib.getLength()>0){
                     String geneID=attrib.getNamedItem("ID").getNodeValue();
                     if(geneID.contains("XLOC")){
                         Matcher m=Pattern.compile("_0+").matcher(geneID);
                         if(m.find()){
                             int startPos=m.end();
                             geneID="Brain.G"+geneID.substring(startPos);
                         }
                     }
                     //System.out.println("reading gene ID:"+geneID);
                     String geneSymbol=attrib.getNamedItem("geneSymbol").getNodeValue();
                     String biotype=attrib.getNamedItem("biotype").getNodeValue();
                     long start=Long.parseLong(attrib.getNamedItem("start").getNodeValue());
                     long stop=Long.parseLong(attrib.getNamedItem("stop").getNodeValue());
                     String strand=attrib.getNamedItem("strand").getNodeValue();
                     String chr=attrib.getNamedItem("chromosome").getNodeValue();
                     String source=attrib.getNamedItem("source").getNodeValue();
                     String description="";
                     Node tmpNode=attrib.getNamedItem("description");
                     if(tmpNode!=null){
                         description=tmpNode.getNodeValue();
                     }
                     Gene tmpG=new Gene(geneID,start,stop,chr,strand,biotype,geneSymbol,source,description);
                     NodeList transcripts=genes.item(i).getChildNodes();
                     ArrayList<Transcript> tmp=readTranscripts(transcripts.item(1).getChildNodes(),geneID);
                     tmpG.setTranscripts(tmp);
                     genelist.add(tmpG);
                 }
             }
         } catch (SAXException ex) {
             ex.printStackTrace(System.err);
             
         } catch (IOException ex) {
             ex.printStackTrace(System.err);
             
         } catch (ParserConfigurationException ex) {
             ex.printStackTrace(System.err);
             
         }
         return genelist;
         
     }
     private static ArrayList<Transcript> readTranscripts(NodeList nodes,String geneID) {
         ArrayList<Transcript> transcripts=new ArrayList<Transcript>();
         String tissue="";
         if(!geneID.startsWith("ENS")&&geneID.indexOf(".")>-1){
             tissue=geneID.substring(0,geneID.indexOf("."));
         }else{
             tissue="Brain";
         }
         for(int i=0;i<nodes.getLength();i++){
             if(nodes.item(i).getNodeName().equals("Transcript")){
                 ArrayList<Exon> exons=null;
                 ArrayList<Intron> introns=null;
                 ArrayList<Annotation> annot=null;
                 NodeList children=nodes.item(i).getChildNodes();
                 for(int j=0;j<children.getLength();j++){
                     //System.out.println(j+":"+children.item(j).getNodeName());
                     if(children.item(j).getNodeName().equals("exonList")){
                         exons=readExons(children.item(j).getChildNodes());
                     }
                     if(children.item(j).getNodeName().equals("intronList")){
                         introns=readIntrons(children.item(j).getChildNodes());
                     }
                     if(children.item(j).getNodeName().equals("annotationList")){
                         annot=readAnnotations(children.item(j).getChildNodes());
                     }
                 }
                 NamedNodeMap nnm=nodes.item(i).getAttributes();
                 long start=Long.parseLong(nnm.getNamedItem("start").getNodeValue());
                 long end=Long.parseLong(nnm.getNamedItem("stop").getNodeValue());
                 String trID=nnm.getNamedItem("ID").getNodeValue();
                 if(!trID.startsWith("ENS")){
                     Matcher m=Pattern.compile("_0+").matcher(trID);
                     if(m.find()){
                         int startPos=m.end();
                         trID=tissue+".T"+trID.substring(startPos);
                     }
                 }
                 Transcript tmptrans=new Transcript(trID,nnm.getNamedItem("strand").getNodeValue(),start,end);
                 tmptrans.setExon(exons);
                 tmptrans.setIntron(introns);
                 tmptrans.setAnnotation(annot);
                 if(nnm.getNamedItem("category")!=null){
                     tmptrans.setCategory(nnm.getNamedItem("category").getNodeValue());
                 }
                 tmptrans.fillFullTranscript();
                 transcripts.add(tmptrans); 
             }
         }
         //System.out.println("Transcript Array List Size at read:"+transcripts.size());
         return transcripts;
         
     }
     private static ArrayList<Exon> readExons(NodeList exonNodes) {
         ArrayList<Exon> ret=new ArrayList<Exon>();
         for(int z=0;z<exonNodes.getLength();z++){
             //System.out.println("exonNodes"+z+":"+exonNodes.item(z).getNodeName());
             if (exonNodes.item(z).getNodeName().equals("exon")) {
                 NamedNodeMap attrib=exonNodes.item(z).getAttributes();
                 String ExonID=attrib.getNamedItem("ID").getNodeValue();
                 //System.out.println("ExonID:"+ExonID);
                 long exonStart=-1,exonStop=-1,CodeStart=-1,CodeStop=-1;
                 exonStart = Long.parseLong(attrib.getNamedItem("start").getNodeValue());
                 exonStop = Long.parseLong(attrib.getNamedItem("stop").getNodeValue());
                 CodeStart = Long.parseLong(attrib.getNamedItem("coding_start").getNodeValue());
                 CodeStop = Long.parseLong(attrib.getNamedItem("coding_stop").getNodeValue());
                 
                 ArrayList<ProbeSet> probesets=new ArrayList<ProbeSet>();
                 NodeList children=exonNodes.item(z).getChildNodes();
                 for (int x = 0; x < children.getLength(); x++) {
                     if(children.item(x).getNodeName().equals("ProbesetList")){
                          NodeList probeNodes=children.item(x).getChildNodes();
                          probesets=readProbeSet(probeNodes);
                      }
                 }
                 
                 ArrayList<SequenceVariant> varList=new ArrayList<SequenceVariant>();
                 //NodeList children=exonNodes.item(z).getChildNodes();
                 for (int x = 0; x < children.getLength(); x++) {
                     if(children.item(x).getNodeName().equals("VariantList")){
                          NodeList varNodes=children.item(x).getChildNodes();
                          varList=readVariant(varNodes);
                      }
                 }
                 
                 Exon tmp=new Exon(exonStart,exonStop,ExonID);
                 tmp.setProteinCoding(CodeStart,CodeStop);
                 tmp.setProbeSets(probesets);
                 tmp.setVariants(varList);
                 ret.add(tmp);
             }
         }
         //System.out.println("Exon Array List Size at read:"+ret.size());
         return ret;
     }
     
     private static ArrayList<Annotation> readAnnotations(NodeList annotationNodes) {
         ArrayList<Annotation> ret=new ArrayList<Annotation>();
         for(int z=0;z<annotationNodes.getLength();z++){
             //System.out.println("exonNodes"+z+":"+exonNodes.item(z).getNodeName());
             if (annotationNodes.item(z).getNodeName().equals("annotation")) {
                 NamedNodeMap attrib=annotationNodes.item(z).getAttributes();
                 String source=attrib.getNamedItem("source").getNodeValue();
                 String value=attrib.getNamedItem("annot_value").getNodeValue();
                 String reason=attrib.getNamedItem("reason").getNodeValue();
                 Annotation tmp=new Annotation(source,value,"transcript",reason);
                 ret.add(tmp);
             }
         }
         //System.out.println("Exon Array List Size at read:"+ret.size());
         return ret;
     }
     
     private static ArrayList<Intron> readIntrons(NodeList intronNodes) {
         ArrayList<Intron> ret=new ArrayList<Intron>();
         for(int z=0;z<intronNodes.getLength();z++){
             //System.out.println("exonNodes"+z+":"+exonNodes.item(z).getNodeName());
             if (intronNodes.item(z).getNodeName().equals("intron")) {
                 NamedNodeMap attrib=intronNodes.item(z).getAttributes();
                 String intronID=attrib.getNamedItem("ID").getNodeValue();
                 //System.out.println("ExonID:"+ExonID);
                 long intronStart=-1,intronStop=-1;
                 intronStart = Long.parseLong(attrib.getNamedItem("start").getNodeValue());
                 intronStop = Long.parseLong(attrib.getNamedItem("stop").getNodeValue());
                 
                 ArrayList<ProbeSet> probesets=new ArrayList<ProbeSet>();
                 NodeList children=intronNodes.item(z).getChildNodes();
                 for (int x = 0; x < children.getLength(); x++) {
                     if(children.item(x).getNodeName().equals("ProbesetList")){
                          NodeList probeNodes=children.item(x).getChildNodes();
                          probesets=readProbeSet(probeNodes);
                      }
                 }
                 Intron tmp=new Intron(intronStart,intronStop,intronID);
                 tmp.setProbeSets(probesets);
                 ret.add(tmp);
             }
         }
         //System.out.println("Exon Array List Size at read:"+ret.size());
         return ret;
     }
     
     private static ArrayList<ProbeSet> readProbeSet(NodeList probesetNodes){
         ArrayList<ProbeSet> ret=new ArrayList<ProbeSet>();
         //System.out.println("Probeset Node size:"+probesetNodes.getLength());
         for(int z=0;z<probesetNodes.getLength();z++){
             if (probesetNodes.item(z).getNodeName().equals("Probeset")) {
                 NamedNodeMap attrib=probesetNodes.item(z).getAttributes();
                 String probeID= attrib.getNamedItem("ID").getNodeValue();
                 //System.err.println("reading ProbeID:"+probeID);
                 long probeStart=-1,probeStop=-1;
                 
                 String seq="",strand="",type="",locUpdate="";
                 probeStart=Integer.parseInt(attrib.getNamedItem("start").getNodeValue());
                 probeStop=Integer.parseInt(attrib.getNamedItem("stop").getNodeValue());
                 seq=attrib.getNamedItem("sequence").getNodeValue();
                 strand=attrib.getNamedItem("strand").getNodeValue();
                 locUpdate=attrib.getNamedItem("updatedlocation").getNodeValue();
                 type=attrib.getNamedItem("type").getNodeValue();
                 ProbeSet tmp=new ProbeSet(probeStart,probeStop,probeID,seq,strand,type,locUpdate);
                 ret.add(tmp);
             }
         }
         //System.out.println("Probeset Array List Size at read:"+ret.size());
         return ret;
     }
     
     private static ArrayList<SequenceVariant> readVariant(NodeList varNodes){
         ArrayList<SequenceVariant> ret=new ArrayList<SequenceVariant>();
         for(int z=0;z<varNodes.getLength();z++){
             if (varNodes.item(z).getNodeName().equals("Variant")) {
                 NamedNodeMap attrib=varNodes.item(z).getAttributes();
                 int ID=Integer.parseInt(attrib.getNamedItem("ID").getNodeValue());
                 //System.err.println("reading ProbeID:"+probeID);
                 int start=-1,stop=-1;
                 
                 String refSeq="",chr="",strainSeq="",type="",strain="";
                 start=Integer.parseInt(attrib.getNamedItem("start").getNodeValue());
                 stop=Integer.parseInt(attrib.getNamedItem("stop").getNodeValue());
                 refSeq=attrib.getNamedItem("refSeq").getNodeValue();
                 strainSeq=attrib.getNamedItem("strainSeq").getNodeValue();
                 strain=attrib.getNamedItem("strain").getNodeValue();
                 type=attrib.getNamedItem("type").getNodeValue();
                 chr=attrib.getNamedItem("chromosome").getNodeValue();
                 SequenceVariant tmp=new SequenceVariant(ID,start,stop,refSeq,strainSeq,type,strain);
                 ret.add(tmp);
             }
         }
         //System.out.println("Probeset Array List Size at read:"+ret.size());
         return ret;
     }
     
     public long[] getMinMaxCoord(){
         if(min<0 && max<0){
             for(int i=0;i<transcripts.size();i++){
                 if(transcripts.get(i).getStart()<transcripts.get(i).getStop()){
                     if(min<0&&max<0){
                         min=transcripts.get(i).getStart();
                         max=transcripts.get(i).getStop();
                     }else{
                         if(min>transcripts.get(i).getStart()){
                             min=transcripts.get(i).getStart();
                         }
                         if(max<transcripts.get(i).getStop()){
                             max=transcripts.get(i).getStop();
                         }
                     }
                 }else if(transcripts.get(i).getStop()<transcripts.get(i).getStart()){
                     if(min<0&&max<0){
                         min=transcripts.get(i).getStop();
                         max=transcripts.get(i).getStart();
                     }else{
                         if(min>transcripts.get(i).getStop()){
                             min=transcripts.get(i).getStop();
                         }
                         if(max<transcripts.get(i).getStart()){
                             max=transcripts.get(i).getStart();
                         }
                     }
                 }
             }
         }
         long[] ret=new long[2];
         ret[0]=min;
         ret[1]=max;
         return ret;
     }
 }
