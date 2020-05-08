 package compiler.abstracter;
 
 import compiler.graph.*;
 import compiler.combinators.CombinatorManager;
 import compiler.combinators.Lambda;
 import compiler.combinators.Var;
 
 /**
  * Classe réalisant l'abstraction d'une expression
  * @author kazmiero
  *
  */
 
 public class Abstracter {
 	
 	private Node abstractedGraph;
 	
 	public Abstracter(Node expression){
 		abstractedGraph = expression;
 		
 	}
 	
 	/**
 	 * Réalise l'abstraction de l'ensemble du graphe puis le renvoie
 	 * @return graphe après abstraction
 	 */
 	public Node getAbstractedGraph(){
 		abstractedGraph = findAbstracter(abstractedGraph).getRoot();
 		return abstractedGraph;
 	}
 	
 	
 	/**
 	 * Cherche des abstracters dans le graphe à partir de la fin de l'expression (associativité à gauche) et réalise les abstractions correspondantes
 	 * Il est important que les abstractions les plus profondes dans le graphe soient effectuées en premier.
 	 * @return graphe après abstraction
 	 */
 	public Node findAbstracter(Node expression){
 		
		CombinatorManager cmanager = CombinatorManager.getInstance();
		
 		Node lastNode = expression.getLastNode();
 		
 		Node node = lastNode;
 		
 		while(node.getFunction().getNode() != null){
 			
 			if(node.getArgument().getNode() != null){
 			
 				Node abstractedArgument = findAbstracter(node.getArgument().getNode()); 
 				node.setArgument(NodeFieldFactory.create(abstractedArgument));
 				
 			}
 			
 			else if(node.getArgument().getCombinator() instanceof Var && node.getFunction().getNode().getArgument().getCombinator() instanceof Lambda){
 				
 				Var var = (Var) node.getArgument().getCombinator();
 				Lambda lambda = (Lambda) node.getFunction().getNode().getArgument().getCombinator();
 				// on coupe la connection au reste du graphe
 				node.getNextNode().setFunction(new NodeNodeField(null));
				
				// ajout d'un I dans le cas de parenthèses litigieuses
				if(node.getNextNode().getArgument().getCombinator() == null)
					node.getNextNode().setFunction(NodeFieldFactory.create(cmanager.get("I")));
					
 				Node abstractedSubGraph = abstraction(node.getNextNode(), lambda.getLevel(), var);
 				
 				node = node.getFunction().getNode();
 				node.setArgument(abstractedSubGraph.getFunction());
 				abstractedSubGraph.setFunction(NodeFieldFactory.create(node));
 				node.setNextNode(abstractedSubGraph);
 				
 			}
 			
 			node = node.getFunction().getNode();
 		}
 		
 		// racine
 		if(node.getFunction().getCombinator() instanceof Lambda && node.getArgument().getCombinator() instanceof Var){
 			
 			Var var = (Var) node.getArgument().getCombinator();
 			Lambda lambda = (Lambda) node.getFunction().getCombinator();
 			// on coupe la connection au reste du graphe
 			node.getNextNode().setFunction(new NodeNodeField(null));
			
			// ajout d'un I dans le cas de parenthèses litigieuses
			if(node.getNextNode().getArgument().getCombinator() == null)
				node.getNextNode().setFunction(NodeFieldFactory.create(cmanager.get("I")));
			
 			Node abstractedGraph = abstraction(node.getNextNode(), lambda.getLevel(), var);
 			node = abstractedGraph;
 		}
 		
 		else if(node.getArgument().getNode() != null){
 			Node abstractedArgument = findAbstracter(node.getArgument().getNode()); 
 			node.setArgument(NodeFieldFactory.create(abstractedArgument));
 		}
 
 		return node.getLastNode();
 		
 	}
 	
 	/**
 	 * Réalise l'abstraction du graphe donné par 'expression', au niveau (nombre de +) 'level', pour la variable 'var'
 	 * la fonction findAbstracter assure qu'il s'agit d'une abstraction simple (à une seule variable)
 	 * Les abstractions lambda+, lambda++ et lambda+++ sont implémentées.
 	 * @return expression abstraite
 	 */
 	public Node abstraction(Node expression, int level, Var var){
 		
 		CombinatorManager cmanager = CombinatorManager.getInstance();
 		NodeField nfS = NodeFieldFactory.create(cmanager.get("S"));
 		
 		Node root = expression.getRoot();
 		
 		// réécriture du graphe de manière plus pratique
 		if(root.getFunction().getCombinator() != null){
 			Node newRoot = new Node(new NodeNodeField(null), NodeFieldFactory.create(root.getFunction().getCombinator()));
 			root.setFunction(NodeFieldFactory.create(newRoot));
 			newRoot.setNextNode(root);
 			root = newRoot;
 		}		
 		
 		Node lastNode = root.getLastNode();
 		Node currentNode = lastNode;
 		
 		// un seul combinateur
 		if(lastNode.equals(root)){
 			
 			if(root.getArgument().getCombinator() != null && root.getArgument().getCombinator().equals(var))
 				return new Node(NodeFieldFactory.create(cmanager.get("I")),NodeFieldFactory.create(cmanager.get("I")));
 			else
 				return new Node(NodeFieldFactory.create(cmanager.get("K")),NodeFieldFactory.create(root.getArgument().getCombinator()));
 			
 		}
 		
 		// règle lambda++++x . F ( G x ) = B F G
 		/*
 		if(level >= 4 && lastNode.getArgument().getCombinator() == null)
 			if(lastNode.getArgument().getNode().getArgument().getCombinator() != null && lastNode.getArgument().getNode().getArgument().getCombinator().equals(var)){
 				
 				Node subgraphNode = lastNode.getArgument().getNode();
 				
 				Node gNode = null;
 				if(subgraphNode.getFunction().getNode() == null && !subgraphNode.getFunction().getCombinator().equals(var))
 					gNode = new Node(new NodeNodeField(null), NodeFieldFactory.create(subgraphNode.getFunction().getCombinator()));
 				else if(searchVariable(subgraphNode.getFunction().getNode(), var) == null){
 					subgraphNode.getFunction().getNode().setNextNode(null);
 					gNode = new Node(new NodeNodeField(null), NodeFieldFactory.create(subgraphNode.getFunction().getNode()));
 				}
 				
 				if(gNode != null){
 					
 					NodeField fNodeField = null;
 					Node bNode = null;
 					
 					currentNode = lastNode.getFunction().getNode();
 					
 					if(currentNode.equals(root) && !currentNode.getArgument().getCombinator().equals(var)){
 						fNodeField = currentNode.getArgument();
 						bNode = new Node(NodeFieldFactory.create(cmanager.get("B")),fNodeField);
 						gNode.setFunction(NodeFieldFactory.create(bNode));
 						return gNode;
 					}
 					
 					Node varNode = searchVariable(currentNode,var);
 					
 					if(varNode == null){
 							
 						root.getNextNode().setFunction(root.getArgument());
 						currentNode.setNextNode(null);
 						fNodeField = NodeFieldFactory.create(currentNode);
 						bNode = new Node(NodeFieldFactory.create(cmanager.get("B")),fNodeField);
 						gNode.setFunction(NodeFieldFactory.create(bNode));
 						return gNode;
 							
 					}
 					
 				}
 					
 			}
 		*/
 		
 		//règle lambda+++x . F = K F
 		if(level >= 3 && (lastNode.getArgument().getCombinator() == null || !lastNode.getArgument().getCombinator().equals(var))){
 			
 			currentNode = searchVariable(lastNode,var);
 			
 			if(currentNode == null){
 				
 				if(root.getNextNode() == null){
 					root.setFunction(NodeFieldFactory.create(cmanager.get("K")));
 					return root.getLastNode();
 				}
 				
 				root.getNextNode().setFunction(root.getArgument());
 				
 				Node newNode = new Node(NodeFieldFactory.create(cmanager.get("K")),NodeFieldFactory.create(lastNode));
 				return newNode.getLastNode();
 			}
 			
 			if(currentNode.equals(lastNode)){
 				
 				lastNode.getFunction().getNode().setNextNode(null);
 				Node firstNode = new Node(nfS,abstractNodeField(lastNode.getFunction(),level,var));
 				lastNode.setArgument(abstractNodeField(lastNode.getArgument(),level,var));
 				lastNode.setFunction(NodeFieldFactory.create(firstNode));
 				firstNode.setNextNode(lastNode);
 				
 				return firstNode.getLastNode();
 			}
 			
 			Node nextNode = currentNode.getNextNode();
 			currentNode.setNextNode(null);
 			
 			Node newNode = new Node(nfS, abstractNodeField(NodeFieldFactory.create(currentNode),level,var));
 			 
 			Node kNode = new Node(NodeFieldFactory.create(cmanager.get("K")));
 			
 			if(nextNode.equals(lastNode))
 				kNode.setArgument(lastNode.getArgument());
 			else{
 				nextNode.getNextNode().setFunction(nextNode.getArgument());
 				kNode.setArgument(NodeFieldFactory.create(lastNode));
 			}
 			
 			new Node(NodeFieldFactory.create(newNode),NodeFieldFactory.create(kNode));
 			
 			return newNode.getLastNode();
 			
 		}
 		
 		
 		
 		// règle lambda++ x . F x = F
 		if(level >= 2 && lastNode.getArgument().getCombinator() != null && lastNode.getArgument().getCombinator().equals(var)){
 			
 			currentNode = searchVariable(lastNode.getFunction().getNode(),var);		
 		
 			if(currentNode == null){
 				
 				if(root.getNextNode().equals(lastNode))
 					return new Node(NodeFieldFactory.create(cmanager.get("I")), NodeFieldFactory.create(root.getArgument().getCombinator()));
 				
 				root.getNextNode().setFunction(root.getArgument());
 				lastNode.getFunction().getNode().setNextNode(null);
 				return lastNode.getFunction().getNode();
 			}
 			
 			currentNode = currentNode.getNextNode();
 			
 			if(!currentNode.equals(lastNode)){
 				
 				lastNode.getFunction().getNode().setNextNode(null);
 				lastNode = lastNode.getFunction().getNode();
 				currentNode.getFunction().getNode().setNextNode(null);
 				Node newNode = new Node(nfS, abstractNodeField(currentNode.getFunction(),level,var));
 				
 				Node parNode;
 				if(currentNode.equals(lastNode))
 					new Node(NodeFieldFactory.create(newNode),NodeFieldFactory.create(currentNode.getArgument().getCombinator()));
 				else {
 					parNode = new Node(NodeFieldFactory.create(newNode));
 					currentNode.getNextNode().setFunction(currentNode.getArgument());
 					parNode.setArgument(NodeFieldFactory.create(lastNode));
 				}
 				
 				return newNode.getLastNode();
 			}
 				
 		}
 		
 		
 		// règle lambda+ x . P Q = S (lambda+ x . P) (lambda+ x . Q)
 		if(lastNode.getFunction().getNode().equals(root)){
 	
 			lastNode.setArgument(abstractNodeField(lastNode.getArgument(),level,var));
 			Node firstNode = new Node(nfS,abstractNodeField(root.getArgument(),level,var));
 			lastNode.setFunction(NodeFieldFactory.create(firstNode));
 			firstNode.setNextNode(lastNode);
 			
 			return firstNode.getLastNode();
 			
 		}
 		
 		else{
 			
 			lastNode.getFunction().getNode().setNextNode(null);
 			Node firstNode = new Node(nfS,abstractNodeField(lastNode.getFunction(),level,var));
 			lastNode.setArgument(abstractNodeField(lastNode.getArgument(),level,var));
 			lastNode.setFunction(NodeFieldFactory.create(firstNode));
 			firstNode.setNextNode(lastNode);
 			
 			return firstNode.getLastNode();
 		}
 		
 		
 	}
 	
 	/**
 	 * Réalise l'abstraction (lambda+) d'un NodeField.
 	 * En cas de NodeNodeField (parenthèses), demande l'astraction du Node correspondant.
 	 *
 	 * @return abstracted NodeField
 	 */
 	private NodeField abstractNodeField(NodeField nf, int level, Var var){
 
 		CombinatorManager cmanager = CombinatorManager.getInstance();
 		
 		if(nf.getCombinator() != null){
 			if(nf.getCombinator().equals(var))
 				return NodeFieldFactory.create(cmanager.get("I"));
 			else
 				return NodeFieldFactory.create(new Node(NodeFieldFactory.create(cmanager.get("K")),NodeFieldFactory.create(nf.getCombinator())));
 		}
 		else {//cas des parenthèses
 			Node node = abstraction(nf.getNode(),level,var);
 			if(node.getFunction().getCombinator() != null && node.getFunction().getCombinator().equals(cmanager.get("I")))
 				return NodeFieldFactory.create(node.getArgument().getCombinator());
 			else
 				return NodeFieldFactory.create(node);
 			
 		}
 		
 	}
 	
 	
 	/**
 	 * Parcourt le graphe à l'envers jusqu'à ce que la variable soit trouvée.
 	 * Utile pour lambda+ et lambda++
 	 *
 	 * @return premier Node contenant var. null si var n'a pas été trouvée
 	 */
 	public Node searchVariable(Node start, Var var){
 		
 		Node node = start;
 	
 		do{
 			
 			if(node.getArgument().getNode() != null)
 				if(searchVariable(node.getArgument().getNode(),var) != null)
 					return node;
 			
 			if(node.getArgument().getCombinator() != null && node.getArgument().getCombinator().equals(var))
 				return node;
 			
 			if(node.getFunction().getCombinator() != null && node.getFunction().getCombinator().equals(var))
 				return node;
 			
 			node = node.getFunction().getNode();
 		}
 		while(node != null);
 		// ici rien n'a été trouvé
 		return null;
 	}
 
 }
