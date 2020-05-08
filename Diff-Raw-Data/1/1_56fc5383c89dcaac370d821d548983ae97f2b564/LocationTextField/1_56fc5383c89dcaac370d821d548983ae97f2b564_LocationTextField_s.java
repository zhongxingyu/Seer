 /*
  *
  *  * Licensed to the Apache Software Foundation (ASF) under one or more
  *  * contributor license agreements.  See the NOTICE file distributed with
  *  * this work for additional information regarding copyright ownership.
  *  * The ASF licenses this file to You under the Apache License, Version 2.0
  *  * (the "License"); you may not use this file except in compliance with
  *  * the License.  You may obtain a copy of the License at
  *  *
  *  *      http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software
  *  * distributed under the License is distributed on an "AS IS" BASIS,
  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and
  *  * limitations under the License.
  *
  */
 
 package org.vaadin.addons.locationtextfield;
 
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.event.FieldEvents;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.AbstractTextField;
 import com.vaadin.ui.ClientWidget;
 import com.vaadin.ui.Select;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vaadin.addons.locationtextfield.client.ui.VLocationTextField2;
 
 @ClientWidget(value = VLocationTextField2.class, loadStyle = ClientWidget.LoadStyle.EAGER)
 public class LocationTextField<T extends GeocodedLocation> extends Select {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(LocationTextField.class) ;
     private final transient Map<String, GeocodedLocation> locations = new WeakHashMap<String, GeocodedLocation>();
     private final LocationProvider<T> locationProvider;
 
     /**
      * The text content when the last messages to the server was sent.
      */
     private String lastKnownTextContent;
 
     /**
      * The prompt to display in an empty field. Null when disabled.
      */
     private String inputPrompt;
 
     /**
      * Flag indicating that a text change event is pending to be triggered.
      * Cleared by {@link #setInternalValue(Object)} and when the event is fired.
      */
     private boolean textChangeEventPending;
 
     private AbstractTextField.TextChangeEventMode textChangeEventMode = AbstractTextField.TextChangeEventMode.LAZY;
 
     private final int DEFAULT_TEXTCHANGE_TIMEOUT = 1000;
 
     private int textChangeEventTimeout = DEFAULT_TEXTCHANGE_TIMEOUT;
 
     /**
      * Track whether the value on the server has actually changed to avoid
      * updating the text in the input element on every repaint
      */
     private boolean localValueChanged = true;
 
     private boolean autoSelectOnSingleResult;
     private boolean selecting;
     private boolean enterKeyFiresTextChange;
     private int minTextLength;
     private boolean enterPressed;
     private boolean debugEnabled;
 
     public LocationTextField(LocationProvider<T> locationProvider, Class<T> clazz) {
         this(locationProvider, clazz, null);
     }
 
     @SuppressWarnings("deprecation")
     public LocationTextField(final LocationProvider<T> locationProvider, Class<T> clazz, String caption) {
         super(caption, new BeanItemContainer<T>(clazz));
         this.locationProvider = locationProvider;
         super.setMultiSelect(false);
         super.setFilteringMode(FILTERINGMODE_OFF);
         super.setImmediate(true);
         super.setNewItemsAllowed(false);
         super.setReadOnly(false);
         super.setNullSelectionAllowed(false);
         this.setItemCaptionPropertyId("geocodedAddress");
     }
 
     @Override
     public Class<?> getType() {
         return GeocodedLocation.class;
     }
 
     @Override
     public void setFilteringMode(int filteringMode) {
         // nothing
     }
 
     @Override
     public void setImmediate(boolean immediate) {
         // nothing
     }
 
     @Override
     public void setNewItemsAllowed(boolean newItemsAllowed) {
         // nothing
     }
 
     @Override
     public void setNullSelectionAllowed(boolean nullSelectionAllowed) {
         // nothing
     }
 
     @Override
     public void setReadOnly(boolean readOnly) {
         // nothing
     }
 
     @Override
     @Deprecated
     public void setMultiSelect(boolean multiSelect) {
         // nothing
     }
 
     /**
      * Whether or not pressing the ENTER key inside the text box fires a text change event
      */
     public boolean isEnterKeyFiresTextChange() {
         return this.enterKeyFiresTextChange;
     }
     public void setEnterKeyFiresTextChange(boolean enterKeyFiresTextChange) {
         this.enterKeyFiresTextChange = enterKeyFiresTextChange;
     }
 
     /**
      * Paints the content of this component.
      *
      * @param target
      *            the Paint Event.
      * @throws com.vaadin.terminal.PaintException
      *             if the paint operation failed.
      */
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         super.paintContent(target);
 
         if (localValueChanged) {
             target.addAttribute(VLocationTextField2.ATTR_TEXT_CHANGED, true);
             localValueChanged = false;
         }
 
         if (getInputPrompt() != null) {
             target.addAttribute("prompt", getInputPrompt());
         }
 
         target.addAttribute(VLocationTextField2.ATTR_DEBUG_ENABLED, this.isDebugEnabled());
 
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("Sending " + this.lastKnownTextContent + " as filter string to client");
         target.addVariable(this, VLocationTextField2.FILTER, this.lastKnownTextContent);
 
         target.addAttribute(VLocationTextField2.ATTR_TEXTCHANGE_EVENTMODE, getTextChangeEventMode().toString());
         target.addAttribute(VLocationTextField2.ATTR_TEXTCHANGE_TIMEOUT, getTextChangeTimeout());
         target.addAttribute(VLocationTextField2.ATTR_ENTER_KEY_FIRES_TEXT_CHANGE, this.isEnterKeyFiresTextChange());
     }
 
     @Override
     protected void setValue(Object newValue, boolean repaintIsNotNeeded) throws ReadOnlyException, ConversionException {
         if (notEqual(newValue, super.getValue())) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("values not equal; local value changed");
 
             // The client should use the new value
             repaintIsNotNeeded = !this.setLocationText(this.getStringValue(newValue));
             if (!repaintIsNotNeeded)
                 this.localValueChanged = true;
         }
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("calling super.value(" + newValue + ", " + repaintIsNotNeeded + ")");
         super.setValue(newValue, repaintIsNotNeeded);
     }
 
     private boolean notEqual(Object newValue, Object oldValue) {
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("old: " + oldValue + "; new: " + newValue);
         String str1 = this.getStringValue(oldValue);
         String str2 = this.getStringValue(newValue);
 
         if (str1 == null && str2 == null)
             return false;
         else if (str1 == null)
             return true;
         else if (str2 == null)
             return true;
         return !str1.equals(str2);
     }
 
     private String getStringValue(Object o) {
         if (o instanceof String)
             return (String)o;
         else if (o instanceof GeocodedLocation)
             return ((GeocodedLocation)o).getGeocodedAddress();
         return null;
     }
 
     @SuppressWarnings("unchecked")
     public BeanItemContainer<T> getContainerDataSource() {
         return (BeanItemContainer<T>)super.getContainerDataSource();
     }
 
     /**
      * Convenience method for explicitly setting the location
      * @param location
      */
     @SuppressWarnings("unchecked")
     public void setLocation(T location) {
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("set location called with " + location);
         getContainerDataSource().removeAllItems();
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("container cleared");
         boolean changed;
         if (location != null) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("adding " + location + " to container");
             getContainerDataSource().addBean(location);
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("last known text now = " + location);
             changed = this.setLocationText(location.getGeocodedAddress());
         } else {
             changed = this.setLocationText(null);
         }
         if (changed)
             this.localValueChanged = true;
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("calling super.setValue(" + location + ")");
         super.setValue(location);
     }
 
     protected boolean setLocationText(String address) {
         boolean changed;
         if (this.lastKnownTextContent == null && address == null)
             changed = false;
         else if (this.lastKnownTextContent == null)
             changed = true;
         else if (address == null)
             changed = true;
         else
             changed = !this.lastKnownTextContent.equals(address);
 
         if (changed) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("Changing last known text content from `" + this.lastKnownTextContent + "' to `" + address + "'");
             this.lastKnownTextContent = address;
         } else {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("No change detected; do nothing");
         }
         return changed;
     }
 
     /**
      * Removes all options and resets text field
      */
     public void clear() {
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("clearing location");
         this.setLocation(null);
     }
 
     @Override
     public void changeVariables(Object source, Map<String, Object> variables) {
         final Map<String, Object> map = new HashMap<String, Object>(variables.size() + 1);
         map.putAll(variables);
         map.put("filter", variables.get(VLocationTextField2.FILTER));
         super.changeVariables(source, map);
 
         // Sets the text
         if (map.containsKey(VLocationTextField2.FILTER)) {
 
             // Only do the setting if the string representation of the value
             // has been updated
             String newValue = ("" + map.get(VLocationTextField2.FILTER));
 
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("filter value from client = `" + newValue + "'");
 
             if (this.setLocationText(newValue)) {
                 if (!"".equals(newValue.trim())) {
                     GeocodedLocation newLocation;
                     synchronized (this.locations) {
                         newLocation = this.locations.get(newValue); // this is the geocoded address if a match is found
                     }
                     if (newLocation != null) {
                         if (LOGGER.isTraceEnabled())
                             LOGGER.trace("already geocoded " + newValue + "; selecting " + newLocation);
                         this.select(newLocation);
                     } else {
                         if (LOGGER.isTraceEnabled())
                             LOGGER.trace("triggering geocode of " + newValue);
                         this.textChangeEventPending = true;
                     }
                 } else {
                     this.clear();
                 }
             }
         }
 
         if (map.containsKey(VLocationTextField2.VAR_ENTER_PRESSED))
             this.enterPressed = (Boolean)map.get(VLocationTextField2.VAR_ENTER_PRESSED);
 
         firePendingTextChangeEvent();
 
         if (map.containsKey(FieldEvents.FocusEvent.EVENT_ID)) {
             fireEvent(new FieldEvents.FocusEvent(this));
         }
         if (map.containsKey(FieldEvents.BlurEvent.EVENT_ID)) {
             fireEvent(new FieldEvents.BlurEvent(this));
         }
     }
 
     @Override
     public void select(Object itemId) {
         this.selecting = true;
         try {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("selecting " + itemId);
             super.select(itemId);
         } finally {
             this.selecting = false;
         }
     }
 
     private void firePendingTextChangeEvent() {
         if (textChangeEventPending) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("text change pending; updating");
             textChangeEventPending = false;
             update();
         }
     }
 
     /**
      * Allows developer to set a known address string to be geocoded on the server-side
      * @param address String representation of an address
      */
     public void geocode(String address) {
         if (this.setLocationText(address)) {
             this.textChangeEventPending = true;
             firePendingTextChangeEvent();
         }
     }
 
     /**
      * Whether or not to auto-select a location when there is only one result
      */
     public boolean isAutoSelectOnSingleResult() {
         return autoSelectOnSingleResult;
     }
     public void setAutoSelectOnSingleResult(boolean autoSelectOnSingleResult) {
         this.autoSelectOnSingleResult = autoSelectOnSingleResult;
     }
 
     private void update() {
         try {
             String addr = this.lastKnownTextContent;
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("geocoding " + addr);
             Collection<T> locs;
             if (addr != null && !"".equals(addr.trim())) {
                 addr = addr.trim();
                 if (this.minTextLength <= 0 || addr.length() >= this.minTextLength || this.enterPressed) {
                     locs = this.locationProvider.geocode(addr);
                     this.enterPressed = false;
                 } else {
                     LOGGER.debug("Input address `" + addr + "' is less than the minimum text length of " + this.minTextLength);
                     return;
                 }
             } else
                 locs = Collections.emptyList();
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("found " + locs.size() + " locations");
             getContainerDataSource().removeAllItems();
             synchronized (this.locations) {
                 this.locations.clear();
                 for (T loc : locs) {
                     this.locations.put(loc.getGeocodedAddress(), loc);
                     getContainerDataSource().addBean(loc);
                 }
                 if (this.locations.size() == 1 && isAutoSelectOnSingleResult())
                     this.select(this.locations.values().iterator().next());
             }
             requestRepaint();
         } catch (GeocodingException e) {
             LOGGER.error(e.getMessage(), e);
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public T getValue() {
         Object value = super.getValue();
         if (value instanceof String) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("get value returned String; geocoding");
             this.setValue(value);
             return this.getValue();
         } if (value instanceof GeocodedLocation)
             return (T)value;
         return null;
     }
 
     @Override
     public void setValue(Object value) {
         if (this.selecting) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("selecting; calling super");
             super.setValue(value);
             return;
         }
 
         Object newValue = null;
         if (value == null) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("value is null; clearing");
             this.clear();
             return;
         } else if (value instanceof String) {
             if (LOGGER.isTraceEnabled())
                 LOGGER.trace("value is string; geocoding");
             geocode((String)value);
             final List<Object> itemIds = new ArrayList<Object>(getItemIds());
             final int index = (isNullSelectionAllowed() ? 1 : 0);
             if (itemIds.size() > (isNullSelectionAllowed() ? 1 : 0))
                 newValue = itemIds.get(index);
         } else if (value instanceof GeocodedLocation) {
             newValue = value;
             this.setLocationText(this.getStringValue(value));
         }
         if (LOGGER.isTraceEnabled())
             LOGGER.trace("setting value to " + newValue);
         super.setValue(newValue);
     }
 
     /**
      * Sets the mode how the TextField triggers {@link com.vaadin.event.FieldEvents.TextChangeEvent}s.
      *
      * @param inputEventMode
      *            the new mode
      *
      * @see com.vaadin.ui.AbstractTextField.TextChangeEventMode
      */
     public void setTextChangeEventMode(AbstractTextField.TextChangeEventMode inputEventMode) {
         if (inputEventMode == AbstractTextField.TextChangeEventMode.EAGER)
             inputEventMode = AbstractTextField.TextChangeEventMode.LAZY;
         textChangeEventMode = inputEventMode;
         requestRepaint();
     }
 
     /**
      * @return the mode used to trigger {@link com.vaadin.event.FieldEvents.TextChangeEvent}s.
      */
     public AbstractTextField.TextChangeEventMode getTextChangeEventMode() {
         return textChangeEventMode;
     }
 
     /**
      * The text change timeout modifies how often text change events are
      * communicated to the application when {@link #getTextChangeEventMode()} is
      * {@link com.vaadin.ui.AbstractTextField.TextChangeEventMode#LAZY} or {@link com.vaadin.ui.AbstractTextField.TextChangeEventMode#TIMEOUT}.
      *
      *
      * @see #getTextChangeEventMode()
      *
      * @param timeout
      *            the timeout in milliseconds
      */
     public void setTextChangeTimeout(int timeout) {
         textChangeEventTimeout = timeout;
         requestRepaint();
     }
 
     /**
      * Gets the timeout used to fire {@link com.vaadin.event.FieldEvents.TextChangeEvent}s when the
      * {@link #getTextChangeEventMode()} is {@link com.vaadin.ui.AbstractTextField.TextChangeEventMode#LAZY} or
      * {@link com.vaadin.ui.AbstractTextField.TextChangeEventMode#TIMEOUT}.
      *
      * @return the timeout value in milliseconds
      */
     public int getTextChangeTimeout() {
         return textChangeEventTimeout;
     }
 
     /**
      * Minimum length of text WITHOUT whitespace in order to initiate geocoding
      * @return
      */
     public int getMinTextLength() {
         return this.minTextLength;
     }
     public void setMinTextLength(int minTextLength) {
         this.minTextLength = minTextLength;
     }
 
     /**
      * Gets the current input prompt.
      *
      * @see #setInputPrompt(String)
      * @return the current input prompt, or null if not enabled
      */
     public String getInputPrompt() {
         return this.inputPrompt;
     }
 
     /**
      * Sets the input prompt - a textual prompt that is displayed when the field
      * would otherwise be empty, to prompt the user for input.
      *
      * @param inputPrompt
      */
     public void setInputPrompt(String inputPrompt) {
         boolean condition1 = this.inputPrompt == null && inputPrompt != null;
         boolean condition2 = this.inputPrompt != null && inputPrompt == null;
         if (condition1 || condition2 || !("" + this.inputPrompt).equals("" + inputPrompt)) {
             this.inputPrompt = inputPrompt;
             requestRepaint();
         }
     }
 
     public boolean isDebugEnabled() {
         return this.debugEnabled;
     }
     public void setDebugEnabled(final boolean debugEnabled) {
         if (this.debugEnabled != debugEnabled) {
             this.debugEnabled = debugEnabled;
             this.requestRepaint();
         }
     }
 }
