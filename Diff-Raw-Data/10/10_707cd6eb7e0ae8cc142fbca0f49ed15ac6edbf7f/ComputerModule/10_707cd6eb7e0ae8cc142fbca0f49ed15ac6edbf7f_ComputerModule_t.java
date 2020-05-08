 package xhl.examples.computer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import xhl.core.GenericModule;
 import xhl.core.elements.Symbol;
 
 public class ComputerModule extends GenericModule {
 
     private static class Speed {
         public int speed;
 
         public Speed(int speed) {
             this.speed = speed;
         }
     }
 
     private static class Cores {
         public int cores;
 
         public Cores(int cores) {
             this.cores = cores;
         }
     }
 
     private static class Size {
         public int size;
 
         public Size(int size) {
             this.size = size;
         }
     }
 
     private Computer computer = null;
 
     public ComputerModule() {
         for (Processor.Type type : Processor.Type.values()) {
             addSymbol(new Symbol(type.name()), type);
         }
        for (Disk.Interface iface : Disk.Interface.values()) {
             addSymbol(new Symbol(iface.name()), iface);
         }
     }
 
     public Computer getComputer() {
         return computer;
     }
 
     @Function
     public void computer(Object... components) {
         Processor processor = null;
         List<Disk> disks = new ArrayList<Disk>();
         for (Object comp : components) {
             if (comp instanceof Processor) {
                 processor = (Processor) comp;
             } else if (comp instanceof Disk) {
                 disks.add((Disk) comp);
             }
         }
         Processor.Type.values();
         Disk[] diskaArray = disks.toArray(new Disk[disks.size()]);
         computer = new Computer(processor, diskaArray);
     }
 
     @Function
     public Processor processor(Object... properties) {
         Processor.Type type = null;
         int speed = Processor.UNKNOWN_SPEED;
         int cores = 1;
         for (Object prop : properties) {
             if (prop instanceof Processor.Type) {
                 type = (Processor.Type) prop;
             } else if (prop instanceof Speed) {
                 speed = ((Speed) prop).speed;
             } else if (prop instanceof Cores) {
                cores = ((Cores) prop).cores;
             }
         }
         return new Processor(type, speed, cores);
     }
 
     @Function
     public Disk disk(Object... properties) {
         int size = Disk.UNKNOWN_SIZE;
         int speed = Disk.UNKNOWN_SPEED;
         Disk.Interface iface = null;
         for (Object prop : properties) {
             if (prop instanceof Size) {
                 size = ((Size) prop).size;
             } else if (prop instanceof Speed) {
                 speed = ((Speed) prop).speed;
             } else if (prop instanceof Disk.Interface) {
                 iface = (Disk.Interface) prop;
             }
         }
         return new Disk(size, speed, iface);
     }
 
     @Function
     public Processor.Type type(Processor.Type type) {
         return type;
     }
 
    @Function(name = "interface")
     public Disk.Interface interface_(Disk.Interface iface) {
         return iface;
     }
 
     @Function
     public Speed speed(double speed) {
         return new Speed((int) speed);
     }
 
     @Function
     public Size size(double size) {
         return new Size((int) size);
     }
 
     @Function
     public Cores cores(double cores) {
         return new Cores((int) cores);
     }
 }
