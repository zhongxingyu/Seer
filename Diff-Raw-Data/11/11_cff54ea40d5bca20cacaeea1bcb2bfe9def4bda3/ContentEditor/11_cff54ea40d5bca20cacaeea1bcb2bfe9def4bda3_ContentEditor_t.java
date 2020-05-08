 package com.retech.reader.web.client.mobile.ui;
 
 import com.goodow.wave.bootstrap.shared.MapBinder;
 import com.goodow.wave.client.shell.WaveShell;
 import com.goodow.wave.client.wavepanel.WavePanel;
 
 import com.google.gwt.activity.shared.Activity;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.dom.client.Touch;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.DragEndEvent;
 import com.google.gwt.event.dom.client.DragEndHandler;
 import com.google.gwt.event.dom.client.DragOverEvent;
 import com.google.gwt.event.dom.client.DragOverHandler;
 import com.google.gwt.event.dom.client.DragStartEvent;
 import com.google.gwt.event.dom.client.DragStartHandler;
 import com.google.gwt.event.dom.client.GestureChangeEvent;
 import com.google.gwt.event.dom.client.GestureChangeHandler;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchEndHandler;
 import com.google.gwt.event.dom.client.TouchMoveEvent;
 import com.google.gwt.event.dom.client.TouchMoveHandler;
 import com.google.gwt.event.dom.client.TouchStartEvent;
 import com.google.gwt.event.dom.client.TouchStartHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.inject.client.AsyncProvider;
 import com.google.gwt.logging.client.LogConfiguration;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.google.web.bindery.requestfactory.shared.EntityProxyId;
 import com.google.web.bindery.requestfactory.shared.Request;
 
 import com.retech.reader.web.client.style.ReaderResources;
 import com.retech.reader.web.shared.proxy.IssueProxy;
 import com.retech.reader.web.shared.proxy.PageProxy;
 import com.retech.reader.web.shared.proxy.ResourceProxy;
 import com.retech.reader.web.shared.proxy.SectionProxy;
 import com.retech.reader.web.shared.rpc.ReaderFactory;
 
 import org.cloudlet.web.mvp.shared.BasePlace;
 import org.cloudlet.web.service.shared.KeyUtil;
 import org.cloudlet.web.service.shared.LocalStorage;
 import org.cloudlet.web.service.shared.rpc.BaseReceiver;
 
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @Singleton
 public class ContentEditor extends WavePanel implements Activity {
 
   public static final String LAST_PAGE = "now_reader";
   private static final int TOPBAR_TOP = -40;
   private static final Logger logger = Logger.getLogger(ContentEditor.class.getName());
 
   private final HTML html;
   private FlowPanel flowPanel;
   private SectionBrowserView sectionView;
   private final PlaceController placeContorller;
   private final ReaderFactory f;
   private List<PageProxy> pages;
   private List<SectionProxy> sections;
   private PageProxy proxy;
   private int sectionIndex;
   private int pageIndex;
   int contentHeight;
   int contentWidth;
   private final Provider<BasePlace> place;
   private final LocalStorage storage;
   private final KeyUtil keyUtil;
   private final MapBinder<String, IsWidget> isWidgetMapBinder;
   private final WaveShell waveShell;
   private int columnCount;
   private int columnIndex = 1;
   private boolean scheduledTwo = false;
   private boolean scheduledOne = false;
   private boolean scheduledGesture = false;
   private int startX1;
   private int startX2;
   private int startScale;
 
   @Inject
   public ContentEditor(final PlaceController placeContorller, final ReaderFactory f,
       final Provider<BasePlace> place, final LocalStorage storage, final KeyUtil keyUtil,
       final MapBinder<String, IsWidget> isWidgetMapBinder, final WaveShell waveShell) {
     this.placeContorller = placeContorller;
     this.f = f;
     this.place = place;
     this.storage = storage;
     this.keyUtil = keyUtil;
     this.waveShell = waveShell;
     this.isWidgetMapBinder = isWidgetMapBinder;
 
     flowPanel = new FlowPanel();
     html = new HTML();
     html.addStyleName(ReaderResources.INSTANCE().style().contentHtmlPanel());
     this.getElement().getStyle().setMarginTop(1, Unit.EM);
     flowPanel.add(html);
 
     this.setWaveContent(flowPanel);
 
     this.addDomHandler(new TouchStartHandler() {
 
       @Override
       public void onTouchStart(final TouchStartEvent event) {
         JsArray<Touch> touchesStart = event.getTouches();
         switch (touchesStart.length()) {
           case 2:
             // logger.info("touches: 2 ");
             scheduledTwo = false;
             scheduledOne = true;
             Touch touch1 = touchesStart.get(0);
             Touch touch2 = touchesStart.get(1);
             startX1 = touch1.getPageX();
             startX2 = touch2.getPageX();
             break;
           case 1:
             // logger.info("touches: 1 ");
             scheduledOne = false;
             scheduledTwo = true;
             Touch touch = touchesStart.get(0);
             startX1 = touch.getPageX();
             columnCount = html.getElement().getScrollWidth() / contentWidth;
             // logger.info("columnCount:" + columnCount);
             break;
 
           default:
             break;
         }
       }
     }, TouchStartEvent.getType());
 
     this.addDomHandler(new TouchMoveHandler() {
 
       @Override
       public void onTouchMove(final TouchMoveEvent event) {
         event.preventDefault();
         JsArray<Touch> touchesMove = event.getTouches();
         switch (touchesMove.length()) {
           case 2:
             if (scheduledTwo || !scheduledOne) {
               return;
             }
             scheduledTwo = true;
             // logger.info("touch 2 move");
             Touch touch1 = touchesMove.get(0);
             Touch touch2 = touchesMove.get(1);
             int nowX1 = touch1.getPageX();
             int nowX2 = touch2.getPageX();
             int subtractX1 = nowX1 - startX1;
             int subtractX2 = nowX2 - startX2;
             if (subtractX1 > 0 && subtractX2 > 0) {
              // scheduledGesture = true;
               sectionView.getElement().getStyle().setWidth(100, Unit.PCT);
               return;
             } else if (subtractX1 < 0 && subtractX2 < 0) {
              // scheduledGesture = true;
               sectionView.getElement().getStyle().setWidth(0, Unit.PX);
               return;
             }
             break;
           case 1:
             if (scheduledOne || !scheduledTwo) {
               return;
             }
             scheduledOne = true;
             // logger.info("touch 1 move");
             Touch touch = touchesMove.get(0);
             int nowX = touch.getPageX();
             int subtractX = nowX - startX1;
             gotoNextPageAndScrollNext(subtractX);
             break;
 
           default:
             break;
         }
       }
 
     }, TouchMoveEvent.getType());
 
     this.addDomHandler(new TouchEndHandler() {
 
       @Override
       public void onTouchEnd(final TouchEndEvent event) {
         // JsArray<Touch> touchesMove = event.getTouches();
         // logger.info("onTouchEnd fingers:" + touchesMove.length());
         scheduledOne = false;
         scheduledTwo = false;
         scheduledGesture = false;
       }
     }, TouchEndEvent.getType());
 
    flowPanel.addDomHandler(new GestureChangeHandler() {
 
       @Override
       public void onGestureChange(final GestureChangeEvent event) {
        if (scheduledGesture) {
           return;
         }
         logger.info("scale:" + event.getScale() + ";rotation:" + event.getRotation());
       }
     }, GestureChangeEvent.getType());
 
     html.getElement().setDraggable("true");
     html.addDomHandler(new DragStartHandler() {
 
       @Override
       public void onDragStart(final DragStartEvent event) {
         scheduledOne = false;
         startX1 = event.getNativeEvent().getClientX();
         columnCount = html.getElement().getScrollWidth() / contentWidth;
       }
     }, DragStartEvent.getType());
 
     html.addDomHandler(new DragOverHandler() {
 
       @Override
       public void onDragOver(final DragOverEvent event) {
         if (scheduledOne) {
           return;
         }
         scheduledOne = true;
         int nowX = event.getNativeEvent().getClientX();
         int subtractX = nowX - startX1;
         gotoNextPageAndScrollNext(subtractX);
       }
     }, DragOverEvent.getType());
 
     html.addDomHandler(new DragEndHandler() {
 
       @Override
       public void onDragEnd(final DragEndEvent event) {
         scheduledOne = false;
       }
     }, DragEndEvent.getType());
 
     flowPanel.addDomHandler(new ClickHandler() {
 
       @Override
       public void onClick(final ClickEvent event) {
         int offsetHeight = html.getOffsetHeight();
         int offsetWidth = html.getOffsetWidth();
         int clientX = event.getClientX();
         int clientY = event.getClientY();
         if (clientY >= offsetHeight * 0.25 && clientY <= offsetHeight * 0.75
             && clientX >= offsetWidth * 0.25 && clientX <= offsetWidth * 0.75) {
           Style style = waveShell.getTopBar().getElement().getStyle();
           if (style.getTop().equals(TOPBAR_TOP + "px")) {
             style.clearTop();
             style.setOpacity(1);
           } else {
             style.setTop(TOPBAR_TOP, Unit.PX);
             style.clearOpacity();
           }
         }
 
       }
     }, ClickEvent.getType());
 
     // html.addClickHandler(new ClickHandler() {
     //
     // @Override
     // public void onClick(final ClickEvent event) {
     // int offsetWidth = event.getRelativeElement().getOffsetWidth();
     // int x = event.getX();
     // goTo(x > offsetWidth / 2 ? 1 : -1);
     // logger.info(html.getElement().getScrollWidth() + "");
     // logger.info((html.getElement().getScrollWidth() - contentWidth) / contentWidth + 1 + "");
     // }
     //
     // });
   }
 
   @Override
   public String mayStop() {
     return null;
   }
 
   @Override
   public void onCancel() {
   }
 
   @Override
   public void onStop() {
   }
 
   @Override
   public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
     waveShell.getTopBar().addStyleName(ReaderResources.INSTANCE().style().contentTopBar());
     waveShell.getTopBar().getElement().getStyle().setTop(TOPBAR_TOP, Unit.PX);
     // contentHeight = Window.getClientHeight() - 73;
     contentHeight = Window.getClientHeight() - 39 - 16;
     contentWidth = Window.getClientWidth() - 14;
     Style htmlStyle = html.getElement().getStyle();
     htmlStyle.setHeight(contentHeight, Unit.PX);
     htmlStyle.setProperty("webkitColumnWidth", contentWidth + "px");
     flowPanel.getElement().getParentElement().getStyle().setHeight(contentHeight, Unit.PX);
 
     // final EntityProxyId<IssueProxy> issueId =
     // ((BasePlace) placeContorller.getWhere()).getParam(IssueProxy.class);
 
     // if (issueId != null) {
     // PageProxy pageProxy = storage.get(keyUtil.proxyKey(issueId, LAST_PAGE));
     //
     // if (pageProxy != null) {
     // pageStart(pageProxy);
     // Storage.getLocalStorageIfSupported().removeItem(keyUtil.proxyKey(issueId, LAST_PAGE));
     // // placeContorller.goTo(place.get().setPath(ContentEditor.class.getName()).setParameter(
     // // pageProxy.stableId()));
     // return;
     // }
 
     // new BaseReceiver<IssueProxy>() {
     //
     // @Override
     // public void onSuccessAndCached(final IssueProxy issueProxy) {
     //
     // new BaseReceiver<PageProxy>() {
     //
     // @Override
     // public void onSuccessAndCached(final PageProxy pageProxy) {
     // pageStart(pageProxy);
     // }
     //
     // @Override
     // public Request<PageProxy> provideRequest() {
     // return f.pageContext().findFirstPageByIssue(issueProxy).with(PageProxy.WITH);
     // }
     // }.setKeyForProxy(issueId, PageProxy.class.getName()).fire();
     // }
     //
     // @Override
     // public Request<IssueProxy> provideRequest() {
     // return f.find(issueId);
     // }
     // }.setKeyForProxy(issueId).fire();
     // return;
     // }
 
     final EntityProxyId<PageProxy> pageId =
         ((BasePlace) placeContorller.getWhere()).getParam(PageProxy.class);
 
     new BaseReceiver<PageProxy>() {
 
       @Override
       public void onSuccessAndCached(final PageProxy pageProxy) {
 
         AsyncProvider<IsWidget> sectionBrowserView =
             isWidgetMapBinder.getAsyncProvider(SectionBrowserView.class.getName());
         sectionBrowserView.get(new AsyncCallback<IsWidget>() {
 
           @Override
           public void onFailure(final Throwable caught) {
             if (LogConfiguration.loggingIsEnabled()) {
               logger.log(Level.WARNING, "加载" + SectionBrowserView.class.getName()
                   + "失败. 请检查网络连接, 并刷新后重试", caught);
             }
           }
 
           @Override
           public void onSuccess(final IsWidget result) {
             sectionView = (SectionBrowserView) result.asWidget();
             sectionView.addStyleName(ReaderResources.INSTANCE().style().contentSectionView());
             sectionView.getElement().getStyle().setWidth(0, Unit.PX);
             sectionView.setIssueId(pageProxy.getSection().getIssue().stableId());
             sectionView.start(panel, eventBus);
             flowPanel.add(sectionView);
           }
         });
 
         pageStart(pageProxy);
       }
 
       @Override
       public Request<PageProxy> provideRequest() {
         return f.find(pageId).with(PageProxy.WITH);
       }
     }.setKeyForProxy(pageId).fire();
   }
 
   @Override
   protected void onUnload() {
     super.onUnload();
     this.waveShell.getTopBar().removeStyleName(ReaderResources.INSTANCE().style().contentTopBar());
     this.waveShell.getTopBar().getElement().getStyle().clearTop();
     this.sectionView.removeStyleName(ReaderResources.INSTANCE().style().contentSectionView());
     this.sectionView.getElement().getStyle().clearWidth();
     this.html.getElement().getStyle().clearLeft();
   }
 
   private void changePageProxy(final PageProxy pageProxy) {
     placeContorller.goTo(place.get().setPath(ContentEditor.class.getName()).setParameter(
         pageProxy.stableId()));
   }
 
   private void display(final PageProxy pageProxy) {
     new BaseReceiver<ResourceProxy>() {
 
       @Override
       public void onSuccessAndCached(final ResourceProxy response) {
         ContentEditor.this.getWaveTitle().setText(proxy.getTitle());
         html.setHTML(response.getDataString());
         if (pageProxy.getPageNum() != 1) {
           storage.put(keyUtil.proxyAndKey(proxy.getSection().getIssue().stableId(), LAST_PAGE),
               proxy);
         }
       }
 
       @Override
       public Request<ResourceProxy> provideRequest() {
         return f.resource().getResource(proxy);
       }
     }.setKeyForProxy(proxy.stableId(), ResourceProxy.class.getName()).fire();
   }
 
   private void findPages(final SectionProxy sectionProxy, final boolean isNextPage,
       final boolean isNextSection) {
     new BaseReceiver<List<PageProxy>>() {
 
       @Override
       public void onSuccessAndCached(final List<PageProxy> pages) {
         ContentEditor.this.pages = pages;
         if (isNextPage) {
           if (isNextSection) {
             changePageProxy(pages.get(0));
           } else {
             changePageProxy(pages.get(pages.size() - 1));
           }
         }
       }
 
       @Override
       public Request<List<PageProxy>> provideRequest() {
         return f.pageContext().findPagesBySection(sectionProxy).with(PageProxy.WITH);
       }
     }.setKeyForList(sectionProxy.stableId(), PageProxy.class.getName()).fire();
   }
 
   private void goTo(final int offset) {
     pageIndex = pages.indexOf(proxy);
     int index = pageIndex + offset;
     if (index < 0) {
       nextSectionIndex(--sectionIndex, false);
       return;
     } else if (index + 1 > pages.size()) {
       nextSectionIndex(++sectionIndex, true);
       return;
     }
     PageProxy pageProxy = pages.get(index);
     changePageProxy(pageProxy);
   }
 
   private void gotoNextPageAndScrollNext(final int subtractX) {
     if (subtractX > 0 && columnIndex <= 1) {
       goTo(-1);
       return;
     } else if (subtractX < 0 && columnIndex >= columnCount) {
       goTo(1);
       columnIndex = 1;
       return;
     }
     html.getElement().getStyle().setLeft(
         -100 * ((subtractX > 0 ? --columnIndex : ++columnIndex) - 1), Unit.PCT);
   }
 
   private void nextSectionIndex(final int sectionIndex, final boolean isNextSection) {
     if (sectionIndex > sections.size() - 1 && pageIndex + 2 > pages.size()) {
       this.sectionIndex = sections.size() - 1;
       logger.info("最后一页");
       return;
     } else if (sectionIndex < 0) {
       this.sectionIndex = 0;
       logger.info("第一页");
       return;
     }
     findPages(sections.get(sectionIndex), true, isNextSection);
   }
 
   private void pageStart(final PageProxy pageProxy) {
     final SectionProxy sectionProxy = pageProxy.getSection();
     IssueProxy issue = sectionProxy.getIssue();
     this.proxy = pageProxy;
     display(pageProxy);
 
     new BaseReceiver<List<SectionProxy>>() {
 
       @Override
       public void onSuccessAndCached(final List<SectionProxy> sections) {
         ContentEditor.this.sections = sections;
         sectionIndex = sectionProxy.getSequence() - 1;
       }
 
       @Override
       public Request<List<SectionProxy>> provideRequest() {
         return f.section().findSectionByPage(pageProxy).with(SectionProxy.WITH);
       }
     }.setKeyForList(issue.stableId(), SectionProxy.class.getName()).fire();
 
     findPages(sectionProxy, false, false);
   }
 }
