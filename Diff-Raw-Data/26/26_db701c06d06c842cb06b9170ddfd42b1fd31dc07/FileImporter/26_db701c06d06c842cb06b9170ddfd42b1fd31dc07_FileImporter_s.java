 package eu.isas.peptideshaker.fileimport;
 
 import com.compomics.util.experiment.ProteomicAnalysis;
 import com.compomics.util.experiment.biology.Enzyme;
 import com.compomics.util.experiment.biology.PTM;
 import com.compomics.util.experiment.biology.PTMFactory;
 import com.compomics.util.experiment.biology.Peptide;
 import com.compomics.util.experiment.identification.Identification;
 import com.compomics.util.experiment.identification.IdentificationMethod;
 import com.compomics.util.experiment.identification.PeptideAssumption;
 import com.compomics.util.experiment.identification.SequenceFactory;
 import com.compomics.util.experiment.identification.matches.ModificationMatch;
 import com.compomics.util.experiment.identification.matches.SpectrumMatch;
 import com.compomics.util.experiment.io.identifications.IdfileReader;
 import com.compomics.util.experiment.io.identifications.IdfileReaderFactory;
 import com.compomics.util.experiment.massspectrometry.Spectrum;
 import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
 import com.compomics.util.protein.Header.DatabaseType;
 import eu.isas.peptideshaker.PeptideShaker;
 import eu.isas.peptideshaker.scoring.InputMap;
 import eu.isas.peptideshaker.gui.WaitingDialog;
 import eu.isas.peptideshaker.preferences.AnnotationPreferences;
 import eu.isas.peptideshaker.preferences.SearchParameters;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 
 /**
  * This class is responsible for the import of identifications
  *
  * @author  Marc Vaudel
  * @author  Harald Barsnes
  */
 public class FileImporter {
 
     /**
      * The class which will load the information into the various maps and do the associated calculations
      */
     private PeptideShaker peptideShaker;
     /**
      * The current proteomicAnalysis
      */
     private ProteomicAnalysis proteomicAnalysis;
     /**
      * The identification filter to use
      */
     private IdFilter idFilter;
     /**
      * A dialog to display feedback to the user
      */
     private WaitingDialog waitingDialog;
     /**
      * The location of the modification file
      */
     private final String MODIFICATION_FILE = "conf/peptideshaker_mods.xml";
     /**
      * The location of the user modification file
      */
     private final String USER_MODIFICATION_FILE = "conf/peptideshaker_usermods.xml";
     /**
      * The modification factory
      */
     private PTMFactory ptmFactory = PTMFactory.getInstance();
     /**
      * The spectrum factory
      */
     private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance(100);
     /**
      * The sequence factory
      */
     private SequenceFactory sequenceFactory = SequenceFactory.getInstance(100000);
     /**
      * Peptide to protein map: peptide sequence -> protein accession
      */
     private HashMap<String, ArrayList<String>> sequences = new HashMap<String, ArrayList<String>>();
     /**
      * db processing disabled only while testing
      */
     boolean testing = false;
 
     /**
      * Constructor for the importer
      *
      * @param identificationShaker  the identification shaker which will load the data into the maps and do the preliminary calculations
      * @param waitingDialog         A dialog to display feedback to the user
      * @param proteomicAnalysis     The current proteomic analysis
      * @param idFilter              The identification filter to use
      */
     public FileImporter(PeptideShaker identificationShaker, WaitingDialog waitingDialog, ProteomicAnalysis proteomicAnalysis, IdFilter idFilter) {
         this.peptideShaker = identificationShaker;
         this.waitingDialog = waitingDialog;
         this.proteomicAnalysis = proteomicAnalysis;
         this.idFilter = idFilter;
     }
 
     /**
      * Constructor for an import without filtering
      * @param identificationShaker  the parent identification shaker
      * @param waitingDialog         a dialog to give feedback to the user
      * @param proteomicAnalysis     the current proteomic analysis
      */
     public FileImporter(PeptideShaker identificationShaker, WaitingDialog waitingDialog, ProteomicAnalysis proteomicAnalysis) {
         this.peptideShaker = identificationShaker;
         this.waitingDialog = waitingDialog;
         this.proteomicAnalysis = proteomicAnalysis;
     }
 
     /**
      * Imports the identification from files.
      *
      * @param idFiles               the identification files to import the Ids from
      * @param spectrumFiles         the files where the corresponding spectra can be imported
      * @param fastaFile             the FASTA file to use
      * @param searchParameters      the search parameters
      * @param annotationPreferences the annotation preferences to use for PTM scoring
      */
     public void importFiles(ArrayList<File> idFiles, ArrayList<File> spectrumFiles, File fastaFile, SearchParameters searchParameters, AnnotationPreferences annotationPreferences) {
         IdProcessorFromFile idProcessor = new IdProcessorFromFile(idFiles, spectrumFiles, fastaFile, idFilter, searchParameters, annotationPreferences);
         idProcessor.execute();
     }
 
     /**
      * Import spectra from spectrum files.
      * 
      * @param spectrumFiles
      */
     public void importFiles(ArrayList<File> spectrumFiles) {
         SpectrumProcessor spectrumProcessor = new SpectrumProcessor(spectrumFiles);
         spectrumProcessor.execute();
     }
 
     /**
      * Imports sequences from a fasta file
      *
      * @param waitingDialog     Dialog displaying feedback to the user
      * @param proteomicAnalysis The proteomic analysis to attach the database to
      * @param fastaFile         FASTA file to process
      * @param idFilter          the identification filter
      * @param searchParameters  The search parameters
      */
     public void importSequences(WaitingDialog waitingDialog, ProteomicAnalysis proteomicAnalysis, File fastaFile, IdFilter idFilter, SearchParameters searchParameters) {
 
         try {
             waitingDialog.appendReport("Importing sequences from " + fastaFile.getName() + ".");
            waitingDialog.setSecondaryProgressDialogIntermediate(false);
            waitingDialog.resetSecondaryProgressBar();
             sequenceFactory.loadFastaFile(fastaFile, waitingDialog.getSecondaryProgressBar());
 
             String firstAccession = sequenceFactory.getAccessions().get(0);
             if (sequenceFactory.getHeader(firstAccession).getDatabaseType() != DatabaseType.UniProt) {
                 JOptionPane.showMessageDialog(waitingDialog,
                         "We strongly recommend the use of UniProt accession numbers.\n"
                         + "Some features will be limited if using other databases.",
                         "Information",
                         JOptionPane.INFORMATION_MESSAGE);
             }
 
             if (!sequenceFactory.concatenatedTargetDecoy()) {
                 JOptionPane.showMessageDialog(waitingDialog,
                         "PeptideShaker validation requires the use of a taget-decoy database.\n"
                         + "Some features will be limited if using other types of databases. See\n"
                         + "the PeptideShaker home page for details.",
                         "No Decoys Found",
                         JOptionPane.INFORMATION_MESSAGE);
             }
 
             waitingDialog.resetSecondaryProgressBar();
             waitingDialog.setSecondaryProgressDialogIntermediate(true);
 
             if (2 * sequenceFactory.getNTargetSequences() < sequenceFactory.getnCache() && !testing) {
                 waitingDialog.appendReport("Creating peptide to protein map.");
                 String sequence;
                 Enzyme enzyme = searchParameters.getEnzyme();
                 int nMissedCleavages = searchParameters.getnMissedCleavages();
                 int nMin = idFilter.getMinPeptideLength();
                 int nMax = idFilter.getMaxPeptideLength();
                 sequences = new HashMap<String, ArrayList<String>>();
 
                 int numberOfSequences = sequenceFactory.getAccessions().size();
 
                 waitingDialog.setSecondaryProgressDialogIntermediate(false);
                 waitingDialog.setMaxSecondaryProgressValue(numberOfSequences);
 
                 for (String proteinKey : sequenceFactory.getAccessions()) {
 
                     waitingDialog.increaseSecondaryProgressValue();
 
                     sequence = sequenceFactory.getProtein(proteinKey).getSequence();
                     for (String peptide : enzyme.digest(sequence, nMissedCleavages, nMin, nMax)) {
                         if (!sequences.containsKey(peptide)) {
                             sequences.put(peptide, new ArrayList<String>());
                         }
                         sequences.get(peptide).add(proteinKey);
                         if (waitingDialog.isRunCanceled()) {
                             return;
                         }
                     }
                 }
 
                 waitingDialog.setSecondaryProgressDialogIntermediate(true);
             }
             waitingDialog.appendReport("FASTA file import completed.");
             waitingDialog.increaseProgressValue();
         } catch (FileNotFoundException e) {
             waitingDialog.appendReport("File " + fastaFile + " was not found. Please select a different FASTA file.");
             e.printStackTrace();
             waitingDialog.setRunCanceled();
         } catch (IOException e) {
             waitingDialog.appendReport("An error occured while loading " + fastaFile + ".");
             e.printStackTrace();
             waitingDialog.setRunCanceled();
         } catch (IllegalArgumentException e) {
             waitingDialog.appendReport(e.getLocalizedMessage() + "\n" + "Please refer to the troubleshooting section at http://peptide-shaker.googlecode.com.");
             e.printStackTrace();
             waitingDialog.setRunCanceled();
         } catch (ClassNotFoundException e) {
             waitingDialog.appendReport("Serialization issue while processing the FASTA file. Please delete the .fasta.cui file and retry.\nIf the error occurs again please report bug at http://peptide-shaker.googlecode.com.");
             e.printStackTrace();
             waitingDialog.setRunCanceled();
         }
     }
 
     /**
      * Imports spectra from various spectrum files.
      * 
      * @param waitingDialog Dialog displaying feedback to the user
      * @param spectrumFiles The spectrum files
      */
     public void importSpectra(WaitingDialog waitingDialog, ArrayList<File> spectrumFiles) {
 
         String fileName = "";
 
         waitingDialog.appendReport("Importing spectra.");
 
         for (File spectrumFile : spectrumFiles) {
             try {
                 fileName = spectrumFile.getName();
                 waitingDialog.appendReport("Importing " + fileName);
                 waitingDialog.setSecondaryProgressDialogIntermediate(false);
                 waitingDialog.resetSecondaryProgressBar();
                 spectrumFactory.addSpectra(spectrumFile, waitingDialog.getSecondaryProgressBar());
                 waitingDialog.resetSecondaryProgressBar();
                 waitingDialog.increaseProgressValue();
             } catch (Exception e) {
                 waitingDialog.appendReport("Spectrum files import failed when trying to import " + fileName + ".");
                 e.printStackTrace();
             }
         }
         waitingDialog.appendReport("Spectra import completed.");
     }
 
     /**
      * Returns the list of proteins which contain in their sequence the given peptide sequence.
      * 
      * @param peptideSequence   the tested peptide sequence
      * @param waitingDialog     the waiting dialog
      * @return                  a list of corresponding proteins found in the database
      */
     private ArrayList<String> getProteins(String peptideSequence, WaitingDialog waitingDialog) {
         
         ArrayList<String> result = sequences.get(peptideSequence);
         boolean inspectAll = 2 * sequenceFactory.getNTargetSequences() < sequenceFactory.getnCache() && !testing;
 
         if (result == null) {
             result = new ArrayList<String>();
             if (inspectAll) {
                 try {
                     for (String proteinKey : sequenceFactory.getAccessions()) {
                         if (sequenceFactory.getProtein(proteinKey).getSequence().contains(peptideSequence)) {
                             result.add(proteinKey);
                         }
                         if (waitingDialog.isRunCanceled()) {
                             return new ArrayList<String>();
                         }
                     }
                 } catch (IOException e) {
                     waitingDialog.appendReport("An error occured while accessing the FASTA file."
                             + "\nProtein to peptide link will be incomplete. Please restart the analysis.");
                     e.printStackTrace();
                     waitingDialog.setRunCanceled();
                 } catch (IllegalArgumentException e) {
                     waitingDialog.appendReport(e.getLocalizedMessage() + "\n" + "Please refer to the troubleshooting section at http://peptide-shaker.googlecode.com."
                             + "\nProtein to peptide link will be incomplete. Please restart the analysis.");
                     e.printStackTrace();
                     waitingDialog.setRunCanceled();
                 }
                 sequences.put(peptideSequence, result);
             }
         }
         return result;
     }
 
     /**
      * Returns a search-engine independent PTM.
      * @param sePTM             The search engine PTM
      * @param modificationSite  The modified site according to the search engine
      * @param sequence          The sequence of the peptide
      * @param searchParameters  The search parameters used
      * @return the best PTM candidate
      */
     private String getPTM(String sePTM, int modificationSite, String sequence, SearchParameters searchParameters) {
         // If someone has a better idea, would be great.
         PTM psPTM;
         ArrayList<PTM> possiblePTMs;
         if (searchParameters.getModificationProfile().getPeptideShakerNames().contains(sePTM.toLowerCase())) {
             return ptmFactory.getPTM(sePTM).getName();
         } else {
             possiblePTMs = new ArrayList<PTM>();
             String[] parsedName = sePTM.split("@");
             double seMass = new Double(parsedName[0]);
             for (String ptmName : searchParameters.getModificationProfile().getPeptideShakerNames()) {
                 psPTM = ptmFactory.getPTM(ptmName);
                 if (Math.abs(psPTM.getMass() - seMass) < 0.01) {
                     possiblePTMs.add(psPTM);
                 }
             }
             if (possiblePTMs.size() == 1) {
                 // Single match for this mass, we are lucky
                 return possiblePTMs.get(0).getName();
             } else if (possiblePTMs.size() > 1) {
                 // More matches, let's see if we can infer something from the position
                 if (modificationSite == 1) {
                     // See if it can be an N-term modification
                     for (PTM possPtm : possiblePTMs) {
                         if (possPtm.getType() == PTM.MODN
                                 || possPtm.getType() == PTM.MODNP) {
                             return possPtm.getName();
                         } else if (possPtm.getType() == PTM.MODAA
                                 || possPtm.getType() == PTM.MODNAA
                                 || possPtm.getType() == PTM.MODNPAA) {
                             for (String aa : possPtm.getResidues()) {
                                 if (sequence.startsWith(aa)) {
                                     return possPtm.getName();
                                 }
                             }
                         }
                     }
                 } else if (modificationSite == sequence.length()) {
                     // See if it can be a C-term modification
                     for (PTM possPtm : possiblePTMs) {
                         if (possPtm.getType() == PTM.MODC
                                 || possPtm.getType() == PTM.MODCP) {
                             return possPtm.getName();
                         } else if (possPtm.getType() == PTM.MODAA
                                 || possPtm.getType() == PTM.MODCAA
                                 || possPtm.getType() == PTM.MODCPAA) {
                             for (String aa : possPtm.getResidues()) {
                                 if (sequence.endsWith(aa)) {
                                     return possPtm.getName();
                                 }
                             }
                         }
                     }
                 } else {
                     for (PTM possPtm : possiblePTMs) {
                         if (possPtm.getType() == PTM.MODAA) {
                             if (modificationSite > 0 && modificationSite <= sequence.length()) {
                                 for (String aa : possPtm.getResidues()) {
                                     if (aa.equals(sequence.charAt(modificationSite - 1) + "")) {
                                         return possPtm.getName();
                                     }
                                 }
                             } else {
                                 int xtandemImportError = modificationSite;
                             }
                         }
                     }
                 }
             }
             return ptmFactory.getPTM(seMass, parsedName[1], sequence).getName();
         }
     }
 
     /**
      * Worker which loads identification from a file and processes them while giving feedback to the user.
      */
     private class IdProcessorFromFile extends SwingWorker {
 
         /**
          * The identification file reader factory of compomics utilities
          */
         private IdfileReaderFactory readerFactory = IdfileReaderFactory.getInstance();
         /**
          * The list of identification files
          */
         private ArrayList<File> idFiles;
         /**
          * The fasta file
          */
         private File fastaFile;
         /**
          * A list of spectrum files (can be empty, no spectrum will be imported)
          */
         private HashMap<String, File> spectrumFiles;
         /**
          * The identification filter.
          */
         private IdFilter idFilter;
         /**
          * The search parameters
          */
         private SearchParameters searchParameters;
         /**
          * The annotation preferences to use for PTM scoring
          */
         private AnnotationPreferences annotationPreferences;
 
         /**
          * Constructor of the worker
          * @param idFiles ArrayList containing the identification files
          */
         public IdProcessorFromFile(ArrayList<File> idFiles, ArrayList<File> spectrumFiles, File fastaFile, IdFilter idFilter, SearchParameters searchParameters, AnnotationPreferences annotationPreferences) {
 
             this.idFiles = new ArrayList<File>();
             HashMap<String, File> filesMap = new HashMap<String, File>();
             for (File file : idFiles) {
                 filesMap.put(file.getName(), file);
             }
             ArrayList<String> names = new ArrayList<String>(filesMap.keySet());
             Collections.sort(names);
             for (String name : names) {
                 this.idFiles.add(filesMap.get(name));
             }
             this.spectrumFiles = new HashMap<String, File>();
             this.fastaFile = fastaFile;
             this.idFilter = idFilter;
             this.searchParameters = searchParameters;
             this.annotationPreferences = annotationPreferences;
 
             for (File file : spectrumFiles) {
                 this.spectrumFiles.put(file.getName(), file);
             }
 
             try {
                 ptmFactory.importModifications(new File(MODIFICATION_FILE));
             } catch (Exception e) {
                 waitingDialog.appendReport("Failed importing modifications from " + MODIFICATION_FILE);
                 waitingDialog.setRunCanceled();
                 e.printStackTrace();
             }
 
             try {
                 ptmFactory.importModifications(new File(USER_MODIFICATION_FILE));
             } catch (Exception e) {
                 waitingDialog.appendReport("Failed importing modifications from " + USER_MODIFICATION_FILE);
                 waitingDialog.setRunCanceled();
                 e.printStackTrace();
             }
         }
 
         @Override
         protected Object doInBackground() throws Exception {
 
             int nTotal = 0;
             int nRetained = 0;
 
             Identification identification = proteomicAnalysis.getIdentification(IdentificationMethod.MS2_IDENTIFICATION);
             importSequences(waitingDialog, proteomicAnalysis, fastaFile, idFilter, searchParameters);
 
             try {
 
                 PeptideShaker.setPeptideShakerPTMs(searchParameters);
                 waitingDialog.appendReport("Reading identification files.");
                 InputMap inputMap = new InputMap();
 
                 ArrayList<String> mgfNeeded = new ArrayList<String>();
 
                 for (File idFile : idFiles) {
 
                     waitingDialog.appendReport("Reading file: " + idFile.getName());
 
                     int searchEngine = readerFactory.getSearchEngine(idFile);
                     IdfileReader fileReader = readerFactory.getFileReader(idFile);
                     HashSet<SpectrumMatch> tempSet = fileReader.getAllSpectrumMatches();
 
                     Iterator<SpectrumMatch> matchIt = tempSet.iterator();
 
                     int numberOfMatches = tempSet.size();
                     waitingDialog.setSecondaryProgressDialogIntermediate(false);
                     waitingDialog.setMaxSecondaryProgressValue(numberOfMatches);
 
                     while (matchIt.hasNext()) {
 
                         waitingDialog.increaseSecondaryProgressValue();
 
                         SpectrumMatch match = matchIt.next();
                         nTotal++;
 
                         PeptideAssumption firstHit = match.getFirstHit(searchEngine);
 
                         if (!idFilter.validateId(firstHit)) {
                             matchIt.remove();
                         } else {
                             Peptide peptide;
                             // use search engine independant PTMs
                             for (PeptideAssumption assumptions : match.getAllAssumptions()) {
                                 peptide = assumptions.getPeptide();
                                 for (ModificationMatch seMod : peptide.getModificationMatches()) {
                                     seMod.setTheoreticPtm(getPTM(seMod.getTheoreticPtm(), seMod.getModificationSite(), peptide.getSequence(), searchParameters));
                                 }
                             }
 
                             inputMap.addEntry(searchEngine, firstHit.getEValue(), firstHit.isDecoy());
                             peptide = firstHit.getPeptide();
                             ArrayList<String> proteins = getProteins(peptide.getSequence(), waitingDialog);
                             if (!proteins.isEmpty()) {
                                 peptide.setParentProteins(proteins);
                             }
 
                             identification.addSpectrumMatch(match);
 
                             String mgfName = Spectrum.getSpectrumFile(match.getKey());
                             if (!mgfNeeded.contains(mgfName)) {
                                 mgfNeeded.add(mgfName);
                             }
 
                             nRetained++;
                         }
 
                         if (waitingDialog.isRunCanceled()) {
                             return 1;
                         }
                     }
 
                     waitingDialog.setSecondaryProgressDialogIntermediate(true);
                     waitingDialog.increaseProgressValue();
                 }
 
                 // clear the sequence to protein map as it is no longer needed
                 sequences.clear();
 
                 if (nRetained == 0) {
                     waitingDialog.appendReport("No identifications retained.");
                     waitingDialog.setRunFinished();
                     return 1;
                 }
 
                 waitingDialog.appendReport("Identification file(s) import completed. "
                         + nTotal + " identifications imported, " + nRetained + " identifications retained.");
 
                 ArrayList<String> mgfMissing = new ArrayList<String>();
                 ArrayList<String> mgfNames = new ArrayList<String>(spectrumFiles.keySet());
                 ArrayList<File> mgfImported = new ArrayList<File>();
 
                 for (String mgfFile : mgfNeeded) {
                     if (!mgfNames.contains(mgfFile)) {
                         mgfMissing.add(mgfFile);
                     } else {
                         mgfImported.add(spectrumFiles.get(mgfFile));
                     }
                 }
 
                 if (mgfMissing.isEmpty()) {
                     for (File file : mgfImported) {
                         searchParameters.addSpectrumFile(file.getAbsolutePath());
                     }
                     waitingDialog.increaseProgressValue(mgfNames.size() - mgfImported.size());
                     importSpectra(waitingDialog, mgfImported);
                 } else {
                    waitingDialog.increaseProgressValue(mgfNames.size());
                 }
 
                 peptideShaker.processIdentifications(inputMap, waitingDialog, searchParameters, annotationPreferences);
 
             } catch (Exception e) {
                 waitingDialog.appendReport("An error occured while loading the identification files:");
                 waitingDialog.appendReport(e.getLocalizedMessage());
                 waitingDialog.setRunCanceled();
                 e.printStackTrace();
             } catch (OutOfMemoryError error) {
                 System.out.println("Ran out of memory!");
                 Runtime.getRuntime().gc();
                 waitingDialog.appendReportEndLine();
                 waitingDialog.appendReport("Ran out of memory!");
                 waitingDialog.setRunCanceled();
                 JOptionPane.showMessageDialog(null,
                         "The task used up all the available memory and had to be stopped.\n"
                         + "Memory boundaries are set in ../conf/JavaOptions.txt.",
                         "Out Of Memory Error",
                         JOptionPane.ERROR_MESSAGE);
                 error.printStackTrace();
             }
 
             return 0;
         }
     }
 
     /**
      * Worker which loads spectra from files assuming that ids are already loaded them while giving feedback to the user.
      */
     private class SpectrumProcessor extends SwingWorker {
 
         /**
          * A list of spectrum files (can be empty, no spectrum will be imported)
          */
         private HashMap<String, File> spectrumFiles;
 
         /**
          * Constructor of the worker
          * @param spectrumFiles ArrayList containing the spectrum files
          */
         public SpectrumProcessor(ArrayList<File> spectrumFiles) {
 
             this.spectrumFiles = new HashMap<String, File>();
 
             for (File file : spectrumFiles) {
                 this.spectrumFiles.put(file.getName(), file);
             }
 
             try {
                 ptmFactory.importModifications(new File(MODIFICATION_FILE));
             } catch (Exception e) {
                 waitingDialog.appendReport("Failed importing modifications from " + MODIFICATION_FILE);
                 waitingDialog.setRunCanceled();
                 e.printStackTrace();
             }
 
             try {
                 ptmFactory.importModifications(new File(USER_MODIFICATION_FILE));
             } catch (Exception e) {
                 waitingDialog.appendReport("Failed importing modifications from " + USER_MODIFICATION_FILE);
                 waitingDialog.setRunCanceled();
                 e.printStackTrace();
             }
         }
 
         @Override
         protected Object doInBackground() throws Exception {
 
             Identification identification = proteomicAnalysis.getIdentification(IdentificationMethod.MS2_IDENTIFICATION);
 
             try {
                 ArrayList<String> mgfNeeded = new ArrayList<String>();
                 String newFile;
 
                 for (String spectrumKey : identification.getSpectrumIdentification()) {
                     newFile = Spectrum.getSpectrumFile(spectrumKey);
                     if (!mgfNeeded.contains(newFile)) {
                         mgfNeeded.add(newFile);
                     }
                 }
 
                 waitingDialog.increaseProgressValue();
 
                 ArrayList<String> mgfMissing = new ArrayList<String>();
                 ArrayList<String> mgfNames = new ArrayList<String>(spectrumFiles.keySet());
                 ArrayList<File> mgfImported = new ArrayList<File>();
 
                 for (String mgfFile : mgfNeeded) {
                     if (!mgfNames.contains(mgfFile)) {
                         mgfMissing.add(mgfFile);
                     } else {
                         mgfImported.add(spectrumFiles.get(mgfFile));
                     }
                 }
 
                 if (mgfMissing.isEmpty()) {
                     for (int i = mgfImported.size(); i < mgfNames.size(); i++) {
                         waitingDialog.increaseProgressValue();
                     }
                     importSpectra(waitingDialog, mgfImported);
                 } else {
                     for (int i = 0; i < mgfNames.size(); i++) {
                         waitingDialog.increaseProgressValue();
                     }
                 }
 
                 waitingDialog.appendReport("File import finished.\n\n");
                 waitingDialog.setRunFinished();
 
             } catch (Exception e) {
                 waitingDialog.appendReport("An error occured while importing spectra:");
                 waitingDialog.appendReport(e.getLocalizedMessage());
                 waitingDialog.setRunCanceled();
                 e.printStackTrace();
             } catch (OutOfMemoryError error) {
                 Runtime.getRuntime().gc();
                 waitingDialog.appendReportEndLine();
                 waitingDialog.appendReport("Ran out of memory!");
                 waitingDialog.setRunCanceled();
                 JOptionPane.showMessageDialog(null,
                         "The task used up all the available memory and had to be stopped.\n"
                         + "Memory boundaries are set in ../conf/JavaOptions.txt.",
                         "Out Of Memory Error",
                         JOptionPane.ERROR_MESSAGE);
 
                 System.out.println("Ran out of memory!");
                 error.printStackTrace();
             }
 
             return 0;
         }
     }
 }
