 /* -*- tab-width: 4 -*-
  *
  * Electric(tm) VLSI Design System
  *
  * File: Output.java
  *
  * Copyright (c) 2003 Sun Microsystems and Static Free Software
  *
  * Electric(tm) is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * Electric(tm) is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Electric(tm); see the file COPYING.  If not, write to
  * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  * Boston, Mass 02111-1307, USA.
  */
 package com.sun.electric.tool.io.output;
 
 import com.sun.electric.database.hierarchy.Cell;
 import com.sun.electric.database.hierarchy.Export;
 import com.sun.electric.database.hierarchy.Library;
 import com.sun.electric.technology.ArcProto;
 import com.sun.electric.database.prototype.NodeProto;
 import com.sun.electric.database.text.Pref;
 import com.sun.electric.database.text.TextUtils;
import com.sun.electric.database.text.Version;
 import com.sun.electric.database.topology.ArcInst;
 import com.sun.electric.database.topology.Connection;
 import com.sun.electric.database.topology.NodeInst;
 import com.sun.electric.database.topology.PortInst;
 import com.sun.electric.database.variable.ElectricObject;
 import com.sun.electric.database.variable.TextDescriptor;
 import com.sun.electric.database.variable.VarContext;
 import com.sun.electric.database.variable.Variable;
 import com.sun.electric.technology.PrimitiveNode;
 import com.sun.electric.technology.PrimitivePort;
 import com.sun.electric.technology.Technology;
 import com.sun.electric.technology.technologies.Generic;
 import com.sun.electric.tool.Listener;
 import com.sun.electric.tool.Tool;
 import com.sun.electric.tool.Job;
 import com.sun.electric.tool.io.IOTool;
 import com.sun.electric.tool.io.FileType;
 import com.sun.electric.tool.io.input.Input;
 import com.sun.electric.tool.user.ui.EditWindow;
 import com.sun.electric.tool.user.ui.TopLevel;
 
 import java.awt.geom.Rectangle2D;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.swing.JOptionPane;
 
 
 /**
  * This class manages writing files in different formats.
  * The class is subclassed by the different file writers.
  */
 public class Output
 {
     /**
      * This is the non-interactive version of exportCellCommand
      * @param cell the Cell to be written.
      * @param context the VarContext of the Cell (its position in the hierarchy above it).
      * @param filePath the path to the disk file to be written.
      * @param type the format of the output file.
      * @param startJob to start job immediately. In case of regressions, job is manually started.
      * @return Job representing this task
     */
     public static OutputCellInfo exportCellCommand(Cell cell, VarContext context, String filePath,
                                                    FileType type, boolean startJob)
     {
         Job.Type jtype = Job.Type.EXAMINE;
         if (type == FileType.EDIF) jtype = Job.Type.CHANGE;
         return (new OutputCellInfo(cell, context, filePath, type, startJob, jtype));
     }
 
     static class OrderedConnections implements Comparator
 	{
 		public int compare(Object o1, Object o2)
 		{
 			Connection c1 = (Connection)o1;
 			Connection c2 = (Connection)o2;
 			int i1 = c1.getPortInst().getPortProto().getPortIndex();
 			int i2 = c2.getPortInst().getPortProto().getPortIndex();
 			int cmp = i1 - i2;
 			if (cmp != 0) return cmp;
 			cmp = c1.getArc().getArcIndex() - c2.getArc().getArcIndex();
 			if (cmp != 0) return cmp;
 			return c1.getEndIndex() - c2.getEndIndex();
 		}
 	}
 
 	static class OrderedExports implements Comparator
 	{
 		public int compare(Object o1, Object o2)
 		{
 			Export e1 = (Export)o1;
 			Export e2 = (Export)o2;
 			int i1 = e1.getOriginalPort().getPortProto().getPortIndex();
 			int i2 = e2.getOriginalPort().getPortProto().getPortIndex();
 			int cmp = i1 - i2;
 			if (cmp != 0) return cmp;
 			return e1.getPortIndex() - e2.getPortIndex();
 		}
 	}
 
 	/** connections comparator for writeNodeInst */		static final Comparator CONNECTIONS_ORDER = new OrderedConnections();
 	/** exports comparator for writeNodeInst */			static final Comparator EXPORTS_ORDER = new OrderedExports();
 
 	/** file path */									protected String filePath;
 	/** for writing text files */						protected PrintWriter printWriter;
 	/** for writing binary files */						protected DataOutputStream dataOutputStream;
 	/** Map of referenced objects for library files */	HashMap objInfo;
 	/** Maps memory face index to disk face index */	int[] faceMap;
 	/** Name space of variable names */					TreeMap/*<String,Integer>*/ nameSpace;
 
 	public Output()
 	{
 	}
 
 	/**
 	 * Method to write a Library.
 	 * This method is never called.
 	 * Instead, it is always overridden by the appropriate write subclass.
 	 * @param lib the Library to be written.
      * @return true on error.
 	 */
 	protected boolean writeLib(Library lib) { return true; }
 
     /**
      * Method to write a cell.
      * This method is never called.
      * Instead, it is always overridden by the appropriate write subclass.
      * @param cell the Cell to be written.
      * @param context the VarContext of the cell (for parameter evaluation)
      * @return true on error.
      */
     protected boolean writeCell(Cell cell, VarContext context) { return true; }
 
 	/**
 	 * Method to write an entire Library with a particular format.
 	 * This is used for output formats that capture the entire library
 	 * (only the ELIB and Readable Dump formats).
 	 * The alternative to writing the entire library is writing a single
 	 * cell and the hierarchy below it (use "writeCell").
 	 * @param lib the Library to be written.
 	 * @param type the format of the output file.
 	 * @param compatibleWith6 true to write a library that is compatible with version 6 Electric.
      * @return true on error.
 	 */
 	public static boolean writeLibrary(Library lib, FileType type, boolean compatibleWith6)
 	{
 		Output out;
 
         // scan for Dummy Cells, warn user that they still exist
         List dummyCells = new ArrayList();
         dummyCells.add("WARNING: "+lib+" contains the following Dummy cells:");
         for (Iterator it = lib.getCells(); it.hasNext(); ) {
             Cell c = (Cell)it.next();
             if (c.getVar(Input.IO_DUMMY_OBJECT) != null) {
                 dummyCells.add("   "+c.noLibDescribe());
             }
         }
         if (dummyCells.size() > 1) {
             dummyCells.add("Do you really want to write this library?");
             Object [] options = {"Continue Writing", "Cancel" };
             int val = JOptionPane.showOptionDialog(TopLevel.getCurrentJFrame(), dummyCells.toArray(),
                     "Dummy Cells Found in "+lib, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
                     options, options[1]);
             if (val == 1) return true;
         }
 
 		// make sure that all "meaning" options are attached to the database
 //		Pref.installMeaningVariables();
 		
 		// make sure that this library save is announced
 		for(Iterator it = Tool.getListeners(); it.hasNext(); )
 		{
 			Listener listener = (Listener)it.next();
 			listener.writeLibrary(lib);
 		}
 
 		// make sure all technologies with irrelevant scale information have the same scale value
 		double largestScale = 0;
 		for(Iterator it = Technology.getTechnologies(); it.hasNext(); )
 		{
 			Technology tech = (Technology)it.next();
 			if (tech.isScaleRelevant()) continue;
 			if (tech == Generic.tech) continue;
 			if (tech.getScale() > largestScale) largestScale = tech.getScale();
 		}
 		for(Iterator it = Technology.getTechnologies(); it.hasNext(); )
 		{
 			Technology tech = (Technology)it.next();
 			if (tech.isScaleRelevant()) continue;
 			if (tech == Generic.tech) continue;
 			tech.setScale(largestScale);
 		}
 
 		// handle different file types
 		URL libFile = lib.getLibFile();
 		if (libFile == null)
 			libFile = TextUtils.makeURLToFile(lib.getName());
 
 		// make the proper output file name
 		String properOutputNameWithoutExtension = TextUtils.getFilePath(libFile) + TextUtils.getFileNameWithoutExtension(libFile);
 		String properOutputName = properOutputNameWithoutExtension;
 		if (type == FileType.ELIB) properOutputName += ".elib";
 		if (type == FileType.JELIB) properOutputName += ".jelib";
 		if (type == FileType.READABLEDUMP) properOutputName += ".txt";
 		if (type == FileType.ELIB || type == FileType.JELIB)
 		{
 			// backup previous files if requested
 			int backupScheme = IOTool.getBackupRedundancy();
 			if (backupScheme == 1)
 			{
 				// one-level backup
 				File newFile = new File(properOutputName);
 				if (newFile.exists())
 				{
 					String backupFileName = properOutputName + "~";
 					File oldFile = new File(backupFileName);
 					boolean canRename = true;
 					if (oldFile.exists())
 					{
 						if (!oldFile.delete())
 						{
 							System.out.println("Unable to delete former library file " + oldFile);
 							canRename = false;
 						}
 					}
 					if (canRename)
 					{
 						if (!newFile.renameTo(oldFile))
 						{
 							System.out.println("Unable to rename " + newFile + " to " + oldFile);
 						}
 					}
 				}
 			} else if (backupScheme == 2)
 			{
 				// full-history backup
 				File newFile = new File(properOutputName);
 				if (newFile.exists())
 				{
 					long modified = newFile.lastModified();
 					Date modifiedDate = new Date(modified);
 					SimpleDateFormat sdf = new SimpleDateFormat("-yyyy-MM-dd");
 					for(int i=0; i<1000; i++)
 					{
 						String backupFileName = properOutputNameWithoutExtension + sdf.format(modifiedDate);
 						if (i != 0)
 							backupFileName += "--" + i;
 						backupFileName += "." + type.getExtensions()[0];
 						File oldFile = new File(backupFileName);
 						if (oldFile.exists()) continue;
 						if (!newFile.renameTo(oldFile))
 						{
 							System.out.println("Unable to rename " + newFile + " to " + oldFile);
 						}
 						break;
 					}
 				}
 			}
 			if (type == FileType.ELIB)
 			{
 				ELIB elib = new ELIB();
 				if (compatibleWith6) elib.write6Compatible();
 				out = (Output)elib;
 				if (out.openBinaryOutputStream(properOutputName)) return true;
 				if (out.writeLib(lib)) return true;
 				if (out.closeBinaryOutputStream()) return true;
 			} else
 			{
 				JELIB jelib = new JELIB();
 				out = (Output)jelib;
 				if (out.openTextOutputStream(properOutputName)) return true;
 				if (out.writeLib(lib)) return true;
 				if (out.closeTextOutputStream()) return true;
 			}
  		} else if (type == FileType.READABLEDUMP)
 		{
 			out = (Output)new ReadableDump();
 			if (out.openTextOutputStream(properOutputName)) return true;
 			if (out.writeLib(lib)) return true;
 			if (out.closeTextOutputStream()) return true;
 		} else
 		{
 			System.out.println("Unknown export type: " + type);
 			return true;
 		}
        // Update the version in library read in memomy
        lib.setVersion(Version.getVersion());
 		return false;
 	}
     
     /**
      * Method to write a Cell to a file with a particular format.
 	 * In addition to the specified Cell, these formats typically
 	 * also include the hierarchy below it.
 	 * The alternative is to write the entire library, regardless of
 	 * hierarchical structure (use "WriteLibrary").
      * @param cellJob contains following information
      * cell: the Cell to be written.
      * context: the VarContext of the Cell (its position in the hierarchy above it).
      * filePath: the path to the disk file to be written.
      * type: the format of the output file.
      */
     private static void writeCell(OutputCellInfo cellJob) //Cell cell, VarContext context, String filePath, FileType type, Job job)
     {
 		if (cellJob.type == FileType.ARCHSIM)
 		{
 			ArchSim.writeArchSimFile(cellJob);
 		} else if (cellJob.type == FileType.CDL)
 		{
 			Spice.writeSpiceFile(cellJob, true);
 		} else if (cellJob.type == FileType.CIF)
 		{
 			CIF.writeCIFFile(cellJob);
 		} else if (cellJob.type == FileType.COSMOS)
 		{
 			Sim.writeSimFile(cellJob);
 		} else if (cellJob.type == FileType.DXF)
 		{
 			DXF.writeDXFFile(cellJob);
 		} else if (cellJob.type == FileType.EAGLE)
 		{
 			Eagle.writeEagleFile(cellJob);
 		} else if (cellJob.type == FileType.ECAD)
 		{
 			ECAD.writeECADFile(cellJob);
 		} else if (cellJob.type == FileType.EDIF)
 		{
 			EDIF.writeEDIFFile(cellJob);
 		} else if (cellJob.type == FileType.ESIM || cellJob.type == FileType.RSIM)
 		{
 			Sim.writeSimFile(cellJob);
 		} else if (cellJob.type == FileType.FASTHENRY)
 		{
 			FastHenry.writeFastHenryFile(cellJob);
 		} else if (cellJob.type == FileType.HPGL)
 		{
 			HPGL.writeHPGLFile(cellJob);
 		} else if (cellJob.type == FileType.GDS)
 		{
 			GDS.writeGDSFile(cellJob);
 		} else if (cellJob.type == FileType.IRSIM)
 		{
 			IRSIM.writeIRSIMFile(cellJob);
 		} else if (cellJob.type == FileType.L)
 		{
 			L.writeLFile(cellJob);
 		} else if (cellJob.type == FileType.LEF)
 		{
 			LEF.writeLEFFile(cellJob);
 		} else if (cellJob.type == FileType.MAXWELL)
 		{
 			Maxwell.writeMaxwellFile(cellJob);
 		} else if (cellJob.type == FileType.MOSSIM)
 		{
 			MOSSIM.writeMOSSIMFile(cellJob);
 		} else if (cellJob.type == FileType.PADS)
 		{
 			Pads.writePadsFile(cellJob);
 		} else if (cellJob.type == FileType.PAL)
 		{
 			PAL.writePALFile(cellJob);
 		} else if (cellJob.type == FileType.POSTSCRIPT || cellJob.type == FileType.EPS)
 		{
 			PostScript.writePostScriptFile(cellJob);
 		} else if (cellJob.type == FileType.SILOS)
 		{
 			Silos.writeSilosFile(cellJob);
 		} else if (cellJob.type == FileType.SKILL)
 		{
 			IOTool.writeSkill(cellJob.cell, cellJob.filePath, false);
         } else if (cellJob.type == FileType.SKILLEXPORTSONLY)
         {
             IOTool.writeSkill(cellJob.cell, cellJob.filePath, true);
 		} else if (cellJob.type == FileType.SPICE)
 		{
 			Spice.writeSpiceFile(cellJob, false);
 		} else if (cellJob.type == FileType.TEGAS)
 		{
 			Tegas.writeTegasFile(cellJob);
 		} else if (cellJob.type == FileType.VERILOG)
 		{
 			Verilog.writeVerilogFile(cellJob);
 		}
     }
 
 	/**
 	 * Gathers into objInfo map all objects references from library objects.
 	 * @param lib the Library to examine.
 	 * @param needVars true if variable name space and font information is necessary
 	 */
 	void gatherReferencedObjects(Library lib, boolean needVars)
 	{
 		objInfo = new HashMap();
 		nameSpace = needVars ? new TreeMap/*<String,Integer>*/(TextUtils.STRING_NUMBER_ORDER) : null;
 		for (Iterator cIt = lib.getCells(); cIt.hasNext(); )
 		{
 			Cell cell = (Cell)cIt.next();
 			gatherCell(cell);
 
 			for(Iterator it = cell.getNodes(); it.hasNext(); )
 			{
 				NodeInst ni = (NodeInst)it.next();
 				if (ni.getName() == null)
 					System.out.println("ERROR: " + cell + " has " + ni + " with no name");
 				NodeProto np = ni.getProto();
 				if (np instanceof Cell)
 				{
 					gatherCell((Cell)np);
 				} else
 				{
 					gatherObj(np);
 					gatherObj(((PrimitiveNode)np).getTechnology());
 				}
 				for (Iterator pIt = ni.getPortInsts(); pIt.hasNext(); )
 				{
 					PortInst pi = (PortInst)pIt.next();
 					gatherVariables(pi);
 				}
 				gatherVariables(ni);
 				if (needVars)
 				{
 					gatherFont(ni.getTextDescriptor(NodeInst.NODE_NAME_TD));
 					gatherFont(ni.getTextDescriptor(NodeInst.NODE_PROTO_TD));
 				}
 			}
 
 			for(Iterator it = cell.getArcs(); it.hasNext(); )
 			{
 				ArcInst ai = (ArcInst)it.next();
 				ArcProto ap = ai.getProto();
 				gatherObj(ap);
 				gatherObj(ap.getTechnology());
 				//gatherObj(ai.getHead().getPortInst().getPortProto());
 				//gatherObj(ai.getTail().getPortInst().getPortProto());
 				gatherVariables(ai);
 				if (needVars) gatherFont(ai.getTextDescriptor(ArcInst.ARC_NAME_TD));
 			}
 
 			for(Iterator it = cell.getPorts(); it.hasNext(); )
 			{
 				Export e = (Export)it.next();
 				//gatherObj(e.getOriginalPort().getPortProto());
 				gatherVariables(e);
 				if (needVars) gatherFont(e.getTextDescriptor(Export.EXPORT_NAME_TD));
 			}
 
 			gatherVariables(cell);
 		}
 		gatherVariables(lib);
 
 		for (Iterator it = Tool.getTools(); it.hasNext(); )
 			gatherMeaningPrefs(it.next());
 
 		for (Iterator it = Technology.getTechnologies(); it.hasNext(); )
 			gatherMeaningPrefs(it.next());
 
 		if (needVars)
 		{
 			putNameSpace(Library.FONT_ASSOCIATIONS.getName());
 			putNameSpace(NodeInst.NODE_NAME.getName());
 			putNameSpace(ArcInst.ARC_NAME.getName());
 			short varIndex = 0;
 			for (Iterator it = nameSpace.entrySet().iterator(); it.hasNext(); )
 			{
 				Map.Entry e = (Map.Entry)it.next();
 				e.setValue(new Short(varIndex++));
 			}
 		}
 	}
 
 	/**
 	 * Gather variables of ElectricObject into objInfo map.
 	 * @param eObj ElectricObject with variables.
 	 */
 	private void gatherVariables(ElectricObject eObj)
 	{
 		for (Iterator it = eObj.getVariables(); it.hasNext(); )
 		{
 			Variable var = (Variable)it.next();
 			//if (var.isDontSave()) continue;
 			if (nameSpace != null) putNameSpace(diskName(var));
 			gatherFont(var.getTextDescriptor());
 			Object value = var.getObject();
 			if (value == null) continue;
 			int length = value instanceof Object[] ? ((Object[])value).length : 1;
 			for (int i = 0; i < length; i++)
 			{
 				Object v = value instanceof Object[] ? ((Object[])value)[i] : value;
 				if (v == null) continue;
 				if (v instanceof Technology || v instanceof Tool)
 				{
 					gatherObj(v);
 				} else if (v instanceof PrimitiveNode)
 				{
 					gatherObj(v);
 					gatherObj(((PrimitiveNode)v).getTechnology());
 				} else if (v instanceof PrimitivePort)
 				{
 					//gatherObj(v);
 					PrimitiveNode pn = (PrimitiveNode)((PrimitivePort)v).getParent();
 					gatherObj(pn);
 					gatherObj(pn.getTechnology());
 				} else if (v instanceof ArcProto)
 				{
 					gatherObj(v);
 					gatherObj(((ArcProto)v).getTechnology());
 				} else if (v instanceof ElectricObject)
 				{
 					gatherObj(v);
 					Cell cell = ((ElectricObject)v).whichCell();
 					if (cell != null) gatherCell(cell);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gather meaning preferences attached to object into objInfo map.
 	 * @param obj Object with attached meaning preferences.
 	 */
 	private void gatherMeaningPrefs(Object obj)
 	{
 		for(Iterator it = Pref.getMeaningVariables(obj).iterator(); it.hasNext(); )
 		{
 			gatherObj(obj);
 			Pref pref = (Pref)it.next();
 			String name = pref.getPrefName();
 			if (nameSpace != null) putNameSpace(name);
 		}
 	}
 
 	/**
 	 * Gather Cell, its Library and its font into objInfo map.
 	 * @param cell Cell to examine.
 	 */
 	private void gatherCell(Cell cell)
 	{
 		gatherObj(cell);
 		gatherObj(cell.getLibrary());
 		gatherObj(cell.getView());
 	}
 
 	/**
 	 * Gather ActiveFont object of the TextDescriptor into objInfo map.
 	 * @param td TextDescriptor to examine.
 	 */
 	private void gatherFont(TextDescriptor td)
 	{
 		int face = td.getFace();
 		if (face != 0) gatherObj(TextDescriptor.ActiveFont.findActiveFont(face));
 	}
 
 	/**
 	 * Gather object into objInfo map.
 	 * @param obj Object to put.
 	 */
 	private void gatherObj(Object obj)
 	{
 		objInfo.put(obj, null);
 	}
 
 	/**
 	 * Put index of object into objInfo map.
 	 * @param obj Object to put.
 	 * @param index index of object.
 	 */
 	void putObjIndex(Object obj, int index)
 	{
 		objInfo.put(obj, new Integer(index));
 	}
 
 	/**
 	 * Put string into variable name space.
 	 * @param name name to put.
 	 */
 	void putNameSpace(String name)
 	{
 		nameSpace.put(name, null);
 	}
 
 	/**
 	 * Returns variable disk name. Usually it is variable name.
 	 * Disk name of PortInst variables is key ATTRP_portName_varName.
 	 * @param var Variable.
 	 * @return disk name of variable.
 	 */
 	String diskName(Variable var)
 	{
 		if (!(var.getOwner() instanceof PortInst)) return var.getKey().getName();
 		PortInst pi = (PortInst)var.getOwner();
 		String portName = pi.getPortProto().getName();
 		StringBuffer sb = new StringBuffer("ATTRP_");
 		for (int i = 0; i < portName.length(); i++)
 		{
 			char ch = portName.charAt(i);
 			if (ch == '\\' || ch == '_') sb.append('\\');
 			sb.append(ch);
 		}
 		sb.append('_');
 		sb.append(var.getKey().getName());
 		return sb.toString();
 	}
 
 	/**
 	 * Method to gather all font settings in a Library.
 	 * The method examines all TextDescriptors that might be saved with the Library
 	 * and returns an array of Strings that describes the font associations.
 	 * Each String is of the format NUMBER/FONTNAME where NUMBER is the font number
 	 * in the TextDescriptor and FONTNAME is the font name.
 	 * @return font association array or null.
 	 */
 	String[] createFontAssociation()
 	{
 		int maxIndices = TextDescriptor.ActiveFont.getMaxIndex();
 		faceMap = new int[maxIndices + 1];
 		TreeMap/*<String,TextDescriptor.ActiveFont>*/ sortedFonts = new TreeMap/*<String,TextDescriptor.ActiveFont>*/();
 		for (int i = 1; i <= maxIndices; i++)
 		{
 			TextDescriptor.ActiveFont af = TextDescriptor.ActiveFont.findActiveFont(i);
 			if (!objInfo.containsKey(af)) continue;
 			sortedFonts.put(af.getName(), af);
 		}
 		if (sortedFonts.size() == 0) return null;
 		String[] fontAssociation = new String[sortedFonts.size()];
 		int face = 0;
 		for (Iterator it = sortedFonts.values().iterator(); it.hasNext(); )
 		{
 			TextDescriptor.ActiveFont af = (TextDescriptor.ActiveFont)it.next();
 			face++;
 			faceMap[af.getIndex()] = face;
 			fontAssociation[face-1] = face + "/" + af.getName();
 		}
 		return fontAssociation;
 	}
 
     /**
      * Opens the dataOutputStream for writing of binary files.
      * @return true on error.
      */
     protected boolean openBinaryOutputStream(String filePath)
     {
 		this.filePath = filePath;
         FileOutputStream fileOutputStream;
 		try
 		{
 			fileOutputStream = new FileOutputStream(filePath);
 		} catch (FileNotFoundException e)
 		{
 			System.out.println("Could not write file " + filePath);
 			System.out.println("Reason: " + e.getMessage());
 			return true;
 		}
 		BufferedOutputStream bufStrm = new BufferedOutputStream(fileOutputStream);
 		dataOutputStream = new DataOutputStream(bufStrm);
         return false;
     }
     
     /** 
      * Closes the dataOutputStream.
      * @return true on error.
      */
     protected boolean closeBinaryOutputStream()
     {
 		try
 		{
 			dataOutputStream.close();
 		} catch (IOException e)
 		{
 			System.out.println("Error closing " + filePath);
 			return true;
 		}
         return false;
     }
     
     /**
      * Open the printWriter for writing text files
      * @return true on error.
      */
     protected boolean openTextOutputStream(String filePath)
     {
 		this.filePath = filePath;
         try
 		{
             printWriter = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
         } catch (IOException e)
 		{
             System.out.println("Error opening " + filePath);
             return true;
         }
         return false;
     }
 
 	/** 
      * Close the printWriter.
      * @return true on error.
      */
     protected boolean closeTextOutputStream()
     {
         printWriter.close();
         return false;
     }
 
 	protected void emitCopyright(String prefix, String postfix)
 	{
 		if (!IOTool.isUseCopyrightMessage()) return;
 		String str = IOTool.getCopyrightMessage();
 		int start = 0;
 		while (start < str.length())
 		{
 			int endPos = str.indexOf('\n', start);
 			if (endPos < 0) endPos = str.length();
 			String oneLine = str.substring(start, endPos);
 			printWriter.println(prefix + oneLine + postfix);
 			start = endPos+1;
 		}
 	}
 
 	/** number of characters written on a line */	private int lineCharCount = 0;
 	private int maxWidth = 80;
 	private boolean strictWidthLimit = false;
 	private char commentChar = 0;
 	private String continuationString = "";
 
 	protected void setOutputWidth(int width, boolean strict) { maxWidth = width;   strictWidthLimit = strict; }
 
 	protected void setCommentChar(char ch) { commentChar = ch; }
 
 	protected void setContinuationString(String str) { continuationString = str; }
 
 	private void writeChunk(String str)
 	{
 		int len = str.length();
 		if (len <= 0) return;
 		printWriter.print(str);
 		lineCharCount += len;
 		if (str.charAt(len-1) == '\n') lineCharCount = 0;
 	}
 
 	/**
 	 * Write to the file, but break into printable lines
 	 */
 	protected void writeWidthLimited(String str)
 	{
 		for(;;)
 		{
 			int len = str.length();
 			if (len <= 0) break;
 			int i = str.indexOf('\n');
 			if (i < 0) i = len; else i++;
 			if (lineCharCount + i < maxWidth)
 			{
 				// the next chunk fits: write it
 				String chunk = str;
 				if (i < len) chunk = str.substring(0, i);
 				writeChunk(chunk);
 
 				str = str.substring(i);
 				if (str.length() == 0) break;
 				continue;
 			}
 
 			// find place to break the line
 			int left = maxWidth - lineCharCount;
 			String exact = str.substring(0, left);
 			int splitPos = exact.lastIndexOf(' ');
 			if (splitPos < 0)
 			{
 				splitPos = exact.lastIndexOf(',');
 				if (splitPos < 0)
 				{
 					splitPos = exact.lastIndexOf('(');
 					if (splitPos < 0)
 					{
 						splitPos = exact.lastIndexOf(')');
 					}
 				}
 			}
 			if (splitPos > 0) exact = exact.substring(0, splitPos+1); else
 			{
 				if (!strictWidthLimit)
 				{
 					// allow a wider line
 					splitPos = str.indexOf(' ', left);
 					if (splitPos < 0)
 					{
 						splitPos = str.indexOf(',', left);
 						if (splitPos < 0)
 						{
 							splitPos = str.indexOf('(', left);
 							if (splitPos < 0)
 							{
 								splitPos = str.indexOf(')', left);
 							}
 						}
 					}
 					if (splitPos > 0) exact = str.substring(0, splitPos+1);
 				}
 			}
 
 			writeChunk(exact);
 			writeChunk("\n");
 			if (continuationString.length() > 0) writeChunk(continuationString);
 			str = str.substring(exact.length());
 		}
 	}
 
 	/**
 	 * Method to determine the area of a cell that is to be printed.
 	 * Returns null if the area cannot be determined.
 	 */
 	public Rectangle2D getAreaToPrint(Cell cell, boolean reduce, EditWindow wnd)
 	{
 		Rectangle2D bounds = cell.getBounds();
 		if (wnd != null) bounds = wnd.getBoundsInWindow();
 
 		// extend it and make it square
 		if (reduce)
 		{
 			double wid = bounds.getWidth() * 0.75;
 			double hei = bounds.getHeight() * 0.75;
 //			us_squarescreen(el_curwindowpart, NOWINDOWPART, FALSE, lx, hx, ly, hy, 0);
 			bounds.setRect(bounds.getCenterX(), bounds.getCenterY(), wid, hei);
 		}
 
 		if (IOTool.getPlotArea() != 0)
 		{
 			if (wnd == null)
 			{
 				System.out.println("No current window: printing entire cell");
 			} else
 			{
 				if (IOTool.getPlotArea() == 2)
 				{
 					bounds = wnd.getDisplayedBounds();
 				} else
 				{
 					Rectangle2D hBounds = wnd.getHighlighter().getHighlightedArea(wnd);
 					if (hBounds == null || hBounds.getWidth() == 0 || hBounds.getHeight() == 0)
 					{
 						System.out.println("Warning: no highlighted area; printing entire cell");
 					} else
 					{
 						bounds = hBounds;
 					}
 				}
 			}
 		}
 		return bounds;
 	}
 
     /**
      * Class to define cell information during output.
      */
     protected static class OutputCellInfo extends Job
     {
         Cell cell;
         VarContext context;
         String filePath;
         FileType type;
 
         /**
          * @param cell the Cell to be written.
          * @param context the VarContext of the Cell (its position in the hierarchy above it).
          * @param filePath the path to the disk file to be written.
          * @param type the format of the output file.
          * @param startJob to start job immediately
          */
         public OutputCellInfo(Cell cell, VarContext context, String filePath, FileType type, boolean startJob, Job.Type jobType)
         {
             super("Export "+cell+" ("+type+")", IOTool.getIOTool(), jobType, null, null, Priority.USER);
             this.cell = cell;
             this.context = context;
             this.filePath = filePath;
             this.type = type;
             if (startJob)
                 startJob();
         }
 
         public boolean doIt()
         {
             writeCell(this);
             return true;
         }
 
     }
 }
 
