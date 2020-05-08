 package net.metadata.auselit.lorestore.servlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.http.HttpServletResponse;
 
 import net.metadata.auselit.lorestore.access.OREAccessPolicy;
 import net.metadata.auselit.lorestore.exceptions.NotFoundException;
 import net.metadata.auselit.lorestore.exceptions.OREException;
 import net.metadata.auselit.lorestore.model.CompoundObject;
 import net.metadata.auselit.lorestore.model.CompoundObjectImpl;
 import net.metadata.auselit.lorestore.triplestore.TripleStoreConnectorFactory;
 
 import org.ontoware.rdf2go.ModelFactory;
 import org.ontoware.rdf2go.RDF2Go;
 import org.ontoware.rdf2go.exception.ModelRuntimeException;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.ModelSet;
 import org.ontoware.rdf2go.model.Syntax;
 import org.ontoware.rdf2go.model.node.URI;
 import org.openrdf.rdf2go.RepositoryModelFactory;
 
 import au.edu.diasb.chico.mvc.RequestFailureException;
 
 /**
  * The OREUpdateHandler class processes updates to compound object by
  * OREController
  * 
  * Based loosely on DannoUpdateHandler
  * 
  * @author uqdayers
  */
 public class OREUpdateHandler {
 
 	protected final OREControllerConfig occ;
 	private final TripleStoreConnectorFactory cf;
 	private OREAccessPolicy ap;
 
 	public OREUpdateHandler(OREControllerConfig occ) {
 		this.occ = occ;
 		this.cf = occ.getContainerFactory();
 		this.ap = occ.getAccessPolicy();
 	}
 
 	/**
 	 * Handle POST requests; posting of new Compound Objects
 	 * 
 	 * @param request
 	 *            the servlet request
 	 * @return
 	 * @throws IOException
 	 * @throws RequestFailureException
 	 * @throws OREException
 	 * @throws InterruptedException
 	 */
 	public OREResponse post(InputStream inputRDF) throws RequestFailureException,
 			IOException, OREException, InterruptedException {
 		
		// TODO: probably should check after we've loaded the model,
		// but at this stage, the security check ignores it anyway
		ap.checkCreate(null);
		
 		RepositoryModelFactory mf = new RepositoryModelFactory();
 
 		String uid = occ.getUidGenerator().newUID();
 		URI newUri = mf.createModel().createURI(occ.getBaseUri() + uid);
 		Model model = mf.createModel(newUri);
 
 		model.open();
 		try {
 			model.readFrom(inputRDF, Syntax.RdfXml, occ.getBaseUri());
 		} catch (ModelRuntimeException e) {
 			throw new RequestFailureException(
 					HttpServletResponse.SC_BAD_REQUEST, "Error reading RDF");
 		}
 
 		CompoundObjectImpl compoundObject = new CompoundObjectImpl(model);
 		compoundObject.assignURI(occ.getBaseUri() + uid);
 
 		// TODO: needs to do stuff like maintaining the created/modified dates,
 		// and the creator
		String userURI = occ.getIdentityProvider().obtainUserURI();
		compoundObject.setUser(userURI);
 
 		ModelSet ms = null;
 		try {
 			ms = cf.retrieveConnection();
 			ms.addModel(model);
 			ms.commit();
 		} finally {
 			cf.release(ms);
 		}
 		OREResponse response = new OREResponse(model);
 		response.setLocationHeaer(occ.getBaseUri() + uid);
 		response.setReturnStatus(201);
 		return response;
 	}
 
 	public OREResponse delete(String oreId) throws NotFoundException,
 			InterruptedException, RequestFailureException {
 		ModelSet container = null;
 		Model model = null;
 		try {
 			container = cf.retrieveConnection();
 			URI objURI = container.createURI(occ.getBaseUri() + oreId);
 
 			model = container.getModel(objURI);
 			CompoundObject co = new CompoundObjectImpl(model);
 			
 			ap.checkDelete(co);
 			
 			if (!container.containsModel(objURI)) {
 				throw new NotFoundException("Cannot delete, object not found");
 			}
 			container.removeModel(objURI);
 		} finally {
 			cf.release(container);
 		}
 
 		return new OREResponse(null);
 	}
 
 	public OREResponse put(String oreId, InputStream inputRDF)
 			throws RequestFailureException, IOException, OREException,
 			InterruptedException {
 		ModelFactory mf = RDF2Go.getModelFactory();
 		URI objURI = mf.createModel().createURI(occ.getBaseUri() + oreId);
 
 		ModelSet container = null;
 		Model model = null;
 		try {
 			container = cf.retrieveConnection();
 			model = container.getModel(objURI);
 			if (model == null || model.isEmpty()) {
 				throw new OREException("Cannot update nonexistant object");
 			}
 
 			model = mf.createModel(objURI);
 			model.open();

 			try {
 				model.readFrom(inputRDF, Syntax.RdfXml, occ.getBaseUri());
 			} catch (ModelRuntimeException e) {
 				throw new RequestFailureException(
 						HttpServletResponse.SC_BAD_REQUEST, "Error reading RDF");
 			}
 
 			// TODO: needs to do stuff like maintaining the created/modified
 			// dates,
 			// and the creator
 
 			container.removeModel(objURI);
 			container.addModel(model);
 			container.commit();
 		} finally {
 			cf.release(container);
 		}
 
 		return new OREResponse(model);
 	}
 }
