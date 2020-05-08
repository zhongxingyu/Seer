 package org.rsbot.log;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.Arrays;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 
 import javax.swing.JLabel;
 
 import sun.font.FontManager;
 
 public class LabelLogHandler extends Handler {
 
 	public final JLabel label = new JLabel();
 	private final Color defaultColor;
 
 	public LabelLogHandler() {
 		super();
 		final String pref = "Segoe UI";
		if (Arrays.asList(FontManager.getFontNamesFromPlatform()).contains(pref)) {
			final Font font = label.getFont();
			label.setFont(new Font(pref, font.getStyle(), font.getSize()));
		}
 		defaultColor = label.getForeground();
 	}
 
 	@Override
 	public void close() throws SecurityException {
 	}
 
 	@Override
 	public void flush() {
 	}
 
 	@Override
 	public void publish(final LogRecord record) {
 		String msg = record.getMessage();
 		if (record.getLevel().intValue() > Level.WARNING.intValue()) {
 			label.setForeground(new Color(0xcc0000));
 		} else {
 			label.setForeground(defaultColor);
 			msg += " ...";
 		}
 		label.setText(msg);
 	}
 
 }
