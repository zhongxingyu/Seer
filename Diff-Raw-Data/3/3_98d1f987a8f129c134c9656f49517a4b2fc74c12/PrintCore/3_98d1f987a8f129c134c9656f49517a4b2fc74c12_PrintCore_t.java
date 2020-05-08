 /*
  * jCleaningSchedule - program for printing house cleaning schedules
  * Copyright (C) 2012  Martin Mareš <mmrmartin[at]gmail[dot]com>
  *
  * This file is part of jCleaningSchedule.
  *
  * jCleaningSchedule is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * jCleaningSchedule is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with jCleaningSchedule.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cz.martinmares.jcleaningschedule.print;
 
 import cz.martinmares.jcleaningschedule.core.Core;
 import cz.martinmares.jcleaningschedule.core.JCleaningScheduleData;
 import cz.martinmares.jcleaningschedule.dateutil.DateUtil;
 import cz.martinmares.jcleaningschedule.dateutil.YearWeekDate;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.font.FontRenderContext;
 import java.awt.font.LineMetrics;
 import java.awt.image.BufferedImage;
 import java.awt.print.PageFormat;
 import java.awt.print.Pageable;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import javax.print.attribute.HashPrintRequestAttributeSet;
 import javax.print.attribute.PrintRequestAttributeSet;
 import javax.print.attribute.standard.JobName;
 import javax.print.attribute.standard.MediaPrintableArea;
 import javax.print.attribute.standard.MediaSizeName;
 
 /**
  * Class whitch handle painting the cleaning schedules on canvas.
  * @author Martin Mareš
  */
 public class PrintCore implements Printable, Pageable{
     
     JCleaningScheduleData data;
     PrintData printData;
     Core core;
     
     PageFormat pageFormat;
     PrinterJob printerJob;
     PrintRequestAttributeSet printAtt;
     
     Font rowFont;
     Font legendFont;
     Font titleFont;
     Font periodFont;
     
     final String title = str.getString("SCHEDULE_PLAN");
     
     //Strings paramethers
     //Rows    
     int table_row_top_margin;
     int table_row_height;
     //Columns
     int[] table_column_w;      //Table column width
     int[] table_column_i;      //Table column indexes (same as in file; -1 = main)
     int   table_column_date_w; //Date column with
     int   table_columns_num;    //Number of column on page without date column
     int   table_width;
     
     int period_height;
     int title_height;
     
     static int COLUMN_MARGIN = 4;
     static int ROW_TOP_MARRGIN = 2;
     static String DEFAULT_DATE_FORMAT = "88.88.-88.88.";
     static Color DEFAULT_COLOR = Color.BLACK;
     private static final ResourceBundle str = ResourceBundle.getBundle("cz/martinmares/jcleaningschedule/resources/lang");
     
     public PrintCore(PrintData inpd, JCleaningScheduleData indata) {
         //Initial data
         printData = inpd;
         data = indata;
 
         printDataUpdated();
 
         core = new Core(printData.getBegin(),data);
 
         //Print data
         printerJob = PrinterJob.getPrinterJob();
         printerJob.setJobName("JCleaningSchedule");
         printerJob.setPageable(this);
         printAtt = new HashPrintRequestAttributeSet();
         //printAtt.add(new PageRanges(1, getNumberOfPages()));
         printAtt.add(new JobName("JCleaningSchedule",Locale.getDefault()));
         //TODO: base it on user's locate
         printAtt.add(MediaSizeName.ISO_A4); //A4 - (210×297 mm)       
         printAtt.add(new MediaPrintableArea(5, 5, 200, 287, 
                 MediaPrintableArea.MM)); 
         pageFormat = printerJob.defaultPage();
  
         //Fonts
         updateFontData();
     }
     
     public PrintData getPrintData() {
         return printData;
     }
     
     public PageFormat getPageFormat() {
         return pageFormat;
     }
     
     public void setPageFormat(PageFormat pf) {
         pageFormat = pf;
     }
     
     public void updateFontData() {
         titleFont=new Font(data.getDefaultFontName(), Font.BOLD, 20);
         rowFont=new Font(data.getDefaultFontName(), Font.PLAIN, 14);
         legendFont=new Font(data.getDefaultFontName(), Font.BOLD, 14);
         periodFont = rowFont;
         
         //Dimensions of lines
         FontRenderContext frc = ((Graphics2D)(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB))
                 .getGraphics()).getFontRenderContext();
         LineMetrics rowLine = rowFont.getLineMetrics("Martin", frc);
         table_row_top_margin =(int)Math.ceil(Math.max(ROW_TOP_MARRGIN,rowLine.getLeading()));
         table_row_height = (int) Math.ceil(rowLine.getHeight() 
                 + Math.max(ROW_TOP_MARRGIN,rowLine.getLeading()));
         
         title_height= (int) Math.ceil(titleFont.getLineMetrics(title, frc).getHeight());
         period_height = (int) Math.ceil(periodFont.getLineMetrics(str.getString("PRINT_PERIOD"), frc).getHeight());
     }
     
     public synchronized void setPrintData(PrintData pd) {
         printData = pd;
         printDataUpdated();
     }
     
     private void printDataUpdated() {
         //I don't know how to set ranges in print dialog :-( and this don't work
         //printAtt.add(new PageRanges(1, printData.getPagesCount()));
     }
     
     public boolean printAll() throws PrinterException {
         printerJob.print();
         return true;
     }
 
     @Override
     public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
             throws PrinterException {
         //Basic
         if (pageIndex > getNumberOfPages()) {
             return NO_SUCH_PAGE;
         }
         Graphics2D g = (Graphics2D)graphics; //Cast to Graphics2D object
         g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
         
         core.setDate(printData.getBegin().getYear(), 
                 printData.getBegin().getWeek()+getNumberOfRowsOnPage()*(pageIndex));
         
         int rows_num;
         if(printData.fillOnePage()) {
             rows_num = getNumberOfRowsOnPage();
         } else {
             rows_num = Math.min(getNumberOfRowsOnPage(),
                     DateUtil.getWeeksCount(core, printData.getEnd())+1);
         }
                 
         //Painting
         FontMetrics fm;
         g.setColor(DEFAULT_COLOR);
 
         //Title
         g.setFont(titleFont);
         fm = g.getFontMetrics();
         title_height = fm.getHeight();
         g.drawString(title, (int)(pageFormat.getImageableWidth()
                 -fm.stringWidth(title))/2, fm.getAscent());
         int pos_y = title_height;
         
         //Period
         if(printData.printPeriod()) {
             String period;
             int date[] = data.getDateOfWeek(core, true);
             period = java.text.MessageFormat.format(str.getString("PRINT_PERIOD_STR"), new Object[] {date[0], date[1], date[2]});
             date = data.getDateOfWeek(
                     new YearWeekDate(core.getYear(),
                     core.getWeek()+rows_num - 1), false);
             period += date[0] + ". " + date[1] + ". " + date[2];
             g.setFont(periodFont);
             fm = g.getFontMetrics();
             period_height = fm.getHeight();
             g.drawString(period, 0, pos_y+fm.getAscent());
             pos_y+=period_height;
             
         }
         
         //Table
         g.setFont(rowFont);
         fm = g.getFontMetrics();
         //Update column data
         updateColumnsData(fm, (int)pageFormat.getImageableWidth());
         //Update dimensions
         table_row_top_margin = Math.max(ROW_TOP_MARRGIN,fm.getLeading());
         table_row_height = fm.getHeight() 
                 + Math.max(ROW_TOP_MARRGIN,fm.getLeading());
         
         //Legend
         g.setFont(legendFont);
         int pos_x = 0;
         //Legednd - Date
         g.drawRect(pos_x, pos_y, table_column_date_w, table_row_height);
         g.drawString(str.getString("PRINT_DATE"), (pos_x + table_column_date_w
                 - fm.stringWidth(str.getString("PRINT_DATE")))/2, pos_y + table_row_top_margin
                     +fm.getAscent());
         pos_x+=table_column_date_w;       
         //Legend - Floors
         for(int i=0;i<table_columns_num;i++) {
             String name;
             if(table_column_i[i]==-1) { 
                 name = str.getString("MAIN");
             }
             else {
                 name = table_column_i[i]+". "+str.getString("MAIN_FLOOR");
             }           
             g.drawRect(pos_x, pos_y, table_column_w[i], table_row_height);
             g.drawString(name, pos_x + (table_column_w[i] -
                     fm.stringWidth(name))/2, pos_y + table_row_top_margin
                     +fm.getAscent());
             pos_x+=table_column_w[i];
         }
         pos_y+=table_row_height;
         
         //Rows
         g.setFont(rowFont);
         for(int i=0;i< rows_num ;i++) {
             Color txtColor = DEFAULT_COLOR;
             //Draw every socond row with diferent background
             if(i%2==1) {
                 g.setColor(printData.getSecondBgColor());
                 g.fillRect(1, pos_y+1, (int)table_width-1,
                         table_row_height-1);
                 txtColor = data.getScTxtColor();
             }
             drawTableRow(pos_y, txtColor, g, fm);
             pos_y+=table_row_height;
             core.nextWeek();
         }
         
         return PAGE_EXISTS;
     }
     
     private void drawTableRow(int pos_y, Color TxtColor, Graphics2D g, FontMetrics fm) {
         int pos_x = 0;
         
         //Date
         g.setColor(DEFAULT_COLOR);
         g.drawRect(pos_x, pos_y, table_column_date_w, table_row_height);
 
         int num_windth = fm.stringWidth("88.");
         pos_x+=COLUMN_MARGIN + num_windth;
         String num = data.getDateOfWeek(core, true)[0]+".";
         g.setColor(TxtColor);
         g.drawString(num, pos_x - fm.stringWidth(num), pos_y + table_row_top_margin
                     +fm.getAscent());
         
         pos_x+=num_windth;
         num = data.getDateOfWeek(core, true)[1]+".";
         g.drawString(num, pos_x - fm.stringWidth(num), pos_y + table_row_top_margin
                     +fm.getAscent());
         
         pos_x+=COLUMN_MARGIN;
         g.drawString("-", pos_x, pos_y + table_row_top_margin
                     +fm.getAscent());
         
         pos_x+=fm.stringWidth("-") + COLUMN_MARGIN + num_windth;
         num = data.getDateOfWeek(core, false)[0]+".";
         g.drawString(num, pos_x - fm.stringWidth(num), pos_y + table_row_top_margin
                     +fm.getAscent());
         
         pos_x+=num_windth;
         num = data.getDateOfWeek(core, false)[1]+".";
         g.drawString(num, pos_x - fm.stringWidth(num), pos_y + table_row_top_margin
                     +fm.getAscent());
        // pos_x+=COLUMN_MARGIN; depracted (rounding problems)
        pos_x = table_column_date_w;
         
         //Names
         for(int i=0;i<table_columns_num;i++) {
             String name = core.getPeople(table_column_i[i]);
             g.setColor(DEFAULT_COLOR);
             g.drawRect(pos_x, pos_y, table_column_w[i], table_row_height);
             g.setColor(TxtColor);
             g.drawString(name, pos_x + (table_column_w[i] -
                     fm.stringWidth(name))/2, pos_y + table_row_top_margin
                     +fm.getAscent());
             pos_x+=table_column_w[i];
         }
     }
     
     private void updateColumnsData(FontMetrics fm, int free_width) {
         findPrintableColumns();
         table_column_date_w = fm.stringWidth(DEFAULT_DATE_FORMAT)+4*COLUMN_MARGIN;
         updateColumnsWidth(fm, free_width - table_column_date_w);
     }
     
     private void updateColumnsWidth(FontMetrics fm, int free_width) {
         int widest = 0;
         int pos_x = 0;
         table_columns_num = 0;
         table_column_w = new int[table_column_i.length];
         for(int i=0;i<table_column_i.length;i++) {
             table_column_w[i] = widestWordOfColumn(table_column_i[i],fm);
             if(widest<table_column_w[i]) {
                 widest=table_column_w[i];
             }
             if(pos_x+table_column_w[i]<=free_width) {
                 pos_x+= table_column_w[i];
                 table_columns_num++;
             } else {
                 break;
             }
         }
         
         if(printData.printSameWideColumns()) {
             if(widest*table_column_i.length<=free_width) {
                 table_columns_num=table_column_i.length;
             } else {
                 table_columns_num=free_width/widest;
             }
             widest=free_width/table_columns_num;
             table_width=widest*table_columns_num + table_column_date_w;
             for(int i=0;i<table_columns_num;i++) {
                 table_column_w[i]=widest;
             }
         } else {
             table_width=table_column_date_w;
             double scale = Integer.valueOf(free_width).doubleValue()
                     / Integer.valueOf(pos_x).doubleValue();
             for(int i=0;i<table_columns_num;i++) {
                 table_column_w[i]=(int)(Integer.valueOf(table_column_w[i])
                         .doubleValue()*scale);
                 table_width+=table_column_w[i];
             }
         }
     }
     
     private void findPrintableColumns() {
         List<Integer> intList = new ArrayList<>();
         if(printData.printMainSchedule()&&data.getMain().length!=0) {
             intList.add(-1);
         }
         for(int c=printData.getFirstPritableFloor();c<data.getNumOfFloors();c++) {
             if(data.getFloor(c).length!=0) {
                 intList.add(c);
             }
         }
         table_column_i = new int[intList.size()];   
         for (int i = 0; i < table_column_i.length; i++) {
             table_column_i[i] = intList.get(i);
         }
     }
     
     private int widestWordOfColumn(int column, FontMetrics fm) {
         int widest=0;
         for(String name: data.getNames(column)) {
             int new_width = fm.stringWidth(name) + COLUMN_MARGIN*2;
             if(widest<new_width) {
                 widest=new_width;
             }
         }
         return widest;
     }
     
     public int getNumberOfRowsOnPage() {
         double num = (pageFormat.getImageableHeight()-title_height);
         if(printData.printPeriod()) {
             num-=period_height;
         }
         num = num / table_row_height; 
         num--; //For legend
         return (int) num;
     }
 
     @Override
     public int getNumberOfPages() {
         return (int)Math.ceil(Integer.valueOf(printData.getWeeksCount()).doubleValue()
                 /(Integer.valueOf(getNumberOfRowsOnPage()).doubleValue()));
     }
 
     @Override
     public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
         return pageFormat;
     }
 
     @Override
     public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
         return this;
     }
 
 }
