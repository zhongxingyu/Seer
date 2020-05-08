 /*
  *  Copyright (C) 2012-2013 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animotron.
  *
  *  Animotron is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of
  *  the License, or (at your option) any later version.
  *
  *  Animotron is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of
  *  the GNU Affero General Public License along with Animotron.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.animotron.animi.cortex;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.animotron.animi.Params;
 import org.animotron.animi.RuntimeParam;
 import org.animotron.animi.acts.LearningHebbian;
 import org.animotron.animi.gui.Application;
 import org.animotron.matrix.MatrixDelay;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import static java.lang.Math.*;
 
 /**
  * @author <a href="mailto:aldrd@yahoo.com">Alexey Redozubov</a>
  * @author <a href="mailto:gazdovsky@gmail.com">Evgeny Gazdovsky</a>
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  */
 public class MultiCortex implements Runnable {
 
     public static final int STOP = 0;
     public static final int PAUSE = 1;
     public static final int STEP = 2;
     public static final int RUN = 3;
 
     public static int MODE = STOP;
 
     private final Application app;
     
     @RuntimeParam(name = "frequency")
 	public int frequency = 0; // Hz
 
     private long frame = 0;
     private long t0 = System.currentTimeMillis();
 
     private boolean run = true;
     
     public long count = 0;
     
     public Retina retina;
 
     public LayerSimple z_in;
     public LayerWLearning layer_1;
     public LayerWLearning layer_2;
     
     @Params
     public LayerSimple [] zones;
 
     private MultiCortex(Application app, LayerSimple [] zones) {
     	this.app = app;
     	this.zones = zones;
 
         retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
         retina.setNextLayer(zones[0]);
     }
 
     public MultiCortex(Application app) {
     	this.app = app;
     	
     	final int delay = 8;
         z_in = new LayerSimple("Зрительный нерв", this, 30, 30, 1,
     		new MatrixDelay.Attenuation() {
 
 				@Override
 				public float next(int step, float value) {
 					if (step > delay)
 						return 0f;
 					
 					return (float) ((pow(step - delay, 2) / pow(delay, 2)) * value);
 				}
         	}
         );
         
         //1st zone
         layer_1 = new LayerWLearning("1й", this, 5, 5, 9, //120, 120, //160, 120,
             new Mapping[]{
                 new MappingHebbian(z_in, 100, 1, true, true) //7x7 (50)
             },
             LearningHebbian.class
         );
         
 //        layer_2 = new LayerWLearning("2й", this, 5, 5, 1, //120, 120, //160, 120,
 //            new Mapping[]{
 //                new MappingHebbian(layer_1, 25, 1, true, false) //7x7 (50)
 ////                new MappingSOM(layer_1, 25, 1, 25,
 ////            		new Value() {
 ////	    				@Override
 ////	    				public float value(int x1, int y1, int x2, int y2, double sigma) {
 ////	    	            	return 1 / (float)25;
 ////	    				}
 ////	            	}
 ////            	)
 //            },
 //            LearningHebbian.class
 ////            LearningSOM.class
 //        );
 
 //        z_1st.addMappring(z_1st);
         
         zones = new LayerSimple[]{z_in, layer_1};//, layer_2};
         
         retina = new Retina(Retina.WIDTH, Retina.HEIGHT);
         retina.setNextLayer(z_in);
     }
     
     public void init() {
     	// Flush all pending tasks
         flush();
 
 		for (LayerSimple zone : zones) {
 			zone.init();
 		}
     }
     
     public void addTask(Task task) throws InterruptedException {
 		task.execute();
     }
 
     /**
      * Flush all pending tasks and finish all running tasks
      */
     public void flush() {
     }
 
 	@Override
 	public void run() {
         while (run) {
 			try {
 				if (paused) {
 					synchronized (this) {
 						this.wait();
 					}
 				}
                 long t = System.currentTimeMillis();
 				
 				process();
                 
                 if (frequency != 0) {
                     t = (1000 / frequency) - (System.currentTimeMillis() - t);
 
                     if (t > 0)
                     	Thread.sleep(t);
                     else
                     	//give some rest any way
                     	Thread.sleep(5);
 
                 } else {
                 	//give some rest any way
                 	Thread.sleep(5);
                 }
 
 			} catch (Throwable e) {
 				e.printStackTrace();
 			} finally {
                 frame++;
                 long t = System.currentTimeMillis();
                 long dt = t - t0;
                 if (dt > 1000) {
                     app.fps = 1000 * frame / dt;
                     frame = 0;
                     t0 = t;
                 }
             }
 		}
 	}
 	
 	public void process() {
         if (MODE >= STEP) {
         	retina.process(app.getStimulator());
         	
     		for (LayerSimple zone : zones) {
     			zone.process();
     		}
     		count++;
     		
     		if (MODE == STEP) {
     			MODE = STOP;
         		app.count.setText(String.valueOf(count));
         		app.refresh();
     		
     		} else if (MODE == RUN && count % 10 == 0) {
         		app.count.setText(String.valueOf(count));
     			app.refresh();
 
     		}
         }
 	}
 	
 	private Thread th = null;
 	private volatile boolean paused = false;
 	
 	public synchronized void start() {
 		if (th != null) {
 			th.interrupt();
 		}
 		
 		MODE = RUN;
 
 		run = true;
 		paused = false;
 		
 		th = new Thread(this);
 		th.setDaemon(true);
 		th.start();
 	}
 
 	public synchronized void stop() {
 		MODE = (MODE == RUN) ? PAUSE : STOP;
 
 		run = false;
 		try {
 			if (th != null) {
 				th.join();
 			}
 		} catch (InterruptedException e) {
 			th.interrupt();
 		}
 		paused = true;
 		th = null;
 	}
 
 	public void resume() {
 		paused = false;
 		synchronized (this) {
 			notifyAll();
 		}
 	}
 
     public void save(Writer out) throws IOException {
     	out.write("<cortex>");
 		for (LayerSimple zone : zones) {
 			zone.save(out);
 		}
     	out.write("</cortex>");
     }
 
 
 	public static MultiCortex load(Application app, File file) throws IOException {
 		try {
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser parser = factory.newSAXParser();
 			SAXPars saxp = new SAXPars(app);
 	
 			parser.parse(file, saxp);
 			
 			return saxp.mc;
 		} catch (IOException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new IOException(e);
 		}
 	}
 	
 	public static class SAXPars extends DefaultHandler { 
 		
 		Application app;
 		
 		MultiCortex mc = null;
 		List<LayerSimple> zones = new ArrayList<LayerSimple>();
 		
 		LayerSimple prevZone = null;
 		LayerSimple zone = null;
 		
 		double fX, fY = 0;
 		
 		List<MappingHebbian> mappings = new ArrayList<MappingHebbian>();
 		
 		public SAXPars(Application app) {
 			this.app = app;
 		}
 		
 		@Override
 	    public void startElement (String uri, String localName, String qName, Attributes attrs) {
 			if ("linkS".equals(qName)) {
 				
 //				new LinkQ(
 //						prevZone.getCol(
 //							Integer.valueOf(attrs.getValue("sX")),
 //							Integer.valueOf(attrs.getValue("sY"))
 //						),
 //						cn, 
 //						Double.valueOf(attrs.getValue("w")),
 //						fX,
 //						fY,
 //						Double.valueOf(attrs.getValue("speed"))
 //					);
 
 			} else if ("linkI".equals(qName)) {
 				
 //				new Link(
 //						zone.getCol(
 //							Integer.valueOf(attrs.getValue("sX")),
 //							Integer.valueOf(attrs.getValue("sY"))
 //						),
 //						cn, 
 //						Double.valueOf(attrs.getValue("w")),
 //						LinkType.INHIBITORY
 //					);
 				
 				
 			} else if ("cn".equals(qName)) {
 //				cn = zone.getCol(
 //							Integer.valueOf(attrs.getValue("x")),
 //							Integer.valueOf(attrs.getValue("y"))
 //						);
 			
 			} else if ("mapping".equals(qName)) {
 				mappings.add(
 					new MappingHebbian(
 						prevZone,
 						Integer.valueOf(attrs.getValue("number-of-synaptic-links")),
 						Double.valueOf(attrs.getValue("synaptic-links-dispersion")),
 						Boolean.valueOf(attrs.getValue("soft")),
 						Boolean.valueOf(attrs.getValue("haveInhibitory"))
 					)
 				);
 				
 			} else if ("zone".equals(qName)) {
 				if ("complex".equals(attrs.getValue("type"))) {
 					LayerWLearning cZone;
 					zone = cZone = new LayerWLearning();
 
 					cZone.count = Integer.valueOf(attrs.getValue("count"));
 					
 				} else {
 					zone = new  LayerSimple();
 				}
 				zone.id = attrs.getValue("id");
 				zone.name = attrs.getValue("name");
 				zone.width = Integer.valueOf(attrs.getValue("width"));
 				zone.height = Integer.valueOf(attrs.getValue("height"));
 				zone.active = Boolean.valueOf(attrs.getValue("active"));
 				zone.isLearning = Boolean.valueOf(attrs.getValue("learning"));
 				
 				zone.init();
 
 				if (prevZone != null) {
 					fX = prevZone.width() / (double) zone.width();
 					fY = prevZone.height() / (double) zone.height();
 				}
 			}
 		}
 
 		@Override
 		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
 			if ("zone".equals(qName)) {
 				prevZone = zone;
 				zones.add(zone);
 				
 				if (zone instanceof LayerWLearning) {
 					((LayerWLearning) zone).in_zones = 
 						mappings.toArray(new MappingHebbian[mappings.size()]);
 				}
 				mappings.clear();
 				
 				zone = null;
 			}
 		}
 		
 	    public void endDocument() throws SAXException {
 	    	mc = new MultiCortex(app, zones.toArray(new LayerSimple[zones.size()]));
 	    }
 	}
 }
