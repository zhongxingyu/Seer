 package rh.selectable.impl;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.UnsupportedRepositoryOperationException;
 import javax.jcr.query.InvalidQueryException;
 import javax.jcr.query.qom.Comparison;
 import javax.jcr.query.qom.Constraint;
 import javax.jcr.query.qom.Literal;
 import javax.jcr.query.qom.PropertyValue;
 import javax.jcr.query.qom.QueryObjectModelFactory;
 import javax.jcr.query.qom.Selector;
 import javax.servlet.ServletException;
 
 import org.apache.sling.api.SlingHttpServletRequest;
 import org.apache.sling.api.SlingHttpServletResponse;
 import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
 import org.apache.sling.commons.json.JSONException;
 import org.apache.sling.commons.json.io.JSONWriter;
 import org.apache.sling.jcr.api.SlingRepository;
 import org.osgi.service.component.ComponentContext;
 
 /** 
  * @scr.component immediate="true" metatype="no"
  * @scr.service interface="javax.servlet.Servlet"
  * @scr.property name="sling.servlet.paths" value="/system/list/selectable" 
  */  
 public class SelectableJsonFactory extends SlingSafeMethodsServlet  {
 
 	private static final String[] ANCESTORS = new String[] { "/content",
 			"/apps" };
 
 	/** @scr.reference */  
 	private SlingRepository repository;
 	
 	private Session session;
 	
 	@Override
 	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) 
 		throws ServletException, IOException {
 		
 		PrintWriter out = response.getWriter();		 
 		String category = request.getParameter("category");
 		
 		if(category == null) {
 			throw new IllegalArgumentException("The 'category' property is mandatory!");
 		}
 				
 		try {
 			JSONWriter jsonWriter = new JSONWriter(out);
 			jsonWriter.setTidy(true);
 			jsonWriter = jsonWriter.array();
 			
 			for(String ancestor : ANCESTORS) {
 				
 				NodeIterator iterator = listResources(ancestor, category);
 				
 				while(iterator.hasNext()) {
 					
 					jsonWriter = print(jsonWriter, iterator.nextNode());
 					
 				}				
 			}
 			
 			jsonWriter.endArray();
 			
 		} catch (RepositoryException e) {
 			throw new RuntimeException(e);
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private JSONWriter print(JSONWriter jsonWriter, Node node) throws IOException,
 			RepositoryException, JSONException {
 		
 		String selectableValue = node.hasProperty("selectable-value") == true ? 
 				node.getProperty("selectable-value").getString() : 
 				node.getPath();
 		
 		String selectableText = node.hasProperty("selectable-text") == true ?
 				node.getProperty("selectable-text").getString() :
 				node.getPath();
 				
		String qtip = (String) (node.hasProperty("selectable-qtip") == true ? node.getProperty("") : "");
 		 
 		return jsonWriter.object().
 				key("value").value(selectableValue).
 				key("text").value(selectableText).
 				key("qtip").value(qtip)
 				.endObject();
 	}
 
 	private NodeIterator listResources(String ancestor, String category) throws RepositoryException,
 			InvalidQueryException, UnsupportedRepositoryOperationException {
 		
 		QueryObjectModelFactory qomFactory = session.getWorkspace().getQueryManager().getQOMFactory();
 		
 		Selector selector = qomFactory.selector("nt:base", "selector");
 		
 		PropertyValue categoryValue = qomFactory.propertyValue(selector.getSelectorName(), "selectable-category");
 		Literal categoryLiteral = qomFactory.literal(session.getValueFactory().createValue(category));
 		Comparison categoryComparison = qomFactory.comparison(categoryValue, 
 				QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, categoryLiteral);
 
 		Constraint constraint = qomFactory.descendantNode(selector.getSelectorName(), ancestor);
 		
 		return qomFactory.createQuery(selector, qomFactory.and(categoryComparison, constraint), null, null).execute().getNodes();
 	}
 	
 	protected void activate(ComponentContext context) throws RepositoryException {
 		this.session = repository.loginAdministrative(repository.getDefaultWorkspace());
 	}
 	
 	protected void deactivate(ComponentContext context) {
 		this.session.logout();
 	}
 }
