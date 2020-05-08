 /* *****************************************************************************
  * Copyright (c) 2009 Ola Spjuth.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ola Spjuth - initial API and implementation
  ******************************************************************************/
 package net.bioclipse.balloon.business;
 
 import java.util.List;
 
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.core.PublishedClass;
 import net.bioclipse.core.PublishedMethod;
 import net.bioclipse.core.Recorded;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.domain.IMolecule;
 import net.bioclipse.jobs.BioclipseUIJob;
 import net.bioclipse.managers.business.IBioclipseManager;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 
 @PublishedClass( "Balloon is a software that creates 3D atomic coordinates. " + 
                  "This manager provides methods for generating 3D " +
                  "coordinates using Balloon in a few different ways. More " +
                  "information can be found at: " +
                  "http://web.abo.fi/~mivainio/balloon/index.php")
 /**
  * An interface for a Bioclipse Manager for invoking Balloon
  * (http://web.abo.fi/~mivainio/balloon/index.php)
  * 
  * @author ola
  *
  */
 public interface IBalloonManager extends IBioclipseManager {
 
     /**
      * Generate 3D coordinates for an inputfile. If a multimol file (e.g. SDF)
      * then an SDF will be generated back.
      * @param inputfile Path to inputfile as String
      * @return
      * @throws BioclipseException 
      */
     @Recorded
     @PublishedMethod(
         params = "String inputfile",
         methodSummary = "Generates 3D coodrinates for all molecules in the " +
         		            "inputfile. Creates a default output file with _3d " +
         		            "added.")
     String generate3Dcoordinates( String inputfile ) throws BioclipseException;
 
     /**
      * Generate 3D coordinates for an inputfile to a dedicated outputfile
      * If a multimol file (e.g. SDF)
      * then an SDF will be generated back.
      * @param inputfile Path to inputfile as String
      * @param outputfile Path to inputfile as String
      * @return
      * @throws BioclipseException 
      */
     @PublishedMethod(
         params = "String inputfile, String outputfile",
         methodSummary = "Generates 3D coordinates for all molecules in the " +
         		            "inputfile and saves to outputfile")
     @Recorded
     String generate3Dcoordinates( String inputfile, String outputfile ) 
            throws BioclipseException;
 
     /**
      * Generate 3D conformations for one or more inputfiles in a list.
      * @param inputfiles List of paths to the inputfiles.
      * @return
      * @throws BioclipseException 
      */
     @PublishedMethod(
         params = "List<String> inputfiles",
         methodSummary = "Generates 3D coordinates for all molecules in all " +
        		            "files in inputFiles and stores them to a default " +
        		            "output file." )
     @Recorded
     List<String> generate3Dcoordinates( List<String> inputFiles) 
                  throws BioclipseException;
 
     /**
     * Generate 3D conformations for one or more inputfiles in a list.
      * @param inputfiles List of paths to the inputfiles.
      * @param numConformations Number of conformations to generate per molecule
     * @return
      * @throws BioclipseException 
      */
     @PublishedMethod(
         params = "List<String> inputfiles, int numConformations",
         methodSummary = "Generate numConformations number of 3D " +
         		            "conformations for each molecule in all files in " +
        		            "inputFiles and return them in a List.")
     @Recorded
     List<String> generate3Dconformations( List<String> inputfiles, 
                                           int numConformations) 
                  throws BioclipseException;
 
     /**
      * Generate 3D conformations for an inputfile
      * @param inputfile Paths to the inputfile
      * @param numConformations Number of conformations to generate per molecule
      * @return
      * @throws BioclipseException 
      */
     @PublishedMethod(
         params = "String inputfile, int numConformations",
         methodSummary = "Generates 3D conformations for all molecules in " +
         		            "the file inputfile and stores them in a file and " +
         		            "returns the name of the created file." )
     @Recorded
     public String generate3Dconformations( String inputfile, int numConformations ) 
            throws BioclipseException;
 
     /**
      * Generate 3D conformations for an inputfile to a dedicated outputfile
      * If a multimol file (e.g. SDF)
      * then an SDF will be generated back.
      * @param inputfile Path to inputfile as String
      * @param numConformations Number of conformations to generate per molecule
      * @param outputfile Path to inputfile as String
      * @return
      * @throws BioclipseException 
      */
     @PublishedMethod(
         params = "String inputfile, String outputfile, int numConformations",
         methodSummary = "Generates 3D conformations for all molecules " +
                         "in the file inputfile and stores them in a file " +
                         "and returns the name of the created file."  )
     @Recorded
     public String generate3Dconformations( String inputfile, 
                                            String outputfile, 
                                            int numConformations ) 
            throws BioclipseException;
     
  
     @PublishedMethod(
                      params = "IMolecule molecule",
                      methodSummary = "Returns a new molecule with 3D " +
                      		"coordinates generated by Balloon.")
     @Recorded
     public ICDKMolecule generate3Dcoordinates( IMolecule molecule ) 
     throws BioclipseException;
 
     @PublishedMethod(
                      params = "IMolecule molecule, int numConf",
                      methodSummary = "Returns a list of molecules with " +
                      		         "numConf number of 3D confirmations " +
                      		         "generated by Balloon for the given " +
                      		         "molecule.")
     @Recorded
     public List<ICDKMolecule> generate3Dconformations( IMolecule molecule, int numConf) 
     throws BioclipseException;
  
     @PublishedMethod(
                      params = "List<IMolecule> molecules",
                      methodSummary = "Returns a new list of molecules with " +
                      		         "3D coordinates for the given list of " +
                      		         "molecules generated by Balloon.")
     @Recorded
     public List<ICDKMolecule> generateMultiple3Dcoordinates( 
                                                      List<IMolecule> molecules ) 
                                                      throws BioclipseException;
     
     public List<ICDKMolecule> generateMultiple3Dcoordinates( 
     												List<IMolecule> molecules, 
     												IProgressMonitor monitor ) 
     												throws BioclipseException;
 
     
     @PublishedMethod(
         params = "List<IMolecule> molecules, int numConf",
         methodSummary = "Returns a new list of molecules with numConf " +
         		            "number of 3D conformations generated by Balloon " +
         		            "for the molecules in the given list.")
     @Recorded
     public List<ICDKMolecule> generateMultiple3Dconformations( 
                                                       List<IMolecule> molecules, 
                                                       int numConf) 
                                                       throws BioclipseException;
 
     public List<ICDKMolecule> generateMultiple3Dconformations( 
     												   List<IMolecule> molecules, 
     												   int numConf,
     												   IProgressMonitor monitor) 
     												   throws BioclipseException;
 
     public void generate3Dcoordinates( IFile input, BioclipseUIJob<IFile> uiJob );
 }
