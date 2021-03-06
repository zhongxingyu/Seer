 package org.freeplane.view.swing.map;
 
 import java.awt.Dimension;
import java.awt.Graphics;
 import java.awt.Window;
 
 import javax.swing.JEditorPane;
 import javax.swing.JToolTip;
 import javax.swing.SwingUtilities;
 
 import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
 import org.freeplane.core.ui.components.UITools;
 
 @SuppressWarnings("serial")
 public class NodeTooltip extends JToolTip {
 	final private JEditorPane tip; 
 	public NodeTooltip(){
 		tip  = new JEditorPane();
 		tip.setContentType("text/html");
 		tip.setEditable(false);
 		final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(tip);
 		scrollPane.setMaximumSize(new Dimension(maximumWidth, maximumWidth / 2));
 		UITools.setScrollbarIncrement(scrollPane);
 		add(scrollPane);
 		tip.setOpaque(false);
 //		scrollPane.setOpaque(false);
 //		scrollPane.getViewport().setOpaque(false);
 	}
 	private static int maximumWidth = Integer.MAX_VALUE;
 	/**
 	 *  set maximum width
 	 *  0 = no maximum width
 	 */
 	public static void setMaximumWidth(final int width) {
 		maximumWidth = width;
 	}
 
 	@Override
     public void setTipText(String tipText) {
 		tip.setText(tipText);
 		Dimension preferredSize = tip.getPreferredSize();
 		if (preferredSize.width < maximumWidth) {
 			return ;
 		}
 		final String TABLE_START = "<html><table>";
 		if (!tipText.startsWith(TABLE_START)) {
 			return ;
 		}
 		tipText = "<html><table width=\"" + maximumWidth + "\">" + tipText.substring(TABLE_START.length());
 		tip.setText(tipText);
     }
 
 	@Override
     public Dimension getPreferredSize() {
 	    return getComponent(0).getPreferredSize();
     }
 
 	@Override
     public void layout() {
 		Window window = SwingUtilities.windowForComponent(this);
 		if(! window.getFocusableWindowState()){
 			window.setFocusableWindowState(true);
 		}
 		getComponent(0).setSize(getPreferredSize());
 	    super.layout();
     }
 
 }
