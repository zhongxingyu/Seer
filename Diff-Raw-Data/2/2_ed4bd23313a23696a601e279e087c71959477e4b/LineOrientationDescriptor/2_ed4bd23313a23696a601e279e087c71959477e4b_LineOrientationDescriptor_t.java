 package ca.etsmtl.capra.vision;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import ca.etsmtl.capra.util.saving.Saveable;
 import ca.etsmtl.octets.vision.commons.descriptors.DescriptorParameter;
 import ca.etsmtl.octets.vision.commons.descriptors.ReflexiveVisionDescriptor;
 
 /**
  	stringstream message;
 	message << "LineOrientation: x1=" << ptLine1.x << " y1=" << ptLine1.y << " x2=" << ptLine2.x << " y2=" << ptLine2.y << endl;
 	sendMessage(message.str());
 
  * 
  * @author lynch
  */
 public class LineOrientationDescriptor extends ReflexiveVisionDescriptor {
 
 	private static final String LINE_ORIENTATION = "LineOrientation";
 	private final LineParser lineParser = new LineParser(LINE_ORIENTATION);
 	
 	private Collection<Line> lines = new ArrayList<Line>();
 	private List<Line> bufferLines = new ArrayList<Line>();
 
 	@DescriptorParameter
 	private Float lineOrientation = 0.0f;
 	
 	public LineOrientationDescriptor(String filterchainName) {
 		super(filterchainName, LINE_ORIENTATION);
 	}
 	
 	public Float getOrientation() {
 		return lineOrientation;
 	}
 	
 	@Override
 	public boolean parseArguments(String definition) {
 	
 		// this method receive an array of line coordinates and finally its receive a lineOrientation
 		if (definition.contains("x1=")) {
 			// this is a line coordinate to parse
 			lineParser.parseArguments(definition);
 			bufferLines.add(lineParser.createLine());
 		}
 		else {
 			// reset the array if its the lineOrientation descriptor
 			lines = Collections.unmodifiableCollection(bufferLines);
 			bufferLines = new ArrayList<Line>();
 			super.parseArguments(definition);
 		}
 		
 		return true;
 	}
 	
 	public LineParser getLineParser(){
 		return lineParser;
 	}
 	
 	public Collection<Line> getLines() {
		return lines;
 	}
 
 	public static class LineParser extends ReflexiveVisionDescriptor {
 		
 		@DescriptorParameter
 		private Integer x1 = 0;
 		@DescriptorParameter
 		private Integer x2 = 0;
 		@DescriptorParameter
 		private Integer y1 = 0;
 		@DescriptorParameter
 		private Integer y2 = 0;
 		
 		LineParser(String filterchain) {
 			super(filterchain, LINE_ORIENTATION);
 		}
 		
 		Line createLine() {
 			return new Line(x1, x2, y1, y2);
 		}
 	}
 	
 	public static class Line {
 		
 		@Saveable
 		public final int x1;
 		@Saveable
 		public final int x2;
 		@Saveable
 		public final int y1;
 		@Saveable
 		public final int y2;
 
 		public Line(int x1, int x2, int y1, int y2) {
 			this.x1 = x1;
 			this.x2 = x2;
 			this.y1 = y1;
 			this.y2 = y2;
 		}
 		
 		public Line(){
 			x1=0;
 			x2=0;
 			y1=0;
 			y2=0;
 		}
 	}
 }
