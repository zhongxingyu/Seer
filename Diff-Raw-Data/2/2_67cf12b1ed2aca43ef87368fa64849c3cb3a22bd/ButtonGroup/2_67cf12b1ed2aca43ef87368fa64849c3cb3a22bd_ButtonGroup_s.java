 package net.rptools.maptool.client.ui.macrobuttons.buttongroups;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import net.rptools.maptool.client.ui.macrobuttons.buttons.TokenMacroButton;
 import net.rptools.maptool.client.ui.macrobuttons.buttons.TransferData;
 import net.rptools.maptool.client.ui.macrobuttons.buttons.TransferableMacroButton;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.util.ImageManager;
 
 //public class ButtonGroup extends JPanel implements DropTargetListener {
 public class ButtonGroup extends AbstractButtonGroup {
 	
 	private DropTarget dt;
 	
 	// constructor for creating a single token's macro buttons 
 	public ButtonGroup(Token token, JPanel panel) {
 		this.token = token;
 		setOpaque(false);
 		
 		if (hasMacros(token)) {
 			addButtons(token);
 		} else {
 			add(new JLabel("No Macros"));
 		}
 
 		ThumbnailedBorder border = createBorder(getName(token));
 		setBorder(border);
 		setLayout(new FlowLayout(FlowLayout.LEFT));
 		dt = new DropTarget(this, this);
 		//addMouseListener(border.getMouseAdapter());
 	}
 
 	// constructor for creating common macro buttons between tokens
 	public ButtonGroup(List<Token> tokenList) {
 		setOpaque(false);
 		if (tokenList.size() <= 1) {
 			return;
 		}
 		this.tokenList = tokenList;
 		addButtons(tokenList);
 	
 		// if there are no common macros, add a label to indicate this.
 		if (getComponents().length == 0) {
 			add(new JLabel("No Macros"));
 		}
 		
 		setBorder(new ThumbnailedBorder(null, "Common Macros"));
 		setLayout(new FlowLayout(FlowLayout.LEFT));
 		dt = new DropTarget(this, this);
 		addMouseListener(new MouseHandler());
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		
 		Dimension size = getParent().getSize();
 
 		FlowLayout layout = (FlowLayout) getLayout();
 		Insets insets = getInsets();
 		
 		// This isn't exact, but hopefully it's close enough
 		int x = layout.getHgap() + insets.left;
 		int y = layout.getVgap();
 		int rowHeight = 0;
 		for (Component c : getComponents()) {
 
 			Dimension cSize = c.getPreferredSize();
			if (x + cSize.width + layout.getHgap() > size.width - insets.right && x > 0) {
 				x = 0;
 				y += rowHeight + layout.getVgap(); 
 				rowHeight = 0;
 			}
 			
 			x += cSize.width + layout.getHgap();
 			rowHeight = Math.max(cSize.height, rowHeight);
 		}
 		
 		y += rowHeight + layout.getVgap();
 
 		y += getInsets().top;
 		y += getInsets().bottom;
 		
 		Dimension prefSize = new Dimension(size.width, y);
 		return prefSize;
 	}
 
 	public Insets getInsets() {
 		return new Insets(18,5,5,0);
 	}
 
 	private void addButtons(Token token) {
 		List<String> keyList = new ArrayList<String>(token.getMacroNames());
 		Collections.sort(keyList);
 		for (String key : keyList) {
 			add(new TokenMacroButton(token, token.getName(), key, token.getMacro(key)));
 		}
 	}
 
 	private void addButtons(List<Token> tokenList) {
 		// get the common macros of the tokens
 		// Macro Name => Token list
 		// example:
 		// "Attack" => [Elf, Mystic] (which are tokens themselves)
 		// meaning "Attack" macro belongs to both "Elf" and "Mystic"
 		// but the common macros can have different macro commands (bodies)
 		Map<String, List<Token>> encounteredMacros = new HashMap<String, List<Token>>();
 		for (Token token : tokenList) {
 			for (String macro : token.getMacroNames()) {
 				List<Token> l = encounteredMacros.get(macro);
 				if (l == null) {
 					l = new ArrayList<Token>();
 					encounteredMacros.put(macro, l);
 				}
 
 				l.add(token);
 			}
 		}
 
 		// since we are only interested in finding common macros between tokens
 		// we skip the map keys which have only 1 item in the arraylist
 		// so we skip those like "Attack" => ["Elf"]
 		TreeSet<String> keys = new TreeSet<String>();
 		// done only to sort the list alphabetically.
 		keys.addAll(encounteredMacros.keySet());
 		for (String macro : keys) {
 			List<Token> l = encounteredMacros.get(macro);
 			if (l.size() > 1) {
 				add(new TokenMacroButton(l, macro));
 			}
 		}
 	}
 
 	private boolean hasMacros(Token token) {
 		return !token.getMacroNames().isEmpty();
 	}
 
 	private String getName(Token token) {
 		// if a token has a GM name, put that to button title too
 		if (token.getGMName() != null && token.getGMName().trim().length() > 0) {
 			return token.getName() + " (" + token.getGMName() + ")";
 		} else {
 			return token.getName();
 		}
 	}
 
 	public void drop(DropTargetDropEvent event) {
 		//System.out.println("BG: drop!");
 		
 		try {
 			Transferable t = event.getTransferable();
 			TransferData data = (TransferData) t.getTransferData(TransferableMacroButton.tokenMacroButtonFlavor);
 			if (data == null) {
 				return;
 			}
 			//System.out.println(data.macro);
 			//System.out.println(data.command);
 
 			if (tokenList != null) {
 				// this is a common group, copy macro to all selected tokens
 				event.acceptDrop(event.getDropAction());
 				for (Token token : tokenList) {
 					token.addMacro(data.macro, data.command);
 				}
 			} else if (token != null) {
 				// this is a token group, copy macro to this.Token only
 				event.acceptDrop(event.getDropAction());
 				token.addMacro(data.macro, data.command);
 			} else {
 				// if this happens, it's a bug
 				throw new Exception("Drag & Drop problem");
 			}
 			//System.out.println("drop accepted");
 			event.dropComplete(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			event.dropComplete(false);
 		}
 	}
 	
 	private class MouseHandler extends MouseAdapter {
 		public void mouseClicked(MouseEvent e) {
 			if (SwingUtilities.isRightMouseButton(e)) {
 				new ButtonGroupPopupMenu(ButtonGroup.this).show(ButtonGroup.this, e.getX(), e.getY());
 			}
 		}
 	}
 
 	private ThumbnailedBorder createBorder(String label) {
 		ImageIcon i = new ImageIcon(ImageManager.getImageAndWait(AssetManager.getAsset(token.getImageAssetId())));
 		Image icon = i.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT);
 		return new ThumbnailedBorder(icon, label);
 	}	
 }
