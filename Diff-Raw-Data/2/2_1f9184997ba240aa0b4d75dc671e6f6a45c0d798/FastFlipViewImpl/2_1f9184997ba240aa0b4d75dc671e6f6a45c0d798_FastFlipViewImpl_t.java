 package gwt.client.view.desktop;
 
 import gwt.client.ui.CustomFrame;
 import gwt.client.view.FastFlipView;
 
 import java.util.ArrayList;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 
 public class FastFlipViewImpl extends Composite implements FastFlipView {
 
     @UiTemplate("FastFlipViewImpl.ui.xml")
     interface MyUiBinder extends UiBinder<Widget, FastFlipViewImpl> {
     }
     private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
 
     public interface Style extends CssResource {
         String titleLb();
         String highlightedTitleLb();
         String iFrame();
         String current();
         String next();
         String far();
         String fartest();
     }
     
     @UiField
     Style style;
 
     @UiField
     Button backBtn;
     @UiField
     Anchor anchor;
     @UiField
     Button titleListBtn;
     @UiField
     FlowPanel titlePanel;
     @UiField
     CustomFrame iFrame1;
     @UiField
     CustomFrame iFrame2;
     @UiField
     CustomFrame iFrame3;
     @UiField
     CustomFrame iFrame4;
     
     private Presenter presenter;
     private String blankLink;
     private int total;
     
     public FastFlipViewImpl() {
         initWidget(uiBinder.createAndBindUi(this));
         
         titlePanel.addDomHandler(new MouseOutHandler() {
             @Override
             public void onMouseOut(MouseOutEvent event) {
                 titlePanel.setVisible(false);
             }
         }, MouseOutEvent.getType());
     }
 
     @Override
     public Widget asWidget() {
         return this;
     }
 
     @Override
     public void init(Presenter presenter, String blankLink) {
         this.presenter = presenter;
         this.blankLink = blankLink;
         
         iFrame1.setCustomUrl("");
         iFrame1.setCustomTitle("");
         iFrame1.setUrl(blankLink);
         
         iFrame2.setCustomUrl("");
         iFrame2.setCustomTitle("");
         iFrame2.setUrl(blankLink);
         
         iFrame3.setCustomUrl("");
         iFrame3.setCustomTitle("");
         iFrame3.setUrl(blankLink);
         
         iFrame4.setCustomUrl("");
         iFrame4.setCustomTitle("");
         iFrame4.setUrl(blankLink);
         
         iFrame1.setStyleName(style.iFrame() + " " + style.current());
         iFrame2.setStyleName(style.iFrame() + " " + style.next());
         iFrame3.setStyleName(style.iFrame() + " " + style.far());
         iFrame4.setStyleName(style.iFrame() + " " + style.fartest());
         
         anchor.setHref("");
         anchor.setText("Loading...");
         
         titleListBtn.setHTML("-/-- &#x25BC;");
         
         titlePanel.setVisible(false);
     }
     
     @Override
     public void setFrames(int index, String link1, String title1, String ruleLink1, 
             String link2, String title2, String ruleLink2,
             String link3, String title3, String ruleLink3, int total){
         
         iFrame1.setCustomUrl(link1);
         iFrame1.setCustomTitle(title1);
         iFrame1.setUrl(ruleLink1);
         
         iFrame2.setCustomUrl(link2);
         iFrame2.setCustomTitle(title2);
         iFrame2.setUrl(ruleLink2);
         
         iFrame3.setCustomUrl(link3);
         iFrame3.setCustomTitle(title3);
         iFrame3.setUrl(ruleLink3);
         
         iFrame4.setCustomUrl("");
         iFrame4.setCustomTitle("");
         iFrame4.setUrl(blankLink);
         
         iFrame1.setStyleName(style.iFrame() + " " + style.current());
         iFrame2.setStyleName(style.iFrame() + " " + style.next());
         iFrame3.setStyleName(style.iFrame() + " " + style.far());
         iFrame4.setStyleName(style.iFrame() + " " + style.fartest());
         
         this.total = total;
        updateHeader(index, iFrame1.getCustomUrl(), iFrame1.getCustomTitle());
         setCurrentFrameFocus();
     }
     
     @Override
     public void next(int index, String link, String title, String ruleLink){
         nextIFrame(iFrame1, index, link, title, ruleLink);
         nextIFrame(iFrame2, index, link, title, ruleLink);
         nextIFrame(iFrame3, index, link, title, ruleLink);
         nextIFrame(iFrame4, index, link, title, ruleLink);
         setCurrentFrameFocus();
     }
     
     @Override
     public void showTitleList(ArrayList<String> titleList, int selectedIndex) {
         Label highlightedTitleLb = null;
         titlePanel.clear();
         for (int i = 0; i < titleList.size(); i++) {    
             Label titleLb = new Label(titleList.get(i));
             titleLb.addStyleName(style.titleLb());
             if ( i == selectedIndex) {
                 titleLb.addStyleName(style.highlightedTitleLb());
                 highlightedTitleLb = titleLb;
             }
             titleLb.addClickHandler(titleLbClickHandler);
             titlePanel.add(titleLb);
         }
         titlePanel.setVisible(true);
         
         if (highlightedTitleLb != null) {
             final Label finalHighlightedTitleLb = highlightedTitleLb;
             Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                 @Override
                 public void execute() {
                     int titlePanelHeight = titlePanel.getOffsetHeight();
                     int titleLbTop = finalHighlightedTitleLb.getElement().getOffsetTop();
                     if (titlePanelHeight < titleLbTop) {
                         titlePanel.getElement().setScrollTop(titleLbTop -
                                 (titlePanelHeight / 3));
                     } else {
                         titlePanel.getElement().setScrollTop(0);
                     }
                 }
             });
         }
     }
 
     @UiHandler("nextBtn")
     void onNextBtnClicked(ClickEvent event){
         presenter.onNextBtnClicked();
     }
     
     @UiHandler("backBtn")
     void onBackBtnClicked(ClickEvent event){
         presenter.onBackBtnClicked();
     }
     
     @UiHandler("titleListBtn")
     void onTitleListBtnClicked(ClickEvent event){
         if (titlePanel.isVisible()) {
             titlePanel.setVisible(false);
         } else {
             presenter.onTitleListBtnClicked();
         }
     }
     
     private ClickHandler titleLbClickHandler = new ClickHandler() {
         @Override
         public void onClick(ClickEvent event) {
             Label titleLb = (Label)event.getSource();
             int index = titlePanel.getWidgetIndex(titleLb);
             presenter.onTitleListItemSelected(index);
             titlePanel.setVisible(false);
         }
     };
     
     private void nextIFrame(final CustomFrame frame, int index, String link,
             String title, String ruleLink){
         if(frame.getStyleName().contains(style.current())){
             frame.setStyleName(style.iFrame() + " " + style.fartest());
             frame.setCustomUrl("");
             frame.setCustomTitle("");
             frame.setUrl(blankLink);
         }else if(frame.getStyleName().contains(style.next())){
             frame.setStyleName(style.iFrame() + " " + style.current());
             updateHeader(index, frame.getCustomUrl(), frame.getCustomTitle());
         }else if(frame.getStyleName().contains(style.far())){
             frame.setStyleName(style.iFrame() + " " + style.next());
         }else if(frame.getStyleName().contains(style.fartest())){
             frame.setStyleName(style.iFrame() + " " + style.far());
             frame.setCustomUrl(link);
             frame.setCustomTitle(title);
             frame.setUrl(ruleLink);
         }else{
             throw new AssertionError(frame.getStyleName());
         }
     }
     
     private void updateHeader(int index, String link, String title){
         anchor.setHref(link);
         anchor.setText(title);
         anchor.setTarget("_blank");
         
         titleListBtn.setHTML((index + 1) + "/" + total + " &#x25BC;");
     }
     
     private void setCurrentFrameFocus(){
         Scheduler.get().scheduleDeferred(new ScheduledCommand() {
             @Override
             public void execute() {
                 if(iFrame1.getStyleName().contains(style.current())){
                     setFrameFocus(iFrame1.getElement());
                 }else if(iFrame2.getStyleName().contains(style.current())){
                     setFrameFocus(iFrame2.getElement());
                 }else if(iFrame3.getStyleName().contains(style.current())){
                     setFrameFocus(iFrame3.getElement());
                 }else if(iFrame4.getStyleName().contains(style.current())){
                     setFrameFocus(iFrame4.getElement());
                 }else{
                     throw new AssertionError();
                 }
             }
         });
     }
     
     private native void setFrameFocus(Element frame) /*-{
         if (frame && frame.contentWindow) {
             frame.contentWindow.focus();
         }
     }-*/;
 }
