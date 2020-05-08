 package controllers;
 
 import infrastructure.IProjectContext;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import jgraph.extensions.CustomGraph;
 import models.domain.DomainDiagram;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import persistence.IGraphPersistenceService;
 import persistence.IXmlFileManager;
 import persistence.IXmlManager;
 import views.IDomainDiagramView;
 
 import com.mxgraph.model.mxCell;
 import com.mxgraph.util.mxEvent;
 import com.mxgraph.util.mxEventObject;
 import com.mxgraph.util.mxEventSource.mxIEventListener;
 import com.mxgraph.view.mxGraph;
 
 public class DomainDiagramController extends BaseController implements
 		IDomainDiagramController, mxIEventListener {
 
 	public static class CellConstants {
 		public static final String ClassPrefix = "Class";
 	}
 
 	// private enum Operations {
 	// None,
 	// // TODO: For now we don't have any operation
 	// // CreateEntity,
 	// // UpdateEntity,
 	// // CreateRelationship,
 	// // UpdateRelationship,
 	// // CreateHierarchy,
 	// // UpdateHierarchy
 	// }
 
 	private CustomGraph graph;
 	private Map<String, mxCell> classCells;
 	// private IEntityControllerFactory entityControllerFactory;
 	// private Entity pendingEntity;
 	private DomainDiagram diagram;
 	private IXmlFileManager xmlFileManager;
 	private IXmlManager<DomainDiagram> diagramXmlManager;
 	private IDomainDiagramView diagramView;
 	private List<mxCell> selectedCells;
 	private Point dragStartPoint;
 	private IGraphPersistenceService graphPersistenceService;
 
 	// private Pattern regex;
 	// private Operations currentOperation;
 
 	public DomainDiagramController(IProjectContext projectContext,
 			IDomainDiagramView diagramView, IXmlFileManager xmlFileManager,
 			IXmlManager<DomainDiagram> diagramXmlManager,
 			IGraphPersistenceService graphPersistenceService) {
 		super(projectContext);
 		// this.currentOperation = Operations.None;
 		this.diagram = new DomainDiagram();
 		this.selectedCells = new ArrayList<mxCell>();
 		this.graph = new CustomGraph();
 
 		this.graph.getSelectionModel().addListener(mxEvent.CHANGE, this);
 
 		this.classCells = new HashMap<String, mxCell>();
 		this.xmlFileManager = xmlFileManager;
 		this.diagramXmlManager = diagramXmlManager;
 		this.diagramView = diagramView;
 		this.diagramView.setController(this);
 		this.graphPersistenceService = graphPersistenceService;
 
 		// this.regex = Pattern
 		// .compile("(?:"
 		// + CellConstants.ClassPrefix
 		// + ")"
 		// +
 		// "([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}).*");
 	}
 
 	@Override
 	public IDomainDiagramView getView() {
 		return diagramView;
 	}
 
 	@Override
 	public mxGraph getGraph() {
 		return graph;
 	}
 
 	public mxCell getClassCell(String id) {
 		return classCells.get(CellConstants.ClassPrefix + id);
 	}
 
 	public void save() throws ParserConfigurationException {
		Document document = this.xmlFileManager.createDocument();
		Element element = this.diagramXmlManager.getElementFromItem(
				this.diagram, document);

		document.appendChild(element);
		this.xmlFileManager.write(document, this.getDomainFilePath());
 
 		this.graphPersistenceService.save(this.getGraphDomainFilePath(),
 				this.graph);
 
 	}
 
 	private String getDomainFilePath() {
 		return this.projectContext.getDataDirectory() + "/"
 				+ this.diagram.getName() + "-domain.xml";
 	}
 
 	private String getGraphDomainFilePath() {
 		return this.projectContext.getDataDirectory() + "/"
 				+ this.diagram.getName() + "-graph_domain.xml";
 	}
 
 	public void openDomainDiagram(String path) throws Exception {
 		Document document = this.xmlFileManager.read(path);
 		Element element = document.getDocumentElement();
 		this.diagram = this.diagramXmlManager.getItemFromXmlElement(element);
 	}
 
 	@Override
 	public void load(DomainDiagram diagram) {
 
 		this.diagram = diagram;
 		String fileName = this.getGraphDomainFilePath();
 		this.graphPersistenceService.load(fileName, this.graph);
 
 		Pattern classRegexp = Pattern.compile(CellConstants.ClassPrefix);
 
 		for (Object o : this.graph.getChildCells(this.graph.getDefaultParent())) {
 			mxCell cell = (mxCell) o;
 
 			Matcher matcher = classRegexp.matcher(cell.getId());
 			if (matcher.find()) {
 				this.classCells.put(cell.getId(), cell);
 				continue;
 			}
 		}
 	}
 
 	@Override
 	public void invoke(Object arg0, mxEventObject arg1) {
 		// JGraph has a bug "removed" are those added to the selection. "added"
 		// are those that were removed from the selection
 		List<?> added = arg1.getProperties().containsKey("removed") ? (List<?>) arg1
 				.getProperties().get("removed") : null;
 		List<?> removed = arg1.getProperties().containsKey("added") ? (List<?>) arg1
 				.getProperties().get("added") : null;
 
 		if (removed != null) {
 			for (Object cell : removed) {
 				this.selectedCells.remove((mxCell) cell);
 			}
 		}
 
 		if (added != null) {
 			for (Object cell : added) {
 				if (this.classCells.containsValue(cell)) {
 					this.selectedCells.add((mxCell) cell);
 				}
 			}
 		}
 	}
 
 	// private String getElementUUID(String elementId) {
 	// Matcher matcher = this.regex.matcher(elementId);
 	// boolean matchFound = matcher.find();
 	//
 	// if (matchFound) {
 	// return matcher.group(1);
 	// }
 	// return null;
 	// }
 
 	public void handleDrop(Point end) {
 		if (this.dragStartPoint != null) {
 			double dx = end.getX() - this.dragStartPoint.getX();
 			double dy = end.getY() - this.dragStartPoint.getY();
 
 			List<mxCell> attributesCellsToMove = new ArrayList<mxCell>();
 
 			// FIXME: I think we need the attributes cells....
 			// for (mxCell cell : this.selectedCells) {
 			// String elementId = this.getElementUUID(cell.getId());
 			// for (String attributeKey : this.attributeCells.keySet()) {
 			// if (elementId != null) {
 			// if (attributeKey
 			// .startsWith(CellConstants.AttributePrefix
 			// + elementId)) {
 			// mxCell attributeCell = this.attributeCells
 			// .get(attributeKey);
 			// attributesCellsToMove.add(attributeCell);
 			// }
 			// }
 			// }
 			// }
 
 			if (attributesCellsToMove.size() == 0) {
 				return;
 			}
 
 			this.graph.getModel().beginUpdate();
 
 			try {
 				this.graph.moveCells(attributesCellsToMove.toArray(), dx, dy);
 			} finally {
 				this.graph.getModel().endUpdate();
 			}
 
 			this.dragStartPoint = null;
 		}
 	}
 
 	public void handleDragStart(Point start) {
 		if (this.dragStartPoint == null && this.selectedCells.size() != 0) {
 			this.dragStartPoint = new Point(start);
 		}
 	}
 
 	@Override
 	public DomainDiagram getDiagram() {
 		return diagram;
 	}
 
 	// @Override
 	// public Iterable<DomainClass> getAvailableClasses() {
 	// Iterable<DomainClass> entities =
 	// this.projectContext.getAllDomainClasses();
 	// List<DomainClass> entityList = IterableExtensions.getListOf(entities);
 	//
 	// for (DomainClass entity : this.diagram.getDomainClasses()) {
 	// entityList.remove(entity);
 	// }
 	//
 	// return entityList;
 	// }
 	//
 }
