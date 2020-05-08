 /*******************************************************************************
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package pennapps2013.where2meet.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import java.util.ArrayList;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.scribe.model.Request;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class where2meet implements EntryPoint {
 	
 	public static final String GEOCODE = "http://maps.googleapis.com/maps/api/geocode/json";
 	
     private VerticalPanel mainPanel;
     private FlexTable addressFlexTable;
     private HorizontalPanel addPanel;
     private TextBox newAddressTextBox;
     private Button addButton;
     private ArrayList <String> addresses = new ArrayList<String>();
     private Label where2meetLabel;
     private Button locateButton;
     
     public static LatLng geocode(String address) {
 		Request request = new Request(Verb.GET, GEOCODE);
 		request.addQuerystringParameter("address", address.replace(' ', '+'));
 		request.addQuerystringParameter("sensor", "false");
 		Response response = request.send();
 		try {
 			JSONObject json = (JSONObject)(new JSONParser()).parse(response.getBody());
			if (!"OK".equals(json.get("status")))
				return null;
			
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println(response.getBody());
 		return null;
     }
     
     public void onModuleLoad() {
         RootPanel rootPanel = RootPanel.get();
 
         mainPanel = new VerticalPanel();
         mainPanel.setStylePrimaryName("gwt-Panel-main");
         mainPanel.setStyleName("body");
         rootPanel.add(mainPanel, 10, 10);
         mainPanel.setSize("250px", "200px");
 
         // where2meetLabel = new Label("where2meet");
         // where2meetLabel.setStylePrimaryName("gwt-Label-where2meet");
         // where2meetLabel.setStyleName("gwt-Label-where2meet");
         // mainPanel.add(where2meetLabel);
 
         addressFlexTable = new FlexTable();
         addressFlexTable.setText(0, 0, "Address");
         addressFlexTable.setText(0, 1, "Remove");
 
         // // Add styles to elements in the address list table.
         // addressFlexTable.setCellPadding(6);
         // addressFlexTable.getRowFormatter().addStyleName(0, "addressListHeader");
         // addressFlexTable.addStyleName("addressList");
         // addressFlexTable.getCellFormatter().addStyleName(0, 1, "addressListRemoveColumn");
         addressFlexTable.setStyleName("table");
         addressFlexTable.setStylePrimaryName("table");
 
         mainPanel.add(addressFlexTable);
         mainPanel.setCellHorizontalAlignment(addressFlexTable, HasHorizontalAlignment.ALIGN_CENTER);
 
         addPanel = new HorizontalPanel();
         mainPanel.add(addPanel);
         mainPanel.setCellHorizontalAlignment(addPanel, HasHorizontalAlignment.ALIGN_CENTER);
 
         newAddressTextBox = new TextBox();
         newAddressTextBox.addKeyPressHandler(new KeyPressHandler() {
             public void onKeyPress(KeyPressEvent event) {
                 if (event.getCharCode() == KeyCodes.KEY_ENTER){
                     addAddress();
                 }
             }
         });
         newAddressTextBox.setFocus(true);
         addPanel.add(newAddressTextBox);
 
         addButton = new Button("New button");
         addButton.setStylePrimaryName("btn");
         addButton.setStyleName("btn");
         addButton.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 addAddress();
             }
         });
         addButton.setText("Add");
         addPanel.add(addButton);
         // addPanel.addStyleName("addPanel");
 
         locateButton = new Button("New button");
         locateButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				//TODO convert addresses into geo cords
 				//TODO call nicks functions which should hopefully trigger map, etc.
 				int rowcount = addressFlexTable.getRowCount();
 				LatLng[] coords = new LatLng[rowcount - 1];
 				for (int row = 1; row < rowcount; row++) {
 					//?address=200+S+33rd+St,+Philadelphia,+PA+19104&sensor=false
 					//GET GEO Coords from google and parse to get lat and long
 					String address = addressFlexTable.getText(row, 0);
 					LatLng templatlng = new LatLng(1, 1);
 					coords[row - 1] = templatlng;
 				}
 				//Call nicks coords to businesses function
 			}
 		});
         locateButton.setStylePrimaryName("btn");
         locateButton.setStyleName("btn");
         locateButton.setText("Locate!");
         mainPanel.add(locateButton);
         mainPanel.setCellHorizontalAlignment(locateButton, HasHorizontalAlignment.ALIGN_CENTER);
     }
     private void addAddress() {
         final String address = newAddressTextBox.getText().trim();
         newAddressTextBox.setFocus(true);
 
         // Address must be between 1 and 10 chars that are numbers, letters, or dots.
         if (!address.matches("^[0-9a-zA-Z ]{1,100}$")) {
           Window.alert("'" + address + "' is not a valid address.");
           newAddressTextBox.selectAll();
           return;
         }
 
         newAddressTextBox.setText("");
 
         // Don't add the address if it's already in the table.
         if (addresses.contains(address))
             return;
 
         // Add the address to the table.
         int row = addressFlexTable.getRowCount();
         addresses.add(address);
         addressFlexTable.setText(row, 0, address);
         //addressFlexTable.getCellFormatter().addStyleName(row, 1, "addressListRemoveColumn");
 
         // Add a button to remove this address from the table.
         Button removeAddress = new Button("x");
         //removeAddress.addStyleDependentName("remove");
         removeAddress.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
             int removedIndex = addresses.indexOf(address);
             addresses.remove(removedIndex);
             addressFlexTable.removeRow(removedIndex + 1);
         }
         });
         addressFlexTable.setWidget(row, 1, removeAddress);
     }
     
     public static void main(String[] args) {
     	where2meet.geocode("dsafsa");
     }
 }
