 package org.coode.dlquery;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.protege.editor.owl.model.OWLModelManager;
 import org.protege.editor.owl.model.classexpression.OWLClassExpressionLeafNode;
 import org.protege.editor.owl.model.classexpression.OWLClassExpressionNodeDifference;
 import org.protege.editor.owl.model.classexpression.OWLClassExpressionNodePossibly;
 import org.protege.editor.owl.model.classexpression.OWLClassExpressionNodeUnion;
 import org.protege.editor.owl.model.classexpression.OWLClassExpressionNodeVisitor;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 import org.semanticweb.owlapi.model.OWLObject;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 
 
 /**
  * Author: Matthew Horridge<br>
  * The University Of Manchester<br>
  * Medical Informatics Group<br>
  * Date: 11-Oct-2006<br><br>
 *
  * matthew.horridge@cs.man.ac.uk<br>
  * www.cs.man.ac.uk/~horridgm<br><br>
  */
 public class OWLClassExpressionNodeQueryVisitor<O extends OWLObject> implements OWLClassExpressionNodeVisitor {
     private OWLModelManager owlModelManager;
 
     private OWLReasoner reasoner;
 
     private ReasonerQueryInvoker<O> queryInvoker;
 
     private Set<O> results;
 
 
     public OWLClassExpressionNodeQueryVisitor(OWLModelManager manager, OWLReasoner reasoner,
                                           ReasonerQueryInvoker<O> queryInvoker) {
         this.owlModelManager = manager;
         this.reasoner = reasoner;
         this.queryInvoker = queryInvoker;
         results = new HashSet<O>();
     }
 
 
     public void reset() {
         results.clear();
     }
 
 
     public Set<O> getResults() {
         return new HashSet<O>(results);
     }
 
 
     public void visit(OWLClassExpressionNodeDifference node) {
         node.getLeftNode().accept(this);
         Set<O> leftResults = results;
         node.getRightNode().accept(this);
         Set<O> rightResults = results;
         results = new HashSet<O>();
         results.addAll(leftResults);
         results.remove(node.getRightNode().getClassExpression());
         results.removeAll(rightResults);
     }
 
 
     public void visit(OWLClassExpressionNodeUnion node) {
         node.getLeftNode().accept(this);
         Set<O> leftResults = results;
         node.getRightNode().accept(this);
         Set<O> rightResults = results;
         results = new HashSet<O>();
         results.addAll(leftResults);
         results.addAll(rightResults);
     }
 
 
     public void visit(OWLClassExpressionNodePossibly node) {
         // LEFTDESC minus not(RIGHTDESC)
         OWLClassExpression leftDesc = node.getLeftNode().getClassExpression();
         OWLClassExpression rightDesc = node.getRightNode().getClassExpression();
         OWLClassExpression negRightDesc = owlModelManager.getOWLDataFactory().getOWLObjectComplementOf(rightDesc);
 
         Set<O> leftResults = queryInvoker.getAnswer(reasoner, leftDesc);
         Set<O> rightResults = queryInvoker.getAnswer(reasoner, negRightDesc);
         results = new HashSet<O>();
         results.addAll(leftResults);
         results.removeAll(rightResults);
     }
 
 
     public void visit(OWLClassExpressionLeafNode node) {
         results = queryInvoker.getAnswer(reasoner, node.getClassExpression());
     }
 }
