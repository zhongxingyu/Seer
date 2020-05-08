 // KNOWN LIMITATION: Doesn't handle 'octopus merges'. Suppports at most 2 parents per rev.
 /* Class to draw the revision graph.
  *
  * Changes Copyright (C) 2010, 2011 Darrell Karbott (see below).
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either
  * version 2.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * Author: djk@isFiaD04zgAgnrEC5XJt1i4IE7AkNPqhBG5bONi6Yks (changes)
  *
  *  This file was developed as component of
  * "fniki" (a wiki implementation running over Freenet).
  *
  * This file is a derived work based on the graphlog.py
  * from rev:643b8212813e in the mercurial hg-stable repo.
  *
  * sha1sum graphlog.py
  * a0e599d4bd0b483025f0b5f182e737d8682a88ba  graphlog.py
  *
  * Original header:
  * # ASCII graph log extension for Mercurial
  * #
  * # Copyright 2007 Joel Rosdahl <joel@rosdahl.net>
  * #
  * # This software may be used and distributed according to the terms of the
  * # GNU General Public License version 2 or any later version.
  *
  */
 
 package fniki.wiki;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class GraphLog {
 
     ////////////////////////////////////////////////////////////
     // A quick and dirty port of the first ~= 200 lines of
     // graphlog.py from the mercurial codebase.
     ////////////////////////////////////////////////////////////
 
     public static ColumnData asciiedges(List<Integer> seen, int rev, List<Integer> parents) {
         // """adds edge info to changelog DAG walk suitable for ascii()"""
         if (!seen.contains(rev)) {
             seen.add(rev);
         }
 
         int nodeidx = seen.indexOf(rev); // i.e. just looking up the index of the rev
 
         // seen maps revs -> ordinals
 
         List<Integer> known_parents = new ArrayList<Integer>();
         List<Integer> new_parents = new ArrayList<Integer>();
 
         for (int parent : parents) {
             if (seen.contains(parent)) {
                 known_parents.add(parent);
             } else {
                 new_parents.add(parent);
             }
         }
 
         int ncols = seen.size();
         seen.remove(nodeidx); // Correct? trying to get python slice assignement semantics
         seen.addAll(nodeidx, new_parents);
 
         List<AsciiEdge> edges = new ArrayList<AsciiEdge>();
         for (int parent : known_parents) {
             edges.add(new AsciiEdge(nodeidx, seen.indexOf(parent)));
         }
 
         if (new_parents.size() > 0) {
             edges.add(new AsciiEdge(nodeidx, nodeidx));
         }
 
         if (new_parents.size() > 1) {
             edges.add(new AsciiEdge(nodeidx, nodeidx + 1));
         }
 
         int nmorecols = seen.size() - ncols;
         String edgeDump = "";
         for (AsciiEdge edge : edges) {
             edgeDump += String.format("(%d, %d), ", edge.mStart, edge.mEnd);
         }
         return new ColumnData(nodeidx, edges, ncols, nmorecols);
     }
 
     static void fix_long_right_edges(List<AsciiEdge> edges) {
         int index = 0;
         for (AsciiEdge edge : edges) {
             if (edge.mEnd > edge.mStart) {
                 edges.set(index,  new AsciiEdge(edge.mStart, edge.mEnd + 1));
             }
             index++;
         }
     }
 
     static List<String> get_nodeline_edges_tail(int node_index,
                                              int p_node_index,
                                              int n_columns,
                                              int n_columns_diff,
                                              int p_diff,
                                              boolean fix_tail) {
         if (fix_tail && n_columns_diff == p_diff && n_columns_diff != 0) {
             //# Still going in the same non-vertical direction.
             if (n_columns_diff == -1) {
                 int start = Math.max(node_index + 1, p_node_index);
                 List<String> tail = repeat(BAR_SPACE,  (start - node_index - 1));
                 tail.addAll(repeat(SLASH_SPACE, (n_columns - start)));
                 return tail;
             } else {
                 return repeat(BACKSLASH_SPACE, (n_columns - node_index - 1));
             }
         } else {
             return repeat(BAR_SPACE, (n_columns - node_index - 1));
         }
     }
 
     static void draw_edges(List<AsciiEdge> edges, List<String> nodeline, List<String> interline) {
         int index = 0;
         for (AsciiEdge edge : edges) {
             if (edge.mStart == edge.mEnd + 1) {
                 interline.set(2 * edge.mEnd + 1, "/");
             } else if (edge.mStart == edge.mEnd - 1) {
                 interline.set(2 * edge.mStart + 1, "\\");
             } else if (edge.mStart == edge.mEnd) {
                 interline.set(2 * edge.mStart, "|");
             } else {
                 nodeline.set(2 * edge.mEnd, "+");
                 if (edge.mStart > edge.mEnd) {
                    edges.set(index, new AsciiEdge(edge.mEnd, edge.mStart));
                 }
 
                 for (int j =  2 * edge.mStart + 1; j < 2 * edge.mEnd; j++) {
                     if (!nodeline.get(j).equals("+")) {
                         nodeline.set(j, "-");
                     }
                 }
             }
 
             index++;
         }
     }
 
     static List<String> get_padding_line(int ni, int n_columns, List<AsciiEdge> edges) {
         List<String> line = new ArrayList<String>();
         line.addAll(repeat(BAR_SPACE, ni));
 
         String c = null;
         //if (ni, ni - 1) in edges or (ni, ni) in edges:
         if (edges.contains(new AsciiEdge(ni, ni - 1)) ||
             edges.contains(new AsciiEdge(ni, ni))) {
             //# (ni, ni - 1)      (ni, ni)
             //# | | | |           | | | |
             //# +---o |           | o---+
             //# | | c |           | c | |
             //# | |/ /            | |/ /
             //# | | |             | | |
             c = "|";
         } else {
             c = " ";
         }
         line.add(c);
         line.add(" ");
 
         line.addAll(repeat(BAR_SPACE, (n_columns - ni - 1)));
         return line;
     }
 
     public static AsciiState asciistate() {
         //"""returns the initial value for the "state" argument to ascii()"""
         return new AsciiState(0, 0);
     }
 
     static boolean  should_add_padding_line(List<String> text, int coldiff, List<AsciiEdge> edges) {
         //add_padding_line = (len(text) > 2 and coldiff == -1 and
         //                [x for (x, y) in edges if x + 1 < y])
         if (!(text.size() > 2 && coldiff == -1)) {
             return false;
         }
         for (AsciiEdge edge : edges) {
             if (edge.mStart + 1 < edge.mEnd) {
                 return true;
             }
         }
         return false;
     }
 
     // # Called once for each entry in the dag list
     public static void ascii(Writer ui, AsciiState state, /* type, */
                       String nodeChar, List<String> text,
                       ColumnData coldata) throws IOException {
         //"""prints an ASCII graph of the DAG
         //
         //takes the following arguments (one call per node in the graph):
 
         //- ui to write to
         //- Somewhere to keep the needed state in (init to asciistate())
         //- Column of the current node in the set of ongoing edges.
         //- Type indicator of node data == ASCIIDATA.
         //- Payload: (char, lines):
         //- Character to use as node's symbol.
         //- List of lines to display as the node's text.
         //- Edges; a list of (col, next_col) indicating the edges between
         //  the current node and its parents.
         //- Number of columns (ongoing edges) in the current revision.
         //- The difference between the number of columns (ongoing edges)
         //  in the next revision and the number of columns (ongoing edges)
         //  in the current revision. That is: -1 means one column removed;
         //  0 means no columns added or removed; 1 means one column added.
         // """
 
         int idx = coldata.mNodeIdx;
         List<AsciiEdge> edges = coldata.mEdges;
         int ncols = coldata.mNCols;
         int coldiff = coldata.mMoreCols;
 
         assert_(coldiff > -2  && coldiff < 2);
 
         if (coldiff == -1) {
             //# Transform
             //#
             //#     | | |        | | |
             //#     o | |  into  o---+
             //#     |X /         |/ /
             //#     | |          | |
             fix_long_right_edges(edges);
         }
 
         //# add_padding_line says whether to rewrite
         //#
         //#     | | | |        | | | |
         //#     | o---+  into  | o---+
         //#     |  / /         |   | |  # <--- padding line
         //#     o | |          |  / /
         //#                    o | |
         //add_padding_line = (len(text) > 2 and coldiff == -1 and
         //                [x for (x, y) in edges if x + 1 < y])
 
         boolean add_padding_line = should_add_padding_line(text, coldiff, edges);
 
         //# fix_nodeline_tail says whether to rewrite
         //#
         //#     | | o | |        | | o | |
         //#     | | |/ /         | | |/ /
         //#     | o | |    into  | o / /   # <--- fixed nodeline tail
         //#     | |/ /           | |/ /
         //#     o | |            o | |
         boolean fix_nodeline_tail = (text.size() <= 2) && (!add_padding_line);
 
         // # nodeline is the line containing the node character (typically o)
         List<String> nodeline = repeat(BAR_SPACE, idx);
         nodeline.add(nodeChar);
         nodeline.add(" ");
 
         nodeline.addAll(get_nodeline_edges_tail(idx, state.mIdx, ncols, coldiff,
                                                 state.mColDiff, fix_nodeline_tail));
 
         //# shift_interline is the line containing the non-vertical
         //# edges between this entry and the next
         List<String> shift_interline = repeat(BAR_SPACE, idx);
         String edge_ch = null;
         int n_spaces = -1;
         if (coldiff == -1) {
             n_spaces = 1;
             edge_ch = "/";
         } else if (coldiff == 0) {
             n_spaces = 2;
             edge_ch = "|";
         } else {
             n_spaces = 3;
             edge_ch = "\\";
         }
 
         shift_interline.addAll(repeat(ONE_SPACE, n_spaces));
         List<String> valueToRepeat = new ArrayList<String>();
         valueToRepeat.add(edge_ch);
         valueToRepeat.add(" ");
         shift_interline.addAll(repeat(valueToRepeat, (ncols - idx - 1)));
 
         //# draw edges from the current node to its parents
         draw_edges(edges, nodeline, shift_interline);
 
         //# lines is the list of all graph lines to print
         List<String> lines = new ArrayList<String>(Arrays.asList(list_to_string(nodeline)));
         if (add_padding_line) {
             lines.add(list_to_string(get_padding_line(idx, ncols, edges)));
         }
         lines.add(list_to_string(shift_interline));
 
         //# make sure that there are as many graph lines as there are
         //# log strings
         while (text.size() < lines.size()) {
             text.add("");
         }
 
         if (lines.size() < text.size()) {
             String extra_interline = list_to_string(repeat(BAR_SPACE, (ncols + coldiff)));
             while (lines.size() < text.size()) {
                 lines.add(extra_interline);
             }
         }
         //# print lines
         int indentation_level = Math.max(ncols, ncols + coldiff);
         assert_(lines.size() == text.size());
         int index = 0;
         for (String line : lines) {
             String logstr = text.get(index);
             index++;
             // "%-*s => means "left justify string with width specified in the tuple"
             // ln = "%-*s %s" % (2 * indentation_level, "".join(line), logstr)
             String fmt = "%-" + ("" + (2 * indentation_level)).trim() + "s %s";
             ui.write(rstrip(String.format(fmt, line, logstr)));
             ui.write("\n");
         }
 
         state.mColDiff = coldiff;
         state.mIdx = idx;
     }
 
     ////////////////////////////////////////////////////////////
     // Helper data structures and functions used to port
     // to Java.
     ////////////////////////////////////////////////////////////
     static final class AsciiEdge {
         public final int mStart;
         public final int mEnd;
         AsciiEdge(int start, int end) { mStart = start; mEnd = end; }
     }
 
     public static final class ColumnData {
         public final int mNodeIdx;
         public final int mNCols;
         public final List<AsciiEdge> mEdges;
         public final int mMoreCols;
 
         public ColumnData(int nodeidx, List<AsciiEdge> edges, int ncols, int nmorecols) {
             mNodeIdx = nodeidx;
             mEdges = edges;
             mNCols = ncols;
             mMoreCols = nmorecols;
         }
     }
 
     // NOT immutable. Hrmmm..
     public static class AsciiState {
         public int mColDiff;
         public int mIdx;
         public AsciiState(int coldiff, int idx) { mColDiff = coldiff; mIdx = idx; }
     }
 
     // A directed graph node.
     public static class DAGNode implements Comparable<DAGNode> {
         public final String mTag;
         public final int mId;
         public final List<Integer> mParentIds;
         DAGNode(String tag, int id, List<Integer> parentIds) {
             mTag = tag;
             mId = id;
             mParentIds = parentIds;
         }
         public int compareTo(DAGNode other) {
             return other.mId - mId; // Descending order of integer id.
         }
 
         public String asString() {
             return "{" + mTag + ", " + mId + ", " + GraphLog.prettyString_(mParentIds) + "}";
         }
     }
 
     final static List<String> BAR_SPACE =
         Collections.unmodifiableList(Arrays.asList("|", " "));
 
     final static List<String> SLASH_SPACE =
         Collections.unmodifiableList(Arrays.asList("/", " "));
 
     final static List<String> BACKSLASH_SPACE =
         Collections.unmodifiableList(Arrays.asList("\\", " "));
 
     final static List<String> ONE_SPACE =
         Collections.unmodifiableList(Arrays.asList(" "));
 
     static List<String> repeat(List<String> value, int count) {
         List<String> ret = new ArrayList<String>();
         while (count > 0) {
             ret.addAll(value);
             count--;
         }
         return ret;
     }
 
     static void assert_(boolean condition) {
         if (condition) { return; }
         throw new RuntimeException("Assertion Failure!");
     }
 
     // PARANOID: Can get rid of the explicit  iteration later if this is too slow.
     static String list_to_string(List<String> values) {
         StringBuilder sb = new StringBuilder();
         for (String singleChar : values) {
             assert_(singleChar.length() == 1);
             sb.append(singleChar);
         }
         return sb.toString();
     }
 
     static String rstrip(String text) {
         if (text.length() == 0) {
             return "";
         }
         int pos = text.length() - 1;
         while (pos >= 0 && text.charAt(pos) == ' ') {
             pos--;
         }
         if (pos == text.length() - 1) {
             return text;
         }
         return text.substring(0, pos + 1);
     }
 
     ////////////////////////////////////////////////////////////
     // New code written on top of the ported mercurial stuff.
     ////////////////////////////////////////////////////////////
 
     public static class GraphEdge {
         public final String mFrom;
         public final String mTo;
         public GraphEdge(String from, String to) { mFrom = from; mTo = to; }
         public boolean equals(Object obj) {
             if (obj == null || (!(obj instanceof GraphEdge))) {
                 return false;
             }
             GraphEdge other = (GraphEdge)obj;
             return mFrom.equals(other.mFrom) && mTo.equals(other.mTo);
         }
         public int hashCode() { return (mFrom + mTo).hashCode(); } // Hmmm... fast enough?
     }
 
     static class ParentInfo {
         public final Set<String> mAllParents;
         public final Set<String> mRootIds;
         public ParentInfo(Set<String> allParents,
                           Set<String> rootIds) {
             mAllParents = allParents;
             mRootIds = rootIds;
         }
     }
 
     static class OrdinalMapper {
         private final Map<String, Integer> mLut = new HashMap<String, Integer>();
         private int mCount;
 
         public OrdinalMapper() {}
 
         public int ordinal(String for_value) {
             if (!mLut.keySet().contains(for_value)) {
                 mLut.put(for_value, mCount);
                 mCount++;
             }
             return mLut.get(for_value);
         }
         public void dump() {
             List<String> keys = new ArrayList<String>(mLut.keySet());
             Collections.sort(keys);
             for (String key : keys) {
                 System.err.println(String.format("%s -> %d", key, mLut.get(key)));
             }
         }
     }
 
     static void remove_edges(List<GraphEdge> edges, String matchingFrom) {
         while (true) {
             top_of_while:
             for (int index = 0; index < edges.size(); index++) {
                 if (edges.get(index).mFrom.equals(matchingFrom)) {
                     edges.remove(index);
                     continue top_of_while; // for
                 }
             }
             break; // while
         }
     }
 
     static Set<String> allDescendants(Map<String, Set<String>> graph, String leaf_id) {
         Set<String> seen = new HashSet<String>();
         List<String> stack = new ArrayList<String>();
         stack.add(leaf_id);
 
         while (stack.size() > 0) {
             String edge_id = stack.remove(stack.size() - 1);
             if (seen.contains(edge_id)) {
                 continue;
             }
 
             seen.add(edge_id);
             if (!graph.keySet().contains(edge_id)) {
                 continue;
             }
 
             for (String parent : graph.get(edge_id)) {
                 stack.add(parent);
             }
         }
         return seen; // # Hmmmm... includes leaf_id
     }
 
     static List<Map<String, Set<String>>> find_connected_subgraphs(Map<String, Set<String>> graph,
                                                                    Set<String> root_ids,
                                                                    Set<String> leaf_node_ids) {
         List<Set<String>> subgraph_id_sets = new ArrayList<Set<String>>();
         for (String leaf_id : leaf_node_ids) {
             subgraph_id_sets.add(allDescendants(graph, leaf_id));
         }
 
         //# Coalesce subgraphs which have common root nodes
         for(String key : root_ids) {
             Set<String> coalesced = new HashSet<String>();
 
             // # Deep copy the list so I can mutate while iterating
             List<Set<String>> copyForMutatingIteration = new ArrayList<Set<String>>();
             copyForMutatingIteration.addAll(subgraph_id_sets);
             for (Set<String> subgraph_id_set : copyForMutatingIteration) {
                 if (!subgraph_id_set.contains(key)) {
                     continue;
                 }
 
                 coalesced.addAll(subgraph_id_set);
                 subgraph_id_sets.remove(subgraph_id_set);
             }
 
             if (coalesced.size() > 0) {
                 subgraph_id_sets.add(coalesced);
             }
 
             if (subgraph_id_sets.size() < 2) {
                 break; //# Bail out if there is only one graph
             }
         }
 
         List<Map<String, Set<String>>> subgraphs = new ArrayList<Map<String, Set<String>>>();
         for (Set<String> subgraph_id_set : subgraph_id_sets) {
             Map<String, Set<String>>copy = new HashMap<String, Set<String>>();
             for (String id_value : subgraph_id_set) {
                 if (graph.keySet().contains(id_value)) { // # handle unseen parents
                     Set<String> deepCopy = new HashSet<String>();
                     deepCopy.addAll(graph.get(id_value));
                     copy.put(id_value, deepCopy);
                 }
             }
             //dumpGraph("subgraph", copy);
             subgraphs.add(copy);
         }
         return subgraphs;
     }
 
     // graph is a map of node identifiers -> set of parent nodes.
     public static Map<String, Set<String>> buildRawGraph(List<GraphEdge> edges) {
         Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
         for (GraphEdge edge : edges) {
             Set<String> parents = graph.get(edge.mTo);
             if (parents == null) {
                 parents = new HashSet<String> ();
                 graph.put(edge.mTo, parents);
             }
             parents.add(edge.mFrom);
         }
         return graph;
     }
 
     // DCI: fix name
     public static ParentInfo getParentInfo(Map<String, Set<String>> graph) {
         Set<String> allParents = new HashSet<String>();
         for (Set<String> parents : graph.values()) {
             allParents.addAll(parents);
         }
 
         Set<String> unseenParents = new HashSet<String>();
         unseenParents.addAll(allParents);
         unseenParents.removeAll(graph.keySet());
 
         Set<String> rootIds = new HashSet<String>();
 
         for(Map.Entry<String, Set<String>> entry : graph.entrySet()) {
             Set<String> parents = entry.getValue();
             if (parents == null || parents.size() == 0) {
                 rootIds.add(entry.getKey());
                 continue;
             }
 
             assert_(parents.size() > 0);
 
             Set<String> unknownParents =  new HashSet<String>();
 
             unknownParents.addAll(parents);
             unknownParents.removeAll(unseenParents);
 
             // BUG: ??? Revisit. what if only some parents are unknown?
             //# All parents are unknown
             if (unknownParents.size() == 0) {
                 rootIds.add(entry.getKey());
             }
         }
 
         return new ParentInfo(allParents, rootIds);
     }
 
     // Graph maps from child -> list of parents (i.e. like a commit DAG).
     static void set_ordinals_in_plausible_commit_order(Map<String, Set<String>> graph,
                                                        OrdinalMapper ordinals) {
 
         // Make a list of all  parent to child edges.
         // Each edge represents the constraint "the ordinal of this parent is less
         // than the ordinal of this child".
         List<GraphEdge> edges = new ArrayList<GraphEdge>();
         Set<String> leaf_nodes = new HashSet<String>();
         for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
             String child = entry.getKey();
             Set<String> parents = entry.getValue();
             if (parents.size() == 0) {
                 leaf_nodes.add(child);
                 continue;
             }
 
             for (String parent : parents) {
                 edges.add(new GraphEdge(parent, child));
             }
         }
 
         while (!edges.isEmpty()) {
             // Find edges to vertices that no other edges reference.
             Set<String> candidates = new HashSet<String>();
             Set<String> referenced = new HashSet<String>();
             for (GraphEdge edge : edges) {
                 candidates.add(edge.mFrom);
                 referenced.add(edge.mTo);
             }
             // i.e. "the parents which no remaining children reference."
             // Implies must have lower ordinals than all unprocessed children.
             candidates.removeAll(referenced);
             assert_(candidates.size() > 0);
 
             List<String> sorted = new ArrayList<String>(candidates);
             Collections.sort(sorted);
             for (String candidate : sorted) {
                 // Set the ordinal.
                 ordinals.ordinal(candidate);
                 // Remove any unreferenced edges from the list.
                  // LATER: fix crappy code. do in one pass outside loop.
                 remove_edges(edges, candidate);
             }
         }
 
         // Set the ordinals for nodes which have no children.
         List<String> sorted_leaf_nodes = new ArrayList<String>(leaf_nodes);
         Collections.sort(sorted_leaf_nodes);
         for (String leaf_node : sorted_leaf_nodes) {
             ordinals.ordinal(leaf_node);
         }
     }
 
     // Create a list of graph nodes in the correct order so that
     // they can be used to draw the graph with the ascii() function.
     static List<DAGNode> build_dag(Map<String, Set<String>> graph, String null_rev_name) {
         assert_(!graph.keySet().contains(null_rev_name));
 
         OrdinalMapper ordinals = new OrdinalMapper();
 
         int null_rev_ordinal = ordinals.ordinal(null_rev_name);
 
         //# Important: This sets the graph ordinals correctly.
         set_ordinals_in_plausible_commit_order(graph, ordinals);
 
         List<DAGNode> dag = new ArrayList<DAGNode>();
         for(Map.Entry<String, Set<String>> entry : graph.entrySet()) {
             String child = entry.getKey();
 
             List<Integer> parent_ids = new ArrayList<Integer>();
             for (String parent : entry.getValue()) {
                 parent_ids.add(ordinals.ordinal(parent));
             }
 
             if (parent_ids.size() == 1 && parent_ids.get(0) == null_rev_ordinal) {
                 // Causes ascii() not to draw a line down from the 'o'.
                 parent_ids = new ArrayList<Integer>();
             }
 
             dag.add(new DAGNode(child, ordinals.ordinal(child), parent_ids));
         }
 
         //ordinals.dump();
 
         // IMPORTANT: Sort in order of descending ordinal otherwise drawing code won't work.
         Collections.sort(dag);
         return dag;
     }
 
     // BUG: detect cylces?
     // REQUIRES: edges form an acyclic directed graph.
     public static List<List<DAGNode>> build_dags(List<GraphEdge> edges, String null_rev_name) {
         //""" Creates a list of DAG lists that can be printed with the ascii()
         //    method,  from an unordered list of edges. """
 
         Map<String, Set<String>> graph = buildRawGraph(edges);
         ParentInfo parentInfo = getParentInfo(graph);
         Set<String> all_parents = parentInfo.mAllParents;
         Set<String> root_ids = parentInfo.mRootIds;
 
         Set<String> leaf_node_ids = new HashSet<String>();
         leaf_node_ids.addAll(graph.keySet());
         leaf_node_ids.removeAll(all_parents);
 
         List<Map<String, Set<String>>> subgraphs = find_connected_subgraphs(graph,
                                                                             root_ids,
                                                                             leaf_node_ids);
         List<List<DAGNode>> dags = new ArrayList<List<DAGNode>>();
         for (Map<String, Set<String>> subgraph : subgraphs) {
             dags.add(build_dag(subgraph, null_rev_name));
         }
         return dags;
     }
 
     private final static String prettyString(Set<String> set) {
         List<String> list = new ArrayList<String>();
         list.addAll(set);
         Collections.sort(list);
         return prettyString(list);
     }
 
     private final static String prettyString(List<String> list) {
         String ret = "{";
         for (String value : list) {
             ret += value + ", ";
         }
         ret = ret.trim() + "}";
         return ret;
     }
 
     // GRRRR... Colliding type erasure. Java is a poor man's C++ :-(
     static String prettyString_(List<Integer> values) {
         List<String> list = new ArrayList<String>();
         for (Integer value : values) {
             list.add(value.toString());
         }
         return prettyString(list);
     }
 
     private final static void dumpGraph(String msg, Map<String, Set<String>> graph) {
         System.err.println("--- graph: " + msg);
         List<String> keys = new ArrayList<String>();
         keys.addAll(graph.keySet());
         Collections.sort(keys);
 
         for (String key : keys) {
             System.err.println(String.format("   [%s] -> [%s]", key, prettyString(graph.get(key))));
         }
     }
     // File format:
     // <child_rev> <parent_rev>
     // one entry per line, lines starting with # and // are ignored.
     private final static List<GraphEdge> readEdgesFromFile(String fileName) throws IOException {
         List<GraphEdge> list = new ArrayList<GraphEdge>();
 
         LineNumberReader reader = new LineNumberReader(new FileReader(fileName));
         String line = reader.readLine();
 
         while(line != null) {
             if (line.trim().equals("") || line.startsWith("#") || line.startsWith("//")) {
                 line = reader.readLine();
                 continue;
             }
             String[] fields = line.trim().split(" ");
             list.add(new GraphEdge(fields[1], fields[0]));
             line = reader.readLine();
         }
         return list;
     }
 
     public static void test_dumping_graph(List<DAGNode> dag) throws IOException {
         List<Integer> seen = new ArrayList<Integer>();
         AsciiState state = asciistate();
         Writer out = new OutputStreamWriter(System.out);
         for (DAGNode value : dag) {
             List<String> lines = new ArrayList<String>();
             lines.add(String.format("id: %s (%d)", value.mTag, value.mId));
             ascii(out, state, "o", lines, asciiedges(seen, value.mId, value.mParentIds));
         }
         out.flush();
     }
 
     public final static void main(String[] args) throws Exception {
         System.out.println(String.format("Reading edges from: %s", args[0]));
         List<GraphEdge> edges = readEdgesFromFile(args[0]);
         List<List<DAGNode>> dags = build_dags(edges, "null_rev");
 
         for (List<DAGNode> dag : dags) {
             System.out.println("--- dag ---");
             test_dumping_graph(dag);
         }
     }
 }
