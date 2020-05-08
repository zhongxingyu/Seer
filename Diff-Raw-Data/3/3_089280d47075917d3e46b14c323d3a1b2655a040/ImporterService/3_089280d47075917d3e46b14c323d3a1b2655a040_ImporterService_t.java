 /**
  * Copyright (c) 2011, Clinton Health Access Initiative.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.chai.kevin.importer;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.chai.kevin.LocationService;
 import org.chai.kevin.data.DataService;
 import org.chai.kevin.data.Enum;
 import org.chai.kevin.data.EnumOption;
 import org.chai.kevin.data.RawDataElement;
 import org.chai.kevin.data.Type;
 import org.chai.kevin.data.Type.Sanitizer;
 import org.chai.kevin.location.DataLocationEntity;
 import org.chai.kevin.util.Utils;
 import org.chai.kevin.value.RawDataElementValue;
 import org.chai.kevin.value.Value;
 import org.chai.kevin.value.ValueService;
 import org.hisp.dhis.period.Period;
 import org.supercsv.io.CsvMapReader;
 import org.supercsv.io.ICsvMapReader;
 import org.supercsv.prefs.CsvPreference;
 
 /**
  * @author Jean Kahigiso M.
  *
  */
 public class ImporterService {
 	
 	private static final Log log = LogFactory.getLog(ImporterService.class);
 	private LocationService locationService;
 	private ValueService valueService;	
 	private DataService dataService;
 	private static final String CODE_HEADER = "code";
 	public void importFile(RawDataElement rawDataElement, Reader reader, Period period,ImporterErrorManager manager) throws IOException{
 		
 		ICsvMapReader readFileAsMap = new CsvMapReader(reader, CsvPreference.EXCEL_PREFERENCE);
 		
 		try{			
 			final String[] headers = readFileAsMap.getCSVHeader(true);
 			String code=null;
 			Map<String,Integer> positions = new HashMap<String,Integer>();
 			Map<String,String> values = readFileAsMap.read(headers);
 			Map<String,Type> types = new HashMap<String,Type>();
 		
 			for (String header : headers)  { 
 				try {
 					if(!header.equals(CODE_HEADER))
 						types.put("[_]."+header,rawDataElement.getType().getType("[_]."+header));
 				} catch(IllegalArgumentException e){
 					if(log.isWarnEnabled()) log.warn("Column type not found for header"+header, e);
 					manager.getErrors().add(new ImporterError(readFileAsMap.getLineNumber(),header,"error.message.unknowm.column.type"));
 				}
 			}
 			Value value = null;
 			DataLocationEntity dataEntity=null;
 			RawDataElementValue rawDataElementValue= null;			
 			manager.setNumberOfSavedRows(0);
 			manager.setNumberOfUnsavedRows(0);
 			manager.setNumberOfRowsSavedWithError(0);
 
 			ImportSanitizer sanitizer = new ImportSanitizer(manager.getErrors(), types);
 			
 			while (values != null) {
 				Map <String,Object> map = new HashMap<String,Object>();
 				Set<String> attributes = new HashSet<String>();
 				sanitizer.setLineNumber(readFileAsMap.getLineNumber());
 				sanitizer.setNumberOfErrorInRows(0);
 				
 				if(!values.get(CODE_HEADER).equals(code)){
 					// The location changes, we need to update the code, location, position, rawDataElementValue
 					// 1 update the code
 					code = values.get(CODE_HEADER);
 					// 2 update the position	
 					if (positions.get(code) == null) positions.put(code, 0);
 					// 3 update the location
 					dataEntity = locationService.findCalculationEntityByCode(code, DataLocationEntity.class);
 					if(dataEntity==null){
 						manager.getErrors().add(new ImporterError(readFileAsMap.getLineNumber(),CODE_HEADER,"error.message.unknown.location"));
 					}else{
 						// 4 update the rawDataElementValue
 						rawDataElementValue = valueService.getDataElementValue(rawDataElement, dataEntity, period);
 						if(rawDataElementValue != null) value = rawDataElementValue.getValue();
 						else{
 							value = new Value("");		
 							rawDataElementValue= new RawDataElementValue(rawDataElement,dataEntity,period,value);
 						}
 					}
 				}
 				
 				for (String header : headers){
 					if (!header.equals(CODE_HEADER)){
 						map.put("[" + positions.get(code) + "]."+ header, values.get(header));
 					}		
 				}
 					
 				map.put("", getLineNumberString(positions.get(code)));
 				positions.put(code, positions.get(code) + 1);
 				
 				if (dataEntity == null)
 					manager.incrementNumberOfUnsavedRows();
 				else {
 					
 					if(log.isDebugEnabled()) log.debug("Marging with data from map of header and data "+map+" Value before marge"+value);
 					value = rawDataElement.getType().mergeValueFromMap(value, map, "",attributes, sanitizer);
 					if(log.isDebugEnabled()) log.debug("Value after marge "+value);	
 					rawDataElementValue.setValue(value);
 					valueService.save(rawDataElementValue);
					if(sanitizer.getNumberOfErrorInRows()>0)
						manager.incrementNumberOfRowsSavedWithError(1);
 					manager.incrementNumberOfSavedRows();
 				}
 				values = readFileAsMap.read(headers);				
 			}
 					
 		}catch(IOException ioe){
 			// TODO Please through something meaningful
 			throw ioe;
 		}finally {
 			readFileAsMap.close();
 		}
 		
 	}
 	
 	private class ImportSanitizer implements Sanitizer {
 		
 		private final List<ImporterError> errors;
 		private final Map<String,Type> types;
 		
 		public ImportSanitizer(List<ImporterError> errors, Map<String,Type> types) {
 			this.errors = errors;
 			this.types = types;
 		}
 		
 		private Integer lineNumber;
 		private Integer numberOfErrorInRows;
 		
 		public void setLineNumber(Integer lineNumber) {
 			this.lineNumber = lineNumber;
 		}
 		
 		public Integer getNumberOfErrorInRows() {
 			return numberOfErrorInRows;
 		}
 		
 		public void setNumberOfErrorInRows(Integer numberOfErrorInRows) {
 			this.numberOfErrorInRows = numberOfErrorInRows;
 		}
 	
 		@Override
 		public Object sanitizeValue(Object value, Type type, String prefix,String genericPrefix) {
 			switch (type.getType()) {
 			case ENUM:
 				return validateImportEnum(genericPrefix, value);
 			case BOOL:
 				return validateImportBool(genericPrefix, value);
 			case NUMBER:
 				return validateImportNumber(genericPrefix, value);
 			case TEXT:
 				return validateImportString(genericPrefix, value);
 			case STRING:
 				return validateImportString(genericPrefix, value);
 			case DATE:
 				return validateImportDate(genericPrefix, value);
 			default:
 				errors.add(new ImporterError(lineNumber, prefix, "error.message.unknown.type")); 
 				return null;
 			}
 		}
 	
 		private String validateImportEnum(String header, Object value) {
 			Enum enumValue = new Enum();
 			List<EnumOption> enumOptions = new ArrayList<EnumOption>();
 			enumValue = dataService.findEnumByCode(types.get(header).getEnumCode());
 			if (enumValue != null) {
 				enumOptions = enumValue.getEnumOptions();
 				for (EnumOption enumOption : enumOptions)
 					if (enumOption.getValue().equals(value))
 						return enumOption.getValue();
 						
 			}
 			this.setNumberOfErrorInRows(this.getNumberOfErrorInRows()+1);
 			errors.add(new ImporterError(lineNumber, header,"error.message.enume"));
 			return null;
 		}
 
 		private Boolean validateImportBool(String header, Object value){
 			if (((String) value).equals("0") || ((String) value).equals("1"))
 				if (((String) value).equals("1"))
 					return true;
 				else
 					return false;
 			this.setNumberOfErrorInRows(this.getNumberOfErrorInRows() + 1);
 			errors.add(new ImporterError(lineNumber, header,
 					"error.message.boolean"));
 			return null;
 		}
 		
 		private Number validateImportNumber(String header, Object value) {
 			try {
 				return Double.parseDouble((String) value);
 			} catch (NumberFormatException e) {
 				if (log.isDebugEnabled()) log.debug("Value in this cell [Line: " + lineNumber+ ",Column: " + header + "] has to be a Number"+ value, e);
 			}
 			this.setNumberOfErrorInRows(this.getNumberOfErrorInRows()+1);
 			errors.add(new ImporterError(lineNumber, header,"error.message.number"));
 			return null;
 		}
 		
 		private String validateImportString(String header, Object value){
 			if(value instanceof String || value.equals(""))
 				return (String) value;
 			this.setNumberOfErrorInRows(this.getNumberOfErrorInRows()+1);
 			errors.add(new ImporterError(lineNumber, header, "error.message.string.text")); 
 			return null;
 		}
 		
 		private Date validateImportDate(String header, Object value){
 			if(value instanceof String)
 				try {
 					return Utils.parseDate((String)value);
 				} catch (ParseException e) {
 					if (log.isDebugEnabled()) log.debug("Value in this cell [Line: " + lineNumber+ ",Column: " + header + "] has to be a Date (dd-MM-yyyy)"+ value, e);
 				}
 			this.setNumberOfErrorInRows(this.getNumberOfErrorInRows()+1);
 			errors.add(new ImporterError(lineNumber, header, "error.message.date")); 
 			return null;
 		}
 		
 	}
 	
 	public void setLocationService(LocationService locationService) {
 		this.locationService = locationService;
 	}
 
 
 	public void setValueService(ValueService valueService) {
 		this.valueService = valueService;
 	}
 	
 	public void setDataService(DataService dataService) {
 		this.dataService = dataService;
 	}
 
 
 
 	private static List<String> getLineNumberString(Integer lineNumber) {
 		List<String> result = new ArrayList<String>();
 		for (int i = 0; i <= lineNumber; i++) {
 			result.add("["+i+"]");
 		}
 		return result;
 	}
 
 }
