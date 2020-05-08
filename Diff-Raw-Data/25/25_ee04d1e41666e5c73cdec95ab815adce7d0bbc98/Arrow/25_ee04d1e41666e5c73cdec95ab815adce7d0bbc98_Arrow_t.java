  package com.live.Debugger;
 
 import javax.swing.text.StyleContext.SmallAttributeSet;
 import javax.xml.stream.events.EndElement;
 
 import javafx.beans.binding.IntegerBinding;
 import javafx.beans.binding.NumberBinding;
 import javafx.beans.property.SimpleDoubleProperty;
 import javafx.beans.property.SimpleIntegerProperty;
 import javafx.scene.Group;
 import javafx.scene.shape.Circle;
 import javafx.scene.shape.Line;
 
 
 public class Arrow extends Group{
 
 	String startMethodName;
 	String endMethodName;
 	
 //	SimpleIntegerProperty lineNumber;
 	
 	public Arrow(CodeWindow startWindow, CodeWindow endWindow)
 	{
 		startMethodName = startWindow.getMethodName();
 		endMethodName = endWindow.getMethodName();
 		
 //		IntegerBinding lineNumber = startWindow.selectedLineNumber.add(1);
		int lineNumber = startWindow.getExecutedLine() + 1;
 		
 //		lineNumber.add(startWindow.getEditor().selectedLineNumber.add(1));
 		
 	
 		//calculate the offset for the y position at the start of the arrow
 		//using the current line selected
 //		NumberBinding yLineOffset = lineNumber.multiply(startWindow.lineoffset);
 //		NumberBinding startY = yLineOffset.add(startWindow.getDraggableY()).add(30);
 		NumberBinding startY = startWindow.lineoffset.multiply(lineNumber).add(startWindow.getDraggableY()).add(30);
 		
 		//calculate the offset for the x position at the start of the arrow
 		//using the width of the editor
 		SimpleDoubleProperty xOffset = new SimpleDoubleProperty();
 		NumberBinding endX = xOffset.add(startWindow.getDraggableX()).add(startWindow.getEditor().widthProperty());
 		
 		Line mainLine = new Line();
 		//bind the start position of the arrow to the start method
 		mainLine.startXProperty().bind(endX);
 		mainLine.startYProperty().bind(startY);
 				
 		//bind the end position of the arrow to the end method
 		mainLine.endXProperty().bind(endWindow.getDraggableX());
 		mainLine.endYProperty().bind(endWindow.getDraggableY());
 		
 		//using a circle as the arrowhead
 		Circle circle = new Circle();
 		circle.layoutXProperty().bind(endWindow.getDraggableX());
 		circle.layoutYProperty().bind(endWindow.getDraggableY());
 		circle.radiusProperty().set(5);
 		circle.setFill(javafx.scene.paint.Paint.valueOf("99CCFF"));
 		
 		this.getChildren().add(mainLine);
 		this.getChildren().add(circle);
 	}
 			
 			
 			
 	
 }
