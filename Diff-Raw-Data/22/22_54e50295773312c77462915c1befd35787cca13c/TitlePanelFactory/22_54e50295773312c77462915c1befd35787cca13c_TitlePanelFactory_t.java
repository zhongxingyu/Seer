 /*
  * Copyright 2009 Sysmap Solutions Software e Consultoria Ltda.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package br.com.sysmap.crux.widgets.rebind.titlepanel;
 
 import br.com.sysmap.crux.core.rebind.widget.declarative.DeclarativeFactory;
import br.com.sysmap.crux.core.rebind.widget.declarative.TagChild;
import br.com.sysmap.crux.core.rebind.widget.declarative.TagChildren;
 import br.com.sysmap.crux.widgets.client.titlepanel.TitlePanel;
 
 /**
  * Factory for Title Panel widget
  * @author Gesse S. F. Dafe
  */
 @DeclarativeFactory(id="titlePanel", library="widgets", targetWidget=TitlePanel.class)
@TagChildren({
	@TagChild(TitlePanelFactory.TitleProcessor.class),
	@TagChild(TitlePanelFactory.BodyProcessor.class)
})
 public class TitlePanelFactory extends AbstractTitlePanelFactory
 {
 }
