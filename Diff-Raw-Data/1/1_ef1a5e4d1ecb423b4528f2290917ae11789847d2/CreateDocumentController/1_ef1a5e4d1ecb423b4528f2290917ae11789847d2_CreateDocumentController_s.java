 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.gispro.petrores.doc.controller;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.management.RuntimeErrorException;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.ApplicationPath;
 import javax.ws.rs.Produces;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.util.Streams;
 import org.apache.commons.io.output.NullOutputStream;
 import org.codehaus.jackson.JsonEncoding;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.AnnotationIntrospector;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 import ru.gispro.petrores.doc.entities.*;
 import ru.gispro.petrores.doc.entities.File;
 import ru.gispro.petrores.doc.entities.Files;
 import ru.gispro.petrores.doc.util.Util;
 import ru.gispro.petrores.doc.view.JsonView;
 
 /**
  *
  * @author fkravchenko
  */
 @Controller
 @RequestMapping(value = "/newDocument")
 public class CreateDocumentController{// implements ServletContextAware{
     @PersistenceContext(unitName = "petro21PU")
     protected EntityManager entityManager;
     
     //private ServletContext servletContext;
     private String rootPath = null;
     private static DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
     //private static JsonView view = new JsonView();
         
         
 
     public CreateDocumentController() {
     }
     
     @RequestMapping(method = RequestMethod.POST)
     //@Produces({"application/json"})
     //@ResponseStatus(HttpStatus.CREATED)
     @Transactional
     public void create(HttpServletRequest req, HttpServletResponse resp) throws Exception {
         
         ObjectMapper mapper = new ObjectMapper();
         resp.setHeader("Content-Type", "text/html; charset=UTF-8"); // "application/json");
         
         try{
         
             if(!ServletFileUpload.isMultipartContent(req)){
                 
                 Document doc = new Document(); 
 
                 ArrayList<Author>authors = new ArrayList<Author>(3);
                 ArrayList<String>directories = new ArrayList<String>(3);
                 ArrayList<GeoObject>geoObjects = new ArrayList<GeoObject>(3);
                 ArrayList<Word>words = new ArrayList<Word>(3);
                 
                 // Parse the request
                 
                 Map<String, String[]> params = req.getParameterMap();
                 Iterator<Map.Entry<String, String[]>>iter = params.entrySet().iterator();
                 while (iter.hasNext()) {
                     Map.Entry<String, String[]> item = iter.next();
                     String name = item.getKey();
                     String[]value = item.getValue();
                     {
 
                         if("domain".equals(name)){
                             //Long id = Long.parseLong(Streams.asString(stream, "UTF-8"));
                             //Domain domain = entityManager.find(Domain.class, id);
                             Domain domain = mapper.readValue(value[0], Domain.class);
                             //Domain domain = entityManager.find(Domain.class, mapper.readValue(stream, Domain.class).getId());
 
                             doc.setDomain(domain);
 
                             /*midPath = domain.getPathPart();
                             while(domain.getParent()!=null && domain.getParent().getId()!=domain.getId()){
                                 domain = domain.getParent();
                                 midPath = domain.getPathPart() + java.io.File.separator + midPath;
                             }
 
                             midPath = getMidPath(midPath);
 
                             realPath = new java.io.File(getRootPath(req) + midPath);
                             realPath.mkdirs();*/
                         }else{
 
                             if("year".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setYear(Integer.parseInt(s));
                             }else if("id".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setId(Long.parseLong(s));
                             }else if("number".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setNumber(s);
                             }else if("archiveNumber".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setArchiveNumber(s);
                             }else if("title".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setTitle(s);
                             }else if("fullTitle".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setFullTitle(s);
                             }else if("comment".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setComment(s);
                             }else if("originationDetails".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setOriginationDetails(s);
                             }else if("limitationDetails".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setLimitationDetails(s);
                             }else if("originationDate".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setOriginationDate(df.parse(s));
                             }else if("approvalDate".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setApprovalDate(df.parse(s));
                             }else if("registrationDate".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setRegistrationDate(df.parse(s));
                             }else if("placementDate".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setPlacementDate(df.parse(s));
                             }else if("type".equals(name)){
                                     doc.setType(mapper.readValue(value[0], Type.class));
                             }else if("stage".equals(name)){
                                 doc.setStage(mapper.readValue(value[0], Stage.class));
                             }else if("site".equals(name)){
                                 doc.setSite(mapper.readValue(value[0], Site.class));
                             }else if("author".equals(name)){
                                 for(String val: value){
                                     authors.add(mapper.readValue(val, Author.class));
                                 }
                             }else if("directory".equals(name)){
                                 for(String val: value){
                                     directories.add(val);
                                 }
                             }else if("geoObjects".equals(name)){
                                 if(!value[0].equals("\"\"")){
                                     GeoObject[] gos = mapper.readValue(value[0], GeoObject[].class);
                                     geoObjects.addAll(Arrays.asList(gos));
                                 }
                             }else if("words".equals(name)){
                                 if(!value[0].equals("\"\"")){
                                     Word[] gos = mapper.readValue(value[0], Word[].class);
                                     words.addAll(Arrays.asList(gos));
                                 }
                             }else if("geometryType".equals(name)){
                                 if(!value[0].equals("\"\""))
                                     doc.setGeometryType(mapper.readValue(value[0], GeoType.class));
                             }else if("periodicity".equals(name)){
                                 if(!value[0].equals("\"\""))
                                     doc.setPeriodicity(mapper.readValue(value[0], Periodicity.class));
                             }else if("classification".equals(name)){
                                 if(!value[0].equals("\"\""))
                                     doc.setClassification(mapper.readValue(value[0], Classification.class));
                             }else if("typeOfWork".equals(name)){
                                 if(!value[0].equals("\"\""))
                                     doc.setTypeOfWork(mapper.readValue(value[0], TypeOfWork.class));
                             }else if("workProcess".equals(name)){
                                 if(!value[0].equals("\"\""))
                                     doc.setWorkProcess(mapper.readValue(value[0], WorkProcess.class));
                             }else if("scale".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setScale(Integer.parseInt(s));
                             }else if("resolution".equals(name)){
                                 String s = value[0].trim();
                                 if(s.length()!=0)
                                     doc.setResolution(Integer.parseInt(s));
                             }else if("projection".equals(name)){
                                 if(!value[0].equals("\"\""))
                                     doc.setProjection(mapper.readValue(value[0], Projection.class));
                                 //String s = value[0].trim();
                                 //if(s.length()!=0)
                                 //    doc.setProjectionCode(s);
                             }else{
                                 // throw away yet
                                 //Streams.asString(stream);
                             }
                         }
                     }
                 }        
 
                 //Files ret = new Files(files, (long)files.size());
 
                 ArrayList<Author> a2 = new ArrayList(authors.size());
                 for(Author a: authors){
                     a2.add(entityManager.find(Author.class, a.getId()));
                 }
                 ArrayList<Word> w2 = new ArrayList(authors.size());
                 for(Word a: words){
                     Word w = entityManager.find(Word.class, a.getWord());
                     if(w==null){
                         //w = a;
                         w = entityManager.merge(a);
                     }
                     w2.add(w);
                 }
                 Query q = entityManager.createQuery(
                     "SELECT f FROM File f WHERE f.path = :path");
                 ArrayList<File>files = new ArrayList<File>(directories.size());
                 for(String a: directories){
                     q.setParameter("path", a);
                     File f;
                     List<File>fs = q.getResultList();
                     if(!fs.isEmpty()){
                         f = fs.get(0);
                     }else{
                         f = new File();
                         f.setPath(a);
                         f.setDocument(doc);
                         f.setFileName("");
                         f.setMimeType("directory");
                     }
                     files.add(f);
                 }
                 
                 ArrayList<GeoObject> go2 = new ArrayList(geoObjects.size());
                 q = entityManager.createQuery(
                         "SELECT object(o) "
                         + "FROM GeoObject AS o "
                         + "WHERE o.idInTable = :idInTable "
                         + "AND o.tableName = :tableName");
 
                 for(GeoObject a: geoObjects){
                     q.setParameter("idInTable", a.getIdInTable());
                     q.setParameter("tableName", a.getTableName());
                     List<GeoObject> goes = q.getResultList();
                     if(!goes.isEmpty()){
                         a = goes.get(0);
                     }else{
                         a = entityManager.merge(a);
                     }
                     go2.add(a);
                 }
 
                 entityManager.flush();
 
                 doc.setAuthors(a2);
                 doc.setGeoObjects(go2);
                 doc.setFiles(files);
                 doc.setPlacementDate(new Date());
                 doc.setWords(w2);
                 
                 Author placer = (Author) entityManager.
                         createNamedQuery("Author.findByLogin").
                         setParameter("login", req.getUserPrincipal().getName()).
                         getResultList().get(0);
                 doc.setPlacer(placer);
 
                 entityManager.merge(doc);
                 entityManager.flush();
 
                 //for(File f: files){
                 //    f.setDocument(doc);
                 //}
 
                 //entityManager.flush();
 
 
 
                 Documents ret = new Documents(Arrays.asList(new Document[]{doc}), 1l);
                 /*ModelMap map = new ModelMap(ret);
                 map.addAttribute("success", true);
                 //View view = new JsonView();
                 ModelAndView mv = new ModelAndView(view, map);
                 return mv;*/
 
                 AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
                 mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
                 mapper.getSerializationConfig().setAnnotationIntrospector(introspector);   
 
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 Util.DoubleOutput udo = new Util.DoubleOutput(baos, resp.getOutputStream());
                 
                 JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(udo, JsonEncoding.UTF8);
                 ObjectNode json = mapper.valueToTree(ret);
                 json.put("success", true);
                 mapper.writeTree(generator, json);
                 generator.flush();
 
                 doc.setCondensed(baos.toString("UTF-8"));
                 entityManager.flush();
             
             }else{
 
                 Document doc = new Document(); 
 
                 // Create a new file upload handler
                 ServletFileUpload upload = new ServletFileUpload();
                 upload.setHeaderEncoding("UTF-8");
 
                 //String path = null;
 
                 java.io.File realPath = null;
                 String midPath = null;
 
                 ArrayList<File> files = new ArrayList<File>(3);
                 ArrayList<Author>authors = new ArrayList<Author>(3);
                 ArrayList<String>directories = new ArrayList<String>(3);
                 ArrayList<GeoObject>geoObjects = new ArrayList<GeoObject>(3);
                 ArrayList<Word>words = new ArrayList<Word>(3);
 
                 // Parse the request
                 FileItemIterator iter = upload.getItemIterator(req);
                 while (iter.hasNext()) {
                     FileItemStream item = iter.next();
                     String name = item.getFieldName();
                     InputStream stream = item.openStream();
                     if (item.isFormField()) {
 
                         if("domain".equals(name)){
                             //Long id = Long.parseLong(Streams.asString(stream, "UTF-8"));
                             //Domain domain = entityManager.find(Domain.class, id);
                             Domain domain = mapper.readValue(stream, Domain.class);
                             //Domain domain = entityManager.find(Domain.class, mapper.readValue(stream, Domain.class).getId());
 
                             doc.setDomain(domain);
 
                             midPath = domain.getPathPart();
                             while(domain.getParent()!=null && domain.getParent().getId()!=domain.getId()){
                                 domain = domain.getParent();
                                 midPath = domain.getPathPart() + java.io.File.separator + midPath;
                             }
 
                             midPath = getMidPath(midPath);
 
                             realPath = new java.io.File(getRootPath(req) + midPath);
                             realPath.mkdirs();
                         }else{
 
                             if("year".equals(name)){
                                 String s = Streams.asString(stream).trim();
                                 if(s.length()!=0)
                                     doc.setYear(Integer.parseInt(s));
                             }else if("number".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setNumber(s);
                             }else if("archiveNumber".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setArchiveNumber(s);
                             }else if("title".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setTitle(s);
                             }else if("fullTitle".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setFullTitle(s);
                             }else if("comment".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setComment(s);
                             }else if("originationDetails".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setOriginationDetails(s);
                             }else if("limitationDetails".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setLimitationDetails(s);
                             }else if("originationDate".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setOriginationDate(df.parse(s));
                             }else if("approvalDate".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setApprovalDate(df.parse(s));
                             }else if("registrationDate".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setRegistrationDate(df.parse(s));
                             }else if("placementDate".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setPlacementDate(df.parse(s));
                             }else if("type".equals(name)){
                                     doc.setType(mapper.readValue(stream, Type.class));
                             }else if("stage".equals(name)){
                                 doc.setStage(mapper.readValue(stream, Stage.class));
                             }else if("site".equals(name)){
                                 doc.setSite(mapper.readValue(stream, Site.class));
                             }else if("author".equals(name)){
                                 authors.add(mapper.readValue(stream, Author.class));
                             }else if("directory".equals(name)){
                                 directories.add(Streams.asString(stream, "UTF-8"));
                             }else if("geoObjects".equals(name)){
                                 GeoObject[] gos = mapper.readValue(stream, GeoObject[].class);
                                 geoObjects.addAll(Arrays.asList(gos));
                                 //doc.setSite(mapper.readValue(stream, Site.class));
                             }else if("words".equals(name)){
                                 Word[] gos = mapper.readValue(stream, Word[].class);
                                 words.addAll(Arrays.asList(gos));
                             }else if("geometryType".equals(name)){
                                 doc.setGeometryType(mapper.readValue(stream, GeoType.class));
                             }else if("periodicity".equals(name)){
                                 doc.setPeriodicity(mapper.readValue(stream, Periodicity.class));
                             }else if("typeOfWork".equals(name)){
                                 doc.setTypeOfWork(mapper.readValue(stream, TypeOfWork.class));
                             }else if("workProcess".equals(name)){
                                 doc.setWorkProcess(mapper.readValue(stream, WorkProcess.class));
                             }else if("classification".equals(name)){
                                 doc.setClassification(mapper.readValue(stream, Classification.class));
                             }else if("scale".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setScale(Integer.parseInt(s));
                             }else if("resolution".equals(name)){
                                 String s = Streams.asString(stream, "UTF-8").trim();
                                 if(s.length()!=0)
                                     doc.setResolution(Integer.parseInt(s));
                             }else if("projection".equals(name)){
                                 doc.setProjection(mapper.readValue(stream, Projection.class));
                                 //String s = Streams.asString(stream, "UTF-8").trim();
                                 //if(s.length()!=0)
                                 //    doc.setProjectionCode(s);
                             }else{
                                 // throw away yet
                                 Streams.asString(stream);
                             }
                         }
 
                     } else {
 
                         if(midPath==null){
                             //throw new RuntimeException("No path met");
                             JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(resp.getOutputStream(), JsonEncoding.UTF8);
                             ObjectNode json = mapper.createObjectNode();
                             json.put("failure", true);
                             json.put("msg", "No path met");
                             mapper.writeTree(generator, json);
                             generator.flush();
                             return;
                         }
 
                         String itemGetName = item.getName();
                         {
                             int lastSlash = itemGetName.lastIndexOf('\\');
                             if(lastSlash>=0){
                                 itemGetName = itemGetName.substring(lastSlash+1);
                             }
                             
                         }
                         java.io.File realFile = new java.io.File(realPath, itemGetName);
                         if(realFile.exists()){
                             //throw new RuntimeException("file " + realFile.getAbsolutePath() + " exists");
                             JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(resp.getOutputStream(), JsonEncoding.UTF8);
                             ObjectNode json = mapper.createObjectNode();
                             json.put("failure", true);
                             json.put("msg", "file " + realFile.getAbsolutePath() + " exists");
                             mapper.writeTree(generator, json);
                             generator.flush();
                             return;
                         }
                         FileOutputStream fos = new FileOutputStream(realFile);
                         Util.inputToOutWithIndex(
                                 req.getSession().getServletContext().getInitParameter("solrUrl"), 
                                 stream, 
                                 fos,
                                 midPath + itemGetName,
                                 item.getContentType());
                         //BufferedInputStream bis = new BufferedInputStream(stream);
                         //BufferedOutputStream bos = new BufferedOutputStream(fos);
                         //int byt;
                         //while((byt = bis.read()) != -1){
                         //    bos.write(byt);
                         //}
                         //bos.close();
                         //bis.close();
                         stream.close();
                         fos.close();
 
                         File file = new File();
                         file.setPath(midPath + itemGetName);
                         file.setFileName(itemGetName);
                         file.setMimeType(item.getContentType());
 
                         entityManager.persist(file);
                         //entityManager.flush();
 
                         files.add(file);
                     }
                 }        
 
                 //Files ret = new Files(files, (long)files.size());
 
                 ArrayList<Author> a2 = new ArrayList(authors.size());
                 for(Author a: authors){
                     a2.add(entityManager.find(Author.class, a.getId()));
                 }
                 ArrayList<Word> w2 = new ArrayList(authors.size());
                 for(Word a: words){
                     Word w = entityManager.find(Word.class, a.getWord());
                     if(w==null){
                         //w = a;
                         w = entityManager.merge(a);
                     }
                     w2.add(w);
                 }
 
                 //ArrayList<File> dirs = new ArrayList(directories.size());
                 Query q = entityManager.createQuery(
                     "SELECT f FROM File f WHERE f.path = :path");
                 for(String a: directories){
                     q.setParameter("path", a);
                     File f;
                     List<File>fs = q.getResultList();
                     if(!fs.isEmpty()){
                         f = fs.get(0);
                     }else{
                         f = new File();
                         f.setPath(a);
                         f.setFileName("");
                         f.setMimeType("directory");
                     }
                     files.add(f);
                 }
                 
                 
                 ArrayList<GeoObject> go2 = new ArrayList(geoObjects.size());
                 q = entityManager.createQuery(
                         "SELECT object(o) "
                         + "FROM GeoObject AS o "
                         + "WHERE o.idInTable = :idInTable "
                         + "AND o.tableName = :tableName");
 
                 for(GeoObject a: geoObjects){
                     q.setParameter("idInTable", a.getIdInTable());
                     q.setParameter("tableName", a.getTableName());
                     List<GeoObject> goes = q.getResultList();
                     if(!goes.isEmpty()){
                         a = goes.get(0);
                     }else{
                         a = entityManager.merge(a);
                     }
                     go2.add(a);
                 }
 
                 entityManager.flush();
 
                 doc.setAuthors(a2);
                 doc.setGeoObjects(go2);
                 doc.setFiles(files);
                 doc.setPlacementDate(new Date());
                 doc.setWords(w2);
                 
                 Author placer = (Author) entityManager.
                         createNamedQuery("Author.findByLogin").
                         setParameter("login", req.getUserPrincipal().getName()).
                         getResultList().get(0);
                 doc.setPlacer(placer);
 
                 entityManager.persist(doc);
                 entityManager.flush();
 
                 for(File f: files){
                     f.setDocument(doc);
                 }
 
                 entityManager.flush();
 
 
 
                 Documents ret = new Documents(Arrays.asList(new Document[]{doc}), 1l);
                 /*ModelMap map = new ModelMap(ret);
                 map.addAttribute("success", true);
                 //View view = new JsonView();
                 ModelAndView mv = new ModelAndView(view, map);
                 return mv;*/
 
                 AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
                 mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
                 mapper.getSerializationConfig().setAnnotationIntrospector(introspector);   
                 
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 Util.DoubleOutput udo = new Util.DoubleOutput(baos, resp.getOutputStream());
 
                 JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(udo, JsonEncoding.UTF8);
                 ObjectNode json = mapper.valueToTree(ret);
                 json.put("success", true);
                 mapper.writeTree(generator, json);
                 generator.flush();
                 
                 Util.inputToOutWithIndex(
                     req.getSession().getServletContext().getInitParameter("solrUrl"), 
                     new ByteArrayInputStream(baos.toByteArray()), 
                     new NullOutputStream(),
                     "solr" + doc.getFiles().iterator().next().getPath(),
                     "application/json");
                 
                 doc.setCondensed(baos.toString("UTF-8"));
                 entityManager.flush();
 
             }/*else{
                 JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(resp.getOutputStream(), JsonEncoding.UTF8);
                 ObjectNode json = mapper.createObjectNode();
                 json.put("failure", true);
                 json.put("msg", "No file");
                 mapper.writeTree(generator, json);
                 generator.flush();
             }*/
         }catch(Exception e){
             JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(resp.getOutputStream(), JsonEncoding.UTF8);
             ObjectNode json = mapper.createObjectNode();
             json.put("failure", true);
             json.put("msg", e.toString());
             mapper.writeTree(generator, json);
             generator.flush();
         }
     }
     
 
     private String getMidPath(String midPath){
         midPath = midPath.trim();
         if(midPath.length()>0){
             if(midPath.startsWith(java.io.File.separator))
                 midPath = midPath.substring(1);
             if(!midPath.endsWith(java.io.File.separator))
                 midPath = midPath + java.io.File.separator;
         }
         //path = getRootPath(req) + path;
         return midPath;
     }
     
     private String getRootPath(HttpServletRequest req){
         if(rootPath==null){
             rootPath = req.getSession().getServletContext().getInitParameter("documentsPath");
             if(rootPath==null)
                 rootPath = java.io.File.separator;
             else if(!rootPath.endsWith(java.io.File.separator))
                 rootPath = rootPath + java.io.File.separator;
         }
         return rootPath;
     }
     
     /*@ExceptionHandler(Exception.class)
     //@Produces({"application/json"})
     public @ResponseBody ModelMap handleException(Exception e, HttpServletResponse resp){
         ModelMap map = new ModelMap();
         map.addAttribute("success", false);
         map.addAttribute("msg", e.toString());
         return map;
     }*/
     
 }
