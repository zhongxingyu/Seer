 package main;
 
 import graph.Edge;
 import graph.Graph;
 import graph.ListGraphs;
 import graph.Vertex;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import shell.MainFrame;
 
 public class Controller {
 	private ListGraphs listGraphs;
 	private MainFrame mainFrame;
 
 	private Algoritm algoritm;
 
 	private Point pointSelectionBegin;
 	private Point pointSelectionEnd;
 	private Point pointDragged;
 
 	private boolean selection;
 	private boolean dragged;
 	private boolean algoritmFlag;
 
 	public Controller() {
 		listGraphs = new ListGraphs();
 
 		algoritm = new Algoritm(this);
 
 		pointSelectionBegin = new Point(0, 0);
 		pointSelectionEnd = new Point(0, 0);
 		pointDragged = new Point(0, 0);
 		dragged = false;
 		selection = false;
 	}
 
 	public void startActions() {
 		create();
 	}
 
 	public void setView(MainFrame mainFrame) {
 		this.mainFrame = mainFrame;
 	}
 
 	public void repaint() {
 		mainFrame.paint();
 	}
 
 	public Graph getCurrentGraph() {
 		return listGraphs.get(mainFrame.getCurrentIdGraph());
 	}
 
 	// ----------------------------------------
 	public void setStatusDragged(boolean flag) {
 		dragged = flag;
 		// repaint();
 	}
 
 	public boolean isStatusDragged() {
 		return dragged;
 	}
 
 	public void setStatusSelection(boolean flag) {
 		selection = flag;
 		// repaint();
 	}
 
 	public boolean isStatusSelection() {
 		return selection;
 	}
 
 	// public boolean isAlgoritmFlag() {
 	// return algoritmFlag;
 	// }
 	//
 	// public void setAlgoritmFlag(boolean algoritmFlag) {
 	// this.algoritmFlag = algoritmFlag;
 	// }
 
 	// ----------------------------------------
 	// public void selectActualObject(int id, Point p) {
 	// numActualVertex = getGraph(id).getIndexVertex(
 	// getGraph(id).findVertex(p));
 	// numActualEdge = getGraph(id).getIndexEdge(getGraph(id).findEdge(p));
 	// }
 	// ----------------------------------------
 	// ----------------------------------------
 	public boolean checkPointIfEmpty(Point point) {
 		if (getCurrentGraph().findVertex(point) == null
 				&& getCurrentGraph().findEdge(point) == null) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean checkPointIfVertex(Point point) {
 		if (getCurrentGraph().findVertex(point) != null) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean checkPointIfEdge(Point point) {
 		if (getCurrentGraph().findEdge(point) != null) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean checkIfActive(Point point) {
 		Graph graph = getCurrentGraph();
 		if (graph.findVertex(point) != null) {
 			if (graph.findVertex(point).isActivate()) {
 				return true;
 			}
 		}
 		if (graph.findEdge(point) != null) {
 			if (graph.findEdge(point).isActive()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// ------------------------------------------
 	public void renameVertex(Point point) {
 		String name = JOptionPane.showInputDialog("Input name");
 		if (name != null) {
 			getCurrentGraph().findVertex(point).setName(name);
 		}
 	}
 
 	public void renameSelectedVertexes() {
 		for (Vertex vertex : getCurrentGraph().getVertexes()) {
 			if (vertex.isActivate()) {
 				String name = JOptionPane.showInputDialog("Input name");
 				if (name != null) {
 					vertex.setName(name);
 				}
 			}
 		}
 	}
 
 	public void resizeEdge(Point point) {
 		String lenght = JOptionPane.showInputDialog("Input size");
 		try {
 			int size = Integer.parseInt(lenght);
 			if (checkSize(size)) {
 				getCurrentGraph().findEdge(point).resize(size);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void resizeSelectedEdges() {
 		for (Edge edge : getCurrentGraph().getEdges()) {
 			if (edge.isActive()) {
 				String lenght = JOptionPane.showInputDialog("Input size");
 				try {
 					int size = Integer.parseInt(lenght);
 					if (checkSize(size)) {
 						edge.resize(size);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private boolean checkSize(int size) {
 		if (size >= 0 && size < 2000000000) {
 			return true;
 		}
 		return false;
 	}
 
 	// ----------------------------------------
 	public void addVertex(Point p) {
 		Vertex vertex = new Vertex(p.getX(), p.getY());
 		getCurrentGraph().addVertex(vertex);
 		repaint();
 	}
 
 	// ------------------------------------------
 	public void setPointSelectionBegin(Point point) {
 		pointSelectionBegin = point;
 	}
 
 	public void setPointSelectionEnd(Point point) {
 		pointSelectionEnd = point;
 	}
 
 	public Rectangle2D getSelectionBorder() {
 		double x1 = (double) pointSelectionBegin.getX();
 		double y1 = (double) pointSelectionBegin.getY();
 		double x2 = (double) pointSelectionEnd.getX();
 		double y2 = (double) pointSelectionEnd.getY();
 		if (x1 > x2) {
 			double temp = x1;
 			x1 = x2;
 			x2 = temp;
 		}
 		if (y1 > y2) {
 			double temp = y1;
 			y1 = y2;
 			y2 = temp;
 		}
 		return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
 	}
 
 	public void setSelectionObjects() {
 		for (Vertex vertex : getCurrentGraph().getVertexes()) {
 			vertex.activeOff();
 			if (vertex.isVertexInArea(pointSelectionBegin, pointSelectionEnd)) {
 				vertex.activeOn();
 			}
 		}
 		for (Edge edge : getCurrentGraph().getEdges()) {
 			edge.activeOff();
 			if (edge.isEdgeInArea(pointSelectionBegin, pointSelectionEnd)) {
 				edge.activeOn();
 			}
 		}
 	}
 
 	// -------------------------------------
 	public void setPointDragged(Point point) {
 		pointDragged = point;
 	}
 
 	public void dragObjects(Point point) {
 		double x = (double) pointDragged.getX() - (double) point.getX();
 		double y = (double) pointDragged.getY() - (double) point.getY();
 
 		for (Vertex vertex : getCurrentGraph().getVertexes()) {
 			if (vertex.isActivate()) {
 				vertex.setPositionVertex(vertex.getX() - x, vertex.getY() - y);
 			}
 		}
 		pointDragged = point;
 		repaint();
 	}
 
 	// -------------------------------------
 
 	// public void runAlgoritm() throws InterruptedException {
 	// algoritm.runAlgoritm(getGraph());
 	// }
 
 	// public boolean[][] getVertexLabels() {
 	// return algoritm.getVertexLabels();
 	// }
 
 	// public void repaintPanel() {
 	// mainFrame.repaintPanel();
 	// }
 
	private int getCurrentIdGraph() {
		return mainFrame.getCurrentIdGraph();
	}

 	// -------------------------------------
 	// COMMANDS
 	public void create() {
 		listGraphs.add(new Graph());
 		int id = listGraphs.getIdLastGraph();
 		mainFrame.addTab(id, "Graph " + id);
 	}
 
 	public void open() {
 		JFileChooser fileopen = new JFileChooser();
 		fileopen.setCurrentDirectory(new File(".\\save"));
 		if (fileopen.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
 			try {
 				getCurrentGraph().loadFile(
 						fileopen.getSelectedFile().getAbsolutePath());
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 		repaint();
 	}
 
 	public void save() {
 		JFileChooser fileopen = new JFileChooser();
 		fileopen.setCurrentDirectory(new File(".\\save"));
 		if (fileopen.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
 			try {
 				getCurrentGraph().saveFile(
 						fileopen.getSelectedFile().getAbsolutePath());
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void close() {
 		listGraphs.remove(mainFrame.getCurrentIdGraph());
 		mainFrame.removeTab();
 	}
 
 	public void exit() {
 		System.exit(0);
 	}
 
 	public void removeSelectedObjects() {
		// TODO Auto-generated method stub

 	}
 
 	public void activateAll() {
		// TODO Auto-generated method stub

 	}
 
 	public void removeTempEdge() {
 		getCurrentGraph().removeTempEdge();
 	}
 }
