 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package controllers;
 
 import java.util.List;
 
 import org.petalslink.dsb.cxf.CXFHelper;
 import org.petalslink.dsb.jaxws.JAXWSHelper;
 
 import utils.Locator;
 import eu.playproject.governance.api.EventGovernance;
 import eu.playproject.governance.api.bean.Topic;
 
 /**
  * @author chamerling
  * 
  */
 public class TopicsController extends PlayController {
 	
 	public static void topics() {
 		EventGovernance client = null;
 		try {
 			client = Locator.getEventGovernance(getNode());
 		} catch (Exception e) {
 			handleException(null, e);
 		}
 
 		List<Topic> topics = null;
 		try {
 			topics = client.getTopics();
 		} catch (Exception e) {
 			handleException("Unable to get topics", e);
 		}
 		render(topics);
 	}
 	
	public static void topic(String name, String ns, String prefix) {
 		render(name, ns, prefix);
 	}
 
 }
