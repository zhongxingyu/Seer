 package edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import javax.annotation.Resource;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 import edu.mayo.cts2.framework.core.client.Cts2RestClient;
 import edu.mayo.cts2.framework.core.config.ServerContext;
 import edu.mayo.cts2.framework.core.constants.URIHelperInterface;
 import edu.mayo.cts2.framework.core.url.UrlConstructor;
 import edu.mayo.cts2.framework.core.xml.Cts2Marshaller;
 import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntry;
 import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntryDirectory;
 import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntryMsg;
 import edu.mayo.cts2.framework.model.codesystemversion.CodeSystemVersionCatalogEntrySummary;
 import edu.mayo.cts2.framework.model.command.Page;
 import edu.mayo.cts2.framework.model.command.ResolvedFilter;
 import edu.mayo.cts2.framework.model.command.ResolvedReadContext;
 import edu.mayo.cts2.framework.model.command.ReturnContentFilter.PropertyType;
 import edu.mayo.cts2.framework.model.core.CodeSystemReference;
 import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference;
 import edu.mayo.cts2.framework.model.core.DescriptionInCodeSystem;
 import edu.mayo.cts2.framework.model.core.EntityReference;
 import edu.mayo.cts2.framework.model.core.NameAndMeaningReference;
 import edu.mayo.cts2.framework.model.core.ScopedEntityName;
 import edu.mayo.cts2.framework.model.core.URIAndEntityName;
 import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference;
 import edu.mayo.cts2.framework.model.core.ValueSetReference;
 import edu.mayo.cts2.framework.model.core.VersionTagReference;
 import edu.mayo.cts2.framework.model.core.types.CompleteDirectory;
 import edu.mayo.cts2.framework.model.directory.DirectoryResult;
 import edu.mayo.cts2.framework.model.entity.Designation;
 import edu.mayo.cts2.framework.model.entity.EntityDescription;
 import edu.mayo.cts2.framework.model.entity.EntityDescriptionBase;
 import edu.mayo.cts2.framework.model.entity.EntityDescriptionMsg;
 import edu.mayo.cts2.framework.model.entity.EntityReferenceMsg;
 import edu.mayo.cts2.framework.model.exception.UnspecifiedCts2Exception;
 import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition;
 import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI;
 import edu.mayo.cts2.framework.model.service.core.NameOrURI;
 import edu.mayo.cts2.framework.model.service.core.types.ActiveOrAll;
 import edu.mayo.cts2.framework.model.service.exception.UnknownValueSetDefinition;
 import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionDirectoryEntry;
 import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionMsg;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility.queryBuilders.CodeSystemVersionQueryBuilder;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.ctsUtility.queryBuilders.ValueSetDefinitionQueryBuilder;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.CodeSystemVersionCatalogEntryAndHref;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.EntityReferenceAndHref;
 import edu.mayo.cts2.framework.plugin.service.valueSetDefinitionResolutionServices.valueSetDefinitionResolutionImpl.utility.EntityReferenceResolver;
 import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
 import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQuery;
 import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionQueryService;
 import edu.mayo.cts2.framework.service.profile.codesystemversion.CodeSystemVersionReadService;
 import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionQueryService;
 import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService;
 import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId;
 import edu.mayo.cts2.framework.service.profile.valueset.ValueSetMaintenanceService;
 import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQuery;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQueryService;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService;
 import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId;
 import edu.mayo.cts2.framework.service.provider.ServiceProvider;
 
 @Component("valueSetDefinitionResolutionUtilities")
 public class Utilities
 {
 	@Resource 
 	protected ServerContext serverContext_;
 
 	@Resource 
 	protected Cts2Marshaller cts2Marshaller_;
 	
 	@Resource
 	private ServiceProvider serviceProvider;
 
 	@Value("#{valueSetDefinitionResolutionConfigProperties.getProperty('valueSetServiceRootURL') ?: ''}") 
 	protected String valueSetServiceRootURL_;
 
 	@Value("#{valueSetDefinitionResolutionConfigProperties.getProperty('valueSetDefinitionServiceRootURL') ?: ''}") 
 	protected String valueSetDefinitionServiceRootURL_;
 
 	@Value("#{valueSetDefinitionResolutionConfigProperties.getProperty('codeSystemAndEntitiesServicesRootURL') ?: ''}") 
 	protected String codeSystemAndEntitiesServicesRootURL_;
 
 	private UrlConstructor urlConstructor_;
 
 	private Cts2RestClient cts2RestClient_;
 
 	protected final Logger logger_ = LoggerFactory.getLogger(this.getClass());
 
 	public void updateValueSetForReturn(ValueSetCatalogEntry valueSetCatalogEntry, String valueSetHref, ResolvedReadContext readContext)
 	{
 		logger_.debug("updateValueSetForReturn {} {}", valueSetCatalogEntry, valueSetHref);
 		valueSetCatalogEntry.setCurrentDefinition(lookupCurrentValueSetDefinitionReference(valueSetCatalogEntry.getAbout(), valueSetHref,
 				valueSetCatalogEntry.getValueSetName(), readContext));
 		valueSetCatalogEntry.setDefinitions(getUrlConstructor().createValueSetUrl(valueSetCatalogEntry.getValueSetName()) + "/definitions");
 	}
 
 	public ValueSetDefinitionReference lookupCurrentValueSetDefinitionReference(final String valueSetURI, String valueSetHref, String valueSetName,
 			final ResolvedReadContext readContext)
 	{
 		logger_.debug("lookupCurrentValueSetDefinitionReference for  valueSetURI {} valueSetHref {} valueSetName{}", valueSetURI, valueSetHref, valueSetName);
 
 		// we will assume that the ValueSetDefininitionResolutionService is on this system - otherwise, they probably won't be using these stub
 		// implementations of the ValueSet code - no need to look for an external service.
 		ValueSetDefinitionQueryService vsdqr = getLocalValueSetDefinitionQueryService();
 
 		ValueSetDefinitionQuery vsdQuery = ValueSetDefinitionQueryBuilder.build(readContext);
 		NameOrURI valueSet = new NameOrURI();
 		valueSet.setUri(valueSetURI);
 		vsdQuery.getRestrictions().setValueSet(valueSet);
 		
 		DirectoryResult<ValueSetDefinitionDirectoryEntry> definitions = vsdqr.getResourceSummaries(vsdQuery, null, null);
 		logger_.debug("Query returned {} results", definitions.getEntries().size());
 
 		ValueSetDefinitionDirectoryEntry vsdde = null;
 		for (ValueSetDefinitionDirectoryEntry temp : definitions.getEntries())
 		{
 			if (vsdde != null)
 			{
 				break;
 			}
 			for (VersionTagReference version : temp.getVersionTag())
 			{
 				if (version.getContent().equals(URIHelperInterface.DEFAULT_TAG))
 				{
 					vsdde = temp;
 					break;
 				}
 			}
 		}
 
 		if (vsdde != null)
 		{
 			ValueSetDefinitionReference vsdr = new ValueSetDefinitionReference();
 
 			ValueSetReference valueSetReference = new ValueSetReference();
 			valueSetReference.setHref(valueSetHref);
 			valueSetReference.setUri(valueSetURI);
 			valueSetReference.setContent(valueSetName);
 			vsdr.setValueSet(valueSetReference);
 
 			NameAndMeaningReference valueSetDefinition = new NameAndMeaningReference();
 			valueSetDefinition.setContent(vsdde.getResourceName());
 			valueSetDefinition.setHref(vsdde.getHref());
 			valueSetDefinition.setUri(vsdde.getAbout());
 
 			vsdr.setValueSetDefinition(valueSetDefinition);
 			return vsdr;
 		}
 
 		logger_.debug("No results matched " + URIHelperInterface.DEFAULT_TAG + " - returning null");
 
 		return null;
 	}
 
 	public UrlConstructor getUrlConstructor()
 	{
 		if (urlConstructor_ == null)
 		{
 			urlConstructor_ = new UrlConstructor(serverContext_);
 		}
 		return urlConstructor_;
 	}
 
 	public Cts2RestClient getRestClient()
 	{
 		if (cts2RestClient_ == null)
 		{
 			Cts2RestClient.connectTimeoutMS = 2000;
 			cts2RestClient_ = new Cts2RestClient(cts2Marshaller_, true);
 		}
 		return cts2RestClient_;
 	}
 
 	public ValueSetCatalogEntry lookupValueSetByURI(String uri, ResolvedReadContext readContext)
 	{
 		logger_.debug("Looking up ValueSet by URI to get localname");
 
 		ValueSetCatalogEntry vs = null;
 
 		try
 		{
 			if (StringUtils.isNotBlank(valueSetServiceRootURL_))
 			{
 				logger_.debug("Looking up valueset using external server '{}'", valueSetServiceRootURL_);
 				String address = valueSetServiceRootURL_ + (valueSetServiceRootURL_.endsWith("/") ? "" : "/") + "valuesetbyuri?uri=" + uri
 						+ parameterizeReadContext(readContext, false);
 				vs = getRestClient().getCts2Resource(address, ValueSetCatalogEntry.class);
 			}
 		}
 		catch (Exception e)
 		{
 			logger_.debug("Remote Service Lookup failed - " + e);
 		}
 		try
 		{
 			if (vs == null)
 			{
 				logger_.debug("Looking up valueset using internal service");
 				ValueSetReadService vssp = getLocalValueSetReadService();
 				NameOrURI nameOrURI = new NameOrURI();
 				nameOrURI.setUri(uri);
 				vs = vssp.read(nameOrURI, readContext);
 			}
 		}
 		catch (Exception e)
 		{
 			logger_.debug("Local Service Lookup failed - " + e);
 		}
 		if (vs == null)
 		{
 			throw ExceptionBuilder.buildUnknownValueSet(uri);
 		}
 		return vs;
 	}
 
 	public ValueSetCatalogEntry lookupValueSetByHref(String href)
 	{
 		logger_.debug("Looking up ValueSet by href to get URI and localName");
 		try
 		{
 			ValueSetCatalogEntry vs = getRestClient().getCts2Resource(href, ValueSetCatalogEntry.class);
 			if (vs == null)
 			{
 				throw ExceptionBuilder.buildUnknownValueSet(href);
 			}
 			return vs;
 		}
 		catch (Exception e)
 		{
 			logger_.error("Lookup failed", e);
 			throw ExceptionBuilder.buildUnknownValueSet(href);
 		}
 	}
 
 	public ValueSetCatalogEntry lookupValueSetByLocalName(String localName, ResolvedReadContext readContext)
 	{
 		logger_.debug("Looking up ValueSet by Name to get URI");
 		ValueSetCatalogEntry vs = null;
 
 		try
 		{
 			if (StringUtils.isNotBlank(valueSetServiceRootURL_))
 			{
 				logger_.debug("Looking up valueset using external server '{}'", valueSetServiceRootURL_);
 				String address = valueSetServiceRootURL_ + (valueSetServiceRootURL_.endsWith("/") ? "" : "/") + "valueset/" + localName
 						+ parameterizeReadContext(readContext, true);
 				vs = getRestClient().getCts2Resource(address, ValueSetCatalogEntry.class);
 			}
 			else
 			{
 				logger_.debug("Looking up valueset using internal service");
 				ValueSetReadService vssp = getLocalValueSetReadService();
 
 				NameOrURI nameOrURI = new NameOrURI();
 				nameOrURI.setName(localName);
 				vs = vssp.read(nameOrURI, readContext);
 			}
 		}
 		catch (Exception e)
 		{
 			logger_.error("Lookup failed", e);
 			throw ExceptionBuilder.buildUnknownValueSet(localName);
 		}
 		if (vs == null)
 		{
 			throw ExceptionBuilder.buildUnknownValueSet(localName);
 		}
 		return vs;
 	}
 
 	/**
 	 * Uses the parameters from left to right - if uri is provided, uses that - otherwise, moves on to localname, then href.
 	 * A lookup failure on any provided value is immediately returned (we don't try to look up again with another provided key)
 	 * 
 	 * @param uri 1st
 	 * @param localName 2nd
 	 * @param href 3rd
 	 * @return
 	 */
 	public ValueSetCatalogEntry lookupValueSetByAny(String uri, String localName, String href, ResolvedReadContext readContext)
 	{
 		if (StringUtils.isNotBlank(uri))
 		{
 			return lookupValueSetByURI(uri, readContext);
 		}
 		else if (StringUtils.isNotBlank(localName))
 		{
 			return lookupValueSetByLocalName(localName, readContext);
 		}
 		else if (StringUtils.isNotBlank(href))
 		{
 			return lookupValueSetByHref(href + parameterizeReadContext(readContext, href.indexOf('?') < 0));
 		}
 		else
 		{
 			throw ExceptionBuilder.buildUnknownValueSetDefinition("No valid parameters specified to enable resolving the Complete ValueSet reference");
 		}
 	}
 
 	public LocalIdValueSetDefinition lookupValueSetDefinition(ValueSetDefinitionReadId definitionId, ResolvedReadContext readContext)
 	{
 		logger_.debug("Looking up ValueSetDefinition '{}'", definitionId);
 
 		LocalIdValueSetDefinition definition = null;
 		try
 		{
 			if (StringUtils.isBlank(valueSetDefinitionServiceRootURL_))
 			{
 				logger_.debug("Looking up via local service");
 				ValueSetDefinitionReadService vsdrs = getLocalValueSetDefinitionReadService();
 				definition = vsdrs.read(definitionId, readContext);
 			}
 			else
 			{
 				String address = valueSetDefinitionServiceRootURL_ + (valueSetDefinitionServiceRootURL_.endsWith("/") ? "" : "/");
 				if (StringUtils.isNotBlank(definitionId.getUri()))
 				{
 					logger_.debug("Looking up via URI on remote service");
 					address += "valuesetdefinitionbyuri?uri=" + definitionId.getUri();
 					address += parameterizeReadContext(readContext, false);
 				}
 				else if (StringUtils.isNotBlank(definitionId.getName()) && definitionId.getValueSet() != null
 						&& (StringUtils.isNotBlank(definitionId.getValueSet().getName()) || StringUtils.isNotBlank(definitionId.getValueSet().getUri())))
 				{
 					if (StringUtils.isNotBlank(definitionId.getValueSet().getName()))
 					{
 						logger_.debug("Looking up via Name/Name on remote service");
 						address += "valueset/" + definitionId.getValueSet().getName() + "/definition/" + definitionId.getName();
 						address += parameterizeReadContext(readContext, true);
 					}
 					else
 					{
 						logger_.debug("Looking up via Name/URI on remote service ");
 						address += "valuesetbyuri/definition/" + definitionId.getName() + "?uri=" + definitionId.getValueSet().getUri();
 						address += parameterizeReadContext(readContext, false);
 					}
 				}
 				else
 				{
 					throw ExceptionBuilder.buildUnknownValueSetDefinition("Missing requird parameters");
 				}
 				ValueSetDefinitionMsg temp = getRestClient().getCts2Resource(address, ValueSetDefinitionMsg.class);
 				definition = new LocalIdValueSetDefinition(temp.getValueSetDefinition());
 
 				String rr = temp.getHeading().getResourceRoot();  // Looks like valueset/AdministrativeGender/definition/1 - need to parse out the end
 				if (rr.indexOf('/') > 0)
 				{
 					definition.setLocalID(rr.substring(rr.lastIndexOf('/'), rr.length()));
 				}
 				else
 				{
 					throw new RuntimeException("failed to parse out the localId");
 				}
 				return definition;
 			}
 		}
 		catch (UnknownValueSetDefinition e)
 		{
 			throw e;
 		}
 		catch (Exception e)
 		{
 			logger_.error("Unable to lookup requested ValueSetDefinition", e);
 			throw ExceptionBuilder.buildUnknownValueSetDefinition("Unable to lookup requested ValueSetDefinition " + e);
 		}
 
 		if (definition == null)
 		{
 			throw ExceptionBuilder.buildUnknownValueSetDefinition(definitionId.toString());
 		}
 		return definition;
 	}
 
 	public String parameterizeReadContext(ResolvedReadContext readContext, boolean isFirstParam)
 	{
 		if (readContext == null)
 		{
 			return "";
 		}
 		StringBuilder sb = new StringBuilder();
 		if (readContext.getActive() != null)
 		{
 			sb.append("&active=");
 			sb.append(readContext.getActive().toString());
 		}
 		if (StringUtils.isNotBlank(readContext.getChangeSetContextUri()))
 		{
 			sb.append("&changesetcontext=");
 			sb.append(readContext.getChangeSetContextUri());
 		}
 		if (readContext.getLanguageReference() != null && StringUtils.isNotBlank(readContext.getLanguageReference().getContent()))
 		{
 			sb.append("&referencelanguage=");
 			sb.append(readContext.getLanguageReference().getContent());
 		}
 		if (readContext.getReferenceTime() != null)
 		{
 			sb.append("&referencetime=");
 
 			Calendar c = GregorianCalendar.getInstance();
 			c.setTime(readContext.getReferenceTime());
 			sb.append(javax.xml.bind.DatatypeConverter.printDateTime(c));
 		}
 		if (readContext.getReturnContentFilter() != null)
 		{
 			for (PropertyType pt : readContext.getReturnContentFilter().getPropertyTypes())
 			{
 				sb.append("&filtercomponent=");
 				sb.append(pt.toString());
 			}
 		}
 		if (sb.length() > 0 && isFirstParam)
 		{
 			sb.replace(0, 1, "?");
 		}
 		return sb.toString();
 	}
 	
 	public EntityReferenceAndHref resolveEntityReference(URIAndEntityName entity, HashMap<String, CodeSystemVersionCatalogEntryAndHref> resolvedCodeSystemVersions, 
 			NameOrURI tag, ResolvedReadContext readContext)
 	{
 		//First, we need to resolve the EntityReference, and find out what code system(s) it is from
 		EntityReferenceAndHref er = resolveEntityReference(entity, readContext);
 		
 		if (er == null)
 		{
 			return null;
 		}
 		
 		if (resolvedCodeSystemVersions != null)
 		{
 			for (DescriptionInCodeSystem d : er.getEntityReference().getKnownEntityDescriptionAsReference())
 			{
 				if (resolvedCodeSystemVersions.containsKey(d.getDescribingCodeSystemVersion().getVersion().getUri()))
 				{
 					//This entity referencs aligns with one of the requested resolvedCodeSystemVersions - use it.
 					return er;
 				}
 			}
 		}
 		
 		//None matched the requested code system versions.  Move on to tag matching.
 		
 		for (DescriptionInCodeSystem d : er.getEntityReference().getKnownEntityDescriptionAsReference())
 		{
 			CodeSystemVersionCatalogEntrySummary cs = lookupCodeSystemVersionByTag(d.getDescribingCodeSystemVersion().getCodeSystem(), tag, "CURRENT", 
 					false, readContext);
 			if (cs != null && !cs.getAbout().equals(d.getDescribingCodeSystemVersion().getVersion().getUri()))
 			{
 				//We found a CodeSystemVersion that will work - and it isn't the one we already have.  lookup the entity there, and return it.
 				CodeSystemVersionReference csvr = new CodeSystemVersionReference();
 				csvr.setCodeSystem(cs.getVersionOf());
 				NameAndMeaningReference namr = new NameAndMeaningReference();
 				namr.setUri(cs.getAbout());
 				namr.setContent(cs.getCodeSystemVersionName());
 				namr.setHref(cs.getHref());
 				csvr.setVersion(namr);
 				EntityReferenceAndHref temp = resolveEntityReference(entity, csvr, readContext);
 				if (temp != null)
 				{
 					return temp;
 				}
 			}
 			
 		}
 		//TODO QUESTION is arbitrary allowed?
 		//None of that worked... return our arbitrary result - 
 		return er;
 	}
 	
 	public EntityReferenceAndHref resolveEntityReference(URIAndEntityName entity, ResolvedReadContext readContext)
 	{
 		readContext.setActive(ActiveOrAll.ACTIVE_ONLY);
 		EntityDescriptionReadService edrs = getLocalEntityDescriptionReadService();
 		if (edrs != null)
 		{
 			try
 			{
 				logger_.debug("resolving up EntityReference using local EntityDescriptionService");
 				EntityNameOrURI e = new EntityNameOrURI();
 				
 				if (StringUtils.isNotBlank(entity.getUri()))
 				{
 					e.setUri(entity.getUri());
 				}
 				//This shouldn't happen if they are following spec (URI is required) but be nice anyway...
 				else if (StringUtils.isNotBlank(entity.getName()) && StringUtils.isNotBlank(entity.getNamespace()))
 				{
 					ScopedEntityName entityName = new ScopedEntityName();
 					entityName.setName(entity.getName());
 					entityName.setNamespace(entity.getNamespace());
 					e.setEntityName(entityName);
 				}
 				else
 				{
 					throw new UnspecifiedCts2Exception("URI is required within a URIAndEntityName!");
 				}
 				EntityReference entityReference = edrs.availableDescriptions(e, readContext);
 				return new EntityReferenceAndHref(entityReference, getUrlConstructor().createEntityUrl(entityReference.getName()));
 			}
 			catch (Exception e)
 			{
 				logger_.debug("Error looking up EntityDesignation on local service " + e);
 			}
 		}
 		if (StringUtils.isNotBlank(codeSystemAndEntitiesServicesRootURL_))
 		{
 			try
 			{
 				logger_.debug("resolving up EntityReference using remote EntityDescriptionService");
 				
 				String url = null;
 				
 				if (StringUtils.isNotBlank(entity.getUri()))
 				{
 					url = "entitybyuri?uri=" + entity.getUri() + "&";
 				}
 				//This shouldn't happen if they are following spec (URI is required) but be nice anyway...
 				else if (StringUtils.isNotBlank(entity.getName()) && StringUtils.isNotBlank(entity.getNamespace()))
 				{
 					url = "entity/" + entity.getNamespace() + ":" + entity.getName() + "?";
 				}
 				else
 				{
 					throw new UnspecifiedCts2Exception("URI is required within a URIAndEntityName!");
 				}
 				return new EntityReferenceAndHref(getRestClient().getCts2Resource(
 						codeSystemAndEntitiesServicesRootURL_ + (codeSystemAndEntitiesServicesRootURL_.endsWith("/") ? "" : "/") + url + "&list=false"
 								+ parameterizeReadContext(readContext, false), EntityReferenceMsg.class));
 			}
 			catch (Exception e)
 			{
 				logger_.debug("Error looking up EntityDesignation on remote service " + e);
 			}
 		}
 		//Our local service didn't work, but perhaps, if there is an href on the entity - that will work.  Give it a try.
 		if (StringUtils.isNotBlank(entity.getHref()))
 		{
 			logger_.debug("resolving up EntityReference using provided href");
 			try
 			{
 				String uriTemp;
 				if (entity.getHref().indexOf("?") > 0)
 				{
 					uriTemp = entity.getHref().substring(0, entity.getHref().indexOf('?'));
 				}
 				else
 				{
 					uriTemp = entity.getHref();
 				}
 				uriTemp += parameterizeReadContext(readContext, false);
 
 				return new EntityReferenceAndHref(getRestClient().getCts2Resource(uriTemp, EntityReferenceMsg.class));
 			}
 			catch (Exception e)
 			{
 				logger_.debug("Error looking up EntityDesignation by provided href " + e);
 			}
 		}
 		return null;
 	}
 
 	public EntityReferenceAndHref resolveEntityReference(URIAndEntityName entity, CodeSystemVersionReference codeSystemVersion, ResolvedReadContext readContext)
 	{
 		EntityDescriptionBase resolvedEntity = null;
 		NameOrURI codeSystemVersionUri = new NameOrURI();
 		codeSystemVersionUri.setUri(codeSystemVersion.getVersion().getUri());
 		readContext.setActive(ActiveOrAll.ACTIVE_ONLY);
 		try
 		{
 			EntityDescriptionReadService edrs = getLocalEntityDescriptionReadService();
 			if (edrs != null)
 			{
 				logger_.debug("resolving up EntityReference using local EntityDescriptionService");
 				EntityDescriptionReadId eid;
 				
 				if (StringUtils.isNotBlank(entity.getUri()))
 				{
 					eid = new EntityDescriptionReadId(entity.getUri(), codeSystemVersionUri);
 				}
 				//This shouldn't happen if they are following spec (URI is required) but be nice anyway...
 				else if (StringUtils.isNotBlank(entity.getName()) && StringUtils.isNotBlank(entity.getNamespace()))
 				{
 					eid = new EntityDescriptionReadId(entity.getName(), entity.getNamespace(), codeSystemVersionUri);
 				}
 				else
 				{
 					throw new UnspecifiedCts2Exception("URI is required within a URIAndEntityName!");
 				}
 				EntityDescription entityDescription = edrs.read(eid, readContext);
				if (entityDescription != null)
				{
					resolvedEntity = (EntityDescriptionBase)entityDescription.getChoiceValue();
				}
 			}
 			else if (StringUtils.isNotBlank(codeSystemAndEntitiesServicesRootURL_))
 			{
 				logger_.debug("resolving up EntityReference using remote EntityDescriptionService");
 				
 				String url = null;
 				
 				if (StringUtils.isNotBlank(entity.getUri()))
 				{
 					url = "codesystem/" + codeSystemVersion.getCodeSystem().getContent() + "/version/" + 
 							codeSystemVersion.getVersion().getContent() + "/entitybyuri?uri=" + entity.getUri() + "&";
 				}
 				//This shouldn't happen if they are following spec (URI is required) but be nice anyway...
 				else if (StringUtils.isNotBlank(entity.getName()) && StringUtils.isNotBlank(entity.getNamespace()))
 				{
 					url = "codesystem/" + codeSystemVersion.getCodeSystem().getContent() + "/version/" + 
 							codeSystemVersion.getVersion().getContent() + "/entity/" + entity.getNamespace() + ":" + entity.getName() + "?";
 				}
 				else
 				{
 					throw new UnspecifiedCts2Exception("URI is required within a URIAndEntityName!");
 				}
 				EntityDescription ed = getRestClient().getCts2Resource(
 						codeSystemAndEntitiesServicesRootURL_ + (codeSystemAndEntitiesServicesRootURL_.endsWith("/") ? "" : "/") + url + "list=false"
 								+ parameterizeReadContext(readContext, false), EntityDescriptionMsg.class).getEntityDescription();
				if (ed != null)
				{
					resolvedEntity = (EntityDescriptionBase)ed.getChoiceValue();
				}
 			}
 			else
 			{
 				logger_.debug("No locally configured service is available to lookup EntityDescription by URI");
 			}
 		}
 		catch (Exception e)
 		{
			logger_.warn("Error looking up EntityDesignation '" + entity + "'", e);
 		}
 
 		//Our local service didn't work, but perhaps, if there is an href on the entity - that will work.  Give it a try.
 		if (resolvedEntity == null && StringUtils.isNotBlank(entity.getHref()))
 		{
 			logger_.debug("resolving up EntityReference using provided href");
 			try
 			{
 				String uriTemp;
 				if (entity.getHref().indexOf("?") > 0)
 				{
 					uriTemp = entity.getHref().substring(0, entity.getHref().indexOf('?'));
 				}
 				else
 				{
 					uriTemp = entity.getHref();
 				}
 				uriTemp += parameterizeReadContext(readContext, true);
 
 				EntityDescription ed = getRestClient().getCts2Resource(uriTemp, EntityDescriptionMsg.class).getEntityDescription();
				if (ed != null)
				{
					resolvedEntity = (EntityDescriptionBase)ed.getChoiceValue();
				}
 			}
 			catch (Exception e)
 			{
 				logger_.debug("Error looking up EntityDesignation by provided href", e);
 			}
 		}
 		
 		if (resolvedEntity != null)
 		{
 			String entityHref = getUrlConstructor().createEntityUrl(resolvedEntity.getEntityID());
 			EntityReference er = new EntityReference();
 			er.setAbout(resolvedEntity.getAbout());
 			er.setName(resolvedEntity.getEntityID());
 			
 			ArrayList<DescriptionInCodeSystem> descriptions = new ArrayList<>(resolvedEntity.getDesignation().length);
 			for (Designation description : resolvedEntity.getDesignation())
 			{
 				DescriptionInCodeSystem d = new DescriptionInCodeSystem();
 				d.setCodeSystemRole(resolvedEntity.getCodeSystemRole());
 				d.setDescribingCodeSystemVersion(codeSystemVersion);
 				d.setDesignation(description.getValue().getContent());
 				d.setHref(entityHref);
 				descriptions.add(d);
 			}
 			
 			er.setKnownEntityDescription(descriptions);
 			return new EntityReferenceAndHref(er, entityHref);
 		}
 		return null;
 	}
 	
 	public CodeSystemVersionCatalogEntryAndHref lookupCodeSystemVersion(NameOrURI codeSystemVersion, String suppliedHref, ResolvedReadContext readContext)
 	{
 		CodeSystemVersionReadService csvrs = getLocalCodeSystemVersionReadService();
 		
 		String queryURL = "";
 		if (StringUtils.isNotBlank(codeSystemVersion.getUri()))
 		{
 			queryURL = "codesystemversionbyuri?uri="+ codeSystemVersion.getUri() + parameterizeReadContext(readContext, false);
 		}
 		else if (StringUtils.isNotBlank(codeSystemVersion.getName()))
 		{
 			queryURL = "codesystemversion/" + codeSystemVersion.getName() + parameterizeReadContext(readContext, true);
 		}
 		else
 		{
 			throw new UnspecifiedCts2Exception("The Name or URI of the codeSystemVersion parameter must be populated");
 		}
 		
 		if (csvrs != null)
 		{
 			logger_.debug("Looking up CodeSystemVersion using local CodeSystemVersionService");
 			try
 			{
 				CodeSystemVersionCatalogEntry temp =  csvrs.read(codeSystemVersion, readContext);
 				if (temp != null)
 				{
 					return new CodeSystemVersionCatalogEntryAndHref(temp, getUrlConstructor().createCodeSystemVersionUrl(temp.getVersionOf().getContent(),
 							temp.getCodeSystemVersionName()));
 				}
 			}
 			catch (Exception e)
 			{
 				logger_.debug("Read failure on local service for " + codeSystemVersion + " " + e);
 			}
 		}
 	
 		if (StringUtils.isNotBlank(codeSystemAndEntitiesServicesRootURL_))
 		{
 			logger_.debug("Looking up CodeSystemVersion using remote CodeSystemVersionService");
 			try
 			{
 				return new CodeSystemVersionCatalogEntryAndHref(getRestClient().getCts2Resource(codeSystemAndEntitiesServicesRootURL_ + (codeSystemAndEntitiesServicesRootURL_.endsWith("/") ? "" : "/") + 
 						queryURL, CodeSystemVersionCatalogEntryMsg.class));
 			}
 			catch (Exception e)
 			{
 				logger_.debug("Read failure on remote service for " + codeSystemVersion + " " + e);
 			}
 		}
 		
 		if (StringUtils.isNotBlank(suppliedHref) && suppliedHref.toLowerCase().indexOf("/codesystem") > 0)
 		{
 			logger_.debug("Looking up CodeSystemVersion using supplied remote CodeSystemVersionService");
 			try
 			{
 				String serverAddress = suppliedHref.substring(0, suppliedHref.toLowerCase().indexOf("/codesystem"));
 				if (StringUtils.isNotBlank(codeSystemAndEntitiesServicesRootURL_) && !codeSystemAndEntitiesServicesRootURL_.startsWith(serverAddress))
 				{
 					return new CodeSystemVersionCatalogEntryAndHref(getRestClient().getCts2Resource(serverAddress + "/" + 
 							queryURL, CodeSystemVersionCatalogEntryMsg.class));
 				}
 			}
 			catch (Exception e)
 			{
 				logger_.debug("Read failure on supplied remote service for " + codeSystemVersion + " " + e);
 			}
 		}
 		throw new RuntimeException("No service is available to resolve the codeSystemVersion");
 	}
 	
 	public CodeSystemVersionCatalogEntrySummary lookupCodeSystemVersionByTag(CodeSystemReference codeSystem, NameOrURI desiredTag, 
 			String fallbackTag, boolean allowFallBackToArbitrary, ResolvedReadContext readContext)
 	{
 		CodeSystemVersionQueryService csvqs = getLocalCodeSystemVersionQueryService();
 
 		CodeSystemVersionCatalogEntrySummary finalResult = null;
 		CodeSystemVersionCatalogEntrySummary fallbackResult = null;
 		CodeSystemVersionCatalogEntrySummary arbitraryResult = null;
 
 		if (csvqs != null && (StringUtils.isNotBlank(codeSystem.getUri()) || StringUtils.isNotBlank(codeSystem.getContent())))
 		{
 			logger_.debug("Looking up a code system using the local service");
 			int pageNumber = 0;
 
 			CodeSystemVersionQuery query = CodeSystemVersionQueryBuilder.build(readContext);
 			NameOrURI csInfo = new NameOrURI();
 			csInfo.setUri(codeSystem.getUri());
 			csInfo.setName(codeSystem.getContent());
 			query.getRestrictions().setCodeSystem(csInfo);
 
 			while (true)
 			{
 				if (finalResult != null)
 				{
 					break;
 				}
 				Page page = new Page();
 				page.setMaxToReturn(500);
 				page.setPage(pageNumber++);
 
 				DirectoryResult<CodeSystemVersionCatalogEntrySummary> result = csvqs.getResourceSummaries(query, null, page);
 				for (CodeSystemVersionCatalogEntrySummary entry : result.getEntries())
 				{
 					if (finalResult != null)
 					{
 						break;
 					}
 					if (arbitraryResult == null)
 					{
 						arbitraryResult = entry;
 					}
 					for (VersionTagReference tag : entry.getCodeSystemVersionTagAsReference())
 					{
 						if (tag.getContent().equals(desiredTag.getName()) || tag.getUri().equals(desiredTag.getUri()))
 						{
 							finalResult = entry;
 							break;
 						}
 						else if (tag.getContent().equals(fallbackTag))
 						{
 							fallbackResult = entry;
 						}
 					}
 				}
 				if (result.isAtEnd())
 				{
 					break;
 				}
 			}
 		}
 		String queryUrl = null;
 		if (StringUtils.isNotBlank(codeSystem.getUri()))
 		{
 			queryUrl = "codesystembyuri/versions?uri=" + codeSystem.getUri() + "&page=";
 		}
 		else if (StringUtils.isNotBlank(codeSystem.getContent()))
 		{
 			queryUrl = "codesystem/" + codeSystem.getContent() + "/versions?page=";
 		}
 
 		if (finalResult == null && queryUrl != null)
 		{
 			if (StringUtils.isNotBlank(codeSystemAndEntitiesServicesRootURL_))
 			{
 				logger_.debug("Looking up a code system using the remote service");
 				int page = 0;
 				while (true)
 				{
 					CodeSystemVersionCatalogEntryDirectory directory = getRestClient().getCts2Resource(
 							codeSystemAndEntitiesServicesRootURL_ + (codeSystemAndEntitiesServicesRootURL_.endsWith("/") ? "" : "/") + queryUrl + page++
 									+ parameterizeReadContext(readContext, false), CodeSystemVersionCatalogEntryDirectory.class);
 	
 					for (CodeSystemVersionCatalogEntrySummary entry : directory.getEntryAsReference())
 					{
 						if (finalResult != null)
 						{
 							break;
 						}
 						if (arbitraryResult == null)
 						{
 							arbitraryResult = entry;
 						}
 						for (VersionTagReference tag : entry.getCodeSystemVersionTagAsReference())
 						{
 							if (tag.getContent().equals(desiredTag.getName()) || tag.getUri().equals(desiredTag.getUri()))
 							{
 								finalResult = entry;
 								break;
 							}
 							else if (fallbackResult == null && tag.getContent().equals(fallbackTag))
 							{
 								fallbackResult = entry;
 							}
 						}
 					}
 					if (finalResult != null || CompleteDirectory.COMPLETE == directory.getComplete() || StringUtils.isBlank(directory.getNext()))
 					{
 						break;
 					}
 				}
 			}
 
 			// Nothing yet... lets try the server from the href...
 			if (finalResult == null && queryUrl != null && StringUtils.isNotBlank(codeSystem.getHref()) 
 					&& codeSystem.getHref().toLowerCase().indexOf("/codesystem") > 0)
 			{
 				logger_.debug("Looking up a code system using the remote service specified in the href");
 				int page = 0;
 				String server = codeSystem.getHref().substring(0, codeSystem.getHref().toLowerCase().indexOf("/codesystem"));
 				if (StringUtils.isNotBlank(codeSystemAndEntitiesServicesRootURL_) && !codeSystemAndEntitiesServicesRootURL_.toLowerCase().startsWith(server.toLowerCase()))
 				{
 					while (true)
 					{
 						CodeSystemVersionCatalogEntryDirectory directory = getRestClient().getCts2Resource(
 								server + "/" + queryUrl + page++ + parameterizeReadContext(readContext, false), CodeSystemVersionCatalogEntryDirectory.class);
 	
 						for (CodeSystemVersionCatalogEntrySummary entry : directory.getEntryAsReference())
 						{
 							if (finalResult != null)
 							{
 								break;
 							}
 							if (arbitraryResult == null)
 							{
 								arbitraryResult = entry;
 							}
 							for (VersionTagReference tag : entry.getCodeSystemVersionTagAsReference())
 							{
 								if (tag.getContent().equals(desiredTag.getName()) || tag.getUri().equals(desiredTag.getUri()))
 								{
 									finalResult = entry;
 									break;
 								}
 								else if (fallbackResult == null && tag.getContent().equals(fallbackTag))
 								{
 									fallbackResult = entry;
 								}
 							}
 						}
 						if (finalResult != null || CompleteDirectory.COMPLETE == directory.getComplete() || StringUtils.isBlank(directory.getNext()))
 						{
 							break;
 						}
 					}
 				}
 			}
 		}
 		if (finalResult != null)
 		{
 			return finalResult;
 		}
 		else if (fallbackResult != null)
 		{
 			return fallbackResult;
 		}
 		else if (allowFallBackToArbitrary)
 		{
 			return arbitraryResult;
 		}
 		return null;
 	}
 	
 	public Iterator<EntityReferenceResolver> getEntities(String codeSystemVersionName, String codeSystemName, String entityHref, ResolvedFilter resolvedFilter,
 			ResolvedReadContext readContext)
 	{
 		return new EntityIterator(codeSystemVersionName, codeSystemName, entityHref, codeSystemAndEntitiesServicesRootURL_, resolvedFilter, readContext, this);
 	}
 
 	public String makeAssociationURL(String codeSystemName, String codeSystemVersionName, String entityName, boolean sourceToTarget, String serviceURL,
 			ResolvedReadContext readContext)
 	{
 		String href = null;
 		if (sourceToTarget)
 		{
 			href = getUrlConstructor().createSubjectOfUrl(codeSystemName, codeSystemVersionName, entityName);
 		}
 		else
 		{
 			href = getUrlConstructor().createTargetOfUrl(codeSystemName, codeSystemVersionName, entityName);
 		}
 
 		//Just keep the end - will be swapping in a different server address
 		String temp = href.substring(href.indexOf("/codesystem/") + 1, href.length());
 		temp += parameterizeReadContext(readContext, true);
 
 		// Just want the server name and port from the altServiceURL
 		int i = serviceURL.toLowerCase().indexOf("/codesystem");  // this breaks if the server name is http://codesystem....
 		if (i > 0)
 		{
 			return serviceURL.substring(0, i) + "/" + temp;
 		}
 		else
 		{
 			throw new RuntimeException("serviceURL contained an unexpected format");
 		}
 	}
 	
 	//TODO FRAMEWORK - We need a way to have multiple service providers - https://github.com/cts2/cts2-framework/issues/29
 	//within a single instance - as the service provider that I am providing above isn't going to return hits for some methods below.
 	
 	public ValueSetDefinitionQueryService getLocalValueSetDefinitionQueryService()
 	{
 		return serviceProvider.getService(ValueSetDefinitionQueryService.class);
 	}
 	
 	public ValueSetReadService getLocalValueSetReadService()
 	{
 		return serviceProvider.getService(ValueSetReadService.class);
 	}
 	
 	public ValueSetMaintenanceService getLocalValueSetMaintenanceService()
 	{
 		return serviceProvider.getService(ValueSetMaintenanceService.class);
 	}
 	
 	public ValueSetDefinitionReadService getLocalValueSetDefinitionReadService()
 	{
 		return serviceProvider.getService(ValueSetDefinitionReadService.class);
 	}
 	
 	public EntityDescriptionReadService getLocalEntityDescriptionReadService()
 	{
 		return serviceProvider.getService(EntityDescriptionReadService.class);
 	}
 
 	public EntityDescriptionQueryService getLocalEntityDescriptionQueryService()
 	{
 		return serviceProvider.getService(EntityDescriptionQueryService.class);
 	}
 
 	public CodeSystemVersionReadService getLocalCodeSystemVersionReadService()
 	{
 		return serviceProvider.getService(CodeSystemVersionReadService.class);
 	}
 
 	public CodeSystemVersionQueryService getLocalCodeSystemVersionQueryService()
 	{
 		return serviceProvider.getService(CodeSystemVersionQueryService.class);
 	}
 
 	public AssociationQueryService getLocalAssociationQueryService()
 	{
 		return serviceProvider.getService(AssociationQueryService.class);
 	}
 }
