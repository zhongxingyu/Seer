 package cm.view.render;
 
 import java.awt.Component;
 
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 
 import cm.model.EffectBase.Duration;
 
 /**
  * Defines a JList cell renderer for an {@link Duration}.
  * 
  * @author Matthew Rinehart &lt;gomamon2k at yahoo.com&gt;
  * @since 1.0
  */
 public class DurationCellRenderer extends JLabel implements ListCellRenderer {
 	/**
 	 * Generated.
 	 */
 	private static final long serialVersionUID = -5058529343769502155L;
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		setText(((Duration) value).getDesc());
 		return this;
 	}
 
 }
