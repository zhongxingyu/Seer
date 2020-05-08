 package org.coode.dlquery;
 
 import java.util.Set;
 
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLObject;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 
 
 /**
  * Author: Matthew Horridge<br>
  * The University Of Manchester<br>
  * Medical Informatics Group<br>
  * Date: 11-Oct-2006<br><br>
 * <p/>
  * matthew.horridge@cs.man.ac.uk<br>
  * www.cs.man.ac.uk/~horridgm<br><br>
  */
 public interface ReasonerQueryInvoker<O extends OWLObject> {
 
     public Set<O> getAnswer(OWLReasoner reasoner, OWLClassExpression description);
 }
