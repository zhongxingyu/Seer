 package com.compomics.relims.model;
 
 import com.compomics.omssa.xsd.UserMod;
 import com.compomics.relims.conf.RelimsProperties;
 import com.compomics.relims.guava.functions.DoubleRounder;
 import com.compomics.relims.guava.functions.ProteinJoiner;
 import com.compomics.relims.interfaces.SearchCommandGenerator;
 import com.compomics.util.experiment.biology.Peptide;
 import com.compomics.util.experiment.identification.Advocate;
 import com.compomics.util.experiment.identification.PeptideAssumption;
 import com.compomics.util.experiment.identification.matches.ModificationMatch;
 import com.compomics.util.experiment.identification.matches.SpectrumMatch;
 import com.compomics.util.experiment.io.identifications.IdfileReader;
 import com.compomics.util.experiment.io.identifications.IdfileReaderFactory;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.common.io.Files;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import javax.annotation.Nullable;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.*;
 
 import static com.google.common.base.Preconditions.checkState;
 
 /**
  * This class is a
  */
 public class OMSSASearchProcessor implements SearchProcessor {
     private static Logger logger = Logger.getLogger(OMSSASearchProcessor.class);
     private static final DoubleRounder iRounder = new DoubleRounder(4);
     private final SearchList<SearchCommandGenerator> iSearchList;
     private char iSeparator = ',';
     private char iSubSeparator = ';';
 
     private ArrayList<UserMod> iRelimsModifications = RelimsProperties.getRelimsMods();
 
     private boolean includeProteinDetails = true;
     private boolean includeExtraModDetails = false;
 
     private ProteinJoiner iProteinJoiner = new ProteinJoiner();
 
 
     public OMSSASearchProcessor(SearchList<SearchCommandGenerator> aSearchList) {
         iSearchList = aSearchList;
     }
 
     public void process() throws SAXException, IOException {
         File lOutput = new File(RelimsProperties.getWorkSpace(), "relims_omssa_output_pr_" + iSearchList.get(0).getProjectId() + ".csv");
         BufferedWriter writer = Files.newWriter(lOutput, Charset.defaultCharset());
 
 
         Joiner joiner = Joiner.on(iSeparator);
         int lLineCount = 0;
 
         for (SearchCommandGenerator lSearch : iSearchList) {
             logger.debug("processing search " + lSearch.getName());
             File lResultFolder = lSearch.getSearchResultFolder();
 
             ArrayList<String> lGeneralFeatures = Lists.newArrayList();
             lGeneralFeatures.add("" + lSearch.getName());
             lGeneralFeatures.add("" + lSearch.getProjectId());
             lGeneralFeatures.add("" + Joiner.on(";").join(lSearch.getSpectrumFiles()));
 
             // load your identification file "yourFile" (Mascot DAT file, OMSSA OMX file or X!Tandem XML file)
             File[] lOmssaResultFiles = lResultFolder.listFiles(new FilenameFilter() {
                 public boolean accept(File aFile, String s) {
                     return s.endsWith(".omx");
                 }
             });
 
             for (File lOmssaResultFile : lOmssaResultFiles) {
                 // get the correspondig reader
                 IdfileReader lIdFileReader = IdfileReaderFactory.getInstance().getFileReader(lOmssaResultFile, true);
                 logger.debug("writing results to " + lOutput.getCanonicalPath());
                 // load all identifications
                 HashSet<SpectrumMatch> matches = lIdFileReader.getAllSpectrumMatches();
                 for (SpectrumMatch lSpectrumMatch : matches) {
                     lLineCount++;
 
                     HashMap<Double, ArrayList<PeptideAssumption>> lAllAssumptions = lSpectrumMatch.getAllAssumptions(Advocate.OMSSA);
                     List<Double> lDoubles = Lists.newArrayList();
                     lDoubles.addAll(lAllAssumptions.keySet());
 
                     Collections.sort(lDoubles, new Comparator<Double>() {
                         public int compare(Double aDouble, Double aDouble1) {
                             if (aDouble < aDouble1) {
                                 return -1;
                             } else if (aDouble > aDouble1) {
                                 return 1;
                             } else {
                                 return 0;
                             }
                         }
                     });
 
 
                     ArrayList<PeptideAssumption> lSortedAssumptions = Lists.newArrayList();
                     int lRank = 0;
                     for (Double lEValue : lDoubles) {
                         ArrayList<PeptideAssumption> lPeptideAssumptions = lAllAssumptions.get(lEValue);
                         lRank++;
                         for (PeptideAssumption lPeptideAssumption : lPeptideAssumptions) {
                             lPeptideAssumption.setRank(lRank);
                         }
                         lSortedAssumptions.addAll(lPeptideAssumptions);
                     }
 
                     for (PeptideAssumption lPeptideAssumption : lSortedAssumptions) {
                         if (lPeptideAssumption != null) {
                             ArrayList<String> lFeatures = Lists.newArrayList();
                             lFeatures.addAll(lGeneralFeatures);
                             lFeatures.addAll(extractSpectrumFeatures(lSpectrumMatch));
                             lFeatures.addAll(extractPeptideFeatures(lPeptideAssumption));
 
                             if (includeExtraModDetails) {
                                 lFeatures.addAll(extractModificationFeatures(lPeptideAssumption));
                             }
 
                             if (includeProteinDetails) {
                                 lFeatures.addAll(extractProteinFeatures(lPeptideAssumption));
                             }
 
                             String line = joiner.join(lFeatures);
                             System.out.println(line);
 
                             writer.write(line);
                             writer.newLine();
 
                            if (lLineCount % 1000 == 0) {
                                 writer.flush();
                             }
                            if (lLineCount % 10000 == 0) {
                                 logger.debug("written " + lLineCount + " entries ...");
                             }
                         }
                     }
                 }
             }
         }
 
         writer.flush();
         writer.close();
 
         logger.debug("finished writing " + lLineCount + " spectrum identifications");
 
     }
 
     private Collection<? extends String> extractProteinFeatures(PeptideAssumption aBestAssumption) {
 
         ArrayList<String> lParentProteins = aBestAssumption.getPeptide().getParentProteins();
         ArrayList<String> lResults = Lists.newArrayList();
 
        lResults.add("" + lParentProteins.size());
         String lConcatenatedProteins = iProteinJoiner.apply(lParentProteins);
         lResults.add("" + lConcatenatedProteins);
         if (lConcatenatedProteins.indexOf("SHUFFLED") > 0) {
             lResults.add("SHUFFLED");
         } else {
             lResults.add("NON_SHUFFLED");
         }
 
         return lResults;
 
     }
 
     private Collection<? extends String> extractSpectrumFeatures(SpectrumMatch aSpectrumMatch) {
         ArrayList<String> lFeatures = Lists.newArrayList();
         lFeatures.add(aSpectrumMatch.getKey());
         return lFeatures;
     }
 
     private Collection<String> extractModificationFeatures(PeptideAssumption aBestAssumption) {
 
         final ArrayList<ModificationMatch> lModificationMatches = aBestAssumption.getPeptide().getModificationMatches();
 
         List<String> lRelimsMods = Lists.transform(iRelimsModifications, new Function<UserMod, String>() {
             public String apply(@Nullable UserMod input) {
                 for (ModificationMatch lModificationMatch : lModificationMatches) {
 
                     if (lModificationMatch.getTheoreticPtm().trim().toLowerCase().equals(input.getModificationName().toLowerCase())) {
                         return String.valueOf(true);
                     }
                 }
                 return String.valueOf(false);
             }
         });
 
         // assert that the an as much booleans are returned as there are relimsmodifiations.
         checkState(iRelimsModifications.size() == lRelimsMods.size());
 
         return lRelimsMods;
     }
 
     private ArrayList<String> extractPeptideFeatures(PeptideAssumption aPeptideAssumption) {
         ArrayList<String> lFeatures = Lists.newArrayList();
 
         Peptide lPeptide = aPeptideAssumption.getPeptide();
 
         String lProteins = "" + Joiner.on(iSubSeparator).join(
                 lPeptide.getParentProteins());
 
         lFeatures.add(lPeptide.getSequence());
         lFeatures.add("" + aPeptideAssumption.getRank());
         lFeatures.add("" + lProteins);
         lFeatures.add("" + iRounder.apply(lPeptide.getMass()));
         lFeatures.add("" + iRounder.apply(aPeptideAssumption.getEValue()));
 
         return lFeatures;
     }
 
     public void setIncludeProteinDetails(boolean aStatus) {
         includeProteinDetails = aStatus;
     }
 
     public void setIncludeExtraModDetails(boolean aStatus) {
         includeExtraModDetails = aStatus;
     }
 }
