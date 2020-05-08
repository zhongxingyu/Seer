 package gov.nih.nci.rembrandt.util;
 
 import gov.nih.nci.caintegrator.application.analysis.AnalysisServerClientManager;
 import gov.nih.nci.rembrandt.cache.RembrandtContextListener;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.QueryHandler;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.File;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException ;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.jms.JMSException;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 
 import com.sun.org.apache.xerces.internal.impl.xs.dom.DOMParser;
 
 import org.apache.ojb.broker.core.PersistenceBrokerFactoryFactory;
 import org.apache.ojb.broker.core.PersistenceBrokerFactoryIF;
 import org.apache.ojb.broker.metadata.ConnectionRepository;
 import org.apache.ojb.broker.metadata.JdbcConnectionDescriptor;
 import org.apache.ojb.broker.metadata.MetadataManager;
 import org.apache.ojb.broker.PBKey;
 /**
  * @todo comment this!
  * @author BhattarR, BauerD
  *
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class ApplicationContext{
 	private static Map mappings = new HashMap();
 	private static Logger logger = Logger.getLogger(ApplicationContext.class);
 	private static Properties labelProps = null;
 	private static Properties messagingProps = null;
     private static Document doc =null;
    /**
     * COMMENT THIS
     * @return
     */
     public static Properties getLabelProperties() {
         return labelProps;
     }
     public static Map getDEtoBeanAttributeMappings() {
     	return mappings;
     }
     public static Properties getJMSProperties(){
     	return messagingProps;
     }
     @SuppressWarnings("unused")
 	public static void init() {
     	 logger.debug("Loading Application Resources");
          labelProps = PropertyLoader.loadProperties(RembrandtConstants.APPLICATION_RESOURCES);
          messagingProps = PropertyLoader.loadProperties(RembrandtConstants.JMS_PROPERTIES);
          try {
 	          logger.debug("Bean to Attribute Mappings");
 	          InputStream inStream = QueryHandler.class.getResourceAsStream(RembrandtConstants.DE_BEAN_FILE_NAME);
 	          assert true:inStream != null;
 	          DOMParser p = new DOMParser();
 	          p.parse(new InputSource(inStream));
 	          doc = p.getDocument();
 	          assert(doc != null);
 	          logger.debug("Begining DomainElement to Bean Mapping");
 	          mappings = new DEBeanMappingsHandler().populate(doc);
 	          logger.debug("DomainElement to Bean Mapping is completed");
 	          QueryHandler.init();
 	      } catch(Throwable t) {
 	         logger.error(new IllegalStateException("Error parsing deToBeanAttrMappings.xml file: Exception: " + t));
 	      }
       //Start the JMS Lister
         try {
 		@SuppressWarnings("unused") AnalysisServerClientManager analysisServerClientManager = AnalysisServerClientManager.getInstance();
 		//set the AnalysisServerClientManager properties
 		//Get the jms properties for this application
 		//analysisServerClientManager.setJMSparameters();
 		
 		  //Get the application properties from the properties file
		  String propertiesFileName = System.getProperty("gov.nih.nci.rembrandt.propertiesFile");
 		
 		  //Load the the application properties and set them as system properties
 		  Properties rembrandtProperties = new Properties();
 		  
 		  
 		  logger.info("Attempting to load application system properties from file: " + propertiesFileName);
 		   
 		  FileInputStream in = new FileInputStream(propertiesFileName);
 		  rembrandtProperties.load(in);
 		   
 		  if (rembrandtProperties.isEmpty()) {
 		     logger.error("Error: no properties found when loading properties file: " + propertiesFileName);
 		  }
 		    		   
 		  String key = null;
 		  String val = null;
 		  for (Iterator i = rembrandtProperties.keySet().iterator(); i.hasNext(); ) {
 			  key = (String) i.next();
 			  val = rembrandtProperties.getProperty(key);
 		      System.setProperty(key, val);
 		  }
 
 		  //Initialize db
 		  // PersistenceBrokerFactoryIF pbf = PersistenceBrokerFactoryFactory.instance();
 		  String dbalias = System.getProperty("gov.nih.nci.rembrandt.dbalias");
 		  String username = System.getProperty("gov.nih.nci.rembrandt.db.username");
 		  String password = System.getProperty("gov.nih.nci.rembrandt.db.password");
 		  String jcdalias = System.getProperty("gov.nih.nci.rembrandt.jcd_alias");
 		  
 		  if (jcdalias != null && jcdalias.length() > 0){
 			  MetadataManager mm = MetadataManager.getInstance();
 			  ConnectionRepository connectionRepository = mm.connectionRepository();
 			  PBKey pbKey = connectionRepository.getStandardPBKeyForJcdAlias(jcdalias);
 			  JdbcConnectionDescriptor jdbcConnectionDescriptor = connectionRepository
 	                .getDescriptor(pbKey);
 		  
 			  if (dbalias != null && dbalias.length() > 0)
 				  jdbcConnectionDescriptor.setDbAlias(dbalias);
 			  if (username != null && username.length() > 0)
 				  jdbcConnectionDescriptor.setUserName(username);
 			  if (password != null && password.length() > 0)
 				  jdbcConnectionDescriptor.setPassWord(password);
 		  }
 		  //end of initialize
 
 		  String jmsProviderURL = System.getProperty("gov.nih.nci.rembrandt.jms.jboss_url");
 		  String jndiFactoryName = System.getProperty("gov.nih.nci.rembrandt.jms.factory_jndi");
 		  String requestQueueName = System.getProperty("gov.nih.nci.rembrandt.jms.analysis_request_queue");
 		  String responseQueueName = System.getProperty("gov.nih.nci.rembrandt.jms.analysis_response_queue");
 		
 		 
 		  analysisServerClientManager.setJMSparameters(jmsProviderURL, jndiFactoryName,requestQueueName, responseQueueName);
 		  
 //        ANALYSIS SERVER  SET the CACHE and GeneExpressionAnnotationService 
 
 		  analysisServerClientManager.setCache(ApplicationFactory.getBusinessTierCache());
 //		  analysisServerClientManager.setGeneExprAnnotationService();
 		
 		  analysisServerClientManager.establishQueueConnection();
 		  
 		  
 		} catch (NamingException e) {
 	        logger.error(new IllegalStateException("Error getting an instance of AnalysisServerClientManager" ));
 			logger.error(e.getMessage());
 			logger.error(e);
 		} catch (JMSException e) {
 	        logger.error(new IllegalStateException("Error getting an instance of AnalysisServerClientManager" ));
 			logger.error(e.getMessage());
 			logger.error(e);
 		} catch(Throwable t) {
 			logger.error(new IllegalStateException("Error getting an instance of AnalysisServerClientManager" ));
 			logger.error(t.getMessage());
 			logger.error(t);
 		}
 
     }
 }
