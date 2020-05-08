 package com.arenz.spriteeditor.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.TitledBorder;
 
 import com.arenz.spriteeditor.model.DisplayableElement;
 import com.arenz.spriteeditor.model.SpriteCategories;
 import com.arenz.spriteeditor.model.SpriteCategory;
 import com.arenz.spriteeditor.ui.components.ButtonSelectionPanel;
 import com.arenz.spriteeditor.ui.components.CategoryButton;
 import com.arenz.spriteeditor.ui.components.SpriteButton;
 
 public class SpriteSelectionView {
 	private JPanel spriteSelectionPanel = new JPanel();
 	private JScrollPane categoriesScrollPane;
 	private JScrollPane spritesScrollPane;
 	private ButtonSelectionPanel categoriesPanel = new ButtonSelectionPanel();
 	private ButtonSelectionPanel spritesPanel = new ButtonSelectionPanel();
 	private CategoryButton selectedCategory;
 	private SpriteButton selectedSprite;
 
 	private SpriteCategories categories;
 
 	public SpriteSelectionView(SpriteCategories categories) {
 		this.categories = categories;
 		spriteSelectionPanel.setBorder(BorderFactory.createRaisedBevelBorder());
 
 		spriteSelectionPanel.setLayout(new BorderLayout());
 		spritesPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		
 		categoriesScrollPane = createScrollPane("Categories", categoriesPanel);
 		spritesScrollPane = createScrollPane("Sprites", spritesPanel);
 
 		spriteSelectionPanel.add(categoriesScrollPane, BorderLayout.NORTH);
 		spriteSelectionPanel.add(spritesScrollPane, BorderLayout.CENTER);
 	}
 
 	private JScrollPane createScrollPane(String titre, JPanel contenu) {
 		JScrollPane jsp = new JScrollPane(contenu);
 		jsp.setBorder(new TitledBorder(BorderFactory.createLoweredBevelBorder(), titre));
 
 		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		jsp.setMinimumSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT/5));
		jsp.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT/5));
 		return jsp;
 	}
 
 	public Component getMainComponent() {
 		return spriteSelectionPanel;
 	}
 
 	public void addCategory(SpriteCategory category, ActionListener categoryListener) {
 		CategoryButton catButton = new CategoryButton(category);
 		catButton.addActionListener(categoryListener);
 		categoriesPanel.addButton(catButton);
 
 		spriteSelectionPanel.revalidate();
 		spriteSelectionPanel.repaint();
 	}
 
 	public void selectCategory(CategoryButton categoryButton) {
 		this.selectedCategory = categoryButton;
 		categoriesPanel.selectButton(selectedCategory);
 		
 		SpriteCategory category = categoryButton.getCategory();
 		System.out.println("Category " + category.getName() + " selected");
 	}
 	
 	public void displaySprite(DisplayableElement sprite, ActionListener spriteListener) {
 		SpriteButton spriteButton = new SpriteButton(sprite);
 		spriteButton.addActionListener(spriteListener);
 		spritesPanel.addButton(spriteButton);
 		spritesPanel.revalidate();
 		spritesPanel.repaint();
 	}
 	
 	public void selectSprite(SpriteButton spriteButton) {
 		this.selectedSprite = spriteButton;
 		spritesPanel.selectButton(selectedSprite);
 		System.out.println("Sprite " + spriteButton.getElement().getName() + " selected");
 	}
 }
