 package de.krkm.trex.inference.concept;
 
 import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
 import de.krkm.trex.booleanexpressions.OrExpression;
 import de.krkm.trex.inference.InferenceStepProvider;
 import de.krkm.trex.inference.Matrix;
 import de.krkm.trex.reasoner.TRexReasoner;
 import de.krkm.trex.util.Util;
 import org.semanticweb.owlapi.model.*;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import static de.krkm.trex.booleanexpressions.ExpressionMinimizer.*;
 
 public class ConceptDisjointnessInferenceStepProvider extends InferenceStepProvider {
     private TRexReasoner reasoner;
     private OWLDataFactory factory;
     private Matrix matrix;
 
     @Override
     public void initMatrix(OWLOntology ontology, TRexReasoner reasoner, Matrix matrix) {
         this.reasoner = reasoner;
         this.matrix = matrix;
         this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
         int dimension = matrix.getNamingManager().getNumberOfConcepts();
         matrix.setMatrix(new boolean[dimension][dimension]);
         matrix.setExplanations(new OrExpression[dimension][dimension]);
 
         Set<OWLDisjointClassesAxiom> disjointClassesAxiomSet = ontology.getAxioms(AxiomType.DISJOINT_CLASSES);
         for (OWLDisjointClassesAxiom a : disjointClassesAxiomSet) {
             for (OWLDisjointClassesAxiom p : a.asPairwiseAxioms()) {
                 Set<OWLClass> disjointClassesSet = a.getClassesInSignature();
                 OWLClass[] disjointClasses = disjointClassesSet.toArray(new OWLClass[disjointClassesSet.size()]);
                 for (int i = 0; i < disjointClasses.length; i++) {
                     for (int j = 0; j < i; j++) {
                        if (i == j) {
                            continue;
                        }
                         if (!disjointClasses[i].isAnonymous() && !disjointClasses[j].isAnonymous()) {
                             String iriI = Util.getFragment(disjointClasses[i].asOWLClass().getIRI().toString());
                             String iriJ = Util.getFragment(disjointClasses[j].asOWLClass().getIRI().toString());
                             matrix.set(iriI, iriJ, true);
                             int idI = matrix.getNamingManager().getConceptId(iriI);
                             int idJ = matrix.getNamingManager().getConceptId(iriJ);
                             matrix.set(iriI, iriJ, true);
                             matrix.addExplanation(idI, idJ, or(and(literal(p.getAxiomWithoutAnnotations()))));
                         }
                     }
                 }
             }
 
 
         }
     }
 
     @Override
     public boolean infer(Matrix matrix, int row, int col) {
         boolean mod = false;
         for (int i = 0; i < matrix.dimensionRow; i++) {
             if (reasoner.conceptSubsumption.matrix[row][i] && matrix.get(col, i)) {
                 matrix.set(row, col, true);
                 mod = matrix.addExplanation(row, col,
                         ExpressionMinimizer.flatten(reasoner.getConceptSubsumption().getExplanation(row, i),
                                 matrix.getExplanation(i, col))) || mod;
             }
         }
         return mod;
     }
 
     @Override
     public String getAxiomRepresentation(Matrix matrix, int row, int col) {
         if (matrix.get(row, col)) {
             return String.format("DisjointWith(%s, %s)", matrix.getNamingManager().getConceptIRI(row),
                     matrix.getNamingManager().getConceptIRI(col));
         }
         return null;
     }
 
     @Override
     public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
         if (matrix.get(row, col)) {
             HashSet<OWLClass> concepts = new HashSet<OWLClass>();
             concepts.add(factory.getOWLClass(IRI.create(getIRIWithNamespace(
                     matrix.getNamingManager().getConceptIRI(row)))));
             concepts.add(factory.getOWLClass(IRI.create(getIRIWithNamespace(
                     matrix.getNamingManager().getConceptIRI(col)))));
             return factory.getOWLDisjointClassesAxiom(concepts);
         }
         return null;
     }
 
     @Override
     public String getIdentifier() {
         return "DisjointWith";
     }
 
     @Override
     public boolean isSymmetric() {
         return true;
     }
 
     @Override
     public int resolveRowIRI(String iri) {
         return reasoner.getNamingManager().getConceptId(iri);
     }
 
     @Override
     public String resolveRowID(int id) {
         return reasoner.getNamingManager().getConceptIRI(id);
     }
 
     @Override
     public AxiomType getAxiomType() {
         return AxiomType.DISJOINT_CLASSES;
     }
 
     @Override
     public boolean isEntailed(OWLAxiom axiom) {
         isProcessable(axiom);
 
         boolean res = false;
         Set<OWLClass> disjointClassesSet = axiom.getClassesInSignature();
         OWLClass[] disjointClasses = disjointClassesSet.toArray(new OWLClass[disjointClassesSet.size()]);
         for (int i = 0; i < disjointClasses.length; i++) {
             for (int j = 0; j < i; j++) {
                if (i == j) {
                    continue;
                }
                 String iriI = Util.getFragment(disjointClasses[i].asOWLClass().getIRI().toString());
                 String iriJ = Util.getFragment(disjointClasses[j].asOWLClass().getIRI().toString());
                 res = matrix.get(iriI, iriJ) || res;
 
                 // if entailed: stop processing
                 if (res) {
                     return true;
                 }
             }
         }
 
         return res;
     }
 
     @Override
     public OrExpression getExplanation(OWLAxiom axiom) {
         isProcessable(axiom);
 
         OrExpression overall = null;
         Set<OWLClass> disjointClassesSet = axiom.getClassesInSignature();
         OWLClass[] disjointClasses = disjointClassesSet.toArray(new OWLClass[disjointClassesSet.size()]);
 
         if (disjointClasses.length == 1) {
             int id = resolveRowIRI(Util.getFragment(disjointClasses[0].asOWLClass().getIRI().toString()));
             if (matrix.get(id, id)) {
                 overall = matrix.getExplanation(id, id);
 
                 if (overall != null) {
                     ExpressionMinimizer.minimize(overall);
                 }
 
             }
 
             return overall;
         }
 
         for (int i = 0; i < disjointClasses.length; i++) {
             for (int j = 0; j < i; j++) {
                 int idI = resolveRowIRI(Util.getFragment(disjointClasses[i].asOWLClass().getIRI().toString()));
                 int idJ = resolveColIRI(Util.getFragment(disjointClasses[j].asOWLClass().getIRI().toString()));
                 if (matrix.get(idI, idJ)) {
                     if (overall == null) {
                         overall = matrix.getExplanation(idI, idJ);
                     } else {
                         System.out.println("Adding to overall: " + overall.toString());
                         overall = ExpressionMinimizer.flatten(overall,
                                 matrix.getExplanation(idI, idJ));
                         System.out.println("Adding to overall: " + overall.toString());
                     }
                 }
             }
         }
 
         if (overall != null) {
             ExpressionMinimizer.minimize(overall);
         }
         return overall;
     }
 
     @Override
     public void addAxiom(OWLAxiom axiom) {
         isProcessable(axiom);
 
         Set<OWLClass> disjointClassesSet = axiom.getClassesInSignature();
         OWLClass[] disjointClasses = disjointClassesSet.toArray(new OWLClass[disjointClassesSet.size()]);
         for (int i = 0; i < disjointClasses.length; i++) {
             for (int j = 0; j < i; j++) {
                if (i == j) {
                    continue;
                }
                 String iriI = Util.getFragment(disjointClasses[i].asOWLClass().getIRI().toString());
                 String iriJ = Util.getFragment(disjointClasses[j].asOWLClass().getIRI().toString());
                 matrix.set(iriI, iriJ, true);
                 int indexA = resolveRowIRI(iriI);
                 int indexB = resolveColIRI(iriJ);
                 matrix.addExplanation(indexA, indexB, or(and(literal(axiom))));
             }
         }
     }
 }
