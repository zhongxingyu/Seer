 /**
  * 
  */
 package com.github.podd.api.purl;
 
 import java.util.List;
 
 import org.openrdf.model.URI;
 
 import com.github.podd.api.PoddRdfProcessor;
 import com.github.podd.exception.PurlGeneratorNotHandledException;
 
 /**
  * An interface that can be subclassed to generate different types of Permanent URLs, or more
  * generally Permanent URIs.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public interface PoddPurlProcessor extends PoddRdfProcessor
 {
     /**
      * Signals to this Purl Processor that it should handle permanent URL generation for URIs that
      * start with the given prefix.
      * 
      * @param temporaryUriPrefix
      *            A string that matches against the start of a temporary URI to identify it as being
      *            relevant to this Purl Processor.
      */
     void addTemporaryUriHandler(String temporaryUriPrefix);
     
     /**
      * Decides whether the given input URI is compatible with this Purl Processor.
      * 
      * @param inputUri
      *            A temporary URI that needs to be translated into a Permanent URL.
      * @return True if this Purl Processor should be able to handle the conversion of this temporary
      *         URI to a Purl, and false if it is not known whether this will be possible.
      */
     boolean canHandle(URI inputUri);
     
     /**
      * 
      * @return A list of temporary URI handler prefixes that have been registered for this
      *         PoddPurlProcessor.
      */
     List<String> getTemporaryUriHandlers();
     
     /**
      * Handles the translation of URIs using this Purl Processor
      * 
      * @param inputUri
      *            A temporary URI that needs to be translated into a Permanent URL.
      * @return A translated URI that was generated using a mechanism which attempts to guarantee
      *         both permanence and uniqueness of the resulting URI.
     * @throws PurlProcessorNotHandledException
      *             If the URI was not able to be handled by this Purl Processor for any reason. To
      *             avoid this exception in normal circumstances, check first using canHandle(URI).
      */
     PoddPurlReference handleTranslation(URI inputUri) throws PurlGeneratorNotHandledException;
     
     /**
      * Signals to this Purl Processor that it should no longer handle permanent URL generation for
      * URIs that start with the given prefix.
      * 
      * @param temporaryUriPrefix
      *            A string that matches against the start of a temporary URI to identify it as being
      *            relevant to this Purl Processor.
      */
     void removeTemporaryUriHandler(String temporaryUriPrefix);
 }
