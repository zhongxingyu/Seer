 package de.lere.vaad.animation.splaytree;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 
 import algoanim.animalscript.AnimalScript;
 import algoanim.primitives.generators.Language;
 import algoanim.util.Node;
 import de.lere.vaad.BinaryTreeProperties;
 import de.lere.vaad.animation.DefaultVisibilityEventListener;
 import de.lere.vaad.animation.GraphWriter;
 import de.lere.vaad.animation.GraphWriterImpl;
 import de.lere.vaad.animation.SourceCodeWriter;
 import de.lere.vaad.animation.StepWriter;
 import de.lere.vaad.animation.Timings;
 import de.lere.vaad.animation.TreeAnimationBase;
 import de.lere.vaad.animation.binarysearchtree.BinaryTreeSetup;
 import de.lere.vaad.animation.locationhandler.LocationDirector;
 import de.lere.vaad.treebuilder.BinaryTreeModel;
 import de.lere.vaad.treebuilder.events.TreeDeleteEvent;
 import de.lere.vaad.treebuilder.events.TreeInsertEvent;
 import de.lere.vaad.treebuilder.events.TreeModelChangeEventListenerForSplaytreeAnimations;
 import de.lere.vaad.treebuilder.events.TreeSearchEvent;
 import de.lere.vaad.utils.TextLoaderUtil;
 
 public class SplayTreeAnimation extends TreeAnimationBase<String> implements SplayTreeStepListener<String> {
 
 	private TextLoaderUtil loaderUtil;
 
 	private TextLoaderUtil getLoaderUtil() {
 		if (loaderUtil == null) {
 			loaderUtil = new TextLoaderUtil(getClass(), "resources/descriptions");
 			
 		}
 		return loaderUtil;
 	}
 
 	private SplayTreeAnimation(Language l, BinaryTreeProperties tp) {
 		super(l,tp, "P,F,R,A,G".split(","));
 	}
 
 	/**
 	 * Container Object for properties of this Animation
 	 */
 	public static void main(String[] args) {
 		// Create a new animation
 		// name, author, screen width, screen height
 
 		BinaryTreeProperties tp = new BinaryTreeProperties();
 		
 		tp.getGraphProperties().set("fillColor", Color.WHITE);
 		tp.getSourceCodeProperties().set("highlightColor", Color.CYAN);
 		
 		
 		tp.authors = "Rene Hertling, Leo Roos";
 
 		tp.title = "Splaytree Animation";
 
 		Language l = new AnimalScript(tp.title, tp.authors,
 				tp.screenResolution.width, tp.screenResolution.height);
 		SplayTreeAnimation animation = new SplayTreeAnimation(l, tp);
 			animation.buildAnimation();
 
 		String animationCode = l.getAnimationCode();
 
 		System.out.println(animationCode);
 
 		File file = new File("/tmp/splayTreeAnimation.asu");
 		try {
 			FileUtils.writeStringToFile(file, animationCode);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private BinaryTreeSetup<String> createSetup(GraphWriter<String> writer) {
 		BinaryTreeSetup<String> setup = new BinaryTreeSetup<String>();
 		setup.setBinaryTreeProperties(getBinaryTreeProperties());
 		setup.setLang(language);
 		setup.setStepWriter(new StepWriter(language));
 		setup.setWriter(writer);
 		return setup;
 	}
 
 	@Override
 	protected void doBuildAnimation() {
 		
 		SourceCodeWriter scw = new SourceCodeWriter(language, getBinaryTreeProperties().getSourceCodeProperties(), DIRECTOR_SMALLISH_SOURCECODE, new Timings());
 		
 		scw.newSourceCode("1. Zig Step\n" + "2. Zig-zig Step\n"
 						+ "3. Zig-zag Step");
 		scw.setAutomaticLineUnhighlighting(true);
 		scw.highlight(1);
 
 		//
 		// Zig-Step
 		//
 		String actionZigMacroDescription = "Wenn p die Wurzel ist wird der Zig-Step ausgeführt. \n"
 				+ "Dieser Schritt wird nur ausgeführt wenn der Baum eine\n"
 				+ "ungerade Knotenanzahl hat und es sich um die letzte\n"
 				+ "Splay-Operation einer Transformation handelt.";
 		nextStateOnLocation(actionZigMacroDescription, DIRECTOR_MACROSTEP);
 
 		final GraphWriter<String> writer = new GraphWriterImpl<String>(
 				language, layout);
 		BinaryTreeSetup<String> setup = createSetup(writer);
 
 		BinaryTreeModel<String> model = BinaryTreeModel
 				.createTreeByInsert("P,F,R,A,G".split(","));
 
 		DefaultVisibilityEventListener<String> visibilityAnimator = new DefaultVisibilityEventListener<String>(
 				writer);
 		TreeModelChangeEventListenerForSplaytreeAnimations<String> changeAnimator = new TreeModelChangeEventListenerForSplaytreeAnimations<String>(setup);
 		model.addListener(changeAnimator);
 		model.addListener(visibilityAnimator);
 		model.show();
 
 		step();
 
 		nextStateOnLocation("Führe Rechtsrotation um P aus.",
 				DIRECTOR_MACROSTEP);
 
 		step();
 
 		rightRotateAround(model, "P");
 
 		//
 		// ZigZig-Step
 		//
 
 		hideAll(model);
 		scw.highlight(2);
 		String actionZigzigMacroDescription = "Es wird C gesucht.\n"
 				+ "Ist der gesuchte Knoten der linke Sohn seines Vaters der wiederum ein linkes Kind ist,\n"
 				+ "wird der Zig-zig Schritt ausgeführt, der eine doppelte Rechtsrotation ist.\n"
 				+ "(bzw. doppelte Linksrotation im Spiegelverekehrten Fall)";
 		nextStateOnLocation(actionZigzigMacroDescription, DIRECTOR_MACROSTEP);
 
 		model = BinaryTreeModel.createTreeByInsert("P,G,R,C,H,A,B".split(","));
 		model.addListener(changeAnimator);
 		model.addListener(visibilityAnimator);
 		model.show();
 		step();
 
 		nextStateOnLocation("Führe Rechtsrotation um g aus.",
 				DIRECTOR_MICROSTEP);
 
 		de.lere.vaad.treebuilder.Node<String> g = model.search("G");
 		step();
 		model.rightRotate(g);
 
 		nextStateOnLocation("Führe Rechtsrotation um p aus.",
 				DIRECTOR_MICROSTEP);
 		step();
 		rightRotateAround(model, "P");
 
 		//
 		// ZigZag-Step
 		//
 		hideAll(model);
 		scw.highlight(3);
 		String actionZigzagMacroDescription = "Sei I gesucht.\n"
 				+ "I befindet sich links von P und recht von G bzw.\n"
 				+ "Hier wird ein Zig-zag Schritt ausgeführt.\n";
 		nextStateOnLocation(actionZigzagMacroDescription, DIRECTOR_MACROSTEP);
 		model = BinaryTreeModel.createTreeByInsert("P,G,S,E,I,H,J".split(","));
 		model.addListener(changeAnimator);
 		model.addListener(visibilityAnimator);
 		model.show();
 		step();
 
 		nextStateOnLocation("Führe Linksrotation um G aus.",
 				DIRECTOR_MICROSTEP);
 		step();
 		leftRotateAround(model, "G");
 
 		nextStateOnLocation("Führe Rechtsrotation um P aus.",
 				DIRECTOR_MICROSTEP);
 		step();
 		rightRotateAround(model, "P");
 
 		//
 		// Description of Behavior for Access Operations
 		//
 		hideAll(model);
 		scw.unhighlight(3);
 		nextStateOnLocation("", DIRECTOR_DESCRIPTION_BEGINNING);
 		String accessBehaviourMacroDescription = "Verhalten bei zugreifenden Operationen	\n"
 				+ "Suche:\n"
 				+ "Wird ein Knoten gesucht, wird auf diesem eine Splay Operation ausgeführt.\n"
 				+ "Einfügen:\n"
 				+ "Beim Einfügen wird der Knoten wie in einen Binärbaum hinzugefügt und\n"
 				+ "anschließend der eingefügte Knoten zur Wurzel gesplayed\n"
 				+ "Löschen:\n"
 				+ "Beim Löschen wird der zu löschende Knoten wie in einem Binärbaum gelöscht\n"
 				+ "und sein Parent zur Wurzel gesplayed.";
 		nextStateOnLocation(accessBehaviourMacroDescription, DIRECTOR_MACROSTEP);
 
 		step();
 		//
 		// Showcase the operations
 		//
 		String showcaseMacroDescription = "Beispielhafte Darstellung der beschriebenen Operationen.";
 		nextStateOnLocation(showcaseMacroDescription, DIRECTOR_MACROSTEP);
 
 		nextStateOnLocation("SUCHE NUN!",
 				DIRECTOR_MICROSTEP);		
 		step();
 		BinaryTreeModel<String> btree = BinaryTreeModel.createTreeByInsert("10,5,20,1,7,15,25".split(","));
 		SplayTreeModel<String> splay = SplayTreeModel.from(btree);
 		SplayTreeModelChangeListener<String> changeListener = new SplayTreeModelChangeListener<String>(setup);
 		splay.addListener(visibilityAnimator);
 		splay.addListener(changeListener);		
 		changeListener.add(this);
 		splay.show();
 		step();
 		splay.search("25");		
 		nextStateOnLocation("EINF�GEN NUN!", DIRECTOR_MICROSTEP);
 		step();
 		splay.insert("40");
 
 		nextStateOnLocation("L�SCHEN NUN!", DIRECTOR_MICROSTEP);
 
 		step();
 
 	}
 
 	private void rightRotateAround(BinaryTreeModel<String> model, String v) {
 		de.lere.vaad.treebuilder.Node<String> p = model.search(v);
 		model.rightRotate(p);
 	}
 
 	private void leftRotateAround(BinaryTreeModel<String> model, String v) {
 		de.lere.vaad.treebuilder.Node<String> p = model.search(v);
 		model.leftRotate(p);
 	}
 
 	private <T extends Node> void nextStateOnLocation(String string,
 			LocationDirector<T> director) {
 		lh.nextStateOnLocation(string, director);
 	}
 
 
 	@Override
 	public void newStep(SplayTreeEvent step) {
 		SplayTreeEvent<String> cause = step;		
 		if(cause instanceof ZigStartedEvent){
 			nextStateOnLocation("Performing a Zig-Step on some node", DIRECTOR_MICROSTEP);			
 		}
 		else if(cause instanceof ZigZigStartedEvent){
 			nextStateOnLocation("Performing a ZigZig", DIRECTOR_MICROSTEP);
 		}
 		else if(cause instanceof ZigZagStartedEvent){
 			nextStateOnLocation("Performing a ZigZag", DIRECTOR_MICROSTEP);
 		}
 		else {
 			throw new IllegalArgumentException("Invalid Splay-Event");
 		}
 		step();		
 	}
 
 	@Override
 	public void nodeFound(TreeSearchEvent<String> event) {
 		nextStateOnLocation("Suche: " + event.searchVal, DIRECTOR_MACROSTEP);
 		step();
 	}
 
 	@Override
 	public void nodeDelete(TreeDeleteEvent<String> event) {
 		nextStateOnLocation("Lösche: " + event.deleteValue, DIRECTOR_MACROSTEP);
 		step();
 	}
 
 	@Override
 	public void nodeInserted(TreeInsertEvent<String> event) {		
		nextStateOnLocation("Einfügen von " + event.nodeOfModification, DIRECTOR_MACROSTEP);
 		step();
 	}
 
 	@Override
 	protected String getAnimationTitle() {
 		return getBinaryTreeProperties().title;
 	}
 
 
 	@Override
 	protected String getInitialDescription() {
 		return getLoaderUtil().getText("intro");
 	}
 }
