 //----------------------------------------------------------------------------
 // $Id$ 
 //----------------------------------------------------------------------------
 
 package hexgui.sgf;
 
 import hexgui.hex.HexColor;
 import hexgui.hex.HexPoint;
 import hexgui.hex.Move;
 import hexgui.game.Node;
 import hexgui.game.GameInfo;
 
 import java.io.*;
 import java.awt.Dimension;
 import java.util.Map;
 import java.util.Iterator;
 
 //----------------------------------------------------------------------------
 
/** Writes a in sgf format. */
 public final class SgfWriter
 {
     
     /** Write a game tree. */
     public SgfWriter(OutputStream out, Node root, GameInfo game)
     {
 	m_out = new PrintStream(out);
 	m_buffer = new StringBuffer(128);
 	m_gameinfo = game;
 
 	writeTree(root, true);
 	print("\n");
 	flushBuffer();
 	m_out.flush();
     }
 
     private void writeTree(Node root, boolean isroot)
     {
 	print("(");
 	writeNode(root, isroot);
 	print(")");
     }
 
     private void writeNode(Node node, boolean isroot)
     {
 	print(";");
 
 	if (isroot) {
 	    String value;
 
 	    node.setSgfProperty("FF", "4");
 	    node.setSgfProperty("AP", "HexGui:0.2");
 	    node.setSgfProperty("GM", "11");
 	    
 	    Dimension dim = m_gameinfo.getBoardSize();
 	    value = Integer.toString(dim.width);
 	    if (dim.width != dim.height)
 		value += ":" + Integer.toString(dim.height);
 	    node.setSgfProperty("SZ", value);
 
 	}
 
 	if (node.getMove() != null)
 	    printMove(node.getMove());
 
 	Map map = node.getProperties();
 	Iterator it = map.entrySet().iterator();
 	while(it.hasNext()) {
 	    Map.Entry e = (Map.Entry)it.next();
 	    String key = (String)e.getKey();
 	    String val = (String)e.getValue();
 	    print(key + "[" + val + "]");
 	}
 	
 	int num = node.numChildren();
 	if (num == 0) return;
 
 	if (num == 1) {
 	    writeNode(node.getChild(), false);
 	    return;
 	} 
 	
 	for (int i=0; i<num; i++) 
 	    writeTree(node.getChild(i), false);
 	
     }
 
     private void printMove(Move move)
     {
 	String color = "B";
 	if (move.getColor() == HexColor.WHITE)
 	    color = "W";
 	print(color + "[" + move.getPoint().toString() + "]");
     }
 
     private void print(String str)
     {
 	if (m_buffer.length() + str.length() > 72) {
 	    m_out.append(m_buffer.toString());
 	    m_out.append("\n");
 	    m_buffer.setLength(0);
 	}
 	m_buffer.append(str);
     }
 
     private void flushBuffer()
     {
 	m_out.append(m_buffer.toString());
 	m_buffer.setLength(0);
     }
     private PrintStream m_out;
     private StringBuffer m_buffer;
     private GameInfo m_gameinfo;
 }
 
 //----------------------------------------------------------------------------
