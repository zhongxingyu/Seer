 /*
  * 
  * This is Jemula802.
  *
  *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
  *    All rights reserved. Urheberrechtlich geschuetzt.
  * 
  *    Redistribution and use in source and binary forms, with or without modification,
  *    are permitted provided that the following conditions are met:
  * 
  *      Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  * 
  *      Redistributions in binary form must reproduce the above copyright notice,
  *      this list of conditions and the following disclaimer in the documentation and/or
  *      other materials provided with the distribution.
  * 
  *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
  *      may be used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
  *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  *    OF SUCH DAMAGE.
  * 
  */
 
 package animation;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import kernel.JEmula;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import station.JE802Station;
 
 import com.sun.org.apache.xml.internal.serialize.OutputFormat;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 
 import emulator.JE802Starter;
 
 public class JE802KmlWriter extends JEmula {
 
 	// name of the result file
 	private final String resultPath;
 
 	private final String filename;
 
 	private Document doc;
 
 	// size of a pixel in the power/channel/address map in meters
 	private final double pixelSize;
 
 	private final List<JE802Station> stations;
 
 	// upper limit of the throughput scale
 	private final double maxTP;
 
 	// upper limit of the delay scale
 	private final double maxDelayMs;
 
 	private final double reuseDistance;
 
 	private final double minTxdBm;
 
 	private final double maxTxdBm;
 
 	private final double attenuationFactor;
 
 	private final double mbPerBlock;
 
 	private final boolean generateOffer;
 
 	private final boolean generatePowerOverlay;
 
 	private final boolean generateAntennas;
 
 	public JE802KmlWriter(final Node anAnimationNode, final String path, final String filename,
 			final List<JE802Station> stations, final double reuseDistance) {
 		Element animationElem = (Element) anAnimationNode;
 
 		if (animationElem.hasAttribute("maxThrp")) {
 			this.maxTP = new Double(animationElem.getAttribute("maxThrp"));
 		} else {
 			this.maxTP = (Double) null;
 			this.error("missing attribute maxThrp in " + animationElem.getNodeName());
 		}
 
 		if (animationElem.hasAttribute("maxDelay")) {
 			this.maxDelayMs = new Double(animationElem.getAttribute("maxDelay"));
 		} else {
 			this.maxDelayMs = (Double) null;
 			this.error("missing attribute maxDelay in " + animationElem.getNodeName());
 		}
 
 		if (animationElem.hasAttribute("overlayAccuracy")) {
 			this.pixelSize = new Double(animationElem.getAttribute("overlayAccuracy"));
 		} else {
 			this.pixelSize = (Double) null;
 			this.error("missing attribute overlayAccuracy in " + animationElem.getNodeName());
 		}
 
 		if (animationElem.hasAttribute("minTxdBm")) {
 			this.minTxdBm = new Double(animationElem.getAttribute("minTxdBm"));
 		} else {
 			this.minTxdBm = (Double) null;
 			this.error("missing attribute minTxdBm in " + animationElem.getNodeName());
 		}
 
 		if (animationElem.hasAttribute("maxTxdBm")) {
 			this.maxTxdBm = new Double(animationElem.getAttribute("maxTxdBm"));
 		} else {
 			this.maxTxdBm = (Double) null;
 			this.error("missing attribute maxTxdBm in " + animationElem.getNodeName());
 		}
 
 		if (animationElem.hasAttribute("attenuationFactor")) {
 			this.attenuationFactor = new Double(animationElem.getAttribute("attenuationFactor"));
 		} else {
 			this.attenuationFactor = (Double) null;
 			this.error("missing attribute attenuationFactor in " + animationElem.getNodeName());
 		}
 
 		String generateBlocksStr = new String();
 		if (animationElem.hasAttribute("generateOfferBlocks")) {
 			generateBlocksStr = animationElem.getAttribute("generateOfferBlocks");
 		} else {
 			this.error("missing attribute generateOfferBlocks in " + animationElem.getNodeName());
 		}
 		if (generateBlocksStr.isEmpty()) {
 			generateOffer = true;
 		} else {
 			generateOffer = new Boolean(generateBlocksStr);
 		}
 
 		String generatePowerOverlayStr = new String();
 		if (animationElem.hasAttribute("generatePowerOverlay")) {
 			generatePowerOverlayStr = animationElem.getAttribute("generatePowerOverlay");
 		} else {
 			this.error("missing attribute generatePowerOverlay in " + animationElem.getNodeName());
 		}
 		if (generatePowerOverlayStr.isEmpty()) {
 			generatePowerOverlay = true;
 		} else {
 			generatePowerOverlay = new Boolean(generatePowerOverlayStr);
 		}
 
 		this.generateAntennas = animationElem.hasAttribute("generateAntennas");
 
 		String mbPBStr = new String();
 		if (animationElem.hasAttribute("mbPerBlock")) {
 			mbPBStr = animationElem.getAttribute("mbPerBlock");
 		} else {
 			this.error("missing attribute mbPerBlock in " + animationElem.getNodeName());
 		}
 		if (!mbPBStr.isEmpty()) {
 			this.mbPerBlock = new Double(mbPBStr);
 		} else {
 			this.mbPerBlock = 0.2;
 			warning("WARNING: no mbPerBlock attribute in JE802Animation tag specified, using default " + this.mbPerBlock);
 		}
 
 		this.reuseDistance = reuseDistance;
 		this.resultPath = path;
 		String[] fileParts = filename.split("/");
 		String name = fileParts[fileParts.length - 1];
 		this.filename = name.substring(0, name.length() - 4);
 		this.stations = stations;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db;
 
 		try {
 			db = dbf.newDocumentBuilder();
 			this.doc = db.newDocument();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Element createHiddenListStyle() {
 		// hidden element style of folders, just needed once in a document
 		Element style = this.doc.createElement("Style");
 		style.setAttribute("id", "hideChildren");
 		Element listStyle = this.doc.createElement("ListStyle");
 		Element listItemType = this.doc.createElement("listItemType");
 		listItemType.appendChild(this.doc.createTextNode("checkHideChildren"));
 		listStyle.appendChild(listItemType);
 		style.appendChild(listStyle);
 		return style;
 	}
 
 	private Element createFolder(final List<JE802KmlGenerator> generators, final String name) {
 		Element folder = this.doc.createElement("Folder");
 		Element folderName = this.doc.createElement("name");
 		folderName.appendChild(this.doc.createTextNode(name));
 		folder.appendChild(folderName);
 
 		Element open = this.doc.createElement("open");
 		open.appendChild(this.doc.createTextNode("0"));
 
 		Element visibility = this.doc.createElement("visibility");
 		visibility.appendChild(this.doc.createTextNode("0"));
 		folder.appendChild(visibility);
 		folder.appendChild(open);
 		for (JE802KmlGenerator gen : generators) {
 			folder.appendChild(gen.createDOM());
 		}
 		return folder;
 	}
 
 	/**
 	 * creates the dom of the animation xml file
 	 */
 	public void createDOM() {
 		Element root = this.doc.createElement("kml");
 		root.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");
 		root.setAttribute("xmlns:gx", "http://www.google.com/kml/ext/2.2");
 		Element document = this.doc.createElement("Document");
 
 		document.appendChild(createHiddenListStyle());
 		Element open = this.doc.createElement("open");
 		open.appendChild(this.doc.createTextNode("1"));
 		document.appendChild(open);
 
 		List<JE802KmlGenerator> generators = new ArrayList<JE802KmlGenerator>();
 
 		// station models
 		generators.add(new JE802ModelKml(this.doc, this.stations, this.filename, false));
 
 		// station models with mickey users
 		// generators.add(new JE802ModelKml(this.doc,this.stations,
 		// this.filename, true));
 
 		// Reuse Distance Overlays
 		generators.add(new JE802ReuseOverlay(this.doc, this.stations, this.filename, true, this.reuseDistance));
 
 		// power Overlays
 		if (generatePowerOverlay) {
 			generators.add(new JE802PowerOverlayKml(this.doc, this.stations, this.filename, this.pixelSize, true,
					this.reuseDistance, this.maxTxdBm, this.minTxdBm, this.attenuationFactor, this.resultPath));
 		}
 
 		if (generateAntennas) {
 			// antenna directions
 			generators.add(new JE802AntennaDirectionsKml(doc, stations));
 
 			// antenna angles
 			generators.add(new JE802AntennaAnglesKml(doc, stations));
 		}
 
 		// offerBlocks
 		if (generateOffer) {
 			generators.add(new JE802OfferBlocks(this.doc, this.stations, this.filename, this.mbPerBlock));
 		}
 
 		// create throughput links
 		generators.add(new JE802ThrpLinksKml(this.doc, this.stations, this.maxTP, false));
 
 		// throughput stations
 		generators.add(new JE802ThrpStationKml(this.doc, this.stations, this.filename, this.maxTP, false));
 
 		// create delay stations
 		// generators.add(new JE802DelayStationKml(this.doc, this.stations,
 		// this.filename, this.maxDelayMs, false));
 
 		// create delay links
 		// generators.add(new JE802DelayLinksKml(this.doc, this.stations,
 		// this.maxDelayMs, true));
 
 		// BlackLinks
 		// generators.add(new JE802StaticLinkKml(this.doc, this.stations, new
 		// Color(0,0,0), "Black"));
 
 		// WhiteLinks
 		// generators.add(new JE802StaticLinkKml(this.doc, this.stations, new
 		// Color(255,255,255), "White"));
 
 		// create delay stations
 		// generators.add(new JE802DelayStationKml(this.doc, this.stations,
 		// this.filename, this.maxDelayMs, true));
 
 		// create delay links
 		// generators.add(new JE802DelayLinksKml(this.doc, this.stations,
 		// this.maxDelayMs, false));
 
 		// route links
 		generators.add(new JE802RouteLinksKml(doc, stations));
 
 		// channel links
 		generators.add(new JE802ChannelLinksKml(doc, stations));
 
 		// phyModeLinks
 		generators.add(new JE802PhyModeLinksKml(doc, stations));
 
 		for (JE802KmlGenerator gen : generators) {
 			document.appendChild(gen.createDOM());
 		}
 
 		// Ground overlays
 		List<JE802KmlGenerator> grounds = new ArrayList<JE802KmlGenerator>();
 		grounds.add(new JE802OverlayGenerator(this.doc, this.stations, "animation_files/white.png", "White", 50000, true,
 				this.reuseDistance));
 		grounds.add(new JE802OverlayGenerator(this.doc, this.stations, "animation_files/stones2.png", "Cobbles", 300, true,
 				this.reuseDistance));
 		grounds.add(new JE802OverlayGenerator(this.doc, this.stations, "animation_files/black.png", "Black", 50000, true,
 				this.reuseDistance));
 
 		Element overlayFolder = createFolder(grounds, "Ground Overlays");
 		document.appendChild(overlayFolder);
 
 		this.doc.appendChild(root);
 		root.appendChild(document);
 	}
 
 	/**
 	 * writes the DOM to and XML file
 	 */
 	public void writeDOMtoFile() {
 		OutputFormat format = new OutputFormat(this.doc);
 		format.setIndenting(true);
 		format.setIndent(4);
 		format.setEncoding("UTF-8");
 		Writer output;
 		String destDirectory = new String(this.resultPath + "/animation_files/");
 		File directory = new File(destDirectory);
 		if (!directory.exists()) { // directory does not exist
 			try {
 				this.message("creating new animation destination directory " + destDirectory);
 				directory.mkdirs();
 			} catch (Exception e) {
 				this.error("could not create the animation destination directory " + destDirectory);
 			}
 		}
 
 		try {
 			output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destDirectory + "/doc.kml"), "UTF-8"));
 			XMLSerializer serializer = new XMLSerializer(output, format);
 			serializer.serialize(this.doc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void createKMZArchive() {
 
 		File modelFolder = new File("resources/models");
 		File filesFolder = new File(this.resultPath + "/animation_files");
 		for (File file : modelFolder.listFiles()) {
 			if (!file.getName().startsWith(".svn")) { // exclude .svn
 				JE802Starter.filecopy("resources/models/" + file.getName(), filesFolder.getAbsolutePath());
 			}
 		}
 
 		// put all files in the files folder into the kmz archive
 		File animationFile = new File(this.resultPath + "/" + this.filename + ".kmz");
 		FileOutputStream fileStream;
 		try {
 			fileStream = new FileOutputStream(animationFile);
 			ZipOutputStream zipStream = new ZipOutputStream(fileStream);
 			for (File file : filesFolder.listFiles()) {
 				if (!file.isDirectory()) {
 					FileInputStream in = new FileInputStream(file);
 					ZipEntry entry = new ZipEntry(filesFolder.getAbsolutePath() + "/" + file.getName());
 					zipStream.putNextEntry(entry);
 					int len;
 					byte[] buf = new byte[1024];
 					while ((len = in.read(buf)) > 0) {
 						zipStream.write(buf, 0, len);
 					}
 					in.close();
 					zipStream.closeEntry();
 				}
 			}
 			zipStream.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
