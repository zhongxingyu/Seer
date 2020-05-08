 package org.iucn.sis.server.extensions.fieldmanager.restlets;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.naming.NamingException;
 
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.fields.FieldSchemaGenerator;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.server.api.utils.DocumentUtils;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyListPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.ForeignKeyPrimitiveField;
 import org.iucn.sis.shared.api.models.primitivefields.PrimitiveFieldType;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.InputRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 
 import com.solertium.db.ExecutionContext;
 import com.solertium.util.portable.XMLWritingUtils;
 import com.solertium.vfs.VFS;
 import com.solertium.vfs.VFSPath;
 
 public class UIRestlet extends BaseServiceRestlet {
 	
 	private final FieldSchemaGenerator generator;
 	private final ExecutionContext ec;
 
 	public UIRestlet(Context context, FieldSchemaGenerator generator) {
 		super(context);
 		
 		this.generator = generator;
 		this.ec = generator.getExecutionContext();
 	}
 
 	@Override
 	public void definePaths() {
 		paths.add("/application/manager/{schema}/views");
 		paths.add("/application/manager/{schema}/field/{fieldName}/structure");
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response) throws ResourceException {
 		String schema = (String)request.getAttributes().get("schema");
 		
 		final VFS vfs = SIS.get().getVFS();
 		if ("views".equals(request.getResourceRef().getLastSegment())) {
 			final VFSPath uri = new VFSPath("/browse/docs/fields/" + schema + "/views.xml");
 			if (!vfs.exists(uri.getCollection())) {
 				try {
 					vfs.makeCollections(uri.getCollection());
 				} catch (IOException e) {
 					throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
 				}
 			}
 			
 			if (!vfs.exists(uri)) {
 				
 				try {
 					Document viewDoc = vfs.getDocument(new VFSPath("/browse/docs/views.xml"));
 					DocumentUtils.writeVFSFile(uri.toString(), vfs, viewDoc);
 				} catch (IOException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 			}
 			
 			try {
 				return new InputRepresentation(vfs.getInputStream(uri), MediaType.TEXT_XML);
 			} catch (IOException e) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 			}
 		}	
 		else {
 			final String fieldName = (String)request.getAttributes().get("fieldName");
 			final VFSPath uri = new VFSPath("/browse/docs/fields/" + schema + "/" + fieldName + ".xml");
 			
 			if (vfs.exists(uri)) {
 				try {
 					return new InputRepresentation(vfs.getInputStream(uri), MediaType.TEXT_XML);
 				} catch (IOException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 			}
 			else {
 				if (!vfs.exists(uri.getCollection())) {
 					try {
 						vfs.makeCollections(uri.getCollection());
 					} catch (IOException e) {
 						throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
 					}
 				}
 				
 				Field field = getField(fieldName);
 				
 				final StringBuilder xml = new StringBuilder();
 				xml.append("<field>\r\n");
 				xml.append(XMLWritingUtils.writeTag("canonicalName", field.getName()) + "\r\n");
 				xml.append(XMLWritingUtils.writeCDATATag("description", field.getName()) + "\r\n");
 				xml.append(XMLWritingUtils.writeTag("classOfService", "None/Factor/Species Attributes") + "\r\n");
 				xml.append("<structures>" + "\r\n");
 				for (PrimitiveField prim : field.getPrimitiveField()) {
 					xml.append("<structure id=\"" + prim.getName() + "\" description=\"Optional Prompt:\">" + "\r\n");
 					xml.append(getDefaultStructureForPrim(field, prim) + "\r\n");
 					xml.append("</structure>" + "\r\n");
 				}
 				xml.append("</structures>" + "\r\n");
 				xml.append("</field>");
 				
 				if (DocumentUtils.writeVFSFile(uri.toString(), vfs, xml.toString()))
 					return new StringRepresentation(xml.toString(), MediaType.TEXT_XML);
 				else
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to write structure to VFS");
 			}
 		}
 	}
 	
 	@Override
 	public void handlePut(Representation entity, Request request, Response response) throws ResourceException {
 		String fieldName = (String)request.getAttributes().get("fieldName");
 		if (fieldName == null && "views".equals(request.getResourceRef().getLastSegment()))
 			fieldName = "views";
 		
 		String schema = (String)request.getAttributes().get("schema");
 		
 		final VFS vfs = SIS.get().getVFS();
 		final VFSPath uri = new VFSPath("/browse/docs/fields/" + schema + "/" + fieldName + ".xml");
 		
 		if (DocumentUtils.writeVFSFile(uri.toString(), vfs, getEntityAsDocument(entity)))
 			response.setStatus(Status.SUCCESS_CREATED);
 		else
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 	}
 	
 	private String getDefaultStructureForPrim(Field parent, PrimitiveField field) {
 		if ("qualifier".equals(field.getName()))
 			return "<qualifier/>";
 		else if ("justification".equals(field.getName()))
 			return "<justification/>";
 		
 		PrimitiveFieldType type = PrimitiveFieldType.get(field.getSimpleName());
 		if (type == null)
 			return "<text></text>";
 		
 		switch (type) {
 		case RANGE_PRIMITIVE:
 			return "<range/>";
 		case BOOLEAN_PRIMITIVE:
 			return "<boolean/>";
 		case BOOLEAN_RANGE_PRIMITIVE:
 			return "<booleanRange/>";
 		case BOOLEAN_UNKNOWN_PRIMITIVE:
			return "<booleanUnknown>";
 		case DATE_PRIMITIVE:
 			return "<date/>";
 		case FLOAT_PRIMITIVE:
 		case INTEGER_PRIMITIVE:
 			return "<number/>";
 		case STRING_PRIMITIVE:
 		case TEXT_PRIMITIVE:
 			return "<text/>";
 		case FOREIGN_KEY_LIST_PRIMITIVE:
 			return "<multipleSelect>\r\n" + getOptions(field) + "</multipleSelect>";
 		case FOREIGN_KEY_PRIMITIVE:
 			return "<singleSelect>\r\n" + getOptions(field) + "</singleSelect>";
 		default:
 			return "<text></text>";
 		}
 	}
 	
 	private String getOptions(PrimitiveField field) {
 		final String table;
 		if (field instanceof ForeignKeyPrimitiveField)
 			table = ((ForeignKeyPrimitiveField)field).getTableID();
 		else if (field instanceof ForeignKeyListPrimitiveField)
 			table = ((ForeignKeyListPrimitiveField)field).getTableID();
 		else
 			return "";
 		
 		final Map<Integer, String> map;
 		try {
 			map = generator.loadLookup(table);
 		} catch (Exception e) {
 			return "";
 		}
 		
 		if (map == null || map.isEmpty())
 			return "";
 		
 		StringBuilder out = new StringBuilder();
 		for (String value : map.values())
 			out.append(XMLWritingUtils.writeCDATATag("option", value) + "\r\n");
 		
 		return out.toString();
 	}
 	
 	private Field getField(String fieldName) {
 		try {
 			return generator.getField(fieldName);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 }
