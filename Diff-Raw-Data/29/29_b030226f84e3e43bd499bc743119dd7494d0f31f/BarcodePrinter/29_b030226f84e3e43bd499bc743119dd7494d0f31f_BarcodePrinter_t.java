 package model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.itextpdf.text.Chunk;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Image;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.Barcode;
 import com.itextpdf.text.pdf.BarcodeEAN;
 import com.itextpdf.text.pdf.PdfContentByte;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 /**
  * Contains logic to generate a pdf of barcodes for a list of items
  * 
  * @author Matthew
  * 
  * @invariant itemsToPrint != null
  */
 public class BarcodePrinter {
 
 	private static BarcodePrinter _instance;
 
 	/**
 	 * Allows global access to singleton BarcodePrinter instance
 	 * 
 	 * @return singleton instance of BarcodePrinter
 	 * 
 	 * @pre true
 	 * @post _instance != null
 	 */
 	public static BarcodePrinter getInstance() {
 		if (_instance == null)
 			_instance = new BarcodePrinter();
 
 		return _instance;
 	}
 
 	private final String saveFolder = "labels";
 
 	private List<Item> itemsToPrint;
 
 	private BarcodePrinter() {
 		itemsToPrint = new ArrayList<Item>();
 	}
 
 	/**
 	 * Adds an item to a list of items to be printed with the next printBatch() command
 	 * 
 	 * @param i
 	 *            Item to be added to list
 	 * 
 	 * @pre i != null
 	 * @post i in itemsToPrint
 	 */
 	public void addItemToBatch(Item i) {
 		itemsToPrint.add(i);
 	}
 
	public boolean hasItemsToPrint() {
		return itemsToPrint.size() > 0;
	}

 	/**
 	 * Prints a pdf of Items in this batch (added by addItemToBatch()). Creates a unique
 	 * filename to save this batch of barcodes to. Filename is of the format labels-(Date).pdf
 	 * and is saved in the "Labels" directory. If the director does not exist, the directory
 	 * will first be created.
 	 * 
 	 * @return NonEmptyString filename where the batch was saved
 	 * 
 	 * @pre true
 	 * @post itemsToPrint.size() == 0
 	 * @post new pdf saved to Labels directory
 	 */
 	public NonEmptyString printBatch() {
 		Date now = new Date();
 		String monthString = Integer.toString(now.getMonth() + 1);
 		if (monthString.length() == 1) {
 			monthString = "0" + monthString;
 		}
 		String filename = "labels-" + monthString + now.getDate() + (now.getYear() + 1900)
 				+ now.getHours() + now.getMinutes() + now.getSeconds() + ".pdf";
 		try {
 			createDocument(filename);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (DocumentException e) {
 			e.printStackTrace();
 		}
 
 		itemsToPrint.clear();
		return new NonEmptyString(saveFolder + "/" + filename);
 	}
 
 	private void createDocument(String filename) throws FileNotFoundException,
 			DocumentException {
 
 		// verify that the folder ./labels/ exists
 		File file = new File(saveFolder);
 		if (!file.exists()) {
 			boolean success = file.mkdirs();
 			if (!success)
				throw new FileNotFoundException("Unable to find or create " + saveFolder
						+ " directory");
 		}
 
 		filename = saveFolder + "/" + filename;
 
 		// step 1
 		Document document = new Document();
 		// step 2
 		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
 		// step 3
 		document.open();
 		// step 4
 
 		PdfPTable table = new PdfPTable(5);
 		table.setWidthPercentage(100);
 
 		PdfContentByte cb = writer.getDirectContent();
 		float documentWidth = document.getPageSize().getWidth() - document.leftMargin()
 				- document.rightMargin();
 		float cellWidth = documentWidth / table.getNumberOfColumns() - 20;
 
 		for (Item i : itemsToPrint) {
 			table.addCell(drawBarcode(i, cb, cellWidth));
 		}

		if ((itemsToPrint.size() % 5) < table.getNumberOfColumns()) {
			int emptyColumns = table.getNumberOfColumns() - (itemsToPrint.size() % 5);
			for (int i = 0; i < emptyColumns; i++) {
 				PdfPCell emptyCell = new PdfPCell();
 				emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
 				emptyCell.setBorder(Rectangle.NO_BORDER);
 				emptyCell.setPaddingTop(15);
 				emptyCell.setPaddingBottom(15);
 				emptyCell.setPaddingLeft(10);
 				emptyCell.setPaddingRight(10);
 				table.addCell(emptyCell);
 			}
 		}

 		document.add(table);
 		document.close();
 	}
 
 	private PdfPCell drawBarcode(Item i, PdfContentByte cb, float cellWidth) {
 		// Set Barcode
 		BarcodeEAN codeEAN = new BarcodeEAN();
 		codeEAN.setCodeType(Barcode.UPCA);
 		codeEAN.setCode(i.getBarcode());
 
 		// Create cell
 		PdfPCell cell = new PdfPCell();
 
 		// Set and size Description
 		String description = i.getProduct().getDescription();
 		Chunk desc = new Chunk(description);
 		desc.getFont().setSize(7);
 		while (desc.getWidthPoint() > cellWidth) {
 			description = description.substring(0, description.length() - 1);
 			desc = new Chunk(description);
 			desc.getFont().setSize(7);
 		}
 
 		// Add description
 		cell.addElement(desc);
 
 		// Create date string
 		String dateString = (i.getEntryDate().getMonth()) + "/" + i.getEntryDate().getDate()
 				+ "/" + (i.getEntryDate().getYear() - 100);
 
 		if (i.getExpirationDate() != null) {
 			dateString += " exp " + (i.getExpirationDate().getMonth()) + "/"
 					+ i.getExpirationDate().getDate() + "/"
 					+ (i.getExpirationDate().getYear() - 100);
 		}
 
 		// create data Paragraph
 		Paragraph date = new Paragraph(dateString);
 		date.setSpacingAfter(3);
 		date.getFont().setSize(7);
 
 		// Add date to cell
 		cell.addElement(date);
 
 		// add barcode
 		Image bc = codeEAN.createImageWithBarcode(cb, null, null);
 		cell.addElement(bc);
 
 		// format cell
 		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
 		cell.setBorder(Rectangle.NO_BORDER);
 		cell.setPaddingTop(15);
 		cell.setPaddingBottom(15);
 		cell.setPaddingLeft(10);
 		cell.setPaddingRight(10);
 		return cell;
 	}
 
 }
