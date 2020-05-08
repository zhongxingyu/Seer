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
 
 package com.dmdirc.addons.channelwho;
 
 import com.dmdirc.interfaces.Connection;
 import com.dmdirc.interfaces.GroupChat;
 
 /**
  * Responsible for managing timers and settings required to who any {@link GroupChat}s on a
  * {@link Connection} as specified by the user.
  */
 public class ConnectionHandler {
 
     private final Connection connection;
    private final String domain;
 
    public ConnectionHandler(final Connection connection, final String domain) {
         this.connection = connection;
        this.domain = domain;
     }
 
     public void load() {
         connection.getWindowModel().getEventBus().subscribe(this);
     }
 
     public void unload() {
         connection.getWindowModel().getEventBus().unsubscribe(this);
     }
 }
