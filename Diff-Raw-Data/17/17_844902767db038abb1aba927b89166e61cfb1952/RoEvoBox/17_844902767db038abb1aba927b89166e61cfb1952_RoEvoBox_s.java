 package pl.psnc.dl.wf4ever.portal.pages.ro.evo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.resource.JavaScriptResourceReference;
 import org.purl.wf4ever.rosrs.client.ResearchObject;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.components.LoadingCircle;
 import pl.psnc.dl.wf4ever.portal.events.RoEvolutionLoadedEvent;
 import pl.psnc.dl.wf4ever.portal.model.RoEvoNode;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * A panel for displaying the RO evolution surroundings.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 public class RoEvoBox extends EventPanel {
 
     /** id. */
     private static final long serialVersionUID = -3775797988389365540L;
 
     /** Logger. */
     @SuppressWarnings("unused")
     private static final Logger LOG = Logger.getLogger(RoEvoBox.class);
 
     /** a temporary panel shown while the evolution info is loaded. */
     private WebMarkupContainer tmp;
 
     /** All nodes in the visualization mapped to research objects they represent. */
    private Map<ResearchObject, RoEvoNode> allNodes;
 
     /** The RO for which the visualization is drawn. */
     private IModel<ResearchObject> researchObjectModel;
 
     /** A JS file for this panel. */
     private static final JavaScriptResourceReference JS_REFERENCE = new JavaScriptResourceReference(RoEvoBox.class,
             "RoEvoBox.js");
 
     /** next unassigned index. */
     private static int nextIndex;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param researchObjectModel
      *            The RO for which the visualization is drawn
      * @param eventBusModel
      *            event bus model
      */
     public RoEvoBox(String id, IModel<ResearchObject> researchObjectModel, IModel<EventBus> eventBusModel) {
         super(id, researchObjectModel, eventBusModel);
         this.researchObjectModel = researchObjectModel;
         setOutputMarkupPlaceholderTag(true);
     }
 
 
     /**
      * Reconstruct this panel.
      */
     protected void init() {
         this.removeAll();
         WebMarkupContainer live = new WebMarkupContainer("live");
         add(live);
         WebMarkupContainer snapshots = new WebMarkupContainer("snapshots");
         add(snapshots);
         WebMarkupContainer archived = new WebMarkupContainer("archived");
         add(archived);
 
         List<RoEvoNode> liveNodes = new ArrayList<>();
         List<RoEvoNode> snapshotNodes = new ArrayList<>();
         List<RoEvoNode> archivedNodes = new ArrayList<>();
         allNodes = new HashMap<>();
 
         if (!researchObjectModel.getObject().isEvolutionInformationLoaded()) {
             researchObjectModel.getObject().loadEvolutionInformation();
         }
         nextIndex = 0;
         switch (researchObjectModel.getObject().getEvoType()) {
             case LIVE:
                 drawForALiveRO(liveNodes, snapshotNodes, archivedNodes);
                 break;
             case SNAPSHOT:
             case ARCHIVE:
                 drawForASnapshot(liveNodes, snapshotNodes, archivedNodes);
                 break;
             default:
                 break;
         }
 
         final int dx = 80, ox = 20;
         fillLayer(live, "liveNodes", liveNodes, dx, ox);
         fillLayer(snapshots, "snapshotNodes", snapshotNodes, dx, ox);
         fillLayer(archived, "archivedNodes", archivedNodes, dx, ox);
     }
 
 
     /**
      * Draw the visualization for a snapshot or archive.
      * 
      * @param liveNodes
      *            nodes representing live ROs
      * @param snapshotNodes
      *            nodes representing snapshots
      * @param archivedNodes
      *            nodes representing archives
      */
     void drawForASnapshot(List<RoEvoNode> liveNodes, List<RoEvoNode> snapshotNodes, List<RoEvoNode> archivedNodes) {
         List<ResearchObject> preorder = new ArrayList<>();
         List<ResearchObject> postorder = new ArrayList<>();
         liveNodes.add(findOrCreateNode(researchObjectModel.getObject().getLiveRO()));
         visit(researchObjectModel.getObject(), preorder, postorder);
         for (ResearchObject ro : postorder) {
             switch (ro.getEvoType()) {
                 case SNAPSHOT:
                     snapshotNodes.add(findOrCreateNode(ro));
                     break;
                 case ARCHIVE:
                     archivedNodes.add(findOrCreateNode(ro));
                 default:
             }
         }
     }
 
 
     /**
      * Draw the visualization for a live RO.
      * 
      * @param liveNodes
      *            nodes representing live ROs
      * @param snapshotNodes
      *            nodes representing snapshots
      * @param archivedNodes
      *            nodes representing archives
      */
     void drawForALiveRO(List<RoEvoNode> liveNodes, List<RoEvoNode> snapshotNodes, List<RoEvoNode> archivedNodes) {
         List<ResearchObject> preorder = new ArrayList<>();
         List<ResearchObject> postorder = new ArrayList<>();
         Set<ResearchObject> snapshotsAndArchives = new HashSet<>();
         snapshotsAndArchives.addAll(researchObjectModel.getObject().getSnapshots());
         snapshotsAndArchives.addAll(researchObjectModel.getObject().getArchives());
         for (ResearchObject ro : snapshotsAndArchives) {
             if (!preorder.contains(ro)) {
                 visit(ro, preorder, postorder);
             }
         }
         liveNodes.add(findOrCreateNode(researchObjectModel.getObject()));
         for (ResearchObject ro : postorder) {
             switch (ro.getEvoType()) {
                 case SNAPSHOT:
                     snapshotNodes.add(findOrCreateNode(ro));
                     break;
                 case ARCHIVE:
                     archivedNodes.add(findOrCreateNode(ro));
                 default:
             }
         }
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         response.renderJavaScriptReference(JS_REFERENCE);
     }
 
 
     @Override
     protected void onConfigure() {
         super.onConfigure();
         if (!researchObjectModel.getObject().isEvolutionInformationLoaded()) {
             tmp = new LoadingCircle(getId(), "Loading...");
             this.replaceWith(tmp);
         }
     }
 
 
     /**
      * Replace the temporary panel with this one and redraw it.
      * 
      * @param event
      *            the trigger
      */
     @Subscribe
     public void onRoEvolutionLoaded(RoEvolutionLoadedEvent event) {
         if (tmp != null) {
             tmp.replaceWith(this);
             tmp = null;
         }
         init();
         event.getTarget().appendJavaScript(getDrawJavaScript());
         event.getTarget().add(this);
     }
 
 
     /**
      * Put the nodes on a layer.
      * 
      * @param layer
      *            the layer
      * @param id
      *            wicket id of the node prototype
      * @param nodes
      *            list of nodes to put
      * @param dx
      *            distance between node centers
      * @param ox
      *            offset of the first node
      */
     @SuppressWarnings("serial")
     private void fillLayer(WebMarkupContainer layer, String id, List<RoEvoNode> nodes, final int dx, final int ox) {
         if (!nodes.isEmpty()) {
             layer.add(new ListView<RoEvoNode>(id, nodes) {
 
                 @Override
                 protected void populateItem(ListItem<RoEvoNode> item) {
                     populateRoEvoNode(item, ox + dx * item.getModelObject().getIndex());
                 }
 
             });
         } else {
             layer.setVisible(false);
         }
     }
 
 
     /**
      * Find a node for the RO or create a new one.
      * 
      * @param ro
      *            the RO
      * @return a node
      */
     private RoEvoNode findOrCreateNode(ResearchObject ro) {
        RoEvoNode node = allNodes.containsKey(ro) ? allNodes.get(ro) : new RoEvoNode(ro);
        allNodes.put(ro, node);
         node.setIndex(nextIndex++);
         return node;
     }
 
 
     /**
      * Visit the evolution history tree.
      * 
      * @param ro
      *            the RO
      * @param preorder
      *            the preorder numbering of ROs
      * @param postorder
      *            the postorder numbering of ROs
      */
     private void visit(ResearchObject ro, List<ResearchObject> preorder, List<ResearchObject> postorder) {
         preorder.add(ro);
         if (!ro.isEvolutionInformationLoaded()) {
             ro.loadEvolutionInformation();
         }
         if (ro.getPreviousSnapshot() != null && !preorder.contains(ro.getPreviousSnapshot())) {
             visit(ro.getPreviousSnapshot(), preorder, postorder);
         }
         postorder.add(ro);
     }
 
 
     /**
      * Create the RO node for the visualization.
      * 
      * @param item
      *            list item containing the node
      * @param x
      *            the horizontal position of the node, in px
      */
     private void populateRoEvoNode(ListItem<RoEvoNode> item, int x) {
         RoEvoNode node = item.getModelObject();
         item.add(new ExternalLink("labelOrIdentifier", new PropertyModel<String>(node, "researchObject.uri.toString"),
                 new PropertyModel<>(node, "labelOrIdentifier")));
         StringBuilder cssClasses = new StringBuilder();
         if (researchObjectModel.getObject().equals(item.getModelObject().getResearchObject())) {
             cssClasses.append(" active");
         }
         item.add(new AttributeAppender("style", "left: " + x + "px;"));
         item.add(new AttributeAppender("class", cssClasses.toString()));
         item.add(new AttributeAppender("rel", "popover"));
         item.add(new AttributeAppender("data-content", node.getResearchObject().getUri()));
         if (node.isResearchObject()) {
             item.add(new AttributeAppender("data-original-title", "Research Object"));
         } else {
             item.add(new AttributeAppender("data-original-title", "An external resource"));
         }
         item.add(new WebMarkupContainer("roMark").setVisible(node.isResearchObject()));
         node.setComponent(item);
         item.setOutputMarkupId(true);
     }
 
 
     /**
      * Create a JavaScript code that connects 2 nodes.
      * 
      * @param source
      *            source node
      * @param target
      *            target node
      * @param label
      *            connection label
      * @return JavaScript code
      */
     protected String createConnection(RoEvoNode source, RoEvoNode target, String label) {
         StringBuilder sb = new StringBuilder();
         String connId = "conn" + Math.abs(new Random().nextInt());
         sb.append("var " + connId + " = instance.connect({");
         sb.append("source: '" + source.getComponent().getMarkupId() + "',");
         sb.append("target: '" + target.getComponent().getMarkupId() + "',");
         sb.append("overlays:[ [ 'Label', { label:'" + label + "', id: 'label', cssClass : 'evolabel' } ] ]");
         sb.append("});");
         sb.append(connId + ".bind('mouseenter', function (c) { c.showOverlay('label'); });");
         sb.append(connId + ".bind('mouseexit', function (c) { c.hideOverlay('label'); });");
         sb.append(connId + ".hideOverlay('label');");
         return sb.toString();
     }
 
 
     /**
      * Generate the JavaScript code that will draw the arrows between the nodes.
      * 
      * @return the JavaScript code
      */
     private String getDrawJavaScript() {
         final StringBuilder sb = new StringBuilder();
         sb.append("function drawArrows() {");
         sb.append("jsPlumb.ready(function() {");
         sb.append("initRoEvo(jsPlumb);");
         sb.append("var instance = jsPlumb.getInstance();");
         sb.append("initRoEvo(instance);");
         for (RoEvoNode node : allNodes.values()) {
             if (node.getResearchObject().getLiveRO() != null) {
                RoEvoNode live = allNodes.get(node.getResearchObject().getLiveRO());
                 sb.append(createConnection(node, live, "Has live RO"));
             }
             if (node.getResearchObject().getPreviousSnapshot() != null) {
                RoEvoNode snapshot = allNodes.get(node.getResearchObject().getPreviousSnapshot());
                 sb.append(createConnection(node, snapshot, "Previous snapshot"));
             }
         }
         sb.append("});");
         sb.append("}");
 
         sb.append("if ($(\"#history\").is(\":visible\")) { drawArrows(); } else {");
         sb.append("$('a[data-toggle=\"tab\"][href=\"#history\"]').on('shown', function (e) { drawArrows(); $('a[data-toggle=\"tab\"][href=\"#history\"]').off('shown'); }); }");
 
         return sb.toString();
     }
 }
