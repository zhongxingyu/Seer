 /**
  * *****************************************************************************
  * Copyright 2012-2013 Trento Rise (www.trentorise.eu/)
  * 
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the GNU Lesser General Public License (LGPL)
  * version 2.1 which accompanies this distribution, and is available at
  * 
  * http://www.gnu.org/licenses/lgpl-2.1.html
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  ******************************************************************************* 
  */
 package eu.trentorise.opendata.dtreporter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import org.ckan.CKANException;
 import org.ckan.Client;
 import org.ckan.Connection;
 import org.ckan.resource.impl.Dataset;
 import org.ckan.resource.impl.Extra;
 import org.ckan.resource.impl.Group;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class MainClass {
 
 	public static void main(String args[]) {
 		Client ccl = new Client(new Connection("http://dati.trentino.it"), null);
 
 		List<String> datasets;
 		datasets = null;
 		try {
 			datasets = ccl.getDatasetList().result;
 		} catch (CKANException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String csvf = "output.csv";
 		FileWriter fw = null;
 		try {
 			fw = new FileWriter(csvf);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
			fw.write("\"#(c) 2013 TrentoRISE -- Created by dt-reporter\"\n");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		CSVWriter csv = null;
 		csv = new CSVWriter(fw,';');
 		
 		csv.writeNext(new String[] {"Name","Title","Maintainer","Author","data di creazione","data di aggiornamento","groupName"});
 		
 		for (String dsname : datasets) {
 			try {
 				Dataset toadd = ccl.getDataset(dsname);
 				String[] toput = new String[7];
 				List<Group> lga = toadd.getGroups();
 				for (Group group : lga) {
 					toput[0] = toadd.getName();
 					toput[1] = toadd.getTitle();
 					toput[2] = toadd.getMaintainer();
 					toput[3] = toadd.getAuthor();
 					toput[4] = "";
 					toput[5] = "";
 					for (Extra e : toadd.getExtras()) {
 						if(e.getKey().toLowerCase().contains("categori"))
 						{
 							System.out.println(e.getValue());
 						}
 						if(e.getKey().toLowerCase().contains("data di creazione"))
 						{
 							toput[4] = e.getValue();
 						}
 						if(e.getKey().toLowerCase().contains("data di aggiornamento"))
 						{
 							toput[5] = e.getValue();
 						}
 					}
 					toput[6] = group.getName();
 					csv.writeNext(toput);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			csv.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
