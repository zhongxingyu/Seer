 package topshelf.gwt.editor.client;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import topshelf.gwt.common.client.AbstractGroupedList;
 
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.IsEditor;
 import com.google.gwt.editor.client.adapters.EditorSource;
 import com.google.gwt.editor.client.adapters.ListEditor;
 
 public abstract class AbstractGroupedListEditor
 	<VALUE, E extends Editor<VALUE>, GROUP,
 	GROUPKEY extends Comparable<?>>
 
 	extends AbstractGroupedList<VALUE,E,GROUP,GROUPKEY>
 	implements IsEditor<ListEditor<VALUE,E>> {
 	
 	private ListEditor<VALUE,E> listEditor;
 	private Map<E,VALUE> values = new HashMap<E,VALUE>();
 	
 	public Collection<VALUE> getValues() {
 		return values.values();
 	}
 	
 	protected AbstractGroupedListEditor() {
 		listEditor = new MyListEditor(new EditorSource<E>() {
 
 			@Override
 			public E create(int index) {
 				VALUE val = listEditor.getList().get(index);
 				E editor = addValue(index, val);
 				values.put(editor, val);
 				return editor;
 			}
 			
 			@Override
 			public void dispose(E editor) {
 				VALUE val = values.remove(editor);
 				if (null != val) {
 					removeValue(val);
 				}
 			}
 		});
 	}
 
 	class MyListEditor extends ListEditor<VALUE,E> {
 
 		protected MyListEditor(EditorSource<E> source) {
 			super(source);
 		}
 
 		@Override
 		public void setValue(List<VALUE> value) {
 			super.setValue(value);
 			onValueSet();
 		}
 	}
 	
	protected void reset() {
		super.reset();
		values.clear();
		listEditor.getList().clear();
	}
	
 	public void onValueSet() {
 		// do nothing, override if necessary
 	}
 	
 	@Override
 	public ListEditor<VALUE,E> asEditor() {
 		return listEditor;
 	}
 }
