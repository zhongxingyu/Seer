 package org.integratedmodelling.thinklab.client.knowledge;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.integratedmodelling.exceptions.ThinklabException;
 import org.integratedmodelling.thinklab.api.knowledge.IAxiom;
 import org.integratedmodelling.thinklab.api.knowledge.IConcept;
 import org.integratedmodelling.thinklab.api.knowledge.IOntology;
 import org.integratedmodelling.thinklab.api.knowledge.IProperty;
 import org.integratedmodelling.thinklab.api.lang.IList;
 import org.integratedmodelling.thinklab.api.metadata.IMetadata;
 import org.semanticweb.owlapi.model.OWLOntology;
 
 /**
  * Simplest possible interface to an OWLApi ontology, only exposing the few things thinklab now
  * needs - basically the read-only type system embedded in the kbox.
  * 
  * @author Ferd
  *
  */
 public class Ontology implements IOntology {
 
 	String _id;
 	String _uriFragment;
 	ArrayList<IList> _axioms = new ArrayList<IList>();
 	HashMap<String, IConcept> _concepts = new HashMap<String, IConcept>();
 
 	OWLOntology _ontology;
 	
 	public Ontology(String id) {
 		this._id = id;
 		this._uriFragment = id.replace('.', '/');
 	}
 
 	@Override
 	public IOntology getOntology() {
 		return this;
 	}
 
 	@Override
 	public Collection<IConcept> getConcepts() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<IProperty> getProperties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Create any concept that isn't available
 	 */
 	@Override
 	public IConcept getConcept(String ID) {
 		IConcept ret = _concepts.get(ID);
 		if (ret == null) {
 			_concepts.put(ID, ret = new Concept(_id, ID));
 		}
 		return ret;
 	}
 
 
 	@Override
 	public IProperty getProperty(String ID) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getConceptSpace() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getURI() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public long getLastModificationDate() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void define(Collection<IAxiom> axioms) throws ThinklabException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean write(String uri) throws ThinklabException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public IMetadata getMetadata() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
