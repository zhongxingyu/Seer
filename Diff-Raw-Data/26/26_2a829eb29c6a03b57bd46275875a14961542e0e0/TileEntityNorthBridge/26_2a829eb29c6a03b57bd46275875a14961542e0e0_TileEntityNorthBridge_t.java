 package eu.tomylobo.ccredbus.common;
 
 import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import com.eloraam.redpower.core.IConnectable;
 import com.eloraam.redpower.core.IRedbusConnectable;
 
 import dan200.computer.api.IComputerAccess;
 import dan200.computer.api.IPeripheral;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 
 public class TileEntityNorthBridge extends TileEntity implements IPeripheral, IConnectable {
 	// IPeripheral
 	@Override
 	public String getType() {
 		return "redbus";
 	}
 
 	private static final String[] methodNames = {
 		"getAddr",
 		"setAddr",
 		"read",
 		"write",
 		"sortronProcessCommand",
 	};
 
 	@Override
 	public String[] getMethodNames() {
 		return methodNames;
 	}
 
 	private static final Method sortronProcessCommand;
 	private static final Constructor<?> worldCoordConstructor;
 	private static final Method redbusLibGetAddr;
 	static {
 		try {
			final Class<?> sortronClass = Class.forName("com.eloraam.redpower.machine.TileSortron");
 			sortronProcessCommand = sortronClass.getDeclaredMethod("processCommand", new Class<?>[0]);
 			sortronProcessCommand.setAccessible(true);
 
 			final Class<?> worldCoordClass = Class.forName("com.eloraam.redpower.core.WorldCoord");
 			worldCoordConstructor = worldCoordClass.getConstructor(TileEntity.class);
 
 			final Class<?> redbusLibClass = Class.forName("com.eloraam.redpower.core.RedbusLib");
 			redbusLibGetAddr = redbusLibClass.getMethod("getAddr", IBlockAccess.class, worldCoordClass, int.class);
 		}
 		catch (ClassNotFoundException e) {
 			e.printStackTrace();
			throw new RuntimeException(String.format("Your version of RedPower is incompatible with this version of CCRedbus. "+e.getMessage(), e.getClass().getCanonicalName()), e);
 		}
 		catch (NoSuchMethodException e) {
 			e.printStackTrace();
			throw new RuntimeException(String.format("Your version of RedPower is incompatible with this version of CCRedbus. "+e.getMessage(), e.getClass().getCanonicalName()), e);
 		}
 	}
 
 	@Override
 	public Object[] callMethod(IComputerAccess computer, int methodIndex, Object[] args) throws Exception {
 		final int slaveId = ((Number) args[0]).intValue();
 
 		final IRedbusConnectable dev = getSlaveById(slaveId);
		if (dev == null) {
			throw new Exception("Device with id "+slaveId+" not found.");
		}
 
 		switch (methodIndex) {
 		case 0: { // getAddr
 			return wrap(dev.rbGetAddr());
 		}
 
 		case 1: { // setAddr
 			final int address = ((Number) args[1]).intValue();
 
 			dev.rbSetAddr(address);
 
 			return wrap();
 		}
 
 		case 2: { // read
 			final int offset = ((Number) args[1]).intValue();
 
 			return wrap(dev.rbRead(offset));
 		}
 
 		case 3: { // write
 			final int offset = ((Number) args[1]).intValue();
 			final int value = ((Number) args[2]).intValue();
 
 			dev.rbWrite(offset, value);
 
 			return wrap();
 		}
 
 		case 4: { // sortronProcessCommand
			try {
				sortronProcessCommand.invoke(dev);
			}
			catch (IllegalArgumentException e) {
				throw new Exception("That's not a Sortron!", e);
			}
 			return wrap();
 		}
 		}
 
 		return wrap();
 	}
 
	private IRedbusConnectable getSlaveById(int slaveId) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
 		return (IRedbusConnectable) redbusLibGetAddr.invoke(null, worldObj, worldCoordConstructor.newInstance(this), slaveId);
 	}
 
 	@Override
 	public boolean canAttachToSide(int paramInt) {
 		return true;
 	}
 
 	@Override
 	public void attach(IComputerAccess computer) {
 	}
 
 	@Override
 	public void detach(IComputerAccess computer) {
 	}
 
 	private static Object[] wrap(Object... args) {
 		return args;
 	}
 
 
 	// IConnectable
 	@Override
 	public int getConnectableMask() {
 		return 16777215;
 	}
 
 	@Override
 	public int getConnectClass(int side) {
 		return 66;
 	}
 
 	@Override
 	public int getCornerPowerMode() {
 		return 0;
 	}
 }
