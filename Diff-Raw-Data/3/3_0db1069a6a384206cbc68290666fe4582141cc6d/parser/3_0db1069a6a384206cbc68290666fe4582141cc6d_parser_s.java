 package kickflick.utility;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 import kickflick.device.*;
 
 public class parser implements SerialPortEventListener {
 	private server Server_;
 	
 	public parser(server Serv)
 	{
 		System.out.println("Create Parser");
 		this.Server_=Serv;
 	}
 	
 	public void parse(byte[] arg)
 	{
 		System.out.print("Parser received message: ");
         System.out.println(Arrays.toString(arg));
 		if (arg.length == 4) // must contain at least sender receiver and key
 		{
 			if ( !this.Server_.get_devices().isEmpty()) // if NOT emtpy
 			{
                 int index;
                 if (arg[0] % 2 == 0)
                     index = find_device_sensor_node(arg[0]);
                 else if ( arg[0] % 2 == 1)
                     index = find_device_sensor_node(arg[0]);
                 else
                 {
                     System.err.println("Parser Error: incorrect Packet Sender ID");
                     return;
                 }
 
                 if (index != -1)
                 {
                     //TODO stuff
                 }
 			}
 			else    //if empty -> create new device and fill
 			{
                 device tmp = new device ( new personality(), (byte) 0);
 
                 if (arg[0] % 2 == 0)
                 {
                     tmp.set_sensor_node(arg[0]);
                     tmp.set_actuator_node(arg[0]++);     //actuator is next to sensor node
                 }
                 else if ( arg[0] % 2 == 1)
                 {
                     tmp.set_actuator_node(arg[0]);
                     tmp.set_sensor_node(arg[0]--);      //sensor is next to actuator node
                 }
 cd P                {
                     System.err.println("Parser Error: incorrect Packet Sender ID");
                     return;
                 }
                 this.Server_.get_devices().add( tmp );
 			}
 		}
 		else {
 			System.err.println("Parser received empty message!");
 		}
 	}
 	
 	public int find_device_sensor_node(byte address)
 	{
 		for ( int i = 0; i < this.Server_.get_devices().size() ; ++i)
 			if ( this.Server_.get_device(i).get_sensor_node() == address)
 				return i;
 		//found nothing
 		return -1;
 	}
 
     public int find_device_actuator_node(byte address)
     {
         for ( int i = 0 ; i < this.Server_.get_devices().size() ; ++i )
             if ( this.Server_.get_device(i).get_actuator_node() == address)
                 return i;
         return -1; //found nothing
     }
 
 	@Override
 	public void serialEvent(SerialPortEvent arg0) {
         System.out.println("INCOMMING!!!");
 		try {
 			serial_lib.com_listener horcher = new serial_lib.com_listener(
 					this.Server_.serial_com, this.Server_.serial_com.get_inputstream()
 					);
 			Thread thread = new Thread(horcher);
 			thread.start();
             try
             {
                 thread.join();
             }
             catch(InterruptedException e)
             {
                 e.fillInStackTrace();
             }
 
 			this.parse(horcher.get_buffer());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
