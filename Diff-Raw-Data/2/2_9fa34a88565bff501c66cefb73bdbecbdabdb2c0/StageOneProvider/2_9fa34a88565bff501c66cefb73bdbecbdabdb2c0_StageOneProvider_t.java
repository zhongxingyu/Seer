 /**
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 
 package org.waveprotocol.wave.examples.client.webclient.client;
 
 import com.google.gwt.dom.client.Element;
 
 import org.waveprotocol.wave.client.StageOne;
 import org.waveprotocol.wave.client.StageTwo;
 import org.waveprotocol.wave.client.StageZero;
 import org.waveprotocol.wave.client.Stages;
 import org.waveprotocol.wave.client.common.util.AsyncHolder;
 import org.waveprotocol.wave.client.widget.common.LogicalPanel;
 
 /**
  * Stages for loading the undercurrent Wave Panel
  *
  * @author zdwang@google.com (David Wang)
  */
 public class StageOneProvider extends Stages {
 
   private final Element wavePanelElement;
   private final LogicalPanel rootPanel;
 
   /**
    * @param wavePanelElement The dom element to become the wave panel
    * @param rootPanel A panel that this an ancestor of wavePanelElement. This
    *    is used for adopting to the GWT widget tree.
    */
   public StageOneProvider(Element wavePanelElement, LogicalPanel rootPanel) {
     this.wavePanelElement = wavePanelElement;
     this.rootPanel = rootPanel;
   }
 
   @Override
   protected AsyncHolder<StageOne> createStageOneLoader(StageZero zero) {
     return new StageOne.DefaultProvider(zero) {
       @Override
       protected Element createWaveHolder() {
         return wavePanelElement;
       }
 
       @Override
       protected LogicalPanel createWaveContainer() {
         return rootPanel;
       }
     };
   }
 
   @Override
   protected AsyncHolder<StageTwo> createStageTwoLoader(StageOne one) {
    return new StageTwoProvider(one, null);
   }
 
 }
