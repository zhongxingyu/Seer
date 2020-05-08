 package org.cotrix.web.codelistmanager.server;
 
 import static org.cotrix.action.CodelistAction.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.repository.CodelistQueries.*;
 import static org.cotrix.web.codelistmanager.shared.ManagerUIFeature.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.xml.namespace.QName;
 
 import org.cotrix.action.events.CodelistActionEvents;
 import org.cotrix.application.VersioningService;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.common.Attribute;
 import org.cotrix.lifecycle.Lifecycle;
 import org.cotrix.lifecycle.LifecycleService;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.MultiQuery;
 import org.cotrix.web.codelistmanager.client.ManagerService;
 import org.cotrix.web.codelistmanager.server.modify.ChangesetUtil;
 import org.cotrix.web.codelistmanager.server.modify.ModifyCommandHandler;
 import org.cotrix.web.codelistmanager.shared.CodelistEditorSortInfo;
 import org.cotrix.web.codelistmanager.shared.CodelistGroup;
 import org.cotrix.web.codelistmanager.shared.Group;
 import org.cotrix.web.codelistmanager.shared.modify.ModifyCommand;
 import org.cotrix.web.codelistmanager.shared.modify.ModifyCommandResult;
 import org.cotrix.web.share.server.CotrixRemoteServlet;
 import org.cotrix.web.share.server.task.ActionMapper;
 import org.cotrix.web.share.server.task.CodelistTask;
 import org.cotrix.web.share.server.task.ContainsTask;
 import org.cotrix.web.share.server.task.Id;
 import org.cotrix.web.share.server.util.Codelists;
 import org.cotrix.web.share.server.util.ValueUtils;
 import org.cotrix.web.share.shared.DataWindow;
 import org.cotrix.web.share.shared.codelist.UICode;
 import org.cotrix.web.share.shared.codelist.UICodelistMetadata;
 import org.cotrix.web.share.shared.exception.ServiceException;
 import org.cotrix.web.share.shared.feature.FeatureCarrier;
 import org.cotrix.web.share.shared.feature.ResponseWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The server side implementation of the RPC serialiser.
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @SuppressWarnings("serial")
 @ContainsTask
 public class ManagerServiceImpl implements ManagerService {
 
 	public static class Servlet extends CotrixRemoteServlet {
 
 		@Inject
 		protected ManagerServiceImpl bean;
 
 		@Override
 		public Object getBean() {
 			return bean;
 		}
 	}
 
 	protected Logger logger = LoggerFactory.getLogger(ManagerServiceImpl.class);
 
 	@Inject
 	ActionMapper mapper;
 
 	@Inject
 	CodelistRepository repository;
 
 	@Inject
 	protected ModifyCommandHandler commandHandler;
 
 	@Inject
 	protected VersioningService versioningService;
 
 	@Inject
 	protected LifecycleService lifecycleService;
 
 	@Inject
 	private Event<CodelistActionEvents.CodelistEvent> events;
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@PostConstruct
 	public void init() {
 		mapper.map(VIEW).to(VIEW_CODELIST, VIEW_METADATA);
 		mapper.map(EDIT).to(EDIT_METADATA, EDIT_CODELIST, ADD_CODE, REMOVE_CODE);
 		mapper.map(LOCK).to(LOCK_CODELIST);
 		mapper.map(UNLOCK).to(UNLOCK_CODELIST);
 		mapper.map(SEAL).to(SEAL_CODELIST);
 	}
 
 	@Override
 	public DataWindow<CodelistGroup> getCodelistsGrouped() throws ServiceException {
 		logger.trace("getCodelistsGrouped");
 
 		Map<QName, CodelistGroup> groups = new HashMap<QName, CodelistGroup>();
 		Iterator<org.cotrix.domain.codelist.Codelist> it = repository.get(allLists()).iterator();
 		while (it.hasNext()) {
 			org.cotrix.domain.codelist.Codelist codelist = (org.cotrix.domain.codelist.Codelist) it.next();
			logger.trace("codelist {}", codelist);
 
 			CodelistGroup group = groups.get(codelist.name());
 			if (group == null) {
 				group = new CodelistGroup(codelist.name().toString());
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
 	public Set<Group> getAttributesGroups(@Id String codelistId) throws ServiceException {
 		logger.trace("getAttributesGroups codelistId {}", codelistId);
 
 		Iterable<Code> codes  = repository.get(allCodesIn(codelistId));
 		Set<Group> groups = GroupFactory.getGroups(codes);
 		
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
 	public FeatureCarrier.Void  lock(@Id String codelistId) throws ServiceException {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@CodelistTask(UNLOCK)
 	public FeatureCarrier.Void  unlock(@Id String codelistId) throws ServiceException {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@CodelistTask(SEAL)
 	public FeatureCarrier.Void  seal(@Id String codelistId) throws ServiceException {
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
 	@CodelistTask(EDIT)
 	public void removeCodelist(String codelistId) throws ServiceException {
 		logger.trace("removeCodelist codelistId: {}",codelistId);
 		repository.remove(codelistId);
 	}
 
 	@Override
 	@CodelistTask(VERSION)
 	public CodelistGroup createNewCodelistVersion(@Id String codelistId, String newVersion)
 			throws ServiceException {
 		Codelist codelist = repository.lookup(codelistId);
 		Codelist newCodelist = versioningService.bump(codelist).to(newVersion);
 		repository.add(newCodelist);
 		lifecycleService.start(newCodelist.id());
 
 		events.fire(new CodelistActionEvents.Version(newCodelist.id(),newCodelist.name(),newVersion));
 
 		CodelistGroup group = new CodelistGroup(newCodelist.name().toString());
 		group.addVersion(newCodelist.id(), newCodelist.version());
 
 		return group;
 	}
 
 	@Override
 	@CodelistTask(VIEW)
 	public ResponseWrapper<String> getCodelistState(@Id String codelistId) throws ServiceException {
 		logger.trace("getCodelistState codelistId: {}",codelistId);
 		Lifecycle lifecycle = lifecycleService.lifecycleOf(codelistId);
 		String state = lifecycle.state().toString();
 		return ResponseWrapper.wrap(state.toUpperCase());
 	}
 }
