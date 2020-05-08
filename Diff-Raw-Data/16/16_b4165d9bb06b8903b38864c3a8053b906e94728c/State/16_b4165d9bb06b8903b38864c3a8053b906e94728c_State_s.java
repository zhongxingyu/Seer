 /*
  * : State.java
  * 
  * Copyright (C) 2013 The James Hutton Institute
  * 
  * This file is part of NetLogo2OWL.
  * 
  * NetLogo2OWL is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * NetLogo2OWL is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with NetLogo2OWL. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: Gary Polhill, The James Hutton Institute,
  * Craigiebuckler, Aberdeen. AB15 8QH. UK. gary.polhill@hutton.ac.uk
  */
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.nlogo.api.Agent;
 import org.nlogo.api.AgentSet;
 import org.nlogo.api.Argument;
 import org.nlogo.api.Context;
 import org.nlogo.api.DefaultCommand;
 import org.nlogo.api.ExtensionException;
 import org.nlogo.api.Link;
 import org.nlogo.api.LogoException;
 import org.nlogo.api.LogoList;
 import org.nlogo.api.Patch;
 import org.nlogo.api.Primitive;
 import org.nlogo.api.Program;
 import org.nlogo.api.Syntax;
 import org.nlogo.api.Turtle;
 import org.nlogo.api.World;
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.AddImport;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLAxiom;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLDataFactory;
 import org.semanticweb.owlapi.model.OWLDataProperty;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLObjectProperty;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyStorageException;
 import org.semanticweb.owlapi.vocab.XSDVocabulary;
 
 /**
  * <!-- State -->
  * 
  * Build a state ontology (A-box assertions) corresponding to the current state
  * of a NetLogo model.
  * 
  * @author Gary Polhill
  */
 public class State extends DefaultCommand implements Primitive {
 
   /**
    * The OWLExtension object, for access to information about the ontology
    * provided through other commands.
    */
   private OWLExtension extension = null;
 
   /**
    * The first number in the variables[] array of a turtle variabe that is one
    * of the turtle's "own" variables as opposed to a NetLogo standard variable.
    */
   public static final int NETLOGO_TURTLE_OWN_ARRAY_START = 13;
 
   /**
    * The first number in the variables[] array of a link variable that is one of
    * the breed's "own" variables as opposed to a NetLogo standard variable.
    */
   public static final int NETLOGO_LINK_OWN_ARRAY_START = 10;
 
   /**
    * The first number in the variables[] array of a patch variable that is one
    * of the patch's "own" variables as opposed to a NetLogo standard variable.
    */
   public static final int NETLOGO_PATCH_OWN_ARRAY_START = 5;
 
   /**
    * <!-- getSyntax -->
    * 
    * The syntax of this command is variadic, with at least one string containing
    * an imported ontology IRI
    * 
    * @see org.nlogo.api.DefaultCommand#getSyntax()
    * @return Syntax of this command
    */
   @Override
   public Syntax getSyntax() {
     return Syntax.commandSyntax(new int[] { Syntax.StringType(), Syntax.NumberType() });
   }
 
   /**
    * <!-- getAgentClassString -->
    * 
    * The command can only be run from the observer
    * 
    * @see org.nlogo.api.DefaultCommand#getAgentClassString()
    * @return String indicating as much
    */
   @Override
   public String getAgentClassString() {
     return "O";
   }
 
   /**
    * <!-- perform -->
    * 
    * Create an IRI for the state ontology, then add A-box axioms for each of the
    * patches, turtles, links and globals.
    * 
    * @see org.nlogo.api.Command#perform(org.nlogo.api.Argument[],
    *      org.nlogo.api.Context)
    * @param args
    * @param context
    * @throws ExtensionException
    * @throws LogoException
    */
   @Override
   public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
     if(extension == null) {
       throw new ExtensionException("Bug: Extension not initialised properly (NetLogo/OWL extension fault)");
     }
     IRI modelIRI = extension.getModelIRI();
     if(modelIRI == null) {
       throw new ExtensionException("You must set the model IRI using the owl:model command");
     }
 
     String model = modelIRI.toString();
 
    double tick = args[1].getDoubleValue();
     StringBuffer buff = new StringBuffer(model.endsWith(".owl") ? model.substring(0, model.length() - 4) : model);
    buff.append("-");
    buff.append(Double.toString(tick));
     if(model.endsWith(".owl")) {
       buff.append(".owl");
     }
 
     IRI logicalIRI = IRI.create(buff.toString());
 
     String physical = args[0].getString();
     
     if(!physical.endsWith(".owl")) {
       String logicalPath = logicalIRI.toURI().getPath();
       String[] pathSplit = logicalPath.split("/");
 
      physical += pathSplit[pathSplit.length];
     }
     
     File physicalFile = new File(physical);
     if(physicalFile.exists()) {
       throw new ExtensionException("Physical location for state ontology (" + physical + ") exists");
     }
     IRI physicalIRI = IRI.create(new File(physical));
 
     Agent observer = context.getAgent();
     World world = observer.world();
     Program program = world.program();
 
     try {
       OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
       OWLDataFactory factory = OWLManager.getOWLDataFactory();
       OWLOntology ontology = manager.createOntology(logicalIRI);
 
       Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
       manager.applyChange(new AddImport(ontology, factory.getOWLImportsDeclaration(modelIRI)));
 
       axioms.addAll(getBreedAxioms(logicalIRI, factory, program, world, extension, extension.getOptions()));
       axioms.addAll(getPatchAxioms(logicalIRI, factory, program, world, extension, extension.getOptions()));
       axioms.addAll(getLinkAxioms(logicalIRI, factory, program, world, extension));
       axioms.addAll(getGlobalAxioms(logicalIRI, factory, observer, program, extension));
       manager.addAxioms(ontology, axioms);
       manager.saveOntology(ontology, physicalIRI);
     }
     catch(OWLOntologyCreationException e) {
       throw new ExtensionException("Cannot create OWL ontology \"" + logicalIRI + "\": " + e);
     }
     catch(OWLOntologyStorageException e) {
       throw new ExtensionException("Cannot save OWL ontology \"" + logicalIRI + "\" to \"" + physical + "\": " + e);
     }
   }
 
   /**
    * <!-- getGlobalAxioms -->
    * 
    * Get all the axioms from global variables. These will be data properties of
    * the 'global entity'.
    * 
    * @param logicalIRI The Logical IRI of the state ontology
    * @param factory An OWL data factory to build axioms with
    * @param observer The NetLogo observer agent object
    * @param program The NetLogo program object
    * @param generator A generator of IRIs
    * @return Global variable axioms.
    * @throws ExtensionException
    */
   private Collection<? extends OWLAxiom> getGlobalAxioms(IRI logicalIRI, OWLDataFactory factory, Agent observer,
       Program program, NetLogoEntityIRIGenerator generator) throws ExtensionException {
     Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
 
     OWLIndividual globalEntity = factory.getOWLNamedIndividual(generator.getGlobalIRI());
 
     int i = 0;
     for(String global: program.globals()) {
       Object value = observer.getVariable(i);
       addDataPropertyAxioms(logicalIRI, globalEntity,
           factory.getOWLDataProperty(generator.getEntityIRI(global, false)), value, axioms, factory, generator);
       i++;
     }
 
     return axioms;
   }
 
   /**
    * <!-- getLinkAxioms -->
    * 
    * Return the link axioms
    * 
    * @param logicalIRI Logical IRI of the state ontology
    * @param factory An OWL data factory to build axioms with
    * @param program The NetLogo API program object
    * @param world The NetLogo API world object
    * @param generator A generator to build IRIs with
    * @return A collection of axioms for the links
    * @throws ExtensionException
    */
   private Collection<? extends OWLAxiom> getLinkAxioms(IRI logicalIRI, OWLDataFactory factory, Program program,
       World world, NetLogoEntityIRIGenerator generator) throws ExtensionException {
     Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
 
     AgentSet links = world.links();
 
     for(Agent agent: links.agents()) {
       Link link = (Link)agent;
 
       String linkName = link.getBreed().printName();
 
       String end1 = link.end1().toString();
 
       String end2 = link.end2().toString();
 
       OWLIndividual indiv = factory.getOWLNamedIndividual(generator.getEntityIRI(logicalIRI, link.toString(), false));
 
       int i = NETLOGO_LINK_OWN_ARRAY_START;
       for(String own: program.linkBreedsOwn().get(linkName)) {
         OWLDataProperty property = factory.getOWLDataProperty(generator.getEntityIRI(own, false));
         Object value = link.getVariable(i);
         addDataPropertyAxioms(logicalIRI, indiv, property, value, axioms, factory, generator);
         i++;
       }
 
       if(i == NETLOGO_LINK_OWN_ARRAY_START) {
         axioms.add(factory.getOWLObjectPropertyAssertionAxiom(
             factory.getOWLObjectProperty(generator.getEntityIRI(linkName, false)),
             factory.getOWLNamedIndividual(generator.getEntityIRI(end1, false)),
             factory.getOWLNamedIndividual(generator.getEntityIRI(end2, false))));
       }
       else {
         // reified link -- link's properties will have been added earlier
         axioms
             .add(factory.getOWLClassAssertionAxiom(factory.getOWLClass(generator.getEntityIRI(linkName, true)), indiv));
 
         axioms.add(factory.getOWLObjectPropertyAssertionAxiom(
             factory.getOWLObjectProperty(generator.getEntityIRI(Structure.reifyOut(linkName), false)),
             factory.getOWLNamedIndividual(generator.getEntityIRI(end1, false)), indiv));
 
         axioms.add(factory.getOWLObjectPropertyAssertionAxiom(
             factory.getOWLObjectProperty(generator.getEntityIRI(Structure.reifyIn(linkName), false)), indiv,
             factory.getOWLNamedIndividual(generator.getEntityIRI(end2, false))));
       }
 
     }
 
     return axioms;
   }
 
   /**
    * <!-- getPatchAxioms -->
    * 
    * Get all the patch axioms
    * 
    * @param logicalIRI The logical IRI of the state ontology
    * @param factory An OWL data factory to build axioms with
    * @param program The NetLogo program object
    * @param world The NetLogo world object
    * @param generator A generator of IRIs
    * @param options Options for building the ontology
    * @return A collection of axioms
    * @throws ExtensionException
    */
   private Collection<? extends OWLAxiom> getPatchAxioms(IRI logicalIRI, OWLDataFactory factory, Program program,
       World world, NetLogoEntityIRIGenerator generator, Options options) throws ExtensionException {
     Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
 
     if(!options.hasOption(Options.NO_PATCHES_OPTION)) {
       AgentSet patches = world.patches();
 
       for(Agent agent: patches.agents()) {
         Patch patch = (Patch)agent;
 
         OWLIndividual indiv =
           factory.getOWLNamedIndividual(generator.getEntityIRI(logicalIRI, patch.toString(), false));
 
         double pxcor = patch.pxcor();
         double pycor = patch.pycor();
 
         axioms.add(factory.getOWLClassAssertionAxiom(
             factory.getOWLClass(generator.getEntityIRI(Structure.PATCH_CLASS, true)), indiv));
 
         axioms.add(factory.getOWLDataPropertyAssertionAxiom(
             factory.getOWLDataProperty(generator.getEntityIRI(Structure.X_PROPERTY, false)), indiv, pxcor));
         axioms.add(factory.getOWLDataPropertyAssertionAxiom(
             factory.getOWLDataProperty(generator.getEntityIRI(Structure.Y_PROPERTY, false)), indiv, pycor));
 
         int i = 0;
         for(String own: program.patchesOwn()) {
           if(i >= NETLOGO_PATCH_OWN_ARRAY_START) {
             OWLDataProperty property = factory.getOWLDataProperty(generator.getEntityIRI(own, false));
             Object value = patch.getVariable(i);
             addDataPropertyAxioms(logicalIRI, indiv, property, value, axioms, factory, generator);
           }
           i++;
         }
 
       }
     }
 
     return axioms;
   }
 
   /**
    * <!-- getBreedAxioms -->
    * 
    * Return all the axioms for the turtles. If the no-patches option isn't set,
    * then include axioms on the turtles' locations.
    * 
    * @param logicalIRI The logical IRI of the state ontology
    * @param factory An OWL data factory to build axioms with
    * @param program The NetLogo API program object
    * @param world The NetLogo API world object
    * @param generator An IRI generator
    * @param options Options for building the ontology
    * @return A collection of axioms
    * @throws ExtensionException
    */
   private Collection<? extends OWLAxiom> getBreedAxioms(IRI logicalIRI, OWLDataFactory factory, Program program,
       World world, NetLogoEntityIRIGenerator generator, Options options) throws ExtensionException {
     Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
 
     for(Agent agent: world.turtles().agents()) {
 
       Turtle turtle = (Turtle)agent;
 
       OWLIndividual indiv = factory.getOWLNamedIndividual(generator.getEntityIRI(logicalIRI, agent.toString(), false));
 
       String breed = turtle.getBreed().printName();
       OWLClass breedClass = factory.getOWLClass(generator.getEntityIRI(breed, true));
 
       double xcor = turtle.xcor();
       double ycor = turtle.ycor();
       boolean hidden = turtle.hidden();
 
       if(!hidden && !options.hasOption(Options.NO_PATCHES_OPTION)) {
         OWLObjectProperty location =
           factory.getOWLObjectProperty(generator.getEntityIRI(Structure.LOCATION_PROPERTY, false));
 
         Patch patch = turtle.getPatchHere();
 
         OWLIndividual patchIndiv =
           factory.getOWLNamedIndividual(generator.getEntityIRI(logicalIRI, patch.toString(), false));
 
         axioms.add(factory.getOWLObjectPropertyAssertionAxiom(location, indiv, patchIndiv));
 
         axioms.add(factory.getOWLDataPropertyAssertionAxiom(
             factory.getOWLDataProperty(generator.getEntityIRI(Structure.X_PROPERTY, false)), indiv, xcor));
         axioms.add(factory.getOWLDataPropertyAssertionAxiom(
             factory.getOWLDataProperty(generator.getEntityIRI(Structure.Y_PROPERTY, false)), indiv, ycor));
 
       }
 
       axioms.add(factory.getOWLClassAssertionAxiom(breedClass, indiv));
       int i = NETLOGO_TURTLE_OWN_ARRAY_START;
       for(String own: program.breedsOwn().get(breed)) {
         OWLDataProperty property = factory.getOWLDataProperty(generator.getEntityIRI(own, false));
         Object value = agent.getVariable(i);
         addDataPropertyAxioms(logicalIRI, indiv, property, value, axioms, factory, generator);
         i++;
       }
 
     }
 
     return axioms;
   }
 
   /**
    * <!-- addDataPropertyAxioms -->
    * 
    * Utility method to add data property assertion axioms, which attempts to
    * behave differently according to the type of the value being stored.
    * 
    * @param logicalIRI Logical IRI of the state ontology
    * @param subject Subject of the data property assertion axiom
    * @param property The data property
    * @param value Object of the data property assertion axiom
    * @param axioms Set of axioms to add to
    * @param factory OWL data factory to build axioms with
    * @param generator IRI generator
    * @throws ExtensionException
    */
   private void addDataPropertyAxioms(IRI logicalIRI, OWLIndividual subject, OWLDataProperty property, Object value,
       Set<OWLAxiom> axioms, OWLDataFactory factory, NetLogoEntityIRIGenerator generator) throws ExtensionException {
     if(value instanceof String) {
       axioms.add(factory.getOWLDataPropertyAssertionAxiom(property, subject, (String)value));
     }
     else if(value instanceof Double) {
       axioms.add(factory.getOWLDataPropertyAssertionAxiom(property, subject, (Double)value));
     }
     else if(value instanceof LogoList) {
       for(Object entry: (LogoList)value) {
         if(entry instanceof Agent) {
           axioms.add(factory.getOWLDataPropertyAssertionAxiom(property, subject, factory.getOWLLiteral(generator
               .getEntityIRI(logicalIRI, entry.toString(), false).toURI().toString(),
               factory.getOWLDatatype(XSDVocabulary.ANY_URI.getIRI()))));
         }
         else if(entry instanceof Double) {
           axioms.add(factory.getOWLDataPropertyAssertionAxiom(property, subject, (Double)entry));
         }
         else {
           axioms.add(factory.getOWLDataPropertyAssertionAxiom(property, subject, entry.toString()));
         }
       }
     }
     else if(value instanceof AgentSet) {
       for(Agent entry: ((AgentSet)value).agents()) {
         axioms.add(factory.getOWLDataPropertyAssertionAxiom(property, subject, factory.getOWLLiteral(generator
             .getEntityIRI(logicalIRI, entry.toString(), false).toURI().toString(),
             factory.getOWLDatatype(XSDVocabulary.ANY_URI.getIRI()))));
       }
     }
     else {
       axioms.add(factory.getOWLDataPropertyAssertionAxiom(property, subject, value.toString()));
     }
 
   }
 
   /**
    * <!-- setExtension -->
    * 
    * Called from the OWLExtension initialisation method to pass in a link to
    * itself.
    * 
    * @param extension
    */
   public void setExtension(OWLExtension extension) {
     this.extension = extension;
   }
 
 }
