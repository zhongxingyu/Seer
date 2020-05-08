 package se.chalmers.dat255.risk.view;
 
 import se.chalmers.dat255.risk.model.IProvince;
 import se.chalmers.dat255.risk.view.resource.ColorHandler;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class ProvinceView extends AbstractView {
 
 	private IProvince province;
 
 	public ProvinceView(IProvince province, int x, int y) {
 		super(Resource.getInstance().circle, Resource.getInstance().triangle);
 		this.province = province;
		setSize(width/2, height/2);
 		setPosition(x, y);
 		setScale(width / 2, height / 2);
 	}
 
 	public float getCenterX() {
 		return getX() + (getWidth() / 4);
 	}
 
 	public float getCenterY() {
 		return getY() + (getWidth() / 4);
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float alpha) {
 
 		batch.setColor(ColorHandler.getInstance().getProvinceColor(
 				province.getId()));
 		super.draw(batch, alpha);
 		font.setColor(Color.RED);
 		font.draw(batch, "" + province.getUnits(), getCenterX(), getCenterY());
 	}
 
 	public IProvince getProvince() {
 		return province;
 	}
 }
