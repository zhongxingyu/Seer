 package com.xaf.report.column;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.sql.*;
 import java.text.*;
 
 import com.xaf.report.*;
 
 public class DecimalColumn extends NumericColumn
 {
     public DecimalColumn()
     {
         setAlignStyle(ALIGN_RIGHT);
         setFormat("decimal");
     }
 
	public String getFormattedData(ReportContext rc, long rowNum, Object[] rowData, boolean doCalc)
 	{
 		int colIndex = getColIndexInArray();
         Object oData = rowData[colIndex];
         String data = "";
         double value = 0;
         if(oData != null)
         {
             value = ((Number) oData).doubleValue();
			NumberFormat fmt = (NumberFormat) getFormatter();
			data = fmt == null ? data : fmt.format(value);
         }
 
         if(doCalc)
         {
             ColumnDataCalculator calc = rc.getCalc(colIndex);
             if(calc != null)
                 calc.addValue(value);
         }
 		return data;
 	}
 }
