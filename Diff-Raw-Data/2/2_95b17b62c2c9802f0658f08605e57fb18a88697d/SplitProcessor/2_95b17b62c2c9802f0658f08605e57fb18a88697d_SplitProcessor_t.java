 package de.dmc.loggi.processors.impl;
 
 import de.dmc.loggi.exceptions.ConfigurationException;
 import de.dmc.loggi.model.Column;
 import de.dmc.loggi.processors.AbstractColumnProcessor;
 import de.dmc.loggi.processors.AttributeDef;
 import de.dmc.loggi.processors.MetaInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author CptSpaetzle
  */
 @MetaInfo(
         description = "Record string is to be split into columns, requested index column value is returned",
         attributes = {
                @AttributeDef(name = SplitProcessor.ATTR_SEPARATOR, description = "separator regexp to split record string with", defaultValue = "\\|"),
                 @AttributeDef(name = SplitProcessor.ATTR_COLUMN, description = "column index to return as a column value (zero based)", defaultValue = "0")
         }
 )
 public class SplitProcessor extends AbstractColumnProcessor {
 
     Logger logger = LoggerFactory.getLogger(this.getClass());
 
     public static final String ATTR_SEPARATOR = "separator";
     public static final String ATTR_COLUMN = "column";
 
     public SplitProcessor(Column column) throws ConfigurationException {
         super(column);
     }
 
     @Override
     public String getColumnValue(String record) {
 
         String[] columns = record.split(this.<String>getAttributeValue(ATTR_SEPARATOR));
 
         int columnIndex = Integer.valueOf(this.<String>getAttributeValue(ATTR_COLUMN));
         if(columnIndex < columns.length){
             return columns[columnIndex];
         }
 
         logger.error("Column #{} not found. Column name:{}. Returning default value", columnIndex, getColumn().getName());
         return getColumn().getDefaultValue();
     }
 }
