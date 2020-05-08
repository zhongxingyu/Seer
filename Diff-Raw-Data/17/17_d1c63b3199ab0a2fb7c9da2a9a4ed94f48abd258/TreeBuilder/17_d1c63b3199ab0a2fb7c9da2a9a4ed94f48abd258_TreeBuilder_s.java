 package org.iucn.sis.server.api.fields;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.NamingException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
import com.solertium.db.ConversionException;
 import com.solertium.db.DBException;
 import com.solertium.db.DBSession;
 import com.solertium.db.DBSessionFactory;
 import com.solertium.db.ExecutionContext;
 import com.solertium.db.Row;
 import com.solertium.db.SystemExecutionContext;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.util.AlphanumericComparator;
 import com.solertium.util.BaseDocumentUtils;
 
 public class TreeBuilder {
 	
 private final ExecutionContext ec;
 	
 	public TreeBuilder() throws NamingException {
 		this(DBSessionFactory.getDBSession("sis_lookups"));
 	}
 	
 	public TreeBuilder(DBSession session) {
 		ec = new SystemExecutionContext(session);
 		ec.setAPILevel(ExecutionContext.SQL_ALLOWED);
 		ec.setExecutionLevel(ExecutionContext.ADMIN);
 	}
 	
 	public Document buildTree(String fieldName) {
 		final Document document = BaseDocumentUtils.impl.newDocument();
 		final Element root = document.createElement("tree");
 		
 		buildTree(fieldName, document, root);
 		
 		document.appendChild(root);
 		
 		return document;
 	}
 	
 	public void buildTree(String fieldName, Document document, Element element) {
 		final SelectQuery query = new SelectQuery();
 		query.select(fieldName + "Lookup", "level", "ASC");
 		query.select(fieldName + "Lookup", "*");
 		
 		final Row.Set rs = new Row.Set();
 		
 		try {
 			ec.doQuery(query, rs);
 		} catch (DBException e) {
 			//Debug.println(e);
 			return;
 		}
 		
 		final List<TreeNode> roots = new ArrayList<TreeBuilder.TreeNode>();
 		final Map<String, TreeNode> nodes = new HashMap<String, TreeBuilder.TreeNode>();
 
 		for (Row row : rs.getSet()) {
 			final TreeNode node = new TreeNode(row);
			final Integer parent;
 			if ((parent = node.getParent()) == null)
 				roots.add(node);
 			/*
 			 * Sorting by level ensures there will 
 			 * not be a case that the parent node 
 			 * is not already mapped.  The only case 
 			 * for this will be the root case, handled 
 			 * above.
 			 */
 			else if (nodes.containsKey(parent))
 				nodes.get(parent).addChild(node);
 			
 			nodes.put(node.getCode(), node);
 		}
 		
 		Collections.sort(roots, new TreeNodeComparator());
 		
 		for (TreeNode node : roots) {
 			final Row row = node.getRow();
 			
 			final Element el = document.createElement("root");
 			el.setAttribute("code", row.get("id").toString());
 			el.setAttribute("codeable", bitToBoolean(row.get("codeable").toString()));
 			el.setAttribute("depth", row.get("level").toString());
 			el.setAttribute("id", row.get("ref").toString());
 			
 			el.appendChild(BaseDocumentUtils.impl.createElementWithText(
 				document, "label", row.get("description").toString()));
 			
 			writeChildren(document, el, node);
 			
 			element.appendChild(el);
 		}
 		
 	}
 	
 	private String bitToBoolean(String bit) {
 		if ("0".equals(bit))
 			return "false";
 		else if ("1".equals(bit))
 			return "true";
 		else
 			return Boolean.toString("true".equals(bit));
 	}
 	
 	private void writeChildren(Document document, Element parentEl, TreeNode parent) {
 		for (TreeNode node : parent.getSortedChildren()) {
 			final Row row = node.getRow();
 			
 			final Element el = document.createElement("child");
 			el.setAttribute("code", row.get("id").toString());
 			el.setAttribute("codeable", bitToBoolean(row.get("codeable").toString()));
 			el.setAttribute("depth", row.get("level").toString());
 			el.setAttribute("id", row.get("ref").toString());
 			
 			el.appendChild(BaseDocumentUtils.impl.createElementWithText(
 					document, "label", row.get("description").toString()));
 			
 			writeChildren(document, el, node);
 			
 			parentEl.appendChild(el);
 		}
 	}
 	
 	private static class TreeNode {
 		
 		private final Row row;
 		private final List<TreeNode> children;
 		
 		public TreeNode(Row row) {
 			this.row = row;
 			this.children = new ArrayList<TreeBuilder.TreeNode>();
 		}
 		
 		public String getCode() {
 			return row.get("code").toString();
 		}
 		
		public Integer getParent() {
			try {
				return row.get("parentID").getInteger();
			} catch (ConversionException e) {
 				return null;
			}
 		}
 		
 		public Row getRow() {
 			return row;
 		}
 		
 		public void addChild(TreeNode node) {
 			children.add(node);
 		}
 		
 		public List<TreeNode> getSortedChildren() {
 			Collections.sort(children, new TreeNodeComparator());
 			return children;
 		}
 		
 	}
 	
 	public static class TreeNodeComparator implements Comparator<TreeNode> {
 		
 		private final AlphanumericComparator comparator = 
 			new AlphanumericComparator();
 		
 		public int compare(TreeNode arg0, TreeNode arg1) {
 			String[] splitA = arg0.getRow().get("ref").toString().split("\\.");
 			String[] splitB = arg1.getRow().get("ref").toString().split("\\.");
 			
 			Integer lenA = splitA.length, lenB = splitB.length;
 			
 			//Should never be the case, but...
 			int c1 = lenA.compareTo(lenB);
 			if (c1 != 0)
 				return c1;
 			
 			//Guaranteed to be the same length...
 			for (int i = 0; i < splitA.length; i++) {
 				String slotA = splitA[i], slotB = splitB[i];
 				
 				int value = comparator.compare(slotA, slotB);
 				if (value != 0)
 					return value;
 			}
 			
 			return 0;
 		}
 		
 	}
 
 }
