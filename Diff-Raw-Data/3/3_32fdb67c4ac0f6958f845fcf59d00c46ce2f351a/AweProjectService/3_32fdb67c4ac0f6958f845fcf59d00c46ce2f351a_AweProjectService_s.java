 package org.amanzi.neo.core.database.services;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.exception.SplashDatabaseException;
 import org.amanzi.neo.core.database.exception.SplashDatabaseExceptionMessages;
 import org.amanzi.neo.core.database.nodes.AbstractNode;
 import org.amanzi.neo.core.database.nodes.AweProjectNode;
 import org.amanzi.neo.core.database.nodes.CellNode;
 import org.amanzi.neo.core.database.nodes.RootNode;
 import org.amanzi.neo.core.database.nodes.RubyProjectNode;
 import org.amanzi.neo.core.database.nodes.RubyScriptNode;
 import org.amanzi.neo.core.database.nodes.SpreadsheetNode;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.SplashRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * Service class for working with Neo4j-Spreadsheet
  * 
  * @author Tsinkel_A
  */
 
 public class AweProjectService {
 
 	/*
 	 * NeoService Provider
 	 */
 	private NeoServiceProvider provider;
 
 	/*
 	 * NeoService
 	 */
 	protected NeoService neoService;
 
 	/**
 	 * Constructor of Service.
 	 * 
 	 * Initializes NeoService and create a Root Element
 	 */
 	public AweProjectService() {	    
 		provider = NeoServiceProvider.getProvider();
 		neoService = provider.getService();
 	}
 
 	/**
 	 * Returns RootNode for projects
 	 * 
 	 * @return root node
 	 */
 	public RootNode getRootNode() {
 		Transaction transaction = neoService.beginTx();
 		try {
 
 			RootNode root = new RootNode(neoService.getReferenceNode());
 			transaction.success();
 			return root;
 		} finally {
 			transaction.finish();
 		}
 	}
 
 	/**
 	 * Find ruby project
 	 * 
 	 * @param rubyProjectName
 	 *            ruby project name
 	 * @return RubyProjectNode
 	 */
 	public RubyProjectNode findRubyProject(String rubyProjectName) {
 		RootNode root = getRootNode();
 		Transaction transaction = neoService.beginTx();
 		try {
 			Iterator<AweProjectNode> iterator = root.getAllProjects();
 			while (iterator.hasNext()) {
 				AweProjectNode project = iterator.next();
 				Iterator<RubyProjectNode> itrRubyProject = project
 						.getAllProjects();
 				while (itrRubyProject.hasNext()) {
 					RubyProjectNode rubyProject = itrRubyProject.next();
 					if (rubyProjectName.equals(rubyProject.getName())) {
 						transaction.success();
 						return rubyProject;
 					}
 				}
 			}
 			transaction.success();
 			return null;
 
 		} finally {
 			transaction.finish();
 		}
 	}
 
 	/**
 	 * Finds or Creates a Spreadsheet
 	 * 
 	 * @param aweProjectName
 	 *            awe project name
 	 * @param rubyProjectName
 	 *            ruby project name
 	 * @param spreadsheetName
 	 *            spreadsheet name
 	 * @return Spreadsheet
 	 */
 	public SpreadsheetNode findOrCreateSpreadsheet(String aweProjectName,
 			String rubyProjectName, String spreadsheetName) {
 		assert aweProjectName != null;
 		assert rubyProjectName != null;
 		assert spreadsheetName != null;
 		AweProjectNode project = findOrCreateAweProject(aweProjectName);
 		RubyProjectNode rubyProject = findOrCreateRubyProject(project,
 				rubyProjectName);
 		return findOrCreateSpreadSheet(rubyProject, spreadsheetName);
 	}
 	
 	/**
      * Searches for Spreadsheets by given name
      * 
      * @param root
      *            root node of Spreadsheet
      * @param name
      *            name of Spreadsheet
      * @return founded Spreadsheet or null if Spreadsheet was not found
      */
     public SpreadsheetNode findSpreadsheet(RubyProjectNode root, String name) {
         SpreadsheetNode result = null;
 
         Transaction transaction = neoService.beginTx();
 
         try {
             Iterator<SpreadsheetNode> spreadsheetIterator = root
                     .getSpreadsheets();
 
             while (spreadsheetIterator.hasNext()) {
                 SpreadsheetNode spreadsheet = spreadsheetIterator.next();
 
                 if (spreadsheet.getSpreadsheetName().equals(name)) {
                     result = spreadsheet;
                     break;
                 }
             }
             transaction.success();
         } finally {
             transaction.finish();
         }
 
         return result;
     }
 
     /**
      * Finds or Creates a Spreadsheet
      * 
      * @param rubyProject ruby project node
      * @param spreadsheetName spreadsheet name
      * @return Spreadsheet
      */
     public SpreadsheetNode findOrCreateSpreadSheet(RubyProjectNode rubyProject, String spreadsheetName) {
         SpreadsheetNode result = findSpreadsheet(rubyProject, spreadsheetName);;
 
         Transaction transaction = neoService.beginTx();
 
         try {
             if (result == null) {
                 result = new SpreadsheetNode(neoService.createNode(),spreadsheetName);
                 rubyProject.addSpreadsheet(result);
             }
             transaction.success();
             return result;
         } finally {
             transaction.finish();
             //Lagutko, 6.10.2009, we should call commit() of NeoManager to be sure
             //that there will be no deadlocks if this spreadsheet 
             //will be used in transaction from other thread
             provider.commit();
         }
     }
 
     /**
      * Find a RubyProject
      * 
      * @param project awe project node
      * @param rubyProjectName name of ruby project
      * @return RubyProjectNode
      */
     public RubyProjectNode findRubyProject(AweProjectNode project, String rubyProjectName) {
         assert project != null;
         assert rubyProjectName != null;
         RubyProjectNode result = null;
         Transaction transaction = neoService.beginTx();
         try {
             Iterator<RubyProjectNode> rubyProjects = project.getAllProjects();
             while (rubyProjects.hasNext()) {
                 RubyProjectNode rubyProject = rubyProjects.next();
                 if (rubyProjectName.equals(rubyProject.getName())) {
                     result = rubyProject;
                     break;
                 }
             }
             transaction.success();
             return result;
         } finally {
             transaction.finish();
         }
     }
 
 	/**
 	 * Find or create a RubyProject
 	 * 
 	 * @param project
 	 *            awe project node awe project node
 	 * @param rubyProjectName
 	 *            ruby project name ruby project name
 	 * @return RubyProjectNode
 	 */
 	public RubyProjectNode findOrCreateRubyProject(AweProjectNode project,
 			String rubyProjectName) {
 		assert project != null;
 		assert rubyProjectName != null;
 		RubyProjectNode result = null;
 		Transaction transaction = neoService.beginTx();
 		try {
 			result = findRubyProject(project, rubyProjectName);
 			if (result == null) {
 				result = new RubyProjectNode(neoService.createNode(),rubyProjectName);
 				project.addRubyProject(result);
 			}
 			transaction.success();
 			return result;
 		} finally {		    
 			transaction.finish();			
 		}
 	}
 
 	/**
 	 * Find or create Awe Project
 	 * 
 	 * @param aweProjectName
 	 *            Awe project name
 	 * @return AweProjectNode
 	 */
 	public AweProjectNode findOrCreateAweProject(String aweProjectName) {
 		assert aweProjectName != null;
 		AweProjectNode result = null;
 		Transaction transaction = neoService.beginTx();
 		try {
 		    //Lagutko, 13.08.2009, use findAweProject() method to find an AWEProjectNode
 		    result = findAweProject(aweProjectName);
 			if (result == null) {
 				result = createEmptyAweProject(aweProjectName);
 			}
 			transaction.success();
 			return result;
 		} finally {
 			transaction.finish();
 		}
 	}
 	
 	/**
 	 * Creates an AWE Project Node without Name
 	 *
 	 * @return created AWE ProjectNode
 	 */
 	public AweProjectNode createEmptyAweProject(String projectName) {
 	    AweProjectNode result = null;
 	    RootNode root = getRootNode();
 	    
 	    Transaction transaction = neoService.beginTx();
 	    try {
 	        result = new AweProjectNode(neoService.createNode(), projectName);
 	        root.addProject(result);
 	        transaction.success();
 	    }
 	    finally {
 	        transaction.finish();
 	    }
 	    
 	    return result;
 	}
 	
 	/**
 	 * Creates a Ruby Project without parent AWE Project
 	 *
 	 * @param projectName name of Project 
 	 * @return created Ruby Project Node
 	 */
 	public RubyProjectNode createEmptyRubyProject(String projectName) {
 	    RubyProjectNode result = null;
 	    
 	    Transaction transaction = neoService.beginTx();
 	    try {
 	        result = new RubyProjectNode(neoService.createNode(),projectName);
 	        
 	        transaction.success();
 	    }
 	    finally {
 	        transaction.finish();
 	    }
 	    
 	    return result;
 	}
 	
 	/**
 	 * Search for AWEProjectNode in database by given Name
 	 *
 	 * @param aweProjectName name of AWE Project
 	 * @return AWEProjectNode
 	 * @author lagutko_n
 	 */
 	public AweProjectNode findAweProject(String aweProjectName) {
 	    assert aweProjectName != null;
         AweProjectNode result = null;
         RootNode root = getRootNode();
         Transaction transaction = neoService.beginTx();
         try {
             Iterator<AweProjectNode> aweProjects = root.getAllProjects();
             while (aweProjects.hasNext()) {
                 AweProjectNode aweProject = aweProjects.next();
 
                 if (aweProjectName.equals(aweProject.getName())) {
                     result = aweProject;
                     break;
                 }
             }            
             transaction.success();
             return result;
         } finally {
             transaction.finish();
         }
 	}
 
 	/**
 	 * Delete Node and all depends nodes from bd
 	 * 
 	 * @param node
 	 *            node to delete
 	 */
 	public void deleteNode(AbstractNode node) {
         deleteNode(node.getUnderlyingNode());
 	}
 
     /**
      * Delete Node and all depends nodes from database
      * 
      * @param node node to delete
      */
     public void deleteNode(Node node) {
         Transaction transaction = neoService.beginTx();
         try {
             LinkedList<Node> nodeToDelete = new LinkedList<Node>();
             nodeToDelete.add(node);
             for (int i = 0; i < nodeToDelete.size(); i++) {
                 Node deleteNode = nodeToDelete.get(i);
                 Iterator<Relationship> relations = deleteNode.getRelationships(Direction.BOTH).iterator();
                 while (relations.hasNext()) {
                     Relationship relationship = relations.next();
                     if (relationship.getStartNode().equals(deleteNode)) {
                         Node endNode = relationship.getEndNode();
                         if (!nodeToDelete.contains(endNode))
                             nodeToDelete.addLast(endNode);
                     }
                     relationship.delete();
                 }
                 deleteNode.delete();
             }
             transaction.success();
         } finally {
             transaction.finish();
         }
 
     }
 	/**
 	 * Find script node
 	 * 
 	 * @param rubyProject
 	 *            RubyProjectNode
 	 * @param scriptName
 	 *            script name
 	 * @return RubyScriptNode or null
 	 */
 	public RubyScriptNode findScript(RubyProjectNode rubyProject,
 			String scriptName) {
 		Transaction transaction = neoService.beginTx();
 		try {
 			Iterator<RubyScriptNode> scripts = rubyProject.getScripts();
 			while (scripts.hasNext()) {
 				RubyScriptNode rubyScriptNode = (RubyScriptNode) scripts.next();
 				if (scriptName.equals(rubyScriptNode.getName())) {
 					transaction.success();
 					return rubyScriptNode;
 				}
 			}
 			transaction.success();
 			return null;
 		} finally {
 			transaction.finish();
 		}
 
 	}
 
 	/**
 	 * Create script node
 	 * 
 	 * @param cellNode
 	 *            cell node
 	 * @param scriptName
 	 *            script name
 	 * @return created script node
 	 */
 	public RubyScriptNode createScript(CellNode cellNode, String scriptName) {
 
 		RubyProjectNode rubyProject = getRubyProject(cellNode);
 		RubyScriptNode result = findScript(rubyProject, scriptName);
 		if (result != null) {
 			String message = SplashDatabaseExceptionMessages
 					.getFormattedString(
 							SplashDatabaseExceptionMessages.Service_Method_Exception,
 							"createScript");
 			NeoCorePlugin.error(message, new SplashDatabaseException(message));
 			return null;
 		}
 		Transaction transaction = neoService.beginTx();
 		try {
 			result = new RubyScriptNode(neoService.createNode(),scriptName);
 			rubyProject.addScript(result);
 			result.addCell(cellNode);
 			transaction.success();
 			return result;
 		} finally {
 			transaction.finish();
 		}
 
 	}
 
 	/**
 	 * Get RubyProject node from cell node
 	 * 
 	 * @param cellNode
 	 *            cell node
 	 * @return RubyProjectNode
 	 */
 	private RubyProjectNode getRubyProject(CellNode cellNode) {
 		Node spreadSheetNode = getSpreadSheet(cellNode);
 		Iterator<Node> iterator = spreadSheetNode.traverse(Order.BREADTH_FIRST,
 				StopEvaluator.DEPTH_ONE,
 				ReturnableEvaluator.ALL_BUT_START_NODE,
 				SplashRelationshipTypes.SPREADSHEET, Direction.INCOMING)
 				.iterator();
 		Node rubyProjectNode = iterator.next();
 		return RubyProjectNode.fromNode(rubyProjectNode);
 	}
 
 	/**
 	 * Get SpreadSheet from cell node
 	 * 
 	 * @param cellNode
 	 *            cell node
 	 * @return wrapper spreadsheet node
 	 */
 	public SpreadsheetNode getSpreadsheetByCell(CellNode cellNode) {
 		Transaction transaction = neoService.beginTx();
 		try {
 			Node result = getSpreadSheet(cellNode);
 			transaction.success();
 			return result == null ? null : SpreadsheetNode.fromNode(result);
 		} finally {
 			transaction.finish();
 		}
 	}
 
 	/**
 	 * Get SpreadSheet from cell node
 	 * 
 	 * @param cellNode
 	 *            cell node
 	 * @return SpreadSheet node
 	 */
 	private Node getSpreadSheet(CellNode cellNode) {
 		Node rowNode = cellNode.getRow().getUnderlyingNode();
 		Node spreadSheetNode = rowNode.traverse(Order.BREADTH_FIRST,
 				StopEvaluator.DEPTH_ONE,
 				ReturnableEvaluator.ALL_BUT_START_NODE,
 				SplashRelationshipTypes.ROW, Direction.INCOMING).iterator()
 				.next();
 		return spreadSheetNode;
 	}
 
 	/**
 	 * Find Cell depends from script
 	 * 
 	 * @param script
 	 *            script node
 	 * @return CellNode or null
 	 */
 	public CellNode findCellByScriptReference(RubyScriptNode script) {
 		Transaction transaction = neoService.beginTx();
 		try {
 			Iterator<Node> cellIterator = script.getUnderlyingNode().traverse(
 					Order.BREADTH_FIRST, StopEvaluator.DEPTH_ONE,
 					ReturnableEvaluator.ALL_BUT_START_NODE,
 					SplashRelationshipTypes.SCRIPT_CELL, Direction.OUTGOING)
 					.iterator();
 			CellNode result = null;
 			if (cellIterator.hasNext()) {
 				result = new CellNode(cellIterator.next());
 			}
 			transaction.success();
 			return result;
 		} finally {
 			transaction.finish();
 		}
 	}
 	
 	/**
 	 * Computes a Root Project of Spreadsheet
 	 *
 	 * @param spreadsheet spreadsheet Node
 	 * @return RubyProjectNode of this Spreadsheet
 	 */
 	
 	public RubyProjectNode getSpreadsheetRoot(SpreadsheetNode spreadsheet) {
 	    RubyProjectNode result = null;
 	    Transaction transaction = neoService.beginTx();
 	    try {
 	        result = spreadsheet.getSpreadsheetRootProject();
 	        
 	        transaction.success();
 	    } 
 	    finally {
 	        transaction.finish();
 	    }
 	    
 	    return result;
 	}
 	
 	/**
 	 * Renames AWE Project Node
 	 *
 	 * @param aweProjectName old Name of AWE Project
 	 * @param newName new Name of AWE Project
 	 * @author Lagutko_N
 	 */
 	public void renameAweProject(String aweProjectName, String newName) {
 	    Transaction transaction = neoService.beginTx();
 	    
 	    try {
 	        AweProjectNode aweProject = findAweProject(aweProjectName);
 	        
 	        if (aweProject != null) {
 	            aweProject.setName(newName);
 	        }
 	        
 	        transaction.success();
 	    }
 	    finally {
 	        transaction.finish();
 	    }
 	}
 	
 	/**
 	 * Renames Ruby Project Node
 	 *
 	 * @param aweProjectName name of AWE Project that contains Ruby Project
 	 * @param oldName old Name of Ruby Project
 	 * @param newName new Name of Ruby Project
 	 * @author Lagutko_N
 	 */
 	public void renameRubyProject(String aweProjectName, String oldName, String newName) {
 	    Transaction transacation = neoService.beginTx();
 	    
 	    try {
 	        AweProjectNode aweProject = findAweProject(aweProjectName);
 	        if (aweProject != null) {
 	            RubyProjectNode rubyProject = findRubyProject(aweProject, oldName);
 	            
 	            rubyProject.setName(newName);
 	        }
 	        transacation.success();
 	    }
 	    finally {
 	        transacation.finish();
 	    }
 	}
 
 	/**
 	 * Renames Spreadsheet Node in Database
 	 *
 	 * @param rubyProjectNode node of parent Ruby Project
 	 * @param oldName old Name of Spreadsheet
 	 * @param newName new Name of Spreadsheet
 	 */
 	public void renameSpreadsheet(RubyProjectNode rubyProjectNode, String oldName, String newName) {
 	    Transaction transacation = neoService.beginTx();
         
         try {
             SpreadsheetNode spreadsheet = findSpreadsheet(rubyProjectNode, oldName);
             if (spreadsheet != null) {
                 spreadsheet.setSpreadsheetName(newName);
             }
             transacation.success();
         }
         finally {
             transacation.finish();
         }
 
         //commit changes after renaming. Commit creates an event that will be catched in 
         //Splash Editor and Editor will change its name
         provider.commit();
 	}
 
     /**
      * Adds network node to necessary awe project node
      * 
      * @param aweProjectName name of awe project
      * @param data network node
      */
     public void addDataNodeToProject(String aweProjectName, Node data) {
         if (aweProjectName == null || data == null) {
             return;
         }
         Transaction transacation = neoService.beginTx();
 
         try {
             AweProjectNode project = findOrCreateAweProject(aweProjectName);
             project.addChildNode(data);
             transacation.success();
         } finally {
             transacation.finish();
         }
     }
 
     /**
      * return Traverser for all dataset node in database
      * 
      * @param root - start node to find
      */
     public Traverser getAllDatasetTraverser(Node root) {
         return root.traverse(Order.DEPTH_FIRST, new StopEvaluator() {
 
             @Override
             public boolean isStopNode(TraversalPosition currentPos) {
                 return currentPos.depth() > 3;
             }
         }, new ReturnableEvaluator() {
 
             @Override
             public boolean isReturnableNode(TraversalPosition currentPos) {
                 Node node = currentPos.currentNode();
                 return node.hasProperty(INeoConstants.PROPERTY_TYPE_NAME)
                         && node.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals(INeoConstants.DATASET_TYPE_NAME);
             }
         }, SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
     }
 }
