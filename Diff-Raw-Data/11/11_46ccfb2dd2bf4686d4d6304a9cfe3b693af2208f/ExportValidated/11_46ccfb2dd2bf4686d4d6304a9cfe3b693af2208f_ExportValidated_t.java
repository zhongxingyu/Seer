 package analysers;
 
 import ij.IJ;
 import ij.Prefs;
 import ij.gui.YesNoCancelDialog;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import util.Cell;
 import GUI.DisplayTrackingWindow;
 
 /** 
  * Copyright 2013 University of Warwick
  * 
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  *
  * This is a plugin for exporting all lineage data to a JSON file.
  *
  * @author Peter Krusche
  */
 public class ExportValidated extends AnalysisPlugin {
 	
 	/* (non-Javadoc)
 	 * @see analysers.AnalysisPlugin#setup()
 	 */
 	@Override
 	public void setup() {
 		FilterValidatedDialog fvd = new FilterValidatedDialog();
 		fvd.setCells(cells);
 		fvd.setVisible(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see analysers.AnalysisPlugin#analyze(util.Cell)
 	 */
 	@Override
 	public void analyze(Cell currentCell) {
 		JFileChooser fc = new JFileChooser();
 		fc.addChoosableFileFilter(
 				new FileNameExtensionFilter("Comma Separated Values", "csv")
 				);
 		fc.addChoosableFileFilter(
 				new FileNameExtensionFilter("JSON file", "json")
 			);
 		int rv = fc.showSaveDialog(null);
 		if (rv == JFileChooser.APPROVE_OPTION) {
 			int b = Prefs.getInt(".TrackApp.FilterValidatedDialog.exportMean", 1);
 			if(b != 0) {
 				IJ.log("Exporting mean intensity values.");				
 			} else {
 				IJ.log("Exporting sum of intensity values.");
 			}
 			
			File ff = fc.getSelectedFile();
 			String fn = fc.getSelectedFile().getAbsolutePath();
 			
 			if (fc.getFileFilter().getDescription().contains("JSON")) {
 				if (!fn.toLowerCase().endsWith(".json")) {
 					fn = fn + ".json";
					ff = new File(ff + ".json");
 				}
 				IJ.log("Writing a JSON file.");
 			} else {
 				if (!fn.toLowerCase().endsWith(".csv")) {
 					fn = fn + ".csv";
					ff = new File(ff + ".csv");
 				}				
 				IJ.log("Writing a CSV file.");
 			}
 			IJ.log("Writing to " + fn);
 			
 			if (fn.endsWith(".csv")) {
				writeCSV(ff);
 			} else if (fn.endsWith(".json")) {
				writeJSON(ff);				
 			} else {
 				IJ.log("Unknown output format for file name " + fn);
 			}
 
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see analysers.AnalysisPlugin#cellClicked(util.Cell)
 	 */
 	@Override
 	public void cellClicked(Cell currentCell) {
 		Cell a = findFirstAncestor(currentCell);
 		
 		String question = ""; 
 		boolean stat = false;
 		if (currentCell.isValidated()) {
 			question = "This cell is marked as validated. Do you really want to remove validation from  this lineage?";
 		} else {
 			stat = true;
 			question = "Validate cell?";
 		}
 		
 		YesNoCancelDialog yncd = new YesNoCancelDialog(null,
 				"Toggle Validation", 
 				question + "  " +  
 				"This cell: " + currentCell.getCellID() 
 					+ ":X=" + currentCell.getX() + ",Y=" + currentCell.getY() + "   " + 
 				"Ancestor : " + a.getCellID() 
 					+ ":X=" + a.getX() + ",Y=" + a.getY() + "   " 
 					);
 		if(yncd.yesPressed()) {
 			IJ.log("Changing " + a.toString());
 			// if we have a display window make sure the ROIs get updated too.
 			if (screen instanceof DisplayTrackingWindow) {
 				DisplayTrackingWindow dtw = (DisplayTrackingWindow) screen;
 				validateCell(a, stat);				
 				// redraw
 				dtw.updateAllDisplay();
 				IJ.log("Done!");
 			} else {
 				validateCell(a, stat);
 			}
 		}
 	}
 
 	/**
 	 * Invalidate a cell
 	 * 
 	 * @param a the cell
 	 * @param stat the status (true => valid)
 	 */
 	public void validateCell(Cell a, boolean stat) {
 		// track back to parent
         if (a == null) {
             return;
         }
         while (a.getPreviousCell() != null) {
             a = a.getPreviousCell();
         }
 		while (a != null) {
 			a.setValidated(stat);
 			if(a.getDaughterCell() != null) {
 				validateCell(a.getDaughterCell(), stat);
 			}
 			a = a.getNextCell();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see analysers.AnalysisPlugin#getName()
 	 */
 	@Override
 	public String getName() {
 		return "Export all validated Cells";
 	}
 
 	/** 
 	 * Find the first ancestor of a cell in a lineage.
 	 * @param c The cell 
 	 * @return the first ancestor of c
 	 */
 	public static Cell findFirstAncestor(Cell c) {
 		Cell anc = c;
 		
 		while (anc.getPreviousCell() != null) {
 			anc = anc.getPreviousCell();
 		}
 		return anc;
 	}
 	
 	/**
 	 * Write the validated lineages as JSON files
      * 
 	 * @param f the file name
 	 */
 	@SuppressWarnings("unchecked")
 	private void writeJSON(File f) {
 		HashMap<String, Integer> lineagesDone = new HashMap<String, Integer>();
 		int nlin = 1;
 		int linsWritten = 0;
 		int cellsWritten = 0;
 		JSONArray jsa = new JSONArray();
 
 		FilterValidatedDialog fvd = new FilterValidatedDialog();
 		fvd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		fvd.setCells(cells);
 
 		for (Cell c : cells) {
 			if (c.isValidated()) {
 				Cell a = findFirstAncestor(c);
 				if (lineagesDone.get(""+a.getCellID()) == null) {
 					lineagesDone.put(""+a.getCellID(), new Integer(nlin++));						
 					if (fvd.accepts(a)) {
 						JSONObject lino = makeLineageJSON(a, nlin);
 						jsa.add( lino );
 						cellsWritten+= ((JSONArray)lino.get("cells")).size();
 						linsWritten++;
 					}
 				}
 			}
 		}
 		
 		IJ.log("Done, " + linsWritten + " lineages, and " + cellsWritten + " cells were exported.");
 
 		fvd.dispose();
 		
 		try {
 			FileWriter fw = new FileWriter(f);
 			jsa.writeJSONString(fw);
 			fw.close();
 		} catch (IOException e) {
 			IJ.log("Failed to write file!");
 			e.printStackTrace();
 		}		
 	}
 
 	
 	/**
 	 * Write the validated lineages as CSV files
      * 
 	 * @param f the file name
 	 */
 	
 	private void writeCSV(File f) {
 		HashMap<String, Integer> lineagesDone = new HashMap<String, Integer>();
 		int nlin = 1;
 		int linsWritten = 0;
 		int cellsWritten = 0;
 		String sep = ", ";
 		double dt = exp.getFrameInterval();
 		
 		try {
 			FileWriter fw = new FileWriter(f);
 			BufferedWriter bw = new BufferedWriter(fw);
 			
 			int maxframe = 1;
 			for (Cell c : cells) {
 				if(c.getFrame() > maxframe) {
 					maxframe = c.getFrame();
 				}
 			}
 
 			// 1. write time axis
 			bw.write("Lineage"       + sep +	
 					 "Cell"          + sep + 
 					 "First Frame"   + sep + 
 					 "Last Frame"    + sep + 
 					 "FirstX"        + sep + 
 					 "FirstY"        + sep + 
 					 "Area"          + sep + 
 					 "Start Time"    + sep +  
 					 "End Time"      + sep 
 					 );
 			bw.write("Time");
 			for (int fr = 1; fr <= maxframe; ++fr) {
 				bw.write(sep + ((fr-1) * dt ));
 			}
 			bw.write("\n");
 
 			FilterValidatedDialog fvd = new FilterValidatedDialog();
 			fvd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			fvd.setCells(cells);
 	
 			for (Cell c : cells) {
 				if (c.isValidated()) {
 					Cell a = findFirstAncestor(c);
 					if (lineagesDone.get(""+a.getCellID()) == null) {
 						lineagesDone.put(""+a.getCellID(), new Integer(nlin++));
 						if(fvd.accepts(a)) {
 							JSONObject lino = makeLineageJSON(a, nlin);
 																			
 							JSONArray cells = (JSONArray) lino.get("cells");
 							for (@SuppressWarnings("rawtypes")
 							Iterator cit = cells.iterator(); cit
 									.hasNext();) {
 								JSONObject co = (JSONObject) cit.next();
 								String name = co.get("name").toString();
 								String firstx = co.get("first_x").toString();
 								String firsty = co.get("first_y").toString();
 								String carea = co.get("area").toString();
 								
 								int start = ((Integer)co.get("start_index")).intValue();
 								String prefix = "";
 								for (int i = 1; i < start; ++i) {
 									prefix += sep + "0";
 								}
 															
 								JSONArray ad = (JSONArray) co.get("data");
 								int i = 1;
 								for (@SuppressWarnings("rawtypes")
 								Iterator itad = ad.iterator(); itad
 										.hasNext();) {
 									JSONArray da = (JSONArray) itad.next();
 									int end = start + da.size() - 1;
 									bw.write("" + nlin);
 									bw.write(sep + name);
 									bw.write(sep + start);
 									bw.write(sep + end);
 									bw.write(sep + firstx);
 									bw.write(sep + firsty);
 									bw.write(sep + carea);
 									bw.write(sep + ((start-1)*dt));
 									bw.write(sep + ((end-1)*dt));
 									bw.write(sep + "Channel " + i + prefix);
 									
 									i++;
 									for (@SuppressWarnings("rawtypes")
 									Iterator itda = da.iterator(); itda
 											.hasNext();) {
 										bw.write(sep + itda.next().toString());
 									}
 									for (int k = end+1; k <= maxframe; ++k) {
 										bw.write(sep + "0");
 									}
 									bw.write("\n");
 								}
 								++cellsWritten;
 							}
 							++linsWritten;
 						}
 					}
 				}
 			}
 			fvd.dispose();
 			bw.close();
 			fw.close();
 			IJ.log("Done, " + linsWritten + " lineages, and " + cellsWritten + " cells were exported.");
 		} catch (IOException e) {
 			IJ.log("Failed to write file!");
 			e.printStackTrace();
 		}		
 	}
 
 	/**
 	 * Make JSON Object for a lineage
 	 * @param a the ancestral cell of the lineage
 	 * @param nlin the lineage number
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private JSONObject makeLineageJSON(Cell a, int nlin) {
 		double dt = exp.getFrameInterval();
 		JSONArray cs = new JSONArray();
 		JSONObject l = new JSONObject();
 		
 		l.put("dt", dt);
 		// also remember the intensity value type
 		l.put("mean_intensities", Prefs.getInt(".TrackApp.FilterValidatedDialog.exportMean", 1));
 		
 		ArrayList<Cell> cells = new ArrayList<Cell>();
 		HashMap<Integer, String> cellNames = new HashMap<Integer, String>();
 		JSONArray divns = new JSONArray();
 		cells.add(a);
 		getAllDaughters(a, cells, cellNames, null, divns);
 		
 		l.put("divisions", divns);
 		
 		int cellid = 1;
 		HashMap<String, Integer> cellsToCellsInLin = new HashMap<String, Integer>();
 		
 		int minframe = -1;
 		int maxframe = -1;
 		
 		for (Iterator<Cell> i = cells.iterator(); i.hasNext();) {
 			Cell cell = (Cell) i.next();
 			
 			if(minframe < 0 || cell.getFrame() < minframe) {
 				minframe = cell.getFrame();
 			}
 			int clf = findLastFrame(cell);
 			if(maxframe < 0 || clf > maxframe) {
 				maxframe = clf;
 			}
 			
 			cellsToCellsInLin.put(""+cell.getCellID(), 
 					new Integer(cellid));
 			++cellid;
 		}
 		
 		// make lineage time axis (minframe -> maxframe)
 		JSONArray lintime = new JSONArray();
 		for(int f = minframe; f <= maxframe; ++f) {
 			lintime.add(new Double( (f-1) * dt ) );
 		}
 		l.put("time", lintime);
 		
 		cellid = 1;
 		for (Iterator<Cell> i = cells.iterator(); i.hasNext();) {
 			Cell cell = (Cell) i.next();
 			JSONObject co = makeCellJSON(cell);
 			co.put("lineage", new Integer(nlin));
 			
 			int parent = 0;
 			// parent is first cell back for which we have an id, or 0
 			Cell pcell = cell.getPreviousCell();
 			Cell ppcell = pcell;
 			int f = 0;
 			while(pcell != null) {
 				int id = pcell.getCellID();
 				Integer pi = cellsToCellsInLin.get(""+id);
 				if (pi != null) {
 					parent = pi.intValue();
 					break;
 				}
 				ppcell = pcell;
 				pcell = pcell.getPreviousCell();
 				if (f++ > maxframe) {
 					IJ.log("Failed to find parent for cell " + cell.getCellID());
 					break;
 				}
 			}
 			if (cellid != 1 && parent == 0) {
 				IJ.log("Error: parent is null within lineage!" + ppcell.toString());
 			}
 			
 			co.put("parent", new Integer(parent));
 			co.put("parent_in_lineage", new Integer(parent));
 			
 			co.put("lineage_id", new Integer(cellid));
 			co.put("name", cellNames.get(cell.getCellID()));
 			
 			cs.add(co);
 			++cellid;
 		}
 
 		JSONObject o = new JSONObject();
 		o.put("lin", l);
 		o.put("cells", cs);
 		
 		return o;
 	}
 	
 	/**
 	 * Make the time axis for a given cell
 	 * @param c
 	 * @return a JSON array containing all time values
 	 */
 	@SuppressWarnings("unchecked")
 	private JSONArray makeCellTimeAxis(Cell c) {
 		JSONArray a = new JSONArray();
 		
 		while (c != null) {
 			double t = (c.getFrame()-1) * exp.getFrameInterval();
 			a.add(t);
 			c = c.getNextCell();
 		}
 		return a;
 	}
 	
 	/**
 	 * Make the data array for a given cell
 	 * @param c
 	 * @return a JSON array containing all time values
 	 */
 	@SuppressWarnings("unchecked")
 	private JSONArray makeCellData(Cell c) {
 		boolean meanInt = true;
 		
 		if(screen instanceof DisplayTrackingWindow) {
 			int b = Prefs.getInt(".TrackApp.FilterValidatedDialog.exportMean", 1);
 			meanInt = b != 0;
 		}
 		
 		JSONArray [] q = new JSONArray [c.getIntensity().length];
 		for (int i = 0; i < q.length; i++) {
 			q[i] = new JSONArray();
 		}
 		
 		while (c != null) {
 			double [] ints = c.getIntensity();
 			double ar = c.getArea();
 			int lm = Math.min(q.length, ints.length);
 			if(lm != ints.length) {
 				IJ.log("Warning : number of channels varies, inconsistent cell dataset.");
 			}
 			for (int i = 0; i < ints.length; i++) {
 				if(meanInt) {
 					q[i].add(new Double(ints[i]/ar));
 				} else {
 					q[i].add(new Double(ints[i]));
 				}
 			}
 			c = c.getNextCell();
 		}
 		JSONArray a = new JSONArray();
 		for (int i = 0; i < q.length; i++) {
 			a.add(q[i]);
 		}
 		return a;
 	}
 	
 	/**
 	 * Make JSON object for a given cell
 	 * @param c the first cell
 	 * @return the JSON object for this cell
 	 */
 	@SuppressWarnings("unchecked")
 	private JSONObject makeCellJSON(Cell c) {
 		JSONObject o = new JSONObject();
 		o.put ("dt", new Double(exp.getFrameInterval()));
 		o.put ("time", makeCellTimeAxis(c));
 		o.put("start_index", c.getFrame());
 		o.put("nseries", new Integer(c.getIntensity().length));
 		o.put("data", makeCellData(c));
 		o.put("description", c.toString());
 		o.put("first_x", c.getX());
 		o.put("first_y", c.getY());
 		o.put("area", c.getArea());
 		return o;		
 	}
 	
 	/**
 	 * Make a list of all daughter cells.
 	 * 
 	 * Also fix the lineage links.
 	 * 
 	 * @param a The ancestral cell
 	 * @param l a list of all daughter cells
 	 * @param names a map of cell names
 	 * @param pname the name prefix
 	 * @param divns JSONArray of division frames
 	 */
 	@SuppressWarnings("unchecked")
 	public static void getAllDaughters(Cell a, 
 			List<Cell> l, 
 			Map<Integer, String> names, 
 			String pname, 
 			JSONArray divns) {
 		int k = 1;
 		if(pname == null) {
 			pname = "Cell 1";
 		}
 		names.put(new Integer(a.getCellID()), pname);
 		Cell p = null;
 		while(a != null) {
 			// fix links
 			if( p != null ) {
 				a.setPreviousCell(p);
 				p.setNextCell(a);
 			}
 			Cell d = a.getDaughterCell();
 			if(d != null) {
 				// fix, just in case.
 				d.setPreviousCell(a);
 				divns.add(new Integer(a.getFrame()+1));
 				l.add(d);
 				getAllDaughters(d, l, names, pname + "-" + k, divns);
 				k++;
 			}
 			p = a;
 			a = a.getNextCell();
 		}
 	}
 	
 	/**
 	 * Find the last frame for a cell
 	 * @param c
 	 * @return the last frame for this cell
 	 */
 	public static int findLastFrame(Cell c) {
 		int f = 1;
 		while(c != null) {
 			f = c.getFrame();
 			c = c.getNextCell();
 		}
 		return f;
 	}
 	
 }
