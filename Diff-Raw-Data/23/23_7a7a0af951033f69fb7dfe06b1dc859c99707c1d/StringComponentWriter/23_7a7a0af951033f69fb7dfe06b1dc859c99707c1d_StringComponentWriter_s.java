 package gda.data.scan.datawriter.scannablewriter;
 
 import gda.data.scan.datawriter.SelfCreatingLink;
 
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Vector;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.nexusformat.NeXusFileInterface;
 import org.nexusformat.NexusException;
 import org.nexusformat.NexusFile;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class StringComponentWriter extends DefaultComponentWriter {
 		private static final Logger logger = LoggerFactory.getLogger(DefaultComponentWriter.class);
 
 		int stringlength = 0;
 		int rank = 0;
 
 		public StringComponentWriter() { }
 		
		@Override
		protected int[] slabsizedimfordim(int[] dim) {
 			int[] onedimfordim = super.slabsizedimfordim(dim);
 			if (onedimfordim.length < rank)
				return ArrayUtils.add(onedimfordim, stringlength);
			onedimfordim[onedimfordim.length - 1] = stringlength;
 			return onedimfordim;
 		}
 
 		@Override
		protected Object getComponentSlab(Object pos) {
 			byte[] slab = ArrayUtils.add(pos.toString().getBytes(Charset.forName("UTF-8")), (byte) 0);
 			return slab;
 		}
 		
 		@Override
 		public Collection<SelfCreatingLink> makeComponent(NeXusFileInterface file, int[] dim, String path,
 				String scannableName, String componentName, Object pos, String unit)
 				throws NexusException {
 
 			String name = enterLocation(file, path);
 
 			stringlength = 127;
 
 			byte[] slab = (byte[]) getComponentSlab(pos);
 
 			if (Arrays.equals(dim, new int[] { 1 })) {
 				stringlength = slab.length;
 			} else if (slab.length + 10 > stringlength) { // if strings vary more than that we are in trouble
 				stringlength = slab.length + 10;
 			}
 
 			dim = makedatadimfordim(dim);
 
 			if (dim[dim.length - 1] == 1) {
 				dim[dim.length - 1] = stringlength;
 			} else {
 				dim = ArrayUtils.add(dim, stringlength);
 			}
 			rank = dim.length;
 
 			file.makedata(name, NexusFile.NX_CHAR, rank, dim);
 			file.opendata(name);
 			file.putattr("local_name", String.format("%s.%s", scannableName, componentName).getBytes(),
 					NexusFile.NX_CHAR);
 
 			file.putslab(slab, nulldimfordim(dim), slabsizedimfordim(dim));
 
 			file.closedata();
 
 			leaveLocation(file);
 			return new Vector<SelfCreatingLink>();
 		}
 		
 		@Override
 		public void writeComponent(NeXusFileInterface file, int[] start, String path, String scannableName,
 				String componentName, Object pos) throws NexusException {
 			String name = enterLocation(file, path);
 
 			file.opendata(name);
 			try {
				file.putslab(getComponentSlab(pos), putslabdimfordim(start), slabsizedimfordim(start));
 			} catch (Exception e) {
 				logger.error("error converting scannable data", e);
 			}
 			file.closedata();
 
 			leaveLocation(file);
 		}
 	}
