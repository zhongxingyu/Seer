 package uk.ac.ed.inf.Metabolic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.pathwayeditor.businessobjectsAPI.IMap;
 import org.pathwayeditor.businessobjectsAPI.IMapObject;
 import org.pathwayeditor.contextadapter.publicapi.IContext;
 import org.pathwayeditor.contextadapter.publicapi.IContextAdapterServiceProvider;
 import org.pathwayeditor.contextadapter.publicapi.IContextAdapterValidationService;
 import org.pathwayeditor.contextadapter.publicapi.IValidationReport;
 import org.pathwayeditor.contextadapter.publicapi.IValidationReportItem;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleDefinition;
 import org.pathwayeditor.contextadapter.publicapi.IValidationReportItem.Severity;
 import org.pathwayeditor.contextadapter.publicapi.IValidationRuleDefinition.RuleLevel;
 import org.pathwayeditor.contextadapter.toolkit.ndom.AbstractNDOMParser.NdomException;
 import org.pathwayeditor.contextadapter.toolkit.validation.DefaultValidationReport;
 import org.pathwayeditor.contextadapter.toolkit.validation.ValidationReportItem;
 import org.pathwayeditor.contextadapter.toolkit.validation.ValidationRuleDefinition;
 
 import uk.ac.ed.inf.Metabolic.ndomAPI.IModel;
 import uk.ac.ed.inf.Metabolic.parser.MetabolicNDOMFactory;
 
 public class MetabolicContextValidationService implements
 		IContextAdapterValidationService {
 	private IContext context;
 	private IValidationReport validationReport;
 	List<IValidationReportItem> reportItems;
 	private boolean beenValidated = false;
 	private boolean readyToValidate = false;
 	private IMap mapToValidate;
 	private IModel ndom;
 	private MetabolicNDOMFactory factory;
 	
 	IValidationRuleDefinition EXAMPLE_ERROR; 
 	IValidationRuleDefinition EXAMPLE_WARNING;
 		
 	
 	public MetabolicContextValidationService(IContextAdapterServiceProvider provider) {
 		this.serviceProvider=provider;
 		context=provider.getContext();
             EXAMPLE_ERROR = new ValidationRuleDefinition(context, "A rule 1", "A Catefory", 1, RuleLevel.MANDATORY);
 		 EXAMPLE_WARNING = new ValidationRuleDefinition(context, "A rule 2", "A Catefory", 2, RuleLevel.GUIDELINE);
 	}
 	private IContextAdapterServiceProvider serviceProvider;
 
 	public IContextAdapterServiceProvider getServiceProvider() {
 		return serviceProvider;
 	}
 
 
 	public IMap getMapBeingValidated() {
 		return mapToValidate;
 	}
 
 	public IValidationReport getValidationReport() {
 		return validationReport;
 	}
 
 	public boolean hasMapBeenValidated() {
 		return beenValidated;
 	}
 
 	
 
 	public boolean isImplemented() {
 		return true;
 	}
 
 	
 
 	public void validateMap() {
 		if(!isReadyToValidate()) return;
 		reportItems = new ArrayList<IValidationReportItem>();
 		factory = new MetabolicNDOMFactory();
 		factory.setRmo(mapToValidate.getTheSingleRootMapObject());
 		try {
 			factory.parse();
 			ndom=factory.getNdom();
 		} catch (NdomException e) {
			reportItems.add(new ValidationReportItem(null, EXAMPLE_ERROR, Severity.ERROR, "specific description1"));
 		}finally{
 			IMapObject exampleShape = getEXampleShapeFromMap();
			ValidationReportItem parent = new ValidationReportItem(exampleShape, EXAMPLE_WARNING, Severity.WARNING,"specific description2");
			parent.addChildReportItem(new ValidationReportItem(null, EXAMPLE_WARNING, Severity.ERROR,"specific description2"));
 			reportItems.add(parent);
 			copyReport();
 		}
 		beenValidated=true;
 	}
     
 	
 	// place holder method, should be reomved once we have report generation
 	private IMapObject getEXampleShapeFromMap() {
 		if (!mapToValidate.getTheSingleRootMapObject().getChildren().isEmpty()) {
 			return mapToValidate.getTheSingleRootMapObject().getChildren().get(0);
 		}
 		return null;
 	}
 
 
 	void copyReport() {
 	//	validationReport.addAll(factory.getReport());
 		validationReport = new DefaultValidationReport(mapToValidate, reportItems);
 		
 	}
 
 	public IContext getContext() {
 		return context;
 	}
 
 	public boolean isReadyToValidate() {
 		return readyToValidate;
 	}
 
 	public void setMapToValidate(IMap mapToValidate) {
 		if(mapToValidate==null) throw new IllegalArgumentException("Map to be validated should not be null");
 		this.mapToValidate = mapToValidate;
 		beenValidated=false;
 		readyToValidate=(this.mapToValidate!=null); 
 	}
 
 	public IModel getModel(){
 		return ndom;
 	}
 
 
 	public List<IValidationRuleDefinition> getRules() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
 	
 	
 
 /*
  * $Log$
 * Revision 1.4  2008/06/16 14:53:16  radams
 * add string message to error reporting
 *
  * Revision 1.3  2008/06/11 11:18:05  radams
  * put in placeholder report generation and altered validation service exception
  *
  * Revision 1.2  2008/06/05 22:15:49  radams
  * added validation method
  *
  * Revision 1.1  2008/06/02 10:31:42  asorokin
  * Reference to Service provider from all Service interfaces
  *
  */
