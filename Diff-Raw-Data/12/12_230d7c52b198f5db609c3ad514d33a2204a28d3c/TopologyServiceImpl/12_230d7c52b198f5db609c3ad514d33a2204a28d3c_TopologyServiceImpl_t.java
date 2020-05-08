 package com.tranhoangdai.korengui.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.tranhoangdai.korengui.client.service.TopologyService;
 
 public class TopologyServiceImpl extends RemoteServiceServlet implements TopologyService {
 
 	@Override
 	public String getTopologySwitches() {
 		String json="";
 	
 //		try {
 //			json = readUrl("http://163.180.140.95:8080/wm/core/controller/switches/json");
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 		
 		try {
 			json = readFile("sample-json/nodes.json");
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		return json;
 	}
 
 	@Override
 	public String getTopologyLinks() {
 		
 		String json="";
 		
 		// try {
 		// json = readUrl("http://163.180.140.95:8080/wm/topology/links/json");
 		// } catch (IOException e) {
 		//
 		// }
 		
 		try {
 			json = readFile("sample-json/links.json");
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		return json;
 	}
 
	public String readUrl(String url) throws IOException {
 		String inputLine = "";
 		String inputLine2 = "";
 
 		URL topologyUrl = new URL(url);
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(topologyUrl.openStream()));
 		while ((inputLine = reader.readLine()) != null) {
 			inputLine2 += inputLine;
 		}
 		reader.close();
 
 		return inputLine2;
 	}
 
	public String readFile(String filePath) throws IOException {
 		String inputLine = "";
 		String inputLine2 = "";
 
 		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 		while ((inputLine = reader.readLine()) != null) {
 			inputLine2 += inputLine;
 		}
 		reader.close();
 
 		return inputLine2;
 	}
 
 }
