 package de.escidoc.core.oai.business.handler.setdefinition;
 
 import de.escidoc.core.aa.service.interfaces.PolicyDecisionPointInterface;
 import de.escidoc.core.common.business.filter.DbRequestParameters;
 import de.escidoc.core.common.business.filter.SRURequestParameters;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.UniqueConstraintViolationException;
 import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.service.UserContext;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ExplainXmlProvider;
 import de.escidoc.core.common.util.xml.stax.handler.OptimisticLockingStaxHandler;
 import de.escidoc.core.oai.business.filter.SetDefinitionFilter;
 import de.escidoc.core.oai.business.interfaces.SetDefinitionHandlerInterface;
 import de.escidoc.core.oai.business.persistence.SetDefinition;
 import de.escidoc.core.oai.business.persistence.SetDefinitionDaoInterface;
 import de.escidoc.core.oai.business.renderer.VelocityXmlSetDefinitionRenderer;
 import de.escidoc.core.oai.business.renderer.interfaces.SetDefinitionRendererInterface;
 import de.escidoc.core.oai.business.stax.handler.set_definition.SetDefinitionCreateHandler;
 import de.escidoc.core.oai.business.stax.handler.set_definition.SetDefinitionUpdateHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import java.io.ByteArrayInputStream;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Rozita Friedman
  */
 @Service("business.SetDefinitionHandler")
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 public class SetDefinitionHandler implements SetDefinitionHandlerInterface {
 
     @Autowired
     @Qualifier("persistence.SetDefinitionDao")
     private SetDefinitionDaoInterface setDefinitionDao;
 
     @Autowired
     private SetDefinitionRendererInterface renderer;
 
     private static final String MSG_SET_DEFINITION_NOT_FOUND_BY_ID = "Set definition with provided id does not exist.";
 
     @Autowired
     private PolicyDecisionPointInterface pdp;
 
     /**
      * The logger.
      */
     private static final Logger LOGGER = LoggerFactory.getLogger(SetDefinitionHandler.class);
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.oai.business.interfaces.SetDefinitionHandlerInterface
      * #create(java.lang.String)
      */
     @Override
     public String create(final String xmlData) throws UniqueConstraintViolationException, InvalidXmlException,
         MissingMethodParameterException, SystemException, XmlParserSystemException, SqlDatabaseSystemException,
         WebserverSystemException {
         final ByteArrayInputStream in = XmlUtility.convertToByteArrayInputStream(xmlData);
         final StaxParser sp = new StaxParser();
         final SetDefinitionCreateHandler sdch = new SetDefinitionCreateHandler(sp);
 
         sp.addHandler(sdch);
         try {
             sp.parse(in);
             sp.clearHandlerChain();
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         final SetDefinition setDefinition = new SetDefinition();
         setModificationValues(setDefinition, sdch.getSetProperties());
         setCreationValues(setDefinition, sdch.getSetProperties());
         this.setDefinitionDao.save(setDefinition);
 
         return this.renderer.render(setDefinition);
 
     }
 
     /**
      * Sets the creation date and the created-by user in the provided <code>SetDefinition</code> object.<br/> The values
      * are set with the values of modification date and modifying user of the provided set definition.<br/>
      *
      * @param setDefinition definition The <code>SetDefinition</code> object to modify.
      * @param setProperties
      * @throws UniqueConstraintViolationException
      *                         The specification of the given set definition has already been used.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
      */
     private void setCreationValues(final SetDefinition setDefinition, final Map<String, String> setProperties)
         throws UniqueConstraintViolationException, SqlDatabaseSystemException, WebserverSystemException {
 
         // initialize creation-date value
         setDefinition.setCreationDate(setDefinition.getLastModificationDate());
 
         // initialize created-by values
         setDefinition.setCreatorId(UserContext.getId());
         setDefinition.setCreatorTitle(UserContext.getRealName());
 
         final String specification = setProperties.get("specification");
         if (!checkSpecificationUnique(specification)) {
             throw new UniqueConstraintViolationException("The provided set specification is not unique.");
         }
         setDefinition.setSpecification(specification);
         final String query = setProperties.get("query");
         setDefinition.setQuery(query);
     }
 
     /**
      * Sets the last modification date, the modified-by user and all values from the given set map in the provided
      * <code>SetDefinition</code> object. <br/> The last modification date is set to the current time, and the modified
      * by user to the user account of the current, authenticated user.
      *
      * @param setDefinition The <code>SetDefinition</code> object to modify.
      * @param setProperties map which contains all properties of the set definition
      * @return
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      */
     private static boolean setModificationValues(
         final SetDefinition setDefinition, final Map<String, String> setProperties) throws WebserverSystemException {
         boolean changed = false;
         if (setProperties != null) {
             final String newDescription = setProperties.get(Elements.ELEMENT_DESCRIPTION);
             if (newDescription != null
                 && (setDefinition.getDescription() != null && !newDescription.equals(setDefinition.getDescription()) || setDefinition
                     .getDescription() == null)) {
                 setDefinition.setDescription(newDescription);
                 changed = true;
             }
             final String newName = setProperties.get(Elements.ELEMENT_NAME);
             if (setDefinition.getName() == null || setDefinition.getName() != null
                 && !newName.equals(setDefinition.getName())) {
                 setDefinition.setName(setProperties.get(Elements.ELEMENT_NAME));
                 changed = true;
             }
         }
         if (changed) {
             setDefinition.setModifiedById(UserContext.getId());
             setDefinition.setModifiedByTitle(UserContext.getRealName());
             setDefinition.setLastModificationDate(new Timestamp(System.currentTimeMillis()));
         }
         return changed;
     }
 
     /**
      * Check if the given specification is already used as set definition specification in the database.
      *
      * @param specification set definition specification
      * @return true if the specification is still unused
      * @throws SqlDatabaseSystemException Thrown in case of an internal database error.
      */
     private boolean checkSpecificationUnique(final String specification) throws SqlDatabaseSystemException {
         return setDefinitionDao.findSetDefinitionBySpecification(specification) == null;
     }
 
     /**
      * See Interface for functional description.
      *
      * @throws ResourceNotFoundException e
      * @throws SystemException           e
      * @see de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface#retrieve(java.lang.String)
      */
     @Override
     public String retrieve(final String setDefinitionId) throws ResourceNotFoundException, SystemException,
         SqlDatabaseSystemException {
         final SetDefinition setDefinition = setDefinitionDao.retrieveSetDefinition(setDefinitionId);
 
         if (setDefinition == null) {
             throw new ResourceNotFoundException(StringUtility.format(MSG_SET_DEFINITION_NOT_FOUND_BY_ID,
                 setDefinitionId));
         }
         return this.renderer.render(setDefinition);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.oai.business.interfaces.SetDefinitionHandlerInterface
      * #update(java.lang.String, java.lang.String)
      */
     @Override
     public String update(final String setDefinitionId, final String xmlData) throws ResourceNotFoundException,
         OptimisticLockingException, MissingMethodParameterException, SystemException, XmlParserSystemException,
         SqlDatabaseSystemException, WebserverSystemException {
         final SetDefinition setDefinition = setDefinitionDao.retrieveSetDefinition(setDefinitionId);
         if (setDefinition == null) {
             throw new ResourceNotFoundException(StringUtility.format(MSG_SET_DEFINITION_NOT_FOUND_BY_ID,
                 setDefinitionId));
         }
 
         final ByteArrayInputStream in = XmlUtility.convertToByteArrayInputStream(xmlData);
         final StaxParser sp = new StaxParser();
 
         final OptimisticLockingStaxHandler optimisticLockingHandler =
             new OptimisticLockingStaxHandler(setDefinition.getLastModificationDate());
         sp.addHandler(optimisticLockingHandler);
 
         final SetDefinitionUpdateHandler sduh = new SetDefinitionUpdateHandler(sp);
 
         sp.addHandler(sduh);
         try {
             sp.parse(in);
         }
         catch (final OptimisticLockingException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         if (setModificationValues(setDefinition, sduh.getSetProperties())) {
             setDefinitionDao.save(setDefinition);
         }
         return this.renderer.render(setDefinition);
     }
 
     /**
      * See Interface for functional description.
      *
      * @throws ResourceNotFoundException e
      * @see de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface #delete(java.lang.String)
      */
     @Override
     public void delete(final String setDefinitionId) throws ResourceNotFoundException, SqlDatabaseSystemException {
         final SetDefinition setDefinition = setDefinitionDao.retrieveSetDefinition(setDefinitionId);
 
         if (setDefinition == null) {
             final String message = StringUtility.format(MSG_SET_DEFINITION_NOT_FOUND_BY_ID, setDefinitionId);
             LOGGER.error(message);
             throw new ResourceNotFoundException(message);
         }
         setDefinitionDao.delete(setDefinition);
     }
 
     /**
      * See Interface for functional description.
      *
      * @throws AuthenticationException     e
      * @throws AuthorizationException      e
      * @throws InvalidSearchQueryException e
      * @throws SystemException             e
      * @see de.escidoc.core.oai.service.interfaces.SetDefinitionHandlerInterface #retrieveSetDefinitions(java.util.Map)
      */
     @Override
     public String retrieveSetDefinitions(final Map<String, String[]> filter) throws AuthenticationException,
         AuthorizationException, InvalidSearchQueryException, SystemException, SqlDatabaseSystemException,
         WebserverSystemException {
 
         final SRURequestParameters parameters = new DbRequestParameters(filter);
 
         final String query = parameters.getQuery();
         final int limit = parameters.getMaximumRecords();
         final int offset = parameters.getStartRecord();
         final boolean explain = parameters.isExplain();
 
         final String result;
         if (explain) {
             final Map<String, Object> values = new HashMap<String, Object>();
 
             values.put("PROPERTY_NAMES", new SetDefinitionFilter(null).getPropertyNames());
             result = ExplainXmlProvider.getInstance().getExplainSetDefinitionXml(values);
         }
         else {
             final int needed = offset + limit;
             int currentOffset = 0;
             final List<SetDefinition> permittedSetDefinitions = new ArrayList<SetDefinition>();
             final int size = permittedSetDefinitions.size();
 
             while (size <= needed) {
 
                 final List<SetDefinition> tmpSetDefinitions =
                     setDefinitionDao.retrieveSetDefinitions(query, currentOffset, needed);
                 if (tmpSetDefinitions == null || tmpSetDefinitions.isEmpty()) {
                     break;
                 }
                 final List<String> ids = new ArrayList<String>(tmpSetDefinitions.size());
                 for (final SetDefinition setDefinition : tmpSetDefinitions) {
                     ids.add(setDefinition.getId());
                 }
                 try {
                     final List<String> tmpPermitted = pdp.evaluateRetrieve(XmlUtility.NAME_SET_DEFINITION, ids);
                     final int numberPermitted = tmpPermitted.size();
                     if (numberPermitted == 0) {
                         break;
                     }
                     else {
                         int permittedIndex = 0;
                         String currentPermittedId = tmpPermitted.get(permittedIndex);
                         for (final SetDefinition setDefinition : tmpSetDefinitions) {
                             if (currentPermittedId.equals(setDefinition.getId())) {
                                 permittedSetDefinitions.add(setDefinition);
                                 ++permittedIndex;
                                 if (permittedIndex < numberPermitted) {
                                     currentPermittedId = tmpPermitted.get(permittedIndex);
                                 }
                                 else {
                                     break;
                                 }
                             }
                         }
                     }
                 }
                 catch (final MissingMethodParameterException e) {
                     throw new SystemException("Unexpected exception during evaluating access " + "rights.", e);
                 }
                 catch (final ResourceNotFoundException e) {
                     throw new SystemException("Unexpected exception during evaluating access " + "rights.", e);
                 }
                 currentOffset += needed;
             }
 
             final List<SetDefinition> offsetSetDefinitions;
             final int numberPermitted = permittedSetDefinitions.size();
             if (offset < numberPermitted) {
                 offsetSetDefinitions = new ArrayList<SetDefinition>(limit);
                 for (int i = offset; i < numberPermitted && i < needed; i++) {
                     offsetSetDefinitions.add(permittedSetDefinitions.get(i));
                 }
             }
             else {
                 offsetSetDefinitions = new ArrayList<SetDefinition>(0);
             }
             result = renderer.renderSetDefinitions(offsetSetDefinitions, parameters.getRecordPacking());
         }
         return result;
     }
 
     /**
      * Injects the set definition data access object.
      *
      * @param setDefinitionDao The data access object.
      */
     public void setSetDefinitionDao(final SetDefinitionDaoInterface setDefinitionDao) {
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug(StringUtility.format("setDefinitionDao", setDefinitionDao));
         }
         this.setDefinitionDao = setDefinitionDao;
     }
 
 }
