 package com.njtransit;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.MessageDigest;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * Hello world!
  * 
  */
 public class App {
 
 	private static DateFormat local = DateFormat.getDateTimeInstance(
 			DateFormat.MEDIUM, DateFormat.FULL);
 	private static DateFormat gmt = DateFormat.getDateTimeInstance(
 			DateFormat.MEDIUM, DateFormat.FULL);
 	private static DateFormat njt = new SimpleDateFormat("yyyyddMM");
 	private static DateFormat time = new SimpleDateFormat("yyyy-dd-mm kk:mm:ss");
 
 	static {
 		gmt.setTimeZone(TimeZone.getTimeZone("GMT"));
 		local.setTimeZone(TimeZone.getTimeZone("America/New_York"));
 		njt.setTimeZone(TimeZone.getTimeZone("America/New_York"));
 		time.setTimeZone(TimeZone.getTimeZone("America/New_York"));
 
 	}
 
 	private static Long gmt(Date date) {
 		long msFromEpochGmt = date.getTime();
 		int offsetFromUTC = TimeZone.getTimeZone("America/New_York").getOffset(
 				msFromEpochGmt);
 		Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
 		gmtCal.setTime(date);
 		gmtCal.add(Calendar.MILLISECOND, offsetFromUTC);
 		return gmtCal.getTimeInMillis();
 	}
 
 	public static void main(String[] args) throws Exception {
 		HttpClient c = new HttpClient();
 		PostMethod m = new PostMethod(
 				"https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevLoginSubmitTo");
 		m.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
 		m.setRequestEntity(new StringRequestEntity("userName="
 				+ System.getProperty("username") + "&password="
 				+ System.getProperty("password"),
 				"application/x-www-form-urlencoded", "utf-8"));
 		c.executeMethod(m);
 		m.releaseConnection();
 		GetMethod g = new GetMethod(
 				"https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevResourceDownloadTo&Category=rail");
 		File railData = new File(System.getProperty("zip_destination"));
 		if (railData.exists()) {
 			Date d = new Date(railData.lastModified());
 			System.out.println(local.format(d));
 			System.out.println(gmt.format(d));
 			g.addRequestHeader("If-Modified-Since", gmt.format(d));
 		}
 		c.executeMethod(g);
 		FileOutputStream fos = new FileOutputStream(railData);
 		BufferedOutputStream bos = new BufferedOutputStream(fos);
 		byte[] mybites = new byte[1024];
 		int read;
 		while ((read = g.getResponseBodyAsStream().read(mybites)) != -1) {
 			bos.write(mybites,0,read);
 		}
 		bos.close();
 		
 		ZipInputStream zis = new ZipInputStream(new FileInputStream(railData));
 		ZipEntry entry = zis.getNextEntry();
 		while(entry!=null) {
 			String entryName = entry.getName();
             FileOutputStream fileoutputstream;
             File newFile = new File(railData.getParent(),entryName);
             String directory = newFile.getParent();
             
             if(directory == null)
             {
                 if(newFile.isDirectory())
                     break;
             }
             
             fileoutputstream = new FileOutputStream(newFile);             
             System.out.println(newFile);
             while ((read = zis.read(mybites, 0, mybites.length)) > -1)
                 fileoutputstream.write(mybites, 0, read);
 
             fileoutputstream.close(); 
             zis.closeEntry();
             entry = zis.getNextEntry();
 		}
 
 		if (g.getResponseHeader("Last-Modified") != null) {
 			railData.setLastModified(local.parse(
 					g.getResponseHeader("Last-Modified").getValue()).getTime());
 		}
 
 		InputStream orig = App.class
 				.getResourceAsStream("../../njtransit.sqlite");
 		FileOutputStream out = new FileOutputStream(System
 				.getProperty("destination"));
 		while ((read = orig.read(mybites)) != -1) {
 			out.write(mybites,0,read);
 		}
 		out.close();
 
 		FileInputStream in = new FileInputStream(System
 				.getProperty("destination"));
 
 		Class.forName("org.sqlite.JDBC");
 		Connection conn = DriverManager.getConnection("jdbc:sqlite:"
 				+ System.getProperty("destination"));
 		Statement stat = conn.createStatement();
 		String[] creates = new String[] {
 				"create table if not exists trips(id int primary key, route_id int, service_id int, headsign varchar(255), direction int, block_id varchar(255))",
 				"create table if not exists stops(id int primary key, name varchar(255), desc varchar(255), lat real, lon real, zone_id)",
 				"create table if not exists stop_times(trip_id int, arrival int, departure int, stop_id int, sequence int, pickup_type int, drop_off_type int)",
 				"create table if not exists routes(id int primary key, agency_id int, short_name varchar(255), long_name varchar(255), route_type int)",
 				"create table if not exists calendar(service_id int, monday int, tuesday int, wednesday int, thursday int, friday int, saturday int, sunday int, start int, end int)",
 				// agency_id,agency_name,agency_url,agency_timezone
 				"create table if not exists calendar_dates(service_id int, calendar_date int, exception_type int)",
 				"create table if not exists agency(id int primary key, name varchar(255), url varchar(255))" };
 		for (String createTable : creates) {
 			stat.executeUpdate(createTable);
 		}
 
 		final TransactionManager manager = new TransactionManager(conn);
 
 		loadPartitioned(manager, "stop_times", new ContentValuesProvider() {
 
 			@Override
 			public List<List<Object>> getContentValues(CSVReader reader)
 					throws IOException {
 				List<List<Object>> values = new ArrayList<List<Object>>();
 				String[] nextLine;
 				while ((nextLine = reader.readNext()) != null) {
 					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
 					List<Object> o = new ArrayList<Object>();
 					o.add(nextLine[0]);
 					try {
 						if (nextLine[1].trim().length() != 0) {
							o.add(gmt(time.parse("1970-01-01 " + nextLine[1])));
 						}
 						if (nextLine[2].trim().length() != 0) {
							o.add(gmt(time.parse("1970-01-01 " + nextLine[2])));
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 						throw new RuntimeException(e);
 					}
 
 					o.add(nextLine[3]);
 					o.add(nextLine[4]);
 					o.add(nextLine[5]);
 					o.add(nextLine[6]);
 					values.add(o);
 				}
 				return values;
 			}
 
 			@Override
 			public String getInsertString() {
 				return "insert into stop_times (trip_id,arrival,departure,stop_id,sequence,pickup_type,drop_off_type) values (?,?,?,?,?,?,?)";
 			}
 
 		});
 
 		loadPartitioned(manager, "trips", new ContentValuesProvider() {
 
 			@Override
 			public List<List<Object>> getContentValues(CSVReader reader)
 					throws IOException {
 				List<List<Object>> values = new ArrayList<List<Object>>();
 				String[] nextLine;
 				while ((nextLine = reader.readNext()) != null) {
 					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
 					List<Object> o = new ArrayList<Object>();
 					o.add(nextLine[0]);
 					o.add(nextLine[1]);
 					o.add(nextLine[2]);
 					o.add(nextLine[3]);
 					o.add(nextLine[4]);
 					o.add(nextLine[5]);
 					values.add(o);
 				}
 				return values;
 			}
 
 			@Override
 			public String getInsertString() {
 				return "insert into trips (route_id,service_id,id,direction,headsign,block_id) values (?,?,?,?,?,?)";
 			}
 
 		});
 
 		loadPartitioned(manager, "stops", new ContentValuesProvider() {
 
 			@Override
 			public List<List<Object>> getContentValues(CSVReader reader)
 					throws IOException {
 				List<List<Object>> values = new ArrayList<List<Object>>();
 				String[] nextLine;
 				while ((nextLine = reader.readNext()) != null) {
 					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
 					List<Object> o = new ArrayList<Object>();
 					o.add(nextLine[0]);
 					o.add(nextLine[1]);
 					o.add(nextLine[2]);
 					o.add(nextLine[3]);
 					o.add(nextLine[4]);
 					o.add(nextLine[5]);
 					values.add(o);
 				}
 				return values;
 			}
 
 			@Override
 			public String getInsertString() {
 				// stop_id,stop_name,stop_desc,stop_lat,stop_lon,zone_id
 				return "insert into stops (id,name,desc,lat,lon,zone_id) values (?,?,?,?,?,?)";
 			}
 
 		});
 
 		loadPartitioned(manager, "calendar", new ContentValuesProvider() {
 
 			@Override
 			public List<List<Object>> getContentValues(CSVReader reader)
 					throws IOException {
 				List<List<Object>> values = new ArrayList<List<Object>>();
 				String[] nextLine;
 				while ((nextLine = reader.readNext()) != null) {
 					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
 					List<Object> o = new ArrayList<Object>();
 					o.add(nextLine[0]);
 					o.add(nextLine[1]);
 					o.add(nextLine[2]);
 					o.add(nextLine[3]);
 					o.add(nextLine[4]);
 					o.add(nextLine[5]);
 					o.add(nextLine[7]);
 					try {
 						o.add(gmt(njt.parse(nextLine[8])));
 					} catch (ParseException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
 					try {
 						o.add(gmt(njt.parse(nextLine[9])));
 					} catch (ParseException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					values.add(o);
 				}
 				return values;
 			}
 
 			@Override
 			public String getInsertString() {
 				// service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date
 				return "insert into calendar (service_id,monday,tuesday,wednesday,thursday,friday, saturday, sunday, start, end) values (?,?,?,?,?,?,?,?,?,?)";
 			}
 
 		});
 
 		loadPartitioned(manager, "calendar_dates", new ContentValuesProvider() {
 
 			@Override
 			public List<List<Object>> getContentValues(CSVReader reader)
 					throws IOException {
 				reader.readNext();
 				List<List<Object>> values = new ArrayList<List<Object>>();
 				String[] nextLine;
 				while ((nextLine = reader.readNext()) != null) {
 					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
 					List<Object> o = new ArrayList<Object>();
 					o.add(nextLine[0]);
 					String start = nextLine[1];
 					try {
 						o.add(gmt(njt.parse(start)));
 					} catch (ParseException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					o.add(nextLine[2]);
 					values.add(o);
 				}
 				return values;
 			}
 
 			@Override
 			public String getInsertString() {
 				// service_id,date,exception_type
 				return "insert into calendar_dates (service_id,calendar_date,exception_type) values (?,?,?)";
 			}
 
 		});
 
 		loadPartitioned(manager, "routes", new ContentValuesProvider() {
 
 			@Override
 			public List<List<Object>> getContentValues(CSVReader reader)
 					throws IOException {
 				List<List<Object>> values = new ArrayList<List<Object>>();
 				String[] nextLine;
 				while ((nextLine = reader.readNext()) != null) {
 					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
 					List<Object> o = new ArrayList<Object>();
 					o.add(nextLine[0]);
 					o.add(nextLine[1]);
 					o.add(nextLine[2]);
 					o.add(nextLine[3]);
 					o.add(nextLine[4]);
 					values.add(o);
 				}
 				return values;
 			}
 
 			@Override
 			public String getInsertString() {
 				// route_id,agency_id,route_short_name,route_long_name,route_type
 				return "insert into routes (id,agency_id,short_name,long_name,route_type) values (?,?,?,?,?)";
 			}
 
 		});
 
 		loadPartitioned(manager, "agency", new ContentValuesProvider() {
 
 			@Override
 			public List<List<Object>> getContentValues(CSVReader reader)
 					throws IOException {
 				List<List<Object>> values = new ArrayList<List<Object>>();
 				String[] nextLine;
 				while ((nextLine = reader.readNext()) != null) {
 					// trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type
 					List<Object> o = new ArrayList<Object>();
 					o.add(nextLine[0]);
 					o.add(nextLine[1]);
 					o.add(nextLine[2]);
 					values.add(o);
 				}
 				return values;
 			}
 
 			@Override
 			public String getInsertString() {
 				// agency_id,agency_name,agency_url
 				return "insert into agency (id,name,url) values (?,?,?)";
 			}
 
 		});
 
 		MessageDigest d = MessageDigest.getInstance("SHA-1");
		
		while ((read = in.read(mybites)) != -1) {
			d.update(mybites, 0, read);
 		}
 		out = new FileOutputStream(System.getProperty("destination") + ".sha");
 		out.write(convertToHex(d.digest()).getBytes());
 		out.close();
 	}
 
 	private static String convertToHex(byte[] data) {
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0; i < data.length; i++) {
 			int halfbyte = (data[i] >>> 4) & 0x0F;
 			int two_halfs = 0;
 			do {
 				if ((0 <= halfbyte) && (halfbyte <= 9))
 					buf.append((char) ('0' + halfbyte));
 				else
 					buf.append((char) ('a' + (halfbyte - 10)));
 				halfbyte = data[i] & 0x0F;
 			} while (two_halfs++ < 1);
 		}
 		return buf.toString();
 	}
 
 	private static void loadPartitioned(TransactionManager tm,
 			final String tableName, final ContentValuesProvider valuesProvider)
 			throws SQLException {
 		load(tm, tableName, valuesProvider);
 	}
 
 	private static void load(TransactionManager tm, final String tableName,
 			final ContentValuesProvider valuesProvider) throws SQLException {
 		tm.exec(new Transactional() {
 			@Override
 			public void work(Connection conn) {
 				InputStream input = null;
 				CSVReader reader = null;
 				try {
 					final String fileName = tableName + ".txt";
 					input = new FileInputStream(new File(System.getProperty("zip_destination")).getParent()+"/"+fileName);
 					reader = new CSVReader(new InputStreamReader(input));
 					reader.readNext(); // read in the header line
 					List<List<Object>> values = valuesProvider
 							.getContentValues(reader);
 					PreparedStatement s = conn.prepareStatement(valuesProvider
 							.getInsertString());
 					for (List<Object> cv : values) {
 						for (int i = 0; i < cv.size(); i++) {
 							s.setObject(i + 1, cv.get(i));
 						}
 						s.addBatch();
 					}
 					System.out.println(s.executeBatch().length);
 				} catch (Throwable t) {
 					t.printStackTrace();
 					throw new RuntimeException(t);
 				} finally {
 					try {
 						input.close();
 						reader.close();
 					} catch (Throwable t) {
 						t.printStackTrace();
 						throw new RuntimeException(t);
 					}
 				}
 			}
 		});
 	}
 
 }
