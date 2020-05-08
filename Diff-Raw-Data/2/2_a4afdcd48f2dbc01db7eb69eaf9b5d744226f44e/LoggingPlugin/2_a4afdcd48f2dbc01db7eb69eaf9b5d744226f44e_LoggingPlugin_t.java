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
 
 package com.dmdirc.addons.logging;
 
 import com.dmdirc.plugins.PluginInfo;
 import com.dmdirc.plugins.implementations.BaseCommandPlugin;
 
 import dagger.ObjectGraph;
 
 /**
  * Adds logging facility to client.
  */
 public class LoggingPlugin extends BaseCommandPlugin {
 
     /** The manager in use. */
     private LoggingManager manager;
 
     @Override
     public void load(final PluginInfo pluginInfo, final ObjectGraph graph) {
         super.load(pluginInfo, graph);
 
        setObjectGraph(graph.plus(new LoggingModule(pluginInfo)));
         manager = getObjectGraph().get(LoggingManager.class);
 
         registerCommand(LoggingCommand.class, LoggingCommand.INFO);
     }
 
     @Override
     public void onLoad() {
         super.onLoad();
         manager.load();
     }
 
     @Override
     public void onUnload() {
         super.onUnload();
         manager.unload();
     }
 
 }
