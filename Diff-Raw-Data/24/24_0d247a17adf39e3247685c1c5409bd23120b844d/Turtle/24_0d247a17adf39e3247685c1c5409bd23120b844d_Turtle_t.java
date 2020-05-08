 package simulation;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.List;
 import util.Location;
 import util.Pixmap;
 import util.Sprite;
 import util.ValueText;
 import util.Vector;
 import view.SLogoView;
 
 
 public class Turtle extends Sprite {
    public static final Pixmap DEFAULT_IMAGE = new Pixmap("turtle_art.png");
    public static final Pixmap NO_IMAGE = new Pixmap("blank.png");
     private static final Dimension DEFAULT_SIZE = new Dimension(70, 70);
    private static final String X_LABEL = "X-coordinate:";
    private static final String Y_LABEL = "Y-coordinate:";
    private static final String ANGLE_LABEL = "Angle:";
    private static final double X_OFFSET = 60;
    private static final double Y_OFFSET = 20;
     private LineAdder myLineAdder;
     private List<ValueText> myStatus;
     private Color myPenColor;
 
     public Turtle (LineAdder la) {
         super(DEFAULT_IMAGE, centerOfScreen(), DEFAULT_SIZE);
         myLineAdder = la;
         initTexts();
         myPenColor = Color.BLACK;
     }
 
     @Override
     public void update (double elapsedTime, Dimension bounds) {
         super.update(elapsedTime, bounds);
         updateStatus();
     }
 
     private void updateStatus () {
         Dimension bounds = SLogoView.PREFERRED_CANVAS_SIZE;
         int x = (int) (getX() - bounds.getWidth()/2); 
         int y = (int) (bounds.getHeight()/2 - getY());
         int angle = (int) getAngle();
         int[] currentStatus = {x, y, angle};
         
         for (int i = 0; i < myStatus.size(); i++) {
             ValueText vt = myStatus.get(i);
             vt.resetValue();
             vt.updateValue(currentStatus[i]);
         }
     }
 
     @Override
     public void paint (Graphics2D pen) {
         super.paint(pen);
         paintStatus(pen);
     }
 
     private void paintStatus (Graphics2D pen) {
        Location textLoc = new Location(X_OFFSET, 0);
         for (ValueText vt:myStatus) {
             textLoc.setLocation(X_OFFSET, textLoc.getY() + Y_OFFSET);
            vt.paint(pen, textLoc, Color.BLACK);
         }
     }
 
     private static Location centerOfScreen () {
         Dimension bounds = SLogoView.PREFERRED_CANVAS_SIZE;
         return new Location(bounds.getWidth() / 2, bounds.getHeight() / 2);
     }
 
     public void resetTurtle () {
         setCenter(centerOfScreen());
         setAngle(0);
     }
 
     @Override
     public void translate (Vector v) {
         Location originalLocation = new Location(getX(), getY());
         super.translate(v);
         Location newLocation = new Location(getX(), getY());
         Line line = new Line(originalLocation, newLocation, myPenColor);
         myLineAdder.addLine(line);
     }
 
     public void changePen (Color color) {
         myPenColor = color;
     }
 
     public Color getPenColor () {
         return myPenColor;
     }
 
     private void initTexts () {
         myStatus = new ArrayList<ValueText>();
        myStatus.add(new ValueText(X_LABEL, 0));
        myStatus.add(new ValueText(Y_LABEL, 0));
        myStatus.add(new ValueText(ANGLE_LABEL, 0));
     }
 
 }
