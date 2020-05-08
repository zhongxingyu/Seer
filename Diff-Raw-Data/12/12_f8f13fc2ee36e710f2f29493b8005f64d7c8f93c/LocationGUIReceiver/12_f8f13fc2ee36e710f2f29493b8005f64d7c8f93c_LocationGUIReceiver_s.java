 /*
  * Copyright (c) 2014. See AUTHORS file.
  *
  * This file is part of SleepFighter.
  *
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  */
 package se.toxbee.sleepfighter.gps.gui;
 
 import android.support.v4.app.FragmentActivity;
 
 /**
 * LocationGUIReceiver receives and communicates with a.json {@link LocationGUIProvider}.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Jan, 15, 2014
  */
 public interface LocationGUIReceiver extends OnMapClick, OnMarkerDragListener {
 	/**
	 * Returns a.json FragmentActivity to add view to, etc.
 	 *
 	 * @return the activity.
 	 */
 	public FragmentActivity getActivity();
 
 	/* --------------------------------
 	 * Zoom & Location related.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Computes the padding to use when moving camera to polygon.
 	 *
 	 * @return the padding.
 	 */
 	public int computeMoveCameraPadding();
 
 	/**
 	 * Moves the user to its current location.
 	 */
 	public void moveToCurrentLocation();
 
 	/**
 	 * Returns the zoom factor to use.
 	 *
 	 * @return the zoom factor.
 	 */
 	public float getZoomFactor();
 
 	/* --------------------------------
 	 * Polygon related.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Returns the fill-color to use.
 	 *
 	 * @return the color value.
 	 */
 	public int getPolygonFillColor();
 
 	/**
 	 * Returns the stroke-color to use.
 	 *
 	 * @return the color value.
 	 */
 	public int getPolygonStrokeColor();
 
 	/**
 	 * Returns true if n(points) >= 3.
 	 *
 	 * @return true if >= 3 points.
 	 */
 	public boolean satisfiesPolygon();
 
 	/* --------------------------------
 	 * Map Events.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Invoked when the map is ready.
 	 */
 	public void onMapReady();
 }
