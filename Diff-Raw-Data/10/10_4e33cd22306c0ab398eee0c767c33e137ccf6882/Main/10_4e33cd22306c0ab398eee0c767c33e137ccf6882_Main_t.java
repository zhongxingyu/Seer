 import org.hyperic.sigar.NetFlags;
 import org.hyperic.sigar.NetInterfaceConfig;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Timophey.Kulishov
  * @version $Rev$ <br/>
  *          $Author$ <br/>
  *          $Date$
  */
 public class Main {
     public static void main(String[] args) throws SigarException, UnknownHostException {
 //        try (FileOutputStream fileOutputStream = new FileOutputStream("d:\\workfolder\\TestCar.obj");
 //             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
 //            Car car = new Car(1, Color.BLACK, "BMV");
 //            objectOutputStream.writeObject(car);
 //        } catch (IOException e) {
 //            e.printStackTrace();
 //        }
 //        try (FileInputStream fileInputStream = new FileInputStream("d:\\workfolder\\TestCar.obj");
 //            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
 //            Car car = (Car) objectInputStream.readObject();
 //            System.out.println(car.getModel());
 //        } catch (IOException e) {
 //            e.printStackTrace();
 //        } catch (ClassNotFoundException e) {
 //            e.printStackTrace();
 //        }
 //        Super ssuper = new Super();
 ////        System.out.println(Color.values()[1]);
 //        int i = 10;
 //        Set<Object> set = new HashSet<>();
 //        set.add(i);
 //        assert true;
 //        System.out.println("hi");
 
        Set<? extends RuntimeException> set = new HashSet<RuntimeException>();
        Car car = new Car(2, Color.BLACK, "BMW");
        car = null;
        System.gc();
 //        while (Car.getCarCount() > 0) {
 //
 //        }
 
 
 
 
     }
 
 }
