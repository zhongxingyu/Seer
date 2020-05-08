 /*
  * ParkingIQ Project.
  *
  * (c) 2011 Aditya Sawhney
  * This code may be freely used and modified for any purpose. 
  */
 package edu.colorado.piq.service.impl;
 
 import java.util.HashMap;
 import java.util.List;
 
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.beans.Rows;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.query.QueryResult;
 import edu.colorado.piq.Const.DataModel.CF_LotStatusArchive;
 import edu.colorado.piq.model.CassandraConfig;
 import edu.colorado.piq.model.ParkingLotInfo;
 import edu.colorado.piq.service.ParkingLotStatusService;
 import edu.colorado.piq.util.CassandraUtil;
 
 /**
  * Implementation of ParkingLotStatusService interface.
  */
 public class ParkingLotStatusServiceImpl implements ParkingLotStatusService {
 	
 	/** Used to connect to Cassandra. */
 	private CassandraConfig cassandraConfig;
 	
 	/**
 	 * Instantiates a new parking lot status service impl.
 	 *
 	 * @param config the config
 	 */
 	public ParkingLotStatusServiceImpl(CassandraConfig config) {
 		this.cassandraConfig = config;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.colorado.piq.service.ParkingLotStatusService#updateParkingLotStatus(java.util.List)
 	 */
 	public void updateParkingLotStatus(List<ParkingLotInfo> parkingLots) {
 		Keyspace keyspace = CassandraUtil.Connect(cassandraConfig);
 		StringSerializer stringSerializer = StringSerializer.get();
 		HashMap<String, ParkingLotInfo> parkingLotInfoMap = createParkingLotInfoMap(parkingLots);
 		
 		// Get the last column as it represents the current status.
 		// Note that the columns are in format:
 		//	name: time stamp 
 		//	value: available space
 		QueryResult<Rows<String, String, String>> result = HFactory.createMultigetSliceQuery(
 				keyspace, 
 				stringSerializer, 
 				stringSerializer, 
 				stringSerializer)
 			.setColumnFamily(CF_LotStatusArchive.NAME)
 			// The rows for which to fetch columns
 			.setKeys(parkingLotInfoMap.keySet())
 			// Get the columns in reverse order and get only one column
 			.setRange("", "", true, 1)
 			.execute();
 		
 		// Update the status in the provided structure
 		for (Row<String, String, String> row : result.get()) {
 			if (!row.getColumnSlice().getColumns().isEmpty()) {
				String value = row.getColumnSlice().getColumns().get(0).getValue();
				try {
					int availableSpace = Integer.parseInt(value);
					parkingLotInfoMap.get(row.getKey()).setAvailableSpace(availableSpace);
				}
				catch (NumberFormatException e) {
					System.out.println(String.format("Corrupt data %1s found for key %2s", value, row.getKey()));
				}
 			}
 			else {
 				System.out.println(String.format("No data found for key %1s", row.getKey()));
 			}
 		}
 	}
 	
 	/**
 	 * Compile the key ranges of lot ids for which data will be fetched.
 	 *
 	 * @param parkingLots the parking lots
 	 * @return the list
 	 */
 	private HashMap<String, ParkingLotInfo> createParkingLotInfoMap(List<ParkingLotInfo> parkingLots) {
 		HashMap<String, ParkingLotInfo> keyRanges = new HashMap<String, ParkingLotInfo>(parkingLots.size());
 		
 		for (ParkingLotInfo lotInfo : parkingLots) {
 			String lotId = Integer.toString(lotInfo.getLotId());
 			keyRanges.put(lotId, lotInfo);
 		}
 		return keyRanges;
 	}
 }
