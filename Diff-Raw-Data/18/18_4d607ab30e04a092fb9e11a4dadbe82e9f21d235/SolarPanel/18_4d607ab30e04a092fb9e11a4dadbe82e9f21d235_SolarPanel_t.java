 package com.evervoid.client.views.solar;
 
 import com.evervoid.client.ui.UIControl;
 import com.evervoid.client.views.Bounds;
 import com.evervoid.client.views.EverView;
 import com.jme3.math.Vector2f;
 
 /**
  * Bottom bar on the solar system view
  */
 public class SolarPanel extends EverView
 {
 	private final UIControl aRootUI;
 
 	public SolarPanel()
 	{
 		aRootUI = new UIControl();
 		addNode(aRootUI);
 	}
 
 	@Override
 	public boolean onLeftClick(final Vector2f position, final float tpf)
 	{
 		return false;
 	}
 
 	@Override
 	protected void setBounds(final Bounds pBounds)
 	{
 		super.setBounds(pBounds);
 		aRootUI.setBounds(pBounds);
 	}
 
 	public void setUI(final UIControl ui)
 	{
 		aRootUI.delAllChildUIs();
 		aRootUI.addUI(ui, 1);
 		aRootUI.setBounds(getBounds());
 	}
 }
