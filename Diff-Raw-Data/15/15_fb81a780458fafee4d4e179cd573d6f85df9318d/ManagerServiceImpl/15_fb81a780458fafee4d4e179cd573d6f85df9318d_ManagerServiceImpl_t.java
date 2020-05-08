 package org.cotrix.web.codelistmanager.server;
 
 import static org.cotrix.action.CodelistAction.*;
 import static org.cotrix.repository.Queries.*;
 import static org.cotrix.web.codelistmanager.shared.ManagerUIFeature.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.xml.namespace.QName;
 
 import org.cotrix.domain.Attribute;
 import org.cotrix.domain.Code;
 import org.cotrix.domain.Codelist;
 import org.cotrix.domain.Container;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.query.CodelistQuery;
 import org.cotrix.repository.query.Range;
 import org.cotrix.web.codelistmanager.client.ManagerService;
 import org.cotrix.web.codelistmanager.server.modify.ModifyCommandHandler;
 import org.cotrix.web.codelistmanager.server.util.CodelistLoader;
 import org.cotrix.web.codelistmanager.server.util.ValueUtils;
 import org.cotrix.web.codelistmanager.shared.CodelistGroup;
 import org.cotrix.web.codelistmanager.shared.CodelistMetadata;
 import org.cotrix.web.codelistmanager.shared.ManagerServiceException;
 import org.cotrix.web.codelistmanager.shared.UIAttribute;
 import org.cotrix.web.codelistmanager.shared.UICode;
 import org.cotrix.web.codelistmanager.shared.modify.ModifyCommand;
 import org.cotrix.web.codelistmanager.shared.modify.ModifyCommandResult;
 import org.cotrix.web.share.server.CotrixRemoteServlet;
 import org.cotrix.web.share.server.task.ActionMapper;
 import org.cotrix.web.share.server.task.ContainsTask;
 import org.cotrix.web.share.server.task.Id;
 import org.cotrix.web.share.server.task.Task;
 import org.cotrix.web.share.shared.DataWindow;
 import org.cotrix.web.share.shared.feature.FeatureCarrier;
 import org.cotrix.web.share.shared.feature.Request;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The server side implementation of the RPC service.
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
 	protected CodelistLoader codelistLoader;
 	
 	@Inject
 	protected ModifyCommandHandler commandHandler;
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@PostConstruct
 	public void init() {
 		codelistLoader.importAllCodelist();
 		logger.trace("codelist in repository:");
 		for (Codelist codelist:repository.queryFor(allLists())) logger.trace(codelist.name().toString());
 		logger.trace("done");
 		
 		mapper.map(EDIT.getInnerAction()).to(EDIT_METADATA);
 		mapper.map(LOCK.getInnerAction()).to(LOCK_CODELIST);
 		mapper.map(UNLOCK.getInnerAction()).to(UNLOCK_CODELIST);
 		mapper.map(SEAL.getInnerAction()).to(SEAL_CODELIST);
 	}
 	
 	@Override
 	public DataWindow<CodelistGroup> getCodelistsGrouped() throws ManagerServiceException {
 		logger.trace("getCodelistsGrouped");
 		
 		Map<QName, CodelistGroup> groups = new HashMap<QName, CodelistGroup>();
 		Iterator<org.cotrix.domain.Codelist> it = repository.queryFor(allLists()).iterator();
 		while (it.hasNext()) {
 			org.cotrix.domain.Codelist codelist = (org.cotrix.domain.Codelist) it.next();
 			
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
 	public DataWindow<UICode> getCodelistCodes(@Id String codelistId, com.google.gwt.view.client.Range range) throws ManagerServiceException {
 		logger.trace("getCodelistRows codelistId {}, range: {}", codelistId, range);
 		
 		CodelistQuery<Code> query = allCodes(codelistId);
 		int from = range.getStart();
 		int to = range.getStart() + range.getLength();
 		logger.trace("query range from: {} to: {}", from ,to);
 		query.setRange(new Range(from, to));
 
 		Codelist codelist = repository.lookup(codelistId);
 		
 		Iterable<Code> codes  = repository.queryFor(query);
 		List<UICode> rows = new ArrayList<UICode>(range.getLength());
 		for (Code code:codes) {
 
 			UICode uicode = new UICode();
 			uicode.setId(code.id());
 			uicode.setName(code.name().toString());
 			
 			List<UIAttribute> attributes = new ArrayList<UIAttribute>(code.attributes().size());
 			
 			for (Attribute attribute:code.attributes()) {
 				UIAttribute rowAttribute = toUIAttribute(attribute);
 				attributes.add(rowAttribute);
 			}
 			uicode.setAttributes(attributes);
 			rows.add(uicode);
 		}
 		logger.trace("retrieved {} rows", rows.size());
 		return new DataWindow<UICode>(rows, codelist.codes().size());
 	}
 
 	@Override
 	public CodelistMetadata getMetadata(String codelistId) throws ManagerServiceException {
 		logger.trace("getMetadata codelistId: {}", codelistId);
 		Codelist codelist = repository.lookup(codelistId);
 		CodelistMetadata metadata = new CodelistMetadata();
 		metadata.setId(codelist.id());
 		metadata.setName(ValueUtils.safeValue(codelist.name()));
 		metadata.setVersion(codelist.version());
 		metadata.setAttributes(getAttributes(codelist.attributes()));
 		return metadata;
 	}
 	
 	protected List<UIAttribute> getAttributes(Container<? extends Attribute> attributesContainer)
 	{
 		List<UIAttribute> attributes = new ArrayList<UIAttribute>(attributesContainer.size());
 		
 		for (Attribute domainAttribute:attributesContainer) {
 			UIAttribute attribute = toUIAttribute(domainAttribute);
 			attributes.add(attribute);
 		}
 		
 		return attributes;
 	}
 	
 	protected UIAttribute toUIAttribute(Attribute attribute)
 	{
 		UIAttribute uiattribute = new UIAttribute();
 		uiattribute.setName(ValueUtils.safeValue(attribute.name()));
 		uiattribute.setType(ValueUtils.safeValue(attribute.type()));
 		uiattribute.setLanguage(ValueUtils.safeValue(attribute.language()));
 		uiattribute.setValue(ValueUtils.safeValue(attribute.value()));
 		uiattribute.setId(ValueUtils.safeValue(attribute.id()));
 		return uiattribute;
 	}
 
 	@Task(LOCK)
 	public FeatureCarrier.Void saveMessage(String message) {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@Task(LOCK)
 	public FeatureCarrier.Void lock(Request<Void> request) {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@Task(UNLOCK)
 	public FeatureCarrier.Void unlock(Request<Void> request) {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	@Task(SEAL)
 	public FeatureCarrier.Void seal(Request<Void> request) {
 		return FeatureCarrier.getVoid();
 	}
 
 	@Override
 	//@Task(EDIT)
 	public ModifyCommandResult modify(@Id String codelistId, ModifyCommand command) throws ManagerServiceException {
 		try {
 		return commandHandler.handle(codelistId, command);
 		} catch(Throwable throwable)
 		{
 			logger.error("Error executing command "+command+" on codelist "+codelistId, throwable);
 			throw new ManagerServiceException(throwable.getMessage());
 		}
 	}
 
 	@Override
 	public void removeCodelist(String codelistId) throws ManagerServiceException {
 		logger.trace("removeCodelist codelistId: {}",codelistId);
 		repository.remove(codelistId);
 	}
 
 	
 }
