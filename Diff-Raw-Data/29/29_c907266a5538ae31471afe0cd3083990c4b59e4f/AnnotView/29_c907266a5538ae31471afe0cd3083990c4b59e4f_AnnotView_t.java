 /*
   Copyright (C) 2001 Concord Consortium
 
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package graph;
 
 import waba.ui.*;
 import waba.fx.*;
 import waba.util.*;
 import waba.sys.*;
 import extra.ui.*;
 import extra.util.*;
 import org.concord.waba.extra.ui.*;
 
 public class AnnotView extends Container
 {
     public GraphView curView = null;
     public GraphViewLine lgView = null;
     public GraphViewBar bgView = null;
     
     public BarGraph bGraph;
     public LineGraph lGraph;
 
     Bin lgBins [] = new Bin [1];
     Object bgBins [] = new Object [1];
 
 	SplitAxis xaxis = null;
 	ColorAxis yaxis = null;
 
     public AnnotView(int w, int h, 
 					 SplitAxis xAx, ColorAxis yAx)
     {
 		setRect(0,0,w,h);
 
 		xaxis = xAx;
 		yaxis = yAx;
 
 		curView = lgView = new GraphViewLine(w, h, xaxis, yaxis);
 		bgView = new GraphViewBar(w, h);	
 		lgView.setPos(0,0);
 		bgView.setPos(0,0);
 
 		lGraph = lgView.lGraph;
 		bGraph = bgView.bGraph;
 	
 		add(curView);
     }
 
 	public void setAxis(SplitAxis xAx, ColorAxis yAx)
 	{	   
 		xaxis = xAx;
 		yaxis = yAx;
 		lgView.setAxis(xAx, yAx);
 	}
 
     float minX, maxX;
 
     public void setRange(float minX, float maxX, float minY, float maxY)
     {
 		yaxis.setRange(minY, maxY - minY);
 		
 		// This is a hack hmmm :)  I need to figure out which axis I'm setting
 		// This probably won't work anyhow because I need to change the min in
 		// the SplitAxis
 		xaxis.setRange(minX, maxX - minX);
 
 		bGraph.setYRange(minY, maxY - minY);
 	
 		this.minX = minX;
 		this.maxX = maxX;
 
     }
 
     public void setPos(int x, int y)
     {
 		setRect(x,y,width,height);
     }
 
     public void addBin(Bin curBin)
     {
 		lgBins[0] = curBin;
 
 		lgView.lGraph.addBin(curBin);
 		bGraph.addBar(0, curBin);
 		return;
     }
 
 	public void removeBin(Bin curBin)
 	{
 		//	  Hack to save memory on palm
 		bGraph.removeBar(curBin);		
 	}
 
 	public void addAnnot()
 	{
 		Annotation a = null;
 
 		if(lgBins[0] != null){
 			a = lGraph.addAnnot("" + lgView.curChar, lgBins[0].getCurX());
 
 			if(a != null){
 				lgView.curChar++;
 				repaint();
 			}
 		}
 	}
 
     public void reset()
     {
 		lgView.lGraph.reset();
 		bGraph.removeAllBars();
 		lgView.curChar = 'A';
 		lgBins[0] = null;
 
 		// repaint
 		repaint();
     }
 
     boolean barDown = false;
     Timer timer = null;
     public void onEvent(Event e)
     {
 		int i;
 		Annotation a = null;
 
 		if((e.type == ControlEvent.PRESSED)){
 			/*
 			if(false) {
 				if(bgView.selBar == null)
 					return;
 
 				if(bgView.selBar instanceof Annotation){
 					a = (Annotation)bgView.selBar;
 					lgView.lGraph.annots.del(lgView.lGraph.annots.find(a));
 					bgView.selBar = null;
 
 					bGraph.removeBar(a);
 					repaint();
 				}
 			}
 			*/
 		} else if(e.type == 1000 && e.target == bgView) {
 			// Bar down event 
 
 			timer = addTimer(750);
 			barDown = true;
 
 		} else if(e.type == 1001 && e.target == bgView) {
 			// Bar up event
 			barDown = false;
 			if(timer != null){
 				removeTimer(timer);
 				timer = null;
 			}
 		} else if(e.type == 1002 && e.target == lgView) {
 			// Annotation added event
 
 		} else if(e.type == 1003 && e.target == lgView) {
 			// Annotation moved event
 			// This should now be taken care of automagically :)
 
 		} else if(e.type == ControlEvent.TIMER && e.target == this){
 			if(timer != null){
 				removeTimer(timer);
 				timer = null;
 			}
 			if(barDown &&
 			   (bgView.selBar instanceof Annotation)){
				postEvent(new ControlEvent(1007, this));
 				//	       annotPage.showProp();
 			}
 			barDown = false;
 		}
 
     }
 
 	public void setViewType(int type)
 	{
 		if(type == 0){
 			if(curView instanceof GraphViewBar) return;
 			
 			// copy settings from lineView to barview
 			bGraph.yaxis.setDispMin(lgView.lGraph.yaxis.dispMin);
 			bGraph.yaxis.setScale(lgView.lGraph.yaxis.scale);
 		    
 			remove(curView);
 			curView = bgView;
 			add(curView);			
 		} else if(type == 1){
 			if(curView instanceof GraphViewLine) return;
 			
 			remove(curView);
 			curView = lgView;
 			add(curView);			
 		}
 
 			repaint();
 	}
 
 	public void setViewMode(char mode)
 	{
 		if(curView instanceof GraphViewLine){
 			lgView.mode = mode;
 		}
 	}
 
     public void update()
     {
 		curView.plot();
     }
     
     public void free()
     {
 		if(lgView != null) lgView.free();
 		if(bgView != null) bgView.free();
     }
 
 }
