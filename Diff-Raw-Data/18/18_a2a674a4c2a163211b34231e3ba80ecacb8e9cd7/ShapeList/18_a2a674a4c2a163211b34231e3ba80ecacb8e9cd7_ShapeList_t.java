 package main;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import data.Shape;
 import data.ShapeData;
 
 
 public class ShapeList extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	static DefaultListModel listModel = new DefaultListModel();
 	static JList list;
 
 	public ShapeList(ShapeData shapeData) {
 
 		ArrayList<Shape> shapes = shapeData.getShapes();
 		for (Shape shape : shapes) {
 			listModel.addElement(shape);
 		}
 		this.setLayout(new GridLayout(1,1));
 		list = new JList(listModel);
 		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		list.addListSelectionListener(new MyListSelectionListener());
 
 		list.setCellRenderer(new MyCellRenderer());
 		list.setBorder(new EmptyBorder(5,5,5,5));
 		add(list);
 	}
 
	public static void removeShape() {
		listModel.remove(God.shapeData.listSelection);
 	}
 
 	public static void addShape(Shape shape) {
 		listModel.addElement(shape);
 	}
 
 	public void emptyList()
 	{
 		listModel.removeAllElements();
 	}
 	
 
 
 	private class MyListSelectionListener implements ListSelectionListener {
 
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if(!e.getValueIsAdjusting())
 			{
 				God.shapeData.listSelection = getSelectedIndex();
 				System.out.println("God.shapeData.listSelection " + God.shapeData.listSelection);
 				God.vertexPanel.paintComponent(God.vertexPanel.getGraphics());
 			}
 		}
 		
 		public int getSelectedIndex()
 		{
 			return list.getSelectedIndex();
 		}
 
 	}
 
 }
