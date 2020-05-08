 package de.krkm.trex.reasoner;
 
 import de.krkm.trex.booleanexpressions.ExpressionMinimizer;
 import de.krkm.trex.booleanexpressions.OrExpression;
 import de.krkm.trex.inference.Matrix;
 import de.krkm.trex.inference.concept.ConceptDisjointnessInferenceStepProvider;
 import de.krkm.trex.inference.concept.SubClassOfInferenceStepProvider;
 import de.krkm.trex.inference.property.*;
 import org.semanticweb.owlapi.model.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Encapsulates the reasoning service
  *
  * @author Daniel Fleischhacker <daniel@informatik.uni-mannheim.de>
  */
 public class TRexReasoner {
     private OWLOntology ontology;
 
     private OntologyNamingManager namingManager;
 
     private Logger log = LoggerFactory.getLogger(TRexReasoner.class);
 
     public Matrix conceptSubsumption;
     public Matrix conceptDisjointness;
     public Matrix propertySubsumption;
     public Matrix propertyDisjointness;
     public Matrix propertyDomain;
     public Matrix propertyRange;
     public Matrix propertyUnsatisfiability;
 
     private boolean generateExplanations;
 
     private boolean conceptOnly;
 
     private HashMap<AxiomType, ArrayList<Matrix>> typeToMatrix = new HashMap<AxiomType, ArrayList<Matrix>>();
     private OWLDataFactory dataFactory;
 
     public TRexReasoner(OWLOntology ontology) {
         this(ontology, false);
     }
 
     /**
      * Initializes the reasoner to perform inference on the given ontology with explanation support enabled.
      *
      * @param ontology ontology to perform inference on
      */
     public TRexReasoner(OWLOntology ontology, boolean conceptOnly) {
         this(ontology, conceptOnly, true);
     }
 
     /**
      * Initializes the reasoner to perform inference on the given ontology.
      *
      * @param ontology             ontology to perform inference on
      * @param generateExplanations if true explanation support is enabled otherwise disabled
      */
     public TRexReasoner(OWLOntology ontology, boolean conceptOnly, boolean generateExplanations) {
         this.conceptOnly = conceptOnly;
         this.ontology = ontology;
         dataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
         namingManager = new OntologyNamingManager(ontology);
         this.generateExplanations = generateExplanations;
 
         conceptSubsumption = new Matrix(ontology, this, namingManager, new SubClassOfInferenceStepProvider());
         registerType(conceptSubsumption);
         materializeConceptSubsumption();
 
         conceptDisjointness = new Matrix(ontology, this, namingManager, new ConceptDisjointnessInferenceStepProvider(),
                 generateExplanations);
         registerType(conceptDisjointness);
         materializeConceptDisjointness();
 
         if (!conceptOnly) {
             propertySubsumption = new Matrix(ontology, this, namingManager, new SubPropertyOfInferenceStepProvider(),
                     generateExplanations);
             registerType(propertySubsumption);
             materializePropertySubsumption();
 
             propertyDisjointness = new Matrix(ontology, this, namingManager,
                     new PropertyDisjointnessInferenceStepProvider(), generateExplanations);
             registerType(propertyDisjointness);
             materializePropertyDisjointness();
 
             propertyDomain = new Matrix(ontology, this, namingManager,
                     new PropertyDomainInferenceStepProvider(), generateExplanations);
             registerType(propertyDomain);
             materializePropertyDomain();
 
             propertyRange = new Matrix(ontology, this, namingManager,
                     new PropertyRangeInferenceStepProvider(), generateExplanations);
             registerType(propertyRange);
             materializePropertyRange();
 
             propertyUnsatisfiability = new Matrix(ontology, this, namingManager,
                     new PropertyUnsatisfiabilityInferenceProvider(), generateExplanations);
             registerType(propertyUnsatisfiability);
             materializePropertyUnsatisfiability();
         }
     }
 
 
     /**
      * Re-runs the materialization step for this reasoner. In this process only new axioms are considered which do not
      * introduce new properties or concepts.
      */
     public void rematerialize() {
         for (ArrayList<Matrix> matrices : typeToMatrix.values()) {
             for (Matrix m : matrices) {
                 m.materialize();
             }
         }
     }
 
     /**
      * Registers the given matrix to the reasoner system.
      *
      * @param matrix matrix to register in reasoner
      */
     public void registerType(Matrix matrix) {
         if (!typeToMatrix.containsKey(matrix.getAxiomType())) {
             typeToMatrix.put(matrix.getAxiomType(), new ArrayList<Matrix>());
         }
         typeToMatrix.get(matrix.getAxiomType()).add(matrix);
     }
 
     /**
      * Returns the naming manager used by this reasoner
      *
      * @return naming manager of this reasoner
      */
     public OntologyNamingManager getNamingManager() {
         return namingManager;
     }
 
     /**
      * Returns the matrix used for concept subsumption
      *
      * @return matrix used for concept subsumption
      */
     public Matrix getConceptSubsumption() {
         return conceptSubsumption;
     }
 
     /**
      * Returns the matrix used for concept disjointness
      *
      * @return matrix used for concept disjointness
      */
     public Matrix getConceptDisjointness() {
         return conceptDisjointness;
     }
 
     /**
      * Returns the matrix used for property subsumption
      *
      * @return matrix used for property subsumption
      */
     public Matrix getPropertySubsumption() {
         return propertySubsumption;
     }
 
     /**
      * Returns the matrix used for property disjointness
      *
      * @return matrix used for property disjointness
      */
     public Matrix getPropertyDisjointness() {
         return propertyDisjointness;
     }
 
     /**
      * Returns the matrix used for property domains
      *
      * @return matrix used for property domains
      */
     public Matrix getPropertyDomain() {
         return propertyDomain;
     }
 
     /**
      * Returns the matrix used for property ranges
      *
      * @return matrix used for property ranges
      */
     public Matrix getPropertyRange() {
         return propertyRange;
     }
 
     /**
      * Starts materialization of derivable concept-subsumption axioms
      */
     public void materializeConceptSubsumption() {
         conceptSubsumption.materialize();
     }
 
     /**
      * Starts materializing of derivable concept disjointness axioms
      */
     public void materializeConceptDisjointness() {
         conceptDisjointness.materialize();
     }
 
     /**
      * Starts materialization of derivable property-subsumption axioms
      */
     public void materializePropertySubsumption() {
         propertySubsumption.materialize();
     }
 
     /**
      * Starts materializing of derivable property disjointness axioms
      */
     public void materializePropertyDisjointness() {
         propertyDisjointness.materialize();
     }
 
     /**
      * Starts materializing of derivable property disjointness axioms
      */
     public void materializePropertyDomain() {
         propertyDomain.materialize();
     }
 
     /**
      * Starts materializing of derivable property disjointness axioms
      */
     public void materializePropertyRange() {
         propertyRange.materialize();
     }
 
     private void materializePropertyUnsatisfiability() {
         propertyUnsatisfiability.materialize();
     }
 
     /**
      * Returns all axioms supported by this reasoner and entailed by the ontology.
      *
      * @return all axioms supported by this reasoner and entailed by the ontology
      */
     public Set<OWLAxiom> getAxioms() {
         HashSet<OWLAxiom> res = new HashSet<OWLAxiom>();
         res.addAll(conceptSubsumption.getOWLAxioms());
         res.addAll(conceptDisjointness.getOWLAxioms());
 
         if (!conceptOnly) {
             res.addAll(propertySubsumption.getOWLAxioms());
             res.addAll(propertyDomain.getOWLAxioms());
             res.addAll(propertyDisjointness.getOWLAxioms());
             res.addAll(propertyRange.getOWLAxioms());
         }
         return res;
     }
 
     /**
      * Adds the given axiom into the ontology which is managed by this reasoner instance. Afterwards, a
      * rematerialization using {@link #rematerialize()} might be required.
      *
      * @param axiom axiom to add into ontology
      */
     public void addAxiom(OWLAxiom axiom) {
         for (Matrix relevantMatrix : typeToMatrix.get(axiom.getAxiomType())) {
             relevantMatrix.addAxiom(axiom);
         }
         ontology.getOWLOntologyManager().addAxiom(ontology, axiom);
     }
 
     /**
      * Determines whether the given axiom is entailed by the ontology.
      *
      * @param axiom axiom to check entailment for
      * @return true if the axiom is entailed by the ontology, false otherwise
      */
     public boolean isEntailed(OWLAxiom axiom) {
         if (!typeToMatrix.containsKey(axiom.getAxiomType())) {
             throw new UnsupportedOperationException(
                     "Reasoner unable to handle axiom type: " + axiom.getAxiomType());
         }
         for (Matrix relevantMatrix : typeToMatrix.get(axiom.getAxiomType())) {
             if (relevantMatrix.isEntailed(axiom)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the explanation for the given axiom or null if the axiom is not entailed.
      *
      * @param axiom axiom to return explanation for
      * @return explanation for given axiom if axiom is entailed, otherwise null
      */
     public OrExpression getExplanation(OWLAxiom axiom) {
         if (!generateExplanations) {
             throw new UnsupportedOperationException(
                     "Trying to retrieve explanations from an reasoner with disabled explanation support");
         }
         if (!typeToMatrix.containsKey(axiom.getAxiomType())) {
             throw new UnsupportedOperationException(
                     "Reasoner unable to handle axiom type: " + axiom.getAxiomType());
         }
         OrExpression explanation = null;
         for (Matrix relevantMatrix : typeToMatrix.get(axiom.getAxiomType())) {
             OrExpression newExplanation = relevantMatrix.getExplanation(axiom);
             if (newExplanation == null) {
                 continue;
             }
             if (explanation == null) {
                 explanation = newExplanation.copy();
             } else {
                //explanation = ExpressionMinimizer.flatten(explanation, newExplanation);
                explanation = ExpressionMinimizer.minimize(explanation, newExplanation);
             }
             ExpressionMinimizer.minimize(explanation);
         }
         return explanation;
     }
 
     /**
      * Returns true if the concept identified by the IRI <code>subClass</code> is a subconcept of the concept identified
      * by the IRI <code>superClass</code>.
      *
      * @param subClass   IRI of potential subconcept
      * @param superClass IRI of potential superconcept
      * @return true if subClass is subconcept of superClass
      */
     public boolean isSubClassOf(String subClass, String superClass) {
         return conceptSubsumption.get(subClass, superClass);
     }
 
     /**
      * Returns true if the concept identified by the id <code>subClass</code> is a subconcept of the concept identified
      * by the id <code>superClass</code>.
      *
      * @param subClass   ID of potential subconcept
      * @param superClass ID of potential superconcept
      * @return true if subClass is subconcept of superClass
      */
     public boolean isSubClassOf(int subClass, int superClass) {
         return conceptSubsumption.get(subClass, superClass);
     }
 
     /**
      * Returns true if the concept identified by the IRI <code>subProperty</code> is a subproperty of the property
      * identified by the IRI <code>superProperty</code>.
      *
      * @param subProperty   IRI of potential subproperty
      * @param superProperty IRI of potential superproperty
      * @return true if subProperty is subproperty of superProperty
      */
     public boolean isSubPropertyOf(String subProperty, String superProperty) {
         return propertySubsumption.get(subProperty, superProperty);
     }
 
     /**
      * Returns true if the concept identified by the id <code>subProperty</code> is a subproperty of the property
      * identified by the id <code>superProperty</code>.
      *
      * @param subProperty   ID of potential subproperty
      * @param superProperty ID of potential superproperty
      * @return true if subProperty is subproperty of superProperty
      */
     public boolean isSubPropertyOf(int subProperty, int superProperty) {
         return propertySubsumption.get(subProperty, superProperty);
     }
 
     /**
      * Determines whether both classes are disjoint.
      *
      * @param class1 first class
      * @param class2 second class
      * @return true if both classes are disjoint, otherwise false
      */
     public boolean areDisjointClasses(String class1, String class2) {
         return conceptDisjointness.get(class1, class2);
     }
 
     /**
      * Determines whether both classes are disjoint.
      *
      * @param class1 first class
      * @param class2 second class
      * @return true if both classes are disjoint, otherwise false
      */
     public boolean areDisjointClasses(int class1, int class2) {
         return conceptDisjointness.get(class1, class2);
     }
 
     /**
      * Determines whether both properties are disjoint.
      *
      * @param property1 first property
      * @param property2 second property
      * @return true if both classes are disjoint, otherwise false
      */
     public boolean areDisjointProperties(String property1, String property2) {
         return propertyDisjointness.get(property1, property2);
     }
 
     /**
      * Determines whether both properties are disjoint.
      *
      * @param property1 first property
      * @param property2 second property
      * @return true if both classes are disjoint, otherwise false
      */
     public boolean areDisjointProperties(int property1, int property2) {
         return propertyDisjointness.get(property1, property2);
     }
 
     /**
      * Returns the set of all classes which are unsatisfiable in the ontology.
      *
      * @return set of all unsatisfiable classes
      */
     public Set<OWLClass> getIncoherentClasses() {
         Set<OWLClass> res = new HashSet<OWLClass>();
         for (int i = 0; i < conceptDisjointness.dimensionCol; i++) {
             if (conceptDisjointness.get(i, i)) {
                 res.add(dataFactory.getOWLClass(IRI.create(namingManager.getConceptIRI(i))));
             }
         }
 
         return res;
     }
 
     /**
      * Returns the set of all unsatisfiable properties, i.e., properties whose extension must be empty.
      *
      * @return set of all unsatisfiable properties
      */
     public Set<OWLObjectProperty> getIncoherentProperties() {
         Set<OWLObjectProperty> res = new HashSet<OWLObjectProperty>();
         for (int i = 0; i < propertyDisjointness.dimensionCol; i++) {
             if (propertyDisjointness.get(i, i)) {
                 res.add(dataFactory.getOWLObjectProperty(IRI.create(namingManager.getPropertyIRI(i))));
             }
         }
 
         for (int i = 0; i < propertyUnsatisfiability.dimensionCol; i++) {
             if (propertyUnsatisfiability.get(0, i)) {
                 res.add(dataFactory.getOWLObjectProperty(IRI.create(namingManager.getPropertyIRI(i))));
             }
         }
 
         return res;
     }
 
     public Set<OWLClass> getConceptCycles() {
         Set<OWLClass> res = new HashSet<OWLClass>();
         for (int i = 0; i < conceptSubsumption.dimensionCol; i++) {
             if (conceptSubsumption.get(i, i)) {
                 res.add(dataFactory.getOWLClass(IRI.create(namingManager.getConceptIRI(i))));
             }
         }
 
         return res;
     }
 
     public boolean isGenerateExplanations() {
         return generateExplanations;
     }
 
     public Set<OWLObjectProperty> getPropertyCycles() {
         Set<OWLObjectProperty> res = new HashSet<OWLObjectProperty>();
         for (int i = 0; i < conceptSubsumption.dimensionCol; i++) {
             if (conceptSubsumption.get(i, i)) {
 
                 res.add(dataFactory.getOWLObjectProperty(IRI.create(namingManager.getPropertyIRI(i))));
             }
         }
 
         return res;
     }
 }
