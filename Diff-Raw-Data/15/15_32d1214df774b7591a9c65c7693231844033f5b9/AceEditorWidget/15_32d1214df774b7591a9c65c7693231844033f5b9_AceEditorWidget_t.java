 package org.rstudio.studio.client.workbench.views.source.editors.text;
 
 import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.dom.client.*;
 import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.RequiresResize;
 import org.rstudio.core.client.BrowseCap;
 import org.rstudio.core.client.widget.FontSizer;
 import org.rstudio.studio.client.server.Void;
 import org.rstudio.studio.client.workbench.views.source.editors.text.ace.AceEditorNative;
 import org.rstudio.studio.client.workbench.views.source.editors.text.events.EditorLoadedEvent;
 import org.rstudio.studio.client.workbench.views.source.editors.text.events.EditorLoadedHandler;
 
 public class AceEditorWidget extends Composite
       implements RequiresResize,
                  HasValueChangeHandlers<Void>,
                  HasKeyDownHandlers
 {
    public AceEditorWidget()
    {
       initWidget(new HTML());
       FontSizer.applyNormalFontSize(this);
       setSize("100%", "100%");
 
       editor_ = AceEditorNative.createEditor(getElement());
       editor_.setShowPrintMargin(false);
       editor_.setPrintMarginColumn(0);
       editor_.setHighlightActiveLine(false);
       editor_.delegateEventsTo(AceEditorWidget.this);
       editor_.onChange(new Command()
       {
          public void execute()
          {
             ValueChangeEvent.fire(AceEditorWidget.this, null);
          }
       });
    }
 
    public AceEditorNative getEditor() {
       return editor_;
    }
 
    @Override
    protected void onLoad()
    {
       super.onLoad();
 
       fireEvent(new EditorLoadedEvent());
       Scheduler.get().scheduleDeferred(new ScheduledCommand()
       {
          public void execute()
          {
             onResize();
          }
       });

      // On Windows desktop sometimes we inexplicably end up at the wrong size
      // if the editor is being resized while it's loading (such as when a new
      // document is created while the source pane is hidden)
      Scheduler.get().scheduleFixedDelay(new RepeatingCommand()
      {
         public boolean execute()
         {
            if (isAttached())
               onResize();
            return false;
         }
      }, 500);
    }
 
    public void onResize()
    {
       editor_.resize();
    }
 
    public void onActivate()
    {
       if (BrowseCap.INSTANCE.aceVerticalScrollBarIssue() && editor_ != null)
          editor_.getRenderer().forceScrollbarUpdate();
    }
 
    public void setCode(String code)
    {
       editor_.getSession().setValue(code);
    }
 
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Void> handler)
    {
       return addHandler(handler, ValueChangeEvent.getType());
    }
 
    public HandlerRegistration addFocusHandler(FocusHandler handler)
    {
       return addHandler(handler, FocusEvent.getType());
    }
 
    public HandlerRegistration addBlurHandler(BlurHandler handler)
    {
       return addHandler(handler, BlurEvent.getType());
    }
 
    public HandlerRegistration addClickHandler(ClickHandler handler)
    {
       return addDomHandler(handler, ClickEvent.getType());
    }
 
    public HandlerRegistration addEditorLoadedHandler(EditorLoadedHandler handler)
    {
       return addHandler(handler, EditorLoadedEvent.TYPE);
    }
 
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler)
    {
       return addHandler(handler, KeyDownEvent.getType());
    }
 
    private final AceEditorNative editor_;
 }
