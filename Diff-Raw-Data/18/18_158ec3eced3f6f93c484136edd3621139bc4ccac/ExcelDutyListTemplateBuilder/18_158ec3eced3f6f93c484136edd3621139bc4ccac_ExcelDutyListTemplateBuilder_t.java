 package war.webapp.service.impl.builder;
 
 import jxl.SheetSettings;
 import jxl.Workbook;
 import jxl.WorkbookSettings;
 import jxl.format.Alignment;
 import jxl.format.Border;
 import jxl.format.BorderLineStyle;
 import jxl.format.Colour;
 import jxl.format.*;
 import jxl.format.VerticalAlignment;
 import jxl.write.*;
 import jxl.write.WritableFont.FontName;
 import jxl.write.biff.RowsExceededException;
 import war.webapp.model.DayDuty;
 import war.webapp.service.builder.BaseDutyListTemplateBuilder;
 
 import java.io.*;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ExcelDutyListTemplateBuilder extends BaseDutyListTemplateBuilder {
 
     private Map<String, FontName> fontNames;
     private Map<String, Object> boldStyles;
 
     @SuppressWarnings("unused")
     private void initialCellFormats() {
 
     }
 
     @SuppressWarnings("unused")
     private void initialsFontNamesAndStyles() {
         fontNames = new HashMap<String, WritableFont.FontName>();
         boldStyles = new HashMap<String, Object>();
         fontNames.put(FONT_NAME_TIMES, WritableFont.TIMES);
         fontNames.put(FONT_NAME_ARIAL, WritableFont.ARIAL);
         boldStyles.put(FONT_STYLE_BOLD, WritableFont.BOLD);
         boldStyles.put(FONT_STYLE_NO_BOLD, WritableFont.NO_BOLD);
     }
 
     private String filename = "temp.xsl";
 
     @Override
     public void createHeader(Object... params) {
 
         WritableSheet sheet = (WritableSheet) params[0];
 
         String month = (String) params[1];
         String floor = (String) params[2];
 
         WritableFont times_16ptBold = new WritableFont(WritableFont.TIMES, 16, WritableFont.BOLD);
 
         WritableFont times_16ptNoBold = new WritableFont(WritableFont.TIMES, 16, WritableFont.NO_BOLD);
 
         WritableFont times_14ptNoBold = new WritableFont(WritableFont.TIMES, 14, WritableFont.NO_BOLD);
 
         WritableFont times_14ptBold = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD);
 
         WritableCellFormat times_16FormatBold = new WritableCellFormat(times_16ptBold);
         WritableCellFormat times_16FormatNoBold = new WritableCellFormat(times_16ptNoBold);
 
         WritableCellFormat times_14FormatNoBold = new WritableCellFormat(times_14ptNoBold);
         WritableCellFormat times_14FormatBold = new WritableCellFormat(times_14ptBold);
         try {
             times_16FormatBold.setAlignment(Alignment.CENTRE);
             times_16FormatBold.setWrap(false);
 
             times_16FormatNoBold.setAlignment(Alignment.CENTRE);
             times_16FormatNoBold.setWrap(false);
 
             times_14FormatNoBold.setAlignment(Alignment.LEFT);
             times_14FormatNoBold.setWrap(false);
 
             times_14FormatBold.setAlignment(Alignment.LEFT);
             times_14FormatBold.setWrap(false);
 
             times_16FormatBold.setOrientation(Orientation.HORIZONTAL);
 
             int headerRow = 0;
             int headerColumn = 4;
 
             sheet.mergeCells(headerColumn, headerRow, 6, headerRow);
             sheet.mergeCells(headerColumn, headerRow + 1, 6, headerRow + 1);
             sheet.mergeCells(headerColumn, headerRow + 2, 6, headerRow + 2);
             sheet.mergeCells(headerColumn, headerRow + 3, 6, headerRow + 3);
 
             sheet.setRowView(0, 800);
             sheet.setRowView(4, 0);
 
             int distCol = 1;
             //
             sheet.setColumnView(distCol - 1, 9);// 10
             sheet.setColumnView(distCol, 20);// 20
             sheet.setColumnView(distCol + 1, 10);// 10
             sheet.setColumnView(distCol + 2, 9);// 8
             sheet.setColumnView(distCol + 3, 20);// 22
             sheet.setColumnView(distCol + 4, 10);// 9
             sheet.setColumnView(distCol + 5, 9);// 8
 
             sheet.mergeCells(0, 5, 6, 5);
             sheet.mergeCells(0, 6, 6, 6);
             sheet.mergeCells(0, 7, 6, 7);
 
             sheet.setRowView(9, 250);
 
             addLabelToSheet(sheet, 0, 5, "График дежурств", times_16FormatBold);
             addLabelToSheet(sheet, 0, 6, "по " + floor + " этажу", times_16FormatNoBold);
             addLabelToSheet(sheet, 0, 7, "на " + month + " " + Calendar.getInstance().get(Calendar.YEAR) + " года",
                     times_16FormatNoBold);
 
             sheet.setRowView(8, 0);
             
             addLabelToSheet(sheet, 4, 0, "У Т В Е Р Ж Д А Ю", times_14FormatNoBold);
             addLabelToSheet(sheet, 4, 1, "Заведущая общежития № 1", times_14FormatNoBold);
             addLabelToSheet(sheet, 4, 2, "____________Наумова С.Л.", times_14FormatNoBold);
             addLabelToSheet(sheet, 4, 3, "\"_____\"__________ 2011 год", times_14FormatNoBold);
 
         } catch (WriteException ex) {
             ex.printStackTrace();
         }
     }
 
     @Override
     public void createContent(Object... params) {
         WritableSheet sheet = (WritableSheet) params[0];
         @SuppressWarnings("unchecked")
         List<DayDuty> duties = (List<DayDuty>) params[5];
         String starosta = (String) params[3];
         String vosptatel = (String) params[4];
         WritableFont fontHeader = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD);
         WritableCellFormat headerFormat = new WritableCellFormat(fontHeader);
         try {
             headerFormat.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
             headerFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
             headerFormat.setAlignment(Alignment.CENTRE);
             headerFormat.setWrap(true);
 
             WritableCellFormat headerFormatGrey = new WritableCellFormat(fontHeader);
             headerFormatGrey.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
             headerFormatGrey.setVerticalAlignment(VerticalAlignment.CENTRE);
             headerFormatGrey.setAlignment(Alignment.CENTRE);
             headerFormatGrey.setWrap(true);
             headerFormatGrey.setBackground(Colour.GREY_25_PERCENT);
 
             WritableFont fontData = new WritableFont(WritableFont.TIMES, 14, WritableFont.NO_BOLD);
             WritableFont fontDataBold = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD);
 
             WritableCellFormat dataFormatBorderAll = new WritableCellFormat(fontData);
             dataFormatBorderAll.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
             dataFormatBorderAll.setAlignment(Alignment.CENTRE);
             dataFormatBorderAll.setVerticalAlignment(VerticalAlignment.CENTRE);
             dataFormatBorderAll.setWrap(true);
 
             WritableCellFormat dataFormatBorderAllGrey = new WritableCellFormat(fontData);
             dataFormatBorderAllGrey.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
             dataFormatBorderAllGrey.setAlignment(Alignment.CENTRE);
             dataFormatBorderAllGrey.setVerticalAlignment(VerticalAlignment.CENTRE);
             dataFormatBorderAllGrey.setWrap(true);
             dataFormatBorderAllGrey.setBackground(Colour.GREY_25_PERCENT);
 
             WritableCellFormat dataFormatBorderAllBold = new WritableCellFormat(fontDataBold);
             dataFormatBorderAllBold.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
             dataFormatBorderAllBold.setAlignment(Alignment.CENTRE);
             dataFormatBorderAllBold.setVerticalAlignment(VerticalAlignment.CENTRE);
             dataFormatBorderAllBold.setWrap(true);
             
             WritableCellFormat dataFormatBorderAllBoldGrey = new WritableCellFormat(fontDataBold);
             dataFormatBorderAllBoldGrey.setBorder(Border.ALL, BorderLineStyle.MEDIUM);
             dataFormatBorderAllBoldGrey.setAlignment(Alignment.CENTRE);
             dataFormatBorderAllBoldGrey.setVerticalAlignment(VerticalAlignment.CENTRE);
             dataFormatBorderAllBoldGrey.setWrap(true);
             dataFormatBorderAllBoldGrey.setBackground(Colour.GREY_25_PERCENT);
 
             WritableFont fontPData = new WritableFont(WritableFont.TIMES, 14, WritableFont.NO_BOLD);
             WritableCellFormat pdataFormat = new WritableCellFormat(fontPData);
             WritableCellFormat pdataFormatBold = new WritableCellFormat(fontHeader);
             
             int distCol = 0;
             int distRow = 10;
             sheet.mergeCells(distCol + 1, distRow, distCol + 3, distRow);
             sheet.mergeCells(distCol + 4, distRow, distCol + 6, distRow);
             sheet.mergeCells(distCol, distRow, distCol, distRow + 1);
 
             addLabelToSheet(sheet, distCol + 1, distRow, "I смена (8:00 - 16:00)", headerFormat);
             addLabelToSheet(sheet, distCol + 4, distRow, "II смена (16:00 - 24:00)", headerFormat);
             addLabelToSheet(sheet, distCol, distRow, "Число", headerFormat);
             addLabelToSheet(sheet, distCol + 1, distRow + 1, "ФИО", headerFormat);
             addLabelToSheet(sheet, distCol + 2, distRow + 1, "Группа", headerFormat);
             addLabelToSheet(sheet, distCol + 3, distRow + 1, "Комн.", headerFormat);
             addLabelToSheet(sheet, distCol + 4, distRow + 1, "ФИО", headerFormat);
             addLabelToSheet(sheet,distCol + 5, distRow + 1, "Группа", headerFormat);
             addLabelToSheet(sheet, distCol + 6, distRow + 1, "Комн.", headerFormat);
 
             sheet.setRowView(distRow+1, 350);
             int dataRow = distRow + 2;
             WritableCellFormat currentFormat = null;
             WritableCellFormat currentFormatDay = null;
             WritableCellFormat currentFormatGroup = null;
             for (DayDuty duty : duties) {
 
                 Calendar dutyDate = duty.getDate();
                 int dayOfWeek = duty.getDayOfWeek();
                 if ((dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY)) {
                     currentFormat = dataFormatBorderAllBoldGrey;
                     currentFormatGroup = dataFormatBorderAllBoldGrey;
                     currentFormatDay = dataFormatBorderAllBoldGrey;
                 } else {
                     currentFormat = dataFormatBorderAllBold;
                     currentFormatGroup = dataFormatBorderAllBold;
                     currentFormatDay = dataFormatBorderAllBold;
                 }
                 addLabelToSheet(sheet,distCol, dataRow, "" + dutyDate.get(Calendar.DAY_OF_MONTH), currentFormatDay);

                addLabelToSheet(sheet, distCol + 1, dataRow, duty.getFirstUser().getFullName(), currentFormat);
                 String firstUSerGroup = duty.getFirstUser().getUniversityGroup();
                 addLabelToSheet(sheet, distCol + 2, dataRow, firstUSerGroup == null ? "" : firstUSerGroup, currentFormatGroup);
                 String firstUserRoom = duty.getFirstUser().getAddress().getHostelRoom();
                 addLabelToSheet(sheet, distCol + 3, dataRow,  firstUserRoom == null ? "" : firstUserRoom, currentFormat);

                addLabelToSheet(sheet, distCol+4, dataRow, duty.getSecondUser().getFullName(), currentFormat);
                 String secondUserGroup = duty.getSecondUser().getUniversityGroup();
                 addLabelToSheet(sheet,distCol + 5, dataRow, secondUserGroup == null ? "" : secondUserGroup,
                         currentFormatGroup);
                 String secondUserRoom = duty.getSecondUser().getAddress().getHostelRoom();
                 addLabelToSheet(sheet, distCol + 6, dataRow, secondUserRoom == null ? "" : secondUserRoom,
                         currentFormat);
                 dataRow++;
             }
 
             sheet.setRowView(dataRow, 150);
             sheet.mergeCells(distCol, dataRow + 1, distCol + 2, dataRow + 1);
             sheet.mergeCells(distCol + 4, dataRow + 1, distCol + 6, dataRow + 1);
             sheet.setRowView(dataRow + 2, 140);
 
             sheet.mergeCells(distCol, dataRow + 3, distCol + 2, dataRow + 3);
             sheet.mergeCells(distCol, dataRow + 4, distCol + 2, dataRow + 4);
             sheet.mergeCells(distCol + 4, dataRow + 4, distCol + 6, dataRow + 4);
 
             addLabelToSheet(sheet, distCol, dataRow + 1, "Староста этажа", pdataFormat);
             addLabelToSheet(sheet, distCol + 4, dataRow + 1, "_____________" + starosta, pdataFormat);
             addLabelToSheet(sheet, distCol, dataRow + 3, "Согласовано:", pdataFormatBold);
             addLabelToSheet(sheet, distCol, dataRow + 4, "Воспитатель общежития №1", pdataFormat);
             addLabelToSheet(sheet, distCol + 4, dataRow + 4, "_____________" + vosptatel, pdataFormat);
 
         } catch (WriteException ex) {
             ex.printStackTrace();
         }
 
     }
 
     private boolean isBlank(String s){
         return (s == null) || (s.length() == 0);
     }
     
     private void addLabelToSheet(WritableSheet sheet, int col, int row, String text, WritableCellFormat format ) throws RowsExceededException, WriteException{
         Label content = new Label(col, row, text, format);
         sheet.addCell(content);
     }
     
     @Override
     public void createFooter(Object... params) {
 
     }
 
     @Override
     public byte[] build(Object... params) {
         String floor = (String) params[0];
         String month = (String) params[1];
         String starosta = (String) params[2];
         String vosptka = (String)params[3];
         
         WorkbookSettings ws = new WorkbookSettings();
         ws.setIgnoreBlanks(true);
         ws.setSuppressWarnings(true);
         ws.setRefreshAll(true);
         try {
             File file = new File(filename);
             WritableWorkbook workbook = Workbook.createWorkbook(file, ws);
             WritableSheet sheet = workbook.createSheet("Duty", 0);
             SheetSettings settings = sheet.getSettings();
             settings.setTopMargin(0);
             settings.setBottomMargin(0);
             
             createTemplate(sheet, month, floor, starosta, vosptka, params[4]);
 
             workbook.write();
             workbook.close();
             return toByteArray(file);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (WriteException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private byte[] toByteArray(File file) throws IOException {
         InputStream is = new FileInputStream(file);
         // Get the size of the file
         long length = file.length();
         if (length > Integer.MAX_VALUE) {
             // File is too large
         }
         // Create the byte array to hold the data
         byte[] bytes = new byte[(int) length];
         // Read in the bytes
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
             offset += numRead;
         }
         // Ensure all the bytes have been read in
         if (offset < bytes.length) {
             throw new IOException("Could not completely read file " + file.getName());
         }
         // Close the input stream and return bytes
         is.close();
         return bytes;
     }
 
     @SuppressWarnings("unused")
     private class CellFormatKey {
         
         private String fontName;
         private String boldStyle;
         private int fontSize;
         private String vAllighment;
         private boolean isWrap = false;
         private String allighment;
         private String color;
         
         public CellFormatKey(String fontName, String boldStyle, int fontSize) {
             this.fontName = fontName;
             this.boldStyle = boldStyle;
             this.fontSize = fontSize;
         }
 
         public String getvAllighment() {
             return vAllighment;
         }
 
         public void setvAllighment(String vAllighment) {
             this.vAllighment = vAllighment;
         }
 
         public boolean isWrap() {
             return isWrap;
         }
 
         public void setWrap(boolean isWrap) {
             this.isWrap = isWrap;
         }
 
         public String getAllighment() {
             return allighment;
         }
 
         public void setAllighment(String allighment) {
             this.allighment = allighment;
         }
 
         public String getColor() {
             return color;
         }
 
         public void setColor(String color) {
             this.color = color;
         }
 
         public CellFormatKey() {
         }
 
         public String getFontName() {
             return fontName;
         }
 
         public void setFontName(String fontName) {
             this.fontName = fontName;
         }
 
         public String getBoldStyle() {
             return boldStyle;
         }
 
         public void setBoldStyle(String boldStyle) {
             this.boldStyle = boldStyle;
         }
 
         public int getFontSize() {
             return fontSize;
         }
 
         public void setFontSize(int fontSize) {
             this.fontSize = fontSize;
         }
 
         public CellFormatKey(String fontName, String boldStyle, int fontSize, String vAllighment, boolean isWrap,
                 String allighment, String color) {
             super();
             this.fontName = fontName;
             this.boldStyle = boldStyle;
             this.fontSize = fontSize;
             this.vAllighment = vAllighment;
             this.isWrap = isWrap;
             this.allighment = allighment;
             this.color = color;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + ((allighment == null) ? 0 : allighment.hashCode());
             result = prime * result + ((boldStyle == null) ? 0 : boldStyle.hashCode());
             result = prime * result + ((color == null) ? 0 : color.hashCode());
             result = prime * result + ((fontName == null) ? 0 : fontName.hashCode());
             result = prime * result + fontSize;
             result = prime * result + (isWrap ? 1231 : 1237);
             result = prime * result + ((vAllighment == null) ? 0 : vAllighment.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             CellFormatKey other = (CellFormatKey) obj;
             if (allighment == null) {
                 if (other.allighment != null)
                     return false;
             } else if (!allighment.equals(other.allighment))
                 return false;
             if (boldStyle == null) {
                 if (other.boldStyle != null)
                     return false;
             } else if (!boldStyle.equals(other.boldStyle))
                 return false;
             if (color == null) {
                 if (other.color != null)
                     return false;
             } else if (!color.equals(other.color))
                 return false;
             if (fontName == null) {
                 if (other.fontName != null)
                     return false;
             } else if (!fontName.equals(other.fontName))
                 return false;
             if (fontSize != other.fontSize)
                 return false;
             if (isWrap != other.isWrap)
                 return false;
             if (vAllighment == null) {
                 if (other.vAllighment != null)
                     return false;
             } else if (!vAllighment.equals(other.vAllighment))
                 return false;
             return true;
         }
 
 
     }
     
         private final static String FONT_STYLE_BOLD = "bold";
         @SuppressWarnings("unused")    
         private final static String ALLIGHMENT_CENTER = "center";
         @SuppressWarnings("unused")
         private final static String ALLIGHMENT_LEFT = "left";
         private final static String FONT_STYLE_NO_BOLD = "nobold";
         private final static String FONT_NAME_TIMES = "times";
         private final static String FONT_NAME_ARIAL = "arial";
         
 
 }
