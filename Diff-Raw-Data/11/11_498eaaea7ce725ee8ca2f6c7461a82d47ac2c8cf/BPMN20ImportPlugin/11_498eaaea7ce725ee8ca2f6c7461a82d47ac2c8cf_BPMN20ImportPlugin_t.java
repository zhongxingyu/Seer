 package org.processmining.plugins.bpmn.importing;
 
 
 
 import org.jdom.Document;
 
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 import org.jdom.transform.XSLTransformer;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 
 import java.io.InputStream;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
 import org.processmining.framework.plugin.PluginContext;
 import org.processmining.framework.plugin.annotations.Plugin;
 import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
 import org.processmining.plugins.xpdl.importing.XpdlImportBpmn;
 
 
 
 
 
 @Plugin(name = "Import BPMN model from BPMN 2.0 file", parameterLabels = { "Filename" }, returnLabels = { "BPMN diagram" }, returnTypes = { BPMNDiagram.class })
 @UIImportPlugin(description = "BPMN 2.0 files", extensions = { "bpmn" })
 public class BPMN20ImportPlugin extends XpdlImportBpmn {
 
 	protected FileFilter getFileFilter() {
 		return new FileNameExtensionFilter("BPMN 2.0 files", "bpmn");
 	}
 
 
 	protected BPMNDiagram importFromStream(PluginContext context, InputStream input, String filename,
 			long fileSizeInBytes) throws Exception {
 
 		File in = this.importbpmn(input);
 		if(in == null){
            context.getFutureResult(0).cancel(true);
 			return null;
 		}
 		FileInputStream newinput = new FileInputStream(in);
 
 
 		BPMNDiagram bpmn = super.importFromStream(context, newinput, filename, fileSizeInBytes);
 
 		return bpmn;
 	}
 
 	private File importbpmn(InputStream input) {
 		try {
 
 
 			System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
 			
 			InputStream path = getClass().getResourceAsStream("bpmnToXpdl2.xslt");
 			//String path ="xslt/bpmnToXpdl2.xslt";
 			XSLTransformer transformer =  new XSLTransformer(path);
 
 			SAXBuilder parser = new SAXBuilder();
 			Document doc = parser.build(input);
 
 
 			Document tras =  transformer.transform(doc);
 
 			File resultFile = File.createTempFile("temps", ".xpdl");
 			
 			//File resultFile = new File("/home/spagnolo1/tmp/temps.xpdl");
 
 			XMLOutputter out = new XMLOutputter();
 
 
 			out.output(tras, new FileOutputStream(resultFile) );
 
 			return resultFile;
 
 		}
 		catch (Exception e) {
 			e.printStackTrace( );
            
 			return null;
 		}
 	}
 
 
 }
