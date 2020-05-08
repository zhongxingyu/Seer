 package smartest;
 
 import java.util.ArrayList;
 
 /**
  * The Class ASTNode represents a node in the STL Abstract Syntax Tree. The AST
  * is constructed during parsing of the source file. Each node represents a
  * single production or family of productions in the grammar. After the tree is
  * constructed, each ASTNode is called upon in a top down fashion to
  * semantically verify itself, and later generate target code for itself.
  * 
  * @author Daniel Walker
  */
 public abstract class ASTNode {
     /** The children of this node */
     private ArrayList<ASTNode> children;
     /** The type that this node evaluates to. */
     private String type;
     /** The line in the source file. */
     private int yyline;
     /** The column in the source file. */
     private int yycolumn;
     /** The integer value, if this node evaluates to one. */
     private int ivalue;
     /** The double value, if this node evaluates to one. */
     private double dvalue;
     /** The boolean value, if this node evaluates to one. */
     private String bvalue;
     /** The String value, if this node evaluates to one. */
     private String svalue;
     /** The char value, if this node evaluates to one. */
     private char cvalue;
 
     /**
      * Verify that this node is semantically sound. First check the children,
      * then verify everything is of the proper type and follows rules dictated
      * in the language reference manual. If applicable, set the type of this
      * node based on the types of the children.
      * 
      * @throws Exception
     *             if a semantic error is found
      */
     public abstract void checkSemantics() throws Exception;
 
     /**
      * Generate target Java source code.
      * 
      * @return the generated code for this node and its children
      */
     public abstract StringBuffer generateCode();
 
     /**
      * Instantiates a new ASTNode.
      * 
      * @param yyline
      *            the line in the input file at which this production was found
      * @param yycolumn
      *            the column in the input file
      */
     public ASTNode(int yyline, int yycolumn) {
         this.setChildren(new ArrayList<ASTNode>());
         this.yyline = yyline;
         this.yycolumn = yycolumn;
     }
 
     /**
      * Gets the child at the given index
      * 
      * @param index
      *            the index
      * @return the child at this index
      */
     ASTNode getChildAt(int index) {
         return this.getChildren().get(index);
     }
 
     /**
      * Adds a child to this node
      * 
      * @param node
      *            a child node
      */
     void addChild(ASTNode node) {
         this.getChildren().add(node);
     }
 
     /**
      * Gets the number of children
      * 
      * @return the child count
      */
     int getChildCount() {
         return this.getChildren().size();
     }
 
     /**
      * Gets the type that this node evaluates to
      * 
      * @return the type
      */
     String getType() {
         return this.type;
     }
 
     /**
      * Sets the type that this node evaluates to
      * 
      * @param typ
      *            the new type
      */
     void setType(String typ) {
         this.type = typ;
     }
 
     /**
      * Gets the corresponding line in the input file
      * 
      * @return the line number
      */
     public int getYyline() {
         return yyline;
     }
 
     /**
      * Sets the corresponding line in the input file
      * 
      * @param yyline
      *            the new line number
      */
     public void setYyline(int yyline) {
         this.yyline = yyline;
     }
 
     /**
      * Gets the corresponding column in the input file.
      * 
      * @return the column number
      */
     public int getYycolumn() {
         return yycolumn;
     }
 
     /**
      * Sets the corresponding column in the input file.
      * 
      * @param yycolumn
      *            the new column number
      */
     public void setYycolumn(int yycolumn) {
         this.yycolumn = yycolumn;
     }
 
     /**
      * Gets the children of this node
      * 
      * @return the list of children
      */
     public ArrayList<ASTNode> getChildren() {
         return children;
     }
 
     /**
      * Sets the children.
      * 
      * @param children
      *            the children to set
      */
     public void setChildren(ArrayList<ASTNode> children) {
         this.children = children;
     }
 
     /**
      * Gets the integer value.
      * 
      * @return the integer value
      */
     public int getIvalue() {
         return ivalue;
     }
 
     /**
      * Sets the integer value.
      * 
      * @param ivalue
      *            the new integer value
      */
     public void setIvalue(int ivalue) {
         this.ivalue = ivalue;
     }
 
     /**
      * Gets the boolean value.
      * 
      * @return the boolean value
      */
     public String getBvalue() {
         return bvalue;
     }
 
     /**
      * Sets the boolean value.
      * 
      * @param bvalue
      *            the new boolean value
      */
     public void setBvalue(String bvalue) {
         this.bvalue = bvalue;
     }
 
     /**
      * Gets the String value.
      * 
      * @return the String value
      */
     public String getSvalue() {
         return svalue;
     }
 
     /**
      * Sets the String value.
      * 
      * @param svalue
      *            the new String value
      */
     public void setSvalue(String svalue) {
         this.svalue = svalue;
     }
 
     /**
      * Gets the char value.
      * 
      * @return the char value
      */
     public char getCvalue() {
         return cvalue;
     }
 
     /**
      * Sets the char value.
      * 
      * @param cvalue
      *            the new char value
      */
     public void setCvalue(char cvalue) {
         this.cvalue = cvalue;
     }
 
     /**
      * Gets the double value.
      * 
      * @return the double value
      */
     public double getDvalue() {
         return dvalue;
     }
 
     /**
      * Sets the double value.
      * 
      * @param dvalue
      *            the new double value
      */
     public void setDvalue(double dvalue) {
         this.dvalue = dvalue;
     }
 }
