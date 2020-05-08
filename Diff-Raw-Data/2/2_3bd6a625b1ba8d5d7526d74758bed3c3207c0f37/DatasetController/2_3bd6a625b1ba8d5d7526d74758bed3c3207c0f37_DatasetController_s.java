 package com.bouncingdata.plfdemo.controller;
 
 import java.io.BufferedOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.bouncingdata.plfdemo.datastore.pojo.dto.ActionResult;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.Attachment;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.DatasetDetail;
 import com.bouncingdata.plfdemo.datastore.pojo.dto.QueryResult;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Analysis;
 import com.bouncingdata.plfdemo.datastore.pojo.model.AnalysisDataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Dataset;
 import com.bouncingdata.plfdemo.datastore.pojo.model.ReferenceDocument;
 import com.bouncingdata.plfdemo.datastore.pojo.model.User;
 import com.bouncingdata.plfdemo.datastore.pojo.model.UserActionLog;
 import com.bouncingdata.plfdemo.service.ApplicationStoreService;
 import com.bouncingdata.plfdemo.service.BcDatastoreService;
 import com.bouncingdata.plfdemo.service.DatastoreService;
 import com.bouncingdata.plfdemo.util.Utils;
 import com.bouncingdata.plfdemo.util.dataparsing.DataParser;
 import com.bouncingdata.plfdemo.util.dataparsing.DataParserFactory;
 import com.bouncingdata.plfdemo.util.dataparsing.DataParserFactory.FileType;
 import com.bouncingdata.plfdemo.util.dataparsing.DatasetColumn;
 import com.bouncingdata.plfdemo.util.dataparsing.DatasetColumn.ColumnType;
 import com.mysql.jdbc.StringUtils;
 
 @Controller
 @RequestMapping("/dataset")
 public class DatasetController {
   
   private Logger logger = LoggerFactory.getLogger(DatasetController.class);
   
   @Autowired
   private DatastoreService datastoreService;
   
   @Autowired
   private BcDatastoreService userDataService;
   
   @Autowired
   private ApplicationStoreService appStoreService;
   
   private String logDir;
   
   public void setLogDir(String ld) {
     this.logDir = ld;
   }
   
   @RequestMapping(value={"/upload"}, method = RequestMethod.GET)
   public String getUploadPage(ModelMap model, Principal principal) {
     try {
       User user = (User) ((Authentication) principal).getPrincipal();
       ObjectMapper logmapper = new ObjectMapper();
       String data;
       data = logmapper.writeValueAsString(new String[] { "0" });
 
       datastoreService.logUserAction(user.getId(), UserActionLog.ActionCode.GET_UPLOAD_PAGE, data);
     } catch (Exception e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     return "upload";
   }
   
   @RequestMapping(value="/upload/schema", method = RequestMethod.GET)
   public String getUploadPage2() {
     return "redirect:/dataset/upload";
   }
   
   @RequestMapping(value="/upload/schema", method = RequestMethod.POST)
   public String getSchemaPage(@RequestParam(value="file", required=false) MultipartFile file,
       @RequestParam(value="fileUrl", required=false) String fileUrl, 
       @RequestParam(value="firstRowAsHeader", required=false) String firstRowAsHeader,
       @RequestParam(value="delimiter", required=false) String delimiter, ModelMap model, Principal principal) {
     
     User user = (User) ((Authentication) principal).getPrincipal();
     ObjectMapper mapper = new ObjectMapper();
     
     try {
       String data = mapper.writeValueAsString(new String[] { "1", fileUrl });
       datastoreService.logUserAction(user.getId(), UserActionLog.ActionCode.GET_SCHEMA_PAGE, data);
     } catch (Exception e) {
       logger.debug("Failed to log action", e);
     }
 	  
     if (file == null && (fileUrl == null || StringUtils.isEmptyOrWhitespaceOnly(fileUrl))) {
       model.addAttribute("errorMsg", "Null input file or file address.");
       return "upload";
     }
     
     String filename = file.getOriginalFilename();
     int index = filename.lastIndexOf(".");
     String type = filename.substring(index + 1);
     filename = filename.substring(0, index);
     long size = file.getSize();
     logger.debug("UPLOAD FILE: Received {} file. Size {}", filename, size);
     if (size <= 0) {
       model.addAttribute("errorMsg", "Cannot determine file size.");
       return "upload";
     }
     
     // parse the schema
     DataParser parser;
     if (type.equals("xls") || type.equals("xlsx")) {
       parser = DataParserFactory.getDataParser(FileType.EXCEL);       
     } else if (type.equals("txt")) {
       parser = DataParserFactory.getDataParser(FileType.TEXT);
     } else if (type.equals("csv")) {
       parser = DataParserFactory.getDataParser(FileType.CSV);
     } else {
       model.addAttribute("errorMsg", "Unknown file type.");
       return "upload";
     }
     
     // temporary store to where? in which format?
     final String ticket = Utils.getExecutionId();
     String tempDataFilePath = logDir + Utils.FILE_SEPARATOR + ticket + Utils.FILE_SEPARATOR + ticket + ".dat";
     File tempDataFile = new File(tempDataFilePath);
     
     try {
       if (!tempDataFile.getParentFile().isDirectory()) {
         tempDataFile.getParentFile().mkdirs();
       }
       
       List<Object[]> data = parser.parse(file.getInputStream());
       /*List<Map> dataMapList = new ArrayList<Map>();
       String[] header = data.get(0);
       int maxRow = Math.min(100, data.size() - 1);
       for (int i = 1; i <= maxRow; i++) {
         HashMap<String, Object> row = new HashMap<String, Object>();
         for (int j = 0; j < header.length; j++) {
           String col = header[j];
           Object val = data.get(i)[j];
           row.put(col, val);
         }
         dataMapList.add(row);
       }
       model.addAttribute("data", mapper.writeValueAsString(dataMapList));*/
       
       ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(tempDataFile));
       for (Object[] row : data) {
         os.writeObject(row);
       }
       os.close();
       
      model.addAttribute("data", mapper.writeValueAsString(data));
       
     } catch (Exception e) {
       logger.debug("Failed to write to temporary datafile {}", tempDataFilePath);
       model.addAttribute("errorMsg", "Failed to parse and save your data.");
       return "upload";
     }
     
     try {
       // parse schema
       List<DatasetColumn> schema = parser.parseSchema(file.getInputStream());
       model.addAttribute("schema", mapper.writeValueAsString(schema));
     } catch(Exception e) {
       logger.debug("Exception occured when parsing data schema", e);
       model.addAttribute("errorMsg", "Failed to parse schema.");
       return "upload";
     }
     
     model.addAttribute("ticket", ticket);
     return "schema";
   }
     
   @SuppressWarnings("rawtypes")
   @RequestMapping(value="/{guid}", method = RequestMethod.GET)
   public @ResponseBody List<Map> getData(@PathVariable String guid) {
     try {
       Dataset ds = datastoreService.getDatasetByGuid(guid);
       if (ds == null) {
         logger.debug("Can't find the dataset {}", guid);
         return null;
       }
       
       return userDataService.getDatasetToList(ds.getName(), 0, 100);
     } catch (Exception e) {
       logger.debug("Exception occurs when retrieving dataset " + guid, e);
     }   
     return null;
   }
   
   @SuppressWarnings("rawtypes")
   @RequestMapping(value="/{guid}?start={start}&count={count}", method = RequestMethod.GET)
   public @ResponseBody List<Map> getData(@PathVariable String guid, @PathVariable int start, @PathVariable int count) {
     try {
       Dataset ds = datastoreService.getDatasetByGuid(guid);
       if (ds == null) {
         logger.debug("Can't find the dataset {}", guid);
         return null;
       }
       
       if (count <= 0) return null;
       
       return userDataService.getDatasetToList(ds.getName(), start, count);
     } catch (Exception e) {
       logger.debug("Exception occurs when retrieving dataset " + guid, e);
     }   
     return null;
   }
   
   @RequestMapping(value="/query", method = RequestMethod.POST)
   public @ResponseBody QueryResult queryDataset(@RequestParam(value="guid", required=true) String guid, 
       @RequestParam(value="query", required=true) String query) {
     try {
       Dataset ds = datastoreService.getDatasetByGuid(guid);
       if (ds == null) {
         logger.debug("Can't find the dataset {}", guid);
         return null;
       }
       
       return new QueryResult(userDataService.query(query), 1, "OK");
     } catch (Exception e) {
       logger.debug("Exception occurs when querying dataset " + guid, e);
       return new QueryResult(null, -1, e.getMessage());
     }
   }
   
   @RequestMapping(value="/m/{guids}", method = RequestMethod.GET)
   public @ResponseBody Map<String, DatasetDetail> getDataMap(@PathVariable String guids) {
     Map<String, DatasetDetail> results = new HashMap<String, DatasetDetail>();
     String[] guidArr = guids.split(",");
     for (String guid : guidArr) {
       guid = guid.trim();
       try {
         Dataset ds = datastoreService.getDatasetByGuid(guid);
         if (ds == null) {
           logger.debug("Can't find the dataset {}", guid);
           continue;
         }
         String data = null;
         String[] columns = null;
         if (ds.getRowCount() < 500) {
           data = userDataService.getDatasetToString(ds.getName());
         } else {
           Map row = userDataService.getDatasetToList(ds.getName(), 0, 1).get(0);
           columns = new String[row.keySet().size()];
           int i = 0;
           for (Object s : row.keySet()) {
             columns[i++] = (String) s;
           }
           
         }
         DatasetDetail detail = new DatasetDetail(guid, ds.getName(), ds.getRowCount(), columns, data);
         //DatasetDetail detail = new DatasetDetail(guid, ds.getName());
         results.put(ds.getGuid(), detail);
       } catch (Exception e) {
         logger.debug("Exception occurs when retrieving dataset " + guid, e);
       } 
     }        
     return results;
   }
   
   @Deprecated
   @RequestMapping(value="/up", method = RequestMethod.POST)
   public @ResponseBody ActionResult submitDataset(@RequestParam(value = "file", required = true) MultipartFile file,
       @RequestParam(value = "type", required = true) String type, ModelMap model, Principal principal) {
     
     User user = (User) ((Authentication)principal).getPrincipal();
     
     try {
       ObjectMapper logmapper = new ObjectMapper();
       String data;
       data = logmapper.writeValueAsString(new String[] { "1", type });
       datastoreService.logUserAction(user.getId(), UserActionLog.ActionCode.SUBMIT_DATASDET, data);
     } catch (Exception e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     
     String filename = file.getOriginalFilename();    
     filename = filename.substring(0, filename.lastIndexOf("."));
     long size = file.getSize();
     logger.debug("UPLOAD FILE: Received {} file. Size {}", filename, size);
     if (size <= 0) {
       return new ActionResult(-1, "Cannot determine file size");
     }
     
     DataParser parser;
     if (type.equals("xls") || type.equals("xlsx")) {
       parser = DataParserFactory.getDataParser(FileType.EXCEL);       
     } else if (type.equals("txt")) {
       parser = DataParserFactory.getDataParser(FileType.TEXT);
     } else if (type.equals("csv")) {
       parser = DataParserFactory.getDataParser(FileType.CSV);
     } else return new ActionResult(-1, "Unknown type");
     
     // temporary store to where? in which format?
     final String ticket = Utils.getExecutionId();
     String tempDataFilePath = logDir + Utils.FILE_SEPARATOR + ticket + Utils.FILE_SEPARATOR + ticket + ".dat";
     File tempDataFile = new File(tempDataFilePath);
     
     try {
       if (!tempDataFile.getParentFile().isDirectory()) {
         tempDataFile.getParentFile().mkdirs();
       }
       
       List<Object[]> data = parser.parse(file.getInputStream());
       ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(tempDataFile));
       for (Object[] row : data) {
         os.writeObject(row);
       }
       os.close();
       
     } catch (Exception e) {
       logger.debug("Failed to write to temporary datafile {}", tempDataFilePath);
       return new ActionResult(-1, "Failed to parse and save your data");
     }
     
     try {
       // parse schema
       List<DatasetColumn> schema = parser.parseSchema(file.getInputStream());
       ActionResult result = new ActionResult(1, "Successfully parsed schema");
       result.setObject(new Object[] { ticket, schema });
       return result;
     } catch(Exception e) {
       return new ActionResult(-1, "Cannot parse schema");
     }
     
     
   }
   
   @RequestMapping(value="/persist", method = RequestMethod.POST)
   @ResponseBody
   public ActionResult persistDataset(@RequestParam(value = "ticket", required = true) String ticket,
       @RequestParam(value = "schema", required = true) String schema,
       @RequestParam(value = "name", required = true) String name, 
       @RequestParam(value = "description", required = false) String description, 
       @RequestParam(value = "isPublic", required = true) boolean isPublic, Principal principal) {
     
     User user = (User) ((Authentication)principal).getPrincipal();
     ObjectMapper mapper = new ObjectMapper();
     
     try {
       String data = mapper.writeValueAsString(new String[] { "4", ticket, schema, name, description });
       datastoreService.logUserAction(user.getId(), UserActionLog.ActionCode.PERSIST_DATASET, data);
     } catch (Exception e) {
       logger.debug("Failed to log action", e);
     }
     
     // check if the guid is valid and temp. file exists
     File tempDataFile = new File(logDir + Utils.FILE_SEPARATOR + ticket + Utils.FILE_SEPARATOR + ticket + ".dat");
     if (!tempDataFile.isFile()) {
       return new ActionResult(-1, "Can't find your submitted data. Please try again.");
     }
     
     List<String[]> data = new ArrayList<String[]>();
     int columnNumber = -1;
     
     // read the temporary file
     try {
       ObjectInputStream is = new ObjectInputStream(new FileInputStream(tempDataFile));
       while (true) {
         try {
           String[] row = (String[]) is.readObject();
           data.add(row);
           if (columnNumber < 0) columnNumber = row.length;
         } catch (EOFException eof) {
           break;
         }
       }
        
     } catch (Exception e) {
       logger.debug("Failed to read temporary datafile {}", tempDataFile.getAbsolutePath());
       return new ActionResult(-1, "Can't read your subbmitted data. Data persist failed.");
     }
     
     DatasetColumn[] columns = new DatasetColumn[columnNumber];
     
     // parse the schema string
     try {
       JsonNode schemaArray = mapper.readTree(schema);
       for (int i = 0; i < schemaArray.size(); i++) {
         JsonNode element = schemaArray.get(i);
         String colName = element.get(0).getTextValue().trim();
         String colType = element.get(1).getTextValue();
         DatasetColumn col = new DatasetColumn(colName);
         col.setTypeName(colType);
         col.setType(ColumnType.getTypeFromName(colType));
         columns[i] = col;
       }
     } catch (Exception e) {
       logger.debug("Failed to parse schema json string: {}", schema);
       logger.debug("", e);
       return new ActionResult(-1, "Failed to parse the submitted schema. Please try again");
     }
     
     String dsFName = user.getUsername() + "." + name;
     String datasetSchema = userDataService.buildSchema(dsFName, columns);
     String guid = Utils.generateGuid();
     try {
       userDataService.storeData(dsFName, columns, data.subList(1, data.size()));  
       Dataset ds = new Dataset();
       ds.setUser(user);
       ds.setActive(true);
       Date timestamp = new Date();
       ds.setCreateAt(timestamp);
       ds.setLastUpdate(timestamp);
       ds.setDescription(description);       
       ds.setName(dsFName);
       ds.setScraper(null);
       ds.setRowCount(data.size() - 1);
       ds.setGuid(guid);      
       ds.setSchema(datasetSchema);
       ds.setPublic(isPublic);
       datastoreService.createDataset(ds);
     } catch (Exception e) {
       logger.debug("Failed to store datafile {} to datastore as {}", tempDataFile.getAbsolutePath(), dsFName);
       logger.debug("Requested schema: {}", datasetSchema);
       logger.debug("", e);
       return new ActionResult(-1, "Failed to store your dataset");
     }
     
     // delete temp. file
     tempDataFile.delete();
     
     ActionResult result = new ActionResult(0, "OK");
     result.setObject(guid);
     return result;
   }
   
   
   /*@RequestMapping(value="/up", method = RequestMethod.POST)
   public @ResponseBody long uploadDataset(@RequestParam(value="file", required=true) MultipartFile file, @RequestParam(value="type", required=true) String type, ModelMap model,
       Principal principal) {
     User user = (User) ((Authentication)principal).getPrincipal();
     String filename = file.getOriginalFilename();    
     filename = filename.substring(0, filename.lastIndexOf("."));
     long size = file.getSize();
     if (size <= 0) return -1;
     logger.debug("UPLOAD FILE: Received {} file. Size {}", filename, size);
     try {
       DataParser parser;
       if (type.equals("xls") || type.equals("xlsx")) {
         parser = DataParserFactory.getDataParser(FileType.EXCEL);       
       } else if (type.equals("txt")) {
         parser = DataParserFactory.getDataParser(FileType.TEXT);
       } else if (type.equals("csv")) {
         parser = DataParserFactory.getDataParser(FileType.CSV);
       } else return -1;
       
       List<String[]> data = parser.parse(file.getInputStream());      
       String[] headers = data.get(0);
       String dsFName = user.getUsername() + "." + filename + "_uploaded";
       //String identifier = user.getId() + "__" + dsName;
       userDataService.storeData(dsFName, headers, data.subList(1, data.size()));
       
       Dataset ds = new Dataset();
       ds.setUser(user);
       ds.setActive(true);
       ds.setCreateAt(new Date());
       ds.setLastUpdate(new Date());
       ds.setDescription("Uploaded from " + file.getOriginalFilename());       
       ds.setName(dsFName);
       ds.setScraper(null);
       ds.setRowCount(data.size() - 1);
       ds.setGuid(Utils.generateGuid());
       
       
       StringBuilder schema = new StringBuilder("CREATE TABLE `");
       schema.append(dsFName).append("` (");
       for (String h : headers) {
         schema.append("`").append(h).append("` text,");
       }
       String schemaStr = schema.substring(0, schema.length() - 1) + ")";
       ds.setSchema(schemaStr);
       datastoreService.createDataset(ds);
     } catch (Exception e) {
       logger.debug("Exception when trying to import data", e);
       return -1;
     }
     return size;
   }*/
   
   @SuppressWarnings("rawtypes")
   @RequestMapping(value="/view/{guid}", method = RequestMethod.GET)
   public String viewDataPage(@PathVariable String guid, ModelMap model, Principal principal) {
     User user = (User) ((Authentication) principal).getPrincipal();
     ObjectMapper mapper = new ObjectMapper();
     try {
       Dataset ds = datastoreService.getDatasetByGuid(guid);
       if (ds == null) {
         logger.debug("Can't find the dataset {}", guid);
         model.addAttribute("errorMsg", "Dataset not found!");
         return "error";
       }
       
       try {
         String data = mapper.writeValueAsString(new String[] { "1", guid });
         datastoreService.logUserAction(user.getId(), UserActionLog.ActionCode.VIEW_DATAPAGE, data);
       } catch (Exception e) {
         logger.debug("Failed to log action", e);
       }
       
       model.addAttribute("dataset", ds);
       
       if (ds.getRowCount() < 1000) {
         //List<Map> data = userDataService.getDatasetToList(ds.getName());
         List<Object[]> data = new ArrayList<Object[]>();
         data.add(userDataService.getColumnNames(ds.getName()));
         data.addAll(userDataService.getDatasetToListOfArray(ds.getName()));
         model.addAttribute("data", mapper.writeValueAsString(data));
       } else {
         /*Map row = userDataService.getDatasetToList(ds.getName(), 0, 1).get(0);
         String[] columns = new String[row.keySet().size()];
         int i = 0;
         for (Object s : row.keySet()) {
           columns[i++] = (String) s;
         }*/
         String[] columns = userDataService.getColumnNames(ds.getName());
         model.addAttribute("columns", mapper.writeValueAsString(columns));
         model.addAttribute("data", null);
         model.addAttribute("guid", guid);
       }
       
       List<AnalysisDataset> relations = datastoreService.getRelatedAnalysis(ds.getId());
       if (relations != null) {
         List<Analysis> relatedAnls = new ArrayList<Analysis>();
         for (AnalysisDataset ad : relations) {
           if (ad.isActive()) {
             Analysis anls = ad.getAnalysis();
             relatedAnls.add(anls);
           }
         }
         model.addAttribute("relatedAnls", relatedAnls);
       }
       
     } catch (Exception e) {
       logger.debug("", e);
       model.addAttribute("errorMsg", e.getMessage());
       return "error";
     }
     return "datapage";
   }
   
   @RequestMapping(value="/ajax/{guid}", method = RequestMethod.GET)
   public @ResponseBody Map<String, Object> loadDatatable(@PathVariable String guid, WebRequest request) {
     try {
       Dataset ds = datastoreService.getDatasetByGuid(guid);
       if (ds == null) {
         logger.debug("Can't find the dataset {}", guid);
         return null;
       }
     
       Map<String, String[]> params = request.getParameterMap();
       int displayStart = Integer.valueOf(params.get("iDisplayStart")[0]);
       int displayLength = Integer.valueOf(params.get("iDisplayLength")[0]);
       int sEcho = Integer.valueOf(params.get("sEcho")[0]);
       
       Map<String, Object> result = new HashMap<String, Object>();
       result.put("sEcho", sEcho);
       
       List<Map> data = userDataService.getDatasetToList(ds.getName(), displayStart, displayLength);
       //int totalDisplayRecords = data.size();
       int totalRecords = ds.getRowCount();
       result.put("iTotalRecords", totalRecords);
       result.put("iTotalDisplayRecords", totalRecords);
       result.put("aaData", data);
       /*StringBuilder sColumns = new StringBuilder();
       for (Object s : data.get(0)) {
         String col = (String) s;
         sColumns.append(col + ",");
       }
       sColumns.substring(0,  sColumns.length() - 1);
       result.put("sColumns", sColumns.toString());*/
       return result;
     } catch (Exception e) {
       logger.debug("", e);
       return null;
     }
     
   }
   
   /**
    * Streams the dataset as CSV file format
    * @param guid dataset guid
    * @param req the <code>HttpServletRequest</code> object
    * @param res the <code>HttpServletResponse</code> object
    * @throws IOException
    */
   @RequestMapping(value="/dl/{type}/{guid}", method = RequestMethod.GET)
   public @ResponseBody void download(@PathVariable String type, @PathVariable String guid, HttpServletRequest req, HttpServletResponse res) throws IOException {
     
     if (!("csv".equalsIgnoreCase(type) || "json".equalsIgnoreCase(type))) {
       res.sendError(400, "Unknown datatype.");
       return;
     }
     try {
       Dataset ds = datastoreService.getDatasetByGuid(guid);
       if (ds == null) {
         logger.debug("Can't find the dataset {}", guid);
         res.sendError(400, "Dataset not found.");
         return;
       }
       
       if ("csv".equalsIgnoreCase(type)) {
         res.setContentType("text/csv;charset=utf-8"); 
         res.setHeader("Content-Disposition","attachment; filename=\"" + ds.getName() + ".csv\"");
         userDataService.getCsvStream(ds.getName(), res.getOutputStream());
       } else if ("json".equalsIgnoreCase(type)) {
         res.setContentType("text/x-json;charset=utf-8"); 
         res.setHeader("Content-Disposition","attachment; filename=\"" + ds.getName() + ".json\"");
         BufferedOutputStream buffer = new BufferedOutputStream(res.getOutputStream(), 8*1024);
         byte[] data = userDataService.getDatasetToString(ds.getName()).getBytes("UTF-8");
         res.setContentLength(data.length);
         buffer.write(data);
         buffer.flush();
       }
       return;
     } catch (Exception e) {
       res.sendError(500, "Sorry, we can't fulfil your download request due to internal error.");
     }
   }
   
   @RequestMapping(value="/att/{type}/{appGuid}/{attName}", method = RequestMethod.GET)
   public @ResponseBody void downloadAttachment(@PathVariable String type, @PathVariable String appGuid, @PathVariable String attName, HttpServletRequest req, HttpServletResponse res) throws IOException {
     if (!("csv".equalsIgnoreCase(type) || "json".equalsIgnoreCase(type))) {
       res.sendError(400, "Unknown datatype.");
       return;
     }
     try {
       Analysis anls = datastoreService.getAnalysisByGuid(appGuid);
       if (anls == null) {
         logger.debug("Can't find analysis {}", appGuid);
         res.sendError(400, "Analysis not found.");
         return;
       }
       
       Attachment attachment = appStoreService.getAttachment(appGuid, attName);
       if (attachment == null) {
         res.sendError(400, "Attachment not found or cannot read.");
         return;
       }
       
       if ("csv".equalsIgnoreCase(type)) {
         res.setContentType("text/csv;charset=utf-8"); 
         res.setHeader("Content-Disposition","attachment; filename=\"" + attName + ".csv\"");
         Utils.jsonToCsv(attachment.getData(), res.getOutputStream());
       } else if ("json".equalsIgnoreCase(type)) {
         res.setContentType("text/x-json;charset=utf-8"); 
         res.setHeader("Content-Disposition","attachment; filename=\"" + attName + ".json\"");
         BufferedOutputStream buffer = new BufferedOutputStream(res.getOutputStream());
         byte[] data = attachment.getData().getBytes("UTF-8");
         res.setContentLength(data.length);
         buffer.write(data);
         buffer.flush();
       }
       
       return;
  
     } catch (Exception e) {
       res.sendError(500, "Sorry, we can't fulfil your download request due to internal error.");
     }
   }
   
   /**
    * Upload reference document, attach to dataset
    * @param guid
    * @param refFile
    * @param refUrl
    * @return
    * @throws Exception
    */
   @RequestMapping(value="/upload/ref/{guid}", method=RequestMethod.POST)
   public @ResponseBody ActionResult uploadReferenceDoc(@PathVariable String guid, @RequestParam(value="file-ref", required=false) MultipartFile refFile, 
       @RequestParam(value="web-ref", required=false) String refUrl) throws Exception {
     
     if (refFile == null) {
       return new ActionResult(0, "No reference file upload");
     }
     
     Dataset dataset = datastoreService.getDatasetByGuid(guid);
     if (dataset == null) {
       return new ActionResult(-1, "Dataset not found");
     }
     
     String filename = refFile.getOriginalFilename();
     int index = filename.lastIndexOf(".");
     String type = filename.substring(index + 1);
     filename = filename.substring(0, index);
     long size = refFile.getSize();
     logger.debug("UPLOAD FILE: Received reference file {}. Size {}", filename, size);
     if (size <= 0) {
       return new ActionResult(-1, "Unknown file size or empty file.");
     }
     
     if (!"pdf".equalsIgnoreCase(type)) {
       return new ActionResult(-1, "Not PDF file");
     }
     
     String refGuid = Utils.generateGuid();
     
     try {
       appStoreService.storeReferenceDocument(guid, refGuid + ".pdf", refFile);
     } catch (IOException e) {
       logger.debug("Cannot store reference document file", e);
       return new ActionResult(-1, "Failed to store file");
     }
     
     // save to datastore
     ReferenceDocument ref = new ReferenceDocument(filename, "pdf", refGuid, null);
     datastoreService.addDatasetRefDocument(dataset.getId(), ref);
     
     return new ActionResult(0, "OK");
   }
   
 }
