 package data;
 
 import java.awt.geom.Point2D.Double;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import com.sun.org.apache.xerces.internal.parsers.DOMParser;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class MdfData extends Data{
 
 	String dataFile;
 	String siteInfoFile;
 	boolean paramInfo;
 	String paramInfoFile;
 	
 	
 	//TODO create parameter info variables
 	
 	/**
 	 * This is the basic constructor for the MDFData File type.
 	 * 
 	 * @param dataFile - the .mdf file containing the data.
 	 * @param siteInfoFile - the .xml file containing site information
 	 */
 	public MdfData(String dataFile, String siteInfoFile){
 		this.dataFile = dataFile;
 		this.siteInfoFile = siteInfoFile;
 	}
 	
 	/**
 	 * This is a basic constructor for the MDFData file type.
 	 * 
 	 * @param dataFile - the .mdf file containing the data.
 	 * @param siteInfoFile - the .xml file containing site information
 	 * @param paramInfoFile - the .xml file containing parameter information
 	 */
 	@SuppressWarnings("unused")
 	private MdfData(String dataFile, String siteInfoFile, String paramInfoFile){
 		this.dataFile = dataFile;
 		this.siteInfoFile = siteInfoFile;
 		this.paramInfoFile = paramInfoFile;
 		this.paramInfo = true;
 		
 	}
 	
 	/**
 	 * This imports data from the files into the basic data structure.
 	 * Note that the import is required before any data is imported.  
 	 */
 	public void importData(){
 		if(paramInfo){
 			importParamInfo();
 		}
 		try {
 			importSiteInfo();
 			importDataFile();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	private void importParamInfo(){
 		
 	}
 	
 	/**
 	 * This imports the information about sites located in the
 	 * siteInfo.xml file. 
 	 * 
 	 * @throws SAXException
 	 * @throws IOException
 	 */
 	private void importSiteInfo() throws SAXException, IOException{
 		DOMParser parser = new DOMParser();
 		parser.parse(this.siteInfoFile);
 		Document doc = parser.getDocument();
 		
 		// Root XML node
 		NodeList root = doc.getChildNodes();
 		
 		Node stin = getNode("siteinfo", root);
 		
 		Node table = getNode("table", stin.getChildNodes());
 		
 		NodeList list = table.getChildNodes();
 		
 		
 		ArrayList<String> field_ids = new ArrayList<String>();
 		ArrayList<String> field_types = new ArrayList<String>();
 		ArrayList<String> sites = new ArrayList<String>();
 		for(int i = 0; i < list.getLength(); i ++){
 			if(list.item(i).getNodeName().equalsIgnoreCase("fieldref")){
 				field_ids.add(getNodeAttr("id", list.item(i)));
 				field_types.add(getNodeAttr("type", list.item(i)));
 			}
 			if(list.item(i).getNodeName().equalsIgnoreCase("record")){
 				//test info
 				//System.out.println(getNodeValue(list.item(i)));
 				sites.add(getNodeValue(list.item(i)));//list.item(i).getNodeValue());
 			}
 		}
 		
 		field_ids.trimToSize();
 		field_types.trimToSize();
 		sites.trimToSize();
 		
 		int siteIdIdx = field_ids.indexOf("stid");
 		int siteNameIdx = field_ids.indexOf("name");
 		int latIdx = field_ids.indexOf("lat");
 		int lonIdx = field_ids.indexOf("lon");
 		int elevIdx = field_ids.indexOf("elev");
 		//Note that other site information is not used by this Software at this time.
 		
 		this.siteids = new String[sites.size()];
 		this.sitenames = new String[sites.size()];
 		this.lats = new double[sites.size()];
 		this.lons = new double[sites.size()];
 		this.elevs = new double[sites.size()];
 		
 		
 		for(int i = 0; i < sites.size(); i++){
 			String[] tmp = Parse.parseMdfRecord(sites.get(i));
 			this.siteids[i] = tmp[siteIdIdx];
 			this.sitenames[i] = tmp[siteNameIdx];
 			this.lats[i] = java.lang.Double.parseDouble(tmp[latIdx]);
 			this.lons[i] = java.lang.Double.parseDouble(tmp[lonIdx]);
 			this.elevs[i] = java.lang.Double.parseDouble(tmp[elevIdx]);
 			
 			//Test Output
 			//System.out.println("id: " + this.siteids[i] + ", name: " + this.sitenames[i] + ", lat: " + this.lats[i]);
 		}
 		
 		return;
 		
 		
 	}
 	
 	/**
 	 * This imports all the data contained in the MDF file.  
 	 */
 	@SuppressWarnings("unused")
 	private void importDataFile(){
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(new File(this.dataFile)));
 			String buffer;
 			int line = 0;
 			String delims = " ";
 			int params;
 			int year;
 			int month;
 			int day;
 			int basehour;
 			int baseminute;
 			int basesecond;
 			int cnt = 0;
 			while((buffer = br.readLine())  != null){
 				//System.out.println(buffer); // test print
 				line++;
 				StringTokenizer st = new StringTokenizer(buffer,delims, false);
 				String tmp;
 				if(line == 1){
 					String ver = st.nextToken();
 					continue;
 				}
 				if(line == 2){
 					params = Integer.parseInt(st.nextToken());
 					
 					this.parameters = new String[params + 3];
 					this.data = new String[this.siteids.length][params + 3];
 					
 					year = Integer.parseInt(st.nextToken());
 					month = Integer.parseInt(st.nextToken());
 					day = Integer.parseInt(st.nextToken());
 					basehour = Integer.parseInt(st.nextToken());
 					baseminute = Integer.parseInt(st.nextToken());
 					basesecond = Integer.parseInt(st.nextToken());
 					continue;
 				}				
 				else{
 					tmp = st.nextToken();
 				}
 				
 				
 				if(tmp.equalsIgnoreCase("STID")){
 					this.parameters[0] = tmp;
 					for(int i = 1; i < this.parameters.length; i++){
 						this.parameters[i] = st.nextToken();
 					}
 				}
 				else{
 					data[cnt][0] = tmp;
 					for(int i = 1; i < this.parameters.length; i++){
 						data[cnt][i] = st.nextToken();
 						//System.out.println(data[cnt][i]);
 					}
 					cnt++;
 				}
 				
 				
 				
 				
 			}
 			
 			br.close();
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	@Override
 	/**
 	 * This gets the lattitude of a site.  
 	 * note the sitename is actually the siteid.
 	 */
 	public double getLat(String sitename) {
 		for(int i = 0; i < siteids.length; i ++){
 			if(siteids[i].equalsIgnoreCase(sitename)){
 				return lats[i];
 			}
 		}
 		return 0;
 	}
 
 
 
 
 	@Override
 	/**
 	 * This gets the longitude of a site
 	 * note the sitename is actually the siteid
 	 */
 	public double getLon(String sitename) {
 		for(int i = 0; i < siteids.length; i ++){
 			if(siteids[i].equalsIgnoreCase(sitename)){
 				return lons[i];
 			}
 		}
 		return 0;
 	}
 
 
 
 
 	@Override
 	/**
 	 * This gets the lat/lon of a site
 	 * note the sitename is actually the siteid
 	 */
 	public Double getLocation(String sitename) {
 		for(int i = 0; i < siteids.length; i ++){
 			if(siteids[i].equalsIgnoreCase(sitename)){
 				return new Double(lons[i],lats[i]);
 			}
 		}
 		return null;
 	}
 
 
 
 
 	@Override
 	/**
 	 * This gets a single data 
 	 */
 	public double getDatum(String sitename, String param) {
 		for(int i = 0; i < siteids.length; i ++){
 			if(siteids[i].equalsIgnoreCase(sitename)){
 				for(int j = 0; j < parameters.length; j++){
 					if(parameters[j].equalsIgnoreCase(param)){
						return java.lang.Double.parseDouble(data[i][j]);
 					}
 				}
				return 0;
 			}
 		}
 		return 0;
 	}
 
 
 	@Override
 	/**
 	 * This pulls data from all sites from a particular
 	 * parameter. 
 	 */
 	public double[] getData(String param) {
 		int idx = 0;
 		double[] tdata = new double[siteids.length];
 		for(int i = 0; i < parameters.length; i++){
 			if(param.equalsIgnoreCase(parameters[i])){
 				idx = i;
 				break;
 			}
 		}
 		for(int i = 0; i < siteids.length; i++){
 			tdata[i] = java.lang.Double.parseDouble(data[i][idx]);
 		}
 		return null;
 	}
 
 
 
 
 	@Override
 	/**
 	 * Returns all site names.  
 	 */
 	public String[] getSites() {
 		return sitenames;
 	}
 
 
 
 
 	@Override
 	/**
 	 * returns all site locations as a Point
 	 */
 	public Double[] getLocations() {
 		Double[] locs = new Double[siteids.length];
 		for(int i = 0; i < siteids.length; i++){
 			locs[i].setLocation(lons[i], lats[i]);
 		}
 		return locs;
 	}
 
 
 
 
 	@Override
 	/**
 	 * returns all site ids
 	 */
 	public String[] getSiteIDs() {
 		return siteids;
 	}
 
 	
 	//***************************************************//
 	//*********** XML Parsing Methods *******************//
 	// These Methods were provided by Eric Bruno         //
 	
 	
 	private Node getNode(String tagname, NodeList nodes){
 		for(int x = 0; x < nodes.getLength(); x++){
 			Node node = nodes.item(x);
 			if(node.getNodeName().equalsIgnoreCase(tagname)){
 				return node;
 			}
 		}
 		return null;
 	}
 	
 	private String getNodeValue(Node node) {
 		NodeList childNodes = node.getChildNodes();
 		for (int x = 0; x< childNodes.getLength(); x++){
 			Node data = childNodes.item(x);
 			if ( data.getNodeType() == Node.TEXT_NODE)
 				return data.getNodeValue();
 		}
 		return "";
 	}
 	
 	@SuppressWarnings("unused")
 	private String getNodeValue(String tagName, NodeList nodes){
 		for (int x = 0; x < nodes.getLength(); x++){
 			Node node = nodes.item(x);
 			if (node.getNodeName().equalsIgnoreCase(tagName)){
 				NodeList childNodes = node.getChildNodes();
 				for (int y = 0; y < childNodes.getLength(); y++){
 					Node data = childNodes.item(y);
 					if (data.getNodeType() == Node.TEXT_NODE)
 						return data.getNodeValue();
 				}
 			}
 		}
 		return "";
 	}
 	
 	private String getNodeAttr(String attrName, Node node){
 		NamedNodeMap attrs = node.getAttributes();
 		for(int y = 0; y < attrs.getLength(); y++){
 			Node attr = attrs.item(y);
 			if (attr.getNodeName().equalsIgnoreCase(attrName)){
 				return attr.getNodeValue();
 			}
 		}
 		return "";
 	}
 	
 	@SuppressWarnings("unused")
 	private String getNodeAttr(String tagName, String attrName, NodeList nodes){
 		for(int x = 0; x < nodes.getLength(); x++){
 			Node node = nodes.item(x);
 			if (node.getNodeName().equalsIgnoreCase(tagName)){
 				NodeList childNodes = node.getChildNodes();
 				for (int y = 0; y < childNodes.getLength(); y++){
 					Node data = childNodes.item(y);
 					if (data.getNodeType() == Node.ATTRIBUTE_NODE){
 						if (data.getNodeName().equalsIgnoreCase(attrName)){
 							return data.getNodeValue();
 						}
 					}
 				}
 			}
 		}
 		return "";
 	}
 	
 }
