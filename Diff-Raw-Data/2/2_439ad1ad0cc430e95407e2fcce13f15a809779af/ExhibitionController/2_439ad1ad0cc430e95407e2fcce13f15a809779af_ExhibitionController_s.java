 package cn.mobiledaily.web.controller;
 
 import cn.mobiledaily.module.applicant.domain.Applicant;
 import cn.mobiledaily.module.exhibition.domain.Exhibition;
 import cn.mobiledaily.common.exception.InternalServerError;
 import cn.mobiledaily.common.exception.InvalidValueException;
 import cn.mobiledaily.common.exception.ValidationException;
 import cn.mobiledaily.module.exhibition.service.ExhibitionService;
 import cn.mobiledaily.common.service.FileService;
 import cn.mobiledaily.common.service.SecurityService;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 @Controller
 @RequestMapping("exhibitions")
 public class ExhibitionController {
     private static final String DATE_FORMAT = "yyyy-M-d";
     private Logger logger = LoggerFactory.getLogger(ExhibitionController.class);
     @Autowired
     private ExhibitionService exhibitionService;
     @Autowired
     private FileService fileService;
     @Autowired
     private SecurityService securityService;
 
     @ResponseStatus(HttpStatus.OK)
     @RequestMapping(value = "put", method = RequestMethod.POST)
     public void saveExhibition(
             String pwd,
             String exKey,
             MultipartFile icon,
             String name,
             String dateFrom,
             String dateTo,
             String address,
             String organizer,
             MultipartFile brief,
             MultipartFile schedule
     ) {
         try {
            if (!securityService.isValidExchangePassword(pwd)) {
                 throw new InvalidValueException("pwd", "****", pwd);
             }
             if (!StringUtils.isAlphanumeric(exKey)) {
                 throw new InvalidValueException("exKey", "[0-9A-Za-z]", exKey);
             }
             Exhibition exhibition = exhibitionService.findByExKey(exKey);
             if (exhibition == null) {
                 exhibition = new Exhibition();
                 exhibition.setExKey(exKey);
             }
             if (StringUtils.isNotEmpty(dateFrom)) {
                 try {
                     exhibition.setDateFrom(new SimpleDateFormat(DATE_FORMAT).parse(dateFrom));
                 } catch (ParseException e) {
                     throw new InvalidValueException("date format",DATE_FORMAT, dateFrom);
                 }
             }
             if (StringUtils.isNotEmpty(dateTo)) {
                 try {
                     exhibition.setDateTo(new SimpleDateFormat(DATE_FORMAT).parse(dateTo));
                 } catch (ParseException e) {
                     throw new InvalidValueException("date format",DATE_FORMAT, dateTo);
                 }
             }
             if (StringUtils.isNotEmpty(name)) {
                 exhibition.setName(name);
             }
             if (StringUtils.isNotEmpty(address)) {
                 exhibition.setAddress(address);
             }
             if (StringUtils.isNotEmpty(organizer)) {
                 exhibition.setOrganizer(organizer);
             }
             if (icon != null) {
                 fileService.save(icon.getInputStream(), exKey + "/icon.png");
             }
             if (brief != null) {
                 fileService.save(brief.getInputStream(), exKey + "/brief.html");
             }
             if (schedule != null) {
                 fileService.save(schedule.getInputStream(), exKey + "/schedule.html");
             }
             exhibitionService.save(exhibition);
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/exhibitions/put", e);
             throw new InternalServerError("/exhibitions/put", e);
         }
     }
 
     @ResponseStatus(HttpStatus.OK)
     @RequestMapping(value = "delete", method = RequestMethod.POST)
     public void deleteExhibition(String pwd, String exKey) {
         try {
             if (!securityService.isValidExchangePassword(pwd)) {
                 throw new InvalidValueException("pwd", "****", pwd);
             }
             exhibitionService.delete(exKey);
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/exhibitions/delete", e);
             throw new InternalServerError("/exhibitions/delete", e);
         }
     }
 
     @RequestMapping(value = "find", method = RequestMethod.GET)
     @ResponseBody
     public NotAppliedExhibitionsWrapper findExhibition(
             String token,
             String size,
             String last,
             String name
     ) {
         try {
             int sizeInt;
             try {
                 sizeInt = Integer.parseInt(size);
             } catch (NumberFormatException e) {
                 throw new InvalidValueException("size", "int", size);
             }
             long lastLong;
             try {
                 lastLong = Long.parseLong(last);
             } catch (NumberFormatException e) {
                 throw new InvalidValueException("last", "long", size);
             }
             NotAppliedExhibitionsWrapper wrapper = new NotAppliedExhibitionsWrapper();
             wrapper.setName(name);
             wrapper.setLast(lastLong);
             List<Exhibition> exhibitions = exhibitionService.findNotAppliedExhibitions(token, sizeInt, lastLong, name);
             for (Exhibition exhibition : exhibitions) {
                 wrapper.addExhibition(new NotAppliedExhibition(exhibition));
             }
             return wrapper;
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/exhibitions/find", e);
             throw new InternalServerError("/exhibitions/find", e);
         }
     }
 
     @RequestMapping(value = "find_applied", method = RequestMethod.GET)
     @ResponseBody
     public List<AppliedExhibition> findAppliedExhibition(String token) {
         try {
             if (StringUtils.isEmpty(token)) {
                 throw new InvalidValueException("token", "token", token);
             }
             List<Applicant> applicants = exhibitionService.findApplicants(token);
             List<AppliedExhibition> list = new ArrayList<>(applicants.size());
             for (Applicant applicant : applicants) {
                 if (applicant.getExhibition() != null) {
                     list.add(new AppliedExhibition(applicant));
                 }
             }
             return list;
         } catch (ValidationException e) {
             throw e;
         } catch (Exception e) {
             logger.error("/exhibitions/find", e);
             throw new InternalServerError("/exhibitions/find", e);
         }
     }
 
     public static class NotAppliedExhibitionsWrapper {
         private String name;
         private long last;
         private List<NotAppliedExhibition> list = new LinkedList<>();
 
         public void addExhibition(NotAppliedExhibition exhibition) {
             list.add(exhibition);
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public long getLast() {
             return last;
         }
 
         public void setLast(long last) {
             this.last = last;
         }
 
         public List<NotAppliedExhibition> getList() {
             return list;
         }
     }
 
     public static class NotAppliedExhibition {
         private String exKey;
         private String name;
         private String date;
         private String address;
         private String organizer;
         private long createdAt;
 
         public NotAppliedExhibition(Exhibition exhibition) {
             this.exKey = exhibition.getExKey();
             this.name = exhibition.getName();
             this.address = "地址：" + exhibition.getAddress();
             this.organizer = "主办单位：" + exhibition.getOrganizer();
             this.createdAt = exhibition.getCreatedAt().getTime();
             DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
             StringBuilder sb = new StringBuilder(30).append("时间：");
             if (exhibition.getDateFrom() != null) {
                 sb.append(dateFormat.format(exhibition.getDateFrom()));
                 if (exhibition.getDateTo() != null && !exhibition.getDateTo().equals(exhibition.getDateFrom())) {
                     sb.append("---").append(dateFormat.format(exhibition.getDateTo()));
                 }
             } else if (exhibition.getDateTo() != null) {
                 sb.append(dateFormat.format(exhibition.getDateFrom()));
             }
             this.date = sb.toString();
         }
 
         public String getExKey() {
             return exKey;
         }
 
         public String getName() {
             return name;
         }
 
         public String getDate() {
             return date;
         }
 
         public String getAddress() {
             return address;
         }
 
         public String getOrganizer() {
             return organizer;
         }
 
         public long getCreatedAt() {
             return createdAt;
         }
     }
 
     public static class AppliedExhibition {
         private String exKey;
         private String name;
         private String status;
 
         public AppliedExhibition(Applicant applicant) {
             Exhibition exhibition = applicant.getExhibition();
             this.exKey = exhibition.getExKey();
             this.name = exhibition.getName();
             this.status = applicant.getApplyStatus().getCode();
         }
 
         public String getExKey() {
             return exKey;
         }
 
         public void setExKey(String exKey) {
             this.exKey = exKey;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getStatus() {
             return status;
         }
 
         public void setStatus(String status) {
             this.status = status;
         }
     }
 }
