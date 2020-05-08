 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // OldViewXML.java
 // Since: 2010/04/22
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
 * For reading view.xml files used utgb 1.3.x.
  * 
  * @author leo
  * 
  */
 public class OldViewXML {
 
 	public static class Prop {
 		public String key;
 		public String value;
 	}
 
 	public static class TrackGroup {
 
 		public String className;
 
 		public GroupProperties groupProperties = new GroupProperties();
 		public List<Prop> property = new ArrayList<Prop>();
 		public List<Track> track = new ArrayList<Track>();
 
 		public String getProperty(String key) {
 			for (Prop p : groupProperties.property) {
 				if (p.key != null && p.key.equals(key)) {
 					return p.value;
 				}
 			}
 			return null;
 		}
 
 	}
 
 	public static class GroupProperties {
 		public List<Prop> property = new ArrayList<Prop>();
 		public TrackWindow trackWindow = new TrackWindow();
 	}
 
 	public static class TrackWindow {
 		public int start;
 		public int end;
 		public int width;
 	}
 
 	public static class Track {
 
 		public String name;
 		public String className;
 		public int height;
 		public boolean pack;
 
 		public List<Prop> property = new ArrayList<Prop>();
 	}
 
 	public TrackGroup trackGroup;
 
 	public TrackView toTrackView() {
 		TrackView v = new TrackView();
 		v.trackGroup.class_ = trackGroup.className;
 		v.trackGroup.id = 1;
 
 		// coordinate
 		TrackView.Coordinate c = new TrackView.Coordinate();
 		c.chr = trackGroup.getProperty("target");
 
 		c.start = trackGroup.groupProperties.trackWindow.start;
 		c.end = trackGroup.groupProperties.trackWindow.end;
 		c.ref = String.format("%s:%s", trackGroup.getProperty("species"), trackGroup.getProperty("revision"));
 
 		c.pixelWidth = trackGroup.groupProperties.trackWindow.width;
 
 		v.trackGroup.coordinate = c;
 
 		//
 		for (Prop p : trackGroup.property) {
 			v.trackGroup.property.put(p.key, p.value);
 		}
 
 		for (Track each : trackGroup.track) {
 			TrackView.Track t = new TrackView.Track();
 			t.name = each.name;
 			t.height = each.height;
 			t.pack = each.pack;
 			t.class_ = each.className;
 			for (Prop p : each.property) {
 				t.property.put(p.key, p.value);
 			}
 
 			v.track.add(t);
 		}
 
 		return v;
 	}
 }
