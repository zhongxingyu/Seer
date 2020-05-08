 package tw.com.sunnybay.daybook.io;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Locale;
 
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import tw.com.sunnybay.daybook.R;
 import tw.com.sunnybay.daybook.db.DaybookDBHelper;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.widget.Toast;
 
 public class ExcelExporter extends Thread {
 
 	Context context = null;
 	Calendar calendar = null;
 	File file = null;
 
 	public ExcelExporter(Context context, Calendar calendar, File file) {
 		this.context = context;
 		this.calendar = calendar;
 		
		String fileName = String.format(Locale.getDefault(), "daybook-%tY-%tm.xls",
 				calendar, calendar);
 		this.file = new File(file, fileName);
 	}
 
 	@Override
 	public void run() {
 		FileOutputStream out = null;
 
 		DaybookDBHelper helper = new DaybookDBHelper(context);
 		SQLiteDatabase db = helper.getReadableDatabase();
 
 		/*
 		 * Get item list of this month.
 		 */
 		String sql = String.format(
 				"SELECT _DATE, _ITEM, _PAYMENT, _AMOUNT, _NOTE FROM %s\n"
 						+ "WHERE _DATE LIKE '%tY-%tm%%'\n"
 						+ "ORDER BY _DATE DESC", DaybookDBHelper.TABLE_NAME,
 				calendar, calendar);
 
 		Workbook wb = new HSSFWorkbook();
 
 		Sheet sheet = wb.createSheet();
 		Row row = sheet.createRow(0);
 		row.createCell(0).setCellValue(context.getString(R.string.date));
 		row.createCell(1).setCellValue(context.getString(R.string.title));
 		row.createCell(2)
 				.setCellValue(context.getString(R.string.payment_type));
 		row.createCell(3).setCellValue(context.getString(R.string.amount));
 		row.createCell(4).setCellValue(context.getString(R.string.note));
 
 		try {
 			out = new FileOutputStream(file);
 
 			Cursor cursor = db.rawQuery(sql, null);
 			cursor.moveToFirst();
 			int i = 1;
 			while (!cursor.isAfterLast()) {
 				row = sheet.createRow(i);
 				row.createCell(0).setCellValue(cursor.getString(0));
 				row.createCell(1).setCellValue(cursor.getString(1));
 
 				switch (cursor.getInt(2)) {
 				case DaybookDBHelper.PAYMENT_CREDIT_CARD:
 					row.createCell(2).setCellValue(context.getString(R.string.credit_card));
 					break;
 				default:
 					row.createCell(2).setCellValue(context.getString(R.string.cash));
 				}
 
 				row.createCell(3).setCellValue(cursor.getString(3));
 				row.createCell(4).setCellValue(cursor.getString(4));
 				i++;
 				cursor.moveToNext();
 			}
 			cursor.close();
 
 			wb.write(out);
 			out.flush();
 			out.close();
 
 			Toast.makeText(context, context.getString(R.string.done),
 					Toast.LENGTH_SHORT).show();
 
 		} catch (IOException e) {
 			Toast.makeText(context,
 					context.getString(R.string.cannot_export_xls),
 					Toast.LENGTH_SHORT).show();
 			Log.d(null, e.getMessage());
 		} finally {
 			try {
 				if (out != null) {
 					out.close();
 					out = null;
 				}
 			} catch (IOException e) {
 				Log.d(null, e.getMessage());
 			}
 		}
 
 	}
 
 }
