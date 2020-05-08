 import java.awt.Color;
 import java.util.ArrayList;
 
 import y.base.Edge;
 import y.base.EdgeCursor;
 import y.base.Node;
 import y.view.Arrow;
 import y.view.EdgeRealizer;
 import y.view.GenericEdgeRealizer;
 import y.view.LineType;
 
 
 public final class Commands {
 	private static String[] noChange = { "highlight", "highlight-queue", "highlight-constraint", "unhighlight-constraint" };
 	
 	public static boolean processIfNoChange (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		assert(answer.size() > 1);
 		assert(answer.get(0) instanceof Symbol);
 		Symbol key = (Symbol)answer.get(0);
 		for (String s : noChange)
 			if (key.isEqual(s)) {
 				processHighlights(ihl, answer, demo);
 				return true;
 			}
 		return false;
 	}
 	
 	public static void processCommand (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		assert(answer.size() > 1);
 		assert(answer.get(0) instanceof Symbol);
 		Symbol key = (Symbol)answer.get(0);
 		ArrayList ch = ihl.changes.get(ihl.changes.size() - 1);
 		//System.out.println("processing " + key);
 
 		if (key.isEqual("beginning"))
 			beginning(ihl, answer, demo);
 		else {
 			if (! ihl.graphfinished) {
 				if (! (key.isEqual("highlight") || key.isEqual("highlight-queue") || key.isEqual("relayouted") || key.isEqual("highlight-constraint") || key.isEqual("unhighlight-constraint")))
 					ihl.numChanges++;
 				if (! key.isEqual("relayouted"))
 					ch.add(answer);
 			}
 			if (key.isEqual("relayouted")) {
 				if (ihl.numChanges > ihl.lastRelayout) {
 					ihl.synchronizeGraphOperations(ch, ihl.chindex, true);
 					ihl.chindex = ch.size();
					ihl.lastRelayout = ihl.numChanges;
 				}
 			} else if (demo.debug.isSelected()) {
 				if (execute(ihl, answer, demo) && (ihl.changed == false))
 					ihl.changed = true;
 				ihl.chindex = ch.size();
 			}
 			processHighlights(ihl, answer, demo);
 		}
 		//System.out.println("counters: lastchangecount " + ihl.lastChangeCount + " numchanges " + ihl.numChanges + " chindex " + ihl.chindex + " lastRelayout " + ihl.lastRelayout + " lastentry " + ihl.lastEntry + " lastslidervalue " + ihl.lastslidervalue);
 	}
 	
 	public static boolean execute (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		assert(answer.size() > 1);
 		assert(answer.get(0) instanceof Symbol);
 		Symbol key = (Symbol)answer.get(0);
 		//System.out.println("executing " + key);
 
 		if (key.isEqual("change-edge"))
 			return changeedge(ihl, answer);
 		if (key.isEqual("remove-edge"))
 			return removeedge(ihl, answer);
 		if (key.isEqual("insert-edge"))
 			return insertedge(ihl, answer);
 		if (key.isEqual("new-computation"))
 			return newcomputation(ihl, answer);
 		if (key.isEqual("remove-computation"))
 			return removenode(ihl, answer, false);
 		if (key.isEqual("add-temporary"))
 			return addtemporary(ihl, answer);
 		if (key.isEqual("add-temporary-user"))
 			return addtemporaryuser(ihl, answer);
 		if (key.isEqual("remove-temporary-user"))
 			return removetemporaryuser(ihl, answer);
 		if (key.isEqual("temporary-generator"))
 			return temporarygenerator(ihl, answer);
 		if (key.isEqual("remove-temporary"))
 			return removenode(ihl, answer, true);
 		if (key.isEqual("change-type"))
 			return changetype(ihl, answer);
 		if (key.isEqual("change-entry-point"))
 			return changeentrypoint(ihl, answer);
 		if (key.isEqual("change-function"))
 			return changefunction(ihl, answer);
 		if (key.isEqual("set-loop-call-loop"))
 			return setloopcallloop(ihl, answer);
 		
 		if (key.isEqual("new-type-variable"))
 			return new_type_var(ihl, answer);
 		if (key.isEqual("new-type-node"))
 			return new_type_node(ihl, answer);
 		if (key.isEqual("connect"))
 			return connect(ihl, answer, demo);
 		if (key.isEqual("disconnect"))
 			return disconnect(ihl, answer, demo);
 		if (key.isEqual("remove-node"))
 			return removetypenode(ihl, answer, demo);
 		if (key.isEqual("type-relation"))
 			return typerelation(ihl, answer);
 		
 		for (String s : noChange)
 			if (key.isEqual(s))
 				return processHighlights(ihl, answer, demo);
 		System.out.println("shouldn't be here");
 		return false;
 	}
 
 	private static boolean processHighlights (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		assert(answer.size() > 1);
 		assert(answer.get(0) instanceof Symbol);
 		Symbol key = (Symbol)answer.get(0);
 		if (key.isEqual("highlight"))
 			return highlight(ihl, answer);
 		if (key.isEqual("highlight-queue"))
 			return highlightqueue(ihl, answer);
 		if (key.isEqual("highlight-constraint"))
 			return highlightedge(ihl, answer, demo, true);
 		if (key.isEqual("unhighlight-constraint"))
 			return highlightedge(ihl, answer, demo, false);
 		return false;
 	}
 	
 	private static boolean setloopcallloop(IncrementalHierarchicLayout ihl, ArrayList answer) {
 		Node loopcall = getNode(ihl, answer, 2, false);
 		Node loop = getNode(ihl, answer, 3, true);
 		if (loop != null)
 			ihl.graph.createEdge(loopcall, loop);
 		return false;
 	}
 
 	private static Node getNode (IncrementalHierarchicLayout ihl, ArrayList answer, int index, boolean maybenull) {
 		assert(answer.size() >= index);
 		assert(answer.get(index) instanceof Integer);
 		Integer nodeid = (Integer)answer.get(index);
 		Node n = ihl.int_node_map.get(nodeid);
 		if (! maybenull) {
 			assert(ihl.int_node_map.containsKey(nodeid));
 			assert(n != null);
 		}
 		return n;
 	}
 	
 	private static boolean beginning (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		if (ihl.lastRelayout < ihl.numChanges) {
 			ihl.synchronizeGraphOperations(ihl.changes.get(ihl.changes.size() - 1), ihl.chindex, true);
 			ihl.lastRelayout = ihl.numChanges;
 		}
 		assert(answer.get(2) instanceof ArrayList);
 		ArrayList mess = (ArrayList)answer.get(2);
 		assert(mess.get(0) instanceof String);
 		String ph = (String)mess.get(0);
 		if (mess.size() == 2) {
 			if (mess.get(1) instanceof Integer) {
 				Node n = getNode(ihl, mess, 1, false);
 				String label = ((GraphNodeRealizer)ihl.graph.getRealizer(n)).getSliderText();
 				ph = ph + " " + label; //or label text? but might be too long
 			} else if (mess.get(1) instanceof Symbol) {
 				Symbol tag = (Symbol)mess.get(1);
 				if (tag.isEqual("global")) {
 					//demo.phase.setText(ph);
 					//demo.phase.validate();
 					return false;
 				}
 			}
 		}
 		ihl.updatephase(ph);
 		return false;
 	}
 	
 	private static boolean changeedge (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 6);
 		assert(answer.get(5) instanceof Symbol);
 		Node from = getNode(ihl, answer, 2, false);
 		Node toold = getNode(ihl, answer, 3, false);
 		Node tonew = getNode(ihl, answer, 4, false);
 		Symbol label = (Symbol)answer.get(5);
 		if (label.isEqual("no"))
 			label = null;
 		Edge change = null;
 		for (EdgeCursor ec = from.outEdges(); ec.ok(); ec.next())
 			if (ec.edge().target() == toold)
 				if (label == null || label.isEqual(ihl.graph.getRealizer(ec.edge()).getLabelText())) {
 					change = ec.edge();
 					break;
 				}
 		if (change != null) {
 			ihl.graph.changeEdge(change, from, tonew);
 			return true;
 		} else
 			if (ihl.safeCreateEdge(from, tonew)) {
 				System.out.println("only created edge");
 				if (label != null)
 					ihl.setEdgeLabel(label);
 				return true;
 			}
 		return false;
 	}
 	
 	private static boolean removeedge (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 5);
 		assert(answer.get(4) instanceof Symbol);
 		Node from = getNode(ihl, answer, 2, false);
 		Node to = getNode(ihl, answer, 3, false);
 		Symbol label = (Symbol)answer.get(4);
 		if (label.isEqual("no"))
 			label = null;
 		for (EdgeCursor ec = from.outEdges(); ec.ok(); ec.next())
 			if (ec.edge().target() == to)
 				if (label == null || label.isEqual(ihl.graph.getRealizer(ec.edge()).getLabelText())) {
 					ihl.graph.removeEdge(ec.edge());
 					return true;
 				}
 		System.out.println("FAILED");
 		return false;
 	}
 	
 	private static boolean insertedge (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 5);
 		Node from = getNode(ihl, answer, 2, false);
 		Node to = getNode(ihl, answer, 3, false);
 		assert(answer.get(4) instanceof Symbol);
 		Symbol label = (Symbol)answer.get(4);
 		if (label.isEqual("no"))
 			label = null;
 		if (ihl.safeCreateEdge(from, to)) {
 			if (label != null)
 				ihl.setEdgeLabel(label);
 			//System.out.println("connected!");
 			return true;
 		}
 		return false;
 	} 
 	
 	private static boolean newcomputation (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 3);
 		assert(answer.get(2) instanceof ArrayList);
 		ArrayList text = (ArrayList)answer.get(2);
 		ihl.createNewNode(text);
 		return true;
 	} 
 	
 	private static boolean removenode (IncrementalHierarchicLayout ihl, ArrayList answer, boolean mayfail) {
 		assert(answer.size() == 3);
 		Node del = getNode(ihl, answer, 2, mayfail);
 		if (del != null) {
 			//System.out.println("D:" + del.degree() + " " + ihl.graph.getRealizer(del).getLabelText());
 			if (del.degree() > 0)
 				for (EdgeCursor ec = del.edges(); ec.ok(); ec.next()) {
 					//System.out.println("  was connected to " + ihl.graph.getRealizer(ec.edge().opposite(del)).getLabelText());
 				}
 			ihl.graph.removeNode(del);
 			ihl.topnodes.remove(del);
 			ihl.int_node_map.remove((Integer)answer.get(2));
 			return true;
 		}
 		return false;
 	} 
 	
 	private static boolean addtemporary (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 5);
 		assert(answer.get(2) instanceof Integer);
 		assert(answer.get(3) instanceof String);
 		assert(answer.get(4) instanceof Integer);
 		int temp_id = (Integer)answer.get(2);
 		String text = (String)answer.get(3);
 		int c_id = (Integer)answer.get(4);
 		if (ihl.int_node_map.get(temp_id) == null) {
 			ihl.createTemporary(temp_id, c_id, text);
 			return true;
 		}
 		//System.out.println("already added temporary " + temp_id + " " + text);
 		return false;
 	}
 	
 	private static boolean addtemporaryuser (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 4);
 		Node temp = getNode(ihl, answer, 2, false);
 		Node comp = getNode(ihl, answer, 3, false);
 		ihl.graph.createEdge(temp, comp);
 		ihl.setEdgeColor(Color.blue);
 		return true;
 	} 
 	
 	private static boolean removetemporaryuser (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 4);
 		Node temp = getNode(ihl, answer, 2, true);
 		if (temp == null) {
 			//System.out.println("temp not present " + (Integer)answer.get(2));
 			return false; //happens with arguments of inlined functions
 		}
 		Node comp = getNode(ihl, answer, 3, false);
 		for (EdgeCursor ec = temp.outEdges(); ec.ok(); ec.next())
 			if (ec.edge().target() == comp) {
 				ihl.graph.removeEdge(ec.edge());
 				return true;
 			}
 		return false;
 	} 
 	
 	private static boolean temporarygenerator (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 5);
 		Node temp = getNode(ihl, answer, 2, false);
 		Node newgenerator = getNode(ihl, answer, 3, false);
 		Node oldgenerator = getNode(ihl, answer, 4, true);
 		if (oldgenerator != null)
 			for (EdgeCursor ec = temp.inEdges(); ec.ok(); ec.next())
 				if (ec.edge().source() == oldgenerator) {
 					ihl.graph.changeEdge(ec.edge(), temp, newgenerator);
 					ihl.scf.removeConstraints(temp);
 					return true;
 				}
 		if (ihl.safeCreateEdge(newgenerator, temp)) {
 			ihl.setEdgeColor(Color.blue);
 			ihl.scf.addPlaceNodeInSameLayerConstraint(temp, newgenerator);
 			return true;
 		}
 		return false;
 	} 
 	
 	private static boolean changetype (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 4);
 		Node n = getNode(ihl, answer, 2, true);
 		if (n != null) {
 			assert(answer.get(3) instanceof String);
 			GraphNodeRealizer nr = (GraphNodeRealizer)ihl.graph.getRealizer(n);
 			nr.setNodeTypeText((String)answer.get(3));
 			//System.out.println("change type " + old + " => " + (String)answer.get(3));
 		}
 		return false;
 	}
         
 	private static boolean changeentrypoint(IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 4);
 		Node n = getNode(ihl, answer, 2, false);
 		assert(answer.get(3) instanceof Symbol);
 		GraphNodeRealizer nr = (GraphNodeRealizer)ihl.graph.getRealizer(n);
 		nr.setPrefix(((Symbol)answer.get(3)).toString());
 		//System.out.println("change entry point " + old + " => " + ((Symbol)answer.get(3)).toString());
 		return true;		
 	}
 	
 	private static boolean changefunction(IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 4);
 		Node n = getNode(ihl, answer, 2, false);
 		assert(answer.get(3) instanceof String);
 		GraphNodeRealizer nr = (GraphNodeRealizer)ihl.graph.getRealizer(n);
 		nr.setSuffix((String)answer.get(3));
 		return true;		
 	}
 
 	private static boolean highlight (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		Node highlightnew = null;
 		if ((Integer)answer.get(2) != 0)
 			highlightnew = getNode(ihl, answer, 2, false);
 		if (ihl.highlight != highlightnew) {
 			if (ihl.highlight != null)
 				((GraphNodeRealizer)ihl.graph.getRealizer(ihl.highlight)).setHighlight(false);
 			if (highlightnew != null)
 				((GraphNodeRealizer)ihl.graph.getRealizer(highlightnew)).setHighlight(true);
 			ihl.highlight = highlightnew;
 		}
 		return false;
 	} 
 		
 	private static boolean highlightqueue (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.get(2) instanceof ArrayList);
 		ArrayList<Integer> queue = (ArrayList<Integer>)answer.get(2);
 		for (Integer n : ihl.opt_queue)
 			if (! queue.contains(n)) {
 				Node unh = ihl.int_node_map.get(n);
 				if (unh != null)
 					((GraphNodeRealizer)ihl.graph.getRealizer(unh)).setOptimizationQueue(false);
 			}
 		for (Integer n : queue) {
 			Node h = ihl.int_node_map.get(n);
 			if (h != null)
 				((GraphNodeRealizer)ihl.graph.getRealizer(h)).setOptimizationQueue(true);
 		}		
 		ihl.opt_queue = queue;
 		return false;
 	}
 
 	private static boolean new_type_var (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 5);
 		//Node object = getNode(ihl, answer, 3, false);
 		assert(answer.get(3) instanceof Integer);
 		assert(answer.get(2) instanceof Integer);
 		String t = null;
 		if (answer.get(4) instanceof Integer)
 			t = Integer.toString((Integer)answer.get(4));
 		else {
 			if (answer.get(4) instanceof String)
 				t = (String)answer.get(4);
 			else if (answer.get(4) instanceof Symbol)
 				t = ((Symbol)answer.get(4)).toString();
 		}
 		ihl.createTypeVariable((Integer)answer.get(2), (Integer)answer.get(3), t);
 		ihl.typechanged = true;
 		return false;
 	}
 	
 	private static boolean new_type_node (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 4);
 		assert(answer.get(2) instanceof Integer);
 		int id = (Integer)answer.get(2);
 		if (answer.get(3) instanceof Integer) {
 			Node object = getNode(ihl, answer, 3, false);
 			ihl.createTypeNode(id, object);
 		} else {
 			Node nnode = null;
 			//got a "base type" / String or Symbol
 			if (answer.get(3) instanceof Symbol) //arrow or tuple!
 				nnode = ihl.createTypeNodeWithLabel(((Symbol)answer.get(3)).toString(), id);
 			else
 				nnode = ihl.createTypeNodeWithLabel((String)answer.get(3), id);
 			ihl.typeHintMap.set(nnode, ihl.typeHintsFactory.createLayerIncrementallyHint(nnode));
 		}
 		ihl.typechanged = true;
 		return false;
 	}
 	
 	private static boolean typerelation (IncrementalHierarchicLayout ihl, ArrayList answer) {
 		assert(answer.size() == 4);
 		Node temp = getNode(ihl, answer, 3, false);
 		Node type = getNode(ihl, answer, 2, false);
 		ihl.tv_temp_map.put(type, temp);
 		GraphNodeRealizer nr = (GraphNodeRealizer)ihl.typegraph.getRealizer(type);
 		nr.setReference((Integer)answer.get(3));
 		return false;
 	}
 	
 	private static boolean removetypenode (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		assert(answer.size() == 3);
 		Node del = getNode(ihl, answer, 2, false);
 		if (del != null) {
 			ihl.typegraph.removeNode(del);
 			ihl.int_node_map.remove((Integer)answer.get(2));
 			ihl.tv_temp_map.remove(del);
 			//ihl.typechanged = true;
 			demo.typeview.repaint();
 		}
 		return false;
 	}
 	
 	private static boolean connect (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		assert(answer.size() == 5);
 		Node from = getNode(ihl, answer, 2, false);
 		Node to = getNode(ihl, answer, 3, false);
 		EdgeRealizer er = new GenericEdgeRealizer(ihl.typegraph.getDefaultEdgeRealizer());
 		assert(answer.get(4) instanceof Symbol);
 		Symbol type = (Symbol)answer.get(4); 
 		if (type.isEqual("<constraint-edge>")) {
 			er.setLineColor(Color.GREEN);
 			er.setArrow(Arrow.NONE);
 			er.setLineType(LineType.DASHED_2);
 		} else if (type.isEqual("<representative-edge>")) {
 			er.setLineColor(Color.red);
 			er.setLineType(LineType.DOTTED_2);
 		} else
 			er.setLineColor(Color.BLUE);
 		ihl.typegraph.createEdge(from, to, er);
 		if (! (type.isEqual("<representative-edge>")))
 			ihl.typescf.addPlaceNodeBelowConstraint(from, to);
 		demo.typeview.repaint();
 		return false;
 	}
 	
 	private static boolean disconnect (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo) {
 		assert(answer.size() == 5);
 		Node from = getNode(ihl, answer, 2, false);
 		Node to = getNode(ihl, answer, 3, false);
 		assert(answer.get(4) instanceof Symbol);
 		Symbol type = (Symbol)answer.get(4);
 		Color nc = Color.BLUE;
 		if (type.isEqual("<constraint-edge>"))
 			nc = Color.green;
 		else if (type.isEqual("<representative-edge>"))
 			nc = Color.red;
 		for (EdgeCursor ec = from.outEdges(); ec.ok(); ec.next())
 			if (ec.edge().target() == to)
 				if (ihl.typegraph.getRealizer(ec.edge()).getLineColor() == nc)
 					ihl.typegraph.removeEdge(ec.edge());
 		demo.typeview.repaint();
 		return false;
 	}
 	
 	private static boolean highlightedge (IncrementalHierarchicLayout ihl, ArrayList answer, DemoBase demo, boolean thick) {
 		Node from = getNode(ihl, answer, 2, false);
 		Node to = getNode(ihl, answer, 3, false);
 		LineType lt = LineType.DASHED_2;
 		if (thick)
 			lt = LineType.DASHED_4;
 		for (EdgeCursor ec = from.outEdges(); ec.ok(); ec.next())
 			if (ec.edge().target() == to)
 				ihl.typegraph.getRealizer(ec.edge()).setLineType(lt);
 		demo.typeview.repaint();
 		return false;
 	}
 }
