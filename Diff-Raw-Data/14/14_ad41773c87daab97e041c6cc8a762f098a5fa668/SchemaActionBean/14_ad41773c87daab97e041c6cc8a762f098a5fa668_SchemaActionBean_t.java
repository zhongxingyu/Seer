 /**
  *
  */
 package eionet.web.action;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.FileBean;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.ValidationMethod;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.xml.sax.SAXException;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.DElemAttribute;
 import eionet.meta.DownloadServlet;
 import eionet.meta.FixedValue;
 import eionet.meta.dao.DAOException;
 import eionet.meta.dao.domain.Schema;
 import eionet.meta.dao.domain.SchemaSet;
 import eionet.meta.dao.domain.SchemaSet.RegStatus;
 import eionet.meta.schemas.SchemaRepository;
 import eionet.meta.service.ISchemaService;
 import eionet.meta.service.IXmlConvService;
 import eionet.meta.service.ServiceException;
 import eionet.meta.service.data.SchemaConversionsData;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.SecurityUtil;
 import eionet.util.Util;
 import eionet.util.XmlValidator;
 
 /**
  * @author Jaanus Heinlaid
  *
  */
 @UrlBinding("/schema/{schemaSet.identifier=" + SchemaSet.ROOT_IDENTIFIER + "}/{schema.fileName}/{$event}")
 public class SchemaActionBean extends AbstractActionBean {
 
     /** */
     private static final Logger LOGGER = Logger.getLogger(SchemaActionBean.class);
 
     /** */
     private static final String ADD_ROOT_LEVEL_SCHEMA_JSP = "/pages/schemaSets/addRootLevelSchema.jsp";
 
     /** */
     private static final String VIEW_SCHEMA_JSP = "/pages/schemaSets/viewSchema.jsp";
 
     /** */
     private static final String EDIT_SCHEMA_JSP = "/pages/schemaSets/editSchema.jsp";
 
     /** */
     private Schema schema;
     private SchemaSet schemaSet;
     private FileBean uploadedFile;
 
     /** */
     private LinkedHashMap<Integer, DElemAttribute> attributes;
 
     /** This schema's attribute values as submitted from the save/add form. */
     private Map<Integer, Set<String>> saveAttributeValues;
 
     /** Values of this schema set's attributes that can have multiple values. */
     private Map<String, Set<String>> multiValuedAttributeValues;
 
     /** Values of this schema set's attributes that can have only fixed values. */
     private Map<String, Set<String>> fixedValuedAttributeValues;
 
     /** The list of this schema's versions. Relevant for root-level schemas only. */
     private List<Schema> otherVersions;
 
     /** Schema service. */
     @SpringBean
     private ISchemaService schemaService;
 
     @SpringBean
     private IXmlConvService xmlConvService;
 
     /** Schema repository. */
     @SpringBean
     private SchemaRepository schemaRepository;
 
     /** Schema file contents to show in web page. */
     private String schemaString;
 
     private SchemaConversionsData xmlConvData;
 
     /** Parameter, that shows if working copy is been viewed. */
     private boolean workingCopy;
 
     /**
      *
      * @return
      * @throws ServiceException
      * @throws IOException
      */
     @DefaultHandler
     public Resolution view() throws ServiceException, IOException {
 
         loadSchemaByName();
         loadSchemaString();
         // "http://dd.eionet.europa.eu/GetSchema?id=TBL4943"
        if (!workingCopy) {
            xmlConvData = xmlConvService.getSchemaConversionsData(getSchemaUrl());
        }
         return new ForwardResolution(VIEW_SCHEMA_JSP);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution edit() throws ServiceException {
 
         loadSchemaByName();
         return new ForwardResolution(EDIT_SCHEMA_JSP);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution save() throws ServiceException {
 
         schemaService.updateSchema(schema, getSaveAttributeValues(), getUserName());
         addSystemMessage("Schema successfully updated!");
         return new ForwardResolution(EDIT_SCHEMA_JSP);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution saveAndClose() throws ServiceException {
 
         schemaService.updateSchema(schema, getSaveAttributeValues(), getUserName());
         addSystemMessage("Schema successfully updated!");
         return new RedirectResolution(getClass())
         .addParameter("schemaSet.identifier", schemaSet == null ? null : schemaSet.getIdentifier())
         .addParameter("schema.fileName", schema.getFileName()).addParameter("workingCopy", true);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      * @throws IOException
      */
     public Resolution add() throws ServiceException, IOException {
 
         if (!isCreateAllowed()) {
             throw new ServiceException("You are not authorised for this operation!");
         }
 
         Resolution resolution = new ForwardResolution(ADD_ROOT_LEVEL_SCHEMA_JSP);
         if (!isGetOrHeadRequest()) {
 
             File savedSchemaFile = null;
             try {
                 savedSchemaFile = schemaRepository.addSchema(uploadedFile, null, true);
 
                 schema.setFileName(uploadedFile.getFileName());
                 schema.setUserModified(getUserName());
                 schema.setWorkingUser(getUserName());
                 schema.setWorkingCopy(true);
 
                 schemaService.addSchema(schema, getSaveAttributeValues());
             } catch (ServiceException e) {
                 SchemaRepository.deleteQuietly(savedSchemaFile);
                 throw e;
             }
             resolution =
                 new RedirectResolution(getClass()).addParameter("schema.fileName", schema.getFileName()).addParameter(
                         "workingCopy", true);
             addSystemMessage("Working copy successfully created!");
         }
         return resolution;
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution cancelEdit() throws ServiceException {
 
         return new RedirectResolution(getClass())
         .addParameter("schemaSet.identifier", schemaSet == null ? null : schemaSet.getIdentifier())
         .addParameter("schema.fileName", schema.getFileName()).addParameter("workingCopy", true);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution cancelAdd() throws ServiceException {
         return new RedirectResolution(BrowseSchemaSetsActionBean.class);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution delete() throws ServiceException {
 
         schemaService.deleteSchemas(Collections.singletonList(schema.getId()), getUserName(), true);
         addSystemMessage("Schema succesfully deleted!");
         return new RedirectResolution(BrowseSchemaSetsActionBean.class);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution checkIn() throws ServiceException {
 
         loadSchemaById();
         int finalId = schemaService.checkInSchema(schema.getId(), getUserName(), schema.getComment());
         addSystemMessage("Schema successfully checked in!");
         return new RedirectResolution(getClass())
         .addParameter("schemaSet.identifier", schemaSet == null ? null : schemaSet.getIdentifier())
         .addParameter("schema.fileName", schema.getFileName()).addParameter("workingCopy", false);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution checkOut() throws ServiceException {
 
         loadSchemaById();
         if (!isCheckoutAllowed()) {
             throw new ServiceException("You are not authorised for this operation!");
         }
 
         int newSchemaSetId = schemaService.checkOutSchema(schema.getId(), getUserName());
         addSystemMessage("Schema successfully checked out!");
         return new RedirectResolution(getClass())
         .addParameter("schemaSet.identifier", schemaSet == null ? null : schemaSet.getIdentifier())
         .addParameter("schema.fileName", schema.getFileName()).addParameter("workingCopy", true);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      * @throws IOException
      */
     public Resolution newVersion() throws ServiceException, IOException {
         if (!isCreateAllowed()) {
             throw new ServiceException("You are not authorised for this operation!");
         }
         int newSchemaId = schemaService.copySchema(schema.getId(), getUserName(), uploadedFile);
         schema.setId(newSchemaId);
         loadSchemaById();
         addSystemMessage("The new version's working copy successfully created!");
         return new RedirectResolution(getClass())
         .addParameter("schemaSet.identifier", schemaSet == null ? null : schemaSet.getIdentifier())
         .addParameter("schema.fileName", schema.getFileName()).addParameter("workingCopy", true);
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Resolution undoCheckout() throws ServiceException {
         loadSchemaById();
         int checkedOutCopyId = schemaService.undoCheckOutSchema(schema.getId(), getUserName());
         addSystemMessage("Working copy successfully deleted!");
         if (checkedOutCopyId > 0) {
             return new RedirectResolution(getClass())
             .addParameter("schemaSet.identifier", schemaSet == null ? null : schemaSet.getIdentifier())
             .addParameter("schema.fileName", schema.getFileName()).addParameter("workingCopy", false);
         } else {
             return new RedirectResolution(BrowseSchemaSetsActionBean.class);
         }
     }
 
     /**
      *
      * @return
      * @throws IOException
      * @throws ServiceException
      */
     public Resolution reupload() throws IOException, ServiceException {
 
         loadSchemaById();
         loadSchemaString();
         String schemaSetIdentifier = schemaSet == null ? null : schemaSet.getIdentifier();
         schemaRepository.reuploadSchema(schema.getFileName(), schemaSetIdentifier, uploadedFile);
 
         addSystemMessage("Schema file successfully uploaded!");
         loadSchemaString();
        if (!workingCopy) {
            xmlConvData = xmlConvService.getSchemaConversionsData(getSchemaUrl());
        }
         return new ForwardResolution(VIEW_SCHEMA_JSP);
     }
 
     /**
      * An event that validates the given schema's content.
      * Expects to be invoked as a GET request, from the schema's view page.
      * Forwards to the schema's view page, with an appropriate feedback message.
      *
      * @return {@link ForwardResolution} to the the schema's view page.
      * @throws ServiceException In case something goes wrong.
      */
     public Resolution validate() throws ServiceException {
 
         // Do the loadings we do for view(), because we forward to view page.
         loadSchemaByName();
         loadSchemaString();
 
         boolean wc = schema.isWorkingCopy() || schema.isSchemaSetWorkingCopy();
         File schemaFile = schemaRepository.getSchemaFile(schema.getFileName(), schema.getSchemaSetIdentifier(), wc);
         if (schemaFile != null) {
 
             InputStream inputStream = null;
             try {
                 inputStream = new FileInputStream(schemaFile);
 
                 XmlValidator xmlValidator = new XmlValidator();
                 if (!xmlValidator.isWellFormedXml(inputStream)) {
                     addWarningMessage("Not a well-formed XML: " + xmlValidator.getValidationError().getMessage());
                     LOGGER.error("Not a well-formed XML!", xmlValidator.getValidationError());
                 } else {
                     IOUtils.closeQuietly(inputStream);
                     inputStream = new FileInputStream(schemaFile);
                     if (!xmlValidator.isValidXmlSchema(inputStream)) {
                         addWarningMessage("The uploaded file was not found to be a valid XML Schema! Reason: "
                                 + xmlValidator.getValidationError().getMessage());
                         LOGGER.error("Not a valid XML Schema file!", xmlValidator.getValidationError());
                     }
                     else{
                         addSystemMessage("The file was found to be valid XML Schema!");
                     }
                 }
             } catch (Exception e) {
                 throw new ServiceException(e.getMessage(), e);
             } finally {
                 IOUtils.closeQuietly(inputStream);
             }
         }
         else{
             addGlobalValidationError("Could not find such a schema file!");
         }
 
         return new ForwardResolution(VIEW_SCHEMA_JSP);
     }
 
     /**
      *
      * @throws Exception
      */
     @ValidationMethod(on = {"add"})
     public void validateAdd() throws Exception {
 
         if (!SecurityUtil.hasPerm(getUserName(), "/schemas", "i")) {
             throw new ServiceException("No permission to create root-level schemas!");
         }
 
         if (isGetOrHeadRequest()) {
             return;
         }
 
         if (uploadedFile == null) {
             addGlobalValidationError("No file uploaded!");
         } else if (uploadedFile.getSize() <= 0) {
             addGlobalValidationError("Uploaded file must not be empty!");
         }
 
         if (!isValidationErrors()) {
 
             if (schemaService.schemaExists(uploadedFile.getFileName(), 0)) {
                 addGlobalValidationError("A root-level schema or a root-level schema working copy by this filename already exists!");
             }
         }
 
         if (!isValidationErrors()) {
             LinkedHashMap<Integer, DElemAttribute> attributesMap = getAttributes();
             for (DElemAttribute attribute : attributesMap.values()) {
 
                 if (attribute.isMandatory()) {
                     Integer attrId = Integer.valueOf(attribute.getID());
                     Set<String> attrValues = getSaveAttributeValues().get(attrId);
                     if (attrValues == null || attrValues.isEmpty() || StringUtils.isBlank(attrValues.iterator().next())) {
                         addGlobalValidationError(attribute.getShortName() + " is missing!");
                     }
                 }
             }
         }
 
         if (isValidationErrors()) {
             getContext().setSourcePageResolution(new ForwardResolution(ADD_ROOT_LEVEL_SCHEMA_JSP));
         }
     }
 
     /**
      * @throws ServiceException
      *
      */
     @ValidationMethod(on = {"view", "edit", "save", "saveAndClose", "reupload"}, priority = 1)
     public void validateViewEditSave() throws ServiceException {
 
         Schema currSchema = null;
         if (schema != null && schema.getId() > 0) {
             currSchema = schemaService.getSchema(schema.getId());
         } else if (schemaSet != null && schema != null && StringUtils.isNotEmpty(schema.getFileName())) {
             if (schemaSet.getIdentifier().equals(SchemaSet.ROOT_IDENTIFIER)) {
                 currSchema = schemaService.getRootLevelSchema(schema.getFileName(), workingCopy);
             } else {
                 currSchema = schemaService.getSchema(schemaSet.getIdentifier(), schema.getFileName(), workingCopy);
             }
         } else {
             throw new ServiceException("Schema id missing!");
         }
 
         int schemaSetId = currSchema.getSchemaSetId();
         SchemaSet currSchemaSet = schemaSetId <= 0 ? null : schemaService.getSchemaSet(schemaSetId);
 
         if (!isUserLoggedIn()) {
             RegStatus regStatus = currSchemaSet == null ? currSchema.getRegStatus() : currSchemaSet.getRegStatus();
             if (!Util.enumEquals(regStatus, SchemaSet.RegStatus.RELEASED, SchemaSet.RegStatus.PUBLIC_DRAFT)) {
                 throw new ServiceException("Un-authenticated users can only see definitions in Released status!");
             }
         }
 
         boolean isAllowed = true;
         if (currSchema.isWorkingCopy()) {
             isAllowed = isUserLoggedIn() && (currSchema.isWorkingCopyOf(getUserName()));
         }
 
         if (isAllowed) {
             if (currSchemaSet != null && currSchemaSet.isWorkingCopy()) {
                 isAllowed = isUserLoggedIn() && (currSchemaSet.isWorkingCopyOf(getUserName()));
             }
         }
 
         if (!isAllowed) {
             throw new ServiceException("A working copy can only be seen and edited by its owner or system administrator!");
         }
 
     }
 
     /**
      *
      * @throws DAOException
      */
     @ValidationMethod(on = {"save", "saveAndClose"}, priority = 2)
     public void validateSave() throws DAOException {
 
         LOGGER.trace("Method entered");
 
         if (isGetOrHeadRequest()) {
             return;
         }
 
         LinkedHashMap<Integer, DElemAttribute> attributesMap = getAttributes();
         for (DElemAttribute attribute : attributesMap.values()) {
 
             if (attribute.isMandatory()) {
                 Integer attrId = Integer.valueOf(attribute.getID());
                 Set<String> attrValues = getSaveAttributeValues().get(attrId);
                 if (attrValues == null || attrValues.isEmpty() || StringUtils.isBlank(attrValues.iterator().next())) {
                     addGlobalValidationError(attribute.getShortName() + " is missing!");
                 }
             }
         }
     }
 
     /**
      *
      * @throws IOException
      * @throws ServiceException
      * @throws DAOException
      * @throws SAXException
      * @throws ParserConfigurationException
      */
     @ValidationMethod(on = {"reupload"}, priority = 2)
     public void validateReupload() throws IOException, ServiceException, DAOException, ParserConfigurationException, SAXException {
 
         LOGGER.trace("Method entered");
 
         if (uploadedFile == null) {
             addGlobalValidationError("No file uploaded!");
         } else if (uploadedFile.getSize() <= 0) {
             addGlobalValidationError("Uploaded file must not be empty!");
         }
 
         if (!isValidationErrors()) {
             if (schema == null || schema.getId() <= 0) {
                 addGlobalValidationError("Schema id missing!");
             }
         }
 
         if (isValidationErrors()) {
             loadSchemaById();
             loadSchemaString();
            if (!workingCopy) {
                xmlConvData = xmlConvService.getSchemaConversionsData(getSchemaUrl());
            }
             getContext().setSourcePageResolution(new ForwardResolution(VIEW_SCHEMA_JSP));
         }
     }
 
     /**
      *
      * @throws ServiceException
      */
     private void loadSchemaByName() throws ServiceException {
         if (schemaSet.getIdentifier().equals(SchemaSet.ROOT_IDENTIFIER)) {
             schema = schemaService.getRootLevelSchema(schema.getFileName(), workingCopy);
         } else {
             schema = schemaService.getSchema(schemaSet.getIdentifier(), schema.getFileName(), workingCopy);
         }
         if (schema != null && !isRootLevelSchema()) {
             schemaSet = schemaService.getSchemaSet(schema.getSchemaSetId());
             if (schemaSet != null){
                 schema.setSchemaSetId(schemaSet.getId());
                 schema.setSchemaSetIdentifier(schemaSet.getIdentifier());
                 schema.setSchemaSetNameAttribute(schemaSet.getNameAttribute());
                 schema.setSchemaSetWorkingCopy(schemaSet.isWorkingCopy());
                 schema.setSchemaSetWorkingUser(schemaSet.getWorkingUser());
             }
         }
     }
 
     /**
      *
      * @throws ServiceException
      */
     private void loadSchemaById() throws ServiceException {
         schema = schemaService.getSchema(schema.getId());
         if (schema != null && !isRootLevelSchema()) {
             schemaSet = schemaService.getSchemaSet(schema.getSchemaSetId());
         }
     }
 
     /**
      * Loads schema contents to string.
      *
      * @throws ServiceException
      */
     private void loadSchemaString() throws ServiceException {
         if (schemaSet.getIdentifier().equals(SchemaSet.ROOT_IDENTIFIER)) {
             schemaString = schemaRepository.getSchemaString(schema.getFileName(), null, workingCopy);
         } else {
             schemaString = schemaRepository.getSchemaString(schema.getFileName(), schemaSet.getIdentifier(), workingCopy);
         }
     }
 
     /**
      *
      * @return
      */
     private Map<Integer, Set<String>> getSaveAttributeValues() {
 
         if (saveAttributeValues == null) {
 
             saveAttributeValues = new HashMap<Integer, Set<String>>();
 
             HttpServletRequest request = getContext().getRequest();
             Enumeration paramNames = request.getParameterNames();
             if (paramNames != null && paramNames.hasMoreElements()) {
                 do {
                     String paramName = paramNames.nextElement().toString();
                     Integer attributeId = null;
                     if (paramName.startsWith(DElemAttribute.REQUEST_PARAM_MULTI_PREFIX)) {
                         attributeId =
                             Integer.valueOf(StringUtils.substringAfter(paramName, DElemAttribute.REQUEST_PARAM_MULTI_PREFIX));
                     } else if (paramName.startsWith(DElemAttribute.REQUEST_PARAM_PREFIX)) {
                         attributeId = Integer.valueOf(StringUtils.substringAfter(paramName, DElemAttribute.REQUEST_PARAM_PREFIX));
                     }
 
                     if (attributeId != null) {
                         String[] paramValues = request.getParameterValues(paramName);
                         LinkedHashSet<String> valueSet = new LinkedHashSet<String>();
                         for (int i = 0; i < paramValues.length; i++) {
                             valueSet.add(paramValues[i]);
                         }
                         saveAttributeValues.put(attributeId, valueSet);
                     }
                 } while (paramNames.hasMoreElements());
             }
         }
         return saveAttributeValues;
     }
 
     /**
      *
      * @return
      * @throws DAOException
      */
     public LinkedHashMap<Integer, DElemAttribute> getAttributes() throws DAOException {
 
         if (attributes == null) {
 
             DDSearchEngine searchEngine = null;
             try {
                 searchEngine = DDSearchEngine.create();
                 int schemaId = schema == null ? 0 : schema.getId();
 
                 attributes =
                     searchEngine.getObjectAttributes(schemaId, DElemAttribute.ParentType.SCHEMA, DElemAttribute.TYPE_SIMPLE);
 
                 // If this is a POST request where new attribute values are submitted (e.g. "save", "add", etc)
                 // then substitute the values we got from database with the values
                 // we got from the request.
                 if (!isGetOrHeadRequest()) {
                     String eventName = getContext().getEventName();
                     if (eventName.equals("add") || eventName.equals("save") || eventName.equals("saveAndClose")) {
 
                         for (Map.Entry<Integer, Set<String>> savedAttrEntry : getSaveAttributeValues().entrySet()) {
                             int attrId = savedAttrEntry.getKey();
                             DElemAttribute attributeObj = attributes.get(attrId);
                             if (attributeObj != null) {
                                 attributeObj.nullifyValues();
                                 Set<String> savedValues = savedAttrEntry.getValue();
                                 for (String savedValue : savedValues) {
                                     attributeObj.setValue(savedValue);
                                 }
                             }
                         }
                     }
                 }
             } finally {
                 DDSearchEngine.close(searchEngine);
             }
         }
         return attributes;
     }
 
     /**
      * @return the schema
      */
     public Schema getSchema() {
         return schema;
     }
 
     /**
      * @param schema
      *            the schema to set
      */
     public void setSchema(Schema schema) {
         this.schema = schema;
     }
 
     /**
      * @param schemaSet
      *            the schemaSet to set
      */
     public void setSchemaSet(SchemaSet schemaSet) {
         this.schemaSet = schemaSet;
     }
 
     /**
      *
      * @return
      */
     public boolean isRootLevelSchema() {
         return schema != null && schema.getSchemaSetId() <= 0;
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public boolean isMySchemaSetWorkingCopy() throws ServiceException {
 
         boolean result = false;
         if (schemaSet != null && schemaSet.isWorkingCopy() && StringUtils.equals(schemaSet.getWorkingUser(), getUserName())) {
             result = true;
         }
 
         return result;
     }
 
     /**
      *
      * @return
      */
     public boolean isUserWorkingCopy() {
 
         boolean result = false;
         String sessionUser = getUserName();
         if (isRootLevelSchema() && !StringUtils.isBlank(sessionUser)) {
             String workingUser = schema.getWorkingUser();
             return schema.isWorkingCopy() && StringUtils.equals(workingUser, sessionUser);
         }
 
         return result;
     }
 
     /**
      *
      * @return
      */
     public boolean isCheckInCommentsRequired() {
         String checkInCommentsRequired = Props.getProperty(PropsIF.CHECK_IN_COMMENTS_REQUIRED);
         return checkInCommentsRequired != null && checkInCommentsRequired.trim().equalsIgnoreCase("true");
     }
 
     /**
      * @return the schemaSet
      * @throws ServiceException
      */
     public SchemaSet getSchemaSet() throws ServiceException {
         return schemaSet;
     }
 
     /**
      * @return the possibleAttributeValues
      * @throws DAOException
      */
     public Map<String, Set<String>> getMultiValuedAttributeValues() throws DAOException {
 
         if (multiValuedAttributeValues == null) {
             multiValuedAttributeValues = new HashMap<String, Set<String>>();
             LinkedHashMap<Integer, DElemAttribute> attributeMap = getAttributes();
             DDSearchEngine searchEngine = null;
             try {
                 searchEngine = DDSearchEngine.create();
                 for (Map.Entry<Integer, DElemAttribute> entry : attributeMap.entrySet()) {
 
                     int attributeId = entry.getKey();
                     DElemAttribute attribute = entry.getValue();
 
                     if (attribute.isMultipleValuesAllowed()) {
 
                         Vector existingValues = attribute.getValues();
                         Collection possibleValues = null;
                         if (StringUtils.equals(attribute.getDisplayType(), "select")) {
                             possibleValues = getFixedValuedAttributeValues().get(attributeId);
                         } else {
                             possibleValues = searchEngine.getSimpleAttributeValues(String.valueOf(attributeId));
                         }
 
                         LinkedHashSet<String> values = new LinkedHashSet<String>();
                         if (existingValues != null) {
                             values.addAll(existingValues);
                         }
                         if (possibleValues != null) {
                             values.addAll(possibleValues);
                         }
                         multiValuedAttributeValues.put(String.valueOf(attributeId), values);
                     }
                 }
             } catch (SQLException e) {
                 throw new DAOException(e.getMessage(), e);
             } finally {
                 DDSearchEngine.close(searchEngine);
             }
         }
         return multiValuedAttributeValues;
     }
 
     /**
      * @return the fixedValuedAttributeValues
      * @throws DAOException
      */
     public Map<String, Set<String>> getFixedValuedAttributeValues() throws DAOException {
 
         if (fixedValuedAttributeValues == null) {
             fixedValuedAttributeValues = new HashMap<String, Set<String>>();
             DDSearchEngine searchEngine = null;
             try {
                 searchEngine = DDSearchEngine.create();
                 for (Map.Entry<Integer, DElemAttribute> entry : getAttributes().entrySet()) {
 
                     int attributeId = entry.getKey();
                     DElemAttribute attribute = entry.getValue();
 
                     if (StringUtils.equals(attribute.getDisplayType(), "select")) {
 
                         LinkedHashSet<String> values = new LinkedHashSet<String>();
                         Collection<FixedValue> fixedValues = searchEngine.getFixedValues(attribute.getID(), "attr");
                         for (FixedValue fxv : fixedValues) {
                             String value = fxv.getValue();
                             if (value != null) {
                                 values.add(value);
                             }
                         }
                         fixedValuedAttributeValues.put(String.valueOf(attributeId), values);
                     }
                 }
             } catch (SQLException e) {
                 throw new DAOException(e.getMessage(), e);
             } finally {
                 DDSearchEngine.close(searchEngine);
             }
         }
         return fixedValuedAttributeValues;
     }
 
     /**
      * @param uploadedFile
      *            the uploadedFile to set
      */
     public void setUploadedFile(FileBean uploadedFile) {
         this.uploadedFile = uploadedFile;
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      * @throws IOException
      */
     public String getSchemaDownloadLink() throws ServiceException, IOException {
 
         if (schema == null) {
             throw new IllegalStateException("Schema object must be initialized!");
         }
 
         // boolean isMyWorkingCopy = getUserName() != null && schema.isWorkingCopyOf(getUserName());
 
         String schemaSetIdentifier = null;
         if (schemaSet != null && !schemaSet.getIdentifier().equals(SchemaSet.ROOT_IDENTIFIER)) {
             schemaSetIdentifier = schemaSet.getIdentifier();
         }
 
         String relPath = schemaRepository.getSchemaRelativePath(schema.getFileName(), schemaSetIdentifier, workingCopy);
         return getContextPath() + DownloadServlet.SCHEMAS_PATH + "/" + relPath;
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      * @throws IOException
      */
     public String getSchemaUrl() throws ServiceException, IOException {
 
         if (schema == null) {
             throw new IllegalStateException("Schema object must be initialized!");
         }
 
         if (schema.isWorkingCopy()) {
             throw new UnsupportedOperationException("Method not supported for working copies!");
         }
 
         String webAppUrl = Props.getRequiredProperty(PropsIF.DD_URL);
         if (webAppUrl.endsWith("/")) {
             StringUtils.substringBeforeLast(webAppUrl, "/");
         }
 
         String schemaSetIdentifier = null;
         if (schemaSet != null && !schemaSet.getIdentifier().equals(SchemaSet.ROOT_IDENTIFIER)) {
             schemaSetIdentifier = schemaSet.getIdentifier();
         }
         String relPath = schemaRepository.getSchemaRelativePath(schema.getFileName(), schemaSetIdentifier, workingCopy);
 
         return webAppUrl + DownloadServlet.SCHEMAS_PATH + "/" + relPath;
     }
 
     /**
      * @return the otherVersions
      * @throws ServiceException
      */
     public List<Schema> getOtherVersions() throws ServiceException {
 
         if (!isRootLevelSchema()) {
             throw new UnsupportedOperationException("Operation relevant for root-level schemas only!");
         }
 
         if (otherVersions == null) {
 
             if (schema == null) {
                 throw new IllegalStateException("The schema must be loaded for this operation!");
             }
 
             otherVersions = schemaService.getSchemaVersions(getUserName(), schema.getContinuityId(), schema.getId());
         }
         return otherVersions;
     }
 
     /**
      * Returns true if this is a root-level schema, and the user is allowed to do its check-out. Otherwise returns false.
      *
      * @return
      */
     public boolean isCheckoutAllowed() {
 
         if (!isRootLevelSchema()) {
             return false;
         }
 
         try {
             if (SecurityUtil.hasPerm(getUserName(), "/schemas", "er")) {
                 return true;
             } else {
                 return !schema.isReleased() && SecurityUtil.hasPerm(getUserName(), "/schemas", "u");
             }
         } catch (Exception e) {
             LOGGER.error(e.getMessage(), e);
             return false;
         }
     }
 
     /**
      * Returns true if the current user is allowed to create new root-level schemas. Otherwise returns false.
      *
      * @return
      */
     public boolean isCreateAllowed() {
 
         if (getUser() != null) {
             try {
                 return SecurityUtil.hasPerm(getUserName(), "/schemas", "i");
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
             }
         }
         return false;
     }
 
     /**
      *
      * @return
      * @throws ServiceException
      */
     public Schema getSchemaWorkingCopy() throws ServiceException {
 
         if (schema == null) {
             throw new IllegalStateException("The schema object must be initialized for this operation!");
         } else if (!isUserLoggedIn() || !schema.isCheckedOutBy(getUserName())) {
             return null;
         } else {
             return schemaService.getWorkingCopyOfSchema(schema.getId());
         }
     }
 
     /**
      * @return the workingCopy
      */
     public boolean isWorkingCopy() {
         return workingCopy;
     }
 
     /**
      * @param workingCopy
      *            the workingCopy to set
      */
     public void setWorkingCopy(boolean workingCopy) {
         this.workingCopy = workingCopy;
     }
 
     /**
      * @return the schemaString
      */
     public String getSchemaString() {
         return schemaString;
     }
 
     /**
      * @return the xmlConvData
      */
     public SchemaConversionsData getXmlConvData() {
         return xmlConvData;
     }
 
 }
