 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.mouse;
 
 import java.awt.Color;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.xml.transform.TransformerException;
 
 import org.apache.batik.dom.events.DOMMutationEvent;
 import org.apache.batik.dom.svg.SVGDOMImplementation;
 import org.apache.batik.swing.JSVGCanvas;
 import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
 import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
 import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
 import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
 import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
 import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
 import org.apache.batik.util.RunnableQueue;
 import org.swows.datatypes.SmartFileManager;
 import org.swows.graph.DynamicDatasetMap;
 import org.swows.graph.EventCachingGraph;
 import org.swows.graph.events.DynamicGraph;
 import org.swows.graph.events.DynamicGraphFromGraph;
 import org.swows.producer.DataflowProducer;
 import org.swows.runnable.RunnableContext;
 import org.swows.runnable.RunnableContextFactory;
 import org.swows.time.SystemTime;
 import org.swows.vocabulary.SWI;
 import org.swows.xmlinrdf.DocumentReceiver;
 import org.swows.xmlinrdf.DomDecoder2;
 import org.swows.xmlinrdf.DomEventListener;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.bootstrap.DOMImplementationRegistry;
 import org.w3c.dom.events.Event;
 import org.w3c.dom.events.EventListener;
 import org.w3c.dom.events.EventTarget;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSOutput;
 import org.w3c.dom.ls.LSSerializer;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.DatasetFactory;
 
 public class MouseApp extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private RunnableQueue batikRunnableQueue = null;
 	private EventCachingGraph cachingGraph = null;
 	private boolean graphicsInitialized = false;
 	private Document newDocument = null;
 	private JSVGCanvas svgCanvas = null;
 
 	public MouseApp(String title, final GraphicsConfiguration gc, Graph dataflowGraph) {
 		this(title, gc, dataflowGraph, true);
 	}
 		
 	public MouseApp(String title, final GraphicsConfiguration gc, Graph dataflowGraph, Color bgColor) {
 		this(title, gc, dataflowGraph, true, bgColor);
 	}
 		
 	public MouseApp(String title, final GraphicsConfiguration gc, Graph dataflowGraph, final boolean fullscreen) {
 		this(title, gc, dataflowGraph, fullscreen, gc.getBounds().width, gc.getBounds().height, null);
 	}
 	
 	public MouseApp(String title, final GraphicsConfiguration gc, Graph dataflowGraph, final boolean fullscreen, Color bgColor) {
 		this(title, gc, dataflowGraph, fullscreen, gc.getBounds().width, gc.getBounds().height, bgColor);
 	}
 	
 	public MouseApp(
 			String title, final GraphicsConfiguration gc, Graph dataflowGraph,
 			final boolean fullscreen, int width, int height) {
 		this(title, gc, dataflowGraph, fullscreen, width, height, null);
 	}
 	
 	public MouseApp(
 			String title, final GraphicsConfiguration gc, Graph dataflowGraph,
 			final boolean fullscreen, int width, int height, Color bgColor) {
 		super(title, gc);
 		RunnableContextFactory.setDefaultRunnableContext(new RunnableContext() {
 			@Override
 			public synchronized void run(final Runnable runnable) {
 				try {
 					while (batikRunnableQueue == null || cachingGraph == null) Thread.yield();
 //					while (batikRunnableQueue == null) Thread.yield();
 					final long start = System.currentTimeMillis();
 					batikRunnableQueue.invokeAndWait(new Runnable() {
 						@Override
 						public void run() {
 //							long runEntered = System.currentTimeMillis();
 //							System.out.println(
 //									"Update thread launched in "
 //											+ (runEntered - start) + "ms" );
 							runnable.run();
 //							long afterCascade = System.currentTimeMillis();
 //							System.out.println(
 //									"RDF envent cascade executed in "
 //											+ (afterCascade - runEntered) + "ms" );
 							cachingGraph.sendEvents();
 //							long afterSvgDom = System.currentTimeMillis();
 //							System.out.println(
 //									"SVG DOM updated in "
 //											+ (afterSvgDom - afterCascade) + "ms" );
 						}
 					});
 //					long runFinished = System.currentTimeMillis();
 //					System.out.println(
 //							"SVG updated and repainted in "
 //									+ (runFinished - start + "ms" ) );
 					if (newDocument != null && svgCanvas != null) {
 						batikRunnableQueue = null;
 						Document doc = newDocument;
 						newDocument = null;
 						svgCanvas.setDocument(doc);
 					}
 				} catch(InterruptedException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		});
 //    	final MouseInput tuioGateway =
 //    			new MouseInput(autoRefresh, new RunnableContext() {
 //    				@Override
 //    				public void run(Runnable runnable) {
 //    					try {
 //    						batikRunnableQueue.invokeAndWait(runnable);
 //    					} catch(InterruptedException e) {
 //    						throw new RuntimeException(e);
 //    					}
 //    				}
 //    			});
     	final MouseInput mouseInput = new MouseInput();
     	final SystemTime systemTime = new SystemTime();
     	final DynamicDatasetMap inputDatasetGraph = new DynamicDatasetMap(systemTime.getGraph());
     	inputDatasetGraph.addGraph(Node.createURI(SWI.getURI() + "mouseEvents"), mouseInput.getGraph());
 //		final DynamicDataset inputDatasetGraph = new SingleGraphDataset(mouseInput.getGraph());
 		DataflowProducer applyOps =	new DataflowProducer(new DynamicGraphFromGraph(dataflowGraph), inputDatasetGraph);
 		DynamicGraph outputGraph = applyOps.createGraph(inputDatasetGraph);
 		cachingGraph = new EventCachingGraph(outputGraph);
 //		cachingGraph = new EventCachingGraph( new LoggingGraph(outputGraph, Logger.getRootLogger(), true, true) );
 		svgCanvas = new JSVGCanvas();
         svgCanvas.setSize(width,height);
         
         // Set the JSVGCanvas listeners.
         svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
 
             public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                 //label.setText("Document Loading...");
             }
 
             public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                 //label.setText("Document Loaded.");
             }
         });
 
         svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
 
             public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                 //label.setText("Build Started...");
             }
 
             public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                 //label.setText("Build Done.");
 //                frame.pack();
             }
         });
 
         svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
 
             public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                 //label.setText("Rendering Started...");
             }
 
             public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
         		batikRunnableQueue = svgCanvas.getUpdateManager().getUpdateRunnableQueue();
             	if (!graphicsInitialized) {
             		// Display the frame.
             		pack();
             		if (fullscreen)
             			gc.getDevice().setFullScreenWindow(MouseApp.this);
             		else
             			setVisible(true);
 //            		tuioGateway.connect();
             		graphicsInitialized = true;
             	}
             }
         });
 
         getContentPane().setSize(width, height);
         if (bgColor != null)
         	getContentPane().setBackground(bgColor);
         svgCanvas.setBackground(bgColor);
         getContentPane().add(svgCanvas);
 
         addWindowListener(new WindowAdapter() {
 
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         });
 
         setSize(width, height);
 
 		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
                 
 		Set<DomEventListener> domEventListenerSet = new HashSet <DomEventListener>();
 		domEventListenerSet.add(mouseInput);
 		Map<String,Set<DomEventListener>> domEventListeners = new HashMap <String,Set<DomEventListener>>();
 		domEventListeners.put("click", domEventListenerSet);
 		domEventListeners.put("mousedown", domEventListenerSet);
 		domEventListeners.put("mouseup", domEventListenerSet);
                 
 		final Document xmlDoc =
 				DomDecoder2.decodeOne(
 						cachingGraph,
 //						outputGraph,
 //						new LoggingGraph(cachingGraph, Logger.getRootLogger(), true, true),
 						domImpl /*,
 						new RunnableContext() {
 							@Override
 							public void run(Runnable runnable) {
 								try {
 									batikRunnableQueue.invokeAndWait(runnable);
 								} catch(InterruptedException e) {
 									throw new RuntimeException(e);
 								}
 							}
 						} */,
 						new DocumentReceiver() {
 //							{
 //								(new Thread() {
 //									public void run() {
 //										while (true) {
 //											while (newDocument == null) yield();
 //											RunnableQueue runnableQueue = batikRunnableQueue;
 //											runnableQueue.suspendExecution(true);
 //											batikRunnableQueue = null;
 ////											batikRunnableQueue.getThread().halt();
 ////											batikRunnableQueue = null;
 //											svgCanvas.setDocument(newDocument);
 //											newDocument = null;
 //											batikRunnableQueue.resumeExecution();
 //										}
 //									}
 //								}).start();
 //							}
 //							private Document newDocument = null;
 							@Override
 							public void sendDocument(Document doc) {
 								newDocument = doc;
 							}
                                                                 
 						},domEventListeners);
 
         svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
 
      /*   EventTarget t = (EventTarget) xmlDoc;
 
         if (EventsProducer.getEventsProducer() == null) {
             try {
                              
               EventsProducer.setEventsProducer();      
             } catch (java.lang.ExceptionInInitializerError ex) {
                 ex.printStackTrace();
                 ex.getCause();
             }
         }
 
        t.addEventListener("click", new EventListener() {
 
             public void handleEvent(Event evt) {
                 EventsProducer.getEventsProducer().update(evt);
                 
             }
         }, false);
 
 */
         
 //        DOMImplementation implementation = null;
 //		try {
 //			implementation = DOMImplementationRegistry.newInstance()
 //					.getDOMImplementation("XML 3.0");
 //		} catch (ClassCastException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		} catch (ClassNotFoundException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		} catch (InstantiationException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		} catch (IllegalAccessException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}
 //      	DOMImplementationLS feature = (DOMImplementationLS) implementation.getFeature("LS",
 //        		"3.0");
 //        LSSerializer serializer = feature.createLSSerializer();
 //        LSOutput output = feature.createLSOutput();
 ////        output.setByteStream(System.out);
 //        
 //        OutputStream os;
 //		try {
 //			os = new FileOutputStream("/home/miguel/tmp/Result.svg");
 //		} catch (FileNotFoundException e) {
 //			e.printStackTrace();
 //			throw new RuntimeException(e);
 //		}
 //        output.setByteStream(os);
 //        serializer.write(xmlDoc, output);
         
         EventListener domEventListener =
 				new EventListener() {
 					@Override
 					public void handleEvent(Event event) {
 						DOMMutationEvent domEvent = (DOMMutationEvent) event;
 						System.out.println("*** DOM Changed Event START ***");
 						System.out.println("Event type: " + domEvent.getType());
 						System.out.println("Target: " + domEvent.getTarget());
 						System.out.println("Attr Name: " + domEvent.getAttrName());
 						System.out.println("Attr Change Type: " + domEvent.getAttrChange());
 						System.out.println("Attr New Value: " + domEvent.getNewValue());
 						System.out.println("Attr Prev Value: " + domEvent.getPrevValue());
 						System.out.println("Related Node: " + domEvent.getRelatedNode());
 						System.out.println("*** DOM Changed Event END ***");
 					}
 				};
 				
 		EventListener domGenericEventListener =
 				new EventListener() {
 					@Override
 					public void handleEvent(Event event) {
 				        DOMImplementation implementation = null;
 						try {
 							implementation = DOMImplementationRegistry.newInstance()
 									.getDOMImplementation("XML 3.0");
 						} catch (ClassCastException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						} catch (ClassNotFoundException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						} catch (InstantiationException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						} catch (IllegalAccessException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 				      	DOMImplementationLS feature = (DOMImplementationLS) implementation.getFeature("LS",
 				        		"3.0");
 				        LSSerializer serializer = feature.createLSSerializer();
 				        LSOutput output = feature.createLSOutput();
 				        OutputStream os;
 						try {
 							os = new FileOutputStream("/home/miguel/tmp/Result.svg");
 						} catch (FileNotFoundException e) {
 							e.printStackTrace();
 							throw new RuntimeException(e);
 						}
 				        output.setByteStream(os);
 				        serializer.write(xmlDoc, output);
 					}
 				};
 						
 //        ((EventTarget) xmlDoc)
 //        		.addEventListener(
 //        				"DOMSubtreeModified",
 //        				domGenericEventListener,
 //						false);
 //
 //        ((EventTarget) xmlDoc)
 //				.addEventListener(
 //						"DOMNodeInserted",
 //						domEventListener,
 //						false);
 //        ((EventTarget) xmlDoc)
 //				.addEventListener(
 //						"DOMNodeRemoved",
 //						domEventListener,
 //						false);
 //        ((EventTarget) xmlDoc)
 //				.addEventListener(
 //						"DOMNodeRemovedFromDocument",
 //						domEventListener,
 //						false);
 //        ((EventTarget) xmlDoc)
 //				.addEventListener(
 //						"DOMNodeInsertedIntoDocument",
 //						domEventListener,
 //						false);
 //        ((EventTarget) xmlDoc)
 //				.addEventListener(
 //						"DOMNodeInserted",
 //						domEventListener,
 //						false);
 ////        ((EventTarget) xmlDoc)
 ////				.addEventListener(
 ////						"DOMAttrModified",
 ////						domEventListener,
 ////						false);
 //        ((EventTarget) xmlDoc)
 //				.addEventListener(
 //						"DOMCharacterDataModified",
 //						domEventListener,
 //						false);
 
         svgCanvas.setDocument(xmlDoc);
 
 //        TransformerFactory transformerFactory = TransformerFactory.newInstance();
 //		Transformer transformer;
 //		try {
 //			transformer = transformerFactory.newTransformer();
 //			DOMSource source = new DOMSource(xmlDoc);
 //			StreamResult result =  new StreamResult(System.out);
 //			transformer.transform(source, result);
 //		} catch (TransformerException e) {
 //			e.printStackTrace();
 //		}
 
 	}
         
     public static void main(final String[] args) throws TransformerException {
     	
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 	    GraphicsDevice device = ge.getDefaultScreenDevice();
         GraphicsConfiguration conf = device.getDefaultConfiguration();
         
         if (args.length != 4) {
         	System.out.println("Wrong Number of Arguments!");
        	System.out.println("usage: java -jar swows-mouse.jar <dataflow_uri> <window_title> F(ull screen)/W(indow) <bg_color>");
         	System.exit(0);
         }
 		String mainGraphUrl = args[0];
 		String windowTitle = args[1];
 		char windowMode = args[2].charAt(0);
 		
 		Color color = Color.decode(args[3]);
 		
 		boolean fullScreen = windowMode == 'f' || windowMode == 'F';
 
 		Dataset wfDataset = DatasetFactory.create(mainGraphUrl, SmartFileManager.get());
 		final Graph wfGraph = wfDataset.asDatasetGraph().getDefaultGraph();
 
 		new MouseApp(windowTitle, conf, wfGraph, fullScreen, color);
 		
     }	
 
 }
