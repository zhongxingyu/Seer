 package org.dozeneyes;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Random;
 
 import jxl.CellView;
 import jxl.Workbook;
 import jxl.WorkbookSettings;
 import jxl.format.UnderlineStyle;
 import jxl.write.Formula;
 import jxl.write.Label;
 import jxl.write.Number;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableFont;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 import jxl.write.biff.RowsExceededException;
 
 import org.dozeneyes.aspect.*;
 
 public class GenerateLevelRows {
 
   protected static Logger log = new Logger(GenerateLevelRows.class);
 
   protected static final int FONT_SIZE = 10;
 
   protected WritableWorkbook workbook;
   protected WritableCellFormat cellFormat;
   protected WritableCellFormat cellFormatBold;
   protected Random random = new Random();
 
   private int row = 1;
   private String level = "[level]";
 
   public GenerateLevelRows(String level) {
      this.level = level;
   }
 
 
   public void createWorkbook(String outputFile) throws IOException, WriteException {
     File file = new File(outputFile);
     WorkbookSettings wbSettings = new WorkbookSettings();
     wbSettings.setLocale(new Locale("en", "EN"));
 
     workbook = Workbook.createWorkbook(file, wbSettings);
     workbook.createSheet("Levels", 0);
 
     WritableSheet excelSheet = workbook.getSheet(0);
     createFormats(excelSheet);
   }
 
   public void closeWorkbook() throws IOException, WriteException {
     workbook.write();
     workbook.close();
   }
 
   public void write(int complexity) throws IOException, WriteException {
     WritableSheet excelSheet = workbook.getSheet(0);
     complexitySelector(excelSheet, complexity);
   }
 
   protected void createFormats(WritableSheet sheet) throws WriteException {
 
     // cell formats
     WritableFont cellFont = new WritableFont(WritableFont.ARIAL, FONT_SIZE);
     cellFormat = new WritableCellFormat(cellFont);
     cellFormat.setWrap(false);
 
     WritableFont headerFont =
        new WritableFont(WritableFont.ARIAL, FONT_SIZE, WritableFont.BOLD, false);
     cellFormatBold = new WritableCellFormat(headerFont);
     cellFormatBold.setWrap(false);
 
     // header
     String[] headers = {
        "Level", "Orientation", "Color", "Pattern", "Sound", "Animation"
     };
     for (int i=0; i < headers.length; i++) {
        sheet.getColumnView(i).setSize(256*50);
        addHeader(sheet, i, 0, headers[i]);
     }
   }
 
 
   protected void complexitySelector(WritableSheet sheet, int complexity)
      throws WriteException, RowsExceededException {
 
      Orientation o = Orientation.LEFT;
 
      // first row is each aspect at random
      Color c = Color.reset();
      Pattern p = Pattern.reset();
      Sound s = Sound.reset();
      Animation a = Animation.reset();
 
      addLabel(sheet, 6, row, "complexity " + complexity);
      addRow(sheet, o, c, p, s, a);
 
     // the, choose which aspects to further randomize
      List<Boolean> isRandom =
         Arrays.asList(new Boolean[] { false, false, false, false });
      for (int i=0; i < 4; i++) {
        isRandom.set(i, i+1 < complexity);
      }
      Collections.shuffle(isRandom, random);
 
      // for rows two and three...
      for (int i=0; i < 2; i++) {
 
         o = (i == 0) ? Orientation.CENTER : Orientation.RIGHT;
 
         if (isRandom.get(0)) c = Color.next();
         if (isRandom.get(1)) p = Pattern.next();
         if (isRandom.get(2)) s = Sound.next();
         if (isRandom.get(3)) a = Animation.next();
 
         addRow(sheet, o, c, p, s, a);
      }
   }
 
   protected void addRow(WritableSheet sheet,
                         Orientation o, Color c, Pattern p, Sound s, Animation a)
      throws WriteException, RowsExceededException {
 
      addLabel(sheet, 0, row, level);
      addLabel(sheet, 1, row, o + " " + o.ordinal());
      addLabel(sheet, 2, row, c + " " + c.ordinal());
      addLabel(sheet, 3, row, p + " " + p.ordinal());
      addLabel(sheet, 4, row, s + " " + s.ordinal());
      addLabel(sheet, 5, row, a + " " + a.ordinal());
      row++;
   }
 
   protected void addHeader(WritableSheet sheet, int column, int row, String s)
           throws RowsExceededException, WriteException {
     Label label;
     label = new Label(column, row, s, cellFormatBold);
     sheet.addCell(label);
   }
 
   protected void addNumber(WritableSheet sheet, int column, int row, Integer integer)
           throws WriteException, RowsExceededException {
     Number number = new Number(column, row, integer, cellFormat);
     sheet.addCell(number);
   }
 
   protected void addLabel(WritableSheet sheet, int column, int row, String s)
           throws WriteException, RowsExceededException {
     Label label = new Label(column, row, s, cellFormat);
     sheet.addCell(label);
   }
 
 
   public static void main(String[] args) throws WriteException, IOException {
 
     if (args.length < 2) {
        System.err.println("Usage: $0 [list of complexities, each from 1-5]");
        System.err.println("e.g.: $0 3: 1 1 2 2 3 3 4 4 5 5");
        System.exit(1);
     }
 
     GenerateLevelRows main = new GenerateLevelRows(args[0].replace(":",""));
     String fn = "gen/levels.xls";
     main.createWorkbook(fn);
 
     for (int i=1; i < args.length; i++) {
         try {
            int complexity = Integer.parseInt(args[i]);
            log.d("generate " + complexity);
            main.write(complexity);
         }
         catch (NumberFormatException nfe) {
            log.e("couldn't parse " + args[i]);
         }
     }
     main.closeWorkbook();
     log.i("results are in " + fn);
   }
 }
 
 
