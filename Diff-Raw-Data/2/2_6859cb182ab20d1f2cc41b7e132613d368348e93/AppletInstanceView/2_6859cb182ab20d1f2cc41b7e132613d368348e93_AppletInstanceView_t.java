 package org.remus.serverNodes;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.mpstore.KeyValuePair;
 import org.mpstore.Serializer;
 import org.remus.RemusInstance;
 import org.remus.work.RemusApplet;
 import org.remus.work.Submission;
 
 public class AppletInstanceView implements BaseNode {
 
 	RemusApplet applet;
 	RemusInstance inst;
 
 	public AppletInstanceView(RemusApplet applet, RemusInstance inst) {
 		this.applet = applet;
 		this.inst = inst;
 	}
 
 	@Override
 	public void doDelete(String name, Map params, String workerID) throws FileNotFoundException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doGet(String name, Map params, String workerID, Serializer serial,
 			OutputStream os) throws FileNotFoundException {
 
 		if ( params.containsKey( DataStackInfo.PARAM_FLAG ) ) {
 			try {
 				os.write( serial.dumps( DataStackInfo.formatInfo(PipelineStatusView.class, "status", applet.getPipeline() ) ).getBytes() );
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return;
 		}
 		
 		String sliceStr = null;
 		int sliceSize = 0;
 		if ( params.containsKey("slice") ) {
 			sliceStr = ((String [])params.get("slice"))[0];
 			sliceSize = Integer.parseInt(sliceStr);
 		}
 
 		if ( name.length() == 0 ) {
 			if ( sliceStr == null ) {
 				for( String key : applet.getDataStore().listKeys( applet.getPath() , inst.toString() ) ) {
 					try {
 						os.write( serial.dumps( key ).getBytes() );
 						os.write("\n".getBytes());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				/*
 				for ( KeyValuePair kv : applet.getDataStore().listKeyPairs( applet.getPath() , inst.toString() ) ) {			
 					Map out = new HashMap();
 					out.put( kv.getKey(), kv.getValue() );	
 					try {
 						os.write( serial.dumps( out ).getBytes() );
 						os.write("\n".getBytes());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				 */		
 			} else {
 				for ( String sliceKey : applet.getDataStore().keySlice( applet.getPath(), inst.toString(), "", sliceSize) ) {
 					try {
 						os.write( serial.dumps( sliceKey ).getBytes() );
 						os.write("\n".getBytes());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				/*
 				for ( String sliceKey : applet.getDataStore().keySlice( applet.getPath(), inst.toString(), "", sliceSize) ) {
 					for ( Object value : applet.getDataStore().get(  applet.getPath(), inst.toString(), sliceKey ) ) {
 						Map oMap = new HashMap();
 						oMap.put( sliceKey, value);
 						try {
 							os.write( serial.dumps( oMap ).getBytes() );
 							os.write("\n".getBytes());
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 				 */
 			}
 		} else {
 			String [] tmp = name.split("/");
 			if ( tmp.length == 1) {
 				if ( sliceStr == null ) {
 					for ( Object obj : applet.getDataStore().get( applet.getPath() , inst.toString(), name) ) {
 						Map out = new HashMap();
 						out.put(name, obj );				
 						try {
 							os.write( serial.dumps(out).getBytes() );
 							os.write("\n".getBytes());
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				} else {
 					for ( String sliceKey : applet.getDataStore().keySlice( applet.getPath(), inst.toString(), name, sliceSize) ) {
 						for ( Object value : applet.getDataStore().get(  applet.getPath(), inst.toString(), sliceKey ) ) {
 							Map oMap = new HashMap();
 							oMap.put( sliceKey, value);
 							try {
 								os.write( serial.dumps( oMap ).getBytes() );
 								os.write("\n".getBytes());
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 			} else {				
 				try {
 					InputStream is = applet.getAttachStore().readAttachement(applet.getPath(), inst.toString(), tmp[0], tmp[1] );
 					byte buffer[] = new byte[1024];
 					int len;
 					while ((len=is.read(buffer)) > 0 ) {
 						os.write(buffer, 0, len);
 					}
					is.close();		
					os.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void doPut(String name, String workerID, Serializer serial, InputStream is, OutputStream os) throws FileNotFoundException {
 		try {
 			if ( name.length() > 0 ) {
 				String [] tmp = name.split("/");
 				if ( tmp.length == 1) {
 					StringBuilder sb = new StringBuilder();
 					byte [] buffer = new byte[1024];
 					int len;			
 					while( (len=is.read(buffer)) > 0 ) {
 						sb.append(new String(buffer, 0, len));
 					}
 					Object data = serial.loads(sb.toString());
 
 					applet.getDataStore().add( applet.getPath(), 
 							inst.toString(),
 							0L, 0L,
 							name, data);		
 				} else {
 					applet.getAttachStore().writeAttachment( applet.getPath() , inst.toString(), tmp[0], tmp[1], is );
 				}
 			} else {
 				StringBuilder sb = new StringBuilder();
 				byte [] buffer = new byte[1024];
 				int len;			
 				while( (len=is.read(buffer)) > 0 ) {
 					sb.append(new String(buffer, 0, len));
 				}
 				String iStr = sb.toString();
 				if ( iStr.length() > 0 ) {
 					Map data = (Map) serial.loads( iStr );
 					if ( data != null ) {
 						for ( Object key : data.keySet() ) {
 							applet.getDataStore().add( applet.getPath(), 
 									inst.toString(),
 									0L, 0L,
 									(String)key, data.get(key) );		
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private Map postStr2Map(String iStr) throws UnsupportedEncodingException {
 		Map out = new HashMap<String, String>();
 		for ( String el : iStr.split("&") ) {
 			String [] tmp = el.split("=");
 			if ( tmp.length > 1) {
 				out.put( URLDecoder.decode(tmp[0], "UTF-8"), URLDecoder.decode(tmp[1], "UTF-8"));
 			} else {
 				out.put( URLDecoder.decode(el, "UTF-8"), null);
 			}
 		}		
 		return out;
 	}
 
 	@Override
 	public void doSubmit(String name, String workerID, Serializer serial,
 			InputStream is, OutputStream os) throws FileNotFoundException {
 
 		if ( applet.getType() == RemusApplet.STORE ) {
 			//A submit to an agent is translated from URL encoding to JSON and stored with a
 			//UUID as the key
 			try {
 				BufferedReader br = new BufferedReader(new InputStreamReader(is));
 				StringBuilder sb = new StringBuilder();
 				String curline;
 				while ( (curline = br.readLine() ) != null ) {
 					sb.append(curline);
 				}
 				Map inData = postStr2Map(sb.toString()) ;
 				applet.getDataStore().add( applet.getPath(), 
 						inst.toString(),
 						0L, 0L,
 						(new RemusInstance()).toString(), inData );		
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}			
 		} else if ( applet.getType() == RemusApplet.AGENT  ) {
 			//A submit to an agent is stored and a new instance is created
 			try {
 				BufferedReader br = new BufferedReader(new InputStreamReader(is));
 				String curline = null;
 				while ( (curline = br.readLine() ) != null ) {
 					Map inObj = (Map)serial.loads(curline);	
 					long jobID = Long.parseLong( inObj.get("id").toString() );
 					long emitID = (Long)inObj.get("order");
 					String key = (String)inObj.get("key");
 					Map value = (Map)inObj.get("value");					
 					RemusInstance inst = new RemusInstance();
 
 					//do some validation of the input work description
 					value.remove(RemusApplet.WORKDONE_OP);
 
 					System.err.println("AGENT SUBMISSION:" + key );
 					if ( ((Map)value).containsKey( Submission.AppletField ) ) {
 						List<String> aList = (List)((Map)value).get(Submission.AppletField);
 						inst = applet.getPipeline().setupInstance( key, (Map)value, aList );					
 					} else {
 						inst = applet.getPipeline().setupInstance( key, (Map)value, new LinkedList() );	
 					}					
 					((Map)value).put(Submission.InstanceField, inst.toString());	
 					applet.getPipeline().getDataStore().add( "/" + applet.getPipeline().getID() + "/@submit", 
 							RemusInstance.STATIC_INSTANCE_STR, 
 							(Long)0L, 
 							(Long)0L, 
 							key,
 							value );		
 					applet.getPipeline().getDataStore().add( "/" + applet.getPipeline().getID() + "/@instance", 
 							RemusInstance.STATIC_INSTANCE_STR, 
 							0L, 0L,
 							inst.toString(),
 							key);			
 				}				
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}		
 		} else {		
 			try {
 				Set outSet = new HashSet<Integer>();
 				BufferedReader br = new BufferedReader(new InputStreamReader(is));
 				String curline = null;
 				List<KeyValuePair> inputList = new ArrayList<KeyValuePair>();
 				while ( (curline = br.readLine() ) != null ) {
 					Map inObj = (Map)serial.loads(curline);	
 					long jobID = Long.parseLong( inObj.get("id").toString() );
 					outSet.add((int)jobID);
 					inputList.add( new KeyValuePair( jobID, 
 							(Long)inObj.get("order"), (String)inObj.get("key") , 
 							inObj.get("value") ) );
 				}
 				applet.getDataStore().add( applet.getPath(), 
 						inst.toString(),
 						inputList );
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	@Override
 	public BaseNode getChild(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
