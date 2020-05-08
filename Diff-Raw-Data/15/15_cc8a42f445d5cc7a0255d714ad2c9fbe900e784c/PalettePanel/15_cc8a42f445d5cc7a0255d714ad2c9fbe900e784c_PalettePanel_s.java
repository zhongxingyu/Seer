 package org.lisapark.octopus.designer.palette;
 
 import com.jidesoft.swing.JideScrollPane;
 import com.jidesoft.swing.JideTabbedPane;
 import org.lisapark.octopus.core.Node;
 import org.lisapark.octopus.core.processor.Processor;
 import org.lisapark.octopus.core.sink.external.ExternalSink;
 import org.lisapark.octopus.core.source.external.ExternalSource;
 import org.lisapark.octopus.designer.OctopusIconsFactory;
 import org.lisapark.octopus.designer.dnd.ExternalSinkTransferable;
 import org.lisapark.octopus.designer.dnd.ExternalSourceTransferable;
 import org.lisapark.octopus.designer.dnd.ProcessorTransferable;
 import org.lisapark.octopus.swing.LayoutConstants;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.Locale;
 
 /**
  * @author dave sinclair(david.sinclair@lisa-park.com)
  */
 public class PalettePanel extends JPanel {
 
     private NodeListModel<Processor> processorListModel;
     private NodeListModel<ExternalSink> externalSinkListModel;
     private NodeListModel<ExternalSource> externalSourceListModel;
 
     private final NodeListCellRenderer cellRenderer = new NodeListCellRenderer();
     private JList externalSinkList;
     private JList externalSourceList;
     private JList processorList;
 
     public PalettePanel() {
         super(new BorderLayout(LayoutConstants.COMPONENT_HORIZONTAL_GAP, LayoutConstants.COMPONENT_VERTICAL_GAP));
         init();
     }
 
     private void init() {
         JideTabbedPane tabbedPane = new JideTabbedPane(JideTabbedPane.BOTTOM);
         tabbedPane.setOpaque(true);
 
         Component leadingComponent = new JLabel(OctopusIconsFactory.getImageIcon(OctopusIconsFactory.OCTOPUS_LARGE));
         tabbedPane.setTabLeadingComponent(leadingComponent);
 
         final String[] titles = new String[]{
                 "Processors",
                 "Sources",
                 "Sinks"
         };
 
         final int[] mnemonics = new int[]{
                 KeyEvent.VK_P,
                 KeyEvent.VK_S,
                 KeyEvent.VK_K,
         };
 
         // todo real icons
         final ImageIcon[] icons = new ImageIcon[]{
                OctopusIconsFactory.getImageIcon("/function-icon.png"),
                OctopusIconsFactory.getImageIcon("/node-insert-icon.png"),
                OctopusIconsFactory.getImageIcon("/node-delete-child-icon.png"),
         };
 
         final JComponent[] components = new JComponent[]{
                 createProcessorPanel(),
                 createExternalSourcePanel(),
                 createExternalSinkPanel()
         };
 
         for (int i = 0; i < titles.length; i++) {
             tabbedPane.addTab(titles[i], icons[i], components[i]);
             tabbedPane.setMnemonicAt(i, mnemonics[i]);
         }
 
         add(tabbedPane);
     }
 
     private JComponent createProcessorPanel() {
         processorList = createListWithTransferHandler(new TransferHandler() {
             @Override
             public int getSourceActions(JComponent c) {
                 return COPY_OR_MOVE;
             }
 
             @Override
             protected Transferable createTransferable(JComponent c) {
                 Processor processor = (Processor) processorList.getSelectedValue();
 
                 return new ProcessorTransferable(processor);
             }
         });
 
         processorListModel = NodeListModel.newNodeListModel();
         processorList.setModel(processorListModel);
 
         JPanel processorPanel = new JPanel(new BorderLayout(LayoutConstants.COMPONENT_HORIZONTAL_GAP, LayoutConstants.COMPONENT_VERTICAL_GAP));
         processorPanel.add(new JideScrollPane(processorList), BorderLayout.CENTER);
 
         return processorPanel;
     }
 
     private JComponent createExternalSourcePanel() {
         externalSourceList = createListWithTransferHandler(new TransferHandler() {
             @Override
             public int getSourceActions(JComponent c) {
                 return COPY_OR_MOVE;
             }
 
             @Override
             protected Transferable createTransferable(JComponent c) {
                 ExternalSource externalSource = (ExternalSource) externalSourceList.getSelectedValue();
 
                 return new ExternalSourceTransferable(externalSource);
             }
         });
         externalSourceListModel = NodeListModel.newNodeListModel();
         externalSourceList.setModel(externalSourceListModel);
 
         JPanel processorPanel = new JPanel(new BorderLayout(LayoutConstants.COMPONENT_HORIZONTAL_GAP, LayoutConstants.COMPONENT_VERTICAL_GAP));
         processorPanel.add(new JideScrollPane(externalSourceList), BorderLayout.CENTER);
 
         return processorPanel;
     }
 
     private JComponent createExternalSinkPanel() {
         externalSinkList = createListWithTransferHandler(new TransferHandler() {
             @Override
             public int getSourceActions(JComponent c) {
                 return COPY_OR_MOVE;
             }
 
             @Override
             protected Transferable createTransferable(JComponent c) {
                 ExternalSink externalSink = (ExternalSink) externalSinkList.getSelectedValue();
 
                 return new ExternalSinkTransferable(externalSink);
             }
         });
         externalSinkListModel = NodeListModel.newNodeListModel();
         externalSinkList.setModel(externalSinkListModel);
 
         JPanel processorPanel = new JPanel(new BorderLayout(LayoutConstants.COMPONENT_HORIZONTAL_GAP, LayoutConstants.COMPONENT_VERTICAL_GAP));
         processorPanel.add(new JideScrollPane(externalSinkList), BorderLayout.CENTER);
 
         return processorPanel;
     }
 
     private JList createListWithTransferHandler(TransferHandler transferHandler) {
         JList list = new ListWithToolTip();
 
         list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         list.setDragEnabled(true);
         list.setCellRenderer(cellRenderer);
         list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.setTransferHandler(transferHandler);
 
         return list;
     }
 
     public void setProcessors(java.util.List<Processor> processors) {
         processorListModel.setData(processors);
     }
 
     public void setExternalSources(java.util.List<ExternalSource> externalSources) {
         externalSourceListModel.setData(externalSources);
     }
 
     public void setExternalSinks(java.util.List<ExternalSink> sinks) {
         externalSinkListModel.setData(sinks);
     }
 
     /**
      * Gets the localized string from resource bundle. Available keys are defined in palette.properties that
      * begin with "PaletteView.".
      *
      * @param key the resource key
      * @return the localized string.
      */
     protected String getResourceString(String key) {
         return Resources.getResourceBundle(Locale.getDefault()).getString(key);
     }
 
     private static class ListWithToolTip extends JList {
         // This method is called as the cursor moves within the list.
 
         public String getToolTipText(MouseEvent evt) {
             int index = locationToIndex(evt.getPoint());
 
             String toolTip = null;
             if (index > -1) {
                 // Get item
                 Object item = getModel().getElementAt(index);
 
                 if (item != null) {
                     Node node = (Node) item;
                     toolTip = node.getDescription();
                 }
             }
 
             return toolTip;
         }
     }
 }
