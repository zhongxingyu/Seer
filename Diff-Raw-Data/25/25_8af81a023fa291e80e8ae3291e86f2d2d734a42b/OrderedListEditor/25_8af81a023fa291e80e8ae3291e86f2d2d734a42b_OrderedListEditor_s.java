 package topshelf.gwt.editor.client;
 
 import topshelf.gwt.common.client.ListItem;
 import topshelf.gwt.common.client.OrderedList;
 
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * A {@link ListEditor<T,Editor<T>>} implementation that manages itself
  * as an {@link OrderedList} widget and manages a list of {@link ListItem}
  * backed sub editors.
  * 
  * Optionally accepts a {@link ListItemRenderer<LI>} that can take the
  * proxy value being "edited" by the subeditors and returns the Widget that
  * will be wrapped by the {@link ListItem} widget.  If no {@link ListItemRenderer<LI>}
  * is provided (either by constructor or the setter), a default renderer
  * is created that strings the proxy value and wraps that in a {@link Label}
  * widget.
  * 
  * @author bloo
  *
  * @param <LI> list item's proxy value
  */
 public class OrderedListEditor<LI>
 	extends AbstractBaseListEditor<OrderedList, LI> {
 
 	public OrderedListEditor() {
 		super(new OrderedList());
 	}
 	
 	public OrderedListEditor(ListItemRenderer<LI> renderer) {
 		super(new OrderedList(), renderer);
 	}
 }
