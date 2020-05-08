 import java.awt.event.ActionListener;
 import java.util.Calendar;
 import java.util.LinkedList;
 
 import javax.swing.JOptionPane;
 
 
 /**
  * A container for several NoteSheets.
  *
  * The NoteBook contains at least one NoteSheet and adds new sheets whenever
  * the forward() function is called. The whole notebook can be saved into
  * individual pictures.
  */
 public class NoteBook {
 	private LinkedList<NoteSheet> sheets;
 
 	private int currentSheet = 0;
 
 	private NoteSheet current;
 
 	private int width;
 	private int height;
 
 	private ActionListener doneDrawing;
 
 	/**
 	 * Creates an empty note book with a single note sheet.
 	 *
 	 * @param width width of the individual sheets
 	 * @param height height of the individual sheets
 	 */
 	public NoteBook(int width, int height) {
 		this.width = width;
 		this.height = height;
 
 		sheets = new LinkedList<NoteSheet>();
 
 		sheets.add(new NoteSheet(width, height));
 		updateCurrrentItem();
 	}
 
 	private boolean touched = false;
 
 	/**
 	 * Draws a line onto the current sheet.
 	 */
 	public void drawLine(int x, int y, int x2, int y2) {
 		current.drawLine(x, y, x2, y2);
 
 		fireDoneDrawing();
 
 		touched = true;
 	}
 
 	/**
 	 * Sets an action listener to be called when something new was drawn.
 	 *
 	 * @param doneDrawing ActionListener to be called after drawing a new line.
 	 */
 	public void setDoneDrawing(ActionListener doneDrawing) {
 		this.doneDrawing = doneDrawing;
 	}
 
 	private void fireDoneDrawing() {
 		if (doneDrawing != null) {
 			doneDrawing.actionPerformed(null);
 		}
 	}
 
 	/**
 	 * Flip the pages forward. It creates a new page if needed. If the current
 	 * page is a blank page, no new blank page will be added.
 	 */
 	public void forward() {
 		currentSheet++;
 		if (sheets.size() > currentSheet) {
 		}
 		else if (current.touched()) {
 			sheets.add(new NoteSheet(width, height));
 		}
 		else {
			// correct the advancing
			currentSheet--;
 			return;
 		}
 		updateCurrrentItem();
 		fireDoneDrawing();
 	}
 
 	/**
 	 * Goes back one sheet.
 	 */
 	public void backward() {
 		if (currentSheet > 0) {
 			currentSheet--;
 			updateCurrrentItem();
 			fireDoneDrawing();
 		}
 	}
 
 	public NoteSheet getCurrentSheet() {
 		return sheets.get(currentSheet);
 	}
 
 	private void updateCurrrentItem() {
 		assert(currentSheet >= 0);
 		assert(currentSheet < sheets.size());
 		current = sheets.get(currentSheet);
 	}
 
 	/**
 	 * Persists the whole notebook into individual files. The user is promted
 	 * for a basename, the current date is added to the front. Each sheet
 	 * suffixed with the page number, padded with as many zeros as needed.
 	 *
 	 * The filename will look like this:
 	 * YYMMDD-basename-001.png
 	 */
 	public void saveToFiles() {
 		if (!touched) {
 			return;
 		}
 
 		String basename = JOptionPane.showInputDialog("Please enter basename (press cancel to stop saving)");
 		if (basename == null) {
 			return;
 		}
 
 		Calendar now = Calendar.getInstance();
 
 		int maxnum = sheets.size() - 1;
 		int length = 1;
 		while (Math.pow(10, length) < maxnum + 1) {
 			length++;
 		}
 
 		for (NoteSheet s : sheets) {
 			String filename =
 			    String.format("%02d%02d%02d-%s-%s",
 			                  (now.get(Calendar.YEAR) % 100),
 			                  now.get(Calendar.MONTH),
 			                  now.get(Calendar.DAY_OF_MONTH),
 			                  basename,
 			                  String.format("%0" + length + "d", s.getPagenumber()) + ".png"
 			                 );
 			s.saveToFile(filename);
 		}
 	}
 
 	/**
 	 * @return number of sheets in the notebook
 	 */
 	public int getSheetCount() {
 		return sheets.size();
 	}
 
 }
