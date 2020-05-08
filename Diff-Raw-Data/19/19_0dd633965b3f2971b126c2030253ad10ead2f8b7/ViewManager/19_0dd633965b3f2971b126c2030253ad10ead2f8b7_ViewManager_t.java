 /*
  * Displays ISGCI graphs.
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ISGCIGraphCanvas.java,v 2.1 2012/10/28 16:00:51 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.graph.view;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.jgrapht.graph.DefaultEdge;
 import org.jgrapht.graph.SimpleDirectedGraph;
 import org.xml.sax.SAXException;
 
 import teo.data.db.Algo;
 import teo.data.xml.GraphMLWriter;
 import teo.graph.drawing.IDrawingService;
 import teo.isgci.gc.GraphClass;
 import teo.isgci.grapht.GAlg;
 import teo.isgci.grapht.Inclusion;
 import teo.isgci.gui.ISGCIMainFrame;
 import teo.isgci.problem.Problem;
 import y.util.D;
 
 /**
  * A canvas that can display an inclusion graph.
  */
 public class ViewManager {
 	
 	
     private List<IDrawingService> drawingServices = new ArrayList<IDrawingService>();
 
     public void attachDrawingService(IDrawingService service) {
     	drawingServices.add(service);
     }
 
 	private boolean isLoading = false;
 	private Problem problem;
 	private Algo.NamePref namingPref;
 	private List<GraphView> graphs;
 	private boolean drawUnproper;
 
 
     public ViewManager() {
         super();
         problem = null;
         namingPref = Algo.NamePref.BASIC;
         graphs = new ArrayList<GraphView>();
         drawUnproper = true;
     }
 
     public String getCurrentGraphML() throws SAXException {
     	StringWriter writer = null;
     	try {
     		writer = new StringWriter();
             GraphMLWriter graphMLWriter = new GraphMLWriter(writer, 
             		GraphMLWriter.MODE_YED, this.getDrawUnproper(), true);
 
             graphMLWriter.startDocument();
             this.write(graphMLWriter);
             graphMLWriter.endDocument();
 
         	return writer.toString();
         } finally {
             try {
 				writer.close();
 			} catch (IOException e) {
 				/* Ignore this, it won't have any effect */
 				e.printStackTrace();
 			}
         }
     }
     
     /** Gets a list of all the current views */
     public Collection<GraphView> getCurrentViews() {
     	return Collections.unmodifiableCollection(this.graphs);
     }
     
     /** Gets a flat list of all the nodes in the current views */
     public Collection<NodeView> getCurrentNodes() {
     	
     	/* Get a list of all the nodes in all graph views */
     	ArrayList<NodeView> result = new ArrayList<NodeView>();
     	for (GraphView gv : graphs) {
     		for (NodeView node : gv.nodes) {
         		if (!result.contains(node)) {
         			result.add(node);
         		}        			
         	}
     	}
     	return result;
     }
 
     
 public void add(Collection<GraphClass> nodes) {
     	
     	Collection<GraphClass> newNodes = new ArrayList<GraphClass>();
     	for (GraphView gv : graphs) {
     		for (Set<GraphClass> l : gv.getGraph().vertexSet()) {
     			for (GraphClass gc : l) {
     				nodes.add(gc);
     			}
     		}
     	}   
     	
     	graphs.clear();
     	//nodes.removeAll(newNodes);
     	
     	if (nodes.isEmpty()) {
     		return;
     	}
     	
         SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph =
             Algo.createHierarchySubgraph(nodes);
 
         List<SimpleDirectedGraph<Set<GraphClass>,DefaultEdge>> list =
                 GAlg.split(graph, DefaultEdge.class);
         
         try {
         	
         	for (SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g : list) {        		
         		addGraph(g);
             }
         	
         	
             
         } catch (Error e) {
 
             if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
             	graphs.clear();
             } else {
                 throw(e);
             }
         }
 
         this.export();        
 
         /* Notify any listeners that the view was initialized */
         
     }
 
 	public void del(Collection<GraphClass> nodes) {
 	
 		Collection<GraphClass> newNodes = new ArrayList<GraphClass>();
		Collection<GraphClass> done = new ArrayList<GraphClass>();
 		for (GraphView gv : graphs) {
 			for (Set<GraphClass> l : gv.getGraph().vertexSet()) {
 				for (GraphClass gc : l) {
 					boolean contained = false;
 					for (GraphClass mc : Algo.equNodes(gc)) {
						if (nodes.contains(mc) || done.contains(mc)) {
 							contained = true;
 							break;
 						}
 					}
					
 					if (!contained) {
						done.add(gc);
 						newNodes.add(gc);
 						System.out.println(gc.toString() + " drin.");
 					} else {
 						System.out.println(gc.toString() + " deleted.");
 					}
 				}
 			}
 		}   
 		
 		graphs.clear();
 		//nodes.removeAll(newNodes);
 		
 		if (newNodes.isEmpty()) {
 			//return;
 		}
 		
 	    SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph =
 	        Algo.createHierarchySubgraph(newNodes);
 	
 	    List<SimpleDirectedGraph<Set<GraphClass>,DefaultEdge>> list =
 	            GAlg.split(graph, DefaultEdge.class);
 	    
 	    try {
 	    	
 	    	for (SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g : list) {        		
 	    		addGraph(g);
 	        }
 	    	
 	    	
 	        
 	    } catch (Error e) {
 	
 	        if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
 	        	graphs.clear();
 	        } else {
 	            throw(e);
 	        }
 	    }
 	
 	    this.export();        
 	
 	    /* Notify any listeners that the view was initialized */
 	    
 	}
     
     /**
      * Add the given graph to this canvas.
      */
     protected GraphView addGraph(SimpleDirectedGraph<Set<GraphClass>,DefaultEdge> g) {
     	GraphView gv = new GraphView(g);
     	
         gv.setIncludeUnproper(drawUnproper);
         graphs.add(gv);
         
         for (NodeView nv : gv.getNodes()) {
             nv.updateColor(this.problem);
             nv.setNameAndLabel(Algo.getName(nv.getNode(), namingPref)); 
         }
         
         this.refresh();
         return gv;
     }
     
     protected GraphView addToGraph(SimpleDirectedGraph<Set<GraphClass>,DefaultEdge> g) {
     	GraphView gv = new GraphView(g);
     	
         gv.setIncludeUnproper(drawUnproper);
         for (Set<GraphClass> gc : g.vertexSet()) {
         	graphs.get(0).graph.addVertex(gc);
         }
         
 
         //graphs.get(0).graph.addEdge(arg0, arg1)
         
         //graphs.add(gv);
         
         for (NodeView nv : gv.getNodes()) {
             nv.updateColor(this.problem);
             nv.setNameAndLabel(Algo.getName(nv.getNode(), namingPref)); 
         }
         
         this.refresh();
         return gv;
     }
     
     /**
      * Return the NodeView for the given node.
      */
     public NodeView getView(Set<GraphClass> node) {
         for (GraphView gv : graphs) {
             NodeView view = gv.getView(node);
             if (view != null)
                 return view;
         }
         return null;
     }
 
 
 
     public void setDrawUnproper(boolean b) {
         drawUnproper = b;
         for (GraphView gv : graphs)
             gv.setIncludeUnproper(b);
         
         this.refresh();
     }
 
     public boolean getDrawUnproper() {
         return drawUnproper;
     }
 
     /**
      * Write this to w.
      */
     public void write(GraphMLWriter w) throws SAXException {
         for (GraphView gv : graphs)
             gv.write(w);
     }
    
     
 
     /**
      * Create a hierarchy subgraph of the given classes
      */
     public void load(Collection<GraphClass> nodes) {
     	this.isLoading = true;
     	
         SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph =
             Algo.createHierarchySubgraph(nodes);
 
         List<SimpleDirectedGraph<Set<GraphClass>,DefaultEdge>> list =
                 GAlg.split(graph, DefaultEdge.class);
         
         try {
 
             graphs.clear();
             for (SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g : list) {
                 addGraph(g);
             }
         } catch (Error e) {
 
             if (e instanceof OutOfMemoryError || e instanceof StackOverflowError) {
             	graphs.clear();
             } else {
                 throw(e);
             }
         }
 
         /* Notify any listeners that the view was initialized */
         D.bug("loading graph. " + drawingServices.size() + " listeners");
         for (IDrawingService ds : drawingServices) {
             ds.initializeView(this.graphs);
         }
     	this.isLoading = false;
     }
     
     public void refresh() {
         if (!isLoading) {
 	        /* Notify any attached services that the view should be refreshed */
 	        for (IDrawingService ds : drawingServices) {
 	            ds.updateView(this.graphs);
 	        }
         }
     }
 
     /**
      * Set all nodes to their preferred names.
      */
     public void setPreferedNames() {
         for (GraphView gv : graphs)
             for (NodeView v : gv.getNodes())
                v.setNameAndLabel(Algo.getName(v.getNode(), namingPref)); 
         
         this.refresh();
     }
 
 
     /**
      * Find the NodeView for the given graph class or null if not found
      */
     public NodeView findNode(GraphClass gc) {
         for (GraphView gv : graphs) {
             for (NodeView v : gv.getNodes())
                 if (v.getNode().contains(gc))
                     return v;
         }
         return null;
     }
      
 
     /**
      * Bit of a hack to get all ISGCI stuff in one place:
      * Set the appropriate properness of the given edgeview.
      */
     protected void setProperness(EdgeView view) {
     	SimpleDirectedGraph<GraphClass, Inclusion> inclusionGraph = 
     					ISGCIMainFrame.DataProvider.getInclusionGraph();
         List<Inclusion> path = GAlg.getPath(inclusionGraph,
                 view.getFrom().iterator().next(),
                 view.getTo().iterator().next());
         view.setProper(Algo.isPathProper(path)  ||
                 Algo.isPathProper(Algo.makePathProper(path)));
     }
 
 
     /**
      * Set coloring for p and repaint.
      */
     public void setProblem(Problem p) {
         if (problem != p) {
             problem = p;
             setComplexityColors();
             this.refresh();
         }
     }
 
     public Problem getProblem() {
         return problem;
     }
     
     public boolean export() {
     	boolean res = true;
     	FileOutputStream f;
     	try {
     		File  myFile = new File("D:/myGraph.graphml");
     		f = new FileOutputStream(myFile);
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	        return false;
 	    }
 	
 	    try {
 	    	exportGML(f);            
 	    } catch (Exception e) {
 	        res = false;
 	        e.printStackTrace();
 	    }
 	    return res;
     }
 
 	/**
 	 * Export to GraphML.
 	 */
 	protected void exportGML(FileOutputStream f) throws Exception {
 		Exception res = null;
 	    Writer out = null;
 	    
 	    try {
 	        out = new OutputStreamWriter(f, "UTF-8");
 	        GraphMLWriter w = new GraphMLWriter(out,GraphMLWriter.MODE_YED,
 	               this.getDrawUnproper(),true);
 	
 	        w.startDocument();
 	        this.write(w);
 	        w.endDocument();
 	    } catch (IOException ex)  {
 	        res = ex;
 	    } finally {
 	        out.close();
 	    }
 	    
 	    if (res != null)
 	        throw res;
 	}
 
 
     /**
      * Set all nodes to the proper complexity color.
      */
     public void setComplexityColors() {
         for (GraphView gv : graphs)
             for (NodeView v : gv.getNodes())
                v.updateColor(this.problem);
     }
 
 
     public void setNamingPref(Algo.NamePref pref) {
         namingPref = pref;
         setPreferedNames();
     }
 
     public Algo.NamePref getNamingPref() {
         return namingPref;
     }
 
 
 }
 
 /* EOF */
