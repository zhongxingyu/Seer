 package com.xidstudios.pixelarena;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 
 public class Node {
 
 	int hValue;
 
 	int moveCostValue;
 
 	int totalValue;
 
 	Node parentNode;
 
 	Vector2 nodeVector;
 
 	Vector2 targetVector;
 
 	Node(Node node, Vector2 p, Vector2 end) {
 		if (p != null) {
 			this.parentNode = node;
 		}
		this.nodeVector = new Vector2((int) p.x / 32,
				(int) p.y / 32);
		this.targetVector = new Vector2((int) end.x / 32,
				(int) end.y / 32);
 		// Gdx.app.log("Node", "Parent " + nodeVector);
 		// Gdx.app.log("Node", "Target " + targetVector);
 		calculateHVal();
 	}
 
 	// just made this constructor to stop me from getting errors form old class
 	public Node(Node node, Vector2 p) {
 		if (p != null) {
 			this.parentNode = node;
 		}
 		this.nodeVector = new Vector2(p.x / 32, p.y / 32);
//		Gdx.app.log("Node", "" + nodeVector);
 	}
 
 	public void calculateHVal() {
 		int x = (int) Math.abs(nodeVector.x - targetVector.x);
 		int y = (int) Math.abs(nodeVector.y - targetVector.y);
 		if (parentNode != null) {
 			hValue += parentNode.hValue;
			Gdx.app.log("Node", "Has Parent:" + hValue);
 		}
 		hValue += (int) (x + y);
//		Gdx.app.log("Node", "H-Val" + hValue);
 	}
 
 	public void setMoveCost(int moveCostValue) {
 		this.moveCostValue = moveCostValue;
 		calculateTotalValue();
 
 	}
 
 	private void calculateTotalValue() {
 		totalValue = moveCostValue + hValue;
 	}
 
 	public int getTotalValue() {
 		return totalValue;
 	}
 
 	public void setParentNode(Node parentNode) {
 		this.parentNode = parentNode;
 	}
 
 	public Vector2 getParentVector() {
 		return nodeVector;
 	}
 
 	public void setParentVector(Vector2 parentVector) {
 		this.nodeVector = parentVector;
 	}
 }
