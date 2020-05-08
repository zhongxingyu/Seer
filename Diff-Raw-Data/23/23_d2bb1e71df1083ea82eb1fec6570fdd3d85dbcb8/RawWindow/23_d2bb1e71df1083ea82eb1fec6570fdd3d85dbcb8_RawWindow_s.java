 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.debug;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.CommandType;
 import com.dmdirc.events.ServerConnectingEvent;
 import com.dmdirc.interfaces.Connection;
import com.dmdirc.parser.common.CallbackManager;
 import com.dmdirc.parser.interfaces.Parser;
import com.dmdirc.parser.interfaces.callbacks.DataInListener;
import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
 import com.dmdirc.ui.core.components.WindowComponent;
 import com.dmdirc.ui.input.TabCompleterFactory;
 import com.dmdirc.ui.messages.BackBufferFactory;
 import com.dmdirc.ui.messages.sink.MessageSinkManager;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Optional;
 
 import net.engio.mbassy.listener.Handler;
 
 /**
  * Shows the raw lines to and from a connection.
  */
 public class RawWindow extends FrameContainer {
 
     private final Connection connection;
 
     public RawWindow(
             final Connection connection,
             final MessageSinkManager messageSinkManager,
             final TabCompleterFactory tabCompleterFactory,
             final BackBufferFactory backBufferFactory) {
         super(connection.getWindowModel(), "raw", "Raw", "(Raw log)",
                 connection.getWindowModel().getConfigManager(),
                 backBufferFactory,
                 tabCompleterFactory.getTabCompleter(connection.getWindowModel().getTabCompleter(),
                         connection.getWindowModel().getConfigManager(),
                         CommandType.TYPE_QUERY, CommandType.TYPE_CHAT),
                 messageSinkManager,
                 connection.getWindowModel().getEventBus(),
                 Arrays.asList(WindowComponent.TEXTAREA.getIdentifier(),
                         WindowComponent.INPUTFIELD.getIdentifier()));
         this.connection = connection;
         initBackBuffer();
 
         connection.getWindowModel().getEventBus().subscribe(this);
        connection.getParser().map(Parser::getCallbackManager).ifPresent(this::addCallbacks);
     }
 
     @Override
     public Optional<Connection> getConnection() {
         return Optional.of(connection);
     }
 
     @Override
     public void close() {
        connection.getParser().map(Parser::getCallbackManager).ifPresent(this::removeCallbacks);
         connection.getWindowModel().getEventBus().unsubscribe(this);
         super.close();
     }
 
     @Override
     public int getMaxLineLength() {
         return -1;
     }
 
     @Handler
     public void handleServerConnecting(final ServerConnectingEvent connectingEvent) {
        connection.getParser().map(Parser::getCallbackManager).ifPresent(this::addCallbacks);
    }

    private void addCallbacks(final CallbackManager callbackManager) {
        callbackManager.addCallback(DataInListener.class, this::handleDataIn);
        callbackManager.addCallback(DataOutListener.class, this::handleDataOut);
    }

    private void removeCallbacks(final CallbackManager callbackManager) {
        callbackManager.delCallback(DataInListener.class, (DataInListener) this::handleDataIn);
        callbackManager.delCallback(DataOutListener.class, (DataOutListener) this::handleDataOut);
     }
 
     private void handleDataIn(final Parser parser, final Date date, final String line) {
         addLine("rawIn", date, line);
     }
 
     private void handleDataOut(final Parser parser, final Date date, final String line,
             final boolean fromParser) {
         addLine("rawOut", date, line);
     }
 
 }
