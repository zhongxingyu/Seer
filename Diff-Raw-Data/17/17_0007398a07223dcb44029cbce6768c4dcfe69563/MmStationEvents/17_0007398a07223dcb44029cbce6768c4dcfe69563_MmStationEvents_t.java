 package mmStationEvents;
 
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdesktop.swingx.mapviewer.GeoPosition;
 import org.json.*;
 
 import javax.swing.*;
 
 
 
 public class MmStationEvents {
 
 	private String type,stationName;
 	private double latitude, longitude;
 	int stationIndex;
 	
 	
     private ArrayList<MmMarkerEvent> markerEvents;
     private ArrayList<MmAudioEvent> audioEvents;
     private ArrayList<MmVideoEvent> videoEvents;
     private ArrayList<MmPanoramaEvent> panoramaEvents;
     private ArrayList<MmImageEvent> imageEvents;
     private ArrayList<MmModelEvent> modelEvents;
     private ArrayList<MmMessageEvent> messageEvents;
     
     private ArrayList<Integer> collectItems;
     
     private ArrayList<JLabel> labels;
     
     private ArrayList<String> otherActions; 
     
     int currentStationIndex;
     
     int prev_index=0,current_index;
     
     String urlLinkText,saveFilePath;
     
     int currentLabelIndex=0;
     
     int gpsRadius;
     
     boolean isSwingPoint;
     
     int nextStationIndex;
     
     int swingIndex = 1;
     
     int stationLastIndex;
     
     String eventsText = null;
     
     MmStationEvents nextStation;
     
     JSONObject stationEvent,attributes,actions;
     
     JSONArray action = new JSONArray();
 	
 	public MmStationEvents()
 	{
 		this.labels = new ArrayList<JLabel>();
 		
 			
 		imageEvents = new ArrayList<MmImageEvent>();
 		markerEvents = new ArrayList<MmMarkerEvent>();
 		audioEvents = new ArrayList<MmAudioEvent>();
 		videoEvents = new ArrayList<MmVideoEvent>();
 		panoramaEvents = new ArrayList<MmPanoramaEvent>();
 		modelEvents = new ArrayList<MmModelEvent>();
 		messageEvents = new ArrayList<MmMessageEvent>();
 		collectItems = new ArrayList<Integer>();
 		
 		otherActions = new ArrayList<String>();
 	}
 	
 	public MmStationEvents(String name)
 	{
 		this.stationName = name; 
 		
 		
 	}
 	
 	public void setType(String type)
 	{
 		this.type = type;
 	}
 	
 	
 	public void setStationName(String name)
 	{
 		this.stationName = name;
 		
 		if(this.stationName.length()==8)
 		{
 			stationLastIndex = Integer.parseInt(stationName.substring(stationName.length()-1));
 			
 		}
 		
 						
 		if(this.stationName.length()==20)
 		{
 			stationLastIndex = Integer.parseInt(stationName.substring(stationName.length()-1));
 		}
 		
 		if(this.stationName.length()>20)
 		{
 			stationLastIndex = Integer.parseInt(stationName.substring(stationName.length()-2,stationName.length()-1));
 		}
 	}
 	
 	public void setGPSRadius(int radius)
 	{
 		gpsRadius = radius;
 	}
 	
 	public int getGPSRadius()
 	{
 		return gpsRadius;
 	}
 	
 	public void setLatLon(double lat,double lon)
 	{
 		this.latitude = lat;
 		this.longitude = lon;
 	}
 	
 	public void setCurrentStationIndex(int index)
 	{
 		currentStationIndex = index;
 	}
 	
 	public int getCurrentStationIndex()
 	{
 		return currentStationIndex;
 	}
 	
 	
 	public void setStationType(boolean swingPoint)
 	{
 		isSwingPoint = swingPoint;
 	}
 	
 	
 	public boolean getStationType()
 	{
 		return isSwingPoint;
 	}
 	
 	
 	public void setStationIndex(int index)
 	{
 		stationIndex = index+1;
 	}
 	
 	public int getStationIndex()
 	{
 		return stationIndex;
 	}
 	
 	
 	//gets the index assicated with station name
 	public int getIndexStationNameIndex()
 	{
 		return stationLastIndex;
 	}
 	
 	/*
 	 * gets the stationIndex of swing point
 	 */
 	public int getSwingPointStationIndex()
 	{
 		if(this.isSwingPoint)
 		{
 			String swingPointName = this.getStationName();
 			
 			swingPointName = swingPointName.substring(7, 8);
 			
 			return Integer.parseInt(swingPointName);
 		}
 		
 		return -1;
 	}
 	
 	public void initializeLabels()
 	{
 		
 		labels.add(addDefaultLabel());
 		labels.add(addDefaultLabel());
 		labels.add(addDefaultLabel());
 		labels.add(addDefaultLabel());
 		
 	}
 	
 	public JLabel addDefaultLabel()
 	{
 		JLabel lb = new JLabel("");
 		lb.setName("label");
 		return lb;
 	}
 	
 	public void setLabels(ArrayList<JLabel> label)
 	{
 	    for(int i=0;i<label.size();i++)
 	    {
 	    	this.labels.add(label.get(i));
 	    }
 			
 	}
 	
 	public void setLabelsText(String text,String path)
 	{
 			    
 		JLabel lb = new JLabel(text);
 		lb.setName(path);
 		
 		int count =-1;
 		
 		for(int i=0;i<labels.size();i++)
 		{
 			if(labels.get(i).getName().equals("label"))
 			{
 				count = i;
 				break;
 			}
 		}
 		
 		if(count<4)
 		{
 			labels.get(count).setText(text);
 			labels.get(count).setName(path);
 		}
 		else
 		{	
 		   labels.add(lb);
 		}   
 		
 			
 	}
 	
 	
 	public String getType()
 	{
 		return this.type;
 	}
 	
 	public String getStationName()
 	{
 		return this.stationName;
 	}
 	
 	public double getLatitude()
 	{
 		return this.latitude;
 	}
 	
 	public double getLongitude()
 	{
 		return this.longitude;
 	}
 	
 		
 	public ArrayList<JLabel> getLabels()
 	{
 		return this.labels;
 	}
 	
 	public String getStationsEvents()
 	{
 		String eventsText = "";
 		
 		if(!labels.isEmpty())
 		{	
 		   if(!labels.get(0).getName().equals("label"))
 		      eventsText ="<html>";
 		
 		   for(int i=0;i<labels.size();i++)
 		   {
 			  if(!labels.get(i).getName().equals("label"))
 			  {
 				eventsText += labels.get(i).getText()+"<br/>";
 				
 			  }
 		   }
 		
 		  if(eventsText.contains("<html>"))
 		      eventsText +="</html>";
 		}
 		
 		return eventsText;
 	}
 	
 	/*
 	 * gets number of events associated with station
 	 */
 	public int getNumberOfEvents()
 	{
 		int count = 0;
 		for(int i=0;i<labels.size();i++)
 		{
 			if(!labels.get(i).getName().equals("label"))
 			{
 				count+=1;
 			}
 		}
 		
 		return count;
 	}
 	
 	public boolean isGPSPointPresent(GeoPosition point)
 	{
 		if((getLatitude()==point.getLatitude()) && (getLongitude()==point.getLongitude()))
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	
 	
 	public void addMouseEvents()
 	{
 		for(int i=0;i<labels.size();i++)
 		{
 			final int index = i;
 			labels.get(i).addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseClicked(MouseEvent event) {
 					// TODO Auto-generated method stub
 					
 					if(labels.get(prev_index).isOpaque())
 					{
 						labels.get(prev_index).setOpaque(true);
 					}
 					
 					current_index=index;
 					labels.get(index).setOpaque(true);
 					labels.get(index).setBackground(Color.lightGray);
 					prev_index = current_index;
 					
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent arg0) {
 					// TODO Auto-generated method stub
 					
 				}
 
 				@Override
 				public void mouseExited(MouseEvent arg0) {
 					// TODO Auto-generated method stub
 					
 				}
 
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					// TODO Auto-generated method stub
 					
 				}
 
 				@Override
 				public void mouseReleased(MouseEvent arg0) {
 					// TODO Auto-generated method stub
 					
 				}
 				
 			});
 		}
 	}
 	
 	public JSONArray getAction()
 	{
 		return action;
 	}
 	
 	public void addEventAction(String str)
 	{
 		action.put(str);
 		
 		
 	}
 	
 	public void addOtherActions(String string)
 	{
 		otherActions.add(string);
 	}
 	
 	public void writeJson(String saveFilePath,MmStationEvents next_station)
 	{
 		
 		nextStation = next_station;
 		
 		stationIndex = -1;
 				
 		this.saveFilePath = saveFilePath;
 		stationEvent = new JSONObject();
 		attributes = new JSONObject();
 		actions = new JSONObject();
 		//JOptionPane.showMessageDialog(null, "Entered station1");
 		
 		//JOptionPane.showMessageDialog(null, this.stationName+"  "+nextStationIndex);
 		
		gpsRadius = 10;
		
 		try {
 			   stationEvent.put("name", this.stationName);
 			   stationEvent.put("type", "region");
 			  
 			   attributes.put("latitude", latitude);
 			   attributes.put("longitude", longitude);
 			   attributes.put("radius", gpsRadius);
 			   attributes.put("shouldDisplayOnCompass", false);
 			   attributes.put("swingPoint", isSwingPoint);
 			   
 			  
 			   stationEvent.put("attributes",attributes);
 			   
 			   if(this.isSwingPoint)
 			   {
 				   System.out.println("substring "+this.stationName.substring(this.stationName.length()-1));
 				   swingIndex = this.getIndexStationNameIndex();
 				   action.put("disableStation"+Integer.toString(this.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(swingIndex)+"Compass");
 				   if(nextStation!=null && !nextStation.isSwingPoint)
 				   {
 					   action.put("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				   }
 				   
 				   if(nextStation!=null && nextStation.isSwingPoint)
 				   {
 					   action.put("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				   }
 				   action.put("wayPointAudio");
 			   }   
 			   		   
 			   if(!this.isSwingPoint)
 			   {	   
 			       action.put("disableStation"+Integer.toString(this.getIndexStationNameIndex())+"Compass");
 			       
 			       
 			       if(nextStation!=null && !nextStation.isSwingPoint && this.getNumberOfEvents()==0)
 				   {
 					   action.put("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				   }
 			       
 			       if(nextStation!=null && nextStation.isSwingPoint && this.getNumberOfEvents()==0)
 				   {
 					   action.put("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				   }
 			       
 			       action.put("stationEnterAudio");
 			   }    
 			   
 						 			   
 			   JLabel lb = null; 
 			   
 			   int i=0;
 			   
 			   stationIndex = this.getIndexStationNameIndex();
 			    
 			   
 			   while(currentLabelIndex<labels.size())
 			   {
 				       
 				   System.out.println("current index "+currentLabelIndex+"  "+labels.get(currentLabelIndex).getText());
 				   if(labels.get(currentLabelIndex).getText().isEmpty() || labels.get(currentLabelIndex).getName().equals("label"))
 				   {
 					      System.out.println("label index "+labels.get(currentLabelIndex).getText());
 					      System.out.println("Data");
 					      
 				    	  break;
 				   }
 				   
 				   
 				   if(currentLabelIndex>=labels.size())
 				   {
 					      System.out.println("Data");
 					      
 				    	  break;
 				   }
 				   
 				   
 				  if(currentLabelIndex<labels.size())
 				  {	  
 				    lb = labels.get(currentLabelIndex);
 				   
 			      if(isImage(lb.getText()))
 			      { 	
 			    	  
 			    	  System.out.println("Entered data");
 			    	  MmImageEvent imageEvent = new MmImageEvent();
 			    	  
 			    	  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"_image");
 					  action.put(imageEvent.getEventName());
 					  String[] attrs = lb.getText().split(":");
 					  
 					   if(attrs.length<=1)
 					   {	   
 						   imageEvent.setImageFile(attrs[0]);
 						   imageEvent.setCollectItem(false);
 					   }  
 					   
 					   if(attrs.length==2)
 					   { 	   
 						   imageEvent.setImageFile(attrs[0]);
 					       imageEvent.setCollectItem(true);
 					       collectItems.add(new Integer(collectItems.size()+1));
 					   }
 					   
 					   /*if(currentLabelIndex==labels.size()-1)
 					   {
 					    	if(!nextStation.getStationType())
 						    	imageEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 						    else
 						    	imageEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 					    	imageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 					    	imageEvent.addActions("Done");
 					   }*/
 					  				   
 					  imageEvent.setSourcePath(lb.getName());
 					  imageEvent.setDestinationPath(this.saveFilePath);
 					  imageEvent.makeJSONObject();
 					  imageEvent.JSONActions();
 					  imageEvents.add(imageEvent);
 			    	  currentLabelIndex++;
 			    	  		    	  
 			    	  System.out.println("currentLabelIndex "+currentLabelIndex+"  "+labels.size());
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    		     if(!createImageEvent(imageEvent))
 			    		     { 	 
 			    		    	  
 			    	    	     break;
 			    		     }    
 			    	  }   
 			    	  else
 			    	  {	  
 			    		  if(!nextStation.getStationType())
 						    	imageEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 						    else
 						    	imageEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    		  imageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 			    		  imageEvent.addActions("Done");
 			    		 break;
 			    	  } 	 
 			    	  
 			    	  //String str = createImageEvent(imageEvent); 
 			          //action.put(str);
 			          //currentLabelIndex++;
 			            
 			      } 
 			      
 			      if((lb.getText().contains("Panorama")))
 			      {
 			    	  System.out.println("index in panorama  "+i+"  "+currentLabelIndex+" "+labels.get(currentLabelIndex).getText());
 			    	  MmPanoramaEvent panoramaEvent = new MmPanoramaEvent();
 			    	  panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_panorama");
 			    	  action.put(panoramaEvent.getEventName());
 			    	  String[] attrs = lb.getText().split(":");
 			    	  System.out.println("panorama  attrs "+attrs.length);
 			    	  
 					   if(attrs.length<=2)
 					   {	   
 						   panoramaEvent.setPanoramaFile(attrs[0]);
 					       panoramaEvent.setCollectItem(false);
 					   }    
 					   if(attrs.length==3)
 					   { 	   
 						   panoramaEvent.setPanoramaFile(attrs[0]);
 						   panoramaEvent.setCollectItem(true);
 						   collectItems.add(new Integer(collectItems.size()+1));
 					   }
 					   panoramaEvent.setSourcePath(lb.getName());
 			    	  
 			    	  panoramaEvent.setDestinationPath(this.saveFilePath);
 			    	  panoramaEvent.makeJSONObject();
 			    	  panoramaEvent.JSONActions();
 			    	  panoramaEvents.add(panoramaEvent);
 			    	  currentLabelIndex++;
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	  
 				          if(!createPanoramaEvent(panoramaEvent))
 				          {
 				        	  //panoramaEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 				        	  break;
 				          }	  
 			    	  } 
 			    	  else
 			    	  {	  
 			    		  if(!nextStation.getStationType())
 						    	panoramaEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 						    else
 						    	panoramaEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    		  panoramaEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 			    		 break;
 			    	  } 
 			      }
 			      
 			      lb = labels.get(currentLabelIndex);
 			      System.out.println("index in audio  "+i+"  "+currentLabelIndex+" "+lb.getText());
 			      //JOptionPane.showMessageDialog(null, lb.getName());
 			      
 			   
 			      if(lb.getText().contains(".mp3")||lb.getText().contains(".m4a")||lb.getText().contains(".aiff"))
 			      {	  
 			    	  System.out.println("index in audio  "+i+"  "+currentLabelIndex+" "+labels.get(currentLabelIndex).getText());
 			    	  MmAudioEvent audioEvent = new MmAudioEvent();
 			    	  audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_audio");
 			    	  action.put(audioEvent.getEventName());
 					  audioEvent.setAudioFileName(lb.getText());
 					  String[] attrs = lb.getText().split(":");
 					   if(attrs.length<=1)
 					   {	   
 						   audioEvent.setAudioFileName(attrs[0]);
 					       audioEvent.setCollectItem(false);
 					   }    
 					   if(attrs.length==2)
 					   { 	   
 						   audioEvent.setAudioFileName(attrs[0]);
 					       audioEvent.setCollectItem(true);
 					       collectItems.add(new Integer(collectItems.size()+1));
 					   }
 					   
 					  audioEvent.setSourcePath(lb.getName());
 					  audioEvent.setDestinationPath(this.saveFilePath);
 					  audioEvent.makeJSONObject();
 					  audioEvent.JSONActions();
 			    	  currentLabelIndex++;
 			    	  audioEvents.add(audioEvent);
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	  
 				            if(!createAudioEvent(audioEvent))
 				            {
 				            	   break;
 				            }  	   
 			    	  } 
 			    	  else
 			    	  {	  
 			    		  if(!nextStation.getStationType())
 						    	audioEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 						    else
 						    	audioEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    		  audioEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));  
 			    		 break;
 			    	  }  
 			    	  
 			      }   
 			      
 			      lb = labels.get(currentLabelIndex);
 			      
 			      if(lb.getText().contains(".m4v"))
 			      { 	  
 			    	  System.out.println("index in loop2 "+i+"  "+currentLabelIndex);
 					  MmVideoEvent videoEvent = new MmVideoEvent();
 					  videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_video");
 					  action.put(videoEvent.getEventName());
 					  String[] attrs = lb.getText().split(":");
 					   if(attrs.length<=1)
 					   {	   
 						   videoEvent.setVideoFileName(attrs[0]);
 					       videoEvent.setCollectItem(false);
 					   }    
 					   if(attrs.length==2)
 					   { 	   
 						   videoEvent.setVideoFileName(attrs[0]);
 					       videoEvent.setCollectItem(true);
 					       collectItems.add(new Integer(collectItems.size()+1));
 					   }
 					  videoEvent.setSourcePath(lb.getName());
 					  videoEvent.setDestinationPath(this.saveFilePath);
 					  videoEvent.makeJSONObject();
 					  videoEvent.JSONActions();
 			    	  currentLabelIndex++;
 			    	  videoEvents.add(videoEvent);
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    	     if(!createVideoEvent(videoEvent))
 			    	     {	 
 			    	    	 videoEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 			    	    	 break;
 			    	     }  	 
 			    	  }   
 			    	  else
 			    	  {	  
 			    		  if(!nextStation.getStationType())
 						    	videoEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 						    else
 						    	videoEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    		  videoEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 			    		 break;
 			    	  }  
 			    	  
 			      }	 
 			      
 			      
 			      lb = labels.get(currentLabelIndex);
 			      
 			      if(lb.getText().contains(".obj")||lb.getText().contains(".osg"))
 			      { 	  
 					 MmModelEvent modelEvent = new MmModelEvent();
 					 if(modelEvents.size()==0)
 						  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_model");
 					  else
 						  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(modelEvents.size())+"_model"); 
 					 String[] attrs = lb.getText().split(":");
 					   if(attrs.length<=1)
 					   {	   
 						   modelEvent.setModelFile(attrs[0]);
 					       modelEvent.setCollectItem(false);
 					   }    
 					   if(attrs.length==2)
 					   { 	   
 						   modelEvent.setModelFile(attrs[0]);
 					       modelEvent.setCollectItem(true);
 					       collectItems.add(new Integer(collectItems.size()+1));
 					   }
 					  action.put(modelEvent.getEventName());
 					  modelEvent.addActions(modelEvent.getEventName());
 					  modelEvent.setSourcePath(lb.getName());
 					  modelEvent.setDestinationPath(this.saveFilePath);
 					  modelEvent.JSONActions();
 					  modelEvent.makeJSONObject();
 			    	  modelEvents.add(modelEvent);
 			    	  currentLabelIndex++;
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    	     if(!createModelEvent(modelEvent));
 			    	     {
 			    	    	 modelEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 			    	    	 break;
 			    	     }
 			    	  } 
 			    	  else
 			    	  {	  
 			    		  if(!nextStation.getStationType())
 						    	modelEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 						    else
 						    	modelEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    		  modelEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 			    		 break;
 			    	  }
 			    	  
 			     }
 			      
 			      lb = labels.get(currentLabelIndex);
 			         		      
                   
 			      
 			      if(lb.getText().contains(".txt"))
 			      { 	  
 			    	  System.out.println("index in loop2 "+i+"  "+currentLabelIndex);
 					  MmMessageEvent messageEvent = new MmMessageEvent();
 					  messageEvent.setEventName("station"+Integer.toString(stationIndex)+"_message");
 					  action.put(messageEvent.getEventName());
 					  String[] attrs = lb.getText().split(":");
 					   if(attrs.length<=1)
 					   {	   
 						   messageEvent.setMessageFile(attrs[0]);
 					       messageEvent.setCollectItem(false);
 					   }    
 					   if(attrs.length==2)
 					   { 	   
 						  messageEvent.setMessageFile(attrs[0]);
 					      messageEvent.setCollectItem(true);
 					      collectItems.add(new Integer(collectItems.size()+1));
 					   }
 					   
 					   messageEvent.setSourcePath(lb.getName());
 					   messageEvent.setDestinationPath(this.saveFilePath);
 					   messageEvent.makeJSONObject();
 					   messageEvent.JSONActions();
 			    	   messageEvents.add(messageEvent);
 			    	   currentLabelIndex++;
 			    	   if(currentLabelIndex<labels.size())
 			    	   {	
 			    	     if(!createMessageEvent(messageEvent))
 			    	     {
 			    	    	 messageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 			    	    	 break;
 			    	     }	 
 			    	   }   
 			    	   else
 			    	   {	  
 			    		   if(!nextStation.getStationType())
 						    	messageEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 						    else
 						    	messageEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    		   messageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex)); 
 			    		 break;
 			    	   } 
 			    	  
 			      }
 			      
 			      
 				  } 
 			      i=currentLabelIndex;
 			     
 			      if(i>=labels.size())
 			      {	  
 			    	  System.out.println("index in loop "+i+"  "+currentLabelIndex);
 			    	  break;
 			      }	  
 			   }   
 			  	
 			   
 			   actions.put("enter-region", action);
 			   stationEvent.put("actions", actions);
 			   System.out.println("data");
 			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public boolean createImageEvent(MmImageEvent imageEvent)
 	{
 		
 		if(currentLabelIndex<labels.size())
 		{
 					
 		    JLabel lb = labels.get(currentLabelIndex);
 		   
 		    if(labels.get(currentLabelIndex).getText().isEmpty())
 		    {
 		    	 
 		    	 		    	 		    	 
 		    	 if(nextStation!=null)
 		    	 {	 
 		    	     if(!nextStation.getStationType())
 				          imageEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				     else
 				          imageEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 		    	     imageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 		    	 }
 				 else
 				 {	
 						imageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 						imageEvent.addActions("Done");   
 			  	 }
 				    
 				    imageEvent.JSONActions();
 		    	 
 			     return false;
 		    } 
 		
 		    
 
 		if(isImage(lb.getText()))
 	    {
 		      
 			  System.out.println("label in image event"+lb.getText());  
 			  
 			  MmImageEvent nextImageEvent = new MmImageEvent();
 			  	  
 			  if(imageEvents.isEmpty())
 				  nextImageEvent.setEventName("station"+Integer.toString(stationIndex)+"image");
 			  else
 				  nextImageEvent.setEventName("enableStation"+Integer.toString(stationIndex)+"SwingPoint"+Integer.toString(swingIndex)+"Compass");
 			  
 			        String[] attrs = lb.getText().split(":");
 			        
 			        if(attrs.length<=1)
 			        {	
 			           	nextImageEvent.setImageFile(attrs[0]);
 			        	nextImageEvent.setCollectItem(false);
 			           		
 			        }   
 			        
 			        if(attrs.length==2)
 			        {	
 			        	nextImageEvent.setImageType(attrs[0]);
 			        	nextImageEvent.setCollectItem(true);
 			        	collectItems.add(new Integer(collectItems.size()+1));
 			        	
 			        }    
 			        	
 			           
 			        nextImageEvent.setSourcePath(lb.getName());
 			        nextImageEvent.setDestinationPath(this.saveFilePath);
 			        
 				    
 				    currentLabelIndex++;
 				    if(currentLabelIndex<labels.size())
 				    {	
 				    	//JOptionPane.showMessageDialog(null, "Entered image event");
 				    	nextImageEvent.makeJSONObject();
 				    	
 //				    	 if(!nextStation.getStationType())
 //						    	imageEvent.addActions("EnableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 //						    else
 //						    	imageEvent.addActions("EnableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");     
 				    	
 				        nextImageEvent.JSONActions();
 					    imageEvents.add(nextImageEvent);
 					    imageEvent.addActions(nextImageEvent.getEventName());	
 				        
 					    if(!createImageEvent(nextImageEvent))
 					    	return false;
 				       
 				    }  
 				    
 				    
 				    
 				    
 				   
 	        }		    
 				  
 		    if(lb.getText().contains("Panorama"))
 			{
 						   
 					MmPanoramaEvent panoramaEvent = new MmPanoramaEvent();
 					if(panoramaEvents.size()==0)
 						panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_panorama");
 					else
 						panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_panorama");
 						imageEvent.addActions(panoramaEvent.getEventName());
 						imageEvent.makeJSONObject();
 						panoramaEvents.add(panoramaEvent);
 						String[] attrs = lb.getText().split(":");
 						if(attrs.length<=2)
 						{	   
 						     panoramaEvent.setPanoramaFile(attrs[0]);
 						     panoramaEvent.setCollectItem(false);
 						}
 						
 						if(attrs.length==3)
 						{ 	   
 							panoramaEvent.setPanoramaFile(attrs[0]);
 							panoramaEvent.setCollectItem(true);
 							collectItems.add(new Integer(collectItems.size()+1));
 						}	   
 						   
 						panoramaEvent.makeJSONObject();
 						currentLabelIndex++;
 						
 						if(!createPanoramaEvent(panoramaEvent))
 							return false;
 					    
 					   }
 		    
 					   
 					   if(lb.getText().contains(".mp3")||lb.getText().contains(".m4a")||lb.getText().contains(".aiff"))
 				       {
 						   
 						   MmAudioEvent audioEvent = new MmAudioEvent();
 						   if(audioEvents.size()==0)
 							   audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_audio");
 						   else
 							   audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_audio");
 						   		   
 						   imageEvent.addActions(audioEvent.getEventName());
 						   imageEvent.JSONActions();
 						   audioEvents.add(audioEvent);
 						   
 						   String[] attrs = lb.getText().split(":");
 						   if(attrs.length<=1)
 						   {	   
 							   audioEvent.setAudioFileName(attrs[0]);
 						       audioEvent.setCollectItem(false);
 						   }    
 						   if(attrs.length==2)
 						   { 	   
 							   audioEvent.setAudioFileName(attrs[0]);
 							   audioEvent.setCollectItem(true);
 							   collectItems.add(new Integer(collectItems.size()+1));
 						   }
 						   audioEvent.setSourcePath(lb.getName());
 						   audioEvent.setDestinationPath(this.saveFilePath);
 					       audioEvent.makeJSONObject();
 					       currentLabelIndex++;
 					       
 					       if(!createAudioEvent(audioEvent))
 					    	   return false;
 					       
 					   }
 					   
 					   if(lb.getText().contains(".m4v"))
 					   { 	  
 							  MmVideoEvent videoEvent = new MmVideoEvent();
 							  if(videoEvents.size()==0)
 							     videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_video");
 							  else
 								  videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_video");
 							   String[] attrs = lb.getText().split(":");
 							   if(attrs.length<=1)
 							   {	   
 								   videoEvent.setVideoFileName(attrs[0]);
 							       videoEvent.setCollectItem(false);
 							   }    
 							   if(attrs.length==2)
 							   { 	   
 								   videoEvent.setVideoFileName(attrs[0]);
 								   videoEvent.setCollectItem(true);
 								   collectItems.add(new Integer(collectItems.size()+1));
 							   }
 							  
 							  videoEvent.setSourcePath(lb.getName());
 							  videoEvent.setDestinationPath(this.saveFilePath);
 							  imageEvent.addActions(videoEvent.getEventName());
 							  imageEvent.JSONActions();
 							  videoEvent.makeJSONObject();
 					    	  videoEvents.add(videoEvent);
 					    	  currentLabelIndex++;
 					    	  if(currentLabelIndex<labels.size())
 					    	  {	
 					    		  if(!createVideoEvent(videoEvent))
 					    			  return false;
 					    	  }   
 					      }	 
 					   
 					   
 					   if(lb.getText().contains(".obj")||lb.getText().contains(".osg"))
 					   { 	  
 							 MmModelEvent modelEvent = new MmModelEvent();
 							 if(modelEvents.size()==0)
 								  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_model");
 							  else
 								  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(modelEvents.size())+"_model"); 
 							 
 							 String[] attrs = lb.getText().split(":");
 							   if(attrs.length<=1)
 							   {	   
 								   modelEvent.setModelFile(attrs[0]);
 							       modelEvent.setCollectItem(false);
 							   }    
 							   if(attrs.length==2)
 							   { 	   
 								   modelEvent.setModelFile(attrs[0]);
 								   modelEvent.setCollectItem(true);
 								   collectItems.add(new Integer(collectItems.size()+1));
 							   } 
 							  modelEvent.setSourcePath(lb.getName());
 							  modelEvent.setDestinationPath(this.saveFilePath);
 							  imageEvent.addActions(modelEvent.getEventName());
 							  imageEvent.JSONActions();
 							  modelEvent.makeJSONObject();
 							  modelEvents.add(modelEvent);
 					    	  currentLabelIndex++;
 					    	  if(currentLabelIndex<labels.size())
 					    	  {	
 					    		  if(!createModelEvent(modelEvent))
 					    			  return false;
 					    	  }   
 					    }
 					      
 					      
 					      if(lb.getText().contains(".txt"))
 						   { 	  
 								  MmMessageEvent messageEvent = new MmMessageEvent();
 								  messageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(messageEvents.size())+"_message"); 
 								  String[] attrs = lb.getText().split(":");
 								  if(attrs.length<=1)
 								  {	   
 									  messageEvent.setMessageFile(attrs[0]);
 									  messageEvent.setCollectItem(false);
 								  }    
 								  if(attrs.length==2)
 								  { 	   
 									  messageEvent.setMessageFile(attrs[0]);
 									  messageEvent.setCollectItem(true);
 									  collectItems.add(new Integer(collectItems.size()+1));
 								  }
 								  messageEvent.setSourcePath(lb.getName());
 								  messageEvent.setDestinationPath(this.saveFilePath);
 								  messageEvent.makeJSONObject();
 								  messageEvents.add(messageEvent);
 								  imageEvent.addActions(messageEvent.getEventName());
 								  imageEvent.JSONActions();
 								  currentLabelIndex++;
 						    	  if(currentLabelIndex<labels.size())
 						    	  {	
 						    		  if(!createMessageEvent(messageEvent))
 						    			  return false;
 						    	  }   
 						      }
 		  
 		}	  
 		
 		
 		 if(labels.size()>=currentLabelIndex)
 		 {	
 				System.out.println("label index "+currentLabelIndex+" labels size "+labels.size());
 				
 				if(nextStation!=null)
 		    	{
 				    if(!nextStation.getStationType())
 				    	 imageEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				    else
 				    	 imageEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				    
 				    imageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 		    	}    
 			    else
 				{	
 					imageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 				    
 				}
 			    
 			    imageEvent.JSONActions();
 				return false;
 		 }
 		
 		
 		
 		
 		return true;
 		
 		
 	}
 	
 	public boolean createPanoramaEvent(MmPanoramaEvent panoramaEvent)
 	{
 		if(currentLabelIndex<labels.size())
 		{	
 		   JLabel lb = labels.get(currentLabelIndex);
 		   
 		   if(labels.get(currentLabelIndex).getText().isEmpty())
 		   {
 			    
 			    if(nextStation!=null)
 		    	{
 			       if(!nextStation.getStationType())
 			    	    panoramaEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			       else
 			    	    panoramaEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			       panoramaEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 		    	}   
 			    else
 				{	
 					panoramaEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 					panoramaEvent.addActions("Done");
 				}
 			    
 			    panoramaEvent.JSONActions();
 			       
 			    return false;
 			       
 		   }
 		   
 		   
 		   if(lb.getText().contains("Panorama"))
 	       {
 			   
 			   MmPanoramaEvent nextPanoramaEvent = new MmPanoramaEvent();
 			   if(panoramaEvents.size()==0)
 				   nextPanoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_panorama");
 			   else
 				   nextPanoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_panorama");
 			   panoramaEvent.addActions(nextPanoramaEvent.getEventName());
 			   panoramaEvent.JSONActions();
 			   panoramaEvents.add(nextPanoramaEvent);
 			   String[] attrs = lb.getText().split(":");
 			   if(attrs.length<=2)
 			   {	   
 			       nextPanoramaEvent.setPanoramaFile(attrs[0]);
 			       nextPanoramaEvent.setCollectItem(false);
 			   }    
 			   if(attrs.length==3)
 			   { 	   
 				   nextPanoramaEvent.setPanoramaFile(attrs[0]);
 				   nextPanoramaEvent.setCollectItem(true);
 				   collectItems.add(new Integer(collectItems.size()+1));
 			   }	   
 			   nextPanoramaEvent.setSourcePath(lb.getName());
 			   nextPanoramaEvent.setDestinationPath(this.saveFilePath);
 			   panoramaEvent.makeJSONObject();
 		       currentLabelIndex++;
 		       
 		       if(currentLabelIndex<labels.size())
 			    {	
 			    	
 			    	nextPanoramaEvent.makeJSONObject();
 			    	
 //			    	if(currentLabelIndex==labels.size()-1)
 //			    	{
 //			    		 if(!nextStation.getStationType())
 //						    	panoramaEvent.addActions("EnableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 //						    else
 //						    	panoramaEvent.addActions("EnableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 //			    	    
 //			    	}     
 			    	
 			        nextPanoramaEvent.JSONActions();
 				    panoramaEvents.add(nextPanoramaEvent);
 				    panoramaEvent.addActions(nextPanoramaEvent.getEventName());	
 				    
 				    if(!createPanoramaEvent(nextPanoramaEvent))
 				    	return false;
 			       
 			    }
 		       
 		   }
 		   
 		   if(lb.getText().contains(".mp3")||lb.getText().contains(".m4a")||lb.getText().contains(".aiff"))
 	       {
 			   
 			   MmAudioEvent audioEvent = new MmAudioEvent();
 			   if(audioEvents.size()==0)
 				   audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_audio");
 			   else
 				   audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_audio");
 			   		   
 			   panoramaEvent.addActions(audioEvent.getEventName());
 			   panoramaEvent.JSONActions();
 			   audioEvents.add(audioEvent);
 			   
 			   String[] attrs = lb.getText().split(":");
 			   if(attrs.length<=1)
 			   {	   
 				   audioEvent.setAudioFileName(attrs[0]);
 			       audioEvent.setCollectItem(false);
 			   }    
 			   if(attrs.length==2)
 			   { 	   
 				   audioEvent.setAudioFileName(attrs[0]);
 				   audioEvent.setCollectItem(true);
 				   collectItems.add(new Integer(collectItems.size()+1));
 			   }
 			   audioEvent.setSourcePath(lb.getName());
 			   audioEvent.setDestinationPath(this.saveFilePath);
 		       audioEvent.setAudioFileName(lb.getText());
 		       audioEvent.makeJSONObject();
 		       currentLabelIndex++;
 		       if(!createAudioEvent(audioEvent))
 		    	   return false;
 		       
 		   }
 		   
 		   if(lb.getText().contains(".m4v"))
 		   { 	  
 				  MmVideoEvent videoEvent = new MmVideoEvent();
 				  if(videoEvents.size()==0)
 				     videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_video");
 				  else
 					  videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_video");
 				   String[] attrs = lb.getText().split(":");
 				   if(attrs.length<=1)
 				   {	   
 					   videoEvent.setVideoFileName(attrs[0]);
 				       videoEvent.setCollectItem(false);
 				   }    
 				   if(attrs.length==2)
 				   { 	   
 					   videoEvent.setVideoFileName(attrs[0]);
 					   videoEvent.setCollectItem(true);
 					   collectItems.add(new Integer(collectItems.size()+1));
 				   }
 				  videoEvent.setVideoFileName(lb.getText());
 				  videoEvent.setSourcePath(lb.getName());
 				  videoEvent.setDestinationPath(this.saveFilePath);
 				  panoramaEvent.addActions(videoEvent.getEventName());
 				  panoramaEvent.JSONActions();
 				  videoEvent.makeJSONObject();
 		    	  videoEvents.add(videoEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    		  if(!createVideoEvent(videoEvent))
 		    			  return false;
 		    	  }   
 		      }	 
 		   
 		   
 		   if(lb.getText().contains(".obj")||lb.getText().contains(".osg"))
 		   { 	  
 				 MmModelEvent modelEvent = new MmModelEvent();
 				 if(modelEvents.size()==0)
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_model");
 				  else
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(modelEvents.size())+"_model"); 
 				 
 				   String[] attrs = lb.getText().split(":");
 				   if(attrs.length<=1)
 				   {	   
 					   modelEvent.setModelFile(attrs[0]);
 				       modelEvent.setCollectItem(false);
 				   }    
 				   if(attrs.length==2)
 				   { 	   
 					   modelEvent.setModelFile(attrs[0]);
 					   modelEvent.setCollectItem(true);
 					   collectItems.add(new Integer(collectItems.size()+1));
 				   } 
 				  modelEvent.setSourcePath(lb.getName());
 				  modelEvent.setDestinationPath(this.saveFilePath);
 				  panoramaEvent.addActions(modelEvent.getEventName());
 				  panoramaEvent.JSONActions();
 				  modelEvent.makeJSONObject();
 		    	  modelEvents.add(modelEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    		  if(!createModelEvent(modelEvent))
 		    			  return false;
 		    	  }   
 		    }
 		      
 		      		      
 		      if(lb.getText().contains(".txt"))
 			   { 	  
 					  MmMessageEvent messageEvent = new MmMessageEvent();
 					  messageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_message"); 
 					  String[] attrs = lb.getText().split(":");
 					  if(attrs.length<=1)
 					  {	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(false);
 					  }    
 					  if(attrs.length==2)
 					  { 	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(true);
 						  collectItems.add(new Integer(collectItems.size()+1));
 					  }
 					  messageEvent.setSourcePath(lb.getName());
 					  messageEvent.setDestinationPath(this.saveFilePath);
 					  panoramaEvent.addActions(panoramaEvent.getEventName());
 					  panoramaEvent.JSONActions();
 					  panoramaEvent.makeJSONObject();
 			    	  panoramaEvents.add(panoramaEvent);
 			    	  currentLabelIndex++;
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    		  if(!createMessageEvent(messageEvent))
 			    			  return false;
 			    	  }   
 			      }
 
 		   
 		
 		      if(isImage(lb.getText()))
 		      {
 		    	  
 		    	  MmImageEvent imageEvent = new MmImageEvent();
 			  	  
 				  if(imageEvents.isEmpty())
 					  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"Image");
 				  else
 					  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(imageEvents.size())+"Image");
 				        String[] attrs = lb.getText().split(":");
 				        
 				        if(attrs.length<=1)
 				        {	
 				        	imageEvent.setImageFile(attrs[0]);
 				           	imageEvent.setCollectItem(false);
 				        }	
 				           
 				        if(attrs.length==2)
 				        {	
 				        	
 				        	imageEvent.setImageFile(attrs[0]);
 				        	imageEvent.setCollectItem(true);
 				        	collectItems.add(new Integer(collectItems.size()+1));
 				        }  	
 				        imageEvent.setSourcePath(lb.getName());
 				        imageEvent.setDestinationPath(this.saveFilePath);
 				        imageEvent.makeJSONObject();
 				        imageEvent.JSONActions();
 					    imageEvents.add(imageEvent);
 					    imageEvent.addActions(imageEvent.getEventName());
 					    currentLabelIndex++;
 					    
 					    panoramaEvent.addActions(imageEvent.getEventName());
 						panoramaEvent.JSONActions();
 					    
 					    if(!createImageEvent(imageEvent))
 					    	return false;
 			        
 		      }
 		
 		   
 		}
 		
 		if(labels.size()>=currentLabelIndex)
 		{	
 			System.out.println("label index "+currentLabelIndex+" labels size "+labels.size());
 			if(nextStation!=null)
 	    	{
 			    if(!nextStation.getStationType())
 			    	panoramaEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    else
 			    	panoramaEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    
 			    panoramaEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 	    	}
 		    else
 			{	
 				panoramaEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 			    
 			}
 		    
 		    
 			
 			panoramaEvent.JSONActions();
 			return false;
 		}
 		
 		return true;
 	}
 	
 		
 	public boolean createModelEvent(MmModelEvent modelEvent)
 	{
 		if(currentLabelIndex<labels.size())
 		{	
 		   JLabel lb = labels.get(currentLabelIndex);
 		   
 		   if(labels.get(currentLabelIndex).getText().isEmpty())
 		   {
 			    
 			    if(nextStation!=null)
 		    	{
 			        if(!nextStation.getStationType())
 			    	     modelEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			        else
 			    	     modelEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			        modelEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 		    	}
 			    else
 				{	
 					modelEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 					modelEvent.addActions("Done");
 				}
 			    
 			    modelEvent.JSONActions();
 			    
 			    return false;
 		   }
 		   
 		   if(lb.getText().contains("Panorama"))
 	       {
 			   
 			   MmPanoramaEvent panoramaEvent = new MmPanoramaEvent();
 			   if(panoramaEvents.size()==0)
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_panorama");
 			   else
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_panorama");
 			   modelEvent.addActions(panoramaEvent.getEventName());
 			   modelEvent.JSONActions();
 			   panoramaEvents.add(panoramaEvent);
 			   
 			   String[] attrs = lb.getText().split(":");
 			   if(attrs.length<=2)
 			   {	   
 				   panoramaEvent.setPanoramaFile(attrs[0]);
 			       panoramaEvent.setCollectItem(false);
 			   }    
 			   if(attrs.length==3)
 			   { 	   
 				   panoramaEvent.setPanoramaFile(attrs[0]);
 				   panoramaEvent.setCollectItem(true);
 				   collectItems.add(new Integer(collectItems.size()+1));
 			   }
 			   panoramaEvent.setSourcePath(lb.getName());
 			   panoramaEvent.setDestinationPath(this.saveFilePath);
 			   panoramaEvent.makeJSONObject();
 			   currentLabelIndex++;
 			   if(!createPanoramaEvent(panoramaEvent))
 				   return false;
 		       
 		   }
 		   
 		   if(lb.getText().contains(".mp3")||lb.getText().contains(".m4a")||lb.getText().contains(".aiff"))
 	       {
 			   
 			   MmAudioEvent audioEvent = new MmAudioEvent();
 			   if(audioEvents.size()==0)
 				   audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_audio");
 			   else
 				   audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_audio");
 			   
 			   modelEvent.addActions(audioEvent.getEventName());
 			   modelEvent.JSONActions();
 			   audioEvents.add(audioEvent);
 			   String[] attrs = lb.getText().split(":");
 			   if(attrs.length<=1)
 			   {	   
 				   audioEvent.setAudioFileName(urlLinkText+"/audios/"+attrs[0]);
 			       audioEvent.setCollectItem(false);
 			   }    
 			   if(attrs.length==2)
 			   { 	   
 				   audioEvent.setAudioFileName(urlLinkText+"/audios/"+attrs[0]);
 				   audioEvent.setCollectItem(true);
 				   collectItems.add(new Integer(collectItems.size()+1));
 			   }
 			   audioEvent.setSourcePath(lb.getName());
 			   audioEvent.setDestinationPath(this.saveFilePath);
 		       audioEvent.makeJSONObject();
 		       currentLabelIndex++;
 		       if(!createAudioEvent(audioEvent))
 		    	   return false;
 		   }
 		   
 		   if(lb.getText().contains(".m4v"))
 		   { 	  
 				  MmVideoEvent videoEvent = new MmVideoEvent();
 				  if(videoEvents.size()==0)
 				     videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_video");
 				  else
 					  videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_video"); 
 				  
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=2)
 				  {	   
 					   videoEvent.setVideoFileName(urlLinkText+"/videos/"+attrs[0]);
 				       videoEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==3)
 				  { 	   
 					   videoEvent.setVideoFileName(urlLinkText+"/videos/"+attrs[0]);
 					   videoEvent.setCollectItem(true);
 					   collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  videoEvent.setSourcePath(lb.getName());
 				  videoEvent.setDestinationPath(this.saveFilePath);
 				  modelEvent.addActions(videoEvent.getEventName());
 				  modelEvent.JSONActions();
 				  videoEvent.makeJSONObject();
 		    	  videoEvents.add(videoEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    	     if(!createVideoEvent(videoEvent))
 		    	    	 return false;
 		    	  }   
 		   }
 		   
 		   if(lb.getText().contains(".obj")||lb.getText().contains(".osg"))
 		   { 	  
 				  MmModelEvent nextModelEvent = new MmModelEvent();
 				  if(modelEvents.size()==0)
 					  nextModelEvent.setEventName("station"+Integer.toString(stationIndex)+"_model");
 				  else
 					  nextModelEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(modelEvents.size())+"_model");
 				  
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  nextModelEvent.setModelFile(urlLinkText+"/objs/"+attrs[0]);
 					  nextModelEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  nextModelEvent.setModelFile(urlLinkText+"/objs/"+attrs[0]);
 					  nextModelEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  
 				  nextModelEvent.setSourcePath(lb.getName());
 				  nextModelEvent.setDestinationPath(this.saveFilePath);
 				  modelEvent.JSONActions();
 				  modelEvent.makeJSONObject();
 		    	  modelEvents.add(nextModelEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    		  nextModelEvent.makeJSONObject();
 				    	
 //		    		  if(!nextStation.getStationType())
 //					    	modelEvent.addActions("EnableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 //					    else
 //					    	modelEvent.addActions("EnableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				    	     
 				    	
 				      nextModelEvent.JSONActions();
 					  modelEvents.add(nextModelEvent);
 					  modelEvent.addActions(nextModelEvent.getEventName());	
 					  
 					  if(!createModelEvent(nextModelEvent))
 						  return false;
 		    	  }   
 		    }
 		     
 		      
 		      if(lb.getText().contains(".txt"))
 			   { 	  
 					  MmMessageEvent messageEvent = new MmMessageEvent();
 					  messageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_message"); 
 					  String[] attrs = lb.getText().split(":");
 					  if(attrs.length<=1)
 					  {	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(false);
 					  }    
 					  if(attrs.length==2)
 					  { 	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(true);
 						  collectItems.add(new Integer(collectItems.size()+1));
 					  }
 					  messageEvent.setSourcePath(lb.getName());
 					  messageEvent.setDestinationPath(this.saveFilePath);
 					  modelEvent.addActions(modelEvent.getEventName());
 					  modelEvent.JSONActions();
 					  modelEvent.makeJSONObject();
 			    	  modelEvents.add(modelEvent);
 			    	  currentLabelIndex++;
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    		  if(!createMessageEvent(messageEvent))
 			    			  return false;
 			    	  }   
 			      }
 
 		   
 		
 		      if(isImage(lb.getText()))
 		      {
                   
 		    	  MmImageEvent imageEvent = new MmImageEvent();
 			  	  
 				  if(imageEvents.isEmpty())
 					  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"Image");
 				  else
 					  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(imageEvents.size())+"Image");
 				        String[] attrs = lb.getText().split(":");
 				        
 				        if(attrs.length<=1)
 				        {	
 				        	imageEvent.setImageFile(attrs[0]);
 				        	imageEvent.setCollectItem(false);
 				        }	
 				           
 				        if(attrs.length==2)
 				        {	
 				        	
 				        	imageEvent.setImageFile(attrs[0]);
 				        	imageEvent.setCollectItem(true);
 				        	collectItems.add(new Integer(collectItems.size()+1));
 				        }  	
 				        imageEvent.setSourcePath(lb.getName());
 				        imageEvent.setDestinationPath(this.saveFilePath);
 				        imageEvent.makeJSONObject();
 				        imageEvent.JSONActions();
 					    imageEvents.add(imageEvent);
 					    imageEvent.addActions(imageEvent.getEventName());
 					    
 					    modelEvent.addActions(imageEvent.getEventName());
 						modelEvent.JSONActions();
 					    currentLabelIndex++;
 					    if(!createImageEvent(imageEvent))
 					    	return false;
 		     }
 		
 		   
 		}
 		
 		if(labels.size()>=currentLabelIndex)
 		{	
 			System.out.println("label index "+currentLabelIndex+" labels size "+labels.size());
 			
 			if(nextStation!=null)
 	    	{
 			     if(!nextStation.getStationType())
 			    	  modelEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			     else
 			    	  modelEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			     modelEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 	    	}
 		    else
 			{	
 				modelEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 			    
 			}
 		    
 		    modelEvent.JSONActions();
 			
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public boolean createAudioEvent(MmAudioEvent audioEvent)
 	{
 		
 		if(currentLabelIndex<labels.size())
 		{	
 		   	
 		   JLabel lb = labels.get(currentLabelIndex);
 		   System.out.println("Entered audio event "+labels.get(currentLabelIndex).getText());
 		   
 		   if(labels.get(currentLabelIndex).getText().isEmpty())
 		   {
 			    
 			    if(nextStation!=null)
 		    	{
 			        if(!nextStation.getStationType())
 			    	    audioEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			        else
 			    	    audioEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			        audioEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 		    	}
 			    else
 			    { 	
 			    	 audioEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 			    	 audioEvent.addActions("Done");
 			    } 	 
 			    audioEvent.JSONActions();
 			    
 			    
 			    return false;
 		   }
 		   
 		   if(lb.getText().contains("Panorama"))
 	       {
 			   
 			   MmPanoramaEvent panoramaEvent = new MmPanoramaEvent();
 			   if(panoramaEvents.size()==0)
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_panorama");
 			   else
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_panorama");
 			   audioEvent.addActions(panoramaEvent.getEventName());
 			   audioEvent.JSONActions();
 			   panoramaEvents.add(panoramaEvent);
 			   
 			   String[] attrs = lb.getText().split(":");
 			   if(attrs.length<=2)
 			   {	   
 					 panoramaEvent.setPanoramaFile(attrs[0]);
 				     panoramaEvent.setCollectItem(false);
 			   }    
 			   if(attrs.length==3)
 			   { 	   
 					 panoramaEvent.setPanoramaFile(attrs[0]);
 					 panoramaEvent.setCollectItem(true);
 					 collectItems.add(new Integer(collectItems.size()+1));
 			   }
 			   panoramaEvent.setSourcePath(lb.getName());
 			   panoramaEvent.setDestinationPath(this.saveFilePath);
 			   panoramaEvent.makeJSONObject();
 			   currentLabelIndex++;
 		       if(createPanoramaEvent(panoramaEvent))
 		    	   return false;
 		       
 		       
 		       panoramaEvent.setSourcePath(lb.getName());
 		   }
 		   
 		   
 		   
 		   if(lb.getText().contains(".mp3")||lb.getText().contains(".m4a")||lb.getText().contains(".aiff"))
 	       {
 			   
 			   MmAudioEvent nextAudioEvent = new MmAudioEvent();
 			   nextAudioEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_audio");
 			   audioEvent.addActions(nextAudioEvent.getEventName());
 			   audioEvent.JSONActions();
 			   audioEvents.add(nextAudioEvent);
 			   
 			   String[] attrs = lb.getText().split(":");
 			   if(attrs.length<=1)
 			   {	   
 					nextAudioEvent.setAudioFileName(attrs[0]);
 				    nextAudioEvent.setCollectItem(false);
 			   }    
 			
 			   if(attrs.length==2)
 			   { 	   
 					nextAudioEvent.setAudioFileName(attrs[0]);
 					nextAudioEvent.setCollectItem(true);
 					collectItems.add(new Integer(collectItems.size()+1));
 			   }
 			   
 			   nextAudioEvent.setSourcePath(lb.getName());
 			   nextAudioEvent.setDestinationPath(this.saveFilePath);
 		       
 		       currentLabelIndex++;
 		       
 		       if(currentLabelIndex<labels.size())
 		       {	   
 		         nextAudioEvent.makeJSONObject();
 		         
 		         //JOptionPane.showMessageDialog(null, "labels size "+labels.size());
 		    	
 		     	 if(currentLabelIndex==labels.size()-1)
 		     	 {	 
 		     		if(currentLabelIndex==labels.size()-1)
 			    	{
 //		     			 if(!nextStation.getStationType())
 //						    	audioEvent.addActions("EnableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 //						    else
 //						    	audioEvent.addActions("EnableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    	}
 		     	 }    
 		    	
 		     	if(!createAudioEvent(nextAudioEvent))
 		     		return false;
 		         
 		       }  
 		   }
 		   
 		   if(lb.getText().contains(".m4v"))
 		   { 	  
 			      System.out.println("video event in audio "+lb.getText());
 				  MmVideoEvent videoEvent = new MmVideoEvent();
 				  if(videoEvents.size()==0)
 				     videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_video");
 				  else
 					  videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_video");
 				  
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  videoEvent.setVideoFileName(attrs[0]);
 				      videoEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  videoEvent.setVideoFileName(attrs[0]);
 					  videoEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  videoEvent.setSourcePath(lb.getName());
 				  videoEvent.setDestinationPath(this.saveFilePath);
 				  audioEvent.addActions(videoEvent.getEventName());
 				  audioEvent.JSONActions();
 				  videoEvent.makeJSONObject();
 		    	  videoEvents.add(videoEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {
 		    		 System.out.println("video event in audio "+lb.getText());
 		    	     if(!createVideoEvent(videoEvent))
                            return false;
 		    	  }   
 		      }	   
 		   
 		   
 		      if(lb.getText().contains(".obj")||lb.getText().contains(".osg"))
 		      { 	  
 				 MmModelEvent modelEvent = new MmModelEvent();
 				 if(modelEvents.size()==0)
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_model");
 				  else
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(modelEvents.size())+"_model"); 
 				  
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  modelEvent.setModelFile(attrs[0]);
 				      modelEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  modelEvent.setModelFile(attrs[0]);
 					  modelEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  modelEvent.setSourcePath(lb.getName());
 				  modelEvent.setDestinationPath(this.saveFilePath);
 				  audioEvent.addActions(modelEvent.getEventName());
 				  audioEvent.JSONActions();
 				  modelEvent.makeJSONObject();
 		    	  modelEvents.add(modelEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    	     if(!createModelEvent(modelEvent))
 		    	    	 return false;
 		    	  }   
 		     }
 		      
 		      		      
 		      if(lb.getText().contains(".txt"))
 			   { 	  
 					  MmMessageEvent messageEvent = new MmMessageEvent();
 					  messageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_message"); 
 					  String[] attrs = lb.getText().split(":");
 					  if(attrs.length<=1)
 					  {	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(false);
 					  }    
 					  if(attrs.length==2)
 					  { 	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(true);
 						  collectItems.add(new Integer(collectItems.size()+1));
 					  }
 					  messageEvent.setSourcePath(lb.getName());
 					  messageEvent.setDestinationPath(this.saveFilePath);
 					  audioEvent.addActions(audioEvent.getEventName());
 					  audioEvent.JSONActions();
 					  audioEvent.makeJSONObject();
 			    	  currentLabelIndex++;
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    	     if(!createMessageEvent(messageEvent))
 			    	    	 return false;
 			    	  }   
 			      }
 
 		   
 		
 		      if(isImage(lb.getText()))
 		      {
                   
 		    	  MmImageEvent imageEvent = new MmImageEvent();
 			  	  //JOptionPane.showMessageDialog(null, "image event in audio "+lb.getText());
 				  if(imageEvents.isEmpty())
 					  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"Image");
 				  else
 					  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(imageEvents.size())+"Image");
 				        String[] attrs = lb.getText().split(":");
 				        
 				        if(attrs.length<=1)
 				        {	
 				        	imageEvent.setImageFile(attrs[0]);
 				        	imageEvent.setCollectItem(false);
 				        }	
 				           
 				        if(attrs.length==2)
 				        {	
 				        	
 				        	imageEvent.setImageFile(attrs[0]);
 				        	imageEvent.setCollectItem(true);
 				        	collectItems.add(new Integer(collectItems.size()+1));
 				        }  	
 				        imageEvent.setSourcePath(lb.getName());
 				        imageEvent.setDestinationPath(this.saveFilePath);
 				        imageEvent.makeJSONObject();
 				        imageEvent.JSONActions();
 					    imageEvents.add(imageEvent);
 					    //imageEvent.addActions(imageEvent.getEventName());   
 					    
 					    audioEvent.addActions(imageEvent.getEventName());
 					    audioEvent.JSONActions();
 					    
 					    currentLabelIndex++;
 				    	if(currentLabelIndex<labels.size())
 				    	{	
 				    	     if(!createImageEvent(imageEvent))
 				    	     {
 				    		     return false;
 				    	    }	 
 				    	}
 				    	
 		     }
 		
 		   
 		}
 		
 		if(labels.size()>=currentLabelIndex)
 		{	
 			System.out.println("label index "+currentLabelIndex+" labels size "+labels.size());
 			
 			if(nextStation!=null)
 	    	{
 			    if(!nextStation.getStationType())
 			    	audioEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    else
 			    	audioEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    audioEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));    
 			    
 	    	}
 			else
 			{	
 				audioEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 			    
 			}    
 			
 			audioEvent.JSONActions();
 			return false;
 		}		
 		
 		return true;
 	}
 	
 	public boolean createVideoEvent(MmVideoEvent videoEvent)
 	{
 		if(currentLabelIndex<labels.size())
 		{	
 		   JLabel lb = labels.get(currentLabelIndex);
 		   
 		   if(labels.get(currentLabelIndex).getText().isEmpty())
 		   {
 			    System.out.println("label index "+labels.get(currentLabelIndex).getText());
 			    
 			    if(nextStation!=null)
 		    	{
 			        if(!nextStation.getStationType())
 			    	    videoEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			        else
 			    	    videoEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			        
 		    	}
 			    else
 				{	
 					videoEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 					videoEvent.addActions("Done");
 				}
 			    
 			    videoEvent.JSONActions();
 			    
 			    return false;
 		   }
 		   
 		   if(lb.getText().contains("Panorama"))
 	       {
 			   
 			   MmPanoramaEvent panoramaEvent = new MmPanoramaEvent();
 			   if(panoramaEvents.size()==0)
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_panorama");
 			   else
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_panorama");
 			   videoEvent.addActions(panoramaEvent.getEventName());
 			   videoEvent.JSONActions();
 			   panoramaEvents.add(panoramaEvent);
 			      
 			      String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=2)
 				  {	   
 					  panoramaEvent.setPanoramaFile(attrs[0]);
 				      panoramaEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==3)
 				  { 	   
 					  panoramaEvent.setPanoramaFile(attrs[0]);
 					  panoramaEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 			   panoramaEvent.setSourcePath(lb.getName());	  
 			   panoramaEvent.setDestinationPath(this.saveFilePath);
 			   panoramaEvent.makeJSONObject();
 		       currentLabelIndex++;
 		       if(!createPanoramaEvent(panoramaEvent))
 		    	   return false;
 		   }
 		   
 		   if(lb.getText().contains(".mp3")||lb.getText().contains(".m4a")||lb.getText().contains(".aiff"))
 	       {
 			   
 			   MmAudioEvent audioEvent = new MmAudioEvent();
 			   if(audioEvents.size()==0)
 				  audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_audio");
 			   else	   
 			      audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_audio");
 			   videoEvent.addActions(audioEvent.getEventName());
 			   videoEvent.JSONActions();
 			   audioEvents.add(audioEvent);
 			      
 			      String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  audioEvent.setAudioFileName(attrs[0]);
 				      audioEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  audioEvent.setAudioFileName(attrs[0]);
 					  audioEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 			   audioEvent.setSourcePath(lb.getName());	  
 			   audioEvent.setDestinationPath(this.saveFilePath);
 		       audioEvent.makeJSONObject();
 		       currentLabelIndex++;
 		       
 		       if(!createAudioEvent(audioEvent))
 		    	   return false;
 		   }
 		   
 		   if(lb.getText().contains(".m4v"))
 		   { 	  
 				  MmVideoEvent nextVideoEvent = new MmVideoEvent();
 				  nextVideoEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_video"); 
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  nextVideoEvent.setVideoFileName(attrs[0]);
 				      nextVideoEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  nextVideoEvent.setVideoFileName(attrs[0]);
 					  nextVideoEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  nextVideoEvent.setSourcePath(lb.getName());
 				  nextVideoEvent.setDestinationPath(this.saveFilePath);
 				  
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    		  nextVideoEvent.makeJSONObject();
 				    	
 //		    		  if(!nextStation.getStationType())
 //					    	videoEvent.addActions("EnableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 //					    else
 //					    	videoEvent.addActions("EnableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 				      	    
 				    	
 				         nextVideoEvent.JSONActions();
 					     videoEvents.add(nextVideoEvent);
 					     videoEvent.addActions(nextVideoEvent.getEventName());	
 					     if(!createVideoEvent(nextVideoEvent))
 					    	 return false;
 				     	    	     
 		    	  }   
 		      }	
 		   
 		   
 		      if(lb.getText().contains(".obj")||lb.getText().contains(".osg"))
 		      { 	  
 				 MmModelEvent modelEvent = new MmModelEvent();
 				 if(modelEvents.size()==0)
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_model");
 				  else
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(modelEvents.size())+"_model"); 
 				  
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  modelEvent.setModelFile(attrs[0]);
 				      modelEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  modelEvent.setModelFile(attrs[0]);
 					  modelEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  modelEvent.setSourcePath(lb.getName());
 				  modelEvent.setDestinationPath(this.saveFilePath);
 				  videoEvent.addActions(modelEvent.getEventName());
 				  videoEvent.JSONActions();
 				  modelEvent.makeJSONObject();
 				  modelEvents.add(modelEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    		  if(!createModelEvent(modelEvent))
 		    			  return false;
 		    	  }   
 		     }
 		      
 		     
 		      
 		       if(lb.getText().contains(".txt"))
 			   { 	  
 					  MmMessageEvent messageEvent = new MmMessageEvent();
 					  messageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_message"); 
 					  String[] attrs = lb.getText().split(":");
 					  if(attrs.length<=1)
 					  {	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(false);
 					  }    
 					  if(attrs.length==2)
 					  { 	   
 						  messageEvent.setMessageFile(attrs[0]);
 						  messageEvent.setCollectItem(true);
 						  collectItems.add(new Integer(collectItems.size()+1));
 					  }
 					  messageEvent.setSourcePath(lb.getName());
 					  messageEvent.setDestinationPath(this.saveFilePath);
 					  videoEvent.addActions(videoEvent.getEventName());
 					  videoEvent.JSONActions();
 					  videoEvent.makeJSONObject();
 			    	  videoEvents.add(videoEvent);
 			    	  currentLabelIndex++;
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    		  if(!createMessageEvent(messageEvent))
 			    			  return false;
 			    	  }   
 			      }
 		   
 		
 		       if(isImage(lb.getText()))
 			   {
 	                  
 			    	  MmImageEvent imageEvent = new MmImageEvent();
 				  	  //JOptionPane.showMessageDialog(null, "image event in audio "+lb.getText());
 					  if(imageEvents.isEmpty())
 						  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"Image");
 					  else
 						  imageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(imageEvents.size())+"Image");
 					        String[] attrs = lb.getText().split(":");
 					        
 					        if(attrs.length<=1)
 					        {	
 					        	imageEvent.setImageFile(attrs[0]);
 					        	imageEvent.setCollectItem(false);
 					        }	
 					           
 					        if(attrs.length==2)
 					        {	
 					        	
 					        	imageEvent.setImageFile(attrs[0]);
 					        	imageEvent.setCollectItem(true);
 					        	collectItems.add(new Integer(collectItems.size()+1));
 					        }  	
 					        imageEvent.setSourcePath(lb.getName());
 					        imageEvent.setDestinationPath(this.saveFilePath);
 					        imageEvent.makeJSONObject();
 					        imageEvent.JSONActions();
 						    imageEvents.add(imageEvent);
 						    //imageEvent.addActions(imageEvent.getEventName());   
 						    
 						    videoEvent.addActions(imageEvent.getEventName());
 						    videoEvent.JSONActions();
 						    
 						    currentLabelIndex++;
 					    	if(currentLabelIndex<labels.size())
 					    	{	
 					    	     if(!createImageEvent(imageEvent))
 					    	     {
 					    		     return false;
 					    	    }	 
 					    	}
 					    	
 			     }
 			
 			   
 			
 		
 		   
 		}
 		
 		
 		if(labels.size()>=currentLabelIndex)
 		{	
 			System.out.println("label index "+currentLabelIndex+" labels size "+labels.size());
 			
 			if(nextStation!=null)
 	    	{
 			    if(!nextStation.getStationType())
 			    	videoEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    else
 			    	videoEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    
 	    	}    
 		    else
 			{	
 				videoEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 			    
 			}
 		    
 		    videoEvent.JSONActions();
 			return false;
 		}
 			
 		
 		return true;
 	}
 	
 	
 	public boolean createMessageEvent(MmMessageEvent messageEvent)
 	{
 		if(currentLabelIndex<labels.size())
 		{	
 		   JLabel lb = labels.get(currentLabelIndex);
 		   
 		   if(labels.get(currentLabelIndex).getText().isEmpty())
 		   {
 			    System.out.println("label index "+labels.get(currentLabelIndex).getText());
 			    
 			    if(nextStation!=null)
 		    	{
 			       if(!nextStation.getStationType())
 			    	   messageEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			       else
 			    	   messageEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			       messageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));
 		    	}   
 			    else
 				{	
 					messageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 					messageEvent.addActions("Done");
 				}
 			    
 			    messageEvent.JSONActions();
 			    return false;
 		   }
 		   
 		   if(lb.getText().contains("Panorama"))
 	       {
 			   
 			   MmPanoramaEvent panoramaEvent = new MmPanoramaEvent();
 			   if(panoramaEvents.size()==0)
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_panorama");
 			   else
 				   panoramaEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_panorama");
 			   messageEvent.addActions(panoramaEvent.getEventName());
 			   messageEvent.JSONActions();
 			   panoramaEvents.add(panoramaEvent);
 			      
 			      String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=2)
 				  {	   
 					  panoramaEvent.setPanoramaFile(attrs[0]);
 				      panoramaEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==3)
 				  { 	   
 					  panoramaEvent.setPanoramaFile(attrs[0]);
 					  panoramaEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 			   panoramaEvent.setSourcePath(lb.getName());	  
 			   panoramaEvent.setDestinationPath(this.saveFilePath);
 			   panoramaEvent.makeJSONObject();
 			   currentLabelIndex++;
 			   
 		       if(!createPanoramaEvent(panoramaEvent))
 		    	   return false;
 		       
 		   }
 		   
 		   if(lb.getText().contains(".mp3")||lb.getText().contains(".m4a")||lb.getText().contains(".aiff"))
 	       {
 			   
 			   MmAudioEvent audioEvent = new MmAudioEvent();
 			   if(audioEvents.size()==0)
 				  audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_audio");
 			   else	   
 			      audioEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_audio");
 			   messageEvent.addActions(audioEvent.getEventName());
 			   messageEvent.JSONActions();
 			   audioEvents.add(audioEvent);
 			      
 			      String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  audioEvent.setAudioFileName(attrs[0]);
 				      audioEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  audioEvent.setAudioFileName(attrs[0]);
 					  audioEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 			   audioEvent.setSourcePath(lb.getName());	  
 			   audioEvent.setDestinationPath(this.saveFilePath);
 		       audioEvent.makeJSONObject();
 		       currentLabelIndex++;
 		       if(!createAudioEvent(audioEvent))
 		    	   return false;
 		   }
 		   
 		   if(lb.getText().contains(".m4v"))
 		   { 	  
 				  MmVideoEvent videoEvent = new MmVideoEvent();
 				  videoEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_video"); 
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  videoEvent.setVideoFileName(attrs[0]);
 				      videoEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  videoEvent.setVideoFileName(attrs[0]);
 					  videoEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  videoEvent.setSourcePath(lb.getName());
 				  videoEvent.setDestinationPath(this.saveFilePath);
 				  messageEvent.addActions(videoEvent.getEventName());
 				  messageEvent.JSONActions();
 				  videoEvent.makeJSONObject();
 		    	  videoEvents.add(videoEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    		  if(!createVideoEvent(videoEvent))
 		    			  return false;
 		    	  }   
 		      }	
 		   
 		   
 		      if(lb.getText().contains(".obj")||lb.getText().contains(".osg"))
 		      { 	  
 				 MmModelEvent modelEvent = new MmModelEvent();
 				 if(modelEvents.size()==0)
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_model");
 				  else
 					  modelEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(modelEvents.size())+"_model"); 
 				  
 				  String[] attrs = lb.getText().split(":");
 				  if(attrs.length<=1)
 				  {	   
 					  modelEvent.setModelFile(attrs[0]);
 				      modelEvent.setCollectItem(false);
 				  }    
 				  if(attrs.length==2)
 				  { 	   
 					  modelEvent.setModelFile(attrs[0]);
 					  modelEvent.setCollectItem(true);
 					  collectItems.add(new Integer(collectItems.size()+1));
 				  }
 				  modelEvent.setSourcePath(lb.getName());
 				  modelEvent.setDestinationPath(this.saveFilePath);
 				  messageEvent.addActions(modelEvent.getEventName());
 				  messageEvent.JSONActions();
 				  modelEvent.makeJSONObject();
 		    	  modelEvents.add(modelEvent);
 		    	  currentLabelIndex++;
 		    	  if(currentLabelIndex<labels.size())
 		    	  {	
 		    		  if(!createModelEvent(modelEvent))
 		    			  return false;
 		    	  }   
 		     }
 		      
 		      
 		      
 		      
 		      if(lb.getText().contains(".txt"))
 			   { 	  
 					  MmMessageEvent nextMessageEvent = new MmMessageEvent();
 					  nextMessageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(videoEvents.size())+"_video"); 
 					  String[] attrs = lb.getText().split(":");
 					  
 					  if(attrs.length<=1)
 					  {	   
 						  nextMessageEvent.setMessageFile(attrs[0]);
 					      nextMessageEvent.setCollectItem(false);
 					  }    
 					  if(attrs.length==2)
 					  { 	   
 						  nextMessageEvent.setMessageFile(attrs[0]);
 						  nextMessageEvent.setCollectItem(true);
 						  collectItems.add(new Integer(collectItems.size()+1));
 					  }
 					  
 					  nextMessageEvent.setSourcePath(lb.getName());
 					  nextMessageEvent.setDestinationPath(this.saveFilePath);
 					  
 			    	  currentLabelIndex++;
 			    	  if(currentLabelIndex<labels.size())
 			    	  {	
 			    		  nextMessageEvent.makeJSONObject();
 					    	
 					      if(currentLabelIndex==labels.size()-1)
 					      {
 //					    	  if(!nextStation.getStationType())
 //							    	messageEvent.addActions("EnableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 //							    else
 //							    	messageEvent.addActions("EnableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 					      }
 					    	    
 					    	
 					      nextMessageEvent.JSONActions();
 						  messageEvents.add(nextMessageEvent);
 						  messageEvent.addActions(nextMessageEvent.getEventName());	
 						  if(!createMessageEvent(nextMessageEvent))
 							    return false;
 			    	  }   
 			      }	
 			   
 
 		   
 		
 		      if(isImage(lb.getText()))
 		      {
 			      
 		    	  
 		    	   MmImageEvent imageEvent = new MmImageEvent();
 				   if(imageEvents.size()==0)
 					   imageEvent.setEventName("station"+Integer.toString(stationIndex)+"_image");
 				   else
 					   imageEvent.setEventName("station"+Integer.toString(stationIndex)+"_"+Integer.toString(audioEvents.size())+"_image");
 				   imageEvent.addActions(imageEvent.getEventName());
 				   imageEvent.JSONActions();
 				   imageEvents.add(imageEvent);
 				      
 				      String[] attrs = lb.getText().split(":");
 					  if(attrs.length<=1)
 					  {	   
 						  imageEvent.setImageFile(attrs[0]);
 						  
 						  imageEvent.setCollectItem(false);
 					  }    
 					  if(attrs.length==2)
 					  { 	   
 						  imageEvent.setImageFile(attrs[0]);
 						  
 						  imageEvent.setCollectItem(true);
 						  collectItems.add(new Integer(collectItems.size()+1));
 					  }
 					  imageEvent.setSourcePath(lb.getName());	  
 					  imageEvent.setDestinationPath(this.saveFilePath);
 					  imageEvent.makeJSONObject();
 					  messageEvent.addActions(imageEvent.getEventName());
 					  messageEvent.JSONActions();
 					  currentLabelIndex++;
 			          if(!createImageEvent(imageEvent))
 			        	  return false;
 			       
 		    	  
 		    	    
 		     }
 		
 		   
 		}
 		
 		
 		if(labels.size()>=currentLabelIndex)
 		{	
 			System.out.println("label index "+currentLabelIndex+" labels size "+labels.size());
 			
 			if(nextStation!=null)
 	    	{
 			    if(!nextStation.getStationType())
 			    	 messageEvent.addActions("enableStation"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    else
 			    	 messageEvent.addActions("enableStation"+Integer.toString(nextStation.getSwingPointStationIndex())+"SwingPoint"+Integer.toString(nextStation.getIndexStationNameIndex())+"Compass");
 			    
 	    	}
 		    else
 			{	
 				messageEvent.addActions("mapEditMarker"+Integer.toString(stationIndex));	
 			    
 			}
 		    
 		    messageEvent.JSONActions();
 			
 			return false;
 		}
 			
 		
 		return true;
 	}
 	
 	public boolean isImage(String imageName)
 	{
 		String[] images = {
 				".png",".PNG",".jpg",".JPG",".jpeg",".JPEG",".gif",".GIF",".tiff",".TIFF",".bmp",".BMP"
 		};
 		
 		String imageExt = imageName;
 		
 		//imageExt = imageName.substring(imageName.length()-4, imageName.length());
 		
 		for(int i=0;i<images.length;i++)
 		{
 			if(imageExt.contains(images[i]) && !(imageExt.contains("Panorama")))
 			{
 				return true;
 			}
 			else
 				continue;
 		}
 		
 		return false;
 	}
 	
 	public JSONObject getStationEevnts()
 	{
 		return stationEvent;
 	}
 	
 	public ArrayList<MmMarkerEvent> getMarkerEvents()
 	{
 		return markerEvents;
 	}
 	
 	public ArrayList<MmAudioEvent> getAudioEvents()
 	{
 		return audioEvents;
 	}
 	
 	public ArrayList<MmVideoEvent> getVideoEvents()
 	{
 		return videoEvents;
 	}
 	
 	public ArrayList<Integer> getItemsCollected()
 	{
 		return collectItems;
 	}
 	
 	public void writeJsonObjects(JSONArray jsonEvents)
 	{
 		
 		jsonEvents.put(stationEvent);
 		
 		
 		
 		
 		/*for(int i=0;i<markerEvents.size();i++)
 			jsonEvents.put(markerEvents.get(i).getMarkerEvent());*/
 		
 		for(int i=0;i<imageEvents.size();i++)
 			jsonEvents.put(imageEvents.get(i).getImageEvent());
 		
 		for(int i=0;i<audioEvents.size();i++)
 			jsonEvents.put(audioEvents.get(i).getAudioEvent());
 		
 		for(int i=0;i<modelEvents.size();i++)
 			jsonEvents.put(modelEvents.get(i).getModelEvent());
 		
 		for(int i=0;i<panoramaEvents.size();i++)
 			jsonEvents.put(panoramaEvents.get(i).getPanoramaEvent());
 		
 		for(int i=0;i<videoEvents.size();i++)
 			jsonEvents.put(videoEvents.get(i).getVideoEvent());
 		
 		for(int i=0;i<messageEvents.size();i++)
 			jsonEvents.put(messageEvents.get(i).getMessageEvent());
 		
 		
 	}
 	
 }
