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
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.io.IOUtils;
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
 import com.bouncingdata.plfdemo.datastore.pojo.model.DatasetVote;
 import com.bouncingdata.plfdemo.datastore.pojo.model.ReferenceDocument;
 import com.bouncingdata.plfdemo.datastore.pojo.model.Tag;
 import com.bouncingdata.plfdemo.datastore.pojo.model.User;
 import com.bouncingdata.plfdemo.datastore.pojo.model.UserActionLog;
 import com.bouncingdata.plfdemo.datastore.pojo.model.VariablesUploadDataset;
 import com.bouncingdata.plfdemo.service.ApplicationStoreService;
 import com.bouncingdata.plfdemo.service.BcDatastoreService;
 import com.bouncingdata.plfdemo.service.DatastoreService;
 import com.bouncingdata.plfdemo.util.PageType;
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
 
   private Logger                  logger = LoggerFactory.getLogger(DatasetController.class);
 
   @Autowired
   private DatastoreService        datastoreService;
 
   @Autowired
   private BcDatastoreService      userDataService;
 
   @Autowired
   private ApplicationStoreService appStoreService;
 
   private String                  logDir;
 
   public void setLogDir(String ld) {
     this.logDir = ld;
   }
 
   @RequestMapping(value={"/delds"}, method=RequestMethod.POST)
   public @ResponseBody ActionResult deletePageViewStream(@RequestParam(value="iguid", required=true) String iguid,
 														  @RequestParam(value="iname", required=true) String iname,
 														   WebRequest request, 
 														   ModelMap model, 
 														   Principal principal,
 														   HttpSession session) {
 	  
 	  User user = (User) ((Authentication)principal).getPrincipal();
       try{
 		  
 	      ObjectMapper logmapper = new ObjectMapper();
 	      String data = logmapper.writeValueAsString(new String[] {"0"});				   	 
 	      datastoreService.logUserAction(user.getId(),UserActionLog.ActionCode.GET_MORE_ACTIVITY,data);
       
 	      Dataset ds = datastoreService.getDatasetByGuid(iguid);
 		  
 		  if(ds==null || !ds.getName().equals(iname))
 			  return new ActionResult(-1, "Type of the item isn't found !");
 		  
 		  // delete dataset  
 		  boolean isRemoveDs = datastoreService.removeDataset(ds.getId());
 		  
 		  // delete table dataset in bcdatastore db
 		  boolean isDropDs = false;
 		  
 		  if (isRemoveDs)
 			  isDropDs = userDataService.dropDataset(ds.getName());
 		  
 		  if(isRemoveDs && isDropDs)
 			  return new ActionResult(0, "Delete Dataset successfully !");
 		  
       }catch(Exception e) {
           logger.debug("Failed to log action", e);
       }
 	  
 	  return new ActionResult(-1, "Can't delete Dataset !");
   }
   
   @RequestMapping(value={"/dels"}, method=RequestMethod.POST)
   public @ResponseBody ActionResult deleteDatasetStream(@RequestParam(value="iguid", required=true) String iguid,
 		  												  @RequestParam(value="itype", required=true) String itype,
 		  												  @RequestParam(value="iname", required=true) String iname,
 														   WebRequest request, 
 														   ModelMap model, 
 														   Principal principal,
 														   HttpSession session) {
 	  
 	  User user = (User) ((Authentication)principal).getPrincipal();
       try{
 		  
 	      ObjectMapper logmapper = new ObjectMapper();
 	      String data = logmapper.writeValueAsString(new String[] {"0"});				   	 
 	      datastoreService.logUserAction(user.getId(),UserActionLog.ActionCode.GET_MORE_ACTIVITY,data);
 	      
 		  if(itype.toLowerCase().equals("dataset")){
 			  Dataset ds = datastoreService.getDatasetByGuid(iguid);
 			  
 			  if(ds==null || !ds.getName().equals(iname))
 				  return new ActionResult(-1, "Type of the item isn't found !");
 			  
 			  // delete dataset  
 			  boolean isRemoveDs =  datastoreService.removeDataset(ds.getId());
 			  
 			  // delete table dataset in bcdatastore db
 			  boolean isDropDs = false;
 			  
 			  if (isRemoveDs)
 				  isDropDs = userDataService.dropDataset(ds.getName());
 			  
 			  if(isRemoveDs && isDropDs)
 				  return new ActionResult(0, "Delete Dataset successfully !");  
 			  
 		  }else
 			  return new ActionResult(-1, "Type of the item isn't found !");
 		  
       }catch(Exception e) {
           logger.debug("Failed to log action", e);
       }
 	  
 	  return new ActionResult(-1, "User isn't found !");
   }
   
   @RequestMapping(value = { "/upload" }, method = RequestMethod.GET)
   public String getUploadPage(ModelMap model, Principal principal, HttpSession session) {
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
     
     // vinhpq : remove temp object upload 
     if(session.getAttribute("varUp") != null)  
     	session.removeAttribute("varUp");
     
     return "upload";
   }
   
   @RequestMapping(value = { "/bupload" }, method = RequestMethod.GET)
   public String backUploadPage(ModelMap model, Principal principal, HttpSession session) {
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
   
   @RequestMapping(value = "/upload/schema", method = RequestMethod.GET)
   public String getUploadPage2() {
     return "redirect:/dataset/upload";
   }
   
   @RequestMapping(value = "/fschema", method = RequestMethod.GET)
   public String forwardSchemaPage(HttpSession session) {
 	  
 	if(session.getAttribute("varUp")==null)
 		return "redirect:/dataset/bupload";
 	
     return "schema";
   }
 
   @RequestMapping(value = "/upload/schema", method = RequestMethod.POST)
   public String getSchemaPage(@RequestParam(value = "file", required = false) MultipartFile file,
 						       @RequestParam(value = "fileUrl", required = false) String fileUrl,
 						       @RequestParam(value = "firstRowAsHeader", required = false) String firstRowAsHeader,
 						       @RequestParam(value = "delimiter", required = false) String delimiter, 
 						       ModelMap model, 
 						       Principal principal, 
 						       HttpSession session) {
 
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
 
     String dataSchema = "";
     List<DatasetColumn> schema;
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
 
       ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(tempDataFile));
       
       //vinhpq : remove empty rows
       int lengthRow = (data.size() > 0 ? data.get(0).length : 0),
     	   empty_cell = 0;
       
       for (Object[] row : data) {
     	empty_cell = 0;
     	for (int i = 0; i < lengthRow; i++) {
 			if(row[i].equals("")){
 				empty_cell++;
 			}
 		}
     	if(empty_cell < lengthRow){
     		os.writeObject(row);
     	}
       }
       os.close();
       
       dataSchema = mapper.writeValueAsString(data.subList(0, Math.min(100, data.size())));
       model.addAttribute("data", dataSchema);
 
     } catch (Exception e) {
       logger.debug("Failed to write to temporary datafile {}", tempDataFilePath);
       model.addAttribute("errorMsg", "Failed to parse and save your data.");
       return "upload";
     }
 
     try {
       // parse schema
       schema = parser.parseSchema(file.getInputStream());
       model.addAttribute("schema", schema);
       
     } catch (Exception e) {
       logger.debug("Exception occured when parsing data schema", e);
       model.addAttribute("errorMsg", "Failed to parse schema.");
       return "upload";
     }
 
     model.addAttribute("ticket", ticket);
     
     // temp schema variables for back page upload    
     VariablesUploadDataset var = new VariablesUploadDataset(file, 
     														file.getOriginalFilename(),
     														fileUrl,
     														firstRowAsHeader,
     														delimiter, 
     														dataSchema, 
     														schema,
     														ticket);
     session.setAttribute("varUp", var);
     
     return "schema";
   }
 
   @SuppressWarnings("rawtypes")
   @RequestMapping(value = "/{guid}", method = RequestMethod.GET)
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
   @RequestMapping(value = "/{guid}?start={start}&count={count}", method = RequestMethod.GET)
   public @ResponseBody List<Map> getData(@PathVariable String guid, @PathVariable int start, @PathVariable int count) {
     try {
       Dataset ds = datastoreService.getDatasetByGuid(guid);
       if (ds == null) {
         logger.debug("Can't find the dataset {}", guid);
         return null;
       }
 
       if (count <= 0)
         return null;
 
       return userDataService.getDatasetToList(ds.getName(), start, count);
     } catch (Exception e) {
       logger.debug("Exception occurs when retrieving dataset " + guid, e);
     }
     return null;
   }
 
   @RequestMapping(value = "/query", method = RequestMethod.POST)
   public @ResponseBody QueryResult queryDataset(@RequestParam(value = "guid", required = true) String guid,
       @RequestParam(value = "query", required = true) String query) {
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
 
   @RequestMapping(value = "/squery", method = RequestMethod.POST)
 	public String searchQuery(
 			@RequestParam(value = "q", required = true) String query,
 			@RequestParam(value = "oq", required = true) String oid,
 			ModelMap model, Principal principal) {
 		User user = (User) ((Authentication) principal).getPrincipal();
 	    ObjectMapper mapper = new ObjectMapper();
 	      	    
 	    try {
 	      Dataset ds = datastoreService.getDatasetByGuid(oid);
 	      if (ds == null) {
 	        logger.debug("Can't find the dataset {}", oid);
 	        model.addAttribute("errorMsg", "Dataset not found!");
 	        return "error";
 	      }
 	      
 	      try {
 	        String data = mapper.writeValueAsString(new String[] { "1", oid });
 	        datastoreService.logUserAction(user.getId(), UserActionLog.ActionCode.VIEW_DATAPAGE, data);
 	      } catch (Exception e) {
 	        logger.debug("Failed to log action", e);
 	      }
 
 	      model.addAttribute("dataset", ds);
 	      
 	      //---
 	      if (ds.getUser().getUsername().equals(user.getUsername())) {
 	        model.addAttribute("isOwner", true);
 	      } else
 	        model.addAttribute("isOwner", false);
 	      
 	      //get column query
 	      String[] columns = userDataService.getColumnNamesByQuery(query);
 	      
 //	      String scol_w_quote = query.substring(query.indexOf("[") +1, query.indexOf("]"))	,
 //	      		 scol_wno_quote = scol_w_quote.replace("`", "");
 //	      
 //	      String sCondition = query.substring(query.indexOf("]") + 2,query.length());
 //	      		 sCondition = (sCondition.trim().length() > 0) ? (" Where " + sCondition):("");
 	      		 
 //	      if (ds.getRowCount() < 1000) {
 	        List<Object[]> data = new ArrayList<Object[]>();
 	        data.add(columns);
 	        data.addAll(userDataService.getDatasetSearchQuery(query));
 	        model.addAttribute("data", mapper.writeValueAsString(data));
 	        model.addAttribute("squery_datapage", query);
 	        
 /*	      } else {
 	        model.addAttribute("columns", mapper.writeValueAsString((!scol_w_quote.equals("*"))?scol_wno_quote.split(","):columns));
 	        model.addAttribute("srStt", "true");
 	        model.addAttribute("srCondition", mapper.writeValueAsString(sCondition));
 	        model.addAttribute("data", null);
 	        model.addAttribute("guid", oid);
 	      }
 */
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
 	      //---
 	      
 	    } catch (Exception e) {
 	      logger.debug("", e);
 	      model.addAttribute("errorMsg", "Column information is not found.");
 	      return "error";
 	    }
 	    
 		return "datapage";
 	}
   
   @RequestMapping(value = "/m/{guids}", method = RequestMethod.GET)
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
         if (ds.getRowCount() < 100) {
           data = userDataService.getDatasetToString(ds.getName());
         } else {
           /*
            * Map row = userDataService.getDatasetToList(ds.getName(), 0,
            * 1).get(0); columns = new String[row.keySet().size()]; int i = 0;
            * for (Object s : row.keySet()) { columns[i++] = (String) s; }
            */
           columns = userDataService.getColumnNames(ds.getName());
         }
         DatasetDetail detail = new DatasetDetail(guid, ds.getName(), ds.getRowCount(), columns, data);
         // DatasetDetail detail = new DatasetDetail(guid, ds.getName());
         results.put(ds.getGuid(), detail);
       } catch (Exception e) {
         logger.debug("Exception occurs when retrieving dataset " + guid, e);
       }
     }
     return results;
   }
 
   @Deprecated
   @RequestMapping(value = "/up", method = RequestMethod.POST)
   public @ResponseBody ActionResult submitDataset(@RequestParam(value = "file", required = true) MultipartFile file,
       @RequestParam(value = "type", required = true) String type, ModelMap model, Principal principal) {
 
     User user = (User) ((Authentication) principal).getPrincipal();
 
     try {
       ObjectMapper logmapper = new ObjectMapper();
       String data;
       data = logmapper.writeValueAsString(new String[] { "1", type });
       datastoreService.logUserAction(user.getId(), UserActionLog.ActionCode.SUBMIT_DATASDET, data);
     } catch (Exception e) {
       logger.debug("Failed to log action", e);
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
     } else
       return new ActionResult(-1, "Unknown type");
 
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
     } catch (Exception e) {
       return new ActionResult(-1, "Cannot parse schema");
     }
 
   }
 
   @RequestMapping(value = "/persist", method = RequestMethod.POST)
   @ResponseBody public ActionResult persistDataset(@RequestParam(value = "ticket", required = true) String ticket,
       @RequestParam(value = "schema", required = true) String schema,
       @RequestParam(value = "name", required = true) String name,
       @RequestParam(value = "description", required = false) String description,
       @RequestParam(value = "tag", required = false) String tag,
       @RequestParam(value = "isPublic", required = true) boolean isPublic, Principal principal) {
 
     User user = (User) ((Authentication) principal).getPrincipal();
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
           if (columnNumber < 0)
             columnNumber = row.length;
         } catch (EOFException eof) {
           break;
         }
       }
       is.close();
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
       //add tag
       
       
       for (String tagItem : tag.split("[,]+")) {
     	  if(tagItem.trim()== null || tagItem.trim()== "")
     		  break;
     	  
     	  Tag tagObj = datastoreService.getTag(tagItem);
     	  if (tagObj == null) {
 				logger.debug(
 						"Tag {} does not exist. Trying create new tag.",
 						tagItem);
 				try {
 					datastoreService.createTag(tagItem);
 					tagObj = datastoreService.getTag(tagItem);
 				} catch (Exception e) {
 					logger.debug("Failed to create new tag {}", tagItem);
 					logger.debug("", e);					
 				}
 
 			}
     	  
     	  datastoreService.addDatasetTag(ds.getId(),
 					tagObj.getId());
   		
       }
       
       
      
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
 
   @SuppressWarnings("rawtypes")
   @RequestMapping(value = "/view/{guid}", method = RequestMethod.GET)
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
      
       model.addAttribute("schema", Utils.parseDatasetSchemaFromSqlCreate(ds.getSchema()));
 
       if (ds.getUser().getUsername().equals(user.getUsername())) {
         model.addAttribute("isOwner", true);
       } else
         model.addAttribute("isOwner", false);
 
       if (ds.getRowCount() < 1000) {
         // List<Map> data =
         // userDataService.getDatasetToList(ds.getName());
         List<Object[]> data = new ArrayList<Object[]>();
         data.add(userDataService.getColumnNames(ds.getName()));
         data.addAll(userDataService.getDatasetToListOfArray(ds.getName()));
         model.addAttribute("data", mapper.writeValueAsString(data));
       } else {
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
       
       // page view increment
       int pageView = datastoreService.increasePageView(ds.getId(), PageType.DATASET.getType());
       model.addAttribute("pageView", pageView);
 
     } catch (Exception e) {
       logger.debug("", e);
       model.addAttribute("errorMsg", e.getMessage());
       return "error";
     }
     return "datapage";
   }
 
   @RequestMapping(value = "/ajax/{guid}", method = RequestMethod.GET)
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
 
       // List<Map> data = userDataService.getDatasetToList(ds.getName(),
       // displayStart, displayLength);
       List<Object[]> data = userDataService.getDatasetToListOfArray(ds.getName(), displayStart, displayLength);
       // int totalDisplayRecords = data.size();
       int totalRecords = ds.getRowCount();
       result.put("iTotalRecords", totalRecords);
       result.put("iTotalDisplayRecords", totalRecords);
       result.put("aaData", data);
       /*
        * StringBuilder sColumns = new StringBuilder(); for (Object s :
        * data.get(0)) { String col = (String) s; sColumns.append(col + ","); }
        * sColumns.substring(0, sColumns.length() - 1); result.put("sColumns",
        * sColumns.toString());
        */
       return result;
     } catch (Exception e) {
       logger.debug("", e);
       return null;
     }
 
   }
 
   /**
    * Streams the dataset as CSV file format
    * 
    * @param guid
    *          dataset guid
    * @param req
    *          the <code>HttpServletRequest</code> object
    * @param res
    *          the <code>HttpServletResponse</code> object
    * @throws IOException
    */
   @RequestMapping(value = "/dl/{type}/{guid}", method = RequestMethod.GET)
   public @ResponseBody void download(@PathVariable String type, @PathVariable String guid, HttpServletRequest req, HttpServletResponse res)
       throws IOException {
 
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
         res.setHeader("Content-Disposition", "attachment; filename=\"" + ds.getName() + ".csv\"");
         userDataService.getCsvStream(ds.getName(), res.getOutputStream());
       } else if ("json".equalsIgnoreCase(type)) {
         res.setContentType("text/x-json;charset=utf-8");
         res.setHeader("Content-Disposition", "attachment; filename=\"" + ds.getName() + ".json\"");
         BufferedOutputStream buffer = new BufferedOutputStream(res.getOutputStream(), 8 * 1024);
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
 
   @RequestMapping(value = "/att/{type}/{appGuid}/{attName}", method = RequestMethod.GET)
   public @ResponseBody void downloadAttachment(@PathVariable String type, @PathVariable String appGuid, @PathVariable String attName,
       HttpServletRequest req, HttpServletResponse res) throws IOException {
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
         res.setHeader("Content-Disposition", "attachment; filename=\"" + attName + ".csv\"");
         Utils.jsonToCsv(attachment.getData(), res.getOutputStream());
       } else if ("json".equalsIgnoreCase(type)) {
         res.setContentType("text/x-json;charset=utf-8");
         res.setHeader("Content-Disposition", "attachment; filename=\"" + attName + ".json\"");
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
    * 
    * @param guid
    * @param refFile
    * @param refUrl
    * @return
    * @throws Exception
    */
   @RequestMapping(value = "/upload/ref/{guid}", method = RequestMethod.POST)
   public @ResponseBody ActionResult uploadReferenceDoc(@PathVariable String guid,
       @RequestParam(value = "file-ref", required = false) MultipartFile refFile,
       @RequestParam(value = "web-ref", required = false) String refUrl) throws Exception {
 
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
 
   @RequestMapping(value = "/ref/{guid}", method = RequestMethod.GET)
   public void getReferenceDocument(@PathVariable String guid, HttpServletRequest request, HttpServletResponse response)
       throws Exception {
     Dataset dataset = datastoreService.getDatasetByGuid(guid);
     if (dataset == null) {
       response.getWriter().write("Failed to load reference document. Error: Dataset not found.");
       return;
     }
 
     String refGuid = request.getParameter("ref");
 
     File refFile = appStoreService.getReferenceDocument(guid, refGuid + ".pdf");
     if (refFile == null) {
       response.getWriter().write("Failed to load reference document. Error: Reference file not found.");
       return;
     }
 
     IOUtils.copy(new FileInputStream(refFile), response.getOutputStream());
     return;
   }
 
   /**
 	 * Votes the analysis
 	 * 
 	 * @param guid
 	 * @param vote
 	 * @param model
 	 * @param principal
 	 * @throws Exception
 	 */
 	@RequestMapping(value = "/vote/{guid}", method = RequestMethod.POST)
 	public @ResponseBody String vote(@PathVariable String guid, @RequestParam(value="vote", required=true) int vote, ModelMap model, Principal principal) throws Exception {
 	  User user = (User) ((Authentication)principal).getPrincipal();
 	  if (user == null) {
 	    return "0";
 	  }
 	  try{
 		    ObjectMapper logmapper = new ObjectMapper();
 		    String data = logmapper.writeValueAsString(new String[] {"2", guid, Integer.toBinaryString(vote)});		   	 
 		    datastoreService.logUserAction(user.getId(),UserActionLog.ActionCode.VOTE,data);
 	  }catch (Exception e) {
 	      logger.debug("Failed to log action", e);
 	    }
 	
 	  Dataset ds = datastoreService.getDatasetByGuid(guid);
 	  if (ds == null) {
 	    return "0";
 	  }
 	  
 	  vote = vote>0?1:-1;
 	  
 	  try {
 	    
 	    DatasetVote dsVote = new DatasetVote();
 	    dsVote.setVote(vote);
 	    dsVote.setVoteAt(new Date());
 	    dsVote.setActive(true);
 	    boolean result = datastoreService.addDatasetVote(user.getId(), ds, dsVote);
 	  
 	    return (result?"1":"0");
 	  } catch (Exception e) {
 	    logger.debug("Failed to add new vote to analysis id {}, user id {}", ds.getId(), user.getId());
 	  }
 	  return "0";
 	}
 	
 	@RequestMapping(value = "/changetitle", method = RequestMethod.POST)
 	public @ResponseBody
 	ActionResult changeTitle(
 			@RequestParam(value = "guid", required = true) String guid,
 			@RequestParam(value = "newTitle", required = true) String newTitle,
 			WebRequest request, ModelMap model, Principal principal,
 			HttpSession session) {
 		User user = (User) ((Authentication) principal).getPrincipal();
 		Dataset dataset;
 		String message;
 		String oldTitle;
 		try {
 			dataset = datastoreService.getDatasetByGuid(guid);
 			if (dataset == null) {
 				message = "Error:Can not get dataset";
 				return new ActionResult(-1, message);
 			}
 			
 			if (!user.getUsername().equals(dataset.getUser().getUsername())) {
 				message = "Error: User does not have permission to change title";
 				return new ActionResult(-1,message);
 			}
 		
 			if (dataset.getName().equals(newTitle)) {
				message = "They are the same name. No need to change";
				return new ActionResult(-1,message);
 			} else {
				
				
 				oldTitle = dataset.getName();
 				newTitle=user.getUsername() + "." + newTitle;
 				dataset.setName(newTitle);				
 				if(datastoreService.updateDataset(dataset)){
 					//update bcDatastore
 					if(userDataService.renameDataset(oldTitle,newTitle)){
 						return new ActionResult(0, newTitle);
 					}else{
 						message = "An exception occurs when update bcDatastore";
 						dataset.setName(oldTitle);		
 						return new ActionResult(-1,	message);
 					}
 				}else{
 					message = "Can not update Dataset";
 					return new ActionResult(-1,	message);
 				}
 				
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block	
 			
 			return new ActionResult(-1,
 					"An exception occurs when changing the title");
 		}
 
 		
 	}
 
 }
