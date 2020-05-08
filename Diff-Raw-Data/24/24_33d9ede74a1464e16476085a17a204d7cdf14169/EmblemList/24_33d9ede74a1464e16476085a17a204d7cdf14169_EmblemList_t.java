 package com.crystalclash.renders.helpers.ui;
 
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.crystalclash.controllers.GameController;
 
 public class EmblemList extends Group {
 	private Table table;
 	private ScrollPane scrollPane;
 	private EmblemListItem selectedItem;
 
 	public EmblemList() {
 		table = new Table();
 		scrollPane = new ScrollPane(table);
 		scrollPane.setScrollingDisabled(true, false);
 		scrollPane.setOverscroll(false, true);
 		scrollPane.setSmoothScrolling(true);
 		scrollPane.setForceScroll(false, true);
 		scrollPane.setFillParent(true);
 		scrollPane.invalidate();
 		addActor(scrollPane);
 
 		table.align(Align.top | Align.left);
 		table.defaults().width(100).height(100).padLeft(3).padTop(3);
 
 		ClickListener unitItemClickListener = new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				EmblemListItem item = (EmblemListItem) event.getListenerActor().getParent();
 				if (item != null) {
 					if (item != selectedItem) {
 						if (selectedItem != null)
 							selectedItem.desselect();
 						item.select();
 						selectedItem = item;
 					}
 				}
 			}
 		};
 		
		for (int i = 0; i < GameController.EMBLEM_COUNT; i++) {
 			if (i % 7 == 0) {
 				table.row();
 			}
 			EmblemListItem item = new EmblemListItem(i, unitItemClickListener);
 			if (i == GameController.getUser().getEmblem()) {
 				item.select();
 				selectedItem = item;
 			}
 			table.add(item);
 		}
 	}
 
 	public int getSelectedEmblem() {
 		return selectedItem.getEmblemNumber();
 	}
 }
