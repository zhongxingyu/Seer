 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.trickl.crawler.robot;
 
 import com.trickl.crawler.api.Droid;
 import com.trickl.crawler.api.Parser;
 import com.trickl.crawler.api.Task;
 import com.trickl.crawler.api.Worker;
 import com.trickl.crawler.handle.LinkExtractor;
 import com.trickl.crawler.handle.TaskResultHandler;
 import com.trickl.crawler.parser.ParserFactory;
 import com.trickl.crawler.parser.html.NekoHtmlParser;
 import com.trickl.crawler.parser.json.JsonParser;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import org.apache.droids.exception.DroidsException;
 
 public class StandardDroid<T extends Task> implements Droid<T>
 {
    protected List<TaskResultHandler> outputHandlers = new
            ArrayList<TaskResultHandler>();
    
    protected ParserFactory parserFactory;
    
    protected boolean forceAllow;
 
    public StandardDroid()
    {
       parserFactory = new ParserFactory();
       Parser htmlParser = new NekoHtmlParser();
       parserFactory.setMap(new HashMap<String, Object>());
       parserFactory.getMap().put("text/html", htmlParser);
       parserFactory.getMap().put("application/json", new JsonParser());      
    }
    
    public boolean getForceAllow() {
       return forceAllow;
    }
 
    public void setForceAllow(boolean forceAllow)
    {
       this.forceAllow = forceAllow;
    }
 
   public void setOutputHandler(List<TaskResultHandler> outputHandlers)
    {
       this.outputHandlers = outputHandlers;
    }
 
    public ParserFactory getParserFactory()
    {
       return parserFactory;
    }
 
    @Override
    public Worker<T> getNewWorker() {
       final StandardWorker<T> worker = new StandardWorker<T>(this);
 
       return worker;
    }
 
    /**
     * @return the outputHandler
     */
    public List<TaskResultHandler> getOutputHandlers() {
       return outputHandlers;
    }
 }
