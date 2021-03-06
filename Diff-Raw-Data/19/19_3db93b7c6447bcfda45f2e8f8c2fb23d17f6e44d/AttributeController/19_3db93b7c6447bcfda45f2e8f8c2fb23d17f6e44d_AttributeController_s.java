 /*
  *  Freeplane - mind map editor
  *  Copyright (C) 2008 Dimitry Polivaev
  *
  *  This file author is Dimitry Polivaev
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.freeplane.features.attribute;
 
 import java.awt.Component;
 
 import org.freeplane.core.extension.IExtension;
 import org.freeplane.core.io.ReadManager;
 import org.freeplane.core.io.WriteManager;
 import org.freeplane.core.resources.ResourceController;
 import org.freeplane.core.util.LogUtils;
 import org.freeplane.core.util.TextUtils;
 import org.freeplane.features.icon.UIIcon;
 import org.freeplane.features.icon.factory.IconStoreFactory;
 import org.freeplane.features.map.IMapLifeCycleListener;
 import org.freeplane.features.map.ITooltipProvider;
 import org.freeplane.features.map.MapController;
 import org.freeplane.features.map.MapModel;
 import org.freeplane.features.map.MapReader;
 import org.freeplane.features.map.NodeModel;
 import org.freeplane.features.mode.Controller;
 import org.freeplane.features.mode.ModeController;
 import org.freeplane.features.text.IContentTransformer;
 import org.freeplane.features.text.TextController;
 
 /**
  * @author Dimitry Polivaev 22.11.2008
  */
 public class AttributeController implements IExtension {
 	private static final Integer ATTRIBUTE_TOOLTIP = 7;
 	static private UIIcon attributeIcon = null;
 	private static final String STATE_ICON = "AttributeExist";
 	public static AttributeController getController() {
 		return getController(Controller.getCurrentModeController());
 	}
 
 	public static AttributeController getController(ModeController modeController) {
 		return (AttributeController) modeController.getExtension(AttributeController.class);
 	}
 	
 	public static void install( final AttributeController attributeController) {
 		Controller.getCurrentModeController().addExtension(AttributeController.class, attributeController);
 	}
 
  	final private ModeController modeController;
 
 	public AttributeController(final ModeController modeController) {
 		this.modeController = modeController;
 		final MapController mapController = Controller.getCurrentModeController().getMapController();
 		final ReadManager readManager = mapController.getReadManager();
 		final WriteManager writeManager = mapController.getWriteManager();
 		final MapReader mapReader = mapController.getMapReader();
 		final AttributeBuilder attributeBuilder = new AttributeBuilder(this, mapReader);
 		attributeBuilder.registerBy(readManager, writeManager);
 		mapController.addMapLifeCycleListener(new IMapLifeCycleListener() {
 			public void onCreate(final MapModel map) {
 				AttributeRegistry.createRegistry(map);
 			}
 
 			public void onRemove(final MapModel map) {
 			}
 		});
 		registerTooltipProvider();
 	}
 
 	public NodeAttributeTableModel createAttributeTableModel(final NodeModel node) {
 		NodeAttributeTableModel attributeModel = (NodeAttributeTableModel) node
 		    .getExtension(NodeAttributeTableModel.class);
 		if (attributeModel != null) {
 			return attributeModel;
 		}
 		attributeModel = new NodeAttributeTableModel(node);
 		node.addExtension(attributeModel);
 		if (node.areViewsEmpty()) {
 			return attributeModel;
 		}
 		modeController.getMapController().nodeRefresh(node);
 		return attributeModel;
 	}
 
 	public void performInsertRow(final NodeAttributeTableModel model, final int row, final String name,
 	                             final Object value) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performRegistryAttribute(final String name) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performRegistryAttributeValue(final String name, final String value) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performRegistrySubtreeAttributes(final NodeModel model) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performRemoveAttribute(final String name) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performRemoveAttributeValue(final String name, final String value) {
 		throw new UnsupportedOperationException();
 	}
 
 	public Attribute performRemoveRow(final NodeAttributeTableModel model, final int row) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performReplaceAtributeName(final String oldName, final String newName) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performReplaceAttributeValue(final String name, final String oldValue, final String newValue) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performSetColumnWidth(final NodeAttributeTableModel model, final int col, final int width) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performSetFontSize(final AttributeRegistry registry, final int size) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performSetRestriction(final int row, final boolean restricted) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performSetValueAt(final NodeAttributeTableModel model, final Object o, final int row, final int col) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void performSetVisibility(final int index, final boolean isVisible) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void setStateIcon(NodeAttributeTableModel attributes) {
 		final NodeModel node = attributes.getNode();
 		final boolean showIcon = ResourceController.getResourceController().getBooleanProperty(
 		    "show_icon_for_attributes");
 		if (showIcon && attributes.getRowCount() == 0) {
 			node.removeStateIcons(STATE_ICON);
 		}
 		if (showIcon && attributes.getRowCount() == 1) {
 			if (attributeIcon == null) {
 				attributeIcon = IconStoreFactory.create().getUIIcon("showAttributes.png");
 			}
 			node.setStateIcon(STATE_ICON, attributeIcon, true);
 		}
 	}
 
 	private void registerTooltipProvider() {
 		modeController.addToolTipProvider(ATTRIBUTE_TOOLTIP, new ITooltipProvider() {
 			public String getTooltip(ModeController modeController, NodeModel node, Component view) {
 				final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
 				final int rowCount = attributes.getRowCount();
 				if (rowCount == 0) {
 					return null;
 				}
 				final AttributeRegistry registry = AttributeRegistry.getRegistry(node.getMap());
 				final TextController textController = TextController.getController(modeController);
 				if (registry.getAttributeViewType().equals(AttributeTableLayoutModel.SHOW_ALL)
 						&& ! textController.getIsShortened(node)) {
 					return null;
 				}
 				final StringBuilder tooltip = new StringBuilder();
 				tooltip.append("<html><body><table style='border: 1px black solid; background-color: black' width='100%' cellspacing='1' cellpadding='2'>");
 				final int currentRowCount = attributes.getRowCount();
 				for (int i = 0; i < currentRowCount; i++) {
 					tooltip.append("<tr><td>");
 					tooltip.append(attributes.getValueAt(i, 0));
 					tooltip.append("</td><td>");
 					tooltip.append(getTransformedValue(node, textController, String.valueOf(attributes.getValueAt(i, 1))));
 					tooltip.append("</td></tr>");
 				}
 				tooltip.append("</table></body></html>");
 				return tooltip.toString();
 			}
 
 			private String getTransformedValue(NodeModel node, final TextController textController, final String originalText) {
 				try {
 					final String text = textController.getTransformedText(originalText, node, null);
 					final boolean markTransformedText = !Controller.getCurrentController().getResourceController()
 					.getBooleanProperty(IContentTransformer.DONT_MARK_TRANSFORMED_TEXT);
 					if (markTransformedText && text != originalText)
 						return colorize(text, "green");
 					else
 						return text;
 				}
 				catch (Throwable e) {
 					LogUtils.warn(e.getMessage(), e);
 					return colorize(
 						TextUtils.format("MainView.errorUpdateText", originalText, e.getLocalizedMessage())
 						.replace("\n", "<br>"), "red");
 				}
 			}
 
 			private String colorize(final String text, String color) {
 				return "<span style=\"color:" + color + ";font-style:italic;\">" + text + "</span>";
 			}
 		});
 	}
 }
