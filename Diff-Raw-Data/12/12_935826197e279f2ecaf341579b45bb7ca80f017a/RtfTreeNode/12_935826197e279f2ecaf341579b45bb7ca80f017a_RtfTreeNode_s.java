 /********************************************************************************
  *   This file is part of NRtfTree Library.
  *
  *   JRtfTree Library is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation; either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   JRtfTree Library is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with this program. If not, see <http://www.gnu.org/licenses/>.
  ********************************************************************************/
 
 /********************************************************************************
  * Library:		JRtfTree
  * Version:     v0.3
  * Date:		15/11/2012
  * Copyright:   2006-2012 Salvador Gomez
  * Home Page:	http://www.sgoliver.net
  * GitHub:		https://github.com/sgolivernet/jrtftree
  * Class:		RtfTreeNode
  * Description:	Representa un documento RTF en forma de rbol.
  * ******************************************************************************/
 
 package net.sgoliver.jrtftree.core;
 
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 
 /**
  * Nodo RTF de la representacin en rbol de un documento. 
  */
 public class RtfTreeNode //In Sync
 {
 	//Atributos
 	private int nodeType;
 	private String key;
 	private boolean hasParam;
 	private int param;
 	private RtfNodeCollection children;
 	private RtfTreeNode parent;
 	private RtfTreeNode root;
 	private RtfTree tree;
 	
 	//Constructores
 	
 	/**
 	 * Constructor por defecto de la clase.
 	 */
     public RtfTreeNode()
     {
         this.children = null;
 
         this.nodeType = RtfNodeType.NONE;
         this.key = "";
         this.hasParam = false;
         this.param = 0;
 
         this.parent = null;
         this.root = null;
         this.tree = null;
     }
 
     /**
      * Constructor de la clase. Crea un nodo vaco del tipo pasado como parmetro.
      * @param nodeType Tipo de nodo a crear.
      */
     public RtfTreeNode(int nodeType)
     {
         this.nodeType = nodeType;
         this.key = "";
         this.hasParam = false;
         this.param = 0;
 
         this.parent = null;
         this.tree = null;
         
         if(nodeType == RtfNodeType.GROUP || nodeType == RtfNodeType.ROOT)
         	this.children = new RtfNodeCollection();
         else
         	this.children = null;
         
         if (nodeType == RtfNodeType.ROOT)
             this.root = this;
         else
         	this.root = null;
     }
 
     /**
      * Constructor de la clase. Crea un nodo completo con los datos suministrados como parametros.
      * @param nodeType Tipo de nodo.
      * @param key Clave del nodo.
      * @param hasParam Indicativo de existencia de parmetro.
      * @param param Parmetro del nodo, en caso de existir.
      */
     public RtfTreeNode(int nodeType, String key, boolean hasParam, int param)
     {
         this.nodeType = nodeType;
         this.key = key;
         this.hasParam = hasParam;
         this.param = param;
         
         this.parent = null;
         this.tree = null;
         
         if(nodeType == RtfNodeType.GROUP || nodeType == RtfNodeType.ROOT)
         	this.children = new RtfNodeCollection();
         else
         	this.children = null;
 
         if (nodeType == RtfNodeType.ROOT)
             this.root = this;
         else
         	this.root = null;
     }
     
     /**
      * Constructor privado de la clase. Crea un nodo a partir de un token del analizador lxico.
      * @param token Token RTF devuelto por el analizador lxico.
      */
     protected RtfTreeNode(RtfToken token)
     {
         this.nodeType = token.getTokenType();
         this.key = token.getKey();
         this.hasParam = token.getHasParam();
         this.param = token.getParam();
 
         this.children = null;
         this.parent = null;
         this.root = null;
         this.tree = null;
     }
     
     //Mtodos Pblicos
     
     /**
      * Aade un nodo al final de la lista de hijos.
      * @param newNode Nuevo nodo a aadir.
      */
     public void appendChild(RtfTreeNode newNode)
     {
     	if(newNode != null)
 		{
     		//Si an no tena hijos se inicializa la coleccin
             if (children == null)
                 children = new RtfNodeCollection();
     		
 	        //Se asigna como nodo padre el nodo actual
 	        newNode.parent = this;
 	
 	        //Se actualizan las propiedades Root y Tree del nuevo nodo y sus posibles hijos
             updateNodeRoot(newNode);
 	
 				        //Se aade el nuevo nodo al final de la lista de nodos hijo
 				        children.add(newNode);
 		}
     }
     
     /**
      * Inserta un nuevo nodo en una posicin determinada de la lista de hijos.
      * @param index Posicin en la que se insertar el nodo.
      * @param newNode Nuevo nodo a insertar.
      */
     public void insertChild(int index, RtfTreeNode newNode)
     {
         if (newNode != null)
         {
             //Si an no tena hijos se inicializa la coleccin
             if (children == null)
                 children = new RtfNodeCollection();
 
             if (index >= 0 && index <= children.size())
             {
                 //Se asigna como nodo padre el nodo actual
                 newNode.parent = this;
 
                 //Se actualizan las propiedades Root y Tree del nuevo nodo y sus posibles hijos
                 updateNodeRoot(newNode);
 
                 //Se aade el nuevo nodo al final de la lista de nodos hijo
                 children.insert(index, newNode);
             }
         }
     }
 
     /**
      * Elimina un nodo de la lista de hijos.
      * @param index Indice del nodo a eliminar.
      */
     public void removeChild(int index)
     {
     	//Si el nodo actual tiene hijos
         if (children != null)
         {
         	if (index >= 0 && index < children.size())
             {
         		//Se elimina el i-simo hijo
         		children.remove(index);
             }
         }
     }
 
     /**
      * Elimina un nodo de la lista de hijos.
      * @param node Nodo a eliminar.
      */
     public void removeChild(RtfTreeNode node)
     {
     	//Si el nodo actual tiene hijos
         if (children != null)
         {
 	        //Se busca el nodo a eliminar
 	        int index = children.indexOf(node);
 	
 	        //Si lo encontramos
             if (index != -1)
             {
 		        //Se elimina el i-simo hijo
 		        children.remove(index);
             }
         }
     }
 
     /**
      * Realiza una copia exacta del nodo actual.
      * @return Devuelve una copia exacta del nodo actual.
      */
     public RtfTreeNode cloneNode()
     {
         RtfTreeNode clon = new RtfTreeNode();
 
         clon.key = this.key;
         clon.hasParam = this.hasParam;
         clon.param = this.param;
         clon.parent = null;
         clon.root = null;
         clon.tree = null;
         clon.nodeType = this.nodeType;
 
         //Se clonan tambin cada uno de los hijos
         clon.children = null;
 
         if (this.children != null)
         {
             clon.children = new RtfNodeCollection();
             
             for(int i=0; i<children.size(); i++)
             {
             	RtfTreeNode childclon = children.get(i).cloneNode();
             	childclon.parent = clon;
             	
             	clon.children.add(childclon);
             }
         }
 
         return clon;
     }
 
     /**
      * Indica si el nodo actual tiene nodos hijos.
      * @return Devuelve true si el nodo actual tiene algn nodo hijo.
      */
     public boolean hasChildNodes()
     {
     	boolean res = false;
     	
     	if (children != null && children.size() > 0)
             res = true;
 
         return res;
     }
 
     /**
      * Devuelve el primer nodo de la lista de nodos hijos del nodo actual cuya palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Primer nodo de la lista de nodos hijos del nodo actual cuya palabra clave es la indicada como parmetro.
      */
     public RtfTreeNode selectSingleChildNode(String keyword)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
 	        while (i < children.size() && !found)
 	        {
 	            if (children.get(i).key.equals(keyword))
 	            {
 	                node = children.get(i);
 	                found = true;
 	            }
 	
 	            i++;
 	        }
         }
 
         return node;
     }
 
     /**
      * Devuelve el primer nodo de la lista de nodos hijos del nodo actual cuyo tipo es el indicado como parmetro.
      * @param nodeType Tipo de nodo buscado.
      * @return Primer nodo de la lista de nodos hijos del nodo actual cuyo tipo es el indicado como parmetro.
      */
     public RtfTreeNode selectSingleChildNode(int nodeType)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
 	        while (i < children.size() && !found)
 	        {
 	            if (children.get(i).nodeType == nodeType)
 	            {
 	                node = children.get(i);
 	                found = true;
 	            }
 	
 	            i++;
 	        }
         }
 
         return node;
     }
     
     /**
      * Devuelve el primer nodo de la lista de nodos hijos del nodo actual cuya palabra clave y parmetro son los indicados como parmetros.
      * @param keyword Palabra clave buscada.
      * @param param Parmetro buscado.
      * @return Primer nodo de la lista de nodos hijos del nodo actual cuya palabra clave y parmetro son los indicados como parmetros.
      */
     public RtfTreeNode selectSingleChildNode(String keyword, int param)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
             while (i < children.size() && !found)
             {
                 if (children.get(i).key.equals(keyword) && children.get(i).param == param)
                 {
                     node = children.get(i);
                     found = true;
                 }
 
                 i++;
             }
         }
 
         return node;
     }
     
     /**
      * Devuelve el primer nodo grupo de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Primer nodo grupo de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfTreeNode selectSingleChildGroup(String keyword)
     {
         return selectSingleChildGroup(keyword, false);
     }
     
     /**
      * Devuelve el primer nodo grupo de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @param ignoreSpecial Si est activo se ignorarn los nodos de control '\*' previos a algunas palabras clave.
      * @return Primer nodo grupo de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfTreeNode selectSingleChildGroup(String keyword, boolean ignoreSpecial)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
             while (i < children.size() && !found)
             {
                 if (children.get(i).getNodeType() == RtfNodeType.GROUP && children.get(i).hasChildNodes() &&
                     (
                      (children.get(i).firstChild().getNodeKey().equals(keyword)) ||
                      (ignoreSpecial && children.get(i).getChildNodes().get(0).getNodeKey().equals("*") && 
                     		 children.get(i).getChildNodes().get(1).getNodeKey().equals(keyword)))
                     )
                 {
                     node = children.get(i);
                     found = true;
                 }
 
                 i++;
             }
         }
 
         return node;
     }
  
     /**
      * Devuelve el primer nodo del rbol, a partir del nodo actual, cuyo tipo es el indicado como parmetro.
      * @param nodeType Tipo del nodo buscado.
      * @return Primer nodo del rbol, a partir del nodo actual, cuyo tipo es el indicado como parmetro.
      */
     public RtfTreeNode selectSingleNode(int nodeType)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
 	        while (i < children.size() && !found)
 	        {
 	            if (children.get(i).nodeType == nodeType)
 	            {
 	                node = children.get(i);
 	                found = true;
 	            }
 	            else
 	            {
 	                node = children.get(i).selectSingleNode(nodeType);
 	
 	                if (node != null)
 	                {
 	                    found = true;
 	                }
 	            }
 	
 	            i++;
 	        }
         }
 
         return node;
     }
 
     /**
      * Devuelve el primer nodo del rbol, a partir del nodo actual, cuya palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Primer nodo del rbol, a partir del nodo actual, cuya palabra clave es la indicada como parmetro.
      */
     public RtfTreeNode selectSingleNode(String keyword)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
 	        while (i < children.size() && !found)
 	        {
 	            if (children.get(i).key.equals(keyword))
 	            {
 	                node = children.get(i);
 	                found = true;
 	            }
 	            else
 	            {
 	                node = children.get(i).selectSingleNode(keyword);
 	
 	                if (node != null)
 	                {
 	                    found = true;
 	                }
 	            }
 	
 	            i++;
 	        }
         }
 
         return node;
     }
     
     /**
      * Devuelve el primer nodo grupo del rbol, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Primer nodo grupo del rbol, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfTreeNode selectSingleGroup(String keyword)
     {
         return selectSingleGroup(keyword, false);
     }
     
     /**
      * Devuelve el primer nodo grupo del rbol, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @param ignoreSpecial Si est activo se ignorarn los nodos de control '\*' previos a algunas palabras clave.
      * @return Primer nodo grupo del rbol, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfTreeNode selectSingleGroup(String keyword, boolean ignoreSpecial)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
             while (i < children.size() && !found)
             {
                 if (children.get(i).getNodeType() == RtfNodeType.GROUP && children.get(i).hasChildNodes() &&
                     (
                      (children.get(i).firstChild().getNodeKey().equals(keyword)) ||
                      (ignoreSpecial && children.get(i).getChildNodes().get(0).getNodeKey().equals("*") && 
                     		 children.get(i).getChildNodes().get(1).getNodeKey().equals(keyword)))
                     )
                 {
                     node = children.get(i);
                     found = true;
                 }
                 else
                 {
                     node = children.get(i).selectSingleGroup(keyword, ignoreSpecial);
 
                     if (node != null)
                     {
                         found = true;
                     }
                 }
 
                 i++;
             }
         }
 
         return node;
     }
     
     /**
      * Devuelve el primer nodo del rbol, a partir del nodo actual, cuya palabra clave y parmetro son los indicados como parmetro.
      * @param keyword Palabra clave buscada.
      * @param param Parmetro buscado.
      * @return Primer nodo del rbol, a partir del nodo actual, cuya palabra clave y parmetro son ls indicados como parmetro.
      */
     public RtfTreeNode selectSingleNode(String keyword, int param)
     {
         int i = 0;
         boolean found = false;
         RtfTreeNode node = null;
 
         if (children != null)
         {
             while (i < children.size() && !found)
             {
                 if (children.get(i).key.equals(keyword) && children.get(i).param == param)
                 {
                     node = children.get(i);
                     found = true;
                 }
                 else
                 {
                     node = children.get(i).selectSingleNode(keyword, param);
 
                     if (node != null)
                     {
                         found = true;
                     }
                 }
 
                 i++;
             }
         }
 
         return node;
     }
 
     /**
      * Devuelve todos los nodos, a partir del nodo actual, cuya palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Coleccin de nodos, a partir del nodo actual, cuya palabra clave es la indicada como parmetro.
      */
     public RtfNodeCollection selectNodes(String keyword)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
 	        for(int i=0; i<children.size(); i++)
 	        {
 	            if (children.get(i).key.equals(keyword))
 	            {
 	                nodes.add(children.get(i));
 	            }
 	
 	            nodes.addRange(children.get(i).selectNodes(keyword));
 	        }
         }
 
         return nodes;
     }
 
     /**
      * Devuelve todos los nodos, a partir del nodo actual, cuyo tipo es el indicado como parmetro.
      * @param nodeType Tipo del nodo buscado.
      * @return Coleccin de nodos, a partir del nodo actual, cuyo tipo es la indicado como parmetro.
      */
     public RtfNodeCollection selectNodes(int nodeType)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
 	        for(int i=0; i<children.size(); i++)
 	        {
 	            if (children.get(i).nodeType == nodeType)
 	            {
 	                nodes.add(children.get(i));
 	            }
 	
 	            nodes.addRange(children.get(i).selectNodes(nodeType));
 	        }
         }
 
         return nodes;
     }
     
     /**
      * Devuelve todos los nodos grupo, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Coleccin de nodos grupo, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfNodeCollection selectGroups(String keyword)
     {
         return selectGroups(keyword, false);
     }
     
     /**
      * Devuelve todos los nodos grupo, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @param ignoreSpecial Si est activo se ignorarn los nodos de control '\*' previos a algunas palabras clave.
      * @return Coleccin de nodos grupo, a partir del nodo actual, cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfNodeCollection selectGroups(String keyword, boolean ignoreSpecial)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
         	for(int i=0; i<children.size(); i++)
 	        {
         		RtfTreeNode node = children.get(i);
         		
                 if (node.getNodeType() == RtfNodeType.GROUP && node.hasChildNodes() &&
                     (
                      (node.firstChild().getNodeKey().equals(keyword)) ||
                      (ignoreSpecial && node.getChildNodes().get(0).getNodeKey().equals("*") && node.getChildNodes().get(1).getNodeKey().equals(keyword)))
                     )
                 {
                     nodes.add(node);
                 }
 
                 nodes.addRange(node.selectGroups(keyword, ignoreSpecial));
             }
         }
 
         return nodes;
     }
     
     /**
      * Devuelve todos los nodos, a partir del nodo actual, cuya palabra clave y parmetro son los indicados como parmetro.
      * @param keyword Palabra clave buscada.
      * @param param Parmetro buscado.
      * @return Coleccin de nodos, a partir del nodo actual, cuya palabra clave y parmetro son los indicados como parmetro.
      */
     public RtfNodeCollection selectNodes(String keyword, int param)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
         	for(int i=0; i<children.size(); i++)
             {
         		RtfTreeNode node = children.get(i);
         		
                 if (node.key.equals(keyword) && node.param == param)
                 {
                     nodes.add(node);
                 }
 
                 nodes.addRange(node.selectNodes(keyword, param));
             }
         }
 
         return nodes;
     }
 
     /**
      * Devuelve todos los nodos de la lista de nodos hijos del nodo actual cuya palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Coleccin de nodos de la lista de nodos hijos del nodo actual cuya palabra clave es la indicada como parmetro.
      */
     public RtfNodeCollection selectChildNodes(String keyword)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
 	        for(int i=0; i<children.size(); i++)
 	        {
 	            if (children.get(i).key.equals(keyword))
 	            {
 	                nodes.add(children.get(i));
 	            }
 	        }
         }
 
         return nodes;
     }
     
     /**
      * Devuelve todos los nodos grupos de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Coleccin de nodos grupo de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfNodeCollection selectChildGroups(String keyword)
     {
     	return selectChildGroups(keyword, false);
     }
     
     /**
      * Devuelve todos los nodos grupos de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @param ignoreSpecial Si est activo se ignorarn los nodos de control '\*' previos a algunas palabras clave.
      * @return Coleccin de nodos grupo de la lista de nodos hijos del nodo actual cuya primera palabra clave es la indicada como parmetro.
      */
     public RtfNodeCollection selectChildGroups(String keyword, boolean ignoreSpecial)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
         	for(int i=0; i<children.size(); i++)
             {
         		RtfTreeNode node = children.get(i);
         		
                 if (node.getNodeType() == RtfNodeType.GROUP && node.hasChildNodes() && 
                     (
                      (node.firstChild().getNodeKey().equals(keyword)) ||
                      (ignoreSpecial && node.getChildNodes().get(0).getNodeKey().equals("*") && node.getChildNodes().get(1).getNodeKey().equals(keyword)))
                     )
                 {
                     nodes.add(node);
                 }
             }
         }
 
         return nodes;
     }
 
     /**
      * Devuelve todos los nodos de la lista de nodos hijos del nodo actual cuyo tipo es el indicado como parmetro.
      * @param nodeType Tipo del nodo buscado.
      * @return Coleccin de nodos de la lista de nodos hijos del nodo actual cuyo tipo es el indicado como parmetro.
      */
     public RtfNodeCollection selectChildNodes(int nodeType)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
 	        for(int i=0; i<children.size(); i++)
 	        {
 	            if (children.get(i).nodeType == nodeType)
 	            {
 	                nodes.add(children.get(i));
 	            }
 	        }
         }
         return nodes;
     }
     
     /**
      * Devuelve todos los nodos de la lista de nodos hijos del nodo actual cuya palabra clave y parmetro son los indicados como parmetro.
      * @param keyword Palabra clave buscada.
      * @param param Parmetro buscado.
      * @return Coleccin de nodos de la lista de nodos hijos del nodo actual cuya palabra clave y parmetro son los indicados como parmetro.
      */
     public RtfNodeCollection selectChildNodes(String keyword, int param)
     {
         RtfNodeCollection nodes = new RtfNodeCollection();
 
         if (children != null)
         {
         	for(int i=0; i<children.size(); i++)
             {
         		RtfTreeNode node = children.get(i);
         		
                 if (node.key.equals(keyword) && node.param == param)
                 {
                     nodes.add(node);
                 }
             }
         }
 
         return nodes;
     }
     
     /**
      * Devuelve el siguiente nodo hermano del actual cuya palabra clave es la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Primer nodo hermano del actual cuya palabra clave es la indicada como parmetro.
      */
     public RtfTreeNode selectSibling(String keyword)
     {
         RtfTreeNode node = null;
         RtfTreeNode par = this.parent;
 
         if (par != null)
         {
             int curInd = par.children.indexOf(this);
 
             int i = curInd + 1;
             boolean found = false;
 
             while (i < par.children.size() && !found)
             {
                 if (par.children.get(i).key.equals(keyword))
                 {
                     node = par.children.get(i);
                     found = true;
                 }
 
                 i++;
             }
         }
 
         return node;
     }
     
     /**
      * Devuelve el siguiente nodo hermano del actual cuyo tipo es el indicado como parmetro.
      * @param nodeType Tipo de nodo buscado.
      * @return Primer nodo hermano del actual cuyo tipo es el indicado como parmetro.
      */
     public RtfTreeNode selectSibling(int nodeType)
     {
         RtfTreeNode node = null;
         RtfTreeNode par = this.parent;
 
         if (par != null)
         {
             int curInd = par.children.indexOf(this);
 
             int i = curInd + 1;
             boolean found = false;
 
             while (i < par.children.size() && !found)
             {
                 if (par.children.get(i).nodeType == nodeType)
                 {
                     node = par.children.get(i);
                     found = true;
                 }
 
                 i++;
             }
         }
 
         return node;
     }
     
     /**
      * Devuelve el siguiente nodo hermano del actual cuya palabra clave y parmetro son los indicados como parmetro.
      * @param keyword Palabra clave buscada.
      * @param param Parmetro buscado.
      * @return Primer nodo hermano del actual cuya palabra clave y parmetro son los indicados como parmetro.
      */
     public RtfTreeNode selectSibling(String keyword, int param)
     {
         RtfTreeNode node = null;
         RtfTreeNode par = this.parent;
 
         if (par != null)
         {
             int curInd = par.children.indexOf(this);
 
             int i = curInd + 1;
             boolean found = false;
 
             while (i < par.children.size() && !found)
             {
                 if (par.children.get(i).key.equals(keyword) && par.children.get(i).param == param)
                 {
                     node = par.children.get(i);
                     found = true;
                 }
 
                 i++;
             }
         }
 
         return node;
     }
     
     /**
      * Busca todos los nodos de tipo Texto que contengan el texto buscado.
      * @param text Texto buscado en el documento.
      * @return Lista de nodos, a partir del actual, que contienen el texto buscado.
      */
     public RtfNodeCollection findText(String text)
     {
         RtfNodeCollection list = new RtfNodeCollection();
 
         //Si el nodo actual tiene hijos
         if (children != null)
         {
         	for(int i=0; i<children.size(); i++)
             {
         		RtfTreeNode node = children.get(i);
         		
                 if (node.getNodeType() == RtfNodeType.TEXT && node.getNodeKey().indexOf(text) != -1)
                     list.add(node);
                 else if(node.getNodeType() == RtfNodeType.GROUP)
                     list.addRange(node.findText(text));
             }
         }
 
         return list;
     }
     
     /**
      * Busca y reemplaza un texto determinado en todos los nodos de tipo Texto a partir del actual.
      * @param oldValue Texto buscado.
      * @param newValue Texto de reemplazo.
      */
     public void replaceText(String oldValue, String newValue)
     {
         //Si el nodo actual tiene hijos
         if (children != null)
         {
         	for(int i=0; i<children.size(); i++)
             {
         		RtfTreeNode node = children.get(i);
         		
                 if (node.getNodeType() == RtfNodeType.TEXT)
                     node.setNodeKey(node.getNodeKey().replace(oldValue, newValue));
                 else if (node.getNodeType() == RtfNodeType.GROUP)
                     node.replaceText(oldValue, newValue);
             }
         }
     }
     
     /**
      * Devuelve una representacin del nodo donde se indica su tipo, clave, indicador de parmetro y valor de parmetro
      * @return Cadena de caracteres del tipo [TIPO, CLAVE, IND_PARAMETRO, VAL_PARAMETRO]
      */
     public String toString()
 	{
     	return "[" + RtfNodeType.toString(this.nodeType) + ", " + this.key + ", " + this.hasParam + ", " + this.param + "]";
 	}
     
     //----------------------------------------------------------------
     
     //Mtodos Privados
     
     /**
      * Decodifica un caracter especial indicado por su cdigo decimal
      * @param code Cdigo del caracter especial (\')
      * @param enc Codificacin utilizada para decodificar el caracter especial.
      * @return Caracter especial decodificado.
      */
     private static String decodeControlChar(int code, Charset enc)
     {
         return enc.decode(ByteBuffer.wrap(new byte[] {(byte)code})).toString();                
     }
     
     /**
      * Devuelve el cdigo RTF del nodo actual y todos sus nodos hijos.
      * @return Cdigo RTF del nodo actual y todos sus nodos hijos.
      */
     public String getRtf()
     {
         String res = "";
         
         Charset enc = this.tree.getEncoding();
 
         res = getRtfInm(this, null, enc);
 
         return res;
     }
     
     /**
      * Mtodo auxiliar para obtener el Texto RTF del nodo actual a partir de su representacin en rbol.
      * @param curNode Nodo actual del rbol.
      * @param prevNode Nodo anterior tratado.
      * @param enc Codificacin del documento.
      * @return Texto en formato RTF del nodo. 
      */
     private String getRtfInm(RtfTreeNode curNode, RtfTreeNode prevNode, Charset enc)
     {
     	StringBuilder res = new StringBuilder("");
 
         if (curNode.nodeType == RtfNodeType.ROOT)
             res.append("");
         else if (curNode.nodeType == RtfNodeType.GROUP)
             res.append("{");
         else
         {
             if (curNode.nodeType == RtfNodeType.CONTROL ||
             	curNode.nodeType == RtfNodeType.KEYWORD)
             {
                 res.append("\\");
             }
             else  //curNode.NodeType == RtfNodeType.TEXT
             {
                 if (prevNode != null && 
                 	prevNode.nodeType == RtfNodeType.KEYWORD)
                 {
                 	int code = Character.codePointAt(curNode.getNodeKey(), 0);
 
                     if (code >= 32 && code < 128)
                         res.append(" ");
                 }
             }
 
             appendEncoded(res, curNode.key, enc);
 
             if (curNode.hasParam)
             {
                 if (curNode.nodeType == RtfNodeType.KEYWORD)
                 {
                     res.append(String.valueOf(curNode.param));
                 }
                 else if (curNode.nodeType == RtfNodeType.CONTROL)
                 {
                     //Si es un caracter especial como las vocales acentuadas
                     if (curNode.key.equals("\'"))
                     {
                     	res.append(getHexa(curNode.param));
                     }
                 }
             }
         }
 
         //Se obtienen los nodos hijos
         RtfNodeCollection children = curNode.children;
 
         //Si el nodo tiene hijos se obtiene el cdigo RTF de los hijos
         if (children != null)
         {
 	        for (int i = 0; i < children.size(); i++)
 	        {
 	            RtfTreeNode node = children.get(i);
 	
 	            if (i > 0)
 	                res.append(getRtfInm(node, children.get(i-1), enc));
 	            else
 	                res.append(getRtfInm(node, null, enc));
 	        }
         }
         
         if (curNode.nodeType == RtfNodeType.GROUP)
         {
             res.append("}");
         }
 
         return res.toString();
     }
     
     /**
      * Concatena dos cadenas utilizando la codificacin del documento.
      * @param res Cadena original.
      * @param s Cadena a aadir.
      * @param enc Codificacin del documento.
      */
     private void appendEncoded(StringBuilder res, String s, Charset enc)
     {
         for (int i = 0; i < s.length(); i++)
         {
             int code = Character.codePointAt(s, i);
 
             if (code >= 128 || code < 32)
             {
                 res.append("\\'");
                 ByteBuffer bytes = enc.encode(CharBuffer.wrap(s, i, i+1));
                 res.append(getHexa(bytes.get(0) & 0xff));  //Para evitar bytes negativos
             }
             else
             {
                 if ((s.charAt(i) == '{') || (s.charAt(i) == '}') || (s.charAt(i) == '\\'))
                 {
                     res.append("\\");
                 }
 
                 res.append(s.charAt(i));
             }
         }
     }
     
     /**
      * Obtiene el cdigo hexadecimal de un entero.
      * @param code Nmero entero.
      * @return Cdigo hexadecimal del entero pasado como parmetro.
      */
     private String getHexa(int code)
     {
         String hexa = Integer.toHexString(code);
 
         if (hexa.length() == 1)
         {
             hexa = "0" + hexa;
         }
 
         return hexa;
     }
     
     /**
      * Actualiza las propiedades Root y Tree de un nodo (y sus hijos) con las del nodo actual.
      * @param node Nodo a actualizar.
      */
     private void updateNodeRoot(RtfTreeNode node)
     {
         //Se asigna el nodo raz del documento
         node.root = this.root;
 
         //Se asigna el rbol propietario del nodo
         node.tree = this.tree;
 
         //Si el nodo actualizado tiene hijos se actualizan tambin
         if (node.children != null)
         {
             //Se actualizan recursivamente los hijos del nodo actual
         	for(int i=0; i< node.children.size(); i++)
             {
                 updateNodeRoot(node.children.get(i));
             }
         }
     }
     
     /**
      * Devuelve el fragmento de texto del documento contenido en el nodo actual.
      * @param raw Si este parmetro est activado se extraer todo el texto contenido en el nodo, independientemente de si ste forma parte del texto real del documento.
      * @return Fragmento de texto del documento contenido en el nodo actual.
      */
     public String getText(boolean raw)
     {
     	return getText(raw, 1);
     }
     
     /**
      * Devuelve todo el texto contenido en el nodo actual.
      * @param raw Si este parmetro est activado se extraer todo el texto contenido en el nodo, independientemente de si ste forma parte del texto real del documento.
      * @param ignoreNchars Ignore next N chars following \\uN keyword
      * @return Texto contenido en el nodo actual.
      */
     public String getText(boolean raw, int ignoreNchars)
     {
     	StringBuilder res = new StringBuilder("");
 
         if (this.getNodeType() == RtfNodeType.GROUP)
         {
             int indkw = this.firstChild().getNodeKey().equals("*") ? 1 : 0;
 
             if (raw ||
                (!this.children.get(indkw).getNodeKey().equals("fonttbl") &&
                 !this.children.get(indkw).getNodeKey().equals("colortbl") &&
                 !this.children.get(indkw).getNodeKey().equals("stylesheet") &&
                 !this.children.get(indkw).getNodeKey().equals("generator") &&
                 !this.children.get(indkw).getNodeKey().equals("info") &&
                 !this.children.get(indkw).getNodeKey().equals("pict") &&
                 !this.children.get(indkw).getNodeKey().equals("object") &&
                 !this.children.get(indkw).getNodeKey().equals("fldinst")))
             {
                 if (children != null)
                 {
                     int uc = ignoreNchars;
                     for(int i = 0; i < children.size(); i++)
                     {
                     	RtfTreeNode node = children.get(i);
                     	
                         res.append(node.getText(raw, uc));
 
                         if (node.getNodeType() == RtfNodeType.KEYWORD && node.getNodeKey().equals("uc"))
                             uc = node.getParameter();
                     }
                 }
             }
         }
         else if (this.getNodeType() == RtfNodeType.CONTROL)
         {
             if (this.getNodeKey().equals("'"))
                 res.append(decodeControlChar(this.getParameter(), this.tree.getEncoding()));
             else if (this.getNodeKey().equals("~"))  // non-breaking space
                 res.append(" ");
         }
         else if (this.getNodeType() == RtfNodeType.TEXT)
         {
             String newtext = this.getNodeKey();
 
             //Si el elemento anterior era un caracater Unicode (\\uN) ignoramos los siguientes N caracteres
             //segn la ltima etiqueta \\ucN
             if (this.previousNode().getNodeType() == RtfNodeType.KEYWORD &&
                 this.previousNode().getNodeKey().equals("u"))
             {
                 newtext = newtext.substring(ignoreNchars);
             }
 
             res.append(newtext);
         }
         else if (this.getNodeType() == RtfNodeType.KEYWORD)
         {
             if (this.getNodeKey().equals("par"))
                res.append("\n");
             else if (this.getNodeKey().equals("tab"))
                 res.append("\t");
             else if (this.getNodeKey().equals("line"))
                res.append("\n");
             else if (this.getNodeKey().equals("lquote"))
                 res.append("");
             else if (this.getNodeKey().equals("rquote"))
                 res.append("");
             else if (this.getNodeKey().equals("ldblquote"))
                 res.append("");
             else if (this.getNodeKey().equals("rdblquote"))
                 res.append("");
             else if (this.getNodeKey().equals("emdash"))
                 res.append("");
             else if (this.getNodeKey().equals("u"))
             {
                 res.append(Character.toChars(this.getParameter()));
             }
         }
 
         return res.toString();
     }
     
     /**
      * Obtiene el nodo raz del rbol RTF.
      * @return Nodo raz del rbol RTF.
      */
     public RtfTreeNode getRootNode()
     {
     	return root;
     }
     
     /**
      * Establece el nodo raz del rbol RTF.
      * @param node Nodo raz del rbol RTF.
      */
     public void setRootNode(RtfTreeNode node)
     {
     	root = node;
     }
     
     /**
      * Obtiene el nodo padre del nodo actual.
      * @return Nodo padre del nodo actual.
      */
     public RtfTreeNode getParentNode()
     {
     	return parent;
     }    
     
     /**
      * Establece el nodo padre del nodo actual.
      * @param node Nodo padre del nodo actual.
      */
     public void setParentNode(RtfTreeNode node)
     {
     	parent = node;
     }
     
     /**
      * Obtiene el tipo de nodo actual.
      * @return Tipo del nodo actual.
      */
     public int getNodeType()
     {
     	return nodeType;
     }
     
     /**
      * Establece el tipo de nodo actual.
      * @param nodeType Tipo de nodo.
      */
     public void setNodeType(int nodeType)
     {
     	this.nodeType = nodeType; 
     }
     
     /**
      * Obtiene la clave del nodo actual.
      * @return Clave del nodo actual.
      */
     public String getNodeKey()
     {
     	return key;
     }
     
     /**
      * Establece la clave del nodo actual.
      * @param key Clave del nodo.
      */
     public void setNodeKey(String key)
     {
     	this.key = key;
     }
     
     /**
      * Obtiene el indicativo de existencia de parmetro asociado al nodo.
      * @return Indicativo de existencia de parmetro asociado al nodo.
      */
     public boolean getHasParameter()
     {
     	return hasParam;
     }
     
     /**
      * Establece el indicativo de existencia de parmetro asociado al nodo.
      * @param hasParam Indicativo de existencia de parmetro asociado al nodo.
      */
     public void setHasParamenter(boolean hasParam)
     {
     	this.hasParam = hasParam;
     }    
     
     /**
      * Obtiene el parmetro asociado al nodo.
      * @return Parmetro asociado al nodo.
      */
     public int getParameter()
     {
     	return param;
     }
     
     /**
      * Establece el parmetro asociado al nodo.
      * @param param Parmetro asociado al nodo.
      */
     public void setParameter(int param)
     {
     	this.param = param; 
     }
     
     /**
      * Obtiene el rbol RTF padre del nodo.
      * @return rbol RTF padre del nodo.
      */
     public RtfTree getTree()
     {
     	return tree;
     }
     
     /**
      * Establece el rbol RTF padre del nodo.
      * @param tree rbol RTF padre del nodo.
      */
     public void setTree(RtfTree tree)
     {
     	this.tree = tree; 
     }
     
     /**
      * Obtienen la coleccin de nodos hijo del nodo actual.
      * @return Coleccin de nodos hijo del nodo actual.
      */
     public RtfNodeCollection getChildNodes()
     {
     	return children;
     }
     
     /**
      * Establece la coleccin de nodos hijo del nodo actual.
      * @param children Coleccin de nodos hijo del nodo actual.
      */
     public void setChildNodes(RtfNodeCollection children)
     {
     	this.children = children;
     	
     	for(int i=0; i<children.size(); i++)
         {
     		RtfTreeNode node = children.get(i);
     		
             node.parent = this;
 
             //Se actualizan las propiedades Root y Tree del nuevo nodo y sus posibles hijos
             updateNodeRoot(node);
         }
     }
        
     /**
      * Devuelve el primer nodo hijo cuya palabra clave sea la indicada como parmetro.
      * @param keyword Palabra clave buscada.
      * @return Primer nodo hijo cuya palabra clave sea la indicada como parmetro. En caso de no existir se devuelve null.
      */
     public RtfTreeNode getChild(String keyword)
     {
     	return this.selectSingleChildNode(keyword);
     }
     
     /**
      * Devuelve el hijo n-simo del nodo actual.
      * @param keyword ndice del nodo hijo a recuperar.
      * @return Nodo hijo n-simo del nodo actual. Devuelve null en caso de no existir.
      */
     public RtfTreeNode getChild(int childIndex)
     {
     	RtfTreeNode res = null;
 
         if (children != null && childIndex >= 0 && childIndex < children.size())
             res = children.get(childIndex);
 
         return res;
     }
     
     /**
      * Obtiene el primer nodo hijo del nodo actual.
      * @return Primer nodo hijo del nodo actual.
      */
     public RtfTreeNode firstChild()
     {
     	RtfTreeNode res = null;
     	
         if (children != null && children.size() > 0)
             res = children.get(0);
         
         return res;
     }
     
     /**
      * Obtiene el ltimo nodo hijo del nodo actual.
      * @return ltimo nodo hijo del nodo actual.
      */
     public RtfTreeNode lastChild()
     {
     	RtfTreeNode res = null;
     	
     	if (children != null && children.size() > 0)
             res = children.get(children.size()-1);
 
     	return res;
     }
     
     /**
      * Obtiene el nodo hermano siguiente del nodo actual (Dos nodos son hermanos si tienen el mismo nodo padre [ParentNode]).
      * @return Siguiente nodo hermano del nodo actual.
      */
     public RtfTreeNode nextSibling()
     {
     	RtfTreeNode res = null;
     	
     	if (parent != null && parent.children != null)
         {
 	        int currentIndex = parent.children.indexOf(this);
 	
 	        if (parent.children.size() > currentIndex + 1)
 	            res = parent.children.get(currentIndex + 1);
         }
     	
     	return res;
     }
     
     /**
      * Obtiene el nodo hermano anterior del nodo actual (Dos nodos son hermanos si tienen el mismo nodo padre [ParentNode]).
      * @return Nodo hermano anterior del nodo actual.
      */
     public RtfTreeNode previousSibling()
     {
     	RtfTreeNode res = null;
     	
     	if (parent != null && parent.children != null)
         {
 	        int currentIndex = parent.children.indexOf(this);
 	
 	        if (currentIndex > 0)
 	            res = parent.children.get(currentIndex - 1);
         }
     	
     	return res;
     }
     
     /**
      * Devuelve el nodo siguiente del rbol.
      * @return Nodo siguiente del rbol.
      */
     public RtfTreeNode nextNode()
     {
     	RtfTreeNode res = null;
 
         if (this.getNodeType() == RtfNodeType.ROOT)
         {
             res = this.firstChild();
         }
         else if (parent != null && parent.children != null)
         {
             if (this.getNodeType() == RtfNodeType.GROUP && this.children.size() > 0)
             {
                 res = this.firstChild();
             }
             else
             {
                 if (this.getIndex() < (parent.children.size() - 1))
                 {
                     res = this.nextSibling();
                 }
                 else
                 {
                     res = parent.nextSibling();
                 }
             }
         }
 
         return res;
     }
     
     /**
      * Devuelve el nodo anterior del rbol.
      * @return Nodo anterior del rbol.
      */
     public RtfTreeNode previousNode()
     {
     	RtfTreeNode res = null;
 
         if (this.getNodeType() == RtfNodeType.ROOT)
         {
             res = null;
         }
         else if (parent != null && parent.children != null)
         {
             if (this.getIndex() > 0)
             {
                 if (this.previousSibling().getNodeType() == RtfNodeType.GROUP)
                 {
                     res = this.previousSibling().lastChild();
                 }
                 else
                 {
                     res = this.previousSibling();
                 }
             }
             else
             {
                 res = parent;
             }
         }
 
         return res;
     }
        
     /**
      * Devuelve el ndice del nodo actual dentro de la lista de hijos de su nodo padre.
      * @return ndice del nodo actual dentro de la lista de hijos de su nodo padre.
      */
     public int getIndex()
     {
     	int res = -1;
 
         if(parent != null)
             res = parent.children.indexOf(this);
 
         return res;
     }
        
     /**
      * Devuelve el fragmento de texto del documento contenido en el nodo actual.
      * @return Devuelve el fragmento de texto del documento contenido en el nodo actual.
      */
     public String getText()
     {
     	return getText(false);
     }
     
     /**
      * Devuelve todo el texto contenido en el nodo actual.
      * @return Devuelve todo el texto contenido en el nodo actual.
      */
     public String getRawText()
     {
     	return getText(true);
     }
 }
