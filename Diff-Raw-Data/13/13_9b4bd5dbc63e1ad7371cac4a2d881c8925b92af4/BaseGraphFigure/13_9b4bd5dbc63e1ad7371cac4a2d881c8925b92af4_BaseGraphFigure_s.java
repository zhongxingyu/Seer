 /**
  * 
  */
 package org.mklab.ishikura.graph.g2d;
 
 import org.mklab.ishikura.graph.figure.ContainerFigureImpl;
 import org.mklab.ishikura.graph.figure.TextAlignment;
 import org.mklab.ishikura.graph.figure.TextFigure;
 import org.mklab.ishikura.graph.figure.TextOrientation;
 import org.mklab.ishikura.graph.graphics.Color;
 import org.mklab.ishikura.graph.graphics.Graphics;
 
 
 /**
  * ラベル付きの座標空間の図です。
  * <p>
  * コンストラクタで与えられた座標空間を元にし、それに加えx軸、y軸のラベル、グラフのタイトルを描画します。
  * 
  * @author Yuhi Ishikura
  * @version $Revision$, 2010/10/22
  */
 class BaseGraphFigure extends ContainerFigureImpl implements HasCoordinateSpace {
 
   private CoordinateSpaceFigure coordinateSpace;
   private TextFigure xLabel;
   private TextFigure yLabel;
   private TextFigure titleLabel;
 
   /**
    * {@link BaseGraphFigure}オブジェクトを構築します。
    * 
    * @param coodinateSpace 座標系の図
    */
   BaseGraphFigure(CoordinateSpaceFigure coodinateSpace) {
     if (coodinateSpace == null) throw new NullPointerException();
     this.coordinateSpace = coodinateSpace;
 
     this.titleLabel = new TextFigure();
     this.titleLabel.setAlignment(TextAlignment.CENTER);
 
     this.xLabel = new TextFigure();
     this.xLabel.setAlignment(TextAlignment.CENTER);
 
     this.yLabel = new TextFigure();
     this.yLabel.setAlignment(TextAlignment.CENTER);
     this.yLabel.setOrientation(TextOrientation.VERTICAL);
 
     add(this.coordinateSpace);
     add(this.xLabel);
     add(this.yLabel);
     add(this.titleLabel);
   }
 
   /**
    * coordinateSpaceを取得します。
    * 
    * @return coordinateSpace
    */
   CoordinateSpaceFigure getCoordinateSpace() {
     return this.coordinateSpace;
   }
 
   void setForegroundColor(Color foregroundColor) {
     this.xLabel.setColor(foregroundColor);
     this.yLabel.setColor(foregroundColor);
     this.titleLabel.setColor(foregroundColor);
   }
 
   TextFigure getXLabel() {
     return this.xLabel;
   }
 
   TextFigure getYLabel() {
     return this.yLabel;
   }
 
   TextFigure getTitleLabel() {
     return this.titleLabel;
   }
 
   /**
    * グラフのタイトルを設定します。
    * 
    * @param graphTitle グラフのタイトル
    */
   void setTitle(String graphTitle) {
     this.titleLabel.setText(graphTitle);
   }
 
   /**
    * グラフのタイトルを取得します。
    * 
    * @return グラフのタイトル
    */
   String getTitle() {
     return this.titleLabel.getText();
   }
 
   /**
    * x軸の名前を設定します。
    * 
    * @param name x軸の名前
    */
   void setXAxisName(String name) {
     this.xLabel.setText(name);
   }
 
   /**
    * x軸の名前を取得します。
    * 
    * @return　x軸の名前
    */
   String getXAxisName() {
     return this.xLabel.getText();
   }
 
   /**
    * y軸の名前を設定します。
    * 
    * @param name y軸の名前
    */
   void setYAxisName(String name) {
     this.yLabel.setText(name);
   }
 
   /**
    * y軸の名前を取得します。
    * 
    * @return　y軸の名前
    */
   String getYAxisName() {
     return this.yLabel.getText();
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected void layout(Graphics g) {
     final int textHeight = g.getTextHeight();
 
     final int titleBottomSpace = 10;
     final int titleLabelHeight = textHeight + titleBottomSpace;
     final int xLabelHeight = textHeight;
 
     this.coordinateSpace.setBounds(textHeight, titleLabelHeight, getWidth() - textHeight, getHeight() - xLabelHeight - titleLabelHeight);
     this.coordinateSpace.validate(g);
 
     final int horizontalLabelX = textHeight + this.coordinateSpace.getGrid().getX();
     final int horizontalLabelWidth = this.coordinateSpace.getGrid().getWidth();
     this.titleLabel.setBounds(horizontalLabelX, 0, horizontalLabelWidth, titleLabelHeight);
     this.xLabel.setBounds(horizontalLabelX, titleLabelHeight + this.coordinateSpace.getHeight(), horizontalLabelWidth, textHeight);
     this.yLabel.setBounds(0, 0, textHeight, this.coordinateSpace.getHeight());
 
     validateChildren(g);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void moveScope(int dx, int dy) {
     this.coordinateSpace.moveScope(dx, dy);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void scaleScope(int x, int y, double ratio) {
    Util.scaleScope(this, this.coordinateSpace.getGrid(), x, y, ratio);
     invalidate();
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public double viewToModelX(int x) {
     return Util.viewToModelX(this, this.coordinateSpace.getGrid(), x);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public double viewToModelY(int y) {
     return Util.viewToModelY(this, this.coordinateSpace.getGrid(), y);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setScope(Scope scope) {
     this.coordinateSpace.setScope(scope);
   }
 
 }
