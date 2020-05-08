 package uk.ac.ebi.fgpt.sampletab.emblbank;
 
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SexAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.SampleTabUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonException;
 import uk.ac.ebi.fgpt.sampletab.utils.TaxonUtils;
 
 public class EMBLBankRunnable implements Runnable{
 
 
 
     private TermSource ncbitaxonomy = new TermSource("NCBI Taxonomy", "http://www.ncbi.nlm.nih.gov/taxonomy/", null);
     Pattern latLongPattern = Pattern.compile("([0-9]+\\.?[0-9]*) ([NS]) ([0-9]+\\.?[0-9]*) ([EW])");
     
     private final EMBLBankHeaders headers;
           
     private final String[] line;
     
     private final Map<String, Set<String>> groupMap;
     private final Map<String, Set<Publication>> publicationMap;
     private final Map<String, SampleData> stMap;
     
     private final File outputDir;
     
     private final String prefix;
     
     private final boolean wgs;
     private final boolean tsa;
     private final boolean bar;
     private final boolean cds;
 
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
     public EMBLBankRunnable(EMBLBankHeaders headers, String[] line, 
             Map<String, Set<String>> groupMap, 
             Map<String, Set<Publication>> publicationMap, 
             Map<String, SampleData> stMap,
             File outputDir, String prefix, boolean wgs, boolean tsa, boolean bar, boolean cds){
         this.headers = headers;
         this.line = line;
         this.groupMap = groupMap;
         this.publicationMap = publicationMap;
         this.stMap = stMap;
         this.outputDir = outputDir;
         this.prefix = prefix;
         this.wgs = wgs;
         this.tsa = tsa;
         this.bar = bar;
         this.cds = cds;
         
     }
     
     private SampleNode lineToSample(String[] nextLine, SampleData st){
 
         SampleNode s = new SampleNode();
         
         for (int i = 0; i < nextLine.length && i < headers.size(); i++){
             String header = headers.get(i).trim();
             String value = nextLine[i].trim();
             if (value != null && value.length() > 0){
                 log.debug(header+" : "+value);
                 
                 if (header.equals("ACCESSION") || header.equals("ACC")){
                     s.setNodeName(value);
                 } else if (header.equals("ORGANISM")){
                     if (value.contains(" BOLD:")){
                         //remove BOLD:xxxxxx 
                         //BOLD = Barcode Of Life Database
                         String bold = value.substring(value.indexOf(" BOLD:"));
                         bold = bold.trim();
                         value = value.substring(0, value.indexOf(" BOLD:"));
                         s.addAttribute(new OrganismAttribute(value));
 
                         s.addAttribute(new DatabaseAttribute("BOLD", bold, "http://www.boldsystems.org/index.php/Public_BarcodeCluster?clusterguid="+bold));
                         
                     } else {
                         //no Bold entry, just use organism
                         s.addAttribute(new OrganismAttribute(value));
                     }
                 } else if (header.equals("BIO_MATERIAL")){
                     s.addAttribute(new CommentAttribute("Biomaterial", value));
                 } else if (header.equals("BIOSEQID")){
                     s.addAttribute(new CommentAttribute("BioSeq ID", value));
                 } else if (header.equals("CULTURE_COLLECTION")){
                     s.addAttribute(new CommentAttribute("Culture Collection", value));
                 } else if (header.equals("SPECIMEN_VOUCHER")){
                     s.addAttribute(new CommentAttribute("Specimen Voucher", value));
                 } else if (header.equals("COLLECTED_BY")){
                     s.addAttribute(new CommentAttribute("Collected By", value));
                 } else if (header.equals("COLLECTION_DATE")){
                     //TODO handle date
                     s.addAttribute(new CommentAttribute("Collection Date", value));
                 } else if (header.equals("COUNTRY")){
                     s.addAttribute(new CharacteristicAttribute("Geographic Origin", value));
                     //TODO split better
                 } else if (header.equals("HOST")){
                     s.addAttribute(new CharacteristicAttribute("Host", value));
                 } else if (header.equals("IDENTIFIED_BY")){
                     s.addAttribute(new CommentAttribute("Identified By", value));
                 } else if (header.equals("ISOLATION_SOURCE")){
                     s.addAttribute(new CharacteristicAttribute("Isolation Source", value));
                 } else if (header.equals("LAT_LON")){
                     Matcher m = latLongPattern.matcher(value);
                     //fails at these values
                     // 19.02 N 72.46
                     // 19.02 N 72.
                     
                     if (m.lookingAt()){
                         Float latitude = new Float(m.group(1));  
                         if (m.group(2).equals("S")){
                             latitude = -latitude;
                         }
                         UnitAttribute decimaldegrees = new UnitAttribute();
                         decimaldegrees.type = "unit";
                         decimaldegrees.setAttributeValue("decimal degree");
                         //TODO ontology term
                         
                         CharacteristicAttribute lat =new CharacteristicAttribute("Latitude", latitude.toString());
                         lat.unit = decimaldegrees;
                         s.addAttribute(lat);
                         
                         Float longitude = new Float(m.group(3));  
                         if (m.group(4).equals("W")){
                             longitude = -longitude;
                         }
                         CharacteristicAttribute longit = new CharacteristicAttribute("Longitude", longitude.toString());
                         longit.unit = decimaldegrees;
                         s.addAttribute(longit);
                     } else {
                         log.warn("Unable to match "+header+" : "+value);
                     }
                 } else if (header.equals("LAB_HOST")){
                     s.addAttribute(new CharacteristicAttribute("Lab Host", value));
                 } else if (header.equals("ENVIRONMENTAL_SAMPLE")){
                     s.addAttribute(new CommentAttribute("Environmental?", value));
                 } else if (header.equals("MATING_TYPE")){
                     s.addAttribute(new SexAttribute(value));
                 } else if (header.equals("SEX")){
                     s.addAttribute(new SexAttribute(value));
                 } else if (header.equals("CELL_TYPE")){
                     s.addAttribute(new CharacteristicAttribute("Cell Type", value));
                 } else if (header.equals("DEV_STAGE")){
                     s.addAttribute(new CharacteristicAttribute("Developmental Stage", value));
                 } else if (header.equals("GERMLINE")){
                     s.addAttribute(new CharacteristicAttribute("Germline", value));
                 } else if (header.equals("TISSUE_LIB")){
                     s.addAttribute(new CharacteristicAttribute("Tissue Library", value));
                 } else if (header.equals("TISSUE_TYPE")){
                     s.addAttribute(new CharacteristicAttribute("Organism Part", value));
                 } else if (header.equals("CULTIVAR")){
                     s.addAttribute(new CharacteristicAttribute("Cultivar", value));
                 } else if (header.equals("ECOTYPE")){
                     s.addAttribute(new CharacteristicAttribute("Ecotype", value));
                 } else if (header.equals("ISOLATE")){
                     s.addAttribute(new CharacteristicAttribute("Isolate", value));
                 } else if (header.equals("STRAIN")){
                     s.addAttribute(new CharacteristicAttribute("Strain or Line", value));
                 } else if (header.equals("SUB_SPECIES")){
                     s.addAttribute(new CharacteristicAttribute("Sub Species", value));
                 } else if (header.equals("VARIETY")){
                     s.addAttribute(new CharacteristicAttribute("Variety", value));
                 } else if (header.equals("SUB_STRAIN")){
                     s.addAttribute(new CharacteristicAttribute("Sub Strain", value));
                 } else if (header.equals("CELL_LINE")){
                     s.addAttribute(new CharacteristicAttribute("Strain or Line", value));
                 } else if (header.equals("SEROTYPE")){
                     s.addAttribute(new CharacteristicAttribute("Serotype", value));
                 } else if (header.equals("SEROVAR")){
                     s.addAttribute(new CharacteristicAttribute("Serovar", value));
                 } else if (header.equals("ORGANELLE")){
                     s.addAttribute(new CharacteristicAttribute("Organelle", value));
                 } else if (header.equals("PLASMID")){
                     s.addAttribute(new CharacteristicAttribute("Plasmid", value));
                 } else if (header.equals("TAX_ID")){
                     //manipulate existing organism attribute
                     boolean found = false;
                     for (SCDNodeAttribute a : s.getAttributes()){
                         if (OrganismAttribute.class.isInstance(a)){
                             OrganismAttribute o = (OrganismAttribute) a;
                             o.setTermSourceREF(st.msi.getOrAddTermSource(ncbitaxonomy));
                             o.setTermSourceID(value);
                             found = true;
                         }
                     }
                     if (!found){
                         //no existing organism attribute, add one
                         log.info("looking up taxid "+value);
                         Integer taxID = new Integer(value);
                         OrganismAttribute o = null;
                         try {
                             o = new OrganismAttribute(TaxonUtils.getSpeciesOfID(taxID));
                         } catch (TaxonException e) {
                             log.warn("Problem getting taxid of "+value, e);
                         } 
                         if (o == null){
                             o = new OrganismAttribute(value);
                         }
                         o.setTermSourceREF(st.msi.getOrAddTermSource(ncbitaxonomy));
                         o.setTermSourceID(value);
                         s.addAttribute(o);
                     }
                 }
             }
         }
         String dbname = "EMBL-Bank";
         if (wgs){
             dbname += " (WGS)";
         } else if (tsa){
             dbname += " (TSA)";
         } else if (bar){
             dbname += " (Barcode)";
         } else if (cds){
             dbname += " (CDS)";
         }
         s.addAttribute(new DatabaseAttribute(dbname, s.getNodeName(), "http://www.ebi.ac.uk/ena/data/view/"+s.getNodeName()));
         
         return s;
     }
     
     public List<String> getGroupIdentifiers(String[] line){
 
         List<String> identifiers = new ArrayList<String>();
         
         String projString = null;
         if (headers.projheaderindex > 0 && headers.projheaderindex < line.length){
             projString = line[headers.projheaderindex].trim();
         }
         if (projString != null && projString.length() > 0){
             for(String projID : projString.split(",")){
                 projID = projID.trim();
                 if (projID.length() > 0){
                     identifiers.add(projID);
                 }
             }
         } else {
             String pubString = null;
             if (headers.pubheaderindex > 0 && headers.pubheaderindex < line.length){
                 pubString = line[headers.pubheaderindex];
                 for(String pubID : pubString.split(",")){
                     pubID = pubID.trim();
                     if (pubID.length() > 0){
                         identifiers.add(pubID);
                     }
                 }
             }
         }
         //no publications, fall back to identified by
         if (identifiers.size() == 0){
             if (headers.identifiedbyindex > 0 && headers.identifiedbyindex < line.length){
                 String identifiedby = line[headers.identifiedbyindex];
                 identifiedby = identifiedby.replaceAll("[^\\w]", "");
                 
                 if (identifiedby.length() > 0){
                     identifiers.add(identifiedby);
                 }
             }
         }
         //no publications, fall back to collected by
         if (identifiers.size() == 0){
             if (headers.collectedbyindex > 0 && headers.collectedbyindex < line.length){
                 String collectedby = line[headers.collectedbyindex];
                 collectedby = collectedby.replaceAll("[^\\w]", "");
                 
                 if (collectedby.length() > 0){
                     identifiers.add(collectedby);
                 }
             }
         }
         //still nothing, have to use species
         //TODO maybe abstract this a level or two up the taxonomy?
         if (bar && identifiers.size() == 0){
             String organism = null;
             
             //use division, not species
             try {
                 organism = TaxonUtils.getDivisionOfID(new Integer(line[headers.taxidindex]));
                 organism.replaceAll(" ", "-");
                 organism.replaceAll("[^\\w_]", "");
                 while (organism.contains("--")){
                     organism.replaceAll("--", "-");
                 }
             } catch (NumberFormatException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (TaxonException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
             if (organism != null && organism.length() > 0){
                 organism = "taxon-"+organism;
                 identifiers.add(organism);
             }
         }
         
         if (identifiers.size() == 0){
             log.debug(line[0]+" has  no identifiers.");
             identifiers.add(line[0]);
         }
         return identifiers;
     }
     
     public Set<Publication> getPublications(String[] line){
         Set<Publication> pubSet = new HashSet<Publication>();
         
         String[] doiStrings = new String[0];
         if (headers.doiindex > 0 && headers.doiindex < line.length){
             doiStrings = line[headers.doiindex].trim().split(",");
         }
         
         String[] pubmedStrings = new String[0];
         if (headers.pubmedindex > 0 && headers.pubmedindex < line.length){
             pubmedStrings = line[headers.pubmedindex].trim().split(",");
         }
         
         int maxlength = doiStrings.length;
         if (pubmedStrings.length > maxlength){
             maxlength = pubmedStrings.length;
         }
 
         for (int i = 0; i < maxlength; i++){
             String pubmed = null;
             String doi = null;
             
             if (i < pubmedStrings.length){
                 pubmed = pubmedStrings[i].trim();
             }
             
             if (i < doiStrings.length){
                 doi = doiStrings[i].trim();
             }
             
             if ((pubmed != null && pubmed.length() > 0)
                     || (doi != null && doi.length() > 0)){
                 log.debug("Publication "+pubmed+" "+doi);
                 Publication p = new Publication(pubmed, doi);
                 pubSet.add(p);
             }
         }
         
         return pubSet;
     }
 
     public void run() {
         String accession = line[0].trim();
         log.debug("Second processing "+accession+" with cache size of "+stMap.size());
         
         //now we have a described node, but need to work out what samples to put it with
 
         
         for (String id : getGroupIdentifiers(line)){
             if (!groupMap.containsKey(id)){
                 continue;
             }
             
             if(!stMap.containsKey(id)){
                 stMap.put(id, new SampleData());
             }
         
             SampleData st = stMap.get(id);
 
 
             SampleNode s = lineToSample(line, st);
             
             if (s.getNodeName() == null){
                 log.error("Unable to add node with null name");
                 continue;
             }
             
             try {
                 if (s.getNodeName() == null){
                     log.error("Unable to add node with null name");
                 } else if (st.scd.getNode(s.getNodeName(), SampleNode.class) != null) {
                     //this node name has already been used
                     log.error("Unable to add duplicate node with name "+s.getNodeName());
                 } else if (s.getAttributes().size() <= 1){
                     //dont add uninformative samples
                     //will always have one database attribute
                     log.warn("Refusing to add sample "+s.getNodeName()+" without attributes");
                 } else {
                     st.scd.addNode(s);
                 }
             } catch (ParseException e) {
                 log.error("Unable to add node "+s.getNodeName(), e);
             }     
             
             if (!groupMap.containsKey(id)){
                 log.error("Problem locating publication ID "+id+" in publication maping");
                 continue;
             }
             
             if (!groupMap.get(id).contains(s.getNodeName())){
                 log.error("Problem removing sample "+s.getNodeName()+" from publication maping");
                 continue;
             }
             
             if (publicationMap.containsKey(accession) && publicationMap.get(accession).size() > 0){
                while(publicationMap.get(accession).contains(null)){
                    publicationMap.get(accession).remove(null);
                }
                 st.msi.publications.addAll(publicationMap.get(accession));
                 log.debug(accession+" "+publicationMap.get(accession).size());
                 log.debug(accession+" "+st.msi.publications.size());
             }
             
             groupMap.get(id).remove(s.getNodeName());
             
             if (groupMap.get(id).size() == 0){
                 //no more accessions to be added to this sampletab
                 //export it
 
                 //create some information
                 st.msi.submissionIdentifier = prefix+"-"+id;
                 st.msi.submissionIdentifier = st.msi.submissionIdentifier.replace(" ", "-");
                 st.msi.submissionIdentifier = st.msi.submissionIdentifier.replace("&", "and");
                 if (wgs){
                     //its a whole genome shotgun sample, describe as such
                     String speciesName = null;
                     for (SampleNode sn : st.scd.getNodes(SampleNode.class)){
                         for (SCDNodeAttribute a : sn.getAttributes()){
                             if (OrganismAttribute.class.isInstance(a)){
                                 speciesName = a.getAttributeValue();
                             }
                         }
                     }
                     if (speciesName == null){
                         log.warn("Unable to determine species name");
                     } else {
                         st.msi.submissionTitle = "Whole Genome Shotgun sequencing of "+speciesName;
                     }
                 } else if (tsa){
                     //its a transcriptome shotgun sample, describe as such
                     String speciesName = null;
                     for (SampleNode sn : st.scd.getNodes(SampleNode.class)){
                         for (SCDNodeAttribute a : sn.getAttributes()){
                             if (OrganismAttribute.class.isInstance(a)){
                                 speciesName = a.getAttributeValue();
                             }
                         }
                     }
                     if (speciesName == null){
                         log.warn("Unable to determine species name");
                     } else {
                         st.msi.submissionTitle = "Transcriptome Shotgun sequencing of "+speciesName;
                     }
                 } else if (bar){
                     //need to generate a title for the submission
                     st.msi.submissionTitle = SampleTabUtils.generateSubmissionTitle(st);
                     //TODO use the title of a publication, if one exists
                 } else if (cds){
                     //need to generate a title for the submission
                     st.msi.submissionTitle = SampleTabUtils.generateSubmissionTitle(st);
                 } else {
                     log.warn("No submission type indicated");
                     
                 }
                 
                 
                 log.debug("No. of publications = "+st.msi.publications.size());
                 log.debug("Empty? "+st.msi.publications.isEmpty());
                 
                 
                 
 
                 //add an intermediate subdir layer based on the initial 7 characters (GEM-...)
                 File outputSubDir = new File(outputDir, SampleTabUtils.getPathPrefix(st.msi.submissionIdentifier));
                 outputSubDir = new File(outputSubDir, st.msi.submissionIdentifier);
                 outputSubDir.mkdirs();
                 File sampletabPre = new File(outputSubDir, "sampletab.pre.txt");
                 
                 
                 
                 log.debug("Writing "+sampletabPre);
                 synchronized(SampleTabWriter.class){
                     SampleTabWriter sampletabwriter = null;
                     try {
                         sampletabwriter = new SampleTabWriter(new BufferedWriter(new FileWriter(sampletabPre)));
                         sampletabwriter.write(st);
                     } catch (IOException e) {
                         log.error("Unable to write to "+sampletabPre, e);
                     } finally {
                         if (sampletabwriter != null){
                             try {
                                 sampletabwriter.close();
                             } catch (IOException e) {
                                 //do nothing
                             }
                         }
                     }
                 }
                 //remove from mappings
                 groupMap.remove(id);
                 stMap.remove(id);
             }
         }
     }
     
     
     
     
     
 
 }
