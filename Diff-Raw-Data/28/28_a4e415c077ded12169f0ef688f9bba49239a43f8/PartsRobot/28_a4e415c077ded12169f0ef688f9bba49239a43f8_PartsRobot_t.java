package factory.interfaces;

import factory.KitConfig;

public interface PartsRobot {

	public void msgBuildKitAtSlot(String slot);
	public void msgDeliverKitParts();
	public void msgMakeKit(KitConfig kitConfig);
}
