 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
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
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.stdcomponent;
 
 import java.util.*;
 
 import om.OmException;
 import om.OmFormatException;
 import om.stdquestion.QComponent;
 import om.stdquestion.QContent;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import util.xml.XML;
 import util.xml.XMLException;
 
 /**
  * Represents the document root.
  * <p>
  * This component is also where we dump shared Javascript and CSS that is
  * needed by other components.
  */
 public class RootComponent extends QComponent
 {
 	/** Layout grid size */
 	int[] aiRows,aiColumns;
 
 	@Override
 	protected void initChildren(Element eThis) throws OmException
 	{
 		// Overridden to exclude 'layout' tag (which we just processed),
 		// 'scoring' (which is handled as metadata by question engine),
 		// and 'define-component' (which is handled by question)
 		getQDocument().buildInsideExcept(this,eThis,new String[]{
 			"layout","scoring","define-component","title"});
 	}
 
 	@Override
 	protected void initSpecific(Element eThis) throws OmException
 	{
 		// Process layout tag
 		try
 		{
 			Element eLayout=XML.getChild(eThis,"layout");
 			Element[] aeRows=XML.getChildren(eLayout,"row");
 			aiRows=new int[aeRows.length];
 			for(int iRow=0;iRow<aeRows.length;iRow++)
 			{
 				aiRows[iRow]=Integer.parseInt(
 					XML.getRequiredAttribute(aeRows[iRow],"height"));
 			}
 			Element[] aeCols=XML.getChildren(eLayout,"column");
 			aiColumns=new int[aeCols.length];
 			for(int iCol=0;iCol<aeCols.length;iCol++)
 			{
 				aiColumns[iCol]=Integer.parseInt(
 					XML.getRequiredAttribute(aeCols[iCol],"width"));
 			}
 		}
 		catch(XMLException xe)
 		{
 			throw new OmFormatException("Invalid format in (or missing) <layout>",xe);
 		}
 		catch(NumberFormatException nfe)
 		{
 			throw new OmFormatException("height= or width= not a valid number in <layout>");
 		}
 		if(aiRows.length==0 || aiColumns.length==0)
 			throw new OmFormatException("<layout> must include at least one row and column");
 	}
 
 	@Override
 	public void produceOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
 	{
 		Document d=qc.getOutputDocument();
 
 		// Add the clear gif (shared between other components)
 		if(bInit && !bPlain)
 		{
 			qc.addResource("clear.gif","image/gif",getClassResource("clear.gif"));
 		}
 
 		Element rootDiv=d.createElement("div");
 		if(!bPlain)
 		{
 			rootDiv.setAttribute("class","om");
 			rootDiv.setAttribute("onkeypress","return checkEnter(event);");
 		}
 
 		qc.addInlineXHTML(rootDiv);
 
 		if(!bPlain) {
 			Element script=XML.createChild(rootDiv,"script");
 			script.setAttribute("type","text/javascript");
			script.setAttribute("src","%%RESOURCES%%/script.js?h=" + getQDocument().getJSHash());
 		}
 
 		// See if there's a component that wants to go there
 		Set<BoxThingy> sBoxes=new HashSet<BoxThingy>();
 		QComponent[] acChildren=getComponentChildren();
 		boolean[][] aabGridOccupancy=new boolean[aiColumns.length][aiRows.length];
 		for(int iChild=0;iChild<acChildren.length;iChild++)
 		{
 			// Only include displayable boxes
 			if((acChildren[iChild] instanceof BoxComponent) &&
 				acChildren[iChild].isDisplayed())
 			{
 				BoxComponent bc=(BoxComponent)acChildren[iChild];
 
 				int
 					iX=acChildren[iChild].getInteger("gridx"),
 					iY=acChildren[iChild].getInteger("gridy"),
 					iWidth=acChildren[iChild].getInteger("gridwidth"),
 					iHeight=acChildren[iChild].getInteger("gridheight");
 				if( iX<0 || iX+iWidth>aiColumns.length ||
 						iY<0 || iY+iHeight>aiRows.length )
 				{
 					throw new OmFormatException("Invalid gridx/y/width/height for box. " +
 						"This question has "+aiColumns.length+" columns and "+aiRows.length+
 						" rows; do not exceed those boundaries when positioning boxes on the grid.");
 				}
 
 				sBoxes.add(new BoxThingy(iX,iY,iWidth,iHeight,bc));
 				for(int iGridX=iX;iGridX<iX+iWidth;iGridX++)
 				{
 					for(int iGridY=iY;iGridY<iY+iHeight;iGridY++)
 					{
 						if(aabGridOccupancy[iGridX][iGridY])
 							throw new OmFormatException("Two displayed boxes are overlapping;" +
 									"question cannot be shown.");
 						aabGridOccupancy[iGridX][iGridY]=true;
 					}
 				}
 			}
 		}
 		// Add spacers for unoccupied squares
 		for(int iGridX=0;iGridX<aiColumns.length;iGridX++)
 		{
 			for(int iGridY=0;iGridY<aiRows.length;iGridY++)
 			{
 				if(!aabGridOccupancy[iGridX][iGridY])
 					sBoxes.add(new BoxThingy(iGridX,iGridY,1,1,null));
 			}
 		}
 
 		qc.setParent(rootDiv);
 		subdivideGrid(sBoxes,0,0,aiColumns.length,aiRows.length,qc, bPlain, bInit);
 		qc.unsetParent();
 
 		XML.createChild(rootDiv,"div").setAttribute("class","endform");
 	}
 
 	private void subdivideGrid(Set<BoxThingy> sBoxes,int iMinX,int iMinY,int iWidth, int iHeight, QContent qc, boolean bPlain, boolean bInit)
 		throws OmException
 	{
 		if(sBoxes.size()==1)
 		{
 			handleSingleBox(sBoxes.iterator().next(),qc, bPlain, bInit);
 			return;
 		}
 
 		int iCrossover;
 		crossoverloop: for(iCrossover=iMinY+1;iCrossover<iHeight;iCrossover++)
 		{
 			for(BoxThingy bt : sBoxes)
 			{
 				if(bt.iY<iCrossover && bt.iY+bt.iH>iCrossover)
 					continue crossoverloop;
 			}
 			break;
 		}
 
 		if(iCrossover < iHeight)
 		{
 			// OK now strip out everything above this crossover and send it to new method
 			Set<BoxThingy> sNew=new HashSet<BoxThingy>();
 			for(Iterator<BoxThingy> iBox=sBoxes.iterator();iBox.hasNext();)
 			{
 				BoxThingy bt=iBox.next();
 				if(bt.iY+bt.iH<=iCrossover)
 				{
 					sNew.add(bt);
 					iBox.remove();
 				}
 			}
 
 			// Make grid box
 			Element eFloat=qc.createElement("div");
 			if(!bPlain)
 			{
 				eFloat.setAttribute("class","gridgroup");
 				eFloat.setAttribute("style","width:"+getGridWidth(iMinX,iWidth)+"px; " +
 					"_height:"+getGridHeight(iMinY,iCrossover-iMinY)+"px; "+ // The _ at start makes it IE-only
 					"min-height:"+getGridHeight(iMinY,iCrossover-iMinY)+"px; " // Min-height is supported by other browsers
 					);
 			}
 			qc.addInlineXHTML(eFloat);
 			qc.setParent(eFloat);
 			// Handle block
 			subdivideGrid(sNew,iMinX,iMinY,iWidth,iCrossover-iMinY, qc, bPlain, bInit);
 			qc.unsetParent();
 
 			eFloat=qc.createElement("div");
 			if(!bPlain)
 			{
 				eFloat.setAttribute("class","gridgroup");
 				eFloat.setAttribute("style","width:"+getGridWidth(iMinX,iWidth)+"px; " +
 					"_height:"+getGridHeight(iCrossover,iHeight-iCrossover)+"px; "+ // The _ at start makes it IE-only
 					"min-height:"+getGridHeight(iCrossover,iHeight-iCrossover)+"px; " // Min-height is supported by other browsers
 					);
 			}
 			qc.addInlineXHTML(eFloat);
 			qc.setParent(eFloat);
 			// Do remaining vertical blocks
 			subdivideGrid(sBoxes,iMinX,iCrossover,iWidth,iHeight-iCrossover, qc, bPlain, bInit);
 			qc.unsetParent();
 
 			return;
 		}
 
 		crossoverloop: for(iCrossover=iMinX+1;iCrossover<iWidth;iCrossover++)
 		{
 			for(BoxThingy bt : sBoxes)
 			{
 				if(bt.iX<iCrossover && bt.iX+bt.iW>iCrossover)
 					continue crossoverloop;
 			}
 			break;
 		}
 
 		if(iCrossover<iWidth)
 		{
 			// OK now strip out everything before this crossover and send it to new method
 			Set<BoxThingy> sNew=new HashSet<BoxThingy>();
 			for(Iterator<BoxThingy> iBox=sBoxes.iterator();iBox.hasNext();)
 			{
 				BoxThingy bt=iBox.next();
 				if(bt.iX+bt.iW<=iCrossover)
 				{
 					sNew.add(bt);
 					iBox.remove();
 				}
 			}
 
 			// Make grid box
 			Element eFloat=qc.createElement("div");
 			if(!bPlain)
 			{
 				eFloat.setAttribute("class","gridgroup");
 				eFloat.setAttribute("style","width:"+getGridWidth(iMinX,iCrossover-iMinX)+"px; " +
 					"_height:"+getGridHeight(iMinY,iHeight)+"px; "+ // The _ at start makes it IE-only
 					"min-height:"+getGridHeight(iMinY,iHeight)+"px; " // Min-height is supported by other browsers
 					);
 			}
 			qc.addInlineXHTML(eFloat);
 			qc.setParent(eFloat);
 			// Handle block
 			subdivideGrid(sNew,iMinX,iMinY,iCrossover-iMinX,iHeight, qc, bPlain, bInit);
 			qc.unsetParent();
 
 			eFloat=qc.createElement("div");
 			if(!bPlain)
 			{
 				eFloat.setAttribute("class","gridgroup");
 				eFloat.setAttribute("style","width:"+getGridWidth(iCrossover,iWidth-iCrossover)+"px; " +
 					"_height:"+getGridHeight(iMinY,iHeight)+"px; "+ // The _ at start makes it IE-only
 					"min-height:"+getGridHeight(iMinY,iHeight)+"px; " // Min-height is supported by other browsers
 					);
 			}
 			qc.addInlineXHTML(eFloat);
 			qc.setParent(eFloat);
 			// Do remaining horizontal blocks
 			subdivideGrid(sBoxes,iCrossover,iMinY,iWidth-iCrossover,iHeight, qc, bPlain, bInit);
 			qc.unsetParent();
 
 			return;
 		}
 
 		throw new OmFormatException("Your box layout is too twisty for " +
 			"our system to convert to HTML. Try to make sure the layout can "+
 			"be subdivided into smaller rectangular blocks.");
 	}
 
 	private final static int MARGIN=8;
 
 	private int getGridWidth(int iX,int iW)
 	{
 		int iWidth=0;
 		for(int iGridX=iX;iGridX<iX+iW;iGridX++)
 		{
 			iWidth+=aiColumns[iGridX];
 		}
 		iWidth=(int)(iWidth*getQuestion().getZoom());
 
 		if(iX+iW < aiColumns.length)
 			iWidth+=iW*MARGIN;
 		else
 			iWidth+=(iW-1)*MARGIN;
 
 		return iWidth;
 	}
 
 	private int getGridHeight(int iY,int iH)
 	{
 		int iHeight=0;
 		for(int iGridY=iY;iGridY<iY+iH;iGridY++)
 		{
 			iHeight+=aiRows[iGridY];
 		}
 		iHeight=(int)(iHeight*getQuestion().getZoom());
 
 		if(iY+iH < aiRows.length)
 			iHeight+=iH*MARGIN;
 		else
 			iHeight+=(iH-1)*MARGIN;
 
 		return iHeight;
 	}
 
 	private final static int PADDING=4;
 
 	private void handleSingleBox(BoxThingy bt,QContent qc, boolean bPlain, boolean bInit) throws OmException
 	{
 		// Calculate width and height
 		int iWidth=getGridWidth(bt.iX,bt.iW),iHeight=getGridHeight(bt.iY,bt.iH);
 
 		// Subtract padding and right/bottom margin to get inner width and height
 		iWidth-=PADDING*2; iHeight-=PADDING*2;
 		boolean
 			bRightMargin=bt.iX+bt.iW < aiColumns.length,
 			bBottomMargin=bt.iY+bt.iH < aiRows.length;
 		if(bRightMargin) iWidth-=MARGIN;
 		if(bBottomMargin) iHeight-=MARGIN;
 		// No need to actually use CSS margin because it automatically goes in the
 		// top left corner of the box
 
 		// Make grid box
 		Element eFloat=qc.createElement("div");
 		if(!bPlain)
 		{
 			eFloat.setAttribute("class","gridcontainer");
 			eFloat.setAttribute("style","width:"+iWidth+"px; height:"+iHeight+"px;");
 		}
 		qc.addInlineXHTML(eFloat);
 
 		if(bt.bcIn!=null)
 		{
 			qc.setParent(eFloat);
 			bt.bcIn.produceOutput(qc,bInit,bPlain);
 			if(!bPlain)
 			{
 				eFloat.setAttribute("style",eFloat.getAttribute("style")+
 					" background-color:"+convertHash(bt.bcIn.getString(
 						BoxComponent.PROPERTY_BACKGROUND))+";");
 			}
 			qc.unsetParent();
 		}
 	}
 
 	static class BoxThingy
 	{
 		int iX,iY,iW,iH;
 		BoxComponent bcIn;
 		BoxThingy(int iX,int iY,int iW,int iH,BoxComponent bcIn)
 		{
 			this.iX=iX; this.iY=iY; this.iW=iW; this.iH=iH;
 			this.bcIn=bcIn;
 		}
 	}
 
 }
