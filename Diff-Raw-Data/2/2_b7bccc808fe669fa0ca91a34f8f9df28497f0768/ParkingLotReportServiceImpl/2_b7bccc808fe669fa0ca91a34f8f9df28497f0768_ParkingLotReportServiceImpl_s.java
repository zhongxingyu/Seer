 package edu.colorado.piq.service.impl;
 
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.HSuperColumn;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.query.QueryResult;
 
 import org.apache.commons.lang.NotImplementedException;
 
 import edu.colorado.piq.Const.DataModel.SCF_QuarterlyAnalysis;
 import edu.colorado.piq.model.CassandraConfig;
 import edu.colorado.piq.model.report.QuarterReport;
 import edu.colorado.piq.model.report.WeekReport;
 import edu.colorado.piq.model.report.DayReport.Session;
 import edu.colorado.piq.model.report.WeekReport.Day;
 import edu.colorado.piq.service.ParkingLotInfoService;
 import edu.colorado.piq.service.ParkingLotReportService;
 import edu.colorado.piq.util.CassandraUtil;
 
 public class ParkingLotReportServiceImpl implements ParkingLotReportService {
 	private CassandraConfig cassandraConfig;
 	private ParkingLotInfoService parkingLotInfoService;
 	
 	public ParkingLotReportServiceImpl(CassandraConfig config, ParkingLotInfoService parkingLotInfoService) {
 		this.cassandraConfig = config;
 		this.parkingLotInfoService = parkingLotInfoService;
 	}
 	
 	@Override
 	public QuarterReport getQuarterReport(String quarter) {
 		throw new NotImplementedException("Specify the lotid to get report for a lot.");
 	}
 
 	@Override
 	public WeekReport getQuarterReportByLotId(String quarter, int lotId) {
 		WeekReport report = new WeekReport();
 		StringSerializer stringSerializer = StringSerializer.get();
 		Keyspace keyspace = CassandraUtil.Connect(this.cassandraConfig);
 		QueryResult<HSuperColumn<String, String, String>> results = HFactory.createSuperColumnQuery(
 				keyspace, 
 				stringSerializer, 
 				stringSerializer,
 				stringSerializer, 
 				stringSerializer)
 			.setColumnFamily(SCF_QuarterlyAnalysis.NAME)
 			.setSuperName(Integer.toString(lotId))
 			.setKey(quarter)
 			.execute();
 		
 		if (results.get() != null) {
 			for (HColumn<String, String> column : results.get().getColumns()) {
 				String[] keys = column.getName().split("_");
 				Day day = Day.parseDay(keys[0]);
 				Session session = Session.parseSession(keys[1]);
				int availableSpace = Integer.parseInt(column.getValue());
 				report.addDayReport(day, session, availableSpace);
 			}
 		}
 
 		report.setParkingLotInfo(parkingLotInfoService.getParkingLotInfo(lotId));
 
 		return report;
 	}
 }
