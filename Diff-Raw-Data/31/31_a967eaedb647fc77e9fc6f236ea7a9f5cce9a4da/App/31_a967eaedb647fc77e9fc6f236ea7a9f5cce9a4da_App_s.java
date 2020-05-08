 /*
  * Copyright 2009 JBoss, a division of Red Hat Hat, Inc
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.ocpsoft.tutorial.regex.client.local;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.api.ErrorCallback;
 import org.jboss.errai.bus.client.api.Message;
 import org.jboss.errai.bus.client.api.RemoteCallback;
 import org.jboss.errai.ioc.client.api.Caller;
 import org.jboss.errai.ioc.client.api.EntryPoint;
 import org.jboss.errai.ui.shared.api.annotations.DataField;
 import org.jboss.errai.ui.shared.api.annotations.EventHandler;
 import org.jboss.errai.ui.shared.api.annotations.Templated;
 import org.ocpsoft.tutorial.regex.client.shared.Highlighter;
 import org.ocpsoft.tutorial.regex.client.shared.RegexParser;
 import org.ocpsoft.tutorial.regex.client.shared.RegexRequest;
 import org.ocpsoft.tutorial.regex.client.shared.RegexResult;
 
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 
 /**
  * Main application entry point.
  */
 @EntryPoint
 @Templated("Mockup.html#app-template")
 public class App extends Composite
 {
    private static final int TIMEOUT = 150;
 
    Highlighter highlighter = new Highlighter();
 
    @Inject
    @DataField
    private TextArea text;
 
    @Inject
    @DataField
    private TextArea regex;
 
    @Inject
    @DataField
    private TextArea replacement;
 
    @Inject
    @DataField
    private Label result;
 
    @Inject
    @DataField
    private Label replaced;
 
    @Inject
    @DataField
    private Label error;
 
    @PostConstruct
    private final void init()
    {
       error.setVisible(false);
       replaced.setVisible(false);
    }
 
    @Override
    protected void onAttach()
    {
       super.onAttach();
       initAutogrow();
    }
 
    private native void initAutogrow()
    /*-{
        var $opts = {within: 0, by: 1, interval: 100, duration: 'fast'};
        $wnd.$('#text').css('overflow', 'hidden').expandable($opts)
        $wnd.$('#regex').css('overflow', 'hidden').expandable($opts)
        $wnd.$('#replacement').css('overflow', 'hidden').expandable($opts)
     }-*/;
 
    @Inject
    private Caller<RegexParser> parser;
    private Timer timer;
    private RegexRequest request;
 
    @EventHandler({ "text", "regex", "replacement" })
    void handleUpdate(KeyUpEvent event)
    {
       if (timer != null)
          timer.cancel();
 
       final RegexRequest update = new RegexRequest(
                text.getText(),
                regex.getText(),
                replacement.getText()

               // JsonUtils.escapeValue(text.getText()),
               // JsonUtils.escapeValue(regex.getText()),
               // JsonUtils.escapeValue(replacement.getText())
                );
 
       if (!update.equals(request))
       {
          request = update;
          timer = new Timer() {
 
             @Override
             public void run()
             {
                timer = null;
                parser.call(callback, errorCallback).parse(request);
             }
          };
       }
       timer.schedule(TIMEOUT);
 
    }
 
    public void handleResult(RegexResult event)
    {
       if (event.getError() != null && !event.getError().isEmpty())
       {
          error.setText(event.getError());
          error.setVisible(true);
          result.removeStyleName("matches");
       }
       else
       {
          error.setVisible(false);
 
          result.getElement().setInnerHTML(highlighter.highlight(event.getText(), event));
          if (event.isMatches())
          {
             result.addStyleName("matches");
          }
          else
             result.removeStyleName("matches");
       }
 
       if (event.getReplaced() != null && !event.getReplaced().isEmpty())
       {
          replaced.setText(event.getReplaced());
          replaced.setVisible(true);
       }
       else
          replaced.setVisible(false);
 
    }
 
    @PostConstruct
    public void setup()
    {
       RootPanel.get().add(this);
    }
 
    RemoteCallback<RegexResult> callback = new RemoteCallback<RegexResult>() {
       public void callback(RegexResult value)
       {
          handleResult(value);
       }
    };
 
    ErrorCallback errorCallback = new ErrorCallback() {
       @Override
       public boolean error(Message message, Throwable throwable)
       {
          return false;
       }
    };
 
 }
