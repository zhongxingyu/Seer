 package eu.dm2e.ws.api;
 
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.annotations.Namespaces;
 import eu.dm2e.ws.grafeo.annotations.RDFClass;
 import eu.dm2e.ws.grafeo.annotations.RDFProperty;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 
 /**
  * This file was created within the DM2E project.
  * http://dm2e.eu
  * http://github.com/dm2e
  *
  * Author: Kai Eckert, Konstantin Baierer
  */
 @Namespaces({"omnom", NS.OMNOM.BASE, 
 			 "skos", NS.SKOS.BASE,
 			 "dc", NS.DC.BASE
 			 })
 @RDFClass(NS.OMNOM.CLASS_PARAMETER)
 public class ParameterPojo extends SerializablePojo<ParameterPojo>{
 	
 	/******************
 	 * CONSTRUCTORS
 	 *****************/
 
 //    public ParameterPojo(String title, String value) {
 //    	this.title = title;
 //    	this.parameterValue = value;
 //	}
 
 	public ParameterPojo() {
 		// TODO Auto-generated constructor stub
 	}
 
 
     /******************
      * HELPERS
      *****************/
 
     public ParameterAssignmentPojo createAssignment(String value) {
         ParameterAssignmentPojo pa = new ParameterAssignmentPojo();
         pa.setForParam(this);
         pa.setParameterValue(value);
         return pa;
     }
     
     public boolean matchesParameterName(String needle) {
     	if (null == needle || "".equals(needle)) return false;
 		return (
 				(this.hasId() && this.getId().equals(needle))
 			||
				(this.hasId() && this.getId().matches(".*" + needle + "$"))
 			||
 				(this.hasLabel() && this.getLabel().equals(needle))
 			);
     }
     
     public void validateParameterInput(String input) throws NumberFormatException {
     	if (null == getParameterType()) {
     		return;
     	}
     	String type = this.getParameterType();
     	if (type.equals(NS.XSD.INT)) {
 			try {
 				Integer.parseInt(input);
 			} catch (NumberFormatException e) {
 				throw e;
 			}
     	}
     }
     
 	/******************
 	 * GETTERS/SETTERS
 	 *****************/
 
 	@RDFProperty(NS.OMNOM.PROP_WEBSERVICE)
     private WebservicePojo webservice;
 	public WebservicePojo getWebservice() { return webservice; }
     public void setWebservice(WebservicePojo webservice) { this.webservice = webservice; }
     
 	@RDFProperty(NS.OMNOM.PROP_WORKFLOW)
     private WorkflowPojo workflow;
 	public WorkflowPojo getWorkflow() { return workflow; }
 	public void setWorkflow(WorkflowPojo workflow) { this.workflow = workflow; }
     
     @RDFProperty(NS.DC.PROP_TITLE)
     private String title;
     public String getTitle() { return title; }
 	public void setTitle(String title) { this.title = title; }
 
     @RDFProperty(NS.OMNOM.PROP_IS_REQUIRED)
     private boolean isRequired;
 	public boolean getIsRequired() { return isRequired; }
 	public void setIsRequired(boolean isRequired) { this.isRequired = isRequired; }
 	
 	@RDFProperty(NS.OMNOM.PROP_PARAMETER_TYPE)
 	private String parameterType;
 	public String getParameterType() { return parameterType; }
 	public void setParameterType(String parameterType) { 
 		Grafeo g = new GrafeoImpl();
 		this.parameterType = g.expand(parameterType); 
 	}
 
 }
