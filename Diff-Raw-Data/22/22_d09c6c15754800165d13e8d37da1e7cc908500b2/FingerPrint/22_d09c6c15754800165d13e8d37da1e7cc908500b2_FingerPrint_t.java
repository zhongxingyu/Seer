 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.performance.graph;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.test.internal.performance.PerformanceTestPlugin;
 import org.eclipse.test.internal.performance.data.Dim;
 import org.eclipse.test.internal.performance.db.DB;
 import org.eclipse.test.internal.performance.db.Scenario;
 import org.eclipse.test.internal.performance.db.SummaryEntry;
 import org.eclipse.test.internal.performance.db.TimeSeries;
 import org.eclipse.test.internal.performance.db.Variations;
 
 
 public class FingerPrint {
     
     private static final int GRAPH_WIDTH= 800;
 
     String outputDirectory;
     String referenceBuildId;
     String thisBuildID;
     String config;
     String jvm;
     SummaryEntry [] entries;
     ArrayList prefixEntries;
     Variations variations;
     String linkUrl;
     
     public FingerPrint() {
     }
 
     public FingerPrint(String config, String jvm, String reference, String thisBuildId, String outputDir, String linkUrl) {
         this();
         referenceBuildId= reference;
         this.config= config;
         this.jvm=jvm;
         this.thisBuildID= thisBuildId;
         outputDirectory= outputDir;
         this.linkUrl=linkUrl;
         variations= new Variations();
         variations.put(PerformanceTestPlugin.CONFIG, config);
         variations.put(PerformanceTestPlugin.BUILD, thisBuildID);
         variations.put("jvm", jvm);
 
         // only return summaries for "org.eclipse.jdt.text"; pass a null for all global scenarios
         entries= DB.querySummaries(variations,null);
        	run(entries,"");
        	getComponentEntries(entries);
 
     }
     
     private void getComponentEntries(SummaryEntry[] entries){
     	if (entries==null)
     		return;
     	for (int i=0;i<entries.length;i++){
     		SummaryEntry entry=entries[i];
     		if (entry==null)
     			return;
     		ArrayList componentEntries;
     		String prefix=entry.scenarioName;
     		if (entry.scenarioName.indexOf(".test")!=-1){
     			prefix=entry.scenarioName.substring(0,entry.scenarioName.indexOf(".test"));
     		}
     		if (prefixEntries==null)
     			prefixEntries=new ArrayList();
     		if (!prefixEntries.contains(prefix)){
     	       	run(DB.querySummaries(variations,prefix+'%'),prefix);
     		}
     	}
     }
     
     public static void main(String args[]) {
         FingerPrint main= new FingerPrint(args[0],args[1],args[2],args[3],args[4],args[5]);  
     }
 
     public void run(Object[] entries, String component) {
     	String fileId="";
     	if (!component.equals(""))
     		fileId=component+"_";
     	
         new File(outputDirectory).mkdirs();
         String title="Performance of " + component +" "+thisBuildID + " relative to " + referenceBuildId;
         BarGraph bar= new BarGraph(null);
                 
         if (entries != null) {
             for (int i= 0; i < entries.length; i++) {
                 SummaryEntry se= (SummaryEntry)entries[i];
                 add(bar, se.shortName, new Dim[] { se.dimension }, se.scenarioName);    
             }
         }
       
         String outName= "FP_" + fileId+ referenceBuildId + '_' + thisBuildID;
         save(bar, outputDirectory + '/' + outName);
         //show(bar);
         
         String areas= bar.getAreas();
         if (areas != null) {
 	        try {
 	            PrintStream os= new PrintStream(new FileOutputStream(outputDirectory + '/' + outName + ".php"));
 	   	        os.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
 	   	        os.println("<link rel=\"stylesheet\" href=\"../../../../default_style.css\" type=\"text/css\">");
 	            os.println("<body>");
 	            os.println("<h3>"+title+"</h3>");
 	            os.println("<img src=\"" + outName + ".jpeg\" usemap=\"#" + outName + "\">");
 	            os.println("<map name=\"" + outName + "\">");
 	            os.println(areas);
 	            os.println("</map><br>");
 
 	            if (component!=""){
 	            char buildType=thisBuildID.charAt(0);
 	            os.println("<?php");
 	            os.println("	$config=trim($QUERY_STRING);");
 	            os.println("	$packageprefix=\""+component+"\";");
 
 	            os.println("	$aDirectory=dir(\"../../../../performance/$config\");");
 	            os.println("	$index = 0;");
 	            	
 	            os.println("	while ($anEntry = $aDirectory->read()) {");
 
 	            os.println("		if ($anEntry != \".\" && $anEntry != \"..\") {");
 	            os.println("			if (strstr($anEntry,$packageprefix) && strstr($anEntry,\".html\")){");
 	            os.println("				$scenarioname=substr($anEntry,0,-5);");
 	            os.println("				$scenarios[$index]=$scenarioname;");
 	            os.println("				$index++;");
 	            				
 	            os.println("			}");
 	            os.println("		}");
 	            os.println("}");
 
 	            os.println("	$scenarioCount=count($scenarios);");
 	            os.println("	if ($scenarioCount==0){");
 	            os.println("		echo \"Results being generated.\";");
 	            os.println("	}");
 	            os.println("	else{");
 	            os.println("	sort($scenarios);");
 	            os.println("	echo \"<h3>All $scenarioCount scenarios</h3>\"; ");
 
 	            os.println("	for ($counter=0;$counter<count($scenarios);$counter++){");
 	            os.println("		$line = \"<a href=\\\"../../../../performance/$config/$scenarios[$counter].html\\\">$scenarios[$counter]</a><br>\";");
 	            os.println("	 	echo \"$line\";");
 	            os.println("	}");
 	            os.println("	}");
 	            os.println("	aDirectory.closedir();");
 	            os.println("?>");
 	            }        
 	            
 	            os.println("</body></html>");
 	            os.close();
 	        } catch (FileNotFoundException e) {
 	            // TODO Auto-generated catch block
 	            e.printStackTrace();
 	        }
         }
     }
     
     private void add(BarGraph bar, String name, Dim[] dims, String scenarioName) {
         
         String refData= "";
         Variations v= new Variations();
         v.put(PerformanceTestPlugin.CONFIG, config);
        v.put("jvm", jvm);
         Scenario scenario= DB.getScenarioSeries(scenarioName, v, PerformanceTestPlugin.BUILD, referenceBuildId, thisBuildID, dims);
         String[] timeSeriesLabels= scenario.getTimeSeriesLabels();
         if (timeSeriesLabels.length == 2) {
             // we mark the label with a '*' or '' to indicate that no data was available for the specified builds
             if (!timeSeriesLabels[0].equals(referenceBuildId)) {
                 name= '*' + name;
                 refData= " (" + timeSeriesLabels[0] + ")";
             } else if (!timeSeriesLabels[1].equals(thisBuildID)) {
                 name= '' + name;
                 refData= " (" + timeSeriesLabels[1] + ")";
             }
         }
         
         for (int i= 0; i < dims.length; i++) {
             TimeSeries timeSeries= scenario.getTimeSeries(dims[i]);
 	        int l= timeSeries.getLength();
 	        if (l >= 1) {
 	            double percent= 0.0;
 	            if (l > 1) {
 	                double ref= timeSeries.getValue(0);
 	                double val= timeSeries.getValue(1);
 	            	percent= 100.0 - ((val / ref) * 100.0);
 	            }
 	            if (Math.abs(percent) < 200) {
 	                String n= name + " (" + dims[i].getName() + ")" + refData;
 	                
 	                bar.addItem(n, percent,linkUrl+"/"+(scenarioName+"_"+dims[i].getName()).replace('#','.').replace(':','_').replace('\\','_')+".html"); //$NON-NLS-1$ //$NON-NLS-2$
 	            }
 	        }
         }
         //scenario.dump(System.out);
 	         
     }
 
     private void save(BarGraph bar, String output) {
 
         Display display= Display.getDefault();
         
         int height= bar.getHeight();
         Image image= new Image(display, GRAPH_WIDTH, height);
         
         GC gc= new GC(image);
         bar.paint(display, GRAPH_WIDTH, height, gc);
         gc.dispose();
 
         ImageLoader il= new ImageLoader();
         il.data= new ImageData[] { image.getImageData()};
 
         OutputStream out= null;
         try {
             out= new BufferedOutputStream(new FileOutputStream(output + ".jpeg")); //$NON-NLS-1$
             //System.out.println("writing: " + output);
             il.save(out, SWT.IMAGE_JPEG);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } finally {
             image.dispose();
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e1) {
                     // silently ignored
                 }
             }
         }
     }
     
     /*
      * Displays bar graph in window
      */
     private void show(final BarGraph bar) {
         Display display= new Display();
         
         Shell shell= new Shell(display);
         shell.setLayout(new FillLayout());
         
         final Canvas canvas= new Canvas(shell, SWT.NO_BACKGROUND);
         canvas.addPaintListener(new PaintListener() {
             public void paintControl(PaintEvent e) {
                 Point s= canvas.getSize();
                 bar.paint(canvas.getDisplay(), s.x, s.y, e.gc);
             }
         });
         
         shell.open();
 
         while (!shell.isDisposed())
             if (!display.readAndDispatch())
                 display.sleep();
             
         display.dispose();
     }
 }
