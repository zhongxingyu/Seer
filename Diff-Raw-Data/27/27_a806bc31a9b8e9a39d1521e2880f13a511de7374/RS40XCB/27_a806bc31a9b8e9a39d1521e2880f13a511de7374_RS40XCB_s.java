 package RS40XCB_Java;
 
 // author: atsushi egashira
 // license: LGPLv3
//license: LGPLv3
 
 import java.io.IOException;
 
 import Serial.Serial;
 import Serial.SerialException;
 
 public class RS40XCB {
 
 	private static Serial serial;
 	private byte[] sendbuf = new byte[32];
 	private byte[] readbuf = new byte[32];
 
 	// /**
 	// * @param args
 	// */
 	// public static void main(String[] args) {
 	// // TODO Auto-generated method stub
 	//
 	// }
 
 	/**
 	 * @param portname
 	 *            portname
 	 * @param baudrate
 	 *            baudrate
 	 */
 	public RS40XCB(String portname, int baudrate) {
 		try {
 			serial = new Serial(portname, baudrate, 'N', 8, 1);
 			serial.clear();
 		} catch (SerialException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Move the servo to the specified angle When you watch servo from a top, a
 	 * clockwise rotation is a positive value and a counterclockwise rotation is
 	 * a negative value. Angle can be specified in units of 0.1 degrees. Time
 	 * can be specified in units of 10 ms(Max error of 0.5%)
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @param sPos
 	 *            specified angle
 	 * @param sTime
 	 *            specifued time
 	 */
 	public void move(int sId, int sPos, int sTime) {
 		byte sum;
 
 		// create packet
 		sendbuf[0] = (byte) 0xFA; // header1
 		sendbuf[1] = (byte) 0xAF; // header2
 		sendbuf[2] = (byte) sId; // servoID
 		sendbuf[3] = (byte) 0x00; // flag
 		sendbuf[4] = (byte) 0x1E; // address(0x1E=30)
 		sendbuf[5] = (byte) 0x04; // length(4unsigned char)
 		sendbuf[6] = (byte) 0x01; // number
 		sendbuf[7] = (byte) (sPos & 0x00FF); // locate
 		sendbuf[8] = (byte) ((sPos & 0xFF00) >> 8); // locate
 		sendbuf[9] = (byte) (sTime & 0x00FF); // time
 		sendbuf[10] = (byte) ((sTime & 0xFF00) >> 8); // time
 		// calculate checksum
 		sum = sendbuf[2];
 		for (int i = 3; i < 11; i++) {
 			sum = (byte) (sum ^ sendbuf[i]);
 		}
 		sendbuf[11] = sum; // checksum
 
 		// send
 		for (int i = 0; i < 12; i++)
 			serial.write(sendbuf[i]);
 	}
 
 	/**
 	 * This function flip of a switch of torque of servo.
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @param sMode
 	 *            If sMode value is true, torque is ON.
 	 */
 	public void torque(int sId, boolean sMode) {
 		byte sum;
 
 		// create packet
 		sendbuf[0] = (byte) (0xFA); // header1
 		sendbuf[1] = (byte) (0xAF); // header2
 		sendbuf[2] = (byte) (sId); // servoID
 		sendbuf[3] = (byte) (0x00); // flag
 		sendbuf[4] = (byte) (0x24); // address(0x24=36)
 		sendbuf[5] = (byte) (0x01); // length(4byte)
 		sendbuf[6] = (byte) (0x01); // // number
 		if (sMode)
 			sendbuf[7] = (byte) (0x001); // ON/OFFflag
 		else
 			sendbuf[7] = (byte) (0x0000);
 
 		// calculate cecksum
 		sum = sendbuf[2];
 		for (int i = 3; i < 8; i++) {
 			sum = (byte) (sum ^ sendbuf[i]);
 		}
 		sendbuf[8] = sum; // checksum
 
 		// send
 		for (int i = 0; i < 9; i++)
 			serial.write(sendbuf[i]);
 	}
 
 	/**
 	 * This function can get current angle of unit of 0.1 degrees. If movable
 	 * range of center is 0, clockwise rotation range is 150 degrees and
 	 * counterclockwise rotation range is -150 degrees.
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @return Current angle
 	 */
 	public int getAngle(int sId) {
 		this.getParam(sId);
		return ((readbuf[8] << 8) & 0x0000FF00) | (readbuf[7] & 0x000000FF);
 	}
 
 	/**
 	 * The lapsed time after starting movement is obtained in the unit for 10
 	 * ms. The last time will be held if movement is completed.
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @return Elapsed time
 	 */
 	public int getTime(int sId) {
 		this.getParam(sId);
		return ((readbuf[10] << 8) & 0x0000FF00) | (readbuf[9] & 0x000000FF);
 	}
 
 	/**
 	 * This function can get current speed of unit of deg/sec. This speed is the
 	 * speed of the moment.
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @return current speed
 	 */
 	public int getSpeed(int sId) {
 		this.getParam(sId);
		return ((readbuf[12] << 8) & 0x0000FF00) | (readbuf[11] & 0x000000FF);
 	}
 
 	/**
 	 * This function can get load of unit of mA.
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @return current load
 	 */
 	public int getLoad(int sId) {
 		this.getParam(sId);
		return ((readbuf[14] << 8) & 0x0000FF00) | (readbuf[13] & 0x000000FF);
 	}
 
 	/**
 	 * This function can get of temperature of basal plate. The temperature
 	 * sensor has a degree of error of + -3 due to individual differences.
 	 * Protective function is activated by temperature and need to reset the
 	 * servo.
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @return current tempreture
 	 */
 	public int getTemperature(int sId) {
 		this.getParam(sId);
		return ((readbuf[16] << 8) & 0x0000FF00) | (readbuf[15] & 0x000000FF);
 	}
 
 	/**
 	 * This function can get supply voltage of unit of 10mV.. There is an error of about + -
 	 * 0.3V.
 	 * 
 	 * @param sId
 	 *            servoID
 	 * @return supply voltage
 	 */
 	public int getVoltage(int sId) {
 		this.getParam(sId);
		return ((readbuf[18] << 8) & 0x0000FF00) | (readbuf[17] & 0x000000FF);
 	}
 
 	private void getParam(int sId) {
 		byte sum;
 
 		// create packet
 		sendbuf[0] = (byte) 0xFA; // header1
 		sendbuf[1] = (byte) 0xAF; // header2
 		sendbuf[2] = (byte) sId; // servoID
 		sendbuf[3] = (byte) 0x09; // flag(0x01 | 0x04<<1)
 		sendbuf[4] = (byte) 0x00; // address(0x00)
 		sendbuf[5] = (byte) 0x00; // length(0byte)
 		sendbuf[6] = (byte) 0x01; // number
 		// calculate cecksum
 		sum = sendbuf[2];
 		for (int i = 3; i < 7; i++) {
 			sum = (byte) (sum ^ sendbuf[i]);
 		}
 		sendbuf[7] = sum; // checksum
 
 		// write
 		for (int i = 0; i < 8; i++)
 			serial.write(sendbuf[i]);
 
 		// wait
 		for (int i = 0; i < 10; i++) {
 			if (serial.available() >= 26)
 				break;
 
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// read
 		for (int i = 0; i < 26; i++)
 			readbuf[i] = (byte) (serial.read() & 0xff);
 
 		// if (len < 26) {
 		// // ��M�G���[
 		// System.out.println("��M�G���[");
 		// // return -2;
 		// }
 
 		// ��M�f�[�^�̊m�F
 		// sum = readbuf[2];
 		// for (i = 3; i < 26; i++) {
 		// sum = sum ^ readbuf[i];
 		// }
 		// if (sum) {
 		// // �`�F�b�N�T���G���[
 		// return -3;
 		// }
 
 	}
 
 }
