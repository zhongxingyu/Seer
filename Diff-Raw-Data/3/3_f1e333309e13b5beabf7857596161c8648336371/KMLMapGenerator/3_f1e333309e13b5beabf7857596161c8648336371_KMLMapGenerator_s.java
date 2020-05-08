 package edu.usc.sirlab.kml;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import edu.usc.sirlab.*;
 import edu.usc.sirlab.db.*;
 
 public class KMLMapGenerator extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kml+xml";
 	private static final String DEFAULT_FILENAME_PREFIX = "QuakeTables-Query";
 	private static final String DEFAULT_FILENAME_SUFFIX = ".kml";
 	//private static final String KML_CONTENT_TYPE = "application/vnd.google-earth.kmz";
 	//private static final String KML_CONTENT_TYPE = "text/plain";
 	
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		DatabaseQuery dbQuery;
 		HttpSession session = request.getSession(true);
 		KMLDocument kml = new KMLDocument();
 		String fileName = DEFAULT_FILENAME_PREFIX + DEFAULT_FILENAME_SUFFIX;
 		int countInSAR = 0, countFault = 0, datasetCount = 0;
 		
 		try {
 			if(session.getAttribute("dbQuery") == null) {
 				dbQuery = new DatabaseQuery();
 				session.setAttribute("dbQuery", dbQuery);
 			}
 			else
 				dbQuery = (DatabaseQuery) session.getAttribute("dbQuery");
 			
 			//Look for DataSet requests
 			if(request.getParameterValues("ds") != null && request.getParameterValues("fid") == null) {
 				String[] dss = request.getParameterValues("ds");
 				if(dss.length == 1) {
 					FaultDataSet dataset = dbQuery.getFaultDataSet(dss[0]);
 					if(dataset != null) {
 						datasetCount++;
 						fileName = "QuakeTables_" + dataset.getNickName().replace(' ', '_') + ".kml";
 						LineStyle style;
 						if(request.getParameter("color") != null && request.getParameter("color").length() == 8)
 							style = new LineStyle("lineStyle", request.getParameter("color"), 3);
 						else
 							style = new LineStyle("lineStyle", 3);
 						kml.addStyle(style);
 						
 						if(dataset.getDataType().equalsIgnoreCase("cgs_fault")) {
 							List<CGSFault> faults = dbQuery.getCGSFaults(dataset.getId());
 							for(CGSFault f : faults) {
 								if(request.getParameter("f") != null && request.getParameter("f").equalsIgnoreCase("qt")) {
 									QuakeSimFault qsf = new QuakeSimFault(f);
 									dataset.addFaultKML(qsf.getKMLPlacemark(style));
 								}
 							}
 							
 						}
 						
 						kml.addFolder(dataset.getKMLFolder());
 						kml.setName(dataset.getTitle());
 						kml.setDescription(dataset.getDescription());
 					}
 				}
 				else {
 					//multiple data sets
 				}
 			}
 			
 			//Look for InSAR interferograms
 			if(request.getParameterValues("iid") != null) {
 				String[] iids = request.getParameterValues("iid");
 				boolean placeOverlay = true;
 				if(request.getParameter("ov") != null)
 					placeOverlay = false;
 				
 				if(iids.length == 1) {
 					if(iids[0].equalsIgnoreCase("all")) {
 						List<Interferogram> insar = dbQuery.getInerferograms();
 						fileName = "QuakeSim_InSAR.kml";
 						for(Interferogram i : insar) {
 							countInSAR++;
 							//kml.addFolder(i.getKMLPlacemark());
 							kml.addFolder(i.getKMLFolder(placeOverlay));
 							kml.setName("QuakeSim InSAR Map View");
 							kml.setDescription("QuakeSim InSAR Interferogram Map from QuakeTables");
 						}
 					}
 					else {
 						Interferogram insar = dbQuery.getInerferogram(iids[0]);
 						if(insar != null) {
 							countInSAR++;
 							fileName = insar.getDataURL().substring(insar.getDataURL().lastIndexOf('/') + 1) + ".kml";
 							kml.addFolder(insar.getKMLFolder(placeOverlay));
 							
 							kml.setName(insar.getTitle());
 							kml.setDescription(insar.getDescription());
 						}
 					}
 				}
 				else {
 					for(String iid : iids) {
 						Interferogram insar = dbQuery.getInerferogram(iid);
 						if(insar == null)
 							continue;
 						
 						countInSAR++;
 						kml.addFolder(insar.getKMLFolder(placeOverlay));
 					}
 				}
 			}
 			
 			//Look for Faults
 			if(request.getParameterValues("ds") != null && request.getParameter("fid") != null) {
 				String[] dss = request.getParameterValues("ds");
 				String[] fids = request.getParameterValues("fid");
 				if(dss.length == 1 && fids.length == 1) {
 					FaultDataSet dataset = dbQuery.getFaultDataSet(dss[0]);
 					if(dataset != null && dataset.getDataType().equalsIgnoreCase("cgs_fault")) {
 						CGSFault fault = dbQuery.getCGSFault(dataset.getId(), fids[0]);
 						if(fault != null) {
 							countFault++;
 							fileName = "QuakeTables_" + dataset.getNickName().replace(' ', '_') + "_" + fault.getId() + ".kml";
 							LineStyle style;
 							if(request.getParameter("color") != null && request.getParameter("color").length() == 8)
 								style = new LineStyle("lineStyle", request.getParameter("color"), 4);
 							else
 								style = new LineStyle("lineStyle", 4);
 							kml.addStyle(style);
 							
 							boolean placemark = false;
 							if(request.getParameter("mark") != null && request.getParameter("mark").equalsIgnoreCase("true"))
 								placemark = true;
 							
 							if(request.getParameter("f") != null && request.getParameter("f").equalsIgnoreCase("qt")) {
 								QuakeSimFault qsf = new QuakeSimFault(fault);
 								kml.addFolder(qsf.getKMLFolder(style, placemark));
 							}
 							
 							else
 								kml.addFolder(fault.getKMLFolder(style, placemark));
 							
 							kml.setName(fault.getName());
 							kml.setDescription("QuakeTables Fault Query on " + new Date());
 							
 						}
 					}
 				}
 				else {
 					//do for multiple faults
 				}
 			}
 			
 			if(countInSAR != 0 || countFault != 0 || datasetCount != 0) {
 				response.setContentType(KML_CONTENT_TYPE);
 				response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
 				ServletOutputStream out = response.getOutputStream();
 				out.println(kml.getKMLDocument());
 				out.flush();
 			}
 			else
 				response.sendRedirect("index.jsp");
 			
 		}catch (SQLException e) {
 		}catch (ClassNotFoundException e) {
 		}catch(InstantiationException e) {
 		}catch(IllegalAccessException e) {}
 		
 		
 		
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 }
