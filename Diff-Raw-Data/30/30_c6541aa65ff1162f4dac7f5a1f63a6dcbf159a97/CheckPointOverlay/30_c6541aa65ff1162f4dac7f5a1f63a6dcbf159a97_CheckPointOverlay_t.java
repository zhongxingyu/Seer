 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
 */
 package se.team05.overlay;
 
 import java.util.ArrayList;
 
 import android.graphics.drawable.Drawable;
 
 import com.google.android.maps.ItemizedOverlay;
 
 /**
  * The CheckPointOverLay class contains a list of checkpoints and a default
  * marker that will show up on map.
  * 
  * @author Patrik Thituson
  * @version 1.0
  */
 public class CheckPointOverlay extends ItemizedOverlay<CheckPoint>
 {
 	public interface Callbacks
 	{
 		public void onCheckPointTap(CheckPoint cp);
 	}
 
 	private ArrayList<CheckPoint> checkPointList = new ArrayList<CheckPoint>();
 	private int selectedCheckpointIndex;
 	private Callbacks callback;
 
 	/**
 	 * The Constructor
 	 * 
 	 * @param defaultMarker
 	 * @param context
 	 */
 	public CheckPointOverlay(Drawable defaultMarker, Callbacks context)
 	{
 		super(boundCenterBottom(defaultMarker));
 		this.callback = context;
 		populate();
 	}
 
 	/**
 	 * Unused Method
 	 */
 	@Override
 	protected CheckPoint createItem(int i)
 	{
 		return checkPointList.get(i);
 	}
 
 	/**
 	 * @return the size of the list
 	 */
 	@Override
 	public int size()
 	{
 		return checkPointList.size();
 	}
 
 	/**
 	 * Adds a checkpoint to the checkpointlist and marks is as the selected
 	 * checkpoint
 	 * 
 	 * @param checkPoint
 	 */
 	public void addCheckPoint(CheckPoint checkPoint)
 	{
 		checkPointList.add(checkPoint);
 		setLastFocusedIndex(-1);
 		selectedCheckpointIndex = checkPointList.size() - 1;
 		populate();
 	}
 	
 	public void setCheckPoints(ArrayList<CheckPoint> listOfCheckPoints)
 	{
 		for(CheckPoint checkPoint : listOfCheckPoints)
 		{
 			addCheckPoint(checkPoint);
 		}
 	}
 
 	/**
 	 * The onTap method that uses callback to send the checkpoint where the
 	 * dialog initiates, it also marks the checkpoint as the selected checkpoint
 	 * wich is used for deleting a checkpoint
 	 */
 	@Override
 	protected boolean onTap(int index)
 	{
 		selectedCheckpointIndex = index;
 		CheckPoint checkPoint = checkPointList.get(index);
 		callback.onCheckPointTap(checkPoint);
 		return true;
 	}
 
 	/**
 	 * 
 	 * @return checkPointList a list with the checkpoints
 	 */
 	public ArrayList<CheckPoint> getOverlays()
 	{
 		return checkPointList;
 	}
 
 	/**
 	 * Deletes the selected checkpoint from the checkpointlist
 	 */
 	public void deleteCheckPoint()
 	{
 		checkPointList.remove(selectedCheckpointIndex);
 		setLastFocusedIndex(-1);
 		populate();
 	}
	
 
 
 }
