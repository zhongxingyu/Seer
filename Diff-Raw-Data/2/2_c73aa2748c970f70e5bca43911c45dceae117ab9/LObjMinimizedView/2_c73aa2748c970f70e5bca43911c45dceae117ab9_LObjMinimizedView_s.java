 package org.concord.LabBook;
 
 import waba.ui.*;
 import waba.util.*;
 import waba.fx.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.probware.probs.*;
 import org.concord.waba.extra.probware.*;
 import extra.ui.*;
 import extra.util.*;
 
 public class LObjMinimizedView extends LabObjectView
 {
 public int rColor = 0;
 public int gColor = 0;
 public int bColor = 255;
 
 	LabObject minObj;
     public LObjMinimizedView(LabObject obj)
     {
 		super(null, obj, null);
 		minObj = obj;
     }
     
 	public void layout(boolean sDone)
     {
 	}
 
 	public void onPaint(Graphics g)
 	{
 		if(minObj != null && minObj.getName() != null){
 			g.setColor(rColor,gColor,bColor);
 			g.drawText(minObj.getName(), 0, 0);
 			FontMetrics fm = getFontMetrics(MainWindow.defaultFont);
 			int lineY = fm.getHeight();
 			g.setColor(rColor,gColor,bColor);
			g.drawLine(0,lineY, width, lineY);
 		}
 	}
 
 	public void close()
 	{
 	}
 
 	public int getPreferredWidth()
 	{
 		if(minObj != null && minObj.getName() != null){
 			FontMetrics fm = getFontMetrics(MainWindow.defaultFont);
 			return fm.getTextWidth(minObj.getName());
 		}
 
 		return -1;
 	}
 
 	public int getPreferredHeight()
 	{
 		if(minObj != null && minObj.getName() != null){
 			FontMetrics fm = getFontMetrics(MainWindow.defaultFont);
 			return fm.getHeight()+2;
 		}
 
 		return -1;
 	}
 }
