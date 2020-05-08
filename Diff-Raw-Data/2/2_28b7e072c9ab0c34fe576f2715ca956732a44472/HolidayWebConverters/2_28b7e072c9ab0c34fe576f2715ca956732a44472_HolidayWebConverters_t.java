 package com.mwiesner.holiday32.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.core.convert.converter.Converter;
 import org.springframework.stereotype.Component;
 
 import com.mwiesner.holiday32.domain.Employee;
 import com.mwiesner.holiday32.domain.HolidayRequest;
 import com.mwiesner.holiday32.service.EmployeeService;
 import com.mwiesner.holiday32.service.HolidayRequestService;
 
 
 /**
  * A central place to register application converters and formatters. 
  */
 @Component
 public class HolidayWebConverters {
 
 
 	@Autowired
     EmployeeService employeeService;
 
 	@Autowired
     HolidayRequestService holidayRequestService;
 
 	public Converter<Employee, String> getEmployeeToStringConverter() {
         return new org.springframework.core.convert.converter.Converter<com.mwiesner.holiday32.domain.Employee, java.lang.String>() {
             public String convert(Employee employee) {
                return new StringBuilder().append(employee.getFirstName()).append(' ').append(employee.getLastName()).toString();
             }
         };
     }
 
 	public Converter<Long, Employee> getIdToEmployeeConverter() {
         return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.mwiesner.holiday32.domain.Employee>() {
             public com.mwiesner.holiday32.domain.Employee convert(java.lang.Long id) {
                 return employeeService.findEmployee(id);
             }
         };
     }
 
 	public Converter<String, Employee> getStringToEmployeeConverter(final ConversionService conversionService) {
         return new org.springframework.core.convert.converter.Converter<java.lang.String, com.mwiesner.holiday32.domain.Employee>() {
             public com.mwiesner.holiday32.domain.Employee convert(String id) {
                 return conversionService.convert(conversionService.convert(id, Long.class), Employee.class);
             }
         };
     }
 
 	public Converter<HolidayRequest, String> getHolidayRequestToStringConverter() {
         return new org.springframework.core.convert.converter.Converter<com.mwiesner.holiday32.domain.HolidayRequest, java.lang.String>() {
             public String convert(HolidayRequest holidayRequest) {
                 return new StringBuilder().append(holidayRequest.getFromDate()).append(' ').append(holidayRequest.getToDate()).append(' ').append(holidayRequest.getComment()).toString();
             }
         };
     }
 
 	public Converter<Long, HolidayRequest> getIdToHolidayRequestConverter() {
         return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.mwiesner.holiday32.domain.HolidayRequest>() {
             public com.mwiesner.holiday32.domain.HolidayRequest convert(java.lang.Long id) {
                 return holidayRequestService.findHolidayRequest(id);
             }
         };
     }
 
 	public Converter<String, HolidayRequest> getStringToHolidayRequestConverter(final ConversionService conversionService) {
         return new org.springframework.core.convert.converter.Converter<java.lang.String, com.mwiesner.holiday32.domain.HolidayRequest>() {
             public com.mwiesner.holiday32.domain.HolidayRequest convert(String id) {
                 return conversionService.convert(conversionService.convert(id, Long.class), HolidayRequest.class);
             }
         };
     }
 
 
 }
