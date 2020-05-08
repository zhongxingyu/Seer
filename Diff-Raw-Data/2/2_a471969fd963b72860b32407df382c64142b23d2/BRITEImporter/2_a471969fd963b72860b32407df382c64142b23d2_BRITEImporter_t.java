 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Importer
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.importer;
 
 import de.tuilmenau.ics.fog.importer.parser.TopologyDistributor;
 import de.tuilmenau.ics.fog.importer.parser.TopologyDistributorAnalyser;
 import de.tuilmenau.ics.fog.importer.parser.TopologyParserBRITE;
 import de.tuilmenau.ics.fog.topology.Simulation;
 
 
 public class BRITEImporter implements ScenarioImporter
 {
 	@Override
 	public void importScenario(String importFilename, Simulation simulation, String parameters) throws Exception
 	{
 		boolean flat = false;
 		boolean distributorAnalysor = false;
 		
 		if(parameters != null) {
 			flat = parameters.contains("flat");
 			
 			meta = parameters.contains("meta");
 			
 			distributorAnalysor = parameters.contains("variable");
 		}
 		
 		ITopologyParser parser = createParser(importFilename, simulation);
 		TopologyDistributor distributor;
 		if(distributorAnalysor) { 
 			distributor = new TopologyDistributorAnalyser(parser, simulation);
 		} else {
 			distributor = new TopologyDistributor(parser, simulation, flat);
 		}
 		
 		distributor.createNodes();
 		distributor.createEdges();
		
		distributor.close();
 	}
 	
 	protected ITopologyParser createParser(String importFilename, Simulation simulation) throws Exception
 	{
 		return new TopologyParserBRITE(simulation.getLogger(), importFilename, meta);
 	}
 	
 	protected boolean useMeta()
 	{
 		return meta;
 	}
 	
 	private boolean meta = false;
 }
