 /*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop 
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A. 
 
 GNU Lesser General Public License
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
 *****************************************************************/
 
 package system;
 
 import jade.core.Agent;
 import jade.core.behaviours.*;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.util.leap.ArrayList;
 import jade.util.leap.List;
 import jade.content.onto.*;
 import jade.content.onto.basic.Action;
 import jade.content.abs.AbsPredicate;
 import jade.content.lang.*;
 import jade.content.ContentElement;
 import jade.content.lang.Codec.CodecException;
 import jade.content.lang.sl.*;
 
 import ontology.CBR.CBRTerminologyOntology;
 import ontology.CBR.Hypothesis;
 import ontology.CBR.PossibleSolution;
 import ontology.CBR.Problem;
 import ontology.CBR.Retrieve;
 import ontology.common.Description;
 import ontology.taxonomy.TaxonomicRank;
 import redundantDiscriminationNet.RDNet;
 import redundantDiscriminationNet.RootNorm;
 import system.searchAutomata.CaseMemoryDFSAutomaton;
 import system.searchAutomata.GoalApproachingHandler;
 import system.searchAutomata.SearchStatus;
 import system.searchAutomata.TaxonomySearchAutomaton;
 import system.searchAutomata.output.CaseMemoryDFSAutomatonOutput;
 
 @SuppressWarnings("serial")
 public class RetrieverAgent extends Agent {
   //Se registra el lenguaje de contenido y la ontologa
   private Codec codec = new SLCodec();
   private Ontology ontology = CBRTerminologyOntology.getInstance();
   private Problem problem;
   private List successfulConflictSet;
   private List failureConflictSet;
   private List noResultsSet;
 
   // Incializacin del agente
 	@Override
   protected void setup() {
     // Imprimir un mensaje de bienvenida
 	System.out.println("Hola! Agente recuperador "+getAID().getName()+" listo.");
 
     getContentManager().registerLanguage(codec);
     getContentManager().registerOntology(ontology);
     
     this.setSuccessfulConflictSet(new ArrayList());
     this.setFailureConflictSet(new ArrayList());
     this.setNoResultsSet(new ArrayList());
     
     // Agrega el comportamiento de servir solicitudes de recuperacin
     addBehaviour(new RetrievalRequestsServer());
   }
 
   // Operaciones de limpieza del agente
     @Override
   protected void takeDown() {
     // Imprimir un mensaje de despedida
     System.out.println("Que tenga buen da! Agente recuperador "+getAID().getName()+" fuera de servicio.");
   }
 
 	/**
 	   La clase interna RetrievalRequestsServer.
 	 */
 	private class RetrievalRequestsServer extends CyclicBehaviour {
 	  public void action() {
 
 		// Preparar plantilla para recibir el mensaje
         MessageTemplate mt = MessageTemplate.and(MessageTemplate.and(
             MessageTemplate.MatchLanguage(codec.getName()),
             MessageTemplate.MatchOntology(ontology.getName())),
             MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
 
         ACLMessage msg = receive(mt);
 
         if (msg != null) {
             try {
                 if (msg.getPerformative() == ACLMessage.REQUEST) {
                     ContentElement ce = null;
                     // Convertir la cadena a objetos Java
                     ce = getContentManager().extractContent(msg);
                     if (ce instanceof Action) {
                     	Retrieve ret = (Retrieve) ((Action) ce).getAction();
                     	
                     	initialize();
                     	setProblem(ret.getSimilarTo());
                     	
                     	retrieveHypothesis();                                      	                    
                     	
                         ACLMessage reply = msg.createReply();
                         reply.setPerformative(ACLMessage.INFORM);
                         
                         AbsPredicate ap = new AbsPredicate(CBRTerminologyOntology.ARESIMILARTO);
                         
                         ap.set(CBRTerminologyOntology.ARESIMILARTO_FAILURECONFLICTSET, ontology.fromObject(getFailureConflictSet()));
                         ap.set(CBRTerminologyOntology.ARESIMILARTO_SUCCESSFULCONFLICTSET, ontology.fromObject(getSuccessfulConflictSet()));
                         ap.set(CBRTerminologyOntology.ARESIMILARTO_PROBLEM, ontology.fromObject(getProblem()));
                         
                         // Convertir objetos Java a cadena
           	          	getContentManager().fillContent(reply, ap);
 
                         myAgent.send(reply);
 
                         System.out.println(getAID().getName()+" ha enviado respuesta de consulta sobre posibles soluciones.");
                     }
                 }
             }
             catch (CodecException ce) {
                 ce.printStackTrace();
             }
             catch (OntologyException oe) {
                 oe.printStackTrace();
             }
         } else {
 		    block();
 		}
        }
 	  } // Fin de la clase interna RetrievalRequestsServer
 
 	/**
 	 * Recupera las posibles soluciones que constituyen hipotesis basado en estructuras
 	 */
 	private void retrieveHypothesis() {
 		// First choice: use the case memory primary search data structure
 		if (!(OracleIDSystem.getInstance().getCaseMemory().getRoot().getNets().isEmpty())) {
 			this.useCaseMemory();
 		} else {
			if (OracleIDSystem.getInstance().isInteractive())
				// Second choice: use the taxonomic hierarchy as primary search data structure
				this.useTaxonomicHierarchy();
 		}
 	}
 	
 	private void initialize() {
 		this.getSuccessfulConflictSet().clear();
 		this.getFailureConflictSet().clear();
 		this.getNoResultsSet().clear();
 		this.setProblem(null);
 	}
 	
 	private Problem getProblem() {
 		return problem;
 	}
 
 	private void setProblem(Problem problem) {
 		this.problem = problem;
 	}
 
 	/**
 	 * Mtodo de instancia agregado
 	 * @param failStructConflictSet
 	 */
 	private void setFailureConflictSet(List failStructConflictSet) {
 		this.failureConflictSet = failStructConflictSet;
 	}
 	
 	/**
 	 * @see "Mtodo failStructConflictSet: del protocolo adding en SUKIA SmallTalk"
 	 * @param aHypothesis
 	 */
 	private void addFailureConflictSet(Hypothesis aHypothesis) {
 		this.getFailureConflictSet().add(aHypothesis);
 	}
 
 	/**
 	 * @see "Mtodo failStructConflictSet del protocolo accessing en SUKIA SmallTalk"
 	 * @return
 	 */
 	private List getFailureConflictSet() {
 		return failureConflictSet;
 	}
 	
 	/**
 	 * Mtodo de instancia agregado
 	 * @param noResultsSet
 	 */
 	private void setNoResultsSet(List noResultsSet) {
 		this.noResultsSet = noResultsSet;
 	}
 
 	/**
 	 * @see "Mtodo noResultsSet: del protocolo adding en SUKIA SmallTalk"
 	 * @param aHypothesis
 	 */
 	private void addNoResultsSet(Hypothesis aHypothesis) {
 		this.getNoResultsSet().add(aHypothesis);
 	}
 	
 	/**
 	 * @see "Mtodo noResultsSet del protocolo accessing en SUKIA SmallTalk"
 	 * @return
 	 */
 	private List getNoResultsSet() {
 		return noResultsSet;
 	}
 	
 	/**
 	 * Mtodo de instancia agregado
 	 * @param succStructConflictSet
 	 */
 	private void setSuccessfulConflictSet(List succStructConflictSet) {
 		this.successfulConflictSet = succStructConflictSet;
 	}
 	
 	/**
 	 * @see "Mtodo succGHConflictSet: del protocolo adding en SUKIA SmallTalk"
 	 * @param aHypothesis
 	 */
 	private void addSuccessfulConflictSet(Hypothesis aHypothesis) {
 		this.getSuccessfulConflictSet().add(aHypothesis);
 	}
 
 	/**
 	 * @see "Mtodo succStructConflictSet del protocolo accessing en SUKIA SmallTalk"
 	 * @return
 	 */
 	private List getSuccessfulConflictSet() {
 		return successfulConflictSet;
 	}
 	
 	/**
 	 * @see "M&eacute;todo useCaseMemory del protocolo private-general en SUKIA SmallTalk"
 	 * @return
 	 */
 	private boolean useCaseMemory() {
 		if (this.searchHypothesisInCaseMemory() == false)
 			return false;
 
 		return true;
 	}
 	
 	/**
 	 * @see "M&eacute;todo useCaseMemory del protocolo private-general en SUKIA SmallTalk"
 	 * @return
 	 */
 	private boolean useTaxonomicHierarchy() {
 		if (this.searchHypothesisInTaxonomy() == false)
 			return false;
 
 		return true;
 	}
 	
 	/**
 	 * PRECONDITION: (anOutputCopy possible solutions list is NOT empty)
 	 * RETURNS:	nil - if the precondition is not met, OR an error occurred.
 	 * self - if all OK
 	 * @param searchAutomaton
 	 * @param hypothesis1
 	 * @param hypothesis2
 	 * @return
 	 */
 	private boolean processSuccessfulSearchOutput(CaseMemoryDFSAutomatonOutput anOutputCopy, 
 			Hypothesis hypothesis1, Hypothesis hypothesis2) {
 		Hypothesis currHypothesis;
 		PossibleSolution ps;
 		GoalApproachingHandler dialog;
 		int index;
 		SearchStatus status;
 	
 		// Check the precondition
 		if (anOutputCopy.getPossibleSolutions().isEmpty())
 			return false;
 
 		// Successful search: at least one case was found. Scan the output copy's possible solution's list
 		while (!(anOutputCopy.getPossibleSolutions().isEmpty())) {
 			// Get the next possible solution from the search automaton's output
 			ps = anOutputCopy.getPossibleSolutions().remove(0);
 
 			/*Attempt to add the possible solution to the first hypothesis. If not successful, attempt
 			to add it to the second hypothesis. If unsuccessful again, return an error value. At the 
 			end of this loop, the following assertion will always hold: hypothesis1 will never be empty
 			(it will always have at least ONE element), and hypothesis2 may be empty*/
 			if (hypothesis1.addPossibleSolution(ps) == false)
 				if (hypothesis2.addPossibleSolution(ps) == false) return false;
 		}
 		
 		// Once all possible solutions have been loaded, copy the search automaton's unmatched description
 		if (!(hypothesis1.getPossibleSolutions().isEmpty())) {
 			hypothesis1.addAllToUnmatchedDescription(anOutputCopy.getUnmatchedDescription());
 			hypothesis1.addAllToJustification(anOutputCopy.getJustification());
 		}
 
 		if (!(hypothesis2.getPossibleSolutions().isEmpty())) {
 			hypothesis2.addAllToUnmatchedDescription(anOutputCopy.getUnmatchedDescription());
 			hypothesis2.addAllToJustification(anOutputCopy.getJustification());
 		}
 		
 		/* HYPOTHESIS1: If none of the possible solutions is equal to, or more specific than
 		 the identification goal, establish a new dialog with the user, in order to try to draw
 		 the existing possible solutions nearer to the said goal*/
 		if (!(hypothesis1.getPossibleSolutions().isEmpty())) {
 			if (TaxonomicRank.getIndex(TaxonomicRank.valueOf(((PossibleSolution)hypothesis1
 					.getPossibleSolutions().get(0)).getLevel().toUpperCase())) <
 					TaxonomicRank.getIndex(TaxonomicRank.valueOf(OracleIDSystem.getInstance().getIdentGoal().toUpperCase()))
 					&& OracleIDSystem.getInstance().isInteractive()) {
 				dialog = new GoalApproachingHandler(OracleIDSystem.getInstance().getIdentGoal(), hypothesis1 //OJO
 						,OracleIDSystem.getInstance().getTaxonomy(), OracleIDSystem.getInstance().getMinSimilarityDegree());
 				dialog.chat();
 				status = dialog.getStatus();
 				if (status == SearchStatus.ERROR || status == SearchStatus.CANCEL)
 					return false;
 			}
 		} else {
 			// hypothesis1 does not contain positive cases
 			/* HYPOTHESIS2: If none of the possible solutions is equal to, or more specific than
 			 the identification goal, establish a new dialog with the user, in order to try to draw
 		 	the existing possible solutions nearer to the said goal*/
 			// OJO:hypothesis2.isEmpty() en SUKIA
 			if (!(hypothesis2.getPossibleSolutions().isEmpty())) {
 				if (TaxonomicRank.getIndex(TaxonomicRank.valueOf(((PossibleSolution)hypothesis2
 						.getPossibleSolutions().get(0)).getLevel().toUpperCase())) <
 						TaxonomicRank.getIndex(TaxonomicRank.valueOf(OracleIDSystem.getInstance().getIdentGoal().toUpperCase()))
 						&& OracleIDSystem.getInstance().isInteractive()) {
 					dialog = new GoalApproachingHandler(OracleIDSystem.getInstance().getIdentGoal(), hypothesis2 //OJO
 							,OracleIDSystem.getInstance().getTaxonomy(), OracleIDSystem.getInstance().getMinSimilarityDegree());
 					dialog.chat();
 					status = dialog.getStatus();
 					if (status == SearchStatus.ERROR || status == SearchStatus.CANCEL)
 						return false;
 				}
 			}
 		}
 		
 		// Finally, add the hypotheses to conflict sets
 		index = 1;
 		currHypothesis = hypothesis1;
 		
 		while (index <= 2) {
 			if (!(currHypothesis.getPossibleSolutions().isEmpty())) {
 				if (((PossibleSolution)currHypothesis.getPossibleSolutions().get(0)).getStatus() == true)
 					this.addSuccessfulConflictSet(currHypothesis);
 				else {
 					if (((PossibleSolution)currHypothesis.getPossibleSolutions().get(0)).getStatus() == false)
 						this.addFailureConflictSet(currHypothesis);
 					else return false;
 				}
 			}
 			
 			currHypothesis = hypothesis2;
 			index = index + 1;
 		}
 
 		return true;
 	}
 	
 	/**
 	 * Use the structure description as search data.
 	 * RETURNS: nil - If an error occurred, or the user cancelled the search process.
 	 * self - OK
 	 * @return
 	 */
 	private boolean searchHypothesisInCaseMemory() {
 		String structureName;
 		Hypothesis hypothesis1, hypothesis2;
 		Description problemDescription;
 		RDNet net;
 		RootNorm caseNetRoot = null;
 		CaseMemoryDFSAutomaton searchAutomaton;
 		CaseMemoryDFSAutomatonOutput outputCopy;
 		SearchStatus status;
 		java.util.List<String> structureList;
 		
 		structureList = this.getProblem().getDescription().getStructuresList();
 		
 		while (!(structureList.isEmpty())) {
 			// Remove the next grouping heuristic from the description
 			structureName = structureList.remove(0);
 
 			/* Create a first instance of Hypothesis and assign the structure as descriptive element for
 			 * a positive case
 			 */
 			hypothesis1 = new Hypothesis();
 			hypothesis1.setDescription(this.getProblem().getDescription().getDescription(structureName));
 
 			/* Create a second instance of hypothesis, and again, assign the same structure as descriptive
 			 * element for a negative case
 			 */
 			hypothesis2 = new Hypothesis();
 			hypothesis2.setDescription(this.getProblem().getDescription().getDescription(structureName));
 			
 			/* Get the SAV problem description from the structure. If no description available, return error
 			 * value
 			 */
 			problemDescription = this.getProblem().getDescription().getDescription(structureName);
 			if (problemDescription == null) return false;
 			
 			// Get the net root that corresponds to the structure
 			net = OracleIDSystem.getInstance().getCaseMemory().getRoot().getRDNet(structureName);
 			if (net == null) 
 				caseNetRoot = null;
 			else caseNetRoot = net.getRoot();
 					
 			if (!(caseNetRoot == null)) {
 				// Create a new instance of case net search automaton
 				searchAutomaton = new CaseMemoryDFSAutomaton(caseNetRoot);
 	
 				// Begin the search with the given problem description
 				searchAutomaton.beginSearch(problemDescription);
 				status = searchAutomaton.getStatus();
 	
 				if (status == SearchStatus.ERROR || status == SearchStatus.CANCEL)
 					return false;
 	
 				/* If the automaton returns a non-empty problem description list, then the REASONER MUST call 
 				 it again with that remaining description. Before doing so, all lists, except the doubtful and unconfirmed 
 				 ones, MUST be flushed. Make sure the new search begins at root level, and all necessary control variables 
 				 are correctly set. This process repeats until the problem description list is EMPTY. After each successive 
 				 call, the unmatched description, the justification and whatever possible solutions MUST be concatenated 
 				 to the original search-automaton output copy*/
 				outputCopy = searchAutomaton.getSearchOutput();
 				outputCopy.setTaxonomy(OracleIDSystem.getInstance().getTaxonomy());
 				
 				while (!(problemDescription.getDescriptors().isEmpty())) {
 					searchAutomaton.beginNewSearch(problemDescription);
 					status = searchAutomaton.getStatus();
 
 					if (status == SearchStatus.ERROR || status == SearchStatus.CANCEL)
 						return false;
 
 					outputCopy.addAllToPossibleSolutions(searchAutomaton.getSearchOutput().getPossibleSolutions());
 					outputCopy.addAllToJustification(searchAutomaton.getSearchOutput().getJustification());
 					outputCopy.addAllToUnmatchedDescription(searchAutomaton.getSearchOutput().getUnmatchedDescription());
 				}
 				
 				outputCopy.compressPossibleSolutions();
 
 				if (!(outputCopy.getPossibleSolutions().isEmpty())) {
 					if (this.processSuccessfulSearchOutput(outputCopy, hypothesis1, hypothesis2) == false)
 						return false;
 
 					continue;
 				}
 				
 				/* At this point, status is neither #error, #cancel, nor #success (this value is implied by the
 				 condition (outputCopy possibleSolutions isEmpty) ifFalse:. Thus, is must be #fail. Copy 
 				 the unmatched description and the justification to the first hypothesis, which is the one
 			   	 we'll continue to use from now on.*/
 				hypothesis1.addAllToUnmatchedDescription(outputCopy.getUnmatchedDescription());
 				hypothesis1.addAllToJustification(outputCopy.getJustification());
 			}
 		
 			/* At this point, either: a) no net root was found, or b) the status of the net search was unsuccessful 
 			 (i.e., status = #fail). Try doing a taxonomic search*/
	
			if (OracleIDSystem.getInstance().isInteractive())
				if (searchInTaxonomyByStructure(structureName, hypothesis1) == false)
					return false;
 		}
 	
 		return true;
 	}
 	
 	/**
 	 * Performs  a search in the taxonomic hierarchy, using a set of structures
 	 * as input data.
 	 * RETURNS: nil - if an error occurs, OR the user cancels the search process.
 	 * self - OK
 	 */
 	private boolean searchHypothesisInTaxonomy() {
 		String structureName;
 		Hypothesis hypothesis;
 		java.util.List<String> structureList;
 		
 		structureList = this.getProblem().getDescription().getStructuresList();
 		
 		while (!(structureList.isEmpty())) {
 			// Remove the next structure from the description
 			structureName = structureList.remove(0);
 			
 			// Create a first instance of Hypothesis and assign the grouping heuristic as descriptive element
 			hypothesis = new Hypothesis();
 			hypothesis.setDescription(this.getProblem().getDescription().getDescription(structureName));
 
 			if (searchInTaxonomyByStructure(structureName, hypothesis) == false)
 				return false;
 		}
 			
 		return true;
 	}
 	
 	private boolean searchInTaxonomyByStructure(String structureName, Hypothesis hypothesis) {
 		Description problemDescription;
 		SearchStatus status;
 		TaxonomySearchAutomaton searchAutomaton;
 		GoalApproachingHandler dialog;
 		
 		// Get the SAV problem description from the structure
 		problemDescription = this.getProblem().getDescription().getDescription(structureName);
 		if (problemDescription == null) return false;
 		
 		// Perform a taxonomic search
 		searchAutomaton = new TaxonomySearchAutomaton(OracleIDSystem.getInstance().getTaxonomy(), 
 				OracleIDSystem.getInstance().getMinSimilarityDegree());
 		searchAutomaton.beginSearch(problemDescription);
 		status = searchAutomaton.getStatus();
 
 		if (status == SearchStatus.ERROR || status == SearchStatus.CANCEL)
 			return false;
 						
 		if (status == SearchStatus.SUCCESS) {
 			// Load all the possible solutions into the hypothesis
 			while (!(searchAutomaton.getSearchOutput().getPossibleSolutions().isEmpty())) {
 				/* Attempt to add the possible solution to the hypothesis. If not successful, return 
 				an error value*/
 				 if (hypothesis.addPossibleSolution(searchAutomaton.getSearchOutput().getPossibleSolutions().remove(0)) == false)
 					 return false;
 			}
 			
 			hypothesis.addAllToUnmatchedDescription(searchAutomaton.getSearchOutput().getUnmatchedDescription());
 			hypothesis.addAllToJustification(searchAutomaton.getSearchOutput().getJustification());
 			
 			/* Determine if necessary to establish a dialog with the user (in case none of the possible solutions is
 			 equal to or more specific than the identification goal)*/
 			if (((PossibleSolution)hypothesis.getPossibleSolutions().get(0)).getStatus() == true) {
 				if (TaxonomicRank.getIndex(TaxonomicRank.valueOf(((PossibleSolution)hypothesis
 						.getPossibleSolutions().get(0)).getLevel().toUpperCase())) <
						TaxonomicRank.getIndex(TaxonomicRank.valueOf(OracleIDSystem.getInstance().getIdentGoal().toUpperCase()))) {
 					dialog = new GoalApproachingHandler(OracleIDSystem.getInstance().getIdentGoal(), hypothesis
 							, OracleIDSystem.getInstance().getTaxonomy(), OracleIDSystem.getInstance().getMinSimilarityDegree());
 					dialog.chat();
 					status = dialog.getStatus();
 					if (status == SearchStatus.ERROR || status == SearchStatus.CANCEL)
 						return false;
 				}
 			}
 
 			// Add the hypothesis to the successful conflict set
 			this.addSuccessfulConflictSet(hypothesis);
 
 			//return this.searchTaxonStructures();
 		} else this.addNoResultsSet(hypothesis); // Search was unsuccessful. Add hypothesis to the no-results set
 		
 		return true;
 	}
 
 }
