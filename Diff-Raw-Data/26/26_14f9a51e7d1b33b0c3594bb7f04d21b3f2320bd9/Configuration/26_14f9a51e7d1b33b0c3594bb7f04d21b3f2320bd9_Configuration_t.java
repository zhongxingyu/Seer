 /**
  * @(#)Configuration.java
  */
 
 package aurora.hwc.report;
 
 import java.io.*;
 import java.util.Vector;
 import org.w3c.dom.Node;
 import aurora.*;
 
 
 /**
  * AuroraConfigurable class for exporting the GUI configuration data.
  * @author Gabriel Gomes
  */
 public class Configuration implements AuroraConfigurable {
 
 	public Utils.ExporterType exporttype;
 	public ReportType reporttype;
 	public int maxpointspercurve;
 	public float congspeed;
 	public float timeto;
 	public float timefrom;
 	public Vector<String> scenarios = new Vector<String>();
 	public Vector<String> datafiles = new Vector<String>();
 	
 	public boolean cbx_orperf_time;
 	public boolean cbx_orperf_space;
 	public boolean cbx_orperf_contour;
 	public boolean cbx_routeperf_time;
 	public boolean cbx_routeperf_space;	
 	public boolean cbx_routeperf_contour;
 	public boolean cbx_routetraveltime;
 	public boolean cbx_routetrajectories;
 	public boolean cbx_dolegend;
 	public boolean cbx_dofill;
 	public boolean cbx_linkstate;
 	public boolean cbx_sysperf;
 	public String txt_outputfile;
 	public String txt_customxaxis;	
 	public boolean cbx_boxplot;	
 	public Vector<String> colors = new Vector<String>();
 	public Vector<String> table_groups_group = new Vector<String>();
 //	public Vector<String> table_groups_marker = new Vector<String>();
 //	public Vector<String> table_groups_markersize = new Vector<String>();
 	public Vector<String> table_groups_xvalue = new Vector<String>();
 	public Vector<String> table_scenariogroups_scenario = new Vector<String>();
 	public Vector<String> table_scenariogroups_group = new Vector<String>();
 	public int cmb_xaxis_subnetwork_selected;
 	public Vector<String> cmb_xaxis_subnetwork = new Vector<String>();
 	public int cmb_xaxis_quantity_selected;
 	public Vector<String> cmb_xaxis_quantity = new Vector<String>();
 	public int cmb_yaxis_subnetwork_selected;
 	public Vector<String> cmb_yaxis_subnetwork = new Vector<String>();
 	public int cmb_yaxis_quantity_selected;
 	public Vector<String> cmb_yaxis_quantity = new Vector<String>();
 	public Vector<String> chk_tree = new Vector<String>();
 	
 	protected String tempDir = System.getProperty("user.home") + "\\ARG\\tempfiles";
 
 	public Quantity get_cmb_yaxis_quantity(){
 		String str = cmb_yaxis_quantity.get(cmb_yaxis_quantity_selected);
 		if(str.equals("Vehicle hours"))
 			return Quantity.tvh;
 		if(str.equals("Vehicle miles"))
 			return Quantity.tvm;
 		if(str.equals("Delay"))
 			return Quantity.delay;
 		return Quantity.none;
 	}
 
 	public Quantity get_cmb_xaxis_quantity(){
 		String str = cmb_xaxis_quantity.get(cmb_xaxis_quantity_selected);
 		if(str.equals("Vehicle hours"))
 			return Quantity.tvh;
 		if(str.equals("Vehicle miles"))
 			return Quantity.tvm;
 		if(str.equals("Delay"))
 			return Quantity.delay;
 		return Quantity.none;
 	}
 	
 	@Override
 	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		String nodename,str;
 		Node n1;
 				
 		if ((p == null) || (!p.hasChildNodes()))
 			return false;
 		try {
 			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
 				nodename = p.getChildNodes().item(i).getNodeName();
 				n1 = p.getChildNodes().item(i);
 				if (nodename.equals("cmb_export"))
 					exporttype = Utils.exportString2Type.get(n1.getTextContent());
 				if (nodename.equals("cmb_reporttype"))
					reporttype = Utils.reportString2Type.get(n1.getTextContent().toLowerCase());
 				if (nodename.equals("chk_tree"))
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),chk_tree);	
 				if (nodename.equals("scenarios"))
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),scenarios);	
 				if (nodename.equals("datafiles"))
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),datafiles);	
 				if (nodename.equals("BatchList")) {
 					Node pp = p.getChildNodes().item(i);
 					if (pp.hasChildNodes())
 						for (int j = 0; j < pp.getChildNodes().getLength(); j++) {
 							if (pp.getChildNodes().item(j).getNodeName().equals("batch")) {
 								Node cl = pp.getChildNodes().item(j);
 								Node name_attr = cl.getAttributes().getNamedItem("name");
 								String batch_name = "";
 								if (name_attr != null)
 									batch_name = name_attr.getNodeValue();
 								if (cl.hasChildNodes())
 									for (int jj = 0; jj < cl.getChildNodes().getLength(); jj++) {
 										if (cl.getChildNodes().item(jj).getNodeName().equals("data_file")) {
 											Node url_attr = cl.getChildNodes().item(jj).getAttributes().getNamedItem("url");
 											if (url_attr != null) {
 												scenarios.add(batch_name);
 												datafiles.add(url_attr.getNodeValue());
 											}
 										}
 									}
 							}
 						}
 				}
 				if (nodename.equals("txt_outputfile"))
 					txt_outputfile = n1.getTextContent();
 
 				if (nodename.equals("txt_timefrom")) {
 					str = n1.getTextContent();
 					if(Utils.isNumber(str))
 						timefrom = Float.parseFloat(str) / (float)3600.0;
 					else
 						timefrom = Float.NaN;
 				}
 				if (nodename.equals("txt_timeto")) {
 					str = n1.getTextContent();
 					if(Utils.isNumber(str))
 						timeto = Float.parseFloat(str) / (float)3600.0;
 					else
 						timefrom = Float.NaN;
 				}
 				if (nodename.equals("cbx_linkstate"))
 					cbx_linkstate = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_sysperf"))
 					cbx_sysperf = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_orperf_time"))
 					cbx_orperf_time = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_orperf_space"))
 					cbx_orperf_space = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_orperf_contour"))
 					cbx_orperf_contour = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_routeperf_time"))
 					cbx_routeperf_time = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_routeperf_space"))
 					cbx_routeperf_space = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_routeperf_contour"))
 					cbx_routeperf_contour = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_routetraveltime"))
 					cbx_routetraveltime = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_routetrajectories"))
 					cbx_routetrajectories = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("txt_congspeed")){
 					str = n1.getTextContent();
 					if(Utils.isNumber(str))
 						congspeed = Float.parseFloat(str);
 					else
 						congspeed = Float.NaN;
 				}
 				if (nodename.equals("txt_maxpointspercurve")){
 					str = n1.getTextContent();
 					if(Utils.isInteger(str))
 						maxpointspercurve = Integer.parseInt(str);
 					else
 						maxpointspercurve = Integer.MAX_VALUE;
 				}
 				if (nodename.equals("cbx_dolegend"))
 					cbx_dolegend = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cbx_dofill"))
 					cbx_dofill = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("colors"))
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),colors);	
 				if (nodename.equals("tbl_scenariogroups")){
 					table_scenariogroups_group.clear();
 					table_scenariogroups_scenario.clear();
 					for (int j = 0; j < n1.getChildNodes().getLength(); j++) {
 						Node n2 = n1.getChildNodes().item(j);
 						if(n2.getNodeName().equals("entry")){
 							table_scenariogroups_group.add(n2.getAttributes().getNamedItem("group").getNodeValue());
 							table_scenariogroups_scenario.add(n2.getAttributes().getNamedItem("scenario").getNodeValue());
 						}
 					}
 				}
 				if (nodename.equals("tbl_groups")) {
 					table_groups_group.clear();
 //					table_groups_marker.clear();
 //					table_groups_markersize.clear();
 					table_groups_xvalue.clear();
 					for (int j = 0; j < n1.getChildNodes().getLength(); j++) {
 						Node n2 = n1.getChildNodes().item(j);
 						if(n2.getNodeName().equals("entry")){
 							table_groups_group.add(n2.getAttributes().getNamedItem("group").getNodeValue());
 //							table_groups_marker.add(n2.getAttributes().getNamedItem("marker").getNodeValue());
 //							table_groups_markersize.add(n2.getAttributes().getNamedItem("markersize").getNodeValue());
 							table_groups_xvalue.add(n2.getAttributes().getNamedItem("xvalue").getNodeValue());	
 						}						
 					}
 				}
 				if (nodename.equals("txt_customxaxis"))
 					txt_customxaxis = n1.getTextContent();
 				if (nodename.equals("cbx_boxplot"))
 					cbx_boxplot = Boolean.parseBoolean(n1.getTextContent());
 				if (nodename.equals("cmb_xaxis_subnetwork")){
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),cmb_xaxis_subnetwork) ;
 					cmb_xaxis_subnetwork_selected = Integer.parseInt(n1.getAttributes().getNamedItem("selected").getNodeValue());
 				}
 				if (nodename.equals("cmb_xaxis_quantity")){
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),cmb_xaxis_quantity) ;
 					cmb_xaxis_quantity_selected =  Integer.parseInt(n1.getAttributes().getNamedItem("selected").getNodeValue());
 				}
 				if (nodename.equals("cmb_yaxis_subnetwork")){
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),cmb_yaxis_subnetwork) ;
 					cmb_yaxis_subnetwork_selected = Integer.parseInt(n1.getAttributes().getNamedItem("selected").getNodeValue());;
 				}
 				if (nodename.equals("cmb_yaxis_quantity")){
 					Utils.readMatlabFormattedStringVector(n1.getTextContent(),cmb_yaxis_quantity) ;
 					cmb_yaxis_quantity_selected =  Integer.parseInt(n1.getAttributes().getNamedItem("selected").getNodeValue());
 				}
 			}
 		}
 		catch(Exception e) {
 			throw new ExceptionConfiguration(e.getMessage());
 		}
 		return true;
 	}
 	
 	@Override
 	public void xmlDump(PrintStream out) throws IOException {
 		out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<guidata>\n");
 		out.print("\t<cmb_export>" + Utils.exportType2String.get(exporttype) +  "</cmb_export>\n");
 		out.print("\t<cmb_reporttype>" + Utils.reportType2String.get(reporttype) +  "</cmb_reporttype>\n");
 		out.print("\t<txt_congspeed>" + congspeed +  "</txt_congspeed>\n");
 		out.print("\t<txt_maxpointspercurve>" + maxpointspercurve +  "</txt_maxpointspercurve>\n");	
 		out.print("\t<cbx_orperf_time>" +  cbx_orperf_time +  "</cbx_orperf_time>\n");
 		out.print("\t<cbx_orperf_space>" + cbx_orperf_space  +  "</cbx_orperf_space>\n");
 		out.print("\t<cbx_orperf_contour>" + cbx_orperf_contour +  "</cbx_orperf_contour>\n");
 		out.print("\t<cbx_routeperf_time>" + cbx_routeperf_time  +  "</cbx_routeperf_time>\n");
 		out.print("\t<cbx_routeperf_space>" + cbx_routeperf_space +  "</cbx_routeperf_space>\n");
 		out.print("\t<cbx_routeperf_contour>" + cbx_routeperf_contour +  "</cbx_routeperf_contour>\n");
 		out.print("\t<cbx_routetraveltime>" + cbx_routetraveltime +  "</cbx_routetraveltime>\n");
 		out.print("\t<cbx_routetrajectories>" + cbx_routetrajectories +  "</cbx_routetrajectories>\n");
 		out.print("\t<cbx_dolegend>" + cbx_dolegend +  "</cbx_dolegend>\n");
 		out.print("\t<cbx_dofill>" + cbx_dofill +  "</cbx_dofill>\n");
 		out.print("\t<cbx_linkstate>" + cbx_linkstate +  "</cbx_linkstate>\n");
 		out.print("\t<cbx_sysperf>" + cbx_sysperf +  "</cbx_sysperf>\n");
 		out.print("\t<txt_outputfile>" + txt_outputfile +  "</txt_outputfile>\n");
 		out.print("\t<txt_timefrom>" + 3600*timefrom +  "</txt_timefrom>\n");
 		out.print("\t<txt_timeto>" + 3600*timeto +  "</txt_timeto>\n");
 		out.print("\t<txt_customxaxis>" + txt_customxaxis +  "</txt_customxaxis>\n");
 		out.print("\t<cbx_boxplot>" + cbx_boxplot +  "</cbx_boxplot>\n");
 		out.print("\t<colors>" + Utils.writeMatlabFormattedVector(colors) + "</colors>\n");
 		out.print("\t<tbl_groups>\n");
 		for (int i = 0; i < table_groups_group.size(); i++) {
 			out.print("\t\t<entry ");
 			out.print("group=\"" + table_groups_group.get(i) + "\" ");
 //			out.print("marker=\"" + table_groups_marker.get(i) + "\" ");
 //			out.print("markersize=\"" + table_groups_markersize.get(i) + "\" ");
 			out.print("xvalue=\"" + table_groups_xvalue.get(i) + "\" ");
 			out.print("/>\n");
 		}
 		out.print("\t</tbl_groups>\n");
 
 		out.print("\t<tbl_scenariogroups>\n");
 		for (int i = 0; i < table_scenariogroups_scenario.size(); i++) {
 			out.print("\t\t<entry ");
 			out.print("scenario=\"" + table_scenariogroups_scenario.get(i) + "\" ");
 			out.print("group=\"" + table_scenariogroups_group.get(i) + "\" ");
 			out.print("/>\n");
 		}
 		out.print("\t</tbl_scenariogroups>\n");
 		
 		out.print("\t<cmb_xaxis_subnetwork selected=\"" + cmb_xaxis_subnetwork_selected);
 		out.print("\">" + Utils.writeMatlabFormattedVector(cmb_xaxis_subnetwork));
 		out.print("</cmb_xaxis_subnetwork>\n");
 		
 		out.print("\t<cmb_xaxis_quantity selected=\"" + cmb_xaxis_quantity_selected);
 		out.print("\">" + Utils.writeMatlabFormattedVector(cmb_xaxis_quantity));
 		out.print("</cmb_xaxis_quantity>\n");
 
 		out.print("\t<cmb_yaxis_subnetwork selected=\"" + cmb_yaxis_subnetwork_selected);
 		out.print("\">" + Utils.writeMatlabFormattedVector(cmb_yaxis_subnetwork));
 		out.print("</cmb_yaxis_subnetwork>\n");
 
 		out.print("\t<cmb_yaxis_quantity selected=\"" + cmb_yaxis_quantity_selected);
 		out.print("\">" + Utils.writeMatlabFormattedVector(cmb_yaxis_quantity));
 		out.print("</cmb_yaxis_quantity>\n");
 
 		out.print("\t<chk_tree>" + Utils.writeMatlabFormattedVector(chk_tree) + "</chk_tree>\n");
 		/* FIXME: to be removed
 		out.print("\t<scenarios>" + Utils.writeMatlabFormattedVector(scenarios) + "</scenarios>\n");
 		out.print("\t<datafiles>" + Utils.writeMatlabFormattedVector(datafiles) + "</datafiles>\n"); */
 		int j = 0;
 		String batch_name = "";
 		if (!scenarios.isEmpty())
 			batch_name = scenarios.firstElement();
 		out.print("\t<BatchList>\n");
 		out.print("\t\t<batch name=\"" + batch_name + "\">\n");
 		while (j < datafiles.size()) {
 			if (!scenarios.get(j).equals(batch_name)) {
 				out.print("\t\t</batch>\n");
 				batch_name = scenarios.get(j);
 				out.print("\t\t<batch name=\"" + batch_name + "\">\n");
 			}
 			out.print("\t\t\t<data_file url=\"" + datafiles.get(j) + "\" />\n");
 		}
 		out.print("\t\t</batch>\n");
 		out.print("\t</BatchList>\n");
 		out.print("</guidata>\n");			
 	}
 	
 	public void check() throws Exception {
 
 		int i;
 		String str;
 		
 		boolean nonselected = !cbx_linkstate && !cbx_sysperf &&
 							  !cbx_orperf_time && !cbx_routeperf_time &&
 							  !cbx_routetrajectories && !cbx_routeperf_space &&
 							  !cbx_orperf_space && !cbx_orperf_contour &&
 							  !cbx_routeperf_contour && !cbx_routetraveltime;
 				
 		if(nonselected && reporttype!=ReportType.scatter)
 			throw(new Exception("Error: Please select plots before proceding."));
 		
 		if(timeto<=timefrom)
 			throw(new Exception("Error: \"Time to\" must be greater than \"Time from\"."));
 		
 		if(reporttype==ReportType.scatter){
 
 			for(i=0;i<table_scenariogroups_group.size();i++){
 				if(table_scenariogroups_group.get(i).trim().isEmpty())
 					throw(new Exception("Error: All scenarios must be assigned to a group."));
 			}
 							
 			for(i=0;i<table_groups_group.size();i++){
 				
 	//			str = table_groups_marker.get(i).toString().trim();
 	//			if(str.isEmpty())
 	//				throw(new Exception("Error: All groups must be assigned a marker."));
 				
 	//			str = table_groups_markersize.get(i).toString().trim();
 	//			if(str.isEmpty() | !Utils.isInteger(str) )
 	//				throw(new Exception("Error: All groups must be assigned an integer marker size."));
 				
 				str = table_groups_xvalue.get(i).toString().trim();
 				if(!txt_customxaxis.isEmpty() & ( str.isEmpty() || !Utils.isNumber(str) ) )
 					throw(new Exception("Error: All groups must be assigned a numeric custom X axis value."));
 			}			
 		}
 	}
 	
 	public boolean xmlValidate() throws ExceptionConfiguration {
 		return true;
 	}
 
 	@Override
 	public boolean validate() throws ExceptionConfiguration {
 		// TODO Auto-generated method stub
 		return true;
 	}
 	
 	/**
 	 * Return temporary directory name.
 	 */
 	public String getTempDir() {
 		return tempDir;
 	}
 	
 	/**
 	 * Set temporary directory name.
 	 */
 	public synchronized void setTempDir(String tmp) {
 		if ((tmp != null) && (!tmp.isEmpty()))
 			tempDir = tmp;
 		return;
 	}
 }
