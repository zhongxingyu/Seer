 package org.cotrix.web.codelistmanager.server;
 
 import static org.cotrix.action.CodelistAction.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.domain.trait.Change.*;
 import static org.cotrix.repository.Queries.*;
 import static org.cotrix.web.codelistmanager.shared.ManagerUIFeature.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
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
 import org.cotrix.web.codelistmanager.server.util.CodelistLoader;
 import org.cotrix.web.codelistmanager.server.util.ValueUtils;
 import org.cotrix.web.codelistmanager.shared.CodelistGroup;
 import org.cotrix.web.codelistmanager.shared.CodelistMetadata;
 import org.cotrix.web.codelistmanager.shared.ManagerServiceException;
 import org.cotrix.web.codelistmanager.shared.UICodelistRow;
 import org.cotrix.web.share.server.CotrixRemoteServlet;
 import org.cotrix.web.share.server.task.ActionMapper;
 import org.cotrix.web.share.server.task.ContainsTask;
 import org.cotrix.web.share.server.task.Id;
 import org.cotrix.web.share.server.task.Task;
 import org.cotrix.web.share.shared.CSVFile;
 import org.cotrix.web.share.shared.CotrixImportModel;
 import org.cotrix.web.share.shared.DataWindow;
 import org.cotrix.web.share.shared.Metadata;
 import org.cotrix.web.share.shared.UIAttribute;
 import org.cotrix.web.share.shared.UICode;
 import org.cotrix.web.share.shared.UICodelist;
 import org.cotrix.web.share.shared.feature.Request;
 import org.cotrix.web.share.shared.feature.Response;
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
 	
 	public ArrayList<UICodelist> getAllCodelists() throws IllegalArgumentException {
 
 		ArrayList<UICodelist> list = new ArrayList<UICodelist>();
 		Iterator<org.cotrix.domain.Codelist> it = repository.queryFor(allLists()).iterator();
 		while (it.hasNext()) {
 			org.cotrix.domain.Codelist codelist = (org.cotrix.domain.Codelist) it
 					.next();
 			UICodelist c = new UICodelist();
 			c.setName(codelist.name().toString());
 			c.setId(codelist.id());
 			list.add(c);
 		}
 		return list;
 	}
 
 	public void addCode(ArrayList<UICode> codes){
 		Attribute.Private a = (Attribute.Private) attr().name("TAXOCODE").value("OOOOOOO").as(NEW).build();
 		Code.Private code = (Code.Private) code().name("LAU").attributes(a).as(NEW).build();
 		Codelist changeset = codelist("id").name("sss").with(code).as(NEW).build();
 
 	}
 
 	public void editCode(ArrayList<UICode> editedCodes) {
 		for (UICode code : editedCodes) {
 			Attribute changedAttribute = attr(code.getAttribute().getId()).name(code.getAttribute().getName()).value(code.getAttribute().getValue()).as(MODIFIED).build();
 			Code changeCode =  code(code.getId()).name(code.getName()).attributes(changedAttribute).as(MODIFIED).build();
 			Codelist changeset = codelist(code.getParent().getId()).name(code.getParent().getName()).with(changeCode).as(MODIFIED).build();
 			repository.update(changeset);
 		}
 		/*
 		Attribute a =  attr().name(code.getAttribute().getName()).value(code.getAttribute().getValue()).as(NEW).build();
 		Code c = code().name(code.getName()).attributes(a).as(NEW).build();
 		Codelist codelist = codelist(code.getParent().getId()).name(code.getParent().getName()).with(c).as(NEW).build();*/
 	}
 
 	public CotrixImportModel getCodeListModel(String codelistId) {
 		logger.trace("getCodeListModel codelistId {} ", codelistId);
 		
 		Codelist codelist = repository.lookup(codelistId);
 		logger.trace("lookup found {}", codelist.name().toString());
 		logger.trace("codelist size {}", codelist.codes().size());
 		
 
 		Metadata meta = new Metadata();
 		meta.setName(codelist.name().toString());
 		meta.setOwner("FAO");
 		meta.setRowCount(codelist.codes().size());
 		meta.setVersion(codelist.version());
 		meta.setDescription("This data was compiled by hand from the above and may contain errors. One small modification is that the various insular areas of the United State listed above are recorded here as the single United States Minor Outlying Islands.");
 
 		CSVFile csvFile = new CSVFile();
 		csvFile.setData(new ArrayList<String[]>());
 		csvFile.setHeader(getHeader(codelist));
 
 		CotrixImportModel model = new CotrixImportModel();
 		model.setMetadata(meta);
 		model.setCsvFile(csvFile);
 		model.setTotalRow(codelist.codes().size());
 		return model;
 	}
 
 	private String[] getHeader(Codelist codelist) {
 		CodelistQuery<Code> codes = allCodes(codelist.id());
 		codes.setRange(new Range(0, 1));
 		String[] line = null;
 		Iterable<Code> inrange = repository.queryFor(codes);
 
 		Iterator<Code> it = inrange.iterator();
 		while (it.hasNext()) {
 			Code code = (Code) it.next();
 			line = new String[code.attributes().size()];
 			Iterator it2 = code.attributes().iterator();
 			int index = 0;
 			while (it2.hasNext()) {
 				Attribute a = (Attribute) it2.next();
 				line[index++] = a.name().toString();
 			}
 		}
 		return line;
 	}
 
 	public ArrayList<UICode[]> getDataRange(String id, int start, int end) {
 		logger.trace("getDataRange id {}, start: {}, end: {}", id, start, end);
 		
 		
 		ArrayList<UICode[]> data = new ArrayList<UICode[]>();
 		CodelistQuery<Code> codes = allCodes(id);
 		codes.setRange(new Range(start,end));
 
 		Codelist codelist = repository.lookup(id);
 		Iterable<Code> inrange  = repository.queryFor(codes);
 		Iterator<Code> it = inrange.iterator();
 		while (it.hasNext()) {
 			Code code = (Code)  it.next();
 			System.out.println("attribute size = "+code.attributes().size());
 			UICode[] line = new UICode[code.attributes().size()];
 			Iterator<Attribute> it2 =  (Iterator<Attribute>) code.attributes().iterator();
 			int index = 0 ;
 			while (it2.hasNext()) {
 				Attribute a = (Attribute) it2.next();
 
 				UIAttribute uiAttr = new UIAttribute();
 				uiAttr.setName(a.name().toString());
 				uiAttr.setType(a.type().toString());
 				uiAttr.setLanguage(a.language());
 				uiAttr.setValue(a.value());
 				uiAttr.setId(a.id());
 
 				UICodelist uiCodeList = new UICodelist();
 				uiCodeList.setId(codelist.id());
 				uiCodeList.setName(codelist.name().toString());
 
 				UICode uiCode = new UICode();
 				uiCode.setAttribute(uiAttr);
 				uiCode.setId(code.id());
 				uiCode.setName(code.name().toString());
 				uiCode.setParent(uiCodeList);
 
 				line[index++] = uiCode;
 
 			}
 			data.add(line);	
 		}
 		return data;
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
 			group.addVersion(codelist.id(), codelist.version());
 		}
 		
 		for (CodelistGroup group:groups.values()) Collections.sort(group.getVersions()); 
 		
 		return new DataWindow<CodelistGroup>(new ArrayList<CodelistGroup>(groups.values()));
 	}
 
 	@Override
 	public DataWindow<UICodelistRow> getCodelistRows(@Id String codelistId, com.google.gwt.view.client.Range range) throws ManagerServiceException {
 		logger.trace("getCodelistRows codelistId {}, range: {}", codelistId, range);
 		
 		CodelistQuery<Code> query = allCodes(codelistId);
		int from = range.getStart();
		int to = range.getStart() + range.getLength();
		logger.trace("query range from: {} to: {}", from ,to);
		query.setRange(new Range(from, to));
 
 		Codelist codelist = repository.lookup(codelistId);
 		
 		Iterable<Code> codes  = repository.queryFor(query);
 		List<UICodelistRow> rows = new ArrayList<UICodelistRow>(range.getLength());
 		for (Code code:codes) {
 
 			UICodelistRow row = new UICodelistRow(code.id(), code.id(), code.name().toString());
 			
 			Map<String, UIAttribute> rowAttributes = new HashMap<String, UIAttribute>(code.attributes().size());
 			
 			for (Attribute attribute:code.attributes()) {
 				UIAttribute rowAttribute = toUIAttribute(attribute);
 				rowAttributes.put(attribute.name().toString(), rowAttribute);
 			}
 			row.setAttributes(rowAttributes);
 			rows.add(row);
 		}
		logger.trace("retrieved {} rows", rows.size());
 		return new DataWindow<UICodelistRow>(rows, codelist.codes().size());
 	}
 
 	@Override
 	public CodelistMetadata getMetadata(String codelistId) throws ManagerServiceException {
 		logger.trace("getMetadata codelistId: {}", codelistId);
 		Codelist codelist = repository.lookup(codelistId);
 		CodelistMetadata metadata = new CodelistMetadata();
 		metadata.setId(codelist.id());
 		metadata.setName(codelist.name().toString());
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
 	
 	protected UIAttribute toUIAttribute(Attribute domainAttribute)
 	{
 		UIAttribute attribute = new UIAttribute();
 		attribute.setName(ValueUtils.safeValue(domainAttribute.name()));
 		attribute.setType(ValueUtils.safeValue(domainAttribute.type()));
 		attribute.setLanguage(ValueUtils.safeValue(domainAttribute.language()));
 		attribute.setValue(ValueUtils.safeValue(domainAttribute.value()));
 		attribute.setId(ValueUtils.safeValue(domainAttribute.id()));
 		return attribute;
 	}
 
 	@Override
 	public void saveMetadata(String codelistId, CodelistMetadata metadata) throws ManagerServiceException {
 		logger.trace("saveMetadata codelistId: {}, metadata {}", codelistId, metadata);
 		Attribute[] attributes = toDomainAttributes(metadata.getAttributes());
 		Codelist changeset = codelist(codelistId).name(metadata.getName()).attributes(attributes).version(metadata.getVersion()).as(MODIFIED).build();
 		//TODO attributes?
 		repository.update(changeset);
 	}
 	
 	protected Attribute[] toDomainAttributes(Collection<UIAttribute> attributes)
 	{
 		Attribute[] domainAttributes = new Attribute[attributes.size()];
 		Iterator<UIAttribute> iterator = attributes.iterator();
 		for (int i = 0; i < domainAttributes.length; i++) {
 			domainAttributes[i] = toDomainAttribute(iterator.next());
 		}
 		return domainAttributes;
 	}
 	
 	protected Attribute toDomainAttribute(UIAttribute attribute)
 	{
 		return attr(attribute.getId()).name(attribute.getName()).value(attribute.getValue()).ofType(attribute.getType()).in(attribute.getLanguage()).build();
 	}
 	
 	@Task(LOCK)
 	public Response<Void> saveMessage(String message) {
 		return new Response<Void>();
 	}
 
 	@Override
 	@Task(LOCK)
 	public Response<Void> lock(Request<Void> request) {
 		return new Response<Void>();
 	}
 
 	@Override
 	@Task(UNLOCK)
 	public Response<Void> unlock(Request<Void> request) {
 		return new Response<Void>();
 	}
 
 	@Override
 	@Task(SEAL)
 	public Response<Void> seal(Request<Void> request) {
 		return new Response<Void>();
 	}
 
 	@Override
 	public void saveCodelistRow(String codelistId, UICodelistRow row) throws ManagerServiceException {
 		//FIXME why name???
 		Codelist codelist = repository.lookup(codelistId);
 		
 		Codelist changeset = codelist(codelistId).name(codelist.name()).with(toCode(row)).as(MODIFIED).build();
 		repository.update(changeset);
 	}
 	
 	protected Code toCode(UICodelistRow row)
 	{
 		return code(row.getId()).name(row.getName()).attributes(toDomainAttributes(row.getAttributes())).build();
 	}
 
 	
 }
