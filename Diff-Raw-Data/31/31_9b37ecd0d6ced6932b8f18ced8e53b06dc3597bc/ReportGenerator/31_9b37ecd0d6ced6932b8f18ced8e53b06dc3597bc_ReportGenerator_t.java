 /**
  * @(#)ReportGenerator.java
  */
 
 package aurora.hwc.report;
 
 import java.io.*;
 import java.util.Vector;
 import aurora.hwc.TypesHWC;
 import aurora.service.Updatable;
 
 
 /**
  * Create report files from gui configuration data.
  * @author Gabriel Gomes
  */
 public class ReportGenerator {
 	protected Configuration cfg;
 	protected Updatable updater = null;
 	protected int update_period = 5;
 	protected int max_percent = 100;
 	protected Vector<ReportSection> report = new Vector<ReportSection>();
 	protected AuroraCSVHeader header = new AuroraCSVHeader();
 
 	protected Vector<Integer> MLlinktypes = new Vector<Integer>();
 	protected Vector<Integer> ORlinktypes = new Vector<Integer>();
 	protected Vector<Integer> FRlinktypes = new Vector<Integer>();
 	protected Vector<Integer> STlinktypes = new Vector<Integer>();
 	
 	protected File reportfile = null; //new File(Configuration.getTempDir() + "\\detailed.xml");
 
 	public ReportGenerator(Configuration c){
 		cfg = c;
 		AuroraCSVFile.loadCommonHeader(cfg, header);
 		MLlinktypes.add(TypesHWC.LINK_FREEWAY);
 		MLlinktypes.add(TypesHWC.LINK_HOV);
 		MLlinktypes.add(TypesHWC.LINK_HIGHWAY);
 		MLlinktypes.add(TypesHWC.LINK_HOT);
 		MLlinktypes.add(TypesHWC.LINK_HV);
 		MLlinktypes.add(TypesHWC.LINK_ETC);
 		ORlinktypes.add(TypesHWC.LINK_ONRAMP);
 		ORlinktypes.add(TypesHWC.LINK_INTERCONNECT);
 		FRlinktypes.add(TypesHWC.LINK_OFFRAMP);
 		STlinktypes.add(TypesHWC.LINK_STREET);
 	}
 	
 	public void run(Configuration c){
 		
 		// create xml description of the report
 		try {
 			PerformanceCalculator perfcalc = new PerformanceCalculator(cfg, header);							
 			// Compile a high level description of the report
 			c.writeToConsole("\t+ Compiling slide list.");
 			GenerateHighLevel();
 
 			c.writeToConsole("\t+ Computing performance measures.");
 
 			// Apply the performance calculator to each element
 			PrintStream xml = new PrintStream(new FileOutputStream(reportfile));
 			xml.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 			xml.print("<Aurora_2_0_Report>\n");
 			xml.print("\t<ActualData>\n");
 			if (updater != null)
 				updater.notify_update((int)Math.round(10.0*(float)max_percent/100.0));
 			int total_slides = 0;
 			int slide_count = 0;
 			for (int i = 0; i < report.size(); i++)
 				total_slides += report.get(i).slides.size();
 			long sys_curr_time = System.currentTimeMillis();
 			long sys_lst_time = sys_curr_time;
 			for (int i = 0; i < report.size(); i++) {
 				xml.print("\t\t<Section title=\"" + report.get(i).title + "\">\n");
 				for (int j = 0; j < report.get(i).slides.size(); j++) {
 					c.writeToConsole("\t\t+ Slide " + report.get(i).slides.get(j).id);
 					for (int k = 0; k < report.get(i).slides.get(j).plots.size(); k++) {
 						c.writeToConsole("\t\t\t+ Plot " + k);
 						Plot thisplot = report.get(i).slides.get(j).plots.get(k);
 						thisplot.showlegend = cfg.cbx_dolegend;
 						thisplot.showlegend &= thisplot.elements.get(0).type!=PlotElement.Type.contour;
 						thisplot.makelabels(thisplot.elements.get(0));
 						perfcalc.processElements(thisplot.elements);
 					}
 					report.get(i).slides.get(j).xmlDump(xml);
 					slide_count++;
 					sys_curr_time = System.currentTimeMillis();
 					if ((sys_curr_time - sys_lst_time) >= 1000*update_period) {
 						sys_lst_time = sys_curr_time;
 						if (updater != null)
 							updater.notify_update((int)(Math.round(10.0*(float)max_percent/100.0) + Math.round(90*((float)max_percent/100.0)*((float)slide_count/total_slides))));
 					}
 				}
 				report.get(i).slides.clear();
 				xml.print("\t\t</Section>\n");	
 			}
 			xml.print("\t</ActualData>\n");
 			xml.print("</Aurora_2_0_Report>\n");
 			xml.close();
 
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	////////////////////////////////////////////////////////////////////////
 	// high level report description
 	////////////////////////////////////////////////////////////////////////
 	
 	public void GenerateHighLevel(){
 
 		if(cfg.reporttype==ReportType.scatter)
 			GenerateHighLevelScatter();
 		else
 			GenerateHighLevelStandard();
 	}
 
 	public void GenerateHighLevelStandard(){
 		
 		int slideid = 0;
 		PlotElement.Type standardplot = PlotElement.Type.none;
 		Vector<String> legendtext = new Vector<String>();
 		Vector<Slide> slides = new Vector<Slide>();
 
 		int i;
 		Vector<Plot> plots = new Vector<Plot>();		
 		Vector<PlotElement> plotelements = new Vector<PlotElement>();
 		
 		switch(cfg.reporttype){
 		case vehicletypes:
 	        standardplot = PlotElement.Type.multiline;
 	        legendtext = header.vehicletype;
 			break;
 		case aggvehicletypes:
 			standardplot = PlotElement.Type.multiline;	
			for(i=0;i<cfg.datafiles.size();i++)
				legendtext.add(cfg.datafiles.get(i));
 			break;
 		case bestworst:
 	        standardplot = PlotElement.Type.minmax;
 			break;	
 		}
 		
 		// Link state section ........................................
 		if(cfg.cbx_linkstate){
 			
 			slides.clear();
 
 			PlotElement E = new PlotElement(standardplot,cfg);
 			E.xquantity = Quantity.time;
 			E.setdatafiles(cfg.scenarios,cfg.datafiles);
 			E.setlegendtext(legendtext);
 			E.legend_precision = 0;
 			
 			for(i=0;i<header.links.size();i++){
 
 				plots.clear();
 				
 				E.setylinks(header.links.get(i).id);				
 
 				E.yquantity = Quantity.density;
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));		
 
 				E.yquantity = Quantity.speed;
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));		
 
 				E.yquantity = Quantity.flow;
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));		
 
 				slides.add(new Slide("Link " + header.links.get(i).id,plots,slideid++));
 			}
 			report.add(new ReportSection("Individual link state.",slides));
 		}
 
 		// System performance over time ........................................
 		if(cfg.cbx_sysperf){
 
 			slides.clear();
 			plots.clear();
 			
 			Vector<String> netlinkids = new Vector<String>();
 			getLinksIds("network",netlinkids);
 			Vector<String> mllinkids = new Vector<String>();
 			getLinksIds("freeway mainline",mllinkids);
 
 			PlotElement E = new PlotElement(standardplot,cfg);
 			E.xquantity = Quantity.time;
 			E.setdatafiles(cfg.scenarios,cfg.datafiles);
 			E.setlegendtext(legendtext);
 			E.legend_precision = 0;
 					
 			E.yquantity = Quantity.tvh;
 			E.legend_units = "veh.hr";
 			E.setylinks(netlinkids);
 			plotelements.clear();
 			plotelements.add(E.clone());
 			plots.add(new Plot(plotelements,Plot.Type.twoD));
 			slides.add(new Slide("Total Vehicle hours",plots,slideid++));
 
 			plots.clear();
 			E.yquantity = Quantity.tvm;
 			E.legend_units = "veh.mile";
 			plotelements.clear();
 			plotelements.add(E.clone());
 			plots.add(new Plot(plotelements,Plot.Type.twoD));
 			slides.add(new Slide("Total Vehicle Miles",plots,slideid++));
 
 			plots.clear();
 			E.yquantity = Quantity.mlvh;
 			E.legend_units = "veh.hr";
 			E.setylinks(mllinkids);
 			plotelements.clear();
 			plotelements.add(E.clone());
 			plots.add(new Plot(plotelements,Plot.Type.twoD));
 			slides.add(new Slide("Mainline Vehicle Hours",plots,slideid++));
 			
 			if(cfg.reporttype!=ReportType.vehicletypes){
 
 				plots.clear();
 				E.yquantity = Quantity.delay;
 				E.legend_units = "veh.hr";
 				E.setylinks(netlinkids);
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Total Delay",plots,slideid++));
 
 				plots.clear();
 				E.yquantity = Quantity.mldelay;
 				E.legend_units = "veh.hr";
 				E.setylinks(mllinkids);
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Total Mainline Delay",plots,slideid++));
 
 				plots.clear();
 				E.yquantity = Quantity.lostprod;
 				E.legend_units = "lane.mile.hr";
 				E.setylinks(mllinkids);
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Lost Mainline Productivity",plots,slideid++));
 			}
 			report.add(new ReportSection("System performance.",slides));
 		}
 		
 		// On-ramp performance over time ........................................
 		if(cfg.cbx_orperf_time){
 			
 			slides.clear();
 			
 			Vector<String> orlinkids = new Vector<String>();
 			getLinksIds("freeway onramps",orlinkids);
 			
 			PlotElement E = new PlotElement(standardplot,cfg);
 			E.xquantity = Quantity.time;
 			E.setdatafiles(cfg.scenarios,cfg.datafiles);
 			E.setylinks(orlinkids);
 			E.setlegendtext(legendtext);
 			E.legend_precision = 0;
 
 			plots.clear();
 			E.yquantity = Quantity.tvh;
 			E.legend_units = "veh.hr";
 			plotelements.clear();
 			plotelements.add(E.clone());
 			plots.add(new Plot(plotelements,Plot.Type.twoD));
 			slides.add(new Slide("Onramp Vehicle Hours",plots,slideid++));
 			
 			if(cfg.reporttype!=ReportType.vehicletypes){	
 				plots.clear();			
 				E.yquantity = Quantity.qoverride;
 				E.legend_units = "hr";
 				E.legend_precision = 1;
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Total Override Time",plots,slideid++));
 			}
 			report.add(new ReportSection("Onramp performance over time.",slides));
 		}
 
 		// On-ramp performance over space ........................................
 		if(cfg.cbx_orperf_space){
 			
 			slides.clear();
 			
 			Vector<String> orlinkids = new Vector<String>();
 			getLinksIds("freeway onramps",orlinkids);
 			
 			PlotElement E = new PlotElement(standardplot,cfg);
 			E.xquantity = Quantity.space;
 			E.setdatafiles(cfg.scenarios,cfg.datafiles);
 			E.setylinks(orlinkids);
 			E.setlegendtext(legendtext);
 			E.legend_precision = 0;
 
 			plots.clear();
 			E.yquantity = Quantity.tvh;
 			E.legend_units = "veh.hr";
 			plotelements.clear();
 			plotelements.add(E.clone());
 			plots.add(new Plot(plotelements,Plot.Type.twoD));
 			slides.add(new Slide("Onramp Vehicle Hours",plots,slideid++));
 
 			if(cfg.reporttype!=ReportType.vehicletypes){	
 				plots.clear();		
 				E.yquantity = Quantity.qoverride;
 				E.legend_units = "hr";
 				E.legend_precision = 1;
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Total Override Time",plots,slideid++));
 			}
 
 			report.add(new ReportSection("Onramp performance over space.",slides));
 		}
 		
 		// On-ramp performance contour ........................................
 		if(cfg.cbx_orperf_contour){
 			
 			slides.clear();
 			
 			Vector<String> orlinkids = new Vector<String>();
 			getLinksIds("freeway onramps",orlinkids);
 			
 			PlotElement E = new PlotElement(PlotElement.Type.contour,cfg);
 			E.setylinks(orlinkids);
 			
 			for(int q=0;q<legendtext.size();q++){
 				
 				switch(cfg.reporttype){
 				case vehicletypes:
 					E.setdatafiles(cfg.scenarios,cfg.datafiles);
 					E.keepSlice = q;
 					break;
 				case aggvehicletypes:
 					E.setdatafiles(cfg.scenarios.get(q),cfg.datafiles.get(q));
 					E.keepSlice = 0;
 					break;
 				}
 
 				plots.clear();
 				E.xquantity = Quantity.none;
 				E.yquantity = Quantity.tvh;
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Onramp Vehicle Hours for " + legendtext.get(q),plots,slideid++));
 
 				if(cfg.reporttype==ReportType.vehicletypes){
 					plots.clear();
 					E.xquantity = Quantity.none;
 					E.yquantity = Quantity.qoverride;
 					plotelements.clear();
 					plotelements.add(E.clone());
 					plots.add(new Plot(plotelements,Plot.Type.twoD));
 					slides.add(new Slide("Total Override Time for " + legendtext.get(q),plots,slideid++));
 				}
 			}
 
 			report.add(new ReportSection("Onramp performance contours.",slides));
 
 		}
 
 		// Route performance over time ........................................
 		if(cfg.cbx_routeperf_time){
 
 			slides.clear();
 			
 			PlotElement E = new PlotElement(standardplot,cfg);
 			E.xquantity = Quantity.time;
 			E.setdatafiles(cfg.scenarios,cfg.datafiles);
 			E.setlegendtext(legendtext);
 			E.legend_precision = 0;
 			
 			Vector<String> routelinkids = new Vector<String>();
 			for(i=0;i<header.routes.size();i++){
 				
 				getLinksIds(header.routes.get(i).name,routelinkids);
 				E.setylinks(routelinkids);
 
 				plots.clear();
 				E.yquantity = Quantity.tvh;
 				E.legend_units = "veh.hr";
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Vehicle Hours on Route" + header.routes.get(i).name,plots,slideid++));
 
 				plots.clear();
 				E.yquantity = Quantity.tvm;
 				E.legend_units = "veh.mile";
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Vehicle Miles on Route" + header.routes.get(i).name,plots,slideid++));
 
 				plots.clear();
 				E.yquantity = Quantity.delay;
 				E.legend_units = "veh.hour";
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Delay on Route" + header.routes.get(i).name,plots,slideid++));
 				
 				if(cfg.reporttype!=ReportType.vehicletypes){
 					
 					Vector<String> mllinkids = new Vector<String>();
 					getLinksIds("freeway mainline",mllinkids);
 
 					plots.clear();
 					E.yquantity = Quantity.lostprod;
 					E.legend_units = "lane.mile.hr";
 					E.setylinks(Utils.vectorintersect(routelinkids,mllinkids));
 					plotelements.clear();
 					plotelements.add(E.clone());
 					plots.add(new Plot(plotelements,Plot.Type.twoD));
 					slides.add(new Slide("Lost Mainline Productivity on Route on Route" + header.routes.get(i).name,plots,slideid++));
 				}
 				
 			}
 			report.add(new ReportSection("Route performance over time.",slides));
 		}
 		
 		// Route performance over space ........................................
 		if(cfg.cbx_routeperf_space){
 
 			slides.clear();
 			
 			PlotElement E = new PlotElement(standardplot,cfg);
 			E.xquantity = Quantity.space;
 			E.setdatafiles(cfg.scenarios,cfg.datafiles);
 			E.setlegendtext(legendtext);
 			E.legend_precision = 0;
 			
 			Vector<String> routelinkids = new Vector<String>();
 			for(i=0;i<header.routes.size();i++){
 				
 				getLinksIds(header.routes.get(i).name,routelinkids);
 				E.setylinks(routelinkids);
 
 				plots.clear();
 				E.yquantity = Quantity.tvh;
 				E.legend_units = "veh.hr";
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Vehicle Hours on Route" + header.routes.get(i).name,plots,slideid++));
 
 				plots.clear();
 				E.yquantity = Quantity.tvm;
 				E.legend_units = "veh.mile";
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Vehicle Miles on Route" + header.routes.get(i).name,plots,slideid++));
 
 				plots.clear();
 				E.yquantity = Quantity.delay;
 				E.legend_units = "veh.hour";
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Delay on Route" + header.routes.get(i).name,plots,slideid++));
 				
 				if(cfg.reporttype!=ReportType.vehicletypes){
 					Vector<String> mllinkids = new Vector<String>();
 					getLinksIds("freeway mainline",mllinkids);
 
 					plots.clear();
 					E.yquantity = Quantity.lostprod;
 					E.legend_units = "lane.mile.hr";
 					E.setylinks(Utils.vectorintersect(routelinkids,mllinkids));
 					plotelements.clear();
 					plotelements.add(E.clone());
 					plots.add(new Plot(plotelements,Plot.Type.twoD));
 					slides.add(new Slide("Lost Mainline Productivity on Route on Route" + header.routes.get(i).name,plots,slideid++));
 				}
 			}
 			report.add(new ReportSection("Route performance over space.",slides));
 
 		}
 		
 		// Route performance contour ........................................
 		if(cfg.cbx_routeperf_contour){
 			
 			slides.clear();
 			
 			PlotElement E = new PlotElement(PlotElement.Type.contour,cfg);
 			E.xquantity = Quantity.none;
 			
 			Vector<String> routelinkids = new Vector<String>();
 			for(int r=0;r<header.routes.size();r++){
 
 				getLinksIds(header.routes.get(r).name,routelinkids);
 				E.setylinks(routelinkids);
 				
 				for(int q=0;q<legendtext.size();q++){
 					
 					switch(cfg.reporttype){
 					case vehicletypes:
 						E.setdatafiles(cfg.scenarios,cfg.datafiles);
 						E.keepSlice = q;
 						break;
 					case aggvehicletypes:
 						E.setdatafiles(cfg.scenarios.get(q),cfg.datafiles.get(q));
 						E.keepSlice = 0;
 						break;
 					}
 
 					plots.clear();
 					E.yquantity = Quantity.density;
 					plotelements.clear();
 					plotelements.add(E.clone());
 					plots.add(new Plot(plotelements,Plot.Type.twoD));		
 
 					E.yquantity = Quantity.speed;
 					plotelements.clear();
 					plotelements.add(E.clone());
 					plots.add(new Plot(plotelements,Plot.Type.twoD));		
 
 					E.yquantity = Quantity.flow;
 					plotelements.clear();
 					plotelements.add(E.clone());
 					plots.add(new Plot(plotelements,Plot.Type.twoD));		
 
 					slides.add(new Slide("Route " + header.routes.get(r).name + " for " + legendtext.get(q),plots,slideid++));
 				}
 			}
 			report.add(new ReportSection("Route performance contours.",slides));
 		}
 		
 		// route travel time over time ........................................
 		if(cfg.cbx_routetraveltime){
 			
 			slides.clear();
 			
 			PlotElement E = new PlotElement(standardplot,cfg);
 			E.xquantity = Quantity.time;
 			E.setdatafiles(cfg.scenarios,cfg.datafiles);
 			E.setlegendtext(legendtext);
 			E.legend_precision = 0;
 			
 			Vector<String> routelinkids = new Vector<String>();
 			for(i=0;i<header.routes.size();i++){
 				
 				getLinksIds(header.routes.get(i).name,routelinkids);
 				E.setylinks(routelinkids);
 
 				plots.clear();
 				E.yquantity = Quantity.routetraveltime;
 				E.legend_units = "hr";
 				plotelements.clear();
 				plotelements.add(E.clone());
 				plots.add(new Plot(plotelements,Plot.Type.twoD));
 				slides.add(new Slide("Travel Time on Route" + header.routes.get(i).name,plots,slideid++));
 				
 			}
 			report.add(new ReportSection("Route travel time.",slides));
 		}
 
 		// route travel time contour  ........................................
 		if(cfg.cbx_routetrajectories){
 			
 			slides.clear();
 			/*
     SlideList = [];
     routes = CSVHeader.routes;
     for r = 1:length(routes)
         
         [z,routeindices]=ismember(routes(r).links,allIds);
         clear z
         
         P = makePlot(makeElement('space','routetrajectories',standardplot,2,routeindices,NaN,Qset,2,'hr',scenario,datafile));
         SlideList = [SlideList makeslide(['Travel time on Route ' num2str(r)'],P)];
         
     end
     
     SectionList = [SectionList makesection('Route trajectories',SlideList)];
     clear SlideList routes			 */
 		}
 
 	}
 
 	public void GenerateHighLevelScatter(){
 		
 		int i,j;
 		int slideid=0;
 		String slidetitle;
 		Vector<String> xlinks = new Vector<String>();
 		Vector<String> ylinks = new Vector<String>();
 		Vector<String> groupscenarios = new Vector<String>();
 		Vector<String> groupdatafiles = new Vector<String>();
 		Vector<Slide> slides = new Vector<Slide>();
 		Vector<Plot> plots = new Vector<Plot>();		
 		Vector<PlotElement> plotelements = new Vector<PlotElement>();
 
 		int numgroups = cfg.table_groups_group.size();
 		String customxaxislabel = cfg.txt_customxaxis;
 		boolean docustomxaxis = !customxaxislabel.isEmpty();
 		
 		Quantity xquantity = cfg.get_cmb_xaxis_quantity();
 		Quantity yquantity = cfg.get_cmb_yaxis_quantity();
 		
 		// xlinks and ylinks
 		String xaxis_subnetwork = cfg.cmb_xaxis_subnetwork.get(cfg.cmb_xaxis_subnetwork_selected);
 		String yaxis_subnetwork = cfg.cmb_yaxis_subnetwork.get(cfg.cmb_yaxis_subnetwork_selected);
 	    if(!docustomxaxis)
 	    	getLinksIds(xaxis_subnetwork,xlinks);
 		getLinksIds(yaxis_subnetwork,ylinks);
 		
 		// gather data files corresponding to each group
 		for(i=0;i<numgroups;i++){
 
 			String groupname = cfg.table_groups_group.get(i);
 			groupscenarios.clear();
 			groupdatafiles.clear();
 			
 			for(j=0;j<cfg.table_scenariogroups_scenario.size();j++){
 				String group = cfg.table_scenariogroups_group.get(j);
 				if(group.equals(groupname)){	
 					String thisscenario = cfg.table_scenariogroups_scenario.get(j);
 					for(int k=0;k<cfg.scenarios.size();k++){
 						if(cfg.scenarios.get(k).equals(thisscenario)){
 							groupscenarios.add(cfg.scenarios.get(k));
 							groupdatafiles.add(cfg.datafiles.get(k));	
 						}	
 					}
 				}
 			}
 
 			PlotElement E = new PlotElement(PlotElement.Type.scatter,cfg);
 
 			E.setylinks(ylinks);
 			E.setlegendtext(groupname);
 			E.setdatafiles(groupscenarios,groupdatafiles);
 			if(docustomxaxis){
 				E.customxvalue = Float.parseFloat(cfg.table_groups_xvalue.get(i));
 				E.xquantity = Quantity.none;
 				E.boxplot = cfg.cbx_boxplot;
 			}
 			else{
 				E.customxvalue = Float.NaN;
 				E.xquantity = xquantity;
 				E.boxplot = false;
 				E.setxlinks(xlinks);
 			}
 			E.yquantity = yquantity;	
 			plotelements.add(E);
 		}
 		plots.add(new Plot(plotelements,Plot.Type.twoD));	
 		if(docustomxaxis)
 			slidetitle = yaxis_subnetwork + " " + yquantity + " vs " + xaxis_subnetwork + " " + customxaxislabel;
 		else
 			slidetitle = yaxis_subnetwork + " " + yquantity + " vs " + xaxis_subnetwork + " " + xquantity;
 		slides.add(new Slide(slidetitle,plots,slideid++));
 		report.add(new ReportSection("Scatter plots",slides));
 		return;
 	}
 	
 	////////////////////////////////////////////////////////////////////////
 	// auxiliary methods
 	////////////////////////////////////////////////////////////////////////
 
 	public void getLinksIds(String subnet,Vector<String> out){
 		
 		int i;
 		out.clear();
 		Vector<AuroraCSVLink> L = header.links;
 		
 		if( subnet.equalsIgnoreCase("network")  ){
 			for(i=0;i<L.size();i++)
 				out.add(L.get(i).id);
 			return;
 		}
 
 		if( subnet.equalsIgnoreCase("freeway mainline")  ){
 			for(i=0;i<L.size();i++){
 				if(MLlinktypes.contains(L.get(i).type))
 					out.add(L.get(i).id);
 			}
 			return;
 		}
 
 		if( subnet.equalsIgnoreCase("freeway onramps")  ){
 			for(i=0;i<L.size();i++){
 				if(ORlinktypes.contains(L.get(i).type))
 					out.add(L.get(i).id);
 			}
 			return;
 		}
 
 		// net is a route:
 		for(i=0;i<header.routes.size();i++){
 			AuroraCSVRoute R = header.routes.get(i);
 			if( R.name.equals(subnet) ){
 				for(int j=0;j<R.linkids.size();j++)
 					out.add(R.linkids.get(j));
 			}
 		}
 	}
 	
 	/**
 	 * Sets report file.
 	 */
 	public synchronized void setReportFile(File rf) {
 		if (rf != null)
 			reportfile = rf;
 		return;
 	}
 	
 	/**
 	 * Sets progress updater.
 	 */
 	public synchronized void setUpdater(Updatable upd, int prd, int max_prgr) {
 		updater = upd;
 		update_period = Math.max(1, prd);
 		max_percent = max_prgr;
 		return;
 	}
 }
