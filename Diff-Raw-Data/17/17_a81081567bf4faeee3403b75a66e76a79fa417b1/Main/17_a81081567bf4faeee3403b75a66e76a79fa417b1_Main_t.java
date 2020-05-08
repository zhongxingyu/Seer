 package com.miravtech.filtering;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 
 import com.miravtech.SBGNUtils.SBGNIterator;
 import com.miravtech.SBGNUtils.SBGNUtils;
 import com.miravtech.sbgn.AndNodeType;
 import com.miravtech.sbgn.ArcType;
 import com.miravtech.sbgn.ComplexType;
 import com.miravtech.sbgn.ConsumptionArcType;
 import com.miravtech.sbgn.DissociationType;
 import com.miravtech.sbgn.EntityPoolNodeType;
 import com.miravtech.sbgn.FindingType;
 import com.miravtech.sbgn.LogicalOperatorNodeType;
 import com.miravtech.sbgn.OrNodeType;
 import com.miravtech.sbgn.ProductionArcType;
 import com.miravtech.sbgn.SBGNGlyphType;
 import com.miravtech.sbgn.SBGNNodeType;
 import com.miravtech.sbgn.SBGNPDl1;
 import com.miravtech.sbgn.SelectType;
 import com.miravtech.sbgn.filter.evaluator.FilteringLexer;
 import com.miravtech.sbgn.filter.evaluator.FilteringParser;
 import com.miravtech.sbgn_graphics.GraphicType;
 
 public class Main {
 
 	static JAXBContext jaxbContext;
 	static Unmarshaller unmarshaller;
 	static Marshaller marshaller;
 
 	/**
 	 * @param args
 	 * @throws JAXBException
 	 */
 	public static void main(String[] args) throws Exception {
 
 		String source;
 		String destination;
 		String filterExpression = "organism='Mus musculus'";
 		if (args.length >= 3) {
 			source = args[0];
 			destination = args[1];
 			filterExpression = args[2];
 		} else {
 			throw new Exception("Please provide source file, destination and the expression");
 		}
 		File srcDir = new File(source);
 		File destDir = new File(destination);
 
 		if (srcDir.getAbsolutePath().compareToIgnoreCase(
 				destDir.getAbsolutePath()) == 0)
 			throw new RuntimeException(
 					"Source and destination directories or files cannot be identical!");
 
 		jaxbContext = JAXBContext
 				.newInstance("com.miravtech.sbgn:com.miravtech.sbgn_graphics");
 		unmarshaller = jaxbContext.createUnmarshaller();
 		marshaller = jaxbContext.createMarshaller();
 
 		filterSBGN(srcDir, destDir, filterExpression);
 
 	}
 
 	public static void filterSBGN(File sourceSBGN, File destSBGN,
 			final String expression) throws JAXBException {
 
 
 		SBGNPDl1 sbgnpath = filterSBGN(sourceSBGN,expression);
		if (sbgnpath != null)
			marshaller.marshal(sbgnpath, destSBGN);
 	}
 	
 		public static SBGNPDl1 filterSBGN(final File sourceSBGN,
 				final String expression) throws JAXBException {
 
 		SBGNPDl1 sbgnpath = (SBGNPDl1) unmarshaller.unmarshal(sourceSBGN);
 
 		SBGNUtils utils = new SBGNUtils(sbgnpath.getValue());
 		
 		utils.setEmptyIDs();
 		utils.expandClones();
 
 		// filter the IDs
 		final Set<String> selected = new HashSet<String>();
 		new SBGNIterator() {
 			@Override
 			public void iterateGlyph(SBGNGlyphType n) {
 
 				Collection<FindingType> c = new HashSet<FindingType>();
 				c.addAll(n.getFinding());
 				
 				if (n.getFinding().size() == 0) {
 					c.add(new FindingType()); // this should match any finding fact
 				}
 
 				for (FindingType f : c) {
 					// build the expression evaluator
 					try {
 						ByteArrayInputStream bais = new ByteArrayInputStream(
 								expression.getBytes());
 						ANTLRInputStream input = new ANTLRInputStream(bais);
 						FilteringLexer lexer = new FilteringLexer(input);
 						CommonTokenStream tokens = new CommonTokenStream(lexer);
 						FilteringParser parser = new FilteringParser(tokens);
 						parser.toEval = f;
 						parser.glyphToEval = n;
 						boolean ret = parser.expr();
 						if (ret) {
 							selected.add(n.getID()); // positive finding
 							break;
 						}
 					} catch (Exception e) {
 						throw new RuntimeException(
 								"Error parsing the condition: "
 										+ e.getMessage());
 					}
 				}
 			}
 		}.run(sbgnpath.getValue());
		
		if (selected.size() == 0) {
			// nothing matched
			System.err.println("No output file will be written, since no entry could be assigned to the pathway");
			return null;
		}
 
 		// reread the file, set the values and remove the introduced IDs.
 		sbgnpath = (SBGNPDl1) unmarshaller.unmarshal(sourceSBGN);
 		final SBGNUtils utils2 = new SBGNUtils(sbgnpath.getValue());
 		utils2.setEmptyIDs();
 
 		// set the values
 		new SBGNIterator() {
 			@Override
 			public void iterateGlyph(SBGNGlyphType n) {
 				String id = n.getID();
 				if (selected.contains(id))
 					n.setSelected(SelectType.INCLUDE);
 				else
 					n.setSelected(SelectType.EXCLUDE);
 			}
 		}.run(sbgnpath.getValue());
 		utils2.removeAssignedIDs();
 
 		// set the affected by propagating to the neighborhood
 		new SBGNIterator() {
 			@Override
 			public void iterateArc(ArcType n) {
 				if (n.getSelected().equals(SelectType.EXCLUDE)) {
 					// Affect both sides of the arcs
 					setAffectedArc(n, utils2);
 				}
 			}
 
 			public void iterateNode(SBGNNodeType n) {
 				if (n.getSelected().equals(SelectType.EXCLUDE)) {
 					// Affect all contained arcs
 					setAffected(n);
 					// affect both ends of the arcs
 					Map<SBGNNodeType, Collection<ArcType>> edges = utils2
 							.getEdges();
 					for (ArcType a : edges.get(n))
 						setAffectedArc(a, utils2);
 				}
 
 			};
 
 		}.run(sbgnpath.getValue());
 
 		// marks complexes that contain disabled EPN
 		new SBGNIterator() {
 			@Override
 			public void iterateNode(SBGNNodeType n) {
 				if (n instanceof ComplexType
 						&& n.getSelected() != SelectType.EXCLUDE) {
 					ComplexType c = (ComplexType) n;
 					boolean hasSel = false;
 					boolean hasUnSel = false;
 					for (SBGNNodeType inner : c.getInnerNodes()) {
 						if (inner instanceof EntityPoolNodeType) {
 							if (inner.getSelected() == SelectType.EXCLUDE)
 								hasUnSel = true;
 							else
 								hasSel = true;
 						}
 					}
 					//composed of both selected and unselected
 					if (hasSel && hasUnSel)
 						c.setSelected(SelectType.AFFECTED); 
 
 					// if composed of excluded only, it becomes excluded					
 					if (!hasSel && hasUnSel)
 						c.setSelected(SelectType.EXCLUDE); 
 				}
 
 			};
 		}.runBottomUp(sbgnpath.getValue());
 
 		// mark the AND and OR nodes
 		new SBGNIterator() {
 			@Override
 			public void iterateNode(SBGNNodeType n) {
 				boolean isAnd = (n instanceof AndNodeType);
 				boolean isOr = (n instanceof OrNodeType);
 				if (!isAnd && !isOr)
 					return; // not our business
 				Collection<SBGNNodeType> inNodes = utils2.getInNodesOfLogic((LogicalOperatorNodeType)n);
 				boolean hasInclude = false;
 				boolean hasNonInclude = false;
 				for ( SBGNNodeType node: inNodes ) {
 					if (node.getSelected() == SelectType.INCLUDE)
 						hasInclude = true;
 					else
 						hasNonInclude = true;
 				}
 				if ( (isOr && ! hasInclude) || (isAnd && hasNonInclude)) {
 					setAffected(utils2.getOutNodeOfLogic((LogicalOperatorNodeType)n));
 					setAffected(n);
 					for (ArcType a : n.getArcs()) {
 						if (a.getSelected() != SelectType.INCLUDE)
 							setAffected(a);
 					}
 				}
 			}
 		}.run(sbgnpath.getValue());
 
 		// mark the affected though dissociation
 		new SBGNIterator() {
 			@Override
 			public void iterateNode(SBGNNodeType n) {
 				if (n instanceof DissociationType) {
 					DissociationType d = (DissociationType) n;
 					// get the consumption arc
 					ConsumptionArcType c = null;
 					for (ArcType a : d.getArcs()) {
 						if (a instanceof ConsumptionArcType) {
 							c = (ConsumptionArcType) a;
 							if (c.getSelected() != SelectType.INCLUDE) 
 								setAffected(a);
 						}
 					}
 					if (c.getSelected() != SelectType.INCLUDE) {
 						// set affected all the products, production arcs and the appropriate consumption arcs
 						for (ArcType a : d.getArcs()) {
 							if (a instanceof ProductionArcType) {
 								SBGNNodeType prod = utils2.getOtherNode(a, n);
 								setAffected(d);
 								setAffected(a);
 								setAffected(prod);
 							}
 						}
 					}
 				}
 			}
 		}.run(sbgnpath.getValue());
 
 		
 		// color the objects
 		new SBGNIterator() {
 			@Override
 			public void iterateGlyph(SBGNGlyphType n) {
 
 				if (n.getGraphic() == null)
 					n.setGraphic(new GraphicType());
 
 				if (n.getGraphic().getBorderColor() == null)
 					n.getGraphic().setBorderColor("Black");
 
 				if (n.getGraphic().getColor() == null)
 					n.getGraphic().setColor("Black");
 
 				if (n.getSelected().equals(SelectType.EXCLUDE)) {
 					n.getGraphic().setBorderColor("Gray");
 					n.getGraphic().setColor("Gray");
 				}
 				if (n.getSelected().equals(SelectType.AFFECTED)) {
 					n.getGraphic().setBorderColor("Blue");
 					n.getGraphic().setColor("Blue");
 				}
 			}
 		}.run(sbgnpath.getValue());
 
 		return sbgnpath;
 
 	}
 
 	private static void setAffectedArc(ArcType n, SBGNUtils utils) {
 		setAffected(n);
 		Map<SBGNNodeType, Collection<ArcType>> edges = utils.getEdges();
 		for (Map.Entry<SBGNNodeType, Collection<ArcType>> e : edges.entrySet())
 			if (e.getValue().contains(n))
 				setAffected(e.getKey());
 	}
 
 	private static void setAffected(SBGNGlyphType n) {
 		if (n.getSelected().equals(SelectType.INCLUDE))
 			n.setSelected(SelectType.AFFECTED);
 	}
 
 }
 
 class XMLFiles implements FilenameFilter {
 	public final static String XML = "xml";
 	private static Pattern file = Pattern.compile("(.*)\\." + XML);
 
 	@Override
 	public boolean accept(File dir, String name) {
 		return file.matcher(name.toLowerCase()).matches();
 	}
 
 	public static String getName(String name) {
 		Matcher m = file.matcher(name.toLowerCase());
 		if (!m.matches())
 			throw new RuntimeException("The name does not match the pattern");
 		return m.group(0);
 	}
 }
