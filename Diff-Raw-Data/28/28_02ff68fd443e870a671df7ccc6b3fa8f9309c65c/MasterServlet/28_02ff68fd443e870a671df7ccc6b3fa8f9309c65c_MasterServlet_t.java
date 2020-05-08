 package org.remus;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mpstore.JsonSerializer;
 import org.mpstore.KeyValuePair;
 import org.mpstore.MPStore;
 import org.mpstore.Serializer;
 import org.remus.applet.RemusApplet;
 
 public class MasterServlet extends HttpServlet {
 	RemusApp app;
 	Serializer serializer;
 	String workDir;
 	String srcDir;
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		try {
 			String mpStore = config.getInitParameter(RemusApp.configStore);
 			workDir = config.getInitParameter(RemusApp.configWork);
 			srcDir = config.getInitParameter(RemusApp.configSource);
 			serializer = new JsonSerializer();
 			Class<?> mpClass = Class.forName(mpStore);			
 			MPStore store = (MPStore) mpClass.newInstance();
 			store.init(serializer, workDir);			
 			app = new RemusApp(new File(srcDir), store);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RemusDatabaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8067165004515233805L;
 
 	Pattern appletSub = Pattern.compile("(\\:\\w+)\\.(\\w+)$");
 
	Pattern instancePat = Pattern.compile("^([^/]*)/([^/]*)$");
	Pattern instanceKeyPat = Pattern.compile("^([^/]*)/([^/]*)/(.*)$");
 	
 	class RequestInfo {
 		public String path;
 		public String appletPath;
 		public String appletSubName;
 		public String api;
 		public String instance;
 		public String key;
 		File file, srcFile;
 
 		public RequestInfo( String pathinfo ) {
 			String [] tmp = pathinfo.split("@");
 			path = tmp[0];
 			api = null;
 			instance = null;
 			appletSubName = null;
 			key = null;
 			if ( tmp.length > 1 ) {
				Matcher m1 = instancePat.matcher(tmp[1]);
				if ( m1.find() ) {
					api = m1.group(1);
					instance = m1.group(2);
				} else {
					Matcher m2 = instanceKeyPat.matcher(tmp[1]); 
					if ( m2.find() ) {
						api = m2.group(1);
						instance = m2.group(2);
						key = m2.group(3);
					} else {
						api = tmp[1];
 					}
 				}
 			}
			if ( api != null && api.length() == 0 )
				api = null;
 			Matcher m = appletSub.matcher( tmp[0] );
 			if ( m.find() ) {
 				String appletName = m.group(1);
 				appletSubName = m.group(2);
 				appletPath = m.replaceAll(appletName);
 			} else {
 				appletPath = tmp[0];
 			}
 			appletPath = (new File(appletPath)).getAbsolutePath();
 			file = new File( path );	
 			path = file.getAbsolutePath();
 			srcFile = new File(app.srcbase, path );		
 		}
 
 		public boolean isSubName() {
 			if ( appletSubName == null )
 				return false;
 			return true;
 		}
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {		
 		RequestInfo reqInfo = new RequestInfo( req.getPathInfo() );
 		if ( app.codeManager.containsKey( reqInfo.appletPath ) ) {
 			if ( reqInfo.api == null ) {
 				PrintWriter out = resp.getWriter();				
 				resp.setContentType( "text/html" );
 				out.println( "<p><a href='../'>MAIN</a></p>" );
 
 				out.println( "<p><a href='" + reqInfo.path + "@code'>CODE</a></p>" );
 
 				RemusApplet applet = app.codeManager.get(reqInfo.appletPath);
 				String instStr = reqInfo.instance;
 				RemusInstance curInst = null;
 				if ( instStr != null )
 					curInst = new RemusInstance(instStr);
 
 
 				out.println("INPUTS<ul>");
 				for ( InputReference iRef : applet.getInputs() ) {
 					if ( instStr != null )
 						out.println( "<li><a href='" + iRef.getPortPath() + "@/" + instStr + "'>" + iRef.getPortPath() + "</a></li>" );
 					else
 						out.println( "<li><a href='" + iRef.getPortPath() + "'>" + iRef.getPortPath() + "</a></li>" );
 				}
 				out.println("</ul>");
 
 				out.println("OUTPUT<ul>");
 				if ( instStr != null )
 					out.println( "<li><a href='" + applet.getPath() + "@/" + instStr + "'>" + applet.getPath() + "</a></li>" );
 				else
 					out.println( "<li><a href='" + applet.getPath() + "'>" + applet.getPath() + "</a></li>" );
 
 				for ( String output : applet.getOutputs() ) {
 					if ( instStr != null )
 						out.println( "<li><a href='" + applet.getPath() + "." + output + "@/" + instStr + "'>" + applet.getPath() + "." + output + "</a></li>" );
 					else
 						out.println( "<li><a href='" + applet.getPath() + "." + output + "'>" + applet.getPath() + "." + output + "</a></li>" );
 				}
 				out.println( "</ul>" );
 
 				if ( instStr != null ) {
 					if ( applet.isComplete(curInst ) ) {
 						out.println("<p>Work Complete</p>");
 					} else { 
 						if ( applet.isReady(curInst) ) {
 							if ( applet.isComplete(curInst) )
 								out.println("<p>Work Ready</p>" );
 							else
 								out.println("<p>Work Pending</p>" );
 						} else {
 							out.println("<p>Work Not Ready</p>" );							
 						}
 					}
 					out.println("<ul>");
 					out.println( "<li><a href='" + reqInfo.path + "@keys/"   + instStr + "'>KEYS</a></li>" );
 					out.println( "<li><a href='" + reqInfo.path + "@data/"   + instStr + "'>DATA</a></li>" );					
 					out.println( "<li><a href='" + reqInfo.path + "@reduce/" + instStr + "'>REDUCE</a></li>" );					
 					out.println("</ul>");
 				} else {
 					for ( RemusInstance inst : applet.getInstanceList() ) {
 						out.println( "<a href='" + reqInfo.path + "@/" + inst.toString() + "'>" + inst.toString() + "</a>" );
 					}
 				}
 			} else {
 				PrintWriter out = resp.getWriter();
 				resp.setBufferSize(2048);
 				if ( reqInfo.api.compareTo("data") == 0 || reqInfo.api.compareTo("submit") == 0 ) {
 					MPStore ds = app.getDataStore();
 					if ( reqInfo.instance != null && reqInfo.key != null ) {
 						String instStr = reqInfo.instance;
 						Object keyObj = serializer.loads(reqInfo.key);
 						if ( ds.containsKey( reqInfo.file + "@" + reqInfo.api, instStr, keyObj ) ) {
 							for ( Object value : ds.get( reqInfo.file + "@" + reqInfo.api, instStr, keyObj ) ) {
 								out.println( serializer.dumps( value ) );
 								resp.flushBuffer();
 							}
 						} else {
 							resp.sendError( HttpServletResponse.SC_NOT_FOUND );
 						}
 					} else if ( reqInfo.instance != null) {
 						for ( KeyValuePair kp : ds.listKeyPairs( reqInfo.file + "@" + reqInfo.api , reqInfo.instance ) ) {
 							Map outMap = new HashMap();
 							outMap.put(kp.getKey(), kp.getValue() );
 							out.println( serializer.dumps( outMap ) );
 							out.flush();
 							resp.flushBuffer();
 						}						
 					}
 				} else if ( reqInfo.api.compareTo("keys") == 0 && reqInfo.instance != null ) {
 					MPStore ds = app.getDataStore();
 					for ( Object key : ds.listKeys( reqInfo.file + "@data", reqInfo.instance ) ) {
 						out.println( serializer.dumps(key) );
 					}
 				} else if ( reqInfo.api.compareTo("code") == 0  ) {
 					out.write( app.codeManager.get(reqInfo.appletPath).getSource() );
 				} else if ( reqInfo.api.compareTo("work") == 0 ) {
 					RemusApplet applet = app.codeManager.get(reqInfo.appletPath);
 					if ( reqInfo.instance != null && reqInfo.key != null ) {
 						if ( applet.getPipeline() != null ) {
 							RemusInstance inst = new RemusInstance( reqInfo.instance );
 							int jobID = Integer.parseInt(reqInfo.key);
 							WorkDescription w = applet.getWork(inst, jobID);
 							if ( w != null )
 								out.println( serializer.dumps( w.getDesc() ) );
 						}
 					}
 				} else if ( reqInfo.api.compareTo("info") == 0 ) {
 					RemusApplet applet = app.codeManager.get( reqInfo.appletPath );
 					Map outMap = applet.getInfo();
 					out.print( serializer.dumps(outMap) );
 				} else if ( reqInfo.api.compareTo("reduce") == 0 ) {
 					MPStore ds = app.getDataStore();
 					//BUG FIX: SQLStore returns streaming iterator having a double loop of MPStore calls will
 					//crash the connection
 					List<Object> keyList = new LinkedList<Object>();
 					for ( Object key : ds.listKeys( reqInfo.file + "@data", reqInfo.instance ) ) {
 						keyList.add( key );
 					}					
 					for ( Object key : keyList ) {
 						Map outMap = new HashMap();
 						List outList = new ArrayList();
 						for ( Object val : ds.get(reqInfo.file + "@data", reqInfo.instance, key) ) {
 							outList.add(val);							
 						}
 						outMap.put(key, outList);
 						out.println( serializer.dumps( outMap ) );
 					}
 				}
 			}
 		} else if ( reqInfo.path.compareTo("/") == 0 ) {
 			if ( reqInfo.api == null ) {
 				PrintWriter out = resp.getWriter();
 				resp.setContentType( "text/html" );
 				out.println( "<h1>Pipelines:</h1> <ul>");
 				for ( int i =0; i < app.codeManager.getPipelineCount(); i++ ) {
 					RemusPipeline pipeline = app.codeManager.getPipeline(i);
 					if ( pipeline.dynamic )
 						out.println( "<li><h2><a href='/@pipeline?id=" + i + "'>Dynamic Pipeline " + i + "</a></h2></li>" );
 					else
 						out.println( "<li><h2><a href='/@pipeline?id=" + i + "'>Static Pipeline " + i + "</a></h2></li>" );
 					out.println("<h3>CodeList</h3><ul>");
 					for ( RemusApplet applet : pipeline.getMembers() ) {
 						out.println( "<li><a href='" + applet.getPath() + "'>" + applet.getPath() + "</a></li>" );
 					}
 					out.println("</ul>");
 				}
 				out.println("</ul>");
 
 				out.println( "<h1>Instances:</h1> <ul>");				
 				for ( Object key : app.codeManager.datastore.listKeys("/@submit", RemusInstance.STATIC_INSTANCE_STR )) {
 					out.println( "<li>" + key + "</li>" );
 				}
 				out.println("</ul>");
 
 
 			} else if ( reqInfo.api.compareTo("work") == 0 ) {
 				PrintWriter out = resp.getWriter();
 				int count = 10;
 				if ( req.getParameter("max") != null ) {
 					count = Integer.parseInt(req.getParameter("max"));
 				}
 				Map<String,Map<?,?>> outMap = new HashMap<String,Map<?,?>>();
 				for ( WorkDescription work : app.codeManager.getWorkQueue( count ) ) {
 					if ( !outMap.containsKey(work.getInstance().toString()) )
 						outMap.put( work.getInstance().toString(), new HashMap() );
 					Map iMap = outMap.get(work.getInstance().toString());
 					if ( !iMap.containsKey( work.getApplet().getPath() ) )
 						iMap.put( work.getApplet().getPath(), new ArrayList() );
 					List aList = (List)iMap.get( work.getApplet().getPath() );
 					aList.add(work.jobID);
 				}
 				out.print( serializer.dumps(outMap) );
 			} else if ( reqInfo.api.compareTo("submit") == 0 ) {
 				PrintWriter out = resp.getWriter();
 				out.print( serializer.dumps( (new RemusInstance()).toString()  ));
 			}
 		} else if (reqInfo.srcFile.exists() ) {
 			FileInputStream fis = new FileInputStream( reqInfo.srcFile );
 			ServletOutputStream os = resp.getOutputStream();
 			byte [] buffer = new byte[1024];
 			int len;
 			while ( (len = fis.read(buffer)) >= 0 ) {
 				os.write( buffer, 0, len );
 			}
 		} else {
 			PrintWriter out = resp.getWriter();
 			out.write( "Not Found:" + reqInfo.path );
 		}
 	}
 
 
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		RequestInfo reqInfo = new RequestInfo( req.getPathInfo() );		
 		if ( reqInfo.path.compareTo("/") == 0 ) {
 			if ( reqInfo.api.compareTo("restart") == 0 ) {
 				try {
 					app = new RemusApp(new File(srcDir), app.workStore );
 				} catch (RemusDatabaseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} else if ( app.codeManager.containsKey( reqInfo.appletPath ) ) {
 			if ( reqInfo.api != null ) {
 				if ( reqInfo.api.compareTo("work") == 0 ) {
 					BufferedReader br = req.getReader();
 					String curline = null;
 					while ((curline=br.readLine())!= null ) {
 						Map m = (Map)serializer.loads( curline );
 						for ( Object key : m.keySet() ) {
 							String instStr = (String)key;
 							RemusInstance inst=new RemusInstance(instStr);
 							List jobList = (List)m.get(key);
 							RemusApplet applet = app.codeManager.get( reqInfo.appletPath );
 							for ( Object key2 : jobList ) {
 								Long jobID = (Long)key2;
 								applet.finishWork(inst,jobID.intValue() );
 							}						
 						}
 					}
 					resp.getWriter().print("\"OK\"");
 				} else if ( reqInfo.api.compareTo("data") == 0 ) {
 					BufferedReader br = req.getReader();
 					String curline = null;
 					while ( (curline = br.readLine() ) != null ) {
 						String writeName = null;
 						if ( reqInfo.appletSubName != null )
 							writeName = reqInfo.appletPath + "." + reqInfo.appletSubName;
 						else
 							writeName = reqInfo.appletPath;
 						Map inObj = (Map)serializer.loads(curline);	
 						app.workStore.add( writeName + "@data", 
 								(String)inObj.get("instance"), 
 								(Long)inObj.get("id"), 
 								(Long)inObj.get("order"), 
 								inObj.get("key") , 
 								inObj.get("value") );
 					}
 					resp.getWriter().print("\"OK\"");
 				} else if ( reqInfo.api.compareTo("submit") == 0) {
 					RemusApplet applet = app.codeManager.get(reqInfo.appletPath);
 					if ( applet.getInputs().size() == 1 && applet.getInputs().get(0).getInputType() == InputReference.DynamicInput ) {
 						boolean found = false;
 						String submitFile = reqInfo.appletPath + "@submit";
 						for ( Object instStr : app.workStore.listKeys( submitFile,  reqInfo.instance ) ) {
 							found = true;
 						}
 						if ( found ) {
 							resp.sendError( HttpServletResponse.SC_FORBIDDEN );
 						} else {
 							BufferedReader br = req.getReader();
 							String curline = null;
 							while ( (curline = br.readLine() ) != null ) {
 								Map inObj = (Map)serializer.loads(curline);	
 								for ( Object key : inObj.keySet() ) {
 									app.workStore.add(submitFile, 
 											reqInfo.instance, 
 											(Long)0L, 
 											(Long)0L, 
 											key , 
 											inObj.get(key) );
 								}
 							}
 							app.workStore.add( "/@submit", 
 									RemusInstance.STATIC_INSTANCE_STR, 
 									(Long)0L, 
 									(Long)0L, 
 									reqInfo.instance, 
 									reqInfo.appletPath );
 							applet.getPipeline().addInstance( new RemusInstance(reqInfo.instance) );
 							resp.getWriter().print("\"OK\"");
 						}
 					} else {
 						resp.sendError( HttpServletResponse.SC_NOT_FOUND );
 					}
 				} else if ( reqInfo.api.compareTo("attach") == 0 ) {
 					if ( reqInfo.instance != null && reqInfo.key != null ) {
 						app.getDataStore().writeAttachment( reqInfo.file.toString(), reqInfo.instance, reqInfo.key, req.getInputStream() );
 					}
 				}
 			}
 		} 
 	}
 
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 
 		RequestInfo reqInfo = new RequestInfo( req.getPathInfo() );		
 		if ( app.codeManager.containsKey( reqInfo.appletPath ) ) {
 			RemusApplet applet = app.codeManager.get(reqInfo.appletPath);
 			if ( reqInfo.instance != null  ) {
 				app.getDataStore().delete( reqInfo.appletPath + "@work", reqInfo.instance );
 				app.getDataStore().delete( reqInfo.appletPath + "@data", reqInfo.instance );
 				for ( String subname : applet.getOutputs() ) {
 					app.getDataStore().delete( reqInfo.appletPath + "." + subname + "@data", reqInfo.instance );
 				}
 				app.getDataStore().delete( reqInfo.appletPath + "@done", RemusInstance.STATIC_INSTANCE_STR, reqInfo.instance );
 				try {
 					app = new RemusApp(new File(srcDir), app.workStore );
 				} catch (RemusDatabaseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}	
 	}
 }
 
 
 
 
