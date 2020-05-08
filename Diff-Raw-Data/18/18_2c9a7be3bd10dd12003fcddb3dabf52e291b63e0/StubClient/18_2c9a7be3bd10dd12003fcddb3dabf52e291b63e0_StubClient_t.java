 package com.kdcloud.lib.client;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 
 import weka.core.DenseInstance;
 import weka.core.Instances;
 
 import com.kdcloud.lib.domain.Modality;
 
 public class StubClient extends BaseClient {
 	
 	
 
 	public StubClient(String url) throws ParserConfigurationException {
 		super(url);
 		// TODO Auto-generated constructor stub
 	}
 
 	public StubClient(String url, Modality modality)
 			throws ParserConfigurationException {
 		super(url, modality);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void log(String message, Throwable thrown) {
 		System.out.println(message);
 		thrown.printStackTrace();
 	}
 	
 	public static double[] readData(String file) {
 		BufferedReader in = null;
 		Vector<Double> sign = new Vector<Double>();
 		try {
 			InputStream stream = StubClient.class.getClassLoader().getResourceAsStream(file);
 			in = new BufferedReader(new InputStreamReader(stream));
 			String line = in.readLine();
 			while (line != null) {
 				// System.out.print(line+"\t");
 				// System.out.println(Double.parseDouble(line));
 				sign.add(Double.parseDouble(line));
 				line = in.readLine();
 			}
 			;
 			in.close();
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		double[] res = new double[sign.size()];
 		for (int i = 0; i < sign.size(); i++) {
 			res[i] = sign.get(i);
 		}
 		return res;
 	}
 
 	@Override
 	public Instances getData() {
 		double[] values = readData("ecg_small.txt");
 		log("data length: " + values.length);
 		Instances data = modality.getInputSpecification().newInstances("test");
 		for (int i = 0; i < values.length; i++) {
 			double[] cells = { values[i] };
 			data.add(new DenseInstance(1, cells));
 		}
 		return data;
 	}
 
 	@Override
 	public String handleChoice(String parameterName, String[] choices) {
 		return choices[0];
 	}
 
 	@Override
 	public void report(Document view) {
 		System.out.println("this is a report");
 	}
 	
 	public static void main(String[] args) throws Exception {
 		String url = "http://localhost:8888";
 		BaseClient kdcloud = new StubClient(url);
 		kdcloud.setAuthentication("admin", "admin");
 		List<Modality> modalities = kdcloud.getModalities();
 		Modality dataFeed = modalities.get(0);
 		kdcloud.setModality(dataFeed);
 		kdcloud.executeModality();
 		Modality single = modalities.get(1);
 		kdcloud.setModality(single);
 		kdcloud.executeModality();
 		Modality global = modalities.get(2);
 		kdcloud.setModality(global);
 		kdcloud.executeModality();
 	}
 
 	@Override
 	public void log(String message) {
 		System.out.println(message);
 	}
 
 	@Override
 	public void handleResourceException(Status status, ResourceException e) {
		System.out.println("fuck!!!");
 	}
 
 }
