 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 package org.s23m.cell.communication.xml;
 
 import org.s23m.cell.Set;
 import org.s23m.cell.api.Query;
 import org.s23m.cell.api.models.S23MSemanticDomains;
 import org.s23m.cell.api.models.SemanticDomain;
 import org.s23m.cell.communication.SetMarshaller;
 import org.s23m.cell.communication.SetMarshallingException;
 import org.s23m.cell.communication.xml.model.dom.Namespace;
 import org.s23m.cell.communication.xml.model.schemainstance.ArtifactSet;
 import org.s23m.cell.communication.xml.model.schemainstance.Command;
 import org.s23m.cell.communication.xml.model.schemainstance.Edge;
 import org.s23m.cell.communication.xml.model.schemainstance.Identity;
 import org.s23m.cell.communication.xml.model.schemainstance.Model;
 import org.s23m.cell.communication.xml.model.schemainstance.SemanticDomainNode;
 import org.s23m.cell.communication.xml.model.schemainstance.SuperSetReference;
 import org.s23m.cell.communication.xml.model.schemainstance.Vertex;
 import org.s23m.cell.communication.xml.model.schemainstance.Visibility;
 import org.s23m.cell.platform.api.Instantiation;
 import org.s23m.cell.platform.models.Agency;
 
 // TODO add a constructor accepting the Schema to use and validate against
 public class XmlSetMarshaller implements SetMarshaller<String> {
 
 	private final Namespace namespace;
 	
 	private final XmlSchemaTerminology terminology;
 		
 	public XmlSetMarshaller(Namespace namespace, XmlSchemaTerminology terminology) {
 		this.namespace = namespace;
 		this.terminology = terminology;
 	}		
 	
 	/*
 	 * Implementation:
 	 * -> for processing exactly one instance, use the filter() operations:
 	 *  filterInstances() goes over all the contained instances (but remember to
 	 *  catch the Edge Ends that must be accessed from each Edge via toEdgeEnd() and fromEdgeEnd()).
 	 * -> can also use filter(<properClass>) to structure the work by proper classes.
 	 * 
 	 * Test case:
 	 * -> should be able to convert the set of models and the set of semantic domains to the XML format.
 	 * 
 	 * Later on:
 	 * 1) serialisation of all sets contained within a top-level agent (recursively)
 	 * 2) processing all changed sets within a transaction, all of which should be related to one agent
 	 * (instantiation API needs to be agent-aware: multiple transactions open at once, and every transaction
 	 * relates to exactly one agent)
 	 * 
 	 * Test case:
 	 * -> processing all semantic domains of an agent and the all models of an agent.
 	 * Need to make at least 2 passes along the containment tree to resolve all references.
 	 * 
 	 * Sets must be serialised in the right order:
 	 * - identities
 	 * - semantic domains and semantic identities (which can only be reconstituted with appropriate identities)
 	 * - models (which can only be reconstituted with appropriate semantic domains)
 	 */
 	@Override
 	public String serialise(Set graph) throws SetMarshallingException {
 		// TODO use sets as shown in Scratch.java
 		String languageIdentifier = "ENGLISH";
 		final InstanceBuilder builder = new InstanceBuilder(namespace, terminology, languageIdentifier);
 		final ArtifactSet artifactSet = builder.artifactSet();
 		
 		for (final Set containedInstance : graph.filterInstances()) {
 			Model model = serialiseModel(builder, containedInstance);
 			artifactSet.addModel(model);
 		}
 		
 		final Set containedSemanticDomains = Instantiation.toSemanticDomain(graph).filterPolymorphic(SemanticDomain.semanticdomain);
 		for (final Set semanticDomain : containedSemanticDomains) {
 			artifactSet.addSemanticDomain(serialiseSemanticDomain(builder, semanticDomain));
 		}
 		
 		return XmlRendering.render(artifactSet).toString();
 	}
 	
 	@Override
 	public Set deserialise(String input) throws SetMarshallingException {
 		// TODO
 	    
 		return null;
 	}
 
 	private Model serialiseModel(InstanceBuilder builder, Set model) {
		if (isSuperSetOf(Agency.agent, model.category()) || isSuperSetOf(Agency.stage, model.category())) {
 			// cover potential polymorphic extensions of Agent and Stage (these could become a variability)
 			
 			// TODO how does this apply in the case of models? 
 			//serialiseSemanticDomain(builder, Instantiation.toSemanticDomain(model));
 		}
 		Model structure = serialiseStructure(builder, model);
 		return structure;
 	}
 
 	private SemanticDomainNode serialiseSemanticDomain(InstanceBuilder builder, Set semanticDomain) {
 		final SemanticDomainNode result = builder.semanticDomain(); 
 		
 		// serialise identities
 		Set orderedSetOfSemanticIdentities = semanticDomain.filterPolymorphic(SemanticDomain.semanticIdentity);
 		for (Set semanticIdentitySet: orderedSetOfSemanticIdentities) {
 			Identity identity = builder.identity(semanticIdentitySet);
 			result.addIdentity(identity);	
 		}
 		
 		final Model model = serialiseStructure(builder, semanticDomain);
 		result.setModel(model);
 		return result;
 	}
 
 	private Model serialiseStructure(InstanceBuilder builder, Set model) {
 		final Model result = builder.model(model);
 		
 		// process Vertex list
 		for (Set vertexInstance : model.filterProperClass(Query.vertex)) {
 			Vertex vertex = builder.vertex(vertexInstance);
 			result.addVertex(vertex);
 		}
 		
 		// process Edge list
 		for (Set edgeInstance : model.filterProperClass(Query.edge)) {
 			Edge edge = builder.edge(edgeInstance);
 			result.addEdge(edge);
 		}
 		
 		// process Visibility list
 		for (Set visibilityInstance : model.filterProperClass(Query.visibility)) {
 			Visibility visibility = builder.visibility(visibilityInstance);
 			result.addVisibility(visibility);
 		}
 		
 		// process SuperSetReference list
 		for (Set superSetReferenceInstance : model.filterProperClass(Query.superSetReference)) {
 			SuperSetReference superSetReference = builder.superSetReference(superSetReferenceInstance);
 			result.addSuperSetReference(superSetReference);
 		}
 		
 		// process Command list
 		for (Set commandInstance : model.filter(S23MSemanticDomains.command)) {
 			Command command = builder.command(commandInstance);
 			result.addCommand(command);
 		}
 		
 		// process Query list
 		for (Set queryInstance : model.filter(S23MSemanticDomains.query)) {
 			org.s23m.cell.communication.xml.model.schemainstance.Query query = builder.query(queryInstance);
 			result.addQuery(query);
 		}
 		
 		return result;
 	}
 	
	private boolean isSuperSetOf(Set set, Set candidateSuperSet) {
		return set.isSuperSetOf(candidateSuperSet).isEqualTo(S23MSemanticDomains.is_TRUE);
 	}
 }
