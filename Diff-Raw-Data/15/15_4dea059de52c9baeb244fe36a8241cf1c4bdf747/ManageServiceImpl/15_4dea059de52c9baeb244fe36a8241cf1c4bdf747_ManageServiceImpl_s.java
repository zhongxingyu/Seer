 package org.cotrix.web.manage.server;
 
 import static org.cotrix.action.CodelistAction.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.repository.CodelistQueries.*;
 import static org.cotrix.web.manage.shared.ManagerUIFeature.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.xml.namespace.QName;
 
 import org.cotrix.action.CodelistAction;
 import org.cotrix.action.MainAction;
 import org.cotrix.action.events.CodelistActionEvents;
 import org.cotrix.application.VersioningService;
 import org.cotrix.common.cdi.BeanSession;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.domain.attributes.Attribute;
 import org.cotrix.domain.attributes.Definition;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.codelist.CodelistLink;
 import org.cotrix.domain.user.User;
 import org.cotrix.lifecycle.Lifecycle;
 import org.cotrix.lifecycle.LifecycleService;
 import org.cotrix.lifecycle.impl.DefaultLifecycleStates;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.CodelistSummary;
 import org.cotrix.repository.MultiQuery;
 import org.cotrix.web.common.server.CotrixRemoteServlet;
 import org.cotrix.web.common.server.task.ActionMapper;
 import org.cotrix.web.common.server.task.CodelistTask;
 import org.cotrix.web.common.server.task.ContainsTask;
 import org.cotrix.web.common.server.task.Id;
 import org.cotrix.web.common.server.util.AttributeTypes;
 import org.cotrix.web.common.server.util.Codelists;
 import org.cotrix.web.common.server.util.LinkTypes;
 import org.cotrix.web.common.server.util.ValueUtils;
 import org.cotrix.web.common.shared.DataWindow;
 import org.cotrix.web.common.shared.codelist.UICode;
 import org.cotrix.web.common.shared.codelist.UICodelist;
 import org.cotrix.web.common.shared.codelist.UICodelistMetadata;
 import org.cotrix.web.common.shared.codelist.UIQName;
 import org.cotrix.web.common.shared.codelist.attributetype.UIAttributeType;
 import org.cotrix.web.common.shared.codelist.linktype.AttributeValue;
 import org.cotrix.web.common.shared.codelist.linktype.LinkValue;
 import org.cotrix.web.common.shared.codelist.linktype.UILinkType;
 import org.cotrix.web.common.shared.exception.ServiceException;
 import org.cotrix.web.common.shared.feature.ApplicationFeatures;
 import org.cotrix.web.common.shared.feature.FeatureCarrier;
 import org.cotrix.web.common.shared.feature.ResponseWrapper;
 import org.cotrix.web.manage.client.ManageService;
 import org.cotrix.web.manage.server.modify.ChangesetUtil;
 import org.cotrix.web.manage.server.modify.ModifyCommandHandler;
 import org.cotrix.web.manage.shared.CodelistEditorSortInfo;
 import org.cotrix.web.manage.shared.CodelistGroup;
 import org.cotrix.web.manage.shared.CodelistRemoveCheckResponse;
 import org.cotrix.web.manage.shared.CodelistValueTypes;
 import org.cotrix.web.manage.shared.Group;
 import org.cotrix.web.manage.shared.UICodeInfo;
 import org.cotrix.web.manage.shared.UILinkTypeInfo;
 import org.cotrix.web.manage.shared.modify.ModifyCommand;
 import org.cotrix.web.manage.shared.modify.ModifyCommandResult;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The server side implementation of the RPC serialiser.
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @SuppressWarnings("serial")
 @ContainsTask
 public class ManageServiceImpl implements ManageService {
 
 	public static class Servlet extends CotrixRemoteServlet {
 
 		@Inject
 		protected ManageServiceImpl bean;
 
 		@Override
 		public Object getBean() {
 			return bean;
 		}
 	}
 
 	protected Logger logger = LoggerFactory.getLogger(ManageServiceImpl.class);
 
 	@Inject
 	private ActionMapper mapper;
 
 	@Inject
 	private CodelistRepository repository;
 
 	@Inject
 	private ModifyCommandHandler commandHandler;
 
 	@Inject
 	private VersioningService versioningService;
 
 	@Inject
 	private LifecycleService lifecycleService;
 
 	@Inject
 	private Event<CodelistActionEvents.CodelistEvent> events;
 
 	@Inject @Current
 	private BeanSession session;
 	
 	@Inject @Current
 	private User currentUser;
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@PostConstruct
 	public void init() {
 		mapper.map(VIEW).to(VIEW_CODELIST, VIEW_METADATA);
 		mapper.map(EDIT).to(EDIT_METADATA, EDIT_CODELIST, ADD_CODE, REMOVE_CODE, ApplicationFeatures.VERSION_CODELIST, ApplicationFeatures.REMOVE_CODELIST);
 		mapper.map(LOCK).to(LOCK_CODELIST);
 		mapper.map(UNLOCK).to(UNLOCK_CODELIST);
 		mapper.map(SEAL).to(SEAL_CODELIST);
 		mapper.map(MainAction.CREATE_CODELIST).to(ApplicationFeatures.CREATE_CODELIST);
 	}
 
 	@Override
 	public DataWindow<CodelistGroup> getCodelistsGrouped() throws ServiceException {
 		logger.trace("getCodelistsGrouped");
 
 		Map<QName, CodelistGroup> groups = new HashMap<QName, CodelistGroup>();
 		Iterator<Codelist> it = repository.get(allLists()).iterator();
 		while (it.hasNext()) {
 			Codelist codelist = (Codelist) it.next();
 
 			CodelistGroup group = groups.get(codelist.name());
 			if (group == null) {
 				group = new CodelistGroup(ValueUtils.safeValue(codelist.name()));
 				groups.put(codelist.name(), group);
 			}
 			group.addVersion(codelist.id(), ValueUtils.safeValue(codelist.version()));
 		}
 
 		for (CodelistGroup group:groups.values()) Collections.sort(group.getVersions()); 
 
 		return new DataWindow<CodelistGroup>(new ArrayList<CodelistGroup>(groups.values()));
 	}
 
 	@Override
 	@CodelistTask(VIEW)
 	public DataWindow<UICode> getCodelistCodes(@Id String codelistId, com.google.gwt.view.client.Range range, CodelistEditorSortInfo sortInfo) throws ServiceException {
 		logger.trace("getCodelistRows codelistId {}, range: {} sortInfo: {}", codelistId, range, sortInfo);
 
 		int start = range.getStart() + 1;
 		int end = range.getStart() + range.getLength();
 		logger.trace("query range from: {} to: {}", start ,end);
 
 		Codelist codelist = repository.lookup(codelistId);
 
 		MultiQuery<Codelist, Code> query = allCodesIn(codelistId).from(start).to(end);
 		if (sortInfo!=null) {
 			if (sortInfo instanceof CodelistEditorSortInfo.CodeNameSortInfo) {
 				if (sortInfo.isAscending()) query.sort(descending(byCodeName()));
 				else query.sort(byCodeName());
 			}
 			if (sortInfo instanceof CodelistEditorSortInfo.AttributeGroupSortInfo) {
 				CodelistEditorSortInfo.AttributeGroupSortInfo attributeGroupSortInfo = (CodelistEditorSortInfo.AttributeGroupSortInfo) sortInfo;
 				Attribute attribute = attribute().name(ChangesetUtil.convert(attributeGroupSortInfo.getName())).value(null)
 						.ofType(ChangesetUtil.convert(attributeGroupSortInfo.getType())).in(ChangesetUtil.convert(attributeGroupSortInfo.getLanguage())).build();
 				if (sortInfo.isAscending()) query.sort(descending(byAttribute(attribute, attributeGroupSortInfo.getPosition() + 1)));
 				else query.sort(byAttribute(attribute, attributeGroupSortInfo.getPosition() + 1));
 			}
 		}
 
 		Iterable<Code> codes  = repository.get(query);
 		List<UICode> uiCodes = new ArrayList<UICode>(range.getLength());
 		for (Code code:codes) {
 			UICode uicode = Codelists.toUiCode(code);
 			uiCodes.add(uicode);
 		}
 		logger.trace("retrieved {} rows", uiCodes.size());
 		return new DataWindow<UICode>(uiCodes, codelist.codes().size());
 	}
 
 	@Override
 	@CodelistTask(VIEW)
 	public List<Group> getGroups(@Id String codelistId) throws ServiceException {
 		logger.trace("getGroups codelistId {}", codelistId);
 
 		Iterable<Code> codes  = repository.get(allCodesIn(codelistId));
 		List<Group> groups = GroupFactory.getGroups(codes);
 
 		logger.trace("Generated {} groups: {}", groups.size(), groups);
 
 		return groups;
 	}
 
 	@Override
 	public UICodelistMetadata getMetadata(@Id String codelistId) throws ServiceException {
 		logger.trace("getMetadata codelistId: {}", codelistId);
 		Codelist codelist = repository.lookup(codelistId);
 		return Codelists.toCodelistMetadata(codelist);
 	}
 
 	@Override
 	@CodelistTask(LOCK)
 	public FeatureCarrier.Void lock(@Id String codelistId) throws ServiceException {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@CodelistTask(UNLOCK)
 	public FeatureCarrier.Void unlock(@Id String codelistId) throws ServiceException {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@CodelistTask(SEAL)
 	public FeatureCarrier.Void seal(@Id String codelistId) throws ServiceException {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@CodelistTask(EDIT)
 	public ModifyCommandResult modify(@Id String codelistId, ModifyCommand command) throws ServiceException {
 		logger.trace("modify codelistId: {} command: {}", codelistId, command);
 		try {
 			return commandHandler.handle(codelistId, command);
 		} catch(Throwable throwable)
 		{
 			logger.error("Error executing command "+command+" on codelist "+codelistId, throwable);
 			throw new ServiceException(throwable.getMessage());
 		}
 	}
 
 	@Override
 	@CodelistTask(VERSION)
 	public CodelistGroup createNewCodelistVersion(@Id String codelistId, String newVersion)	throws ServiceException {
 		try {
 			Codelist codelist = repository.lookup(codelistId);
 			
 			Codelist newCodelist = versioningService.bump(codelist).to(newVersion);
 	
 			CodelistGroup group = addCodelist(newCodelist);
 			
 			return group;
 		} catch(Throwable throwable)
 		{
 			logger.error("Error creating new codelist version for codelist "+codelistId, throwable);
 			throw new ServiceException(throwable.getMessage());
 		}
 	}
 
 	@Override
 	@CodelistTask(VIEW)
 	public ResponseWrapper<String> getCodelistState(@Id String codelistId) throws ServiceException {
 		logger.trace("getCodelistState codelistId: {}",codelistId);
 		Lifecycle lifecycle = lifecycleService.lifecycleOf(codelistId);
 		String state = lifecycle.state().toString();
 		return ResponseWrapper.wrap(state.toUpperCase());
 	}
 
 	@Override
 	public Set<UIQName> getAttributeNames(String codelistId) throws ServiceException {
 		logger.trace("getAttributeNames codelistId: {}",codelistId);
 		CodelistSummary summary = repository.get(summary(codelistId));
 
 		Set<UIQName> names = new HashSet<>();
 		for (QName qName:summary.allNames()) {
 			names.add(ValueUtils.safeValue(qName));
 		}
 		return names;
 	}
 
 	@Override
 	public CodelistGroup createNewCodelist(String name, String version)	throws ServiceException {
 		logger.trace("createNewCodelist name: {}, version: {}",name, version);
 		Codelist newCodelist = codelist().name(name).version(version).build();
 		CodelistGroup group = addCodelist(newCodelist);
 		events.fire(new CodelistActionEvents.Create(newCodelist.id(),newCodelist.name(), newCodelist.version(), session));
 		return group;
 	}
 
 	private CodelistGroup addCodelist(Codelist newCodelist) {
 		repository.add(newCodelist);
 		lifecycleService.start(newCodelist.id());
 
 		CodelistGroup group = new CodelistGroup(ValueUtils.safeValue(newCodelist.name()));
 		group.addVersion(newCodelist.id(), newCodelist.version());
 
 		return group;
 	}
 
 	@Override
 	public DataWindow<UILinkType> getCodelistLinkTypes(@Id String codelistId) throws ServiceException {
 		logger.trace("getCodelistLinkTypes codelistId: {}", codelistId);
 		
 		Codelist codelist = repository.lookup(codelistId);
 
 		List<UILinkType> types = new ArrayList<>();
 		for (CodelistLink codelistLink:codelist.links()) {
 			types.add(LinkTypes.toUILinkType(codelistLink));
 		}
 		
 		logger.trace("found {} link types", types.size());
 		
 		return new DataWindow<>(types);
 	}
 
 	@Override
 	public List<UICodelist> getCodelists() throws ServiceException {
 		logger.trace("getCodelists");
 		List<UICodelist> codelists = new ArrayList<>();
 		Iterator<Codelist> it = repository.get(allLists()).iterator();
 		while (it.hasNext()) {
 			Codelist codelist = (Codelist) it.next();
 			codelists.add(Codelists.toUICodelist(codelist));
 		}
 		return codelists;
 	}
 
 	@Override
 	public CodelistValueTypes getCodelistValueTypes(String codelistId)	throws ServiceException {
 		logger.trace("getCodelistValueTypes codelistId: {}",codelistId);
 		CodelistSummary summary = repository.get(summary(codelistId));
 
 		//FIXME namespace lost in BE
 		List<AttributeValue> attributeTypes = new ArrayList<>();
 		for (QName name:summary.codeNames()) {
 			for (QName type:summary.codeTypesFor(name)) {
 				Collection<String> languages = summary.codeLanguagesFor(name, type);
 				if (languages.isEmpty()) attributeTypes.add(new AttributeValue(ValueUtils.safeValue(name), ValueUtils.safeValue(type), ""));
 				else {
 					for (String language:languages) {
 						attributeTypes.add(new AttributeValue(ValueUtils.safeValue(name), ValueUtils.safeValue(type), language));
 					}
 				}
 			}
 		}
 		logger.trace("returning {} attribute types", attributeTypes.size());
 		
 		Codelist codelist = repository.lookup(codelistId);
 
 		List<LinkValue> types = new ArrayList<>();
 		for (CodelistLink codelistLink:codelist.links()) {
 			types.add(LinkTypes.toLinkType(codelistLink));
 		}
 
 		logger.trace("returning {} link types", types.size());
 		return new CodelistValueTypes(attributeTypes, types);
 	}
 
 	@Override
 	public List<UILinkTypeInfo> getLinkTypes(String codelistId)	throws ServiceException {
 		logger.trace("getLinkTypes codelistId: {}",codelistId);
 		Codelist codelist = repository.lookup(codelistId);
 		List<UILinkTypeInfo> types = new ArrayList<>();
 		for (CodelistLink link:codelist.links()) types.add(new UILinkTypeInfo(link.id(), ValueUtils.safeValue(link.name())));
 		return types;
 	}
 
 	@Override
 	public List<UICodeInfo> getCodes(String codelistId, String linkTypeId) throws ServiceException {
 		logger.trace("getCodes codelistId: {} linkTypeId: {}",codelistId, linkTypeId);
 		Codelist codelist = repository.lookup(codelistId);
 		CodelistLink link = codelist.links().lookup(linkTypeId);
 		Codelist target = link.target();
 		List<UICodeInfo> codes = new ArrayList<>();
 		for (Code code:target.codes()) codes.add(new UICodeInfo(code.id(), ValueUtils.safeValue(code.name())));
 		return codes;
 	}
 
 	@Override
 	public CodelistRemoveCheckResponse canUserRemove(String codelistId) throws ServiceException {
 		logger.trace("canUserDelete codelistId: {}",codelistId);
 		
 		Lifecycle lifecycle = lifecycleService.lifecycleOf(codelistId);
 		if (lifecycle.state() == DefaultLifecycleStates.sealed) return CodelistRemoveCheckResponse.cannot("the codelist is sealed");
 		
 		if (!currentUser.can(CodelistAction.EDIT.on(codelistId))) return CodelistRemoveCheckResponse.cannot("insufficient premissions"); 
 		return CodelistRemoveCheckResponse.can();
 	}
 	
 	@Override
 	@CodelistTask(EDIT)
 	public void removeCodelist(@Id String codelistId) throws ServiceException {
 		logger.trace("removeCodelist codelistId: {}",codelistId);
 		repository.remove(codelistId);
 	}
 
 	@Override
 	@CodelistTask(VIEW)
 	public DataWindow<UIAttributeType> getCodelistAttributeTypes(@Id String codelistId) throws ServiceException {
 		logger.trace("getCodelistAttributeTypes codelistId: {}",codelistId);
 		List<UIAttributeType> types = new ArrayList<>();
 		Codelist codelist = repository.lookup(codelistId);
 		for (Definition definition:codelist.definitions()) types.add(AttributeTypes.toUIAttributeType(definition));		
 		return new DataWindow<UIAttributeType>(types);
 	}
 }
