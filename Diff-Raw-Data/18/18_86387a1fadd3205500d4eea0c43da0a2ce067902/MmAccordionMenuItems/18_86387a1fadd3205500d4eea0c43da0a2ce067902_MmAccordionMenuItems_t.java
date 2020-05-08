 package mmAccordionMenu;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Paint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 
 import javax.swing.*;
 import javax.swing.border.EtchedBorder;
 
 import mmEvents.MmAddEvents;
 import mmEvents.MmAddGlobalMarkers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.*;
 
 import mmLanguage.MmLanguage;
 import mmMap.*;
 
 import org.geonames.*;
 import org.jdesktop.swingx.mapviewer.GeoPosition;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 
 
 public class MmAccordionMenuItems  {
 	
 	private JButton button;
 	private JTextField textField;
 	private JLabel label;
 	private JPanel panel;
 	private ArrayList<JLabel> labels;
 	
 	int select_index=-1,move_select_index = -1;
 	
 	int start_select_index=-1;
 	
 	String selectText; 
 	
 	double latitude,longitude;
 	
 	JTextField latitudeField = new JTextField(25);
 	JTextField longitudeField = new JTextField(25);
 	
 	
 	String menuName="";
 	JLabel moveLabel;
 	
 	
 	MmMapViewer map;
 	
 	boolean isMovable;
 	
 	public MmAddGlobalMarkers events;
 	
 	JTextArea searchArea = new JTextArea(5,25);
 	
 	JWindow window = new JWindow();
 	
 	int language;
 		
 	
 	public MmAccordionMenuItems(String text)
 	{
 		//super(text);
 	}
 	
 	public int getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(int language) {
 		this.language = language;
 	}
 
 	
 	public MmAccordionMenuItems()
 	{
 		//super("Text");
 		labels = new ArrayList<JLabel>();
 		panel = new JPanel();
 		moveLabel = new JLabel();
 		
 		
 	}
 	
 	public void addButtonItem(String text,String name)
 	{
 		button = new JButton(text);
 		button.setName(name);
 		
 	}
 	
 	public void addLabelItem(String text)
 	{
 		label = new JLabel(text);
 		label.setName(text);
 		label.addMouseListener(getMenuItemMouseAdapter());
 		
 	}
 	
 	
 	
 	public void addLabelIconItem(String text)
 	{
 		label = new JLabel();
 		ImageIcon icon = new ImageIcon(this.getClass().getResource("resources/image_add.png"));
 		
 		label.setIcon(icon);
 		label.setName(text);
 	}
 	
 	public void setMap(MmMapViewer map1)
 	{
 		map = map1;
 	}
 	
 	public void addTextFieldItem(String text,int length)
 	{
 		textField = new JTextField(length);
 		textField.setText(text);
 		textField.setForeground(Color.black);
 		//textField.setPreferredSize(new Dimension(375,20));
 		textField.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				// TODO Auto-generated method stub
 				textField.setSelectionStart(textField.getCaretPosition());
 				textField.setSelectionEnd(textField.getText().length());
 				textField.setSelectedTextColor(Color.black);
 				
 				
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
 		
 		textField.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				// TODO Auto-generated method stub
 				
 				 String[] strings = textField.getText().split("\\s+");
 				 
 				 				 
 				 
 				 String str = "http://maps.googleapis.com/maps/api/geocode/json?address=";
 				 
 				if(strings.length>0)
 				 {
 					 for(int i=0;i<strings.length-1;i++)
 					     str +=strings[i]+"+";
 				 }
 				 
 				 str+=strings[strings.length-1]+",+";
 				 
 				
 				 try
 				  {
 				     URL url = new URL(str+"SE&sensor=false");
 				     //JOptionPane.showMessageDialog(null, url.getQuery());
 				     
 				     
 				     InputStream is = url.openStream();
 				     InputStreamReader is1 = new InputStreamReader(is);
 		             StringBuilder sb=new StringBuilder();
 		             BufferedReader br = new BufferedReader(is1);
 		             String read = br.readLine();
 
 		             while(read != null) {
 		                    //System.out.println(read);
 		                    sb.append(read);
 		                    read =br.readLine();
 
 		              }
 		                
 		             //JOptionPane.showMessageDialog(null, sb.toString());
 		             //System.out.println("data "+sb.toString());
 		             
 		             String jsonContent = sb.toString();
 		             JSONTokener jsonTokens = new JSONTokener(jsonContent);
 		             
 		             JSONObject jsonObject = new JSONObject(jsonTokens);
 		             
 		             //JOptionPane.showMessageDialog(null, jsonObject);
 		             //JOptionPane.showMessageDialog(null, jsonObject.get("results"));
 		             
 		             if(!jsonObject.isNull("results"))
 		             {
 		            	 JSONArray jsonArray = new JSONArray(jsonObject.get("results").toString());
 		            	 //JOptionPane.showMessageDialog(null, jsonArray.get(0));
 		            	 jsonObject = (JSONObject) jsonArray.getJSONObject(0).getJSONObject("geometry");
 		            	 jsonObject = (JSONObject)jsonObject.get("location");
 		            	 
 		            	 //JOptionPane.showMessageDialog(null, jsonObject.get("lat"));
 		            	 //JOptionPane.showMessageDialog(null, jsonObject.get("lng"));
 		            	 
 		            	 latitude = (Double)jsonObject.get("lat");
 				         longitude = (Double)jsonObject.get("lng");
 				         
 				         map.getMap().getMainMap().setAddressLocation(new GeoPosition(latitude,longitude));
 						 map.getMap().setZoom(1);
 				         
 				         //JOptionPane.showMessageDialog(null, "lat "+latitude+" long  "+longitude);
 		            	 
 		            	 //jsonArray = new JSONArray(jsonObject.get("geometry").toString());
 		            	 //JOptionPane.showMessageDialog(null, jsonArray.length());
 		             }
 		             
 		             /*JSONArray placeAttrs = new JSONArray(jsonTokens);
 		             for(int i=0;i<placeAttrs.length();i++)
 		             {
 		            	 try
 		            	 {
 		            	    jsonObject = (JSONObject)placeAttrs.get(i);
 		            	    
 		            	    if(!jsonObject.isNull("location"))
 		            	    {
 		            	    	
 		            	    }
 		            	 }
 		            	 catch(Exception e)
 		            	 {
 		            		 JOptionPane.showMessageDialog(null,e);
 		            	 }
 		            	 
 		            	 
 		             }*/
 		             
 		             
 				  }
 				  catch(Exception e)
 				  {
 					  JOptionPane.showMessageDialog(null, e);
 				  }
 				
 				//JOptionPane.showMessageDialog(null, textField.getText());
 				
 				/*WebService.setUserName("umapathi"); // add your username here
 				 
 				  ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
 				  searchCriteria.setQ(textField.getText());
 				  try
 				  {
 				     ToponymSearchResult searchResult = WebService.search(searchCriteria);
 				     
 				     for (Toponym toponym : searchResult.getToponyms()) {
 				         System.out.println(toponym.getName()+" "+toponym.getLatitude()+"  "+toponym.getLongitude()+" "+toponym.getName());
 				         latitude = toponym.getLatitude();
 				         longitude = toponym.getLongitude();
 				         System.out.println("map kit "+map.getMap()+"  "+textField.getText());
 						 map.getMap().getMainMap().setAddressLocation(new GeoPosition(latitude,longitude));
 						 map.getMap().setZoom(1);
 						 
 						 if(searchArea.getText().isEmpty())
 						     searchArea.setText(searchArea.getText()+textField.getText());
 						 else
 							 searchArea.setText(searchArea.getText()+","+toponym.getName());
 				     }
 				  }
 				  catch(Exception e)
 				  {
 					  JOptionPane.showMessageDialog(null, e);
 				  }*/
 				  
                   
 				 
 				
 				/*InputStream is = null;
 				
 				 try {
 		                String address = URLEncoder.encode(textField.getText(), "utf-8");
 		                String geocodeURL = "http://nominatim.openstreetmap.org/search?format=json&amp;limit=1&amp;polygon=1&amp;addressdetails=0&amp;email=venkata_tallap@hotmail.com&amp;countrycodes=se&amp;q=";
 		                //query google geocode api
 		                String formattedUrl = geocodeURL + address;
 		                
 		                		                                 
 		                URL geocodeUrl = new URL(formattedUrl);
 		                
 		                is = geocodeUrl.openStream();
 		                
 		                String jsonString = new String("");
 		                
 		                InputStreamReader is1 = new InputStreamReader(is);
 		                StringBuilder sb=new StringBuilder();
 		                BufferedReader br = new BufferedReader(is1);
 		                String read = br.readLine();
 
 		                while(read != null) {
 		                    //System.out.println(read);
 		                    sb.append(read);
 		                    read =br.readLine();
 
 		                }
 		                
 		                jsonString = sb.toString();
 		                
 		                //JOptionPane.showMessageDialog(null, jsonString);
 		                System.out.println(jsonString);
 		                
 		            } catch (IOException ex) {
 		            	
 		            	JOptionPane.showMessageDialog(null, ex);
 		                
 		            } finally {
 		                try {
 		                    is.close();
 		                } catch (IOException ex) {
 		                    
 		                }
 		            }*/
 				
 			}
 			
 		});
 	}
 	
 	
 	
 	public JPanel getSearchItemPanel()
 	{
 		
 		JPanel searchPanel = new JPanel();
 		//searchPanel.setLayout(new BorderLayout());
 			    
 	    
 	    //searchPanel.setBackground(Color.white);
 	    
 	    JPanel gridPanel = new JPanel();
 	    
 	    gridPanel.setLayout(new GridBagLayout());
 	    
 	    GridBagConstraints gbc = new GridBagConstraints();
 	        
 	    
 	    JScrollPane searchScrollPane = new JScrollPane(searchArea);
 	    
 	    //JPanel searchTextFieldPanel = new JPanel();
 	    
 	    //searchTextFieldPanel.add(textField);
 	    
 	    //searchPanel.add(textField,BorderLayout.NORTH);
 	    
 	    
 	    //searchScrollPane.setBorder(BorderFactory.createEtchedBorder());
 	    //gridPanel.add(searchScrollPane);
 	    gbc.fill = GridBagConstraints.HORIZONTAL;
 	    gbc.gridx = 0;
 	    gbc.gridy = 0;
 	    gridPanel.add(textField, gbc);
 	    
 	    gbc.fill = GridBagConstraints.HORIZONTAL;
 	    gbc.gridx = 0;
 	    gbc.gridy = 1;
 	    gridPanel.add(searchScrollPane, gbc);
 	    
 	    //gridPanel.add(getGeoItemPanel());
 	    gbc.fill = GridBagConstraints.HORIZONTAL;
 	    gbc.gridx = 0;
 	    gbc.gridy = 2;
 	    gridPanel.add(getGeoItemPanel(), gbc);
 	    searchPanel.add(gridPanel,BorderLayout.SOUTH);
 	    
 	    
 	    return searchPanel;
 	   
 	    
 	}
 	
 	public void addGeoFields()
 	{
 		latitudeField.setText("latitude");
 		longitudeField.setText("longitude");
 		
 		latitudeField.setToolTipText(MmLanguage.language_search[language][1]);
 		
 		longitudeField.setToolTipText(MmLanguage.language_search[language][2]);
 	}
 	
 	public double getLatitude()
 	{
 		return Double.parseDouble(latitudeField.getText());
 	}
 	
 	public double getLongitude()
 	{
 		return Double.parseDouble(longitudeField.getText());
 	}
 	
 	
 	public JPanel getGeoItemPanel()
 	{
 		
 		JPanel geoPanel = new JPanel();
 		geoPanel.setLayout(new BorderLayout());
 			    
 	    
 	    //geoPanel.setBackground(Color.white);
 	    
 	     
 	    geoPanel.add(latitudeField,BorderLayout.NORTH);
 	        
 	    geoPanel.add(longitudeField,BorderLayout.SOUTH);
 	    
 	    return geoPanel;
 	   
 	    
 	}
 	
 		
 	
 	public String getTextFieldsText()
 	{
 		return textField.getText();
 	}
 	
 	public Paint getBackgroundPaint()
 	{
 		return null;
 	}
 	
 	public ImageIcon getMenuImage()
 	{
 		return null;
 	}
 	
 	public ImageIcon getMenuIcon()
 	{
 		return new ImageIcon(this.getClass().getResource("resources/default_item.png")); 
 	}
 	
 	private MouseAdapter getMenuItemMouseAdapter() {
         return new MouseAdapter() {
 
             @Override
             public void mousePressed(MouseEvent e) {
                 //MmAccordionMenuItems item = (MmAccordionMenuItems) e.getSource();
                 
                 //System.out.println(" selected item "+e.getSource());
                    
                
             }
         };
    }
 	
 	public void addLabelItems(JLabel lb1,String menu)
 	{
 		if(menuName.equals(""))
 			menuName = menu;
 		JLabel lb = lb1;
 		
 		
 		
 		if(menuName.equals("start") &&  labels.size()<4)
 		{
 			lb.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 			lb.setOpaque(false);
 			lb.setForeground(new Color(24,24,24));
 		}
 		
 		if(menuName.equals("start") &&  labels.size()>5)
 		{
 			lb.setOpaque(false);
 			lb.setForeground(new Color(255,255,255));
 		}
 		
 		labels.add(lb1);
 	}
 	
 	
 	
 	public ArrayList<JLabel> getLabels()
 	{
 		return labels;
 	}
 	
 	public JPanel getButton()
 	{
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.add(button);
 		return buttonPanel;
 	}
 	
 	public JButton getButtonItem()
 	{
 		return button;
 	}
 	
 	public JLabel getLabel()
 	{
 		return label;
 	}
 	
 	public JTextField getTextField()
 	{
 		//JPanel panel1 = new JPanel();
 		//panel1.add(textField);
 		return textField;
 	}
     
 	public JScrollPane getScrollPaneItem()
 	{
 		//panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		panel.setLayout(new GridLayout(0,1));
 		for(int i=0;i<labels.size();i++)
 			panel.add(labels.get(i));
 		panel.setBackground(Color.white);
 		JScrollPane pane = new JScrollPane(panel);
 		//pane.setPreferredSize(new Dimension(275,75));
 		pane.setBorder(BorderFactory.createEtchedBorder());
 		return pane;
 	}
 	
 	public void addLabelItemActions()
 	{
 		panel.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent event) {
 				// TODO Auto-generated method stub
 			
 				JLabel lb = (JLabel) panel.getComponentAt(event.getPoint());
 				if(select_index!=-1)
 				{
                     JLabel lb1 = (JLabel) panel.getComponent(select_index);
 					
 					lb1.setOpaque(false);
 					lb1.setBackground(Color.white);
 					panel.revalidate();
 				}   
 				
 				JLabel lb1 = (JLabel) panel.getComponentAt(event.getPoint());
 				lb1.setOpaque(true);
 				lb1.setBackground(Color.lightGray);
 				panel.revalidate();
 				select_index = labels.indexOf(lb1);
 				selectText = labels.get(select_index).getText();
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
 			public void mousePressed(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				
 				/*if(menuName.equals("start"))
 				{
 					JLabel lb =  (JLabel) panel.getComponentAt(event.getPoint());
 					move_select_index = labels.indexOf(lb);
 					//JOptionPane.showMessageDialog(null, move_select_index);
 					if(move_select_index!=0 && move_select_index<4)
 					{	
 					   
 					   System.out.println("Entered data");
 					   isMovable = true;
 					   moveLabel.setText(lb.getText());
 					   moveLabel.setName(lb.getName());
 					   moveLabel.setOpaque(true);
 					   moveLabel.setBackground(Color.lightGray);
 					   moveLabel.setLocation(lb.getLocation());
 					   panel.add(moveLabel);
 					   panel.revalidate();
 					   
 					}   
 				}*/
 				
 				
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				/*if(menuName.equals("start"))
 				{
 					JLabel lb = (JLabel) panel.getComponentAt(event.getPoint());
 					int index = labels.indexOf(lb);
 					
 					if(index!=0 && move_select_index!=index)
 					{	
 						 
 						if(move_select_index>index)
 						{	
 							
 							JLabel tempLabel = new JLabel(labels.get(index).getText());
 							tempLabel.setName(labels.get(index).getName());
 							labels.get(index).setText(moveLabel.getText());
 							labels.get(index).setName(moveLabel.getName());
 							swapDown(tempLabel,index);	
 							
 						   
 						
 						}
 						
 						if(move_select_index<index)
 						{
 							JLabel tempLabel = new JLabel(labels.get(select_index+1).getText());
 							tempLabel.setName(labels.get(select_index+1).getName());
 							int index1 = select_index; 
 							index1++;
 							swapUp(tempLabel,index,index1);
 							labels.get(index).setText(moveLabel.getText());
 							labels.get(index).setName(moveLabel.getName());
 							
 						}
 					   
 						panel.remove(moveLabel);
 						panel.revalidate();
 					}   
 					else
 					{
 						panel.remove(moveLabel);
 						panel.revalidate();
 					}
  				}*/
 				
 			}
 			
 		});
 		
 		panel.addMouseMotionListener(new MouseMotionListener() {
 
 			@Override
 			public void mouseDragged(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				//moveLabel.setLocation(event.getPoint());
 				
 				
 			}
 
 			@Override
 			public void mouseMoved(MouseEvent event) {
 				// TODO Auto-generated method stub
 				
 				JLabel lb = (JLabel) panel.getComponentAt(event.getPoint());
 				
 				int labelIndex = labels.indexOf(lb);
 				
 				if(events!=null && events.getStations().isEmpty())
 				{
 					JComponent cmp = (JComponent)event.getSource();
 			        cmp.setToolTipText(MmLanguage.language_media[language][0]);
 				}
 				
 				if(events!=null && labelIndex!=-1 && labelIndex<events.getStations().size())
 				{
 					if(events.getStations().get(labelIndex).getMarkerIndex()!=-1 && !events.getStations().get(labelIndex).getMarkerMediaEvents().isEmpty())
 					{
 						JComponent cmp = (JComponent)event.getSource();
 				        cmp.setToolTipText(events.getStations().get(labelIndex).getMarkerMediaEvents());
 					}
 					else
 					{
 						JComponent cmp = (JComponent)event.getSource();
 				        cmp.setToolTipText(MmLanguage.language_media[language][0]);
 					}
 				}
 				else
 				{
 					JComponent cmp = (JComponent)event.getSource();
 			        cmp.setToolTipText(MmLanguage.language_media[language][0]);
 				}
 				
 				
 				
 				
 			}
 			
 		});
 		
 		
 	}
 	
 	public int getMarkerSelected()
 	{
 		return select_index;
 	}
 	
 	public void updatePanel()
 	{
 		panel.revalidate();
 	}
 	
 	public void setMarkerSelectedText(String string)
 	{
 		selectText = string;
 		
 		labels.get(select_index).setText(selectText);
 		panel.revalidate();
 	}
 	
 	public void setMarkersText(ArrayList<String> texts)
 	{
 		
 		
 		for(int i=0;i<texts.size();i++)
 		{
 			labels.get(i).setText(texts.get(i));
 		}
 		panel.revalidate();
 	}
 	
 	public void resetContent()
 	{
		for(int i=0;i<14;i++)
 		{
 			labels.get(i).setText(MmLanguage.language_markers[language][i]);
 			labels.get(i).setName("patt.marker"+Integer.toString(i+1));
 		}
 		
 		/*labels.get(0).setText(MmLanguage.language_markers[0][0]);
 		labels.get(0).setName("patt.marker1");
 		labels.get(1).setText(MmLanguage.language_markers[0][1]);
 		labels.get(1).setName("patt.marker2");
 		labels.get(2).setText(MmLanguage.language_markers[0][2]);
 		labels.get(2).setName("patt.marker3");
 		labels.get(3).setText(MmLanguage.language_markers[0][3]);
 		labels.get(3).setName("patt.marker4");
 		labels.get(4).setText("MarkÃ¶r 5");
 		labels.get(4).setName("patt.marker5");
 		labels.get(5).setText("MarkÃ¶r 6");
 		labels.get(5).setName("patt.marker6");
 		labels.get(6).setText("MarkÃ¶r 7");
 		labels.get(6).setName("patt.marker7");
 		labels.get(7).setText("MarkÃ¶r 8");
 		labels.get(7).setName("patt.marker8");
 		labels.get(8).setText("MarkÃ¶r 9");
 		labels.get(8).setName("patt.marker9");
 		labels.get(9).setText("MarkÃ¶r 10");
 		labels.get(9).setName("patt.marker10");
 		labels.get(10).setText("MarkÃ¶r 11");
 		labels.get(10).setName("patt.marker11");
 		labels.get(11).setText("MarkÃ¶r 12");
 		labels.get(11).setName("patt.marker12");
 		labels.get(12).setText("MarkÃ¶r 13");
 		labels.get(12).setName("patt.marker13");
 		labels.get(13).setText("MarkÃ¶r 14");
 		labels.get(13).setName("patt.marker14");
 		labels.get(14).setText("MarkÃ¶r 15");
 		labels.get(14).setName("patt.marker15");
 		labels.get(15).setText("MarkÃ¶r 16");
 		labels.get(15).setName("patt.marker16");
 		labels.get(16).setText("MarkÃ¶r 17");
 		labels.get(16).setName("patt.marker17");
 		labels.get(17).setText("MarkÃ¶r 18");
 		labels.get(17).setName("patt.marker18");*/
 		
 		
 		
		for(int i=0;i<14;i++)
 		{
 			labels.get(i).setBackground(Color.white);
 		}
         
 		//labels.get(select_index).setText(selectText);
 		panel.revalidate();
 		
 		
 		
 	}
 	
 	public void resetStartContent()
 	{
 				
 		labels.get(0).setText(MmLanguage.language_startMedia[language][0]);
 		labels.get(1).setText(MmLanguage.language_startMedia[language][1]);
 		labels.get(2).setText(MmLanguage.language_startMedia[language][1]);
 		labels.get(3).setText(MmLanguage.language_startMedia[language][1]);
 		labels.get(4).setText("");
 		labels.get(5).setText("");
 		
 		panel.revalidate();
 	}
 	
 	
 	public String getMarkerSelectedText()
 	{
 		return selectText;
 	}
 	
 	public JLabel getMarkerSelectedLabel()
 	{
 		JLabel lb = labels.get(select_index);
 		return lb;
 	}
 	
 	public void removeSelectedItem(int index)
 	{
 		if(index<4)
 		{
 			JLabel lb = (JLabel)panel.getComponent(index);
 			lb.setText("new");
 			lb.setName("text");
 			lb.setOpaque(false);
 			lb.setBackground(Color.white);
 			
 		}
 		   
 	}
 	
 	
 	
 
 }
