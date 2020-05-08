 package org.motechproject.carereporting.web.controller;
 
 import org.apache.log4j.Logger;
 import org.motechproject.carereporting.domain.CronTaskEntity;
 import org.motechproject.carereporting.domain.IndicatorCategoryEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorTypeEntity;
 import org.motechproject.carereporting.domain.dto.IndicatorDto;
 import org.motechproject.carereporting.domain.views.BaseView;
 import org.motechproject.carereporting.domain.views.IndicatorJsonView;
 import org.motechproject.carereporting.exception.CareApiRuntimeException;
 import org.motechproject.carereporting.service.CronService;
 import org.motechproject.carereporting.service.IndicatorService;
 import org.motechproject.carereporting.xml.XmlIndicatorParser;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.validation.Valid;
 import javax.xml.bind.UnmarshalException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Set;
 
 @RequestMapping("api/indicator")
 @Controller
 public class IndicatorController extends BaseController {
 
     @Autowired
     private IndicatorService indicatorService;
 
     @Autowired
     private CronService cronService;
 
     @Autowired
     private XmlIndicatorParser xmlIndicatorParser;
 
     // IndicatorEntity
 
     @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getIndicatorList() {
         return this.writeAsString(BaseView.class,
                 indicatorService.getAllIndicators());
     }
 
     @RequestMapping(value = "/filter/{categoryId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getIndicatorListByCategoryId(@PathVariable Integer categoryId) {
         return this.writeAsString(BaseView.class,
                 indicatorService.getIndicatorsByCategoryId(categoryId));
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
             BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
 
         indicatorService.createNewIndicatorFromDto(indicatorDto);
     }
 
     @RequestMapping(value = "/{indicatorId}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     public void updateIndicator(@RequestBody @Valid IndicatorDto indicatorDto,
             BindingResult bindingResult, @PathVariable Integer indicatorId) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
 
         indicatorDto.setId(indicatorId);
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
     // IndicatorCategoryEntity
 
     @RequestMapping(value = "/category", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getIndicatorCategoryList() {
 
         return this.writeAsString(IndicatorJsonView.IndicatorDetails.class,
                 indicatorService.getAllIndicatorCategories());
     }
 
     @RequestMapping(value = "/category/{indicatorCategoryId}", method = RequestMethod.GET,
             produces = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     @ResponseBody
     public String getIndicatoryCategory(@PathVariable Integer indicatorCategoryId) {
         return this.writeAsString(IndicatorJsonView.IndicatorDetails.class, indicatorService.getIndicatorCategoryById(indicatorCategoryId));
     }
 
     @RequestMapping(value = "/category", method = RequestMethod.PUT,
             consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     public void createNewIndicatorCategory(@RequestBody @Valid IndicatorCategoryEntity indicatorCategoryEntity,
             BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
 
         indicatorService.createNewIndicatorCategory(indicatorCategoryEntity);
     }
 
     @RequestMapping(value = "/category/{indicatorCategoryId}", method = RequestMethod.PUT,
             consumes = { MediaType.APPLICATION_JSON_VALUE })
     @ResponseStatus(HttpStatus.OK)
     public void updateIndicatorCategory(@RequestBody @Valid IndicatorCategoryEntity indicatorCategoryEntity,
             BindingResult bindingResult, @PathVariable Integer indicatorCategoryId) {
         if (bindingResult.hasErrors()) {
             throw new CareApiRuntimeException(bindingResult.getFieldErrors());
         }
 
         IndicatorCategoryEntity foundIndicatorCategoryEntity = indicatorService.getIndicatorCategoryById(indicatorCategoryId);
 
         foundIndicatorCategoryEntity.setName(indicatorCategoryEntity.getName());
         foundIndicatorCategoryEntity.setShortCode(indicatorCategoryEntity.getShortCode());
         indicatorService.updateIndicatorCategory(foundIndicatorCategoryEntity);
     }
 
     @RequestMapping(value = "/category/{indicatorCategoryId}", method = RequestMethod.DELETE)
     @ResponseStatus(HttpStatus.OK)
     public void deleteIndicatorCategory(@PathVariable Integer indicatorCategoryId) {
         IndicatorCategoryEntity indicatorCategoryEntity = indicatorService.getIndicatorCategoryById(indicatorCategoryId);
 
         indicatorService.deleteIndicatorCategory(indicatorCategoryEntity);
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
     public String uploadIndicatorXml(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttrs) {
         try {
             IndicatorEntity indicatorEntity = xmlIndicatorParser.parse(file.getInputStream());
             indicatorService.createNewIndicator(indicatorEntity);
            return "redirect:/#";
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
         return "redirect:/#/indicator/upload-xml";
     }
 
     @RequestMapping(value = "{indicatorId}/export/caselistreport", method = RequestMethod.GET)
     @ResponseStatus(HttpStatus.OK)
     @Transactional
     public ResponseEntity<byte[]> exportCaseListReport(
             @PathVariable Integer indicatorId,
             @RequestParam(required = false) Date fromDate,
             @RequestParam(required = false) Date toDate) {
 
         IndicatorEntity indicatorEntity = indicatorService.getIndicatorById(indicatorId);
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy_HH.mm");
         String filename = indicatorEntity.getName() + "_" + simpleDateFormat.format(new Date()) + ".csv";
 
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
         headers.setContentDispositionFormData("attachment", filename);
 
         return new ResponseEntity<>(indicatorService.getCaseListReportAsCsv(indicatorEntity, fromDate, toDate),
                 headers, HttpStatus.OK);
     }
 
 }
