 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.beans;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.media.entity.simple.Broadcast;
 import org.atlasapi.media.entity.simple.ContentQueryResult;
 import org.atlasapi.media.entity.simple.Item;
 import org.atlasapi.media.entity.simple.Location;
 import org.atlasapi.media.entity.simple.Playlist;
 import org.joda.time.DateTime;
 
 import com.google.common.collect.Sets;
 
 /**
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class JaxbXmlTranslatorTest extends TestCase {
 
 	OutputStream stream = new ByteArrayOutputStream();
 
 	public void testCanOutputSimpleItemObjectModelAsXml() throws Exception {
 		Set<Object> graph = Sets.newHashSet();
 
 		Item item = new Item();
 		item.setTitle("Blue Peter");
 		item.setUri("http://www.bbc.co.uk/programmes/bluepeter");
 		item.setAliases(Sets.newHashSet("http://www.bbc.co.uk/p/bluepeter"));
 		Location location = new Location();
 		location.setUri("http://www.bbc.co.uk/bluepeter");
 		location.setEmbedCode("object><embed></embed></object>");
 		item.addLocation(location);
 		DateTime transmitted = new DateTime(1990, 1, 1, 1, 1, 1, 1);
 		item.addBroadcast(new Broadcast("channel", transmitted, transmitted.plusHours(1)));
 		ContentQueryResult result = new ContentQueryResult();
 		result.addItem(item);
 		graph.add(result);
 		
 		new JaxbXmlTranslator().writeTo(graph, stream);
 		
 		String output = stream.toString();
 		assertThat(output, containsString("<play:item>" +
 											"<uri>http://www.bbc.co.uk/programmes/bluepeter</uri>" +
 				                            "<aliases>" +
 				                              "<alias>http://www.bbc.co.uk/p/bluepeter</alias>" +
 				                            "</aliases>" +
				                            "<clips/>" +
 				                            "<play:containedIn/>" +
 				                            "<play:genres/>" +
 				                            "<play:tags/>" +
                                				"<title>Blue Peter</title>" +
                                				"<play:broadcasts>" +
                                					"<play:broadcast>" +
                                						"<broadcastDuration>3600</broadcastDuration>" +
                                						"<broadcastOn>channel</broadcastOn><transmissionEndTime>1990-01-01T02:01:01.001Z</transmissionEndTime>" +
                                						"<transmissionTime>1990-01-01T01:01:01.001Z</transmissionTime>" +
                                					"</play:broadcast>" +
                                				"</play:broadcasts>" +
 											"<play:locations>" +
 												"<play:location>" +
 												"<available>true</available>" +
 												"<embedCode><![CDATA[object><embed></embed></object>]]></embedCode>" +
 												"<uri>http://www.bbc.co.uk/bluepeter</uri>" +
 												"</play:location>" +
 											"</play:locations>" +
 										  "</play:item>"));
 	}
 
 	
 	public void testCanOutputSimpleListObjectModelAsXml() throws Exception {
 		Set<Object> graph = Sets.newHashSet();
 
 		Playlist list = new Playlist();
 		Item item = new Item();
 		item.setTitle("Blue Peter");
 		Location location = new Location();
 		location.setUri("http://www.bbc.co.uk/bluepeter");
 		item.addLocation(location);
 		list.addItem(item);
 		graph.add(list);
 		
 		new JaxbXmlTranslator().writeTo(graph, stream);
 		
 		assertThat(stream.toString(), containsString("<play:item>" +
 														"<aliases/>" +
														"<clips/>" +
 														"<play:containedIn/>" +
 														"<play:genres/>" +
 														"<play:tags/>" +
 	                                         			"<title>Blue Peter</title>" +
 	                                         			"<play:broadcasts/>" +
 														"<play:locations>" +
 															"<play:location>" +
 															"<available>true</available>" +
 															"<uri>http://www.bbc.co.uk/bluepeter</uri>" +
 															"</play:location>" +
 														  "</play:locations>" +
 													  "</play:item>"));
 	}
 }
