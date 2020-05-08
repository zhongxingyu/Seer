 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package strategy.game.version.delta.configuration;
 
 import static strategy.game.common.PieceType.BOMB;
 import static strategy.game.common.PieceType.CAPTAIN;
 import static strategy.game.common.PieceType.COLONEL;
 import static strategy.game.common.PieceType.FLAG;
 import static strategy.game.common.PieceType.GENERAL;
 import static strategy.game.common.PieceType.LIEUTENANT;
 import static strategy.game.common.PieceType.MAJOR;
 import static strategy.game.common.PieceType.MARSHAL;
 import static strategy.game.common.PieceType.MINER;
 import static strategy.game.common.PieceType.SCOUT;
 import static strategy.game.common.PieceType.SERGEANT;
 import static strategy.game.common.PieceType.SPY;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import strategy.common.PlayerColor;
 import strategy.game.common.Location;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.version.common.board.BoardRuleSet;
 import strategy.game.version.common.configuration.ConfigurationRuleSet;
 
 /**
  * Constructor for DeltaStartingConfigurationRuleSet
  * @author rjsmieja, jrspicola
  * @version Sept 27, 2013
  */
 public class DeltaStartingConfigurationRuleSet implements ConfigurationRuleSet {
 
 	final private static int TOTAL_NUMBER_OF_PIECES_FOR_PLAYER = 40;
 	final private static int TOTAL_NUMBER_OF_CHOKEPOINTS = 8;
 
 	final private BoardRuleSet startingBoardRules;
 	final private HashMap<PieceType, Integer> requiredPieceCount;
 
 	/**
 	 * Constructor for DeltaStartingConfigurationRuleSet
 	 * @param boardRules the BoardRuleSet to follow
 	 */
 	public DeltaStartingConfigurationRuleSet(BoardRuleSet boardRules){
 		requiredPieceCount = new HashMap<PieceType, Integer>();
 
 		requiredPieceCount.put(MARSHAL, 1);
 		requiredPieceCount.put(GENERAL, 1);
 		requiredPieceCount.put(COLONEL, 2);
 		requiredPieceCount.put(MAJOR, 3);
 		requiredPieceCount.put(CAPTAIN, 4);
 		requiredPieceCount.put(LIEUTENANT, 4);
 		requiredPieceCount.put(SERGEANT, 4);
 		requiredPieceCount.put(MINER, 5);
 		requiredPieceCount.put(SCOUT, 8);
 		requiredPieceCount.put(SPY, 1);
 		requiredPieceCount.put(BOMB, 6);
 		requiredPieceCount.put(FLAG, 1);
 
 		startingBoardRules = boardRules;
 	}
 
 	@Override
 	public boolean validateConfiguration(PlayerColor player, Collection<PieceLocationDescriptor> configuration){
 		if(configuration == null){
 			return false;
 		}
 
 		if(player != null){
 			//Normal player
 			if(configuration.size() != TOTAL_NUMBER_OF_PIECES_FOR_PLAYER){
 				return false;
 			}
 
 			//HashSet to keep track of duplicates
 			final HashMap<PieceType, Integer> pieceCount = new HashMap<PieceType, Integer>();
			pieceCount.put(MARSHAL, 0);
			pieceCount.put(GENERAL, 0);
			pieceCount.put(COLONEL, 0);
			pieceCount.put(MAJOR, 0);
			pieceCount.put(CAPTAIN, 0);
			pieceCount.put(LIEUTENANT, 0);
			pieceCount.put(SERGEANT, 0);
			pieceCount.put(MINER, 0);
			pieceCount.put(SCOUT, 0);
			pieceCount.put(SPY, 0);
			pieceCount.put(BOMB, 0);
			pieceCount.put(FLAG, 0);
 
 			final Set<Location> previousLocations = new HashSet<Location>();
 
 			for(PieceLocationDescriptor i : configuration){
 
 				if(!requiredPieceCount.containsKey(i.getPiece().getType())){
 					return false;
 				}
 
 				if (!startingBoardRules.validateLocation(i)){
 					return false;
 				}
 
 				if (previousLocations.contains(i.getLocation())){
 					return false;
 				}else{ 
 					previousLocations.add(i.getLocation());
 				}
 
 				//Increment how many we have seen so far
 				pieceCount.put(i.getPiece().getType(), pieceCount.get(i.getPiece().getType()) + 1);
 			}
 
 			if (!requiredPieceCount.equals(pieceCount)){
 				return false;
 			}
 		} else {
 			//ChokePoints
 			if(configuration.size() != TOTAL_NUMBER_OF_CHOKEPOINTS){
 				return false;
 			}
 
 			for (PieceLocationDescriptor i : configuration){
 				if (!startingBoardRules.validateLocation(i)){
 					return false;
 				}
 				
 				if (i.getPiece().getType() != PieceType.CHOKE_POINT){
 					return false;
 				}
 				if (i.getPiece().getOwner() != null){
 					return false;
 				}
 			}
 		}
 
 		//All validations passed
 		return true;
 	}
 }
