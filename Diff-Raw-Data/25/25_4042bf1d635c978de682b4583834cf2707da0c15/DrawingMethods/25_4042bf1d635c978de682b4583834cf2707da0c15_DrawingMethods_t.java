 package edu.mit.wi.haploview;
 
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.text.*;
 import java.io.*;
 import java.awt.image.*;
 import java.awt.geom.*;
 import com.sun.jimi.core.Jimi;
 import com.sun.jimi.core.JimiException;
 import com.sun.jimi.core.component.JimiCanvas;
 
 
 class DrawingMethods {
 
     int labeloffset = 80;
     
     public Dimension haploGetPreferredSize(Haplotype[][] hapsInBlocks, Graphics tg){
 	int windowX = 10;
 	int windowY = 10;
 	int height = 0;	 
 	FontMetrics fm = tg.getFontMetrics(new Font("Lucida Bright", Font.PLAIN, 12));
 	
 	for (int i = 0; i < hapsInBlocks.length; i++){
 	    height = ((fm.getHeight() + 5) * hapsInBlocks[i].length) + 53;
 	    String sizeString = new String();
 	    int[] theGeno = hapsInBlocks[i][0].getGeno();
 	    for (int k = 0; k < theGeno.length; k++){
 		sizeString += theGeno[k];
 	    }
 	    // add space for percentage which should be equal to or less than:
 	    sizeString += "(8.888)";
 	    
 	    windowX += (fm.stringWidth(sizeString) + 40);
 	    if (windowY < height){windowY = height;}
 	}
 	
 	return new Dimension(windowX, windowY);
     }
 
     public void haploDraw(Graphics gr, boolean myuseThickness,
 			  int mycolorThresh, int mycrossThinThresh, int mycrossThickThresh,
 			  double[] gapDPrime, Haplotype[][] hapsInBlocks){
 
 	Graphics2D g = (Graphics2D) gr;
 	final BasicStroke stroke = new BasicStroke(1.0f);
 	final BasicStroke wideStroke = new BasicStroke(2.0f);
 	final int verticalOffset = 43;
 	final Font nonMonoFont = new Font("Lucida Bright", Font.PLAIN, 12);
 	final Font regFont = new Font("Lucida Sans Typewriter", Font.PLAIN, 12);
 	final Font smallFont = new Font("Lucida Sans Typewriter", Font.PLAIN, 7);
 	final Font boldFont = new Font("Lucida Bright", Font.BOLD, 12);
 	FontMetrics regfm = g.getFontMetrics(regFont);
 	FontMetrics nonMonofm = g.getFontMetrics(nonMonoFont);
 	FontMetrics boldfm = g.getFontMetrics(boldFont);
 	String theHap = new String();
 	int x = 10;
 	int y = verticalOffset;
 	int totalWidth = 0;
 	NumberFormat nf = NumberFormat.getInstance();
 	NumberFormat nfMulti = NumberFormat.getInstance();
 	nf.setMinimumFractionDigits(3);
 	nf.setMaximumFractionDigits(3);
 	nfMulti.setMinimumFractionDigits(2);
 	nfMulti.setMaximumFractionDigits(2);
 
 	
 	int[][]lookupPos = new int[hapsInBlocks.length][];
 	for (int p = 0; p < lookupPos.length; p++){
 	    lookupPos[p] = new int[hapsInBlocks[p].length];
 	    for (int q = 0; q < lookupPos[p].length; q++){
 		lookupPos[p][hapsInBlocks[p][q].getListOrder()] = q;
 		//System.out.println(p + " " + q + " " + hapsInBlocks[p][q].getListOrder());
 	    }
 	}
 
 	Dimension theDimension = haploGetPreferredSize(hapsInBlocks, gr);
 	int windowX = (int)theDimension.getWidth();
 	int windowY = (int)theDimension.getHeight();
 	g.setColor(Color.white);
 	g.fillRect(0,0,windowX,windowY);
 	g.setColor(Color.black);
 
 	for (int i = 0; i < hapsInBlocks.length; i++){
 	    int[] markerNums = hapsInBlocks[i][0].getMarkers();
 	    boolean[] tags = hapsInBlocks[i][0].getTags();
 	    int headerX = x;
 	    for (int z = 0; z < markerNums.length; z++){
 		//put tag snps in red
 		if (tags[z]) {
 		    g.setColor(Color.red);
 		}
 		//write labels with more than one digit vertically
 		if (markerNums[z]+1 < 10){
 		    g.setFont(regFont);
 		    g.drawString(String.valueOf(markerNums[z]+1), headerX, 18);
 		    headerX += (regfm.stringWidth(String.valueOf(markerNums[z]+1)));
 		}else {
 		    int ones = (markerNums[z]+1)%10;
 		    int tens = (((markerNums[z]+1)-ones)%100)/10;
 		    g.setFont(regFont);
 		    g.drawString(String.valueOf(ones), headerX, 18);
 		    g.setFont(smallFont);
 		    g.drawString(String.valueOf(tens), headerX-2, 20-regfm.getAscent());
 		    headerX += (regfm.stringWidth(String.valueOf(ones)));
 		}
 		g.setColor(Color.black);
 	    }
 	    for (int j = 0; j < hapsInBlocks[i].length; j++){
 		int curHapNum = lookupPos[i][j];
 		theHap = new String();
 		String thePercentage = new String();
 		int[] theGeno = hapsInBlocks[i][curHapNum].getGeno();
 		for (int k = 0; k < theGeno.length; k++){
 		    //if we don't know what one of the alleles for a marker is, use "x"
 		    if (theGeno[k] == 8){
 			theHap += "x";
 		    }else{
 			theHap += theGeno[k];
 		    }
 		}
 		//draw the haplotype in mono font
 		g.setFont(regFont);
 		g.drawString(theHap, x, y);
 		//draw the percentage value in non mono font
 		thePercentage = " (" + nf.format(hapsInBlocks[i][curHapNum].getPercentage()) + ")";
 		g.setFont(nonMonoFont);
 		g.drawString(thePercentage, x+regfm.stringWidth(theHap), y);
 		totalWidth = regfm.stringWidth(theHap) + nonMonofm.stringWidth(thePercentage);
 		
 		if (i < hapsInBlocks.length - 1){  //draw crossovers
 		    for (int crossCount = 0; crossCount < hapsInBlocks[i+1].length; crossCount++){
 			double crossVal = hapsInBlocks[i][curHapNum].getCrossover(crossCount);
 			if (myuseThickness){
 				//draw thin and thick lines
 			    if (crossVal*100 > mycrossThinThresh){
 				if (crossVal*100 > mycrossThickThresh){
 				    g.setStroke(wideStroke);
 				}else{
 				    g.setStroke(stroke);
 				}
 				//this arcane formula draws lines neatly from one hap to another
 				g.draw(new Line2D.Double((x+totalWidth+3),
 							  (y-regfm.getAscent()/2),
 							  (x+totalWidth+37),
 							  (verticalOffset-regfm.getAscent()/2+((regfm.getHeight()+5)*hapsInBlocks[i+1][crossCount].getListOrder()))));
 			    }
 			}else{
 			    //draw colored lines
 			    //only draw if this connection is strong enough
 			    if(crossVal*100 > mycolorThresh){
 				g.setStroke(stroke);
 				Color lineColor;
 				//set the color based on strength
 				if (crossVal > 0.25){
 				    lineColor = new Color(255,0,0);
 				}else if (crossVal > 0.20){
 				    lineColor = new Color(255,153,0);
 				}else if (crossVal > 0.15){
 				    lineColor = new Color(0,204,0);
 				}else if (crossVal > 0.5){
 				    lineColor = new Color(0,51,204);
 				}else{
 				    lineColor = new Color(0,0,153);
 				}
 
 				g.setColor(lineColor);
 				g.setStroke(new BasicStroke(1.5f));
 				g.draw(new Line2D.Double((x+totalWidth+3),
 							  (y-regfm.getAscent()/2),
 							  (x+totalWidth+37),
 							  (verticalOffset-regfm.getAscent()/2+((regfm.getHeight()+5)*hapsInBlocks[i+1][crossCount].getListOrder()))));
 				g.setColor(Color.black);
 				g.setStroke(stroke);
 			    }
 			}
 		    }
 		}
 		y += (regfm.getHeight()+5);
 	    }
 	    //add the multilocus d prime if appropriate
 	    if (i < hapsInBlocks.length - 1){
 		int multiX = x +totalWidth+3;
 		g.setStroke(wideStroke);
 		g.setFont(boldFont);
 		g.drawRect(multiX, windowY-boldfm.getAscent()-4, boldfm.stringWidth("8.88")+3, boldfm.getAscent()+3);
 		g.drawString(String.valueOf(nfMulti.format(gapDPrime[i])), multiX+2, windowY - 3);
 		g.setStroke(stroke);
 	    }
 	    x += (totalWidth + 40);
 	    y = verticalOffset;
 	}
     }
 
 
     public Dimension dPrimeGetPreferredSize(int size, boolean info){
 	if (info){
 	    return new Dimension((labeloffset+size*30), size*30);
 	}else{
 	    return new Dimension(size*30, size*30);
 	}
     }
 
     public void dPrimeDraw(String[][] table, boolean info, Vector snps, Graphics g){
 	int scale = table.length*30;
 	int activeOffset = 0;
 	float d, l, blgr;
 	Color myColor;
 	int[] shifts;
 	Font regFont = new Font("Lucida Sans Regular", Font.PLAIN, 10);
 	FontMetrics regfm = g.getFontMetrics(regFont);
 	Font boldFont = new Font("Lucida Sans Bold", Font.BOLD, 14);
 	FontMetrics boldfm = g.getFontMetrics(boldFont);	
 
 	if (info) activeOffset = labeloffset;
 
 	//background color
 	g.setColor(new Color(192,192,192));
 	g.fillRect(0,0,scale+activeOffset,scale);
 
 	//first label:
 	g.setColor(Color.black);
 	g.setFont(boldFont);
 	shifts = centerString("1", boldfm);
 	g.drawString("1", activeOffset + shifts[0], shifts[1]);
 
 	//if we know the marker names, print them down the side
 	if (info){
 	    g.setFont(regFont);
 	    for (int y = 0; y < table.length; y++){
 		String name = ((SNP)snps.elementAt(y)).getName();
 		g.drawString(name, labeloffset-3-regfm.stringWidth(name), y*30 + shifts[1]);
 	    }
 
 	    //now draw a diagonal bar showing marker spacings
 	    if (table.length > 3){
 		g.drawLine(labeloffset+90,5,labeloffset+scale-5,scale-90);
 		double lineLength = Math.sqrt((scale-95)*(scale-95)*2);
 		double start = ((SNP)snps.elementAt(0)).getPosition();
 		double totalLength = ((SNP)snps.elementAt(table.length-1)).getPosition() - start;
 		int numKB = (int)(totalLength/1000);
 		g.drawString(numKB+" Kb", labeloffset+150, 30); 
 		for (int y = 0; y < table.length; y++){
 		    double fracLength = (((SNP)snps.elementAt(y)).getPosition() - start)/totalLength;
 		    double xOrYDist = Math.sqrt((fracLength*lineLength*fracLength*lineLength)/2);
 		    g.drawLine(labeloffset+25+y*30, 5+y*30,(int)(labeloffset+90+xOrYDist),(int)(5+xOrYDist));
 		}
 	    }
 	}
 
 	//draw table column by column
 	for (int x = 0; x < table.length-1; x++){
 	    for (int y = x + 1; y < table.length; y++){
 		StringTokenizer st = new StringTokenizer(table[x][y]);
		d = Float.parseFloat(st.nextToken());
 		l = Float.parseFloat(st.nextToken());
 		//set coloring based on LOD and D'
 		if (l > 2){
 		    if (d < 0.5) {
 			//high LOD, low D' bluish color
 			myColor = new Color(255, 224, 224);
 		    } else {
 			//high LOD, high D' shades of red
 			blgr = (255-32)*2*(1-d);
 			myColor = new Color(255, (int) blgr, (int) blgr);
 		    } 
 		}else if (d > 0.99){
 		    //high D', low LOD gray color
 		    myColor = new Color(192, 192, 240);
 		}else {
 		    //no LD
 		    myColor = Color.white;
 		}
 
 		//draw the boxes
 		g.setColor(myColor);
 		g.fillRect(x*30+1+activeOffset, y*30+1, 28, 28);
 		g.setColor(Color.black);
 		g.drawRect(x*30+activeOffset, y*30, 30, 30);
 		g.setFont(regFont);
 		shifts=centerString(Float.toString(d), regfm);
 		g.drawString(Float.toString(d), shifts[0]+(x*30)+activeOffset,(y*30)+shifts[1]);
 	    }
 	    //draw the labels
 	    g.setColor(Color.black);
 	    g.setFont(boldFont);
 	    shifts = centerString(Integer.toString(x+2), boldfm);
 	    g.drawString(Integer.toString(x+2), shifts[0]+(x+1)*30+activeOffset, shifts[1]+(x+1)*30);
 	}
     }
 
     int[] centerString(String s, FontMetrics fm){
 	int[] returnArray = new int[2];
 	returnArray[0] = (30-fm.stringWidth(s))/2;
 	returnArray[1] = 10+(30-fm.getAscent())/2;
 
 	return returnArray;
     }
 
     void saveImage(BufferedImage image, String filename) throws IOException{
 	try {
 	    if (! (filename.endsWith(".jpg") || filename.endsWith(".JPG"))){
 		filename += ".jpg";
 	    }
 	    Jimi.putImage("image/jpg", (Image)image, filename);
 	}catch (com.sun.jimi.core.JimiException e){
 	    e.printStackTrace();
 	}
     }
 }
 	
