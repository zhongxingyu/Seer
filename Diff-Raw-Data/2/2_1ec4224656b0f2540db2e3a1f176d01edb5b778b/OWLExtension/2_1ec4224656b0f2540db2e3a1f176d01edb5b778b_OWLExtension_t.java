 /*
  * : OWLExtension.java
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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.nlogo.api.DefaultClassManager;
 import org.nlogo.api.ExtensionException;
 import org.nlogo.api.ExtensionManager;
 import org.nlogo.api.PrimitiveManager;
 import org.semanticweb.owlapi.model.IRI;
 
 /**
  * <!-- OWLExtension -->
  * 
  * <p>
  * A netlogo extension for creating OWL ontologies from netlogo models. This
  * consists of the following commands:
  * </p>
  * 
  * <ol>
  * <li><code><b>owl:domain</b> <i>link-breed</i> <i>breed</i></code> -- declare
  * that the <code><i>link-breed</i></code> is always from agents of the
  * <code><i>breed</i></code></li>
  * <li><code><b>owl:range</b> <i>link-breed</i> <i>breed</i></code> -- declare
  * that the <code><i>link-breed</i></code> is always to agents of the
  * <code><i>breed</i></code></li>
  * <li><code><b>owl:imports</b> <i>IRI...</i></code> -- state that the model
  * structure ontology imports the one(s) given by the IRI string argument(s)</li>
  * <li><code><b>owl:options</b> <i>option string...</i></code> -- request
  * options when the ontology is built. The string <code>"owl2"</code> adds OWL 2
  * assertions to the ontology, and the string <code>"relations"</code> adds
  * relational attributes to properties where possible. The string
  * <code>"no-parcels"</code> suppresses spatial axioms. The string
  * <code>"none"</code> stipulates that none of the <code>"owl2"</code>, the
  * <code>"no-parcels"</code> or the <code>"relations"</code> options are
  * required. Each execution of the <code>owl:options</code> command overrides
  * any earlier settings.</li>
  * <li>
  * <code><b>owl:model</b> <i>IRI</i></code> -- define the IRI of the model
  * ontology. All entities will be named as fragments of this IRI</li>
  * <li><code><b>owl:structure</b> <i>file-name</i></code> -- save the structure
  * of the netlogo model as an OWL ontology to the specified file name</li>
  * <li>
  * <code><b>owl:state</b> <i>file-name</i> <i>time-step</i> [<i>extension</i>]</code>
  * -- save the current state of the netlogo model to the specified file name.
  * The logical IRI of the state ontology will be constructed from the model IRI,
  * the time-step, and, if given, the <code><i>extension</i></code> argument</li>
  * </ol>
  * 
  * <p>
  * The commands should be used in the approximate order given above.
  * Specifically, you must use <code>domain</code>, <code>range</code> and
  * <code>imports</code> before you call <code>model</code>, and you must use
  * <code>structure</code> and <code>state</code> after you call
  * <code>model</code>. You may only call <code>model</code> once. You can call
  * <code>options</code> as often as you like, but it will only affect
  * subsequently executed <code>structure</code> and <code>state</code> commands.
  * </p>
  * 
  * @author Gary Polhill
  */
 public class OWLExtension extends DefaultClassManager implements NetLogoEntityIRIGenerator {
   /**
    * Model IRI
    */
   private IRI modelIRI = null;
 
   /**
    * Set of imports for the model structure ontology
    */
   private Set<IRI> imports = new HashSet<IRI>();
 
   /**
    * Domains of link breeds
    */
   private Map<String, String> linkDomains = new HashMap<String, String>();
 
   /**
    * Ranges of link breeds
    */
   private Map<String, String> linkRanges = new HashMap<String, String>();
 
   /**
    * Options
    */
   private Options options = null;
 
   /**
    * <!-- load -->
    * 
    * Called each time a model using the extension is compiled. This is used to
    * build the command objects.
    * 
    * @see org.nlogo.api.DefaultClassManager#load(org.nlogo.api.PrimitiveManager)
    * @param manager
    * @throws ExtensionException
    */
   @Override
   public void load(PrimitiveManager manager) throws ExtensionException {
     Structure structureCmd = new Structure();
     structureCmd.setExtension(this);
     manager.addPrimitive("structure", structureCmd);
 
     Imports importsCmd = new Imports();
     importsCmd.setExtension(this);
     manager.addPrimitive("imports", importsCmd);
 
     Model modelCmd = new Model();
     modelCmd.setExtension(this);
     manager.addPrimitive("model", modelCmd);
 
     State stateCmd = new State();
     stateCmd.setExtension(this);
     manager.addPrimitive("state", stateCmd);
 
     Domain domainCmd = new Domain();
     domainCmd.setExtension(this);
     manager.addPrimitive("domain", domainCmd);
 
     Range rangeCmd = new Range();
     rangeCmd.setExtension(this);
     manager.addPrimitive("range", rangeCmd);
 
     // options command -- doesn't need a reference to this
     options = new Options();
     manager.addPrimitive("options", options);
   }
 
   /**
    * <!-- runOnce -->
    * 
    * Called once per NetLogo instance
    * 
    * @see org.nlogo.api.DefaultClassManager#runOnce(org.nlogo.api.ExtensionManager)
    * @param manager
    * @throws ExtensionException
    */
   @Override
   public void runOnce(ExtensionManager manager) throws ExtensionException {
     // nothing to do at present
   }
 
   /**
    * <!-- unload -->
    * 
    * Called once before load(), and once before NetLogo is closed or a new model
    * opened
    * 
    * @see org.nlogo.api.DefaultClassManager#unload()
    * @throws ExtensionException
    */
   @Override
   public void unload(ExtensionManager manager) throws ExtensionException {
     modelIRI = null;
     imports.clear();
   }
 
   /**
    * <!-- clearAll -->
    * 
    * Clear any stored state
    * 
    * @see org.nlogo.api.DefaultClassManager#clearAll()
    */
   @Override
   public void clearAll() {
     modelIRI = null;
     imports.clear();
     linkDomains.clear();
     linkRanges.clear();
   }
 
   /**
    * <!-- addImport -->
    * 
    * Add an IRI to the set of ontologies imported by the model ontology
    * 
    * @param iri
    * @throws ExtensionException
    */
   public void addImport(IRI iri) throws ExtensionException {
     if(imports.contains(iri)) {
       throw new ExtensionException("Import " + iri + " already declared");
     }
     imports.add(iri);
   }
 
   /**
    * <!-- imports -->
    * 
    * @return The set of imports as an iterable. This prevents changes to the
    *         set.
    */
   public Iterable<IRI> imports() {
     return imports;
   }
 
   /**
    * <!-- setModelIRI -->
    * 
    * Set the IRI of the model structure ontology
    * 
    * @param iri
    * @throws ExtensionException
    */
   public void setModelIRI(IRI iri) throws ExtensionException {
     if(modelIRI != null) {
       throw new ExtensionException("Model IRI has already been set (to " + modelIRI + ")");
     }
     modelIRI = iri;
   }
 
   /**
    * <!-- getModelIRI -->
    * 
    * @return The IRI of the model structure ontology (<code>null</code> by
    *         default)
    */
   public IRI getModelIRI() {
     return modelIRI;
   }
 
   /**
    * <!-- getEntityIRI -->
    * 
    * Return a standardised entity IRI
    * 
    * @param nlEntity NetLogo entity name
    * @param isClass <code>true</code> if the entity is an OWLClass/NetLogo breed
    * @return entity IRI
    * @throws ExtensionException
    */
   public IRI getEntityIRI(IRI iri, String nlEntity, boolean isClass) throws ExtensionException {
    String nlEntityNoSpace = nlEntity.replaceAll("\\W", "_");
     String owlEntityName = nlEntityNoSpace.toLowerCase();
     if(isClass) {
       owlEntityName = owlEntityName.substring(0, 1).toUpperCase() + owlEntityName.substring(1);
     }
     return IRI.create(iri + "#" + owlEntityName);
   }
 
   public IRI getEntityIRI(String nlEntity, boolean isClass) throws ExtensionException {
     if(modelIRI == null) {
       throw new ExtensionException("Cannot get IRI of entity \"" + nlEntity + "\" before model IRI has been specified");
     }
     return getEntityIRI(modelIRI, nlEntity, isClass);
   }
 
   public IRI getGlobalIRI() throws ExtensionException {
     return getEntityIRI("_global", false);
   }
 
   /**
    * <!-- setDomain -->
    * 
    * Set the domain of the link to the breed
    * 
    * @param link
    * @param breed
    */
   public void setDomain(String link, String breed) {
     linkDomains.put(link.toLowerCase(), breed);
   }
 
   /**
    * <!-- setRange -->
    * 
    * Set the range of the link to the breed
    * 
    * @param link
    * @param breed
    */
   public void setRange(String link, String breed) {
     linkRanges.put(link.toLowerCase(), breed);
   }
 
   /**
    * <!-- getDomain -->
    * 
    * Get the domain of the link
    * 
    * @see NetLogoEntityIRIGenerator#getDomain(java.lang.String)
    * @param link
    * @return
    */
   public String getDomain(String link) {
     return linkDomains.get(link.toLowerCase());
   }
 
   /**
    * <!-- getRange -->
    * 
    * Get the range of the link
    * 
    * @see NetLogoEntityIRIGenerator#getRange(java.lang.String)
    * @param link
    * @return
    */
   public String getRange(String link) {
     return linkRanges.get(link.toLowerCase());
   }
 
   /**
    * <!-- hasDomainSpecified -->
    * 
    * Check whether a link has a domain specified
    * 
    * @see NetLogoEntityIRIGenerator#hasDomainSpecified(java.lang.String)
    * @param link
    * @return
    */
   public boolean hasDomainSpecified(String link) {
     return linkDomains.containsKey(link.toLowerCase());
   }
 
   /**
    * <!-- hasRangeSpecified -->
    * 
    * Check whether a link has a range specified
    * 
    * @see NetLogoEntityIRIGenerator#hasRangeSpecified(java.lang.String)
    * @param link
    * @return
    */
   public boolean hasRangeSpecified(String link) {
     return linkRanges.containsKey(link.toLowerCase());
   }
 
   /**
    * <!-- option -->
    * 
    * Check whether an option has been specified. No check is made whether the
    * option is valid at check time, but only valid options can be specified by
    * the user.
    * 
    * @param arg
    * @return <code>true</code> if the option has been specied
    */
   public boolean option(String arg) {
     return options.hasOption(arg);
   }
 
   /**
    * <!-- getOptions -->
    * 
    * @return the options the user has set
    */
   public Options getOptions() {
     return options;
   }
 }
