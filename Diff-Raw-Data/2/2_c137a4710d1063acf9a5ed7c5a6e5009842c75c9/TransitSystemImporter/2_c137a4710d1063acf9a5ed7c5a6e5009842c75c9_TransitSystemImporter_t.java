 package com.eatthepath.gtfs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.nio.charset.Charset;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.csvreader.CsvReader;
 
 public class TransitSystemImporter {
 	
 	private static final DateFormat gtfsDateFormat = new SimpleDateFormat("yyyyMMdd");
 	
 	private interface CsvLoader {
 		void loadRow(CsvReader reader, PreparedStatement insertStatement) throws IOException, SQLException;
 	}
 	
 	final File schemaDefinitionFile;
 	
 	public TransitSystemImporter(final File schemaDefinitionFile) {
 		if (!schemaDefinitionFile.exists()) {
 			throw new IllegalArgumentException(String.format("Schema definition file %s does not exist.", schemaDefinitionFile.getAbsolutePath()));
 		}
 		
 		if (!schemaDefinitionFile.isFile()) {
 			throw new IllegalArgumentException(String.format("Schema definition file %s exists but is not a file.", schemaDefinitionFile.getAbsolutePath()));
 		}
 		
 		this.schemaDefinitionFile = schemaDefinitionFile;
 	}
 	
 	public static String getJdbcUrlForFile(final File file) {
 		return String.format("jdbc:sqlite:%s", file.getAbsolutePath());
 	}
 	
 	public void importFromZipFile(final ZipFile file, final File outputFile) throws IOException, SQLException {
 		
 		if (outputFile.exists()) {
 			throw new IOException(String.format("Destination file %s already exists.", outputFile.getAbsolutePath()));
 		}
 		
 		final Connection connection = DriverManager.getConnection(TransitSystemImporter.getJdbcUrlForFile(outputFile));
 		
 		try {
 			this.initializeDatabase(connection);
 			
 			{
 				final InputStream agenciesInputStream = file.getInputStream(file.getEntry("agency.txt"));
 				
 				try {
 					this.loadAgencies(agenciesInputStream, connection);
 				} finally {
 					try {
 						agenciesInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final InputStream stopsInputStream = file.getInputStream(file.getEntry("stops.txt"));
 				
 				try {
 					this.loadStops(stopsInputStream, connection);
 				} finally {
 					try {
 						stopsInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final InputStream routesInputStream = file.getInputStream(file.getEntry("routes.txt"));
 				
 				try {
 					this.loadRoutes(routesInputStream, connection);
 				} finally {
 					try {
 						routesInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final InputStream calendarInputStream = file.getInputStream(file.getEntry("calendar.txt"));
 				
 				try {
 					this.loadCalendar(calendarInputStream, connection);
 				} finally {
 					try {
 						calendarInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final InputStream calendarDatesInputStream = file.getInputStream(file.getEntry("calendar_dates.txt"));
 				
 				try {
 					this.loadCalendarDates(calendarDatesInputStream, connection);
 				} finally {
 					try {
 						calendarDatesInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final InputStream shapesInputStream = file.getInputStream(file.getEntry("shapes.txt"));
 				
 				try {
 					this.loadShapes(shapesInputStream, connection);
 				} finally {
 					try {
 						shapesInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final InputStream tripInputStream = file.getInputStream(file.getEntry("trips.txt"));
 				
 				try {
 					this.loadTrips(tripInputStream, connection);
 				} finally {
 					try {
 						tripInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final InputStream stopTimeInputStream = file.getInputStream(file.getEntry("stop_times.txt"));
 				
 				try {
 					this.loadStopTimes(stopTimeInputStream, connection);
 				} finally {
 					try {
 						stopTimeInputStream.close();
 					} catch (IOException e) {
 						// TODO Log this exception
 					}
 				}
 			}
 			
 			{
 				final ZipEntry fareAttributesEntry = file.getEntry("fare_attributes.txt");
 				
 				if (fareAttributesEntry != null) {
 					final InputStream fareAttributesInputStream = file.getInputStream(fareAttributesEntry);
 					
 					try {
 						this.loadFareAttributes(fareAttributesInputStream, connection);
 					} finally {
 						try {
 							fareAttributesInputStream.close();
 						} catch (IOException e) {
 							// TODO Log this exception
 						}
 					}
 				}
 			}
 			
 			{
 				final ZipEntry fareRulesEntry = file.getEntry("fare_rules.txt");
 				
 				if (fareRulesEntry != null) {
 					final InputStream fareRulesInputStream = file.getInputStream(fareRulesEntry);
 					
 					try {
 						this.loadFareRules(fareRulesInputStream, connection);
 					} finally {
 						try {
 							fareRulesInputStream.close();
 						} catch (IOException e) {
 							// TODO Log this exception
 						}
 					}
 				}
 			}
 			
 			{
 				final ZipEntry frequenciesEntry = file.getEntry("frequencies.txt");
 				
 				if (frequenciesEntry != null) {
 					final InputStream frequenciesInputStream = file.getInputStream(frequenciesEntry);
 					
 					try {
 						this.loadFrequencies(frequenciesInputStream, connection);
 					} finally {
 						try {
 							frequenciesInputStream.close();
 						} catch (IOException e) {
 							// TODO Log this exception
 						}
 					}
 				}
 			}
 			
 			{
 				final ZipEntry transfersEntry = file.getEntry("transfers.txt");
 				
 				if (transfersEntry != null) {
 					final InputStream transfersInputStream = file.getInputStream(transfersEntry);
 					
 					try {
 						this.loadTransfers(transfersInputStream, connection);
 					} finally {
 						try {
 							transfersInputStream.close();
 						} catch (IOException e) {
 							// TODO Log this exception
 						}
 					}
 				}
 			}
 			
 			{
 				final ZipEntry feedInfoEntry = file.getEntry("feed_info.txt");
 				
 				if (feedInfoEntry != null) {
 					final InputStream feedInfoInputStream = file.getInputStream(feedInfoEntry);
 					
 					try {
 						this.loadFeedInfo(feedInfoInputStream, connection);
 					} finally {
 						try {
 							feedInfoInputStream.close();
 						} catch (IOException e) {
 							// TODO Log this exception
 						}
 					}
 				}
 			}
 		} finally {
 			try {
 				connection.close();
 			} catch (SQLException e) {
 				// TODO Log this exception
 			}
 		}
 	}
 	
 	private void initializeDatabase(final Connection connection) throws SQLException, IOException {
 		final BufferedReader reader = new BufferedReader(new FileReader(this.schemaDefinitionFile));
 		final StringBuilder builder = new StringBuilder();
 		
 		try {
 			String line = reader.readLine();
 			
 			while (line != null) {
 				builder.append(line);
 				line = reader.readLine();
 			}
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			reader.close();
 		}
 		
 		final Statement statement = connection.createStatement();
 		
 		try {
 			statement.executeUpdate(builder.toString());
 		} finally {
 			TransitSystemImporter.closeStatementIfPossible(statement);
 		}
 	}
 	
 	private void loadAgencies(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO agency (agency_id, agency_name, agency_url, agency_timezone, agency_lang, agency_phone, agency_fare_url) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String id = StringUtils.trimToNull(reader.get("agency_id"));
 				final String name = reader.get("agency_name");
 				final String url = StringUtils.trimToNull(reader.get("agency_url"));
 				final String timezone = reader.get("agency_timezone");
 				final String lang = StringUtils.trimToNull(reader.get("agency_lang"));
 				final String phone = StringUtils.trimToNull(reader.get("agency_phone"));
 				final String fareUrl = StringUtils.trimToNull(reader.get("agency_fare_url"));
 				
 				TransitSystemImporter.setNullableString(insertStatement, 1, id);
 				insertStatement.setString(2, name);
 				insertStatement.setString(3, url);
 				insertStatement.setString(4, timezone);
 				TransitSystemImporter.setNullableString(insertStatement, 5, lang);
 				TransitSystemImporter.setNullableString(insertStatement, 6, phone);
 				TransitSystemImporter.setNullableString(insertStatement, 7, fareUrl);
 			}
 		});
 	}
 	
 	private void loadStops(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO stops (stop_id, stop_code, stop_name, stop_desc, stop_lat, stop_lon, zone_id, stop_url, location_type, parent_station, stop_timezone, wheelchair_boarding) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String id = reader.get("stop_id");
 				final String code = StringUtils.trimToNull(reader.get("stop_code"));
 				final String name = reader.get("stop_name");
 				final String description = StringUtils.trimToNull(reader.get("stop_desc"));
 				final double latitude = Double.parseDouble(reader.get("stop_lat"));
 				final double longitude = Double.parseDouble(reader.get("stop_lon"));
 				final String zoneId = StringUtils.trimToNull(reader.get("zone_id"));
 				final String url = StringUtils.trimToNull(reader.get("stop_url"));
 				final Integer locationType = StringUtils.isNotBlank(reader.get("location_type")) ? Integer.parseInt(reader.get("location_type")) : null;
 				final String parentStationId = StringUtils.trimToNull(reader.get("parent_station"));
 				final String timezone = StringUtils.trimToNull(reader.get("stop_timezone"));
 				final Integer wheelchairBoarding = StringUtils.isNotBlank(reader.get("wheelchair_boarding")) ? Integer.parseInt(reader.get("wheelchair_boarding")) : null;
 				
 				insertStatement.setString(1, id);
 				TransitSystemImporter.setNullableString(insertStatement, 2, code);
 				insertStatement.setString(3, name);
 				TransitSystemImporter.setNullableString(insertStatement, 4, description);
 				insertStatement.setDouble(5, latitude);
 				insertStatement.setDouble(6, longitude);
 				TransitSystemImporter.setNullableString(insertStatement, 7, zoneId);
 				TransitSystemImporter.setNullableString(insertStatement, 8, url);
 				TransitSystemImporter.setNullableInteger(insertStatement, 9, locationType);
 				TransitSystemImporter.setNullableString(insertStatement, 10, parentStationId);
 				TransitSystemImporter.setNullableString(insertStatement, 11, timezone);
 				TransitSystemImporter.setNullableInteger(insertStatement, 12, wheelchairBoarding);
 			}
 		});
 	}
 	
 	private void loadRoutes(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_desc, route_type, route_url, route_color, route_text_color) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String id = reader.get("route_id");
 				final String agencyId = StringUtils.trimToNull(reader.get("agency_id"));
 				final String shortName = reader.get("route_short_name");
 				final String longName = reader.get("route_long_name");
 				final String description = StringUtils.trimToNull(reader.get("route_desc"));
 				final int routeType = Integer.parseInt(reader.get("route_type"));
 				final String url = StringUtils.trimToNull(reader.get("route_url"));
 				final String color = StringUtils.trimToNull(reader.get("route_color"));
 				final String textColor = StringUtils.trimToNull(reader.get("route_text_color"));
 				
 				insertStatement.setString(1, id);
 				TransitSystemImporter.setNullableString(insertStatement, 2, agencyId);
 				insertStatement.setString(3, shortName);
 				insertStatement.setString(4, longName);
 				TransitSystemImporter.setNullableString(insertStatement, 5, description);
 				insertStatement.setInt(6, routeType);
 				TransitSystemImporter.setNullableString(insertStatement, 7, url);
 				TransitSystemImporter.setNullableString(insertStatement, 8, color);
 				TransitSystemImporter.setNullableString(insertStatement, 9, textColor);
 			}
 		});
 	}
 	
 	private void loadCalendar(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO calendar (service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 	
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String serviceId = reader.get("service_id");
 				final int monday = Integer.parseInt(reader.get("monday"));
 				final int tuesday = Integer.parseInt(reader.get("tuesday"));
 				final int wednesday = Integer.parseInt(reader.get("wednesday"));
 				final int thursday = Integer.parseInt(reader.get("thursday"));
 				final int friday = Integer.parseInt(reader.get("friday"));
 				final int saturday = Integer.parseInt(reader.get("saturday"));
 				final int sunday = Integer.parseInt(reader.get("sunday"));
 				final java.sql.Date startDate = TransitSystemImporter.parseGtfsDate(reader.get("start_date"));
 				final java.sql.Date endDate = TransitSystemImporter.parseGtfsDate(reader.get("end_date"));
 				
 				insertStatement.setString(1, serviceId);
 				insertStatement.setInt(2, monday);
 				insertStatement.setInt(3, tuesday);
 				insertStatement.setInt(4, wednesday);
 				insertStatement.setInt(5, thursday);
 				insertStatement.setInt(6, friday);
 				insertStatement.setInt(7, saturday);
 				insertStatement.setInt(8, sunday);
 				insertStatement.setDate(9, startDate);
 				insertStatement.setDate(10, endDate);
 			}
 		});
 	}
 
 	private void loadCalendarDates(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO calendar_dates (service_id, date, exception_type) " +
 				"VALUES (?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 	
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String serviceId = reader.get("service_id");
 				final java.sql.Date date = TransitSystemImporter.parseGtfsDate(reader.get("date"));
 				final int exceptionType = Integer.parseInt(reader.get("exception_type"));
 				
 				insertStatement.setString(1, serviceId);
 				insertStatement.setDate(2, date);
 				insertStatement.setInt(3, exceptionType);
 			}
 		});
 	}
 	
 	private void loadShapes(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) " +
 				"VALUES (?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String id = reader.get("shape_id");
 				final double latitude = Double.parseDouble(reader.get("shape_pt_lat"));
 				final double longitude = Double.parseDouble(reader.get("shape_pt_lon"));
 				final int sequence = Integer.parseInt(reader.get("shape_pt_sequence"));
				final Double distanceTraveled = StringUtils.isNotBlank(reader.get("shape_dist_traveled")) ? Double.parseDouble(reader.get("shape_pt_sequence")) : null;
 				
 				insertStatement.setString(1, id);
 				insertStatement.setDouble(2, latitude);
 				insertStatement.setDouble(3, longitude);
 				insertStatement.setInt(4, sequence);
 				TransitSystemImporter.setNullableDouble(insertStatement, 5, distanceTraveled);
 			}
 		});
 	}
 
 	private void loadTrips(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO trips (route_id, service_id, trip_id, trip_headsign, trip_short_name, direction_id, block_id, shape_id, wheelchair_accessible) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String id = reader.get("route_id");
 				final String serviceId = reader.get("service_id");
 				final String tripId = reader.get("trip_id");
 				final String tripHeadsign = StringUtils.trimToNull(reader.get("trip_headsign"));
 				final String shortName = StringUtils.trimToNull(reader.get("trip_short_name"));
 				final Integer directionId = StringUtils.isNotBlank(reader.get("direction_id")) ? Integer.parseInt(reader.get("direction_id")) : null;
 				final String blockId = StringUtils.trimToNull(reader.get("block_id"));
 				final String shapeId = StringUtils.trimToNull(reader.get("shape_id"));
 				final Integer wheelchairAccessible = StringUtils.isNotBlank(reader.get("wheelchair_accessible")) ? Integer.parseInt(reader.get("wheelchair_accessible")) : null;;
 				
 				insertStatement.setString(1, id);
 				insertStatement.setString(2, serviceId);
 				insertStatement.setString(3, tripId);
 				TransitSystemImporter.setNullableString(insertStatement, 4, tripHeadsign);
 				TransitSystemImporter.setNullableString(insertStatement, 5, shortName);
 				TransitSystemImporter.setNullableInteger(insertStatement, 6, directionId);
 				TransitSystemImporter.setNullableString(insertStatement, 7, blockId);
 				TransitSystemImporter.setNullableString(insertStatement, 8, shapeId);
 				TransitSystemImporter.setNullableInteger(insertStatement, 9, wheelchairAccessible);
 			}
 		});
 	}
 	
 	private void loadStopTimes(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO stop_times (trip_id, arrival_time, departure_time, stop_id, stop_sequence, stop_headsign, pickup_type, drop_off_type, shape_dist_traveled) " +
 				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String tripId = reader.get("trip_id");
 				final String arrivalTime = reader.get("arrival_time");
 				final String departureTime = reader.get("departure_time");
 				final String stopId = reader.get("stop_id");
 				final int stopSequence = Integer.parseInt(reader.get("stop_sequence"));
 				final String stopHeadsign = StringUtils.trimToNull(reader.get("stop_headsign"));
 				final Integer pickupType = StringUtils.isNotBlank(reader.get("pickup_type")) ? Integer.parseInt(reader.get("pickup_type")) : null;
 				final Integer dropOffType = StringUtils.isNotBlank(reader.get("drop_off_type")) ? Integer.parseInt(reader.get("drop_off_type")) : null;
 				final Double distanceTraveled = StringUtils.isNotBlank(reader.get("shape_dist_traveled")) ? Double.parseDouble(reader.get("shape_dist_traveled")) : null;
 				
 				insertStatement.setString(1, tripId);
 				insertStatement.setInt(2, TransitSystemImporter.secondsSinceNoonMinusTwelveHours(arrivalTime));
 				insertStatement.setInt(3, TransitSystemImporter.secondsSinceNoonMinusTwelveHours(departureTime));
 				insertStatement.setString(4, stopId);
 				insertStatement.setInt(5, stopSequence);
 				TransitSystemImporter.setNullableString(insertStatement, 6, stopHeadsign);
 				TransitSystemImporter.setNullableInteger(insertStatement, 7, pickupType);
 				TransitSystemImporter.setNullableInteger(insertStatement, 8, dropOffType);
 				TransitSystemImporter.setNullableDouble(insertStatement, 9, distanceTraveled);
 			}
 		});
 	}
 	
 	private void loadFareAttributes(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO fare_attributes (fare_id, price, currency_type, payment_method, transfers, transfer_duration) " +
 				"VALUES (?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String fareId = reader.get("fare_id");
 				final BigDecimal price = new BigDecimal(reader.get("price"));
 				final String currencyType = reader.get("currency_type");
 				final int paymentMethod = Integer.parseInt(reader.get("payment_method"));
 				final Integer transfers = StringUtils.isNotBlank(reader.get("transfers")) ? Integer.parseInt(reader.get("transfers")) : null;
 				final Integer transferDuration = StringUtils.isNotBlank(reader.get("transfer_duration")) ? Integer.parseInt(reader.get("transfer_duration")) : null;
 				
 				insertStatement.setString(1, fareId);
 				insertStatement.setBigDecimal(2, price);
 				insertStatement.setString(3, currencyType);
 				insertStatement.setInt(4, paymentMethod);
 				TransitSystemImporter.setNullableInteger(insertStatement, 5, transfers);
 				TransitSystemImporter.setNullableInteger(insertStatement, 6, transferDuration);
 			}
 		});
 	}
 	
 	private void loadFareRules(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO fare_attributes (fare_id, route_id, origin_id, destination_id, contains_id) " +
 				"VALUES (?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String fareId = reader.get("fare_id");
 				final String routeId = StringUtils.trimToNull(reader.get("route_id"));
 				final String originId = StringUtils.trimToNull(reader.get("origin_id"));
 				final String destinationId = StringUtils.trimToNull(reader.get("destination_id"));
 				final String containsId = StringUtils.trimToNull(reader.get("contains_id"));
 				
 				insertStatement.setString(1, fareId);
 				TransitSystemImporter.setNullableString(insertStatement, 2, routeId);
 				TransitSystemImporter.setNullableString(insertStatement, 3, originId);
 				TransitSystemImporter.setNullableString(insertStatement, 4, destinationId);
 				TransitSystemImporter.setNullableString(insertStatement, 5, containsId);
 			}
 		});
 	}
 	
 	private void loadFrequencies(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO frequencies (trip_id, start_time, end_time, headway_secs, exact_times) " +
 				"VALUES (?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String fareId = reader.get("trip_id");
 				final String startTime = reader.get("start_time");
 				final String endTime = reader.get("end_time");
 				final int headwaySecs = Integer.parseInt(reader.get("headway_secs"));
 				final Boolean exactTimes = StringUtils.isNotBlank(reader.get("exact_times")) ? (Integer.parseInt(reader.get("exact_times")) == 1 ? true : false) : null;
 				
 				insertStatement.setString(1, fareId);
 				insertStatement.setInt(2, TransitSystemImporter.secondsSinceNoonMinusTwelveHours(startTime));
 				insertStatement.setInt(3, TransitSystemImporter.secondsSinceNoonMinusTwelveHours(endTime));
 				insertStatement.setInt(4, headwaySecs);
 				TransitSystemImporter.setNullableBoolean(insertStatement, 5, exactTimes);
 			}
 		});
 	}
 	
 	private void loadTransfers(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO transfers (from_stop_id, to_stop_id, transfer_type, min_transfer_time) " +
 				"VALUES (?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String fromStopId = reader.get("from_stop_id");
 				final String toStopId = reader.get("to_stop_id");
 				final int transferType = Integer.parseInt(reader.get("transfer_type"));
 				final Integer minTransferTime = StringUtils.isNotBlank(reader.get("min_transfer_time")) ? Integer.parseInt(reader.get("min_transfer_time")) : null;
 				
 				insertStatement.setString(1, fromStopId);
 				insertStatement.setString(2, toStopId);
 				insertStatement.setInt(3, transferType);
 				TransitSystemImporter.setNullableInteger(insertStatement, 4, minTransferTime);
 			}
 		});
 	}
 	
 	private void loadFeedInfo(final InputStream inputStream, final Connection connection) throws SQLException, IOException {
 		
 		final String insertQuery =
 				"INSERT INTO feed_info (feed_publisher_name, feed_publisher_url, feed_lang, feed_start_date, feed_end_date, feed_version) " +
 				"VALUES (?, ?, ?, ?, ?, ?)";
 		
 		TransitSystemImporter.loadTable(inputStream, connection, insertQuery, new CsvLoader() {
 
 			@Override
 			public void loadRow(final CsvReader reader, final PreparedStatement insertStatement) throws IOException, SQLException {
 				final String publisherName = reader.get("feed_publisher_name");
 				final String publisherUrl = reader.get("feed_publisher_url");
 				final String publisherLang = reader.get("feed_lang");
 				final java.sql.Date startDate = StringUtils.isNotBlank(reader.get("feed_start_date")) ? TransitSystemImporter.parseGtfsDate(reader.get("feed_start_date")) : null;
 				final java.sql.Date endDate = StringUtils.isNotBlank(reader.get("feed_end_date")) ? TransitSystemImporter.parseGtfsDate(reader.get("feed_end_date")) : null;
 				final String version = StringUtils.trimToNull(reader.get("feed_version"));
 				
 				insertStatement.setString(1, publisherName);
 				insertStatement.setString(2, publisherUrl);
 				insertStatement.setString(3, publisherLang);
 				TransitSystemImporter.setNullableDate(insertStatement, 4, startDate);
 				TransitSystemImporter.setNullableDate(insertStatement, 5, endDate);
 				TransitSystemImporter.setNullableString(insertStatement, 6, version);
 			}
 		});
 	}
 	
 	private static void loadTable(final InputStream inputStream, final Connection connection, final String insertQuery, final CsvLoader loader) throws SQLException, IOException {
 		
 		connection.setAutoCommit(false);
 		
 		final PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
 		final CsvReader reader = new CsvReader(inputStream, Charset.forName("ISO-8859-1"));
 		
 		try {
 			reader.readHeaders();
 			
 			while (reader.readRecord()) {
 				loader.loadRow(reader, insertStatement);
 				insertStatement.addBatch();
 			}
 		} catch (IOException e) {
 			TransitSystemImporter.closeStatementIfPossible(insertStatement);
 			throw e;
 		} catch (SQLException e) {
 			TransitSystemImporter.closeStatementIfPossible(insertStatement); 
 			throw e;
 		} finally {
 			reader.close();
 		}
 		
 		// TODO Roll back if something goes wrong
 		try {
 			insertStatement.executeBatch();
 			connection.commit();
 		} finally {
 			TransitSystemImporter.closeStatementIfPossible(insertStatement);
 		}
 	}
 	
 	private static void closeStatementIfPossible(final Statement statement) {
 		try {
 			statement.close();
 		} catch (SQLException e) {
 			// TODO Log this exception
 		}
 	}
 	
 	private static void setNullableString(final PreparedStatement statement, final int parameterIndex, final String string) throws SQLException {
 		if (string != null) {
 			statement.setString(parameterIndex, string);
 		} else {
 			statement.setNull(parameterIndex, java.sql.Types.CHAR);
 		}
 	}
 	
 	private static void setNullableInteger(final PreparedStatement statement, final int parameterIndex, final Integer integer) throws SQLException {
 		if (integer != null) {
 			statement.setInt(parameterIndex, integer);
 		} else {
 			statement.setNull(parameterIndex, java.sql.Types.INTEGER);
 		}
 	}
 	
 	private static void setNullableDouble(final PreparedStatement statement, final int parameterIndex, final Double d) throws SQLException {
 		if (d != null) {
 			statement.setDouble(parameterIndex, d);
 		} else {
 			statement.setNull(parameterIndex, java.sql.Types.DOUBLE);
 		}
 	}
 	
 	private static void setNullableBoolean(final PreparedStatement statement, final int parameterIndex, final Boolean b) throws SQLException {
 		if (b != null) {
 			statement.setBoolean(parameterIndex, b);
 		} else {
 			statement.setNull(parameterIndex, java.sql.Types.BOOLEAN);
 		}
 	}
 	
 	private static void setNullableDate(final PreparedStatement statement, final int parameterIndex, final java.sql.Date date) throws SQLException {
 		if (date != null) {
 			statement.setDate(parameterIndex, date);
 		} else {
 			statement.setNull(parameterIndex, java.sql.Types.DATE);
 		}
 	}
 	
 	private static java.sql.Date parseGtfsDate(final String dateString) {
 		try {
 			return new java.sql.Date(gtfsDateFormat.parse(dateString).getTime());
 		} catch (ParseException e) {
 			throw new IllegalArgumentException("Could not parse date string as GTFS date.", e);
 		}
 	}
 	
 	private static int secondsSinceNoonMinusTwelveHours(final String timeString) {
 		final String[] pieces = timeString.split(":");
 		
 		final int hours = Integer.parseInt(pieces[0]);
 		final int minutes = Integer.parseInt(pieces[0]);
 		final int seconds = Integer.parseInt(pieces[0]);
 		
 		return (hours * 3600) + (minutes * 60) + seconds;
 	}
 }
