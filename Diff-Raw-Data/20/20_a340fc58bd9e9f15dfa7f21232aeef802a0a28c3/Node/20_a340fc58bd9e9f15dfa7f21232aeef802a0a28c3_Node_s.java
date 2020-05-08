 /*
  * Copyright (c) 2013, Kyle Mulleady
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the organization nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY KYLE MULLEADY ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL KYLE MULLEADY BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ics311km;
 
 /** 
  * Node is the abstract class that defines the basic functionality of Node 
  * objects used in implementations of the DynamicSet interface.
  * In the ics311km package it is extended by {@link BSTNode}, {@link DLLNode}, 
 * and {@link QuadNode}.
  *
  * @author Kyle Mulleady
  * @version 1.0
  */
 public abstract class Node {
     protected Node left;
     protected Node right;
     protected KeyType key;
     /**
      * Creates an empty node.
      */
     public Node() { }
     /**
      * Creates a node and a KeyType object to store s.
      *
      * @param s  the String to be stored as this node's key.
      */
     public Node(String s) { this.key = new KeyType(s); }
     /**
      * Creates a node and sets its key.
      *
      * @param k  this node's key
      */
     public Node(KeyType k) { this.key = k; }
     /**
      * Returns this node's left node in DLLNode and QuadNode, 
     * or this node's left child node in BSTNode.
      *
      * @return  the left node
      */
     public Node getLeft()   { return this.left; }
     /**
      * Returns this node's right node in DLLNode and QuadNode, 
     * or this node's right child node in BSTNode.
      *
      * @return  the right node
      */
     public Node getRight()  { return this.right; }
     /**
      * Returns this node's key.
      *
      * @return  this node's key
      */
     public KeyType getKey() { return this.key; }
     /**
      * Sets this node's left node in DLLNode and QuadNode, 
     * or this node's left child node in BSTNode.
      *
      * @param n  the node to be set
      */
     public void setLeft(Node n)   { this.left = n; }
     /**
      * Sets this node's right node in DLLNode and QuadNode, 
     * or this node's right child node in BSTNode
      *
      * @param n  the node to be set
      */
     public void setRight(Node n)  { this.right = n; }
     /**
      * Sets this node's key.
      *
      * @param k  the key to be set
      */
     public void setKey(KeyType k) { this.key = k; }
     
     // Abstract methods for QuadNode.
 
     /**
      * Returns this node's above node in QuadNode.
      * Not used by other classes that extend Node.
      * 
      * @return  this node's above node.
      */
     abstract Node getAbove();
     /**
      * Returns this node's below node in QuadNode.
      * Not used by other classes that extend Node.
      *
      * @return  this node's below node.
      */
     abstract Node getBelow();
     /**
      * Sets this node's above node in QuadNode.
      * Not used by other classes that extend Node.
      *
      * @param n  the node to be set
      */
     abstract void setAbove(Node n);
     /**
      * Sets this node's below node in QuadNode.
      * Not used by other classes that extend Node.
      *
      * @param n  the node to be set
      */
     abstract void setBelow(Node n);
     
     // Abstract methods for BSTNode.
 
     /**
     * Returns this node's parent node in BSTNode.
      * Not used by other classes that extend Node.
      *
      * @return  this node's parent node.
      */
     abstract Node getP();
     /**
     * Sets this node's parent node in BSTNode.
      * Not used by other classes that extend Node.
      *
      * @param n  the node to be set
      */
     abstract void setP(Node n);
 
     // Abstract methods for RBNode.
 
     /**
      * Returns this node's color in RBNode.
      * Not used by other classes that extend Node.
      *
      * @return  true if this node is red, false if black
      */
     abstract boolean isRed();
     /**
      * Sets this node's color in RBNode.
      * Not used by other classes that extend Node.
      *
      * @param b  true is this node is red, false if black
      */
     abstract void isRed(boolean b);
 }
