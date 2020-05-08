 package org.ow2.mindEd.adl.textual.ui.contentassist;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.xtext.Assignment;
 import org.eclipse.xtext.RuleCall;
 import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
 import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
 import org.ow2.mindEd.adl.AdlDefinition;
 import org.ow2.mindEd.adl.ArchitectureDefinition;
 import org.ow2.mindEd.adl.BindingDefinition;
 import org.ow2.mindEd.adl.CompositeBody;
 import org.ow2.mindEd.adl.Element;
 import org.ow2.mindEd.adl.InterfaceDefinition;
 import org.ow2.mindEd.adl.SubComponentDefinition;
 import org.ow2.mindEd.adl.TemplateSpecifier;
 import org.ow2.mindEd.adl.TemplateSpecifiersList;
 import org.ow2.mindEd.adl.custom.impl.AdlDefinitionCustomImpl;
 import org.ow2.mindEd.adl.custom.impl.CompositeComponentDefinitionCustomImpl;
 import org.ow2.mindEd.adl.textual.util.FractalUtil;
 import org.ow2.mindEd.ide.core.MindModelManager;
 import org.ow2.mindEd.ide.core.ModelToProjectUtil;
 import org.ow2.mindEd.ide.model.MindAdl;
 import org.ow2.mindEd.ide.model.MindPackage;
 
 /**
  * Content assist for Mind adl editor
  * 
  * see
  * http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on
  * how to customize content assistant
  * 
  * @author Damien Fournier
  * @contributor Rémi Mélisson
  * 
  */
 
 public class FractalProposalProvider extends AbstractFractalProposalProvider {
 
     // Reference on the project model
     private MindModelManager mindModel = MindModelManager.getMindModelManager();
 
     // -------------------------------------------------------------------------------//
     // Overridden methods
     // -------------------------------------------------------------------------------//
 
     /**
      * Content Assist for Interface definition
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeInterfaceDefinition_Signature(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeInterfaceDefinition_Signature(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         // Only works if project is a Mind project
         if (mindModel != null) {
 
             String proposal;
             ICompletionProposal completionProposal;
 
             // Interfaces proposals
 
             List<String> itf = ModelToProjectUtil.INSTANCE.getInterfacesInProject();
 
             for (String key : itf) {
                 proposal = key;
                 completionProposal = createCompletionProposal(proposal, proposal + " - interface",
                         getImage(model), context);
 
                 acceptor.accept(completionProposal);
             }
 
         }
     }
 
     /**
      * Content assist for import
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeImportDefinition_ImportName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeImportDefinition_ImportName(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         // Only works if project is a Mind project
         if (mindModel != null) {
 
             String proposal;
             ICompletionProposal completionProposal;
 
             // Package proposals
 
             Map<String, List<MindPackage>> packages = mindModel.getMindModel().getAllPackages();
 
             // Remove unnamed package
             packages.remove("");
 
             // Add packages registered for this project to proposals
             // Filtering is done by Xtext automatically
 
             for (String key : packages.keySet()) {
                 proposal = key;
                 completionProposal = createCompletionProposal(proposal + ".*;", proposal
                         + ".* - package", null, context);
 
                 acceptor.accept(completionProposal);
             }
 
             // Component proposals
 
             Collection<? extends MindAdl> components = mindModel.getMindModel().getAllComponents();
 
             for (MindAdl mindAdl : components) {
                 proposal = mindAdl.getQualifiedName();
                 completionProposal = createCompletionProposal(proposal + ";", proposal
                         + " - component", null, context);
 
                 acceptor.accept(completionProposal);
             }
 
             // Interface proposals
 
             List<String> itfList = ModelToProjectUtil.INSTANCE.getInterfacesInProject();
             for (String itf : itfList) {
                 completionProposal = createCompletionProposal(itf + ";", itf + " - interface",
                         null, context);
 
                 acceptor.accept(completionProposal);
             }
 
         }
     }
 
     /**
      * Content assist for composite reference definition
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeCompositeReferenceDefinition_ReferenceName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeCompositeReferenceDefinition_ReferenceName(EObject model,
             Assignment assignment, ContentAssistContext context,
             ICompletionProposalAcceptor acceptor) {
 
         if (model.eContainer() instanceof CompositeBody)
             return;
 
         completeExtends(model, assignment, context, acceptor);
 
     }
 
     /**
      * Content assist for primitive reference definition
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completePrimitiveReferenceDefinition_ReferenceName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completePrimitiveReferenceDefinition_ReferenceName(EObject model,
             Assignment assignment, ContentAssistContext context,
             ICompletionProposalAcceptor acceptor) {
 
         if (model.eContainer() instanceof CompositeBody)
             return;
 
         completeExtends(model, assignment, context, acceptor);
 
     }
 
     /**
      * Content assist for type reference definition
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeTypeReferenceDefinition_ReferenceName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeTypeReferenceDefinition_ReferenceName(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         if (model.eContainer() instanceof CompositeBody)
             return;
 
         completeExtends(model, assignment, context, acceptor);
 
     }
 
     /**
      * Disable content assist with ID Rule
      * 
      * @see org.eclipse.xtext.common.contentassist.TerminalsProposalProvider#complete_ID(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.RuleCall,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void complete_ID(EObject model, RuleCall ruleCall, ContentAssistContext context,
             ICompletionProposalAcceptor acceptor) {
         // Does nothing to disable default content assist for ID Role
     }
 
     /**
      * Content assist for binding source component
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeBindingDefinition_InterfaceSourceParentName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeBindingDefinition_InterfaceSourceParentName(EObject model,
             Assignment assignment, ContentAssistContext context,
             ICompletionProposalAcceptor acceptor) {
 
         // Construct components proposal for binding definition
         completeBindingComponents(model, assignment, context, acceptor);
 
     }
 
     /**
      * Content assist for binding target component
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeBindingDefinition_InterfaceTargetParentName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeBindingDefinition_InterfaceTargetParentName(EObject model,
             Assignment assignment, ContentAssistContext context,
             ICompletionProposalAcceptor acceptor) {
 
         // Retrieve composite component definition
         model = model.eContainer();
         // Construct components proposal for binding definition
         completeBindingComponents(model, assignment, context, acceptor);
 
     }
 
     /**
      * Content assist for binding target interface
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeBindingDefinition_InterfaceTargetName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeBindingDefinition_InterfaceTargetName(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         ArchitectureDefinition elt = FractalUtil.getArchitecureDefinitionFromChild(model);
 
         // Proposal to add for content assist
         String proposal = null;
         ICompletionProposal completionProposal = null;
 
         // Ensure model is an instance of mind architecture definition
         if (model instanceof BindingDefinition) {
 
             // retrieve binding definition
             BindingDefinition definition = (BindingDefinition) model;
 
             // content assist using corresponding sub component
             if (definition.getInterfaceTargetParentName() != null) {
 
                 Map<String, ArchitectureDefinition> map = getSubComponents(elt);
 
                 // retrieve sub component
                 elt = map.get(definition.getInterfaceTargetParentName());
 
             }
             // else content assist using current component
 
             Map<String, InterfaceDefinition> map = getInterfaces(elt);
 
             for (String key : map.keySet()) {
 
                 proposal = key;
                 proposal = getValueConverter().toString(proposal, "ID");
 
                 completionProposal = createCompletionProposal(proposal, proposal
                         + " - provided interface", getImage(map.get(key)), context);
                 acceptor.accept(completionProposal);
 
             }
 
         }
 
     }
 
     /**
      * Content assist for binding source interface
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeBindingDefinition_InterfaceSourceName(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      */
 
     @Override
     public void completeBindingDefinition_InterfaceSourceName(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         ArchitectureDefinition elt = FractalUtil.getArchitecureDefinitionFromChild(model);
 
         // Proposal to add for content assist
         String proposal = null;
         ICompletionProposal completionProposal = null;
 
         // If model instance is BindingDefinition then
         // provides content assist using corresponding sub component
         if (model instanceof BindingDefinition) {
 
             // retrieve binding definition
             BindingDefinition definition = (BindingDefinition) model;
 
             Map<String, ArchitectureDefinition> map = getSubComponents(definition.getParentBody()
                     .getParentComponent());
 
             // retrieve sub component
             elt = map.get(definition.getInterfaceSourceParentName());
 
             // else content assist using current component
 
             Map<String, InterfaceDefinition> itf = getInterfaces(elt);
 
             for (String key : map.keySet()) {
 
                 proposal = key;
                 proposal = getValueConverter().toString(proposal, "ID");
 
                 completionProposal = createCompletionProposal(proposal, proposal
                         + " - required interface", getImage(map.get(key)), context);
                 acceptor.accept(completionProposal);
 
             }
         }
 
     }
 
     /**
      * Content assist for composite component definition name
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeCompositeComponentDefinition_Name(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      * 
      */
     @Override
     public void completeCompositeComponentDefinition_Name(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
         this.completeComponentDefinition_Name((AdlDefinitionCustomImpl) model.eContainer(),
                 context, acceptor);
     }
 
     /**
      * Content assist for primitive component definition name
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completePrimitiveComponentDefinition_Name(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      * 
      */
     @Override
     public void completePrimitiveComponentDefinition_Name(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
         this.completeComponentDefinition_Name((AdlDefinitionCustomImpl) model.eContainer(),
                 context, acceptor);
     }
 
     /**
      * Content assist for component type definition name
      * 
      * @see org.ow2.fractal.mind.xtext.contentassist.AbstractFractalProposalProvider#completeComponentTypeDefinition_Name(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.Assignment,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.core.editor.contentassist.ICompletionProposalAcceptor)
      * 
      */
     @Override
     public void completeComponentTypeDefinition_Name(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
         this.completeComponentDefinition_Name((AdlDefinitionCustomImpl) model.eContainer(),
                 context, acceptor);
     }
 
     /**
      * Content Assist for Component Reference (used in ADL definition after
      * 'contains' keyword)
      * 
      * @see org.ow2.mindEd.adl.textual.ui.contentassist.AbstractFractalProposalProvider#complete_ComponentReference(org.eclipse.emf.ecore.EObject,
      *      org.eclipse.xtext.RuleCall,
      *      org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext,
      *      org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor)
      */
     @Override
     public void complete_ComponentReference(EObject model, RuleCall ruleCall,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         // Proposal to add for content assist
         String proposal = null;
         ICompletionProposal completionProposal = null;
 
         // List of declared imports
         Collection<String> importDefinition = new ArrayList<String>();
 
         // Retrieved declared imports for this Adl definition
         EObject eobject = model;
         while (!(eobject instanceof AdlDefinition)) {
             eobject = eobject.eContainer();
         }
 
         AdlDefinition definition = (AdlDefinition) eobject;
         importDefinition = FractalUtil.getDeclaredImports(definition);
 
         // Only works if project is a Mind project
         if (mindModel != null) {
 
             // Retrieve all components declared for this mind project
             Collection<? extends MindAdl> c = mindModel.getMindModel().getAllComponents();
 
             // Add components name to proposals
             for (MindAdl mindAdl : c) {
 
                 // if package for this component has been declared using import
                 // only add component name to the proposals
                 if (importDefinition.contains(mindAdl.getPackage().getName())
                         | importDefinition.contains(mindAdl.getQualifiedName())) {
                     proposal = mindAdl.getName();
                     // else add component full qualified name
                 } else {
                     proposal = mindAdl.getQualifiedName();
                 }
 
                 // Creates proposal
                 proposal = getValueConverter().toString(proposal, "ID");
                 // Set display text
                 String displayText = proposal + " - component reference";
 
                 completionProposal = createCompletionProposal(proposal, displayText,
                         getImage(definition), context);
 
                 acceptor.accept(completionProposal);
 
             }
         }
 
     }
 
     // -------------------------------------------------------------------------------//
     // Custom methods
     // -------------------------------------------------------------------------------//
 
     public void completeComponentDefinition_Name(AdlDefinitionCustomImpl adlDefinitionCustomImpl,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         String fQN = FractalUtil.getFQNFromAdlDefinition(adlDefinitionCustomImpl);
 
         ICompletionProposal completionProposal = createCompletionProposal(fQN, fQN, null, context);
         acceptor.accept(completionProposal);
     }
 
     /**
      * Creates a list of components to use in a binding proposal
      */
 
     public void completeBindingComponents(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         // Proposal to add for content assist
         String proposal = null;
         ICompletionProposal completionProposal = null;
 
         model = FractalUtil.getArchitecureDefinitionFromChild(model);
         // Ensure model is an instance of mind architecture definition
         if (model instanceof ArchitectureDefinition) {
 
             ArchitectureDefinition definition = (ArchitectureDefinition) model;
 
             // Retrieve sub element for this definition
             for (Element element : definition.getBody().getElements()) {
 
                 // Only elements which are instance of SubComponentDefinition
                 // are valid proposal for binding
                 if (element instanceof SubComponentDefinition) {
 
                     // Add sub component name to the proposal
                     ArchitectureDefinition elt = (ArchitectureDefinition) element;
                     proposal = elt.getName();
                     proposal = getValueConverter().toString(proposal, "ID");
 
                     completionProposal = createCompletionProposal(proposal, proposal
                             + " - bind component", getImage(elt), context);
 
                     acceptor.accept(completionProposal);
 
                 }
 
             }
 
         }
 
     }
 
     /**
      * Content assist for composite and primitive reference definition
      */
 
     public void completeExtends(EObject model, Assignment assignment, ContentAssistContext context,
             ICompletionProposalAcceptor acceptor) {
 
         ArchitectureDefinition architectureDefinition = FractalUtil
                 .getArchitecureDefinitionFromChild(model);
 
         // name of the package where this component is located
         String packageName = FractalUtil.getPackageNameFromFQN(architectureDefinition.getName());
 
         // fully qualified name of this component
         String fQN = architectureDefinition.getName();
 
         // Retrieve all components declared for this mind project
         Collection<? extends MindAdl> components = mindModel.getMindModel().getAllComponents();
 
         // -- Remove this definition from the components
         MindAdl toRemove = null;
         for (MindAdl mindAdl : components) {
             if (mindAdl.getQualifiedName().equals(fQN)) {
                 toRemove = mindAdl;
                 break;
             }
         }
         if (toRemove != null)
             components.remove(toRemove);
         // --
 
         // Restrict to components which are able to be extended by our
         // definition
         components = FractalUtil
                 .filterAccordingToExtendValidity(architectureDefinition, components);
 
         // -- Change names for packages and components which are declared into
         // import: FQN -> simple names
 
         HashSet<String> importedPackAndComp = new HashSet<String>();
 
         importedPackAndComp.add(packageName);
 
         Collection<String> imports = FractalUtil
                 .getDeclaredImports((AdlDefinition) architectureDefinition.eContainer());
 
         // avoid .* at the end of import packages.
         for (String importName : imports) {
             if (importName.endsWith(".*")) {
                 packageName = importName.substring(0, importName.length() - 2);
                 imports.remove(importName);
                 imports.add(packageName);
             }
         }
 
         importedPackAndComp.addAll(imports);
 
         HashSet<String> finalProposals = FractalUtil.convertFQNToSimpleNamesAccordingToImports(
                 components, importedPackAndComp);
 
         // --
 
         // -- Adding components proposals
         ICompletionProposal completionProposal;
         for (String componentName : finalProposals) {
             completionProposal = createCompletionProposal(componentName, componentName, null,
                     context);
             acceptor.accept(completionProposal);
         }
         // --
 
     }
 
     /**
      * Get sub components for this ArchitectureDefinition
      * 
      * @param definition
      * @return
      */
 
     public Map<String, ArchitectureDefinition> getSubComponents(ArchitectureDefinition definition) {
 
         HashMap<String, ArchitectureDefinition> map = new HashMap<String, ArchitectureDefinition>();
 
         if (definition != null) {
             for (Element element : definition.getBody().getElements()) {
                 if (element instanceof SubComponentDefinition) {
                     ArchitectureDefinition def = (ArchitectureDefinition) element;
                     map.put(def.getName(), def);
                 }
             }
         }
 
         return map;
 
     }
 
     /**
      * Get components interfaces according to their role
      * 
      * @param definition
      *            - component
      * @param role
      *            - role of the interfaces
      * @return
      */
 
     public Map<String, InterfaceDefinition> getInterfaces(ArchitectureDefinition definition) {
 
         HashMap<String, InterfaceDefinition> map = new HashMap<String, InterfaceDefinition>();
 
         if (definition != null) {
             for (Element element : definition.getBody().getElements()) {
                 // retrieve interfaces elements
                 if (element instanceof InterfaceDefinition) {
                     InterfaceDefinition def = (InterfaceDefinition) element;
                     // Keep interfaces of the given Role (provides or requires)
                     // TODO no more restriction
                     // if (def.getRole() == role)
                     map.put(def.getName(), def);
                 }
             }
         }
 
         return map;
 
     }
 
     /**
      * 
      * Template sub component proposal provider
      * 
      */
     public void completeTemplateSubComponent_ReferenceName(EObject model, Assignment assignment,
             ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
 
         // We get the Architecture Definition
         EObject eObject = model;
         while (!(eObject instanceof CompositeComponentDefinitionCustomImpl)) {
             try {
                 eObject = eObject.eContainer();
             } catch (Exception e) {
                 // TODO: handle exception
                 // nothing to do here
                 return;
             }
         }
         ArchitectureDefinition architectureDefinition = (ArchitectureDefinition) eObject;
 
         // in order to get the template specifier names
         TemplateSpecifiersList templateSpecifierList = null;
         for (EObject architectureContent : architectureDefinition.eContents()) {
             if (architectureContent instanceof TemplateSpecifiersList) {
                 templateSpecifierList = (TemplateSpecifiersList) architectureContent;
                 break;
             }
         }
 
         // so, we can add names to the proposals
         ICompletionProposal completionProposal;
         String name;
         for (EObject e : templateSpecifierList.eContents()) {
             name = ((TemplateSpecifier) e).getName();
             completionProposal = createCompletionProposal(name + " as", name,
                     getImage(architectureDefinition), context);
             acceptor.accept(completionProposal);
         }
 
     }
 
 }
