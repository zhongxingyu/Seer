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
 import java.util.List;
 
 import net.miginfocom.layout.ComponentWrapper;
 import net.miginfocom.layout.ContainerWrapper;
 
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.BorderStyle;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.core.client.dom.XElement;
 
 class GxtContainerWrapper extends GxtComponentWrapper implements ContainerWrapper {
 
 	private final MigLayoutContainer container;
 	private final List<XElement> debugOverlays = new ArrayList<XElement>();
 
 	GxtContainerWrapper(MigLayoutContainer container) {
 		super(container);
 		this.container = container;
 	}
 
 	@Override
 	public ComponentWrapper[] getComponents() {
 		return container.getComponents();
 	}
 
 	@Override
 	public int getComponentCount() {
 		return container.getWidgetCount();
 	}
 
 	@Override
 	public Object getLayout() {
 		return container;
 	}
 
 	@Override
 	public boolean isLeftToRight() {
 		// TODO support RTL containers
 		return true;
 	}
 
 	@Override
 	public void paintDebugOutline() {
 		removeDebugOverlays();
 		paintDebug(0, 0, container.getOffsetWidth(true), container.getOffsetHeight(true), "blue");
 	}
 
 	private void removeDebugOverlays() {
 		Element root = container.getElement();
 		for (Element overlay : debugOverlays) {
 			root.removeChild(overlay);
 		}
 		debugOverlays.clear();
 	}
 
 	@Override
 	public void paintDebugCell(int x, int y, int width, int height) {
 		paintDebug(x, y, width, height, "red");
 	}
 
 	void applyLayout(Widget widget, int x, int y, int width, int height) {
 		container.applyLayout(widget, x, y, width, height);
 	}
 
 	void paintDebug(int x, int y, int width, int height, String color) {
 		XElement overlay = XElement.createElement("div");
 		Style style = overlay.getStyle();
 		style.setPosition(Position.ABSOLUTE);
 		style.setBorderColor(color);
 		style.setBorderStyle(BorderStyle.DASHED);
 		style.setBorderWidth(1, Unit.PX);
 		style.setProperty("pointerEvents", "none");
		overlay.setBounds(x, y, width - 2, height - 2);
 		container.getElement().appendChild(overlay);
 		debugOverlays.add(overlay);
 	}
 
 }
