 package lichen.controller;
 import ij.*;
 import ij.process.ColorProcessor;
 import ij.process.FloatProcessor;
 import ij.process.ImageProcessor;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 import javax.naming.NameNotFoundException;
 
 import de.thm.bi.recognition.data.Point;
 import lichen.model.Genus;
 import lichen.model.Measurement;
 import lichen.model.MeasurementsFactory;
 import lichen.view.MainGUI;
 
 
 /**	This class, which does flood filling, is used by the floodFill() macro function and
 	by the particle analyzer
 	The Wikipedia at "http://en.wikipedia.org/wiki/Flood_fill" has a good 
 	description of the algorithm used here as well as examples in C and Java. 
  */
 public class FloodFiller {
 	int maxStackSize = 500; // will be increased as needed
 	int[] xstack = new int[maxStackSize];
 	int[] ystack = new int[maxStackSize];
 	int stackSize;
 	ImageProcessor ip;
 	int max;
 	boolean isFloat;
 	protected long pixelcount = 0;
 	private int ThallusCount = 0;
 	protected double linewidth = 0.76;
 	protected double pixelrate; 
 	private UndoStack undoStack;
 	private ArrayList<int[]> undoPos = new ArrayList<int[]>(); 
 	private ArrayList<String[]> thalliList = new ArrayList<String[]>();
 
 	public FloodFiller(ImageProcessor ip) {
 		this.ip = ip;
 		this.undoStack = new UndoStack(ip);
 		MainGUI.getInstance().setUndoStack(this.undoStack);
 
 		isFloat = ip instanceof FloatProcessor;
 	}
 
 	/** Does a 4-connected flood fill using the current fill/draw
 		value, which is defined by ImageProcessor.setValue(). */
 	@SuppressWarnings("unchecked")
 	public boolean fill(int x, int y) {
 		double oldPixelcount = pixelcount;
 		
 		String[] tmp = new String[2]; 
 		tmp[0] = x + ":" + y; 
 
 		
 		undoPos.clear();
 		//		undoPos.clear();
 
 		int width = ip.getWidth();
 		int height = ip.getHeight();
 		int color = ip.getPixel(x, y);
 
 		fillLine(ip, x, x, y);
 		int newColor = ip.getPixel(x, y);
 		ip.putPixel(x, y, color);
 		if (color==newColor) return false;
 		stackSize = 0;
 		push(x, y);
  
 		while(true) { 
 			//boolean pushed = false;
 			//			System.out.println("SS: "+ stackSize);
 			x = popx(); 
 			if (x ==-1) break;
 			y = popy();
 			if (ip.getPixel(x,y)!=color) continue;
 			int x1 = x; int x2 = x;
 			while (ip.getPixel(x1,y)==color && x1>=0) x1--; // find start of scan-line
 			//			x1++; 
 			while (ip.getPixel(x2,y)==color && x2<width) x2++;  // find end of scan-line                 
 			//			x2--;
 
 
 			fillLine(ip, x1, x2,y);
 
 
 			boolean inScanLine = false;
 			for (int i=x1; i<=x2; i++) { // find scan-lines above this one
 				if (!inScanLine && y>0 && ip.getPixel(i,y-1)==color) {
 					push(i, y-1); 
 					inScanLine = true;
 				} 
 				else if (inScanLine && y>0 && ip.getPixel(i,y-1)!=color){ 
 					inScanLine = false; 
 				} 
 			}
 
 			inScanLine = false;
 			for (int i=x1; i<=x2; i++) { // find scan-lines below this one
 				if (!inScanLine && y<height-1 && ip.getPixel(i,y+1)==color){
 					push(i, y+1); 
 					inScanLine = true;
 				}
 				else if (inScanLine && y<height-1 && ip.getPixel(i,y+1)!=color)
 					inScanLine = false;
 			}
 		} 
 
 		fillEdge((int) Math.round((linewidth*pixelrate)));
 
 		undoStack.add((ArrayList<int[]>) undoPos.clone());
 
 		ThallusCount++; 
		tmp[1] = pixelcount+""; 
 		this.thalliList.add(tmp);
 		
 
 		return true;
 	}
 
 	/**
 	 * finds the edge of the filled area and adds an edge to it
 	 * TODO
 	 */
 
 	private void fillEdge(int n) { 
 		//decrease edge size:
 		n = (int) Math.round(n-n*0.333);
 
 		int[] imageArray = new int[ip.getWidth()* ip.getHeight()];
 		ImageProcessor org = MainGUI.getInstance().getImp().getProcessor();
 
 		//	imageArray = (int[]) MainGUI.getInstance().getImp().getProcessor().getPixels();
 
 		for(int i = 0; i < imageArray.length; i++){
 			imageArray[i]= -1;
 		}
 
 //		for(Point p: undoPos){
 //			imageArray[p.y*ip.getWidth()+p.x] = 0; 
 //		}
 
 		for(int[] p: undoPos){ 
 			imageArray[p[1]*ip.getWidth()+p[0]] = 0; 
 		}
 		
 		ImagePlus imp = new ImagePlus("", new ColorProcessor(ip.getWidth(), ip.getHeight(), imageArray));
 		//		imp.getProcessor().findEdges();
 		ImageProcessor impp = imp.getProcessor();
 
 		ArrayList<int[]> edge = new ArrayList<int[]>();
 
 
 		for(int i =0; i < impp.getWidth(); i++){
 			for(int j = 0; j< impp.getHeight(); j++){
 				int pixel = impp.getPixel(i, j);
 				if(pixel == 0){ 
 					for(int b = 1; b <= n; b++){ 
 						if(impp.getPixel(i, j-n) == -1)
 							edge.add(new int[] {i, j-n});
 
 						if(impp.getPixel(i, j+n) == -1)
 							edge.add(new int[] {i, j+n});
 
 						if(impp.getPixel(i-n, j) == -1)
 							edge.add(new int[] {i-n, j});
 
 						if(impp.getPixel(i+n, j) == -1) 
 							edge.add(new int[] {i+n, j});
 					}
 
 				}
 			}
 		}
 
 		for(int[] p: edge){
 			if(impp.getPixel(p[0], p[1]) != 0){ 
 				//	undoArray[p[0]][p[1]] = ip.getPixel(p[0], p[1]); 
 								undoPos.add(new int[] {p[0],p[1]});
 				
 //				undoPos.add(new Point(p[0], p[1]));
 
 				this.pixelcount++;
 
 				impp.setPixel(p[0] , p[1], 0); 
 				org.drawPixel(p[0], p[1]); 
 			}
 		}
 
 		//	imp.setProcessor(impp); 
 		//	MainGUI.getInstance().setImp(imp);
 
 	}
 
 	/** Does a 8-connected flood fill using the current fill/draw
 		value, which is defined by ImageProcessor.setValue(). */
 	public boolean fill8(int x, int y) {
 		int width = ip.getWidth();
 		int height = ip.getHeight();
 		int color = ip.getPixel(x, y);
 		int wm1=width-1;
 		int hm1=height-1; 
 		fillLine(ip, x, x, y);
 		int newColor = ip.getPixel(x, y);
 		ip.putPixel(x, y, color);
 		if (color==newColor) return false;
 		stackSize = 0;
 		push(x, y);
 		while(true) {   
 			x = popx(); 
 			if (x==-1) return true;
 			y = popy();
 			int x1 = x; int x2 = x;
 			if(ip.getPixel(x1,y)==color){ 
 				while (ip.getPixel(x1,y)==color && x1>=0) x1--; // find start of scan-line
 				x1++;
 				while (ip.getPixel(x2,y)==color && x2<width) x2++;  // find end of scan-line
 				x2--;
 				fillLine(ip, x1,x2,y); // fill scan-line
 			} 
 			if(y>0){
 				if (x1>0){
 					if (ip.getPixel(x1-1,y-1)==color){
 						push(x1-1,y-1);
 					}
 				}
 				if (x2<wm1){
 					if (ip.getPixel(x2+1,y-1)==color){
 						push(x2+1,y-1);
 					}
 				}
 			}
 			if(y<hm1){
 				if (x1>0){
 					if (ip.getPixel(x1-1,y+1)==color){
 						push(x1-1,y+1);
 					}
 				}
 				if (x2<wm1){
 					if (ip.getPixel(x2+1,y+1)==color){
 						push(x2+1,y+1);
 					}
 				}
 			}
 			boolean inScanLine = false;
 			for (int i=x1; i<=x2; i++) { // find scan-lines above this one
 				if (!inScanLine && y>0 && ip.getPixel(i,y-1)==color)
 				{push(i, y-1); inScanLine = true;}
 				else if (inScanLine && y>0 && ip.getPixel(i,y-1)!=color)
 					inScanLine = false;
 			}
 			inScanLine = false;
 			for (int i=x1; i<=x2; i++) {// find scan-lines below this one
 				if (!inScanLine && y<hm1 && ip.getPixel(i,y+1)==color)
 				{push(i, y+1); inScanLine = true;}
 				else if (inScanLine && y<hm1 && ip.getPixel(i,y+1)!=color)
 					inScanLine = false;
 			}
 		}
 	}
 
 
 	/** This method is used by the particle analyzer to remove interior holes from particle masks. */
 	public void particleAnalyzerFill(int x, int y, double level1, double level2, ImageProcessor mask, Rectangle bounds) {
 		//if (count>100) return;
 		int width = ip.getWidth();
 		int height = ip.getHeight();
 		mask.setColor(0);
 		mask.fill();
 		mask.setColor(255);
 		stackSize = 0;
 		push(x, y);
 		while(true) {   
 			x = popx(); 
 			if (x ==-1) break;
 			y = popy();
 			if (!inParticle(x,y,level1,level2)) continue;
 			int x1 = x; int x2 = x;
 
 			while (inParticle(x1,y,level1,level2) && x1>=0) x1--; // find start of scan-line
 			x1++;
 			while (inParticle(x2,y,level1,level2) && x2<width) x2++;  // find end of scan-line                 
 			x2--;
 
 			fillLine(mask, x1-bounds.x, x2-bounds.x, y-bounds.y); // fill scan-line i mask
 			fillLine(ip,x1,x2,y); // fill scan-line in image
 
 
 			boolean inScanLine = false;
 			if (x1>0) x1--; if (x2<width-1) x2++;
 			for (int i=x1; i<=x2; i++) { // find scan-lines above this one
 				if (!inScanLine && y>0 && inParticle(i,y-1,level1,level2))
 				{push(i, y-1); inScanLine = true;}
 				else if (inScanLine && y>0 && !inParticle(i,y-1,level1,level2))
 					inScanLine = false;
 			}
 			inScanLine = false;
 			for (int i=x1; i<=x2; i++) { // find scan-lines below this one
 				if (!inScanLine && y<height-1 && inParticle(i,y+1,level1,level2))
 				{push(i, y+1); inScanLine = true;}
 				else if (inScanLine && y<height-1 && !inParticle(i,y+1,level1,level2))
 					inScanLine = false;
 			}
 		}        
 	}
 
 	final boolean inParticle(int x, int y, double level1, double level2) {
 		if (isFloat)
 			return ip.getPixelValue(x,y)>=level1 &&  ip.getPixelValue(x,y)<=level2;
 			else {
 				int v = ip.getPixel(x,y);
 				return v>=level1 && v<=level2;
 			}
 	}
 
 	final void push(int x, int y) {
 		stackSize++;
 		if (stackSize==maxStackSize) {
 			int[] newXStack = new int[maxStackSize*2];
 			int[] newYStack = new int[maxStackSize*2];
 			System.arraycopy(xstack, 0, newXStack, 0, maxStackSize);
 			System.arraycopy(ystack, 0, newYStack, 0, maxStackSize);
 			xstack = newXStack;
 			ystack = newYStack;
 			maxStackSize *= 2;
 		}
 		xstack[stackSize-1] = x;
 		ystack[stackSize-1] = y;
 	}
 
 	final int popx() {
 		if (stackSize==0)
 			return -1;
 		else
 			return xstack[stackSize-1];
 	}
 
 	final int popy() {
 		int value = ystack[stackSize-1];
 		stackSize--;
 		return value;
 	}
 
 	private void fillLine(ImageProcessor ip, int x1, int x2, int y) {
 		
 		//	System.out.println("fillline" + x1 + ":" + x2 + "linec: " + y); 
 
 		if (x1>x2) {
 			int t = x1;
 			x1=x2;
 			x2=t;
 		}	
 
 		if(x1 < 0)
 			x1 =0;
 		if( x1 > ip.getWidth()-1)
 			x1 = ip.getWidth()-1; 
 		if(x2 < 0)
 			x2 =0; 
 		if(x2 > ip.getWidth()-1)
 			x2 = ip.getWidth()-1; 
 		if(y < 0)
 			y =0;
 		if( y > ip.getHeight()-1)
 			y = ip.getHeight()-1;
 
 		for (int x=x1; x<=x2; x++){ 
 
 			undoPos.add(new int[]{x,y});
 //			undoPos.add(new Point(x, y));
 
 			ip.drawPixel(x, y);
 			pixelcount++;
 		}
 		
 	}
 
 	public long getPixelCount() {
 		return this.pixelcount;
 	}
 
 	public void setPixelCount(int i) {
 		this.pixelcount = 0;
 
 	}
 
 	/**
 	 * Restores the previous filled pixels from undoPos
 	 * @pre an area should have been filled
 	 */
 	public void unfill() {
 
 		int sub = undoStack.undo();	
 		//TODO: remove from thallus list
 
 		//if pixelcount < 0 substract undo area from old measurment
 		if(pixelcount-sub < 0){
 			ArrayList<Measurement> mList = MeasurementsFactory.getInstance().returnAll(); 
 
 			Measurement m =mList.get(mList.size()-1); 
 			m.addArea(-sub);
 			m.setCount(m.getCount()-1);
 
 			if(m.getArea() == 0){ 
 				try {
 					Genus.getInstance().getSpeciesFromID(m.getSpecies()).setResults(null);;
 					
 				} catch (NameNotFoundException e) {
 					//Nothing to be done here, cannot happen
 				}
 				mList.remove(mList.size()-1); 
 				
 			}
 
 		}else{ 
 			this.pixelcount -= sub;
 			this.ThallusCount--;
 		}
 
 	}
 
 	public void setPixelrate(double pixelrate) {
 		this.pixelrate = pixelrate;
 
 	}
 
 
 	/**
 	 * @return the linewidth
 	 */
 	public double getLinewidth() {
 		return linewidth;
 	}
 
 	/**
 	 * @param linewidth the linewidth to set
 	 */
 	public void setLinewidth(double linewidth) {
 		this.linewidth = linewidth;
 	}
 
 	/**
 	 * @return the thallusCount
 	 */
 	public int getThallusCount() {
 		return ThallusCount;
 	}
 
 	/**
 	 * @param thallusCount the thallusCount to set
 	 */
 	public void setThallusCount(int thallusCount) {
 		ThallusCount = thallusCount;
 	}
 
 	/**
 	 * @return the thalliList
 	 */
 	public ArrayList<String[]> getThalliList() {
 		return thalliList;
 	}
 
 	/**
 	 * @param thalliList the thalliList to set
 	 */
 	public void setThalliList(ArrayList<String[]> thalliList) {
 		this.thalliList = thalliList;
 	}
 
 }
