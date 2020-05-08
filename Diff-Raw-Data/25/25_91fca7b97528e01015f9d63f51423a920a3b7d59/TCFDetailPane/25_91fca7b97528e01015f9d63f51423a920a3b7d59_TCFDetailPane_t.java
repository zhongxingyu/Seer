 /*******************************************************************************
  * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.debug.ui.IDebugView;
 import org.eclipse.debug.ui.IDetailPane;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.ITextPresentationListener;
 import org.eclipse.jface.text.TextPresentation;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.jface.text.source.SourceViewerConfiguration;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchCommandConstants;
 import org.eclipse.ui.IWorkbenchPartSite;
 
 /**
  * This detail pane uses a source viewer to display detailed information about the current
  * selection.
  */
 public class TCFDetailPane implements IDetailPane {
 
     public static final String ID = "org.eclipse.tcf.debug.DetailPaneFactory";
     public static final String NAME = "TCF Detail Pane";
     public static final String DESC = "TCF Detail Pane";
 
     private static final String DETAIL_COPY_ACTION = IDebugView.COPY_ACTION + ".DetailPane"; //$NON-NLS-1$
     private static final String DETAIL_SELECT_ALL_ACTION = IDebugView.SELECT_ALL_ACTION + ".DetailPane"; //$NON-NLS-1$
 
     private SourceViewer source_viewer;
     private Display display;
     private int generation;
     private IWorkbenchPartSite part_site;
     private final Document document = new Document();
     private final ArrayList<StyleRange> style_ranges = new ArrayList<StyleRange>();
     private final HashMap<RGB,Color> colors = new HashMap<RGB,Color>();
     private final Map<String,IAction> action_map = new HashMap<String,IAction>();
     private final List<String> selection_actions = new ArrayList<String>();
 
     private final ITextPresentationListener presentation_listener = new ITextPresentationListener() {
         public void applyTextPresentation(TextPresentation presentation) {
             for (StyleRange r : style_ranges) presentation.addStyleRange(r);
         }
     };
 
     private class DetailPaneAction extends Action {
         final int op_code;
         DetailPaneAction(String text, int op_code) {
             super(text);
             this.op_code = op_code;
         }
         void update() {
             boolean was_enabled = isEnabled();
             boolean is_enabled = (source_viewer != null && source_viewer.canDoOperation(op_code));
             setEnabled(is_enabled);
             if (was_enabled == is_enabled) return;
             firePropertyChange(ENABLED, was_enabled, is_enabled);
         }
         @Override
         public void run() {
             if (!isEnabled()) return;
             source_viewer.doOperation(op_code);
         }
     }
 
     private void createActions() {
         DetailPaneAction action = null;
 
         action = new DetailPaneAction("Select &All", ITextOperationTarget.SELECT_ALL);
         action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_SELECT_ALL);
         action_map.put(DETAIL_SELECT_ALL_ACTION, action);
 
         action = new DetailPaneAction("&Copy", ITextOperationTarget.COPY);
         action.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
         action_map.put(DETAIL_COPY_ACTION, action);
 
         selection_actions.add(DETAIL_COPY_ACTION);
 
         updateSelectionDependentActions();
     }
 
     private IAction getAction(String id) {
         return action_map.get(id);
     }
 
     private void setGlobalAction(String id, IAction action){
        if (part_site instanceof IViewSite) {
            ((IViewSite)part_site).getActionBars().setGlobalActionHandler(id, action);
        }
     }
 
     private void createDetailContextMenu(Control menuControl) {
        if (part_site == null) return;
         MenuManager manager = new MenuManager();
         manager.setRemoveAllWhenShown(true);
         manager.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager mgr) {
                 fillDetailContextMenu(mgr);
             }
         });
         Menu menu = manager.createContextMenu(menuControl);
         menuControl.setMenu(menu);
         part_site.registerContextMenu(ID, manager, source_viewer.getSelectionProvider());
     }
 
     private void fillDetailContextMenu(IMenuManager menu) {
         //menu.add(new Separator(MODULES_GROUP));
         //menu.add(new Separator());
         menu.add(getAction(DETAIL_COPY_ACTION));
         menu.add(getAction(DETAIL_SELECT_ALL_ACTION));
         menu.add(new Separator());
         menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
     }
 
     private void updateAction(String id) {
         IAction action = getAction(id);
         if (action instanceof DetailPaneAction) ((DetailPaneAction)action).update();
     }
 
     private void updateSelectionDependentActions() {
         for (String id : selection_actions) updateAction(id);
     }
 
     public Control createControl(Composite parent) {
         assert source_viewer == null;
         source_viewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
         source_viewer.configure(new SourceViewerConfiguration());
         source_viewer.setDocument(document);
         source_viewer.setEditable(false);
         source_viewer.addTextPresentationListener(presentation_listener);
         Control control = source_viewer.getControl();
         GridData gd = new GridData(GridData.FILL_BOTH);
         control.setLayoutData(gd);
         display = control.getDisplay();
         createActions();
         // Add the selection listener so selection dependent actions get updated.
         document.addDocumentListener(new IDocumentListener() {
             public void documentAboutToBeChanged(DocumentEvent event) {}
             public void documentChanged(DocumentEvent event) {
                 updateSelectionDependentActions();
             }
         });
         // Add the selection listener so selection dependent actions get updated.
         source_viewer.getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener() {
             public void selectionChanged(SelectionChangedEvent event) {
                 updateSelectionDependentActions();
             }
         });
         // Add a focus listener to update actions when details area gains focus
         source_viewer.getControl().addFocusListener(new FocusAdapter() {
             public void focusGained(FocusEvent e) {
                if (part_site != null) part_site.setSelectionProvider(source_viewer.getSelectionProvider());
                 setGlobalAction(IDebugView.SELECT_ALL_ACTION, getAction(DETAIL_SELECT_ALL_ACTION));
                 setGlobalAction(IDebugView.COPY_ACTION, getAction(DETAIL_COPY_ACTION));
                if (part_site instanceof IViewSite) ((IViewSite)part_site).getActionBars().updateActionBars();
             }
             public void focusLost(FocusEvent e) {
                if (part_site != null) part_site.setSelectionProvider(null);
                 setGlobalAction(IDebugView.SELECT_ALL_ACTION, null);
                 setGlobalAction(IDebugView.COPY_ACTION, null);
                if (part_site instanceof IViewSite) ((IViewSite)part_site).getActionBars().updateActionBars();
             }
         });
         // Add a context menu to the detail area
         createDetailContextMenu(source_viewer.getTextWidget());
         return control;
     }
 
     public void display(IStructuredSelection selection) {
         if (source_viewer == null) return;
         generation++;
         final int g = generation;
         final ArrayList<TCFNode> nodes = new ArrayList<TCFNode>();
         if (selection != null) {
             Iterator<?> iterator = selection.iterator();
             while (iterator.hasNext()) {
                 Object next = iterator.next();
                 if (next instanceof TCFNode) nodes.add((TCFNode)next);
             }
         }
         Protocol.invokeLater(new Runnable() {
             public void run() {
                 if (g != generation) return;
                 final StyledStringBuffer s = getDetailText(nodes, this);
                 if (s == null) return;
                 display.asyncExec(new Runnable() {
                     public void run() {
                         if (g != generation) return;
                         document.set(getStyleRanges(s));
                     }
                 });
             }
         });
     }
 
     private StyledStringBuffer getDetailText(ArrayList<TCFNode> nodes, Runnable done) {
         StyledStringBuffer bf = new StyledStringBuffer();
         for (TCFNode n : nodes) {
             if (n instanceof IDetailsProvider) {
                 if (!((IDetailsProvider)n).getDetailText(bf, done)) return null;
             }
         }
         return bf;
     }
 
     private String getStyleRanges(StyledStringBuffer s) {
         style_ranges.clear();
         for (StyledStringBuffer.Style x : s.getStyle()) {
             style_ranges.add(new StyleRange(x.pos, x.len, getColor(x.fg), getColor(x.bg), x.font));
         }
         return s.toString();
     }
 
     private Color getColor(RGB rgb) {
         if (rgb == null) return null;
         Color c = colors.get(rgb);
         if (c == null) colors.put(rgb, c = new Color(display, rgb));
         return c;
     }
 
     public void dispose() {
         for (Color c : colors.values()) c.dispose();
         colors.clear();
         if (source_viewer == null) return;
         generation++;
         if (source_viewer.getControl() != null) {
             source_viewer.getControl().dispose();
         }
         source_viewer = null;
         selection_actions.clear();
         action_map.clear();
     }
 
     public String getDescription() {
         return DESC;
     }
 
     public String getID() {
         return ID;
     }
 
     public String getName() {
         return NAME;
     }
 
     public void init(IWorkbenchPartSite part_site) {
         this.part_site = part_site;
     }
 
     public boolean setFocus() {
         if (source_viewer == null) return false;
         source_viewer.getTextWidget().setFocus();
         return true;
     }
 }
