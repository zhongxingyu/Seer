 package org.bh.gui.swing.forms.border;
 
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.PlatformEvent.Type;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 
 public class BHTitledBorder extends TitledBorder implements IPlatformListener {
 
 	static ITranslator trans = BHTranslator.getInstance();

 	Object key;

 	public BHTitledBorder(Border border, Object key) {
 		super(border);
 		setTitle(key);

 	}

	// TODO Konstruktor so anpassen, dass Übersetzbarkeit gewährleistet ist (z.B
	// im BHBalanceSheetForm.java)
 	public BHTitledBorder(Border createEtchedBorder, Object key, int position,
 			int justification) {
 		this(createEtchedBorder, key);
 		setTitleJustification(justification);
 		setTitlePosition(position);

 	}
 
 	public void setTitle(Object key) {
 		this.key = key;
 		setTitle();
 	}

	private void setTitle() {
 		setTitle(trans.translate(key));
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 		if (e.getEventType() == Type.LOCALE_CHANGED) {
 			setTitle();
 		}
 	}
 }
