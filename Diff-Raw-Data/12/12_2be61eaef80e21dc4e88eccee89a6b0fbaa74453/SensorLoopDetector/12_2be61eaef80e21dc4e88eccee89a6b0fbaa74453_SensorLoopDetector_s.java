 /**
  * @(#)SensorLoopDetector.java
  */
 
 package aurora.hwc;
 
 import java.io.*;
 import java.util.*;
 
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import aurora.*;
 import aurora.hwc.fdcalibration.*;
 
 /**
  * Implementation of loop detector.
  * @author Gabriel Gomes
  * $Id: SensorLoopDetector.java 156 2011-03-29 03:37:11Z cratershine $
  */
 public final class SensorLoopDetector extends AbstractSensor {
 	private static final long serialVersionUID = 5743495158840080833L;
 
 //	 ========================================================================
 //	 FIELDS =================================================================
 //	 ========================================================================
 	
 	private String data_id;
 	private int vds = 0;
 	private String hwy_name;
 	private String hwy_dir;
 	private String postmile;
 	private int lanes=0;
 	private String link_type;
 	
 	private double cumflow = 0.0;
 	private boolean iscounter = true;		// GCG Make this input/output
 	private double flow = 0.0;
 	private double dens = 0.0;
 	private double speed = 0.0;
 	private int count = 0;
 	
 	private double looplength = 0.0;
 	private double vehiclelength = 17.0/5280.0;
 	private AbstractLinkHWC myHWCLink = null;
 	//private AbstractNode bnd;
 	//private AbstractNode end;
 	
 	// data files
 	protected double start_time = 0.0;
 	protected Vector<HistoricalDataSource> data_files = new Vector<HistoricalDataSource>();
 	protected Node data_sources_node; // save this so that we can replicate the urls in xmldump;
 	
 	
 	
 	// calibrated parameters
 	public float vf;		// [mph]
 	public float w;			// [mph]
 	public float q_max;		// [miles/hr/lane]
 	public float rj;		// [veh/hr/lane]
 	public float rho_crit;  // [veh/hr/lane]
 	
 
 //	 ========================================================================
 //	 INTERFACE ==============================================================
 //	 ========================================================================
 	
 //	public int getLanes() {
 //		return lanes;
 //	}
 	
 	public boolean IsCounter() 	 { return iscounter; } 
 //	-------------------------------------------------------------------------
 	public boolean ReceivedCall(double dt){
 		double lambda;
 		lambda = (speed*dt+vehiclelength+looplength)*dens;
 		if( Math.random() < 1-Math.exp(-lambda) )
 			return true;
 		else
 			return false;
 	}
 	
 	public void setFD(float vfin,float win,float q_maxin,float rjin,float rho_critin){
 		vf = vfin;
 		w = win;
 		q_max = q_maxin;
 		rj = rjin;
 		rho_crit = rho_critin;  
 
 	}
 	
 //	-------------------------------------------------------------------------
 	public double Flow()	 { return flow; }
 //	-------------------------------------------------------------------------
 	public double Density()	 { return dens; }
 //	-------------------------------------------------------------------------
 	public double Length()	 { return looplength; }
 //	-------------------------------------------------------------------------
 	public double Speed()	 { return speed; }
 //	-------------------------------------------------------------------------
	public double Occupancy() { return dens / myHWCLink.getJamDensity(); }
 //	-------------------------------------------------------------------------
 	public int Count() { return count; }
 //	-------------------------------------------------------------------------
 	public int getVDS() { return vds; }
 //	-------------------------------------------------------------------------
 	public void ResetCount(){
 		count = 0;
 	}
 
 //	 ========================================================================
 //	 METHODS AND OVERRIDES===================================================
 //	 ========================================================================
 	/**
 	 * Updates SensorLoopDetector data.<br>
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		boolean res = true;
 		res &= super.dataUpdate(ts);
 		
 		flow = myHWCLink.getFlow().sum().getCenter();	
 		dens = myHWCLink.getDensity().sum().getCenter();
 		speed = myHWCLink.getSpeed().getCenter();
 		
 		if(iscounter){
 			cumflow += flow*myNetwork.getTP();
 			if(cumflow>=1.0){
 				count   += Math.floor(cumflow);
 				cumflow -= Math.floor(cumflow);
 			}
 		}
 		
 		return res;
 	}
 //	-------------------------------------------------------------------------
 	/*
 	public void interpValues(){
 
 		Object value;
 		
 		// update flow ...............................
 
 		double ifl,ofl;	
 		Object q;
 		q = bnd.getOutputs().get(bnd.getSuccessors().indexOf(myLink));		// GCG don't assume bnd!=null
 		if(q==null)
 			ifl = 0.0;
 		else
 			ifl = (Double) q;
 		
 		q = end.getInputs().get(end.getPredecessors().indexOf(myLink));
 		if(q==null)
 			ofl = 0.0;
 		else
 			ofl = (Double) q;
 		flow = ((myLink.getLength()-linkPosition)*ifl + linkPosition*ofl)/myLink.getLength();
 		return flow;
 
 		value = myHWCLink.getActualFlow();
 		if(value!=null)
 			flow = (Double) value;
 		else
 			flow = 0.0;
 		
 		
 		// update density ...............................
 		value = myHWCLink.getDensity();
 		if(value!=null)
 			dens = (Double) value;
 		else
 			dens =  0.0;
 		
 		// update speed .................................
 		if(dens<0.0001)
 			speed = (Double) myHWCLink.getSpeed();
 		else
 			speed = flow/dens;
 			
 	}
 */
 //	-------------------------------------------------------------------------
 	/**
 	 * Overrides <code>java.lang.Object.toString()</code>.
 	 * @return string that describes the Sensor.
 	 */
 	public String toString() {
 		String buf = "Loop detector";
 		buf += " (" + Integer.toString(id) + ")";
 		return buf;
 	}
 //	-------------------------------------------------------------------------
 	/**
 	 * Initializes the Link from given DOM structure.
 	 * @param p DOM node.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration
 	 */
 	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = true;
 		if ((p == null) || (myNetwork == null))
 			return !res;
 		res &= super.initFromDOM(p);
 		try  {
 			Node pp = p.getAttributes().getNamedItem("length");
 			if (pp != null)
 				looplength = Double.parseDouble(pp.getNodeValue()) / 5280.0;
 			if (myLink != null)
 				myHWCLink = (AbstractLinkHWC) myLink;
 			pp = p.getAttributes().getNamedItem("data_id");
 			if (pp != null)
 				data_id = pp.getNodeValue();
 			pp = p.getAttributes().getNamedItem("vds");
 			if (pp != null)
 				vds = Integer.parseInt(pp.getNodeValue());
 			pp = p.getAttributes().getNamedItem("hwy_name");
 			if (pp != null)
 				hwy_name = pp.getNodeValue();
 			pp = p.getAttributes().getNamedItem("hwy_dir");
 			if (pp != null)
 				hwy_dir = pp.getNodeValue();
 			pp = p.getAttributes().getNamedItem("postmile");
 			if (pp != null)
 				postmile = pp.getNodeValue();
 			pp = p.getAttributes().getNamedItem("lanes");
 			if (pp != null)
 				lanes = Integer.parseInt(pp.getNodeValue());	
 			pp = p.getAttributes().getNamedItem("link_type");
 			if (pp != null)
 				link_type = pp.getNodeValue();
 			// new schema
 			if (p.hasChildNodes()) {
 				NodeList cl = p.getChildNodes();
 				for (int i = 0; i < cl.getLength(); i++) {
 					if (cl.item(i).getNodeName().equals("parameters"))
 						if (cl.item(i).hasChildNodes()) {
 							NodeList cc = cl.item(i).getChildNodes();
 							for (int j = 0; j < cc.getLength(); j++)
 								if (cc.item(j).getNodeName().equals("parameter")) {
 									Node name_attr = cc.item(j).getAttributes().getNamedItem("name");
 									Node value_attr = cc.item(j).getAttributes().getNamedItem("value");
 									if ((name_attr != null) && (value_attr != null)) {
 										if (name_attr.getNodeValue().equals("data_id"))
 											data_id = value_attr.getNodeValue();
 										if (name_attr.getNodeValue().equals("vds"))
 											vds = Integer.parseInt(value_attr.getNodeValue());
 										if (name_attr.getNodeValue().equals("hwy_name"))
 											hwy_name = value_attr.getNodeValue();
 										if (name_attr.getNodeValue().equals("hwy_dir"))
 											hwy_dir = value_attr.getNodeValue();
 										if (name_attr.getNodeValue().equals("postmile"))
 											postmile = value_attr.getNodeValue();
 										if (name_attr.getNodeValue().equals("offset_in_link"))
 											offset_in_link = Float.parseFloat(value_attr.getNodeValue());
 										if (name_attr.getNodeValue().equals("length"))
 											looplength = Double.parseDouble(value_attr.getNodeValue()) / 5280;
 										if (name_attr.getNodeValue().equals("lanes"))
 											lanes = Integer.parseInt(value_attr.getNodeValue());
 										if (name_attr.getNodeValue().equals("start_time"))
 											start_time = Double.parseDouble(value_attr.getNodeValue()) / 3600;
 									}
 									; //TODO
 								}
 						}
 					if (cl.item(i).getNodeName().equals("data_sources"))
 						data_sources_node = cl.item(i).cloneNode(true);			// keep a copy
 						if (cl.item(i).hasChildNodes()) {
 							NodeList cc = cl.item(i).getChildNodes();
 							for (int j = 0; j < cc.getLength(); j++)
 								if (cc.item(j).getNodeName().equals("source")) {
 									Node n1 = cc.item(j);
 									Node n2 = n1.getAttributes().getNamedItem("url");
 									if(n2!=null){
 										String n2str = n2.getTextContent();
 										StringTokenizer st = new StringTokenizer(n2str, ",");
 										while (st.hasMoreTokens()) {
 											String thisurl = st.nextToken();
 											if(isValidURL(thisurl)){
 												HistoricalDataSource hdc = new HistoricalDataSource();
 												hdc.initFromDOM(n1);
 												hdc.setUrl(thisurl);
 												data_files.add(hdc);
 											}
 										}
 									}
 								}
 						}
 				}
 			}
 		}
 		catch(Exception e) {
 			res = false;
 			throw new ExceptionConfiguration(e.getMessage());
 		}
 		initialized = true;
 		return res;
 	}
 //	-------------------------------------------------------------------------
 	public int getType() {
 		return TypesHWC.SENSOR_LOOPDETECTOR;
 	}
 	
 	/**
 	 * Returns letter code of the Sensor type.
 	 */
 	public String getTypeLetterCode() {
 		return "loop";
 	}
 	
 	/**
 	 * Returns type description.
 	 */
 	public final String getTypeString() {
 		return "Loop Detector";
 	}
 	
 	/**
 	 * Return data source liSt.
 	 */
 	public Vector<HistoricalDataSource> getDataSources() {
 		return data_files;
 	}
 	
 //	-------------------------------------------------------------------------
 	public void xmlDump(PrintStream out) throws IOException {
 		if (out == null)
 			out = System.out;
 		out.print("<sensor type=\"" + getTypeLetterCode() + "\" id=\"" + id + "\"");
 		if (link_type != null)
 			out.print(" link_type=\"" + link_type + "\"");
 		out.print(">\n");
 		if (description != null)
 			out.print("  <description>" + description + "</description>\n");
 		if (myLink != null)
 			out.print("  <links>" + myLink.getId()  + "</links>\n");
 		out.print("  <parameters>\n");
 		if (data_id != null)
 			out.print("    <parameter name=\"data_id\" value=\"" + data_id + "\" />\n");
 		if (vds != 0)
 			out.print("    <parameter name=\"vds\" value=\"" + vds + "\" />\n");
 		if (hwy_name != null)
 			out.print("    <parameter name=\"hwy_name\" value=\"" + hwy_name + "\" />\n");
 		if (hwy_dir != null)
 			out.print("    <parameter name=\"hwy_dir\" value=\"" + hwy_dir + "\" />\n");
 		if (postmile != null)
 			out.print("    <parameter name=\"postmile\" value=\"" + postmile + "\" />\n");
 		if (!Float.isNaN(offset_in_link))
 			out.print("    <parameter name=\"offset_in_link\" value=\"" + offset_in_link + "\" />\n");
 		if (!Double.isNaN(looplength))
 			out.print("    <parameter name=\"length\" value=\"" + 5280*looplength + "\" />\n");
 		if (lanes != 0)
 			out.print("    <parameter name=\"lanes\" value=\"" + lanes + "\" />\n");
 		out.print("    <parameter name=\"start_time\" value=\"" + (int)Math.round(3600*start_time) + "\" />\n");
 		out.print("  </parameters>\n");
 		out.print("  <data_sources>\n");
 		if (data_sources_node.hasChildNodes()) {
 			NodeList cc = data_sources_node.getChildNodes();
 			for (int j = 0; j < cc.getLength(); j++)
 				if (cc.item(j).getNodeName().equals("source")) {
 					out.print("    <source");
 					Node n1 = cc.item(j);
 					Node n2;
 					n2 = n1.getAttributes().getNamedItem("url");
 					if(n2!=null)
 						out.print(" url=\"" + n2.getTextContent() + "\"");
 					n2 = n1.getAttributes().getNamedItem("dt");
 					if(n2!=null)
 						out.print(" dt=\"" + n2.getTextContent() + "\"");
 					n2 = n1.getAttributes().getNamedItem("format");
 					if(n2!=null)
 						out.print(" format=\"" + n2.getTextContent() + "\"");
 					out.print(" />\n");
 				}
 		}
 		out.print("  </data_sources>\n");
 		if ((display_lat != null) && (display_lng != null))
 			out.print("  <display_position><point lat=\"" + display_lat + "\" lng=\"" + display_lng + "\" elevation=\"0.0\"/></display_position>\n  ");
 		position.xmlDump(out);
 		out.print("\n</sensor>\n"); 
 		return;
 	}
 	
 	@Override
 	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private boolean isValidURL(String url){
 		if(url.compareToIgnoreCase("null")==0)
 				return false;
 		// TODO: more comprehensive verification
 		return true;
 	}
 }
