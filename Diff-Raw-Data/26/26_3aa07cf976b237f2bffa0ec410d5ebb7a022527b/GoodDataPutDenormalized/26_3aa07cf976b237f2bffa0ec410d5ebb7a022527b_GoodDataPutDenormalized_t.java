 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.snaplogic.snap.gooddata;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import org.apache.log4j.MDC;
 import org.snaplogic.cc.Capabilities;
 import org.snaplogic.cc.Capability;
 import org.snaplogic.cc.InputView;
 import org.snaplogic.cc.OutputView;
 import org.snaplogic.cc.prop.SimpleProp;
 import org.snaplogic.cc.prop.SimpleProp.SimplePropType;
 import org.snaplogic.common.ComponentResourceErr;
 import org.snaplogic.common.Field;
 import org.snaplogic.common.Record;
 import org.snaplogic.common.Field.SnapFieldType;
 import org.snaplogic.common.exceptions.SnapComponentException;
 import org.snaplogic.log.Log;
 import org.snaplogic.snapi.PropertyConstraint;
 import org.snaplogic.snapi.ResDef;
 import org.snaplogic.snapi.PropertyConstraint.Type;
 import org.snaplogic.util.ConvertUtils;
 
 import com.gooddata.connector.Connector;
 import com.gooddata.connector.CsvConnector;
 import com.gooddata.connector.backend.DerbyConnectorBackend;
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.exception.GdcProjectAccessException;
 import com.gooddata.exception.GdcRestApiException;
 import com.gooddata.exception.GdcUploadErrorException;
 import com.gooddata.exception.HttpMethodException;
 import com.gooddata.integration.ftp.GdcFTPApiWrapper;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLI;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.integration.model.Project;
 import com.gooddata.integration.rest.GdcRESTApiWrapper;
 import com.gooddata.integration.rest.configuration.NamePasswordConfiguration;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.processor.CliParams;
 import com.gooddata.processor.Command;
 import com.gooddata.processor.ProcessingContext;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.StringUtil;
 
 public class GoodDataPutDenormalized extends AbstractGoodDataComponent {
 
 	private static final String ALL_SNAPSHOTS = "All Snapshots";
 	private static final String LAST_SNAPSHOT = "Last Snapshot";
 
 	@Override
 	public String getDescription() {
 		return getLabel() + ". Uploads data into GoodData.";
 	}
 
 	@Override
 	public Capabilities getCapabilities() {
 		return new Capabilities() {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 3848994857590856035L;
 
 			{
 				put(Capability.INPUT_VIEW_LOWER_LIMIT, 1);
 				put(Capability.INPUT_VIEW_UPPER_LIMIT, 100);
 				put(Capability.OUTPUT_VIEW_LOWER_LIMIT, 0);
 				put(Capability.OUTPUT_VIEW_UPPER_LIMIT, 0);
 			}
 		};
 	}
 
 	@Override
 	public String getLabel() {
 		return "GoodData Upload (Java)";
 	}
 
 	private static final String DLI_MANIFEST_FILENAME = "upload_info.json";
 	private static final String DLI_ARCHIVE_SUFFIX = ".zip";
 
 	public static final String PROP_PROJECT_NAME = "project_name";
 	public static final String PROP_PROJECT_ID = "project_id";
 
 	public static final String PROP_DLI = "dli";
 
 	public static final String PROP_INCREMENTAL = "overwrite";
 	private static final String PROP_TRANSFER_SNAPSHOTS = "transfer_snapshots";
 	private static final String PROP_WAIT_FINISH = "wait_finish";
 	public static final String PROP_SOURCE_SCHEMA = "put_comp_source_schema";
 
 	@Override
 	public String getAPIVersion() {
 		return "1.0";
 	}
 
 	@Override
 	public String getComponentVersion() {
 		return "1.0";
 	}
 
 	private PropertyConstraint projNameLovConstraint = new PropertyConstraint(Type.UNMODIFIABLE, true);
 	private PropertyConstraint projIdLovConstraint = new PropertyConstraint(Type.UNMODIFIABLE, true);
 
 	private SimpleProp projNameProp = new SimpleProp("Project Name", SimplePropType.SnapString, "Project name",
 			projNameLovConstraint, true);
 
 	private SimpleProp projIdProp = new SimpleProp("Project Id", SimplePropType.SnapString, "Project id",
 			projIdLovConstraint, true);
 
 	private PropertyConstraint dlisLovConstraint = new PropertyConstraint(Type.UNMODIFIABLE, true);
 
 	private SimpleProp dliProp = new SimpleProp("DLI", SimplePropType.SnapString, "Data Loading Interface",
 			dlisLovConstraint, true);
 
 	@Override
 	public void createResourceTemplate() {
 		super.createResourceTemplate();
 		setPropertyDef(PROP_PROJECT_NAME, projNameProp);
 		setPropertyDef(PROP_PROJECT_ID, projIdProp);
 		setPropertyDef(PROP_DLI, dliProp);
 
 		setPropertyDef(PROP_INCREMENTAL, new SimpleProp("Incremental", SimplePropType.SnapBoolean,
 				"Append if true, replace if false", true));
		setPropertyValue(PROP_INCREMENTAL, false);
 
 		setPropertyDef(PROP_WAIT_FINISH, new SimpleProp("Wait  for Finish", SimplePropType.SnapBoolean,
 				"waits for the server-side processing", true));
 		setPropertyValue(PROP_WAIT_FINISH, true);
 
 		PropertyConstraint transferSnapshotConstrain = new PropertyConstraint(Type.LOV, new String[] { LAST_SNAPSHOT,
 				ALL_SNAPSHOTS });
 		SimpleProp transferSnProp = new SimpleProp("Transfer Snapshots", SimplePropType.SnapString,
 				"Snapshots to transfer to GoodData", transferSnapshotConstrain, true);
 		setPropertyDef(PROP_TRANSFER_SNAPSHOTS, transferSnProp);
		setPropertyValue(PROP_TRANSFER_SNAPSHOTS, LAST_SNAPSHOT);
 
		setPropertyDef(PROP_SOURCE_SCHEMA, new SimpleProp("XML Schema", SimplePropType.SnapString, "",
 				new PropertyConstraint(Type.HIDDEN, true), true));
 		setPropertyValue(PROP_SOURCE_SCHEMA, "");
 
 	}
 
 	@Override
 	public void validate(ComponentResourceErr err) {
 		GdcRESTApiWrapper restApi = login(err);
 		if (restApi == null) {
 			return;
 		}
 		String curProjName = (String) getPropertyValue(PROP_PROJECT_NAME);
 		try {
 			if (curProjName != null && restApi.getProjectByName(curProjName) == null) {
 				err
 						.getPropertyErr(PROP_PROJECT_NAME)
 						.setMessage(
 								"Project '"
 										+ curProjName
 										+ "' does not exist for this account. Use auto-fill to populate this property with a list of eligible projects.");
 			}
 		} catch (HttpMethodException hme) {
 			elog(hme);
 			err.setMessage("Error occurred validating project name: " + hme.getMessage());
 		}
 		// TODO check that view matches the metadata
 		catch (GdcProjectAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void suggestResourceValues(ComponentResourceErr err) {
 		info("Auto-Fill for GoodDataSnap started, but nothing will be done, there's nothing to auto-guess");
 		super.suggestResourceValues(err);
 	}
 
 	// TODO
 	// this pattern should also be used in other places where
 	// writes to intermediate file or db are done
 	// even better to keep them in a StringBuffer-like object, not a list
 	// (so writes are in one swoop) but StringBuffer doesn't seem to have
 	// a clear() method, or maybe it's just late in the day
 
 	protected final int getRecordBufSize() {
 		if (Log.isLevelDebug()) {
 			return 16;
 		} else {
 			return 1024 * 10;
 		}
 	}
 
 	protected Map<String, File> stageInputsAsCsv(Map<String, InputView> inputs, boolean createHeader,
 			boolean useFieldDescForHeader, File tmpDir) {
 		List<InputView> activeInputViews = new ArrayList<InputView>();
 		activeInputViews.addAll(inputs.values());
 		if (activeInputViews.size() == 0) {
 			throw new SnapComponentException("Not enough input views available");
 		}
 		Map<String, File> viewToCsvFile = new HashMap<String, File>();
 		Map<String, BufferedWriter> viewToCsvWriter = new HashMap<String, BufferedWriter>();
 
 		// TODO
 		Map<String, List<String>> bufferedRecords = new HashMap<String, List<String>>();
 		try {
 			while (activeInputViews.size() > 0) {
 				Collection<InputView> viewsWithData = selectInputView(activeInputViews);
 				Collection<InputView> finishedViewList = new ArrayList<InputView>();
 				// Get the records from all the views that currently have data
 				for (InputView view : viewsWithData) {
 					String viewName = view.getName();
 					File csvFile = viewToCsvFile.get(viewName);
 					// csvFile.deleteOnExit();
 					BufferedWriter csvWriter = viewToCsvWriter.get(viewName);
 					if (csvFile == null) {
 
 						csvFile = new File(tmpDir, viewName + ".csv");
 						info("Creating stage CSV file: " + csvFile.getAbsolutePath());
 						csvWriter = FileUtil.createBufferedUtf8Writer(csvFile);
 						viewToCsvFile.put(viewName, csvFile);
 						viewToCsvWriter.put(viewName, csvWriter);
 					}
 					Record record = view.readRecord();
 					if (record == null) {
 						// We get null as a record, if the view is closing on
 						// the other side. We are keeping track of those that
 						// have
 						// closed in this round, and create a reverse sorted
 						// list of their indices in the active_input_view list.
 						// We can't just simply erase them right now out of the
 						// list since that would screw up our iteration over the
 						// list.
 						finishedViewList.add(view);
 						continue;
 					}
 					List<String> curBuffer = bufferedRecords.get(viewName);
 					if (curBuffer == null) {
 						curBuffer = new ArrayList<String>(getRecordBufSize());
 						bufferedRecords.put(viewName, curBuffer);
 						if (createHeader) {
 							// No buffer yet created, so write a header
 							String header = "";
 							boolean first = true;
 							for (Field field : view.getFields()) {
 								if (first) {
 									first = false;
 								} else {
 									header += ",";
 								}
 								if (useFieldDescForHeader) {
 									header += "\"" + field.getDescription() + "\"";
 								} else {
 									header += "\"" + field.getName() + "\"";
 								}
 
 							}
 							curBuffer.add(header);
 						}
 
 					}
 					String curLine = "";
 					boolean first = true;
 					for (Field field : view.getFields()) {
 						Object val = record.get(field.getName());
 						String stringVal = null;
 						if (val == null) {
 							stringVal = "";
 						} else if (val instanceof String) {
 							stringVal = val.toString();
 							stringVal = stringVal.replace('"', '\"');
 						} else if (val instanceof BigDecimal) {
 							stringVal = val.toString();
 						} else if (val instanceof Timestamp) {
 							stringVal = ISO_DATE_FORMAT.format((Timestamp) val);
 						}
 						if (first) {
 							first = false;
 						} else {
 							curLine += ",";
 						}
 						curLine += "\"" + stringVal + "\"";
 					}
 					curBuffer.add(curLine);
 
 					if (curBuffer.size() > getRecordBufSize()) {
 						debug("Writing " + curBuffer.size() + " lines to " + csvFile);
 						for (String line : curBuffer) {
 							csvWriter.write(line);
 							csvWriter.write(System.getProperty("line.separator"));
 						}
 						csvWriter.flush();
 						curBuffer.clear();
 					}
 
 				}
 				// Any views that have finished in the last round?
 				for (InputView view : finishedViewList) {
 					// Now we can delete them (starting from the back) from the
 					// list of active views.
 					activeInputViews.remove(view);
 				}
 			}
 
 			// Write out the buffers
 			for (String viewName : bufferedRecords.keySet()) {
 				List<String> curBuffer = bufferedRecords.get(viewName);
 				File csvFile = viewToCsvFile.get(viewName);
 				BufferedWriter csvWriter = viewToCsvWriter.get(viewName);
 				if (curBuffer.size() > 0) {
 					debug("Writing " + curBuffer.size() + " lines to " + csvFile);
 					for (String line : curBuffer) {
 						csvWriter.write(line);
 						csvWriter.write(System.getProperty("line.separator"));
 					}
 					csvWriter.flush();
 					csvWriter.close();
 					curBuffer.clear();
 				}
 
 			}
 			return viewToCsvFile;
 		} catch (IOException ioe) {
 			// TODO be more graceful here
 			elog(ioe);
 			throw new SnapComponentException(ioe);
 		}
 	}
 
 	public static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat(ConvertUtils.DATE_FORMAT_1);
 
 	@Override
 	public void execute(Map<String, InputView> inputViews, Map<String, OutputView> outputViews) {
 
 		info("Staged CSVs will be uploaded to GoodData. Logging in...");
 		//First we need connection to GoodData
 		ResDef gdConnection = getConnectionReference(null);
 		NamePasswordConfiguration httpConfiguration = GoodDataConnection.getHttpConfiguration(gdConnection);
 		NamePasswordConfiguration ftpConfiguration = GoodDataConnection.getFtpConfiguration(gdConnection);
 		
 		// And extract its parameters
 		CliParams cliParams = new CliParams();
 		cliParams.setHttpConfig(httpConfiguration);
 		cliParams.setFtpConfig(ftpConfiguration);
 
 		// All the properties set by the wizard and the user...
 		String projectName = (String) getPropertyValue(PROP_PROJECT_NAME);
 		String projectId = getStringPropertyValue(PROP_PROJECT_ID);
 		String dliName = (String) getPropertyValue(PROP_DLI);
 		String transferSnapshots = getStringPropertyValue(PROP_TRANSFER_SNAPSHOTS);
 		Boolean incremental = (Boolean) getPropertyValue(PROP_INCREMENTAL);
 		boolean waitForFinish = getBooleanPropertyValue(PROP_WAIT_FINISH);
 		String sourceSchemaString = getStringPropertyValue(PROP_SOURCE_SCHEMA);
 		
 		// Properties for GdcDI Commands
 		Properties props = new Properties();
 		props.setProperty("incremental", incremental.toString());
 		props.setProperty("waitForFinish", new Boolean(waitForFinish).toString());
 
 		// SourceSchema was serialized into string by the wizard, let's get it
 		SourceSchema sourceSchema;
 		try {
 			sourceSchema = SourceSchema.createSchema(new ByteArrayInputStream(sourceSchemaString.getBytes()));
 		} catch (IOException e) {
 			elog(e);
 			throw new SnapComponentException(e);
 		}
 
 		if (projectId == null || dliName == null)
 			throw new SnapComponentException("Project or DLI is not specified!");
 
 		info("Integration starting for project ID " + projectId);
 
 		Map<String, File> stagedCSVs = null;
 		String archivePath = null;
 
 		try {
 			// Transform data flowing into our input view into CSV file
 			File tmpDir = FileUtil.createTempDir();
			stagedCSVs = stageInputsAsCsv(inputViews, false, false, tmpDir);
 
 			if (stagedCSVs.size() == 1) {
 				// One file - exactly what's expected
 				// Setup backend
 				DerbyConnectorBackend derbyConnectorBackend = DerbyConnectorBackend.create();
 				derbyConnectorBackend.setProjectId(projectId);
 				derbyConnectorBackend.setPdm(PdmSchema.createSchema(sourceSchema));
 
 				// setup CSV connector
 				CsvConnector csvConnector = CsvConnector.createConnector(derbyConnectorBackend);
 				csvConnector.setSchema(sourceSchema);
 				csvConnector.initialize();
 				csvConnector.setHasHeader(false);
 				csvConnector.setDataFile(stagedCSVs.entrySet().iterator().next().getValue());
 
 				// Setup processing context
 				ProcessingContext context = new ProcessingContext();
 				context.setConnector(csvConnector);
 				context.setConnectorBackend(derbyConnectorBackend);
 				context.setProjectId(projectId);
 								
 				// Finally, do the job
 				if (transferSnapshots.equals(LAST_SNAPSHOT)) {
 					Command cmd = new Command("TransferLastSnapshot");
 					cmd.setParameters(props);
 					csvConnector.processCommand(cmd, cliParams, context);
 				} else if (transferSnapshots.equals(ALL_SNAPSHOTS)) {
 					Command cmd = new Command("TransferAllSnapshots");
 					cmd.setParameters(props);
 					csvConnector.processCommand(cmd, cliParams, context);
 				}
 				
 			} else {
 				error("Unexpected number of staged files: " + stagedCSVs.size());
 				throw new SnapComponentException("Unexpected number of staged files: " + stagedCSVs.size());
 			}
 			
 			info("Data for project " + projectId + " updated");
 
 		} catch (HttpMethodException e) {
 			elog(e);
 			throw new SnapComponentException(e);
 		} catch (GdcProjectAccessException e) {
 			elog(e);
 			throw new SnapComponentException(e);
 		} catch (IOException e) {
 			elog(e);
 			throw new SnapComponentException(e);
 		} catch (GdcUploadErrorException e) {
 			elog(e);
 			throw new SnapComponentException(e);
 		} catch (GdcRestApiException e) {
 			elog(e);
 			throw new SnapComponentException(e);
 		} finally {
 			// cleanup
 			info("Cleaning-up staging CSV files and uploaded archive...");
 			if (stagedCSVs != null) {
 				for (Entry<String, File> file : stagedCSVs.entrySet()) {
 					file.getValue().delete();
 				}
 			}
 
 			if (archivePath != null) {
 				new File(archivePath).delete();
 			}
 			info("Clean-up done");
 		}
 	}
 }
