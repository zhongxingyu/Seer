 package com.gemserk.games.archervsworld;
 
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Vector2f;
 import javax.vecmath.Vector3f;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.badlogic.gdx.math.Vector2;
 import com.gemserk.commons.svg.inkscape.SvgDocument;
 import com.gemserk.commons.svg.inkscape.SvgDocumentHandler;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeGroup;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeGroupHandler;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeImage;
 import com.gemserk.commons.svg.inkscape.SvgInkscapeImageHandler;
 import com.gemserk.commons.svg.inkscape.SvgInkscapePath;
 import com.gemserk.commons.svg.inkscape.SvgInkscapePathHandler;
 import com.gemserk.commons.svg.inkscape.SvgParser;
 
 public class LayerProcessor {
 
 	private SvgDocument svgDocument;
 
 	private String layer;
 
 	public LayerProcessor(String layer) {
 		this.layer = layer;
 	}
 
 	public void processWorld(Document document) {
 		SvgParser svgParser = new SvgParser();
 		svgParser.addHandler(new SvgDocumentHandler() {
 			@Override
 			protected void handle(SvgParser svgParser, SvgDocument svgDocument, Element element) {
 				LayerProcessor.this.svgDocument = svgDocument;
 			}
 		});
 		svgParser.addHandler(new SvgInkscapeGroupHandler() {
 
 			@Override
 			protected void handle(SvgParser svgParser, SvgInkscapeGroup svgInkscapeGroup, Element element) {
 
 				if (isInkscapeLayer(svgInkscapeGroup) && !isLayer(svgInkscapeGroup)) {
 					svgParser.processChildren(false);
 					return;
 				}
 
 			}
 
 			private boolean isLayer(SvgInkscapeGroup svgInkscapeGroup) {
 				return svgInkscapeGroup.getLabel().equalsIgnoreCase(layer);
 			}
 
 			private boolean isInkscapeLayer(SvgInkscapeGroup svgInkscapeGroup) {
 				return svgInkscapeGroup.getGroupMode().equals("layer");
 			}
 
 		});
 		svgParser.addHandler(new SvgInkscapeImageHandler() {
 
 			private boolean isFlipped(Matrix3f matrix) {
 				return matrix.getM00() != matrix.getM11();
 			}
 
 			@Override
 			protected void handle(SvgParser svgParser, SvgInkscapeImage svgImage, Element element) {
 
 				if (svgImage.getLabel() == null)
 					return;
 
 				float width = svgImage.getWidth();
 				float height = svgImage.getHeight();
 
 				Matrix3f transform = svgImage.getTransform();
 
 				Vector3f position = new Vector3f(svgImage.getX() + width * 0.5f, svgImage.getY() + height * 0.5f, 0f);
 				transform.transform(position);
 
 				Vector3f direction = new Vector3f(1f, 0f, 0f);
 				transform.transform(direction);
 
 				float angle = 360f - (float) (Math.atan2(direction.y, direction.x) * 180 / Math.PI);
 
 				float sx = 1f;
 				float sy = 1f;
 
				if (isFlipped(transform)) 
 					sy = -1f;
 
 				// this stuff should be processed automatically using SVG specification with transformation of the document, etc.
 				float x = position.x;
 				float y = svgDocument.getHeight() - position.y;
 
 				handleImageObject(svgImage, element, x, y, width, height, sx, sy, angle);
 			}
 
 		});
 		svgParser.addHandler(new SvgInkscapePathHandler() {
 			@Override
 			protected void handle(SvgParser svgParser, SvgInkscapePath svgPath, Element element) {
 				Vector2f[] points = svgPath.getPoints();
 				Vector2[] vertices = new Vector2[points.length];
 
 				for (int i = 0; i < points.length; i++) {
 					Vector2f point = points[i];
 					// this coordinates transform, should be processed when parsed
 					vertices[i] = new Vector2(point.x, svgDocument.getHeight() - point.y);
 					System.out.println(vertices[i]);
 				}
 				handlePathObject(svgPath, element, vertices);
 			}
 		});
 		svgParser.parse(document);
 	}
 	
 	protected void handlePathObject(SvgInkscapePath svgPath, Element element, Vector2[] vertices) {
 		
 	}
 
 	protected void handleImageObject(SvgInkscapeImage svgImage, Element element, float x, float y, float width, float height, float sx, float sy, float angle) {
 		
 	}
 
 }
