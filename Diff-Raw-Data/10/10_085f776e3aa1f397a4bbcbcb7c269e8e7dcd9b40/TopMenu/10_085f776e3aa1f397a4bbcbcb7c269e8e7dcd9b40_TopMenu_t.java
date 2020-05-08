 package com.essaid.p4.urigen.menu;
 
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 
 import org.protege.editor.core.ProtegeManager;
 import org.protege.editor.core.editorkit.EditorKit;
 import org.protege.editor.core.ui.workspace.WorkspaceFrame;
 import org.protege.editor.owl.OWLEditorKit;
 import org.protege.editor.owl.model.OWLEditorKitHook;
 import org.protege.editor.owl.model.OWLWorkspace;
 import org.protege.editor.owl.model.event.EventType;
 import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
 import org.protege.editor.owl.model.event.OWLModelManagerListener;
 import org.semanticweb.owlapi.model.OWLAnnotation;
 import org.semanticweb.owlapi.model.OWLLiteral;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.essaid.p4.urigen.menu.action.ActiveAction;
 import com.essaid.p4.urigen.menu.action.FollowAction;
 import com.essaid.p4.urigen.menu.action.GeneratorAction;
 
 public class TopMenu extends OWLEditorKitHook {
 
 	public static final String SERVER_URL_AP_IRI = "http://urigen.p4.essaid.com/server-url";
 	public static final String ONTOLOGY_IRI_AP_IRI = "http://urigen.p4.essaid.com/ontology-iri";
 
 	private static Logger log = LoggerFactory.getLogger(TopMenu.class);
 	private OWLEditorKit editorKit;
 	private ActiveAction active;
 	private FollowAction follow;
 	private JMenu select;
 	private OWLModelManagerListener modelListener;
 	private JMenu topMenu;
 
 	private boolean menuFound = false;
 
 	private String serverUrl;
 	private String ontologyIri;
 
 	public String getServerUrl() {
 		return serverUrl;
 	}
 
 	public String getOntologyIri() {
 		return ontologyIri;
 	}
 
 	public void setServerUrl(String serverUrl) {
 		this.serverUrl = serverUrl;
 	}
 
 	public void setOntologyIri(String ontologyIri) {
 		this.ontologyIri = ontologyIri;
 	}
 
 	private GeneratorAction generator;
 
 	public TopMenu() {
 	}
 
 	public TopMenu(EditorKit kit) {
 		this.editorKit = (OWLEditorKit) kit;
 	}
 
 	public void setActiveAction(ActiveAction item) {
 		this.active = item;
 	}
 
 	public ActiveAction getActiveMenuItem() {
 		return active;
 	}
 
 	public void setSelectMenu(JMenu menu) {
 		this.select = menu;
 	}
 
 	public JMenu getSelectMenu(JMenu menu) {
 		return select;
 	}
 
 	@Override
 	public void initialise() throws Exception {
 		log.debug("TopMenu initializing");
 		this.editorKit = getEditorKit();
 		MenuManager.INSTANCE().addTopMenu(editorKit, this);
 		this.modelListener = new OWLModelManagerListener() {
 
 			@Override
 			public void handleChange(OWLModelManagerChangeEvent event) {
//				log.info("OWLModel event: {}", event.getType());
 
 				if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
 					serverUrl = null;
 					ontologyIri = null;
 					OWLOntology ontology = editorKit.getOWLModelManager()
 							.getActiveOntology();
 					for (OWLAnnotation a : ontology.getAnnotations()) {
 						if (a.getProperty().getIRI().toString()
 								.equals(ONTOLOGY_IRI_AP_IRI)) {
 							ontologyIri = ((OWLLiteral) a.getValue())
 									.getLiteral();
 						}
 						if (a.getProperty().getIRI().toString()
 								.equals(SERVER_URL_AP_IRI)) {
 							serverUrl = ((OWLLiteral) a.getValue())
 									.getLiteral();
 						}
 					}
 
					
					MenuManager.INSTANCE().getEntityFactory(getEditorKit())
					.setNeedsUpdate(true);
 				}
 			}
 		};
 
 		editorKit.getOWLModelManager().addListener(modelListener);
 
 		
 	}
 
 	@Override
 	public void dispose() throws Exception {
 		editorKit.getOWLModelManager().removeListener(modelListener);
 		MenuManager.INSTANCE().clearKitMappings(editorKit);
 	}
 
 	public void setTopMenu(JMenu menu) {
 		this.topMenu = menu;
 		active.setMenuItem((JCheckBoxMenuItem)topMenu.getMenuComponent(0));
 		follow.setMenuItem((JCheckBoxMenuItem)topMenu.getMenuComponent(1));
 	}
 
 	public JMenu getTopMenuJMenu() {
 		return topMenu;
 	}
 
 	public void setFollowAction(FollowAction followAction) {
 		this.follow = followAction;
 	}
 
 	public FollowAction getFollowAction() {
 		return follow;
 	}
 
 	public boolean isActive() {
 		return active.isActive();
 	}
 
 	public boolean isFollow() {
 		return follow.isFollow();
 	}
 
 	public void setGeneratorAction(GeneratorAction generatorAction) {
 		this.generator = generatorAction;
 	}
 
 	public GeneratorAction getGeneratorAction() {
 		return generator;
 	}
 }
