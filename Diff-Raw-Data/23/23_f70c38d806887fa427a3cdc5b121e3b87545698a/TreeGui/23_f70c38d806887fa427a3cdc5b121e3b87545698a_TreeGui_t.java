 package com.joshondesign.treegui;
 
 import com.joshondesign.treegui.actions.JAction;
 import com.joshondesign.treegui.docmodel.Layer;
 import com.joshondesign.treegui.docmodel.Page;
 import com.joshondesign.treegui.docmodel.SketchDocument;
 import com.joshondesign.treegui.docmodel.SketchNode;
 import com.joshondesign.treegui.model.TreeNode;
 import com.joshondesign.treegui.model.TreeNodeListView;
 import com.joshondesign.treegui.modes.amino.*;
 import com.joshondesign.xml.Doc;
 import com.joshondesign.xml.Elem;
 import com.joshondesign.xml.XMLParser;
 import java.awt.geom.Point2D;
 import java.io.File;
 import org.joshy.gfx.Core;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.draw.Font;
 import org.joshy.gfx.draw.GFX;
 import org.joshy.gfx.event.*;
 import org.joshy.gfx.node.Bounds;
 import org.joshy.gfx.node.Node;
 import org.joshy.gfx.node.Parent;
 import org.joshy.gfx.node.control.Control;
 import org.joshy.gfx.node.control.ListView;
 import org.joshy.gfx.stage.Stage;
 import org.joshy.gfx.util.u;
 
 public class TreeGui implements Runnable {
 
     public static void main(String ... args) throws Exception {
         Core.init();
         Core.getShared().defer(new TreeGui());
     }
 
     public void run() {
         final TreeNode<Mode> modes = initModes();
         Mode mode = modes.get(0);
         final SketchDocument doc = initDoc();
         Stage stage = Stage.createStage();
         stage.setWidth(800);
         stage.setHeight(600);
 
         EventBus.getSystem().addListener(SystemMenuEvent.Quit, new Callback<Event>() {
             public void call(Event event) throws Exception {
                 System.exit(0);
             }
         });
 
         Control rootControl;
 
         try {
             File file = new File("/Users/josh/projects/Leo/nodes.xml");
             Doc xml = XMLParser.parse(file);
 
             u.p("parsed the xml: " + xml);
             Elem root = xml.xpathElement("/xml/panel");
             rootControl = new ControlLoader().processControls(root);
             stage.setContent(rootControl);
 
             PropsView propsView = (PropsView) find("propsview", rootControl);
 
             //hook up the canvas
             final Canvas canvas = (Canvas) find("canvas",rootControl);
             canvas.setTarget(doc.get(0).get(0));
             canvas.setPropsView(propsView);
 
 
             //hoook up the props view
             propsView.setPropFilter(new PropsView.PropFilter() {
                 public boolean include(Object obj, String name) {
                     return true;
                 }
             });
             propsView.setSelection(doc.get(0).get(0).get(0));
             propsView.onUpdate(new Callback<Void>() {
                 public void call(Void aVoid) throws Exception {
                     canvas.setLayoutDirty();
                 }
             });
 
 
             //hook up the actions toolbar
             TreeNode<JAction> actions = (TreeNode<JAction>)mode.get(0);
             HTMLBindingExport exp = new HTMLBindingExport();
             exp.canvas = canvas;
             exp.page = doc.get(0);
             actions.add(exp);
             ToolbarListView toolbar = (ToolbarListView) find("toolbar", rootControl);
             toolbar.setModel(actions);
 
             //hook up the symbols view
 
             final TreeNodeListView symbolsView = (TreeNodeListView) find("symbolsview", rootControl);
             TreeNode<SketchNode> symbols = mode.get(1);
             symbolsView.setTreeNodeModel(symbols);
             symbolsView.setRenderer(new ListView.ItemRenderer<TreeNode>() {
                 public void draw(GFX gfx, ListView listView, TreeNode treeNode, int index, double x, double y, double w, double h) {
                     if(treeNode == null) return;
                     SketchNode node = (SketchNode) treeNode;
                     gfx.translate(x, y);
                     node.draw(gfx);
                     gfx.setPaint(FlatColor.WHITE);
 
                     gfx.fillRect(0, h-20, w, 20);
                     gfx.setPaint(FlatColor.BLACK);
                     gfx.drawText(node.getId(), Font.DEFAULT,5,h-4);
                     gfx.translate(-x,-y);
                     if(listView.getSelectedIndex() == index) {
                         gfx.setPaint(new FlatColor(1.0,0,0,0.3));
                         gfx.fillRect(x,y,w,h);
                     }
                     gfx.setPaint(FlatColor.BLACK);
                     gfx.drawRect(x,y,w,h);
                 }
             });
             EventBus.getSystem().addListener(symbolsView, MouseEvent.MouseAll, new Callback<MouseEvent>() {
                 public boolean created;
                 public SketchNode dupe;
                 public double prevx;
 
                 public void call(MouseEvent event) throws Exception {
                     if(event.getType() == MouseEvent.MouseDragged) {
                         Point2D pt = event.getPointInNodeCoords(canvas);
                         if(created && dupe != null) {
                             //pt = canvas.transformToCanvas(pt);
                             Bounds b = dupe.getInputBounds();
                             dupe.setTranslateX(pt.getX()-b.getWidth()/2);
                             dupe.setTranslateY(pt.getY()-b.getHeight()/2);
                             //context.redraw();
                             canvas.setLayoutDirty();
                         }
                         if(event.getX() < 0 && prevx >= 0 && !created) {
                             created = true;
                             if(symbolsView.getSelectedIndex() < 0) return;
                             SketchNode node = (SketchNode) symbolsView.getModel().get(symbolsView.getSelectedIndex());
                             SketchDocument sd = doc;//context.getDocument();
                             dupe = node.duplicate(null);
                             Bounds b = dupe.getInputBounds();
                             //sd.getCurrentPage().add(dupe);
                             sd.get(0).get(0).add(dupe);
                             //Point2D pt = event.getPointInNodeCoords(context.getCanvas());
                             //pt = context.getSketchCanvas().transformToCanvas(pt);
                             dupe.setTranslateX(pt.getX() - b.getWidth() / 2);
                             dupe.setTranslateY(pt.getY() - b.getHeight() / 2);
                             canvas.redraw();
                         }
                         prevx = event.getX();
                     }
                     if(event.getType() == MouseEvent.MouseReleased) {
                         if(created) {
                             canvas.clearSelection();
                             canvas.addToSelection(dupe);
                             dupe = null;
                             created = false;
                             prevx = 0;
                         }
                         canvas.redraw();
                     }
 
                 }
             });
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     private TreeNode<Mode> initModes() {
         TreeNode<Mode> modes = new TreeNode<Mode>();
         Mode amino = new Mode();
         amino.setId("com.joshondesign.modes.aminojs");
         modes.add(amino);
 
         TreeNode<JAction> actions = new TreeNode<JAction>();
         actions.add(new JAction() {
             @Override
             public void execute() {
             }
             @Override
             public String getShortName() {
                 return "Save XML";
             }
         });
         amino.add(actions);
 
 
         TreeNode<SketchNode> symbols = new TreeNode<SketchNode>();
         Rect rect = new Rect();
         rect.setId("Rect");
         symbols.add(rect);
         com.joshondesign.treegui.modes.amino.Button button = new com.joshondesign.treegui.modes.amino.Button();
         button.setId("Button");
         symbols.add(button);
         com.joshondesign.treegui.modes.amino.Slider slider = new Slider();
         slider.setId("Slider");
         symbols.add(slider);
 
         StringListModel stringList = new StringListModel();
         stringList.setId("StringList");
         symbols.add(stringList);
 
         com.joshondesign.treegui.modes.amino.ListView lv = new com.joshondesign.treegui.modes.amino.ListView();
         lv.setId("ListView");
         symbols.add(lv);
 
 
         FlickrQuery fq = new FlickrQuery();
         fq.setId("FlickrQuery");
         symbols.add(fq);
 
         Image image = new Image();
         image.setId("image");
         symbols.add(image);
 
         Textbox tb = new Textbox();
         tb.setId("textbox");
         symbols.add(tb);
 
         amino.add(symbols);
 
         return modes;
     }
 
     private SketchDocument initDoc() {
 
 
         SketchDocument doc = new SketchDocument();
         Layer layer = new Layer();
         layer.add(new Rect().setFill(FlatColor.GREEN).setWidth(50).setHeight(50));
         Page page = new Page();
         page.add(layer);
         doc.add(page);
 
 
         return doc;
     }
 
     private Node find(String name, Control rootControl) {
         if(name.equals(rootControl.getId())) return rootControl;
         if(rootControl instanceof Parent) {
             Parent parent = (Parent) rootControl;
             for(Node node : parent.children()) {
                 if(node instanceof Control) {
                     Node nd = find(name, (Control) node);
                     if(nd != null) return nd;
                 }
             }
         }
         return null;
     }
 
 }
