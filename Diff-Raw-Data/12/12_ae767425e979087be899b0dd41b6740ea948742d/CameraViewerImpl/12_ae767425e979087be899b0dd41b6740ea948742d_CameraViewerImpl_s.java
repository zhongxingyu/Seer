 // -*- Java -*-
 /*!
  * @file  CameraViewerImpl.java
  * @brief Camera Viewer
  * @date  $Date$
  *
  * $Id$
  */
 
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JFrame;
 
 import jp.go.aist.rtm.RTC.DataFlowComponentBase;
 import jp.go.aist.rtm.RTC.Manager;
 import jp.go.aist.rtm.RTC.port.InPort;
 import jp.go.aist.rtm.RTC.util.DataRef;
 import RTC.CameraImage;
 import RTC.ReturnCode_t;
 
 /*!
  * @class CameraViewerImpl
  * @brief Camera Viewer
  *
  */
 public class CameraViewerImpl extends DataFlowComponentBase {
 
   private JFrame frame;
 private CameraViewPanel panel;
 private BufferedImage img;
 	/*!
    * @brief constructor
    * @param manager Maneger Object
    */
 	public CameraViewerImpl(Manager manager) {  
         super(manager);
         // <rtc-template block="initializer">
         m_in_val = new CameraImage();
         m_in = new DataRef<CameraImage>(m_in_val);
         m_inIn = new InPort<CameraImage>("in", m_in);
         // </rtc-template>
 
     }
 
     /**
      *
      * The initialize action (on CREATED->ALIVE transition)
      * formaer rtc_init_entry() 
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
     @Override
     protected ReturnCode_t onInitialize() {
         // Registration: InPort/OutPort/Service
         // <rtc-template block="registration">
         // Set InPort buffers
         addInPort("in", m_inIn);
         // </rtc-template>
         return super.onInitialize();
     }
 
     /***
      *
      * The finalize action (on ALIVE->END transition)
      * formaer rtc_exiting_entry()
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //    @Override
 //    protected ReturnCode_t onFinalize() {
 //        return super.onFinalize();
 //    }
 
     /***
      *
      * The startup action when ExecutionContext startup
      * former rtc_starting_entry()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //    @Override
 //    protected ReturnCode_t onStartup(int ec_id) {
 //        return super.onStartup(ec_id);
 //    }
 
     /***
      *
      * The shutdown action when ExecutionContext stop
      * former rtc_stopping_entry()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //    @Override
 //    protected ReturnCode_t onShutdown(int ec_id) {
 //        return super.onShutdown(ec_id);
 //    }
 
     /***
      *
      * The activated action (Active state entry action)
      * former rtc_active_entry()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
     @Override
     protected ReturnCode_t onActivated(int ec_id) {
     	frame = new JFrame("CapturedImage");
     	panel = new CameraViewPanel();
     	
     	frame.setContentPane(panel);
     	panel.setSize(new Dimension(480, 360));
     	
     	frame.setSize(new Dimension(500, 400));
     	frame.setVisible(true);
         return super.onActivated(ec_id);
     }
 
     /***
      *
      * The deactivated action (Active state exit action)
      * former rtc_active_exit()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
     @Override
     protected ReturnCode_t onDeactivated(int ec_id) {
     	frame.setVisible(false);
     	frame = null;
     	img = null;
         return super.onDeactivated(ec_id);
     }
 
     /***
      *
      * The execution action that is invoked periodically
      * former rtc_active_do()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
     @Override
     protected ReturnCode_t onExecute(int ec_id) {
     	if (this.m_inIn.isNew()) {
     		m_inIn.read();
    		System.out.println("Width  =" + m_in.v.width);
    		System.out.println("Height =" + m_in.v.height);
     		if (img == null) {
     			img = new BufferedImage(m_in.v.width, m_in.v.height, BufferedImage.TYPE_INT_RGB);
     		}
     		for (int y = 0;y < m_in.v.height;y++) {
     			for (int x = 0;x < m_in.v.width;x++) {
     				int index = y * m_in.v.width + x;
    				byte r = m_in.v.pixels[index*3 + 2];
     				byte g = m_in.v.pixels[index*3 + 1];
    				byte b = m_in.v.pixels[index*3 + 0];
    				int rgb = (int)r << 16 | (int)g << 8 | (int)b;
     				img.setRGB(x, y, rgb);
     			}
     		}
     		panel.img = img;
     		panel.repaint();
     	}
         return super.onExecute(ec_id);
     }
 
     /***
      *
      * The aborting action when main logic error occurred.
      * former rtc_aborting_entry()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //  @Override
 //  public ReturnCode_t onAborting(int ec_id) {
 //      return super.onAborting(ec_id);
 //  }
 
     /***
      *
      * The error action in ERROR state
      * former rtc_error_do()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //    @Override
 //    public ReturnCode_t onError(int ec_id) {
 //        return super.onError(ec_id);
 //    }
 
     /***
      *
      * The reset action that is invoked resetting
      * This is same but different the former rtc_init_entry()
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //    @Override
 //    protected ReturnCode_t onReset(int ec_id) {
 //        return super.onReset(ec_id);
 //    }
 
     /***
      *
      * The state update action that is invoked after onExecute() action
      * no corresponding operation exists in OpenRTm-aist-0.2.0
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //    @Override
 //    protected ReturnCode_t onStateUpdate(int ec_id) {
 //        return super.onStateUpdate(ec_id);
 //    }
 
     /***
      *
      * The action that is invoked when execution context's rate is changed
      * no corresponding operation exists in OpenRTm-aist-0.2.0
      *
      * @param ec_id target ExecutionContext Id
      *
      * @return RTC::ReturnCode_t
      * 
      * 
      */
 //    @Override
 //    protected ReturnCode_t onRateChanged(int ec_id) {
 //        return super.onRateChanged(ec_id);
 //    }
 //
     // DataInPort declaration
     // <rtc-template block="inport_declare">
     protected CameraImage m_in_val;
     protected DataRef<CameraImage> m_in;
     /*!
      */
     protected InPort<CameraImage> m_inIn;
 
     
     // </rtc-template>
 
     // DataOutPort declaration
     // <rtc-template block="outport_declare">
     
     // </rtc-template>
 
     // CORBA Port declaration
     // <rtc-template block="corbaport_declare">
     
     // </rtc-template>
 
     // Service declaration
     // <rtc-template block="service_declare">
     
     // </rtc-template>
 
     // Consumer declaration
     // <rtc-template block="consumer_declare">
     
     // </rtc-template>
 
 
 }
