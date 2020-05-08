 package org.suite.node;
 
 import org.util.Util;
 
 public class Vector extends Node {
 
 	private static class Data { // Immutable
 		private Node nodes[];
 		private int startUsed, endUsed;
 
 		private Data() {
 			this(16);
 		}
 
 		private Data(int len) {
 			this(len, len * 3 / 4);
 		}
 
 		private Data(int len, int startUsed) {
 			this.nodes = new Node[len];
 			this.endUsed = this.startUsed = startUsed;
 		}
 
 		private void insertBefore(Node n[], int s, int e) {
 			int l1 = e - s;
 			startUsed -= l1;
 			System.arraycopy(n, s, nodes, startUsed, l1);
 		}
 
 		private void insertAfter(Node n[], int s, int e) {
 			int l1 = e - s;
 			endUsed += l1;
 			System.arraycopy(n, s, nodes, endUsed, l1);
 		}
 	}
 
 	private Data data;
 	private int start, end;
 
 	private Vector(Data data, int start, int end) {
 		this.data = data;
 		this.start = start;
 		this.end = end;
 	}
 
 	public void insertBefore(Vector v) {
 		Data data1;
 		int vlen = v.end - v.start;
 
 		if (start != data.startUsed || start < vlen) {
 			int l0 = data.nodes.length, l1 = l0;
 			while (l1 < l0 + vlen)
 				l1 += 1 + l1 >> 1;
 
 			data1 = new Data(l1, l1 - (end - start));
 			data1.insertAfter(data.nodes, start, end);
 		} else
 			data1 = data;
 
 		data1.insertBefore(v.data.nodes, v.start, v.end);
 	}
 
 	public void insertAfter(Vector v) {
 		Data data1;
 		int vlen = v.end - v.start;
 
 		if (end != data.endUsed || data.nodes.length - end < vlen) {
 			int l1 = calculateNewSize(vlen);
 
 			data1 = new Data(l1, 0);
 			data1.insertAfter(data.nodes, start, end);
 		} else
 			data1 = data;
 
 		data1.insertAfter(v.data.nodes, v.start, v.end);
 	}
 
 	private int calculateNewSize(int nNodesToBeAdded) {
 		int l0 = data.nodes.length, l1 = l0;
 		int l = l0;
 		while (l < l1 + nNodesToBeAdded)
 			l += l >> 1;
 		return l1;
 	}
 
 	public Vector subVector(int s, int e) {
 		return new Vector(data, start + s, start + e);
 	}
 
 	@Override
 	public int hashCode() {
 		int result = 1;
 		for (int i = start; i < end; i++) {
 			int h = Util.hashCode(data.nodes[i + data.startUsed]);
 			result = 31 * result + h;
 		}
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		boolean result = false;
 
 		if (object instanceof Node) {
 			Node node = ((Node) object).finalNode();
 			if (node instanceof Vector) {
 				Vector v = (Vector) node;
 				result = end - start == v.end - v.start;
				int si = start + data.startUsed;
				int di = v.start + v.data.startUsed;
 
 				while (result && si < end)
 					result &= Util.equals(data.nodes[si++], v.data.nodes[di++]);
 			}
 		}
 
 		return result;
 	}
 
 }
