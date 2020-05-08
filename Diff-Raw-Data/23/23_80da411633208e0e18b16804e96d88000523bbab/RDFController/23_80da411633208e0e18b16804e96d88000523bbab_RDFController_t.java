 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.wordpress.erenha.arjuna.jauza.rdf;
 
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFProperty;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFClass;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFContext;
 import com.wordpress.erenha.arjuna.jauza.controller.MainController;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFIndividual;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFIndividualProperty;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFNamespace;
 import com.wordpress.erenha.arjuna.jauza.rdf.model.RDFOntology;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.collections.ObservableList;
 import org.openrdf.model.Graph;
 import org.openrdf.model.Literal;
 import org.openrdf.model.Namespace;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.TreeModel;
 import org.openrdf.model.impl.URIImpl;
 import org.openrdf.model.util.GraphUtil;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.query.BindingSet;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.TupleQuery;
 import org.openrdf.query.TupleQueryResult;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.config.RepositoryConfig;
 import org.openrdf.repository.config.RepositoryConfigException;
 import org.openrdf.repository.http.HTTPRepository;
 import org.openrdf.repository.manager.RemoteRepositoryManager;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.repository.sail.config.SailRepositoryConfig;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.sail.config.SailImplConfig;
 import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
 import org.openrdf.sail.inferencer.fc.config.ForwardChainingRDFSInferencerConfig;
 import org.openrdf.sail.nativerdf.NativeStore;
 import org.openrdf.sail.nativerdf.config.NativeStoreConfig;
 
 /**
  *
  * @author Hindarwan
  */
 public class RDFController {
 
     private Repository repo;
     private MainController mainController;
 
     public void setMainController(MainController mainController) {
         this.mainController = mainController;
     }
 
     //for native stor repo
     public void initRepository(String data) {
         try {
             File dataDir = new File(data);
             if (dataDir.exists()) {
                 File[] listFiles = dataDir.listFiles();
                 for (File file : listFiles) {
                     file.delete();
                 }
             }
             String indexes = "spoc,posc,cosp";
             repo = new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(dataDir, indexes)));
             repo.initialize();
         } catch (RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void initRepository(String sesameServer, String repositoryId) {
         try {
             RemoteRepositoryManager manager = new RemoteRepositoryManager(sesameServer);
             manager.initialize();
 //            String repositoryId = "jauzafx-db";
             SailImplConfig config = new ForwardChainingRDFSInferencerConfig(new NativeStoreConfig("spoc,posc,cosp"));
             SailRepositoryConfig repositoryTypeSpec = new SailRepositoryConfig(config);
 
             RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
             manager.addRepositoryConfig(repConfig);
 
             repo = manager.getRepository(repositoryId);
 //            repo = new HTTPRepository(sesameServer, repositoryID);
 
             repo.initialize();
         } catch (RepositoryException | RepositoryConfigException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void add(File file) {
         try {
             RepositoryConnection connection = repo.getConnection();
             ValueFactory factory = repo.getValueFactory();
             URI context = factory.createURI(file.toURI().toString());
             RDFFormat format = RDFFormat.forFileName(file.toString());
             try {
                 connection.begin();
                 connection.add(file, null, format, context);
                 connection.commit();
             } catch (RepositoryException re) {
                 connection.rollback();
             } finally {
                 connection.close();
             }
         } catch (IOException | RDFParseException | RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
         getContext();
     }
 
     public void add(URL url) {
         try {
             RepositoryConnection connection = repo.getConnection();
             ValueFactory factory = repo.getValueFactory();
             URI context = factory.createURI(url.toString());
             RDFFormat format = RDFFormat.forFileName(url.toString());
             try {
                 connection.begin();
                 connection.add(url, null, format, context);
                 connection.commit();
             } catch (RepositoryException re) {
                 connection.rollback();
             } finally {
                 connection.close();
             }
         } catch (IOException | RDFParseException | RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
         getContext();
     }
 
     public void getContext() {
         try {
             mainController.getCurrentContext().clear();
             RepositoryConnection connection = repo.getConnection();
             try {
                 RepositoryResult<Resource> contextIDs = connection.getContextIDs();
                 while (contextIDs.hasNext()) {
                     URI resource = (URI) contextIDs.next();
                     mainController.getCurrentContext().add(new RDFContext(resource.toString(), resource.getLocalName()));
                 }
             } finally {
                 connection.close();
             }
 
         } catch (RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void addNamespace(String prefix, String ns) {
         try {
             RepositoryConnection connection = repo.getConnection();
             try {
                 connection.setNamespace(prefix, ns);
             } finally {
                 connection.close();
             }
         } catch (RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
         getNamespaces();
     }
 
     public void getNamespaces() {
         try {
             RepositoryConnection connection = repo.getConnection();
             try {
                 RepositoryResult<Namespace> namespaces = connection.getNamespaces();
                 while (namespaces.hasNext()) {
                     Namespace ns = namespaces.next();
                     mainController.getCurrentNamespaces().add(new RDFNamespace(ns.getName(), ns.getPrefix()));
                 }
             } finally {
                 connection.close();
             }
         } catch (RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public String getNamespaces(String prefix) {
         try {
             RepositoryConnection connection = repo.getConnection();
             try {
                 return connection.getNamespace(prefix);
             } finally {
                 connection.close();
             }
         } catch (RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
             return "";
         }
     }
 
     public void getOntologies() {
         try {
             mainController.getCurrentOntologies().clear();
             RepositoryConnection connection = repo.getConnection();
             String query = "PREFIX dc:<http://purl.org/dc/elements/1.1/> "
                     + "SELECT DISTINCT ?c ?cLabel\n"
                     + "WHERE\n"
                     + "{\n"
                     + "?c rdf:type owl:Ontology.\n"
                     + "?c rdfs:label|dc:title ?cLabel.\n"
                     + "}";
             try {
                 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                 TupleQueryResult result = tupleQuery.evaluate();
                 List<String> bindingNames = result.getBindingNames();
                 while (result.hasNext()) {
                     BindingSet bindingSet = result.next();
                     Value uri = bindingSet.getValue(bindingNames.get(0));
                     Value label = bindingSet.getValue(bindingNames.get(1));
                     mainController.getCurrentOntologies().add(new RDFOntology(uri.stringValue(), label.stringValue()));
                 }
             } finally {
                 connection.close();
             }
 
         } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void getClasses() {
         try {
             mainController.getCurrentClassesLabel().clear();
             RepositoryConnection connection = repo.getConnection();
             String query = "SELECT DISTINCT ?c\n"
                     + "WHERE\n"
                     + "{\n"
                     + "?c rdf:type rdfs:Class.\n"
                     + "}";
             try {
                 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                 TupleQueryResult result = tupleQuery.evaluate();
                 List<String> bindingNames = result.getBindingNames();
                 while (result.hasNext()) {
                     BindingSet bindingSet = result.next();
                     Value uri = bindingSet.getValue(bindingNames.get(0));
 //                    mainController.getCurrentClasses().add(new RDFClass(uri.stringValue(), label.stringValue()));
                     mainController.getCurrentClassesLabel().add(toNamespacePrefix(uri.stringValue()));
                 }
             } finally {
                 connection.close();
             }
 
         } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void getClassesInNSdefined() {
         mainController.getCurrentClassesLabel().clear();
         ObservableList<RDFNamespace> currentNamespaces = mainController.getCurrentNamespaces();
         for (RDFNamespace ns : currentNamespaces) {
             try {
                 RepositoryConnection connection = repo.getConnection();
                 String query = "SELECT DISTINCT ?c ?cLabel\n"
                         + "WHERE\n"
                         + "{\n"
                         + "?c rdf:type rdfs:Class.\n"
                         //                        + "?c rdfs:label ?cLabel.\n"
                         //                    + "?c rdfs:isDefinedBy <" + ns + ">.\n"
                         //                        + "FILTER(STRSTARTS(STR(?c),\"" + ns.getNamespace() + "\"))"
                         //                        + "}"
                         + "FILTER(STRSTARTS(STR(?c),\"" + ns.getNamespace() + "\")).\n"
                         + "BIND(STRAFTER(STR(?c),\"" + ns.getNamespace() + "\") AS ?cLabel)"
                         + "}";
                 try {
                     TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                     TupleQueryResult result = tupleQuery.evaluate();
                     List<String> bindingNames = result.getBindingNames();
                     while (result.hasNext()) {
                         BindingSet bindingSet = result.next();
                         Value uri = bindingSet.getValue(bindingNames.get(0));
                         Value label = bindingSet.getValue(bindingNames.get(1));
 //                        mainController.getCurrentClasses().add(new RDFClass(uri.stringValue(), label.stringValue()));
 //                        mainController.getCurrentClassesLabel().add(ns.getPrefix() + ":" + label.stringValue());
                         mainController.getCurrentClassesLabel().add(uri.stringValue());
 //                        mainController.getCurrentClassesLabel().add(label.stringValue());
                     }
                 } finally {
                     connection.close();
                 }
 
             } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                 Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public void getClassesByNS(String ns) {
         try {
             mainController.getCurrentClasses().clear();
             RepositoryConnection connection = repo.getConnection();
             String query = "SELECT DISTINCT ?c ?cLabel\n"
                     + "WHERE\n"
                     + "{\n"
                     + "?c rdf:type rdfs:Class.\n"
                     + "?c rdfs:label ?cLabel.\n"
                     //                    + "?c rdfs:isDefinedBy <" + ns + ">.\n"
                     + "FILTER(STRSTARTS(STR(?c),\"" + ns + "\"))"
                     + "}";
             try {
                 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                 TupleQueryResult result = tupleQuery.evaluate();
                 List<String> bindingNames = result.getBindingNames();
                 while (result.hasNext()) {
                     BindingSet bindingSet = result.next();
                     Value uri = bindingSet.getValue(bindingNames.get(0));
                     Value label = bindingSet.getValue(bindingNames.get(1));
                     mainController.getCurrentClasses().add(new RDFClass(uri.stringValue(), label.stringValue()));
 //                    mainController.getCurrentClassesLabel().add(label.stringValue());
                 }
             } finally {
                 connection.close();
             }
 
         } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void getProperties() {
         try {
             mainController.getCurrentProperties().clear();
             RepositoryConnection connection = repo.getConnection();
             String query = "SELECT DISTINCT ?p ?pLabel\n"
                     + "WHERE\n"
                     + "{\n"
                     + "?p rdf:type rdf:Property.\n"
                     + "?p rdfs:label ?pLabel.\n"
                     + "}";
             try {
                 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                 TupleQueryResult result = tupleQuery.evaluate();
                 List<String> bindingNames = result.getBindingNames();
                 while (result.hasNext()) {
                     BindingSet bindingSet = result.next();
                     Value uri = bindingSet.getValue(bindingNames.get(0));
                     Value label = bindingSet.getValue(bindingNames.get(1));
                     mainController.getCurrentProperties().add(new RDFProperty(uri.stringValue(), label.stringValue()));
 //                    mainController.getCurrentPropertiesLabel().add(label.stringValue());
                 }
             } finally {
                 connection.close();
             }
 
         } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void getPropertiesInNSdefined() {
         mainController.getCurrentPropertiesLabel().clear();
         ObservableList<RDFNamespace> currentNamespaces = mainController.getCurrentNamespaces();
         for (RDFNamespace ns : currentNamespaces) {
             try {
                 RepositoryConnection connection = repo.getConnection();
                 String query = "SELECT DISTINCT ?p ?pLabel\n"
                         + "WHERE\n"
                         + "{\n"
                         + "?p rdf:type rdf:Property.\n"
                         //                        + "?p rdfs:label ?pLabel.\n"
                         //                    + "?p rdfs:isDefinedBy <" + ns + ">.\n"
                         + "FILTER(STRSTARTS(STR(?p),\"" + ns.getNamespace() + "\")).\n"
                         + "BIND(STRAFTER(STR(?p),\"" + ns.getNamespace() + "\") AS ?pLabel)"
                         + "}";
                 try {
                     TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                     TupleQueryResult result = tupleQuery.evaluate();
                     List<String> bindingNames = result.getBindingNames();
                     while (result.hasNext()) {
                         BindingSet bindingSet = result.next();
                         Value uri = bindingSet.getValue(bindingNames.get(0));
                         Value label = bindingSet.getValue(bindingNames.get(1));
 //                        mainController.getCurrentClasses().add(new RDFClass(uri.stringValue(), label.stringValue()));
                         mainController.getCurrentPropertiesLabel().add(ns.getPrefix() + ":" + label.stringValue());
 //                        mainController.getCurrentPropertiesLabel().add(label.stringValue());
 //                        mainController.getCurrentPropertiesToShow().add(new RDFProperty(uri.stringValue(), ns.getPrefix() + label.stringValue()));
 
                     }
                 } finally {
                     connection.close();
                 }
 
             } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
                 Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public void getPropertiesByNS(String ns) {
         try {
             mainController.getCurrentProperties().clear();
             RepositoryConnection connection = repo.getConnection();
             String query = "SELECT DISTINCT ?p ?pLabel\n"
                     + "WHERE\n"
                     + "{\n"
                     + "?p rdf:type rdf:Property.\n"
                     + "?p rdfs:label ?pLabel.\n"
                     //                    + "?p rdfs:isDefinedBy <" + ns + ">.\n"
                     + "FILTER(STRSTARTS(STR(?p),\"" + ns + "\"))"
                     + "}";
             try {
                 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                 TupleQueryResult result = tupleQuery.evaluate();
                 List<String> bindingNames = result.getBindingNames();
                 while (result.hasNext()) {
                     BindingSet bindingSet = result.next();
                     Value uri = bindingSet.getValue(bindingNames.get(0));
                     Value label = bindingSet.getValue(bindingNames.get(1));
                     mainController.getCurrentProperties().add(new RDFProperty(uri.stringValue(), label.stringValue()));
 //                    mainController.getCurrentPropertiesLabel().add(label.stringValue());
                 }
             } finally {
                 connection.close();
             }
 
         } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void getPropertiesByClass(String rdfClass) {
         try {
             mainController.getCurrentPropertiesLabel().clear();
             RepositoryConnection connection = repo.getConnection();
             String query = "";
             if (rdfClass.equals("<<Choose Class>>")) {
                 query = "SELECT DISTINCT ?p\n"
                         + "WHERE\n"
                         + "{\n"
                         + "?p rdf:type rdf:Property.\n"
                         + "}";
             } else {
 //                toNamespaceFull(rdfClass);
                 query = "SELECT DISTINCT ?p\n"
                         + "WHERE\n"
                         + "{\n"
                         + "?p rdf:type rdf:Property.\n"
                         + "?p rdfs:domain <" + rdfClass + ">.\n"
                         + "}";
             }
             try {
                 TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
                 TupleQueryResult result = tupleQuery.evaluate();
                 List<String> bindingNames = result.getBindingNames();
                 while (result.hasNext()) {
                     BindingSet bindingSet = result.next();
                     Value uri = bindingSet.getValue(bindingNames.get(0));
 
                     mainController.getCurrentPropertiesLabel().add(toNamespacePrefix(uri.stringValue()));
 //                    mainController.getCurrentPropertiesLabel().add(label.stringValue());
                 }
             } finally {
                 connection.close();
             }
 
         } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void saveAllIndividual() {
         String ns = "http://localhost:8080/resource/";
         ObservableList<RDFIndividual> currentIndividuals = mainController.getCurrentIndividuals();
         ValueFactory f = repo.getValueFactory();
         Graph g = new TreeModel();
         for (RDFIndividual individual : currentIndividuals) {
             URI uriIndividual = f.createURI(ns, individual.getUri());
            URI typeIndividual = f.createURI(toNamespaceFull(individual.getRdfClass().getUri()));
             Statement typeStatement = f.createStatement(uriIndividual, RDF.TYPE, typeIndividual);
             g.add(typeStatement);
             List<RDFIndividualProperty> propertyList = individual.getRdfIndividualProperty();
             for (RDFIndividualProperty property : propertyList) {
                URI uriProperty = f.createURI(toNamespaceFull(property.getRdfProperty().getUri()));
                 Literal valueProperty = f.createLiteral(property.getPropertyValue());
                 Statement propertyStatement = f.createStatement(uriIndividual, uriProperty, valueProperty);
                 g.add(propertyStatement);
             }
         }
         try {
             RepositoryConnection connection = repo.getConnection();
             try {
                 connection.add(g, new URIImpl(ns + "saved_on_" + System.currentTimeMillis()));
                 System.out.println("Individual saved");
             } finally {
                 connection.close();
             }
         } catch (RepositoryException ex) {
             Logger.getLogger(RDFController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public String toNamespacePrefix(String url) {
         for (RDFNamespace ns : mainController.getCurrentNamespaces()) {
             if (url.startsWith(ns.getNamespace())) {
                 return ns.getPrefix() + url.replace(ns.getNamespace(), ":");
             }
         }
         return url;
     }
 
     public String toNamespaceFull(String prefixUrl) {
         try {
             URL url = new URL(prefixUrl);
             return prefixUrl;
         } catch (MalformedURLException ex) {
             try {
                 String[] split = prefixUrl.split(":");
                 String namespaces = getNamespaces(split[0]);
                 if (namespaces == null) {
                     return prefixUrl;
                 }
                 return namespaces + split[1];
             } catch (Exception e) {
                 return prefixUrl;
             }
         }
     }
 }
