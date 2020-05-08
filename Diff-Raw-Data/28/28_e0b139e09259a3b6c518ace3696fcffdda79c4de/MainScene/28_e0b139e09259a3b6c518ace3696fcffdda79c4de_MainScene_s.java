 package com.workhub.android.scene;
 
 import jade.core.AID;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.IEntityMatcher;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.ITouchArea;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.detector.ContinuousHoldDetector;
 import org.andengine.input.touch.detector.HoldDetector;
 import org.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
 import org.andengine.input.touch.detector.ScrollDetector;
 import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.andengine.opengl.util.GLState;
 import org.andengine.util.adt.list.SmartList;
 
 import android.app.Dialog;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ListView;
 
 import com.workhub.android.R;
 import com.workhub.android.element.AbstractElement;
 import com.workhub.android.element.BaseElement;
 import com.workhub.android.element.GroupElement;
 import com.workhub.android.element.LinkElement;
 import com.workhub.android.element.PictureElement;
 import com.workhub.android.element.RoundButtonElement;
 import com.workhub.android.element.TextElement;
 import com.workhub.android.utils.ConstantsAndroid;
 import com.workhub.android.utils.GPoint;
 import com.workhub.android.utils.MyListAdapter;
 import com.workhub.android.utils.Ressources;
 import com.workhub.model.ElementModel;
 import com.workhub.model.LinkElementModel;
 import com.workhub.model.PictureElementModel;
 import com.workhub.model.TextElementModel;
 import com.workhub.utils.Constants;
 
 public class MainScene extends Scene implements IOnSceneTouchListener, IHoldDetectorListener, IScrollDetectorListener, OnClickListener{
 
 	private ContinuousHoldDetector mHoldDetector;
 	private ScrollDetector mScrollDetector;
 	private Ressources res;
 	private GroupElement groupElement;
 	private Runnable groupRunnable;
 
 	private Dialog currentDialog;
 	private MyListAdapter adapter;
 	private Sprite touchRound;
 
 
 	public MainScene(Ressources res) {
 		this.res = res;
 		mHoldDetector = new ContinuousHoldDetector(this){
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				boolean visible = false;
 			
 				if(this.mPointerID != TouchEvent.INVALID_POINTER_ID) {
 					final long holdTimeMilliseconds = System.currentTimeMillis() - this.mDownTimeMilliseconds;
 					if(!this.mMaximumDistanceExceeded){
 						if(holdTimeMilliseconds <= this.mTriggerHoldMinimumMilliseconds) {
 							visible = true;
 							float r = -0.25f+((float)(this.mTriggerHoldMinimumMilliseconds-holdTimeMilliseconds))/this.mTriggerHoldMinimumMilliseconds;
 							touchRound.setScale(1/getScaleX()-r, 1/getScaleY()-r);
 						}
 						
 					}						
 				}
 				touchRound.setVisible(visible);
 				super.onUpdate(pSecondsElapsed);
 			}
 		};
 		mHoldDetector.setTriggerHoldMinimumMilliseconds(600);
 		this.registerUpdateHandler(mHoldDetector);
 		mScrollDetector = new ScrollDetector(this);
 		touchRound = new Sprite(-res.toPixel(50), -res.toPixel(50), res.toPixel(50)*2,res.toPixel(50)*2, res.getTR_Rond(), res.getContext().getVertexBufferObjectManager());
 		touchRound.setColor(0.2f, 0.2f, 0.2f);
 		this.attachChild(touchRound);
 		groupRunnable = new Runnable() {
 			@Override
 			public void run() {
 				if(groupElement!=null){
 					if(!groupElement.isInitialize()){
 
 
 						ArrayList<BaseElement> list = new ArrayList<BaseElement>();
 						for (int i = getChildCount()-1; i >=0; i--) {
 							if(getChildByIndex(i) instanceof BaseElement){
 								if(groupElement.collideWith((BaseElement) getChildByIndex(i))){
 									list.add((BaseElement) getChildByIndex(i));
 								}
 							}
 						}
 
 
 						if(list.size()>1){
 							for (int j = 0; j < getChildCount(); j++) {
 
 								if(getChildByIndex(j)instanceof GroupElement){
 									GroupElement g  = (GroupElement) getChildByIndex(j);
 									if(g!=groupElement&&g.containsOneOf(list)){
 										g.clearGroup();
 										g.remove();
 									}
 								}
 							}
 							groupElement.initialize(list);
 							groupElement.setZIndex(ConstantsAndroid.ZINDEX++);
 							registerTouchArea(groupElement);
 							sortChildren(false);
 						}else{
 							groupElement.detachSelf();
 						}
 
 					}else{
 
 						unregisterTouchArea(groupElement);
 						groupElement.detachSelf();
 						groupElement=null;
 					}
 				}}};
 
 
 	}
 
 	@Override
 	protected void onManagedDraw(GLState pGLState, Camera pCamera) {
 		Boolean b = mChildrenSortPending;
 		super.onManagedDraw(pGLState, pCamera);
 
 
 
 
 
 		SmartList<ITouchArea> list = getTouchAreas();
 		if(b){
 			//IEntity entity = (IEntity) getTouchAreas().get(0);
 			list.clear();
 			for (int i = 0; i<getChildCount(); i ++) {
 				if(getChildByIndex(i) instanceof AbstractElement){
 					list.add((ITouchArea) getChildByIndex(i));
 				}
 			}
 			//ZIndexSorter.getInstance().sort((SmartList<IEntity>) getTouchAreas());
 		}
 
 	}
 	public void populate() {
 
 		this.setBackground(new Background(198/255f, 200/255f, 200/255f));
 		float logoW = res.getTR_Logo().getWidth();
 		float logoH = res.getTR_Logo().getHeight();
 		Sprite logo = new Sprite(res.getScreenCenter().x-logoW/2, res.getScreenCenter().y-logoH/2, res.getTR_Logo(), res.getContext().getVertexBufferObjectManager());
 		logo.setAlpha(0.4f);
 		this.attachChild(logo);
 
 		this.setTouchAreaBindingOnActionDownEnabled(true);
 		this.setOnAreaTouchTraversalFrontToBack();
 		this.setOnSceneTouchListenerBindingOnActionDownEnabled(true);
 		this.setOnSceneTouchListener(this);
 		float width = res.getSceneWidth();
 		float height = res.getSceneHeight();
 
 		RoundButtonElement rb = new RoundButtonElement(width-res.toPixel(45), -res.toPixel(14), R.id.bt_raccourci_supprimer, res, null );
 		this.attachChild(rb);
 		this.registerTouchArea(rb);
 		rb = new RoundButtonElement(width-res.toPixel(45), height+res.toPixel(14), R.id.bt_raccourci_envoyer, res, null );
 		this.attachChild(rb);
 		this.registerTouchArea(rb);
 		rb = new RoundButtonElement(0+res.toPixel(45), height+res.toPixel(14), R.id.bt_raccourci_recevoir, res, null );
 		this.attachChild(rb);
 		this.registerTouchArea(rb);  
 
 //		TextElementModel txtM = new TextElementModel(0,"", null, "" );
 //		txtM.setContent("tQu'est-ce que qsdf.org ? QSDF.ORG est un domaine � usage initialement personnel. Les services ou sites webs li�s � qsdf.");
 //		txtM.setTitle("titre de l'element txt");
 //		TextElement txt = new TextElement(txtM, 200, 200, res);
 //		this.registerTouchArea(txt);
 //		this.attachChild(txt);
 
 //		PictureElementModel pm = new PictureElementModel(0,"Image Element", null, null);
 //
 //		PictureElement txt1 = new PictureElement(pm, 100, 200, res);
 //		this.registerTouchArea(txt1);
 //		this.attachChild(txt1);
 
 		//		this.mHullVertices = new float[this.mMeshVertices.length];
 		//		System.arraycopy(this.mMeshVertices, 0, this.mHullVertices, 0, this.mMeshVertices.length);
 		//		this.mHullVertexCount = new JarvisMarch().computeHull(this.mHullVertices, this.mMeshVertexCount, 0, 1, 3);
 		//
 		//		this.mHull = new Mesh(centerX, centerY, this.mHullVertices, this.mHullVertexCount, DrawMode.LINE_LOOP, this.getVertexBufferObjectManager(), DrawType.STATIC);
 		//		this.mHull.registerEntityModifier(new LoopEntityModifier(new SequenceEntityModifier(new ScaleModifier(1, 0.95f, 1.05f), new ScaleModifier(1, 1.05f, 0.95f))));
 		//		this.mHull.setColor(Color.RED);
 
 
 
 	}
 
 	public void addElement(ElementModel model) throws Exception {
 		BaseElement e = null;
 		switch (model.getType()) {
 		case Constants.TYPE_ELEMENT_TEXT:
 			e = new TextElement((TextElementModel) model, res.getScreenCenter().x,res.getScreenCenter().y, res);
 			break;
 		case Constants.TYPE_ELEMENT_PICTURE:
 			e = new PictureElement((PictureElementModel) model, res.getScreenCenter().x,res.getScreenCenter().y, res);
 			break;
 		case Constants.TYPE_ELEMENT_LINK:
 			e = new LinkElement((LinkElementModel) model, res.getScreenCenter().x,res.getScreenCenter().y, res);
 			break;
 		case Constants.TYPE_ELEMENT_FILE:
 			//e = new FileElement((FileElementModel) model, res.getScreenCenter().x,res.getScreenCenter().y, res);
 			break;
 		}
 		if(e!=null){
 			this.registerTouchArea(e);
 			this.attachChild(e);
 		}else{
 			throw new Exception("Element non supporté");
 		}
 
 	}
 
 	private void addShortCut(int id, Object arg) {
 		RoundButtonElement rb = new RoundButtonElement(res.getScreenCenter().x, res.getScreenCenter().y, id, res , arg);
 		this.attachChild(rb);
 		this.registerTouchArea(rb);  
 		
 	}
 	
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent event) {
 		int myEventAction = event.getAction(); 
 
 		final float X = event.getX();
 		final float Y = event.getY();
 
 		switch (myEventAction) {
 		case TouchEvent.ACTION_DOWN:
 			touchRound.setPosition(X-touchRound.getWidth()/2, Y-touchRound.getHeight()/2);
 			touchRound.setZIndex(ConstantsAndroid.ZINDEX++);
 			sortChildren(false);
 			break;
 		case TouchEvent.ACTION_MOVE: {
 			if(groupElement!=null&&!groupElement.isInitialize()){
 				res.getContext().runOnUpdateThread(new Runnable() {
 					@Override
 					public void run() {
 						groupElement.addMeshVertex(X, Y);
 						groupElement.refreshVertexDraw();
 					}
 				});
 
 			}
 
 			break;}
 		case TouchEvent.ACTION_UP:
 		case TouchEvent.ACTION_CANCEL:
 		case TouchEvent.ACTION_OUTSIDE:
 
 			if(groupElement!=null){
 				res.getContext().runOnUpdateThread(groupRunnable);
 			}
 			break;
 		}
 
 
 
 
 		this.mHoldDetector.onTouchEvent(event);
 		this.mScrollDetector.onTouchEvent(event);
 
 
 		return true;
 	}
 
 	@Override
 	public void onHoldStarted(HoldDetector pHoldDetector, int pPointerID,
 			float pHoldX, float pHoldY) {
 		mHoldDetector.reset();		
 	}
 
 	@Override
 	public void onHold(HoldDetector pHoldDetector, long pHoldTimeMilliseconds,
 			int pPointerID, float pHoldX, float pHoldY) {
 
 	}
 
 	@Override
 	public void onHoldFinished(HoldDetector pHoldDetector,
 			long pHoldTimeMilliseconds, int pPointerID, float pHoldX,
 			float pHoldY) {
 		res.getContext().runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				currentDialog = new Dialog(res.getContext(), R.style.dialog_app_theme);
 				currentDialog.setContentView(R.layout.dialog_scene_main);
 				((Button)currentDialog.findViewById(R.id.bt_clear_all)).setOnClickListener(MainScene.this);
 				((Button)currentDialog.findViewById(R.id.bt_nouveau)).setOnClickListener(MainScene.this);
 				((Button)currentDialog.findViewById(R.id.bt_importer)).setOnClickListener(MainScene.this);
 				((Button)currentDialog.findViewById(R.id.bt_nouveau_raccourci)).setOnClickListener(MainScene.this);
 				currentDialog.show();
 			}
 		});
 
 	}
 
 	@Override
 	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
 			float pDistanceX, float pDistanceY) {
 		if(groupElement!=null){
 			final GroupElement grp = groupElement;
 			res.getContext().runOnUpdateThread(new Runnable() {
 				
 				@Override
 				public void run() {
 					unregisterTouchArea(grp);
 					grp.detachSelf();
 				}
 			});
 		}
 		groupElement = new GroupElement(res );
 		attachChild(groupElement);
 	}
 
 	@Override
 	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
 			float pDistanceX, float pDistanceY) {
 
 
 	}
 
 	@Override
 	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
 			float pDistanceX, float pDistanceY) {
 
 
 
 	}
 
 	public void verifyRoundButton(AbstractElement abstractElement) {
 		if(abstractElement instanceof RoundButtonElement)
 			return;
 
 		for (int i = 0; i < getChildCount(); i++) {
 			if(getChildByIndex(i) instanceof RoundButtonElement){
 				RoundButtonElement rb = (RoundButtonElement) getChildByIndex(i);
 
 
 
 				if(abstractElement.contains(rb.getX(), rb.getY())){
 					rb.setActionOn(abstractElement);
 					return;
 				}
 			}
 
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		currentDialog.dismiss();
 		switch (v.getId()) {
 		case R.id.bt_clear_all:
 
 			for (int i = 0; i<getChildCount(); i ++) {
 				if(getChildByIndex(i) instanceof BaseElement){
 					((BaseElement) getChildByIndex(i)).remove();
 				}
 			}
 
 
 			break;
 
 		case R.id.bt_nouveau:
 			currentDialog = new Dialog(res.getContext(), R.style.dialog_app_theme);
 			currentDialog.setContentView(R.layout.dialog_scene_new);
 			((Button)currentDialog.findViewById(R.id.bt_element_texte)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_element_lien)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_element_fichier)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_element_image)).setOnClickListener(MainScene.this);
 			currentDialog.show();
 
 			break;
 		case R.id.bt_nouveau_raccourci:
 			currentDialog = new Dialog(res.getContext(), R.style.dialog_app_theme);
 			currentDialog.setContentView(R.layout.dialog_scene_new_shortcut);
 			((Button)currentDialog.findViewById(R.id.bt_raccourci_editer)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_raccourci_envoyer)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_raccourci_exporter)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_raccourci_masquer)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_raccourci_recevoir)).setOnClickListener(MainScene.this);
 			((Button)currentDialog.findViewById(R.id.bt_raccourci_supprimer)).setOnClickListener(MainScene.this);
 			currentDialog.show();
 			break;
 		case R.id.bt_importer:
 			iniElementList();
			res.getContext().getNeightbourgList();
 			break;
 		case R.id.bt_element_fichier:
 			res.getContext().createElement(Constants.TYPE_ELEMENT_FILE);
 			break;
 		case R.id.bt_element_lien:
 			res.getContext().createElement(Constants.TYPE_ELEMENT_LINK);
 			break;
 		case R.id.bt_element_image:
 			res.getContext().createElement(Constants.TYPE_ELEMENT_PICTURE);
 			break;
 		case R.id.bt_element_texte:
 			res.getContext().createElement(Constants.TYPE_ELEMENT_TEXT);
 			break;
 
 		case R.id.bt_raccourci_editer:
 		case R.id.bt_raccourci_exporter:
 		case R.id.bt_raccourci_masquer:
 		case R.id.bt_raccourci_recevoir:
 		case R.id.bt_raccourci_supprimer:
 			addShortCut( v.getId(), null);
 			
 			break;
 		case R.id.bt_raccourci_envoyer:
 			iniNeighboursList(null);
 			res.getContext().getNeightbourgList();
 			break;
 		}
 
 	}
 
 	
 
 	public  BaseElement getElement(final AID aidModel) {
 		return (BaseElement) getChildByMatcher(new IEntityMatcher() {
 
 			@Override
 			public boolean matches(IEntity pEntity) {
 				if(pEntity instanceof BaseElement){
 					BaseElement b = (BaseElement) pEntity;
 					return (b.getModel().getAgent().equals(aidModel));
 
 				}
 				return false;
 			}
 		});
 	}
 
 	
 	public void addToAdapter(Entry<AID, String> entry){
 		if(adapter!=null){
 			synchronized (adapter) {
 				adapter.addEntry(entry);
 			}
 			
 		}
 	}
 	
 	public void iniElementList() {
 		iniListDialog();
 
 		adapter = new MyListAdapter(res.getContext(),
 				android.R.layout.simple_list_item_1, new ArrayList<String>());
 		ListView listview = (ListView)currentDialog.findViewById(R.id.listview);
 		listview.setAdapter(adapter);
 
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, final View view,
 					int position, long id) {
 				//final String item = (String) parent.getItemAtPosition(position);
 				res.getContext().getElement(adapter.getListAID().get(position));
 			}
 			
 
 		});
 
 		currentDialog.show();
 
 	}
 	
 	public void iniNeighboursList(final BaseElement baseElement) {
 		
 		iniListDialog();
 
 		adapter = new MyListAdapter(res.getContext(),
 				android.R.layout.simple_list_item_1, new ArrayList<String>());
 		ListView listview = (ListView)currentDialog.findViewById(R.id.listview);
 		listview.setAdapter(adapter);
 
 		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, final View view,
 					int position, long id) {
 				//final String item = (String) parent.getItemAtPosition(position);
 				//res.getContext().getElement(adapter.getListAID().get(position));
 				if(baseElement==null){
 					addShortCut(R.id.bt_raccourci_envoyer, adapter.getListAID().get(position));
 				}else{
 					res.getContext().sendElement(adapter.getListAID().get(position), baseElement.getModel().getAgent());
 				}
 			
 			}
 
 		});
 		
 		
 		currentDialog.show();
 
 	}
 
 	private void iniListDialog(){
 		currentDialog = new Dialog(res.getContext(), R.style.dialog_app_theme){
 			@Override
 			public void onDetachedFromWindow() {
 				adapter = null;
 				super.onDetachedFromWindow();
 			}
 		};
 		currentDialog.setContentView(R.layout.listdialog);
 
 	}
 
 
 
 }
 
