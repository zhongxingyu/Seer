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
 public class BalanceSaleExcelView extends AbstractJExcelView {
 
 	Logger logger = LoggerFactory.getLogger(getClass());
 	
 	int BALANCE_MONTH = 0;
 	int SALE_COMPANY = 1;
 	int PRODUCT = 2;
 	int SALE_COUNT = 3;
 	int TOTAL_PRICE = 4;
 	int SALE_COMPANY_FEE = 5;
 	int CP_FEE = 6;
 	int GROSS = 7;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void buildExcelDocument(Map model, WritableWorkbook myWorkbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
 
 		try {
 			logger.info("<SaleContractExcelView> EXCEL file making start!");
 			
 			String fileName = createExcelFileName("BalanceSale");
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
 																		
 			WritableSheet sheet = myWorkbook.createSheet("BalanceSale", 0); 
 
 			// setCloumnView(몇번째 컬럼, 넓이)
 			sheet.setColumnView(BALANCE_MONTH, 20);
 			sheet.setColumnView(SALE_COMPANY, 15);
 			sheet.setColumnView(PRODUCT, 30);
 			sheet.setColumnView(SALE_COUNT, 15);
 			sheet.setColumnView(TOTAL_PRICE, 20);
 			sheet.setColumnView(SALE_COMPANY_FEE, 20);
 			sheet.setColumnView(CP_FEE, 20);
 			sheet.setColumnView(GROSS, 20);
 
 			sheet.addCell(new Label(BALANCE_MONTH, 0, "정산월", subtitleFormat));
 			sheet.addCell(new Label(SALE_COMPANY, 0, "판매처", subtitleFormat)); 
 			sheet.addCell(new Label(PRODUCT, 0, "상품", subtitleFormat)); 
 			sheet.addCell(new Label(SALE_COUNT, 0, "판매횟수", subtitleFormat)); 
 			sheet.addCell(new Label(TOTAL_PRICE, 0, "총매출금액", subtitleFormat)); 
 			sheet.addCell(new Label(SALE_COMPANY_FEE, 0, "판매업체수수료", subtitleFormat)); 
 			sheet.addCell(new Label(CP_FEE, 0, "업체수수료", subtitleFormat)); 
 			sheet.addCell(new Label(GROSS, 0, "자사수익", subtitleFormat)); 
 
 			List<BalanceDTOEx> balanceList = (List<BalanceDTOEx>)model.get("balanceList");
 			logger.info("size {}", balanceList.size());
 			int row = 1;
 			for(BalanceDTOEx bde : balanceList) {
 				sheet.addCell(new Label(BALANCE_MONTH, row, bde.getSale_str_date() + "", dataFormat));
 				sheet.addCell(new Label(SALE_COMPANY, row, bde.getCompany_name(), dataFormat)); 
 				sheet.addCell(new Label(PRODUCT, row, bde.getContents_name(), dataFormat)); 
				sheet.addCell(new Label(SALE_COUNT, row, bde.getTotalMoney() + "", dataFormat)); 
				sheet.addCell(new Label(TOTAL_PRICE, row, bde.getSaleMoney(), dataFormat)); 
				sheet.addCell(new Label(SALE_COMPANY_FEE, row, bde.getSaleMoney(), dataFormat)); 
				sheet.addCell(new Label(CP_FEE, row, bde.getCpMoney(), dataFormat)); 
				sheet.addCell(new Label(GROSS, row, bde.getOwnerMoney(), dataFormat)); 
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
