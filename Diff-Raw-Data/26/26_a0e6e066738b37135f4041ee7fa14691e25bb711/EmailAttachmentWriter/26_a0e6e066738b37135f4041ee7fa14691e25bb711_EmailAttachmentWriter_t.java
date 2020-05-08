 package wb.receiptslibrary;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.Set;
 
 import android.app.ProgressDialog;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Paint.Align;
 import android.os.AsyncTask;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.itextpdf.text.BadElementException;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.FontFactory;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.ColumnText;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfPageEventHelper;
 import com.itextpdf.text.pdf.PdfWriter;
 import com.itextpdf.text.Element;
import wb.receiptspro.R;
 
 import wb.android.flex.Flex;
 import wb.android.storage.StorageManager;
 
 public class EmailAttachmentWriter extends AsyncTask<TripRow, Integer, Long>{
 	
 	public enum EmailOptions {
 		PDF_FULL (0),
 		PDF_IMAGES_ONLY (1),
 		CSV (2),
 		ZIP_IMAGES_STAMPED (3);
 		
 		private final int index;
 		EmailOptions(int index) { this.index = index; }
 		int getIndex() { return this.index; }
 	}
 
 	private static final boolean D = true;
 	private static final String TAG = "EmailAttachmentWriter";
 	
 	private final SmartReceiptsActivity _activity;
 	private final StorageManager _sdCard;
 	private final DatabaseHelper _db;
 	private final ProgressDialog _dialog;
 	private final File[] _files;
 	private final EnumSet<EmailOptions> _options;
 	private boolean memoryErrorOccured = false;
 
 	private static final String IMAGES_PDF = "Images.pdf";
 	private static final String FOOTER = "Report Generated using Smart Receipts for Android";
 	
 	public EmailAttachmentWriter(SmartReceiptsActivity activity, StorageManager sdCard, DatabaseHelper db, ProgressDialog dialog, EnumSet<EmailOptions> options) {
 		_activity = activity;
 		_sdCard = sdCard;
 		_db = db;
 		_dialog = dialog;
 		_options = options;
 		_files = new File[] {null, null, null, null};
 		memoryErrorOccured = false;
 	}
 	
 	@Override
 	protected Long doInBackground(TripRow... trips) {
 		if (trips.length == 0)
 			return new Long(100L); //Should never be reached
 		final TripRow trip = trips[0];
 		final ReceiptRow[] receipts = _db.getReceipts(trip, false);
 		final int len = receipts.length;
 		if (_options.contains(EmailOptions.PDF_FULL)) {
 			try {
 				File dir = trip.dir;
 				if (!dir.exists()) {
 					dir = _sdCard.getFile(trip.dir.getName());
 					if (!dir.exists())
 						dir = _sdCard.mkdir(trip.dir.getName());
 				}
 				final FileOutputStream pdf = _sdCard.getFOS(dir, dir.getName() + ".pdf");
 	            Document document = new Document();
 				PdfWriter writer = PdfWriter.getInstance(document, pdf);
 				writer.setPageEvent(new Footer());
 				document.open();
 				document.add(new Paragraph(SmartReceiptsActivity.CurrencyValue(trip.price, trip.currency) + "  \u2022  " + dir.getName() + "\n"
 							+ "From: " + DateFormat.getDateFormat(_activity).format(trip.from) + " To: " + DateFormat.getDateFormat(_activity).format(trip.to) + "\n\n\n"));
 				PdfPTable table = new PdfPTable(7);
 				table.setWidthPercentage(100);
 				table.addCell("Name");
 				table.addCell("Price");
 				table.addCell("Date");
 				table.addCell("Category");
 				table.addCell("Comment");
 				table.addCell("Expensable");
 				table.addCell("Pictured");
 				ReceiptRow receipt;
 				for (int i=0; i < len; i++) {
 					receipt = receipts[i];
 					if (_activity._onlyIncludeExpensable && !receipt.expensable) 
 						continue;
 					if (Float.parseFloat(receipt.price) < _activity._minReceiptPrice)
 						continue;
 					table.addCell(receipt.name);
 					table.addCell(SmartReceiptsActivity.CurrencyValue(receipt.price, receipt.currency));
 					table.addCell(DateFormat.getDateFormat(_activity).format(receipt.date));
 					table.addCell(receipt.category);
 					table.addCell(receipt.comment);
 					table.addCell(((receipt.expensable)?"":"Not ") + "Expensable");
 					table.addCell(((receipt.img != null)?"":"Not ") + "Pictured");
 				}
 				document.add(table);
 				document.newPage();
 				this.addImageRows(document, receipts);
 				document.close();
 				pdf.close();
 				_files[EmailOptions.PDF_FULL.getIndex()] = _sdCard.getFile(dir, dir.getName() + ".pdf");
 			} catch (FileNotFoundException e) {
 				Log.e(TAG, e.toString());
 			} catch (DocumentException e) {
 				Log.e(TAG, e.toString());
 			} catch (IOException e) {
 				Log.e(TAG, e.toString());
 			}
 		}
 		if (_options.contains(EmailOptions.PDF_IMAGES_ONLY)) {
 			try {
 				File dir = trip.dir;
 				if (!dir.exists()) {
 					dir = _sdCard.getFile(trip.dir.getName());
 					if (!dir.exists())
 						dir = _sdCard.mkdir(trip.dir.getName());
 				}
 				final FileOutputStream pdf = _sdCard.getFOS(dir, dir.getName() + IMAGES_PDF);
 	            Document document = new Document();
 				PdfWriter writer = PdfWriter.getInstance(document, pdf);
 				writer.setPageEvent(new Footer());
 				document.open();
 				this.addImageRows(document, receipts);
 				document.close();
 				pdf.close();
 				_files[EmailOptions.PDF_IMAGES_ONLY.getIndex()] = _sdCard.getFile(dir, dir.getName() + IMAGES_PDF);
 			} catch (FileNotFoundException e) {
 				Log.e(TAG, e.toString());
 			} catch (DocumentException e) {
 				Log.e(TAG, e.toString());
 			} catch (IOException e) {
 				Log.e(TAG, e.toString());
 			} catch (RuntimeException e) {
 				Log.e(TAG, e.toString()); //Document has no pages (likely selected an image pdf with no images)
 			}
 		}
 		if (_options.contains(EmailOptions.CSV)) {
 			File dir = trip.dir;
 			if (!dir.exists()) {
 				dir = _sdCard.getFile(trip.dir.getName());
 				if (!dir.exists())
 					dir = _sdCard.mkdir(trip.dir.getName());
 			}
 			String data = "";
 			CSVColumns columns = _db.getCSVColumns(_activity._flex);
 			for (int i=0; i < len; i++) {
 				if (_activity._onlyIncludeExpensable && !receipts[i].expensable) 
 					continue;
 				if (Float.parseFloat(receipts[i].price) < _activity._minReceiptPrice)
 					continue;
 				data += columns.print(receipts[i], trip);
 			}
 			String filename = dir.getName() + ".csv";
 			if (!_sdCard.write(dir, filename, data))
 				Log.e(TAG, "Failed to write the csv file");
 			_files[EmailOptions.CSV.getIndex()] = _sdCard.getFile(dir, filename);
 		}
 		if (_options.contains(EmailOptions.ZIP_IMAGES_STAMPED)) { //Get rid of Debug line here
 			File dir = _sdCard.mkdir(trip.dir, trip.dir.getName());
 			for (int i=0; i < len; i++) {
 				if (_activity._onlyIncludeExpensable && !receipts[i].expensable) 
 					continue;
 				if (receipts[i].img == null)
 					continue;
 				try {
 					Bitmap b = stampImage(trip, receipts[i], Bitmap.Config.ARGB_8888);
 					_sdCard.writeBitmap(dir, b, (i+1) + "_" + receipts[i].name + ".jpg", CompressFormat.JPEG, 85);
 					b.recycle();
 				}
 				catch (OutOfMemoryError e) {
 					System.gc();
 					try {
 						Bitmap b = stampImage(trip, receipts[i], Bitmap.Config.RGB_565);
 						_sdCard.writeBitmap(dir, b, (i+1) + "_" + receipts[i].name + ".jpg", CompressFormat.JPEG, 85);
 						b.recycle();
 					}
 					catch (OutOfMemoryError e2) {
 						memoryErrorOccured = true;
 						break;
 					}
 				}
 			}
 			File zip = _sdCard.zipBuffered(dir, 2048);
 			_sdCard.deleteRecursively(dir);
 			_files[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] = zip;			
 		}
 		return new Long(100L);
 	}
 	
 	private static final float IMG_SCALE_FACTOR = 2.1f;
 	private static final float HW_RATIO = 0.75f;
     private Bitmap stampImage(final TripRow trip, final ReceiptRow receipt, Bitmap.Config config) {
         Bitmap foreground = _sdCard.getMutableMemoryEfficientBitmap(receipt.img);
         int foreWidth = foreground.getWidth(), foreHeight = foreground.getHeight();
         if (foreHeight > foreWidth) { foreWidth = (int) (foreHeight*HW_RATIO); }
         else { foreHeight = (int) (foreWidth/HW_RATIO); }
         int xPad = (int) (foreWidth/IMG_SCALE_FACTOR), yPad = (int) (foreHeight/IMG_SCALE_FACTOR);
         Bitmap background = Bitmap.createBitmap(foreWidth + xPad, foreHeight + yPad, config);
         Canvas canvas = new Canvas(background);
         canvas.drawARGB(0xFF,0xFF,0xFF,0xFF); //This represents White color
         Paint dither = new Paint(); 
         dither.setDither(true); dither.setFilterBitmap(false);
         canvas.drawBitmap(foreground, (background.getWidth() - foreground.getWidth())/2, (background.getHeight() - foreground.getHeight())/2, dither);
         Paint brush = new Paint();
         brush.setAntiAlias(true);
         brush.setTypeface(Typeface.SANS_SERIF);
         brush.setColor(Color.BLACK);
         brush.setStyle(Paint.Style.FILL);
         brush.setTextAlign(Align.LEFT);
         int num = 5; 
         if (receipt.extra_edittext_1 != null) num++;
         if (receipt.extra_edittext_2 != null) num++;
         if (receipt.extra_edittext_3 != null) num++;
         float spacing = getOptimalSpacing(num, yPad/2, brush);
         float y = spacing*4;
         canvas.drawText(trip.dir.getName(), xPad/2, y, brush); y += spacing;
         canvas.drawText(DateFormat.getDateFormat(_activity).format(trip.from) + " -- " + DateFormat.getDateFormat(_activity).format(trip.to), xPad/2, y, brush); y += spacing;
         y = background.getHeight() - yPad/2 + spacing*2;
         Flex flex = _activity._flex;
         canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_NAME) + ": " + receipt.name, xPad/2, y, brush); y += spacing;
         canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_PRICE) + ": " + receipt.price + " " + receipt.currency.toString(), xPad/2, y, brush); y += spacing;
         canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_DATE) + ": " + DateFormat.getDateFormat(_activity).format(receipt.date), xPad/2, y, brush); y += spacing;
         canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_CATEGORY) + ": " + receipt.category, xPad/2, y, brush); y += spacing;
         canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_COMMENT) + ": " + receipt.comment, xPad/2, y, brush); y += spacing;
         if (receipt.extra_edittext_1 != null) { canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1) + ": " + receipt.extra_edittext_1, xPad/2, y, brush); y += spacing; }
         if (receipt.extra_edittext_2 != null) { canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2) + ": " + receipt.extra_edittext_2, xPad/2, y, brush); y += spacing; }
         if (receipt.extra_edittext_3 != null) { canvas.drawText(flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3) + ": " + receipt.extra_edittext_3, xPad/2, y, brush); y += spacing; }
         return background;
     }
     
     private float getOptimalSpacing(int count, int space, Paint brush) {
     	float fontSize = 8f; //Seed
     	brush.setTextSize(fontSize);
     	while (space > (count + 2)*brush.getFontSpacing()) { brush.setTextSize(++fontSize);}
     	brush.setTextSize(--fontSize);
     	return brush.getFontSpacing();
     }
 	
 	private Document addImageRows(Document document, ReceiptRow[] receipts) {
 			PdfPTable table = new PdfPTable(2);
 			table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
 			table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
 			table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
 			table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
 			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
 			table.setWidthPercentage(100);
 			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
 			table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
 			table.setSplitLate(false);
 			int size = receipts.length;
 			ReceiptRow receipt;
 			Image img1 = null, img2 = null;
 			ReceiptRow receipt1 = null;
 			int num1 = 0, flag = 0;
 			final HashMap<ReceiptRow, Integer> fullpageReceipts = new HashMap<ReceiptRow, Integer>();
 			for (int i=0; i < size; i++) {
 				receipt = receipts[i];
 				if (_activity._onlyIncludeExpensable && !receipt.expensable) 
 					continue;
 				if (receipt.img == null)
 					continue;
 				if (receipt.fullpage) {
 					fullpageReceipts.put(receipt, i+1);
 					continue;
 				}
 				if (Float.parseFloat(receipt.price) < _activity._minReceiptPrice)
 					continue;
 				if (receipt.img != null && img1 == null) {
 					try {
 						img1 = Image.getInstance(receipt.img.getCanonicalPath());
 					} catch (BadElementException e) {
 						Log.e(TAG, e.toString());
 						continue;
 					} catch (MalformedURLException e) {
 						Log.e(TAG, e.toString());
 						continue;
 					} catch (IOException e) {
 						Log.e(TAG, e.toString());
 						continue;
 					}
 					receipt1 = receipt;
 					num1 = i;
 				}
 				else if (receipt.img != null && img2 == null) {
 					try {
 						img2 = Image.getInstance(receipt.img.getCanonicalPath());
 					} catch (BadElementException e) {
 						Log.e(TAG, e.toString());
 						continue;
 					} catch (MalformedURLException e) {
 						Log.e(TAG, e.toString());
 						continue;
 					} catch (IOException e) {
 						Log.e(TAG, e.toString());
 						continue;
 					}
 					table.addCell((num1+1) + "  \u2022  " + receipt1.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(receipt1.date));
 					table.addCell((i+1) + "  \u2022  " + receipt.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(receipt.date));
 					table.addCell(getCell(img1));
 					table.addCell(getCell(img2));
 					table.setSpacingAfter(40);
 					img1 = null; img2 = null;
 					if (++flag==2) {//ugly hack to fix how page breaks are separated
 						table.completeRow();
 						try {
 							document.add(table);
 						} catch (DocumentException e) {
 							Log.e(TAG, e.toString());
 						}
 						table = new PdfPTable(2);
 						table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
 						table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
 						table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
 						table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
 						table.setWidthPercentage(100);
 						table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
 						table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
 						table.setSplitLate(false);
 						flag = 0;
 					}
 				}
 			}
 			if (img1 != null) {
 				table.addCell((num1+1) + "  \u2022  " + receipt1.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(receipt1.date));
 				table.addCell(" ");
 				table.addCell(getCell(img1));
 			}
 			table.completeRow();
 			try {
 				document.add(table);
 			} catch (DocumentException e) {
 				Log.e(TAG, e.toString());
 			}
 			document.newPage();
 			//Full Page Stuff Below
 			Set<ReceiptRow> set = fullpageReceipts.keySet();
 			size = fullpageReceipts.size();
 			for (ReceiptRow rcpt:set) { //Redo to get rid of iterator
 				if (_activity._onlyIncludeExpensable && !rcpt.expensable) 
 					continue;
 				if (Float.parseFloat(rcpt.price) < _activity._minReceiptPrice)
 					continue;
 				try {
 					img1 = Image.getInstance(rcpt.img.getCanonicalPath());
 					table = new PdfPTable(1);
 					table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
 					table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
 					table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
 					table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
 					table.setWidthPercentage(100);
 					table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
 					table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
 					table.setSplitLate(false);
 					table.addCell(fullpageReceipts.get(rcpt).intValue() + "  \u2022  " + rcpt.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(rcpt.date));
 					table.addCell(getFullCell(img1));
 					table.completeRow();
 					document.add(table);
 					document.newPage();
 				} catch (BadElementException e) {
 					Log.e(TAG, e.toString());
 					continue;
 				} catch (MalformedURLException e) {
 					Log.e(TAG, e.toString());
 					continue;
 				} catch (IOException e) {
 					Log.e(TAG, e.toString());
 					continue;
 				}
 				catch (DocumentException e) {
 					Log.e(TAG, e.toString());
 					continue;
 				}
 			}
 			return document;
 	}
 	
 	private final PdfPCell getCell(Image img) {
 		PdfPCell cell = new PdfPCell(img, true);
 		cell.disableBorderSide(PdfPCell.LEFT);
 		cell.disableBorderSide(PdfPCell.RIGHT);
 		cell.disableBorderSide(PdfPCell.TOP);
 		cell.disableBorderSide(PdfPCell.BOTTOM);
 		cell.setBorder(Rectangle.NO_BORDER);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setVerticalAlignment(Element.ALIGN_CENTER);
 		cell.setFixedHeight(PageSize.A4.getHeight()/2.4f);
 		return cell;
 	}
 	
 	private final PdfPCell getFullCell(Image img) {
 		PdfPCell cell = new PdfPCell(img, true);
 		cell.disableBorderSide(PdfPCell.LEFT);
 		cell.disableBorderSide(PdfPCell.RIGHT);
 		cell.disableBorderSide(PdfPCell.TOP);
 		cell.disableBorderSide(PdfPCell.BOTTOM);
 		cell.setBorder(Rectangle.NO_BORDER);
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setVerticalAlignment(Element.ALIGN_CENTER);
 		cell.setFixedHeight(PageSize.A4.getHeight()/1.15f);
 		return cell;
 	}
 	
 	@Override
 	protected void onPostExecute(Long result) {
 		_activity.postCreateAttachments(_files);
 		_dialog.cancel();
 	}
 	
 	@Override
 	protected void onProgressUpdate(Integer... values) {
 		if (memoryErrorOccured) {
 			memoryErrorOccured = false;
 			Toast.makeText(_activity, "Error: Not enough memory to stamp the images. Try stopping some other apps and try again.", Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	private class Footer extends PdfPageEventHelper {
 		public void onEndPage(PdfWriter writer, Document document) {
 			Rectangle rect = writer.getPageSize();
 			ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, 
 					new Phrase(FOOTER, FontFactory.getFont("Times-Roman", 9, Font.ITALIC)), rect.getLeft()+36, rect.getBottom()+36, 0);
 		}
 	}
 
 }
