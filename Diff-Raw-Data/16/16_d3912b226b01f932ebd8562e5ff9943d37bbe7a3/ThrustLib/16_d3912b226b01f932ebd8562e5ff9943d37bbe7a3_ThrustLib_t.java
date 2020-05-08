 package gpugen;
 
 import com.googlecode.javacpp.*;
 import com.googlecode.javacpp.annotation.*;
 
 import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
 
 @Platform(include = {
         "<thrust/device_vector.h>",
         "<thrust/transform.h>",
         "<thrust/sequence.h>",
         "<thrust/copy.h>",
         "<thrust/fill.h>",
         "<thrust/replace.h>",
         "<thrust/functional.h>",
         "/host/Keldysh/prj/Scalan-v2/tmp/fun.cu"},
         includepath = {"/opt/cuda/include/"})
 public class ThrustLib {
     static { Loader.load(); }
 
    @Name("thrust::host_vector<int>")
    public static class HostVectorPointer extends Pointer {
        static { Loader.load(); }

        public HostVectorPointer() { allocate(); }

        public HostVectorPointer(long n) { allocate(n); }

        private native void allocate();

        private native void allocate(long n);
    }

     @Name("thrust::device_vector<int>")
     public static class DeviceVectorPointer extends Pointer {
         static { Loader.load(); }
 
         public DeviceVectorPointer() { allocate(); }
 
         public DeviceVectorPointer(Pointer p) { super(p); }
 
         @Name("operator=") public native @ByRef
         DeviceVectorPointer copy(@ByRef DeviceVectorPointer x);
 
         public DeviceVectorPointer(long n) { allocate(n); }
 
         private native void allocate();
 
         private native void allocate(long n);
 
         public native long size();
 
         //public native @Index(1) void resize(long n);
 
         public native @Cast("bool") boolean empty();
 
         public native void resize(long n);
 
         @Name("operator[]") public native int get(long n);
 
         //public native @Adapter("thrust::device_vector<int>") PointerPointer at(long n);
 
         public native void push_back(int x);
 
         public native void pop_back();
 
         public native int front();
     }
 
     @Name("thrust::tuple<int, int>")
     public static class TupleIntInt extends Pointer {
         static { Loader.load(); }
 
         public TupleIntInt() { allocate(); }
 
         private native void allocate();
     }
 
     /*
     @Name("Test")
     public static class TestClass extends Pointer {
         static { Loader.load(); }
 
         public TestClass() { allocate(); }
 
         private native void allocate();
 
         public native void test(DeviceVectorPointer x, DeviceVectorPointer x_out);
 
         public native void test1();
 
         public native void test2(@ByRef TupleIntInt tII);
 
         public native @ByPtr DeviceVectorPointer test3(DeviceVectorPointer x);
     }
     */
 
     /*
     @Name("someGlobFun")
     public native static void someGlobFun();
     */
 
     @Name("test")
     //public native static @ByPtr DeviceVectorPointer test(@ByPtr DeviceVectorPointer dvp);
     public native static int test(@ByPtr DeviceVectorPointer dvp);
 
     @Name("thrust::make_tuple<int, int>")
     public native static @ByVal TupleIntInt makeTuple(int a, int b);
 
     @Name("thrust::get<0>")
     public native static int get0(@ByRef TupleIntInt tII);
 
     @Name("thrust::get<1>")
     public native static int get1(@ByRef TupleIntInt tII);
 
     public static void main(String... args) {
         PrintStream o = System.out;
 
         //System.setProperty("java.library.path", System.getProperty("java.library.path") + ":/host/Keldysh/prj/Scalan-v2/tmp");
         o.println(System.getProperty("java.library.path"));
         //o.println("hi");
 
         DeviceVectorPointer vp_in = new DeviceVectorPointer();
         o.println(vp_in.empty());
         o.println(vp_in.size());
 //        vp_in.resize(10);
 //        o.println(vp_in.empty());
 //        o.println(vp_in.size());
 
         vp_in.push_back(100);
         vp_in.push_back(20);
         vp_in.push_back(30);
         for (int i = 11; i < 28; i++) {
             vp_in.push_back(i);
         }
 
         o.println(vp_in.front());
         o.print("vp_in: ");
         for (int i = 0; i < vp_in.size(); i++) {
             o.print("(" + i + " | " + vp_in.get(i) + ") ");
         }
         o.println();
     }
 }
