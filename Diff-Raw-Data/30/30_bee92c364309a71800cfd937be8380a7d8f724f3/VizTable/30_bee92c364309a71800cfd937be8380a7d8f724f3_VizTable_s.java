 package com.anotherbrick.inthewall;
 
 import static com.anotherbrick.inthewall.Helper.*;
 
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 
 public class VizTable extends VizPanel {
 
   private static final float MAX_ROWS = 0;
   private static final int TABLE_HEIGHT = 0;
   private static final float TABLE_WIDTH = 0;
   private static final int ROW_HEIGHT = 0;
   private static final int TABLE_PADDING_TOP = 0;
   private static final float NAMES_HEIGHT = 0;
   private static final float FIRST_PLOT_X = 0;
   private static final float SECOND_PLOT_X = 0;
   private static final float THIRD_PLOT_X = 0;
   private static final float FOURTH_PLOT_X = 0;
   private static final float YEAR_X = 0;
   private MyColorEnum oddRowsColor, evenRowsColor;
   private String title;
   private ArrayList<String> colNames;
   private ArrayList<Plot> plots;
   private float xStart, xStop;
   public MyColorEnum[] palette = { MyColorEnum.GRAPH_COLOR_1, MyColorEnum.GRAPH_COLOR_2,
       MyColorEnum.GRAPH_COLOR_3, MyColorEnum.GRAPH_COLOR_4 };
 
   public float getxStart() {
     return xStart;
   }
 
   public float getxStop() {
     return xStop;
   }
 
   public void setxStop(float xStop) {
     this.xStop = xStop;
   }
 
   public void setXStart(float xStart) {
     this.xStart = xStart;
   }
 
   public VizTable(float x0, float y0, float width, float height, VizPanel parent) {
     super(x0, y0, width, height, parent);
   }
 
   public void setup(MyColorEnum oddRowsColor, MyColorEnum evenRowsColor) {
     this.oddRowsColor = oddRowsColor;
     this.evenRowsColor = evenRowsColor;
     plots = new ArrayList<Plot>();
 
     if (plots.isEmpty()) {
       plots.ensureCapacity(4);
       while (plots.size() < 4) {
         plots.add(null);
       }
     }
     colNames = new ArrayList<String>();
     initColNames();
 
     xStart = 0;
     xStop = MAX_ROWS;
   }
 
   public void addPlot(Plot plot, int index) {
     plots.set(index, plot);
 
     xStop = getOverallXMax(plots);
     xStart = getOverallXMin(plots);
   }
 
   public void removePlot(Plot plot) {
     if (plots.contains(plot)) {
       plots.remove(plot);
     }
   }
 
   public void removePlotAtIndex(int index) {
     plots.set(index, null);
   }
 
   private void initColNames() {
     colNames.add(c.translate("Year"));
     colNames.add("Plot No. 1");
     colNames.add("Plot No. 2");
     colNames.add("Plot No. 3");
     colNames.add("Plot No. 4");
   }
 
   public boolean draw() {
     pushStyle();
     background(MyColorEnum.MEDIUM_GRAY);
 
     drawColNames();
     MyColorEnum color;
 
     fill(MyColorEnum.DARK_GRAY);
     noStroke();
     rect(TABLE_WIDTH, 0, 20, TABLE_HEIGHT + 5);
     rect(0, 0, -100, TABLE_HEIGHT + 5);
     fill(MyColorEnum.WHITE);
     for (int i = 0, j = (int) xStart; i < MAX_ROWS && j <= xStop; i++, j++) {
       VizTableRow row = new VizTableRow(0, TABLE_PADDING_TOP + ROW_HEIGHT * i, TABLE_WIDTH,
          ROW_HEIGHT, x0, y0);
       for (Plot p : plots) {
         if (p != null) {
           PVector vector = new PVector(p.getPoints().get((j - (int) p.getXMin())).x, p.getPoints()
               .get((j - (int) p.getXMin())).y);
           row.addValue(vector, plots.indexOf(p));
         }
       }
       if (i % 2 == 0) {
         color = evenRowsColor;
       } else {
         color = oddRowsColor;
       }
 
       drawRow(row, color);
     }
 
     popStyle();
 
     return false;
   }
 
   private void drawColNames() {
     pushStyle();
     textSize(14);
     text(colNames.get(0), YEAR_X, NAMES_HEIGHT);
     textAlign(PApplet.RIGHT, PApplet.BASELINE);
     if (colNames.size() >= 2) {
       fill(palette[0]);
       text(colNames.get(1), FIRST_PLOT_X, NAMES_HEIGHT);
     }
     if (colNames.size() >= 3) {
       fill(palette[1]);
       text(colNames.get(2), SECOND_PLOT_X, NAMES_HEIGHT);
     }
     if (colNames.size() >= 4) {
       fill(palette[2]);
       text(colNames.get(3), THIRD_PLOT_X, NAMES_HEIGHT);
     }
     if (colNames.size() == 5) {
       fill(palette[3]);
       text(colNames.get(4), FOURTH_PLOT_X, NAMES_HEIGHT);
     }
     popStyle();
 
   }
 
   private void drawRow(VizTableRow row, MyColorEnum color) {
     pushStyle();
     row.backgroundColor = color;
     row.strokeColor = MyColorEnum.WHITE;
     row.draw();
     popStyle();
   }
 
   public String getTitle() {
     return title;
   }
 
   public void setTitle(String title) {
     this.title = title;
   }
 
   public ArrayList<String> getColNames() {
     return colNames;
   }
 
   public void setColNames(ArrayList<String> colNames) {
     this.colNames = colNames;
   }
 
 }
