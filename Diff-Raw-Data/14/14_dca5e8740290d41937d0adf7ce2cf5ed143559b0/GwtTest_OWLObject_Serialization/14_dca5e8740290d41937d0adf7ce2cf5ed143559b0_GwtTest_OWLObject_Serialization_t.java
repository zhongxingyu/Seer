 package org.semanticweb.owlapi.gwt.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.junit.client.GWTTestCase;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.util.CollectionFactory;
 import org.semanticweb.owlapi.vocab.OWL2Datatype;
 import org.semanticweb.owlapi.vocab.OWLFacet;
 import uk.ac.manchester.cs.owl.owlapi.*;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Author: Matthew Horridge<br>
  * Stanford University<br>
  * Bio-Medical Informatics Research Group<br>
  * Date: 19/11/2013
  * <p>
  * This class name is prefixed with GwtTest so that it is recognized by the maven GWT plugin as a GWTTestCase.
  * Also, it does not end with "TestCase" so that it is not picked up by the maven sure fire plugin and run
  * as a regular test case.
  * </p>
  */
 public class GwtTest_OWLObject_Serialization extends GWTTestCase {
 
     public static final int TEST_DELAY_MS = 10000;
 
     private OWLObjectSerializationTestsServiceAsync service;
 
     private OWLDataFactory dataFactory;
 
     @Override
     public String getModuleName() {
         return "org.semanticweb.owlapi.gwt.OWLObjectSerializationTestsJUnit";
     }
 
     @Override
     protected void gwtSetUp() throws Exception {
         delayTestFinish(TEST_DELAY_MS);
         service = GWT.create(OWLObjectSerializationTestsService.class);
         dataFactory = new OWLDataFactoryImpl(false, false);
     }
 
     private int counter = 0;
 
     private synchronized int nextInt() {
         counter++;
         return counter;
     }
 
     private IRI newIRI() {
         return IRI.create("http://semanticweb.org/owlapi/I" + nextInt());
     }
 
     private OWLClass cls() {
         return dataFactory.getOWLClass(newIRI());
     }
 
     private OWLDatatype dt() {
         return dataFactory.getOWLDatatype(newIRI());
     }
 
     private OWLDataProperty dp() {
         return dataFactory.getOWLDataProperty(newIRI());
     }
 
     private OWLAnnotationProperty ap() {
         return dataFactory.getOWLAnnotationProperty(newIRI());
     }
 
     private OWLLiteral lit() {
         return dataFactory.getOWLLiteral("Literal" + nextInt());
     }
 
     private OWLNamedIndividual ind() {
         return dataFactory.getOWLNamedIndividual(newIRI());
     }
     
     private Set<OWLAnnotation> annos() {
         return CollectionFactory.createSet(anno(), anno(), anno());
     }
 
     private OWLAnnotation anno() {
         return dataFactory.getOWLAnnotation(ap(), lit());
     }
 
     private OWLObjectPropertyExpression op() {
         return dataFactory.getOWLObjectInverseOf(dataFactory.getOWLObjectProperty(newIRI()));
     }
 
 
     private Set<OWLClassExpression> newOWLClassExpressionSet() {
         return CollectionFactory.<OWLClassExpression>createSet(cls(), cls(), cls());
     }
 
     private Set<OWLDataProperty> dpSet() {
         return CollectionFactory.createSet(dp(), dp(), dp());
     }
 
     private Set<OWLDataRange> drSet() {
         return CollectionFactory.<OWLDataRange>createSet(dt(), dt(), dt());
     }
 
     private Set<OWLLiteral> litSet() {
         return CollectionFactory.createSet(lit(), lit(), lit());
     }
 
     private Set<OWLNamedIndividual> indSet() {
         return CollectionFactory.createSet(ind(), ind(), ind());
     }
 
     private Set<OWLObjectPropertyExpression> opSet() {
         return CollectionFactory.createSet(op(), op(), op());
     }
 
 
 
     public void testShouldSerializeAxiomType() {
         final AxiomType<?> in = AxiomType.ANNOTATION_ASSERTION;
         service.testAxiomType(in, new AsyncCallback<AxiomType<?>>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(AxiomType<?> out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeEntityType() {
         final EntityType<?> in = EntityType.CLASS;
         service.testEntityType(in, new AsyncCallback<EntityType<?>>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(EntityType<?> out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeClassExpressionType() {
         final ClassExpressionType in = ClassExpressionType.OWL_CLASS;
         service.testClassExpressionType(in, new AsyncCallback<ClassExpressionType>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(ClassExpressionType out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeIRI() {
         final IRI in = newIRI();
         service.testIRI(in, new AsyncCallback<IRI>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(IRI out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLOntologyID() {
         final OWLOntologyID in = new OWLOntologyID(newIRI());
         service.testOWLOntologyId(in, new AsyncCallback<OWLOntologyID>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLOntologyID out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLClassImpl() {
         final OWLClassImpl in = new OWLClassImpl(newIRI());
         service.testOWLClassImpl(in, new AsyncCallback<OWLClassImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLClassImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyImpl() {
         final OWLObjectPropertyImpl in = new OWLObjectPropertyImpl(newIRI());
         service.testOWLObjectPropertyImpl(in, new AsyncCallback<OWLObjectPropertyImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataPropertyImpl() {
         final OWLDataPropertyImpl in = new OWLDataPropertyImpl(newIRI());
         service.testOWLDataPropertyImpl(in, new AsyncCallback<OWLDataPropertyImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAnnotationPropertyImpl() {
         final OWLAnnotationPropertyImpl in = new OWLAnnotationPropertyImpl(newIRI());
         service.testOWLAnnotationPropertyImpl(in, new AsyncCallback<OWLAnnotationPropertyImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAnnotationPropertyImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLNamedIndividualImpl() {
         final OWLNamedIndividualImpl in = new OWLNamedIndividualImpl(newIRI());
         service.testOWLNamedIndividualImpl(in, new AsyncCallback<OWLNamedIndividualImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLNamedIndividualImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDatatypeImpl() {
         final OWLDatatypeImpl in = new OWLDatatypeImpl(newIRI());
         service.testOWLDatatypeImpl(in, new AsyncCallback<OWLDatatypeImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDatatypeImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplBoolean() {
         final OWLLiteralImplBoolean in = new OWLLiteralImplBoolean(true);
         service.testOWLLiteralImplBoolean(in, new AsyncCallback<OWLLiteralImplBoolean>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLLiteralImplBoolean out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplDouble() {
         final OWLLiteralImplDouble in = new OWLLiteralImplDouble(3.3, new OWLDatatypeImpl(newIRI()));
         service.testOWLLiteralImplDouble(in, new AsyncCallback<OWLLiteralImplDouble>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLLiteralImplDouble out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplInteger() {
         final OWLLiteralImplInteger in = new OWLLiteralImplInteger(3, new OWLDatatypeImpl(newIRI()));
         service.testOWLLiteralImplInteger(in, new AsyncCallback<OWLLiteralImplInteger>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLLiteralImplInteger out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWL2DatatypeImpl() {
         final OWL2DatatypeImpl in = (OWL2DatatypeImpl) OWL2DatatypeImpl.getDatatype(OWL2Datatype.RDF_PLAIN_LITERAL);
         service.testOWL2DatatypeImpl(in, new AsyncCallback<OWL2DatatypeImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWL2DatatypeImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLLiteralImplNoCompression() {
         final OWLLiteralImplNoCompression in = new OWLLiteralImplNoCompression("xyz", null, null);
         service.testOWLLiteralImplNoCompression(in, new AsyncCallback<OWLLiteralImplNoCompression>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLLiteralImplNoCompression out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectInverseOfImpl() {
         final OWLObjectInverseOfImpl in = new OWLObjectInverseOfImpl(op());
         service.testOWLObjectInverseOfImpl(in, new AsyncCallback<OWLObjectInverseOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectInverseOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectIntersectionOfImpl() {
         final OWLObjectIntersectionOfImpl in = new OWLObjectIntersectionOfImpl(newOWLClassExpressionSet());
         service.testOWLObjectIntersectionOfImpl(in, new AsyncCallback<OWLObjectIntersectionOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectIntersectionOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectUnionOfImpl() {
         final OWLObjectUnionOfImpl in = new OWLObjectUnionOfImpl(newOWLClassExpressionSet());
         service.testOWLObjectUnionOfImpl(in, new AsyncCallback<OWLObjectUnionOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectUnionOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectComplementOfImpl() {
         final OWLObjectComplementOfImpl in = new OWLObjectComplementOfImpl(cls());
         service.testOWLObjectComplementOfImpl(in, new AsyncCallback<OWLObjectComplementOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectComplementOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectOneOfImpl() {
         final OWLObjectOneOfImpl in = new OWLObjectOneOfImpl(indSet());
         service.testOWLObjectOneOfImpl(in, new AsyncCallback<OWLObjectOneOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectOneOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectSomeValuesFromImpl() {
         final OWLObjectSomeValuesFromImpl in = new OWLObjectSomeValuesFromImpl(op(), cls());
         service.testOWLObjectSomeValuesFromImpl(in, new AsyncCallback<OWLObjectSomeValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectSomeValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLObjectAllValuesFromImpl() {
         final OWLObjectAllValuesFromImpl in = new OWLObjectAllValuesFromImpl(op(), cls());
         service.testOWLObjectAllValuesFromImpl(in, new AsyncCallback<OWLObjectAllValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectAllValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectHasValueImpl() {
         final OWLObjectHasValueImpl in = new OWLObjectHasValueImpl(op(), ind());
         service.testOWLObjectHasValueImpl(in, new AsyncCallback<OWLObjectHasValueImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectHasValueImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectMinCardinalityImpl() {
         final OWLObjectMinCardinalityImpl in = new OWLObjectMinCardinalityImpl(op(), 3, cls());
         service.testOWLObjectMinCardinalityImpl(in, new AsyncCallback<OWLObjectMinCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectMinCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectMaxCardinalityImpl() {
         final OWLObjectMaxCardinalityImpl in = new OWLObjectMaxCardinalityImpl(op(), 3, cls());
         service.testOWLObjectMaxCardinalityImpl(in, new AsyncCallback<OWLObjectMaxCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectMaxCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectExactCardinalityImpl() {
         final OWLObjectExactCardinalityImpl in = new OWLObjectExactCardinalityImpl(op(), 3, cls());
         service.testOWLObjectExactCardinalityImpl(in, new AsyncCallback<OWLObjectExactCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectExactCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectHasSeltImpl() {
         final OWLObjectHasSelfImpl in = new OWLObjectHasSelfImpl(op());
         service.testOWLObjectHasSelfImpl(in, new AsyncCallback<OWLObjectHasSelfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectHasSelfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataIntersectionOfImpl() {
         final OWLDataIntersectionOfImpl in = new OWLDataIntersectionOfImpl(drSet());
         service.testOWLDataIntersectionOfImpl(in, new AsyncCallback<OWLDataIntersectionOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataIntersectionOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
 
 
     public void testShouldSerializeOWLDataUnionOfImpl() {
         final OWLDataUnionOfImpl in = new OWLDataUnionOfImpl(drSet());
         service.testOWLDataUnionOfImpl(in, new AsyncCallback<OWLDataUnionOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataUnionOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataComplementOfImpl() {
         final OWLDataComplementOfImpl in = new OWLDataComplementOfImpl(dt());
         service.testOWLDataComplementOfImpl(in, new AsyncCallback<OWLDataComplementOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataComplementOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataOneOfImpl() {
         final OWLDataOneOfImpl in = new OWLDataOneOfImpl(litSet());
         service.testOWLDataOneOfImpl(in, new AsyncCallback<OWLDataOneOfImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataOneOfImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataSomeValuesFromImpl() {
         final OWLDataSomeValuesFromImpl in = new OWLDataSomeValuesFromImpl(dp(), dt());
         service.testOWLDataSomeValuesFromImpl(in, new AsyncCallback<OWLDataSomeValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataSomeValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataAllValuesFromImpl() {
         final OWLDataAllValuesFromImpl in = new OWLDataAllValuesFromImpl(dp(), dt());
         service.testOWLDataAllValuesFromImpl(in, new AsyncCallback<OWLDataAllValuesFromImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataAllValuesFromImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataHasValueImpl() {
         final OWLDataHasValueImpl in = new OWLDataHasValueImpl(dp(), lit());
         service.testOWLDataHasValueImpl(in, new AsyncCallback<OWLDataHasValueImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataHasValueImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataMinCardinalityImpl() {
         final OWLDataMinCardinalityImpl in = new OWLDataMinCardinalityImpl(dp(), 3, dt());
         service.testOWLDataMinCardinalityImpl(in, new AsyncCallback<OWLDataMinCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataMinCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataMaxCardinalityImpl() {
         final OWLDataMaxCardinalityImpl in = new OWLDataMaxCardinalityImpl(dp(), 3, dt());
         service.testOWLDataMaxCardinalityImpl(in, new AsyncCallback<OWLDataMaxCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataMaxCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataExactCardinalityImpl() {
         final OWLDataExactCardinalityImpl in = new OWLDataExactCardinalityImpl(dp(), 3, dt());
         service.testOWLDataExactCardinalityImpl(in, new AsyncCallback<OWLDataExactCardinalityImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataExactCardinalityImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDatatypeRestrictionImpl() {
         Set<OWLFacetRestriction> facetRestrictions = new HashSet<OWLFacetRestriction>();
         facetRestrictions.add(dataFactory.getOWLFacetRestriction(OWLFacet.MIN_LENGTH, lit()));
         facetRestrictions.add(dataFactory.getOWLFacetRestriction(OWLFacet.MAX_LENGTH, lit()));
         final OWLDatatypeRestrictionImpl in = new OWLDatatypeRestrictionImpl(dt(), facetRestrictions);
         service.testOWLDatatypeRestrictionImpl(in, new AsyncCallback<OWLDatatypeRestrictionImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDatatypeRestrictionImpl out) {
                 assertEquals(in, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSubClassOfAxiomImpl() {
         final OWLSubClassOfAxiomImpl ax = new OWLSubClassOfAxiomImpl(cls(), cls(), annos());
         service.testOWLSubClassOfAxiomImpl(ax, new AsyncCallback<OWLSubClassOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubClassOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLEquivalentClassesAxiomImpl() {
         final OWLEquivalentClassesAxiomImpl ax = new OWLEquivalentClassesAxiomImpl(newOWLClassExpressionSet(), annos());
         service.testOWLEquivalentClassesAxiomImpl(ax, new AsyncCallback<OWLEquivalentClassesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLEquivalentClassesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
 
 
     public void testShouldSerializeOWLDisjointClassesAxiomImpl() {
         final OWLDisjointClassesAxiomImpl ax = new OWLDisjointClassesAxiomImpl(newOWLClassExpressionSet(), annos());
         service.testOWLDisjointClassesAxiomImpl(ax, new AsyncCallback<OWLDisjointClassesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointClassesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDisjointUnionAxiomImpl() {
         final OWLDisjointUnionAxiomImpl ax = new OWLDisjointUnionAxiomImpl(cls(), newOWLClassExpressionSet(), annos());
         service.testOWLDisjointUnionAxiomImpl(ax, new AsyncCallback<OWLDisjointUnionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointUnionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLClassAssertionAxiomImpl() {
         final OWLClassAssertionAxiomImpl ax = new OWLClassAssertionAxiomImpl(ind(), cls(), annos());
         service.testOWLClassAssertionAxiomImpl(ax, new AsyncCallback<OWLClassAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLClassAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyAssertionAxiomImpl() {
         final OWLObjectPropertyAssertionAxiomImpl ax = new OWLObjectPropertyAssertionAxiomImpl(ind(), op(), ind(), annos());
         service.testOWLObjectPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLObjectPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLNegativeObjectPropertyAssertionAxiomImpl() {
         final OWLNegativeObjectPropertyAssertionAxiomImpl ax = new OWLNegativeObjectPropertyAssertionAxiomImpl(ind(), op(), ind(), annos());
         service.testOWLNegativeObjectPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLNegativeObjectPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLNegativeObjectPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataPropertyAssertionAxiomImpl() {
         final OWLDataPropertyAssertionAxiomImpl ax = new OWLDataPropertyAssertionAxiomImpl(ind(), dp(), lit(), annos());
         service.testOWLDataPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLDataPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLNegativeDataPropertyAssertionAxiomImpl() {
         final OWLNegativeDataPropertyAssertionAxiomImpl ax = new OWLNegativeDataPropertyAssertionAxiomImpl(ind(), dp(), lit(), annos());
         service.testOWLNegativeDataPropertyAssertionAxiomImpl(ax, new AsyncCallback<OWLNegativeDataPropertyAssertionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLNegativeDataPropertyAssertionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSameIndividualAxiomImpl() {
         Set<OWLAnnotation> annos = annos();
         final OWLSameIndividualAxiomImpl ax = new OWLSameIndividualAxiomImpl(indSet(), annos);
         service.testOWLSameIndividualAxiomImpl(ax, new AsyncCallback<OWLSameIndividualAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSameIndividualAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDifferentIndividualsAxiomImpl() {
         final OWLDifferentIndividualsAxiomImpl ax = new OWLDifferentIndividualsAxiomImpl(indSet(), annos());
         service.testOWLDifferentIndividualsAxiomImpl(ax, new AsyncCallback<OWLDifferentIndividualsAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDifferentIndividualsAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSubObjectPropertyOfAxiomImpl() {
         final OWLSubObjectPropertyOfAxiomImpl ax = new OWLSubObjectPropertyOfAxiomImpl(op(), op(), annos());
         service.testOWLSubObjectPropertyOfAxiomImpl(ax, new AsyncCallback<OWLSubObjectPropertyOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubObjectPropertyOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLEquivalentObjectPropertiesAxiomImpl() {
         final OWLEquivalentObjectPropertiesAxiomImpl ax = new OWLEquivalentObjectPropertiesAxiomImpl(opSet(), annos());
         service.testOWLEquivalentObjectPropertiesAxiomImpl(ax, new AsyncCallback<OWLEquivalentObjectPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLEquivalentObjectPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
 
 
     public void testShouldSerializeOWLDisjointObjectPropertiesAxiomImpl() {
         final OWLDisjointObjectPropertiesAxiomImpl ax = new OWLDisjointObjectPropertiesAxiomImpl(opSet(), annos());
         service.testOWLDisjointObjectPropertiesAxiomImpl(ax, new AsyncCallback<OWLDisjointObjectPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointObjectPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLInverseObjectPropertiesAxiomImpl() {
         final OWLInverseObjectPropertiesAxiomImpl ax = new OWLInverseObjectPropertiesAxiomImpl(op(), op(), annos());
         service.testOWLInverseObjectPropertiesAxiomImpl(ax, new AsyncCallback<OWLInverseObjectPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLInverseObjectPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyDomainAxiomImpl() {
         final OWLObjectPropertyDomainAxiomImpl ax = new OWLObjectPropertyDomainAxiomImpl(op(), cls(), annos());
         service.testOWLObjectPropertyDomainAxiomImpl(ax, new AsyncCallback<OWLObjectPropertyDomainAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyDomainAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLObjectPropertyRangeAxiomImpl() {
         final OWLObjectPropertyRangeAxiomImpl ax = new OWLObjectPropertyRangeAxiomImpl(op(), cls(), annos());
         service.testOWLObjectPropertyRangeAxiomImpl(ax, new AsyncCallback<OWLObjectPropertyRangeAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLObjectPropertyRangeAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLFunctionalObjectPropertyAxiomImpl() {
         final OWLFunctionalObjectPropertyAxiomImpl ax = new OWLFunctionalObjectPropertyAxiomImpl(op(), annos());
         service.testOWLFunctionalObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLFunctionalObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLFunctionalObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
 
     public void testShouldSerializeOWLInverseFunctionalObjectPropertyAxiomImpl() {
         final OWLInverseFunctionalObjectPropertyAxiomImpl ax = new OWLInverseFunctionalObjectPropertyAxiomImpl(op(), annos());
         service.testOWLInverseFunctionalObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLInverseFunctionalObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLInverseFunctionalObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLReflexiveObjectPropertyAxiomImpl() {
         final OWLReflexiveObjectPropertyAxiomImpl ax = new OWLReflexiveObjectPropertyAxiomImpl(op(), annos());
         service.testOWLReflexiveObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLReflexiveObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLReflexiveObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLIrreflexiveObjectPropertyAxiomImpl() {
         final OWLIrreflexiveObjectPropertyAxiomImpl ax = new OWLIrreflexiveObjectPropertyAxiomImpl(op(), annos());
         service.testOWLIrreflexiveObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLIrreflexiveObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLIrreflexiveObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSymmetricObjectPropertyAxiomImpl() {
         final OWLSymmetricObjectPropertyAxiomImpl ax = new OWLSymmetricObjectPropertyAxiomImpl(op(), annos());
         service.testOWLSymmetricObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLSymmetricObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSymmetricObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAsymmetricObjectPropertyAxiomImpl() {
         final OWLAsymmetricObjectPropertyAxiomImpl ax = new OWLAsymmetricObjectPropertyAxiomImpl(op(), annos());
         service.testOWLAsymmetricObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLAsymmetricObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAsymmetricObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLTransitiveObjectPropertyAxiomImpl() {
         final OWLTransitiveObjectPropertyAxiomImpl ax = new OWLTransitiveObjectPropertyAxiomImpl(op(), annos());
         service.testOWLTransitiveObjectPropertyAxiomImpl(ax, new AsyncCallback<OWLTransitiveObjectPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLTransitiveObjectPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSubDataPropertyOfAxiomImpl() {
         final OWLSubDataPropertyOfAxiomImpl ax = new OWLSubDataPropertyOfAxiomImpl(dp(), dp(), annos());
         service.testOWLSubDataPropertyOfAxiomImpl(ax, new AsyncCallback<OWLSubDataPropertyOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubDataPropertyOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLEquivalentDataPropertiesAxiomImpl() {
         final OWLEquivalentDataPropertiesAxiomImpl ax = new OWLEquivalentDataPropertiesAxiomImpl(dpSet(), annos());
         service.testOWLEquivalentDataPropertiesAxiomImpl(ax, new AsyncCallback<OWLEquivalentDataPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLEquivalentDataPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDisjointDataPropertiesAxiomImpl() {
         final OWLDisjointDataPropertiesAxiomImpl ax = new OWLDisjointDataPropertiesAxiomImpl(dpSet(), annos());
         service.testOWLDisjointDataPropertiesAxiomImpl(ax, new AsyncCallback<OWLDisjointDataPropertiesAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDisjointDataPropertiesAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataPropertyDomainAxiomImpl() {
         final OWLDataPropertyDomainAxiomImpl ax = new OWLDataPropertyDomainAxiomImpl(dp(), cls(), annos());
         service.testOWLDataPropertyDomainAxiomImpl(ax, new AsyncCallback<OWLDataPropertyDomainAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyDomainAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDataPropertyRangeAxiomImpl() {
         final OWLDataPropertyRangeAxiomImpl ax = new OWLDataPropertyRangeAxiomImpl(dp(), dt(), annos());
         service.testOWLDataPropertyRangeAxiomImpl(ax, new AsyncCallback<OWLDataPropertyRangeAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDataPropertyRangeAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLFunctionalDataPropertyAxiomImpl() {
         final OWLFunctionalDataPropertyAxiomImpl ax = new OWLFunctionalDataPropertyAxiomImpl(dp(), annos());
         service.testOWLFunctionalDataPropertyAxiomImpl(ax, new AsyncCallback<OWLFunctionalDataPropertyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLFunctionalDataPropertyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLSubAnnotationPropertyOfAxiomImpl() {
         final OWLSubAnnotationPropertyOfAxiomImpl ax = new OWLSubAnnotationPropertyOfAxiomImpl(ap(), ap(), annos());
         service.testOWLSubAnnotationPropertyOfAxiomImpl(ax, new AsyncCallback<OWLSubAnnotationPropertyOfAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLSubAnnotationPropertyOfAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAnnotationPropertyDomainAxiomImpl() {
         final OWLAnnotationPropertyDomainAxiomImpl ax = new OWLAnnotationPropertyDomainAxiomImpl(ap(), newIRI(), annos());
         service.testOWLAnnotationPropertyDomainAxiomImpl(ax, new AsyncCallback<OWLAnnotationPropertyDomainAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAnnotationPropertyDomainAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLAnnotationPropertyRangeAxiomImpl() {
         final OWLAnnotationPropertyRangeAxiomImpl ax = new OWLAnnotationPropertyRangeAxiomImpl(ap(), newIRI(), annos());
         service.testOWLAnnotationPropertyRangeAxiomImpl(ax, new AsyncCallback<OWLAnnotationPropertyRangeAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLAnnotationPropertyRangeAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDatatypeDefinitionAxiomImpl() {
         final OWLDatatypeDefinitionAxiomImpl ax = new OWLDatatypeDefinitionAxiomImpl(dt(), dt(), annos());
         service.testOWLDatatypeDefinitionAxiomImpl(ax, new AsyncCallback<OWLDatatypeDefinitionAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDatatypeDefinitionAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLHasKeyAxiomImpl() {
         Set<OWLPropertyExpression<?, ?>> props = CollectionFactory.<OWLPropertyExpression<?, ?>>createSet(op(), op(), dp(), dp());
         final OWLHasKeyAxiomImpl ax = new OWLHasKeyAxiomImpl(cls(), props, annos());
         service.testOWLHasKeyAxiomImpl(ax, new AsyncCallback<OWLHasKeyAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLHasKeyAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLDeclarationAxiomImpl() {
         final OWLDeclarationAxiomImpl ax = new OWLDeclarationAxiomImpl(cls(), annos());
         service.testOWLDeclarationAxiomImpl(ax, new AsyncCallback<OWLDeclarationAxiomImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLDeclarationAxiomImpl out) {
                 assertEquals(ax, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLImportsDeclarationImpl() {
         final OWLImportsDeclarationImpl decl = new OWLImportsDeclarationImpl(newIRI());
         service.testOWLImportsDeclarationImpl(decl, new AsyncCallback<OWLImportsDeclarationImpl>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLImportsDeclarationImpl out) {
                 assertEquals(decl, out);
                 finishTest();
             }
         });
     }
 
     public void testShouldSerializeOWLFacet() {
         service.testOWLFacet(OWLFacet.MIN_INCLUSIVE, new AsyncCallback<OWLFacet>() {
             @Override
             public void onFailure(Throwable throwable) {
                 throwable.printStackTrace();
                 fail(throwable.getMessage());
             }
 
             @Override
             public void onSuccess(OWLFacet out) {
                 assertEquals(OWLFacet.MIN_INCLUSIVE, out);
                 finishTest();
             }
         });
     }
 }
