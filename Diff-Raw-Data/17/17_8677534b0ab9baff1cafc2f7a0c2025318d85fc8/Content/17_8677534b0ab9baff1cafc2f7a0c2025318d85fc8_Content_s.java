 package applets.AnalytischeGeometrieundLA_04_Ebene_StuetzNormRichtung;
 
 import java.awt.Color;
 import java.util.Collection;
 import java.util.Random;
 
import applets.AnalytischeGeometrieundLA_04_Ebene_StuetzNormRichtung.PGraph3D.DynVector3D;
import applets.AnalytischeGeometrieundLA_04_Ebene_StuetzNormRichtung.PGraph3D.Vector3D;

 public class Content {
 
 	Applet applet;
 	PGraph3D graph;
 	
 	public Content(Applet applet) {
 		this.applet = applet;		
 	}
 	
 	public void init() {
 		applet.setSize(620, 620);
 	}
 
 	void postinit() {}
 	void next(int i) {}
 	
 	protected String Round(double x) {
 		return "" + (Math.round(x * 10) / 10.0);
 	}
 	
 	protected PGraph.Point randomChoiceFrom(Collection<PGraph.Point> points) {
 		PGraph.Point[] ar = points.toArray(new PGraph.Point[0]);
 		int i = new Random().nextInt(ar.length);
 		return ar[i];
 	}
 		
 	public void run() {
 		graph = new PGraph3D(applet, 600, 600);		
 		graph.addBaseAxes();
 	
 		PGraph3D.Vector3D normalUnnorminated = new PGraph3D.Vector3D(4,4,4);
 		PGraph3D.Float length = new PGraph3D.Float(0);
 		try {
 			length.x = normalUnnorminated.abs().get();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		PGraph3D.DynVector3D normal = normalUnnorminated.norminated();
 
 
 		PGraph3D.MoveablePointOnLine stuetzPt = graph.new MoveablePointOnLine(new PGraph3D.Vector3D(4,4,4), Color.black);
 		final PGraph3D.MoveablePointOnPlane normalPt = graph.new MoveablePointOnPlane(new PGraph3D.Vector3D(), Color.blue);
		PGraph3D.Vector3DUpdater normalUpdater = new PGraph3D.Vector3DUpdater((Vector3D) normalPt.point, stuetzPt.dynPoint().sum(normalUnnorminated));
 		stuetzPt.updater.add( normalUpdater );
 		normalPt.updater.add( new PGraph3D.Vector3DUpdater(normalUnnorminated, normalPt.dynPoint().diff(stuetzPt.dynPoint()).norminated().product(length) ) );
 		normalPt.updater.add( normalUpdater );
 
 		PGraph3D.MoveablePointOnPlane richtungPt1 = graph.new MoveablePointOnPlane(stuetzPt.dynPoint().sum( normalUnnorminated.someOrthogonal().norminated().product(length) ).fixed(), Color.green);
		DynVector3D richtung1 = richtungPt1.dynPoint().diff(stuetzPt.dynPoint());
 		PGraph3D.MoveablePointOnPlane richtungPt2 = graph.new MoveablePointOnPlane(new PGraph3D.Vector3D(), Color.green);
		DynVector3D richtung2 = richtungPt2.dynPoint().diff(stuetzPt.dynPoint());
 
		PGraph3D.Vector3DUpdater richtungPt1Updater = new PGraph3D.Vector3DUpdater((Vector3D) richtungPt1.point, stuetzPt.dynPoint().sum( richtung2.crossProduct(normalUnnorminated).norminated().product(length) ), false);
		PGraph3D.Vector3DUpdater richtungPt2Updater = new PGraph3D.Vector3DUpdater((Vector3D) richtungPt2.point, stuetzPt.dynPoint().sum( normalUnnorminated.crossProduct(richtung1).norminated().product(length) ));
 		normalPt.updater.add(richtungPt1Updater);
 		normalPt.updater.add(richtungPt2Updater);
 		stuetzPt.updater.add(richtungPt1Updater);
 		stuetzPt.updater.add(richtungPt2Updater);
 
 		graph.objects.add(stuetzPt);
 		graph.objects.add(normalPt);
 		graph.objects.add(new PGraph3D.VectorArrow(stuetzPt.dynPoint(), normalPt.dynPoint().diff(stuetzPt.dynPoint()), Color.blue));
 				
 		PGraph3D.Plane plane = new PGraph3D.Plane( stuetzPt.dynPoint().dotProduct(normal), normal, Color.red );
 		graph.objects.add(plane);
 		
 		normalPt.plane = new PGraph3D.Plane( normalPt.dynPoint().dotProduct(normal), normal );
 		stuetzPt.line = new PGraph3D.Line(stuetzPt.dynPoint(), normal);
 		
 		graph.objects.add(richtungPt1);
 		graph.objects.add(richtungPt2);
 		graph.objects.add(new PGraph3D.VectorArrow(stuetzPt.dynPoint(), richtung1, Color.green));
 		graph.objects.add(new PGraph3D.VectorArrow(stuetzPt.dynPoint(), richtung2, Color.green));
 
 		richtungPt1.plane = new PGraph3D.Plane(richtungPt1.dynPoint(), richtung2, plane.normal);
 		richtungPt2.plane = new PGraph3D.Plane(richtungPt2.dynPoint(), plane.normal, richtung1);
 		richtungPt1.updater.add( new PGraph3D.Vector3DUpdater(normalUnnorminated, richtung1.crossProduct(richtung2).norminated().product(length)) );
 		richtungPt1.updater.add( normalUpdater );
 		richtungPt1.updater.add( richtungPt1Updater );
 		richtungPt1.updater.add( richtungPt2Updater );
 		richtungPt2.updater.add( new PGraph3D.Vector3DUpdater(normalUnnorminated, richtung1.crossProduct(richtung2).norminated().product(length)) );
 		richtungPt2.updater.add( normalUpdater );
 		richtungPt2.updater.add( richtungPt1Updater );
 		richtungPt2.updater.add( richtungPt2Updater );
 		
 		// for better imagination
 		graph.objects.add(plane.intersectionLine(PGraph3D.Plane.zPlane).setColor(Color.darkGray));
 				
 		applet.vtmeta.setExtern(new VisualThing[] {
 				new VTImage("graph", 10, 5, graph.W, graph.H, graph)
 		});	
 	}
 	
 }
