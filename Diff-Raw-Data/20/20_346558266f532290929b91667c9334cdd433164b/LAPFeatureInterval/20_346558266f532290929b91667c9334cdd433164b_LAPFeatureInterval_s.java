 package LinearArcPlotEditor;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.geom.Line2D;
 
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 
 import com.clcbio.api.clc.graphics.framework.ChildDrawingNode;
 import com.clcbio.api.clc.graphics.framework.DrawingLayer;
 import com.clcbio.api.clc.graphics.framework.DrawingResult;
 import com.clcbio.api.clc.graphics.framework.RootDrawingNode;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
 
 public class LAPFeatureInterval extends ChildDrawingNode implements Comparable{
 
 	Line2D startLine;
 	Line2D endLine;
 	private int startPos;
 	private int endPos;
 	private int offset;
 	
 	
 	
 	
 	
 	private String name;
 	
 	
 	private LAPFeatureType type;
 	private LAP root;
 	
 	public LAPFeatureInterval(String name, int startPos, int endPos, int offset, LAP root){
 		this.name = name;
 		this.startPos = startPos;
 		this.endPos = endPos;
 		this.offset = offset;
 		System.out.println("offset: " + offset + " \n");
 		this.startLine = new Line2D.Double(startPos*getScaleX(), offset+30, startPos*getScaleX(), offset+15);
 		this.endLine = new Line2D.Double(endPos*getScaleX(),offset+30,endPos*getScaleX(), offset);
 		this.root = root;
 	}
 	
 	private void update(){
 		
 	}
 	
 	protected DrawingResult internalDraw(Graphics2D g2, boolean drawoutline, DrawingLayer drawinglayer, double minx, double maxx, double miny, double maxy)
 	{
 		if(type.isRelevant()){
 		//	if(type.isChanged() || type.getLastX() != root.getXViewBounds()){
 				g2.setStroke(new BasicStroke(1));
 				g2.setColor(Color.BLACK);
 		
 				int lineStart = (int)(startPos*getScaleX());
 				int lineEnd = (int)(endPos*getScaleX());
 		
				g2.draw(new Line2D.Double(lineStart, root.getBaseXAxis()+offset+15, lineEnd, root.getBaseXAxis()+offset+15));
				startLine = new Line2D.Double(lineStart, root.getBaseXAxis()+offset+30, lineStart, root.getBaseXAxis()+offset+15);
				endLine = new Line2D.Double(lineEnd,root.getBaseXAxis()+offset+30,lineEnd,root.getBaseXAxis()+offset);
 				g2.draw(startLine);
 				g2.draw(endLine);
 				
 			}
 		//}
 		return DrawingResult.NORMAL;
 	}
 	
 	public int getStartPos(){
 		return startPos;
 	}
 	
 	public int getEndPos(){
 		return endPos;
 	}
 	
 	public int getOffset(){
 		return offset;
 	}
 	
 	public void setOffset(int o){
 		this.offset=o;
 	}
 	
 	public void addToOffset(int a){
 		this.offset += a;
 	}
 
 	@Override
 	public int compareTo(Object o) {
 		LAPFeatureInterval li = (LAPFeatureInterval)o;
 		return this.getStartPos() < li.getStartPos() ? 1 : 0;
 		
 	}
 
 	public LAPFeatureType getType() {
 		return type;
 	}
 
 	public void setType(LAPFeatureType type) {
 		this.type = type;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 }
