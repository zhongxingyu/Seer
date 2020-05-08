 package edu.hiro.converter.excel;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import edu.hiro.converter.ImportHelper;
 import edu.hiro.util.AbstractEntity;
 import edu.hiro.util.DateHelper;
 import edu.hiro.util.ExcelHelper;
 import edu.hiro.util.FileHelper;
 import edu.hiro.util.StringHelper;
 
 public abstract class AbstractBloodTestLoader
 {	
 	protected final int FIRST_COL;//=1;
 	protected final int LAST_COL;//=41;
 	protected final int HEADER_ROW;
 	protected static final String filepattern=".*[0-9]+-[0-9]+\\.xlsx";
 	protected static final String sheetpattern="[0-9]+";
 	
 	protected ExcelHelper helper=new ExcelHelper();	
 	protected final Map<String,String> conversions=Maps.newHashMap();
 	protected final ImportHelper importhelper=new ImportHelper();
 	
 	public AbstractBloodTestLoader(int first_col, int last_col, int header_row)
 	{
 		this.FIRST_COL=first_col;
 		this.LAST_COL=last_col;
 		this.HEADER_ROW=header_row;
 	}
 	
 	public void loadFolder(String folder)
 	{
 		for (String filename : FileHelper.listFiles(folder,".xlsx",true))
 		{
 			if (!filenameMatches(filename))
 				continue;
 			try
 			{
 				loadFile(filename);
 			}
 			catch (Exception e)
 			{
 				System.err.println("error opening file "+filename+": "+e);
 			}
 		}
 	}
 	
 	public void loadFile(String filename)
 	{
 		System.out.println("loading file: "+filename);
 		Workbook workbook=helper.openSpreadsheet(filename);
 		for (int index=0;index<workbook.getNumberOfSheets();index++)
 		{
 			Sheet sheet=workbook.getSheetAt(index);
 			if (sheetMatches(sheet))
 				loadSheet(sheet);
 		}
 	}
 	
 	public void loadSheet(Sheet sheet)
 	{
 		String idnum=sheet.getSheetName();
 		List<String> fields=getFields(sheet);
 		for (int rownum=HEADER_ROW+1; rownum<=sheet.getLastRowNum(); rownum++)
 		{
 			try
 			{
 				loadRow(sheet,fields,rownum,idnum);
 			}
 			catch (Exception e)
 			{
 				System.err.println("error in row, skipping rest: "+e);
 				return;
 			}
 		}
 	}
 	
 	protected abstract AbstractEntity createEntity();
 	
 	protected abstract void save(AbstractEntity bloodtest);
 	
 	private void loadRow(Sheet sheet, List<String> fields, int rownum, String idnum)
 	{
 		List<String> values=getValues(sheet,rownum);
 		//StringHelper.println("values"+StringHelper.join(values), Charsets.UTF_16);
 		AbstractEntity bloodtest=createEntity();
 		importhelper.setProperty(bloodtest,"idnum",idnum);
 		for (int index=0;index<fields.size();index++)
 		{
 			String field=fields.get(index);
 			String value=values.get(index);
 			if (field.equals("日付"))
 			{
 				if (value.trim().equals(""))
					return;
 				value=fixDate(value);
 			}
 			//StringHelper.println("set "+field+"="+value);
 			importhelper.setProperty(bloodtest,field,value);
 		}
 		save(bloodtest);
 		//System.out.println("bloodtest="+bloodtest.toString());
 	}
 	
 	protected String fixDate(String value)
 	{
 		try
 		{
 			Date date=DateHelper.parse(value,DateHelper.EXCEL_PATTERN);			
 			return DateHelper.format(date,DateHelper.YYYYMMDD_PATTERN);
 		}
 		catch (Exception e)
 		{
 			return value;
 		}
 	}
 	
 	private List<String> getFields(Sheet sheet)
 	{
 		List<String> fields=getValues(sheet,HEADER_ROW);
 		for (int index=0;index<fields.size();index++)
 		{
 			String field=fields.get(index);
 			field=ImportHelper.adjustFieldName(field,conversions);
 			field=StringHelper.replace(field,"-","");
 			if (field.endsWith("gtp"))
 				field="ggtp";
 			fields.set(index,field);
 		}
 		//StringHelper.println("fields: "+StringHelper.join(fields), Charsets.UTF_16);
 		return fields;
 	}
 	
 	private List<String> getValues(Sheet sheet, int rownum)
 	{
 		List<String> values=Lists.newArrayList();
 		for (int col=FIRST_COL; col<=LAST_COL; col++)
 		{
 			Object value=helper.getCellValue(sheet,rownum,col);
 			if (value==null)
 				value="";
 			values.add(value.toString());
 		}
 		return values;
 	}
 	
 	private boolean filenameMatches(String path)
 	{
 		String filename=FileHelper.stripPath(path);
 		boolean matches=filename.matches(filepattern);
 		if (!matches)
 			System.out.println("filename "+filename+" does not match pattern "+filepattern+". skipping. ("+path+")");
 		return matches;
 	}	
 
 	private boolean sheetMatches(Sheet sheet)
 	{
 		String name=sheet.getSheetName();
 		if (name.equals("Sheet1"))
 			return false;
 		boolean matches=name.matches(sheetpattern);
 		if (!matches)
 			System.out.println("sheet "+name+" does not match pattern "+sheetpattern+". skipping.");
 		return matches;
 	}
 }
