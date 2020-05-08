 package com.tp.action.log;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.apache.shiro.authz.annotation.RequiresPermissions;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.google.common.collect.Lists;
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.entity.log.LogContentMarket;
 import com.tp.entity.log.LogCountContent;
 import com.tp.orm.Page;
 import com.tp.orm.PageRequest.Sort;
 import com.tp.orm.PropertyFilter;
 import com.tp.service.LogService;
 import com.tp.utils.ExcelUtils;
 import com.tp.utils.ServletUtils;
 import com.tp.utils.Struts2Utils;
 
 @Namespace("/report")
 public class ReportContentAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 	private Page<LogCountContent> page = new Page<LogCountContent>();
 	private LogService logService;
 	private List<Integer> sliders = Lists.newArrayList();
 
 	private Map<String, CellStyle> styles;
 	private int rowIndex = 0;
 
     @RequiresPermissions("report_content:view")
 	public String execute() throws Exception{
 		List<PropertyFilter> filters = PropertyFilter.buildFromHttpRequest(Struts2Utils.getRequest());
 		if (!page.isOrderBySetted()) {
			page.setOrderBy("createTime,totalVisit");
 			page.setOrderDir(Sort.DESC+","+Sort.DESC);
 		}
 		page = logService.searchLogCountContent(page, filters);
 		sliders = page.getSlider(10);
 		return SUCCESS;
 	}
 
 	public String export() throws Exception {
 		String themeName = new String(Struts2Utils.getParameter("theme").getBytes("iso-8859-1"),"utf-8");
 		String sdate = Struts2Utils.getParameter("date");
 
 		List<LogCountContent> list = logService.getContentByThemeOrDate(themeName, sdate);
 		Workbook wb = exportExcelWorkbook(list, sdate);
 		HttpServletResponse response = Struts2Utils.getResponse();
 		response.setContentType(ServletUtils.EXCEL_TYPE);
 		ServletUtils.setFileDownloadHeader(response, sdate + "内容统计.xls");
 
 		wb.write(response.getOutputStream());
 		response.getOutputStream().flush();
 		return null;
 	}
 
 	private Workbook exportExcelWorkbook(List<LogCountContent> contents, String date) {
 		Workbook wb = new HSSFWorkbook();
 		styles = ExcelUtils.createStyles(wb);
 		if(date==null||date.isEmpty())
 			date="all";
 		Sheet s = wb.createSheet(date);
 
 		s.createFreezePane(0, 2, 0, 2);
 		for (int i = 0; i < 8; i++) {
 			s.autoSizeColumn(i);
 		}
 
 		generateTitle(s, date);
 		generateHeader(s);
 		generateContent(s, contents);
 		return wb;
 	}
 
 	private void generateTitle(Sheet s, String date) {
 		Row r = s.createRow(rowIndex++);
 		Cell cl = r.createCell(0);
 		cl.setCellValue(date + "内容日报");
 		cl.setCellStyle(styles.get("header"));
 		s.addMergedRegion(CellRangeAddress.valueOf("$A$1:$F$1"));
 	}
 
 	private void generateHeader(Sheet s) {
 		Row r = s.createRow(rowIndex++);
 		CellStyle headerStyle = styles.get("header");
 		String[] headers = { "序号", "内容标题", "访问总量", "广告访问量", "商店访问量", "下载总量"};
 		for (int i = 0; i < headers.length; i++) {
 			Cell cl = r.createCell(i);
 			cl.setCellValue(headers[i]);
 			cl.setCellStyle(headerStyle);
 		}
 //		s.addMergedRegion(CellRangeAddress.valueOf("$G$2:$H$2"));
 	}
 
 	private void generateContent(Sheet s, List<LogCountContent> contents) {
 //		int stCell = 3;
 		int contentSize = 0;
 		for (LogCountContent content : contents) {
 			contentSize++;
 //			int size = content.getDownByPerMarket().size();
 //			if (size == 0) {
 				createRowWithNoMarket(content, s, contentSize);
 //				stCell++;
 //			} else {
 //				createRowWithMarket(content, s, stCell, size, contentSize);
 //				stCell += size;
 //			}
 
 		}
 	}
 
 	private Row createRowWithNoMarket(LogCountContent content, Sheet s, int count) {
 		Row r = s.createRow(rowIndex++);
 		Cell c0 = r.createCell(0);
 		c0.setCellValue(count);
 		Cell c1 = r.createCell(1);
 		c1.setCellValue(content.getThemeName());
 		Cell c2 = r.createCell(2);
 		c2.setCellValue(content.getTotalVisit());
 		Cell c3 = r.createCell(3);
 		c3.setCellValue(content.getVisitByAd());
 		Cell c4 = r.createCell(4);
 		c4.setCellValue(content.getVisitByStore());
 		Cell c5 = r.createCell(5);
 		c5.setCellValue(content.getTotalDown());
 //		Cell c8 = r.createCell(8);
 //		c8.setCellValue(content.getDownByStore());
 		return r;
 	}
 
 //	private void createRowWithMarket(LogCountContent content, Sheet s, int stCell, int size, int count) {
 //		for (LogContentMarket m : content.getDownByPerMarket()) {
 //			Row r = createRowWithNoMarket(content, s, count);
 //
 //			Cell c6 = r.createCell(6);
 //			c6.setCellValue(m.getMarketName());
 //			Cell c7 = r.createCell(7);
 //			c7.setCellValue(m.getTotalDown());
 //
 //		}
 //		s.addMergedRegion(CellRangeAddress.valueOf("$A$" + stCell + ":$A$" + (stCell + size - 1)));
 //		s.addMergedRegion(CellRangeAddress.valueOf("$B$" + stCell + ":$B$" + (stCell + size - 1)));
 //		s.addMergedRegion(CellRangeAddress.valueOf("$C$" + stCell + ":$C$" + (stCell + size - 1)));
 //		s.addMergedRegion(CellRangeAddress.valueOf("$D$" + stCell + ":$D$" + (stCell + size - 1)));
 //		s.addMergedRegion(CellRangeAddress.valueOf("$E$" + stCell + ":$E$" + (stCell + size - 1)));
 //		s.addMergedRegion(CellRangeAddress.valueOf("$F$" + stCell + ":$F$" + (stCell + size - 1)));
 //		s.addMergedRegion(CellRangeAddress.valueOf("$I$" + stCell + ":$I$" + (stCell + size - 1)));
 //
 //	}
 
 	@Autowired
 	public void setLogService(LogService logService) {
 		this.logService = logService;
 	}
 
 	public Page<LogCountContent> getPage() {
 		return page;
 	}
 
 	public List<Integer> getSliders() {
 		return sliders;
 	}
 }
