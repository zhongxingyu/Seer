package GraphicsInterfaces;

import DeviceGraphics.BinGraphics;
import Utils.Location;

public interface FeederGraphics {

	public abstract void receiveBin(BinGraphics bin);
	public abstract void purgeBin(BinGraphics bin);
	public abstract void flipDiverter();
	public abstract Location getLocation();
}
