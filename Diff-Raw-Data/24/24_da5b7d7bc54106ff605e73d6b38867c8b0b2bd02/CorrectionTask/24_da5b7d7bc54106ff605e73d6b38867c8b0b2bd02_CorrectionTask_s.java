 package com.sun.darkstar.example.snowman.game.task.state.battle;
 
 import com.sun.darkstar.example.snowman.common.entity.view.View;
 import com.sun.darkstar.example.snowman.common.interfaces.IEntity;
 import com.sun.darkstar.example.snowman.game.Game;
 import com.sun.darkstar.example.snowman.game.entity.util.EntityManager;
 import com.sun.darkstar.example.snowman.game.entity.view.util.ViewManager;
 import com.sun.darkstar.example.snowman.game.task.RealTimeTask;
 import com.sun.darkstar.example.snowman.game.task.enumn.ETask;
 
 /**
  * <code>CorrectionTask</code> extends <code>RealTimeTask</code> to correct
  * the position of a character.
  * <p>
  * <code>CorrectionTask</code> execution logic:
  * 1. Retrieve the character entity and view based on given ID.
  * 2. Teleport the character to the given coordinates.
  * <p>
  * <code>CorrectionTask</code> are considered 'equal' if and only if the
  * <code>CharacterEntity</code> are 'equal'.
  * 
  * @author Yi Wang (Neakor)
  * @version Creation date: 08-07-2008 15:01 EST
  * @version Modified date: 08-07-2008 15:09 EST
  */
 public class CorrectionTask extends RealTimeTask {
 	/**
 	 * The ID number of the character to be corrected.
 	 */
 	private final int id;
 	/**
 	 * The x coordinate to correct to.
 	 */
 	private final float x;
 	/**
 	 * The z coordinate to correct to.
 	 */
 	private final float z;
 
 	/**
 	 * Constructor of <code>CorrectionTask</code>.
 	 * @param game The <code>Game</code> instance.
 	 * @param id The ID number of the character to be corrected.
 	 * @param x The x coordinate to correct to.
 	 * @param z The z coordinate to correct to.
 	 */
 	public CorrectionTask(Game game, int id, float x, float z) {
 		super(ETask.Correction, game);
 		this.id = id;
 		this.x = x;
 		this.z = z;
 	}
 
 	@Override
 	public void execute() {
 		// Step 1.
		IEntity entity = EntityManager.getInstance().getEntity(this.id);
 		View view = (View)ViewManager.getInstance().getView(entity);
 		// Step 2.
 		view.getLocalTranslation().x = this.x;
 		view.getLocalTranslation().z = this.z;
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if(super.equals(object)) {
 			if(object instanceof CorrectionTask) {
 				CorrectionTask given = (CorrectionTask)object;
 				return (this.id == given.id);
 			}
 		}
 		return false;
 	}
 }
