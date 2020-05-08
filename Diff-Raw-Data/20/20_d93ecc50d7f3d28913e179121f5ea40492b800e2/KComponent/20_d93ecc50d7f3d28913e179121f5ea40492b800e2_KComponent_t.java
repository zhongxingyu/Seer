 package org.Biotax.Main;
 //Imports de Java
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.List;
 import java.util.Iterator;
 import org.Biotax.Main.*;
 //Imports del Owl Api
 import org.semanticweb.owlapi.io.*;
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.apibinding.*;
 import org.semanticweb.owlapi.reasoner.*;
 
 //Imports del Hermit Reasoner
 import org.semanticweb.HermiT.Reasoner;
 
 
 
 // Developed by Rolilink (https://github.com/Rolilink)
 
 
 
 //Implementacion de un diccionarios de iris 
 //para acceder a los individuos
 class IRIdictionary{
 public IRI documentiri;
 //partes
 public IRI hoja;
 public IRI fruto;
 public IRI flor;
 //estructuras accsesorias
 public IRI semilla;
 public IRI savia;
 public IRI sarcillo;
 public IRI punto;
 public IRI pelo;
 public IRI nervadura;
 public IRI glandula;
 public IRI estipula;
 public IRI espina;
 public IRI escama;
 public IRI olor;
 //color
 public IRI amarilla;
 public IRI anaranjada;
 public IRI blanca;
 public IRI incolora;
 public IRI roja;
 //tipos de frutos
 public IRI alado;
 public IRI baya;
 public IRI capsula;
 public IRI dicarpo;
 public IRI foliculo;
 public IRI legumbre;
 public IRI monocarpos;
 public IRI nuez;
 public IRI pixidio;
 public IRI sincarpos;
 //tipos de nervadura
 public IRI trinervada;
 public IRI pinnada;
 public IRI escaleriforme;
 //tipo de hoja
 public IRI simple;
 public IRI trifoliada;
 public IRI paripinnada;
 public IRI imparipinnada;
 public IRI digitada;
 public IRI bipinnada;
 public IRI bifoliada;
 public IRI compuesta;
 //tipo de borde
 public IRI dentado;
 public IRI entero;
 //posicion de hoja
 public IRI opuesta;
 public IRI alterna;
 //relaciones
 
 
 IRIdictionary(String r){
   this.documentiri=IRI.create(r);
   String s = r+"#";
   //declaracion de IRIs
 //partes
   this.hoja = IRI.create(s + "ihoja");
   this.fruto= IRI.create(s + "ifruto");
   this.flor= IRI.create(s + "iflor");
   //estructuras accsesorias
   this.semilla= IRI.create(s + "isemilla");
   this.savia= IRI.create(s + "isavia");
   this.sarcillo= IRI.create(s + "isarcillo");
   this.punto= IRI.create(s + "ipunto");
   this.pelo= IRI.create(s + "ipelo");
   this.nervadura= IRI.create(s + "inervadura");
   this.glandula= IRI.create(s + "iglandula");
   this.estipula= IRI.create(s + "iestipula");
   this.espina= IRI.create(s + "iespina");
   this.escama= IRI.create(s + "iescama");
   this.olor= IRI.create(s + "xolor");
   //color
   this.amarilla= IRI.create(s + "xamarilla");
   this.anaranjada= IRI.create(s + "xanaranjada");
   this.blanca= IRI.create(s + "xblanca");
   this.incolora= IRI.create(s + "xincolora");
   this.roja= IRI.create(s + "xroja");
   //tipos de frutos
   this.alado= IRI.create(s + "falado");
   this.baya= IRI.create(s + "fbaya");
   this.capsula= IRI.create(s + "fcapsula");
   this.dicarpo= IRI.create(s + "fdicarpo");
   this.foliculo= IRI.create(s + "ffoliculo");
   this.legumbre= IRI.create(s + "flegumbre");
   this.monocarpos= IRI.create(s + "fmonocarpos");
   this.nuez= IRI.create(s + "fnuez");
   this.pixidio= IRI.create(s + "fpixidio");
   this.sincarpos= IRI.create(s + "fsincarpos");
   //tipos de nervadura
   this.trinervada = IRI.create(s + "ntrinervada");
   this.pinnada = IRI.create(s + "npinnada");
   this.escaleriforme = IRI.create(s + "nescaleriforme");
   //tipo de hoja
   this.simple= IRI.create(s + "hsimple");
   this.trifoliada= IRI.create(s + "htrifoliolada");
   this.paripinnada= IRI.create(s + "hparipinnada");
   this.imparipinnada= IRI.create(s + "himparipinnada");
   this.digitada= IRI.create(s + "hdigitada");
   this.bipinnada= IRI.create(s + "hbipinnada");
   this.bifoliada= IRI.create(s + "hbifoliolada");
   this.compuesta= IRI.create(s + "hcompuesta");
   //tipo de borde
   this.dentado = IRI.create(s + "bdentado");
   this.entero = IRI.create(s + "bentero");
   //posicion de hoja
   this.opuesta= IRI.create(s + "popuesto");
   this.alterna= IRI.create(s + "palterna");
 }
 }
 //Diccionario de relaciones de la ontologia
 class IRIrelationship{
 	public IRI documentiri;
 	public IRI bordetipo;
 	public IRI tienecolor;
 	public IRI frutotipo;
 	public IRI hojatipo;
 	public IRI nervaduratipo;
 	public IRI tieneparte;
 	public IRI posiciontipo;
 	
 	IRIrelationship(String r){
 		this.documentiri=IRI.create(r);
 		  String s = r+"#";
 			this.bordetipo = IRI.create(s + "TieneBorde");
 			this.tienecolor = IRI.create(s + "TieneColor");
 			this.frutotipo = IRI.create(s + "TieneFruto");
 			this.hojatipo = IRI.create(s + "TieneHoja");
 			this.nervaduratipo = IRI.create(s + "TieneNervadura");
 			this.tieneparte = IRI.create(s + "TieneParte");
 			this.posiciontipo= IRI.create(s + "TienePosicion");
 	}
 }
 
 //Componente de conocimiento de la aplicacion consta de una Ontologia(Base de Conocimientos)
 //y el razonador hermit (Motor de Inferencia)
 public class KComponent {
 // Atributos del Componente de conocimiento
 private File file;
 private OWLOntology knowledgebase;
 private OWLOntologyManager manager;
 private OWLDataFactory factory;
 private IRIdictionary iridictionary;
 private OWLReasoner reasoner;
 private IRIrelationship relationship;
 public List<OWLAxiom> assertionset;
 KComponent(String s) throws OWLOntologyCreationException{
 	this.file = new File(s);
 	manager = OWLManager.createOWLOntologyManager();
 	knowledgebase = manager.loadOntologyFromOntologyDocument(file);
 	factory = manager.getOWLDataFactory();
 	iridictionary= new IRIdictionary("http://www.Biotax.com/Biotax.owl");
 	relationship= new IRIrelationship("http://www.Biotax.com/Biotax.owl");
 	OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
 	ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
 	OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
 	reasoner = reasonerFactory.createReasoner(knowledgebase, config);
 	assertionset = new ArrayList();
 }
 public String Identify(Planta especie){
 	
 	return "";
 }
 public void IdentifyHoja(Hoja hoja){
 	if(hoja.posicionhoja != null){
 		if(hoja.posicionhoja == "Alterna"){
 		 assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.posiciontipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.alterna)));
 		}
 		else
 		if(hoja.posicionhoja == "Opuesta"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.posiciontipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.opuesta)));
 	
 		}
 	}
 	
 	if(hoja.tipoborde != null){
 		if(hoja.tipoborde == "Dentado"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.bordetipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.dentado)));
 
 		}
 		else
 			if(hoja.tipoborde == "Entero"){
 				assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.bordetipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.entero)));
 	
 			}	
 		
 		}
 	
 	
 	if(hoja.tipohoja != null){
 		
 		if(hoja.tipohoja == "Compuesta"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.compuesta)));
 	
 		}
 
		if(hoja.tipohoja == "Simple"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.simple)));
 	
 			}
		if(hoja.tipohoja == "Bifoliolada"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.bifoliada)));
 
 		}
 		
		if(hoja.tipohoja == "Bipinnada"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.bipinnada)));
 
 		}
 		
		if(hoja.tipohoja == "Digitada"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.digitada)));
 			
 		}
 		
		if(hoja.tipohoja == "Imparipinnada"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.imparipinnada)));
 	
 		}
 		
		if(hoja.tipohoja == "Paripinnada"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.paripinnada)));
 
 		}
		if(hoja.tipohoja == "Trifoliolada"){
 			assertionset.add(factory.getOWLObjectPropertyAssertionAxiom(factory.getOWLObjectProperty(relationship.hojatipo), factory.getOWLNamedIndividual(iridictionary.hoja),factory.getOWLNamedIndividual(iridictionary.trifoliada)));
 
 		}
 	}
 }
 public void IdentifyFruto(Fruto fruto){
 	
 }
 public void IdentifyNervadura(String nervadura){
 	
 }
 
 };
 
