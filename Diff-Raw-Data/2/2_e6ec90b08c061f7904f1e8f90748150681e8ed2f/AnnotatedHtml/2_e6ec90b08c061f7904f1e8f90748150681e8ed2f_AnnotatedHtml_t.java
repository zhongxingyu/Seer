 package models.annotationHtml;
 
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.nodes.TagNode;
 import org.htmlparser.nodes.TextNode;
 import org.htmlparser.tags.Span;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 
 public class AnnotatedHtml 
 {
 	private String htmlContent ;
 
 	public AnnotatedHtml(String newHtmlContent)
 	{
 		htmlContent = newHtmlContent ;
 	}
 	
 	public String getHtmlContent() {
 		return htmlContent;
 	}
 
 	public void setHtmlContent(String htmlContent) {
 		this.htmlContent = htmlContent;
 	}
 
 	public void highLight(SplitedXpointer start , SplitedXpointer end, String color, String annotationId) throws ParserException //throws ParserException
 	{
 		Parser parser = Parser.createParser(htmlContent , null);
 		NodeList nl = parser.parse(null);
 		//seeThroughNodeList(0, nl);
 		System.out.println("[AnnotatedHtml.highLight] nl size : " + nl.size());
 		Node startNode = findNode(nl, start);
 		System.out.println("[AnnotatedHtml.highLight] startNode content : " + startNode.toHtml());
 		Node endNode = findNode(nl, end);
 		System.out.println("[AnnotatedHtml.highLight] endNode content : " + endNode.toHtml());
 		addSpans(startNode, endNode, start.getIndice(), end.getIndice(), color, annotationId);
 		System.out.println("[AnnotatedHtml.highLight] modified html : " + nl.toHtml()) ;
 		//sets the new Annotated HtmlContent
 		htmlContent = nl.toHtml();
 	}
 	
 	private static NodeList addSpanInTextNode(TextNode node, int indiceStart, int indiceEnd, String color, String annotationId)
 	{
 		NodeList toreturn = new NodeList();
 		
 		String originalContent = node.getText() ;
 		String beforeSpan = originalContent.substring(0, indiceStart);
 		String contentAnnotated = originalContent.substring(indiceStart, indiceEnd);
 		String afterSpan = originalContent.substring(indiceEnd);
 		System.out.println("[AnnotatedHtml.addSpanInTextNode] beforeSpan : " + beforeSpan);
 		System.out.println("[AnnotatedHtml.addSpanInTextNode] contentAnnotated : " + contentAnnotated);
 		System.out.println("[AnnotatedHtml.addSpanInTextNode] afterSpan : " + afterSpan);
 		
 		Span annotationSpan = new Span();
 		TagNode endSpan = new TagNode();
 		endSpan.setTagName("/SPAN");
 		annotationSpan.setEndTag(endSpan);
		annotationSpan.setAttribute("id", "'annotationSpan-" + annotationId + "'");
 		annotationSpan.setAttribute("style", "'background-color:" + color + "'");
 		annotationSpan.setAttribute("class", "'annotated-coloration'");
 		NodeList toAdd = new NodeList();
 		toAdd.add(new TextNode(contentAnnotated));
 		annotationSpan.setChildren(toAdd);
 		System.out.println("[AnnotatedHtml.addSpanInTextNode] Span : " + annotationSpan.toHtml());
 		
 		toreturn.add(new TextNode(beforeSpan));
 		toreturn.add(annotationSpan);
 		toreturn.add(new TextNode(afterSpan));
 		
 		return toreturn ;
 	}
 	
 	private static void addSpanInANode(Node node, int indiceStart, int indiceEnd, String color, String annotationId)
 	{
 		System.out.println("[AnnotatedHtml.addSpanInANode]");
 		NodeList children = node.getChildren();
 		node.setChildren(null);
 		NodeList newChildren = new NodeList();
 		if(children != null)
 		{
 			System.out.println("[AnnotatedHtml.addSpanInANode] node has children");
 			int currentIndiceStart = indiceStart ;
 			int currentIndiceEnd = indiceEnd ;
 			int nbNodes = children.size() ;
 			int cptNode = 0 ;
 			boolean done = false ;
 			while(cptNode < nbNodes && !done )
 			{
 				System.out.println(" child : " + cptNode + " class : " + children.elementAt(cptNode).getClass() + " content : " + children.elementAt(cptNode).toHtml());
 				Node currentNode = children.elementAt(cptNode) ;
 				if(currentNode instanceof TextNode)
 				{
 					int contentLength = currentNode.getText().length() ;
 					// dans ce cas on n'annote pas mais on met à jour l'indice de début et l'indice de fin
 					if(contentLength < currentIndiceStart) 
 					{
 						currentIndiceStart = currentIndiceStart - contentLength ;
 						currentIndiceEnd = currentIndiceEnd - contentLength ;
 						newChildren.add(currentNode);
 					}
 					else //là va falloir annoter
 					{
 						//toute l'annotation est dans ce TextNode
 						if(contentLength > currentIndiceEnd) 
 						{
 							NodeList newNodes = addSpanInTextNode((TextNode)currentNode , currentIndiceStart, currentIndiceEnd, color, annotationId) ;
 							newChildren.add(newNodes);
 							done = true ;
 						}
 						else //là il va falloir continuer à annoter
 						{
 							NodeList newNodes = addSpanInTextNode((TextNode)currentNode , currentIndiceStart, contentLength, color, annotationId);
 							newChildren.add(newNodes);
 							currentIndiceStart = 0 ;
 							currentIndiceEnd = currentIndiceEnd - contentLength ;
 							if(currentIndiceEnd <= 0) 
 							{
 								done = true ;
 								System.out.println("[AnnotatedHtml.addSpanInANode] annotations done");
 							}
 						} 
 					}
 				}
 				cptNode ++ ;
 			}
 		}
 		else
 		{
 			if(node instanceof TextNode)
 			{
 				NodeList modifieds = addSpanInTextNode((TextNode)node, indiceStart, indiceEnd, color, annotationId);
 				//TODO needs to be tested ...
 				node.setChildren(modifieds);
 			}
 		}
 		node.setChildren(newChildren);
 		System.out.println("[AnnotatedHtml.addSpanInANode] new Node content : " + node.toHtml());
 	}
 	
 	//TODO ajouter les balises span
 	private static void addSpans(Node node1, Node node2, int indiceStart, int indiceEnd, String color, String annotationId)
 	{
 		//soit node1 == node2
 		if(node1.equals(node2))
 		{
 			System.out.println("[AnnotatedHtml.addSpans] same node");
 			addSpanInANode(node1, indiceStart, indiceEnd, color, annotationId);
 			System.out.println("[AnnotatedHtml.addSpans] same node, new node content : " + node1.toHtml());
 		}
 		else 
 		{
 			//soit node1 et node2 de même niveau
 			if(sameLevel(node1, node2))
 			{
 				System.out.println("[AnnotatedHtml.addSpans] node of same level in the html tree and node1 before node2");
 			}
 			else
 			{
 				if(sameLevel(node2, node1))
 				{
 					System.out.println("[AnnotatedHtml.addSpans] node of same level in the html tree and node2 before node1");
 				}
 				else
 				{
 					//soit node1 fils de node2
 					if(hasChild(node1 , node2))
 					{
 						System.out.println("[AnnotatedHtml.addSpans] node2 child of node1");
 					}
 					else
 					{
 						//soit node2 fils de node1
 						if(hasChild(node2 , node1))
 						{
 							System.out.println("[AnnotatedHtml.addSpans] node1 child of node2");
 						}
 						else
 						{
 							//soit node1 et node2 pas de même niveau et pas fils l'un de l'autre
 							System.out.println("[AnnotatedHtml.addSpans] not child one of another");
 							//dans ce cas il faut trouver l'ensemble des noeuds entre les deux noeuds et il faut l'ordre entre start et end
 						}
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * Tests if node2 is in the same level than node1 but after him in the DOM.
 	 * @param node1
 	 * @param node2
 	 * @return boolean
 	 */
 	public static boolean sameLevel(Node node1, Node node2)
 	{
 		boolean found = false ;
 		Node currentNode = node1 ;
 		Node nextSibling = currentNode.getNextSibling() ;
 		while(nextSibling != null && !found)
 		{
 			if(nextSibling.equals(node2)) found = true ;
 			nextSibling = nextSibling.getNextSibling();
 		}
 		return found ;
 	}
 	/**
 	 * Tests if node1 has node2 as a child. Recursive method.
 	 * @param node1
 	 * @param node2
 	 * @return boolean
 	 */
 	public static boolean hasChild(Node node1, Node node2)
 	{
 		boolean found = false ;
 		NodeList children = node1.getChildren() ;
 		if(children != null)
 		{
 			int nbChild = children.size();
 			Node currentChild ;
 			int cptChild = 0 ;
 			while(cptChild < nbChild)
 			{
 				currentChild = children.elementAt(cptChild);
 				if(currentChild.equals(node2)) found = true ;
 				else
 				{
 					found = hasChild(currentChild , node2);
 				}
 				cptChild ++ ;
 			}
 			
 		}
 		return found ;
 	}
 	
 	/**
 	 * Usefull for debuging HTMLTreeNode
 	 * @param level recursive param, level in the tree
 	 * @param nl one level NodeList
 	 */
 	public static void seeThroughNodeList(int level, NodeList nl)
 	{
 		int size = 0;
 		if(nl != null) size = nl.size();
 		for(int i= 0 ; i < size ; i++)
 		{
 			System.out.println("level : " + level + " elt nb : " + i);
 			Node node = nl.elementAt(i);
 			System.out.println(node.toHtml());
 			seeThroughNodeList(level + 1 , node.getChildren());
 		}
 	}
 	
 	private Node findNode(NodeList nl , SplitedXpointer splitedXpointer)
 	{
 		int nbNode = splitedXpointer.getXpointerTree().length ;
 		NodeList currentNodeList = nl ;
 		//TODO changer ce comportement
 		//dans notre contexte le contenu d'un article ou d'une annotation est encadré par une balise div d'id commençant par article- ou annotation-
 		int cptNode = 0 ;
 		if(splitedXpointer.getXpointerTree()[0].contains("article-")||splitedXpointer.getXpointerTree()[0].contains("annotation-"))
 		{
 			cptNode ++ ;
 		}
 		//fin du comportement propre à notre contexte
 		
 		while(cptNode < nbNode -1)
 		{
 			System.out.println("[AnnotatedHtml.findNode] cptNode : " + cptNode);
 			String info = splitedXpointer.getXpointerTree()[cptNode];
 			try{// Pour l'instant on ne fabrique que des Xpointer avec des int après l'id de la ressource
 				Integer nextSibling = Integer.parseInt(info);
 				System.out.println("[AnnotatedHtml.findNode] nextSibling : " + nextSibling);
 				//Attention, on ne peut pas juste aller chercher l'élément à nextSibling car la lib htmlParser compte les TextNode
 				//Donc il faut itérer et ignorer les TextNode
 				int cptGlobal = 0;
 				int cptWhithoutTextNode = 0 ;
 				while(cptWhithoutTextNode < nextSibling )
 				{
 					System.out.println("[AnnotatedHtml.findNode] in while next node : " + nl.elementAt(cptGlobal).toHtml());
 					if(! (nl.elementAt(cptGlobal) instanceof TextNode)) cptWhithoutTextNode ++ ;
 					cptGlobal ++ ;
 					System.out.println("[AnnotatedHtml.findNode] cptWithoutTexteNode : " + cptWhithoutTextNode + " cptGlobal : " + cptGlobal);
 				}
 				Node testTextNode = currentNodeList.elementAt(cptGlobal) ;
 				while(testTextNode instanceof TextNode) 
 				{
 					cptGlobal ++ ;
 					testTextNode = currentNodeList.elementAt(cptGlobal) ;
 				}
 				System.out.println("[AnnotatedHtml.findNode] cptGlobal : " + cptGlobal);
 				currentNodeList = testTextNode.getChildren();
 			}
 			catch (NumberFormatException exception) // La syntaxe Xpointer permet d'autres cibles que des int
 			{
 				exception.printStackTrace();
 				System.out.println("[AnnotatedHtml.findNode] nextSibling is not an integer !!!");
 				//TODO gérer les autres syntaxes XPointer
 			}
 			cptNode ++ ;
 		}
 		//il nous reste le dernier à parcourir pour récupérer le noeud
 		Node currentNode = currentNodeList.elementAt(0) ;
 		String info = splitedXpointer.getXpointerTree()[cptNode];
 		try{// Pour l'instant on ne fabrique que des Xpointer avec des int après l'id de la ressource
 			Integer nextSibling = Integer.parseInt(info);
 			int cptGlobal = 0;
 			int cptWhithoutTextNode = 0 ;
 			while(cptWhithoutTextNode < nextSibling )
 			{
 				if(! (nl.elementAt(cptGlobal) instanceof TextNode)) cptWhithoutTextNode ++ ;
 				cptGlobal ++ ;
 			}
 			currentNode = currentNodeList.elementAt(cptGlobal) ;
 		}
 		catch (NumberFormatException exception) // La syntaxe Xpointer permet d'autres cibles que des int
 		{
 			exception.printStackTrace();
 			System.out.println("[AnnotatedHtml.findNode] nextSibling is not an integer !!!");
 			//TODO gérer les autres syntaxes XPointer
 		}
 		//return currentNodeList.elementAt(0) ;
 		return currentNode ;
 	}
 }
