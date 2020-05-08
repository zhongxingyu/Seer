 package ru.AenSidhe.jefit.exporter.xlsx;
 
 import java.io.*;
 import java.util.*;
 
 import org.apache.poi.ss.usermodel.*;
 import org.apache.poi.hssf.usermodel.*;
 import org.joda.time.*;
 import android.content.*;
 
 public class ExcelFormatter
 {
 	public String CreateExcel(Iterator<Set> sets, Context context) throws IOException
 	{
 		Workbook wb = new HSSFWorkbook();
		String fileName = String.format("jefit_%s.xls", _now.toString("yyyyMMdd_HHmmss"));
 		File file = new File(context.getExternalFilesDir(null), fileName);
 		FileOutputStream fileOut = new FileOutputStream(file);
 		wb.write(fileOut);
 		fileOut.close();
 		return file.getAbsolutePath() + "\\" + fileName;
 	}
 
 	private final DateTime _now = new DateTime();
 }
