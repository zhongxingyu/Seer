 /*
  * Kurento Android MSControl: MSControl implementation for Android.
  * Copyright (C) 2011  Tikal Technologies
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 3
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.kurento.kas.mscontrol.join;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.kurento.commons.media.format.MediaSpec;
import com.kurento.commons.media.format.Payload;
 import com.kurento.commons.media.format.SessionSpec;
 import com.kurento.commons.media.format.enums.MediaType;
 import com.kurento.commons.media.format.enums.Mode;
 import com.kurento.commons.mscontrol.join.JoinableContainer;
 import com.kurento.commons.mscontrol.join.JoinableStream;
 
 public abstract class JoinableStreamBase extends JoinableImpl implements JoinableStream {
 
 	private JoinableContainer container;
 	private StreamType type;
 	
 	protected JoinableStreamBase(JoinableContainer container, StreamType type) {
 		this.container = container;
 		this.type = type;
 	}
 	
 	@Override
 	public JoinableContainer getContainer() {
 		return this.container;
 	}
 
 	@Override
 	public StreamType getType() {
 		return this.type;
 	}
 
 	protected static Map<MediaType, Mode> getModesOfMediaTypes(
 			SessionSpec session) {
 		Map<MediaType, Mode> map = new HashMap<MediaType, Mode>();
 		for (MediaSpec m : session.getMediaSpecs()) {
 			Set<MediaType> mediaTypes = m.getTypes();
 			if (mediaTypes.size() != 1)
 				continue;
 			for (MediaType t : mediaTypes) {
 				map.put(t, m.getMode());
 				break;
 			}
 		}
 		return map;
 	}
 
 	protected static SessionSpec filterMediaByType(SessionSpec session,
 			MediaType type) {
 		List<MediaSpec> mediaList = new ArrayList<MediaSpec>();
		MediaSpec newM = new MediaSpec();

 		for (MediaSpec m : session.getMediaSpecs()) {
 			Set<MediaType> mediaTypes = m.getTypes();
 			if (mediaTypes.size() != 1)
 				continue;
 			for (MediaType t : mediaTypes) {
				if (t == type) {
					for (Payload p : m.getPayloads()) {
						newM.setMode(m.getMode());
						newM.setTransport(m.getTransport());
						newM.setTypes(m.getTypes());
						newM.addPayload(p);
						mediaList.add(newM);
						break;
					}
				}
 				break;
 			}
 		}
 
 		return new SessionSpec(mediaList, "-");
 	}
 
 }
