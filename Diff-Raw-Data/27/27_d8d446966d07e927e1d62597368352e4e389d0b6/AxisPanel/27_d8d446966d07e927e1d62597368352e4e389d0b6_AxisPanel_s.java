 package edu.msoe.se2800.h4.jplot;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 public class AxisPanel extends JPanel {
 
     /**
      * Generated serialVersionUID
      */
     private static final long serialVersionUID = 672661897751955073L;
 
     private int ORIENTATION = Constants.HORIZONTAL; //default orientation is set to horizontal (x axis)
 
     public AxisPanel(int orientation) {
         if (orientation == Constants.HORIZONTAL) {
             ORIENTATION = Constants.HORIZONTAL;
             initHorizontal();
         } else if (orientation == Constants.VERTICAL) {
             ORIENTATION = Constants.VERTICAL;
             initVertical();
         } else {
             throw new IllegalArgumentException("You cannot initialize an Axis Panel without a valid VERTICAL or HORIZONTAL constant.");
         }
         setVisible(true);
     }
 
     private void initHorizontal() {
         setPreferredSize(new Dimension(Constants.GRID_WIDTH(), Constants.X_AXIS_HEIGHT));
     }
 
     private void initVertical() {
         setPreferredSize(new Dimension(Constants.Y_AXIS_WIDTH, Constants.GRID_HEIGHT));
     }
 
     /**
     * Draws the x axis numbers
      */
     public void drawAxisMarkersHorizontal(int density, Graphics g) {
         g.setColor(Color.BLACK);
         int newWidth = Constants.GRID_WIDTH() + (Constants.GRID_WIDTH() / density);
         int counter = 0;
         for (int i = getHeight() + Constants.GRID_OFFSET; i < newWidth + getHeight(); i += (Constants.GRID_WIDTH() / density)) {
             g.drawString("" + counter, i - (("" + counter).length() * 4), 30);
             counter += Constants.STEP_INCREMENT;
         }
     }
 
     /**
     * Draws the y axis numbers
      */
     public void drawAxisMarkersVertical(int density, Graphics g) {
         g.setColor(Color.BLACK);
         int newHeight = Constants.GRID_HEIGHT + (Constants.GRID_HEIGHT / density);
         int counter = 0;
         for (int i = Constants.GRID_HEIGHT - Constants.GRID_OFFSET; i >= (Constants.GRID_HEIGHT - newHeight); i -= (Constants.GRID_HEIGHT / density)) {
             g.drawString("" + counter, getWidth() - 30, i + 4);
             counter += Constants.STEP_INCREMENT;
         }
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (ORIENTATION == Constants.HORIZONTAL) {
             drawAxisMarkersHorizontal(JPlotController.getInstance().getGridDensity(), g);
         } else if (ORIENTATION == Constants.VERTICAL) {
             drawAxisMarkersVertical(JPlotController.getInstance().getGridDensity(), g);
         }
     }
 
 }
