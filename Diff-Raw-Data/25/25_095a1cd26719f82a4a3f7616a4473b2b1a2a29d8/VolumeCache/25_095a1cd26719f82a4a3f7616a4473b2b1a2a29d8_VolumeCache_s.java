 package edu.uw.zookeeper.safari.data;
 
 import java.util.Set;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicReference;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.MapMaker;
 
 import edu.uw.zookeeper.common.Reference;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.data.ZNodeLabelTrie;
 import edu.uw.zookeeper.safari.Identifier;
 
 public class VolumeCache {
 
     public static VolumeCache newInstance() {
         return new VolumeCache(
                 ZNodeLabelTrie.of(VolumeLookupNode.root()), 
                 new MapMaker().<Identifier, VolumeLookupNode>makeMap());
     }
     
     protected final ZNodeLabelTrie<VolumeLookupNode> lookupTrie;
     protected final ConcurrentMap<Identifier, VolumeLookupNode> byVolumeId;
     
     protected VolumeCache(
             ZNodeLabelTrie<VolumeLookupNode> lookupTrie,
             ConcurrentMap<Identifier, VolumeLookupNode> byVolumeId) {
         this.lookupTrie = lookupTrie;
         this.byVolumeId = byVolumeId;
     }
     
     public void clear() {
         lookupTrie.clear();
         byVolumeId.clear();
     }
 
     public ZNodeLabelTrie<VolumeLookupNode> asTrie() {
         return lookupTrie;
     }
     
     public Volume get(Identifier id) {
         VolumeLookupNode node = byVolumeId.get(id);
         return (node == null) ? null : node.get();
     }
 
     public Volume get(ZNodeLabel.Path path) {
         VolumeLookupNode node = asTrie().longestPrefix(path);
         Volume volume = null;
         while ((volume == null) && (node != null) && node.path().prefixOf(path)) {
             volume = node.get();
             node = node.parent().isPresent() ? node.parent().get().get() : null;
         }
        return volume;
     }
 
     public Volume put(Volume volume) {
         VolumeLookupNode node = byVolumeId.get(volume.getId());
         if (node == null) {
             synchronized (this) {
                 ZNodeLabel.Path volumeRoot = volume.getDescriptor().getRoot();
                 node = asTrie().root().add(volumeRoot);
                 VolumeLookupNode prev = byVolumeId.putIfAbsent(volume.getId(), node);
                 if (prev != null) {
                     if (prev != node) {
                         throw new AssertionError();
                     }
                 }
             }
         }
         return node.set(volume);
     }
 
     public Volume remove(Identifier id) {
         Volume prev = null;
         synchronized (this) {
             VolumeCache.VolumeLookupNode node = byVolumeId.remove(id);
             if (node != null) {
                 prev = node.getAndSet(null);
                 // delete empty leaves
                 while (node.isEmpty() && (node.get() == null)) {
                     ZNodeLabelTrie.Pointer<VolumeCache.VolumeLookupNode> parent = node.parent().orNull();
                     if (parent == null) {
                         break;
                     }
                     if (! parent.get().remove(parent.label(), node)) {
                         break;
                     }
                     node = parent.get();
                 }
             }
         }
         return prev;
     }
 
     public Set<Identifier> keySet() {
         return byVolumeId.keySet();
     }
     
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                 .addValue(Iterators.toString(asTrie().iterator()))
                 .toString();
     }
     
     protected static class VolumeLookupNode extends ZNodeLabelTrie.DefaultsNode<VolumeLookupNode> implements Reference<Volume> {
     
         public static VolumeLookupNode root() {
             return new VolumeLookupNode(
                     Optional.<ZNodeLabelTrie.Pointer<VolumeLookupNode>>absent());
         }
         
         protected AtomicReference<Volume> value;
     
         protected VolumeLookupNode(
                 Optional<ZNodeLabelTrie.Pointer<VolumeLookupNode>> parent) {
             this(null, parent);
         }
         
         protected VolumeLookupNode(
                 Volume value,
                 Optional<ZNodeLabelTrie.Pointer<VolumeLookupNode>> parent) {
             super(parent);
             this.value = new AtomicReference<Volume>(value);
         }
         
         @Override
         public Volume get() {
             return value.get();
         }
         
         public Volume getAndSet(Volume value) {
             return this.value.getAndSet(value);
         }
         
         public Volume set(Volume volume) {
             Volume prev = value.get();
             if (value.compareAndSet(prev, volume)) {
                 return prev;
             } else {
                 return set(volume);
             }
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this)
                     .add("path", path())
                     .add("children", keySet())
                     .add("volume", get())
                     .toString();
         }
 
         protected VolumeLookupNode newChild(ZNodeLabel.Component label) {
             ZNodeLabelTrie.Pointer<VolumeLookupNode> pointer = ZNodeLabelTrie.SimplePointer.of(label, this);
             return new VolumeLookupNode(Optional.of(pointer));
         }
     }
 }
