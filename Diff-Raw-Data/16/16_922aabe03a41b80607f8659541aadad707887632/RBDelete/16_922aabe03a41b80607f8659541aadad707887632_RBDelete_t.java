 /*******************************************************************************
  * Copyright (c) 2012 Jakub Kováč, Katarína Kotrlová, Pavol Lukča, Viktor Tomkovič, Tatiana Tóthová
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package algvis.redblacktree;
 
 import algvis.bst.BSTFind;
 import algvis.bst.BSTNode;
 import algvis.core.Algorithm;
 import algvis.core.Node;
 import algvis.core.NodeColor;
 import algvis.core.visual.ZDepth;
 
 import java.util.HashMap;
 
 public class RBDelete extends Algorithm {
 	private final RB T;
 	private final int K;
 
 	public RBDelete(RB T, int x) {
 		super(T.panel);
 		this.T = T;
 		K = x;
 	}
 
 	@Override
 	public void runAlgorithm() throws InterruptedException {
 		setHeader("delete", K);
 		addNote("bstdeletestart");
 		BSTFind find = new BSTFind(T, K, this);
 		find.runAlgorithm();
 		RBNode d = (RBNode) find.getResult().get("node");
 
 		if (d != null) {
 			addToScene(d);
 //			d.setColor(NodeColor.DELETE);
 			d.setColor(NodeColor.FOUND); // TODO aj tak je to jedno, lebo metoda draw zmeni kazdy RBNode na cerveny 
 			// alebo cierny 
 			pause();
 
 			RBNode u = d, w = (u.getLeft() != null) ? u.getLeft() : u
 					.getRight2();
 			T.NULL.setParent(u.getParent2());
 			if (d.isLeaf()) { // case I - list
 				addStep("bst-delete-case1");
 				pause();
 				if (d.isRoot()) {
 					T.setRoot(null);
 				} else if (d.isLeft()) {
 					d.getParent().setLeft(null);
 				} else {
 					d.getParent().setRight(null);
 				}
 			} else if (d.getLeft() == null || d.getRight() == null) {
 				// case IIa - 1 syn
 				addStep("bst-delete-case2");
 				pause();
 				BSTNode s = (d.getLeft() == null) ? d.getRight() : d.getLeft();
 				if (d.isRoot()) {
 					T.setRoot(s);
 					s.setParent(null);
 				} else {
 					s.setParent(d.getParent());
 					if (d.isLeft()) {
 						d.getParent().setLeft(s);
 					} else {
 						d.getParent().setRight(s);
 					}
 				}
 			} else { // case III - 2 synovia
 				addStep("bst-delete-case3");
 				RBNode s = d.getRight();
 				BSTNode v = new BSTNode(T, -Node.INF, ZDepth.ACTIONNODE);
 				v.setColor(NodeColor.FIND);
				addToScene(v);
 				v.goTo(s);
 				pause();
 				while (s.getLeft() != null) {
 					s = s.getLeft();
 					v.goTo(s);
 					pause();
 				}
 				u = s;
 				w = u.getRight2();
 				T.NULL.setParent(u.getParent2());
 				removeFromScene(v);
 				v = s;
 				addToScene(v);
 				((RBNode) v).setRed(d.isRed());
 				if (s.isLeft()) {
 					s.getParent().linkLeft(u.getRight());
 				} else {
 					s.getParent().linkRight(u.getRight());
 				}
 				v.goNextTo(d);
 				pause();
 				if (d.getParent() == null) {
 					v.setParent(null);
 					T.setRoot(v);
 				} else {
 					if (d.isLeft()) {
 						d.getParent().linkLeft(v);
 					} else {
 						d.getParent().linkRight(v);
 					}
 				}
				removeFromScene(v);
 				v.linkLeft(d.getLeft());
 				v.linkRight(d.getRight());
 				v.goTo(d);
 				v.calc();
 			} // end case III
 			d.goDown();
 			removeFromScene(d);
 
 			if (!u.isRed()) {
 				// bubleme nahor
 				while (w.getParent2() != T.NULL && !w.isRed()) {
 					T.NULL.setRed(false);
 					if (w.getParent2().getLeft2() == w) {
 						RBNode s = w.getParent2().getRight2();
 						if (s.isRed()) {
 							addStep("rbdelete1");
 							pause();
 							s.setRed(false);
 							w.getParent2().setRed(true);
 							T.rotate(s);
 						} else if (!s.getLeft2().isRed() && !s.getRight2().isRed()) {
 							addStep("rbdelete2");
 							pause();
 							s.setRed(true);
 							w = w.getParent2();
 						} else if (!s.getRight2().isRed()) {
 							addStep("rbdelete3");
 							pause();
 							s.getLeft2().setRed(false);
 							s.setRed(true);
 							T.rotate(s.getLeft());
 						} else {
 							addStep("rbdelete4");
 							pause();
 							s.setRed(s.getParent2().isRed());
 							w.getParent2().setRed(false);
 							s.getRight2().setRed(false);
 							T.rotate(s);
 							w = (RBNode) T.getRoot();
 						}
 					} else {
 						RBNode s = w.getParent2().getLeft2();
 						if (s.isRed()) {
 							addStep("rbdelete1");
 							pause();
 							s.setRed(false);
 							w.getParent2().setRed(true);
 							T.rotate(s);
 						} else if (!s.getRight2().isRed() && !s.getLeft2().isRed()) {
 							addStep("rbdelete2");
 							pause();
 							s.setRed(true);
 							w = w.getParent2();
 						} else if (!s.getLeft2().isRed()) {
 							s.getRight2().setRed(false);
 							addStep("rbdelete3");
 							pause();
 							s.setRed(true);
 							T.rotate(s.getRight2());
 						} else {
 							addStep("rbdelete4");
 							pause();
 							s.setRed(s.getParent2().isRed());
 							w.getParent2().setRed(false);
 							s.getLeft2().setRed(false);
 							T.rotate(s);
 							w = (RBNode) T.getRoot();
 						}
 					}
 					pause();
 				}
 				w.setRed(false);
 			}
 
 			T.reposition();
 			addStep("done");
 		}
 	}
 
 	@Override
 	public HashMap<String, Object> getResult() {
 		return null; // TODO
 	}
 }
