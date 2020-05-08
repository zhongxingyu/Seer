 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.events.IEventListener;
 import com.eagerlogic.cubee.client.events.MouseDragEventArgs;
 import com.eagerlogic.cubee.client.properties.AExpression;
 import com.eagerlogic.cubee.client.properties.BackgroundProperty;
 import com.eagerlogic.cubee.client.properties.ColorProperty;
 import com.eagerlogic.cubee.client.properties.DoubleProperty;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.ext.AlignCenterExp;
 import com.eagerlogic.cubee.client.properties.ext.AlignMiddleExp;
 import com.eagerlogic.cubee.client.style.Style;
 import com.eagerlogic.cubee.client.style.styles.ABackground;
 import com.eagerlogic.cubee.client.style.styles.Border;
 import com.eagerlogic.cubee.client.style.styles.Color;
 import com.eagerlogic.cubee.client.style.styles.ColorBackground;
 import com.eagerlogic.cubee.client.style.styles.ECursor;
 
 /**
  *
  * @author dipacs
  */
 public class VSplitPanel extends AUserControl {
 
     public static class StyleClass<T extends VSplitPanel> extends AUserControl.StyleClass<T> {
 
         private final Style<Integer> separatorHeight = new Style<Integer>(null, false);
         private final Style<ABackground> separatorBackground = new Style<ABackground>(null, true);
         private final Style<Color> separatorDotColor = new Style<Color>(null, true);
 
         @Override
         public void apply(T component) {
             super.apply(component);
 
             separatorHeight.apply(component.separatorHeightProperty());
             separatorBackground.apply(component.separatorBackgroundProperty());
             separatorDotColor.apply(component.separatorDotColorProperty());
         }
 
         public Style<Integer> getSeparatorHeight() {
             return separatorHeight;
         }
 
         public Style<ABackground> getSeparatorBackground() {
             return separatorBackground;
         }
 
         public Style<Color> getSeparatorDotColor() {
             return separatorDotColor;
         }
 
     }
 
     private final IntegerProperty separatorHeight = new IntegerProperty(10, false, false);
     private final BackgroundProperty separatorBackground = new BackgroundProperty(new ColorBackground(Color.getRgbColor(
             0xf0f0f0)), true, false);
     private final ColorProperty separatorDotColor = new ColorProperty(Color.GRAY, true, false);
     private final DoubleProperty separatorPosition = new DoubleProperty(0.5, false, false, new SplitPanelSeparatorPositionValidator());
 
     private Panel topPanel;
     private Panel bottomPanel;
     private Panel separatorPanel;
 
     private AFillView topContent;
     private AFillView bottomContent;
 
     public VSplitPanel() {
         topPanel = new Panel();
         topPanel.heightProperty().bind(new AExpression<Integer>() {
 
             {
                 bind(clientHeightProperty(), separatorHeight, separatorPosition);
             }
 
             @Override
             public Integer calculate() {
                 int fullHeight = clientHeightProperty().get();
                 int usableHeight = fullHeight - separatorHeight.get();
                 return (int) (usableHeight * separatorPosition.get());
             }
         });
         topPanel.widthProperty().bind(this.clientWidthProperty());
         this.getChildren().add(topPanel);
 
         bottomPanel = new Panel();
         bottomPanel.heightProperty().bind(new AExpression<Integer>() {
 
             {
                 bind(clientHeightProperty(), separatorHeight, separatorPosition);
             }
 
             @Override
             public Integer calculate() {
                 int fullHeight = clientHeightProperty().get();
                 int usableHeight = fullHeight - separatorHeight.get();
                 return usableHeight - ((int) (usableHeight * separatorPosition.get()));
             }
         });
         bottomPanel.translateYProperty().bind(new AExpression<Integer>() {
 
             {
                 bind(separatorHeight, topPanel.boundsHeightProperty());
             }
 
             @Override
             public Integer calculate() {
                 return separatorHeight.get() + topPanel.boundsHeightProperty().get();
             }
         });
         bottomPanel.widthProperty().bind(this.clientWidthProperty());
         this.getChildren().add(bottomPanel);
 
         separatorPanel = new Panel();
         separatorPanel.backgroundProperty().bind(separatorBackground);
         separatorPanel.heightProperty().bind(separatorHeight);
         separatorPanel.widthProperty().bind(this.clientWidthProperty());
        separatorPanel.cursorProperty().set(ECursor.S_RESIZE);
         separatorPanel.translateYProperty().bind(topPanel.heightProperty());
         this.getChildren().add(separatorPanel);
 
         AComponent dots = createDots();
         dots.translateXProperty().bind(new AlignCenterExp(separatorPanel, dots));
         dots.translateYProperty().bind(new AlignMiddleExp(separatorPanel, dots));
         separatorPanel.getChildren().add(dots);
 
         separatorPanel.onMouseDragEvent().addListener(new IEventListener<MouseDragEventArgs>() {
 
             @Override
             public void onFired(MouseDragEventArgs args) {
                 int componentTop = VSplitPanel.this.getScreenY();
                 separatorPosition.set((args.getScreenY() - componentTop)
                         / ((double) VSplitPanel.this.clientHeightProperty().get()));
             }
         });
     }
 
     private AComponent createDots() {
         HBox res = new HBox();
         res.handlePointerProperty().set(false);
 
         res.getChildren().add(createDot());
 
         AComponent sep = createDot();
         sep.alphaProperty().set(0.0);
         res.getChildren().add(sep);
 
         res.getChildren().add(createDot());
 
         sep = createDot();
         sep.alphaProperty().set(0.0);
         res.getChildren().add(sep);
 
         res.getChildren().add(createDot());
 
         return res;
     }
 
     private AComponent createDot() {
         Panel res = new Panel();
         res.backgroundProperty().bind(new AExpression<ABackground>() {
 
             {
                 bind(separatorDotColor);
             }
 
             @Override
             public ABackground calculate() {
                 if (separatorDotColor.get() == null) {
                     return null;
                 }
                 return new ColorBackground(separatorDotColor.get());
             }
         });
         res.widthProperty().bind(new AExpression<Integer>() {
 
             {
                 bind(separatorHeight);
             }
 
             @Override
             public Integer calculate() {
                 int quarter = separatorHeight.get() / 4;
                 return separatorHeight.get() - (quarter * 2);
             }
         });
         res.heightProperty().bind(res.widthProperty());
         res.borderProperty().bind(new AExpression<Border>() {
 
             {
                 bind(separatorHeight);
             }
 
             @Override
             public Border calculate() {
                 int quarter = separatorHeight.get() / 4;
                 int width = separatorHeight.get() - (quarter * 2);
                 return new Border(0, Color.BLACK, width / 2);
             }
         });
         return res;
     }
 
     public AFillView getTopContent() {
         return topContent;
     }
 
     public void setTopContent(AFillView topContent) {
         topPanel.getChildren().clear();
         if (this.topContent != null) {
             this.topContent.hiddenWidthProperty().unbind();
             this.topContent.hiddenHeightProperty().unbind();
         }
         this.topContent = null;
         if (topContent != null) {
             topContent.hiddenWidthProperty().bind(topPanel.clientWidthProperty());
             topContent.hiddenHeightProperty().bind(topPanel.clientHeightProperty());
             topPanel.getChildren().add(topContent);
         }
         this.topContent = topContent;
     }
 
     public AFillView getBottomContent() {
         return bottomContent;
     }
 
     public void setBottomContent(AFillView bottomContent) {
         bottomPanel.getChildren().clear();
         if (this.bottomContent != null) {
             this.bottomContent.hiddenWidthProperty().unbind();
             this.bottomContent.hiddenHeightProperty().unbind();
         }
         this.bottomContent = null;
         if (bottomContent != null) {
             bottomContent.hiddenWidthProperty().bind(bottomPanel.clientWidthProperty());
             bottomContent.hiddenHeightProperty().bind(bottomPanel.clientHeightProperty());
             bottomPanel.getChildren().add(bottomContent);
         }
         this.bottomContent = bottomContent;
     }
 
     @Override
     public IntegerProperty widthProperty() {
         return super.widthProperty();
     }
 
     @Override
     public IntegerProperty heightProperty() {
         return super.heightProperty();
     }
 
     public IntegerProperty separatorHeightProperty() {
         return separatorHeight;
     }
 
     public BackgroundProperty separatorBackgroundProperty() {
         return separatorBackground;
     }
 
     public ColorProperty separatorDotColorProperty() {
         return separatorDotColor;
     }
 
     public DoubleProperty separatorPositionProperty() {
         return separatorPosition;
     }
 
 }
