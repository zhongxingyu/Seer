 package org.hudson.trayapp.model;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.hudson.trayapp.gui.tray.TrayIconImplementation;
 import org.hudson.trayapp.util.XMLHelper;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class Server {
 
 	private static final long serialVersionUID = 1L;
 
 	private String description;
 	private String url;
 	private String name = "";
 	private List jobs = new Vector();
 	
 	private transient boolean bVersion173OrGreater = false;
 
 	public Server() {}
 
 	public Server(String url, String name) {
 		defaults();
 		this.name = name;
 		this.url = Job.getRFC2396CompliantURL(url);
 		if (url.length() > 0)
 			update();
 	}
 	
 	private void defaults() {
 		description = "";
 		url = "";
 		jobs = new Vector();
 	}
 
 	private static String getRootHudsonURL(String url) {
 		try {
 			 URL urlo = new URL(url);
 			 if (urlo.getFile().startsWith("/hudson/")) {
 				 return new URL(urlo.getProtocol(), urlo.getHost(), urlo.getPort(), "/hudson").toString();
 			 } else {
 				 return new URL(urlo.getProtocol(), urlo.getHost(), urlo.getPort(), "").toString();
 			 }
 		} catch (MalformedURLException e) {
 			return url;
 		}
 	}
 
 	public boolean update() {
 		boolean updated = false;
 		try {
 			boolean hudsonBuild173orGreater = isHudsonBuild173orGreater(true);
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			URL urlo;
 			if (hudsonBuild173orGreater) {
 				urlo = new URL(url + "/api/xml?depth=1");
 			} else {
 				urlo = new URL(url + "/api/xml");
 			}
 			URLConnection conn = urlo.openConnection();
 			InputSource inputSource = new InputSource(conn.getInputStream());
 			Document document = builder.parse(inputSource);
 
 			process(document.getChildNodes());
 
 			/*
 			 * OK, if we have an older version of Hudson, then we won't have fetched back all the information
 			 * that we needed. Thus we will have to fetch all the information about each server, job by job.
 			 */
 			if (updated == true && !hudsonBuild173orGreater) {
 				Iterator iterator = jobs.iterator();
 				while (iterator.hasNext()) {
 					((Job) iterator.next()).update();
 				}
 			}
 		} catch (ParserConfigurationException e) {
 			TrayIconImplementation.displayException("Server Update Exception", "Updating Server " + name, e);
 		} catch (IOException e) {
 			TrayIconImplementation.displayException("Server Update Exception", "Updating Server " + name, e);
 		} catch (SAXException e) {
 			TrayIconImplementation.displayException("Server Update Exception", "Updating Server " + name, e);
 		}
 
 		return updated;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public String getURL() {
 		return url;
 	}
 
 	public Collection getJobs() {
 		return jobs;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void process(NodeList nodes) {
 		defaults();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Node node = nodes.item(i);
 			String name = node.getNodeName();
 			String value = XMLHelper.getTextContent(node);
 			if (name.equals("hudson")) {
 				process(node.getChildNodes());
 			} else if (name.equals("listView")) {
 				process(node.getChildNodes());
 			} else if (name.equals("description")) {
 				description = value;
 			} else if (name.equals("url")) {
 				url = Job.getRFC2396CompliantURL(value);
 			} else if (name.equals("job")) {
 				Job job = Job.process(node);
 				jobs.add(job);
 			} else if (name.equals("internalname")) {
 				this.name = value;
 			}
 		}
 	}
 	
 	/**
 	 * This method will traverse all of the jobs, and determine the worst state of the jobs
 	 * and return all of those jobs that share that same state.
 	 * @return This returns an array of Job's that share the same colour state. This will
 	 * always return an array, but this array may be empty if there are no jobs defined.
 	 */
 	public Job[] getWorstJobs() {
 		Vector vecWorst = new Vector(jobs.size());
 		int worstCase = -1;
 		Iterator jobiter = jobs.iterator(); 
 		for (int i = 0; jobiter.hasNext(); i++) {
 			Job job = (Job) jobiter.next();
 			String colour = job.getColour();
 			for (int j = 0; worstCase != j && j < Model.colours.length; j++) {
 				if (colour.equals(Model.colours[j])) {
 					worstCase = j;
 					vecWorst.clear();
 				}
 			}
 			if (colour.equals(Model.colours[worstCase])) {
 				vecWorst.add(job);
 			}
 		}
 		Job[] jobaReturn = new Job[vecWorst.size()];
 		for (int i = 0; i < jobaReturn.length; i ++) {
 			jobaReturn[i] = (Job) vecWorst.get(i);
 		}
 		return jobaReturn;
 	}
 
 	/**
 	 * This method will traverse all the HudsonJobs, and determine what
 	 * the worst colour is in the following order (worst to best):
 	 * red_anime, red, aborted_anime, aborted, yellow_anime, yellow, blue_anime, blue,
 	 * disabled_anime, disabled
 	 * @return
 	 */
 	public String getColour() {
 		int worstCase = -1;
 		Iterator jobiter = jobs.iterator(); 
 		for (int i = 0; jobiter.hasNext(); i++) {
 			Job job = (Job) jobiter.next();
 			String colour = job.getColour();
 			for (int j = 0; worstCase != j && j < Model.colours.length; j++) {
 				if (colour.equals(Model.colours[j])) {
 					worstCase = j;
 				}
 			}
 		}
 		if (worstCase == -1) {
 			return null;
 		} else {
 			return Model.colours[worstCase];
 		}
 	}
 	
 	/**
 	 * This method will traverse all of the Jobs, and determine the total
 	 * of all of the jobs that share the same colour that you pass in
 	 * @param colour The colour you want to get a count for.
 	 * @return The total number of jobs that are the same state as the colour provided
 	 */
 	public int getNumberOfJobsWithColour(String colour) {
 		int iReturn = 0;
 		Iterator jobiter = jobs.iterator(); 
 		while (jobiter.hasNext()) {
 			Job job = (Job) jobiter.next();
 			if (job.getColour().equals(colour))
 				iReturn++;
 		}
 		return iReturn;
 	}
 	
 	/**
 	 * This method is provided as a convenience method to determine the total
 	 * number of jobs that are currently red (this includes those building).
 	 * @return The total number of jobs that are in a Red state (including those building).
 	 */
 	public int getNumberOfRedJobs() {
 		return getNumberOfJobsWithColour("red") + getNumberOfJobsWithColour("red_anime");
 	}
 	
 	/**
 	 * This method is provided as a convenience method to determine the total
 	 * number of jobs that are currently yellow (this includes those building).
 	 * @return The total number of jobs that are in a Yellow state (including those building).
 	 */
 	public int getNumberOfYellowJobs() {
 		return getNumberOfJobsWithColour("yellow") + getNumberOfJobsWithColour("yellow_anime");
 	}
 	
 	/**
 	 * This method is provided as a convenience method to determine the total
 	 * number of jobs that are currently blue (this includes those building).
 	 * @return The total number of jobs that are in a Blue state (including those building).
 	 */
 	public int getNumberOfBlueJobs() {
 		return getNumberOfJobsWithColour("blue") + getNumberOfJobsWithColour("blue_anime");
 	}
 	
 	/**
 	 * This method is provided as a convenience method to determine the total
 	 * number of jobs that are currently grey.
 	 * @return The total number of jobs that are in a grey state.
 	 */
 	public int getNumberOfGreyJobs() {
 		return getNumberOfJobsWithColour("grey");
 	}
 	
 	/**
 	 * This method is provided as a convenience method to determine the total
 	 * number of jobs that are currently building.
 	 * @return The total number of jobs that are building.
 	 */
 	public int getNumberOfBuildingJobs() {
 		return getNumberOfJobsWithColour("red_anime")	+ getNumberOfJobsWithColour("yellow_anime")
 		 												+ getNumberOfJobsWithColour("blue_anime");
 	}
 
 	public Object clone() {
 		Server modelReturn = new Server();
 		modelReturn.description = description;
 		modelReturn.url = url;
 		modelReturn.name = name;
 		Iterator iterator = jobs.iterator();
 		while (iterator.hasNext()) {
 			Job job = (Job) iterator.next();
 			modelReturn.jobs.add(job.clone());
 		}
 		return modelReturn;
 	}
 
 	/** This method is added, as from Hudson build 173 onwards, you can apply a paremeter to the api/xml of
 	 * ?depth=1 that will give you the details about the build, including the HealthReports. Prior to this
 	 * you have to request specifically about each job what build number it is. Note that older versions do
 	 * not support reporting of HealthReports, and thus restricted information will have to be displayed.
 	 *
 	 * @return Returns true if the build of Hudson this URL points to is of version 173 or greater. Returns false otherwise.
 	 */
 	public boolean isHudsonBuild173orGreater() { return isHudsonBuild173orGreater(false); }
 	private boolean isHudsonBuild173orGreater(boolean fetchFromServer) {
 
 		if (fetchFromServer) {
 			URL urlo = null;
 			try {
 				 urlo = new URL(getRootHudsonURL(url));
 				 URLConnection conn = urlo.openConnection();
				 String version = conn.getHeaderField("X-Hudson") + " ";
				 Matcher matcher = null;
				 if (version != null)
					 matcher = pattern.matcher(version);
				 if (matcher != null && matcher.matches()) {
 					 float f = Float.parseFloat(matcher.group(1));
 					 bVersion173OrGreater = f > 1.172;
 					 return bVersion173OrGreater;
 				 } else {
 					 bVersion173OrGreater = false;
 					 return bVersion173OrGreater;
 				 }
 			} catch (MalformedURLException e) {
 				TrayIconImplementation.displayException("Exception on Version Check", "Trying to get Hudson Version", e);
 			} catch (IOException e) {
 				TrayIconImplementation.displayException("Exception on Version Check", "Trying to get Hudson Version", e);
 			}
 			bVersion173OrGreater = false;
 			return false;
 		}
 		else {
 			return bVersion173OrGreater;
 		}
 	}
 
 	private static Pattern pattern = Pattern.compile("([0-9.]+).*");
 
 	public void setUrl(String url) {
 		this.url = Job.getRFC2396CompliantURL(url);
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public void writeXML(Writer w) throws IOException {
 		w.write("<server>");
 		w.write("<internalname>"); w.write(name); w.write("</internalname>");
 		w.write("<url>"); w.write(url); w.write("</url>");
 		w.write("</server>");
 	}
 	
 	public boolean isHealthSupported() {
 		return isHudsonBuild173orGreater();
 	}
 }
