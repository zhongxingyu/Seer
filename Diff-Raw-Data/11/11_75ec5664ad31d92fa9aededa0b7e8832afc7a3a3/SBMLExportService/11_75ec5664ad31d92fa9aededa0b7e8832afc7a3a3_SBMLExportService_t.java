 package uk.ac.ed.inf.Metabolic.sbmlexport;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.pathwayeditor.businessobjectsAPI.IMap;
 import org.pathwayeditor.contextadapter.publicapi.ExportServiceException;
 import org.pathwayeditor.contextadapter.publicapi.IContext;
 import org.pathwayeditor.contextadapter.publicapi.IContextAdapterExportService;
 import org.pathwayeditor.contextadapter.publicapi.IContextAdapterServiceProvider;
 import org.pathwayeditor.contextadapter.publicapi.IContextAdapterValidationService;
 import org.pathwayeditor.contextadapter.publicapi.IValidationReport;
 
 import uk.ac.ed.inf.Metabolic.ExportAdapterCreationException;
 import uk.ac.ed.inf.Metabolic.IExportAdapter;
 import uk.ac.ed.inf.Metabolic.MetabolicNDOMValidationService;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IModel;
 
 public class SBMLExportService implements IContextAdapterExportService {
 
 	String DISPLAY_NAME = "SBML export L2v3";
 
 	String RECOMMENDED_SUFFIX = "sbml";
 
 	final String TYPECODE = "MetSBML_1.0.0";
 	private IContext context;
 	private IExportAdapter<IModel> generator;
 
 	public SBMLExportService(IContextAdapterServiceProvider provider) {
 		this.serviceProvider = provider;
 		context = provider.getContext();
 	}
 
 	private IContextAdapterServiceProvider serviceProvider;
 
 	public IContextAdapterServiceProvider getServiceProvider() {
 		return serviceProvider;
 	}
 
 	/**
 	 * @throws IllegalArgumentException
 	 *             if either argument is null
 	 * @throws ExportServiceException
 	 *             if exportFile:
 	 *             <ul>
 	 *             <li> Doesn't exist
 	 *             <li> Is not writable.
 	 *             <li> Cannot produce valid SBML
 	 *             </ul>
 	 */
	public void exportMap(IMap map, File exportFile) throws ExportServiceException {
		if(map == null || exportFile == null){
			throw new IllegalArgumentException("parameters map or exportFile canot be null");
		}
 		FileOutputStream fos = null;
 		try {
 			checkArgs(map, exportFile);
 
 			generator = getGenerator();//new MetabolicSBMLExportAdapter<IModel>();
 
 			IContextAdapterValidationService validator =  serviceProvider
 					.getValidationService();
 			validator.setMapToValidate(map);
 			IModel ndom = null;
 			if (validator.isReadyToValidate()) {
 				validator.validateMap();
 				IValidationReport report =validator.getValidationReport();
 				if(!report.isMapValid()){
 					String sb="Map is not valid:\n";
 					
 					throw new ExportServiceException(sb, report);
 				}else {
 					ndom=getModel(validator);
 				
 			
 				generator.createTarget(ndom);
 				
 				fos = new FileOutputStream(exportFile);
 				generator.writeTarget(fos);
 				}
 			}
 		} catch (ExportAdapterCreationException e) {
 			throw new ExportServiceException(e);
 		} catch (IOException e) {
 			throw new ExportServiceException(e);
		} catch (UnsatisfiedLinkError e){
			throw new ExportServiceException(e);
		} catch (RuntimeException e){
			throw new ExportServiceException(e);
 		} finally {
 			try {
 				if (fos != null)
 					fos.close();
 			} catch (Exception e) {
 			}
 
 		}
 
 	}
 	
 	IModel getModel(IContextAdapterValidationService validator) {
 		if(validator.getValidationReport().isMapValid()){
 		return (IModel) MetabolicNDOMValidationService.getInstance(serviceProvider).getNDOM();
 		}else{
 			return null;
 		}
 	}
 
 	private void checkArgs(IMap map, File exportFile)
 			throws ExportServiceException, IOException {
 
 		if (map == null || exportFile == null
 				|| map.getTheSingleRootMapObject() == null) {
 			throw new IllegalArgumentException("Arguments must not be null");
 		}
         // yes yt is, export fails without it
 //		exportFile.createNewFile();
 
 		File parent = exportFile.getParentFile();
 		if (parent != null && !parent.canWrite()) {
 			throw new ExportServiceException("Directory " + parent	+ " is not writable");
 		}
 	}
 
 	public String getCode() {
 		return TYPECODE;
 	}
 
 	public IContext getContext() {
 		return context;
 	}
 
 	public String getDisplayName() {
 		return DISPLAY_NAME;
 	}
 
 	public String getRecommendedSuffix() {
 		return RECOMMENDED_SUFFIX;
 	}
 
 	public String toString() {
 		return new StringBuffer().append("Export service for context :")
 				.append(context.toString()).append("\n Display name :").append(
 						DISPLAY_NAME).append("\n Code: ").append(TYPECODE)
 				.toString();
 	}
 
 	IExportAdapter<IModel> getGenerator() {
 			generator = new MetabolicSBMLExportAdapter<IModel>();
 		return generator;
 	}
 
 }
