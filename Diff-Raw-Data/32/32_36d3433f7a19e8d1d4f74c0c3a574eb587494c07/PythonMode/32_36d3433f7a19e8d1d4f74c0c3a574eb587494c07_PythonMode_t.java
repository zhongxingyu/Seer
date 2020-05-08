 package com.martinleopold.mode.python;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 import org.python.util.PythonInterpreter;
 import processing.app.*;
 import processing.mode.java.JavaMode;
 import processing.mode.java.runner.Runner;
 
 /**
  * Mode Template for extending Java mode in Processing IDE 2.0a5 or later.
  *
  */
 public class PythonMode extends JavaMode {
     String template = "";
 
     public PythonMode(Base base, File folder) {
         super(base, folder);

 	// hack: make jython load stuff now instead of first compile
	//new PythonInterpreter();
 	PythonInterpreter.initialize(System.getProperties(), new Properties(), new String[] {});

 	// load tempate
 	try {
 	    File templateFile = new File(getContentFile("template.py").getAbsolutePath());
 	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(templateFile), "UTF-8"));
 	    int ch;
 	    while ((ch = reader.read()) != -1) {
 		template += (char)ch;
 	    }
 	    reader.close();
 	} catch (Exception e) {
 	    System.err.println(e.getMessage());
 	    template = "";
 	}
 
     }
 
     /**
      * Called by PDE
      */
     @Override
     public String getTitle() {
         return "Python";
     }
 
     @Override
     public Runner handleRun(Sketch sketch, RunnerListener listener) throws SketchException {
         //System.out.println("handleRun...");
 
         //System.out.println("building");
         PythonBuild build = new PythonBuild(sketch);
        String appletClassName = build.build(false);
         if (appletClassName != null) {
             final Runner runtime = new Runner(build, listener);
             new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     runtime.launch(false);  // this blocks until finished
                 }
             }).start();
             return runtime;
         }
         return null;
     }

     @Override
     public Editor createEditor(Base base, String path, EditorState state) {
         Editor editor = super.createEditor(base, path, state);
 	editor.setText(template);
 	editor.setSelection(0, 0);
 	return editor;
     }
 }
