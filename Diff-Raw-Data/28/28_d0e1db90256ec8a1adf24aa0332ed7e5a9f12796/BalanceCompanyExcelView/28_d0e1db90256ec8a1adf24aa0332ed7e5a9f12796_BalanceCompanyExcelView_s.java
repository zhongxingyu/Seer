 package com.bsg.pcms.excel.view;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jxl.format.Alignment;
 import jxl.format.Border;
 import jxl.format.BorderLineStyle;
 import jxl.format.Colour;
 import jxl.format.VerticalAlignment;
 import jxl.write.Label;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.view.document.AbstractJExcelView;
 
 import com.bsg.pcms.balance.dto.BalanceDTOEx;
 
 
 /**
  * 브라우저 에서 Excel파일로 다운로드 받을 수 있게 해주는 뷰 클래스 이다.
  */
 @Component
 public class BalanceCompanyExcelView extends AbstractJExcelView {
 
 	Logger logger = LoggerFactory.getLogger(getClass());
 	
 	int BALANCE_MONTH = 0;
 	int SALE_COMPANY = 1;
 	int CONTRACT_CONDITION = 2;
 	int TOTAL_PRICE = 3;
 	int FEE = 4;
 	int REG_DT = 5;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void buildExcelDocument(Map model, WritableWorkbook myWorkbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		try {
 			logger.info("<SaleContractExcelView> EXCEL file making start!");
 			
 			String fileName = createExcelFileName("BalanceCompany");
 			setFileNameToResponse(request, response, fileName);
 
 			//부제 셋팅
 			WritableCellFormat subtitleFormat = new WritableCellFormat(); 
 			subtitleFormat.setAlignment(Alignment.CENTRE); 
 			subtitleFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
 			subtitleFormat.setBackground(Colour.GREY_25_PERCENT);
 			subtitleFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
 				
 			//데이터 셋팅
 			WritableCellFormat dataFormat = new WritableCellFormat(); 
 			//dataFormat.setAlignment(Alignment.RIGHT); 
 																		
 			WritableSheet sheet = myWorkbook.createSheet("BalanceCompany", 0); 
 
 			// setCloumnView(몇번째 컬럼, 넓이)
 			sheet.setColumnView(BALANCE_MONTH, 15);
 			sheet.setColumnView(SALE_COMPANY, 15);
 			sheet.setColumnView(CONTRACT_CONDITION, 15);
 			sheet.setColumnView(TOTAL_PRICE, 30);
 			sheet.setColumnView(FEE, 13);
 			sheet.setColumnView(REG_DT, 13);
 
 			sheet.addCell(new Label(BALANCE_MONTH, 0, "계약코드", subtitleFormat));
 			sheet.addCell(new Label(SALE_COMPANY, 0, "판매형태", subtitleFormat)); 
 			sheet.addCell(new Label(CONTRACT_CONDITION, 0, "판매처", subtitleFormat)); 
 			sheet.addCell(new Label(TOTAL_PRICE, 0, "상품명", subtitleFormat)); 
 			sheet.addCell(new Label(FEE, 0, "계약일", subtitleFormat)); 
 			sheet.addCell(new Label(REG_DT, 0, "계약일", subtitleFormat)); 
 
 			List<BalanceDTOEx> balanceList = (List<BalanceDTOEx>)model.get("balanceList");
 			logger.info("size {}", balanceList.size());
 			int row = 1;
 			for(BalanceDTOEx bde : balanceList) {
 				sheet.addCell(new Label(BALANCE_MONTH, row, bde.getSale_str_date() + "", dataFormat));
 				sheet.addCell(new Label(SALE_COMPANY, row, bde.getCompany_name(), dataFormat)); 
 				sheet.addCell(new Label(CONTRACT_CONDITION, row, bde.getContract_type(), dataFormat)); 
				sheet.addCell(new Label(TOTAL_PRICE, row, bde.getTotalMoney(), dataFormat)); 
				sheet.addCell(new Label(FEE, row, bde.getCpMoney(), dataFormat)); 
 				sheet.addCell(new Label(REG_DT, row, bde.getReg_dt().toString(), dataFormat)); 
 				row++;
 			}
 
 			logger.info("<SaleContractExcelView> EXCEL file making finished!");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void setFileNameToResponse(HttpServletRequest request, HttpServletResponse response, String fileName) {
 		String userAgent = request.getHeader("User-Agent");
 		if (userAgent.indexOf("MSIE 5.5") >= 0) {
 			response.setContentType("doesn/matter");
 			response.setHeader("Content-Disposition", "filename=\"" + fileName + "\"");
 		} else {
 			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
 		}
 	}
 
 	/** 파라미터로 받은 파일이름 뒤에 날짜를 더해 돌려준다. 
 	 * 예) Contents_20130724.xls
 	 * @param fileName
 	 * @return fileName_yyyyMMdd 
 	 */
 	public String createExcelFileName(String fileName) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 		String excelFileName = String.format("%s_%s.xls", fileName, sdf.format(new Date()) );
 		return excelFileName;
 	}
 
 }
