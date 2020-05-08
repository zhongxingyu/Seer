 //    This file is part of SimpleHistogramPanel.
 //
 //    SimpleHistogramPanel is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU Lesser General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    SimpleHistogramPanel is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU Lesser General Public License for more details.
 //
 //    You should have received a copy of the GNU Lesser General Public License
 //    along with SimpleHistogramPanel.  If not, see <http://www.gnu.org/licenses/>.
 
 package histogram;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.swing.JPanel;
 
 public class SimpleHistogramPanel extends JPanel{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private ArrayList<HistogramBar> data;
 	private HistogramBar maxHistBar;
 	private boolean barNames = false;
 	private int[] bgLines = {223,223,223};
 	private int histWidth;
 	private int histHeight;
 	private int defaultStartX = 15;
 	private int defaultStartY = 5;
 	private int startX = defaultStartX;
 	private int startY = defaultStartY;
 	private String histName;
 	Font defaultFont = new Font("Arial", Font.BOLD, 13);
 	private FontMetrics fm;
 	private ActionHighlightHBar ahhb;
 	private ActionBarClickListener abcl;
 	private Graphics gr;
 	private ArrayList<HistogramBar> barsToMark;
 	private int histMax = 0;
 	private int histMin = 0;
 	private int minBarHeight = 1;
 	private int minBarWidth = 1;
 	private int maxBarWidth = 100;	
 	private boolean linearize = false;
 
 	public SimpleHistogramPanel(){
 		super();
 		ahhb = new ActionHighlightHBar(this);
 		abcl = new ActionBarClickListener(this);
 		this.addMouseMotionListener(ahhb);
 		this.addMouseListener(abcl);
 		barsToMark = new ArrayList<HistogramBar>();
 	}
 
 	public SimpleHistogramPanel(int x,int y){
 		super();
 		this.setPreferredSize(new Dimension(x,y));
 		ahhb = new ActionHighlightHBar(this);
 		abcl = new ActionBarClickListener(this);
 		this.addMouseMotionListener(ahhb);
 		this.addMouseListener(abcl);
 		barsToMark = new ArrayList<HistogramBar>();
 	}
 
 	public SimpleHistogramPanel(String name){
 		histName  = name;
 		ahhb = new ActionHighlightHBar(this);
 		abcl = new ActionBarClickListener(this);
 		this.addMouseMotionListener(ahhb);
 		this.addMouseListener(abcl);
 		barsToMark = new ArrayList<HistogramBar>();
 	}
 
 	public SimpleHistogramPanel(String name,int x,int y){
 		this.setPreferredSize(new Dimension(x,y));
 		histName  = name;
 		ahhb = new ActionHighlightHBar(this);
 		abcl = new ActionBarClickListener(this);
 		this.addMouseMotionListener(ahhb);
 		this.addMouseListener(abcl);
 		barsToMark = new ArrayList<HistogramBar>();
 	}
 
 	public void enableLinearize(boolean b){
 		linearize = b;
 	}
 
 	public void setName(String name){
 		histName = name;
 	}
 
 	public String getName(){
 		return histName;
 	}
 
 	public void setBarNames(boolean b){
 		barNames = b;
 	}
 
 	private HistogramBar getMax(){
 		Iterator<HistogramBar> iterator = data.iterator();
 		if(data.size() > 0){
 			HistogramBar max = data.get(0);
 
 			while(iterator.hasNext()){
 				HistogramBar e = iterator.next();
 				if(e.getValue() > max.getValue()){
 					max = e;
 				}
 			}
 			return max;
 		}else{
 			return null;
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private HistogramBar getMin(){
 		Iterator<HistogramBar> iterator = data.iterator();
 		if(data.size() > 0){
 			HistogramBar min = data.get(0);
 
 			while(iterator.hasNext()){
 				HistogramBar e = iterator.next();
 				if(e.getValue() < min.getValue()){
 					min = e;
 				}
 			}
 			return min;
 		}else{
 			return null;
 		}
 	}
 
 	public void setData(ArrayList<HistogramBar> d){
 		data = d;
 		if(data.size() > 0){
 			maxHistBar = getMax();
 			ahhb.setData(data);
 			abcl.setData(data);
 		}
 	}	
 
 	public ArrayList<HistogramBar> getData(){
 		return data;
 	}
 	private int getTotalEvents(){
 		return data.size();
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 
 		super.paintComponent(g);
 		gr = g;
 		int sizeX = this.getWidth();
 		int sizeY = this.getHeight();
 		setFontMetrics(getFontMetrics(defaultFont));
 		
 		// Paint histogram name
 		if(histName != null){
 			int txtWidth = getFontMetrics().stringWidth(histName);
 			int txtHeight = getFontMetrics().getHeight();
 			int x = (int)(Math.ceil(sizeX/2 - txtWidth/2));
 			int y = txtHeight;
 			startY = defaultStartY + y;
 			gr.drawString(histName, x, y);
 		}
 
 		histWidth = sizeX-startX-10;
 		int maxP = (int)(Math.ceil(Math.log10(maxHistBar.getValue())));
 		startX = defaultStartX + getFontMetrics().stringWidth(String.format("%.1e", Math.pow(10, maxP)));
 		
 		if(barNames){
 			histHeight = sizeY-startY*2 - 20;
 		}else{
 			histHeight = sizeY-startY*2;
 		}
 
 		drawOutsideBorders();
 		if(data != null){
 			if(data.size() > 0){
 				drawYMarks();
 				drawBars();
 				drawYBarMarks();
 			}
 		}
 	}
 
 
 	private void drawOutsideBorders(){
 		gr.drawRect(startX, startY, histWidth, histHeight);		
 	}
 	
 	private void drawYBarMarks(){
 		// Draw y-axis marks on selected bars
 		if(barsToMark.size() > 0){
 			Iterator<HistogramBar> iterator = barsToMark.iterator();
 			while(iterator.hasNext()){
 				HistogramBar b = iterator.next();
 				gr.drawLine(startX,b.getY(),b.getX(),b.getY());
 
 				// Draw numbers
 				gr.drawString(String.valueOf(b.getValue()), startX-20,b.getY());
 			}
 		}
 	}
 
 	private void drawYMarks(){
 		if(data.size() > 0 && maxHistBar != null){
 			int yMax = maxHistBar.getValue();
 			int maxP = 0;
 			double yGap = 0;
 
 			if(yMax > 0){
 				setHistMin(0);
 				// Draw y-axis elements
 				if(linearize){
 					maxP = (int)(Math.ceil(Math.log10(yMax)));
 					setHistMax((int)(Math.pow(10, maxP)));
 					yGap = histHeight/Math.max(1, maxP);
 					// Origin
 					drawYNumbersMark(0,0);
 					// Middle
 					drawYNumbersMark((int)(Math.pow(10, maxP/2)),yGap*(int)(maxP/2));
 					// Max
 					drawYNumbersMark((int)(Math.pow(10, maxP)),yGap*maxP);	
 					
 					// draw other marks but without numbers
 					for(int up = 0;up < maxP;up++){
 						drawYWithoutNumbersMark(yGap*up);
 					}
 				}else{
 					setHistMax((int)(yMax*1.1));
 					// Max
 					drawYNumbersMark(getHistMax(),histHeight);
 					// Min
 					drawYNumbersMark(getHistMin(),0);
 					// Middle
 					drawYNumbersMark((int)((getHistMax()-getHistMin())/2),(int)(histHeight/2));				
 				}
 			}
 		}
 	}
 
 	private void drawYNumbersMark(int mark,double gap){
 		int xYMark = startX;
 		int yYMark = (int)(startY+histHeight-gap+7);
 
 		// Draw numbers
 		setFontMetrics(getFontMetrics(defaultFont));
 		String str;
 		if(mark >= 10000)
 			str = String.format("%.1e", (double)mark);
 		else
 			str = Integer.toString(mark);
 		int txtWidth = getFontMetrics().stringWidth(str);
 		//int txtHeight = getFontMetrics().getHeight();
 		
 		int x = (int)(Math.ceil(xYMark - txtWidth - defaultStartX));
 		int y = yYMark;
 		if(barsToMark.size() > 0){
 			gr.setColor(new Color(190,190,190));
 		}else{
 			gr.setColor(Color.BLACK);			
 		}
 		gr.drawString(String.valueOf(str), x,y);
 
 		// Draw small line at y-axis
 		gr.setColor(Color.black);
 		gr.drawLine(xYMark, yYMark-5, xYMark-2, yYMark-5);
 
 		// Draw gray line from y-axis to histogram end line
 		gr.setColor(new Color(bgLines[0],bgLines[1],bgLines[2]));
 		gr.drawLine(startX, yYMark-5,startX+histWidth,yYMark-5);
 		gr.setColor(Color.black);
 	}	
 	
 	private void drawYWithoutNumbersMark(double gap){
 		int xYMark = startX;
 		int yYMark = (int)(startY+histHeight-gap+7);
 
 		// Draw small line at y-axis
 		gr.setColor(Color.black);
 		gr.drawLine(xYMark, yYMark-5, xYMark-2, yYMark-5);
 
 		// Draw gray line from y-axis to histogram end line
 		gr.setColor(new Color(bgLines[0],bgLines[1],bgLines[2]));
 		gr.drawLine(startX, yYMark-5,startX+histWidth,yYMark-5);
 		gr.setColor(Color.black);
 	}
 
 	private void drawXMarks(int x,int y){
 		gr.drawLine(x, y, x, y+5);
 		gr.setColor(new Color(bgLines[0],bgLines[1],bgLines[2]));
 		gr.drawLine(x,y,x,y-histHeight);
 		gr.setColor(Color.black);
 
 	}
 
 	public void drawMark(HistogramBar b){
 		if(!barsToMark.contains(b))
 			barsToMark.add(b);
 		this.repaint();
 	}
 
 	public void dontDrawMark(HistogramBar b){
 		while(barsToMark.contains(b)){
 			barsToMark.remove(b);
 			this.repaint();
 		}
 	}
 	
 	public void barClicked(HistogramBar b){
 		// Called when a bar receives a mouse click
 		System.out.println("Bar click: "+b.getName());
 	}
 
 	private void drawBars(){
 		int totalEvents = getTotalEvents();
 		int xGap = (int)(Math.ceil(histWidth*0.005));
 		int startXPos = startX + 3*xGap;
 		HistogramBar histMax = getMax();
 		if(histMax != null){
 			int yMax = histMax.getValue();
 			if(yMax > 0){
 				int barWidth = (int)(Math.ceil((histWidth - xGap*totalEvents)/ totalEvents));
 				if(barWidth < getMinBarWidth())
 					barWidth = getMinBarWidth();
 				if(barWidth > getMaxBarWidth())
 					barWidth = getMaxBarWidth();
 				int barHeight = 0;
 				HistogramBar e;
 				Iterator<HistogramBar> iterator = data.iterator();
 
 				// Desenha barras
 				for(int i=0;iterator.hasNext();i++){
 					e = iterator.next();
 					if(linearize){
 						barHeight = (int)Math.ceil(histHeight*Math.log10(e.getValue())/Math.log10(getHistMax()));	
 					}else{
 						barHeight = (int)Math.ceil(histHeight*e.getValue()/(float)(getHistMax()));	
 					}
 					if(barHeight < getMinBarHeight())
 						barHeight = getMinBarHeight();
 					drawBar(e,startXPos+i*(barWidth+xGap),startY+histHeight-barHeight,barWidth,barHeight);
 				}
 			}
 		}
 	}
 
 	private void drawBar(HistogramBar t,int x,int y,int width,int height){
 		Color c = t.getColor();
 		Color barBorder = new Color(0,0,0);
 
 		drawXMarks((int)(Math.ceil(x + width/2)), y + height);
 		gr.setColor(c);
 		gr.fillRect(x, y, width, height);
 		gr.setColor(barBorder);
 		gr.drawRect(x, y, width, height);
 		if(barNames){
 			setFontMetrics(getFontMetrics(defaultFont));
 			int txtWidth = getFontMetrics().stringWidth(t.getName());
 			int txtHeight = getFontMetrics().getHeight();
 			int xName = (int)(Math.ceil(x + width/2 - txtWidth/2));
 			int yName = y+height + txtHeight;	
 			gr.drawString(t.getName(), xName, yName);
 		}
 		t.setX(x);
 		t.setY(y);
 		t.setWidth(width);
 		t.setHeight(height);
 	}
 
 	private FontMetrics getFontMetrics() {
 		return fm;
 	}
 
 	private void setFontMetrics(FontMetrics fm) {
 		this.fm = fm;
 	}
 
 	private int getHistMax() {
 		return histMax;
 	}
 
 	private void setHistMax(int histMax) {
 		this.histMax = histMax;
 	}
 
 	private int getHistMin() {
 		return histMin;
 	}
 
 	private void setHistMin(int histMin) {
 		this.histMin = histMin;
 	}
 
 	public int getMaxBarWidth() {
 		return maxBarWidth;
 	}
 
 	public void setMaxBarWidth(int maxBarWidth) {
 		this.maxBarWidth = maxBarWidth;
 	}
 
 	public int getMinBarWidth() {
 		return minBarWidth;
 	}
 
 	public void setMinBarWidth(int minBarWidth) {
 		this.minBarWidth = minBarWidth;
 	}
 
 	private int getMinBarHeight() {
 		return minBarHeight;
 	}
 
 	@SuppressWarnings("unused")
 	private void setMinBarHeight(int minBarHeight) {
 		this.minBarHeight = minBarHeight;
 	}
 	
 	public ActionHighlightHBar getMouseListener(){
 		return ahhb;
 	}
 	
 	public void clearAllBars(){
		if(data != null)
			data.clear();
 	}
 
 }
