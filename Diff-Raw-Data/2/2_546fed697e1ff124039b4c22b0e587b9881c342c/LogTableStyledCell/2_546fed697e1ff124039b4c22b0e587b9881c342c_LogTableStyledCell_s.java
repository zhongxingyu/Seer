 package pleocmd.itfc.gui.log;
 
 import java.awt.Color;
 
 final class LogTableStyledCell {
 
 	private final String text;
 
 	private final Color foreground;
 
 	private final Color background;
 
 	private final boolean bold;
 
 	private final boolean italic;
 
 	private final boolean multiLine;
 
 	public LogTableStyledCell(final String text, final boolean multiLine,
 			final Color foreground, final Color background, final boolean bold,
 			final boolean italic) {
		this.text = text;
 		this.multiLine = multiLine;
 		this.foreground = foreground;
 		this.background = background;
 		this.bold = bold;
 		this.italic = italic;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public boolean isMultiLine() {
 		return multiLine;
 	}
 
 	public Color getForeground() {
 		return foreground;
 	}
 
 	public Color getBackground() {
 		return background;
 	}
 
 	public boolean isBold() {
 		return bold;
 	}
 
 	public boolean isItalic() {
 		return italic;
 	}
 
 }
