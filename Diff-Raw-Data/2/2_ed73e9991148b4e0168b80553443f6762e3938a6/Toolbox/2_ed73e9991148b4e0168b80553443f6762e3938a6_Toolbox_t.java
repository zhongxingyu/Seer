 package main;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import data.Shape;
 import data.ShapeData;
 import fileio.FileIOPanel;
 
 public class Toolbox extends JPanel 
 {
 
 	private static final long serialVersionUID = 1L;
 	ShapeData shapeData;
 	JButton btnNewObject = new JButton("New Object");
 	/**
 	 * Create the panel.
 	 */
 	public Toolbox(final ShapeData shapeData, ActionListener changeColor,
 			 ActionListener newFile, ActionListener save, ActionListener load) 
 	{		
 		this.shapeData = shapeData;
 		this.setPreferredSize(new Dimension(200,600));
 
 		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 
 		ShapeList shapeList = new ShapeList(shapeData);
 		JScrollPane scrollPane = new JScrollPane(shapeList);
 		
 //		JList list = new JList(new String[]{"lol","lol2"}); //data has type Object[]
 //		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 //		list.setLayoutOrientation(JList.VERTICAL_WRAP);
 //		list.setVisibleRowCount(-1);
 //		JScrollPane scrollPane = new JScrollPane(list);
 		scrollPane.setPreferredSize(new Dimension(200, 300));
 
		btnNewObject = new JButton("New Object");
 		btnNewObject.addActionListener(new ActionListener() 
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) 
 			{
 				if (shapeData.getShapes().size() != 0) {
 					shapeData.endShape(shapeData.getIndex());
 				}
 				shapeData.addShape(new Shape());
 			}
 		});
 		add(btnNewObject);
 
 		JButton btnSelectObject = new JButton("Move Object");
 		add(btnSelectObject);
 
 		ColorPalette colourPalette = new ColorPalette(shapeData, changeColor);
 		add(colourPalette);
 		add(scrollPane);
 		
 		FileIOPanel fileIO = new FileIOPanel(newFile, save, load);
 		add(fileIO);
 	}
 	
 	public void changeShapeData(final ShapeData shapeData)
 	{
 		btnNewObject.addActionListener(new ActionListener() 
 		{
 			@Override
 			public void actionPerformed(ActionEvent e) 
 			{
 				shapeData.addShape(new Shape());
 			}
 		});
 	}
 
 }
