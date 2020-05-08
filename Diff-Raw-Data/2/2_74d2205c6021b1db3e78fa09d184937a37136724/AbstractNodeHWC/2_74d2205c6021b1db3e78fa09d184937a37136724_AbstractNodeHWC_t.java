 /**
  * @(#)AbstractNodeHWC.java
  */
 
 package aurora.hwc;
 
 import java.io.*;
 import java.util.*;
 
 import org.w3c.dom.*;
 
 import aurora.*;
 import aurora.util.*;
 
 
 /**
  * Base class for all simple highway nodes.
  * 
  * @see NodeFreeway, NodeHighway, NodeUJSignal, NodeUJStop
  * 
  * @author Alex Kurzhanskiy
  * @version $Id: AbstractNodeHWC.java 135 2010-06-05 00:57:31Z akurzhan $
  */
 public abstract class AbstractNodeHWC extends AbstractNodeSimple {
 	private static final long serialVersionUID = 3609519064761135698L;
 	
 	protected double[][] weavingFactorMatrix = null;
 	protected AuroraIntervalVector[][] splitRatioMatrix = null;
 	protected AuroraIntervalVector[][] splitRatioMatrix0 = null;
 	protected Vector<AuroraIntervalVector[][]> srmProfile = new Vector<AuroraIntervalVector[][]>();
 	protected double srTP = 1.0/12.0; // split ratio matrix change period (default: 1/12 hour)
 	protected double srST = 0.0; // start time for split ratio profile
 	
 		
 	/**
 	 * Initializes the simple Node from given DOM structure.
 	 * @param p DOM node.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration
 	 */
 	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = true;
 		if ((p == null) || (myNetwork == null))
 			return !res;
 		if (initialized) {
 			try  {
 				if (p.hasChildNodes()) {
 					NodeList pp = p.getChildNodes();
 					Vector<String> srbuf = new Vector<String>();
 					Vector<String> wfbuf = new Vector<String>();
 					int i, j;
 					int m = 0;
 					int n = 0;
 					for (i = 0; i < pp.getLength(); i++) {
 						if (pp.item(i).getNodeName().equals("inputs"))
 							if (pp.item(i).hasChildNodes()) {
 								NodeList pp2 = pp.item(i).getChildNodes();
 								for (j = 0; j < pp2.getLength(); j++)
 									if (pp2.item(j).getNodeName().equals("input")) {
 										AbstractControllerSimple ctrl = null;
 										Node id_attr = pp2.item(j).getAttributes().getNamedItem("link_id");
 										if (id_attr == null)
 											id_attr = pp2.item(j).getAttributes().getNamedItem("id");
 										AbstractLink lk = myNetwork.getLinkById(Integer.parseInt(id_attr.getNodeValue()));
 										if (pp2.item(j).hasChildNodes()) {
 											NodeList pp3 = pp2.item(j).getChildNodes();
 											for (int k = 0; k < pp3.getLength(); k++) {
 												if (pp3.item(k).getNodeName().equals("splitratios"))
 													srbuf.add(pp3.item(k).getTextContent());
 												if (pp3.item(k).getNodeName().equals("weavingfactors"))
 													wfbuf.add(pp3.item(k).getTextContent());
 												if (pp3.item(k).getNodeName().equals("controller")) {
 													Node type_attr = pp3.item(k).getAttributes().getNamedItem("type");
 													String class_name = null;
 													if (type_attr != null)
 														class_name = myNetwork.getContainer().ctrType2Classname(type_attr.getNodeValue());
 													else
 														class_name = pp3.item(k).getAttributes().getNamedItem("class").getNodeValue();
 													Class c = Class.forName(class_name);
 													ctrl = (AbstractControllerSimple)c.newInstance();
 													ctrl.setMyLink(lk);
 													res &= ctrl.initFromDOM(pp3.item(k));
 												}
 											}
 										}
 										if (lk != null) {
 											int ilidx = predecessors.indexOf(lk);
 											if (ilidx < 0)
 												addInLink(lk, ctrl);
 											else {
 												setSimpleController(ctrl, ilidx);
 											}
 											m++;
 										}
 									}
 							}
 						if (pp.item(i).getNodeName().equals("outputs"))
 							if (pp.item(i).hasChildNodes()) {
 								NodeList pp2 = pp.item(i).getChildNodes();
 								for (j = 0; j < pp2.getLength(); j++)
 									if (pp2.item(j).getNodeName().equals("output")) {
 										Node id_attr = pp2.item(j).getAttributes().getNamedItem("link_id");
 										if (id_attr == null)
 											id_attr = pp2.item(j).getAttributes().getNamedItem("id");
 										AbstractLink olk = myNetwork.getLinkById(Integer.parseInt(id_attr.getNodeValue()));
 										int olidx = successors.indexOf(olk);
 										if (olidx < 0)
 											addOutLink(olk);
 										n++;
 									}
 							}
 						if (pp.item(i).getNodeName().equals("splitratios"))
 							initSplitRatioProfileFromDOM(pp.item(i));
 					}
 					int nIL = predecessors.size();
 					int nOL = successors.size();
 					if ((nIL > 0) && (nOL > 0)) {
 						weavingFactorMatrix = new double[nIL][nOL];
 						for (i = 0; i < nIL; i++) {
 							if (i < wfbuf.size()) {
 								StringTokenizer st = new StringTokenizer(wfbuf.get(i), ", \t");
 								j = 0;
 								while ((st.hasMoreTokens()) && (j < nOL)) {
 									try {
 										weavingFactorMatrix[i][j] = Double.parseDouble(st.nextToken());
 									}
 									catch(Exception e) {
 										weavingFactorMatrix[i][j] = 1;
 									}
 									j++;
 								}
 							}
 							else
 								for (j = 0; j < nOL; j++)
 									weavingFactorMatrix[i][j] = 1;
 						}
 					}
 					else {
 						weavingFactorMatrix = new double[m][n];
 						for (i = 0; i < m; i++)
 							for (j = 0; j < n; j++)
 								weavingFactorMatrix[i][j] = 1;
 					}
 					if ((nIL > 0) && (nOL > 0)) {
 						splitRatioMatrix = new AuroraIntervalVector[nIL][nOL];
 						splitRatioMatrix0 = new AuroraIntervalVector[nIL][nOL];
 						int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
 						for (i = 0; i < nIL; i++) {
 							if (i < srbuf.size()) {
 								StringTokenizer st = new StringTokenizer(srbuf.get(i), ", \t");
 								j = 0;
 								while ((st.hasMoreTokens()) && (j < nOL)) {
 									String srvtxt = st.nextToken();
 									AuroraIntervalVector srv = new AuroraIntervalVector();
 									srv.setRawIntervalVectorFromString(srvtxt);
 									int rsz = srv.size();
 									if (myNetwork.getContainer().isSimulation()) {
 										srv = new AuroraIntervalVector(sz);
 										srv.setIntervalVectorFromString(srvtxt);
 										for (int idx = rsz; idx < sz; idx++)
 											srv.get(idx).copy(srv.get(rsz-1));
 									}
 									splitRatioMatrix[i][j] = srv;
 									splitRatioMatrix0[i][j] = new AuroraIntervalVector();
 									splitRatioMatrix0[i][j].copy(srv);
 									j++;
 								}
 								while (j < nOL) {
 									if (myNetwork.getContainer().isSimulation()) {
 										splitRatioMatrix[i][j] = new AuroraIntervalVector(sz);
 										splitRatioMatrix0[i][j] = new AuroraIntervalVector(sz);
 									}
 									else {
 										splitRatioMatrix[i][j] = new AuroraIntervalVector();
 										splitRatioMatrix0[i][j] = new AuroraIntervalVector();
 									}
 									j++;
 								}
 							}
 							else
 								for (j = 0; j < nOL; j++)
 									if (myNetwork.getContainer().isSimulation()) {
 										splitRatioMatrix[i][j] = new AuroraIntervalVector(sz);
 										splitRatioMatrix0[i][j] = new AuroraIntervalVector(sz);
 									}
 									else {
 										splitRatioMatrix[i][j] = new AuroraIntervalVector();
 										splitRatioMatrix0[i][j] = new AuroraIntervalVector();
 									}
 						}
 					}
 				}
 				else
 					res = false;
 			}
 			catch(Exception e) {
 				res = false;
 				throw new ExceptionConfiguration(e.getMessage());
 			}
 			return res;
 		}
 		try  {
 			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
 			name = p.getAttributes().getNamedItem("name").getNodeValue();
 			if (p.hasChildNodes()) {
 				NodeList pp = p.getChildNodes();
 				for (int i = 0; i < pp.getLength(); i++) {
 					if (pp.item(i).getNodeName().equals("description")) {
 						description = pp.item(i).getTextContent();
 						if ((description == null) || (description.equals("null")))
 							description = "";
 					}
 					if (pp.item(i).getNodeName().equals("position")) {
 						position = new PositionNode();
 						res &= position.initFromDOM(pp.item(i));
 					}
 				}
 			}
 			else
 				res = false;
 		}
 		catch(Exception e) {
 			res = false;
 			throw new ExceptionConfiguration(e.getMessage());
 		}
 		initialized = true;
 		return res;
 	}
 	
 	/**
 	 * Initializes the split ratio profile from given DOM structure.
 	 * @param p DOM node.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration
 	 */
 	public boolean initSplitRatioProfileFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = true;
 		srmProfile.clear();
 		int m = getPredecessors().size();
 		int n = getSuccessors().size();
 		if ((p == null) || (!p.hasChildNodes()) || (m <= 0) || (n <= 0))
 			return !res;
 		try {
 			Node st_attr = p.getAttributes().getNamedItem("start_time");
 			if (st_attr != null) {
 				srST = Double.parseDouble(st_attr.getNodeValue()) / 3600;
 			}
 			Node dt_attr = p.getAttributes().getNamedItem("dt");
 			if (dt_attr == null)
 				dt_attr = p.getAttributes().getNamedItem("tp");
 			srTP = Double.parseDouble(dt_attr.getNodeValue());
			if (srTP > 0.5) // sampling period in seconds
 				srTP = srTP/3600;
 			NodeList pp = p.getChildNodes();
 			for (int ii = 0; ii < pp.getLength(); ii++)
 				if (pp.item(ii).getNodeName().equals("srm")) {
 					AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 					String bufM = pp.item(ii).getTextContent();
 					if ((bufM.isEmpty()) || (bufM.equals("\n")) || (bufM.equals("\r\n")))
 						continue;
 					StringTokenizer st1 = new StringTokenizer(bufM, ";");
 					int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
 					int i = -1;
 					while ((st1.hasMoreTokens()) && (++i < m)) {
 						String bufR = st1.nextToken();
 						StringTokenizer st2 = new StringTokenizer(bufR, ", \t");
 						int j = -1;
 						while ((st2.hasMoreTokens()) && (++j < n)) {
 							String srvtxt = st2.nextToken();
 							AuroraIntervalVector srv = new AuroraIntervalVector();
 							srv.setRawIntervalVectorFromString(srvtxt);
 							int rsz = srv.size();
 							if (myNetwork.getContainer().isSimulation()) {
 								srv = new AuroraIntervalVector(sz);
 								srv.setIntervalVectorFromString(srvtxt);
 								for (int idx = rsz; idx < sz; idx++)
 									srv.get(idx).copy(srv.get(rsz-1));
 							}
 							srm[i][j] = srv;
 						}
 						while (++j < n) {
 							if (myNetwork.getContainer().isSimulation())
 								srm[i][j] = new AuroraIntervalVector(sz);
 							else
 								srm[i][j] = new AuroraIntervalVector();
 						}
 					}
 					int lastIndex = i;
 					while (++i < m) {
 						for (int j = 0; j < n; j++) {
 							AuroraIntervalVector srv = new AuroraIntervalVector();
 							srv.copy(srm[lastIndex][j]);
 							srm[i][j] = srv;
 						}
 							
 					}
 					srmProfile.add(srm);
 				}
 		}
 		catch(Exception e) {
 			res = false;
 			throw new ExceptionConfiguration(e.getMessage());
 		}
 		return res;
 	}
 	
 	/**
 	 * Generates XML buffer for controller descriptions.<br>
 	 * If the print stream is specified, then XML buffer is written to the stream.
 	 * @param out print stream.
 	 * @throws IOException
 	 */
 	public void xmlDumpControllers(PrintStream out) throws IOException {
 		if (out == null)
 			out = System.out;
 		if (controller != null)
 			controller.xmlDump(out);
 		for (int i = 0; i < controllers.size(); i++)
 			if (controllers.get(i) != null)
 				controllers.get(i).xmlDump(out);
 		return;
 	}
 	
 	/**
 	 * Generates XML buffer for the split ratio profile.<br>
 	 * If the print stream is specified, then XML buffer is written to the stream.
 	 * @param out print stream.
 	 * @throws IOException
 	 */
 	public void xmlDumpSplitRatioProfile(PrintStream out) throws IOException {
 		if (out == null)
 			out = System.out;
 		out.print("<splitratios node_id=\"" + id + "\" start_time=\"" + Math.round(3600*srST) + "\" dt=\"" + Math.round(3600*srTP) + "\">\n");
 		if (srmProfile != null)
 			out.print(getSplitRatioProfileAsXML());
 		else
 			out.print(getSplitRatioMatrixAsXML());
 		out.print("</splitratios>\n");
 		return;
 	}
 	
 	/**
 	 * Generates XML description of the simple Node.<br>
 	 * If the print stream is specified, then XML buffer is written to the stream.
 	 * @param out print stream.
 	 * @throws IOException
 	 */
 	public void xmlDump(PrintStream out) throws IOException {
 		if (out == null)
 			out = System.out;
 		out.print("<node type=\"" + getTypeLetterCode() + "\" id=\"" + id + "\" name=\"" + name + "\">\n");
 		out.print("  <description>" + description + "</description>\n");
 		out.print("  <outputs>\n");
 		for (int i = 0; i < successors.size(); i++)
 			out.print("    <output link_id=\"" + successors.get(i).getId() + "\"/>\n");
 		out.print("  </outputs>\n  <inputs>\n");
 		for (int i = 0; i < predecessors.size(); i++) {
 			//String buf = "";
 			String buf2 = "";
 			for (int j = 0; j < successors.size(); j++) {
 				if (j > 0) {
 					//buf += ", ";
 					buf2 += ", ";
 				}
 				//buf += splitRatioMatrix[i][j].toString();
 				buf2 += Double.toString(weavingFactorMatrix[i][j]);
 			}
 			out.print("    <input link_id=\"" + predecessors.get(i).getId() + "\">\n");
 			//out.print("<splitratios>" + buf + "</splitratios>");
 			out.print("      <weavingfactors>" + buf2 + "</weavingfactors>\n");
 			//if (controllers.get(i) != null)
 				//controllers.get(i).xmlDump(out);
 			out.print("    </input>\n");
 		}
 		out.print("  </inputs>\n  ");
 		/*if (srmProfile != null)
 			out.print("<splitratios tp=\"" + Double.toString(srTP) + "\">\n" + getSplitRatioProfileAsXML() + "</splitratios>\n");*/
 		position.xmlDump(out);
 		out.print("\n</node>\n");
 		return;
 	}
 	
 	/**
 	 * Updates Node data.<br>
 	 * Invokes controllers if there are any,
 	 * computes input and output flows based on densities in the incoming
 	 * and outgoing links and split ratio matrix.
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		if (myNetwork.getContainer().getMySettings().isPrediction())
 			return dataUpdate1(ts);
 		return dataUpdate0(ts);
 	}
 
 	/**
 	 * Implementation greedy policy with fair distribution of demand.<br>
 	 * New implementation - unabridged.
 	 * (Algorithm of Ajith Muralidharan).
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	private synchronized boolean dataUpdate0(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		boolean res = super.dataUpdate(ts);
 		if (!res)
 			return res;
 		//
 		// start up
 		//
 		
 		int nIn = predecessors.size(); // number of inputs
 		int nOut = successors.size(); // number of outputs
 		int nTypes = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();  // number of vehicle types
 		//
 		// Set current split ratio matrix
 		//
 		double t = myNetwork.getSimTime(); // current simulation time
 		t -= srST;
 		int idx = (int)Math.floor(t/srTP);		
 		if (splitRatioMatrix0 != null)
 			setSplitRatioMatrix(splitRatioMatrix0);
 		else if ((!srmProfile.isEmpty()) && (idx >= 0)){
 			setSplitRatioMatrix(srmProfile.get(Math.min(idx, srmProfile.size()-1)));
 		}
 		else {
 			splitRatioMatrix = new AuroraIntervalVector[nIn][nOut];
 			for (int i = 0; i < nIn; i++)
 				for (int j = 0; j < nOut; j++)
 					splitRatioMatrix[i][j] = new AuroraIntervalVector(nTypes);
 		}
 		
 		//
 		// Initialize input demands
 		//
 		AuroraIntervalVector[] inDemand = new AuroraIntervalVector[nIn];
 		for (int i = 0; i < nIn; i++) {
 			inDemand[i] = ((AbstractLinkHWC)predecessors.get(i)).getFlow();
 			if ((myNetwork.isControlled()) && (controllers.get(i) != null)) {
 				AuroraInterval sumDemand = inDemand[i].sum();
 				double controllerRate = (Double)controllers.get(i).computeInput(this);
 				if (controllerRate < sumDemand.getUpperBound()) { // adjust inputs according to controller rates
 					double c = controllerRate / sumDemand.getUpperBound();
 					for (int ii = 0; ii < nTypes; ii++)
 						inDemand[i].get(ii).setUpperBound(c * inDemand[i].get(ii).getUpperBound());
 				}
 			}
 		}
 		//
 		// Initialize output capacities
 		//
 		AuroraInterval[] outCapacity = new AuroraInterval[nOut];
 		for (int j = 0; j < nOut; j++)
 			outCapacity[j] = ((AbstractLinkHWC)successors.get(j)).getCapacity();
 		//
 		// Initialize split ratio matrix taking into account multiple vehicle types
 		//
 		// 1. Fill in the values
 		double[][] srm = new double[nIn * nTypes][nOut];
 		for (int j = 0; j < nOut; j++)
 			for (int i = 0; i < nIn; i++)
 				for (int ii = 0; ii < nTypes; ii++)
 					srm[i + ii*nIn][j] = splitRatioMatrix[i][j].get(ii).getCenter();
 		// 2. Make sure split ratio matrix is valid
 		Util.normalizeMatrix(srm);
 					
 		// 3. Record outputs with undefined split ratios
 		Vector<Integer> badColumns = new Vector<Integer>();
 		for (int j = 0; j < nOut; j++) {
 			boolean badColumn = false;
 			for (int i = 0; i < nIn*nTypes; i++)
 				if (srm[i][j] < 0)
 					if (Util.countNegativeElements(srm, i) > 1)
 						badColumn = true;
 					else
 						srm[i][j] = Math.max(0, 1 - Util.rowSumPositive(srm, i));
 			if (badColumn) // save indices of the outputs that have undefined split ratios
 				badColumns.add(j);	
 		}
 		// 4. Collect info about available share for undefined split ratios
 		//    and the number of undefined split ratios for each input
 		double[][] inputsSRInfo = new double[nIn*nTypes][2];
 		for (int i = 0; i < nIn*nTypes; i++) {
 			inputsSRInfo[i][1] = Util.countNegativeElements(srm, i);  // number of undefined split ratios
 			if (inputsSRInfo[i][1] < 1)
 				inputsSRInfo[i][0] = 0;
 			else
 				inputsSRInfo[i][0] = 1; // remaining share for a split ratio
 		}
 		//
 		// Reduce input demand according to capacities on the outputs for which all split ratios are known
 		//
 		
 		for (int j = 0; j < nOut; j++) {
 			if (badColumns.indexOf((Integer)j) > -1)
 				continue;
 			AuroraInterval sumIns = new AuroraInterval();
 			Vector<Integer> contributors = new Vector<Integer>();
 			// compute total input demand assigned to output 'j'
 			for (int i = 0; i < nIn; i++) {
 				boolean isContributor = false;
 				for (int ii = 0; ii < nTypes; ii++) {
 					AuroraInterval val = new AuroraInterval();
 					val.copy(inDemand[i].get(ii));
 					val.affineTransform(srm[i + ii*nIn][j], 0);
 					sumIns.add(val);
 					if (val.getUpperBound() > 0.0001)
 						isContributor = true;
 					inputsSRInfo[i + ii*nIn][0] -= srm[i + ii*nIn][j];
 				} // vehicle types 'for' loop
 				if (isContributor)
 					contributors.add((Integer)i);
 			} // row 'for' loop
 			// adjust inputs to capacity
 			double lbc = 1;
 			double ubc = 1;
 			if (outCapacity[j].getLowerBound() < sumIns.getLowerBound())
 				lbc = outCapacity[j].getLowerBound() / sumIns.getLowerBound();
 			if (outCapacity[j].getUpperBound() < sumIns.getUpperBound())
 				ubc = outCapacity[j].getUpperBound() / sumIns.getUpperBound();
 			if ((lbc < 1) || (ubc < 1)) {
 				for (int i = 0; i < contributors.size(); i++) {
 					for (int ii = 0; ii < nTypes; ii++) {
 						if ((inDemand[contributors.get(i)].get(ii).getSize() == 0) && (lbc == ubc))
 							inDemand[contributors.get(i)].get(ii).affineTransform(lbc, 0);
 						else {
 							inDemand[contributors.get(i)].get(ii).affineTransformLB(lbc, 0);
 							inDemand[contributors.get(i)].get(ii).affineTransformUB(ubc, 0);
 						}
 					} // vehicle types 'for' loop
 				} // contributors 'for' loop
 			} // 'if'
 		} // column 'for' loop	
 
 		//
 		// Process outputs with undefined split ratios if there are any
 		//
 		if (!badColumns.isEmpty()) {
 			// 1. Reduce inputs according to available capacities for known split ratios
 			double[] outDemand = new double[badColumns.size()];
 			for (int j = 0; j < badColumns.size(); j++) {
 				AuroraInterval sumIns = new AuroraInterval();
 				for (int i = 0; i < nIn; i++) {
 					for (int ii = 0; ii < nTypes; ii++) {
 						AuroraInterval val = new AuroraInterval();
 						val.copy(inDemand[i].get(ii));
 						double sr = srm[i + ii*nIn][badColumns.get(j)];
 						if (sr >= 0) {
 							val.affineTransform(sr, 0);
 							sumIns.add(val);
 						}
 						else
 							srm[i + ii*nIn][badColumns.get(j)] = 0.0;
 					} // vehicle types 'for' loop
 				} // row 'for' loop
 				outDemand[j] = sumIns.getCenter();
 			}
 			// 2. Fill in the remaining capacity uniformly
 			for (int i = 0; i < nIn; i++) {
 				for (int ii = 0; ii < nTypes; ii++) {
 					double demand = inDemand[i].get(ii).getCenter();
 					while (inputsSRInfo[i + ii*nIn][0] > 0.00000001) {
 						double minRatio = 100.0;
 						double maxRatio = 0.0;
 						int minIndex = 0;
 						for (int j = 0; j < badColumns.size(); j++) {
 							if (splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() >= 0)
 								continue;
 							double a2cRatio;
 							if (outCapacity[badColumns.get(j)].getCenter() > 0)
 								a2cRatio = outDemand[j] / outCapacity[badColumns.get(j)].getCenter();
 							else
 								a2cRatio = 1.0;
 							if (a2cRatio < minRatio) {
 								minRatio = a2cRatio;
 								minIndex = j;
 							}
 							if (a2cRatio > maxRatio)
 								maxRatio = a2cRatio;
 						} // column 'for' loop
 						if ((maxRatio - minRatio) < 0.0000001)
 							break;
 						double flow;
 						flow = maxRatio * outCapacity[badColumns.get(minIndex)].getCenter() - outDemand[minIndex];
 						flow = Math.min(flow, inputsSRInfo[i + ii*nIn][0]*demand);
 						double sr;
 						if (demand > 0.0001)
 							sr = flow / demand;
 						else
 							sr = inputsSRInfo[i + ii*nIn][0];
 						srm[i + ii*nIn][badColumns.get(minIndex)] += sr;
 						inputsSRInfo[i + ii*nIn][0] -= sr;
 						inputsSRInfo[i + ii*nIn][0] = Math.max(0, inputsSRInfo[i + ii*nIn][0]);
 						outDemand[minIndex] += flow;
 						if (sr < 0.000000001)
 							break;
 					} // 'while' loop
 				} // vehicle types 'for' loop
 			} // row 'for' loop
 			// 3. Assign the remaining split ratio shares proportionally to capacities
 			for (int i = 0; i < nIn; i++) {
 				for (int ii = 0; ii < nTypes; ii++) {
 					AuroraInterval totalCapacity = new AuroraInterval(); 
 					for (int j = 0; j < badColumns.size(); j++) {
 						if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
 							(inputsSRInfo[i + ii*nIn][1] > 0))
 							totalCapacity.add(outCapacity[badColumns.get(j)]);
 					}
 					for (int j = 0; j < badColumns.size(); j++) {
 						if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
 							(inputsSRInfo[i + ii*nIn][1] > 0)) {
 							if (totalCapacity.getCenter() > 0)
 								srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0] * (outCapacity[badColumns.get(j)].getCenter()/totalCapacity.getCenter());
 							else
 								srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0]/inputsSRInfo[i + ii*nIn][1];
 						}
 					}
 				} // vehicle types 'for' loop
 			} // row 'for' loop
 			// 4. Reduce inputs according to capacities if necessary 
 			for (int j = 0; j < badColumns.size(); j++) {
 				AuroraInterval sumIns = new AuroraInterval();
 				Vector<Integer> contributors = new Vector<Integer>();
 				for (int i = 0; i < nIn; i++) {
 					boolean isContributor = false;
 					for (int ii = 0; ii < nTypes; ii++) {
 						AuroraInterval val = new AuroraInterval();
 						val.copy(inDemand[i].get(ii));
 						if (srm[i + ii*nIn][badColumns.get(j)] >= 0) {
 							val.affineTransform(srm[i + ii*nIn][badColumns.get(j)], 0);
 							sumIns.add(val);
 							if (val.getUpperBound() > 0.0001)
 								isContributor = true;
 						}
 					} // vehicle types 'for' loop
 					if (isContributor)
 						contributors.add((Integer)i);
 				} // row 'for' loop
 				double cc = 1;
 				if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
 					cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
 				if (cc < 1)
 					for (int i = 0; i < contributors.size(); i++)
 						for (int ii = 0; ii < nTypes; ii++)
 							inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
 			}
 		} // end of processing undefined split ratios
 		//
 		// Assign split ratios
 		//
 		for (int j = 0; j < nOut; j++)
 			for (int i = 0; i < nIn; i++)
 				for (int ii = 0; ii < nTypes; ii++) {
 					splitRatioMatrix[i][j].get(ii).setCenter(srm[i + ii*nIn][j], 0);
 				}
 		//
 		// Assign input flows
 		//
 		for (int i = 0; i < nIn; i++) {
 			inputs.set(i, inDemand[i]);
 			AbstractControllerSimple ctrl = controllers.get(i);
 			if (ctrl != null)
 				ctrl.setActualInput(new Double(inDemand[i].sum().getUpperBound()));
 		}
 		//
 		// Assign output weaving factors
 		//
 		for (int i = 0; i < nIn; i++) {
 			double[] owf = new double[nTypes];
 			for (int ii = 0; ii < nTypes; ii++) {
 				double wf = 1;
 				for (int j = 0; j < nOut; j++)
 					wf += (Math.max(1, -weavingFactorMatrix[i][j]) - 1) * srm[i + ii*nIn][j];
 				owf[ii] = wf;
 			}
 			((AbstractLinkHWC)predecessors.get(i)).setOutputWeavingFactors(owf);
 		}
 		//
 		// Assign output flows
 		//
 		for (int j = 0; j < nOut; j++) {
 			AuroraIntervalVector outFlow = new AuroraIntervalVector(nTypes);
 			AuroraIntervalVector outFlow2 = new AuroraIntervalVector(nTypes);
 			for (int i = 0; i < nIn; i++) {
 				double[] splitRatios = new double[nTypes];
 				for (int ii = 0; ii < nTypes; ii++)
 					splitRatios[ii] = srm[i + ii*nIn][j];
 				AuroraIntervalVector flw = new AuroraIntervalVector(nTypes);
 				AuroraIntervalVector flw2 = new AuroraIntervalVector(nTypes);
 				flw.copy(inDemand[i]);
 				flw.affineTransform(splitRatios, 0);
 				outFlow.add(flw);
 				flw2.copy(flw);
 				flw2.affineTransform(Math.max(1, weavingFactorMatrix[i][j]), 0);
 				outFlow2.add(flw2);
 			}
 			outputs.set(j, outFlow);
 			double nmL = outFlow2.sum().getLowerBound();
 			double dnmL = outFlow.sum().getLowerBound();
 			if (nmL > Util.EPSILON)
 				nmL = nmL / dnmL;
 			else
 				nmL = 1;
 			double nmU = outFlow2.sum().getUpperBound();
 			double dnmU = outFlow.sum().getUpperBound();
 			if (nmU > Util.EPSILON)
 				nmU = nmU / dnmU;
 			else
 				nmU = 1;
 			if (nmU - nmL < Util.EPSILON) // avoid rounding error
 				nmU = nmL;
 			((AbstractLinkHWC)successors.get(j)).setInputWeavingFactor(new AuroraInterval((nmL+nmU)/2, Math.abs(nmU-nmL)));
 		}
 		return res;
 	}
 
 	/**
 	 * Flow range computation.<br> It is implied that all the split ratios are well defined.
 	 * Proper handling of intervals.
 	 * @param ts time step.
 	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
 	 * @throws ExceptionDatabase, ExceptionSimulation
 	 */
 	private synchronized boolean dataUpdate1(int ts) throws ExceptionDatabase, ExceptionSimulation {
 		boolean res = super.dataUpdate(ts);
 		if (!res)
 			return res;
 		//
 		// start up
 		//
 		int nIn = predecessors.size(); // number of inputs
 		int nOut = successors.size(); // number of outputs
 		int nTypes = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();  // number of vehicle types
 		//
 		// Set current split ratio matrix
 		//
 		double t = myNetwork.getSimTime(); // current simulation time
 		t -= srST;
 		int idx = (int)Math.floor(t/srTP);
 		if (splitRatioMatrix0 != null)
 			setSplitRatioMatrix(splitRatioMatrix0);
 		else if ((!srmProfile.isEmpty()) && (idx >= 0))
 			setSplitRatioMatrix(srmProfile.get(Math.min(idx, srmProfile.size()-1)));
 		else {
 			splitRatioMatrix = new AuroraIntervalVector[nIn][nOut];
 			for (int i = 0; i < nIn; i++)
 				for (int j = 0; j < nOut; j++)
 					splitRatioMatrix[i][j] = new AuroraIntervalVector(nTypes);
 		}
 		//
 		// Initialize input demands
 		//
 		AuroraIntervalVector[] demandLI = new AuroraIntervalVector[nIn];
 		AuroraIntervalVector[] demandLO = new AuroraIntervalVector[nIn];
 		AuroraIntervalVector[] demandUI = new AuroraIntervalVector[nIn];
 		AuroraIntervalVector[] demandUO = new AuroraIntervalVector[nIn];
 		AuroraIntervalVector[] demandUOm = new AuroraIntervalVector[nIn];
 		AuroraIntervalVector[] demandUOt = new AuroraIntervalVector[nIn];
 		for (int i = 0; i < nIn; i++) {
 			demandLI[i] = ((AbstractLinkHWC)predecessors.get(i)).getFlowL();
 			demandLO[i] = new AuroraIntervalVector();
 			demandLO[i].copy(demandLI[i]);
 			demandLI[i].toUpper();
 			demandLO[i].toLower();
 			demandUI[i] = ((AbstractLinkHWC)predecessors.get(i)).getFlowU();
 			demandUO[i] = new AuroraIntervalVector();
 			demandUO[i].copy(demandUI[i]);
 			demandUI[i].toLower();
 			demandUO[i].toUpper();
 			if ((myNetwork.isControlled()) && (controllers.get(i) != null)) {
 				double controllerRate = (Double)controllers.get(i).computeInput(this);
 				AuroraInterval sumDemandL = demandLI[i].sum();
 				if (controllerRate < sumDemandL.getCenter()) { // adjust lower best case input bounds according to controller rates
 					double c = controllerRate / sumDemandL.getCenter();
 					for (int ii = 0; ii < nTypes; ii++)
 						demandLI[i].get(ii).setCenter(c * demandLI[i].get(ii).getCenter(), 0);
 				}
 				sumDemandL = demandLO[i].sum();
 				if (controllerRate < sumDemandL.getCenter()) { // adjust upper best case input bounds according to controller rates
 					double c = controllerRate / sumDemandL.getCenter();
 					for (int ii = 0; ii < nTypes; ii++)
 						demandLO[i].get(ii).setCenter(c * demandLO[i].get(ii).getCenter(), 0);
 				}
 				AuroraInterval sumDemandU = demandUI[i].sum();
 				if (controllerRate < sumDemandU.getCenter()) { // adjust lower worst case input bounds according to controller rates
 					double c = controllerRate / sumDemandU.getCenter();
 					for (int ii = 0; ii < nTypes; ii++)
 						demandUI[i].get(ii).setCenter(c * demandUI[i].get(ii).getCenter(), 0);
 				}
 				sumDemandU = demandUO[i].sum();
 				if (controllerRate < sumDemandU.getCenter()) { // adjust upper worst case input bounds according to controller rates
 					double c = controllerRate / sumDemandU.getCenter();
 					for (int ii = 0; ii < nTypes; ii++)
 						demandUO[i].get(ii).setCenter(c * demandUO[i].get(ii).getCenter(), 0);
 				}
 			}
 			demandUOm[i] = new AuroraIntervalVector();
 			demandUOm[i].copy(demandUO[i]);
 			demandUOt[i] = new AuroraIntervalVector();
 			demandUOt[i].copy(demandUO[i]);
 		}
 		//
 		// Initialize output capacities
 		//
 		AuroraInterval[] outCapacityL = new AuroraInterval[nOut];
 		AuroraInterval[] outCapacityU = new AuroraInterval[nOut];
 		for (int j = 0; j < nOut; j++) {
 			outCapacityL[j] = ((AbstractLinkHWC)successors.get(j)).getCapacityL();
 			outCapacityU[j] = ((AbstractLinkHWC)successors.get(j)).getCapacityU();
 		}
 		//
 		// Initialize split ratio matrix taking into account multiple vehicle types
 		//
 		// 1. Fill in the values
 		double[][] srm = new double[nIn * nTypes][nOut];
 		for (int j = 0; j < nOut; j++)
 			for (int i = 0; i < nIn; i++)
 				for (int ii = 0; ii < nTypes; ii++)
 					srm[i + ii*nIn][j] = Math.max(0, splitRatioMatrix[i][j].get(ii).getCenter()); // no undefined split ratios here
 		// 2. Make sure split ratio matrix is valid
 		Util.normalizeMatrix(srm);
 		// 3. Record outputs with undefined split ratios
 		Vector<Integer> badColumns = new Vector<Integer>();
 		for (int j = 0; j < nOut; j++) {
 			boolean badColumn = false;
 			for (int i = 0; i < nIn*nTypes; i++)
 				if (srm[i][j] < 0)
 					if (Util.countNegativeElements(srm, i) > 1)
 						badColumn = true;
 					else
 						srm[i][j] = Math.max(0, 1 - Util.rowSumPositive(srm, i));
 			if (badColumn) // save indices of the outputs that have undefined split ratios
 				badColumns.add(j);	
 		}
 		// 4. Collect info about available share for undefined split ratios
 		//    and the number of undefined split ratios for each input
 		double[][] inputsSRInfo = new double[nIn*nTypes][2];
 		for (int i = 0; i < nIn*nTypes; i++) {
 			inputsSRInfo[i][1] = Util.countNegativeElements(srm, i);  // number of undefined split ratios
 			if (inputsSRInfo[i][1] < 1)
 				inputsSRInfo[i][0] = 0;
 			else
 				inputsSRInfo[i][0] = 1; // remaining share for a split ratio
 		}
 		//
 		// Reduce input demand according to capacities on the outputs for which all split ratios are known
 		//
 		for (int j = 0; j < nOut; j++) {
 			if (badColumns.indexOf((Integer)j) > -1)
 				continue;
 			AuroraInterval sumInsLI = new AuroraInterval();
 			AuroraInterval sumInsLO = new AuroraInterval();
 			AuroraInterval sumInsUI = new AuroraInterval();
 			Vector<Integer> contributors = new Vector<Integer>();
 			// compute total input demand assigned to output 'j'
 			for (int i = 0; i < nIn; i++) {
 				boolean isContributor = false;
 				for (int ii = 0; ii < nTypes; ii++) {
 					AuroraInterval val = new AuroraInterval();
 					val.copy(demandLI[i].get(ii));
 					val.affineTransform(srm[i + ii*nIn][j], 0);
 					sumInsLI.add(val);
 					val.copy(demandLO[i].get(ii));
 					val.affineTransform(srm[i + ii*nIn][j], 0);
 					sumInsLO.add(val);
 					val.copy(demandUI[i].get(ii));
 					val.affineTransform(srm[i + ii*nIn][j], 0);
 					sumInsUI.add(val);
 					if (srm[i + ii*nIn][j] > 0)
 						isContributor = true;
 					inputsSRInfo[i + ii*nIn][0] -= srm[i + ii*nIn][j];
 				} // vehicle types 'for' loop
 				if (isContributor)
 					contributors.add((Integer)i);
 			} // row 'for' loop
 			// adjust inputs to capacity
 			double lbcI = Math.min(1, outCapacityL[j].getUpperBound()/sumInsLI.getCenter());
 			if (lbcI == Double.NaN)
 				lbcI = 1;
 			double lbcO = Math.min(1, outCapacityL[j].getLowerBound()/sumInsLO.getCenter());
 			if (lbcO == Double.NaN)
 				lbcO = 1;
 			double ubcI = Math.min(1, outCapacityU[j].getLowerBound()/sumInsUI.getCenter());
 			if (ubcI == Double.NaN)
 				ubcI = 1;
 			for (int i = 0; i < contributors.size(); i++) {
 				demandLI[contributors.get(i)].affineTransform(lbcI, 0);
 				demandLO[contributors.get(i)].affineTransform(lbcO, 0);
 				demandUI[contributors.get(i)].affineTransform(ubcI, 0);
 			} // contributors 'for' loop
 		} // column 'for' loop
 		//
 		// Special treatment for demandUO
 		//
 		for (int jj = 0; jj < nOut; jj++) {
 			if (badColumns.indexOf((Integer)jj) > -1)
 				continue;
 			for (int j = 0; j < nOut; j++) {
 				AuroraInterval sumInsU = new AuroraInterval();
 				Vector<Integer> contributorsU = new Vector<Integer>();
 				for (int i = 0; i < nIn; i++) {
 					boolean isContributorU = false;
 					for (int ii = 0; ii < nTypes; ii++) {
 						AuroraInterval val = new AuroraInterval();
 						val.copy(demandUI[i].get(ii));
 						val.affineTransform(srm[i + ii*nIn][j], 0);
 						if (val.getCenter() > 0.000001)
 							isContributorU = true;
 						sumInsU.add(val);
 					} // vehicle types 'for' loop
 					if (isContributorU)
 						contributorsU.add((Integer)i);
 				} // row 'for' loop
 				double ubcO;
 				if (jj == j)
 					ubcO = Math.min(1, outCapacityU[j].getUpperBound()/sumInsU.getCenter());
 				else
 					ubcO = Math.min(1, outCapacityL[j].getUpperBound()/sumInsU.getCenter());
 				if (ubcO == Double.NaN)
 					ubcO = 1;
 				for (int i = 0; i < contributorsU.size(); i++)
 					demandUOt[contributorsU.get(i)].affineTransform(ubcO, 0);
 			} // column 'for' loop
 			// update demandUOm and re-initialize demandUOt
 			for (int i = 0; i < nIn; i++) {
 				if (jj == 0)
 					demandUOm[i].copy(demandUOt[i]);
 				else
 					for (int ii = 0; ii < nTypes; ii++)
 						demandUOm[i].get(ii).setCenter(Math.max(demandUOm[i].get(ii).getCenter(), demandUOt[i].get(ii).getCenter()));
 				demandUO[i].copy(demandUO[i]);
 			}
 		}
 		//
 		// Set adjusted demandUO
 		//
 		for (int i = 0; i < nIn; i++)
 			demandUO[i].copy(demandUOm[i]);
 		//
 		// Assign split ratios
 		//
 		for (int j = 0; j < nOut; j++)
 			for (int i = 0; i < nIn; i++)
 				for (int ii = 0; ii < nTypes; ii++) {
 					splitRatioMatrix[i][j].get(ii).setCenter(srm[i + ii*nIn][j], 0);
 				}
 		//
 		// Assign input flows
 		//
 		for (int i = 0; i < nIn; i++) {
 			AbstractLinkHWC lnk = (AbstractLinkHWC)predecessors.get(i);
 			lnk.inverseOutputBounds(demandLI[i].sum().getCenter() > demandUI[i].sum().getCenter());
 			for (int ii = 0; ii < nTypes; ii++)
 				demandLI[i].get(ii).setBounds(demandLI[i].get(ii).getCenter(), demandUI[i].get(ii).getCenter());
 			inputs.set(i, demandLI[i]);
 			AbstractControllerSimple ctrl = controllers.get(i);
 			if (ctrl != null)
 				ctrl.setActualInput(new Double(demandLI[i].sum().getCenter())); // FIXME: should be interval
 		}
 		//
 		// Assign output weaving factors
 		//
 		for (int i = 0; i < nIn; i++) {
 			double[] owf = new double[nTypes];
 			for (int ii = 0; ii < nTypes; ii++) {
 				double wf = 1;
 				for (int j = 0; j < nOut; j++)
 					wf += (Math.max(1, -weavingFactorMatrix[i][j]) - 1) * srm[i + ii*nIn][j];
 				owf[ii] = wf;
 			}
 			((AbstractLinkHWC)predecessors.get(i)).setOutputWeavingFactors(owf);
 		}
 		//
 		// Assign output flows
 		//
 		for (int j = 0; j < nOut; j++) {
 			AuroraIntervalVector outFlowL = new AuroraIntervalVector(nTypes);
 			AuroraIntervalVector outFlowL2 = new AuroraIntervalVector(nTypes);
 			AuroraIntervalVector outFlowU = new AuroraIntervalVector(nTypes);
 			AuroraIntervalVector outFlowU2 = new AuroraIntervalVector(nTypes);
 			for (int i = 0; i < nIn; i++) {
 				double[] splitRatios = new double[nTypes];
 				for (int ii = 0; ii < nTypes; ii++)
 					splitRatios[ii] = srm[i + ii*nIn][j];
 				AuroraIntervalVector flw = new AuroraIntervalVector(nTypes);
 				AuroraIntervalVector flw2 = new AuroraIntervalVector(nTypes);
 				flw.copy(demandLO[i]);
 				flw.affineTransform(splitRatios, 0);
 				outFlowL.add(flw);
 				flw2.copy(flw);
 				flw2.affineTransform(Math.max(1, weavingFactorMatrix[i][j]), 0);
 				outFlowL2.add(flw2);
 				flw.copy(demandUO[i]);
 				flw.affineTransform(splitRatios, 0);
 				outFlowU.add(flw);
 				flw2.copy(flw);
 				flw2.affineTransform(Math.max(1, weavingFactorMatrix[i][j]), 0);
 				outFlowU2.add(flw2);
 			}
 			// compute bounds for the input weaving factor
 			double nmL = outFlowL2.sum().getCenter();
 			double dnmL = outFlowL.sum().getCenter();
 			if (nmL > Util.EPSILON)
 				nmL = nmL / dnmL;
 			else
 				nmL = 1;
 			double nmU = outFlowU2.sum().getCenter();
 			double dnmU = outFlowU.sum().getCenter();
 			if (nmU > Util.EPSILON)
 				nmU = nmU / dnmU;
 			else
 				nmU = 1;
 			if (nmU - nmL < Util.EPSILON) // avoid rounding error
 				nmU = nmL;
 			((AbstractLinkHWC)successors.get(j)).inverseIWFBounds(nmL > nmU);
 			((AbstractLinkHWC)successors.get(j)).setInputWeavingFactor(new AuroraInterval((nmL+nmU)/2, Math.abs(nmU-nmL)));
 			// scale back upper out-flow bound if necessary
 			double ubc = outCapacityU[j].getCenter() / outFlowU.sum().getCenter();
 			if (ubc == Double.NaN)
 				ubc = 1;
 			if (ubc < 1)
 				outFlowU.affineTransform(ubc, 0);
 			((AbstractLinkHWC)successors.get(j)).inverseInputBounds(outFlowL.sum().getCenter() > outFlowU.sum().getCenter());
 			for (int ii = 0; ii < nTypes; ii++)
 				outFlowL.get(ii).setBounds(outFlowL.get(ii).getCenter(), outFlowU.get(ii).getCenter());
 			outputs.set(j, outFlowL);
 		}
 		return res;
 	}
 	
 	/**
 	 * Validates Node configuration.<br>
 	 * Checks if the dimensions of the split ratio matrix are correct.
 	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration
 	 */
 	public boolean validate() throws ExceptionConfiguration {
 		boolean res = super.validate();
 		if (weavingFactorMatrix == null) {
 			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Weaving factor matrix not assigned."));
 			res = false;
 			return res;
 		}
 		if (weavingFactorMatrix.length != inputs.size()) {
 			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Weaving factor matrix inputs (" + Integer.toString(weavingFactorMatrix.length) + ") does not match number of in-links (" + Integer.toString(inputs.size()) + ")."));
 			res = false;
 		}
 		if (weavingFactorMatrix.length>0 && weavingFactorMatrix[0].length != outputs.size()) {
 			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Weaving factor matrix outputs (" + Integer.toString(weavingFactorMatrix[0].length) + ") does not match number of out-links (" + Integer.toString(outputs.size()) + ")."));
 			res = false;
 		}
 		if (splitRatioMatrix == null) {
 			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Split ratio matrix not assigned."));
 			res = false;
 			return res;
 		}
 		if (splitRatioMatrix.length != inputs.size()) {
 			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Split ratio matrix inputs (" + Integer.toString(splitRatioMatrix.length) + ") does not match number of in-links (" + Integer.toString(inputs.size()) + ")."));
 			res = false;
 		}
 		if (splitRatioMatrix[0].length != outputs.size()) {
 			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Split ratio matrix outputs (" + Integer.toString(splitRatioMatrix[0].length) + ") does not match number of out-links (" + Integer.toString(outputs.size()) + ")."));
 			res = false;
 		}
 		return res;
 	}
 	
 	/**
 	 * Returns compatible simple controller type names.
 	 */
 	public String[] getSimpleControllerTypes() {
 		String[] ctrlTypes = {"ALINEA",
 							  "Traffic Responsive",
 							  "TOD",
 							  "VSL TOD",
 							  "Simple Signal"};
 		return ctrlTypes;
 	}
 	
 	/**
 	 * Returns compatible simple controller classes.
 	 */
 	public String[] getSimpleControllerClasses() {
 		String[] ctrlClasses = {"aurora.hwc.control.ControllerALINEA",
 								"aurora.hwc.control.ControllerTR",
 								"aurora.hwc.control.ControllerTOD",
 								"aurora.hwc.control.ControllerVSLTOD",
 								"aurora.hwc.control.ControllerSimpleSignal"};
 		return ctrlClasses;
 	}
 	
 	/**
 	 * Returns compatible queue controller type names.
 	 */
 	public String[] getQueueControllerTypes() {
 		String[] qcTypes = {"Queue Override",
 							"Proportional",
 							"PI Control"};
 		return qcTypes;
 	}
 	
 	/**
 	 * Returns compatible queue controller classes.
 	 */
 	public String[] getQueueControllerClasses() {
 		String[] qcClasses = {"aurora.hwc.control.QOverride",
 							  "aurora.hwc.control.QProportional",
 							  "aurora.hwc.control.QPI"};
 		return qcClasses;
 	}
 	
 	/**
 	 * Returns weaving factor matrix.
 	 */
 	public double[][] getWeavingFactorMatrix() {
 		if (weavingFactorMatrix == null)
 			return null;
 		int m = weavingFactorMatrix.length;
 		int n = weavingFactorMatrix[0].length;
 		double[][] wfm = new double[m][n];
 		for (int i = 0; i < m; i++)
 			for (int j = 0; j < n; j++)
 				wfm[i][j] = weavingFactorMatrix[i][j];
 		return wfm;
 	}
 	
 	/**
 	 * Returns split ratio matrix.
 	 */
 	public AuroraIntervalVector[][] getSplitRatioMatrix() {
 		if (splitRatioMatrix == null)
 			return null;
 		int m = splitRatioMatrix.length;
 		int n = splitRatioMatrix[0].length;
 		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 		for (int i = 0; i < m; i++)
 			for (int j = 0; j < n; j++) {
 				srm[i][j] = new AuroraIntervalVector();
 				srm[i][j].copy(splitRatioMatrix[i][j]);
 			}
 		return srm;
 	}
 	
 	/**
 	 * Returns split ratio matrix template.
 	 */
 	public AuroraIntervalVector[][] getSplitRatioMatrix0() {
 		if (splitRatioMatrix0 == null)
 			return null;
 		int m = splitRatioMatrix0.length;
 		int n = splitRatioMatrix0[0].length;
 		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 		for (int i = 0; i < m; i++)
 			for (int j = 0; j < n; j++) {
 				srm[i][j] = new AuroraIntervalVector();
 				srm[i][j].copy(splitRatioMatrix0[i][j]);
 			}
 		return srm;
 	}
 	
 	/**
 	 * Returns split ratio profile.
 	 */
 	public Vector<AuroraIntervalVector[][]> getSplitRatioProfile() {
 		return srmProfile;
 	}
 	
 	/**
 	 * Returns split ratio profile as text buffer.
 	 */
 	public String getSplitRatioProfileAsText() {
 		String buf = "";
 		for (int i = 0; i < srmProfile.size(); i++) {
 			AuroraIntervalVector[][] srm = srmProfile.get(i);
 			int m = 0;
 			int n = 0;
 			if (srm != null) {
 				m = srm.length;
 				n = srm[0].length;
 			}
 			if (i > 0)
 				buf += "\n";
 			for (int j = 0; j < m; j++) {
 				if (j > 0)
 					buf += "; ";
 				for (int k = 0; k < n; k++) {
 					if (k > 0)
 						buf += ", ";
 					buf += srm[j][k].toString();
 				}
 			}
 		}
 		return buf;
 	}
 	
 	/**
 	 * Returns split ratio matrix as XML buffer.
 	 */
 	public String getSplitRatioMatrixAsXML() {
 		String buf = "";
 		int m = 0;
 		int n = 0;
 		if (splitRatioMatrix != null) {
 			m = splitRatioMatrix.length;
 			n = splitRatioMatrix[0].length;
 		}
 		buf += "<srm>";
 		for (int j = 0; j < m; j++) {
 			if (j > 0)
 				buf += "; ";
 			for (int k = 0; k < n; k++) {
 				if (k > 0)
 					buf += ", ";
 				buf += splitRatioMatrix[j][k].toString();
 			}
 		}
 		buf += "</srm>\n";
 		return buf;
 	}
 	
 	/**
 	 * Returns split ratio profile as XML buffer.
 	 */
 	public String getSplitRatioProfileAsXML() {
 		String buf = "";
 		for (int i = 0; i < srmProfile.size(); i++) {
 			AuroraIntervalVector[][] srm = srmProfile.get(i);
 			int m = 0;
 			int n = 0;
 			if (srm != null) {
 				m = srm.length;
 				n = srm[0].length;
 			}
 			buf += "<srm>";
 			for (int j = 0; j < m; j++) {
 				if (j > 0)
 					buf += "; ";
 				for (int k = 0; k < n; k++) {
 					if (k > 0)
 						buf += ", ";
 					buf += srm[j][k].toString();
 				}
 			}
 			buf += "</srm>\n";
 		}
 		return buf;
 	}
 	
 	/**
 	 * Returns split ratio sampling period.
 	 */
 	public double getSplitRatioTP() {
 		return srTP;
 	}
 	
 	/**
 	 * Returns split ratio profile start time in hours.
 	 */
 	public double getSplitRatioStartTime() {
 		return srST;
 	}
 	
 	/**
 	 * Returns the split ration for given pair of input and output Links.
 	 * @param in input Link.
 	 * @param out output Link.
 	 * @return corresponding value from the split ratio matrix.
 	 */
 	public AuroraIntervalVector getSplitRatio(AbstractLink in, AbstractLink out) {
 		AuroraIntervalVector sr = null;
 		int i = getPredecessors().indexOf(in);
 		int j = getSuccessors().indexOf(out);
 		if ((i >= 0) && (j >= 0)) {
 			sr = new AuroraIntervalVector();
 			sr.copy(splitRatioMatrix[i][j]);
 		}
 		return sr;
 	}
 	
 	/**
 	 * Sets weaving factor matrix.
 	 * @param x weaving factor matrix.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public boolean setWeavingFactorMatrix(double[][] x) {
 		if (x == null)
 			return false;
 		int m = x.length;
 		int n = x[0].length;
 		if ((m != inputs.size()) || (n != outputs.size()))
 			return false;
 		for (int i = 0; i < m; i++)
 			for (int j = 0; j < n; j++)
 				weavingFactorMatrix[i][j] = x[i][j];
 		return true;
 	}
 	
 	/**
 	 * Sets split ratio matrix.
 	 * @param x split ratio matrix.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public boolean setSplitRatioMatrix(AuroraIntervalVector[][] x) {
 		if (x == null)
 			return false;
 		int m = x.length;
 		int n = x[0].length;
 		if ((m != inputs.size()) || (n != outputs.size()))
 			return false;
 		for (int i = 0; i < m; i++)
 			for (int j = 0; j < n; j++)
 				splitRatioMatrix[i][j].copy(x[i][j]);
 		return true;
 	}
 	
 	/**
 	 * Sets split ratio matrix template.
 	 * @param x split ratio matrix.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public boolean setSplitRatioMatrix0(AuroraIntervalVector[][] x) {
 		if (x == null)
 			if (!srmProfile.isEmpty()) {
 				splitRatioMatrix0 = null;
 				return true;
 			}
 			else
 				return false;
 		int m = x.length;
 		int n = x[0].length;
 		if ((m != inputs.size()) || (n != outputs.size()))
 			return false;
 		splitRatioMatrix0 = new AuroraIntervalVector[m][n];
 		for (int i = 0; i < m; i++)
 			for (int j = 0; j < n; j++) {
 				splitRatioMatrix0[i][j] = new AuroraIntervalVector();
 				splitRatioMatrix0[i][j].copy(x[i][j]);
 			}
 		return true;
 	}
 	
 	/**
 	 * Sets split ratio profile from text.
 	 * @param buf text buffer. Each line of this text describes a split ratio matrix,
 	 * and matrix rows are separated by ';' as in MATLAB.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setSplitRatioProfile(String buf) {
 		int m = getPredecessors().size();
 		int n = getSuccessors().size();
 		if ((buf == null) || (m <= 0) || (n <= 0))
 			return false;
 		StringTokenizer st1 = new StringTokenizer(buf, "\n");
 		if (st1.hasMoreTokens())
 			srmProfile.clear();
 		while (st1.hasMoreTokens()) {
 			AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 			String bufM = st1.nextToken();
 			StringTokenizer st2 = new StringTokenizer(bufM, ";");
 			int i = -1;
 			while ((st2.hasMoreTokens()) && (++i < m)) {
 				String bufR = st2.nextToken();
 				StringTokenizer st3 = new StringTokenizer(bufR, ", ");
 				int j = -1;
 				int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
 				while ((st3.hasMoreTokens()) && (++j < n)) {
 					String srvtxt = st3.nextToken();
 					AuroraIntervalVector srv = new AuroraIntervalVector();
 					srv.setRawIntervalVectorFromString(srvtxt);
 					int rsz = srv.size();
 					if (myNetwork.getContainer().isSimulation()) {
 						srv = new AuroraIntervalVector(sz);
 						srv.setIntervalVectorFromString(srvtxt);
 						for (int idx = rsz; idx < sz; idx++)
 							srv.get(idx).copy(srv.get(rsz-1));
 					}
 					srm[i][j] = srv;
 				}
 				while (++j < n) {
 					if (myNetwork.getContainer().isSimulation())
 						srm[i][j] = new AuroraIntervalVector(sz);
 					else
 						srm[i][j] = new AuroraIntervalVector();
 				}
 			}
 			srmProfile.add(srm);
 		}
 		return true;
 	}
 	
 	/**
 	 * Sets split ratio matrix change frequency.<br>
 	 * @param x split ratio sampling period.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setSplitRatioTP(double x) {
 		if (x < myNetwork.getTP())
 			return false;
 		srTP = x;
 		return true;
 	}
 	
 	/**
 	 * Sets split ratio matrix profile start time.<br>
 	 * @param x start time in hours.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean setSplitRatioStartTime(double x) {
 		if ((x < 0) || (x > 24))
 			return false;
 		srST = x;
 		return true;
 	}
 	
 	/**
 	 * Computes the sum of input flows.
 	 * @return total input flow object.
 	 */
 	public AuroraIntervalVector totalInput() {
 		AuroraIntervalVector sum = new AuroraIntervalVector();
 		sum.copy((AuroraIntervalVector)inputs.get(0));
 		for (int i = 1; i < inputs.size(); i++) {
 			AuroraIntervalVector o = (AuroraIntervalVector)inputs.get(i);
 			if (o != null)
 				sum.add(o);
 		}
 		return sum;
 	}
 	
 	/**
 	 * Computes the sum of output flows.
 	 * @return total input flow object.
 	 */
 	public AuroraIntervalVector totalOutput() {
 		AuroraIntervalVector sum = new AuroraIntervalVector();
 		sum.copy((AuroraIntervalVector)outputs.get(0));
 		for (int i = 1; i < outputs.size(); i++) {
 			AuroraIntervalVector o = (AuroraIntervalVector)outputs.get(i);
 			if (o != null)
 				sum.add(o);
 		}
 		return sum;
 	}
 	
 	/**
 	 * Adds input Link to the list.
 	 * @param x input Link.
 	 * @param c corresponding simple controller.
 	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
 	 */
 	public synchronized int addInLink(AbstractLink x, AbstractControllerSimple c) {
 		int idx = super.addInLink(x, c);
 		if (idx >= 0) {
 			int m = predecessors.size();
 			int n = successors.size();
 			if ((m < 1) || (n < 1)) {
 				splitRatioMatrix = null;
 				weavingFactorMatrix = null;
 				return idx;
 			}
 			int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
 			double[][] wfm = new double[m][n];
 			AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 			for (int i = 0; i < m; i++) {
 				for (int j = 0; j < n; j++) {
 					wfm[i][j] = 1;
 					srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
 				}
 			}
 			splitRatioMatrix = srm;
 			weavingFactorMatrix = wfm;
 		}
 		return idx;
 	}
 
 	/**
 	 * Adds input Link to the list.
 	 * @param x input Link.
 	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
 	 */
 	public synchronized int addInLink(AbstractLink x) {
 		return addInLink(x, null);
 	}
 	
 	/**
 	 * Adds output Link to the list.
 	 * @param x output Link.
 	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
 	 */
 	public synchronized int addOutLink(AbstractLink x) {
 		int idx = super.addOutLink(x);
 		if (idx >= 0) {
 			int m = predecessors.size();
 			int n = successors.size();
 			if ((m < 1) || (n < 1)) {
 				splitRatioMatrix = null;
 				weavingFactorMatrix = null;
 				return idx;
 			}
 			int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
 			double[][] wfm = new double[m][n];
 			AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 			for (int i = 0; i < m; i++) {
 				for (int j = 0; j < n; j++) {
 					wfm[i][j] = 1;
 					srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
 				}
 			}
 			splitRatioMatrix = srm;
 			weavingFactorMatrix = wfm;
 		}
 		return idx;
 	}
 	
 	/**
 	 * Deletes specified NE from the list of predecessors. 
 	 * @param x predecessor NE to be deleted.
 	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
 	 */
 	public synchronized int deletePredecessor(AbstractNetworkElement x) {
 		int idx = super.deletePredecessor(x);
 		int m = predecessors.size();
 		int n = successors.size();
 		if ((m < 1) || (n < 1)) {
 			splitRatioMatrix = null;
 			weavingFactorMatrix = null;
 			return idx;
 		}
 		int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
 		double[][] wfm = new double[m][n];
 		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 		for (int i = 0; i < m; i++) {
 			for (int j = 0; j < n; j++) {
 				wfm[i][j] = 1;
 				srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
 			}
 		}
 		splitRatioMatrix = srm;
 		weavingFactorMatrix = wfm;
 		return idx;
 	}
 	
 	/**
 	 * Deletes specified NE from the list of successors. 
 	 * @param x successor NE to be deleted.
 	 * @return idx index of deleted successor, <code>-1</code> - if such NE was not found.
 	 */
 	public synchronized int deleteSuccessor(AbstractNetworkElement x) {
 		int idx = super.deleteSuccessor(x);
 		int m = predecessors.size();
 		int n = successors.size();
 		if ((m < 1) || (n < 1)) {
 			splitRatioMatrix = null;
 			weavingFactorMatrix = null;
 			return idx;
 		}
 		int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
 		double[][] wfm = new double[m][n];
 		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
 		for (int i = 0; i < m; i++) {
 			for (int j = 0; j < n; j++) {
 				wfm[i][j] = 1;
 				srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
 			}
 		}
 		splitRatioMatrix = srm;
 		weavingFactorMatrix = wfm;
 		return idx;
 	}
 	
 	/**
 	 * Adjust vector data according to new vehicle weights.
 	 * @param w array of weights.
 	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean adjustWeightedData(double[] w) {
 		double[] ow = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights();
 		if ((w == null) || (ow == null))
 			return false;
 		boolean res = true;
 		for (int i = 0; i < inputs.size(); i++) {
 			if (inputs.get(i) != null) {
 				res &= ((AuroraIntervalVector)inputs.get(i)).inverseAffineTransform(ow, 0);
 				res &= ((AuroraIntervalVector)inputs.get(i)).affineTransform(w, 0);
 			}
 		}
 		for (int i = 0; i < outputs.size(); i++) {
 			if (outputs.get(i) != null) {
 				res &= ((AuroraIntervalVector)outputs.get(i)).inverseAffineTransform(ow, 0);
 				res &= ((AuroraIntervalVector)outputs.get(i)).affineTransform(w, 0);
 			}
 		}
 		return res;
 	}
 	
 	/**
 	 * Additional initialization.
 	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
 	 * @throws ExceptionConfiguration, ExceptionDatabase
 	 */
 	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
 		boolean res = super.initialize();
 		if (!srmProfile.isEmpty())
 			splitRatioMatrix0 = null;
 		boolean srmDefined = true;
 		if (splitRatioMatrix != null) {
 			int nIn = splitRatioMatrix.length;
 			int nOut = splitRatioMatrix[0].length;
 			for (int i = 0; i < nIn; i++)
 				for (int j = 0; j < nOut; j++)
 					if (splitRatioMatrix[i][j].minCenter() < 0)
 						srmDefined = false;
 		}
 		if (srmDefined)
 			splitRatioMatrix0 = null;
 		return res;
 	}
 	
 	/**
 	 * Copies data from given Node to a current one.
 	 * @param x given Network Element.
 	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
 	 */
 	public synchronized boolean copyData(AbstractNetworkElement x) {
 		boolean res = super.copyData(x);
 		if (res) {
 			AbstractNodeHWC nd = (AbstractNodeHWC)x;
 			weavingFactorMatrix = nd.getWeavingFactorMatrix();
 			splitRatioMatrix = nd.getSplitRatioMatrix();
 			splitRatioMatrix0 = nd.getSplitRatioMatrix0();
 		}
 		return res;
 	}
 	
 }
