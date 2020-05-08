 package org.remus;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
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
 import org.remus.applet.RemusApplet;
 
 public class MasterServlet extends HttpServlet {
 	RemusApp app;
 	WorkManager workManage;
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
 			workManage = new WorkManager(app);
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
 		RemusPath reqInfo = new RemusPath( app, req.getPathInfo() );
 		if ( app.codeManager.containsKey( reqInfo.getAppletPath() ) ) {
 			if ( reqInfo.getView() == null ) {
 				PrintWriter out = resp.getWriter();				
 				resp.setContentType( "text/html" );
 				out.println( "<p><a href='../'>MAIN</a></p>" );
 
 				out.println( "<p><a href='" + reqInfo.getAppletPath() + "@code'>CODE</a></p>" );
 
 				RemusApplet applet = app.codeManager.get(reqInfo.getAppletPath());
 				String instStr = reqInfo.getInstance();
 				RemusInstance curInst = null;
 				if ( instStr != null )
 					curInst = new RemusInstance(instStr);
 
 				for ( RemusPath iref : applet.getInputs() )	{
 					if ( iref.getInputType() == RemusPath.DynamicInput ) {
 						out.println("SUBMISSION<ul>");
 						MPStore ds = app.getDataStore();
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
 						MPStore ds = app.getDataStore();
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
 					MPStore ds = app.getDataStore();
 					if ( reqInfo.getInstance() != null && reqInfo.getKey() != null ) {
 						String instStr = reqInfo.getInstance();
 						String keyStr = reqInfo.getKey();
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
 					} else if ( reqInfo.getInstance() != null) {
 						for ( KeyValuePair kp : ds.listKeyPairs( reqInfo.getPortPath() + "@" + reqInfo.getView() , reqInfo.getInstance() ) ) {
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
 					MPStore ds = app.getDataStore();
 					for ( Object key : ds.listKeys( reqInfo.getPortPath() + "@data", reqInfo.getInstance() ) ) {
 						out.println( serializer.dumps(key) );
 					}
 				} else if ( reqInfo.getView().compareTo("code") == 0  ) {
 					PrintWriter out = resp.getWriter();
 					resp.setBufferSize(2048);
 					out.write( app.codeManager.get( reqInfo.getAppletPath() ).getSource() );
 				} else if ( reqInfo.getView().compareTo("work") == 0 ) {
 					PrintWriter out = resp.getWriter();
 					resp.setBufferSize(2048);
 					RemusApplet applet = app.codeManager.get( reqInfo.getAppletPath() );
 					if ( reqInfo.getInstance() != null && reqInfo.getKey() != null ) {
 						if ( applet.getPipeline() != null ) {
 							RemusInstance inst = new RemusInstance( reqInfo.getInstance() );
 							int jobID = Integer.parseInt(reqInfo.getKey());
 							String workerID = getWorkerID(req);
 							if ( workerID != null ) {
 								Object workDesc = workManage.getWork( workerID, applet, inst, jobID );
 								if ( workDesc != null )
 									out.println( serializer.dumps( workDesc) );
 							}
 						}
 					}
 				} else if ( reqInfo.getView().compareTo("info") == 0 ) {
 					PrintWriter out = resp.getWriter();
 					resp.setBufferSize(2048);
 					RemusApplet applet = app.codeManager.get( reqInfo.getAppletPath() );
 					Map outMap = applet.getInfo();
 					out.print( serializer.dumps(outMap) );
 				} else if ( reqInfo.getView().compareTo("reduce") == 0 ) {
 					PrintWriter out = resp.getWriter();
 					resp.setBufferSize(2048);
 					MPStore ds = app.getDataStore();
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
 						MPStore ds = app.getDataStore();
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
 						MPStore ds = app.getDataStore();
 						List<String> outList = new ArrayList<String>();
 						for ( String val : ds.listKeys(reqInfo.getAppletPath() + "@attach", reqInfo.getInstance() ) ) {
 							outList.add(val);
 						}
 						out.println( serializer.dumps( outList ) );
 					}
 				} else if ( reqInfo.getView().compareTo("instance") == 0 ) {
 					PrintWriter out = resp.getWriter();
 					MPStore ds = app.getDataStore();
 					List outList = new LinkedList();
 					for ( String key : ds.listKeys( reqInfo.getAppletPath() + "@instance", RemusInstance.STATIC_INSTANCE_STR) ) {
 						outList.add(key);
 					}
 					out.println( serializer.dumps( outList ) );
 				}
 			}
 		} else if ( reqInfo.getAppletPath().compareTo("/") == 0 ) {
 			if ( reqInfo.getView() == null ) {
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
 						out.println("<ul>");
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
 				for ( String workerID : workManage.workerSets.keySet() ) {
 					Map curMap = new HashMap();
 					curMap.put("activeCount", workManage.workerSets.get(workerID).size() );
 					curMap.put("lastContact", workManage.lastAccess.get(workerID).toString() );
 					workerMap.put(workerID, curMap );	
 				}
 				outMap.put( "workers", workerMap );
 				outMap.put( "workBufferSize", workManage.workQueue.size() );
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
 		} else if (reqInfo.getSrcFile().exists() ) {
 			FileInputStream fis = new FileInputStream( reqInfo.getSrcFile() );
 			ServletOutputStream os = resp.getOutputStream();
 			byte [] buffer = new byte[1024];
 			int len;
 			while ( (len = fis.read(buffer)) >= 0 ) {
 				os.write( buffer, 0, len );
 			}
 		} else {
 			PrintWriter out = resp.getWriter();
 			out.write( "Not Found:" + reqInfo.getURL() );
 		}
 	}
 
 
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		RemusPath reqInfo = new RemusPath(app, req.getPathInfo() );		
 		if ( reqInfo.getAppletPath().compareTo("/") == 0 ) {
 			if ( reqInfo.getView().compareTo("restart") == 0 ) {
 				try {
 					app = new RemusApp(new File(srcDir), app.workStore );
 				} catch (RemusDatabaseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else if ( reqInfo.getView().compareTo("kickstart") == 0 ) {
 				app.kickStart();
 			}
 		} else if ( app.codeManager.containsKey( reqInfo.getAppletPath() ) ) {
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
 								RemusApplet applet = app.codeManager.get( reqInfo.getAppletPath() );
 								for ( Object key2 : jobList ) {
 									Long jobID = (Long)key2;
 									workManage.finishWork(workerID, applet, inst, jobID);
 								}						
 							}
 						}
 						resp.getWriter().print("\"OK\"");
 					}
 				} else if ( reqInfo.getView().compareTo("data") == 0 && reqInfo.getInstance() != null) {
 					RemusApplet applet = app.codeManager.get(reqInfo.getAppletPath());
 
					applet.formatInput( reqInfo, req.getInputStream(), serializer );
 					
 					
 					resp.getWriter().print("\"OK\"");
 				} else if ( reqInfo.getView().compareTo("submit") == 0) {
 					RemusApplet applet = app.codeManager.get(reqInfo.getAppletPath());
 
 					boolean found = false;
 					String submitFile = reqInfo.getAppletPath() + "@submit";
 					for ( Object instStr : app.workStore.listKeys( submitFile,  reqInfo.getInstance() ) ) {
 						found = true;
 					}
 					if ( found ) {
 						resp.sendError( HttpServletResponse.SC_FORBIDDEN );
 					} else {
 						BufferedReader br = req.getReader();
 						String curline = br.readLine();
 						app.workStore.add( submitFile, 
 								RemusInstance.STATIC_INSTANCE_STR, 
 								(Long)0L, 
 								(Long)0L, 
 								reqInfo.getInstance(), 
 								curline );
 						applet.getPipeline().addInstance( new RemusInstance(reqInfo.getInstance() ) );
 						resp.getWriter().print("\"OK\"");
 					}
 
 				} else if ( reqInfo.getView().compareTo("attach") == 0 ) {
 					if ( reqInfo.getInstance() != null && reqInfo.getKey() != null ) {
 						app.getDataStore().writeAttachment( reqInfo.getAppletPath() + "@attach", reqInfo.getInstance(), reqInfo.getKey(), req.getInputStream() );
 					}
 				}
 			}
 		} 
 	}
 
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException {
 		RemusPath reqInfo = new RemusPath(app, req.getPathInfo() );		
 		if ( app.codeManager.containsKey( reqInfo.getAppletPath() ) ) {
 			RemusApplet applet = app.codeManager.get(reqInfo.getAppletPath());
 			if ( reqInfo.getInstance() != null  ) {
 				applet.deleteInstance( new RemusInstance( reqInfo.getInstance()) );
 				try {
 					app = new RemusApp(new File(srcDir), app.workStore );
 				} catch (RemusDatabaseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} else if ( reqInfo.getAppletPath().compareTo("/") == 0 ) {
 			if ( reqInfo.getView().compareTo("submit") == 0 && reqInfo.getInstance() != null ) {
 				app.getDataStore().delete( "/@submit", RemusInstance.STATIC_INSTANCE_STR, reqInfo.getInstance() );
 			} else if ( reqInfo.getView().compareTo("instance") == 0 && reqInfo.getInstance() != null ) {
 				RemusInstance instance = new RemusInstance(reqInfo.getInstance());
 				for ( RemusPipeline pipeline : app.codeManager.getPipelines() ) {
 					pipeline.deleteInstance(instance);
 				}
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
 
 
 
 
