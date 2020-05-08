 /**
  * Dark Beam
  * TileMasking.java
  * 
  * @author krakel
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 package de.krakel.darkbeam.tile;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 
 import de.krakel.darkbeam.client.renderer.IMaskRenderer;
 import de.krakel.darkbeam.core.MaskLib;
 import de.krakel.darkbeam.core.Position;
 import de.krakel.darkbeam.core.helper.LogHelper;
 
 public class TileMasking extends TileEntity implements Iterable<Integer> {
 	private static final String NBT_AREAS = "areas";
 	private static final String NBT_MASKS = "masks";
 	private int mArea;
 	private int[] mArr = new int[32];
 
 	public TileMasking() {
 	}
 
 	public int getArea() {
 		return mArea;
 	}
 
 	@Override
 	public Packet getDescriptionPacket() {
 		NBTTagCompound nbt = new NBTTagCompound();
 		writeToNBT( nbt);
 		return new Packet132TileEntityData( xCoord, yCoord, zCoord, 0, nbt);
 	}
 
 	public IMaskRenderer getMaskRenderer( int area) {
 		int meta = getMeta( area);
 		return MaskLib.getRendererForDmg( meta);
 	}
 
 	public int getMeta( int area) {
 		try {
 			return mArr[area];
 		}
 		catch (IndexOutOfBoundsException ex) {
 			return 0;
 		}
 	}
 
 	public boolean isEmpty() {
 		return mArea == 0;
 	}
 
 	public boolean isValid( int value) {
 		return (mArea & value) == 0;
 	}
 
 	@Override
 	public Iterator<Integer> iterator() {
 		return new AreaIterator( mArea);
 	}
 
 	@Override
 	public void onDataPacket( INetworkManager net, Packet132TileEntityData paket) {
 		readFromNBT( paket.customParam1);
 	}
 
 	@Override
 	public void readFromNBT( NBTTagCompound nbt) {
 		LogHelper.info( "readFromNBT: %s", nbt);
 		super.readFromNBT( nbt);
 		int areas = nbt.getInteger( NBT_AREAS);
 		byte[] arr = nbt.getByteArray( NBT_MASKS);
 		int n = 0;
 		for (int a = areas, i = 0; a != 0; a >>>= 1, ++i) {
 			if ((a & 1) != 0) {
 				int mat = arr[n++] & 0xFF;
 				int msk = arr[n++] & 0xFF;
 				mArr[i] = msk << 8 | mat;
 			}
 		}
 		mArea |= areas;
 	}
 
 	public void reset() {
 		mArea = 0;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer sb = new StringBuffer( "TileMasking[");
 		for (int a = mArea, i = 0; a != 0; a >>>= 1, ++i) {
 			if (i > 0) {
 				sb.append( ',');
 			}
 			if ((a & 1) != 0) {
 				int value = mArr[i];
 				sb.append( value >> 8 & 0xFF);
 				sb.append( '|');
 				sb.append( value & 0xFF);
 			}
 			else {
 				sb.append( "-|-");
 			}
 		}
 		sb.append( "]");
 		return sb.toString();
 	}
 
 	public boolean tryAdd( int area, int meta) {
 		try {
 			int off = 1 << area;
 			if (isValid( off)) {
 				mArr[area] = meta;
 				mArea |= off;
 				LogHelper.info( "tryAdd: %b, %s, %s", worldObj != null && worldObj.isRemote, Position.toString( area), toString());
 				return true;
 			}
 		}
 		catch (IndexOutOfBoundsException ex) {
 		}
 		return false;
 	}
 
 	public int tryRemove( int area) {
 		try {
 			int off = 1 << area;
 			if (!isValid( off)) {
 				int value = mArr[area];
 				mArr[area] = 0;
 				mArea &= ~off;
 				LogHelper.info( "tryRemove: %b, %s, %s", worldObj != null && worldObj.isRemote, Position.toString( area), toString());
 				return value;
 			}
 		}
 		catch (IndexOutOfBoundsException ex) {
 		}
 		return -1;
 	}
 
 	@Override
 	public void updateEntity() {
		if (worldObj != null) {
			worldObj.markBlockForUpdate( xCoord, yCoord, zCoord);
		}
 	}
 
 	public boolean validate( int area, IMaskRenderer rndr) {
 		return true;
 	}
 
 	@Override
 	public void writeToNBT( NBTTagCompound nbt) {
 		super.writeToNBT( nbt);
 		int n = 0;
 		for (int a = mArea; a != 0; a >>>= 1) {
 			if ((a & 1) != 0) {
 				n += 2;
 			}
 		}
 		nbt.setInteger( NBT_AREAS, mArea);
 		byte[] arr = new byte[n];
 		n = 0;
 		for (int a = mArea, i = 0; a != 0; a >>>= 1, ++i) {
 			if ((a & 1) != 0) {
 				int value = mArr[i];
 				arr[n++] = (byte) (value & 0xFF);
 				arr[n++] = (byte) (value >> 8 & 0xFF);
 			}
 		}
 		nbt.setByteArray( NBT_MASKS, arr);
 		LogHelper.info( "writeToNBT: %s", nbt);
 	}
 
 	private static final class AreaIterator implements Iterator<Integer> {
 		private int mArea;
 		private int mIndex;
 
 		private AreaIterator( int area) {
 			mArea = area;
 			findNext();
 		}
 
 		private void findNext() {
 			while (mArea != 0 && (mArea & 1) == 0) {
 				mArea >>>= 1;
 				++mIndex;
 			}
 		}
 
 		@Override
 		public boolean hasNext() {
 			return mArea != 0;
 		}
 
 		@Override
 		public Integer next() {
 			if (mArea == 0) {
 				throw new NoSuchElementException( "No more elements");
 			}
 			Integer n = mIndex;
 			findNext();
 			return n;
 		}
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException( "Removal not supported");
 		}
 	}
 }
