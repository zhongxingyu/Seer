 /*
  * Copyright (c) 2012, Stephan Beisken. All rights reserved.
  *
  * This file is part of BiNChe.
  *
  * BiNChe is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * BiNChe is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with BiNChe. If not, see <http://www.gnu.org/licenses/>.
  */
 package net.sourceforge.metware.binche.execs;
 
 import BiNGO.BingoParameters;
 import BiNGO.methods.BingoAlgorithm;
 import java.awt.*;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.prefs.BackingStoreException;
 import java.util.prefs.Preferences;
 import javax.swing.*;
 import net.sourceforge.metware.binche.BiNChe;
 import net.sourceforge.metware.binche.graph.ChebiGraph;
 import net.sourceforge.metware.binche.graph.SvgWriter;
 import net.sourceforge.metware.binche.gui.SettingsPanel;
 import net.sourceforge.metware.binche.loader.BiNChEOntologyPrefs;
 import net.sourceforge.metware.binche.loader.OfficialChEBIOboLoader;
 import org.apache.commons.cli.Option;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  * @author Stephan Beisken
  */
 public class BiNCheExec extends CommandLineMain {
 
     private static final Logger LOGGER = Logger.getLogger(BiNCheExec.class);
 
     public static void main(String[] args) {
 
         new BiNCheExec(args).process();
     }
 
     public BiNCheExec(String[] args) {
 
         super(args);
     }
 
     @Override
     public void setupOptions() {
 
        add(new Option("g", "true", false, "run graphical user interface"));
         add(new Option("i", "file to load", true, "association file to load"));
         add(new Option("o", "output directory", true, "directory to write output to"));
     }
 
     /**
      * Main processing method
      */
     @Override
     public void process() {
 
         if (!(hasOption("g") || (hasOption("i") && hasOption("o")))) {
             printHelp();
             System.exit(0);
         }
 
         if (hasOption("g")) {
             runGui();
         } else {
             String inputPath = getCommandLine().getOptionValue("i");
             String outputPath = getCommandLine().getOptionValue("o");
 
             runDefault(inputPath, outputPath);
         }
     }
 
     private void runGui() {
 
         final JFrame window = new JFrame("binche Settings");
         final SettingsPanel settingsPanel = new SettingsPanel();
         window.getContentPane().add(settingsPanel);
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         window.setGlassPane(settingsPanel.getProgressPanel());
         window.pack();
 
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         window.setLocation(screenSize.width / 2 - (window.getWidth() / 2),
                 screenSize.height / 2 - (window.getHeight() / 2));
         window.setVisible(true);
         window.setResizable(true);
     }
 
     private void runDefault(String inputPath, String outputPath) {
 
         LOGGER.log(Level.INFO, "############ Start ############");
 
         Preferences binchePrefs = Preferences.userNodeForPackage(BiNChe.class);
         try {
             if (binchePrefs.keys().length == 0) {
                 new OfficialChEBIOboLoader();
             }
         } catch (BackingStoreException e) {
             LOGGER.error("Problems loading preferences", e);
             return;
         } catch (IOException e) {
             LOGGER.error("Problems loading preferences", e);
             return;
         }
 
         //String ontologyFile = getClass().getResource("/BiNGO/data/chebi_clean.obo").getFile();
         String ontologyFile = binchePrefs.get(BiNChEOntologyPrefs.RoleAndStructOntology.name(), null);
         String elementsForEnrichFile = inputPath;
 
         LOGGER.log(Level.INFO, "Setting default parameters ...");
         BingoParameters parametersSaddle = getDefaultParameters(ontologyFile);
 
         BiNChe binche = new BiNChe();
         binche.setParameters(parametersSaddle);
 
         LOGGER.log(Level.INFO, "Reading input file ...");
         try {
             binche.loadDesiredElementsForEnrichmentFromFile(elementsForEnrichFile);
         } catch (IOException exception) {
             LOGGER.log(Level.ERROR, "Error reading file: " + exception.getMessage());
             System.exit(1);
         }
 
         binche.execute();
 
         ChebiGraph chebiGraph =
                 new ChebiGraph(binche.getEnrichedNodes(), binche.getOntology(), binche.getInputNodes());
         //new ChebiGraph(binche.getPValueMap(), binche.getOntology(), binche.getInputNodes());
 
         LOGGER.log(Level.INFO, "Writing out graph ...");
         SvgWriter writer = new SvgWriter();
 
         writer.writeSvg(chebiGraph.getVisualisationServer(), outputPath);
 
         LOGGER.log(Level.INFO, "############ Stop ############");
     }
 
     /**
      * This should be set through the parameters factory. This should be removed.
      *
      * @param ontologyFile
      * @return
      */
     private BingoParameters getDefaultParameters(String ontologyFile) {
 
         BingoParameters parametersSaddle = new BingoParameters();
 
         parametersSaddle.setTest(BingoAlgorithm.SADDLESUM);
         parametersSaddle.setCorrection(BingoAlgorithm.NONE);
         parametersSaddle.setOntologyFile(ontologyFile);
         parametersSaddle.setOntology_default(false);
         parametersSaddle.setNameSpace("chebi_ontology");
         parametersSaddle.setOverOrUnder("Overrepresentation");
         parametersSaddle.setSignificance(new BigDecimal(0.05));
         parametersSaddle.setCategory(BingoAlgorithm.CATEGORY_CORRECTION);
         parametersSaddle.setReferenceSet(BingoAlgorithm.GENOME);
         parametersSaddle.setAllNodes(null);
         parametersSaddle.setSelectedNodes(null);
 
         return parametersSaddle;
     }
 }
