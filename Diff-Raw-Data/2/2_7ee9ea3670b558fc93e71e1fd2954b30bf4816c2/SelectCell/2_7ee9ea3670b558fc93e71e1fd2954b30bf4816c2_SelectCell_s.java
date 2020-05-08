 /**
  * 
  */
 package org.mklab.taskit.client.ui;
 
 import java.util.List;
 
 import com.google.gwt.cell.client.AbstractInputCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.SelectElement;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 
 
 /**
  * 提出物の状態を編集するセルです。
  * <p>
  * コンボボックスにより編集を行います。列にそもそも課題がない場合には、編集ができないようにしています。
  * 
  * @author ishikura
  */
 class SelectCell<E> extends AbstractInputCell<E, E> {
 
   private List<E> options;
   private boolean editable;
   private Renderer<E> renderer;
 
   /**
    * {@link SelectCell}オブジェクトを構築します。
    * 
    * @param options 選択可能なオプションのリスト
    * @param renderer 値を、描画する文字列に変換するオブジェクト
    */
   public SelectCell(List<E> options, Renderer<E> renderer) {
     super("change"); //$NON-NLS-1$
     this.options = options;
     this.renderer = renderer;
   }
 
   /**
    * editableを設定します。
    * 
    * @param editable editable
    */
   public void setEditable(boolean editable) {
     this.editable = editable;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, E value, NativeEvent event, ValueUpdater<E> valueUpdater) {
     if ("change".equals(event.getType()) == false) return; //$NON-NLS-1$
 
     final SelectElement select = parent.getFirstChild().cast();
     final int selectedIndex = select.getSelectedIndex();
     if (selectedIndex < 0) return;
 
     final E selectedValue = this.options.get(selectedIndex);
     finishEditing(parent, value, context.getKey(), valueUpdater);
     if (valueUpdater != null) {
       valueUpdater.update(selectedValue);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("nls")
   @Override
   public void render(@SuppressWarnings("unused") com.google.gwt.cell.client.Cell.Context context, E value, SafeHtmlBuilder sb) {
     if (this.editable) {
       sb.appendHtmlConstant("<select>");
     } else {
       sb.appendHtmlConstant("<select disabled>");
     }
 
     for (E option : this.options) {
       final boolean selected = equals(option, value);
      final String escapedOption = value == null ? "" : SafeHtmlUtils.htmlEscape(this.renderer.render(option));
       sb.appendHtmlConstant("<option value='" + escapedOption + "'" + (selected ? " selected" : "") + ">" + escapedOption + "</option>");
     }
     sb.appendHtmlConstant("</select>");
   }
 
   private boolean equals(E e1, E e2) {
     if (e1 == null || e2 == null) {
       if (e1 == null && e2 == null) return true;
       return false;
     }
     return e1.equals(e2);
   }
 
   /**
    * 値をセルに描画する文字列に変換するクラスです。
    * 
    * @author ishikura
    * @param <E> 値の型
    */
   public static interface Renderer<E> {
 
     /**
      * 値を文字列にします。
      * 
      * @param value 値
      * @return 文字列
      */
     String render(E value);
   }
 
 }
