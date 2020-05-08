 package org.tekkotsu.stateMachine.parts;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.awt.geom.RectangularShape;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.eclipse.draw2d.geometry.*;
 
 import javax.annotation.PostConstruct;
 import org.tekkotsu.api.*;
 
 import org.eclipse.draw2d.*;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Ellipse;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureCanvas;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Panel;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.ToolbarLayout;
 import org.eclipse.draw2d.Triangle;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.XYLayout;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.tekkotsu.api.DefaultClassReader;
 import org.tekkotsu.api.NodeClass;
 import org.tekkotsu.api.TransitionClass;
 public class NodesPart  {
 	@PostConstruct
 	public void createUserInterface(Composite parent) throws IOException {
 			parent.setLayout(new GridLayout(2, false));
 		
 		
 		
 			// Enable a label as a Drag Source
 			final Label dragLabel = new Label(parent, SWT.BORDER);
 			dragLabel.setText("Drag this text");
 			
 		
			Label foo = new Label(parent, SWT.NONE);
			foo.setText("foo");
 			 
 			// Allow data to be copied or moved from the drag source
			int operations = DND.DROP_COPY;
 			DragSource source = new DragSource(dragLabel, operations);
 			 
 			// Provide data in Text format
 			Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
 			source.setTransfer(types);
 			new Label(parent, SWT.NONE);
 			
 			Label lblTest = new Label(parent, SWT.NONE);
 			lblTest.setText("test");
 			
 			/*Ellipse ellipse=new Ellipse();
 			
 			ellipse.setBackgroundColor(ColorConstants.darkBlue);
 			ellipse.setPreferredSize(60, 40);
 			Rectangle blah = new Rectangle();
 			//blah.setDimension(ellipse.getPreferredSize());
 			//((IFigure) lblTest).add(ellipse, new Rectangle());*/
 			
 			
 			
 			//Canvas creation---***
 			Canvas canvas = new Canvas(parent, SWT.NONE);
 			GridData gd_canvas = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			canvas.setBackground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
 			gd_canvas.heightHint = 65;
 			canvas.setLayoutData(gd_canvas);
 			//Create root figure and layout container
 		
			//ArrayList<NodeClass> nodes = new DefaultClassReader().getNodes();
			//ArrayList<TransitionClass> transitions= new DefaultClassReader().getTransitions();
 			
 			
 		
 
 	
 	
 			
 			canvas.addPaintListener(new PaintListener() { 
 		        public void paintControl(PaintEvent e) { 
 		        	
 		         
 		        	Canvas canvas = (Canvas) e.widget;
 		            int x = canvas.getBounds().width;
 		            int y = canvas.getBounds().height;
 		            e.gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		            e.gc.drawPolygon(new int[] { 25,5,45,45,5,45 });
		            e.gc.fillPolygon(new int[] { 25,5,45,45,5,45 });
		            //e.gc.drawText("Turn",16,35, true); 
 		            
 		            
 		            //e.gc.drawRectangle(5,5,50,45);
 		           // e.gc.fillRectangle(5, 5, 50, 45);
 		            
 		            
 		            
 		        }
 
 				
 			});
 			
 			
 	       
 			
 			
 			/*Button btnFind = new Button(parent, SWT.NONE);
 			GridData gd_btnFind = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 			gd_btnFind.widthHint = 99;
 			btnFind.setLayoutData(gd_btnFind);
 			btnFind.setText("Find");
 			new Label(parent, SWT.NONE);
 			
 			Button btnSpeak = new Button(parent, SWT.NONE);
 			GridData gd_btnSpeak = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 			gd_btnSpeak.widthHint = 99;
 			btnSpeak.setLayoutData(gd_btnSpeak);
 			btnSpeak.setText("Speak");
 			new Label(parent, SWT.NONE);
 			
 			Button btnPlay = new Button(parent, SWT.NONE);
 			GridData gd_btnPlay = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 			gd_btnPlay.widthHint = 99;
 			btnPlay.setLayoutData(gd_btnPlay);
 			btnPlay.setText("Play");
 			new Label(parent, SWT.NONE);*/
 			 
 	
 	
 	
 			source.addDragListener(new DragSourceListener() {
 			   public void dragStart(DragSourceEvent event) {
 			      // Only start the drag if there is actually text in the
 			      // label - this text will be what is dropped on the target.
 			      if (dragLabel.getText().length() == 0) {
 			          event.doit = false;
 			      }
 			   }
 			   public void dragSetData(DragSourceEvent event) {
 			     // Provide the data of the requested type.
 			     if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
 			          event.data = dragLabel.getText();
 			     }
 			   }
 			   public void dragFinished(DragSourceEvent event) {
 			     // If a move operation has been performed, remove the data
 			     // from the source
 			     if (event.detail == DND.DROP_MOVE)
 			         dragLabel.setText("");
 			     }
 			   });
 
 	}
 
 		
 	} 
 
