 package se.findout.tempo.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.vaadin.gwtgraphics.client.DrawingArea;
 import org.vaadin.gwtgraphics.client.Line;
 import org.vaadin.gwtgraphics.client.shape.Circle;
 import org.vaadin.gwtgraphics.client.shape.Text;
 
 import se.findout.tempo.client.VersionModel.VersionChangeEvent;
 import se.findout.tempo.client.VersionModel.VersionChangeListener;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 
 public class VersionView extends FlowPanel implements VersionChangeListener {
 	private DrawingArea drawingArea;
 	private VersionTreeRenderer versionTreeRenderer;
 	private Map<Version, Circle> versionToCircle = new HashMap<Version, Circle>();
 	private Version selectedVersion = null;
 	private Circle selectedVersionCircle;
 	private List<SelectionChangeListener> selectionChangeListeners = new ArrayList<SelectionChangeListener>();
 
 	public VersionView(VersionModel versionModel) {
 		drawingArea = new DrawingArea(2000, 1000);
 		drawingArea.getElement().getStyle()
 				.setProperty("border", "3px solid #e7e7e7");
 		add(new ScrollPanel(drawingArea));
 		
 		
 		versionTreeRenderer = new VersionTreeRenderer(versionModel, new VersionTreeRenderer.ShapeFactory() {
 			
 			@Override
 			public void addVersion(Version version, int x, int y) {
 				VersionClickHandler handler = new VersionClickHandler(version);
 				
 				Circle circle = new Circle(40 + x, 40 + y, 20);
 				circle.setStrokeWidth(2);
 				circle.addClickHandler(handler);
 				circle.setTitle(version.getChange() != null ? version.getChange().getDescription() : "Initial version");
 				drawingArea.add(circle);
 				versionToCircle.put(version, circle);
 				
 				Text text = new Text(0, 0, version.getName());
 				text.setFillColor("black");
 				text.setFontSize(14);
 				text.setX(40 + x - text.getTextWidth() / 2);
 				text.setY(40 + y + (int)(text.getTextHeight() / 2.7));
 				text.addClickHandler(handler);
 				drawingArea.add(text);
 			}
 			
 			@Override
 			public void addRelation(int x0, int y0, int x1, int y1) {
 				Line line = new Line(40 + x0, 40 + y0, 40 + x1, 40 + y1);
 				line.setStrokeWidth(2);
 				drawingArea.insert(line, 0);
 			}
 		});
 		versionTreeRenderer.setxScale(60);
 		versionTreeRenderer.setyScale(60);
 		
 		versionTreeRenderer.render();
 		
 		versionModel.addVersionChangeListener(this);
 		
 	}
 
 	@Override
 	public void versionChanged(VersionChangeEvent event) {
 		drawingArea.clear();
 		selectedVersionCircle = null;
 		versionTreeRenderer.render();
 	}
 
 	private final class VersionClickHandler implements ClickHandler {
 		private Version version;
 
 		public VersionClickHandler(Version version) {
 			this.version = version;
 		}
 
 		@Override
 		public void onClick(ClickEvent event) {
 			selectVersion(version);
 		}
 	}
 	
 	public void selectVersion(Version version) {
 		System.out.println("VersionView.selectVersion(" + version + ")");
 		if (version != selectedVersion) {
 			if (version == null) {
 				if (selectedVersionCircle != null) {
 					drawingArea.remove(selectedVersionCircle);
 				}
 			} else {
 				Circle versionCircle = versionToCircle.get(version);
 				if (selectedVersionCircle != null) {
 					selectedVersionCircle.setX(versionCircle.getX());
 					selectedVersionCircle.setY(versionCircle.getY());
 				} else {
 					Circle selectionCircle = new Circle(versionCircle.getX(), versionCircle.getY(), versionCircle.getRadius() + 4);
 					selectionCircle.setFillOpacity(0.0);
 					selectionCircle.setStrokeWidth(3);
 					selectedVersionCircle = selectionCircle;
 					drawingArea.add(selectionCircle);
 				}
 				Version prevSelectedVersion = selectedVersion;
 				selectedVersion = version;
 				for (SelectionChangeListener listener : selectionChangeListeners) {
 					listener.selectionChanged(new SelectionChangedEvent(this, prevSelectedVersion, version));
 				}
 			}
 		}
 	}
 	
 	public void addSelectionChangeListener(SelectionChangeListener listener) {
 		selectionChangeListeners.add(listener);
 	}
 
 	public class SelectionChangedEvent {
 		private final VersionView versionView;
 		private final Version prevSelectedVersion;
 		private final Version newSelectedVersion;
 
 		public SelectionChangedEvent(VersionView versionView,
 				Version prevSelectedVersion, Version newSelectedVersion) {
 					this.versionView = versionView;
 					this.prevSelectedVersion = prevSelectedVersion;
 					this.newSelectedVersion = newSelectedVersion;
 		}
 
 		public VersionView getVersionsView() {
 			return versionView;
 		}
 
 		public Version getPrevSelectedVersion() {
 			return prevSelectedVersion;
 		}
 
 		public Version getNewSelectedVersion() {
 			return newSelectedVersion;
 		}
 		
 	}
 
 	public interface SelectionChangeListener {
 		void selectionChanged(SelectionChangedEvent event);
 	}
 
 	public Version getSelectedVersion() {
 		return selectedVersion;
 	}
 
 }
