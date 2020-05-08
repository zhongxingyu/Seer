 package net.cscott.sdr.webapp.client;
 
 import java.util.List;
 
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.webapp.client.Model.EngineResultsChangeEvent;
 import net.cscott.sdr.webapp.client.Model.EngineResultsChangeHandler;
 import net.cscott.sdr.webapp.client.Model.FirstInvalidCallChangeEvent;
 import net.cscott.sdr.webapp.client.Model.FirstInvalidCallChangeHandler;
 import net.cscott.sdr.webapp.client.Model.HighlightChangeEvent;
 import net.cscott.sdr.webapp.client.Model.HighlightChangeHandler;
 import net.cscott.sdr.webapp.client.Model.InsertionPointChangeEvent;
 import net.cscott.sdr.webapp.client.Model.InsertionPointChangeHandler;
 import net.cscott.sdr.webapp.client.Model.PlayStatusChangeEvent;
 import net.cscott.sdr.webapp.client.Model.PlayStatusChangeHandler;
 import net.cscott.sdr.webapp.client.Model.SequenceChangeEvent;
 import net.cscott.sdr.webapp.client.Model.SequenceChangeHandler;
 import net.cscott.sdr.webapp.client.Model.SequenceInfoChangeEvent;
 import net.cscott.sdr.webapp.client.Model.SequenceInfoChangeHandler;
 import net.cscott.sdr.webapp.client.Sequence.StartingFormationType;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.resources.client.ClientBundle;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.Window.ClosingEvent;
 import com.google.gwt.user.client.Window.ClosingHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CaptionPanel;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Frame;
 import com.google.gwt.user.client.ui.HTMLTable;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SuggestBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.AbstractImagePrototype.ImagePrototypeElement;
 import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
 import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class SDRweb implements EntryPoint, SequenceChangeHandler, PlayStatusChangeHandler {
     public static final double BPM = 128;
 
     final CallOracle callOracle = new CallOracle();
     final SuggestBox callEntry = new SuggestBox(callOracle);
     final FlexTable callList = new FlexTable();
     final Label currentCall = new Label();
     final Label errorMsg = new Label();
     final VerticalPanel topPanel = new VerticalPanel();
     final AbsolutePanel canvasPanel = new AbsolutePanel();
     final DanceFloor danceFloor = new DanceFloor();
     final MenuItem sequenceTitle =
         new MenuItem(SequenceInfo.UNTITLED, (Command)null);
     final SliderBar playSlider = new SliderBar(0.0, 1.0);
     DockPanel playBar = new DockPanel();
     Animation animation = null;
 
     final Model model = new Model(GWT.<DanceEngineServiceAsync>create
                                   (DanceEngineService.class)) {
         @Override
         public void handleFailure(Throwable caught) {
             Window.alert(caught.toString());
         }
     };
     SequenceStorageServiceAsync storageService =
         GWT.create(SequenceStorageService.class);
 
     public boolean confirmDiscard() {
         if (!model.isDirty()) return true; // nothing to save
         return Window.confirm("Are you sure you want to discard the "+
                               "current unsaved sequence?");
     }
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
         topPanel.setWidth("100%");
         // Menu bar
         MenuBar fileMenu = new MenuBar(true);
         fileMenu.addItem("New", new Command() {
             public void execute() {
                 if (!confirmDiscard()) return;
                 model.newSequence();
             }});
         fileMenu.addItem("Open", new Command() {
             public void execute() {
                 if (!confirmDiscard()) return;
                 doOpen();
             }
         });
         fileMenu.addItem("Save", new Command() {
             public void execute() { doSave(); }
         });
         fileMenu.addItem("Print", new Command() {
             public void execute() {
                 // XXX: in the future we'd open a window with a
                 // better-formatted version, and print *that*
                 Window.print();
             }});
         fileMenu.addItem("Locate", new Command() {
             public void execute() {
                 getLocation(new EAsyncCallback<GeoPt>(){
                     public void onSuccess(GeoPt result) {
                         Window.alert("Got location: "+result.latitude+","+result.longitude);
                     }});
             }});
         fileMenu.addItem("Logout", new Command() {
             public void execute() {
                 storageService.logout(new EAsyncCallback<String>() {
                     public void onSuccess(String logoutURL) {
                         new SdrPopup("Logout from Google", logoutURL) {
                             @Override
                             public void onClose() {
                                 Window.alert("You are now logged out");
                             }};
                     }});
             }});
         fileMenu.addItem("Close", new Command() {
             public native void execute() /*-{ $wnd.close(); }-*/;
             });
         Window.addWindowClosingHandler(new ClosingHandler() {
             public void onWindowClosing(ClosingEvent event) {
                 if (model.isDirty())
                     event.setMessage("This will discard the current sequence.");
             }});
 
         MenuBar programMenu = new MenuBar(true);
         for (Program p: Program.values()) {
             final Program pp = p;
             programMenu.addItem(p.toTitleCase(), new Command() {
                 public void execute() {
                     model.setProgram(pp);
                 }});
         }
         MenuBar formationMenu = new MenuBar(true);
         for (StartingFormationType sft : StartingFormationType.values()) {
             final StartingFormationType ty = sft;
             formationMenu.addItem(sft.humanName, new Command() {
                 public void execute() {
                     model.setStartingFormation(ty);
                 }});
         }
 
         model.addSequenceInfoChangeHandler(new SequenceInfoChangeHandler() {
             String lastTitle = null;
             public void onSequenceInfoChange(SequenceInfoChangeEvent sce) {
                 String title = sce.getSource().getSequenceInfo().title;
                 if (title.equals(lastTitle)) return; // no change
                 lastTitle = title;
                 sequenceTitle.setText(title);
                 Window.setTitle("Square Dance Revolution: "+title);
             }});
 
         // Make a new menu bar, adding a few cascading menus to it.
         MenuBar menu = new MenuBar();
         menu.addItem("File", fileMenu);
         menu.addItem("Program", programMenu);
         menu.addItem("Dancers", formationMenu); // better name for this?
         menu.addSeparator();
         menu.addItem(sequenceTitle);
         topPanel.add(menu);
 
         DockPanel callBar = new DockPanel();
         callBar.setWidth("100%");
         Label callLabel = new Label("Call: ");
         callLabel.setHorizontalAlignment(Label.ALIGN_RIGHT);
         Button callGo = new Button
             (AbstractImagePrototype.create(imageBundle.icon_add()).getHTML());
         callEntry.setWidth("98%");
         callEntry.setStyleName("callEntry");
         callEntry.getElement().setAttribute("autocorrect", "off");
         callEntry.getElement().setAttribute("autocapitalize", "off");
         callBar.add(callLabel, DockPanel.LINE_START);
         callBar.add(callGo, DockPanel.LINE_END);
         callBar.add(callEntry, DockPanel.CENTER);
         callBar.setCellVerticalAlignment(callLabel, DockPanel.ALIGN_MIDDLE);
         callBar.setCellVerticalAlignment(callEntry, DockPanel.ALIGN_MIDDLE);
         callBar.setCellVerticalAlignment(callGo, DockPanel.ALIGN_MIDDLE);
         callBar.setCellWidth(callEntry, "100%");
         callBar.setSpacing(5);
         topPanel.add(callBar);
         RootPanel.get("div-top").add(topPanel);
 
         // call list panel
         callList.setText(0, 0, "Sequence");
         callList.getFlexCellFormatter().setColSpan(0, 0, 2);
         callList.getRowFormatter().setStyleName(0, "callListHeader");
         callList.setStyleName("callList");
         RootPanel.get("div-calllist").add(callList);
         callList.addClickHandler(new ClickHandler(){
             public void onClick(ClickEvent event) {
                 HTMLTable.Cell c = callList.getCellForEvent(event);
                 if (c==null) return; /* click in inactive area */
                 if (model.getEngineResults()==null) return; /*no valid timing*/
                 int row = c.getRowIndex();
                 int callNum = row-1;
                 model.setPlaying(false);
                 model.setInsertionPoint(callNum);
                 model.setHighlightedCall(callNum);
                 // move the slider position to correspond to this
                 // call (careful when moving past the invalid call marker!)
                 double when = 0;
                 int firstInvalidCall=model.getEngineResults().firstInvalidCall;
                 for (int i=0; i<callNum && i<firstInvalidCall; i++)
                     when += model.getEngineResults().timing.get(i);
                 model.setSliderPos(when);
             }});
         model.addInsertionPointChangeHandler(new InsertionPointChangeHandler(){
             public void onInsertionPointChange(InsertionPointChangeEvent sce) {
                 RowFormatter rf = callList.getRowFormatter();
                 int oldRow = sce.oldValue + 1;
                 if (oldRow > 0 && oldRow < callList.getRowCount())
                     rf.removeStyleName(oldRow, "insertionPoint");
                 int newRow = sce.newValue + 1;
                 if (newRow > 0 && newRow < callList.getRowCount())
                     rf.addStyleName(newRow, "insertionPoint");
                 updateErrorMsg();
             }});
         model.addHighlightChangeHandler(new HighlightChangeHandler(){
             public void onHighlightChange(HighlightChangeEvent sce) {
                 RowFormatter rf = callList.getRowFormatter();
                 int oldRow = sce.oldValue + 1;
                 if (oldRow > 0 && oldRow < callList.getRowCount())
                     rf.removeStyleName(oldRow, "highlight");
                 int newRow = sce.newValue + 1;
                 if (newRow > 0 && newRow < callList.getRowCount())
                     rf.addStyleName(newRow, "highlight");
                 updateCurrentCall();
             }});
         model.addFirstInvalidCallChangeHandler(new FirstInvalidCallChangeHandler(){
             public void onFirstInvalidCallChange(FirstInvalidCallChangeEvent fic) {
                 RowFormatter rf = callList.getRowFormatter();
                 // we don't add one, because we're annotating the *last valid*
                 // call, but being given the *first invalid* call
                 int oldRow = fic.oldValue;
                 if (oldRow >= 0 && oldRow < callList.getRowCount())
                     rf.removeStyleName(oldRow, "last-valid");
                 int newRow = fic.newValue;
                 if (newRow >= 0 && newRow < callList.getRowCount())
                     rf.addStyleName(newRow, "last-valid");
             }});
 
 	currentCall.setStyleName("currentCall");
 	errorMsg.setStyleName("errorMsg");
 	canvasPanel.setStyleName("canvasPanel");
 	VerticalPanel topMsgs = new VerticalPanel();
 	topMsgs.add(errorMsg);
         topMsgs.add(currentCall);
         topMsgs.setStyleName("messagePanel");
 
         final ImagePrototypeElement playElement =
             AbstractImagePrototype.create(imageBundle.icon_media_play())
             .createElement();
         final Button playButton = new Button();
         playButton.getElement().appendChild(playElement);
         model.addPlayStatusChangeHandler(new PlayStatusChangeHandler() {
             boolean wasPlay = true;
             public void onPlayStatusChange(PlayStatusChangeEvent sce) {
                 // should be the *opposite* of the current play status
                 boolean isPlay = !model.isPlaying();
                 if (wasPlay==isPlay) return; // suppress extra updates
                 wasPlay = isPlay;
                 // updating just the image src/title/alt instead of replacing
                 // the entire <img> element causes less flashing on gecko
                 ImageResource p = isPlay ?
                         imageBundle.icon_media_play() :
                         imageBundle.icon_media_pause();
                 AbstractImagePrototype.create(p).applyTo(playElement);
             }});
         playButton.addStyleName("playButton");
         playButton.addClickHandler(new ClickHandler(){
             public void onClick(ClickEvent event) {
                 // if we pressed play, but were already at the end, jump to the start
                 if ((!model.isPlaying()) && model.getEngineResults()!=null &&
                     model.getEngineResults().totalBeats - model.getSliderPos() < 0.5)
                     model.setSliderPos(0);
                 // toggle play status
                 model.setPlaying(!model.isPlaying());
             }});
         playSlider.setStepSize(0.25);
         playSlider.setCurrentValue(0);
         playSlider.setNumTicks(1);
         playSlider.setNumLabels(1);
         playSlider.setWidth("100%");
         playSlider.addValueChangeHandler(new ValueChangeHandler<Double>(){
             public void onValueChange(ValueChangeEvent<Double> event) {
                 model.setSliderPos(event.getValue());
             }});
         model.addPlayStatusChangeHandler(new PlayStatusChangeHandler(){
             public void onPlayStatusChange(PlayStatusChangeEvent sce) {
                 playSlider.setCurrentValue(model.getSliderPos(), false);
             }});
         model.addEngineResultsChangeHandler(new EngineResultsChangeHandler() {
             public void onEngineResultsChange(EngineResultsChangeEvent sce) {
                 double totalBeats = model.getEngineResults().totalBeats;
                 playSlider.setMaxValue(totalBeats);
                 playSlider.setNumTicks((int)Math.round(totalBeats));
                 boolean enabled = (totalBeats!=0);
                 playSlider.setEnabled(enabled);
                 playButton.setEnabled(enabled);
                 playSlider.setNumLabels(enabled?1:0);
                 updateErrorMsg();
             }});
         // level label
         final Label levelLabel = new Label("--");
         levelLabel.addStyleName("levelLabel");
         model.addSequenceChangeHandler(new SequenceChangeHandler() {
             public void onSequenceChange(SequenceChangeEvent sce) {
                 Program p = sce.getSource().getSequence().program;
                 String s = (p==Program.MAINSTREAM)?"MS":p.name().toUpperCase();
                 levelLabel.setText(s);
             }});
         playBar.add(playButton, DockPanel.LINE_START);
         playBar.add(levelLabel, DockPanel.LINE_END);
         playBar.add(playSlider, DockPanel.CENTER);
         playBar.setCellWidth(playSlider, "100%");
         playBar.setCellHorizontalAlignment(playSlider, DockPanel.ALIGN_CENTER);
         playBar.setCellVerticalAlignment(playButton, DockPanel.ALIGN_MIDDLE);
         playBar.setCellVerticalAlignment(levelLabel, DockPanel.ALIGN_MIDDLE);
         playBar.setCellVerticalAlignment(playSlider, DockPanel.ALIGN_MIDDLE);
         RootPanel.get("div-playbar").add(playBar);
 
         // canvas takes up all the rest of the space
         canvasPanel.add(danceFloor, 0, 0);
         // but put it behind the call name/error message panel
         canvasPanel.add(topMsgs, 0, 0);
         RootPanel.get("div-canvas-inner").add(canvasPanel);
 
         // we want to take advantage of the entire client area
         Window.setMargin("0px");
         // set a resize handler to keep all the dimensions in line.
         Window.addResizeHandler(new ResizeHandler() {
             public void onResize(ResizeEvent event) {
                 doResize(event.getWidth(), event.getHeight());
           }
         });
 
         // set up default text and handlers for callEntry
         // Listen for keyboard events in the input box.
         callEntry.getTextBox().addKeyPressHandler(new KeyPressHandler() {
             public void onKeyPress(KeyPressEvent event) {
                 if (event.getCharCode() == KeyCodes.KEY_ENTER &&
                     !callEntry.isSuggestionListShowing()) {
                     activate();
                 }
             }});
         // Listen for mouse events on the Add button.
         callGo.addClickHandler(new ClickHandler() {
           public void onClick(ClickEvent event) {
             activate();
           }
         });
         // hook up model
         model.addPlayStatusChangeHandler(this);
         model.addSequenceChangeHandler(this);
         model.addSequenceChangeHandler(new SequenceChangeHandler() {
             public void onSequenceChange(SequenceChangeEvent sce) {
                 callOracle.setProgram(sce.getSource().getSequence().program);
             }});
         model.addEngineResultsChangeHandler(new EngineResultsChangeHandler() {
             public void onEngineResultsChange(EngineResultsChangeEvent sce) {
                 // ensure that animation is updated
                 onPlayStatusChange(null);
             }});
         // initialize all the model-dependent fields
         model.fireEvent(new SequenceInfoChangeEvent());
         model.fireEvent(new SequenceChangeEvent());
         model.fireEvent(new InsertionPointChangeEvent(-1, model.insertionPoint()));
         model.fireEvent(new HighlightChangeEvent(-1, model.highlightedCall()));
         model.fireEvent(new PlayStatusChangeEvent());
         // trigger resize & focus shortly after load
         Timer postLoadTimer = new Timer() {
             @Override
             public void run() { doResize(); callEntry.setFocus(true); }
         };
         postLoadTimer.schedule(1);
     }
 
     void selectSequence(AsyncCallback<SequenceInfo> cb) {
         Window.alert("select one");
     }
     void doOpen() {
         selectSequence(new EAsyncCallback<SequenceInfo>() {
             public void onSuccess(final SequenceInfo info) {
                 storageService.load(info.id, new EAsyncCallback<Sequence>(){
                     public void onSuccess(Sequence sequence) {
                         model.load(info, sequence);
                     }});
             }});
     }
     void doSave() {
         model.regenerateTags(); // ensure automatic tags are up-to-date
         if (model.getSequenceInfo().title.equals(SequenceInfo.UNTITLED))
             model.setTitle(Window.prompt("Title for this sequence",
                                          model.getSequenceInfo().title));
         // XXX ask for tags; use nicer interface
         storageService.save(model.getSequenceInfo(), model.getSequence(),
                             new LAsyncCallback<Long>() {
             @Override
             public void onSuccess(Long result) {
                 model.getSequenceInfo().id = result;
                 model.clean();
                 Window.alert("Saved!");
             }
             @Override
             public void retry() { doSave(); /* retry */ }
         });
     }
     /** Create a new popup which logs into/out of Google and then closes. */
     public static abstract class SdrPopup extends PopupPanel {
         public SdrPopup(String caption, String loginUrl) {
             CaptionPanel cp = new CaptionPanel
                 (caption+" (<a href=\"javascript:cancelPopup()\">close</a>)",
                  true);
             Frame frame = new Frame(loginUrl);
             cp.add(frame);
             setTitle("Login with your Google ID");
             cp.setWidth((Window.getClientWidth()*3/4)+"px");
             cp.setHeight((Window.getClientHeight()*3/4)+"px");
             frame.setStyleName("login-popup-frame");
             cp.addStyleName("login-popup-caption");
             this.addStyleName("login-popup");
             setWidget(cp);
             setPopup(this);
             this.center(); // and show
         }
         public final void closeMe() {
             this.hide();
             // okay, proceed.
             onClose();
         }
         public final void cancelMe() {
             this.hide();
             // but don't call onClose
         }
         /* called after login */
         protected abstract void onClose();
     }
     // stash a reference to the static 'hidePopup' method in a place where the
     // inner iframe can get to it.
     public static native void setPopup(SdrPopup p) /*-{
         $wnd.cancelPopup = function() {
             p.@net.cscott.sdr.webapp.client.SDRweb.SdrPopup::cancelMe()();
         };
         $wnd.hidePopup = function() {
             p.@net.cscott.sdr.webapp.client.SDRweb.SdrPopup::closeMe()();
         };
     }-*/;
     // --- end popup support
 
     void activate() {
         String newCall = callEntry.getText();
         if (newCall.trim().length()==0) return; // nothing entered
         this.model.addCallAtPoint(newCall);
         // clear entry.
         callEntry.setText("");
     }
     void doResize() {
         doResize(Window.getClientWidth(), Window.getClientHeight());
     }
     void doResize(int width, int height) {
         int panelBottom = topPanel.getAbsoluteTop()+topPanel.getOffsetHeight();
         String style = "padding-top: "+panelBottom+"px;";
         RootPanel.get("div-calllist").getElement().setAttribute("style", style);
         RootPanel.get("div-canvas").getElement().setAttribute("style", style);
         // compensate for border of canvasPanel
         //width-=2; height-=2;
         int right = callList.getAbsoluteLeft()+callList.getOffsetWidth();
         right += 5; // padding on right of call list
         style = "padding-left: "+right+"px;";
         RootPanel.get("div-canvas-inner").getElement().setAttribute("style", style);
         playBar.getElement().setAttribute("style", style);
         int playBarHeight = playBar.getOffsetHeight();
         canvasPanel.setHeight((height-panelBottom-playBarHeight)+"px");
         danceFloor.updateCenter();
     }
     public void updateErrorMsg() {
         String msg = null;
         int insertionPoint = model.insertionPoint();
         EngineResults er = model.getEngineResults();
         if (er != null && insertionPoint >=0 && insertionPoint < er.messages.size()) {
             msg = er.messages.get(insertionPoint);
         }
         if (msg==null)
             errorMsg.setVisible(false);
         else {
             errorMsg.setText(msg);
             errorMsg.setVisible(true);
         }
     }
     public void updateCurrentCall() {
         String msg = "";
         int highlight = model.highlightedCall();
         List<String> calls = model.getSequence().calls;
         if (highlight>=0 && highlight < calls.size())
             msg = calls.get(highlight);
         currentCall.setText(msg);
     }
     public void onPlayStatusChange(PlayStatusChangeEvent sce) {
         if (model.isPlaying()) {
             if (this.animation == null)
                 this.newAnimation();
         } else {
             if (this.animation != null)
                 this.animation.cancel();
             this.animation = null;
         }
         EngineResults er = model.getEngineResults();
         if (er!=null) {
             double time = model.getSliderPos();
             // highlight the call list row corresponding to the current slider pos
             int callNum = er.getCallNum(time);
             // note that callNum could be calls.size() (eg, if there are 0 calls)
             model.setHighlightedCall(callNum);
             // update the dance floor
             int n = er.getNumDancers();
             this.danceFloor.setNumDancers(n);
             for (int i=0; i<n; i++)
                 this.danceFloor.update(i, er.getPosition(i, time));
         }
     }
     private void newAnimation() {
         if (this.animation!=null)
             this.animation.cancel();
         if (model.getEngineResults()==null) {
             model.setPlaying(false);
             return;
         }
         final double start = model.getSliderPos();
         final double end = model.getEngineResults().totalBeats;
         final double totalMillis = (end-start)*(60*1000/*one minute*/)/BPM;
         if (totalMillis < 100) {
             model.setPlaying(false);
            model.setSliderPos(end);
             model.setHighlightedCall(model.getEngineResults().firstInvalidCall);
             return;
         }
         this.animation = new Animation() {
             @Override
             protected double interpolate(double progress) {
                 return start + progress * (end-start);
             }
             @Override
             protected void onUpdate(double beat) {
                 boolean last = false;
                 if (beat > model.getEngineResults().totalBeats) {
                     // whoops, sequence has shrunk!
                     beat = model.getEngineResults().totalBeats;
                     last = true;
                 }
                 model.setSliderPos(beat);
                 // dancer and slider animation happens via playstatus listeners
                 if (last) newAnimation();
             }
             @Override
             protected void onComplete() {
                 // keep playing, maybe the sequence has grown.
                 newAnimation();
             }
             @Override
             protected void onCancel() {
                 /* do nothing, just stop */
             }};
         this.animation.run((int)totalMillis);
     }
     public void onSequenceChange(SequenceChangeEvent sce) {
         // build the call list from the model
         final Model model = sce.getSource();
         FlexCellFormatter fcf = callList.getFlexCellFormatter();
         RowFormatter rf = callList.getRowFormatter();
         rf.removeStyleName(0, "last-call");
         int row=1; // row number
         List<String> calls = model.getSequence().calls;
         for (int callIndex=0; callIndex<calls.size(); callIndex++, row++) {
             String call = calls.get(callIndex);
             callList.setText(row, 0, call);
             rf.removeStyleName(row, "not-a-call");
             rf.removeStyleName(row, "last-call");
             fcf.setColSpan(row, 0, 1);
             PushButton removeButton = new PushButton
                 (new Image(imageBundle.icon_close_button()));
             removeButton.setStyleName("removeButton");
             final int ci = callIndex; // for use in click handler
             removeButton.addClickHandler(new ClickHandler(){
                 public void onClick(ClickEvent event) {
                     model.removeCallAt(ci);
                 }});
             fcf.setColSpan(row, 1, 1);
             callList.setWidget(row, 1, removeButton);
         }
         // specially mark the last call, so we can suppress the red line if it
         // is also the last valid call
         rf.addStyleName(row-1, "last-call");
         // add 'end of sequence' row
         if (row < callList.getRowCount() &&
             callList.getCellCount(row) > 1)
             callList.removeCell(row, 1);
         fcf.setColSpan(row, 0, 2);
         callList.setHTML(row, 0, "&nbsp;(end of sequence)&nbsp;");
         rf.addStyleName(row, "not-a-call");
         row++;
         // remove other rows
         for (int j=callList.getRowCount()-1; j>=row; j--)
             callList.removeRow(j);
         // update current call to match new sequence
         updateCurrentCall();
         doResize();
     }
     /** Support for geolocation. */
     public static class GeoPt {
         public final double latitude, longitude;
         public GeoPt(double latitude, double longitude) {
             this.latitude = latitude;
             this.longitude = longitude;
         }
     }
     /** Helper function for native method getLocation() */
     @SuppressWarnings("unused")
     private static void invokeLocationCallback(double latitude,
                                                double longitude,
                                                AsyncCallback<GeoPt> async) {
         async.onSuccess(new GeoPt(latitude, longitude));
     }
     /** Helper function for native method getLocation() */
     @SuppressWarnings("unused")
     private static void invokeLocationCallbackError(String message,
                                                     AsyncCallback<GeoPt> async){
         async.onFailure(new RuntimeException(message));
     }
     /** Ask for location.  Unfortunately, it looks like the async callback is
      *  simply never invoked if permission to use the location is denied by
      *  the user.
      */
     public static native void getLocation(AsyncCallback<GeoPt> async) /*-{
         var cb = function(lat, lng) {
           @net.cscott.sdr.webapp.client.SDRweb::invokeLocationCallback(DDLcom/google/gwt/user/client/rpc/AsyncCallback;)(lat, lng, async);
         };
         var err = function(msg) {
           @net.cscott.sdr.webapp.client.SDRweb::invokeLocationCallbackError(Ljava/lang/String;Lcom/google/gwt/user/client/rpc/AsyncCallback;)(msg, async);
         };
         try {
           navigator.geolocation.getCurrentPosition(function(position, perror) {
             if (position)
               cb(position.coords.latitude, position.coords.longitude);
             else
               err("Geolocation error");
           });
         } catch (e) {
             err("Geolocation not supported");
         }
     }-*/;
 
     /** Callback interface which handles errors (in a very simplistic way). */
     static abstract class EAsyncCallback<T> implements AsyncCallback<T> {
         public void onFailure(Throwable caught) {
             Window.alert(caught.getMessage());
         }
         public abstract void onSuccess(T result);
     }
     /** Callback interface which retries if the user is not yet logged in. */
     abstract class LAsyncCallback<T> extends EAsyncCallback<T> {
         @Override
         public final void onFailure(Throwable error) {
             if (error instanceof NotLoggedInException) {
                 NotLoggedInException nlie = (NotLoggedInException) error;
                 new SdrPopup("Login to Google", nlie.loginUrl) {
                     @Override
                     public void onClose() { retry(); }
                 };
                 return;
             }
             super.onFailure(error);
         }
         public abstract void retry();
     }
     public interface SdrImageBundle extends ClientBundle {
         @Source("icon_add.png")
         public ImageResource icon_add();
         @Source("icon_close_button.png")
         public ImageResource icon_close_button();
         @Source("icon_media_pause.png")
         public ImageResource icon_media_pause();
         @Source("icon_media_play.png")
         public ImageResource icon_media_play();
     }
     public final SdrImageBundle imageBundle = GWT.create(SdrImageBundle.class);
 }
