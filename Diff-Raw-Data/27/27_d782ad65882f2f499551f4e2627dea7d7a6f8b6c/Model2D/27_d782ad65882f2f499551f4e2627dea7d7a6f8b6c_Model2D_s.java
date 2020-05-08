 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.model;
 
 import net.epsilony.tb.solid.Polygon2D;
 import net.epsilony.tb.solid.Node;
 import net.epsilony.tb.solid.Line2D;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import net.epsilony.mf.model.influence.InfluenceRadiusCalculator;
 import net.epsilony.mf.model.support_domain.SupportDomainSearcherFactory;
 import net.epsilony.tb.solid.Segment;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class Model2D {
 
     public final static int DIMENSION = 2;
     ArrayList<MFNode> allNodes;
     ArrayList<MFNode> spaceNodes;   //allNode except polygon.getVertes()
     private Polygon2D<MFNode> polygon;
     private double maxInfluenceRadius;
     private SupportDomainSearcherFactory supportDomainSearcherFactory;
     private InfluenceRadiusCalculator influenceRadiusCalculator;
 
     public Polygon2D<MFNode> getPolygon() {
         return polygon;
     }
 
     public ArrayList<MFNode> getSpaceNodes() {
         return spaceNodes;
     }
 
     public ArrayList<MFNode> getAllNodes() {
         return allNodes;
     }
 
     public Model2D(Polygon2D<MFNode> polygon, List<MFNode> spaceNodes) {
         this.polygon = polygon;
        this.polygon.fillSegmentsIds();
         allNodes = new ArrayList<>();
         if (null != this.polygon) {
             LinkedList<MFNode> segNds = new LinkedList<>();
             for (Line2D<MFNode> seg : this.polygon) {
                 segNds.add(seg.getStart());
             }
             allNodes.addAll(segNds);
         }
 
         this.spaceNodes = new ArrayList<>(spaceNodes);
         allNodes.addAll(spaceNodes);
 
         int id = 0;
         for (Node nd : allNodes) {
             nd.setId(id);
             id++;
         }
     }
 
     public void updateInfluenceAndSupportDomains(InfluenceRadiusCalculator influenceRadiusCalculator) {
         this.influenceRadiusCalculator = influenceRadiusCalculator;
         supportDomainSearcherFactory = new SupportDomainSearcherFactory();
         supportDomainSearcherFactory.setAllMFNodes(getAllNodes());
         if (null != getPolygon()) {
             supportDomainSearcherFactory.setBoundaries(getPolygon().getChainsHeads());
         } else {
             supportDomainSearcherFactory.setSegmentsSearcher(null);
         }
 
         influenceRadiusCalculator.setSupportDomainSearcher(supportDomainSearcherFactory.produce());
         for (MFNode nd : getSpaceNodes()) {
             double rad = influenceRadiusCalculator.calcInflucenceRadius(nd, null);
             nd.setInfluenceRadius(rad);
         }
 
         if (null != getPolygon()) {
             for (Segment seg : getPolygon()) {
                 MFNode nd = (MFNode) seg.getStart();
                 influenceRadiusCalculator.calcInflucenceRadius(nd, seg);
             }
         }
 
         maxInfluenceRadius = MFNode.calcMaxInfluenceRadius(getAllNodes());
     }
 
     public double getMaxInfluenceRadius() {
         return maxInfluenceRadius;
     }
 
     public SupportDomainSearcherFactory getSupportDomainSearcherFactory() {
         return supportDomainSearcherFactory;
     }
 
     public InfluenceRadiusCalculator getInfluenceRadiusCalculator() {
         return influenceRadiusCalculator;
     }
 }
