 package org.jboss.tools.fuse.reddeer.editor;
 
 import java.util.List;
 
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.gef.ui.palette.PaletteViewer;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.jboss.reddeer.swt.util.Display;
 import org.jboss.reddeer.swt.util.ResultRunnable;
 import org.jboss.tools.fuse.reddeer.editor.finder.PaletteEntryFinder;
 
 /**
  * 
  * @author apodhrad
  * 
  */
 public class Palette {
 
 	private PaletteViewer paletteViewer;
 
 	public Palette(PaletteViewer paletteViewer) {
		if(paletteViewer == null) {
			throw new NullPointerException("PaletteViewer cannot be null!");
		}
 		this.paletteViewer = paletteViewer;
 	}
 
 	public void activateTool(String label) {
 		List<PaletteEntry> entries = getPaletteEntries(new PaletteEntryWithLabel(label));
 		if (entries.isEmpty()) {
 			throw new RuntimeException("Cannot find palette entry with label '" + label + "'");
 		}
 		final ToolEntry toolEntry = (ToolEntry) entries.get(0);
 		Display.syncExec(new Runnable() {
 			@Override
 			public void run() {
 				paletteViewer.setActiveTool(toolEntry);
 			}
 		});
 	}
 
 	public String getActiveTool() {
 		return Display.syncExec(new ResultRunnable<String>() {
 			@Override
 			public String run() {
 				return paletteViewer.getActiveTool().getLabel();
 			}
 		});
 	}
 
 	public List<PaletteEntry> getPaletteEntries(final Matcher<PaletteEntry> matcher) {
 		return Display.syncExec(new ResultRunnable<List<PaletteEntry>>() {
 			@Override
 			public List<PaletteEntry> run() {
 				PaletteRoot paletteRoot = paletteViewer.getPaletteRoot();
 				return new PaletteEntryFinder().find(paletteRoot, matcher);
 			}
 
 		});
 	}
 
 	public class PaletteEntryWithLabel extends BaseMatcher<PaletteEntry> {
 
 		private String label;
 
 		public PaletteEntryWithLabel(String label) {
 			this.label = label;
 		}
 
 		@Override
 		public boolean matches(Object obj) {
 			if (obj instanceof ToolEntry) {
 				ToolEntry toolEntry = (ToolEntry) obj;
 				return toolEntry.getLabel().equals(label);
 			}
 			return false;
 		}
 
 		@Override
 		public void describeTo(Description desc) {
 
 		}
 
 	}
 
 }
