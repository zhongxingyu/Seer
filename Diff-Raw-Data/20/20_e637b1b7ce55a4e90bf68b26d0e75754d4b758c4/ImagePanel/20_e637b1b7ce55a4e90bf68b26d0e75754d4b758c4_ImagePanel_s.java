 package newjavafx;
 
 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 
 /**
 *
 * @author 
 */
 import java.awt.Color;
 import javafx.scene.Group;
 import javafx.scene.Node;
 import javafx.scene.layout.HBox;
 import javafx.scene.paint.Paint;
 import javafx.scene.shape.Arc;
 import javafx.scene.shape.Ellipse;
 import javafx.scene.shape.Line;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Text;
 public class ImagePanel extends HBox
 {
 private Group graphic=new Group();
 private HBox iView;
String colorst;
 public ImagePanel(int width, int height)
 {
 setImageSize(width, height);
 }
 private void setImageSize(int width, int height)
 { 
 this.setMaxSize(width, height);
 this.getChildren().add(graphic);
 clear(Color.white);
 }
 public void setBackgroundColour(Color colour){
 this.setStyle("-fx-fill:"+colour.toString()+";");
 }
 public void clear(Color colour)
 {
 setBackgroundColour(colour);
 }
 public void setColour(Color colour)
 {
 Integer blue=colour.getBlue();
 Integer green=colour.getGreen();
 Integer red=colour.getRed();
 String hex = String.format("#%02x%02x%02x", red, green, blue);
 colorst=hex;
 
 }
 public void drawLine(int x1, int y1, int x2, int y2)
 {
 Line line = new Line();
 line.setStartX(x1);
 line.setStartY(y1);
 line.setEndX(x2);
 line.setEndY(y2);
 line.setStroke(Paint.valueOf(colorst));
 graphic.getChildren().add(line);
 
 }
 public void drawRect(int x1, int y1, int x2, int y2)
 {
 Rectangle rect = new Rectangle(x1,y1,x2,y2);
 rect.setStroke(Paint.valueOf("000000"));
 rect.setFill(Paint.valueOf("00000000"));
 graphic.getChildren().add(rect);
 }
 public void fillRect(int x1, int y1, double x2, double y2)
 {
 Rectangle rectFill = new Rectangle(x1,y1,x2,y2);
 rectFill.setFill(Paint.valueOf(colorst));
 graphic.getChildren().add(rectFill);
 }
 public void drawString(int x, int y, String s)
 {
 Text t = new Text(x,y,s);
 graphic.getChildren().add(t);
 }
 
 public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle)
 {
 Arc arc = new Arc(x,y,width/2,height/2,startAngle,arcAngle);
arc.setStroke(Paint.valueOf("000000"));
 arc.setFill(Paint.valueOf("00000000"));
 graphic.getChildren().add(arc);
 }
 
 public void drawOval(int x, int y, int width, int height)
 {
Node oval = new Ellipse(x,y,width,height);
 graphic.getChildren().add(oval);
 }
 }
