 package org.iucn.sis.server.extensions.integrity;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.gogoego.api.utils.DocumentUtils;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.io.FieldIO;
 import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
import com.solertium.db.Column;
 import com.solertium.db.DBException;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.TrivialExceptionHandler;
 import com.solertium.vfs.VFS;
 import com.solertium.vfs.VFSPath;
 
 public class IntegrityStructureGenerator {
 	
 	public static final VFSPath CACHED_STRUCTURE = 
 		new VFSPath("/integrity/struct.xml");
 	
 	public static Document generate() {
 		final VFS vfs = SIS.get().getVFS();
 		if (vfs.exists(CACHED_STRUCTURE))
 			try {
 				return vfs.getDocument(CACHED_STRUCTURE);
 			} catch (IOException e) {
 				//??? meh, regenerate...
 				TrivialExceptionHandler.ignore(vfs, e);
 			}
 			
 		final Document document = BaseDocumentUtils.impl.newDocument();
 		document.appendChild(document.createElement("tables"));
 		
		//Manually attach the assessment table
 		ExecutionContext ec = SIS.get().getLookupDatabase();
		try {
			Row row = SIS.get().getExecutionContext().getRow("assessment");
			final Element el = document.createElement("table");
			el.setAttribute("name", "assessment");
			
			for (Column c : row.getColumns()) {
				final String dataType = c.getClass().getSimpleName();
				final String columnName = c.getLocalName();
				
				final Element columnEl = document.createElement("column");
				columnEl.setAttribute("name", columnName);
				columnEl.setAttribute("type", dataType);
				
				el.appendChild(columnEl);
			}
			
			document.getDocumentElement().appendChild(el);
		} catch (DBException e) {
			TrivialExceptionHandler.ignore(ec, e);
		}
		
 		
 		final List<String> allFields =
 			FieldIO.getAllFields();
 		
 		for (String field : allFields) {
 			final Row.Set rs = new Row.Set(); {
 				final SelectQuery query = new SelectQuery();
 				query.select(field, "*");
 				
 				try {
 					ec.doQuery(query, rs);
 				} catch (DBException e) {
 					e.printStackTrace();
 					continue;
 				}
 			}
 			
 			final Element tableEl = document.createElement("table");
 			tableEl.setAttribute("name", field);
 			
 			for (Row row : rs.getSet()) {
 				final String dataType = row.get("data_type").toString();
 				final String columnName = row.get("name").toString();
 				
 				final Element columnEl = document.createElement("column");
 				columnEl.setAttribute("name", columnName);
 				columnEl.setAttribute("type", getType(dataType));
 				
 				if (dataType.startsWith("fk")) {
 					String lookupTable = field + "_" + columnName + "Lookup";
 					try {
 						if (!ec.getMatchingTables(lookupTable).isEmpty()) {
 							columnEl.setAttribute("relatedTable", lookupTable);
 							columnEl.setAttribute("relatedColumn", "id");
 						}
 					} catch (DBException e) {
 						e.printStackTrace();
 						TrivialExceptionHandler.ignore(ec, e);
 					}
 				}
 				
 				tableEl.appendChild(columnEl);
 			}
 			
 			document.getDocumentElement().appendChild(tableEl);
 		}
 		
 		DocumentUtils.writeVFSFile(CACHED_STRUCTURE.toString(), vfs, document);
 		
 		return document;
 	}
 
 	private static String getType(String type) {
 		PrimitiveFieldType pType = PrimitiveFieldType.get(type);
 		if (pType == null)
 			return "CString";
 		
 		switch (pType) {
 			case FOREIGN_KEY_LIST_PRIMITIVE:
 			case FOREIGN_KEY_PRIMITIVE:
 			case INTEGER_PRIMITIVE:
 				return "CInteger";
 			case BOOLEAN_PRIMITIVE:
 				return "CBoolean";
 			case FLOAT_PRIMITIVE:
 				return "CLong";
 			case DATE_PRIMITIVE:
 				return "CDate";
 			default:
 				return "CString";
 		}
 	}
 }
