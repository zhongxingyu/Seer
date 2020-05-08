 package de.berlios.statcvs.xml.output;
 
 import java.awt.print.Pageable;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 
 import net.sf.statcvs.model.CvsContent;
 
 import org.jdom.transform.JDOMResult;
 import org.jdom.transform.JDOMSource;
 
 /**
  * Writes xml files to disk.
  *  
  * @author Steffen Pingel
  */
 public class XMLRenderer implements DocumentRenderer {
 
 	private File outputPath;
 
 	private static Logger logger
 		= Logger.getLogger("net.sf.statcvs.output.XMLRenderer");
 
 	private XMLOutputter out;
 	private Transformer transformer;
 	private String extension;
 	
 	public XMLRenderer(Transformer transformer, File outputPath) 
 	{
 		this.transformer = transformer;
 		this.outputPath = outputPath;
 		
 		setExtension(".xml");
 
 		XMLOutputter xout = new XMLOutputter();
		xout.setEncoding("ISO-8859-1");
 		setOutputter(xout);
 
 //		if (transformer != null) {
 //			logger.info("Using transformer " + transformer.getClass().getName());
 //		}
 	}
 
 	public XMLRenderer(File outputPath)
 	{
 		this(null, outputPath);
 	}
 
 	/**
 	 * Invoked by Main.
 	 */
 	public static DocumentRenderer create(CvsContent content, ReportSettings settings)
 			throws IOException
 	{
 		return new XMLRenderer(settings.getOutputPath());
 	}
 
 	public void setOutputter(XMLOutputter outputter) {
 		out = outputter;
 		out.setTextNormalize(true);
 		out.setIndent("  ");
 		out.setNewlines(true);
 	}
 	
 	public void setExtension(String ext) {
 		this.extension = ext;
 	}
 	
 	public String getExtension() {
 		return this.extension;
 	}
 	
 	public File getOutputPath()
 	{
 		return this.outputPath;
 	}
 	
 	public void render(StatCvsDocument document) throws IOException
 	{
 		logger.info("Rendering " + document.getFilename());
 		if (document instanceof Pageable) {
 			renderPages((Pageable)document);
 		} else {
 			renderSingle(document);
 		}
 		
 	}
 	
 	private void renderPages(Pageable document) throws IOException
 	{
 //		document.setItemsPerPage
 //			(OutputSettings.getInstance().get("itemsPerPage", 10));
 //		for (int i = 0; i < document.getPageCount(); i++) {
 //			renderSingle(document.getPage(i));					
 //		}
 	}
 
 	private void renderSingle(StatCvsDocument document) throws IOException 
 	{
 		File file = new File(outputPath, document.getFilename() + extension);
 		FileWriter writer = new FileWriter(file);
 
 		try {
 			if (transformer != null) {
 				JDOMResult result = new JDOMResult();
 				try {
 					transformer.transform(new JDOMSource(document), result);
 				}
 				catch (TransformerException e) {
 					logger.warning("XSLT transformation failed: " + e);
 				}
 				out.output(result.getDocument(), writer);
 			}
 			else {
 				out.output(document, writer);
 			}
 		}
 		finally {
 			writer.close();
 		}	
 
 		document.saveResources(outputPath);
 	}
 
 	/**
 	 * Copies the required resources.
 	 */
 	public void postRender()
 	{
 	}
 	
 }
