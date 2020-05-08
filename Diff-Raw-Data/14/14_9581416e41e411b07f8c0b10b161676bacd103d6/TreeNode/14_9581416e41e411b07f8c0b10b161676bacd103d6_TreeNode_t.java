 package edu.berkeley.gamesman.propogater.tree;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparable;
 
 import edu.berkeley.gamesman.propogater.factory.Factory;
 import edu.berkeley.gamesman.propogater.writable.BitSetWritable;
 import edu.berkeley.gamesman.propogater.writable.Entry;
 import edu.berkeley.gamesman.propogater.writable.IntEntry;
 import edu.berkeley.gamesman.propogater.writable.ValueWrapper;
 import edu.berkeley.gamesman.propogater.writable.list.WritableList;
 
 public class TreeNode<K extends WritableComparable<K>, V extends Writable, PI extends Writable, UM extends Writable, CI extends Writable, DM extends Writable>
 		extends Configured implements Writable {
 	private ValueWrapper<V> value;
 	private int lastParentsLength = 0;
 	private WritableList<IntEntry<Entry<K, PI>>> parents;
 	private final BitSetWritable cleanSet = new BitSetWritable();
 	private WritableList<Entry<K, CI>> children;
 
 	private WritableList<IntEntry<UM>> uMess;
 
 	private WritableList<IntEntry<Entry<K, DM>>> dMess;
 
 	private boolean combine = false;
 	private ValueWrapper<V> tempValue;
 
 	public TreeNode() {
 	}
 
 	public TreeNode(Configuration conf) {
 		super(conf);
 	}
 
 	@Override
 	public void readFields(DataInput in) throws IOException {
 		if (combine) {
 			tempValue.readFields(in);
 			if (tempValue.hasValue()) {
 				swapValues();
 				readMain(in);
 			}
 
 			parents.addFields(in);
 			// This works since only one of the inputs will have non-empty
			// parents. It's important since the value may not have
 			// been set yet
 
 			uMess.addFields(in);
 			dMess.addFields(in);
 		} else {
 			value.readFields(in);
 			if (value.hasValue())
 				readMain(in);
 			parents.readFields(in);
 			uMess.readFields(in);
 			dMess.readFields(in);
 		}
 	}
 
 	private void readMain(DataInput in) throws IOException {
 		lastParentsLength = in.readInt();
 		cleanSet.readFields(in);
 		children.readFields(in);
 	}
 
 	private void swapValues() {
 		ValueWrapper<V> tmp = tempValue;
 		tempValue = value;
 		value = tmp;
 	}
 
 	@Override
 	public void write(DataOutput out) throws IOException {
 		value.write(out);
 		if (value.hasValue())
 			writeMain(out);
 		parents.write(out);
 		uMess.write(out);
 		dMess.write(out);
 	}
 
 	private void writeMain(DataOutput out) throws IOException {
 		out.writeInt(lastParentsLength);
 		cleanSet.write(out);
 		children.write(out);
 	}
 
 	@Override
 	public void setConf(final Configuration conf) {
 		super.setConf(conf);
 		if (conf != null) {
 			final Class<? extends K> kClass = Tree.getRunKClass(conf);
 			final Class<? extends V> vClass = Tree.getRunVClass(conf);
 			final Class<? extends PI> piClass = Tree.getRunPiClass(conf);
 			final Class<? extends CI> ciClass = Tree.getRunCiClass(conf);
 			value = new ValueWrapper<V>(vClass, conf);
 			tempValue = new ValueWrapper<V>(vClass, conf);
 			parents = new WritableList<IntEntry<Entry<K, PI>>>(
 					new Factory<IntEntry<Entry<K, PI>>>() {
 
 						@Override
 						public IntEntry<Entry<K, PI>> create() {
 							return new IntEntry<Entry<K, PI>>(new Entry<K, PI>(
 									kClass, piClass, conf));
 						}
 					});
 			children = new WritableList<Entry<K, CI>>(
 					new Factory<Entry<K, CI>>() {
 						@Override
 						public Entry<K, CI> create() {
 							return new Entry<K, CI>(kClass, ciClass, conf);
 						}
 					});
 			final Class<? extends UM> umClass = Tree.getRunUmClass(conf);
 			final Class<? extends DM> dmClass = Tree.getRunDmClass(conf);
 			uMess = new WritableList<IntEntry<UM>>(new Factory<IntEntry<UM>>() {
 				@Override
 				public IntEntry<UM> create() {
 					return new IntEntry<UM>(umClass, conf);
 				}
 			});
 			dMess = new WritableList<IntEntry<Entry<K, DM>>>(
 					new Factory<IntEntry<Entry<K, DM>>>() {
 						@Override
 						public IntEntry<Entry<K, DM>> create() {
 							return new IntEntry<Entry<K, DM>>(new Entry<K, DM>(
 									kClass, dmClass, conf));
 						}
 					});
 		}
 	}
 
 	public boolean hasValue() {
 		return value.hasValue();
 	}
 
 	private final NodeAdder<K, CI, DM> fvAdder = new NodeAdder<K, CI, DM>();
 	private final ParList<K, PI> parList = new ParList<K, PI>();
 
 	public void firstVisit(Tree<K, V, PI, UM, CI, DM> tree, K key,
 			WritableList<DM> childMessagesToFill) {
		children.clear();
		cleanSet.clear();
 		fvAdder.setList(children, childMessagesToFill);
 		parList.setList(parents);
 		tree.firstVisit(key, value.setHasAndGet(), parList, fvAdder);
 		lastParentsLength = parents.length();
 		if (children.length() != childMessagesToFill.length()) {
 			throw new RuntimeException(
 					"Must have exactly one message per child");
 		}
 	}
 
 	public void combineDown(Tree<K, V, PI, UM, CI, DM> tree, K key) {
		if (!value.hasValue())
			throw new RuntimeException("firstVisit should be called first");
 		parList.setList(parents);
 		tree.combineDown(key, value.get(), parList, lastParentsLength, children);
 		lastParentsLength = parents.length();
 		cleanSet.clear();
 	}
 
 	public boolean combineUp(Tree<K, V, PI, UM, CI, DM> tree, K key) {
 		parList.setList(parents);
 		return tree.combineUp(key, value.get(), parList, children);
 	}
 
 	public void beginCombine() {
 		combine = true;
 	}
 
 	public void endCombine() {
 		combine = false;
 	}
 
 	public V getValue() {
 		return value.get();
 	}
 
 	public WritableList<IntEntry<Entry<K, PI>>> getParentList() {
 		return parents;
 	}
 
 	public WritableList<Entry<K, CI>> getChildren() {
		if (!value.hasValue())
			throw new RuntimeException("firstVisit should be called first");
 		return children;
 	}
 
 	public WritableList<IntEntry<Entry<K, DM>>> getDownList() {
 		return dMess;
 	}
 
 	public BitSetWritable getCleanSet() {
		if (!value.hasValue())
			throw new RuntimeException("firstVisit should be called first");
 		return cleanSet;
 	}
 
 	public WritableList<IntEntry<UM>> getUpList() {
 		return uMess;
 	}
 }
