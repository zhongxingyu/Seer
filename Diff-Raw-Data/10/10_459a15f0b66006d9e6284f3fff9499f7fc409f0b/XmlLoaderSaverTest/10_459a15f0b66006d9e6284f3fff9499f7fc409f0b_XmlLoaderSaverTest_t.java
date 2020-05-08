 package edu.kpi.pzks.gui;
 
 import edu.kpi.pzks.core.exceptions.GraphException;
 import edu.kpi.pzks.core.model.Graph;
 import edu.kpi.pzks.core.model.Link;
 import edu.kpi.pzks.core.model.Links;
 import edu.kpi.pzks.core.model.Node;
 import edu.kpi.pzks.gui.io.GraphLoader;
 import edu.kpi.pzks.gui.io.GraphSaver;
 import edu.kpi.pzks.gui.io.impl.XmlGraphLoader;
 import edu.kpi.pzks.gui.io.impl.XmlGraphSaver;
 import edu.kpi.pzks.gui.modelview.GraphView;
 import edu.kpi.pzks.gui.modelview.LinkView;
 import edu.kpi.pzks.gui.modelview.NodeView;
 import edu.kpi.pzks.gui.modelview.impl.GraphViewImpl;
 import edu.kpi.pzks.gui.modelview.impl.LinkViewImpl;
 import java.awt.Point;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 import java.util.Set;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Aloren
  */
 public class XmlLoaderSaverTest {
 
     Random random = new Random();
     int size = 5;
     Set<Node> nodes;
     Set<Link> links;
     Collection<LinkView> linkViews;
     Collection<NodeView> nodeViews;
 
     @Test
     public void testSave() {
         nodes = getNodes();
         links = getLinks(nodes);
         linkViews = createLinkViewsCollection();
         nodeViews = createNodeViewsCollection();
 
         String fileName = "test.xml";
         GraphSaver saver = new XmlGraphSaver();
         GraphLoader loader = new XmlGraphLoader();
 
         File file = new File(fileName);
         if (file.exists()) {
             file.delete();
         }
         try {
             file.createNewFile();
         } catch (IOException ex) {
             fail("Exception while creating new file!");
         }
         GraphView graphView = mock(GraphViewImpl.class);
         Graph graph = mock(Graph.class);
         when(graph.getLinks()).thenReturn(new Links(links));
         when(graph.getNodes()).thenReturn(nodes);
         when(graphView.getGraph()).thenReturn(graph);
         when(graph.isOriented()).thenReturn(Boolean.TRUE);
 
         when(graphView.getLinkViews()).thenReturn(linkViews);
         when(graphView.getNodeViews()).thenReturn(nodeViews);
         when(graphView.getType()).thenReturn(GraphViewImpl.TYPE.TASK);
         try {
             saver.saveToFile(graphView, file);
         } catch (GraphException ex) {
             fail("Exception while saving graph to file!");
         }
         //TODO add validation
         try {
             GraphView testGraphView = loader.loadFromFile(file);
             checkGraphs(testGraphView, graphView);
         } catch (GraphException ex) {
             fail("Unexpected exception");
         }
 
         //Comment this line if you want to look through file with saved graph
         file.delete();
     }
 
     private Set<Node> getNodes() {
         Set<Node> nodes = new HashSet<>();
         for (int i = 0; i < size; i++) {
            nodes.add(new Node(random(1, 100)));
         }
         return nodes;
     }
 
     private Set<Link> getLinks(Set<Node> nodes) {
         Set<Link> links = new HashSet<>();
         Node[] nodesAr = nodes.toArray(new Node[nodes.size()]);
         for (int i = 0; i < size; i++) {
             Node fromNode = nodesAr[i];
             Node toNode = (i + 1 == size) ? nodesAr[0] : nodesAr[i + 1];
            links.add(new Link(fromNode, toNode, random(1, 100)));
         }
         return links;
     }
 
     private Collection<LinkView> createLinkViewsCollection() {
         Collection<LinkView> linkViews = new LinkedList<>();
         Link[] linksAr = links.toArray(new Link[links.size()]);
         for (int i = 0; i < size; i++) {
             LinkView linkView = mock(LinkViewImpl.class);
             when(linkView.getLink()).thenReturn(linksAr[i]);
             when(linkView.getBendPoint()).thenReturn(new Point(i * 5, i * 5));
             linkViews.add(linkView);
         }
         return linkViews;
     }
 
     private Collection<NodeView> createNodeViewsCollection() {
         Collection<NodeView> nodeViews = new LinkedList<>();
         Node[] nodesAr = nodes.toArray(new Node[nodes.size()]);
         for (int i = 0; i < size; i++) {
             NodeView nodeView = mock(NodeView.class);
             when(nodeView.getNode()).thenReturn(nodesAr[i]);
             when(nodeView.getUpperLeftCorner()).thenReturn(new Point(i, i));
             nodeViews.add(nodeView);
         }
         return nodeViews;
     }
 
     private void checkGraphs(GraphView testGraphView, GraphView controlGraphView) {
         List<NodeView> testNodeViews = new LinkedList<>(testGraphView.getNodeViews());
         List<NodeView> controlNodeViews = new LinkedList<>(controlGraphView.getNodeViews());
 
         assertTrue(testNodeViews.size() == controlNodeViews.size());
 
         List<LinkView> testLinkViews = new LinkedList<>(testGraphView.getLinkViews());
         List<LinkView> controlLinkViews = new LinkedList<>(controlGraphView.getLinkViews());
 
         assertTrue(testLinkViews.size() == controlLinkViews.size());
 
         Graph testGraph = testGraphView.getGraph();
         Graph controlGraph = controlGraphView.getGraph();
 
         List<Link> testLinks = new LinkedList<>(testGraph.getLinks());
         List<Link> controlLinks = new LinkedList<>(controlGraph.getLinks());
 
         assertTrue(testLinks.size() == controlLinks.size());
 
         List<Node> testNodes = new LinkedList<>(testGraph.getNodes());
         List<Node> controlNodes = new LinkedList<>(controlGraph.getNodes());
 
         assertTrue(testNodes.size() == controlNodes.size());
 
         assertTrue(testLinkViews.size() == testLinks.size());
         assertTrue(testNodeViews.size() == testNodes.size());
 
         ListIterator<NodeView> tit = testNodeViews.listIterator();
         ListIterator<NodeView> cit = controlNodeViews.listIterator();
         while (tit.hasNext()) {
             NodeView testNodeView = tit.next();
             NodeView controlNodeView = cit.next();
             while (!testNodeView.getUpperLeftCorner().equals(controlNodeView.getUpperLeftCorner())
                     && testNodeView.getNode().getWeight() != controlNodeView.getNode().getWeight()) {
                 controlNodeView = cit.next();
             }
             cit.remove();
             cit = controlNodeViews.listIterator();
         }
         assertTrue(controlNodeViews.isEmpty());
 
         ListIterator<LinkView> titL = testLinkViews.listIterator();
         ListIterator<LinkView> citL = controlLinkViews.listIterator();
         while (titL.hasNext()) {
             LinkView testLinkView = titL.next();
             LinkView controlLinkView = citL.next();
             final Link testLink = testLinkView.getLink();
             final Link controlLink = controlLinkView.getLink();
             while (!testLinkView.getBendPoint().equals(controlLinkView.getBendPoint())
                     && testLink.getWeight() != controlLink.getWeight()
                     && testLink.getFromNode().getWeight() != controlLink.getFromNode().getWeight()
                     && testLink.getToNode().getWeight() != controlLink.getToNode().getWeight()) {
                 controlLinkView = citL.next();
             }
             citL.remove();
             citL = controlLinkViews.listIterator();
         }
         assertTrue(controlLinkViews.isEmpty());
 
         ListIterator<Link> linkTIt = testLinks.listIterator();
         ListIterator<Link> linkCIt = controlLinks.listIterator();
         while (linkTIt.hasNext()) {
             Link testLink = linkTIt.next();
             Link controlLink = linkCIt.next();
             while (testLink.getWeight() != controlLink.getWeight()
                     && testLink.getFromNode().getWeight() != controlLink.getFromNode().getWeight()
                     && testLink.getToNode().getWeight() != controlLink.getToNode().getWeight()) {
                 controlLink = linkCIt.next();
             }
             linkCIt.remove();
             linkCIt = controlLinks.listIterator();
         }
 
         ListIterator<Node> nodeTIt = testNodes.listIterator();
         ListIterator<Node> nodeCIt = controlNodes.listIterator();
         while (nodeTIt.hasNext()) {
             Node testNode = nodeTIt.next();
             Node controlNode = nodeCIt.next();
             while (testNode.getWeight() != controlNode.getWeight()) {
                 controlNode = nodeCIt.next();
             }
             nodeCIt.remove();
             nodeCIt = controlNodes.listIterator();
         }
 
     }

    private static int random(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
 }
