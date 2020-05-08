 package dk.frv.eavdam.layers;
 
 import com.bbn.openmap.InformationDelegator;
 import com.bbn.openmap.LayerHandler;
 import com.bbn.openmap.gui.OpenMapFrame;
 import com.bbn.openmap.layer.OMGraphicHandlerLayer;
 import com.bbn.openmap.proj.Projection;
 import com.bbn.openmap.omGraphics.OMGraphic;
 import com.bbn.openmap.omGraphics.OMGraphicList;
 import com.bbn.openmap.omGraphics.OMLine;
 import com.bbn.openmap.omGraphics.OMRect;
 import com.bbn.openmap.omGraphics.OMText;
 import dk.frv.eavdam.data.AISDatalinkCheckArea;
 import java.awt.Color;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JOptionPane;
 
 public class AISDatalinkCheckBandwidthAreasLayer extends OMGraphicHandlerLayer {
 
 	private static final long serialVersionUID = 1L;
 	
 	private OMGraphicList graphics = new OMGraphicList();
 	private OpenMapFrame openMapFrame;
 	private LayerHandler layerHandler;
 	private InformationDelegator infoDelegator;
 	
 	private List<AISDatalinkCheckArea> areas = null;	
 	
 	public AISDatalinkCheckBandwidthAreasLayer() {}
 	
 	public List<AISDatalinkCheckArea> getAreas() {
 		return areas;
 	}
 	
 	public void setAreas(List<AISDatalinkCheckArea> areas) {
 		this.areas = areas;
 	}
 	
 	public OMGraphicList getGraphicsList() {
 	    return graphics;
 	}
 
 	@Override
 	public synchronized OMGraphicList prepare() {	
 	
 		graphics.clear();
 		
 		// XXX: for testing
 		/*
 		areas = new ArrayList<AISDatalinkCheckArea>();
 		areas.add(new AISDatalinkCheckArea(60.5, 10, 59, 11, 0));
 		areas.add(new AISDatalinkCheckArea(60.5, 11, 59, 12, 0.05));
 		areas.add(new AISDatalinkCheckArea(60.5, 12, 59, 13, 0.15));
 		areas.add(new AISDatalinkCheckArea(60.5, 13, 59, 14, 0.25));
 		areas.add(new AISDatalinkCheckArea(60.5, 14, 59, 15, 0.35));
 		areas.add(new AISDatalinkCheckArea(60.5, 15, 59, 16, 0.45));
 		areas.add(new AISDatalinkCheckArea(60.5, 16, 59, 17, 0.55));
 		areas.add(new AISDatalinkCheckArea(60.5, 17, 59, 18, 0.65));		
 		areas.add(new AISDatalinkCheckArea(60.5, 18, 59, 19, 0.75));
 		areas.add(new AISDatalinkCheckArea(60.5, 19, 59, 20, 0.85));
 		areas.add(new AISDatalinkCheckArea(60.5, 20, 59, 21, 0.95));
 		areas.add(new AISDatalinkCheckArea(60.5, 21, 59, 22, 1));
 		*/
 		
 		if (areas != null) {
 		
 			for (AISDatalinkCheckArea area : areas) {
 		
 				double topLeftLatitude = area.getTopLeftLatitude();
 				double topLeftLongitude = area.getTopLeftLongitude();
 				double lowerRightLatitude = area.getLowerRightLatitude();
 				double lowerRightLongitude = area.getLowerRightLongitude();
 				double bandwithUsageLevel = area.getBandwithUsageLevel();
 		
 				OMRect omRect = new OMRect(topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude, OMGraphic.LINETYPE_RHUMB);
 				Color c = null;
 				if (bandwithUsageLevel >= 0 && bandwithUsageLevel <= 0.1) {
 					c = new Color(144, 238, 144, (int) Math.round(2.55*5+bandwithUsageLevel*10*2.55*5));  // 0-10% BW loading:   light green colors  95-90% transparency
 				} else if (bandwithUsageLevel > 0.1 && bandwithUsageLevel <= 0.2) {	
 					c = new Color(0, 128, 0, (int) Math.round(2.55*10+(bandwithUsageLevel-0.1)*10*2.55*5));  // 11-20% BW loading: medium green colors 90-85% transparency
 				} else if (bandwithUsageLevel > 0.2 && bandwithUsageLevel <= 0.3) {	
 					c = new Color(128, 128, 0, (int) Math.round(2.55*15+(bandwithUsageLevel-0.2)*10*2.55*5));  // 21-30% BW loading: dark yellow colors 85-80% transparency
 				} else if (bandwithUsageLevel > 0.3 && bandwithUsageLevel <= 0.4) {	
 					c = new Color(255, 51, 51, (int) Math.round(2.55*20+(bandwithUsageLevel-0.3)*10*2.55*10));  // 31-40% BW loading: light red colors 80-70% transparency
 				} else if (bandwithUsageLevel > 0.4 && bandwithUsageLevel <= 0.5) {	
 					c = new Color(255, 0, 0, (int) Math.round(2.55*30+(bandwithUsageLevel-0.4)*10*2.55*10));  // 41-50% BW loading: Medium red colors 70-60% transparency				
 				} else if (bandwithUsageLevel > 0.5 && bandwithUsageLevel <= 1) {	
 					c = new Color(204, 0, 0, (int) Math.round(2.55*40+(bandwithUsageLevel-0.5)*2*2.55*50));  // Above 50% BW loading: Dark red colors 60-10% transparency
 				}
 				if (c != null) {
 					omRect.setFillPaint(c);
 					omRect.setLinePaint(c);
 					graphics.add(omRect);
 				}		
 			}
 		}
 		
 		graphics.project(getProjection(), true);
 		this.repaint();
 		this.validate();
 		return graphics;
 	}
 
 	@Override
 	public void findAndInit(Object obj) {
 		if (obj instanceof OpenMapFrame) {
 			this.openMapFrame = (OpenMapFrame) obj;	
 		} else if (obj instanceof LayerHandler) {
 		    this.layerHandler = (LayerHandler) obj;
 		} else if (obj instanceof InformationDelegator) {
 			this.infoDelegator = (InformationDelegator) obj;	    
 		}
 	}
 
 }
