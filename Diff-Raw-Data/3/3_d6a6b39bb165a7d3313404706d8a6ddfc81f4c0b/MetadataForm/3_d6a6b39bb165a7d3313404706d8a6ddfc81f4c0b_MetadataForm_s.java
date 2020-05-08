 /*
  * Copyright 2009 Andrew Pietsch 
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you 
  * may not use this file except in compliance with the License. You may 
  * obtain a copy of the License at 
  *      
  *      http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing permissions 
  * and limitations under the License. 
  */
 
 package com.pietschy.gwt.pectin.demo.client.metadata;
 
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.pietschy.gwt.pectin.client.binding.WidgetBinder;
 import com.pietschy.gwt.pectin.client.components.AbstractDynamicList;
 import com.pietschy.gwt.pectin.client.components.ComboBox;
 import com.pietschy.gwt.pectin.client.components.EnhancedTextBox;
 import com.pietschy.gwt.pectin.client.components.NullSafeCheckBox;
 import com.pietschy.gwt.pectin.client.format.DisplayFormat;
 import com.pietschy.gwt.pectin.client.metadata.binding.MetadataBinder;
 import com.pietschy.gwt.pectin.demo.client.domain.Protocol;
 import com.pietschy.gwt.pectin.demo.client.domain.Wine;
 import com.pietschy.gwt.pectin.demo.client.misc.NickNameEditor;
 import com.pietschy.gwt.pectin.demo.client.misc.VerySimpleForm;
 
 /**
  * 
  */
 public class MetadataForm extends VerySimpleForm
 {
    private TextBox givenName = new TextBox();
    private TextBox surname = new TextBox();
 
    private ComboBox<Protocol> protocol = new ComboBox<Protocol>(Protocol.values());
    private TextBox port = new EnhancedTextBox();
    private Label defaultPortLabel = createHint();
 
    private CheckBox hasNickName = new CheckBox("I have a nick name");
    // NickNameEditor is an example of a custom HasValue<T> widget.
    private NickNameEditor nickName = new NickNameEditor();
 
    // value models can contain nulls so if we need a null safe check
    // box if we're binding directly to one.
    private CheckBox wineLover = new NullSafeCheckBox("I like wine");
    private CheckBox hasFavoriteWines = new NullSafeCheckBox("I have favorite wines");
    // the containingValue and withValue bindings never use nulls so we
    // can use regular check boxes.
    private CheckBox cabSavCheckBox = new CheckBox("Cab Sav");
    private CheckBox merlotCheckBox = new CheckBox("Merlot");
    private CheckBox shirazCheckBox = new CheckBox("Shiraz");
 
    private CheckBox cheeseLover = new NullSafeCheckBox("I like cheese");
    private AbstractDynamicList<String> favoriteCheeses = new AbstractDynamicList<String>("Add Cheese")
    {
       protected HasValue<String> createWidget()
       {
          return new TextBox();
       }
    };
    
    private WidgetBinder widgets = new WidgetBinder();
    private MetadataBinder metadataBinder = new MetadataBinder();
 
 
    public MetadataForm(MetadataFormModel model)
    {
       protocol.setRenderer(new ProtocolRenderer());
       port.setVisibleLength(5);
 
       widgets.bind(model.givenName).to(givenName);
       widgets.bind(model.surname).to(surname);
 
       widgets.bind(model.protocol).to(protocol);
       widgets.bind(model.port).to(port);
       widgets.bind(model.defaultPort).toLabel(defaultPortLabel).withFormat(new DisplayFormat<Integer>()
       {
          public String format(Integer port)
          {
             return port != null ? "(the default is " + port + ")" : "";
          }
       });
 
       widgets.bind(model.hasNickName).to(hasNickName);
       widgets.bind(model.nickName).to(nickName);
       
       widgets.bind(model.wineLover).to(wineLover);
       widgets.bind(model.hasFavoriteWines).to(hasFavoriteWines);
 
       widgets.bind(model.favoriteWines).containingValue(Wine.CAB_SAV).to(cabSavCheckBox);
       widgets.bind(model.favoriteWines).containingValue(Wine.MERLOT).to(merlotCheckBox);
       widgets.bind(model.favoriteWines).containingValue(Wine.SHIRAZ).to(shirazCheckBox);
 
       widgets.bind(model.cheeseLover).to(cheeseLover);
       widgets.bind(model.favoriteCheeses).to(favoriteCheeses);
 
 
       addRow("First Name", givenName, "The first two fields use a plain text watermark");
       addRow("Last Name", surname);
 
       addGap();
       addNote("The following shows a dynamically changing watermark for the port field " +
               "based on the selected protocol.");
       addNote("The default port is also displayed if the user enters a port value that isn't the default.");
       addRow("Protocol", protocol);
       addRow("Port", port, defaultPortLabel);
 
       addGap();
       addRow("", hasNickName);
       addRow("Nick name", nickName);
       addGap();
       addNote("The favorites list is only enabled only if you're a wine lover with favorites.");
       addRow("Wine", wineLover);
       addRow("", hasFavoriteWines);
       addRow("Favorites", cabSavCheckBox, merlotCheckBox, shirazCheckBox);
 
       addGap();
       addNote("Now we'll show and hide a field based on the checkbox value.");
       addRow("Cheese", cheeseLover);
       Row favoriteCheeseRow = addTallRow("Favorites", favoriteCheeses);
 
       // Now lets hide the whole row based on the metadata for the field.
       metadataBinder.show(favoriteCheeseRow).usingMetadataOf(model.favoriteCheeses);

      // Please Nte: Normally if I was hiding the whole row like above I'd probably
       // use metadataBinder.show(favoriteCheeseRow).when(cheeseLover) and
       // not bother using metadata at all.
 
    }
 
    private static class ProtocolRenderer implements ComboBox.Renderer<Protocol>
    {
       public String toDisplayString(Protocol protocol)
       {
          return protocol != null ? protocol.getDisplayName() : "";
       }
    }
 }
