 package speedith.draw;
 
 import speedith.core.lang.PrimarySpiderDiagram;
 import speedith.core.lang.SpiderDiagram;
 import speedith.core.lang.Zone;
 import speedith.core.lang.Zones;
 import speedith.core.lang.reader.SpiderDiagramsReaderTest;
 import speedith.core.reasoning.rules.transformers.CopyContoursTransformerTest;
import speedith.core.reasoning.util.unitary.ContourRelationsTest;
 import speedith.core.reasoning.util.unitary.ZoneTransferTest;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static speedith.core.lang.SpiderDiagrams.createPrimarySD;
 import static speedith.core.lang.Zones.*;
 import static speedith.core.reasoning.rules.transformers.CopyContoursTransformer.addContoursToDiagram;
 import static speedith.core.reasoning.rules.transformers.CopyContoursTransformerTest.diagramWithASpider;
 import static speedith.draw.SpiderDiagramPanelTest.*;
 
 public class DiagramTestPortfolio implements Serializable {
     public final PrimarySpiderDiagram shadedBInsideA = createPrimarySD(null, null, asList(CopyContoursTransformerTest.zoneBMinusA, CopyContoursTransformerTest.zoneBAndA), asList(CopyContoursTransformerTest.zoneBAndA, CopyContoursTransformerTest.zoneAMinusB));
     private ArrayList<SpiderDiagram> spiderDiagrams;
 
     public DiagramTestPortfolio() {
         this.spiderDiagrams = new ArrayList<>(asList(
                 ZoneTransferTest.getDiagramABCWhereCContainsA(),
                 ZoneTransferTest.getDiagramSpeedithPaperD2(),
                 ZoneTransferTest.getDiagramSpeedithPaperD1(),
                 ZoneTransferTest.getDiagramABCWhereCContainsA(),
                 ZoneTransferTest.diagramABC_shadedSetC_A,
                 ZoneTransferTest.diagramABC_shadedSetAC,
                ContourRelationsTest.getVennABCDiagramWithPartlyShadedBAndSpiderInZoneBC_A(),
                ContourRelationsTest.getVennABCDiagramWithPartlyShadedB(),
                ContourRelationsTest.getVennABCDiagramWithShadedBC(),
                 diagramWithASpider,
                 addContoursToDiagram(diagramWithASpider, asList("G")),
                 shadedBInsideA,
                 createPrimarySD(null, null, sameRegionWithNewContours(extendRegionWithNewContour(asList(Zone.fromInContours("A")), "B", null), "C"), Zones.allZonesForContours("A", "B", "C")),
                 createPrimarySD(null, null, sameRegionWithNewContours(asList(Zone.fromInContours("B")), "A", "C"), Zones.allZonesForContours("A", "B", "C")),
                 createPrimarySD(null, null, sameRegionWithNewContours(asList(Zone.fromInContours("B")), "A"), Zones.sameRegionWithNewContours(asList(Zone.fromInContours("B")), "A")),
                 createPrimarySD(null, null, getZonesOutsideContours(Zones.allZonesForContours("A", "B", "C", "D"), "C", "B"), Zones.allZonesForContours("A", "B", "C", "D")),
                 getSDExample1(),
                 addContoursToDiagram(getSDExample1(), asList("C")),
                 getSDExample2(),
                 getSDExample3()
         ));
         loadFromTestFiles();
     }
 
     public List<SpiderDiagram> getSpiderDiagrams() {
         return Collections.unmodifiableList(spiderDiagrams);
     }
 
     public int size() {
         return getSpiderDiagrams().size();
     }
 
     public SpiderDiagram getDiagramAt(int spiderDiagramIndex) {
         return getSpiderDiagrams().get(spiderDiagramIndex);
     }
 
     public static void main(String[] args) {
         new LotsOfDiagramsForm().setVisible(true);
     }
 
     private void loadFromTestFiles() {
         for (int i = 0; i < SpiderDiagramsReaderTest.testFilesCount(); i++) {
             try {
                 spiderDiagrams.add(SpiderDiagramsReaderTest.readFromTestFile(i));
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 }
