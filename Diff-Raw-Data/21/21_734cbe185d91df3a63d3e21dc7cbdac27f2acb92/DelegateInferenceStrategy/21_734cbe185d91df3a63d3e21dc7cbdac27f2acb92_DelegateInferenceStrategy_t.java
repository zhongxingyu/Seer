 /*
  * Copyright 2011 JBoss Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.drools.semantics.builder.model.inference;
 
 import org.apache.commons.collections15.map.MultiKeyMap;
 import org.drools.io.Resource;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.drools.semantics.builder.DLUtils;
 import org.drools.semantics.builder.model.Concept;
 import org.drools.semantics.builder.model.OntoModel;
 import org.drools.semantics.builder.model.PropertyRelation;
 import org.drools.semantics.builder.model.SubConceptOf;
 import org.semanticweb.HermiT.Reasoner;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.util.*;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.*;
 
 public class DelegateInferenceStrategy extends AbstractModelInferenceStrategy {
 
     public static int inferredDomains = 0;
     public static int minCounter = 0;
     public static int maxCounter = 0;
 
     private InferredOntologyGenerator reasoner;
 
     private Map<String, Concept> cache = new HashMap<String, Concept>();
     private Map<String, String> props = new HashMap<String, String>();
     private Map<OWLProperty, Set<OWLClassExpression> > domains = new HashMap<OWLProperty, Set<OWLClassExpression>>();
     private Map<OWLProperty, Set<OWLClassExpression> > ranges = new HashMap<OWLProperty, Set<OWLClassExpression>>();
     private Map<OWLProperty, Set<OWLDataRange> > dataRanges = new HashMap<OWLProperty, Set<OWLDataRange>>();
 
     private MultiKeyMap minCards = new MultiKeyMap();
     private MultiKeyMap maxCards = new MultiKeyMap();
 
     private static void register( String prim, String klass ) {
         IRI i1 = IRI.create( prim );
         Concept con = new Concept( i1.toQuotedString(), klass );
         con.setPrimitive(true);
         primitives.put( i1.toQuotedString(), con );
     }
 
     private static Map<String, Concept> primitives = new HashMap<String, Concept>();
 
     {
 //        register( "http://www.w3.org/2001/XMLSchema#string", String.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#dateTime", Date.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#date", Date.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#time", Date.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#int", int.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#integer", BigInteger.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#long", long.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#float", float.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#double", double.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#short", short.class );
 //
 //        register( "http://www.w3.org/2000/01/rdf-schema#Literal", Object.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#boolean", boolean.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#decimal", BigDecimal.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#byte", byte.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#unsignedByte", short.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#unsignedShort", int.class );
 //
 //        register( "http://www.w3.org/2001/XMLSchema#unsignedInt", long.class );
 
         register( "http://www.w3.org/2001/XMLSchema#string", "xsd:string" );
 
         register( "http://www.w3.org/2001/XMLSchema#dateTime", "xsd:dateTime" );
 
         register( "http://www.w3.org/2001/XMLSchema#date", "xsd:date" );
 
         register( "http://www.w3.org/2001/XMLSchema#time", "xsd:time" );
 
         register( "http://www.w3.org/2001/XMLSchema#int", "xsd:int" );
 
         register( "http://www.w3.org/2001/XMLSchema#integer", "xsd:integer" );
 
         register( "http://www.w3.org/2001/XMLSchema#long", "xsd:long" );
 
         register( "http://www.w3.org/2001/XMLSchema#float", "xsd:float" );
 
         register( "http://www.w3.org/2001/XMLSchema#double", "xsd:double" );
 
         register( "http://www.w3.org/2001/XMLSchema#short", "xsd:short" );
 
         register( "http://www.w3.org/2000/01/rdf-schema#Literal", "xsd:anySimpleType" );
 
         register( "http://www.w3.org/2001/XMLSchema#boolean", "xsd:boolean" );
 
         register( "http://www.w3.org/2001/XMLSchema#decimal", "xsd:decimal" );
 
         register( "http://www.w3.org/2001/XMLSchema#byte", "xsd:byte" );
 
         register( "http://www.w3.org/2001/XMLSchema#unsignedByte", "xsd:unsignedByte" );
 
         register( "http://www.w3.org/2001/XMLSchema#unsignedShort", "xsd:unsignedShort" );
 
         register( "http://www.w3.org/2001/XMLSchema#unsignedInt", "xsd:unsignedInt" );
     }
 
 
     @Override
     protected OntoModel buildProperties( OWLOntology ontoDescr, StatefulKnowledgeSession kSession, Map<InferenceTask, Resource> theory, OntoModel hierachicalModel ) {
 
         boolean dirty = false;
         OWLDataFactory factory = ontoDescr.getOWLOntologyManager().getOWLDataFactory();
 
 
         for ( OWLDataProperty dp : ontoDescr.getDataPropertiesInSignature() ) {
             String propIri = dp.getIRI().toQuotedString();
             String propName = DLUtils.getInstance().buildLowCaseNameFromIri( dp.getIRI() );
             props.put( propIri, propName );
         }
 
         for ( OWLObjectProperty op : ontoDescr.getObjectPropertiesInSignature() ) {
             String propIri = op.getIRI().toQuotedString();
             String propName = DLUtils.getInstance().buildLowCaseNameFromIri( op.getIRI() );
             props.put( propIri, propName );
         }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
         // infer domain / range from quantified restrictions...
         for ( OWLClass klass : ontoDescr.getClassesInSignature() ) {
             for ( OWLClassExpression clax : klass.getSuperClasses( ontoDescr ) ) {
                 clax = clax.getNNF();
 
                 final OWLClass inKlass = klass;
                 final OWLDataFactory fac = factory;
                 clax.accept( new OWLClassExpressionVisitor() {
 
 
 
                     private void process( OWLClassExpression expr ) {
                         if ( expr instanceof OWLNaryBooleanClassExpression ) {
                             for (OWLClassExpression clax : ((OWLNaryBooleanClassExpression) expr).getOperandsAsList() ) {
                                 process( clax );
                             }
                         } else if ( expr instanceof OWLQuantifiedObjectRestriction ) {
                             OWLQuantifiedObjectRestriction rest = (OWLQuantifiedObjectRestriction) expr;
                             OWLObjectProperty prop = rest.getProperty().asOWLObjectProperty();
                             addDomain( domains, prop, inKlass, fac );
                             inferredDomains++;
                             OWLClassExpression fil = rest.getFiller();
                             if ( fil instanceof OWLObjectComplementOf ) {
                                 fil = ((OWLObjectComplementOf) fil ).getOperand();
                             }
                             try {
                                 addRange( ranges, prop, rest.getFiller().asOWLClass(), fac );
                             } catch (OWLRuntimeException ore) {
                                 System.err.println( "ORE --- " + rest.getFiller() );
                             }
                             process( fil );
                         } else if ( expr instanceof  OWLQuantifiedDataRestriction ) {
                             OWLQuantifiedDataRestriction rest = (OWLQuantifiedDataRestriction) expr;
                             addDomain(domains, ((OWLQuantifiedDataRestriction) expr).getProperty().asOWLDataProperty(), inKlass, fac);
                             inferredDomains++;
                             OWLDataRange fil = rest.getFiller();
                             if ( fil instanceof OWLDataComplementOf ) {
                                 fil = ((OWLDataComplementOf) rest.getFiller()).getDataRange();
                             }
                             if ( fil instanceof OWLDataOneOf ) {
                                 return;
                             }
                             addDataRange(dataRanges, ((OWLQuantifiedDataRestriction) expr).getProperty().asOWLDataProperty(), fil.asOWLDatatype(), fac);
                         } else if ( expr instanceof OWLCardinalityRestriction ) {
                             if ( expr instanceof OWLDataMinCardinality ) {
                                 minCards.put(inKlass.getIRI().toQuotedString(),
                                         ((OWLDataMinCardinality) expr).getProperty().asOWLDataProperty().getIRI().toQuotedString(),
                                         ((OWLDataMinCardinality) expr).getCardinality());
                                 minCounter++;
                             } else if ( expr instanceof OWLDataMaxCardinality ) {
                                 maxCards.put( inKlass.getIRI().toQuotedString(),
                                         ((OWLDataMaxCardinality) expr).getProperty().asOWLDataProperty().getIRI().toQuotedString(),
                                         ((OWLDataMaxCardinality) expr).getCardinality() );
                                 maxCounter++;
                             } else if ( expr instanceof OWLObjectMaxCardinality ) {
                                 maxCards.put( inKlass.getIRI().toQuotedString(),
                                         ((OWLObjectMaxCardinality) expr).getProperty().asOWLObjectProperty().getIRI().toQuotedString(),
                                         ((OWLObjectMaxCardinality) expr).getCardinality() );
                                 maxCounter++;
                             } else if ( expr instanceof OWLObjectMinCardinality ) {
                                 minCards.put( inKlass.getIRI().toQuotedString(),
                                         ((OWLObjectMinCardinality) expr).getProperty().asOWLObjectProperty().getIRI().toQuotedString(),
                                         ((OWLObjectMinCardinality) expr).getCardinality() );
                                 minCounter++;
                             }
                             System.out.println( expr );
                         }
                         else return;
                     }
 
                     public void visit(OWLClass ce) {
                         process( ce );
                     }
 
                     public void visit(OWLObjectIntersectionOf ce) {
                         process( ce );
                     }
 
                     public void visit(OWLObjectUnionOf ce) {
                         process( ce );
                     }
 
                     public void visit(OWLObjectComplementOf ce) {
                         process( ce );
                     }
 
                     public void visit(OWLObjectSomeValuesFrom ce) {
                         process(ce);
                     }
 
                     public void visit(OWLObjectAllValuesFrom ce) {
                         process(ce);
                     }
 
                     public void visit(OWLObjectHasValue ce) {
                         process(ce);
                     }
 
                     public void visit(OWLObjectMinCardinality ce) {
 //                        minCards.put(inKlass, ce.getProperty().asOWLObjectProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        minCounter++;
                         process(ce);
                     }
 
                     public void visit(OWLObjectExactCardinality ce) {
 //                        maxCards.put(inKlass, ce.getProperty().asOWLObjectProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        minCards.put(inKlass, ce.getProperty().asOWLObjectProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        minCounter++;
 //                        maxCounter++;
                         process(ce);
                     }
 
                     public void visit(OWLObjectMaxCardinality ce) {
 //                        maxCards.put(inKlass, ce.getProperty().asOWLObjectProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        maxCounter++;
                         process(ce);
                     }
 
                     public void visit(OWLObjectHasSelf ce) {
                         throw new UnsupportedOperationException();
                     }
 
                     public void visit(OWLObjectOneOf ce) {
                         throw new UnsupportedOperationException();
                     }
 
                     public void visit(OWLDataSomeValuesFrom ce) {
                         process(ce);
                     }
 
                     public void visit(OWLDataAllValuesFrom ce) {
                         process(ce);
                     }
 
                     public void visit(OWLDataHasValue ce) {
                         process(ce);
                     }
 
                     public void visit(OWLDataMinCardinality ce) {
 //                        minCards.put(inKlass, ce.getProperty().asOWLDataProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        minCounter++;
                         process(ce);
                     }
 
                     public void visit(OWLDataExactCardinality ce) {
 //                        minCards.put(inKlass, ce.getProperty().asOWLDataProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        maxCards.put(inKlass, ce.getProperty().asOWLDataProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        maxCounter++;
 //                        minCounter++;
                         process(ce);
                     }
 
                     public void visit(OWLDataMaxCardinality ce) {
 //                        maxCards.put(inKlass, ce.getProperty().asOWLDataProperty().getIRI().toQuotedString(), ce.getCardinality());
 //                        maxCounter++;
                         process(ce);
                     }
                 });
 
             }
         }
 
 
 
 
         for ( OWLProperty prop : domains.keySet() ) {
             if ( domains.get( prop ).size() > 1 ) {
                 dirty = true;
                 OWLClass dom = factory.getOWLClass(
                         IRI.create(DLUtils.getInstance().buildNameFromIri(prop.getIRI().toQuotedString() + "Domain")))  ;
                 ontoDescr.getOWLOntologyManager().applyChange(
                         new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( dom ) )
                 );
                 ontoDescr.getOWLOntologyManager().applyChange(
                         new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( dom, factory.getOWLObjectUnionOf( domains.get( prop ) ) ) )
                 );
             }
         }
         for ( OWLProperty prop : ranges.keySet() ) {
             if ( ranges.get( prop ).size() > 1 ) {
                 dirty = true;
                 OWLClass dom = factory.getOWLClass(
                         IRI.create(DLUtils.getInstance().buildNameFromIri(prop.getIRI().toQuotedString() + "Range")))  ;
                 ontoDescr.getOWLOntologyManager().applyChange(
                         new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( dom ) )
                 );
                 ontoDescr.getOWLOntologyManager().applyChange(
                         new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( dom, factory.getOWLObjectUnionOf( ranges.get( prop ) ) ) )
                 );
             }
         }
         for ( OWLProperty prop : dataRanges.keySet() ) {
             if ( dataRanges.get( prop ).size() > 1 ) {
                 dirty = true;
                 OWLDatatype dom = factory.getOWLDatatype(
                         IRI.create(DLUtils.getInstance().buildNameFromIri(prop.getIRI().toQuotedString() + "Range")))  ;
                 ontoDescr.getOWLOntologyManager().applyChange(
                         new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( dom ) )
                 );
                 ontoDescr.getOWLOntologyManager().applyChange(
                         new AddAxiom( ontoDescr, factory.getOWLDataPropertyRangeAxiom( prop.asOWLDataProperty(), factory.getOWLDataUnionOf( dataRanges.get( prop ) )
                         ) )
                 );
             }
         }
 
 
 
 //
 
 
 
         for ( OWLProperty prop : domains.keySet() ) {
             if ( prop instanceof OWLObjectProperty ) {
                 OWLObjectProperty oProp = (OWLObjectProperty) prop;
                 for ( OWLClassExpression d : domains.get( prop ) ) {
                     if ( ! ontoDescr.getObjectPropertyDomainAxioms( oProp ).contains( d ) ) {
                         ontoDescr.getOWLOntologyManager().applyChange(
                                 new AddAxiom( ontoDescr, factory.getOWLObjectPropertyDomainAxiom( oProp, d ) )
                         );
                     }
                 }
 
             } else if ( prop instanceof OWLDataProperty ) {
                 OWLDataProperty dProp = (OWLDataProperty) prop;
                 for ( OWLClassExpression d : domains.get( prop ) ) {
                     if ( ! ontoDescr.getDataPropertyDomainAxioms( dProp ).contains( d ) ) {
                         ontoDescr.getOWLOntologyManager().applyChange(
                                 new AddAxiom( ontoDescr, factory.getOWLDataPropertyDomainAxiom( dProp, d ) )
                         );
                     }
                 }
 
             }
         }
 
         for ( OWLProperty prop : ranges.keySet() ) {
             if ( prop instanceof OWLObjectProperty ) {
                 OWLObjectProperty oProp = (OWLObjectProperty) prop;
                 for ( OWLClassExpression r : ranges.get( prop ) ) {
                     if ( ! ontoDescr.getObjectPropertyRangeAxioms( oProp ).contains( r ) ) {
                         ontoDescr.getOWLOntologyManager().applyChange(
                                 new AddAxiom( ontoDescr, factory.getOWLObjectPropertyRangeAxiom( oProp, r ) )
                         );
                     }
                 }
 
             }
         }
 
         for ( OWLProperty prop : dataRanges.keySet() ) {
             if ( prop instanceof OWLDataProperty ) {
                 OWLDataProperty dProp = (OWLDataProperty) prop;
                 for ( OWLDataRange r : dataRanges.get( prop ) ) {
                     if ( ! ontoDescr.getDataPropertyRangeAxioms( dProp ).contains( r ) ) {
                         ontoDescr.getOWLOntologyManager().applyChange(
                                 new AddAxiom( ontoDescr, factory.getOWLDataPropertyRangeAxiom( dProp, r ) )
                         );
                     }
                 }
 
             }
         }
 
 
         for ( OWLDataProperty dProp : ontoDescr.getDataPropertiesInSignature() ) {
             for ( OWLDataPropertyDomainAxiom dom : ontoDescr.getDataPropertyDomainAxioms( dProp ) ) {
                 if ( domains.get( dProp ) == null || ! domains.get( dProp ).contains( dom.getDomain() ) ) {
                     addDomain( domains, dProp, dom.getDomain(), factory );
                 }
             }
 
             for ( OWLDataPropertyRangeAxiom ran : ontoDescr.getDataPropertyRangeAxioms( dProp ) ) {
                 if ( dataRanges.get( dProp ) == null || ! dataRanges.get( dProp ).contains( ran.getRange() ) ) {
                     addDataRange(dataRanges, dProp, ran.getRange(), factory);
                 }
             }
         }
 
         for ( OWLObjectProperty oProp : ontoDescr.getObjectPropertiesInSignature() ) {
             for ( OWLObjectPropertyDomainAxiom dom : ontoDescr.getObjectPropertyDomainAxioms( oProp ) ) {
                 if ( domains.get( oProp ) == null || ! domains.get( oProp ).contains( dom.getDomain() ) ) {
                     addDomain( domains, oProp, dom.getDomain(), factory );
                 }
             }
 
             for ( OWLObjectPropertyRangeAxiom ran : ontoDescr.getObjectPropertyRangeAxioms(oProp) ) {
                 if ( ranges.get( oProp ) == null || ! ranges.get( oProp ).contains( ran.getRange() ) ) {
                     addRange( ranges, oProp, ran.getRange(), factory );
                 }
             }
         }
 
         // set domains and ranges
 //        for ( OWLProperty prop : domains.keySet() ) {
 
 //            OWLClassExpression dom;
 //            if ( domains.get( prop ).size() > 1 ) {
 //                dom = factory.getOWLObjectUnionOf( domains.get( prop ) );
 //            } else {
 //                dom = domains.get( prop ).iterator().next();
 //            }
 //            dirty = true;
 //            if ( prop instanceof OWLObjectPropertyExpression ) {
 //                OWLObjectPropertyExpression oxProp = (OWLObjectPropertyExpression) prop;
 //                ontoDescr.getOWLOntologyManager().applyChange(
 //                        new AddAxiom( ontoDescr, factory.getOWLObjectPropertyDomainAxiom( oxProp, dom ) )
 //                );
 //            } else {
 //                OWLDataPropertyExpression datProp = (OWLDataPropertyExpression) prop;
 //                ontoDescr.getOWLOntologyManager().applyChange(
 //                        new AddAxiom( ontoDescr, factory.getOWLDataPropertyDomainAxiom( datProp, dom ) )
 //                );
 //            }
 //        }
 //
 //
 //        for ( OWLProperty prop : ranges.keySet() ) {
 //            OWLClassExpression ran;
 //            if ( ranges.get( prop ).size() > 1 ) {
 //                ran = factory.getOWLObjectUnionOf( ranges.get( prop ) );
 //            } else {
 //                ran = ranges.get( prop ).iterator().next();
 //            }
 //            dirty = true;
 //            if ( prop instanceof OWLObjectPropertyExpression ) {
 //                OWLObjectPropertyExpression oxProp = (OWLObjectPropertyExpression) prop;
 //                ontoDescr.getOWLOntologyManager().applyChange(
 //                        new AddAxiom( ontoDescr, factory.getOWLObjectPropertyRangeAxiom( oxProp, ran ) )
 //                );
 //            }
 //        }
 //        for ( OWLProperty prop : dataRanges.keySet() ) {
 //            OWLDataRange ran = dataRanges.get( prop ).iterator().next();;
 //
 //            if ( prop instanceof OWLDataPropertyExpression ) {
 //                OWLDataPropertyExpression dataProp = (OWLDataPropertyExpression) prop;
 //                ontoDescr.getOWLOntologyManager().applyChange(
 //                        new AddAxiom( ontoDescr, factory.getOWLDataPropertyRangeAxiom( dataProp, ran ) )
 //                );
 //            }
 //        }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
         // Handle anonymous domains and ranges
         for ( OWLDataProperty dp : ontoDescr.getDataPropertiesInSignature() ) {
             String propIri = dp.getIRI().toQuotedString();
             String propName = DLUtils.getInstance().buildLowCaseNameFromIri( dp.getIRI() );
             String typeName = DLUtils.getInstance().buildNameFromIri( dp.getIRI() );
 
             for (OWLClassExpression dom : dp.getDomains( ontoDescr )) {
                 for (OWLDataRange ran : dp.getRanges( ontoDescr )) {
                     String domainIri = dom.isAnonymous() ? "" : dom.asOWLClass().getIRI().toQuotedString();
                     if ( dom.isAnonymous() ) {
                         OWLClass domain = factory.getOWLClass(IRI.create( typeName + "Domain") );
                         domainIri = domain.getIRI().toQuotedString();
                         ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( domain ) ) );
                         ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom(ontoDescr, factory.getOWLEquivalentClassesAxiom(domain, dom)));
                         dirty = true;
                     }
 
 //                    hierachicalModel.addProperty( new PropertyRelation( domainIri, propIri, ran.asOWLDatatype().getIRI().toQuotedString(), propName ) );
                     addDomain(domains, dp, dom, factory);
                 }
             }
 
         }
 
 
         for ( OWLObjectProperty op : ontoDescr.getObjectPropertiesInSignature() ) {
             String propIri = op.getIRI().toQuotedString();
             String propName = DLUtils.getInstance().buildLowCaseNameFromIri( op.getIRI() );
             String typeName = DLUtils.getInstance().buildNameFromIri( op.getIRI() );
 
 
             for (OWLClassExpression dom : op.getDomains( ontoDescr )) {
                 for (OWLClassExpression ran : op.getRanges( ontoDescr )) {
                     String domainIri = dom.isAnonymous() ? "" : dom.asOWLClass().getIRI().toQuotedString();
                     String rangeIri = ran.isAnonymous() ? "" : ran.asOWLClass().getIRI().toQuotedString();
 
                     if ( dom.isAnonymous() ) {
                         OWLClass domain = factory.getOWLClass(IRI.create( typeName + "Domain") );
                         domainIri = domain.getIRI().toQuotedString();
                         ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( domain ) ) );
                         ontoDescr.getOWLOntologyManager().applyChange(new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( domain, dom ) ) );
                         dirty = true;
                     }
 
                     if ( ran.isAnonymous() ) {
                         OWLClass range = factory.getOWLClass(IRI.create( typeName + "Range") );
                         rangeIri = range.getIRI().toQuotedString();
                         ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLDeclarationAxiom( range ) ) );
                         ontoDescr.getOWLOntologyManager().applyChange(new AddAxiom( ontoDescr, factory.getOWLEquivalentClassesAxiom( range, ran ) ) );
                         dirty = true;
                     }
 
 //                    hierachicalModel.addProperty( new PropertyRelation( domainIri, propIri, rangeIri, propName ) );
                     addDomain( domains, op, dom, factory );
 
                 }
             }
 
         }
 
 
 
 
 
 
 
 
 
 
 
         // Complete missing domains and ranges for properties. Might be overridden later, if anything can be inferred
         int missDomain = 0;
         int missDataRange = 0;
         int missObjRange = 0;
         for ( OWLDataProperty dp : ontoDescr.getDataPropertiesInSignature() ) {
             if ( ! dp.isOWLTopDataProperty() && dp.getDomains( ontoDescr ).isEmpty() ) {
                 ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLDataPropertyDomainAxiom( dp, factory.getOWLThing() ) ) );
                 addDomain( domains, dp, factory.getOWLThing(), factory );
                 System.err.println( "Added missing domain for" + dp);
                 missDomain++;
             }
         }
 
         for ( OWLObjectProperty op : ontoDescr.getObjectPropertiesInSignature() ) {
             if ( ! op.isOWLTopObjectProperty() && op.getRanges( ontoDescr ).isEmpty() ) {
                 ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLObjectPropertyRangeAxiom( op, factory.getOWLThing() ) ) );
                 addRange( ranges, op, factory.getOWLThing(), factory );
                 System.err.println( "Added missing range for" + op);
                 missObjRange++;
             }
         }
 
         for ( OWLDataProperty dp : ontoDescr.getDataPropertiesInSignature() ) {
             if ( ! dp.isOWLTopDataProperty() && dp.getRanges( ontoDescr ).isEmpty() ) {
                 ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLDataPropertyRangeAxiom( dp, factory.getTopDatatype() ) ) );
                 addDataRange(dataRanges, dp, factory.getTopDatatype(), factory );
                 System.err.println( "Added missing dataRange for" + dp);
                 missDataRange++;
             }
         }
 
         for ( OWLObjectProperty op : ontoDescr.getObjectPropertiesInSignature() ) {
             if ( ! op.isOWLTopObjectProperty() && op.getDomains( ontoDescr ).isEmpty() ) {
                 ontoDescr.getOWLOntologyManager().applyChange( new AddAxiom( ontoDescr, factory.getOWLObjectPropertyDomainAxiom( op, factory.getOWLThing() ) ) );
                 addDomain( domains, op, factory.getOWLThing(), factory );
                 System.err.println( "Added missing domain for" + op);
                 missDomain++;
             }
         }
 
         System.err.println("Misses : ");
         System.err.println(inferredDomains);
         System.err.println(missDomain);
         System.err.println(missDataRange);
         System.err.println(missObjRange);
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 //        if ( dirty ) {
 //            initReasoner( kSession, ontoDescr );
 //            reasoner.fillOntology( ontoDescr.getOWLOntologyManager(), ontoDescr );
 //        }
 
 
         addConceptsToModel( kSession, ontoDescr, hierachicalModel );
 
         addSubConceptsToModel(kSession, ontoDescr, hierachicalModel);
 
 
         for ( OWLObjectProperty op : ontoDescr.getObjectPropertiesInSignature() ) {
             if ( ! op.isTopEntity() ) {
 //                PropertyRelation prop = hierachicalModel.getProperty( op.getIRI().toQuotedString() );
 //                Concept con = cache.get( prop.getSubject() );
 //                con.getProperties().put( prop, cache.get( prop.getObject() ) );
                 String propIri = op.getIRI().toQuotedString();
                 String propName = props.get( propIri );
                 for ( OWLClassExpression dom : domains.get( op ) ) {
                     for ( OWLClassExpression ran : ranges.get( op ) ) {
                         PropertyRelation rel = new PropertyRelation( dom.asOWLClass().getIRI().toQuotedString(),
                                 propIri,
                                 ran.asOWLClass().getIRI().toQuotedString(),
                                 propName );
 
                         Concept con = cache.get( rel.getSubject() );
 
                         Integer min = (Integer) minCards.get( rel.getSubject(), propIri );
                         if ( min != null ) {
                             rel.setMinCard( min );
                         } else {
                            rel.setMinCard( 0 );
                         }
                         Integer max = (Integer) maxCards.get( rel.getSubject(), propIri );
                         if ( max != null ) {
                             rel.setMaxCard( max );
                         }
                         con.getProperties().put( rel, cache.get( rel.getObject() ) );
                         hierachicalModel.addProperty( rel );
                     }
                 }
             }
         }
 
         for ( OWLDataProperty dp : ontoDescr.getDataPropertiesInSignature() ) {
             if ( ! dp.isTopEntity() ) {
 //                PropertyRelation prop = hierachicalModel.getProperty( dp.getIRI().toQuotedString() );
 //                Concept con = cache.get( prop.getSubject() );
 //                con.getProperties().put( prop, primitives.get( prop.getObject() ) );
 //            }
                 String propIri = dp.getIRI().toQuotedString();
                 String propName = props.get( propIri );
                 for ( OWLClassExpression dom : domains.get( dp ) ) {
                     for ( OWLDataRange ran : dataRanges.get( dp ) ) {
 
                         PropertyRelation rel = new PropertyRelation( dom.asOWLClass().getIRI().toQuotedString(),
                                 propIri,
                                 ran.asOWLDatatype().getIRI().toQuotedString(),
                                 propName );
 
                         Concept con = cache.get( rel.getSubject() );
 
                         Integer min = (Integer) minCards.get( rel.getSubject(), propIri );
                         if ( min != null ) {
                             rel.setMinCard( min );
                         }  else {
                            rel.setMinCard( 0 );
                         }
                         Integer max = (Integer) maxCards.get( rel.getSubject(), propIri );
                         if ( max != null ) {
                             rel.setMaxCard(max);
                         }
 
 //                        later mapping
 //                        con.getProperties().put( rel, primitives.get( rel.getObject() ) );
                         con.getProperties().put( rel, primitives.get( rel.getObject() ) );
                         hierachicalModel.addProperty( rel );
                     }
                 }
             }
         }
 
         kSession.fireAllRules();
 
         return hierachicalModel;
     }
 
     private OWLProperty lookupDataProperty(String propId, Set<OWLDataProperty> set ) {
         for (OWLDataProperty prop : set ) {
             if ( prop.getIRI().toQuotedString().equals( propId ) ) {
                 return prop;
             }
         }
         return null;
     }
 
     private OWLProperty lookupObjectProperty(String propId, Set<OWLObjectProperty> set ) {
         for (OWLObjectProperty prop : set ) {
             if ( prop.getIRI().toQuotedString().equals( propId ) ) {
                 return prop;
             }
         }
         return null;
     }
 
 
     @Override
     protected OntoModel buildClassLattice(OWLOntology ontoDescr, StatefulKnowledgeSession kSession, Map<InferenceTask, Resource> theory, OntoModel baseModel) {
 //        initReasoner( kSession, ontoDescr );
 //        reasoner.fillOntology( ontoDescr.getOWLOntologyManager(), ontoDescr );
 
 
         addConceptsToModel( kSession, ontoDescr, baseModel );
 
         addSubConceptsToModel( kSession, ontoDescr, baseModel );
 
 
         kSession.fireAllRules();
 
 
         return baseModel;
     }
 
 
 
 
 
     private void addSubConceptsToModel(StatefulKnowledgeSession kSession, OWLOntology ontoDescr, OntoModel model) {
         for ( OWLClass con : ontoDescr.getClassesInSignature() ) {
             Concept concept = cache.get( con.getIRI().toQuotedString() );
 
             int namedSupers = 0;
             for ( OWLSubClassOfAxiom sc : ontoDescr.getSubClassAxiomsForSubClass( con ) ) {
                 if ( ! sc.getSuperClass().isAnonymous() ) {
                     namedSupers++;
                     SubConceptOf subcon = new SubConceptOf( sc.getSubClass().asOWLClass().getIRI().toQuotedString(), sc.getSuperClass().asOWLClass().getIRI().toQuotedString() );
                     if ( model.getSubConceptOf( subcon.getSubject(), subcon.getObject() ) == null ) {
                         concept.getSuperConcepts().add( cache.get( subcon.getObject() ) );
                         model.addSubConceptOf( subcon );
                         kSession.insert( subcon );
                     }
                 }
             }
             if ( namedSupers == 0 && ! concept.getName().equals("Thing")) {
                 SubConceptOf subcon = new SubConceptOf( con.getIRI().toQuotedString(), ontoDescr.getOWLOntologyManager().getOWLDataFactory().getOWLThing().getIRI().toQuotedString() );
                     if ( model.getSubConceptOf( subcon.getSubject(), subcon.getObject() ) == null ) {
                         concept.getSuperConcepts().add( cache.get( subcon.getObject() ) );
                         model.addSubConceptOf( subcon );
                         kSession.insert( subcon );
                     }
             }
         }
     }
 
 
     private void addConceptsToModel(StatefulKnowledgeSession kSession, OWLOntology ontoDescr, OntoModel baseModel) {
         for ( OWLClass con : ontoDescr.getClassesInSignature() ) {
             if ( baseModel.getConcept( con.getIRI().toQuotedString()) == null ) {
                 Concept concept =  new Concept(
                         con.getIRI().toQuotedString(),
                         DLUtils.getInstance().buildNameFromIri( con.getIRI() ) );
                 baseModel.addConcept( concept );
                 kSession.insert( concept );
                 cache.put( con.getIRI().toQuotedString(), concept );
                 System.out.println(" ADD concept " + con.getIRI() );
             }
         }
     }
 
 
     private void addDataRange(Map<OWLProperty, Set<OWLDataRange>> dataRanges, OWLDataProperty dp, OWLDataRange owlData, OWLDataFactory factory ) {
         Set<OWLDataRange> set = dataRanges.get( dp );
         if ( set == null ) {
             set = new HashSet<OWLDataRange>();
             dataRanges.put( dp, set );
         }
         set.add( owlData );
         if ( set.size() >= 2 && set.contains( factory.getTopDatatype() ) ) {
             set.remove( factory.getTopDatatype() );
         }
     }
 
     private void addRange(Map<OWLProperty, Set<OWLClassExpression>> ranges, OWLProperty rp, OWLClassExpression owlKlass, OWLDataFactory factory ) {
         Set<OWLClassExpression> set = ranges.get( rp );
         if ( set == null ) {
             set = new HashSet<OWLClassExpression>();
             ranges.put( rp, set );
         }
         set.add( owlKlass );
         if ( set.size() >= 2 && set.contains( factory.getOWLThing() ) ) {
             set.remove( factory.getOWLThing() );
         }
 
     }
 
     private void addDomain(Map<OWLProperty, Set<OWLClassExpression>> domains, OWLProperty dp, OWLClassExpression owlKlass, OWLDataFactory factory) {
         Set<OWLClassExpression> set = domains.get( dp );
         if ( set == null ) {
             set = new HashSet<OWLClassExpression>();
             domains.put( dp, set );
         }
 
         set.add( owlKlass );
         if ( set.size() >= 2 && set.contains( factory.getOWLThing() ) ) {
             set.remove( factory.getOWLThing() );
         }
     }
 
 
     protected void initReasoner( StatefulKnowledgeSession kSession, OWLOntology ontoDescr ) {
         Reasoner hermit = new Reasoner( ontoDescr );
         reasoner = new InferredOntologyGenerator( hermit );
         reasoner.addGenerator( new InferredSubClassAxiomGenerator() );
         reasoner.addGenerator( new InferredEquivalentClassAxiomGenerator() );
         reasoner.addGenerator( new InferredClassAssertionAxiomGenerator() );
         reasoner.addGenerator( new InferredSubObjectPropertyAxiomGenerator() );
         reasoner.addGenerator( new InferredObjectPropertyCharacteristicAxiomGenerator() );
         reasoner.addGenerator( new InferredPropertyAssertionGenerator() );
     }
 
 
 }
