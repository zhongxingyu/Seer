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
 
 package ultraextreme.controller;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import ultraextreme.controller.ControllerEvent.ControllerEventType;
 import ultraextreme.view.GameOverScene;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * 
  */
 public class GameOverController extends AbstractController implements
 		IOnMenuItemClickListener {
 
 	private final GameOverScene scene;
 	private HighscoreDBOpenHelper dbOpenHelper;
 
 	public GameOverController(final Camera camera, final Font font,
 			final VertexBufferObjectManager vertexBufferObjectManager,
			HighscoreDBOpenHelper dbOpenHelper) {
 		super();
		scene = new GameOverScene(camera, font, vertexBufferObjectManager);
 		scene.setOnMenuItemClickListener(this);
 		this.dbOpenHelper = dbOpenHelper;
 	}
 
 	@Override
 	public boolean onMenuItemClicked(final MenuScene menuScene,
 			final IMenuItem menuItem, float menuItemLocalX, float menuItemLocalY) {
 		switch (menuItem.getID()) {
 		case GameOverScene.GOTO_MENU:
 			SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
 			String nameTag = "test";
 			String score = "test";
 			String sqlCommand = "INSERT INTO "
 					+ HighscoreDBOpenHelper.TABLE_NAME + " ("
 					+ HighscoreDBOpenHelper.NAME + ", "
 					+ HighscoreDBOpenHelper.HIGHSCORE + ") VALUES ('" + nameTag
 					+ "', '" + score + "')";
 			Log.d("DEBUG", "Saving highscore: " + sqlCommand);
 			database.execSQL(sqlCommand);
 			dbOpenHelper.close();
 			
 			// Testing the database
 			SQLiteDatabase readableDb = dbOpenHelper.getReadableDatabase();
 			String query = "SELECT * FROM " + HighscoreDBOpenHelper.TABLE_NAME;
 			Cursor cursor = readableDb.rawQuery(query, null);
 			cursor.moveToFirst();
 			while (!cursor.isAfterLast())
 			{
 				Log.d("DEBUG", "Cursor pointing on: " + cursor.getString(0) + ", " + cursor.getString(1));
 				cursor.moveToNext();
 			}
 			// End of database test
 			
 			fireEvent(new ControllerEvent(this,
 					ControllerEventType.SWITCH_TO_MENU));
 			break;
 
 		default:
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public Scene getScene() {
 		return scene;
 	}
 
 	@Override
 	public void activateController() {
 		// Auto-generated method stub
 
 	}
 
 	@Override
 	public void deactivateController() {
 		// Auto-generated method stub
 
 	}
 }
