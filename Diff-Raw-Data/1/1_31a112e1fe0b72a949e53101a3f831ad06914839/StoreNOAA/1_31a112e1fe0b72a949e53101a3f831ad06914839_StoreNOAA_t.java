 /*
 Copyright (c) 2013, Colorado State University
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 This software is provided by the copyright holders and contributors "as is" and
 any express or implied warranties, including, but not limited to, the implied
 warranties of merchantability and fitness for a particular purpose are
 disclaimed. In no event shall the copyright holder or contributors be liable for
 any direct, indirect, incidental, special, exemplary, or consequential damages
 (including, but not limited to, procurement of substitute goods or services;
 loss of use, data, or profits; or business interruption) however caused and on
 any theory of liability, whether in contract, strict liability, or tort
 (including negligence or otherwise) arising in any way out of the use of this
 software, even if advised of the possibility of such damage.
  */
 
 package galileo.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import galileo.comm.Disconnection;
 import galileo.comm.QueryRequest;
 import galileo.comm.QueryResponse;
 import galileo.comm.StorageRequest;
 import galileo.dataset.Block;
 import galileo.dataset.BlockMetadata;
 import galileo.dataset.Device;
 import galileo.dataset.DeviceSet;
 import galileo.dataset.FileBlock;
 import galileo.dataset.Metadata;
 import galileo.dataset.SpatialProperties;
 import galileo.dataset.TemporalProperties;
 import galileo.dataset.feature.Feature;
 import galileo.dataset.feature.FeatureSet;
 import galileo.event.EventContainer;
 import galileo.event.EventType;
 import galileo.net.ClientMessageRouter;
 import galileo.net.GalileoMessage;
 import galileo.net.MessageListener;
 import galileo.net.NetworkDestination;
 import galileo.query.Query;
 import galileo.samples.ConvertNetCDF;
 import galileo.serialization.Serializer;
 import galileo.util.FileNames;
 import galileo.util.GeoHash;
 import galileo.util.Pair;
 import galileo.util.ProgressBar;
 
 public class StoreNOAA {
 
 	private ClientMessageRouter messageRouter;
 	private EventPublisher publisher;
 
 	public StoreNOAA() throws IOException {
 		messageRouter = new ClientMessageRouter();
 		publisher = new EventPublisher(messageRouter);
 	}
 
 	public NetworkDestination connect(String hostname, int port)
 			throws UnknownHostException, IOException {
 		return messageRouter.connectTo(hostname, port);
 	}
 
 	public void disconnect() {
 		messageRouter.shutdown();
 	}
 
 	public void store(NetworkDestination destination, Block block)
 			throws Exception {
 		StorageRequest store = new StorageRequest(block);
 		publisher.publish(destination, store);
 	}
 
 	public static void main(String[] args) throws Exception {
 		if (args.length != 3) {
 			System.out.println("Usage: galileo.client.TextClient "
 					+ "<server-hostname> <server-port> <directory-name>");
 			return;
 		}
 
 		String serverHostName = args[0];
 		int serverPort = Integer.parseInt(args[1]);   
 
 		StoreNOAA client = new StoreNOAA();
 		File dir = new File(args[2]);
 		NetworkDestination server = client.connect(serverHostName, serverPort);
 		for (File f : dir.listFiles()) {
 			Pair<String, String> nameParts = FileNames.splitExtension(f);
 			String ext = nameParts.b;
 			if (ext.equals("grb") || ext.equals("bz2") || ext.equals("gz")) {
 				
 				if(!f.getName().endsWith("_000.grb.bz2"))
 						continue;
 				
 				System.out.println("Parsing: "+ f.getName());
 				Map<String, Metadata> metas = ConvertNetCDF.readFile(f.getAbsolutePath());
 				System.out.println("Storing: "+ f.getName());
 				int cntr = 0;
 				ProgressBar pb = new ProgressBar(metas.size(), f.getName());
 				for (Map.Entry<String, Metadata> entry : metas.entrySet()) {
 					pb.setVal(cntr++);
 					client.store(server, ConvertNetCDF.createBlock("", entry.getValue()));
 				}
 				pb.finish();
 			}
 		}
 		System.out.println("Completed Directory");
 	}
 }
