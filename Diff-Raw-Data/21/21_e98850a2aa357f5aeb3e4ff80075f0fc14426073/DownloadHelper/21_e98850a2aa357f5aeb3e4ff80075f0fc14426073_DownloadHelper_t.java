 package de.fhb.dlService;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.amazonaws.services.s3.AmazonS3;
 
 public class DownloadHelper {
 
 	public List<String> getDlLinks() {
 		List<String> links = new ArrayList<String>();
 		links.add("link_1");
 		links.add("link_2");

 		try {
 			AmazonS3 awsS3 = AwsS3Credentials.getIns(
 					"AwsCredentials.properties").initCredentials();
 			BucketUtil bucket = new BucketUtil(awsS3);
 
			// bucket.downloadFromBucket("", aKey)
			//
 			// InputStreamReader inReader = new InputStreamReader(
 			// filecontent);
 			// CSVReader csvReader = new CSVReader(inReader);
			//
 			// String[] nextLine;
 			// while ((nextLine = csvReader.readNext()) != null) {
 			// links.add(nextLine[0]);
 			// System.out.println(nextLine[0]);
 			// }
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return links;
 	}
 }
