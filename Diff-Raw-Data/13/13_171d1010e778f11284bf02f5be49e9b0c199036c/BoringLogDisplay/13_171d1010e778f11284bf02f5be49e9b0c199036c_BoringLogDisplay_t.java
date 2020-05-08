 package panels;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Polygon;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import calculations.GeologyCalculator;
 
 import panels.SitePanel.SiteType;
 
 import dataInterfaces.Boring;
 import dataInterfaces.Site;
 import dataInterfaces.Soil;
 import dataInterfaces.SoilInstance;
 import dataInterfaces.WaterTable;
 import flatFileData.SiteFF;
 import flatFileData.WaterTableFF;
 
 /**
  * This class generates a graphical representation of the boring log.
  * It draws all the text related to soils, and should be extended to drawing
  * a well in future versions.
  * @author Team Recharge
  *
  */
 public class BoringLogDisplay extends JPanel implements Serializable
 {
      private static final long serialVersionUID = 1L;
      public static final int X_MARGIN = 80; //Pixels from the horizontal border on the left and Text
      public static final int SPT_MARGIN = 10;
      public static final int WELL_BORING_LOG_DIST = 40;
      public static final int WELL_WIDTH = 10;
      public static final int Y_MARGIN = 30; //Pixels from the vertical border on the top
      public static final int BORING_LOG_WIDTH = 50; //Pixel width of the boring log
      public static final int VERTICAL_INCREMENT = 10; //Pixels for each depth increment
      public static final int VERTICAL_HALF_INCREMENT = VERTICAL_INCREMENT/2; //Half of VERTICAL_INCREMENT for shifting the info text vertically
      public static final int TEXT_SHIFT = 5; //Pixels to shift the Text for formatting
      public static final int INFO_TEXT_SHIFT = 30; //Pixels to shift the info text from the boring log
      public static final int LABEL_LENGTH = 300; //Length of the short info Label
      public static final int LABEL_HEIGHT = 30; //Height of the short info Label
      public static final int NEW_LINE_SIZE = 14;
      public static final int WELL_TRIANGLE_HEIGHT = WELL_WIDTH;
      public static final int WELL_TRIANGLE_WIDTH = 10; //Must be an even number
      
      public enum InfoType {LONG, SHORT};
 
     
      private SiteType siteType;
      private boolean sptCalculated;
      private ArrayList<Integer> sptVals;
      private int textDepth;
      private int currentDepth;
      private int count;
      private int finalDepth;
      private int incrementSize;
      private int xCoord;
      private int yCoord;
 	private WaterTable waterTable;
 	private boolean hasInstalledWell;
      //TODO Expand for multiple wells
      private int wellTop, wellBottom;
      private int maxElevation;
      private Boring theBoring;
      private ArrayList<SoilInstance> soilsForThisSite;
      
      //This is used to change the name from B1 to MW1
      private int boringNumber;
      /**
       * The boringID is simply a letter and a number, for example B1
       */
      private String boringID;
      /**
       * The siteID depends on the current site, but does not change with
       * borings, may later be made static.  For example the siteID could
       * be "Blytheville 1"
       */
      private String siteID;
      private JLabel labelWithDepthAndLocation;
 
 
 
      //TODO
      /*
       * Needs a method to paint a well onto the boring log
       */
 
      /**
       * Creates a boring log with all the information necessary to generate the
       * graphical representation.
       */
 
      public BoringLogDisplay(Boring BI, int incSize, String siteID, int maxElevation, SiteType siteType, Site theSite)
      {
           super();
           //this.setOpaque(false); // uncomment to make opaque
          this.setVisible(true);
          sptCalculated = false;
          this.siteType = siteType;
           this.setTheBoring(BI);
           this.siteID = siteID;
           this.setWellTop(BI.getWellStartDepth());
           this.setWellBottom(BI.getWellEndDepth());
           this.maxElevation = maxElevation;
           waterTable = new WaterTableFF(siteID, 0, BI.getDepth(), BI.getxPos(), BI.getyPos(), (SiteFF)theSite);
           textDepth = Y_MARGIN;
           count = 0;
           currentDepth = 0;
           finalDepth = BI.getDepth();
           incrementSize = incSize;
           setxCoord(BI.getxPos());
           setyCoord(BI.getyPos());
           soilsForThisSite = BI.getSoilInstanceList();
           //MS #changing size
           Dimension size = new Dimension(800, BI.getDepth()*12 + 10000);
           setPreferredSize(size);
 //          setMaximumSize(size);
           setMinimumSize(size);
 
 //          System.out.println("boring number: " + boringNumber);
           
           sptVals = new ArrayList<Integer>();
           if(siteID.contains("Fort")){
         	  labelWithDepthAndLocation = new JLabel("Depth: " + BI.getDepth() * 5 + " ft   Elevation: " + maxElevation + " ft   Location: (" + BI.getxPos() +','+ BI.getyPos() + ')');
           } else {
         	  labelWithDepthAndLocation = new JLabel("Depth: " + BI.getDepth() + " ft   Elevation: " + maxElevation + " ft   Location: (" + BI.getxPos() +','+ BI.getyPos() + ')');
           }
           
           setLayout(null);
           //labelWithDepthAndLocation.setBounds(30,5,300,15);
           labelWithDepthAndLocation.setBounds(55,5,400,15);
 
          
      }
 
      @Override
      protected void paintComponent(Graphics g)
      {
           add(labelWithDepthAndLocation);
           createLog(g, InfoType.LONG);
           validate();
      }
 
 
      /**
       * Paints a boring log using the passed in graphics. The boring log
       * is a stack of rectangles that have different colors, it also
       * contains information about the soils which are represented by the
       * rectangles.
       * @param g The graphics type to be used
       * @param type The type of information to be drawn, can either be LONG or SHORT
       */
      public void createLog(Graphics g, InfoType type)
      {
           super.paintComponent(g);
           textDepth = Y_MARGIN;
          
           //Drawing the different soils
           int sptDepth = 0;
           Integer sptValI;
           int sptCount = 0;
           
           for(SoilInstance si : soilsForThisSite)
           {
                Soil currentSoil = si.getSoil();
 //               System.out.println("startDepth: " + si.getStartDepth() + " EndDepth: "+ si.getEndDepth());
                if(!sptCalculated){
 	               if(currentSoil.getSPT() != null){
 		               while(true){
 		                    if(si.getStartDepth() - 1 <= sptDepth && si.getEndDepth() >= sptDepth){
 	//	                         System.out.println("spt depth added: " + sptDepth);
 		                         
 		                         sptValI = (int)GeologyCalculator.performCalc(currentSoil.getSPT(), sptDepth, xCoord, yCoord);
 		                         sptVals.add(sptValI);
 		                         sptDepth += 5;
 		                         System.out.println("sptDepth: " + sptDepth);
 		                        
 		                    }else{
 	//	                         System.out.println("spt not added");
 		                         break;
 		                    }
 		               }
 	               }else {
 	            	   if(si.getStartDepth() - 1 <= sptDepth && si.getEndDepth() >= sptDepth){
 	//                       System.out.println("spt depth added: " + sptDepth);
 	                       sptVals.add(-1);
 	                       sptDepth += 5;
 	                      
 	                  }else{
 	//                       System.out.println("spt not added");
 	                       break;
 	                  }
 	            	   
 	               }
                }
                //System.out.println("boringlogdisplay -> currentSoil Id = " + currentSoil.getId());
                g.setColor(new Color(currentSoil.getColor())); //Sets the color of the soil
                //System.out.println("soil: " + currentSoil.getDescription());
                g.fillRect(X_MARGIN, (Y_MARGIN + (si.getStartDepth()-1)*VERTICAL_INCREMENT),
                          BORING_LOG_WIDTH, (si.getEndDepth() - si.getStartDepth() + 1)*VERTICAL_INCREMENT); //Draws the rectangle component of the log
 //               System.out.println(si.getStartDepth());
                //System.out.println("startDepth: " + si.getStartDepth());
                //System.out.println("endDepth: " + si.getEndDepth());
                g.setColor(Color.black);
 
                String info = currentSoil.getUSCS() + " - ";
                //This is where the information gets drawn
                if(type == InfoType.LONG )
                {
                     drawLongSiteInfo(si, info + currentSoil.getDescription(), g);
                }
                else if(type == InfoType.SHORT)
                {
                     drawShortSiteInfo(si, info + currentSoil.getShortDescription(), g);
                }
                //draws the horizontal borders
                g.drawLine(X_MARGIN ,
                          Y_MARGIN + (VERTICAL_INCREMENT*(si.getStartDepth()-1)),
                          X_MARGIN + BORING_LOG_WIDTH,
                          Y_MARGIN + (VERTICAL_INCREMENT*(si.getStartDepth()-1)));
                g.drawLine(X_MARGIN ,
                          Y_MARGIN + (VERTICAL_INCREMENT*(si.getEndDepth())),
                          X_MARGIN + BORING_LOG_WIDTH,
                          Y_MARGIN + (VERTICAL_INCREMENT*(si.getEndDepth())));
                //draws the vertical borders
                g.drawLine(X_MARGIN, Y_MARGIN, X_MARGIN, Y_MARGIN + (VERTICAL_INCREMENT*(si.getEndDepth())));
                g.drawLine(X_MARGIN + BORING_LOG_WIDTH, Y_MARGIN, X_MARGIN + BORING_LOG_WIDTH, Y_MARGIN + (VERTICAL_INCREMENT*(si.getEndDepth())));
               
                //System.out.println("line drawn");
           }
           sptCalculated = true;
 //          int sptCount =0;
 //          for(Double d : sptVals){
 //               System.out.println("SPT" + sptCount + " :" + d);
 //               sptCount++;
 //          }
          
           //Draws well Installation if there is a well that should be displayed(patterned rectangle)
           drawWell(g);
           //watertable.
          
           int waterDepth = maxElevation - waterTable.getWaterElevation();
 //          System.out.println("waterDepth: " + waterDepth);
           if(this.siteType == SitePanel.SiteType.GEOTECH && finalDepth >= waterDepth){
               
                Polygon wellTriangle = makeTriangle(waterDepth);
                JLabel waterElevationLabel = new JLabel("Groundwater Elevation: " + waterTable.getWaterElevation());
                waterElevationLabel.setBounds(440,5,600,15);
                g.fillPolygon(wellTriangle);
                add(waterElevationLabel);
                g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST, Y_MARGIN + VERTICAL_INCREMENT*waterDepth, X_MARGIN-WELL_BORING_LOG_DIST+WELL_WIDTH, Y_MARGIN + VERTICAL_INCREMENT*waterDepth);
           }
          
           //g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST-WELL_HALF_WIDTH, y1, x2, y2)
          
          
           //Draws depth increments
           int c = 0;
          System.out.println("#finalDepth: " + finalDepth);
          if(siteID.contains("Fort")){
        	  finalDepth = 440;
          }
           for(int i = 0; i <= finalDepth; i+= incrementSize)
           {
               
                g.setColor(Color.black);
                if(i%(incrementSize*5) == 0)
                {
                     /*g.drawString(("-"),
                               (X_MARGIN - 4),
                               (TEXT_SHIFT + Y_MARGIN + (VERTICAL_INCREMENT*count))); //Displays the depth*/
                     g.drawLine(X_MARGIN - 6, (VERTICAL_INCREMENT*count+ Y_MARGIN), X_MARGIN, (VERTICAL_INCREMENT*count+ Y_MARGIN));
                     if(i != finalDepth){
                     	if(c < sptVals.size()){
                     		if(sptVals.get(c) == -1){
                         		g.drawString("N/A", SPT_MARGIN, VERTICAL_INCREMENT*count + Y_MARGIN + 5);
                         	}else{
                         		g.drawString(Integer.toString(sptVals.get(c)), SPT_MARGIN, VERTICAL_INCREMENT*count + Y_MARGIN + 5);
                         	}
                     	}
                     	
                     }
                     g.drawString("SPT", SPT_MARGIN, Y_MARGIN - 14);
                     c++;
                    
                         
                     g.drawString((Integer.toString(currentDepth)),
                              (X_MARGIN - 33),
                               (TEXT_SHIFT + Y_MARGIN + (VERTICAL_INCREMENT*count))); //Displays the depth
                }
                // draws the black bar indicating where boring was taken from
                if(i%(incrementSize*5)==1){
                     g.fillRect((X_MARGIN - 3),
                          (Y_MARGIN + (VERTICAL_INCREMENT*(count-1)))
                          , 3,
                          VERTICAL_INCREMENT);
                }
                currentDepth += incrementSize;
                count++;
 
               
           }
           reset();
           currentDepth = 0;
          
 
      }
 
      private void drawWell(Graphics g)
      {
           if((wellBottom-wellTop)!=0)
           {    
                int waterDepth = maxElevation - waterTable.getWaterElevation();
                //Adds the waterElevation label
               
                //add(waterElevationLabel);
          
                //Triangle marker above the well installation
                Polygon wellTriangle = makeTriangle(waterDepth);
               
                // Should only be drawn if groundwater elevation is above the bottom of the well DONE
                if(wellBottom >= waterDepth){
                     JLabel waterElevationLabel = new JLabel("Groundwater Elevation: " + waterTable.getWaterElevation());
                     waterElevationLabel.setBounds(340,5,500,15);
                     g.fillPolygon(wellTriangle);
                     add(waterElevationLabel);
                     g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST, Y_MARGIN + VERTICAL_INCREMENT*waterDepth, X_MARGIN-WELL_BORING_LOG_DIST+WELL_WIDTH, Y_MARGIN + VERTICAL_INCREMENT*waterDepth);
                }
                g.drawRect(X_MARGIN-WELL_BORING_LOG_DIST, (wellTop*VERTICAL_INCREMENT + Y_MARGIN), WELL_WIDTH, ((wellBottom-wellTop)*VERTICAL_INCREMENT));
               
               
                //Draws the horizontal dashed pattern
                for(int i = (wellTop*VERTICAL_INCREMENT + Y_MARGIN); i < (wellBottom*VERTICAL_INCREMENT + Y_MARGIN); i++)
                {
                     if(i%4==0)
                     {
                          //draws a horizontal line within the well symbol every 4 pixels
                          g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST, i, X_MARGIN-WELL_BORING_LOG_DIST+WELL_WIDTH, i);
                     }
                }
               
                //Draws horizontal lines on top and bottom of well line and well triangle
                g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST, Y_MARGIN, X_MARGIN-WELL_BORING_LOG_DIST+WELL_WIDTH, Y_MARGIN);
                g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST, Y_MARGIN + VERTICAL_INCREMENT*finalDepth, X_MARGIN-WELL_BORING_LOG_DIST+WELL_WIDTH, Y_MARGIN + VERTICAL_INCREMENT*finalDepth);
                //MS
                //g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST, Y_MARGIN + VERTICAL_INCREMENT*waterDepth, X_MARGIN-WELL_BORING_LOG_DIST+WELL_WIDTH, Y_MARGIN + VERTICAL_INCREMENT*waterDepth);
               
                //Draws vertical lines above and beneath the well
                g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST+(WELL_WIDTH/2), Y_MARGIN, X_MARGIN-WELL_BORING_LOG_DIST+(WELL_WIDTH/2), wellTop*VERTICAL_INCREMENT + Y_MARGIN);
                g.drawLine(X_MARGIN-WELL_BORING_LOG_DIST+(WELL_WIDTH/2), wellBottom*VERTICAL_INCREMENT + Y_MARGIN, X_MARGIN-WELL_BORING_LOG_DIST+(WELL_WIDTH/2), finalDepth*VERTICAL_INCREMENT + Y_MARGIN);
               
           }
      }
 
     
      private Polygon makeTriangle(int prevDepth)
      {
          
           int xCoords[] = {X_MARGIN-WELL_BORING_LOG_DIST+(WELL_TRIANGLE_WIDTH/2), X_MARGIN-WELL_BORING_LOG_DIST, X_MARGIN-WELL_BORING_LOG_DIST+WELL_TRIANGLE_WIDTH};
           int yCoords[] = {(prevDepth*VERTICAL_INCREMENT)+Y_MARGIN, (prevDepth*VERTICAL_INCREMENT)+Y_MARGIN - WELL_TRIANGLE_HEIGHT, (prevDepth*VERTICAL_INCREMENT)+Y_MARGIN - WELL_TRIANGLE_HEIGHT};
           Polygon triangle = new Polygon(xCoords, yCoords, 3);
           return triangle;
      }
     
     
      /**
       * TODO This function is used for temporary purposes, should either
       * be removed or updated closer to completion.
       * The function draws the short information for a site by finding the
       * soil that corresponds to the given color.  Currently only applies
       * to Blytheville sites.
       * @param currentColor The color of the current depth.
       * @param g The graphics to be used to draw the information.
       */
      private void drawShortSiteInfo(SoilInstance si,String info,Graphics g) {
           ArrayList<String> formattedInfo = formatTheTextToFitWindow(info, InfoType.SHORT);
           int newLine = 0;
           int baseX = (X_MARGIN + BORING_LOG_WIDTH);
           int baseY = (Y_MARGIN + (VERTICAL_INCREMENT*(si.getStartDepth()-1)));
 
 
           if(textDepth < baseY)
           {
                textDepth = baseY;
           }
 
           int extraTextDepth = textDepth - baseY;
 
           //Draws top line
           g.drawLine(baseX + INFO_TEXT_SHIFT, textDepth, (int) this.getPreferredSize().getWidth()/3, textDepth);
 
 
           for(String s : formattedInfo)
           {
                g.drawString(s, (baseX + INFO_TEXT_SHIFT), baseY + extraTextDepth + newLine + (NEW_LINE_SIZE/2) + TEXT_SHIFT);
                newLine += NEW_LINE_SIZE;
           }
 
           textDepth += newLine;
 
 
           g.drawLine((baseX),(baseY),(baseX + INFO_TEXT_SHIFT), (baseY + extraTextDepth));
 
      }
 
      /**
       * TODO This function is used for temporary purposes, should either
       * be removed or updated closer to completion.
       * The function draws the long information for a site by finding the
       * soil that corresponds to the given color.  Currently only applies
       * to Blytheville sites.
       * @param currentColor The color of the current depth.
       * @param g The graphics to be used to draw the information.
       */
      private void drawLongSiteInfo(SoilInstance si, String info, Graphics g) {
           ArrayList<String> formattedInfo = formatTheTextToFitWindow(info, InfoType.LONG);
           int newLine = 0;
           int baseX = (X_MARGIN + BORING_LOG_WIDTH);
           int baseY = (Y_MARGIN + (VERTICAL_INCREMENT*(si.getStartDepth()-1)));
 
 
           if(textDepth < baseY)
           {
                textDepth = baseY;
           }
 
           int extraTextDepth = textDepth - baseY;
 
           //Draws top line
           g.drawLine(baseX + INFO_TEXT_SHIFT, textDepth, (int) this.getPreferredSize().getWidth(), textDepth);
 
 
           for(String s : formattedInfo)
           {
                g.drawString(s, (baseX + INFO_TEXT_SHIFT), baseY + extraTextDepth + newLine + (NEW_LINE_SIZE/2) + TEXT_SHIFT);
                newLine += NEW_LINE_SIZE;
           }
 
           textDepth += newLine;
 
           g.drawLine((baseX),(baseY),(baseX + INFO_TEXT_SHIFT), (baseY + extraTextDepth));
 
      }
 
      /**
       * Resets values that need to be reused to their defaults during
       * the drawing of boring logs.
       */
      private void reset()
      {
           count = 0;
           currentDepth = 0;
      }
      /**
       * Returns a list of lines of the text that is passed in, at
       * a minimum, it will always return an array list with one
       * value inside of it.
       * @param info The full string that needs to be formatted
       * @param type The type of string to format, SHORT or LONG
       * @return The list of formatted text text to fit the current window.
       */
      private ArrayList<String> formatTheTextToFitWindow(String info, InfoType type) {
           int charactersPerLine;
           if(type == InfoType.LONG)
                charactersPerLine = 103;
           else
                charactersPerLine = 25;
 
           ArrayList<String> formattedText = new ArrayList<String>();
           if(info.length()>charactersPerLine)
           {
                int textRemaining = info.length();
                int beginIndex = 0;
                int endIndex = charactersPerLine;
                while(textRemaining>0)
                {
                     String temp = info.substring(beginIndex, endIndex);
                     if(temp.charAt(temp.length()-1) != ' '  && textRemaining > charactersPerLine)
                     {
                          endIndex = endIndex - (temp.length() - temp.lastIndexOf(' ')-1);
                          temp = info.substring(beginIndex, endIndex);
                     }
                     int textPrinted = endIndex - beginIndex;
                     textRemaining-= textPrinted;
                     beginIndex = endIndex;
                     if(textRemaining < charactersPerLine)
                          endIndex += textRemaining;
                     else
                          endIndex+= textPrinted;
                     formattedText.add(temp);
                }
                return formattedText;
           }
           else
           {
                formattedText.add(info);
                return formattedText;
           }
      }
 
 
 
      public void setxCoord(int xCoord) {
           this.xCoord = xCoord;
      }
 
 
 
      public int getxCoord() {
           return xCoord;
      }
 
 
 
      public void setyCoord(int yCoord) {
           this.yCoord = yCoord;
      }
 
 
 
      public int getyCoord() {
           return yCoord;
      }
 
      public int getFinalDepth() {
           return finalDepth;
      }
 
      public String getSiteID() {
           return siteID;
      }
 
 
 
      public JLabel getLabelWithDepthAndLocation() {
           return labelWithDepthAndLocation;
      }
 
 
 
      public int getIncrementSize() {
           return incrementSize;
      }
 
 
 
      public void setBoringID(String boringID) {
           this.boringID = boringID;
      }
 
 
 
      public String getBoringID() {
           return boringID;
      }
 
      public void setWellTop(int wellTop) {
           this.wellTop = wellTop;
      }
 
      public int getWellTop() {
           return wellTop;
      }
 
      public void setWellBottom(int wellBottom) {
           this.wellBottom = wellBottom;
      }
 
      public int getWellBottom() {
           return wellBottom;
      }
 
      public void setWaterTable(WaterTable waterTable) {
           this.waterTable = waterTable;
      }
 
      public WaterTable getWaterTable() {
           return waterTable;
      }
 
      public void setHasInstalledWell(boolean hasInstalledWell) {
           this.hasInstalledWell = hasInstalledWell;
      }
 
      public boolean getHasInstalledWell() {
           return hasInstalledWell;
      }
 
      public void setTheBoring(Boring theBoring) {
           this.theBoring = theBoring;
      }
 
      public Boring getTheBoring() {
           return theBoring;
      }
      
      public void setBoringNumber(int num){
     	 boringNumber = num;
      }
      public int getBoringNumber(){
     	 return boringNumber;
      }
 
 
 
 }
