 package lu.sven.epcmodeler.http;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.Locale;
 
 import lu.sven.epcmodeler.EPCModeler;
 import lu.sven.epcmodeler.graph.Edge;
 import lu.sven.epcmodeler.graph.Node;
 import lu.sven.epcmodeler.util.NodeUtil;
 import lu.sven.epcmodeler.util.Transformers;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.MethodNotSupportedException;
 import org.apache.http.entity.ContentProducer;
 import org.apache.http.entity.EntityTemplate;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.io.GraphMLWriter;
 
 public class HttpReqHandler implements HttpRequestHandler {
 	Logger logger = Logger.getRootLogger();
 	Graph<Node,Edge> graph;
 	
 	public HttpReqHandler() {
         super();
     }
     
     public HttpReqHandler(Graph<Node,Edge> g) {
         super();
         this.graph = g;
     }
     
     public void handle(
             final HttpRequest request, 
             final HttpResponse response,
             final HttpContext context) throws HttpException, IOException {
     	
     	// Check for the request method
         String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
         if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
             throw new MethodNotSupportedException(method + " method not supported"); 
         }
 		String target = request.getRequestLine().getUri();
 
 		String postContent = "";
         if (request instanceof HttpEntityEnclosingRequest) {
             HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
             byte[] entityContent = EntityUtils.toByteArray(entity);
             postContent = new String(entityContent);
             logger.debug("Incoming entity content (bytes): " + entityContent.length);
            	logger.debug("The content is: " + postContent);
         }
         
         EntityTemplate body;
         
         logger.debug("The target: "+target);
         
         if(this.graph == null) {
         	throw new NullPointerException("No graph defined!");
         }
         
         if(target.equals("/addNode")) {
         	Node n = new Node(postContent);
         	EPCModeler.receivedNodes.add(n);
         	if(!this.graph.getVertices().contains(n)) {
         		this.graph.addVertex(n);
             	logger.debug("Node inserted");
         	}
         	response.setStatusCode(HttpStatus.SC_OK);
             // generate output
             body = new EntityTemplate(new ContentProducer() {
             	public void writeTo(final OutputStream outstream) throws IOException {
             		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
             		writer.write("Computers are useless. They can only give you answers. --Pablo Picasso");
             		writer.flush();
             	}    
             });
         } else if(target.equals("/addEdge")) {
             	Edge e = new Edge(postContent);
             	EPCModeler.receivedEdges.add(e);
             	if(!this.graph.getEdges().contains(e)) {
             		this.graph.addEdge(e, NodeUtil.getNodeById(e.source, this.graph), NodeUtil.getNodeById(e.dest, this.graph));
                 	logger.debug("Edge inserted");
             	}
            	
             	response.setStatusCode(HttpStatus.SC_OK);
                 // generate output
                 body = new EntityTemplate(new ContentProducer() {
                 	public void writeTo(final OutputStream outstream) throws IOException {
                 		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
                 		writer.write("Computers are useless. They can only give you answers. --Pablo Picasso");
                 		writer.flush();
                 	}    
                 });
         } else if(target.equals("/rmNode")) {
         	this.graph.removeVertex(new Node(postContent));
         	
         	response.setStatusCode(HttpStatus.SC_OK);
             // generate output
             body = new EntityTemplate(new ContentProducer() {
             	public void writeTo(final OutputStream outstream) throws IOException {
             		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
             		writer.write("The files that had been deleted were surgically removed from the database. They specifically were the files the company needed to survive. --William Hoffman");
             		writer.flush();
             	}    
             });
         } else if(target.equals("/updateNode")) {
         	Node n = new Node(postContent);
         	try {
         		this.graph.removeVertex(n);
         	} catch (Exception e) {
         		// not found
         		// TODO: debug
         	}
         	
         	this.graph.addVertex(n);
         	
         	response.setStatusCode(HttpStatus.SC_OK);
             // generate output
             body = new EntityTemplate(new ContentProducer() {
             	public void writeTo(final OutputStream outstream) throws IOException {
             		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
             		writer.write("The challenge is to update software without introducing problems. --Wietse Venema");
             		writer.flush();
             	}    
             });
         } else if(target.equals("/getGraph")) {
         	logger.info("Got a /getGraph request! I will now send the entire graph...");
         	response.setStatusCode(HttpStatus.SC_OK);
             // generate output
             body = new EntityTemplate(new ContentProducer() {
             	public void writeTo(final OutputStream outstream) throws IOException {
             		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
             		
             		GraphMLWriter<Node, Edge> graphWriter = new GraphMLWriter<Node, Edge> ();
             		
             		graphWriter.setVertexIDs(Transformers.setVertexId);
         			
         			// Add the X Position
         	    	graphWriter.addVertexData("x", null, "0", Transformers.setXValue);
         	    	
         	    	// Add the Y Position
         	    	graphWriter.addVertexData("y", null, "0", Transformers.setYValue);
         	    	
         	    	// Add a label
         	    	graphWriter.addVertexData("label", null, "Node", Transformers.setLabel);
         	    	
         	    	// Add the NodeType
         	    	graphWriter.addVertexData("type", null, "FUNCTION", Transformers.setNodeType);
         	    	
         	    	// Add the timestamp
         	    	graphWriter.addVertexData("timestamp", null, "0", Transformers.setTimestamp);
         	    	
         	    	// Add the AccessType
         	    	graphWriter.addVertexData("access", null, "0", Transformers.setAccess);
         	    	
         	    	// Add the State
         	    	graphWriter.addVertexData("state", null, "0", Transformers.setState);
         			
         			graphWriter.save(graph, writer);
         			
             		writer.flush();
             	}    
             });
         } else if(target.equals("/getNodeSize")) {
         	
         	response.setStatusCode(HttpStatus.SC_OK);
             // generate output
             body = new EntityTemplate(new ContentProducer() {
             	public void writeTo(final OutputStream outstream) throws IOException {
             		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
             		String nodeSize = Integer.toString(graph.getVertexCount());
             		writer.write(nodeSize);
             		writer.flush();
             	}    
             });
         }else {
         	response.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
             // generate output
             body = new EntityTemplate(new ContentProducer() {
             	public void writeTo(final OutputStream outstream) throws IOException {
             		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
             		writer.write("The Internet? Is that thing still around? -- Homer Simpson");
             		writer.flush();
             	}     
             });
         }
         
         body.setContentType("text/xml; charset=UTF-8");
         response.setEntity(body);   
     }
 }
