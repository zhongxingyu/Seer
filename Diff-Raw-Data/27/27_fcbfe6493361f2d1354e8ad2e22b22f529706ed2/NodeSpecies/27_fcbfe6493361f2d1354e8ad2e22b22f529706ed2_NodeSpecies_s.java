 package org.vamdc.portal.registry;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.core.ResourceLoader;
 import org.vamdc.portal.RedirectPage;
 
 @Name("nodespecies")
 @Scope(ScopeType.CONVERSATION)
 public class NodeSpecies {
 	// node id
     private String ivoaId = "";
     //tap request parameters
     private String query = "sync?LANG=VSS2&REQUEST=doQuery&FORMAT=XSAMS&QUERY=Select+Species";
     //contains printable results
    private String formatedResult= "";
     //true if node results are valid
     private Boolean displayable = false;
     //message describing request status
    private String message = "";
     //xsl stylesheet name
     private String xslFile = "getspecies.xsl";
     
     public NodeSpecies(){
     	
     }
      
     public void setIvoaId(String id){
     	this.ivoaId = id;
     }
     
     public String getIvoaId(){
     	return this.ivoaId;
     }
     
     public String displaySpecies(String ivoaId){
     	querySpecies(ivoaId);
     	return RedirectPage.SPECIES;
     }
     
    public String getFormatedResult(){
		return this.formatedResult; 
     }  
     
     public boolean isDisplayable(){
     	return this.displayable;
     }
  
     public String getMessage(){
     	return this.message;
     }
     
     public void querySpecies(String id){
     	setIvoaId(id);
    	RegistryFacade rf = new RegistryFacade();    	
    	URL node = rf.getVamdcTapURL(this.ivoaId);    	
     	try {
 			URL url = new URL(node.toExternalForm()+query);
			this.formatedResult = formatRequestResult(url);
 			this.message = "Request completed";
 			this.displayable = true;
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			this.message = "Incorrect service URL";
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
			this.message = "Timeout error. Informations could not be loaded";
 		} catch (TransformerException e) {
 			// TODO Auto-generated catch block
 			this.message = "XSAMS file could not be parsed.";
		}   	
     }
     
     /**
      * convert XSAMS file contained at URL into HTML
      * @param url
      * @return
      * @throws IOException
      * @throws TransformerException
      */
     private String formatRequestResult(URL url) throws IOException, TransformerException{
 		URLConnection c = url.openConnection();
 		c.setConnectTimeout(3000);
		c.setReadTimeout(5000);
 		
 		Source xmlSource = new StreamSource(new InputStreamReader(c.getInputStream()));		
 		Source xsltSource = new StreamSource(ResourceLoader.instance().getResourceAsStream(this.xslFile));
 		
 		TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer = factory.newTransformer(xsltSource);
         ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         transformer.transform(xmlSource, new StreamResult(bOut));   
         bOut.close();
         
         return bOut.toString();
     }
 }
