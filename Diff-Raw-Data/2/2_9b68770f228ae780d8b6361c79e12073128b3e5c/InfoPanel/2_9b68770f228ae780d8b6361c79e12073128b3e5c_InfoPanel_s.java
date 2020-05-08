 /*
  * Information panel for a hovored/selected GraphElement.Displayed information changes with mouse hover content.  When the mouse is
  * not hovering over an element, all selected elements will have their information
  * rendered.
  */
 package nodes;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 
 import controlP5.Button;
 import controlP5.CallbackEvent;
 import controlP5.CallbackListener;
 import controlP5.ControlP5;
 import controlP5.Textarea;
 import java.util.Collection;
 
 import processing.core.PApplet;
 import processing.core.PFont;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import org.apache.jena.riot.RDFDataMgr;
 import org.openjena.riot.RiotException;
 
 /**
  *
  * @author kdbanman
  */
 public class InfoPanel extends PApplet implements Selection.SelectionListener {
     int w, h;
     
     int padding;
     int elementHeight;
     int buttonWidth;
     int buttonHeight;
     
     PFont infoFont;
     PFont eventFont;
     
     ControlP5 cp5;
     Graph graph;
     
     // update flag raised if hovered or selected graph elemnents have changed.
     //   see selectionChanged() and draw().
     AtomicBoolean updateNecessary;
     
     // default string for infobox when single GraphElement is not selected
     String infoDefault;
     
     // string to add events to
     String eventLogString;
     
     // scrollable text area to render triples readable according to selection
     // or to provide application instructions
     Textarea infoBox;
     
     // scrollable text area to log events/feedback to the user
     Textarea eventLog;
     
     // exploration buttons to query web/SPARQL for information about selection
     Button exploreWeb;
     Button exploreSparql;
     
     public InfoPanel(int frameWidth, int frameHeight, Graph parentGraph) {
         w = frameWidth;
         h = frameHeight;
         
         try {
             infoFont = createFont("resources/labelFont.ttf", 12, true);
         } catch (Exception e) {
             System.out.println("ERROR: font not loaded.  ensure labelFont.tiff is in program directory.");
             infoFont = cp5.getFont().getFont();
         }
         
         try {
             eventFont = createFont("resources/labelFont.ttf", 11, false);
         } catch (Exception e) {
             System.out.println("ERROR: font not loaded.  ensure labelFont.tiff is in program directory.");
             eventFont = cp5.getFont().getFont();
         }
         
         padding = 10;
         elementHeight = 20;
         buttonWidth = 200;
         buttonHeight = 30;
         
         // initialize graph
         graph = parentGraph;
         
         updateNecessary = new AtomicBoolean();
         
         infoDefault = "Select a single node or edge for information about it " +
                 "to be rendered here.\n\nTo get more information, use the exploration buttons to the " +
                 "right to:\n - Query the node/edge URI over the web\n - Query the SPARQL endpoint " +
                 "at the address in Load Triples -> SPARQL -> Endpoint IP:Port or URL\n";
         
         eventLogString = "";
     }
     
     @Override
     public void setup() {
         // subscribe to changes in selection.  see overridden selectionChanged()
         graph.getSelection().addListener(this);
         
         size(w, h);
         frameRate(25);
         
         cp5 = new ControlP5(this)
                 .setMoveable(false);
     
         // scrollable text area to render triples readable
         infoBox = cp5.addTextarea("Data Window")
                 .setPosition(padding, padding)
                 .setSize(w - 3 * padding - buttonWidth, h - 2 * padding - 50)
                 .setText(infoDefault)
                 .setFont(infoFont);
         
         eventLog = cp5.addTextarea("Event Log")
                 .setPosition(w - padding - buttonWidth, 4 * padding + 3 * buttonHeight)
                 .setSize(buttonWidth, h - 5 * padding - 3 * buttonHeight - 25)
                 .setText(eventLogString)
                 .setFont(eventFont)
                 .setColor(0xFFFF5555);
         
         // TODO: move these to main control panel
         // exploration buttons to query the web or the database
         exploreWeb = cp5.addButton("Explore Web")
                 .setPosition(w - padding - buttonWidth, padding)
                 .setSize(buttonWidth, buttonHeight)
                 .addCallback(new WebExplorationListener());
         exploreSparql = cp5.addButton("Explore SPARQL endpoint")
                 .setPosition(w - padding - buttonWidth, 2 * padding + buttonHeight)
                 .setSize(buttonWidth, buttonHeight)
                 .addCallback(new SparqlExplorationListener());
         
     }
     
     // draw() is called every time a frame is rendered
     @Override
     public void draw() {
         background(0);
         
         // if the update flag has been raised, rerender the text in the infobox
         if (updateNecessary.getAndSet(false)) {
             String toDisplay = "";
             
             // if no elements are hovered over by mouse and selection is empty,
             // then display the default text
             if (!graph.pApp.getHovered().isEmpty() ||
                 graph.getSelection().nodeCount() + graph.getSelection().edgeCount() != 0) {
                 // if elements are hovered over, display their information
                 // instead of selected elements
                 if (!graph.pApp.getHovered().isEmpty()) {
                     toDisplay += "==================\n" +
                                  "Mouseover Elements\n" +
                                  "==================\n\n";
                     toDisplay += renderElementSetString(graph.pApp.getHovered());
                     toDisplay += "\n\n";
                 } else {
                     // render information about selected nodes
                     if (graph.getSelection().nodeCount() != 0) {
                         toDisplay += "==============\n" +
                                     "Selected Nodes\n" +
                                     "==============\n\n";
                         toDisplay += renderElementSetString(graph.getSelection().getNodes());
                         toDisplay += "\n\n";
                     }
                     // render information about selected edges
                     if (graph.getSelection().edgeCount() != 0) {
                         toDisplay += "==============\n" +
                                     "Selected Edges\n" +
                                     "==============\n\n";
                         toDisplay += renderElementSetString(graph.getSelection().getEdges());
                     }
                 }
             } else {
                 // no graph elements are being inspected, so display the default text
                 toDisplay = infoDefault;
             }
             infoBox.setText(toDisplay);
         }
     }
     
     /**
      * set the flag to update the infopanel text (i.e. if the selection or 
      * mouseover elements have changed)
      */
     public void updateNextFrame() {
         updateNecessary.compareAndSet(false, true);
     }
     
     // every time selection is changed, this is called
     @Override
     public void selectionChanged() {
         // if the selection has changed, then the text will need to be updated,
         // so set the flag.
         updateNextFrame();
     }
     
     /**
      * renders a collection of graph elements as a sequence of strings describing
      * their local data neihborhood.
      * 
      * @param elements collection of graph elements whose data neighborhoods
      * will be serialized sequentially.
      */
     private String renderElementSetString(Collection<? extends GraphElement> elements) {
         String setInfo = "";
         String barDivide = "\n\n===================================\n\n";
         try {
             for (GraphElement e : elements) {
                 setInfo += renderedElementString(e);
                 setInfo += barDivide;
             }
         } catch (java.util.ConcurrentModificationException exc) {
             // if, during iteration through the collection of graph elements,
             // that collection is modified, then this catch block is executed.
             // the string setInfo will be rerendered next frame, so return the
             // error string as a passive indicator of the (transient state)
             return "<comodification error in renderElementSetString()>";
         }
         if (setInfo.length() != 0) 
             setInfo = setInfo.substring(0, setInfo.length() - barDivide.length());
         
         return setInfo;
     }
     
     // log event to user.  sufficient newlines automatically added.
     public void logEvent(String s) {
         s = s.trim();
         eventLogString = ">>>>>\n\n" + s + "\n\n" + eventLogString;
         eventLog.setText(eventLogString);
     }
     
     /*
      * returns a well-formatted description of the passed graph element based
      * on its data neighborhood
      */
     private String renderedElementString(GraphElement e) {
         if (e instanceof Node) {
             return renderedNodeString((Node) e);
         } else {
             return renderedEdgeString((Edge) e);
         }
     }
     
     /*
      * returns a well formatted description of the entity the passed Node
      * represents based on the existing (imported) data.  do not pass null
      * values.
      */
     private String renderedNodeString(Node n) {
         // construct string describing currently rendered data neighborhood
         String rendered = "Data Neigborhood for:  " + graph.prefixed(n.getName()) + "\n\n";
         
         StmtIterator outbound = graph.getRenderedTriples().listStatements(graph.getResource(n.getName()), null, (RDFNode) null);
         while (outbound.hasNext()) {
             Statement s = outbound.next();
             rendered += "  " + graph.prefixed(s.getPredicate().toString()) + "  "
                     + graph.prefixed(s.getObject().toString()) + "\n";
         }
         rendered += "\n";
         
         StmtIterator inbound = graph.getRenderedTriples().listStatements(null, null, (RDFNode) graph.getResource(n.getName()));
         if (!inbound.hasNext()) {
             inbound = graph.getRenderedTriples().listStatements(null, null, n.getName());
         }
         if (!inbound.hasNext()) {
             //TODO: figure out a way to get literal statuments working.  they don't
             // show up in the infopanel as "is <pred> of <uri resource>
         }
         while (inbound.hasNext()) {
             Statement s = inbound.next();
             rendered += "  is  " + graph.prefixed(s.getPredicate().toString()) + "  of  "
                     + graph.prefixed(s.getSubject().toString()) + "\n";
         }
         return rendered;
     }
     
     /*
      * returns a well-formatted description of the predicates that the passed
      * edge represents based on the existing data.
      */
     private String renderedEdgeString(Edge e) {
         String rendered = "";
         for (Statement s : e.getTriples()) {
             rendered += graph.prefixed(s.getSubject().toString()) + "  " +
                     graph.prefixed(s.getPredicate().toString()) + "  " +
                     graph.prefixed(s.getObject().toString());
             Node predicateNode = graph.getNode(s.getPredicate().toString());
             if (predicateNode != null) {
                rendered += renderedNodeString(predicateNode);
             } else {
                 rendered += "\n\nNo data yet retrieved about predicate\n  " + s.getPredicate().toString()
                         + "\n\nTry querying the web or a SPARQL instance with the button to the right.";
             }
             rendered += "\n\n";
         }
         
         return rendered;
     }
     
     /*
      * returns String uri of singly selected node or edge, null if selection
      * is not a single element.
      */
     private String getSelectedURI() {
         String uri;
         if (graph.getSelection().nodeCount() == 1 && graph.getSelection().edgeCount() == 0) {
             uri = graph.getSelection().getNodes().iterator().next().getName();
         } else if (graph.getSelection().edgeCount() == 1 && graph.getSelection().nodeCount() == 0) {
             Edge edge = (Edge) graph.getSelection().getEdges().iterator().next();
 
             if (edge.getTriples().size() > 1) {
                 // user needs to choose which triple to explore
                 TripleChooserFrame chooser = new TripleChooserFrame(this, edge);
 
                 // this thread will be started again upon closure of TripleChooserFrame
                 try {
                     synchronized(this) {
                         this.wait();
                     }
                 } catch (InterruptedException e) {
                     System.out.println("ExplorationListener was not able to wait for TripleChooserFrame");
                 }
                 // triple chooser's thread will be sleeping, waiting for this
                 // to retrieve the chosen triple (Statement)
                 uri = chooser.choice().getPredicate().getURI();
                 chooser.close();
             } else {
                 uri = edge.getSingleTriple().getPredicate().getURI();
             }
         } else {
             // more than one GraphElement is selected
             uri = null;
         }
         
         return uri;
     }
     
     /***************************************
      * exploration button callback listeners
      ***************************************/
     
     /*
      * attach to web query button to enable retrieval of rdf
      * descriptions as published at resources' uris.
      */
     private class WebExplorationListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 
                 // get uri from selected node or edge
                 
                 String uri = getSelectedURI();
                 
                 if (uri == null) {
                     logEvent("Select a *single* node or edge to retrieve its data.");
                     return;
                 }
                 
                 // retrieve description as a jena model
                 ///////////////////////////////////////
                 Model toAdd = ModelFactory.createDefaultModel();
         
                 try {
                     RDFDataMgr.read(toAdd, uri);
                 } catch (RiotException e) {
                     logEvent("Valid RDF not hosted at uri \n  " + uri);
                     return;
                 }
                 
                 // add retrieved model to graph
                 ///////////////////////////////
                 
                 int retrievedSize = (int) toAdd.size();
                 int addedSize = graph.tripleCount();
                 
                 // protect from concurrency issues during import
                 graph.pApp.waitForNewFrame(this);
                 
                 // add the retrieved model to the graph (toAdd is empty if 
                 // an error was encountered)
                 graph.addTriples(toAdd);
                 
                 // concurrency issues now over
                 graph.pApp.restartRendering(this);
                 
                 addedSize = graph.tripleCount() - addedSize;
                 
                 // log number of triples added to user
                 logEvent("From uri:\n<" + uri + ">\n  " + 
                          retrievedSize + " triples retrieved,\n  " +
                          addedSize + " triples are new");
                 
                 
                 // queue controller selection update if one is not already queued
                 updateNecessary.compareAndSet(false, true);
             }
         }
     }
     
     /*
      * attach to sparql query button to enable retrieval of rdf
      * descriptions as published at resources' uris.  (coupled to textfield in
      * ControlPanel)
      */
     private class SparqlExplorationListener implements CallbackListener {
         @Override
         public void controlEvent(CallbackEvent event) {
             if (event.getAction() == ControlP5.ACTION_RELEASED) {
                 
                 // get uri from selected node or edge and form query
                 String uri = getSelectedURI();
                 
                 // ensure uri was retrieved
                 if (uri == null) {
                     logEvent("Select a *single* node or edge to retrieve its data.");
                     return;
                 }
                 
                 // form query
                 String queryString = "CONSTRUCT { <" + uri + "> ?p1 ?o . "
                         + "?s ?p2 <" + uri + "> } " + ""
                         + "WHERE { <" + uri + "> ?p1 ?o . "
                         + "?s ?p2 <" + uri + "> }";
                 
                 // get endpoint uri
                 String endpoint = graph.pApp.getSparqlEndpoint();
                 
                 // construct query
                 Query query = QueryFactory.create(queryString);
                 
                 QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query);
                 
                 // retrieve description as a jena model
                 Model toAdd;
                 try {
                     toAdd = qexec.execConstruct();
                 } catch (Exception e) {
                     logEvent("Valid RDF not returned from endpoint:\n" + endpoint +
                             "\n\nCheck endpoint address and status.");
                     return;
                 }
                 
                 // add retrieved model to graph
                 ///////////////////////////////
                 
                 // protect from concurrency issues during import
                 graph.pApp.waitForNewFrame(this);
                 
                 int retrievedSize = (int) toAdd.size();
                 int beforeSize = graph.tripleCount();
                 
                 // add the retriveed model to the graph (toAdd is empty if 
                 // an error was encountered)
                 graph.addTriples(toAdd);
                 
                 int addedSize = graph.tripleCount() - beforeSize;
                 
                 // log number of triples added to user
                 logEvent("From endpoint:\n" + endpoint + "\n\n" +
                          "about uri: \n" + uri + "\n " +
                          retrievedSize + " triples retrieved\n " +
                          addedSize + " triples are new");
                  
                 graph.pApp.restartRendering(this);
                 
                 // queue controller selection update if one is not already queued
                 updateNecessary.compareAndSet(false, true);
             }
         }
     }
 }
