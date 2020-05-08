 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.view;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.TextMenuItem;
import org.andengine.helperclasses.InputText;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.color.Color;
 
 import ultraextreme.util.Resources;
 import ultraextreme.util.Resources.ResourceName;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * 
  */
 public class GameOverScene extends MenuScene {
 
 	public static final int GOTO_MENU = 0;
 
 	public GameOverScene(final Camera camera, final Font font,
			final VertexBufferObjectManager vertexBufferObjectManager, BaseGameActivity activity) {
 		super(camera);
 		setBackground(new Background(0.9f, 0.1f, 0.1f));
 		final IMenuItem gotoMenuButton = new TextMenuItem(GOTO_MENU, font,
 				Resources.getInstance().getResource(ResourceName.GOTO_MENU),
 				vertexBufferObjectManager);
 		gotoMenuButton.setPosition(100, 100);
 		gotoMenuButton.setColor(Color.BLACK);
 		addMenuItem(gotoMenuButton);
 		
		InputText nameInput = new InputText(100, 200, "Input name", "Input name", null, font, 17, 19, vertexBufferObjectManager, activity);
		this.attachChild(nameInput);
		this.registerTouchArea(nameInput);
 	}
 }
