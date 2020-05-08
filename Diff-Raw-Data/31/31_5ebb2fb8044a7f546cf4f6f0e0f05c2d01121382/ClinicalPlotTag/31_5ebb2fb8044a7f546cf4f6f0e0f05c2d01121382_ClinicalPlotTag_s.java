 package gov.nih.nci.rembrandt.web.taglib;
 
 import gov.nih.nci.caintegrator.service.findings.ClinicalFinding;
 import gov.nih.nci.caintegrator.ui.graphing.chart.CaIntegratorChartFactory;
 import gov.nih.nci.caintegrator.ui.graphing.chart.plot.ClinicalPlot;
 import gov.nih.nci.caintegrator.ui.graphing.data.clinical.ClinicalDataPoint;
 import gov.nih.nci.caintegrator.enumeration.ClinicalFactorType;
 
 import gov.nih.nci.caintegrator.ui.graphing.util.ImageMapUtil;
 import gov.nih.nci.rembrandt.cache.BusinessTierCache;
 import gov.nih.nci.rembrandt.cache.PresentationTierCache;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.RembrandtImageFileHandler;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspWriter;
 
 import org.apache.log4j.Logger;
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.entity.StandardEntityCollection;
 
 /**
  * this class generates a Clinical Plot tag which will take a taskId, the components
  * with which to compare, and possibly a colorBy attribute which colors the
  * samples either by Disease or Gender. Disease is colored by default.
  * @author rossok
  *
  */
 public class ClinicalPlotTag extends AbstractGraphingTag {
 
 	private String beanName = "";
 	private String taskId = "";
     private String colorBy = "";
     private String components ="";
     private Collection<ClinicalDataPoint> clinicalData = new ArrayList();
 	private List<JFreeChart> jFreeChartsList;
     private JFreeChart chart = null;
     private Logger logger = Logger.getLogger(ClinicalPlotTag.class);
 	private PresentationTierCache presentationTierCache = ApplicationFactory.getPresentationTierCache();
 	private BusinessTierCache businessTierCache = ApplicationFactory.getBusinessTierCache();
     
 	public int doStartTag() {
 		chart = null;
 		clinicalData.clear();
 
 		
 		ServletRequest request = pageContext.getRequest();
 		HttpSession session = pageContext.getSession();
 		Object o = request.getAttribute(beanName);
 		JspWriter out = pageContext.getOut();
 		ServletResponse response = pageContext.getResponse();
 		
 		try {
 			
 			//
             //retrieve the Finding from cache and build the list of  Clinical Data points
             //ClinicalFinding clinicalFinding = (ClinicalFinding)businessTierCache.getSessionFinding(session.getId(),taskId);
 			
 			
 			//-------------------------------------------------------------
 			//GET THE CLINICAL DATA AND POPULATE THE clinicalData list
 			//Note the ClinicalFinding is currently an empty class
 			//----------------------------------------------------------
 			
 			
             //check the components to see which graph to get
 			if(components.equalsIgnoreCase("SurvivalvsAgeAtDx")){
                 chart = (JFreeChart) CaIntegratorChartFactory.getClinicalGraph(clinicalData,ClinicalFactorType.Survival,ClinicalFactorType.AgeAtDx);
             }
             if(components.equalsIgnoreCase("NeuroAssesmentvsAgeAtDx")){
                chart = (JFreeChart) CaIntegratorChartFactory.getClinicalGraph(clinicalData,ClinicalFactorType.NeurologicalAssessment, ClinicalFactorType.AgeAtDx);
             }
           
             
             RembrandtImageFileHandler imageHandler = new RembrandtImageFileHandler(session.getId(),"png",700,500);
 			//The final complete path to be used by the webapplication
 			String finalPath = imageHandler.getSessionTempFolder();
             String finalURLpath = imageHandler.getFinalURLPath();
 			/*
 			 * Create the actual charts, writing it to the session temp folder
 			*/ 
             ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
             String mapName = imageHandler.createUniqueMapName();
            
 			ChartUtilities.writeChartAsPNG(new FileOutputStream(finalPath),chart, 700,500,info);
            
 			
 			/*	This is here to put the thread into a loop while it waits for the
 			 *	image to be available.  It has an unsophisticated timer but at 
 			 *	least it is something to avoid an endless loop.
 			 **/ 
             boolean imageReady = false;
             int timeout = 1000;
             while(!imageReady) {
                 timeout--;
                 try {
                     FileInputStream inputStream = new FileInputStream(finalPath);
                     inputStream.available();
                     imageReady = true;
                     inputStream.close();
                 }catch(IOException ioe) {
                     imageReady = false;                 
                 }
                 if(timeout <= 1) {
                     
                     break;
                 }
              }
             
             out.print(ImageMapUtil.getBoundingRectImageMapTag(mapName,true,info));
             finalURLpath = finalURLpath.replace("\\", "/");
             long randomness = System.currentTimeMillis(); //prevent image caching
 		    out.print("<img id=\"geneChart\" name=\"geneChart\" src=\""+finalURLpath+"?"+randomness+"\" usemap=\"#"+mapName + "\" border=\"0\" />");
             
         
 		}catch (IOException e) {
 			logger.error(e);
 		}catch(Exception e) {
 			logger.error(e);
 		}catch(Throwable t) {
 			logger.error(t);
 		}
 	
 		return EVAL_BODY_INCLUDE;
 	}
 	
 
 
 	public int doEndTag() throws JspException {
 		return doAfterEndTag(EVAL_PAGE);
 	}
 	public void reset() {
 		//chartDefinition = createChartDefinition();
 	}
 	/**
 	 * @return Returns the bean.
 	 */
 	public String getBean() {
 		return beanName;
 	}
 	/**
 	 * @param bean The bean to set.
 	 */
 	public void setBean(String bean) {
 		this.beanName = bean;
 	}
 
     /**
      * @return Returns the taskId.
      */
     public String getTaskId() {
         return taskId;
     }
 
     /**
      * @param taskId The taskId to set.
      */
     public void setTaskId(String taskId) {
         this.taskId = taskId;
     }
 
     /**
      * @return Returns the colorBy.
      */
     public String getColorBy() {
         return colorBy;
     }
 
     /**
      * @param colorBy The colorBy to set.
      */
     public void setColorBy(String colorBy) {
         this.colorBy = colorBy;
     }
 
     /**
      * @return Returns the components.
      */
     public String getComponents() {
         return components;
     }
 
     /**
      * @param components The components to set.
      */
     public void setComponents(String components) {
         this.components = components;
     }
 	
 }
