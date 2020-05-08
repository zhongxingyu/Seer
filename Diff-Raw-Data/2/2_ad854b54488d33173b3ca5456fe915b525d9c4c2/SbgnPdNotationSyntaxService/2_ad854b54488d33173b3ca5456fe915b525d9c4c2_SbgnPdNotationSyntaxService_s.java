 package org.pathwayeditor.notations.sbgnpd.services;
 
 import java.math.BigDecimal;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LabelLocationPolicy;
 import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LineStyle;
 import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
 import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
 import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextPropertyDefinition;
 import org.pathwayeditor.businessobjects.notationsubsystem.INotation;
 import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
 import org.pathwayeditor.businessobjects.notationsubsystem.INotationSyntaxService;
 import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
 import org.pathwayeditor.businessobjects.typedefn.IObjectType;
 import org.pathwayeditor.businessobjects.typedefn.IRootObjectType;
 import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
 import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType.LinkEditableAttributes;
 import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefinition.LinkTermEditableAttributes;
 import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType.EditableShapeAttributes;
 import org.pathwayeditor.figure.geometry.Dimension;
 import org.pathwayeditor.notationsubsystem.toolkit.definition.IntegerPropertyDefinition;
 import org.pathwayeditor.notationsubsystem.toolkit.definition.LinkObjectType;
 import org.pathwayeditor.notationsubsystem.toolkit.definition.LinkTerminusDefinition;
 import org.pathwayeditor.notationsubsystem.toolkit.definition.NumberPropertyDefinition;
 import org.pathwayeditor.notationsubsystem.toolkit.definition.PlainTextPropertyDefinition;
 import org.pathwayeditor.notationsubsystem.toolkit.definition.RootObjectType;
 import org.pathwayeditor.notationsubsystem.toolkit.definition.ShapeObjectType;
 
 public class SbgnPdNotationSyntaxService implements INotationSyntaxService {
 	private static final int NUM_ROOT_OTS = 1;
 	private static final String UNIT_OF_INFO_DEFN = "curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "x y w h rect";
 	private static final String STATE_DEFN = "curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "h w le  {x y w h 1 h mul 1 h mul rrect}\n"
 		+ "{x y w h 1 w mul 1 w mul rrect} ifelse";
 	private static final String SIMPLE_CHEM_DEFN =
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
 		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
 		"/cardinalityBox { /card exch def /fsize exch def /cpy exch def /cpx exch def\n" +
 		"fsize setfontsize\n" +
 		"card cvs textbounds /hoff exch curlinewidth 2 mul add h div def /woff exch curlinewidth 2 mul add w div def \n" +
 		"cpx woff 2 div sub xoffset cpy hoff 2 div sub yoffset woff w mul hoff h mul rect\n" +
 		"gsave\n" +
 		"null setfillcol cpx xoffset cpy yoffset (C) card cvs text\n" +
 		"grestore\n" +
 		"} def\n" +
 		":cardinality 1 gt {\n" +
 		"0.10 xoffset 0.10 yoffset 0.90 w mul 0.90 w mul oval\n" +
 		"0 xoffset 0 yoffset 0.90 w mul 0.90 w mul oval\n" +
 		"0.25 0.05 :cardFontSize :cardinality cardinalityBox\n" +
 		"}\n" +
 		"{ x y w w oval } ifelse";
 	private static final String UNSPECIFIED_ENTITY_DEFN =
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "x y w h oval";
 	private static final String COMPARTMENT_DEFN =
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 		+ "gsave null setlinecol\n"
 		+ "x 0.10 yoffset w 0.81 h mul rect\n"
 		+ "0.4 xoffset y 0.22 w mul 0.10 h mul rect\n"
 		+ " 0.4 xoffset 0.90 yoffset 0.22 w mul 0.10 h mul rect\n"
 		+ "grestore\n"
 		+ "x 0.90 yoffset x 0.10 yoffset line 0.40 xoffset 0 yoffset 0.60 xoffset 0 yoffset line\n"
 		+ "1.00 xoffset 0.10 yoffset 1.00 xoffset 0.90 yoffset line\n"
 		+ "0.60 xoffset 1.00 yoffset 0.40 xoffset 1.00 yoffset line\n"
 		+ "0 xoffset 0 yoffset 0.80 w mul 0.20 h mul 90 90 arc\n"
 		+ "0.20 xoffset 0 yoffset 0.80 w mul 0.20 h mul 0 90 arc\n"
 		+ "0 xoffset 0.80 yoffset 0.80 w mul 0.20 h mul 180 90 arc\n"
 		+ "0.20 xoffset 0.80 yoffset 0.80 w mul 0.20 h mul 270 90 arc";
 	private static final String COMPLEX_DEFN = 
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n" + 
 		":cardinality 1 gt {\n" +
 		"/cardinalityBox { /card exch def /fsize exch def /cpy exch def /cpx exch def\n" +
 		"fsize setfontsize\n" +
 		"card cvs textbounds /hoff exch curlinewidth 2 mul add h div def /woff exch curlinewidth 2 mul add w div def \n" +
 		"cpx woff 2 div sub xoffset cpy hoff 2 div sub yoffset woff w mul hoff h mul rect\n" +
 		"gsave\n" +
 		"null setfillcol cpx xoffset cpy yoffset (C) card cvs text\n" +
 		"grestore\n" +
 		"} def\n" +
 		"/xoffset { w 0.9 mul mul x 0.1 w mul add add } def /yoffset { h 0.9 mul mul y 0.1 h mul add add } def\n" +
 		"[0 xoffset 0.15 yoffset 0.15 xoffset 0 yoffset 0.85 xoffset 0 yoffset 1.00 xoffset 0.15 yoffset 1.00 xoffset 0.85 yoffset 0.85 xoffset 1.00 yoffset 0.15 xoffset 1.00 yoffset 0 xoffset 0.85 yoffset] pgon\n" +
 		"/xoffset { w 0.9 mul mul x add } def /yoffset { h 0.9 mul mul y add } def\n" +
 		"[0 xoffset 0.15 yoffset 0.15 xoffset 0 yoffset 0.85 xoffset 0 yoffset 1.00 xoffset 0.15 yoffset 1.00 xoffset 0.85 yoffset 0.85 xoffset 1.00 yoffset 0.15 xoffset 1.00 yoffset 0 xoffset 0.85 yoffset] pgon\n" +
 		"0.3 0 :cardFontSize :cardinality cardinalityBox\n" +
 		"}\n" +
 		"{\n" +
 		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
 		"[0 xoffset 0.15 yoffset 0.15 xoffset 0 yoffset 0.85 xoffset 0 yoffset 1.00 xoffset 0.15 yoffset 1.00 xoffset 0.85 yoffset 0.85 xoffset 1.00 yoffset 0.15 xoffset 1.00 yoffset 0 xoffset 0.85 yoffset] pgon\n" +
 		"} ifelse";
 //
 //	"(C) setanchor\n" +
 //	"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
 //	"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
 //	":cardinality 1.0 gt {\n" +
 //	"0.10 xoffset 0.10 yoffset 0.90 w mul 0.90 h mul 0.20 w mul 0.20 h mul rrect\n" +
 //	"0 xoffset 0 yoffset 0.90 w mul 0.90 h mul 0.20 w mul 0.20 h mul rrect\n" +
 //	"{ x y w h 0.2 w mul 0.20 h mul rrect } ifelse";
 	private static final String NUCLEIC_ACID_FEATURE_DEFN =
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 			+ "gsave null setlinecol x y w 0.82 h mul rect\n"
 			+ "0.20 xoffset 0.80 yoffset 0.62 w mul 0.20 h mul rect\n"
 			+ "0 xoffset 0.60 yoffset 0.40 w mul 0.40 h mul 180 90 arc\n"
 			+ "0.60 xoffset 0.60 yoffset 0.40 w mul 0.40 h mul 270 90 arc\n" 
 			+ "grestore\n"
 			+ "[0 xoffset 0.80 yoffset 0 xoffset 0 yoffset 1.00 xoffset 0 yoffset 1.00 xoffset 0.80 yoffset ] pline\n"
 			+ "0.20 xoffset 1.00 yoffset 0.80 xoffset 1.00 yoffset line\n"
 			+ "0 xoffset 0.60 yoffset 0.40 w mul 0.40 h mul 180 90 arc\n"
 			+ "0.60 xoffset 0.60 yoffset 0.40 w mul 0.40 h mul 270 90 arc";
 	private static final String EMPTY_SET_DEFN = 
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 			+ "0.05 xoffset 0.05 yoffset 0.90 w mul 0.90 h mul oval 1.00 xoffset 0 yoffset 0 xoffset 1.00 yoffset line";
 	private static final String OBSERVABLE_DEFN =
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 			+ "[0.25 xoffset 0 yoffset 0.75 xoffset 0 yoffset 1.00 xoffset 0.50 yoffset 0.75 xoffset 1.00 yoffset 0.25 xoffset 1.00 yoffset 0 xoffset 0.50 yoffset] pgon";
 	private static final String PERTURBATION_DEFN =
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 			+ "[0 xoffset 0 yoffset 1.00 xoffset 0 yoffset 0.70 xoffset 0.50 yoffset 1.00 xoffset 1.00 yoffset 0 xoffset 1.00 yoffset 0.30 xoffset 0.50 yoffset] pgon";
 	private static final String PROCESS_DEFN =
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 		+ "0 xoffset y w h rect\n"
 		+ "0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n"
 		+ "1.20 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset line\n" +
 		"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset 0.5 xoffset 0.0 yoffset 0.5 xoffset 1.0 yoffset] (S) setanchor\n";
 	private static final String PROCESS_V_DEFN =
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 		+ "0 xoffset y w h rect\n"
 		+ "0.50 xoffset 0 yoffset 0.50 xoffset -0.20 yoffset line\n"
 		+ "0.50 xoffset 1.20 yoffset 0.50 xoffset 1.00 yoffset line\n" +
 		"[0.50 xoffset -0.20 yoffset 0.50 xoffset 1.20 yoffset 0 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset] (S) setanchor\n";
 	private static final String OMITTED_PROCESS_DEFN =
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 		+ "0 xoffset y w h rect\n"
 		+ "0.5 h mul setfontsize 0.5 xoffset 0.5 yoffset (C) (\\\\) text\n"
 		+ "0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n"
 		+ "1.20 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset line\n" +
 		"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset 0.5 xoffset 0.0 yoffset 0.5 xoffset 1.0 yoffset] (S) setanchor\n";
 	private static final String UNCERTAIN_PROCESS_DEFN =
 			"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 			+ "0 xoffset y w h rect\n"
 			+ "0.5 h mul setfontsize 0.5 xoffset 0.5 yoffset (C) (?) text\n"
 			+ "0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n"
 			+ "1.2 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset line\n" +
 			"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset 0.5 xoffset 0.0 yoffset 0.5 xoffset 1.0 yoffset] (S) setanchor\n";
 	private static final String ASSOC_DEFN =
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
 		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
 		"curlinecol setfillcol\n" +
 		"0 xoffset 0.0 yoffset 1.0 w mul 1.00 h mul oval\n" +
 		"0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n" +
 		"1.0 xoffset 0.50 yoffset 1.20 xoffset 0.50 yoffset line\n" +
 		"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset 0.5 xoffset 0.0 yoffset 0.5 xoffset 1.0 yoffset] (S) setanchor\n";
 	private static final String DISSOC_DEFN =
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 		+ "0 xoffset 0 yoffset 1.0 w mul 1.0 h mul oval\n"
 		+ "/indim 0.6 def\n"
 		+ "/inoffset 0.2 def\n"
 		+ "inoffset xoffset inoffset yoffset indim w mul indim h mul oval\n"
 		+ "0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n"
 		+ "1.20 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset line\n" +
 		"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset 0.5 xoffset 0.0 yoffset 0.5 xoffset 1.0 yoffset] (S) setanchor\n";
 	private static final String MACROMOLECULE_DEFN =
 		"(C) setanchor\n" +
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
 		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
 		"/cardinalityBox { /card exch def /fsize exch def /cpy exch def /cpx exch def\n" +
 		"fsize setfontsize\n" +
 		"card cvs textbounds /hoff exch curlinewidth 2 mul add h div def /woff exch curlinewidth 2 mul add w div def \n" +
 		"cpx woff 2 div sub xoffset cpy hoff 2 div sub yoffset woff w mul hoff h mul rect\n" +
 		"gsave\n" +
 		"null setfillcol cpx xoffset cpy yoffset (C) card cvs text\n" +
 		"grestore\n" +
 		"} def\n" +
 		":cardinality 1 gt {\n" +
 		"0.10 xoffset 0.10 yoffset 0.90 w mul 0.90 h mul 0.20 w mul 0.20 h mul rrect\n" +
 		"0 xoffset 0 yoffset 0.90 w mul 0.90 h mul 0.20 w mul 0.20 h mul rrect\n" +
 		"0.3 0 :cardFontSize :cardinality cardinalityBox\n" +
 		"}\n" +
 		"{ x y w h 0.2 w mul 0.20 h mul rrect } ifelse";
 	private static final String AND_SHAPE_DEFN =
 			"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 			+ "x y w h oval h 0.35 mul setfontsize null setfillcol 0.5 xoffset 0.5 yoffset (C) (AND) text\n" +
 			"0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n" +
 			"1.20 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset line\n" +
 			"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset ] (S) setanchor\n";
 	private static final String NOT_SHAPE_DEFN =
 		"curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 		+ "x y w h oval h 0.35 mul setfontsize null setfillcol 0.5 xoffset 0.5 yoffset (C) (NOT) text\n" +
 		"0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n" +
 		"1.20 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset line\n" +
 		"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset ] (S) setanchor\n";
 	private static final String OR_SHAPE_DEFN = "curbounds /h exch def /w exch def /y exch def /x exch def\n"
 			+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 			+ "x y w h oval h 0.35 mul setfontsize null setfillcol 0.5 xoffset 0.5 yoffset (C) (OR) text\n" +
 			"0 xoffset 0.50 yoffset -0.20 xoffset 0.50 yoffset line\n" +
 			"1.20 xoffset 0.50 yoffset 1.00 xoffset 0.50 yoffset line\n" +
 			"[-0.2 xoffset 0.5 yoffset 1.2 xoffset 0.5 yoffset ] (S) setanchor\n";
 	private static final String OR_SHAPE_V_DEFN = "curbounds /h exch def /w exch def /y exch def /x exch def\n"
 		+ "/xoffset { w mul x add } def /yoffset { h mul y add } def\n"
 		+ "x y w h oval h 0.35 mul setfontsize null setfillcol 0.5 xoffset 0.5 yoffset (C) (OR) text\n" 
 		+ "0.50 xoffset 0 yoffset 0.50 xoffset -0.20 yoffset line\n"
 		+ "0.50 xoffset 1.20 yoffset 0.50 xoffset 1.00 yoffset line\n" +
 		"[0.5 xoffset -0.2 yoffset 0.5 xoffset 1.2 yoffset ] (S) setanchor\n";
 	private final INotation context;
 	private final Map<Integer, IShapeObjectType> shapes = new HashMap<Integer, IShapeObjectType>();
 	private final Map<Integer, ILinkObjectType> links = new HashMap<Integer, ILinkObjectType>();
 
 	private RootObjectType rmo;
 	// shapes
 	private ShapeObjectType State;
 	private ShapeObjectType UnitOfInf;
 	private ShapeObjectType Compartment;
 	private ShapeObjectType Complex;
 	private ShapeObjectType GeneticUnit;
 	private ShapeObjectType Macromolecule;
 	private ShapeObjectType SimpleChem;
 	private ShapeObjectType UnspecEntity;
 	private ShapeObjectType Sink;
 	private ShapeObjectType Source;
 	private ShapeObjectType Perturbation;
 	private ShapeObjectType Observable;
 	private ShapeObjectType Process;
 	private ShapeObjectType ProcessV;
 	private ShapeObjectType OmittedProcess;
 	private ShapeObjectType UncertainProcess;
 	private ShapeObjectType Association;
 	private ShapeObjectType Dissociation;
 	private ShapeObjectType AndGate;
 	private ShapeObjectType OrGate;
 	private ShapeObjectType OrGateV;
 	private ShapeObjectType NotGate;
 
 	// links
 	private LinkObjectType Consumption;
 	private LinkObjectType Production;
 	private LinkObjectType Modulation;
 	private LinkObjectType Stimulation;
 	private LinkObjectType Catalysis;
 	private LinkObjectType Inhibition;
 	private LinkObjectType Trigger;
 	private LinkObjectType LogicArc;
 
 	private INotationSubsystem serviceProvider;
 
 	public SbgnPdNotationSyntaxService(INotationSubsystem serviceProvider) {
 		this.serviceProvider = serviceProvider;
 		this.context = serviceProvider.getNotation();
 		// "SBGN-PD"
 		// "SBGN process diagram language context"
 		// 1_0_0
 		createRMO();
 		// shapes
 		this.State = new ShapeObjectType(this, 10, "State");
 		createState();
 		this.UnitOfInf = new ShapeObjectType(this, 11, "Unit Of Information");
 		createUnitOfInf();
 		this.Compartment = new ShapeObjectType(this, 13, "Compartment");
 		createCompartment();
 		this.Complex = new ShapeObjectType(this, 14, "Complex");
 		createComplex();
 		this.GeneticUnit = new ShapeObjectType(this, 15, "Nucleic Acid Feature");
 		createGeneticUnit();
 		this.Macromolecule = new ShapeObjectType(this, 16, "Macromolecule");
 		createMacromolecule();
 		this.SimpleChem = new ShapeObjectType(this, 18, "Simple Chemical");
 		createSimpleChem();
 		this.UnspecEntity = new ShapeObjectType(this, 110, "Unspecified Entity");
 		createUnspecEntity();
 		this.Sink = new ShapeObjectType(this, 111, "Sink");
 		createSink();
 		this.Source = new ShapeObjectType(this, 112, "Source");
 		createSource();
 		this.Perturbation = new ShapeObjectType(this, 113, "Perturbing Agent");
 		createPerturbation();
 		this.Observable = new ShapeObjectType(this, 114, "Observable");
 		createObservable();
 		this.Process = new ShapeObjectType(this, 118, "Process");
 		createProcess();
 		this.ProcessV = new ShapeObjectType(this, 1018, "ProcessV");
 		createProcessV();
 		this.OmittedProcess = new ShapeObjectType(this, 119, "Omitted Process");
 		createOmittedProcess();
 		this.UncertainProcess = new ShapeObjectType(this, 120,
 				"Uncertain Process");
 		createUncertainProcess();
 		this.Association = new ShapeObjectType(this, 121, "Association");
 		createAssociation();
 		this.Dissociation = new ShapeObjectType(this, 122, "Dissociation");
 		createDissociation();
 		this.AndGate = new ShapeObjectType(this, 123, "AND");
 		createAndGate();
 		this.OrGate = new ShapeObjectType(this, 124, "OR");
 		createOrGate();
 		this.OrGateV = new ShapeObjectType(this, 1024, "ORv");
 		createOrGateV();
 		this.NotGate = new ShapeObjectType(this, 125, "NOT");
 		createNotGate();
 
 		defineParentingRMO();
 		// shapes parenting
 		defineParentingState();
 		defineParentingUnitOfInf();
 		defineParentingCompartment();
 		defineParentingComplex();
 		defineParentingGeneticUnit();
 		defineParentingMacromolecule();
 		defineParentingSimpleChem();
 		defineParentingUnspecEntity();
 		defineParentingSink();
 		defineParentingSource();
 		defineParentingPerturbation();
 		defineParentingObservable();
 		defineParentingProcess();
 		defineParentingOmittedProcess();
 		defineParentingUncertainProcess();
 		defineParentingAssociation();
 		defineParentingDissociation();
 		defineParentingAndGate();
 		defineParentingOrGate();
 		defineParentingNotGate();
 
 		// links
 		this.Consumption = new LinkObjectType(this, 20, "Consumption");
 		createConsumption();
 		this.Production = new LinkObjectType(this, 21, "Production");
 		createProduction();
 		this.Modulation = new LinkObjectType(this, 22, "Modulation");
 		createModulation();
 		this.Stimulation = new LinkObjectType(this, 23, "Stimulation");
 		createStimulation();
 		this.Catalysis = new LinkObjectType(this, 24, "Catalysis");
 		createCatalysis();
 		this.Inhibition = new LinkObjectType(this, 25, "Inhibition");
 		createInhibition();
 		this.Trigger = new LinkObjectType(this, 26, "Necessary Stimulation");
 		createTrigger();
 		this.LogicArc = new LinkObjectType(this, 27, "LogicArc");
 		createLogicArc();
 
 		// shape set
 		this.shapes.put(this.State.getUniqueId(), this.State);
 		this.shapes.put(this.UnitOfInf.getUniqueId(), this.UnitOfInf);
 		this.shapes.put(this.Compartment.getUniqueId(), this.Compartment);
 		this.shapes.put(this.Complex.getUniqueId(), this.Complex);
 		this.shapes.put(this.GeneticUnit.getUniqueId(), this.GeneticUnit);
 		this.shapes.put(this.Macromolecule.getUniqueId(), this.Macromolecule);
 		this.shapes.put(this.SimpleChem.getUniqueId(), this.SimpleChem);
 		this.shapes.put(this.UnspecEntity.getUniqueId(), this.UnspecEntity);
 		this.shapes.put(this.Sink.getUniqueId(), this.Sink);
 		this.shapes.put(this.Source.getUniqueId(), this.Source);
 		this.shapes.put(this.Perturbation.getUniqueId(), this.Perturbation);
 		this.shapes.put(this.Observable.getUniqueId(), this.Observable);
 		this.shapes.put(this.ProcessV.getUniqueId(), this.ProcessV);
 		this.shapes.put(this.Process.getUniqueId(), this.Process);
 		this.shapes.put(this.OmittedProcess.getUniqueId(), this.OmittedProcess);
 		this.shapes.put(this.UncertainProcess.getUniqueId(),
 				this.UncertainProcess);
 		this.shapes.put(this.Association.getUniqueId(), this.Association);
 		this.shapes.put(this.Dissociation.getUniqueId(), this.Dissociation);
 		this.shapes.put(this.AndGate.getUniqueId(), this.AndGate);
 		this.shapes.put(this.OrGate.getUniqueId(), this.OrGate);
 		this.shapes.put(this.OrGateV.getUniqueId(), this.OrGateV);
 		this.shapes.put(this.NotGate.getUniqueId(), this.NotGate);
 
 		// link set
 		this.links.put(this.Consumption.getUniqueId(), this.Consumption);
 		this.links.put(this.Production.getUniqueId(), this.Production);
 		this.links.put(this.Modulation.getUniqueId(), this.Modulation);
 		this.links.put(this.Stimulation.getUniqueId(), this.Stimulation);
 		this.links.put(this.Catalysis.getUniqueId(), this.Catalysis);
 		this.links.put(this.Inhibition.getUniqueId(), this.Inhibition);
 		this.links.put(this.Trigger.getUniqueId(), this.Trigger);
 		this.links.put(this.LogicArc.getUniqueId(), this.LogicArc);
 	}
 
 	public INotationSubsystem getNotationSubsystem() {
 		return serviceProvider;
 	}
 
 	public INotation getNotation() {
 		return this.context;
 	}
 
 	public Iterator<ILinkObjectType> linkTypeIterator() {
 		return this.links.values().iterator();
 	}
 
 	public IRootObjectType getRootObjectType() {
 		return this.rmo;
 	}
 
 	public Iterator<IShapeObjectType> shapeTypeIterator() {
 		return this.shapes.values().iterator();
 	}
 
 	public Iterator<IObjectType> objectTypeIterator() {
 		Set<IObjectType> retVal = new HashSet<IObjectType>(this.shapes.values());
 		retVal.addAll(this.links.values());
 		retVal.add(this.rmo);
 		return retVal.iterator();
 	}
 
 	public boolean containsLinkObjectType(int uniqueId) {
 		return this.links.containsKey(uniqueId);
 	}
 
 	public boolean containsObjectType(int uniqueId) {
 		boolean retVal = this.links.containsKey(uniqueId);
 		if (!retVal) {
 			retVal = this.shapes.containsKey(uniqueId);
 		}
 		if (!retVal) {
 			retVal = this.rmo.getUniqueId() == uniqueId;
 		}
 		return retVal;
 	}
 
 	public boolean containsShapeObjectType(int uniqueId) {
 		return this.shapes.containsKey(uniqueId);
 	}
 
 	public ILinkObjectType getLinkObjectType(int uniqueId) {
 		return this.links.get(uniqueId);
 	}
 
 	public IObjectType getObjectType(int uniqueId) {
 		IObjectType retVal = this.links.get(uniqueId);
 		if (retVal == null) {
 			retVal = this.shapes.get(uniqueId);
 		}
 		if (retVal == null) {
 			retVal = (this.rmo.getUniqueId() == uniqueId) ? this.rmo : null;
 		}
 		if (retVal == null) {
 			throw new IllegalArgumentException(
 					"The unique Id was not present in this notation subsystem");
 		}
 		return retVal;
 	}
 
 	public IShapeObjectType getShapeObjectType(int uniqueId) {
 		return this.shapes.get(uniqueId);
 	}
 
 	private <T extends IObjectType> T findObjectTypeByName(
 			Collection<? extends T> otSet, String name) {
 		T retVal = null;
 		for (T val : otSet) {
 			if (val.getName().equals(name)) {
 				retVal = val;
 				break;
 			}
 		}
 		return retVal;
 	}
 
 	public ILinkObjectType findLinkObjectTypeByName(String name) {
 		return findObjectTypeByName(this.links.values(), name);
 	}
 
 	public IShapeObjectType findShapeObjectTypeByName(String name) {
 		return findObjectTypeByName(this.shapes.values(), name);
 	}
 
 	public int numLinkObjectTypes() {
 		return this.links.size();
 	}
 
 	public int numShapeObjectTypes() {
 		return this.shapes.size();
 	}
 
 	public int numObjectTypes() {
 		return this.numLinkObjectTypes() + this.numShapeObjectTypes()
 				+ NUM_ROOT_OTS;
 	}
 
 	private void createRMO() {
 		this.rmo = new RootObjectType(0, "Root Object", "ROOT_OBJECT", this);
 	}
 
 	private void defineParentingRMO() {
 		HashSet<IShapeObjectType> set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.State,
 				this.UnitOfInf, this.Compartment, this.Complex,
 				this.GeneticUnit, this.Macromolecule, this.SimpleChem,
 				this.UnspecEntity, this.Sink, this.Source, this.Perturbation,
 				this.Observable,
 				this.Process,  this.ProcessV,this.OmittedProcess, this.UncertainProcess,
 				this.Association, this.Dissociation, this.AndGate, this.OrGate, this.OrGateV,
 				this.NotGate }));
 		set.removeAll(Arrays.asList(new IShapeObjectType[] { this.State,
 				this.UnitOfInf }));
 		for (IShapeObjectType child : set) {
 			this.rmo.getParentingRules().addChild(child);
 		}
 
 	}
 
 	private void createState() {
 		this.State.setDescription("State variable value");// ment to be
 															// TypeDescription
 															// rather
 		PlainTextPropertyDefinition stateDescnProp = new PlainTextPropertyDefinition(
 				"stateValue", "?");
 		stateDescnProp.setAlwaysDisplayed(true);
 		stateDescnProp.setEditable(true);
 		stateDescnProp.setDisplayName("Value");
 		stateDescnProp.getLabelDefaults().setNoBorder(true);
 		stateDescnProp.getLabelDefaults().setNoFill(true);
 		stateDescnProp.getLabelDefaults().setMinimumSize(new Dimension(30, 30));
 		this.State.getDefaultAttributes().addPropertyDefinition(stateDescnProp);
 		this.State.getDefaultAttributes().setShapeDefinition(STATE_DEFN);
 		this.State.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.State.getDefaultAttributes().setLineWidth(1);
 		this.State.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.State.getDefaultAttributes().setLineColour(RGB.BLACK);
 		this.State.getDefaultAttributes().setSize(new Dimension(40, 30));
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.State.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingState() {
 		this.State.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getState() {
 		return this.State;
 	}
 
 	private void createUnitOfInf() {
 		this.UnitOfInf.setDescription("Auxiliary information box");// ment to be
 																	// TypeDescription
 																	// rather
 		PlainTextPropertyDefinition infoDescnProp = new PlainTextPropertyDefinition("unitOfInfo", "Info");
 		infoDescnProp.setAlwaysDisplayed(true);
 		infoDescnProp.setEditable(true);
 		infoDescnProp.setDisplayName("Information");
 		this.UnitOfInf.getDefaultAttributes().addPropertyDefinition(infoDescnProp);
 		this.UnitOfInf.getDefaultAttributes().setShapeDefinition(UNIT_OF_INFO_DEFN);
 		this.UnitOfInf.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.UnitOfInf.getDefaultAttributes().setSize(new Dimension(45, 25));
 		this.UnitOfInf.getDefaultAttributes().setLineWidth(1);
 		this.UnitOfInf.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.UnitOfInf.getDefaultAttributes().setLineColour(RGB.BLACK);
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.UnitOfInf.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingUnitOfInf() {
 		this.UnitOfInf.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getUnitOfInf() {
 		return this.UnitOfInf;
 	}
 
 	private void createCompartment() {
 		this.Compartment.setDescription("Functional compartment");// ment to be
 																	// TypeDescription
 																	// rather
 		this.Compartment.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.Compartment.getDefaultAttributes().setShapeDefinition(COMPARTMENT_DEFN);
 		this.Compartment.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.Compartment.getDefaultAttributes().setSize(new Dimension(200, 200));
 		this.Compartment.getDefaultAttributes().setLineWidth(3);
 		this.Compartment.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Compartment.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Compartment.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingCompartment() {
 		HashSet<IShapeObjectType> set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.State,
 				this.UnitOfInf, this.Compartment, this.Complex,
 				this.GeneticUnit, this.Macromolecule, this.SimpleChem,
 				this.UnspecEntity, this.Sink, this.Source, this.Perturbation,
 				this.Observable,
 				this.Process, this.ProcessV, this.OmittedProcess, this.UncertainProcess,
 				this.Association, this.Dissociation, this.AndGate, this.OrGate, this.OrGateV,
 				this.NotGate }));
 		set.removeAll(Arrays.asList(new IShapeObjectType[] { this.State }));
 		for (IShapeObjectType child : set) {
 			this.Compartment.getParentingRules().addChild(child);
 		}
 
 	}
 
 	public ShapeObjectType getCompartment() {
 		return this.Compartment;
 	}
 
 	private NumberPropertyDefinition createCardFontSizeProperty(){
 		NumberPropertyDefinition cardFontSizeProp = new NumberPropertyDefinition("cardFontSize", new BigDecimal(12.0));
 		cardFontSizeProp.setVisualisable(false);
 		cardFontSizeProp.setEditable(true);
 		cardFontSizeProp.setDisplayName("Cardinality Font Size");
 		return cardFontSizeProp;
 	}
 	
 	private IntegerPropertyDefinition createCardinalityProperty(){
 		IntegerPropertyDefinition cardinalityProp = new IntegerPropertyDefinition("cardinality", 1);
 		cardinalityProp.setVisualisable(false);
 		cardinalityProp.setEditable(true);
 		cardinalityProp.setDisplayName("Cardinality");
 		cardinalityProp.getLabelDefaults().setLineWidth(1.0);
 		cardinalityProp.getLabelDefaults().setFillColour(RGB.WHITE);
 		cardinalityProp.getLabelDefaults().setLineColour(RGB.BLACK);
 		cardinalityProp.getLabelDefaults().setNoFill(false);
 		cardinalityProp.getLabelDefaults().setNoBorder(false);
 		cardinalityProp.getLabelDefaults().setLabelLocationPolicy(LabelLocationPolicy.COMPASS);
 		return cardinalityProp;
 	}
 	
 	
 	private IPlainTextPropertyDefinition createNameProperty(){
 		PlainTextPropertyDefinition nameProp = new PlainTextPropertyDefinition("name", "Name");
 		nameProp.setAlwaysDisplayed(true);
 		nameProp.setEditable(true);
 		nameProp.setDisplayName("Name");
 		nameProp.getLabelDefaults().setNoFill(true);
 		nameProp.getLabelDefaults().setNoBorder(true);
 		return nameProp;
 	}
 	
 	
 	private void createComplex() {
 		this.Complex.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.Complex.getDefaultAttributes().addPropertyDefinition(createCardinalityProperty());
 		this.Complex.getDefaultAttributes().addPropertyDefinition(createCardFontSizeProperty());
 		this.Complex.getDefaultAttributes().setShapeDefinition(COMPLEX_DEFN);
 		this.Complex.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.Complex.getDefaultAttributes().setSize(new Dimension(120, 80));
 		this.Complex.getDefaultAttributes().setLineWidth(1);
 		this.Complex.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Complex.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 	}
 
 	private void defineParentingComplex() {
 		HashSet<IShapeObjectType> set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.State,
 				this.UnitOfInf, this.Macromolecule, this.SimpleChem,
 				this.Complex,this.GeneticUnit }));
 		for (IShapeObjectType child : set) {
 			this.Complex.getParentingRules().addChild(child);
 		}
 
 	}
 
 	public ShapeObjectType getComplex() {
 		return this.Complex;
 	}
 
 	private void createGeneticUnit() {
 		this.GeneticUnit.setDescription("Nucleic Acid Feature");// ment
 																			// to
 																			// be
 																			// TypeDescription
 																			// rather
 		this.GeneticUnit.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.GeneticUnit.getDefaultAttributes().setShapeDefinition(NUCLEIC_ACID_FEATURE_DEFN);
 		this.GeneticUnit.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.GeneticUnit.getDefaultAttributes().setSize(new Dimension(60, 40));
 		this.GeneticUnit.getDefaultAttributes().setLineWidth(1);
 		this.GeneticUnit.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.GeneticUnit.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.GeneticUnit.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingGeneticUnit() {
 		HashSet<IShapeObjectType> set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.State,
 				this.UnitOfInf }));
 		for (IShapeObjectType child : set) {
 			this.GeneticUnit.getParentingRules().addChild(child);
 		}
 
 	}
 
 	public ShapeObjectType getGeneticUnit() {
 		return this.GeneticUnit;
 	}
 
 	private void createMacromolecule() {
 		this.Macromolecule.setDescription("Macromolecule");// ment to be
 															// TypeDescription
 															// rather
 		this.Macromolecule.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.Macromolecule.getDefaultAttributes().setShapeDefinition(MACROMOLECULE_DEFN);
 		this.Macromolecule.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.Macromolecule.getDefaultAttributes().setLineColour(RGB.BLACK);
 		this.Macromolecule.getDefaultAttributes().setLineWidth(1);
 		this.Macromolecule.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Macromolecule.getDefaultAttributes().setSize(new Dimension(60, 40));
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Macromolecule.setEditableAttributes(editableAttributes);
 		this.Macromolecule.getDefaultAttributes().addPropertyDefinition(createCardinalityProperty());
 		this.Macromolecule.getDefaultAttributes().addPropertyDefinition(createCardFontSizeProperty());
 	}
 
 	private void defineParentingMacromolecule() {
 		HashSet<IShapeObjectType> set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.State,
 				this.UnitOfInf }));
 		for (IShapeObjectType child : set) {
 			this.Macromolecule.getParentingRules().addChild(child);
 		}
 
 	}
 
 	public ShapeObjectType getMacromolecule() {
 		return this.Macromolecule;
 	}
 
 	private void createSimpleChem() {
 		this.SimpleChem.setDescription("Simple chemical");
 		this.SimpleChem.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.SimpleChem.getDefaultAttributes().addPropertyDefinition(createCardinalityProperty());
 		this.SimpleChem.getDefaultAttributes().addPropertyDefinition(createCardFontSizeProperty());
 		this.SimpleChem.getDefaultAttributes().setShapeDefinition(SIMPLE_CHEM_DEFN);
 		this.SimpleChem.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.SimpleChem.getDefaultAttributes().setSize(new Dimension(40, 40));
 		this.SimpleChem.getDefaultAttributes().setLineWidth(1.0);
 		this.SimpleChem.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.SimpleChem.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.SimpleChem.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingSimpleChem() {
 		HashSet<IShapeObjectType> set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] {}));
 		for (IShapeObjectType child : set) {
 			this.SimpleChem.getParentingRules().addChild(child);
 		}
 
 	}
 
 	public ShapeObjectType getSimpleChem() {
 		return this.SimpleChem;
 	}
 
 	private void createUnspecEntity() {
 		this.UnspecEntity.setDescription("Unspecified entity");// ment to be
 																// TypeDescription
 																// rather
 		this.UnspecEntity.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.UnspecEntity.getDefaultAttributes().setShapeDefinition(UNSPECIFIED_ENTITY_DEFN);
 		this.UnspecEntity.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.UnspecEntity.getDefaultAttributes().setSize(new Dimension(60, 40));
 		this.UnspecEntity.getDefaultAttributes().setLineWidth(1);
 		this.UnspecEntity.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.UnspecEntity.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.UnspecEntity.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingUnspecEntity() {
 		HashSet<IShapeObjectType> set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] {}));
 		for (IShapeObjectType child : set) {
 			this.UnspecEntity.getParentingRules().addChild(child);
 		}
 
 	}
 
 	public ShapeObjectType getUnspecEntity() {
 		return this.UnspecEntity;
 	}
 
 	private void createSink() {
 		this.Sink.setDescription("Sink");// ment to be TypeDescription rather
 		this.Sink.getDefaultAttributes().setShapeDefinition(EMPTY_SET_DEFN);
 		this.Sink.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.Sink.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.Sink.getDefaultAttributes().setLineWidth(1);
 		this.Sink.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Sink.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Sink.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingSink() {
 		this.Sink.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getSink() {
 		return this.Sink;
 	}
 
 	private void createSource() {
 		this.Source.setDescription("Source");// ment to be TypeDescription
 												// rather
 		this.Source.getDefaultAttributes().setShapeDefinition(EMPTY_SET_DEFN);
 		this.Source.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.Source.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.Source.getDefaultAttributes().setLineWidth(1);
 		this.Source.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Source.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_TYPE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Source.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingSource() {
 		this.Source.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getSource() {
 		return this.Source;
 	}
 
 	private void createPerturbation() {
 		this.Perturbation.setDescription("Perturbing Agent");// ment to be
 															// TypeDescription
 															// rather
 		this.Perturbation.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.Perturbation.getDefaultAttributes().setShapeDefinition(PERTURBATION_DEFN);
 		this.Perturbation.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.Perturbation.getDefaultAttributes().setSize(new Dimension(80, 60));
 		this.Perturbation.getDefaultAttributes().setLineWidth(1);
 		this.Perturbation.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Perturbation.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Perturbation.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingPerturbation() {
 		this.Perturbation.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getPerturbation() {
 		return this.Perturbation;
 	}
 
 	private void createObservable() {
 		this.Observable.setDescription("Observable");// ment to be
 														// TypeDescription
 														// rather
 		this.Observable.getDefaultAttributes().setShapeDefinition(OBSERVABLE_DEFN);
 		this.Observable.getDefaultAttributes().addPropertyDefinition(createNameProperty());
 		this.Observable.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.Observable.getDefaultAttributes().setLineColour(RGB.BLACK);
 		this.Observable.getDefaultAttributes().setSize(new Dimension(80, 60));
 		this.Observable.getDefaultAttributes().setLineWidth(1);
 		this.Observable.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Observable.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingObservable() {
 		this.Observable.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getObservable() {
 		return this.Observable;
 	}
 
 
 	private void createProcess() {
 		this.Process.setDescription("Process node");// ment to be
 													// TypeDescription rather
 		this.Process.getDefaultAttributes().setShapeDefinition(PROCESS_DEFN);
 		this.Process.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.Process.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.Process.getDefaultAttributes().setLineWidth(1);
 		this.Process.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Process.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Process.setEditableAttributes(editableAttributes);
 	}
 
 	private void createProcessV() {
 		this.ProcessV.setDescription("Process node");// ment to be
 													// TypeDescription rather
 		this.ProcessV.getDefaultAttributes().setShapeDefinition(PROCESS_V_DEFN);
 		this.ProcessV.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.ProcessV.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.ProcessV.getDefaultAttributes().setLineWidth(1);
 		this.ProcessV.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.ProcessV.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.ProcessV.setEditableAttributes(editableAttributes);
 	}
 
 
 	private void defineParentingProcess() {
 		this.Process.getParentingRules().clear();
 		this.ProcessV.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getProcess() {
 		return this.Process;
 	}
 
 	public ShapeObjectType getProcessV() {
 		return this.ProcessV;
 	}
 
 	private void createOmittedProcess() {
 		this.OmittedProcess.setDescription("omitted process");// ment to be
 																// TypeDescription
 																// rather
 		this.OmittedProcess.getDefaultAttributes().setShapeDefinition(OMITTED_PROCESS_DEFN);
 		this.OmittedProcess.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.OmittedProcess.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.OmittedProcess.getDefaultAttributes().setLineWidth(1);
 		this.OmittedProcess.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.OmittedProcess.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.OmittedProcess.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingOmittedProcess() {
 		this.OmittedProcess.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getOmittedProcess() {
 		return this.OmittedProcess;
 	}
 
 	private void createUncertainProcess() {
 		this.UncertainProcess.setDescription("Uncertain process");// ment to be
 																	// TypeDescription
 																	// rather
 		this.UncertainProcess.getDefaultAttributes().setShapeDefinition(UNCERTAIN_PROCESS_DEFN);
 		this.UncertainProcess.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.UncertainProcess.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.UncertainProcess.getDefaultAttributes().setLineWidth(1);
 		this.UncertainProcess.getDefaultAttributes().setLineStyle(
 				LineStyle.SOLID);
 		this.UncertainProcess.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.UncertainProcess.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingUncertainProcess() {
 		this.UncertainProcess.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getUncertainProcess() {
 		return this.UncertainProcess;
 	}
 
 	private void createAssociation() {
 		this.Association.setDescription("Association");// ment to be
 														// TypeDescription
 														// rather
 		this.Association.getDefaultAttributes().setShapeDefinition(ASSOC_DEFN);
 		this.Association.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.Association.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.Association.getDefaultAttributes().setLineWidth(1);
 		this.Association.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Association.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_TYPE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_STYLE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Association.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingAssociation() {
 		this.Association.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getAssociation() {
 		return this.Association;
 	}
 
 	private void createDissociation() {
 		this.Dissociation.setDescription("Dissociation");// ment to be
 															// TypeDescription
 															// rather
 		this.Dissociation.getDefaultAttributes().setShapeDefinition(DISSOC_DEFN);
 		this.Dissociation.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.Dissociation.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.Dissociation.getDefaultAttributes().setLineWidth(1);
 		this.Dissociation.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Dissociation.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.Dissociation.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingDissociation() {
 		this.Dissociation.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getDissociation() {
 		return this.Dissociation;
 	}
 
 	private void createAndGate() {
 		this.AndGate.setDescription("AndGate");// ment to be TypeDescription
 												// rather
 		this.AndGate.getDefaultAttributes().setShapeDefinition(AND_SHAPE_DEFN);
 		this.AndGate.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.AndGate.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.AndGate.getDefaultAttributes().setLineWidth(1);
 		this.AndGate.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.AndGate.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.AndGate.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingAndGate() {
 		this.AndGate.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getAndGate() {
 		return this.AndGate;
 	}
 
 	private void createOrGate() {
 		this.OrGate.setDescription("OR Logical Operator");// ment to be
 													// TypeDescription rather
 		this.OrGate.getDefaultAttributes().setShapeDefinition(OR_SHAPE_DEFN);
 		this.OrGate.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.OrGate.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.OrGate.getDefaultAttributes().setLineWidth(1);
 		this.OrGate.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.OrGate.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.OrGate.setEditableAttributes(editableAttributes);
 	}
 
 
 	private void createOrGateV() {
 		this.OrGateV.setDescription("OR Logical Operator");// ment to be
 													// TypeDescription rather
 		this.OrGateV.getDefaultAttributes().setShapeDefinition(OR_SHAPE_V_DEFN);
 		this.OrGateV.getDefaultAttributes().setFillColour(new RGB(255, 255, 255));
 		this.OrGateV.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.OrGateV.getDefaultAttributes().setLineWidth(1);
 		this.OrGateV.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.OrGateV.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.OrGateV.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingOrGate() {
 		this.OrGate.getParentingRules().clear();
 		this.OrGateV.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getOrGate() {
 		return this.OrGate;
 	}
 	public ShapeObjectType getOrGateV() {
 		return this.OrGateV;
 	}
 
 	private void createNotGate() {
 		this.NotGate.setDescription("NOT Logical Operator");// ment to be
 														// TypeDescription
 														// rather
 		this.NotGate.getDefaultAttributes().setShapeDefinition(NOT_SHAPE_DEFN);
 		this.NotGate.getDefaultAttributes().setFillColour(RGB.WHITE);
 		this.NotGate.getDefaultAttributes().setSize(new Dimension(30, 30));
 		this.NotGate.getDefaultAttributes().setLineWidth(1);
 		this.NotGate.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.NotGate.getDefaultAttributes().setLineColour(RGB.BLACK);
 
 		EnumSet<EditableShapeAttributes> editableAttributes = EnumSet
 				.noneOf(EditableShapeAttributes.class);
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.FILL_COLOUR);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.SHAPE_SIZE);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_WIDTH);
 		}
 		if (true) {
 			editableAttributes.add(EditableShapeAttributes.LINE_COLOUR);
 		}
 		this.NotGate.setEditableAttributes(editableAttributes);
 	}
 
 	private void defineParentingNotGate() {
 		this.NotGate.getParentingRules().clear();
 	}
 
 	public ShapeObjectType getNotGate() {
 		return this.NotGate;
 	}
 
 	private void createConsumption() {
 		Set<IShapeObjectType> set = null;
 		this.Consumption
 				.setDescription("Consumption is the arc used to represent the fact that an entity only affects a process, but is not affected by it");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.Consumption.getDefaultAttributes().setLineWidth(1);
 		this.Consumption.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Consumption.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.Consumption.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.Consumption.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.Consumption.getDefaultAttributes().setLineWidthEditable(true);
 		this.Consumption.setEditableAttributes(editableAttributes);
 
 		NumberPropertyDefinition Stoich = new NumberPropertyDefinition("STOICH", new BigDecimal(1));
 		Stoich.setEditable(true);
 		Stoich.setVisualisable(true);
 		Stoich.setDisplayName("Stoichiometry");
 		this.Consumption.getDefaultAttributes().addPropertyDefinition(Stoich);
 
 		// LinkEndDefinition sport=this.Consumption.getLinkSource();
 		// LinkEndDefinition tport=this.Consumption.getLinkTarget();
 		LinkTerminusDefinition sport = this.Consumption
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.Consumption
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap(5);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		// sport.getDefaultAttributes().setLineProperties(0, LineStyle.SOLID);
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
 		tport.getDefaultAttributes().setGap((short) 0);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		tport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process,this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Consumption.getLinkConnectionRules().addConnection(
 					this.Complex, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process,this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Consumption.getLinkConnectionRules().addConnection(
 					this.Macromolecule, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process,this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Consumption.getLinkConnectionRules().addConnection(
 					this.GeneticUnit, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process,this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Consumption.getLinkConnectionRules().addConnection(
 					this.SimpleChem, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process,this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Consumption.getLinkConnectionRules().addConnection(
 					this.UnspecEntity, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process,this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Consumption.getLinkConnectionRules().addConnection(
 					this.Source, tgt);
 		}
 
 	}
 
 	public LinkObjectType getConsumption() {
 		return this.Consumption;
 	}
 
 	private void createProduction() {
 		Set<IShapeObjectType> set = null;
 		this.Production
 				.setDescription("Production is the arc used to represent the fact that an entity is produced by a process.");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.Production.getDefaultAttributes().setLineWidth(1);
 		this.Production.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Production.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.Production.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.Production.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.Production.getDefaultAttributes().setLineWidthEditable(true);
 		this.Production.setEditableAttributes(editableAttributes);
 		NumberPropertyDefinition Stoich = new NumberPropertyDefinition("STOICH", new BigDecimal(1));
 		Stoich.setEditable(true);
 		Stoich.setVisualisable(true);
 		Stoich.setDisplayName("Stoichiometry");
 		this.Production.getDefaultAttributes().addPropertyDefinition(Stoich);
 
 		// LinkEndDefinition sport=this.Production.getLinkSource();
 		// LinkEndDefinition tport=this.Production.getLinkTarget();
 		LinkTerminusDefinition sport = this.Production
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.Production
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap((short) 0);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
 		tport.getDefaultAttributes().setGap((short) 5);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.TRIANGLE);// , 5,5);
 		tport.getDefaultAttributes().setEndSize(new Dimension(5, 5));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Complex,
 				this.Macromolecule, this.GeneticUnit,this.SimpleChem, this.UnspecEntity,
 				this.Sink }));
 		for (IShapeObjectType tgt : set) {
 			this.Production.getLinkConnectionRules().addConnection(
 					this.Process, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Production.getLinkConnectionRules().addConnection(
 					this.ProcessV, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Complex,
 				this.Macromolecule, this.GeneticUnit,this.SimpleChem, this.UnspecEntity,
 				this.Sink }));
 		for (IShapeObjectType tgt : set) {
 			this.Production.getLinkConnectionRules().addConnection(
 					this.OmittedProcess, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Complex,
 				this.Macromolecule, this.GeneticUnit,this.SimpleChem, this.UnspecEntity,
 				this.Sink }));
 		for (IShapeObjectType tgt : set) {
 			this.Production.getLinkConnectionRules().addConnection(
 					this.UncertainProcess, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Complex,
 				this.Macromolecule, this.GeneticUnit,this.SimpleChem, this.UnspecEntity,
 				this.Sink }));
 		for (IShapeObjectType tgt : set) {
 			this.Production.getLinkConnectionRules().addConnection(
 					this.Association, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Complex,
 				this.Macromolecule, this.GeneticUnit,this.SimpleChem, this.UnspecEntity,
 				this.Sink }));
 		for (IShapeObjectType tgt : set) {
 			this.Production.getLinkConnectionRules().addConnection(
 					this.Dissociation, tgt);
 		}
 
 	}
 
 	public LinkObjectType getProduction() {
 		return this.Production;
 	}
 
 	private void createModulation() {
 		Set<IShapeObjectType> set = null;
 		this.Modulation
 				.setDescription("A modulation affects the flux of a process represented by the target transition.");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.Modulation.getDefaultAttributes().setLineWidth(1);
 		this.Modulation.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Modulation.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.Modulation.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.Modulation.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.Modulation.getDefaultAttributes().setLineWidthEditable(true);
 		this.Modulation.setEditableAttributes(editableAttributes);
 
 		// LinkEndDefinition sport=this.Modulation.getLinkSource();
 		// LinkEndDefinition tport=this.Modulation.getLinkTarget();
 		LinkTerminusDefinition sport = this.Modulation
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.Modulation
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap((short) 0);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
 		tport.getDefaultAttributes().setGap((short) 5);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.EMPTY_DIAMOND);// , 5,5);
 		tport.getDefaultAttributes().setEndSize(new Dimension(5, 5));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.Complex, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.Macromolecule, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.SimpleChem, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.Perturbation, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.GeneticUnit, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.UnspecEntity, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.AndGate, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(this.OrGate,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(this.OrGateV,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Modulation.getLinkConnectionRules().addConnection(
 					this.NotGate, tgt);
 		}
 
 	}
 
 	public LinkObjectType getModulation() {
 		return this.Modulation;
 	}
 
 	private void createStimulation() {
 		Set<IShapeObjectType> set = null;
 		this.Stimulation
 				.setDescription("A stimulation affects positively the flux of a process represented by the target transition.");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.Stimulation.getDefaultAttributes().setLineWidth(1);
 		this.Stimulation.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Stimulation.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.Stimulation.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.Stimulation.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.Stimulation.getDefaultAttributes().setLineWidthEditable(true);
 		this.Stimulation.setEditableAttributes(editableAttributes);
 
 		// LinkEndDefinition sport=this.Stimulation.getLinkSource();
 		// LinkEndDefinition tport=this.Stimulation.getLinkTarget();
 		LinkTerminusDefinition sport = this.Stimulation
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.Stimulation
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap((short) 0);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
 		tport.getDefaultAttributes().setGap((short) 5);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.EMPTY_TRIANGLE);// , 5,5);
 		tport.getDefaultAttributes().setEndSize(new Dimension(5, 5));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.Complex, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.Macromolecule, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.SimpleChem, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.Perturbation, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.UnspecEntity, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.AndGate, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.GeneticUnit, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.OrGate, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.OrGateV, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Stimulation.getLinkConnectionRules().addConnection(
 					this.NotGate, tgt);
 		}
 
 	}
 
 	public LinkObjectType getStimulation() {
 		return this.Stimulation;
 	}
 
 	private void createCatalysis() {
 		Set<IShapeObjectType> set = null;
 		this.Catalysis
 				.setDescription("A catalysis is a particular case of stimulation.");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.Catalysis.getDefaultAttributes().setLineWidth(1);
 		this.Catalysis.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Catalysis.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.Catalysis.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.Catalysis.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.Catalysis.getDefaultAttributes().setLineWidthEditable(true);
 		this.Catalysis.setEditableAttributes(editableAttributes);
 		LinkTerminusDefinition sport = this.Catalysis
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.Catalysis
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap((short) 0);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
 		tport.getDefaultAttributes().setGap((short) 10);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.EMPTY_CIRCLE);// , 5,5);
 		tport.getDefaultAttributes().setEndSize(new Dimension(5, 5));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(this.Complex,
 					tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(
 					this.Macromolecule, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(
 					this.GeneticUnit, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(
 					this.SimpleChem, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(
 					this.UnspecEntity, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(this.AndGate,
 					tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(this.OrGate,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(this.OrGateV,
 					tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation }));
 		for (IShapeObjectType tgt : set) {
 			this.Catalysis.getLinkConnectionRules().addConnection(this.NotGate,
 					tgt);
 		}
 
 	}
 
 	public LinkObjectType getCatalysis() {
 		return this.Catalysis;
 	}
 
 	private void createInhibition() {
 		Set<IShapeObjectType> set = null;
 		this.Inhibition
 				.setDescription("An inhibition affects negatively the flux of a process represented by the target transition.");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.Inhibition.getDefaultAttributes().setLineWidth(1);
 		this.Inhibition.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Inhibition.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.Inhibition.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.Inhibition.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.Inhibition.getDefaultAttributes().setLineWidthEditable(true);
 		this.Inhibition.setEditableAttributes(editableAttributes);
 
 		LinkTerminusDefinition sport = this.Inhibition
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.Inhibition
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap((short) 0);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
		tport.getDefaultAttributes().setGap((short) 5);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.BAR);// , 5,5);
 		tport.getDefaultAttributes().setEndSize(new Dimension(5, 5));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.Complex, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.Perturbation, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.Macromolecule, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.SimpleChem, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.GeneticUnit, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.UnspecEntity, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.AndGate, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(this.OrGate,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(this.OrGateV,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Inhibition.getLinkConnectionRules().addConnection(
 					this.NotGate, tgt);
 		}
 
 	}
 
 	public LinkObjectType getInhibition() {
 		return this.Inhibition;
 	}
 
 	private void createTrigger() {
 		Set<IShapeObjectType> set = null;
 		this.Trigger
 				.setDescription("A trigger effect, or absolute stimulation, is a stimulation that is necessary for a process to take place.");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.Trigger.getDefaultAttributes().setLineWidth(1);
 		this.Trigger.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.Trigger.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.Trigger.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.Trigger.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.Trigger.getDefaultAttributes().setLineWidthEditable(true);
 		this.Trigger.setEditableAttributes(editableAttributes);
 
 		// LinkEndDefinition sport=this.Trigger.getLinkSource();
 		// LinkEndDefinition tport=this.Trigger.getLinkTarget();
 		LinkTerminusDefinition sport = this.Trigger
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.Trigger
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap((short) 0);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
 		tport.getDefaultAttributes().setGap((short) 5);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.TRIANGLE_BAR);// , 5,5);
 		tport.getDefaultAttributes().setEndSize(new Dimension(5, 5));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.Process, this.ProcessV,
 				this.OmittedProcess, this.UncertainProcess, this.Association,
 				this.Dissociation, this.Observable }));
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(this.Complex,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(
 					this.Perturbation, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(
 					this.Macromolecule, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(
 					this.SimpleChem, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(
 					this.GeneticUnit, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(
 					this.UnspecEntity, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(this.AndGate,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(this.OrGate,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(this.OrGateV,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.Trigger.getLinkConnectionRules().addConnection(this.NotGate,
 					tgt);
 		}
 
 	}
 
 	public LinkObjectType getTrigger() {
 		return this.Trigger;
 	}
 
 	private void createLogicArc() {
 		Set<IShapeObjectType> set = null;
 		this.LogicArc
 				.setDescription("Logic arc is the arc used to represent the fact that an entity influences outcome of logic operator.");
 		int[] lc = new int[] { 0, 0, 0 };
 		this.LogicArc.getDefaultAttributes().setLineWidth(1);
 		this.LogicArc.getDefaultAttributes().setLineStyle(LineStyle.SOLID);
 		this.LogicArc.getDefaultAttributes().setLineColour(
 				new RGB(lc[0], lc[1], lc[2]));
 		EnumSet<LinkEditableAttributes> editableAttributes = EnumSet
 				.noneOf(LinkEditableAttributes.class);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.COLOUR);
 		}
 		// this.LogicArc.getDefaultAttributes().setLineColourEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_STYLE);
 		}
 		// this.LogicArc.getDefaultAttributes().setLineStyleEditable(true);
 		if (true) {
 			editableAttributes.add(LinkEditableAttributes.LINE_WIDTH);
 		}
 		// this.LogicArc.getDefaultAttributes().setLineWidthEditable(true);
 		this.LogicArc.setEditableAttributes(editableAttributes);
 
 		// LinkEndDefinition sport=this.LogicArc.getLinkSource();
 		// LinkEndDefinition tport=this.LogicArc.getLinkTarget();
 		LinkTerminusDefinition sport = this.LogicArc
 				.getSourceTerminusDefinition();
 		LinkTerminusDefinition tport = this.LogicArc
 				.getTargetTerminusDefinition();
 		sport.getDefaultAttributes().setGap((short) 2);
 		sport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 8,8);
 		sport.getDefaultAttributes().setEndSize(new Dimension(8, 8));
 		EnumSet<LinkTermEditableAttributes> editablesportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editablesportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// sport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editablesportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// sport.getDefaultAttributes().setColourEditable(true);
 		sport.setEditableAttributes(editablesportAttributes);
 		tport.getDefaultAttributes().setGap((short) 0);
 		tport.getDefaultAttributes().setEndDecoratorType(
 				LinkEndDecoratorShape.NONE);// , 5,5);
 		tport.getDefaultAttributes().setEndSize(new Dimension(5, 5));
 		EnumSet<LinkTermEditableAttributes> editabletportAttributes = EnumSet
 				.of(LinkTermEditableAttributes.END_SIZE,
 						LinkTermEditableAttributes.OFFSET,
 						LinkTermEditableAttributes.TERM_DECORATOR_TYPE,
 						LinkTermEditableAttributes.TERM_SIZE);
 		if (true) {
 			editabletportAttributes
 					.add(LinkTermEditableAttributes.END_DECORATOR_TYPE);
 		}
 		// tport.getDefaultAttributes().setShapeTypeEditable(true);
 		if (true) {
 			editabletportAttributes.add(LinkTermEditableAttributes.TERM_COLOUR);
 		}
 		// tport.getDefaultAttributes().setColourEditable(true);
 		tport.setEditableAttributes(editabletportAttributes);
 
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.AndGate,
 				this.OrGate, this.OrGateV, this.NotGate }));
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(this.Complex,
 					tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.AndGate,
 				this.OrGate, this.OrGateV, this.NotGate }));
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(
 					this.Macromolecule, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.AndGate,
 				this.OrGate, this.OrGateV, this.NotGate }));
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(
 					this.SimpleChem, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.AndGate,
 				this.OrGate, this.OrGateV, this.NotGate }));
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(
 					this.UnspecEntity, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(
 					this.Perturbation, tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(
 					this.GeneticUnit, tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.AndGate,
 				this.OrGate, this.OrGateV, this.NotGate }));
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(this.AndGate,
 					tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.AndGate,
 				this.OrGate, this.OrGateV, this.NotGate }));
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(this.OrGate,
 					tgt);
 		}
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(this.OrGateV,
 					tgt);
 		}
 		set = new HashSet<IShapeObjectType>();
 		set.addAll(Arrays.asList(new IShapeObjectType[] { this.AndGate,
 				this.OrGate, this.OrGateV, this.NotGate }));
 		for (IShapeObjectType tgt : set) {
 			this.LogicArc.getLinkConnectionRules().addConnection(this.NotGate,
 					tgt);
 		}
 
 	}
 
 	public LinkObjectType getLogicArc() {
 		return this.LogicArc;
 	}
 
 }
