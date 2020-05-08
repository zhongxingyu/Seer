 // Copyright © 2011 Martin Ueding <dev@martin-ueding.de>
 
 /*
  * This file is part of jscribble.
  *
  * jscribble is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 2 of the License, or (at your option)
  * any later version.
  *
  * jscribble is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * jscribble.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package jscribble.notebook;
 
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 
 import jscribble.NoteBookProgram;
 import jscribble.helpers.FileComparator;
 import jscribble.helpers.Localizer;
 import jscribble.helpers.Logger;
 import jscribble.helpers.SettingsWrapper;
 
 /**
  * A container for several NoteSheet.
  *
  * The NoteBook contains at least one NoteSheet and adds new sheets whenever
  * the forward() function is called. The whole NoteBook can be saved into
  * individual pictures.
  *
  * @author Martin Ueding <dev@martin-ueding.de>
  */
 public class NoteBook implements Comparable<NoteBook> {
 	// XXX This list cannot cope with running out of memory.
 	// The lists need to be able to shrink if you advance way into the
 	// notebook.
 	/**
 	 * A List with all the NoteSheet this NoteBook contains,
 	 */
 	private LinkedList<NoteSheet> sheets;
 
 	/**
 	 * The currently opened page.
 	 */
 	private int currentSheet = 0;
 
 	/**
 	 * The folder which contains the NoteSheet.
 	 */
 	private File folder;
 
 	/**
 	 * Size of the individual NoteSheet.
 	 */
 	private Dimension noteSize;
 
 	private Dimension noteSizeDefault = new Dimension(
 	    SettingsWrapper.getInteger("default_width", 1024),
 	    SettingsWrapper.getInteger("default_height", 600)
 	);
 
 	/**
 	 * Listener that needs to be notified after the current sheet is changed.
 	 */
 	private ActionListener doneDrawing;
 
 	/**
 	 * Count of pages. Latest page number is page count.
 	 */
 	private int pagecount = 1;
 
 	/**
 	 * How many images to cache back and front.
 	 */
 	private int cacheWidth = 10;
 
 	/**
 	 * The name of this NoteBook. It is also the folder name.
 	 */
 	private String name;
 
 	/**
 	 * Creates an empty note book with a single note sheet.
 	 *
 	 * @param noteSize size of the NoteSheet within the NoteBook
 	 * @param folder place to store images
 	 * @param name name of the NoteBook
 	 */
 	public NoteBook(String name) {
 		sheets = new LinkedList<NoteSheet>();
 
 		this.name = name;
 
 		// if a NoteBook should be used
 		if (name != null) {
 			folder = new File(NoteBookProgram.getDotDir().getAbsolutePath() +
 			        File.separator + name);
 			loadImagesFromFolder();
 		}
 		else {
 			name = UUID.randomUUID().toString();
 			folder = new File(System.getProperty("java.io.tmpdir") +
 			        File.separator + name);
 		}
 	}
 
 	/**
 	 * Add an empty sheet if the NoteBook would be empty otherwise.
 	 */
 	private void addPageIfEmpty() {
 		if (sheets.size() == 0) {
 			sheets.add(new NoteSheet(noteSize, pagecount,
 			           generateNextFilename(pagecount)));
 			pagecount++;
 			currentSheet = 0;
 		}
 	}
 
 	private void askForResolution() {
 		Dimension nativeSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 		if (!nativeSize.equals(noteSizeDefault) && JOptionPane.showConfirmDialog(
 		            null,
 		            String.format(
 		                    Localizer.get("Would you like to use your native resolution (%dx%d) instead of the default (%dx%d)?"),
 		                    (int) nativeSize.getWidth(),
 		                    (int) nativeSize.getHeight(),
 		                    (int) noteSizeDefault.getWidth(),
 		                    (int) noteSizeDefault.getHeight()
 		            ),
 		            Localizer.get("Default Resolution"),
 		            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 			noteSize = nativeSize;
 		}
 		else {
 			noteSize = noteSizeDefault;
 		}
 	}
 
 	@Override
 	public int compareTo(NoteBook other) {
 		return getName().compareTo(other.getName());
 	}
 
 	/**
 	 * Asks the user to delete the NoteBook.
 	 */
 	public boolean delete() {
 		int answer = JOptionPane.showConfirmDialog(null, String.format(
 		            Localizer.get("Do you really want to delete \"%s\"?"),
 		            name), Localizer.get("Really delete?"),
 		        JOptionPane.YES_NO_OPTION);
 
 		if (answer == 0) {
 			deleteSure();
 			return true;
 		};
 		return false;
 	}
 
 	/**
 	 * Delete the NoteBook from the file system.
 	 */
 	public void deleteSure() {
 		if (folder == null) {
 			return;
 		}
 
 		// Delete all the files in the folder.
 		File[] subfiles = folder.listFiles();
 		if (subfiles == null) {
 			return;
 		}
 
 		for (File file : subfiles) {
 			file.delete();
 		}
 
 		// Delete the folder itself.
 		folder.delete();
 
 		if (folder.exists()) {
 			Logger.log(getClass().getName(),
 			           Localizer.get("Could not delete folder."));
 		}
 	}
 
 	/**
 	 * Draws a line onto the current sheet.
 	 */
 	public void drawLine(int x, int y, int x2, int y2) {
 		getCurrentSheet().drawLine(x, y, x2, y2);
 
 		fireDoneDrawing();
 	}
 
 	public void eraseLine(int x, int y, int x2, int y2) {
 		getCurrentSheet().eraseLine(x, y, x2, y2);
 
 		fireDoneDrawing();
 	}
 
 	/**
 	 * Tell the listener (the DrawPanel) that the NoteBook has changed and
 	 * needs to be redrawn.
 	 */
 	private void fireDoneDrawing() {
 		if (doneDrawing != null) {
 			doneDrawing.actionPerformed(null);
 		}
 	}
 
 	/**
 	 * Generates the File for the next NoteSheet.
 	 *
 	 * @param pagenumber page number to use
 	 * @return File object with correct name
 	 */
 	private File generateNextFilename(int pagenumber) {
 		if (folder != null && name != null) {
 			try {
 				return new File(folder.getCanonicalPath() + File.separator +
 				        String.format("%06d", pagenumber) + ".png");
 			}
 			catch (IOException e) {
 				Logger.handleError(Localizer.get(
 				            "Could not determine path of NoteBook folder."));
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the NoteSheet object which the currently open page of the NoteBook.
 	 */
 	public NoteSheet getCurrentSheet() {
 		addPageIfEmpty();
 		if (currentSheet < 0 || currentSheet >= sheets.size()) {
 			Logger.log(getClass().getName(), "IndexOutOfBounds");
 		}
 		return sheets.get(currentSheet);
 	}
 
 	/**
 	 * Returns the name of the NoteBook.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Number of sheets in the NoteBook
 	 */
 	public int getSheetCount() {
 		// If there is no sheets list (should be impossible to achieve this),
 		// then there are no sheets in the NoteBook.
 		if (sheets == null) {
 			return 0;
 		}
 
 		// If there is a sheet in the NoteBook, but is has not been touched
 		// yet, count it as zero sheets.
 		if (sheets.size() == 1 && !sheets.get(0).touched()) {
 			return 0;
 		}
 
 		return sheets.size();
 	}
 
 	public Dimension getSize() {
 		if (noteSize == null) {
 			askForResolution();
 		}
 		return noteSize;
 	}
 
 	/**
 	 * Goes back one sheet.
 	 */
 	public void goBackwards() {
 		if (currentSheet > 0) {
 			if (currentSheet + cacheWidth < sheets.size()) {
 				sheets.get(currentSheet + cacheWidth).saveToFile();
 			}
 
 			currentSheet--;
 			fireDoneDrawing();
 		}
 	}
 
 	/**
 	 * Flip the pages forward. It creates a new page if needed. If the current
 	 * page is a blank page, no new blank page will be added.
 	 */
 	public void goForward() {
 		// If we are within the NoteBook, we just need to advance a step.
 		if (sheets.size() > currentSheet + 1) {
 			currentSheet++;
 		}
 		// If we are the last sheet and it has been touched, the user needs a
 		// new blank sheet. Add it.
 		else if (getCurrentSheet().touched()) {
 			sheets.add(new NoteSheet(noteSize, pagecount,
 			           generateNextFilename(pagecount)));
 			currentSheet++;
 
 			pagecount++;
 		}
 		// The NoteBook is at a blank sheet at its end. There is nothing to do.
 		else {
 			return;
 		}
 
 		// Page sheets that are far away to RAM.
 		if (currentSheet >= cacheWidth) {
 			sheets.get(currentSheet - cacheWidth).saveToFile();
 		}
 
 		fireDoneDrawing();
 	}
 
 	/**
 	 * Goes to the first page in the NoteBook.
 	 */
 	public void gotoFirst() {
 		currentSheet = 0;
 		fireDoneDrawing();
 	}
 
 	/**
 	 * Goes to the last page in the NoteBook.
 	 */
 	public void gotoLast() {
 		currentSheet = Math.max(0, sheets.size() - 1);
 		fireDoneDrawing();
 	}
 
 	/**
 	 * Loads the images from the previously set folder.
 	 */
 	private void loadImagesFromFolder() {
 		if (!folder.exists()) {
 			folder.mkdirs();
 
 			// Since the folder was just created, there are not going to be any
 			// pictures in it.
 			return;
 		}
 
 		// try to load all images that match the name
 		File[] allImages = folder.listFiles(new NoteSheetFileFilter());
 
 		if (allImages != null && allImages.length > 0) {
 			Arrays.sort(allImages, new FileComparator());
 
 
 			Pattern p = Pattern.compile("(\\d+)\\.png");
 
 			for (File file : allImages) {
 				String[] nameparts =
 				    file.getName().split(Pattern.quote(File.separator));
 				String basename = nameparts[nameparts.length-1];
 				Matcher m = p.matcher(basename);
 				if (m.matches()) {
 					pagecount = Math.max(pagecount,
 					            Integer.parseInt(m.group(1)));
 					sheets.add(new NoteSheet(noteSize,
 					           Integer.parseInt(m.group(1)), file));
 
 					if (pagecount == 0) {
 						try {
 							BufferedImage resolutionSampler = ImageIO.read(file);
 							noteSize = new Dimension(resolutionSampler.getWidth(), resolutionSampler.getHeight());
 						}
 						catch (IOException e) {
							Logger.handleError(Localizer.get("Unable to determine resolution from first image in NoteBook."));
 							askForResolution();
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 			pagecount++;
 		}
 	}
 
 	/**
 	 * Tells the WriteoutThread that this NoteBook has no more sheets to save.
 	 */
 	private void quitWithWriteoutThread() {
 		sheets.getFirst().stopWriteoutThread();
 
 	}
 
 	/**
 	 * Persists the whole NoteBook into individual files.
 	 */
 	public void saveToFiles() {
 		Logger.log(getClass().getName(), Localizer.get(
 		        "Starting to write out image files."));
 		for (NoteSheet s : sheets) {
 			s.saveToFile();
 		}
 		quitWithWriteoutThread();
 	}
 
 	/**
 	 * Sets an action listener to be called when something new was drawn.
 	 *
 	 * @param doneDrawing ActionListener to be called after drawing a new line.
 	 */
 	public void setDoneDrawing(ActionListener doneDrawing) {
 		this.doneDrawing = doneDrawing;
 	}
 
 	/**
 	 * Returns a string representation of the NoteBook, consisting of the name
 	 * and page count.
 	 */
 	public String toString() {
 		return String.format("%s (%d)", name, getSheetCount());
 	}
 }
