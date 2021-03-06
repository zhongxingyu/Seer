 package org.remus;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mpstore.JsonSerializer;
 import org.mpstore.KeyValuePair;
 import org.mpstore.MPStore;
 import org.mpstore.Serializer;
 import org.remus.manage.WorkManager;
 import org.remus.work.RemusApplet;
 
 public class MasterServlet extends HttpServlet {
 	RemusApp app;
 	WorkManager workManage;
 	Serializer serializer;
 	//	String workDir;
 	String srcDir;
 	Map<String,String> configMap;
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		try {
 			configMap = new HashMap<String,String>();			
 			Enumeration names = config.getInitParameterNames();
 			while ( names.hasMoreElements() ) {
 				String name = (String) names.nextElement();
 				configMap.put(name, config.getInitParameter(name));
 			}
 			serializer = new JsonSerializer();
 			srcDir = configMap.get( RemusApp.configSource );
 			app = new RemusApp(new File(srcDir), configMap);
 			workManage = new WorkManager(app);
 
 		} catch (RemusDatabaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8067165004515233805L;
 
 
 	private String getWorkerID( HttpServletRequest req ) {
 		String workerID = null;
 		if ( req.getCookies() != null ) {
 			for ( Cookie cookie : req.getCookies() ) {
 				if ( cookie.getName().compareTo( "remusWorker" ) == 0 ) {
 					workerID = cookie.getValue();
 				}
 			}
 		}
 		return workerID;
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {		
 		RemusPath reqInfo = new RemusPath( app, req.getRequestURI() );
 		if ( reqInfo.getApplet() != null ) {
 			if ( app.hasApplet( reqInfo.getAppletPath() ) ) {
 				if ( reqInfo.getView() == null ) {
 					PrintWriter out = resp.getWriter();				
 					resp.setContentType( "text/html" );
 					out.println( "<p><a href='../'>MAIN</a></p>" );
 
 					out.println( "<p><a href='" + reqInfo.getAppletPath() + "@code'>CODE</a></p>" );
 
 					RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 					String instStr = reqInfo.getInstance();
 					RemusInstance curInst = null;
 					if ( instStr != null ) {
 						curInst = new RemusInstance(applet.getDataStore(), instStr);
 						instStr = curInst.toString();
 					}
 					for ( RemusPath iref : applet.getInputs() )	{
 						if ( iref.getInputType() == RemusPath.DynamicInput ) {
 							out.println("SUBMISSION<ul>");
 							MPStore ds = applet.getDataStore();
 							for ( KeyValuePair kv : ds.listKeyPairs(applet.getPath() + "@submit", RemusInstance.STATIC_INSTANCE_STR)  ) {
 								out.println( "<li>" + kv.getKey() + " " + kv.getValue() + "</li>" );
 							}
 
 							out.println("</ul>");
 
 						}
 					}
 					out.println("INPUTS<ul>");
 					for ( RemusPath iRef : applet.getInputs() ) {
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
 						if ( applet.isReady(curInst) || applet.getType() == RemusApplet.STORE ) {
 							out.println("<ul>");
 							out.println( "<li><a href='" + reqInfo.getPortPath() + "@keys/"   + instStr + "'>KEYS</a></li>" );
 							out.println( "<li><a href='" + reqInfo.getPortPath() + "@data/"   + instStr + "'>DATA</a></li>" );					
 							out.println( "<li><a href='" + reqInfo.getPortPath() + "@reduce/" + instStr + "'>REDUCE</a></li>" );					
 							out.println("</ul>");
 						}
 						if ( applet.getType() == RemusApplet.PIPE ) {
 							MPStore ds = applet.getDataStore();
 							boolean first = true;
 							for ( String key : ds.listKeys( applet.getPath() + "@attach", curInst.toString() ) ) {
 								if ( first ) {
 									out.println( "<p>Attachments</p>" );
 									first = false;
 								}
 								out.println( "<li><a href='" + reqInfo.getAppletPath() + "@attach/"   + instStr + "/" + key + "'>" + key + "</a></li>" );
 							}
 						}					
 					} else {
 						for ( RemusInstance inst : applet.getInstanceList() ) {
 							out.println( "<li><a href='" + reqInfo.getPortPath() + "@/" + inst.toString() + "'>" + inst.toString() + "</a></li>" );
 						}
 					}
 				} else {
 					if ( reqInfo.getView().compareTo("data") == 0 || reqInfo.getView().compareTo("submit") == 0 ) {
 						PrintWriter out = resp.getWriter();
 						resp.setBufferSize(2048);
 						RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 						MPStore ds = applet.getDataStore();
 						if ( reqInfo.getInstance() != null && reqInfo.getKey() != null ) {
 							String instStr = (new RemusInstance(ds, reqInfo.getInstance())).toString();
 							String keyStr = reqInfo.getKey();
 							String sliceStr = req.getParameter("slice");
 							if ( sliceStr == null ) {
 								if ( ds.containsKey( reqInfo.getPortPath() + "@" + reqInfo.getView(), instStr, keyStr ) ) {
 									for ( Object value : ds.get( reqInfo.getPortPath() + "@" + reqInfo.getView(), instStr, keyStr ) ) {
 										Map oMap = new HashMap();
 										oMap.put( keyStr, value);
 										out.println( serializer.dumps( oMap ) );
 										resp.flushBuffer();
 									}
 								} else {
 									resp.sendError( HttpServletResponse.SC_NOT_FOUND );
 								}
 							} else {
 								int sliceSize = Integer.parseInt(sliceStr);
 								for ( String sliceKey : ds.keySlice( reqInfo.getPortPath() + "@" + reqInfo.getView(), instStr, keyStr, sliceSize) ) {
 									for ( Object value : ds.get( reqInfo.getPortPath() + "@" + reqInfo.getView(), instStr, sliceKey ) ) {
 										Map oMap = new HashMap();
 										oMap.put( sliceKey, value);
 										out.println( serializer.dumps( oMap ) );
 										resp.flushBuffer();
 									}
 								}
 							}
 						} else if ( reqInfo.getInstance() != null) {
 							String instStr = (new RemusInstance(ds, reqInfo.getInstance())).toString();
 							for ( KeyValuePair kp : ds.listKeyPairs( reqInfo.getPortPath() + "@" + reqInfo.getView() , instStr ) ) {
 								Map outMap = new HashMap();
 								outMap.put(kp.getKey(), kp.getValue() );
 								out.println( serializer.dumps( outMap ) );
 								out.flush();
 								resp.flushBuffer();
 							}						
 						}
 					} else if ( reqInfo.getView().compareTo("keys") == 0 && reqInfo.getInstance() != null ) {
 						PrintWriter out = resp.getWriter();
 						resp.setBufferSize(2048);
 						RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 						MPStore ds = applet.getDataStore();
 						String instStr = (new RemusInstance(ds, reqInfo.getInstance())).toString();
 						for ( Object key : ds.listKeys( reqInfo.getPortPath() + "@data", instStr ) ) {
 							out.println( serializer.dumps(key) );
 						}
 					} else if ( reqInfo.getView().compareTo("code") == 0  ) {
 						PrintWriter out = resp.getWriter();
 						resp.setBufferSize(2048);
 						out.write( app.getApplet( reqInfo.getAppletPath() ).getSource() );				
 					} else if ( reqInfo.getView().compareTo("info") == 0 ) {
 						PrintWriter out = resp.getWriter();
 						resp.setBufferSize(2048);
 						RemusApplet applet = app.getApplet( reqInfo.getAppletPath() );
 						Map outMap = applet.getInfo();
 						out.print( serializer.dumps(outMap) );
 					} else if ( reqInfo.getView().compareTo("reduce") == 0 ) {
 						PrintWriter out = resp.getWriter();
 						resp.setBufferSize(2048);
 						RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 						MPStore ds = applet.getDataStore();
 						for ( String key : ds.listKeys( reqInfo.getPortPath() + "@data", reqInfo.getInstance() ) ) {
 							Map outMap = new HashMap();
 							List outList = new ArrayList();
 							for ( Object val : ds.get(reqInfo.getPortPath() + "@data", reqInfo.getInstance(), key) ) {
 								outList.add(val);							
 							}
 							outMap.put(key, outList);
 							out.println( serializer.dumps( outMap ) );
 						} 
 					} else if ( reqInfo.getView().compareTo("attach") == 0 ) {
 						if ( reqInfo.getInstance() != null && reqInfo.getKey() != null ) {
 							RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 							MPStore ds = applet.getDataStore();
 							InputStream is = ds.readAttachement( reqInfo.getAppletPath() + "@attach", reqInfo.getInstance(), reqInfo.getKey() );
 							ServletOutputStream os = resp.getOutputStream();
 							byte [] buffer = new byte[1024];
 							int len;
 							while ( (len = is.read(buffer)) >= 0 ) {
 								os.write( buffer, 0, len );
 							}
 							os.close();
 						} else {
 							PrintWriter out = resp.getWriter();
 							RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 							MPStore ds = applet.getDataStore();
 							List<String> outList = new ArrayList<String>();
 							for ( String val : ds.listKeys(reqInfo.getAppletPath() + "@attach", reqInfo.getInstance() ) ) {
 								outList.add(val);
 							}
 							out.println( serializer.dumps( outList ) );
 						}
 					} else if ( reqInfo.getView().compareTo("instance") == 0 ) {
 						PrintWriter out = resp.getWriter();
 						RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 						MPStore ds = applet.getDataStore();
 						List outList = new LinkedList();
 						for ( String key : ds.listKeys( reqInfo.getAppletPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR) ) {
 							outList.add(key);
 						}
 						out.println( serializer.dumps( outList ) );
 					}
 				}
 			}
 		} else {
 			if ( reqInfo.getView() == null ) {
 				PrintWriter out = resp.getWriter();
 				resp.setContentType( "text/html" );
 				out.println( "<h1>Pipelines:</h1> <ul>");
 				for ( RemusPipeline pipeline : app.pipelines.values() ) {
 					out.println( "<li><h2><a href='/@pipeline/" + pipeline.id + "'>Pipeline " + pipeline.id + "</a></h2></li>" );
 					out.println("<h3>CodeList</h3><ul>");
 					for ( RemusApplet applet : pipeline.getMembers() ) {
 						out.print( "<li><a href='" + applet.getPath() + "'>" + applet.getPath() + "</a>" );
 						if ( applet.getInput().getInputType() == RemusPath.DynamicInput ) {
 							out.print(" - Input Applet");
 						}
 						if ( applet.getInput().getInputType() == RemusPath.StaticInput ) {
 							out.print(" - Static Applet");
 						}
 						out.println("</li><ul>");
 						for ( RemusInstance appInst : applet.getInstanceList() ) {
 							out.print( "<li><a href='" + applet.getPath() + "@/" + appInst.toString() + "'>" +
 									appInst.toString() + "</a> - ");
 							if ( applet.isReady(appInst) ) {
 								if ( applet.isComplete(appInst) ) {
 									out.print( "Complete");
 								} else {
 									out.print( "Ready");									
 								}
 							} else {
 								out.print( "Waiting");
 							}
 							out.print( "</li>" );
 						}
 						out.println("</ul>");
 					}
 					out.println("</ul>");
 				}
 				out.println("</ul>");
 
 				/*
 				out.println( "<h1>Submission:</h1> <ul>");				
 				for ( String key : app.codeManager.datastore.listKeys("/@submit", RemusInstance.STATIC_INSTANCE_STR )) {
 					out.println( "<li>" + key + "</li>" );
 					out.println( "<ul>" );
 					for ( Object path : app.codeManager.datastore.get( "/@submit", RemusInstance.STATIC_INSTANCE_STR, key) ) {
 						out.println( "<li>" + path + "</li>" );											
 					}
 					out.println( "</ul>" );					
 				}
 				out.println("</ul>");
 				 */
 
 			} else if ( reqInfo.getView().compareTo("pipeline") == 0 ) {
 				if ( reqInfo.getInstance() != null ) {
 					RemusPipeline pipe = app.pipelines.get(reqInfo.getInstance() );
 					PrintWriter out = resp.getWriter();
 					Map outMap = new HashMap();
 					for ( RemusApplet applet : pipe.getMembers() ) {
 						Map pipeMap = new HashMap();
 						List instList = new ArrayList();
 						for ( RemusInstance inst : applet.getInstanceList() ) {
 							instList.add( inst.toString() );
 						}
 						pipeMap.put("instance", instList );
 						outMap.put(applet.getID(), pipeMap);
 					}
 					out.print( serializer.dumps(outMap) );
 
 				} else {			
 					PrintWriter out = resp.getWriter();
 					Map outMap = new HashMap();
 					for ( String pipeline : app.pipelines.keySet() ) {
 						Map pipeMap = new HashMap();
 						outMap.put(pipeline, pipeMap);
 					}
 					out.print( serializer.dumps(outMap) );
 				}
 			} else if ( reqInfo.getView().compareTo("work") == 0 ) {
 				PrintWriter out = resp.getWriter();
 				int count = 10;
 				if ( req.getParameter("max") != null ) {
 					count = Integer.parseInt(req.getParameter("max"));
 				}
 				String workerID = getWorkerID(req);
 				if ( workerID != null ) {
 					Object outVal = workManage.getWorkMap( workerID, count );
 					out.print( serializer.dumps(outVal) );
 				} else {
 					resp.sendError( HttpServletResponse.SC_NOT_FOUND );
 				}
 			} else if ( reqInfo.getView().compareTo("submit") == 0 ) {
 				PrintWriter out = resp.getWriter();
 				out.print( serializer.dumps( (new RemusInstance()).toString()  ));
 			} else if ( reqInfo.getView().compareTo("status") == 0 ) {
 				PrintWriter out = resp.getWriter();
 				Map outMap = new HashMap();				
 				Map workerMap = new HashMap();
 				for ( String workerID : workManage.getWorkers()) {
 					//TODO: put in more methods to access work manager statistics
 					Map curMap = new HashMap();
 					//curMap.put("activeCount", workManage.workerSets.get(workerID).size() );
 					//curMap.put("lastContact", workManage.lastAccess.get(workerID).toString() );
 					workerMap.put(workerID, curMap );	
 				}
 				outMap.put( "workers", workerMap );
 				//outMap.put( "workBufferSize", workManage.workQueue.size() );
 				outMap.put("finishRate", workManage.getFinishRate() );
 				out.print( serializer.dumps(outMap) );
 			}
 			/*
 			else if ( reqInfo.api.compareTo("list") == 0 ) {
 				List outList = new LinkedList();
 				for ( String key : app.codeManager.datastore.listKeys("/@submit", RemusInstance.STATIC_INSTANCE_STR )) {
 					outList.add(key);
 				}
 				PrintWriter out = resp.getWriter();
 				out.print( serializer.dumps( outList ) );
 			}
 			 */
 		} /* else {
 			PrintWriter out = resp.getWriter();
 			out.write( "Not Found:" + reqInfo.getPath() );
 		} */
 	}
 
 
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		RemusPath reqInfo = new RemusPath(app, req.getPathInfo() );		
 		if ( reqInfo.getApplet() == null ) {
 			if ( reqInfo.getView().compareTo("restart") == 0 ) {
 				try {
 					app = new RemusApp(new File(srcDir),configMap );
 				} catch (RemusDatabaseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}  /* else if ( reqInfo.getView().compareTo("alias") == 0 && reqInfo.getInstance() != null ) {
 				BufferedReader br = req.getReader();
 				String curline = br.readLine();
 				if ( !app.getDataStore().containsKey("/@alias", RemusInstance.STATIC_INSTANCE_STR, curline) ) {
 					app.getDataStore().add("/@alias", RemusInstance.STATIC_INSTANCE_STR, 0L, 0L, curline, reqInfo.getInstance() );
 				}
 			} */
 		} else if ( app.hasApplet( reqInfo.getAppletPath() ) ) {
 			if ( reqInfo.getView() != null ) {
 				if ( reqInfo.getView().compareTo("work") == 0 ) {
 					String workerID = getWorkerID(req);
 					if ( workerID != null ) {
 						BufferedReader br = req.getReader();
 						String curline = null;
 						while ((curline=br.readLine())!= null ) {
 							Map m = (Map)serializer.loads( curline );
 							for ( Object key : m.keySet() ) {
 								String instStr = (String)key;
 								RemusInstance inst=new RemusInstance(instStr);
 								List jobList = (List)m.get(key);
 								RemusApplet applet = app.getApplet( reqInfo.getAppletPath() );
 								for ( Object key2 : jobList ) {
 									long jobID = Long.parseLong( key2.toString() );
 									//TODO:add emit id count check
 									workManage.finishWork(workerID, applet, inst, (int)jobID, 0L);
 								}						
 							}
 						}
 						resp.getWriter().print("\"OK\"");
 					}
 				} else if ( reqInfo.getView().compareTo("data") == 0 && reqInfo.getInstance() != null) {
 					RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 					applet.formatInput( reqInfo, req.getInputStream(), serializer );
 					resp.getWriter().print("\"OK\"");
 				} else if ( reqInfo.getView().compareTo("submit") == 0) {
 					RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 
 					boolean found = false;
 					String submitFile = reqInfo.getAppletPath() + "@submit";
 					String instStr = null;
 					if ( reqInfo.getInstance() != null ) {
 						instStr = (new RemusInstance(applet.getDataStore(), reqInfo.getInstance() )).toString(); 
 					} else {
 						instStr = (new RemusInstance()).toString();
 					}
 					for ( Object instmp : applet.getDataStore().listKeys( submitFile,  instStr ) ) {
 						found = true;
 					}
 					if ( found ) {
 						resp.sendError( HttpServletResponse.SC_FORBIDDEN );
 					} else {
 						BufferedReader br = req.getReader();
 						String curline = br.readLine();	
 						applet.submit(new RemusInstance(instStr), new RemusPath(app, curline)) ;
 						resp.getWriter().print("{\"" + instStr + "\":\"OK\"}");
 					}
 
 				} else if ( reqInfo.getView().compareTo("attach") == 0 ) {
 					RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 					if ( reqInfo.getInstance() != null && reqInfo.getKey() != null ) {
 						applet.getDataStore().writeAttachment( reqInfo.getAppletPath() + "@attach", reqInfo.getInstance(), reqInfo.getKey(), req.getInputStream() );
 					}
 				}
 			}
 		} 
 	}
 
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		RemusPath reqInfo = new RemusPath(app, req.getPathInfo() );	
 		if ( reqInfo.getApplet() == null ) {
 			if ( reqInfo.getView().compareTo("instance") == 0 && reqInfo.getInstance() != null ) {
 				RemusInstance instance = new RemusInstance(reqInfo.getInstance());
 				for ( RemusPipeline pipeline : app.pipelines.values() ) {
 					pipeline.deleteInstance(instance);
 				}
 				try {
 					app = new RemusApp(new File(srcDir), configMap );
 				} catch (RemusDatabaseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} else if ( app.hasApplet( reqInfo.getAppletPath() ) ) {
 			RemusApplet applet = app.getApplet(reqInfo.getAppletPath());
 			if ( reqInfo.getInstance() != null  ) {
 				applet.deleteInstance( new RemusInstance( reqInfo.getInstance()) );
 				try {
 					app = new RemusApp(new File(srcDir), configMap );
 				} catch (RemusDatabaseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} 
 	}
 }
 
 
 
 
