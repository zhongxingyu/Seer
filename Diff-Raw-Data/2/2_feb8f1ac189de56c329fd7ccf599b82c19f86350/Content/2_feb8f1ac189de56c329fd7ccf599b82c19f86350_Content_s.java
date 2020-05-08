 package applets.Komplexe$Zahlen_Polarkoord_Multiplikation;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 
 import javax.swing.JLabel;
 
 
 
 public class Content {
 
 	Applet applet;
 	PGraph.Point z1Params = new PGraph.Point(Math.sqrt(2), Math.sqrt(2));
 	PGraph.Point z2Params = new PGraph.Point(0, 1);
 	PGraph.Point multParams = new PGraph.Point(-Math.sqrt(2), Math.sqrt(2));	
 	PGraph graph;
 	
 	public Content(Applet applet) {
 		this.applet = applet;		
 	}
 	
 	public void init() {
 		applet.setSize(440, 550);
 	}
 
 	protected String Round(double x) {
 		return "" + (Math.round(x * 10) / 10.0);
 	}
 	
 	public void run() {
 		graph = new PGraph(applet, 400, 400);
 		graph.x_l = -4;
 		graph.x_r = 4;
 		graph.y_o = 4;
 		graph.y_u = -4;
 		graph.showPolarcircles = true;
 		
 		graph.dragablePoints.add(new PGraph.GraphPoint(z1Params, Color.RED, true, true));
 		graph.dragablePoints.add(new PGraph.GraphPoint(z2Params, Color.RED, true, true));
		graph.dragablePoints.add(new PGraph.GraphPoint(multParams, Color.BLUE, true, true));
 		
 		graph.OnDragablePointMoved =
 				new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						double abs1 = z1Params.abs();
 						double angle1 = Math.atan2(z1Params.y, z1Params.x) / Math.PI;
 						double abs2 = z2Params.abs();
 						double angle2 = Math.atan2(z2Params.y, z2Params.x) / Math.PI;
 						
 						multParams.x = z1Params.x * z2Params.x - z1Params.y * z2Params.y;
 						multParams.y = z1Params.x * z2Params.y + z1Params.y * z2Params.x;
 						
 						double absM = multParams.abs();
 						double angleM = Math.atan2(multParams.y, multParams.x) / Math.PI;
 						
 						((JLabel) applet.getComponentByName("z1"))
 						.setText(
 								"z1 = " + Round(z1Params.x) + " + " + Round(z1Params.y) + "i" +
 								" = " + Round(abs1) + "∙( cos(" + Round(angle1) + "π) + i∙sin(" + Round(angle1) + "π) )");
 
 						((JLabel) applet.getComponentByName("z2"))
 						.setText(
 								"z2 = " + Round(z2Params.x) + " + " + Round(z2Params.y) + "i" +
 								" = " + Round(abs2) + "∙( cos(" + Round(angle2) + "π) + i∙sin(" + Round(angle2) + "π) )");
 
 						((JLabel) applet.getComponentByName("multz"))
 						.setText(
 								"z1 * z2 = " + Round(multParams.x) + " + " + Round(multParams.y) + "i" +
 								" = " + Round(absM) + "∙( cos(" + Round(angleM) + "π) + i∙sin(" + Round(angleM) + "π) )");
 
 						((JLabel) applet.getComponentByName("z1")).setForeground(Color.RED);
 						((JLabel) applet.getComponentByName("z2")).setForeground(Color.RED);
 						((JLabel) applet.getComponentByName("multz")).setForeground(Color.BLUE);
 					}
 				};
 		
 		applet.vtmeta.setExtern(new VisualThing[] {
 				new VTImage("graph", 10, 5, graph.getW(), graph.getH(), graph)
 		});	
 	}
 	
 	void postinit() {
 		graph.OnDragablePointMoved.actionPerformed(null);		
 	}
 }
