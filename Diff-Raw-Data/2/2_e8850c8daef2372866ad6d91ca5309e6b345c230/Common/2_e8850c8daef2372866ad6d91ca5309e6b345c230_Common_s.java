 package edu.agh.tunev.model;
 
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 public final class Common {
 
 	/**
 	 * Creates an ellipse with given center point and rotation angle.
 	 * 
 	 * @param center
 	 *            coordinates of the shape's center.
 	 * @param a
 	 *            original (pre-rotation) OX dimension.
 	 * @param b
 	 *            original (pre-rotation) OY dimension.
 	 * @param deg
 	 *            counter-clockwise rotation around the {@code center}.
 	 * 
 	 * @return Shape representing the ellipse.
 	 */
 	public static Shape createEllipse(Point2D.Double center, double a, double b,
 			double deg) {
		return AffineTransform.getRotateInstance(-deg * Math.PI / 180.0, center.x, center.y)
 				.createTransformedShape(
 						new Ellipse2D.Double(center.x - a / 2, center.y - b / 2, a, b));
 	}
 
 	/**
 	 * Zwraca wspólne pole dwóch Shape.
 	 */
 	public static double intersectionArea(Shape s1, Shape s2) {
 		// poniższe liczby definiują wymiary siatki na którą zostanie
 		// przeskalowana (rozciągnięta) część wspólna kształtów s1, s2
 		//
 		// oczywiście im większa siatka, tym dłużej zajmie policzenie tego
 		//
 		// złożoność tej funkcji to O(w*h)
 		final int w = 100;
 		final int h = 100;
 		// dla siatki 5000x5000 i dwóch identycznych elips 200x100:
 		//
 		// Shape e1 = ellipse(250, 250, 200, 100, 0);
 		// Shape e2 = ellipse(250, 250, 200, 100, 0);
 		//
 		// zwrócony wynik to:
 		// 15711.904
 		//
 		// a analityczny wynik:
 		// 200/2*100/2*Math.PI == 15707.9632679489661923
 		//
 		// procentowa różnica: -0.0251%
 		//
 		// oczywiście 5000x5000 to zabójcza wielkość, liczy się jakieś 2 sekundy
 		// u mnie, nie wspominając o zajętej pamięci
 		//
 		// dla porównania: siatka 100x100 daje błąd 0.66%, więc wciąż spoko
 		//
 		// 10x10 -> 10.87%
 
 		final Area area = new Area(s1);
 		area.intersect(new Area(s2));
 		Rectangle2D bounds = area.getBounds2D();
 
 		final double dx = bounds.getWidth() / w;
 		final double dy = bounds.getHeight() / h;
 		final double tx = -bounds.getMinX();
 		final double ty = -bounds.getMinY();
 
 		final AffineTransform at = AffineTransform.getScaleInstance(1.0 / dx,
 				1.0 / dy);
 		at.concatenate(AffineTransform.getTranslateInstance(tx, ty));
 
 		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
 				w, h, java.awt.image.BufferedImage.TYPE_INT_RGB);
 
 		java.awt.Graphics2D g = image.createGraphics();
 		g.setPaint(java.awt.Color.WHITE);
 		g.fillRect(0, 0, w, h);
 		g.setPaint(java.awt.Color.BLACK);
 		g.fill(at.createTransformedShape(area));
 
 		int num = 0;
 
 		for (int i = 0; i < w; i++)
 			for (int j = 0; j < h; j++)
 				if ((image.getRGB(i, j) & 0x00ffffff) == 0)
 					num++;
 
 		return dx * dy * num;
 	}
 
 	private Common() {
 		// you shall not instantiate ^-^
 	}
 
 }
