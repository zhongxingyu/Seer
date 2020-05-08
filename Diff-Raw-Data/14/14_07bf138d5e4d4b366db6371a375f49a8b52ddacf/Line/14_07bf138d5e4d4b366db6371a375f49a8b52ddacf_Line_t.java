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
 package om.equation;
 
 import java.awt.Graphics2D;
 
 import org.w3c.dom.Element;
 
 /** Arranges child Items into a horizontal line */
 public class Line extends Item
 {
 	/** Alignment type */
 	private int iVAlign=ALIGN_BASELINE;
 
 	/** Position of baseline */
	private int baseline=0;
 
 	/** Names of valign= constants */
 	private final static String[] ALIGNMENT=
 	{
 		"top",
 		"bottom",
 		"middle",
 		"baseline"
 	};
 
 	/** Alignment constants match ALIGNMENT */
 	private final static int
 		ALIGN_TOP=0,ALIGN_BOTTOM=1,ALIGN_MIDDLE=2,ALIGN_BASELINE=3;
 
 	@Override
 	public int getBaseline()
 	{
		return baseline;
 	}
 
 	@Override
 	public void render(Graphics2D g2,int iX,int iY)
 	{
 		int iXPos=0;
 		Item iBefore=null;
 		for(int iChild=0;iChild<getChildren().length;iChild++)
 		{
 			Item i=getChildren()[iChild];
 			if(iBefore!=null)
 			{
 				if(i.useAdvanceAfter(iBefore))
 					iXPos+=iBefore.getAdvanceWidth();
 				else
 					iXPos+=iBefore.getWidth();
 				iXPos+=Math.max(iBefore.getRightMargin(),i.getLeftMargin());
 			}
 
 			int iYPos;
 			switch(iVAlign)
 			{
 			case ALIGN_TOP : iYPos=0; break;
 			case ALIGN_BOTTOM : iYPos=iHeight-i.getHeight(); break;
 			case ALIGN_MIDDLE : iYPos=(iHeight-i.getHeight())/2; break;
			case ALIGN_BASELINE : iYPos=baseline-i.getBaseline(); break;
 			default: throw new Error("Invalid alignment");
 			}
 
 			i.render(g2,iX+iXPos,iY+iYPos);
 			iBefore=i;
 		}
 	}
 
 	@Override
 	protected void internalInit(Element e) throws EquationFormatException
 	{
 		// Init XML
 		if(e.hasAttribute("valign"))
 		{
 			String sVAlign=e.getAttribute("valign");
 			for(iVAlign=0;iVAlign<ALIGNMENT.length;iVAlign++)
 			{
 				if(sVAlign.equals(ALIGNMENT[iVAlign]))	break;
 			}
 			if(iVAlign==ALIGNMENT.length)
 				throw new EquationFormatException(this,"valign attribute invalid: "+sVAlign);
 		}
 	}
 
 	@Override
 	protected void internalPrepare()
 	{
 		// Init metrics
 		iWidth=0;
 		int iMaxAboveBaseline=0;
 		int iMaxBelowBaseline=0;
 		int iMaxHeight=0;
 		Item iBefore=null;
 		for(int iChild=0;iChild<getChildren().length;iChild++)
 		{
 			Item i=getChildren()[iChild];
 			if(iBefore!=null)
 			{
 				if(i.useAdvanceAfter(iBefore))
 					iWidth+=iBefore.getAdvanceWidth();
 				else
 					iWidth+=iBefore.getWidth();
 				// DON'T add an overall gap between each item, this results in
 				// a horrible mess (there's separate items for e and x in e^x, for instance)
 				iWidth+=Math.max(iBefore.getRightMargin(),i.getLeftMargin());
 			}
 
 			int
 				iAbove=i.getBaseline(),
 				iBelow=i.getHeight()-iAbove;
 			iMaxAboveBaseline=Math.max(iAbove,iMaxAboveBaseline);
 			iMaxBelowBaseline=Math.max(iBelow,iMaxBelowBaseline);
 			iMaxHeight=Math.max(iMaxHeight,i.getHeight());
 
 			iBefore=i;
 		}
 		if(iBefore!=null) iWidth+=iBefore.getWidth();
 
 		if(iVAlign==ALIGN_BASELINE)
 		{
 			iHeight=iMaxAboveBaseline+iMaxBelowBaseline;
			baseline=iMaxAboveBaseline;
 		}
 		else
 		{
 			iHeight=iMaxHeight;
			baseline=iHeight;
 		}
 	}
 
 	/**
 	 * @param f ItemFactory to register this class with.
 	 */
 	public static void register(ItemFactory f)
 	{
 		f.addItemClass("int_line",new ItemCreator()
 			{	public Item newItem()	 {	return new Line();	}	});
 	}
 }
