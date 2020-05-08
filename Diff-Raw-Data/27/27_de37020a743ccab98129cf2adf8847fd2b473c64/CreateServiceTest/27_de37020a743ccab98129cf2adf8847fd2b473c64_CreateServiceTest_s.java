 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.tests.cases;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.graphiti.mm.algorithms.Ellipse;
 import org.eclipse.graphiti.mm.algorithms.Image;
 import org.eclipse.graphiti.mm.algorithms.MultiText;
 import org.eclipse.graphiti.mm.algorithms.PlatformGraphicsAlgorithm;
 import org.eclipse.graphiti.mm.algorithms.Polygon;
 import org.eclipse.graphiti.mm.algorithms.Polyline;
 import org.eclipse.graphiti.mm.algorithms.Rectangle;
 import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
 import org.eclipse.graphiti.mm.algorithms.Text;
 import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.Point;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.graphiti.services.Graphiti;
 import org.eclipse.graphiti.services.ICreateService;
 import org.eclipse.graphiti.tests.GFAbstractCreateTestCase;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class CreateServiceTest extends GFAbstractCreateTestCase {
 
 	public static ICreateService crs = Graphiti.getCreateService();
 	private Diagram d;
 	private Shape s1;
 	private static final String FONTNAME = "Baskerville Old Face";
 
 	@BeforeClass
 	public static void prepareClass() {
 	}
 
 	@Before
 	public void initializeTest() {
 		d = Graphiti.getPeCreateService().createDiagram("tutorial", "test", true);
 		assertNotNull(d);
 		s1 = Graphiti.getPeCreateService().createShape(d, true);
 		s1.setGraphicsAlgorithm(Graphiti.getGaCreateService().createRectangle(d));
 		assertNotNull(s1);
 		ResourceImpl resource = new ResourceImpl();
 		resource.getContents().add(d);
 	}
 
 	@Test
 	public void createDefaultMultiText() {
 		MultiText multiText = crs.createDefaultMultiText(s1);
 		assertNotNull(multiText);
 		assertEquals(DEFAULT_FONT, multiText.getFont().getName());
 		checkTextDefaults(multiText, "");
 	}
 
 	@Test
 	public void createDefaultMultiText2() {
 		MultiText multiText = crs.createDefaultMultiText(s1, VALUE);
 		assertNotNull(multiText);
 		assertEquals(DEFAULT_FONT, multiText.getFont().getName());
 		checkTextDefaults(multiText, VALUE);
 	}
 
 	@Test
 	public void createDefaultText() {
 		Text text = crs.createDefaultText(s1);
 		assertNotNull(text);
 		assertEquals(DEFAULT_FONT, text.getFont().getName());
 		checkTextDefaults(text, "");
 	}
 
 	@Test
 	public void createDefaultText2() {
 		Text text = crs.createDefaultText(s1, VALUE);
 		assertNotNull(text);
 		assertEquals(DEFAULT_FONT, text.getFont().getName());
 		checkTextDefaults(text, VALUE);
 	}
 
 	@Test
 	public void createEllipse() {
 		Ellipse ellipse = crs.createEllipse(s1);
 		assertNotNull(ellipse);
 		checkGraphicsAlgorithmDefaults(ellipse);
 	}
 
 	@Test
 	public void createFont() {
 		Text text = crs.createDefaultText(s1);
 		Font font = crs.createFont(text, FONTNAME, 10);
 		assertNotNull(font);
 		assertEquals(10, font.getSize());
 		assertEquals(FONTNAME, font.getName());
 	}
 
 	@Test
 	public void createFont2() {
 		Text text = crs.createDefaultText(s1);
 		Font font = crs.createFont(text, FONTNAME, 10, true, true);
 		assertNotNull(font);
 		assertEquals(10, font.getSize());
 		assertEquals(FONTNAME, font.getName());
 		assertTrue(font.isItalic());
 		assertTrue(font.isBold());
 		font = crs.createFont(text, FONTNAME, 10, true, false);
 		assertTrue(font.isItalic());
 		assertFalse(font.isBold());
 		font = crs.createFont(text, FONTNAME, 10, false, true);
 		assertFalse(font.isItalic());
 		assertTrue(font.isBold());
 		font = crs.createFont(text, FONTNAME, 10, false, false);
 		assertFalse(font.isItalic());
 		assertFalse(font.isBold());
 	}
 
 	@Test
 	public void createFont3() {
 		Style style = crs.createStyle(d, VALUE);
 		Font font = crs.createFont(style, FONTNAME, 10);
 		assertNotNull(font);
 		assertEquals(10, font.getSize());
 		assertEquals(FONTNAME, font.getName());
 		assertFalse(font.isItalic());
 		assertFalse(font.isBold());
 		assertEquals(font, style.getFont());
 
 	}
 
 	@Test
 	public void createFont4() {
 		Style style = crs.createStyle(d, VALUE);
 		Font font = crs.createFont(style, FONTNAME, 10, true, true);
 		assertNotNull(font);
 		assertEquals(10, font.getSize());
 		assertEquals(FONTNAME, font.getName());
 		assertTrue(font.isItalic());
 		assertTrue(font.isBold());
 		assertEquals(font, style.getFont());
 		assertTrue(font.isBold());
 		assertTrue(font.isItalic());
 		font = crs.createFont(style, FONTNAME, 10, true, false);
 		assertTrue(font.isItalic());
 		assertFalse(font.isBold());
 
 	}
 
 	@Test
 	public void createImage() {
 		Image im = crs.createImage(d, VALUE);
 		checkGraphicsAlgorithmDefaults(im);
 		checkImageDefaults(im);
 	}
 
 	@Test
 	public void createInvisibleRectangle() {
 		Rectangle invisibleRect = crs.createInvisibleRectangle(s1);
 		assertFalse(invisibleRect.getLineVisible());
 	}
 
 	@Test
 	public void createMultiText() {
 		MultiText multiText = crs.createMultiText(s1);
 		assertNotNull(multiText);
 		assertNull(multiText.getFont());
 		checkTextDefaults(multiText, "");
 	}
 
 	@Test
 	public void createMultiText2() {
 		MultiText multiText = crs.createMultiText(s1, VALUE);
 		assertNotNull(multiText);
 		assertNull(multiText.getFont());
 		checkTextDefaults(multiText, VALUE);
 	}
 
 	@Test
 	public void createPlatformGa() {
 		PlatformGraphicsAlgorithm pGa = crs.createPlatformGraphicsAlgorithm(s1, VALUE);
 		assertEquals(s1, pGa.eContainer());
 		assertEquals(VALUE, pGa.getId());
 	}
 
 	@Test
 	public void createPoint() {
 		Point point = crs.createPoint(0, 1);
 		assertEquals(0, point.getX());
 		assertEquals(1, point.getY());
 		point = crs.createPoint(1, 0, 2, 3);
 		assertEquals(1, point.getX());
 		assertEquals(0, point.getY());
 		assertEquals(2, point.getBefore());
 		assertEquals(3, point.getAfter());
 	}
 
 	@Test
 	public void createPointList() {
 		List<Point> points = crs.createPointList(new int[] { 1, 2, 3, 4 });
 		Point p1 = points.get(0);
 		assertEquals(1, p1.getX());
 		assertEquals(2, p1.getY());
 		Point p2 = points.get(1);
 		assertEquals(3, p2.getX());
 		assertEquals(4, p2.getY());
 	}
 
 	@Test
 	public void createPointList2() {
 		List<Point> points = crs.createPointList(new int[] { 1, 2, 3, 4 }, new int[] { 5, 6, 7, 8 });
 		Point p1 = points.get(0);
 		assertEquals(1, p1.getX());
 		assertEquals(2, p1.getY());
 		assertEquals(5, p1.getBefore());
 		assertEquals(6, p1.getAfter());
 		Point p2 = points.get(1);
 		assertEquals(3, p2.getX());
 		assertEquals(4, p2.getY());
 		assertEquals(7, p2.getBefore());
 		assertEquals(8, p2.getAfter());
 	}
 
 	@Test
 	public void createPolygon() {
 		Polygon polygon = crs.createPolygon(s1);
 		assertEquals(polygon, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(polygon);
 	}
 
 	@Test
 	public void createPolygon2() {
 		List<Point> points = crs.createPointList(new int[] { 1, 2, 3, 4 });
 		Polygon polygon = crs.createPolygon(s1, points);
 		assertEquals(polygon, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(polygon);
 		EList<Point> points2 = polygon.getPoints();
 		assertEquals(points, points2);
 	}
 
 	@Test
 	public void createPolygon3() {
 		int[] points = new int[] { 1, 2, 3, 4 };
 		Polygon polygon = crs.createPolygon(s1, points);
 		assertEquals(polygon, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(polygon);
 		EList<Point> points2 = polygon.getPoints();
 		assertEquals(1, points2.get(0).getX());
 		assertEquals(2, points2.get(0).getY());
 		assertEquals(3, points2.get(1).getX());
 		assertEquals(4, points2.get(1).getY());
 		int[] beforeAfter = new int[] { 5, 6, 7, 8 };
 		polygon = crs.createPolygon(s1, points, beforeAfter);
 		checkGraphicsAlgorithmDefaults(polygon);
 		points2 = polygon.getPoints();
 		assertEquals(1, points2.get(0).getX());
 		assertEquals(2, points2.get(0).getY());
 		assertEquals(3, points2.get(1).getX());
 		assertEquals(4, points2.get(1).getY());
 		assertEquals(5, points2.get(0).getBefore());
 		assertEquals(6, points2.get(0).getAfter());
 		assertEquals(7, points2.get(1).getBefore());
 		assertEquals(8, points2.get(1).getAfter());
 	}
 
 	@Test
 	public void createPolyline() {
 		Polyline polyline = crs.createPolyline(s1);
 		assertEquals(polyline, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(polyline);
 	}
 
 	@Test
 	public void createPolyline2() {
 		List<Point> points = crs.createPointList(new int[] { 1, 2, 3, 4 });
 		Polyline polyline = crs.createPolyline(s1, points);
 		assertEquals(polyline, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(polyline);
 		EList<Point> points2 = polyline.getPoints();
 		assertEquals(points, points2);
 	}
 
 	@Test
 	public void createPolyline3() {
 		int[] points = new int[] { 1, 2, 3, 4 };
 		Polyline polyline = crs.createPolyline(s1, points);
 		assertEquals(polyline, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(polyline);
 		EList<Point> points2 = polyline.getPoints();
 		assertEquals(1, points2.get(0).getX());
 		assertEquals(2, points2.get(0).getY());
 		assertEquals(3, points2.get(1).getX());
 		assertEquals(4, points2.get(1).getY());
 		int[] beforeAfter = new int[] { 5, 6, 7, 8 };
 		polyline = crs.createPolyline(s1, points, beforeAfter);
 		checkGraphicsAlgorithmDefaults(polyline);
 		points2 = polyline.getPoints();
 		assertEquals(1, points2.get(0).getX());
 		assertEquals(2, points2.get(0).getY());
 		assertEquals(3, points2.get(1).getX());
 		assertEquals(4, points2.get(1).getY());
 		assertEquals(5, points2.get(0).getBefore());
 		assertEquals(6, points2.get(0).getAfter());
 		assertEquals(7, points2.get(1).getBefore());
 		assertEquals(8, points2.get(1).getAfter());
 	}
 
 	@Test
 	public void createRectangle() {
 		Rectangle rect = crs.createRectangle(s1);
 		assertEquals(rect, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(rect);
 	}
 
 	@Test
 	public void createRoundedRectangle() {
 		RoundedRectangle rect = crs.createRoundedRectangle(s1, 5, 6);
 		assertEquals(rect, s1.getGraphicsAlgorithm());
 		checkGraphicsAlgorithmDefaults(rect);
 		assertEquals(5, rect.getCornerWidth());
 		assertEquals(6, rect.getCornerHeight());
 	}
 
 	@Test
 	public void createStyle() {
 		Style style = crs.createStyle(d, VALUE);
 		assertEquals(VALUE, style.getId());
 		assertEquals(1, d.getStyles().size());
 	}
 }
