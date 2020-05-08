 // Copyright (c) 2011 Martin Ueding <dev@martin-ueding.de>
 
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.text.Collator;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.LinkedList;
import java.util.logging.Logger;
 
 
 /**
  * A container for several NoteSheets.
  *
  * The NoteBook contains at least one NoteSheet and adds new sheets whenever
  * the forward() function is called. The whole notebook can be saved into
  * individual pictures.
  */
 public class NoteBook {
 	// TODO save sheets that are far away in the list to save RAM space then
 	// reload images when the user is going back in the history
 	private LinkedList<NoteSheet> sheets;
 
 	private int currentSheet = 0;
 
 	private File folder;
 
 	private NoteSheet current;
 
 	private int width;
 	private int height;
 
 	private ActionListener doneDrawing;
 
 	/**
 	 * Count of pages. Latest page number is pagecount.
 	 */
 	private int pagecount = 1;
 
 	private String name;
 
 	/**
 	 * Creates an empty note book with a single note sheet.
 	 *
 	 * @param width width of the individual sheets
 	 * @param height height of the individual sheets
 	 */
 	public NoteBook(int width, int height, File folder, String name) {
 		this.width = width;
 		this.height = height;
 
 		this.folder = folder;
 		this.name = name;
 
 		sheets = new LinkedList<NoteSheet>();
 
 		// if a notebook should be used
 		if (folder != null && name != null) {
 			if (!folder.exists()) {
 				folder.mkdirs();
 			}
 
 			// try to load all images that match the name
 			File[] allImages = folder.listFiles(new NoteSheetFileFilter(name));
 
 			if (allImages != null) {
 				Arrays.sort(allImages, new Comparator<File>() {
 
 					private Collator c = Collator.getInstance();
 
 					public int compare(File o1, File o2) {
 						if (o1 == o2) {
 							return 0;
 						}
 
 						File f1 = (File) o1;
 						File f2 = (File) o2;
 
 						if (f1.isDirectory() && f2.isFile()) {
 							return -1;
 						}
 						if (f1.isFile() && f2.isDirectory()) {
 							return 1;
 						}
 
 						return c.compare(f1.getName(), f2.getName());
 					}
 				});
 
 
 				// FIXME load pictures in correct order
 				for (File file : allImages) {
 					try {
 						System.out.println(String.format("adding sheet with file %s", file.getCanonicalPath()));
 					}
 					catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					sheets.add(new NoteSheet(width, height, pagecount++, file));
 				}
 			}
 
 		}
 
 		// add an empty sheet if the notebook would be empty otherwise
 		if (sheets.size() == 0) {
 			System.out.println("generating new sheet in empty notebook");
 			sheets.add(new NoteSheet(width, height, pagecount, generateNextFilename(pagecount)));
 			pagecount++;
 		}
 
 
 		updateCurrrentItem();
 	}
 
 	private File generateNextFilename(int pagenumber) {
 		if (folder != null && name != null) {
 			try {
 				return new File(folder.getCanonicalPath() + File.separator + name + "-" + String.format("%06d", pagenumber) + ".png");
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Draws a line onto the current sheet.
 	 */
 	public void drawLine(int x, int y, int x2, int y2) {
 		current.drawLine(x, y, x2, y2);
 
 		fireDoneDrawing();
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
 		if (sheets.size() > currentSheet + 1) {
 			currentSheet++;
 			System.out.println("advancing a sheet");
 		}
 		else if (current.touched()) {
 			System.out.println("adding a blank sheet");
 			sheets.add(new NoteSheet(width, height, pagecount, generateNextFilename(pagecount)));
 			currentSheet++;
 
 			pagecount++;
 		}
 		else {
 			System.out.println("you are at the last one and it is blank");
 			return;
 		}
 
 
 		if (currentSheet >= cacheWidth) {
 			sheets.get(currentSheet - cacheWidth).saveToFile();
 		}
 
 		updateCurrrentItem();
 		fireDoneDrawing();
 	}
 
 
 
 	/**
 	 * @return The number of pages given out so far.
 	 */
 	public int getPagecount() {
 		return pagecount;
 	}
 
 	/**
 	 * How many images to cache back and front.
 	 */
 	private int cacheWidth = 10;
 
 	/**
 	 * Goes back one sheet.
 	 */
 	public void backward() {
 		if (currentSheet > 0) {
 			if (currentSheet + cacheWidth < sheets.size()) {
 				System.out.println(String.format("%s + %s < %s", currentSheet, cacheWidth, sheets.size()));
 				sheets.get(currentSheet + cacheWidth).saveToFile();
 			}
 
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
 	 * Persists the whole notebook into individual files.
 	 */
 	public void saveToFiles() {
 		for (NoteSheet s : sheets) {
 			s.saveToFile();
 		}
 		quitWithWriteoutThread();
 
 	}
 
 	/**
 	 * @return number of sheets in the notebook
 	 */
 	public int getSheetCount() {
 		return sheets.size();
 	}
 
 	private void quitWithWriteoutThread() {
 		sheets.getFirst().stopWriteoutThread();
 
 	}
 
 	public String toString() {
 		return name;
 	}
 
 }
