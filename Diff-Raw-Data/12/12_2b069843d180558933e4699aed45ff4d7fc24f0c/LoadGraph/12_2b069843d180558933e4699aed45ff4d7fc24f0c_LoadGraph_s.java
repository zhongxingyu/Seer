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
 package org.swows.graph;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.swows.reader.RDFDataMgr;
 import org.swows.reader.ReaderFactory;
import org.swows.reader.XmlReader;
 import org.swows.runnable.LocalTimer;
 import org.swows.runnable.RunnableContextFactory;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.query.Syntax;
 import com.hp.hpl.jena.sparql.graph.GraphFactory;
 import com.hp.hpl.jena.util.FileUtils;
 
 public class LoadGraph extends DynamicChangingGraph {
 	
 	static {
 		ReaderFactory.initialize();
 	}
 	
 	private String filenameOrURI, baseURI, rdfSyntax;
 //	private long pollingPeriod;
 	
 	public static String guessLang(String filenameOrURI) {
 		
 		// TODO: probeContentType?
 		
 		String ext = FileUtils.getFilenameExt(filenameOrURI);
         if (ext.equals( "sparql" )) return Syntax.syntaxSPARQL.getSymbol();
         if (ext.equals( "xml" ) || ext.equals( "svg" ) || ext.equals( "html" ))
        	return XmlReader.XML_SYNTAX_URI;
 		return FileUtils.guessLang(filenameOrURI);
 	}
 
 	public LoadGraph(String filenameOrURI, String baseURI, String rdfSyntax, final long pollingPeriod) {
 		this.filenameOrURI = filenameOrURI;
 		this.baseURI = baseURI;
 		this.rdfSyntax = rdfSyntax != null ? rdfSyntax : guessLang(filenameOrURI);
 //		this.pollingPeriod = pollingPeriod;
 		baseGraph = GraphFactory.createGraphMem();
 		RDFDataMgr.read(baseGraph,filenameOrURI,baseURI,null);
 		if (pollingPeriod > 0) {
 //			final RunnableContext runnableCtxt = RunnableContextFactory.getDefaultRunnableContext();
 //			Timer updateTimer = new Timer();
 			Timer updateTimer = LocalTimer.get();
 //			(new Thread() {
 //				@Override
 //				public void run() {
 //					try {
 //						while(true) {
 //							sleep(pollingPeriod);
 //							runnableCtxt.run(new Runnable() {
 //								@Override
 //								public void run() {
 //									update();
 //								}
 //							});
 //							Thread.yield();
 //						}
 //					} catch(InterruptedException e) {
 //						throw new RuntimeException(e);
 //					}
 //				}
 //			}).run();
 			updateTimer.schedule( new TimerTask() {
 				@Override
 				public void run() {
 					RunnableContextFactory.getDefaultRunnableContext().run(new Runnable() {
 						@Override
 						public void run() {
 							update();
 						}
 					});
 				}
 			}, pollingPeriod, pollingPeriod);
 		}
 	}
 	
 	private void update() {
 		Graph newGraph = GraphFactory.createGraphMem();
 		RDFDataMgr.read(newGraph,filenameOrURI,baseURI,null);
 		setBaseGraph( newGraph );
 		// TODO: not using rdfSyntax!
 	}
 
 }
