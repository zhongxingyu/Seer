 package semanticcompression;
 
 import moa.AbstractMOAObject;
 import moa.core.InstancesHeader;
 import moa.streams.InstanceStream;
 import weka.core.Instance;
 
 /**
  * Package: semanticcompression
  * Class: StripClassifiedStream
  * Description: Strips classified outputs from the stream it wraps, so that classifiers will not have that data to use (if that column has already been compressed).
  */
 public class StripClassifiedStream extends AbstractMOAObject implements InstanceStream {
     InstanceStream _stream;
     int[] _classified;
 
     public StripClassifiedStream(InstanceStream stream, int[] classified) {
         if (stream.isRestartable())
             stream.restart();
 
         _stream = stream;
         _classified = classified;
 
         restart();
     }
 
     public InstancesHeader getHeader() {
         return _stream.getHeader();
     }
 
     public long estimatedRemainingInstances() {
         return _stream.estimatedRemainingInstances();
     }
 
     public boolean hasMoreInstances() {
         return _stream.hasMoreInstances();
     }
 
     public Instance nextInstance() {
         Instance inst = _stream.nextInstance();
         return stripClassified(inst);
     }
 
     public boolean isRestartable() {
         return _stream.isRestartable();
     }
 
     public void restart() {
         _stream.restart();
     }
 
     public void getDescription(StringBuilder sb, int indent) {}
 
     private Instance stripClassified(Instance inst) {
         for (int i = 0; i < _classified.length && _classified[i] != 0; i++) {
            //inst.setMissing(i);
            inst.setValue(i, 0);
         }
         return inst;
     }
 }
