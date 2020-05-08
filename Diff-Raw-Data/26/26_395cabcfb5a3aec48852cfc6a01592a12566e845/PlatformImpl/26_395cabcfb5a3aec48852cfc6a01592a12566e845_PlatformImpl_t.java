 /**
  * 
  */
 package edu.thu.keg.mdap_impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.naming.OperationNotSupportedException;
 
 //import org.apache.commons.logging.Log;
 
 import edu.thu.keg.mdap.DataProviderManager;
 import edu.thu.keg.mdap.DataSetManager;
 import edu.thu.keg.mdap.Platform;
 import edu.thu.keg.mdap.datafeature.DataFeatureType;
 import edu.thu.keg.mdap.datafeature.DataView;
 import edu.thu.keg.mdap.datamodel.AggregatedDataField;
 import edu.thu.keg.mdap.datamodel.DataField;
 import edu.thu.keg.mdap.datamodel.AggregatedDataField.AggrFunction;
 import edu.thu.keg.mdap.datamodel.DataField.FieldFunctionality;
 import edu.thu.keg.mdap.datamodel.DataField.FieldType;
 import edu.thu.keg.mdap.datamodel.DataSet;
 import edu.thu.keg.mdap.datamodel.GeneralDataField;
 import edu.thu.keg.mdap.datamodel.Query;
 import edu.thu.keg.mdap.datamodel.Query.Operator;
 import edu.thu.keg.mdap.datamodel.Query.Order;
 import edu.thu.keg.mdap.provider.DataProvider;
 import edu.thu.keg.mdap.provider.DataProviderException;
 import edu.thu.keg.mdap_impl.datamodel.DataSetImpl;
 
 /**
  * @author Yuanchao Ma
  * 
  */
 public class PlatformImpl implements Platform {
 
 	public PlatformImpl(String file) {
 		try {
 			Config.init(file);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.thu.keg.mdap.Platform#getDataSetManager()
 	 */
 	@Override
 	public DataSetManager getDataSetManager() {
 		return DataSetManagerImpl.getInstance();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.thu.keg.mdap.Platform#getDataProviderManager()
 	 */
 	@Override
 	public DataProviderManager getDataProviderManager() {
 		return DataProviderManagerImpl.getInstance();
 	}
 
 	public void crud() {
 		// Platform p = new PlatformImpl(
 		// "C:\\Users\\ybz\\GitHub\\keg-operator-data\\platform\\config.xml");
 		// Construct a new dataset
 		DataProvider provider = getDataProviderManager().getDefaultSQLProvider(
 				"BeijingData", "ybz_sql", "root");
 		DataProvider orclProvider = getDataProviderManager()
 				.getDefaultOracleProvider("orcl", "bj_tuoming", "root");
 		DataProvider hiveProvider = getDataProviderManager()
 				.getDefaultHiveProvider("default", null, null);
 
 		Query q = null;
 		DataField[] fields = null;
 		DataSet dsSite = null;
 		DataView dv = null;
 
 		fields = new DataField[5];
 		fields[0] = new GeneralDataField("EN_NAME", FieldType.ShortString, "",
 				true, false, true, FieldFunctionality.Identifier);
 		fields[1] = new GeneralDataField("LAC", FieldType.Int, "", false,
 				false, true, FieldFunctionality.Value);
 		fields[2] = new GeneralDataField("CI", FieldType.Int, "", false, false,
 				true, FieldFunctionality.Value);
 		fields[3] = new GeneralDataField("LONGITUDE", FieldType.Double, "",
 				false, false, true, FieldFunctionality.Longitude);
 		fields[4] = new GeneralDataField("LATITUDE", FieldType.Double, "",
 				false, false, true, FieldFunctionality.Latitude);
 		try {
 			dsSite = getDataSetManager().createDataSet("TESTF", "liqi",
 					"小区地理位置信息hadoop", hiveProvider, true, fields);
 			getDataSetManager().setDataSetPermission(dsSite.getId(), "liqi",
 					DataSetImpl.PERMISSION_PUBLIC, null);
 
 			q = dsSite.getQuery().select(dsSite.getField("EN_NAME"),
 					dsSite.getField("LATITUDE"), dsSite.getField("LONGITUDE"));
 			dv = getDataSetManager().defineView("Lac_Ci_Map_hadoop",
 					dsSite.getOwner(), dsSite.getId(), "小区分布图hadoop",
 					DataFeatureType.GeoFeature, q);
 			dv.setDescription(Locale.ENGLISH, "Area distribution hadoop");
 		} catch (OperationNotSupportedException | IllegalArgumentException
 				| DataProviderException e1) {
 			// TODO Auto-generated catch block
 			System.out.println(e1.getMessage());
 		}
 
 		// 1st oracle
 		fields = new DataField[5];
 		fields[0] = new GeneralDataField("AREANAME_EN", FieldType.ShortString,
 				"", true, false, true, FieldFunctionality.Identifier);
 		fields[1] = new GeneralDataField("LAC", FieldType.Int, "", false,
 				false, true, FieldFunctionality.Value);
 		fields[2] = new GeneralDataField("CI", FieldType.Int, "", false, false,
 				true, FieldFunctionality.Value);
 		fields[3] = new GeneralDataField("LONGITUDE", FieldType.Double, "",
 				false, false, true, FieldFunctionality.Longitude);
 		fields[4] = new GeneralDataField("LATITUDE", FieldType.Double, "",
 				false, false, true, FieldFunctionality.Latitude);
 		try {
 			dsSite = getDataSetManager().createDataSet("Lac_Ci_Map", "ybz",
 					"小区地理位置信息orcl", orclProvider, true, fields);
 			getDataSetManager().setDataSetPermission(dsSite.getId(), "ybz",
 					DataSetImpl.PERMISSION_PUBLIC, null);
 
 			q = dsSite.getQuery().select(dsSite.getField("AREANAME_EN"),
 					dsSite.getField("LATITUDE"), dsSite.getField("LONGITUDE"));
 			dv = getDataSetManager().defineView("Lac_Ci_Map_orcl",
 					dsSite.getOwner(), dsSite.getId(), "小区分布图",
 					DataFeatureType.GeoFeature, q);
 			dv.setDescription(Locale.ENGLISH, "Area distribution orcl");
 		} catch (OperationNotSupportedException | IllegalArgumentException
 				| DataProviderException e1) {
 			// TODO Auto-generated catch block
 			System.out.println(e1.getMessage());
 		}
 
 		// 1st DataSet
 		// fields = new DataField[2];
 		// fields[0] = new GeneralDataField("WebsiteId", FieldType.Int, "",
 		// true,
 		// false, true, FieldFunctionality.Identifier);
 		// fields[1] = new GeneralDataField("URL", FieldType.ShortString, "",
 		// false, false, true, FieldFunctionality.Other);
 		// dsSite = getDataSetManager().createDataSet("WebsiteId_URL", "myc",
 		// "网站信息", provider, true, fields);
 		// List<String> users = new ArrayList<>();
 		// users.add("wc");
 		// users.add("xm");
 		// getDataSetManager().setDataSetPermission(dsSite.getId(), "myc",
 		// DataSetImpl.PERMISSION_LIMITED, users);
 		// 2nd DataSet
 		// fields = new DataField[4];
 		// fields[0] = new GeneralDataField("Region", FieldType.Int, "", true,
 		// false, true, FieldFunctionality.Other);
 		// fields[1] = new GeneralDataField("Name", FieldType.ShortString, "",
 		// false, false, true, FieldFunctionality.Other);
 		// fields[2] = new GeneralDataField("Latitude", FieldType.Double, "",
 		// false, false, true, FieldFunctionality.Latitude);
 		// fields[3] = new GeneralDataField("Longitude", FieldType.Double, "",
 		// false, false, true, FieldFunctionality.Longitude);
 		// dsSite = getDataSetManager().createDataSet("RegionInfo3", "xm",
 		// "地理区域",
 		// provider, true, fields);
 		// 3rd DataSet
 		fields = new DataField[5];
 		fields[0] = new GeneralDataField("SiteId", FieldType.Int, "", true,
 				false, true, FieldFunctionality.Identifier);
 		fields[1] = new GeneralDataField("SiteName", FieldType.ShortString, "",
 				false, false, true, FieldFunctionality.Identifier);
 		fields[2] = new GeneralDataField("Latitude", FieldType.Double, "",
 				false, false, true, FieldFunctionality.Latitude);
 		fields[3] = new GeneralDataField("Longitude", FieldType.Double, "",
 				false, false, true, FieldFunctionality.Longitude);
 		fields[4] = new GeneralDataField("Region", FieldType.Int, "", false,
 				false, true, FieldFunctionality.Other);
 		try {
			dsSite = getDataSetManager().createDataSet("RegionInfo2", "ybz",
 					"基站信息", provider, true, fields);
			getDataSetManager().setDataSetPermission(dsSite.getId(), "ybz",
					DataSetImpl.PERMISSION_PUBLIC, null);
 			// 3rd DataView
 
 			q = dsSite
 					.getQuery()
 					.select(dsSite.getField("Region"),
 							new AggregatedDataField(dsSite.getField("SiteId"),
 									AggrFunction.COUNT, "SiteCount", null))
 					.orderBy("SiteCount", Order.DESC);
 			dv = getDataSetManager()
 					.defineView("RegionSta", dsSite.getOwner(), "区域内基站数统计",
 							dsSite.getId(), DataFeatureType.ValueFeature, q);
 			dv.setDescription(Locale.ENGLISH, "Cell tower count within regions");
 			// ----------------------------------------测试查询
 			System.out.println("select1: "
 					+ q.whereOr(q.getFields()[0].getName(), Operator.EQ, 1)
 							.toString());
 			Map<DataField, DataField> fm1 = new HashMap<DataField, DataField>();
 			Map<DataField, DataField> fm2 = new HashMap<DataField, DataField>();
 			Query q2 = dsSite.getQuery();
 			System.out
 					.println("select2: "
 							+ q2.whereOr(q2.getFields()[0].getName(),
 									Operator.EQ, 3439)
 									.whereAnd(q2.getFields()[0].getName(),
 											Operator.EQ, 3435)
 									.whereAnd(q2.getFields()[0].getName(),
 											Operator.EQ, 3436)
 									.whereOr(q2.getFields()[0].getName(),
 											Operator.GT, 1000)
 									.whereOr(q2.getFields()[0].getName(),
 											Operator.GT, 1001)
 									.whereAnd(q2.getFields()[0].getName(),
 											Operator.EQ, 1002).toString());
 			fm1.put(q2.getFields()[0].clone(), q.getFields()[0].clone());
 			fm2.put(q.getFields()[0].clone(), q2.getFields()[0].clone());
 
 			System.out.println("JOIN1: "
 					+ q2.join(q, fm1).whereOr("SiteId", Operator.EQ, 193)
 							.toString());
 			System.out.println("JOIN2: " + q.join(q2, fm2).toString());
 
 		} catch (OperationNotSupportedException | IllegalArgumentException
 				| DataProviderException e1) {
 			// TODO Auto-generated catch block
 			System.out.println(e1.getMessage());
 		}
 
 		// 4th DataSet
 		// fields = new DataField[6];
 		// fields[0] = new GeneralDataField("Domain", FieldType.LongString,
 		// "Domain", true, false, true, FieldFunctionality.Identifier);
 		// fields[1] = new GeneralDataField("DayCount", FieldType.Int,
 		// "appear days of this domain", false, false, true,
 		// FieldFunctionality.Value);
 		// fields[2] = new GeneralDataField("HourCount", FieldType.Int,
 		// "appear hours of this domain", false, false, true,
 		// FieldFunctionality.Value);
 		// fields[3] = new GeneralDataField("LocCount", FieldType.Int,
 		// "appear locations of this domain", false, false, true,
 		// FieldFunctionality.Value);
 		// fields[4] = new GeneralDataField("UserCount", FieldType.Int,
 		// "number of users visiting this domain", false, false, true,
 		// FieldFunctionality.Value);
 		// fields[5] = new GeneralDataField("TotalCount", FieldType.Int,
 		// "total visits of this domain", false, false, true,
 		// FieldFunctionality.Value);
 		// getDataSetManager().createDataSet("FilteredByCT_Domain", "wc",
 		// "Domain statistics", provider, false, fields);
 		// 5th DataSet
 		fields = new DataField[2];
 		fields[0] = new GeneralDataField("ContentType", FieldType.LongString,
 				"Content Type of websites", true, false, true,
 				FieldFunctionality.Identifier);
 		fields[1] = new GeneralDataField("times", FieldType.Int,
 				"appear times of the ContentType", false, false, true,
 				FieldFunctionality.Value);
 		try {
 			dsSite = getDataSetManager().createDataSet(
					"DataAggr_ContentTypes_Up90", "ybz", "ContentType分布集",
 					provider, true, fields);
			getDataSetManager().setDataSetPermission(dsSite.getId(), "ybz",
					DataSetImpl.PERMISSION_PUBLIC, null);
 			// 5th DataView
 
 			q = dsSite.getQuery().orderBy("times", Order.DESC);
 			dv = getDataSetManager().defineView("ContentTypeView",
 					dsSite.getOwner(), dsSite.getId(), "ContentType分布",
 
 					DataFeatureType.DistributionFeature, q);
 			dv.setDescription(Locale.ENGLISH, "ContentType distribution");
 		} catch (OperationNotSupportedException | IllegalArgumentException
 				| DataProviderException e1) {
 			System.out.println(e1.getMessage());
 		}
 
 		// 6th DataSet
 		fields = new DataField[4];
 		fields[0] = new GeneralDataField("Imsi", FieldType.ShortString,
 				"User IMSI", true, false, true, FieldFunctionality.Identifier);
 		fields[1] = new GeneralDataField("WebsiteCount", FieldType.Int,
 				"Total count of visited websites", false, false, true,
 				FieldFunctionality.Value);
 		fields[2] = new GeneralDataField("RegionCount", FieldType.Int,
 				"Total count of appeared regions", false, false, true,
 				FieldFunctionality.Value);
 		fields[3] = new GeneralDataField("TotalCount", FieldType.Int,
 				"Total count of requests", false, false, true,
 				FieldFunctionality.Value);
 
 		try {
 			dsSite = getDataSetManager().createDataSet("slot_Imsi_All", "myc",
 					"User statistics by time slot", provider, true, fields);
 			// 6th DataView
 
 			DataField va = new AggregatedDataField(dsSite.getField("Imsi"),
 					AggrFunction.COUNT, "UserCount", null);
 			q = dsSite.getQuery().select(dsSite.getField("WebsiteCount"), va)
 					.orderBy("WebsiteCount", Order.ASC);
 			DataField[] vas = { va };
 			dv = getDataSetManager().defineView("UserWebsiteCountView",
 					dsSite.getOwner(), dsSite.getId(), "用户网站访问数分布",
 					DataFeatureType.DistributionFeature, q,
 					new DataField[] { dsSite.getField("WebsiteCount") }, vas);
 			dv.setDescription(Locale.ENGLISH, "User website count distribution");
 		} catch (OperationNotSupportedException | IllegalArgumentException
 				| DataProviderException e1) {
 			System.out.println(e1.getMessage());
 		}
 		// 7th DataSet
 		fields = new DataField[3];
 		fields[0] = new GeneralDataField("SiteId", FieldType.ShortString,
 				"基站ID", true, false, true, FieldFunctionality.Identifier);
 		fields[1] = new GeneralDataField("ConnHour", FieldType.Int, "时间段(小时)",
 				false, false, true, FieldFunctionality.Identifier);
 		fields[2] = new GeneralDataField("TotalCount", FieldType.Int, "连接数",
 				false, false, true, FieldFunctionality.Value);
 
 		try {
 			dsSite = getDataSetManager().createDataSet("SiteId_ConnHour",
 					"myc", "每小时每基站连接数", provider, true, fields);
 			// 7th DataView
 
 			q = dsSite
 					.getQuery()
 					.select(dsSite.getField("ConnHour"),
 							new AggregatedDataField(dsSite
 									.getField("TotalCount"), AggrFunction.MAX,
 									"TotalCount", null))
 					.orderBy("ConnHour", Order.ASC);
 			getDataSetManager()
 					.defineView("ConnHourView", dsSite.getOwner(),
 							dsSite.getId(), "每小时连接数统计",
 							DataFeatureType.ValueFeature, q);
 		} catch (OperationNotSupportedException | IllegalArgumentException
 				| DataProviderException e1) {
 			System.out.println(e1.getMessage());
 		}
 		// 8th DataSet
 		fields = new DataField[4];
 		fields[0] = new GeneralDataField("SiteId", FieldType.ShortString,
 				"基站ID", true, false, true, FieldFunctionality.Identifier);
 		fields[1] = new GeneralDataField("ConnectTime", FieldType.DateTime,
 				"连接时间", false, false, true, FieldFunctionality.TimeStamp);
 		fields[2] = new GeneralDataField("Latitude", FieldType.Double, "经度",
 				false, false, true, FieldFunctionality.Latitude);
 		fields[3] = new GeneralDataField("Longitude", FieldType.Double, "纬度",
 				false, false, true, FieldFunctionality.Longitude);
 
 		try {
 			dsSite = getDataSetManager().createDataSet("group_data", "ybz",
 					"分组数据_时间经纬度", provider, true, fields);
 
 			q = dsSite.getQuery();
 			getDataSetManager().defineView("Timeseries_Connect_info_data",
 					dsSite.getOwner(), dsSite.getId(), "时间_连接地理位置",
 					DataFeatureType.TimeSeries, q);
 			getDataSetManager().defineView("Location_Connect_info_data",
 					dsSite.getOwner(), dsSite.getId(), "位置_连接时间",
 					DataFeatureType.GeoFeature, q);
 		} catch (OperationNotSupportedException | DataProviderException
 				| IllegalArgumentException e1) {
 
 			// TODO Auto-generated catch block
 			System.out.println(e1.getMessage());
 		}
 		// fields = new DataField[2];
 		// fields[0] = new GeneralDataField("ContentType", FieldType.LongString,
 		// "Content Type of websites", true);
 		// fields[1] = new GeneralDataField("times", FieldType.Int,
 		// "appear times of the ContentType", false);
 		// DataSet tds2 = p.getDataSetManager().createDataSet("New_Test",
 		// "Top 90% Content Type distribution", provider, fields, true,
 		// new StatisticsFeature(fields[0], fields[1]));
 
 		// try {
 		// tds2.writeData(tds.getQuery());
 		// } catch (OperationNotSupportedException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// } catch (DataProviderException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 
 		// //remove a dataset
 		// try {
 		// p.getDataSetManager().removeDataSet(tds2);
 		// } catch (DataProviderException e) {
 		// // TODO Auto-generated catch block
 		// e.printStackTrace();
 		// }
 
 		try {
 			getDataSetManager().saveChanges();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("----------------------------"
 				+ getDataSetManager().getDataSetList().size());
 		for (DataSet ds : getDataSetManager().getDataSetList()) {
 			System.out.println(ds.getId() + " " + ds.getOwner() + " "
 					+ DataSetImpl.permissionToString(ds.getPermission()) + " "
 					+ ds.getName() + " " + ds.getDescription());
 		}
 		System.out.println("PUBLIC----------------------------- "
 				+ getDataSetManager().getPublicDataSetList().size());
 		for (DataSet ds : getDataSetManager().getPublicDataSetList()) {
 			System.out.println(ds.getId() + " " + ds.getOwner() + " "
 					+ ds.getName() + " " + ds.getDescription());
 		}
 		// System.out.println("LIMITED----------------------------- "
 		// + getDataSetManager().getLimitedDataSetList("wc").size());
 		// for (DataSet ds : getDataSetManager().getLimitedDataSetList("wc")) {
 		// System.out.println(ds.getId() + " " + ds.getOwner() + " "
 		// + ds.getName() + " " + ds.getLimitedUsers() + " "
 		// + ds.getDescription());
 		// }
 
 		System.out.println("END-----------------------------");
 		// System.out.println("myc-----------------------------"
 		// + getDataSetManager().getPrivateDataSetList("myc").size());
 		// for (DataSet ds : getDataSetManager().getPrivateDataSetList("myc")) {
 		// System.out.println(ds.getName() + " " + ds.getDescription());
 		// }
 		//
 		// System.out.println("wc-----------------------------"
 		// + getDataSetManager().getPrivateDataSetList("wc").size());
 		// for (DataSet ds : getDataSetManager().getPrivateDataSetList("wc")) {
 		// System.out.println(ds.getName() + " " + ds.getDescription());
 		// }
 		// System.out.println();
 	}
 
 	public static void main(String[] args) {
 
 		PlatformImpl p = new PlatformImpl("config.xml");
 		p.crud();
 		// p.query();
 	}
 
 	private void query() {
 		// Get a dataset
 		for (DataView v : getDataSetManager().getDataViewList(
 				DataFeatureType.ValueFeature)) {
 			System.out.println(v.getName());
 			System.out.println(v.getFeatureType());
 			try {
 				System.out.println(v.getQuery().toString());
 			} catch (OperationNotSupportedException | DataProviderException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		DataView dv = getDataSetManager().getDataView("UserWebsiteCountView");
 		Query q, q1;
 		DataSet ds = getDataSetManager().getDataSet("TESTF");
 		try {
 			q = dv.getQuery();
 			q1 = ds.getQuery();
 			q1.open();
 			while (q1.next()) {
 				// System.out.println(q.getValue(dv.getKeyField()) + " "
 				// + q.getValue(dv.getValueFields()[0]));
 				System.out.println(q1.getValue(ds.getField("LAC")) + " "
 						+ q1.getValue(ds.getField("CI")));
 			}
 			q1.close();
 		} catch (OperationNotSupportedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DataProviderException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// for (DataSet ds :
 		// getDataSetManager().getDataSetList(DataFeatureType.GeoFeature)) {
 		// //Read data from a dataset
 		// try {
 		// System.out.println(ds.getDescription());
 		// DataContent q = ds.getQuery();
 		// // GeoDataSet gds = (GeoDataSet)ds.getFeature(GeoDataSet.class);
 		// q.open();
 		// int count = 0;
 		// DataFeature feature =
 		// ds.getFeature(DataFeatureType.DistributionFeature);
 		// while (q.next()) {
 		// count ++;
 		// System.out.println(
 		// q.getValue(feature.getKeyFields()[0]).toString()
 		// + " " + q.getValue(feature.getValueFields()[0]).toString());
 		// }
 		// q.close();
 		// System.out.println(count);
 		// } catch (DataProviderException | OperationNotSupportedException ex) {
 		// ex.printStackTrace();
 		// }
 		// }
 		// DataSet ds = getDataSetManager().getDataSet("RegionInfo2");
 		// try {
 		// System.out.println(ds.getDescription());
 		// DataField aggr = new AggregatedDataField(ds.getField("SiteId"),
 		// AggrFunction.COUNT, "TotalCount");
 		// DataContent q = ds.getQuery()
 		// .select(aggr, ds.getField("Region")).orderBy("TotalCount", Order.ASC)
 		// .where("Region", Operator.GT,100).where("TotalCount", Operator.GT,
 		// 1);
 		// // GeoDataSet gds = (GeoDataSet)ds.getFeature(GeoDataSet.class);
 		// // System.out.println(q.ge)
 		// Query qq = (Query)q;
 		// System.out.println(qq.getProvider().getQueryString(qq));
 		// q.open();
 		// int count = 0;
 		// while (q.next()) {
 		// count ++;
 		// System.out.println(
 		// q.getValue(q.getFields()[0]).toString()
 		// + " " + q.getValue(q.getFields()[1]).toString());
 		// }
 		// q.close();
 		// System.out.println(count);
 		// } catch (DataProviderException | OperationNotSupportedException ex) {
 		// ex.printStackTrace();
 		// }
 		// ds = getDataSetManager().getDataSet("RegionInfo2");
 		// System.out.println("ss".equals(new String("ss")));
 
 	}
 }
