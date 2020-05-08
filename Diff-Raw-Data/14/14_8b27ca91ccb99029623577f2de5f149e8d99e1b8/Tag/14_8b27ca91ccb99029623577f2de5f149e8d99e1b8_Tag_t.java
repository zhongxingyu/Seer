 package edu.utsa.tagger.view;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.swing.JLabel;
 
 import edu.utsa.tagger.model.TagData;
 import edu.utsa.xgui.Anchor;
 import edu.utsa.xgui.Margins;
 import edu.utsa.xgui.XComponent;
 
 @SuppressWarnings("serial")
 public class Tag extends XComponent implements ViewListener
 {
 	private TagData tagdata;
 	private Collection<Tag> children = new ArrayList<Tag>();
 	private boolean selected = false;
 	
 	private JLabel indentation;
 	private JLabel name;
 	private JLabel qualifier;
 	private Collapser collapser;
 	private JLabel options;
 	
 	public Tag(TagData tagdata)
 	{
 		this.tagdata = tagdata;
 		
 		// options
 		options = new JLabel(" \u25bc "); // or \ue0a1?
 		options.setFont(FontsAndColors.segoe_ui_symbol);
 		options.setForeground(FontsAndColors.GREY_DARK);
 		options.setVisible(false);
 		
 		// indentation
 		String s = "";
 		int depth = tagdata.getTagDepth();
 		for (int i = 0; i < depth+1; i++) s += "      ";
 		indentation = new JLabel(s);
 		indentation.setFont(FontsAndColors.segoe_ui);
 		
 		// name
 		name = new JLabel(tagdata.getLabel());
 		name.setFont(FontsAndColors.segoe_ui);
 		
 		// qualifier
 		qualifier = new JLabel("");
 		qualifier.setFont(FontsAndColors.segoe_ui.deriveFont(Font.ITALIC));
 		if (tagdata.isChildRequired())
 		{
 			qualifier.setText("(child required)");
 		}
 		else if (tagdata.isExtensionAllowed())
 		{
 			qualifier.setText("(extensions allowed)");
 		}
 		
 		// collapser
 		collapser = new Collapser();
 		collapser.setFont(FontsAndColors.segoe_ui);
 		collapser.addMouseListener(new MouseAdapter(){
 			@Override public void mouseClicked(MouseEvent e)
 			{
 				if (collapser.isCollapsed())
 				{
 					Tag.this.uncollapse();
 				}
 				else
 				{
 					Tag.this.collapse();
 				}
				collapser.setCollapsed(!collapser.isCollapsed());
 			}
 		});
 		
 		// this
 		setDraggable(!tagdata.isChildRequired());
 		add(options, new Anchor(Anchor.TL, Anchor.NOSTRETCH, new Margins(1, 0, 1, 0)));
 		add(indentation, new Anchor(options, Anchor.RIGHT));
 		add(name, new Anchor(indentation, Anchor.RIGHT, Anchor.NOSTRETCH, new Margins(1, 0, 1, 0)));
 		add(qualifier, new Anchor(name, Anchor.RIGHT, Anchor.NOSTRETCH, new Margins(0, 5, 0, 0)));
 		add(collapser, new Anchor(name, Anchor.LEFT));
 	}
 	
 	private void collapse()
 	{
 		for (Tag child_tag : children)
 		{
 			child_tag.setVisible(false);
 			child_tag.collapse();
 		}
 	}
 	
 	private void uncollapse()
 	{
 		for (Tag child_tag : children)
 		{
 			child_tag.setVisible(true);
 			if (!child_tag.getCollapser().isCollapsed())
 			{
 				child_tag.uncollapse();
 			}
 		}
 	}
 	
 	public void setSelected(boolean selected)
 	{
 		this.selected = selected;
 	}
 	
 	public Collection<Tag> getChildren()
 	{
 		return children;
 	}
 	
 	public Collapser getCollapser()
 	{
 		return collapser;
 	}
 	
 	public JLabel getOptionsButton()
 	{
 		return options;
 	}
 	
 	public TagData getTagData()
 	{
 		return tagdata;
 	}
 	
 	public boolean isSelected()
 	{
 		return selected;
 	}
 	
 	@Override public void setForeground(Color fg)
 	{
 		super.setForeground(fg);
 		for (Component c : this.getComponents())
 		{
 			c.setForeground(fg);
 		}
 	}
 	
 	////////////////////////
 	// XCOMPONENT METHODS //
 	////////////////////////
 	
 	@Override public void entered()
 	{
 		options.setVisible(true);
 	}
 	
 	@Override public void exited()
 	{
 		options.setVisible(false);
 	}
 	
 	@Override public String getDragText()
 	{
 		return tagdata.getLabel();
 	}
 	
 	@Override public Object dragExport()
 	{
 		return tagdata;
 	}
 
 	///////////////////////////
 	// VIEW LISTENER METHODS //
 	///////////////////////////
 	
 	@Override public void fontSizeChanged(ViewEvent e)
 	{
 		for (Component c : getComponents())
 		{
 			ToolBox.changeFontSize(c, e.getFontSizeDelta());
 		}
 	}
 	
 	@Override public void propertiesChanged(ViewEvent e){}
 
 	@Override public void dataUpdated(ViewEvent e)
 	{
 		if (e.getPinnedData() == tagdata)
 		{
 			name.setText(tagdata.getLabel());
 		}
 	}
 }
