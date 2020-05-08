 /* Copyright 2011 Anthony Cassidy
  * Usage of the works is permitted provided that this instrument is retained with the works, so that any entity that uses the works is notified of this instrument.
  *  DISCLAIMER: THE WORKS ARE WITHOUT WARRANTY.
  */
 package com.github.a2g.core.objectmodel;
 
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 import com.github.a2g.bridge.animation.Timer;
 import com.github.a2g.bridge.image.PackagedImage;
 import com.google.gwt.event.dom.client.LoadHandler;
 import com.github.a2g.bridge.panel.MasterPanel;
 import com.github.a2g.bridge.panel.Window;
 import com.github.a2g.bridge.thing.AcceptsOneThing;
 import com.github.a2g.core.action.ActionRunner;
 import com.github.a2g.core.action.BaseAction;
 import com.github.a2g.core.action.NullParentAction;
 import com.github.a2g.core.loader.ImageBundleLoaderCallbackAPI;
 import com.github.a2g.core.loader.ImageBundleLoaderAPI;
 import com.github.a2g.core.loader.ImageBundleLoader;
 import com.github.a2g.core.primitive.ColorEnum;
 import com.github.a2g.core.action.BaseDialogTreeAction;
 import com.github.a2g.core.authoredscene.ConstantsForAPI;
 import com.github.a2g.core.authoredscene.ImageAddAPI;
 import com.github.a2g.core.authoredscene.InternalAPI;
 import com.github.a2g.core.authoredscene.OnDialogTreeAPI;
 import com.github.a2g.core.authoredscene.OnDoCommandAPI;
 import com.github.a2g.core.authoredscene.OnEntryAPI;
 import com.github.a2g.core.authoredscene.OnEveryFrameAPI;
 import com.github.a2g.core.authoredscene.OnFillLoadListAPI;
 import com.github.a2g.core.authoredscene.OnFillLoadListAPIImpl;
 import com.github.a2g.core.authoredscene.OnPreEntryAPI;
 import com.github.a2g.core.authoredscene.SceneAPI;
 
 import com.github.a2g.core.event.SaySpeechCallDialogTreeEvent;
 import com.github.a2g.core.event.SaySpeechCallDialogTreeEventHandlerAPI;
 import com.google.gwt.event.shared.EventBus;
 
 public class MasterPresenter  
 implements InternalAPI, 
 SaySpeechCallDialogTreeEventHandlerAPI
 , ImageBundleLoaderCallbackAPI
 , ImageAddAPI
 , OnFillLoadListAPI
 , OnEntryAPI
 , OnPreEntryAPI
 , OnEveryFrameAPI
 , OnDoCommandAPI
 , OnDialogTreeAPI
 {
 
 	private CommandLinePresenter commandLinePresenter;
 	private InventoryPresenter inventoryPresenter;
 	private VerbsPresenter verbsPresenter;
 	private ScenePresenter scenePresenter;
 	private DialogTreePresenter dialogTreePresenter;
 	private LoadingPresenter loadingPresenter;
 	private CueCardPresenter cueCardPresenter;
 
 	private SceneAPI callbacks;
 	private TreeMap<Short, SceneObject> theObjectMap;
 	private TreeMap<Integer, Animation> theAnimationMap;
 	private Set<String> setOfCompletedLoaders;
 
 	private EventBus bus;
 	private MasterPresenterHostAPI parent;
 	
 	private Timer timer;
 	private MasterPanel masterPanel;
 	private List<ImageBundleLoader> listOfEssentialLoaders;
 	private List<ImageBundleLoader> listOfNonEssentialLoaders;
 	private ActionRunner actionRunner;
 	private int textSpeedDelay;
 	private Integer[] theListOfIndexesToInsertAt;
 	private Map<String, SceneObjectCache>  objectCache;
 
 	private Logger logger = Logger.getLogger("com.mycompany.level");
 	private String inventoryResourceAsString;
 	
 	private ImageBundleLoader theCurrentLoader;
 	
 
 	public MasterPresenter(final AcceptsOneThing panel, EventBus bus, MasterPresenterHostAPI parent) {
 		this.bus = bus;
 		this.timer = null;
 		this.parent = parent;
 		this.textSpeedDelay = 20;
 		
 		this.theObjectMap = new TreeMap<Short, SceneObject>();
 		this.theAnimationMap = new TreeMap<Integer, Animation>();
 		this.listOfEssentialLoaders = new LinkedList<ImageBundleLoader>();
 		this.listOfNonEssentialLoaders = new LinkedList <ImageBundleLoader>();
 		this.setOfCompletedLoaders = new TreeSet<String>();
 		this.objectCache = new TreeMap<String,SceneObjectCache>();
 		this.actionRunner = new ActionRunner();
 		this.theListOfIndexesToInsertAt= new Integer[100];
 		for(int i=0;i<100;i++)
 			theListOfIndexesToInsertAt[i] = new Integer(0);
 
 		bus.addHandler(
 				SaySpeechCallDialogTreeEvent.TYPE,
 				this);
 
 
 
 		this.masterPanel = new MasterPanel();
 		panel.setThing(this.masterPanel);
 
 
 		//masterPanel.getHostForCommandLine().setSize(320, 50);
 		this.dialogTreePresenter = new DialogTreePresenter(
 				masterPanel.getHostForDialogTree(), bus, this);
 		this.commandLinePresenter = new CommandLinePresenter(
 				masterPanel.getHostForCommandLine(), bus, this);
 		
 		this.inventoryPresenter = new InventoryPresenter(
 				masterPanel.getHostForInventory(), bus, parent, this);
 		this.verbsPresenter = new VerbsPresenter(
 				masterPanel.getHostForVerbs(), bus, parent, this);
 		this.scenePresenter = new ScenePresenter(
 				masterPanel.getHostForScene(), bus, this);
 		this.loadingPresenter =  new LoadingPresenter(
 				masterPanel.getHostForLoading(), bus, this);
 		this.cueCardPresenter =  new CueCardPresenter(
 				masterPanel.getHostForCueCard(), bus, this);
 
 
 		this.scenePresenter.setWorldViewSize(
 				scenePresenter.getWidth(),
 				scenePresenter.getHeight());
 
 		this.setLoadingActive();
 
 	}
 
 	public void setCallbacks(SceneAPI callbacks) {
 		this.loadingPresenter.setName(callbacks.toString());
 		this.callbacks = callbacks;
 		this.getCommandLineGui().setCallbacks(
 				callbacks);
 	}
 
 	public MasterPresenter getHeaderPanel() {
 		return this;
 	}
 
 	
 	
 	@Override
 	public boolean addImageForAnInventoryItem(LoadHandler lh, String objectTextualId, int objectCode, PackagedImage imageResource)
 	{
 		if (this.callbacks == null) {
 			return true;
 		}
 		InventoryItem item = this.inventoryPresenter.getInventory().items().at(
 				objectTextualId);
 		boolean result = true;
 
 		
 		if (item == null) 
 		{
 
 			Image imageAndPos = inventoryPresenter.getView().createNewImageAndAdddHandlers(
 					imageResource,lh, bus, objectTextualId, objectCode, 0,0);
 			
 
 			result = inventoryPresenter.addInventory(
 					objectTextualId, objectCode,
 					imageAndPos);
 		
 			imageAndPos.addImageToPanel( 0 );
 			
 	    }
 
 		return result;
 	} 
 
 	@Override
 	public boolean addImageForASceneObject(LoadHandler lh, int numberPrefix, int x, int y, String objectTextualId, String animationTextualId, short objectCode, int objPlusAnimCode, PackagedImage imageResource) {
 		if (this.callbacks == null) {
 			return true;
 		}
 
 				
 		Image imageAndPos = this.scenePresenter.getView().createNewImageAndAdddHandlers(lh, imageResource, this, bus, x,y, objectTextualId, objectCode);
 		
 		theCurrentLoader.addToAppropriateAnimation(numberPrefix, imageAndPos, objectTextualId, animationTextualId, objectCode, objPlusAnimCode, scenePresenter.getWidth(), scenePresenter.getHeight());
 				
 		
 		int before = getIndexToInsertAt(numberPrefix);
 		updateTheListOfIndexesToInsertAt(numberPrefix);
 		
 		// this triggers the loading
 		imageAndPos.addImageToPanel( before );
 	
 		
 		return true;
 	}
 
 	@Override
 	public SceneObject getObject(short code) {
 		SceneObject ob = this.theObjectMap.get(
 				code);
 
 		if (ob == null) {
 		}
 		return ob;
 	}
 
 	@Override
 	public Animation getAnimation(int code) {
 		Animation anim = this.theAnimationMap.get(
 				code);
 
 		if (anim == null) {
 			// first param is name, second is parent;
 			anim = new Animation("", null);
 			this.theAnimationMap.put(code,
 					anim);
 		}
 		return anim;
 	}
 
 	@Override
 	public InventoryItem getInventoryItem(int i) {
 		InventoryItem inv = inventoryPresenter.getInventoryItem(
 				i);
 
 		return inv;
 	}
 
 	public int getIndexToInsertAt(int numberPrefix) {
 		int i = theListOfIndexesToInsertAt[numberPrefix];
 		return i;
 	}
 
 	void updateTheListOfIndexesToInsertAt(int numberPrefix)
 	{
 		for(int i=numberPrefix;i<=99;i++)
 		{
 			theListOfIndexesToInsertAt[i]++;
 		}
 	}
 	
 	public void onEnterScene() {
 		NullParentAction npa = new NullParentAction();
 		npa.setApi(this);
 		BaseAction a = this.callbacks.onEntry(this,npa);
 
 		executeBaseActionAndProcessReturnedInteger(a);
 	}
 
 	@Override
 	public void executeBaseActionAndProcessReturnedInteger(BaseAction a) {
 		actionRunner.runAction(a);
 	}
 	
 	public void skip()
 	{
 		actionRunner.skip();
 	}
 	
 	public void decrementTextSpeed()
 	{
 		textSpeedDelay++;
 	}
 	
 	public void incrementTextSpeed()
 	{
 		textSpeedDelay--;
 	}
 	
 
 	public void showEverything() {
 		int count = this.scenePresenter.getModel().objectCollection().count();
 		for (int i = 0; i<count; i++) 
 		{
 			SceneObject sceneObject = this.scenePresenter.getModel().objectCollection().at(i);
 
 			if (sceneObject != null) {
 				if (sceneObject.getAnimations().at(ConstantsForAPI.INITIAL)!= null) 
 				{
 					sceneObject.getAnimations().at(ConstantsForAPI.INITIAL).setAsCurrentAnimation();
 				} 
 				else 
 				{
 					boolean b = true;
 
 					b = (b) ? true : false;
 				}
 
 			}
 		}
 
 	}
 
 
 
 	public void prepareSceneForFocus() {
 		this.callbacks.onPreEntry(this);
 	}	
 
 	public void doEveryFrame() {
 		if(timer!=null)
 		{
 			this.callbacks.onEveryFrame(this);
 		}
 	}
 
 
 
 	public void showEverythingThenEnterScene()
 	{
 
 		prepareSceneForFocus();
 		showEverything();
 		loadInventoryFromAPI();
 		this.dialogTreePresenter.setInDialogTreeMode(
 				false);
 		startEveryFrameCallbacks();
 		onEnterScene();
 	}
 
 	public void loadInventoryFromAPI() {
 		
 		inventoryPresenter.updateInventory();
 	}
 
 	public void saveInventoryToAPI() {
 		InventoryItemCollection items = this.inventoryPresenter.getInventory().items();
 
 		for (int i = 0; i < items.getCount(); i++) {
 			String name = items.at(i).getTextualId();
 
 			int isCarrying = items.at(i).isVisible()
 			? 1
 					: 0;
 
 			setValue(
 					"CARRYING_"
 					+ name.toUpperCase(),
 					isCarrying);
 		}
 	}
 
 	@Override
 	public void setValue(String name, int value) {
 		parent.setValue(name, value);
 
 	}
 
 	@Override
 	public int getValue(String name) {
 		int i = parent.getValue(name);
 
 		return i;
 	}
 
 	@Override
 	public boolean isTrue(String name) {
 		int property = getValue(name);
 
 		return property != 0;
 	}
 
 	@Override
 	public void switchToScene(String scene) {
 		// since instantiateScene..ToIt does some assynchronous stuff,
 		// I thought maybe I could do it, then cancel the timers.
 		// but I've put it off til I need the microseconds.
 		cancelTimer();
 		this.actionRunner.cancel();
 		this.parent.instantiateSceneAndCallSetSceneBackOnTheMasterPresenter(	scene);
 	}
 
 	@Override
 	public String getLastScene() {
 
 		return null;
 	}
 
 	@Override
 	public boolean isInDebugMode() {
 		return true;
 	}
 
 	public void startEveryFrameCallbacks() 
 	{
 		timer = new Timer()
 		{
 
 			@Override
 			public void run() {
 				doEveryFrame();
 			}
 		};
 		timer.scheduleRepeating(40);
 	}
 
 	public void cancelTimer() 
 	{
 		if(this.timer!=null)
 		{
 			this.timer.cancel();
 			timer = null;
 		}
 
 	}
 
 	@Override
 	public void setLastCommand(double x, double y, int v,
 			String a, String b) {
 		parent.setLastCommand(x, y, v, a, b);
 
 	}
 
 	public void setCommandLineGui(CommandLinePresenter commandLinePanel) {
 		this.commandLinePresenter = commandLinePanel;
 	}
 
 	@Override
 	public CommandLinePresenter getCommandLineGui() {
 		return commandLinePresenter;
 	}
 
 	@Override
 	public InventoryPresenter getInventoryGui() {
 		return inventoryPresenter;
 	}
 
 	public Inventory getInventory() {
 		return inventoryPresenter.getInventory();
 	}
 
 	@Override
 	public SceneAPI getCurrentScene() {
 		return this.callbacks;
 	}
 
 	@Override
 	public void executeBranchOnCurrentScene(int branchId) {
 		NullParentAction npa = new NullParentAction();
 		npa.setApi(this);
 		BaseDialogTreeAction actionChain = this.callbacks.onDialogTree(this, npa, branchId);
 
 		executeBaseActionAndProcessReturnedInteger(
 				actionChain);
 	}
 
 
 	public void saySpeechAndThenExecuteBranchWithBranchId(String speech, int branchId) {
 		this.dialogTreePresenter.clear();
 
 		// this code is dodgey because it doesn't use the current scene to execute..
 		// it uses a dynamically created scene to execute it. This seems ok, if it has the same api pointer.
 		short objId = getDialogTreeGui().getDialogTreeTalker();
 		
 		NullParentAction npa = new NullParentAction();
 		npa.setApi(this);
 		BaseAction say = npa.say(objId, speech);
 		BaseDialogTreeAction actionChain = callbacks.onDialogTree(this, say, branchId);
 
 		executeBaseActionAndProcessReturnedInteger(actionChain);
 	}
 
 	@Override
 	public VerbsPresenter getVerbsGui() {
 		return this.verbsPresenter;
 	}
 
 	@Override
 	public DialogTreePresenter getDialogTreeGui() {
 		return this.dialogTreePresenter;
 	}
 
 	@Override
 	public ScenePresenter getSceneGui() {
 		return this.scenePresenter;
 	}
 
 	@Override
 	public void onSaySpeechCallBranch(String speech, int branchId) {
 		saySpeechAndThenExecuteBranchWithBranchId(
 				speech, branchId);
 	}
 
 	public MasterPanel getMasterPanel() {
 		return masterPanel;
 	}
 
 
 	@Override
 	public void onImageLoaded() 
 	{
 		loadingPresenter.incrementProgress();
 
 	}
 	
 	void startGame()
 	{
 		setSceneActive();
 		showEverythingThenEnterScene();
 	}
 
 
 	@Override
 	public void addEssential(ImageBundleLoaderAPI blah)
 	{
 
 		for(int i=0;i<blah.getNumberOfBundles();i++)
 		{
 			listOfEssentialLoaders.add( new ImageBundleLoader(this,blah,i));
 		}
 
 
 	}
 
 	@Override
 	public void addNonEssential(ImageBundleLoaderAPI blah) {
 
 		for(int i=0;i<blah.getNumberOfBundles();i++)
 		{
 
 			listOfNonEssentialLoaders.add( new ImageBundleLoader(this,blah,i));
 			
 		}
 
 	}
 
 
 	@Override
 	public void kickStartLoading() 
 	{
 		Collections.sort(listOfNonEssentialLoaders);
 		Collections.sort(listOfEssentialLoaders);
 		int total = 0;
 		boolean isSameInventory = false;
 		// get totals
 		Iterator<ImageBundleLoader> iter = listOfEssentialLoaders.iterator();
 		while(iter.hasNext())
 		{
 			ImageBundleLoader loader = iter.next();
 		
 			if(loader.isInventory())
 			{
 				String loaderName = loader.getCombinedClassAndNumber();
 				
 				if(loaderName.equalsIgnoreCase(this.inventoryResourceAsString))
 				{
 					iter.remove();
 					isSameInventory = true;
 					continue;
 				}
 				else
 				{
 					this.inventoryResourceAsString = loaderName;
 				}
 			}
 				 
 			total+=loader.getNumberOfImages();
 		}
 		for(int i=0;i<listOfNonEssentialLoaders.size();i++)
 		{
 			total+=listOfNonEssentialLoaders.get(i).getNumberOfImages();
 		}
 
 		// hide all visible images.
 		// (using scene's data is quicker than using scenePanel data)
 		for(int i=0;i<scenePresenter.getModel().objectCollection().count();i++)
 		{
 			scenePresenter.getModel().objectCollection().at(i).setVisible(false);
 		}
 
 		theObjectMap.clear();
 		theAnimationMap.clear();
 		this.theObjectMap.clear();
 		this.theAnimationMap.clear();
 		scenePresenter.reset();
 		
 		
 		// set gui to blank
 		setLoadingActive();
 		//scenePresenter.clear(); don't clear, all its images are switched off anyhow.
 		loadingPresenter.clear();
 		commandLinePresenter.clear();
 		verbsPresenter.clear();
 		
 	
 		
 
 		if(!isSameInventory)
 		{
 			inventoryPresenter.clear();
 			
 		}
 		
 		loadingPresenter.setTotal(total);
 		loadNext();
 	}
 
 	void loadNext()
 	{
 		if(!listOfEssentialLoaders.isEmpty())
 		{
 			theCurrentLoader = this.listOfEssentialLoaders.get(0);
 		}
 		else if(!listOfNonEssentialLoaders.isEmpty())
 		{
 			theCurrentLoader = this.listOfNonEssentialLoaders.get(0);
 		}
 		else
 		{
 			startGame();
 			return;
 		}
 		
 		String nameAndNum = theCurrentLoader.getCombinedClassAndNumber();
 		if(objectCache.containsKey(nameAndNum))
 		{
 			mergeWithScene(objectCache.get(nameAndNum));
 			
 			//remove from processing straight away.
 			this.listOfEssentialLoaders.remove(0);
 			loadNext();
 		}
 		else
 		{
 			theCurrentLoader.setCallbacks(this);
 			String s = theCurrentLoader.toString();
 			if(this.setOfCompletedLoaders.contains(s))
 			{
 				theCurrentLoader.runLoaderAfterItsBeenLoaded();
 			}else
 			{
 				theCurrentLoader.runLoader();
 			}
 			// only when it completes will it
 			// a) be removed from the list
 			// b) loadNext
 		}
 		
 	}
 
 	@Override
 	public void onLoaderComplete(ImageBundleLoader loader) 
 	{
 		String loaderName = loader.toString();
 		setOfCompletedLoaders.add(loaderName);
 		SceneObjectCache cachedCollection = loader.getSceneObjectCollection();
 		String combinedName = loader.getCombinedClassAndNumber();
 		objectCache.put(combinedName, cachedCollection);
 		mergeWithScene(cachedCollection);
 		// now we need to remove the loader from the list.
 		// and since we only load non-ess after ess
 		// then we try ess first.
 		if(!listOfEssentialLoaders.isEmpty())
 		{
 			this.listOfEssentialLoaders.remove(0);
 		}
 		else
 		{
 			this.listOfNonEssentialLoaders.remove(0);
 		}
 		loadNext();
 	}
 
 
 	@Override
 	public void setWorldViewSize(int width, int height) {
 		this.scenePresenter.setWorldViewSize(width, height);
 		
 	}
 
 	@Override
 	public int getPopupDelay() {
 		return textSpeedDelay;
 	}
 
 
 	public void setScene(SceneAPI sceneCallbacks) {
 		
 		setCallbacks(sceneCallbacks);
 
 		this.callbacks.onFillLoadList(new OnFillLoadListAPIImpl(this));
 	}
 
 	@Override
 	public void restartReloading() 
 	{
 		Iterator<ImageBundleLoader> iter = listOfEssentialLoaders.iterator();
 		while(iter.hasNext())
 		{
 			iter.next();
 		}
 		for(int i=0;i<listOfNonEssentialLoaders.size();i++)
 		{
 			//listOfNonEssentialLoaders.get(i).fireCompleted();	
 		}
 
 		listOfNonEssentialLoaders.clear();
 		listOfEssentialLoaders.clear();
 
 		this.callbacks.onFillLoadList(new OnFillLoadListAPIImpl(this));
 
 	}
 
 
 
 
 
 
 	private void mergeWithScene(SceneObjectCache s) 
 	{
 
 		
 		String name = s.getName();
 		logger.fine(name);
 		System.out.println("dumping " +  name);
 		SceneObjectCollection theirs = s.getSceneObjectCollection();
 		SceneObjectCollection ours = this.scenePresenter.getModel().objectCollection();
 
 		for(int i=0;i<theirs.count();i++)
 		{
 			SceneObject srcObject = theirs.at(i);
 			String objTextualId = srcObject.getTextualId();
 			int prefix = srcObject.getNumberPrefix();
 			short objectCode = srcObject.getCode();
 			SceneObject destObject = ours.at(objTextualId);
 			if(destObject==null)
 			{
 				destObject = new SceneObject(objTextualId, scenePresenter.getWidth(), scenePresenter.getHeight());
 				destObject.setNumberPrefix(prefix);
 				destObject.setCode(objectCode);
 
 				if (objectCode == -1) {
 					Window.alert(
 							"Missing initial image for "
 									+ objTextualId
 									+ " ");
 					return;
 				}
 
 				ours.add(destObject);
 				this.theObjectMap.put(objectCode,destObject);
 				System.out.println("object " + objTextualId + " " + objectCode);
 				
 			}
 
 			for(int j=0;j<srcObject.getAnimations().getCount();j++)
 			{
 				Animation srcAnimation = srcObject.getAnimations().at(j);
 				int animationCode = srcAnimation.getCode();
 				String animTextualId = srcAnimation.getTextualId();
 				
 				Animation destAnimation = destObject.getAnimations().at(animTextualId);
 				if(destAnimation==null)
 				{
 					destAnimation = new Animation(animTextualId, destObject);
 					destObject.getAnimations().add(destAnimation);
 					destAnimation.setCode(animationCode);
 					this.theAnimationMap.put(animationCode, destAnimation);
 				}
 
 				System.out.println("new anim " + objTextualId + " " + animTextualId+" = "+animationCode);
 				
 				for(int k=0;k<srcAnimation.getFrames().getCount();k++)
 				{
 					Image srcImage = srcAnimation.getFrames().at(k);
 					destAnimation.getFrames().add(srcImage);
 				}
 
 			}
 
 
 		}
 	}
 
 	@Override
 	public void displayCueCard(String text, ColorEnum color) 
 	{
 		if(text.length()>0)
 		{
 			
 			cueCardPresenter.setText(text);
 			cueCardPresenter.setColor(color);
 			this.setCueCardActive();
			
			
 		}
 		else
 		{
 			this.setSceneActive();
 		}
 	}
 	
 	public void setDialogTreeActive()
 	{
 		masterPanel.getHostForDialogTree().setVisible(true);
 		masterPanel.getHostForLoading().setVisible(false);
 		masterPanel.getHostForCueCard().setVisible(false);
 	}
 	
 	public void setLoadingActive()
 	{
 		masterPanel.getHostForDialogTree().setVisible(false);
 		masterPanel.getHostForLoading().setVisible(true);
 		masterPanel.getHostForCueCard().setVisible(false);
 	}
 	
 	public void setCueCardActive()
 	{
 		masterPanel.getHostForDialogTree().setVisible(false);
 		masterPanel.getHostForLoading().setVisible(false);
 		masterPanel.getHostForCueCard().setVisible(true);
 	}
 	
 	
 	public void setSceneActive()
 	{
 		masterPanel.getHostForDialogTree().setVisible(false);
 		masterPanel.getHostForLoading().setVisible(false);
		masterPanel.getHostForCueCard().setVisible(true);
 	}
 }
 
