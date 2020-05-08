 package utils.mutualinformation.misticmod;
 
 import graphics.profile.PngWriter;
 import io.bufferreaders.UncommenterBufferedReader;
 import io.onelinelister.OneLineListReader;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 
 import cmdGA2.CommandLine;
 import cmdGA2.MultipleArgumentOption;
 import cmdGA2.OptionsFactory;
 import cmdGA2.SingleArgumentOption;
 import cmdGA2.returnvalues.OutfileValue;
 
 public class MIOnePixelMap {
 	
 	public static void main(String[] args) {
 		
 		////////////////////////
 		// Create Command Line
 		CommandLine cmd = new CommandLine();
 		
 		////////////////////////////
 		// Add Command Line Options
 		SingleArgumentOption<InputStream> inOpt = OptionsFactory.createBasicInputStreamArgument(cmd);
 		
		SingleArgumentOption<File> outOpt = new SingleArgumentOption<File>(cmd, "-out", new OutfileValue() , null);
 		
		MultipleArgumentOption<Integer> lenOpt = OptionsFactory.createBasicCommaSeparatedIntegersArgument(cmd, "-lengths");
 
 		////////////////////////////////
 		// Parse Command Line
 		cmd.readAndExitOnError(args);
 		
 		////////////////////////////////
 		// Get Values From Command Line
 		InputStream in = inOpt.getValue();
 		
 		File out = outOpt.getValue();
 		
 		List<Integer> lengths = lenOpt.getValues();
 
 		////////////////////////////////
 		// Validate Options
 		if (!outOpt.isPresent() || out==null) {
 			
 			System.err.println("There was an error with the output file");
 			
 			System.exit(1);
 			
 		}
 		
 		if (!lenOpt.isPresent() || lengths.isEmpty()) {
 			
 			System.err.println("There was an error with the protein lengths");
 			
 			System.exit(1);
 			
 		}
 		
 		////////////////////////////////
 		// Program
 		MIOnePixelMap.MI_PosLinePaser mi_parser = new MIOnePixelMap.MI_PosLinePaser();
 		
 		mi_parser.setLengths(lengths);
 		
 		OneLineListReader<MI_PositionWithProtein> reader = new OneLineListReader<>(mi_parser);
 		
 		try {
 			
 			List<MI_PositionWithProtein> positions = reader.read(new UncommenterBufferedReader(new InputStreamReader(in)));
 
 			int max = getMaxResidueNumber(positions);
 			
 			double maxMIValue = getMaxValue(positions);
 			
 			double minMIValue = getMinValue(positions);
 			
 			BufferedImage bi = getImage(lengths, positions, max, maxMIValue, minMIValue);
 			
 			PngWriter writer = new PngWriter();
 			
 			writer.write(out, bi);
 			
 		} catch (IOException e) {
 
 			System.err.println("There was an error, trying to read the input data: "+ e.getMessage());
 			
 			System.exit(1);
 			
 		}
 		
 	}
 
 	public static BufferedImage getImage(List<Integer> lengths, List<MI_PositionWithProtein> positions, int max, double maxMIValue, double minMIValue) {
 		
 		BufferedImage bi = new BufferedImage(max+lengths.size()-1, max + lengths.size()-1, BufferedImage.TYPE_INT_RGB);
 		
 		bi.getGraphics().setColor(Color.black);
 		
 		bi.getGraphics().fillRect(0, 0, max+lengths.size()-1, max+max+lengths.size()-1);
 		
 		drawMI_Points(positions, maxMIValue, minMIValue, bi);
 		
 		return drawProteinRegions(lengths, max, bi);
 	}
 
 	public static BufferedImage drawProteinRegions(List<Integer> lengths, int max, BufferedImage bi) {
 
 		BufferedImage bli = new BufferedImage(max + 50 + lengths.size()-1, max+50 + lengths.size()-1, BufferedImage.TYPE_INT_RGB);
 		
 		Graphics2D graphics = (Graphics2D) bli.getGraphics();
 
 		int offset = 50;
 		
 		Color[] colors = new Color[]{Color.red, Color.green}; 
 		
 		int i =0;
 		
 		int region_counter = 0;
 		
 		for (int len : lengths) {
 		
 			graphics.setColor(colors[i]);
 			
 			graphics.fillRect(offset + region_counter , 0 , len, 50);	
 			
 			graphics.fillRect(0 ,offset+region_counter , 50 , len);
 			
 			offset = offset + len;
 			
 			region_counter = region_counter+1;
 			
 			i=1-i;
 			
 		}
 		
 		graphics.drawImage(bi, null, 50, 50);
 		
 		return bli;
 	}
 
 	public static void drawMI_Points(List<MI_PositionWithProtein> positions, double maxMIValue, double minMIValue, BufferedImage bi) {
 		
 		for (MI_PositionWithProtein mi_PositionWithProtein : positions) {
 			
 			int rgb = calculateRGB(mi_PositionWithProtein.getMi(), maxMIValue, minMIValue);
 			
 			bi.setRGB(mi_PositionWithProtein.getPos1()-1 + mi_PositionWithProtein.getProtein_1(), mi_PositionWithProtein.getPos2()-1  + mi_PositionWithProtein.getProtein_2(), rgb);
 			
 			bi.setRGB(mi_PositionWithProtein.getPos2()-1  + mi_PositionWithProtein.getProtein_2(), mi_PositionWithProtein.getPos1()-1  + mi_PositionWithProtein.getProtein_1(), rgb);
 			
 		}
 		
 	}
 
 	private static int calculateRGB(Double mi , double maxMI, double minMI) {
 		
 		if (mi < (-900) ) {
 			
 			return new Color(0,50,0).getRGB();
 			
 		} else 
 		
 		if (mi<0) {
 			
 			double v = (mi / minMI);
 			
 			return new Color (0 , 0 ,(int) (255 * Math.abs(v))).getRGB();
 			
 		} else {
 			
 			return new Color ((int) (255 * (mi / maxMI)),0,0).getRGB();
 			
 		}
 		
 	}
 
 	private static double getMaxValue(List<MI_PositionWithProtein> positions) {
 		
 		double max = Double.NEGATIVE_INFINITY;
 
 		for (MI_PositionWithProtein mi_PositionWithProtein : positions) {
 			
 			max = Math.max(max, mi_PositionWithProtein.getMi());
 			
 		}
 		
 		return max;
 	}
 
 	private static double getMinValue(List<MI_PositionWithProtein> positions) {
 		
 		double min = Double.POSITIVE_INFINITY;
 
 		for (MI_PositionWithProtein mi_PositionWithProtein : positions) {
 			
 			Double current_MI = mi_PositionWithProtein.getMi();
 			
 			if (current_MI>-900) {
 
 				min = Math.min(min, current_MI);
 				
 			}
 			
 		}
 		
 		return min;
 		
 	}
 	
 	public static int getMaxResidueNumber(List<MI_PositionWithProtein> positions) {
 		
 		int max = Integer.MIN_VALUE;
 		
 		for (MI_PositionWithProtein mi_PositionWithProtein : positions) {
 			
 			max= Math.max(mi_PositionWithProtein.getPos1(), max);
 			
 			max= Math.max(mi_PositionWithProtein.getPos2(), max);
 			
 		}
 		
 		return max;
 		
 	}
 	
 	///////////////////////////////////
 	// Auxiliary classes
 	static class MI_PosLinePaser implements OneLineListReader.LineParser<MI_PositionWithProtein>{
 
 		List<Integer> lengths;
 		
 		protected void setLengths(List<Integer> lengths) {
 			
 			this.lengths = lengths;
 			
 		}
 
 		@Override
 		public MI_PositionWithProtein parse(String line) {
 
 			MI_PositionWithProtein pos = MI_PositionWithProtein.valueOf(line);
 			
 			pos.assignProteinNumber(lengths);
 			
 			return pos;
 			
 		}
 		
 		
 		
 		
 	}
 	
 
 }
