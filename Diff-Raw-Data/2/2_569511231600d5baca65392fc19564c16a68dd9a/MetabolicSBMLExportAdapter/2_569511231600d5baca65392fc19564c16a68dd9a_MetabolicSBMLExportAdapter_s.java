 package uk.ac.ed.inf.Metabolic.sbmlexport;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.sbml.libsbml.Model;
 import org.sbml.libsbml.SBMLDocument;
 import org.sbml.libsbml.SBMLError;
 import org.sbml.libsbml.libsbml;
 import org.sbml.libsbml.libsbmlConstants;
 
 import uk.ac.ed.inf.Metabolic.ExportAdapterCreationException;
 import uk.ac.ed.inf.Metabolic.IExportAdapter;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IModel;
 
 class MetabolicSBMLExportAdapter<N extends IModel> implements IExportAdapter<N> {
 	static boolean isLibraryLoaded = false;
 	static {  
 		
 	   isLibraryLoaded = LibSBMLLoader.getInstance().loadLibrary();
 	
 	}
 	
 	final long SPATIAL_DIMENSIONS = 3;
 	private boolean isTargetCreated = false;
 	
 	//package protected to allow access from tests
 	SBMLDocument document;
 	
 	IEntityFactory entityFactory     = new EntityBuilder();
 	IModelFactory modelFactory       = new ModelFactory();
 	IReactionBuilder reactionFactory = new SBMLReactionFactory();;
 	
 
 	public void createTarget(IModel model) throws ExportAdapterCreationException {
 		isTargetCreated = false; //reset
 		if (model == null) {
 			throw new IllegalArgumentException("model is null");
 		}
		if(!isLibraryLoaded) {
 			throw new ExportAdapterCreationException("Could not load libSBML - only supported on Windows at present");
 		}
 		// concession to testing
 		if(document ==null) 
 		   document = new SBMLDocument();
 		Model sbmlModel = modelFactory.createSBMLModel(document, model);
 	    entityFactory.buildSpeciesAndCompartments(sbmlModel, model);
 		reactionFactory.buildReactions(sbmlModel, model);
 		System.out.println(libsbml.writeSBMLToString(document));
 		document.setConsistencyChecks(libsbmlConstants.LIBSBML_CAT_UNITS_CONSISTENCY, false);
 		if(document.checkConsistency() == 0)
 			isTargetCreated = true;
 		else {
 			StringBuffer sb = new StringBuffer();
 			for(int i=0; i< document.getNumErrors();i++) {
 				SBMLError err = document.getError(i);
 				sb.append(err.getMessage()).append("\n");
 			}
 			throw new ExportAdapterCreationException("Invalid SBML document created: \n" + sb.toString());
 		}
 	}
     
 	public boolean isTargetCreated() {
 		return isTargetCreated;
 	}
 
 	public void writeTarget(OutputStream stream) throws IOException {
 		if (stream == null) {
 			throw new IllegalArgumentException("output stream is null");
 		}
 		if (!isTargetCreated()) {
 			throw new IllegalStateException("Target not created");
 		}
 		String s = libsbml.writeSBMLToString(document);
 		stream.write(s.getBytes());
 		
 	}
 	
 
 
 }
