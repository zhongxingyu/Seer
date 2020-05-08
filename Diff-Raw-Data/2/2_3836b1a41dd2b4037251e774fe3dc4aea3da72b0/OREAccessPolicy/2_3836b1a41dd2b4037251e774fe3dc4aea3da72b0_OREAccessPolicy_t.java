 package net.metadata.auselit.lorestore.access;
 
 import net.metadata.auselit.lorestore.model.CompoundObject;
 
 import org.ontoware.rdf2go.model.Model;
 
 import au.edu.diasb.chico.mvc.RequestFailureException;
 
 /**
  * An OREAccessPolicy is exactly based upon DannoAccessPolicy, but I don't want
  * to depend on the servlet components of Danno.
  * 
  * A DannoAccessPolicy object implements fine-grained access control rules on
  * Annotea (and related) requests. A typical implementation will decide on the
  * basis of the users' identity, authorities and other attributes, together with
  * details of the target object(s).
  * 
  * @author scrawley
  */
 public interface OREAccessPolicy {
 
 	/**
 	 * Called for GET requests after fetching the object(s) from the triple
 	 * store and preparing the result container. An implementation may throw an
 	 * unchecked exception or modify the contents of the container.
 	 * 
 	 * @param res
 	 *            the result container.
 	 */
 	void checkRead(Model res) throws RequestFailureException;
 
 	/**
 	 * Called for PUT and POST requests after preparing the result container and
 	 * prior to committing. An implementation may throw an unchecked exception.
 	 * (Modifying the result container is neither necessary or advisable. It
 	 * will only change the result, not the triples committed to the triple
 	 * store.)
 	 * 
 	 * @param res
 	 *            the result container containing the triples to be returned in
 	 *            the response.
 	 * @throws RequestFailureException
 	 */
 	void checkCreate(Model res) throws RequestFailureException;
 
 	/**
 	 * Called for PUT requests to check that the requestor may update an object.
 	 * An implementation may throw an unchecked exception.
 	 * 
 	 * @param obj
 	 *            the original version of the object as fetched from the triple
 	 *            store.
 	 * @throws RequestFailureException
 	 */
 	void checkUpdate(CompoundObject obj) throws RequestFailureException;
 
 	/**
 	 * Called for DELETE requests prior to committing. An implementation may
 	 * throw an unchecked exception.
 	 * 
 	 * @param obj
 	 *            the original version of the object as fetched from the triple
 	 *            store.
 	 * @throws RequestFailureException
 	 */
 	void checkDelete(CompoundObject obj) throws RequestFailureException;
 
 	/**
 	 * Called for administrative requests to check that the request has the
 	 * rights required for a given admin request.
 	 * 
 	 * @param request
 	 *            the specific request.
 	 * @throws RequestFailureException
 	 */
 	void checkAdmin() throws RequestFailureException;
 
 }
