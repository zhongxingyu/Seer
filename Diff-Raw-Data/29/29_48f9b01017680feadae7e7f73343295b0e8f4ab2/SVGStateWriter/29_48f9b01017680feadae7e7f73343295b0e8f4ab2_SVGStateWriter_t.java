 package at.stefl.svm.tosvg;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import at.stefl.commons.lwxml.writer.LWXMLStreamWriter;
 import at.stefl.commons.lwxml.writer.LWXMLWriter;
 import at.stefl.commons.math.RectangleD;
 import at.stefl.commons.math.vector.Vector2d;
 
 // TODO: improve style handling
 public class SVGStateWriter {
 
     private LWXMLWriter out;
 
     private Map<String, String> currentStyle = new LinkedHashMap<String, String>();
 
     private boolean started;
     private boolean ended;
     private boolean openElement;
     private boolean attributeWriteable;
 
     public SVGStateWriter(Writer out) {
 	this.out = new LWXMLStreamWriter(out);
     }
 
     public SVGStateWriter(LWXMLWriter out) {
 	this.out = out;
     }
 
     public void writeHeader() throws IOException {
 	out.writeStartElement("svg");
 	out.writeAttribute("xmlns", "http://www.w3.org/2000/svg");
 	out.writeAttribute("version", "1.1");
 
 	started = true;
 	attributeWriteable = true;
     }
 
     public void writeFooter() throws IOException {
 	preWrite();
 
 	out.writeEndElement("svg");
 
 	ended = true;
 	attributeWriteable = false;
     }
 
     public void writeAttribute(String name, String value) throws IOException {
 	if (!attributeWriteable)
 	    throw new IllegalStateException(
 		    "cannot write attribute in current state");
 
 	out.writeAttribute(name, value);
     }
 
     public Map<String, String> getCurrentStyle() {
 	return currentStyle;
     }
 
     public String getCurrentStyle(String property) {
 	return currentStyle.get(property);
     }
 
     public boolean hasCurrentStyle(String property) {
 	return currentStyle.containsKey(property);
     }
 
     public void setCurrentStyle(Map<String, String> currentStyle) {
 	this.currentStyle = currentStyle;
     }
 
     public void addCurrentStyle(String property, String value) {
 	currentStyle.put(property, value);
     }
 
     public void removeCurrentStyle(String property) {
 	currentStyle.remove(property);
     }
 
     public void clearCurrentStyle() {
 	currentStyle.clear();
     }
 
     private void writeCurrentStyle() throws IOException {
 	if (currentStyle.isEmpty())
 	    return;
 
 	out.writeAttribute("style", "");
 
 	for (Map.Entry<String, String> entry : currentStyle.entrySet()) {
 	    out.write(entry.getKey());
 	    out.write(":");
 	    out.write(entry.getValue());
 	    out.write(";");
 	}
     }
 
     private void closeOpenElement() throws IOException {
 	out.writeEndEmptyElement();
 
 	openElement = false;
 	attributeWriteable = false;
     }
 
     private void preWrite() throws IOException {
 	if (!started)
 	    throw new IllegalStateException("header was not written since now");
 	if (ended)
 	    throw new IllegalStateException("footer already writen");
 	if (openElement)
 	    closeOpenElement();
     }
 
     private void postWrite() throws IOException {
 	writeCurrentStyle();
 
 	openElement = true;
 	attributeWriteable = true;
     }
 
     private void writeVector2iAttributes(Vector2d vector, String xAttribute,
 	    String yAttribute) throws IOException {
 	out.writeAttribute(xAttribute, "" + vector.getX());
 	out.writeAttribute(yAttribute, "" + vector.getY());
     }
 
     private void writePointAttributes(Vector2d point) throws IOException {
 	writeVector2iAttributes(point, "x", "y");
     }
 
     private void writePoint1Attributes(Vector2d point) throws IOException {
 	writeVector2iAttributes(point, "x1", "y1");
     }
 
     private void writePoint2Attributes(Vector2d point) throws IOException {
 	writeVector2iAttributes(point, "x2", "y2");
     }
 
     private void writePointsAttributes(Collection<Vector2d> points)
 	    throws IOException {
 	out.writeAttribute("points", "");
 
 	for (Vector2d point : points) {
 	    out.write("" + point.getX());
 	    out.write(",");
 	    out.write("" + point.getY());
	    // TODO: remove on last
 	    out.write(" ");
 	}
     }
 
     private void writeCenterAttributes(Vector2d center) throws IOException {
 	writeVector2iAttributes(center, "cx", "cy");
     }
 
     private void writeRadiusAttribute(double radius) throws IOException {
 	writeRadiusAttributes(new Vector2d(radius));
     }
 
     private void writeRadiusAttributes(Vector2d radius) throws IOException {
 	writeVector2iAttributes(radius, "rx", "ry");
     }
 
     private void writeRectangleAttributes(RectangleD rectangle)
 	    throws IOException {
 	writePointAttributes(rectangle.getLeftTop());
 	out.writeAttribute("width", "" + rectangle.getWidth());
 	out.writeAttribute("height", "" + rectangle.getHeight());
     }
 
     public void writeLine(Vector2d point1, Vector2d point2) throws IOException {
 	preWrite();
 
 	out.writeStartElement("line");
 	writePoint1Attributes(point1);
 	writePoint2Attributes(point2);
 
 	postWrite();
     }
 
     public void writeRectange(RectangleD rectangle) throws IOException {
 	preWrite();
 
 	out.writeStartElement("rect");
 	writeRectangleAttributes(rectangle);
 
 	postWrite();
     }
 
     public void writeRectange(RectangleD rectangle, Vector2d radius)
 	    throws IOException {
 	preWrite();
 
 	out.writeStartElement("rect");
 	writeRectangleAttributes(rectangle);
 	writeRadiusAttributes(radius);
 
 	postWrite();
     }
 
     public void writeCircle(Vector2d center, double radius) throws IOException {
 	preWrite();
 
 	out.writeStartElement("ellipse");
 	writeCenterAttributes(center);
 	writeRadiusAttribute(radius);
 
 	postWrite();
     }
 
     public void writeEllipse(Vector2d center, Vector2d radius)
 	    throws IOException {
 	preWrite();
 
 	out.writeStartElement("ellipse");
 	writeCenterAttributes(center);
 
 	postWrite();
     }
 
     public void writePolyLine(Collection<Vector2d> points) throws IOException {
 	preWrite();
 
 	out.writeStartElement("polyline");
 	writePointsAttributes(points);
 
 	postWrite();
     }
 
     public void writePolygon(Collection<Vector2d> points) throws IOException {
 	preWrite();
 
 	out.writeStartElement("polygon");
 	writePointsAttributes(points);
 
 	postWrite();
     }
 
     public void writeText(Vector2d point, String text) throws IOException {
 	preWrite();
 
 	out.writeStartElement("text");
 	writePointAttributes(point);
 
 	postWrite();
 
 	out.writeCharacters(text);
 
 	out.writeEndElement("text");
 	openElement = false;
 	attributeWriteable = false;
     }
 
    public void flush() throws IOException {
	out.flush();
    }

     public void close() throws IOException {
	flush();
 	out.close();
     }
 
 }
