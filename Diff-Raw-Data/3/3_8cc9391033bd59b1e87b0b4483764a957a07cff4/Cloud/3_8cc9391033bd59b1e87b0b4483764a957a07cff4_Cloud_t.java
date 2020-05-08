 package edu.msu.monopoly.project2;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlSerializer;
 
 import android.os.NetworkOnMainThreadException;
 import android.util.Xml;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Cloud {
     private static final String ADDUSER_URL = "https://www.cse.msu.edu/~siaudvyt/monopoly/new-user.php";
     private static final String LOGIN_URL = "https://www.cse.msu.edu/~siaudvyt/monopoly/login.php";
     private static final String UTF8 = "UTF-8";
     
     
     /**
      * Skip the XML parser to the end tag for whatever 
      * tag we are currently within.
      * @param xml the parser
      * @throws IOException
      * @throws XmlPullParserException
      */
     public static void skipToEndTag(XmlPullParser xml) 
         throws IOException, XmlPullParserException {
         int tag;
         do
         {
             tag = xml.next();
             if(tag == XmlPullParser.START_TAG) {
                 // Recurse over any start tag
                 skipToEndTag(xml);
             }
         } while(tag != XmlPullParser.END_TAG && 
         tag != XmlPullParser.END_DOCUMENT);
     }
     
     public boolean addNewUser(String name, String pass) {
     	return createUserXml(ADDUSER_URL, name, pass);
     }
     
     public boolean login(String name, String pass) {
     	return createUserXml(LOGIN_URL, name, pass);
     }
     
     private boolean createUserXml(String dbUrl, String name, String pass) {
         name = name.trim();
         if(name.length() == 0) {
             return false;
         }
         
         // Create an XML packet with the information about the current image
         XmlSerializer xml = Xml.newSerializer();
         StringWriter writer = new StringWriter();
         
         try {
             xml.setOutput(writer);
             
             xml.startDocument("UTF-8", true);
             
             xml.startTag(null, "userinfo");
 
             xml.attribute(null, "user", name);
             xml.attribute(null, "pw", pass);
             
             xml.endTag(null, "userinfo");
             
             xml.endDocument();
 
         } catch (IOException e) {
             // This won't occur when writing to a string
             return false;
         }
         
         final String xmlStr = writer.toString();
         
         /*
          * Convert the XML into HTTP POST data
          */
         String postDataStr;
         try {
             postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             return false;
         }
         
         /*
          * Send the data to the server
          */
         byte[] postData = postDataStr.getBytes();
         
         InputStream stream = null;
         try {
             URL url = new URL(dbUrl);
 
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
             conn.setUseCaches(false);
 
             OutputStream out = conn.getOutputStream();
             out.write(postData);
             out.close();
 
             int responseCode = conn.getResponseCode();
             if(responseCode != HttpURLConnection.HTTP_OK) {
                 return false;
             } 
             
             stream = conn.getInputStream();
             
             if (!parseUserResult(stream)) {
             	return false;
             }
             
         } catch (MalformedURLException e) {
             return false;
         } catch (IOException ex) {
             return false;
         } catch (SecurityException es) {
             return false;
         } catch (NetworkOnMainThreadException en) {
             return false;
         } finally {
             if(stream != null) {
                 try {
                     stream.close();
                 } catch(IOException ex) {
                     // Fail silently
                 }
             }
         }
         
         return true;
     }
 
     private InputStream getUsersCatalog() {
 		try {
 			URL url = new URL("");
 	    	HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
 	    	InputStream stream =conn.getInputStream();
 	    	
 	    	int responseCode = conn.getResponseCode();
 	    	
             if(responseCode != HttpURLConnection.HTTP_OK) {
                 return null;
             }
 	    	
 	    	return stream;
 		} catch (MalformedURLException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
     }
     
     private void getGamesCatalog() {
     	
     }
     
     private boolean saveGame(DrawingView view, String user, String password) {
         // Create an XML packet with the information about the current image
         XmlSerializer xml = Xml.newSerializer();
         StringWriter writer = new StringWriter();
         
         try {
             xml.setOutput(writer);
             
             xml.startDocument("UTF-8", true);
             
             xml.startTag(null, "game");
 
             xml.attribute(null, "user", user);
             xml.attribute(null, "pw", password);
             
             view.saveXml(xml);
             
             xml.endTag(null, "game");
             
             xml.endDocument();
 
         } catch (IOException e) {
             // This won't occur when writing to a string
             return false;
         }
         
         final String xmlStr = writer.toString();
     	return false;
     }
     
     private InputStream loadGame() {
 		try {
 			URL url = new URL("");
 	    	HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
 	    	InputStream stream =conn.getInputStream();
 
 	    	int responseCode = conn.getResponseCode();
 	    	
             if(responseCode != HttpURLConnection.HTTP_OK) {
                 return null;
             }
 	    	
 	    	return stream;
 		} catch (MalformedURLException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
     }
 //    /**
 //     * An adapter so that list boxes can display a list of filenames from 
 //     * the cloud server.
 //     */
 //    public static class CatalogAdapter extends BaseAdapter {
 //
 //    	/**
 //    	 * The items to display in the list box. Initially null.
 //    	 */
 //    	private ArrayList<Item> items = new ArrayList<Item>();
 //    	
 //        /**
 //         * Constructor
 //         */
 //        public CatalogAdapter(final View view) {
 //            // Create a thread to load the catalog
 //        	new Thread(new Runnable() {
 //
 //				@Override
 //				public void run() {
 //                    ArrayList<Item> newItems = getCatalog();
 //                    
 //                    if(newItems != null) {
 //                        items = newItems;
 //                        
 //                        view.post(new Runnable() {
 //    
 //                            @Override
 //                            public void run() {
 //                                // Tell the adapter the data set has been changed
 //                                notifyDataSetChanged();
 //                            }
 //                            
 //                        });
 //                    } else {
 //                        // Error condition
 //                        view.post(new Runnable() {
 //
 //                            @Override
 //                            public void run() {
 //                                Toast.makeText(view.getContext(), R.string.catalog_fail, Toast.LENGTH_SHORT).show();
 //                            }
 //                            
 //                        });
 //                    }
 //				}
 //        		
 //        	}).start();
 //        }
 //        
 //        public ArrayList<Item> getCatalog() {
 //            ArrayList<Item> newItems = new ArrayList<Item>();
 //            
 //            String query = CATALOG_URL + "?user=" + USER + "&magic=" + MAGIC + "&pw=" + PASSWORD;
 //            
 //            /**
 //             * Open a connection
 //             */
 //            InputStream stream = null;
 //            try {
 //            	URL url = new URL(query);
 //            	
 //            	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 //            	int responseCode = conn.getResponseCode();
 //            	if (responseCode != HttpURLConnection.HTTP_OK){
 //            		return null;
 //            	}
 //            	
 //            	stream = conn.getInputStream();
 //            	
 //            	try {
 //            		XmlPullParser xml = Xml.newPullParser();
 //            		xml.setInput(stream, UTF8);
 //            		
 //            		xml.nextTag();
 //            		xml.require(XmlPullParser.START_TAG, null, "hatter");
 //            		
 //            		String status = xml.getAttributeValue(null, "status");
 //                    if(status.equals("no")) {
 //                        return null;
 //                    }
 //                    
 //                    while(xml.nextTag() == XmlPullParser.START_TAG) {
 //                        if(xml.getName().equals("hatting")) {
 //                            Item item = new Item();
 //                            item.name = xml.getAttributeValue(null, "name");
 //                            item.id = xml.getAttributeValue(null, "id");
 //                            newItems.add(item);
 //                        }
 //                        
 //                        skipToEndTag(xml);
 //                    }
 //                    
 //            	} catch(XmlPullParserException ex) {
 //            		return null;
 //            	} catch (IOException ex) {
 //            		return null;
 //            	} finally {
 //                    try {
 //                        stream.close();
 //                    } catch(IOException ex) {
 //                        
 //                    }
 //                }
 //            	
 //            	
 //            } catch (MalformedURLException e) {
 //            	return null;
 //            } catch (IOException ex) {
 //            	return null;
 //            }
 //            
 //            return newItems;
 //        }
 //        
 //		@Override
 //		public int getCount() {
 //			return items.size();
 //		}
 //
 //		@Override
 //		public Item getItem(int position) {
 //			return items.get(position);
 //		}
 //
 //		@Override
 //		public long getItemId(int position) {
 //			return position;
 //		}
 //
 //		@Override
 //		public View getView(int position, View view, ViewGroup parent) {
 //			if (view == null) {
 //				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_item, parent, false);
 //			}
 //			
 //			TextView tv = (TextView)view.findViewById(R.id.textItem);
 //			tv.setText(items.get(position).name);
 //			
 //			return view;
 //		}
 //        
 //		public String getId(int position) {
 //			return items.get(position).id;
 //		}
 //		
 //		public String getName(int position) {
 //			return items.get(position).name;
 //		}
 //    }
     //    /**
 //     * Open a connection to a hatting in the cloud.
 //     * @param id id for the hatting
 //     * @return reference to an input stream or null if this fails
 //     */
 //    public InputStream openFromCloud(final String id) {
 //        // Create a get query
 //        String query = LOAD_URL + "?user=" + USER + "&magic=" + MAGIC + "&pw" + PASSWORD + "&id=" + id;
 //        
 //        try {
 //            URL url = new URL(query);
 //
 //            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 //            int responseCode = conn.getResponseCode();
 //            if(responseCode != HttpURLConnection.HTTP_OK) {
 //                return null;
 //            }
 //            
 //            InputStream stream = conn.getInputStream();
 //            return stream;
 //
 //        } catch (MalformedURLException e) {
 //            // Should never happen
 //            return null;
 //        } catch (IOException ex) {
 //            return null;
 //        }
 //    }
     
 //    /**
 //     * Save a hatting to the cloud.
 //     * This should be run in a thread.
 //     * @param name name to save under
 //     * @param view view we are getting the data from
 //     * @return true if successful
 //     */
 //    public boolean saveToCloud(String name, HatterView view) {
 //        name = name.trim();
 //        if(name.length() == 0) {
 //            return false;
 //        }
 //        
 //        // Create an XML packet with the information about the current image
 //        XmlSerializer xml = Xml.newSerializer();
 //        StringWriter writer = new StringWriter();
 //        
 //        try {
 //            xml.setOutput(writer);
 //            
 //            xml.startDocument("UTF-8", true);
 //            
 //            xml.startTag(null, "hatter");
 //
 //            xml.attribute(null, "user", USER);
 //            xml.attribute(null, "pw", PASSWORD);
 //            xml.attribute(null, "magic", MAGIC);
 //            
 //            view.saveXml(name, xml);
 //            
 //            xml.endTag(null, "hatter");
 //            
 //            xml.endDocument();
 //
 //        } catch (IOException e) {
 //            // This won't occur when writing to a string
 //            return false;
 //        }
 //        
 //        final String xmlStr = writer.toString();
 //        
 //        /*
 //         * Convert the XML into HTTP POST data
 //         */
 //        String postDataStr;
 //        try {
 //            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
 //        } catch (UnsupportedEncodingException e) {
 //            return false;
 //        }
 //        
 //        /*
 //         * Send the data to the server
 //         */
 //        byte[] postData = postDataStr.getBytes();
 //        
 //        InputStream stream = null;
 //        try {
 //            URL url = new URL(SAVE_URL);
 //
 //            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 //
 //            conn.setDoOutput(true);
 //            conn.setRequestMethod("POST");
 //            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
 //            conn.setUseCaches(false);
 //
 //            OutputStream out = conn.getOutputStream();
 //            out.write(postData);
 //            out.close();
 //
 //            int responseCode = conn.getResponseCode();
 //            if(responseCode != HttpURLConnection.HTTP_OK) {
 //                return false;
 //            } 
 //            
 //            stream = conn.getInputStream();
 //            
 //            if (!parseResult(stream)) {
 //            	return false;
 //            }
 //             
 //            
 //        } catch (MalformedURLException e) {
 //            return false;
 //        } catch (IOException ex) {
 //            return false;
 //        } finally {
 //            if(stream != null) {
 //                try {
 //                    stream.close();
 //                } catch(IOException ex) {
 //                    // Fail silently
 //                }
 //            }
 //        }
 //        
 //        return true;
 //    }
     
 //    public boolean deleteFromCloud(final String id){
 //
 //        // Create a get query
 //        String query = DELETE_URL + "?user=" + USER + "&magic=" + MAGIC + "&pw" + PASSWORD + "&id=" + id;
 //        
 //        try {
 //            URL url = new URL(query);
 //
 //            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 //            int responseCode = conn.getResponseCode();
 //            if(responseCode != HttpURLConnection.HTTP_OK) {
 //                return false;
 //            }
 //            
 //            InputStream stream = conn.getInputStream();
 //
 //            if (!parseResult(stream)) {
 //            	return false;
 //            }
 //
 //        } catch (MalformedURLException e) {
 //            // Should never happen
 //            return false;
 //        } catch (IOException ex) {
 //            return false;
 //        }
 //        return true;
 //    }
 
 //    /**
 //     * Create an XML parser for the result
 //     */
 //    public boolean parseResult(InputStream stream) {
 //        try {
 //            XmlPullParser xmlR = Xml.newPullParser();
 //            xmlR.setInput(stream, UTF8);
 //            
 //            xmlR.nextTag();      // Advance to first tag
 //            xmlR.require(XmlPullParser.START_TAG, null, "hatter");
 //            
 //            String status = xmlR.getAttributeValue(null, "status");
 //            if(status.equals("no")) {
 //                return false;
 //            }
 //            
 //        } catch(XmlPullParserException ex) {
 //            return false;
 //        } catch(IOException ex) {
 //            return false;
 //        }
 //        return true;
 //    }
     
     /**
      * Create an XML parser for the result
      */
     public boolean parseUserResult(InputStream stream) {
         try {
             XmlPullParser xmlR = Xml.newPullParser();
             xmlR.setInput(stream, UTF8);
             
             xmlR.nextTag();      // Advance to first tag
             xmlR.require(XmlPullParser.START_TAG, null, "userinfo");
             
             String status = xmlR.getAttributeValue(null, "status");
             if(status.equals("no")) {
                 return false;
             }
             
         } catch(XmlPullParserException ex) {
             return false;
         } catch(IOException ex) {
             return false;
         }
         return true;
     }
     
     
 }
