 package com.duggan.workflow.server.helper.dao;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.duggan.workflow.client.model.TaskType;
 import com.duggan.workflow.server.dao.DocumentDaoImpl;
 import com.duggan.workflow.server.dao.model.ADDocType;
 import com.duggan.workflow.server.dao.model.ADValue;
 import com.duggan.workflow.server.dao.model.DetailModel;
 import com.duggan.workflow.server.dao.model.DocumentModel;
 import com.duggan.workflow.server.db.DB;
 import com.duggan.workflow.server.helper.auth.LoginHelper;
 import com.duggan.workflow.shared.model.DataType;
 import com.duggan.workflow.shared.model.DocStatus;
 import com.duggan.workflow.shared.model.Doc;
 import com.duggan.workflow.shared.model.Document;
 import com.duggan.workflow.shared.model.DocumentLine;
 import com.duggan.workflow.shared.model.DocumentType;
 import com.duggan.workflow.shared.model.GridValue;
 import com.duggan.workflow.shared.model.Notification;
 import com.duggan.workflow.shared.model.SearchFilter;
 import com.duggan.workflow.shared.model.Value;
 
 import static com.duggan.workflow.server.helper.dao.FormDaoHelper.*;
 
 /**
  * This class is Dao Helper for persisting all document related entities.
  * 
  * @author duggan
  * 
  */
 public class DocumentDaoHelper {
 
 	public static List<Doc> getAllDocuments(DocStatus status) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		List<DocumentModel> models = dao.getAllDocuments(status);
 
 		List<Doc> lst = new ArrayList<>();
 
 		for (DocumentModel m : models) {
 			lst.add(getDoc(m));
 		}
 
 		return lst;
 	}
 
 	/**
 	 * This method saves updates a document
 	 * 
 	 * @param document
 	 * 
 	 * @return Document
 	 */
 	public static Document save(Document document) {
 		DocumentModel model = getDoc(document);
 
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		if (document.getId() != null) {
 			model = dao.getById(document.getId());
 			model.setDescription(document.getDescription());
 			model.setDocumentDate(document.getDocumentDate());
 			model.setPartner(document.getPartner());
 			model.setPriority(document.getPriority());
 			model.setSubject(document.getSubject());
 			model.setType(getType(document.getType()));
 			model.setValue(document.getValue());
 			model.setStatus(document.getStatus());
 			model.setProcessInstanceId(document.getProcessInstanceId());
 			model.setSessionId(document.getSessionId());
 		}
 
 		model.getValues().clear();
 		Map<String, Value> vals = document.getValues();
 		Collection<Value> values = vals.values();
 		for (Value val : values) {
 			if (val == null || (val instanceof GridValue)) {
 				// Ignore
 				continue;
 			}
 
 			ADValue previousValue = new ADValue();
 			if (val.getId() != null) {
 				previousValue = DB.getFormDao().getValue(val.getId());
 			}
 
 			ADValue adValue = getValue(previousValue, val);
 			assert adValue != null;
 			model.addValue(adValue);
 		}
 
 		// setDetails
 		setDetails(model, document.getDetails());
 
 		// save
 		if(model.getId()==null){
 			if(model.getSubject()==null){
 				model.setSubject(dao.generateDocumentSubject(model.getType()));
				//model.addValue(value);
 			}
 	
 			if(model.getDescription()==null){
 				model.setDescription(model.getSubject());
 			}
 		
 		}
 		
 		model = dao.saveDocument(model);
 
 		Document doc = getDoc(model);
 
 		return doc;
 	}
 
 	private static void setDetails(DocumentModel model,
 			Map<String, List<DocumentLine>> details) {
 
 		if (details.isEmpty())
 			return;
 
 		for (String key : details.keySet()) {
 			List<DocumentLine> docs = details.get(key);
 			for (DocumentLine line : docs) {
 				line.setName(key);
 				setDetails(model, line);
 			}
 		}
 	}
 
 	private static void setDetails(DocumentModel docModel, DocumentLine line) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		DetailModel detail = new DetailModel();
 		if (line.getId() != null) {
 			detail = dao.getDetailById(line.getId());
 		}
 
 		detail.setName(line.getName());
 
 		detail.getValues().clear();
 		Map<String, Value> vals = line.getValues();
 		Collection<Value> values = vals.values();
 		for (Value val : values) {
 			ADValue previousValue = new ADValue();
 			if (val.getId() != null) {
 				previousValue = DB.getFormDao().getValue(val.getId());
 			}
 
 			ADValue adValue = getValue(previousValue, val);
 			assert adValue != null;
 			detail.addValue(adValue);
 		}
 
 		docModel.addDetail(detail);
 
 	}
 
 	public static ADDocType getType(DocumentType type) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		ADDocType adtype = new ADDocType(type.getId(), type.getName(),
 				type.getDisplayName());
 
 		if (type.getId() != null) {
 			adtype = dao.getDocumentTypeById(type.getId());
 			adtype.setName(type.getName());
 			adtype.setDisplay(type.getDisplayName());
 		}
 
 		return adtype;
 	}
 
 	/**
 	 * 
 	 * @param model
 	 * @return
 	 */
 	public static Document getDoc(DocumentModel model) {
 		if (model == null) {
 			return null;
 		}
 		Document doc = new Document();
 		doc.setCreated(model.getCreated());
 		doc.setDescription(model.getDescription());
 		doc.setDocumentDate(model.getDocumentDate());
 		doc.setId(model.getId());
 		doc.setOwner(LoginHelper.get().getUser(model.getCreatedBy()));
 		doc.setSubject(model.getSubject());
 		doc.setType(getType(model.getType()));
 		doc.setDocumentDate(model.getDocumentDate());
 		doc.setPartner(model.getPartner());
 		doc.setPriority(model.getPriority());
 		doc.setValue(model.getValue());
 		doc.setStatus(model.getStatus());
 		doc.setProcessInstanceId(model.getProcessInstanceId());
 		doc.setSessionId(model.getSessionId());
 		doc.setHasAttachment(DB.getAttachmentDao().getHasAttachment(model.getId()));
 		Collection<ADValue> values = model.getValues();
 		if (values != null) {
 			for (ADValue val : values) {
 				// val.
 				DataType type = getDataType(val);
 
 				doc.setValue(val.getFieldName(), getValue(val, type));
 			}
 		}
 
 		doc.setDetails(getDetails(model.getDetails()));
 
 		return doc;
 	}
 
 	private static DataType getDataType(ADValue val) {
 		DataType type = null;
 
 		if (val.getBooleanValue() != null) {
 			type = DataType.BOOLEAN;
 		}
 
 		if (val.getLongValue() != null) {
 			type = DataType.INTEGER;
 		}
 
 		if (val.getDateValue() != null) {
 			type = DataType.DATE;
 		}
 
 		if (val.getDoubleValue() != null) {
 			type = DataType.DOUBLE;
 		}
 
 		if (val.getStringValue() != null) {
 			type = DataType.STRING;
 		}
 
 		return type;
 
 	}
 
 	private static Map<String, List<DocumentLine>> getDetails(
 			Collection<DetailModel> details) {
 		Map<String, List<DocumentLine>> lines = new HashMap<>();
 
 		for (DetailModel lineModel : details) {
 			DocumentLine line = new DocumentLine();
 			line.setDocumentId(lineModel.getDocument().getId());
 			line.setId(lineModel.getId());
 			line.setName(lineModel.getName());
 
 			for (ADValue value : lineModel.getValues()) {
 				Value val = getValue(value, getDataType(value));
 				line.addValue(value.getFieldName(), val);
 			}
 
 			List<DocumentLine> detailz = new ArrayList<>();
 			if (lines.get(line.getName()) != null) {
 				detailz = lines.get(line.getName());
 			} else {
 				lines.put(line.getName(), detailz);
 			}
 
 			if (!detailz.contains(line))
 				detailz.add(line);
 
 		}
 
 		return lines;
 	}
 
 	public static DocumentType getType(ADDocType adtype) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 		DocumentType type = new DocumentType(adtype.getId(), adtype.getName(),
 				adtype.getDisplay(), adtype.getClassName());
 		type.setFormId(dao.getFormId(adtype.getId()));
 		return type;
 	}
 
 	/**
 	 * 
 	 * @param document
 	 * @return
 	 */
 	private static DocumentModel getDoc(Document document) {
 		DocumentModel model = new DocumentModel(document.getId(),
 				document.getSubject(), document.getDescription(),
 				getType(document.getType()));
 
 		model.setDocumentDate(document.getDocumentDate());
 		model.setPartner(document.getPartner());
 		model.setPriority(document.getPriority());
 		model.setValue(document.getValue());
 		model.setCreated(document.getCreated());
 		model.setStatus(document.getStatus());
 		model.setProcessInstanceId(document.getProcessInstanceId());
 		model.setSessionId(document.getSessionId());
 
 		return model;
 	}
 
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static Document getDocument(Long id) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		DocumentModel model = dao.getById(id);
 
 		return getDoc(model);
 	}
 
 	/**
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static Document getDocumentByProcessInstance(Long processInstanceId) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		DocumentModel model = dao
 				.getDocumentByProcessInstanceId(processInstanceId);
 
 		return getDoc(model);
 	}
 
 	/**
 	 * 
 	 * @param content
 	 * @return
 	 */
 	public static Document getDocument(Map<String, Object> content) {
 		Document doc = new Document();
 
 		if (content.get("document") != null) {
 			doc = (Document) content.get("document");
 		} else {
 
 			String description = content.get("description") == null ? null
 					: (String) content.get("description");
 
 			String subject = content.get("subject") == null ? null
 					: (String) content.get("subject");
 
 			String value = content.get("value") == null ? null
 					: (String) content.get("value");
 
 			Integer priority = content.get("priority") == null ? null
 					: (Integer) content.get("priority");
 
 			doc.setDescription(description);
 			doc.setSubject(subject);
 			doc.setValue(value);
 			doc.setPriority(priority);
 		}
 		
 		Object idStr = content.get("documentId");
 		if (idStr == null || idStr.equals("null")) {
 			idStr = null;
 		}
 		Long id = idStr == null ? null : new Long(idStr.toString());
 		doc.setId(id);
 		
 		return doc;
 	}
 
 	/**
 	 * 
 	 * @param docId
 	 * @param isApproved
 	 */
 	public static void saveApproval(Long docId, Boolean isApproved) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 		DocumentModel model = dao.getById(docId);
 		if (model == null) {
 			throw new IllegalArgumentException(
 					"Cannot Approve/Reject document: Unknown Model");
 		}
 
 		model.setStatus(isApproved ? DocStatus.APPROVED : DocStatus.REJECTED);
 
 		dao.saveDocument(model);
 	}
 
 	public static void getCounts(HashMap<TaskType, Integer> counts) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		counts.put(TaskType.DRAFT, dao.count(DocStatus.DRAFTED));
 		counts.put(TaskType.INPROGRESS, dao.count(DocStatus.INPROGRESS));
 		counts.put(TaskType.APPROVED, dao.count(DocStatus.APPROVED));
 		counts.put(TaskType.REJECTED, dao.count(DocStatus.REJECTED));
 		// counts.put(TaskType.FLAGGED, dao.count(DocStatus.));
 	}
 
 	public static List<Document> search(String subject) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 		List<DocumentModel> models = dao.search(subject);
 
 		List<Document> docs = new ArrayList<>();
 		for (DocumentModel doc : models) {
 			docs.add(getDoc(doc));
 		}
 
 		return docs;
 	}
 
 	public static Long getProcessInstanceIdByDocumentId(Long documentId) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		return dao.getProcessInstanceIdByDocumentId(documentId);
 	}
 
 	public static List<Document> search(String userId, SearchFilter filter) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 		List<DocumentModel> models = dao.search(userId, filter);
 		List<Document> docs = new ArrayList<>();
 		for (DocumentModel doc : models) {
 			docs.add(getDoc(doc));
 		}
 
 		return docs;
 	}
 
 	public static DocumentType getDocumentType(String docTypeName) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		ADDocType adtype = dao.getDocumentTypeByName(docTypeName);
 
 		return getType(adtype);
 	}
 
 	public static List<DocumentType> getDocumentTypes() {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 
 		List<ADDocType> adtypes = dao.getDocumentTypes();
 
 		List<DocumentType> types = new ArrayList<>();
 
 		if (adtypes != null)
 			for (ADDocType adtype : adtypes) {
 				types.add(getType(adtype));
 			}
 
 		return types;
 	}
 
 	public static void delete(DocumentLine line) {
 		DocumentDaoImpl dao = DB.getDocumentDao();
 		DetailModel model = dao.getDetailById(line.getId());
 		dao.delete(model);
 	}
 
 }
