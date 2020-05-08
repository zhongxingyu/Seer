 package c3i.smartClient.client.widgets;
 
 import c3i.core.imageModel.shared.Profile;
 import c3i.smartClient.client.model.ImageStack;
 import c3i.smartClient.client.model.ImageStackChangeListener;
 import c3i.smartClient.client.model.ImageWidget;
 import c3i.smartClient.client.model.Img;
 import c3i.smartClient.client.model.LayerState;
 import c3i.smartClient.client.model.ViewModel;
 import c3i.smartClient.client.widgets.dragToSpin.DragToSpin;
 import c3i.util.shared.events.ChangeListener;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.ComplexPanel;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.Widget;
 import smartsoft.util.gwt.client.Console;
 import smartsoft.util.lang.shared.RectSize;
 
 public class ViewPanel extends ComplexPanel {
 
    private static final int IMAGE_COUNT = 10;
 
     private ViewModel viewModel;
     private RectSize fixedSize;
 
     private ImmutableList<ImageWidget> imageWidgets;
     private AbsolutePanel absolutePanel;
 
 
     private DragToSpin dragToSpin;
 
     public ViewPanel(final ViewModel viewModel) {
         this(viewModel, null, null, null);
     }
 
     public ViewPanel(final ViewModel viewModel, String viewPanelDebugName) {
         this(viewModel, null, null, viewPanelDebugName);
     }
 
     public ViewPanel(final ViewModel viewModel, Integer viewIndex) {
         this(viewModel, viewIndex, null, null);
     }
 
     public ViewPanel(final ViewModel viewModel, Integer viewIndex, String viewPanelDebugName) {
         this(viewModel, viewIndex, null, viewPanelDebugName);
     }
 
     public ViewPanel(ViewModel viewModel, RectSize fixedSize, String viewPanelDebugName) {
         this(viewModel, null, fixedSize, viewPanelDebugName);
     }
 
     public ViewPanel(final ViewModel pViewModel, Integer viewIndex, RectSize fixedSize, final String viewPanelDebugName) {
 //        Console.log("ViewPanel.ViewPanel  viewIndex[" + viewIndex + "]  viewPanelDebugName["+viewPanelDebugName+"]");
         setElement(DOM.createDiv());
 //        onAttach();
         init(pViewModel, viewIndex, fixedSize, viewPanelDebugName);
 
     }
 
     private void init(ViewModel pViewModel, Integer viewIndex, RectSize fixedSize, String viewPanelDebugName) {
         if (viewIndex != null) {
             this.viewModel = pViewModel.getViewModel(viewIndex);
         } else {
             this.viewModel = pViewModel;
         }
 
         Style style = getElement().getStyle();
         style.setPosition(Style.Position.RELATIVE);
 
         this.fixedSize = fixedSize;
 
         RectSize initialSize = getInitialSize(fixedSize, this.viewModel);
 
         absolutePanel = createAbsolutePanel(initialSize);
 
         add(absolutePanel);
         imageWidgets = createImageWidgets(absolutePanel, initialSize);
         dragToSpin = createDragToSpin(absolutePanel, this.viewModel, initialSize);
 
         this.viewModel.addImageStackChangeListener(new ImageStackChangeListener() {
             @Override
             public void onChange(ImageStack newValue) {
                 refreshImageStack();
             }
         });
 
 
         this.viewModel.addLayerStateListener(new ChangeListener<LayerState>() {
             @Override
             public void onChange(LayerState newValue) {
                 Console.log("LayerState.onChange. HiddenLayers: " + newValue.getHiddenLayers());
                 refreshImageStack();
             }
         });
 
         refreshImageStack();
 
         addStyleName("ViewPanel");
 
         if (viewPanelDebugName != null) {
             getElement().setId(viewPanelDebugName);
         }
     }
 
     public ViewPanel(com.google.gwt.dom.client.Element el, final ViewModel pViewModel, Integer viewIndex, RectSize fixedSize, final String viewPanelDebugName) {
         setElement(el);
         onAttach(); //this is key!!
         init(pViewModel, viewIndex, fixedSize, viewPanelDebugName);
     }
 
 
     private void refreshSize(RectSize imageSize) {
         setPixelSize(imageSize);
         absolutePanel.setPixelSize(imageSize.getWidth(), imageSize.getHeight());
 
         for (ImageWidget imageWidget : imageWidgets) {
             imageWidget.setPixelSize(imageSize);
         }
 
         dragToSpin.setPixelSize(imageSize);
     }
 
 
     private static RectSize getInitialSize(RectSize fixedSize, ViewModel viewModel) {
         if (fixedSize != null) {
             return fixedSize;
         } else {
             Profile profile = viewModel.profile().get();
             return profile.getImageSize();
         }
     }
 
 
     private static ImmutableList<ImageWidget> createImageWidgets(AbsolutePanel ap, RectSize initSize) {
 
         ImmutableList.Builder<ImageWidget> builder = ImmutableList.builder();
         for (int i = 0; i < IMAGE_COUNT; i++) {
             ImageWidget imageWidget = new ImageWidget();
             imageWidget.setPixelSize(initSize);
             ap.add(imageWidget, 0, 0);
             builder.add(imageWidget);
         }
         return builder.build();
     }
 
 
     private static AbsolutePanel createAbsolutePanel(RectSize initSize) {
         AbsolutePanel p = new AbsolutePanel();
         p.setPixelSize(initSize.getWidth(), initSize.getHeight());
         return p;
     }
 
     private void refreshImageStack() {
         ImageStack imageStack = this.viewModel.getImageStack();
         if (imageStack == null || imageStack.getFixedPicks() == null || imageStack.getFixedPicks().isInvalidBuild()) {
             setVisible(false);
             for (ImageWidget imageWidget : imageWidgets) {
                 imageWidget.setUrl(null);
                 imageWidget.setVisible(false);
             }
         } else {
             if (fixedSize == null) {
                 refreshSize(this.viewModel.profile().get().getImageSize());
             }
             setVisible(true);
             for (int i = 0; i < IMAGE_COUNT; i++) {
                 try {
                     Img img = imageStack.get(i);
                     imageWidgets.get(i).setUrl(img.getUrl());
                     boolean imageEnable = img.isEnabled();
                     imageWidgets.get(i).setVisible(imageEnable);
                 } catch (IndexOutOfBoundsException e) {
                     imageWidgets.get(i).setUrl(null);
                     imageWidgets.get(i).setVisible(false);
                 }
             }
         }
 
 
     }
 
 
 
     public void setPixelSize(RectSize imageSize) {
         Preconditions.checkNotNull(imageSize);
         setPixelSize(imageSize.getWidth(), imageSize.getHeight());
     }
 
 
     public RectSize getPreferredSize() {
         return getInitialSize(fixedSize, this.viewModel);
     }
 
 
     private static DragToSpin createDragToSpin(AbsolutePanel ap, ViewModel viewModel, RectSize initSize) {
         DragToSpin dts = new DragToSpin(viewModel, initSize);
         ap.add(dts, 0, 0);
         return dts;
     }
 
 //    private static DragToSpin createDragToSpin(com.google.gwt.user.client.Element el, ViewModel viewModel, RectSize initSize) {
 //        DragToSpin dts = new DragToSpin(viewModel, initSize);
 //        el.appendChild(dts.getElement());
 //        return dts;
 //    }
 
 
     /**
      * Adds a new child widget to the panel.
      *
      * @param w the widget to be added
      */
     @Override
     public void add(Widget w) {
         add(w, getElement());
     }
 
     @Override
     public void clear() {
         throw new UnsupportedOperationException("Dave");
     }
 
     public void insert(IsWidget w, int beforeIndex) {
         insert(asWidgetOrNull(w), beforeIndex);
     }
 
     /**
      * Inserts a widget before the specified index.
      *
      * @param w the widget to be inserted
      * @param beforeIndex the index before which it will be inserted
      * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
      *           range
      */
     public void insert(Widget w, int beforeIndex) {
         insert(w, getElement(), beforeIndex, true);
     }
 
 }
