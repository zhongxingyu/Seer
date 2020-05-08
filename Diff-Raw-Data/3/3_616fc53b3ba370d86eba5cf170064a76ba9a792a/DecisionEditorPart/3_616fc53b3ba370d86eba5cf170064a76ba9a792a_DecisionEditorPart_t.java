 package me.bahadir.bsemantix.parts;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.ValidationEvent;
 import javax.xml.bind.ValidationEventHandler;
 
 import me.bahadir.bsemantix.ProConfig;
 import me.bahadir.bsemantix.S;
 import me.bahadir.bsemantix.ccortex.CCortex;
 import me.bahadir.bsemantix.ccortex.CCortex.DectreeDocument;
 import me.bahadir.bsemantix.ngraph.NeuralGraph;
 import me.bahadir.bsemantix.ngraph.SphereNode;
 import me.bahadir.bsemantix.ngraph.SynapticEdge;
 import me.bahadir.bsemantix.ngraph.dtree.Answer.AnswerData;
 import me.bahadir.bsemantix.ngraph.dtree.DecisionTree;
 import me.bahadir.bsemantix.ngraph.dtree.DecisionTree.DecisionTreeData;
 import me.bahadir.bsemantix.ngraph.dtree.Leaf.LeafData;
 import me.bahadir.bsemantix.ngraph.dtree.Leaf;
 import me.bahadir.bsemantix.ngraph.dtree.Question;
 import me.bahadir.bsemantix.semantic.SampleOM;
 
 import org.eclipse.e4.core.di.annotations.Optional;
 import org.eclipse.e4.ui.di.Focus;
 import org.eclipse.e4.ui.di.UIEventTopic;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.internal.win32.LRESULT;
 import org.eclipse.swt.internal.win32.TCHAR;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Sash;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.zest.core.widgets.GraphConnection;
 import org.eclipse.zest.core.widgets.GraphNode;
 
 import com.hp.hpl.jena.ontology.OntModel;
 
 import org.eclipse.wb.swt.ResourceManager;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 public class DecisionEditorPart {
 	
 	protected static Logger log = Logger.getLogger(DecisionEditorPart.class
 			.getSimpleName());
 	
 	public static final String TOPIC_TREE_ITEM_SELECTED = "DT_ITEM_SELECTED";
 	public static final String TOPIC_TREE_ITEM_UNSELECTED = "DT_ITEM_UNSELECTED";
 	public static final String TOPIC_EDIT_SYNAPTIC_EDGE = "DT_EDIT_SYNAPTIC_DECISION";
 	private Shell shell;
 	private DecisionTree activeTree;
 	private Menu addLeafMenu;
 
 	private ToolBar bar;
 	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 
 	private Composite parent;
 
 	private enum MouseClickType {
 		DOUBLE, RIGHT
 	};
 
 	@Inject
 	public DecisionEditorPart() {
 		// TODO Your code here
 	}
 
 	
 	@Inject @Optional
 	void editSynapticTree(@UIEventTopic(TOPIC_EDIT_SYNAPTIC_EDGE) SynapticEdge synapticEdge) {
 		log.info("loading synaptic edge decision tree");
 		
 		DecisionTree.DecisionTreeData data = new DecisionTreeData(
 				synapticEdge.getSourceVertex().getOntClass(), 
 				synapticEdge.getProperty(),
 				synapticEdge.getTargetVertex().getOntClass());
 		DecisionTree tree = new DecisionTree(parent, synapticEdge.getNg(), data);
 		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));;
 		// autmatically set this to root
 		tree.addQuestion("Ask your question here..", "Question");
 		
 		
 		setActiveTree(tree);
 		
 		
 	}
 	
 	@PostConstruct
 	public void postConstruct(final Composite parent, final Shell shell) {
 		this.shell = shell;
 		this.parent = parent;
 		parent.setLayout(new GridLayout(1, false));
 
 		bar = new ToolBar(parent, SWT.FLAT);
 		bar.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 
 	
 
 		final ToolItem tbAddLeaf = new ToolItem(bar, SWT.DROP_DOWN);
 
 		tbAddLeaf.setText("Add Leaf");
 		tbAddLeaf.setImage(ResourceManager.getPluginImage("me.bahadir.bsemantix", "icons/leaf.png"));
 		tbAddLeaf.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 
 				ToolItem item = (ToolItem) e.widget;
 				Rectangle rect = item.getBounds();
 				Point pt = item.getParent()
 						.toDisplay(new Point(rect.x, rect.y));
 				addLeafMenu.setLocation(pt.x, pt.y + rect.height);
 				addLeafMenu.setVisible(true);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		final ToolItem tbAddQuestionNode = new ToolItem(bar, SWT.PUSH);
 		tbAddQuestionNode.setText("Add Question");
 		tbAddQuestionNode.setImage(ResourceManager.getPluginImage("me.bahadir.bsemantix", "icons/question.png"));
 		tbAddQuestionNode.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				addQuestion();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		
 		ToolItem toolItem = new ToolItem(bar, SWT.SEPARATOR);
 
 		final ToolItem tbExportXML = new ToolItem(bar, SWT.PUSH);
 		tbExportXML.setText("Export");
 		tbExportXML.setImage(ResourceManager.getPluginImage("me.bahadir.bsemantix", "icons/xml.png"));
 		tbExportXML.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				exportXML();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		final ToolItem tbImportXML = new ToolItem(bar, SWT.PUSH);
 		tbImportXML.setText("Import");
 		tbImportXML.setImage(ResourceManager.getPluginImage("me.bahadir.bsemantix", "icons/xml.png"));
 		tbImportXML.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				importXML();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 		
 
 		
 		
 
 		
 
 		bar.pack();
 		
 		ToolItem toolItem_1 = new ToolItem(bar, SWT.SEPARATOR);
 		
 		ToolItem tbCommit = new ToolItem(bar, SWT.NONE);
 		tbCommit.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				commit2CCortex();
 			}
 		});
 		tbCommit.setImage(ResourceManager.getPluginImage("me.bahadir.bsemantix", "icons/commit_db.png"));
 		tbCommit.setText("Commit");
 
 
 
 	}
 
 	protected void commit2CCortex() {
 		
 		CCortex ccortex = S.getStandartCCortex();
 		
 		ccortex.store(new DectreeDocument(activeTree));
 		
 		
 		
 	}
 
 	public void onNodeMouseClick(GraphNode node, MouseClickType clickType) {
 		switch(clickType) {
 		case DOUBLE:
 			break;
 		case RIGHT:
 			break;
 		default:
 			break;
 		}
 	}
 
 	public void onConnectionMouseClick(GraphConnection conn,
 			MouseClickType clickType) {
 		System.out.println(conn);
 	}
 
 	private void exportXML() {
 		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
 		dialog.setFilterExtensions(new String[] { "*.xml" });
 		dialog.setOverwrite(true);
 
 		String path = dialog.open();
 		if (path == null)
 			return;
 
 		try {
 			File file = new File(path);
 			if (file.exists() && !dialog.getOverwrite()) {
 				return;
 			}
 
 			activeTree.saveXML(new FileOutputStream(file));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		//
 
 	}
 
 	private void setActiveTree(DecisionTree decTree) {
		//reset();
		if(activeTree != null) activeTree.dispose();
 		this.activeTree = decTree;
 		activeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		activeTree.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Object o = activeTree.getSelection().size() < 1 ? null : activeTree.getSelection().get(0);
 		
 					S.broker.post(TOPIC_TREE_ITEM_SELECTED, o);
 			
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		activeTree.addMouseListener(new MouseListener() {
 
 			@Override
 			public void mouseUp(MouseEvent e) {
 				DecisionTree dec = (DecisionTree) e.getSource();
 				if (dec.getSelection().size() < 1)
 					return;
 				Object o = dec.getSelection().get(0);
 
 				if (o == null)
 					return;
 				if (e.button == SWT.BUTTON2) {
 					if (o instanceof GraphNode) {
 
 						onNodeMouseClick((GraphNode) o, MouseClickType.RIGHT);
 
 					} else if (o instanceof GraphConnection) {
 						onConnectionMouseClick((GraphConnection) o,
 								MouseClickType.RIGHT);
 					}
 				}
 
 			}
 
 			@Override
 			public void mouseDown(MouseEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void mouseDoubleClick(MouseEvent e) {
 				DecisionTree dec = (DecisionTree) e.getSource();
 				if (dec.getSelection().size() < 1)
 					return;
 				Object o = dec.getSelection().get(0);
 
 				if (o == null)
 					return;
 				if (o instanceof GraphNode) {
 					onNodeMouseClick((GraphNode) o, MouseClickType.DOUBLE);
 
 				} else if (o instanceof GraphConnection) {
 					onConnectionMouseClick((GraphConnection) o,
 							MouseClickType.DOUBLE);
 				}
 			}
 		});
 		generateMenus(bar);
 		
 		parent.layout(true, true);
 		//bench.pack();
 	}
 	
 	private void importXML() {
 
 		FileDialog dialog = new FileDialog(shell);
 		dialog.setFilterExtensions(new String[] { "*.xml" });
 		String path = dialog.open();
 
 		if (path == null)
 			return;
 
 		// S.showErrorDialog("Hata", new Exception("yalan dolan hep"), shell);
 		try {
 			File file = new File(path);
 			JAXBContext jaxbContext = JAXBContext
 					.newInstance(DecisionTreeData.class);
 
 			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
 			jaxbUnmarshaller.setEventHandler(new ValidationEventHandler() {
 
 				@Override
 				public boolean handleEvent(ValidationEvent event) {
 					switch (event.getSeverity()) {
 					case ValidationEvent.FATAL_ERROR:
 						log.severe("Can not read decision xml file. Please check format..");
 						return true;
 					}
 					return false;
 				}
 			});
 			DecisionTreeData dData = (DecisionTreeData) jaxbUnmarshaller
 					.unmarshal(file);
 			reset();
 			activeTree.loadData(dData);
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	private void reset() {
 		if(activeTree == null) return;
 		activeTree.disposeChildren();
 
 		// Dispose leaf menu
 		while (addLeafMenu.getItemCount() > 0) {
 			addLeafMenu.getItem(0).dispose();
 		}
 
 	}
 
 	protected void addQuestion() {
 		// TODO Auto-generated method stub
 		int selectSize = activeTree.getSelection().size();
 		if (selectSize < 1) {
 			log.warning("You must select a question node");
 			return;
 		}
 
 		Object object = activeTree.getSelection().get(selectSize - 1);
 		if (object instanceof Question) {
 			Question source = (Question) object;
 			Question target = activeTree.addQuestion("<question>");
 			activeTree.addAnswer(source, target, new AnswerData("<answer>",
 					"<fact>"));
 		} else {
 			log.warning("You must select a question node");
 
 		}
 	}
 
 	private void addLeaf(LeafData data) {
 
 		int selectSize = activeTree.getSelection().size();
 		if (selectSize < 1) {
 			log.warning("You must select a question node");
 			return;
 		}
 
 		Object object = activeTree.getSelection().get(selectSize - 1);
 		if (object instanceof Question) {
 			Question source = (Question) object;
 			Leaf targetLeaf;
 			if(data == null) {
 				targetLeaf = Leaf.createBlockLeaf(activeTree);
 			} else {
 				log.info(data.getOutputUri());
 				if(source.getAnswerDataByTargetURI(data.getOutputUri()) == null) {
 					targetLeaf =  new Leaf(activeTree, data);
 				} else {
 					log.warning("This question already have answer for that leaf node. Consider using synoynms.");
 					return;
 				}
 			}
 			
 			 
 
 			if (!activeTree.hasDirectedConnection(source, targetLeaf)) {
 
 				activeTree.addAnswer(source, targetLeaf, new AnswerData(
 						"<answer>", "<fact>"));
 				onItemAdded();
 			} else {
 				log.warning("these nodes already connected");
 			}
 
 		} else {
 			log.warning("You must select a question node");
 
 		}
 		// if(leaf == null) leaf = activeTree.createBlockLeaf();
 
 	}
 
 	private void onItemAdded() {
 		activeTree.applyLayout();
 	}
 
 	private void generateMenus(ToolBar bar) {
 		addLeafMenu = new Menu(bar);
 
 		MenuItem item = new MenuItem(addLeafMenu, SWT.None);
 		item.setText("Block leaf");
 		item.setImage(S.getImage("icons/leaf-red.png"));
 		item.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 				addLeaf(null);
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		if (activeTree.getOutputs().size() > 0) {
 			MenuItem seperator = new MenuItem(addLeafMenu, SWT.SEPARATOR);
 
 		}
 		for (final LeafData leafData : activeTree.getOutputs().values()) {
 			MenuItem leafItem = new MenuItem(addLeafMenu, SWT.None);
 			leafItem.setText(leafData.getText());
 			leafItem.setImage(S.getImage("icons/leaf.png"));
 			leafItem.addSelectionListener(new SelectionListener() {
 
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					// TODO Auto-generated method stub
 					addLeaf(leafData);
 				}
 
 				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 					// TODO Auto-generated method stub
 
 				}
 			});
 		}
 
 	}
 
 	private DecisionTree sampleTree(Composite parent) {
 //
 //		OntModel om = SampleOM.getOntologyModel();
 //		NeuralGraph ng = new NeuralGraph();
 //
 //		SphereNode sn1 = new SphereNode(S.unitVectorX);
 //		sn1.setOntClass(om.getOntClass("bx:Animal"));
 //		SphereNode sn2 = new SphereNode(S.unitVectorY);
 //		sn2.setOntClass(om.getOntClass("bx:Bird"));
 //		ng.addVertex(sn1);
 //		ng.addVertex(sn2);
 //
 //		
 //		
 //		SashForm sf = new SashForm(parent, SWT.VERTICAL);
 //		
 //		List<String> targets = new LinkedList<>();
 //		targets.add(sn2.getOntClass().getURI());
 //		DecisionTree decTree = new DecisionTree(parent, ng,
 //				new DecisionTreeData(sn1.getOntClass().getURI(), "http://test/tov#conna", targets));
 //		decTree.setLayout(new FillLayout(SWT.HORIZONTAL));
 //		decTree.setLayoutData(new GridData(GridData.FILL_BOTH));
 //
 //		Question qSicakMi = decTree.addQuestion("Madde sıcak mı?");
 //		Question qCokMuSicak = decTree.addQuestion("Çok mu sıcak?");
 //		decTree.addAnswer(qSicakMi, qCokMuSicak, new AnswerData("evet",
 //				"madde sıcak"));
 //		decTree.addAnswer(qSicakMi, decTree.createBlockLeaf(), new AnswerData(
 //				"hayır", "madde sıcak değil"));
 //
 //		Question qYaniyorMu = decTree.addQuestion("Madde yanıyor mu?",
 //				"Yanıyor mu?");
 //
 //		decTree.addAnswer(qCokMuSicak, decTree.createBlockLeaf(),
 //				new AnswerData("biraz sıcak", "madde biraz sıcak"));
 //		decTree.addAnswer(qCokMuSicak, qYaniyorMu, new AnswerData("evet",
 //				"madde çok sıcak"));
 //
 //		decTree.addAnswer(qYaniyorMu, decTree.getLeaf(sn2), new AnswerData(
 //				"evet", "madde yanıyor"));
 //		decTree.addAnswer(qYaniyorMu, decTree.createBlockLeaf(),
 //				new AnswerData("hayır", "madde sönük"));
 //
 //		return decTree;
 		return null;
 	}
 
 	@Focus
 	public void onFocus() {
 		// TODO Your code here
 	}
 }
