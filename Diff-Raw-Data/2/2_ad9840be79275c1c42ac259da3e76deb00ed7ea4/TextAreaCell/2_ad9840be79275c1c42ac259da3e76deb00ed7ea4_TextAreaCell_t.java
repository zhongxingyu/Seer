 /**
  * 
  */
 package org.mklab.taskit.client.ui.cell;
 
 import com.google.gwt.cell.client.AbstractInputCell;
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.InputElement;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 
 
 /**
  * @author ishikura
  */
 public class TextAreaCell extends AbstractInputCell<String, String> {
 
   /**
    * {@link TextAreaCell}オブジェクトを構築します。
    */
   public TextAreaCell() {
     super("change"); //$NON-NLS-1$
   }
 
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"nls", "unused"})
   @Override
   public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb) {
     sb.appendHtmlConstant("<textarea rows='2'>");
    if (value != null) sb.appendEscaped(value);
     sb.appendHtmlConstant("</textarea>");
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void onBrowserEvent(com.google.gwt.cell.client.Cell.Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
     super.onBrowserEvent(context, parent, value, event, valueUpdater);
     if ("change".equals(event.getType())) { //$NON-NLS-1$
       finishEditing(parent, value, context.getKey(), valueUpdater);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected void onEnterKeyDown(com.google.gwt.cell.client.Cell.Context context, Element parent, String value, NativeEvent event, ValueUpdater<String> valueUpdater) {
     if (event.getCtrlKey()) {
       super.onEnterKeyDown(context, parent, value, event, valueUpdater);
     }
     return;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   protected void finishEditing(Element parent, String value, Object key, ValueUpdater<String> valueUpdater) {
     final InputElement input = getInputElement(parent).cast();
     valueUpdater.update(input.getValue());
     super.finishEditing(parent, value, key, valueUpdater);
   }
 }
