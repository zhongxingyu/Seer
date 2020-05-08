 package org.semanticweb.owlapi.gwt.client;
 
 import com.google.gwt.user.client.rpc.RemoteService;
 import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.vocab.OWLFacet;
 import uk.ac.manchester.cs.owl.owlapi.*;
 
 /**
  * Author: Matthew Horridge<br>
  * Stanford University<br>
  * Bio-Medical Informatics Research Group<br>
  * Date: 19/11/2013
  * <p>
  *     Test cases for concrete implementation classes.  Real services would use interfaces e.g. OWLClass.
  *     The service has a method for each type that should be serializable.  This ensures it gets compiled by
  *     the GWT compiler and that errors are informative.
  * </p>
  */
 @RemoteServiceRelativePath("owlobjects")
 public interface OWLObjectSerializationTestsService extends RemoteService {
 
     AxiomType<?> testAxiomType(AxiomType<?> axiomType);
 
     EntityType<?> testEntityType(EntityType<?> entityType);
 
     ClassExpressionType testClassExpressionType(ClassExpressionType type);
 
     IRI testIRI(IRI iri);
 
     OWLOntologyID testOWLOntologyId(OWLOntologyID id);
 
     OWLClassImpl testOWLClassImpl(OWLClassImpl cls);
 
     OWLObjectPropertyImpl testOWLObjectPropertyImpl(OWLObjectPropertyImpl property);
 
     OWLDataPropertyImpl testOWLDataPropertyImpl(OWLDataPropertyImpl dataProperty);
 
     OWLAnnotationPropertyImpl testOWLAnnotationPropertyImpl(OWLAnnotationPropertyImpl annotationProperty);
 
     OWLNamedIndividualImpl testOWLNamedIndividualImpl(OWLNamedIndividualImpl namedIndividual);
 
     OWLDatatypeImpl testOWLDatatypeImpl(OWLDatatypeImpl datatype);
 
     OWLAnonymousIndividualImpl testOWLAnonymousIndividualImpl(OWLAnonymousIndividualImpl individual);
 
     OWLAnnotationImpl testOWLAnnotationImpl(OWLAnnotationImpl annotation);
 
     OWLImportsDeclarationImpl testOWLImportsDeclarationImpl(OWLImportsDeclarationImpl importsDeclaration);
 
     OWLLiteralImplBoolean testOWLLiteralImplBoolean(OWLLiteralImplBoolean literal);
 
     OWLLiteralImplDouble testOWLLiteralImplDouble(OWLLiteralImplDouble literal);
 
     OWLLiteralImplInteger testOWLLiteralImplInteger(OWLLiteralImplInteger literal);
 
     OWLLiteralImplNoCompression testOWLLiteralImplNoCompression(OWLLiteralImplNoCompression literal);
 
     OWL2DatatypeImpl testOWL2DatatypeImpl(OWL2DatatypeImpl datatype);
 
     OWLObjectInverseOfImpl testOWLObjectInverseOfImpl(OWLObjectInverseOfImpl property);
 
 
     // Complex class expressions
 
     OWLObjectSomeValuesFromImpl testOWLObjectSomeValuesFromImpl(OWLObjectSomeValuesFromImpl classExpression);
     
     OWLObjectAllValuesFromImpl testOWLObjectAllValuesFromImpl(OWLObjectAllValuesFromImpl classExpression);
 
     OWLObjectHasValueImpl testOWLObjectHasValueImpl(OWLObjectHasValueImpl classExpression);
 
     OWLObjectMinCardinalityImpl testOWLObjectMinCardinalityImpl(OWLObjectMinCardinalityImpl classExpression);
 
     OWLObjectMaxCardinalityImpl testOWLObjectMaxCardinalityImpl(OWLObjectMaxCardinalityImpl classExpression);
 
     OWLObjectExactCardinalityImpl testOWLObjectExactCardinalityImpl(OWLObjectExactCardinalityImpl classExpression);
 
     OWLObjectHasSelfImpl testOWLObjectHasSelfImpl(OWLObjectHasSelfImpl classExpression);
 
     OWLObjectIntersectionOfImpl testOWLObjectIntersectionOfImpl(OWLObjectIntersectionOfImpl classExpression);
 
     OWLObjectUnionOfImpl testOWLObjectUnionOfImpl(OWLObjectUnionOfImpl classExpression);
 
     OWLObjectComplementOfImpl testOWLObjectComplementOfImpl(OWLObjectComplementOfImpl classExpression);
 
     OWLObjectOneOfImpl testOWLObjectOneOfImpl(OWLObjectOneOfImpl classExpression);
 
 
 
     OWLDataSomeValuesFromImpl testOWLDataSomeValuesFromImpl(OWLDataSomeValuesFromImpl classExpression);
 
     OWLDataAllValuesFromImpl testOWLDataAllValuesFromImpl(OWLDataAllValuesFromImpl classExpression);
 
     OWLDataHasValueImpl testOWLDataHasValueImpl(OWLDataHasValueImpl classExpression);
 
     OWLDataMinCardinalityImpl testOWLDataMinCardinalityImpl(OWLDataMinCardinalityImpl classExpression);
 
     OWLDataMaxCardinalityImpl testOWLDataMaxCardinalityImpl(OWLDataMaxCardinalityImpl classExpression);
 
     OWLDataExactCardinalityImpl testOWLDataExactCardinalityImpl(OWLDataExactCardinalityImpl classExpression);
 
     OWLDataIntersectionOfImpl testOWLDataIntersectionOfImpl(OWLDataIntersectionOfImpl classExpression);
 
     OWLDataUnionOfImpl testOWLDataUnionOfImpl(OWLDataUnionOfImpl classExpression);
 
     OWLDataComplementOfImpl testOWLDataComplementOfImpl(OWLDataComplementOfImpl classExpression);
 
     OWLDataOneOfImpl testOWLDataOneOfImpl(OWLDataOneOfImpl classExpression);
 
 
     OWLDatatypeRestrictionImpl testOWLDatatypeRestrictionImpl(OWLDatatypeRestrictionImpl classExpression);
 
     OWLFacetRestrictionImpl testOWLFacetRestrictionImpl(OWLFacetRestrictionImpl facetRestriction);
 
 
 
     OWLSubClassOfAxiomImpl testOWLSubClassOfAxiomImpl(OWLSubClassOfAxiomImpl axiom);
 
     OWLEquivalentClassesAxiomImpl testOWLEquivalentClassesAxiomImpl(OWLEquivalentClassesAxiomImpl axiom);
 
     OWLDisjointClassesAxiomImpl testOWLDisjointClassesAxiomImpl(OWLDisjointClassesAxiomImpl axiom);
 
     OWLDisjointUnionAxiomImpl testOWLDisjointUnionAxiomImpl(OWLDisjointUnionAxiomImpl axiom);
 
 
     OWLClassAssertionAxiomImpl testOWLClassAssertionAxiomImpl(OWLClassAssertionAxiomImpl axiom);
     
     OWLObjectPropertyAssertionAxiomImpl testOWLObjectPropertyAssertionAxiomImpl(OWLObjectPropertyAssertionAxiomImpl axiom);
     
     OWLDataPropertyAssertionAxiomImpl testOWLDataPropertyAssertionAxiomImpl(OWLDataPropertyAssertionAxiomImpl axiom);
 
     OWLNegativeObjectPropertyAssertionAxiomImpl testOWLNegativeObjectPropertyAssertionAxiomImpl(OWLNegativeObjectPropertyAssertionAxiomImpl axiom);
 
     OWLNegativeDataPropertyAssertionAxiomImpl testOWLNegativeDataPropertyAssertionAxiomImpl(OWLNegativeDataPropertyAssertionAxiomImpl axiom);
 
     OWLSameIndividualAxiomImpl testOWLSameIndividualAxiomImpl(OWLSameIndividualAxiomImpl axiom);
 
     OWLDifferentIndividualsAxiomImpl testOWLDifferentIndividualsAxiomImpl(OWLDifferentIndividualsAxiomImpl axiom);
 
 
     OWLSubObjectPropertyOfAxiomImpl testOWLSubObjectPropertyOfAxiomImpl(OWLSubObjectPropertyOfAxiomImpl axiom);
 
     OWLEquivalentObjectPropertiesAxiomImpl testOWLEquivalentObjectPropertiesAxiomImpl(OWLEquivalentObjectPropertiesAxiomImpl axiom);
 
     OWLDisjointObjectPropertiesAxiomImpl testOWLDisjointObjectPropertiesAxiomImpl(OWLDisjointObjectPropertiesAxiomImpl axiom);
 
     OWLInverseObjectPropertiesAxiomImpl testOWLInverseObjectPropertiesAxiomImpl(OWLInverseObjectPropertiesAxiomImpl axiom);
 
     OWLObjectPropertyDomainAxiomImpl testOWLObjectPropertyDomainAxiomImpl(OWLObjectPropertyDomainAxiomImpl axiom);
 
     OWLObjectPropertyRangeAxiomImpl testOWLObjectPropertyRangeAxiomImpl(OWLObjectPropertyRangeAxiomImpl axiom);
 
     OWLFunctionalObjectPropertyAxiomImpl testOWLFunctionalObjectPropertyAxiomImpl(OWLFunctionalObjectPropertyAxiomImpl axiom);
 
     OWLInverseFunctionalObjectPropertyAxiomImpl testOWLInverseFunctionalObjectPropertyAxiomImpl(OWLInverseFunctionalObjectPropertyAxiomImpl axiom);
 
     OWLReflexiveObjectPropertyAxiomImpl testOWLReflexiveObjectPropertyAxiomImpl(OWLReflexiveObjectPropertyAxiomImpl axiom);
 
     OWLIrreflexiveObjectPropertyAxiomImpl testOWLIrreflexiveObjectPropertyAxiomImpl(OWLIrreflexiveObjectPropertyAxiomImpl axiom);
 
     OWLSymmetricObjectPropertyAxiomImpl testOWLSymmetricObjectPropertyAxiomImpl(OWLSymmetricObjectPropertyAxiomImpl axiom);
 
     OWLAsymmetricObjectPropertyAxiomImpl testOWLAsymmetricObjectPropertyAxiomImpl(OWLAsymmetricObjectPropertyAxiomImpl axiom);
 
     OWLTransitiveObjectPropertyAxiomImpl testOWLTransitiveObjectPropertyAxiomImpl(OWLTransitiveObjectPropertyAxiomImpl axiom);
 
 
     OWLSubDataPropertyOfAxiomImpl testOWLSubDataPropertyOfAxiomImpl(OWLSubDataPropertyOfAxiomImpl axiom);
 
     OWLEquivalentDataPropertiesAxiomImpl testOWLEquivalentDataPropertiesAxiomImpl(OWLEquivalentDataPropertiesAxiomImpl axiom);
 
     OWLDisjointDataPropertiesAxiomImpl testOWLDisjointDataPropertiesAxiomImpl(OWLDisjointDataPropertiesAxiomImpl axiom);
 
     OWLDataPropertyDomainAxiomImpl testOWLDataPropertyDomainAxiomImpl(OWLDataPropertyDomainAxiomImpl axiom);
 
     OWLDataPropertyRangeAxiomImpl testOWLDataPropertyRangeAxiomImpl(OWLDataPropertyRangeAxiomImpl axiom);
 
     OWLFunctionalDataPropertyAxiomImpl testOWLFunctionalDataPropertyAxiomImpl(OWLFunctionalDataPropertyAxiomImpl axiom);
 
 
     OWLSubAnnotationPropertyOfAxiomImpl testOWLSubAnnotationPropertyOfAxiomImpl(OWLSubAnnotationPropertyOfAxiomImpl axiom);
 
     OWLAnnotationPropertyDomainAxiomImpl testOWLAnnotationPropertyDomainAxiomImpl(OWLAnnotationPropertyDomainAxiomImpl axiom);
 
     OWLAnnotationPropertyRangeAxiomImpl testOWLAnnotationPropertyRangeAxiomImpl(OWLAnnotationPropertyRangeAxiomImpl axiom);
 
 
     OWLDatatypeDefinitionAxiomImpl testOWLDatatypeDefinitionAxiomImpl(OWLDatatypeDefinitionAxiomImpl axiom);
 
     OWLHasKeyAxiomImpl testOWLHasKeyAxiomImpl(OWLHasKeyAxiomImpl axiom);
 
     OWLDeclarationAxiomImpl testOWLDeclarationAxiomImpl(OWLDeclarationAxiomImpl axiom);
 
     OWLFacet testOWLFacet(OWLFacet facet);
 }
