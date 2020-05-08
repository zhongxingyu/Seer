 package net.malariagen.gatk.coverage;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.samtools.AlignmentBlock;
 import net.sf.samtools.SAMReadGroupRecord;
 import net.sf.samtools.SAMRecord;
 
 import org.broadinstitute.sting.utils.sam.GATKSAMRecord;
 
 public abstract class FragmentLengths {
 
 	public class FirstMate {
 		int start;
 		int stop;
 		int length;
 		int trim;
 	}
 
 	protected long size;
 
 	protected int maxLength;
 	protected Collection<String> samples;
 	protected Collection<String> readGroups;
 	protected Map<String, Integer> rgIndex;
 	protected Map<String, Integer> smIndex;
 	protected Map<String, FirstMate> firstMates = new HashMap<String, FirstMate>(
 			100);
 
 	public static long FREQ_SIZE_THR = 1000;
 
 	public static FragmentLengths create(Collection<String> samples,
 			Collection<String> rgs, int maxLength) {
 		return new FragmentLengthArrays(samples, rgs, maxLength);
 	}
 	
 	public static FragmentLengths create(Collection<String> samples,
 			Collection<String> rgs, int maxLength, Class<? extends FragmentLengths> clazz) {
 		if (!FragmentLengths.class.isAssignableFrom(clazz))
 			throw new IllegalArgumentException("not a fl class");
 		Constructor<? extends FragmentLengths> c;
 		try {
 			c = clazz.getConstructor(Collection.class, Collection.class, Integer.TYPE);
 		} catch (NoSuchMethodException e) {
 			throw new UnsupportedOperationException("no suitable constructor found in " + clazz.getName(),e);
 		} catch (SecurityException e) {
 			throw new UnsupportedOperationException("no suitable constructor found in " + clazz.getName(),e);
 		}
 		if (c == null)
 			throw new UnsupportedOperationException("no suitable constructor found in " + clazz.getName());
 		FragmentLengths result;
 		try {
 			result = c.newInstance(samples,rgs,maxLength);
 		} catch (InstantiationException e) {
 			throw new UnsupportedOperationException("no suitable constructor found in " + clazz.getName(),e);
 		} catch (IllegalAccessException e) {
 			throw new UnsupportedOperationException("no suitable constructor found in " + clazz.getName(),e);
 		} catch (IllegalArgumentException e) {
 			throw new UnsupportedOperationException("no suitable constructor found in " + clazz.getName(),e);
 		} catch (InvocationTargetException e) {
 			throw new UnsupportedOperationException("no suitable constructor found in " + clazz.getName(),e);
 		}
 		return result;
 	}
 
 	public static FragmentLengths merge(FragmentLengths fl1, FragmentLengths fl2) {
 		long newSize = fl1.size + fl2.size;
 
 		if (newSize > FREQ_SIZE_THR
 				&& !(fl1 instanceof FragmentLengthFrequencies)) {
 			FragmentLengthFrequencies result = new FragmentLengthFrequencies(
 					fl1.samples, fl1.readGroups, fl1.maxLength);
 			result.mergeIn(fl1);
 			result.mergeIn(fl2);
 			return result;
 		} else if (fl1.getClass().equals(fl2.getClass())) {
 			fl1.mergeIn(fl2);
 			return fl1;
 		} else if (fl1 instanceof FragmentLengthFrequencies) {
 			fl1.mergeIn(fl2);
 			return fl1;
 		} else {
 			FragmentLengthFrequencies result = new FragmentLengthFrequencies(
 					fl1.samples, fl1.readGroups, fl1.maxLength);
 			result.mergeIn(fl1);
 			result.mergeIn(fl2);
 			return result;
 		}
 	}
 
 	protected FragmentLengths(Collection<String> samples,
 			Collection<String> rgs, int maxLength) {
 		if (maxLength <= 0)
 			throw new IllegalArgumentException(
 					"max-fragment length must be more than 0");
 		this.maxLength = maxLength;
 		this.samples = samples;
 		this.readGroups = rgs;
 		int nextSmIdx = 0;
 		int nextRgIdx = 0;
 		smIndex = new HashMap<String, Integer>(samples.size());
 		rgIndex = new HashMap<String, Integer>(rgs.size());
 		for (String s : samples)
 			smIndex.put(s, nextSmIdx++);
 		for (String rg : rgs)
 			rgIndex.put(rg, nextRgIdx++);
 		firstMates = new HashMap<String, FirstMate>(100);
 
 	}
 
 	public boolean add(SAMRecord read) {
 		if (read == null)
 			throw new IllegalArgumentException("input read cannot be null");
 		FirstMate fm =  firstMates.remove(read.getReadName());;
 		if (fm != null)
 			return countSecond(read,fm);
 		else
 			return countFirst(read);
 	}
 
 	private boolean countFirst(SAMRecord read) {
 		if (read.getReadUnmappedFlag())
 			return false;
 		if (read.getMateUnmappedFlag())
 			return false;
 		String mateRef = read.getMateReferenceName();
 		if (!mateRef.equals("=") && !mateRef.equals(read.getReferenceName()))
 			return false;
 		int mateStart = read.getMateAlignmentStart();
 		int start = read.getAlignmentStart();
 		if (mateStart - start > maxLength || start - mateStart > maxLength)
 			return false;
 		if (read.getReadNegativeStrandFlag())
 			return false;
 		FirstMate fm = new FirstMate();
 		fm.length = read.getReadLength();
 		fm.start = read.getAlignmentStart();
 		fm.stop = read.getAlignmentEnd();
 		fm.trim = (fm.stop - fm.start + 1 == fm.length) ? 0
 				: calculateTrim(read);
 		firstMates.put(read.getReadName(), fm);
 		return false;
 	}
 
 	private int calculateTrim(SAMRecord read) {
 		List<AlignmentBlock> blocks = read.getAlignmentBlocks();
 		int lastPos = 0;
 		int result = 0;
 		for (AlignmentBlock b : blocks) {
 			result += b.getReadStart() - lastPos - 1;
 			lastPos = b.getReadStart() + b.getLength() - 1;
 		}
 		result += read.getReadLength() - lastPos - 1;
 		return result;
 	}
 
 	protected boolean countSecond(SAMRecord read, FirstMate fm) {
 		if (!read.getReadNegativeStrandFlag())
 			return false;
 		int start = read.getAlignmentStart();
 		int end = read.getAlignmentEnd();
 		int length = read.getReadLength();
 		int trim = ((end - start + 1 == length) ? 0 : calculateTrim(read));
 		int fragmentLength = end - fm.start + 1 + fm.trim + trim;
 		int insertLength = fragmentLength - length - fm.length;
 		SAMReadGroupRecord rg = read.getReadGroup();
 		if (rg == null)
 			throw new IllegalArgumentException(
 					"reads need to belong to a read-group");
 		Integer smIdx = smIndex.get(rg.getSample());
 		Integer rgIdx = rgIndex.get(rg.getId());
 		if (smIdx == null)
 			return false;
 		if (rgIdx == null)
 			return false;
 		if (fragmentLength <= fm.length + length)
 			return false;
		if (fragmentLength > maxLength)
			return false;
 		addLengths(fragmentLength, insertLength, smIdx, rgIdx);
 		size++;
 		return true;
 	}
 
 	protected abstract void addLengths(int fragmentLength, int insertLength,
 			Integer smIdx, Integer rgIdx);
 
 	public void mergeIn(FragmentLengths other) {
 		if (other == null)
 			return;
 		else if (other instanceof FragmentLengthFrequencies) {
 			mergeIn((FragmentLengthFrequencies) other);
 		} else if (other instanceof FragmentLengthArrays) {
 			mergeIn((FragmentLengthArrays) other);
 		} else {
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	public void mergeIn(FragmentLengthArrays other) {
 		throw new UnsupportedOperationException("yet not implemented");
 	}
 
 	public void mergeIn(FragmentLengthFrequencies other) {
 		throw new UnsupportedOperationException("yet not implemented");
 	}
 
 	public static FragmentLengths add(GATKSAMRecord value, FragmentLengths sum) {
 		if (sum.add(value))
 			if (sum instanceof FragmentLengthArrays && sum.size > FREQ_SIZE_THR) {
 				FragmentLengthFrequencies result = new FragmentLengthFrequencies(
 						sum.samples, sum.readGroups, sum.maxLength);
 				result.mergeIn(sum);
 				return result;
 			}
 		return sum;
 	}
 
 	public long size() {
 		return size;
 	}
 
 	public abstract FragmentLengthSummary summary();
 
 }
