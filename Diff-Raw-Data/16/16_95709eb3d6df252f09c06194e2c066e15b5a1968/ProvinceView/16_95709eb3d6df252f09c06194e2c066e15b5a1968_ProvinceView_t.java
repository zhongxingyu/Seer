 package se.chalmers.dat255.risk.view;
 
 import se.chalmers.dat255.risk.model.IProvince;
 import se.chalmers.dat255.risk.view.resource.ColorHandler;
 import se.chalmers.dat255.risk.view.resource.Resource;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class ProvinceView extends AbstractView {
 
 	private IProvince province;
 
 	public ProvinceView(IProvince province, int x, int y) {
 		super(Resource.getInstance().circle, Resource.getInstance().circleSelected);
 		this.province = province;
 		setSize(width / 2, height / 2);
 		setPosition(x, y);
 		scale = width / 2;
 	}
 
 	private float getTextX() {
 		return getX() + (width / 8);
 	}
 
 	private float getTextY() {
		return getY() + (height / 2);
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float alpha) {
 
 		batch.setColor(ColorHandler.getInstance().getProvinceColor(
 				province.getId()));
 		batch.draw(province.isActive() ? imageDown : imageUp, getX(), getY(),
 				scale, scale);
 		font.setColor(Color.WHITE);
 		font.draw(batch, "" + province.getUnits(), getTextX(), getTextY());
 	}
 
 	public IProvince getProvince() {
 		return province;
 	}
 }
