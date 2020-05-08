 /*******************************************************************************
  * Copyright (c) 2009  Miguel Rojas <miguelrojasch@users.sf.net>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
  *
  * Contact: http://www.bioclipse.net/
  ******************************************************************************/
 package net.bioclipse.reaction.business;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.bioclipse.cdk.business.CDKManagerHelper;
 import net.bioclipse.core.ResourcePathTransformer;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.domain.IReactionScheme;
 import net.bioclipse.managers.business.IBioclipseManager;
 import net.bioclipse.reaction.domain.CDKReaction;
 import net.bioclipse.reaction.domain.CDKReactionScheme;
 import net.bioclipse.reaction.domain.ICDKReaction;
 import net.bioclipse.reaction.domain.ICDKReactionScheme;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.content.IContentType;
 import org.openscience.cdk.ChemFile;
 import org.openscience.cdk.ChemModel;
 import org.openscience.cdk.ReactionSet;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IChemFile;
 import org.openscience.cdk.interfaces.IChemModel;
 import org.openscience.cdk.interfaces.IChemSequence;
 import org.openscience.cdk.interfaces.IReactionSet;
 import org.openscience.cdk.io.CMLReader;
 import org.openscience.cdk.io.CMLWriter;
 import org.openscience.cdk.io.FormatFactory;
 import org.openscience.cdk.io.IChemObjectWriter;
 import org.openscience.cdk.io.ISimpleChemObjectReader;
 import org.openscience.cdk.io.MDLRXNReader;
 import org.openscience.cdk.io.ReaderFactory;
 import org.openscience.cdk.io.WriterFactory;
 import org.openscience.cdk.io.formats.CMLFormat;
 import org.openscience.cdk.io.formats.IChemFormat;
 import org.openscience.cdk.io.formats.IResourceFormat;
 import org.openscience.cdk.io.formats.MDLRXNFormat;
 import org.openscience.cdk.nonotify.NNChemFile;
 import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
 import org.openscience.cdk.tools.manipulator.ReactionSchemeManipulator;
 
 /**
  * The manager class for CDK. Contains CDK related methods.
  * 
  * @author olas
  *
  */
 public class ReactionManager implements IBioclipseManager {
 	
 	private static final Logger logger = Logger.getLogger(ReactionManager.class);
 
     // ReaderFactory used to instantiate IChemObjectReaders
     private static ReaderFactory readerFactory;
 
     // ReaderFactory used solely to determine chemical file formats
     private static FormatFactory formatsFactory;
 
     private static final WriterFactory writerFactory = new WriterFactory();
 
     static {
         readerFactory = new ReaderFactory();
         CDKManagerHelper.registerSupportedFormats(readerFactory);
         formatsFactory = new FormatFactory();
         CDKManagerHelper.registerAllFormats(formatsFactory);
     }
 	
    public String getNamespace() {
         return "reaction";
     }
     
     public ICDKReactionScheme loadReactionScheme(IFile file, IProgressMonitor monitor)
 	    throws IOException, BioclipseException, CoreException {
 	
     	ICDKReactionScheme loadedReact = loadReactionScheme( file.getContents(),
 		                           determineIChemFormat(file),
 		                           monitor
 		);
     	loadedReact.setResource(file);
 
     	return loadedReact;
 	}
 
     public ICDKReactionScheme loadReactionScheme( InputStream instream,
             IChemFormat format, 
             IProgressMonitor monitor )
 			throws BioclipseException, IOException {
 	
 		if (monitor == null) {
 		monitor = new NullProgressMonitor();
 		}
 		
 		
 		try {
 			// Create the reader
 			ISimpleChemObjectReader reader = readerFactory.createReader(format);
 			if (reader == null)
 				throw new BioclipseException("Could not create reader in CDK.");
 			
 			try {
 				reader.setReader(instream);
 			}catch ( CDKException e1 ) {
 				throw new RuntimeException("Failed to set the reader's inputstream", e1);
 			}
 			
 			// Do some customizations...
 			CDKManagerHelper.customizeReading(reader);
 			
 			IReactionSet reactionSet = new ReactionSet();
 			// Read file
 			try {
 				if (reader.accepts(ChemFile.class)) {
 					IChemFile chemFile = (IChemFile) reader.read(new NNChemFile());
 					List<org.openscience.cdk.interfaces.IReaction> reactionList =  ChemFileManipulator.getAllReactions(chemFile);
 					for(int i = 0; i < reactionList.size(); i++)
 					reactionSet.addReaction(reactionList.get(i));
 			
 			//} else if (reader.accepts(Reaction.class)) {
 			//reactionSet.addListener(
 			//(NNReaction) reader.read(new NNReaction())
 			//);
 			//} else {
 			//throw new RuntimeException("Failed to read file.");
 				}
 			}catch (CDKException e) {
 				throw new RuntimeException("Failed to read file", e);
 			}
 			
 			// Store the chemFormat used for the reader
 			IResourceFormat chemFormat = reader.getFormat();
 			logger.debug("Read CDK chemfile with format: "+ chemFormat.getFormatName());
 			
 			int nuReacts = reactionSet.getReactionCount();
 			logger.debug("This file contained: " + nuReacts + " reactions");
 			
 			org.openscience.cdk.interfaces.IReactionScheme reactionScheme = ReactionSchemeManipulator.createReactionScheme(reactionSet);
 			ICDKReactionScheme retreact = new CDKReactionScheme(reactionScheme);
 			return retreact;
 			
 		}finally {
 			monitor.done();
 		}
 	}
 
     public String calculateSMILES(net.bioclipse.core.domain.IReaction reaction)
 		    throws BioclipseException {
 		return reaction.getSMILES(net.bioclipse.core.domain.IReaction.Property.USE_CACHED_OR_CALCULATED);
 	}
     
 	public IChemFormat determineIChemFormat(IFile file)
 		throws IOException, CoreException {
 		return formatsFactory.guessFormat(
 			new BufferedReader(new InputStreamReader(file.getContents())));
 	}
 
 	public void saveReactionScheme(IReactionScheme reaction)
 	          throws BioclipseException, CDKException, CoreException{
 		saveReaction(reaction, false);
 	}
 
 	public void saveReaction(IReactionScheme reaction, boolean overwrite)
 	          throws BioclipseException, CDKException, CoreException{
 		if (reaction.getResource() == null ||
 	              !(reaction.getResource() instanceof IFile))
 	              throw new BioclipseException("Reaction does not have an associated File." );
 
         IFile file = (IFile)reaction.getResource();
         IContentDescription contentDesc = file.getContentDescription();
         if (contentDesc == null) {
             logger.error("Hej, you are running OS/X! You just encountered a" +
                 "known bug: contentDesc == null for who-knows-what" +
                 "reason... it works on Linux... So, I am guessing from" +
                 "the file name. Blah, yuck...");
             saveReaction(
             		reaction, file,
 	                guessFormatFromExtension(file.getName()),
 	                overwrite
             );
         } else {
         	saveReaction(
             		reaction, file,
 	                determineFormat(contentDesc.getContentType()),
 	                overwrite
             );
         }
 	}
 	
 	public void saveReaction(IReactionScheme reaction, String filename)
 	          throws BioclipseException, CDKException, CoreException{
 		saveReaction(reaction, filename, false);
 	}
 
 	public void saveReaction(IReactionScheme reaction, String filename, boolean overwrite)
 	          throws BioclipseException, CDKException, CoreException{
 		IFile file = ResourcePathTransformer.getInstance().transform(filename);
 		saveReaction(reaction, file, overwrite);
 	}
 
 	public void saveReaction(IReactionScheme reaction, IFile file, boolean overwrite)
 	          throws BioclipseException, CDKException, CoreException{
 		IChemFormat format = null;
 
         // are we really overwriting an old file?
         if (reaction.getResource() != null &&
             (reaction.getResource() instanceof IFile)) {
             IFile oldFile = (IFile)reaction.getResource();
             if (oldFile.getContentDescription() == null) {
                 logger.error("Hej, you are running OS/X! You just encountered " +
                         "a known bug: contentDesc == null for who-knows-what" +
                         "reason... it works on Linux... So, I am guessing" +
                         "from the file name. Blah, yuck...");
             } else {
                 format = determineFormat(
                     oldFile.getContentDescription().getContentType()
                 );
             }
         }
 
         if (overwrite && format == null) {
             format = guessFormatFromExtension(file);
         }
 
         // OK, not overwriting, but unknown format: default to CML
         if (format == null) format = (IChemFormat)CMLFormat.getInstance();
 
         saveReaction(reaction, file, format, overwrite);
 	}
 
 	public void saveReaction( IReactionScheme reaction, 
                           String filename, 
                           IChemFormat filetype )
 	          throws BioclipseException, CDKException, CoreException{
 		
 	}
 	
 	public void saveReaction( IReactionScheme reaction,
             IFile target,
             IChemFormat filetype,
             boolean overwrite)
 		throws BioclipseException, CDKException, CoreException {
 		
 		if ( target.exists() && overwrite == false ) {
 			throw new BioclipseException("File already exists!");
 		}
 		
 		ICDKReactionScheme react = create(reaction);
 		IChemModel chemModel = new ChemModel();
 		chemModel.setReactionSet(ReactionSchemeManipulator.getAllReactions(react.getReactionScheme()));
 		this.save(chemModel, target, filetype, null);
 		
 		react.setResource(target);
 	}
 
 	public void saveReaction( IReactionScheme reaction, 
                           String filename, 
                           IChemFormat filetype, 
                           boolean overwrite )
 	          throws BioclipseException, CDKException, CoreException{
 		
 	}
 	
 	public void save(IChemModel model, String target, IChemFormat filetype)
 		    throws BioclipseException, CDKException, CoreException {
 		save( model,ResourcePathTransformer.getInstance().transform(target),filetype, null );
 	}
 
 	public void save( IChemModel model,
 	          IFile target,
 	          IChemFormat filetype,
 	          IProgressMonitor monitor )
 	    throws BioclipseException, CDKException, CoreException {
 	
 		if (monitor == null)
 			monitor = new NullProgressMonitor();
 		
 		if (filetype == null) 
 			filetype = (IChemFormat)CMLFormat.getInstance();
 		
 		try {
 			int ticks = 10000;
 			monitor.beginTask("Writing file", ticks);
 			StringWriter writer = new StringWriter();
 			
 			writerFactory.registerWriter(CMLWriter.class);
 			IChemObjectWriter chemWriter = writerFactory.createWriter(filetype);
 			if (chemWriter == null) {
 			    throw new BioclipseException(
 			        "No writer available for this format: " +
 			        filetype.getFormatName());
 			}
 			chemWriter.setWriter(writer);
 			
 			if (chemWriter.accepts(ChemModel.class)) {
 			  chemWriter.write(model);
 			} else if (chemWriter.accepts(ReactionSet.class)){
 				chemWriter.write(model.getReactionSet());
 			} else {
 			  throw new BioclipseException("Writer does not support writing" +
 			          "IChemModel or IReactionSet.");
 			}
 			
 			chemWriter.close();
 			
 			if (target.exists()) {
 			    target.setContents(
 			            new ByteArrayInputStream(writer.toString()
 			                    .getBytes("US-ASCII")),
 			                    false,
 			                    true, // overwrite
 			                    monitor );
 			} else {
 			    target.create(
 			            new ByteArrayInputStream(writer.toString()
 			                    .getBytes("US-ASCII")),
 			                    false,
 			                    monitor );
 			}
 			monitor.worked(ticks);
 			} catch (IOException exception) {
 			throw new BioclipseException("Failed to write file: " +
 			      exception.getMessage());
 		} finally {
 			monitor.done();
 		}
 	}
 	
     /**
      * Create an ICDKReactionScheme from an IReaction. First tries to create
      * ICDKReactionScheme from CML. 
      */
     public ICDKReactionScheme create(IReactionScheme ireact) throws BioclipseException {
 
         if (ireact instanceof ICDKReactionScheme) {
             return (ICDKReactionScheme) ireact;
         }
 
         // First try to create from CML
         try {
             String cmlString = ireact.getCML();
             if (cmlString != null) {
                 return fromCml(cmlString);
             }
         }
         catch (IOException e) {
             logger.debug("Could not create react scheme from CML");
         }
         catch (UnsupportedOperationException e) {
             logger.debug("Could not create react scheme from CML");
         }
 
         return null;
     }
 
     /**
      * Create reaction scheme from SMILES.
      *
      * @throws BioclipseException
      */
     public ICDKReactionScheme fromSMILES(String smilesDescription)
                         throws BioclipseException {
 
 //        SmilesParser parser
 //            = new SmilesParser( DefaultChemObjectBuilder.getInstance() );
 //
 //        try {
 //            org.openscience.cdk.interfaces.IReaction react
 //                = parser.parseSmiles(smilesDescription);
 //            return new ICDKReactionScheme(mol);
 //        }
 //        catch (InvalidSmilesException e) {
 //            throw new BioclipseException("SMILES string is invalid", e);
 //        }
     	return null;
     }
 
     /**
      * Create molecule from String
      *
      * @throws BioclipseException
      * @throws IOException
      */
     public ICDKReactionScheme fromCml(String molstring)
                         throws BioclipseException, IOException {
 
         if (molstring == null)
             throw new IllegalArgumentException("Input cannot be null");
 
         ByteArrayInputStream bais
             = new ByteArrayInputStream( molstring.getBytes() );
 
         return loadReactionScheme( (InputStream)bais,
                              (IChemFormat)CMLFormat.getInstance(),
                              new NullProgressMonitor() );
     }
 
 
     private IChemFormat guessFormatFromExtension(IFile file) {
         return guessFormatFromExtension(file.getName());
     }
     
 	public IChemFormat guessFormatFromExtension(String file) {
         if (file.endsWith(".rxn")) {
             return (IChemFormat)MDLRXNFormat.getInstance();
         }else if(file.endsWith(".cml")){
             return (IChemFormat)MDLRXNFormat.getInstance();
         }
         return null;
     }
 	
 	public IChemFormat determineFormat(IContentType type) {
         if (type == null) return null;
 
         // first try a quick exact match ...
         if (contentTypeMap.containsKey(type.getId()))
             return contentTypeMap.get(type.getId());
         // ... then as prefix
         for (String prefix : contentTypeMap.keySet()) {
             if (type.getId().startsWith(prefix)) {
                 return contentTypeMap.get(prefix);
             }
         }
 
         // OK, no clue...
         return null;
     }
 	
 	public String determineFormat( String path ) throws IOException,CoreException {
 		IChemFormat format = determineIChemFormat(
 				ResourcePathTransformer.getInstance().transform(path)
 		);
 		return format == null ? "Unknown" : format.getFormatName();
 	}
 	
 	private final static Map<String, IChemFormat> contentTypeMap = new HashMap<String, IChemFormat>();
 
     static {
         contentTypeMap.put(
                 "net.bioclipse.contenttypes.cml",
                 (IChemFormat)CMLFormat.getInstance());
         contentTypeMap.put(
                 "net.bioclipse.contenttypes.rxnFile",
                 (IChemFormat)MDLRXNFormat.getInstance());
     }
 
     
     public List<ICDKReaction> loadReactions( IFile file, IProgressMonitor monitor )
     	throws IOException,
 		  BioclipseException,
 		  CDKException,
 		  CoreException {
 
     	List<ICDKReaction> loadedMol = loadReactions(
 				file.getContents(), determineIChemFormat( file ), monitor
 			);
 		
 		return loadedMol;
 	}
 		
 	public List<ICDKReaction> loadReactions( InputStream instream,
 	                        IChemFormat format,
 	                        IProgressMonitor monitor)
 	                                      throws BioclipseException,
 	                                      CDKException,
 		                                      IOException {
 		if ( monitor == null ) {
 			monitor = new NullProgressMonitor();
 		}
 		
 		IChemModel model;
 		if ( format instanceof CMLFormat ) {
 			CMLReader reader = new CMLReader( instream );
 			IChemFile chemFile =
 			      (IChemFile) reader
 			              .read( new org.openscience.cdk.ChemFile() );
 			IChemSequence seq = chemFile.getChemSequence( 0 );
 			model = seq.getChemModel( 0 );
 		} else if ( format instanceof MDLRXNFormat ) {
 			MDLRXNReader reader = new MDLRXNReader( instream );
 			IChemFile chemFile =
 			      (IChemFile) reader
 			              .read( new org.openscience.cdk.ChemFile() );
 			IChemSequence seq = chemFile.getChemSequence( 0 );
 			model = seq.getChemModel( 0 );
 		} else {
 			throw new BioclipseException( "Invalid format" );
 		}
 		List<ICDKReaction> reactions = new ArrayList<ICDKReaction>();
 		for(int i=0;i<model.getReactionSet().getReactionCount();i++){
 			reactions.add( new CDKReaction(model.getReactionSet().getReaction( i )) );
 		}
 		return reactions;
 	}
 }
