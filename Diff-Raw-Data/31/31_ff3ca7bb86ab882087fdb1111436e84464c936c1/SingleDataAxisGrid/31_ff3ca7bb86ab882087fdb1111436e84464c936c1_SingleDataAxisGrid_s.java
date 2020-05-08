 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /**
  * Last modification information:
 * $Revision: 1.5 $
 * $Date: 2005-08-04 21:46:09 $
 * $Author: maven $
  *
  * Copyright 2004 The Concord Consortium
 */
 package org.concord.datagraph.ui;
 
 import java.text.Format;
 
 import org.concord.data.Unit;
 import org.concord.framework.data.DataDimension;
 import org.concord.graph.ui.SingleAxisGrid;
 
 /**
  * SingleDataAxisGrid
  *
  * Date created: Feb 15, 2005
  *
  * @author scott<p>
  * @author imoncada<p>
  *
  */
 public class SingleDataAxisGrid 
 	extends SingleAxisGrid 
 {
 	protected DataDimension unit = null;
 	protected DataDimension dataUnit = null;
 	protected double unitScale = 1.0;
 	
 	public SingleDataAxisGrid(int orientation)
 	{
 		super(orientation);
 	}
 	
 	public String getUnitString()
 	{
 		if (unit != null){
 			return unit.getDimension();			
 		}
 		
 		return null;
 	}
 		
 	/**
 	 * @return Returns the dataUnit.
 	 */
 	public DataDimension getDataUnit()
 	{
 		return dataUnit;
 	}
 	
 	/**
 	 * @param dataUnit The dataUnit to set.
 	 */
 	public void setDataUnit(DataDimension dataUnit)
 	{
 		this.dataUnit = dataUnit;
 
 		calculateUnitScale();
 	}
 	
 	/**
 	 * @return Returns the unit.
 	 */
 	public DataDimension getUnit()
 	{
 		return unit;
 	}
 	
 	public void setUnit(DataDimension unit)
 	{
 		this.unit = unit;
 
 		calculateUnitScale();
 	}
 
 	/**
 	 * 
 	 */
 	protected void calculateUnitScale()
 	{
 		//calculate the scale according to seconds
 		if (unit instanceof Unit){
 			Unit objUnit = (Unit)unit;
 			Unit objUnit2;
 			
 			if (dataUnit == null){
 				//Hack. Default data unit is seconds
 				if (Unit.isUnitCompatible(objUnit.code, Unit.UNIT_CODE_S)){
 					dataUnit = Unit.getUnit(Unit.UNIT_CODE_S);
 					objUnit2 = (Unit)dataUnit;
 				}
 				else{
 					return;
 				}
 			}
 			else if (dataUnit instanceof Unit){
 				objUnit2 = (Unit)dataUnit;
 			}
 			else{
 				return;
 			}
 			
 			if (Unit.isUnitCompatible(objUnit, objUnit2)){
 				unitScale = Unit.unitConvert(objUnit, 1, objUnit2);
 				
 				//System.out.println("scale unit "+unitScale);
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	protected String getAxisLabelToDraw()
 	{
 		String unitStr = getUnitString();
 		if (unitStr != null){ 
 			return getAxisLabel() + " (" + unitStr + ")";
 		}
 		
 		return getAxisLabel();
 	}
 	
 	/**
 	 * FIXME: In order to show different units in the grid, I need to override 
 	 * this three methods: getBestInterval, getGridLabel, createFormatObj.
 	 * There could be a better way to implement this, but for now, this works.
 	 */
 	
 	/**
 	 * @see org.concord.graph.ui.SingleAxisGrid#getBestInterval(double)
 	 */
 	public double getBestInterval(double newInterval)
 	{
 		double bestInt = super.getBestInterval(newInterval/unitScale);
 		return bestInt * unitScale;
 	}
 	
 	/**
 	 * @see org.concord.graph.ui.SingleAxisGrid#getGridLabel(double, double)
 	 */
 	protected String getGridLabel(double val, double threshold)
 	{
 		return super.getGridLabel(val / unitScale, threshold / unitScale);
 	}
 	
 	/**
 	 * @see org.concord.graph.ui.SingleAxisGrid#createFormatObj(double, double, double)
 	 */
 	protected Format createFormatObj(double interval, double startTick,
 			double endTick)
 	{
 		return super.createFormatObj(interval / unitScale, startTick / unitScale, endTick / unitScale);
 	}
 }
