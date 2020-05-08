 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package net.rptools.maptool.model;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Path<T extends AbstractPoint> {
 	private final List<T> cellList = new LinkedList<T>();
 	private final List<T> waypointList = new LinkedList<T>();
 
 	public void addPathCell(T point) {
 		cellList.add(point);
 	}
 
 	public void addAllPathCells(List<T> cells) {
 		cellList.addAll(cells);
 	}
 
 	public List<T> getCellPath() {
 		return Collections.unmodifiableList(cellList);
 	}
 
 	public void replaceLastPoint(T point) {
 		cellList.remove(cellList.size() - 1);
 		cellList.add(point);
 	}
 
 	public void addWayPoint(T point) {
 		waypointList.add(point);
 	}
 
 	public boolean isWaypoint(T point) {
 		return waypointList.contains(point);
 	}
 
 	public T getLastWaypoint() {
 		if (waypointList.isEmpty())
 			return null;
 		return waypointList.get(waypointList.size() - 1);
 	}
 
	/**
	 * Returns the last waypoint if there is one, or the last T point if there is not.
	 * 
	 * @return a non-<code>null</code> location
	 */
	public T getLastJunctionPoint() {
		T temp = getLastWaypoint();
		return temp != null ? temp : cellList.get(cellList.size() - 1);
	}

 	public Path<T> derive(int cellOffsetX, int cellOffsetY) {
 		Path<T> path = new Path<T>();
 		for (T cp : cellList) {
 			T np = (T) cp.clone();
 			np.x -= cellOffsetX;
 			np.y -= cellOffsetY;
 			path.addPathCell(np);
 		}
 		for (T cp : waypointList) {
 			T np = (T) cp.clone();
 			np.x -= cellOffsetX;
 			np.y -= cellOffsetY;
 			path.addWayPoint(np);
 		}
 		return path;
 	}
 }
