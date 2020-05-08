 package utils.ConservationImage;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import utils.ConservationImage.color.ColoringStrategy;
 import utils.ConservationImage.color.RedBlueColorringStrategy;
 import utils.ConservationImage.renderer.DrawingLayout;
 import utils.ConservationImage.renderer.Renderer;
 import utils.ConservationImage.renderer.XYPlotRenderer;
 
 import cmdGA.NoOption;
 import cmdGA.Parser;
 import cmdGA.SingleOption;
 import cmdGA.exceptions.IncorrectParameterTypeException;
 import cmdGA.parameterType.InputStreamParameter;
 import cmdGA.parameterType.IntegerParameter;
 import cmdGA.parameterType.OutFileParameter;
 
 import com.sun.image.codec.jpeg.*;
 
 import fileformats.fastaIO.FastaMultipleReader;
 import fileformats.fastaIO.Pair;
 
 public class ConservationImageGenerator {
 
 	////////////////////
 	// Instance Variable
 	private double[] 			data;
 	private String 				bases								= "ACTG";
 	private String			 	amino 								= "ACDEFGHIKLMNPQRSTVWY";
 
 	
 	//////////////
 	// Constructor 
 	
 	public 						ConservationImageGenerator			(double[] data) {
 		this.data = data;
 	}
 	
 	public 						ConservationImageGenerator			() {
 		this.data = null;
 	}
 
 	
 	///////////////////
 	// Public Interface
 	
 	@SuppressWarnings("restriction")
 	public void 				printImage							(File outfile, ColoringStrategy color, int windowSize, Renderer renderer ) throws ImageFormatException, IOException {
 		
 		// notice that createGraphics returns a g2d object directly, no cast!
 		
 		BufferedImage bi = renderer.render(color,this.data,windowSize);
 		
 //		 save the image
 
 		this.exportJPG(outfile, bi);
 	}
 
 	@SuppressWarnings("restriction")
 	private void 				exportJPG							(File outfile, BufferedImage bi) throws FileNotFoundException, IOException {
 		FileOutputStream out = new FileOutputStream(outfile);
 		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
 		param.setQuality(1.0f, false);
 		encoder.setJPEGEncodeParam(param);
 		encoder.encode(bi);
 	}
 	
 	//////////////////
 	// Private Methods
 	
 	/**
 	 * A line above the alignment is used to mark strongly conserved positions. Three characters ('*', ':' and '.') are used:<br>
      * <b>'*'</b> indicates positions which have a single, fully conserved residue.<br>
      * <b>':'</b> indicates that one of the following 'strong' groups is fully conserved: <br>
      * <ol type=disc>
      * <li>STA</li>
      * <li>NEQK</li>
      * <li>NHQK</li>
      * <li>NDEQ</li>
      * <li>QHRK</li>
      * <li>MILV</li>
      * <li>MILF</li>
      * <li>HY</li>
      * <li>FYW</li>
      * </ol>
      * '.' indicates that one of the following 'weaker' groups is fully conserved:<br>
      * <ol type=disc>
      * <li>CSA</li>
      * <li>ATV</li>
      * <li>SAG</li>
      * <li>STNK</li>
      * <li>STPA</li>
      * <li>SGND</li>
      * <li>SNDEQK</li>
      * <li>NDEQHK</li>
      * <li>NEQHRK</li>
      * <li>FVLIM</li>
      * <li>HFY</li>
      * </ol>
      * These are all the positively scoring groups that occur in the Gonnet Pam250 matrix. The strong and weak groups are defined as strong score >0.5 and weak score =<0.5 respectively.
 	 */
 	private double[]			getDataFromClustalConservationProfile	(InputStream inputFastaAlignment, boolean isDNA) {
 		
 		FastaMultipleReader fmr = new FastaMultipleReader();
 		List<Pair<String, String>> alin = null;
 		
 		String[] majorSets = new String[]{"STA","NEQK", "NHQK", "NDEQ", "QHRK", "MILV","MILF","HY", "FYW"};
 		String[] minorSets = new String[]{"CSA","ATV", "SAG", "STNK", "STPA", "SGND","SNDEQK","NDEQHK", "NEQHRK","FVLIM","HFY"};
 		
 		List<Set<Character>> majorSetsList = getSetListFromStringArray(majorSets);
 		List<Set<Character>> minorSetsList = getSetListFromStringArray(minorSets);
 		
 		alin = fmr.readBuffer(new BufferedReader(new InputStreamReader(inputFastaAlignment)));
 
 		int Len = alin.get(0).getSecond().length();
 
 		StringBuilder sb = new StringBuilder(Len);
 		if (isDNA) {
 			// Just Check for identity.
 			for (int x=0; x<Len;x++) {
 				Set<Character> cs = getCharsAtColumn(alin,x);
 				if (cs.size()==1) {
 					sb.append("*");
 				} else {
 					sb.append(" ");
 				}; 
 			}
 		} else {
 			// If is no DNA, is a protein
 			for (int x=0; x<Len;x++) {
 				Set<Character> cs = getCharsAtColumn(alin,x);
 				if (cs.size()==1) {
 					// check for identity
 					sb.append("*");
 				} else {
 					// check major groups
 					boolean found = false;
 					for (Set<Character> group : majorSetsList) {
 						if (!found && group.equals(cs)) {
 							found = true;
 						}
 					}
 					if (found) {
 						sb.append(":");
 					} else {
 						// check minor groups
 						for (Set<Character> group : minorSetsList) {
 							if (!found && group.equals(cs)) {
 								found = true;
 							}
 						}
 						if (found) {
 							sb.append(".");
 						} else {
 							sb.append(" ");					
 						}
 					}
 				}
 			}
 		}
 		return this.getDataFromClustal(sb.toString());
 	}
 	
 	/**
 	 * Given an array of String, converts it to a List of Sets. Where each set represents one of the strings and the elements within the set are the characters from the string. 
 	 * @param strings an array of strings
 	 * @return
 	 */
 	private List<Set<Character>> getSetListFromStringArray(String[] strings) {
 		List<Set<Character>> setslist = new Vector<Set<Character>>();
 		for(String s: strings) {
 			Set<Character> sc = new HashSet<Character>();
 			for (char c: s.toCharArray()) { sc.add(c); }
 			setslist.add(sc);
 		}
 		return setslist;
 	}
 
 	/**
 	 * Given a List<Pair<String, String>> that represents a multiple sequence alignment, this method returns the set of Characters from a single column. 
 	 * @param alin List<Pair<String, String>> that represents an alignment.
 	 * @param column is the position number of the column.
 	 * @return an array of Character with all the characters in the column
 	 */
 	private Set<Character> getCharsAtColumn(List<Pair<String, String>> alin, int column) {
 		Set<Character> set = new HashSet<Character>();
 		for (int x=0;x<alin.size(); x++) {
 			set.add(alin.get(x).getSecond().charAt(column));
 		}
 		return set;
 		
 	}
 	
 	private double[]			getDataFromClustal					(String line) {
 		double[] data = new double[line.length()];
 		
 		for (int i = 0; i < data.length; i++) {
 			switch (line.charAt(i)) {
 			case '*': data[i] = 1; 	 	break;
 
 			case ':': data[i] = 0.5; 	break;
 
 			case '.': data[i] = 0.25; 	break;
 
 			case ' ': data[i] = 0; 		break;
 				
 			default:   					break;
 			}
 		}
 		
 		return data;
 	}
 	
 	private double[]			getDataFromInformationContent		(InputStream inputFastaAlignment, boolean isDNA) {
 		// From "Crooks, Gavin,E. et al, WebLogo: a A sequence Logo Generator. 2004"
 		// R_seq = S_max - S_obs = Log_2(N) - ( - SUM(n=1,N) p_n * Log_2(p_n) )
 
 		FastaMultipleReader fmr = new FastaMultipleReader();
 		List<Pair<String, String>> alin = null;
 
 		double S_max;
 		double[][] p;
 		double[] data;
 		int N;
 		int Len;
 		int n_seqs=0;
 
 		
 
 		alin = fmr.readBuffer(new BufferedReader(new InputStreamReader(inputFastaAlignment)));
 		n_seqs = alin.size();
 		
 		Len = alin.get(0).getSecond().length();
 		if (isDNA) N=4; else N=20;
 		p = new double[Len][N];
 		S_max = Math.log(N)/Math.log(2);
 		
 		
 		if (isDNA) p = getFreqForDNA(p,alin,n_seqs); else p = getFreqForAmino(p,alin,n_seqs);
 
 		data = new double[Len];
 		
 		for (int i = 0; i < data.length; i++) {
 			
 			double s_obs=0;
 
 			for (int j = 0; j < N; j++) {
 				if (p[i][j]!= 0 ) { 
 					s_obs = s_obs +  p[i][j] *  Math.log(p[i][j]) / Math.log(2);
 				}
 			}
 			 
 			data[i] = (S_max + s_obs) / S_max;
 		}
 
 		return data;
 	}	
 	
 	private double[][] 			getFreqForAmino						(double[][] p ,List<Pair<String, String>> alin, int n_seqs) {
 
 		return getFreq(p , alin, n_seqs, this.amino);
 		
 	}
 	
 	private double[][] 			getFreqForDNA						(double[][] p ,List<Pair<String, String>> alin, int n_seqs) {
 
 		return getFreq(p , alin, n_seqs, this.bases);
 		
 	}
 	
 	private double[][] 			getFreq								(double[][] p ,List<Pair<String, String>> alin, int n_seqs, String chars) {
 
 		// Initialize array
 		
 		for (int i=0;i<p.length;i++) {
 			for (int j = 0; j < p[i].length; j++) {
 				p[i][j]=0;
 			} 
 		}
 		
 		// count characters in each position
 		
 		for (int i = 0; i<p.length; i++) {
 			//iterate over each position
 			
 			for (int j = 0; j < n_seqs; j++) {
 				//iterate over each sequence
 
 				char c = Character.toUpperCase(alin.get(j).getSecond().charAt(i));
 				
 				try {
 					p[i][chars.lastIndexOf(c)]++;	
 				} catch (Exception e) {
					System.err.println("el caracter es: " +  c + " y el índice es: " + chars.lastIndexOf(c));
 				}
 				
 				
 			} 
 			
 		}
 		
 		// calculate frequencies
 		
 		for (int i=0;i<p.length;i++) {
 			for (int j = 0; j < p[i].length; j++) {
 				p[i][j]=p[i][j]/n_seqs;
 			} 
 		}
 				
 		return p;
 	}
 	
 	
 	///////////////////
 	// GETTERS & SETTER
 	
 	public double[] 			getData								() {
 		return data;
 	}
 
 	public void 				setData								(double[] data) {
 		this.data = data;
 	}
 	
 	
 	//////////////////
 	// Executable Main
 	public static void 			main								(String[] args) {
 
 		commandlineMain(args);
 
 	}
 	
 	@SuppressWarnings("restriction")
 	public static void commandlineMain(String[] args) {
 		
 		if (args.length==0) {
 			System.err.println("No option was found.\nProgram Terminated.");
 			System.exit(1);
 		}
 		
 		// STEP ONE:
 		// Create a Parser.
 		Parser parser = new Parser();
 		
 		// STEP TWO:
 		// DEFINE THE POSSIBLE OPTIONS ACCEPTED IN THE COMMAND LINE. (TAKE CARE OF AVOID AMBIGUITY) 
 		SingleOption in  = new SingleOption(parser, System.in, "-infile", InputStreamParameter.getParameter());
 		SingleOption outfile = new SingleOption(parser, null, "-outfile", OutFileParameter.getParameter());
 		
 		SingleOption windowSize = new SingleOption(parser, 11, "-window", IntegerParameter.getParameter());
 		NoOption isProtein = new NoOption(parser, "-protein");
 		NoOption isInformationContent = new NoOption(parser, "-ic");
 		SingleOption renderOpt = new SingleOption(parser, new XYPlotRenderer(),"-renderer",RendererParameter.getParameter());
 		SingleOption layoutOpt = new SingleOption(parser, null, "-layout", LayoutParameter.getParameter());
 		
 		
 		// STEP THREE
 		// PARSE THE COMMAND LINE
 		try {
 			parser.parseEx(args);
 		} catch ( IncorrectParameterTypeException e )  {
 			System.err.println( "The was an error:"       );
 			System.err.println(  e.getMessage()        );
 			System.err.println( "Program Terminated." );
 			System.exit(1);
 		}
 		
 			
 		if (outfile.getValue() == null) {
 			System.err.println("No outfile was given.\n.");
 		}
 		
 		// Program 
 		ConservationImageGenerator cig = new ConservationImageGenerator();
 		InputStream invalue = (InputStream) in.getValue();
 		
 		
 		if (isInformationContent.isPresent()) {
 			cig.setData(cig.getDataFromInformationContent(invalue, !isProtein.isPresent()));
 		} else {
 			cig.setData(cig.getDataFromClustalConservationProfile(invalue, !isProtein.isPresent()));
 		}
 		
 		try {   
 			Renderer renderer = (Renderer) renderOpt.getValue();
 			
 			DrawingLayout layout = (DrawingLayout) layoutOpt.getValue();
 			
 			if (layout ==null) { layout = renderer.getDefaultLayout() ; }
 			
 			renderer.setLayout(layout);
 			
 			cig.printImage((File)outfile.getValue(), new RedBlueColorringStrategy(),(Integer) windowSize.getValue(),renderer );   		
 			} catch (ImageFormatException e) { System.out.println("Hubo Un error con el formato de la imagen."); 
 				e.printStackTrace();  
 			} catch (IOException e) {          System.out.println("Hubo Un error con el archivo de salida.");
 				e.printStackTrace(); }
 	}
 
 }
