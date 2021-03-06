 /*
  * Copyright (C) 2010 SonarSource SA
  * All rights reserved
  * mailto:contact AT sonarsource DOT com
  */
 
 package com.sonar.sslr.api;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * the parser is in charge to construct an abstract syntax tree (AST) which is a tree representation of the abstract syntactic structure of
  * source code. Each node of the tree is an AstNode and each node denotes a construct occurring in the source code which starts at a given
  * Token.
  * 
  * @see Token
  */
 public class AstNode {
 
   protected final AstNodeType type;
   private String name;
   private Token token;
   private List<AstNode> children;
   private int childIndex = -1;
   private AstNode parent;
   private int fromIndex;
   private int toIndex;
   private AstListener action = null;
 
   public AstNode(Token token) {
     this(token.getType(), token.getType().getName(), token);
   }
 
   public AstNode(AstNodeType type, String name, Token token) {
     this.type = type;
     this.token = token;
     this.name = name;
   }
 
   /**
    * Get the parent of this node in the tree.
    * 
    * @param parent
    */
   public AstNode getParent() {
     return parent;
   }
 
   public void addChild(AstNode child) {
     if (child != null) {
       if (children == null) {
         children = new ArrayList<AstNode>();
       }
       if (child.hasToBeSkippedFromAst()) {
         if (child.hasChildren()) {
           for (AstNode subChild : child.children) {
             addChildToList(subChild);
           }
         }
       } else {
         addChildToList(child);
       }
     }
   }
 
   private void addChildToList(AstNode child) {
     children.add(child);
     child.childIndex = children.size() - 1;
     child.parent = this;
   }
 
   /**
    * @return true if this AstNode has some children.
    */
   public boolean hasChildren() {
     return children != null && !children.isEmpty();
   }
 
   /**
    * Get the list of children.
    * 
    * @return list of children
    */
   public List<AstNode> getChildren() {
     return children;
   }
 
   public int getNumberOfChildren() {
     if (children == null) {
       return 0;
     }
     return children.size();
   }
 
   /**
    * Get the desired child
    * 
    * @param index
    *          the index of the child (start at 0)
    * @return the AstNode child
    */
   public AstNode getChild(int index) {
     if (index >= getNumberOfChildren()) {
       throw new IllegalStateException("The AstNode '" + this + "' has only " + getNumberOfChildren()
           + " children. Requested child index is wrong : " + index);
     }
     return children.get(index);
   }
 
   /**
    * Get the next sibling AstNode in the tree and if this node doesn't exist try to get the next AST Node of the parent.
    */
   public AstNode nextAstNode() {
     AstNode nextSibling = nextSibling();
     if (nextSibling != null) {
       return nextSibling;
     }
     if (parent != null) {
       return parent.nextAstNode();
     }
     return null;
   }
 
   /**
    * Get the next sibling AstNode if exists in the tree.
    * 
    * @return next sibling AstNode
    */
   public AstNode nextSibling() {
     if (parent == null) {
       return null;
     }
     if (parent.getNumberOfChildren() > childIndex + 1) {
       return parent.children.get(childIndex + 1);
     }
     return null;
   }
 
   /**
    * Get the previous sibling AstNode if exists in the tree.
    * 
    * @return previous sibling AstNode
    */
   public AstNode previousSibling() {
     if (parent == null) {
       return null;
     }
     if (childIndex > 0) {
       return parent.children.get(childIndex - 1);
     }
     return null;
   }
 
   /**
    * Get the Token's value associated to this AstNode
    * 
    * @return token's value
    */
   public String getTokenValue() {
     if (token == null) {
       return null;
     }
     return token.getValue();
   }
 
   /**
    * Get the Token associated to this AstNode
    */
   public Token getToken() {
     return token;
   }
 
   /**
    * Get the Token's line associated to this AstNode
    * 
    * @return token's line
    */
   public int getTokenLine() {
     return token.getLine();
   }
 
   public boolean hasToken() {
     return token != null;
   }
 
   public String getName() {
     return name;
   }
 
   public int getFromIndex() {
     return fromIndex;
   }
 
   public void setFromIndex(int fromIndex) {
     this.fromIndex = fromIndex;
   }
 
   public int getToIndex() {
     return toIndex;
   }
 
   public boolean hasToBeSkippedFromAst() {
     if (AstNodeSkippingPolicy.class.isAssignableFrom(type.getClass())) {
       return ((AstNodeSkippingPolicy) type).hasToBeSkippedFromAst(this);
     }
     return false;
   }
 
   public void setToIndex(int toIndex) {
     this.toIndex = toIndex;
   }
 
   public boolean is(AstNodeType type) {
     return this.type == type;
   }
 
   public void setAstNodeListener(AstListener action) {
     this.action = action;
   }
 
   public void startListening(Object output) {
     if (action != null) {
       action.startListening(this, output);
     }
   }
 
   public void stopListening(Object output) {
     if (action != null) {
       action.stopListening(this, output);
     }
   }
 
   public boolean isNot(AstNodeType type) {
     return this.type != type;
   }
 
   /**
    * Find the first child among all direct children having one of the desired types.
    * 
    * @param list
    *          of desired node types
    * @return the first child or null
    */
   public AstNode findFirstDirectChild(AstNodeType... nodeTypes) {
     for (AstNode child : children) {
       for (AstNodeType nodeType : nodeTypes) {
         if (child.type == nodeType) {
           return child;
         }
       }
     }
     return null;
   }
 
   /**
    * Find the first child among all children and grand-children having one of the desired types.
    * 
    * @param AstNodeType
    *          list of desired node types
    * @return the first child or null
    */
   public AstNode findFirstChild(AstNodeType... nodeTypes) {
     if (children != null) {
       for (AstNode child : children) {
         for (AstNodeType nodeType : nodeTypes) {
           if (child.type == nodeType) {
             return child;
           }
           AstNode node = child.findFirstChild(nodeTypes);
           if (node != null) {
             return node;
           }
         }
       }
     }
     return null;
   }
 
   /**
    * Get the first child of this node
    * 
    * @return the first child or null if there is no child
    */
   public AstNode getFirstChild() {
     if (children != null && children.size() > 0) {
       return children.get(0);
     }
     return null;
   }
 
   /**
    * Find the all children among direct children having the desired type.
    * 
    * @param AstNodeType
    *          the node type
    * @return the list of matching children
    */
   public List<AstNode> findDirectChildren(AstNodeType nodeType) {
     List<AstNode> nodes = new ArrayList<AstNode>();
     for (AstNode child : children) {
       if (child.type == nodeType) {
         nodes.add(child);
       }
     }
     return nodes;
   }
 
   /**
    * Get the last child of this node
    * 
    * @return the last child or null if there is no child
    */
   public AstNode getLastChild() {
     if (children != null && children.size() > 0) {
       return children.get(children.size() - 1);
     }
     return null;
   }
 
   /**
    * @return true if this node has some direct children with the desired node types
    */
   public boolean hasDirectChildren(AstNodeType... nodeTypes) {
     return findFirstDirectChild(nodeTypes) != null;
   }
 
   /**
    * @return true if this node has some children and/or grand-children with the desired node types
    */
   public boolean hasChildren(AstNodeType... nodeTypes) {
     return findFirstChild(nodeTypes) != null;
   }
 
   /**
    * @return true if this node has a parent or a grand-parent with the desired node type.
    */
   public boolean hasParents(AstNodeType nodeType) {
     if (findFirstParent(nodeType) != null) {
       return true;
     }
     return false;
   }
 
   /**
    * Find the first parent with the desired node type
    * 
    * @param AstNodeType
    *          the desired Ast node type
    * @return the parent/grand-parent or null
    */
   public AstNode findFirstParent(AstNodeType nodeType) {
     if (parent == null) {
       return null;
     } else if (parent.type == nodeType) {
       return parent;
     }
     return parent.findFirstParent(nodeType);
   }
 
   public boolean isCopyBookOrGeneratedNode() {
     return getToken().isCopyBook() || getToken().isGeneratedCode();
   }
 
   public AstNodeType getType() {
     return type;
   }
 
   /**
    * Dump the partial source code covered by this node.
    */
   public String dumpSourceCode() {
     StringBuilder result = new StringBuilder();
     List<Token> tokens = getTokens();
     int line = tokens.get(0).getLine();
     int column = 0;
     for (Token token : getTokens()) {
       if (line != token.getLine()) {
         result.append("\n");
       }
       for (int i = column; i < token.getColumn(); i++) {
         result.append(' ');
       }
       result.append(token.getValue());
       line = token.getLine();
       column = token.getColumn() + token.getValue().toString().length();
     }
     return result.toString();
   }
 
   /**
    * Return all tokens contained in this tree node. Those tokens can be directly or indirectly attached to this node.
    */
   public List<Token> getTokens() {
     List<Token> tokens = new ArrayList<Token>();
     if ( !hasChildren()) {
       tokens.add(token);
       return tokens;
     }
     for (AstNode child : children) {
       if ( !child.hasChildren()) {
         tokens.add(child.getToken());
       } else {
         tokens.addAll(child.getTokens());
       }
     }
     return tokens;
   }
 
   public String toString() {
     StringBuilder result = new StringBuilder();
     result.append(name);
     if (token != null) {
       result.append(" token='").append(token.getValue()).append("'");
       result.append(" line=").append(token.getLine());
       result.append(" column=").append(token.getColumn());
      result.append(" file='").append(token.getFile().getName() + "'");
     }
     return result.toString();
   }
 
   public Token getLastToken() {
     AstNode lastAstNode = this;
     while (lastAstNode.getLastChild() != null) {
       lastAstNode = lastAstNode.getLastChild();
     }
     return lastAstNode.getToken();
   }
 }
