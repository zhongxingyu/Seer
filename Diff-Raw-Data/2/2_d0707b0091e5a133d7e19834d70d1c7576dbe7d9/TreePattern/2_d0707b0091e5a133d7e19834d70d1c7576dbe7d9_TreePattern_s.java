 package nuthatch.pattern.impl;
 
 import nuthatch.pattern.Environment;
 import nuthatch.pattern.NotBuildableException;
 import nuthatch.tree.TreeCursor;
 
 public class TreePattern<Value, Type> extends MinimalAbstractPattern<Value, Type> {
 	private final TreeCursor<Value, Type> pattern;
 
 
 	public TreePattern(TreeCursor<Value, Type> tree) {
 		if(tree == null) {
 			throw new IllegalArgumentException("Tree must not be null");
 		}
 
 		this.pattern = tree;
 	}
 
 
 	@Override
 	public <T extends TreeCursor<Value, Type>> T build(T tree, Environment<T> env) throws NotBuildableException {
 		throw new UnsupportedOperationException();
 	}
 
 
 	@Override
 	public <T extends TreeCursor<Value, Type>> boolean match(T tree, Environment<T> env) {
		return pattern.equals(tree);
 	}
 
 }
