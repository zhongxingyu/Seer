 package com.pace.base.data;
 
 import java.util.List;
 
 import com.pace.base.PafException;
 import com.pace.base.mdb.PafDimTree.LevelGenType;
 
 /**
  * Interface that provides access to data cache; used in custom functions
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 public interface IPafDataCache {
 
 	public int getAxisIndex(String msrDim);
 	public double getBaseCellValue(String[] coords) throws PafException;
     public double getCellValue(Intersection intersection) throws PafException;
 	public double getCumMbrCount(Intersection cellIs);
 	public double getCumMbrCount(Intersection cellIs, String cumDim);
 	public double getCumMbrCount(Intersection cellIs, String cumDim, int level);
 	public double getCumMbrCount(final Intersection sourceIs, final String cumDim, final LevelGenType levelGenType, final int levelGen, final String yearMbr);
 	public double getCumTotal(Intersection cellIs) throws PafException;
 	public double getCumTotal(Intersection cellIs, String cumDim) throws PafException;
 	public double getCumTotal(Intersection cellIs, String cumDim, int offset) throws PafException;
 	public double getCumTotal(Intersection dataIs, String timeDim, int offset, LevelGenType levelGenType, int levelGen, String yearMbr) throws PafException;
 	public int getMeasureAxis();    
 	public String getMeasureDim();    
 	public double getNextCellValue(Intersection dataIs) throws PafException;    
 	public double getNextCellValue(Intersection dataIs, String offsetDim, int offset) throws PafException;    
 	public double getNextCellValue(Intersection dataIs, String offsetDim, int offset, boolean bWrap) throws PafException;    
 	public Intersection getNextIntersection(Intersection cellIs);
 	public Intersection getNextIntersection(Intersection cellIs, String offsetDim, int offset);
 	public Intersection getNextIntersection(Intersection cellIs, String offsetDim, int offset, boolean bWrap);
 	public double getPrevCellValue(Intersection cellIs) throws PafException;
 	public double getPrevCellValue(Intersection cellIs, String offsetDim, int offset) throws PafException;
 	public double getPrevCellValue(Intersection dataIs, String offsetDim, int offset, boolean bWrap) throws PafException;    
 	public String getTimeDim();
 	public String getTimeHorizonDim();
 	public String getVersionDim();
 	public String getYearDim();
 	public boolean isMember(String dimension, String member);
 	public void setCellValue(Intersection target, double allocValue) throws PafException;
 	public Intersection shiftIntersection(Intersection cellIs);
 	public Intersection shiftIntersection(Intersection cellIs, final String offsetDim, final int offset);
 	public Intersection shiftIntersection(Intersection cellIs, final String offsetDim, final int offset, final boolean bCrossYears);
 	public Intersection shiftIntersection(Intersection cellIs, final String offsetDim, final int offset, final boolean bCrossYears, final boolean bWrap);
 	public boolean hasValidTimeHorizonCoord(Intersection cellIs);
 	public Intersection getFirstDescendantIs(Intersection cellIs, final String dim, final int level);
 	public Intersection getLastDescendantIs(Intersection cellIs, final String dim, final int level);
 	public List<Intersection> getDescIntersectionsAtLevel(Intersection cellIs, String dim, int level);
 	public Intersection getFirstFloorIs(final Intersection cellIs, final String dim);
 	public Intersection getFirstFloorIs(final Intersection dataIs, final String timeDim, final LevelGenType levelGenType, final int levelGen, final String yearMbr) throws PafException;
 	public Intersection getLastFloorIs(Intersection cellIs, String dim);
 	public boolean isBaseIntersection(Intersection sourceIs);
 
 }
