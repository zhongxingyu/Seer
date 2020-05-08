 package de.krkm.trex.inference.property;
 
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
 
 public class PropertyDisjointnessInferenceStepProvider extends InferenceStepProvider {
     private TRexReasoner reasoner;
     private OWLDataFactory factory;
     private Matrix matrix;
 
     @Override
     public void initMatrix(OWLOntology ontology, TRexReasoner reasoner, Matrix matrix) {
         this.matrix = matrix;
         int dimension = matrix.getNamingManager().getNumberOfProperties();
         matrix.setMatrix(new boolean[dimension][dimension]);
         matrix.setExplanations(new OrExpression[dimension][dimension]);
 
         this.reasoner = reasoner;
         this.factory = ontology.getOWLOntologyManager().getOWLDataFactory();
         Set<OWLDisjointObjectPropertiesAxiom> disjointPropertyAxiomSet = ontology.getAxioms(
                 AxiomType.DISJOINT_OBJECT_PROPERTIES);
         for (OWLDisjointObjectPropertiesAxiom a : disjointPropertyAxiomSet) {
             Set<OWLObjectProperty> disjointPropertySet = a.getObjectPropertiesInSignature();
             OWLObjectProperty[] disjointProperties = disjointPropertySet.toArray(
                     new OWLObjectProperty[disjointPropertySet.size()]);
             for (int i = 0; i < disjointProperties.length; i++) {
                 for (int j = 0; j < disjointProperties.length; j++) {
                     if (i == j) {
                         continue;
                     }
                     if (!disjointProperties[i].isAnonymous() && !disjointProperties[j].isAnonymous()) {
                         String iriI = Util.getFragment(disjointProperties[i].asOWLObjectProperty().getIRI().toString());
                         String iriJ = Util.getFragment(disjointProperties[j].asOWLObjectProperty().getIRI().toString());
                         matrix.set(iriI, iriJ, true);
                         int idI = matrix.getNamingManager().getPropertyId(iriI);
                         int idJ = matrix.getNamingManager().getPropertyId(iriJ);
                         matrix.set(iriI, iriJ, true);
                         matrix.addExplanation(idI, idJ,
                                 or(and(literal(a.getAxiomWithoutAnnotations()))));
                     }
                 }
             }
         }
     }
 
     @Override
     public boolean infer(Matrix matrix, int row, int col) {
         boolean mod = false;
         for (int i = 0; i < reasoner.getPropertySubsumption().dimensionRow; i++) {
             if (reasoner.propertySubsumption.matrix[row][i] && matrix.get(i, col)) {
                 matrix.set(row, col, true);
                 mod = matrix.addExplanation(row, col,
                         ExpressionMinimizer.flatten(reasoner.getPropertySubsumption().getExplanation(row, i),
                                 matrix.getExplanation(i, col))) || mod;
             }
         }
         return mod;
     }
 
     @Override
     public String getAxiomRepresentation(Matrix matrix, int row, int col) {
         if (matrix.get(row, col)) {
             return String.format("DisjointObjectProperty(%s, %s)", matrix.getNamingManager().getConceptIRI(row),
                     matrix.getNamingManager().getConceptIRI(col));
         }
         return null;
     }
 
     public OWLAxiom getAxiom(Matrix matrix, int row, int col) {
         if (matrix.get(row, col)) {
             HashSet<OWLObjectProperty> props = new HashSet<OWLObjectProperty>();
             props.add(factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(
                     matrix.getNamingManager().getPropertyIRI(row)))));
             props.add(factory.getOWLObjectProperty(IRI.create(getIRIWithNamespace(
                     matrix.getNamingManager().getPropertyIRI(col)))));
             return factory.getOWLDisjointObjectPropertiesAxiom(props);
         }
         return null;
     }
 
     @Override
     public String getIdentifier() {
         return "DisjointObjectProperty";
     }
 
     @Override
     public boolean isSymmetric() {
         return true;
     }
 
 
     @Override
     public int resolveRowIRI(String iri) {
         return reasoner.getNamingManager().getPropertyId(iri);
     }
 
     @Override
     public String resolveRowID(int id) {
         return reasoner.getNamingManager().getPropertyIRI(id);
     }
 
     @Override
     public AxiomType getAxiomType() {
         return AxiomType.DISJOINT_OBJECT_PROPERTIES;
     }
 
     @Override
     public boolean isEntailed(OWLAxiom axiom) {
         isProcessable(axiom);
 
         boolean res = false;
         Set<OWLObjectProperty> disjointPropertySet = axiom.getObjectPropertiesInSignature();
         OWLObjectProperty[] disjointClasses = disjointPropertySet.toArray(
                 new OWLObjectProperty[disjointPropertySet.size()]);
         for (int i = 0; i < disjointClasses.length; i++) {
             for (int j = 0; j < i; j++) {
                 String iriI = Util.getFragment(disjointClasses[i].asOWLObjectProperty().getIRI().toString());
                 String iriJ = Util.getFragment(disjointClasses[j].asOWLObjectProperty().getIRI().toString());
                 res = matrix.get(iriI, iriJ) || res;
 
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
         Set<OWLObjectProperty> disjointPropertySet = axiom.getObjectPropertiesInSignature();
         OWLObjectProperty[] disjointProperties = disjointPropertySet.toArray(
                 new OWLObjectProperty[disjointPropertySet.size()]);
         if (disjointProperties.length == 1) {
             int id = resolveRowIRI(Util.getFragment(disjointProperties[0].asOWLObjectProperty().getIRI().toString()));
             if (matrix.get(id, id)) {
                 overall = matrix.getExplanation(id, id);
 
                 if (overall != null) {
                     ExpressionMinimizer.minimize(overall);
                 }
 
             }
 
             return overall;
         }
 
         for (int i = 0; i < disjointProperties.length; i++) {
             for (int j = 0; j < i; j++) {
                 int idI = resolveRowIRI(Util.getFragment(disjointProperties[i].asOWLObjectProperty().getIRI().toString()));
                 int idJ = resolveColIRI(Util.getFragment(disjointProperties[j].asOWLObjectProperty().getIRI().toString()));
                 if (matrix.get(idI, idJ)) {
                     if (overall == null) {
                         overall = matrix.getExplanation(idI, idJ);
                     }
                     else {
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
 
         Set<OWLObjectProperty> disjointPropertySet = axiom.getObjectPropertiesInSignature();
         OWLObjectProperty[] disjointClasses = disjointPropertySet.toArray(
                 new OWLObjectProperty[disjointPropertySet.size()]);
         for (int i = 0; i < disjointClasses.length; i++) {
             for (int j = 0; j < i; j++) {
                 String iriI = Util.getFragment(disjointClasses[i].asOWLObjectProperty().getIRI().toString());
                 String iriJ = Util.getFragment(disjointClasses[j].asOWLObjectProperty().getIRI().toString());
                 matrix.set(iriI, iriJ, true);
                 int indexA = resolveRowIRI(iriI);
                 int indexB = resolveColIRI(iriJ);
                 matrix.addExplanation(indexA, indexB, or(and(literal(axiom))));
             }
         }
     }
 }
