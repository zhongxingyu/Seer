 /* -*- tab-width: 4 -*-
  *
  * Electric(tm) VLSI Design System
  *
  * File: Verilog.java
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
 import com.sun.electric.database.hierarchy.Nodable;
 import com.sun.electric.database.hierarchy.Export;
 import com.sun.electric.database.hierarchy.View;
 import com.sun.electric.database.network.Netlist;
 import com.sun.electric.database.network.JNetwork;
 import com.sun.electric.database.network.Global;
 import com.sun.electric.database.prototype.NodeProto;
 import com.sun.electric.database.prototype.PortProto;
 import com.sun.electric.database.text.Version;
 import com.sun.electric.database.text.TextUtils;
 import com.sun.electric.database.topology.NodeInst;
 import com.sun.electric.database.topology.ArcInst;
 import com.sun.electric.database.topology.PortInst;
 import com.sun.electric.database.topology.Connection;
 import com.sun.electric.database.variable.Variable;
 import com.sun.electric.database.variable.VarContext;
 import com.sun.electric.database.variable.ElectricObject;
 import com.sun.electric.technology.PrimitiveNode;
 import com.sun.electric.technology.technologies.Generic;
 import com.sun.electric.tool.simulation.Simulation;
 import com.sun.electric.tool.user.User;
 
 import java.util.Iterator;
 import java.util.Date;
 import java.util.ArrayList;
 
 /**
  * This is the Simulation Interface tool.
  */
 public class Verilog extends Topology
 {
 	/** maximum size of output line */						private static final int MAXDECLARATIONWIDTH = 80;
 	/** name of inverters generated from negated wires */	private static final String IMPLICITINVERTERNODENAME = "Imp";
 	/** name of signals generated from negated wires */		private static final String IMPLICITINVERTERSIGNAME = "ImpInv";
 
 	/** key of Variable holding verilog code. */			public static final Variable.Key VERILOG_CODE_KEY = ElectricObject.newKey("VERILOG_code");
 	/** key of Variable holding verilog declarations. */	public static final Variable.Key VERILOG_DECLARATION_KEY = ElectricObject.newKey("VERILOG_declaration");
 	/** key of Variable holding verilog wire time. */		public static final Variable.Key WIRE_TYPE_KEY = ElectricObject.newKey("SIM_verilog_wire_type");
 	/** key of Variable holding verilog templates. */		public static final Variable.Key VERILOG_TEMPLATE_KEY = ElectricObject.newKey("ATTR_verilog_template");
 	/** key of Variable holding file name with Verilog. */	public static final Variable.Key VERILOG_BEHAVE_FILE_KEY = ElectricObject.newKey("SIM_verilog_behave_file");
 
 	/**
 	 * The main entry point for Verilog deck writing.
 	 * @param cell the top-level cell to write.
 	 * @param filePath the disk file to create with Verilog.
 	 */
 	public static void writeVerilogFile(Cell cell, VarContext context, String filePath)
 	{
 		Verilog out = new Verilog();
 		if (out.openTextOutputStream(filePath)) return;
 		if (out.writeCell(cell, context)) return;
 		if (out.closeTextOutputStream()) return;
 		System.out.println(filePath + " written");
 	}
 
 	/**
 	 * Creates a new instance of Verilog
 	 */
 	Verilog()
 	{
 	}
 
 	protected void start()
 	{
 		// write header information
 		printWriter.print("/* Verilog for cell " + topCell.describe() +
 			" from Library " + topCell.getLibrary().getName() + " */\n");
 		emitCopyright("/* ", " */");
 		if (User.isIncludeDateAndVersionInOutput())
 		{
 			printWriter.print("/* Created on " + TextUtils.formatDate(topCell.getCreationDate()) + " */\n");
 			printWriter.print("/* Last revised on " + TextUtils.formatDate(topCell.getRevisionDate()) + " */\n");
 			printWriter.print("/* Written on " + TextUtils.formatDate(new Date()) +
 				" by Electric VLSI Design System, version " + Version.getVersion() + " */\n");
 		} else
 		{
 			printWriter.print("/* Written by Electric VLSI Design System */\n");
 		}
 
 		// gather all global signal names
 		Netlist netList = getNetlistForCell(topCell);
 		Global.Set globals = netList.getGlobals();
         int globalSize = globals.size();
 
         // see if any globals besides power and ground to write
         ArrayList globalsToWrite = new ArrayList();
         for (int i=0; i<globalSize; i++) {
             Global global = (Global)globals.get(i);
             if (global == Global.power || global == Global.ground) continue;
             globalsToWrite.add(global);
         }
 
 		if (globalsToWrite.size() > 0)
 		{
 			printWriter.print("\nmodule glbl();\n");
 			for(int i=0; i<globalsToWrite.size(); i++)
 			{
 				Global global = (Global)globalsToWrite.get(i);
 				if (Simulation.getVerilogUseTrireg())
 				{
 					printWriter.print("    trireg " + global.getName() + ";\n");
 				} else
 				{
 					printWriter.print("    wire " + global.getName() + ";\n");
 				}
 			}
 			printWriter.print("endmodule\n");
 		}
 	}
 
 	protected void done()
 	{
 	}
 
     protected boolean skipCellAndSubcells(Cell cell) {
         // do not write modules for cells with verilog_templates defined
         // also skip their subcells
         if (cell.getVar(VERILOG_TEMPLATE_KEY) != null) {
             return true;
         }
         // also skip subcells if a behavioral file specified.
         // If one specified, write it out here and skip both cell and subcells
         Variable behaveFileVar = cell.getVar(VERILOG_BEHAVE_FILE_KEY);
         if (behaveFileVar != null)
         {
             String fileName = behaveFileVar.getObject().toString();
             if (fileName.length() > 0)
             {
                 printWriter.print("`include \"" + fileName + "\"\n");
                 return true;
             }
         }
 
         return false;
     }
 
 	/**
 	 * Method to write cellGeom
 	 */
 	protected void writeCellTopology(Cell cell, CellNetInfo cni, VarContext context)
 	{
 		// use library behavior if it is available
 		Cell verViewCell = cell.otherView(View.VERILOG);
 		if (verViewCell != null)
 		{
 			String [] stringArray = verViewCell.getTextViewContents();
 			if (stringArray != null)
 			{
 				for(int i=0; i<stringArray.length; i++)
 					printWriter.print(stringArray[i] + "%s\n");
 			}
 			return;
 		}
 
         // if verilog template specified, don't need to write out this cell
 /*        Variable varT = cell.getVar(VERILOG_TEMPLATE_KEY);
         if (varT != null) {
             return;
         } */
 
 //		// prepare arcs to store implicit inverters
 //		impinv = 1;
 //		for(ai = np->firstarcinst; ai != NOARCINST; ai = ai->nextarcinst)
 //			ai->temp1 = 0;
 //		for(ai = np->firstarcinst; ai != NOARCINST; ai = ai->nextarcinst)
 //		{
 //			if ((ai->userbits&ISNEGATED) == 0) continue;
 //			if ((ai->userbits&REVERSEEND) == 0)
 //			{
 //				ni = ai->end[0].nodeinst;
 //				pi = ai->end[0].portarcinst;
 //			} else
 //			{
 //				ni = ai->end[1].nodeinst;
 //				pi = ai->end[1].portarcinst;
 //			}
 //			if (ni->proto == sch_bufprim || ni->proto == sch_andprim ||
 //				ni->proto == sch_orprim || ni->proto == sch_xorprim)
 //			{
 //				if (Simulation.getVerilogUseAssign()) continue;
 //				if (estrcmp(pi->proto->protoname, x_("y")) == 0) continue;
 //			}
 //
 //			// must create implicit inverter here
 //			ai->temp1 = impinv;
 //			if (ai->proto != sch_busarc) impinv++; else
 //			{
 //				net = ai->network;
 //				if (net == NONETWORK) impinv++; else
 //					impinv += net->buswidth;
 //			}
 //		}
 
 		// gather networks in the cell
 		Netlist netList = getNetlistForCell(cell);
 
 		// write the module header
 		printWriter.print("\n");
 		StringBuffer sb = new StringBuffer();
 		sb.append("module " + cni.getParameterizedName() + "(");
 		boolean first = true;
 		for(Iterator it = cni.getCellAggregateSignals(); it.hasNext(); )
 		{
 			CellAggregateSignal cas = (CellAggregateSignal)it.next();
 			if (cas.getExport() == null) continue;
 			if (!first) sb.append(", ");
 			sb.append(cas.getName());
 			first = false;
 		}
 		sb.append(");");
 		writeLongLine(sb.toString());
 
 		// look for "wire/trireg" overrides
 		for(Iterator it = cell.getArcs(); it.hasNext(); )
 		{
 			ArcInst ai = (ArcInst)it.next();
 			Variable var = ai.getVar(WIRE_TYPE_KEY);
 			if (var == null) continue;
 			String wireType = var.getObject().toString();
 			int overrideValue = 0;
 			if (wireType.equalsIgnoreCase("wire")) overrideValue = 1; else
 				if (wireType.equalsIgnoreCase("trireg")) overrideValue = 2;
 			int busWidth = netList.getBusWidth(ai);
 			for(int i=0; i<busWidth; i++)
 			{
 				JNetwork net = netList.getNetwork(ai, i);
 				CellSignal cs = cni.getCellSignal(net);
 				if (cs == null) continue;
 				cs.getAggregateSignal().setFlags(overrideValue);
 			}
 		}
 
 		// write description of formal parameters to module
 		first = true;
 		for(Iterator it = cni.getCellAggregateSignals(); it.hasNext(); )
 		{
 			CellAggregateSignal cas = (CellAggregateSignal)it.next();
 			Export pp = cas.getExport();
 			if (pp == null) continue;
 
 			String portType = "input";
 			if (pp.getCharacteristic() == PortProto.Characteristic.OUT)
 				portType = "output";
 			printWriter.print("  " + portType);
 			if (cas.getLowIndex() > cas.getHighIndex())
 			{
 				printWriter.print(" " + cas.getName() + ";");
 			} else
 			{
 				int low = cas.getLowIndex(), high = cas.getHighIndex();
 				if (cas.isDescending())
 				{
 					low = cas.getHighIndex();   high = cas.getLowIndex();
 				}
 				printWriter.print(" [" + low + ":" + high + "] " + cas.getName() + ";");
 			}
 			if (cas.getFlags() != 0)
 			{
 				if (cas.getFlags() == 1) printWriter.print("  wire"); else
 					printWriter.print("  trireg");
 				printWriter.print(" " + cas.getName() + ";");
 			}
 			printWriter.print("\n");
 			first = false;
 		}
 		if (!first) printWriter.print("\n");
 
 		// describe power and ground nets
 		if (cni.getPowerNet() != null) printWriter.print("  supply1 vdd;\n");
 		if (cni.getGroundNet() != null) printWriter.print("  supply0 gnd;\n");
 
 		// determine whether to use "wire" or "trireg" for networks
 		String wireType = "wire";
 		if (Simulation.getVerilogUseTrireg()) wireType = "trireg";
 
 		// write "wire/trireg" declarations for internal single-wide signals
 		int localWires = 0;
 		for(int wt=0; wt<2; wt++)
 		{
 			first = true;
 			for(Iterator it = cni.getCellAggregateSignals(); it.hasNext(); )
 			{
 				CellAggregateSignal cas = (CellAggregateSignal)it.next();
 				if (cas.getExport() != null) continue;
 				if (cas.isSupply()) continue;
 				if (cas.getLowIndex() <= cas.getHighIndex()) continue;
 				if (cas.isGlobal()) continue;
 
 				String impSigName = wireType;
 				if (cas.getFlags() != 0)
 				{
 					if (cas.getFlags() == 1) impSigName = "wire"; else
 						impSigName = "trireg";
 				}
 				if ((wt == 0) ^ !wireType.equals(impSigName))
 				{
 					if (first)
 					{
 						initDeclaration("  " + impSigName);
 					}
 					addDeclaration(cas.getName());
 					localWires++;
 					first = false;
 				}
 			}
 			if (!first) termDeclaration();
 		}
 
 		// write "wire/trireg" declarations for internal busses
 		for(Iterator it = cni.getCellAggregateSignals(); it.hasNext(); )
 		{
 			CellAggregateSignal cas = (CellAggregateSignal)it.next();
 			if (cas.getExport() != null) continue;
 			if (cas.isSupply()) continue;
 			if (cas.getLowIndex() > cas.getHighIndex()) continue;
 			if (cas.isGlobal()) continue;
 
 			if (cas.isDescending())
 			{
 				printWriter.print("  " + wireType + " [" + cas.getHighIndex() + ":" + cas.getLowIndex() + "] " + cas.getName() + ";\n");
 			} else
 			{
 				printWriter.print("  " + wireType + " [" + cas.getLowIndex() + ":" + cas.getHighIndex() + "] " + cas.getName() + ";\n");
 			}
 			localWires++;
 		}
 		if (localWires != 0) printWriter.print("\n");
 
 //		// add "wire" declarations for implicit inverters
 //		if (impinv > 1)
 //		{
 //			esnprintf(invsigname, 100, x_("  %s"), wireType);
 //			initDeclaration(invsigname);
 //			for(i=1; i<impinv; i++)
 //			{
 //				esnprintf(impsigname, 100, x_("%s%ld"), IMPLICITINVERTERSIGNAME, i);
 //				addDeclaration(impsigname);
 //			}
 //			termDeclaration();
 //		}
 
 		// add in any user-specified declarations and code
 		first = includeTypedCode(cell, VERILOG_DECLARATION_KEY, "declarations");
 		first |= includeTypedCode(cell, VERILOG_CODE_KEY, "code");
 		if (!first)
 			printWriter.print("  /* automatically generated Verilog */\n");
 
 		// look at every node in this cell
 		for(Iterator nIt = netList.getNodables(); nIt.hasNext(); )
 		{
 			Nodable no = (Nodable)nIt.next();
 			NodeProto niProto = no.getProto();
 
 			// not interested in passive nodes (ports electrically connected)
 			NodeProto.Function nodeType = NodeProto.Function.UNKNOWN;
 			if (niProto instanceof PrimitiveNode)
 			{
 				NodeInst ni = (NodeInst)no;
 				Iterator pIt = ni.getPortInsts();
 				if (pIt.hasNext())
 				{
 					boolean allConnected = true;
 					PortInst firstPi = (PortInst)pIt.next();
 					JNetwork firstNet = netList.getNetwork(firstPi);
 					for( ; pIt.hasNext(); )
 					{
 						PortInst pi = (PortInst)pIt.next();
 						JNetwork thisNet = netList.getNetwork(pi);
 						if (thisNet != firstNet) { allConnected = false;   break; }
 					}
 					if (allConnected) continue;
 				}
 				nodeType = ni.getFunction();
 
 				// special case: verilog should ignore R L C etc.
 				if (nodeType == NodeProto.Function.RESIST || nodeType == NodeProto.Function.CAPAC ||
 					nodeType == NodeProto.Function.ECAPAC || nodeType == NodeProto.Function.INDUCT ||
 					nodeType == NodeProto.Function.DIODE || nodeType == NodeProto.Function.DIODEZ)
 						continue;
 			}
 
 			// look for a Verilog template on the prototype
 			if (niProto instanceof Cell)
 			{
 				Variable varTemplate = niProto.getVar(VERILOG_TEMPLATE_KEY);
 				if (varTemplate != null)
 				{
 					writeTemplate((String)varTemplate.getObject(), no, cni, context);
 					continue;
 				}
 			}
 
 			// use "assign" statement if requested
 			if (Simulation.getVerilogUseAssign())
 			{
 				if (nodeType == NodeProto.Function.GATEAND || nodeType == NodeProto.Function.GATEOR ||
 					nodeType == NodeProto.Function.GATEXOR || nodeType == NodeProto.Function.BUFFER)
 				{
 					// assign possible: determine operator
 					String op = "";
 					if (nodeType == NodeProto.Function.GATEAND) op = " & "; else
 					if (nodeType == NodeProto.Function.GATEOR) op = " | "; else
 					if (nodeType == NodeProto.Function.GATEXOR) op = " ^ ";
 
 					// write a line describing this signal
 					StringBuffer infstr = new StringBuffer();
 					boolean wholeNegated = false;
 					first = true;
 					NodeInst ni = (NodeInst)no;
 					for(int i=0; i<2; i++)
 					{
 						for(Iterator cIt = ni.getConnections(); cIt.hasNext(); )
 						{
 							Connection con = (Connection)cIt.next();
 							PortInst pi = con.getPortInst();
 							if (i == 0)
 							{
 								if (!pi.getPortProto().getName().equals("y")) continue;
 							} else
 							{
 								if (!pi.getPortProto().getName().equals("a")) continue;
 							}
 
 							// determine the network name at this port
 							ArcInst ai = con.getArc();
 							JNetwork net = netList.getNetwork(ai, 0);
 							CellSignal cs = cni.getCellSignal(net);
 							String sigName = cs.getName();
 
 							// see if this end is negated
 							boolean isNegated = false;
 //							if (ai.isNegated())
 //							{
 //								if (ai.getTail().getPortInst() == pi && !ai.isReverseEnds() ||
 //									ai.getHead().getPortInst() == pi && ai.isReverseEnds())
 //										isNegated = true;
 //							}
 
 							// write the port name
 							if (i == 0)
 							{
 								// got the output port: do the left-side of the "assign"
 								infstr.append("assign " + sigName + " = ");
 								if (isNegated)
 								{
 									infstr.append("~(");
 									wholeNegated = true;
 								}
 								break;
 							} else
 							{
 								if (!first)
 									infstr.append(op);
 								first = false;
 								if (isNegated) infstr.append("~");
 								infstr.append(sigName);
 							}
 						}
 					}
 					if (wholeNegated)
 						infstr.append(")");
 					infstr.append(";");
 					writeLongLine(infstr.toString());
 					continue;
 				}
 			}
 
 			// get the name of the node
 			int implicitPorts = 0;
 			boolean dropBias = false;
 			String nodeName = "";
 			if (niProto instanceof Cell)
 			{
 				nodeName = parameterizedName(no, context);
 			} else
 			{
 				// convert 4-port transistors to 3-port
 				if (nodeType == NodeProto.Function.TRA4NMOS)
 				{
 					nodeType = NodeProto.Function.TRANMOS;  dropBias = true;
 				} else if (nodeType == NodeProto.Function.TRA4PMOS)
 				{
 					nodeType = NodeProto.Function.TRAPMOS;  dropBias = true;
 				}
 
 				if (nodeType == NodeProto.Function.TRANMOS)
 				{
 					implicitPorts = 2;
 					nodeName = "tranif1";
 					Variable varWeakNode = ((NodeInst)no).getVar(Simulation.WEAK_NODE_KEY);
 					if (varWeakNode != null) nodeName = "rtranif1";
 				} else if (nodeType == NodeProto.Function.TRAPMOS)
 				{
 					implicitPorts = 2;
 					nodeName = "tranif0";
 					Variable varWeakNode = ((NodeInst)no).getVar(Simulation.WEAK_NODE_KEY);
 					if (varWeakNode != null) nodeName = "rtranif0";
 				} else if (nodeType == NodeProto.Function.GATEAND)
 				{
 					implicitPorts = 1;
 					nodeName = chooseNodeName((NodeInst)no, "and", "nand");
 				} else if (nodeType == NodeProto.Function.GATEOR)
 				{
 					implicitPorts = 1;
 					nodeName = chooseNodeName((NodeInst)no, "or", "nor");
 				} else if (nodeType == NodeProto.Function.GATEXOR)
 				{
 					implicitPorts = 1;
 					nodeName = chooseNodeName((NodeInst)no, "xor", "xnor");
 				} else if (nodeType == NodeProto.Function.BUFFER)
 				{
 					implicitPorts = 1;
 					nodeName = chooseNodeName((NodeInst)no, "buf", "not");
 					nodeName = "buf";
 				}
 			}
 			if (nodeName.length() == 0) continue;
 
 			// write the type of the node
 			StringBuffer infstr = new StringBuffer();
 			infstr.append("  " + nodeName + " " + nameNoIndices(no.getName()) + "(");
 
 			// write the rest of the ports
 			first = true;
 			NodeInst ni = null;
 			switch (implicitPorts)
 			{
 				case 0:		// explicit ports (for cell instances)
 					CellNetInfo subCni = getCellNetInfo(nodeName);
 					for(Iterator sIt = subCni.getCellAggregateSignals(); sIt.hasNext(); )
 					{
 						CellAggregateSignal cas = (CellAggregateSignal)sIt.next();
 
 						// ignore networks that aren't exported
 						PortProto pp = cas.getExport();
 						if (pp == null) continue;
 
 						if (first) first = false; else
 							infstr.append(", ");
 						int low = cas.getLowIndex(), high = cas.getHighIndex();
 						if (low > high)
 						{
 							// single signal
 							infstr.append("." + cas.getName() + "(");
 							JNetwork net = netList.getNetwork(no, pp, cas.getExportIndex());
 							CellSignal cs = cni.getCellSignal(net);
 							infstr.append(cs.getName());
 							infstr.append(")");
 						} else
 						{
 							int total = high - low + 1;
 							CellSignal [] outerSignalList = new CellSignal[total];
							int totalBusWidth = netList.getBusWidth(cas.getExport());
 							for(int j=low; j<=high; j++)
 							{
								JNetwork net = netList.getNetwork(no, cas.getExport(), j-low);
								if (cas.isDescending()) net = netList.getNetwork(no, cas.getExport(), totalBusWidth-1-(j-low)); else
									net = netList.getNetwork(no, cas.getExport(), j-low);
//								if (cas.isDescending()) outerSignalList[total-1-(j-low)] = cni.getCellSignal(net); else
 								outerSignalList[j-low] = cni.getCellSignal(net);
 							}
 							writeBus(outerSignalList, low, high, cas.isDescending(),
 								cas.getName(), cni.getPowerNet(), cni.getGroundNet(), infstr);
 						}
 					}
 					infstr.append(");");
 					break;
 
 				case 1:		// and/or gate: write ports in the proper order
 					ni = (NodeInst)no;
 					for(int i=0; i<2; i++)
 					{
 						for(Iterator pIt = ni.getPortInsts(); pIt.hasNext(); )
 						{
 							PortInst pi = (PortInst)pIt.next();
 							if (i == 0)
 							{
 								if (!pi.getPortProto().getName().equals("y")) continue;
 							} else
 							{
 								if (!pi.getPortProto().getName().equals("a")) continue;
 							}
 							if (first) first = false; else
 								infstr.append(", ");
 							JNetwork net = netList.getNetwork(pi);
 							CellSignal cs = cni.getCellSignal(net);
 							String sigName = cs.getName();
 //							if (i != 0 && pi->conarcinst->temp1 != 0)
 //							{
 //								// this input is negated: write the implicit inverter
 //								String invsigname = IMPLICITINVERTERSIGNAME + (pi->conarcinst->temp1+nindex);
 //								printWriter.print("  inv " + IMPLICITINVERTERNODENAME +
 //									(pi->conarcinst->temp1+nindex) + " (" + invsigname + ", " + sigName + ");\n");
 //								sigName = invsigname;
 //							}
 							infstr.append(sigName);
 						}
 					}
 					infstr.append(");");
 					break;
 
 				case 2:		// transistors: write ports in the proper order
 					// schem: g/s/d[/b]  mos: g/s/g/d
 					ni = (NodeInst)no;
 					JNetwork gateNet = netList.getNetwork(ni.getTransistorGatePort());
 					for(int i=0; i<2; i++)
 					{
 						for(Iterator pIt = ni.getPortInsts(); pIt.hasNext(); )
 						{
 							PortInst pi = (PortInst)pIt.next();
 							JNetwork net = netList.getNetwork(pi);
 
 //							// see if it connects to an earlier portinst
 //							boolean connected = false;
 //							for(Iterator ePIt = ni.getPortInsts(); ePIt.hasNext(); )
 //							{
 //								PortInst ePi = (PortInst)ePIt.next();
 //								if (ePi == pi) break;
 //								JNetwork eNet = netList.getNetwork(ePi);
 //								if (eNet == net) { connected = true;   break; }
 //							}
 //							if (connected) continue;
 							if (dropBias && pi.getPortProto().getName().equals("b")) continue;
 							if (i == 0)
 							{
 								if (net == gateNet) continue;
 							} else
 							{
 								if (net != gateNet) continue;
 							}
 							if (first) first = false; else
 								infstr.append(", ");
 
 							CellSignal cs = cni.getCellSignal(net);
 							String sigName = cs.getName();
 //							if (i != 0 && pi->conarcinst->temp1 != 0)
 //							{
 //								// this input is negated: write the implicit inverter
 //								String invsigname = IMPLICITINVERTERSIGNAME + (pi->conarcinst->temp1+nindex);
 //								printWriter.print("  inv " + IMPLICITINVERTERNODENAME +
 //									(pi->conarcinst->temp1+nindex) + " (" + invsigname + ", " + sigName + ");\n");
 //								sigName = invsigname;
 //							}
 							infstr.append(sigName);
 						}
 					}
 					infstr.append(");");
 					break;
 			}
 			writeLongLine(infstr.toString());
 		}
 		printWriter.print("endmodule   /* " + cni.getParameterizedName() + " */\n");
 	}
 
 	private String chooseNodeName(NodeInst ni, String positive, String negative)
 	{
 		for(Iterator aIt = ni.getConnections(); aIt.hasNext(); )
 		{
 			Connection con = (Connection)aIt.next();
 			if (con.getPortInst().getPortProto().getName().equals("y") &&
 				con.isNegated()) return negative;
 		}
 		return positive;
 	}
 
 	private void writeTemplate(String line, Nodable no, CellNetInfo cni, VarContext context)
 	{
 		// special case for Verilog templates
 		Netlist netList = cni.getNetList();
 		StringBuffer infstr = new StringBuffer();
 		infstr.append("  ");
 		for(int pt = 0; pt < line.length(); pt++)
 		{
 			char chr = line.charAt(pt);
 			if (chr != '$' || pt+1 >= line.length() || line.charAt(pt+1) != '(')
 			{
 				// process normal character
 				infstr.append(chr);
 				continue;
 			}
 
 			int startpt = pt + 2;
 			for(pt = startpt; pt < line.length(); pt++)
 				if (line.charAt(pt) == ')') break;
 			String paramName = line.substring(startpt, pt);
 			PortProto pp = no.getProto().findPortProto(paramName);
 			if (pp != null)
 			{
 				// port name found: use its verilog node
 				JNetwork net = netList.getNetwork(no, pp, 0);
 				CellSignal cs = cni.getCellSignal(net);
 				infstr.append(cs.getName());
 			} else if (paramName.equalsIgnoreCase("node_name"))
 			{
 				infstr.append(getSafeNetName(no.getName()));
 			} else
 			{
 				// no port name found, look for variable name
 				String attrName = "ATTR_" + paramName;
 				Variable var = no.getVar(attrName);
 				if (var == null)
 				{
 //					// value not found: see if this is a parameter and use default
 //					nip = ni->proto;
 //					nipc = contentsview(nip);
 //					if (nipc != NONODEPROTO) nip = nipc;
 //					var = getval((INTBIG)nip, VNODEPROTO, -1, line);
 				}
 				if (var == null) infstr.append("??"); else
 				{
                     infstr.append(context.evalVar(var));
 				}
 			}
 		}
 		writeLongLine(infstr.toString());
 	}
 
 	/*
 	 * Method to add a bus of signals named "name" to the infinite string "infstr".  If "name" is zero,
 	 * do not include the ".NAME()" wrapper.  The signals are in "outerSignalList" and range in index from
 	 * "lowindex" to "highindex".  They are described by a bus with characteristic "tempval"
 	 * (low bit is on if the bus descends).  Any unconnected networks can be numbered starting at
 	 * "*unconnectednet".  The power and grounds nets are "pwrnet" and "gndnet".
 	 */
 	private void writeBus(CellSignal [] outerSignalList, int lowIndex, int highIndex, boolean descending,
 		String name, JNetwork pwrNet, JNetwork gndNet, StringBuffer infstr)
 	{
 		// array signal: see if it gets split out
 		boolean breakBus = false;
 
 		// bus cannot have pwr/gnd, must be connected
 		int numExported = 0, numInternal = 0;
 		for(int j=lowIndex; j<=highIndex; j++)
 		{
 			CellSignal cs = outerSignalList[j-lowIndex];
 			if (cs.isPower() || cs.isGround()) { breakBus = true;   break; }
 			if (cs.isExported()) numExported++; else
 				numInternal++;
 		}
 
 		// must be all exported or all internal, not a mix
 		if (numExported > 0 && numInternal > 0) breakBus = true;
 
 		if (!breakBus)
 		{
 			// see if all of the nets on this bus are distinct
 			int j = lowIndex+1;
 			for( ; j<=highIndex; j++)
 			{
 				CellSignal cs = outerSignalList[j-lowIndex];
 				int k = lowIndex;
 				for( ; k<j; k++)
 				{
 					CellSignal oCs = outerSignalList[k-lowIndex];
 					if (cs == oCs) break;
 				}
 				if (k < j) break;
 			}
 			if (j <= highIndex) breakBus = true; else
 			{
 				// bus entries must have the same root name and go in order
 				String lastnetname = null;
 				for(j=lowIndex; j<=highIndex; j++)
 				{
 					CellSignal wl = outerSignalList[j-lowIndex];
 					String thisnetname = wl.getName();
 					if (wl.isDescending())
 					{
 						if (!descending) break;
 					} else
 					{
 						if (descending) break;
 					}
 
 					int openSquare = thisnetname.indexOf('[');
 					if (openSquare < 0) break;
 					if (j > lowIndex)
 					{
 						int li = 0;
 						for( ; li < lastnetname.length(); li++)
 						{
 							if (thisnetname.charAt(li) != lastnetname.charAt(li)) break;
 							if (lastnetname.charAt(li) == '[') break;
 						}
 						if (lastnetname.charAt(li) != '[' || thisnetname.charAt(li) != '[') break;
 						int thisIndex = TextUtils.atoi(thisnetname.substring(li+1));
 						int lastIndex = TextUtils.atoi(lastnetname.substring(li+1));
 						if (thisIndex != lastIndex + 1) break;
 					}
 					lastnetname = thisnetname;
 				}
 				if (j <= highIndex) breakBus = true;
 			}
 		}
 
 		if (name != null) infstr.append("." + name + "(");
 		if (breakBus)
 		{
 			infstr.append("{");
 			int start = lowIndex, end = highIndex;
 			int order = 1;
 			if (descending)
 			{
 				start = highIndex;
 				end = lowIndex;
 				order = -1;
 			}
 			for(int j=start; ; j += order)
 			{
 				if (j != start)
 					infstr.append(", ");
 				CellSignal cs = outerSignalList[j-lowIndex];
 				infstr.append(cs.getName());
 				if (j == end) break;
 			}
 			infstr.append("}");
 		} else
 		{
 			CellSignal lastCs = outerSignalList[0];
 			String lastNetName = lastCs.getName();
 			int openSquare = lastNetName.indexOf('[');
 			CellSignal cs = outerSignalList[highIndex-lowIndex];
 			String netName = cs.getName();
 			int i = 0;
 			for( ; i<netName.length(); i++)
 			{
 				if (netName.charAt(i) == '[') break;
 				infstr.append(netName.charAt(i));
 			}
 			if (descending)
 			{
 				int first = TextUtils.atoi(netName.substring(i+1));
 				int second = TextUtils.atoi(lastNetName.substring(openSquare+1));
 				infstr.append("[" + first + ":" + second + "]");
 			} else
 			{
 				int first = TextUtils.atoi(netName.substring(i+1));
 				int second = TextUtils.atoi(lastNetName.substring(openSquare+1));
 				infstr.append("[" + second + ":" + first + "]");
 			}
 		}
 		if (name != null) infstr.append(")");
 	}
 
 	/*
 	 * Method to add text from all nodes in cell "np"
 	 * (which have "verilogkey" text on them)
 	 * to that text to the output file.  Returns true if anything
 	 * was found.
 	 */
 	private boolean includeTypedCode(Cell cell, Variable.Key verilogkey, String descript)
 	{
 		// write out any directly-typed Verilog code
 		boolean first = true;
 		for(Iterator it = cell.getNodes(); it.hasNext(); )
 		{
 			NodeInst ni = (NodeInst)it.next();
 			if (ni.getProto() != Generic.tech.invisiblePinNode) continue;
 			Variable var = ni.getVar(verilogkey);
 			if (var == null) continue;
 			if (!var.isDisplay()) continue;
 			Object obj = var.getObject();
 			if (!(obj instanceof String) && !(obj instanceof String [])) continue;
 			if (first)
 			{
 				first = false;
 				printWriter.print("  /* user-specified Verilog " + descript + " */\n");
 			}
 			if (obj instanceof String)
 			{
 				printWriter.print("  " + (String)obj + "\n");
 			} else
 			{
 				String [] stringArray = (String [])obj;
 				int len = stringArray.length;
 				for(int i=0; i<len; i++)
 					printWriter.print("  " + stringArray[i] + "\n");
 			}
 		}
 		if (!first) printWriter.print("\n");
 		return first;
 	}
 
 	/*
 	 * Method to write a long line to the Verilog file, breaking it where sensible.
 	 */
 	private void writeLongLine(String s)
 	{
 		while (s.length() > MAXDECLARATIONWIDTH)
 		{
 			int lastSpace = s.lastIndexOf(' ', MAXDECLARATIONWIDTH);
             //if (lastSpace < 0) lastSpace = MAXDECLARATIONWIDTH;
             if (lastSpace < 0) lastSpace = s.length();
 			printWriter.print(s.substring(0, lastSpace) + "\n      ");
 			while (lastSpace+1 < s.length() && s.charAt(lastSpace) == ' ') lastSpace++;
 			s = s.substring(lastSpace);
 		}
 		printWriter.print(s + "\n");
 	}
 
 	private StringBuffer sim_verDeclarationLine;
 	private int sim_verdeclarationprefix;
 
 	/*
 	 * Method to initialize the collection of signal names in a declaration.
 	 * The declaration starts with the string "header".
 	 */
 	private void initDeclaration(String header)
 	{
 		sim_verDeclarationLine = new StringBuffer();
 		sim_verDeclarationLine.append(header);
 		sim_verdeclarationprefix = header.length();
 	}
 
 	/*
 	 * Method to add "signame" to the collection of signal names in a declaration.
 	 */
 	private void addDeclaration(String signame)
 	{
 		if (sim_verDeclarationLine.length() + signame.length() + 3 > MAXDECLARATIONWIDTH)
 		{
 			printWriter.print(sim_verDeclarationLine.toString() + ";\n");
 			sim_verDeclarationLine.delete(sim_verdeclarationprefix, sim_verDeclarationLine.length());
 		}
 		if (sim_verDeclarationLine.length() != sim_verdeclarationprefix)
 			sim_verDeclarationLine.append(",");
 		sim_verDeclarationLine.append(" " + signame);
 	}
 
 	/*
 	 * Method to terminate the collection of signal names in a declaration
 	 * and write the declaration to the Verilog file.
 	 */
 	private void termDeclaration()
 	{
 		printWriter.print(sim_verDeclarationLine.toString() + ";\n");
 	}
 
 	/*
 	 * Method to adjust name "p" and return the string.
 	 * This code removes all index indicators and other special characters, turning
 	 * them into "_".
 	 */
 	private String nameNoIndices(String p)
 	{
 		StringBuffer sb = new StringBuffer();
 		if (Character.isDigit(p.charAt(0))) sb.append('_');
 		for(int i=0; i<p.length(); i++)
 		{
 			char chr = p.charAt(i);
 			if (!Character.isLetterOrDigit(chr) && chr != '_' && chr != '$') chr = '_';
 			sb.append(chr);
 		}
 		return sb.toString();
 	}
 
 	/****************************** SUBCLASSED METHODS FOR THE TOPOLOGY ANALYZER ******************************/
 
 	/**
 	 * Method to adjust a cell name to be safe for Verilog output.
 	 * @param name the cell name.
 	 * @return the name, adjusted for Verilog output.
 	 */
 	protected String getSafeCellName(String name)
 	{
 		return getSafeNetName(name);
 	}
 
 	/** Method to return the proper name of Power */
 	protected String getPowerName() { return "vdd"; }
 
 	/** Method to return the proper name of Ground */
 	protected String getGroundName() { return "gnd"; }
 
 	/** Method to return the proper name of a Global signal */
 	protected String getGlobalName(Global glob) { return "glbl." + glob.getName(); }
 
     /** Method to report that export names DO take precedence over
      * arc names when determining the name of the network. */
     protected boolean isNetworksUseExportedNames() { return true; }
 
 	/** Method to report that library names ARE always prepended to cell names. */
 	protected boolean isLibraryNameAlwaysAddedToCellName() { return true; }
 
 	/** Method to report that aggregate names (busses) ARE used. */
 	protected boolean isAggregateNamesSupported() { return true; }
 
 	/*
 	 * Method to adjust a network name to be safe for Verilog output.
 	 * Verilog does permit a digit in the first location; prepend a "_" if found.
 	 * Verilog only permits the "_" and "$" characters: all others are converted to "_".
 	 * Verilog does not permit nonnumeric indices, so "P[A]" is converted to "P_A_"
 	 * Verilog does not permit multidimensional arrays, so "P[1][2]" is converted to "P_1_[2]"
 	 *   and "P[1][T]" is converted to "P_1_T_"
 	 */
 	protected String getSafeNetName(String name)
 	{
 		// simple names are trivially accepted as is
 		boolean allAlnum = true;
 		int len = name.length();
 		if (len == 0) return name;
 		for(int i=0; i<len; i++)
 		{
 			if (!Character.isLetterOrDigit(name.charAt(i))) { allAlnum = false;   break; }
 		}
 		if (allAlnum && Character.isLetter(name.charAt(0))) return name;
 
 		StringBuffer sb = new StringBuffer();
 		for(int t=0; t<name.length(); t++)
 		{
 			char chr = name.charAt(t);
 			if (chr == '[' || chr == ']')
 			{
 				sb.append('_');
 				if (t+1 < name.length() && chr == ']' && name.charAt(t+1) == '[') t++;
 			} else
 			{
 				if (Character.isLetterOrDigit(chr) || chr == '$')
 					sb.append(chr); else
 						sb.append('_');
 			}
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Method to obtain Netlist information for a cell.
 	 * This is pushed to the writer because each writer may have different requirements for resistor inclusion.
 	 * Verilog ignores resistors.
 	 */
 	protected Netlist getNetlistForCell(Cell cell)
 	{
 		// get network information about this cell
 		boolean shortResistors = true;
 		Netlist netList = cell.getNetlist(shortResistors);
 		return netList;
 	}
 
 	/**
 	 * Method to tell whether the topological analysis should mangle cell names that are parameterized.
 	 */
 	protected boolean canParameterizeNames() { return true; }
 
 }
