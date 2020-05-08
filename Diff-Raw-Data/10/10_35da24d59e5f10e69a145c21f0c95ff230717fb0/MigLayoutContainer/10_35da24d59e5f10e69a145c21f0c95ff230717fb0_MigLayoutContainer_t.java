 /*
  * Copyright (c) 2013, Harald Westphal
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the author nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
  * DAMAGE.
  */
 package net.miginfocom.layout.gxt3;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.miginfocom.layout.AC;
 import net.miginfocom.layout.CC;
 import net.miginfocom.layout.ComponentWrapper;
 import net.miginfocom.layout.ConstraintParser;
 import net.miginfocom.layout.Grid;
 import net.miginfocom.layout.LC;
 import net.miginfocom.layout.LayoutCallback;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.uibinder.client.UiChild;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.util.Rectangle;
 import com.sencha.gxt.widget.core.client.container.InsertResizeContainer;
 
 /**
  * A layout container that lays out its children using the MiG Layout Manager.
  * 
  * <p/>
  * Code Snippet:
  * 
  * <pre>
  * MigLayoutContainer container = new MigLayoutContainer();
  * container.add(new Label(&quot;First Name&quot;, false));
  * container.add(new TextField());
  * container.add(new Label(&quot;Surname&quot;), &quot;gap unrelated&quot;);
  * container.add(new TextField(), &quot;wrap&quot;);
  * container.add(new Label(&quot;Address&quot;));
  * container.add(new TextField(), &quot;span, grow&quot;);
  * RootLayoutPanel.get().add(container);
  * </pre>
  * 
  * @see <a href="http://www.miglayout.com/">http://www.miglayout.com/</a>
  */
 public final class MigLayoutContainer extends InsertResizeContainer {
 
 	private final ArrayList<LayoutCallback> layoutCallbacks = new ArrayList<LayoutCallback>();
 	private final Map<ComponentWrapper, CC> ccMap = new HashMap<ComponentWrapper, CC>();
 	private final Map<Widget, ComponentWrapper> widgetMap = new HashMap<Widget, ComponentWrapper>();
 	private final GxtContainerWrapper containerWrapper;
 	private final LC layoutConstraints;
 	private final AC colConstraints;
 	private final AC rowConstraints;
 
 	private Grid grid;
 	private boolean secondPassRequired;
 	private boolean debug;
 
 	public MigLayoutContainer() {
 		this("");
 	}
 
 	public MigLayoutContainer(LC layoutConstraints) {
 		this(layoutConstraints, null);
 	}
 
 	public MigLayoutContainer(LC layoutConstraints, AC colConstraints) {
 		this(layoutConstraints, colConstraints, null);
 	}
 
 	public MigLayoutContainer(String layoutConstraints) {
 		this(layoutConstraints, null);
 	}
 
 	public MigLayoutContainer(String layoutConstraints, String colConstraints) {
 		this(layoutConstraints, colConstraints, null);
 	}
 
 	public MigLayoutContainer(String layoutConstraints, String colConstraints, String rowConstraints) {
 		this(ConstraintParser.parseLayoutConstraint(ConstraintParser.prepare(layoutConstraints)), ConstraintParser.parseColumnConstraints(ConstraintParser
 				.prepare(colConstraints)), ConstraintParser.parseRowConstraints(ConstraintParser.prepare(rowConstraints)));
 	}
 
 	public boolean isDebug() {
 		return debug;
 	}
 
 	public void setDebug(boolean debug) {
 		this.debug = debug;
 	}
 
 	public MigLayoutContainer(LC layoutConstraints, AC colConstraints, AC rowConstraints) {
 		this.layoutConstraints = layoutConstraints;
 		this.colConstraints = colConstraints;
 		this.rowConstraints = rowConstraints;
 		containerWrapper = new GxtContainerWrapper(this);
 		setElement(DOM.createDiv());
 		getContainerTarget().makePositionable(true);
 	}
 
 	public void addLayoutCallback(LayoutCallback callback) {
 		if (!layoutCallbacks.contains(callback)) {
 			layoutCallbacks.add(callback);
 		}
 	}
 
 	public void removeLayoutCallback(LayoutCallback callback) {
 		layoutCallbacks.remove(callback);
 	}
 
 	@UiChild(tagname = "child")
 	public void add(IsWidget child, String layoutData) {
 		child.asWidget().setLayoutData(layoutData);
 		add(child);
 	}
 
 	public void add(IsWidget child, CC layoutData) {
 		child.asWidget().setLayoutData(layoutData);
 		add(child);
 	}
 
 	public void insert(IsWidget child, int beforeIndex, String layoutData) {
 		child.asWidget().setLayoutData(layoutData);
 		insert(child, beforeIndex);
 	}
 
 	public void insert(IsWidget child, int beforeIndex, CC layoutData) {
 		child.asWidget().setLayoutData(layoutData);
 		insert(child, beforeIndex);
 	}
 
 	public void layout() {
 		layoutCommand.execute();
 	}
 
 	@Override
 	protected void doLayout() {
 		if (secondPassRequired) {
 			secondPassRequired = false;
 			Scheduler.get().scheduleDeferred(layoutCommand);
 			return;
 		}
 
 		if (grid == null) {
 			for (int i = 0; i < getWidgetCount(); i++) {
 				Widget widget = getWidget(i);
 				if (!widgetMap.containsKey(widget)) {
 					wrapWidget(widget);
 				}
 			}
 			grid = new Grid(containerWrapper, layoutConstraints, rowConstraints, colConstraints, ccMap, layoutCallbacks);
 		}
 		
 		grid.layout(new int[] { 0, 0, getOffsetWidth(true), getOffsetHeight(true) }, null, null, debug, false);
		containerWrapper.removeDebugOverlays();
 		if (debug) {
 			grid.paintDebug();
 		}
 	}
 
 	private void wrapWidget(Widget widget) {
 		ComponentWrapper wrapper = new GxtComponentWrapper(widget, containerWrapper, widget.getOffsetWidth(), widget.getOffsetHeight());
 		widgetMap.put(widget, wrapper);
 
 		Object layoutData = widget.getLayoutData();
 		CC cc = null;
 		if (layoutData instanceof String) {
 			cc = ConstraintParser.parseComponentConstraint(ConstraintParser.prepare((String) layoutData));
 		} else if (layoutData instanceof CC) {
 			cc = (CC) layoutData;
 		}
 		if (cc != null) {
 			ccMap.put(wrapper, cc);
 		}
 	}
 
 	@Override
 	protected void onInsert(int index, Widget child) {
 		child.getElement().getStyle().setPosition(Position.ABSOLUTE);
 		grid = null;
 		secondPassRequired = true;
 	}
 
 	@Override
 	protected void onRemove(Widget child) {
 		ComponentWrapper wrapper = widgetMap.remove(child);
 		if (wrapper != null) {
 			ccMap.remove(wrapper);
 		}
 		grid = null;
 	}
 
 	void applyLayout(Widget widget, int x, int y, int width, int height) {
 		applyLayout(widget, new Rectangle(x, y, width, height));
 	}
 
 	ComponentWrapper[] getComponents() {
 		ComponentWrapper[] components = new ComponentWrapper[getWidgetCount()];
 		for (int i = 0; i < getWidgetCount(); i++) {
 			components[i] = widgetMap.get(getWidget(i));
 		}
 		return components;
 	}
 
 }
