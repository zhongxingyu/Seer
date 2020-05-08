 package com.theatlantic.autograph.py4j;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 
 import org.mozilla.javascript.RhinoException;
 
 import com.theatlantic.highcharts.export.ExportType;
 import com.theatlantic.highcharts.export.HighchartsExporter;
 
 
 import py4j.Py4JJavaException;
 
 /**
  * Exports SVG or PNG from a json input of Highcharts options.
  *
  * @author <a href="https://github.com/fdintino">Frankie Dintino</a>
  * @version $Id$
  */
 public class HighchartsExport {
     
     HighchartsExporter svgFromJsonExporter;
     SVGRasterizer rasterizer;
     
     public HighchartsExport() {
         svgFromJsonExporter = ExportType.svg.createJsonExporter();
         rasterizer = new SVGRasterizer();
     }
     
     private String readFileAsString(String filePath) throws Py4JJavaException {
         byte[] buffer = new byte[(int) new File(filePath).length()];
         BufferedInputStream f = null;
         try {
             f = new BufferedInputStream(new FileInputStream(filePath));
             f.read(buffer);
         } catch (FileNotFoundException e) {
         	throw new Py4JJavaException(
 					String.format("Could not find file '%s'", filePath), e);
         } catch (IOException e) {
         	throw new Py4JJavaException(
 					String.format("Error reading file '%s'", filePath), e);
         } finally {
             if (f != null) {
             	try { f.close(); } catch (IOException ignored) { }
             }
         }
         return new String(buffer);
     }
     
     /**
      * Lets you specify the raster image width.
      *
      * @param w the desired raster image width.
      */
 	public void set_width(Integer w) {
 		rasterizer.set_width(w);
 	}
 	
 	/**
      * Lets you specify the raster image width.
      *
      * @param w the desired raster image width.
      */
 	public void set_width(Double w) {
 		rasterizer.set_width(w);
 	}
 	
 	/**
      * Lets you specify the raster image height.
      *
      * @param h the desired raster image height.
      */
 	public void set_height(Integer h) {
 		rasterizer.set_height(h);
 	}
 	
 	/**
      * Lets you specify the raster image height.
      *
      * @param h the desired raster image height.
      */
 	public void set_height(Double h) {
 		rasterizer.set_height(h);
 	}
 
     public String export_to_svg(String jsonOptions) throws Py4JJavaException {
         ByteArrayOutputStream svgOutput = new ByteArrayOutputStream();
         try {
             try {
                 svgFromJsonExporter.export(jsonOptions, null, svgOutput);
     	    } catch (RhinoException re) {
     	        re.printStackTrace();
     	        throw new ExtendedScriptException(re);
     		}
         } catch (ExtendedScriptException e) {
             throw new Py4JJavaException(e.getMessage(), e);
         }
        return svgOutput.toString();
     }
     
     public String export_file_to_svg(String jsonFile) throws Py4JJavaException {
     	String jsonOptions = readFileAsString(jsonFile);
     	return export_to_svg(jsonOptions);
     }
     
     public void export_to_svg(String jsonOptions, String destFile) throws Py4JJavaException {
         FileOutputStream ostream;
 		try {
 			ostream = new FileOutputStream(new File(destFile));
 		} catch (FileNotFoundException e) {
 			throw new Py4JJavaException(
 					String.format("Could not create file '%s' as output source", destFile), e);
 		}
 		try {
     		try {
     		    svgFromJsonExporter.export(jsonOptions, null, ostream);
     	    } catch (RhinoException re) {
     	        re.printStackTrace();
     	        throw new ExtendedScriptException(re);
     		}
         } catch (ExtendedScriptException e) {
             throw new Py4JJavaException(e.getMessage(), e);
         }
     }
     
     public void export_file_to_svg(String jsonFile, String destFile) throws Py4JJavaException {
     	String jsonOptions = readFileAsString(jsonFile);
     	export_to_svg(jsonOptions, destFile);
      }
      
     public byte[] export(String jsonOptions) throws Py4JJavaException {
         String svgString = export_to_svg(jsonOptions);
 		return rasterizer.convert(svgString);
     }
     
     public byte[] export_file(String jsonFile) throws Py4JJavaException {
     	String jsonOptions = readFileAsString(jsonFile);
     	return export(jsonOptions);
     }
     
     public void export(String jsonOptions, String destFile) throws Py4JJavaException {
         String svgString = export_to_svg(jsonOptions);
         rasterizer.convert(svgString, destFile);
     }
     
     public void export_file(String jsonFile, String destFile) throws Py4JJavaException {
     	String jsonOptions = readFileAsString(jsonFile);
     	export(jsonOptions, destFile);
     }
 }
