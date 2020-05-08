 package pruebas.Renders;
 
 import pruebas.Entities.Unit;
 import pruebas.Renders.helpers.UnitHelper;
 import pruebas.Util.SuperAnimation;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class UnitRender {
 	public enum FACING {
 		right, left
 	}
 
 	public enum ANIM {
 		idle, fight, walk
 	}
 
 	private Unit unit;
 
 	public SuperAnimation idleAnim;
 	public SuperAnimation fightAnim;
 	public SuperAnimation walkAnim;
 	public SuperAnimation shieldAnim;
 	private SuperAnimation currnetAnim;
 	private Texture hpBar;
 	private float hpWidth;
 	private FACING facing = FACING.right;
 
 	public UnitRender() {
 	}
 
 	public Unit getUnit() {
 		return unit;
 	}
 
 	public void setUnit(Unit u) {
 		unit = u;
 		if (unit.isEnemy()) {
 			hpBar = UnitHelper.getEnemyHPBar();
 		} else {
 			hpBar = UnitHelper.getAllyHPBar();
 		}
		updateHp();
 	}
 
 	public void setFacing(FACING at) {
 		facing = at;
 	}
 
 	public void setAnimation(ANIM anim) {
 		switch (anim) {
 		case idle: {
 			currnetAnim = idleAnim;
 		}
 			break;
 		case fight: {
 			currnetAnim = fightAnim;
 		}
 			break;
 		case walk: {
 			currnetAnim = walkAnim;
 		}
 			break;
 		}
 	}
 
 	public void updateHp() {
 		if (unit.getTotalHP() != 0) {
 			hpWidth = (unit.getHP() * UnitHelper.HP_BAR_WIDTH)
 					/ unit.getTotalHP();
 		}
 	}
 
 	public void draw(SpriteBatch batch, float dt) {
 		currnetAnim.update(dt, true, facing);
 		currnetAnim.draw(batch, unit.getX(), unit.getY());
 		batch.draw(hpBar, unit.getX() + UnitHelper.HP_BAR_X, unit.getY()
 				+ UnitHelper.HP_BAR_Y, hpWidth, UnitHelper.HP_BAR_HEIGHT);

		if (unit.isInDefensePosition()) {
 			shieldAnim.update(dt, true, facing);
 			shieldAnim.draw(batch, unit.getX(), unit.getY());
 		}
	}
 
 	public UnitRender clone() {
 		UnitRender ren = new UnitRender();
 		ren.idleAnim = idleAnim.clone();
 		ren.fightAnim = fightAnim.clone();
 		ren.walkAnim = walkAnim.clone();
 		ren.setAnimation(ANIM.idle);
 		return ren;
 	}
 }
