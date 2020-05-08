 package gov.nih.nci.cagrid.data.service;
 
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.data.DataServiceConstants;
 import gov.nih.nci.cagrid.data.MalformedQueryException;
 import gov.nih.nci.cagrid.data.QueryProcessingException;
 import gov.nih.nci.cagrid.data.cql.validation.CqlDomainValidator;
 import gov.nih.nci.cagrid.data.cql.validation.CqlStructureValidator;
 import gov.nih.nci.cagrid.data.cql2.validation.walker.Cql2Walker;
 import gov.nih.nci.cagrid.data.cql2.validation.walker.Cql2WalkerDomainModelValidationHandler;
 import gov.nih.nci.cagrid.data.cql2.validation.walker.Cql2WalkerException;
 import gov.nih.nci.cagrid.data.cql2.validation.walker.Cql2WalkerExtensionCompatibilityValidationHandler;
 import gov.nih.nci.cagrid.data.cql2.validation.walker.Cql2WalkerHandler;
 import gov.nih.nci.cagrid.data.cql2.validation.walker.Cql2WalkerStructureValidationHandler;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class CqlValidationUtil {
     
     private static Log LOG = LogFactory.getLog(CqlValidationUtil.class);
     
     private Properties dataServiceConfiguration = null;
     private DomainModel domainModel = null;
     private CqlStructureValidator cql1StructureValidator = null;
     private CqlDomainValidator cql1DomainValidator = null;
     private Cql2Walker cql2Walker = null;
         
     CqlValidationUtil(Properties dataServiceConfiguration, DomainModel domainModel) {
         this.dataServiceConfiguration = dataServiceConfiguration;
         this.domainModel = domainModel;
         this.cql2Walker = new Cql2Walker();
         // initialize the CQL 2 walker handlers
        initializeCql2WalkerHandlers();
    }
    
    
    private void initializeCql2WalkerHandlers() {
        if (shouldValidateCqlStructure()) {
            this.cql2Walker.addListener(new Cql2WalkerStructureValidationHandler());
        }
        if (shouldValidateDomainModel()) {
            this.cql2Walker.addListener(new Cql2WalkerDomainModelValidationHandler(domainModel));
        }
         Collection<QName> empty = Collections.emptyList();
         this.cql2Walker.addListener(new Cql2WalkerExtensionCompatibilityValidationHandler(empty));
     }
     
     
     void setSupportedExtensions(Collection<QName> supported) {
         Cql2WalkerHandler[] handlers = this.cql2Walker.getListeners();
         for (Cql2WalkerHandler handler : handlers) {
             if (handler instanceof Cql2WalkerExtensionCompatibilityValidationHandler) {
                 this.cql2Walker.removeListener(handler);
             }
         }
         cql2Walker.addListener(new Cql2WalkerExtensionCompatibilityValidationHandler(supported));
     }
     
     
     public void validateCql1Query(CQLQuery query) throws QueryProcessingException, MalformedQueryException {
         if (shouldValidateCqlStructure()) {
             getCqlStructureValidator().validateCqlStructure(query);
         }
         if (shouldValidateDomainModel()) {
             getCqlDomainValidator().validateDomainModel(query, domainModel);
         }
     }
     
     
     public void validateCql2Query(org.cagrid.cql2.CQLQuery query) throws QueryProcessingException, MalformedQueryException {
         try {
             cql2Walker.walkCql(query);
         } catch (Cql2WalkerException ex) {
             throw new MalformedQueryException(ex.getMessage(), ex);
         }
     }    
     
     
     protected boolean shouldValidateCqlStructure() {
         return dataServiceConfiguration.getProperty(DataServiceConstants.VALIDATE_CQL_FLAG) != null 
             && Boolean.valueOf(dataServiceConfiguration.getProperty(
                 DataServiceConstants.VALIDATE_CQL_FLAG)).booleanValue();
     }
     
     
     protected boolean shouldValidateDomainModel() {
         return dataServiceConfiguration.getProperty(DataServiceConstants.VALIDATE_DOMAIN_MODEL_FLAG) != null
             && Boolean.valueOf(dataServiceConfiguration.getProperty(
                 DataServiceConstants.VALIDATE_DOMAIN_MODEL_FLAG)).booleanValue();
     }
         
     
     private CqlStructureValidator getCqlStructureValidator() throws QueryProcessingException {
         if (cql1StructureValidator == null) {
             try {
                 String validatorClassName = dataServiceConfiguration.getProperty(DataServiceConstants.CQL_VALIDATOR_CLASS);
                 LOG.debug("Loading CQL structure validator class " + validatorClassName);
                 Class<?> validatorClass = Class.forName(validatorClassName);
                 cql1StructureValidator = (CqlStructureValidator) validatorClass.newInstance();
             } catch (Exception ex) {
                 throw new QueryProcessingException("Error getting CQL structure validator: " + ex.getMessage(), ex);
             }
         }
         return cql1StructureValidator;
     }
     
     
     private CqlDomainValidator getCqlDomainValidator() throws QueryProcessingException {
         if (cql1DomainValidator == null) {
             try {
                 String validatorClassName = dataServiceConfiguration.getProperty(DataServiceConstants.DOMAIN_MODEL_VALIDATOR_CLASS);
                 LOG.debug("Loading CQL Domain Model validator class " + validatorClassName);
                 Class<?> validatorClass = Class.forName(validatorClassName);
                 cql1DomainValidator = (CqlDomainValidator) validatorClass.newInstance();
             } catch (Exception ex) {
                 throw new QueryProcessingException("Error getting CQL domain validator: " + ex.getMessage(), ex);
             }
         }
         return cql1DomainValidator;
     }
 }
