 package uk.ac.ed.inf.Metabolic.excelexport;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.pathwayeditor.businessobjectsAPI.IMap;
 import org.pathwayeditor.contextadapter.publicapi.ExportServiceException;
 import org.pathwayeditor.contextadapter.publicapi.IContext;
 import org.pathwayeditor.contextadapter.publicapi.IContextAdapterExportService;
 import org.pathwayeditor.contextadapter.publicapi.IContextAdapterServiceProvider;
 import org.pathwayeditor.contextadapter.publicapi.IValidationReport;
 
 import uk.ac.ed.inf.Metabolic.ExportAdapterCreationException;
import uk.ac.ed.inf.Metabolic.MetabolicContextValidationService;
 import uk.ac.ed.inf.Metabolic.ndomAPI.IModel;
 
 public class ExcelExportService implements IContextAdapterExportService {
 
 	String DISPLAY_NAME = "Excel export";
 
 	String RECOMMENDED_SUFFIX = "xls";
 	
 	String PROPERTIES_LOCATION = "/templatepath.properties" ;
 
 	final String TYPECODE = "Excel_Export_1.0.0";
 	private IContext context;
 	private IExcelFileGenerator generator;
 
 	public ExcelExportService(IContextAdapterServiceProvider provider) {
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
 	 *             <li> Cannot produce valid Excel
 	 *             </ul>
 	 */
 	public void exportMap(IMap map, File exportFile)
 	throws ExportServiceException {
 FileOutputStream fos = null;
 try {
 	checkArgs(map, exportFile);
 
	MetabolicContextValidationService validator = (MetabolicContextValidationService) serviceProvider
			.getValidationService();
 	validator.setMapToValidate(map);
 	IModel ndom = null;
 	if (validator.isReadyToValidate()) {
 		validator.validateMap();
 		IValidationReport report =validator.getValidationReport();
 		if(!report.isMapValid()){
 			String sb="Map is not valid:\n";
 			
 			throw new ExportServiceException(sb, report);
 		}else{
			ndom=validator.getModel();
 		}
 		System.out.println(validator.getValidationReport());
 		System.out.println(ndom.toString());
 		
 		generator = getGenerator( ndom );
 		
 		generator.createNewWorkbook();
 		
 		if ( !generator.wasWorkBookCreated() )
 			throw new ExportServiceException ( "Workbook generator was not created propery" ) ;
 		
 		try {
 		
 		generator.populateModelPage() ;
 		generator.populateCompoundPage();
 		generator.populateReactionsPage();
 		generator.populateMacromoleculePage() ;
 		}
 		catch (IllegalStateException e)
 		{
 			throw new ExportServiceException ( "There was a problem while generating the Excel file.") ;
 		}
 		
 		generator.saveWorkBook(exportFile) ;
 	}
 } catch (ExportAdapterCreationException e) {
 	throw new ExportServiceException(e);
 } catch (IOException e) {
 	throw new ExportServiceException(e);
 } finally {
 	try {
 		if (fos != null)
 			fos.close();
 	} catch (Exception e) {
 	}
 
 }
 
 }
 
 	private void checkArgs(IMap map, File exportFile)
 			throws ExportServiceException, IOException {
 
 		if (map == null || exportFile == null
 				|| map.getTheSingleRootMapObject() == null) {
 			throw new IllegalArgumentException("Arguments must not be null");
 		}
 		exportFile.createNewFile();
 
 		if (!exportFile.canWrite()) {
 			throw new ExportServiceException("File " + exportFile
 					+ " is not writable");
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
 
 	IExcelFileGenerator getGenerator(IModel model) throws ExportAdapterCreationException {
 		try 
 		{
 			generator = new ExcelGenerator ( model , getTemplatePath () ) ;
 			return generator;
 		}
 		catch ( IllegalArgumentException e)
 		{
 			throw new ExportAdapterCreationException () ;
 		} 
 		
 		
 	}
 	
 	private String getTemplatePath () throws ExportAdapterCreationException  
 	{
 
 		String path ;
 		try {
 			BufferedReader inputReader = new BufferedReader ( new InputStreamReader(this
 		            .getClass().getClassLoader().getResourceAsStream(PROPERTIES_LOCATION))) ;
 			
 			try {
 				path = inputReader.readLine() ;
 			}
 			finally
 			{
 				inputReader.close() ;
 			}
 		}
 		catch ( IOException exc )
 		{
 			throw new ExportAdapterCreationException () ; 
 		}
 		
 		return path;
 	}
 
 }
 
