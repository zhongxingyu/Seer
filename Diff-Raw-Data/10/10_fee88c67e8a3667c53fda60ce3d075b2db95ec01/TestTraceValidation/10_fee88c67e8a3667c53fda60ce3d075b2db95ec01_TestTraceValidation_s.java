 package org.lttng.flightbox.junit.xml;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.eclipse.linuxtools.lttng.jni.exception.JniException;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.jdom.xpath.XPath;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.lttng.flightbox.io.TraceReader;
 import org.lttng.flightbox.xml.ManifestReader;
 import org.lttng.flightbox.xml.MarkerSetOperations;
 import org.lttng.flightbox.xml.TraceEventHandlerInventory;
 
 public class TestTraceValidation {
 
 	static String trace_dir;
 	static public String trace_dir_var = "TRACE_DIR";
 	public static String manifestPath = "/tests/manifest/";
 	public static String dtdPath = "/manifest/";
 	
 	@BeforeClass
 	public static void setUp() {
 		trace_dir = System.getenv(trace_dir_var);
 		if (trace_dir == null) {
 			throw new RuntimeException("TRACE_DIR not set");
 		}
 	}
 	
 	@Test
 	public void testTraceValidateLinuxPass() throws JniException, JDOMException {
 		TraceReader reader = new TraceReader(trace_dir + "sleep-1x-1sec");
 		TraceEventHandlerInventory handler = new TraceEventHandlerInventory();
 		reader.register(handler);
 		reader.process();
 		Document inventory = handler.getInventory();
 		XPath xpath = XPath.newInstance("//channel");
 		List<Element> channels = (List<Element>) xpath.selectNodes(inventory);
 		
 		// ltt-armall enable 22 channels by default
		assertEquals(channels.size(), 22);
 		
 		/*
 		XMLOutputter out = new XMLOutputter();
 		out.setFormat(Format.getPrettyFormat());
 		try {
 			out.output(inventory, System.out);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		*/
 		
 	}
 
 	@Test
 	public void testReadAndValidatePassSleep1() throws JDOMException, IOException, JniException {
 		String filePass = System.getenv("project_loc") + manifestPath + "linux_pass_metadata.xml";
 		String fileFail1 = System.getenv("project_loc") + manifestPath + "linux_fail_metadata_unknown_channel.xml";
 		String fileFail2 = System.getenv("project_loc") + manifestPath + "linux_fail_metadata_unknown_event.xml";
 		String fileFail3 = System.getenv("project_loc") + manifestPath + "linux_fail_metadata_unknown_field.xml";
 		String path = System.getenv("project_loc") + dtdPath;
 		ManifestReader manifestReader = new ManifestReader();
 		Document manifestPass = manifestReader.read(filePass, path);
 		Document manifestFail1 = manifestReader.read(fileFail1, path);
 		Document manifestFail2 = manifestReader.read(fileFail2, path);
 		Document manifestFail3 = manifestReader.read(fileFail3, path);
 		
 		TraceReader reader = new TraceReader(trace_dir + "sleep-1x-1sec");
 		TraceEventHandlerInventory handler = new TraceEventHandlerInventory();
 		reader.register(handler);
 		reader.process();
 		
 		Document inventory = handler.getInventory();
 		
 		List<Element> missing;
 		missing = MarkerSetOperations.containsAll(inventory, manifestPass);
 		assertEquals(missing.size(), 0);
 		missing = MarkerSetOperations.containsAll(inventory, manifestFail1);
 		assertEquals(missing.size(), 1);
 		missing = MarkerSetOperations.containsAll(inventory, manifestFail2);
 		assertEquals(missing.size(), 1);
 		missing = MarkerSetOperations.containsAll(inventory, manifestFail3);
 		assertEquals(missing.size(), 1);
 	}
 	
 }
