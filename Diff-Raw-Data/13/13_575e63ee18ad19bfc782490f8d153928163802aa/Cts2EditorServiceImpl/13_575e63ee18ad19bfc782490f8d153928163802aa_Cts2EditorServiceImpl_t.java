 package mayo.edu.cts2.editor.server;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URI;
 import java.util.Calendar;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.transform.stream.StreamSource;
 
 import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry;
 import mayo.edu.cts2.editor.client.Cts2EditorService;
 import mayo.edu.cts2.editor.server.rest.Cts2Client;
 import mayo.edu.cts2.editor.server.rest.EntityClient;
 import mayo.edu.cts2.editor.shared.CTS2Result;
 import mayo.edu.cts2.editor.shared.Definition;
 import mayo.edu.cts2.editor.shared.DefinitionEntry;
 
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.resteasy.client.ProxyFactory;
 import org.jboss.resteasy.util.Base64;
 
 import edu.mayo.cts2.framework.core.client.Cts2RestClient;
 import edu.mayo.cts2.framework.core.constants.URIHelperInterface;
 import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller;
 import edu.mayo.cts2.framework.model.core.ChangeableElementGroup;
 import edu.mayo.cts2.framework.model.core.Comment;
 import edu.mayo.cts2.framework.model.core.RoleReference;
 import edu.mayo.cts2.framework.model.core.SourceAndNotation;
 import edu.mayo.cts2.framework.model.core.SourceAndRoleReference;
 import edu.mayo.cts2.framework.model.core.SourceReference;
 import edu.mayo.cts2.framework.model.core.TsAnyType;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.core.ValueSetReference;
 import edu.mayo.cts2.framework.model.core.VersionTagReference;
 import edu.mayo.cts2.framework.model.core.types.FinalizableState;
 import edu.mayo.cts2.framework.model.core.types.NoteType;
 import edu.mayo.cts2.framework.model.core.types.SetOperator;
 import edu.mayo.cts2.framework.model.service.core.UpdateChangeSetMetadataRequest;
 import edu.mayo.cts2.framework.model.service.core.UpdatedChangeInstructions;
 import edu.mayo.cts2.framework.model.service.core.UpdatedCreator;
 import edu.mayo.cts2.framework.model.service.core.UpdatedState;
 import edu.mayo.cts2.framework.model.util.ModelUtils;
 import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionMsg;
 
 /**
  * The server side implementation of the RPC service.
  */
 public class Cts2EditorServiceImpl extends BaseEditorServlet implements Cts2EditorService {
 
 	private static final long serialVersionUID = 5L;
 	private static final String XML_ROOT = "ValueSetCatalogEntryMsgList";
 	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
 	private static final String XML_ROOT_START = "<" + XML_ROOT + ">\n";
 	private static final String XML_ROOT_END = "</" + XML_ROOT + ">\n";
 	private static Logger logger = Logger.getLogger(Cts2EditorServiceImpl.class.getName());
 
 	public static final String XPATH_VALUESETS_BASE = "/" + XML_ROOT;
 
 	private final int MAX_RECORDS = Cts2EditorServiceProperties.getValueSetRestPageSize();
 
 	public Cts2EditorServiceImpl() {
 		super();
 	}
 
 	/**
 	 * Returns the xml representation of the value set
 	 * 
 	 * @param oid
 	 *            id of the value set to return
 	 * @return the xml representation of the value set
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getValueSet(String oid) throws IllegalArgumentException {
 		if (oid == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		return getCts2Client().getValueSet(getAuthorizationHeader(), oid);
 	}
 
 	/**
 	 * Returns the xml representation of the value sets in <code>oids</code>
 	 * 
 	 * @param oids
 	 *            ids of the value sets to fetch
 	 * @return xml representation of the value sets in <code>oids</code>
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getValueSets(List<String> oids) throws IllegalArgumentException {
 		if (oids == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 
 		/*
 		 * For each OID in the list, return the XML representation of the value
 		 * sets.
 		 */
 		StringBuilder sb = new StringBuilder(XML_HEADER + XML_ROOT_START);
 
 		for (String oid : oids) {
 			String xmlResponse = getCts2Client().getValueSet(getAuthorizationHeader(), oid);
 			int end = xmlResponse.indexOf(XML_HEADER) + XML_HEADER.length();
 			xmlResponse = xmlResponse.substring(end);
 			sb.append(xmlResponse);
 		}
 		sb.append(XML_ROOT_END);
 
 		return sb.toString();
 	}
 
 	/**
 	 * Returns the current definition for the value set
 	 * 
 	 * @param oid
 	 *            id of the value set
 	 * @return the current definition for the value set
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getValueSetDefinition(String oid) throws IllegalArgumentException {
 		if (oid == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		return getValueSetDefinition(oid, "1");
 	}
 
 	/**
 	 * Returns the value set definition for the value set oid and definition
 	 * version
 	 * 
 	 * @param oid
 	 *            id of the value set
 	 * @param version
 	 *            id of the version
 	 * @return value set definition matching the value set oid and definition
 	 *         version
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	public String getValueSetDefinition(String oid, String version) throws IllegalArgumentException {
 		if (oid == null || version == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		return getCts2Client().getValueSetDefinition(getAuthorizationHeader(), oid, version);
 	}
 
 	/**
 	 * <<<<<<< HEAD Returns the entities for the current version of the value
 	 * set
 	 * 
 	 * @param oid
 	 *            id of the value set to fetch the current version for =======
 	 *            Returns the entities for the value set. If version is
 	 *            <code>null</code> returns the entities for the current
 	 *            version. If the changeSetUri is <code>null</code> returns the
 	 *            enitites with no change set applied.
 	 * 
 	 * @param oid
 	 *            id of the value set to fetch
 	 * @param version
 	 *            of the value set definition
 	 * @param changeSetUri
 	 *            id of the change set to apply >>>>>>>
 	 *            refs/remotes/choose_remote_name/master
 	 * @return the entities for the current version <<<<<<< HEAD
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code> =======
 	 * @throws IllegalArgumentException
 	 *             if oid argument is <code>null</code> >>>>>>>
 	 *             refs/remotes/choose_remote_name/master
 	 */
 	@Override
 	public String getResolvedValueSet(String oid, String version, String changeSetUri) throws IllegalArgumentException {
 		if (oid == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		version = version == null || version.equals("") ? "1" : version;
 
 		if (changeSetUri == null || changeSetUri.equals("")) {
 			return getCts2Client().getResolvedValueSet(getAuthorizationHeader(), oid, version, MAX_RECORDS);
 		} else {
 			return getCts2Client().getResolvedValueSet(getAuthorizationHeader(), oid, version, changeSetUri,
 			        MAX_RECORDS);
 		}
 	}
 
 	/**
 	 * Returns all the definitions of a value set
 	 * 
 	 * @param oid
 	 *            id of the value set to fetch the definitions for
 	 * @return the definitions of the value set
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getDefinitions(String oid) throws IllegalArgumentException {
 		if (oid == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		return getCts2Client().getDefinitions(getAuthorizationHeader(), oid, MAX_RECORDS);
 	}
 
 	/**
 	 * Returns value sets matching the match value criteria
 	 * 
 	 * @param matchValue
 	 * @return value sets matching the match value criteria
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getMatchingValueSets(String matchValue) throws IllegalArgumentException {
 		if (matchValue == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		return getCts2Client().getValueSets(getAuthorizationHeader(), MAX_RECORDS, matchValue);
 	}
 
 	/**
 	 * Creates a change set with the CTS2 service
 	 * 
 	 * @return the id of the created change set
 	 */
 	@Override
 	public String createChangeSet() {
 		ClientResponse<String> response = getCts2Client().createChangeSet(getAuthorizationHeader());
 		String uri = null;
 		if (response.getStatus() == 201) {
 			uri = response.getHeaders().get("location").get(0);
 			uri = uri.substring(uri.lastIndexOf("/") + 1);
 		}
 		response.releaseConnection();
 		return uri;
 	}
 
 	/**
 	 * Marks a change set for deletion with the CTS2 service
 	 * 
 	 * @param uri
 	 *            id of the change set to marked for deletion
 	 * @return the updated change set marked for deletion
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String deleteChangeSet(String uri) throws IllegalArgumentException {
 		if (uri == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		return getCts2Client().deleteChangeSet(getAuthorizationHeader(), uri);
 	}
 
 	/**
 	 * Returns the change set.
 	 * 
 	 * @param uri
 	 *            id of the change set
 	 * @return the change set
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getChangeSet(String uri) throws IllegalArgumentException {
 		return getCts2Client().getChangeSet(getAuthorizationHeader(), uri);
 	}
 
 	/**
 	 * Returns the xml representation of the matching entities
 	 * 
 	 * @param matchValue
 	 * @return the xml representation of the matching entities
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getMatchingEntities(String matchValue) throws IllegalArgumentException {
 		if (matchValue == null) {
 			throw new IllegalArgumentException("Argument can not be null.");
 		}
 		return getEntityClient().getMatchingEntities(getAuthorizationHeader(), MAX_RECORDS, matchValue);
 	}
 
 	/**
 	 * 
 	 * @param definition
 	 *            to save
 	 * @return the results
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public CTS2Result saveDefinition(Definition definition) throws IllegalArgumentException {
 		if (definition == null)
 			throw new IllegalArgumentException("Argument can not be null.");
 
 		CTS2Result result = new CTS2Result();
 		if (isFinal(definition)) {
 			result.setError(true);
 			result.setMessage("The value set definition is final and can not be updated.");
 		} else {
 			result = saveValueSet(toValueSetDefinition(definition));
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param definition
 	 *            to clone and save
 	 * @return the results
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public CTS2Result saveDefinitionAs(Definition definition) throws IllegalArgumentException {
 		if (definition == null)
 			throw new IllegalArgumentException("Argument can not be null.");
 
 		/* Set new random version */
 		definition.setVersion(UUID.randomUUID().toString());
 		CTS2Result result = saveValueSetAs(toValueSetDefinition(definition));
 		return result;
 	}
 
 	/**
 	 * Returns the definitions for a value set by a specific creator.
 	 * 
 	 * @param oid
 	 *            id of the value set
 	 * @param username
 	 *            id of the creator
 	 * @return the xml representation of user's definitions
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getUserDefinitions(String oid, String username) throws IllegalArgumentException {
 		if (oid == null || username == null)
 			throw new IllegalArgumentException("Arguments can not be null.");
 
 		return getCts2Client().getUserDefinitions(getAuthorizationHeader(), oid, "creator", username, 5000);
 	}
 
 	/**
 	 * Returns the definitions for a list of value sets by a specific creator.
 	 * 
 	 * @param oids
 	 *            list of ids that represent the value set
 	 * @param username
 	 *            id of the creator
 	 * @return the xml representation of user's definitions
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getUserDefinitions(List<String> oids, String username) throws IllegalArgumentException {
 		if (oids == null || username == null)
 			throw new IllegalArgumentException("Arguments can not be null.");
 
 		String test = "";
 
 		for (String oid : oids) {
 			test = getCts2Client().getUserDefinitions(getAuthorizationHeader(), oid, "creator", username, 5000);
 			// System.out.print(test);
 		}
 
 		return test;
 	}
 
 	/**
 	 * Returns a specific value set definition.
 	 * 
 	 * @param oid
 	 *            id of the value set
 	 * @param changeSetUri
 	 *            id of the change set to apply
 	 * @return the value set definition version
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public String getDefinition(String oid, String version, String changeSetUri) throws IllegalArgumentException {
 		if (oid == null || version == null)
 			throw new IllegalArgumentException("Oid and version can not be null.");
 		if (changeSetUri == null || changeSetUri.equals("")) {
 			return getCts2Client().getDefinition(getAuthorizationHeader(), oid, version);
 		} else {
 			return getCts2Client().getDefinition(getAuthorizationHeader(), oid, version, changeSetUri);
 		}
 	}
 
 	/**
 	 * Updates the change set metadata
 	 * 
 	 * @param uri
 	 *            id of the change set
 	 * @param creator
 	 *            updated creator
 	 * @param changeInstructions
 	 *            updated change instructions
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public void updateChangeSet(String uri, String creator, String changeInstructions) throws IllegalArgumentException {
 		if (uri == null || creator == null || changeInstructions == null)
 			throw new IllegalArgumentException("Arguments can not be null.");
 
 		UpdateChangeSetMetadataRequest metadata = new UpdateChangeSetMetadataRequest();
 
 		UpdatedCreator updatedCreator = new UpdatedCreator();
 		updatedCreator.setCreator(ModelUtils.nameOrUriFromName(creator));
 		metadata.setUpdatedCreator(updatedCreator);
 
 		UpdatedChangeInstructions updatedChangeInstructions = new UpdatedChangeInstructions();
 		updatedChangeInstructions.setChangeInstructions(ModelUtils.createOpaqueData(changeInstructions));
 		metadata.setUpdatedChangeInstructions(updatedChangeInstructions);
 
 		UpdatedState state = new UpdatedState();
 		state.setState(FinalizableState.OPEN);
 		metadata.setUpdatedState(state);
 
 		Cts2RestClient restClient = Cts2RestClient.instance();
 		restClient.postCts2Resource(Cts2EditorServiceProperties.getValueSetDefinitionMaintenanceUrl() + "/changeset/"
 		        + uri, Cts2EditorServiceProperties.getCts2ValueSetRestUsername(),
 		        Cts2EditorServiceProperties.getCts2ValueSetRestPassword(), metadata);
 	}
 
 	/**
 	 * Returns <code>true</code> if the definition version is marked FINAL.
 	 * Returns <code>false</code> if the definition version is not marked FINAL.
 	 * 
 	 * @param definition
 	 * @return <code>true</code> if the definition version is marked FINAL,
 	 *         <code>false</code> if the definition version is not marked FINAL
 	 * @throws IllegalArgumentException
 	 *             if any argument is <code>null</code>
 	 */
 	@Override
 	public boolean isFinal(Definition definition) throws IllegalArgumentException {
 		if (definition == null)
 			throw new IllegalArgumentException("Arguments can not be null.");
 
 		String definitionXml = getCts2Client().getDefinition(getAuthorizationHeader(), definition.getValueSetOid(),
 		        definition.getVersion(), definition.getChangeSetUri());
 		ValueSetDefinition vsDefinition = unmarshallValueSetDefinition(definitionXml);
 		return vsDefinition.getState() == FinalizableState.FINAL;
 	}
 
 	private ValueSetDefinition toValueSetDefinition(Definition definition) {
 		ValueSetDefinition vsd = new ValueSetDefinition();
 
 		vsd.setDefinedValueSet(new ValueSetReference(definition.getValueSetOid()));
 		vsd.setDocumentURI(definition.getDocumentUri() == null || definition.getDocumentUri().trim().equals("") ? UUID
 		        .randomUUID().toString() : definition.getDocumentUri());
 		vsd.setAbout(definition.getValueSetOid());
 
 		VersionTagReference[] references = new VersionTagReference[]{new VersionTagReference(definition.getVersion())};
 		vsd.setVersionTag(references);
 		vsd.setState(FinalizableState.OPEN);
 		vsd.setOfficialReleaseDate(Calendar.getInstance().getTime());
 
 		ChangeableElementGroup group = new ChangeableElementGroup();
 		vsd.setChangeableElementGroup(group);
 
 		SourceAndRoleReference snrr = new SourceAndRoleReference();
 		SourceReference sourceReference = new SourceReference();
 		sourceReference.setContent(definition.getCreator());
 		RoleReference roleReference = new RoleReference();
 		roleReference.setContent("creator");
 		roleReference.setUri("http://purl.org/dc/elements/1.1/creator");
 		snrr.setSource(sourceReference);
 		snrr.setRole(roleReference);
 		SourceAndRoleReference[] sourceAndRoleReferences = new SourceAndRoleReference[]{snrr};
 		vsd.setSourceAndRole(sourceAndRoleReferences);
 
 		Comment comment = new Comment();
 		comment.setType(NoteType.EDITORIALNOTE);
 		TsAnyType anyType = new TsAnyType();
 		anyType.setContent(definition.getNote());
 		comment.setValue(anyType);
 		vsd.setNote(new Comment[]{comment});
 
 		SourceAndNotation snn = new SourceAndNotation();
 		snn.setSourceDocument("");
 		vsd.setSourceAndNotation(snn);
 
 		edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry entry = new edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry();
 		entry.setOperator(SetOperator.UNION);
 		entry.setEntryOrder(1L);
 		entry.setEntityList(createEntityList(definition.getEntries()));
 		vsd.setEntry(new edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry[]{entry});
 
 		return vsd;
 	}
 
 	private SpecificEntityList createEntityList(List<DefinitionEntry> definitionEntries) {
 		SpecificEntityList entityList = new SpecificEntityList();
 
 		for (DefinitionEntry definitionEntry : definitionEntries) {
 			URIAndEntityName entity = new URIAndEntityName();
 			entity.setName(definitionEntry.getName());
 			entity.setNamespace(definitionEntry.getNamespace());
 			entity.setUri(definitionEntry.getUri());
 			entity.setHref(definitionEntry.getHref());
 			/* TODO: Need to set the description in the service, waiting for the updated spec:
 			 * https://github.com/cts2/cts2-specification/issues/143 */
 //			entity.setDesignation(definitionEntry.getDescription());
 			entityList.addReferencedEntity(entity);
 		}
 
 		return entityList;
 	}
 
 	private CTS2Result saveValueSet(ValueSetDefinition definition) {
 		CTS2Result result = new CTS2Result();
 
 		String changeSetUri = createChangeSet();
 		if (changeSetUri != null) {
 			updateChangeSet(changeSetUri, definition.getSourceAndRole(0).getSource().getContent(), definition
 			        .getNote(0).getValue().getContent());
 			try {
 				if (saveToService(definition, changeSetUri)) {
 					result.setChangeSetUri(changeSetUri);
 					result.setValueSetOid(definition.getDefinedValueSet().getContent());
 					result.setValueSetVersion(definition.getVersionTag(0).getContent());

					String doc = getCts2Client().getDefinition(getAuthorizationHeader(), result.getValueSetOid(), result.getValueSetVersion(), result.getChangeSetUri());
					String start = "documentURI=\"";
					int startIdx = doc.indexOf(start) + start.length();
					doc = doc.substring(startIdx, startIdx + 37);
					result.setDocumentUri(doc);
 				}
 			} catch (Exception e) {
 				logger.log(Level.WARNING, "Unable to persist the value set definition.", e);
 				result.setError(true);
 				result.setMessage("Failed to persist the definition. Error: " + e.getMessage());
 			}
 		} else {
 			result.setError(true);
 			result.setMessage("An error occurred while creating the change set.");
 		}
 		return result;
 	}
 
 	private CTS2Result saveValueSetAs(ValueSetDefinition definition) {
 		CTS2Result result = new CTS2Result();
 		String changeSetUri = createChangeSet();
 
 		if (changeSetUri != null) {
 			updateChangeSet(changeSetUri, definition.getSourceAndRole(0).getSource().getContent(), definition
 			        .getNote(0).getValue().getContent());
 
 			try {
 				if (saveAsToService(definition, changeSetUri)) {
 					result.setChangeSetUri(changeSetUri);
 					result.setValueSetOid(definition.getDefinedValueSet().getContent());
 					result.setDocumentUri(definition.getDocumentURI());
 					result.setValueSetVersion(definition.getVersionTag(0).getContent());
 				} else {
 					result.setError(true);
 					result.setMessage("An error occurred while saving the definition to the service.");
 				}
 			} catch (Exception e) {
 				logger.log(Level.WARNING, "Unable to persist the value set definition.", e);
 				result.setError(true);
 				result.setMessage("Failed to persist the definition. Error: " + e.getMessage());
 			}
 		} else {
 			result.setError(true);
 			result.setMessage("An error occurred while creating the change set.");
 		}
 		return result;
 	}
 
 	private boolean saveToService(ValueSetDefinition definition, String changeSetUri) throws Exception {
 		/*
 		 * "PUT
 		 * /valueset/{oid}/definition/{version}?changesetcontext={changeSetUri}
 		 */
 		Cts2RestClient restClient = Cts2RestClient.instance();
 		String url = Cts2EditorServiceProperties.getValueSetDefinitionMaintenanceUrl() + "/valueset/"
 		        + definition.getDefinedValueSet().getContent() + "/definition/"
 		        + definition.getVersionTag(definition.getVersionTag().length - 1).getContent() + "?"
 		        + URIHelperInterface.PARAM_CHANGESETCONTEXT + "=" + changeSetUri;
 		restClient.putCts2Resource(url, Cts2EditorServiceProperties.getCts2ValueSetRestUsername(),
 		        Cts2EditorServiceProperties.getCts2ValueSetRestPassword(), definition);
 		return true;
 	}
 
 	private boolean saveAsToService(ValueSetDefinition definition, String changeSetUri) throws Exception {
		/*
		 * POST
		 * /valuesetdefinition?changesetcontext={changeSetUri}
		 */
 		Cts2RestClient restClient = Cts2RestClient.instance();
 		URI uri = restClient.postCts2Resource(Cts2EditorServiceProperties.getValueSetDefinitionMaintenanceUrl()
 		        + URIHelperInterface.PATH_VALUESETDEFINITION + "?" + URIHelperInterface.PARAM_CHANGESETCONTEXT + "="
 		        + changeSetUri, Cts2EditorServiceProperties.getCts2ValueSetRestUsername(),
 		        Cts2EditorServiceProperties.getCts2ValueSetRestPassword(), definition);
 		return uri != null;
 	}
 
 	private ValueSetDefinition unmarshallValueSetDefinition(String xml) {
 		DelegatingMarshaller marshaller = new DelegatingMarshaller();
 		StringReader reader = new StringReader(xml);
 		ValueSetDefinitionMsg message;
 		ValueSetDefinition definition = null;
 		try {
 			message = (ValueSetDefinitionMsg) marshaller.unmarshal(new StreamSource(reader));
 			if (message != null) {
 				definition = message.getValueSetDefinition();
 			}
 		} catch (IOException ioe) {
 			logger.warning("Unable to unmarshal the returned value set definition xml. Message: " + ioe.getMessage());
 		}
 		return definition;
 	}
 
 	private String getAuthorizationHeader() {
 		return "Basic "
 		        + Base64.encodeBytes((Cts2EditorServiceProperties.getCts2ValueSetRestUsername() + ":" + Cts2EditorServiceProperties
 		                .getCts2ValueSetRestPassword()).getBytes());
 	}
 
 	private Cts2Client getCts2Client() {
 		return ProxyFactory.create(Cts2Client.class, Cts2EditorServiceProperties.getValueSetDefinitionMaintenanceUrl());
 	}
 
 	private EntityClient getEntityClient() {
 		return ProxyFactory.create(EntityClient.class,
 		        Cts2EditorServiceProperties.getValueSetDefinitionMaintenanceEntitiesUrl());
 	}
 
 }
