 package org.motechproject.carereporting.web.controller;
 
 import org.apache.log4j.Logger;
 import org.dwQueryBuilder.builders.QueryBuilder;
 import org.dwQueryBuilder.data.DwQuery;
 import org.jooq.SQLDialect;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.CronTaskEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.IndicatorClassificationEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorTypeEntity;
 import org.motechproject.carereporting.domain.LevelEntity;
 import org.motechproject.carereporting.domain.RoleEntity;
 import org.motechproject.carereporting.domain.dto.DwQueryDto;
 import org.motechproject.carereporting.domain.dto.IndicatorDto;
 import org.motechproject.carereporting.domain.views.BaseView;
 import org.motechproject.carereporting.domain.views.IndicatorJsonView;
 import org.motechproject.carereporting.domain.views.QueryJsonView;
 import org.motechproject.carereporting.exception.CareApiRuntimeException;
 import org.motechproject.carereporting.exception.CareRuntimeException;
 import org.motechproject.carereporting.indicator.DwQueryHelper;
 import org.motechproject.carereporting.service.CronService;
 import org.motechproject.carereporting.service.IndicatorService;
 import org.motechproject.carereporting.service.UserService;
 import org.motechproject.carereporting.xml.XmlIndicatorParser;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 import javax.xml.bind.UnmarshalException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 @RequestMapping("api/indicator")
 @Controller
 public class IndicatorController extends BaseController {
 
     private static final String AREA_NAME = "<area_name>";
     private static final String AREA_LEVEL_NAME = "<area_level_name>";
     @Autowired
     private IndicatorService indicatorService;
 
     @Autowired
     private CronService cronService;
 
     @Autowired
     private UserService userService;
 
     @Autowired
     private XmlIndicatorParser xmlIndicatorParser;
 
     @Value("${care.jdbc.schema}")
     private String careSchemaName;
 
     // IndicatorEntity
 
     @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getIndicatorList() {
         return this.writeAsString(BaseView.class,
                 indicatorService.getAllIndicators());
     }
 
     @RequestMapping(value = "/creationform", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     @Transactional
     public String getIndicatorCreationFormDto() {
         return this.writeAsString(IndicatorJsonView.CreationForm.class,
                 indicatorService.getIndicatorCreationFormDto());
     }
 
     @RequestMapping(value = "/queries/creationform", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     @Transactional
     public String getIndicatorQueryCreationFormDto() {
         return this.writeAsString(QueryJsonView.CreationForm.class,
                 indicatorService.getIndicatorQueryCreationFormDto());
     }
 
     @RequestMapping(value = "/queries", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getAllDwQueries() {
         return this.writeAsString(BaseView.class, indicatorService.getAllTopLevelDwQueries());
     }
 
     @RequestMapping(value = "/queries/{dwQueryId}", method = RequestMethod.DELETE,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public void deleteDwQueryById(@PathVariable Integer dwQueryId) {
         DwQueryEntity dwQueryEntity = indicatorService.getDwQueryById(dwQueryId);
         indicatorService.deleteDwQuery(dwQueryEntity);
     }
 
     @RequestMapping(value = "/queries/{dwQueryId}/getsql", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getQuerySqlById(@PathVariable Integer dwQueryId) {
         DwQueryEntity dwQueryEntity = indicatorService.getDwQueryById(dwQueryId);
         DwQueryHelper dwQueryHelper = new DwQueryHelper();
         DwQuery dwQuery = dwQueryHelper.buildDwQuery(dwQueryEntity, prepareMockArea());
         String sqlString = QueryBuilder.getDwQueryAsSQLString(SQLDialect.POSTGRES,
                 careSchemaName, dwQuery, true);
 
         return sqlString;
     }
 
     private AreaEntity prepareMockArea() {
         return new AreaEntity(AREA_NAME, new LevelEntity(AREA_LEVEL_NAME, null));
     }
 
     @RequestMapping(value = "/queries/new", method = RequestMethod.POST,
             consumes = { MediaType.APPLICATION_JSON_VALUE },
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public void createNewDwQuery(@RequestBody @Valid DwQueryDto dwQueryDto, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
 
         indicatorService.createNewDwQuery(dwQueryDto);
     }
 
     @RequestMapping(value = "/filter/{classificationId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getIndicatorListByClassificationId(@PathVariable Integer classificationId) {
         return this.writeAsString(BaseView.class,
                 indicatorService.getIndicatorsByClassificationId(classificationId));
     }
 
     @RequestMapping(value = "/{indicatorId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getIndicator(@PathVariable Integer indicatorId) {
         return this.writeAsString(IndicatorJsonView.IndicatorModificationDetails.class,
                 indicatorService.getIndicatorById(indicatorId));
     }
 
     @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     public void createNewIndicator(@RequestBody @Valid IndicatorDto indicatorDto,
             BindingResult bindingResult, HttpServletRequest request) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
         IndicatorEntity indicatorEntity = indicatorService.createIndicatorEntityFromDto(indicatorDto);
         if (!canUserCreateIndicator(indicatorEntity, request)) {
             List<FieldError> errors = new ArrayList<>();
             errors.add(new FieldError("indicator", "owners", "You don't have permission to add indicator with this report views."));
             throw new CareApiRuntimeException(errors);
         }
         indicatorService.createNewIndicator(indicatorEntity);
     }
 
     @RequestMapping(value = "/{indicatorId}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     public void updateIndicator(@RequestBody @Valid IndicatorDto indicatorDto,
             BindingResult bindingResult, @PathVariable Integer indicatorId) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
 
         indicatorService.updateIndicatorFromDto(indicatorDto);
     }
 
     @RequestMapping(value = "/{indicatorId}", method = RequestMethod.DELETE)
     @ResponseStatus(HttpStatus.OK)
     public void deleteIndicator(@PathVariable Integer indicatorId) {
         IndicatorEntity indicatorEntity = indicatorService.getIndicatorById(indicatorId);
         indicatorService.deleteIndicator(indicatorEntity);
     }
 
     // IndicatorTypeEntity
 
     @RequestMapping(value = "/type", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public Set<IndicatorTypeEntity> getIndicatorTypeList() {
         return indicatorService.getAllIndicatorTypes();
     }
 
     @RequestMapping(value = "/type/{indicatorTypeId}", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public IndicatorTypeEntity getIndicatorType(@PathVariable Integer indicatorTypeId) {
         return indicatorService.getIndicatorTypeById(indicatorTypeId);
     }
     // IndicatorClassificationEntity
 
     @RequestMapping(value = "/classification", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     @Transactional
     public String getIndicatorClassificationList() {
         return this.writeAsString(IndicatorJsonView.ListIndicatorNames.class,
                 indicatorService.getAllIndicatorClassifications());
     }
 
     @RequestMapping(value = "/classification/{indicatorClassificationId}", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     @PreAuthorize("hasRole('CAN_EDIT_CLASSIFICATIONS')")
     public String getIndicatoryClassification(@PathVariable Integer indicatorClassificationId) {
         return this.writeAsString(IndicatorJsonView.IndicatorDetails.class, indicatorService.getIndicatorClassificationById(indicatorClassificationId));
     }
 
     @RequestMapping(value = "/classification", method = RequestMethod.PUT,
             consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @PreAuthorize("hasRole('CAN_EDIT_CLASSIFICATIONS')")
     public void createNewIndicatorClassification(@RequestBody @Valid IndicatorClassificationEntity indicatorClassificationEntity,
             BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
         indicatorService.createNewIndicatorClassification(indicatorClassificationEntity);
     }
 
     @RequestMapping(value = "/classification/{indicatorClassificationId}", method = RequestMethod.PUT,
             consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @Transactional(readOnly = false)
     @PreAuthorize("hasRole('CAN_EDIT_CLASSIFICATIONS')")
     public void updateIndicatorClassification(@RequestBody @Valid IndicatorClassificationEntity indicatorClassificationEntity,
             BindingResult bindingResult, @PathVariable Integer indicatorClassificationId) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
 
         IndicatorClassificationEntity foundIndicatorClassificationEntity = indicatorService.getIndicatorClassificationById(indicatorClassificationId);
 
         foundIndicatorClassificationEntity.setName(indicatorClassificationEntity.getName());
         indicatorService.updateIndicatorClassification(foundIndicatorClassificationEntity);
     }
 
     @RequestMapping(value = "/classification/{indicatorClassificationId}", method = RequestMethod.DELETE)
     @ResponseStatus(HttpStatus.OK)
     @Transactional(readOnly = false)
     public void deleteIndicatorClassification(@PathVariable Integer indicatorClassificationId) {
         IndicatorClassificationEntity indicatorClassificationEntity = indicatorService.getIndicatorClassificationById(indicatorClassificationId);
         Set<IndicatorEntity> indicatorEntities = indicatorService.getIndicatorsByClassificationId(indicatorClassificationId);
         Iterator<IndicatorEntity> iterator = indicatorEntities.iterator();
 
         while (iterator.hasNext()) {
             IndicatorEntity indicatorEntity = iterator.next();
             indicatorEntity.getClassifications().remove(indicatorClassificationEntity);
 
             if (indicatorEntity.getClassifications().size() > 0) {
                 indicatorService.updateIndicator(indicatorEntity);
             } else {
                 indicatorService.deleteIndicator(indicatorEntity);
             }
         }
 
         indicatorService.deleteIndicatorClassification(indicatorClassificationEntity);
     }
 
     @RequestMapping(value = "/calculator/frequencies", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getAllFrequencies() {
         return writeAsString(BaseView.class, cronService.getAllFrequencies());
     }
 
     @RequestMapping(value = "/calculator/frequency/daily", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getDailyTaskTime() {
         return cronService.getDailyCronTask().getTime();
     }
 
     @RequestMapping(value = "/calculator/frequency/daily", method = RequestMethod.PUT,
             consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     public void updateDailyTaskTime(@RequestBody String time) {
         CronTaskEntity cronTaskEntity = cronService.getDailyCronTask();
         cronTaskEntity.setTime(time);
 
         cronService.updateCronTask(cronTaskEntity);
     }
 
     @RequestMapping(value = "/upload", method = RequestMethod.POST)
     public String uploadIndicatorXml(@RequestParam("file") MultipartFile[] files, RedirectAttributes redirectAttrs, HttpServletRequest request) {
         try {
             for (MultipartFile file : files) {
                 IndicatorEntity indicatorEntity = xmlIndicatorParser.parse(file.getInputStream());
                 if (!canUserCreateIndicator(indicatorEntity, request)) {
                     throw new CareRuntimeException("You don't have permission to create indicator with this owner and/or report views.");
                 }
                 indicatorService.createNewIndicator(indicatorEntity);
             }
             return "redirect:/#/indicators";
         } catch (UnmarshalException e) {
             String message;
             if (e.getLinkedException() != null) {
                 if (e.getLinkedException().getCause() != null) {
                     message = e.getLinkedException().getCause().getMessage();
                 } else {
                     message = e.getLinkedException().getMessage();
                 }
             } else {
                 message = e.getMessage();
             }
             redirectAttrs.addFlashAttribute("error", message);
         } catch (Exception e) {
             Logger.getLogger(IndicatorController.class).error("", e);
             redirectAttrs.addFlashAttribute("error", e.getMessage());
         }
         return "redirect:/#/indicators/upload-xml";
     }
 
     private boolean canUserCreateIndicator(IndicatorEntity indicatorEntity, HttpServletRequest request) {
         if (!canUserCreateIndicators(request)) {
             return false;
         }
         boolean indicatorHasRoles = indicatorEntity.getRoles() != null && indicatorEntity.getRoles().size() > 0;
         if (indicatorHasRoles) {
             return isUserAdmin();
         }
         return isCurrentUserOwnerOfIndicator(indicatorEntity);
     }
 
     private boolean isUserAdmin() {
         return userService.getCurrentlyLoggedUser()
                 .getRoles().contains(new RoleEntity("Admin"));
     }
 
     private boolean isCurrentUserOwnerOfIndicator(IndicatorEntity indicatorEntity) {
         return indicatorEntity.getOwner() != null && indicatorEntity.getOwner().equals(userService.getCurrentlyLoggedUser());
     }
 
     private boolean canUserCreateIndicators(HttpServletRequest request) {
         return request.isUserInRole("CAN_CREATE_INDICATORS");
     }
 
     @RequestMapping(value = "{indicatorId}/export/caselistreport", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @Transactional
     public ResponseEntity<byte[]> exportCaseListReport(
             @PathVariable Integer indicatorId,
             @RequestParam(required = false) Date fromDate,
             @RequestParam(required = false) Date toDate,
             @RequestParam Integer areaId) {
 
         IndicatorEntity indicatorEntity = indicatorService.getIndicatorById(indicatorId);
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd.yyyy_HH.mm");
         String filename = indicatorEntity.getName() + "_" + simpleDateFormat.format(new Date()) + ".csv";
 
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
         headers.setContentDispositionFormData("attachment", filename);
 
         return new ResponseEntity<>(indicatorService.getCaseListReportAsCsv(indicatorEntity, areaId, fromDate, toDate),
                 headers, HttpStatus.OK);
     }
 
     @RequestMapping(value = "/calculator/dateDepth", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getDateDepth() {
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         return sdf.format(cronService.getDateDepth());
     }
 
     @RequestMapping(value = "/calculator/dateDepth", method = RequestMethod.PUT,
         consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     public void setDateDepth(@RequestBody @Valid Date newDateDepth,
                                BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
         cronService.updateDateDepth(newDateDepth);
     }
 
     @RequestMapping(value = "calculator/recalculate/{classificationId}", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     public void recalculateIndicators(@PathVariable Integer classificationId) {
         indicatorService.calculateAllIndicators(classificationId);
     }
 }
