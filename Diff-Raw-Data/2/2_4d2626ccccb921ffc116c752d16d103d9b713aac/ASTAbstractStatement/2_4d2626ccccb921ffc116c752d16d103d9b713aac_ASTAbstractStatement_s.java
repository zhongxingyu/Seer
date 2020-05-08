 package org.ita.testrefactoring.astparser;
 
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.ita.testrefactoring.codeparser.Block;
 import org.ita.testrefactoring.codeparser.Statement;
 
 public class ASTAbstractStatement<T extends ASTNode> implements Statement, ASTWrapper<T> {
 
 	private Block parentBlock;
 	private T astObject;
 	
 	@Override
 	public Block getParent() {
 		return parentBlock;
 	}
 	
 	protected void setParent(Block block) {
 		parentBlock = block;
 	}
 
 	@Override
 	public void setASTObject(T astObject) {
 		this.astObject = astObject;
 		
 	}
 
 	@Override
 	public T getASTObject() {
 		return astObject;
 	}
 
 	@Override
 	public String toString() {
		return astObject.toString();
 	}
 }
