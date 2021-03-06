 package org.zkoss.fiddle.composer;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.zkoss.fiddle.component.renderer.SourceTabRendererFactory;
 import org.zkoss.fiddle.composer.event.FiddleEventQueues;
 import org.zkoss.fiddle.composer.event.FiddleEvents;
 import org.zkoss.fiddle.composer.event.InsertResourceEvent;
 import org.zkoss.fiddle.composer.event.ResourceChangedEvent;
 import org.zkoss.fiddle.composer.event.SaveCaseEvent;
 import org.zkoss.fiddle.composer.event.ShowResultEvent;
 import org.zkoss.fiddle.composer.event.SourceRemoveEvent;
 import org.zkoss.fiddle.core.utils.CRCCaseIDEncoder;
 import org.zkoss.fiddle.core.utils.ResourceFactory;
 import org.zkoss.fiddle.dao.api.ICaseRecordDao;
 import org.zkoss.fiddle.dao.api.ICaseTagDao;
 import org.zkoss.fiddle.dao.api.IResourceDao;
 import org.zkoss.fiddle.dao.api.ITagDao;
 import org.zkoss.fiddle.fiddletabs.Fiddletabs;
 import org.zkoss.fiddle.manager.CaseManager;
 import org.zkoss.fiddle.manager.VirtualCaseManager;
 import org.zkoss.fiddle.model.Case;
 import org.zkoss.fiddle.model.CaseRecord;
 import org.zkoss.fiddle.model.Resource;
 import org.zkoss.fiddle.model.Tag;
 import org.zkoss.fiddle.model.api.ICase;
 import org.zkoss.fiddle.model.api.IResource;
 import org.zkoss.fiddle.seo.SEOContainer;
 import org.zkoss.fiddle.seo.handle.SEOTokenHandlerAdpter;
 import org.zkoss.fiddle.seo.model.SEOToken;
 import org.zkoss.fiddle.visualmodel.FiddleSandbox;
 import org.zkoss.fiddle.visualmodel.ViewRequest;
 import org.zkoss.fiddle.visualmodel.VirtualCase;
 import org.zkoss.social.facebook.event.LikeEvent;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.EventQueue;
 import org.zkoss.zk.ui.event.EventQueues;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.util.GenericForwardComposer;
 import org.zkoss.zkplus.spring.SpringUtil;
 import org.zkoss.zul.A;
 import org.zkoss.zul.Checkbox;
 import org.zkoss.zul.Div;
 import org.zkoss.zul.Hlayout;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.Tabpanels;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Window;
 
 public class SourceCodeEditorComposer extends GenericForwardComposer {
 
 	/**
 	 * Logger for this class
 	 */
 	private static final Logger logger = Logger.getLogger(SourceCodeEditorComposer.class);
 
 	public List<Resource> resources;
 
 	private Fiddletabs sourcetabs;
 
 	private Tabpanels sourcetabpanels;
 
 	private ICase $case = null;
 
 	private Textbox caseTitle;
 
 	private Window insertWin;
 	
 	
 	private Div caseToolbar;
 	
 	private A download;	
 
 	/* for tags */
 	private Hlayout tagContainer;
 
 	private Label tagEmpty;
 
 	private Textbox tagInput;
 
 	private Hlayout editTag;
 
 	private Hlayout viewTag;
 
 	private String lastVal;
 	
 	private Checkbox cbSaveTag;
 	
 	/**
 	 * a state for if content is changed.
 	 * 
 	 * Note: For implementation , If user modify the content and then modify it
 	 * back , we think taht's a source changed state ,too.
 	 */
 	private boolean sourceChange = false;
 
 	/**
 	 * we use desktop level event queue.
 	 */
 	private EventQueue sourceQueue = EventQueues.lookup(FiddleEventQueues.SOURCE, true);
 
 	public void doAfterCompose(Component comp) throws Exception {
 		super.doAfterCompose(comp);
 
 		resources = new ArrayList<Resource>();
 
 		$case = (ICase) requestScope.get("__case"); // new Case();
 
 		boolean newCase = ($case == null || $case.getId() == null);
 		if (newCase) { // new case!
 			resources.addAll(getDefaultResources());
 			initSEOHandler($case, resources);
 		} else {
 			initForCaseExist();
 		}
 
 		
 		initSourceEvents();
 		for (IResource resource : resources) {
 			SourceTabRendererFactory.getRenderer(resource.getType()).
 				appendSourceTab(sourcetabs, sourcetabpanels,resource);
 			if (newCase) {
 				// Notify content to do some processing,since we use desktop
 				// scope eventQueue,it will not be a performance issue.
 				sourceQueue.publish(new ResourceChangedEvent(null, resource));
 			}
 		}
 		
 		
 		// @see FiddleDispatcherFilter for those use this directly
 		ViewRequest viewRequestParam = (ViewRequest) requestScope.get("runview");
 		if (viewRequestParam != null) {
 			runDirectlyView(viewRequestParam);
 		}
 	}
 	
 	private void initForCaseExist(){
 		ICaseRecordDao manager = (ICaseRecordDao) SpringUtil.getBean("caseRecordDao");
 		manager.increase(CaseRecord.Type.View, $case);
 		if (logger.isDebugEnabled()) {
 			logger.debug("counting:" + $case.getToken() + ":" + $case.getVersion() + ":view");
 		}
 		IResourceDao dao = (IResourceDao) SpringUtil.getBean("resourceDao");
 		List<Resource> dbResources = dao.listByCase($case.getId());
 		for (IResource r : dbResources) {
 			// we clone it , since we will create a new resource instead of
 			// updating old one.
 			Resource resource = r.clone();
 			resource.setId(null);
 			resource.setCaseId(null);
 			resource.setCreateDate(new Date());
 			resources.add(resource);
 		}
 
 		caseTitle.setValue($case.getTitle());
 		
 		download.setHref("/download/"+$case.getToken() + "/" + $case.getVersion());
 		caseToolbar.setVisible(true);
 		
 		initTagEditor();
 		
 		initSEOHandler($case, dbResources);
 	}
 
 	
 	private void initTagEditor(){
 		ICaseTagDao caseTagDao = (ICaseTagDao) SpringUtil.getBean("caseTagDao");
 		List<Tag> list = caseTagDao.findTagsBy($case, 1, 30);
 		updateTags(list);
 		
 		EventListener handler = new EventListener() {
 			public void onEvent(Event event) throws Exception {
 				performUpdateTag();
 			}
 		};
 		
 		tagInput.addEventListener("onOK",handler);
 		tagInput.addEventListener("onCancel",new EventListener() {
 			public void onEvent(Event event) throws Exception {
 				tagInput.setValue(lastVal);
 				setTagEditable(false);
				event.stopPropagation();
 			}
 		});
 	}
 	
 	private void setTagEditable(boolean bool){
 		
 		//2011/6/27:TonyQ 
 		//set visible twice for forcing smart update
 		//sicne we set visible in client , so the visible state didn't sync with server,
 		//we need to make sure the server will really send the smartUpdate messages. ;)		
 		editTag.setVisible(!bool);
 		editTag.setVisible(bool); //actually we want editTag visible false
 		
 		viewTag.setVisible(bool);
 		viewTag.setVisible(!bool); //actually we want viewTag visible true
 	}
 	
 	private void performUpdateTag(){
 		String val = tagInput.getValue();
 		
		boolean valueChange = ( lastVal == null || !val.equals(lastVal));
 		//Do nothing if it didn't change
 		if(valueChange){
 			ITagDao tagDao = (ITagDao) SpringUtil.getBean("tagDao");
			
			List<Tag> list = "".equals(val.trim()) ? new ArrayList<Tag>() : 
					tagDao.prepareTags(val.split("[ ]*,[ ]*"));
 			ICaseTagDao caseTagDao = (ICaseTagDao) SpringUtil.getBean("caseTagDao");
 			caseTagDao.replaceTags($case, list);
 			
 			EventQueues.lookup(FiddleEventQueues.Tag).
 				publish(new Event(FiddleEvents.ON_TAG_UPDATE,null));
 			
 			updateTags(list);
 		}
 		
 		setTagEditable(false);
 	}
 	
 	private void updateTags(List<Tag> list){
		tagContainer.getChildren().clear();		
 		if(list.size() == 0){
 			tagEmpty.setVisible(true);
 			cbSaveTag.setVisible(false);
 		}else{
 			StringBuffer sb = new StringBuffer();
 			for(Tag tag:list){
 				A lbl = new A(tag.getName());
 				lbl.setHref("/tag/"+tag.getName());
 				lbl.setSclass("case-tag");
 				sb.append(tag.getName()+",");
 				tagContainer.appendChild(lbl);
 			}
 			if(sb.length()!=0){
 				sb.deleteCharAt(sb.length()-1);
 			}
 			tagInput.setValue(sb.toString());
 			lastVal = sb.toString();
 			tagEmpty.setVisible(false);
 			cbSaveTag.setVisible(true);
 		}
 	}
 	
 	private void runDirectlyView(ViewRequest viewRequestParam){
 
 		FiddleSandbox inst = viewRequestParam.getFiddleInstance();
 		if (inst != null) { // inst can't be null
 			// use echo event to find a good timing
 			ShowResultEvent sv = new ShowResultEvent(FiddleEvents.ON_SHOW_RESULT, $case, viewRequestParam.getFiddleInstance());
 			Events.echoEvent(new Event(FiddleEvents.ON_SHOW_RESULT, self, sv));
 		} else {
 			alert("Can't find sandbox from specific version ");
 		}
 	}
 	private void initSourceEvents(){
 		sourceQueue.subscribe(new EventListener() {
 			public void onEvent(Event event) throws Exception {
 				
 				if (event instanceof ResourceChangedEvent) {
 					sourceChange = true;
 				} else if (event instanceof ShowResultEvent) {
 					ShowResultEvent result = (ShowResultEvent) event;
 
 					if (sourceChange) {
 						if ($case != null && $case.getId() != null) {
 							ICaseRecordDao manager = (ICaseRecordDao) SpringUtil.getBean("caseRecordDao");
 							manager.increase(CaseRecord.Type.RunTemp, $case);
 							if (logger.isDebugEnabled()) {
 								logger.debug("counting:" + $case.getToken() + ":" + $case.getVersion() + ":run-temp");
 							}
 						}
 						Case tmpcase = new Case();
 						CRCCaseIDEncoder encoder = CRCCaseIDEncoder.getInstance();
 						String token = encoder.encode(new Date().getTime());
 						tmpcase.setToken(token);
 						tmpcase.setVersion(0);
 
 						List<IResource> newlist = new ArrayList<IResource>();
 						for (IResource current : resources) {
 							IResource cloneResource = current.clone();
 							cloneResource.setFinalConetnt(tmpcase);
 							newlist.add(cloneResource);
 						}
 						VirtualCase virtualCase = new VirtualCase(tmpcase, newlist);
 						VirtualCaseManager.getInstance().save(virtualCase);
 						result.setCase(tmpcase);
 					} else {
 						result.setCase($case);
 						ICaseRecordDao manager = (ICaseRecordDao) SpringUtil.getBean("caseRecordDao");
 						manager.increase(CaseRecord.Type.Run, $case);
 						if (logger.isDebugEnabled()) {
 							logger.debug($case.getToken() + ":" + $case.getVersion() + ":run");
 						}
 					}
 
 					EventQueues.lookup(FiddleEventQueues.SHOW_RESULT, true).publish(result);
 
 				} else if (event instanceof InsertResourceEvent) {
 					InsertResourceEvent insertEvent = (InsertResourceEvent) event;
 					Resource ir = ResourceFactory.getDefaultResource(insertEvent.getType(), insertEvent.getFileName());
 					resources.add(ir);
 					SourceTabRendererFactory.getRenderer(ir.getType()).appendSourceTab(sourcetabs, sourcetabpanels, ir);
 					insertWin.setVisible(false);
 				} else if (event instanceof SaveCaseEvent) {
 					SaveCaseEvent saveEvt = (SaveCaseEvent) event;
 
 					CaseManager caseManager = (CaseManager) SpringUtil.getBean("caseManager");
 					
 					String ip = Executions.getCurrent().getRemoteAddr();
 					ICase saved = caseManager.saveCase($case, resources, caseTitle.getValue(), saveEvt.isFork(), ip,
 							cbSaveTag.isChecked());
 					if (saved != null) {
 						Executions.getCurrent().sendRedirect(
 								"/sample/" + saved.getToken() + "/" + saved.getVersion() + saved.getURLFriendlyTitle());
 					}
 				} else if (event instanceof SourceRemoveEvent) {
 					SourceRemoveEvent sourceRmEvt = (SourceRemoveEvent) event;
 					if (sourceRmEvt.getResource() == null) {
 						throw new IllegalStateException("removing null resource ");
 					}
 					removeResource(sourceRmEvt.getResource());
 				}
 			}
 		});		
 	}
 	
 	public void onLike$fblike(LikeEvent evt) {
 		ICaseRecordDao manager = (ICaseRecordDao) SpringUtil.getBean("caseRecordDao");
 
 		if (evt.isLiked()) {
 			if (logger.isDebugEnabled()) {
 				logger.debug($case.getToken() + ":" + $case.getVersion() + ":like");
 			}
 			manager.increase(CaseRecord.Type.Like, $case);
 		} else {
 			if (logger.isDebugEnabled()) {
 				logger.debug($case.getToken() + ":" + $case.getVersion() + ":unlike");
 			}
 			manager.decrease(CaseRecord.Type.Like, $case.getId());
 		}
 	}
 
 	public void onShowResult(Event e) {
 		EventQueues.lookup(FiddleEventQueues.SHOW_RESULT, true).publish((ShowResultEvent) e.getData());
 	}
 
 	private void removeResource(IResource ir) {
 		int k = -1;
 		for (int i = 0; i < resources.size(); ++i) {
 			if (resources.get(i) == ir) {
 				k = i;
 				break;
 			}
 		}
 		if (k != -1)
 			resources.remove(k);
 	}
 
 	private List<Resource> getDefaultResources() {
 		List resources = new ArrayList<IResource>();
 		resources.add(ResourceFactory.getDefaultResource(IResource.TYPE_ZUL));
 		// resources.add(getDefaultResource(IResource.TYPE_JS));
 		// resources.add(getDefaultResource(IResource.TYPE_CSS));
 		// resources.add(getDefaultResource(IResource.TYPE_HTML));
 		resources.add(ResourceFactory.getDefaultResource(IResource.TYPE_JAVA));
 
 		return resources;
 	}
 	
 	public void onAdd$sourcetabs(Event e) {
 		try {
 			insertWin.doOverlapped();		
 		} catch (Exception e1) {
 			logger.error("onAdd$sourcetabs(Event) - e=" + e, e1);
 		}
 	}
 
 	
 	private void initSEOHandler(ICase $case, List<Resource> resources) {
 
 		SEOContainer seo = SEOContainer.getInstance(desktop);
 
 		if ($case != null)
 			seo.addToken(new SEOToken<ICase>("case", $case));
 		if (resources != null)
 			seo.addToken(new SEOToken<List<Resource>>("resources", resources));
 
 		seo.addHandler(new SEOTokenHandlerAdpter<ICase>() {
 
 			public boolean accept(String type) {
 				return "case".equals(type);
 			}
 
 			public void resolve(Writer out, String type, ICase model) throws IOException {
 
 				appendTagStart(out, "div", "case");
 
 				appendTitle(out, 2, model.getTitle());
 				appendText(out, "version", model.getVersion());
 				appendText(out, "token", model.getToken());
 				appendText(out, "create date", model.getCreateDate());
 
 				appendTagEnd(out, "div");
 			}
 		});
 
 		seo.addHandler(new SEOTokenHandlerAdpter<List<Resource>>() {
 
 			public boolean accept(String type) {
 				return "resources".equals(type);
 			}
 
 			public void resolve(Writer out, String type, List<Resource> model) throws IOException {
 
 				appendTagStart(out, "div", "resoruces");
 				appendTitle(out, 3, "resources");
 
 				for (Resource r : model) {
 					appendText(out, "fileName", r.getName());
 					appendText(out, "fileType", r.getTypeName());
 					// r.getFinalContent() == null only when default resources
 					appendText(out, "fileContent", r.getFinalContent() == null ? r.getContent() : r.getFinalContent());
 				}
 				appendTagEnd(out, "div");
 
 			}
 		});
 	}
 
 }
