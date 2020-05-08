 package it.essepuntato.earmark.core;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 
 /**
  * This class represents a concrete URI docuverse in the EARMARK model.
  * 
  * @author Silvio Peroni
  *
  */
 public class URIDocuverse extends Docuverse {
 	private String cache = null;
 	
 	/**
 	 * Create a URI docuverse.
 	 * 
 	 * @param d the document to which this item is associated.
 	 * @param u the URI of the content of the new docuverse.
 	 * @param id the identifier for the new docuverse.
 	 */
 	protected URIDocuverse(EARMARKDocument d, URI u, URI id) {
 		super(d, u, id);
 	}
 	
 	/**
 	 * This method returns the URI in which there is the content.
 	 * 
 	 * @return the URI in which there is the content of the docuverse.
 	 */
 	public URI hasContent() {
 		return (URI) super.hasContent();
 	}
 	
 	protected String getCache() throws IOException {
 		if (cache == null) {
 			URL url = ((URI) hasContent()).toURL();
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
			String inputLine;
			while ((inputLine = in.readLine()) != null) ;
 			in.close();
 			
			cache = inputLine;
 		}
 		
 		return cache;
 	}
 	
 	protected void clearCache() {
 		cache = null;
 	}
 	
 	/**
 	 * This method says the type of this docuverse.
 	 * 
 	 * @return the item Docuverse.Type.URIDocuverse;
 	 */
 	public Docuverse.Type getType() {
 		return Docuverse.Type.URIDocuverse;
 	}
 	
 	@Override
 	public URIDocuverse clone() {
 		return getEARMARKDocument().createURIDocuverse(hasContent());
 	}
 	
 	public String toString() {
 		return super.toString() + " => " + hasContent();
 	}
 }
