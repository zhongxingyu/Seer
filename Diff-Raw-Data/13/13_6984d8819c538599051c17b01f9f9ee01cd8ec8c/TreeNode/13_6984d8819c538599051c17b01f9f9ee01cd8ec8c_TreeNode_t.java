 package com.mayroro.util.tree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TreeNode {
 	private String name;
 	private double data;
 	private TreeNode parent;
 	private List<TreeNode> children;
 	
 	private MautFunction mautFunction;
 	private double mautWeight;
 	
 	public TreeNode(){
 		this(null);
 	}
 	public TreeNode(String name){
 		this(name, null);
 	}
 	public TreeNode(String name, TreeNode parent){
 		this(name, 1, parent);
 	}
 	public TreeNode(String name, double mautWeight, TreeNode parent){
 		this.name = name;
 		this.parent = parent;
 		this.mautWeight = mautWeight;
 		children = new ArrayList<TreeNode>();
 		
 		this.data = -1;
 		this.mautFunction = null;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public double getData() {
 		return data;
 	}
 	public void setData(double data) {
 		this.data = data;
 	}
 	public TreeNode getParent() {
 		return parent;
 	}
 	public void setParent(TreeNode parent) {
 		this.parent = parent;
 		if (parent.getChild(this.name) == null)
 			parent.addChild(this);
 	}
 	public List<TreeNode> getChildren() {
 		return children;
 	}
 	public void setChildren(List<TreeNode> children) {
 		this.children = children;
 	}
 	public TreeNode getChild(int child){
 		return children.get(child);
 	}
 	public TreeNode getChild(String name){
 		for (TreeNode child : children){
 			if (name.equals(child.getName()))
 				return child;
 		}
 		return null;
 	}
 	public TreeNode addChild(TreeNode child){
 		if (child == null)
 			return null;
 		this.children.add(child);
 		if (child.getParent() != this)
 			child.setParent(child);
 		return child;
 	}
 	public TreeNode addChild(String name){
 		return addChild(name, -1);
 	}
 	public TreeNode addChild(String name, double mautWeight){
 		return addChild(new TreeNode(name, mautWeight, this));
 	}
 	public void removeChild(String name){
 		this.children.remove(this.getChild(name));
 	}
 	public void removeChild(int child){
 		this.children.remove(child);
 	}
 
 	public double getMautWeight() {
 		return mautWeight;
 	}
 	public void setMautWeight(double mautWeight) {
 		this.mautWeight = mautWeight;
 	}
 	public MautFunction getMautFunction() {
 		return mautFunction;
 	}
 	public void setMautFunction(MautFunction mautFunction) {
 		this.mautFunction = mautFunction;
 	}
 	
 	public double calculateValue() {
 		double value = -1;
 		// vozlie
 		if (this.children.size() > 0){
 			value = 0;
 			for (TreeNode child : this.children){
 				value += child.calculateValue();
 			}
			value = value*mautWeight;
 		}
 		// list
 		else if (mautFunction != null) {
 			value = mautFunction.calculateValue(data)*mautWeight;
 		}
 		return value;
 	}
 	public List<TreeNode> getAllNodes(){
 		List<TreeNode> nodes = new ArrayList<TreeNode>();
 		nodes.add(this);
 		for (TreeNode node : this.children){
 			nodes.addAll(node.getAllNodes());
 		}
 		return nodes;
 	}
 	public List<TreeNode> getLeafNodes(){
 		List<TreeNode> nodes = new ArrayList<TreeNode>();
 		if (children.size() == 0)
 			nodes.add(this);
 		else{
 			for (TreeNode node : children){
 				nodes.addAll(node.getLeafNodes());
 			}
 		}
 		return nodes;
 	}
 	
 	public void cleanName(){
 		String n = name.substring("<input type=\"text\" value=\"".length());
 		name = n.substring(0,n.indexOf('"'));
 	}
 	
 	public String toString(){
 		return toString("");
 	}
 	public String toString(String tab){
		String s = String.format("Name: %s, Data: %f, Parent: %s, Weight: %f\n", this.name, this.data, this.parent == null ? "null" : this.parent.getName(), this.mautWeight);
 		for (TreeNode child : this.children){
 			s = s.concat(tab+"\t"+child.toString(tab+"\t"));
 		}
 		return s;
 	}
 }
